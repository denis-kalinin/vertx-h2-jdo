package com.x.services;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import com.github.jknack.handlebars.Template;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.x.LogbackInit;
import com.x.di.AccountModule;
import com.x.models.Account;
import com.x.models.Balance;
import com.x.models.ServiceMessage;
import com.x.models.Transfer;
import com.x.util.NetworkUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG;
	
	static {
		LogbackInit.start();
		LOG = org.slf4j.LoggerFactory.getLogger(MainVerticle.class);
	}
	

	private DeploymentOptions workerDeploymentOptions = new DeploymentOptions().setWorker(true);
	
	@Inject
	private MessageCodec<Account, Account> accountCodec;
	
	@Inject
	private MessageCodec<Transfer, Transfer> transferCodec;
	
	@Inject
	private MessageCodec<Balance, Balance> balanceCodec;
	
	@Inject
	private Template template;
	
	private Module guiceModule;
	
	
	@Override
	public void start(final Future<Void> startFuture){
		if(guiceModule == null) guiceModule = new AccountModule();
		Injector injector = Guice.createInjector(guiceModule);
		injector.injectMembers(this);
		LOG.trace("CODEC registered: {}", accountCodec);
		vertx.eventBus().registerDefaultCodec(Account.class, accountCodec);
		vertx.eventBus().registerDefaultCodec(Transfer.class, transferCodec);
		vertx.eventBus().registerDefaultCodec(Balance.class, balanceCodec);
		LOG.trace("Main Verticle started");
		Router router = Router.router(vertx);

		Router bankRouter = Router.router(vertx);
		bankRouter.route("/accounts").handler(BodyHandler.create());
		bankRouter.route("/accounts*").handler(BodyHandler.create());
		bankRouter.route("/transfers*").handler(BodyHandler.create());
		bankRouter.route("/balance").handler(BodyHandler.create());
		bankRouter.get("/accounts").handler(this :: getAccounts);
		bankRouter.get("/accounts/:id").handler(this :: getAccountById);
		bankRouter.post("/accounts").handler(this :: addAccount);
		bankRouter.post("/transfers").handler(this :: sendTransfer);
		bankRouter.get("/transfers").handler(this :: getTransfers);
		bankRouter.get("/transfers/:id").handler(this :: getTransferById);
		bankRouter.get("/balance").handler(this :: getBalance);
		
		int port = 80;
		int httpPort = config().getInteger("http.port", 80);
		LOG.trace("http.port from config: {}", httpPort);
		if(httpPort == port){
			if(!NetworkUtils.isPortAvailable(httpPort, null)){
				try {
					port = NetworkUtils.getEphemeralPort();
					LOG.warn("Port {} is not available. Ephimeral port choosen to start HTTP server: {}", httpPort, port);
				} catch (Exception e) {
					RuntimeException newE = new RuntimeException("Failed to assign ephimeral port", e);
					LOG.error(newE.getMessage());
					startFuture.fail(newE);
					throw newE;
				}
			}
		} else {
			port = httpPort;
		}
		
		vertx.deployVerticle(com.x.services.SubVerticle.class.getName());
		//vertx.deployVerticle(injector.getInstance(com.x.services.AccountVerticle.class), workerDeploymentOptions);
		vertx.deployVerticle(injector.getInstance(AccountVerticle.class), workerDeploymentOptions, ar -> {
			if(ar.succeeded()){
				startFuture.complete();
			}else{
				LOG.error("Account API deployment failed", ar.cause());
				startFuture.fail(ar.cause());
			}
		});
		//mount bank API
		router.mountSubRouter("/bank", bankRouter);
		//make raml files accessible
		//router.route("/raml/*").handler(StaticHandler.create("raml").setCachingEnabled(false));
		router.get("/raml/accounts.yaml").handler( this :: getYamlHandlebars);
		router.route("/raml/*").handler(StaticHandler.create("META-INF/raml").setCachingEnabled(false));
		//register raml console
		StaticHandler staticHandler = StaticHandler.create("META-INF/webroot").setFilesReadOnly(true)
			.setCachingEnabled(true).setEnableFSTuning(true);
		router.route("/raml-console/*").handler(staticHandler);
		//redirect root to raml console
		router.get("/").handler(rc -> {
			LOG.trace("index page");
			rc.response().setStatusCode(302).putHeader("Location", "/raml-console/?raml=/raml/accounts.yaml").end();
		});
		LOG.info("HttpServert binding to port {}", port);
		HttpServer httpServer = vertx.createHttpServer()
		.requestHandler(router::accept)
		.listen(port);
		
		//LOG.info("Http server is listening on port {}", httpServer.);
	}
	/**
	 * Substitute embedded Guice module with <code>customModule</code>.
	 * @param customModule to substitute default one.
	 */
	public void setGuiceModule(Module customModule){
		this.guiceModule = customModule;
	}
	///////////////////ROUTE HANDLERS///////////////////
	protected void getAccounts(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getAll");
		vertx.eventBus().send("accounts", null, deliveryOptions, result -> {
			if(result.succeeded()){
				rc.response().putHeader("Content-Type", "application/json")
					.end((String)result.result().body());
			}else if(result.failed()){
				LOG.error("Failed: {}", result.cause());
			}
		});
	}
	
	protected void getAccountById(RoutingContext rc){
		String id = rc.request().getParam("id");
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "get");
		vertx.eventBus().send("accounts", id, deliveryOptions, result -> {
				if(result.succeeded()){
					rc.response().putHeader("Content-Type", "application/json")
						.end((String) result.result().body());
				}else if (result.failed()){
					int status = 400;
					if(result.cause() instanceof ReplyException){
						int replyStatus = ((ReplyException) result.cause()).failureCode();
						status = replyStatus > 0 ? replyStatus : status;
					}
					ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
					rc.response().setStatusCode(status)
						.putHeader("content-type", "application/json")
						.end(Json.encodePrettily(msg));
				} else {
					rc.response().setStatusCode(500)
					.putHeader("content-type", "application/json")
					.end(Json.encodePrettily(new ServiceMessage(null, "Server error!")));
				}
		});
	}
	
	protected void addAccount(RoutingContext rc){
		Account account =  Json.decodeValue(rc.getBodyAsString(), Account.class);
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "addAccount");
		vertx.eventBus().send("accounts", account, deliveryOptions, result -> {
			if(result.succeeded()){
				Account createdAccount = (Account) result.result().body();
				rc.response().setStatusCode(201).putHeader("Content-Type", "application/json")
					.putHeader("Location", "accounts/"+createdAccount.getId())
					.end(Json.encodePrettily(createdAccount));
			}else if (result.failed()){
				int status = 400;
				if(result.cause() instanceof ReplyException){
					int replyStatus = ((ReplyException) result.cause()).failureCode();
					status = replyStatus > 0 ? replyStatus : status;
				}
				ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
				rc.response().setStatusCode(status)
					.putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(msg));
			} else {
				rc.response().setStatusCode(204).end();
			}
		});
	}
	
	protected void sendTransfer(RoutingContext rc) {
		Transfer transfer = Json.decodeValue(rc.getBodyAsString(), Transfer.class);
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "send");
		vertx.eventBus().send("transfers", transfer, deliveryOptions, result -> {
			if(result.succeeded()){
				Transfer commitedTransfer = (Transfer) result.result().body();
				rc.response().setStatusCode(201).putHeader("Content-Type", "application/json")
					.putHeader("Location", "transfers/"+commitedTransfer.getId())
					.end(Json.encodePrettily(commitedTransfer));
			}else if (result.failed()){
				int status = 400;
				if(result.cause() instanceof ReplyException){
					int replyStatus = ((ReplyException) result.cause()).failureCode();
					status = replyStatus > 0 ? replyStatus : status;
				}
				LOG.error("{}", result.cause());
				ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
				rc.response().setStatusCode(status)
					.putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(msg));
			} else {
				rc.response().setStatusCode(500)
				.putHeader("Content-Type", "application/json")
				.end(Json.encodePrettily(new ServiceMessage(null, "Server error!")));
			}
		});
	}
	
	protected void getTransferById(RoutingContext rc){
		String id = rc.request().getParam("id");
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "get");
		vertx.eventBus().send("transfers", id, deliveryOptions, result -> {
			if(result.succeeded()){
				Transfer savedTransfer = (Transfer) result.result().body();
				rc.response().putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(savedTransfer));
			}else if (result.failed()){
				int status = 400;
				if(result.cause() instanceof ReplyException){
					int replyStatus = ((ReplyException) result.cause()).failureCode();
					status = replyStatus > 0 ? replyStatus : status;
				}
				ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
				rc.response().setStatusCode(status)
					.putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(msg));
			} else {
				rc.response().setStatusCode(500)
				.putHeader("Content-Type", "application/json")
				.end(Json.encodePrettily(new ServiceMessage(null, "Server error!")));
			}
		});
	}
	
	protected void getTransfers(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getAll");
		vertx.eventBus().send("transfers", null, deliveryOptions, result -> {
			if(result.succeeded()){
				rc.response().putHeader("Content-Type", "application/json")
					.end((String)result.result().body());
			}else if(result.failed()){
				int status = 400;
				if(result.cause() instanceof ReplyException){
					int replyStatus = ((ReplyException) result.cause()).failureCode();
					status = replyStatus > 0 ? replyStatus : status;
				}
				ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
				rc.response().setStatusCode(status)
					.putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(msg));
			}
		});
	}
	
	protected void getBalance(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getBalance");
		vertx.eventBus().send("accounts", null, deliveryOptions, result -> {
			if(result.succeeded()){
				Balance balance = (Balance) result.result().body();
				rc.response().putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(balance));
			}else if (result.failed()){
				int status = 400;
				if(result.cause() instanceof ReplyException){
					int replyStatus = ((ReplyException) result.cause()).failureCode();
					status = replyStatus > 0 ? replyStatus : status;
				}
				ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
				rc.response().setStatusCode(status)
					.putHeader("Content-Type", "application/json")
					.end(Json.encodePrettily(msg));
			} else {
				rc.response().setStatusCode(500)
				.putHeader("Content-Type", "application/json")
				.end(Json.encodePrettily(new ServiceMessage(null, "Server error!")));
			}
		});
	}
	
	protected void getYamlHandlebars(RoutingContext rc){
		try {
			String hostAndPort = rc.request().getHeader("Host");
			if(hostAndPort == null){
				throw new IOException("HTTP request for /raml/accounts.yaml MUST contain \"Host\" HTTP header!");
			}
			String yamlResult = template.apply(hostAndPort);
			rc.response().setStatusCode(200).putHeader("Content-Type", "application/x-yaml")
				.end(yamlResult);
		} catch (IOException e) {
			rc.response().setStatusCode(500).putHeader("Content-Type", "text/plain")
				.end(e.getMessage());
		}
	}
}

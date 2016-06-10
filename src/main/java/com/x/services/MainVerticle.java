package com.x.services;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.Json;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.x.di.AccountModule;
import com.x.models.Account;
import com.x.models.ServiceMessage;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MainVerticle.class);
	private DeploymentOptions workerDeploymentOptions = new DeploymentOptions()
			.setWorker(true);
	
	@Override
	public void start(Future<Void> startFuture){
		Injector injector = Guice.createInjector(new AccountModule());//.injectMembers(this);
		LOG.debug("Main Verticle started");
		Router router = Router.router(vertx);
		//router.get("/").handler( rc -> { rc.response().putHeader("content-type", "text/html").end("Hello World!");});
		
		Router bankRouter = Router.router(vertx);
		bankRouter.get("/accounts").handler(this :: getAccounts);
		bankRouter.get("/accounts/:id").handler(this :: getAccountById);
		bankRouter.put("/accounts").handler(this :: addAccount);
		
		int port = config().getInteger("http.port", 8029);
		
		vertx.deployVerticle(com.x.services.SubVerticle.class.getName());
		vertx.deployVerticle(injector.getInstance(com.x.services.AccountVerticle.class), workerDeploymentOptions);
		vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(port);
		
		router.mountSubRouter("/bank", bankRouter);
		
		Router secondRouter = Router.router(vertx);
		secondRouter.get().handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("Hello Mars!");
		});
		
		router.mountSubRouter("/mars", secondRouter);
		
		router.route("/api/*").handler(StaticHandler.create("api").setCachingEnabled(false));
				
		router.route("/api-console/*").handler(StaticHandler.create().setCachingEnabled(false));
		
		router.get("/").handler(rc -> {
			rc.response().setStatusCode(302).putHeader("Location", "/api-console/?raml=/api/accounts.yaml").end();
		});
		
		startFuture.complete();
	}
	///////////////////ROUTE HANDLERS///////////////////
	protected void getAccounts(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getAll");
		vertx.eventBus().send("accounts", null, deliveryOptions, result -> {
			if(result.succeeded()){
				rc.response().putHeader("content-type", "application/json")
					.end((String)result.result().body());
			}else if(result.failed()){
				LOG.error("Failed: {} : {}", result.result().body(), result.result().headers());
			}
		});
	}
	
	protected void getAccountById(RoutingContext rc){
		String id = rc.request().getParam("id");
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "get");
		vertx.eventBus().send("accounts", id, deliveryOptions, result -> {
				if(result.succeeded()){
					rc.response().putHeader("content-type", "application/json")
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
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		vertx.eventBus().send("addAccount", account, deliveryOptions, result ->{
			if(result.succeeded()){
				rc.response().putHeader("content-type", "application/json")
					.end(Json.encodePrettily(result.result().body()));
			}else if (result.failed()){
				ServiceMessage msg = new ServiceMessage(null, result.cause().getMessage());
				rc.response().setStatusCode(400)
					.putHeader("content-type", "application/json")
					.end(Json.encodePrettily(msg));
			} else {
				rc.response().setStatusCode(204).end();
			}
	});
	}
	
	//////////// main method if you want/////////
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainVerticle());
	}
}

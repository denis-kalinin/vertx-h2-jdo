package com.x.routers;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.jknack.handlebars.Template;
import com.x.models.Account;
import com.x.models.Balance;
import com.x.models.ServiceMessage;
import com.x.models.Transfer;
import com.x.util.JavadocHandler;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

@Singleton
public class ApiRouter implements RouterFactory{
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApiRouter.class);
	
	@Inject
	private Template template;
	
	/**
	 * @param vertx Vertx context
	 * @return http router
	 * @throws IllegalArgumentException if <code>vertx</code> argument is null
	 */
	public Router getRouter(Vertx vertx){
		if(vertx==null) throw new IllegalArgumentException("Vertx context is NULL");
		return createRouter(vertx);
	}
	
	private Router createRouter(Vertx vertx){
		Router router = Router.router(vertx);
		Router bankRouter = Router.router(vertx);
		//return body for all requests to /accounts* and /transfers* and /balance
		bankRouter.route(ApiEndpoints.ACCOUNTS.getPath()+"*").handler(BodyHandler.create());
		bankRouter.route(ApiEndpoints.TRANSFERS.getPath()+"*").handler(BodyHandler.create());
		bankRouter.route(ApiEndpoints.BALANCE.getPath()).handler(BodyHandler.create());
		
		bankRouter.get(ApiEndpoints.ACCOUNTS.getPath()).handler(ApiRouter :: getAccounts);
		bankRouter.post(ApiEndpoints.ACCOUNTS.getPath()).handler(ApiRouter :: addAccount);
		// /accounts/:id
		bankRouter.get(ApiEndpoints.ACCOUNTS.getPath()+"/:id").handler(ApiRouter :: getAccountById);
		
		bankRouter.post(ApiEndpoints.TRANSFERS.getPath()).handler(ApiRouter :: sendTransfer);
		bankRouter.get(ApiEndpoints.TRANSFERS.getPath()).handler(ApiRouter :: getTransfers);
		// /transfers/:id
		bankRouter.get(ApiEndpoints.TRANSFERS.getPath()+"/:id").handler(ApiRouter :: getTransferById);
		
		bankRouter.get(ApiEndpoints.BALANCE.getPath()).handler(ApiRouter :: getBalance);

		//mount bank API
		router.mountSubRouter(MOUNTPOINT, bankRouter);
		//main RAML file is dynamic - handle it separately from other raml-files
		router.get("/raml/accounts.yaml").handler( this :: getYamlHandlebars);

		StaticHandler staticRamlHandler = StaticHandler.create("META-INF/raml")
			.setFilesReadOnly(true).setCachingEnabled(true).setEnableFSTuning(true);
		//register static raml files
		router.route("/raml/*").handler(staticRamlHandler);

		StaticHandler apiConsoleHandler = StaticHandler.create("META-INF/webroot")
			.setFilesReadOnly(true).setCachingEnabled(true).setEnableFSTuning(true);
		//register raml console
		router.route("/raml-console/*").handler(apiConsoleHandler);
		//redirect root to raml console
		router.get("/").handler(rc -> {
			rc.response().setStatusCode(302).putHeader("Location", "/raml-console/?raml=/raml/accounts.yaml").end();
		});

		JavadocHandler.getHandler().ifPresent( handler -> {
			router.get("/javadoc/*").handler(handler);
		});
		return router;
	}
	
	///////////////////ROUTE HANDLERS///////////////////
	private static void getAccounts(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getAll");
		rc.vertx().eventBus().send("accounts", null, deliveryOptions, result -> {
			if(result.succeeded()){
				rc.response().putHeader("Content-Type", "application/json")
					.end((String)result.result().body());
			}else if(result.failed()){
				LOG.error("Failed: {}", result.cause());
			}
		});
	}
	
	private static void getAccountById(RoutingContext rc){
		String id = rc.request().getParam("id");
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "get");
		rc.vertx().eventBus().send("accounts", id, deliveryOptions, result -> {
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
	
	private static void addAccount(RoutingContext rc){
		Account account =  Json.decodeValue(rc.getBodyAsString(), Account.class);
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "addAccount");
		rc.vertx().eventBus().send("accounts", account, deliveryOptions, result -> {
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
	
	private static void sendTransfer(RoutingContext rc) {
		Transfer transfer = Json.decodeValue(rc.getBodyAsString(), Transfer.class);
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "send");
		rc.vertx().eventBus().send("transfers", transfer, deliveryOptions, result -> {
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
	
	private static void getTransferById(RoutingContext rc){
		String id = rc.request().getParam("id");
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "get");
		rc.vertx().eventBus().send("transfers", id, deliveryOptions, result -> {
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
	
	private static void getTransfers(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getAll");
		rc.vertx().eventBus().send("transfers", null, deliveryOptions, result -> {
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
	
	private static void getBalance(RoutingContext rc){
		DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("method", "getBalance");
		rc.vertx().eventBus().send("accounts", null, deliveryOptions, result -> {
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
	/**
	 * Handles method to process request for RAML-file, embedding <code>baseUri</code> 
	 * as received from <code>Host</code> HTTP-header into response. 
	 * @param rc {@linkplain RoutingContext} automatically provided by vert.x handler.
	 */
	protected void getYamlHandlebars(RoutingContext rc){
		try {
			String hostAndPort = rc.request().getHeader("Host");
			if(hostAndPort == null){
				throw new IOException("HTTP request for "+ rc.request().absoluteURI() +" MUST contain \"Host\" HTTP header!");
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

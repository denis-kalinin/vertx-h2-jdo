package com.x.services;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;

import com.google.inject.Guice;
import com.x.di.AccountModule;
import com.x.models.Account;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.core.DeploymentOptions;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MainVerticle.class);
	
	private DeliveryOptions deliveryOptions = new DeliveryOptions();
	private DeploymentOptions workerDeploymentOptions = new DeploymentOptions()
			.setWorker(true);
	
	@Override
	public void start(Future<Void> startFuture){
		LOG.debug("Main Verticle started");
		deliveryOptions.setSendTimeout(3000);
		Router router = Router.router(vertx);
		router.get("/").handler( rc -> {
			rc.response().putHeader("content-type", "text/html").end("Hello World!");
		});
		
		Router bankRouter = Router.router(vertx);
		bankRouter.get("/accounts").handler(this :: getAccounts);
		bankRouter.get("/accounts/:id").handler(this :: getAccountById);
		bankRouter.put("/accounts").handler(this :: addAccount);
		
		int port = config().getInteger("http.port", 8080);
		
		vertx.deployVerticle(com.x.services.SubVerticle.class.getName());
		vertx.deployVerticle(com.x.services.AccountService.class.getName(), workerDeploymentOptions);
		vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(port);
		
		router.mountSubRouter("/bank", bankRouter);
		
		Router secondRouter = Router.router(vertx);
		secondRouter.get().handler(rc -> {
			rc.response().putHeader("content-type", "text/html").end("Hello Mars!");
		});
		
		router.mountSubRouter("/mars", secondRouter);
		startFuture.complete();
	}
	///////////////////ROUTE HANDLERS///////////////////
	protected void getAccounts(RoutingContext rc){
		deliveryOptions.addHeader("method", "getAll");
		vertx.eventBus().send("accounts", null, deliveryOptions, result -> {
			if(result.succeeded()){
				rc.response().putHeader("content-type", "application/json")
					.end((String)result.result().body());
			}else if(result.failed()){
				System.out.println("Failed!");
			}
		});
	}
	
	protected void getAccountById(RoutingContext rc){
		String id = rc.request().getParam("id");
		deliveryOptions.addHeader("method", "get");
		vertx.eventBus().send("accounts", id, deliveryOptions, result ->{
				if(result.succeeded()){
					rc.response().putHeader("content-type", "application/json")
						.end((String) result.result().body());
				}else if (result.failed()){
					rc.response().setStatusCode(404)
						.putHeader("content-type", "application/json")
						.end(Json.encodePrettily(result.cause().getMessage()));
				} else {
					rc.response().setStatusCode(204).end();
				}
		});
	}
	
	protected void addAccount(RoutingContext rc){
		Account account =  Json.decodeValue(rc.getBodyAsString(), Account.class);
		vertx.eventBus().send("addAccount", account, deliveryOptions, result ->{
			if(result.succeeded()){
				rc.response().putHeader("content-type", "application/json")
					.end(Json.encodePrettily(result.result().body()));
			}else if (result.failed()){
				rc.response().setStatusCode(400)
					.putHeader("content-type", "application/json")
					.end(Json.encodePrettily(result.cause().getMessage()));
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

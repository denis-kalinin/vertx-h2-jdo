package com.x.verticles;

import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageCodec;


import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.x.LogbackInit;
import com.x.di.AccountModule;
import com.x.models.Account;
import com.x.models.Balance;
import com.x.models.Transfer;
import com.x.routers.RouterFactory;
import com.x.util.NetworkUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.web.Router;

/**
 * <p>Verticle that starts {@linkplain io.vertx.core.http.HttpServer http server} and
 * registers {@linkplain Router routes} for RESTful API, Javadoc, RAML and RAML web-console.</p>
 * 
 * @author Kalinin_DP
 *
 */
public class MainVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG;
	
	static {
		LogbackInit.start();
		LOG = org.slf4j.LoggerFactory.getLogger(MainVerticle.class);
	}
	
	protected static final int DEFAULT_HTTP_PORT = 80;

	private DeploymentOptions workerDeploymentOptions = new DeploymentOptions().setWorker(true);
	
	@Inject
	private MessageCodec<Account, Account> accountCodec;
	
	@Inject
	private MessageCodec<Transfer, Transfer> transferCodec;
	
	@Inject
	private MessageCodec<Balance, Balance> balanceCodec;
	
	@Inject
	private RouterFactory routerFactory;

	private Module guiceModule;
	
	
	@Override
	public void start(final Future<Void> startFuture){
		if(guiceModule == null) guiceModule = new AccountModule();
		Injector injector = Guice.createInjector(guiceModule);
		injector.injectMembers(this);
		registerCodecs();
		LOG.trace("Main Verticle started");
		Router router = routerFactory.getRouter(vertx);
		int httpPort = config().getInteger("http.port", DEFAULT_HTTP_PORT);
		LOG.trace("http.port from config: {}", httpPort);
		if(httpPort == DEFAULT_HTTP_PORT){
			if(!NetworkUtils.isPortAvailable(httpPort, null)){
				try {
					int ephimeralPort = NetworkUtils.getEphemeralPort();					
					LOG.warn("Port {} is not available. Ephimeral port choosen to start HTTP server: {}", httpPort, ephimeralPort);
					httpPort = ephimeralPort;
					LOG.info("To specify your port - adjust file \"config.json\" and run \"java -jar vertx-h2-jdo.jar -conf config.json\"");
				} catch (Exception e) {
					RuntimeException newE = new RuntimeException("Failed to assign ephimeral port", e);
					LOG.error(newE.getMessage());
					startFuture.fail(newE);
					throw newE;
				}
			}
		}
		
		vertx.deployVerticle(injector.getInstance(AccountVerticle.class), workerDeploymentOptions, ar -> {
			if(ar.succeeded()){
				startFuture.complete();
			}else{
				LOG.error("Account API deployment failed", ar.cause());
				startFuture.fail(ar.cause());
			}
		});
		
		LOG.info("HttpServert binding to port {}", httpPort);
		vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(httpPort);
	}
	/**
	 * Substitutes embedded Guice module with <code>customModule</code>.
	 * @param customModule to substitute default one.
	 */
	public void setGuiceModule(Module customModule){
		this.guiceModule = customModule;
	}
	/**
	 * Registers codes for event-bus
	 */
	private void registerCodecs(){
		LOG.trace("CODEC registered: {}", accountCodec);
		vertx.eventBus().registerDefaultCodec(Account.class, accountCodec);
		vertx.eventBus().registerDefaultCodec(Transfer.class, transferCodec);
		vertx.eventBus().registerDefaultCodec(Balance.class, balanceCodec);
	}
}

package com.x.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.x.di.AccountModuleForTests;
import com.x.models.Account;
import com.x.models.Balance;
import com.x.models.Transfer;
import com.x.util.NetworkUtils;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.RamlMatchers;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.core.Vertx;
import rx.Observable;
import rx.Subscriber;

@RunWith(VertxUnitRunner.class)
public class TransactionsTests {

	private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransactionsTests.class);
	private static Vertx vertx;
	
	private static RamlDefinition api;
	private static ResteasyClient client = new ResteasyClientBuilder().build();
	private static CheckingWebTarget checking;
	private static int port;
	/**
	 * Initial balance. At any moment total amount of money on accounts MUST be equal to the value.
	 */
	private static final long INITIAL_BALANCE = 100000L;
	/**
	 * Total amount of money on system account.
	 */
	private static BigDecimal moneyPile = new BigDecimal(INITIAL_BALANCE).setScale(2, RoundingMode.HALF_UP);
	
	@BeforeClass
	public static void before(TestContext context){
		try {
			port = NetworkUtils.getEphimeralPort();
		} catch (Exception e) {
			LOG.error("Failed to assign ephemeral port to test environment");
			context.fail(e);
		}
		//port = 80;
		LOG.info("Total amount of money on System account: {}", moneyPile);
		vertx = Vertx.vertx();
		JsonObject jsonOptions = new JsonObject();
		jsonOptions.put("http.port", port);
		DeploymentOptions options = new DeploymentOptions().setConfig(jsonOptions);
		MainVerticle mainVerticle = new MainVerticle();
		mainVerticle.setGuiceModule(new AccountModuleForTests());
		LOG.info("Deploying HTTP at port {}", port);
		vertx.deployVerticle(mainVerticle, options, context.asyncAssertSuccess( h -> {
			LOG.trace("MAIN VERTICLE deployed: {} at port {}", h, port);
			context.async().complete();
		}));
		api = RamlLoaders.fromUrl("http://localhost:"+port).load("/raml/accounts.yaml");
		//Assert.assertThat(api.validate(), RamlMatchers.validates());
		api.assumingBaseUri("http://localhost:"+port).failFast(false);
		checking = api.createWebTarget(client.target("http://localhost:"+port));
	}
	@AfterClass
	public static void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}
	
	@Before
	public void createTarget() {
		//final Async async = context.async();
		//async.complete();
	}

	@After
	public void assertBalance(){
		printAllAccounts();
		printAllTransfers();
		Balance balance = getAccountsBalance();
		LOG.info("Balance of {} accounts: {} + Money pile: {}", balance.getAccounts(), balance.getBalance(), moneyPile);
		BigDecimal totalBalance = moneyPile.add(balance.getBalance());
		Assert.assertEquals(INITIAL_BALANCE, totalBalance.longValue());
	}

	@Test
	public void testTransfer(TestContext context){
		final Async async = context.async();
		//create accounts to send money from
		Observable<Account> fromAccounts = Observable.just("Denis", "Ivan", "Alex")
			.map( name -> {
				return new Account(name);
			})
			.map( account -> {
				//register account and get information back with RESTful
				return saveAndGetAccount(account);
			})
			.map( savedAccount -> {
				//put some money on account
				return initDeposit( savedAccount, someMoneyFromPile(100) );
			});
		
		Observable<Account> toAccounts = Observable.just("Duke", "Mike", "Sophie")
				.map( name -> {
					return new Account(name);
				})
				.map( account -> {
					return saveAndGetAccount(account);
				})
				.map( savedAccount -> {
					return initDeposit( savedAccount, someMoneyFromPile(1) );
				});
			
		//for each account "from" transfer money "to" account
		fromAccounts.zipWith(toAccounts, (from, to) -> {
			Transfer t = new Transfer();
			t.setAmount(randomMoney(from.getDeposit().doubleValue()));
			t.setFrom(from);
			t.setTo(to);
			return sendTransfer(t);
		}).subscribe( 
			transfer -> {
				LOG.debug("Transfer: {}", transfer);
			},
			error -> {context.fail(error);},
			() -> {
				async.complete();
			}
		);
	}
	
	private static Account saveAndGetAccount(Account account){
		WebTarget webTarget = checking.path("/bank/accounts");
		Response resp = webTarget.request().post(Entity.json(account));
		Assert.assertEquals("Status SHOULD be 201", 201, resp.getStatus());
		Account savedAccount = resp.readEntity(Account.class);
		LOG.trace("Last-reposrt: {}", checking.getLastReport());
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Assert.assertNotNull("Saved account has not ID", savedAccount.getId());
		Assert.assertEquals("Saved account has wrong customerId", savedAccount.getCustomerId(), account.getCustomerId());
		
		//List<Object> locations = resp.getHeaders().get("Location");
		//Assert.assertNotNull("Location header SHOULD not be null", locations);
		//Assert.assertFalse("Location header SHOULD not be empty", locations.isEmpty());
		//String location = (String) locations.get(0);
		URI locationUri = resp.getLocation();
		webTarget = checking.path("/bank/"+locationUri.toString());
		Response accountResponse = webTarget.request().get();
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Assert.assertEquals("Saved account (POST) doesn't match with retrieved one (GET)", savedAccount, accountResponse.readEntity(Account.class));
		return savedAccount;
	}
	
	private static Account getAccountById(String id){
		WebTarget webTarget = checking.path("/bank/accounts/"+id);
		Response resp = webTarget.request().get();
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Assert.assertEquals("Response code SOULD be 200", resp.getStatus(), 200);
		Account savedAccount = resp.readEntity(Account.class);
		return savedAccount;
	}
	
	private static Transfer getTransferById(String id){
		WebTarget webTarget = checking.path("/bank/transfers/"+id);
		Response resp = webTarget.request().get();
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Transfer savedTransfer = resp.readEntity(Transfer.class);
		return savedTransfer;
	}
	
	private Transfer sendTransfer(Transfer transfer){
		WebTarget webTarget = checking.path("/bank/transfers");
		//Response resp = webTarget.request().post(Entity.json(transfer));
		Response resp = webTarget.request().post(Entity.json(Json.encode(transfer)));
		Assert.assertEquals("Status code SHOULD BE 201", 201, resp.getStatus());
		LOG.trace("Violations: {}", checking.getLastReport());
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		//Transfer commitedTransfer = mapper.readValue(resp.readEntity(String.class), Transfer.class);
		Transfer commitedTransfer = resp.readEntity(Transfer.class);
		Assert.assertNotNull("Commited transfer has no ID", commitedTransfer.getId());
		
		List<Object> locations = resp.getHeaders().get("Location");
		Assert.assertNotNull("Location header SHOULD not be null", locations);
		Assert.assertFalse("Location header SHOULD not be empty", locations.isEmpty());
		String location = (String) locations.get(0);
		webTarget = checking.path("/bank/"+location);
		Response transferResponse = webTarget.request().get();
		LOG.trace("Violations: {}", checking.getLastReport());
		Assert.assertTrue("RAML contract violation", RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Transfer savedTransfer = transferResponse.readEntity(Transfer.class);
		//Transfer savedTransfer = mapper.readValue(resp.readEntity(String.class), Transfer.class);
		Assert.assertEquals("Commited transfer (POST) doesn't match with retrieved one (GET)", commitedTransfer, savedTransfer);
		return savedTransfer;
	}
	
	private Observable<Account> saveAccount(Account account){
		return Observable.create( (Subscriber<? super Account> subscriber) -> {
			if ( subscriber.isUnsubscribed() ) {return;}
			//subscriber.onError(new RuntimeException("SDFD"));
			Account savedAccount = saveAndGetAccount(account);
			subscriber.onNext(savedAccount);
			subscriber.onCompleted();
		});
	}
	
	private Account initDeposit(Account account, BigDecimal amount){
		LOG.debug("Add initial deposit of {} to account {}", amount, account);
		Assert.assertNotNull("Account ID SHOULD not be NULL", account.getId());
		Transfer initTransfer = new Transfer();
		initTransfer.setAmount(amount);
		initTransfer.setTo(account);
		initTransfer = sendTransfer(initTransfer);
		Assert.assertNotNull("Transfer has no ID", initTransfer.getId());
		Assert.assertNotNull("Transfer has no TO", initTransfer.getTo());
		return getAccountById(initTransfer.getTo().getId());
	}
	
	private synchronized BigDecimal randomMoney(double max){
		BigDecimal maxDecimal = new BigDecimal(max);
		BigDecimal randFromDouble = new BigDecimal(Math.random());
		BigDecimal actualRandom = maxDecimal.multiply(randFromDouble).setScale(2, RoundingMode.HALF_UP);
		return actualRandom;
	}
	
	private BigDecimal someMoneyFromPile(double max){
		BigDecimal randomMoney = randomMoney(max);
		moneyPile = moneyPile.subtract(randomMoney);
		return randomMoney;
	}
	
	
	
	/**
	 * Check that total amount of money is equal to {@linkplain #INITIAL_BALANCE}
	 */
	private Balance getAccountsBalance(){
		WebTarget webTarget = checking.path("/bank/balance");
		Response resp = webTarget.request().get();
		Balance balance = resp.readEntity(Balance.class);
		return balance;
	}
	
	@SuppressWarnings("unchecked")
	private void printAllAccounts(){
		WebTarget webTarget = checking.path("/bank/accounts");
		Response resp = webTarget.request().get();
		Class<? extends List<Account>> cl = (Class<? extends List<Account>>) new ArrayList<Account>().getClass();
		LOG.info("Accounts list:");
		System.out.println(StringUtils.join(resp.readEntity(cl), "\r\n"));
	}
	@SuppressWarnings("unchecked")
	private void printAllTransfers(){
		WebTarget webTarget = checking.path("/bank/transfers");
		Response resp = webTarget.request().get();
		Class<? extends List<Transfer>> cl = (Class<? extends List<Transfer>>) new ArrayList<Transfer>().getClass();
		LOG.info("Transfers:");
		System.out.println(StringUtils.join(resp.readEntity(cl), "\r\n"));
	}
}

package com.x.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
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
import com.x.util.TestDataBuilder;
import com.x.verticles.MainVerticle;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.RamlMatchers;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.core.Vertx;
import rx.Observable;

/**
 * Creates few accounts, puts money in account. Then:
 * <ol>
 * <li>make transfers among accounts (payers &#8594; payees)</li>
 * </ol> 
 * @author Kalinin_DP
 *
 */
@RunWith(VertxUnitRunner.class)
public class TestTransactions {

	private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestTransactions.class);
	private static Vertx vertx;
	
	private static RamlDefinition api;
	private static ResteasyClient client = new ResteasyClientBuilder().build();
	private static CheckingWebTarget checking;
	/**
	 * Initial balance. At any moment total amount of money on accounts MUST be equal to the value.
	 */
	private static final long INITIAL_BALANCE = 100000L;
	private static TestDataBuilder tdb;	
	private static Observable<Account> payersObservable;
	private static Observable<Account> payeesObservable;
	private static String[] payers = new String[]{"Yuri", "Ivan", "Maria"};
	private static String[] payees = new String[]{"Duke", "Mike", "Sophie"};
	
	
	@BeforeClass
	public static void before(TestContext context){
		int port = -1;
		try {
			port = NetworkUtils.getEphemeralPort();
		} catch (Exception e) {
			LOG.error("Failed to assign ephemeral port to test environment");
			context.fail(e);
		}
		BigDecimal moneyPile = new BigDecimal(INITIAL_BALANCE).setScale(2, RoundingMode.HALF_UP);
		LOG.info("Total amount of money on System account: {}", moneyPile);
		vertx = Vertx.vertx();
		JsonObject jsonOptions = new JsonObject();
		jsonOptions.put("http.port", port);
		DeploymentOptions options = new DeploymentOptions().setConfig(jsonOptions);
		MainVerticle mainVerticle = new MainVerticle();
		mainVerticle.setGuiceModule(new AccountModuleForTests());
		LOG.info("Deploying HTTP at port {}", port);
		final String hostUrl = "http://localhost:"+port;
		vertx.deployVerticle(mainVerticle, options, context.asyncAssertSuccess( h -> {
			LOG.trace("MAIN VERTICLE deployed: {} at {}", h, hostUrl);
			api = RamlLoaders.fromUrl(hostUrl).load("/raml/accounts.yaml");
			Assert.assertThat(api.validate(), RamlMatchers.validates());
			api.assumingBaseUri(hostUrl).failFast(false);
			checking = api.createWebTarget(client.target(hostUrl));
			tdb = new TestDataBuilder(moneyPile, checking);
			payersObservable = tdb.initAccounts(payers, 1000);
			payeesObservable = tdb.initAccounts(payees, 10);
		}));
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
		LOG.info("=========after test==============");
		printAllAccounts();
		printAllTransfers();
		Balance balance = tdb.getAccountsBalance();
		BigDecimal moneyPile = tdb.getMoneyPile();
		LOG.info("Balance of {} accounts: {} + Money pile: {}", balance.getAccounts(), balance.getBalance(), moneyPile);
		BigDecimal totalBalance = moneyPile.add(balance.getBalance());
		Assert.assertEquals(INITIAL_BALANCE, totalBalance.longValue());
	}

	@Test
	public void testTransfer(TestContext context){
		LOG.info("====== test transfers among accounts ======");
		//for each account "from" transfer money "to" account
		payersObservable.zipWith(payeesObservable, (from, to) -> {
			BigDecimal amount = TestDataBuilder.randomMoney(from.getDeposit().doubleValue());
			Transfer t = new Transfer(to.getId(), from.getId(), amount);
			return tdb.sendTransfer(t);
		}).subscribe( 
			transfer -> { LOG.trace("Transfer: {}", transfer); },
			error -> {context.fail(error);},
			() -> { context.async().complete(); }
		);
	}
	
	
	@SuppressWarnings("unchecked")
	private static void printAllAccounts(){
		WebTarget webTarget = checking.path("/bank/accounts");
		Response resp = webTarget.request().get();
		Class<? extends List<Account>> cl = (Class<? extends List<Account>>) new ArrayList<Account>().getClass();
		LOG.info("Accounts list:");
		System.out.println(StringUtils.join(resp.readEntity(cl), "\r\n"));
	}
	@SuppressWarnings("unchecked")
	private static void printAllTransfers(){
		WebTarget webTarget = checking.path("/bank/transfers");
		Response resp = webTarget.request().get();
		Class<? extends List<Transfer>> cl = (Class<? extends List<Transfer>>) new ArrayList<Transfer>().getClass();
		LOG.info("Transfers:");
		System.out.println(StringUtils.join(resp.readEntity(cl), "\r\n"));
	}
}

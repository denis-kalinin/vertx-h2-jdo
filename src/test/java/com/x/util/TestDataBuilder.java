package com.x.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.x.models.Account;
import com.x.models.Balance;
import com.x.models.Transfer;
import com.x.routers.RouterFactory;

import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.RamlMatchers;
import io.vertx.core.json.Json;
import rx.Observable;
import rx.Subscriber;

/**
 * Populates data for tests
 * @author Kalinin_DP
 *
 */
public class TestDataBuilder {
	
	private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestDataBuilder.class);
	private CheckingWebTarget checking;
	private BigDecimal moneyPile;
	private Object moneyLock = new Object();
	
	public TestDataBuilder(BigDecimal moneyPile, CheckingWebTarget checking){
		this.moneyPile = moneyPile;
		this.checking = checking;
	}
	
	/**
	 * 
	 * @param names of users to create accounts for
	 * @param walletMoneMax maximum amount of initial money for created accounts
	 * @return a RX Observable for accounts (created from <code>names</code>)
	 * 
	 */
	public Observable<Account> initAccounts(String[] names, int walletMoneyMax){
		//create and populate accounts
		return Observable.from(names)
			.map( name -> {
				return new Account(name);
			})
			.map( account -> {
				//register account and get information back with RESTful
				return saveAndGetAccount(account);
			})
			.map( savedAccount -> {
				//put some money on account
				return initDeposit( savedAccount, getMoneyFromPile(walletMoneyMax) );
			});
	}
	
	public Account getAccountById(String id){
		WebTarget webTarget = checking.path(RouterFactory.MOUNTPOINT+RouterFactory.ApiEndpoints.ACCOUNTS+"/"+id);
		Response resp = webTarget.request().get();
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Assert.assertEquals("Response code SOULD be 200", resp.getStatus(), 200);
		Account savedAccount = resp.readEntity(Account.class);
		return savedAccount;
	}
	
	public Account saveAndGetAccount(Account account){
		WebTarget webTarget = checking.path(RouterFactory.MOUNTPOINT+RouterFactory.ApiEndpoints.ACCOUNTS);
		Response resp = webTarget.request().post(Entity.json(account));
		Assert.assertEquals("Status SHOULD be 201", 201, resp.getStatus());
		Account savedAccount = resp.readEntity(Account.class);
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Assert.assertNotNull("Saved account has not ID", savedAccount.getId());
		Assert.assertEquals("Saved account has wrong customerId", savedAccount.getCustomerId(), account.getCustomerId());
		URI locationUri = resp.getLocation();
		Assert.assertNotNull("Response after saving account doesn't has Location header", locationUri);
		webTarget = checking.path(RouterFactory.MOUNTPOINT+"/"+locationUri.toString());
		Response accountResponse = webTarget.request().get();
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Assert.assertEquals("Saved account (POST) doesn't match with retrieved one (GET)", savedAccount, accountResponse.readEntity(Account.class));
		return savedAccount;
	}
	
	public Account initDeposit(Account account, BigDecimal amount){
		LOG.debug("Add initial deposit of {} to account {}", amount, account);
		Assert.assertNotNull("Account ID SHOULD not be NULL", account.getId());
		Transfer initTransfer = new Transfer(account.getId(), null, amount);
		//initTransfer.setAmount(amount);
		//initTransfer.setTo(account);
		initTransfer = sendTransfer(initTransfer);
		Assert.assertNotNull("Transfer has no ID", initTransfer.getId());
		Assert.assertNotNull("Transfer has no TO", initTransfer.getTo());
		return getAccountById(initTransfer.getTo().getId());
	}
	
	public BigDecimal getMoneyFromPile(double max){
		BigDecimal randomMoney = randomMoney(max);
		synchronized(moneyLock){
			moneyPile = moneyPile.subtract(randomMoney);
		}
		return randomMoney;
	}
	
	public BigDecimal getMoneyPile(){
		synchronized(moneyLock){
			return moneyPile;
		}
	}
	
	/**
	 * @param max
	 * @return random number that is less than <code>max</code>
	 */
	public static BigDecimal randomMoney(double max){
		BigDecimal maxDecimal = new BigDecimal(max);
		BigDecimal randFromDouble = new BigDecimal(Math.random());
		BigDecimal actualRandom = maxDecimal.multiply(randFromDouble).setScale(2, RoundingMode.HALF_UP);
		return actualRandom;
	}
	
	public Transfer sendTransfer(Transfer transfer){
		WebTarget webTarget = checking.path(RouterFactory.MOUNTPOINT+RouterFactory.ApiEndpoints.TRANSFERS);
		//Response resp = webTarget.request().post(Entity.json(transfer));
		Response resp = webTarget.request().post(Entity.json(Json.encode(transfer)));
		Assert.assertEquals("Status code SHOULD BE 201", 201, resp.getStatus());
		Assert.assertTrue("RAML violations: " + checking.getLastReport().toString(), RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		//Transfer commitedTransfer = mapper.readValue(resp.readEntity(String.class), Transfer.class);
		Transfer commitedTransfer = resp.readEntity(Transfer.class);
		Assert.assertNotNull("Commited transfer has no ID", commitedTransfer.getId());
		List<Object> locations = resp.getHeaders().get("Location");
		Assert.assertNotNull("Location header SHOULD not be null", locations);
		Assert.assertFalse("Location header SHOULD not be empty", locations.isEmpty());
		String location = (String) locations.get(0);
		Assert.assertNotNull("Created transfer doesn't return resource location", location);
		Assert.assertFalse("Transfer resource Location " + location +" contains \"null\"", StringUtils.contains(location,  "null"));
		webTarget = checking.path(RouterFactory.MOUNTPOINT+"/"+location);
		Response transferResponse = webTarget.request().get();
		//LOG.trace("Violations: {}", checking.getLastReport());
		Assert.assertTrue("RAML contract violation", RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Transfer savedTransfer = transferResponse.readEntity(Transfer.class);
		//Transfer savedTransfer = mapper.readValue(resp.readEntity(String.class), Transfer.class);
		Assert.assertEquals("Commited transfer (POST) doesn't match with retrieved one (GET)", commitedTransfer, savedTransfer);
		return savedTransfer;
	}
	
	/**
	 * Check that total amount of money is equal to {@linkplain #INITIAL_BALANCE}
	 */
	public Balance getAccountsBalance(){
		WebTarget webTarget = checking.path(RouterFactory.MOUNTPOINT+RouterFactory.ApiEndpoints.BALANCE);
		Response resp = webTarget.request().get();
		Balance balance = resp.readEntity(Balance.class);
		return balance;
	}
	
	public Transfer getTransferById(String id){
		WebTarget webTarget = checking.path("/bank/transfers/"+id);
		Response resp = webTarget.request().get();
		Assert.assertTrue(RamlMatchers.hasNoViolations().matches(checking.getLastReport()));
		Transfer savedTransfer = resp.readEntity(Transfer.class);
		return savedTransfer;
	}

	public Observable<Account> saveAccount(Account account){
		return Observable.create( (Subscriber<? super Account> subscriber) -> {
			if ( subscriber.isUnsubscribed() ) {return;}
			//subscriber.onError(new RuntimeException("SDFD"));
			Account savedAccount = saveAndGetAccount(account);
			subscriber.onNext(savedAccount);
			subscriber.onCompleted();
		});
	}

}

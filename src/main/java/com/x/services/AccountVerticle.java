package com.x.services;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.x.di.AccountModule;
import com.x.models.Account;
import com.x.models.AccountDAO;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.Message;


public class AccountVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountVerticle.class);
	private Map<String, Account> accounts = new ConcurrentHashMap<>();
	
	@Inject
	private AccountDAO dao;
	
	@Override
	public void start(Future<Void> startFuture){	
		registerMessageHandlers();		
	}
	
	@Override
	public void stop(){
		//databaseServer.stop();
	}
	public void registerMessageHandlers(){
		vertx.eventBus().consumer("accounts", message -> {
			String method = message.headers().get("method");
			switch ( method ){
				//return accounts
				case "getAll":{
					message.reply(Json.encodePrettily(dao.getAccounts()));
					break;
				}
				//return specific account
				case "get":{
					String accountId = (String) message.body();
					Optional<Account> opAccount = dao.getById(accountId);
					if(opAccount.isPresent()){
						Account acc = opAccount.get();
						String accString = Json.encodePrettily(acc);
						LOG.debug("Account for {} : {}", acc.getDeposit().floatValue(), accString);
						message.reply(accString);
					}else{
						message.fail(404, "Account "+accountId+" is not found.");
					}
					break;
				}
				//create account
				case "create":{
					
				}
				
				default: {
					message.fail(404, "Wrong eventbus request");
				}
			}
			
		});
		/*return account by id
		vertx.eventBus().consumer("account", message -> {
			vertx.executeBlocking(
				fut -> {
					String accountId = (String) message.body();
					if(accounts.containsKey(accountId)){
						JsonObject jsonObject = new JsonObject(Json.encode(accounts.get(accountId)));
						fut.complete(jsonObject);
					}else{
						fut.fail("Account #"+accountId+" is not found.");
					}
				}, 
				false,
				res -> {
					if(res.failed()){
						message.fail(404, "OOPS! "+res.cause().getMessage());
					} else {
						message.reply(res.result());
					}
				}
			);
		});
		//create account for user and return detached one
		vertx.eventBus().consumer("addAccount", message -> {
			vertx.executeBlocking(
				fut -> {
					Account account = (Account) message.body();
					JsonObject jsonObject = new JsonObject(Json.encode(account));
					fut.complete(jsonObject);
				}, 
				false,
				res -> {
					if(res.failed()){
						message.fail(404, "OOPS! "+res.cause().getMessage());
					} else {
						message.reply(res.result());
					}
				}
			);
		});
		*/
	}
	/**
	 * 
	 * @param account
	 * @throws IllegalArgumentException if account parameter is wrong
	 * @throws IllegalStateException if account is already registered
	 */
	public void addAccount(Account account){
		if(account == null) throw new IllegalArgumentException("account is NULL");
		if(account.getId() == null) throw new IllegalArgumentException("account has no id");
		if(!accounts.containsKey(account.getId())){
			accounts.put(account.getId(), account);
		}else{
			throw new IllegalStateException("Account "+account.getId()+" is already registered.");
		}		
	}
	/**
	 * 
	 * @param id of the account
	 * @return account as optional
	 */
	public Optional<Account> getAccount(String id){
		return Optional.ofNullable(accounts.get(id));
	}
	/**
	 * @return an immutable copy of accounts map: [id, account]
	 */
	public Map<String, Account> getAccountsMap(){
		return ImmutableMap.copyOf(accounts);
	}
	
	public void createData(){
		Account account1 = new Account("001");
		account1.setDeposit(BigDecimal.valueOf(2.43));
		//addAccount(account1);
				
		Account account2 = new Account("002");
		account2.setDeposit(BigDecimal.valueOf(234.34));
		//addAccount(account2);
		
		dao.addAccounts(account1, account2);
		
	}
}

package com.x.services;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.x.dao.AccountDAO;
import com.x.dao.TransferDAO;
import com.x.models.Account;
import com.x.models.Transfer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

public class AccountVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountVerticle.class);
	private Map<String, Account> accounts = new ConcurrentHashMap<>();
	
	@Inject
	private AccountDAO accountDao;
	
	@Inject
	private TransferDAO transferDao;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception{
		registerMessageHandlers();
		super.start(startFuture);
	}
	
	@Override
	public void stop(){
		//databaseServer.stop();
	}
	public void registerMessageHandlers(){
		LOG.trace("Registering ACCOUNTS API");
		vertx.eventBus().consumer("accounts", message -> {
			String method = message.headers().get("method");
			switch ( method ){
				//return accounts
				case "getAll":{
					message.reply(Json.encodePrettily(accountDao.getAccounts()));
					break;
				}
				//return specific account
				case "get":{
					String accountId = (String) message.body();
					Optional<Account> opAccount = accountDao.getAccountById(accountId);
					if(opAccount.isPresent()){
						Account acc = opAccount.get();
						String accString = Json.encodePrettily(acc);
						LOG.trace("Account for {} : {}", acc.getDeposit(), accString);
						message.reply(accString);
					}else{
						message.fail(404, "Account "+accountId+" is not found.");
					}
					break;
				}
				//create account
				case "addAccount":{
					try{
						Account account = (Account) message.body();
						accountDao.addAccounts(account);
						message.reply(account);
					}catch(Exception e){
						message.fail(500, e.getMessage());
					}
					break;
				}
				case "getBalance": {
					message.reply(accountDao.getBalance());
					break;
				}
				default: {
					message.fail(404, "Wrong eventbus request");
				}
			}
			
		});
		
		vertx.eventBus().consumer("transfers", message -> {
			String method = message.headers().get("method");
			switch (method){
				case "send": {
					try{
						Transfer transfer = (Transfer) message.body();
						transferDao.commitTransfer(transfer);
						message.reply(transfer);
					}catch(Exception e){
						message.fail(500, e.getMessage());
					}
					break;
				}
				case "get" : {
					String transferId = (String) message.body();
					Optional<Transfer> opT = transferDao.getTransferById(transferId);
					if(opT.isPresent()){
						message.reply(opT.get());
					}else{
						message.fail(404, "Transfer "+transferId+" is not found.");
					}
					break;
				}
				case "getForAccount": {
					message.fail(500, "Not implemented");
					break;
				}
				case "getAll": {
					message.reply(Json.encodePrettily(transferDao.getTransfers()));
					break;
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
}

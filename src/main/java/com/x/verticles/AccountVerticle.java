package com.x.verticles;

import java.util.Optional;

import javax.inject.Inject;

import com.x.dao.AccountDAO;
import com.x.dao.TransferDAO;
import com.x.models.Account;
import com.x.models.Transfer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
/**
 * Verticle that serves requests to database - deploy it as worker verticle.
 * @author Kalinin_DP
 *
 */
public class AccountVerticle extends AbstractVerticle {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountVerticle.class);
	
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
	/**
	 * Handler for serving accounts and transfers requests
	 */
	private void registerMessageHandlers(){
		LOG.trace("Registering ACCOUNTS API");
		vertx.eventBus().consumer("accounts", message -> {
			String method = message.headers().get("method");
			switch ( method ){
				case "getAll":{
					message.reply(Json.encodePrettily(accountDao.getAccounts()));
					break;
				}
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
						Transfer commitedTransfer = transferDao.commitTransfer(transfer);
						message.reply(commitedTransfer);
					}catch(Exception e){
						message.fail(500, e.getMessage());
					}
					break;
				}
				case "get" : {
					String transferId = (String) message.body();
					try{
						Optional<Transfer> opT = transferDao.getTransferById(transferId);
						if(opT.isPresent()){
							message.reply(opT.get());
						}else{
							message.fail(404, "Transfer "+transferId+" is not found.");
						}
					}catch (Exception e){
						message.fail(500, e.getMessage());
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
	}
}

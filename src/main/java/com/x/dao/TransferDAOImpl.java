package com.x.dao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.x.models.Account;
import com.x.models.Transfer;

@Singleton
public class TransferDAOImpl implements TransferDAO {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransferDAOImpl.class);
	
	@Inject
	private PersistenceManagerFactory pmf;

	@Override
	public List<Transfer> getTranfersForAccount(String accountId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transfer commitTransfer(Transfer transferToCommit) throws Exception {
		LOG.trace("Transfering {}", transferToCommit);
		PersistenceManager pm = pmf.getPersistenceManager();
		//pm.getFetchPlan().setGroups("account-metainfo", "transfer-base");
		pm.getFetchPlan().setMaxFetchDepth(2);
		Transaction tx = pm.currentTransaction();
		try{
			tx.begin();
			BigDecimal amount = transferToCommit.getAmount();
			Account toAccount = transferToCommit.getTo();
			//at this moment toAccount has only ID, other fields are empty, get account from database
			toAccount = pm.getObjectById(Account.class, toAccount.getId());
			//and detach a copy to update "deposit" field and then persist back to database together with transferToCommit 
			toAccount = pm.detachCopy(toAccount);
			toAccount.setDeposit( toAccount.getDeposit().add(amount));
			pm.makePersistent(toAccount);
			Account fromAccount = transferToCommit.getFrom();
			//fromAccount may be null if account is credited/debited from outer service
			if(fromAccount!=null && fromAccount.getId()!=null){
				fromAccount = pm.getObjectById(Account.class, fromAccount.getId());
				fromAccount = pm.detachCopy(fromAccount);
				fromAccount.setDeposit(fromAccount.getDeposit().subtract(amount));
				pm.makePersistent(fromAccount);
			}else{
				fromAccount = null;
			}
			//FIXME some policy here to define status
			Transfer transferToPersist = new Transfer(toAccount, fromAccount, amount);
			transferToPersist.setStatus(Transfer.Status.Success);
			transferToPersist = pm.makePersistent(transferToPersist);
			tx.commit();
			return pm.detachCopy(transferToPersist);
		}catch (Exception e){
			LOG.debug("Transfer exception", e);
			throw e;
		}finally{
			if (tx.isActive()){tx.rollback();}
			pm.close();
		}
	}

	@Override
	public Optional<Transfer> getTransferById(String id) {
		PersistenceManager pm = pmf.getPersistenceManager();
		//pm.getFetchPlan().setGroups("account-metainfo", "transfer-base");
		pm.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_OPTIMAL);
		try{
			Transfer transfer = pm.getObjectById(Transfer.class, id);
			return Optional.of(pm.detachCopy(transfer));
		}
		catch(javax.jdo.JDOObjectNotFoundException e){
			LOG.error("Failed to get transfer", e);
			throw e;
		}		
		finally{
			pm.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Transfer> getTransfers(){
		PersistenceManager pm = pmf.getPersistenceManager();
		Query<Transfer> query = pm.newQuery(Transfer.class);
		List<Transfer> result = (List<Transfer>) query.execute();
		return pm.detachCopyAll(result);
	}
}

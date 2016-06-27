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
	public void commitTransfer(Transfer transferToCommit) {
		LOG.trace("Transfering {}", transferToCommit);
		PersistenceManager pm = pmf.getPersistenceManager();
		//pm.getFetchPlan().setGroups("account-metainfo", "transfer-base");
		pm.getFetchPlan().setMaxFetchDepth(2);
		Transaction tx = pm.currentTransaction();
		try{
			tx.begin();
			//Account from = pm.getObjectById(Account.class, transferToCommit.getFrom().getId());
			//Account to = pm.getObjectById(Account.class, transferToCommit.getFrom().getId());
			BigDecimal amount = transferToCommit.getAmount();
			Account to = pm.getObjectById(Account.class, transferToCommit.getTo().getId());
			to = pm.detachCopy(to);
			to.setDeposit( to.getDeposit().add(amount));
			transferToCommit.setTo(to);
			if(transferToCommit.getFrom()!=null){
				Account from = pm.getObjectById(Account.class, transferToCommit.getFrom().getId());
				from = pm.detachCopy(from);
				from.setDeposit(from.getDeposit().subtract(amount));
				transferToCommit.setFrom(from);
				//pm.makePersistent(from);
			}
			//pm.detachCopy(from);
			//pm.makePersistent(to);
			//pm.detachCopy(to);
			transferToCommit.setStatus(Transfer.Status.Success);
			pm.makePersistent(transferToCommit);
			tx.commit();
			pm.detachCopy(transferToCommit);
		}catch (Exception e){
			LOG.debug("Transfer exception", e);
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
			return Optional.empty();
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

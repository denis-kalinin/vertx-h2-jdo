package com.x.models;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

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
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try{
			tx.begin();
			//Account from = pm.getObjectById(Account.class, transferToCommit.getFrom().getId());
			//Account to = pm.getObjectById(Account.class, transferToCommit.getFrom().getId());
			BigDecimal amount = transferToCommit.getAmount();
			LOG.debug("Transfering {}", amount);
			Account from = transferToCommit.getFrom();
			Account to = transferToCommit.getTo();
			from.setDeposit(from.getDeposit().subtract(amount));
			to.setDeposit(to.getDeposit().add(amount));
			pm.makePersistent(from);
			//pm.detachCopy(from);
			pm.makePersistent(to);
			//pm.detachCopy(to);
			pm.makePersistent(transferToCommit);
			transferToCommit.setStatus(Transfer.Status.Success);
			tx.commit();
			//return pm.detachCopy(transferToCommit);
		}finally{
			if (tx.isActive()){tx.rollback();}
			pm.detachCopy(transferToCommit);
			pm.close();
		}
	}

}

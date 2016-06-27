package com.x.dao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.x.models.Account;
import com.x.models.Balance;


@Singleton
public class AccountDAOImpl implements AccountDAO {
	
	@Inject
	private PersistenceManagerFactory pmf;

	@Override
	public void addAccounts(Account ... accounts){
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try{
			tx.begin();
			for(Account acc : accounts){
				if(acc.getDeposit().compareTo(BigDecimal.ZERO) != 0){
					throw new IllegalStateException("Creating account with deposit greater or less than 0 is prohibited");
				}
				pm.makePersistent(acc);
				pm.setDetachAllOnCommit(true);
			}
			tx.commit();
		}finally{
			if (tx.isActive()){tx.rollback();}
			pm.close();
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<Account> getAccounts(){
		PersistenceManager pm = pmf.getPersistenceManager();
		Query<Account> query = pm.newQuery(Account.class);
		List<Account> result = (List) query.execute();
		return pm.detachCopyAll(result);
	}
	@Override
	public Optional<Account> getAccountById(String id){
		PersistenceManager pm = pmf.getPersistenceManager();
		try{
			Account account = pm.getObjectById(Account.class, id);
			return Optional.of(pm.detachCopy(account));
		}
		catch(javax.jdo.JDOObjectNotFoundException e){
			return Optional.empty();
		}		
		finally{
			pm.close();
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override	
	public Collection<Account> getAccounts(String customerId){
		PersistenceManager pm = pmf.getPersistenceManager();
		Query<Account> query = pm.newQuery(Account.class);
		query.setFilter(":customerId == this.customerId");
		List<Account> result = (List) query.execute(customerId);
		return pm.detachCopyAll(result);		
	}
	
	public Balance getBalance(){
		PersistenceManager pm = pmf.getPersistenceManager();
		
		Query<Account> query = pm.newQuery(Account.class);
		query.setResult("count(this), sum(this.deposit)");
		Object results[] = (Object[]) query.execute();
		Balance balance = new Balance();
		balance.setAccounts((long) results[0]);
		balance.setBalance((BigDecimal) results[1]);
		return balance;
	}

}


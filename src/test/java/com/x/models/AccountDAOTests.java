package com.x.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.jdo.PersistenceManagerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.x.di.AccountModule;
import com.x.di.AccountModuleForTests;


public class AccountDAOTests {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountDAOTests.class);
	
	private static Injector injector;
	private AccountDAO accountDao;
	private TransferDAO transferDao;
	
	@BeforeClass
	public static void setPMF(){
		injector = Guice.createInjector(new AccountModuleForTests());
		//PersistenceManagerFactory pmf = injector.getInstance(PersistenceManagerFactory.class);
		//pmf.getPersistenceManager().newQuery(Transfer.class).deletePersistentAll();
		//pmf.getPersistenceManager().newQuery(Account.class).deletePersistentAll();
	}
	@Before
	public void bootDAO(){
		accountDao = injector.getInstance(AccountDAO.class);
		transferDao = injector.getInstance(TransferDAO.class);
	}
	
	//@Test
	public void addAccounts() throws MalformedURLException, ClassNotFoundException, URISyntaxException{
		AccountDAO dao = new AccountDAOImpl();
		Account account1 = new Account("001");
		account1.setDeposit(BigDecimal.valueOf(2.43f).setScale(2, RoundingMode.HALF_UP));
		Account account2 = new Account("002");
		account2.setDeposit(BigDecimal.valueOf(234.34f).setScale(2, RoundingMode.HALF_UP));
		dao.addAccounts(account1, account2);
		
		Assert.assertNotNull("ID is null", account1.getId());
		Assert.assertEquals("002", account2.getCustomerId());
		Assert.assertEquals(BigDecimal.valueOf(234.34f).setScale(2, RoundingMode.HALF_UP), account2.getDeposit());
		LOG.debug("Account2 deposited: {}", account2.getDeposit());
		
		String id = account2.getId();
		Account storedAccount = dao.getAccountById(id).get();
		Assert.assertEquals(id, storedAccount.getId());
		Assert.assertEquals(storedAccount.getDeposit(), account2.getDeposit());
		LOG.debug("Account {} is saved for user {}, balance: {}", storedAccount.getId(), storedAccount.getCustomerId(), storedAccount.getDeposit());
		
		LOG.debug("User 002 has {}", dao.getAccounts("002").size());
		
	}
	
	@Test
	public void testTransfer(){
		Account to = new Account("denis");
		Account from = new Account("ivan");
		accountDao.addAccounts(to, from);
		LOG.info("{}[{}], {}", to.getId(), to.getCustomerId(), from.getId());
		Transfer trans = new Transfer();
		trans.setFrom(from);
		trans.setTo(to);
		trans.setAmount(BigDecimal.valueOf(123.54423).setScale(2, RoundingMode.HALF_UP));
		transferDao.commitTransfer(trans);
		LOG.info("Transfer {} commited ammount {}. Result: {}", trans.getId(), trans.getAmount(), trans.getStatus());
		
		accountDao.getAccountById(to.getId()).ifPresent( accountTo -> {
				LOG.info("{} has {}", accountTo.getCustomerId(), accountTo.getDeposit());
			});
		
		accountDao.getAccountById(from.getId()).ifPresent( accountFrom -> {
			LOG.info("{}: {} has {}", accountFrom.getId(), accountFrom.getCustomerId(), accountFrom.getDeposit());
		});
	}
	
	

}

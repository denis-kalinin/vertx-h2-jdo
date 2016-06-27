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
import com.x.dao.AccountDAO;
import com.x.dao.AccountDAOImpl;
import com.x.dao.TransferDAO;
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
	
	@Test
	public void checkBalance(){
		LOG.info("Balance: {}", accountDao.getBalance());
	}
	
}

package com.x.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;


public class AccountDAOTests {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountDAOTests.class);
	
	@Test
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
		Account storedAccount = dao.getById(id).get();
		Assert.assertEquals(id, storedAccount.getId());
		Assert.assertEquals(storedAccount.getDeposit(), account2.getDeposit());
		LOG.debug("Account {} is saved for user {}, balance: {}", storedAccount.getId(), storedAccount.getCustomerId(), storedAccount.getDeposit());
		
		LOG.debug("User 002 has {}", dao.getAccounts("002").size());
		
	}

}

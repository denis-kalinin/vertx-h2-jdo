package com.x.models;

import java.math.BigDecimal;
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
		account1.setDeposit(BigDecimal.valueOf(2.43));
		Account account2 = new Account("002");
		account2.setDeposit(BigDecimal.valueOf(234.34));
		dao.addAccounts(account1, account2);
		
		Assert.assertNotNull("ID is null", account1.getId());
		Assert.assertEquals("002", account2.getCustomerId());
		
		String id = account2.getId();
		Account storedAccount = dao.getById(id);
		Assert.assertEquals(id, storedAccount.getId());
		LOG.debug("Account {} is saved for user {}", storedAccount.getId(), storedAccount.getCustomerId());
		
		LOG.debug("User 002 has {}", dao.getAccounts("002").size());
		
	}

}

package com.x.dao;

import java.util.Collection;
import java.util.Optional;

import com.x.models.Account;
import com.x.models.Balance;
/**
 * Account DAO
 * @author Kalinin_DP
 *
 */
public interface AccountDAO{
	/**
	 * Persists accounts in database
	 * @param accounts accounts to persist
	 */
	void addAccounts(Account...accounts);
	/**
	 * Gets accoutns from database
	 * @return accounts
	 */
	Collection<Account> getAccounts();
	/**
	 * Gets account by ID
	 * @param id account ID
	 * @return optional of account
	 */
	Optional<Account> getAccountById(String id);
	/**
	 * Gets accounts beloning to specified <code>customer</code>
	 * @param customerId customer ID
	 * @return specified custemer's accounts
	 */
	Collection<Account> getAccounts(String customerId);
	/**
	 * Gets current system balance
	 * @return
	 */
	Balance getBalance();
	
}
package com.x.dao;

import java.util.Collection;
import java.util.Optional;

import com.x.models.Account;
import com.x.models.Balance;

public interface AccountDAO{

	void addAccounts(Account...accounts);

	Collection<Account> getAccounts();

	Optional<Account> getAccountById(String id);

	Collection<Account> getAccounts(String customerId);
	
	Balance getBalance();
	
}
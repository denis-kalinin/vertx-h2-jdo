package com.x.models;

import java.util.Collection;

public interface AccountDAO{

	void addAccounts(Account...accounts);

	Collection<Account> getAccounts();

	Account getById(String id);

	Collection<Account> getAccounts(String customerId);
	
}
package com.x.models;

import java.util.Collection;
import java.util.Optional;

public interface AccountDAO{

	void addAccounts(Account...accounts);

	Collection<Account> getAccounts();

	Optional<Account> getAccountById(String id);

	Collection<Account> getAccounts(String customerId);
	
}
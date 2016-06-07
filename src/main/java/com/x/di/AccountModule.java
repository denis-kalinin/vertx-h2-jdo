package com.x.di;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.google.inject.AbstractModule;
import com.x.models.AccountDAO;
import com.x.models.AccountDAOImpl;

public class AccountModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AccountDAO.class).to(AccountDAOImpl.class);
		
		
	}

}

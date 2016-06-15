package com.x.models;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;


@Singleton
public class AccountDAOImpl implements AccountDAO {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountDAO.class);
	
	@Inject
	private PersistenceManagerFactory pmf;
	/*
	public AccountDAOImpl() throws URISyntaxException, MalformedURLException, ClassNotFoundException{
		URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
		Path jarPath = Paths.get(jarUrl.toURI());
		LOG.info("Path is {} URL is {}", jarPath, jarUrl.getPath());
		File jarFile = jarPath.toFile();
		if(jarFile.isFile()) jarFile = jarFile.getParentFile();
		File databaseFile = new File(jarFile, "bank.db");
		URL databaseFileURL = databaseFile.toURI().toURL();
		LOG.info("Database path: {}", databaseFileURL);
		//databaseServer = org.h2.tools.Server.createTcpServer().start();
		Class.forName("org.h2.Driver");
		String databaseUrl = "jdbc:h2:"+databaseFileURL.toString();
		LOG.debug("Database URL: {}", databaseUrl);
		Map<String, Object> props = new HashMap<>();
		props.put("datanucleus.ConnectionDriverName", "org.h2.Driver");
		props.put("datanucleus.ConnectionURL", databaseUrl);
		props.put("datanucleus.ConnectionUserName", "sa");
		props.put("datanucleus.ConnectionPassword", "");
		////////additional/////////
		props.put("datanucleus.attachSameDatastore", true);
		props.put("datanucleus.schema.autoCreateAll", true);
		props.put("datanucleus.schema.validateTables", false);
		props.put("datanucleus.schema.validateConstraints", false);
		pmf = JDOHelper.getPersistenceManagerFactory(props);
	}
	*/
	@Override
	public void addAccounts(Account ... accounts){
		PersistenceManager pm = pmf.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try{
			tx.begin();
			for(Account acc : accounts){
				pm.makePersistent(acc);
				pm.setDetachAllOnCommit(true);
			}
			tx.commit();
		}finally{
			if (tx.isActive()){tx.rollback();}
		}
		pm.close();
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

}


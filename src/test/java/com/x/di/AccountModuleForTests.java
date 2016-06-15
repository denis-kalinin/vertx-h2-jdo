package com.x.di;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;


import com.google.inject.AbstractModule;
import com.x.models.AccountDAO;
import com.x.models.AccountDAOImpl;
import com.x.models.TransferDAO;
import com.x.models.TransferDAOImpl;

public class AccountModuleForTests extends AbstractModule {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountModule.class);

	@Override
	protected void configure() {
		bind(AccountDAO.class).to(AccountDAOImpl.class);
		bind(TransferDAO.class).to(TransferDAOImpl.class);
		try {
			PersistenceManagerFactory pmf = getPmf();
			bind(PersistenceManagerFactory.class).toInstance(pmf);
		} catch (MalformedURLException | ClassNotFoundException | URISyntaxException e) {
			addError(e);
		}
		
	}
	
	
	private PersistenceManagerFactory getPmf() throws MalformedURLException, ClassNotFoundException, URISyntaxException{
		URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
		Path jarPath = Paths.get(jarUrl.toURI());
		LOG.info("Path is {} URL is {}", jarPath, jarUrl.getPath());
		File jarFile = jarPath.toFile();
		if(jarFile.isFile()) jarFile = jarFile.getParentFile();
		String uuid = UUID.randomUUID().toString();
		File databaseFile = new File(jarFile, "bank-test-"+uuid+".db");
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
		return JDOHelper.getPersistenceManagerFactory(props);
	}

}


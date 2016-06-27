package com.x.di;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.x.codecs.AccountMessageCodec;
import com.x.codecs.BalanceMessageCodec;
import com.x.codecs.TransferMessageCodec;
import com.x.dao.AccountDAO;
import com.x.dao.AccountDAOImpl;
import com.x.dao.TransferDAO;
import com.x.dao.TransferDAOImpl;
import com.x.models.Account;
import com.x.models.Balance;
import com.x.models.Transfer;

import io.vertx.core.eventbus.MessageCodec;

public class AccountModule extends AbstractModule {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountModule.class);

	@Override
	protected void configure() {
		bind(AccountDAO.class).to(AccountDAOImpl.class);
		bind(TransferDAO.class).to(TransferDAOImpl.class);
		bind(new TypeLiteral<MessageCodec<Account, Account>>(){}).to(AccountMessageCodec.class);
		bind(new TypeLiteral<MessageCodec<Transfer, Transfer>>(){}).to(TransferMessageCodec.class);
		bind(new TypeLiteral<MessageCodec<Balance, Balance>>(){}).to(BalanceMessageCodec.class);
		try {
			PersistenceManagerFactory pmf = getPmf();
			bind(PersistenceManagerFactory.class).toInstance(pmf);
		} catch (MalformedURLException | ClassNotFoundException | URISyntaxException e) {
			addError(e);
		}
		try{
			Handlebars handlebars = new Handlebars();
			Template template = handlebars.compile(source("/raml/accounts.yaml"));
			bind(Template.class).toInstance(template);
		} catch (IOException e){
			addError(e);
		}
		
	}
	
	
	protected PersistenceManagerFactory getPmf() throws MalformedURLException, ClassNotFoundException, URISyntaxException{
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
		return JDOHelper.getPersistenceManagerFactory(props);
	}
	
	public static TemplateSource source(final String filename) throws IOException {
		TemplateSource source = new URLTemplateSource(filename, AccountModule.class.getResource(filename));
		return source;
	}

}

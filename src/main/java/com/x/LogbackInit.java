package com.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import com.x.services.MainVerticle;
import com.x.util.JarUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


public class LogbackInit {
	/*
	static {
		try {
			URL jarUrl = MainVerticle.class.getProtectionDomain().getCodeSource().getLocation();
			Path jarPath = Paths.get(jarUrl.toURI());
			File jarFile = jarPath.toFile();
			if( jarFile.isFile() ){ //look for logback.groovy file near this jar-file
				File logbackFile = new File(jarFile.getParent(), "logbackConfig.groovy");
				System.out.println("Looking for file " + logbackFile.getAbsolutePath());
				if(logbackFile.isFile()){
					System.out.println("LOGBACK config file is found!");
					System.setProperty("logback.configurationFile", logbackFile.getAbsolutePath());
				}
			}
		} catch (URISyntaxException e) {
			//use embedded logback.groovy
		}
	}
	*/
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Launcher.class);
	
	public static void start(){
		if(System.getProperty("logback.configurationFile") == null){
			try {
				Optional<File> jarOptFile = JarUtils.getThisJarFile();
				if(jarOptFile.isPresent()){ //look for logback.groovy file near this jar-file
					File logbackFile = new File(jarOptFile.get().getParent(), "logbackConfig.groovy");
					System.out.println("Looking for file " + logbackFile.getAbsolutePath());
					if(logbackFile.isFile()){
						System.out.println("LOGBACK config file is found!");
						//System.setProperty("logback.configurationFile", logbackFile.getAbsolutePath());
						LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
						loggerContext.reset();
						GafferConfigurator configurator = new GafferConfigurator(loggerContext);
						configurator.run(logbackFile);
						/*
						JoranConfigurator configurator = new JoranConfigurator();
						try (InputStream configStream = FileUtils.openInputStream(logbackFile)) {
							configurator.setContext(loggerContext);
							configurator.doConfigure(configStream); // loads logback file
							configStream.close();
						} catch (IOException | JoranException e) {
							throw new Exception("Error reading logback configuration", e);
						}
						*/
					}
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				//use embedded logback.groovy
			}
		}
		LOG.info("=========== START =============");
	}
}

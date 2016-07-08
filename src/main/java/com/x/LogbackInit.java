package com.x;

import java.io.File;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import com.x.util.JarUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;

public class LogbackInit {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Launcher.class);
	
	/**
	 * <p>Loads <a href="http://logback.qos.ch/">logback</a> configuration from auxiliary
	 * <code>logbackConfig.groovy</code> file residing next to application's jar-file.</p>
	 * <p><strong>NB!</strong> if JVM argument <code>logback.configurationFile</code>
	 * is defined&mdash;the method do nothing.</p>
	 */
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

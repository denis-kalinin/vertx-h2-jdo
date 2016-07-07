package com.x.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;


public class JavadocHandler {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JavadocHandler.class);
	/**
	 * Gets handler for serving Javadoc if application is launched from jar-file
	 * @return Optional of handler
	 */
	public static Optional<Handler<RoutingContext>> getHandler(){
		try{
			URL jarUrl = JavadocHandler.class.getProtectionDomain().getCodeSource().getLocation();
			Path jarPath = Paths.get(jarUrl.toURI());
			File jarFile = jarPath.toFile();
			if( jarFile.isFile() ){
				Path javadocPath = jarFile.toPath().getParent().resolve("javadoc");
				if(javadocPath.toFile().isDirectory()){
					LOG.info("/javadoc/* is served from {}", javadocPath.toString());
					StaticHandler javadocHandler = StaticHandler.create(javadocPath.toString())
						.setFilesReadOnly(true);
					return Optional.of(javadocHandler);
				}
			}else{
				LOG.warn("Javadoc is not available on site, becuase app is not running from jar-file");
			}
		}catch (Exception e){
			if(LOG.isTraceEnabled()){
				LOG.error("Failed to get URL for source location", e);
			}else{
				LOG.error("{}", e.getMessage());
			}
		}
		return Optional.empty();
	}

}

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
					StaticHandler javadocHandler = StaticHandler.create("C:\\Users\\Kalinin_DP\\Documents\\UK\\workspace\\vertx-h2-jdo\\build\\awsCodeDeploy\\vertx-h2-jdo\\javadoc")
						.setFilesReadOnly(true);
					return Optional.of(javadocHandler);
				}
			}
		}catch (Exception e){}
		return Optional.empty();
	}

}

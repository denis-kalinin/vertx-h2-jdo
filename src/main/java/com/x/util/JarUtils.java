package com.x.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class JarUtils {
	/**
	 * Gets Jar-File containing this application
	 * @return Optional with application jar-file.
	 */
	public static Optional<File> getThisJarFile(){
		try{
			URL jarUrl = JarUtils.class.getProtectionDomain().getCodeSource().getLocation();
			Path jarPath = Paths.get(jarUrl.toURI());
			File jarFile = jarPath.toFile();
			if(jarFile.isFile()){
				return Optional.of(jarFile);
			}
		}catch(Exception e){}
		return Optional.empty();
	}

}

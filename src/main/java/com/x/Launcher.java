package com.x;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.jar.Attributes;
import java.util.jar.Manifest;


public class Launcher {

	public static void main(String[] args){
		//String className = Launcher.class.getName().replace('.', '/');
		//String classJar = Launcher.class.getResource("/" + className + ".class").toString();
		//if (classJar.startsWith("jar:")) {}
		//System.setProperty("file.encoding", "UTF-8");
		System.out.println("Vert.x application starting...");
		if(args.length > 0){
			io.vertx.core.Launcher.main(args);
		}else{
			io.vertx.core.Launcher.main(new String[]{"run", com.x.services.MainVerticle.class.getName()});
		}
	}
	/**
	 * @return <code>true</code> if <code>Main-Verticle</code> attribute is found in <code>MANIFEST.MF</code>
	 */
	private static boolean hasMainVerticle(){
		URL manifestUrl = Launcher.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
		try(InputStream stream = manifestUrl.openStream()){
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String theMainVerticle = attributes.getValue("Main-Verticle");
			if(theMainVerticle==null){
				throw new IllegalStateException("Main-Verticle is not found in META-INF/MANIFEST.MF");
			}
			return true;
		} catch (IOException e) {
			System.err.println("META-INF/MANIFEST.MF is not found");
		} catch (IllegalStateException e){
			System.err.println(e.getMessage());
		}
		return false;
	}
}

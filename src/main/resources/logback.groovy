import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.TRACE

import org.fusesource.jansi.AnsiConsole


import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;



if(isJUnitTest()){
	appender("console", ConsoleAppender) {
		encoder(PatternLayoutEncoder) {
			pattern = "%date{HH:mm:ss,UTC}|%-5.5level|%-25.-25logger{0}|%-360.-360msg| %-25.-25thread |%20.20method|%caller{1}"
		}
	}
	logger("com.x", INFO, ["console"], false)
	root(INFO, ["console"])
}else{
	Optional<File> jarOptFile = com.x.util.JarUtils.thisJarFile;
	if(jarOptFile.isPresent()){
		println "Application is jar file!"
		/*
		appender("console", ConsoleAppender)
			encoder(PatternLayoutEncoder) {
				pattern = "%msg%n"
			}
		root(INFO, ["console"])
		*/
	} else {
		withJansi = true
		appender("console", ConsoleAppender) {
			encoder(PatternLayoutEncoder) {
				pattern = "%date{HH:mm:ss,UTC}|%highlight(%-5.5level)|%-25.-25logger{0}|%highlight(%-360.-360msg)| %-25.-25thread |%20.20method|%caller{1}"
			}
		}
		logger("com.x", DEBUG, ["console"], false)
		root(DEBUG, ["console"])
	}
}


public static boolean isJUnitTest() {
	for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
		if (element.getClassName().startsWith("org.junit.")) {
			return true;
		}
	}
	return false;
}
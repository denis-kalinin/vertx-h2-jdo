import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.TRACE

import org.fusesource.jansi.AnsiConsole

import com.github.jknack.handlebars.internal.path.ThisPath;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;



def script = this


enum LogAppender {
	FILE,
	CONSOLE,
	COLORED_CONSOLE
}
def logAppender = LogAppender.FILE
//def logAppender = LogAppender.CONSOLE
//def logAppender = LogAppender.COLORED_CONSOLE


switch (logAppender){
	case LogAppender.CONSOLE :
		appender("console", ConsoleAppender) {
			encoder(PatternLayoutEncoder) {
				pattern = "%date{HH:mm:ss,UTC}|%-5.5level|%-25.-25logger{0}|%-360.-360msg| %-25.-25thread |%20.20method|%caller{1}"
			}
		}
		logger("com.x", DEBUG, ["console"], false)
		root(DEBUG, ["console"])
		break
	case LogAppender.COLORED_CONSOLE :
		appender("console", ConsoleAppender) {
			withJansi = true
			encoder(PatternLayoutEncoder) {
				pattern = "%highlight(%-5.5level)  %date{HH:mm:ss,UTC}   %logger{0}%n    : %msg%n"
			}
		}
		logger("com.x", DEBUG, ["console"], false)
		root(WARN, ["console"])
		break
	case LogAppender.FILE:
		appender("FILE", RollingFileAppender) {
			URL jarUrl = com.x.services.AccountVerticle.class.getProtectionDomain().getCodeSource().getLocation();
			Path jarPath = Paths.get(jarUrl.toURI());
			File appDir = jarPath.toFile();
			if(appDir.isFile()) appDir = appDir.getParentFile();
			File logsDir = new File(appDir, "logs");
			File logFile = new File(logsDir, "application.log");
			file = logFile
			rollingPolicy(TimeBasedRollingPolicy){
				String fpattern = "${logsDir}" + File.separator + "app.%d{yyyy-MM-dd_HH-mm}.log"
				println "LOG file pattern - ${fpattern}"
				fileNamePattern = fpattern
				maxHistory = 12
			}
			encoder(PatternLayoutEncoder) {
				pattern = "%date{HH:mm:ss,UTC}|%-5.5level|%-25.-25logger{0}|%-360.-360msg| %-25.-25thread |%20.20method|%caller{1}"
			}
		}
		logger("com.x", TRACE, ["FILE"], false)
		root(WARN, ["FILE"])
		break
}

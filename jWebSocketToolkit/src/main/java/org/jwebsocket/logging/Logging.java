//	---------------------------------------------------------------------------
//	jWebSocket - Shared Logging Support
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.logging;

import java.io.IOException;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 * Provides the common used jWebSocket logging support based on
 * Apache's log4j.
 * @author aschulze
 */
public class Logging {

	private static PatternLayout layout = null;
	private static Appender appender = null;
	private static Level logLevel = Level.DEBUG;
	/**
	 *
	 */
	public static int CONSOLE = 0;
	/**
	 *
	 */
	public static int ROLLING_FILE = 1;
	/**
	 *
	 */
	public static int SINGLE_FILE = 2;
	/**
	 *
	 */
	public static String LOG_FILENAME = "jWebSocket.log";
	/**
	 *
	 */
	public static int BUFFER_SIZE = 2048;
	private static int logTarget = CONSOLE; // ROLLING_FILE;

	// TODO: Load the conversion pattern and the logging target from a configuration file (e.g. jWebSocket.xml)
	/**
	 * Initializes the Apache log4j system to produce the desired logging
	 * output.
	 * @param aLogLevel one of the values TRACE, DEBUG, INFO, WARN, ERROR or FATAL.
	 *
	 */
	private static void checkLogAppender() {
		if (layout == null) {
			layout = new PatternLayout();
			layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n");
		}
		if (appender == null) {
			if (1 == logTarget) {
				try {
					appender = new RollingFileAppender(layout, LOG_FILENAME, true);
					((RollingFileAppender) appender).setBufferedIO(true);
					((RollingFileAppender) appender).setBufferSize(BUFFER_SIZE);
					((RollingFileAppender) appender).setEncoding("UTF-8");
				} catch (IOException ex) {
					appender = new ConsoleAppender(layout);
				}
			} else if (2 == logTarget) {
				try {
					appender = new FileAppender(layout, LOG_FILENAME, true);
					((FileAppender) appender).setBufferedIO(true);
					((FileAppender) appender).setBufferSize(BUFFER_SIZE);
					((FileAppender) appender).setEncoding("UTF-8");
				} catch (IOException ex) {
					appender = new ConsoleAppender(layout);
				}
			} else {
				appender = new ConsoleAppender(layout);
			}
		}
	}

	/**
	 * Initializes the jWebSocket logging system with the given log level.
	 * All subsequently instantiated class specific loggers will use this
	 * setting.
	 * @param aLogLevel
	 */
	public static void initLogs(String aLogLevel) {
		logLevel = Level.toLevel(aLogLevel);
		checkLogAppender();
	}

	/**
	 * closes the log file. Take care that no further lines are appended
	 * to the logs after it has been closed!
	 */
	public static void exitLogs() {
		if (appender != null) {
			// properly close log files if such
			appender.close();
		}
	}

	/**
	 * Returns a logger for a certain class by using the jWebSocket settings
	 * for logging and ignoring inherited log4j settings.
	 * @param aClass
	 * @return Logger the new logger for the given class.
	 */
	public static Logger getLogger(Class aClass) {
		checkLogAppender();
		Logger logger = Logger.getLogger(aClass);
		logger.addAppender(appender);
		// don't inherit global log4j settings, we intend to configure that
		// in our own jWebSocket.xml config file.
		logger.setAdditivity(false);
		logger.setLevel(logLevel);
		return logger;
	}
}

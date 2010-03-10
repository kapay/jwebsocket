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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Provides the common used jWebSocket logging support based on
 * Apache's log4j.
 * @author aschulze
 */
public class Logging {

	private static PatternLayout layout = null;
	private static ConsoleAppender consoleAppender = null;
	private static Level logLevel = Level.DEBUG;

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
		if (consoleAppender == null) {
			consoleAppender = new ConsoleAppender(layout);
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
	 * Returns a logger for a certain class by using the jWebSocket settings
	 * for logging and ignoring inherited log4j settings.
	 * @param aClass
	 * @return Logger the new logger for the given class.
	 */
	public static Logger getLogger(Class aClass) {
		checkLogAppender();
		Logger logger = Logger.getLogger(aClass);
		logger.addAppender(consoleAppender);
		// don't inherit global log4j settings, we intend to configure that
		// in our own jWebSocket.xml config file.
		logger.setAdditivity(false);
		logger.setLevel(logLevel);
		return logger;
	}
}

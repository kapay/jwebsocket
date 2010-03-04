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
package org.jWebSocket.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author aschulze
 */
public class Logging {

	/**
	 *
	 */
	public static Logger rootLogger = Logger.getRootLogger();

	// TODO: Load the conversion pattern and the logging target from a configuration file (e.g. jWebSocket.xml)

	/**
	 * Initializes the Apache log4j system to produce the desired logging
	 * output.
	 * @param aLogLevel
	 */
	public static void initLogs(String aLogLevel) {
		PatternLayout layout = new PatternLayout();
		// layout.setConversionPattern("%d{HH:mm:ss,SSS} %p - %C{1}: %m%n");
		layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n");
		// delete all potentially already existing appenders
		rootLogger.removeAllAppenders();

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.toLevel(aLogLevel));
	}

}

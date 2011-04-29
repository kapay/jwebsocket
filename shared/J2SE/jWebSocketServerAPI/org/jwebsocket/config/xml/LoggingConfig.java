//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Configuration for logging
 * User: puran
 *
 * @version $Id: LoggingConfig.java 616 2010-07-01 08:04:51Z fivefeetfurther $
 */
public class LoggingConfig implements Config {

	private final String mAppender;
	private final String mPattern;
	private final String mLevel;
	private final String mFilename;
	private final Integer mBufferSize;

	/**
	 * Costrutor
	 *
	 * @param appender the logging appender
	 * @param pattern  logging pattern
	 * @param level    the level of logging
	 * @param filename the log file name
	 */
	public LoggingConfig(String appender, String pattern, String level,
			String filename, Integer aBufferSize) {
		this.mAppender = appender;
		this.mPattern = pattern;
		this.mLevel = level;
		this.mFilename = filename;
		this.mBufferSize = aBufferSize;
	}

	public String getAppender() {
		return mAppender;
	}

	public String getPattern() {
		return mPattern;
	}

	public String getLevel() {
		return mLevel;
	}

	public String getFilename() {
		return mFilename;
	}

	public Integer getBufferSize() {
		return mBufferSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mAppender != null && mAppender.length() > 0)
				&& (mPattern != null && mPattern.length() > 0)
				&& (mLevel != null && mLevel.length() > 0)
				&& (mFilename != null && mFilename.length() > 0)
				&& (mBufferSize != null && mBufferSize >= 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the logging configuration directives, "
				+ "please check your configuration file");
	}
}

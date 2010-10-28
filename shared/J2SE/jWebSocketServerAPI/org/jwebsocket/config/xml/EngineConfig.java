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

import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.kit.WebSocketRuntimeException;

import java.util.List;
import org.jwebsocket.config.Config;

/**
 * Class that represents the engine config
 *
 * @author puran
 * @version $Id: EngineConfig.java 616 2010-07-01 08:04:51Z fivefeetfurther $
 */
public final class EngineConfig implements Config, EngineConfiguration {

	private final String mId;
	private final String mName;
	private final String mJar;
	private final int mPort;
	private final int mTimeout;
	private final int mMaxframesize;
	private final List<String> mDomains;

	/**
	 * Constructor for engine
	 *
	 * @param aId           the engine id
	 * @param aName         the name of the engine
	 * @param aJar          the jar file name
	 * @param aPort         the port number where engine runs
	 * @param aTimeout      the timeout value
	 * @param aMaxFrameSize the maximum frame size that engine will
	 *						receive without closing the connection
	 * @param aDomains      list of domain names
	 */
	public EngineConfig(String aId, String aName, String aJar, int aPort, 
			int aTimeout, int aMaxFrameSize, List<String> aDomains) {
		this.mId = aId;
		this.mName = aName;
		this.mJar = aJar;
		this.mPort = aPort;
		this.mTimeout = aTimeout;
		this.mMaxframesize = aMaxFrameSize;
		this.mDomains = aDomains;
		validate();
	}
	
	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return mId;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * @return the jar
	 */
	@Override
	public String getJar() {
		return mJar;
	}

	/**
	 * @return the port
	 */
	@Override
	public int getPort() {
		return mPort;
	}

	/**
	 * @return the timeout
	 */
	@Override
	public int getTimeout() {
		return mTimeout;
	}

	/**
	 * @return the max frame size
	 */
	@Override
	public int getMaxFramesize() {
		return mMaxframesize;
	}

	/**
	 * @return the domains
	 */
	@Override
	public List<String> getDomains() {
		return mDomains;
	}

	/**
	 * validate the engine configuration
	 *
	 * @throws WebSocketRuntimeException if any of the engine configuration is mising
	 */
	@Override
	public void validate() {
		if ((mId != null && mId.length() > 0)
				&& (mName != null && mName.length() > 0)
				&& (mJar != null && mJar.length() > 0)
				&& (mDomains != null && mDomains.size() > 0)
				&& mPort > 0 && mPort < 65536
				&& mTimeout >= 0) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the engine configuration, "
				+ "please check your configuration file");
	}
}

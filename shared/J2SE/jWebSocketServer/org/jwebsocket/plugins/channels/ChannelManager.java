//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelManager
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.channels;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jwebsocket.api.PluginConfiguration;

/**
 * Manager class responsible for all the channel operations within the jWebSocket 
 * server system.
 * @author puran
 * @version $Id$
 */
public class ChannelManager {

	/** channel store */
	private final ChannelStore channelStore = new BaseChannelStore();
	/** system channels map */
	private final Map<String, Channel> systemChannels = new ConcurrentHashMap<String, Channel>();
	/** user channels map */
	private final Map<String, Channel> publicChannels = new ConcurrentHashMap<String, Channel>();
	/**
	 * Logger channel
	 */
	private Channel loggerChannel = null;
	/**
	 * admin channel
	 */
	private Channel adminChannel = null;
	/**
	 * single instance of the channel manager
	 */
	private static final ChannelManager INSTANCE = new ChannelManager();

	/**
	 * don't allow this
	 */
	private ChannelManager() {
		throw new AssertionError();
	}

	/**
	 * @return the static manager instance
	 */
	public static ChannelManager getChannelManager() {
		return INSTANCE;
	}

	/**
	 * Starts the system channels within the jWebSocket system
	 * @param configuration the channel plugin configuration
	 */
	public void startSystemChannels(PluginConfiguration configuration) {
	}

	public void stopSystemChannels(PluginConfiguration pluginConfiguration) {
	}

	public Channel getChannel(String channelId) {
		if (systemChannels.containsKey(channelId)) {
			return systemChannels.get(channelId);
		}
		if (publicChannels.containsKey(channelId)) {
			return publicChannels.get(channelId);
		}
		//if not anywhere then look in the channel store
		Channel channel = channelStore.getChannel(channelId);
		if (channel != null) {
			publicChannels.put(channelId, channel);
		}
		return channel;
	}

	public void registerChannel(Channel channel) {
	}

	/**
	 * Returns the instance of the logger channel.
	 * @return the logger channel
	 */
	public Channel getLoggerChannel() {
		return loggerChannel;
	}

	/**
	 * Returns the instance of the admin channel
	 * @return the admin channel
	 */
	public Channel getAdminChannel() {
		return adminChannel;
	}
}

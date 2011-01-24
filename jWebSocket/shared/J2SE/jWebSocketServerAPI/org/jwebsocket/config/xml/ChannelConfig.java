// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 jwebsocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Represents the channels configuration information configured via jWebSocket.xml file
 * @author puran
 * @version $Id$
 */
public class ChannelConfig implements Config {

	private String id;
	private String name;
	private String secretKey;
	private String accessKey;
	private boolean privateChannel;
	private boolean systemChannel;
	private String owner;

	public ChannelConfig(String id, String name, boolean isPrivate, boolean isSystem, String secretKey, String accessKey,
			String owner) {
		this.id = id;
		this.name = name;
		this.secretKey = secretKey;
		this.accessKey = accessKey;
		this.privateChannel = isPrivate;
		this.systemChannel = isSystem;
		this.owner = owner;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @return the accessKey
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * @return the privateChannel
	 */
	public boolean isPrivateChannel() {
		return privateChannel;
	}

	/**
	 * @return the systemChannel
	 */
	public boolean isSystemChannel() {
		return systemChannel;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	@Override
	public void validate() {
		if ((id != null && id.length() > 0)
				&& (name != null && name.length() > 0)
				&& (owner != null && owner.length() > 0)
				&& (secretKey != null && secretKey.length() > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the engine configuration, "
				+ "please check your configuration file");
	}
}

//  ---------------------------------------------------------------------------
//  jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.api;

import java.util.List;
import java.util.Map;

/**
 * The Base interface for plugin configuration
 * @author puran
 * @version $Id$
 */
public interface PluginConfiguration extends Configuration {
  /**
   * @return the package
   */
  String getPackage();

  /**
   * @return the jar
   */
  String getJar();

  /**
   * @return the namespace
   */
  String getNamespace();

  /**
   * @return the list of servers
   */
  List<String> getServers();

  /**
   * @return the settings
   */
  Map<String, String> getSettings();

}
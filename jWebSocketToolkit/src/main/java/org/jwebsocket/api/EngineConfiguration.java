//  ---------------------------------------------------------------------------
//  jWebSocket - Copyright (c) 2010 jwebsocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//  more details.
//  You should have received a copy of the GNU General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.api;

import java.util.List;
/**
 * Engine Configuration
 * @author puran 
 * @version $Id:$
 */
public interface EngineConfiguration extends Configuration {
    /**
     * @return the jar file name
     */
    String getJar();
    /**
     * @return the port at which this engine runs
     */
    int getPort();
    /**
     * @return timeout value
     */
    int getTimeout();
    /**
     * @return the maximum frame size
     */
    int getMaxframesize();
    /**
     * @return the list of allowed domains
     */
    List<String> getDomains();
    
}

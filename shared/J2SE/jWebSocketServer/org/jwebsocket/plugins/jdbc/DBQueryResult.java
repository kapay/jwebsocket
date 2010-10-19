//	---------------------------------------------------------------------------
//	jWebSocket - DBQueryResult
//	Copyright (c) 2010 Innotrade GmbH (http://jWebSocket.org)
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
//  for more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * Provides a record with the resultset of a SQL select command.
 * Contains the resultset as well as the metadata, and optional exception and
 * message.
 * @since 1.0
 * @author Alexander Schulze
 */
public class DBQueryResult {

	public Statement sql = null;
	public ResultSet resultSet = null;
	public ResultSetMetaData metaData = null;
	public String exception = null;
	public String message = null;
}

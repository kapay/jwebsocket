//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Native SQL Access for JDBC Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jdbc;

import javax.sql.DataSource;
import org.jwebsocket.token.Token;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author aschulze
 */
public class NativeAccess {

	private JdbcTemplate mJDBCTemplate;

	public void setDataSource(DataSource aDataSource) {
		mJDBCTemplate = new JdbcTemplate(aDataSource);
	}

	public Token query(String aSQL) {
		SqlRowSet lRowSet = mJDBCTemplate.queryForRowSet(aSQL);
		return JDBCTools.resultSetToToken(lRowSet);
	}

	public int update(String aSQL) {
		return mJDBCTemplate.update(aSQL);
	}
}

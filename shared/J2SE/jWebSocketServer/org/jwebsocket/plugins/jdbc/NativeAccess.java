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
import org.jwebsocket.token.TokenFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author aschulze
 */
public class NativeAccess {

	private JdbcTemplate mJDBCTemplate;
	private String mSelectSequenceSQL = null;

	public void setDataSource(DataSource aDataSource) {
		mJDBCTemplate = new JdbcTemplate(aDataSource);
	}

	public void setSelectSequenceSQL(String aSQL) {
		mSelectSequenceSQL = aSQL;
	}

	public String getSelectSequenceSQL() {
		return mSelectSequenceSQL;
	}

	public Token query(String aSQL) {
		Token lResToken;
		try {
			SqlRowSet lRowSet = mJDBCTemplate.queryForRowSet(aSQL);
			lResToken = JDBCTools.resultSetToToken(lRowSet);
		} catch (Exception lEx) {
			lResToken = TokenFactory.createToken();
			lResToken.setInteger("code", -1);
			lResToken.setString("msg",
					lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		return lResToken;
	}

	public Token update(String aSQL) {
		Token lResToken = TokenFactory.createToken();
		int lAffectedRows = 0;
		try {
			lAffectedRows = mJDBCTemplate.update(aSQL);
			lResToken.setInteger("code", 0);
		} catch (Exception lEx) {
			lResToken.setInteger("code", -1);
			lResToken.setString("msg",
					lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		lResToken.setInteger("rowsAffected", lAffectedRows);
		return lResToken;
	}
	
	public Token exec(String aSQL) {
		Token lResToken = TokenFactory.createToken();
		try {
			mJDBCTemplate.execute(aSQL);
			lResToken.setInteger("code", 0);
		} catch (Exception lEx) {
			lResToken.setInteger("code", -1);
			lResToken.setString("msg",
					lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		return lResToken;
	}
	
}

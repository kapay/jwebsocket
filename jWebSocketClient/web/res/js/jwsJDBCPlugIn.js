//	---------------------------------------------------------------------------
//	jWebSocket Sample Client PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
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


//	---------------------------------------------------------------------------
//  jWebSocket Sample Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.JDBCPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.JDBCPlugIn[/tt] class.
jws.JDBCPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.jdbc (jws.NS_BASE + ".plugins.jdbc")
	//:d:en:Namespace for the [tt]JDBCPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.jdbc",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.JDBCPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "select" == aToken.reqType ) {
				if( this.OnJDBCResult ) {
					this.OnJDBCResult( aToken );
				}
			}
		}
	},

	jdbcQuerySQL: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "querySQL",
				sql: aQuery
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcUpdateSQL: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "updateSQL",
				sql: aQuery
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcExecSQL: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "execSQL",
				sql: aQuery
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcSelect: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "select",
				table: aQuery.table,
				fields: aQuery.fields,
				order: aQuery.order,
				where: aQuery.where,
				group: aQuery.group,
				having: aQuery.having
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setJDBCCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnJDBCResult !== undefined ) {
			this.OnJDBCResult = aListeners.OnJDBCResult;
		}
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.JDBCPlugIn );

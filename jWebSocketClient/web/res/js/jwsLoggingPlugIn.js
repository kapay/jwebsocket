//	---------------------------------------------------------------------------
//	jWebSocket Logging PlugIn (uses jWebSocket Client and Server)
//	(C) 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
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
//  jWebSocket Logging Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.LoggingPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.LoggingPlugIn[/tt] class.
jws.LoggingPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.Logging (jws.NS_BASE + ".plugins.logging")
	//:d:en:Namespace for the [tt]LoggingPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.logging",

	DEBUG: "debug",
	INFO: "info",
	WARN: "warn",
	ERROR: "error",
	FATAL: "fatal",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.LoggingPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "log" == aToken.reqType ) {
				if( this.OnLogged ) {
					this.OnLogged( aToken );
				}
			}
		}
	},

	loggingLog: function( aLevel, aInfo, aMessage, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.LoggingPlugIn.NS,
				type: "log",
				level: aLevel,
				info: aInfo,
				message: aMessage
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setLoggingCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnLogged !== undefined ) {
			this.OnLogged = aListeners.OnLogged;
		}
	}

}

// add the JWebSocket Logging PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.LoggingPlugIn );

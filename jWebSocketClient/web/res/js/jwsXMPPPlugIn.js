//	---------------------------------------------------------------------------
//	jWebSocket XMPP PlugIn (uses jWebSocket Client and Server)
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
//  jWebSocket XMPP Client Plug-In
//	---------------------------------------------------------------------------

jws.XMPPPlugIn = {

	// namespace for xmpp plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.xmpp",

	AVAILABLE: "",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.XMPPPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "login" == aToken.reqType ) {
				if( this.onXMPPRequestToken ) {
					this.onXMPPRequestToken( aToken );
				}
			}
		}
	},

	xmppConnect: function( aHost, aPort, aDomain, aUseSSL, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// XMPP API calls XMPP Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "connect",
				host: aHost,
				port: aPort,
				domain: aDomain,
				useSSL: aUseSSL
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppDisconnect: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "disconnect"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppLogin: function( aUsername, aPassword, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// XMPP API calls XMPP Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "login",
				username: aUsername,
				password: aPassword
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppLogout: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "logout"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppRoster: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "getRoster"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppSetStatus: function( aStatus, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "setStatus",
				status: aStatus
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setXMPPCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		/*
		if( aListeners.onXMPPRequestToken !== undefined ) {
			this.onXMPPRequestToken = aListeners.onXMPPRequestToken;
		}
		*/
	}

}

// add the JWebSocket XMPP PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.XMPPPlugIn );

//	---------------------------------------------------------------------------
//	jWebSocket Authentication Client PlugIn (uses jWebSocket Client and Server)
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
//  jWebSocket Authentication Client Plug-In
//	---------------------------------------------------------------------------

jws.AuthPlugIn = {

	// namespace for filesystem plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.auth",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.AuthPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			/*
			if( "load" == aToken.reqType ) {
				if( aToken.code == 0 ) {
					aToken.data = Base64.decode( aToken.data );
					if( this.OnFileLoaded ) {
						this.OnFileLoaded( aToken );
					}
				} else {
					if( this.OnFileError ) {
						this.OnFileError( aToken );
					}
				}
			} else if( "event" == aToken.type ) {
				if( "filesaved" == aToken.name ) {
					if( this.OnFileSaved ) {
						this.OnFileSaved( aToken );
					}
				} else if( "filesent" == aToken.name ) {
					if( this.OnFileSent ) {
						this.OnFileSent( aToken );
					}
				}
			}
			*/
		}
	},

	authLogin: function( aUsername, aPassword, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.AuthPlugIn.NS,
				type: "logon",
				username: aUsername,
				password: aPassword
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	authLogout: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.AuthPlugIn.NS,
				type: "logoff"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setFileSystemCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		/*
		if( aListeners.OnFileLoaded !== undefined ) {
			this.OnFileLoaded = aListeners.OnFileLoaded;
		}
		*/
	}

}

// add the JWebSocket Authentication PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.AuthPlugIn );

//	---------------------------------------------------------------------------
//	jWebSocket Client (uses jWebSocket Server)
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------


// create name space "jws" for jWebSocket client
var jws = {};

jws.NS_BASE = "org.jWebSocket";

// some namespace global constants
jws.CUR_TOKEN_ID = 0;
jws.JWS_SERVER_URL = "ws://" + ( self.location.hostname ? self.location.hostname : "localhost" ) + ":8787";

jws.$ = function( aId ) {
	return document.getElementById( aId );
};


//	---------------------------------------------------------------------------
//  jWebSocket - some convenience JavaScript OOP tools
//	---------------------------------------------------------------------------
jws.oop = {};

// implement simple class declaration to support multi-level inheritance
// and easy 'inherited' calls (super-calls) in JavaScript
jws.oop.declareClass = function( aNamespace, aClassname, aAncestor, aFields ) {
	var lNS = self[ aNamespace ];
	if( !lNS ) { 
		self[ aNamespace ] = { };
	}
	var lConstructor = function() {
		if( this.create ) {
			this.create.apply( this, arguments );
		}
	};
	lNS[ aClassname ] = lConstructor;

	var lField;
	for( lField in aFields ) {
		lConstructor.prototype[ lField ] = aFields[ lField ];
	}
	if( aAncestor != null ) {
		for( lField in aAncestor.prototype ) {
			var lAncMthd = aAncestor.prototype[ lField ];
			if( typeof lAncMthd == "function" ) {
				if( lConstructor.prototype[ lField ] ) {
					lConstructor.prototype[ lField ].inherited = lAncMthd;
				} else {
					lConstructor.prototype[ lField ] = lAncMthd;
				}
				// every method gets a reference to its super class
				// to allow class to inherited method from such
				lConstructor.prototype[ lField ].superClass = aAncestor;
			}
		}
	}
}

// plug-in functionality to allow to add plug-ins into existing classes
jws.oop.addPlugIn = function( aClass, aPlugIn ) {
	for( var lField in aPlugIn ) {
		aClass.prototype[ lField ] = aPlugIn[ lField ];
	}
}


//	---------------------------------------------------------------------------
//  jWebSocket - Base Client
//  This class does not handle exceptions or error, it throws exceptions,
//  which are handled by the descendant classes.
//	---------------------------------------------------------------------------

// declaration for the jws.jWebSocketBaseClient class
jws.oop.declareClass( "jws", "jWebSocketBaseClient", null, {

	processOpenEvent: function( aEvent ) {
	// can to be overwritten in descendant classes
	// to easily handle open event in descendants
	},

	processMessageEvent: function( aEvent ) {
	// can to be overwritten in descendant classes
	// to easily handle message event in descendants
	},

	processCloseEvent: function( aEvent ) {
	// can to be overwritten in descendant classes
	// to easily handle open event in descendants
	},

	connect: function( aURL, aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		// if browser support WebSockets at all...
		if( self.WebSocket) {

			// if connection not already established...
			if( !this.fConn ) {
				var lThis = this;
				var lValue = null;

				// create a new web socket instance
				this.fConn = new WebSocket( aURL );

				// assign the listeners to local functions (closure) to allow
				// to handle event before and after the application
				this.fConn.onopen = function( aEvent ) {
					lValue = lThis.processOpenEvent( aEvent );
					// give application change to handle event
					if( aOptions.OnOpen ) {
						aOptions.OnOpen( aEvent, lValue, lThis );
					}
				};

				this.fConn.onmessage = function( aEvent ) {
					lValue = lThis.processMessageEvent( aEvent );
					// give application change to handle event first
					if( aOptions.OnMessage ) {
						aOptions.OnMessage( aEvent, lValue, lThis );
					}
				};

				this.fConn.onclose = function( aEvent ) {
					// check if still disconnect timeout active and clear if needed
					if( lThis.hDisconnectTimeout ) {
						clearTimeout( lThis.hDisconnectTimeout );
						delete lThis.hDisconnectTimeout;
					}
					lValue = lThis.processCloseEvent( aEvent );
					// give application change to handle event
					if( aOptions.OnClose ) {
						aOptions.OnClose( aEvent, lValue, lThis );
					}
					lThis.fConn = null;
				}
			} else {
				throw new Error( "Already connected" );
			}
		} else {
			throw new Error( "WebSockets not supported by browser" );
		}
	},

	sendStream: function( aData ) {
		// is client already connected
		if( this.isConnected() ) {
			this.fConn.send( aData );
		// if not raise exception
		} else {
			throw new Error( "Not connected" );
		}
	},

	isConnected: function() {
		return( this.fConn && this.fConn.readyState == 1 );
	},

	forceClose: function() {
		if( this.fConn ) {
			this.fConn.close();
			this.fConn.onopen = null;
			this.fConn.onmessage = null;
			this.fConn.onclose = null;
		}
		this.fConn = null;
	},

	disconnect: function( aOptions ) {
		// check if timeout option is used
		var lTimeout = 0;
		if( aOptions ) {
			if( aOptions.timeout ) {
				lTimeout = aOptions.timeout;
			}
		}
		// connection established at all?
		if( this.fConn ) {
			if( lTimeout <= 0 ) {
				this.forceClose();
			} else {
				var lThis = this;
				this.hDisconnectTimeout = setTimeout(
					function() {
						lThis.forceClose.call( lThis );
					},
					lTimeout
					);
			}
		// throw exception if not connected
		} else {
			throw new Error( "Not connected" );
			this.fConn = null;
		}
	}
});


//	---------------------------------------------------------------------------
//  jWebSocket token client (this is an abstract class)
//  don't create direct instances of jWebSocketTokenClient
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketTokenClient", jws.jWebSocketBaseClient, {

	// this method is called by the contructor of this class
	create: function() {
		this.fRequestCallbacks = {};
	},

	checkCallbacks: function( aToken ) {
		var lField = "utid" + aToken.utid;
		// console.log("checking result for utid: " + aToken.utid + "...");
		var lClbkRec = this.fRequestCallbacks[ lField ];
		if( lClbkRec ) {
			lClbkRec.callback.call( this, aToken );
			delete this.fRequestCallbacks[ lField ];
		}
		// todo: delete timed out requests and optionally fire timeout callbacks
		for( lField in this.fRequestCallbacks ) {
			// ..
			}
	},

	createDefaultResult: function() {
		jws.CUR_TOKEN_ID++;
		return{
			code: 0,
			msg: "Ok",
			localeKey: "jws.jsc.res.Ok",
			args: null,
			tid: jws.CUR_TOKEN_ID
		};
	},

	resultToString: function( aRes ) {
		return(
			aRes.msg
			// + " (code: " + aRes.code + ", tid: " + aRes.tid + ")"
			);
	},

	tokenToStream: function( aToken ) {
		// this is supposed to convert a token into a string stream which is
		// send to the server, not implemented in base class.
		// needs to be overwritten in descendant classes!
		throw new Error( "tokenToStream needs to be overwritten in descendant classes" );
	},

	streamToToken: function( aStream ) {
		// this is supposed to convert a string stream from the server into 
		// a token (object), not implemented in base class.
		// needs to be overwritten in descendant classes
		throw new Error( "streamToToken needs to be overwritten in descendant classes" );
	},

	processMessageEvent: function( aEvent ) {
		// parse incoming token and process it...
		var lToken = this.streamToToken( aEvent.data );
		// check welcome and goodBye tokens to manage the session
		if( lToken.type == "welcome" && lToken.usid ) {
			this.fSessionId = lToken.usid;
		} else if( lToken.type == "goodBye" ) {
			this.fSessionId = null;
		// check if we got a response from a previous request
		} else if( lToken.type == "response" ) {
			// check login and logout manage the username
			if( lToken.reqType == "login" ) {
				this.fUsername = lToken.username;
			}
			if( lToken.reqType == "logout" ) {
				this.fUsername = null;
			}
			// check if some requests need to be answered
			this.checkCallbacks( lToken );
		}
		return lToken;
	},

	processCloseEvent: function( aEvent ) {
		// in case of a server side close event...
		this.fConn = null;
		// reset the session...
		this.fSessionId = null;
		// and the username as well
		this.fUsername = null;
	},

	sendToken: function( aToken, aOptions ) {
		var lOnResponse = null;
		if( aOptions ) {
			if( aOptions.OnResponse ) {
				lOnResponse = aOptions.OnResponse;
			}
		}
		if( lOnResponse ) {
			this.fRequestCallbacks[ "utid" + jws.CUR_TOKEN_ID ] = {
				request: new Date().getTime(),
				callback: lOnResponse
			}
		}
		var lStream = this.tokenToStream( aToken );
		this.sendStream( lStream );
	},

	sendText: function( aReceiver, aText ) {
		var lRes = this.createDefaultResult();
		if( this.isLoggedIn() ) {
			this.sendToken({
				type: "send",
				receiver: aReceiver,
				data: aText
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	broadcastText: function( aPool, aText, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isLoggedIn() ) {
			this.sendToken({
				type: "broadcast",
				pool: aPool,
				data: aText
			},
			aOptions
			);
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	echo: function( aData ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				type: "echo",
				data: aData
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	connect: function( aURL, aOptions ) {
		var lRes = this.createDefaultResult();
		try {
			// call inherited connect, catching potential exception
			arguments.callee.inherited.call( this, aURL, aOptions );
		} catch( ex ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.ex";
			lRes.args = [ ex.message ];
			lRes.msg = "Exception on connect: " + ex.message;
		}
		return lRes;
	},

	disconnect: function( aOptions ) {
		var lTimeout = 0;
		if( aOptions ) {
			if( aOptions.timeout ) {
				lTimeout = aOptions.timeout;
			}
		}
		var lRes = this.createDefaultResult();
		try {
			// if connected and timeout is passed give server a chance to
			// register the disconnect properly and send a good bye response.
			if( this.fConn  ) {
				this.sendToken({
					type: "close",
					timeout: lTimeout
				});
			}
			// call inherited disconnect, catching potential exception
			arguments.callee.inherited.call( this, aOptions );
		} catch( ex ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.ex";
			lRes.args = [ ex.message ];
			lRes.msg = "Exception on disconnect: " + ex.message;
		}
		return lRes;
	},

	login: function( aUsername, aPassword, aPool ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				type: "login",
				username: aUsername,
				password: aPassword,
				pool: aPool
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	logout: function() {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				type: "logout"
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	isLoggedIn: function() {
		return( this.isConnected() && this.fUsername );
	},

	getUsername: function() {
		return( this.isLoggedIn() ? this.fUsername : null );
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket Client System Plug-In
//	---------------------------------------------------------------------------

jws.SystemClientPlugIn = {

	// namespace for system plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.system",

	ALL_CLIENTS: 0,
	AUTHENTICATED: 1,
	NON_AUTHENTICATED: 2,

	getClients: function( aOptions ) {
		var lMode = jws.SystemClientPlugIn.ALL_CLIENTS;
		var lPool = null;
		if( aOptions ) {
			if( aOptions.mode == jws.SystemClientPlugIn.AUTHENTICATED ||
				aOptions.mode == jws.SystemClientPlugIn.NON_AUTHENTICATED ) {
				lMode = aOptions.mode
			}
			if( aOptions.pool ) {
				lPool = aOptions.pool;
			}
		}
		var lRes = this.createDefaultResult();
		if( this.isLoggedIn() ) {
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "getClients",
				mode: lMode,
				pool: lPool
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	getNonAuthClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.NON_AUTHENTICATED;
		return this.getClients( aOptions );
	},

	getAuthClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.AUTHENTICATED;
		return this.getClients( aOptions );
	},

	getAllClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.ALL_CLIENTS;
		return this.getClients( aOptions );
	},

	ping: function( aOptions ) {
		var lEcho = false;
		if( aOptions ) {
			if( aOptions.echo ) {
				lEcho = true;
			}
		}
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "ping",
				echo: lEcho
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	startKeepAlive: function( aOptions ) {
		// if we have a keep alive running already stop it
		if( this.hKeepAlive ) {
			stopKeepAlive();
		}
		// return if not (yet) connected
		if( !this.isConnected() ) {
			return;
		}
		var lInterval = 10000;
		var lEcho = true;
		var lImmediate = true;
		if( aOptions ) {
			if( aOptions.interval != undefined ) {
				lInterval = aOptions.interval;
			}
			if( aOptions.echo != undefined ) {
				lEcho = aOptions.echo;
			}
			if( aOptions.immediate != undefined ) {
				lImmediate = aOptions.immediate;
			}
		}
		if( lImmediate ) {
			// send first ping immediately, if requested
			this.ping({
				echo: lEcho
			});
		}
		// and then initiate interval...
		var lThis = this;
		this.hKeepAlive = setInterval(
			function() {
				if( lThis.isConnected() ) {
					lThis.ping({
						echo: lEcho
					});
				} else {
					lThis.stopKeepAlive();
				}
			},
			lInterval
			);
	},

	stopKeepAlive: function() {
		if( this.hKeepAlive ) {
			clearInterval( this.hKeepAlive );
			this.hKeepAlive = null;
		}
	}
}

// add the JWebSocket SystemClient PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SystemClientPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket Client Streaming Plug-In
//	---------------------------------------------------------------------------

jws.StreamingPlugIn = {

	// namespace for client streaming plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.streaming",

	registerStream: function( aStream ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.StreamingPlugIn.NS,
				type: "register",
				stream: aStream
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	unregisterStream: function( aStream ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.StreamingPlugIn.NS,
				type: "unregister",
				stream: aStream
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	}

}

// add the JWebSocket SystemClient PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.StreamingPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket RPC Client Plug-In
//	---------------------------------------------------------------------------

jws.RPCClientPlugIn = {

	// namespace for RPC plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.rpc",

	rpc: function( aClass, aMthd, aArgs, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.RPCClientPlugIn.NS,
				type: "rpc",
				classname: aClass,
				method: aMthd,
				args: aArgs
			},
			aOptions
			);
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	}

}

// add the JWebSocket RPC PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.RPCClientPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket JSON client
//	todo: consider potential security issues with 'eval'
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketJSONClient", jws.jWebSocketTokenClient, {

	// this converts a token to a JSON stream
	tokenToStream: function( aToken ) {
		var lJSON = "{utid:" + jws.CUR_TOKEN_ID;
		if( this.fSessionId ) {
			lJSON += ",usid:\"" + this.fSessionId + "\"";
		}
		for( var lKey in aToken ) {
			var lVal = aToken[ lKey ];
			if( typeof lVal == "string" ) {
				lJSON += "," + lKey + ":\"" + lVal + "\"";
			} else {
				lJSON += "," + lKey + ":" + lVal;
			}
		}
		lJSON += "}\n";
		return lJSON;
	},

	streamToToken: function( aStream ) {
		// parsing a JSON object in JavaScript couldn't be simpler...
		// but using 'eval', so be aware of security issues!
		var lObj = null;
		eval( "lObj=" + aStream );
		return lObj;
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket CSV client
//	todo: implement jWebSocket JavaScript CSV client
//	jWebSocket target release 1.1
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketCSVClient", jws.jWebSocketTokenClient, {

	// this converts a token to a CSV stream
	// todo: implement escaping of command separators and equal signs
	tokenToStream: function( aToken ) {
		var lCSV = "utid=" + jws.CUR_TOKEN_ID;
		if( this.fSessionId ) {
			lCSV += ",usid=\"" + this.fSessionId + "\"";
		}
		for( var lKey in aToken ) {
			var lVal = aToken[ lKey ];
			if( lVal === null || lVal === undefined ) {
				lCSV += "," + lKey + "=";
			} else if( typeof lVal == "string" ) {
				// escape commata and quotes
				lVal = lVal.replace( /[,]/g, "\\x2C" );
				lVal = lVal.replace( /["]/g, "\\x22" );
				lCSV += "," + lKey + "=\"" + lVal + "\"";
			} else {
				lCSV += "," + lKey + "=" + lVal;
			}
		}
		lCSV += "\n";
		return lCSV;
	},

	// this converts a CSV stream into a token
	// todo: implement escaping of command separators and equal signs
	streamToToken: function( aStream ) {
		var lToken = {};
		var lItems = aStream.split(",");
		for( var lIdx = 0, lCnt = lItems.length; lIdx < lCnt; lIdx++ ) {
			var lKeyVal = lItems[ lIdx ].split( "=" );
			if( lKeyVal.length == 2 ) {
				var lKey = lKeyVal[ 0 ];
				var lVal = lKeyVal[ 1 ];
				if( lVal.length >= 2 
					&& lVal.charAt(0)=="\""
					&& lVal.charAt(lVal.length-1)=="\"" ) {
					// unescape commata and quotes
					lVal = lVal.replace( /\\x2C/g, "\x2C" );
					lVal = lVal.replace( /\\x22/g, "\x22" );
					// strip string quotes
					lVal = lVal.substr( 1, lVal.length - 2 );
				}
				lToken[ lKey ] = lVal;
			}
		}
		return lToken;
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket XML client
//	todo: implement jWebSocket JavaScript XML client
//	jWebSocket target release 1.1
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketXMLClient", jws.jWebSocketTokenClient, {

	// this converts a token to a XML stream
	tokenToStream: function( aToken ) {
		return "not yet implemented";
	},

	// this converts a XML stream into a token
	streamToToken: function( aStream ) {
		return {};
	}

});


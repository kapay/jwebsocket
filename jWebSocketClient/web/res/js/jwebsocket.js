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
var jws = {

	NS_BASE: "org.jWebSocket",

	// some namespace global constants
	CUR_TOKEN_ID: 0,
	JWS_SERVER_URL: "ws://" + ( self.location.hostname ? self.location.hostname : "localhost" ) + ":8787",

	$: function( aId ) {
		return document.getElementById( aId );
	},

	browserSupportsWebSockets: function() {
		return( window.WebSocket != null );
	}
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
	// publish the new class in the given name space
	lNS[ aClassname ] = lConstructor;

	// move all fields from spec to new class' prototype
	var lField;
	for( lField in aFields ) {
		lConstructor.prototype[ lField ] = aFields[ lField ];
	}
	if( aAncestor != null ) {
		// every class maintains an array of its direct descendants
		if( !aAncestor.descendants ) {
			aAncestor.descendants = [];
		}
		aAncestor.descendants.push( lConstructor );
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

	// if the class has no plug-ins yet initialize array
	if( !aClass.fPlugIns ) {
		aClass.fPlugIns = [];
	}
	// add the plug-in to the class
	aClass.fPlugIns.push( aPlugIn );
	// clone all methods of the plug-in to the class
	for( var lField in aPlugIn ) {
		// don't overwrite existing methods of class with plug.in methods
		if( !aClass.prototype[ lField ] ) {
			aClass.prototype[ lField ] = aPlugIn[ lField ];
		}
	}
	// if the class already has descendants recursively
	// clone the plug-in methods to these as well.
	// checkDescendants( aClass );
	if( aClass.descendants ) {
		for( var lIdx = 0, lCnt = aClass.descendants.length; lIdx < lCnt; lIdx ++ ) {
			jws.oop.addPlugIn( aClass.descendants[ lIdx ], aPlugIn );
		}
	}
}


//	---------------------------------------------------------------------------
//  jWebSocket - Base Client
//  This class does not handle exceptions or error, it throws exceptions,
//  which are handled by the descendant classes.
//	---------------------------------------------------------------------------

// declaration for the jws.jWebSocketBaseClient class
jws.oop.declareClass( "jws", "jWebSocketBaseClient", null, {

	processOpened: function( aEvent ) {
		// can to be overwritten in descendant classes
		// to easily handle open event in descendants
	},

	processPacket: function( aEvent ) {
		// can to be overwritten in descendant classes
		// to easily handle message event in descendants
	},

	processClosed: function( aEvent ) {
		// can to be overwritten in descendant classes
		// to easily handle open event in descendants
	},

	open: function( aURL, aOptions ) {
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
					lValue = lThis.processOpened( aEvent );
					// give application change to handle event
					if( aOptions.OnOpen ) {
						aOptions.OnOpen( aEvent, lValue, lThis );
					}
				};

				this.fConn.onmessage = function( aEvent ) {
					lValue = lThis.processPacket( aEvent );
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
					lValue = lThis.processClosed( aEvent );
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

	connect: function( aURL, aOptions ) {
		return this.open(aURL, aOptions );
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
			this.fConn.onopen = null;
			this.fConn.onmessage = null;
			this.fConn.onclose = null;
			this.fConn.close();
			this.processClosed();
		}
		this.fConn = null;
	},

	close: function( aOptions ) {
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
	},

	disconnect: function( aOptions ) {
		return this.close( aOptions );
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

	getId: function() {
		return this.fClientId;
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

	checkConnected: function() {
		jws.CUR_TOKEN_ID++;
		var lRes = this.createDefaultResult();
		if( !this.isConnected() ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	checkLoggedIn: function() {
		jws.CUR_TOKEN_ID++;
		var lRes = this.createDefaultResult();
		if( !this.isLoggedIn() ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
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

	notifyPlugInsOpened: function() {
		var lToken = {
			sourceId: this.fClientId
		};
		// notify all plug-ins about sconnect event
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processOpened ) {
					lPlugIn.processOpened.call( this, lToken );
				}
			}
		}
	},

	notifyPlugInsClosed: function() {
		var lToken = {
			sourceId: this.fClientId
		};
		// notify all plug-ins about disconnect event
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processClosed ) {
					lPlugIn.processClosed.call( this, lToken );
				}
			}
		}
		// in case of a server side close event...
		this.fConn = null;
		// reset the session...
		this.fSessionId = null;
		// and the username as well
		this.fUsername = null;
	},

	processPacket: function( aEvent ) {
		// parse incoming token...
		var lToken = this.streamToToken( aEvent.data );
		// and process it...
		this.processToken( lToken );
		return lToken;
	},

	processToken: function( aToken ) {
		// check welcome and goodBye tokens to manage the session
		if( aToken.type == "welcome" && aToken.usid ) {
			this.fSessionId = aToken.usid;
			this.fClientId = aToken.sourceId;
			this.notifyPlugInsOpened();
		} else if( aToken.type == "goodBye" ) {
			this.fSessionId = null;
		} else if( aToken.type == "close" ) {
			// if the server closes the connection close immediately too.
			this.close({
				timeout: 0
			});
			// check if we got a response from a previous request
		} else if( aToken.type == "response" ) {
			// check login and logout manage the username
			if( aToken.reqType == "login" ) {
				this.fUsername = aToken.username;
			}
			if( aToken.reqType == "logout" ) {
				this.fUsername = null;
			}
			// check if some requests need to be answered
			this.checkCallbacks( aToken );
		} else if( aToken.type == "event" ) {
			// check login and logout manage the username
			if( aToken.name == "connect" ) {
				this.processConnected( aToken );
			}
			if( aToken.name == "disconnect" ) {
				this.processDisconnected( aToken );
			}
		}
		// notify all plug-ins that a token has to be processed
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processToken ) {
					lPlugIn.processToken.call( this, aToken );
				}
			}
		}
	},

	processClosed: function( aEvent ) {
		this.notifyPlugInsClosed();
		this.fClientId = null;
	},

	processConnected: function( aToken ) {
		// notify all plug-ins that a new client connected
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processConnected ) {
					lPlugIn.processConnected.call( this, aToken );
				}
			}
		}
	},

	processDisconnected: function( aToken ) {
		// notify all plug-ins that a client disconnected
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processDisconnected ) {
					lPlugIn.processDisconnected.call( this, aToken );
				}
			}
		}
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

		console.log("sending" + lStream + "...");

		this.sendStream( lStream );
	},

	sendText: function( aReceiver, aText ) {
		var lRes = this.checkLoggedIn();
		if( lRes.code == 0 ) {
			this.sendToken({
				type: "send",
				targetId: aReceiver,
				sourceId: this.fClientId,
				sender: this.fUsername,
				data: aText
			});
		}
		return lRes;
	},

	broadcastText: function( aPool, aText, aOptions ) {
		var lRes = this.checkLoggedIn();
		var lSenderIncluded = false;
		var lResponseRequested = true;
		if( aOptions ) {
			if( aOptions.senderIncluded ) {
				lSenderIncluded = aOptions.senderIncluded;
			}
			if( aOptions.responseRequested ) {
				lResponseRequested = aOptions.responseRequested;
			}
		}
		if( lRes.code == 0 ) {
			this.sendToken({
				type: "broadcast",
				sourceId: this.fClientId,
				sender: this.fUsername,
				pool: aPool,
				data: aText,
				senderIncluded: lSenderIncluded,
				responseRequested: lResponseRequested
			},
			aOptions
		);
		}
		return lRes;
	},

	echo: function( aData ) {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			this.sendToken({
				type: "echo",
				data: aData
			});
		}
		return lRes;
	},

	open: function( aURL, aOptions ) {
		var lRes = this.createDefaultResult();
		try {
			// call inherited connect, catching potential exception
			arguments.callee.inherited.call( this, aURL, aOptions );
		} catch( ex ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.ex";
			lRes.args = [ ex.message ];
			lRes.msg = "Exception on open: " + ex.message;
		}
		return lRes;
	},

	// deprecated, kept for upward compatibility
	connect: function( aURL, aOptions ) {
		return this.open( aURL, aOptions );
	},

	close: function( aOptions ) {
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
			lRes.msg = "Exception on close: " + ex.message;
		}
		return lRes;
	},

	// deprecated, kept for upward compatibility
	disconnect: function( aOptions ) {
		return this.close( aOptions );
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
			},
			aOptions
		);
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

// the RRPCServer server provides the methods which are granted to be called
// from the "outside".
jws.RRPCServer = {

	demo: function( aArgs ) {
		return(
		confirm(
		"aArgs received: '" + aArgs + "'\n" +
			"'true' or 'false' will be returned to requester."
	)
	);
	}

};


jws.RPCClientPlugIn = {

	// namespace for RPC plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.rpc",

	// granted rrpc's
	grantedProcs: [
		"jws.RRPCServer.demo"
	],

	processToken: function( aToken ) {
		// console.log( "jws.RPCClientPlugIn: Processing token " + aToken.ns + "/" + aToken.type + "..." );
		if( aToken.ns == jws.RPCClientPlugIn.NS ) {
			if( aToken.type == "rrpc" ) {
				this.onRRPC( aToken );
			}
		}
	},

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
	},

	// calls a remote procedure an another client
	rrpc: function( aTarget, aClass, aMthd, aArgs, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.RPCClientPlugIn.NS,
				type: "rrpc",
				targetId: aTarget,
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
	},

	// handles a remote procedure call from another client
	onRRPC: function( aToken ) {
		var lClassname = aToken.classname;
		var lMethod = aToken.method;
		var lArgs = aToken.args;
		var lPath = lClassname + "." + lMethod;
		// check if the call is granted on this client
		if( jws.RPCClientPlugIn.grantedProcs.indexOf( lPath ) >= 0 ) {
			var lEvalStr = lPath + "(" + lArgs + ")";
			// console.log( "Reverse RPC request '" + lPath + "(" + lArgs + ")' granted, running '" + lEvalStr + "'...");
			var lRes;
			eval( "lRes=" + lEvalStr );
			// send result back to requester

			this.sendToken({
				// ns: jws.SystemPlugIn.NS,
				type: "send",
				targetId: aToken.sourceId,
				result: lRes,
				reqType: "rrpc",
				code: 0
			},
			null // aOptions
		);
		} else {
			// console.log( "Reverse RPC request '" + lPath + "(" + lArgs + ")' not granted!" );
		}
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
//	todo: PRELIMINARY! Implement jWebSocket JavaScript XML client
//	Targetted for jWebSocket release 1.1
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketXMLClient", jws.jWebSocketTokenClient, {

	// this converts a token to a XML stream
	tokenToStream: function( aToken ) {

		function obj2xml( aKey, aValue ) {
			var lXML = "";
			// do we have an array? Caution! Keep this condition on
			// the top because array is also an object!
			if ( aValue instanceof Array ) {
				lXML += "<" + aKey + " type=\"" + "array" + "\">";
				for( var lIdx = 0, lCnt = aValue.length; lIdx < lCnt; lIdx++ ) {
					lXML += obj2xml( "item", aValue[ lIdx ] );
				}
				lXML += "</" + aKey + ">"
			}
			// or do we have an object?
			else if ( typeof aValue  == "object" ) {
				lXML += "<" + aKey + " type=\"" + "object" + "\">";
				for(var lField in aValue ) {
					lXML += obj2xml( lField, aValue[ lField ] );
				}
				lXML += "</" + aKey + ">"
			}
			// or do we have a plain field?
			else {
				lXML +=
					"<" + aKey + " type=\"" + typeof aValue + "\">" +
					aValue.toString() +
					"</" + aKey + ">";
			}
			return lXML;
		}

		var lEncoding = "windows-1252";
		var lResXML =
			"<?xml version=\"1.0\" encoding=\"" + lEncoding + "\"?>" +
			"<token>";
		for( var lField in aToken ) {
			lResXML += obj2xml( lField, aToken[ lField ] );
		}
		lResXML += "</token>";
		return lResXML;
	},

	// this converts a XML stream into a token
	streamToToken: function( aStream ) {
		// first convert the stream into an XML document 
		// by using the embedded XML parser.
		// We do not really want to parse the XML in Javascript!
		// Using the built-in parser should be more performant.
		var lDoc = null;
/* Once we have an applet for IEx ;-)
		if( window.ActiveXObject ) {
			//:i:de:Internet Explorer
			lDoc = new ActiveXObject( "Microsoft.XMLDOM" );
			lDoc.async = "false";
			lDoc.loadXML( aStream );
		} else {
*/
			// For all other Browsers
			try{
				var lParser = new DOMParser();
				lDoc = lParser.parseFromString( aStream, "text/xml" );
			} catch( ex ) {
				// ignore exception here, lDoc will keep being null
			}
/*
		}
*/

		function node2obj( aNode, aObj ) {
			var lNode = aNode.firstChild;
			while( lNode != null ) {
				// 1 = element node
				if( lNode.nodeType == 1 ) {
					var lType = lNode.getAttribute( "type" );
					var lKey = lNode.nodeName;
					if( lType ) {
						var lValue = lNode.firstChild;
						// 3 = text node
						if( lValue && lValue.nodeType == 3 ) {
							lValue = lValue.nodeValue;
							if( lValue ) {
								if( lType == "string" ) {
								} else if( lType == "number" ) {
								} else if( lType == "boolean" ) {
								} else if( lType == "date" ) {
								} else {
									lValue = undefined;
								}
								if( lValue ) {
									if ( aObj instanceof Array ) {
										aObj.push( lValue );
									} else {
										aObj[ lKey ] = lValue;
									}
								}
							}
						} else
						// 1 = element node
						if( lValue && lValue.nodeType == 1 ) {
							if( lType == "array" ) {
								aObj[ lKey ] = [];
								node2obj( lNode, aObj[ lKey ] );
							} else if( lType == "object" ) {
								aObj[ lKey ] = {};
								node2obj( lNode, aObj[ lKey ] );
							}
						}
					}
				}
				lNode = lNode.nextSibling;
			}
		}

		var lToken = {};
		if( lDoc ) {
			node2obj( lDoc.firstChild, lToken );
		}
		return lToken;
	}

});

/*

(function() {
	var lObj = {
		aNumber: 1,
		aString: "test1",
		aBoolean: true,
		aArray: [ 2, "test2", false ],
		aObject: {
			bNumber: 3,
			bString: "test3",
			bBoolean: true,
			bArray: [ 3, "test3", true ]
		}
	};
	var lStream = 
		'<?xml version="1.0" encoding="windows-1252"?>' +
		'<token>' +
			'<aNumber type="number">1</aNumber>' +
			'<aString type="string">test1</aString>' +
			'<aBoolean type="boolean">true</aBoolean>' +
			'<aArray type="array">' +
				'<item type="number">2</item>'+
				'<item type="string">test2</item>' +
				'<item type="boolean">false</item>' +
			'</aArray>' +
			'<aObject type="object">' +
				'<bNumber type="number">3</bNumber>'+
				'<bString type="string">test3</bString>' +
				'<bBoolean type="boolean">true</bBoolean>' +
				'<bArray type="array">'+
					'<item type="number">3</item>' +
					'<item type="string">test3</item>' +
					'<item type="boolean">true</item>' +
				'</bArray>' +
			'</aObject>' +
		'</token>';

	var lXMLClient = new jws.jWebSocketXMLClient();
//	var lStream = lXMLClient.tokenToStream( lObj );
	var lToken = lXMLClient.streamToToken( lStream );
	console.log( lStream );
})();

*/
//	---------------------------------------------------------------------------
//	jWebSocket API PlugIn (uses jWebSocket Client and Server)
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
//  jWebSocket API Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.APIPlugInClass
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.APIPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to register and unregister at certain _
//:d:en:stream sn the server.
jws.APIPlugInClass = {

	//:const:*:NS:String:org.jwebsocket.plugins.API (jws.NS_BASE + ".plugins.api")
	//:d:en:Namespace for the [tt]APIPlugIn[/tt] class.
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.api",
	//:const:*:ID:String:APIPlugIn
	//:d:en:Id for the [tt]APIPlugIn[/tt] class.
	ID: "api",

	processToken: function( aToken ) {
		console.log( "Processing incoming token: " + JSON.stringify( aToken ) );
	},

	hasPlugIn: function( aId, aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.has.plugin",
			plugin_id: aId
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	plugInAPI: function( aId, aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.export.plugin.api",
			plugin_id: aId
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	supportToken: function( aId, aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.support.token",
			token_type: aId
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	serverAPI: function( aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.export.api"
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	plugInsIds: function( aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type:  "server.export.plugin.ids"
		}
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	parseParams: function( aParamsDef ) {
		var params = {};
		var end = aParamsDef.length;

		var value;
		for ( var i = 0; i < end; i++ ){
			if( aParamsDef[i].testValue ) {
				value = aParamsDef[i].testValue;
			} else if( aParamsDef[i].optional ){
				// do nothing!
			} else {
				debugger;
				//:todo:en:what is t here?
				switch( aParamsDef[i].type ) {
					case "string":
						value = jws.t.values.getString();
						break;
					case "integer":
						value = jws.t.values.getInteger();
						break;
					case "double":
						value = jws.t.values.getDouble();
						break;
					case "boolean":
						value = jws.t.values.getBoolean();
						break;
					case "number":
						value = jws.t.values.getNumber();
						break;
					default:
						break;
				}
			}

			// setting the argument value
			params[ aParamsDef[i].name ] = value;
		}

		return params;
	},

	describePlugIn: function( aPlugIn ) {
		var lCount = aPlugIn.supportedTokens.length;
		for (var lIdx = 0; lIdx < lCount; lIdx++ ) {

			var lToken = aPlugIn.supportedTokens[ lIdx ];
			console.log( lToken.comment );

			if ( "none" == lToken.requestType ) {
				console.log ( "'" + lToken.type + "' test omitted!" );
				continue;
			}

			var lCallToken = this.parseParams( lToken.inArguments );
			//:todo:en:need to support namespace!
			// lCallToken.ns = lToken.ns;
			lCallToken.type = lToken.type;
			console.log( JSON.stringify( lCallToken ) );

			this.conn.sendToken( lCallToken, {

			});
		}
	}

};

jws.APIPlugIn = function() {
	// here you can allocate instance variables!
	}

jws.APIPlugIn.prototype = jws.APIPlugInClass;

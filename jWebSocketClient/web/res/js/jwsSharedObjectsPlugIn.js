//	---------------------------------------------------------------------------
//	jWebSocket Shared Objects PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
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


//	---------------------------------------------------------------------------
//  jWebSocket Shared Objects Plug-In
//	---------------------------------------------------------------------------

jws.SharedObjectsPlugIn = {

	// namespace for shared objects plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.sharedObjs",
	// if data types are changed update server plug-in accordingly!
	DATA_TYPES: [ "number", "string", "boolean", "object", "set", "list", "map", "table" ],

	fObjects: {},

	processToken: function( aToken ) {
		console.log( "jws.SharedObjectsPlugIn: Processing token " + aToken.ns + "/" + aToken.type + "..." );
		if( aToken.ns == jws.SharedObjectsPlugIn.NS ) {
			if( aToken.type == "create" ) {
				// create a new object on the client
			} else if( aToken.type == "destroy" ) {
				// destroy an existing object on the client
			} else if( aToken.type == "update" ) {
				// update an existing object on the client
			} else if( aToken.type == "init" ) {
				// init all shared object on the client
			}
		}
	},

	createSharedObject: function( aId, aDataType, aValue, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SharedObjectsPlugIn.NS,
				type: "create",
				id: aId,
				datatype: aDataType,
				value: aValue
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

	destroySharedObject: function( aId, aDataType, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SharedObjectsPlugIn.NS,
				type: "destroy",
				id: aId,
				datatype: aDataType
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

	getSharedObject: function( aId, aDataType, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SharedObjectsPlugIn.NS,
				type: "get",
				id: aId,
				datatype: aDataType
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

	updateSharedObject: function( aId, aDataType, aValue, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SharedObjectsPlugIn.NS,
				type: "put",
				id: aId,
				datatype: aDataType,
				value: aValue
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

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SharedObjectsPlugIn );

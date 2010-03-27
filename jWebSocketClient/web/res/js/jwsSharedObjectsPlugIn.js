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


//	---------------------------------------------------------------------------
//  jWebSocket Shared Objects Plug-In
//	---------------------------------------------------------------------------

jws.SharedObjectsPlugIn = {

	// namespace for RPC plugin
	// if namespace changed update server plug-in accordingly!
	NS_OBJ: jws.NS_BASE + ".plugins.sharedObj",
	NS_LISTS: jws.NS_BASE + ".plugins.sharedLists",
	NS_SETS: jws.NS_BASE + ".plugins.sharedSets",
	NS_MAPS: jws.NS_BASE + ".plugins.sharedMaps",

	fLists: {},
	fSets: {},
	fMaps: {},

	processToken: function( aToken ) {
		console.log( "jws.SharedObjectsPlugIn: Processing token " + aToken.ns + "/" + aToken.type + "..." );
		if( aToken.ns == jws.SharedObjectsPlugIn.NS_LISTS ) {
			if( aToken.type == "create" ) {

			} else if( aToken.type == "destroy" ) {

			} else if( aToken.type == "add" ) {
				
			} else if( aToken.type == "remove" ) {

			} else if( aToken.type == "update" ) {

			}
		}
	},

	lists: {
		create: function( aId ) {
			var lRes = this.createDefaultResult();
			if( this.isConnected() ) {
				this.sendToken({
					ns: NS_LISTS,
					type: "create",
					id: aId
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

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SharedObjectsPlugIn );

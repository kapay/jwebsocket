//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the JMS Plug-in
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

jws.tests.JMS = {

	NS: "jws.tests.JMS", 

	// this spec tests the login function of the system plug-in
	testLoginValidCredentials: function() {
		var lSpec = this.NS + ": Logging in with valid credentials";
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();
			var lResponse = {};

			// open a separate control connection
			lConn.logon( jws.getDefaultServerURL(), "guest", "guest", {
				OnToken: function ( aToken ) {
					if( "org.jwebsocket.plugins.system" == aToken.ns
						&& "login" == aToken.reqType) {
						lResponse = aToken;
					}
				}
			});

			waitsFor(
				function() {
					return( lResponse.code != undefined );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				lConn.close();
			});
		});
	},


	// this spec tests the login function of the system plug-in
	testLoginInvalidCredentials: function() {
		var lSpec = this.NS + ": Logging in with invalid credentials";
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();
			var lResponse = {};

			// open a separate control connection
			lConn.logon( jws.getDefaultServerURL(), "InVaLiD", "iNvAlId", {
				OnToken: function ( aToken ) {
					if( "org.jwebsocket.plugins.system" == aToken.ns
						&& "login" == aToken.reqType) {
						lResponse = aToken;
					}
				}
			});

			waitsFor(
				function() {
					return( lResponse.code != undefined );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( -1 );
				lConn.close();
			});
		});
	},


	// this spec tests the send method of the system plug-in by sending
	// this spec requires an established connection
	testSendLoopBack: function() {
		var lSpec = this.NS + ": Send and Loopback";
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lResponse = {};
			var lMsg = "This is my message";

			// open a separate control connection
			var lToken = {
				ns: jws.NS_SYSTEM,
				type: "send",
				targetId: jws.Tests.getAdminConn().getId(),
				sourceId: jws.Tests.getAdminConn().getId(),
				sender: jws.Tests.getAdminConn().getUsername(),
				data: lMsg
			};

			var lListener = function( aToken ) {
				if( "org.jwebsocket.plugins.system" == aToken.ns
					&& "send" == aToken.type) {
					lResponse = aToken;
				}
			};

			jws.Tests.getAdminConn().addListener( lListener );
			jws.Tests.getAdminConn().sendToken( lToken );

			waitsFor(
				function() {
					return( lResponse.data == lMsg );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.data ).toEqual( lMsg );
				jws.Tests.getAdminConn().removeListener( lListener );
			});

		});
	},

	runSpecs: function() {
		jws.tests.JMS.testLoginValidCredentials();
		jws.tests.JMS.testLoginInvalidCredentials();
		jws.tests.JMS.testSendLoopBack();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}


//	---------------------------------------------------------------------------
//	jWebSocket Testsuites
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

var NS_BENCHMARK = jws.NS_BASE  + ".plugins.benchmark";

var MAX_CONNECTIONS = 50;
var MAX_BROADCASTS = 100;
var OPEN_CONNECTIONS_TIMEOUT = 30000;
var BROADCAST_TIMEOUT = 30000;
var CLOSE_CONNECTIONS_TIMEOUT = 30000;
var BROADCAST_MESSAGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghihjklmnopqrstuvwxyz 0123456789";

var ROOT_USER = "root";

var lConnectionsOpened = 0;
var lConnections = [];
var lPacketsReceived = 0;

// this global connection is shared between multiple tests
var lSharedRootConn = null;

// this spec opens all connections
function testOpenConnections() {
	var lSpec = "Opening " + MAX_CONNECTIONS + " connections";
	it( lSpec, function () {

		// reset all watches
		jws.StopWatchPlugIn.resetWatches();

		// start stop watch for this spec
		jws.StopWatchPlugIn.startWatch( "openConn", lSpec );

		for( var lIdx = 0; lIdx < MAX_CONNECTIONS; lIdx++ ) {

			lConnections[ lIdx ] = new jws.jWebSocketJSONClient();
			lConnections[ lIdx ].open( jws.getDefaultServerURL(), {

				OnOpen: function () {
					lConnectionsOpened++;
				},

				OnClose: function () {
					lConnectionsOpened--;
				},

				OnToken: function( aToken ) {
					if ( "s2c_performance" == aToken.type
							&& NS_BENCHMARK == aToken.ns ) {
						lPacketsReceived++;
					}
				}

			});
		}

		// wait for expected connections being opened
		waitsFor(
			function() {
				return lConnectionsOpened == MAX_CONNECTIONS;
			},
			"opening connection...",
			OPEN_CONNECTIONS_TIMEOUT
		);

		runs(
			function () {
				expect( lConnectionsOpened ).toEqual( MAX_CONNECTIONS );
				// stop watch for this spec
				jws.StopWatchPlugIn.stopWatch( "openConn" );
			}
		);

	});
}

// this spec tests the login function of the system plug-in
function testLoginValidCredentials() {
	var lSpec = "Logging in with valid credentials";
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
}


// this spec tests the login function of the system plug-in
function testLoginInvalidCredentials() {
	var lSpec = "Logging in with invalid credentials";
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
}

// this spec tries to open a connection to be shared across multiple tests
function testOpenSharedAdminConn() {
	var lSpec = "Opening shared connection with administrator role";
	it( lSpec, function () {

		// we need to "control" the server to broadcast to all connections here
		lSharedRootConn = new jws.jWebSocketJSONClient();
		var lResponse = {};

		// open a separate control connection
		lSharedRootConn.logon( jws.getDefaultServerURL(), ROOT_USER, "root", {
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
			expect( lResponse.username ).toEqual( ROOT_USER );
		});
	});
}

// this spec tries to open a connection to be shared across multiple tests
function testCloseSharedAdminConn() {
	var lSpec = "Closing shared connection with administrator role";
	it( lSpec, function () {

		// open a separate control connection
		lSharedRootConn.close({
			timeout: 3000
		});

		waitsFor(
			function() {
				return( !lSharedRootConn.isOpened() );
			},
			lSpec,
			3000
		);

		runs( function() {
			expect( lSharedRootConn.isOpened() ).toEqual( false );
		});
	});
}


// this spec tests the send method of the system plug-in by sending
// this spec requires an established connection
function testSendLoopBack() {
	var lSpec = "Send and Loopback";
	it( lSpec, function () {

		// we need to "control" the server to broadcast to all connections here
		var lResponse = {};
		var lMsg = "This is my message";

		// open a separate control connection
		var lToken = {
			ns: jws.NS_SYSTEM,
			type: "send",
			targetId: lSharedRootConn.getId(),
			sourceId: lSharedRootConn.getId(),
			sender: lSharedRootConn.getUsername(),
			data: lMsg
		};

		var lListener = function( aToken ) {
			if( "org.jwebsocket.plugins.system" == aToken.ns
				&& "send" == aToken.type) {
				lResponse = aToken;
			}
		};

		lSharedRootConn.addListener( lListener );
		lSharedRootConn.sendToken( lToken );

		waitsFor(
			function() {
				return( lResponse.data == lMsg );
			},
			lSpec,
			3000
		);

		runs( function() {
			expect( lResponse.data ).toEqual( lMsg );
			lSharedRootConn.removeListener( lListener );
		});

	});
}

// this spec closes all connections
function testCloseConnections() {
	var lSpec = "Closing " + MAX_CONNECTIONS + " connections";
	it( lSpec, function () {

		// start stop watch for this spec
		jws.StopWatchPlugIn.startWatch( "closeConn", lSpec );

		for( var lIdx = 0; lIdx < MAX_CONNECTIONS; lIdx++ ) {
			lConnections[ lIdx ].close({
				timeout: 3000,
				// fireClose: true,
				// noGoodBye: true,
				noLogoutBroadcast: true,
				noDisconnectBroadcast: true
			});
		}

		// wait for expected connections being opened
		waitsFor(
			function() {
				return lConnectionsOpened == 0;
			},
			"closing connections...",
			CLOSE_CONNECTIONS_TIMEOUT
		);

		runs(
			function () {
				expect( lConnectionsOpened ).toEqual( 0 );

				// stop watch for this spec
				jws.StopWatchPlugIn.stopWatch( "closeConn" );

				// print all watches to the console
				jws.StopWatchPlugIn.printWatches();

				// reset all watches
				jws.StopWatchPlugIn.resetWatches();
			}
		);
	});
}

function testBenchmark() {
	var lSpec = "Broadcasting " + MAX_BROADCASTS + " packets to " + MAX_CONNECTIONS + " connections";
	it( lSpec, function () {

		// start stop watch for this spec
		jws.StopWatchPlugIn.startWatch( "broadcast", lSpec );

		// we need to "control" the server to broadcast to all connections here
		var lConn = new jws.jWebSocketJSONClient();

		// open a separate control connection
		lConn.open(jws.getDefaultServerURL(), {

			OnOpen: function () {
				lPacketsReceived = 0;
				var lToken = {
					ns: NS_BENCHMARK,
					type: "s2c_performance",
					count: MAX_BROADCASTS,
					message: BROADCAST_MESSAGE
				};
				lConn.sendToken( lToken );
			}
		});

		waitsFor(
			function() {
				return lPacketsReceived == MAX_CONNECTIONS * MAX_BROADCASTS;
			},
			"broadcasting test packages...",
			BROADCAST_TIMEOUT
		);
			
		runs( function() {
			expect( lPacketsReceived ).toEqual( MAX_CONNECTIONS * MAX_BROADCASTS );

			// stop watch for this spec
			jws.StopWatchPlugIn.stopWatch( "broadcast" );
		});
	});
}

var lSpecs = [];

function testGetAPIDefaults() {
	var lSpec = "Running default API spec";

	it( lSpec, function () {

		var lDone = 0;

		// start stop watch for this spec
		jws.StopWatchPlugIn.startWatch( "defAPIspec", lSpec );

		// we need to "control" the server to broadcast to all connections here
		var lConn = new jws.jWebSocketJSONClient();
		lDone = 0;
		
		// open a separate control connection
		lConn.open(jws.getDefaultServerURL(), {

			OnOpen: function () {
				var lAPIPlugIn = new jws.APIPlugIn();
				lConn.addPlugIn( lAPIPlugIn );
				// request the API of the benchmark plug-in
				lAPIPlugIn.getPlugInAPI(
					"jws.benchmark", {
					// if API received successfully run the tests...
					OnResponse: function( aServerPlugIn ) {
						lSpecs = lAPIPlugIn.createSpecFromAPI( lConn, aServerPlugIn );
						lDone = 1;
					},
					OnTimeout: function() {
						lConn.close();
						lDone = 1;
					}
				});
			}
		});

		waitsFor(
			function() {
				return lDone == 1;
			},
			"Running against API...",
			BROADCAST_TIMEOUT
		);

		runs( function() {
			expect( lDone ).toEqual( 1 );

			// stop watch for this spec
			jws.StopWatchPlugIn.stopWatch( "defAPIspec" );
		});
	});
}

function testRunAPIDefaults() {
	it( "Running default tests", function() {
		eval( 
			"  for( var i = 0; i < lSpecs.length; i++ ) { "
			+ "  lSpecs[ i ]();"
			+ "}"
		);
	});
}

// ---------------------------------------------------------------------------
// the various jWebSocket test suites
// ---------------------------------------------------------------------------

// this is a suite
function runOpenCloseSuite () {

	describe( "Open/Close Test Suite", function () {
		testOpenConnections();
		testCloseConnections();
	});
}

function runBenchmarkSuite() {

	describe( "Benchmark Test Suite", function () {

		// open all connections
		testOpenConnections();

		// run the benchmark
		testBenchmark();

		// close all connections
		testCloseConnections();
	});

}

function runDefaultAPISuite() {

	describe( "Default API test Suite", function () {

		// open all connections
		testOpenConnections();

		// get the default specs from the API
		testGetAPIDefaults();

		// run all the obtained default specs
		testRunAPIDefaults();

		// close all connections
		testCloseConnections();
	});

}

function runManualTestSuite() {

	describe( "jWebSocket Test Suite", function () {

		testLoginValidCredentials();
		testLoginInvalidCredentials();

		testOpenSharedAdminConn();
		testSendLoopBack();
		testCloseSharedAdminConn();
	});

}


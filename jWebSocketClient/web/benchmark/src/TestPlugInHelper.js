//	---------------------------------------------------------------------------
//	jWebSocket Benchmarks
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

var MAX_CONNECTIONS = 10;
var MAX_BROADCASTS = 10;
var OPEN_CONNECTIONS_TIMEOUT = 5000;
var BROADCAST_TIMEOUT = 10000;
var CLOSE_CONNECTIONS_TIMEOUT = 5000;

var lConnectionsOpened = 0;
var lConnections = [];


// this spec opens all connections
function runOpenSpec() {
	it( "Open " + MAX_CONNECTIONS + " connections", function () {

		testing.initTimeMarks[ testing.initTimeMarksIndex++ ] =
			new Date().getTime();

		for( var lIdx = 0; lIdx < MAX_CONNECTIONS; lIdx++ ) {

			lConnections[ lIdx ] = new jws.jWebSocketJSONClient();
			lConnections[ lIdx ].open( jws.JWS_SERVER_URL, {

				OnOpen: function () {
					lConnectionsOpened++;
				},

				OnClose: function () {
					lConnectionsOpened--;
				}

			});

		}

		testing.report[ testing.reportIndexI++ ] = 0;

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
				testing.report[ testing.reportIndexI - 1 ] =
					( new Date().getTime() - testing.initTimeMarks[ testing.initTimeMarksIndex - 1 ] );
			}
		);

	});
}


// this spec closes all connections
function runCloseSpec() {
	it( "Close " + MAX_CONNECTIONS + " connections", function () {

		testing.initTimeMarks[ testing.initTimeMarksIndex++ ] =
			new Date().getTime();

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
				testing.report[ testing.reportIndexI - 1 ] =
					( new Date().getTime() - testing.initTimeMarks[ testing.initTimeMarksIndex - 1 ] );
			}
		);
	});
}

// this is a suite
function runOpenCloseSuite () {
	describe( "Open/Close Test Suite", function () {
		runOpenSpec();
		runCloseSpec();
	});

}

function runBenchmarkSpec() {
	it( "run benchmarks", function () {

		var lConn = new jws.jWebSocketJSONClient();

		lConn.open(jws.getDefaultServerURL(), {

			OnOpen: function () {
				// create and add the API plug-in to the connection
				var lAPIPlugIn = new jws.APIPlugIn();
				lConn.addPlugIn( lAPIPlugIn );
				// request the API of the benchmark plug-in
				lAPIPlugIn.plugInAPI(
					"jws.benchmark",
					function( aPlugIn ) {
						testing.describePlugIn( lConn, aPlugIn );
					}
				);
			}
		});

		waitsFor(
			function() {
				return lConnectionsOpened == 0;
			},
			"closing connections...",
			CLOSE_CONNECTIONS_TIMEOUT
		);
			
		runs( function() {
			expect( true ).toEqual( true );
		});
	});
}

function runAPISuite () {
	describe( "Benchmark Test Suite", function () {
		runOpenSpec();
		runBenchmarkSpec();
		runCloseSpec();
	});
}


function Connection (helper) {

	this.helper = helper;
	
	this.processToken = function ( aToken ) {

		if ("s2c_performance" == aToken.type
			&& NS_BENCHMARK == aToken.ns ) {

			if( aToken .data == this.helper.message ) {
				this.helper.received++;
				console.log(this.helper.received);
			}
			
			if( this.helper.received == MAX_CONNECTIONS * MAX_BROADCASTS ) {
				this.helper.ready = true;
			}
		}
	}
}

testing.helpers.set( NS_BENCHMARK, "s2c_performance", {

	ready		: false,
	received	: 0,
	message		: "",
	startTime	: 0,
	endTime		: 0,

	initialize: function( aToken ) {

		//Initializing test values
		this.message =  aToken.message;
		
		for( var lIdx = 0; lIdx < MAX_CONNECTIONS; lIdx++ ) {
			lConnections[ lIdx ].addPlugIn( new Connection( this ), "benchmark" );
		}
		
		this.startTime = new Date().getTime(); // Time stats goes in the client ;)
	},

	isReady: function() {
		return this.ready;
	},

	validate: function() {
		//Ensure that all the messages has been received
		expect( this.received ).toEqual( MAX_CONNECTIONS * MAX_BROADCASTS );
	}

});

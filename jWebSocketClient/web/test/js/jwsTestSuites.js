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

function runFullTestSuite() {

	describe( "jWebSocket Test Suite", function () {

		// open connections for admin and guest
		jws.Tests.testOpenSharedAdminConn();
		jws.Tests.testOpenSharedGuestConn();
		
		// run test suites for the various plug-ins
		jws.tests.System.runSuite();
		jws.tests.FileSystem.runSuite();
		jws.tests.Logging.runSuite();
		jws.tests.AutomatedAPI.runSuite();
		
		// run RPC tests
		jws.tests.RPC.runSuite();

		// run JDBC tests
		jws.tests.JDBC.runSuite();

		// close connections for admin and guest
		jws.Tests.testCloseSharedAdminConn();
		jws.Tests.testCloseSharedGuestConn();
	});

}

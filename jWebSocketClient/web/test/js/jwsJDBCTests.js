//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the Logging Plug-in
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


jws.tests.JDBC = {

	NS: "jws.tests.jdbc", 
	
	TEST_TABLE: "jwebsocket_automated_test",
	TEST_STRING_1: "This is an automated demo text", 
	TEST_STRING_2: "This is an updated demo text", 

	// this spec tests the jdbc plug-in, creating a temporary table for test purposes
	testCreateTable: function() {
		
		var lSpec = this.NS + ": create table (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().jdbcExecSQL(
				"create table " + jws.tests.JDBC.TEST_TABLE + " (id int, text varchar(80))",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.msg !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
			});

		});
	},
	
	// this spec tests the jdbc plug-in, creating a temporary table for test purposes
	testDropTable: function() {
		
		var lSpec = this.NS + ": drop table (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().jdbcExecSQL(
				"drop table " + jws.tests.JDBC.TEST_TABLE,
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.msg !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
			});

		});
	},
	
	// this spec tests the file save method of the fileSystem plug-in
	testQuerySQL: function() {
		
		var lSpec = this.NS + ": querySQL (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().jdbcQuerySQL(
				"select * from " + jws.tests.JDBC.TEST_TABLE,
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.code !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},

	// this spec tests the file save method of the fileSystem plug-in
	testInsertSQL: function() {
		
		var lSpec = this.NS + ": insertSQL (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().jdbcUpdateSQL(
				"insert into " 
					+ jws.tests.JDBC.TEST_TABLE 
					+ " (id, text) values (1, '" 
					+ jws.tests.JDBC.TEST_STRING_1 + "')",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected ).toEqual( 1 );
			});

		});
	},

	// this spec tests the file save method of the fileSystem plug-in
	testUpdateSQL: function() {
		
		var lSpec = this.NS + ": updateSQL (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().jdbcUpdateSQL(
				"update " 
				+ jws.tests.JDBC.TEST_TABLE 
				+ " set text = '" + jws.tests.JDBC.TEST_STRING_2 + "'"
				+ " where id = 1",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected ).toEqual( 1 );
			});

		});
	},

	// this spec tests the file save method of the fileSystem plug-in
	testDeleteSQL: function() {
		
		var lSpec = this.NS + ": deleteSQL (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().jdbcUpdateSQL(
				"delete from " 
				+ jws.tests.JDBC.TEST_TABLE 
				+ " where id = 1",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected ).toEqual( 1 );
			});

		});
	},

	runSpecs: function() {
		// run alls tests within an outer test suite
		this.testCreateTable();
		this.testInsertSQL();
		this.testUpdateSQL();
		this.testQuerySQL();
		this.testDeleteSQL();
		this.testDropTable();
	},

	runSuite: function() {
		
		// run alls tests as a separate test suite
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};
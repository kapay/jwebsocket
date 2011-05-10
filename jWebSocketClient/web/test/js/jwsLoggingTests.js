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


jws.tests.Logging = {

	NS: "jws.tests.logging", 
	TABLE: "ACTION_LOG",
	PRIMARY_KEY: "ID",
	SEQUENCE: "SQ_ACTION_LOG_ID",
	MESSAGE: "This is an message from the automated test suite.",
	
	mLogId: null,

	// this spec tests the file save method of the fileSystem plug-in
	testLog: function() {
		var lSpec = this.NS + ": LogEvent";
		
		it( lSpec, function () {

			var lResponse = {};
			var lNow = new Date();
			var lData = {
				"MESSAGE": jws.tests.Logging.MESSAGE,
				"BROWSER": "Mein Browser",
				"IP": "${ip}",
				"TIMESTAMP": "TO_DATE('" +
					lNow.getUTCFullYear().toString() + "/" +
					jws.tools.zerofill( lNow.getUTCMonth() + 1, 2 ) + "/" +
					jws.tools.zerofill( lNow.getUTCDate(), 2 ) + " " +
					jws.tools.zerofill( lNow.getUTCHours(), 2 ) + "/" +
					jws.tools.zerofill( lNow.getUTCMinutes(), 2 ) + "/" +
					jws.tools.zerofill( lNow.getUTCSeconds(), 2 ) +
					"','YYYY/MM/DD HH24/MI/SS')"
			};
			jws.Tests.getAdminConn().loggingEvent( jws.tests.Logging.TABLE, lData, {
				primaryKey: jws.tests.Logging.PRIMARY_KEY,
				sequence: jws.tests.Logging.SEQUENCE,
				OnResponse: function( aToken ) {
					lResponse = aToken;
					jws.tests.Logging.mLogId = lResponse.key;
				}
			});

			waitsFor(
				function() {
					return( lResponse.rowsAffected == 1 && lResponse.key > 0 );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.rowsAffected ).toEqual( 1 );
			});

		});
	},

	// this spec tests the file save method of the fileSystem plug-in
	testGetLog: function() {
		var lSpec = this.NS + ": GetLog";
		
		it( lSpec, function () {

			var lResponse = {};
			var lDone = false;
			jws.Tests.getAdminConn().loggingGetEvents( jws.tests.Logging.TABLE, {
				primaryKey: jws.tests.Logging.PRIMARY_KEY,
				fromKey: jws.tests.Logging.mLogId,
				toKey: jws.tests.Logging.mLogId,
				OnResponse: function( aToken ) {
					lResponse = aToken;
					// check if only one row is returned
					if( lResponse.data.length == 1 ) {
						// check if the row contains the message previously sent.
						var lRow = lResponse.data[ 0 ];
						for( var lIdx = 0, lCnt = lRow.length; lIdx < lCnt; lIdx++ ) {
							if( lRow[ lIdx ] == jws.tests.Logging.MESSAGE ) {
								lDone = true;
								break;
							}
						}
					}
				}
			});

			waitsFor(
				function() {
					return( lDone == true );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lDone ).toEqual( true );
			});

		});
	},


	runSpecs: function() {
		this.testLog();
		this.testGetLog();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};
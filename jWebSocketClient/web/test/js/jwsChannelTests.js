//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the Channel Plug-in
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

jws.tests.Channels = {

	NS: "jws.tests.channels", 

	// this spec tests the listen method of the Channels plug-in
	testSubscribe: function( aChannelName, aAccessKey ) {
		var lSpec = this.NS + ": subscribe (" + aChannelName + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().channelSubscribe( 
				aChannelName,
				aAccessKey,
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},

	// this spec tests the listen method of the Channels plug-in
	testUnsubscribe: function( aChannelName ) {
		var lSpec = this.NS + ": Unsubscribe (publicA)";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().channelUnsubscribe( 
				aChannelName,
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},
	
	runSpecs: function() {
		jws.tests.Channels.testSubscribe( "systemA", "access" );
		jws.tests.Channels.testUnsubscribe( "systemA", "access" );
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}


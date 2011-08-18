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
				{
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				1000
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
				{
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				1000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},

	// this spec tests the create method for a new channel
	testChannelCreate: function( aChannelId, aChannelName, aAccessKey, aSecretKey, 
		aIsPrivate, aIsSystem, aComment, aExpectedReturnCode ) {
		var lSpec = this.NS + ": channelCreate (id: " + aChannelId + ", name: " + aChannelName + ", " + aComment + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().channelCreate( 
				aChannelId, 
				aChannelName,
				{	isPrivate: aIsPrivate,
					isSystem: aIsSystem,
					accessKey: aAccessKey,
					secretKey: aSecretKey,
					
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( aExpectedReturnCode );
			});

		});
	},

	// this spec tests the create method for a new channel
	testChannelRemove: function( aChannelId, aAccessKey, aSecretKey, 
		aComment, aExpectedReturnCode ) {
		var lSpec = this.NS + ": channelRemove (id: " + aChannelId + ", " + aComment + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().channelRemove( 
				aChannelId, 
				{	accessKey: aAccessKey,
					secretKey: aSecretKey,
					
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( aExpectedReturnCode );
			});

		});
	},
	
	runSpecs: function() {
		// testing existing, pre-defined channels
		jws.tests.Channels.testSubscribe( "systemA", "access" );
		jws.tests.Channels.testUnsubscribe( "systemA", "access" );
		
		// creating new public channels
		jws.tests.Channels.testChannelCreate( "myPubSec", "myPublicSecure", "myPublicAccess", "myPublicSecret", false, false, 
			"Creating public channel with correct credentials (allowed)", 0 );
		jws.tests.Channels.testChannelCreate( "myPubSec", "myPublicSecure", "myPublicAccess", "myPublicSecret", false, false, 
			"Creating public channel that already exists (invalid)", -1 );
		jws.tests.Channels.testChannelCreate( "myPubUnsec", "myPublicUnsecure", "", "", false, false, 
			"Creating public channel w/o access key and secret key (allowed)", 0 );

		// creating new private channels
		jws.tests.Channels.testChannelCreate( "myPrivSec", "myPrivateSecure", "myPrivateAccess", "myPrivateSecret", true, false, 
			"Creating private channel with access key and secret key (allowed)", 0 );
		jws.tests.Channels.testChannelCreate( "myPrivUnsec", "myUnsecurePrivateChannel", "", "", true, false, 
			"Creating private channel w/o access key and secret key (not allowed)", -1 );
			
		// removing public channels
		jws.tests.Channels.testChannelRemove( "myPubSec", "myInvalidAccess", "myInvalidSecret",
			"Removing secure public channel with incorrect credentials (not allowed)", -1 );
		jws.tests.Channels.testChannelRemove( "myPubSec", "myPublicAccess", "myPublicSecret",
			"Removing secure public channel with correct credentials (allowed)", 0 );
		jws.tests.Channels.testChannelRemove( "myPubUnsec", "", "",
			"Removing unsecure public channel w/o credentials (allowed)", 0 );
			
		// removing private channels
		jws.tests.Channels.testChannelRemove( "myPrivSec", "myInvalidAccess", "myInvalidSecret",
			"Removing private channel with invalid credentials (invalid)", -1 );
		jws.tests.Channels.testChannelRemove( "myPrivSec", "myPrivateAccess", "myPrivateSecret",
			"Removing private channel with correct credentials (allowed)", 0 );
		jws.tests.Channels.testChannelRemove( "myPrivSec", "myPrivateAccess", "myPrivateSecret",
			"Removing private channel that should alredy have been removed (invalid)", -1 );
		jws.tests.Channels.testChannelRemove( "myPrivUnsec", "", "",
			"Removing channel that should never have existed (invalid)", -1 );
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}


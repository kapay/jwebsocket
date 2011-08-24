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
	TEST_MESSAGE: "this is a test message",
	
	ST_INIT: 0,
	ST_AUTH: 1,

	// this spec tests the subscribe method of the Channels plug-in
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

	// this spec tests the unsubscribe method of the Channels plug-in
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
				{
					isPrivate: aIsPrivate,
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
	testChannelAuth: function( aChannelId, aAccessKey, aSecretKey, 
		aComment, aExpectedReturnCode ) {
		var lSpec = this.NS + ": channelAuth (id: " + aChannelId + ", " + aComment + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().channelAuth( 
				aChannelId, 
				aAccessKey,
				aSecretKey,
				{
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
	testChannelPublish: function( aChannelId, aData,
		aComment, aExpectedReturnCode ) {
		var lSpec = this.NS + ": channelAuth (id: " + aChannelId + ", data: " + aData + ", " + aComment + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().channelPublish( 
				aChannelId, 
				aData,
				{
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
	testChannelComplexTest: function( aComment) {
		var lSpec = this.NS + ": complex test (" + aComment + ")";
		
		it( lSpec, function () {

			var lPubCnt = 3;
			var lSubCnt = 6;

			var lPubs = [];
			var lSubs = [];
			
			var lPub, lSub;
			
			var lLoggedIn = 0, lChannelId = 0;

			// create a number of publishers
			for( var lPubIdx = 0; lPubIdx < lPubCnt; lPubIdx++ ) {
				lPub = new jws.jWebSocketJSONClient();
				lPubs[ lPubIdx ] = {
					client: lPub, 
					status: jws.tests.Channels.INIT
				};
				lPub.logon( jws.getDefaultServerURL(), jws.Tests.ADMIN_USER, jws.Tests.ADMIN_PWD, {
					OnToken: function ( aToken ) {
						if( "org.jwebsocket.plugins.system" == aToken.ns
							&& "login" == aToken.reqType) {
							// jws.tests.Channels.AUTH;
							// lResponse = aToken;
							lLoggedIn++;
							lChannelId++;
							debugger;
							this.channelCreate( 
								"ch_" + lChannelId, 
								"channel_" + lChannelId, 
								{	isPrivate: false,
									isSystem: false,
									accessKey: "testAccessKey",
									secretKey: "testSecretKey",

									OnResponse: function( aToken ) {
										if( aToken.code == 0 ) {
											this.channelAuth( 
												aChannelId, 
												aAccessKey,
												aSecretKey,
												{
													OnResponse: function( aToken ) {
														lResponse = aToken;
													}
												}
											);
										}
									}
								}
							);
						}
					}
				});
			}
			// create a number of subscribers
			for( var lSubIdx = 0; lSubIdx < lSubCnt; lSubIdx++ ) {
				lSub = new jws.jWebSocketJSONClient();
				lSubs[ lSubIdx ] = {
					client: lSub,
					status: jws.tests.Channels.INIT
				};
				lSub.logon( jws.getDefaultServerURL(), jws.Tests.ADMIN_USER, jws.Tests.ADMIN_PWD, {
					OnToken: function ( aToken ) {
						if( "org.jwebsocket.plugins.system" == aToken.ns
							&& "login" == aToken.reqType) {
							// jws.tests.Channels.AUTH;
							// lResponse = aToken;
							lLoggedIn++;
						}
					}
				});
			}

			waitsFor(
				function() {
					return( lLoggedIn == lPubCnt + lSubCnt );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lLoggedIn ).toEqual( lPubCnt + lSubCnt );

				// close all opened connections
				for( var lPubIdx = 0; lPubIdx < lPubCnt; lPubIdx++ ) {
					lPubs[ lPubIdx ].client.close();
				}
				for( var lSubIdx = 0; lSubIdx < lSubCnt; lSubIdx++ ) {
					lSubs[ lSubIdx ].client.close();
				}
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
				{
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
				if( lResponse.code !== undefined 
					&& lResponse.code != aExpectedReturnCode ) {
					jasmine.log( "Error: " + lResponse.msg );
				}
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

		// channel authentication prior to publishing
		jws.tests.Channels.testChannelAuth( "myInvalid", "myPublicAccess", "myPublicSecret",
			"Authenticating against invalid channel with access key and secret key (not allowed)", -1 );
		jws.tests.Channels.testChannelAuth( "myPubSec", "", "",
			"Authenticating against public channel w/o access key and secret key (not allowed)", -1 );
		jws.tests.Channels.testChannelPublish( "myPubSec", jws.tests.Channels.TEST_MESSAGE,
			"Publishing test message on a non-authenticated channel (not allowed)", -1 );
		jws.tests.Channels.testChannelAuth( "myPubSec", "myPublicAccess", "myPublicSecret",
			"Authenticating against public channel access key and secret key (allowed)", 0 );
		jws.tests.Channels.testChannelPublish( "myPubSec", jws.tests.Channels.TEST_MESSAGE,
			"Publishing test message on authenticated channel (allowed)", 0 );

		// run complex publish and subscribe test
		jws.tests.Channels.testChannelComplexTest(
			"Multiple publishers distributing messages to multiple subscribers.", 0 );

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

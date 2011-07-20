//	---------------------------------------------------------------------------
//	jWebSocket JMS PlugIn (uses jWebSocket Client and Server)
//	(c) 2011 Innotrade GmbH - jWebSocket.org, Alexander Schulze
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

 
//:author:*:Johannes Smutny

//:package:*:jws
//:class:*:jws.JMSPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.JMSPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to subscribe and unsubscribe at certain _
//:d:en:channel on the server.
jws.JMSPlugIn = {

	// :const:*:NS:String:org.jwebsocket.plugins.channels (jws.NS_BASE +
	// ".plugins.channels")
	// :d:en:Namespace for the [tt]ChannelPlugIn[/tt] class.
	// if namespace changes update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.jms",
	
	SEND_TEXT: "sendText",
	SEND_MAP: "sendMap",
	LISTEN: "listen",
	UNLISTEN: "unlisten",
	
	listen: function(aConnectionFactoryName, aDestinationName, aPubSubDomain) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.LISTEN,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				isPubSubDomain: aPubSubDomain
			});
		}
		return lRes;
	},
	
	unlisten: function(aConnectionFactoryName, aDestinationName, aPubSubDomain) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.UNLISTEN,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				isPubSubDomain: aPubSubDomain
			});
		}
		return lRes;
	},
	
	sendJmsText: function(aConnectionFactoryName, aDestinationName, aPubSubDomain, aText) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_TEXT,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				isPubSubDomain: aPubSubDomain,
				text: aText
			});
		}
		return lRes;
	},
	
	sendJmsMap: function(aConnectionFactoryName, aDestinationName, aPubSubDomain, aMap) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_MAP,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				isPubSubDomain: aPubSubDomain,
				map: aMap
			});
		}
		return lRes;
	},
	
	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.JMSPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "event" == aToken.type ) {
				if( "handleText" == aToken.name ) {
					if( this.OnHandleText ) {
						this.OnHandleText( aToken );
					}
				} else if( "handleMap" == aToken.name ) {
					if( this.OnHandleMap ) {
						this.OnHandleMap( aToken );
					}
				} else if( "channelRemoved" == aToken.name ) {
					if( this.OnChannelRemoved ) {
						this.OnChannelRemoved( aToken );
					}
				} 
			} else if( "getChannels" == aToken.reqType ) {
				if( this.OnChannelsReceived ) {
					this.OnChannelsReceived( aToken );
				}
			}
		}
	},
	
	setHandleMessageCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnHandleText !== undefined ) {
			this.OnHandleText = aListeners.OnHandleText;
		}
		if( aListeners.OnHandleMap !== undefined ) {
			this.OnHandleMap = aListeners.OnHandleMap;
		}
		if( aListeners.OnChannelsReceived !== undefined ) {
			this.OnChannelsReceived = aListeners.OnChannelsReceived;
		}
		if( aListeners.OnChannelRemoved !== undefined ) {
			this.OnChannelRemoved = aListeners.OnChannelRemoved;
		}
	}

};
// add the JMSPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.JMSPlugIn );

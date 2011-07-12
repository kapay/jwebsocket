//	---------------------------------------------------------------------------
//	jWebSocket JMS PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
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


//	---------------------------------------------------------------------------
//  jWebSocket JMS Plug-In
//	---------------------------------------------------------------------------

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
	
	SEND_STRING_MESSAGE: "sendStringMessage",
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
	
	sendStringMessage: function(aConnectionFactoryName, aDestinationName, aPubSubDomain, aStringMessage) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_STRING_MESSAGE,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				isPubSubDomain: aPubSubDomain,
				stringMessage: aStringMessage
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
				if( "handleMessageString" == aToken.name ) {
					if( this.OnHandleMessageString ) {
						this.OnHandleMessageString( aToken );
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
		if( aListeners.OnHandleMessageString !== undefined ) {
			this.OnHandleMessageString = aListeners.OnHandleMessageString;
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

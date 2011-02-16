//	---------------------------------------------------------------------------
//	jWebSocket Channel PlugIn (uses jWebSocket Client and Server)
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
//  jWebSocket Channel Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.ChannelPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.ChannelPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to subscribe and unsubscribe at certain _
//:d:en:channel sn the server.
jws.ChannelPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.channels (jws.NS_BASE + ".plugins.channels")
	//:d:en:Namespace for the [tt]ChannelPlugIn[/tt] class.
	// if namespace changes update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.channels",

	SUBSCRIBE: "subscribe",
	UNSUBSCRIBE: "unsubscribe",
	GET_CHANNELS: "getChannels",
	CREATE_CHANNEL:  "createChannel",
	REMOVE_CHANNEL:  "removeChannel",
	GET_SUBSCRIBERS: "getSubscribers",
	GET_SUBSCRIPTIONS: "getSubscriptions",

	AUTHORIZE: "authorize",
	PUBLISH: "publish",
	STOP: "stop",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.ChannelPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "event" == aToken.type ) {
				if( "channelCreated" == aToken.name ) {
					if( this.OnChannelCreated ) {
						this.OnChannelCreated( aToken );
					}
				} else if( "channelRemoved" == aToken.name ) {
					if( this.OnChannelRemoved ) {
						this.OnChannelRemoved( aToken );
					}
				}
			}
		}
	},

	//:m:*:channelSubscribe
	//:d:en:Registers the client at the given channel on the server. _
	//:d:en:After this operation the client obtains all messages on this _
	//:d:en:channel. Basically, a client can subscribe at multiple channels.
	//:d:en:If no channel with the given ID exists on the server an error token _
	//:d:en:is returned. Depending on the type of the channel it may take more _
	//:d:en:or less time until you get the first token from the channel.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelSubscribe: function( aChannel, aAccessKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.SUBSCRIBE,
				channel: aChannel,
				access_key: aAccessKey
			});
		}
		return lRes;
	},

	//:m:*:channelUnsubscribe
	//:d:en:Unsubscribes the client from the given channel on the server.
	//:d:en:From this point in time the client does not receive any messages _
	//:d:en:on this channel anymore.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelUnsubscribe: function( aChannel ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.UNSUBSCRIBE,
				channel: aChannel
			});
		}
		return lRes;
	},

	//:m:*:channelAuth
	//:d:en:Authenticates the client at a certain channel to publish messages.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aAccessKey:String:Access key configured for the channel.
	//:a:en::aSecretKey:String:Secret key configured for the channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelAuth: function( aChannel, aAccessKey, aSecretKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.AUTHORIZE,
				channel: aChannel,
				login: this.getUsername(),
				access_key: aAccessKey,
				secret_key: aSecretKey
			});
		}
		return lRes;
	},

	//:m:*:channelPublish
	//:d:en:Sends a message to the given channel on the server.
	//:d:en:The client needs to be authenticated against the server and the
	//:d:en:channel to publish data. All clients that subscribed to the channel
	//:d:en:will receive the message.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aData:String:Data to be sent to the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelPublish: function( aChannel, aData ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.PUBLISH,
				channel: aChannel,
				data: aData
			});
		}
		return lRes;
	},

	channelCreate: function( aChannel, aName ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.CREATE_CHANNEL,
				channel: aChannel,
				name: aName
			});
		}
		return lRes;
	},

	channelRemove: function( aChannel ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.REMOVE_CHANNEL,
				channel: aChannel
			});
		}
		return lRes;
	},

	channelGetSubscribers: function( aChannel ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_SUBSCRIBERS,
				channel: aChannel
			});
		}
		return lRes;
	},

	channelGetSubscriptions: function() {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_SUBSCRIPTIONS
			});
		}
		return lRes;
	},


	//:m:*:channelPublish
	//:d:en:Tries to obtain all id of the channels
	//:a:en:::none
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelGetIds: function() {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_CHANNELS
			});
		}
		return lRes;
	},

	setChannelCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnChannelCreated !== undefined ) {
			this.OnChannelCreated = aListeners.OnChannelCreated;
		}
		if( aListeners.OnChannelRemoved !== undefined ) {
			this.OnChannelRemoved = aListeners.OnChannelRemoved;
		}
	}

};

// add the ChannelPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.ChannelPlugIn );

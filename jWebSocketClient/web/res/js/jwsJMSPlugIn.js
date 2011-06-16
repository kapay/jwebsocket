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

	SUBSCRIBE: "subscribe",
	UNSUBSCRIBE: "unsubscribe",
	GET_CHANNELS: "getChannels",
	CREATE_CONNECTION:  "createConnection",
	CREATE_SESSION:  "createSession",
	CREATE_QUEUE:  "createQueue",
	CREATE_CONSUMER:  "createConsumer",
	REMOVE_CHANNEL:  "removeChannel",
	GET_SUBSCRIBERS: "getSubscribers",
	GET_SUBSCRIPTIONS: "getSubscriptions",

	AUTHORIZE: "authorize",
	PUBLISH: "publish",
	STOP: "stop",
	AUTO_ACKNOWLEDGE: 1,
	CLIENT_ACKNOWLEDGE: 2,
	DUPS_OK_ACKNOWLEDGE: 3,
	SESSION_TRANSACTED: 0,
	
	MessageConsumer: function(aDestinationName) {
		this.mDestinationName = aDestinationName;
		this.mRes;
		this.setRes = function(aRes) {
			this.mRes = aRes;
		}
		this.getRes = function() {
			return this.mRes;
		}
	},
	
	Destination: function(aName) {
		this.mName = aName;
		this.mRes;
		this.setRes = function(aRes) {
			this.mRes = aRes;
		}
		this.getRes = function() {
			return this.mRes;
		}
	},

	Queue: function(aName) {
	},
	
	Session: function(aWSC) {
		this.mWSC = aWSC;
		this.mTransacted;
		this.mAcknowledgeMode;
		this.mRes;
		this.setRes = function(aRes) {
			this.mRes = aRes;
		}
		this.getRes = function() {
			return this.mRes;
		}
		this.setTransacted = function(aTransacted) {
			this.mTransacted = aTransacted;
		}
		this.getTransacted = function() {
			return this.mTransacted;
		}
		this.setAcknowledgeMode = function(aAcknowledgeMode) {
			this.mAcknowledgeMode = aAcknowledgeMode;
		}
		this.getAcknowledgeMode = function() {
			return this.mAcknowledgeMode;
		}
		this.createQueue = function(aQueueName) {
			var lRes = this.mWSC.createQueue(aQueueName);
			var lQueue = new jws.JMSPlugIn.Queue(aQueueName);
			lQueue.setRes(lRes);
			return lQueue;
		}
		this.createConsumer = function(aDestinationName) {
			var lRes = this.mWSC.createConsumer(aDestinationName);
			var lConsumer = new jws.JMSPlugIn.MessageConsumer(aDestinationName);
			lConsumer.setRes(lRes);
			return lConsumer;
		}
	},
	
	
	Connection: function(aWSC) {
		this.mWSC = aWSC;
		this.mRes;
		this.setRes = function(aRes) {
			this.mRes = aRes;
		}
		this.getRes = function() {
			return this.mRes;
		}
		this.createSession = function(aTransacted, aAcknowledgeMode) {
			var lRes = this.mWSC.createSession(aTransacted, aAcknowledgeMode);
			var lSession = new jws.JMSPlugIn.Session(this.mWSC);
			lSession.setRes(lRes);
			lSession.setTransacted(aTransacted);
			lSession.setAcknowledgeMode(aAcknowledgeMode);
			return lSession;
		}
	},
	
	ConnectionFactory: function(aJwssUrl, aWSC) {
		this.mUrl = aJwssUrl;
		this.mWSC = aWSC;
		this.logon = function(aUsername, aPassword, aOptions) {
			var lRes = this.mWSC.logon(this.mUrl, aUsername, aPassword, aOptions);
			return lRes;
		}
		this.createConnection = function() {
			var lRes = this.mWSC.createConnection();
			var lConnection = new jws.JMSPlugIn.Connection(this.mWSC);
			lConnection.setRes(lRes);
			return lConnection;
		}
	},
	
	// :m:*:createConection
	// :d:en:Creates a new channel on the server. If a channel with the given _
	// :d:en:channel-id already exists the create channel request is rejected. _
	// :d:en:A private channel requires an access key, if this is not provided _
	// :d:en:for a private channel the request is rejected. For public channel _
	// :d:en:the access key is optional.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :a:en::aName:String:The name (human readably) of the channel.
	// :r:*:::void:none
	createConnection: function() {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.CREATE_CONNECTION
			});
		}
		return lRes;
	},
	
	createSession: function(aTransacted, aAcknowledgeMode) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.CREATE_SESSION,
				transacted: aTransacted,
				acknowledgeMode: aAcknowledgeMode
			});
		}
		return lRes;
	},
	
	createQueue: function(aQueueName) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.CREATE_QUEUE,
				queueName: aQueueName
			});
		}
		return lRes;
	},
	
	createConsumer: function(aDestinationName) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.CREATE_CONSUMER,
				destinationName: aDestinationName
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
				if( "channelCreated" == aToken.name ) {
					if( this.OnChannelCreated ) {
						this.OnChannelCreated( aToken );
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
	


	// :m:*:channelSubscribe
	// :d:en:Registers the client at the given channel on the server. _
	// :d:en:After this operation the client obtains all messages on this _
	// :d:en:channel. Basically, a client can subscribe at multiple channels.
	// :d:en:If no channel with the given ID exists on the server an error token
	// _
	// :d:en:is returned. Depending on the type of the channel it may take more
	// _
	// :d:en:or less time until you get the first token from the channel.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelSubscribe: function( aChannel, aAccessKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.SUBSCRIBE,
				channel: aChannel,
				accessKey: aAccessKey
			});
		}
		return lRes;
	},

	// :m:*:channelUnsubscribe
	// :d:en:Unsubscribes the client from the given channel on the server.
	// :d:en:From this point in time the client does not receive any messages _
	// :d:en:on this channel anymore.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :r:*:::void:none
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

	// :m:*:channelAuth
	// :d:en:Authenticates the client at a certain channel to publish messages.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :a:en::aAccessKey:String:Access key configured for the channel.
	// :a:en::aSecretKey:String:Secret key configured for the channel.
	// :r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelAuth: function( aChannel, aAccessKey, aSecretKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.AUTHORIZE,
				channel: aChannel,
				login: this.getUsername(),
				accessKey: aAccessKey,
				secretKey: aSecretKey
			});
		}
		return lRes;
	},

	// :m:*:channelPublish
	// :d:en:Sends a message to the given channel on the server.
	// :d:en:The client needs to be authenticated against the server and the
	// :d:en:channel to publish data. All clients that subscribed to the channel
	// :d:en:will receive the message.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :a:en::aData:String:Data to be sent to the server side data channel.
	// :r:*:::void:none
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

	// :m:*:channelRemove
	// :d:en:Removes a (non-system) channel on the server. Only the owner of _
	// :d:en:channel can remove a channel. If a accessKey/secretKey pair is _
	// :d:en:defined for a channel this needs to be passed as well, otherwise _
	// :d:en:the remove request is rejected.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :r:*:::void:none
	channelRemove: function( aId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lAccessKey = null;
			var lSecretKey = null;
			var lOwner = null;
			var lPassword = null;
			if( aOptions ) {
				if( aOptions.accessKey != undefined ) {
					lAccessKey = aOptions.accessKey;
				}
				if( aOptions.secretKey != undefined ) {
					lSecretKey = aOptions.secretKey;
				}
				if( aOptions.owner != undefined ) {
					lOwner = aOptions.owner;
				}
				if( aOptions.password != undefined ) {
					lPassword = aOptions.password;
				}
			}
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.REMOVE_CHANNEL,
				channel: aId,
				accessKey: lAccessKey,
				secretKey: lSecretKey,
				owner: lOwner,
				password: lPassword
			});
		}
		return lRes;
	},

	// :m:*:channelGetSubscribers
	// :d:en:Returns all channels to which the current client currently has
	// :d:en:subscribed to. This also includes private channels. The owners of
	// :d:en:the channels are not returned due to security reasons.
	// :a:en::aChannel:String:The id of the server side data channel.
	// :a:en::aAccessKey:String:Access Key for the channel (required for private
	// channels, optional for public channels).
	// :r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelGetSubscribers: function( aChannel, aAccessKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_SUBSCRIBERS,
				channel: aChannel,
				accessKey: aAccessKey
			});
		}
		return lRes;
	},

	// :m:*:channelGetSubscriptions
	// :d:en:Returns all channels to which the current client currently has
	// :d:en:subscribed to. This also includes private channels. The owners of
	// :d:en:the channels are not returned due to security reasons.
	// :a:en:::none
	// :r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
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


	// :m:*:channelPublish
	// :d:en:Tries to obtain all id of the channels
	// :a:en:::none
	// :r:*:::void:none
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
		if( aListeners.OnChannelsReceived !== undefined ) {
			this.OnChannelsReceived = aListeners.OnChannelsReceived;
		}
		if( aListeners.OnChannelRemoved !== undefined ) {
			this.OnChannelRemoved = aListeners.OnChannelRemoved;
		}
	}

};

jws.JMSPlugIn.Queue.prototype = new jws.JMSPlugIn.Destination;

// add the ChannelPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.JMSPlugIn );
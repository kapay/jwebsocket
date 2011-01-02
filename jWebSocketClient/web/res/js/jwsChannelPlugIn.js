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

	//:const:*:NS:String:org.jwebsocket.plugins.channeling (jws.NS_BASE + ".plugins.channeling")
	//:d:en:Namespace for the [tt]ChannelPlugIn[/tt] class.
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.channels",

	PUBLISHER: "publisher",
	SUBSCRIBER: "subscriber",

	AUTHORIZE: "authorize",
	PUBLISH: "publish",
	STOP: "stop",
	SUBSCRIBE: "subscribe",
	UNSUBSCRIBE: "unsubscribe",

	EVENT: "event",
	

	//:m:*:channelSubscribe
	//:d:en:Registers the client at the given channel on the server. _
	//:d:en:After this operation the client obtains all messages in this _
	//:d:en:channel. Basically a client can subscribe at multiple channels.
	//:d:en:If no channel with the given ID exists on the server an error token _
	//:d:en:is returned. Depending on the type of the channel it may take more _
	//:d:en:or less time until you get the first token from the channel.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	// TODO: Use checkConnected()
	channelSubscribe: function( aChannel ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.SUBSCRIBER,
				event: jws.ChannelPlugIn.SUBSCRIBE,
				channel: aChannel
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:channelUnsubscribe
	//:d:en:Unsubscribes the client from the given channel on the server.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	// TODO: Use checkConnected()
	channelUnsubscribe: function( aChannel ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.SUBSCRIBER,
				event: jws.ChannelPlugIn.UNSUBSCRIBE,
				channel: aChannel
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:channelAuth
	//:d:en:Authenticates the client at a certain channel to publish messages.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aData:String:Data to be sent to the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	// TODO: Use checkConnected()
	channelAuth: function( aChannel, aUsername, aAccessKey, aSecretKey ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.PUBLISHER,
				event: jws.ChannelPlugIn.AUTHORIZE,
				channel: aChannel,
				login: aUsername,
				access_key: aAccessKey,
				secret_key: aSecretKey
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:channelPublish
	//:d:en:Sends a message to the given channel on the server,
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aData:String:Data to be sent to the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	// TODO: Use checkConnected()
	channelPublish: function( aChannel, aData ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.PUBLISHER,
				event: jws.ChannelPlugIn.PUBLISH,
				channel: aChannel,
				data: aData
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	}

};

// add the ChannelPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.ChannelPlugIn );

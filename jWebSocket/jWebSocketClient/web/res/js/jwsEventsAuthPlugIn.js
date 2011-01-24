//	---------------------------------------------------------------------------
//	jWebSocket Base and EventsAuth Plug-In
//	Copyright (C) 2010 Innotrade GmbH, Herzogenrath, Germany (jWebSocket.org)
//	Author(s): Rolando Santamaria Maso
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


//:author:*:Rolando Santamaria Maso
//:created:*:2010-11-29

jws.BasePlugIn = {
	events: [],

	notify: function(aEventName, aOptions){
		//Token creation
		var lToken = {};

		if (aOptions.args){
			Ext.apply(lToken, aOptions.args)
			delete aOptions.args
		}

		lToken.type      = aEventName;
		lToken.ns        = this.ns;
		lToken._IS_EM_   = true;
    
		//Server notification of the token
		if (this.client.isConnected())
			return this.client.sendToken(lToken, aOptions);
		else
			throw "The client is not connected!";
	},

	processToken: function(aToken) {
		log("Incoming token type: " + Ext.encode(aToken))
	},

	initialize: function(){
		jws.oop.addPlugIn( jws.jWebSocketTokenClient, this)
	}
}

jws.EventsAuthPlugIn = function EventsAuthPlugIn(aOptions){
	Ext.apply(this , aOptions)
}

jws.EventsAuthPlugIn.prototype = jws.BasePlugIn;


//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------

//:file:*:jwsEventsPlugIn.js
//:d:en:Implements the EventsPlugIn in the client side

//:package:*:jws
//:class:*:jws.EventsNotifier
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsNotifier[/tt] class. _
//:d:en:This class handle raw events notifications to/from the server side.
jws.oop.declareClass( "jws", "EventsNotifier", null, {
	jwsClient: {}
	,
	filterChain: []
	,
	plugIns: []
	,
	//:m:*:initialize
	//:d:en:Initialize this component. 
	//:a:en::::none
	//:r:*:::void:none
	initialize : function(){
		jws.oop.addPlugIn(jws.jWebSocketTokenClient, this);
	}
	,
	//:m:*:notify
	//:d:en:Notify an event in the server side
	//:a:en::aEventName:String:The event name.
	//:a:en::aOptions:Object:Contains the event arguments and the OnResponse, OnSuccess and OnFailure callbacks.
	//:r:*:::void:none
	notify: function(aEventName, aOptions){
		if (this.jwsClient.isConnected()){
			var lToken = {};
			if (aOptions.args){
				lToken = aOptions.args;
				delete (aOptions.args);
			}
			lToken.type      = aEventName;
			lToken._IS_EM_   = true;
			
			//Generating the unique token identifier
			aOptions._tokenUID = hex_md5(lToken);

			var aOnResponseObject = new jws.OnResponseObject();
			aOnResponseObject.request = aOptions;
			aOnResponseObject.filterChain = this.filterChain;

			if (undefined != aOptions.eventDefinition){
				for (var i = 0; i < this.filterChain.length; i++){
					try
					{
						this.filterChain[i].beforeCall(lToken, aOnResponseObject);
					}
					catch(err)
					{
						switch (err)
						{
							case "stop_filter_chain":
								return;
							break;
							default:
								throw err;
							break;
						}
					}
					
				}
			}

			this.jwsClient.sendToken(lToken, aOnResponseObject);
		}
		else
			throw "client:not_connected";
    }
	,
	//:m:*:processToken
	//:d:en:Processes an incoming token. Used to support S2C events notifications. _
	//:d:en:Use the "event_name" and "plugin_id" information to execute _
	//:d:en:a targered method in a plug-in.
	//:a:en::aToken:Object:Token to be processed
	//:r:*:::void:none
	processToken: function (aToken) {
		if ("s2c.event_notification" == aToken.type){
			var event_name = aToken.event_name;
			var plugin_id = aToken.plugin_id;

			if (undefined != this.plugIns[plugin_id] && undefined != this.plugIns[plugin_id][event_name]){
				result = this.plugIns[plugin_id][event_name](aToken);
				
				//Sending response back to the server
				if (undefined != aToken.response_type){
					this.notify("s2c.onresponse", {
						args: {
							req_id: aToken.uid,
							response: result
						}
					});
				}
			}
			else {
				//Sending the "not supported" event notification
				this.notify("s2c.event_not_supported", {
					args: {
						req_id: aToken.uid
					}
				});
				throw "s2c_event_support_not_found:" + event_name;
			}
		}
	}
});

//:package:*:jws
//:class:*:jws.OnResponseObject
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.OnResponseObject[/tt] class. _
//:d:en:This class offer support for the "OnSuccess" and "OnFailure" callbacks
jws.oop.declareClass( "jws", "OnResponseObject", null, {
	request: {}
	,
	filterChain: []
	,
	OnResponse: function(aResponseToken){
		if (undefined != this.request.eventDefinition){
			var index = this.filterChain.length - 1;
			while (index > -1){
				try
				{
					this.filterChain[index].afterCall(this.request, aResponseToken);
				}
				catch(err)
				{
					switch (err)
					{
						case "stop_filter_chain":
							return;
						break;
						default:
							throw err;
						break;
					}
				}
				index--;
			}
		}
		
		if (aResponseToken.code == 0){
			if (undefined != this.request.OnResponse)
				this.request.OnResponse(aResponseToken);

			if (undefined != this.request.OnSuccess)
				this.request.OnSuccess(aResponseToken);
		}
		else {
			if (undefined != this.request.OnResponse)
				this.request.OnResponse(aResponseToken);

			if (undefined != this.request.OnFailure)
				this.request.OnFailure(aResponseToken);
		}
    }
});

//:package:*:jws
//:class:*:jws.EventsPlugInGenerator
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsPlugInGenerator[/tt] class. _
//:d:en:This class handle the generation of server plug-ins as _
//:d:en:Javascript objects.
jws.oop.declareClass( "jws", "EventsPlugInGenerator", null, {

	//:m:*:generate
	//:d:en:Processes an incoming token. Used to support S2C events notifications. _
	//:a:en::aPlugInId:String:Remote plug-in "id" to generate in the client.
	//:a:en::aNotifier:jws.EventsNotifier:The event notifier used to connect with the server.
	//:a:en::OnReady:Function:This callback is called when the plug-in has been generated.
	//:r:*:::void:none
	generate: function(aPlugInId, aNotifier, OnReady){
		var plugIn = new jws.EventsPlugIn();
		plugIn.notifier = aNotifier;

		aNotifier.notify("plugin.getapi", {
			args: {
				plugin_id: aPlugInId
			}
			,
			plugIn: plugIn
			,
			OnReady: OnReady
			,
			OnSuccess: function(aResponseToken){
				this.plugIn.id = aResponseToken.id;
				this.plugIn.plugInAPI = aResponseToken.api;

				//Generate the plugin here
				for (method in aResponseToken.api){
					eval("this.plugIn." + method + "=function(aOptions){if (undefined == aOptions){aOptions = {};};var eventName=this.plugInAPI."+method+".type; aOptions.eventDefinition=this.plugInAPI."+ method + "; this.notifier.notify(eventName, aOptions);}")
				}

				//Registering the plugin in the notifier
				this.plugIn.notifier.plugIns[this.plugIn.id] = this.plugIn;

				//Plugin is ready to use
				this.OnReady(this.plugIn);
			}
			,
			OnFailure: function(aResponseToken){
				throw aResponseToken.msg;
			}	
		});

		return plugIn;
	}
});

//:package:*:jws
//:class:*:jws.EventsPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsPlugIn[/tt] class. _
//:d:en:This class represents an abstract client plug-in. The methods are _
//:d:en:generated in runtime.
jws.oop.declareClass( "jws", "EventsPlugIn", null, {
	id: ""
	,
	notifier: {}
	,
	plugInAPI: {}
	
	//Methods are generated in runtime!
	//Custom methods can be added using the OnReady callback
});

//:package:*:jws
//:class:*:jws.EventsBaseFilter
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsBaseFilter[/tt] class. _
//:d:en:This class represents an abstract client filter.
jws.oop.declareClass( "jws", "EventsBaseFilter", null, {

	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){}
	,
	//:m:*:afterCall
	//:d:en:This method is called after every C2S event notification.
	//:a:en::aRequest:Object:The request to be filtered.
	//:a:en::aResponseToken:Object:The response token from the server.
	//:r:*:::void:none
	afterCall: function(aRequest, aResponseToken){}
});

//:package:*:jws
//:class:*:jws.SecurityFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.SecurityFilter[/tt] class. _
//:d:en:This class handle the security for every C2S event notification _
//:d:en:in the client, using the server side security configuration.
jws.oop.declareClass( "jws", "SecurityFilter", jws.EventsBaseFilter, {
	user:[]
	,
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks that the logged in user has the correct roles to notify _
	//:d:en:a custom event in the server.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){
		if (aOnResponseObject.request.eventDefinition.isSecurityEnabled){
			var r, u;
			var roles, users = null;
			var exclusion = false;
			var role_authorized = false;
			var user_authorized = false;
			var stop = false;
			
			//@TODO: Support IP addresses restrictions checks on the JS client

			//Getting users restrictions
			users = aOnResponseObject.request.eventDefinition.users;

			//Getting roles restrictions
			roles = aOnResponseObject.request.eventDefinition.roles;

			//Checking if the user have the allowed roles
			if (users.length > 0){
				var user_match = false;
				for (var i = 0; i < users.length; i++){
					u = users[i];
					
					if ("all" != u){
						exclusion = (u.substring(0,1) == "!") ? true : false;
						u = (exclusion) ? u.substring(1) : u;

						if (u == this.user.username){
							user_match = true;
							if (!exclusion){
								user_authorized = true;
							}
							break;
						}
					} else {
						user_match = true;
						user_authorized = true;
						break;
					}
				}

				//Not Authorized USER
				if (!user_authorized && user_match || 0 == roles.length){
					aOnResponseObject.OnResponse({
						code: -1,
						msg: "Not autorized to notify this event. USER restrictions: " + users.toString()
					});
					this.OnNotAuthorized(aToken);
					throw "stop_filter_chain";
				}
			}

			//Checking if the user have the allowed roles
			if (roles.length > 0){
				for (var i = 0; i < roles.length; i++){
					for (var j = 0; j < this.user.roles.length; j++){
						r = roles[i];
					
						if ("all" != r){
							exclusion = (r.substring(0,1) == "!") ? true : false;
							r = (exclusion) ? r.substring(1) : r;

							if (r == this.user.roles[j]){
								if (!exclusion){
									role_authorized = true;
								}
								stop = true;
								break;
							}
						} else {
							role_authorized = true;
							stop = true;
							break;
						}	
					}
					if (stop){
						break;
					}
				}

				//Not Authorized ROLE
				if (!role_authorized){
					aOnResponseObject.OnResponse({
						code: -1,
						msg: "Not autorized to notify this event. ROLE restrictions: " + roles.toString()
					});
					this.OnNotAuthorized(aToken);
					throw "stop_filter_chain";
				}
			}
		}
	}
	,
	//:m:*:OnNotAuthorized
	//:d:en:This method is called when a "not authorized" event notification _
	//:d:en:is detected. Allows to define a global behiavor for this kind _
	//:d:en:of exception.
	//:a:en::aToken:Object:The "not authorized" token to be processed.
	//:r:*:::void:none
	OnNotAuthorized: function(aToken){
		throw "not_authorized";
	}
});

//:package:*:jws
//:class:*:jws.CacheFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.CacheFilter[/tt] class. _
//:d:en:This class handle the cache for every C2S event notification _
//:d:en:in the client, using the server side cache configuration.
jws.oop.declareClass( "jws", "CacheFilter", jws.EventsBaseFilter, {
	cache:{}
	,
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks if exist a non-expired cached response for the outgoing event. _
	//:d:en:If TRUE, the cached response is used and the server is not notified.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){
		if (aOnResponseObject.request.eventDefinition.isCacheEnabled){
			var cachedResponseToken = this.cache.getItem(aOnResponseObject.request._tokenUID);
			if (null != cachedResponseToken){
				aOnResponseObject.OnResponse(cachedResponseToken);
				throw "stop_filter_chain";
			}
		}
	}
	,
	//:m:*:afterCall
	//:d:en:This method is called after every C2S event notification. _
	//:d:en:Checks if a response needs to be cached. The server configuration _
	//:d:en:for cache used.
	//:a:en::aRequest:Object:The request to be filtered.
	//:a:en::aResponseToken:Object:The response token from the server.
	//:r:*:::void:none
	afterCall: function(aRequest, aResponseToken){
		if (aRequest.eventDefinition.isCacheEnabled){
			this.cache.setItem(aRequest._tokenUID, aResponseToken, {
				expirationAbsolute: null,
				expirationSliding: aRequest.eventDefinition.cacheTime,
				priority: jws.cache.CachePriority.High
			});
		}
	}
});

//:package:*:jws
//:class:*:jws.ValidatorFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.ValidatorFilter[/tt] class. _
//:d:en:This class handle the validation for every argument in the request.
jws.oop.declareClass( "jws", "ValidatorFilter", jws.EventsBaseFilter, {

	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks if the request arguments match with the validation server rules.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){
		var arguments = aOnResponseObject.request.eventDefinition.incomingArgsValidation;
		
		for (var index = 0; index < arguments.length; index++){
			if (!aToken[arguments[index].name] && !arguments[index].optional){
				aOnResponseObject.OnResponse({
					code: -1,
					msg: "Argument '"+arguments[index].name+"' is required!"
				});
				throw "stop_filter_chain";
			}else if (aToken.hasOwnProperty(arguments[index].name)){
				var requiredType = arguments[index].type;
				if (requiredType != typeof(aToken[arguments[index].name])){
					//Supporting 'array' as types too
					if ("array" == requiredType && aToken[arguments[index].name] instanceof Array){
						return;
					}

					aOnResponseObject.OnResponse({
						code: -1,
						msg: "Argument '"+arguments[index].name+"' has invalid type. Required: '"+requiredType+"'"
					});
					throw "stop_filter_chain";
				}
			}
		}
	}
});



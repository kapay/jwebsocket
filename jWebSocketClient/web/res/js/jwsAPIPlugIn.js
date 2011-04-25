//	---------------------------------------------------------------------------
//	jWebSocket API PlugIn (uses jWebSocket Client and Server)
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

//:author:*:kyberneees
//:author:*:aschulze

//:package:*:jws
//:class:*:jws.APIPlugInClass
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.APIPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to register and unregister at certain _
//:d:en:stream sn the server.
jws.APIPlugInClass = {

	//:const:*:NS:String:org.jwebsocket.plugins.API (jws.NS_BASE + ".plugins.api")
	//:d:en:Namespace for the [tt]APIPlugIn[/tt] class.
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.api",
	//:const:*:ID:String:APIPlugIn
	//:d:en:Id for the [tt]APIPlugIn[/tt] class.
	ID: "api",

	hasPlugIn: function( aId, aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.has.plugin",
			plugin_id: aId
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	plugInAPI: function( aId, aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.export.plugin.api",
			plugin_id: aId
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	supportToken: function( aId, aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.support.token",
			token_type: aId
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	serverAPI: function( aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type: "server.export.api"
		};
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	},

	plugInsIds: function( aCallback ) {
		var lToken = {
			ns: jws.APIPlugInClass.NS,
			type:  "server.export.plugin.ids"
		}
		this.conn.sendToken( lToken, {
			OnResponse: aCallback
		});
	}
};

jws.APIPlugIn = function() {
	// do NOT use this.conn = aConn; here!
	// Add the plug-in via conn.addPlugin instead!

	// here you can add optonal instance fields
}

jws.APIPlugIn.prototype = jws.APIPlugInClass;



//:package:*:jws
//:class:*:jws.TestHelperManagerClass
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.TestHelperManagerClass[/tt] class. This _
//:d:en:component handle the TestHelpers map collection.
jws.TestHelperManagerClass = {
	
	//
	// Component used to validate custom tests where 
	// the runtime scenario and the validation is complex.
	//
	//TestHelper = {
	//	/**
	//	 * Initialize the test context.
	//	 *
	//	 * @param token The token used for the test call
	//	 */
	//	initialize: function (token) {},
	//	
	//	/**
	//	 * @return TRUE if the helper is ready to be validate it, FALSE otherwise
	//	 */
	//	isReady: function () {},
	//	
	//	/**
	//	 * Validate the server response
	//	 * 
	//	 * @param response (null if requestType equals to nr) The response token to validate it
	//	 */
	//	validate: function (response) {}
	//}
	
	set: function (ns, tokenType, helper){
		if (!this.helpers[ns]){
			this.helpers[ns] = {};
		}
		this.helpers[ns][tokenType] = helper;
	},
	
	get: function(ns, tokenType){
		return this.helpers[ns][tokenType];
	},
	
	has: function (ns, tokenType) {
		return (null != this.helpers[ns] && null != this.helpers[ns][tokenType]);
	},
	
	remove: function (ns, tokenType){
		delete (this.helpers[ns][tokenType]);
	}
}

jws.TestHelperManager = function TestHelperManager(){
	this.helpers = {};
}

jws.TestHelperManager.prototype = jws.TestHelperManagerClass;


//:package:*:jws
//:class:*:jws.Testing
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.Testing[/tt] class. 
//:d:en:This class provide methods to generate dynamic functional test 
//:d:en:using the Jasmine framework according to a given configuration.
jws.TestingClass = {

	report: [],
	initTimeMarks: [],
	initTimeMarksIndex: 0,
	reportIndexI: 0,
	reportIndexJ: 0,

	valueGenerator: {

		getString: function(length){
			length = (length) ? length : 10;
		
			var characters = 'a,b,c,d,e,f,g,h,i,j,k,m,n,p,q,r,';
			characters += 's,t,u,v,w,x,y,z,1,2,3,4,5,6,7,8,9';
			var charArray = new Array(34);
			charArray = characters.split(',');
		
			var i = 0, j, tmpstr = "";
			do {
				var randscript = -1
			
				while (randscript < 1 || randscript > charArray.length || isNaN(randscript)) {
					randscript = parseInt(Math.random() * charArray.length)
				}
			
				j = randscript;
				tmpstr = tmpstr + charArray[j];
				i = i + 1;
			} while (i < length)
			
			return tmpstr;
		},

		getInteger: function (min, max){ 
			min = (min) ? min : 0;
			max = (max) ? max : 1000;
		
			var interval = max - min; 
			number = Math.random() * interval; 
			number = Math.round(number) 
			return parseInt(min) + number; 
		},
	
		getBoolean: function(){
			return (this.getInteger(0, 1)) ? true : false
		},
	
		getDouble: function (){
			return Math.random() * this.getInteger();
		},

		getNumber: function (){
			return (this.getInteger(0, 1)) ? this.getInteger() : this.getDouble();
		}
	},
	
	parseJSON: function(testValue){
		eval("var obj = {value: "+ testValue + "}");
		return obj.value;
	},
	
	parseParams: function( aParamsDef ) {
		var params = {};
		var end = aParamsDef.length;

		var value;
		for ( var i = 0; i < end; i++ ){
			if( aParamsDef[i].testValue ) {
				value = testing.parseJSON(aParamsDef[i].testValue);
			} else if( aParamsDef[i].optional ){
			// do nothing!
			} else {
				//Generating the param value
				switch( aParamsDef[i].type ) {
					case "string":
						value = testing.valueGenerator.getString();
						break;
					case "integer":
						value = testing.valueGenerator.getInteger();
						break;
					case "double":
						value = testing.valueGenerator.getDouble();
						break;
					case "boolean":
						value = testing.valueGenerator.getBoolean();
						break;
					case "number":
						value = testing.valueGenerator.getNumber();
						break;
					default:
						break;
				}
			}

			// setting the argument value
			params[ aParamsDef[i].name ] = value;
		}

		return params;
	},
	
	describePlugIn: function( aConn, aServerPlugIn ) {

		describe( aServerPlugIn.id + ": " +  aServerPlugIn.comment, function() {
			var lCnt =  aServerPlugIn.supportedTokens.length;
			for (var lIdx = 0; lIdx < lCnt; lIdx++ ) {
			
				var lToken = aServerPlugIn.supportedTokens[ lIdx ];
				lToken.ns = aServerPlugIn.namespace;
						
				if( "none" == lToken.requestType ) {
					console.log ("'" + lToken.type + "' test omitted!")
					continue;
				}

				var lComment = lToken.type + ": " + lToken.comment;
				var lDef = lToken;

				it( lComment, function () {

					var lCall = {
						ready: false,
						token: {},
						response: {},
						def: lDef,

						isReady: function (){
							if ("nr" == this.def.requestType){
								var hIsReady = true;
								if (testing.helpers.has(this.def.ns, this.def.type)
									&& typeof testing.helpers.get(this.def.ns, this.def.type)['isReady'] == 'function'){
									hIsReady = testing.helpers.get(this.def.ns, this.def.type).isReady();
								}
								return hIsReady;
							} else {
								var hIsReady = true;
								if (testing.helpers.has(this.def.ns, this.def.type)
									&& typeof testing.helpers.get(this.def.ns, this.def.type)['isReady'] == 'function'){
									hIsReady = testing.helpers.get(this.def.ns, this.def.type).isReady();
								}
								return this.ready && hIsReady;
							}
						},

						OnResponse: function(response){
							this.response = response;
							this.ready = true;
						}

					};

					lCall.token = testing.parseParams(lCall.def.inArguments);
					lCall.token.type = lCall.def.type;
					lCall.token.ns = lCall.def.ns;

					if (testing.helpers.has(lCall.def.ns, lCall.def.type)
						&& typeof testing.helpers.get(lCall.def.ns, lCall.def.type)['initialize'] == 'function'){
						testing.helpers.get(lCall.def.ns, lCall.def.type).initialize(lCall.token);
					}

					testing.initTimeMarks[testing.initTimeMarksIndex++] = new Date().getTime();
					
debugger;

					if( "nr" == lCall.def.requestType ) {
						aConn.sendToken( lCall.token, {} );
					} else {
						aConn.sendToken( lCall.token, lCall );
					}

					testing.report[testing.reportIndexI++] = '';
					waitsFor(function() {
						return lCall.isReady();
					}, 'calling: ' + lDef.type + ' ...', 20000);

					runs(function () {
						testing.report[testing.reportIndexI - 1] = new Date().getTime() - testing.initTimeMarks[testing.initTimeMarksIndex - 1];
						if (testing.helpers.has(lCall.def.ns, lCall.def.type)
							&& typeof testing.helpers.get(lCall.def.ns, lCall.def.type)['validate'] == 'function'){
							testing.helpers.get(lCall.def.ns, lCall.def.type).validate(lCall.response);
						} else if ('wr' == lCall.def.requestType){
							expect(lCall.response.code).toEqual(lCall.def.responseCode);
							var end = lCall.def.outArguments.length;
							for (var j = 0; j < end; j++){
								if (lCall.def.outArguments[j].optional
									&& !(lCall.response[lCall.def.outArguments[j].name])){
									continue;
								}else {
									if (lCall.def.outArguments[j].testValue){
										var expectedValue = testing.parseJSON(lCall.def.outArguments[j].testValue);
										expect(lCall.response[lCall.def.outArguments[j].name]).
										toEqual(expectedValue);
									} else {
										expect(lCall.response[lCall.def.outArguments[j].name]).
										toBeTypeOf(lCall.def.outArguments[j].type);
									}
								}
							}
						}
					});
				});
			}	
		});
	}
}

jws.Testing = function(helpersManager) {
	this.helpers = helpersManager;
}
jws.Testing.prototype = jws.TestingClass;


//Creating the singleton instance
testing = new jws.Testing( new jws.TestHelperManager() );
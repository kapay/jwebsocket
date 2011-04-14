/**
 * Base to generate the plug-in tests
 */
function describePlugIn(p) {
	
	describe(p.id + ": " + p.comment, function() {
		var end = p.supportedTokens.length;
		for (var i = 0; i < end; i++){	
			
			var t = p.supportedTokens[i];
			if ('none' == t.requestType){
				console.log ("'" + t.type + "' test omitted!")
				continue;
			}
			eval ("it(\""+ t.type + ": " + t.comment +"\", function () {\n\
				var call = {\n\
					ready: false,\n\
					token: {},\n\
					response: {},\n\
					def: " + JSON.stringify(t) +",\n\
					isReady: function (){\n\
						if ('nr' == this.def.requestType){\n\
							var hIsReady = true;\n\
							if (jws.t.helpers.has(this.def.type) \n\
								&& typeof jws.t.helpers.get(this.def.type)['isReady'] == 'function'){\n\
								hIsReady = jws.t.helpers.get(this.def.type).isReady();\n\
							}\n\
							return hIsReady;\n\
						} else {\n\
							var hIsReady = true;\n\
							if (jws.t.helpers.has(this.def.type) \n\
								&& typeof jws.t.helpers.get(this.def.type)['isReady'] == 'function'){\n\
								hIsReady = jws.t.helpers.get(this.def.type).isReady();\n\
							}\n\
							return this.ready && hIsReady;\n\
						}\n\
					},\n\
					OnResponse: function(response){\n\
						this.response = response;\n\
						this.ready = true;\n\
					}\n\
				};\n\
		\n\
				call.token = parseParams(call.def.inArguments);\n\
				call.token.type = call.def.type;\n\
		\n\
				if (jws.t.helpers.has(call.def.type) \n\
					&& typeof jws.t.helpers.get(call.def.type)['initialize'] == 'function'){\n\
					jws.t.helpers.get(call.def.type).initialize(call.token);\n\
				}\n\
		\n\
				if ('nr' == call.def.requestType){\n\
					jws.t.conn.sendToken(call.token, {});\n\
				} else {\n\
					jws.t.conn.sendToken(call.token, call);\n\
				}\n\
		\n\
				waitsFor(function() {\n\
					return call.isReady();\n\
				}, 'calling: " + t.type + " ...', 10000);\n\
		\n\
				runs(function () {\n\
					if (jws.t.helpers.has(call.def.type) \n\
						&& typeof jws.t.helpers.get(call.def.type)['validate'] == 'function'){\n\
						jws.t.helpers.get(call.def.type).validate(call.response);\n\
					} else if ('wr' == call.def.requestType){\n\
						expect(call.response.code).toEqual(call.def.responseCode);\n\
						var end = call.def.outArguments.length;\n\
						for (var j = 0; j < end; j++){\n\
							if (call.def.outArguments[j].optional \n\
								&& !(call.response[call.def.outArguments[j].name])){\n\
								continue;\n\
							}else {\n\
								if (call.def.outArguments[j].testValue){\n\
									var expectedValue = parseJSON(call.def.outArguments[j].testValue);\n\
									expect(call.response[call.def.outArguments[j].name]).\n\
									toEqual(expectedValue);\n\
								} else {\n\
									expect(call.response[call.def.outArguments[j].name]).\n\
									toBeTypeOf(call.def.outArguments[j].type);\n\
								}\n\
							}\n\
						}\n\
					}\n\
				});\n\
			});");
		}	
	});
}

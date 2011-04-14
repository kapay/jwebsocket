
jws.t.helpers.set("t.broadcast", {
	ready: false,
	outText: "",
	inText: "",

	initialize: function(token){
		this.outText = token.text;
		jws.oop.addPlugIn(jws.jWebSocketTokenClient, this);
	},

	processToken: function (token) {
		if ("t.broadcast" == token.type){
			this.inText = token.text;
			this.ready = true;
		}
	},

	isReady: function(){
		return this.ready;
	},

	validate: function(){
		expect(this.outText).toEqual(this.inText);
	}

});


jws.t.helpers.set("t.complex_validation", {
	validate: function(response){
		expect(response.name).toEqual("JWebSocket");
		expect(response.version).toEqual(1.1);
		
		expect(response.team).toBeTypeOf("array");
		expect(response.team).toContain("one");
		
		expect(response.services).toBeTypeOf("object");
		expect(response.services.chat).toEqual(true);
		expect(response.services.facebook).toEqual(false);
	}
});

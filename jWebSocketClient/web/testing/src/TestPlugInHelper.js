
var NS_EXAMPLE = jws.NS_BASE  + ".plugins.test";

testing.helpers.set(NS_EXAMPLE, "broadcast", {
	ready: false,
	outText: "",
	inText: "",
	initialize: function(token){
		this.outText = token.text;
		auxConn.addPlugIn(this, "TestPlugIn_broadcast");
	},
	processToken: function (token) {
		if ("broadcast" == token.type && NS_EXAMPLE == token.ns){
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


testing.helpers.set(NS_EXAMPLE, "complex_validation", {
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


testing.helpers.set(NS_EXAMPLE, "s2c_performance", {
	ready: false,
	total: 0,
	received: 0,
	message: "",
	startTime: 0,
	endTime: 0,
	initialize: function(token){
		//Initializing test values
		this.total = token.count;
		token.count = this.total;
		this.message = token.message;

		//Registering as plug-in in other connection
		auxConn.addPlugIn(this, "TestPlugIn_s2c_performance");
		
		this.startTime = new Date().getTime(); //Time stats goes in the client ;)
	},
	processToken: function (token) {
		if ("s2c_performance" == token.type && NS_EXAMPLE == token.ns){
			if (token.data == this.message){ //Ensure is the same value
				this.received++;
			}
			
			if (this.received == this.total){ //All messages received
				this.ready = true; //The helper has finished
				this.endTime = new Date().getTime();
			}
		}
	},
	isReady: function(){
		return this.ready;
	},
	validate: function(){
		
		//Ensure that all the messages has been received
		expect(this.received).toEqual(this.total);
		
		console.log(NS_EXAMPLE + ": " + "s2c_performance (" + this.total + 
			" calls in " + (this.endTime - this.startTime) + " ms)");
	}
});

describe("jWebSocket Client", function() {

  var jwcJSON;

  beforeEach(function() {
     jwcJSON = new jWebSocketJSON();
  });

  it("should be defined", function() {
	    expect(jwcJSON).toBeDefined();
  });

});
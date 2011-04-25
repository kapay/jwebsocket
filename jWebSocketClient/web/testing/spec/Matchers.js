beforeEach(function() {
	this.addMatchers({
		toBeTypeOf: function(type) {
			return type == jws.tools.getType(this.actual);
		}
	})
});
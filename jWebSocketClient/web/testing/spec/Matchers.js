beforeEach(function() {
	this.addMatchers({
		toBeTypeOf: function(type) {
			var value = this.actual;
			var t = typeof value;
	
			if ("number" == t){
				if((parseFloat(value) == parseInt(value))){
					t = "integer";
				} else {
					t = "double";
				}
			} else if (Object.prototype.toString.call(value) === "[object Array]") {
				t = "array";
			} else if (value === null) {
				t = "null";
			}
			
			return type == t;
		}
	})
});
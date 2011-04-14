
function ValueGenerator(){
	
	/**
	 * @param length integer
	 * @return The generated string 
	 */
	this.getString = function(length){
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
	}
	
	/**
	 * @param min integer 
	 * @param max integer 
	 * @return The random integer value in the min - max interval.
	 */
	this.getInteger = function (min, max){ 
		min = (min) ? min : 0;
		max = (max) ? max : 1000;
		
		var interval = max - min; 
		number = Math.random() * interval; 
		number = Math.round(number) 
		return parseInt(min) + number; 
	} 
	
	/**
	 * @return The random boolean value
	 */
	this.getBoolean = function(){
		return (this.getInteger(0, 1)) ? true : false
	}
	
	/**
	 * @return The random double value
	 */
	this.getDouble = function (){
		return Math.random() * this.getInteger();
	}
	
	/**
	 * @return The random number value
	 */
	this.getNumber = function (){
		return (this.getInteger(0, 1)) ? this.getInteger() : this.getDouble();
	}
}

/**
 * @param paramsDefinition Array The request params definiton
 * @return Object The request arguments 
 */
function parseParams(paramsDefinition){
	var params = {};
	var end = paramsDefinition.length;
	
	var value;
	for (var i = 0; i < end; i++){
		if (paramsDefinition[i].testValue){
			value = parseJSON(paramsDefinition[i].testValue);
		} else if (paramsDefinition[i].optional){
			//Do Nothing!
		} else {
			switch (paramsDefinition[i].type) {
				case "string":
					value = jws.t.values.getString();
					break;
				case "integer":
					value = jws.t.values.getInteger();
					break;
				case "double":
					value = jws.t.values.getDouble();
					break;
				case "boolean":
					value = jws.t.values.getBoolean();
					break;
				case "number":
					value = jws.t.values.getNumber();
					break;
				default:
					break;
			}
		}
		
		//Setting the argument value
		params[paramsDefinition[i].name] = value;
	}
	
	return params;
}

function parseJSON(testValue){
	eval("var obj = {value: "+ testValue + "}");
	return obj.value;
}
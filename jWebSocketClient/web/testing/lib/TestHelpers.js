/**
 * Component used to validate custom tests where 
 * the runtime scenario and the validation is complex.
 */
function TestHelper (){
	/**
	 * Initialize the test context.
	 *
	 * @param token The token used for the test call
	 */
	this.initialize = function (token) {}
	
	/**
	 * @return TRUE if the helper is ready to be validate it, FALSE otherwise
	 */
	this.isReady = function () {}
	
	/**
	 * Validate the server response
	 * 
	 * @param response (null if requestType equals to nr) The response token to validate it
	 */
	this.validate = function (response) {}
}

/**
 * The test helpers manager
 */
function TestHelperManager (){
	this.helpers = {};
	
	/**
	 * Register a test helper for a custom token
	 *
	 * @param tokenType string
	 * @param helper TestHelper 
	 */
	this.set = function (tokenType, helper){
		this.helpers[tokenType] = helper;
	}
	
	/**
	 * @param tokenType string
	 * @return The test helper for the custom token type
	 */
	this.get = function(tokenType){
		return this.helpers[tokenType];
	}
	
	/**
	 * @param tokenType string
	 * @return TRUE if the the token type has a helper assigned, FALSE otherwise
	 */
	this.has = function (tokenType) {
		return (null != this.helpers[tokenType]);
	}
	
	/**
	 * Remove a test helper using a giving token type
	 * 
	 * @param tokenType string
	 */
	this.remove = function (tokenType){
		delete (this.helpers[tokenType]);
	}
}
/**
 * Client representation of the server-side InterfacePlugIn
 */
function InterfacePlugIn(conn){
	this.conn = conn;
	
	this.hasPlugIn = function (id, callback){
		var token = {};
		token.type = "server.has.plugin";
		token.plugin_id = id;
				
		this.conn.sendToken(token, {
			callback: callback,
			OnResponse: function (response){ 
				this.callback(response);
			}
		});
	}
			
	this.plugInAPI = function (id, callback){
		var token = {};
		token.type = "server.export.plugin.api";
		token.plugin_id = id;
				
		this.conn.sendToken(token, {
			callback: callback,
			OnResponse: function (response){ 
				this.callback(response);
			}
		});
	}
			
	this.supportToken = function(id, callback){
		var token = {};
		token.type = "server.support.token";
		token.token_type = id;
				
		this.conn.sendToken(token, {
			callback: callback,
			OnResponse: function (response){ 
				this.callback(response);
			}
		});
	}
			
	this.serverAPI = function(callback){
		var token = {};
		token.type = "server.export.api";
				
		this.conn.sendToken(token, {
			callback: callback,
			OnResponse: function (response){ 
				this.callback(response);
			}
		});
	}
			
	this.plugInsIds = function(callback){
		var token = {};
		token.type = "server.export.plugin.ids";
				
		this.conn.sendToken(token, {
			callback: callback,
			OnResponse: function (response){ 
				this.callback(response);
			}
		});
	}    
}


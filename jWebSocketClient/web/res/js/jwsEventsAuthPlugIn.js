
jws.BasePlugIn = {
  events: []

  ,notify: function(aEventName, aOptions){
    //Token creation
    var lToken = {}

    if (aOptions.args){
      Ext.apply(lToken, aOptions.args)
      delete aOptions.args
    }
    lToken.type      = aEventName
    lToken.ns        = this.ns
    lToken._IS_EM_   = true
    
    //Server notification of the token
    if (this.client.isConnected())
      return this.client.sendToken(lToken, aOptions)
    else
      throw "The client is not connected!"
  }

  ,processToken: function(aToken) {
    log("Incoming token type: " + Ext.encode(aToken))
  }

  ,initialize: function(){
    jws.oop.addPlugIn( jws.jWebSocketTokenClient, this)
  }
}

jws.EventsAuthPlugIn = function EventsAuthPlugIn(aOptions){
  Ext.apply(this , aOptions)
}
jws.EventsAuthPlugIn.prototype  = jws.BasePlugIn


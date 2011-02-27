//  <JasobNoObfs>
//	---------------------------------------------------------------------------
//	jWebSocket Client (uses jWebSocket Server)
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
//  </JasobNoObfs>

// ## :#file:*:jWebSocket.js
// ## :#d:en:Implements the jWebSocket Web Client.

//:package:*:jws
//:class:*:jws
//:ancestor:*:-
//:d:en:Implements the basic "jws" name space for the jWebSocket client
//:d:en:including various utility methods.
var jws = {

	//:const:*:NS_BASE:String:org.jwebsocket
	//:d:en:Base namespace
	NS_BASE: "org.jwebsocket",
	NS_SYSTEM: "org.jwebsocket.plugins.system",
	
	MSG_WS_NOT_SUPPORTED:
		"Unfortunately your browser does neither natively support WebSockets\n" +
		"nor you have the Adobe Flash-PlugIn 9+ installed.",

	// some namespace global constants
	
	//:const:*:CUR_TOKEN_ID:Integer:0
	//:d:en:Current token id, incremented per token exchange to assign results.
	CUR_TOKEN_ID: 0,
	//:const:*:JWS_SERVER_SCHEMA:String:ws
	//:d:en:Default schema, [tt]ws[/tt] for un-secured WebSocket-Connections.
	JWS_SERVER_SCHEMA: "ws",
	//:const:*:JWS_SERVER_SSL_SCHEMA:String:ws
	//:d:en:Default schema, [tt]wss[/tt] for secured WebSocket-Connections.
	JWS_SERVER_SSL_SCHEMA: "wss",
	//:const:*:JWS_SERVER_HOST:String:[hostname|localhost]
	//:d:en:Default hostname of current webbite or [tt]localhost[/tt] if no hostname can be detected.
	JWS_SERVER_HOST: ( self.location.hostname ? self.location.hostname : "localhost" ),
	//:const:*:JWS_SERVER_PORT:Integer:8787
	//:d:en:Default port number, 8787 for stand-alone un-secured servers, _
	//:d:en:80 for Jetty or Glassfish un-secured servers.
	JWS_SERVER_PORT: 8787,
	//:const:*:JWS_SERVER_SSL_PORT:Integer:9797
	//:d:en:Default port number, 9797 for stand-alone SSL secured servers, _
	//:d:en:443 for Jetty or Glassfish SSL secured servers.
	JWS_SERVER_SSL_PORT: 9797,
	//:const:*:JWS_SERVER_CONTEXT:String:jWebSocket
	//:d:en:Default application context in web application servers or servlet containers like Jetty or GlassFish.
	JWS_SERVER_CONTEXT: "/jWebSocket",
	//:const:*:JWS_SERVER_SERVLET:String:jWebSocket
	//:d:en:Default servlet in web application servers or servlet containers like Jetty or GlassFish.
	JWS_SERVER_SERVLET: "/jWebSocket",
	//:const:*:JWS_SERVER_URL:String:ws://[hostname]/jWebSocket/jWebSocket:8787
	//:d:en:Current token id, incremented per token exchange to assign results.
	//:@deprecated:en:Use [tt]getDefaultServerURL()[/tt] instead.
	JWS_SERVER_URL:
		"ws://" + ( self.location.hostname ? self.location.hostname : "localhost" ) + ":8787/jWebSocket/jWebSocket",

	JWS_FLASHBRIDGE: null,

	//:const:*:CONNECTING:Integer:0
	//:d:en:The connection has not yet been established.
	CONNECTING: 0,
	//:const:*:OPEN:Integer:1
	//:d:en:The WebSocket connection is established and communication is possible.
	OPEN: 1,
	//:const:*:CLOSING:Integer:2
	//:d:en:The connection is going through the closing handshake.
	CLOSING: 2,
	//:const:*:CLOSED:Integer:3
	//:d:en:The connection has been closed or could not be opened.
	CLOSED: 3,

	//:const:*:WS_SUBPROT_JSON:String:jwebsocket.org/json
	//:d:en:jWebSocket sub protocol JSON
	WS_SUBPROT_JSON: "jwebsocket.org/json",
	//:const:*:WS_SUBPROT_XML:String:jwebsocket.org/xml
	//:d:en:jWebSocket sub protocol XML
	WS_SUBPROT_XML: "jwebsocket.org/xml",
	//:const:*:WS_SUBPROT_CSV:String:jwebsocket.org/csv
	//:d:en:jWebSocket sub protocol CSV
	WS_SUBPROT_CSV: "jwebsocket.org/csv",
	//:const:*:WS_SUBPROT_CUSTOM:String:jwebsocket.org/custom
	//:d:en:jWebSocket sub protocol Custom
	WS_SUBPROT_CUSTOM: "jwebsocket.org/custom",

	//:const:*:SCOPE_PRIVATE:String:private
	//:d:en:private scope, only authenticated user can read and write his personal items
	SCOPE_PRIVATE: "private",
	//:const:*:SCOPE_PUBLIC:String:public
	//:d:en:public scope, everybody can read and write items from this scope
	SCOPE_PUBLIC: "public",

	//:const:*:DEF_RESP_TIMEOUT:integer:30000
	//:d:en:Default timeout in milliseconds for waiting on asynchronous responses.
	//:d:en:An individual timeout can be passed per request.
	DEF_RESP_TIMEOUT: 30000,

	//:m:*:$
	//:d:en:Convenience replacement for [tt]document.getElementById()[/tt]. _
	//:d:en:Returns the first HTML element with the given id or [tt]null[/tt] _
	//:d:en:if the element could not be found.
	//:a:en::aId:String:id of the HTML element to be returned.
	//:r:*:::void:none
	$: function( aId ) {
		return document.getElementById( aId );
	},
	
	//:m:*:getDefaultServerURL
	//:d:en:Returns the default URL to the un-secured jWebSocket Server. This is a convenience _
	//:d:en:method used in all jWebSocket demo dialogs. In case of changes to the _
	//:d:en:server URL you only need to change to above JWS_SERVER_xxx constants.
	//:a:en::voide::
	//:r:*:::void:Default jWebSocket server URL consisting of schema://host:port/context/servlet
	getDefaultServerURL: function() {
		var lURL =  
			jws.JWS_SERVER_SCHEMA + "://"
			+ jws.JWS_SERVER_HOST + ":" +
			+ jws.JWS_SERVER_PORT;
			
		if( jws.JWS_SERVER_CONTEXT && jws.JWS_SERVER_CONTEXT.length > 0 ) {
			lURL += jws.JWS_SERVER_CONTEXT;
			
			if( jws.JWS_SERVER_SERVLET && jws.JWS_SERVER_SERVLET.length > 0 ) {
				lURL += jws.JWS_SERVER_SERVLET;
			}
		}
		return lURL;
	},

	//:m:*:getDefaultSSLServerURL
	//:d:en:Returns the default URL to the secured jWebSocket Server. This is a convenience _
	//:d:en:method used in all jWebSocket demo dialogs. In case of changes to the _
	//:d:en:server URL you only need to change to above JWS_SERVER_xxx constants.
	//:a:en::voide::
	//:r:*:::void:Default jWebSocket server URL consisting of schema://host:port/context/servlet
	getDefaultSSLServerURL: function() {
		var lURL =
			jws.JWS_SERVER_SSL_SCHEMA + "://"
			+ jws.JWS_SERVER_HOST + ":" +
			+ jws.JWS_SERVER_SSL_PORT;

		if( jws.JWS_SERVER_CONTEXT && jws.JWS_SERVER_CONTEXT.length > 0 ) {
			lURL += jws.JWS_SERVER_CONTEXT;

			if( jws.JWS_SERVER_SERVLET && jws.JWS_SERVER_SERVLET.length > 0 ) {
				lURL += jws.JWS_SERVER_SERVLET;
			}
		}
		return lURL;
	},

	//:m:*:browserSupportsWebSockets
	//:d:en:checks if the browser or one of its plug-ins like flash or chrome _
	//:d:en:do support web sockets to be used by an application.
	//:a:en::::none
	//:r:*:::boolean:true if the browser or one of its plug-ins support websockets, otherwise false.
	browserSupportsWebSockets: function() {
		return( 
			window.WebSocket !== null && window.WebSocket !== undefined
		);
	},

	//:m:*:browserSupportsNativeWebSockets
	//:d:en:checks if the browser natively supports web sockets, no plug-ins
	//:d:en:are considered. Caution! This is a public field not a function!
	//:a:en::::none
	//:r:*:::boolean:true if the browser natively support websockets, otherwise false.
	browserSupportsNativeWebSockets: (function() {
		return(
			window.WebSocket !== null && window.WebSocket !== undefined
		);
	})(),

	//:m:*:browserSupportsJSON
	//:d:en:checks if the browser natively or by JSON lib does support JSON.
	//:a:en::::none
	//:r:*:::boolean:true if the browser or one of its plug-ins support JSON, otherwise false.
	browserSupportsJSON: function() {
		return(
			window.JSON !== null && window.JSON !== undefined
		);
	},

	//:m:*:browserSupportsNativeJSON
	//:d:en:checks if the browser natively supports JSON, no plug-ins
	//:d:en:are considered. Caution! This is a public field not a function!
	//:a:en::::none
	//:r:*:::boolean:true if the browser natively support websockets, otherwise false.
	browserSupportsNativeJSON: (function() {
		return(
			window.JSON !== null && window.JSON !== undefined
		);
	})(),

	//:m:*:isIE
	//:d:en:checks if the browser is Internet Explorer. _
	//:d:en:This is needed to switch to IE specific event model.
	//:a:en::::none
	//:r:*:::boolean:true if the browser is IE, otherwise false.
	isIE: (function() {
		var lUserAgent = navigator.userAgent;
		var lIsIE = lUserAgent.indexOf( "MSIE" );
		return( lIsIE >= 0 );
	})()

};


//:package:*:jws.events
//:class:*:jws.events
//:ancestor:*:-
//:d:en:Implements event abstraction for Internet Explorer.
jws.events = {

	//:m:*:addEventListener
	//:d:en:Adds a listener (callback) to an event in a cross-browser compatible way.
	//:a:en::aElement:Node:Source element that fires events.
	//:a:en::aEvent:String:Name of the event as a string.
	//:a:en::aListener:Function:The listener function which is called in case of the event.
	//:r:*:::void:none
	addEventListener : (
		jws.isIE ?
			function( aElement, aEvent, aListener ) {
				aElement.attachEvent( "on" + aEvent, aListener);
			}
		:
			function( aElement, aEvent, aListener ) {
				aElement.addEventListener( aEvent, aListener, false );
			}
	),

	// :d:en:Removes a listener (callback) from an event in a cross-browser compatible way.
	// :a:en::aElement:Node:Source element that fires events.
	// :a:en::aEvent:String:Name of the event as a string.
	// :a:en::aListener:Function:The listener function which is called in case of the event.

	//:m:*:getTarget
	//:d:en:Returns the element which originally fired the event in a cross-browser compatible way.
	//:r:*:::Node:Element that originally fired the event.
	getTarget : (
		jws.isIE ?
			function( aEvent ) {
				return aEvent.srcElement;
			}
		:
			function( aEvent ) {
				return aEvent.target;
			}
	),
	
	preventDefault : (
		jws.isIE ?
			function( aEvent ) {
				aEvent = window.event;
				if( aEvent ) {
					aEvent.returnValue = false;
				}
			}
		:
			function( aEvent ) {
				return aEvent.preventDefault();
			}
	)

};

//:package:*:jws.tools
//:class:*:jws.tools
//:ancestor:*:-
//:d:en:Implements some required JavaScript tools.
jws.tools = {

	//:m:*:zerofill
	//:d:en:Fills up an integer value with the given number of zero characters
	//:d:en:to support a date time exchange according to ISO 8601
	//:a:en::aInt:Number:Number to be formatted.
	//:a:en::aDigits:Number:Nu,ber of digits for the result.
	//:r:*:::String:String with the exact number of digits filled with 0.
	zerofill : function(aInt, aDigits) {
		var lRes = aInt.toFixed(0);
		if( lRes.length > aDigits ) {
			lRes = lRes.substring( )
		} else {
			while( lRes.length < aDigits ) {
				lRes = "0" + lRes;
			}
		}
        return lRes;
    },

	date2ISO: function( aDate ) {
		// JavaScript returns negative values for +GMT
		var lTZO = -aDate.getTimezoneOffset();
		var lAbsTZO = Math.abs( lTZO );
		var lRes =
			aDate.getUTCFullYear()
			+ this.zerofill( aDate.getUTCMonth() + 1, 2 )
			+ this.zerofill( aDate.getUTCDate(), 2 )
			+ this.zerofill( aDate.getUTCHours(), 2 )
			+ this.zerofill( aDate.getUTCMinutes(), 2 )
			+ this.zerofill( aDate.getUTCSeconds(), 2 )
			+ this.zerofill( aDate.getUTCMilliseconds(), 2 )
			+ ( lTZO >= 0 ? "+" : "-" )
			+ this.zerofill( lAbsTZO / 60, 2 )
			+ this.zerofill( lAbsTZO % 60, 2 )
			// trailing Z means it's UTC
			+ "Z";
		return lRes;
	},

	ISO2Date: function( aISO, aTimezone ) {
		var lDate = new Date();
		lDate.setUTCFullYear( aISO.substr( 0, 4 ) );
		lDate.setUTCMonth( aISO.substr( 4, 2 ) - 1 );
		lDate.setUTCDate( aISO.substr( 6, 2 ) );
		lDate.setUTCHours( aISO.substr( 8, 2 ) );
		lDate.setUTCMinutes( aISO.substr( 10, 2 ) );
		lDate.setUTCSeconds( aISO.substr( 12, 2 ) );
		lDate.setUTCMilliseconds( aISO.substr( 14, 3 ) );
		return lDate;
	}

};


if( !jws.browserSupportsNativeWebSockets ) {

	//	<JasobNoObfs>
	// --- swfobject.js ---
	// SWFObject v2.2 <http://code.google.com/p/swfobject/> 
	// is released under the MIT License <http://www.opensource.org/licenses/mit-license.php> 
	var swfobject=function(){var D="undefined",r="object",S="Shockwave Flash",W="ShockwaveFlash.ShockwaveFlash",q="application/x-shockwave-flash",R="SWFObjectExprInst",x="onreadystatechange",O=window,j=document,t=navigator,T=false,U=[h],o=[],N=[],I=[],l,Q,E,B,J=false,a=false,n,G,m=true,M=function(){var aa=typeof j.getElementById!=D&&typeof j.getElementsByTagName!=D&&typeof j.createElement!=D,ah=t.userAgent.toLowerCase(),Y=t.platform.toLowerCase(),ae=Y?/win/.test(Y):/win/.test(ah),ac=Y?/mac/.test(Y):/mac/.test(ah),af=/webkit/.test(ah)?parseFloat(ah.replace(/^.*webkit\/(\d+(\.\d+)?).*$/,"$1")):false,X=!+"\v1",ag=[0,0,0],ab=null;if(typeof t.plugins!=D&&typeof t.plugins[S]==r){ab=t.plugins[S].description;if(ab&&!(typeof t.mimeTypes!=D&&t.mimeTypes[q]&&!t.mimeTypes[q].enabledPlugin)){T=true;X=false;ab=ab.replace(/^.*\s+(\S+\s+\S+$)/,"$1");ag[0]=parseInt(ab.replace(/^(.*)\..*$/,"$1"),10);ag[1]=parseInt(ab.replace(/^.*\.(.*)\s.*$/,"$1"),10);ag[2]=/[a-zA-Z]/.test(ab)?parseInt(ab.replace(/^.*[a-zA-Z]+(.*)$/,"$1"),10):0}}else{if(typeof O.ActiveXObject!=D){try{var ad=new ActiveXObject(W);if(ad){ab=ad.GetVariable("$version");if(ab){X=true;ab=ab.split(" ")[1].split(",");ag=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}}catch(Z){}}}return{w3:aa,pv:ag,wk:af,ie:X,win:ae,mac:ac}}(),k=function(){if(!M.w3){return}if((typeof j.readyState!=D&&j.readyState=="complete")||(typeof j.readyState==D&&(j.getElementsByTagName("body")[0]||j.body))){f()}if(!J){if(typeof j.addEventListener!=D){j.addEventListener("DOMContentLoaded",f,false)}if(M.ie&&M.win){j.attachEvent(x,function(){if(j.readyState=="complete"){j.detachEvent(x,arguments.callee);f()}});if(O==top){(function(){if(J){return}try{j.documentElement.doScroll("left")}catch(X){setTimeout(arguments.callee,0);return}f()})()}}if(M.wk){(function(){if(J){return}if(!/loaded|complete/.test(j.readyState)){setTimeout(arguments.callee,0);return}f()})()}s(f)}}();function f(){if(J){return}try{var Z=j.getElementsByTagName("body")[0].appendChild(C("span"));Z.parentNode.removeChild(Z)}catch(aa){return}J=true;var X=U.length;for(var Y=0;Y<X;Y++){U[Y]()}}function K(X){if(J){X()}else{U[U.length]=X}}function s(Y){if(typeof O.addEventListener!=D){O.addEventListener("load",Y,false)}else{if(typeof j.addEventListener!=D){j.addEventListener("load",Y,false)}else{if(typeof O.attachEvent!=D){i(O,"onload",Y)}else{if(typeof O.onload=="function"){var X=O.onload;O.onload=function(){X();Y()}}else{O.onload=Y}}}}}function h(){if(T){V()}else{H()}}function V(){var X=j.getElementsByTagName("body")[0];var aa=C(r);aa.setAttribute("type",q);var Z=X.appendChild(aa);if(Z){var Y=0;(function(){if(typeof Z.GetVariable!=D){var ab=Z.GetVariable("$version");if(ab){ab=ab.split(" ")[1].split(",");M.pv=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}else{if(Y<10){Y++;setTimeout(arguments.callee,10);return}}X.removeChild(aa);Z=null;H()})()}else{H()}}function H(){var ag=o.length;if(ag>0){for(var af=0;af<ag;af++){var Y=o[af].id;var ab=o[af].callbackFn;var aa={success:false,id:Y};if(M.pv[0]>0){var ae=c(Y);if(ae){if(F(o[af].swfVersion)&&!(M.wk&&M.wk<312)){w(Y,true);if(ab){aa.success=true;aa.ref=z(Y);ab(aa)}}else{if(o[af].expressInstall&&A()){var ai={};ai.data=o[af].expressInstall;ai.width=ae.getAttribute("width")||"0";ai.height=ae.getAttribute("height")||"0";if(ae.getAttribute("class")){ai.styleclass=ae.getAttribute("class")}if(ae.getAttribute("align")){ai.align=ae.getAttribute("align")}var ah={};var X=ae.getElementsByTagName("param");var ac=X.length;for(var ad=0;ad<ac;ad++){if(X[ad].getAttribute("name").toLowerCase()!="movie"){ah[X[ad].getAttribute("name")]=X[ad].getAttribute("value")}}P(ai,ah,Y,ab)}else{p(ae);if(ab){ab(aa)}}}}}else{w(Y,true);if(ab){var Z=z(Y);if(Z&&typeof Z.SetVariable!=D){aa.success=true;aa.ref=Z}ab(aa)}}}}}function z(aa){var X=null;var Y=c(aa);if(Y&&Y.nodeName=="OBJECT"){if(typeof Y.SetVariable!=D){X=Y}else{var Z=Y.getElementsByTagName(r)[0];if(Z){X=Z}}}return X}function A(){return !a&&F("6.0.65")&&(M.win||M.mac)&&!(M.wk&&M.wk<312)}function P(aa,ab,X,Z){a=true;E=Z||null;B={success:false,id:X};var ae=c(X);if(ae){if(ae.nodeName=="OBJECT"){l=g(ae);Q=null}else{l=ae;Q=X}aa.id=R;if(typeof aa.width==D||(!/%$/.test(aa.width)&&parseInt(aa.width,10)<310)){aa.width="310"}if(typeof aa.height==D||(!/%$/.test(aa.height)&&parseInt(aa.height,10)<137)){aa.height="137"}j.title=j.title.slice(0,47)+" - Flash Player Installation";var ad=M.ie&&M.win?"ActiveX":"PlugIn",ac="MMredirectURL="+O.location.toString().replace(/&/g,"%26")+"&MMplayerType="+ad+"&MMdoctitle="+j.title;if(typeof ab.flashvars!=D){ab.flashvars+="&"+ac}else{ab.flashvars=ac}if(M.ie&&M.win&&ae.readyState!=4){var Y=C("div");X+="SWFObjectNew";Y.setAttribute("id",X);ae.parentNode.insertBefore(Y,ae);ae.style.display="none";(function(){if(ae.readyState==4){ae.parentNode.removeChild(ae)}else{setTimeout(arguments.callee,10)}})()}u(aa,ab,X)}}function p(Y){if(M.ie&&M.win&&Y.readyState!=4){var X=C("div");Y.parentNode.insertBefore(X,Y);X.parentNode.replaceChild(g(Y),X);Y.style.display="none";(function(){if(Y.readyState==4){Y.parentNode.removeChild(Y)}else{setTimeout(arguments.callee,10)}})()}else{Y.parentNode.replaceChild(g(Y),Y)}}function g(ab){var aa=C("div");if(M.win&&M.ie){aa.innerHTML=ab.innerHTML}else{var Y=ab.getElementsByTagName(r)[0];if(Y){var ad=Y.childNodes;if(ad){var X=ad.length;for(var Z=0;Z<X;Z++){if(!(ad[Z].nodeType==1&&ad[Z].nodeName=="PARAM")&&!(ad[Z].nodeType==8)){aa.appendChild(ad[Z].cloneNode(true))}}}}}return aa}function u(ai,ag,Y){var X,aa=c(Y);if(M.wk&&M.wk<312){return X}if(aa){if(typeof ai.id==D){ai.id=Y}if(M.ie&&M.win){var ah="";for(var ae in ai){if(ai[ae]!=Object.prototype[ae]){if(ae.toLowerCase()=="data"){ag.movie=ai[ae]}else{if(ae.toLowerCase()=="styleclass"){ah+=' class="'+ai[ae]+'"'}else{if(ae.toLowerCase()!="classid"){ah+=" "+ae+'="'+ai[ae]+'"'}}}}}var af="";for(var ad in ag){if(ag[ad]!=Object.prototype[ad]){af+='<param name="'+ad+'" value="'+ag[ad]+'" />'}}aa.outerHTML='<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'+ah+">"+af+"</object>";N[N.length]=ai.id;X=c(ai.id)}else{var Z=C(r);Z.setAttribute("type",q);for(var ac in ai){if(ai[ac]!=Object.prototype[ac]){if(ac.toLowerCase()=="styleclass"){Z.setAttribute("class",ai[ac])}else{if(ac.toLowerCase()!="classid"){Z.setAttribute(ac,ai[ac])}}}}for(var ab in ag){if(ag[ab]!=Object.prototype[ab]&&ab.toLowerCase()!="movie"){e(Z,ab,ag[ab])}}aa.parentNode.replaceChild(Z,aa);X=Z}}return X}function e(Z,X,Y){var aa=C("param");aa.setAttribute("name",X);aa.setAttribute("value",Y);Z.appendChild(aa)}function y(Y){var X=c(Y);if(X&&X.nodeName=="OBJECT"){if(M.ie&&M.win){X.style.display="none";(function(){if(X.readyState==4){b(Y)}else{setTimeout(arguments.callee,10)}})()}else{X.parentNode.removeChild(X)}}}function b(Z){var Y=c(Z);if(Y){for(var X in Y){if(typeof Y[X]=="function"){Y[X]=null}}Y.parentNode.removeChild(Y)}}function c(Z){var X=null;try{X=j.getElementById(Z)}catch(Y){}return X}function C(X){return j.createElement(X)}function i(Z,X,Y){Z.attachEvent(X,Y);I[I.length]=[Z,X,Y]}function F(Z){var Y=M.pv,X=Z.split(".");X[0]=parseInt(X[0],10);X[1]=parseInt(X[1],10)||0;X[2]=parseInt(X[2],10)||0;return(Y[0]>X[0]||(Y[0]==X[0]&&Y[1]>X[1])||(Y[0]==X[0]&&Y[1]==X[1]&&Y[2]>=X[2]))?true:false}function v(ac,Y,ad,ab){if(M.ie&&M.mac){return}var aa=j.getElementsByTagName("head")[0];if(!aa){return}var X=(ad&&typeof ad=="string")?ad:"screen";if(ab){n=null;G=null}if(!n||G!=X){var Z=C("style");Z.setAttribute("type","text/css");Z.setAttribute("media",X);n=aa.appendChild(Z);if(M.ie&&M.win&&typeof j.styleSheets!=D&&j.styleSheets.length>0){n=j.styleSheets[j.styleSheets.length-1]}G=X}if(M.ie&&M.win){if(n&&typeof n.addRule==r){n.addRule(ac,Y)}}else{if(n&&typeof j.createTextNode!=D){n.appendChild(j.createTextNode(ac+" {"+Y+"}"))}}}function w(Z,X){if(!m){return}var Y=X?"visible":"hidden";if(J&&c(Z)){c(Z).style.visibility=Y}else{v("#"+Z,"visibility:"+Y)}}function L(Y){var Z=/[\\\"<>\.;]/;var X=Z.exec(Y)!=null;return X&&typeof encodeURIComponent!=D?encodeURIComponent(Y):Y}var d=function(){if(M.ie&&M.win){window.attachEvent("onunload",function(){var ac=I.length;for(var ab=0;ab<ac;ab++){I[ab][0].detachEvent(I[ab][1],I[ab][2])}var Z=N.length;for(var aa=0;aa<Z;aa++){y(N[aa])}for(var Y in M){M[Y]=null}M=null;for(var X in swfobject){swfobject[X]=null}swfobject=null})}}();return{registerObject:function(ab,X,aa,Z){if(M.w3&&ab&&X){var Y={};Y.id=ab;Y.swfVersion=X;Y.expressInstall=aa;Y.callbackFn=Z;o[o.length]=Y;w(ab,false)}else{if(Z){Z({success:false,id:ab})}}},getObjectById:function(X){if(M.w3){return z(X)}},embedSWF:function(ab,ah,ae,ag,Y,aa,Z,ad,af,ac){var X={success:false,id:ah};if(M.w3&&!(M.wk&&M.wk<312)&&ab&&ah&&ae&&ag&&Y){w(ah,false);K(function(){ae+="";ag+="";var aj={};if(af&&typeof af===r){for(var al in af){aj[al]=af[al]}}aj.data=ab;aj.width=ae;aj.height=ag;var am={};if(ad&&typeof ad===r){for(var ak in ad){am[ak]=ad[ak]}}if(Z&&typeof Z===r){for(var ai in Z){if(typeof am.flashvars!=D){am.flashvars+="&"+ai+"="+Z[ai]}else{am.flashvars=ai+"="+Z[ai]}}}if(F(Y)){var an=u(aj,am,ah);if(aj.id==ah){w(ah,true)}X.success=true;X.ref=an}else{if(aa&&A()){aj.data=aa;P(aj,am,ah,ac);return}else{w(ah,true)}}if(ac){ac(X)}})}else{if(ac){ac(X)}}},switchOffAutoHideShow:function(){m=false},ua:M,getFlashPlayerVersion:function(){return{major:M.pv[0],minor:M.pv[1],release:M.pv[2]}},hasFlashPlayerVersion:F,createSWF:function(Z,Y,X){if(M.w3){return u(Z,Y,X)}else{return undefined}},showExpressInstall:function(Z,aa,X,Y){if(M.w3&&A()){P(Z,aa,X,Y)}},removeSWF:function(X){if(M.w3){y(X)}},createCSS:function(aa,Z,Y,X){if(M.w3){v(aa,Z,Y,X)}},addDomLoadEvent:K,addLoadEvent:s,getQueryParamValue:function(aa){var Z=j.location.search||j.location.hash;if(Z){if(/\?/.test(Z)){Z=Z.split("?")[1]}if(aa==null){return L(Z)}var Y=Z.split("&");for(var X=0;X<Y.length;X++){if(Y[X].substring(0,Y[X].indexOf("="))==aa){return L(Y[X].substring((Y[X].indexOf("=")+1)))}}}return""},expressInstallCallback:function(){if(a){var X=c(R);if(X&&l){X.parentNode.replaceChild(l,X);if(Q){w(Q,true);if(M.ie&&M.win){l.style.display="block"}}if(E){E(B)}}a=false}}}}();
	//	</JasobNoObfs>

	// check if appropriate flash version is installed
	if( swfobject.hasFlashPlayerVersion( "9.0.0" ) ) {
	
		//	<JasobNoObfs>
		// --- FABridge.js ---
		// http://opensource.adobe.com/wiki/display/flexsdk/Flex+SDK
		// Copyright 2006 Adobe Systems Incorporated
		// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
		// to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
		// and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
		// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
		// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
		// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
		// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
		// OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
		// Full Sources codes provided in FABridge.js
		eval((function(x){var d="";var p=0;while(p<x.length){if(x.charAt(p)!="`")d+=x.charAt(p++);else{var l=x.charCodeAt(p+3)-28;if(l>4)d+=d.substr(d.length-x.charCodeAt(p+1)*96-x.charCodeAt(p+2)+3104-l,l);else d+="`";p+=4}}return d})("function FABridge(target,b` )!Name){this.` 2\"=` 9\";` .!remoteTypeCache={}` ,(Instanc` '1F`!0#` .*local` %2`!I\"ID=`!`$.next` %\"ID++` @\"name=`!p&` .#extL` r$ID=0;` [%i`!\\#s[` V%]=this` 8'dMap` 9\"`!<$` <#return ` '!}` G%TYPE_ASINSTANCE=1` a&` 4#FUNCTION=2` ,+J` 1&3` F,NONYMOUS=4`!r(itCallbacks={}` 0&userType` (*addToU` 2%`%3$(){for(var i=0;i<arguments.length;i++){` k.[` @%[i]]={typ`#r!:` ,(,enriched:false};}`!F(rgsToArray`!E&args){var result=[];`!P-`!N*` C\"[i]=args[i];}`$7#` 4\";};`'`%`%,$Factory(objID`']#fb_` 7$_id=` 6!`$z*`(?-__invokeJS`'1$`!v'f`&L\"`!P!0];var throughArgs` 3!.concat();` /'.shift()` M!`&Q\"`'o&extrac`'u#FromID(`!#\")`!d$` M\".`!T\"`'s%`!X!` D\",`!\"')`'3'addInitializa`)@\"`&:\"`$5&`(u&,c` 6#`\"G\"inst`!\\&`(q&` G&];if(inst!=undefined){` ^$.call` :!`\" $;}var ` 8$Li` r*`'r'` t,` F)=null`'5'` A5=` J)[];}` %(.push` q%)`&'(`%B&`*|#`#;$ed`#$'`# \"objects=doc`(?!.getEle`(J!ByTagName(\"` A\"\"`%<\"ol=` P#`'n$var activeO` i#[`\"T!ol>0`),ol`(?\"if(typeof`!<$[i].SetVariable!=\"`$-%\"){` r)[`! )`!?#]`!N$`),!}`$H!embed`!}=` A!`\"6#el=` O\"`\"*.E` g\"`\"5\"e`\".)j=0;j<el;j`\"-*` e\"[j`!x>` w\"`\"2#` '\"`\"/%` ]%`\"/$aol=`\"O0`!g\"e` 5$` Z)` 7!searchStr=\"`%@&=\"+`1Z'if(aol==1&&!ael||` &$ael==1`'/'attach`4A#`$2*0]`4F(;}else if(` ^\"&&!ao`'x(` W/`\"i#` U0`,f\"lash_found=`/F!`!r#>1`$,&k=0;k<aol;k++`'j\"params`#/*[k].childNodes`/T%l=0;l<` K\"`#<$l` [)=` 7\"[l`%M!` (!.nodeType`\"B!` -\"t`&B\".toLowerCase()==\"` 9!\"` >$n` /0`\"A!vars` >%value.indexOf(`$g%)>=0`#uBk`#D*`#<(true;break;}}if(` 1'){` 0$}if(!` /'`%T!`#j(m=0;m<ael;m`#&$` J!Var`#q$`$v#m].attributes.getNamedItem(`\"T\"Vars\")`#O!Valu`$!` 0%`\"1P`!0%`\"W)`\"2%`4?$`\"d!`1=&nex`29#ID=0;`0c.={}` +'dMap` %)refCount` P(`3$/`2+&id`#2\"`/9#D=id>>16`3H$` }*`0M#ID];`!)'`\"Q(` n'`!k#`\"S(`!%!new`\";#` ;#=new`! %` C1`!#%`1h)` X-`+s!`1`$s`2cKs`3'$`3z$`0^*` >%`)X$`0u!` -%[i]`4`\"`!D-);}delete`#A'`!75`#K'blockedMethods={toString:true,get` #\"se` \"#call` %!` Y'prototype={root:`$4%`\">$ this.deser`4Q\"(` ,!targe`1~!Root());},releaseAS`*7#` U4` U#` ?,(` Y'`)7!` 2$` ^&`+k!`2'(` ,!!=\"`3g\"\"` %`._\"`.w&ret=`!)/` z)` x\".fb_`&9$_id)`'_$ret;}},create`!H&class`'4\"`\"l@` Y\"` K');},makeID` j&token` h$` Y\"`(n$<<16)+` <!;},getPropertyFromAS` Z&objRef,prop`!X\"if(`*R->0){throw new Error(\"You are trying to`(K! recursively into the Flash Player which is not allowed. In most cases` P!JavaScript setTimeout `!r$, can be used as a workaround.\"`3n$`!{-++;retVal`${)`\"{#`\"x\"`\"d-` E)handle`\"_\"` 2\"`+K&` }%--`%E'Val;}},s`#|&In`#i7,`'4&`#(~`#(~`#[K`\"#In`#v.,` C!`(G&`#\"\"`#WccallASF`\"-#`$3&funcID,args`#1~`#1~`#_Pinvoke`\"s&`\"l$`$ +`\"~!`#Ai`1l\"`(<)ID,func`%J!`#2~`#2~`#oAargs`#K\"`#_+`$28`#7\"`\"1`#^b`!#\"Local`(8;`3T\"sult;var`\"m!`!6\"l` Q(Cache[` S\"];if` _!!=undefined){` ^\"`\"Z,func.apply(null`&q\"`3M(`&s\");}`!z%`!F!`2s!TypeFromNam`4C'objType`4>.remoteType`!h\"` >'];},`4Q\"Proxy`'&,t` g%var ` Q#`\"%\"`!8+(` B%;instanceFactory.prototype=` Z#`#K!` >$=new` $%` L#`!8\");`!s'I` ;#`!y%ID]=` O$`$$` '%`#!!`!~0`\"e0` g0;},addTypeDataTo` 7!` b&t` 2#`\"r\"new`\"s!new AS`!2!(this`#:!Data.n`\"t!var accessors=` 4%` *%;for(`\"w!=0;i<` /%.length;i++){`!t!add`4.$ToType(`!=#,` L%[i]);}var m`(o!`!&'` *#`! +` /#`! )`,;(blocked`)\\\"s[` H#[i]]=`'c(`!X$` B\"`!O+` K&);}}`&h1` D#.`&($]=` +#`$}$` '$`$=!`\"X*`$5(`3x%`$>\"c=` ($.charAt(0`$+\"setterNam`&q\"g` %&if(c>=\"a\"&&c<=\"z\"){` 4&=\"get\"+c.toUpperCase()+` |%substr(1);` w&=\"s` -C`.0\"` }-` B$` b.` 1%}ty[` 4&]=`\"i%val`$6#b`$j\"s`(=!`#9!InAS`'-!.fb_`(]$_id`#;%,val);};ty[`!K&` r'`(i*` z#`,b(` ,(`)U\"`!4!From` |;));}`%&\"`&&(`$})`&0\"`%&\"ty`&s#`!&X`4](`!H1` y&,`(7%argsToArray(argument`/<\"`,)\"`0J$`,(+`0U\"`&\"`!9\"`.;!;if`!4\"`(:\"`0z1==nul`$n$` ,8`\"['`\"5)` F$`2Q$`!l<;}`1F%` t<`\"F*ID`\"C*`+U!`2^!__`!L\"_id__`+<)` ,/`!0!makeID`\"w\"next`4c%ID++`0M#`4,3` a*`\"r\"`\")%` /.;},`%W%`!v&valu`+N#`4.\"={}`+6!t`.=!of ` =!`$\\!==\"number\"||t==\"string` &#boolean` (\"null` $!`\"W'` x#` i\"`*n! if`!:\"`2B%of `$B!` E%[]`/I+` K!`/I)` D\"[i]`#K\"`'z&` F!`.v#`!'$t==\"`\"T$\"` T$`.y!=`%f%TYPE_JSFUNCTION;` >#` j!` ~\"`%?)`#<#`!w8`3@\"` s8ASINSTANCE`!!*`\"I\"`)b*`.)#` R7NONYMOUS` Y/`%X%` 5\";},`+,'`%M&packedV`%M,`%L*` :'`%4H` L'`%b$` &)`%b.`#p!handleError`!R)`#o&` +'`%gG` K'`%|8`.2(` H'`&-/object\"){` n6newTypes`!$)`!\"!addTypeDataTo`*G!`!\"(` P%`!1\"` y$aRefID in`$'(.newRefs` r#create`&d!(` J\",` :/[` 5\"]);}`#C*`&B\"`&:+PRIMITIVE`#U%` F(`*;+` M<A`))%`%**`0},` \\)`),,` c>`) $` t-` :_`)0$`\"S8`)/-addRef`)1&obj`$L#target.incRef(obj`*Z+);},releas`)o'` N-` ;#` I6`(n'`0'-if(`/{(`*+&&&`,5\"indexOf(\"__FLASHERROR\")==0`+2\"my` t!Messag`,h$split(\"||\");if(`#=%refCount>0){` $---;}throw new `*b\"` p*[1]);`#X#`&R'{` %*}};`.j#`3-!`\"I!`3!\",typeName`#:#` 0\"=` 7\"`4%\"` :$`->!Name`!($this;` q%.proto`%_!{get`.-'rop` |\"` M'`!\"#`+V)` ,(`&w\"pertyFromAS` 8\"`$\\*,` r%);},s`!!0,`$n#` p(s` r&In` [;` Y#;},call` v&func` ;!arg`+g$` x#callASMethod` g1` N*`'|/` b*` 8\"` e!`'i1` @*` 8#` G$};"));
		 
		// --- web_socket.js (minified) ---
		// Copyright: Hiroshi Ichikawa <http://gimite.net/en/>
		// http://github.com/gimite/web-socket-js
		// http://www.lightsphere.com/dev/articles/socketpolicy.pl.html
		// License: New BSD License
		// Full Sources codes provided in web_socket.js
		eval((function(x){var d="";var p=0;while(p<x.length){if(x.charAt(p)!="`")d+=x.charAt(p++);else{var l=x.charCodeAt(p+3)-28;if(l>4)d+=d.substr(d.length-x.charCodeAt(p+1)*96-x.charCodeAt(p+2)+3104-l,l);else d+="`";p+=4}}return d})("(function(){if(window.WebSocket){return;}var console=` ?#` (#;if(!` %#){` <${log:` r'},error` &)};}if(!swfobject.hasFlashPlayerVersion(\"9.0.0\")` t%.` f!(\"` G! ` G\" is not installed.\");`!u$if(location.protocol==\"file:\"` f-WARNING: web-s`\"c!-js doesn't work in ` Y!///... URL unless you set `!W\"Security Settings properly. Open the page via Web server i.e. http:` }!\");}`$-%=`#<%url,`\")$,proxyHost` $\"Port,headers){var self=this;self.readyState=` r%.CONNECTING` ?\"bufferedAmount=0;setTimeout`%p(` W&__addTask` 3(` f!__create`\"u!`!QF;});},1);};` |&` U!type` h*`\"3c__f` i!`\"*(` -!.`!&\"` g3||nul` )#Port||0` $` 5\")` r).addEventListener(\"open\",`!|%fe){try`#9\"`$D'` Y)getR` 2%();if(` 9#timer){clearInterval` .*`(6!`*(#opera`$B%` ;!=set` P%`$].handleMessages();},500` o\"` >!onopen` I#` '\"();}}catch(`+$&`)3#e.toString());}}`\"n=close`\".~`#!+`\"'#`!8!`\"3%` )!`!Pim`#R\"`\"4'`\"2'`#i0` Qd` R!`#C/`\"QT` k!`# %`!O\"`\"=hstateChan`# )`$wQ`,0fe.getB` ()`#BI`,B2send`,E&data`3}!this`!W$){` )!`!r'` 2(`!l-`3H!` 6(||` P,`/T1){throw\"INVALID_STATE_ERR:`1L!` J\" conne`!h! ha`3f\"been established\";}var result`!^*send(encodeURIComponent`\"R\"`)?!` P\"<0){`4P\" true;}else`\"a\"`$(+` J\";` F#false;}`#U2`)W!`#b&`/c,if(!`%J(`!:$;}`*sQ`#X3LOSED||` $;`$*!`!2)`11%`+Z#`4@7` ~!`,.^tTimeout` ,)`3|!`#I2`)B,`#`&type,l` 0#,useCapture`'e!!(\"__events\"in this)`'k#` 0$={}`'P\"` e! ` ?#` 6%` ?,[type]=[];if(\"`!>$\"==typeof` X![\"on\"+` D!` I1.defaultH`.:!r`%]!` I';` U+`'^$create`\"b!` S#`*%!,type);}}` v0push(`\"v$`*o4remov` s\"`\"^~`#N.`&c$for(var i`\"I$` <\".length;i>-1;--i`!4!`!I$==` ?*`\"Q\"[i`#b3splice(i,1);break;}`&:3dispatch`\"q!`\"d&` i!`\">9row\"UNSPECIFIED_EVENT_TYP`-*!\"`\"e#` T!.`&86` E=`##&0,l`\"U+` s&]`#4%<l;++i`\"f,` ?'[i]`\"7#;if`!Y#cancelBubble){`#)$if(`-F!!==` A\"`$[\"Value&&`'}3`!H2`'{*`!C7` ;+`!b$`$F3__h` C!Messages`.h,ar`)/\"`-0%read` `\"Data();`#T';i<arr`#@%++` ^\"data=de`0}-arr[i]);try`3c%onm`!D\"` U\"e;if(window.`!\\#`&B!&&!` /#opera){e=docum`$&!`*S&(\"` I(\");e.init` )((\"`!*#\",`$F!,`$L!,data,null,null,`!'\"` '!)`2_#e={data:data};`+M\"`!u%(`+b!catch(e){console.error(e.toString())`(^5`,R0`$4&object`,k\"`4;$` 4%`4b!`#:\"`)O!new ` z%` k!;`%k\"ini` *\"`,V\"true,true)` 9#target`'5#currentT` .\"`!;\"`%5%key in`%&!){` K![key]=data` %!;}` L\"`+!*`&x\",arg`$W!s);};};`\"!$`!f+(){}` $*`#$'`);\"able=true`#I&` 32`)d\"=`%(!` 26pr`![!D`(l\"`(0(`'($`!-&`)L#`*7'` w#`$t(` q,stopPropagation` v(` t'`!f#`\"3!` R7`$e%`.V+TypeArg,can` d\"` &#`!r#Arg`!'#type`%'\"` L#`3[!`#O(` I)` 8\"timeStamp`&B!Dat`!W).CONNECTING=0` ,'OPEN=1` <(LOSING=2` %+ED=3` ('__tasks=[]` ()initialize`$6+` 9(swfLoc`#d!){`+8#WEB_SOCKET_SWF_LOCATION=` A3`1N\"` >:`*?,\"[` d%] set ` A3 to l`!!# of`'s&Main.swf\");`&8\";}var container`-*-lem`,u!div\");` ?%.id=\"w` n$C` /$\"` 7'style.posi`&j!\"absolute\";`#=+isFlashLite()`\"M!` U)left=\"0px` k.top` 3#`.\"\"` E2-10` D6` 5$`\"b!hold`\"C=` ?\"`\"S*`!|!` v(appendChild(` K\");` n%body` 5)` O%);swf`,v#embedSWF(`$m3,`!/,,\"1\",\"1\",\"9.0.0\"`1)\"{bridgeName:` H&\"},{hasPriority:`/*!allowScriptAccess:\"always\"}` f\"`*b&`(,!!e.su` D!`&j:`\"\"- failed\");}});FAB`!^!.addI`)5$`'6!Callback(`!n'`!;&){try{`%~(flash=` g%` L%.root()`*:)` G!.setCallerUrl(`(J$.href` 73Debug(!`)e/DEBUG)`1}%i=0;i<`+]-.length;++i)`!o)` 9![i]();`1P&`,8(}catch(e`#O9\"+e.toString()`#Z\"`-w(__addTask`,r&task`,o-`\"K!){task()`(w#`!z.push` Z\"`1M)`*&*`-w+`#2$navigator||` \"-.mimeTypes){`,A\" `2Y#var ` 5$=` =6['appli`$i\"/x-shockwave-`\"4!'];if(!` G$||` \"%.enabledPlugin||` \"3.filename`!P,` '#` 7;.m`$f!/`!H!lite/i)?true:` `#;`\"4#`'9%Log`#>&messag`%>'log(decodeURIComponent` ?%)` ^/Error` X7`&6\"` V;`1k2DISABLE_AUTO_INITIALIZ`2)#if(` K#addEventListener`3?%` ),(\"load\",`4/2,`#.!`&|$` [$ttach` a!(\"on` D8);}}})();"));
		//	</JasobNoObfs>

		// init flash bridge
		// use function to not polute the namespace with identifiers
		// get all scripts on the page to find jWebSocket.js path
		(function() {
			var lScripts = document.getElementsByTagName( "script" );
			for( var lIdx = 0, lCnt = lScripts.length; lIdx < lCnt; lIdx++ ) {
				var lScript = lScripts[ lIdx ];
				var lPath = lScript.src;
				if( !lPath ) {
					lPath = lScript.getAttribute( "src" );
				}
				if( lPath ) {
					var lPos = lPath.lastIndexOf( "jWebSocket" );
					if( lPos > 0 ) {
						jws.JWS_FLASHBRIDGE = lPath.substr( 0, lPos ) + "flash-bridge/WebSocketMain.swf";
						break;
					}
				}
			}
		})();

		if( jws.JWS_FLASHBRIDGE != null ) {
			WebSocket.__swfLocation = jws.JWS_FLASHBRIDGE;
		} else {
			WebSocket = null;
		}
	} else {
		// no Flash Player installed
		WebSocket = null;
	}

}

if( !jws.browserSupportsNativeJSON ) {
	// <JasobNoObfs>
	// Please refer to http://json.org/js
	if(!this.JSON){this.JSON={};}(function(){function f(n){return n<10?'0'+n:n;}if(typeof Date.prototype.toJSON!=='function'){Date.prototype.toJSON=function(key){return isFinite(this.valueOf())?this.getUTCFullYear()+'-'+f(this.getUTCMonth()+1)+'-'+f(this.getUTCDate())+'T'+f(this.getUTCHours())+':'+f(this.getUTCMinutes())+':'+f(this.getUTCSeconds())+'Z':null;};String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(key){return this.valueOf();};}var cx=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,escapable=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta={'\b':'\\b','\t':'\\t','\n':'\\n','\f':'\\f','\r':'\\r','"':'\\"','\\':'\\\\'},rep;function quote(string){escapable.lastIndex=0;return escapable.test(string)?'"'+string.replace(escapable,function(a){var c=meta[a];return typeof c==='string'?c:'\\u'+('0000'+a.charCodeAt(0).toString(16)).slice(-4);})+'"':'"'+string+'"';}function str(key,holder){var i,k,v,length,mind=gap,partial,value=holder[key];if(value&&typeof value==='object'&&typeof value.toJSON==='function'){value=value.toJSON(key);}if(typeof rep==='function'){value=rep.call(holder,key,value);}switch(typeof value){case'string':return quote(value);case'number':return isFinite(value)?String(value):'null';case'boolean':case'null':return String(value);case'object':if(!value){return'null';}gap+=indent;partial=[];if(Object.prototype.toString.apply(value)==='[object Array]'){length=value.length;for(i=0;i<length;i+=1){partial[i]=str(i,value)||'null';}v=partial.length===0?'[]':gap?'[\n'+gap+partial.join(',\n'+gap)+'\n'+mind+']':'['+partial.join(',')+']';gap=mind;return v;}if(rep&&typeof rep==='object'){length=rep.length;for(i=0;i<length;i+=1){k=rep[i];if(typeof k==='string'){v=str(k,value);if(v){partial.push(quote(k)+(gap?': ':':')+v);}}}}else{for(k in value){if(Object.hasOwnProperty.call(value,k)){v=str(k,value);if(v){partial.push(quote(k)+(gap?': ':':')+v);}}}}v=partial.length===0?'{}':gap?'{\n'+gap+partial.join(',\n'+gap)+'\n'+mind+'}':'{'+partial.join(',')+'}';gap=mind;return v;}}if(typeof JSON.stringify!=='function'){JSON.stringify=function(value,replacer,space){var i;gap='';indent='';if(typeof space==='number'){for(i=0;i<space;i+=1){indent+=' ';}}else if(typeof space==='string'){indent=space;}rep=replacer;if(replacer&&typeof replacer!=='function'&&(typeof replacer!=='object'||typeof replacer.length!=='number')){throw new Error('JSON.stringify');}return str('',{'':value});};}if(typeof JSON.parse!=='function'){JSON.parse=function(text,reviver){var j;function walk(holder,key){var k,v,value=holder[key];if(value&&typeof value==='object'){for(k in value){if(Object.hasOwnProperty.call(value,k)){v=walk(value,k);if(v!==undefined){value[k]=v;}else{delete value[k];}}}}return reviver.call(holder,key,value);}text=String(text);cx.lastIndex=0;if(cx.test(text)){text=text.replace(cx,function(a){return'\\u'+('0000'+a.charCodeAt(0).toString(16)).slice(-4);});}if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,'@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,']').replace(/(?:^|:|,)(?:\s*\[)+/g,''))){j=eval('('+text+')');return typeof reviver==='function'?walk({'':j},''):j;}throw new SyntaxError('JSON.parse');};}}());
	// </JasobNoObfs>
}

//	<JasobNoObfs>
//	Base64 encode / decode
//  http://www.webtoolkit.info/
var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(input){var output="";var chr1,chr2,chr3,enc1,enc2,enc3,enc4;var i=0;input=Base64._utf8_encode(input);while(i<input.length){chr1=input.charCodeAt(i++);chr2=input.charCodeAt(i++);chr3=input.charCodeAt(i++);enc1=chr1>>2;enc2=((chr1&3)<<4)|(chr2>>4);enc3=((chr2&15)<<2)|(chr3>>6);enc4=chr3&63;if(isNaN(chr2)){enc3=enc4=64;}else if(isNaN(chr3)){enc4=64;}output=output+this._keyStr.charAt(enc1)+this._keyStr.charAt(enc2)+this._keyStr.charAt(enc3)+this._keyStr.charAt(enc4);}return output;},decode:function(input){var output="";var chr1,chr2,chr3;var enc1,enc2,enc3,enc4;var i=0;input=input.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(i<input.length){enc1=this._keyStr.indexOf(input.charAt(i++));enc2=this._keyStr.indexOf(input.charAt(i++));enc3=this._keyStr.indexOf(input.charAt(i++));enc4=this._keyStr.indexOf(input.charAt(i++));chr1=(enc1<<2)|(enc2>>4);chr2=((enc2&15)<<4)|(enc3>>2);chr3=((enc3&3)<<6)|enc4;output=output+String.fromCharCode(chr1);if(enc3!=64){output=output+String.fromCharCode(chr2);}if(enc4!=64){output=output+String.fromCharCode(chr3);}}output=Base64._utf8_decode(output);return output;},_utf8_encode:function(string){string=string.replace(/\r\n/g,"\n");var utftext="";for(var n=0;n<string.length;n++){var c=string.charCodeAt(n);if(c<128){utftext+=String.fromCharCode(c);}else if((c>127)&&(c<2048)){utftext+=String.fromCharCode((c>>6)|192);utftext+=String.fromCharCode((c&63)|128);}else{utftext+=String.fromCharCode((c>>12)|224);utftext+=String.fromCharCode(((c>>6)&63)|128);utftext+=String.fromCharCode((c&63)|128);}}return utftext;},_utf8_decode:function(utftext){var string="";var i=0;var c=c1=c2=0;while(i<utftext.length){c=utftext.charCodeAt(i);if(c<128){string+=String.fromCharCode(c);i++;}else if((c>191)&&(c<224)){c2=utftext.charCodeAt(i+1);string+=String.fromCharCode(((c&31)<<6)|(c2&63));i+=2;}else{c2=utftext.charCodeAt(i+1);c3=utftext.charCodeAt(i+2);string+=String.fromCharCode(((c&15)<<12)|((c2&63)<<6)|(c3&63));i+=3;}}return string;}}
//	</JasobNoObfs>


//	---------------------------------------------------------------------------
//  jWebSocket - some convenience JavaScript OOP tools
//	---------------------------------------------------------------------------
jws.oop = {};

// implement simple class declaration to support multi-level inheritance
// and easy 'inherited' calls (super-calls) in JavaScript
jws.oop.declareClass = function( aNamespace, aClassname, aAncestor, aFields ) {
	var lNS = self[ aNamespace ];
	if( !lNS ) { 
		self[ aNamespace ] = { };
	}
	var lConstructor = function() {
		if( this.create ) {
			this.create.apply( this, arguments );
		}
	};
	// publish the new class in the given name space
	lNS[ aClassname ] = lConstructor;

	// move all fields from spec to new class' prototype
	var lField;
	for( lField in aFields ) {
		lConstructor.prototype[ lField ] = aFields[ lField ];
	}
	if( aAncestor != null ) {
		// every class maintains an array of its direct descendants
		if( !aAncestor.descendants ) {
			aAncestor.descendants = [];
		}
		aAncestor.descendants.push( lConstructor );
		for( lField in aAncestor.prototype ) {
			var lAncMthd = aAncestor.prototype[ lField ];
			if( typeof lAncMthd == "function" ) {
				if( lConstructor.prototype[ lField ] ) {
					lConstructor.prototype[ lField ].inherited = lAncMthd;
				} else {
					lConstructor.prototype[ lField ] = lAncMthd;
				}
				// every method gets a reference to its super class
				// to allow class to inherited method from such
				lConstructor.prototype[ lField ].superClass = aAncestor;
			}
		}
	}
};


// plug-in functionality to allow to add plug-ins into existing classes
jws.oop.addPlugIn = function( aClass, aPlugIn ) {

	// if the class has no plug-ins yet initialize array
	if( !aClass.fPlugIns ) {
		aClass.fPlugIns = [];
	}
	// add the plug-in to the class
	aClass.fPlugIns.push( aPlugIn );
	// clone all methods of the plug-in to the class
	for( var lField in aPlugIn ) {
		// don't overwrite existing methods of class with plug-in methods
		// ensure that native jWebSocket methods don't get overwritten!
		if( !aClass.prototype[ lField ] ) {
			aClass.prototype[ lField ] = aPlugIn[ lField ];
			// var lObj = aClass.prototype[ lField ];
		}
	}
	// if the class already has descendants recursively
	// clone the plug-in methods to these as well.
	// checkDescendants( aClass );
	if( aClass.descendants ) {
		for( var lIdx = 0, lCnt = aClass.descendants.length; lIdx < lCnt; lIdx ++ ) {
			jws.oop.addPlugIn( aClass.descendants[ lIdx ], aPlugIn );
		}
	}
};


//	---------------------------------------------------------------------------
//  jWebSocket - Base Client
//  This class does not handle exceptions or error, it throws exceptions,
//  which are handled by the descendant classes.
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.jWebSocketBaseClient
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.jWebSocketBaseClient[/tt] class. _
//:d:en:This class does not handle exceptions or error, it throws exceptions, _
//:d:en:which are (have to be) handled by the descendant classes.

jws.oop.declareClass( "jws", "jWebSocketBaseClient", null, {

	//:m:*:processOpened
	//:d:en:Called when the WebSocket connection successfully was established. _
	//:d:en:Can to be overwritten in descendant classes to process _
	//:d:en:[tt]onopen[/tt] event in descendant classes.
	//:a:en::aEvent:Object:Pending...
	//:r:*:::void:none
	processOpened: function( aEvent ) {
		// method to be overwritten in descendant classes
	},

	//:m:*:processPacket
	//:d:en:Called when a data packet was received. _
	//:d:en:Can to be overwritten in descendant classes to process _
	//:d:en:[tt]onmessage[/tt] event in descendant classes.
	//:a:en::aEvent:Object:Pending...
	//:r:*:::void:none
	processPacket: function( aEvent ) {
		// method to be overwritten in descendant classes
	},

	//:m:*:processClosed
	//:d:en:Called when the WebSocket connection was closed. _
	//:d:en:Can to be overwritten in descendant classes to process _
	//:d:en:[tt]onclose[/tt] event in descendant classes.
	//:a:en::aEvent:Object:Pending...
	//:r:*:::void:none
	processClosed: function( aEvent ) {
		// method to be overwritten in descendant classes
	},

	//:m:*:open
	//:d:en:Tries to establish a connection the jWebSocket server.
	//:a:en::aURL:String:URL to the jWebSocket Server
	//:a:en::aOptions:Object:Optional arguments, see below...
	//:a:en:aOptions:OnOpen:function:Callback when connection was successfully established.
	//:r:*:::void:none
	open: function( aURL, aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		// if browser natively supports WebSockets...
		// otherwise flash bridge may have embedded WebSocket class
		if( self.WebSocket) {

			// TODO: !this.fConn is not enough here! Check for readystate!
			// if connection not already established...
			if( !this.fConn ) {
				var lThis = this;
				var lValue = null;

				// check if subprotocol is given
				// if not use JSON as default
				var lSubProt = jws.WS_SUBPROT_JSON;
				if( aOptions.subProtocol ) {
					lSubProt = aOptions.subProtocol;
				}
				// console.log("Opening with sub-prot: " + lSubProt)

				// create a new web socket instance
				this.fConn = new WebSocket( aURL, lSubProt );

				// assign the listeners to local functions (closure) to allow
				// to handle event before and after the application
				this.fConn.onopen = function( aEvent ) {
					lValue = lThis.processOpened( aEvent );
					// give application change to handle event
					if( aOptions.OnOpen ) {
						aOptions.OnOpen( aEvent, lValue, lThis );
					}
				};

				this.fConn.onmessage = function( aEvent ) {
					lValue = lThis.processPacket( aEvent );
					// give application change to handle event first
					if( aOptions.OnMessage ) {
						aOptions.OnMessage( aEvent, lValue, lThis );
					}
				};

				this.fConn.onclose = function( aEvent ) {
					// check if still disconnect timeout active and clear if needed
					if( lThis.hDisconnectTimeout ) {
						clearTimeout( lThis.hDisconnectTimeout );
						delete lThis.hDisconnectTimeout;
					}
					lValue = lThis.processClosed( aEvent );
					// give application chance to handle event
					if( aOptions.OnClose ) {
						aOptions.OnClose( aEvent, lValue, lThis );
					}
					lThis.fConn = null;
				};

			} else {
				throw new Error( "Already connected" );
			}
		} else {
			throw new Error( "WebSockets not supported by browser" );
		}
	},

	//:m:*:connect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore!
	//:a:en::aURL:String:Please refer to [tt]open[/tt] method.
	//:a:en::aOptions:Object:Please refer to [tt]open[/tt] method.
	//:r:*:::void:none
	connect: function( aURL, aOptions ) {
		return this.open(aURL, aOptions );
	},

	//:m:*:sendStream
	//:d:en:Sends a given string to the jWebSocket Server. The methods checks _
	//:d:en:if the connection is still up and throws an exception if not.
	//:a:en::aData:String:String to be send the jWebSocketServer
	//:r:*:::void:none
	sendStream: function( aData ) {
		// is client already connected
		if( this.isConnected() ) {
			this.fConn.send( aData );
		// if not raise exception
		} else {
			throw new Error( "Not connected" );
		}
	},

	//:m:*:isConnected
	//:d:en:Returns [tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	//:a:en::::none
	//:r:*:::boolean:[tt]true[/tt] if the WebSocket connection is up otherwise [tt]false[/tt].
	isConnected: function() {
		return( this.fConn && this.fConn.readyState == jws.OPEN );
	},

	//:m:*:forceClose
	//:d:en:Forces an immediate client side disconnect. The processClosed
	//:d:en:method is called if the connection was up otherwise no operation is
	//:d:en:performed.
	//:a:en::::none
	//:r:*:::void:none
	forceClose: function( aOptions ) {
		// if client closes usually no event is fired
		// here you optionally can fire it if required in your app!
		var lFireClose = false;
		if( aOptions ) {
			if( aOptions.fireClose && this.fConn.onclose ) {
				// TODO: Adjust to event fields 
				// if such are delivered in real event
				var lEvent = {};
				this.fConn.onclose( lEvent );
			}
		}
		if( this.fConn ) {
			// if( window.console ) { console.log( "forcing close...." ); }
			// reset listeners to prevent any kind of potential memory leaks.
			this.fConn.onopen = null;
			this.fConn.onmessage = null;
			this.fConn.onclose = null;
			// TODO: what about CONNECTING state ?!
			if( this.fConn.readyState == jws.OPEN ) {
				this.fConn.close();
			}
			// TODO: should be called only if client was really opened before
			this.processClosed();
		}
		// explicitely reset fConn to "null"
		this.fConn = null;
	},

	//:m:*:close
	//:d:en:Closes the connection either immediately or with an optional _
	//:d:en:timeout. _
	//:d:en:If the connection is established up an exception s fired.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:timeout:Number:The close timeout in milliseconds, default [tt]0[/tt].
	//:r:*:::void:none
	close: function( aOptions ) {
		// check if timeout option is used
		var lTimeout = 0;
		if( aOptions ) {
			if( aOptions.timeout ) {
				lTimeout = aOptions.timeout;
			}
		}
		// connection established at all?
		// TODO: Shouldn't we test for ready state here?
		if( this.fConn ) {
			if( lTimeout <= 0 ) {
				this.forceClose( aOptions );
			} else {
				var lThis = this;
				this.hDisconnectTimeout = setTimeout(
					function() {
						lThis.forceClose( aOptions );
					},
					lTimeout
				);
			}
		// throw exception if not connected
		} else {
			this.fConn = null;
			throw new Error( "Not connected" );
		}
	},

	//:m:*:disconnect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore! _
	//:d:en:Please refer to the [tt]close[/tt] method.
	//:a:en::aOptions:Object:Please refer to the [tt]close[/tt] method.
	//:r:*::::Please refer to the [tt]close[/tt] method.
	disconnect: function( aOptions ) {
		return this.close( aOptions );
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket token client (this is an abstract class)
//  don't create direct instances of jWebSocketTokenClient
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.jWebSocketTokenClient
//:ancestor:*:jws.jWebSocketBaseClient
//:d:en:Implementation of the [tt]jWebSocketTokenClient[/tt] class. This is _
//:d:en:an abstract class as an ancestor for the JSON-, CSV- and XML client. _
//:d:en:Do not create direct instances of jWebSocketTokenClient.
jws.oop.declareClass( "jws", "jWebSocketTokenClient", jws.jWebSocketBaseClient, {

	//:m:*:create
	//:d:en:This method is called by the contructor of this class _
	//:d:en:to init the instance.
	//:a:en::::none
	//:r:*:::void:none
	create: function( aOptions ) {
		this.fRequestCallbacks = {};
	},

	//:m:*:getId
	//:d:en:Returns the unique id of this client assigned by the jWebSocket server.
	//:a:en::::none
	//:r:*:::String:Unique id of this client.
	getId: function() {
		return this.fClientId;
	},

	//:m:*:checkCallbacks
	//:d:en:Processes an incoming result token and assigns it to a previous _
	//:d:en:request. If a request was found it calls it OnResponse method _
	//:d:en:and removes the reference of the list of pending results.
	//:d:en:This method is used internally only and should not be called by _
	//:d:en:the application.
	//:a:en::aToken:Object:The incoming result token.
	//:r:*:::void:none
	checkCallbacks: function( aToken ) {
		var lField = "utid" + aToken.utid;
		// console.log( "checking result for utid: " + aToken.utid + "..." );
		var lClbkRec = this.fRequestCallbacks[ lField ];
		if( lClbkRec ) {
			lClbkRec.callback.OnResponse( aToken );
			// result came in within the given timeout
			if( lClbkRec.hCleanUp ) {
				// thus reset the timeout observer
				clearTimeout( lClbkRec.hCleanUp );
			}
			delete this.fRequestCallbacks[ lField ];
		}
	},

	//:m:*:createDefaultResult
	//:d:en:Creates a response token with [tt]code = 0[/tt] and _
	//:d:en:[tt]msg = "Ok"[/tt]. It automatically increases the TOKEN_ID _
	//:d:en:to obtain a unique serial id for the next request.
	//:a:en::::none
	//:r:*:::void:none
	createDefaultResult: function() {
		return{
			code: 0,
			msg: "Ok",
			localeKey: "jws.jsc.res.Ok",
			args: null,
			tid: jws.CUR_TOKEN_ID
		};
	},

	//:m:*:checkConnected
	//:d:en:Checks if the client is connected and if so returns a default _
	//:d:en:response token (please refer to [tt]createDefaultResult[/tt] _
	//:d:en:method. If the client is not connected an error token is returned _
	//:d:en:with [tt]code = -1[/tt] and [tt]msg = "Not connected"[/tt]. _
	//:d:en:This is a convenience method if a function needs to check if _
	//:d:en:the client is connected and return an error token if not.
	//:a:en::::none
	//:r:*:::void:none
	checkConnected: function() {
		var lRes = this.createDefaultResult();
		if( !this.isConnected() ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:checkLoggedIn
	//:d:en:Checks if the client is connected and logged in and if so returns _
	//:d:en:a default response token (please refer to [tt]createDefaultResult[/tt] _
	//:d:en:method. If the client is not connected or nott logged in an error _
	//:d:en:token is returned with [tt]code = -1[/tt] and _
	//:d:en:[tt]msg = "Not logged in"[/tt]. _
	//:d:en:This is a convenience method if a function needs to check if _
	//:d:en:the client is connected and return an error token if not.
	//:a:en::::none
	//:r:*:::void:none
	checkLoggedIn: function() {
		var lRes = this.checkConnected();
		if( lRes.code == 0 && !this.isLoggedIn() ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	//:m:*:resultToString
	//:d:en:Converts a result token to a readable string e.g. to be displayed _
	//:d:en:in the GUI.
	//:a:en::aResToken:Object:The result token to be converted into a string.
	//:r:*:::String:The human readable string output of the result token.
	resultToString: function( aResToken ) {
		return(
			( aResToken && typeof aResToken == "object" && aResToken.msg ? 
				aResToken.msg : "invalid response token" )
			// + " (code: " + aRes.code + ", tid: " + aRes.tid + ")"
		);
	},

	//:m:*:tokenToStream
	//:d:en:Converts a token into a string (stream). This method needs to be _
	//:d:en:overwritten by the descendant classes to implement a certain _
	//:d:en:sub protocol like JSON, CSV or XML. If you call this method _
	//:d:en:directly an exception is raised.
	//:a:en::aToken:Object:Token to be converted into a stream.
	//:r:*:::void:none
	tokenToStream: function( aToken ) {
		// this is supposed to convert a token into a string stream which is
		// send to the server, not implemented in base class.
		// needs to be overwritten in descendant classes!
		throw new Error( "tokenToStream needs to be overwritten in descendant classes" );
	},

	//:m:*:streamToToken
	//:d:en:Converts a string (stream) into a token. This method needs to be _
	//:d:en:overwritten by the descendant classes to implement a certain _
	//:d:en:sub protocol like JSON, CSV or XML. If you call this method _
	//:d:en:directly an exception is raised.
	//:a:en::aStream:String:Stream to be converted into a token.
	//:r:*:::void:none
	streamToToken: function( aStream ) {
		// this is supposed to convert a string stream from the server into 
		// a token (object), not implemented in base class.
		// needs to be overwritten in descendant classes
		throw new Error( "streamToToken needs to be overwritten in descendant classes" );
	},

	//:m:*:notifyPlugInsOpened
	//:d:en:Iterates through the client side plug-in chain and calls the _
	//:d:en:[tt]processOpened[/tt] method of each plug-in after the client _
	//:d:en:successfully established the connection to the server.
	//:d:en:By this mechanism all plug-ins easily can handle a new connection.
	//:a:en::::none
	//:r:*:::void:none
	notifyPlugInsOpened: function() {
		var lToken = {
			sourceId: this.fClientId
		};
		// notify all plug-ins about sconnect event
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processOpened ) {
					lPlugIn.processOpened.call( this, lToken );
				}
			}
		}
	},

	//:m:*:notifyPlugInsClosed
	//:d:en:Iterates through the client side plug-in chain and calls the _
	//:d:en:[tt]processClosed[/tt] method of each plug-in after the client _
	//:d:en:successfully established the connection to the server.
	//:d:en:By this mechanism all plug-ins easily can handle a terminated connection.
	//:a:en::::none
	//:r:*:::void:none
	notifyPlugInsClosed: function() {
		var lToken = {
			sourceId: this.fClientId
		};
		// notify all plug-ins about disconnect event
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processClosed ) {
					lPlugIn.processClosed.call( this, lToken );
				}
			}
		}
		// in case of a server side close event...
		this.fConn = null;
		// reset the session...
		this.fSessionId = null;
		// and the username as well
		this.fUsername = null;
	},

	//:m:*:processPacket
	//:d:en:Is called when a new raw data packet is received by the client. _
	//:d:en:This methods calls the [tt]streamToToken[/tt] method of the _
	//:d:en:its descendant who is responsible to implement the sub protocol _
	//:d:en:JSON, CSV or XML, here to parse the raw packet in the corresponding _
	//:d:en:format.
	//:a:en::aEvent:Object:Event object from the browser's WebSocket instance.
	//:r:*:::void:none
	processPacket: function( aEvent ) {
		// parse incoming token...
		var lToken = this.streamToToken( aEvent.data );
		// and process it...
		this.processToken( lToken );
		return lToken;
	},

	// TODO: move handlers to system plug-in in the same way as on server.
	// TODO: No change for application!
	//:m:*:processToken
	//:d:en:Processes an incoming token. The method iterates through all _
	//:d:en:plug-ins and calls their specific [tt]processToken[/tt] method.
	//:a:en::aToken:Object:Token to be processed by the plug-ins in the plug-in chain.
	//:r:*:::void:none
	processToken: function( aToken ) {

		// TODO: Remove this temporary hack with final release 1.0
		// TODO: this was required to ensure upward compatibility from 0.10 to 0.11
		var lNS = aToken.ns;
		if ( lNS != null && lNS.indexOf( "org.jWebSocket" ) == 1 ) {
			aToken.ns = "org.jwebsocket" + lNS.substring( 15 );
		} else if( lNS == null ) {
			aToken.ns = "org.jwebsocket.plugins.system";
		}

		// is it a token from the system plug-in at all?
		if( jws.NS_SYSTEM == aToken.ns ) {
			// check welcome and goodBye tokens to manage the session
			if( aToken.type == "welcome" && aToken.usid ) {
				this.fSessionId = aToken.usid;
				this.fClientId = aToken.sourceId;
				this.notifyPlugInsOpened();
				// fire OnWelcome Event if assigned
				if( this.fOnWelcome ) {
					this.fOnWelcome( aToken );
				}
			} else if( aToken.type == "goodBye" ) {
				// fire OnGoodBye Event if assigned
				if( this.fOnGoodBye ) {
					this.fOnGoodBye( aToken );
				}
				this.fSessionId = null;
				this.fUsername = null;
			} else if( aToken.type == "close" ) {
				// if the server closes the connection close immediately too.
				this.close({
					timeout: 0
				});
			// check if we got a response from a previous request
			} else if( aToken.type == "response" ) {
				// check login and logout manage the username
				if( aToken.reqType == "login" ) {
					this.fUsername = aToken.username;
					// if re-login used previous session-id re-assign it here!
					if( aToken.usid ) {
						this.fSessionId = aToken.usid;
					}
				}
				if( aToken.reqType == "logout" ) {
					this.fUsername = null;
				}
				// check if some requests need to be answered
				this.checkCallbacks( aToken );
			} else if( aToken.type == "event" ) {
				// check login and logout manage the username
				if( aToken.name == "connect" ) {
					this.processConnected( aToken );
				}
				if( aToken.name == "disconnect" ) {
					this.processDisconnected( aToken );
				}
			}
		} else {
			// check the incoming token for an optional response callback
			this.checkCallbacks( aToken );
		}

		// notify all plug-ins that a token has to be processed
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processToken ) {
					lPlugIn.processToken.call( this, aToken );
				}
			}
		}
	},

	//:m:*:processClosed
	//:d:en:Iterates through all plug-ins of the plugin-chain and calls their _
	//:d:en:specific [tt]processClosed[/tt] method.
	//:a:en::aEvent:Object:...
	//:r:*:::void:none
	processClosed: function( aEvent ) {
		this.notifyPlugInsClosed();
		this.fClientId = null;
	},

	//:m:*:processConnected
	//:d:en:Called when the client successfully received a connect event token _
	//:d:en:which means that another client has connected to the network.
	//:a:en::aToken:Object:...
	//:r:*:::void:none
	processConnected: function( aToken ) {
		// notify all plug-ins that a new client connected
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processConnected ) {
					lPlugIn.processConnected.call( this, aToken );
				}
			}
		}
	},

	//:m:*:processDisconnected
	//:d:en:Called when the client successfully received a disconnect event token _
	//:d:en:which means that another client has disconnected from the network.
	//:a:en::aToken:Object:...
	//:r:*:::void:none
	processDisconnected: function( aToken ) {
		// notify all plug-ins that a client disconnected
		var lPlugIns = jws.jWebSocketTokenClient.fPlugIns;
		if( lPlugIns ) {
			for( var lIdx = 0, lLen = lPlugIns.length; lIdx < lLen; lIdx++ ) {
				var lPlugIn = lPlugIns[ lIdx ];
				if( lPlugIn.processDisconnected ) {
					lPlugIn.processDisconnected.call( this, aToken );
				}
			}
		}
	},

	//:m:*:sendToken
	//:d:en:Sends a token to the jWebSocket server.
	//:a:en::aToken:Object:Token to be send to the jWebSocket server.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:OnResponse:Function:Reference to callback function, which is called when the response is received.
	//:r:*:::void:none
	sendToken: function( aToken, aOptions ) {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			var lSpawnThread = false;
			var lTimeout = jws.DEF_WAITRESP_TIMEOUT;
			var lCallbacks = {
				OnResponse: null,
				OnSuccess: null,
				OnError: null,
				OnTimeout: null
			};
			var lControlResponse = false;
			if( aOptions ) {
				if( aOptions.OnResponse ) {
					lCallbacks.OnResponse = aOptions.OnResponse;
					lControlResponse = true;
				}
				if( aOptions.OnError ) {
					lCallbacks.OnError = aOptions.OnError;
					lControlResponse = true;
				}
				if( aOptions.OnSuccess ) {
					lCallbacks.OnSuccess = aOptions.OnSuccess;
					lControlResponse = true;
				}
				if( aOptions.OnTimeout ) {
					lCallbacks.OnTimeout = aOptions.OnTimeout;
					lControlResponse = true;
				}
				if( aOptions.timeout ) {
					lTimeout = aOptions.timeout;
				}
				if( aOptions.spawnThread ) {
					lSpawnThread = aOptions.spawnThread;
				}
			}
			jws.CUR_TOKEN_ID++;
			if( lControlResponse ) {
				var lUTID = jws.CUR_TOKEN_ID;
				var lClbkId = "utid" + lUTID;
				var lThis = this;
				var lClbkRec = {
					request: new Date().getTime(),
					callback: lCallbacks,
					timeout: lTimeout
				};
				this.fRequestCallbacks[ lClbkId ] = lClbkRec;
				// set timeout to observe response
				lClbkRec.hCleanUp = setTimeout( function() {
					var lCallbacks = lClbkRec.callback;
					if( lCallbacks.OnTimeout ) {
						lCallbacks.OnTimeout({
							utid: lUTID,
							timeout: lTimeout,
							token: aToken
						});
					}
					delete lThis.fRequestCallbacks[ lClbkId ];
				}, lTimeout );
			}
			if( lSpawnThread ) {
				aToken.spawnThread = true;
			}
			var lStream = this.tokenToStream( aToken );

			// console.log("sending" + lStream + "...");
			this.sendStream( lStream );
		}
		return lRes;
	},

	//:m:*:getLastTokenId
	//:d:en:Returns the last token id that has been used for the last recent
	//:d:en:request.This id was already used and cannot be used for further
	//:d:en:tranmissions.
	//:a:en::::none
	//:r:*:::Integer:Last recently used unique token-id.
	getLastTokenId: function() {
		return jws.CUR_TOKEN_ID;
	},

	//:m:*:getNextTokenId
	//:d:en:Returns the next token id that will be used for the next request.
	//:d:en:This id will be used by the next sendToken call.
	//:a:en::::none
	//:r:*:::Integer:Next unique token-id used for the next sendToken call.
	getNextTokenId: function() {
		return jws.CUR_TOKEN_ID + 1;
	},

	//:m:*:sendText
	//:d:en:Sends a simple text message to a certain target client within the _
	//:d:en:WebSocket network by creating and sending a [tt]send[/tt] token. _
	//:d:en:The receiver must be addressed by its client id.
	//:d:en:This method requires the user to be authenticated.
	//:a:en::aTarget:String:Client id of the target client for the message.
	//:a:en::aText:String:Textmessage to be send to the target client.
	//:r:*:::void:none
	sendText: function( aTarget, aText ) {
		var lRes = this.checkLoggedIn();
		if( lRes.code == 0 ) {
			this.sendToken({
				ns: jws.NS_SYSTEM,
				type: "send",
				targetId: aTarget,
				sourceId: this.fClientId,
				sender: this.fUsername,
				data: aText
			});
		}
		return lRes;
	},

	//:m:*:broadcastText
	//:d:en:Broadcasts a simple text message to all clients or a limited set _
	//:d:en:of clients within the WebSocket network by creating and sending _
	//:d:en:a [tt]broadcast[/tt] token. The caller can decide to wether or not _
	//:d:en:included in the broadcast and if he requests a response (optional _
	//:d:en:"one-way" token).
	//:d:en:This method requires the user to be authenticated.
	//:a:en::aPool:String:...
	//:a:en::aText:type:...
	//:a:en::aOptions:Object:...
	//:a:en:aOptions:senderIncluded:Boolean:..., default [tt]false[/tt].
	//:a:en:aOptions:responseRequested:Boolean:..., default [tt]true[/tt].
	//:r:*:::void:none
	broadcastText: function( aPool, aText, aOptions ) {
		var lRes = this.checkLoggedIn();
		var lSenderIncluded = false;
		var lResponseRequested = true;
		if( aOptions ) {
			if( aOptions.senderIncluded ) {
				lSenderIncluded = aOptions.senderIncluded;
			}
			if( aOptions.responseRequested ) {
				lResponseRequested = aOptions.responseRequested;
			}
		}
		if( lRes.code == 0 ) {
			this.sendToken({
				ns: jws.NS_SYSTEM,
				type: "broadcast",
				sourceId: this.fClientId,
				sender: this.fUsername,
				pool: aPool,
				data: aText,
				senderIncluded: lSenderIncluded,
				responseRequested: lResponseRequested
			},
			aOptions
			);
		}
		return lRes;
	},

	//:m:*:echo
	//:d:en:Sends an echo token to the jWebSocket server. The server returns
	//:d:en:the same message with a prefix.
	//:a:en::aData:String:An arbitrary string to be returned by the server.
	//:r:*:::void:none
	echo: function( aData ) {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			this.sendToken({
				ns: jws.NS_SYSTEM,
				type: "echo",
				data: aData
			});
		}
		return lRes;
	},

	//:m:*:open
	//:d:en:Tries to establish a connection to the jWebSocket server. Unlike _
	//:d:en:the inherited [tt]open[/tt] method no exceptions is fired in case _
	//:d:en:of an error but a response token is returned.
	//:a:en::aURL:String:URL to the jWebSocket server.
	//:a:en::aOptions:Object:Optional arguments, for details please refer to the open method of the [tt]jWebSocketBaseClient[/tt] class.
	//:r:*:::Object:The response token.
	//:r:*:Object:code:Number:Response code (0 = ok, otherwise error).
	//:r:*:Object:msg:String:"Ok" or error message.
	open: function( aURL, aOptions ) {
		var lRes = this.createDefaultResult();
		try {
			if( aOptions && aOptions.OnWelcome && typeof aOptions.OnWelcome == "function" ) {
				this.fOnWelcome = aOptions.OnWelcome;
			}
			if( aOptions && aOptions.OnGoodBye && typeof aOptions.OnGoodBye == "function" ) {
				this.fOnGoodBye = aOptions.OnGoodBye;
			}
			// call inherited connect, catching potential exception
			arguments.callee.inherited.call( this, aURL, aOptions );
		} catch( ex ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.ex";
			lRes.args = [ ex.message ];
			lRes.msg = "Exception on open: " + ex.message;
		}
		return lRes;
	},

	//:m:*:connect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore!
	//:d:en:Please refer to the [tt]open[/tt] method.
	//:a:en:::Deprecated:Please refer to the [tt]open[/tt] method.
	//:r:*:::Deprecated:Please refer to the [tt]open[/tt] method.
	connect: function( aURL, aOptions ) {
		return this.open( aURL, aOptions );
	},

	//:m:*:close
	//:d:en:Closes an established WebSocket connection.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:timeout:Number:Timeout in milliseconds.
	//:r:*:::void:none
	close: function( aOptions ) {
		var lTimeout = 0;
		if( aOptions ) {
			if( aOptions.timeout ) {
				lTimeout = aOptions.timeout;
			}
		}
		var lRes = this.checkConnected();
		try {
			// if connected and timeout is passed give server a chance to
			// register the disconnect properly and send a good bye response.
			if( lRes.code == 0 ) {
				this.sendToken({
					ns: jws.NS_SYSTEM,
					type: "close",
					timeout: lTimeout
				});
				// call inherited disconnect, catching potential exception
				arguments.callee.inherited.call( this, aOptions );
			} else {
				lRes.code = -1;
				lRes.localeKey = "jws.jsc.res.notConnected";
				lRes.msg = "Not connected.";
			}
		} catch( ex ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.ex";
			lRes.args = [ ex.message ];
			lRes.msg = "Exception on close: " + ex.message;
		}
		return lRes;
	},

	//:m:*:disconnect
	//:d:en:Deprecated, kept for upward compatibility only. Do not use anymore!
	//:d:en:Please refer to the [tt]close[/tt] method.
	//:a:en:::Deprecated:Please refer to the [tt]close[/tt] method.
	//:r:*:::Deprecated:Please refer to the [tt]close[/tt] method.
	disconnect: function( aOptions ) {
		return this.close( aOptions );
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket Client System Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.SystemClientPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.SystemClientPlugIn[/tt] class.
jws.SystemClientPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.system (jws.NS_BASE + ".plugins.system")
	//:d:en:Namespace for SystemClientPlugIn
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_SYSTEM,

	//:const:*:ALL_CLIENTS:Number:0
	//:d:en:For [tt]getClients[/tt] method: Returns all currently connected clients irrespective of their authentication state.
	ALL_CLIENTS: 0,
	//:const:*:AUTHENTICATED:Number:1
	//:d:en:For [tt]getClients[/tt] method: Returns all authenticated clients only.
	AUTHENTICATED: 1,
	//:const:*:NON_AUTHENTICATED:Number:2
	//:d:en:For [tt]getClients[/tt] method: Returns all non-authenticated clients only.
	NON_AUTHENTICATED: 2,

	//:m:*:login
	//:d:en:Tries to authenticate the client against the jWebSocket Server by _
	//:d:en:sending a [tt]login[/tt] token.
	//:a:en::aUsername:String:The login name of the user.
	//:a:en::aPassword:String:The password of the user.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:pool:String:Default pool the user want to register at (default [tt]null[/tt], no pool).
	//:a:en:aOptions:autoConnect:Boolean:not yet supported (defautl [tt]true[/tt]).
	//:r:*:::void:none
	login: function( aUsername, aPassword, aOptions ) {
		var lPool = null;
		var lAutoConnect = false;
		if( aOptions ) {
			if( aOptions.pool !== undefined ) {
				lPool = aOptions.pool;
			}
			if( aOptions.autoConnect !== undefined ) {
				lAutoConnect = aOptions.autoConnect;
			}
		}
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "login",
				username: aUsername,
				password: aPassword,
				pool: lPool
			});
		} else {
			if( lAutoConnect ) {
				// TODO: Implement auto connect! Update documentation when done.
				lRes.code = -1;
				lRes.localeKey = "jws.jsc.res.notConnected";
				lRes.msg = "Not connected.";
			} else {
				lRes.code = -1;
				lRes.localeKey = "jws.jsc.res.notConnected";
				lRes.msg = "Not connected.";
			}
		}
		return lRes;
	},

	//:m:*:logon
	//:d:en:Tries to connect and authenticate the client against the _
	//:d:en:jWebSocket Server in a single call. If the client is already _
	//:d:en:connected this connection is used and not re-established. _
	//:d:en:If the client is already authenticated he is logged off first and _
	//:d:en:re-logged in afterwards by sending a [tt]login[/tt] token.
	//:d:en:The logoff of the client in case of a re-login is automatically _
	//:d:en:processed by the jWebSocket server and does not need to be _
	//:d:en:explicitely triggered by the client.
	// TODO: check server if it sends logout event in ths case!
	//:a:en::aURL:String:The URL of the jWebSocket Server.
	//:a:en::aUsername:String:The login name of the user.
	//:a:en::aPassword:String:The password of the user.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	// TODO: document options!
	//:r:*:::void:none
	logon: function( aURL, aUsername, aPassword, aOptions ) {
		var lRes = this.createDefaultResult();
		if( !aOptions ) {
			aOptions = {};
		}
		if( this.isConnected() ) {
			this.login( aUsername, aPassword );
		} else {
			var lAppOnOpenClBk = aOptions.OnOpen;
			var lThis = this;
			aOptions.OnOpen = function( aEvent ) {
				if( lAppOnOpenClBk ) {
					lAppOnOpenClBk.call( lThis, aEvent );
				}
				lThis.login( aUsername, aPassword );
			};
			this.open(
				aURL,
				aOptions
			);
		}
		return lRes;
	},

	//:m:*:logout
	//:d:en:Logs the currently authenticated used out. After that the user _
	//:d:en:is not authenticated anymore against the jWebSocket network. _
	//:d:en:The client is not automatically disconnected.
	//:d:en:If you want to logout and disconnect please refere to the _
	//:d:en:[tt]close[/tt] method. Closing a connection automatically logs off _
	//:d:en:a potentially authenticated user.
	// TODO: implement optional auto disconnect!
	//:a:en::::none
	//:r:*:::void:none
	logout: function() {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "logout"
			});
		}
		return lRes;
	},

	//:m:*:isLoggedIn
	//:d:en:Returns [tt]true[/tt] when the client is authenticated, _
	//:d:en:otherwise [tt]false[/tt].
	//:a:en::::none
	//:r:*:::Boolean:[tt]true[/tt] when the client is authenticated, otherwise [tt]false[/tt].
	isLoggedIn: function() {
		return( this.isConnected() && this.fUsername );
	},

	broadcastToken: function( aToken, aOptions ) {
		aToken.ns = jws.SystemClientPlugIn.NS;
		aToken.type = "broadcast";
		aToken.sourceId = this.fClientId;
		aToken.sender = this.fUsername;
		return this.sendToken( aToken, aOptions );
	},

	//:m:*:getUsername
	//:d:en:Returns the login name when the client is authenticated, _
	//:d:en:otherwise [tt]null[/tt].
	//:d:en:description pending...
	//:a:en::::none
	//:r:*:::String:Login name when the client is authenticated, otherwise [tt]null[/tt].
	getUsername: function() {
		return( this.isLoggedIn() ? this.fUsername : null );
	},

	//:m:*:getClients
	//:d:en:Returns an array of clients that are currently connected to the
	//:d:en:jWebSocket network by using the [tt]getClients[/tt] token.
	//:d:en:Notice that the call is non-blocking, i.e. the clients are _
	//:d:en:returned asynchronously by the OnResponse event.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	// TODO: support and/or check pool here!
	//:a:en:aOptions:pool:String:Only consider connections to that certain pool (default=[tt]null[/tt]).
	//:a:en:aOptions:mode:Number:One of the following constants [tt]AUTHENTICATED[/tt], [tt]NON_AUTHENTICATED[/tt], [tt]ALL_CLIENTS[/tt].
	//:r:*:::void:none
	getClients: function( aOptions ) {
		var lMode = jws.SystemClientPlugIn.ALL_CLIENTS;
		var lPool = null;
		if( aOptions ) {
			if( aOptions.mode == jws.SystemClientPlugIn.AUTHENTICATED ||
				aOptions.mode == jws.SystemClientPlugIn.NON_AUTHENTICATED ) {
				lMode = aOptions.mode;
			}
			if( aOptions.pool ) {
				lPool = aOptions.pool;
			}
		}
		var lRes = this.createDefaultResult();
		if( this.isLoggedIn() ) {
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "getClients",
				mode: lMode,
				pool: lPool
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	//:m:*:getNonAuthClients
	//:d:en:Requests an array of all clients that are currently connected to _
	//:d:en:the jWebSocket network but not authenticated.
	//:d:en:Notice that the call is non-blocking, i.e. the clients are _
	//:d:en:returned asynchronously by the OnResponse event.
	//:a:en::aOptions:Object:Please refer to the [tt]getClients[/tt] method.
	//:r:*:::void:none
	getNonAuthClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.NON_AUTHENTICATED;
		return this.getClients( aOptions );
	},

	//:m:*:getAuthClients
	//:d:en:Requests an array of all clients that are currently connected to _
	//:d:en:the jWebSocket network and that are authenticated.
	//:d:en:Notice that the call is non-blocking, i.e. the clients are _
	//:d:en:returned asynchronously by the OnResponse event.
	//:a:en::aOptions:Object:Please refer to the [tt]getClients[/tt] method.
	//:r:*:::void:none
	getAuthClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.AUTHENTICATED;
		return this.getClients( aOptions );
	},

	//:m:*:getAllClients
	//:d:en:Requests an array of all clients that are currently connected to _
	//:d:en:the jWebSocket network irrespective of their authentication status.
	//:d:en:Notice that the call is non-blocking, i.e. the clients are _
	//:d:en:returned asynchronously by the OnResponse event.
	//:a:en::aOptions:Object:Please refer to the [tt]getClients[/tt] method.
	//:r:*:::void:none
	getAllClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.ALL_CLIENTS;
		return this.getClients( aOptions );
	},

	//:m:*:ping
	//:d:en:Sends a simple [tt]ping[/tt] token to the jWebSocket Server as a _
	//:d:en:notification that the client is still alive. The client optionally _
	//:d:en:can request an echo so that the client also get a notification _
	//:d:en:that the server still is alive. The [tt]ping[/tt] thus is an _
	//:d:en:important part of the jWebSocket connection management.
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:echo:Boolean:Specifies whether the client expects a response from the server (default=[tt]true[/tt]).
	//:r:*:::void:none
	ping: function( aOptions ) {
		var lEcho = false;
		if( aOptions ) {
			if( aOptions.echo ) {
				lEcho = true;
			}
		}
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "ping",
				echo: lEcho
				},
				aOptions
			);
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:wait
	//:d:en:Simply send a wait request to the jWebSocket server. _
	//:d:en:The server waits for the given amount of time and returns a _
	//:d:en:result token. This feature is for test and debugging purposes only _
	//:d:en:and is not related to any particular business logic.
	//:a:en::aDuration:Integer:Duration in ms the server waits for a response
	//:a:en::aOptions:Object:Optional arguments as listed below...
	//:a:en:aOptions:OnResponse:Function:Callback to be invoked once the response is received.
	//:r:*:::void:none
	wait: function( aDuration, aOptions ) {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			var lResponseRequested = true;
			if( aOptions ) {
				if( aOptions.responseRequested != undefined ) {
					lResponseRequested = aOptions.responseRequested;
				}
			}
			this.sendToken({
				ns: jws.SystemClientPlugIn.NS,
				type: "wait",
				duration: aDuration,
				responseRequested: lResponseRequested
				},
				aOptions
			);
		}
		return lRes;
	},

	//:m:*:startKeepAlive
	//:d:en:Starts the keep-alive timer in background. keep-alive sends _
	//:d:en:periodic pings to the server with an configurable interval.
	//:d:en:If the keep-alive timer has already has been started, the previous _
	//:d:en:one will be stopped automatically and a new one with new options _
	//:d:en:will be initiated.
	//:a:en::aOptions:Objects:Optional arguments as listed below...
	//:a:en:aOptions:interval:Number:Number of milliseconds for the interval.
	//:a:en:aOptions:echo:Boolean:Specifies wether the server is supposed to send an answer to the client.
	//:a:en:aOptions:immediate:Boolean:Specifies wether to send the first ping immediately or after the first interval.
	//:r:*:::void:none
	startKeepAlive: function( aOptions ) {
		// if we have a keep alive running already stop it
		if( this.hKeepAlive ) {
			stopKeepAlive();
		}
		// return if not (yet) connected
		if( !this.isConnected() ) {
			// TODO: provide reasonable result here!
			return;
		}
		var lInterval = 10000;
		var lEcho = true;
		var lImmediate = true;
		if( aOptions ) {
			if( aOptions.interval != undefined ) {
				lInterval = aOptions.interval;
			}
			if( aOptions.echo != undefined ) {
				lEcho = aOptions.echo;
			}
			if( aOptions.immediate != undefined ) {
				lImmediate = aOptions.immediate;
			}
		}
		if( lImmediate ) {
			// send first ping immediately, if requested
			this.ping({
				echo: lEcho
			});
		}
		// and then initiate interval...
		var lThis = this;
		this.hKeepAlive = setInterval(
			function() {
				if( lThis.isConnected() ) {
					lThis.ping({
						echo: lEcho
					});
				} else {
					lThis.stopKeepAlive();
				}
			},
			lInterval
		);
	},

	//:m:*:stopKeepAlive
	//:d:en:Stops the keep-alive timer in background. If no keep-alive is _
	//:d:en:running no operation is performed.
	//:a:en::::none
	//:r:*:::void:none
	stopKeepAlive: function() {
		// TODO: return reasonable results here
		if( this.hKeepAlive ) {
			clearInterval( this.hKeepAlive );
			this.hKeepAlive = null;
		}
	}
};

// add the JWebSocket SystemClient PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SystemClientPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket JSON client
//	todo: consider potential security issues with 'eval'
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.jWebSocketJSONClient
//:ancestor:*:jws.jWebSocketTokenClient
//:d:en:Implementation of the [tt]jws.jWebSocketJSONClient[/tt] class.
jws.oop.declareClass( "jws", "jWebSocketJSONClient", jws.jWebSocketTokenClient, {

	//:m:*:tokenToStream
	//:d:en:converts a token to a JSON stream. If the browser provides a _
	//:d:en:native JSON class this is used, otherwise it use the automatically _
	//:d:en:embedded JSON library from json.org.
	//:a:en::aToken:Token:The token (an JavaScript Object) to be converted into an JSON stream.
	//:r:*:::String:The resulting JSON stream.
	tokenToStream: function( aToken ) {
		aToken.utid = jws.CUR_TOKEN_ID;
		//:todo:en:Do we really want the session id per call? Alex: Don't think so! To be checked!
 		if( this.fSessionId ) {
			aToken.usid = this.fSessionId;
 		}
		var lJSON = JSON.stringify( aToken );
 		return( lJSON );
	},

	//:m:*:streamToToken
	//:d:en:converts a JSON stream into a token. If the browser provides a _
	//:d:en:native JSON class this is used, otherwise it use the automatically _
	//:d:en:embedded JSON library from json.org. For security reasons the _
	//:d:en:use of JavaScript's eval explicitely was avoided.
	//:a:en::aStream:String:The data stream received from the server to be parsed as JSON.
	//:r:*::Token:Object:The Token object of stream could be parsed successfully.
	//:r:*:Token:[i]field[/i]:[i]type[/i]:Fields of the token depend on its content and purpose and need to be interpreted by the higher level software tiers.
	streamToToken: function( aStream ) {
		// parsing a JSON object in JavaScript couldn't be simpler...
		var lObj = JSON.parse( aStream );
		return lObj;
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket CSV client
//	todo: implement jWebSocket JavaScript CSV client
//	jWebSocket target release 1.1
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.jWebSocketCSVClient
//:ancestor:*:jws.jWebSocketTokenClient
//:d:en:Implementation of the [tt]jws.jWebSocketCSVClient[/tt] class.
jws.oop.declareClass( "jws", "jWebSocketCSVClient", jws.jWebSocketTokenClient, {

	// todo: implement escaping of command separators and equal signs
	//:m:*:tokenToStream
	//:d:en:converts a token to a CSV stream.
	//:a:en::aToken:Token:The token (an JavaScript Object) to be converted into an CSV stream.
	//:r:*:::String:The resulting CSV stream.
	tokenToStream: function( aToken ) {
		var lCSV = "utid=" + jws.CUR_TOKEN_ID;
		if( this.fSessionId ) {
			lCSV += ",usid=\"" + this.fSessionId + "\"";
		}
		for( var lKey in aToken ) {
			var lVal = aToken[ lKey ];
			if( lVal === null || lVal === undefined ) {
				// simply do not generate a value, keep value field empty
				lCSV += "," + lKey + "=";
			} else if( typeof lVal == "string" ) {
				// escape commata and quotes
				lVal = lVal.replace( /[,]/g, "\\x2C" );
				lVal = lVal.replace( /["]/g, "\\x22" );
				lCSV += "," + lKey + "=\"" + lVal + "\"";
			} else {
				lCSV += "," + lKey + "=" + lVal;
			}
		}
		return lCSV;
	},

	// todo: implement escaping of command separators and equal signs
	//:m:*:streamToToken
	//:d:en:converts a CSV stream into a token.
	//:a:en::aStream:String:The data stream received from the server to be parsed as CSV.
	//:r:*::Token:Object:The Token object of stream could be parsed successfully.
	//:r:*:Token:[i]field[/i]:[i]type[/i]:Fields of the token depend on its content and purpose and need to be interpreted by the higher level software tiers.
	streamToToken: function( aStream ) {
		var lToken = {};
		var lItems = aStream.split(",");
		for( var lIdx = 0, lCnt = lItems.length; lIdx < lCnt; lIdx++ ) {
			var lKeyVal = lItems[ lIdx ].split( "=" );
			if( lKeyVal.length == 2 ) {
				var lKey = lKeyVal[ 0 ];
				var lVal = lKeyVal[ 1 ];
				if( lVal.length >= 2 
					&& lVal.charAt(0)=="\""
					&& lVal.charAt(lVal.length-1)=="\"" ) {
					// unescape commata and quotes
					lVal = lVal.replace( /\\x2C/g, "\x2C" );
					lVal = lVal.replace( /\\x22/g, "\x22" );
					// strip string quotes
					lVal = lVal.substr( 1, lVal.length - 2 );
				}
				lToken[ lKey ] = lVal;
			}
		}
		return lToken;
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket XML client
//	todo: PRELIMINARY! Implement jWebSocket JavaScript XML client
//	Targetted for jWebSocket release 1.1
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.jWebSocketXMLClient
//:ancestor:*:jws.jWebSocketTokenClient
//:d:en:Implementation of the [tt]jws.jWebSocketXMLClient[/tt] class.
jws.oop.declareClass( "jws", "jWebSocketXMLClient", jws.jWebSocketTokenClient, {

	//:m:*:tokenToStream
	//:d:en:converts a token to a XML stream.
	//:a:en::aToken:Token:The token (an JavaScript Object) to be converted into an XML stream.
	//:r:*:::String:The resulting XML stream.
	tokenToStream: function( aToken ) {

		function obj2xml( aKey, aValue ) {
			var lXML = "";
			// do we have an array? Caution! Keep this condition on
			// the top because array is also an object!
			if ( aValue instanceof Array ) {
				lXML += "<" + aKey + " type=\"" + "array" + "\">";
				for( var lIdx = 0, lCnt = aValue.length; lIdx < lCnt; lIdx++ ) {
					lXML += obj2xml( "item", aValue[ lIdx ] );
				}
				lXML += "</" + aKey + ">";
			}
			// or do we have an object?
			else if ( typeof aValue  == "object" ) {
				lXML += "<" + aKey + " type=\"" + "object" + "\">";
				for(var lField in aValue ) {
					lXML += obj2xml( lField, aValue[ lField ] );
				}
				lXML += "</" + aKey + ">";
			}
			// or do we have a plain field?
			else {
				lXML +=
				"<" + aKey + " type=\"" + typeof aValue + "\">" +
				aValue.toString() +
				"</" + aKey + ">";
			}
			return lXML;
		}

		var lEncoding = "windows-1252";
		var lResXML =
		"<?xml version=\"1.0\" encoding=\"" + lEncoding + "\"?>" +
		"<token>";
		for( var lField in aToken ) {
			lResXML += obj2xml( lField, aToken[ lField ] );
		}
		lResXML += "</token>";
		return lResXML;
	},

	//:m:*:streamToToken
	//:d:en:converts a XML stream into a token.
	//:a:en::aStream:String:The data stream received from the server to be parsed as XML.
	//:r:*::Token:Object:The Token object of stream could be parsed successfully.
	//:r:*:Token:[i]field[/i]:[i]type[/i]:Fields of the token depend on its content and purpose and need to be interpreted by the higher level software tiers.
	streamToToken: function( aStream ) {
		// first convert the stream into an XML document 
		// by using the embedded XML parser.
		// We do not really want to parse the XML in Javascript!
		// Using the built-in parser should be more performant.
		var lDoc = null;
		/* Once we have an applet for IEx ;-)
		if( window.ActiveXObject ) {
			//:i:de:Internet Explorer
			lDoc = new ActiveXObject( "Microsoft.XMLDOM" );
			lDoc.async = "false";
			lDoc.loadXML( aStream );
		} else {
*/
		// For all other Browsers
		try{
			var lParser = new DOMParser();
			lDoc = lParser.parseFromString( aStream, "text/xml" );
		} catch( ex ) {
		// ignore exception here, lDoc will keep being null
		}
		/*
		}
*/

		function node2obj( aNode, aObj ) {
			var lNode = aNode.firstChild;
			while( lNode != null ) {
				// 1 = element node
				if( lNode.nodeType == 1 ) {
					var lType = lNode.getAttribute( "type" );
					var lKey = lNode.nodeName;
					if( lType ) {
						var lValue = lNode.firstChild;
						// 3 = text node
						if( lValue && lValue.nodeType == 3 ) {
							lValue = lValue.nodeValue;
							if( lValue ) {
								if( lType == "string" ) {
								} else if( lType == "number" ) {
								} else if( lType == "boolean" ) {
								} else if( lType == "date" ) {
								} else {
									lValue = undefined;
								}
								if( lValue ) {
									if ( aObj instanceof Array ) {
										aObj.push( lValue );
									} else {
										aObj[ lKey ] = lValue;
									}
								}
							}
						} else
						// 1 = element node
						if( lValue && lValue.nodeType == 1 ) {
							if( lType == "array" ) {
								aObj[ lKey ] = [];
								node2obj( lNode, aObj[ lKey ] );
							} else if( lType == "object" ) {
								aObj[ lKey ] = {};
								node2obj( lNode, aObj[ lKey ] );
							}
						}
					}
				}
				lNode = lNode.nextSibling;
			}
		}

		var lToken = {};
		if( lDoc ) {
			node2obj( lDoc.firstChild, lToken );
		}
		return lToken;
	}

});

/*
(function() {
	var lObj = {
		aNumber: 1,
		aString: "test1",
		aBoolean: true,
		aArray: [ 2, "test2", false ],
		aObject: {
			bNumber: 3,
			bString: "test3",
			bBoolean: true,
			bArray: [ 3, "test3", true ]
		}
	};
	var lStream = 
		'<?xml version="1.0" encoding="windows-1252"?>' +
		'<token>' +
			'<aNumber type="number">1</aNumber>' +
			'<aString type="string">test1</aString>' +
			'<aBoolean type="boolean">true</aBoolean>' +
			'<aArray type="array">' +
				'<item type="number">2</item>'+
				'<item type="string">test2</item>' +
				'<item type="boolean">false</item>' +
			'</aArray>' +
			'<aObject type="object">' +
				'<bNumber type="number">3</bNumber>'+
				'<bString type="string">test3</bString>' +
				'<bBoolean type="boolean">true</bBoolean>' +
				'<bArray type="array">'+
					'<item type="number">3</item>' +
					'<item type="string">test3</item>' +
					'<item type="boolean">true</item>' +
				'</bArray>' +
			'</aObject>' +
		'</token>';

	var lXMLClient = new jws.jWebSocketXMLClient();
//	var lStream = lXMLClient.tokenToStream( lObj );
	var lToken = lXMLClient.streamToToken( lStream );
	console.log( lStream );
})();
*/
/*
MIT LICENSE
Copyright (c) 2007 Monsur Hossain (http://www.monsur.com)

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
*/

// ****************************************************************************
// CachePriority ENUM
// An easier way to refer to the priority of a cache item

jws.cache = {};

jws.cache.CachePriority = {
    Low: 1,
    Normal: 2,
    High: 4
};

// ****************************************************************************
// Cache constructor
// Creates a new cache object
// INPUT: maxSize (optional) - indicates how many items the cache can hold.
//                             default is -1, which means no limit on the 
//                             number of items.
jws.cache.Cache = function Cache(maxSize) {
    this.items = {};
    this.count = 0;
    if (maxSize == null)
        maxSize = -1;
    this.maxSize = maxSize;
    this.fillFactor = .75;
    this.purgeSize = Math.round(this.maxSize * this.fillFactor);
    
    this.stats = {};
    this.stats.hits = 0;
    this.stats.misses = 0;
}

// ****************************************************************************
// Cache.getItem
// retrieves an item from the cache, returns null if the item doesn't exist
// or it is expired.
// INPUT: key - the key to load from the cache
jws.cache.Cache.prototype.getItem = function(key) {

    // retrieve the item from the cache
    var item = this.items[key];
    
    if (item != null) {
        if (!this._isExpired(item)) {
            // if the item is not expired
            // update its last accessed date
            item.lastAccessed = new Date().getTime();
        } else {
            // if the item is expired, remove it from the cache
            this._removeItem(key);
            item = null;
        }
    }
    
    // return the item value (if it exists), or null
    var returnVal = null;
    if (item != null) {
        returnVal = item.value;
        this.stats.hits++;
    } else {
        this.stats.misses++;
    }
    return returnVal;
};

// ****************************************************************************
// Cache.setItem
// sets an item in the cache
// parameters: key - the key to refer to the object
//             value - the object to cache
//             options - an optional parameter described below
// the last parameter accepts an object which controls various caching options:
//      expirationAbsolute: the datetime when the item should expire
//      expirationSliding: an integer representing the seconds since
//                         the last cache access after which the item
//                         should expire
//      priority: How important it is to leave this item in the cache.
//                You can use the values CachePriority.Low, .Normal, or 
//                .High, or you can just use an integer.  Note that 
//                placing a priority on an item does not guarantee 
//                it will remain in cache.  It can still be purged if 
//                an expiration is hit, or if the cache is full.
//      callback: A function that gets called when the item is purged
//                from cache.  The key and value of the removed item
//                are passed as parameters to the callback function.
jws.cache.Cache.prototype.setItem = function(key, value, options) {

    function CacheItem(k, v, o) {
        if ((k == null) || (k == ''))
            throw new Error("key cannot be null or empty");
        this.key = k;
        this.value = v;
        if (o == null)
            o = {};
        if (o.expirationAbsolute != null)
            o.expirationAbsolute = o.expirationAbsolute.getTime();
        if (o.priority == null)
            o.priority = jws.cache.CachePriority.Normal;
        this.options = o;
        this.lastAccessed = new Date().getTime();
    }

    // add a new cache item to the cache
    if (this.items[key] != null)
        this._removeItem(key);
    this._addItem(new CacheItem(key, value, options));
    
    // if the cache is full, purge it
    if ((this.maxSize > 0) && (this.count > this.maxSize)) {
        this._purge();
    }
};

// ****************************************************************************
// Cache.clear
// Remove all items from the cache
jws.cache.Cache.prototype.clear = function() {

    // loop through each item in the cache and remove it
    for (var key in this.items) {
      this._removeItem(key);
    }  
};

// ****************************************************************************
// Cache._purge (PRIVATE FUNCTION)
// remove old elements from the cache
jws.cache.Cache.prototype._purge = function() {
    
    var tmparray = new Array();
    
    // loop through the cache, expire items that should be expired
    // otherwise, add the item to an array
    for (var key in this.items) {
        var item = this.items[key];
        if (this._isExpired(item)) {
            this._removeItem(key);
        } else {
            tmparray.push(item);
        }
    }
    
    if (tmparray.length > this.purgeSize) {

        // sort this array based on cache priority and the last accessed date
        tmparray = tmparray.sort(function(a, b) { 
            if (a.options.priority != b.options.priority) {
                return b.options.priority - a.options.priority;
            } else {
                return b.lastAccessed - a.lastAccessed;
            }
        });
        
        // remove items from the end of the array
        while (tmparray.length > this.purgeSize) {
            var ritem = tmparray.pop();
            this._removeItem(ritem.key);
        }
    }
};

// ****************************************************************************
// Cache._addItem (PRIVATE FUNCTION)
// add an item to the cache
jws.cache.Cache.prototype._addItem = function(item) {
    this.items[item.key] = item;
    this.count++;
};

// ****************************************************************************
// Cache._removeItem (PRIVATE FUNCTION)
// Remove an item from the cache, call the callback function (if necessary)
jws.cache.Cache.prototype._removeItem = function(key) {
    var item = this.items[key];
    delete this.items[key];
    this.count--;
    
    // if there is a callback function, call it at the end of execution
    if (item.options.callback != null) {
        var callback = function() {
            item.options.callback(item.key, item.value);
        };
        setTimeout(callback, 0);
    }
};

// ****************************************************************************
// Cache._isExpired (PRIVATE FUNCTION)
// Returns true if the item should be expired based on its expiration options
jws.cache.Cache.prototype._isExpired = function(item) {
    var now = new Date().getTime();
    var expired = false;
    if ((item.options.expirationAbsolute) && (item.options.expirationAbsolute < now)) {
        // if the absolute expiration has passed, expire the item
        expired = true;
    } 
    if (!expired && (item.options.expirationSliding)) {
        // if the sliding expiration has passed, expire the item
        var lastAccess = item.lastAccessed + (item.options.expirationSliding * 1000);
        if (lastAccess < now) {
            expired = true;
        }
    }
    return expired;
};

jws.cache.Cache.prototype.toHtmlString = function() {
    var returnStr = this.count + " item(s) in cache<br /><ul>";
    for (var key in this.items) {
        var item = this.items[key];
        returnStr = returnStr + "<li>" + item.key.toString() + " = " + item.value.toString() + "</li>";
    }
    returnStr = returnStr + "</ul>";
    return returnStr;
};
//	---------------------------------------------------------------------------
//	jWebSocket Channel PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Channel Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.ChannelPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.ChannelPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to subscribe and unsubscribe at certain _
//:d:en:channel sn the server.
jws.ChannelPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.channels (jws.NS_BASE + ".plugins.channels")
	//:d:en:Namespace for the [tt]ChannelPlugIn[/tt] class.
	// if namespace changes update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.channels",

	SUBSCRIBE: "subscribe",
	UNSUBSCRIBE: "unsubscribe",
	GET_CHANNELS: "getChannels",
	CREATE_CHANNEL:  "createChannel",
	REMOVE_CHANNEL:  "removeChannel",
	GET_SUBSCRIBERS: "getSubscribers",
	GET_SUBSCRIPTIONS: "getSubscriptions",

	AUTHORIZE: "authorize",
	PUBLISH: "publish",
	STOP: "stop",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.ChannelPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "event" == aToken.type ) {
				if( "channelCreated" == aToken.name ) {
					if( this.OnChannelCreated ) {
						this.OnChannelCreated( aToken );
					}
				} else if( "channelRemoved" == aToken.name ) {
					if( this.OnChannelRemoved ) {
						this.OnChannelRemoved( aToken );
					}
				} 
			} else if( "getChannels" == aToken.reqType ) {
				if( this.OnChannelsReceived ) {
					this.OnChannelsReceived( aToken );
				}
			}
		}
	},

	//:m:*:channelSubscribe
	//:d:en:Registers the client at the given channel on the server. _
	//:d:en:After this operation the client obtains all messages on this _
	//:d:en:channel. Basically, a client can subscribe at multiple channels.
	//:d:en:If no channel with the given ID exists on the server an error token _
	//:d:en:is returned. Depending on the type of the channel it may take more _
	//:d:en:or less time until you get the first token from the channel.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelSubscribe: function( aChannel, aAccessKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.SUBSCRIBE,
				channel: aChannel,
				accessKey: aAccessKey
			});
		}
		return lRes;
	},

	//:m:*:channelUnsubscribe
	//:d:en:Unsubscribes the client from the given channel on the server.
	//:d:en:From this point in time the client does not receive any messages _
	//:d:en:on this channel anymore.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelUnsubscribe: function( aChannel ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.UNSUBSCRIBE,
				channel: aChannel
			});
		}
		return lRes;
	},

	//:m:*:channelAuth
	//:d:en:Authenticates the client at a certain channel to publish messages.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aAccessKey:String:Access key configured for the channel.
	//:a:en::aSecretKey:String:Secret key configured for the channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get notified on error or success.
	channelAuth: function( aChannel, aAccessKey, aSecretKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.AUTHORIZE,
				channel: aChannel,
				login: this.getUsername(),
				accessKey: aAccessKey,
				secretKey: aSecretKey
			});
		}
		return lRes;
	},

	//:m:*:channelPublish
	//:d:en:Sends a message to the given channel on the server.
	//:d:en:The client needs to be authenticated against the server and the
	//:d:en:channel to publish data. All clients that subscribed to the channel
	//:d:en:will receive the message.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aData:String:Data to be sent to the server side data channel.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelPublish: function( aChannel, aData ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.PUBLISH,
				channel: aChannel,
				data: aData
			});
		}
		return lRes;
	},

	//:m:*:channelCreate
	//:d:en:Creates a new channel on the server. If a channel with the given _
	//:d:en:channel-id already exists the create channel request is rejected. _
	//:d:en:A private channel requires an access key, if this is not provided _
	//:d:en:for a private channel the request is rejected. For public channel _
	//:d:en:the access key is optional.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aName:String:The name (human readably) of the channel.
	//:r:*:::void:none
	channelCreate: function( aId, aName, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lIsPrivate = false;
			var lIsSystem = false;
			var lAccessKey = null;
			var lSecretKey = null;
			var lOwner = null;
			var lPassword = null;
			if( aOptions ) {
				if( aOptions.isPrivate != undefined ) {
					lIsPrivate = aOptions.isPrivate;
				}
				if( aOptions.isSystem != undefined ) {
					lIsSystem = aOptions.isSystem;
				}
				if( aOptions.accessKey != undefined ) {
					lAccessKey = aOptions.accessKey;
				}
				if( aOptions.secretKey != undefined ) {
					lSecretKey = aOptions.secretKey;
				}
				if( aOptions.owner != undefined ) {
					lOwner = aOptions.owner;
				}
				if( aOptions.password != undefined ) {
					lPassword = aOptions.password;
				}
			}
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.CREATE_CHANNEL,
				channel: aId,
				name: aName,
				isPrivate: lIsPrivate,
				isSystem: lIsSystem,
				accessKey: lAccessKey,
				secretKey: lSecretKey,
				owner: lOwner,
				password: lPassword
			});
		}
		return lRes;
	},

	//:m:*:channelRemove
	//:d:en:Removes a (non-system) channel on the server. Only the owner of _
	//:d:en:channel can remove a channel. If a accessKey/secretKey pair is _
	//:d:en:defined for a channel this needs to be passed as well, otherwise _
	//:d:en:the remove request is rejected.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:r:*:::void:none
	channelRemove: function( aId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lAccessKey = null;
			var lSecretKey = null;
			var lOwner = null;
			var lPassword = null;
			if( aOptions ) {
				if( aOptions.accessKey != undefined ) {
					lAccessKey = aOptions.accessKey;
				}
				if( aOptions.secretKey != undefined ) {
					lSecretKey = aOptions.secretKey;
				}
				if( aOptions.owner != undefined ) {
					lOwner = aOptions.owner;
				}
				if( aOptions.password != undefined ) {
					lPassword = aOptions.password;
				}
			}
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.REMOVE_CHANNEL,
				channel: aId,
				accessKey: lAccessKey,
				secretKey: lSecretKey,
				owner: lOwner,
				password: lPassword
			});
		}
		return lRes;
	},

	//:m:*:channelGetSubscribers
	//:d:en:Returns all channels to which the current client currently has
	//:d:en:subscribed to. This also includes private channels. The owners of
	//:d:en:the channels are not returned due to security reasons.
	//:a:en::aChannel:String:The id of the server side data channel.
	//:a:en::aAccessKey:String:Access Key for the channel (required for private channels, optional for public channels).
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelGetSubscribers: function( aChannel, aAccessKey ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_SUBSCRIBERS,
				channel: aChannel,
				accessKey: aAccessKey
			});
		}
		return lRes;
	},

	//:m:*:channelGetSubscriptions
	//:d:en:Returns all channels to which the current client currently has
	//:d:en:subscribed to. This also includes private channels. The owners of
	//:d:en:the channels are not returned due to security reasons.
	//:a:en:::none
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelGetSubscriptions: function() {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_SUBSCRIPTIONS
			});
		}
		return lRes;
	},


	//:m:*:channelPublish
	//:d:en:Tries to obtain all id of the channels
	//:a:en:::none
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	channelGetIds: function() {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.ChannelPlugIn.NS,
				type: jws.ChannelPlugIn.GET_CHANNELS
			});
		}
		return lRes;
	},

	setChannelCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnChannelCreated !== undefined ) {
			this.OnChannelCreated = aListeners.OnChannelCreated;
		}
		if( aListeners.OnChannelsReceived !== undefined ) {
			this.OnChannelsReceived = aListeners.OnChannelsReceived;
		}
		if( aListeners.OnChannelRemoved !== undefined ) {
			this.OnChannelRemoved = aListeners.OnChannelRemoved;
		}
	}

};

// add the ChannelPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.ChannelPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Sample Client PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------

//	---------------------------------------------------------------------------
//  jWebSocket Sample Client Plug-In
//	---------------------------------------------------------------------------

jws.CanvasPlugIn = {

	// namespace for shared objects plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.canvas",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.reqNS == jws.CanvasPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "clear" == aToken.reqType ) {
				this.doClear( aToken.id );
			} else if( "beginPath" == aToken.reqType ) {
				this.doBeginPath( aToken.id );
			} else if( "moveTo" == aToken.reqType ) {
				this.doMoveTo( aToken.id, aToken.x, aToken.y );
			} else if( "lineTo" == aToken.reqType ) {
				this.doLineTo( aToken.id, aToken.x, aToken.y );
			} else if( "line" == aToken.reqType ) {
				this.doLine( aToken.id, aToken.x1, aToken.y1,
					aToken.x2, aToken.y2, { color: aToken.color });
			} else if( "closePath" == aToken.reqType ) {
				this.doClosePath( aToken.id );
			}
		}
	},

	fCanvas: {},

	canvasOpen: function( aId, aElementId ) {
		var lElem = jws.$( aElementId );
		this.fCanvas[ aId ] = {
			fDOMElem: lElem,
			ctx: lElem.getContext( "2d" )
		};
	},

	canvasClose: function( aId ) {
		this.fCanvas[ aId ] = null;
		delete this.fCanvas[ aId ];
	},

	doClear: function( aId ) {
		var lCanvas = this.fCanvas[ aId ];
		if( lCanvas != null ) {
			var lW = lCanvas.fDOMElem.getAttribute( "width" );
			var lH = lCanvas.fDOMElem.getAttribute( "height" );
			lCanvas.ctx.clearRect( 0, 0, lW, lH );
			return true;
		}
		return false;
	},

	canvasClear: function( aId ) {
		if( this.doClear( aId ) ) {
			var lToken = {
				reqNS: jws.CanvasPlugIn.NS,
				reqType: "clear",
				id: aId
			};
			this.broadcastToken(lToken);
		}
	},

	doBeginPath: function( aId ) {
		var lCanvas = this.fCanvas[ aId ];
		if( lCanvas != null ) {
			// console.log( "doBeginPath: " + aId);
			lCanvas.ctx.beginPath();
			return true;
		}
		return false;
	},

	canvasBeginPath: function( aId ) {
		if( this.doBeginPath( aId ) ) {
			var lToken = {
				reqNS: jws.CanvasPlugIn.NS,
				reqType: "beginPath",
				id: aId
			};
			this.broadcastToken(lToken);
		}
	},

	doMoveTo: function( aId, aX, aY ) {
		var lCanvas = this.fCanvas[ aId ];
		if( lCanvas != null ) {
			// console.log( "doMoveTo: " + aId + ", x:" + aX + ", y: " + aX );
			lCanvas.ctx.moveTo( aX, aY );
			return true;
		}
		return false;
	},

	canvasMoveTo: function( aId, aX, aY ) {
		if( this.doMoveTo( aId, aX, aY ) ) {
			var lToken = {
				reqNS: jws.CanvasPlugIn.NS,
				reqType: "moveTo",
				id: aId,
				x: aX,
				y: aY
			};
			this.broadcastToken(lToken);
		}
	},

	doLineTo: function( aId, aX, aY ) {
		var lCanvas = this.fCanvas[ aId ];
		if( lCanvas != null ) {
			// console.log( "doLineTo: " + aId + ", x:" + aX + ", y: " + aX );
			lCanvas.ctx.lineTo( aX, aY );
			lCanvas.ctx.stroke();
			return true;
		}
		return false;
	},

	canvasLineTo: function( aId, aX, aY ) {
		if( this.doLineTo( aId, aX, aY ) ) {
			var lToken = {
				reqNS: jws.CanvasPlugIn.NS,
				reqType: "lineTo",
				id: aId,
				x: aX,
				y: aY
			};
			this.broadcastToken(lToken);
		}
	},

	doLine: function( aId, aX1, aY1, aX2, aY2, aOptions ) {
		if( undefined == aOptions ) {
			aOptions = {};
		}
		var lColor = "black";
		if( aOptions.color ) {
			lColor = aOptions.color;
		}
		var lCanvas = this.fCanvas[ aId ];
		if( lCanvas != null ) {
			lCanvas.ctx.beginPath();
			lCanvas.ctx.moveTo( aX1, aY1 );
			lCanvas.ctx.strokeStyle = lColor;
			lCanvas.ctx.lineTo( aX2, aY2 );
			lCanvas.ctx.stroke();
			lCanvas.ctx.closePath();
			return true;
		}
		return false;
	},

	canvasLine: function( aId, aX1, aY1, aX2, aY2, aOptions ) {
		if( undefined == aOptions ) {
			aOptions = {};
		}
		var lColor = "black";
		if( aOptions.color ) {
			lColor = aOptions.color;
		}
		if( this.doLine( aId, aX1, aY1, aX2, aY2, aOptions ) ) {
			var lToken = {
				reqNS: jws.CanvasPlugIn.NS,
				reqType: "line",
				id: aId,
				x1: aX1,
				y1: aY1,
				x2: aX2,
				y2: aY2,
				color: lColor
			};
			this.broadcastToken(lToken);
		}
	},

	doClosePath: function( aId ) {
		var lCanvas = this.fCanvas[ aId ];
		if( lCanvas != null ) {
			// console.log( "doClosePath" );
			lCanvas.ctx.closePath();
			return true;
		}
		return false;
	},

	canvasClosePath: function( aId ) {
		if( this.doClosePath( aId ) ) {
			var lToken = {
				reqNS: jws.CanvasPlugIn.NS,
				reqType: "closePath",
				id: aId
			};
			this.broadcastToken(lToken);
		}
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.CanvasPlugIn );

// optionally include canvas support for IE8
if( jws.isIE ) {

	//  <JasobNoObfs>
	//
	//	-------------------------------------------------------------------------------
	//	ExplorerCanvas
	//
	//	Google Open Source:
	//		<http://code.google.com>
	//		<opensource@google.com>
	//
	//	Developers:
	//		Emil A Eklund <emil@eae.net>
	//		Erik Arvidsson <erik@eae.net>
	//		Glen Murphy <glen@glenmurphy.com>
	//
	//	-------------------------------------------------------------------------------
	//	DESCRIPTION
	//
	//	Firefox, Safari and Opera 9 support the canvas tag to allow 2D command-based
	//	drawing operations. ExplorerCanvas brings the same functionality to Internet
	//	Explorer; web developers only need to include a single script tag in their
	//	existing canvas webpages to enable this support.
	//
	//	-------------------------------------------------------------------------------
	//	INSTALLATION
	//
	//	Include the ExplorerCanvas tag in the same directory as your HTML files, and
	//	add the following code to your page, preferably in the <head> tag.
	//
	//	<!--[if IE]><script type="text/javascript" src="excanvas.js"></script><![endif]-->
	//
	//	If you run into trouble, please look at the included example code to see how
	//	to best implement this
	//	
	//	Copyright 2006 Google Inc.
	//
	//	Licensed under the Apache License, Version 2.0 (the "License");
	//	you may not use this file except in compliance with the License.
	//	You may obtain a copy of the License at
	//
	//	http://www.apache.org/licenses/LICENSE-2.0
	//
	//	Unless required by applicable law or agreed to in writing, software
	//	distributed under the License is distributed on an "AS IS" BASIS,
	//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	//	See the License for the specific language governing permissions and
	//	limitations under the License.
	//
	//	Fullsource code at: http://excanvas.sourceforge.net/
	//	and http://code.google.com/p/explorercanvas/
	//
	//	</JasobNoObfs>

	document.createElement("canvas").getContext||(function(){var s=Math,j=s.round,F=s.sin,G=s.cos,V=s.abs,W=s.sqrt,k=10,v=k/2;function X(){return this.context_||(this.context_=new H(this))}var L=Array.prototype.slice;function Y(b,a){var c=L.call(arguments,2);return function(){return b.apply(a,c.concat(L.call(arguments)))}}var M={init:function(b){if(/MSIE/.test(navigator.userAgent)&&!window.opera){var a=b||document;a.createElement("canvas");a.attachEvent("onreadystatechange",Y(this.init_,this,a))}},init_:function(b){b.namespaces.g_vml_||
	b.namespaces.add("g_vml_","urn:schemas-microsoft-com:vml","#default#VML");b.namespaces.g_o_||b.namespaces.add("g_o_","urn:schemas-microsoft-com:office:office","#default#VML");if(!b.styleSheets.ex_canvas_){var a=b.createStyleSheet();a.owningElement.id="ex_canvas_";a.cssText="canvas{display:inline-block;overflow:hidden;text-align:left;width:300px;height:150px}g_vml_\\:*{behavior:url(#default#VML)}g_o_\\:*{behavior:url(#default#VML)}"}var c=b.getElementsByTagName("canvas"),d=0;for(;d<c.length;d++)this.initElement(c[d])},
	initElement:function(b){if(!b.getContext){b.getContext=X;b.innerHTML="";b.attachEvent("onpropertychange",Z);b.attachEvent("onresize",$);var a=b.attributes;if(a.width&&a.width.specified)b.style.width=a.width.nodeValue+"px";else b.width=b.clientWidth;if(a.height&&a.height.specified)b.style.height=a.height.nodeValue+"px";else b.height=b.clientHeight}return b}};function Z(b){var a=b.srcElement;switch(b.propertyName){case "width":a.style.width=a.attributes.width.nodeValue+"px";a.getContext().clearRect();
	break;case "height":a.style.height=a.attributes.height.nodeValue+"px";a.getContext().clearRect();break}}function $(b){var a=b.srcElement;if(a.firstChild){a.firstChild.style.width=a.clientWidth+"px";a.firstChild.style.height=a.clientHeight+"px"}}M.init();var N=[],B=0;for(;B<16;B++){var C=0;for(;C<16;C++)N[B*16+C]=B.toString(16)+C.toString(16)}function I(){return[[1,0,0],[0,1,0],[0,0,1]]}function y(b,a){var c=I(),d=0;for(;d<3;d++){var f=0;for(;f<3;f++){var h=0,g=0;for(;g<3;g++)h+=b[d][g]*a[g][f];c[d][f]=
	h}}return c}function O(b,a){a.fillStyle=b.fillStyle;a.lineCap=b.lineCap;a.lineJoin=b.lineJoin;a.lineWidth=b.lineWidth;a.miterLimit=b.miterLimit;a.shadowBlur=b.shadowBlur;a.shadowColor=b.shadowColor;a.shadowOffsetX=b.shadowOffsetX;a.shadowOffsetY=b.shadowOffsetY;a.strokeStyle=b.strokeStyle;a.globalAlpha=b.globalAlpha;a.arcScaleX_=b.arcScaleX_;a.arcScaleY_=b.arcScaleY_;a.lineScale_=b.lineScale_}function P(b){var a,c=1;b=String(b);if(b.substring(0,3)=="rgb"){var d=b.indexOf("(",3),f=b.indexOf(")",d+
	1),h=b.substring(d+1,f).split(",");a="#";var g=0;for(;g<3;g++)a+=N[Number(h[g])];if(h.length==4&&b.substr(3,1)=="a")c=h[3]}else a=b;return{color:a,alpha:c}}function aa(b){switch(b){case "butt":return"flat";case "round":return"round";case "square":default:return"square"}}function H(b){this.m_=I();this.mStack_=[];this.aStack_=[];this.currentPath_=[];this.fillStyle=this.strokeStyle="#000";this.lineWidth=1;this.lineJoin="miter";this.lineCap="butt";this.miterLimit=k*1;this.globalAlpha=1;this.canvas=b;
	var a=b.ownerDocument.createElement("div");a.style.width=b.clientWidth+"px";a.style.height=b.clientHeight+"px";a.style.overflow="hidden";a.style.position="absolute";b.appendChild(a);this.element_=a;this.lineScale_=this.arcScaleY_=this.arcScaleX_=1}var i=H.prototype;i.clearRect=function(){this.element_.innerHTML=""};i.beginPath=function(){this.currentPath_=[]};i.moveTo=function(b,a){var c=this.getCoords_(b,a);this.currentPath_.push({type:"moveTo",x:c.x,y:c.y});this.currentX_=c.x;this.currentY_=c.y};
	i.lineTo=function(b,a){var c=this.getCoords_(b,a);this.currentPath_.push({type:"lineTo",x:c.x,y:c.y});this.currentX_=c.x;this.currentY_=c.y};i.bezierCurveTo=function(b,a,c,d,f,h){var g=this.getCoords_(f,h),l=this.getCoords_(b,a),e=this.getCoords_(c,d);Q(this,l,e,g)};function Q(b,a,c,d){b.currentPath_.push({type:"bezierCurveTo",cp1x:a.x,cp1y:a.y,cp2x:c.x,cp2y:c.y,x:d.x,y:d.y});b.currentX_=d.x;b.currentY_=d.y}i.quadraticCurveTo=function(b,a,c,d){var f=this.getCoords_(b,a),h=this.getCoords_(c,d),g={x:this.currentX_+
	0.6666666666666666*(f.x-this.currentX_),y:this.currentY_+0.6666666666666666*(f.y-this.currentY_)};Q(this,g,{x:g.x+(h.x-this.currentX_)/3,y:g.y+(h.y-this.currentY_)/3},h)};i.arc=function(b,a,c,d,f,h){c*=k;var g=h?"at":"wa",l=b+G(d)*c-v,e=a+F(d)*c-v,m=b+G(f)*c-v,r=a+F(f)*c-v;if(l==m&&!h)l+=0.125;var n=this.getCoords_(b,a),o=this.getCoords_(l,e),q=this.getCoords_(m,r);this.currentPath_.push({type:g,x:n.x,y:n.y,radius:c,xStart:o.x,yStart:o.y,xEnd:q.x,yEnd:q.y})};i.rect=function(b,a,c,d){this.moveTo(b,
	a);this.lineTo(b+c,a);this.lineTo(b+c,a+d);this.lineTo(b,a+d);this.closePath()};i.strokeRect=function(b,a,c,d){var f=this.currentPath_;this.beginPath();this.moveTo(b,a);this.lineTo(b+c,a);this.lineTo(b+c,a+d);this.lineTo(b,a+d);this.closePath();this.stroke();this.currentPath_=f};i.fillRect=function(b,a,c,d){var f=this.currentPath_;this.beginPath();this.moveTo(b,a);this.lineTo(b+c,a);this.lineTo(b+c,a+d);this.lineTo(b,a+d);this.closePath();this.fill();this.currentPath_=f};i.createLinearGradient=function(b,
	a,c,d){var f=new D("gradient");f.x0_=b;f.y0_=a;f.x1_=c;f.y1_=d;return f};i.createRadialGradient=function(b,a,c,d,f,h){var g=new D("gradientradial");g.x0_=b;g.y0_=a;g.r0_=c;g.x1_=d;g.y1_=f;g.r1_=h;return g};i.drawImage=function(b){var a,c,d,f,h,g,l,e,m=b.runtimeStyle.width,r=b.runtimeStyle.height;b.runtimeStyle.width="auto";b.runtimeStyle.height="auto";var n=b.width,o=b.height;b.runtimeStyle.width=m;b.runtimeStyle.height=r;if(arguments.length==3){a=arguments[1];c=arguments[2];h=g=0;l=d=n;e=f=o}else if(arguments.length==
	5){a=arguments[1];c=arguments[2];d=arguments[3];f=arguments[4];h=g=0;l=n;e=o}else if(arguments.length==9){h=arguments[1];g=arguments[2];l=arguments[3];e=arguments[4];a=arguments[5];c=arguments[6];d=arguments[7];f=arguments[8]}else throw Error("Invalid number of arguments");var q=this.getCoords_(a,c),t=[];t.push(" <g_vml_:group",' coordsize="',k*10,",",k*10,'"',' coordorigin="0,0"',' style="width:',10,"px;height:",10,"px;position:absolute;");if(this.m_[0][0]!=1||this.m_[0][1]){var E=[];E.push("M11=",
	this.m_[0][0],",","M12=",this.m_[1][0],",","M21=",this.m_[0][1],",","M22=",this.m_[1][1],",","Dx=",j(q.x/k),",","Dy=",j(q.y/k),"");var p=q,z=this.getCoords_(a+d,c),w=this.getCoords_(a,c+f),x=this.getCoords_(a+d,c+f);p.x=s.max(p.x,z.x,w.x,x.x);p.y=s.max(p.y,z.y,w.y,x.y);t.push("padding:0 ",j(p.x/k),"px ",j(p.y/k),"px 0;filter:progid:DXImageTransform.Microsoft.Matrix(",E.join(""),", sizingmethod='clip');")}else t.push("top:",j(q.y/k),"px;left:",j(q.x/k),"px;");t.push(' ">','<g_vml_:image src="',b.src,
	'"',' style="width:',k*d,"px;"," height:",k*f,'px;"',' cropleft="',h/n,'"',' croptop="',g/o,'"',' cropright="',(n-h-l)/n,'"',' cropbottom="',(o-g-e)/o,'"'," />","</g_vml_:group>");this.element_.insertAdjacentHTML("BeforeEnd",t.join(""))};i.stroke=function(b){var a=[],c=P(b?this.fillStyle:this.strokeStyle),d=c.color,f=c.alpha*this.globalAlpha;a.push("<g_vml_:shape",' filled="',!!b,'"',' style="position:absolute;width:',10,"px;height:",10,'px;"',' coordorigin="0 0" coordsize="',k*10," ",k*10,'"',' stroked="',
	!b,'"',' path="');var h={x:null,y:null},g={x:null,y:null},l=0;for(;l<this.currentPath_.length;l++){var e=this.currentPath_[l];switch(e.type){case "moveTo":a.push(" m ",j(e.x),",",j(e.y));break;case "lineTo":a.push(" l ",j(e.x),",",j(e.y));break;case "close":a.push(" x ");e=null;break;case "bezierCurveTo":a.push(" c ",j(e.cp1x),",",j(e.cp1y),",",j(e.cp2x),",",j(e.cp2y),",",j(e.x),",",j(e.y));break;case "at":case "wa":a.push(" ",e.type," ",j(e.x-this.arcScaleX_*e.radius),",",j(e.y-this.arcScaleY_*e.radius),
	" ",j(e.x+this.arcScaleX_*e.radius),",",j(e.y+this.arcScaleY_*e.radius)," ",j(e.xStart),",",j(e.yStart)," ",j(e.xEnd),",",j(e.yEnd));break}if(e){if(h.x==null||e.x<h.x)h.x=e.x;if(g.x==null||e.x>g.x)g.x=e.x;if(h.y==null||e.y<h.y)h.y=e.y;if(g.y==null||e.y>g.y)g.y=e.y}}a.push(' ">');if(b)if(typeof this.fillStyle=="object"){var m=this.fillStyle,r=0,n={x:0,y:0},o=0,q=1;if(m.type_=="gradient"){var t=m.x1_/this.arcScaleX_,E=m.y1_/this.arcScaleY_,p=this.getCoords_(m.x0_/this.arcScaleX_,m.y0_/this.arcScaleY_),
	z=this.getCoords_(t,E);r=Math.atan2(z.x-p.x,z.y-p.y)*180/Math.PI;if(r<0)r+=360;if(r<1.0E-6)r=0}else{var p=this.getCoords_(m.x0_,m.y0_),w=g.x-h.x,x=g.y-h.y;n={x:(p.x-h.x)/w,y:(p.y-h.y)/x};w/=this.arcScaleX_*k;x/=this.arcScaleY_*k;var R=s.max(w,x);o=2*m.r0_/R;q=2*m.r1_/R-o}var u=m.colors_;u.sort(function(ba,ca){return ba.offset-ca.offset});var J=u.length,da=u[0].color,ea=u[J-1].color,fa=u[0].alpha*this.globalAlpha,ga=u[J-1].alpha*this.globalAlpha,S=[],l=0;for(;l<J;l++){var T=u[l];S.push(T.offset*q+
	o+" "+T.color)}a.push('<g_vml_:fill type="',m.type_,'"',' method="none" focus="100%"',' color="',da,'"',' color2="',ea,'"',' colors="',S.join(","),'"',' opacity="',ga,'"',' g_o_:opacity2="',fa,'"',' angle="',r,'"',' focusposition="',n.x,",",n.y,'" />')}else a.push('<g_vml_:fill color="',d,'" opacity="',f,'" />');else{var K=this.lineScale_*this.lineWidth;if(K<1)f*=K;a.push("<g_vml_:stroke",' opacity="',f,'"',' joinstyle="',this.lineJoin,'"',' miterlimit="',this.miterLimit,'"',' endcap="',aa(this.lineCap),
	'"',' weight="',K,'px"',' color="',d,'" />')}a.push("</g_vml_:shape>");this.element_.insertAdjacentHTML("beforeEnd",a.join(""))};i.fill=function(){this.stroke(true)};i.closePath=function(){this.currentPath_.push({type:"close"})};i.getCoords_=function(b,a){var c=this.m_;return{x:k*(b*c[0][0]+a*c[1][0]+c[2][0])-v,y:k*(b*c[0][1]+a*c[1][1]+c[2][1])-v}};i.save=function(){var b={};O(this,b);this.aStack_.push(b);this.mStack_.push(this.m_);this.m_=y(I(),this.m_)};i.restore=function(){O(this.aStack_.pop(),
	this);this.m_=this.mStack_.pop()};function ha(b){var a=0;for(;a<3;a++){var c=0;for(;c<2;c++)if(!isFinite(b[a][c])||isNaN(b[a][c]))return false}return true}function A(b,a,c){if(!!ha(a)){b.m_=a;if(c)b.lineScale_=W(V(a[0][0]*a[1][1]-a[0][1]*a[1][0]))}}i.translate=function(b,a){A(this,y([[1,0,0],[0,1,0],[b,a,1]],this.m_),false)};i.rotate=function(b){var a=G(b),c=F(b);A(this,y([[a,c,0],[-c,a,0],[0,0,1]],this.m_),false)};i.scale=function(b,a){this.arcScaleX_*=b;this.arcScaleY_*=a;A(this,y([[b,0,0],[0,a,
	0],[0,0,1]],this.m_),true)};i.transform=function(b,a,c,d,f,h){A(this,y([[b,a,0],[c,d,0],[f,h,1]],this.m_),true)};i.setTransform=function(b,a,c,d,f,h){A(this,[[b,a,0],[c,d,0],[f,h,1]],true)};i.clip=function(){};i.arcTo=function(){};i.createPattern=function(){return new U};function D(b){this.type_=b;this.r1_=this.y1_=this.x1_=this.r0_=this.y0_=this.x0_=0;this.colors_=[]}D.prototype.addColorStop=function(b,a){a=P(a);this.colors_.push({offset:b,color:a.color,alpha:a.alpha})};function U(){}G_vmlCanvasManager=
	M;CanvasRenderingContext2D=H;CanvasGradient=D;CanvasPattern=U})();

}//	---------------------------------------------------------------------------
//	jWebSocket Chat PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Chat Client Plug-In
//	---------------------------------------------------------------------------

jws.ChatPlugIn = {

	// namespace for Chat plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.chat",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.ChatPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "login" == aToken.reqType ) {
				if( this.onChatRequestToken ) {
					this.onChatRequestToken( aToken );
				}
			}
		}
	},

	ChatLogin: function( aUsername, aPassword, aServer, aPort, aUseSSL, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// Chat API calls Chat Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.ChatPlugIn.NS,
				type: "login",
				username: aUsername,
				password: aPassword,
				server: aServer,
				port: aPort,
				useSSL: aUseSSL
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	ChatLogout: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.ChatPlugIn.NS,
				type: "logout"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setChatCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		/*
		if( aListeners.onChatRequestToken !== undefined ) {
			this.onChatRequestToken = aListeners.onChatRequestToken;
		}
		*/
	}

}

// add the JWebSocket Chat PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.ChatPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Client Gaming Plug-In
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Client Gaming Plug-In
//	---------------------------------------------------------------------------

jws.ClientGamingPlugIn = {

	// namespace for client gaming plugin
	// not yet used because here we use the system broadcast only
	NS: jws.NS_BASE + ".plugins.clientGaming",


	// this places a div on the current document when a new client connects
	addPlayer: function( aId, aUsername, aColor ) {
		aId = "player" + aId;
		var lDiv = document.getElementById( aId );
		if( !lDiv ) {
			lDiv = document.createElement( "div" );
		}
		lDiv.id = aId;
		lDiv.style.position = "absolute";
		lDiv.style.overflow = "hidden";
		lDiv.style.opacity = 0.85;
		lDiv.style.left = "100px";
		lDiv.style.top = "100px";
		lDiv.style.width = "75px";
		lDiv.style.height = "75px";
		lDiv.style.border = "1px solid black";
		lDiv.style.background = "url(img/player_" + aColor + ".png) 15px 18px no-repeat";
		lDiv.style.backgroundColor = aColor;
		lDiv.style.color = "white";
		lDiv.innerHTML = "<font style=\"font-size:8pt\">Player " + aUsername + "</font>";
		document.body.appendChild( lDiv );

		if( !this.players ) {
			this.players = {};
		}
		this.players[ aId ] = lDiv;
	},

	removeAllPlayers: function() {
		if( this.players ) {
			for( var lId in this.players) {
				document.body.removeChild( this.players[ lId ] );
			}
		}
		delete this.players;
	},

	// this removes a div from the document when a client logs out
	removePlayer: function( aId ) {
		aId = "player" + aId;
		var lDiv = document.getElementById( aId );
		if( lDiv ) {
			document.body.removeChild( lDiv );
			if( this.players ) {
				delete this.players[ aId ];
			}
		}
	},

	// this moves a div when the user presses one of the arrow keys
	movePlayer: function( aId, aX, aY ) {
		aId = "player" + aId;
		var lDiv = document.getElementById( aId );
		if( lDiv ) {
			lDiv.style.left = aX + "px";
			lDiv.style.top = aY + "px";
		}
	},

	// this method is called when the server connection was established
	processOpened: function( aToken ) {
		// console.log( "jws.ClientGamingPlugIn: Opened " + aToken.sourceId );

		// add own player to playground
		this.addPlayer( aToken.sourceId, aToken.sourceId, "green" );

		// broadcast an identify request to all clients to initialize game.
		aToken.ns = jws.SystemClientPlugIn.NS;
		aToken.type = "broadcast";
		aToken.request = "identify";
		this.sendToken( aToken );
	},

	// this method is called when the server connection was closed
	processClosed: function( aToken ) {
		// console.log( "jws.ClientGamingPlugIn: Closed " + aToken.sourceId );

		// if disconnected remove ALL players from playground
		this.removeAllPlayers();
	},

	// this method is called when another client connected to the network
	processConnected: function( aToken ) {
		// console.log( "jws.ClientGamingPlugIn: Connected " + aToken.sourceId );
		this.addPlayer( aToken.sourceId, aToken.sourceId, "red" );
	},

	// this method is called when another client disconnected from the network
	processDisconnected: function( aToken ) {
		// console.log( "jws.ClientGamingPlugIn: Disconnected " + aToken.sourceId );
		this.removePlayer( aToken.sourceId );
	},

	// this method processes an incomng token from another client or the server
	processToken: function( aToken ) {
		// Clients use system broadcast, so there's no special namespace here
		if( aToken.ns == jws.SystemClientPlugIn.NS ) {
			// process a move from another client
			var lX, lY;
			if( aToken.event == "move" ) {
				lX = aToken.x;
				lY = aToken.y;
				this.movePlayer( aToken.sourceId, lX, lY );
			// process a move from another client
			} else if( aToken.event == "identification" ) {
				this.addPlayer( aToken.sourceId, aToken.sourceId, "red" );
				lX = aToken.x;
				lY = aToken.y;
				this.movePlayer( aToken.sourceId, lX, lY );
			// process an identification request from another client
			} else if( aToken.request == "identify" ) {
				var lDiv = document.getElementById( "player" + this.getId() );
				lX = 100;
				lY = 100;
				if( lDiv ) {
					lX = parseInt( lDiv.style.left );
					lY = parseInt( lDiv.style.top );
				}
				var lToken = {
					ns: jws.SystemClientPlugIn.NS,
					type: "broadcast",
					event: "identification",
					x: lX,
					y: lY,
					username: this.getUsername()
				}
				this.sendToken( lToken );
			}
		}
	},

	// this method broadcasts a token to all other clients on the server
	broadcastGamingEvent: function( aToken, aOptions ) {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			// use name space of system plug in here because 
			// there's no server side plug-in for the client-pluh-in
			aToken.ns = jws.SystemClientPlugIn.NS;
			aToken.type = "broadcast";
			aToken.event = "move";
			// explicitely include sender,
			// default is false on the server
			aToken.senderIncluded = true;
			// do not need a response here, save some time ;-)
			aToken.responseRequested = false;
			aToken.username = this.getUsername();
			this.sendToken( aToken, aOptions );
		}
		return lRes;
	}

};

// add the JWebSocket Client Gaming PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.ClientGamingPlugIn );
//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------

//:file:*:jwsEventsPlugIn.js
//:d:en:Implements the EventsPlugIn in the client side

//:package:*:jws
//:class:*:jws.EventsNotifier
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsNotifier[/tt] class. _
//:d:en:This class handle raw events notifications to/from the server side.
jws.oop.declareClass( "jws", "EventsNotifier", null, {
	jwsClient: {}
	,
	filterChain: []
	,
	plugIns: []
	,
	//:m:*:initialize
	//:d:en:Initialize this component. 
	//:a:en::::none
	//:r:*:::void:none
	initialize : function(){
		jws.oop.addPlugIn(jws.jWebSocketTokenClient, this);
	}
	,
	//:m:*:notify
	//:d:en:Notify an event in the server side
	//:a:en::aEventName:String:The event name.
	//:a:en::aOptions:Object:Contains the event arguments and the OnResponse, OnSuccess and OnFailure callbacks.
	//:r:*:::void:none
	notify: function(aEventName, aOptions){
		if (this.jwsClient.isConnected()){
			var lToken = {};
			if (aOptions.args){
				lToken = aOptions.args;
				delete (aOptions.args);
			}
			lToken.type      = aEventName;
			lToken._IS_EM_   = true;
			
			//Generating the unique token identifier
			aOptions._tokenUID = hex_md5(lToken);

			var aOnResponseObject = new jws.OnResponseObject();
			aOnResponseObject.request = aOptions;
			aOnResponseObject.filterChain = this.filterChain;

			if (undefined != aOptions.eventDefinition){
				for (var i = 0; i < this.filterChain.length; i++){
					try
					{
						this.filterChain[i].beforeCall(lToken, aOnResponseObject);
					}
					catch(err)
					{
						switch (err)
						{
							case "stop_filter_chain":
								return;
							break;
							default:
								throw err;
							break;
						}
					}
					
				}
			}

			this.jwsClient.sendToken(lToken, aOnResponseObject);
		}
		else
			throw "client:not_connected";
    }
	,
	//:m:*:processToken
	//:d:en:Processes an incoming token. Used to support S2C events notifications. _
	//:d:en:Use the "event_name" and "plugin_id" information to execute _
	//:d:en:a targered method in a plug-in.
	//:a:en::aToken:Object:Token to be processed
	//:r:*:::void:none
	processToken: function (aToken) {
		if ("s2c.event_notification" == aToken.type){
			var event_name = aToken.event_name;
			var plugin_id = aToken.plugin_id;

			if (undefined != this.plugIns[plugin_id] && undefined != this.plugIns[plugin_id][event_name]){
				result = this.plugIns[plugin_id][event_name](aToken);
				
				//Sending response back to the server
				if (undefined != aToken.response_type){
					this.notify("s2c.onresponse", {
						args: {
							req_id: aToken.uid,
							response: result
						}
					});
				}
			}
			else {
				//Sending the "not supported" event notification
				this.notify("s2c.event_not_supported", {
					args: {
						req_id: aToken.uid,
					}
				});
				throw "s2c_event_support_not_found:" + event_name;
			}
		}
	}
});

//:package:*:jws
//:class:*:jws.OnResponseObject
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.OnResponseObject[/tt] class. _
//:d:en:This class offer support for the "OnSuccess" and "OnFailure" callbacks
jws.oop.declareClass( "jws", "OnResponseObject", null, {
	request: {}
	,
	filterChain: []
	,
	OnResponse: function(aResponseToken){
		if (undefined != this.request.eventDefinition){
			var index = this.filterChain.length - 1;
			while (index > -1){
				try
				{
					this.filterChain[index].afterCall(this.request, aResponseToken);
				}
				catch(err)
				{
					switch (err)
					{
						case "stop_filter_chain":
							return;
						break;
						default:
							throw err;
						break;
					}
				}
				index--;
			}
		}
		
		if (aResponseToken.code == 0){
			if (undefined != this.request.OnResponse)
				this.request.OnResponse(aResponseToken);

			if (undefined != this.request.OnSuccess)
				this.request.OnSuccess(aResponseToken);
		}
		else {
			if (undefined != this.request.OnResponse)
				this.request.OnResponse(aResponseToken);

			if (undefined != this.request.OnFailure)
				this.request.OnFailure(aResponseToken);
		}
    }
});

//:package:*:jws
//:class:*:jws.EventsPlugInGenerator
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsPlugInGenerator[/tt] class. _
//:d:en:This class handle the generation of server plug-ins as _
//:d:en:Javascript objects.
jws.oop.declareClass( "jws", "EventsPlugInGenerator", null, {

	//:m:*:generate
	//:d:en:Processes an incoming token. Used to support S2C events notifications. _
	//:a:en::aPlugInId:String:Remote plug-in "id" to generate in the client.
	//:a:en::aNotifier:jws.EventsNotifier:The event notifier used to connect with the server.
	//:a:en::OnReady:Function:This callback is called when the plug-in has been generated.
	//:r:*:::void:none
	generate: function(aPlugInId, aNotifier, OnReady){
		var plugIn = new jws.EventsPlugIn();
		plugIn.notifier = aNotifier;

		aNotifier.notify("plugin.getapi", {
			args: {
				plugin_id: aPlugInId
			}
			,
			plugIn: plugIn
			,
			OnReady: OnReady
			,
			OnSuccess: function(aResponseToken){
				this.plugIn.id = aResponseToken.id;
				this.plugIn.plugInAPI = aResponseToken.api;

				//Generate the plugin here
				for (method in aResponseToken.api){
					eval("this.plugIn." + method + "=function(aOptions){if (undefined == aOptions){aOptions = {};};var eventName=this.plugInAPI."+method+".type; aOptions.eventDefinition=this.plugInAPI."+ method + "; this.notifier.notify(eventName, aOptions);}")
				}

				//Registering the plugin in the notifier
				this.plugIn.notifier.plugIns[this.plugIn.id] = this.plugIn;

				//Plugin is ready to use
				this.OnReady(this.plugIn);
			}
			,
			OnFailure: function(aResponseToken){
				throw aResponseToken.msg;
			}	
		});

		return plugIn;
	}
});

//:package:*:jws
//:class:*:jws.EventsPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsPlugIn[/tt] class. _
//:d:en:This class represents an abstract client plug-in. The methods are _
//:d:en:generated in runtime.
jws.oop.declareClass( "jws", "EventsPlugIn", null, {
	id: ""
	,
	notifier: {}
	,
	plugInAPI: {}
	
	//Methods are generated in runtime!
	//Custom methods can be added using the OnReady callback
});

//:package:*:jws
//:class:*:jws.EventsBaseFilter
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsBaseFilter[/tt] class. _
//:d:en:This class represents an abstract client filter.
jws.oop.declareClass( "jws", "EventsBaseFilter", null, {

	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){}
	,
	//:m:*:afterCall
	//:d:en:This method is called after every C2S event notification.
	//:a:en::aRequest:Object:The request to be filtered.
	//:a:en::aResponseToken:Object:The response token from the server.
	//:r:*:::void:none
	afterCall: function(aRequest, aResponseToken){}
});

//:package:*:jws
//:class:*:jws.SecurityFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.SecurityFilter[/tt] class. _
//:d:en:This class handle the security for every C2S event notification _
//:d:en:in the client, using the server side security configuration.
jws.oop.declareClass( "jws", "SecurityFilter", jws.EventsBaseFilter, {
	user:[]
	,
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks that the logged in user has the correct roles to notify _
	//:d:en:a custom event in the server.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){
		if (aOnResponseObject.request.eventDefinition.isSecurityEnabled){
			var r, u;
			var roles, users = null;
			var exclusion = false;
			var role_authorized = false;
			var user_authorized = false;
			var stop = false;
			
			//@TODO: Support IP addresses restrictions checks on the JS client

			//Getting users restrictions
			users = aOnResponseObject.request.eventDefinition.users;

			//Getting roles restrictions
			roles = aOnResponseObject.request.eventDefinition.roles;

			//Checking if the user have the allowed roles
			if (users.length > 0){
				var user_match = false;
				for (var i = 0; i < users.length; i++){
					u = users[i];
					
					if ("all" != u){
						exclusion = (u.substring(0,1) == "!") ? true : false;
						u = (exclusion) ? u.substring(1) : u;

						if (u == this.user.username){
							user_match = true;
							if (!exclusion){
								user_authorized = true;
							}
							break;
						}
					} else {
						user_match = true;
						user_authorized = true;
						break;
					}
				}

				//Not Authorized USER
				if (!user_authorized && user_match || 0 == roles.length){
					aOnResponseObject.OnResponse({
						code: -1,
						msg: "Not autorized to notify this event. USER restrictions: " + users.toString()
					});
					this.OnNotAuthorized(aToken);
					throw "stop_filter_chain";
				}
			}

			//Checking if the user have the allowed roles
			if (roles.length > 0){
				for (var i = 0; i < roles.length; i++){
					for (var j = 0; j < this.user.roles.length; j++){
						r = roles[i];
					
						if ("all" != r){
							exclusion = (r.substring(0,1) == "!") ? true : false;
							r = (exclusion) ? r.substring(1) : r;

							if (r == this.user.roles[j]){
								if (!exclusion){
									role_authorized = true;
								}
								stop = true;
								break;
							}
						} else {
							role_authorized = true;
							stop = true;
							break;
						}	
					}
					if (stop){
						break;
					}
				}

				//Not Authorized ROLE
				if (!role_authorized){
					aOnResponseObject.OnResponse({
						code: -1,
						msg: "Not autorized to notify this event. ROLE restrictions: " + roles.toString()
					});
					this.OnNotAuthorized(aToken);
					throw "stop_filter_chain";
				}
			}
		}
	}
	,
	//:m:*:OnNotAuthorized
	//:d:en:This method is called when a "not authorized" event notification _
	//:d:en:is detected. Allows to define a global behiavor for this kind _
	//:d:en:of exception.
	//:a:en::aToken:Object:The "not authorized" token to be processed.
	//:r:*:::void:none
	OnNotAuthorized: function(aToken){
		throw "not_authorized";
	}
});

//:package:*:jws
//:class:*:jws.CacheFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.CacheFilter[/tt] class. _
//:d:en:This class handle the cache for every C2S event notification _
//:d:en:in the client, using the server side cache configuration.
jws.oop.declareClass( "jws", "CacheFilter", jws.EventsBaseFilter, {
	cache:{}
	,
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks if exist a non-expired cached response for the outgoing event. _
	//:d:en:If TRUE, the cached response is used and the server is not notified.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){
		if (aOnResponseObject.request.eventDefinition.isCacheEnabled){
			var cachedResponseToken = this.cache.getItem(aOnResponseObject.request._tokenUID);
			if (null != cachedResponseToken){
				aOnResponseObject.OnResponse(cachedResponseToken);
				throw "stop_filter_chain";
			}
		}
	}
	,
	//:m:*:afterCall
	//:d:en:This method is called after every C2S event notification. _
	//:d:en:Checks if a response needs to be cached. The server configuration _
	//:d:en:for cache used.
	//:a:en::aRequest:Object:The request to be filtered.
	//:a:en::aResponseToken:Object:The response token from the server.
	//:r:*:::void:none
	afterCall: function(aRequest, aResponseToken){
		if (aRequest.eventDefinition.isCacheEnabled){
			this.cache.setItem(aRequest._tokenUID, aResponseToken, {
				expirationAbsolute: null,
				expirationSliding: aRequest.eventDefinition.cacheTime,
				priority: jws.cache.CachePriority.High
			});
		}
	}
});

//:package:*:jws
//:class:*:jws.ValidatorFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.ValidatorFilter[/tt] class. _
//:d:en:This class handle the validation for every argument in the request.
jws.oop.declareClass( "jws", "ValidatorFilter", jws.EventsBaseFilter, {

	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks if the request arguments match with the validation server rules.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aOnResponseObject:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aOnResponseObject){
		var arguments = aOnResponseObject.request.eventDefinition.incomingArgsValidation;
		
		for (var index = 0; index < arguments.length; index++){
			if (!aToken[arguments[index].name] && !arguments[index].optional){
				aOnResponseObject.OnResponse({
					code: -1,
					msg: "Argument '"+arguments[index].name+"' is required!"
				});
				throw "stop_filter_chain";
			}else if (aToken.hasOwnProperty(arguments[index].name)){
				var requiredType = arguments[index].type;
				if (requiredType != typeof(aToken[arguments[index].name])){
					//Supporting 'array' as types too
					if ("array" == requiredType && aToken[arguments[index].name] instanceof Array){
						return;
					}

					aOnResponseObject.OnResponse({
						code: -1,
						msg: "Argument '"+arguments[index].name+"' has invalid type. Required: '"+requiredType+"'"
					});
					throw "stop_filter_chain";
				}
			}
		}
	}
});


//	---------------------------------------------------------------------------
//	jWebSocket Filesyste, Client PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Filesystem Client Plug-In
//	---------------------------------------------------------------------------

jws.FileSystemPlugIn = {

	// namespace for filesystem plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.filesystem",

	NOT_FOUND_ERR: 1,
	SECURITY_ERR: 2,
	ABORT_ERR: 3,
	NOT_READABLE_ERR: 4,
	ENCODING_ERR: 5,
	NO_MODIFICATION_ALLOWED_ERR: 6,
	INVALID_STATE_ERR: 7,
	SYNTAX_ERR: 8,
	INVALID_MODIFICATION_ERR: 9,
	QUOTA_EXCEEDED_ERR: 10,
	TYPE_MISMATCH_ERR: 11,
	PATH_EXISTS_ERR: 12,

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.FileSystemPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "load" == aToken.reqType ) {
				if( aToken.code == 0 ) {
					aToken.data = Base64.decode( aToken.data );
					if( this.OnFileLoaded ) {
						this.OnFileLoaded( aToken );
					}
				} else {
					if( this.OnFileError ) {
						this.OnFileError( aToken );
					}
				}
			} else if( "event" == aToken.type ) {
				if( "filesaved" == aToken.name ) {
					if( this.OnFileSaved ) {
						this.OnFileSaved( aToken );
					}
				} else if( "filesent" == aToken.name ) {
					if( this.OnFileSent ) {
						this.OnFileSent( aToken );
					}
				}
			}
		}
	},

	fileLoad: function( aFilename, aOptions ) {
		var lRes = this.createDefaultResult();
		var lScope = jws.SCOPE_PRIVATE;

		if( aOptions ) {
			if( aOptions.scope != undefined ) {
				lScope = aOptions.scope;
			}
		}
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.FileSystemPlugIn.NS,
				type: "load",
				scope: lScope,
				filename: aFilename
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	fileSave: function( aFilename, aData, aOptions ) {
		var lRes = this.createDefaultResult();
		var lEncoding = "base64";
		var lSuppressEncoder = false;
		var lScope = jws.SCOPE_PRIVATE;
		if( aOptions ) {
			if( aOptions.scope != undefined ) {
				lScope = aOptions.scope;
			}
			if( aOptions.encoding != undefined ) {
				lEncoding = aOptions.encoding;
			}
			if( aOptions.suppressEncoder != undefined ) {
				lSuppressEncoder = aOptions.suppressEncoder;
			}
		}
		if( !lSuppressEncoder ) {
			if( lEncoding == "base64" ) {
				aData = Base64.encode( aData );
			}
		}
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.FileSystemPlugIn.NS,
				type: "save",
				scope: lScope,
				encoding: lEncoding,
				notify: true,
				data: aData,
				filename: aFilename
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	fileSend: function( aTargetId, aFilename, aData, aOptions ) {
		var lEncoding = "base64";
		if( aOptions ) {
			if( aOptions.encoding != undefined ) {
				lEncoding = aOptions.encoding;
			}
		}
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			this.sendToken({
				ns: jws.FileSystemPlugIn.NS,
				type: "send",
				data: aData,
				targetId: aTargetId,
				encoding: lEncoding,
				filename: aFilename
			});
		}
		return lRes;
	},

	fileGetErrorMsg: function( aCode ) {
		var lMsg = "unkown";
		switch( aCode ) {
			case jws.FileSystemPlugIn.NOT_FOUND_ERR: {
				lMsg = "NOT_FOUND_ERR";
				break;
			}
			case jws.FileSystemPlugIn.SECURITY_ERR: {
				lMsg = "SECURITY_ERR";
				break;
			}
			case jws.FileSystemPlugIn.ABORT_ERR: {
				lMsg = "ABORT_ERR";
				break;
			}
			case jws.FileSystemPlugIn.NOT_READABLE_ERR: {
				lMsg = "NOT_READABLE_ERR";
				break;
			}
			case jws.FileSystemPlugIn.ENCODING_ERR: {
				lMsg = "ENCODING_ERR";
				break;
			}
			case jws.FileSystemPlugIn.NO_MODIFICATION_ALLOWED_ERR: {
				lMsg = "NO_MODIFICATION_ALLOWED_ERR";
				break;
			}
			case jws.FileSystemPlugIn.INVALID_STATE_ERR: {
				lMsg = "INVALID_STATE_ERR";
				break;
			}
			case jws.FileSystemPlugIn.SYNTAX_ERR: {
				lMsg = "SYNTAX_ERR";
				break;
			}
			case jws.FileSystemPlugIn.INVALID_MODIFICATION_ERR: {
				lMsg = "INVALID_MODIFICATION_ERR";
				break;
			}
			case jws.FileSystemPlugIn.QUOTA_EXCEEDED_ERR: {
				lMsg = "QUOTA_EXCEEDED_ERR";
				break;
			}
			case jws.FileSystemPlugIn.TYPE_MISMATCH_ERR: {
				lMsg = "TYPE_MISMATCH_ERR";
				break;
			}
			case jws.FileSystemPlugIn.PATH_EXISTS_ERR: {
				lMsg = "PATH_EXISTS_ERR";
				break;
			}
		}
		return lMsg;
	},

	//:author:*:Unni Vemanchery Mana:2011-02-17:Incorporated image processing capabilities.
	//:m:*:fileLoadLocal
	//:d:en:This is a call back method which gets the number of files selected from the user.
	//:d:en:Construts a FileReader object that is specified in HTML 5 specification
	//:d:en:Finally calls its readAsDataURL with the filename obeject and reads the
	//:d:en:file content in Base64 encoded string.
	//:a:en::evt:Object:File Selection event object.
	//:r:*:::void:none
	fileLoadLocal: function( aDOMElem, aOptions ) {
		// to locally load a file no check for websocket connection is required
		var lRes = {
			code: 0,
			msg: "ok"
		};
		// check if the file upload element exists at all
		if( !aDOMElem || !aDOMElem.files ) {
			// TODO: Think about error message here!
			return {
				code: -1,
				msg: "No input file element passed."
			};
		}
		// check if the browser already supports the HTML5 File API
		if( undefined == window.FileReader ) {
			return {
				code: -1,
				msg: "Your browser does not yet support the HTML5 File API."
			};
		}
		// create options if not passed (eg. encoding)
		if( !aOptions ) {
			aOptions = {};
		}
		// if no encoding was passed and a default one
		if( !aOptions.encoding ) {
			aOptions.encoding = "base64";
		}
		// iterate through list of files
		var lFileList = aDOMElem.files;
		if( !lFileList || !lFileList.length ) {
			return {
				code: -1,
				msg: "No files selected."
			};
		}
		for( var lIdx = 0, lCnt = lFileList.length; lIdx < lCnt; lIdx++ ) {
			var lFile = lFileList[ lIdx ]
			var lReader = new FileReader();
			var lThis = this;

			// if file is completely loaded, fire OnLocalFileRead event
			lReader.onload = (function( aFile ) {
				return function( aEvent ) {
					if( lThis.OnLocalFileRead ) {
						var lToken = {
							encoding: aOptions.encoding,
							fileName : aFile.fileName,
							fileSize: aFile.fileSize,
							type: aFile.type,
							lastModified: aFile.lastModifiedDate,
							data: aEvent.target.result
						};
						if( aOptions.args ) {
							lToken.args = aOptions.args;
						}
						if( aOptions.action ) {
							lToken.action = aOptions.action;
						}
						lThis.OnLocalFileRead( lToken );
					}
				}
			})( lFile );

			// if any error appears fire OnLocalFileError event
			lReader.onerror = (function( aFile ) {
				return function( aEvent ) {
					if( lThis.OnLocalFileError ) {
						// TODO: force error case and fill token
						var lCode = aEvent.target.error.code;
						var lToken = {
							code: lCode,
							msg: lThis.fileGetErrorMsg( lCode )
						};
						if( aOptions.args ) {
							lToken.args = aOptions.args;
						}
						if( aOptions.action ) {
							lToken.action = aOptions.action;
						}
						lThis.OnLocalFileError( lToken );
					}
				}
			})( lFile );

			// and finally read the file(s)
			try{
				lReader.readAsDataURL( lFile );
			} catch( lEx ) {
				if( lThis.OnLocalFileError ) {
					var lToken = {
						code: -1,
						msg: lEx.message
					};
					if( aOptions.args ) {
						lToken.args = aOptions.args;
					}
					if( aOptions.action ) {
						lToken.action = aOptions.action;
					}
					lThis.OnLocalFileError( lToken );
				}
			}
		}
		return lRes;
	},

	setFileSystemCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnFileLoaded !== undefined ) {
			this.OnFileLoaded = aListeners.OnFileLoaded;
		}
		if( aListeners.OnFileSaved !== undefined ) {
			this.OnFileSaved = aListeners.OnFileSaved;
		}
		if( aListeners.OnFileSent !== undefined ) {
			this.OnFileSent = aListeners.OnFileSent;
		}
		if( aListeners.OnFileError !== undefined ) {
			this.OnFileError = aListeners.OnFileError;
		}

		if( aListeners.OnLocalFileRead !== undefined ) {
			this.OnLocalFileRead = aListeners.OnLocalFileRead;
		}
		if( aListeners.OnLocalFileError !== undefined ) {
			this.OnLocalFileError = aListeners.OnLocalFileError;
		}
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.FileSystemPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Sample Client PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Sample Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.JDBCPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.JDBCPlugIn[/tt] class.
jws.JDBCPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.jdbc (jws.NS_BASE + ".plugins.jdbc")
	//:d:en:Namespace for the [tt]JDBCPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.jdbc",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.JDBCPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "select" == aToken.reqType ) {
				if( this.OnJDBCResult ) {
					this.OnJDBCResult( aToken );
				}
			}
		}
	},

	jdbcSelect: function( aQuery, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "select",
				table: aQuery.table,
				fields: aQuery.fields,
				order: aQuery.order,
				where: aQuery.where,
				group: aQuery.group,
				having: aQuery.having
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	setJDBCCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnJDBCResult !== undefined ) {
			this.OnJDBCResult = aListeners.OnJDBCResult;
		}
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.JDBCPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Mail PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Mail Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.MailPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.MailPlugIn[/tt] class.
jws.MailPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.mail (jws.NS_BASE + ".plugins.mail")
	//:d:en:Namespace for the [tt]MailPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.mail",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.MailPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "sendMail" == aToken.reqType ) {
				if( this.OnMailSent ) {
					this.OnMailSent( aToken );
				}
			}
		}
	},

	sendMail: function( aFrom, aTo, aCC, aBCC, aSubject, aBody, aIsHTML, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.MailPlugIn.NS,
				type: "sendMail",
				from: aFrom,
				to: aTo,
				cc: aCC,
				bcc: aBCC,
				subject: aSubject,
				body: aBody,
				isHTML: aIsHTML
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	setMailCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnMailSent !== undefined ) {
			this.OnMailSent = aListeners.OnMailSent;
		}
	}

}

// add the JWebSocket Mail PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.MailPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Mail RPC/RRPC  (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	Authors: Alexander Schulze, Quentin Ambard
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket RPC/RRPC Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.RPCClientPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.RPCClientPlugIn[/tt] class.
jws.RPCClientPlugIn = {

	// granted rrpc's
	grantedProcs: [],

	// granted rrpc's
	spawnThreadDefault: false,

	//:const:*:NS:String:org.jwebsocket.plugins.rpc (jws.NS_BASE + ".plugins.rpc")
	//:d:en:Namespace for the [tt]RPCClientPlugIn[/tt] class.
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.rpc",

	//:m:*:setSpawnThreadDefault
	//:d:en:set the default value of the spawnThread option
	//:a:en::aDefault:Boolean.
	//:r:*:::void:none
	setSpawnThreadDefault: function (aDefault) {
		this.spawnThreadDefault = aDefault;
	},

	//:m:*:addGrantedProcedure
	//:d:en:grant the access to a rrpc procedure
	//:a:en::aProcedure:String procedure name (including name space).
	//:r:*:::void:none
	addGrantedProcedure: function (aProcedure) {
		jws.RPCClientPlugIn.grantedProcs[ jws.RPCClientPlugIn.grantedProcs.length ] = aProcedure;
	},

	//:m:*:removeGrantedProcedure
	//:d:en:remove the access to a rrpc procedure
	//:a:en::aProcedure:String procedure name (including name space).
	//:r:*:::void:none
	removeGrantedProcedure: function (aProcedure) {
		var lIdx = jws.RPCClientPlugIn.grantedProcs.indexOf( aProcedure );
		if( lIdx >= 0 ) {
			jws.RPCClientPlugIn.grantedProcs.splice( lIdx, 1 );
		}
	},

	//:m:*:processToken
	//:d:en:Processes an incoming token from the server or a remote client. _
	//:d:en:Here the token is checked for type [tt]rrpc[/tt]. If such is _
	//:d:en:detected it gets processed by the [tt]onRRPC[/tt] method of this class.
	//:a:en::aToken:Object:Token received from the server or a remote client.
	//:r:*:::void:none
	processToken: function( aToken ) {
		// console.log( "jws.RPCClientPlugIn: Processing token " + aToken.ns + "/" + aToken.type + "..." );
		if( aToken.ns == jws.RPCClientPlugIn.NS ) {
			if( aToken.type == "rrpc" ) {
				this.onRRPC( aToken );
			}
		}
	},

	//:m:*:rpc
	//:d:en:Runs a remote procedure call (RPC) on the jWebSocket server. _
	//:d:en:The security mechanisms on the server require the call to be _
	//:d:en:granted, otherwise it gets rejected.
	//:a:en::aClass:String:Class of the method that is supposed to be called.
	//:a:en::aMthd:String:Name of the method that is supposed to be called.
	//:a:en::aArgs:Array:Arguments for method that is supposed to be called. Should always be an array, but also works with simple values. Caution with a simple array as parameter (args mus be: [[1,2..]]).
	//:a:en::aOptions:Object:Optional arguments. For details please refer to the [tt]sendToken[/tt] method.
	//:r:*:::void:none
	rpc: function( aClass, aMthd, aArgs, aOptions) {
		if (aArgs != null && !(aArgs instanceof Array)) {
			aArgs = [aArgs];
		}
		aOptions = this.setDefaultOption (aOptions) ;
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.RPCClientPlugIn.NS,
				type: "rpc",
				classname: aClass,
				method: aMthd,
				args: aArgs
				},
				aOptions
			);
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	setDefaultOption: function( aOptions ) {
		if (aOptions === undefined) {
			aOptions = {} ;
		}
		if (aOptions.spawnThread === undefined) {
			aOptions.spawnThread = this.spawnThreadDefault;
		}
		return aOptions ;
	},

	//:m:*:rrpc
	//:d:en:Runs a reverse remote procedure call (RRPC) on another client.
	//:a:en::aTarget:String:Id of the target remote client.
	//:a:en::aClass:String:Class of the method that is supposed to be called.
	//:a:en::aMthd:String:Name of the method that is supposed to be called.
	//:a:en::aArgs:Array:Arguments for method that is supposed to be called.
	//:a:en::aOptions:Object:Optional arguments. For details please refer to the [tt]sendToken[/tt] method.
	//:r:*:::void:none
	rrpc: function( aTarget, aClass, aMthd, aArgs, aOptions ) {
		if (aArgs != null && !(aArgs instanceof Array)) {
			aArgs = [aArgs];
		}
		aOptions = this.setDefaultOption (aOptions) ;
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.RPCClientPlugIn.NS,
				type: "rrpc",
				targetId: aTarget,
				classname: aClass,
				method: aMthd,
				args: aArgs
			},
			aOptions
			);
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:onRRPC
	//:d:en:Processes a remote procedure call from another client. _
	//:d:en:This method is called internally only and should not be invoked _
	//:d:en:by the application.
	//:a:en::aToken:Object:Token that contains the rrpc arguments from the source client.
	//:r:*:::void:none
	onRRPC: function( aToken ) {
		var lClassname = aToken.classname;
		var lMethod = aToken.method;
		var lArgs = aToken.args;
		var lPath = lClassname + "." + lMethod;
		// check if the call is granted on this client
		if( jws.RPCClientPlugIn.grantedProcs.indexOf( lPath ) >= 0 ) {
			var lFunctionSplit = lClassname.split( '.' );
			var lFunctionSplitSize = lFunctionSplit.length;
			var lTheFunction = window[ lFunctionSplit[ 0 ] ] ;
			for( var j = 1; j < lFunctionSplitSize; j++ ) {
				lTheFunction = lTheFunction[ lFunctionSplit[ j ] ];
			}
			var lRes;
			try {
				lRes = lTheFunction[ lMethod ].apply( null, lArgs);
			} catch (ex) {
				//TODO: send back the error under a better format
				lRes = ex
					+ "\nProbably a typo error (method called="
					+ lMethod
					+ ") or wrong number of arguments (args: "
					+ JSON.stringify(lArgs)
					+ ")";
			}
		} else {
			//TODO: send back the error under a better format
			lRes =
			+ "\nAcces not granted to the="
			+ lMethod;
		}
		this.sendToken({
				// ns: jws.SystemPlugIn.NS,
				type: "send",
				targetId: aToken.sourceId,
				result: lRes,
				reqType: "rrpc",
				code: 0
			},null // aOptions
		);
	}
}

// add the JWebSocket RPC PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.RPCClientPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Sample Client PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Sample Client Plug-In
//	---------------------------------------------------------------------------

jws.SamplesPlugIn = {

	// namespace for shared objects plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.samples",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.SamplesPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "requestServerTime" == aToken.reqType ) {
				// this is just for demo purposes
				// don't use blocking calls here which block the communication!
				// like alert( "jWebSocket Server returned: " + aToken.time );
				if( this.OnSamplesServerTime ) {
					this.OnSamplesServerTime( aToken );
				}
			}
		}
	},

	requestServerTime: function( aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.SamplesPlugIn.NS,
				type: "requestServerTime"
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	setSamplesCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnSamplesServerTime !== undefined ) {
			this.OnSamplesServerTime = aListeners.OnSamplesServerTime;
		}
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SamplesPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Shared Objects PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Shared Objects Plug-In
//	---------------------------------------------------------------------------

jws.SharedObjectsPlugIn = {

	// namespace for shared objects plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.sharedObjs",
	// if data types are changed update server plug-in accordingly!
	DATA_TYPES: [ "number", "string", "boolean", "object", "set", "list", "map", "table" ],

	fObjects: {},

	processToken: function( aToken ) {
		// console.log( "jws.SharedObjectsPlugIn: Processing token " + aToken.ns + "/" + aToken.type + "..." );
		if( aToken.ns == jws.SharedObjectsPlugIn.NS ) {
			if( aToken.name == "created" ) {
				// create a new object on the client
				if( this.OnSharedObjectCreated ) {
					this.OnSharedObjectCreated( aToken );
				}
			} else if( aToken.name == "destroyed" ) {
				// destroy an existing object on the client
				if( this.OnSharedObjectDestroyed ) {
					this.OnSharedObjectDestroyed( aToken );
				}
			} else if( aToken.name == "updated" ) {
				// update an existing object on the client
				if( this.OnSharedObjectUpdated ) {
					this.OnSharedObjectUpdated( aToken );
				}
			} else if( aToken.name == "init" ) {
				// init all shared object on the client
				if( this.OnSharedObjectsInit ) {
					var lObj = JSON.parse( aToken.value );
					this.OnSharedObjectsInit( aToken, lObj );
				}
			}
		}
	},

	createSharedObject: function( aId, aDataType, aValue, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.SharedObjectsPlugIn.NS,
				type: "create",
				id: aId,
				datatype: aDataType,
				value: aValue
			};
			this.sendToken( lToken,	aOptions );
			if( this.OnSharedObjectCreated ) {
				this.OnSharedObjectCreated( lToken );
			}
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	destroySharedObject: function( aId, aDataType, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.SharedObjectsPlugIn.NS,
				type: "destroy",
				id: aId,
				datatype: aDataType
			};
			this.sendToken( lToken, aOptions );
			if( this.OnSharedObjectDestroyed ) {
				this.OnSharedObjectDestroyed( lToken );
			}
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	getSharedObject: function( aId, aDataType, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.SharedObjectsPlugIn.NS,
				type: "get",
				id: aId,
				datatype: aDataType
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	updateSharedObject: function( aId, aDataType, aValue, aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.SharedObjectsPlugIn.NS,
				type: "update",
				id: aId,
				datatype: aDataType,
				value: aValue
			};
			this.sendToken( lToken,	aOptions );
			if( this.OnSharedObjectUpdated ) {
				this.OnSharedObjectUpdated( lToken );
			}
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	setSharedObjectsCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnSharedObjectCreated !== undefined ) {
			this.OnSharedObjectCreated = aListeners.OnSharedObjectCreated;
		}
		if( aListeners.OnSharedObjectDestroyed !== undefined ) {
			this.OnSharedObjectDestroyed = aListeners.OnSharedObjectDestroyed;
		}
		if( aListeners.OnSharedObjectUpdated !== undefined ) {
			this.OnSharedObjectUpdated = aListeners.OnSharedObjectUpdated;
		}
		if( aListeners.OnSharedObjectsInit !== undefined ) {
			this.OnSharedObjectsInit = aListeners.OnSharedObjectsInit;
		}
	},

	initSharedObjects: function( aOptions ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			var lToken = {
				ns: jws.SharedObjectsPlugIn.NS,
				type: "init"
			};
			this.sendToken( lToken,	aOptions );
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SharedObjectsPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Streaming PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Streaming Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.StreamingPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.StreamingPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to register and unregister at certain _
//:d:en:stream sn the server.
jws.StreamingPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.streaming (jws.NS_BASE + ".plugins.streaming")
	//:d:en:Namespace for the [tt]StreamingPlugIn[/tt] class.
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.streaming",

	//:m:*:registerStream
	//:d:en:Registers the client at the given stream on the server. _
	//:d:en:After this operation the client obtains all messages in this _
	//:d:en:stream. Basically a client can register at multiple streams.
	//:d:en:If no stream with the given ID exists on the server an error token _
	//:d:en:is returned. Depending on the type of the stream it may take more _
	//:d:en:or less time until you get the first token from the stream.
	//:a:en::aStream:String:The id of the server side data stream.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	registerStream: function( aStream ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.StreamingPlugIn.NS,
				type: "register",
				stream: aStream
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	//:m:*:unregisterStream
	//:d:en:Unregisters the client from the given stream on the server.
	//:a:en::aStream:String:The id of the server side data stream.
	//:r:*:::void:none
	// TODO: introduce OnResponse here too to get noticed on error or success.
	unregisterStream: function( aStream ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				ns: jws.StreamingPlugIn.NS,
				type: "unregister",
				stream: aStream
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	}
};

// add the StreamingPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.StreamingPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Test PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Test Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.TestPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.TestPlugIn[/tt] class.
jws.TestPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.test (jws.NS_BASE + ".plugins.test")
	//:d:en:Namespace for the [tt]TestPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.test",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.TestPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "event" == aToken.type ) {
				// callback when a server started a certain test
				if( "testStarted" == aToken.name && this.OnTestStarted ) {
					this.OnTestStarted( aToken );
				// callback when a server stopped a certain test
				} else if( "testStopped" == aToken.name && this.OnTestStopped ) {
					this.OnTestStopped( aToken );
				// event used to run a test triggered by the server
				} else if( "startTest" == aToken.name && this.OnStartTest ) {
					this.OnStartTest( aToken );
				}
			}
		}
	},

	testS2CPerformance: function( aCount, aMessage, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TestPlugIn.NS,
				type: "testS2CPerformance",
				count: aCount,
				message: aMessage
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setTestCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		// event used to run a test triggered by the server
		if( aListeners.OnStartTest !== undefined ) {
			this.OnStartTest = aListeners.OnStartTest;
		}
		// callback when a server started a certain test
		if( aListeners.OnTestStarted !== undefined ) {
			this.OnTestStarted = aListeners.OnTestStarted;
		}
		// callback when a server stopped a certain test
		if( aListeners.OnTestStopped !== undefined ) {
			this.OnTestStopped = aListeners.OnTestStopped;
		}
	}

}

// add the JWebSocket Test PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.TestPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket Twitter PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket Twitter Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.TwitterPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.TwitterPlugIn[/tt] class.
jws.TwitterPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.twitter (jws.NS_BASE + ".plugins.twitter")
	//:d:en:Namespace for the [tt]TwitterPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.twitter",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.TwitterPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "getTimeline" == aToken.reqType ) {
				if( this.OnGotTwitterTimeline ) {
					this.OnGotTwitterTimeline( aToken );
				}
			} else if( "requestAccessToken" == aToken.reqType ) {
				if( this.OnTwitterAccessToken ) {
					this.OnTwitterAccessToken( aToken );
				}
			} else if( "event" == aToken.type ) {
				if( "status" == aToken.name && this.OnTwitterStatus ) {
					this.OnTwitterStatus( aToken );
				}
			}
		}
	},

	tweet: function( aMessage, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "tweet",
				message: aMessage
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterRequestAccessToken: function( aCallbackURL, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// Twitter API calls Twitter Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "requestAccessToken",
				callbackURL: aCallbackURL
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterSetVerifier: function( aVerifier, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// passes the verifier from the OAuth window
			// to the jWebSocket server.
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "setVerifier",
				verifier: aVerifier
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterLogin: function( aCallbackURL, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// Twitter API calls Twitter Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "login",
				callbackURL: aCallbackURL
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterLogout: function( aUsername, aPassword, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "logout"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterTimeline: function( aUsername, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "getTimeline",
				username: aUsername
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterQuery: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "query",
				query: aQuery
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterTrends: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "getTrends"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterStatistics: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "getStatistics"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterPublicTimeline: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "getPublicTimeline"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterSetStream: function( aFollowers, aKeywords, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "setStream",
				keywords: aKeywords,
				followers: aFollowers
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	twitterUserData: function( aUsername, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.TwitterPlugIn.NS,
				type: "getUserData",
				username: aUsername
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setTwitterCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnGotTwitterTimeline !== undefined ) {
			this.OnGotTwitterTimeline = aListeners.OnGotTwitterTimeline;
		}
		if( aListeners.OnTwitterStatus !== undefined ) {
			this.OnTwitterStatus = aListeners.OnTwitterStatus;
		}
		if( aListeners.OnTwitterAccessToken !== undefined ) {
			this.OnTwitterAccessToken = aListeners.OnTwitterAccessToken;
		}
	}

}

// add the JWebSocket Twitter PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.TwitterPlugIn );
//	---------------------------------------------------------------------------
//	jWebSocket XMPP PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


//	---------------------------------------------------------------------------
//  jWebSocket XMPP Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.XMPPPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.XMPPPlugIn[/tt] class.
jws.XMPPPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.xmpp (jws.NS_BASE + ".plugins.xmpp")
	//:d:en:Namespace for the [tt]XMPPPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.xmpp",

	// presence flags
	// Status: free-form text describing a user's presence (i.e., gone to lunch).
	// Priority: non-negative numerical priority of a sender's resource.
	//		The highest resource priority is the default recipient
	//		of packets not addressed to a particular resource.
	// Mode: one of five presence modes: available (the default), chat, away, xa (extended away), and dnd (do not disturb).

	MODE_AVAILABLE: "available",		// default
	MODE_AWAY: "away",					// away
	MODE_CHAT: "chat",					// free to chat
	MODE_DND: "dnd",					// do not disturb
	MODE_XA: "xa",						// away for an extended period of time

	TYPE_AVAILABLE: "available",		// (Default) indicates the user is available to receive messages.
	TYPE_UNAVAILABLE: "unavailable",	// the user is unavailable to receive messages.
	TYPE_SUBSCRIBE: "subscribe",		// request subscription to recipient's presence.
	TYPE_SUBSCRIBED: "subscribed",		// grant subscription to sender's presence.
	TYPE_UNSUBSCRIBE: "unsubscribe",	// request removal of subscription to sender's presence.
	TYPE_UNSUBSCRIBED: "unsubscribed",	// grant removal of subscription to sender's presence.
	TYPE_ERROR: "error",				// the presence packet contains an error message.

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.XMPPPlugIn.NS ) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if( "event" == aToken.type ) {
				if( "chatMessage" == aToken.name ) {
					if( this.OnXMPPChatMessage ) {
						this.OnXMPPChatMessage( aToken );
					}
				} 
			} else if( "getRoster" == aToken.reqType) {
				if( this.OnXMPPRoster ) {
					this.OnXMPPRoster( aToken );
				}
			}
		}
	},

	xmppConnect: function( aHost, aPort, aDomain, aUseSSL, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// XMPP API calls XMPP Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "connect",
				host: aHost,
				port: aPort,
				domain: aDomain,
				useSSL: aUseSSL
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppDisconnect: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "disconnect"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppLogin: function( aUsername, aPassword, aOptions ) {
		// check websocket connection status
		var lRes = this.checkConnected();
		// if connected to websocket network...
		if( 0 == lRes.code ) {
			// XMPP API calls XMPP Login screen,
			// hence here no user name or password are required.
			// Pass the callbackURL to notify Web App on successfull connection
			// and to obtain OAuth verifier for user.
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "login",
				username: aUsername,
				password: aPassword
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppLogout: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "logout"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppRoster: function( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "getRoster"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppSetPresence: function( aMode, aType, aStatus, aPriority, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "setPresence",
				pmode: aMode,
				ptype: aType,
				ppriority: aPriority,
				pstatus: aStatus
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppOpenChat: function( aUserId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "openChat",
				userId: aUserId
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppSendChat: function( aUserId, aMessage, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				type: "sendChat",
				userId: aUserId,
				message: aMessage
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmppCloseChat: function( aUserId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				userId: aUserId,
				type: "closeChat"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	xmpp: function( aUserId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.XMPPPlugIn.NS,
				userId: aUserId,
				type: "closeChat"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setXMPPCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnXMPPChatMessage !== undefined ) {
			this.OnXMPPChatMessage = aListeners.OnXMPPChatMessage;
		}
		if( aListeners.OnXMPPRoster !== undefined ) {
			this.OnXMPPRoster = aListeners.OnXMPPRoster;
		}
	}

}

// add the JWebSocket XMPP PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.XMPPPlugIn );

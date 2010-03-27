//	---------------------------------------------------------------------------
//	jWebSocket Client (uses jWebSocket Server)
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------

// create name space "jws" for jWebSocket client
var jws = {

	NS_BASE: "org.jWebSocket",

	// some namespace global constants
	CUR_TOKEN_ID: 0,
	JWS_SERVER_URL:
		"ws://" + ( self.location.hostname ? self.location.hostname : "localhost" ) + ":8787",
	JWS_FLASHBRIDGE: null,

	$: function( aId ) {
		return document.getElementById( aId );
	},

	browserSupportsWebSockets: function() {
		return( window.WebSocket != null );
	}
};

// check if browser supports WebSockets and embed flash-bridge if possible
if( !jws.browserSupportsWebSockets() ) {

	// --- swfobject.js ---
	// SWFObject v2.2 <http://code.google.com/p/swfobject/>
	// is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>
	var swfobject=function(){var D="undefined",r="object",S="Shockwave Flash",W="ShockwaveFlash.ShockwaveFlash",q="application/x-shockwave-flash",R="SWFObjectExprInst",x="onreadystatechange",O=window,j=document,t=navigator,T=false,U=[h],o=[],N=[],I=[],l,Q,E,B,J=false,a=false,n,G,m=true,M=function(){var aa=typeof j.getElementById!=D&&typeof j.getElementsByTagName!=D&&typeof j.createElement!=D,ah=t.userAgent.toLowerCase(),Y=t.platform.toLowerCase(),ae=Y?/win/.test(Y):/win/.test(ah),ac=Y?/mac/.test(Y):/mac/.test(ah),af=/webkit/.test(ah)?parseFloat(ah.replace(/^.*webkit\/(\d+(\.\d+)?).*$/,"$1")):false,X=!+"\v1",ag=[0,0,0],ab=null;if(typeof t.plugins!=D&&typeof t.plugins[S]==r){ab=t.plugins[S].description;if(ab&&!(typeof t.mimeTypes!=D&&t.mimeTypes[q]&&!t.mimeTypes[q].enabledPlugin)){T=true;X=false;ab=ab.replace(/^.*\s+(\S+\s+\S+$)/,"$1");ag[0]=parseInt(ab.replace(/^(.*)\..*$/,"$1"),10);ag[1]=parseInt(ab.replace(/^.*\.(.*)\s.*$/,"$1"),10);ag[2]=/[a-zA-Z]/.test(ab)?parseInt(ab.replace(/^.*[a-zA-Z]+(.*)$/,"$1"),10):0}}else{if(typeof O.ActiveXObject!=D){try{var ad=new ActiveXObject(W);if(ad){ab=ad.GetVariable("$version");if(ab){X=true;ab=ab.split(" ")[1].split(",");ag=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}}catch(Z){}}}return{w3:aa,pv:ag,wk:af,ie:X,win:ae,mac:ac}}(),k=function(){if(!M.w3){return}if((typeof j.readyState!=D&&j.readyState=="complete")||(typeof j.readyState==D&&(j.getElementsByTagName("body")[0]||j.body))){f()}if(!J){if(typeof j.addEventListener!=D){j.addEventListener("DOMContentLoaded",f,false)}if(M.ie&&M.win){j.attachEvent(x,function(){if(j.readyState=="complete"){j.detachEvent(x,arguments.callee);f()}});if(O==top){(function(){if(J){return}try{j.documentElement.doScroll("left")}catch(X){setTimeout(arguments.callee,0);return}f()})()}}if(M.wk){(function(){if(J){return}if(!/loaded|complete/.test(j.readyState)){setTimeout(arguments.callee,0);return}f()})()}s(f)}}();function f(){if(J){return}try{var Z=j.getElementsByTagName("body")[0].appendChild(C("span"));Z.parentNode.removeChild(Z)}catch(aa){return}J=true;var X=U.length;for(var Y=0;Y<X;Y++){U[Y]()}}function K(X){if(J){X()}else{U[U.length]=X}}function s(Y){if(typeof O.addEventListener!=D){O.addEventListener("load",Y,false)}else{if(typeof j.addEventListener!=D){j.addEventListener("load",Y,false)}else{if(typeof O.attachEvent!=D){i(O,"onload",Y)}else{if(typeof O.onload=="function"){var X=O.onload;O.onload=function(){X();Y()}}else{O.onload=Y}}}}}function h(){if(T){V()}else{H()}}function V(){var X=j.getElementsByTagName("body")[0];var aa=C(r);aa.setAttribute("type",q);var Z=X.appendChild(aa);if(Z){var Y=0;(function(){if(typeof Z.GetVariable!=D){var ab=Z.GetVariable("$version");if(ab){ab=ab.split(" ")[1].split(",");M.pv=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}else{if(Y<10){Y++;setTimeout(arguments.callee,10);return}}X.removeChild(aa);Z=null;H()})()}else{H()}}function H(){var ag=o.length;if(ag>0){for(var af=0;af<ag;af++){var Y=o[af].id;var ab=o[af].callbackFn;var aa={success:false,id:Y};if(M.pv[0]>0){var ae=c(Y);if(ae){if(F(o[af].swfVersion)&&!(M.wk&&M.wk<312)){w(Y,true);if(ab){aa.success=true;aa.ref=z(Y);ab(aa)}}else{if(o[af].expressInstall&&A()){var ai={};ai.data=o[af].expressInstall;ai.width=ae.getAttribute("width")||"0";ai.height=ae.getAttribute("height")||"0";if(ae.getAttribute("class")){ai.styleclass=ae.getAttribute("class")}if(ae.getAttribute("align")){ai.align=ae.getAttribute("align")}var ah={};var X=ae.getElementsByTagName("param");var ac=X.length;for(var ad=0;ad<ac;ad++){if(X[ad].getAttribute("name").toLowerCase()!="movie"){ah[X[ad].getAttribute("name")]=X[ad].getAttribute("value")}}P(ai,ah,Y,ab)}else{p(ae);if(ab){ab(aa)}}}}}else{w(Y,true);if(ab){var Z=z(Y);if(Z&&typeof Z.SetVariable!=D){aa.success=true;aa.ref=Z}ab(aa)}}}}}function z(aa){var X=null;var Y=c(aa);if(Y&&Y.nodeName=="OBJECT"){if(typeof Y.SetVariable!=D){X=Y}else{var Z=Y.getElementsByTagName(r)[0];if(Z){X=Z}}}return X}function A(){return !a&&F("6.0.65")&&(M.win||M.mac)&&!(M.wk&&M.wk<312)}function P(aa,ab,X,Z){a=true;E=Z||null;B={success:false,id:X};var ae=c(X);if(ae){if(ae.nodeName=="OBJECT"){l=g(ae);Q=null}else{l=ae;Q=X}aa.id=R;if(typeof aa.width==D||(!/%$/.test(aa.width)&&parseInt(aa.width,10)<310)){aa.width="310"}if(typeof aa.height==D||(!/%$/.test(aa.height)&&parseInt(aa.height,10)<137)){aa.height="137"}j.title=j.title.slice(0,47)+" - Flash Player Installation";var ad=M.ie&&M.win?"ActiveX":"PlugIn",ac="MMredirectURL="+O.location.toString().replace(/&/g,"%26")+"&MMplayerType="+ad+"&MMdoctitle="+j.title;if(typeof ab.flashvars!=D){ab.flashvars+="&"+ac}else{ab.flashvars=ac}if(M.ie&&M.win&&ae.readyState!=4){var Y=C("div");X+="SWFObjectNew";Y.setAttribute("id",X);ae.parentNode.insertBefore(Y,ae);ae.style.display="none";(function(){if(ae.readyState==4){ae.parentNode.removeChild(ae)}else{setTimeout(arguments.callee,10)}})()}u(aa,ab,X)}}function p(Y){if(M.ie&&M.win&&Y.readyState!=4){var X=C("div");Y.parentNode.insertBefore(X,Y);X.parentNode.replaceChild(g(Y),X);Y.style.display="none";(function(){if(Y.readyState==4){Y.parentNode.removeChild(Y)}else{setTimeout(arguments.callee,10)}})()}else{Y.parentNode.replaceChild(g(Y),Y)}}function g(ab){var aa=C("div");if(M.win&&M.ie){aa.innerHTML=ab.innerHTML}else{var Y=ab.getElementsByTagName(r)[0];if(Y){var ad=Y.childNodes;if(ad){var X=ad.length;for(var Z=0;Z<X;Z++){if(!(ad[Z].nodeType==1&&ad[Z].nodeName=="PARAM")&&!(ad[Z].nodeType==8)){aa.appendChild(ad[Z].cloneNode(true))}}}}}return aa}function u(ai,ag,Y){var X,aa=c(Y);if(M.wk&&M.wk<312){return X}if(aa){if(typeof ai.id==D){ai.id=Y}if(M.ie&&M.win){var ah="";for(var ae in ai){if(ai[ae]!=Object.prototype[ae]){if(ae.toLowerCase()=="data"){ag.movie=ai[ae]}else{if(ae.toLowerCase()=="styleclass"){ah+=' class="'+ai[ae]+'"'}else{if(ae.toLowerCase()!="classid"){ah+=" "+ae+'="'+ai[ae]+'"'}}}}}var af="";for(var ad in ag){if(ag[ad]!=Object.prototype[ad]){af+='<param name="'+ad+'" value="'+ag[ad]+'" />'}}aa.outerHTML='<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'+ah+">"+af+"</object>";N[N.length]=ai.id;X=c(ai.id)}else{var Z=C(r);Z.setAttribute("type",q);for(var ac in ai){if(ai[ac]!=Object.prototype[ac]){if(ac.toLowerCase()=="styleclass"){Z.setAttribute("class",ai[ac])}else{if(ac.toLowerCase()!="classid"){Z.setAttribute(ac,ai[ac])}}}}for(var ab in ag){if(ag[ab]!=Object.prototype[ab]&&ab.toLowerCase()!="movie"){e(Z,ab,ag[ab])}}aa.parentNode.replaceChild(Z,aa);X=Z}}return X}function e(Z,X,Y){var aa=C("param");aa.setAttribute("name",X);aa.setAttribute("value",Y);Z.appendChild(aa)}function y(Y){var X=c(Y);if(X&&X.nodeName=="OBJECT"){if(M.ie&&M.win){X.style.display="none";(function(){if(X.readyState==4){b(Y)}else{setTimeout(arguments.callee,10)}})()}else{X.parentNode.removeChild(X)}}}function b(Z){var Y=c(Z);if(Y){for(var X in Y){if(typeof Y[X]=="function"){Y[X]=null}}Y.parentNode.removeChild(Y)}}function c(Z){var X=null;try{X=j.getElementById(Z)}catch(Y){}return X}function C(X){return j.createElement(X)}function i(Z,X,Y){Z.attachEvent(X,Y);I[I.length]=[Z,X,Y]}function F(Z){var Y=M.pv,X=Z.split(".");X[0]=parseInt(X[0],10);X[1]=parseInt(X[1],10)||0;X[2]=parseInt(X[2],10)||0;return(Y[0]>X[0]||(Y[0]==X[0]&&Y[1]>X[1])||(Y[0]==X[0]&&Y[1]==X[1]&&Y[2]>=X[2]))?true:false}function v(ac,Y,ad,ab){if(M.ie&&M.mac){return}var aa=j.getElementsByTagName("head")[0];if(!aa){return}var X=(ad&&typeof ad=="string")?ad:"screen";if(ab){n=null;G=null}if(!n||G!=X){var Z=C("style");Z.setAttribute("type","text/css");Z.setAttribute("media",X);n=aa.appendChild(Z);if(M.ie&&M.win&&typeof j.styleSheets!=D&&j.styleSheets.length>0){n=j.styleSheets[j.styleSheets.length-1]}G=X}if(M.ie&&M.win){if(n&&typeof n.addRule==r){n.addRule(ac,Y)}}else{if(n&&typeof j.createTextNode!=D){n.appendChild(j.createTextNode(ac+" {"+Y+"}"))}}}function w(Z,X){if(!m){return}var Y=X?"visible":"hidden";if(J&&c(Z)){c(Z).style.visibility=Y}else{v("#"+Z,"visibility:"+Y)}}function L(Y){var Z=/[\\\"<>\.;]/;var X=Z.exec(Y)!=null;return X&&typeof encodeURIComponent!=D?encodeURIComponent(Y):Y}var d=function(){if(M.ie&&M.win){window.attachEvent("onunload",function(){var ac=I.length;for(var ab=0;ab<ac;ab++){I[ab][0].detachEvent(I[ab][1],I[ab][2])}var Z=N.length;for(var aa=0;aa<Z;aa++){y(N[aa])}for(var Y in M){M[Y]=null}M=null;for(var X in swfobject){swfobject[X]=null}swfobject=null})}}();return{registerObject:function(ab,X,aa,Z){if(M.w3&&ab&&X){var Y={};Y.id=ab;Y.swfVersion=X;Y.expressInstall=aa;Y.callbackFn=Z;o[o.length]=Y;w(ab,false)}else{if(Z){Z({success:false,id:ab})}}},getObjectById:function(X){if(M.w3){return z(X)}},embedSWF:function(ab,ah,ae,ag,Y,aa,Z,ad,af,ac){var X={success:false,id:ah};if(M.w3&&!(M.wk&&M.wk<312)&&ab&&ah&&ae&&ag&&Y){w(ah,false);K(function(){ae+="";ag+="";var aj={};if(af&&typeof af===r){for(var al in af){aj[al]=af[al]}}aj.data=ab;aj.width=ae;aj.height=ag;var am={};if(ad&&typeof ad===r){for(var ak in ad){am[ak]=ad[ak]}}if(Z&&typeof Z===r){for(var ai in Z){if(typeof am.flashvars!=D){am.flashvars+="&"+ai+"="+Z[ai]}else{am.flashvars=ai+"="+Z[ai]}}}if(F(Y)){var an=u(aj,am,ah);if(aj.id==ah){w(ah,true)}X.success=true;X.ref=an}else{if(aa&&A()){aj.data=aa;P(aj,am,ah,ac);return}else{w(ah,true)}}if(ac){ac(X)}})}else{if(ac){ac(X)}}},switchOffAutoHideShow:function(){m=false},ua:M,getFlashPlayerVersion:function(){return{major:M.pv[0],minor:M.pv[1],release:M.pv[2]}},hasFlashPlayerVersion:F,createSWF:function(Z,Y,X){if(M.w3){return u(Z,Y,X)}else{return undefined}},showExpressInstall:function(Z,aa,X,Y){if(M.w3&&A()){P(Z,aa,X,Y)}},removeSWF:function(X){if(M.w3){y(X)}},createCSS:function(aa,Z,Y,X){if(M.w3){v(aa,Z,Y,X)}},addDomLoadEvent:K,addLoadEvent:s,getQueryParamValue:function(aa){var Z=j.location.search||j.location.hash;if(Z){if(/\?/.test(Z)){Z=Z.split("?")[1]}if(aa==null){return L(Z)}var Y=Z.split("&");for(var X=0;X<Y.length;X++){if(Y[X].substring(0,Y[X].indexOf("="))==aa){return L(Y[X].substring((Y[X].indexOf("=")+1)))}}}return""},expressInstallCallback:function(){if(a){var X=c(R);if(X&&l){X.parentNode.replaceChild(l,X);if(Q){w(Q,true);if(M.ie&&M.win){l.style.display="block"}}if(E){E(B)}}a=false}}}}();

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
	eval((function(x){var d="";var p=0;while(p<x.length){if(x.charAt(p)!="`")d+=x.charAt(p++);else{var l=x.charCodeAt(p+3)-28;if(l>4)d+=d.substr(d.length-x.charCodeAt(p+1)*96-x.charCodeAt(p+2)+3104-l,l);else d+="`";p+=4}}return d})("function FABridge(target,b` )!Name){this.` 2\"=` 9\";` .!remoteTypeCache={}` ,(Instanc` '1F`!0#` .*local` %2`!I\"ID=`!`$.next` %\"ID++` @\"name=`!p&` .#extL` r$ID=0;` [%i`!\\#s[` V%]=this` 8'dMap` 9\"`!<$` <#return ` '!}` G%TYPE_ASINSTANCE=1` a&` 4#FUNCTION=2` ,+J` 1&3` F,NONYMOUS=4`!r(itCallbacks={}` 0&userType` (*addToU` 2%`%3$(){for(var i=0;i<arguments.length;i++){` k.[` @%[i]]={typ`#r!:` ,(,enriched:false};}`!F(rgsToArray`!E&args){var result=[];`!P-`!N*` C\"[i]=args[i];}`$7#` 4\";};`'`%`%,$Factory(objID`']#fb_` 7$_id=` 6!`$z*`(?-__invokeJS`'1$`!v'f`&L\"`!P!0];var throughArgs` 3!.concat();` /'.shift()` M!`&Q\"`'o&extrac`'u#FromID(`!#\")`!d$` M\".`!T\"`'s%`!X!` D\",`!\"')`'3'addInitializa`)@\"`&:\"`$5&`(u&,c` 6#`\"G\"inst`!\\&`(q&` G&];if(inst!=undefined){` ^$.call` :!`\" $;}var ` 8$Li` r*`'r'` t,` F)=null`'5'` A5=` J)[];}` %(.push` q%)`&'(`%B&`*|#`#;$ed`#$'`# \"objects=doc`(?!.getEle`(J!ByTagName(\"` A\"\"`%<\"ol=` P#`'n$var activeO` i#[`\"T!ol>0`),ol`(?\"if(typeof`!<$[i].SetVariable!=\"`$-%\"){` r)[`! )`!?#]`!N$`),!}`$H!embed`!}=` A!`\"6#el=` O\"`\"*.E` g\"`\"5\"e`\".)j=0;j<el;j`\"-*` e\"[j`!x>` w\"`\"2#` '\"`\"/%` ]%`\"/$aol=`\"O0`!g\"e` 5$` Z)` 7!searchStr=\"`%@&=\"+`1Z'if(aol==1&&!ael||` &$ael==1`'/'attach`4A#`$2*0]`4F(;}else if(` ^\"&&!ao`'x(` W/`\"i#` U0`,f\"lash_found=`/F!`!r#>1`$,&k=0;k<aol;k++`'j\"params`#/*[k].childNodes`/T%l=0;l<` K\"`#<$l` [)=` 7\"[l`%M!` (!.nodeType`\"B!` -\"t`&B\".toLowerCase()==\"` 9!\"` >$n` /0`\"A!vars` >%value.indexOf(`$g%)>=0`#uBk`#D*`#<(true;break;}}if(` 1'){` 0$}if(!` /'`%T!`#j(m=0;m<ael;m`#&$` J!Var`#q$`$v#m].attributes.getNamedItem(`\"T\"Vars\")`#O!Valu`$!` 0%`\"1P`!0%`\"W)`\"2%`4?$`\"d!`1=&nex`29#ID=0;`0c.={}` +'dMap` %)refCount` P(`3$/`2+&id`#2\"`/9#D=id>>16`3H$` }*`0M#ID];`!)'`\"Q(` n'`!k#`\"S(`!%!new`\";#` ;#=new`! %` C1`!#%`1h)` X-`+s!`1`$s`2cKs`3'$`3z$`0^*` >%`)X$`0u!` -%[i]`4`\"`!D-);}delete`#A'`!75`#K'blockedMethods={toString:true,get` #\"se` \"#call` %!` Y'prototype={root:`$4%`\">$ this.deser`4Q\"(` ,!targe`1~!Root());},releaseAS`*7#` U4` U#` ?,(` Y'`)7!` 2$` ^&`+k!`2'(` ,!!=\"`3g\"\"` %`._\"`.w&ret=`!)/` z)` x\".fb_`&9$_id)`'_$ret;}},create`!H&class`'4\"`\"l@` Y\"` K');},makeID` j&token` h$` Y\"`(n$<<16)+` <!;},getPropertyFromAS` Z&objRef,prop`!X\"if(`*R->0){throw new Error(\"You are trying to`(K! recursively into the Flash Player which is not allowed. In most cases` P!JavaScript setTimeout `!r$, can be used as a workaround.\"`3n$`!{-++;retVal`${)`\"{#`\"x\"`\"d-` E)handle`\"_\"` 2\"`+K&` }%--`%E'Val;}},s`#|&In`#i7,`'4&`#(~`#(~`#[K`\"#In`#v.,` C!`(G&`#\"\"`#WccallASF`\"-#`$3&funcID,args`#1~`#1~`#_Pinvoke`\"s&`\"l$`$ +`\"~!`#Ai`1l\"`(<)ID,func`%J!`#2~`#2~`#oAargs`#K\"`#_+`$28`#7\"`\"1`#^b`!#\"Local`(8;`3T\"sult;var`\"m!`!6\"l` Q(Cache[` S\"];if` _!!=undefined){` ^\"`\"Z,func.apply(null`&q\"`3M(`&s\");}`!z%`!F!`2s!TypeFromNam`4C'objType`4>.remoteType`!h\"` >'];},`4Q\"Proxy`'&,t` g%var ` Q#`\"%\"`!8+(` B%;instanceFactory.prototype=` Z#`#K!` >$=new` $%` L#`!8\");`!s'I` ;#`!y%ID]=` O$`$$` '%`#!!`!~0`\"e0` g0;},addTypeDataTo` 7!` b&t` 2#){new`\"o!new AS`!.!(this`#6!Data.n`\"p!var accessors=` 4%` *%;for(`\"s!=0;i<` /%.length;i++){`!p!add`4*$ToType(`!=#,` L%[i]);}var m`(k!`!&'` *#`! +` /#`! )`,7(blocked`)X\"s[` H#[i]]=`'_(`!X$` B\"`!O+` K&);}}`&d1` D#.`&$$]=` +#`$y$` '$`$9!`\"X*`$1(`3t%`'2\"c=` ($.charAt(0`$+\"setterNam`&m\"g` %&if(c>=\"a\"&&c<=\"z\"){` 4&=\"get\"+c.toUpperCase()+` |%substr(1);` w&=\"s` -C`.,\"` }-` B$` b.` 1%}ty[` 4&]=`\"i%val`$6#b`$j\"s`(9!`#9!InAS`'-!.fb_`(Y$_id`#;%,val);};ty[`!K&` r'`(e*` z#`,^(` ,(`)Q\"`!4!From` |;));}`%&\"`&&(`$})`&0\"`%&\"ty`&s#`!&X`4Y(`!H1` y&,`(7%argsToArray(argument`/8\"`,%\"`0F$`,$+`0Q\"`&\"`!9\"`.7!;if`!4\"`(:\"`0v1==nul`$n$` ,8`\"['`\"5)` F$`2M$`!l<;}`1B%` t<`\"F*ID`\"C*`+U!`2Z!__`!L\"_id__`+<)` ,/`!0!makeID`\"w\"next`4_%ID++`0I#`4(3` a*`\"r\"`\")%` /.;},`%W%`!v&valu`+N#`4*\"={}`+6!t`.=!of ` =!`$\\!==\"number\"||t==\"string` &#boolean` (\"null` $!`\"W'` x#` i\"`*n! if`!:\"`2>%of `$B!` E%[]`/I+` K!`/I)` D\"[i]`#K\"`'z&` F!`.v#`!'$t==\"`\"T$\"` T$`.y!=`%f%TYPE_JSFUNCTION;` >#` j!` ~\"`%?)`#<#`!w8`3@\"` s8ASINSTANCE`!!*`\"I\"`)b*`.)#` R7NONYMOUS` Y/`%X%` 5\";},`+,'`%M&packedV`%M,`%L*` :'`%4H` L'`%b$` &)`%b.`#p!handleError`!R)`#o&` +'`%gG` K'`%|8`.2(` H'`&-/object\"){` n6newTypes`!$)`!\"!addTypeDataTo`*G!`!\"(` P%`!1\"` y$aRefID in`$'(.newRefs` r#create`&d!(` J\",` :/[` 5\"]);}`#C*`&B\"`&:+PRIMITIVE`#U%` F(`*;+` M<A`))%`%**`0},` \\)`),,` c>`) $` t-` :_`)0$`\"S8`)/-addRef`)1&obj`$L#target.incRef(obj`*Z+);},releas`)o'` N-` ;#` I6`(n'`0'-if(`/{(`*+&&&`,5\"indexOf(\"__FLASHERROR\")==0`+2\"my` t!Messag`,h$split(\"||\");if(`#=%refCount>0){` $---;}throw new `*b\"` p*[1]);`#X#`&R'{` %*}};`.j#`3-!`\"I!`3!\",typeName`#:#` 0\"=` 7\"`4%\"` :$`->!Name`!($this;` q%.proto`%_!{get`.-'rop` |\"` M'`!\"#`+V)` ,(`&w\"pertyFromAS` 8\"`$\\*,` r%);},s`!!0,`$n#` p(s` r&In` [;` Y#;},call` v&func` ;!arg`+g$` x#callASMethod` g1` N*`'|/` b*` 8\"` e!`'i1` @*` 8#` G$};"))

	// --- web_socket.js ---
	// Copyright: Hiroshi Ichikawa <http://gimite.net/en/>
	// http://github.com/gimite/web-socket-js
	// http://www.lightsphere.com/dev/articles/socketpolicy.pl.html
	// License: New BSD Lincense
	// Full Sources codes provided in web_socket.js
	eval((function(x){var d="";var p=0;while(p<x.length){if(x.charAt(p)!="`")d+=x.charAt(p++);else{var l=x.charCodeAt(p+3)-28;if(l>4)d+=d.substr(d.length-x.charCodeAt(p+1)*96-x.charCodeAt(p+2)+3104-l,l);else d+="`";p+=4}}return d})("if(!window.WebSocket){` +'console){` \"#={log:function(){},error` &)};}` e%=` 0%url,protoco` $!xyHost` $\"Port,headers){var self=this;self.readyState=` r%.CONNECTING` ?\"bufferedAmount=0;` A&__addTask(`!^'` P!__flash` l'` +#.create`!a3||nul` )#Port||0`!y$` 5\")`!N\"` e$addEventListener(\"open\",`!F%fe){try{if(` T!onopen`!^#` '\"();}}catch(`$(&.`#{!(e.toString());}}` ~=close` vF` J!` Timessag`!3,var data=decodeURIComponent(fe.getData());`!_*` g#` [\"e;if(`';#M` 3\"`!8!){e=document`$x#` 3!(\"` :(\");e.init` )(`!d(alse,false,data,`%E!`%J!`!.\");}else{e={data:data};}`!\\*(e`\"ahstateChan`#@-try`$^\"`(X'`#C\"R` (%(` y#`(R+` A\"B` ()();`!KF});}`):'`(a!type.send`*p&data`+p\"this`\">$||` )!`!l'`*F1){throw\"INVALID_STATE_ERR: Web ` J\" conne`!)! has not been established\";}var result`+v!`#g%send`!`\";if(` <\"<0){return true`%,#` R!`#8+` J\";` F#`%s!;}`\"b2`)!!`\"o&`\"c.`!,$;}if(`\"q+!`\"u'OPEN` B&`\" )`*\"$`#N,`#R'LOSED;` z$`*V#)`\"7\"`*_(`!o1`&m,`\"%&type,l` 0#,useCapture`\"A\"(\"__events\"in this)`!,#` 0$={}`\"S!!` e! ` ?#` 6%` ?,[type]=[];if(\"`!>$\"==typeof` X![\"on\"+` D!` I1.defaultHandler`%R!` I'`#@!` %'`#;&_Fir`+3#this,typ`*L!` p0push(`\"p$`(;4remov` l\"`\"X~`#H.`&#$for(var i`(:$` <\".length;i>-1;--i`!4!`!I$==` ?*`\"Q\"[i`#\\3splice(i,1);break;}`&43dispatch`\"q!`\"d&` i!`\">9row\"UNSPECIFIED_EVENT_TYP`+5!\"`\"e#` T!.`&26` E=`##&0,l`\"U+` s&]`#4%<l;++i`\"f,` ?'[i]`\"7#;if`!Y#cancelBubble){`#)$if(`+e!!==` A\"`$[\"Value&&`'w3`!H2`'u*`!C7` ;+`!b$}};`!'$ `(,0object`(<\"`.+$`0-+var ` i!=new` [&` [!;`!<\"ini` *\"`('\"true,true)` 9#target`\"f#currentT` .\"`!;\";`$F$key in `!4\"` K![key]=data` %!;}` L\"`&R*`\"I\",argum`%^!;};}`\"G.` F\"){}` $*`/y(`$l!able=`0u!` *;`%4\"=`0r\"` 35pr`!Z!D`$<\"`0t+`$m!`!-&`$|#`%g'` w#`);(` q,stopPropagation` v(` t'`!f#`3,\"` S6`$d%`*&+TypeArg,can` d\"` &#`!r#Arg`!'#type`%&\"` L#`2I\"`#P'` I)` 8\"timeStamp`&A!Dat`!W).CONNECTING=0` ,'OPEN=1` <(`3:!=2` ('__tasks=[]` ()initialize`$\"+!` :(swfLoc`#Q!){console.error(\"[` @%] set`&=&` J* to l` $$of` =&Main.swf\");`/Z$var container=doc`'J!.createElement(\"div\");` ?%.id=\"w` n$C` /$\"` 7'style.posi`%[!\"absolute` 2.left=\"-100px` ,.top` 3&var hold`!I=` ?\"`!Y*Flash` u(appendChild(` K\");` n%body` 5)` O%);swf`*l#embedSWF(`#s3,`!/,,\"10\",\"10\",\"9.0.0\",null,{bridgeName:` J&\"}` 8\"` >!`(2&`%o\"e.success`%I:`![- failed\");}});FAB`!5!.addI`&y$`!w!Callback(`!E'`!;&){try{`\"K(flash=` g%` L%.root()`'~)` G!.setCallerUrl(`')$.href)`.v%i=0;i<`(k-`3=$`3<!` --[i]();`.J&`)F(}catch(e`\"y9\"+e.toString()`#$\"`*q(__addTask`* &task`$4!`\"7-){task();}else`!\\..push` Z\"`31)`#9%Log(messag`!k'log(decodeURIComponent` ?%)`1C'` `%E`\"E!` Z-`\"[\"` O:if(window.add`/(!Listener){` \"3(\"load\",`-.2,`1 !`\"m$` [$tta`3Y$\"on` D8);}}"))

	// init flash bridge
	// get all scripts on the page to find jWebSocket.js path
	var lScripts = document.getElementsByTagName( "script" );
	for( var lIdx = 0, lCnt = lScripts.length; lIdx < lCnt; lIdx++ ) {
		var lScript = lScripts[ lIdx ];
		var lPath = lScript.getAttribute( "src" );
		if( lPath ) {
			var lPos = lPath.indexOf( "jwebsocket.js" );
			if( lPos > 0 ) {
				jws.JWS_FLASHBRIDGE = lPath.substr( 0, lPos ) + "flash-bridge/WebSocketMain.swf";
				break;
			}
		}
	}

	if( jws.JWS_FLASHBRIDGE != null ) {
		WebSocket.__swfLocation = jws.JWS_FLASHBRIDGE;
	} else {
		WebSocket = null;
	}
}

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
}

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
		// don't overwrite existing methods of class with plug.in methods
		if( !aClass.prototype[ lField ] ) {
			aClass.prototype[ lField ] = aPlugIn[ lField ];
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
}


//	---------------------------------------------------------------------------
//  jWebSocket - Base Client
//  This class does not handle exceptions or error, it throws exceptions,
//  which are handled by the descendant classes.
//	---------------------------------------------------------------------------

// declaration for the jws.jWebSocketBaseClient class
jws.oop.declareClass( "jws", "jWebSocketBaseClient", null, {

	processOpened: function( aEvent ) {
		// can to be overwritten in descendant classes
		// to easily handle open event in descendants
	},

	processPacket: function( aEvent ) {
		// can to be overwritten in descendant classes
		// to easily handle message event in descendants
	},

	processClosed: function( aEvent ) {
		// can to be overwritten in descendant classes
		// to easily handle open event in descendants
	},

	open: function( aURL, aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		// if browser support WebSockets at all...
		if( self.WebSocket) {

			// if connection not already established...
			if( !this.fConn ) {
				var lThis = this;
				var lValue = null;

				// create a new web socket instance
				this.fConn = new WebSocket( aURL );

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
					// give application change to handle event
					if( aOptions.OnClose ) {
						aOptions.OnClose( aEvent, lValue, lThis );
					}
					lThis.fConn = null;
				}
			} else {
				throw new Error( "Already connected" );
			}
		} else {
			throw new Error( "WebSockets not supported by browser" );
		}
	},

	connect: function( aURL, aOptions ) {
		return this.open(aURL, aOptions );
	},

	sendStream: function( aData ) {
		// is client already connected
		if( this.isConnected() ) {
			this.fConn.send( aData );
			// if not raise exception
		} else {
			throw new Error( "Not connected" );
		}
	},

	isConnected: function() {
		return( this.fConn && this.fConn.readyState == 1 );
	},

	forceClose: function() {
		if( this.fConn ) {
			this.fConn.onopen = null;
			this.fConn.onmessage = null;
			this.fConn.onclose = null;
			this.fConn.close();
			this.processClosed();
		}
		this.fConn = null;
	},

	close: function( aOptions ) {
		// check if timeout option is used
		var lTimeout = 0;
		if( aOptions ) {
			if( aOptions.timeout ) {
				lTimeout = aOptions.timeout;
			}
		}
		// connection established at all?
		if( this.fConn ) {
			if( lTimeout <= 0 ) {
				this.forceClose();
			} else {
				var lThis = this;
				this.hDisconnectTimeout = setTimeout(
				function() {
					lThis.forceClose.call( lThis );
				},
				lTimeout
			);
			}
			// throw exception if not connected
		} else {
			throw new Error( "Not connected" );
			this.fConn = null;
		}
	},

	disconnect: function( aOptions ) {
		return this.close( aOptions );
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket token client (this is an abstract class)
//  don't create direct instances of jWebSocketTokenClient
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketTokenClient", jws.jWebSocketBaseClient, {

	// this method is called by the contructor of this class
	create: function() {
		this.fRequestCallbacks = {};
	},

	getId: function() {
		return this.fClientId;
	},

	checkCallbacks: function( aToken ) {
		var lField = "utid" + aToken.utid;
		// console.log("checking result for utid: " + aToken.utid + "...");
		var lClbkRec = this.fRequestCallbacks[ lField ];
		if( lClbkRec ) {
			lClbkRec.callback.call( this, aToken );
			delete this.fRequestCallbacks[ lField ];
		}
		// todo: delete timed out requests and optionally fire timeout callbacks
		for( lField in this.fRequestCallbacks ) {
			// ..
		}
	},

	createDefaultResult: function() {
		jws.CUR_TOKEN_ID++;
		return{
			code: 0,
			msg: "Ok",
			localeKey: "jws.jsc.res.Ok",
			args: null,
			tid: jws.CUR_TOKEN_ID
		};
	},

	checkConnected: function() {
		jws.CUR_TOKEN_ID++;
		var lRes = this.createDefaultResult();
		if( !this.isConnected() ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	checkLoggedIn: function() {
		jws.CUR_TOKEN_ID++;
		var lRes = this.createDefaultResult();
		if( !this.isLoggedIn() ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	resultToString: function( aRes ) {
		return(
		aRes.msg
		// + " (code: " + aRes.code + ", tid: " + aRes.tid + ")"
	);
	},

	tokenToStream: function( aToken ) {
		// this is supposed to convert a token into a string stream which is
		// send to the server, not implemented in base class.
		// needs to be overwritten in descendant classes!
		throw new Error( "tokenToStream needs to be overwritten in descendant classes" );
	},

	streamToToken: function( aStream ) {
		// this is supposed to convert a string stream from the server into 
		// a token (object), not implemented in base class.
		// needs to be overwritten in descendant classes
		throw new Error( "streamToToken needs to be overwritten in descendant classes" );
	},

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

	processPacket: function( aEvent ) {
		// parse incoming token...
		var lToken = this.streamToToken( aEvent.data );
		// and process it...
		this.processToken( lToken );
		return lToken;
	},

	processToken: function( aToken ) {
		// check welcome and goodBye tokens to manage the session
		if( aToken.type == "welcome" && aToken.usid ) {
			this.fSessionId = aToken.usid;
			this.fClientId = aToken.sourceId;
			this.notifyPlugInsOpened();
		} else if( aToken.type == "goodBye" ) {
			this.fSessionId = null;
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

	processClosed: function( aEvent ) {
		this.notifyPlugInsClosed();
		this.fClientId = null;
	},

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

	sendToken: function( aToken, aOptions ) {
		var lOnResponse = null;
		if( aOptions ) {
			if( aOptions.OnResponse ) {
				lOnResponse = aOptions.OnResponse;
			}
		}
		if( lOnResponse ) {
			this.fRequestCallbacks[ "utid" + jws.CUR_TOKEN_ID ] = {
				request: new Date().getTime(),
				callback: lOnResponse
			}
		}
		var lStream = this.tokenToStream( aToken );

		console.log("sending" + lStream + "...");

		this.sendStream( lStream );
	},

	sendText: function( aReceiver, aText ) {
		var lRes = this.checkLoggedIn();
		if( lRes.code == 0 ) {
			this.sendToken({
				type: "send",
				targetId: aReceiver,
				sourceId: this.fClientId,
				sender: this.fUsername,
				data: aText
			});
		}
		return lRes;
	},

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

	echo: function( aData ) {
		var lRes = this.checkConnected();
		if( lRes.code == 0 ) {
			this.sendToken({
				type: "echo",
				data: aData
			});
		}
		return lRes;
	},

	open: function( aURL, aOptions ) {
		var lRes = this.createDefaultResult();
		try {
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

	// deprecated, kept for upward compatibility
	connect: function( aURL, aOptions ) {
		return this.open( aURL, aOptions );
	},

	close: function( aOptions ) {
		var lTimeout = 0;
		if( aOptions ) {
			if( aOptions.timeout ) {
				lTimeout = aOptions.timeout;
			}
		}
		var lRes = this.createDefaultResult();
		try {
			// if connected and timeout is passed give server a chance to
			// register the disconnect properly and send a good bye response.
			if( this.fConn  ) {
				this.sendToken({
					type: "close",
					timeout: lTimeout
				});
			}
			// call inherited disconnect, catching potential exception
			arguments.callee.inherited.call( this, aOptions );
		} catch( ex ) {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.ex";
			lRes.args = [ ex.message ];
			lRes.msg = "Exception on close: " + ex.message;
		}
		return lRes;
	},

	// deprecated, kept for upward compatibility
	disconnect: function( aOptions ) {
		return this.close( aOptions );
	},

	login: function( aUsername, aPassword, aPool ) {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				type: "login",
				username: aUsername,
				password: aPassword,
				pool: aPool
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notConnected";
			lRes.msg = "Not connected.";
		}
		return lRes;
	},

	logout: function() {
		var lRes = this.createDefaultResult();
		if( this.isConnected() ) {
			this.sendToken({
				type: "logout"
			});
		} else {
			lRes.code = -1;
			lRes.localeKey = "jws.jsc.res.notLoggedIn";
			lRes.msg = "Not logged in.";
		}
		return lRes;
	},

	isLoggedIn: function() {
		return( this.isConnected() && this.fUsername );
	},

	getUsername: function() {
		return( this.isLoggedIn() ? this.fUsername : null );
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket Client System Plug-In
//	---------------------------------------------------------------------------

jws.SystemClientPlugIn = {

	// namespace for system plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.system",

	ALL_CLIENTS: 0,
	AUTHENTICATED: 1,
	NON_AUTHENTICATED: 2,

	getClients: function( aOptions ) {
		var lMode = jws.SystemClientPlugIn.ALL_CLIENTS;
		var lPool = null;
		if( aOptions ) {
			if( aOptions.mode == jws.SystemClientPlugIn.AUTHENTICATED ||
				aOptions.mode == jws.SystemClientPlugIn.NON_AUTHENTICATED ) {
				lMode = aOptions.mode
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

	getNonAuthClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.NON_AUTHENTICATED;
		return this.getClients( aOptions );
	},

	getAuthClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.AUTHENTICATED;
		return this.getClients( aOptions );
	},

	getAllClients: function( aOptions ) {
		if( !aOptions ) {
			aOptions = {};
		}
		aOptions.mode = jws.SystemClientPlugIn.ALL_CLIENTS;
		return this.getClients( aOptions );
	},

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

	startKeepAlive: function( aOptions ) {
		// if we have a keep alive running already stop it
		if( this.hKeepAlive ) {
			stopKeepAlive();
		}
		// return if not (yet) connected
		if( !this.isConnected() ) {
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

	stopKeepAlive: function() {
		if( this.hKeepAlive ) {
			clearInterval( this.hKeepAlive );
			this.hKeepAlive = null;
		}
	}
}

// add the JWebSocket SystemClient PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SystemClientPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket Client Streaming Plug-In
//	---------------------------------------------------------------------------

jws.StreamingPlugIn = {

	// namespace for client streaming plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.streaming",

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

}

// add the JWebSocket SystemClient PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.StreamingPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket RPC Client Plug-In
//	---------------------------------------------------------------------------

// the RRPCServer server provides the methods which are granted to be called
// from the "outside".
jws.RRPCServer = {

	demo: function( aArgs ) {
		return(
		confirm(
		"aArgs received: '" + aArgs + "'\n" +
			"'true' or 'false' will be returned to requester."
	)
	);
	}

};


jws.RPCClientPlugIn = {

	// namespace for RPC plugin
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.rpc",

	// granted rrpc's
	grantedProcs: [
		"jws.RRPCServer.demo"
	],

	processToken: function( aToken ) {
		// console.log( "jws.RPCClientPlugIn: Processing token " + aToken.ns + "/" + aToken.type + "..." );
		if( aToken.ns == jws.RPCClientPlugIn.NS ) {
			if( aToken.type == "rrpc" ) {
				this.onRRPC( aToken );
			}
		}
	},

	rpc: function( aClass, aMthd, aArgs, aOptions ) {
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

	// calls a remote procedure an another client
	rrpc: function( aTarget, aClass, aMthd, aArgs, aOptions ) {
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

	// handles a remote procedure call from another client
	onRRPC: function( aToken ) {
		var lClassname = aToken.classname;
		var lMethod = aToken.method;
		var lArgs = aToken.args;
		var lPath = lClassname + "." + lMethod;
		// check if the call is granted on this client
		if( jws.RPCClientPlugIn.grantedProcs.indexOf( lPath ) >= 0 ) {
			var lEvalStr = lPath + "(" + lArgs + ")";
			// console.log( "Reverse RPC request '" + lPath + "(" + lArgs + ")' granted, running '" + lEvalStr + "'...");
			var lRes;
			eval( "lRes=" + lEvalStr );
			// send result back to requester

			this.sendToken({
				// ns: jws.SystemPlugIn.NS,
				type: "send",
				targetId: aToken.sourceId,
				result: lRes,
				reqType: "rrpc",
				code: 0
			},
			null // aOptions
		);
		} else {
			// console.log( "Reverse RPC request '" + lPath + "(" + lArgs + ")' not granted!" );
		}
	}

}

// add the JWebSocket RPC PlugIn into the BaseClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.RPCClientPlugIn );


//	---------------------------------------------------------------------------
//  jWebSocket JSON client
//	todo: consider potential security issues with 'eval'
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketJSONClient", jws.jWebSocketTokenClient, {

	// this converts a token to a JSON stream
	tokenToStream: function( aToken ) {
		var lJSON = "{utid:" + jws.CUR_TOKEN_ID;
		if( this.fSessionId ) {
			lJSON += ",usid:\"" + this.fSessionId + "\"";
		}
		for( var lKey in aToken ) {
			var lVal = aToken[ lKey ];
			if( typeof lVal == "string" ) {
				lJSON += "," + lKey + ":\"" + lVal + "\"";
			} else {
				lJSON += "," + lKey + ":" + lVal;
			}
		}
		lJSON += "}\n";
		return lJSON;
	},

	streamToToken: function( aStream ) {
		// parsing a JSON object in JavaScript couldn't be simpler...
		// but using 'eval', so be aware of security issues!
		var lObj = null;
		eval( "lObj=" + aStream );
		return lObj;
	}

});


//	---------------------------------------------------------------------------
//  jWebSocket CSV client
//	todo: implement jWebSocket JavaScript CSV client
//	jWebSocket target release 1.1
//	---------------------------------------------------------------------------

jws.oop.declareClass( "jws", "jWebSocketCSVClient", jws.jWebSocketTokenClient, {

	// this converts a token to a CSV stream
	// todo: implement escaping of command separators and equal signs
	tokenToStream: function( aToken ) {
		var lCSV = "utid=" + jws.CUR_TOKEN_ID;
		if( this.fSessionId ) {
			lCSV += ",usid=\"" + this.fSessionId + "\"";
		}
		for( var lKey in aToken ) {
			var lVal = aToken[ lKey ];
			if( lVal === null || lVal === undefined ) {
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
		lCSV += "\n";
		return lCSV;
	},

	// this converts a CSV stream into a token
	// todo: implement escaping of command separators and equal signs
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

jws.oop.declareClass( "jws", "jWebSocketXMLClient", jws.jWebSocketTokenClient, {

	// this converts a token to a XML stream
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
				lXML += "</" + aKey + ">"
			}
			// or do we have an object?
			else if ( typeof aValue  == "object" ) {
				lXML += "<" + aKey + " type=\"" + "object" + "\">";
				for(var lField in aValue ) {
					lXML += obj2xml( lField, aValue[ lField ] );
				}
				lXML += "</" + aKey + ">"
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

	// this converts a XML stream into a token
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


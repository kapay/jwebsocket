//	---------------------------------------------------------------------------
//	jWebSocket jQuery PlugIn (uses jWebSocket Client and Server)
//	(C) 2011 Innotrade GmbH, jWebSocket.org, Herzogenrath
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
// authors: Victor and Carlos

$(document).bind("mobileinit", function(){
	$.jws.open();
    
	//BINDING THIS EVENT IS MORE RECOMMENDED THAN $(document).ready()
	$('#mainPage').live('pagecreate', function(event){
		$("#time").text("Hh:Mm:Ss").css({
			"text-align":"center"
		});
		$.jws.bind("org.jwebsocket.jquery:datetime", function(evt, aToken){
			$("#time").text(aToken.hours +" : "+ aToken.minutes + " : "+aToken.seconds).css({
				"text-align":"center"
			});
		});
	});
});

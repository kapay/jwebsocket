

function makeImgExp( aId ) {
	var lImg = document.getElementById( aId );
	if( lImg ) {
		var lP = document.createElement( "p" );
		lP.innerHTML = "Screenshot";
		lP.className = "screenshotPlus";
		lP.onclick = function() {
			if( lImg.className != "screenshotOff" ) {
				lImg.className = "screenshotOff" ;
				lP.className = "screenshotPlus";
			} else {
				lImg.className = "screenshotOn";
				lP.className = "screenshotMinus";
			}
		};
		lImg.parentNode.insertBefore( lP, lImg );
	}
}

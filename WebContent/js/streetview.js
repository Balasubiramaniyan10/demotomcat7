var imStreetView = new imStreetView();

function imStreetView() {
	this.mapDiv = 'mapdetail';
	this.streetDiv = 'mapdetail';
	this.streetClose = 'imStreetViewClose';
	this.switchPoi='';
	this.point = '';
	this.init = function(ll) {
		this.mapDiv = 'mapdetail';
		this.streetDiv = 'mapstreetview';
		this.switchPoi='switchpoi';
		this.point = ll;
		this.toggleStreetView(true);
	};
	
	this.toggleStreetView = function(bShow) {
		if (bShow) {
			document.getElementById( this.mapDiv ).style.display = 'none'
			if (document.getElementById( this.switchPoi )!=null) document.getElementById( this.switchPoi ).style.display = 'none'
			document.getElementById( this.streetDiv ).style.display = 'block';
			document.getElementById( this.streetClose ).style.display = 'block';
             var myPano = new GStreetviewPanorama( document.getElementById( this.streetDiv ));
			GEvent.addListener(myPano, "error", this.handleNoFlash);
			myPano.setLocationAndPOV(this.point);
		} else {
			document.getElementById( this.streetDiv ).style.display = 'none';
			document.getElementById( this.streetClose ).style.display = 'none';
            document.getElementById( this.mapDiv ).style.display = 'block';
            if (document.getElementById( this.switchPoi )!=null) document.getElementById( this.switchPoi ).style.display = 'block'
    			
		}	
	};
	
	this.handleNoFlash = function(errorCode) {
		if (errorCode == 603) {
		   alert("Error: Flash doesn't appear to be supported by your browser");
		   return;
		}
	}; 
}


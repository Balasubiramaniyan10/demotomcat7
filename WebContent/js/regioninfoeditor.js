function setLocation(id,lat,lng,title){
var changemarker=confirm('Change location of '+title+'?');
  	  					if (changemarker) $.ajax({
  	  	  					url:"https://<%=request.getServerName()%>/editor/updatecoordinates.jsp", 
  	  	  					data:{regionid:id,latitude: lat, longitude: lng},
  	  						success:function(data) {
  	  							if (data!='OK') {
  	  	  							alert("Error. Could not save location!");
  	  							} else {
									
  	  	  						}
  	  	  					},
  	  	  					error:function(data) {
  	  							alert("Error. Could not save location!");
  	  							
  	  	  					}
  	  					});
}
function putonmap(id, text){
	var marker2=new LabeledMarker(mapdetail.getBounds().getCenter(),{title:text,labelText:text,labelClass:'regionpoi',draggable:true});
	GEvent.addListener(marker2, 'dragend', function() {
			var point = this.getLatLng(); 
		  	var lat = point.lat();
		  	var lng = point.lng();
		  	setLocation(id,lat,lng,text);
		});
	mapdetail.addOverlay(marker2);
	$('#regiontabs').tabs().click(0);
}
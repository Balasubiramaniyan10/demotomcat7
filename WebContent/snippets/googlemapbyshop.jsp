	<!--Google Map-->
    <script type="text/javascript">
    function load() {
  		var map = new GMap2(document.getElementById("map"));
  		map.addControl(new GLargeMapControl());
  		var bounds = new GLatLngBounds;
  		map.setCenter(new GLatLng(51,5), 4);
		var icon = new GIcon();
		icon.image = "/images/bottles/0750.png";
		icon.shadow = "/images/bottles/shadow.png";
	  	icon.iconSize = new GSize(12, 40);
	  	icon.shadowSize = new GSize(50, 40);
	  	icon.iconAnchor = new GPoint(6, 40);
	  	icon.infoWindowAnchor = new GPoint(5, 1);
		
		
  		// Add shop locations
   <%if (wineset!=null){
  	out.write("var marker;\n");
  	out.write("var gmarkeroptions;\n"); 
  	out.write("var bounds = new GLatLngBounds;");
  		for (int i=0;i<wineset.Wine.length;i++){
	  		Shop shop=new Shop(wineset.Wine[i].ShopId+"");
	  		if (shop.lat!=0){
	  			//out.write("gmarkeroptions = new Object();gmarkeroptions.title = \""+shop.name+"\";");
	  			out.write("marker=new GMarker(new GLatLng("+shop.lat+","+shop.lon+"),{title:\""+shop.name+"\",icon:icon});\n");
	  			//out.write("marker=new GMarker(new GLatLng("+shop.lat+","+shop.lon+"),mOpts);\n");
	  			out.write("bounds.extend(new GLatLng("+shop.lat+","+shop.lon+"));");
	  			
	  			String info="";
				info+=shop.name+"<br/>";
				info+=shop.address+"<br/>";
				for (int j=0;j<wineset.Wine.length;j++){
					if (wineset.Wine[j].ShopId==shop.id) {
						info+="<a href=\"/link.jsp?wineid="+wineset.Wine[j].Id+"\" target=\"_blank\">"+wineset.Wine[j].Name+" "+wineset.Wine[j].Vintage+" </a>"+Webroutines.formatPrice(wineset.Wine[j].PriceEuroIn,wineset.Wine[i].PriceEuroEx,advice.getCurrency(),"IN")+" Rating:"+advice.rating[j]+".<br/>";
					}
				}
				info=info.replace("'","&apos;");
				out.write("marker.bindInfoWindowHtml('<html><body>"+info+"</body></html>','clickable=true;');\n");
				out.write("map.addOverlay(marker);\n");
	  		}
  		}
   }   	
  		
  	
   %>
	  map.setCenter(bounds.getCenter());
	  var zoom=map.getBoundsZoomLevel(bounds);
	  if (zoom<3) zoom=3;
	  map.setZoom(zoom);
	  	
	}		
	
    </script>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAuPfgtY5yGQowyqWw-A_zlhSWbZnLUFTo4QI7JJ99u1XlFae5FRTIvNMt7rPlWIt8z9eFfYAtlQwM1g"
      type="text/javascript"></script>

    <script type="text/javascript">load();</script>
	<br/><br/>

	<br/>	

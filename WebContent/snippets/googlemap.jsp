<br/><%	
	int n=6; // number of different markers
	int small=0;
	int normal=0;
	int large=0;
	int smallcnt=0;
	int normalcnt=0;
	int largecnt=0;

	//com.freewinesearcher.common.Wineset wineset;
	for (int i=0;i<wineset.Wine.length;i++){
		if (wineset.Wine[i].Size<0.75) small++;
		if (wineset.Wine[i].Size==0.75) normal++;
		if (wineset.Wine[i].Size>0.75) large++;
	}
	

%>
	<!--Google Map-->
    <div id="GoogleMap" style="width: 650px; height: 450px"></div>
	<script type="text/javascript">
    function load() {
    	var map = new GMap2(document.getElementById("GoogleMap"));
  		map.addControl(new GLargeMapControl());
  		var bounds = new GLatLngBounds;
  		map.setCenter(new GLatLng(49,5), 4);
		var icon = new GIcon();
		
		
		
  		// Add markers
   <%if (true){
  	out.write("var marker;\n");
  	out.write("var gmarkeroptions;\n"); 
  	out.write("var bounds = new GLatLngBounds;");
  		for (int i=wineset.Wine.length-1;i>=0;i--){
	  		Shop shop=new Shop(wineset.Wine[i].ShopId);
	  		if (shop.lat!=0){
	  			if (wineset.Wine[i].Size<(float)0.75) {
	  				smallcnt++;
	  				int rank=smallcnt;
	  				if (small>n){
	  					rank=1+Math.round((n-1)*(smallcnt-1)/(small-1));
	  				}
	  				%>
	  				icon.image = "/images/bottles/037<%=rank%>.png";
	  				icon.shadow = "/images/bottles/shadow.png";
	  				icon.iconSize = new GSize(9, 30);
	  				icon.shadowSize = new GSize(38, 30);
	  				icon.iconAnchor = new GPoint(5, 30);
	  				icon.infoWindowAnchor = new GPoint(5, 1);
	  				
	  				<%

	  			}
	  			if (wineset.Wine[i].Size==(float)0.75) {
	  				normalcnt++;
	  				int rank=normalcnt;
	  				if (normal>n){
	  					rank=1+Math.round((n-1)*(normalcnt-1)/(normal-1));
	  				}
	  				%>
	  				icon.image = "/images/bottles/075<%=rank%>.png";
	  				icon.shadow = "/images/bottles/shadow.png";
	  				icon.iconSize = new GSize(12, 40);
	  				icon.shadowSize = new GSize(50, 40);
	  				icon.iconAnchor = new GPoint(6, 40);
	  				icon.infoWindowAnchor = new GPoint(5, 1);
					<%
	  			}
	  			if (wineset.Wine[i].Size>(float)0.75) {
	  				largecnt++;
	  				int rank=largecnt;
	  				if (large>n){
	  					rank=1+Math.round((n-1)*(largecnt-1)/(large-1));
	  				}
	  				%>
	  				icon.image = "/images/bottles/150<%=rank%>.png";
	  				icon.shadow = "/images/bottles/shadow.png";
	  				icon.iconSize = new GSize(18, 60);
	  				icon.shadowSize = new GSize(70, 60);
	  				icon.iconAnchor = new GPoint(9, 60);
	  				icon.infoWindowAnchor = new GPoint(5, 1);
					<%
	  			}

	  			//out.write("gmarkeroptions = new Object();gmarkeroptions.title = \""+shop.name+"\";");
	  			out.write("marker=new GMarker(new GLatLng("+shop.lat+","+shop.lon+"),{title:'"+shop.name+"',icon:icon});\n");
	  			//out.write("marker=new GMarker(new GLatLng("+shop.lat+","+shop.lon+"),mOpts);\n");
	  			out.write("bounds.extend(new GLatLng("+shop.lat+","+shop.lon+"));");
	  			
	  			String info="";
				info+="<a href=\"/link.jsp?wineid="+wineset.Wine[i].Id+"&amp;shopid="+wineset.Wine[i].ShopId+"\" target='_blank'>"+shop.name+"</a><br/>";
				info+="<a href=\"/link.jsp?wineid="+wineset.Wine[i].Id+"\" target=\"_blank\">"+Webroutines.formatSize(wineset.Wine[i].Size).replace("&nbsp;","")+" "+wineset.Wine[i].Name+" "+wineset.Wine[i].Vintage+" </a>"+Webroutines.formatPrice(wineset.Wine[i].PriceEuroIn,wineset.Wine[i].PriceEuroEx,searchdata.getCurrency(),"IN")+(wineset.Wine[i].getAverageRating()>0?(" Rating:"+wineset.Wine[i].getAverageRating()+"."):(""))+"<br/>";
				info=info.replace("'","&apos;");
				out.write("marker.bindInfoWindowHtml('<html><body>"+info+"</body></html>','clickable=true;');\n");
				out.write("map.addOverlay(marker);\n");
	  		}
  		}
   }   	
  		
  	
   %>
	  map.setCenter(bounds.getCenter());
	  map.setZoom(map.getBoundsZoomLevel(bounds));
	  	
	}		
	
    </script>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAuPfgtY5yGQowyqWw-A_zlhSWbZnLUFTo4QI7JJ99u1XlFae5FRTIvNMt7rPlWIt8z9eFfYAtlQwM1g"
      type="text/javascript"></script>

    <script type="text/javascript">load();</script>
	<br/>
<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Region"
	import = "com.freewinesearcher.batch.ProducersOld"
	import = "java.util.ArrayList"
	
	%>

<%@page import="com.freewinesearcher.common.Configuration"%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><html>
<head>
<%@ include file="/header2.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">


</head>
<body>

<%	boolean showmaps=false;
	try{showmaps=Boolean.parseBoolean(request.getParameter("showmaps"));}catch(Exception e){}
	double lat1=44;
	double lat2=44.3;
	double lon1=4.7;
	double lon2=5.3;
	try{
		lat1=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lat1")));
		lat2=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lat2")));
		lon1=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lon1")));
		lon2=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lon2")));
	} catch(Exception e){
		lat1=44;
		lat2=44.3;
		lon1=4.7;
		lon2=5.3;
		}
	ProducersOld producers=new ProducersOld(lat1,lon1,lat2,lon2, true,2005);
%>
	<!--Google Map-->
    <div id="GoogleMap" style="width: 1000px; height: 700px;"></div>
    <input type='button' value='Refresh' onClick='javascript:refreshdata()'>
	<input type='button' value='Show all maps' onClick='javascript:showallmaps()'>
	<script type="text/javascript">
    var map;
  		function load() {
    	map = new GMap2(document.getElementById("GoogleMap"));
    	var lat=<%=(lat1+lat2)/2%>;
    	var lon=<%=(lon1+lon2)/2%>;
    	map.addControl(new GLargeMapControl());
  		var bounds = new GLatLngBounds;
  		bounds.extend(new GLatLng(<%=lat1%>,<%=lon1%>));
  		bounds.extend(new GLatLng(<%=lat2%>,<%=lon2%>));
  		map.setCenter(new GLatLng(lat,lon), 4);
  		map.setZoom(map.getBoundsZoomLevel(bounds));
		var icon = new GIcon();
		var customicon = new GIcon();
      	customicon.image = "/images/icons/questionmark.png";
      	customicon.iconSize = new GSize(35, 35);
      	customicon.iconAnchor = new GPoint(18,18);
      	customicon.transparent = "/images/icons/empty.png";
      	customicon.infoWindowAnchor = new GPoint(18,18 );
      	customicon.imageMap=[0,0,0,35,35,35,35,0,0,0];
      	var invicon = new GIcon();
		invicon.image = "/images/transparent.gif";
		invicon.iconSize = new GSize(0, 0);
	  	invicon.shadowSize = new GSize(0, 0);
	  	invicon.iconAnchor = new GPoint(0, 0);
		
		
  		// Add markers
   <%if (true){
    out.write("var marker;\n");
  	out.write("var gmarkeroptions;\n"); 
  	out.write("var bounds = new GLatLngBounds;");
  		for (int i=0;i<producers.name.size();i++){
	  			out.write("marker=new LabeledMarker(new GLatLng("+producers.lat.get(i)+","+producers.lon.get(i)+"),{title:\""+producers.name.get(i).replace("'","&apos;").replace("\"","&apos;")+"\",labelText:\""+(i+1)+"\",labelClass:\"regionpoi\",icon:invicon,labelOffset:new GSize(-5, -10)});\n");
	  			//out.write("marker=new GMarker(new GLatLng("+shop.lat+","+shop.lon+"),mOpts);\n");
	  			out.write("bounds.extend(new GLatLng("+producers.lat.get(i)+","+producers.lon.get(i)+"));");
	  			out.write("GEvent.addListener(marker, 'click', function() {  window.location.hash='producer"+(i+1)+"'; });");
	  			out.write("map.addOverlay(marker);\n");
	  			//out.write("clusterer.AddMarker(marker, '"+producers.name.get(i)+"')");
	  			
	  		
  		}
   }   	
  		
  	if (false){
   %>
	  
	  map.setCenter(bounds.getCenter());
	  map.setZoom(map.getBoundsZoomLevel(bounds));
	  	<%}%>
	}		
	
    </script>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKey%>"
      type="text/javascript"></script>
<script type="text/javascript" src="/js/googlemaps/labeled_marker.js"></script>
    <script type="text/javascript">load();</script>
	<script type="text/javascript">
	function refreshdata(){
		var bounds2 = map.getBounds();
		var southWest = bounds2.getSouthWest();
  		var northEast = bounds2.getNorthEast();
  		var newlocation="/admin/visitingguide.jsp?lat1="+southWest.lat()+"&lat2="+northEast.lat()+"&lon1="+southWest.lng()+"&lon2="+northEast.lng();
  		location.href=newlocation;
  		
        
	}
	function showallmaps(){
		var bounds2 = map.getBounds();
		var southWest = bounds2.getSouthWest();
  		var northEast = bounds2.getNorthEast();
  		var newlocation="/admin/visitingguide.jsp?lat1="+southWest.lat()+"&lat2="+northEast.lat()+"&lon1="+southWest.lng()+"&lon2="+northEast.lng()+"&showmaps=true";
  		location.href=newlocation;
  		
        
	}
	</script>
	<br/>
	<%
	if (producers.numberofproducers>producers.maxproducers) out.write("<h3>Please zoom in, too many producers found</h3>");
	for (int i=0;i<producers.name.size();i++){
		if (showmaps==true){
		%>
    <div style="width: 310px; height: 150px;clear:left;float:left;padding-top:8px;">
    
    	<div style="width: 150px; height: 150px;clear:left;float:left;border:solid;border-width:1px;">
			<img src='http://maps.google.com/maps/api/staticmap?center=<%=producers.lat.get(i) %>,<%=producers.lon.get(i) %>&markers=<%=producers.lat.get(i) %>,<%=producers.lon.get(i) %>,red&zoom=<%=producers.accuracy.get(i)+5%>&size=150x150&sensor=false'/>
		</div>
		<div style="width: 4px; height: 150px;float:left;border:none;"/>
		</div>
		<div style="width: 150px; height: 150px;float:left;border:solid;border-width:1px;">
			<img src='http://maps.google.com/maps/api/staticmap?center=<%=producers.lat.get(i) %>,<%=producers.lon.get(i) %>&markers=<%=producers.lat.get(i) %>,<%=producers.lon.get(i) %>,red&zoom=<%=producers.accuracy.get(i)+9%>&size=150x150&sensor=false'/>
		</div>
		<div style="width: 4px; height: 150px;float:left;border:none;"/>
		</div>
	</div>
<%	
	}
	out.write("<div style='float:left;width:600px;'><a name=\"producer"+(i+1)+"\"></a><h2>"+(i+1)+" "+producers.name.get(i)+"</h2>");
	out.write(producers.address.get(i)+",<br/>");
	out.write(producers.telephone.get(i)+",<br/>");
	out.write(producers.visiting.get(i)+"<br/>");
	out.write("<table>");
	
	if (producers.wines.get(i).size()>0){
		for (int j=0;j<producers.wines.get(i).size();j++){
		out.write("<tr");
		if (producers.avgrelprice>0&&producers.relativeratings.get(i).get(j)>0&&producers.relativeratings.get(i).get(j)/producers.avgrelprice<0.8) out.write(" style='font-weight:bold;'");
		out.write("><td>"+producers.wines.get(i).get(j)+"</td><td>"+(producers.prices.get(i).get(j)>0?"&euro;&nbsp;"+Webroutines.formatPrice(producers.prices.get(i).get(j))+" ":"")+"</td><td>"+producers.ratings.get(i).get(j)+"</td></tr>");
		
		//out.write(producers.ratings.get(i).get(j).author+": "+producers.ratings.get(i).get(j).ratinglow+"<br/>");
		}
	}
	out.write("</table>");
	
	out.write("</div>");
	
	}
	%>
	</body></html>
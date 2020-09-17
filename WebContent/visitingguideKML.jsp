<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.sql.Time"%>
<%@page import="java.sql.Date"%>
<%@page import="java.util.Set"%>
<%@page import="com.freewinesearcher.common.POI"%>
<%@page import="com.freewinesearcher.online.MapDataset"%>
<%@page import="com.freewinesearcher.online.Bounds"%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="com.freewinesearcher.online.PageHandler"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Region"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Configuration"
	import="com.freewinesearcher.online.Producer"%>
	
	
	

<%@page import="com.freewinesearcher.common.Configuration"%> 
<%@page import="com.freewinesearcher.online.*"%>
<%@page import="java.util.Iterator"%>
<%PageHandler p=PageHandler.getInstance(request,response,"Visiting Guide"); %>
<%	boolean showmaps=false;
	try{showmaps=Boolean.parseBoolean(request.getParameter("showmaps"));}catch(Exception e){}
	Bounds bounds=new Bounds();
	bounds.latmin=44.2;
	bounds.latmax=44.3;
	bounds.lonmin=4.8;
	bounds.lonmax=5.0;
	try{
		bounds.latmin=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lat1")));
		bounds.latmax=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lat2")));
		bounds.lonmin=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lon1")));
		bounds.lonmax=Double.parseDouble(Webroutines.filterUserInput(request.getParameter("lon2")));
	} catch(Exception e){
		
	 }
	
	
MapDataset md=new MapDataset();
md.extrazoomlevel=1;
md.bounds=bounds;
md.mapid="map";
boolean mapedit=false;
try{mapedit=(Boolean)request.getAttribute("edit");}catch(Exception e){}
Bounds mapbounds=null;
mapbounds=md.bounds;

%> 
<head>
<title>Visiting guide</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
<%@ include file="/header2.jsp" %>


<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKeyDev%>"
      type="text/javascript"></script>

<script type="text/javascript">
/*<![CDATA[*/


           var vpmapcenter;
var vpmapzoomlevel=0;


//var infoWindow= new google.maps.InfoWindow();
var markers= [];
var m=0;
var kmlLayer;
var gml;
var goverlays = [];


	function refreshdata(){
		var bounds2 = map.getBounds();
		var southWest = bounds2.getSouthWest();
  		var northEast = bounds2.getNorthEast();
  		var newlocation="?lat1="+southWest.lat()+"&lat2="+northEast.lat()+"&lon1="+southWest.lng()+"&lon2="+northEast.lng();
  		location.href=newlocation;
  		
        
	}

    function createMarker(point, title,htmlstr, shopid,icon) {
  	  var marker = new google.maps.Marker({position:point,title:title,icon:reddoticon,map:map});
  	  marker.marker_id = shopid;
      google.maps.event.addListener(marker, 'click', function() {
    	  infoWindow.open(map, marker);

    	  infoWindow.setContent(htmlstr);
    	  
        });
  	
        //GEvent.addListener(marker, "infowindowopen", function(marker)  {loadmarker(marker,marker.marker_id);}); 
        //GEvent.addListener(marker, 'mouseover', function() { GEvent.trigger(marker, 'click');  });
  	  return marker;
  	}
	function addtovisitlist(id, name){
		var div = document.createElement('div');
		div.innerHTML = name;
		document.getElementById("visitlist").appendChild(div);
	}
	
	function createSidebarEntry(marker, head, body, distance) {
		var n=m;
  	  var div = document.createElement('div');
  	  var html = '<font style="text-decoration:underline">' + head + '</font><br/>' + body;
  	  div.innerHTML = html;
  	  div.style.cursor = 'pointer';
  	  div.style.marginBottom = '5px';
  		google.maps.event.addDomListener(div, 'click', function() {
  	  	google.maps.event.trigger(markers[n], 'click');
  	  });
  		google.maps.event.addDomListener(div, 'mouseover', function() {
  	    div.style.backgroundColor = '#eee';
  	  });
  		google.maps.event.addDomListener(div, 'mouseout', function() {
  	    div.style.backgroundColor = '#fff';
  	  });
    	  m++;
  	  return div;
  	}


	           
var <%=md.mapid%>;
function load<%=md.mapid%>() {
//	    	var sidebar=document.getElementById("sidebar");
	    	var myOptions = {
			  mapTypeId: 'roadmap' 
			};
			//<%=md.mapid%> = new google.maps.Map(document.getElementById("map"), myOptions);
	  	//kmlLayer = new google.maps.KmlLayer('https://test.vinopedia.com/vp.kml');
	  	//var kml = new GGeoXml('https://test.vinopedia.com/vp.kml?session=<%=session.getId()%>');
  		 //map.addOverlay(kml)
  		 map = new GMap2(document.getElementById("map"));
map.addControl(new GLargeMapControl3D());

map.addControl(new GMapTypeControl());
map.addMapType(G_SATELLITE_3D_MAP);


var bounds = new google.maps.LatLngBounds(new google.maps.LatLng(<%=bounds.latmin%>,<%=bounds.lonmin%>),new google.maps.LatLng(<%=bounds.latmax%>,<%=bounds.lonmax%>));
vpmapzoomlevel=<%=md.mapid%>.getBoundsZoomLevel(bounds)-1+<%=md.extrazoomlevel%>;
	if (vpmapzoomlevel>14) vpmapzoomlevel=14;

	vpmapcenter=bounds.getCenter();
	
	<%=md.mapid%>.setCenter(vpmapcenter, vpmapzoomlevel); 
	
	map.infoWindowEnabled(true); 
    
	//gml = new EGeoXml("gml", map, "https://test.vinopedia.com/vp.kml");
	//gml = new GeoXml("gml", map, "https://test.vinopedia.com/KML/?BBOX=<%=bounds.lonmin%>,<%=bounds.latmin%>,<%=bounds.lonmax%>,<%=bounds.latmax%>", {sidebarid:"sidebar",iwwidth:250 });
	
    //gml.parse();
	//GEvent.addListener(map,"addoverlay",function(overlay){traverseKml(overlay);});
	//geoXml = new GGeoXml("https://test.vinopedia.com/KML/?BBOX=<%=bounds.lonmin%>,<%=bounds.latmin%>,<%=bounds.lonmax%>,<%=bounds.latmax%>");
	//geoXml = new GGeoXml("https://test.vinopedia.com/vp.kml?session=<%=new java.sql.Timestamp(new java.util.Date().getTime())%>");

	//map.addOverlay(geoXml);
	
	//var kml = new GGeoXml("http://maps.google.com/maps/ms?ie=UTF8&hl=en&om=1&msa=0&output=nl&msid=103763259662194171141.00000111b083b28bf007c");
	var kml = new GGeoXml("https://<%=("DEV".equals(Configuration.serverrole)?"test":"www")%>.vinopedia.com/vp.kml/<%=session.getId()%>");
	map.addOverlay(kml);
	
  		 
	  	//var ctaLayer = new google.maps.KmlLayer('https://test.vinopedia.com/vp.kml?session=<%=new java.sql.Timestamp(new java.util.Date().getTime())%>',{preserveViewport:true});
	    //ctaLayer.setMap(map);
	
	
}

var intervalID = setInterval(function(){
	$.post("/KML/?output=list",function(data){if (data!=null&&!data=='') $('#sidebar').html(data);$('.vpmarker').onclick='alert($(".vpmarker").getAttribute("id"));'});
	}, 1000);



 


		/*]]>*/	

	</script>

</head>

<body >

<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<div class='centertext'>

	<!--Google Map-->
    
 	<h2>Wine map</h2>
 	Drag and zoom to the area you would like to visit, then click refresh to display winery information.<br/>
 	<input type='button' value='Refresh' onclick='javascript:refreshdata()'/>
	<input type='button' value='Show all maps' onclick='javascript:showallmaps()'/><br/>
	Or go straight to some famous wine regions:<br/>
	<a href="/visitingguide.jsp?lat1=48.78397854482421&lat2=49.4133884143308&lon1=3.3901523590087557&lon2=4.488785171508756">Champagne</a>, <a href="/visitingguide.jsp?lat1=46.66518009747812&lat2=47.32086148291153&lon1=4.341843032836881&lon2=5.440475845336881">Bourgogne</a>, <a href=""></a>, <a href=""></a>, <a href=""></a>, <a href=""></a>
 	

<div id='sidebar' class='sidebar'>

</div><div id="map" style="float:left;width: 800px; height: 700px;">Map</div>
<div id="visitlist"></div>

<script type='text/javascript'>loadmap();</script>
	
	<%@ include file="/snippets/footer.jsp" %>
</div>	
	
	</body></html>
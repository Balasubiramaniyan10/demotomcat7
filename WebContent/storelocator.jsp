<%
//StoreLocator sl=new StoreLocator();
//try{sl.setProducer(Integer.parseInt(request.getParameter("id")));}catch(Exception e){}
PageHandler p=PageHandler.getInstance(request,response);
if ("Pageload".equals(p.getPageaction())) if (request.getParameter("knownwineid")!=null||request.getParameter("id")!=null) {
	p=PageHandler.getInstance(request,response,(request.getParameter("knownwineid")==null?"Locator "+request.getParameter("id"):"Search map"));
} else {
	p=PageHandler.getInstance(request,response,"Heatmap");
}
try{if (request.getParameter("knownwineid")!=null) p.getLogger().knownwineid=Integer.parseInt(request.getParameter("knownwineid"));}catch(Exception e){}
try{if (request.getParameter("vintage")!=null) p.getLogger().vintage=(request.getParameter("vintage"));}catch(Exception e){}
//sl.setCountrycode(request.getParameter("country"));
//if (sl.getCountrycode()==null||sl.getCountrycode().length()==0) sl.setCountrycode(p.hostcountry);
//sl.setMetrics("km");
//if (sl.getCountrycode()!=null&&"US,CA".contains(sl.getCountrycode().toUpperCase())) sl.setMetrics("mi");
//String address=request.getParameter("address");
//StoreLocator.Location location=null;
int width=950;
int height=500;
if (request.getParameter("width")!=null) try{width=Integer.parseInt(Webroutines.getRegexPatternValue("(\\d+)px",request.getParameter("width")));}catch(Exception e){}
if (request.getParameter("height")!=null) try{height=Integer.parseInt(Webroutines.getRegexPatternValue("(\\d+)px",request.getParameter("height")));}catch(Exception e){}
String color="#4D0027";

//try{location=(StoreLocator.Location)session.getAttribute(address);}catch(Exception e){Dbutil.logger.info(address,e);}
//if (location==null){
//	if (address==null){
		//location=new StoreLocator.Location(request.getRemoteAddr());
//		address=location.address;
//	} else {
//		location=StoreLocator.Location.getAddressLocation(address);
//	}
//}
//session.setAttribute(address,location);

//sl.setLocation(location);
//HashMap<Integer,StoreLocator.StoreData> sd=sl.getStores();%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.*"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<script type="text/javascript" src="//maps.googleapis.com/maps/api/js?key=<%=Configuration.GoogleApiKeyv3%>&sensor=false"></script>
<!--  <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKey%>" type="text/javascript"></script>-->
<script src="/js/googlemaps/labeled_markerv3.js"></script>
<script type="text/javascript" src="http://www.google.com/jsapi?key=<%=Configuration.GoogleApiKey%>"></script>
<script type="text/javascript">google.load("jquery", "1.4.2");</script>
<style type="text/css">
.infow, .infow a{font-family:arial;font-size:12px;color:<%=color%>}
.sidebar {font-family:arial;font-size:12px;color:<%=color%>;float:left;width: 190px;padding:5px; height:<%=(height-12)%>px;overflow:auto;'}
</style>
</head><body style='margin:0px;padding:0px;width:950px;min-width:950px;'>
<%@ page import = "com.freewinesearcher.online.*"	
 import = "com.freewinesearcher.common.*"	
 import = "com.freewinesearcher.batch.Coordinates"	%>
 

<%
out.write("<div id='sidebar' class='sidebar'></div><div id='map' class='map' style='float:left;width: "+(width-200)+"px; height: "+(height)+"px;'></div>");
MapDataset mapdataset=new MapDataset();
Set<POI> pois=new LinkedHashSet<POI>();

mapdataset.pois=pois;
mapdataset.mapid="map";
//mapdataset.bounds=sl.bounds;
mapdataset.extrazoomlevel=2;
mapdataset.maptype="google.maps.MapTypeId.ROADMAP";
request.setAttribute("mapdataset",mapdataset);
%><script type="text/javascript">
//<![CDATA[
var producer=<%=request.getParameter("id")%>;      
var knownwineid=<%=request.getParameter("knownwineid")%>;      
var vintage=<%=request.getParameter("vintage")%>;   
var showprices=<%=request.getParameter("showprices")%>;
var marker;
var lastopeninfowindow;
var gmarkeroptions; 
var bounds = new google.maps.LatLngBounds();     
var map = new google.maps.Map(document.getElementById("map"),{
	mapTypeId: google.maps.MapTypeId.ROADMAP,mapTypeControl: true,
    scaleControl: true
});
var iconimage = "/images/greenmarker.gif";
var iconimagered = "/images/redmarker.gif";
if(false){////v3
var mapmarkers= [];
	var reddoticon = new GIcon();
	reddoticonimage = "/images/redmarker.gif";
	reddoticon.iconSize = new GSize(14,15);
	reddoticon.shadowSize = new GSize(0, 0);
	reddoticon.iconAnchor = new GPoint(7,7);
	reddoticon.infoWindowAnchor  = new GPoint(7,7);
	var greendoticon = new GIcon();
	greendoticon.image = "/images/greenmarker.gif";
	greendoticon.iconSize = new GSize(14,15);
	greendoticon.shadowSize = new GSize(0, 0);
	greendoticon.iconAnchor = new GPoint(7,7);
	greendoticon.infoWindowAnchor  = new GPoint(7,7);
	var sidebar=document.getElementById("sidebar");
}
var sidebar=document.getElementById("sidebar");
	
function drawmap(markers){
	loadmap(markers);
	////v3map.addControl(new GMapTypeControl());
   	////v3map.addControl(new GLargeMapControl());
   	map.fitBounds(bounds);
	////v3var zoom=map.getBoundsZoomLevel(bounds);
   	////v3if (zoom>10) zoom=10;
   	////v3map.setCenter(bounds.getCenter(), zoom); 
   	////v3google.maps.event.addListener(map, "dragend", function(){refresh();});
   	////v3google.maps.event.addListener(map, "zoomend", function(){refresh();});
}
    function createSidebarEntry(marker, name, address, distance) {
    	  var div = document.createElement('div');
    	  var html = '<font style="text-decoration:underline">' + name + '</font><br/>' + address;
    	  div.innerHTML = html;
    	  div.style.cursor = 'pointer';
    	  div.style.marginBottom = '5px';
    	  google.maps.event.addDomListener(div, 'click', function() {
    		  google.maps.event.trigger(marker, 'click');
    	    
    	  });
    	  google.maps.event.addDomListener(div, 'mouseover', function() {
    	    div.style.backgroundColor = '#eee';
    	  });
    	  google.maps.event.addDomListener(div, 'mouseout', function() {
    	    div.style.backgroundColor = '#fff';
    	  });
    	  return div;
    	}

    function createMarker(point, title,htmlstr, shopid,icon) {
    	////v2var marker = new google.maps.Marker(point,{title:title,icon:icon});
    	var marker = new google.maps.Marker({position:point,title:title,icon:icon,map:map});
  	  	var infoDiv = document.createElement('div');
    	  infoDiv.innerHTML = htmlstr;
          marker.marker_id = shopid;
          var infowindow = new google.maps.InfoWindow({
        	    content: htmlstr
        	  });
          marker.addListener('click', function() {
        	  if(lastopeninfowindow) lastopeninfowindow.close();
        	    infowindow.open(map, marker);
        	  });
          google.maps.event.addListener(infowindow, 'domready', function(){
        	  loadmarker(marker,marker.marker_id,infowindow);
        	    //code to dynamically load new content to infowindow
        	    //for example:
        	    //    var existing_content = referenceToInfoWindow.getContent();
        	    //    var new_content = "...";
        	    //    referenceToInfoWindow.setContent(existing_content + new_content);
        	}); 
          ////v3marker.bindInfoWindow(infoDiv);
          ////v3GEvent.addListener(marker, "infowindowopen", function(marker)  {loadmarker(marker,marker.marker_id);}); 
          ////v3GEvent.addListener(marker, 'mouseover', function() { GEvent.trigger(marker, 'click');  });
          google.maps.event.addListener(marker, "mouseover", function() {google.maps.event.trigger(marker, 'click');  });
    	  return marker;
    	}
    		   	
   		   	
    function loadmap(markers) {
        ////v3map.clearOverlays();
        sidebar.innerHTML='';
	  	for (var i = 0; i < markers.length; i++) {
	  		var name = markers[i].name;
	  		var title = markers[i].title;
	  		var html = markers[i].html;
	  		var shopid=markers[i].shopid;
	  		var address = markers[i].address;
	  		var distance = markers[i].distance;
	        var point = new google.maps.LatLng(parseFloat(markers[i].lat),
	                                parseFloat(markers[i].lon));
			//console.log(markers[i].offers);
	        var icon=iconimage;
			if (markers[i].offers==0) icon=iconimagered;
	        var marker=createMarker(point,title,html,shopid,icon);
	        marker.setMap(map);
	        ////v3map.addOverlay(marker);
			var sidebarEntry = createSidebarEntry(marker, name, address, distance);
	        sidebar.appendChild(sidebarEntry);
	        bounds.extend(point);
	      }
   }
  //]]>
	
function success(position) {
  lat=position.coords.latitude;
  lon=position.coords.longitude;
  ////v3map.clearOverlays();
  initmap();
  }


</script>


<script type="text/javascript" >
var ne;
var sw;
google.maps.event.addListener(map, 'idle', function() {
    var bounds =  map.getBounds();
    ne = bounds.getNorthEast();
    sw = bounds.getSouthWest();
    refresh();
})
function getMapBounds(){
	////v3var southWest = map.getBounds().getSouthWest();
	////v3var northEast = map.getBounds().getNorthEast();
	return ('lonmin='+sw.lng()+"&lonmax="+ne.lng()+'&latmin='+sw.lat()+"&latmax="+ne.lat());
}
var lat=48.85;
var lon=2.31;
var currency='EUR';
if (google.loader.ClientLocation) {lat=google.loader.ClientLocation.latitude;lon=google.loader.ClientLocation.longitude;if (google.loader.ClientLocation.address.country_code == "US") currency='USD';if (google.loader.ClientLocation.address.country_code == "CA") currency='CAD';if (google.loader.ClientLocation.address.country_code == "CH") currency='CHF'; }
else {
	if (navigator.geolocation) {
		  navigator.geolocation.getCurrentPosition(success);
		}
}

function loadmarker(marker,shopid,infowindow){
	if (document.getElementById('store'+shopid)!=null){
	$.post('/storelocatorjson.jsp','action=getwines&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&shopid='+shopid+'&showprices='+showprices+'&currency='+currency, function(data) {
		infowindow.setContent(data.wines);
		if(lastopeninfowindow) lastopeninfowindow.close();
  	  
		lastopeninfowindow=infowindow;
  	  
		//marker.bindInfoWindowHtml("<div class='infow'>"+data.wines+"</div>");
		//GEvent.trigger(marker, 'click');
		},"json");
	}
}

function refresh(){
	$.post('/storelocatorjson.jsp','action=getstores&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&'+getMapBounds()+'&showprices='+showprices+'&currency='+currency, function(data) {
		  loadmap(data);
		},"json");
	}
function initmap(){
	bounds = new google.maps.LatLngBounds;  
	$.post('/storelocatorjson.jsp','action=initmap&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&lat='+lat+'&lon='+lon+'&showprices='+showprices+'&currency='+currency, function(data) {
		  drawmap(data);;
		},"json");
	
	}
initmap();

</script>

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-1788182-2', 'auto');
<%=PageHandler.getInstance(request,response).asyncGAtracking%>
ga('send', 'pageview');
</script>
<!-- End Google Analytics -->

<%
PageHandler.getInstance(request,response).getLogger().logaction();
PageHandler.getInstance(request,response).firstrequest=false;
PageHandler.getInstance(request,response).cleanup(); %>
</body>
</html>
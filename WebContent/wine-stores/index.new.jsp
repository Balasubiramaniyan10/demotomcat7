<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%@page import="java.util.Set"%>
<%@page import="com.freewinesearcher.common.POI"%>
<%@page import="java.util.LinkedHashSet"%>
<%@page import="com.freewinesearcher.online.MapDataset"%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN"><%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%> <%String color="#4D0027"; %>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>

<script type="text/javascript" src="http://www.google.com/jsapi?key=<%=Configuration.GoogleApiKey%>"></script>
<script type="text/javascript">google.load("jquery", "1.4.2");</script>
<style type="text/css">
.infow, .infow a{font-family:arial;font-size:12px;color:<%=color%>}
.sidebar {font-family:arial;font-size:12px;color:<%=color%>;float:left;width: 190px;padding:5px; height:<%=(600-12)%>px;overflow:auto;'}
</style>
<title>Wine stores and new deals close to you</title>
<meta name="description" content="A map showing you nearby wine stores and which stores have new and interesting deals."/>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Heatmap");%>
<%@ include file="/header2.jsp" %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h1>What is happening in wine stores around you?</h1>
We track the inventory and prices of thousands of wine stores. Because we daily update all our price information, we know about new wines coming to the market immediately. On the map below, we show wine stores close to you (use the zoom function if your location is not correct). A green dot means that a store added new wines to their inventory during the past week.<br/><br/><br/>
<div id='storelocator' style='width:1000px;height:700px;border:1px solid #4d0027;'>
<%
out.write("<div id='sidebar' class='sidebar'></div><div id='map' class='map' style='float:left;width: "+(800)+"px; height: "+(700)+"px;'></div>");
Set<POI> pois=new LinkedHashSet<POI>();

%><script type="text/javascript">
//<![CDATA[
var producer=<%=request.getParameter("id")%>;      
var knownwineid=<%=request.getParameter("knownwineid")%>;      
var vintage=<%=request.getParameter("vintage")%>;   
var showprices=<%=request.getParameter("showprices")%>;
var marker;
var gmarkeroptions; 
var bounds = new google.maps.LatLngBounds();

var sidebar=document.getElementById("sidebar");
	

    function createSidebarEntry(shopid,marker, name, address, distance) {
    	  var div = document.createElement('div');
    	  var html = '<font style="text-decoration:underline">' + name + '</font><br/>' + address;
    	  div.innerHTML = html;
    	  div.style.cursor = 'pointer';
    	  div.style.marginBottom = '5px';
    	  div.addEventListener( 'click', function() {
    		  alert(document.getElementById("shop"+shopid+""));
    		  google.maps.event.trigger(document.getElementById("shop"+shopid+""), 'click');
    		  
    	  });
    	  div.addEventListener( 'mouseover', function() {
    	    div.style.backgroundColor = '#eee';
    	  });
    	  div.addEventListener( 'mouseout', function() {
    	    div.style.backgroundColor = '#fff';
    	  });
    	  return div;
    	}

    	   	
    
  //]]>
	
function success(position) {
  lat=position.coords.latitude;
  lon=position.coords.longitude;
  reinitmap();
  }


</script>


<script type="text/javascript" >
var map;
var lat=48.85;
var lon=2.31;
var currency='EUR';
if (google.loader.ClientLocation) {lat=google.loader.ClientLocation.latitude;lon=google.loader.ClientLocation.longitude;if (google.loader.ClientLocation.address.country_code == "US") currency='USD';if (google.loader.ClientLocation.address.country_code == "CA") currency='CAD';if (google.loader.ClientLocation.address.country_code == "CH") currency='CHF'; }
else {
	if (navigator.geolocation) {
		  navigator.geolocation.getCurrentPosition(success);
		}
}
function loadmap(markers) {
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
		var sidebarEntry = createSidebarEntry(shopid,marker, name, address, distance);
		bounds.extend(point);
        sidebar.appendChild(sidebarEntry);
      }
}
function loadmarker(marker,shopid){
	if (document.getElementById('store'+shopid)!=null){
	$.post('/storelocatorjson.jsp','action=getwines&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&shopid='+shopid+'&showprices='+showprices+'&currency='+currency, function(data) {
		marker.bindInfoWindowHtml("<div class='infow'>"+data.wines+"</div>");
		GEvent.trigger(marker, 'click');
		},"json");
	}
}

function refresh(){
	$.post('/storelocatorjson.jsp','action=getstores&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&'+getMapBounds()+'&showprices='+showprices+'&currency='+currency, function(data) {
		  loadmap(data);
		},"json");
	}
	
function initmap(){
	$.post('/storelocatorjson.jsp','action=initmap&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&lat='+lat+'&lon='+lon+'&showprices='+showprices+'&currency='+currency, function(data) {
		  drawmap(data);
		},"json");
	}
function reinitmap(){
	bounds = new google.maps.LatLngBounds();
	$.post('/storelocatorjson.jsp','action=initmap&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&lat='+lat+'&lon='+lon+'&showprices='+showprices+'&currency='+currency, function(data) {
		  loadmap(data);
		  map.fitBounds(bounds);
		},"json");
	}
function drawmap(){
	var myOptions = {
			center: new google.maps.LatLng(lat,lon),
			zoom:8,
		    panControl: true,
		    zoomControl: true,
		    scaleControl: true,
		    mapTypeId: google.maps.MapTypeId.ROADMAP
		  }

	map = new google.maps.Map(document.getElementById("map"),myOptions);
	google.maps.event.addListener(map, 'bounds_changed', function() {
		    refresh();
		  });
	var georssLayer = new google.maps.KmlLayer('https://test.vinopedia.com/StoreKML?v=22',{preserveViewport:true}); 
	georssLayer.setMap(map);
	//map.fitBounds(bounds);
	//bounds = new google.maps.LatLngBounds();
	//$.post('/storelocatorjson.jsp','action=initmap&producer='+producer+'&knownwineid='+knownwineid+'&vintage='+vintage+'&lat='+lat+'&lon='+lon+'&showprices='+showprices+'&currency='+currency, function(data) {
	//	  drawmap(data);refresh();
//		},"json");
	
	}
initmap();
function getMapBounds(){
	var southWest = map.getBounds().getSouthWest();
	var northEast = map.getBounds().getNorthEast();
	return ('lonmin='+southWest.lng()+"&lonmax="+northEast.lng()+'&latmin='+southWest.lat()+"&latmax="+northEast.lat());
}
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


<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	
</div>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
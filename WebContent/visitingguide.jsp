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
		if (session.getAttribute("lonmax")!=null){
			bounds.latmin=(Double) session.getAttribute("latmin");
			bounds.latmax=(Double) session.getAttribute("latmax");
			bounds.lonmin=(Double) session.getAttribute("lonmin");
			bounds.lonmax=(Double) session.getAttribute("lonmax");
		}
			
	 }
	
	

%> 
<head>
<title>Visiting guide</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
<%@ include file="/header2.jsp" %>
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKeyDev%>"  type="text/javascript"></script>
<script type="text/javascript" src="/js/googlemaps/labeled_marker.js"></script>
<script type="text/javascript">
/*<![CDATA[*/
var vpmapcenter;
var vpmapzoomlevel=0;
var markers= [];
var visits=[];
var m=0;
var bounds;
var map;
var myPano;
var panoClient = new GStreetviewClient();
var vcTimeout;
var hassv;
var reddoticon = new GIcon();
	reddoticon.image = "/images/smallreddot32.gif";
	reddoticon.iconSize = new GSize(32,32);
	reddoticon.shadowSize = new GSize(0, 0);
	reddoticon.iconAnchor = new GPoint(16,16);
	reddoticon.infoWindowAnchor  = new GPoint(16,16);
	var invicon = new GIcon();
	invicon.image = "/images/transparent.gif";
	invicon.iconSize = new GSize(0, 0);
  	invicon.shadowSize = new GSize(0, 0);
  	invicon.iconAnchor = new GPoint(0, 0);


function addtovisitlist(id, name){
		var li = document.createElement('li');
		li.innerHTML = name+" (<span class='jslink' onclick='removefromlist(this,\""+id+"\")>Remove</span>)";
		if (!contains(visits,id)) {
			visits[visits.length]=id;
			document.getElementById("visitlist").appendChild(li);
		}
		$("#addtolist").replaceWith("On visit list");
		$('#producers').val(visits+"");
	}
function removefromlist(el,id){
		visits = jQuery.grep(visits, function(value) {
			return value != id;
		});
		$('#producers').val(visits+"");
		$(el).parent().remove();
	}
function contains(arr,el){
		for (var i = 0; i < arr.length; i++) {
			if (arr[i] == el) {
				return true;
			}
		}
		return false;
}
function newview(lat1,lon1,lat2,lon2){
	bounds = new google.maps.LatLngBounds(new google.maps.LatLng(lat1,lon1),new google.maps.LatLng(lat2,lon2));
	vpmapzoomlevel=map.getBoundsZoomLevel(bounds);
	vpmapcenter=bounds.getCenter();
	map.setCenter(vpmapcenter, vpmapzoomlevel); 
	
}
	


function loadmap() {
   	map = new GMap2(document.getElementById("map"));
	map.addControl(new GLargeMapControl3D());
	map.addControl(new GMapTypeControl());
	map.removeMapType(G_SATELLITE_MAP);
	map.addMapType(G_SATELLITE_3D_MAP);
	map.setMapType(G_HYBRID_MAP); 
	newview(<%=bounds.latmin%>,<%=bounds.lonmin%>,<%=bounds.latmax%>,<%=bounds.lonmax%>);
	vpmapzoomlevel=map.getBoundsZoomLevel(bounds);
	vpmapcenter=bounds.getCenter();
	map.setCenter(vpmapcenter, vpmapzoomlevel); 
	map.enableContinuousZoom();
	map.enableScrollWheelZoom();
	$("#message").appendTo(map.getPane(G_MAP_FLOAT_SHADOW_PANE));
	GEvent.addListener(map,"moveend",function(){if (vcTimeout!=null) window.clearTimeout(vcTimeout);vcTimeout=window.setTimeout('refresh()',1000);}); 
	GEvent.addListener(map,"move",function(){if (vcTimeout!=null) window.clearTimeout(vcTimeout);vcTimeout=null;});
	GEvent.addListener(map,"zoomend",function(){if (vcTimeout!=null) window.clearTimeout(vcTimeout);vcTimeout=window.setTimeout('refresh()',1000);});
    refresh();
    myPano = new GStreetviewPanorama(document.getElementById("pano"));
    map.getInfoWindow(); 
    GEvent.addListener(myPano, "error", function(errorCode) {});
	
}

function checksv(point){
	hassv=false;
	panoClient.getNearestPanoramaLatLng(point,function(data){
		if (data!=null) {
			hassv=true;
			$('#svlink').css("display","");
		}
	});
}
function streetview(point){
	$('#panobox').css("display","block");
	myPano = new GStreetviewPanorama(document.getElementById("pano"));
    myPano.setLocationAndPOV(point);
}
function closestreetview(){
	$('#panobox').css("display","none");
}

function refresh(){
	
		bounds=map.getBounds();	
		$.getJSON("/KML/?BBOX="+bounds.getSouthWest().lng()+","+bounds.getSouthWest().lat()+","+bounds.getNorthEast().lng()+","+bounds.getNorthEast().lat() , function(json) {
			if (json[0].producers.length > 0||json[0].regions.length > 0) {
				map.clearOverlays();
				$("#sidebar").html("");
				for (i=0; i<json[0].producers.length; i++) {
					//if (!drag){
						var location = json[0].producers[i];
						addLocation(location);
					//}
				}
				for (i=0; i<json[0].regions.length; i++) {
					//if (!drag){
						var location = json[0].regions[i];
						addRegion(location);
					//}
				}
			}
		});
	}

function showMessage(marker, id,html){
	var content=$(html);
	closestreetview();
	$(".selectedside").attr("class","");
	$("#side"+id).attr("class","selectedside");
	if (!isScrolledIntoView($("#side"+id))) $('#sidebar').scrollTo($("#side"+id));
	if (vcTimeout!=null) window.clearTimeout(vcTimeout);
	window.setTimeout('if (vcTimeout!=null) window.clearTimeout(vcTimeout);',900);
	map.openInfoWindow(marker.getLatLng() ,html);
	checksv(marker.getLatLng());
	$.get('wineryinfo.jsp','producer='+id, function(data) {
		window.setTimeout('if (vcTimeout!=null) window.clearTimeout(vcTimeout);',900);
		$('#markerinfo',content).replaceWith(data);
		//html=html.replace("<div id='markerinfo'>Loading info <img src='/images/spinner.gif' alt='loading'/></div>",data);
		//if (hassv) html=html.replace("display:none","");
		if (hassv) $("#svlink",content).show();
		if (contains(visits,id)) $("#addtolist",content).replaceWith("On visit list");
		map.openInfoWindow(marker.getLatLng() ,content.html());
	});
	
	
}
function isScrolledIntoView(elem)
{
    var docViewTop = $(window).scrollTop();
    var docViewBottom = docViewTop + $(window).height();
    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom)
      && (elemBottom <= docViewBottom) &&  (elemTop >= docViewTop) );
}


function addLocation(location) {
	var point = new GLatLng(location.p[0], location.p[1]);		
	var marker = new GMarker(point,reddoticon);
	<% //<span id='addtolist' class='jslink' style='font-family:arial;' onclick='addtovisitlist("+location.id+",\""+location.n+"\");'>Add to visit list</span>&nbsp;&nbsp;&nbsp;
	%>
	var html="<div id='vpballoon'><h3>"+location.n+"</h3><a href=\"/winery/"+(location.na)+"#wines\" target='_blank'>View wine information</a><div id='markerinfo'>Loading info <img src='/images/spinner.gif' alt='loading'/></div><br/><span class='jslink' id='svlink' style='font-family:arial;display:none'  onclick='streetview(new GLatLng("+location.p[0]+", "+location.p[1]+"));'>Streetview</span></div>"; 
	GEvent.addListener(marker, 'click', function() {
		showMessage(marker, location.id,html);
		//map.openInfoWindow(marker.getLatLng() ,html);
		//$.get('producer.info.jsp', function(data) {$('#markerinfo').html(data);});
      });
		map.addOverlay(marker);
		
		$("<a id='side"+location.id+"' href='#'/>")
		.html(location.n+"<br/>")
		.click(function(){
			showMessage(marker, location.id,html);
		})
		.appendTo("#sidebar");
}
function addRegion(location) {
	var point = new GLatLng(location.p[0], location.p[1]);		
		var marker=new LabeledMarker(point,{labelClass:"regionpoi",title:location.n,labelText:location.n,icon:invicon});
		var html="<div id='vpballoon'><h3>"+location.n+"</h3></div>"; 
		GEvent.addListener(marker, 'click', function() {
			showMessage(marker, location.id,html);
			//map.openInfoWindow(marker.getLatLng() ,html);
			//$.get('producer.info.jsp', function(data) {$('#markerinfo').html(data);});
	      });
		map.addOverlay(marker);
		$("<a id='side"+location.id+"' href='#'/>")
		.html(location.n+"<br/>")
		.click(function(){
			showMessage(marker, location.id,html);
		})
		.prependTo("#sidebar");
}
		/*]]>*/</script>
</head>
<body >
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<div class='centertext'>

	<!--Google Map-->
    
 	<h2>Wine map</h2>
 	Zoom and drag the map to the wine area you would like to visit. Or go straight to these famous wine regions:<br/>
	<span class='jslink' onclick='newview(48.78397854482421,3.3901523590087557,49.4133884143308,4.488785171508756);'>Champagne</span>, <span class='jslink' onclick='newview(46.66518009747812,4.341843032836881,47.32086148291153,5.440475845336881)'>Bourgogne</span>
 	<br/><br/>
<div><ul id='sidebar' class='sidebar'><li/></ul><div id="map" style="float:left;width: 800px; height: 700px;"></div></div>
<div id="message" style="display:none;position:absolute; padding:10px; background:#555; color:#fff; width:75px;z-index:1000;"></div>
<div id='panobox' style="display:none;position:relative"><div style="position:absolute;left:200px;top:0px;width:800px;height:20px;z-index:200;background-color:white"><span class='jslink' onclick='closestreetview();'>&nbsp;Click here to close Streetview</span></div><div id="pano" style="position:absolute;top:20px;left:200px;z-index:1;width: 800px; height: 680px"></div></div>
<%// <ul id="visitlist" style='clear:both'><li/></ul><form id='reportform' action='/visitingreport.jsp' method='post'><input id='producers' type='hidden' name='producers' value=''/><input type='submit' value='Report' onclick='setlist();return false;'/></form>%>
<script type='text/javascript'>loadmap();</script>
	
	
	<%@ include file="/snippets/footer.jsp" %>
</div>	

	
</body></html>
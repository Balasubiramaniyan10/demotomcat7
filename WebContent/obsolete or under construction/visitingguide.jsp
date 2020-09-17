<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
	Producers producers=new Producers(bounds);
	
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

<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

<script type="text/javascript">
/*<![CDATA[*/
var vpmapcenter;
var vpmapzoomlevel=0;


var infoWindow= new google.maps.InfoWindow();
var markers= [];
var m=0;

  
var reddoticon = new google.maps.MarkerImage('/images/reddot.gif',
 	      // This marker is 20 pixels wide by 32 pixels tall.
 	      new google.maps.Size(11,11),
 	      // The origin for this image is 0,0.
 	      new google.maps.Point(0,0),
 	      // The anchor for this image is the base of the flagpole at 0,32.
 	      new google.maps.Point(6,6));
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
	        
	  
	    	var sidebar=document.getElementById("sidebar");
	    	var myOptions = {
  mapTypeId: 'roadmap' 
};<%=md.mapid%> = new google.maps.Map(document.getElementById("map"),
	    myOptions);
	  		
	  		
	  		

	  		
			
		  	//map.addControl(new GMapTypeControl());
		   	//map.addControl(new GLargeMapControl());
		   	var reddoticon = new google.maps.MarkerImage('/images/reddot.gif',
		   	      // This marker is 20 pixels wide by 32 pixels tall.
		   	      new google.maps.Size(11,11),
		   	      // The origin for this image is 0,0.
		   	      new google.maps.Point(0,0),
		   	      // The anchor for this image is the base of the flagpole at 0,32.
		   	      new google.maps.Point(6,6));

	  		
	   <%if (producers.producer!=null){
	  	out.write("var marker;\n");
	  	out.write("var gmarkeroptions;\n"); 
	  	
		if (producers.producer!=null&&producers.producer.size()>0&&!md.onlyshowcenter){	
	  	Iterator<Producer> i=producers.producer.iterator();
	  		while (i.hasNext()){
	  			Producer poi=i.next();
	  			if (poi.getLat()!=null){
	  				if (poi.getLabelText()!=null&&!poi.getLabelText().equals("")){
	  					out.write("marker=new LabeledMarker(new GLatLng("+poi.getLat()+","+poi.getLon()+"),{labelClass:\"regionpoi\",title:\""+poi.getTitle()+"\",labelText:\""+poi.getLabelText()+(mapedit&&Dbutil.readIntValueFromDB("select (lasteditor='') as checked from kbregionhierarchy where id="+poi.getId()+";","checked")==1?"<font style='color:white;'>???</font>":"")+"\""+(mapedit?",draggable:true":",icon:invicon")+"});\n");
	  				} else {
	  					out.write("marker=createMarker(new google.maps.LatLng("+poi.getLat()+","+poi.getLon()+"), \""+poi.getTitle()+"\",\""+poi.getHTML()+"\", \""+poi.id+"\",reddoticon);");
	  					//out.write("var infowindow = new google.maps.InfoWindow({content:\""+poi.getHTML()+"\"});");
	  					//out.write("marker=new google.maps.Marker({position:new google.maps.LatLng("+poi.getLat()+","+poi.getLon()+"),title:\""+poi.getTitle()+"\",icon:reddoticon,map:map});\n");
	  					//out.write("google.maps.event.addListener(marker, 'click', function() {infowindow.open(map,marker);});");
	  					//out.write("marker.bindInfoWindowHtml(\""+poi.getHTML()+"<br/><a onclick='imStreetView.init(new GLatLng("+poi.getLat()+","+poi.getLon()+"));return false;'>Street view</a>"+"\",'clickable=true;');\n");
	  					//out.write("marker.bindInfoWindowHtml(\""+poi.getHTML()+"\",'clickable=true;');\n");
	  		  			
	  				}
	  				out.write("markers.push(marker);");
	  				out.write("sidebar.appendChild(createSidebarEntry(marker, \""+poi.getTitle().replaceAll("\"","&quot;")+"\", \""+poi.address+"\", 0));");
	  				
		  			if (mapbounds==null){  	
						//out.write("bounds.extend(new google.maps.LatLng("+poi.getLat()+","+poi.getLon()+"));");
					}		
	  			}
	  		}
		}
	  		 %>
	  		var bounds = new google.maps.LatLngBounds(new google.maps.LatLng(<%=bounds.latmin%>,<%=bounds.lonmin%>),new google.maps.LatLng(<%=bounds.latmax%>,<%=bounds.lonmax%>));
	  	   map.fitBounds(bounds);
	  	 map.addMapType(G_SATELLITE_3D_MAP);
	  	   
	  			<%
		}
	   

	  		if (mapbounds!=null){  	
				out.write("bounds.extend(new google.maps.LatLng("+mapbounds.latmin+","+mapbounds.lonmin+"));");
				out.write("bounds.extend(new google.maps.LatLng("+mapbounds.latmax+","+mapbounds.lonmax+"));");
			}
			
			
	      	
			%>
			  
			    
	    }	

	 
		

		/*]]>*/	

	</script>

</head>

<body  onload="javascript:doonload();">

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
 	
<%   		request.setAttribute("pois",producers.producer);
	request.setAttribute("bounds",bounds);
	
	if (producers.numberofproducers>producers.maxproducers) out.write("<h4>"+producers.numberofproducers+" wineries found in this area, please zoom in to display individual wineries on the map</h4>");
%>
<div id='sidebar' class='sidebar'>

</div><div id="map" style="float:left;width: 800px; height: 700px;">Map</div>


<script type='text/javascript'>loadmap();</script>
	
	<%@ include file="/snippets/footer.jsp" %>
</div>	
	<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
	</body></html>
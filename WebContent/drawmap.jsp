<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.LinkedHashSet"%>
<%@page import="com.freewinesearcher.online.Regioninfo"%>
<%@page import="com.freewinesearcher.online.web20.CommunityUpdater"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN" xml:lang="EN"><%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/>
<% long start=System.currentTimeMillis();
	boolean debuglog=false;
	boolean edit=false;
	
	if (request.isUserInRole("admin")) edit=true;
	if (request.isUserInRole("editor")) edit=true;
	PageHandler p=PageHandler.getInstance(request,response,"Regioninfo");
	if (p.ipaddress.equals("85.147.228.61")||p.ipaddress.equals("127.0.0.1")) debuglog=true;

	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"create regioninfo object for "+(String)request.getAttribute("originalURL"));
	Regioninfo regioninfo=new Regioninfo(Integer.parseInt(request.getParameter("id"))); 
	regioninfo.debug=debuglog;
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"get regioninfo text"); 
	String text=regioninfo.getInfo(edit);
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start process page"); 
	boolean showproducers=true;
	if (session.getAttribute("showproducers")!=null) try{showproducers=Boolean.parseBoolean((String)session.getAttribute("showproducers"));}catch(Exception e){}
	boolean showsubregions=true;
	if (session.getAttribute("showsubregions")!=null) try{showsubregions=Boolean.parseBoolean((String)session.getAttribute("showsubregions"));}catch(Exception e){}
%>
<%@page import="com.freewinesearcher.online.Regioninfo"%>
<head>
<title>
Wine regions, appellations and wine maps<%=(regioninfo.currentlocale.length()>0?" of "+regioninfo.currentlocale:"")%>. See which wines <%=(regioninfo.currentlocale.length()>0?"from  "+regioninfo.regionname+" ":"")%>are the best and where to buy them on line!
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<meta name="description" content="<%=(regioninfo.currentlocale.length()>0?"Information about wine from "+regioninfo.currentlocale.replaceAll("/",", ")+": wine regions, appellations, maps, recommended wines and producers like "+regioninfo.stats.getTopProducerText():"Wine regions and appellation around the world, their producers and their wines")%>" />
<meta name="keywords" content="<%=(regioninfo.currentlocale.length()>0?regioninfo.currentlocale.replaceAll("/",", ")+", wine region, appellation, wine map of "+regioninfo.regionname:"Wine regions, appellations, wine maps")%>" />
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<script type="text/javascript">
    //<![CDATA[

    var map;
    var polyShape;
    var polygonMode;
    var polygonDepth = "20";
    var polyPoints = [];
    var marker;
    var geocoder = null;

    var fillColor = "#0000FF"; // blue fill
    var lineColor = "#000000"; // black line
    var opacity = .5;
    var lineWeight = 2;

    var kmlFillColor = "7dff0000"; // half-opaque blue fill

    function load() {
      if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("map"));
        map.setCenter(new GLatLng(37.4224, -122.0838), 13);
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
	GEvent.addListener(map, 'click', mapClick);
        geocoder = new GClientGeocoder();
      }
    }


   // mapClick - Handles the event of a user clicking anywhere on the map
   // Adds a new point to the map and draws either a new line from the last point
   // or a new polygon section depending on the drawing mode.
    function mapClick(marker, clickedPoint) {
      polygonMode = document.getElementById("drawMode_polygon").checked;

      // Push onto polypoints of existing polygon
      polyPoints.push(clickedPoint);
      drawCoordinates();
     }


      // Clear current Map
      function clearMap(){
        map.clearOverlays();
        polyPoints = [];
	document.getElementById("coords").value =  "&lt;-- Click on the map to digitize!";
      }


      // Toggle from Polygon PolyLine mode

      function toggleDrawMode(){
        map.clearOverlays();
        polyShape = null;
        polygonMode = document.getElementById("drawMode_polygon").checked;
        drawCoordinates();
      }


    // Delete last Point
    // This function removes the last point from the Polyline/Polygon and redraws
    // map.

    function deleteLastPoint(){
      map.removeOverlay(polyShape);

      // pop last element of polypoint array
      polyPoints.pop();
      drawCoordinates();
     }


    // drawCoordinates
    function drawCoordinates(){
      //Re-create Polyline/Polygon
      if (polygonMode) {
        polyShape = new GPolygon(polyPoints,lineColor,lineWeight,opacity,fillColor,opacity);
      } else {
        polyShape = new GPolyline(polyPoints,lineColor,lineWeight,opacity);
      }
      map.clearOverlays();

      // Grab last point of polyPoints to add marker
      marker = new GMarker(polyPoints[polyPoints.length -1]);
      map.addOverlay(marker);
      map.addOverlay(polyShape);
      logCoordinates();
    }


    // logCoordinates - prints out coordinates of global polyPoints array
    //  This version only logs KML, but could be extended to log different types of output

    function logCoordinates(){

      var header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
       "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n" +
       "<Document><name>Your name of document</name><description>Your description</description>\n" +
       "<Placemark><Style>\n<LineStyle><width>" + lineWeight + "</width></LineStyle>\n<PolyStyle><color>" + 
        kmlFillColor +"</color></PolyStyle>\n</Style>\n";

       // check mode
       if (polygonMode){
         header += "<Polygon><extrude>1</extrude>\n<altitudeMode>relativeToGround</altitudeMode>" +
		       "<outerBoundaryIs>\n<LinearRing>\n<coordinates>\n";

         var footer = "</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>\n</Document>\n</kml>";

         // print coords header
         document.getElementById("coords").value =  header;

         // loop to print coords 
         for (var i = 0; i<(polyPoints.length); i++) {
           var lat = polyPoints[i].lat();
           var longi = polyPoints[i].lng();
	   document.getElementById("coords").value += longi + ", " + lat + ", "+ polygonDepth + "\n";
	 }
      } else {
        header += "<LineString><tessellate>1</tessellate>\n<coordinates>\n";
        var footer = "</coordinates></LineString></Placemark>\n</Document>\n</kml>";
        // print coords header
        document.getElementById("coords").value =  header;
        // loop to print coords 
        for (var i = 0; i<(polyPoints.length); i++) {
          var lat = polyPoints[i].lat();
          var longi = polyPoints[i].lng();
          document.getElementById("coords").value += longi + ", " + lat + ",0\n";
        }
     }
     document.getElementById("coords").value +=  footer;
   }


   function showAddress(address) {
     if (geocoder) {
       geocoder.getLatLng(address,
         function(point) {
           if (!point) {
             alert(address + " not found");
           } else {
	     clearMap();
             map.setCenter(point, 13);
           }
         }
       );
     }
   }


    //]]>
    </script>

<script type="text/javascript">
var vpmapcenter;
var vpmapzoomlevel=0;

<%if(edit){ %>
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
<%}%>
function switchpoi(el){
	jQuery.post('/jsinteraction.jsp?action=setAttribute&name='+el.className+'&value='+el.checked);
	if (el.className=='showproducers'){
		if (el.checked) {
			for(var i=0; i<mapdetailmarkers.length; i++){mapdetailmarkers[i].show();}
		} else {
			for(var i=0; i<mapdetailmarkers.length; i++){mapdetailmarkers[i].hide();}
		}
	}
	if (el.className=='showsubregions'){
		if (el.checked) {
			javascript:$(".regionpoi").show();
		} else {
			javascript:$(".regionpoi").hide();
		}
	}
}

function poiinit(){
	$("#switchpoi").html("<form><input type='checkbox' class='showproducers' id='showproducers' <%=(showproducers?"checked='checked'":"")%> onclick='switchpoi(this);' />Show producers<br/><input type='checkbox' class='showsubregions' id='showsubregions' <%=(showsubregions?"checked='checked'":"")%> onclick='switchpoi(this);' />Show subregions</form>");
	switchpoi(document.getElementById("showproducers"));
	switchpoi(document.getElementById("showsubregions"));
}

function setTypegotoGuide(type){
	setType(type);
	$('#regiontabs').tabs().click(3);
	$('#regiontabs').tabs().click(4);
}
function setGrapegotoGuide(grape){
	setGrape(grape);
	$('#regiontabs').tabs().click(3);
	$('#regiontabs').tabs().click(4);
}
</script>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<script type="text/javascript">

$(document).ready(function(){
	setTimeout(function(){ initSmartSuggest(); }, 1500);
	$("#regiontabs").tabs("#regionpane > div");
	<%=(regioninfo.showmap?"poiinit();":"")%>
	initAjax();
	
});

function get_data_1()
{	var grapedata='<%=regioninfo.stats.grapejson%>';
	return (grapedata);
}
function get_data_2()
{
	var typedata='<%=regioninfo.stats.typejson%>';
	return (typedata);
}
swfobject.embedSWF(
		  "/open-flash-chart.swf", "grapestats",
		  "350", "200", "9.0.0", "expressInstall.swf",
		  {"get-data":"get_data_1"} );

		swfobject.embedSWF(
		  "/open-flash-chart.swf", "typestats",
		  "350", "200", "9.0.0", "expressInstall.swf",
		  {"get-data":"get_data_2"} );

</script>
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKey%>" type="text/javascript"></script>
<script type="text/javascript" src="/js/googlemaps/labeled_marker.js"></script>

</head>
<body >
 
	
<%@ include file="/snippets/topbar.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %></div>
		<div class='main'><br/>

<%out.write(text); %>
		<div id='guidedsearch'>
<!--<div class='dialog crit'><div class='content'><div class='t'></div>-->
		<form id="GuidedSearchform"  action="#" onsubmit='newSearch();return false;' autocomplete="off">
		<noscript>
		<h4><font color='red'>Javascript in currently disabled in your browser. In order to use the Buying Guide you need to enable Javascript... </font></h4>
		</noscript>
<div class='criterionh'>By price range:</div>
<img id='priceclose' src='/images/transparent.gif' class='close sprite2 sprite2-close' onclick='javascript:clearBudget();' alt='Clear price range'/><img class='spinner' id='pricespinner' alt='Loading...' src='/images/spinner.gif'/><div class='slidercontainer'><div class='slider' id='priceslider'><select name="pricemin" id="pricemin">
<% 	int max=200;
	String symbol=Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.getCurrency()+"';", "symbol");
	for (int i=0;i<max;i=i+5)out.write("<option value=\""+i+"\""+(i==0?" selected='selected'":"")+">"+symbol+" "+i+"</option>"); %>
				<option value="0"><%=symbol+max %>+</option>
</select>

			<select name="pricemax" id="pricemax">
<%	for (int i=0;i<max;i=i+5)out.write("<option value=\""+i+"\""+(i==200?" selected='selected'":"")+">"+symbol+" "+i+"</option>"); %>
				<option value="<%=max %>"<%=(200==max?" selected='selected'":"") %>><%=symbol+max %>+</option>
			</select>

</div></div><div class='sliderlegend'>
<div class="slider-min"><%=Webroutines.getCurrencySymbol(p.searchdata.getCurrency())%> <span id='slider-min'>0</span></div>
<div class="slider-max"><%=Webroutines.getCurrencySymbol(p.searchdata.getCurrency())%> <span id='slider-max'>200</span>+</div>
</div>
<input type='hidden' id="ratingmin" name="ratingmin" value="<%=80%>"/>
<input type='hidden' id="ratingmax" name="ratingmax" value="100"/>
<div id='criteria'>
<input type='hidden' id="region" name="region" value="<%= regioninfo.regionname %>"/>
<input type='hidden' id='symbol' value='&euro;'/>
<input type='hidden' id='numpages' value='0'/>
<div class='slider' id='rating'></div><div class='sliderlegend'><div class='slider-min'>80</div><div class='slider-max'>100</div></div>
<input type='hidden' id='ratingminscale' name='ratingminscale' value='80'/>
<input type='hidden' id='ratingmaxscale' name='ratingmaxscale' value='100'/>
<div class='slider' id='vintageslider'><select name='vintageminsl' id='vintageminsl'><option selected='selected' value='0'>N.V.</option></select></div>
<input type='hidden' id='vintageminscale' name='vintageminscale' value='0'/>
<input type='hidden' id='vintagemaxscale' name='vintagemaxscale' value='2009'/>
<input type='hidden' id='vintagemin' name='vintagemin' value='0'/>
<input type='hidden' id='vintagemax' name='vintagemax' value='0'/>
<select name='countryofseller' id='countryofseller' onchange='javascript:spin(&quot;country&quot;);newSearch();' ><option selected='selected' value='All'>All countries</option></select>
</div>
<input type='hidden' name='winetype' id='winetype' value='alltypes'/>
<input type='hidden' name="subregions" value="true"/>
<input type="hidden" name="dosearch" value="true" />
<input type="hidden" name="pqratio" value="true" />		
<input type="hidden" name="pages" value="5" />	
<input type="hidden" name="resultsperpage" value="5" />
<div class='criterionh'><%=p.t.get("displaycurrency")%></div>
<input type="radio" name="currency" id="EUR" value="EUR" onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio"  onchange="javascript:newSearch();" name="currency" value="GBP" <%if (p.searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF"  onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD"  onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$
<input type="hidden" id="page" name="page" value="0"/>		
</form>

<!--</div><div class='b'><div></div></div></div>-->
</div><!--  guidedsearch-->
<div id="regionwineguide"><div id="result">
<h1>&nbsp;</h1><ul class='tabs'  id='gstabs'><li><a href='#'>Best value</a></li><li><a href='#storewineinfo'>By Rating</a></li><li><a href='#'>By Price</a></li></ul>
<div class='panes' id='gspanes'>
<div class='pan' id='pane1'><div class='items' id='items1'><div class='page'></div></div><!--items--><div id='next1' class='nav nextPage'><a>Next</a></div><div id='prev1'  class='prevPage'><a>Previous</a></div></div>
<div class='pan' id='pane2'><div class='items' id='items2'><div class='page'></div></div><!--items--><div id='next2' class='nav nextPage'><a>Next</a></div><div id='prev2'  class='prevPage'><a>Previous</a></div></div>
<div class='pan' id='pane3'><div class='items' id='items3'><div class='page'></div></div><!--items--><div id='next3' class='nav nextPage'><a>Next</a></div><div id='prev3'  class='prevPage'><a>Previous</a></div></div>
</div>
<div class='clearboth'></div>
</div><!-- result -->

<div class='pricenote'><%=p.t.get("pricenote")%></div></div></div></div>
<% 
if (regioninfo.showmap||edit){
	request.setAttribute("edit",edit);
	Set<POI> pois=new LinkedHashSet<POI>();
	pois.addAll(regioninfo.subregions);
	pois.addAll(regioninfo.producers);
MapDataset mapdataset=new MapDataset();
mapdataset.extrazoomlevel=1;
mapdataset.pois=pois;
mapdataset.bounds=regioninfo.bounds;
mapdataset.mapid="mapdetail";
request.setAttribute("mapdataset",mapdataset);
%><%@ include file="/snippets/map.jsp" %> 

<%
mapdataset.extrazoomlevel=-3;
mapdataset.onlyshowcenter=true;
mapdataset.mapid="mapregion";
request.setAttribute("mapdataset",mapdataset);
%><%@ include file="/snippets/map.jsp" %><% 

mapdataset.extrazoomlevel=-6;
mapdataset.mapid="mapworld";
request.setAttribute("mapdataset",mapdataset);
%><%@ include file="/snippets/map.jsp" %><% 
}


%>


<script>

mapdetail.addControl(new GMapTypeControl());
mapdetail.addControl(new GLargeMapControl());
//jQuery('#mapdetail').streetview({map:mapdetail, icon_image: "/images/street-view.png"});
</script>

<%@ include file="/snippets/textpagefooter.jsp" %>
<% 
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Finished process page"); 

} %>
<%if (edit){
	


	out.write("<script type='text/javascript' src='/js/tiny_mce/tiny_mce.js'></script>");
	cu.setAl(new Auditlogger(request));
	cu.setId(regioninfo.regionid);
	cu.setTablename("kbregionhierarchy");
	cu.setIdcolumn("id");
	cu.setContentcolumn("description");
	cu.setElementid("regiondescription");
	out.write(cu.getHtml(request));
	
	
} %>
<input type="button" onclick="deleteLastPoint();" value="Delete Last Point"/>
<input type="button" onclick="clearMap();" value="Clear Map"/>

</div>
</body> 
</html>
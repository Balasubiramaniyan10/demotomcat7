<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@page import="com.freewinesearcher.common.Knownwine"%><%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Translator"
	import="java.util.LinkedHashSet"
	import = "com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.online.Regioninfo"
	import="com.freewinesearcher.common.Configuration"
%><%
long start=System.currentTimeMillis();
boolean debuglog=false;
boolean edit=false;
PageHandler p=PageHandler.getInstance(request,response,"Regioninfo");
if (p.ipaddress.equals("85.147.228.61")||p.ipaddress.equals("127.0.0.1")) debuglog=true;
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"create regioninfo object for "+(String)request.getAttribute("originalURL"));
Regioninfo regioninfo=new Regioninfo((String)request.getAttribute("originalURL"),debuglog,p); 
regioninfo.debug=debuglog; 
regioninfo.newmobile=true;
regioninfo.mobile=true;
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"get regioninfo text"); 
String text=regioninfo.getInfo(edit);
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start process page"); 
boolean showproducers=true;
if (session.getAttribute("showproducers")!=null) try{showproducers=Boolean.parseBoolean((String)session.getAttribute("showproducers"));}catch(Exception e){}
boolean showsubregions=true;
if (session.getAttribute("showsubregions")!=null) try{showsubregions=Boolean.parseBoolean((String)session.getAttribute("showsubregions"));}catch(Exception e){}
%><!DOCTYPE html> 
<html> 
	<head> 
	<%@ include file="/mobile/includes.jsp" %> 
<script type="text/javascript">$(document).ready(function(){showregionlocation();});</script>
	<script type="text/javascript">
var vpmapcenter;
var vpmapzoomlevel=0;
function showregionlocation(){
	if ($('#mapdetail').html()==''){
		$('#mappane').css('display','block');
	loadmapdetail();
	mapdetail.addControl(new GMapTypeControl());
	mapdetail.addControl(new GLargeMapControl());
	}
}
</script>
<%
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
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKey%>" type="text/javascript"></script>
<script type="text/javascript" src="/js/googlemaps/labeled_marker.js"></script>
<title>
Wine regions, appellations and wine maps<%=(regioninfo.currentlocale.length()>0?" of "+regioninfo.currentlocale:"")%>. See which wines <%=(regioninfo.currentlocale.length()>0?"from  "+regioninfo.regionname+" ":"")%>are the best and where to buy them on line!
</title>
<%
	NumberFormat format  = new DecimalFormat("#,##0.00");
	response.setHeader("Cache-Control","max-age=3600");
	response.setDateHeader ("Expires", 0);
%>

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

</head> 
<body>
<div data-role="page" data-theme="a" class="vp" id="region">
<%@ include file="/mobile/header.jsp" %>


<div data-role="content">
<%
	out.write(text); 
%></div>
<%@ include file="/mobile/footer.jsp" %>
</div><!-- /page -->
</body>
</html>

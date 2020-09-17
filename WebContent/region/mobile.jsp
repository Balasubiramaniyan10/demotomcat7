<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd"><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
<head><meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta name="viewport" content="width=device-width; initial-scale=1.0; " />

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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% //content type text/html ivm google maps die anders niet werkt %>
<%@page import="java.util.LinkedHashSet"
session="true"  
import="com.freewinesearcher.online.Regioninfo"
import="com.freewinesearcher.online.web20.CommunityUpdater"
import="com.freewinesearcher.online.Auditlogger"
import="com.freewinesearcher.common.Configuration"
import = "com.freewinesearcher.online.Webroutines"	
%>
<% ArrayList<String> countries = Webroutines.getCountries();
long start=System.currentTimeMillis();
	boolean debuglog=false;
	boolean edit=false;
	
	if (request.isUserInRole("admin")) edit=true;
	if (request.isUserInRole("editor")) edit=true;
	PageHandler p=PageHandler.getInstance(request,response,"Regioninfo");
	if (p.ipaddress.equals("85.147.228.61")||p.ipaddress.equals("127.0.0.1")) debuglog=true;

	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"create regioninfo object for "+(String)request.getAttribute("originalURL"));
	Regioninfo regioninfo=new Regioninfo((String)request.getAttribute("originalURL"),debuglog,p); 
	regioninfo.debug=debuglog; 
	regioninfo.mobile=true;
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"get regioninfo text"); 
	String text=regioninfo.getInfo(edit);
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start process page"); 
	boolean showproducers=true;
	if (session.getAttribute("showproducers")!=null) try{showproducers=Boolean.parseBoolean((String)session.getAttribute("showproducers"));}catch(Exception e){}
	boolean showsubregions=true;
	if (session.getAttribute("showsubregions")!=null) try{showsubregions=Boolean.parseBoolean((String)session.getAttribute("showsubregions"));}catch(Exception e){}
%>
<%if (PageHandler.getInstance(request,response).abuse){
	%>
	<%@page import="com.freewinesearcher.common.Dbutil"%><jsp:forward page="abuse.jsp" /><%
			return;
		}%><%@page import="com.freewinesearcher.online.Regioninfo"%>
<title>
Wine regions, appellations and wine maps<%=(regioninfo.currentlocale.length()>0?" of "+regioninfo.currentlocale:"")%>. See which wines <%=(regioninfo.currentlocale.length()>0?"from  "+regioninfo.regionname+" ":"")%>are the best and where to buy them on line!
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<meta name="description" content="<%=(regioninfo.currentlocale.length()>0?"Information about wine from "+regioninfo.currentlocale.replaceAll("/",", ")+": wine regions, appellations, maps, recommended wines and producers like "+regioninfo.stats.getTopProducerText():"Wine regions and appellation around the world, their producers and their wines")%>" />
<meta name="keywords" content="<%=(regioninfo.currentlocale.length()>0?regioninfo.currentlocale.replaceAll("/",", ")+", wine region, appellation, wine map of "+regioninfo.regionname:"Wine regions, appellations, wine maps")%>" />
<%@ include file="/snippets/jsincludes.jsp" %>
<script type="text/javascript">$(document).ready(function(){initSmartSuggest();showregionlocation();});</script>
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
<%@ include file="/headersmall.jsp" %>
Mobile version | <a href='<%=p.thispage.replaceAll("/mregion/","/region/")%>'>Normal version</a><br/>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>

<%out.write(text); %>
<h2>Find a wine on Vinopedia</h2>
<%@ include file="/snippets/mobilesearchform.jsp" %>
<%@ include file="/snippets/footersmall.jsp" %>
<% 
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Finished process page"); 

} %>
</div>
</div>
</body> 
</html>
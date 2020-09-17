<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/>
<%@page import="java.util.LinkedHashSet"%>
<%@page import="com.freewinesearcher.online.web20.CommunityUpdater"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">

<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%><% PageHandler p=PageHandler.getInstance(request,response,"Winery");
String mobileloc=p.thispage.replaceAll("/winery/","/mwinery/");
if (!p.bot&&p.firstrequest&&p.mobile){
	if (mobileloc!=null&&!mobileloc.equals("")){
	//Dbutil.logger.info("Redirecting to mobile page: "+request.getHeader("user-agent"));
	response.setStatus(302);
	response.setHeader( "Location", mobileloc);
	response.setHeader( "Connection", "close" );
	return;
}
} 
	Producer producer=new Producer(request.getParameter("winery")); 
	cu.setAl(new Auditlogger(request));
	cu.setId(producer.id);
	cu.setTablename("kbproducers");
	cu.setIdcolumn("id");
	cu.setAccesscode(request.getParameter("accesscode"));
	boolean edit=false;
	if (request.isUserInRole("admin")||cu.validAccessCode()) {
		edit=true;
		producer.showallinfo=true;
		//producer.edithashcode=request.getParameter("accesscode");
	}
	String text=producer.getInfo(PageHandler.getInstance(request,response));  


%>
<%@page import="com.freewinesearcher.online.Producer"%>
<%@page import="com.freewinesearcher.online.Producerinfo"%>
<head>
<title>
<%=(producer.name.replaceAll("&","&amp;"))%> and their wines
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<meta name="description" content="<%=producer.generateddescription.replaceAll("&","&amp;")%>" />
<meta name="keywords" content="<%=producer.keywords.replaceAll("&","&amp;") %>" />

<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKey%>" type="text/javascript"></script>
<script  type="text/javascript">
/*<![CDATA[*/
var vpmapcenter;
var vpmapzoomlevel=0;
function showmap(){	if(document.getElementById("storelocator")!=null&&document.getElementById("storelocator").innerHTML==''){		var vpiframe = document.createElement( "iframe" );		if (document.getElementById("storelocator").style.width=='') document.getElementById("storelocator").style.width="950px";		if (document.getElementById("storelocator").style.height=='') document.getElementById("storelocator").style.height="500px";		vpiframe.setAttribute("frameborder","0");		vpiframe.setAttribute("scrolling","no");		vpiframe.setAttribute("width",document.getElementById("storelocator").style.width);		vpiframe.setAttribute("height",document.getElementById("storelocator").style.height);		vpiframe.setAttribute("overflow","hidden");		vpiframe.setAttribute( "src", ""+"/storelocator.jsp?id="+<%=producer.id%>+"&width="+document.getElementById("storelocator").style.width+"&height="+document.getElementById("storelocator").style.height+"&showprices=true");		document.getElementById("storelocator").appendChild(vpiframe);	}}
function processwinerytabs(){
	$("#winerytabs").tabs("#winerypane > div");
	if($("#winerytabs").data("tabs")&&$("#winerytabs").data("tabs").getCurrentTab().attr("href")=='#storelocator') showmap();
	if($("#winerytabs").data("tabs")&&$("#winerytabs").data("tabs").getCurrentTab().attr("href")=='#mappane') showwinerylocation();
}
/*]]>*/</script>

</head> 
<body  onload="javascript:processwinerytabs();doonload();">
<%@ include file="/snippets/topbar.jsp" %>
<%@ include file="/snippets/logoandsearch.jsp" %>
<div class='main'>
<noscript><img src='/images/nojs.gif' alt=''/></noscript>
<br/>
<%
if (request.isUserInRole("admin")) { 
	%><a href='/winery/<%=(Webroutines.URLEncodeUTF8Normalized(producer.name).replaceAll("%2F", "/").replace("&", "&amp;")+"&amp;accesscode="+producer.edithashcode)%>'>URL for editing winery</a><br/><%
	}
	out.write(text); 

if (producer.hasvalidlocation){ 
MapDataset mapdataset=new MapDataset();
Set<POI> pois=new LinkedHashSet<POI>();
pois.add(producer);
mapdataset.pois=pois;
mapdataset.mapid="mapdetail";
request.setAttribute("mapdataset",mapdataset);

%><%@ include file="/snippets/map.jsp" %><%
mapdataset.extrazoomlevel=-4;
mapdataset.onlyshowcenter=true;
mapdataset.mapid="mapregion";
request.setAttribute("mapdataset",mapdataset);
%><%@ include file="/snippets/map.jsp" %><% 

mapdataset.extrazoomlevel=-8;
mapdataset.mapid="mapworld";
request.setAttribute("mapdataset",mapdataset);
%><%@ include file="/snippets/map.jsp" %>
<%} 


if (edit){
	


	out.write("<div class='clear'></div>");
	out.write("<script type='text/javascript' src='/js/tiny_mce/tiny_mce.js'></script>");
	cu.setContentcolumn("address");
	cu.setElementid("address");
	out.write(cu.getHtml(request));
	cu.setContentcolumn("telephone");
	cu.setElementid("telephone");
	out.write(cu.getHtml(request));
	cu.setContentcolumn("email"); 
	cu.setElementid("email");
	out.write(cu.getHtml(request));
	cu.setContentcolumn("description");
	cu.setElementid("description");
	out.write(cu.getHtml(request));
	cu.setContentcolumn("website");
	cu.setElementid("website");
	out.write(cu.getHtml(request));
	cu.setContentcolumn("visiting");
	cu.setElementid("visiting");
	out.write(cu.getHtml(request));
	cu.setContentcolumn("twitter");
	cu.setElementid("twitter");
	out.write(cu.getHtml(request));
	
	
}%>
<script type="text/javascript" >
function showwinerylocation(){
	if ($('#mapdetail').html()==''){
		$('#mappane').css('display','block');
	loadmapdetail();
	mapdetail.addControl(new GMapTypeControl());
	mapdetail.addControl(new GLargeMapControl());
	loadmapregion();
	loadmapworld();
	
	}
}
</script>
<%@ include file="/snippets/footer.jsp" %>
<% } %>
	
</div>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
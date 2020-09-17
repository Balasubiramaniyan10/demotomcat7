<!DOCTYPE HTML>
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
	//if (p.ipaddress.equals("85.147.228.61")||p.ipaddress.equals("127.0.0.1")) debuglog=true;

	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"create regioninfo object for "+(String)request.getAttribute("originalURL"));
	Regioninfo regioninfo=new Regioninfo((String)request.getAttribute("originalURL"),debuglog,p); 
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
<title><%=regioninfo.regionname%> wines</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%if (regioninfo.parent==100){ %>
<meta name="description" content="The wines from <%=(regioninfo.currentlocale.length()>0?regioninfo.regionname+": Map of the wine regions, a list of the best producers and an interactive wine guide. ":"Wine regions and appellation around the world, their producers and their wines")%>" />
<%}else { %>
<meta name="description" content="<%=(regioninfo.currentlocale.length()>0?regioninfo.regionname+" wines: Compare "+regioninfo.stats.offers+" offers of "+regioninfo.regionname+" wines from "+regioninfo.stats.producers+" producers. Wine map of the region and a list of the best producers. ":"Wine regions and appellation around the world, their producers and their wines")%>" />
<%} %>
<meta name="keywords" content="<%=(regioninfo.currentlocale.length()>0?regioninfo.regionname+", "+regioninfo.regionname+" wines, best "+regioninfo.regionname+" wines, wine map of "+regioninfo.regionname:"Wine regions, appellations, wine maps")%>" />
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<script type="text/javascript">
var showsubregions=<%=(showsubregions?"true":"false")%>;
var showproducers=<%=(showproducers?"true":"false")%>;

function get_data_1()
{	var grapedata='<%=regioninfo.stats.grapejson%>';
	return (grapedata);
}
function get_data_2()
{
	var typedata='<%=regioninfo.stats.typejson%>';
	return (typedata);
}
</script>
<%if(edit){ %>
<script src="/js/regioninfoeditor.js" type="text/javascript" defer="defer"></script>
<%}%>
<script src="/js/regioninfo.js" type="text/javascript" defer="defer"></script>
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKey%>" type="text/javascript"></script>
<script type="text/javascript" src="/js/googlemaps/labeled_marker.js"></script>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>

</head>
<body onload="<%=(regioninfo.showmap?"poiinit();":"")%>">
 
<% request.setAttribute("wineguidelink", "/wine-guide/region/"+Webroutines.removeAccents(regioninfo.regionname).replaceAll(" ", "+")); %>	
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
		<h4><font color='red'>Javascript is currently disabled in your browser. In order to use the Buying Guide you need to enable Javascript... </font></h4>
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
<div class='criterionh'><%=p.getTranslator().get("displaycurrency")%></div>
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

<div class='pricenote'><%=p.getTranslator().get("pricenote")%></div></div></div>
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
</div>
</body> 
</html>
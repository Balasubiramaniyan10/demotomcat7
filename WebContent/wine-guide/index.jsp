<%	request.setCharacterEncoding("UTF-8");
	session = request.getSession(true);
	PageHandler p=PageHandler.getInstance(request,response,"Wine guide");
	
	%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%@ page   
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.common.Region"
	
%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%String url=Webroutines.URLDecodeUTF8((String)request.getAttribute("originalURL"));
String country = Webroutines.filterUserInput(request.getParameter("country"));
if (country==null||country.equals("")) country="All";
String grape = Webroutines.getRegexPatternValue("/grape/([^/]+)",url);
if (grape==null) grape="";
String ratingmin = Webroutines.getRegexPatternValue("/ratingmin/([^/]+)",(String)request.getAttribute("originalURL"));
if (ratingmin==null||ratingmin.equals("")) ratingmin="0";
String winetype = Webroutines.filterUserInput(Webroutines.getRegexPatternValue("/winetype/([^/]+)",(String)request.getAttribute("originalURL")));
if (winetype==null) winetype="alltypes";
winetype=winetype.toUpperCase();
String scale = Webroutines.filterUserInput(request.getParameter("scale"));
if (scale==null||scale.equals("")) scale="100";
boolean subregions=true;
if (Webroutines.filterUserInput(request.getParameter("subregions"))!=null&&Webroutines.filterUserInput(request.getParameter("subregions")).equals("N")) subregions=false;
String pricemax = Webroutines.filterUserInput(request.getParameter("pricemax"));
if (pricemax==null) pricemax="";
int pricemaxint=200;
try{pricemaxint=Integer.parseInt(pricemax);}catch(Exception e){}
String pricemin = Webroutines.filterUserInput(request.getParameter("pricemin"));
if (pricemin==null) pricemin="";
int priceminint=0;
try{priceminint=Integer.parseInt(request.getParameter("pricemin"));}catch(Exception e){}
String vintage = Webroutines.filterUserInput(request.getParameter("vintage"));
if (vintage==null) vintage="";
String searchtype = Webroutines.filterUserInput(request.getParameter("searchtype"));
boolean pqratio=true;
if (searchtype!=null&&searchtype.equals("rating")) pqratio=false;
vintage=Webroutines.filterVintage(vintage);
vintage=Webroutines.filterUserInput(vintage);
boolean dosearch=Boolean.parseBoolean(Webroutines.filterUserInput(request.getParameter("dosearch")));
String region = Webroutines.getRegexPatternValue("/region/([^/]+)",url);
if (region==null) region="";


String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { 
		offset="0";

			}else {
 					}
 				%><%@page import="com.google.gdata.data.DateTime"%>
<%@page import="com.freewinesearcher.online.WineAdvice"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(p.searchdata.getLanguage().toString().toLowerCase())?"EN":p.searchdata.getLanguage().toString().toLowerCase())%>" xml:lang="<%=("".equals(p.searchdata.getLanguage().toString().toLowerCase())?"EN":p.searchdata.getLanguage().toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%=(grape+region).equals("")?"Wine Buying Guide":"Buying Guide for "+(winetype!=null&&!winetype.equals("")?winetype.toLowerCase()+" ":"")+(grape.equals("")?"":grape+" ")+"wine"+(region.equals("")?"":" from "+region)%></title>
<meta name="keywords" content="<%=(grape.equals("")?"":grape+", ")%><%=(region.equals("")?"":region+", ") %>wine, guide, recommendations" />
<meta name="description" content="A wine guide for the best <%=(grape.equals("")?"":grape+" ")%>wines<%=(region.equals("")?" around the world":" from "+region)%> based on current market prices" />
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<% request.setAttribute("ad","true"); %>
<% request.setAttribute("numberofimages","0"); %>
</head>
<body  onload="javascript:init();">
<%@ include file="/snippets/textpage.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%
	ArrayList<String> countries = Webroutines.getCountries();
	Translator t=new Translator();
    t.setLanguage(Translator.languages.EN);
	Ad rightad = new Ad("newdesign",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid,"");
	//Ad bottomleftad = new Ad("newdesign",187, 300, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner + "");			
	Ad betweenresults = new Ad("newdesign",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner+"");
%>
		<div id='guidedsearch'>
<!--<div class='dialog crit'><div class='content'><div class='t'></div>-->
		<form id="GuidedSearchform"  action="#" onsubmit='newSearch();return false;' autocomplete="off">
		<h4>Refine criteria</h4> 
		<noscript>
		<h4><font color='red'>Javascript in currently disabled in your browser. In order to use the Buying Guide you need to enable Javascript... </font></h4>
		</noscript>
<div class='criterionh'>By price range:</div>
<img id='priceclose' src='/images/transparent.gif' class='close sprite2 sprite2-close' onclick='javascript:clearBudget();' alt='Clear price range'/><img class='spinner' id='pricespinner' alt='Loading...' src='/images/spinner.gif'/><div class='slidercontainer'><div class='slider' id='priceslider'><select name="pricemin" id="pricemin">
<% 	int max=200;
	String symbol=Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.getCurrency()+"';", "symbol");
	for (int i=0;i<10;i=i+1)out.write("<option value=\""+i+"\""+(i==priceminint?" selected='selected'":"")+">"+symbol+" "+i+"</option>");
	for (int i=10;i<20;i=i+2)out.write("<option value=\""+i+"\""+(i==priceminint?" selected='selected'":"")+">"+symbol+" "+i+"</option>");
	for (int i=20;i<max;i=i+5)out.write("<option value=\""+i+"\""+(i==priceminint?" selected='selected'":"")+">"+symbol+" "+i+"</option>");
	%>
				<option value="0"><%=symbol+max %>+</option>
</select>

			<select name="pricemax" id="pricemax">
<%	for (int i=0;i<10;i=i+1)out.write("<option value=\""+i+"\""+(i==pricemaxint?" selected='selected'":"")+">"+symbol+" "+i+"</option>");
for (int i=10;i<20;i=i+2)out.write("<option value=\""+i+"\""+(i==pricemaxint?" selected='selected'":"")+">"+symbol+" "+i+"</option>");
for (int i=20;i<max;i=i+5)out.write("<option value=\""+i+"\""+(i==pricemaxint?" selected='selected'":"")+">"+symbol+" "+i+"</option>");
 %>
				<option value="<%=max %>"<%=(pricemaxint==max?" selected='selected'":"") %>><%=symbol+max %>+</option>
			</select>

</div></div><div class='sliderlegend'>
<div class="slider-min"><%=Webroutines.getCurrencySymbol(p.searchdata.getCurrency())%> <span id='slider-min'>0</span></div>
<div class="slider-max"><%=Webroutines.getCurrencySymbol(p.searchdata.getCurrency())%> <span id='slider-max'>200</span>+</div>
</div>
<input type='hidden' id="ratingmin" name="ratingmin" value="<%=ratingmin%>"/>
<input type='hidden' id="ratingmax" name="ratingmax" value="100"/>
<div id='criteria'>

<jsp:include page="/js/advicehtml.jsp" flush="true" >
     <jsp:param name="section"	value="gs" /> 
     <jsp:param name="json"	value="false" /> 
     <jsp:param name="resultsperpage"	value="5" /> 
     <jsp:param name="grape"	value="<%= grape %>" /> 
     <jsp:param name="winetype"	value="<%= winetype %>" /> 
     <jsp:param name="region"	value="<%= region %>" /> 
     <jsp:param name="currency"	value="<%= p.searchdata.getCurrency() %>" /> 
     <jsp:param name="pricemin"	value="<%= pricemin %>" /> 
     <jsp:param name="pricemax"	value="<%= pricemax %>" /> 
     <jsp:param name="ratingmin"	value="<%= ratingmin %>" /> 
     <jsp:param name="countryofseller"	value="<%= p.searchdata.getCountry() %>" /> 
     
</jsp:include>
</div>
<input type='hidden' name='winetype' id='winetype' value='<%=winetype %>'/>
<input type='hidden' name="subregions" value="true"/>
<input type="hidden" name="dosearch" value="true" />
<input type="hidden" name="pqratio" value="true" />		
<input type="hidden" name="pages" value="5" />	
<input type="hidden" name="resultsperpage" value="5" />
<div class='criterionh'><%=t.get("displaycurrency")%></div>
<input type="radio" name="currency" id="EUR" value="EUR" onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio"  onchange="javascript:newSearch();" name="currency" value="GBP" <%if (p.searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF"  onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD"  onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$
<input type="hidden" id="page" name="page" value="0"/>		
</form>
<!--</div><div class='b'><div></div></div></div>-->
</div><!--  guidedsearch-->
<div id='mainright'><div id="result">
<jsp:include page="/js/advicehtml.jsp" flush="true" >
     <jsp:param name="section"	value="results" /> 
     <jsp:param name="json"	value="false" /> 
     <jsp:param name="grape"	value="<%= grape %>" /> 
     <jsp:param name="winetype"	value="<%= winetype %>" /> 
     <jsp:param name="resultsperpage"	value="5" /> 
     <jsp:param name="region"	value="<%= region %>" /> 
     <jsp:param name="currency"	value="<%= p.searchdata.getCurrency() %>" /> 
     <jsp:param name="pricemin"	value="<%= pricemin %>" /> 
     <jsp:param name="pricemax"	value="<%= pricemax %>" /> 
     <jsp:param name="ratingmin"	value="<%= ratingmin %>" /> 
     
</jsp:include>
</div><!-- result -->
<div class='pricenote'><%=p.getTranslator().get("pricenote")%></div>
<% p.getLogger().type=((WineAdvice)request.getAttribute("advice")).loggerinfo;
p.getLogger().name=((WineAdvice)request.getAttribute("advice")).searchinfo;
%>
<%@ include file="/snippets/textpagefooter.jsp" %>	
</div><!--  mainright-->
<% }
%>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
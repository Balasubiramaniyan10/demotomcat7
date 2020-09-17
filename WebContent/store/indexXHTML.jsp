<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.online.Auditlogger"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Wine"
import = "com.freewinesearcher.common.Wineset"
%>
<jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/>
<% long start=System.currentTimeMillis();
	boolean debuglog=false;
	%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%PageHandler p=PageHandler.getInstance(request,response); if (p.ipaddress.equals("85.147.228.61")) debuglog=true;%>
<jsp:useBean id="cartmanager" class="com.freewinesearcher.online.shoppingcart.CartManager" scope="session"/>
<jsp:setProperty name="cartmanager" property="wineid" value="0" />
<jsp:setProperty name="cartmanager" property="*" />
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% 
	if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Storepage: Start Pagehandler"); %>
<% 
String message="";
	Shoppingcart cart=cartmanager.getCart(new Context(request),0,false);
	if (cart.shopid==0){
		%><jsp:include page="/error.jsp" /><%
	} else {
%>
<jsp:setProperty name="cartmanager" property="amount"  value="0"/> 
<%@page import="com.freewinesearcher.online.shoppingcart.Shoppingcart"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.Comparator"%>
<%@page import="com.freewinesearcher.common.Knownwine"%>
<%@page import="com.freewinesearcher.online.Shop"%>
<%@page import="com.freewinesearcher.common.Configuration"%>
<%@page import="com.freewinesearcher.online.shoppingcart.CartManager"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.online.shoppingcart.CartSerializer"%>
<%@page import="com.freewinesearcher.online.WineAdvice"%>
<%@page import="com.freewinesearcher.common.datamining.Chart"%>
<%@page import="com.freewinesearcher.online.StoreInfo"%>
<%@page import="com.freewinesearcher.online.RecommendationAd"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%
	out.print(Webroutines.getShopNameFromShopId(cart.shopid,"")+" shopping recommendations on Vinopedia");
	
%></title>
<%
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
	Shop shop=StoreInfo.getStore(cartmanager.getShopid());
	if (shop.name.toLowerCase().contains("boottle")&&!p.bot){
		Dbutil.logger.info("Storepage "+shop.name+" was accessed with url "+p.thispage+". IP="+p.ipaddress+". Useragent="+request.getHeader("User-Agent")+"");
	}
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%if (request.getAttribute("originalQueryString")!=null&&((String)request.getAttribute("originalQueryString")).length()>0) { %><link rel="canonical" href="https://www.vinopedia.com/store/<%=Webroutines.URLEncode(Webroutines.removeAccents(shop.name))%>/" /><%} %>
<meta name="keywords" content="<%=shop.name %>, wine, shopping, recommendations" />
<meta name="description" content="Vinopedia shows you recommendations on the best wines <%=shop.name %> has to offer." />
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<script type='text/javascript'>/*<![CDATA[*/function loadframe(){if ($('#storeiframe').attr('src')=='') window.setTimeout("$('#storeiframe').attr('src','<% 
	String url=ExternalManager.makeAffiliateUrl((cartmanager.thiswine!=null?cartmanager.thiswine.SourceUrl:shop.shopurl),shop.affiliateparams).replaceAll("'","\\'");
	if (url.contains("boottle")) {
		if (cartmanager.thiswine!=null){
			url="/link.jsp?wineid="+cartmanager.thiswine.Id;
		} else {
			url="/link.jsp?shopid="+cartmanager.getShopid();
		}
	}
	out.write(url);%>');",200);}/*]]>*/</script>

</head>


<body onload="javascript:loadframe();">

<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	
	RecommendationAd ra=new RecommendationAd(cart.wineid);
	ra.setShopid(cart.shopid);
	ra.setCountrycode(shop.countrycode);
	if (p.hostcountry!=null&&p.hostcountry.length()>0) ra.setCountrycode(p.hostcountry);
	String skyscraper=ra.getStoreSkyScraper();
	if (!p.bot) p.getLogger().setBannersshown(p.getLogger().getBannersshown()+ra.getBannersshown());
	String rightadhtml=ra.getAd(p,"Storead");
	if (rightadhtml.equals("")){
		Ad rightad = new Ad("winered",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.bestknownwineid,"");
		rightad.getHTML();
		rightadhtml=rightad.html;
	}
		//Ad bottomleftad = new Ad("newdesign",187, 300, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner + "");			
	cu.setAl(new Auditlogger(request));
	cu.setId(shop.id);
	cu.setTablename("shops"); 
	cu.setIdcolumn("id");
	boolean edit=false;
	if (request.isUserInRole("admin")||cu.validAccessCode()) {
		edit=true;
	}
	
%>
		<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %></div>
		<div class='main<%if (skyscraper.length()>0) {%>store<%} %>'><br/>
<%@page import="java.io.File"%>
<%@page import="com.freewinesearcher.common.Configuration"%>
<%	String imgfile=""; 
	File dir = new File(Configuration.workspacedir+"store"+System.getProperty("file.separator"));
FileFilter fileFilter = new WildcardFileFilter(shop.id+".*");
File[] files = dir.listFiles(fileFilter); 
if (files.length>0) {
   imgfile=(files[0].getName());
} else {
	fileFilter = new WildcardFileFilter(shop.id+" *.*");
	files = dir.listFiles(fileFilter);
	if (files.length>0) {
		imgfile=(files[0].getName());
	}
}  

 %>

<%@page import="java.io.FileFilter"%>
<%@page import="org.apache.commons.io.filefilter.WildcardFileFilter"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.online.ExternalManager"%><div class='storeinfo'><h1 class='category'><img class='categoryicon' src='/images/merchant.gif' alt='merchant'/><span class='categorylabel'>Merchant: </span><%=shop.name %></h1>

<div class='clearboth'></div>
		<ul class='tabs' id='storetabs'><li><a href='#storehome'>Selected wine</a></li><li><a href='#storegeneral'>General info</a></li><li><a href='#storelocation' id='maptab'>Location</a></li><li><a href='#recommendations' id='recommendationtab'>Shopping Guide</a></li><li><a href='#storestats' id='statstab'>Wine regions</a></li><li><a href='#carttab' id='shoppinglist'>Shopping list</a></li><% if((shop.description!=null&&!shop.description.equals(""))||edit) out.write("<li><a href='#descriptionpane'>About</a></li>");%></ul>
	<div class='panes' id='storepanes'>
	<div class='storepane' id='storehome'>
	<div class='storecontent'>
	<div class='header'>The web site of the store is shown below. Loading this page might be slower than usual depending on the speed of the web site of the merchant. To view their home page full-screen, <a href='/external.jsp?shopid=<%=shop.id%>'>click here</a>.</div>
	<iframe id="storeiframe" src=""  ></iframe>
	</div>
	</div>
	<div class='storepane' id='storegeneral'>
	<div class='storecontent'>
		<div class='businesscard' >
			<div class='text'>
				<h3><%=shop.name %></h3>
				<%=shop.address.replace(", ",",<br/>").replaceAll("(?<=(^|,)[0-9 ]*),<br/>"," ") %><br/>
				<%=shop.shopurl.equals("")?"":"Web site: <a href='/external.jsp?shopid="+shop.id+"'>"+shop.shopurl+"</a><br/>"%>
			</div>
		</div>
<% 		if (imgfile!=null&&imgfile.length()>0) out.print("<div class='storeimage'>"+(shop.shopurl.equals("")?"":"<a href='/external.jsp?shopid="+shop.id+"'>")+"<img src='/storeimage/"+imgfile+"' alt=\"\"/>"+(shop.shopurl.equals("")?"":"</a>")+"</div>");
					
		
 
	%>				
	<%=shop.getShopInfoText(p) %>
		
	</div>
	</div>
	<div class='storepane' id='storelocation'>
	<div class='storecontent'>
	<div id="map" style="width: 936px; height: 500px;"></div>
	</div>
	</div>
	<div class='storepane' id='recommendations'>
	<div class='storecontent'>
	<div id='winedetails'>
<% if (cartmanager.getWineid()>0){ %>
<jsp:include page="/js/advicehtml.jsp?section=getwinehtml" flush="true" >
     <jsp:param name="shopid"	value="<%= cart.shopid %>" /> 
     <jsp:param name="currency"	value="<%= p.searchdata.getCurrency() %>" /> 
     <jsp:param name="wineid"	value="<%= cartmanager.getWineid() %>" /> 
</jsp:include>
<%} %> 

</div>
	<div id='adright'><%out.write(rightadhtml);%></div>
<% 
int pricemaxint=200;
int priceminint=0;
%>

		<div id='guidedsearch'>
<!--<div class='dialog crit'><div class='content'><div class='t'></div>-->
		<form id="GuidedSearchform"  action="#" onsubmit='javascript:newSearch();return false;' autocomplete="off">
		<h4>Refine criteria</h4> 
		<noscript>
		<h4><font color='red'>Javascript in currently disabled in your browser. In order to use the Buying Guide you need to enable Javascript... </font></h4>
		</noscript>
<div class='criterionh'>By price range:</div>
<img id='priceclose' src='/images/transparent.gif' class='close sprite2 sprite2-close' onclick='javascript:clearBudget();' alt='Clear price range'/><img class='spinner' id='pricespinner' alt='Loading...' src='/images/spinner.gif'/><div class='slidercontainer'><div class='slider' id='priceslider'>
<select name="pricemin" id="pricemin">
<% 	int max=200;
	String symbol=Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.getCurrency()+"';", "symbol");
	for (int i=0;i<max;i=i+5)out.write("<option value=\""+i+"\""+(i==priceminint?" selected='selected'":"")+">"+symbol+" "+i+"</option>"); %>
				<option value="0"><%=symbol+max %>+</option>
</select>

			<select name="pricemax" id="pricemax">
<%	for (int i=0;i<max;i=i+5)out.write("<option value=\""+i+"\""+(i==pricemaxint?" selected='selected'":"")+">"+symbol+" "+i+"</option>"); %>
				<option value="<%=max %>"<%=(pricemaxint==max?" selected='selected'":"") %>><%=symbol+max %>+</option>
			</select>

</div></div><div class='sliderlegend'>
<div class="slider-min"><%=Webroutines.getCurrencySymbol(p.searchdata.getCurrency())%> <span id='slider-min'>0</span></div>
<div class="slider-max"><%=Webroutines.getCurrencySymbol(p.searchdata.getCurrency())%> <span id='slider-max'>200</span>+</div>
</div>
<input type='hidden' id="ratingmin" name='ratingmin' value="80"/>
<input type='hidden' id="ratingmax" name='ratingmax' value="100"/>
<div id='criteria'>
<jsp:include page="/js/advicehtml.jsp" flush="true" >
     <jsp:param name="json"	value="false" /> 
     <jsp:param name="section"	value="gs" />  
     <jsp:param name="shopid"	value="<%= cart.shopid %>" />  
     <jsp:param name="currency"	value="<%= p.searchdata.getCurrency() %>" /> 
     <jsp:param name="pricemin"	value="<%= priceminint %>" /> 
     <jsp:param name="resultsperpage"	value="20" /> 
</jsp:include>
</div>
<input type='hidden' name='winetype' id='winetype' value='ALLTYPES'/>
<input type='hidden' name="subregions" value="true"/>
<input type="hidden" name="dosearch" value="true" />
<input type='hidden' name='shopid' id='shopid' value='<%= cartmanager.getShopid() %>'/>
<input type="hidden" name="dosearch" value="true" />
<input type="hidden" name="pages" value="5" />
<input type="hidden" name="resultsperpage" value="20" />	
<div class='criterionh'><%=p.getTranslator().get("displaycurrency")%></div>
<input type="radio" name="currency" id="EUR" value="EUR" onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio"  onchange="javascript:newSearch();" name="currency" value="GBP" <%if (p.searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF"  onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD"  onchange="javascript:newSearch();" <%if (p.searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$
<input type="hidden" id="page" name="page" value="0"/>		
</form>
<!--</div><div class='b'><div></div></div></div>-->
</div><!--  guidedsearch-->
<div id='mainright'>
<div id="result" class='shopresult'>
<jsp:include page="/js/advicehtml.jsp?section=results" flush="true" >
     <jsp:param name="json"	value="false" /> 
     <jsp:param name="activepane"	value="1" /> 
     <jsp:param name="shopid"	value="<%= cart.shopid %>" /> 
     <jsp:param name="currency"	value="<%= p.searchdata.getCurrency() %>" /> 
     <jsp:param name="resultsperpage"	value="20" /> 
     <jsp:param name="pricemin"	value="<%= priceminint %>" /> 
</jsp:include>
</div><!-- result -->
<div class='pricenote'><%=p.getTranslator().get("pricenote")%></div>
	</div>
	</div>	</div>
	<div class='storepane' id='storestats'>
	<div class='storecontent'>
	<div id='shopcharts' style='float: left;'><%=shop.getShopStatsText(PageHandler.getInstance(request,response)) %></div>
	</div>
	</div>
	<div class='storepane' id='carttab'>
	<div class='storecontent' id='cart'>
	<%=cartmanager.getCart(new Context(request),0,false).getSimpleCartHTML(p,cartmanager.oldcart) %>
	</div>
	</div>
	<% if ((shop.description!=null&&!shop.description.equals(""))||edit){%>
	<div class='storepane' id='descriptionpane'>
	<div class='storecontent' id='description'>
	<%=shop.description %>
	</div>
	</div>
	<%} %>
	</div><%if (skyscraper.length()>0) {%><div id="storeskyscraper"><%=skyscraper %></div><%} %>
	</div>		
<div id="my_chart"></div>
<div class='clearboth'></div>


<%if (edit){
	


	out.write("<script type='text/javascript' src='/js/tiny_mce/tiny_mce.js'></script>");
	cu.setContentcolumn("description");
	cu.setElementid("description");
	out.write(cu.getHtml(request));
	
} %>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start footer"); 
 p.getLogger().type=((WineAdvice)request.getAttribute("advice")).loggerinfo;
 p.getLogger().wineid=cartmanager.getWineid()+"";
 
 if (cartmanager.thiswine!=null&&cartmanager.thiswine.PriceEuroEx!=null) p.getLogger().price=cartmanager.thiswine.PriceEuroEx.floatValue();
 
 if (cartmanager.getWineid()>0) p.getLogger().knownwineid=Dbutil.readIntValueFromDB("select * from wines where id="+cartmanager.getWineid(),"knownwineid");
 p.getLogger().shopid=((WineAdvice)request.getAttribute("advice")).shopid+"";
	p.getLogger().name=((WineAdvice)request.getAttribute("advice")).searchinfo;
	if (request.getAttribute("originalQueryString")!=null&&((String)request.getAttribute("originalQueryString")).length()>0) request.setAttribute("canonicallink","Permalink: <a href='https://www.vinopedia.com/store/"+Webroutines.URLEncode(Webroutines.removeAccents(shop.name))+"/'>"+Webroutines.getShopNameFromShopId(cart.shopid,"")+" shopping recommendations</a>");
%>
<jsp:include page="/snippets/footer.jsp" flush="true" />
		
			
		</div>

	<%} %>
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<% out.write(((String)request.getAttribute("originalURL")).contains("localhost")?Configuration.GoogleApiKeyDev:Configuration.GoogleApiKey);%>" type="text/javascript"></script>
<script type="text/javascript">var center = new GLatLng(<%=shop.lat%>,<%=shop.lon%>);var shopid=<%=cartmanager.getShopid() %>;</script>
<script type="text/javascript">$(document).ready(function() {init();});</script>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>
<%}%>
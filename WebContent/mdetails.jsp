<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@page import="com.freewinesearcher.common.Knownwine"%>
<%@ page   
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
	import = "com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Configuration"
%><%
	session = request.getSession(true); 
	Wine wine=null;
	int wineid=0;
	try{
		wineid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("wineid")));
		wine=new Wine(wineid);}catch(Exception e){}
		
	NumberFormat format  = new DecimalFormat("#,##0.00");
	PageHandler p=PageHandler.getInstance(request,response,"Mobile Details");
	if (!p.block){
	 	
	p.searchdata.sponsoredresults=true;
	p.searchpage="/m"; 
	p.searchdata.numberofrows=25;
	p.processSearchdata(request);
	boolean showwinelist=false;
	if (p.s!=null&&p.s.wineset!=null&&(p.s.wineset.searchtype.equals("smart")||"true".equals(request.getParameter("freetext")))){
		
	}else if (p.searchdata.getName().length()>2&&p.s.wineset!=null&&p.s.wineset.Wine!=null){
		showwinelist=true;
		p.s.wineset.getKnownWineList();
	}
	int singlevintage=0;
	try {
		singlevintage=Integer.parseInt(p.searchdata.getVintage().trim());
	} catch (Exception e){}
	Wineset wineset=p.s.wineset;
	Knownwine k=new Knownwine(p.s.wineset.bestknownwineid);
	k.getProperties();
	
	if (p.newsearch){//keepdata should not be cached
	response.setHeader("Cache-Control","max-age=3600");
	response.setDateHeader ("Expires", 0);
	} else {
		response.setHeader("Pragma","no-cache");
			
	}
	 	 	 		String ipaddress="";
 	 	 	 	    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
 	 	 	 	    	ipaddress = request.getRemoteAddr();
 	 	 	 	    } else {
 	 	 	 	        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
 	 	 	 	    }
 	 	 	 	    
 	 	 	 		ArrayList<String> countries = Webroutines.getCountries();
 	 	 	 		if (p.searchdata.getVat()==null||p.searchdata.getVat().equals("")) p.searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));

 	 	 	 		Translator.languages language=Translator.getLanguage(p.searchdata.getLanguage());
 	 	 	 	    if (language==null){
 	 	 	 	    	language=Translator.getDefaultLanguageForCountry(Webroutines.getCountryCodeFromIp(ipaddress));
 	 	 	 	    }
 	 	 	 	    Translator t=new Translator();
 	 	 	 	    t.setLanguage(language);
 	 	 	 	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
 	 	 	 String headerwinename="";
 	 	 	 if ("yes".equals(request.getParameter("data"))){ 	
 	 	 		out.print("<!DOCTYPE html><html><head><title>.</title></head><body><div data-role='page' >");
 	 	 		 out.print(Webroutines.mobileprices(wineset,p));
 	 	 		 if (wineset.records>p.searchdata.getOffset()){
  	 	 		out.print("<a href='/m?keepdata=true&amp;offset="+(p.searchdata.getOffset()+p.searchdata.numberofrows)+"&amp;data=yes' data-icon='gear' class='more ui-btn-right' title='Settings'>More...</a>");
 	 	 		 } else {
 	 	 			out.print("<a href='nomore' data-icon='gear' class='more ui-btn-right' title='Settings'></a>");
 	 	 	 		 
 	 	 		 }
  	 	 	out.print("</div></body></html>");
	 	 		
  	 	 	} else{
  	 	 	
 	%><!DOCTYPE html> 
<html> 
	<head> 
	<%@ include file="/mobile/includes.jsp" %>
	<title><%
	if (!p.searchdata.getName().equals("")){out.print(p.searchdata.getName()+" price at "+wine.Shopname);} else {out.print("vinopedia");}
%></title>
<%
	
	if (PageHandler.getInstance(request,response).s!=null&&PageHandler.getInstance(request,response).s.wineset!=null) headerwinename=Webroutines.escape(PageHandler.getInstance(request,response).s.wineset.knownwineid>0?Knownwines.getKnownWineName(PageHandler.getInstance(request,response).s.wineset.knownwineid):p.searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", "");
	if (!p.searchdata.getName().equals("")) {
		out.print("<meta name=\"keywords\" content=\"");
		for (int i=0;i<headerwinename.split("\\s+").length;i++){
			out.print(headerwinename.split("\\s+")[i]+", ");
		}
		out.print(headerwinename+", wine ratings, compare prices\" />");
	} else {
		out.print("<meta name=\"keywords\" content=\"wine, search, wines, searcher, free wine searcher, wine searcher,port, compare, wine comparison, buy, buying, shopping, shop, precio, price, prices, prijs, prijzen, preis, prix, free, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn vergelijken, wijn, wijn prijs, zoeken, zoeker, kopen, vente, achat, acheter, vin, acheter du vin, Wein, kaufen, suchen, vino, buscar\" />");
	}
	
if ("NL".equals(p.searchdata.getLanguage())){
	if (!headerwinename.equals("")&&PageHandler.getInstance(request,response).s!=null) {
		out.write("<meta name=\"description\" content=\""+PageHandler.getInstance(request,response).t.get("pricecomparisonfor")+" "+headerwinename+" (mobiele versie, "+PageHandler.getInstance(request,response).s.wineset.records+" aanbiedingen gevonden)\" />");
	} else {
		out.write("<meta name=\"description\" content=\"De meest complete gratis prijsvergelijkingssite van wijnen in Europa.\" />");
	}

 } else {if (!headerwinename.equals("")&&PageHandler.getInstance(request,response).s!=null) { 
		out.print("<meta name=\"description\" content=\""+PageHandler.getInstance(request,response).t.get("pricecomparisonfor")+" "+headerwinename+" (mobile version, "+PageHandler.getInstance(request,response).s.wineset.records+" wine prices found)\" />");
	} else {
		out.print("<meta name=\"description\" content=\"Vinopedia is a free price comparison engine for millions of wines world wide. Find your favorite wine for the best price.\" />");
	} 
 }
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
<%if (p.s.wineset.canonicallink!=null&&!p.s.wineset.canonicallink.equals("")){ %>
<link rel="canonical" href="<%=p.s.wineset.canonicallink.replaceAll("/wine","/mwine")%>" />
<%} %>

</head> 
<body>
<div data-role="page" data-theme="a" class="vp" id="details"><%@ include file="/mobile/header.jsp" %><div data-role="content">
<% if (wine!=null){%>
<table>
<tr><td>Wine name:</td><td><%=wine.Name %></td></tr>
<tr><td>Vintage:</td><td><%=wine.Vintage %></td></tr>
<tr><td>Size:</td><td><%=wine.Size+" l." %></td></tr>
<tr><td>Sold at:</td><td><%=wine.Shopname %> (<%=wine.Country %>)</td></tr>
<tr><td>Price:</td><td><%=Webroutines.formatPriceMobile(wine.PriceEuroIn,wine.PriceEuroEx,p.searchdata.getCurrency(),"EX")%> excl. VAT</td></tr>
<tr><td>Price as listed at store:</td><td><%=Webroutines.getCurrencySymbol(Dbutil.readValueFromDB("select * from shops where id="+wine.ShopId, "currency"))+" "+Webroutines.formatPrice((double)wine.Price)+(Dbutil.readIntValueFromDB("select * from shops where id="+wine.ShopId, "exvat")==0?" (incl. VAT)":" (excl. VAT)")%></td></tr>
</table>
<a href='/link.jsp?wineid=<%=wineid%>' data-rel="back" data-role="button" data-inline="true"  data-icon="back">Back</a>
<a href='/link.jsp?wineid=<%=wineid%>' rel='external' data-role="button" data-inline="true" data-theme="b" data-icon="arrow-r">Visit store</a>
	<%} else {
		%>Sorry... wine not found!
	<%}	%>

<%@ include file="/mobile/footer.jsp" %>
</div><!--  content -->
</div><!-- /page -->
</body>
</html>
<% 	} } %>
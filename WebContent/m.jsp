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

	session.setAttribute("sneakpreview",true);
	NumberFormat format  = new DecimalFormat("#,##0.00");
	PageHandler p=PageHandler.getInstance(request,response,"Mobile Search Sneak Preview");
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
 	 	 		 if (wineset.records>p.searchdata.getOffset()+p.searchdata.numberofrows){
  	 			out.print("<a class='more'  href='/m?keepdata=true&amp;sneakpreview=true&amp;offset="+(p.searchdata.getOffset()+p.searchdata.numberofrows)+"&amp;data=yes' ></a>");
 	 	 		 } 
  	 	 	out.print("</div></body></html>");
	 	 		
  	 	 	} else{
  	 	 	
 	%><!DOCTYPE html> 
<html> 
	<head> 
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="-1">
	
	<%@ include file="/mobile/includes.jsp" %>
	<title><%
	if (!p.searchdata.getName().equals("")){out.print(Spider.escape(p.searchdata.getName())+" prices by vinopedia");} else {out.print("vinopedia");}
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

<%if (p.s.wineset.canonicallink!=null&&!p.s.wineset.canonicallink.equals("")){ %><link rel="canonical" href="<%=p.s.wineset.canonicallink.replaceAll("/wine","/mwine")%>" /><%} %>
</head> 
<body>
<div data-role="page" data-theme="a" class="vp" id="prices">
<%@ include file="/mobile/header.jsp" %>

<div data-role="content">
<span id="status"></span>	

<%
		if (p.searchdata.getName().length()<3) { 
		p=PageHandler.getInstance(request,response,"Mobile load");
		if (request.getParameter("dosearch")!=null) {
			out.print("<br/><font class='warn'>Please enter at least 3 characters for the wine name.</font>");
		}
			}
			
			if (p.searchdata.getName().length()>2) {
		//Webroutines.logWebAction("Mobile Search",request.getServletPath(),ipaddress,  request.getHeader("referer"), p.searchdata.getName(),p.searchdata.getVintage(), p.searchdata.getCreated(),p.searchdata.getPricemin(), p.searchdata.getPricemax(), p.searchdata.getCountry(), p.searchdata.getRareold(), "", "", "", "",0.0);
				if (showwinelist){
					out.print(Webroutines.getRefineResultsMobile(p, 100,true));
				} else if (wineset==null||wineset.Wine==null||wineset.Wine.length==0){
			out.print("No results found. ");
			ArrayList<String> alternatives=com.freewinesearcher.common.Knownwines.getAlternatives(p.searchdata.getName());
			if (alternatives.size()>0){	
		out.write ("<br/><br/>Were you looking for:<br/>");
		for (int i=0;i<alternatives.size();i=i+3){
			out.write("<a href='/mwine/"+Webroutines.URLEncodeUTF8Normalized(alternatives.get(i+1))+"'>"+alternatives.get(i)+ "</a>&#160;("+alternatives.get(i+2)+" "+(alternatives.get(i+2).equals("1")?p.getTranslator().get("wine"):p.getTranslator().get("wines"))+")<br/>");
		}
			}
			//if (wineset.searchtype.equals("text")) out.print(Webroutines.didYouMean(p.searchdata.getName(),"/mobile.jsp"));
		} else {
			
	if (k.getProperties().get("producer")!=null&&k.getProperties().get("locale")!=null){			
	%>
	<div data-role="controlgroup" data-type="horizontal">
<a href="#prices" class="ui-btn-active" data-role="button">Prices</a>
<a  data-role="button" <%=(wineset.knownwineid>0?"href=\"#ratings\"":"href=\"#\" class='ui-disabled'") %>>Ratings</a>
<a href="/mwinery/<%=Webroutines.URLEncodeUTF8Normalized(k.getProperties().get("producer")).replaceAll("%2F", "/").replace("&", "&amp;")%>&amp;sneakpreview=true" data-role="button">Winery</a>
<a rel='external' href="/mregion/<%=Webroutines.removeAccents(k.getProperties().get("locale")).replaceAll(", ", "/").replaceAll(" ", "+").replaceAll("'", "&apos;")%>&amp;sneakpreview=true" data-role="button">Region</a>

</div><%} %>
	<h2><%=p.searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ","")%></h2><% 		
	out.print("<form autocomplete='off'  action='/m?keepdata=true&amp;sneakpreview=true' method='post'><div data-role='fieldcontain'><label for='vintage' class='select'>Vintage:</label><select name='vintage' id='vintage' onchange='$(this).parents(\"form\").submit();'>");
	out.print("<option value=\"All\""+(p.searchdata.getVintage().equals("All")||p.searchdata.getVintage().equals("")?(" selected=\"selected\""):"")+">"+p.t.get("all")+"</option>");
	for (int vintage:wineset.vintages.descendingSet()){
		out.print("<option value=\""+vintage+"\""+(p.searchdata.getVintage().equals(vintage+"")?(" selected=\"selected\""):"")+">"+vintage+"</option>");
	}
	out.print("</select></div>");

	%>
		Sort by:<div data-role="controlgroup" data-type="horizontal" id="sortgroup"><%
		out.print("<a "+(p.searchdata.getOrder().contains("price")?"class='ui-btn-active' ":"")+"data-role='button' href='/m?keepdata=true&amp;sneakpreview=true&amp;offset=0&amp;order="+Webroutines.URLEncode(Webroutines.getOrder(p.searchdata.getOrder(),"priceeuroex"))+"'>Price</a>");	
		out.print("<a "+(p.searchdata.getOrder().contains("vintage")?"class='ui-btn-active' ":"")+"data-role='button' href='/m?keepdata=true&amp;sneakpreview=true&amp;offset=0&amp;order="+Webroutines.URLEncode(Webroutines.getOrder(p.searchdata.getOrder(),"vintage"))+"'>Vintage</a>");
		out.print("<a "+(p.searchdata.getOrder().contains("size")?"class='ui-btn-active' ":"")+"data-role='button' href='/m?keepdata=true&amp;sneakpreview=true&amp;offset=0&amp;order="+Webroutines.URLEncode(Webroutines.getOrder(p.searchdata.getOrder(),"size"))+"'>Size</a>");
		out.print("<a "+(p.searchdata.getOrder().contains("distance")?"class='ui-btn-active distancebtn' ":"class='distancebtn'")+"data-role='button' "+(p.searchdata.getLon()!=0&&p.searchdata.getLat()!=0?"href='/m?keepdata=true&amp;sneakpreview=true&amp;offset=0&amp;order=distance'":"href='#' ")+">Distance</a>");

		%></div></form><%	
 													
		out.print(wineset.records+" offers found<br/>");
	out.print(Webroutines.mobileprices(wineset,p));	
	if (wineset.records>p.searchdata.getOffset()+p.searchdata.numberofrows){
			out.print("<a href='/m?sneakpreview=true&amp;keepdata=true&amp;offset="+(p.searchdata.getOffset()+p.searchdata.numberofrows)+"&amp;data=yes' class='more' title='more results'></a>");
	 		 } 
	}
	//scroll



			
			}	
			if (!showwinelist) if (wineset!=null&&wineset.Wine!=null&&wineset.Wine.length>0){
	%><div id="note">Note: Prices shown exclude VAT, shipping and handling costs and may exclude duty. Always check the price with the seller.</div><%
		} else {
		out.print("Today's wine tips:");
		out.print(Webroutines.getTipsHTML("",(float)0.75,20,"m.jsp",Translator.languages.EN).replaceAll("&nbsp;","&#160;").replaceAll("&euro;","&#8364;").replaceAll("tip=true","tip=true&amp;sneakpreview=true"));
		out.print((Webroutines.getTipsHTML("",(float)0.75,20,"m.jsp",Translator.languages.EN)).equals("")?("<br />"+new Translator(Translator.languages.EN).get("notips")):("<br />"+new Translator(Translator.languages.EN).get("pricenote")+"<br />"));
		
}%>	
</div><!-- /content -->


</div><!-- /page -->
<div data-role="page" data-theme="a" class="vp" id="ratings">
<%@ include file="/mobile/header.jsp" %>
<div data-role="content">	
<div data-role="controlgroup" data-type="horizontal">
<a href="#prices" data-role="button">Prices</a>
<a  data-role="button" class="ui-btn-active" <%=(wineset.knownwineid>0?"href=\"#ratings\"":"href=\"#\" class='ui-disabled'") %>>Ratings</a>
<a rel='external' href="/mwinery/<%=Webroutines.URLEncodeUTF8Normalized(k.getProperties().get("producer")).replaceAll("%2F", "/").replace("&", "&amp;")%>" data-role="button">Winery</a>
<a rel='external' href="/mregion/<%=Webroutines.removeAccents(k.getProperties().get("locale")).replaceAll(", ", "/").replaceAll(" ", "+").replaceAll("'", "&apos;")%>" data-role="button">Region</a>
</div>
<% 
				out.print("<h2>"+Knownwines.getKnownWineName(wineset.bestknownwineid).replaceAll("&","&amp;")+(singlevintage>0?" "+singlevintage:"")+"</h2>");
				String ratings=Webroutines.getMobileRatingsHTML(wineset.bestknownwineid,"/m.jsp",singlevintage)+"";
				if (!ratings.equals("")) {
				out.print(ratings);
				}%>
<%@ include file="/mobile/footer.jsp" %>
</div><!--  content -->
</div><!-- /page -->
</body>
</html>
<% 	} } %>
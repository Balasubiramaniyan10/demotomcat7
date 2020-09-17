<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd">
<%@page import="com.freewinesearcher.common.Knownwine"%>
<%@page import="com.freewinesearcher.common.Configuration"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" ><%@ page   
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
%><%
	session = request.getSession(true); 

	NumberFormat format  = new DecimalFormat("#,##0.00");
%><jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/><%

	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset="0";
%><jsp:setProperty name="searchdata" property="name" value=""/><jsp:setProperty name="searchdata" property="order" value=""/><jsp:setProperty name="searchdata" property="vintage" value=""/><jsp:setProperty name="searchdata" property="priceminstring" value=""/><jsp:setProperty name="searchdata" property="pricemaxstring" value=""/><jsp:setProperty name="searchdata" property="*"/><jsp:setProperty name="searchdata" property="offset" value="0"/><%
			}else {
		%><jsp:setProperty name="searchdata" property="*"/><%
 					}

if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")){
%><jsp:setProperty name="searchdata" property="vintage" value="<%=(searchdata.getVintage()+\" \"+Webroutines.getVintageFromName(searchdata.getName()))%>"/><jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/><%
 		}
PageHandler p=PageHandler.getInstance(request,response,"Mobile Search");
p.processSearchdata(request);
 	 	 	 		String ipaddress="";
 	 	 	 	    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
 	 	 	 	    	ipaddress = request.getRemoteAddr();
 	 	 	 	    } else {
 	 	 	 	        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
 	 	 	 	    }
 	 	 	 	if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")||(Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp"))){
 	 	 			
 	 	 	 	    if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
 	 	 	 	    	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
 	 	 	 	    	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
 	 	 	 			
 	 	 	 	    }
 	 	 	 	}else {
 	 	 	 	    
 	 	 	 		ArrayList<String> countries = Webroutines.getCountries();
 	 	 	 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));

 	 	 	 		Translator.languages language=Translator.getLanguage(searchdata.getLanguage());
 	 	 	 	    if (language==null){
 	 	 	 	    	language=Translator.getDefaultLanguageForCountry(Webroutines.getCountryCodeFromIp(ipaddress));
 	 	 	 	    }
 	 	 	 	    Translator t=new Translator();
 	 	 	 	    t.setLanguage(language);
 	%><%@ page contentType="application/xhtml+xml ; charset=UTF-8" %><%@page import="com.freewinesearcher.online.PageHandler"%><head><meta http-equiv="Content-Type" content="application/xhtml+xml;charset=UTF-8" /><meta name="viewport" content="width=320" /><meta name="MobileOptimized" content="320" /><title><%
	if (!searchdata.getName().equals("")){out.print(Spider.escape(searchdata.getName())+("".equals(searchdata.getVintage())?"":" "+searchdata.getVintage())+" wine price");} else {out.print("vinopedia");}
%></title><%
	session.setAttribute("winename",searchdata.getName());
	if (p.s.wineset.canonicallink!=null&&!p.s.wineset.canonicallink.equals("")){ %><link rel="canonical" href="<%=p.s.wineset.canonicallink.replace("/wine/","/mwine/")%>" /><%} 
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
	String headerwinename="";
	if (PageHandler.getInstance(request,response).s!=null&&PageHandler.getInstance(request,response).s.wineset!=null) headerwinename=Webroutines.escape(PageHandler.getInstance(request,response).s.wineset.knownwineid>0?Knownwines.getKnownWineName(PageHandler.getInstance(request,response).s.wineset.knownwineid):PageHandler.getInstance(request,response).searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", "");
	if (!PageHandler.getInstance(request,response).searchdata.getName().equals("")) {
		out.print("<meta name=\"keywords\" content=\"");
		for (int i=0;i<headerwinename.split("\\s+").length;i++){
			out.print(headerwinename.split("\\s+")[i]+", ");
		}
		out.print(headerwinename+", wine ratings, compare prices\" />");
	} else {
		out.print("<meta name=\"keywords\" content=\"wine, search, wines, searcher, free wine searcher, wine searcher,port, compare, wine comparison, buy, buying, shopping, shop, precio, price, prices, prijs, prijzen, preis, prix, free, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn vergelijken, wijn, wijn prijs, zoeken, zoeker, kopen, vente, achat, acheter, vin, acheter du vin, Wein, kaufen, suchen, vino, buscar\" />");
	}
if ("NL".equals(PageHandler.getInstance(request,response).searchdata.getLanguage())){
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
<meta name="apple-itunes-app" content="app-id=668028831<%=(p.s.wineset.knownwineid>0?", app-argument="+p.s.wineset.canonicallink.replaceAll("/wine","/mwine"):"")%>"/>
<meta name="viewport" content="width=device-width,initial-scale=1.0" />

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

<%
if (p.s.wineset.canonicallink!=null&&!p.s.wineset.canonicallink.equals("")){ %><link rel="canonical" href="<%=p.s.wineset.canonicallink.replaceAll("/wine","/mwine")%>" /><%} %>
<% String newloc=(PageHandler.getInstance(request,response).s.wineset.knownwineid>0?"/wine/":"/index.jsp?name=")+Webroutines.URLEncode((Webroutines.removeAccents(PageHandler.getInstance(request,response).searchdata.getName())+" "+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>0?PageHandler.getInstance(request,response).searchdata.getVintage():"")).trim()).replaceAll("%2F", "/");
	if (newloc.endsWith("?name=")) newloc=newloc.replace("?name=","");
 	if (false){ //Apparently, Google follows the link from the ormal page and gets served the normal page instead of mobile
	boolean forcemobile=false;
	try{forcemobile=Boolean.parseBoolean(request.getParameter("forcemobile"));}catch (Exception e){}
	if (p.firstrequest&&!p.mobile&&!forcemobile){
		if (newloc!=null&&!newloc.equals("")){
		response.setStatus(302);
		response.setHeader( "Location", newloc);
		response.setHeader( "Connection", "close" );
		return;
	}
	}
} %>
<%@ include file="/headersmall.jsp" %>
<% if (p.firstrequest&&p.iphone&&!p.appmessageshown){ %>
<%@ include file="/snippets/mobileapp.jsp" %><div id='mobileresult' style='display:none'><%} else { %>
<div id='mobileresult'>
<%} %>
Mobile version | <a href='<%=newloc %>'>Normal version</a><div><%
	Wineset wineset=p.s.wineset;
	
%><%
		if (searchdata.getName().length()<3) { 
		p=PageHandler.getInstance(request,response,"Mobile load");
	%><%
		if (request.getParameter("dosearch")!=null) {
			out.print("<br/><font class='warn'>Please enter at least 3 characters for the wine name.</font>");
		}
			}
			
			if (searchdata.getName().length()>2) {
		//Webroutines.logWebAction("Mobile Search",request.getServletPath(),ipaddress,  request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
		if (wineset==null||wineset.Wine==null||wineset.Wine.length==0){
			out.print("No results found. ");
			ArrayList<String> alternatives=com.freewinesearcher.common.Knownwines.getAlternatives(searchdata.getName());
			if (alternatives.size()>0){	
		out.write ("<br/><br/>Were you looking for:<br/>");
		for (int i=0;i<alternatives.size();i=i+3){
			out.write("<a href='/mwine/"+Webroutines.URLEncodeUTF8Normalized(alternatives.get(i+1)).replaceAll("%2F","/")+"'>"+alternatives.get(i)+ "</a>&#160;("+alternatives.get(i+2)+" "+(alternatives.get(i+2).equals("1")?t.get("wine"):t.get("wines"))+")<br/>");
		}
			}
			//if (wineset.searchtype.equals("text")) out.print(Webroutines.didYouMean(searchdata.getName(),"/mobile.jsp"));
		} else {
			int singlevintage=0;
				try {
					singlevintage=Integer.parseInt(searchdata.getVintage().trim());
				} catch (Exception e){}
				Knownwine k=new Knownwine(wineset.bestknownwineid);
				k.getProperties();
				out.print("<h1>"+Knownwines.getKnownWineName(wineset.bestknownwineid).replaceAll("&","&amp;")+(singlevintage>0?" "+singlevintage:"")+"</h1>");
				out.print(k.getDescription(singlevintage,true).replaceAll("&","&amp;"));
				String ratings=Webroutines.getRatingsHTML(wineset.bestknownwineid,11,"/iphonemobile.jsp",singlevintage)+"";
				if (!ratings.equals("")) {
				out.print(ratings);
				
				
				out.print(Webroutines.getRatingText(singlevintage,wineset.bestknownwineid));
				out.print("<h3><a href='"+Webroutines.winelink(k.uniquename,0,true)+"' title='"+k.name.replaceAll("'", "&apos;").replaceAll("&","&amp;")+" ratings' >See all vintages and ratings of "+k.name.replaceAll("&","&amp;")+"</a>.</h3>");
				}
				
	%><h2>Price comparison: <%
		out.print(wineset.records+" wines found");
		if (wineset.knownwineid==0) out.print(" for \""+searchdata.getName()+"\"");
		out.print("</h2>");
		%>
		Click to sort by <%
				out.print("<a href='/iphonemobile.jsp?keepdata=true&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"vintage")+"'>Vintage</a>");
			%>, <%
				out.print("<a href='/iphonemobile.jsp?keepdata=true&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"size")+"'>Size</a>, ");
			%><%
				out.print("<a href='iphonemobile.jsp?keepdata=true&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"priceeuroex")+"'>Price</a>");
    				 
 				
			if (wineset.searchtype.equals("text")){	
				if (wineset.knownwinelist.size()>1){
					out.print("<br/><a href='#refine'>Click to select a specific wine</a>");
				}
			}
 				for (int i=0;i<wineset.Wine.length;i++){
											
	out.print("<div class='results'><div");
	if (wineset.Wine[i].CPC>0){
		out.print(" class=\"sponsoredeven\"");
	} else {
		if (i%2==1){out.print(" class=\"odd\"");}
	}
	out.print (">");
	out.print("<div class='flag'><img src='/images/flags/"+wineset.Wine[i].Country.toLowerCase()+".gif' alt='"+wineset.Wine[i].Country.toUpperCase()+"'/></div><div class='wine'><a href='/details.jsp?wineid="+wineset.Wine[i].Id+"' >"+(wineset.Wine[i].Vintage+" "+Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name))).trim()+"</a></div>");
	out.print("</div></div>");
	out.print("<div class='results'><div");
	if (wineset.Wine[i].CPC>0){
		out.print(" class=\"sponsoredeven\"");
	} else {
		if (i%2==1){out.print(" class=\"odd\"");}
	}
	out.print (">");
	out.print("<div class='shop'><a href='/details.jsp?wineid="+wineset.Wine[i].Id+"' >"+Spider.escape(wineset.Wine[i].Shopname)+"</a></div>");
	out.print("<div class='price' >"+Webroutines.formatPriceMobile(wineset.Wine[i].PriceEuroIn,wineset.Wine[i].PriceEuroEx,p.searchdata.getCurrency(),"EX").replaceAll("&nbsp;","&#160;").replaceAll("&euro;","&#8364;")+"</div>");
	out.print("<div class='size' >" + Webroutines.formatSizecompact(wineset.Wine[i].Size).replaceAll("&nbsp;","&#160;")+"</div>");
	out.print("</div></div>");
															}
	if (wineset.records>Webroutines.numberofnormalrows){
	out.print("Page ");
	for (int i=0;i<wineset.records;i=i+Webroutines.numberofnormalrows){
	 out.print("<a href='"+response.encodeURL("/iphonemobile.jsp?keepdata=true&amp;offset="+i)+"' >");
	 if (Integer.toString(i).equals(offset)) out.print("<strong>");
	 out.print((i/Webroutines.numberofnormalrows+1)+"  ");
	 if (Integer.toString(i).equals(offset)) out.print("</strong>");
	 out.print("</a>");
	}
}

	}		

			if (wineset.knownwinelist.size()>1){
				NumberFormat knownwineformat  = new DecimalFormat("000000");	
				out.write ("<br/><div id='refine'>Refine results:<br/></div>");
				out.write ("<table>");
				int counter=0;
				for (int wines:wineset.knownwinelist.keySet()){
			if (counter<20&&wines>0) {
				out.write("<tr><td><a href='/mwine/"+(Webroutines.URLEncode(Webroutines.removeAccents(Knownwines.getKnownWineName(wines))))+(searchdata.getVintage().trim().length()>0?"?amp;vintage="+searchdata.getVintage().trim():"")+"'>"+(wines==0?"Unrecognized wines":(Spider.escape(Knownwines.getKnownWineName((wines)))))+"</a> </td><td>("+wineset.knownwinelist.get(wines)+" "+(wineset.knownwinelist.get(wines)==1?t.get("wine"):t.get("wines"))+")</td></tr>");
				counter++;
			}
				}
				out.write ("</table>");
			}
			}	%><%@ include file="/snippets/mobilesearchform.jsp" %><%
		if (wineset!=null&&wineset.Wine!=null&&wineset.Wine.length>0){
	%><div id="note">Note: Prices shown exclude VAT, shipping and handling costs and may exclude duty. Always check the price with the seller.</div><%
		} else {
		out.print("Today's wine tips:");
		out.print(Webroutines.getTipsHTML("",(float)0.75,20,"iphonemobile.jsp",Translator.languages.EN).replaceAll("&nbsp;","&#160;").replaceAll("&euro;","&#8364;"));
		out.print((Webroutines.getTipsHTML("",(float)0.75,20,"iphonemobile.jsp",Translator.languages.EN)).equals("")?("<br />"+new Translator(Translator.languages.EN).get("notips")):("<br />"+new Translator(Translator.languages.EN).get("pricenote")+"<br />"));
		
}
} //NZ filter %></div></div></div>
<%@ include file="/snippets/footersmall.jsp" %>
</body></html>
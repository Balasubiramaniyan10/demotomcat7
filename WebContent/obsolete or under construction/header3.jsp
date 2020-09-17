<%@ page   
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Knownwines"
	import="com.freewinesearcher.common.Dbutil"
%>
<%
	//if (request.getServerName().toLowerCase().contains("searchasaservice")&&!request.getServletPath().toLowerCase().contains("searchasaservice")) {
	//	response.sendRedirect("/searchasaservice.jsp");
	//	return;
	//}
	String originalurl=(String)request.getAttribute("originalURL");
	if (originalurl==null) originalurl="";	
 
	String querystring=(String)request.getAttribute("originalQueryString");
	if (querystring==null||querystring.equals("")) {
		querystring="";	
	} else {
		querystring="?"+querystring;
	}
	

	if (request.getParameter("logoff") != null) {
    response.sendRedirect("/logout.jsp");
    return;
  }
	if (PageHandler.getInstance(request,response).abuse){
		%><jsp:forward page="abuse.jsp" /><%
		return;
	}
	if (PageHandler.getInstance(request,response).block){
		out.print("An error occurred at line: 17 in the jsp file: /index.jsp");
		return;
	}
	if (originalurl.contains("freewinesearcher")&&!originalurl.contains("wine2/")){
		String newurl=originalurl.replace("freewinesearcher","vinopedia")+querystring;
		session.setAttribute("fws","true");
		response.setStatus(301);
		response.setHeader( "Location", newurl );
		response.setHeader( "Connection", "close" );
		return;
	}
	
	if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){
%>
<!-- google_ad_section_start -->
<%
	String headerwinename="";
	if (PageHandler.getInstance(request,response).s!=null&&PageHandler.getInstance(request,response).s.wineset!=null) headerwinename=Webroutines.escape(PageHandler.getInstance(request,response).s.wineset.knownwineid>0?Knownwines.getKnownWineName(PageHandler.getInstance(request,response).s.wineset.knownwineid):PageHandler.getInstance(request,response).searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", "");
	if (!PageHandler.getInstance(request,response).searchdata.getName().equals("")) {
		out.print("<meta name=\"keywords\" content=\"");
		for (int i=0;i<headerwinename.split("\\s+").length;i++){
			out.print(headerwinename.split("\\s+")[i]+", ");
		}
		out.print("buy "+headerwinename+", wine ratings, buy wine online, price, compare prices\" />");
	} else {
		out.print("<meta name=\"keywords\" content=\"wine, search, wines, searcher, free wine searcher, wine searcher,port, compare, wine comparison, buy, buying, shopping, shop, precio, price, prices, prijs, prijzen, preis, prix, free, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn vergelijken, wijn, wijn prijs, zoeken, zoeker, kopen, vente, achat, acheter, vin, acheter du vin, Wein, kaufen, suchen, vino, buscar\" />");
	}
if ("NL".equals(PageHandler.getInstance(request,response).searchdata.getLanguage())){
	if (!headerwinename.equals("")&&PageHandler.getInstance(request,response).s!=null) {
		out.write("<meta name=\"description\" content=\""+PageHandler.getInstance(request,response).t.get("pricecomparisonfor")+" "+headerwinename+" ("+PageHandler.getInstance(request,response).s.wineset.records+" aanbiedingen gevonden)\" />");
	} else {
		out.write("<meta name=\"description\" content=\"De meest complete gratis prijsvergelijkingssite van wijnen in Europa.\" />");
	}

 } else {if (!headerwinename.equals("")&&PageHandler.getInstance(request,response).s!=null) { 
		out.print("<meta name=\"description\" content=\""+PageHandler.getInstance(request,response).t.get("pricecomparisonfor")+" "+headerwinename+" ("+PageHandler.getInstance(request,response).s.wineset.records+" wine prices found)\" />");
	} else {
		out.print("<meta name=\"description\" content=\"Vinopedia is a free price comparison engine for millions of wines world wide. Find your favorite wine for the best price.\" />");
	} 
	
 }
	
%>
<%	if (!"Y".equals(Webroutines.filterUserInput(request.getParameter("print")))){%>


<link rel="stylesheet" type="text/css" href="/css/stylesheet2.css?version=18" />
<%} else {%>
<link rel="stylesheet" type="text/css" media="print" href="/print.css" />
<%} %>

<!-- google_ad_section_end -->
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<% if (originalurl.contains("/index.jsp")&&!originalurl.contains("wine-guide")){ 
%><meta name="robots" content="noindex,nofollow" />
<% } else if (originalurl.contains("/wine")&&!originalurl.contains("wine-guide")) {
		if (!originalurl.contains("lang-EN")&&!originalurl.contains("lang-FR")&&!originalurl.contains("/wine2")&&originalurl.replaceAll("\\d\\d\\d\\d\\d\\d","").equals(originalurl.replaceAll("\\d\\d\\d\\d\\d\\d","").replaceAll("\\d\\d\\d\\d",""))){
		%><meta name="robots" content="index,follow" />
<% 		} else {
		%><meta name="robots" content="noindex,nofollow" />
<%		}
   	} else if (originalurl.contains("topwines")) {
%><meta name="robots" content="noindex,follow" />
<%	} else {	
%><meta name="robots" content="index,follow" />
<% } %>
<link rel="icon" type="image/png" href="/favicon.png" />
<%@ include file="/snippets/jsincludes.jsp" %>
 <%} %>

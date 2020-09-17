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
	if (querystring==null) {
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
	if (originalurl.contains("searcher")){
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
	String headerwinename=Webroutines.escape(PageHandler.getInstance(request,response).s.wineset.knownwineid>0?Knownwines.getKnownWineName(PageHandler.getInstance(request,response).s.wineset.knownwineid):PageHandler.getInstance(request,response).searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", "");
	if (!PageHandler.getInstance(request,response).searchdata.getName().equals("")) {
		out.print("<meta name=\"keywords\" content=\""+headerwinename+", "+headerwinename+" price");
		for (int i=0;i<headerwinename.split("\\s+").length;i++){
			out.print(", "+headerwinename.split("\\s+")[i]);
		}
		out.print(", vinopedia, prices, compare, wine, wines, free wine searcher, freewinesearcher,wine searcher, buy, buying, shop, precio, price, prices, prijs, prijzen, preis, prix, kopen, vente, achat, acheter, kaufen, suchen, vino, buscar\" />");
	} else {
		out.print("<meta name=\"keywords\" content=\"wine, search, wines, searcher, free wine searcher, wine searcher,port, compare, wine comparison, buy, buying, shopping, shop, precio, price, prices, prijs, prijzen, preis, prix, free, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn vergelijken, wijn, wijn prijs, zoeken, zoeker, kopen, vente, achat, acheter, vin, acheter du vin, Wein, kaufen, suchen, vino, buscar\" />");
	}
	if ("NL".equals(session.getAttribute("language")+"")){
	if (!headerwinename.equals("")) {
		out.write("<meta name=\"description\" content=\"Prijsvergelijking van "+headerwinename+". Ontdek waar je deze wijn het beste en tegen de laagste prijs kunt kopen.\" />");
	} else {
		out.write("<meta name=\"description\" content=\"De meest complete gratis prijsvergelijkingssite van wijnen in Europa.\" />");
	}

 } else {if (!headerwinename.equals("")) {
		out.print("<meta name=\"description\" content=\"Price comparison for "+headerwinename+" ("+PageHandler.getInstance(request,response).s.wineset.records+" wine prices found)\" />");
	} else {
		out.print("<meta name=\"description\" content=\"vinopedia is a free price comparison engine for hundreds of thousands of wines. Find your favorite wine for the best price.\" />");
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
ga('send', 'pageview');
</script>
<!-- End Google Analytics -->

<!-- google_ad_section_end -->
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<% if (originalurl.contains("index.jsp")){ 
%><meta name="robots" content="noindex,nofollow" />
<% } else if (originalurl.contains("/wine")&&!originalurl.contains("/wine2")) {
		if (originalurl.replaceAll("\\d\\d\\d\\d\\d\\d","").equals(originalurl.replaceAll("\\d\\d\\d\\d\\d\\d","").replaceAll("\\d\\d\\d\\d",""))){
		%><meta name="robots" content="index,nofollow" />
<% 		} else {
		%><meta name="robots" content="noindex,nofollow" />
<%		}
   	} else if (originalurl.contains("topwines")) {
%><meta name="robots" content="noindex,follow" />
<%	} else {	
%><meta name="robots" content="index,follow" />
<% } %>
<link rel="icon" type="image/png" href="/favicon.png" />
<%	if (!"Y".equals(Webroutines.filterUserInput(request.getParameter("print")))){%>
<link rel="stylesheet" type="text/css" href="/stylesheet.css?version=2" />
<%} else {%>
<link rel="stylesheet" type="text/css" media="print" href="/print.css" />
<%} %>

 <%} %>

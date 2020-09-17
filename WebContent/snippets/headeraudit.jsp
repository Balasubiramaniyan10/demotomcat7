<%@ page   
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.PageHandler"
%>

<%@page import="com.freewinesearcher.online.Authorization"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<%
	//if (request.getServerName().toLowerCase().contains("searchasaservice")&&!request.getServletPath().toLowerCase().contains("searchasaservice")) {
	//	response.sendRedirect("/searchasaservice.jsp");
	//	return;
	//}
	if (request.getParameter("logoff") != null) {
    response.sendRedirect("/logout.jsp");
    return;
  }
%>
<%	
	Authorization auth=new Authorization(request);
	Auditlogger al=new Auditlogger(request);
	request.setAttribute("al",al);
	if (PageHandler.getInstance(request,response).abuse){
%><jsp:forward page="abuse.jsp" /><%
	return;
	}
	if (PageHandler.getInstance(request,response).block){
		out.print("An error occurred at line: 17 in the jsp file: /index.jsp");
		return;
	}
	if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){
%>
<!-- google_ad_section_start -->
<meta name="keywords" content="<%
	
	if (!PageHandler.getInstance(request,response).searchdata.getName().equals("")) out.print(Webroutines.escape(PageHandler.getInstance(request,response).searchdata.getName())+", ");%>wine, search, wines, searcher, port, compare, wine comparison, buy, buying, shopping, shop, precio, price, prices, prijs, prijzen, preis, prix, free, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn vergelijken, wijn, wijn prijs, zoeken, zoeker, kopen, vente, achat, acheter, vin, acheter du vin, Wein, kaufen, suchen, vino, buscar" />
<%
	if ("NL".equals(session.getAttribute("language")+"")){
	if (!PageHandler.getInstance(request,response).searchdata.getName().equals("")) {
		out.write("<meta name=\"description\" content=\"Prijsvergelijking van "+Webroutines.escape(PageHandler.getInstance(request,response).searchdata.getName())+". Ontdek waar je deze wijn het beste en tegen de laagste prijs kunt kopen.\" />");
	} else {
		out.write("<meta name=\"description\" content=\"De meest complete gratis prijsvergelijkingssite van wijnen in Europa.\" />");
	}

 } else {
	 out.write("<meta name=\"description\" content=\"Find and compare prices ");
 
	if (!PageHandler.getInstance(request,response).searchdata.getName().equals("")) {
		out.print("for "+Webroutines.escape(PageHandler.getInstance(request,response).searchdata.getName()));
	} else {
		out.print("of wines");
	} 
	out.write(". Free and unfiltered. Get daily search results by email and &#82;&#83;&#83; feeds.\" />");
 }
%>
<!-- google_ad_section_end -->
<% if ((String)request.getAttribute("originalURL")!=null&&((String)request.getAttribute("originalURL")).contains("index.jsp")){ %>
<meta name="robots" content="noindex,nofollow" />
<% } else if ((String)request.getAttribute("originalURL")!=null&&((String)request.getAttribute("originalURL")).contains("/wine")) {%>
<meta name="robots" content="index,nofollow" />
<% } else if ((String)request.getAttribute("originalURL")!=null&&((String)request.getAttribute("originalURL")).contains("topwines")) {%>
<meta name="robots" content="noindex,follow" />
<% } else {	%>
<meta name="robots" content="index,follow" />
<% } %>
<link rel="shortcut icon" href="/favicon.ico" />
<%	if (!"Y".equals(Webroutines.filterUserInput(request.getParameter("print")))){%>
<link rel="stylesheet" type="text/css" href="/css/stylesheet2.css?version=18" />
<%} else {%>
<link rel="stylesheet" type="text/css" media="print" href="/print.css" />
<%} %>

 <%} %>

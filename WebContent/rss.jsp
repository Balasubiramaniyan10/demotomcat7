<%	String ipaddress="";
	if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
		ipaddress = request.getRemoteAddr();
	} else {
	    ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
	}
	if (Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp")){
%><jsp:forward page="abuse.jsp" /><%
	return;
	} else{ %><jsp:directive.page contentType="application/xml; charset=utf-8" /><?xml version="1.0" encoding="utf-8" ?> 
<rss version="0.91">
<channel>
<title>vinopedia</title> 
<link>https://www.vinopedia.com/</link> 
<description>Find the lowest price on wines</description> 
<%@ page 
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.batch.Spider"
	import = "java.util.ArrayList"
%>
<%
	String username=Webroutines.filterUserInput(request.getParameter("username"));
	String code=Webroutines.filterUserInput(request.getParameter("code"));
	String name=Webroutines.filterUserInput(request.getParameter("name"));
	String vintage=Webroutines.filterUserInput(request.getParameter("vintage"));
	vintage=Webroutines.filterVintage(vintage);
	String message = Webroutines.filterUserInput(request.getParameter("message"));
	String country = Webroutines.filterUserInput(request.getParameter("country"));
	if (country==null) country="All";
	float pricemin=0;
	float pricemax=0;
	String createdstring=Webroutines.filterUserInput(request.getParameter("created"));
	String priceminstring=Webroutines.filterUserInput(request.getParameter("pricemin"));
	String pricemaxstring=Webroutines.filterUserInput(request.getParameter("pricemax"));
	String rareoldstring=Webroutines.filterUserInput(request.getParameter("rareold"));
	boolean rareold=Boolean.parseBoolean(rareoldstring);
	String data="";
	if (username==null) username="";
	if (code==null) code="";
	if (createdstring==null) createdstring="0";
	if (priceminstring==null) priceminstring="";
	if (pricemaxstring==null) pricemaxstring="";
	if (rareoldstring==null) rareoldstring="false";
	if (message==null) message="";
	try {
		pricemin=Float.valueOf("0"+priceminstring).floatValue();
		pricemax=Float.valueOf("0"+pricemaxstring).floatValue();
	} catch (Exception exc) {
		exc.printStackTrace();
		message="Price range values are incorrect. Please enter a price like '1500,00'";
	}
	int created=Integer.parseInt(createdstring);
	if (name==null) name="";
	if (vintage==null) vintage="";
	message="";
	if (!username.equals("")){
		if (Webroutines.validUserCode(username,code)){
	ArrayList<Wine> wineset = Wijnzoeker.getSearchResults(username,"","createdate DESC",200);
	Webroutines.logWebAction("RSS","Username: "+username,request.getRemoteAddr(),  request.getHeader("referer"), name,vintage, created,pricemin, pricemax, country, false, "", "", "", "",0.0);
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	for (int i=0;i<wineset.size();i++){
		data=wineset.get(i).Shopname+": "+wineset.get(i).Name+" "+" "+Webroutines.formatSizecompact(wineset.get(i).Size).replaceAll("&nbsp;"," ")+" "+wineset.get(i).Vintage+" &#8364; " + format.format(wineset.get(i).PriceEuroEx);
		data=Spider.escape(data);
		out.println("<item>");
		out.println("<title>"+ data+"</title>");
		out.println("<link>https://www.vinopedia.com/link.jsp?wineid="+wineset.get(i).Id+"&amp;shopid="+wineset.get(i).ShopId+"</link>");
		out.println("<description>"+ data+"</description>");
		out.println("</item>");
	}
		} else {
		out.println("<item>");
		out.println("<title>Incorrect username/code. </title>");
		out.println("<link>https://www.vinopedia.com/settings</link>");
		out.println("<description>Please verify the RSS link in your settings </description>");
		out.println("</item>");
		}
	} else {
		Wineset wineset = new Wineset(name,vintage, created,pricemin, pricemax, new Float(0),country,rareold, "", "createdate DESC",0,200);
		Webroutines.logWebAction("RSS",request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), name,vintage, created,pricemin, pricemax, country, false, "", "", "", "",0.0);
		NumberFormat format  = new DecimalFormat("#,##0.00");	
		for (int i=0;i<wineset.Wine.length;i++){
		data=wineset.Wine[i].Shopname+": "+wineset.Wine[i].Name+" "+Webroutines.formatSize(wineset.Wine[i].Size)+" "+wineset.Wine[i].Vintage+" &#8364; " + format.format(wineset.Wine[i].PriceEuroEx);
		data=Spider.escape(data);
		out.println("<item>");
		out.println("<title>"+ data+"</title>");
		out.println("<link>https://www.vinopedia.com/link.jsp?wineid="+wineset.Wine[i].Id+"&amp;shopid="+wineset.Wine[i].ShopId+"</link>");
		out.println("<description>"+ data+"</description>");
		out.println("</item>");
		}
	}
%></channel></rss><%}%>
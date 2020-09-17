
<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/index.jsp");
    return;
  }
%>


<%@ page 
	import = "java.text.*"
	import = "com.freewinesearcher.online.Search"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Searchset"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.Webactionlogger"
%>

<%
	session.removeAttribute("image");	
	int shopid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("shopid")));
	int partnerid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("partnerid")));
	String yearstring=Webroutines.filterUserInput(request.getParameter("year"));
	String monthstring=Webroutines.filterUserInput(request.getParameter("month"));
	String order=Webroutines.filterUserInput(request.getParameter("sort"));
	if (yearstring==null||yearstring.equals("")) yearstring="";	
	if (monthstring==null||monthstring.equals("")) monthstring="";	
	if (order==null||order.equals("")) order="";	
	int year=2006;
	int month=1;
	try{
		year=Integer.parseInt(yearstring);
		month=Integer.parseInt(monthstring);
	} catch (Exception e){}
%>
<html>
<head>
<title>Personal page for <%=Webroutines.getShopNameFromShopId(shopid,"")%></title>
</head>
<body>
<%@ include file="/header.jsp" %>

<%
	Webactionlogger logger;
	logger=new Webactionlogger("Clickdetails "+year+"-"+month+" shop "+shopid,request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	logger.logaction();
	
%>

Details of clicked links in month <%=month%>, <%=year%>
<br/><br/>
<% out.print(Webroutines.getDetailedClickOverview(partnerid,shopid,year,month,order)) ; %>
<br/>
<a href='<%=response.encodeUrl("index.jsp")%>'>Back to overview</a>	
</body>
</html>

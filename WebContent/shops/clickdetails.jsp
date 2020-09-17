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
	import = "com.freewinesearcher.online.Auditlogger"
	import = "com.freewinesearcher.online.Webactionlogger"
%>

<%
	session.removeAttribute("image");	
	int shopid=Webroutines.getShopFromUserId(request.getRemoteUser());
	int partnerid=Webroutines.getPartnerFromUserId(request.getRemoteUser());
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
<title>Click details for <%=Webroutines.getShopNameFromShopId(shopid,"")%></title>
	<%PageHandler p=PageHandler.getInstance(request,response,"Click details");%>
	<%@ include file="/header2.jsp" %>
	</head><body>
	<%Auditlogger al=new Auditlogger(request);
		al.setAction("Click details");
		al.run();
	
	 %>
	 <%@ include file="/snippets/textpagenosearch.jsp" %>
Details of clicked links in month <%=month%>, <%=year%>
<br/><br/>
<% out.print(Webroutines.getDetailedClickOverview(partnerid,shopid,year,month,order)) ; %>
<br/>
<a href='<%=response.encodeUrl("index.jsp")%>'>Back to overview</a>	
<%@ include file="snippets/footer.jsp" %>
</body>
</html>

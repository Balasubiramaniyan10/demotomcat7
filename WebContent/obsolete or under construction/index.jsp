
<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/logout");
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
	import = "com.freewinesearcher.online.Auditlogger"
%>

<%	PageHandler p=PageHandler.getInstance(request,response,"Pageload Shop index");
	Auditlogger al=new Auditlogger(request);
	al.setAction("Shop index");
	al.setObjecttype("Shop");
	al.logaction();
	session.removeAttribute("image");	
	
%>
<html>
<head>
<title>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></title>
<%@ include file="/header2.jsp" %>
</head>
<body>

<%@ include file="/snippets/textpagenosearch.jsp" %>
<%
	
	Webactionlogger logger;
	logger=new Webactionlogger("Shop overview",request.getServletPath(),al.ip, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	logger.logaction();
%>

<%if (al.partnerid==0) {
	out.write("Your account is not linked to any shop or partner. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
} else { %>
<h3>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></h4> 
<h4>Change settings</h4>
<a href='admanagement.jsp'>Click here</a> to change the settings for sponsored links and banners.<br/><br/>
<h4>Overview of sponsored clicks per month</h4><br/>
This table shows a summary for each month of the sponsored links that were clicked and the  banners that were clicked. Sponsored links are only shown if your company is listed as a retailer.<br/>
<% out.print(Webroutines.getClickOverview(al.partnerid,al.shopid)) ; %>
<br/>
Click on a month to see a detailed list of all clicks and the wines they referred to.
<%} %>	
<%@ include file="snippets/footer.jsp" %> 
</body>
</html>


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
%>

<%
	session.removeAttribute("image");	
	int shopid=Webroutines.getShopFromUserId(request.getRemoteUser());
	int partnerid=Webroutines.getPartnerFromUserId(request.getRemoteUser());
%>
<html>
<head>
<title>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(partnerid)%></title>
</head>
<body>
<%@ include file="/header.jsp" %>
<%
	Webactionlogger logger;
	logger=new Webactionlogger("Shop overview",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	logger.logaction();
	
%>

<%if (partnerid==0) {
	out.write("Your account is not linked to any shop or partner. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
} else { %>
<h3>Advertisement overview</h4> 
<h4>Change settings</h4>
<a href='admanagement.jsp'>Click here</a> to change the settings for sponsored links and banners.<br/><br/>
<h4>Overview of sponsored clicks per month</h4><br/>
This table shows a summary for each month of the sponsored links that were clicked and the  banners that were clicked. Sponsored links are only shown if your company is listed as a retailer.<br/>
<% out.print(Webroutines.getClickOverview()) ; %>
<br/>
Click on a month to see a detailed list of all clicks and the wines they referred to.
<%} %>	
</body>
</html>

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

<%
	session.removeAttribute("image");	
	int shopid=Webroutines.getShopFromUserId(request.getRemoteUser());
	int partnerid=Webroutines.getPartnerFromUserId(request.getRemoteUser());
	PageHandler p=PageHandler.getInstance(request,response,"Winecheck shop "+shopid);
	%><html>
	<head>
<title>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(partnerid)%></title>
	<%@ include file="/header2.jsp" %>
</head>
<body>
<%
	Auditlogger al=new Auditlogger(request);
	al.setPartnerid(partnerid);	
	al.setShopid(shopid);
	al.setUserid(request.getRemoteUser());
	al.setAction("Shop index");
	al.setObjecttype("Shop");
	al.run();
	
%>

<%if (partnerid==0) {
	out.write("Your account is not linked to any shop or partner. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
} else { %>
<h3>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(partnerid)%></h3> 
<h4>Wine overview</h4>
<%=Webroutines.getShopWineOverview(shopid,"") %>
<%} %>
<%@ include file="snippets/footer.jsp" %>
</body>
</html>

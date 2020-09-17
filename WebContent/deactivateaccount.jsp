<html>
<head>
<title>
Account deactivation
</title>
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wineset"
%>
<%	String username=Webroutines.filterUserInput(request.getParameter("username"));
	String email=request.getParameter("email");
	String activationcode=Webroutines.filterUserInput(request.getParameter("activationcode"));
	if (username==null) username="";
	if (activationcode==null) activationcode="";

		
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Publishers");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<%	String message=Webroutines.deactivateAccount(username,email,activationcode);
	if (message.equals("Success")){
		out.println("<br/><br/><h4>PriceAlerts deactivated</h4>The PriceAlerts for "+email+" were deactivated. If you wish to activate them again, or change your search criteria, click <a href='/settings/index.jsp'>here</a>.");
	} else if (message.equals("Unknown error")){
		out.println("We're sorry, we could not deactivate your account. ");
	} else {
		out.println("Ooops, something went wrong here... Please try again later. ");
	
	}
%>	

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>

<%	String message=Webroutines.deactivateAccount(username,email,activationcode);
	if (message.equals("Success")){
		out.println("<br/><br/><h4>PriceAlerts deactivated</h4>The PriceAlerts for "+email+" were deactivated. If you wish to activate them again, or change your search criteria, click <a href='/settings/index.jsp'>here</a>.");
	} else if (message.equals("Unknown error")){
		out.println("We're sorry, we could not deactivate your account. ");
	} else {
		out.println("Ooops, something went wrong here... Please try again later. ");
	
	}
%>	

<% } %>

</body> 
</html>
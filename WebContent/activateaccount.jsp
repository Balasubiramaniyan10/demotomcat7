<html>
<head>
<title>
Account activation
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Activate Account");%>
<% request.setAttribute("numberofimages","0"); %>
<jsp:include page="/header2.jsp" />

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
	String message=Webroutines.activateAccount(username,email,activationcode);
%>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<%	if (message.equals("Success")){
		out.println("<br/><br/><h4>Welcome !</h4>Your account is activated now. Please <a href='/settings/index.jsp'>log in</a> to access scheduled searches.");
	} else if (message.equals("Unknown error")){
		out.println("We're sorry, we could not activate your account. Please activate your account again using the link in the email we sent you.");
	} else {
		out.println("Ooops, something went wrong here... Please try again later. Errormessage=\""+message+"\".");
	
	}
%>	

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
</body> 
</html>
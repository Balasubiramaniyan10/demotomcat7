<html>
<head>
<title>
Account activation
</title>
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%	String username=Webroutines.filterUserInput(request.getParameter("username"));
	if (username==null) username="";
	String message;
 
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Send Activation mail");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<%	
if (Webroutines.getEmail(username).split(":").length>1){
	String email=Webroutines.getEmail(username).split(":")[1];
	message=Webroutines.sendActivationMail(username,email,response.encodeURL(email));
	
	if (message.equals("Success")){
		out.println("<h4>Activation mail was sent</h4>OK! You should receive a confirmation email shortly. Please use the link in this email to activate your account.");
	} else {
		out.println("<h4>Oooops...</h4>We're sorry, but something went wrong while sending you the activation email. The error returned was \""+message+"\". If you think this is a problem on our side, leave us a message at the <a href='/forum'>forum</a>.");
	}
	} else {
		out.println("<h4>Oooops...</h4>We're sorry, but something went wrong while sending you the activation email.");
		
	}
%>		

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
<br/><br/>
<%	if (Webroutines.getEmail(username).split(":").length>1){
	String email=Webroutines.getEmail(username).split(":")[1];
	message=Webroutines.sendActivationMail(username,email,response.encodeURL(email));
	
	if (message.equals("Success")){
		out.println("<h4>Activation mail was sent</h4>OK! You should receive a confirmation email shortly. Please use the link in this email to activate your account.");
	} else {
		out.println("<h4>Oooops...</h4>We're sorry, but something went wrong while sending you the activation email. The error returned was \""+message+"\". If you think this is a problem on our side, leave us a message at the <a href='/forum'>forum</a>.");
	}
} else {
	out.println("<h4>Oooops...</h4>We're sorry, but something went wrong while sending you the activation email.");
	
}
	
%>		
<%} %>
</table>
</body> 
</html>
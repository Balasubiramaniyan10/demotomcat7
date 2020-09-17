<html>
<head>
<title>
Account activation
</title>
<jsp:include page="/header.jsp" />
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wineset"
%>
<%	String username=Webroutines.filterUserInput(request.getParameter("username"));
	String password=request.getParameter("password");
	String firstname = Webroutines.filterUserInput(request.getParameter("firstname"));
	String lastname = Webroutines.filterUserInput(request.getParameter("lastname"));
	String email = Webroutines.filterUserInput(request.getParameter("email"));
	if (username==null) username="0";
	if (password==null) password="";
	if (firstname==null) firstname="";
	if (lastname==null) lastname="";
	if (email==null) email="";
	String message="";

%>
<br/><br/>
<%	//message=Webroutines.adduser(username,password,firstname,lastname,email);
	
	if (message.equals("Success")){
		out.println("OK! You should receive a confirmation email shortly. Please use the link in this email to activate you're account.");
	} else {
		if (message.equals("User already exists")){
			out.println("We're sorry, that user account already exists. Please try a different one.");
		} else {
			if (message.equals("Unknown error")){
				out.println("Whooops! Something went wrong while saving your information. Please try again.");
			} else {
				out.println("Whooops! Something went wrong here. Please try again.");
			}
		}
	}
%>		

</table>
</body> 
</html>
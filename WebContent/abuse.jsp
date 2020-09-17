	<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Emailer"
%>
<%	response.setStatus(403); %>
<% 	String name = request.getParameter("name");
	String email = request.getParameter("email");
	String site = request.getParameter("site");
	String message = request.getParameter("message");
	if (name==null) name="";
	if (email==null) email="";
	if (site==null) site="";
	if (message==null) message="";
	int captcharesult=2;
%><html>
<head>
<title>
403 - Access Denied
</title>
<script src='https://www.google.com/recaptcha/api.js'></script>
</head>
<body><% 

	String ipaddress="";
	if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
		ipaddress = request.getRemoteAddr();
	} else {
	    ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
	}
	boolean showform=true;
	Webroutines.logWebAction("Abuse message",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
	Webroutines.logUserAgentAbuse(request.getHeader("User-Agent"),ipaddress);
	if ("sent".equals(request.getParameter("formsent"))){
		// Check form
		// Check captcha
		captcharesult=Webroutines.checkCaptcha(request);
		if (captcharesult==1){
			//Webroutines.logWebAction("Message sent",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
			Emailer emailer=new Emailer();
			String HTMLmessage="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/TR/html4/loose.dtd\">";
			HTMLmessage=HTMLmessage+"<html><body>Name: <br/>"+name+"<br/>";
			HTMLmessage=HTMLmessage+"Email: <br/>"+email+"<br/>";
			if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
				ipaddress = request.getRemoteAddr();
			} else {
				ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			
			HTMLmessage=HTMLmessage+"IP: <br/>"+ipaddress+"<br/>";
			HTMLmessage=HTMLmessage+"Message: <br/>"+message+"<br/>";
			HTMLmessage=HTMLmessage+"</body></html>";
							
			if (emailer.sendEmail("site@vinopedia.com","Jasper.Hammink@vinopedia.com","Abuse Message on web site",HTMLmessage)){
				out.write("<br/><br/>Your message has bent sent.<br/>");
				showform=false;
			} else {
				out.write("<br/><br/>Sorry... Something went wrong while trying to send your message. Please try again later.<br/>");
			
			} 

		}
		
	}
	
	
	if (showform){
%>

<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">
<h4>IP address blocked for vinopedia</h4>
<FORM ACTION="abuse.jsp" METHOD="POST">
Hi,<br/><br/>We noticed an unusually high a mount of traffic coming from your computer. To prevent Denial of Service attacks, we have blocked all traffic coming from your computer. If you are using this site legitimately or are running a search engine, please let us know the reason why the amount of searches is so high. We will then unblock access to the site. Please note that automated retrieval of pages from this site is prohibited according to our terms of use. However, if you are looking for ways to integrate our content with your own site, just let us know: we are open to that and provide ways of doing so automatically.<br/><br/>
<br/>
<br/>
    <TABLE>
		<TR><TD>Name</TD><TD><INPUT TYPE="TEXT" NAME="name" size=50></TD></TR>
		<TR><TD>Email</TD><TD><INPUT TYPE="TEXT" NAME="email"  size=50></TD></TR>
		<TR><TD valign=top>Reason for high volume access</TD><TD><TEXTAREA rows=6 cols=60  NAME="message"></TEXTAREA></TD></TR>
		<TR><TD>Anti spam check</TD><TD><%=Webroutines.showCaptchaHTML(request) %><%
		if (captcharesult==0) out.write("<br/>Wrong words were entered, please try again.");
		%></TD></TR><TR><TD></TD><TD><INPUT TYPE="SUBMIT" VALUE="Send!"></TD></TR>
	</TABLE>
<input type="hidden" name="formsent" value="sent"/>
	
	</FORM>
</TD><TD class="right">
	</TD></TR>
</TABLE>
<% } %>
</body> 
</html>
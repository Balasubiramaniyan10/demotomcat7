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
<% 	String name = request.getParameter("name");
	String email = request.getParameter("email");
	String site = request.getParameter("site");
	String message = request.getParameter("message");
	if (name==null) name="";
	if (email==null) email="";
	if (site==null) site="";
	if (message==null) message="";
%>
<html>
<head>
<title>
Contact
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Contact");%>
<jsp:include page="/header2.jsp" />
<script src='https://www.google.com/recaptcha/api.js'></script>
</head>
<body>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){ %>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<%    	boolean showform=true;
    	int captcharesult=2;
		if ("sent".equals(request.getParameter("formsent"))){
    		// Check form
    		// Check captcha
    		captcharesult=Webroutines.checkCaptcha(request);
    		if (captcharesult==1||"nocheck".equals(request.getParameter("captcha"))){
    			PageHandler.getInstance(request,response,"Message sent");
    			//Webroutines.logWebAction("Message sent",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
    			Emailer emailer=new Emailer();
    			String HTMLmessage="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/TR/html4/loose.dtd\">";
    			HTMLmessage=HTMLmessage+"<html><body>Name: <br/>"+name+"<br/>";
    			HTMLmessage=HTMLmessage+"<html><body>Email: <br/>"+email+"<br/>";
    			HTMLmessage=HTMLmessage+"<html><body>Website: <br/>"+site+"<br/>";
    			HTMLmessage=HTMLmessage+"IP: <br/>"+p.ipaddress+"<br/>";
    			HTMLmessage=HTMLmessage+"<html><body>Message: <br/>"+message+"<br/>";
    			HTMLmessage=HTMLmessage+"</body></html>";
    			/* emailer.replyto=email; */			
    			if (emailer.sendEmail("do_not_reply@vinopedia.com","management@vinopedia.com","Message on Vinopedia.com from "+email,HTMLmessage)){
    				out.write("<br/><br/>Thanks! Your message has bent sent!<br/>");
    				showform=false;
    				//emailer.sendEmail("site@vinopedia.com","jeroen@vinopedia.com","Message on web site",HTMLmessage);
    			} else {
    				out.write("<br/><br/><b style='color:red'>Sorry... Something went wrong while trying to send your message. Please try again later.</b><br/>");
    			
    			} 

    		}
    		
    	}
		
		
		if (showform){
		//Webroutines.logWebAction("Contact",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);

%>

<h1>Contact us</h1>
<FORM ACTION="contact.jsp" METHOD="POST">
What do you think of our site? Did you experience any problems? Got any suggestions or comments? We love to hear from you! If you are a selling wines and would like to get listed on Vinopedia.com, please <a href='retailers.jsp'>sign up here</a>.<br/><br/>
Note: <b style='color:red'>Vinopedia is a search engine for wine, we do not sell wine</b> directly or negotiate deals. If you see a wine on our site that you would like to buy or that you need more information about, please contact the seller/store directly. We will not answer any inquiries about price/availability/shipment.<br/>
<br/>
    <TABLE>
		<TR><TD>Name</TD><TD><INPUT TYPE="TEXT" NAME="name" value="<%=name %>" size=70></TD></TR>
		<TR><TD>Email</TD><TD><INPUT TYPE="TEXT" NAME="email" value="<%=email %>"  size=70></TD></TR>
		<TR><TD>Your Website URL</TD><TD><INPUT TYPE="TEXT" NAME="site" value="<%=site %>"  size=70></TD></TR>
		<TR><TD valign='top'>Message</TD><TD><TEXTAREA rows='6' cols='60'  NAME="message" ><%=message.replace("<","&lt;").replace(">","&gt;")%></TEXTAREA></TD></TR>
		<TR><TD>Anti spam check</TD><TD><%=Webroutines.showCaptchaHTML(request) %><%
		if (captcharesult==0) out.write("<br/>Wrong words were entered, please try again.");
		%></TD></TR>
		<TR><TD></TD><TD><INPUT TYPE="SUBMIT" VALUE="Send!"></TD></TR>
	</TABLE>
	<input type='hidden' name='formsent' value='sent'/>
	</FORM>
	<%} %>

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
</body> 
</html>
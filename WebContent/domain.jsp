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
Vinopedia for sale
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Contact");%>
<jsp:include page="/header2.jsp" />
<script src='https://www.google.com/recaptcha/api.js'></script>
</head>
<body>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
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
    			if (emailer.sendEmail("do_not_reply@vinopedia.com","management@vinopedia.com","Vinopedia.com for sale "+email,HTMLmessage)){
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

<h1>Are you interested in buying Vinopedia?</h1>
<FORM ACTION="domain.jsp" METHOD="POST">
Vinopedia.com has been a leading search engine for wine for the past 8 years and is now for sale. Some key figures and features:<br/>
<ul>
<li>The site gets around 3.000.000 unique visitors a year</li>
<li>Obviously our visitors are highly interested in buying wine and have an above average income</li>
<li>The site is highly automated. All prices are automatically refreshed daily by accessing the web shop of participating wine stores</li>
<li>A single person is able to maintain the web site and add new stores to it</li>
<li>We currently index 1.6 million wine offers world wide</li>
<li>The site features a wine recognition system that can determine the exact wine from the description on a web shop</li>
</ul> <br/>
<h2>Interested?</h2>
We are selling the vinopedia.com domain name, software and all wine and shop data. If you are interested, please leave a message below and we will contact you at our earliest convenience.
<br/>
<br/>
    <TABLE>
		<TR><TD>Name</TD><TD><INPUT TYPE="TEXT" NAME="name" value="<%=name %>" size=70></TD></TR>
		<TR><TD>Email</TD><TD><INPUT TYPE="TEXT" NAME="email" value="<%=email %>"  size=70></TD></TR>
		<TR><TD valign='top'>Contact details</TD><TD><TEXTAREA rows='6' cols='60'  NAME="message" ><%=message.replace("<","&lt;").replace(">","&gt;")%></TEXTAREA></TD></TR>
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
</div>
</body> 
</html>
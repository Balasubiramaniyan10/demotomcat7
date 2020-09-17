	<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.PageHandler"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Emailer"
	import = "com.freewinesearcher.common.Dbutil"
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
	String target=PageHandler.getInstance(request,response).URLbeforebotcheck;
	if (target==null||target.equals("")) target=(String)request.getParameter("targeturl");
	if (target==null||target.equals("")||target.contains("check.jsp")) target="https://www.vinopedia.com";
	if (target==null||target.equals("")||target.contains("checkimage")) target="https://www.vinopedia.com";
%>
<html>
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
	Webroutines.logWebAction("Are you human challenge",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
	Webroutines.logUserAgentAbuse(request.getHeader("User-Agent"),ipaddress);
	if ("sent".equals(request.getParameter("formsent"))){
		// Check form
		// Check captcha
		captcharesult=Webroutines.checkCaptcha(request);
		if (captcharesult==1){
			Webroutines.logWebAction("Are you human solved",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
			session.setAttribute("imageverified",true);
			PageHandler.getInstance(request, response).botstatus=0;
			response.setStatus(302);
			response.setHeader( "Location", target);
			response.setHeader( "Connection", "close" );
			return; 

		}
		
	}
	
	
	if (showform){
%>

<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">
<h4>No access to vinopedia</h4>
<FORM ACTION="areyouhuman.jsp" METHOD="POST">
<input type='hidden' name='targeturl' value='<%=target %>'/>
Hi,<br/><br/>We recently experienced some attacks on the vinopedia website. We had to take some measures to protect ourselves, and somehow your visit to vinopedia triggered this protection (this does not mean there is a problem with your computer though). But as a result, you do not see the website but this message.<br/><br/>
<noscript>Javascript is not working in your browser. This is needed for our site to work properly and is probably the reason this message shows up. Please enable javascript and try loading vinopedia again.<br/></noscript>
<%
String currency=null;
if (request.getCookies()!=null) for (Cookie cookie:request.getCookies()) {
	if (cookie.getName().equals("currency")&&(cookie.getPath()==null||cookie.getPath().equals("/"))) currency=cookie.getValue();
}
if (currency==null) {%>
Please make sure you allow cookies for our site. This is needed for our site to work properly and is probably the reason this message shows up.<br/><br/>
<%} %>
<script type='text/javascript'>document.write('If you solve the anti-spam check you will have access again to the full website.<br/><br/>');</script>
<%=Webroutines.showCaptchaHTML(request) %><%
		if (captcharesult==0) out.write("<br/>Wrong words were entered, please try again.");
		%><INPUT TYPE="SUBMIT" VALUE="Send!">
<input type="hidden" name="formsent" value="sent"/>
	
	</FORM>
</TD><TD class="right">
	</TD></TR>
</TABLE>
<% } %>
</body> 
</html>
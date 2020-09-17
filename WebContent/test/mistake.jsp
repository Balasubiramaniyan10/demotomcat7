<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"		
	import = "com.freewinesearcher.batch.Emailer"
%>
<html>
<head>
<title>
Report a mistake
</title>
<jsp:include page="/header.jsp" />
<%
	int wineid=0;
wineid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("wineid")));
int knownwineid=0;
knownwineid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("knownwineid")));
String message = request.getParameter("message");



String wine=Dbutil.readValueFromDB("select * from wines where id="+wineid+";","name"); 
String knownwine=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";","wine"); 
String fulltext=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";","fulltextsearch"); 
String ipaddress="";
	if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
		ipaddress = request.getRemoteAddr();
	} else {
	    ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
	}

	if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
		out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
    	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
	} else {
    	Webroutines.logWebAction("Report Mistake",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
%>
<%
	if ("true".equals(request.getParameter("send"))){
	Emailer emailer=new Emailer();
	String HTMLmessage="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/TR/html4/loose.dtd\">";
	String email=Dbutil.readValueFromDB("Select * from jforum_users where username='"+request.getRemoteUser()+"';","user_email");
	if (email==null||!email.contains("@")) email="site@vinopedia.com"; 
	HTMLmessage=HTMLmessage+"<html><body>";
	HTMLmessage=HTMLmessage+"Username: <br/>"+request.getRemoteUser()+"<br/>";
	HTMLmessage=HTMLmessage+"Email: <br/>"+email+"<br/>";
	HTMLmessage=HTMLmessage+"Wineid: <br/>"+wineid+"<br/>";
	HTMLmessage=HTMLmessage+"Wine name: <br/>"+wine+"<br/>";
	HTMLmessage=HTMLmessage+"Knownwineid: <br/>"+knownwineid+"<br/>";
	HTMLmessage=HTMLmessage+"Knownwine name: <br/>"+knownwine+"<br/>";
	HTMLmessage=HTMLmessage+"Mistake report: <br/>"+message+"<br/>";
	HTMLmessage=HTMLmessage+"</body></html>";
	
	if (emailer.sendEmail(email,"Jasper.Hammink@vinopedia.com","FWS Mistake report",HTMLmessage)){
	out.write("<br/><br/>Thanks! Your message has bent sent!<br/>");
	} else {
	out.write("<br/><br/>Sorry... Something went wrong while trying to send your message. Please try again later.<br/>");
	
	} 
} else {
%>
<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">
<h4>Report a mistake</h4>
<FORM ACTION="mistake.jsp" METHOD="POST">
Please fill out all details necessary to determine the nature of the problem. If the wrong wine was recognized, which wine is it really?<br/>
<br/>
<table>
<tr><td>Wine description: </td><td><%=wine%></td></tr>
<tr><td>Recognized as wine: </td><td><%=knownwine%></td></tr>
<tr><td>Recognition pattern: </td><td><%=fulltext.replace("+","") %></td></tr></table>
<br/>Problem description:<br/><TEXTAREA rows=6 cols=60  name="message">
</TEXTAREA>
<input type="hidden" name="send" value="true"/>		
<input type="hidden" name="wineid" value="<%=wineid %>"/>		
<input type="hidden" name="knownwineid" value="<%=knownwineid %>"/>		
<input type="hidden" name="username" value="<%=request.getRemoteUser() %>"/>		
<br/>		
<INPUT TYPE="SUBMIT" VALUE="Send!">
	</FORM>
</TD><TD class="right">
		<script type="text/javascript"><!--
		google_ad_client = "pub-5573504203886586";
		google_ad_width = 120;
		google_ad_height = 600;
		google_ad_format = "120x600_as";
		google_ad_type = "text_image";
		google_ad_channel ="";
		google_color_border = "336699";
		google_color_bg = "FFFFFF";
		google_color_link = "0000FF";
		google_color_url = "008000";
		google_color_text = "000000";
		//--></script>
		<script type="text/javascript"
		  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
		</script>
		
	</TD></TR>
</TABLE>
<jsp:include page="/footer.jsp" />
<%}
}%>	
</div>
</body> 
</html>
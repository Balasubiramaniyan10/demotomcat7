<html>
<head>
<title>Test a shop</title>
</head>
<body bgcolor="white">
<jsp:include page="/admin/adminlinks.jsp" />
<%@ page 
	import = "org.apache.log4j.net.*"
	import = "javax.mail.internet.*"
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Dbutil"
	
%>
Hello
<%
	try{
SMTPAppender SMTPapp=new SMTPAppender();
SMTPapp.setSMTPHost("www.vinopedia.com");
SMTPapp.setFrom("do_not_reply@vinopedia.com");
SMTPapp.setTo("jasper.hammink@vinopedia.com");
SMTPapp.setSubject("Error mesage from vinopedia");
out.write(SMTPapp.getSMTPHost());
}catch (Exception e){
	Dbutil.logger.error("bugtest.jsp error:",e);
}
%>
</body></html>
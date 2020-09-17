<html>

<head>
<%@ page 
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%>
<title>
The Know Wines List Updater
</title>
</head>
<body>
<%
	String sql=request.getParameter("sql");	
	Dbutil.executeQuery(sql);
	//Dbutil.logger.info(sql);
	//out.print(sql);
%>
	<script type="text/javascript">
	if(navigator.appName=="Microsoft Internet Explorer") {
	this.focus();self.opener = this;self.close(); }
	else { window.open('','_parent',''); window.close(); }
	window.opener='x';window.close();</script>



</body> 
</html>
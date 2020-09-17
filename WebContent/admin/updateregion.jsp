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
	import = "com.freewinesearcher.common.Region"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%>
<title>
The Region Updater
</title>
</head>
<body>
<%	String region=request.getParameter("region");	
	String replacement=request.getParameter("replacement");	
	Region.replaceregion(region,replacement);
	%>
	<script type="text/javascript">
	if(navigator.appName=="Microsoft Internet Explorer") {
	this.focus();self.opener = this;self.close(); }
	else { window.open('','_parent',''); window.close(); }
	window.opener='x';window.close();</script>



</body> 
</html>
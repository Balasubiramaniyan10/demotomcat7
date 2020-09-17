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
Set a shop comment
</title>
</head>
<body>
<%
String shopid=request.getParameter("shopid");	
String wsid=request.getParameter("wsid");	
String comment=request.getParameter("comment");	
if (comment==null) comment="";
if (wsid!=null){
	Dbutil.executeQuery("update wsshops set comment='"+Spider.SQLEscape(comment)+"' where wsid="+wsid);
}
System.out.println(shopid);
if (shopid!=null&&!"0".equals(shopid)){
	Dbutil.executeQuery("update shops set comment='"+Spider.SQLEscape(comment)+"' where id="+shopid);


	//Dbutil.logger.info(sql);
	//out.print(sql);
%>
	<script type="text/javascript">
	if(navigator.appName=="Microsoft Internet Explorer") {
	this.focus();self.opener = this;self.close(); }
	else { window.open('','_parent',''); window.close(); }
	window.opener='x';window.close();</script>

<%} else{ %>
Before you set a shop comment, you need to retrieve the shop information from the same page, and only then you can change the shop comment
<%} %>

</body> 
</html>
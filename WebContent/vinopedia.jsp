<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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

<%@page import="com.freewinesearcher.online.PageHandler"%><html>
<head>
<title>
Coming soon: vinopedia
</title>
<% PageHandler p=PageHandler.getInstance(request,response,"Vinopedia aankondiging");%>
</head>
<body style='width:100%'>
<h1 style='padding-top:200px;text-align:center;font-face:Georgia'>Coming soon:</h1>
<div style="text-align: center;"><img src='/images/vinopedia.gif' /'/></div>
</body> 
</html>
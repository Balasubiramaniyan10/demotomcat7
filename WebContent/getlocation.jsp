<% response.setHeader("Cache-Control","max-age=3600");
		response.setDateHeader ("Expires", 0); %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@page import="com.freewinesearcher.common.Knownwine"%>
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.PageHandler"
%><!DOCTYPE html> 
<html> 
	<head> 
	<meta charset="utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1"/>
	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0b1/jquery.mobile-1.0b1.min.css" />
	<link rel="stylesheet" href="/mobile/mobile.css" />
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.1.min.js"></script>
	<script type="text/javascript" src="http://code.jquery.com/mobile/1.0b1/jquery.mobile-1.0b1.min.js"></script>


<%
	session = request.getSession(true); 
	PageHandler p=PageHandler.getInstance(request,response,"Mobile Search");
	p.searchdata.sponsoredresults=true;
	p.searchpage="/m.jsp";
	 	%><title>Location</title><%
	session.setAttribute("winename",p.searchdata.getName());
%>

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-1788182-2', 'auto');
<%=PageHandler.getInstance(request,response).asyncGAtracking%>
ga('send', 'pageview');
</script>
<!-- End Google Analytics -->

<%
if (p.s.wineset.canonicallink!=null&&!p.s.wineset.canonicallink.equals("")){ %><link rel="canonical" href="<%=p.s.wineset.canonicallink.replaceAll("/wine","/mwine")%>" /><%} %>
<script type="text/javascript">

</script>
</head> 
<body><div data-role="page" data-theme="a" class="vp" id="prices">
<%@ include file="/mobile/header.jsp" %>
<div data-role="content">	
<span id="status">Checking your location...</span>
</div>
<%@ include file="/mobile/footer.jsp" %>
</div><!-- /page -->
</body>
</html>

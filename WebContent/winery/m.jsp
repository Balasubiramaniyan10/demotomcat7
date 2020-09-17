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
	import="com.freewinesearcher.online.Producer"
	import="com.freewinesearcher.online.Producerinfo"
	import="com.freewinesearcher.common.Configuration"
%><%
ArrayList<String> countries = Webroutines.getCountries();
Producer producer=new Producer(request.getParameter("winery")); 
producer.mobile=true;
producer.newmobile=true;
String text=producer.getInfo(PageHandler.getInstance(request,response)); 
session = request.getSession(true); 

%><!DOCTYPE html> 
<html> 
	<head> 
	<%@ include file="/mobile/includes.jsp" %> 
	<title>Winery information: <%=(producer.name.replaceAll("&","&amp;"))%></title>

<%
	NumberFormat format  = new DecimalFormat("#,##0.00");
	PageHandler p=PageHandler.getInstance(request,response,"Winery");
	response.setHeader("Cache-Control","max-age=3600");
	response.setDateHeader ("Expires", 0);
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

</head> 
<body>
<div data-role="page" data-theme="a" class="vp" id="winery">
<%@ include file="/mobile/header.jsp" %>


<div data-role="content">
<%
	out.write(text); 
%></div>
<%@ include file="/mobile/footer.jsp" %>
</div><!-- /page -->
</body>
</html>

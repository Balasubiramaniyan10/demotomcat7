<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd"><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" >
<%@page 
contentType="application/xhtml+xml; charset=UTF-8" 
pageEncoding="UTF-8"
session="true"  
import="java.util.ArrayList"
import="com.freewinesearcher.common.Configuration"
import="java.util.LinkedHashSet"
import="com.freewinesearcher.online.web20.CommunityUpdater"
import="com.freewinesearcher.online.Auditlogger"
import = "com.freewinesearcher.batch.Spider"
import = "com.freewinesearcher.online.Webroutines"	
import="com.freewinesearcher.online.Producer"
import="com.freewinesearcher.online.Producerinfo"
%>
<%	ArrayList<String> countries = Webroutines.getCountries();
	Producer producer=new Producer(request.getParameter("winery")); 
	producer.mobile=true;
	String text=producer.getInfo(PageHandler.getInstance(request,response)); 
%>
<head><meta http-equiv="Content-Type" content="application/xhtml+xml;charset=UTF-8" />
<meta name="viewport" content="width=device-width; initial-scale=1.0; " />

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

<title>
Winery information: <%=(producer.name.replaceAll("&","&amp;"))%>
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<meta name="description" content="<%=producer.generateddescription.replaceAll("&","&amp;")%>" />
<meta name="keywords" content="<%=producer.keywords.replaceAll("&","&amp;") %>" />
<% PageHandler p=PageHandler.getInstance(request,response,"Winery");%>
<%@ include file="/headersmall.jsp" %>
Mobile version | <a href='<%=p.thispage.replaceAll("/mwinery/","/winery/")%>'>Normal version</a><br/>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>


<%
	out.write(text); 
%>
<h2>Find a wine on Vinopedia</h2>
<%@ include file="/snippets/mobilesearchform.jsp" %>
<%@ include file="/snippets/footersmall.jsp" %>
<% } %>
	
</div>
</body> 
</html>
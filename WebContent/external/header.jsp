<% long start=System.currentTimeMillis();
	boolean debuglog=false;
	%><%PageHandler p=PageHandler.getInstance(request,response);
if (p.ipaddress.startsWith("172.20.20.")||p.ipaddress.startsWith("127.0.0.1")) debuglog=true;
if (debuglog) Dbutil.logger.info("start /external/header");
p.searchpage="/index.jsp";
p.createWineset=false;
//if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start processSearchdata"); 
p.processSearchdata(request);
//if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"End processSearchData"); 
//String originaltesturl=(String)request.getAttribute("originalURL");


%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Configuration"
%>

<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>

<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head> 

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-1788182-2', 'auto');
ga('send', 'pageview');
</script>
<!-- End Google Analytics -->

<title>Vinopedia: External page</title>
<%@ include file="/header2.jsp" %>
</head>
<body id='framebody'>
<div id='frameheader' class='logoandsearch'>
<div>
<a href='<%=(session.getAttribute("lasturl")==null?"/index.jsp?keepdata=true":(String)session.getAttribute("lasturl")) %>' target='_top'><img src='/images/logosmall.gif' alt='Home page'/></a>
</div><div class='text'>
You are viewing a page outside of Vinopedia.com. <a href='<%=(session.getAttribute("lasturl")==null?"/index.jsp?keepdata=true":(String)session.getAttribute("lasturl")) %>' target='_top'>Click here to go back to the previous page.</a></div><div class='closeframe text'><a href="<%=request.getParameter("targeturl") %>" target="_top">
Remove frame</a></div>
</div>
<div style="height:2px;width:100%;background-color: #aaaaaa;"></div>


</body>
</html>
<% if (debuglog) Dbutil.logger.info("end /external/header");%>
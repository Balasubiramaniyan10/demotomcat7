<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
	<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%	//Dbutil.logger.info("Start");
	PageHandler p=PageHandler.getInstance(request,response);
	p.createWineset=false;
	p.processSearchdata(request);
	ExternalManager em=new ExternalManager(request,response,searchhistory);
	//Dbutil.logger.info("End");
	
	 
%>
<%@page import="com.freewinesearcher.online.ExternalManager"%><html>
<head><%@ include file="/snippets/jsincludes.jsp" %> 
<script type='text/javascript'>function load(){if ($('#bottom_frame').attr('src')=='') $('#bottom_frame').attr('src','<%=em.url%>');}</script>
</head>
<%@ page 
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.PageHandler"
	import = "com.freewinesearcher.common.Dbutil"
%>

<frameset border="0" frameborder="0" framespacing="0" rows="47px, 100%">
<frame src="/external/header.jsp?keepdata=true&targeturl=<%=Webroutines.URLEncode(em.url) %>"  noresize="noresize" scrolling="no"  onload="window.setTimeout('load();',200);"/>
<frame src="" id="bottom_frame" name="bottom_frame"/>
</frameset><noframes></noframes>
</html> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="com.freewinesearcher.online.ExternalManager"%><html>
<%@ page 
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.PageHandler"
	import = "com.freewinesearcher.common.Dbutil"
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
	<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%	ExternalManager em=new ExternalManager(request,response,searchhistory);
	
	
%>
<frameset border="0" frameborder="0" framespacing="0" rows="147px, 100%">
<frame src="ordertestheader.jsp"  noresize="noresize" scrolling="no"/>
<frame src="http://www.grapy.nl/" id="bottom_frame" name="bottom_frame"/>
</frameset><noframes></noframes>
</html> 
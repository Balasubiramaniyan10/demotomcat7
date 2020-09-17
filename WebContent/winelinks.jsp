<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
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
<%
	session = request.getSession(true);
	PageHandler p=PageHandler.getInstance(request,response,"Link Clicked");
	String source=request.getParameter("source");
	if (source==null) source=(String)session.getAttribute("source");
	if (source==null) source="";
	session.setAttribute("source",source);
	int knownwineid=0;
	int vintage=0;
	try{knownwineid=Integer.parseInt(request.getParameter("knownwineid"));}catch(Exception e){}
	if (knownwineid==0)	try{p.createWineset=false;
	p.processSearchdata(request);
	knownwineid=p.s.wineset.knownwineid;}catch(Exception e){} 
	try{vintage=Integer.parseInt(request.getParameter("vintage"));}catch(Exception e){}
	
	
%>
<frameset border="0" frameborder="0" framespacing="0" rows="42px, 100%" id="winelinksframeset">
<frame src="/winelinksheader.jsp?knownwineid=<%=knownwineid+(vintage>0?"&vintage="+vintage:"")+"&keepdata=true" %>"  noresize="noresize" scrolling="no"/>
<frame src="<%=(source.equals("")?"about:blank":"/loading.jsp")%>" id="bottom_frame" name="bottom_frame"/>
</frameset><noframes></noframes>
</html> 
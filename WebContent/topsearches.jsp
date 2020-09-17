<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<html>
<head>
<title>
Top Searches
</title>
<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<%@ page contentType="text/html; charset=UTF-8" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>vinopedia price quotes for publishers</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Topsearches");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
This is a list of the most looked after wines on this site.<br/><br/>

<% out.print (Webroutines.getTopSearches()); %>	

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>

<br/><br/>
This is a list of the most looked after wines on this site.<br/><br/>

<% out.print (Webroutines.getTopSearches()); %>	
<%} %>
</body> 
</html>
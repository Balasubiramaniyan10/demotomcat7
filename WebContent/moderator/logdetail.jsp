<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
%>
<%String shopid = request.getParameter("shopid");

	%>
<title>
Shop logging details
</title>
<%	String html=Webroutines.getLogDetails(shopid);
	%>
</head>
<body>
<jsp:include page="moderatorlinks.jsp" />
Shop logging details<br/>

<% out.print(html);%>
</body> 
</html>
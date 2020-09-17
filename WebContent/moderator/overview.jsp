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
<%String order = request.getParameter("order");

	%>
<title>
Overview of shops
</title>
<%	String html=Webroutines.getShopOverview(order,false);
	%>
</head>
<body>
<jsp:include page="moderatorlinks.jsp" />
Overview of all shops
<% out.print(html);%>
</body> 
</html>
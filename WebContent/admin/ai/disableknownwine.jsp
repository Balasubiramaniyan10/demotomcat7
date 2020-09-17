<%@ page 
	import = "java.text.*"
	import = "com.freewinesearcher.online.Search"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Searchset"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Auditlogger"
%>

<%	PageHandler p=PageHandler.getInstance(request,response,"Pageload Shop index");
	Auditlogger al=new Auditlogger(request);
	al.setAction("Shop index");
	al.setObjectid(al.shopid+"");
	al.setObjecttype("Shop");
	al.logaction();
	session.removeAttribute("image");	
	
%>
<html>
<head>
<title>Disable knownwines</title>
<%@ include file="/header2.jsp" %>
</head>
<body>
<%@ include file="/snippets/textpage.jsp" %>

<%@ include file="/snippets/textpagefooter.jsp" %> 
</body>
</html>

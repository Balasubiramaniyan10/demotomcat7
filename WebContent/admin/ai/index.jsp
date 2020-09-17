
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.searchasaservice.ai.AiHtmlRefiner"%><html>

<head>
<%@ page 
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Region"
	
	%>
<title>
Ai Tools
</title>
</head>
<body>

<jsp:include page="/header2.jsp" />
<jsp:include page="/snippets/textpagenosearch.jsp" />
<jsp:include page="ailinks.jsp" />
</body> 
</html>
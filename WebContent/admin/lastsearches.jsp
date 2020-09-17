<html><head>
<%@ page 
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "com.freewinesearcher.common.Dbutil"
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
	<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%
	session = request.getSession(true);
%>
<title>
Last searches and links clicked
</title>
</head>
<body>
<%
	int wineid=0;
out.write("Last clicks:<br/>");
for (int i:searchhistory.getClicks()){
	wineid=Dbutil.readIntValueFromDB("select * from logging where id="+i+";","wineid");
	out.write(Dbutil.readValueFromDB("select * from wines where id="+wineid+";","name")+"<br/>");
}
out.write("<br/><br/>Last searches:<br/>");
for (int i:searchhistory.getSearches()){
	out.write(Dbutil.readValueFromDB("select * from logging where id="+i+";","name")+"<br/>");
}
%>
</body> 
</html>
		
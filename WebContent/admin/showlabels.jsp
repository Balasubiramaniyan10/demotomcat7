<html>

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
<%
	int[] list;
	String name = request.getParameter("name");
	if (name==null||name.equals("")) name="Enter name";
	try{
		int id=Integer.parseInt(name);
		if (id>0) {
			list=new int[1];
			list[0]=id;
		} else{
			list=Webroutines.getKnownwineids(name);
		}
	} catch (Exception e){
		list=Webroutines.getKnownwineids(name);
	}
	
%>
<title>
Labels
</title>
</head>
<body>
	<form id="theform" name="theform" action='showlabels.jsp' method='POST'>
Search wines containing: <input type='text' id='name' name='name' value='<%=name%>'><input type='submit' value='Query'/></form>

<%	
for (int id:list){
	out.print(Webroutines.getLabels(id));
}
%>
</body> 
</html>
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
<script type="text/javascript">
function mark(elm){
elm.innerHTML='';
}

</script>
<title>
The Know Wines List editor
</title>
</head>
<body>
<jsp:include page="/admin/adminlinks.jsp" />
<br><a href='taskrunner.jsp?task=analyseKnownWines' target='_blank'>Start Batch job to Analyze Known Wines</a>

<%
	String appellation = request.getParameter("appellation");
	if (appellation==null||appellation.equals("")) appellation="mostmatches";
%>
	<form id="theform" name="theform" action='editknownwineslist.jsp' method='POST'>
Select region: <select name='appellation' onChange="document.getElementById('theform').submit();">
<%
	ArrayList<String> regions=Region.getRegions("All");
out.write("<option value='Select'>Select");

for (String region:regions){
%>
<option value='<%= region%>' <%if (region.equals(appellation)) out.write(" Selected");%>><%=region%>
<%}%>
</select>
</form>
<%
out.print(Knownwines.editKnownWinesHTML(appellation));
%>
</body> 
</html>
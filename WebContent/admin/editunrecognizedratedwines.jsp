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
	
	
%>
<script type="text/javascript">

var g_remoteServer = '/admin/removeterm.jsp?row=';
function removeterm(row,term,ratedwineid){
	script = document.createElement('script');	
	script.src = g_remoteServer+row+"&term="+term+"&ratedwineid="+ratedwineid;	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').innerHTML='';
	document.getElementById('result').appendChild(script);

	}

</script>
<script type="text/javascript">

function testKnownWine(id){
	script = document.createElement('script');	
	script.src = '/admin/testknownwine.jsp?id='+id;
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').appendChild(script);
	}

</script>
<div id='result'></div>
<title>
Edit Unrecognized Rated Wines
</title>
</head>
<jsp:include page="/admin/adminlinks.jsp" />
<% String selectedregion=request.getParameter("region");
	if (selectedregion==null||selectedregion.equals("")){
		selectedregion="Select";
	}
%>
<br><a href='taskrunner.jsp?task=analyseRatedWines' target='_blank'>Analyze rated wines</a>
<%	out.print(Knownwines.editUnrecognizedRatedWinesHTML());
%>
</body> 
</html>
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

var g_remoteServer = 'removeerror.jsp?id=';
function updateValue(id,actie){
	value="";
	
	script = document.createElement('script');	
	if (actie=="fulltext") while (value.indexOf('+')>-1) {
	value=value.replace('+','|');
	}
	script.src = g_remoteServer+id+"&value="+escape(value)+"&actie="+actie;	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').innerHTML='';
	document.getElementById('result').appendChild(script);

	}

</script>
<div id='result'></div>
<title>
The Error Console
</title>
</head>
<jsp:include page="/admin/adminlinks.jsp" />
<a href="javascript:updateValue(0,'handleall')">Delete ALL</a>
<%
	out.write(Dbutil.getErrorHTML(9999));
%>
</body> 
</html>
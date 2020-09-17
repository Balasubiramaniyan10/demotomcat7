
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.searchasaservice.ai.AiHtmlRefiner"%>
<%@page import="com.searchasaservice.ai.Recognizer"%><html>

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
var g_remoteServer = 'updateairecognizermanual.jsp?id=';
function urlescape(str){
	str=escape(str);
	while (str.indexOf('+')>-1){
		str=str.replace("+","%2B")
	}
	return str;
}

function updateValue(id){
	value="";
	var fts=document.getElementById("fulltext"+id).value;
	var regex=document.getElementById("regex"+id).value;
	var regexexcl=document.getElementById("regexexcl"+id).value;
	var script = document.createElement('script');	
	
	script.src = g_remoteServer+id+"&fts="+urlescape(fts)+"&regex="+urlescape(regex)+"&regexexcl="+urlescape(regexexcl);	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').innerHTML='';
	document.getElementById('result').appendChild(script);
	}
</script>
<script type="text/javascript">
var disorenable = '/admin/updatekbknownwine.jsp?id=';
function enableWine(id){
	var script = document.createElement('script');	
	script.src = disorenable+id+"&actie=enable";	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').innerHTML='';
	document.getElementById('result').appendChild(script);
}
function disableWine(id){
	var script = document.createElement('script');	
	script.src = disorenable+id+"&actie=disable";	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').innerHTML='';
	document.getElementById('result').appendChild(script);
}
</script>


<script type="text/javascript">
function mark(elm){
elm.innerHTML='';
}

</script>
<div id='result'></div>
<title>
Edit items with no candidates
</title>
</head>
<body>
<%
	String appellation = request.getParameter("appellation");
if (appellation==null||appellation.equals("All")) appellation="%";
%>
<jsp:include page="/header2.jsp" />
<jsp:include page="/snippets/textpagenosearch.jsp" />
<jsp:include page="ailinks.jsp" />
<%	
Context c=new Context(request);
AiHtmlRefiner r=new AiHtmlRefiner(c.tenant,new Recognizer("name","itemid","wineid","aiwines",1,true));
out.print(r.refineNoCandidatesHtml(200));
%>
</body> 
</html>
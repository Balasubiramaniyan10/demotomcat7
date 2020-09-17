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

var g_remoteServer = '/admin/removeterm.jsp?row=';
function removeterm(row,term){
	script = document.createElement('script');	
	script.src = g_remoteServer+row+"&term="+term;	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').innerHTML='';
	document.getElementById('result').appendChild(script);

	}

</script>
<script type="text/javascript">
function testKnownWine(id,full,lit,litex){
	script = document.createElement('script');	
	script.src = '/admin/testknownwine.jsp?id='+id+'&full='+full+'&lit='+lit+'&litex='+litex;
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	document.getElementById('result').appendChild(script);
	}

</script>
<div id='result'></div>
<title>
The Know Wines List editor
</title>
</head>
<jsp:include page="/admin/adminlinks.jsp" />
<%
	String selectedregion=request.getParameter("region");
	if (selectedregion==null||selectedregion.equals("")){
		selectedregion="Select";
	}
%>

<form id="theform" name="theform" action='removeredundantterms.jsp' method='POST'>
Select region: <select name='region' onChange="document.getElementById('theform').submit();">
<%
	ArrayList<String> regions=Region.getRegions("All");
out.write("<option value='Select'>Select");

for (String region:regions){
%>
<option value='<%= region%>' <%if (region.equals(selectedregion)) out.write(" Selected");%>><%=region%>
<%}%>
</select>
</form>

<%	if (!selectedregion.equals("Select")) out.print(Knownwines.redundantWineTermsHTML2(0,selectedregion));
%>
</body> 
</html>
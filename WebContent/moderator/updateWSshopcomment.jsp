<html>
<head>
<title>Autodiscovery of a URL</title>
</head>
<jsp:include page="/admin/adminlinks.jsp" />
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
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%>
<%
	String reason = request.getParameter("reason");
	if(reason==null) reason="";
	String shopid= request.getParameter("shopid");
	if (shopid==null) shopid="";

	// New shop, Loose url and postdata
	if (!shopid.equals("")){
		String query="update wsshops set comment='"+Spider.SQLEscape(reason)+"' where wsid='"+shopid+"';";
		Dbutil.executeQuery(query);
%>
		<script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script>
		<%
		
	}
	%>
</body></html>

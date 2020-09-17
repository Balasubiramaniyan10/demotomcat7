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
	import = "com.freewinesearcher.common.Region"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%>
<title>
The Region Updater
</title>
</head>
<body>
<%
	String region=request.getParameter("region");	
String parent=request.getParameter("parent");	
String like=request.getParameter("like");
if (parent==null) parent="";
if (like==null) like="";
if (!like.equals("")) like=Dbutil.readValueFromDB("select parent from regions where region='"+Spider.SQLEscape(like)+"';","parent");
out.write(like);
	if (parent.equals("")){
		ArrayList<String> regions=Region.getRegions("All");
%>
		<form action='addregion.jsp' method="post" id="Searchform" name="Searchform">
		<select name="parent">
			<option value="All" <%if (region.equals("All")) out.print(" selected='selected' ");%>>All countries/regions</option>
					<% for (int i=0;i<regions.size();i++){%>
					<option value="<%=regions.get(i)%>" <%if (like.equals(regions.get(i))) out.print(" selected='selected' ");%>><%=regions.get(i)%></option>
					<%}%>
					</select>
					<input type='hidden' name='region' value='<%=region %>'/>
					<input type="submit" value="Submit"/>
	  	</form><%
	} else {
		Region.addRegion(region,parent);
		%>
		<script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script>
<%	
	}
	%>


</body> 
</html>
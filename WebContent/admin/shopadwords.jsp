<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="com.freewinesearcher.online.PageHandler"
 import="com.freewinesearcher.common.Context"
%><html>
<head>
<title>Test a shop</title>
</head>
<body bgcolor="white">

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
	
%>
<% 	int shopid=0;
	try{	shopid=Integer.valueOf(request.getParameter("shopid"));}	catch (Exception exc){}
	ArrayList<String> shops = Webroutines.getShopList(""); 
	
%>
<FORM ACTION="<%= response.encodeURL("shopadwords.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to test</TD><TD width="75%"><select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(shopid)) ) out.print(" Selected");%>><%=shops.get(i+1)%>
<%}%>
</select></TD></TR><tr><td>Language</td><td><select name='language'><option value='NL'>Nederlands</option><option value='EN'>English</option></select></td></tr></TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" value="testshop">
    <input type="Submit" name="testshop"
       value="Generate ads" >
</FORM><br/><br/>
<% if (shopid>0) out.write(Webroutines.getShopAdwordsData(shopid, request.getParameter("language")));%>

</body>
</html>
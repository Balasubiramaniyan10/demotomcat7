
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.online.PageHandler"
 import="com.freewinesearcher.common.Context"
%><html>
<head>
<title>Test a shop</title>
</head>
<body bgcolor="white">
<div class="textpage">
<jsp:include page="moderatorlinks.jsp" />
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
<% 	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	int shop=0;
	try{	shop=Integer.valueOf(shopid);}
	catch (Exception exc){
		
	}
	ArrayList shops = Webroutines.getShopList(""); 
	ArrayList autoshops = Webroutines.getShopList("auto");
	String actie = request.getParameter("actie");
	if (actie!=null&&actie.equals("testshop")) {
		if (shop>0){
			ThreadGroup threadgroup = new ThreadGroup("Scrape Shops");
			Spider s=new Spider(shopid, "","",1);
			s.c=new Context(1);
			Thread t=new Thread(threadgroup,s);
			t.start();
			Auditlogger al2=new Auditlogger(request);
			al2.setAction("Test shop");
			try{al2.setShopid(Integer.parseInt(shopid));}catch (Exception e){}
			al2.logaction();
			response.sendRedirect("manage.jsp?shopid="+shopid);
			}
	} else {	
%>
<FORM ACTION="<%= response.encodeURL("testshop.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to test</TD><TD width="75%"><select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(shopid))&&(auto.equals("")) ) out.print(" Selected");%>><%=shops.get(i+1)%>
<%}%>
<% for (int i=0;i<autoshops.size();i=i+2){%>
<option value="auto<%=autoshops.get(i)%>"<% if ((autoshops.get(i).equals(shopid))&&(auto.equals("auto")) ) out.print(" Selected");%>><%=autoshops.get(i+1)%>
<%}%>
</select></TD></TR></TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" value="testshop">
    <input type="Submit" name="testshop"
       value="Test" >
</FORM>
<% 	}%>
</div>
</body>
</html>
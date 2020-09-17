<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%	String shopid = request.getParameter("shopid");
	String reason = request.getParameter("reason");
	if (reason==null) reason="";
	if(shopid==null) shopid="";
	String confirm = request.getParameter("confirm");
	ArrayList<String> currentconfig;
	ArrayList autoshops = Webroutines.getShopList("auto");
	if (confirm!=null&&confirm.equals("Delete")){
		out.write("Result: "+Webroutines.deleteAutoShop(shopid,reason));
	} 
	if (confirm!=null&&confirm.equals("Abort")){
		Wijnzoeker.stopautoshop=shopid;
	}
	%>
<title>
Overview of all autoshops
</title>
</head>
<jsp:include page="/admin/adminlinks.jsp" />
<%=Webroutines.getAutoShopOverviewHTML() %>
</body> 
</html>
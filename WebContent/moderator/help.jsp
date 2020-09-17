<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.batch.Spider"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/>
<%
	int id=0;
	id=Dbutil.readIntValueFromDB("select * from helpscreens where page='"+Spider.SQLEscape(request.getParameter("page"))+"'","id");
	if (id==0){
		Dbutil.executeQuery("insert into helpscreens (page,text) values ('"+Spider.SQLEscape(request.getParameter("page"))+"','')");
		id=Dbutil.readIntValueFromDB("select * from helpscreens where page='"+Spider.SQLEscape(request.getParameter("page"))+"'","id");
		
	}
cu.setAl(new Auditlogger(request));
cu.setId(id);
cu.setTablename("helpscreens");
cu.setIdcolumn("id");
boolean edit=false;
if (request.isUserInRole("admin")||cu.validAccessCode()) {
	edit=true;
} %>
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
%>
<%String order = request.getParameter("order");

	%>
<title>
Help
</title>
</head>
<body>
<jsp:include page="moderatorlinks.jsp" />
<br/>
<div id='helptext'><%=Dbutil.readValueFromDB("select * from helpscreens where page='"+Spider.SQLEscape(request.getParameter("page"))+"'","text")%></div>
<% if (request.isUserInRole("admin")){out.write("<script type='text/javascript' src='/js/tiny_mce/tiny_mce.js'></script>");
	cu.setContentcolumn("text");
	cu.setElementid("helptext");
	out.write(cu.getHtml(request));}%>
</body> 
</html>
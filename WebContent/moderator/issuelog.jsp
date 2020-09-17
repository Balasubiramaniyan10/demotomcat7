<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.freewinesearcher.online.web20.CommunityUpdater"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.batch.XpathWineScraper"%>
<%@page import="com.freewinesearcher.batch.Spider"%>
<%@page import="com.freewinesearcher.online.Shopapplication"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.*"%><%request.setCharacterEncoding("UTF-8"); %>
<html><head>
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
	import = "com.freewinesearcher.common.Context"
%>
<title>
To Do list for store management</title>
<% 	PageHandler p=PageHandler.getInstance(request,response,"Edit shop");%>

<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
</head>
<body>
<div class="textpage">
<jsp:include page="moderatorlinks.jsp" />
<%	int shopid=0;
Dbutil.logger.info(request.getParameter("message"));
	try{shopid=Integer.parseInt(request.getParameter("shopid"));}catch(Exception e){}
	String action=request.getParameter("action");
	if (action!=null&&action.equals("update")){
		Dbutil.executeQuery("update issuelog set actueel=0 where shopid="+shopid);
		Dbutil.executeQuery("insert into issuelog (date,userid,shopid,status,message,actueel) values (sysdate(),'"+Spider.SQLEscape(request.getRemoteUser())+"',"+shopid+","+request.getParameter("status")+",'"+Spider.SQLEscape(request.getParameter("message").replaceAll("\r\n","<br/>"))+"',("+request.getParameter("status")+" in(1,2)));");
	}
	int laststatus=Dbutil.readIntValueFromDB("select * from issuelog where shopid="+shopid+" order by date desc,id desc limit 1;","status");
	
	%>
	<br/><h2>Issues for store <%=shopid %> (<%=Webroutines.getShopNameFromShopId(shopid,"") %>)</h2><br/>
	<%=Webroutines.lastshopissue(shopid)%><br/>
	<br/>New comment, question or solution:<br/>
	<form accept-charset="UTF-8" action='issuelog.jsp' method='post'><textarea name='message' rows='10'cols='80'></textarea><br/>
	New status: <select name='status'>
	<option value='0'<%if (laststatus>0) out.write(" selected='selected'"); %>>Issue resolved</option>
	<option value='1'<%if (laststatus==0&&request.getRemoteUser().equalsIgnoreCase("china")) out.write(" selected='selected'"); %>>Action/question for Netherlands</option>
	<option value='2'<%if (laststatus==0&&!request.getRemoteUser().equalsIgnoreCase("china")) out.write(" selected='selected'"); %>>Action/question for China</option>
	<option value='3'<%if ("3".equals(request.getParameter("status"))) out.write(" selected='selected'"); %>>Issue cannot be solved</option>
	</select>
	<input type='hidden' name='shopid' value='<%=shopid %>'/>
	<input type='hidden' name='action' value='update'/>
	<input type='Submit' value='Update'/>
	</form>


<h3>History:</h3>
<%=Webroutines.query2table("select date,userid as user, message from issuelog where shopid="+shopid+" order by date desc,id desc",false,false)%>
</body> 
</html>
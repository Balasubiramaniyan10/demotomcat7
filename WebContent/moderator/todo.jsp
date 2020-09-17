<%@page import="com.freewinesearcher.batch.Shopstats"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.batch.XpathWineScraper"%>
<%@page import="com.freewinesearcher.batch.Spider"%>
<%@page import="com.freewinesearcher.online.Shopapplication"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.*"%><html>

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
<%	int minwines=0;
	try{minwines=Integer.parseInt(request.getParameter("minwines"));}catch(Exception e){}
	String country=request.getParameter("country");
	if (country==null) country="";
	String type=request.getParameter("type");
	if (type==null) type="";
	int shopid=0;
	try{shopid=Integer.parseInt(request.getParameter("shopid"));}catch(Exception e){}
	int wsid=0;
	try{wsid=Integer.parseInt(request.getParameter("wsid"));}catch(Exception e){}
	if (wsid>0&&shopid>0) Dbutil.executeQuery("update wsshops set shopid="+shopid+" where wsid="+wsid);
	boolean moderator=false;
	if (request.getParameter("role")!=null)moderator=true; 
	%>
	
<h1>Issuelog</h1>
<%=Webroutines.IssuesToDoHtml(request.isUserInRole("admin")&&!moderator) %>
<h1>Stores with problems</h1>
<%=Shopstats.getHtml(request.getParameter("sort"),true,request.isUserInRole("admin")&&!moderator)  %>
<h1>Add a new store</h1>
<h2><%=country %></h2>
<%=Webroutines.StoresToDoHtml(country,false,minwines,type) %>
<h2>Stores that take very long to process</h2>
<%=Webroutines.query2table("select concat('<a href=\\'editspiderregex.jsp?shopid=',shopid,'&actie=retrieve\\'>',shopname,'</a>') as shopname,concat('<a href=\\'manage.jsp?actie=retrieve&shopid=',shopid,'\\'>Log</a>') as Log,shopid,winestotal,urls,duration from shopstats join shops on (shopstats.shopid=shops.id) where shoptype!=2 and duration between 3600 and 999998 and shops.disabled=0 order by urls desc;",true,false) %>
</div>
</body> 
</html>
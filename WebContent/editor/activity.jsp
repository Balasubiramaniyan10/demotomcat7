<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="java.util.LinkedHashSet"%>
<%@page import="com.freewinesearcher.online.Regioninfo"%>
<%@page import="com.freewinesearcher.online.web20.CommunityUpdater"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN"><%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<% long start=System.currentTimeMillis(); 
	boolean debuglog=false;
	boolean edit=false;
	
	if (request.isUserInRole("admin")) edit=true;
	if (request.isUserInRole("editor")) edit=true;
	PageHandler p=PageHandler.getInstance(request,response,"Regioninfo");
	if (p.ipaddress.equals("85.147.228.61")||p.ipaddress.equals("127.0.0.1")) debuglog=true;

%>
<%@page import="com.freewinesearcher.online.Regioninfo"%>
<head>
<title>
User activity
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>


</head>
<body>
 
 	
<%@ include file="/snippets/topbar.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %></div>
		<div class='main'><br/>
<h1>User activity</h1>
<%=Webroutines.query2table("select changelog.userid,sel.n as Regions,mid(valuenew,instr(valuenew,'<region>')+8,instr(valuenew,'</region>')-instr(valuenew,'<region>')-8) as 'Last region',max(changelog.date) as 'Last activity' from (select count(distinct(rowid)) as n,userid,max(date) as lastentry from changelog where userid!='' and tablename='kbregionhierarchy' group by userid) sel join changelog on (sel.userid=changelog.userid and sel.lastentry=changelog.date) where changelog.tablename='kbregionhierarchy' group by changelog.userid order by regions desc;",true,false)%>
</div>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
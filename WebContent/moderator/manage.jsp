<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="com.freewinesearcher.batch.Spider"%><html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
%>
<%String shopid = request.getParameter("shopid");
String action = request.getParameter("action");
if (action==null) action="";
	%>
<title>
Overview of spider and wines for shop <%=shopid %>
</title>
</head>
<body>
<div class="textpage">
<jsp:include page="moderatorlinks.jsp" />
<br/>Manage wines from shop <%=shopid %><br/>
<%	boolean killspider=false;

	if (action!=null&&action.equals("killspider")) killspider=true;
	
	if (action!=null&&action.equals("remove")){
	Webroutines.removeOldWines(shopid);
	response.sendRedirect("index.jsp");
	} else {
		if (action!=null&&action.equals("fixcharset")){
		Dbutil.executeQuery("update shops set encoding='ISO-8859-1' where id="+shopid);
	} 
		out.write(Webroutines.getLogDetails(shopid));
		out.write("<br/>");
		out.write("<a href='"+("/moderator/detectscraper.jsp?shopid="+shopid)+"'>Edit Scraper</a>&nbsp;&nbsp;&nbsp;");
		out.write("<a href='"+("/moderator/editspiderregex.jsp?actie=retrieve&shopid="+shopid)+"'>Edit Spider</a><br/>");
		
		out.write("Store url: <a href='"+Dbutil.readValueFromDB("select * from shops where id="+shopid,"shopurl")+"'target='_blank'>"+Dbutil.readValueFromDB("select * from shops where id="+shopid,"shopurl")+"</a><br/>");
		ThreadGroup threadgroup = Thread.currentThread().getThreadGroup().getParent();
		Thread [] threads = new Thread [threadgroup.activeCount ()];
		int n = threadgroup.enumerate (threads);
		boolean running=false;
		for (int i = 0; i < n; i++){
			if (("Shop "+shopid).equals(threads[i].getName())){
				running=true;
				if (killspider){
					threads[i].interrupt();
				}
			}
		}
		if (running){
			out.write("Spider is running for this store. <a href='manage.jsp?shopid="+shopid+"&action=killspider'>Kill it!</a><br/>");
			Auditlogger al2=new Auditlogger(request);
			al2.setAction("Manage shop refresh");
			try{al2.setShopid(Integer.parseInt(shopid));}catch (Exception e){}
			int wines=Dbutil.readIntValueFromDB("select count(*) as thecount from wines where shopid=0"+shopid+" and lastupdated>date_sub(sysdate(),interval 1 hour);","thecount");
			al2.info=wines+" wines scraped";
			al2.logaction();
		} else {
			Auditlogger al2=new Auditlogger(request);
			al2.setAction("Manage shop "+action);
			try{al2.setShopid(Integer.parseInt(shopid));}catch (Exception e){}
			int wines=Dbutil.readIntValueFromDB("select count(*) as thecount from wines where shopid=0"+shopid+" and lastupdated>date_sub(sysdate(),interval 1 hour);","thecount");
			al2.info=wines+" wines scraped";
			al2.logaction();
		}
		if (Dbutil.readIntValueFromDB("select * from shops where id="+shopid+" and encoding='Unknown'","id")>0){
			out.write("WARN: encoding is set to Unknown. <a href='removeoldwines.jsp?shopid="+shopid+"&action=fixcharset'>Click here</a> if the current list of wines looks fine (encoding will be set to ISO-8859-1).<br/>");
		}
		out.write("<br/>");
		out.write(Webroutines.displayScrapelist(shopid));
		%>
		Remove old wines? <a href='manage.jsp?shopid=<%=shopid %>&action=remove'>Yes </a><a href='overview.jsp'>No </a><br/>
		<%
		out.write(Webroutines.displayOldWines(shopid));
	}

%></div>
</body> 
</html>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.sql.Time"%>
<%@page import="java.sql.Date"%>
<%@page import="java.util.Set"%>
<%@page import="com.freewinesearcher.common.POI"%>
<%@page import="com.freewinesearcher.online.MapDataset"%>
<%@page import="com.freewinesearcher.online.Bounds"%>
<%@page import="com.freewinesearcher.common.Configuration"%> 
<%@page import="com.freewinesearcher.online.*"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.freewinesearcher.online.PageHandler"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Region"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Configuration"
	import="com.freewinesearcher.online.Producer"%>
<%PageHandler p=PageHandler.getInstance(request,response,"Visiting Report"); 
	String ids=request.getParameter("producers");
	if (ids==null||ids.split(",").length>9) ids="";
	Producers producers=null;
	Producers selectedproducers=null;
	producers=new Producers(request.getParameter("producers"));
	if (request.getParameter("selected")!=null)  selectedproducers=new Producers(request.getParameterValues("selected"));
	boolean map=false;
	String mapjs="";
	if ("on".equals(request.getParameter("map"))) map=true;
%> 
<head>
<title>Visiting guide</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
<%@ include file="/header2.jsp" %>
<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=Configuration.GoogleApiKeyDev%>"  type="text/javascript"></script>

</head>
<body >
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<div class='centertext'>
<form method='post'><input type='hidden' name='producers' value='<%=ids %>'/>
<table><tr><td>
<% if (producers!=null) for (Producer producer:producers.producer){
%><input type='checkbox' name='selected' value='<%=producer.id %>' <%if (selectedproducers==null){out.write("checked='checked'");} else { for (Producer pr:selectedproducers.producer){if (producer.id==pr.id) out.write("checked='checked'");}}%>><%=producer.name %></input><br/>
<% }%></td><td>
<input type='checkbox' name='map' <%if(map) out.write("checked='checked'"); %>>Show small maps for each winery</input><br/>
</td></tr></table>
<input type='submit' name='action' value='Generate report'/>
</form><br/><br/>
<% if (selectedproducers!=null) for (Producer producer:selectedproducers.producer){%>
<% out.write(producer.getVisitingReport(p,map));
	mapjs+=producer.mapjs;
%>	
<% }%>
<%@ include file="/snippets/footer.jsp" %>
<% if (mapjs.length()>0){ %><script type='text/javascript'><%=mapjs%></script><%} %>
</div></body></html>
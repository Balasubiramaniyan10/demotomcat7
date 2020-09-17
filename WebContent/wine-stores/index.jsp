<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN"><%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%> 
<head>
<title>Wine stores and new deals close to you</title>
<meta name="description" content="A map showing you nearby wine stores and which stores have new and interesting deals."/>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Heatmap");%>
<%@ include file="/header2.jsp" %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h1>What is happening in wine stores around you?</h1>
We track the inventory and prices of thousands of wine stores. Because we daily update all our price information, we know about new wines coming to the market immediately. On the map below, we show wine stores close to you (use the zoom function if your location is not correct). A green dot means that a store added new wines to their inventory during the past week.<br/><br/><br/>
<div id='storelocator' style='width:900px;height:600px;border:1px solid #4d0027;'></div><script type='text/javascript'>	if(document.getElementById("storelocator")!=null&&document.getElementById("storelocator").innerHTML==''){		var vpiframe = document.createElement( "iframe" );		if (document.getElementById("storelocator").style.width=='') document.getElementById("storelocator").style.width="1000px";		if (document.getElementById("storelocator").style.height=='') document.getElementById("storelocator").style.height="500px";		vpiframe.setAttribute("frameborder","0");		vpiframe.setAttribute("scrolling","no");		vpiframe.setAttribute("width",document.getElementById("storelocator").style.width);		vpiframe.setAttribute("height",document.getElementById("storelocator").style.height);		vpiframe.setAttribute("overflow","hidden");		vpiframe.setAttribute( "src", "/storelocator.jsp?width="+document.getElementById("storelocator").style.width+"&height="+document.getElementById("storelocator").style.height+'&showprices=true');		document.getElementById("storelocator").appendChild(vpiframe);	}</script>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>

<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
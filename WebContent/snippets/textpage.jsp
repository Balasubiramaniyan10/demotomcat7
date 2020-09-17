<%@ page 
import = "com.freewinesearcher.online.Ad"
%>
<%	int numberofimages=2;
	try {numberofimages=Integer.parseInt((String)request.getAttribute("numberofimages"));}catch (Exception e){}
 %>
<%@ include file="/snippets/topbar.jsp" %>
<%@page import="java.util.HashSet"%><div class='textpage'>
<%@ include file="/snippets/logoandsearch.jsp" %>
<% if (numberofimages>0){ %>
<div id='right'><%
	String imagepath="/images/photos/";
 	HashSet<String> images=Webroutines.getRandomImages(application.getRealPath("/")+imagepath,numberofimages);
	int n=0;
 	for (String filename:images){
		n++;
		out.write("<img class='photo' src='"+imagepath+filename+"' alt='"+filename+"'/>");
	}
	if ("true".equals(request.getAttribute("ad"))){
	Ad rightad = new Ad("newdesign",160, 600, PageHandler.getInstance(request,response).hostcountry, PageHandler.getInstance(request,response).s.wineset.region, PageHandler.getInstance(request,response).s.wineset.knownwineid,"");
	out.write(rightad.html);
	}%>
</div>
<div id='left'>	
<%} else {	%>

<%} %>


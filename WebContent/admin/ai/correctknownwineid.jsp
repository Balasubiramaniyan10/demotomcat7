<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<html>
<head>
<title>
Wine correction
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"About");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head> 
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<%@ page 

	import = "com.searchasaservice.ai.AiHtmlRefiner"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Context"
	
%>

<%	boolean success=false;
	String wineidstr = request.getParameter("wineid");
	int wineid=0;
	try {wineid=Integer.parseInt(wineidstr);}catch(Exception e){}
	String ratedwineidstr = request.getParameter("ratedwineid");
	int ratedwineid=0;
	try {ratedwineid=Integer.parseInt(ratedwineidstr);}catch(Exception e){}
	int disableknownwineid=0;
	try {disableknownwineid=Integer.parseInt(request.getParameter("disableknownwineid"));}catch(Exception e){}
	String newwineidstr = request.getParameter("newknownwineid");
	int newwineid=0;
	try {newwineid=Integer.parseInt(newwineidstr);}catch(Exception e){}
	if (((wineid>0||ratedwineid>0||disableknownwineid>0)&&newwineid!=0)||disableknownwineid>0){
		Context c=new Context(request);
		success=Knownwine.editKnownwineId(c,wineid,ratedwineid,newwineid,disableknownwineid);
		if (success){
			%>
<%@page import="com.freewinesearcher.common.Knownwine"%><script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script><% 
		} else {%><h1>Error while saving information</h1><% 
		}
	}
	if (!success){	
		out.write(Knownwine.editKnownwineidHtml(wineid, ratedwineid));

	}
	
		
	%>
	
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	
</div>
</body> 
</html>
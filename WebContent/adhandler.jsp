
<%@page import="com.freewinesearcher.online.ExternalManager"%>
<%@page import="com.freewinesearcher.online.RecommendationAd"%><html><head>
<%@ page 
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%
	session = request.getSession(true);
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	String id=Webroutines.filterUserInput(request.getParameter("id"));
	String type=Webroutines.filterUserInput(request.getParameter("type"));
	int wineid=0;
	try{wineid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("wineid")));}catch (Exception e){}
	String hostcountry=String.valueOf(session.getAttribute("hostcountry"));
	String region=String.valueOf(session.getAttribute("region"));
	String knownwineid=String.valueOf(session.getAttribute("knownwineid"));
%>
<title>
Link to external site
</title>
</head>
<body>
<%	String url="https://www.vinopedia.com";
	if (wineid==0){
		url=Ad.getUrl(id,request.getRemoteAddr(),request.getRemoteUser(),searchdata,hostcountry,region,knownwineid);
	} else {
		url=RecommendationAd.getUrl(wineid,request.getRemoteAddr(),request.getRemoteUser(),searchdata,hostcountry,region,knownwineid,type);
	}
	url=ExternalManager.addGoogleParams(url);
%>
You should be forwarded to a new page. If this does not work, click <a href="<%=url%>">here</a>.
<%	response.sendRedirect(url);

%>

		
		
</body> 
</html>
		
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
%>
<html>
<head>
<title>
Advertising on Vinopedia
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Advertising");%>
<jsp:include page="/header.jsp" />
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %> 
<h1>Advertising on Vinopedia</h1>
Vinopedia offers very effective ways to advertise your product to a community of wine lovers. Please contact Jeroen Starrenburg if you would like to learn about the options.<br/><br/><div>
<a href="m&#97;ilto:&#102;&#101;&#101;&#100;&#98;&#97;&#99;&#107;&#64;&#118;&#105;&#110;&#111;&#112;&#101;&#100;&#105;&#97;&#46;&#99;&#111;&#109;" ><img src="/images/em.png" style='height:55px;'/></a>
<a href="skype:vinopediacom?call"><img src="/images/skypecall.jpg"  style='height:60px;'/></a>
<a href="skype:vinopediacom?chat"><img src="/images/skypetext.jpg"   style='height:60px;'/></a>

<%@ include file="/snippets/textpagefooter.jsp" %>
<%} %>	
</div>
</body> 
</html>
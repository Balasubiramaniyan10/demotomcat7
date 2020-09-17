<%@page import="com.freewinesearcher.online.Authorization"%><html>
<head>
<title>Tool Index</title>
<% 	PageHandler p=PageHandler.getInstance(request,response,"Moderator index");%>
<%@ include file="/header2.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
<%@ include file="/snippets/textpage.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head> 
<body>
<jsp:include page="moderatorlinks.jsp" />	

<jsp:include page="/snippets/textpagefooter.jsp" />
<% 	}	%>
</body>
</html>
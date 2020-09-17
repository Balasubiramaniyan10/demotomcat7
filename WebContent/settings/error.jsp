
<%if (request.getAttribute("javax.servlet.forward.request_uri") == null) {
        response.sendRedirect("/index.jsp");
    }%>
<html>
<head>
<title>Invalid login</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Login error");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
Invalid username and/or password, please try
<a href='<%= response.encodeURL("login.jsp") %>'>again</a>.
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
Invalid username and/or password, please try
<a href='<%= response.encodeURL("login.jsp") %>'>again</a>.
<%} %>
</body>
</html>

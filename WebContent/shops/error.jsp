<%if (request.getAttribute("javax.servlet.forward.request_uri") == null) {
        response.sendRedirect("/index.jsp");
    }%>
<html>
<head>
<title>Invalid login</title>
</head>
<body>
<jsp:include page="/header.jsp" /><br/><br/>
Invalid username and/or password, please try
<a href='<%= response.encodeURL("login.jsp") %>'>again</a>.
</body>
</html>

<%@page import="com.freewinesearcher.batch.Shopstats"%>
<html>
<head>
<jsp:include page="/header2.jsp" />
</head><body>
<% if (request.getParameter("update")!=null) Shopstats.updateStats(); %>
<a href='?update'>Update list</a><br/><br/>
<%=Shopstats.getHtml(request.getParameter("sort"),false,request.isUserInRole("admin"))%>
</body></html>
<% request.setAttribute("numberofimages","-1"); %><html><head><%
	int interval=0;
try{interval=Integer.parseInt(request.getParameter("interval"));}catch(Exception e){}
if (interval==0) interval=15; //minutes
%>
<jsp:include page="/header2.jsp" />
<title>Activity Monitor</title>
</head><body>
<%@ include file="/snippets/textpage.jsp" %>
<jsp:include page="adminlinks.jsp" />
<%=Webroutines.ActivityMonitor(interval) %> 
<%@ include file="/snippets/textpagefooter.jsp" %>
</body></html>
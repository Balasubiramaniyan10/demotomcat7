
<%
  if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/index.jsp");
    return;
  }
%>


<%@ page 
	import = "java.text.*"
	import = "com.freewinesearcher.online.Search"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Searchset"
	import = "com.freewinesearcher.online.Webroutines"
%>


<html>
<head>
<title>Visitors overview</title>
</head>
<body>
<jsp:include page="adminlinks.jsp" />


<br/>You are logged in as user <%= request.getRemoteUser() %><br/>
Click <a href="<%=response.encodeURL("index.jsp?logoff=true") %>">here</a> to logoff.<br/>
<br/>
Visitor overview<br/><br/>
<a href='visitoroverview.jsp?history=999'>Show all</a><br/>
<% int n=14;
	try{
		n=Integer.parseInt(request.getParameter("history"));
	} catch (Exception e){};
	
	
out.print(Webroutines.getVisitorOverview(n)) ; %>
<br/>
	
</body>
</html>

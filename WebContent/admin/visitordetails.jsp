
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

<%
	String date=Webroutines.filterUserInput(request.getParameter("date"));
	boolean markfirsttimeusers=false;
	try{	markfirsttimeusers=Boolean.parseBoolean(Webroutines.filterUserInput(request.getParameter("markfirsttimeusers")));
	} catch (Exception e){}
%>
<html>
<head>
<title>Visitors details for <%=date%></title>
</head>
<body>
<jsp:include page="adminlinks.jsp" />

<% 
	
%>
<br/>You are logged in as user <%= request.getRemoteUser() %><br/>
Click <a href="<%=response.encodeURL("index.jsp?logoff=true") %>">here</a> to logoff.<br/>
<br/>
Visitors details for <%=date%> (<a href='visitordetails.jsp?date=<%=date%>&markfirsttimeusers=true'>mark first time users</a>)<br/><br/>
<% out.print(Webroutines.getVisitorDetails(date, "date",markfirsttimeusers)) ; %>
<br/>
	
</body>
</html>

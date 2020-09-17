
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
Visitor overview<br/><br/>
<% 	int history=0;
	try{
		history=Integer.parseInt(request.getParameter("history"));
	} catch (Exception e){}

	String referrer=request.getParameter("referrer");
	if (referrer!=null&&!referrer.equals("")){
		out.print("<a href='referrers.jsp?referrer="+referrer+"'>Show all</a><br/><br/>");
		out.print(Webroutines.getReferrerDetails(history,referrer));
	} else {
	out.print(Webroutines.getReferrers(history,0)) ; 
	}%>
	
<br/>
	
</body>
</html>

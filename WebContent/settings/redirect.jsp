<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/index.jsp");
    return;
  }
%>
<%
	if (request.getParameter("url") != null) {
    response.sendRedirect(request.getParameter("url"));
    return;
  } else {
	  response.sendRedirect("/index.jsp");
  }
%>

<%
		String newurl=request.getServerName().replace("freewinesearcher","vinopedia")+request.getParameter("orgurl")+(request.getQueryString()==null?"":request.getQueryString());
		session.setAttribute("fws","true");
		response.setStatus(301);
		response.setHeader( "Location", newurl );
%>
<%
  if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/index.jsp");
    return;
  }
%>
<%@ page 
	import = "java.text.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%	String description=Webroutines.filterUserInput(request.getParameter("description"));
	String name=Webroutines.filterUserInput(request.getParameter("name"));
	String vintage=Webroutines.filterUserInput(request.getParameter("vintage"));
	String country = Webroutines.filterUserInput(request.getParameter("country"));
	String message = Webroutines.filterUserInput(request.getParameter("message"));
	String priceminstring=Webroutines.filterUserInput(request.getParameter("pricemin"));
	String pricemaxstring=Webroutines.filterUserInput(request.getParameter("pricemax"));
	String cheapest=Webroutines.filterUserInput(request.getParameter("cheapest"));
	String rareold=Webroutines.filterUserInput(request.getParameter("rareold"));
	String idstring = Webroutines.filterUserInput(request.getParameter("id"));
	message=Webroutines.saveSearch( request.getRemoteUser(),  idstring,  name, vintage, country, description,  priceminstring,  pricemaxstring,  rareold,  cheapest);
	%>
		<jsp:forward page="<%=response.encodeURL("index.jsp")%>">
		<jsp:param name="message" value="<%=message%>" />
		</jsp:forward>
	

%>

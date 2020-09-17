<%@page import="com.freewinesearcher.online.shoppingcart.Shoppingcart"%>
<%@page import="com.freewinesearcher.common.Context"%>

<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>

<%@page import="com.freewinesearcher.online.Webroutines"%><jsp:useBean id="cartmanager" class="com.freewinesearcher.online.shoppingcart.CartManager" scope="session"/>
<jsp:setProperty name="cartmanager" property="*" />
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%	cartmanager.p=PageHandler.getInstance(request,response);
	cartmanager.setAction(request.getParameter("action"));
	String f=cartmanager.handleAction(request);
	if (request.getParameter("action")!=null&&request.getParameter("action").equals("fillremotecart")){
		response.sendRedirect("/external.jsp?exturl="+Webroutines.URLEncode(f));
		return;
	}
	out.write(f);
	cartmanager.p.getLogger().logaction(); 
%>
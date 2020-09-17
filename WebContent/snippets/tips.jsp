<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>
<a name="tips"></a><div class='tips'>
<h2><%=PageHandler.getInstance(request,response).t.get("todaystips")%><%if (!PageHandler.getInstance(request,response).thispage.contains("/tips.jsp")) {%>  <span class='small'>(view <a href='/tips.jsp'>all price drops</a> here)</span><%} %></h2>
<p><%=PageHandler.getInstance(request,response).t.get("tiptext")%></p>
			<%
			String tips=(Webroutines.getTipsHTML3("", 8,PageHandler.getInstance(request,response).searchdata));
			out.print(tips.equals("") ? ("<br />" + PageHandler.getInstance(request,response).t.get("notips")):(tips));%>
<% if (!PageHandler.getInstance(request,response).thispage.contains("/tips.jsp")){%>
<%} %>
</div>


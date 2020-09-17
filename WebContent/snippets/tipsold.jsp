<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>
<div class='tips'>
<h2><%=PageHandler.getInstance(request,response).t.get("todaystips")%></h2>
<% if (PageHandler.getInstance(request,response).thispage.contains("/tips.jsp")){%>
<p><%=PageHandler.getInstance(request,response).t.get("tiptext")%></p>
<%} %>
			<%out.print(Webroutines.getTipsHTML3("", (float) 0.75, 6,PageHandler.getInstance(request,response).searchpage, PageHandler.getInstance(request,response).t.language, PageHandler.getInstance(request,response).searchdata));
			out.print((Webroutines.getTipsHTML3("", (float) 0.75, 6, PageHandler.getInstance(request,response).searchpage, PageHandler.getInstance(request,response).t.language, PageHandler.getInstance(request,response).searchdata)).equals("") ? ("<br />" + PageHandler.getInstance(request,response).t.get("notips")):(""));%>
<% if (!PageHandler.getInstance(request,response).thispage.contains("/tips.jsp")){%>
<div class='tipslink'><a href='/tips.jsp'>More tips</a></div>
<%} %>
</div>


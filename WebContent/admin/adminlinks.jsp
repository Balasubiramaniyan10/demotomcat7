
<%@page import="java.text.SimpleDateFormat"%>
<a href="/moderator/index.jsp">Moderator menu</a><br/>
<a href="index.jsp">Admin overview</a><br/>
<a href="visitoroverview.jsp">Visitors overview</a><br/>
<a href="<%= response.encodeURL("referrers.jsp")%>">Referrers</a><br/>
<a href="<%= response.encodeURL("top50.jsp")%>">Top 50 lists</a><br/>
<a href="<%= response.encodeURL("addautodiscovershop.jsp")%>">Automatically discover a new URL</a><br/>
<a href="<%= response.encodeURL("autoshopoverview.jsp")%>">View Automatically discovered shops</a><br/>
<a href="<%= response.encodeURL("/moderator/index.jsp")%>">Manage shops and spiders</a><br/>
<a href="<%= response.encodeURL("taskrunner.jsp")%>">Admin Task Runner</a><br/>
<a href="/log/vinopediaBatch.log">View batch logging</a><br/>
<a href="/log/tomcat.log">View Tomcat logging</a><br/>
<a href="<%= response.encodeURL("/admin/useractivity.jsp")%>">Check/rollback community changes</a><br/>
<a href="<%= response.encodeURL("/admin/ai/")%>">Knownwine tools</a><br/>
<a href="<%= response.encodeURL("/admin/salesdashboard.jsp")%>">Sales dashboard</a><br/>
<a href="<%= response.encodeURL("/admin/sponsors.jsp")%>">Sponsor overview</a><br/>
<a href="<%= response.encodeURL("/admin/financedashboard.jsp")%>">Finance dashboard</a><br/>
<a href='countryoverview.jsp'>Country overview of wines and shops</a><br/>
<a href='sendstoreinvitations.jsp'>Send mailing to stores</a><br/>
<a href='winerymailing.jsp'>Send mailing to wineries</a><br/>
<a href='usatodo.jsp'>To do list USA stores</a><br/> 
<a href='/admin/monitor.jsp?interval=15'>Activity Monitor</a><br/>
<a href='/admin/createpayment.jsp'>Create payment link</a><br/>
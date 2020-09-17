<%@page import="com.freewinesearcher.common.Configuration"%><%@ page 
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.common.Dbutil"
%><% 
	PageHandler.getInstance(request,response).getLogger().logaction();
	session.setAttribute("lasturl",PageHandler.getInstance(request,response).thispage);
	String canonicallink=(String)request.getAttribute("canonicallink");
	String regionlink=(String)request.getAttribute("regionlink");
	if (regionlink==null) regionlink="<a href='/region/'>Wines by region</a>";
%><div class='footer'><div id='links'><a href='/' rel='nofollow'>Home</a><a href='/links.jsp' rel='nofollow'>Links</a><a href='/contact.jsp' rel='nofollow'>Contact Vinopedia</a><a href='/about.jsp' rel='nofollow'>About us</a><a href='/termsofuse.jsp' rel='nofollow'>Terms of use</a><%=regionlink%><% if (canonicallink!=null) out.write("<br/>"+canonicallink); %></div>
<div class='clear'></div><div id='copyright'>&#169; <%=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR)%> Vinopedia.com</div></div><%
PageHandler.getInstance(request,response).firstrequest=false;
PageHandler.getInstance(request,response).cleanup();
//for (Cookie cookie:request.getCookies()) { 
//	if (cookie.getName().equals("wineid")||cookie.getName().equals("sort")||cookie.getName().equals("vintage")) {cookie.setMaxAge(0);response.addCookie(cookie);}
//}
 %>
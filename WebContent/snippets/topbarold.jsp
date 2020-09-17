<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>
<% //PageHandler p=(PageHandler)request.getAttribute("pagehandler");%>
<div class='topbar'>
<div class='topbarcontent'>
<div id='mobile'><a href='/mobile.jsp?name=<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName())+" "+PageHandler.getInstance(request,response).searchdata.getVintage()).trim()%>'>Mobile access</a></div>
<div id='language'><%=PageHandler.getInstance(request,response).t.get("language")%>: <a href='/lang-EN/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'",
							"&apos;")
							+ " " + PageHandler.getInstance(request,response).searchdata.getVintage())%>'><img src="/images/flags/english.gif" alt="English" /></a>&nbsp;<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'",
							"&apos;")) + " " + PageHandler.getInstance(request,response).searchdata.getVintage()%>'><img src="/images/flags/french.gif" alt="Français" /></a>&nbsp;<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'",
							"&apos;")
							+ " " + PageHandler.getInstance(request,response).searchdata.getVintage())%>'><img src="/images/flags/dutch.gif" alt="Nederlands" /></a></div>
&nbsp;
</div>
</div>
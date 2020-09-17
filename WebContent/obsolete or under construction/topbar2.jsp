<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>
<% //PageHandler p=(PageHandler)request.getAttribute("pagehandler");%>
<div class='topbar'>
<div class='topbarcontent'>
<div id='mobile'>
	<a href='<%=(PageHandler.getInstance(request,response).s!=null&&PageHandler.getInstance(request,response).s.wineset.knownwineid>0)?"/mwine/":"/m"+(PageHandler.getInstance(request,response).searchdata.getName().length()>2?"?name=":"")%><%=(PageHandler.getInstance(request,response).searchdata.getName().length()>2?Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName())+" "+PageHandler.getInstance(request,response).searchdata.getVintage():"").trim()%>'>Mobile access</a>
	<a href='/wine-guide/'>Wine Guide</a><a href='/settings/index.jsp'>PriceAlerts</a>
	<a href='/retailers.jsp'>Getting listed</a>
	<a href='/support.jsp'>Support us</a>
	<a href='/links.jsp'>Links</a>
	<a href='/publishers.jsp'>For web site owners</a>
	<!-- <a href='/Deals.jsp'>Deals</a> -->
	<%if (request.getRemoteUser()!=null) {%><a href='/settings/index.jsp?logoff=true'>Log off</a><%} %></div>
<div id='language'><%=PageHandler.getInstance(request,response).t.get("language")%>: 
<a href='/lang-EN/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+ " " + PageHandler.getInstance(request,response).searchdata.getVintage())%>'><img alt='English' src='/images/transparent.gif' class='sprite sprite-language sprite-english'/></a>
<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")) + " " + PageHandler.getInstance(request,response).searchdata.getVintage()%>'><img alt='Français' src='/images/transparent.gif' class='sprite sprite-language sprite-french'/></a>
<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+ " " + PageHandler.getInstance(request,response).searchdata.getVintage())%>'><img alt='Nederlands' src='/images/transparent.gif' class='sprite sprite-language sprite-dutch'/></a>
</div>
&nbsp;
</div>
</div>
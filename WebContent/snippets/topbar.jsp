<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>
<% //PageHandler p=(PageHandler)request.getAttribute("pagehandler");%>
<% String mobilelink="/m";
 if (PageHandler.getInstance(request,response).thispage.contains("/wine/")){
	 mobilelink=PageHandler.getInstance(request,response).thispage.replace("/wine/","/mwine/");
 } else if (PageHandler.getInstance(request,response).thispage.contains("/winery/")){
	 mobilelink=PageHandler.getInstance(request,response).thispage.replace("/winery/","/mwinery/");
 } else if (PageHandler.getInstance(request,response).thispage.contains("/region/")){
	 mobilelink=PageHandler.getInstance(request,response).thispage.replace("/region/","/mregion/");
 } else {
	mobilelink="/mobile.jsp"+(PageHandler.getInstance(request,response).searchdata.getName().length()>2?"?name="+Webroutines.URLEncode(Webroutines.removeAccents(PageHandler.getInstance(request,response).searchdata.getName()))+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"+"+PageHandler.getInstance(request,response).searchdata.getVintage():"").trim():"");
 }
%>
<%@page import="com.freewinesearcher.common.Configuration"%><div class='topbar spriter spriter-refine'>
<div class='topbarcontent'><img src='<%=Configuration.static2prefix %>/css/sprite4.png' style='display:none;width:1px;height:1px' alt=''/><img src='<%=Configuration.cdnprefix %>/css/spriter.png' style='display:none;width:1px;height:1px' alt=''/>
<div id='mobile'>
<a href="<%=mobilelink%>">Mobile access</a>
<a href='<%=(request.getAttribute("wineguidelink")==null?"/nf/wine-guide/":request.getAttribute("wineguidelink"))%>' rel='nofollow'>Wine Guide</a>
<a href='/settings/index.jsp' rel='nofollow'>PriceAlerts</a>
<a href='/retailers.jsp' rel='nofollow'>Getting listed</a>
<a href='/about.jsp' rel='nofollow'>About us</a>
<a href='/links.jsp' rel='nofollow'>Links</a>
<a href='/publishers.jsp' rel='nofollow'>For web site owners</a>
<!-- <a href='/Deals.jsp' rel='nofollow'>Deals</a> -->
<%if (request.getRemoteUser()!=null) {%><a href='/settings/index.jsp?logoff=true'>Log off</a><%} %>
</div>
<%if (false){ %>
<!-- 
<div id='language'><%=PageHandler.getInstance(request,response).t.get("language")%>: 
<a href='/lang-EN/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"?vintage=" + PageHandler.getInstance(request,response).searchdata.getVintage():""))%>'><img alt='English' src='/images/transparent.gif' class='sprite sprite-language sprite-english'/></a>
<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"?vintage=" + PageHandler.getInstance(request,response).searchdata.getVintage():""))%>'><img alt='Français' src='/images/transparent.gif' class='sprite sprite-language sprite-french'/></a>
<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"?vintage=" + PageHandler.getInstance(request,response).searchdata.getVintage():""))%>'><img alt='Nederlands' src='/images/transparent.gif' class='sprite sprite-language sprite-dutch'/></a>
</div>
 --><%} %>
&nbsp;
</div>
</div>
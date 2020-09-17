<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.online.Auditlogger"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Wine"
import = "com.freewinesearcher.common.Wineset"
%>
<jsp:useBean id="cu" class="com.freewinesearcher.online.web20.CommunityUpdater" scope="request"/><jsp:setProperty property="*" name="cu"/>
<% long start=System.currentTimeMillis();
	boolean debuglog=false;%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%PageHandler p=PageHandler.getInstance(request,response); %>
<jsp:useBean id="cartmanager" class="com.freewinesearcher.online.shoppingcart.CartManager" scope="session"/>
<jsp:setProperty name="cartmanager" property="wineid" value="0" />
<jsp:setProperty name="cartmanager" property="*" />
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% 
	if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Storepage: Start Pagehandler"); %>
<% 
String message="";
	Shoppingcart cart=cartmanager.getCart(new Context(request),0,false);
	if (cart.shopid==0){
		%><jsp:include page="/error.jsp" /><%
	} else {
%>
<jsp:setProperty name="cartmanager" property="amount"  value="0"/> 
<%@page import="com.freewinesearcher.online.shoppingcart.Shoppingcart"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.Comparator"%>
<%@page import="com.freewinesearcher.common.Knownwine"%>
<%@page import="com.freewinesearcher.online.Shop"%>
<%@page import="com.freewinesearcher.common.Configuration"%>
<%@page import="com.freewinesearcher.online.shoppingcart.CartManager"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.online.shoppingcart.CartSerializer"%>
<%@page import="com.freewinesearcher.online.WineAdvice"%>
<%@page import="com.freewinesearcher.common.datamining.Chart"%>
<%@page import="com.freewinesearcher.online.StoreInfo"%>
<%@page import="com.freewinesearcher.online.RecommendationAd"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head> 
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<script type='text/javascript'>function loadframe(){if ($('#storeiframe').attr('src')=='') window.setTimeout("$('#storeiframe').attr('src','https://www.vinopedia.com/wine/Chateau+Latour?utm_source=vinopedia1&utm_medium=vinopedia2&utm_campaign=freshonlyjsload');",200);}</script>
</head>
<body > 

<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

Test pagina frames<br/>
<a href='https://www.vinopedia.com/wine/Chateau+Latour?utm_source=vinopedia1&utm_medium=vinopedia2&utm_campaign=freshlinkclick'>Click</a>


	<iframe id="storeiframe" src=""  ></iframe>

</body>
</html>
<%}%>
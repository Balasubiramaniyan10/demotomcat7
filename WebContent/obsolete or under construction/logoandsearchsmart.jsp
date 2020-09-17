<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>

<%@page import="com.freewinesearcher.common.Dbutil"%>
<div class='logoandsearch' id='logoandsearch'>
		<div class='logo'><a href="/"><img src='/css2/logo.png' alt='Home page'/></a></div>
		<div class='search'>
		<div class='findwine'><%=PageHandler.getInstance(request,response).t.get("searchwine")%></div>
			<%// <img class='searchgosmall' src='/css2/gosmall.jpg' onclick='document.getElementById("searchform").submit()' alt='Search'/>%>
			<form action='<%=PageHandler.getInstance(request,response).searchpage%>' method="post" id="searchform" name="searchform"><input type="hidden" name="dosearch" value="true" /><input class='searchinput' id='name' type='text' autocomplete="off" name="name" value="<%=Webroutines.escape((PageHandler.getInstance(request,response).searchdata.getName()+" "+PageHandler.getInstance(request,response).searchdata.getVintage()).trim())%>" size="25" /></form>
			<div class='sprite sprite-gosmall searchgosmall' onclick='document.getElementById("searchform").submit()' alt='Search'></div>
		</div>
</div>

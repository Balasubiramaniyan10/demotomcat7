<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>

<%@page import="com.freewinesearcher.common.Dbutil"%>
<div class='logoandsearch'>
		<%
		if (!request.getServerName().contains("searcher")&&(session.getAttribute("fws")==null||!((String)session.getAttribute("fws")).equals("true"))){ %><div class='logo'><a href="/"><img src='/css/logo.png' alt='Home page'/></a><%} else {session.removeAttribute("fws");	%><div class='logofws'><a href="https://www.vinopedia.com"><img src='/css/FWSisnowvinopedia.png' alt='To vinopedia'/></a><%} %></div>
		<div class='search'>
			<img class='searchgosmall' src='/css/searchgosmall.jpg' onclick='document.getElementById("searchform").submit()' alt='Search'/>
			<form action='<%=PageHandler.getInstance(request,response).searchpage%>' method="post" id="searchform" name="searchform"><input type="hidden" name="dosearch" value="true" /><input class='searchinput' id='name' type='text' autocomplete="off" name="name" value="<%=Webroutines.escape(PageHandler.getInstance(request,response).searchdata.getName())%>" size="25" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" /></form>
			<div id="search_suggest" class="search_suggest_hidden" ></div>
		</div>
</div>

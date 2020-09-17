<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>
<div class='container'>
	<%if (!request.getServerName().contains("searcher")&&(session.getAttribute("fws")==null||!((String)session.getAttribute("fws")).equals("true"))){ %><div class='logo'><a href="/"><img src='/css/logo.png' alt='Home page'/></a><%} else {session.removeAttribute("fws");	%><div class='logofws'><a href="https://www.vinopedia.com"><img src='/css/FWSisnowvinopedia.png' alt='To vinopedia'/></a><%} %></div>
	<div class='clearboth'></div>
</div>
<div class="bigsearchbackground">
<div class="bigsearch">
	<div id="bigsearchleft" class="column"></div>
	<div id="bigsearchcenter" class="column">
		<h1>Find your favorite wine for the best price</h1>
		<form action='<%=PageHandler.getInstance(request,response).searchpage%>' method="post" id="searchform" name="searchform"><input type="hidden" name="dosearch" value="true" /><input class='searchinput' id='name' type='text' autocomplete="off" name="name" value="<%=Webroutines.escape(PageHandler.getInstance(request,response).searchdata.getName())%>" size="25" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" /></form>
		<div id="search_suggest" class="search_suggest_hidden" ></div>
		<div class='explanation'>Enter (part of) a wine name or producer, e.g. <a href='<%=PageHandler.getInstance(request,response).searchpage%>?name=Rieussec'>Rieussec</a> or <a href='<%=PageHandler.getInstance(request,response).searchpage%>?name=Gigondas+Guigal'>Gigondas Guigal</a></div>
	</div>
  	<div id="bigsearchright" class="column">
		<img class='searchgo' src='/css/searchgo.png' onclick='document.getElementById("searchform").submit()' alt='Search'/>
	</div>
</div>
</div>


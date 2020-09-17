<%@ page
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>

<%@page import="com.freewinesearcher.common.Dbutil"
import="com.freewinesearcher.common.Configuration"%>
<div class='logoandsearch' id='logoandsearch'>
		<div class='logo'><a href="/" title="Wine Searcher and Wine Prices Comparison"><img src='<%=Configuration.cdnprefix%>/css2/logo.png' alt='Vinopedia Wine Searcher and Price Comparison'/></a></div>
		<div class='search'>
		<div class='findwine'><%=PageHandler.getInstance(request,response).t.get("searchwine")%></div>
			<%// <img class='searchgosmall' alt='Search' src='/css2/gosmall.jpg' onclick='document.getElementById("searchform").submit()' />%>
			<form action='<%=PageHandler.getInstance(request,response).searchpage%>' method="post" id="searchform" name="searchform" accept-charset="UTF-8"><input type="hidden" name="dosearch" value="true" /><input class='searchinput' id='name' type='text' name="name" value="<%=Webroutines.escape((PageHandler.getInstance(request,response).searchdata.getName()+" "+PageHandler.getInstance(request,response).searchdata.getVintage()).trim())%>" size="25" /></form>
			<div class='sprite sprite-gosmall searchgosmall' onclick='document.getElementById("searchform").submit()' ></div>
		</div>
</div>
<% // <div id='plusone'><script type='text/javascript' defer="defer">/*<![CDATA[*/document.write('<g:plusone href="<%=PageHandler.getInstance(request,response).plusonelink % >"></g:plusone>');/*]]>*/</script></div>
%>
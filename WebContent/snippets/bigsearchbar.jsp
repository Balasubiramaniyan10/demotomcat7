<%@ page
import="com.freewinesearcher.common.Configuration"
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Hemabox"
import="com.freewinesearcher.online.Webroutines" 
import="com.freewinesearcher.common.Dbutil"%>
<div class='container'>
	<%if (!request.getServerName().contains("searcher")&&(session.getAttribute("fws")==null||!((String)session.getAttribute("fws")).equals("true"))){ %><div class='logo'><a href="/"><img src='<%=Configuration.cdnprefix%>/css2/logo.png' alt='Home page'/></a><%if (false){ %><div id='plusone'><script type='text/javascript'>/*<![CDATA[*/document.write('<g:plusone href="<%=PageHandler.getInstance(request,response).plusonelink %>"></g:plusone>');/*]]>*/</script></div><%}} else {session.removeAttribute("fws");	%><div class='logofws'><a href="https://www.vinopedia.com"><img src='/css/FWSisnowvinopedia.png' alt='To vinopedia'/></a><%} %></div>
	<div class='clearboth'></div>
</div>
<div class="bigsearchbackground">
<div class="bigsearch">
		<h1>The world's most powerful search engine for wine</h1>
		<form action='<%=PageHandler.getInstance(request,response).searchpage%>' method="post" id="searchform" name="searchform" accept-charset="UTF-8"><input type="hidden" name="dosearch" value="true" /><input class='searchinput' id='name' type='text' name="name" value="<%=Webroutines.escape(PageHandler.getInstance(request,response).searchdata.getName())%>" size="25"  /></form>
		<div class='explanation'>Enter a wine name, store, region or producer name and search in <%=Hemabox.getWinecount()%> wine offers. <br/>Try our <a style='text-decoration:underline;' href='/wine-guide/'>wine guide</a> if you're not looking for any specific wine, or <a style='text-decoration:underline;' href='/wine-stores/'>find a wine store close to you</a>.</div>
		<input type='image' class='sprite sprite-searchgo searchgo' src='<%=Configuration.staticprefix %>/images/transparent.gif' onclick='document.getElementById("searchform").submit()' alt='Search'/>
</div>
</div>


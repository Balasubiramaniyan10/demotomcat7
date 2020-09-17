<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
	import = "com.freewinesearcher.common.Dbutil"	
%>

<%@page import="com.freewinesearcher.online.PageHandler"%><html>
<head>
<title>
vinopedia Links
</title>
<% PageHandler p=PageHandler.getInstance(request,response,"Links");%>
<jsp:include page="/header2.jsp" />
<meta name="description" content="Links to interesting wine resources on the Web" />
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<h1>External Links</h1> 
Check out the following interesting web sites:<br/>
<a href='/link.jsp?exttarget=Robert Parker&exturl=<%=Webroutines.URLEncode("http://www.erobertparker.com")%>' target='_blank'>eRobertParker.com</a><br/>
The web site of the worlds most influential wine writer.<br/>
<a href='/link.jsp?exttarget=Wine Spectator&exturl=<%=Webroutines.URLEncode("http://www.winespectator.com")%>' target='_blank'>Wine Spectator</a><br/>
The web site of the worlds second most influential wine magazine, featuring interviews, blogs and tasting notes.<br/>
<a href='/link.jsp?exttarget=Jancis Robinson&exturl=<%=Webroutines.URLEncode("http://www.jancisrobinson.com/")%>' target='_blank'>Jancis Robinson</a><br/>
Jancis Robinson is an excellent wine writer. Her web site has very good background information about wine regions and producers, a well informed forum and the Oxford Companion on wine (members only).<br/>
<a href='/link.jsp?exttarget=Bordoverview&exturl=<%=Webroutines.URLEncode("http://www.bordoverview.com")%>' target='_blank'>Bordoverview.com</a><br/>
A site that lists the ratings of the major Bordeaux chateaux. Parker, Wine Spectator, Jancis Robinson, they're all neatly listed! A great help for choosing the right bottle for those with deep pockets... Also visit the <a href='/link.jsp?exttarget=Bordoverview Blog&exturl=<%=Webroutines.URLEncode("http://bordoverview.blogspot.com")%>' target='_blank'>Bordoverview Blog</a><br/>
<a href='/link.jsp?exttarget=Discover the origin&exturl=<%=Webroutines.URLEncode("http://www.discovertheorigin.co.uk/")%>' target='_blank'>Discover the Origin</a><br/>
European Union financed website with info on Burgundy wines, Douro wines and Port<br/>
<a href='/link.jsp?exttarget=eBacchus&exturl=<%=Webroutines.URLEncode("http://www.eBacchus.com")%>' target='_blank'>eBacchus</a><br/>
The Wine Information Community with SweetGrapes® Unbiased Member Reviews<br/>
<a href='/link.jsp?exttarget=WineLibraryTV&exturl=<%=Webroutines.URLEncode("http://tv.winelibrary.com")%>' target='_blank'>Wine Library TV</a><br/>
The Internets most passionate wine program. Some really funny episodes are there to discover if you can handle Gary's enthousiasm.<br/>
<a href='/link.jsp?exttarget=Espavino&exturl=<%=Webroutines.URLEncode("http://www.espavino.com/index_en.php")%>' target='_blank'>Espavino</a><br/>
Spain - Wine regions and their wine<br/>
<a href="/link.jsp?exttarget=WineDirectory&exturl=<%=Webroutines.URLEncode("http://www.winedirectory.org")%>" target="_blank">Wine Directory</a>
<br/>Wine Directory and Winery Search 
<h2>Dutch Links</h2>
<a href='/link.jsp?exttarget=Wijn Prikbord&exturl=<%=Webroutines.URLEncode("http://www.prikpagina.nl/list.php?f=310")%>' target='_blank'>Het Wijn Prikbord</a><br>
A Dutch forum on wine. 
<br/><a href='/link.jsp?exttarget=WijnenEten blog&exturl=<%=Webroutines.URLEncode("http://wijneneten.blogspot.com")%>' target='_blank'>Wijn en eten blog</a><br>
A blog specialized on wine, food and their pairing.<br>
<h1 id='linktovp'>Links to vinopedia</h1>
A link to vinopedia helps other people find this site and is highly appreciated by us! Our suggestion for a text link in English:<br/>
<a href='https://www.vinopedia.com' target='_blank'>Find your favorite wines for the best price on Vinopedia.com</a><br/>html code: <br/><code>&lt;a href='https://www.vinopedia.com' target='_blank'&gt;Find your favorite wines for the best price on Vinopedia.com&lt;/a&gt;</code><br/>
<br/>For image links, you can use the following image:<br/><br/>
<img src='https://www.vinopedia.com/images2/listedonvinopediasmall.gif' alt='Vinopedia.com wine search engine'/> <br/>
html code: <br/><code>&lt;a href='https://www.vinopedia.com' target='_blank' style:'border-style: none;'&gt;&lt;img src='https://www.vinopedia.com/images2/listedonvinopediasmall.gif'  style:'border-style: none;' alt='Vinopedia.com wine search engine'/&gt;&lt;/a&gt;</code><br/><br/> 
Be sure to check out the more advanced possibilities to add vinopedia functionality to your own web pages <a href='/publishers.jsp'>here</a>.
<br/>
<jsp:include page="/snippets/footer.jsp" />
</div>
<%} %>
</body> 
</html>
<html>
<head>
<title>RSS</title>
<script type="text/javascript">
<!--
function rss(form) {
	actionurl="/rss.jsp?name="+form.name.value;
	actionurl=actionurl+"&vintage="+form.vintage.value;
	actionurl=actionurl+"&pricemin="+form.priceminstring.value;
	actionurl=actionurl+"&pricemax="+form.pricemaxstring.value;
	actionurl=actionurl+"&rareoldstring="+form.rareoldstring.value;
  	document.Searchform.action=actionurl;
	form.submit();
	
  	return 0;
}
-->
</script>

<%@ page session="true"  
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>

<%
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { 
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="priceminstring" value=""/> 
		<jsp:setProperty name="searchdata" property="pricemaxstring" value=""/> 
		<jsp:setProperty name="searchdata" property="*"/> 
		<jsp:setProperty name="searchdata" property="offset" value="0"/>
		
<%
			}else {
		%>
		<jsp:setProperty name="searchdata" property="*"/> 
		
		<%
 					}

 				 					ArrayList<String> countries = Webroutines.getCountries();
 				 					if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
 				%>
<% PageHandler p=PageHandler.getInstance(request,response,"Show RSS url");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h4>Get your daily search results through RSS</h4>
<br/>
Never miss a good deal again! You can be informed automatically when new results for your search become available. For instance, suppose you are still dreaming of owning a Chateau d'Yquem, but unfortunately this is way over your budget. Then finally someone offers a bottle for &euro; 50! Now that is interesting, and you want to know this as soon as possible, without searching for Yquem every day. Well, that is exactly what is possible with RSS. Enter your search criteria on the <a href='/'>home page</a>, press the XML button and copy the url that is presented below to your RSS reader.  <br/>
<br/>
An RSS reader or News reader is a small program that sits on your desktop. It searches for you automatically so you don't have to search yourself every day. That way, you can stay informed about news, but also on new wines that become available. You can download several RSS readers for free, for example <a href="http://www.feedreader.com">www.feedreader.com</a> or <a href="http://www.sharpreader.net">www.sharpreader.net</a>.<br/><br/>
The search you just entered can be tracked by copying this entire URL in your  RSS reader: <br/>
<br/>
<b>https://www.vinopedia.com/rss.jsp?name=<%=Spider.escape(searchdata.getName()).replaceAll(" ","%20")%><wbr>&vintage=<%=Spider.escape(searchdata.getVintage()).replaceAll(" ","%20")%><wbr>&pricemin=<%=Spider.escape(searchdata.getPriceminstring())%><wbr>&pricemax=<%=Spider.escape(searchdata.getPricemaxstring())%></b><br/>
<br/>
If you paste this URL in your browser you will see some XML garbage, that is because you need to use an RSS reader. Please make sure you configure your RSS reader so that it refreshes only once per day for results (refreshing more often is useless as we refresh the winelist only once per day).

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
<br/><br/>
<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">
<h4>Get your daily search results through RSS</h4>
<br/>
Never miss a good deal again! You can be informed automatically when new results for your search become available. For instance, suppose you are still dreaming of owning a Chateau d'Yquem, but unfortunately this is way over your budget. Then finally someone offers a bottle for &euro; 50! Now that is interesting, and you want to know this as soon as possible, without searching for Yquem every day. Well, that is exactly what is possible with RSS. Enter your search criteria on the <a href='/'>home page</a>, press the XML button and copy the url that is presented below to your RSS reader.  <br/>
<br/>
An RSS reader or News reader is a small program that sits on your desktop. It searches for you automatically so you don't have to search yourself every day. That way, you can stay informed about news, but also on new wines that become available. You can download several RSS readers for free, for example <a href="http://www.feedreader.com">www.feedreader.com</a> or <a href="http://www.sharpreader.net">www.sharpreader.net</a>.<br/><br/>
The search you just entered can be tracked by copying this entire URL in your  RSS reader: <br/>
<br/>
<b>https://www.vinopedia.com/rss.jsp?name=<%=Spider.escape(searchdata.getName()).replaceAll(" ","%20")%><wbr>&vintage=<%=Spider.escape(searchdata.getVintage()).replaceAll(" ","%20")%><wbr>&pricemin=<%=Spider.escape(searchdata.getPriceminstring())%><wbr>&pricemax=<%=Spider.escape(searchdata.getPricemaxstring())%></b><br/>
<br/>
If you paste this URL in your browser you will see some XML garbage, that is because you need to use an RSS reader. Please make sure you configure your RSS reader so that it refreshes only once per day for results (refreshing more often is useless as we refresh the winelist only once per day).
		
</TD><TD class="right">
	<script async src="//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"></script>
		<!-- showrssurl 1 -->
		<ins class="adsbygoogle"
     		style="display:inline-block;width:120px;height:600px"
     		data-ad-client="ca-pub-5573504203886586"
     		data-ad-slot="5320995485">
     	</ins>
	<script>
		(adsbygoogle = window.adsbygoogle || []).push({});
	</script>
		
	</TD></TR>
</TABLE>
<jsp:include page="/footer.jsp" />	
</div>
</table>
<% } %>
</body> 
</html>
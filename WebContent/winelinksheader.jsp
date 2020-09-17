<% long start=System.currentTimeMillis();
	boolean debuglog=false;
	%><%PageHandler p=PageHandler.getInstance(request,response);
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start processSearchdata"); 
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"End processSearchData"); 
String originaltesturl=(String)request.getAttribute("originalURL");

int knownwineid=0;
int vintage=0;
try{knownwineid=Integer.parseInt(request.getParameter("knownwineid"));}catch(Exception e){}
try{vintage=Integer.parseInt(request.getParameter("vintage"));}catch(Exception e){}
if (knownwineid==0){
	p.createWineset=false; 
	p.processSearchdata(request);
	knownwineid=p.s.wineset.knownwineid;
	try{vintage=Integer.parseInt(p.searchdata.getVintage());}catch(Exception e){}
}
String source=request.getParameter("source");
if (source==null) source=(String)session.getAttribute("source");
if (source==null) source="";
session.setAttribute("source",source);
Linkboard linkboard=new Linkboard();
linkboard.setSource(source);
linkboard.setKnownwineid(knownwineid);
linkboard.setVintage(vintage);
linkboard.generateLinks();
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Configuration"
%>

<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>


<%@page import="com.freewinesearcher.online.Linkboard"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-1788182-2', 'auto');
ga('send', 'pageview');
</script>
<!-- End Google Analytics -->

<title>Vinopedia Link Board</title>
<%@ include file="/header2.jsp" %> 
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
<script type='text/javascript'>
function showbar(){
	 $('#linkboard').animate({marginTop:'0px'}, 0);
	 $("#winelinksframeset",parent.document).attr('rows','150px, 100%');
	 return true;
	
}
function hidebar(){
	 $('#linkboard').animate({marginTop:'-148px'}, 0);
	 $("#winelinksframeset",parent.document).attr('rows','42px, 100%');
	
}

function setSource(src){
	$("#source").val(src);
}

$(document).ready(function() {
	if (<%=(knownwineid>0)%>) parent.document.getElementById("bottom_frame").src='<%=(linkboard.getLinks().get(source)==null?"about:blank":linkboard.getLinks().get(source))%>';
	$('#linkboard').mouseenter(function() {showbar();});
	$('#linkboard').mouseleave(function() {hidebar();});
	if (false){
		var api = $("#links").scrollable({
		vertical: true,
		size: 1,
		clickable: false,
		keyboard: 'static',
		next:'#und',
		prev:'#und',
		nextPage:'#und',
		prevPage:'#und',
		onSeek: function(event, i) {
			horizontal.scrollable(i).focus();
		}
	}).navigator("#linkboardnavi");
	api.scrollable().move(<%=linkboard.getCategoryindex()%>);
	var horizontal = $(".scrollable").scrollable({size: 4});
	horizontal.eq(0).scrollable().focus();
	}
	});
</script>
</head>
<body>
<div id='linkboard' >
<%=linkboard.getLinkBoardHeader(1000, p.thispage,p.searchdata,p.t,false,(session.getAttribute("lasturl")==null?"https://www.vinopedia.com":(String)session.getAttribute("lasturl")))%>
	<div class='textpage'>
	<div id='content'>
	<%=linkboard.getLinkHtml() %>
	</div>
	</div>
	<div id='linkboardheader'>
		<div class='textpage'><img src='images/logosmall.gif' alt='Vinopedia'></img><div class='text'>Showing a page outside of Vinopedia. Move your mouse here to select a different publication or go back.</div></div>
		
	</div>
</div>

<div class='divider'></div>
</body>
</html>
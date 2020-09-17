<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN"><%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%> 
<head>
<title>
About Vinopedia.com
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"About");%>
<%@ include file="/header2.jsp" %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h2>The world's most powerful search engine for wine</h2>
<h1>About us</h1>
We combine up-to-date market information, factual wine information and expert reviews in one big database and let you play with it! You can use the following tools to look for wine:<br/>
<table><tr>
<td width='25%'><div class='dialog y'><div class='content'><div class='t'></div>
<h2>Wine search</h2>Provides you an overview of who is offering the wine in market as well as background information with links to Parker, Wine Spectator, Gary V and Cellar tracker. <br/><a href='/wine/Chateau+Rieussec' >Try it!</a>
</div><div class='b'><div></div></div></div></td>
<td width='25%'><div class='dialog y'><div class='content'><div class='t'></div>
<h2><a href='/wine-guide/'>Wine guide</a></h2>Allows you to discover wines by region, wine type, grape, vintage, price range, expert ratings and country of seller.<br/>
For example: find the best options for Napa Valley Chardonnay with an expert rating above 90 points. <br/><a href='/wine-guide/region/Napa+Valley/grape/Chardonnay/ratingmin/90' >Try it!</a>
</div><div class='b'><div></div></div></div></td>
<td width='25%'><div class='dialog y'><div class='content'><div class='t'></div>
<h2>Store advisor</h2>Shows you the most interesting deals for each merchant in terms of price, quality and latest arrivals. <br/>Helps you pick the best deals based on your preferences.<br/><a href='/store/Wine Library TV/#recommendations' >Try it!</a>
</div><div class='b'><div></div></div></div></td>
<td width='25%'><div class='dialog y'><div class='content'><div class='t'></div>
<h2><a href='/'>What's hot</a></h2>The home page provides you a number of top ten most popular searches, so you can see which wines suddenly get a lot of attention. Also shows major price drops in the market. <br/><a href='/#tips' >Try it!</a>
</div><div class='b'><div></div></div></div></td>
</tr></table>
<h2>Watch the demo to see how it all works:</h2>
<object type="application/x-shockwave-flash" style="width:600px; height:470px;" data="http://vimeo.com/moogaloop.swf?clip_id=9041973&amp;server=vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=ff9933&amp;fullscreen=1"><param name="movie" value="http://vimeo.com/moogaloop.swf?clip_id=9041973&amp;server=vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=ff9933&amp;fullscreen=1" /></object>
<h1>Free and objective</h1>
We believe search engines should be free and objective. That's why our visitors do not have to pay or sign up to see all search results. All our listings are objective, advertisements are clearly marked as such. And our search engine is open to all retailers.
<!-- 
<h1>Who's behind this</h1>
Vinopedia was created by two guys following their passion for wine and the Internet:<br/><br/>
<div class='clear'/>
<img  src="/pics/cellarsmall.jpg" alt='The Vinopedia team'/>
<div class='clear'/><br/>
Jeroen Starrenburg (left) and Jasper Hammink (right)
 -->
<h1>Spread the word</h1>
If you like Vinopedia.com, please do us a favor and tell your friends about us. Write about the site on blogs, forums, Twitter, Facebook, Hyves etc. And most of all help us create an even better product. Your thoughts on what we should develop next are much appreciated. Only with your help we are able to develop the search engine for wine you are looking for!
Please feel free to contact us with any questions or remarks about the search engine: <br/><br/><div>
<a href="m&#97;ilto:&#109;&#97;&#110;&#97;&#103;&#101;&#109;&#101;&#110;&#116;&#64;&#118;&#105;&#110;&#111;&#112;&#101;&#100;&#105;&#97;&#46;&#99;&#111;&#109;"  onClick="return confirmPost();"><img src="/images/em.png"  style='height:55px;'/></a>
<a href="skype:vinopediacom?call"><img src="/images/skypecall.jpg"  style='height:60px;'/></a>
<a href="skype:vinopediacom?chat"><img src="/images/skypetext.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://www.youtube.com/user/vinopediadotcom"><img src="/images/youtube.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://twitter.com/vinopediacom"><img src="/images/twitter.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://www.facebook.com/#!/pages/Vinopedia/154789861969?ref=ts"><img src="/images/facebook.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://www.linkedin.com/groups?gid=2291547&trk=myg_ugrp_ovr"><img src="/images/linkedin.jpg"   style='height:60px;'/></a>
</div>
<script type="text/javascript">
function confirmPost()
{
var agree=confirm("Vinopedia is a search engine for wine, we do not sell wine. With over 7 million users, we cannot answer any questions about a wine, its value or quality: you will have to look it up yourself on Vinopedia.com or contact the supplier directly.");
if (agree)
return true ;
else
return false ;
}
// -->
</script>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
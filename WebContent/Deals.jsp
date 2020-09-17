<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<%@ page session="true"  
import="com.freewinesearcher.online.PageHandler" 
import="com.freewinesearcher.online.Webroutines" %>

<%@page import="com.freewinesearcher.common.Dbutil"
import="com.freewinesearcher.common.Configuration"%>

<% //PageHandler p=(PageHandler)request.getAttribute("pagehandler");%>
<% String mobilelink="/m";
 if (PageHandler.getInstance(request,response).thispage.contains("/wine/")){
	 mobilelink=PageHandler.getInstance(request,response).thispage.replace("/wine/","/mwine/");
 } else if (PageHandler.getInstance(request,response).thispage.contains("/winery/")){
	 mobilelink=PageHandler.getInstance(request,response).thispage.replace("/winery/","/mwinery/");
 } else if (PageHandler.getInstance(request,response).thispage.contains("/region/")){
	 mobilelink=PageHandler.getInstance(request,response).thispage.replace("/region/","/mregion/");
 } else {
	mobilelink="/mobile.jsp"+(PageHandler.getInstance(request,response).searchdata.getName().length()>2?"?name="+Webroutines.URLEncode(Webroutines.removeAccents(PageHandler.getInstance(request,response).searchdata.getName()))+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"+"+PageHandler.getInstance(request,response).searchdata.getVintage():"").trim():"");
 }
%>

<html> 
<head>
<title>
Deals Vinopedia.com
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Deals");%>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<link rel="stylesheet" href="css/bootstrap.css">
<link rel="stylesheet" href="css/style.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
<script src="js/incrementing.js"></script>

<style>
.spriter {
    background: rgba(0, 0, 0, 0) url("https://static.vinopedia.com/css/spriter.png") repeat-x scroll left top;
}
.spriter-refine {
    background-position: 0 0;
}

div.topbar.spriter.spriter-refine {
    color: white;
    font-family: arial,sans-serif;
    font-size: 12px;
    padding-top: 1px;
    width: 100%;
	background-size: cover !important;
}
div.topbarcontent {
    margin-left: auto;
    margin-right: auto;
    width: 100%;
}
#mobile {
    float: left;
    margin-left: 3px;
}
div.topbar a {
    color: white;
    padding-right: 30px;
    text-decoration: underline;
	border-style: none;
}
div.footer {
    clear: both;
    font-family: arial;
    font-size: 11px;
    height: 20px;
    margin-left: 10px;
    margin-top: 10px;
}
div.footer #links {
    float: left;
    margin-top: 8px;
}

div.footer a {
    padding-right: 10px;
    text-decoration: underline;
    border-style: none;
    color: #4d0027;	
}
div.clear {
    clear: both;
}
div.footer #copyright {
    float: left;
    margin-top: 8px;
}
div.logoandsearch {
    font-size: 13px;
    height: 100px;
    margin-left: auto;
    margin-right: auto;
    width: 100%;
    z-index: 1;
}
div.textpage h2 {
    color: #4d0027;
    font-family: georgia,arial;
    font-size: 18px;
    padding-bottom: 4px;
    padding-top: 10px;
    font-weight: normal;
    margin-bottom: 2px;
    margin-top: 5px;	
}
div.search {
    background-image: url("https://static.vinopedia.com/css2/search.jpg");
    background-position: 0 40px;
    background-repeat: no-repeat;
    float: right;
    height: 100px;
    position: relative;
    width: 475px;
    z-index: 990;
}
.findwine {
    color: white;
    left: 7px;
    position: absolute;
    top: 49px;
}
#name {
    padding-left: 18px;
}
div.logoandsearch .searchinput, #linkboard .searchinput {
    border: medium hidden;
    color: black;
    display: block;
    font-family: Georgia,"Times New Roman",Times,serif;
    font-size: 17px;
    left: 161px;
    position: relative;
    top: 46px;
    width: 252px;
	border-radius: 4px;
	line-height: 20px !important;
}
.sprite {
    background: rgba(0, 0, 0, 0) url("https://static2.vinopedia.com/css/sprite4.png") no-repeat scroll left top;
}
.sprite-gosmall {
    background-position: -122px -1541px;
}
div.searchgosmall {
    cursor: pointer;
    height: 28px;
    left: 442px;
    position: absolute;
    top: 45px;
    width: 28px;
}
.tagline {
    color: #2b2b2b;
    display: inline-block;
    font-style: italic;
    margin: 30px 0 0 20px;
}
.container {
	max-width: 1052px!important;
}
.poplogo {
    display: inline-block;
    float: left;
    margin-bottom: 17px;
}
.popheader {
    display: inline-block;
    padding: 25px 0px 15px;
    vertical-align: top;
	width: 100%;
}
</style>

</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>

<%@page import="com.freewinesearcher.common.Configuration"%>
<div class='topbar spriter spriter-refine'>
<div class='topbarcontent hidden-xs hidden-phone'>
<div class="row-fluid container">
<img src='<%=Configuration.static2prefix %>/css/sprite4.png' style='display:none;width:1px;height:1px' alt=''/><img src='<%=Configuration.cdnprefix %>/css/spriter.png' style='display:none;width:1px;height:1px' alt=''/>
<div id='mobile'><a href="<%=mobilelink%>">Mobile access</a><a href='<%=(request.getAttribute("wineguidelink")==null?"/nf/wine-guide/":request.getAttribute("wineguidelink"))%>' rel='nofollow'>Wine Guide</a><a href='/settings/index.jsp' rel='nofollow'>PriceAlerts</a><a href='/retailers.jsp' rel='nofollow'>Getting listed</a><a href='/about.jsp' rel='nofollow'>About us</a><a href='/links.jsp' rel='nofollow'>Links</a>
<a href='/publishers.jsp' rel='nofollow'>For web site owners</a>
<!-- <a href='/Deals.jsp' rel='nofollow'>Deals</a> -->
<%if (request.getRemoteUser()!=null) {%><a href='/settings/index.jsp?logoff=true'>Log off</a><%} %></div>
<%if (false){ %>
<!-- 
<div id='language'><%=PageHandler.getInstance(request,response).t.get("language")%>: 
<a href='/lang-EN/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"?vintage=" + PageHandler.getInstance(request,response).searchdata.getVintage():""))%>'><img alt='English' src='/images/transparent.gif' class='sprite sprite-language sprite-english'/></a>
<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"?vintage=" + PageHandler.getInstance(request,response).searchdata.getVintage():""))%>'><img alt='Français' src='/images/transparent.gif' class='sprite sprite-language sprite-french'/></a>
<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(PageHandler.getInstance(request,response).searchdata.getName()).replace("'","&apos;")+(PageHandler.getInstance(request,response).searchdata.getVintage().length()>3?"?vintage=" + PageHandler.getInstance(request,response).searchdata.getVintage():""))%>'><img alt='Nederlands' src='/images/transparent.gif' class='sprite sprite-language sprite-dutch'/></a>
</div>
 --><%} %>
&nbsp;
</div>
</div>
</div>

<div id="productpopup" class="productpopup logoandsearch">
<div class="container">

<div class="row-fluid">
<div class="row-fluid popheader">
	<div class="poplogo">
		<a href="/"><img alt="" src="images/logo_deals.png">
		</a>
		<div class="tagline">We negotiate world class deals for you</div>
	</div>	
</div>
<div class="topheading">
 <h1>93 Point Small-Lot Cab Franc from Chile at the Lowest Price in the World</h1>
</div>
<div class="popcantentwrap">
<div class="pricesection">
<h2>2010 William Fèvre Franq Rouge Cabernet Franc</h2>
<div class="priceblock">
 <span>Retail Price: <samp>$42.00</samp> <i>(30% off)</i></span>
<span>Our Price: <strong>$29.00</strong></span>
</div>
<div class="availabletext">75 bottles available</div>
<!-- <form method="get" action="http://www.shareasale.com/r.cfm"> -->
<form method="get" action="http://www.globalwinecellars.com/!WGTRACK/Cart">
<div class="numbers-row">
<!-- <input type="hidden" name="b" id="b" value="847242" />
<input type="hidden" name="u" id="u" value="1248478" />
<input type="hidden" name="m" id="m" value="53209" />
<input type="hidden" name="urllink" id="urllink" value="" />
<input type="hidden" name="afftrack" id="afftrack" value="" />
 -->
 <input type="hidden" name="ADDTOCART" id="ADDTOCART" value="S000098X" />
<input type="text" name="qty" id="french-hens" value="1" maxlength="2" />
</div>
<label class="numbers-label">Bottles</label>
<input type="submit" class="addtobtn" value="GET THE DEAL" />
</form>
<p>Wines sold by Global Wine Cellars</p>
</div>
<div class="cnatentleft">
<div class="product_images">
<img src="images/product1.png" alt="" />
</div>
<div class="cantentleft">
<div class="expertblock">
<a href="javascript:;" class="primary-btn">Expert Review</a>
<h5><span>James Suckling</span>93/100</h5>
<p>"This is very structured and rich with lots of toasted oak and dark berry character. Full and round with a chewy texture and a long finish. <strong>Better in 2016.</strong> " - James Suckling</p>
</div>
<div class="expertblock">
<a href="javascript:;" class="primary-btn">Vinopedia Note</a>
<p>If you don't have Chilean wines in your rotation, you are missing out on huge value. This bold cabernet franc from the Maipo Valley has incredible QPR (quality/price ratio) at the price we've negotiated.</p>
<p>Made by the Chilean outpost of William Fèvre, one of the most respected vintners in France's famed Chablis region, this small-lot wine <strong>(just eight barrels made)</strong> comes from a vineyard located at an elevation of more than 2,000 feet. Thin mountain soils concentrate the grapes' flavors and cold nights give the wine freshness.</p>
</div>
</div>
</div>
</div>
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

</div>
</div>	
<div class="right">
	<script async src="//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"></script>
<!-- ad.java 1 -->
<ins class="adsbygoogle"
     style="display:inline-block;width:160px;height:600px"
     data-ad-client="ca-pub-5573504203886586"
     data-ad-slot="8134861081"></ins>
<script>
(adsbygoogle = window.adsbygoogle || []).push({});
</script>
		
	</div>
	
</body>
</html>
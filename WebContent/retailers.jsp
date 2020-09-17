<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.common.Configuration"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%PageHandler p=PageHandler.getInstance(request,response,"Retailer options");%><!DOCTYPE HTML>
<head>
<title></title>
<meta name="keywords" content="How to get listed as a retailer" />
<meta name="description" content="How to get listed as a retailer" />
<%@ include file="/header2.jsp" %>
<style type="text/css">
a:hover div.round{
background-color:#ff8;
color:#000;
} 
div.round {
	height:160px;
	float:left;
	background:white;
	padding:5px;
	margin-right:20px;
	margin-top:10px;
	border:5px solid #810f0c;
	-moz-border-radius:15px;
	-webkit-border-radius:15px;
	border-radius:15px;
		-moz-box-shadow: 5px 5px 5px #000;
			-webkit-box-shadow: 5px 5px 5px #000;
			box-shadow: 5px 5px 5px #000;
	behavior: url(border-radius.htc)}
</style>
</head>
<body  onload="javascript:doonload();">
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<div class='main'>
	<noscript><img src='/images/nojs.gif' alt=''/></noscript>
<h1>Get your store listed on Vinopedia</h1>
Every month more than 400.000 wine lovers use Vinopedia to find out where to buy their wines. We are an open platform and any wine retailer is welcome to join Vinopedia.com. <br/><br/>
We have three options for getting listed with us. Just click on the option most suited to your needs.
<div class="clear"></div><a href='retailerdetails.jsp?option=1' title='Select Free listing'><div class="round" style="width:290px;"><h2>Free Listing</h2>We list your wine offers for free, in return you are required to place a link back to us on your store's web site using this banner:<br/><br/>
<img style="margin-left:80px;" src='/images/listedbuttonwhite.gif' alt='Vinopedia wine search engine'/>
</div></a><a href='retailerdetails.jsp?option=2' title='Select Basic listing'><div class="round" style="width:290px;">
<h2>Basic Listing</h2>We list your wine offers for only &euro;250 per year excluding VAT (E.U. countries) or US$450 per year including VAT (USA and rest of the world) . No link back to vinopedia is required and no ads are shown on your store page.
</div></a><a href='retailerdetails.jsp?option=3' title='Select Sponsored listing'><div class="round" style="width:290px;">
<h2>Sponsorship</h2>We list your wine offers and turn on all advertising options for &euro;1500 per year excluding VAT (E.U. countries) or US$2550 per year including VAT (USA and rest of the world). Warning: advertising may double the amount of visitors to your site!
</div></a>
<div class="clear"></div>
<img src="/retailer/down_48.gif" alt="down" style='float:left;margin-left:130px;margin-top:10px;'/>
<img src="/retailer/downleft_48.png" alt="down" style='float:left;margin-left:230px;margin-top:10px;'/>
<img src="/retailer/down_48.gif" alt="down" style='float:left;margin-left:370px;margin-top:10px;'/>
<div class="clear"></div>
<div class="round" style="width:455px;height:700px;">
<h2>Standard listing</h2>
<h3>1. Your wines appear in search results</h3>
<img src='/retailer/search.gif' style='width:300px;height:83px;' alt="Search results"/><br/><br/>
<h3>2. Your own store page on Vinopedia</h3>
<img src='/retailer/store.gif' style='width:300px;height:75px;' alt="Store page"/><br/><br/>
<h3>3. Your store is included on the wine store map</h3>
<img src='/retailer/map.jpg' style='width:300px;height:180px;' alt="Wine store map"/><br/><br/>
</div>
<div class="round" style="width:455px;height:700px;">
<h2>Sponsored listing</h2>
This includes all the features from the standard listing, plus:<br/><br/>
<h3>4. Your banner (234x60) appears on search result pages<sup>*</sup></h3>
<img src='/retailer/banner234.jpg' style='width:234px;height:60px;' alt="Banner 234x60"/><br/><br/>
<h3>5. Your offers appear on the top of search results as featured merchant<sup>*</sup></h3>
<img src='/retailer/featured.jpg' style='width:300px;height:106px;' alt="Featured merchant"/><br/><br/>
<h3>6. Your banner (160x600) appears on pages of stores with free listing<sup>*</sup></h3>
<img src='/retailer/skyscraper.jpg' style='width:400px;height:246px;' alt="Banner 234x60"/><br/><br/>
<sup>*</sup>We select relevant advertisements based on the location (country) of the visitor and the selected wine. 
<br/>
</div>
<div class="round" style='width:0px;height:0px;visibility:hidden'></div>
<div class="clear"></div>
<br/>

<h2>Frequently asked questions</h2>
<br/>Q: What do I need to do to get my wines and prices into the search engine?<br/>
A: Nothing. We'll take care of that! If you have a datafeed (electronic price list) please let us know its URL and we will use that. Otherwise, we will use the wines and prices listed on your web site.<br/>
<br/>Q: Do I need to send updates when I have new wines?<br/>
A: There is no need to do that. We check the wines on your web site or datafeed automatically every 24 hours. All you need to do is make sure that your web site is up to date.<br/>
<br/>Q: How long does it take before my wines are listed?<br/>
A: This usually takes around a week.<br/> 
<br/>


<%@ include file="/snippets/footer.jsp" %>	
		</div></div> <%// workaround: IE positioning of footer %>
		</div> <!--  main--> 
<%} %>
	 <script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>
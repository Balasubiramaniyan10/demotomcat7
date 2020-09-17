<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Emailer"
	 
%>
<html>
<head>
<title>
Vinopedia data feeds
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Data Feed info page");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/topbar.jsp" %>
<div class='textpage'>
<%@ include file="/snippets/logoandsearch.jsp" %>


<div>
<h1>Data feeds</h1>
A data feed is a structured list of your products that can easily be read by a computer. 
<h2>Data feed formats</h2>
Vinopedia can read CSV (Comma Separated Values), XML and Excel feeds. Examples are shown below.<br/>
<br/>
A CSV file example<br/><img class='feedexample' src='/images/feedexamples/csv.gif'/><br/>
<br/>
An XML file example<br/><img class='feedexample' src='/images/feedexamples/xml.gif'/><br/>
<br/>
An Excel file example<br/><img class='feedexample' src='/images/feedexamples/Excel.gif'/>
<br/>
<h2>Generating a data feed</h2>
Comparison Shopping Engines like Yahoo! Shopping, Google Base, Shopping.com, PriceGrabber and also Vinopedia use data feeds to receive product information from merchants. It is a file containing information on all wines you are selling. Data feeds for Google Base, Shopping.com, MySimon.com and Yahoo! Shopping are all standard formats which Vinopedia can process. If you already have a data feed for other search engines, we can probable reuse it without the need for you to change anything. 
<br/>There is no need to create a data feed by hand: Most web shop software is able to generate one for all products listed in your shop. It is a matter of installing an extra package if it is not already installed. Some examples:
<ul>
<li>osCommerce has free packages for download (see for instance <a href="http://addons.oscommerce.com/info/6308" target="_blank">here</a> or <a href="http://www.oscommerce.com/community/contributions,4455" target="_blank">here</a>).</li>
<li>x-Cart can export to CSV files.</li>
<li>Zen-cart has free packages for download  (see <a href="http://www.zen-cart.com/index.php?main_page=product_contrib_info&cPath=40_60&products_id=473" target="_blank">here</a>).</li>
</ul>
(Note: if you have problems generating a data feed, please contact your Hosting Provider or Web Master. Vinopedia cannot help you as we don't know all the different types of webshop-software.)<br/>


<h2>Information needed</h2>
A data feed must contain at least the following information: 
<ul>
<li>Description of the wine, including all information needed to exactly identify a wine ("Meursault 1er Cru" or "Veuve Cliquot" are not accurate enough, because the producer and cuvee are missing)</li>
<li>Vintage. May be part of the description as shown below in the Excel example. If you stock more than one vintage list them as separate wines.</li>
<li>Bottle size (preferably like "75 cl" or "1,5 l." instead of "bottle" and "magnum")</li>
<li>URL where people can buy the wine. This must start with "http://"</li>
<li>Price per bottle</li>
<li>Optionally any other fields like appellation</li>
</ul>


<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
</div>
</body> 
</html>
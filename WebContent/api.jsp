<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.common.Configuration"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%PageHandler p=PageHandler.getInstance(request,response,"API documentation");%><!DOCTYPE HTML>
<head>
<title></title>
<meta name="keywords" content="API Documentation" />
<meta name="description" content="API Documentation" />
<%@ include file="/header2.jsp" %>
</head>
<body  onload="javascript:doonload();">
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<div class='main'>
	<div id='mainleft'>	
	<noscript><img src='/images/nojs.gif' alt=''/></noscript>
<h1>Vinopedia API documentation (experimental)</h1>
<br/>
<h2>Request structure</h2>
https://www.vinopedia.com/api/?action=&lt;action&gt;&amp;timestamp=&lt;timestamp&gt;&amp;clientid=&lt;clientid&gt;&amp;sig=&lt;signature&gt;&amp;param1=&lt;param1&gt;<br/>
All requests are done using GET. The URL must be properly encoded using UTF-8, i.e. Ch�teau d'Yquem becomes Ch%C3%A2teau+d%27Yquem. Accents may be omitted such that Ch�teau becomes Chateau.<br/><br/>
<h2>Response</h2>
Output is returned in JSON format with the UTF-8 character set. The response contains two objects: 
<ul>
<li>results: The content we found resulting from the request</li>
<li>object: Vinopedia tries to determine the correct wine or winery from the request parameters. The "object" states which object we are returning information about.</li>
</ul>
<h2>Common parameters</h2>
<h3>action</h3>
Currently 2 actions are supported: price and searchstats. See their explanations below.<br/>
<h3>timestamp</h3>
Current timestamp (milliseconds since midnight, January 1, 1970 UTC).<br/>
<h3>clientid</h3>
Identification of the user of the API. To be provided by Vinopedia.
<h3>sig</h3>
Signature of the message sent. <br/>
Sample code in Java to calculate the signature:<br/>
<code><br/>
	public static String getSignature(long time){<br/>
<dd>		//time can be generated with long time=System.currentTimeMillis();<br/>
		StringBuilder buff = new StringBuilder();<br/>
		buff.append("&lt;clientid&gt;").append("\n");<br/>
		buff.append("GET").append("\n");<br/>
		buff.append("&lt;secret&gt;").append("\n");<br/>
		buff.append(Long.toString(time)).append("\n");<br/>
		String src = buff.toString().toLowerCase(); <br/>
		String sig = DigestUtils.md5Hex(src);<br/>
		return sig;<br/>
</dd>	}<br/>
</code>
<br/>
<h2>Actions</h2><br/>
<h2>price</h2>
This returns the current lowest price and average price for a 0.75l. bottle of the requested wine. Prices always exclude sales tax or VAT. "average" price is actually calculated by determining the <a href='http://en.wikipedia.org/wiki/Median' alt='Median'>median</a> price, in order to prevent extremes prices and misinterpretation of the bottle or case size from influencing the result too much.<br/>
<br/><b>Input parameters</b>
<h3>name (mandatory)</h3>
Name of the wine. Must be as descriptive as possible, including name of the producer and region of origin if possible.<br/>
Example: name=Ch%C3%A2teau+d%27Yquem<br/>
<h3>vintage (optional)</h3>
The vintage of the wine. If omitted, minimum and average price is calculated over all vintages.<br/>
Example: vintage=2001<br/>
<h3>currency (optional)</h3>
ISO4217 code of currency used for the prices in the results. If omitted or not a valid value, prices are returned in Euro<br/>
Example: currency=USD<br/>
Supported values: USD,EUR,GBP,CHF,NOK,CZK,DKK,HUF,AUD,PLN,SEK,CAD,NZD,ZAR,SGD,JPY,HKD,CNY,MXN,BRL,RON
<br/>
<br/><b>Output</b><br/>
Sample output:<br/>
{"results":[{"offers":[188],"minimumprice":["333.47"],"averageprice":["780.00"],"stores":[71],"currency":["USD"]}],"object":[{"region":["France, Bordeaux, Sauternais, Sauternes"],"name":["Château d'Yquem"],"producer":["Château d'Yquem"],"type":["wine"],"url":["https://www.vinopedia.com/wine/Chateau+d%27Yquem+2001"]}]}
<br/><br/>results<br/>
<dd>
offers: number of prices found for this wine/vintage. Includes all prices, not just 0.75l. bottles.<br/>
minimumprice: lowest price found for a 0.75l. bottle<br/>
averageprice: median price found for a 0.75l. bottle<br/>
currency: ISO4217 code of currency used for the prices<br/>
</dd>
object
<dd>
region: name of the region of origin of the wine in a hierarchy, separated by ", "<br/>
name: full name of the wine we recognized<br/>
producer: name of the producer of the wine<br/>
type: type of the object, in this case "wine"<br/>
url: link to the relevant page on Vinopedia where price information can be found<br/>
</dd>
<br/><br/>
<h2>searchstats</h2>
This returns statistics on the number of unique visitors that have searched Vinopedia for wines of a given producer on a given date<br/>
<br/><b>Input parameters</b>
<h3>winery (mandatory)</h3>
Name of the winery for which statistics are requested. <br/>
Example: winery=Screaming+Eagle
<h3>startdate (optional)</h3>
Start date of the period for which data is requested. If omitted, statistics are given for the last full day of data (in other words, yesterday). Earliest date for which data can be retrieved is Januari 1st 2012.<br/>
Format: yyyy-MM-dd<br/>
Example: 2012-03-30<br/>
<h3>enddate (optional)</h3>
End date of the period for which data is requested. Must be on same day or after start date. If omitted, statistics are given for a single day (start date).<br/>
Format and example: see startdate.

<br/>
<br/><b>Output</b><br/>
Sample output:<br/>
{"results":[{"searchdata":[[{"wine":["Château Mouton Rothschild"],"date":["2012-01-01"],"searches":[45]},{"wine":["Château Mouton Rothschild Aile D'Argent"],"date":["2012-01-01"],"searches":[4]},{"wine":["Château Mouton Rothschild"],"date":["2012-01-02"],"searches":[36]},{"wine":["Château Mouton Rothschild Aile D'Argent"],"date":["2012-01-02"],"searches":[2]}]]}],"object":[{"type":["winery"],"url":["https://www.vinopedia.com/winery/Chateau+Mouton+Rothschild/"],"winery":["Château Mouton Rothschild"]}]}
<br/><br/>results<br/>
<dd>
searchdata: container for the array of statistics. Its value is an array of data, one element for each wine and day.<br/>
wine: name of the wine<br/>
date: date of the searches<br/>
searches: integer indicating the number of unique visitors that have searched for this wine<br/>
</dd>
object
<dd>
type: type of the object, in this case "winery"<br/>
url: link to the relevant page on Vinopedia where information about this winery can be found<br/>
winery: name of the winery<br/>
</dd>
<br/>

<%@ include file="/snippets/footer.jsp" %>	
		</div></div> <%// workaround: IE positioning of footer %>
		</div>
		</div> <!--  main--> 
<%} %>
	 <script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>
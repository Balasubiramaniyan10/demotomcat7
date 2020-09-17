<!DOCYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.*"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
</head><body>
<%@ page import = "com.freewinesearcher.online.*"	
 import = "com.freewinesearcher.common.*"	
 import = "com.freewinesearcher.batch.Coordinates"	%>
 
<%
StoreLocator sl=new StoreLocator();
try{sl.setProducer(Integer.parseInt(request.getParameter("id")));}catch(Exception e){}
if (sl.getProducer()>0) Dbutil.executeQuery("update kbproducers set demopage=now() where id="+sl.getProducer());
PageHandler p=PageHandler.getInstance(request,response,"Storelocatordemo "+sl.getProducer()); 

%>
<h1>Store locator for <%=sl.producername %> wines</h1>
Visitors to your website may be looking for retailers where they can buy your wine. Vinopedia is a search engine for wine that knows exactly who is selling your wines. We created a "Store Locator" widget that you can place on your own website, that shows your visitors who is selling your wines. <br/>
<h2>How it works</h2>
The widget is extremely simple and easy to use. We detect the location of the visitor and show all nearby retailers that carry your wine. When a visitor clicks on a store, we show which of your wines they have in stock. <br/>
By zooming out or dragging the map, they can look at availability in other locations.
<h2>Give it a try!</h2>
The box below is how it will appear on your web site. Use the zoom function to see the availability of your wines in Europe, the USA, Australia etc.<br/><br/>
<div id='storelocator' style='border:1px solid black;width:900px;height:500px;'>Loading store locator from <a href='https://www.vinopedia.com/winery/<%=Webroutines.URLEncodeUTF8Normalized(sl.producername).replaceAll("%2F", "/").replace("&", "&amp;") %>' target='_blank' id='vplink'>Vinopedia.com</a>
<noscript><h4><font color='red'>Javascript in currently disabled in your browser. In order to use the store locator you need to enable Javascript. </font></h4></noscript>
<script src='http://<%=(Configuration.serverrole.equals("DEV")?"test":"www") %>.vinopedia.com/js/injectstorelocator.jsp?id=<%=sl.getProducer() %>' ></script></div>
<h2>How to put this widget on your own web site</h2>
Well, this is quite easy: All you need to do is copy/paste the following piece of html-code into your website.<br/><br/>
<code>
&lt;div id='storelocator' style='border:1px solid black;width:900px;height:500px;'&gt;<br/>Loading store locator from &lt;a href='https://www.vinopedia.com/winery/<%=Webroutines.URLEncodeUTF8Normalized(sl.producername).replaceAll("%2F", "/").replace("&", "&amp;") %>' target='_blank' id='vplink'&gt;Vinopedia.com&lt;/a&gt;
&lt;noscript&gt;&lt;h4&gt;&lt;font color='red'&gt;Javascript in currently disabled in your browser. In order to use the store locator you need to enable Javascript. &lt;/font&gt;&lt;/h4&gt;&lt;/noscript&gt;
&lt;script src='https://www.vinopedia.com/js/injectstorelocator.jsp?id=<%=sl.getProducer() %>' &gt;&lt;/script&gt;&lt;/div&gt;

</code><br/><br/>
If you copy the code above into your own web site, the store locator will be part of your site in a minute. It is already customized to wines from <%=sl.producername %>. The first line allows customization of the store locator window. For example, you can change the size of the store locator window. The rest of the code needs to stay intact.
<br/><br/>
<h2>How does this work?</h2>
<a href='/'>Vinopedia.com</a> is a search engine for wine. Every day we track the inventory of thousands of wine stores world wide. We make this information freely available to wine buyers so they know where they can buy the wine they are looking for.<br/>
Our search engine recognizes individual wines from different producers, and as a result, we know who is selling the wines that you make. As soon as one of the stores decides to start selling one of your wines, this shows up on the map, usually within 24 hours. <br/>
If you find that stores are missing from the list, please feel free to let them know they too can get listed. They can <a href='https://www.vinopedia.com/retailers.jsp'>contact us</a> for more information.
</body></html><% p.logger.logaction();
%>
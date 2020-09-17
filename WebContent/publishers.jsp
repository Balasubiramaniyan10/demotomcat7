<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<%@ page contentType="text/html; charset=UTF-8" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>Integrating Vinopedia data on your site</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Publishers");%>
<%@ include file="/header.jsp" %>
<meta name="description" content="If you have a web site or forum about wine, we show you how to integrate Vinopedia data on your site." />
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head> 
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h1>Vinopedia mash-up</h1>
If you have a blog on wine or a wine related site, your visitors may be interested in what a wine costs. Vinopedia offers several very simple methods of listing live wine prices on your page as an extra service to your visitors.
<h2>Price quotes as a tool-tip</h2>
Suppose this is a tasting note you put on your web site:<br>
<i><h4 class='item fn'>Yquem 2001</h4>
Honey/golden colour. Impressive nose of apricot, papaya and nectarine. Silky flavours of dried pineapple, candied apricot, mango and papaya, finish of several minutes. </i><br><br>
If you move your mouse pointer over the wine name, you will see the pricing details of this wine. Always up-to-date, coming straight from our search engine, and without influencing the layout of your page. Adding tool-tip prices is a matter of putting a couple of lines of Javascript at the end of your web page:<br>
<textarea width="50%" style="width:100%;height:120px" name="code" wrap="logical" rows="6" cols="42">
<script type="text/javascript"><!--
var FWScolor='#c0f60F'; 
var FWStagname='item fn';
// -->
</script>
<script src="https://www.vinopedia.com/js/getinfo.js" type="text/javascript"></script></textarea>
<br><br>
The header of the tasting note looks like this: &lt;h4 class='item fn'&gt;Yquem 2001&lt;/h4&gt;. The script looks for HTML elements with a class name of 'item fn' and interprets everything within that element as the wine name and vintage. You can change the variable FWStagname to whatever you use as a class name for the wine name (the class name 'item fn' is a standard in <a href='http://wineformats.org/wiki/Tasting_note'>Wineformats</a>, which we recommend to use if you are publishing tasting notes). You can also change the variable FWScolor to match the color in the rest of your web page. You only need to include the javascript line once, and it will find all wines on that page.<br><br>
<b>Important: </b>You must put the Javascript code at the <i>end</i> of your page, otherwise it will not pick up all wines!
<h2>In-line price quotes</h2>
You can get the lowest and average price for a 0.75 l. bottle of a specific wine as an in-line quote. All you have to do is add a line like this:<br/>
<textarea width="50%" style="width:100%;height:35px" name="code" wrap="logical" rows="2" cols="42">
&lt;script type="text/javascript" src="https://www.vinopedia.com/price.jsp?name=Yquem 2001"/&gt;&lt;/script&gt;</textarea><br/>
Replace Yquem 2001 with any wine name with or without a year. Your visitors will see: <script type="text/javascript" src="https://www.vinopedia.com/price.jsp?name=Yquem 2001"/></script>. 
<h2>Miniature Search window</h2>
You can offer your visitors a miniature version of vinopedia straight onto your web site. All you have to do is to include a line of code into your web site. When users do a search, they do not navigate away from your page: the results are shown as part of your own web page.<br/>
<br/>
You can choose between two different sizes: normal and compact. Examples are shown below, try them out.<br/>
<table style="layout:fixed;width=400px;"><tr style="layout:fixed;width=400px;">
<td style="width:300px;"><script type="text/javascript" src="https://www.vinopedia.com/js/vinopedia.js"></script></td>
<td style="width:134px;"><div style="width:134px;"><script type="text/javascript" src="https://www.vinopedia.com/js/vinopediacompact.js"></script></div></td>
</tr></table>
To include the searches in your web page, enter these lines of code into the source HTML file:<br/>
Normal: <textarea width="50%" style="width:100%;height:35px" name="code" wrap="logical" rows="2" cols="42">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://www.vinopedia.com/js/vinopedia.js&quot;&gt;&lt;/script&gt;</textarea><br/>
Compact: <textarea width="50%" style="width:100%;height:35px" name="code" wrap="logical" rows="2" cols="42">
&lt;script type=&quot;text/javascript&quot; src=&quot;https://www.vinopedia.com/js/vinopediacompact.js&quot;&gt;&lt;/script&gt;</textarea>



</TD><TD class="right">
<script type="text/javascript"><!--
var FWScolor='#f1e7ec';
var FWStagname='item fn';
-->
</script>
<script src="https://www.vinopedia.com/js/getinfo.js" type="text/javascript" defer="defer"></script>
		
	</TD></TR>
</TABLE>	
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
<%

	if (hostcountry.equals("NZ")){
	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
	} else {%>
<br/><br/>
<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">
<h3>Integrate with Vinopedia on your site</h3>
If you have a blog or a wine related site, people are generally interested in what a wine costs. Vinopedia offers several very simple methods of getting live wine prices on your page, in a way that is non-intrusive.<br><br>
<h3>Price quotes as a tool-tip</h3>
Suppose this is a tasting note you put on your web site:<br>
<i><h4 class='item fn'>Yquem 2001</h4>
Honey/golden colour. Impressive nose of apricot, papaya and nectarine. Silky flavours of dried pineapple, candied apricot, mango and papaya, finish of several minutes. </i><br><br>
If you move your mouse pointer over the wine name, you will see the pricing details of this wine. Always up-to-date, coming straight from our search engine, and without influencing the layout of your page. Adding tool-tip prices is a matter of putting a couple of lines of Javascript at the end of your web page:<br>
<textarea width="50%" style="width:100%;height:120px" name="code" wrap="logical" rows="6" cols="42">
<script type="text/javascript"><!--
var FWScolor='#c0f60F';
var FWStagname='item fn';
// -->
</script>
<script src="https://www.vinopedia.com/js/getinfo.js" type="text/javascript"></script></textarea>
<br><br>
The header of the tasting note looks like this: &lt;h4 class='item fn'&gt;Yquem 2001&lt;/h4&gt;. The script looks for HTML elements with a class name of 'item fn' and interprets everything within that element as the wine name and vintage. You can change the variable FWStagname to whatever you use as a class name for the wine name (the class name 'item fn' is a standard in <a href='http://wineformats.org/wiki/Tasting_note'>Wineformats</a>, which we recommend to use if you are publishing tasting notes). You can also change the variable FWScolor to match the color in the rest of your web page. You only need to include the javascript line once, and it will find all wines on that page.<br><br>
<b>Important: </b>You must put the Javascript code at the <i>end</i> of your page, otherwise it will not pick up all wines!
<H3>In-line price quotes</H3>
You can get the lowest and average price for a 0.75 l. bottle of a specific wine as an in-line quote. All you have to do is add a line like this:<br/>
<i>&lt;script type="text/javascript" src="https://www.vinopedia.com/price.jsp?name=Yquem 2001"/&gt;&lt;/script&gt;</i><br/>
Replace Yquem 2001 with any wine name with or without a year. Your visitors will see: <script type="text/javascript" src="https://www.vinopedia.com/price.jsp?name=Yquem 2001"/></script>. 
<h3>Miniature Search window</h3>
You can offer your visitors a miniature version of Vinopedia straight onto your web site. All you have to do is to include a line of code into your web site. When users do a search, they do not navigate away from your page: the results are shown as part of your own web page.<br/>
<br/>
You can choose between two different sizes: normal and compact. Examples are shown below, try them out.<br/> 
<table style="layout:fixed;width=400px;"><tr style="layout:fixed;width=400px;">
<td style="width:300px;"><script type="text/javascript" src="https://www.vinopedia.com/js/FWS.js"></script></td>
<td style="width:134px;"><div style="width:134px;"><script type="text/javascript" src="https://www.vinopedia.com/js/FWScompact.js"></script></div></td>
</tr></table>
To include the searches in your web page, enter these lines of code into the source HTML file:<br/>
Normal: <i>&lt;script type=&quot;text/javascript&quot; src=&quot;https://www.vinopedia.com/js/vinopedia.js&quot;&gt;&lt;/script&gt;</i><br/>
Compact: <i>&lt;script type=&quot;text/javascript&quot; src=&quot;https://www.vinopedia.com/js/vinopediacompact.js&quot;&gt;&lt;/script&gt;</i>



</TD><TD class="right">
<script type="text/javascript"><!--
var FWScolor='#c0f0f0';
var FWStagname='item fn';
-->
</script>
<script src="https://www.vinopedia.com/js/getinfo.js" type="text/javascript" defer="defer"></script>
		
	</TD></TR>
</TABLE>	
<jsp:include page="/footer.jsp" />

</div>
<%} %>
<%} %>
</body> 
</html> 
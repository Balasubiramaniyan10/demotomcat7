<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ 
	page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" 
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
Getting listed on Vinopedia
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Retailers");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<meta name="description" content="Information for wine retailers to get listed in Vinopedia.com." />
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
<%@ include file="/snippets/textpage.jsp" %>

<%
	
    	ArrayList<String> countries = Webroutines.getCountries();
    	ArrayList<String> currencies = Webroutines.getCurrency();
    	Emailer emailer=new Emailer();
    	String companyname = request.getParameter("companyname");
    	String address = request.getParameter("address");
    	String name = request.getParameter("name");
    	String country = request.getParameter("country");
    	String email = request.getParameter("email");
    	String siteurl = request.getParameter("siteurl");
    	String datafeedurl = request.getParameter("datafeedurl");
    	String currency = request.getParameter("currency");
    	String remarks = request.getParameter("remarks");
    	String vat = request.getParameter("vat");
    	String submit = request.getParameter("submit");
    	String terms= request.getParameter("terms");
    	String link= request.getParameter("link");
    	boolean complete=true;
    	if (companyname==null) companyname="";
    	if (address==null) address="";
    	if (name==null) name="";
    	if (country==null) country="";
    	if (email==null) email="";
    	if (siteurl==null) siteurl="";
    	if (datafeedurl==null) datafeedurl="";
    	if (currency==null) currency="";
    	if (remarks==null) remarks="";
    	if (vat==null) vat="";
    	if (submit==null) submit="";
       	if (terms==null) terms="";
       	if (link==null) link="";
           	
    	if (companyname.equals("")) complete=false;
    	if (name.equals("")) complete=false;
    	if (country.equals("")) complete=false;
    	if (email.equals("")) complete=false;
    	if (siteurl.equals("")) complete=false;
    	if (datafeedurl.equals("")) complete=false;
    	if (currency.equals("")) complete=false;
    	if (vat.equals("")) complete=false;
    	if (datafeedurl.equals(siteurl)) complete=false;
    	if (!terms.equals("on")) complete=false;
    	if (!link.equals("on")) complete=false;
    	if (submit.equals("true")&&complete){

    	String HTMLmessage="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/TR/html4/loose.dtd\"><html><body><table>";
    	HTMLmessage=HTMLmessage+"<tr><td>Company Name:</td><td>"+companyname+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Company Address:</td><td>"+address+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Name:</td><td>"+name+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Country:</td><td>"+country+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Email:</td><td>"+email+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Site URL:</td><td><a href='"+siteurl+"'>"+siteurl+"</a></td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Data feed URL:</td><td><a href='"+datafeedurl+"'>"+datafeedurl+"</a></td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Currency:</td><td>"+currency+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>VAT Included:</td><td>"+vat+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Remarks:</td><td>"+remarks+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Terms accepted:</td><td>"+terms+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Will place link:</td><td>"+link+"</td></tr>";
    	HTMLmessage=HTMLmessage+"</table>";
    	HTMLmessage=HTMLmessage+"<a href='https://www.vinopedia.com/moderator/editshop.jsp?shopname="+Webroutines.URLEncode(companyname)+"&address="+Webroutines.URLEncode(address)+"&countrycode="+Webroutines.URLEncode(country)+"&email="+Webroutines.URLEncode(email)+"&shopurl="+Webroutines.URLEncode(siteurl)+"&datafeedurl="+Webroutines.URLEncode(datafeedurl)+"&currency="+Webroutines.URLEncode(currency)+"&vat="+Webroutines.URLEncode(vat)+"'>Add shop to Vinopedia</a>";
    	HTMLmessage=HTMLmessage+"</body></html>";
    					
    	if (emailer.sendEmail(email,"storeadmin@vinopedia.com","Listing "+companyname+" on Vinopedia.com",HTMLmessage)){
    	out.write("<br/><br/>Your request has been received, please allow 2 weeks for processing it. <br/>");
    	} else {
    	out.write("<br/><br/>Sorry... Something went wrong while trying to send your message. Please try again later.<br/>");
    	
    	} 
    	} else {
%>

<h1>Get your store listed</h1>
Every day thousands of people visit us to find out where to buy their wine. Our visitors can be your new customers. Any store selling wine is welcome on Vinopedia.com. Getting listed is very easy. Send us an <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;&#109;&#97;&#110;&#97;&#103;&#101;&#109;&#101;&#110;&#116;&#64;&#118;&#105;&#110;&#111;&#112;&#101;&#100;&#105;&#97;&#46;&#99;&#111;&#109;'>email</a> to find out how to get your store listed a.s.a.p. and to see our advertisement opportunities.<br/>
<br/>
Kind regards,<br/>
<br/>
<div style='float:left;clear:both;margin:20px;'>
<div style='margin-left:60px;'>Jasper Hammink</div>
<img style='height:350px;' src="/images/Jasper.jpg" alt='Jasper Hammink'/>
</div>
<div style='float:left;margin:20px;'>
<div style='margin-left:70px;'>Jeroen Starrenburg</div>
<img style='height:350px;' src="/images/Jeroen.jpg" alt='Jeroen Starrenburg'/>
</div>
<div class='clear'>
PS: If you ever wonder why search engines are important for your business, did you know that:<br/>
<ul>
<li>87% use search engines to find websites</li> 
<li>88% of all e-commerce transactions start with a search engine request</li>
</ul>
(source: Worldwide Search market overview, comScore qSearch)<br/><br/>
</div>
<div class='clear'>Please feel free to contact us at any time on: <br/><br/>
<a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;&#109;&#97;&#110;&#97;&#103;&#101;&#109;&#101;&#110;&#116;&#64;&#118;&#105;&#110;&#111;&#112;&#101;&#100;&#105;&#97;&#46;&#99;&#111;&#109;'><img src="/images/em.png" style='height:55px;'/></a>
<a href="skype:vinopediacom?call"><img src="/images/skypecall.jpg"  style='height:60px;'/></a>
<a href="skype:vinopediacom?chat"><img src="/images/skypetext.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://www.youtube.com/user/vinopediadotcom"><img src="/images/youtube.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://twitter.com/vinopediacom"><img src="/images/twitter.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://www.facebook.com/#!/pages/Vinopedia/154789861969?ref=ts"><img src="/images/facebook.jpg"   style='height:60px;'/></a>
<a target='_blank' href="http://www.linkedin.com/groups?gid=2291547&trk=myg_ugrp_ovr"><img src="/images/linkedin.jpg"   style='height:60px;'/></a>
</div>
<%if (false){ %>
<h2>Free and easy</h2>
If you are a wine merchant in the US, Canada or Europe and you are selling and shipping to customers, you too can list your wines on Vinopedia. Until further notice, a listing on Vinopedia is free and there are no plans to start charging for this. The only thing we require is for you to place a link back to www.vinopedia.com on your web site. 
<h2>Get listed in four steps</h2>
You can add your wines to Vinopedia yourself! Just follow this four-step process.<br/>
<div class='feedexamples'>
<div>
<img class='feedexamples' src='/images/feedexamples/1feed.gif'/>
<h3>Generate a data feed</h3>
Most web shops can export a list of products and prices for search engines, a so-called data feed. Vinopedia can process most formats, including Excel, CSV (Comma Separated Values), and many types of XML. You may have to install an extra module in your web shop software to be able to generate a data feed. For details, see the <a href='/datafeed.jsp'>data feed information page</a>.
</div><div>
<img class='feedexamples' src='/images/feedexamples/2link.gif'/>
<h3>Place a link to Vinopedia</h3>
We ask a link back to Vinopedia.com on your web site. This can be a text link or an image link. See <a href='/links.jsp#linktovp'>the links page</a> for details.
</div><div>
<img class='feedexamples' src='/images/feedexamples/3feedrecognized.gif'/>
<h3>Test your data feed</h3>
Once you have a data feed, go to <a href='/settings/editdatafeed.jsp'>the data feed test page</a> (if you have not done so already, you need to <a href="/forum/user/insert.page">create a user account</a> first). Here you can configure and test your feed to make sure all your wines are listed correctly. You may need to change a few settings so the search engine knows where to find the information in your feed.
</div><div>
<img class='feedexamples' src='/images/feedexamples/4shopinfo.gif'/>
<h3>Enter your company information</h3>
Next, you need to enter the details of your company such as name, address, contact details, and pricing details such as VAT and currency. The link to Vinopedia.com is checked here as well and you need to agree with our <a href='/siteindexationagreement.jsp' target='_blank'>Site indexation agreement</a>.
</div>
</div>
&nbsp;<br/>That's all. Vinopedia will download the wine prices from your data feed automatically, every day. Your wines should be listed on Vinopedia within 24 hours of submission. If you run into trouble, <a href='contact.jsp'>leave us a message</a> and we will help you.
<br/>&nbsp;<br/>
<h2>If you cannot supply a data feed</h2>
You may not be able to supply a data feed as described above, for instance because you do not use web shop software that can produce a feed. We may still be able to include your wines on Vinopedia by getting the prices from your web site. <a href='/htmllisting.jsp'>For more information, click here</a>.
<h2>Price list requirements</h2>
<h3>Accuracy</h3>
We want to keep the prices on vinopedia as accurate as possible. The price list you supply must be accurate and up-to-date, accessible from the Internet without having to log in, and the prices mentioned must be the real prices and the same prices as on your web site. In principle, you must be able to supply the listed wines. If we receive repetitive complaints that listed wines cannot be supplied, we will remove the complete listing to prevent listing of "spectacular offers" that are only meant to attract visitors. 
<h3>Tip: correct wine descriptions</h3>
Vinopedia uses a wine recognition system that tries to recognize the exact wine you are offering. If the wine description is incomplete or contains spelling mistakes, it cannot correctly determine the exact wine and your wine may not be listed in the search results. <br/><br/>For instance, suppose you sell a Château Margaux 1990, but you list it as "Margaux 1990". "Margaux" could be any wine from the Margaux region. Without the "Château" it will not be recognized (although wines where there can be no confusion will still be recognized without the "Château" keyword, for instance "Mouton Rothschild" will be recognized correctly). Other examples:
<table>
<tr><th>Problematic description</th><th>Reason</th><th>Correct description</th></tr>
<tr><td>Le Montrachet</td><td>There are more than one producers of Le Montrachet</td><td>Le Montrachet Vincent Girardin</td></tr>
<tr><td>Pahlmeyer California red wine</td><td>Could be the Merlot, Pinot Noir or the proprietary red</td><td>Pahlmeyer merlot</td></tr>
<tr><td>Château Bel-Air</td><td>There are 10 different Château Bel-Air's</td><td>Château Bel-Air St. Estèphe</td></tr>
</table><br/>
So be as precise as possible in the wine description to avoid confusion and to make sure your wines can be found.
<%} %>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } %>
</body> 
</html>
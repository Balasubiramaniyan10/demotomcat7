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
Retailers
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Retailers");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","2"); %>
<%@ include file="/snippets/textpage.jsp" %>
<form action="retailers.jsp" method="post">

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

<h1>Getting listed</h1>
<h2>Free and easy</h2>
Getting listed on vinopedia is easy and free. If you are a wine merchant in Europe and you are selling and shipping to customers throughout Europe, you too can have your wines listed on vinopedia. Until further notice, a listing on vinopedia is free and there are no plans to start charging for this. The only thing we require is for you to place a link back to www.vinopedia.com on your web site. If you prefer an image link to a text link, you can use one of these two images:<br/><br/>
<img src='https://www.vinopedia.com/images2/Listed-on-Vinopedia.gif' /><br/>
html code: <br/><code>&lt;a href='https://www.vinopedia.com' target='_blank' style:'border-style: none;'&gt;&lt;img src='https://www.vinopedia.com/images2/Listed-on-Vinopedia.gif' style:'border-style: none;' /&gt;&lt;/a&gt; <br/><img src='https://www.vinopedia.com/images2/Listed-on-Vinopedia-small.gif' /></code><br/> 
html code: <br/><code>&lt;a href='https://www.vinopedia.com' target='_blank' style:'border-style: none;'&gt;&lt;img src='https://www.vinopedia.com/images2/Listed-on-Vinopedia-small.gif'  style:'border-style: none;'/&gt;&lt;/a&gt;</code><br/><br/> 
<h2>How to supply wine prices</h2>
Vinopedia uses a web robot ("spider") that downloads price information from retailers every day. All we need is to have access to a single list that contains all of your wines and their prices. We support the following formats: 
<ul>
<li>Data feed in CSV format</li>
A data feed is a list of items meant to be read by a computer, instead of a human being. CSV (Comma Separated Value) is a plain text file containing all wines you are selling. If you already have a data feed for any other search engine, we can probably reuse it without the need for you to change anything. For more information on data feeds, see <a href='/datafeed.jsp'>this page</a>.
<li>Data feed in XML format</li>
Many of the popular web shop systems have the option to export your product list in XML format. You may have to install an extra module for it (check with your supplier).
<li>Elmar</li>
Elmar is a standard for exporting product information for search engines. Your web shop probably supports this standard if you install an extra module (see <a href='http://projekt.wifo.uni-mannheim.de/elmar/nav?dest=impl.shopsystems.index&rid=26' target='_blank'>the Elmar home page</a> for details. If you have installed Elmar you can add your shop and products to Vinopedia yourself. 
<li>HTML</li>
This could simply be the price list on your web site. All wines must be listed on a single page. If you have a search function that shows all of your wines on one page, perfect. If your wines are listed on multiple pages (for instance per region) we are happy to give you a price quote for making a custom spider that downloads prices from all of these web pages.
<li>Excel file</li>
We can also process most Excel files (up to Excel 2003). A restriction is that the information on a single wine cannot be spread over several rows (so 1 wine is 1 row).
<li>Other formats</li>
We cannot proccess any other price list formats (like PDF files). </ul>
<h2>Other requirements</h2>
We want to keep the prices on vinopedia as accurate as possible. The price list you supply must be accurate and up-to-date, accessible from the Internet without having to log in, and the prices mentioned must be the real prices and the same prices as on your web site. In principle, you must be able to supply the listed wines. If we receive repetitive complaints that listed wines cannot be supplied, we will remove the complete listing to prevent listing of "spectacular offers" that are only meant to attract visitors. 
<h2>Tip: correct wine descriptions</h2>
Vinopedia uses a wine recognition system that tries to recognize the exact wine you are offering. If the wine description is incomplete or contains spelling mistakes, it cannot correctly determine the exact wine and your wine may not be listed in the search results. <br/><br/>For instance, suppose you sell a Château Margaux 1990, but you list it as "Margaux 1990". "Margaux" could be any wine from the Margaux region. Without the "Château" it will not be recognized (although wines where there can be no confusion will still be recognized without the "Château" keyword, for instance "Mouton Rothschild" will be recognized correctly). Other examples:
<table>
<tr><th>Problematic description</th><th>Reason</th><th>Correct description</th></tr>
<tr><td>Le Montrachet</td><td>There are more than one producers of Le Montrachet</td><td>Le Montrachet Vincent Girardin</td></tr>
<tr><td>Pahlmeyer California red wine</td><td>Could be the Merlot, Pinot Noir or the proprietary red</td><td>Pahlmeyer merlot</td></tr>
<tr><td>Château Bel-Air</td><td>There are 10 different Château Bel-Air's</td><td>Château Bel-Air St. Estèphe</td></tr>
</table><br/>
So be as precise as possible in the wine description to avoid confusion and to make sure your wines can be found.
<h2>To get a listing</h2>
Please fill out the following form. If you have any questions, please put them in the Remarks field.<br/>
<br/>
<%if (!complete&&submit.equals("true")){
	out.write ("<font color='red'>One or more fields are incorrect or missing. Please check the red fields.</font><br/>");
	if (datafeedurl.equals(siteurl)){
		out.write("<font color='red'>Note: The price list URL must be a real price list, not just the URL of the web site.");
	}
}%>
    <TABLE style='width:100%;'>
		<TR><TD><font <%if(companyname.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Name of your company</font></TD><TD><INPUT TYPE="TEXT" NAME="companyname" VALUE="<%=companyname%>" size=50></TD></TR>
		<TR><TD>Full Address of your company</TD><TD><INPUT TYPE="TEXT" NAME="address" VALUE="<%=address%>" size=50></TD></TR>
		<TR><TD><font <%if(country.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Country of your company</font></TD><TD><select name="country" >
		<option value="">Please select
<% for (int i=0;i<countries.size();i=i+2){%>
<option value="<%=countries.get(i)%>" <%if (countries.get(i).equals(country)) out.write("Selected");  %>><%=countries.get(i+1)%>
<%}%>
</select></TD></TR>
		<TR><TD><font <%if(name.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Your name</font></TD><TD><INPUT TYPE="TEXT" NAME="name"  VALUE="<%=name%>" size=50></TD></TR>
		<TR><TD><font <%if(email.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Email address</font></TD><TD><INPUT TYPE="TEXT" NAME="email"  VALUE="<%=email%>"  size=50></TD></TR>
		<TR><TD><font <%if(siteurl.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>URL of your company's web site</font></TD><TD><INPUT TYPE="TEXT" NAME="siteurl"  VALUE="<%=siteurl%>"  size=50></TD></TR>
		<TR><TD><font <%if((datafeedurl.equals("")||datafeedurl.equals(siteurl))&&submit.equals("true")) {out.write(" color='red'");} %>>URL of the price list or data feed</font></TD><TD><INPUT TYPE="TEXT" NAME="datafeedurl"  VALUE="<%=datafeedurl%>"  size=50></TD></TR>
		<TR><TD><font <%if(currency.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Currency of the prices</font></TD><TD><select name="currency" >
		<option value="">Please select
<% for (int i=0;i<currencies.size();i++){%>
<option value="<%=currencies.get(i)%>" <%if (currencies.get(i).equals(currency)) out.write("Selected");  %>><%=currencies.get(i)%>
<%}%>
</select></TD></TR>
		<TR><TD><font <%if(vat.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Is VAT (sales tax) included in the prices?</font></TD><TD><SELECT NAME="vat" ><OPTION VALUE="">Please select<OPTION VALUE="Y" <%if (vat.equals("Y")) out.write(" selected "); %>>Yes<OPTION VALUE="N" <%if (vat.equals("N")) out.write(" selected "); %>>No</SELECT></TD></TR>
		<TR><TD valign=top>Remarks</TD><TD><TEXTAREA rows=6 cols=38  NAME="remarks" VALUE="<%=remarks%>" ></TEXTAREA></TD></TR>
		<%if(!terms.equals("on")&&submit.equals("true")) {out.write("<tr><td colspan='2'><font color='red'>You must accept the terms to get listed.</font></td></tr>");} %>
		<TR><TD>I accept the <a href='/siteindexationagreement.jsp' target='_blank'>terms and conditions</a></TD><TD><INPUT TYPE="checkbox" NAME="terms" <%=("on".equals(terms)?"checked='checked' ":"")%>></TD></TD></TR>
		<%if(!link.equals("on")&&submit.equals("true")) {out.write("<tr><td colspan='2'><font color='red'>You must agree to place a link back to vinopedia.com on your site.</font></td></tr>");} %>
		<TR><TD>I will place a link back to vinopedia on my web site </TD><TD><INPUT TYPE="checkbox" NAME="link" <%=("on".equals(link)?"checked='checked' ":"")%>></TD></TD></TR>
		<TR><TD></TD><TD><INPUT TYPE="SUBMIT" VALUE="Send!"></TD></TR>
	</TABLE>
<INPUT type="hidden" name="submit" value="true">
	</FORM>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } %>
</body> 
</html>
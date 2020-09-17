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
Getting listed on Vinopedia without data feed
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Retailers");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
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
    	HTMLmessage=HTMLmessage+"<tr><td>Price list URL:</td><td><a href='"+datafeedurl+"'>"+datafeedurl+"</a></td></tr>";
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

<h1>Getting listed on Vinopedia without a data feed</h1>
The preferred way of getting listed on Vinopedia.com is through a <a href='/retailers.jsp'>data feed</a>. If you cannot supply one, we may be able to get the wine prices from your web site.<br/>
<h2>Price list on one web page</h2>
If you have a web page that lists all your wine price on one page, we will try to use those prices. Also if you have a search function that lists all your wines on a single page we can probably use that list. This is usually free of charge.
<h2>Price list on multiple pages</h2>
If you have a web site where wines are listed on multiple pages (for instance grouped by color or appellation) we can "spider" all pages and take the wine prices from them. Depending on the structure and complexity of your web site we will charge you for the creation of such a spider. We will tell you the price beforehand (usually between &euro;100 and &euro;300). Please read our <a href='/siteindexationagreement.jsp' target='_blank'>site indexation agreement</a> carefully because it addresses issues like changes on your web site and adaption of the spider to them.
<h2>To get a listing</h2>
Please fill out the following form. If you have any questions, please put them in the Remarks field.<br/>
<br/>
<%if (!complete&&submit.equals("true")){
	out.write ("<font color='red'>One or more fields are incorrect or missing. Please check the red fields.</font><br/>");
	if (datafeedurl.equals(siteurl)){
		out.write("<font color='red'>Note: The price list URL must be a real price list, not just the URL of the web site.");
	}
}%>
<form action="retailers.jsp" method="post">
    <TABLE style='width:100%;'>
		<TR><TD width="300"><font <%if(companyname.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Name of your company</font></TD><TD><INPUT TYPE="TEXT" NAME="companyname" VALUE="<%=companyname%>" size=50></TD></TR>
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
		<TR><TD><font <%if((datafeedurl.equals("")||datafeedurl.equals(siteurl))&&submit.equals("true")) {out.write(" color='red'");} %>>URL of the price list</font></TD><TD><INPUT TYPE="TEXT" NAME="datafeedurl"  VALUE="<%=datafeedurl%>"  size=50></TD></TR>
		<TR><TD><font <%if(currency.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Currency of the prices</font></TD><TD><select name="currency" >
		<option value="">Please select
<% for (int i=0;i<currencies.size();i++){%>
<option value="<%=currencies.get(i)%>" <%if (currencies.get(i).equals(currency)) out.write("Selected");  %>><%=currencies.get(i)%>
<%}%>
</select></TD></TR>
		<TR><TD><font <%if(vat.equals("")&&submit.equals("true")) {out.write(" color='red'");} %>>Is VAT (sales tax) included in the prices?</font></TD><TD><SELECT NAME="vat" ><OPTION VALUE="">Please select<OPTION VALUE="Y" <%if (vat.equals("Y")) out.write(" selected "); %>>Yes<OPTION VALUE="N" <%if (vat.equals("N")) out.write(" selected "); %>>No</SELECT></TD></TR>
		<TR><TD valign=top>Remarks (if a search function on your site can show all wines, please tell us about it)</TD><TD><TEXTAREA rows=6 cols=38  NAME="remarks" VALUE="<%=remarks%>" ></TEXTAREA></TD></TR>
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
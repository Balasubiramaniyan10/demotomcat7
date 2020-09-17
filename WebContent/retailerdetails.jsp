<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.common.Configuration"
import = "com.freewinesearcher.batch.Emailer"
import = "java.util.ArrayList"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%
int option=0;
try{option=Integer.parseInt(request.getParameter("option"));}catch(Exception e){}

PageHandler p=PageHandler.getInstance(request,response,"Retailer details option "+option);%><!DOCTYPE HTML>
<head>
<title>Retailerdetails</title>
<meta name="keywords" content="How to get listed as a retailer" />
<meta name="description" content="How to get listed as a retailer" />
<%@ include file="/header2.jsp" %>
</head>
<body  onload="javascript:doonload();">
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<div class='main'>
	<div id='mainleft'>	
	<noscript><img src='/images/nojs.gif' alt=''/></noscript>




<%
	
    	ArrayList<String> countries = Webroutines.getCountries();
    	ArrayList<String> currencies = Webroutines.getCurrency();
    	Emailer emailer=new Emailer();
    	String companyname = request.getParameter("companyname");
    	String address = request.getParameter("address");
    	String name = request.getParameter("name");
    	String country = request.getParameter("country");
    	String email = request.getParameter("email");
    	String contactemail=request.getParameter("contactemail");
    	String siteurl = request.getParameter("siteurl");
    	String linkbackurl = request.getParameter("linkbackurl");
    	String datafeedurl = request.getParameter("datafeedurl");
    	String currency = request.getParameter("currency");
    	String remarks = request.getParameter("remarks");
    	String vat = request.getParameter("vat");
    	String submit = request.getParameter("submit");
    	String terms= request.getParameter("terms");
    	String link= request.getParameter("link");
    	String subject="";
    	String selectedoptiontext="";
    	
	
    	
    	boolean complete=true;
    	if (companyname==null) companyname="";
    	if (address==null) address="";
    	if (name==null) name="";
    	if (country==null) country="";
    	if (email==null) email="";
    	if (contactemail==null) contactemail="";
    	if (siteurl==null) siteurl="";
    	if (linkbackurl==null) linkbackurl="";
    	if (datafeedurl==null) datafeedurl="";
    	if (currency==null) currency="";
    	if (remarks==null) remarks="";
    	if (vat==null) vat="";
    	if (submit==null) submit="";
       	if (terms==null) terms="";
       	if (link==null) link="";
       	if (option==1){
       		selectedoptiontext="Free Listing";
		} else if (option==2){
			selectedoptiontext="Basic Listing";	
		} else if (option==3){
			selectedoptiontext="Sponsored Listing";
		} 
       	
       	if (option<1||option>3) complete=false;
       	if (companyname.equals("")) complete=false;
       	if (name.equals("")) complete=false;
       	if (address.equals("")) complete=false;
    	if (country.equals("")) complete=false;
    	if (email.equals("")) complete=false;
    	if (!"".equals(email)&&(email.indexOf("@")<1||email.indexOf(" ")>=0)) complete=false;
    	if (contactemail.equals("")) complete=false;
    	if (!"".equals(contactemail)&&(contactemail.indexOf("@")<1||contactemail.indexOf(" ")>=0)) complete=false;
    		
    	if (siteurl.equals("")) complete=false;
    	if (currency.equals("")) complete=false;
    	if (vat.equals("")) complete=false;
    	if (!terms.equals("on")) complete=false;
    	if (option==1&&linkbackurl.equals("")) complete=false;
    	if (!siteurl.equals("")&&!siteurl.startsWith("http")) siteurl="http://"+siteurl;
    	if (!datafeedurl.equals("")&&!datafeedurl.startsWith("http")) datafeedurl="http://"+datafeedurl;
    	if (!linkbackurl.equals("")&&!linkbackurl.startsWith("http")) linkbackurl="http://"+linkbackurl;
		if (submit.equals("true")&&complete){
			p=PageHandler.getInstance(request,response,"Retailer request sent option "+option);
			String requestdate="";
			Calendar cal = GregorianCalendar.getInstance();
			requestdate = new Timestamp(cal.getTime().getTime()).toString();
			
		subject=selectedoptiontext+" of "+companyname+" on Vinopedia.com";
    	String HTMLmessage="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/TR/html4/loose.dtd\"><html><body>";
    	HTMLmessage=HTMLmessage+"<a href='https://www.vinopedia.com/moderator/editshop.jsp?shopname="+Webroutines.URLEncode(companyname)+"&amp;address="+Webroutines.URLEncode(address)+"&amp;countrycode="+Webroutines.URLEncode(country)+"&amp;email="+Webroutines.URLEncode(email)+"&amp;invoiceemail="+Webroutines.URLEncode(contactemail)+"&amp;sponsoringshop="+(option==1?"0":"1")+"&amp;contactname="+Webroutines.URLEncode(name)+"&amp;commercialcomment="+Webroutines.URLEncode(name+" selected option "+selectedoptiontext+" on "+requestdate)+"&amp;shopurl="+Webroutines.URLEncode(siteurl)+"&amp;datafeedurl="+Webroutines.URLEncode(datafeedurl)+"&amp;currency="+Webroutines.URLEncode(currency)+"&amp;vat="+Webroutines.URLEncode(vat)+"&amp;linkback="+Webroutines.URLEncode(linkbackurl)+"'>Click here to add shop to Vinopedia</a><br/><br>";
    	HTMLmessage=HTMLmessage+"<table><tr><td>Company Name:</td><td>"+companyname+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Company Address:</td><td>"+address+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Name:</td><td>"+name+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Country:</td><td>"+country+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Email:</td><td>"+email+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Contact Email:</td><td>"+contactemail+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Site URL:</td><td><a href='"+siteurl+"'>"+siteurl+"</a></td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Data feed URL:</td><td><a href='"+datafeedurl+"'>"+datafeedurl+"</a></td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Currency:</td><td>"+currency+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>VAT Included:</td><td>"+vat+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Remarks:</td><td>"+remarks+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Terms accepted:</td><td>"+terms+"</td></tr>";
    	HTMLmessage=HTMLmessage+"<tr><td>Will place link:</td><td>"+link+"</td></tr>";
    	HTMLmessage=HTMLmessage+"</table>";
    	HTMLmessage=HTMLmessage+"</body></html>";
    	
    	boolean mailsent=false;
    	if (option==1) mailsent=emailer.sendEmail(email,"freelistings@vinopedia.com",subject,HTMLmessage);
    	if (option==2) mailsent=emailer.sendEmail(email,"basiclistings@vinopedia.com",subject,HTMLmessage);
    	if (option==3) mailsent=emailer.sendEmail(email,"sponsoredlistings@vinopedia.com",subject,HTMLmessage);
    	if (mailsent&&(option==1||option==2)&&!"".equals(Configuration.chinaemail)) {
    			emailer.sendEmail("management@vinopedia.com",Configuration.chinaemail,subject,HTMLmessage);
    	}
    	if (mailsent) {out.write("<br/><br/>Thank you for your interest in Vinopedia. We look forward to listing your store. Please allow us around 5 business days to finalize the listing. We will contact you as soon as possible. <br/><br/>Kind regards,<br/<br/>The Vinopedia team.  	");
    	} else {
    	out.write("<br/><br/>Sorry... Something went wrong while trying to send your message. Please try again later.<br/>");
    	
    	} 
    	} else {

if (1==1){%>
	<h1>New listings suspended</h1>
	Sorry... We currently cannot process any new listing requests on Vinopedia. When this situation changes we will update this page to allow for registration.<br/><br/>Kind regards,<br/>Vinopedia management
<% } else  {
%>

<h1>Your company details</h1>

 
You selected <%=selectedoptiontext%>. All we need to know now are your company details. If you have any questions, please put them in the Remarks field.<br/>
<br/>
<%if (!complete&&submit.equals("true")){
	out.write ("<span style='color:red'>One or more fields are incorrect or missing. Please check the red fields.</span><br/>");
	if (datafeedurl.equals(siteurl)&&!"".equals(datafeedurl)){
		out.write("Note: The datafeed URL must be a complete price list, not just the URL of the web site.");
	}
}%>

<form action="retailerdetails.jsp" method="post">
    <TABLE style='width:100%;'>
		<TR><TD><span style='<%if(companyname.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Name of your company</span></TD><TD><INPUT TYPE="TEXT" NAME="companyname" VALUE="<%=companyname%>" size=50></TD></TR>
		<TR><TD><span style='<%if(address.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Full Address of your company</span></TD><TD><INPUT TYPE="TEXT" NAME="address" VALUE="<%=address%>" size=50></TD></TR>
		<TR><TD><span style='<%if(country.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Country of your company</span></TD><TD><select name="country" >
		<option value="">Please select</option>
<% for (int i=0;i<countries.size();i=i+2){%>
<option value="<%=countries.get(i)%>" <%if (countries.get(i).equals(country)) out.write("selected='selected'");  %>><%=countries.get(i+1)%></option>
<%}%>
<option value="Other" <%if ("Other".equals(country)) out.write("selected");   %>>Other</option>
</select></TD></TR>
		<TR><TD><span style='<%if(name.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Your name</span></TD><TD><INPUT TYPE="TEXT" NAME="name"  VALUE="<%=name%>" size=50><font></span></TD></TR>
		<% if (!"".equals(email)&&(email.indexOf("@")<1||email.indexOf(" ")>=0)){
			%><TR><TD colspan='2'><span style='color:#f00'>Please check the email address:</span></TD></TR><%}%>
		<TR><TD><span style='<%if(email.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Email address for sending orders</span></TD><TD><INPUT TYPE="TEXT" NAME="email"  VALUE="<%=email%>"  size=50></TD></TR>
		<% if (!"".equals(contactemail)&&(contactemail.indexOf("@")<1||contactemail.indexOf(" ")>=0)){
			%><TR><TD colspan='2'><span style='color:#f00'>Please check the email address:</span></TD></TR><%}%>
		<TR><TD><span style='<%if(contactemail.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Email address we can use to contact you</span></TD><TD><INPUT TYPE="TEXT" NAME="contactemail"  VALUE="<%=contactemail%>"  size=50></TD></TR>
		<TR><TD><span style='<%if(siteurl.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>URL of your company's web site</span></TD><TD><INPUT TYPE="TEXT" NAME="siteurl"  VALUE="<%=siteurl%>"  size=50></TD></TR>
		<TR><TD>URL of your data feed (optional)</TD><TD><INPUT TYPE="TEXT" NAME="datafeedurl"  VALUE="<%=datafeedurl%>"  size=50></TD></TR>
		<TR><TD><span style='<%if(vat.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Is VAT (sales tax) included in the prices listed?</span></TD><TD><SELECT NAME="vat" ><OPTION VALUE="">Please select<OPTION VALUE="Y" <%if (vat.equals("Y")) out.write(" selected "); %>>Yes<OPTION VALUE="N" <%if (vat.equals("N")) out.write(" selected "); %>>No</SELECT></TD></TR>
		<TR><TD><span style='<%if(currency.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>Currency of the prices</span></TD><TD><select name="currency" >
		<option value="">Please select</option>
<% for (int i=0;i<currencies.size();i++){ if (!"EURcent".equals(currencies.get(i))){%>
<option value="<%=currencies.get(i)%>" <%if (currencies.get(i).equals(currency)) out.write("Selected");  %>><%=currencies.get(i)%></option>
<%}}%>
<option value="Other" <%if ("Other".equals(currency)) out.write("selected='selected'");  %>>Other</option></select></TD></TR>
		<%if (option==1){ 
		if (!"".equals(linkbackurl)&&!"".equals(siteurl)&&!Webroutines.sameDomain(siteurl,linkbackurl)){
			%><TR><TD colspan='2'><span style='color:#f00'>The link to vinopedia must be on the same domain as your web site</span></TD></TR><%}%>
		<TR><TD><span style='<%if(linkbackurl.equals("")&&submit.equals("true")) {out.write("color:#f00");} %>'>URL where you placed a link to Vinopedia.com</span></TD><TD><INPUT TYPE="TEXT" NAME="linkbackurl"  VALUE="<%=linkbackurl%>"  size=50></TD></TR><%} %>
		<TR><TD><span style='<%if(!terms.equals("on")&&submit.equals("true")) {out.write("color:#f00");}%>'>I accept the <a href='/siteindexationagreement.jsp' target='_blank'><span style='<%if(!terms.equals("on")&&submit.equals("true")) {out.write("color:#f00");}%>'>terms and conditions</span></a></span></TD><TD><INPUT TYPE="checkbox" NAME="terms" <%=("on".equals(terms)?"checked='checked' ":"")%>></TD></TD></TR>
		<TR><TD valign=top>Remarks or questions</TD><TD><TEXTAREA rows=6 cols=38  NAME="remarks" ><%=remarks%></TEXTAREA></TD></TR>
		<TR><TD></TD><TD><INPUT TYPE="SUBMIT" VALUE="Send!"></TD></TR>
	</TABLE>
<INPUT type="hidden" name="submit" value="true">
<INPUT type="hidden" name="option" value="<%=option %>">
	</form>

<%if (option==1){ %>
<br/>
To place the link to Vinopedia you can use the following HTML code: <br/><code>&lt;a href='https://www.vinopedia.com' title='Vinopedia wine search engine' target='_blank' style='border-style:none'&gt;&lt;img src='https://www.vinopedia.com/images/listedbuttonwhite.gif'/&gt;&lt;/a&gt;</code><br/><br/> 
<a href='https://www.vinopedia.com' title='Vinopedia wine search engine' target='_blank' style='border-style:none'><img src='/images/listedbuttonwhite.gif'/></a></div>
<%} %>

<%} %>

<%} %>




<%@ include file="/snippets/footer.jsp" %>	
		</div></div> <%// workaround: IE positioning of footer %>
		</div>
		</div> <!--  main--> 
<%} %>
	 <script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>



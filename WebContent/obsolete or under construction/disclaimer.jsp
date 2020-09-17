<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
%><html>
<head>
<title>
Disclaimer
</title>
<jsp:include page="/header.jsp" />
<%String ipaddress="";
if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
	ipaddress = request.getRemoteAddr();
} else {
    ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
}

if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
} else {
	Webroutines.logWebAction("Disclaimer",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
 %>
<br/><br/>
<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">
<h4>Disclaimer</h4>
vinopedia is an independent site which aims to help you find the lowest price for a wine, without giving any guarantees. The information shown on this site is presented "as is", with no guarantee on the correctness, accuracy and completeness of the content, including but not limited to prices, availability and quality of wines or the trustworthyness of the sellers listed. Always verify the price, bottle size, taxes and transport costs with the seller before buying a wine. <br/>
No rights can be derived from being listed or from the information displayed on this site. vinopedia reserves the right to refuse or remove listings without prior notice or stating a reason. Performance and availability of the web site are not guaranteed.<br/>
vinopedia or the owner cannot be held responsible in case of incorrectness or incompleteness of the content. By using this site you agree with its terms of use. Free WineSearcher operates under Dutch law.<br/><br/>
vinopedia reserves the right to refuse access to the site based on IP address (ranges) without prior notice. If your IP address has been blocked, you can request for access <a href='/abuse.jsp'>here</a>.<br/><br/>
Ratings are for educational, non-profit, personal use only, no rights or guarantees can be derived from them. <br/>
If you are the owner of information displayed on this site and you feel that this site breaches your copyright, please <a href='/contact.jsp'>inform us</a> and we will remove the information at our earliest convenience. 
<h4>Privacy statement</h4>
vinopedia stores search information, email adresses and IP-addresses and uses cookies to store preferences and identify users. Search information is used to track, improve and personalize the functionality of the site. IP addresses are used to demonstrate advertisers no "click fraud" takes place. IP addresses and search information are shown to advertisers in a depersonalized way: without showing the last number in the IP address (e.g. 192.168.1.---). Email addresses are solely used to send information from vinopedia.com. We will not share IP information or email adresses with 3rd parties unless forced to do so by law. By using this site you agree that your information may be used as indicated by vinopedia. 

</TD><TD class="right">

	</TD></TR>
</TABLE>
<jsp:include page="/footer.jsp" />	
<%} %>
</div>
</body> 
</html>
<%@page import="com.freewinesearcher.common.Configuration"%>
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"

	
	
%>
<%PageHandler p=PageHandler.getInstance(request,response,"Mobile details");
	session = request.getSession(true); 
	String wineid=Webroutines.filterUserInput(request.getParameter("wineid"));
	String shopid=Webroutines.filterUserInput(request.getParameter("shopid"));
	if (wineid==null) wineid="0";
	Wine wine=null;
	try{wine=new Wine(wineid);}catch(Exception e){}
	if (shopid==null&&wine!=null) shopid=wine.ShopId+"";
	NumberFormat format  = new DecimalFormat("#,##0.00");
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="order" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="priceminstring" value=""/> 
		<jsp:setProperty name="searchdata" property="pricemaxstring" value=""/> 
		<jsp:setProperty name="searchdata" property="*"/> 
		<jsp:setProperty name="searchdata" property="offset" value="0"/>
		
<%
			}else {
		%>
		<jsp:setProperty name="searchdata" property="*"/> 
		
		<%
 					}

 					if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")){
 				%>
		<jsp:setProperty name="searchdata" property="vintage" value='<%=searchdata.getVintage().concat("ss ")+Webroutines.getVintageFromName(searchdata.getName())%>'/> 
		<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/> 
	<%
 		}
 		String ipaddress="";
 	    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
 	    	ipaddress = request.getRemoteAddr();
 	    } else {
 	        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
 	    }

 	    if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
 	    	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
 	    	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
 			
 	    } else {
 	    
 		ArrayList<String> countries = Webroutines.getCountries();
 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
 	%>
<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<meta name="viewport" content="width=320" />
<meta name="MobileOptimized" content="320" />
<title><%
	if (!searchdata.getName().equals("")){out.print(searchdata.getName()+" prices by vinopedia");} else {out.print("vinopedia");}
%></title>
<%
	session.setAttribute("winename",searchdata.getName());
%>
<%@ include file="/headersmall.jsp" %>
<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
function feed(form) {
	actionurl="/showrssurl.jsp?name="+form.name.value;
	actionurl=actionurl+"&vintage="+form.vintage.value;
	document.Searchform.action=actionurl; 
	form.submit();
	
  	return 0;
}
-->
</script>
<script type="text/javascript" src="/js/suggest.js"></script>
<!-- google_ad_section_end -->

<!-- Google Analytics -->
<script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-1788182-2', 'auto', {'legacyCookieDomain': 'vinopedia.com'});
<%=PageHandler.getInstance(request,response).asyncGAtracking%>
ga('send', 'pageview');

</script>
<!-- End Google Analytics -->

</head>	
<table  class="main" onclick="javascript:emptySuggest();">
	<tr>
<% if (wine!=null){ 
	Webroutines.logWebAction("Mobile details",request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), shopid, wineid, wine.PriceEuroEx.toString(),wine.SourceUrl, 0.0);
%>
<td class="centre">
Wine description: <%=wine.Name %><br/>
Vintage: <%=wine.Vintage %><br/>
Size: <%=wine.Size %> l.<br/>
Sold at: <%=wine.Shopname %> (<%=wine.Country %>)<br/>
Price: <%=Webroutines.formatPriceMobile(wine.PriceEuroIn,wine.PriceEuroEx,p.searchdata.getCurrency(),"EX")%> ex. VAT<br/>
Price as listed on the web site of the store: <%=Webroutines.getCurrencySymbol(Dbutil.readValueFromDB("select * from shops where id="+wine.ShopId, "currency"))+" "+Webroutines.formatPrice((double)wine.Price)+(Dbutil.readIntValueFromDB("select * from shops where id="+wine.ShopId, "exvat")==0?" (incl. VAT)":" (excl. VAT)")%><br/>

<br/>
<a href='/link.jsp?wineid=<%=wineid%>' target='_blank'>Link to shop (external site, may not be suitable for mobile devices)</a>
	<%} else {
		%>Sorry... wine not found!
	<%}	%>
	
	</td></tr>
</table>	
<!--main-->			
		<!-- google_ad_section_start(weight=ignore) -->	
		<form action='/mobile.jsp' method="post" id="Searchform" >
		<table class="searchform">
			<tr><td><% if (searchdata.getName().length()<3) {%>Name (mandatory)<%} else {%>Name (mandatory)<%}%></td><td>Vintage</td></tr>
			<tr><td><input type="text" id="name" value="<%=searchdata.getName()%>" size="20" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);"  /></td>
			<td><input type="text" value="<%=searchdata.getVintage()%>" size="4" /></td></tr>
			<tr><td><div id="search_suggest" class="search_suggest_noborder" ></div></td><td></td></tr></table>
			<table><tr><td>Country of Seller</td><td></td></tr>
			<tr><td><select name="country">
					<option value="All"<%if (searchdata.getCountry().equals("All")) out.print(" selected='selected'");%>>All</option>
					<% for (int i=0;i<countries.size();i=i+2){%>
					<option value="<%=countries.get(i)%>"<%if (searchdata.getCountry().equals(countries.get(i))) out.print(" selected");%>><%=countries.get(i+1)%></option>
					<%}%>
					</select></td><td><input type="submit" value="Search" /></td></tr>
			
			</table>
		<!--searchform-->
		<div><input type="hidden" id="dosearch" value="true" />		
		<input type="hidden" id="order" value="" />		
		<input type="hidden" id="rareoldstring" value="false" />
		<input type="hidden" id="createdstring" value="0" />
	  	</div>
	  	</form>
	
		
	<div id="note">Note: Prices shown include local VAT but may exclude duty, shipping and handling costs. Always check the price with the seller.</div>
	
<%} //NZ filter %>

</div>
</body> 
</html>
<html>
<head>
<title>
vinopedia
</title>
<jsp:include page="/header.jsp" />

<%
	// Handle source IP address
	
	String ipaddress="";
	if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
	ipaddress = request.getRemoteAddr();
} else {
    ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
}
String hostcountry=Webroutines.getCountryCodeFromIp(ipaddress);
if (hostcountry.equals("NZ")){
	out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
} else {

	// Javascript to respond to XML button
%>
<br/>
<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
function feed(form) {
	actionurl="/showrssurl.jsp?name="+form.name.value;
	actionurl=actionurl+"&vintage="+form.vintage.value;
	actionurl=actionurl+"&pricemin="+form.priceminstring.value;
	actionurl=actionurl+"&pricemax="+form.pricemaxstring.value;
	actionurl=actionurl+"&rareoldstring="+form.rareoldstring.value;
  	document.Searchform.action=actionurl;
	form.submit();
	
  	return 0;
}
-->
</script>
<!-- google_ad_section_end -->

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
	import = "com.freewinesearcher.online.Ad"
%>
<%
	session = request.getSession(true); 

	// Deal with all variables and post data
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	int numberofrows=Webroutines.numberofnormalrows;
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
	<jsp:setProperty name="searchdata" property="vintage" value="<%=searchdata.getVintage().concat("ss ")+Webroutines.getVintageFromName(searchdata.getName())%>"/> 
	<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/> 
	<%
 		}

 	 	 		// Analyse Google query and reuse query on Google instead of Google's link
 	 	 		if (request.getHeader("Referer")!=null&&(request.getHeader("Referer").contains("google")||request.getHeader("Referer").contains("hammink"))){
 	 	 			int start=request.getHeader("Referer").indexOf("&q=")+3;
 	 	 			if (start>0) {
 	 	 		int end=request.getHeader("Referer").indexOf("&",start+2);
 	 	 		if (end>start) {
 	 	 			String googlequery=request.getHeader("Referer").substring(start,end).replaceAll("\\+"," ");
 	 	 			googlequery=googlequery.replaceAll("[Ww]ine","");
 	 	 			googlequery=googlequery.replaceAll("[Ff]ree","");
 	 	 			googlequery=googlequery.replaceAll("[Ss]earcher","");
 	 	 			googlequery=googlequery.replaceAll("[Ss]earch","");
 	 	 			googlequery=googlequery.replaceAll("[Pp]rice","");
 	 	 			googlequery=googlequery.replaceAll("\\d+","").trim();
 	 	 			googlequery=googlequery.replaceAll(" +"," ");
 	 	 			
 	 	 			if (googlequery.length()>3){
 	 	 				Wineset tempwineset=new Wineset(googlequery,searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),1);
 	 	 				if (tempwineset.records>5){
 	 	 					searchdata.setName(googlequery);
 	 	 					searchdata.setVintage("");
 	 	 					searchdata.setCountry("All");
 	 	 				}			
 	 	 			}
 	 	 		}
 	 	 			}
 	 	 		}
 	 	 		
 	 	 		ArrayList<String> countries = Webroutines.getCountries();
 	 	 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));

 	 	 		// Standard search, no javascript
 	%>
<TABLE  class="main" >
	<TR><TD class="left">
	<%
		if (!request.getHeader("User-Agent").contains("Mozilla")) out.write("If you are on a mobile device, click <a href='/mobile.jsp'>here</a>");
	%>
	<TABLE class="search">
		<TR><TD>
		<!-- google_ad_section_start(weight=ignore) -->	
		<FORM ACTION='/' METHOD="POST" id="Searchform" name="Searchform">
		<TABLE class="searchform">
			<TR><TD><h4>Search a wine</h4>
			<%
				if (searchdata.getName().length()<3) {
			%><font color='red'>Name (mandatory)</font><%
				} else {
			%>Name (mandatory)<%
				}
			%></TD></TR><TR><TD><INPUT TYPE="TEXT" NAME="name" value="<%=searchdata.getName()%>" size=25></TD></TR>
			<TR><TD>Vintage</TD></TR><TR><TD><INPUT TYPE="TEXT" NAME="vintage" value="<%=searchdata.getVintage()%>" size=25></TD></TR>
			<TR><TD>Minimum price</TD></TR><TR><TD><INPUT TYPE="TEXT" NAME="priceminstring" value="<%=searchdata.getPriceminstring()%>" size=25></TD></TR>
			<TR><TD>Maximum price</TD></TR><TR><TD><INPUT TYPE="TEXT" NAME="pricemaxstring" value="<%=searchdata.getPricemaxstring()%>" size=25></TD></TR>
			<TR><TD>Country of Seller</TD></TR><TR><TD><select name="country" >
					<option value="All"<%if (searchdata.getCountry().equals("All")) out.print(" selected");%>>All
					<%
						for (int i=0;i<countries.size();i=i+2){
					%><option value="<%=countries.get(i)%>"<%if (searchdata.getCountry().equals(countries.get(i))) out.print(" selected");%>><%=countries.get(i+1)%><%
						}
					%>
					</select></TD></TR>
			<TR><TD>Show wines added in the last </TD></TR><TR><TD><select name="createdstring" >
			<option value="0"<%if (searchdata.getCreated()==0) out.print(" selected");%>>No limit
			<option value="1"<%if (searchdata.getCreated()==1) out.print(" selected");%>>1 Day
			<option value="3"<%if (searchdata.getCreated()==3) out.print(" selected");%>>3 Days
			<option value="7"<%if (searchdata.getCreated()==7) out.print(" selected");%>>1 Week
			<option value="30"<%if (searchdata.getCreated()==30) out.print(" selected");%>>1 Month
			</select></TD></TR>
			</TABLE>
		<!--searchform-->
		<input type="hidden" name="dosearch" Value="true">		
		<input type="hidden" name="order" Value="">		
		<input type="hidden" name="rareoldstring" Value="false">
		<INPUT TYPE="SUBMIT" VALUE="Search">
	  	<input type="image" src="/images/xml.bmp" onclick="javascript:feed(this.form);">
	  	</FORM>
	</TD></TR>
	</TABLE>

<%
	// Initialize results in the form of wineset and advertisements

	Wineset wineset =null;
	if (searchdata.getName().length()>2) {

		// For now, on this page sponsored links have been disabled
		// Wineset sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
		wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
	}
	String region="";
	int knownwineid=0;
	if (wineset!=null){
		region=wineset.region;
		knownwineid=wineset.knownwineid;
		if (region==null||region.equals("")){
	if (knownwineid>0){
		region=Dbutil.readValueFromDB("Select * from knownwines where id="+knownwineid+";","appellation");
	}
		}
	}
	Ad rightad= new Ad(120,600,hostcountry,region,knownwineid,"");
	Ad bottomleftad= new Ad(187,300,hostcountry,region,knownwineid,rightad.partner+"");
	session.setAttribute("hostcountry",hostcountry);
	session.setAttribute("region",region);
	session.setAttribute("knownwineid",knownwineid);
	out.write(bottomleftad.html);
%>

	<!--search-->
	</TD><TD class="centre"><%=Webroutines.getConfigKey("systemmessage")%>
	
	
	<%
				// No search if less than 3 characters have been used
					// Instead, display welcome text
					
					if (searchdata.getName().length()<3) { 
					Webroutines.logWebAction("Pageload",request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
			%>
	
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start -->
			<H4>How to use this site</H4><BR/>
			On the left, you can enter the criteria for the wine you are looking for. Some tips:<BR/>
			<ul>
			<li>Name is a mandatory field. You can enter one or more keywords that describe a wine (at least 3 letters), for instance Almaviva or Vega Sicilia. Parts of your keywords will be matched, so if you are not sure about the spelling, just enter part of the word.  
			<li>To exclude a keyword, enter -keyword. So Leoville -Poyferre will find L&eacute;oville Las Cases, but not L&eacute;oville Poyferr&eacute;!
			<li>You can enter a keyword without accents: Leoville will find both Leoville and L&eacute;oville. 
			<li>For vintage, you can use a ranges using '-' or multiple years using ',' ( like 1970-1975, 1982, 1990, 1998-).
			</ul>
			The <a href='/index.jsp'>standard search engine</a> is looking for all terms you have entered, so if your search does not give any results, try leaving out some terms that may not be included in the wine descriptions (like "AOC" or the first name of the wine grower). The <a href='/newindex.jsp'>advanced search</a> is more intelligent: it suggests wines as you type and gives more narrow results.<br><br>
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start(weight=ignore) -->	The &#88;ML button allows you to create an &#82;&#83&#83; feed from your search. Copy and paste the URL in your favorite feed reader to keep track of your search.<br/>
			<br/>Also check out <a href='/settings/index.jsp'>PriceAlerts</a>, which keep you informed when new interesting wines become available. Just let us know what you are looking for and we will send you an email whenever we find a new match. 
			<br><br>For more features, have a look on the <a href='/sitemap.jsp'>site map</a>.			
	<!--hints-->
	<!-- google_ad_section_end -->
	<br/><br/>Happy searching!
	<%
		// Check if a search was executed, if so show a warn message about the length of the search criteria
		
			if (request.getParameter("dosearch")!=null) {
		out.print("<BR/><BR/><font color='red'>Please enter at least 3 characters for the wine name.</font>");
			}
		}
		
		if (searchdata.getName().length()>2) {
			// Show the search results.
			
			Webroutines.logWebAction("Search",request.getServletPath(),ipaddress, request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
			if (wineset==null||wineset.Wine.length==0){
		out.print("No results found. "+Webroutines.didYouMean(searchdata.getName(),"/index.jsp"));
			} else {
	%><h4>Search results: <%
		out.print(wineset.records+" wines found. "+Webroutines.didYouMean(searchdata.getName(),"/index.jsp")+"<br>Permalink: <a href='"+response.encodeURL("https://www.vinopedia.com/wine/"+searchdata.getName()+" "+searchdata.getVintage())+"'>"+response.encodeURL("https://www.vinopedia.com/wine/"+searchdata.getName()+" "+searchdata.getVintage())+"</a>");
	%></h4>
			<%
				if (wineset.records>numberofrows){
					
					// Show the links to the rest of the results
					for (int i=0;i<wineset.records;i=i+numberofrows){
					 out.print("<a href='"+response.encodeURL("/index.jsp?name="+searchdata.getName()+"&offset="+i)+"' style='{color: blue;}'>");
					 if (Integer.toString(i).equals(offset)) out.print("<b>");
					 out.print("Page&nbsp;"+(i/numberofrows+1)+" ");
					 if (Integer.toString(i).equals(offset)) out.print("</b>");
					 out.print("</a>");
					
					}
				}
			%>
	<TABLE class="results"><TR><TH class="flag"></TH><TH class="shop">Store</TH><TH class="name">Wine</TH><TH class="vintage"><%
		out.print("<a href='"+response.encodeURL("/index.jsp?name="+searchdata.getName()+"&offset=0&order="+Webroutines.getOrder(searchdata.getOrder(),"vintage"))+"'>Vintage</a>");
	%></TH><TH class="size" align="right">&nbsp;&nbsp;&nbsp;<%
		out.print("<a href='"+response.encodeURL("/index.jsp?name="+searchdata.getName()+"&offset=0&order="+Webroutines.getOrder(searchdata.getOrder(),"size"))+"'>Size</a>&nbsp;&nbsp;&nbsp;");
	%></TH><TH class="price" align="right"><%
		out.print("<a href='"+response.encodeURL("/index.jsp?name="+searchdata.getName()+"&offset=0&order="+Webroutines.getOrder(searchdata.getOrder(),"priceeuroex"))+"'>Price</a>");
	%></TH></TR>    
			<%
    				NumberFormat format  = new DecimalFormat("#,##0.00");	
    				
    				// Sponsored results are disabled here
    				
    				//if (sponsoredwineset.records>0) {	
    				//	out.print("<tr><TD><i>Sponsored results:</i></TD></tr>");
    				//	for (int i=0;i<sponsoredwineset.Wine.length;i++){
    				//			out.print("<tr");
    				//			if (i%2==1)out.print(" class=\"sponsoredodd\"");
    				//			if (i%2==0)out.print(" class=\"sponsoredeven\"");
    				//			out.print (">");
    				//			out.print("<TD><a href="+response.encodeURL("link.jsp?shopid="+sponsoredwineset.Wine[i].ShopId)+" target='_blank'>"+sponsoredwineset.Wine[i].Shopname+"</a></TD>");
    				//			out.print("<TD><a href="+response.encodeURL("link.jsp?wineid="+sponsoredwineset.Wine[i].Id)+" target='_blank'>"+sponsoredwineset.Wine[i].Name+"</a></TD>");
    				//			out.print("<TD>" + sponsoredwineset.Wine[i].Vintage+"</TD>");
    				//			out.print("<TD align='right'>" + Webroutines.formatSize(sponsoredwineset.Wine[i].Size)+"</TD>");
    				//			out.print("<TD align='right'>&euro; " + format.format(sponsoredwineset.Wine[i].Price)+"</TD>");
    				//			out.print("</tr>");
    				//	}
    				//	out.print("<tr><TD><i>All results:</i></TD></tr>");
    					
    				//} else {	
    				//	out.print("<tr><TD><i>Sponsored results:</i></TD></tr>");
    				//	out.print("<tr class=\"sponsoredeven\">");
    				//	out.print("<TD><a href='"+response.encodeURL("/sponsoring.jsp")+"'>vinopedia</a></TD>");
    				//	out.print("<TD><a href='"+response.encodeURL("/sponsoring.jsp")+"'>Your wine could be listed here! Click for more information.</a></TD>");
    				//	out.print("<TD></TD>");
    				//	out.print("<TD align='right'></TD>");
    				//	out.print("<TD align='right'>&euro; 0.10</TD>");
    				//	out.print("</tr>");
    				//	out.print("<tr><TD><i>All results:</i></TD></tr>");
    				//}
    					
    				
    				// Give the complete result list
    				for (int i=0;i<wineset.Wine.length;i++){
    						out.print("<TR");
    						if (wineset.Wine[i].CPC>0){
    							out.print(" class=\"sponsoredeven\"");
    						} else {
    							if (i%2==1){out.print(" class=\"odd\"");}
    						}
    						out.print (">");
    						out.print("<TD class='flag'><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"&shopid="+wineset.Wine[i].ShopId+"' target='_blank'><img src='/images/flags/"+wineset.Wine[i].Country.toLowerCase()+".gif'/></a></TD>");
    						out.print("<TD><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"&shopid="+wineset.Wine[i].ShopId+"' target='_blank'>"+wineset.Wine[i].Shopname+"</a></TD>");
    						out.print("<TD><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"' target='_blank'>"+Webroutines.formatCapitals(wineset.Wine[i].Name)+"</a></TD>");
    						out.print("<TD>" + wineset.Wine[i].Vintage+"</TD>");
    						out.print("<TD align='right'>" + Webroutines.formatSize(wineset.Wine[i].Size)+"</TD>");
    						out.print("<TD class='price' align='right'>&euro; " + format.format(wineset.Wine[i].PriceEuroEx)+"</TD>");
    						out.print("</TR>");
    				}
    			%>
	</TABLE>
	<!--results-->	
	<font size=1><br/>Note: Prices shown include local VAT but may exclude duty, shipping and handling costs. Always check the price with the seller.<br/></font>
	<%		if (wineset.records>numberofrows){

		// Show the links to the rest of the results again at the bottom

		for (int i=0;i<wineset.records;i=i+numberofrows){
				 out.print("<a href='"+response.encodeURL("/index.jsp?name="+searchdata.getName()+"&offset="+i)+"' style='{color: blue;}'>");
				 if (Integer.toString(i).equals(offset)) out.print("<b>");
				 out.print("Page&nbsp;"+(i/numberofrows+1)+" ");
				 if (Integer.toString(i).equals(offset)) out.print("</b>");
				 out.print("</a>");
				}
			}			
		}		
	}
	%>
</TD><TD class="right">
<%out.write(rightad.html); %>
	</TD></TR>
</TABLE>	
<!--main-->		
<jsp:include page="/footer.jsp" />	
<%} //NZ filter %>

</div>

</body> 
</html>
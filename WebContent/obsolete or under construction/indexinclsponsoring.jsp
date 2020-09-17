<html>
<head>
<title>
vinopedia
</title>
<jsp:include page="/header.jsp" />
<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
function rss(form) {
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
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	
%>
<%
	session = request.getSession(true);
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	int numberofrows=Webroutines.numberofnormalrows;
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { 
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
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

 					ArrayList<String> countries = Webroutines.getCountries();
 					if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
 				%>

<BR/>Please enter your search criteria below.
<!-- google_ad_section_start(weight=ignore) -->	
<TABLE><TR><TD>    	<FORM ACTION='/indexinclsponsoring.jsp' METHOD="POST" id="Searchform" name="Searchform">
		<TABLE class="form">
		<TR><TD>Name (mandatory)</TD><TD><INPUT TYPE="TEXT" NAME="name" value="<%=searchdata.getName()%>" size=40></TD></TR>
		<TR><TD>Vintage</TD><TD><INPUT TYPE="TEXT" NAME="vintage" value="<%=searchdata.getVintage()%>" size=40></TD></TR>
		<TR><TD>Minimum price</TD><TD><INPUT TYPE="TEXT" NAME="priceminstring" value="<%=searchdata.getPriceminstring()%>" size=40></TD></TR>
		<TR><TD>Maximum price</TD><TD><INPUT TYPE="TEXT" NAME="pricemaxstring" value="<%=searchdata.getPricemaxstring()%>" size=40></TD></TR>
		<TR><TD>Country of Seller</TD><TD><select name="country" >
				<option value="All"<%if (searchdata.getCountry().equals("All")) out.print(" selected");%>>All
				<%
					for (int i=0;i<countries.size();i=i+2){
				%>
				<option value="<%=countries.get(i)%>"<%if (searchdata.getCountry().equals(countries.get(i))) out.print(" selected");%>><%=countries.get(i+1)%>
				<%
					}
				%>
				</select></TD></TR>
		<TR><TD>Show wines added in the last </TD><TD><select name="createdstring" >
		<option value="0"<%if (searchdata.getCreated()==0) out.print(" selected");%>>No limit
		<option value="1"<%if (searchdata.getCreated()==1) out.print(" selected");%>>1 Day
		<option value="3"<%if (searchdata.getCreated()==3) out.print(" selected");%>>3 Days
		<option value="7"<%if (searchdata.getCreated()==7) out.print(" selected");%>>1 Week
		<option value="30"<%if (searchdata.getCreated()==30) out.print(" selected");%>>1 Month
		</select></TD></TR>
		<TR><TD>Show wines in the category</TD><TD><select name="rareoldstring" >
		<option value="false"<%if (searchdata.getRareoldstring().equals("false")) out.print(" selected");%>>All wines
		<option value="true"<%if (searchdata.getRareoldstring().equals("true")) out.print(" selected");%>>Rare and old wines
		</select></TD></TR>
	</TABLE>
	<INPUT TYPE="SUBMIT" VALUE="Search">
  	<input type="image" src="images/xml.bmp" onclick="javascript:rss(this.form);">
  	
	</FORM></TD><TD width=5% ></TD><TD valign="top" bgcolor="#FFFFFF"; style="padding:10; border-width: 3px; border-style: inset;"  >
<!-- google_ad_section_end -->
	Some search hints:<BR/>
	<ul>
	<li>In the name field, enter one or more keywords that describe a wine (at least 3 letters), for instance Almaviva or Vega Sicilia. Parts of your keywords will be matched, so Chateau will match both Chateau and Chateauneuf. 
	<li>To exclude a keyword, enter -keyword. So Leoville -Poyferre will find L&eacute;oville Las Cases, but not L&eacute;oville Poyferr&eacute;.
	<li>You can enter a keyword without accents: Leoville will find Leoville and L&eacute;oville. 
	<li>For vintage, you can use a ranges using '-' or multiple years using ',' ( like 1970-1975, 1982, 1990, 1998-).
	</ul>
	<!-- google_ad_section_start(weight=ignore) -->	The XML button allows you to create an RSS feed from your search. Copy and paste the URL in your favorite feed reader to keep track of your search.</TD></TR></TABLE>
<!-- google_ad_section_end -->

<%
	out.print(searchdata.getMessage());
	searchdata.setMessage("");
	
	if (searchdata.getName().length()>2) {
		//Wineset sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),100);
		Wineset sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
		//Wineset wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "","",searchdata.getOffset(),100);
		Wineset wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
		//Wineset.logWebSearch(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(),request.getRemoteAddr(),request.getHeader("referer"));
		if (wineset.Wine.length==0){
	out.print("No results found. Try broadening your search criteria");
		} else {
%><h4>Search results: <%
	out.print(wineset.records+" wines found. ");
%></h4>
			<%
				if (wineset.records>100){
					for (int i=0;i<wineset.records;i=i+100){
					 out.print("<a href='"+response.encodeURL("/index.jsp?offset="+i)+"' style='{color: blue;}'>");
					 if (Integer.toString(i).equals(offset)) out.print("<b>");
					 out.print("Page&nbsp;"+(i/100+1)+" ");
					 if (Integer.toString(i).equals(offset)) out.print("</b>");
					 out.print("</a>");
					}
				}
			%>
		<TABLE><TR><TD width=90% valign="top">
			<TABLE class="results"><TR><TH class="shop">Store</TH><TH class="name">Wine</TH><TH class="vintage">Vintage</TH><TH class="size" align="right">&nbsp;&nbsp;&nbsp;Size&nbsp;&nbsp;&nbsp;</TH><TH class="price" align="right">Price</TH></TR>    
			<%
    				NumberFormat format  = new DecimalFormat("#,##0.00");	
    				
    				if (sponsoredwineset.records>0) {	
    					out.print("<tr><td><i>Sponsored results:</i></td></tr>");
    					for (int i=0;i<sponsoredwineset.Wine.length;i++){
    							out.print("<tr");
    							if (i%2==1)out.print(" class=\"sponsoredodd\"");
    							if (i%2==0)out.print(" class=\"sponsoredeven\"");
    							out.print (">");
    							out.print("<td><a href="+response.encodeURL("link.jsp?shopid="+sponsoredwineset.Wine[i].ShopId)+" target='_blank'>"+sponsoredwineset.Wine[i].Shopname+"</a></td>");
    							out.print("<td><a href="+response.encodeURL("link.jsp?wineid="+sponsoredwineset.Wine[i].Id)+" target='_blank'>"+sponsoredwineset.Wine[i].Name+"</a></td>");
    							out.print("<td>" + sponsoredwineset.Wine[i].Vintage+"</td>");
    							out.print("<td align='right'>" + Webroutines.formatSize(sponsoredwineset.Wine[i].Size)+"</td>");
    							out.print("<td align='right'>&euro; " + format.format(sponsoredwineset.Wine[i].Price)+"</td>");
    							out.print("</tr>");
    					}
    					out.print("<tr><td><i>All results:</i></td></tr>");
    					
    				} else {	
    					out.print("<tr><td><i>Sponsored results:</i></td></tr>");
    					out.print("<tr class=\"sponsoredeven\">");
    					out.print("<td><a href='"+response.encodeURL("/sponsoring.jsp")+"'>vinopedia</a></td>");
    					out.print("<td><a href='"+response.encodeURL("/sponsoring.jsp")+"'>Your wine could be listed here! Click for more information.</a></td>");
    					out.print("<td></td>");
    					out.print("<td align='right'></td>");
    					out.print("<td align='right'>&euro; 0.10</td>");
    					out.print("</tr>");
    					out.print("<tr><td><i>All results:</i></td></tr>");
    				}
    					
    				
    				
    				for (int i=0;i<wineset.Wine.length;i++){
    						out.print("<tr");
    						if (wineset.Wine[i].CPC>0){
    							out.print(" class=\"sponsoredeven\"");
    						} else {
    							if (i%2==1){out.print(" class=\"odd\"");}
    						}
    						out.print (">");
    						out.print("<td><a href="+response.encodeURL("link.jsp?wineid="+wineset.Wine[i].Id+"&shopid="+wineset.Wine[i].ShopId)+" target='_blank'>"+wineset.Wine[i].Shopname+"</a></td>");
    						out.print("<td><a href="+response.encodeURL("link.jsp?wineid="+wineset.Wine[i].Id)+" target='_blank'>"+wineset.Wine[i].Name+"</a></td>");
    						out.print("<td>" + wineset.Wine[i].Vintage+"</td>");
    						out.print("<td align='right'>" + Webroutines.formatSize(wineset.Wine[i].Size)+"</td>");
    						out.print("<td align='right'>&euro; " + format.format(wineset.Wine[i].Price)+"</td>");
    						out.print("</tr>");
    				}
    			%></TABLE><font size=1><br/>Note: Prices shown are ex. VAT.<br/></font>
			
		</TD><TD valign="top" width=130>
		<script type="text/javascript"><!--
		google_ad_client = "pub-5573504203886586";
		google_ad_width = 120;
		google_ad_height = 600;
		google_ad_format = "120x600_as";
		google_ad_type = "text_image";
		google_ad_channel ="";
		google_color_border = "336699";
		google_color_bg = "FFFFFF";
		google_color_link = "0000FF";
		google_color_url = "008000";
		google_color_text = "000000";
		//--></script>
		<script type="text/javascript"
		  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
		</script>
		
		</TD></TR></TABLE>	
			
			
			<%
			if (wineset.records>100){
				for (int i=0;i<wineset.records;i=i+100){
				 out.print("<a href='"+response.encodeURL("/index.jsp?offset="+i)+"' style='{color: blue;}'>");
				 if (Integer.toString(i).equals(offset)) out.print("<b>");
				 out.print("Page&nbsp;"+(i/100+1)+" ");
				 if (Integer.toString(i).equals(offset)) out.print("</b>");
				 out.print("</a>");
				}
			}
		}		
	} else { %>
		<br/><br/><script type="text/javascript"><!--
google_ad_client = "pub-5573504203886586";
google_ad_width = 728;
google_ad_height = 90;
google_ad_format = "728x90_as";
google_ad_type = "text_image";
google_ad_channel ="";
google_color_border = "336699";
google_color_bg = "FFFFFF";
google_color_link = "0000FF";
google_color_url = "008000";
google_color_text = "000000";
//--></script>
<script type="text/javascript"
  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script> <%
		if (searchdata.getName().length()>0) {
			out.print("Please enter at least 3 characters for the wine name.");
		} 
	}	
	
%>
</table>
</body> 
</html>
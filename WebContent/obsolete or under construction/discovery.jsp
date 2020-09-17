<%@ page   
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.WineAdvice"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Shop"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Region"
	import = "com.freewinesearcher.common.Winerating"

	
	
%>
<%
	session = request.getSession(true);
%>

	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	// Handle source IP address
	

String country = Webroutines.filterUserInput(request.getParameter("country"));
if (country==null||country.equals("")) country="All";
String scale = Webroutines.filterUserInput(request.getParameter("scale"));
if (scale==null||scale.equals("")) scale="100";
String type = request.getParameter("type");
if (type==null||type.equals("")) type="All";
String currency = Webroutines.filterUserInput(request.getParameter("currency"));
if (currency==null) currency="EUR";
boolean subregions=true;
if (Webroutines.filterUserInput(request.getParameter("subregions"))!=null&&Webroutines.filterUserInput(request.getParameter("subregions")).equals("N")) subregions=false;
String pricemax = Webroutines.filterUserInput(request.getParameter("pricemax"));
if (pricemax==null) pricemax="";
String vintage = Webroutines.filterUserInput(request.getParameter("vintage"));
if (vintage==null) vintage="";
String searchtype = Webroutines.filterUserInput(request.getParameter("searchtype"));
boolean pqratio=true;
if (searchtype!=null&&searchtype.equals("rating")) pqratio=false;
vintage=Webroutines.filterVintage(vintage);
vintage=Webroutines.filterUserInput(vintage);
boolean dosearch=Boolean.parseBoolean(Webroutines.filterUserInput(request.getParameter("dosearch")));

String region = (request.getParameter("region"));
if (region==null) region="All";
ArrayList<String> regions=Region.getRegions("All");

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
 				%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title>vinopedia: The Discovery</title>
<%
	session.setAttribute("winename",searchdata.getName());
%>
<%@ include file="/header.jsp" %>
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

<%
	if (hostcountry.equals("NZ")){
	out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
} else {
	ArrayList<String> countries = Webroutines.getCountries();
	if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
	Webroutines.logWebAction("Discovery ("+request.getRemoteUser()+")",request.getServletPath(),ipaddress, request.getHeader("referer"), region,searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
	Translator t=new Translator();
    t.setLanguage(Translator.languages.EN);
%>

<table class="main" >
	<tr style="height:1px;"><td class="left"></td><td class="centre"></td><td class="right"></td></tr>
	<tr><td class="left">


		<!-- google_ad_section_start(weight=ignore) -->	
		<form action='/newindex.jsp' method="post" id="Searchform" name="Searchform">
		<%=t.get("displaycurrency")%><br />
		<input type="radio" name="currency" value="EUR" <%if (searchdata.getCurrency().equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio" name="currency" value="GBP" <%if (searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF" <%if (searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD" <%if (searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$
		<table class="search">
		<tr><td>
		<table class="searchform">
			<tr><td><h4><%=t.get("searchawine")%></h4>
			<%
				if (searchdata.getName().length()<3&&request.getParameter("dosearch")!=null) {
			%><font color='red'><%=t.get("name")%></font><%
				} else {
			%><%=t.get("name")%><%
				}
			%> <a href='/index.jsp?help=true' onmouseover='javascript:document.getElementById("helptext").style.visibility="visible";' onmouseout='javascript:document.getElementById("helptext").style.visibility="hidden";'><%=t.get("help")%></a></td></tr><tr><td><input class="leftfull" type="text" name="name" id="name" value="<%=Spider.escape(searchdata.getName())%>" size="25" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" /><div id="search_suggest" class="search_suggest_noborder" ></div></td></tr>
			<tr><td><%=t.get("vintage")%></td></tr><tr><td><input class="leftfull" type="text" name="vintage" value="<%=searchdata.getVintage()%>"  /></td></tr>
			<tr><td><%=t.get("countryofretailer")%></td></tr><tr><td><select name="country" >
					<option value="All"<%if (searchdata.getCountry().equals("All")) out.print(" selected=\"selected\"");%>><%=t.get("all")%></option>
					<%
						for (int i=0;i<countries.size();i=i+2){
					%><option value="<%=countries.get(i)%>"<%if (searchdata.getCountry().equals(countries.get(i))) out.print(" selected=\"selected\"");%>><%=countries.get(i+1)%></option><%
						}
					%>
					</select></td></tr>
			<tr><td><%=t.get("showwinesaddedinthelast")%> </td></tr><tr><td><select name="createdstring" >
			<option value="0"<%if (searchdata.getCreated()==0) out.print(" selected=\"selected\"");%>><%=t.get("nolimit")%></option>
			<option value="1"<%if (searchdata.getCreated()==1) out.print(" selected=\"selected\"");%>><%=t.get("1day")%></option>
			<option value="3"<%if (searchdata.getCreated()==3) out.print(" selected=\"selected\"");%>><%=t.get("3days")%></option>
			<option value="7"<%if (searchdata.getCreated()==7) out.print(" selected=\"selected\"");%>><%=t.get("1week")%></option>
			<option value="30"<%if (searchdata.getCreated()==30) out.print(" selected=\"selected\"");%>><%=t.get("1month")%></option>
			</select></td></tr>
			</table>
		<!--searchform-->
		<input type="hidden" name="dosearch" value="true" />		
		<input type="hidden" name="order" value="" />		
		<input type="hidden" name="rareoldstring" value="false" />
		<input type="submit" value="<%=t.get("search")%>" />
	  	<input type="image" src="/images/xml.bmp" onclick="javascript:feed(this.form);" />
	</td></tr>
	</table>
  	</form>
	<%
		Ad rightad= new Ad(120,600,hostcountry,region,0,"");
			Ad bottomleftad= new Ad(187,300,hostcountry,region,0,rightad.partner+"");
			session.setAttribute("hostcountry",hostcountry);
			session.setAttribute("region",region);
			session.setAttribute("knownwineid",0);
			out.write(bottomleftad.html);
	%>
	
	</td>
	<td class="centre"><%=Webroutines.getConfigKey("systemmessage")%>
		<form action='discovery.jsp' method="post" id="RegionSearchform" name="Searchform">
		<h4>The Discovery</h4>vinopedia features a unique system for discovering new wines. Of course we have the well-known pricing information, but we also give ratings to the best wines. The two of them combined give you a very powerful tool for discovering the best new wines your money can buy. <br/><br/>
			Just fill out what you are looking for, what price you would like to pay, and we will give you suggestions for wines to try.<br/><br/> 
		<table>
			<tr><td>Wine Region/Appellation</td><td><select name="region">
			<option value="All" <%if (region.equals("All")) out.print(" selected='selected' ");%>>All countries/regions</option>
					<%
						for (int i=0;i<regions.size();i++){
					%>
					<option value="<%=regions.get(i)%>" <%if (region.equals(regions.get(i))) out.print(" selected='selected' ");%>><%=regions.get(i)%></option>
					<%
						}
					%>
					</select></td></tr>			
			<tr><td>Include subregions?</td><td><input type="radio" name="subregions" value="Y" <%if (subregions) out.print(" checked=\"checked\"");%> />Yes<input type="radio" name="subregions" value="N" <%if (!subregions) out.print(" checked=\"checked\"");%> />No</td></tr>
			<tr><td>Wine type</td><td><select name="type">
			<option value="All" <%if (type.equals("All")) out.print(" selected='selected' ");%>>All wine types</option>
					<%
						for (int i=0;i<Wijnzoeker.typevalues.length;i++){
					%>
					<option value="<%=Wijnzoeker.typevalues[i]%>" <%if (type.equals(Wijnzoeker.typevalues[i])) out.print(" selected='selected' ");%>><%=Wijnzoeker.typevalues[i]%></option>
					<%
						}
					%>
					</select></td></tr>
			<tr><td>Vintage</td><td><input type="text" name="vintage" value="<%=vintage%>" size='25'/></td></tr>
			<tr><td><%=t.get("countryofretailer")%></td><td><select name="country" >
					<option value="All"<%if (country.equals("All")) out.print(" selected=\"selected\"");%>><%=t.get("all")%></option>
					<%
						for (int i=0;i<countries.size();i=i+2){
					%><option value="<%=countries.get(i)%>"<%if (country.equals(countries.get(i))) out.print(" selected=\"selected\"");%>><%=countries.get(i+1)%></option>
					<%
						}
					%>
					</select><input type="hidden" name="dosearch" value="true" />		
		</td></tr>
	  	<tr><td>Maximum price</td><td><input type="text" name="pricemax" value="<%=pricemax%>" size='25'/></td></tr>
		<tr><td>Search type</td><td><input type="radio" name="searchtype" value="PQ" <%if (pqratio) out.write("checked=\"checked\"");%>/>Best price/quality ratio<input type="radio" name="searchtype" value="rating" <%if (!pqratio) out.write("checked=\"checked\"");%>/>Highest rating</td></tr>
		<tr><td>
	  	<%=t.get("displaycurrency")%></td><td>
		<input type="radio" name="currency" value="EUR" <%if (currency.equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio" name="currency" value="GBP" <%if (currency.equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF" <%if (currency.equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD" <%if (currency.equals("USD")) out.print(" checked=\"checked\"");%> />$
		</td></tr>
		<tr><td>
	  	Rating scale</td><td>
		<input type="radio" name="scale" value="100" <%if (scale.equals("100")) out.print(" checked=\"checked\"");%> />100 points<input type="radio" name="scale" value="20" <%if (scale.equals("20")) out.print(" checked=\"checked\"");%> />20 points
		</td></tr>
		</table>
		<!--searchform-->
		<input type="hidden" name="dosearch" value="true"/>		
		<input type="submit" value="Search"/>
	  	</form>
		<%
			if (dosearch){
		%><h4>Search results: </h4>
	<%
		WineAdvice advice=new WineAdvice();
		advice.setLimit(30);
		advice.setCountryofseller(country);
		advice.setCurrency(currency);
		advice.setRegion(region);
		
		try{
			if (!pricemax.equals("")) advice.setPricemax(Float.parseFloat(pricemax));
		} catch (Exception e){}
		advice.setVintage(vintage);
		advice.setType(type);
		advice.setPqratio(pqratio);
		advice.setSubregions(subregions);
		advice.getAdvice();
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		if (advice!=null&&advice.wine!=null){
			String html="<table>";
			for (int i=0;i<advice.wine.length;i++){
		html+="<tr>";
		html+="<td><a href='/wine/"+Webroutines.URLEncode(Knownwines.getKnownWineName(advice.wine[i].Knownwineid)+" "+advice.wine[i].Vintage)+"'>"+advice.wine[i].Name+"</a><br/>("+Dbutil.readValueFromDB("select * from knownwines where id="+advice.wine[i].Knownwineid+";","wine")+", "+Dbutil.readValueFromDB("select * from knownwines where id="+advice.wine[i].Knownwineid+";","appellation")+")</td>";
		html+="<td><a href='/wine/"+Webroutines.URLEncode(Knownwines.getKnownWineName(advice.wine[i].Knownwineid)+" "+advice.wine[i].Vintage)+"'>"+advice.wine[i].Vintage+"</a></td>";
		html+="<td>"+Webroutines.formatPrice(advice.wine[i].PriceEuroIn,(double)0,advice.getCurrency(),"IN")+"</td>";
		if (scale.equals("100")){
			html+="<td><div class='ratingbox'><div class='rating' style='background:#"+Integer.toHexString( 0x100 | Math.min(255,(Math.round((100-advice.rating[i])*256/10)))).substring(1)+Integer.toHexString( 0x100 | Math.min(255,(Math.round((advice.rating[i]-75)*256/15)))).substring(1)+"00;'>"+advice.rating[i]+"</div></div></td>";
		} else {
			html+="<td><div class='twentyratingbox'><div class='twentyrating' style='background:#"+Integer.toHexString( 0x100 | Math.min(255,(Math.round((100-advice.rating[i])*256/10)))).substring(1)+Integer.toHexString( 0x100 | Math.min(255,(Math.round((advice.rating[i]-75)*256/15)))).substring(1)+"00;'>"+(Winerating.twentypoints(advice.rating[i])==(float)Math.round(Winerating.twentypoints(advice.rating[i]))?Math.round(Winerating.twentypoints(advice.rating[i])):Math.round(Winerating.twentypoints(advice.rating[i])-0.5)+"&#189;")+"</div></div></td>";
		}
		if (request.isUserInRole("admin")){
			html+="<td><a href=\"/admin/addknownwine.jsp?id="+advice.wine[i].Knownwineid+"\" target=\"_blank\">Edit wine</a></td>";
		}else {
			html+="<td><a href=\"/test/mistake.jsp?wineid="+advice.wine[i].Id+"&knownwineid="+advice.wine[i].Knownwineid+"\" target=\"_blank\">Report mistake</a></td>";
		}
		html+="</tr>";
		rs=Dbutil.selectQuery("select * from ratedwines where knownwineid="+advice.wine[i].Knownwineid+" and vintage="+advice.wine[i].Vintage+";",con);
		html+="<tr>";
		html+="<td>";
		while (rs.next()){
		html+=rs.getString("author")+": "+rs.getString("rating")+(rs.getInt("ratinghigh")>0?("-"+rs.getString("ratinghigh")):"")+" pts. "+(rs.getString("name").equals(rs.getString("knownwineid"))?"":("for "+rs.getString("name")+". <br/>"));
		
		}
		html+="</td><td></td><td></td><td></td><td></td></tr>";
		
		}
			html+="</table>";
			out.write(html);
		}
		if (advice!=null&&advice.wine!=null){
			Dbutil.closeConnection(con);
			Wineset wineset=new Wineset();
			wineset.Wine=advice.wine;
	%>
	<div id="map" style="width: 675px; height: 400px"></div>
	<%@ include file="/snippets/googlemapbyshop.jsp" %>
	<%} %>

	<%} %>
	
</td>
<td class="right">
		<%out.write(rightad.html); %>
		
	</td></tr>
</table>	
<!--main-->			

<% }%>
</div>

</body> 
</html>
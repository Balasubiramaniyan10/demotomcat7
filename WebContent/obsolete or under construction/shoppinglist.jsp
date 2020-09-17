<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<%
 	long startload=System.currentTimeMillis(); 
       	Webactionlogger logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
 %>
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.online.Shop"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Translator"
%>
<%
	request.setCharacterEncoding("ISO-8859-1");
	session = request.getSession(true); 
	
	// Deal with all variables and post data
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	String shopidstring = Webroutines.filterUserInput(request.getParameter("shopid"));
	if (shopidstring==null||shopidstring.equals("")) shopidstring="0";
	ArrayList<String> shops = Webroutines.getShopList(""); 
	int shopid=0;
	try{	shopid=Integer.valueOf(shopidstring);}
	catch (Exception exc){
	}

	String recommended=Webroutines.filterUserInput(request.getParameter("onlyrecommended"));
	boolean onlyrecommended=true;
	if (recommended!=null&&recommended.equals("N")){
		onlyrecommended=false;
	}
	String prints=Webroutines.filterUserInput(request.getParameter("print"));
	boolean print=false;
	if (prints!=null&&prints.equals("Y")){
		print=true;
	}
%>
	<jsp:setProperty name="searchdata" property="*"/> 
	<%
 		//	 Retrieve currency from cookie if not filled already
 	 	 	 	 	 	 		Cookie[] cookies = request.getCookies();
 	 	 	 	 	 	 	    if (searchdata.getCurrency().equals("")){
 	 	 	 	 	 	 			searchdata.setCurrency(Webroutines.getCookieValue(cookies,"currency","EUR"));
 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	    Cookie currencyCookie =
 	 	 	 	 	 	 	        new Cookie("currency", searchdata.getCurrency());
 	 	 	 	 	 	 	    currencyCookie.setMaxAge(60*60*24*365);
 	 	 	 	 	 	 	    response.addCookie(currencyCookie);
 	 	 	 	 	 	 		//  Retrieve language from cookie if not filled already
 	 	 	 	 	 	 		if (searchdata.getLanguage()==null||searchdata.getLanguage().toString().equals("")){
 	 	 	 	 	 	 		searchdata.setLanguage(Webroutines.getCookieValue(cookies,"language",""));
 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	    if (searchdata.getLanguage()!=null&&!searchdata.getLanguage().toString().equals("")){
 	 	 	 	 	 	 			Cookie languageCookie =
 	 	 	 	 	 	 	    	    new Cookie("language", searchdata.getLanguage().toString());
 	 	 	 	 	 	 	    	languageCookie.setMaxAge(60*60*24*365);
 	 	 	 	 	 	 	    	response.addCookie(languageCookie);
 	 	 	 	 	 	 	    }
 	 	 	 	 	 	 		
 	 	 	 	 	 	 		ArrayList<String> countries = Webroutines.getCountries();
 	 	 	 	 	 	 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
 	%>
<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%
	if (!shopidstring.equals("")){out.print(Spider.escape("vinopedia.com Shopping List for "+Webroutines.getShopNameFromShopId(shopid,"")));} else {out.print("vinopedia Shopping List");}
%></title>
<%
	session.setAttribute("winename",searchdata.getName());
%>
<meta name="verify-v1" content="NCbZ8OZfQYOrTgCcBIMo8RoVHJLwcfCL4klQS+SgiIs=" />
<%@ include file="/header.jsp" %>
<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
function feed(form) {
	actionurl="/showrssurl.jsp?name="+form.name.value;
	actionurl=actionurl+"&amp;vintage="+form.vintage.value;
	actionurl=actionurl+"&amp;pricemin="+form.priceminstring.value;
	actionurl=actionurl+"&amp;pricemax="+form.pricemaxstring.value;
	actionurl=actionurl+"&amp;rareoldstring="+form.rareoldstring.value;
  	document.Searchform.action=actionurl;
	form.submit();
	
  	return 0;
}
-->
</script>
<!-- google_ad_section_end -->

	<%
		// Handle source IP address
		
		if (hostcountry.equals("NZ")||(Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp"))){
			if (hostcountry.equals("NZ")){
			out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
			logger=new Webactionlogger("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
			}
		} else {
			
			Translator.languages language=Translator.getLanguage(searchdata.getLanguage());
		    if (language==null){
		    	language=Translator.getDefaultLanguageForCountry(hostcountry);
		    }
		    Translator t=new Translator();
		    t.setLanguage(language);
			
			// Initialize results in the form of wineset and advertisements

			String region="";
			int knownwineid=0;
			Ad rightad= new Ad(120,600,hostcountry,region,knownwineid,"");
			Ad bottomleftad= new Ad(187,300,hostcountry,region,knownwineid,rightad.partner+"");
			session.setAttribute("hostcountry",hostcountry);
			session.setAttribute("region",region);
			session.setAttribute("knownwineid",knownwineid);

			 if (!print){
	%>
<!-- google_ad_section_start(weight=ignore) -->
<table class="main" >
	<tr><td class="left">
	<%
		if (true){
	%>
		<%=t.get("language")%>: <a href='/index.jsp?name=<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;"))%>&amp;vintage=<%=searchdata.getVintage()%>&amp;language=EN'><img src="/images/flags/english.gif" alt="English" /></a>&nbsp;<a href='/index.jsp?name=<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;"))%>&amp;vintage=<%=searchdata.getVintage()%>&amp;language=NL'><img src="/images/flags/dutch.gif" alt="Nederlands" /></a>
		
		<%
					} else {
													out.print("<br />");
													} 
													if ((request.getHeader("User-Agent")!=null&&(request.getHeader("User-Agent").contains("dows CE")||request.getHeader("User-Agent").toLowerCase().contains("mobile")||request.getHeader("User-Agent").contains("PIE")||request.getHeader("User-Agent").contains("WM5")||request.getHeader("User-Agent").contains("PPC")||request.getHeader("User-Agent").contains("Nokia")||request.getHeader("User-Agent").contains("Symbian")))||(request.getHeader("x-wap-profile")!=null&&!request.getHeader("x-wap-profile").equals(""))) out.write("If you are on a mobile device, click <a href='/mobile.jsp?name="+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage()+"'>here</a>");
				%>
	<form action='/' method="post" id="Searchform" name="Searchform">
	<%
		//=(t.get("preferences")+"<br/>")
	%>
	<%=t.get("displaycurrency")%><br />
		<input type="radio" name="currency" value="EUR" <%if (searchdata.getCurrency().equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio" name="currency" value="GBP" <%if (searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF" <%if (searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD" <%if (searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$
		<table class="search">
		<tr><td>
		<!-- google_ad_section_start(weight=ignore) -->	
		<table class="searchform">
			<tr><td><h4><%=t.get("searchawine")%></h4>
			<%
				if (searchdata.getName().length()<3&&request.getParameter("dosearch")!=null) {
			%><font color='red'><%=t.get("name")%></font><%
				} else {
			%><%=t.get("name")%><%
				}
			%> <a href='/index.jsp?help=true' onmouseover='javascript:document.getElementById("helptext").style.visibility="visible";' onmouseout='javascript:document.getElementById("helptext").style.visibility="hidden";'><%=t.get("help")%></a></td></tr><tr><td><input class="leftfull" type="text" name="name" value="<%=Spider.escape(searchdata.getName())%>"  /></td></tr>
			<tr><td><%=t.get("vintage")%></td></tr><tr><td><input class="leftfull" type="text" name="vintage" value="<%=searchdata.getVintage()%>"  /></td></tr>
			<tr><td><%=t.get("minimumprice")%></td></tr><tr><td><input class="leftfull" type="text" name="priceminstring" value="<%=searchdata.getPriceminstring()%>" /></td></tr>
			<tr><td><%=t.get("maximumprice")%></td></tr><tr><td><input class="leftfull" type="text" name="pricemaxstring" value="<%=searchdata.getPricemaxstring()%>" /></td></tr>
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
			<tr><td><input type="submit" value="<%=t.get("search")%>" />&nbsp;&nbsp;&nbsp;&nbsp;<input type="image" src="/images/xml.bmp" onclick="javascript:feed(this.form);" /></td></tr>
			</table>
		<!--searchform-->
		<input type="hidden" name="dosearch" value="true" />		
		<input type="hidden" name="order" value="" />		
		<input type="hidden" name="rareoldstring" value="false" />
		
	  	
	</td></tr>
	</table>
		
	</form>
	
<!-- google_ad_section_end -->
<%
	out.write(bottomleftad.html);
%>

	<!--search-->
	</td><td class="centre"><%=Webroutines.getConfigKey("systemmessage")%>
	
	<%
			} //print
			
			
			 if (!print){
		%>
	
<h4>Shoppinglist</h4>
<form action="<%=response.encodeURL("shoppinglist.jsp")%>" method="post"  id="formOne">
<table>
<tr><td width="25%">Select a shop to explore</td><td width="75%"><select name="shopid" >
<%
	for (int i=0;i<shops.size();i=i+2){
%>
<option value="<%=shops.get(i)%>"<%if (shops.get(i).equals(shopid)) out.print(" Selected");%>><%=shops.get(i+1).replace("&","&amp;")%></option>
<%
	}
%>
</select></td></tr>
<tr><td colspan='2'><input type="radio" name="onlyrecommended" value="Y" <%if (onlyrecommended) out.print(" checked=\"checked\"");%> />Show only recommended wines<input type="radio" name="onlyrecommended" value="Y" <%if (!onlyrecommended) out.print(" checked=\"checked\"");%> />Show all wines
		</td></tr></table>
    <input type="submit" name="Submit"
       value="Submit" />
</form>
<%
	}
if (shopid>0) {
	out.write("<div><h2>vinopedia.com</h2><h3>Shopping guide for "+Webroutines.getShopNameFromShopId(shopid,"")+"</h3>");
	out.write(Dbutil.readValueFromDB("Select address from shops where id="+shopid+";","address")+"   <a href='shoppinglist.jsp?shopid="+shopid+"&amp;print=Y' target='_blank'>Print this page</a>");
%>	<!--Google Map-->
    <script type="text/javascript">
    function load() {
  		map.addControl(new GSmallMapControl());
  		var bounds = new GLatLngBounds;
  		map.setCenter(new GLatLng(51,5), 4);
		
   <%out.write("var marker;\n");
  	out.write("var gmarkeroptions;\n"); 
  	out.write("var bounds = new GLatLngBounds;");
  		Shop shop=new Shop(shopid+"");
  		if (shop.lat!=0){
			out.write("marker=new GMarker(new GLatLng("+shop.lat+","+shop.lon+"),{title:\""+shop.name+"\"});\n");
			out.write("bounds.extend(new GLatLng("+shop.lat+","+shop.lon+"));");
			String info="";
			info+=shop.name+"<br/>";
			info+=shop.address+"<br/>";
			info=info.replace("'","&apos;");
			out.write("map.addOverlay(marker);\n");
  		}%>
	  map.setCenter(bounds.getCenter());
	  map.setZoom(zoom);
	  	
	}		
	
    </script>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAuPfgtY5yGQowyqWw-A_zlhSWbZnLUFTo4QI7JJ99u1XlFae5FRTIvNMt7rPlWIt8z9eFfYAtlQwM1g"
      type="text/javascript"></script>
	<div>
	<div id="map" style="width: 320px; height: 240px;float:left;"></div>
	<div id="map2" style="width: 320px; height: 240px;float:right;"></div>
	
    <script type="text/javascript">
    var zoom=12;var map = new GMap2(document.getElementById("map"));load();
    zoom=15;map = new GMap2(document.getElementById("map2"));load();</script>
	</div>

	<div style='clear: both;'>	
<%
		out.write(Webroutines.getShoppingList(shopid, searchdata.getCurrency(),onlyrecommended));
		}
	%></div>
	
<%	 if (!print){
			%>
	
	
</td><td class="right">
<%out.write(rightad.html); %>
	</td></tr>
</table>	
<!--main-->		
<jsp:include page="/footer.jsp" />	
<%} %>
<%} //NZ filter %>

</div>

</body> 
</html>
<% long endload=System.currentTimeMillis();

	logger.loadtime=((endload-startload));
	logger.logaction();
%>
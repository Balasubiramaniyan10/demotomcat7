<%@ page contentType="text/html; charset=ISO-8859-1"  pageEncoding="ISO-8859-1"%> 
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.batch.XpathWineScraper"
	import = "com.freewinesearcher.online.Shop"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.common.Context"
	
%> 
<%
 	long startload=System.currentTimeMillis(); 
       	Webactionlogger logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
 %>
<%
	request.setCharacterEncoding("ISO-8859-1");
	session = request.getSession(true); 
	boolean sponsoredresults=false;
	String thispage="index.jsp";
	if (Webroutines.getConfigKey("showsponsoredlinks").equals("true")) sponsoredresults=true;
	// Deal with all variables and post data
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
Context c=(Context)session.getAttribute("context");
if (c==null){
	c=new Context(request);
	session.setAttribute("context",c);
}
if (c.an==null||c.an.config==null) {
	out.write("<html><body><script type='text/javascript'>window.location='quickanalyzer.jsp';</script></body></html>");
}	else {

int numberofrows=Webroutines.numberofnormalrows;
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	String map=Webroutines.filterUserInput(request.getParameter("map"));
	if (map==null) map="";
	if (offset==null||offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="order" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="country" value=""/> 
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
 		 		 		 		 		 		 		 			
 	 	 	 	 	 	 	 		boolean help=false;
 	 	 	 	 	 	 	 		if ("true".equals(request.getParameter("help"))) help=true;
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
 	 	 	 	 	 	 	 		
 	 	 	 	 	 	 	 		Translator.languages language=Translator.getLanguage(searchdata.getLanguage());
 	 	 	 	 	 	 	 	    if (language==null){
 	 	 	 	 	 	 	 	    	language=Translator.getDefaultLanguageForCountry("UK");
 	 	 	 	 	 	 	 	    }
 	 	 	 	 	 	 	 	    Translator t=new Translator();
 	 	 	 	 	 	 	 	    t.setLanguage(language);
 	%>
<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%
	if (!searchdata.getName().equals("")){out.print(Spider.escape(searchdata.getName())+" "+t.get("pricesbyfws"));} else {out.print(t.get("sitetitle"));}
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
			
			
			// Initialize results in the form of wineset and advertisements
			XpathWineScraper xpws=new XpathWineScraper(c.an.domainurl,c.an.url,null,c.an.shopid );
			Wine[] winesfound=xpws.getWines(c.an.result); 
			Wineset wineset =new Wineset();
			wineset.Wine=winesfound;
			wineset.records=winesfound.length;
			Wineset sponsoredwineset=null;
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
	%>
<!-- google_ad_section_start(weight=ignore) -->
<table class="main" >
	<tr><td class="left">



	</td><td class="centre"><%=Webroutines.getConfigKey("systemmessage")%><div id='helptext' class='helptext' ></div>
	
		<h4>Search results (<%out.print(wineset.records+" "+t.get("winesfound")+"). <br /></h4>"+t.get("permalink")+": <a href='/wine/"+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'>"+"https://www.vinopedia.com/wine/"+Spider.escape(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"</a><br />");

							
																				
																				// Show the links to the rest of the results
																				for (int i=0;i<wineset.records;i=i+numberofrows){
																				 out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset="+i)+"' style='{color: blue;}'>");
																				 if (Integer.toString(i).equals(offset)) out.print("<b>");
																				 out.print(t.get("page")+"&nbsp;"+(i/numberofrows+1)+" ");
																				 if (Integer.toString(i).equals(offset)) out.print("</b>");
																				 out.print("</a>");
																				
																				
																			}
																			out.print("<script type=\"text/javascript\">addthis_url = 'https://www.vinopedia.com/wine/"+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'; addthis_title  = document.title;  addthis_pub    = 'vinopedia';</script><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\" ></script>");
																			if (wineset!=null&&wineset.Wine.length>0&&Webroutines.getConfigKey("map").equals("true")) {
																				if (map.equals("true")){
																					out.print("&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\">Show results as list</a>");
																				} else {
																					out.print("&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='true';document.getElementById('Searchform').submit();\"><img src='/images/bottles/Map.png' alt='Show on map'/>&nbsp;New! Show results on map</a>");
																				}
																			}
																			out.print("<br/>");
																			
																			
						%>
	<table class="results"><tr><th class="flag"></th><th class="shop"><%=t.get("store")%></th><th class="name"><%=t.get("wine")%></th><th class="vintage"><%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"vintage")).replace("'","&apos;")+"'>"+t.get("vintage")+"</a>");
	%></th><th class="size" align="right">&nbsp;&nbsp;&nbsp;<%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"size")).replace("'","&apos;")+"'>"+t.get("size")+"</a>&nbsp;&nbsp;&nbsp;");
	%></th><th class="price" align="right"><%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"priceeuroex")).replace("'","&apos;")+"'>"+t.get("price")+"</a>");
	%></th></tr>    
			<%
    				NumberFormat format  = new DecimalFormat("#,##0.00");	
    			    			    			    				
    			    			    			    				
    			    			    			    				// Give the complete result list
    			    			    			    				for (int i=0;i<wineset.Wine.length;i++){
    			    			    			    						out.print("<tr");
    			    			    			    						if (wineset.Wine[i].CPC>0&&sponsoredresults){
    			    			    			    							out.print(" class=\"sponsoredeven\"");
    			    			    			    						} else {
    			    			    			    							if (i%2==1){out.print(" class=\"odd\"");}
    			    			    			    						}
    			    			    			    						out.print (">");
    			    			    			    						out.print("<td class='flag'><a href='"+thispage+"?name="+(Webroutines.URLEncode(searchdata.getName())+"&amp;country="+wineset.Wine[i].Country.toUpperCase()+"&amp;vintage="+searchdata.getVintage()).replace("'","&apos;")+"' target='_blank'><img src='/images/flags/"+wineset.Wine[i].Country.toLowerCase()+".gif' alt='"+wineset.Wine[i].Country.toLowerCase()+"' /></a></td>");
    			    			    			    						out.print("<td><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"&amp;shopid="+wineset.Wine[i].ShopId+"' target='_blank'>"+wineset.Wine[i].Shopname.replace("&","&amp;")+"</a></td>");
    			    			    			    						out.print("<td><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"' target='_blank'>"+Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name))+"</a></td>");
    			    			    			    						out.print("<td>" + (wineset.Wine[i].Vintage.equals("0")?"":wineset.Wine[i].Vintage)+"</td>");
    			    			    			    						out.print("<td align='right'>" + Webroutines.formatSize(wineset.Wine[i].Size)+"</td>");
    			    			    			    						out.print("<td class='price' align='right'>" + Webroutines.formatPrice(wineset.Wine[i].PriceEuroIn,wineset.Wine[i].PriceEuroEx,searchdata.getCurrency(),searchdata.getVat())+"</td>");
    			    			    			    						out.print("</tr>");
    			    			    			    				}
    			%>
	</table>
	<!--results-->	
	
	<font size="1"><br /><%=t.get("pricenote")%><br /></font>
	
</td><td class="right">
<%out.write(rightad.html); %>
	</td></tr>
</table>	
<script type="text/javascript">
<!--
document.getElementById("helptext").innerHTML="<%=t.get("helptext")%>";
-->
</script>	
<!--main-->		
<jsp:include page="/footer.jsp" />	
<%} //NZ filter %>

</div>

</body> 
</html>
<% long endload=System.currentTimeMillis();

	logger.loadtime=((endload-startload));
	logger.logaction();
	
}//xp=null
%>
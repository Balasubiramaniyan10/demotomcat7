<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%> 
<%
 	long startload=System.currentTimeMillis(); 
        	Webactionlogger logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);

        	int numberofresults=0;
 %>
	<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.Shop"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Translator"
	
	%><%
		request.setCharacterEncoding("ISO-8859-1");
			session = request.getSession(true); 
			boolean sponsoredresults=false;
			String thispage="newindex.jsp";
			if (Webroutines.getConfigKey("showsponsoredlinks").equals("true")) sponsoredresults=true;
	%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	int numberofrows=Webroutines.numberofnormalrows;
	Wineset wineset=null;
	
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
 				 				 				 				 				 				 				 					// Analyse Google query and reuse query on Google instead of Google's link
 				 				 				 				 				 				 				 					if (request.getHeader("Referer")!=null&&(request.getHeader("Referer").contains("google")||request.getHeader("Referer").contains("hammink"))){
 				 				 				 				 				 				 				 						int start=request.getHeader("Referer").indexOf("&q=")+3;
 				 				 				 				 				 				 				 						if (start==0) start=request.getHeader("Referer").indexOf("?q=")+3;
 				 				 				 				 				 				 				 						if (start>0) {
 				 				 				 				 				 				 				 					int end=request.getHeader("Referer").indexOf("&",start+2);
 				 				 				 				 				 				 				 					if (end>start) {
 				 				 				 				 				 				 				 						String googlequery=request.getHeader("Referer").substring(start,end).replaceAll("\\+"," ");
 				 				 				 				 				 				 				 						googlequery=URLDecoder.decode(googlequery,"UTF-8");
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll("[Ww]ine","");
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll("[Ff]ree","");
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll("[Ss]earcher","");
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll("[Ss]earch","");
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll("[Pp]rice","");
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll("\\d+","").trim();
 				 				 				 				 				 				 				 						googlequery=googlequery.replaceAll(" +"," ");
 				 				 				 				 				 				 				 						googlequery=Webroutines.filterUserInput(googlequery);
 				 				 				 				 				 				 				 						if (googlequery.length()>3){
 				 				 				 				 				 				 				 							Wineset tempwineset=new Wineset(googlequery,searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),1);
 				 				 				 				 				 				 				 							if (tempwineset.records>5){
 				 				 				 				 				 				 				 								searchdata.setName(googlequery);
 				 				 				 				 				 				 				 								
 				 				 				 				 				 				 				 							}			
 				 				 				 				 				 				 				 						}
 				 				 				 				 				 				 				 					}
 				 				 				 				 				 				 				 						}
 				 				 				 				 				 				 				 					}
 				 				 				 				 				 				 				 					
 				 				 				 				 				 				 				 					if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")){
 				%>
		<jsp:setProperty name="searchdata" property="vintage" value="<%=searchdata.getVintage().concat("ss ")+Webroutines.getVintageFromName(searchdata.getName())%>"/> 
		<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/> 
	<%
 		}
 	 	 	 	 	 	 	 		Wineset sponsoredwineset=null;
 	 	 	 	 	 	 	 		
 	 	 	 	 	 	 	 		if ("true".equals(request.getParameter("fuzzy"))){
 	 	 	 	 	 	 	 			if (searchdata.getName().length()>2) {
 	 	 	 	 	 	 	 		  		// For now, on this page sponsored links have been disabled
 	 	 	 	 	 	 	 		// Wineset sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 		String newname=Knownwines.determineKnownWine(searchdata.getName());
 	 	 	 	 	 	 	 		  		wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 		Wineset winesetfuzzy=new Wineset(newname,searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 		if (winesetfuzzy!=null&&(wineset==null||winesetfuzzy.records>=wineset.records)){
 	 	 	 	 	 	 	 			searchdata.setName(newname); 	 
 	 	 	 	 	 	 	 			wineset=winesetfuzzy;
 	 	 	 	 	 	 	 			if (sponsoredresults){
 	 	 	 	 	 	 	 				sponsoredwineset = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 			}
 	 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	 			}
 	 	 	 	 	 	 	 		} else {
 	 	 	 	 	 	 	 	  		wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 	  		sponsoredwineset=null;
 	 	 	 	 	 	 	 			
 	 	 	 	 	 	 	 	  		if (wineset==null||(wineset.searchtype.equals("smart")&&wineset.records==0)){
 	 	 	 	 	 	 	 		wineset = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 		if (sponsoredresults){
 	 	 	 	 	 	 	 			sponsoredwineset = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	 			} else {
 	 	 	 	 	 	 	 		if (sponsoredresults){
 	 	 	 	 	 	 	 			sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
 	 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	 			}
 	 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	 		
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
 	 	 	 	 	 	 	 		session.setAttribute("language",language);
 	%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%
	if (!searchdata.getName().equals("")){out.print(Spider.escape(searchdata.getName())+" "+t.get("pricesbyfws"));} else {out.print("vinopedia");}
%></title>
<%
	session.setAttribute("winename",searchdata.getName());
%>
<%@ include file="/header.jsp" %>

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
<script language="JavaScript" type="text/javascript" src="/js/suggest.js?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime()).toString()%>"></script>
<!-- google_ad_section_end -->

	<%
		// Handle source IP address
		if (hostcountry.equals("NZ")||(Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp"))){
			if (hostcountry.equals("NZ")){
			out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
			logger=new Webactionlogger("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
			}
		} else {
	%>

<table class="main" onclick="javascript:emptySuggest();">
	<tr><td class="left">
	<%
		if (request.getHeader("User-Agent")!=null&&(request.getHeader("User-Agent").contains("dows CE")||request.getHeader("User-Agent").contains("PIE")||request.getHeader("User-Agent").contains("WM5")||request.getHeader("User-Agent").contains("PPC")||request.getHeader("User-Agent").contains("Nokia")||request.getHeader("User-Agent").contains("Symbian"))) out.write("If you are on a mobile device, click <a href='/mobile.jsp?name="+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage()+"'>here</a>");
		if (true){
	%>
		<%=t.get("language")%>: <a href='/lang-EN/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;")+" "+searchdata.getVintage())%>'><img src="/images/flags/english.gif" alt="English" /></a>&nbsp;<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;"))+" "+searchdata.getVintage()%>'><img src="/images/flags/french.gif" alt="Français" /></a>&nbsp;<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;")+" "+searchdata.getVintage())%>'><img src="/images/flags/dutch.gif" alt="Nederlands" /></a>
		
		<%
					} else {
																	out.print("<br />");
																	}
				%>
	
	
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
			%> <a href='/index.jsp?help=true' onmouseover='javascript:document.getElementById("helptext").style.visibility="visible";' onmouseout='javascript:document.getElementById("helptext").style.visibility="hidden";'><%=t.get("help")%></a></td></tr><tr><td><input class="leftfull" type="text" name="name" autocomplete="off" id="name" value="<%=Spider.escape(searchdata.getName())%>" size="25" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" /><div id="search_suggest" class="search_suggest_noborder" ></div></td></tr>
			<tr><td><%=t.get("vintage")%></td></tr><tr><td><input class="leftfull" type="text" name="vintage" autocomplete="off" value="<%=searchdata.getVintage()%>"  /></td></tr>
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
		<input type="hidden" id="map" name="map" value="" />		
		<input type="submit" value="<%=t.get("search")%>" />
	  	<input type="image" src="/images/xml.bmp" onclick="javascript:feed(this.form);" />
	</td></tr>
	</table>
  	</form>

<%
	if (wineset!=null&&wineset.knownwineid>0){
	if (new File(Wijnzoeker.basedir+"images\\wines\\"+wineset.knownwineid+".gif").exists()){
		out.print ("<img src='/images/wines/"+wineset.knownwineid+".gif' alt='"+searchdata.getName().replace("'","&apos;")+"' />");
	} else {
		if (new File(Wijnzoeker.basedir+"images\\wines\\"+wineset.knownwineid+".jpg").exists()){
	out.print ("<img src='/images/wines/"+wineset.knownwineid+".jpg' alt='"+searchdata.getName().replace("'","&apos;")+"' />");
		}
	}
		
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
	Ad betweenresults= new Ad(646,60,hostcountry,region,knownwineid,rightad.partner+","+bottomleftad.partner);
	session.setAttribute("hostcountry",hostcountry);
	session.setAttribute("region",region);
	session.setAttribute("knownwineid",knownwineid);
	
	out.write(bottomleftad.html);
%>
	<!--search-->
	
	</td><td class="centre"><%=Webroutines.getConfigKey("systemmessage")%><div id='helptext' class='helptext' ></div>
	
	<%
			if (searchdata.getName().length()<3) { 
				logger=new Webactionlogger("Pageloadnewindex",request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0,0);
		%>
	
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start -->
			<h4><%=t.get("welcome")%></h4><br />
			<%=t.get("welcometextsmartsearch")%>
			<br />&nbsp;<br />
			<h4><%=t.get("todaystips")%></h4>
			<%=t.get("tiptext")%>
			<%
				out.print(Webroutines.getTipsHTML("",(float)0.75,10,"newindex.jsp",t.language));
								out.print((Webroutines.getTipsHTML("",(float)0.75,10,"newindex.jsp",t.language)).equals("")?("<br />"+t.get("notips")):("<font size=\"1\"><br />"+t.get("pricenote")+"<br /></font>"));
			%>
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start(weight=ignore) -->	
			
			
	<!--hints-->
	<!-- google_ad_section_end -->
	
	<%
			out.print("<script type=\"text/javascript\">addthis_url = location.href; addthis_title  = document.title;  addthis_pub    = 'vinopedia';</script><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\" ></script><br/>");
				if (request.getParameter("dosearch")!=null) {
			out.print("<br /><br /><font color='red'>"+t.get("3characters")+"</font>");
			}
			}
			
			if (searchdata.getName().length()>2) {
				String tip="";
				if ("true".equals(request.getParameter("tip"))) tip= "Tip ";
				logger=new Webactionlogger(tip+"Search",request.getServletPath(),ipaddress,  request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0,wineset.records);
				if (wineset==null||wineset.Wine.length==0){
			//Wineset winesetalt= new Wineset(Knownwines.bestMatchKnownWine(searchdata.getName()),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
			out.print(t.get("noresultsfound"));
			//if (winesetalt==null||winesetalt.Wine.length==0){
			//} else {
			//	out.print(". Were you looking for <a href='/wine/"+Knownwines.getKnownWineName(winesetalt.knownwineid)+" "+searchdata.getVintage()+"'>"+Knownwines.getKnownWineName(winesetalt.knownwineid)+" "+searchdata.getVintage()+"</a>? ("+winesetalt.records+" results)<br/>");
			//}
			boolean othervintagefound=false;
			if (!searchdata.getVintage().equals("")){
				wineset = new Wineset(searchdata.getName(),"", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
				if (wineset!=null&&wineset.Wine.length!=0){
					out.print(" "+t.get("forvintage")+" "+searchdata.getVintage()+". "+t.get("othervintages"));
					searchdata.setVintage("");
					othervintagefound=true;
					if (sponsoredresults){
						sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows);
					}
				}
			}
			out.print(".<br/>");
			if (!othervintagefound){
				ArrayList<String> alternatives=com.freewinesearcher.common.Knownwines.getAlternatives(searchdata.getName());
				if (alternatives.size()>0){	
					out.write ("<br/><br/>"+t.get("alternatives")+"<br/>");
					for (int i=0;i<alternatives.size();i=i+3){
						out.write("<a href='/wine/"+Webroutines.URLEncode(alternatives.get(i+1))+"'>"+alternatives.get(i)+ "</a>&nbsp;("+alternatives.get(i+2)+" "+(alternatives.get(i+2).equals("1")?t.get("wine"):t.get("wines"))+")<br/>");
					}
				}
			}
				}
				if (wineset!=null&&wineset.Wine.length!=0){
		%><h4><%=t.get("searchresultsfor")%> <%
 	out.print(Spider.escape(searchdata.getName())+" "+searchdata.getVintage());
 %> (<%
 	out.print(wineset.records+" "+t.get("winesfound")+"). <br />");
    	out.print("</h4>"+t.get("permalink")+": <a href='/wine/"+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'>"+"https://www.vinopedia.com/wine/"+Spider.escape(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"</a><br />");
 %>
			<%
				if (!wineset.searchtype.equals("text")){
								int singlevintage=0;
								try {
									singlevintage=Integer.parseInt(searchdata.getVintage().trim());
								} catch (Exception e){}
								out.print(Webroutines.getRatingsHTML(wineset.knownwineid,19,"/newindex.jsp",singlevintage));
								out.print("<br/>");
							}
								if (false&&wineset.records>numberofrows){ //Disabled
								// Show the links to the rest of the results
								for (int i=0;i<wineset.records;i=i+numberofrows){
								 out.print("<a href='"+response.encodeURL("/index.jsp?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset="+i)+"' style='{color: blue;}'>");
								 if (Integer.toString(i).equals(offset)) out.print("<b>");
								 out.print(t.get("page")+"&nbsp;"+(i/numberofrows+1)+" ");
								 if (Integer.toString(i).equals(offset)) out.print("</b>");
								 out.print("</a>");
								
								}
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
								
								out.write(betweenresults.html); 
							if (!map.equals("true")){
							 	int bestid=0;
								if (wineset.knownwineid>0) {
							bestid=wineset.knownwineid;
								} else {
							if (wineset.knownwinelist.size()>0){
								bestid=	wineset.knownwinelist.get(0).get(0);
							}
							if (bestid==0&&wineset.knownwinelist.size()>1){
								bestid=	wineset.knownwinelist.get(1).get(0);
							}
							
								}
								if (searchdata.getName().length()>3){
			%>
			<div id='tastingnote' style='border-style:solid;border-color:blue;background-color:white;visibility:hidden;position:absolute;z-index:999;\' onMouseOver='javascript:document.getElementById("tastingnote").style.visibility = "visible";' onMouseOut='javascript:document.getElementById("tastingnote").style.visibility = "hidden;";'>
	Tasting notes by <a href='http://www.scrugy.com' target='_blank'>Scrugy</a>:
	<script language="JavaScript" src="http://feed2js.org//feed2js.php?src=http%3A%2F%2Fscrugy.com%2Fsearch%3Fq%3D<%=((searchdata.getName()+" "+searchdata.getVintage()).replace(" ","%2B"))%>%26s%3Dtns%26format%3Drss%26hpp%3D5&amp;targ=y&amp;utf=n&amp;html=a" type="text/javascript"></script>

	<noscript>
	<a href="http://feed2js.org//feed2js.php?src=http%3A%2F%2Fscrugy.com%2Fsearch%3Fq%3Dquatro%2Bventos%2B2003%26s%3Dtns%26format%3Drss&amp;chan=y&amp;desc=1&amp;utf=y&amp;html=y">View RSS feed</a>
	</noscript>
	</div>
	<div id='show' onMouseOver='javascript:document.getElementById("tastingnote").style.visibility = "visible";' onMouseOut='javascript:document.getElementById("tastingnote").style.visibility = "hidden;";'>Show TN</div>
	</div>
			<%
				}
								if (bestid>0){
							 		String youmayalsolike=Webroutines.youMayAlsoLikeHTML(bestid,searchdata);
							 		if (!"".equals(youmayalsolike)){
							 			out.write("<div id=\"youmayalsolike\" style=\"border-style:solid;border-color:blue;background-color:white;visibility:hidden;position:absolute;z-index:999;\" onmouseover='javascript:document.getElementById(\"youmayalsolike\").style.visibility=\"visible\";' onmouseout='javascript:document.getElementById(\"youmayalsolike\").style.visibility=\"hidden\";'>");
								out.write(youmayalsolike);		
								out.write("</div><br/><img src=\"images/ifyoulikethis.jpg\" alt=\"You may also like...\" style=\"align:left;\" onmouseover='javascript:document.getElementById(\"youmayalsolike\").style.visibility=\"visible\";' onmouseout='javascript:document.getElementById(\"youmayalsolike\").style.visibility=\"hidden\";' /><br/>");
								
							 		}
								}
			%>
	<table class="results"><tr><th class="flag"></th><th class="shop"><%=t.get("store")%></th><th class="name"><%=t.get("wine")%></th><th class="vintage"><%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"vintage")).replace("'","&apos;")+"'>"+t.get("vintage")+"</a>");
	%></th><th class="size" align="right">&nbsp;&nbsp;&nbsp;<%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"size")).replace("'","&apos;")+"'>"+t.get("size")+"</a>&nbsp;&nbsp;&nbsp;");
	%></th><th class="price" align="right"><%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"priceeuroin")).replace("'","&apos;")+"'>"+t.get("price")+"</a>");
	%></th></tr>    
			<%
    				NumberFormat format  = new DecimalFormat("#,##0.00");	
    			    			    			    				
    			    			    			    				if (sponsoredresults){
    			    			    			    				
    			    			    			    				if (sponsoredwineset!=null&&sponsoredwineset.records>0) {	
    			    			    			    					out.print("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
    			    			    			    					for (int i=0;i<sponsoredwineset.Wine.length;i++){
    			    			    			    							out.print("<tr");
    			    			    			    							if (i%2==1)out.print(" class=\"sponsoredodd\"");
    			    			    			    							if (i%2==0)out.print(" class=\"sponsoredeven\"");
    			    			    			    							out.print (">");
    			    			    			    							out.print("<td class='flag'><a href='"+thispage+"?name="+(Webroutines.URLEncode(searchdata.getName())+"&amp;country="+wineset.Wine[i].Country.toUpperCase()+"&amp;vintage="+searchdata.getVintage()).replace("'","&apos;")+"' target='_blank'><img src='/images/flags/"+sponsoredwineset.Wine[i].Country.toLowerCase()+".gif' alt='"+sponsoredwineset.Wine[i].Country.toLowerCase()+"' /></a></td>");
    			    			    			    							out.print("<td><a href="+response.encodeURL("link.jsp?shopid="+sponsoredwineset.Wine[i].ShopId)+" target='_blank'>"+sponsoredwineset.Wine[i].Shopname+"</a></td>");
    			    			    			    							out.print("<td><a href="+response.encodeURL("link.jsp?wineid="+sponsoredwineset.Wine[i].Id)+" target='_blank'>"+sponsoredwineset.Wine[i].Name+"</a></td>");
    			    			    			    							out.print("<td>" + sponsoredwineset.Wine[i].Vintage+"</td>");
    			    			    			    							out.print("<td align='right'>" + Webroutines.formatSize(sponsoredwineset.Wine[i].Size)+"</td>");
    			    			    			    							out.print("<td align='right'>"+Webroutines.formatPrice(sponsoredwineset.Wine[i].PriceEuroIn,sponsoredwineset.Wine[i].PriceEuroEx,searchdata.getCurrency(),searchdata.getVat())+"</td>");
    			    			    			    							out.print("</tr>");
    			    			    			    					}
    			    			    			    					out.print("<tr><td colspan='4'><i>All results:</i></td></tr>");
    			    			    			    				
    			    			    			    				} else {	
    			    			    			    					out.print("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
    			    			    			    					out.print("<tr class=\"sponsoredeven\">");
    			    			    			    					out.print("<td colspan='2'><a href='"+response.encodeURL("/sponsoring.jsp")+"'></a></td>");
    			    			    			    					out.print("<td colspan='2'><a href='"+response.encodeURL("/sponsoring.jsp")+"'>Your wine could be listed here! Click for more information.</a></td>");
    			    			    			    					out.print("<td align='right'></td>");
    			    			    			    					out.print("<td align='right'>&euro; 0.10</td>");
    			    			    			    					out.print("</tr>");
    			    			    			    					out.print("<tr><td colspan='4'><i>All results:</i></td></tr>");
    			    			    			    				}
    			    			    			    				}	
    			    			    			    				
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
	<%
			} else {
				// Show on map
		%>
		<%@ include file="/snippets/googlemap.jsp" %><%
			}
		%>
	<font size="1"><br /><%=t.get("pricenote")%><br /></font>
	<%
		if (wineset.records>numberofrows){

			// Show the links to the rest of the results again at the bottom

			for (int i=0;i<wineset.records;i=i+numberofrows){
			 out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset="+i)+"' style='{color: blue;}'>");
			 if (Integer.toString(i).equals(offset)) out.print("<b>");
			 out.print(t.get("page")+"&nbsp;"+(i/numberofrows+1)+" ");
			 if (Integer.toString(i).equals(offset)) out.print("</b>");
			 out.print("</a>");
			}
		}			
			}		
		}
	%>
</td><td class="right">
<%out.write(rightad.html); %>
	</td></tr>
</table>	
<script type="text/javascript">
<!--
document.getElementById("helptext").innerHTML="<%@ include file="/helptext.txt" %>";
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
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%
 	long startload = System.currentTimeMillis();
 	Webactionlogger logger = new Webactionlogger("Pageload", request
 			.getServletPath(), request.getRemoteAddr(), request
 			.getHeader("referer"), "", "", 0, (float) 0.0, (float) 0.0,
 			"", false, "", "", "", "", (double) 0.0, 0);

 	int numberofresults = 0;
 %><%@ page   
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
	import = "com.freewinesearcher.online.WineLibraryTV"
	
	%><%
		request.setCharacterEncoding("ISO-8859-1");
		session = request.getSession(true);
		boolean sponsoredresults = false;
		String thispage = "indexnewstyle.jsp";
		String referrer = "";
		WineLibraryTV wltv;
		boolean fuzzy = false;
		String youmayalsolike = "";
		if (Webroutines.getConfigKey("showsponsoredlinks").equals("true"))
			sponsoredresults = true;
%>	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
	<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/><%
	
	int numberofrows = Webroutines.numberofnormalrows;
	Wineset wineset = null;
	String offset = Webroutines.filterUserInput(request
			.getParameter("offset"));
	String map = Webroutines.filterUserInput(request
			.getParameter("map"));
	if (map == null)
		map = "";
	if (offset == null || offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset = "0";
%>		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="order" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="country" value=""/> 
		<jsp:setProperty name="searchdata" property="priceminstring" value=""/> 
		<jsp:setProperty name="searchdata" property="pricemaxstring" value=""/> 
		<jsp:setProperty name="searchdata" property="*"/> 
		<jsp:setProperty name="searchdata" property="offset" value="0"/>
<%
	} else {
%>
		<jsp:setProperty name="searchdata" property="*"/> 
<%
 	} // Analyse Google query and reuse query on Google instead of Google's link
 	if (request.getHeader("Referer") != null
 			&& (request.getHeader("Referer").contains("google") || request
 					.getHeader("Referer").contains("hammink"))) {
 		referrer = request.getHeader("Referer");
 	}
 	if (!Webroutines.getVintageFromName(searchdata.getName())
 			.equals("")) {
 %>		<jsp:setProperty name="searchdata" property="vintage" value="<%=searchdata.getVintage().concat("ss ")
						+ Webroutines.getVintageFromName(searchdata.getName())%>"/> 
		<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata
								.getName())%>"/> <%
 		}
 		if ("true".equals(request.getParameter("fuzzy"))) {
 			fuzzy = true;
 		}
 		wineset = Wineset.getWineset(searchdata, referrer, numberofrows,
 				fuzzy, false);

 		//	 Retrieve currency from cookie if not filled already
 		Cookie[] cookies = request.getCookies();
 		if (searchdata.getCurrency().equals("")) {
 			searchdata.setCurrency(Webroutines.getCookieValue(cookies,
 					"currency", "EUR"));
 		}
 		Cookie currencyCookie = new Cookie("currency", searchdata
 				.getCurrency());
 		currencyCookie.setMaxAge(60 * 60 * 24 * 365);
 		response.addCookie(currencyCookie);
 		//  Retrieve language from cookie if not filled already
 		if (searchdata.getLanguage() == null
 				|| searchdata.getLanguage().toString().equals("")) {
 			searchdata.setLanguage(Webroutines.getCookieValue(cookies,
 					"language", ""));
 		}
 		if (searchdata.getLanguage() != null
 				&& !searchdata.getLanguage().toString().equals("")) {
 			Cookie languageCookie = new Cookie("language", searchdata
 					.getLanguage().toString());
 			languageCookie.setMaxAge(60 * 60 * 24 * 365);
 			response.addCookie(languageCookie);
 		}

 		ArrayList<String> countries = Webroutines.getCountries();
 		if (searchdata.getVat() == null || searchdata.getVat().equals(""))
 			searchdata.setVat(Webroutines.getCountryCodeFromIp(request
 					.getRemoteAddr()));

 		Translator.languages language = Translator.getLanguage(searchdata
 				.getLanguage());
 		if (language == null) {
 			language = Translator.getDefaultLanguageForCountry("UK");
 		}
 		Translator t = new Translator();
 		t.setLanguage(language);
 		session.setAttribute("language", language);
 	%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%
	if (!searchdata.getName().equals("")) {
		out.print(Spider.escape(searchdata.getName()) + " "
				+ t.get("pricesbyfws"));
	} else {
		out.print("vinopedia");
	}
%></title>
<%
	request.setAttribute("winename", searchdata.getName());
%>
<%@ include file="/header2.jsp" %>
</head><body onclick="javascript:emptySuggest();">
<div class='topbar'>
<div id='mobile'><a href='/mobile.jsp?name="+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage()+"'>Mobile access</a></div>
<div id='language'><%=t.get("language")%>: <a href='/lang-EN/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'",
							"&apos;")
							+ " " + searchdata.getVintage())%>'><img src="/images/flags/english.gif" alt="English" /></a>&nbsp;<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'",
							"&apos;")) + " " + searchdata.getVintage()%>'><img src="/images/flags/french.gif" alt="Français" /></a>&nbsp;<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'",
							"&apos;")
							+ " " + searchdata.getVintage())%>'><img src="/images/flags/dutch.gif" alt="Nederlands" /></a></div>
&nbsp;
</div>
<div class="logo"></div>
<div class="search">
	<div id="left" class="column"></div>
	<div id="center" class="column">
		<h1>Find your favorite wine for the best price</h1>
		<form action='/<%=thispage%>' method="post" id="searchform" name="searchform"><input type="hidden" name="dosearch" value="true" /><input class='searchinput' id='name' type='text' autocomplete="off" name="name" value="<%=Spider.escape(searchdata.getName())%>" size="25" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" /></form>
		<div id="search_suggest" class="search_suggest_hidden" ></div>
	</div>
  	<div id="right" class="column">
		<img class='searchgo' src='/css/searchgo.png' onclick='dosearch' alt='Search'/>
	</div>
</div>

<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
//document.getElementById("searchinput").focus();
function feed(form) {
	actionurl="/showrssurl.jsp?name="+form.name.value;
	actionurl=actionurl+"&vintage="+form.vintage.value;
	document.Searchform.action=actionurl; 
	form.submit();
	
  	return 0;
}
-->
</script>
<script type="text/javascript" src="/js/Dojo/dojo/dojo.js" djConfig="parseOnLoad: true">
</script>
<script type="text/javascript"> 
        dojo.require("dojo.dnd.Source"); // capital-S Source in 1.0
        dojo.require("dojo.parser");	
        dojo.require("dojo.fx");	
</script>
<script language="JavaScript" type="text/javascript" src="/js/tn.js?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime())
							.toString()%>"></script>
<script language="JavaScript" type="text/javascript" src="/js/advice.jsp?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime())
							.toString()%>"></script>
<script language="JavaScript" type="text/javascript" src="/js/suggest.js?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime())
							.toString()%>"></script>
<!-- google_ad_section_end -->

	<%
		// Handle source IP address
		if (hostcountry.equals("NZ")
				|| (Webroutines.ipBlocked(ipaddress)
						&& !request.getServletPath().contains(
								"savecontact.jsp") && !request
						.getServletPath().contains("abuse.jsp"))) {
			if (hostcountry.equals("NZ")) {
				out.print("An error occurred at line: 17 in the jsp file: /index.jsp");
				logger = new Webactionlogger("NZ: Access denied", request
						.getServletPath(), ipaddress, request
						.getHeader("referer"), "", "", 0, (float) 0.0,
						(float) 0.0, "", true, "", "", "", "", 0.0, 0);
			}
		} else {
	%>


	
<%
		

			String region = "";
			int knownwineid = 0;
			if (wineset != null) {
				region = wineset.region;
				knownwineid = wineset.knownwineid;
				if (region == null || region.equals("")) {
					if (knownwineid > 0) {
						region = Dbutil.readValueFromDB(
								"Select * from knownwines where id="
										+ knownwineid + ";", "appellation");
					}
				}
			}
			Ad rightad = new Ad(120, 600, hostcountry, region, knownwineid,
					"");
			Ad bottomleftad = new Ad(187, 300, hostcountry, region,
					knownwineid, rightad.partner + "");
			Ad betweenresults = new Ad(646, 60, hostcountry, region,
					knownwineid, rightad.partner + ","
							+ bottomleftad.partner);
			session.setAttribute("hostcountry", hostcountry);
			session.setAttribute("region", region);
			session.setAttribute("knownwineid", knownwineid);
	%><%=Webroutines.getConfigKey("systemmessage")%>
<div class='clear'/>	
<div id='adright'><%
		out.write(rightad.html);
	%></div>
<div id='main'>	
	<%
			if (searchdata.getName().length() < 3) {
					logger = new Webactionlogger("Pageloadindex2", request
							.getServletPath(), request.getRemoteAddr(), request
							.getHeader("referer"), searchdata.getName(),
							searchdata.getVintage(), searchdata.getCreated(),
							searchdata.getPricemin(), searchdata.getPricemax(),
							searchdata.getCountry(), searchdata.getRareold(),
							"", "", "", "", 0.0, 0);
		%>

	<h2><%=t.get("todaystips")%></h2>
			<%=t.get("tiptext")%>
			<%out.print(Webroutines.getTipsHTML3("", (float) 0.75, 9,"index2.jsp", t.language, searchdata));
			out.print((Webroutines.getTipsHTML3("", (float) 0.75, 9, "index2.jsp", t.language, searchdata)).equals("") ? ("<br />" + t.get("notips")):("<div class='pricenote'>"+ t.get("pricenote") + "</div>"));%>
		<div class='announcements'>
		<div class='column'>
		<h2>News</h2>
	2008/05/26 Do you want to buy priceless wines, but you don't want to pay
	too much? Are you trying to find a wine from your year of birth? Or do
	you think matured Bordeaux is hard to find? <br />
	vinopedia, a search engine especially for wines, can help you
	out! <br />
	<br />
	<a href='/news/test.jsp'>More news</a>
	</div>
		<div class='column'>
		<h2>Vendors</h2>
	Getting listed on vinopedia is easy and free. If you are a wine
	merchant in Europe and you are selling and shipping to customers
	throughout Europe, you too can have a listing on vinopedia. <br />
	<br />
	vinopedia uses a custom built web robot that downloads price
	information from your site every day. <br />
	<br />
	<a href='/retailers.jsp'>Get listed </a>
	</div>
	</div>
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start(weight=ignore) -->	
			
			
	<!--hints-->
	<!-- google_ad_section_end -->
	
	<%
			out.print("<script type=\"text/javascript\">addthis_url = location.href; addthis_title  = document.title;  addthis_pub    = 'vinopedia';</script><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\" ></script><br/>");
					if (request.getParameter("dosearch") != null) {
						out.print("<br /><br /><font color='red'>"
								+ t.get("3characters") + "</font>");
					}
				}

				if (searchdata.getName().length() > 2) {
					String tip = "";
					if ("true".equals(request.getParameter("tip")))
						tip = "Tip ";
					String suggestion = "";
					if ("true".equals(request.getParameter("suggestion")))
						suggestion = "Suggestion ";
					int records = 0;
					if (wineset != null)
						records = wineset.records;
					logger = new Webactionlogger(tip + suggestion + "Search",
							request.getServletPath(), ipaddress, request
									.getHeader("referer"),
							searchdata.getName(), searchdata.getVintage(),
							searchdata.getCreated(), searchdata.getPricemin(),
							searchdata.getPricemax(), searchdata.getCountry(),
							searchdata.getRareold(), "", "", "", "", 0.0,
							records);
					if (request.getParameter("dosearch") != null)
						logger.searchhistory = searchhistory;
					if (wineset != null && wineset.othervintage) {
						out.print(t.get("noresultsfound"));
						out.print(" " + t.get("forvintage") + " "
								+ searchdata.getVintage() + ". "
								+ t.get("othervintages"));
					}

					if (wineset == null || wineset.Wine == null
							|| wineset.Wine.length == 0
							&& !wineset.othervintage) {
						out.print(t.get("noresultsfound"));
						out.print(".");
						
					}
					if (wineset != null && wineset.Wine != null
							&& wineset.Wine.length != 0) {
		%><h4><%=t.get("searchresultsfor")%> <%
 	out.print(Spider.escape(searchdata.getName()
 						.replaceAll("\\d\\d\\d\\d\\d\\d ", ""))
 						+ " " + searchdata.getVintage());
 %> (<%
 	out.print(wineset.records + " " + t.get("winesfound")
 						+ "). <br />");
 				out.print("</h4>");
		if (wineset.bestknownwineid > 0) { //!wineset.searchtype.equals("text")){
			int singlevintage = 0;
			try {
				singlevintage = Integer.parseInt(searchdata.getVintage().trim());
			} catch (Exception e) {
			}
			String ratingshtml = Webroutines.getNewRatingsHTML(wineset.bestknownwineid, 922, "/indexnewstyle.jsp",singlevintage);
			out.print(ratingshtml);
			if (!"".equals(ratingshtml)&& wineset.knownwineid == 0) {
				out.print("Note: the ratings given above are for "+ Knownwines.getKnownWineName(wineset.bestknownwineid)+ " only. The results list may contain other wines as well.");
			}
			out.print("<br/>");
		}
	}
				}

%>	
<jsp:include page="/footer.jsp" />
<%
	} //NZ filter
%>

</div>

</body> 
</html>
<%
	long endload = System.currentTimeMillis();

	logger.loadtime = ((endload - startload));
	logger.logaction();
%>
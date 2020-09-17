
<%
	PerformanceLogger pl = new PerformanceLogger();
	pl.log("Start index.jsp");
	long start = System.currentTimeMillis();
	boolean debuglog = false;

	PageHandler p = PageHandler.getInstance(request, response, "Search");

	if ("sneak preview".equalsIgnoreCase(request.getParameter("name"))) {
		session.setAttribute("testmode", true);
		Dbutil.logger.info("testmode");
	}

	debuglog = false;

	pl.log("Start processSearchdata");
	p.searchdata.sponsoredresults = true;
	p.searchpage = "/index.jsp";
	p.processSearchdata(request);
	pl.log("End processSearchData");
	if (p.s != null && p.s.singlevintage > 0)
		p.getLogger().vintage = p.s.singlevintage + "";
	String originaltesturl = (String) request.getAttribute("originalURL");
	if (request.getAttribute("originalURL") != null
			&& ((String) request.getAttribute("originalURL")).contains("/wine2/") && p.s != null
			&& p.s.wineset != null && p.s.wineset.canonicallink != null
			&& p.s.wineset.canonicallink.length() > 0) {
		String newurl = p.s.wineset.canonicallink;
		response.setStatus(301);
		response.setHeader("Location", newurl);
		response.setHeader("Connection", "close");
		return;
	}
%><!DOCTYPE HTML><%@ page
	import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.online.Searchdata"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.online.SearchHandler"
	import="com.freewinesearcher.online.Ad"
	import="com.freewinesearcher.common.Knownwines"
	import="com.freewinesearcher.common.PerformanceLogger"
	import="com.freewinesearcher.common.Configuration"
	import="com.freewinesearcher.online.Hemabox"
	import="com.freewinesearcher.online.RecommendationAd"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><jsp:useBean
	id="searchdata" class="com.freewinesearcher.online.Searchdata"
	scope="session" /><jsp:useBean id="searchhistory"
	class="com.freewinesearcher.online.Searchhistory" scope="session" /><html>
<head><%@ include file="/header2.jsp"%><title>
	<%
		if (!p.searchdata.getName().equals("")) {
			out.print((request.isUserInRole("admin") && p.s.wineset.knownwineid > 0
					? p.s.wineset.knownwineid + " "
					: "")
					+ (p.s.wineset.knownwineid > 0
							? Knownwines.getKnownWineName(p.s.wineset.knownwineid)
							: p.searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ", ""))
									.replaceAll("^Ch.teau ", "").replaceAll("&", "&amp;")
					+ (p.s.singlevintage > 0 ? " " + p.s.singlevintage : "") + " Prices");
		} else {
			//out.print("Vinopedia.com Wine Search and Price Comparison - Buy wine online");
			out.print("Wine Search and Price Comparison - Vinopedia.com");
		}
	%>
</title>
<%
	session.setAttribute("winename",
			(p.s.wineset.knownwineid > 0
					? Knownwines.getKnownWineName(p.s.wineset.knownwineid)
					: searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
	String headerwinename = "";
	if (PageHandler.getInstance(request, response).s != null
			&& PageHandler.getInstance(request, response).s.wineset != null) {
		headerwinename = Webroutines
				.escape(PageHandler.getInstance(request, response).s.wineset.knownwineid > 0
						? Knownwines.getKnownWineName(
								PageHandler.getInstance(request, response).s.wineset.knownwineid)
						: PageHandler.getInstance(request, response).searchdata.getName())
				.replaceAll("^\\d\\d\\d\\d\\d\\d ", "")
				+ (p.s.singlevintage > 0 ? " " + p.s.singlevintage : "");
	}
	if (!PageHandler.getInstance(request, response).searchdata.getName().equals("")) {
		out.print("<meta name=\"keywords\" content=\"");
		for (int i = 0; i < headerwinename.split("\\s+").length; i++) {
			out.print(headerwinename.split("\\s+")[i] + ", ");
		}
		out.print("buy " + headerwinename + ", Parker ratings, wine price, buy wine\" />");
	} else {
		out.print(
				"<meta name=\"keywords\" content=\"buy wine, wine online, wine guide, price, wine searcher\" />");
	}
	if ("NL".equals(PageHandler.getInstance(request, response).searchdata.getLanguage())) {
		if (!headerwinename.equals("") && PageHandler.getInstance(request, response).s != null) {
			out.write("<meta name=\"description\" content=\""
					+ PageHandler.getInstance(request, response).t.get("pricecomparisonfor") + " "
					+ headerwinename + " (" + PageHandler.getInstance(request, response).s.wineset.records
					+ " aanbiedingen gevonden)\" />");
		} else {
			out.write(
					"<meta name=\"description\" content=\"De meest complete gratis prijsvergelijkingssite van wijnen in Europa.\" />");
		}

	} else {
		if (!headerwinename.equals("") && PageHandler.getInstance(request, response).s != null) {
			out.write("<meta name=\"description\" content=\""
					+ (PageHandler.getInstance(request, response).s.wineset.records > 4
							? "Compare " + PageHandler.getInstance(request, response).s.wineset.records
									+ " online offers for " + headerwinename + ", "
							: "Price comparison for " + headerwinename + ", ")
					+ "Parker and Wine Spectator ratings, the best vintages and background information.\" />");
		} else {
			out.print(
					"<meta name=\"description\" content=\"See where you can buy your favorite wine online, ratings, the best vintages and background info.\" />");
		}

	}
%>
<%
	pl.log("Start header");
%>
<%
	if (p.s.wineset.canonicallink != null && !p.s.wineset.canonicallink.equals("")) {
%><link rel="canonical" href="<%=p.s.wineset.canonicallink%>" />
<%
	}
	//if (false){ //Apparently, mobile detection does not work for Google bot
	if (!p.bot && p.firstrequest && p.mobile) {
		String newloc = "/m";
		try {
			if (PageHandler.getInstance(request, response).s.wineset.knownwineid > 0)
				newloc = "/mwine/" + Webroutines
						.URLEncode((PageHandler.getInstance(request, response).searchdata.getName() + " "
								+ PageHandler.getInstance(request, response).searchdata.getVintage()).trim());
		} catch (Exception e) {
		}

		if (newloc != null && !newloc.equals("")) {
			//Dbutil.logger.info("Redirecting to mobile page: "+request.getHeader("user-agent"));
			response.setStatus(302);
			response.setHeader("Location", newloc);
			response.setHeader("Connection", "close");
			return;
		}
	}
	//}
%><meta name="verify-v1"
	content="DPurn9ZNRpI1pXuOlIigNqJ6JoMePo97QY0m2L3eBrA=" />
<meta name="google-site-verification"
	content="_fzJ24glka7UkSftFW4UhkGseJvlVn4Wa-zA7r6N5VM" />
</head>
<body onload="javascript:doonload();">
	<%
		if (!PageHandler.getInstance(request, response).block
				&& !PageHandler.getInstance(request, response).abuse) {
			if (!p.s.search || p.s.wineset == null) {
				// No search
	%><div class='topbar spriter spriter-refine'>
		<div class='topbarcontent'>
			<img src='<%=Configuration.staticprefix%>/css/sprite4.png' alt=''
				style='display: none; width: 1px; height: 1px' /><img
				src='<%=Configuration.cdnprefix%>/css/spriter.png' alt=''
				style='display: none; width: 1px; height: 1px' />
			<div id='mobile'>
				<a
					href='<%=(PageHandler.getInstance(request, response).s != null
							&& PageHandler.getInstance(request, response).s.wineset.knownwineid > 0)
									? "/mwine/"
									: "/m" + (PageHandler.getInstance(request, response).searchdata.getName()
											.length() > 2 ? "?name=" : "")%><%=(PageHandler.getInstance(request, response).searchdata.getName().length() > 2
									? Webroutines.URLEncode(
											Webroutines.removeAccents(
													PageHandler.getInstance(request, response).searchdata.getName()))
											+ (PageHandler.getInstance(request, response).searchdata.getVintage()
													.length() > 3
															? "+" + PageHandler.getInstance(request,
																	response).searchdata.getVintage()
															: "").trim()
									: "")%>'>Mobile
					access</a> <a href='/nf/wine-guide/' rel='nofollow'>Wine Guide</a> <a
					href='/settings/index.jsp' rel='nofollow'>PriceAlerts</a> <a
					href='/retailers.jsp'>Getting listed</a> <a href='/about.jsp'
					rel='nofollow'>About us</a> <a href='/links.jsp'>Links</a> <a
					href='/publishers.jsp' rel='nofollow'>For web site owners</a>
				<!-- <a href='/Deals.jsp' rel='nofollow'>Deals</a> -->
				<%
					if (request.getRemoteUser() != null) {
				%><a href='/settings/index.jsp?logoff=true'>Log off</a>
				<%
					}
				%>
			</div>
			&nbsp;
		</div>
	</div>
	<%@ include file="/snippets/bigsearchbar.jsp"%>
	<div class='textpage'>
		<%
			//<div style='border:3px dashed #4d0027;padding:10px;padding-top:0px;'><h2><font style='color:black;font-size:22px;' face='times, times new roman'>Breaking news! FINANCIAL TIMES: </font></h2><img src='/images/Jancis_Robinson.jpg' alt='Jancis Robinson' style='float:left;padding-right:10px;'/><h2>&#8220;Perhaps the most attractive, user-friendly competitor is Vinopedia.com&#8221;</h2>Jancis Robinson, in an article on Wine Search Engines in the <a href='http://www.ft.com/cms/s/2/121122dc-c1ea-11df-9d90-00144feab49a.html' target='_blank'>Financial Times</a>. Thank you, Jancis, this is the stuff that keeps us going!<br/>"Perhaps the most attractive, user-friendly competitor is Vinopedia.com, set up by Dutch wine lovers Jeroen Starrenburg and Jasper Hammink, who quit their jobs in IT to build a site that locates prices and stockists of 1.6m different wines (the current tally) but also incorporates individual wine ratings (from American wine magazine Wine Spectator). One particularly useful feature is that they identify wines whose prices have dropped most significantly recently and showcase them on their home page." Read the full artice in the <a href='http://www.ft.com/cms/s/2/121122dc-c1ea-11df-9d90-00144feab49a.html' target='_blank'>Financial Times</a> or the <a href='http://www.jancisrobinson.com/articles/a201009173.html' target='_blank'>Purple pages</a>.</div>
		%>
		<h1>
			Compare online wine prices with vino<font color='black'>pedia</font>
		</h1>
		<br />Vinopedia.com is a wine search engine that helps you to buy
		wine online. We compare the price lists of wine stores world wide,
		show you who is selling your favorite wine and who is the cheapest in
		the market. Just type (part) of a wine name in the search box to see
		how it works.<br />We also give you background information of each
		wine, such as ratings, and you can use our <a href='/wine-guide/'>wine
			buying guide</a> to discover the best wines from a region or a grape
		variety, based on price and quality.<br /> <br />
		<%=Webroutines.getConfigKey("systemmessage")%>
		<div
			style='border: 3px dashed #4d0027; padding: 10px; padding-top: 0px; height: 320px; width: 45%; float: left'>
			<a
				href='https://itunes.apple.com/us/app/vinopedia.com/id668028831?mt=8'
				title='Vinopedia iPhone app'><img
				style='margin-right: 20px; width: 128px; height: 184px; float: right; margin: 10px; margin-top: 20px'
				src='<%=Configuration.staticprefix%>/images/iphoneapp.jpg'
				alt='Vinopedia iPhone app' /></a><a
				href='https://itunes.apple.com/us/app/vinopedia.com/id668028831?mt=8'
				title='Vinopedia iPhone app'><img
				style='margin: 10px; width: 135px; height: 40px; float: right; clear: right;'
				src='<%=Configuration.staticprefix%>/images/Download_on_the_App_Store_Badge_US-UK_135x40.png'
				alt='Vinopedia iPhone app' /></a>
			<h2>
				New: <a
					href='https://itunes.apple.com/us/app/vinopedia.com/id668028831?mt=8'
					title='Vinopedia iPhone app'>Vinopedia iPhone app</a>
			</h2>
			We just released the Vinopedia app for iPhone. With this app you can
			easily search through millions of wine offers from 4200 online wine
			stores indexed by vinopedia.com from your iPhone. At home after you
			finished the bottle you wish you had more. In a restaurant when you
			wonder if the price is reasonable. Or at the winery to find out if
			stores nearby your home offer this wine. Vinopedia will direct you to
			the store page of the wine offer you selected, or you can have it
			sent to your email account in case you prefer to order it from your
			home computer.
		</div>
		<div
			style='border: 3px dashed #4d0027; padding: 10px; padding-top: 0px; height: 320px; width: 45%; float: right'>
			<h2>
				<a href='/plugins/' title='Browser extension'>Vinopedia Browser
					Plugin</a>
			</h2>
			Get the plugin for your browser that lets you quickly check the price
			of any wine from anywhere on the web. Just highlight (select) a wine
			on any page, select "Find on Vinopedia" from the menu and a new page
			opens with the pricing information of the wine you selected. Saving
			money has never been easier! You can now install the tool from the <a
				href='/plugins/' title='Browser extension'>browser plugin</a> page.
			Go ahead and try it out!<br /> <a href='/plugins/'
				title='Browser extension'><img
				style='margin-right: 20px; width: 300px; height: 150px'
				src='<%=Configuration.staticprefix%>/images/extensions.gif'
				alt='Browser Plugin' /></a>
		</div>
		<div style='clear: both'>
			<div>
				<h2>
					<a href='/wine-stores/'>Wine Stores</a>
				</h2>
				Shows a map of wine stores close to you and interesting new wines
				they got in the last week.
				<%@ include file="/snippets/tips.jsp"%><%=Hemabox.getHtml(p)%><%@ include
					file="/snippets/footer.jsp"%></div>
			<!--  mainwide-->
			<%
				} else {
						pl.log("Start ads");
						String bannersshown = "";
						//RecommendationAd ra=new RecommendationAd(p.s.wineset.bestknownwineid,p.searchdata.getLastcountry());
						String rightadhtml = "";//ra.getAd(p,"Winead");
						if (rightadhtml.equals("")) {
							pl.log("Got no recommendadtion ad, try again");
							Ad newad = new Ad(p.bot, "winered", 160, 600, p.hostcountry, p.s.wineset.region,
									p.s.wineset.bestknownwineid, "");
							bannersshown = newad.bannersshown;
							rightadhtml = newad.html;
							pl.log("Got right ad");
						} else {
							pl.log("Got recommendation ad");
						}

						//Show search results
			%><%@ include file="/snippets/topbar.jsp"%><div
				class='container'><%@ include
					file="/snippets/logoandsearch.jsp"%><%=Webroutines.getConfigKey("systemmessage")%></div>
			<%
				pl.log("Start rating");
			%>
			<%
				Webroutines.RatingInfo ri = null;
						if (p.s.singlevintage > 0 && p.s.wineset.records > 0) {
							out.print(Webroutines.getWineInfo(p.s.wineset.bestknownwineid, p.s.singlevintage, p));
						} else {
							ri = Webroutines.getRatingsMicroformatHTML(p, p.s.wineset.bestknownwineid, 1000, p.searchpage,
									p.s.singlevintage, p.searchdata, p.t, request.isUserInRole("admin"));
							out.print(ri.html);
							pl.log("Getting banners");
							Ad adbetween = new Ad(p.bot, "winered", 1000, 60, p.hostcountry, p.s.wineset.region,
									p.s.wineset.bestknownwineid, "");
							pl.log("Got banners");
							out.print(adbetween.html);
							if (!p.bot)
								p.getLogger().setBannersshown(adbetween.bannersshown + bannersshown);
						}
			%><div class='main'>
				<div id='adright'>
					<%
						out.write(rightadhtml);
					%>
				</div>
				<div id='mainleft'>
					<noscript>
						<img src='/images/nojs.gif' alt='' />
					</noscript>
					<%
						pl.log("Start results");
					%>
					<%=Webroutines.getTabbedWineResultsHTML(p.s.wineset, p.getTranslator(), p.searchdata, 25,
							response, "true", true, p.searchpage, p.s.singlevintage, ri, false,
							request.isUserInRole("admin"))%>
					<div class='pricenote'><%=p.getTranslator().get("pricenote")%></div>
					<%
						pl.log("Start footer");
					%>
					<%
						//if (p.s.wineset.region!=null&&p.s.wineset.region.length()>1) request.setAttribute("regionlink","<a href='/region/"+Webroutines.removeAccents(p.s.wineset.region).replaceAll(", ","/").replaceAll("'","&apos;").replaceAll(" ","+")+"/'>"+(p.s.wineset.region.split(", ")[p.s.wineset.region.split(", ").length-1])+" information</a>");
					%>
					<%
						out.print(Webroutines.getSpecialPageLinks(p.searchdata.getName()));
								pl.log("Finished");

								if (System.currentTimeMillis() - p.getLogger().startload > 10000) {
									Dbutil.logger.info("Long load time for index.jsp. Loadtime: "
											+ (System.currentTimeMillis() - p.getLogger().startload) + ". Log:" + pl.getLog());

								}
					%><%@ include file="/snippets/footer.jsp"%>
				</div>
			</div>
			<%
				// workaround: IE positioning of footer
			%>
		</div>
		<%
			if ("sneak preview".equalsIgnoreCase(p.searchdata.getName()))
						out.write(
								"<br/><br/><h1>Test modus geactiveerd. </h1><h2>Om terug te gaan naar normale modus, sluit je browser helemaal af en keer terug naar Vinopedia.com</h2>Opmerkingen, fouten, etc. graag terugmelden aan <a style='text-decoration:underline;' href='mailto:feedback@vinopedia.com'>feedback@vinopedia.com</a>. <br/><br/>Bedankt, Jasper");
		%>
	</div>
	<!--  main-->
	<%
		}
		}
	%>
	<script type="text/javascript">
		$(document).ready(function() {
			setTimeout(function() {
				initSmartSuggest();
			}, 1500)
		});
	</script>
</body>
</html>
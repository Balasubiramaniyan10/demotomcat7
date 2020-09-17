package com.freewinesearcher.online;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wineset;

public class PageHandler implements Serializable {
	private static final long serialVersionUID = 1L;
	// long startload = System.currentTimeMillis();
	public transient Webactionlogger logger;
	public String referrer;
	// HttpSession session;
	public transient Cookie[] cookies;
	public Searchdata searchdata;
	// public HttpServletRequest request;
	// HttpServletResponse response;
	public String pageaction;
	public transient Translator t;
	public String thispage;
	public transient SearchHandler s = new SearchHandler();
	public String ipaddress;
	public String normalip = "";
	public String forwardedforip = "";
	public String hostcountry;
	public boolean block = false;
	public boolean mobile = false;
	public boolean firstrequest = true; // only in the first request we check mobile or not
	public boolean abuse = false;
	public Searchhistory searchhistory;
	public String searchpage = "/index.jsp";
	public int useridentifier = 0;
	public boolean bot = false;
	public boolean seemsbot = false;
	public boolean createWineset = true;
	public String GAtracking = "";
	public String asyncGAtracking = "";
	public String plusonelink = "https://www.vinopedia.com/";
	public boolean newsearch = true;
	public String useragent = "";
	public boolean iphone = false;
	public boolean appmessageshown = false;
	public int botstatus = 3;// 0 Normal browser //1 Friendly bot //2 Hostile bot //3 Not yet challenged //4
								// challenged //-1=iPhone app
	public String URLbeforebotcheck = "";
	public String challenge = "";
	public String firstreferrer = "firstreferrer";
	private static ArrayList<String> friendlyBots = new ArrayList<String>();

	public boolean isFriendlyBot(String hostname) {
		if (!"".equals(Dbutil.readValueFromDB("select * from bots where '" + hostname + "' regexp regex;", "regex"))) {
			botstatus = 1;
			return true;
		} else {
			return false;
		}

	}

	public boolean isFriendlyBotIp(String ip) {
		if (friendlyBots.contains(ip)) {
			botstatus = 1;
			return true;
		}
		String hostname = "";
		try {
			hostname = InetAddress.getByName(ip).getHostName();
		} catch (UnknownHostException e) {
			// who cares
		}
		if (!"".equals(hostname) && !""
				.equals(Dbutil.readValueFromDB("select * from bots where '" + hostname + "' regexp regex;", "regex"))) {
			botstatus = 1;
			friendlyBots.add(ip);
			return true;

		} else {
			return false;
		}

	}

	public boolean shouldProcessPage() {
		return getBotstatus() < 2;
	}

	public int getBotstatus() {
		// Dbutil.logger.info(botstatus);
		if (!Configuration.detectSuspectedBot)
			botstatus = 0;
		if (botstatus < 3)
			return botstatus;
		isFriendlyBotIp(ipaddress);
		// Dbutil.logger.info(botstatus);
		return botstatus;
	}

	private void setGA(String event) {
		if (thispage.contains("/wine/") || thispage.contains("/index.jsp")) {
			if (Webroutines.getRegexPatternValue("(\\d\\d\\d\\d)$", thispage).length() == 0) {
				// GAtracking="pageTracker._trackEvent('"+event+"', 'Wine');";
				GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Wine');";

				// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Wine']);";
				asyncGAtracking = "ga('send', 'event', '" + event + "', 'Wine');";
			} else {
				// GAtracking="pageTracker._trackEvent('"+event+"', 'Wine with vintage');";
				GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Wine with vintage');";

				// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Wine with
				// vintage']);";
				asyncGAtracking = "ga('send', 'event', '" + event + "', 'Wine with vintage');";
			}
		} else if (thispage.endsWith(".com/") || thispage.endsWith(".com")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Home page');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Home page');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Home page']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Home page');";
		} else if (thispage.contains("/mwine/")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Mobile wine');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Mobile wine');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Mobile wine']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Mobile wine');";
		} else if (thispage.contains("/details.jsp")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Mobile details');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Mobile details');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Mobile details']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Mobile details');";
		} else if (thispage.contains("/region/")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Region');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Region');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Region']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Region');";
		} else if (thispage.contains("/mregion/")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Mobile Region');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Mobile Region');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Mobile Region']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Mobile Region');";
		} else if (thispage.contains("/winery/")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Winery');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Winery');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Winery']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Winery');";
		} else if (thispage.contains("/mwinery/")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Mobile Winery');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Mobile Winery');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Mobile Winery']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Mobile Winery');";
		} else if (thispage.contains("/store/")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Store');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Store');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Store']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Store');";
		} else if (thispage.contains("storelocator")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Locator');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Locator');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Locator']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Locator');";
		} else if (thispage.contains("retailers.jsp")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Retailers');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Retailers');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Retailers']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Retailers');";
		} else if (thispage.contains("retailerdetails")) {
			// GAtracking="pageTracker._trackEvent('"+event+"', 'Retailer details');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', 'Retailer details');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"', 'Retailer
			// details']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', 'Retailer details');";
		} else {
			// GAtracking="pageTracker._trackEvent('"+event+"', '"+thispage.replaceAll("&",
			// "&amp;")+"');";
			GAtracking = "ga('pageTracker.send', 'event', '" + event + "', '" + thispage.replaceAll("&", "&amp;")
					+ "');";

			// asyncGAtracking="_gaq.push(['_trackEvent', '"+event+"',
			// '"+thispage.replaceAll("&", "&amp;")+"']);";
			asyncGAtracking = "ga('send', 'event', '" + event + "', '" + thispage.replaceAll("&", "&amp;") + "');";
		}

	}

	private void setPlusOneLink() {
		if (thispage.contains("/region/")) {
			plusonelink = thispage;
		} else if (thispage.contains("/winery/")) {
			plusonelink = thispage;
		} else if (thispage.contains("/store/")) {
			plusonelink = ""; // thispage.replaceAll("\\?.*", ""); removed: plus one bug "Read
								// plusone.google.com" makes page hanging
		} else if (thispage.contains("/wine-guide/")) {
			plusonelink = "https://www.vinopedia.com/wine-guide/";
		} else if (thispage.contains("storelocator")) {
			plusonelink = thispage;
		} else {
			plusonelink = "https://www.vinopedia.com/";
		}

	}

	public String getImageChallenge(HttpServletRequest request) {
		challenge = DigestUtils.md5Hex("fjwfj" + Math.random());
		request.getSession().setAttribute("challenge", challenge);
		if (botstatus == 3)
			botstatus = 4;
		return challenge;
	}

	// Used to create a new Pagehandler for a new session
	private PageHandler(HttpServletRequest request, HttpServletResponse response) {
		// this.request=request;
		// this.response=response;
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: A botstatus=" + botstatus + "; URLbeforebotcheck="
				+ URLbeforebotcheck + ", RequestURL=" + request.getRequestURL());
		if (botstatus == 3 && "".equals(URLbeforebotcheck)) {
			Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: B");
			if (!request.getRequestURL().toString().contains("check.jsp")) {
				Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: C");
				URLbeforebotcheck = request.getRequestURL()
						+ (request.getQueryString() != null ? "?" + request.getQueryString() : "");
			}
			URLbeforebotcheck = URLbeforebotcheck.replace("vinopedia.com//", "vinopedia.com/");
			Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: D URLbeforebotcheck=" + URLbeforebotcheck);
			// if (!request.getRequestURL().toString().contains("check.jsp"))
			// URLbeforebotcheck=request.getAttribute("originalURI")+(request.getAttribute("originalQueryString")!=null&&!request.getAttribute("originalQueryString").equals("")?"?"+request.getAttribute("originalQueryString"):"");
		}
		Object originalURLObj = request.getAttribute("originalURL");
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: E originalURLObj=" + originalURLObj);

		Object originalQueryStringObj = request.getAttribute("originalQueryString");
		Dbutil.logger
				.info("********** PAGE HANDLER 234 PageHandler: F originalQueryStringObj=" + originalQueryStringObj);

		this.thispage = (originalURLObj != null ? (String) originalURLObj
				+ (originalQueryStringObj != null && !((String) originalQueryStringObj).equals("")
						? "?" + (String) originalQueryStringObj
						: "")
				: request.getServletPath());
		// request.getSession(true).setAttribute("originalurl", thispage);
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: G this.thispage=" + this.thispage);

		this.pageaction = "Pageload";
		setGA("Landingpage");
		Searchdata searchdata = (Searchdata) request.getSession(true).getAttribute("searchdata");
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: H searchdata from session=" + searchdata);
		if (searchdata != null) {
			this.searchdata = searchdata;
		} else {
			this.searchdata = new Searchdata();
		}
		this.searchhistory = new Searchhistory();
		getIP(request);
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: I hostcountry=" + hostcountry);
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: J ipaddress=" + ipaddress);
		if (hostcountry == null && botstatus < 3) {
			hostcountry = Webroutines.getCountryCodeFromIp(ipaddress);
		}
		// String hostname="";
		// try{hostname=InetAddress.getByName(ipaddress).getHostName();} catch
		// (Exception e){}
		useragent = (String) request.getHeader("user-agent");
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: K useragent=" + useragent);
		if (useragent == null) {
			useragent = "";
		}
		if (useragent.toLowerCase().contains("bot")) {
			seemsbot = true;
		}
		bot = seemsbot;
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: L seemsbot=" + seemsbot);
		checkBlock(request);
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: M abuse=" + abuse);

		cookies = request.getCookies();
		setReferrer(request);
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: N referrer=" + referrer);
		firstreferrer = referrer;
		handleCookies(request, response);
		determinemobile();
		setLanguage();
		setPlusOneLink();

		Dbutil.logger
				.info("********** PAGE HANDLER 234 PageHandler: O request.getServerName()=" + request.getServerName());
		if (request.getServerName().contains("searcher")) {
			searchpage = "https://www.vinopedia.com/index.jsp";
		}
		Dbutil.logger.info("********** PAGE HANDLER 234 PageHandler: P searchpage=" + searchpage);
		try {
			request.setCharacterEncoding("ISO-8859-1");
			// session = request.getSession(true);
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}

		logger = new Webactionlogger(pageaction, thispage, ipaddress, referrer, "", "", 0, (float) 0.0, (float) 0.0, "",
				false, "", "", "", "", (double) 0.0, 0, searchhistory);
		getLogger().useragent = request.getHeader("User-Agent");
		if (getLogger().useragent == null) {
			getLogger().useragent = "";
		}
		request.getSession(true).setAttribute("pagehandler", this);
	}

	private void determinemobile() {
		String ua = useragent.toLowerCase();
		if (!mobile && ua.contains("iphone")) {
			iphone = true;
			mobile = true;
		}
		if (!mobile && ua.contains("ppc;")) {
			mobile = true;
		}
		if (!mobile && ua.contains("windows ce")) {
			mobile = true;
		}
		if (!mobile && ua.contains("mobile")) {
			mobile = true;
		}
		if (!mobile && ua.contains("midp")) {
			mobile = true;
		}
		if (!mobile && ua.contains("smartphone")) {
			mobile = true;
		}
		if (!mobile && ua.contains("android")) {
			mobile = true;
		}
		if (mobile && ua.contains("ipad;")) {
			mobile = false;
		}
		if (thispage != null
				&& (thispage.contains("/mwine/") || thispage.contains("/mregion/") || thispage.contains("/mwinery/"))) {
			mobile = true;
		}
		// Dbutil.logger.info(mobile+" "+ua);
	}

	// Used to update the Pagehandler for this session with a new request
	private void refresh(HttpServletRequest request, HttpServletResponse response) {
		logger = null;
		getLogger().useragent = request.getHeader("User-Agent");
		if (getLogger().useragent == null)
			getLogger().useragent = "";

		// if (request == null) {
		// Dbutil.logger.info("request is null 1");
		// }
		this.GAtracking = "";
		// this.request=request;
		// this.response=response;
		this.thispage = (request.getAttribute("originalURL") != null
				? (String) request.getAttribute("originalURL") + (request.getAttribute("originalQueryString") != null
						&& !((String) request.getAttribute("originalQueryString")).equals("")
								? "?" + (String) request.getAttribute("originalQueryString")
								: "")
				: request.getServletPath());
		this.pageaction = "Pageload";
		getLogger().type = "";
		getLogger().page = thispage;
		String oldIP = ipaddress;
		getIP(request);
		if (!oldIP.equals(ipaddress) || botstatus == 3) {
			botstatus = 3;
			request.setAttribute("existingrequest", null);
		} else {
			request.setAttribute("existingrequest", "true");
		}
		if (hostcountry == null && botstatus < 3)
			hostcountry = Webroutines.getCountryCodeFromIp(ipaddress);
		checkBlock(request);
		cookies = request.getCookies();
		setReferrer(request);
		if (firstreferrer != null && !firstreferrer.equals("firstreferrer"))
			referrer = firstreferrer;
		firstreferrer = "firstreferrer";
		setGA("Pageload");
		handleCookies(request, response);
		setLanguage();
		setPlusOneLink();
		request.getSession(true).setAttribute("pagehandler", this);
		createWineset = true;
	}

	/*
	 * public PageHandler (HttpServletRequest request, HttpServletResponse response,
	 * String pageaction, Searchdata searchdata, Searchhistory searchhistory, String
	 * thispage){ this.request=request; this.response=response;
	 * this.pageaction=pageaction; this.thispage=thispage; if (searchdata!=null) {
	 * this.searchdata=searchdata; } else { this.searchdata=new Searchdata(); }
	 * this.searchhistory=searchhistory; getIP(); checkBlock(); setLanguage();
	 * cookies = request.getCookies(); setReferrer(); handleCookies();
	 * 
	 * try { request.setCharacterEncoding("ISO-8859-1"); //session =
	 * request.getSession(true); } catch (Exception e) {
	 * Dbutil.logger.error("Problem: ", e); }
	 * 
	 * request.setAttribute("pagehandler", this); logger = new
	 * Webactionlogger(pageaction, request .getServletPath(), ipaddress, referrer,
	 * "", "", 0, (float) 0.0, (float) 0.0, "", false, "", "", "", "", (double) 0.0,
	 * 0); }
	 */

	public static PageHandler getInstance(HttpServletRequest request, HttpServletResponse response) {
		try {
			Dbutil.logger.info("********** PAGE HANDLER 397 getInstance: A ");
			Object pageHandlerObjFromSession = request.getSession(true).getAttribute("pagehandler");
			Dbutil.logger.info(
					"********** PAGE HANDLER 397 getInstance: B pageHandlerFromSession : " + pageHandlerObjFromSession);
			if (pageHandlerObjFromSession != null) {
				// Existing session
				// last check: to prevent null pointers if within the same request a call
				// to the page handler is being made after is has been cleaned up
				Object existingRequest = request.getAttribute("existingrequest");
				Dbutil.logger.info("********** PAGE HANDLER 397 getInstance: C existingRequest : " + existingRequest);
				PageHandler pageHandler = (PageHandler) pageHandlerObjFromSession;
				Dbutil.logger.info("********** PAGE HANDLER 397 getInstance: D pageHandler.t : " + pageHandler.t);
				if (existingRequest != null && pageHandler.t != null) {
					Dbutil.logger.info("********** PAGE HANDLER 397 getInstance: E returning pageHandler");
					return pageHandler;
				} else {
					Dbutil.logger.info(
							"********** PAGE HANDLER 397 getInstance: F calling pageHandler.refresh(request, response) and return pageHandler");
					pageHandler.refresh(request, response); // Existing session, but new request
					return pageHandler;
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error(
					"********** PAGE HANDLER 397 getInstance: G Problem while serving instance of PageHandler", e);
			Dbutil.logger.info(
					"********** PAGE HANDLER 397 getInstance: H ERROR request: " + request + ", response: " + response,
					e);
		}
		Dbutil.logger.info(
				"********** PAGE HANDLER 397 getInstance: I setting existingrequest = true, calling PageHandler(request, response)");
		request.setAttribute("existingrequest", "true");
		return new PageHandler(request, response);

	}

	public static PageHandler getInstance(HttpServletRequest request, HttpServletResponse response, String pageaction) {
		PageHandler p = getInstance(request, response);
		p.pageaction = pageaction;
		p.getLogger().type = pageaction;
		return p;
	}

	public void getIP(HttpServletRequest request) {
		if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
			ipaddress = request.getRemoteAddr();
		} else {
			ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		normalip = request.getRemoteAddr();
		forwardedforip = request.getHeader("HTTP_X_FORWARDED_FOR");
		hostcountry = Webroutines.getCountryCodeFromIp(ipaddress);
	}

	public void checkBlock(HttpServletRequest request) {
		if (Webroutines.ipBlocked(ipaddress) && !request.getServletPath().contains("savecontact.jsp")
				&& !request.getServletPath().contains("abuse.jsp")) {
			abuse = true;
		}
		// if (false && hostcountry.equals("NZ")) {
		// logger = new Webactionlogger("NZ: Access denied", request.getServletPath(),
		// ipaddress,
		// request.getHeader("referer"), "", "", 0, (float) 0.0, (float) 0.0, "", true,
		// "", "", "", "", 0.0,
		// 0);
		// logger.run();
		// block = true;
		// }
	}

	public void setLanguage() {
		Translator.languages language = null;
		if (searchdata != null)
			language = Translator.getLanguage(searchdata.getLanguage());
		if (language == null) {
			language = Translator.getDefaultLanguageForCountry("UK");
		}
		t = new Translator();
		t.setLanguage(language);
	}

	public void setReferrer(HttpServletRequest request) {
		if (request.getHeader("Referer") != null) { // skipped
													// &&!request.getHeader("Referer").toLowerCase().contains("vinopedia.com")
			referrer = request.getHeader("Referer");
		}
		// if (referrer!=null)
		// Webroutines.saveReferrer(request.getRemoteAddr(),referrer);

	}

	public void handleCookies(HttpServletRequest request, HttpServletResponse response) {

		// Order to determine currency, language and country of seller:
		// First, look at a parameter in the request
		// If null, look at the cookies
		// if null, look at the searchdata (cookies may be off)
		// Finally, use the country of the ip address to determine the value
		try {
			String currency = null;
			if (currency == null)
				if (request.getParameter("currency") != null && !"".equals(request.getParameter("currency"))) {
					currency = request.getParameter("currency");
				}
			if (currency == null && request.getCookies() != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("currency")
							&& (cookie.getPath() == null || cookie.getPath().equals("/")))
						currency = cookie.getValue();
				}
			}
			if (currency == null && searchdata.currency != null && !searchdata.currency.equals("")) {
				currency = searchdata.currency;
			}
			if (currency == null) {
				if (hostcountry != null) {
					currency = getCurrencyFromCountrycode(hostcountry);
				}
			}
			if (currency == null)
				currency = "EUR";
			currency = currency.toUpperCase().replaceAll("[^A-Z]", "");
			searchdata.setCurrency(currency);
			Cookie currencyCookie = new Cookie("currency", currency);
			currencyCookie.setPath("/");
			currencyCookie.setMaxAge(60 * 60 * 24 * 365);
			response.addCookie(currencyCookie);

			/*
			 * 
			 * // Retrieve currency from cookie if not filled already Cookie currencyCookie
			 * = new Cookie("currency", searchdata .getCurrency());
			 * currencyCookie.setMaxAge(60 * 60 * 24 * 365);
			 * response.addCookie(currencyCookie);
			 */
			// Retrieve language from cookie if not filled already
			if (request.getParameter("language") != null) {
				Cookie languageCookie = new Cookie("language", request.getParameter("language"));
				languageCookie.setMaxAge(60 * 60 * 24 * 365);
				languageCookie.setPath("/");
				response.addCookie(languageCookie);
				searchdata.setLanguage(request.getParameter("language"));
			}
			if (searchdata.getLanguage() == null || searchdata.getLanguage().toString().equals("")) {
				searchdata.setLanguage(Webroutines.getCookieValue(cookies, "language", ""));
			}
			if (searchdata.getLanguage() != null && !searchdata.getLanguage().toString().equals("")) {
				Cookie languageCookie = new Cookie("language", searchdata.getLanguage().toString());
				languageCookie.setMaxAge(60 * 60 * 24 * 365);
				languageCookie.setPath("/");
				response.addCookie(languageCookie);
			}

			String country = null;
			if (country == null)
				if (request.getParameter("country") != null && !request.getParameter("country").equals("")) {
					country = request.getParameter("country").toUpperCase();
				}
			if (country == null)
				if (request.getParameter("countryofseller") != null
						&& !request.getParameter("countryofseller").equals("")) {
					country = request.getParameter("countryofseller").toUpperCase();

				}
			if (!seemsbot && country == null && cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("country")
							&& (cookie.getPath() == null || cookie.getPath().equals("/")))
						country = cookie.getValue().toUpperCase();
				}
			}
			if (country == null && searchdata.country != null && !searchdata.country.equals("")) {
				country = searchdata.country;
			}
			if (!seemsbot && country == null) {
				if (hostcountry != null) {
					if (hostcountry.equals("US") || hostcountry.equals("CA")) {
						country = "UC";
					} else if ("AT,BE,CZ,CY,DK,EE,FI,FR,DE,GR,H,IE,IT,LV,LT,LU,MT,NL,PL,PT,SK,SK,ES,SE,GB,CH,BG,NO"
							.contains(hostcountry)) {
						country = "EU";
					}
				}
			}

			if (country == null || country.equals(""))
				country = "All";
			country = country.toUpperCase().replaceAll("[^A-Z]", "");
			if (country.equals("ALL"))
				country = "All";
			searchdata.setCountry(country);
			if (!country.equals("EU") && !country.equals("UC") && !country.equals("All")) {
				searchdata.lastcountry = country;
				request.getSession().setAttribute("lastcountry", country);
			} else if (request.getSession().getAttribute("lastcountry") != null) {
				searchdata.lastcountry = (String) request.getSession().getAttribute("lastcountry");
			}
			if ((hostcountry != null) && (searchdata.lastcountry == null || searchdata.lastcountry.equals("")))
				searchdata.lastcountry = hostcountry;
			searchdata.lastcountry = searchdata.lastcountry.replaceAll("[^A-Z]", "");
			Cookie countryCookie = new Cookie("country", country);
			countryCookie.setMaxAge(60 * 60 * 24 * 365);
			countryCookie.setPath("/");
			response.addCookie(countryCookie);
		} catch (Exception e) {
			// Dbutil.logger.error("Pagehandler issue: ",e);
		}

	}

	public static String getCurrencyFromCountrycode(String countrycode) {
		String currency = "EUR";
		if (countrycode != null && countrycode.length() > 0) {
			if (countrycode.equals("US")) {
				currency = "USD";
			} else if (countrycode.equals("CA")) {
				currency = "CAD";
			} else if (countrycode.equals("AU")) {
				currency = "AUD";
			} else if (countrycode.equals("NZ")) {
				currency = "NZD";
			} else if (countrycode.equals("HK")) {
				currency = "HKD";
			} else if (countrycode.equals("NO")) {
				currency = "NOK";
			} else if (countrycode.equals("DK")) {
				currency = "DKK";
			} else if (countrycode.equals("UK")) {
				currency = "GBP";
			} else if (countrycode.equals("CH")) {
				currency = "CHF";
			}
		}
		return currency;
	}

	public String getCurrencyFromCountrycode(HttpServletRequest request) {
		String currency = "EUR";
		if (hostcountry != null && hostcountry.length() > 0) {
			if (hostcountry.equals("US") || request.getLocale().getCountry().equals("US")) {
				currency = "USD";
			} else if (hostcountry.equals("CA") || request.getLocale().getCountry().equals("CA")) {
				currency = "CAD";
			} else if (hostcountry.equals("AU") || request.getLocale().getCountry().equals("AU")) {
				currency = "AUD";
			} else if (hostcountry.equals("NZ") || request.getLocale().getCountry().equals("NZ")) {
				currency = "NZD";
			} else if (hostcountry.equals("HK") || request.getLocale().getCountry().equals("HK")) {
				currency = "HKD";
			} else if (hostcountry.equals("NO") || request.getLocale().getCountry().equals("NO")) {
				currency = "NOK";
			} else if (hostcountry.equals("DK") || request.getLocale().getCountry().equals("DK")) {
				currency = "DKK";
			} else if (hostcountry.equals("UK") || request.getLocale().getCountry().equals("UK")) {
				currency = "GBP";
			} else if (hostcountry.equals("CH") || request.getLocale().getCountry().equals("CH")) {
				currency = "CHF";
			}
		}
		return currency;
	}

	public void processSearchdata(HttpServletRequest request) {
		if (shouldProcessPage()) {
			boolean fuzzy = false;
			newsearch = true;
			try {
				if (request != null && "true".equals(request.getParameter("fuzzy"))) {
					fuzzy = true;
				}
			} catch (Exception e) {

			}
			String tip = "";
			if ("true".equals(request.getParameter("tip")))
				tip = "Tip ";
			String suggestion = "";
			if ("true".equals(request.getParameter("suggestion")))
				suggestion = "Suggestion ";

			String offsetstr = Webroutines.filterUserInput(request.getParameter("offset"));
			if (offsetstr == null || offsetstr.equals("")) { // First empty the fields in case one of the field was made
																// empty: then it would not refresh
				offsetstr = "0";
			}
			int offset = 0;
			try {
				offset = Integer.parseInt(offsetstr);
			} catch (Exception e) {
			}

			try {
				newsearch = Boolean.parseBoolean(Webroutines.filterUserInput(request.getParameter("dosearch")));
			} catch (Exception e) {
			}
			try {
				newsearch = !Boolean.parseBoolean(Webroutines.filterUserInput(request.getParameter("keepdata")));
			} catch (Exception e) {
			}

			String map = Webroutines.filterUserInput(request.getParameter("map"));
			if (map == null)
				map = "";
			if (newsearch) {
				searchdata.setOrder("");
				// searchdata.setCountry("");
				// searchdata.setCurrency("");
				searchdata.setName("");
				searchdata.setOffset(0);
				searchdata.setVintage("");
				searchdata.setVat("");
				searchdata.setPricemaxstring("");
				searchdata.setPriceminstring("");
				searchdata.setFreetext(false);
				searchdata.setSize("0.0");
			}
			if (request.getParameter("dosearch") != null) {
				logger.searchhistory = searchhistory;
			}
			if (request.getParameter("order") != null)
				searchdata.setOrder(request.getParameter("order"));
			// if (request.getParameter("country")!=null)
			// searchdata.setCountry(request.getParameter("country"));
			if (request.getParameter("currency") != null)
				searchdata.setCurrency(request.getParameter("currency"));
			if (request.getParameter("name") != null)
				searchdata.setName(request.getParameter("name"));
			searchdata.setOffset(offset);
			if (request.getParameter("lat") != null)
				try {
					searchdata.setLat(Float.parseFloat(request.getParameter("lat")));
				} catch (Exception e) {
				}
			if (request.getParameter("lon") != null)
				try {
					searchdata.setLon(Float.parseFloat(request.getParameter("lon")));
				} catch (Exception e) {
				}
			if (request.getParameter("history") != null)
				searchdata.setHistory(request.getParameter("history"));
			if (request.getParameter("vintage") != null)
				searchdata.setVintage(request.getParameter("vintage"));
			if (request.getParameter("vat") != null)
				searchdata.setVat(request.getParameter("vat"));
			if (request.getParameter("pricemaxstring") != null)
				searchdata.setPricemaxstring(request.getParameter("pricemaxstring"));
			if (request.getParameter("priceminstring") != null)
				searchdata.setPriceminstring(request.getParameter("priceminstring"));
			if (request.getParameter("size") != null)
				searchdata.setSize(request.getParameter("size"));
			if ("true".equals(request.getParameter("freetext")))
				searchdata.setFreetext(true);
			if (searchdata.getOrder().equals(""))
				searchdata.setOrder("price");
			searchdata.hostcountry = hostcountry;
			if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")) {
				searchdata.setVintage(Webroutines.getVintageFromName(searchdata.getName()));
				searchdata.setName(Webroutines.filterVintageFromName(searchdata.getName()));
			}
			if (searchdata.getCurrency().equals("")) {
				searchdata.setCurrency(Webroutines.getCookieValue(cookies, "currency", "EUR"));
			}

			if (createWineset) {
				s = new SearchHandler(searchdata, referrer, fuzzy, t, newsearch);
				pageaction = pageaction + tip + suggestion;
				if (s.search && s.wineset != null) {
					getLogger().type = pageaction;
					getLogger().name = searchdata.getName();
					getLogger().vintage = searchdata.getVintage();
					getLogger().created = searchdata.getCreated();
					getLogger().pricemin = searchdata.getPricemin();
					getLogger().pricemax = searchdata.getPricemax();
					getLogger().countryseller = searchdata.getCountry().replace("All", "");
					getLogger().rareold = searchdata.getRareold();
					getLogger().numberofresults = s.wineset.records;
					getLogger().knownwineid = s.wineset.knownwineid;
				}
			} else {
				searchdata.fuzzy = true;
				if (s == null)
					s = new SearchHandler();
				s.wineset = new Wineset(searchdata);
				s.wineset.determineSearchType();
			}
		}
	}

	public void doSearch(HttpServletRequest request) {
		boolean fuzzy = false;
		if ("true".equals(request.getParameter("fuzzy"))) {
			fuzzy = true;
		}
		if ("true".equals(request.getParameter("freetext")))
			searchdata.setFreetext(true);
		s = new SearchHandler(searchdata, referrer, fuzzy, t, true);

	}

	public Webactionlogger getLogger() {
		if (logger == null)
			logger = new Webactionlogger(pageaction, thispage, ipaddress, referrer, "", "", 0, (float) 0.0, (float) 0.0,
					"", false, "", "", "", "", (double) 0.0, 0, searchhistory);
		return logger;
	}

	public SearchHandler getSearchHandler() {
		if (s == null)
			s = new SearchHandler();
		return s;
	}

	public Translator getTranslator() {
		if (t == null) {
			setLanguage();
			if (t == null)
				t = new Translator();
		}
		return t;
	}

	public void cleanup() { // Run to remove unneeded resources after a page has been processed
		s = new SearchHandler();
		cookies = null;
		logger = null;
		// request=null;
		t = null;
		// response=null;
	}

	public String getPageaction() {
		return pageaction;
	}
}
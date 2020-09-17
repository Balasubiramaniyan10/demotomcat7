<%@ page import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Knownwines"
	import="com.freewinesearcher.common.Dbutil"%>
<%
	//if (request.getServerName().toLowerCase().contains("searchasaservice")&&!request.getServletPath().toLowerCase().contains("searchasaservice")) {
	//	response.sendRedirect("/searchasaservice.jsp");
	//	return;
	//}
	String originalurl = (String) request.getAttribute("originalURL");
	if (Configuration.detectSuspectedBot) {
		String ua = request.getHeader("user-agent");
		if (ua == null) {
			ua = "";
		}
		ua = ua.toLowerCase();
		boolean mobile = false;
		if (!mobile && ua.contains("iphone")) {
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
		if (originalurl != null && (originalurl.contains("/mwine/") || originalurl.contains("/mregion/")
				|| originalurl.contains("/mwinery/"))) {
			mobile = true;
		}

		int botstatus = PageHandler.getInstance(request, response).getBotstatus();
		if (!mobile && botstatus >= 2) {
			Dbutil.logger.info("header2.jsp botstatus=" + botstatus + " called, should not happen. IP="
					+ PageHandler.getInstance(request, response).ipaddress);
			response.setStatus(302);
			response.setHeader("Location", "/check.jsp?targeturl=" + Webroutines.URLEncodeUTF8(originalurl));
			response.setHeader("Connection", "close");
			return;
		}
	} else {
		PageHandler.getInstance(request, response).botstatus = 0;
	}

	try {
		if (originalurl == null)
			originalurl = "";
		String querystring = (String) request.getAttribute("originalQueryString");

		if (querystring == null || querystring.equals("")) {
			querystring = "";
		} else {
			querystring = "?" + querystring;
		}
		if (querystring.contains("width")) {
			Dbutil.logger.info("Width in url " + originalurl + "" + querystring);
			Dbutil.logger
					.info("IP: " + request.getRemoteAddr() + ", Referrer: " + request.getHeader("Referer"));
		}

		if (originalurl.contains("/winery/") || originalurl.contains("/store/")
				|| originalurl.contains("/wine-guide/") || originalurl.contains("/region/")) {
			response.setHeader("Cache-Control", "max-age=3600");
		} //HTTP 1.1
			//response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader("Expires", 0); //prevents caching at the proxy server

		if (PageHandler.getInstance(request, response).abuse) {
			response.setStatus(403);
%><%@page import="com.freewinesearcher.common.Configuration"%><jsp:forward
	page="abuse.jsp" />
<%
	return;
		}
		if (originalurl.contains("/region/")
				&& (originalurl.contains(" ") || originalurl.contains("%20") || !originalurl.endsWith("/"))) {
			String newurl = originalurl.replaceAll(" ", "+").replaceAll("%20", "+");
			if (!newurl.endsWith("/"))
				newurl += "/";
			response.setStatus(301);
			response.setHeader("Location", newurl);
			response.setHeader("Connection", "close");
			return;
		}
		if (originalurl.contains("https://") && request.getRemoteUser() == null
				&& (!originalurl.contains("/admin") && !originalurl.contains("/settings")
						&& !originalurl.contains("/moderator") && !originalurl.contains("thankyou"))) {
			String newurl = originalurl.replace("https://", "http://");
			response.setStatus(301);
			response.setHeader("Location", newurl);
			response.setHeader("Connection", "close");
			return;
		}
		//(!Webroutines.getRegexPatternValue("([^%_-]\\d\\d\\d\\d)$",originalurl).equals("")&&!querystring.contains("vintage"))
		if (originalurl.contains("/wine/") && (originalurl.contains("%20") || !Webroutines
				.removeAccents(request.getParameter("name")).equals(request.getParameter("name")))) {
			String originalvintage = Webroutines.getRegexPatternValue("(?:%20| |\\+)(\\d\\d\\d\\d)$",
					originalurl);
			if (request.getHeader("Referrer") != null && request.getHeader("Referrer").contains("vinopedia"))
				Dbutil.logger.info("URL " + originalurl + " found on " + request.getHeader("Referrer"));
			String redurl = "/wine/"
					+ Webroutines.URLEncode(Webroutines.removeAccents(request.getParameter("name")))
					+ (querystring.length() > 1 ? querystring : "");
			if (originalvintage.length() > 0)
				redurl = redurl.replaceAll("\\+" + originalvintage, "") + "+" + originalvintage;
			response.setStatus(301);
			response.setHeader("Location", redurl);
			response.setHeader("Connection", "close");
			return;
		}
		if (originalurl.contains("/wine/") && querystring.startsWith("?vintage")
				&& querystring.length() == 13) {
			String originalvintage = Webroutines.getRegexPatternValue("(\\d\\d\\d\\d)$", querystring);
			if (request.getHeader("Referrer") != null && request.getHeader("Referrer").contains("vinopedia"))
				Dbutil.logger.info("URL " + originalurl + " found on " + request.getHeader("Referrer"));
			String redurl = "/wine/"
					+ Webroutines.URLEncode(Webroutines.removeAccents(request.getParameter("name")));
			if (originalvintage.length() > 0)
				redurl = redurl + "+" + originalvintage;
			response.setStatus(301);
			response.setHeader("Location", redurl);
			response.setHeader("Connection", "close");
			return;
		}
		if (originalurl.contains("/wine/"))
			try {
				int reqvintage = Integer.parseInt(
						Webroutines.getRegexPatternValue("(?:%20| |\\+)(\\d\\d\\d\\d)$", originalurl));
				if (reqvintage > Configuration.thisyear) {
					String redurl = "/wine/"
							+ Webroutines.URLEncode(Webroutines.removeAccents(request.getParameter("name")))
							+ (querystring.length() > 1 ? querystring : "");
					redurl = redurl.replaceAll("\\+" + reqvintage, "");
					response.setStatus(301);
					response.setHeader("Location", redurl);
					response.setHeader("Connection", "close");
				}
			} catch (Exception e) {
			}

		if (request.getParameter("logoff") != null) {
			response.sendRedirect("/logout.jsp");
			return;
		}

		if (originalurl.contains("freewinesearcher") && !originalurl.contains("wine2/")) {
			String newurl = originalurl.replace("freewinesearcher", "vinopedia") + querystring;
			session.setAttribute("fws", "true");
			response.setStatus(301);
			response.setHeader("Location", newurl);
			response.setHeader("Connection", "close");
			return;
		}
	} catch (Exception e) {
		Dbutil.logger.error("Exception in header2.jsp", e);
	}
	if (!PageHandler.getInstance(request, response).block
			&& !PageHandler.getInstance(request, response).abuse) {
%>
<%
	if (!"Y".equals(Webroutines.filterUserInput(request.getParameter("print")))) {
%><link rel="stylesheet" type="text/css"
	href="<%=request.isSecure() ? Configuration.securestylesheet : Configuration.stylesheet%>" />
<%
	} else {
%><link rel="stylesheet" type="text/css" media="print" href="/print.css" />
<%
	}
%>
<%
	if (false && !"".equals(PageHandler.getInstance(request, response).plusonelink)) {
%><script type="text/javascript">
	(function() {
		var po = document.createElement('script');
		po.type = 'text/javascript';
		po.async = true;
		po.src = 'https://apis.google.com/js/plusone.js';
		var s = document.getElementsByTagName('script')[0];
		s.parentNode.insertBefore(po, s);
	})();
</script>
<%
	}
%><meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<%
	if ((originalurl.contains("/index.jsp") && !originalurl.contains("wine-guide")
				&& !originalurl.contains("/store/") || originalurl.contains("/external.jsp")
				|| originalurl.contains("/winelinks.jsp"))) {
%><meta name="robots" content="noindex,nofollow" />
<%
	} else if (originalurl.contains("/wine2")) {
%><meta name="robots" content="noindex,nofollow" />
<%
	} else if (originalurl.contains("topwines")) {
%><meta name="robots" content="noindex,follow" />
<%
	} else {
%><meta name="robots" content="index,follow" />
<%
	}
%><link rel="icon" type="image/png"
	href="<%=Configuration.staticprefix%>/favicon.png" /><%@ include
	file="/snippets/jsincludes.jsp"%>
<%
	}
%>

<!-- Google Analytics -->
<script>
	(function(i, s, o, g, r, a, m) {
		i['GoogleAnalyticsObject'] = r;
		i[r] = i[r] || function() {
			(i[r].q = i[r].q || []).push(arguments)
		}, i[r].l = 1 * new Date();
		a = s.createElement(o), m = s.getElementsByTagName(o)[0];
		a.async = 1;
		a.src = g;
		m.parentNode.insertBefore(a, m)
	})(window, document, 'script', '//www.google-analytics.com/analytics.js',
			'ga');

	ga('create', 'UA-1788182-2', 'auto', {
		'legacyCookieDomain' : 'vinopedia.com'
	});
<%=PageHandler.getInstance(request, response).asyncGAtracking%>
	ga('send', 'pageview');
</script>
<!-- End Google Analytics -->

<script src="//load.sumome.com/"
	data-sumo-site-id="acdd8ce37b7f36678940878463b273d4ff0c2a4bfa21ae34571c8b2fe3614ca6"
	async="async"></script>
<!-- Quantcast Tag -->
<script type="text/javascript">
	var _qevents = _qevents || [];

	(function() {
		var elem = document.createElement('script');
		elem.src = (document.location.protocol == "https:" ? "https://secure"
				: "http://edge")
				+ ".quantserve.com/quant.js";
		elem.async = true;
		elem.type = "text/javascript";
		var scpt = document.getElementsByTagName('script')[0];
		scpt.parentNode.insertBefore(elem, scpt);
	})();

	_qevents.push({
		qacct : "p-_LkbUtSUQgFCM"
	});
</script>

<noscript>
	<div style="display: none;">
		<img src="//pixel.quantserve.com/pixel/p-_LkbUtSUQgFCM.gif" border="0"
			height="1" width="1" alt="Quantcast" />
	</div>
</noscript>
<!-- End Quantcast tag -->
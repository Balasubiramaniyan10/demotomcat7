<%@ page import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Configuration"%>
<%
	if (Configuration.detectSuspectedBot) {
		String originalurl = (String) request.getAttribute("originalURL");
		int botstatus = PageHandler.getInstance(request, response).getBotstatus();

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

		if (!mobile && botstatus >= 2) {
			Dbutil.logger.info("headersmall.jsp botstatus=" + botstatus + " called, should not happen. IP="
					+ PageHandler.getInstance(request, response).ipaddress);

			response.setStatus(302);
			response.setHeader("Location", "/check.jsp?targeturl=" + Webroutines.URLEncodeUTF8(originalurl));
			response.setHeader("Connection", "close");
			return;
		}
	} else {
		PageHandler.getInstance(request, response).botstatus = 0;
	}
	if (PageHandler.getInstance(request, response).abuse) {
%>
<%@page import="com.freewinesearcher.common.Dbutil"%><jsp:forward
	page="abuse.jsp" />
<%
	return;
	}
	if (PageHandler.getInstance(request, response).block) {
		out.print("An error occurred at line: 17 in the jsp file: /index.jsp");
		return;
	}
	response.addHeader("Vary", "Accept-Encoding");

	if (request.getHeader("Referrer") != null)
		Webroutines.saveReferrer(request.getRemoteAddr(), request.getHeader("Referrer"));
	if (request.getParameter("logoff") != null) {
		response.sendRedirect("/logout.jsp");
		return;
	}
	String originalurl = (String) request.getAttribute("originalURL");
	if (originalurl == null)
		originalurl = "";

	String querystring = (String) request.getAttribute("originalQueryString");
	if (querystring == null) {
		querystring = "";
	} else {
		querystring = "?" + querystring;
	}
	if (originalurl.contains("/mwine/") && (originalurl.contains("%20")
			|| !Webroutines.removeAccents(request.getParameter("name")).equals(request.getParameter("name")))) {
		String originalvintage = Webroutines.getRegexPatternValue("(?:%20| |\\+)(\\d\\d\\d\\d)$", originalurl);
		if (request.getHeader("Referrer") != null && request.getHeader("Referrer").contains("vinopedia"))
			Dbutil.logger.info("URL " + originalurl + " found on " + request.getHeader("Referrer"));
		String redurl = "/mwine/"
				+ Webroutines.URLEncode(Webroutines.removeAccents(request.getParameter("name")))
				+ (querystring.length() > 1 ? querystring : "");
		if (originalvintage.length() > 0)
			redurl = redurl.replaceAll("\\+" + originalvintage, "") + (querystring.length() > 0 ? "&" : "?")
					+ "vintage=" + originalvintage;
		response.setStatus(301);
		response.setHeader("Location", redurl);
		response.setHeader("Connection", "close");
		return;
	}

	if (request.getParameter("logoff") != null) {
		response.sendRedirect("/logout.jsp");
		return;
	}

	if (originalurl.contains("searcher")) {
		String newurl = originalurl.replace("freewinesearcher", "vinopedia") + querystring;
		session.setAttribute("fws", "true");
		response.setStatus(301);
		response.setHeader("Location", newurl);
		response.setHeader("Connection", "close");
		return;
	}
%>
<%
	if ((originalurl.contains("/mwine/") && PageHandler.getInstance(request, response).s != null
			&& PageHandler.getInstance(request, response).s.wineset != null
			&& PageHandler.getInstance(request, response).s.wineset.Wine != null)
			&& PageHandler.getInstance(request, response).s.wineset.Wine.length > 0
			|| originalurl.contains("/mwinery/") || originalurl.contains("/mregion/")
			|| originalurl.endsWith("/m")) {
%><meta name="robots" content="index,follow" />
<%
	} else {
%><meta name="robots" content="noindex,nofollow" />
<%
	}
%>
<link rel="shortcut icon" href="/favicon.ico" />
<link rel="stylesheet" type="text/css" href="/themesmall.css?v=2" />
<script src='/js/mobile.js' type='text/javascript'></script>
</head>
<body onclick="javascript:emptySuggest();">
	<div class="logo">
		<a href='/m' title='Vinopedia wine search engine'><img
			src="/images/smallheader.jpg" alt="Vinopedia wine search engine" /></a>
	</div>
	<div class="centralpane">
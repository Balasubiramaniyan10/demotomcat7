<%@ page import="java.io.*" 
	import="java.net.*" 
	import="java.text.*"
	import="java.lang.*" 
	import="java.sql.*"
	import="com.freewinesearcher.online.PageHandler"
	import="java.util.ArrayList"
	import="com.freewinesearcher.common.Configuration"
	import="com.freewinesearcher.common.Dbutil"
	import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.batch.Emailer"
	import="com.freewinesearcher.common.Dbutil"%>
<%
	response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server

	//Dbutil.logger.info("Challenge");
	String target = PageHandler.getInstance(request, response).URLbeforebotcheck;
	if (target == null || target.equals("")) {
		target = (String) request.getParameter("targeturl");
	}
	if (target == null || target.equals("") || target.contains("check.jsp")) {
		target = "https://www.vinopedia.com";
	}
	//if (target == null || target.equals("") || target.contains("checkimage")) {
	//	target = "https://www.vinopedia.com";
	//}
	PageHandler p = PageHandler.getInstance(request, response, "Check scraper");
	Dbutil.logger.info("******************************** check.jsp botstatus='" + p.botstatus + "',  ip='" + p.normalip
			+ "', forwarded ip='" + p.forwardedforip + "', useragent='" + p.useragent + "', hostname='"
			+ InetAddress.getByName(p.ipaddress).getHostName() + "', referrer='" + p.referrer + "'");

	boolean validcookie = false;
	if (p.botstatus == 0) {
		response.setStatus(302);
		response.setHeader("Location", target);
		response.setHeader("Connection", "close");
		return;

	}
	if (p.botstatus == 4 || p.botstatus == 2) {
		Dbutil.logger.info("Forward to are you human, normal ip='" + p.normalip + "', forwarded ip='"
				+ p.forwardedforip + "', useragent='" + p.useragent + "', hostname='"
				+ InetAddress.getByName(p.ipaddress).getHostName() + "', referrer='" + p.referrer + "'");
		p.botstatus = 2; //Challenged but checked again
		response.setStatus(302);
		response.setHeader("Location", "/areyouhuman.jsp?targeturl=" + Webroutines.URLEncodeUTF8(target));
		response.setHeader("Connection", "close");
		return;

	} else {
		if (p.botstatus != 1) {
			Cookie[] cookies;
			cookies = request.getCookies();
			String sessionid = "";
			String PagehandlerSessionid = "invalid";
			if (cookies != null)
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("JSESSIONID")
							&& (cookie.getPath() == null || cookie.getPath().equals("/")))
						sessionid = cookie.getValue();
				}

			if (p.cookies != null)
				for (Cookie cookie : p.cookies) {
					if (cookie.getName().equals("JSESSIONID")
							&& (cookie.getPath() == null || cookie.getPath().equals("/")))
						PagehandlerSessionid = cookie.getValue();
				}
			if (sessionid.equals(PagehandlerSessionid))
				validcookie = true;
		}

		//Dbutil.logger.info("Challenge, normal ip='"+p.normalip+"', forwarded ip='"+p.forwardedforip+"', useragent='"+p.useragent+"', hostname='"+InetAddress.getByName(p.ipaddress).getHostName()+"', referrer='"+p.referrer+"'");

		String challenge = p.getImageChallenge(request);
		//if (p.cookies!=null&&p.cookies.length>0){
		if (true) {//no cookies if not redirected
%>
<html>
<head>
<%@ include file="/snippets/jsincludes.jsp"%>
<script type='text/javascript'>
function forward() {
	// $("body").load("<%=target%>");
	location.href = "<%=target%>";
}
</script>
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
</head>
<body>
	<img src='/checkimage/?response=<%=challenge%>' onload='forward()' />Loading Vinopedia...
	<br/>
	<br/>
	<a href='<%=target%>'>Please click here if this page does not load automatically.</a>
</body>
</html>
<%
	} else {
%>
<html>
<head>
<%@ include file="/snippets/jsincludes.jsp"%>

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

</head>
<body>
	<b>Vinopedia requires cookies to load properly. Please enable
		cookies in your browser or device, then reload.</b>
	<br />
	<br />
	<img src='/checkimage/?response=<%=challenge%>' />
</body>
</html>
<%
	}
	}
%>
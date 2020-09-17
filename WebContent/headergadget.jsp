<%@ page   
	import = "com.freewinesearcher.online.Webroutines"
%>
<%
	if (request.getHeader("Referrer")!=null) Webroutines.saveReferrer(request.getRemoteAddr(),request.getHeader("Referrer"));
  	if (request.getParameter("logoff") != null) {
    response.sendRedirect("/logout.jsp");
    return;
  }
%>
<!-- google_ad_section_start -->
<META NAME="Keywords" CONTENT="wine, search, wines, price, prices, searcher, compare, free, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn, zoeken, zoeker, vin, Wein, suchen, vino, buscar, precio">
<META NAME="Description" CONTENT="Find and compare prices on wines. Free and unfiltered. Get daily search results by email and &#82;&#83;&#83; feeds.">
<!-- google_ad_section_end -->
<META NAME="Robots" CONTENT="INDEX,FOLLOW">
<LINK REL="SHORTCUT ICON" HREF="/favicon.ico">
<link rel="stylesheet" type="text/css" href="/themegadget.css" />
</head>
<body topmargin="0" leftmargin="0" rightmargin="0">
<a href='/gadget.jsp'><div class="logo">
</div></a>
<div class="centralpane">

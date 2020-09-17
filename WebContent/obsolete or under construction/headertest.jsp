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
<META NAME="Keywords" CONTENT="wine, wines, price, prices, searcher, compare, free, search, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn, vin, Wein, suchen, vino, buscar">
<META NAME="Description" CONTENT="Find and compare prices on wines. Free and unfiltered. Get daily search results by email and &#82;&#83;&#83; feeds.">
<!-- google_ad_section_end -->
<META NAME="Robots" CONTENT="INDEX,FOLLOW">
<LINK REL="SHORTCUT ICON" HREF="/favicon.ico">
<link rel="stylesheet" type="text/css" href="/theme.css" />
</head>
<body>
<div class="centralpane">
   <map name="Headermap">
   <area title="Home" href="/" shape="rect" coords="165,86,271,106">
   <area title="PriceAlert" href="/settings/index.jsp" shape="rect" coords="276,86,382,106" >
   <area title="Forum" href="/forum" shape="rect" coords="387,86,492,106" >
   <area title="About Us" href="/about.jsp" shape="rect" coords="497,86,600,106" >
   <area title="Disclaimer" href="/disclaimer.jsp" shape="rect" coords="607,86,712,106">
   <area title="Contact" href="/contact.jsp" shape="rect" coords="717,86,823,106" >
   <area title="Links" href="/links.jsp" shape="rect" coords="829,86,920,106" >
   </map><img border="0" src="/images/Headerlinks.jpg" alt="vinopedia: Search for wine prices in Europe" usemap="#Headermap">
<a href="/about.jsp"></a>
<a href="/forum"></a>
<a href="/settings/index.jsp"></a>

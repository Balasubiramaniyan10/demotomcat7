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
<META NAME="Keywords" CONTENT="wine, wines, searcher, free, search, crawler, Bordeaux, Bourgogne, Burgundy, Grand Cru, Premier Cru, primeur, chateau, wijn, vin, Wein, vino">
<META NAME="Description" CONTENT="Find and compare prices on wines. Free and unfiltered. Get daily search results by email and RSS feeds.">
<!-- google_ad_section_end -->
<META NAME="Robots" CONTENT="INDEX,FOLLOW">
<LINK REL="SHORTCUT ICON" HREF="/favicon.ico">
<link rel="stylesheet" type="text/css" href="/themetest.css" />
</head>
<body topmargin="0" leftmargin="0" rightmargin="0">
<div align="center">
 <table border="0" cellpadding="0" style="border-collapse: collapse" bordercolor="#111111" width="100%">
  <tr>
   <td background="/images/Headerside.jpg" align="center">
   <map name="Headermap">
   <area coords="165, 111, 237, 145" shape="rect" href="/" target="_blank">
   <area title="Home" href="/" shape="rect" coords="212,86,331,106">
   <area title="PriceAlert" href="/settings/index.jsp" shape="rect" coords="337,86,457,106" >
   <area title="Contact" href="/contact.jsp" shape="rect" coords="464,86,583,106" >
   <area title="About Us" href="/about.jsp" shape="rect" coords="589,86,706,106" >
   <area title="Disclaimer" href="/disclaimer.jsp" shape="rect" coords="714,86,833,106">
   </map><img border="0" src="images/Header.jpg" alt="vinopedia: Search for wine prices in Europe" usemap="#Headermap"></td>
  </tr>
 </table>
</div>




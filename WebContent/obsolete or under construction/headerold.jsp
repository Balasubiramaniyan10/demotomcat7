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
<link rel="stylesheet" type="text/css" href="/theme.css" />
</head>
<body>
<TABLE class="logo" BORDER="0" CELLSPACING="0" WIDTH="600" HEIGHT="120" >
   <a href='<%= response.encodeURL("/")%>' ><TR><TD WIDTH="60" HEIGHT="80" ><TD WIDTH="60" HEIGHT="80" ></TD><TD WIDTH="100" HEIGHT="80" ></TD><TD WIDTH="80" HEIGHT="80" ></TD><TD WIDTH="90" HEIGHT="80" ></TD><TD WIDTH="70" HEIGHT="80" ><TD WIDTH="80" HEIGHT="80" ></TD><TD WIDTH="70" HEIGHT="80" ></TD></TR></a>
   <TR><a href='<%= response.encodeURL("/")%>' ><TD WIDTH="80" HEIGHT="40" ></TD></a><TD><a href="/">Home</a><br/></TD><TD><a href="/settings/index.jsp">Notifications</a><br/></TD><TD><a href="/about.jsp">About us</a><br/></TD><TD><a href="/contact.jsp">Contact</a><br/></TD><TD><a href="/disclaimer.jsp">Disclaimer</a></TD><TD><% if (request.getRemoteUser()!=null) {%><a href="<%=response.encodeURL("/logout.jsp") %>">Log out</a><br/><%}%></TD><TD></TD><TD></TD></TR></a>
   </TD></TR></TABLE>


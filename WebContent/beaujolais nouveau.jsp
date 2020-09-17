<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.common.Configuration"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%PageHandler p=PageHandler.getInstance(request,response,"Retailer options");%><!DOCTYPE HTML>
<% int vintage=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
	if (java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.MONTH)<9) vintage=vintage-1;
	p.createWineset=false;
	p.processSearchdata(request);
	p.searchdata.setName("beaujolais nouveau");
	p.searchdata.setVintage(vintage+"");
	p.s.wineset.getKnownWineList(true);
%><head>
<title>Beaujolais Nouveau <%=vintage %> Prices</title>
<meta name="keywords" content="How to get listed as a retailer" />
<meta name="description" content="How to get listed as a retailer" />
<%@ include file="/header2.jsp" %>
</head>
<body  onload="javascript:doonload();">
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<div class='main'>
	<div id='mainleft'>	
	<noscript><img src='/images/nojs.gif' alt=''/></noscript>
<h1>Beaujolais Nouveau <%=vintage %></h1>
Beaujolais Nouveau is a category of wines from the Beaujolais region in France. They are released on the third Thursday of November, just a few weeks after the harvest. Because they are so young, their color is pink to purple. <br/><br/>
Beaujolais Nouveau (they used to be called Beaujolais Primeur) is intended for immediate consumption and not for cellaring. The production process with malolactic fermentation is aimed to make them drinkable despite their young age, leaving as little as possible tannins in the wine.<br/><br/>
To find prices of Beaujolais Nouveau wines, click on the wine of your choice in the list below. You can also search for <a href='/index.jsp?name=Beaujolais+Nouveau+<%=vintage %>'>all Beaujolais Nouveau prices</a>.<br/><br/>
  <%
  for (int n:p.s.wineset.knownwinelist.keySet()){
	  out.print("<a rel='external' href='"+Webroutines.winelink(Knownwines.getUniqueKnownWineName(n),vintage,false)+"'>"+Knownwines.getKnownWineName(n)+" "+vintage+"</a><br/>");	  
  }
  
 %>
<%@ include file="/snippets/footer.jsp" %>	
		</div></div> <%// workaround: IE positioning of footer %>
		</div>
		</div> <!--  main--> 
<%} %>
	 <script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>
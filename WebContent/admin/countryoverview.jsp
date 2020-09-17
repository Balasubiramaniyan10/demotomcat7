<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
%>
<%String order = request.getParameter("order");

	%>
<title>
Overview of Countries
</title>
</head>
<body>
<jsp:include page="/admin/adminlinks.jsp" /><h1>
Overview of all shops</h1>
<% out.print(Webroutines.query2table("select vat.country,sel.shops,sel.wines,round(sel.wines/sel.shops) as `Wines per shop` from (select countrycode,count(distinct shopid) as shops,count(*) as wines from wines join shops on (wines.shopid=shops.id) group by shops.countrycode order by shops desc) sel join vat on (sel.countrycode=vat.countrycode) order by shops desc;",true,true));%>
<% out.print(Webroutines.query2table("select count(distinct shopid) as `Total shops`,count(*) as `Total wines` from wines join shops on (wines.shopid=shops.id);",true,false));%>
<br/><i>Wat is die Starrenburg strak bezig hè?</i>
</body> 
</html>
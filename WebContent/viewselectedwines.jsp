<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.batch.StoreReport"
import = "com.freewinesearcher.common.Configuration"
import = "com.freewinesearcher.common.Shop"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><!DOCTYPE HTML>
<head>
<title></title>
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
<%int shopid=0;
String code=request.getParameter("authorizationcode");
try{shopid=Integer.parseInt(request.getParameter("store"));}catch(Exception e){};
int year=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
int month=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.MONTH);
if (month==0){
	year--;month=12;
}
PageHandler p=PageHandler.getInstance(request,response,"Retailer selected wines "+year+"-"+(month>9?month:"0"+month));
p.getLogger().shopid=shopid+"";
Shop s = new Shop(shopid);
if (StoreReport.codeOK(code,shopid)||request.isUserInRole("admin")) {%>
	<br/><br/><h1>Wines displayed for <%=s.shopname %> in <%=StoreReport.getMonthName(month) %> <%=year %>.</h1>
		<% 
		//out.write(Webroutines.query2table("select concat(wines.name,CAST(if(logging.vintage>0,logging.vintage,'') AS CHAR)) as Wine,price as `Price on site`,format(priceeuroex,2) as `Price in euro ex. VAT`,count(*) as Views, format(count(*) * priceeuroex,2)  as `Total value (euro)` from logging join wines on (logging.wineid=wines.id) where logging.shopid="+shopid+" and date between date('"+StoreReport.getStartDate(month,year)+"') and date('"+StoreReport.getEndDate(month,year)+"') and bot=0 and (type='Storepage' or type='Store wineinfo') group by wineid,wines.vintage order by Views desc;", true, false));
		out.write(Webroutines.getViewedWines(month,year,shopid,s.currency));
		


} else {
	%>Your authorization code is not correct.<%
	p=PageHandler.getInstance(request,response,"Retailer selected wines invalid code");
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
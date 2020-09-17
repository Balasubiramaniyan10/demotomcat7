<%@page import="com.freewinesearcher.batch.SponsorStats"%>
<%@page import="com.freewinesearcher.online.Webroutines"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<html><body><%@page import="com.freewinesearcher.batch.StoreReport"%><%@page import="com.freewinesearcher.common.datamining.Chart"%><%
int shopid=0;
int year=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
int month=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.MONTH);
if (month==0){
	year--;month=12;
}
try{shopid=Integer.parseInt(request.getParameter("shopid"));}catch(Exception e){}
try{year=Integer.parseInt(request.getParameter("year"));}catch(Exception e){}
try{month=Integer.parseInt(request.getParameter("month"));}catch(Exception e){}
boolean refresh=false;
try{refresh="on".equals(request.getParameter("refreshstats"));}catch(Exception e){}
if (refresh) SponsorStats.refresh();
StoreReport report=new StoreReport(month,year);
report.setShop(shopid); 
report.generateReport();
if (report.sponsorship!=null) request.getSession().setAttribute("chart"+report.sponsorship.id,report.sponsorship);


ArrayList shops = Webroutines.getShopList("",true); 
%>


<h1>Jeroens Sales Dashboard</h1>
<form action='salesdashboard.jsp' method='post'>
Store: <select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%><option value="<%=shops.get(i)%>"<% if (((String)shops.get(i)).equals(shopid+"")) out.print(" selected='selected'");%>><%=shops.get(i+1)%><%}%></select>
Month: <select name='month'>
<% for (int i=1;i<=12;i++){
out.write("<option value='"+i+"'"+(month==i?" selected='selected'":"")+">"+StoreReport.getMonthName(i)+"</option>");
}
%></select>
Year: <select name='year'>
<% for (int i=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR)-3;i<=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);i++){
out.write("<option value='"+i+"'"+(year==i?" selected='selected'":"")+">"+i+"</option>");
}
%></select>
<input type='checkbox' name='refreshstats'/>Refresh statistics
 <input type="Submit" name="testshop"   value="Go!" /></form>

<%if (report.body!=null){
float dollar=Float.parseFloat(Dbutil.readValueFromDB("select * from currency where currency='USD'","rate")); 
if (shopid>0) out.write("Email address: "+Dbutil.readValueFromDB("select * from shops where id="+shopid,"email")); %><br/>
Subject:<%=report.subject%><br/><br/><%=report.body.replace("<img src=cid:stats>","<img src='/images/chart/"+(report.sponsorship==null?"":report.sponsorship.id+"")+"/"+new java.sql.Timestamp(new java.util.Date().getTime()).toString()+"'/>")%>
<br/><br/>Wines displayed:
<%out.write(Webroutines.query2table("select concat(wines.name,CAST(if(logging.vintage>0,logging.vintage,'') AS CHAR)) as Wine,price as `Price on site`,format(priceeuroex,2) as `Price in euro ex. VAT`,count(*) as Views, format(count(*) * priceeuroex,2)  as `Total value` from logging join wines on (logging.wineid=wines.id) where logging.shopid="+shopid+" and date between date('"+report.startdate+"') and date('"+report.enddate+"') and bot=0 and (type='Storepage' or type='Store wineinfo') group by wineid,wines.vintage order by Views desc;", true, false));%><br/>
Wines displayed:
<%out.write(Webroutines.query2table("select concat(wines.name,CAST(if(logging.vintage>0,logging.vintage,'') AS CHAR)) as Wine,price as `Price on site`,format(priceeuroex/"+dollar+",2) as `Price in US$ ex. sales tax`,count(*) as Views, format(count(*) * priceeuroex/"+dollar+",2)  as `Total value` from logging join wines on (logging.wineid=wines.id) where logging.shopid="+shopid+" and date between date('"+report.startdate+"') and date('"+report.enddate+"') and bot=0  and (type='Storepage' or type='Store wineinfo') group by wineid,wines.vintage order by Views desc;", true, false));%><br/>

<%} else { %>
No data available.
<%} %>
</body></html>
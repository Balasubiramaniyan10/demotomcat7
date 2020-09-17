<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<%@page import="java.util.ArrayList"%>

<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.TimeZone"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%><html>
<head>
<title>Finance Dashboard</title>
<%@ include file="/header2.jsp" %>

</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>

<%@ include file="/snippets/textpage.jsp" %>
<%@ include file="adminlinks.jsp" %>
<%  	String shopid = request.getParameter("shopid");
if (shopid==null||shopid.equals("")) shopid="0";
int shop=0;
try{	shop=Integer.valueOf(shopid);}
catch (Exception exc){
	
}
boolean validdates=true;
SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
int history=7;
try {history=Integer.parseInt(request.getParameter("history"));}catch(Exception e){}
Timestamp startdate=null;
Timestamp enddate=null;
try {startdate=new Timestamp(df.parse(request.getParameter("startdate")).getTime());}catch(Exception e){out.println("Error");}
try {enddate=new Timestamp(df.parse(request.getParameter("enddate")).getTime());}catch(Exception e){out.println("Error");}
if (startdate==null) {
	validdates=false;
	TimeZone tz = TimeZone.getTimeZone("Europe/Madrid");

	Calendar cal = GregorianCalendar.getInstance();
	cal.add(Calendar.MONTH, -3);
	cal.set(Calendar.DAY_OF_MONTH,1);
	cal.set(Calendar.HOUR,0);
	cal.set(Calendar.MINUTE,0);
	cal.set(Calendar.SECOND,0);
	cal.set(Calendar.MILLISECOND,0);
	
	startdate = new Timestamp(cal.getTime().getTime()); 
		
}
if (enddate==null){
	validdates=false;
	
	TimeZone tz = TimeZone.getTimeZone("Europe/Madrid");

	Calendar cal = GregorianCalendar.getInstance();
	cal.set(Calendar.DAY_OF_MONTH,1);
	cal.set(Calendar.HOUR,0);
	cal.set(Calendar.MINUTE,0);
	cal.set(Calendar.SECOND,0);
	cal.set(Calendar.MILLISECOND,0);
	enddate = new Timestamp(cal.getTime().getTime()); 
	
}


ArrayList shops = Webroutines.getPayingShopList(); 
%>


<h1>Finance Dashboard</h1>
<form action='financedashboard.jsp'>
Store: <select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(shopid))) out.print(" Selected");%>><%=shops.get(i+1)%>
<%}%></select><br/>
Period: from <input type='text' name='startdate' value='<%=startdate.toString().substring(0,10) %>'/> to (not including) <input type='text' name='enddate' value='<%=enddate.toString().substring(0,10)  %>'/> <input type="Submit" name="testshop"   value="Go!" /></form>
<%if (shop>0&&validdates){ 
	out.write("Email address: "+Dbutil.readValueFromDB("select * from shops where id="+shop,"invoiceemail")+"<br/>"); 
	out.write("VAT number: "+Dbutil.readValueFromDB("select * from shops where id="+shop,"vatnumber")+"<br/>"); 
	out.write("Contract comment: "+Dbutil.readValueFromDB("select * from shops where id="+shop,"commercialcomment")+"<br/>"); 
	out.write("<br/><br/>To: "+Dbutil.readValueFromDB("select * from shops where id="+shop,"shopname")+",<br/>");
	out.write("Attention: "+Dbutil.readValueFromDB("select * from shops where id="+shop,"contactname")+"<br/>"); 
	out.write(""+Dbutil.readValueFromDB("select * from shops where id="+shop,"invoiceaddress")+"<br/>");
	String vatnum=Dbutil.readValueFromDB("select * from shops where id="+shop,"vatnumber");
	if (vatnum!=null&&vatnum.length()>1){
	out.write("VAT number: "+vatnum+"<br/>"); 
	out.write("<br/>Subject: Invoice period "+startdate.toString().substring(0,10)+" to "+enddate.toString().substring(0,10));
	}
	%>

<br/><br/>
<% if (Dbutil.readIntValueFromDB("select id from shops where id="+shop+" and starttrial>date('2001-01-01');","id")>0) out.write(Webroutines.query2table("select * from (select monthyear as Period,Visitors,staffelgroup as Category, if(log.date<shops.startpaying,'TRIAL','') as Comment,concat(staffel.currency,' ',format(if(log.date<shops.startpaying,0,price),2)) as Price from (select count(distinct(ip)) as Visitors,concat(monthname(date),' ',year(date)) as monthyear,shopid,date  from logging where shopid="+shop+" and date between date('"+startdate.toString().substring(0,10)+"') and date('"+enddate.toString().substring(0,10)+"')  and bot=0 group by monthyear) log join shops on (log.shopid=shops.id) join staffel on (shops.staffelname=staffel.staffelname and visitorslow<=visitors and visitorshigh>=visitors) order by date) lst  union select 'Total' as Period, '' as visitors, '' as Category,'' as Comment,concat(currency,' ',format(sum(price),2)) as Price from (select monthyear as Period,Visitors,staffelgroup as Category,staffel.currency, if(log.date<shops.startpaying,'TRIAL','') as Comment,if(log.date<shops.startpaying,0,price) as Price from (select count(distinct(ip)) as Visitors,concat(monthname(date),' ',year(date)) as monthyear,shopid,date  from logging where shopid="+shop+" and date between date('"+startdate.toString().substring(0,10)+"') and date('"+enddate.toString().substring(0,10)+"')  and bot=0 group by monthyear) log join shops on (log.shopid=shops.id) join staffel on (shops.staffelname=staffel.staffelname and visitorslow<=visitors and visitorshigh>=visitors)) sel ;", true, false));%><br/>
<%	String bannerids=Dbutil.readValueFromDB("select group_concat(id SEPARATOR '|') as ids from banners where shopid="+shop,"ids");
if (bannerids!=null&&bannerids.length()>0) out.write("Banners<br/>"+Webroutines.query2table("select concat(monthname(date),' ',year(date)) as Period, count(*) as Views from logging where bot=0 and date between date('"+startdate.toString().substring(0,10)+"') and date('"+enddate.toString().substring(0,10)+"') and bannersshown regexp '(^| )("+bannerids+")($| )' group by Period order by date;", true, false));%>
<%} %>


<%@ include file="/snippets/textpagefooter.jsp" %>

</body>
</html>
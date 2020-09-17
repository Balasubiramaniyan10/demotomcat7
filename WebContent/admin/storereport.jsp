<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<html><body><%@page import="com.freewinesearcher.batch.StoreReport"%><%@page import="com.freewinesearcher.common.datamining.Chart"%><%
int shopid=3;
int year=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
int month=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.MONTH);
if (month==0){
	year--;month=12;
}
try{shopid=Integer.parseInt(request.getParameter("shopid"));}catch(Exception e){}
try{year=Integer.parseInt(request.getParameter("year"));}catch(Exception e){}
try{month=Integer.parseInt(request.getParameter("month"));}catch(Exception e){}
StoreReport report=new StoreReport(month,year);
report.setShop(shopid); 
report.generateReport();
if (report.sponsorship!=null) request.getSession().setAttribute("chart"+report.sponsorship.id,report.sponsorship);
if (report.body!=null){
%>Subject:<%=report.subject%><br/><br/><%=report.body.replace("<img src=cid:stats>","<img src='/images/chart/"+(report.sponsorship==null?"":report.sponsorship.id+"")+"/"+new java.sql.Timestamp(new java.util.Date().getTime()).toString()+"'/>")%>
<%} %>
</body></html>
<%@page import= "java.util.Date"
import= "com.freewinesearcher.batch.StoreReport"
import= "com.freewinesearcher.batch.SponsorStats"
import= "java.text.SimpleDateFormat"
import= "java.util.Calendar"
import= "com.freewinesearcher.common.datamining.Chart"
%>
<% 

java.util.Calendar cal = java.util.GregorianCalendar.getInstance();
cal.add(java.util.Calendar.HOUR, -24);
int year=cal.get(java.util.GregorianCalendar.YEAR);
int month=cal.get(java.util.GregorianCalendar.MONTH);
SponsorStats stats=SponsorStats.getSponsorStats(month,year);
Chart chart=stats.getChart();
request.getSession().setAttribute("chart"+chart.id,chart);
%><img src='/images/chart/<%=chart.id%>/<%=new java.sql.Timestamp(new java.util.Date().getTime())%>'/>
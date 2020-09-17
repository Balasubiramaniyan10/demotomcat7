<%@page import="com.freewinesearcher.common.datamining.Chart"%>
<html><body><%
Chart perf=new Chart();
perf=new Chart();
perf.xscale=400;
perf.yscale=200;
perf.title="Average loadtime";

perf.createPerformanceChart(48);
request.getSession().setAttribute("chart"+perf.id,perf);
%>
<img src='/images/chart/<%=perf.id%>/<%=new java.sql.Timestamp(new java.util.Date().getTime())%>'/>
</body></html>
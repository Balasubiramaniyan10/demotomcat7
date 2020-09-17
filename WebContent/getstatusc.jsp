<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.online.QueueLogger"%><html><head><meta name="viewport" content="width=320" /><meta name="MobileOptimized" content="320" /></head><body style="background-color:#000;color:#fff">
<%@page import="com.freewinesearcher.common.Dbutil" %><% 
String sysdate=Dbutil.readValueFromDB("select sysdate() as sd;","sd");
int lastvisit=Dbutil.readIntValueFromDB("SELECT TIMESTAMPDIFF(MINUTE,logtable.date,sysdate()) as dif from (select date from logging order by id desc limit 1) logtable;","dif");
int visitors=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where date>(now()- INTERVAL 10 MINUTE);","n");
int visitorstoday=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where date>=curdate() and bot=0;","n");
int visitorsyesterday=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where date>=date_sub(curdate(), interval 1 DAY) and date<curdate() and bot=0;","n");
int visitors24=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where date>=(now()- interval 1 DAY) and bot=0;","n");
if (lastvisit>5||visitors<2) {
	out.print("Problem.<br/>");
} 
out.print(visitors24+" (24 h).<br/>");
out.print(visitorsyesterday+" (yest).<br/>");
out.print(visitors+" (10 min).<br/>");
out.print(visitorstoday+" (today).");
%></body></html>
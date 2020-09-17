<html><head><meta name="viewport" content="width=320" /><meta name="MobileOptimized" content="320" /></head><body>
<%@page import="com.freewinesearcher.common.Dbutil"%><% 
String sysdate=Dbutil.readValueFromDB("select sysdate() as sd;","sd");
int lastvisit=Dbutil.readIntValueFromDB("SELECT TIMESTAMPDIFF(MINUTE,logtable.date,sysdate()) as dif from (select date from logging order by date desc limit 1) logtable;","dif");
int visitors=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where date>(now()- INTERVAL 10 MINUTE);","n");
int visitorstoday=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where type not like 'App iPhone %' and date>=curdate() and bot=0;","n");
int iphonetoday=Dbutil.readIntValueFromDB("SELECT count(distinct(hostname)) as n from logging where date>=curdate() and bot=0 and type like 'App iPhone %' ;","n");

int visitorsyesterday=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where type not like 'App iPhone %' and date>=date_sub(curdate(), interval 1 DAY) and date<curdate() and bot=0;","n");
int visitors24=Dbutil.readIntValueFromDB("SELECT count(distinct(ip)) as n from logging where type not like 'App iPhone %' and date>=(now()- interval 1 DAY) and bot=0;","n");
int iphone24=Dbutil.readIntValueFromDB("SELECT count(distinct(hostname)) as n from logging where date>=(now()- interval 1 DAY) and bot=0 and type like 'App iPhone %' ;","n");
if (lastvisit>5||visitors<2) {
	out.print("Problem.<br/>");
} else {
	out.print("All is OK.<br/>");
}
out.print("Last visit was "+lastvisit+" minutes ago.<br/>");
out.print(visitors+" visitors in the last 10 minutes.<br/>");
out.print(visitors24+" visitors in the last 24 hours (web), "+iphone24+" iPhone.<br/>");
out.print(visitorstoday+" visitors today (web), "+iphonetoday+" iPhone.<br/>");
out.print(visitorsyesterday+" visitors yesterday.<br/>");
%></body></html>
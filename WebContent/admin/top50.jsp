<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%><html>
<head>
<title>Top 50 lists</title>
<%@ include file="/header2.jsp" %>
</head>
<body>
<% int n=50;
try {n=Integer.parseInt(request.getParameter("n"));}catch(Exception e){}
int history=7;
try {history=Integer.parseInt(request.getParameter("history"));}catch(Exception e){}%>
<h1>Top 50 lists</h1>
<form action='top50.jsp'>
Top <input type='text' name='n' value='<%=n %>'/> of last <input type='text' name='history' value='<%=history %>'/> days.<input type='submit'/><br/></form>
<h2>Top <%=n %> views of shops last <%=history %> days</h2><%=Webroutines.query2table("select shopname as 'Shop name',baseurl as Url, shopid,count(distinct(ip)) as Visitors from logging join shops on (logging.shopid=shops.id) where date > (now() - interval "+history+" day)  and (type = 'Storepage' or type='Banner') and shopid>0 group by shopid order by Visitors desc,shopid limit "+n+";",true,true) %>
<h2>Top <%=n %> wines more popular in last <%=history %> days</h2><%=(true?"":Webroutines.query2table("select knownwines.Wine, concat('https://www.vinopedia.com/wine/',knownwines.wine) as Url, sel.thecount as 'Number of clicks',Difference as 'Extra clicks', round((100*difference/thecount)) as Percentage from (select thisweek.knownwineid, thisweek.thecount as thecount, thisweek.thecount-if(lastweek.thecount is null,0,lastweek.thecount) as difference from (SELECT knownwineid,count(distinct(ip)) as thecount FROM logging where date > now() - interval "+history+" day and bot=0 group by knownwineid) thisweek left join (SELECT knownwineid,count(distinct(ip)) as thecount FROM logging use index (Date_bot) where date > now() - interval "+(history*2)+" day and date < now() - interval "+history+" day and bot=0 and knownwineid!=0 group by knownwineid) lastweek on (thisweek.knownwineid=lastweek.knownwineid) where thisweek.knownwineid>0 having difference > 0 order by difference desc limit "+n+") sel join knownwines on (sel.knownwineid=knownwines.id);",true,true))%>
<h2>Top <%=n %> wines clicked on in the last <%=history %> days</h2><%=(true?"":Webroutines.query2table("select shops.shopname as Shop, wines.name as Wine,wines.sourceurl as url,floor(wines.priceeuroex*100)/100 as Price, thecount as 'Number of clicks' from (select wineid,shopid,count(distinct(ip)) as thecount from logging where date > now() - interval "+history+" day and bot=0 and type='Link Clicked' group by wineid having wineid>0 order by thecount desc,wineid limit "+n+") sel join wines on (sel.wineid=wines.id) join shops on (sel.shopid=shops.id);",true,true))%>

</body>
</html>
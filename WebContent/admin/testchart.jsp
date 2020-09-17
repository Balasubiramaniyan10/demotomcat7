<html><body>
<%@ page   
	import = "com.freewinesearcher.common.datamining.Chart"	
	import = "com.freewinesearcher.common.datamining.Pricehistory"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.datamining.Pricehistory"
	%>
<%
String knownwineids=Dbutil.readValueFromDB("select group_concat(concat(knownwineid,',',vintage)) as knownwineids from (select knownwineid,vintage from ratedwines where knownwineid>0  and author='RP' and issuedate='2008-02-01 00:00:00' ) asd;","knownwineids");
String[] wine=knownwineids.split(",");

int knownwineid=0;
int vintage=0;
for (int i=0;(i<wine.length-1);i=i+2){
	knownwineid=Integer.parseInt(wine[i]);
	vintage=Integer.parseInt(wine[i+1]);
	Chart winechart=new Chart();
	winechart.xscale=390;
	winechart.yscale=300;
	winechart.title=Knownwines.getKnownWineName(knownwineid);
	winechart.createChart("2009-01-01","2009-10-01",knownwineid,vintage,Pricehistory.types.individual);
	if (winechart.hasdata){
	request.getSession().setAttribute("chart"+winechart.id,winechart);
%>
<img src='/images/chart/<%=winechart.id %>/<%=new java.sql.Timestamp(new java.util.Date().getTime()) %>'/>
<%
}
}
%></body></html>

<%@page import="com.freewinesearcher.common.Configuration"%>
<%@ page   
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "java.util.regex.Matcher"
	import = "java.util.regex.Pattern"
	
%>
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.net.*"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.batch.Spider"
	import = "java.util.ArrayList"
%>

<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.common.Knownwines"%><jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>

<%	PageHandler p=PageHandler.getInstance(request,response,"Pricequote");
	p.createWineset=false;
	
	p.searchdata.fuzzy=true;
	p.processSearchdata(request);
	
	p.s.wineset.s=p.searchdata;
	p.s.wineset.s.setSize("0.75");
	//p.s.wineset.knownwineid=p.s.wineset.guessKnownWineId();
	p.s.wineset.s.setNumberofrows(1);
	p.s.wineset.search();
	p.s.wineset.s.setNumberofrows(Configuration.numberofnormalrows);
	String result="";
	if (p.s.wineset.Wine!=null&&p.s.wineset.Wine.length>0){
		Double minimum=p.s.wineset.Wine[0].PriceEuroEx;
		//Double average=p.s.wineset.getMedianPriceEuroEx();
		Double average=Webroutines.getAveragePrice(p.s.wineset.bestknownwineid,p.searchdata.vintage);
		
		
	if (average>0){
		result=result+("document.write(\"");
		result=result+("Lowest price: "+Webroutines.formatPrice(minimum,minimum,p.searchdata.getCurrency(),"EX")+". ");
		result=result+("Average price: "+Webroutines.formatPrice(average,average,p.searchdata.getCurrency(),"EX")+". ");
		result=result+("<a href='https://www.vinopedia.com/wine/"+Webroutines.URLEncode(Webroutines.removeAccents(Knownwines.getUniqueKnownWineName(p.s.wineset.knownwineid)+" "+p.searchdata.vintage).trim())+"' target='_blank'>View all prices on Vinopedia</a>");
		result=result+("\");");
		}
	out.print(result);	
	p.logger.logaction();
	}
	
%>
<%@ page   
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.WineAdvice"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Shop"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Region"
	import = "com.freewinesearcher.common.Winerating"

	
	
%>
<%
	request.setCharacterEncoding("ISO-8859-1");
session = request.getSession(true);
%>

	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	// Handle source IP address
	
String country = Webroutines.filterUserInput(request.getParameter("country"));
if (country==null||country.equals("")) country="All";
String scale = Webroutines.filterUserInput(request.getParameter("scale"));
if (scale==null||scale.equals("")) scale="100";
String winetypecoding = "";
boolean alltypes=true;
if ("true".equals(Webroutines.filterUserInput(request.getParameter("red")))) {winetypecoding+=",1";} else {alltypes=false;}
if ("true".equals(Webroutines.filterUserInput(request.getParameter("white")))) {winetypecoding+=",3,7";} else {alltypes=false;}
if ("true".equals(Webroutines.filterUserInput(request.getParameter("rose")))) {winetypecoding+=",4";} else {alltypes=false;}
if ("true".equals(Webroutines.filterUserInput(request.getParameter("champagne")))) {winetypecoding+=",2,8,12";} else {alltypes=false;}
if ("true".equals(Webroutines.filterUserInput(request.getParameter("dessert")))) {winetypecoding+=",5,10,11";} else {alltypes=false;}
if ("true".equals(Webroutines.filterUserInput(request.getParameter("port")))) {winetypecoding+=",6,9";} else {alltypes=false;}
if (winetypecoding.length()>0) winetypecoding=winetypecoding.substring(1);
if (alltypes) winetypecoding="";
String currency = Webroutines.filterUserInput(request.getParameter("currency"));
if (currency==null) currency="EUR";
boolean subregions=true;
if (Webroutines.filterUserInput(request.getParameter("subregions"))!=null&&Webroutines.filterUserInput(request.getParameter("subregions")).equals("N")) subregions=false;
String pricemax = Webroutines.filterUserInput(request.getParameter("pricemax"));
if (pricemax==null) pricemax="";
String vintage = Webroutines.filterUserInput(request.getParameter("vintage"));
if (vintage==null) vintage="";
String searchtype = Webroutines.filterUserInput(request.getParameter("searchtype"));
boolean pqratio=true;
if (searchtype!=null&&searchtype.equals("rating")) pqratio=false;
vintage=Webroutines.filterVintage(vintage);
vintage=Webroutines.filterUserInput(vintage);
boolean dosearch=Boolean.parseBoolean(Webroutines.filterUserInput(request.getParameter("dosearch")));

String region = Webroutines.filterUserInput(request.getParameter("region"));
Dbutil.logger.info(region);
if (region==null) region="All";
ArrayList<String> regions=Region.getRegions("All");

String offset=Webroutines.filterUserInput(request.getParameter("offset"));
int offsetint=0;
if (offset==null||offset.equals("")) { 
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="priceminstring" value=""/> 
		<jsp:setProperty name="searchdata" property="pricemaxstring" value=""/> 
		<jsp:setProperty name="searchdata" property="*"/> 
		<jsp:setProperty name="searchdata" property="offset" value="0"/>
		
<%
			}else {
		%>
		<jsp:setProperty name="searchdata" property="*"/> 
		
		<%
 					}
 				 				try{
 				 					offsetint=Integer.parseInt(offset);
 				 				} catch (Exception e){}




 				 					WineAdvice advice=new WineAdvice();
 				 					advice.setOffset(offsetint);
 				 					advice.setCountryofseller(country);
 				 					advice.setCurrency(currency);
 				 					advice.setRegion(region);
 				 					try{
 				 						if (!pricemax.equals("")) advice.setPricemax(Float.parseFloat(pricemax));
 				 					} catch (Exception e){}
 				 					advice.setVintage(vintage);
 				 					//advice.setType("All");
 				 					advice.setOffset(offsetint);
 				 					advice.setWinetypecoding(winetypecoding);
 				 					//advice.setPqratio(pqratio);
 				 					advice.setSubregions(subregions);
 				 					advice.getAdvice();
 				 					advice.setScale(scale);
 				 					Dbutil.logger.info(region);	

 				 					//out.write(advice.getAdviceJSON().toString());
 				%>

	
	
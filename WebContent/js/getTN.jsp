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
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.batch.Spider"
	import = "java.util.regex.Matcher"
	import = "java.util.regex.Pattern"
	
%>

<%
	String knownwineidstr=Webroutines.filterUserInput(request.getParameter("id"));
String vintage=Webroutines.filterUserInput(request.getParameter("vintage"));
String tag=Webroutines.filterUserInput(request.getParameter("tag"));
if (tag==null||tag.equals("")) tag="tncontent";
int knownwineid=0;
	try{knownwineid=Integer.parseInt(knownwineidstr);}catch(Exception e){}
	
	String tn=Webroutines.getTastingNotesFromXML(knownwineid,vintage);
	out.print("document.getElementById('"+tag+"').innerHTML='"+tn.replace("'","\\'")+"';");
%>
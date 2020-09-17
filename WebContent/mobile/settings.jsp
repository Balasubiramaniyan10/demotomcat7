<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@page import="com.freewinesearcher.common.Knownwine"%>
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Configuration"
%><!DOCTYPE html> 
<html> 
	<head> 
	<%@ include file="/mobile/includes.jsp" %><%
	session = request.getSession(true); 
	
	PageHandler p=PageHandler.getInstance(request,response,"Mobile Settings");
	p.searchdata.sponsoredresults=true;
	p.searchpage="/m";
	p.searchdata.numberofrows=10;
	ArrayList<String>countries=Webroutines.getCountries();
 	%><title>
	Settings
</title>
	

</head> 
<body><div data-role="page" data-theme="a" class="vp" id="settings">
<%@ include file="/mobile/header.jsp" %>


<div data-role="content">
<h2>Settings</h2>
	<% 		
	out.print("<form id='settingsform' action='/m' method='post'><input type='hidden'name='keepdata' value='true'/><div data-role='fieldcontain'><label for='currency' class='select'>Currency:</label><select name='currency' id='currency' onchange='javascript:document.getElementById(\"settingsform\").submit();'>");
	out.print(Webroutines.currenciesOptions(p.searchdata.getCurrency()));
	out.print("</select></div><div data-role='fieldcontain'><label for='country' class='select'>Country:</label><select name='country' id='country' onchange='javascript:document.getElementById(\"settingsform\").submit();'>");
	out.print("<option value='AL'"+((p.searchdata.getCountry().equals("AL"))?(" selected=\"selected\""):"")+">All</option>");
	out.print("<option value='EU'"+((p.searchdata.getCountry().equals("EU"))?(" selected=\"selected\""):"")+">Europe</option>");
	for (int i=0;i<countries.size();i=i+2){
		out.print("<option value='"+countries.get(i)+"'"+((p.searchdata.getCountry().equals(countries.get(i)))?(" selected=\"selected\""):"")+">"+countries.get(i+1)+"</option>");
	}
	out.print("</select></form><br/><a href='/m' data-rel='back' data-role='button' data-inline='true' data-icon='back'>Back</a><a data-role='button' data-inline='true' data-theme='b' data-icon='arrow-r' rel='external' href='/?keepdata=true"+(session.getAttribute("sneakpreview")!=null?"&amp;sneakpreview=false":"")+"'>Full web site</a>");

	%>
</div><!-- /content -->


<%@ include file="/mobile/footer.jsp" %>
</div><!-- /page -->

</body>
</html>

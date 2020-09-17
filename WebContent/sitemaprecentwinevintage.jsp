<jsp:directive.page contentType="application/xml; charset=utf-8" /><?xml version="1.0" encoding="utf-8" ?>
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"

	
	
%><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<%=Webroutines.getSiteMapRecentVintages()%></urlset>
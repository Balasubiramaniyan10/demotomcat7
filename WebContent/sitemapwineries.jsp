<jsp:directive.page contentType="application/xml; charset=utf-8" /><?xml version="1.0" encoding="utf-8" ?>
<%@ page   
	import = "com.freewinesearcher.online.Webroutines"
%><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<%=Webroutines.getWinerySiteMap()%></urlset>
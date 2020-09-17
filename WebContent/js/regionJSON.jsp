<%@ page contentType="application/json; charset=UTF-8"  pageEncoding="UTF-8"%> 
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
		request.setCharacterEncoding("UTF-8");
		session = request.getSession(true); 

		String region= Webroutines.filterUserInput(request.getParameter("region"));
		if (region==null||region.equals("")) region="All";
		String json=Region.getRegionJSON(region).toString();
			
			out.write(json);
	%>

	
	
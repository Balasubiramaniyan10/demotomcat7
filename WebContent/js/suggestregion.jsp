
<%@page import="com.freewinesearcher.online.Guidedsearch"%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%><%@ page   
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%><%String input="";
	Guidedsearch gs=(Guidedsearch)session.getAttribute("guidedsearch");
	input=Webroutines.filterUserInput(request.getParameter("q"));
	if (input!=null){
	out.print(Webroutines.autoSuggestRegion(input.replaceAll("\\d",""),gs));
	}
	%>
	
<%request.setCharacterEncoding("UTF-8");%><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@page import="com.freewinesearcher.common.Dbutil"%><%@page import="com.freewinesearcher.online.PageHandler"%><%
PageHandler p=PageHandler.getInstance(request,response,"Search");
p.searchdata.sponsoredresults=true;
p.searchpage="/index.jsp";
p.createWineset=false;
p.processSearchdata(request);
//Dbutil.logger.info(request.getParameter("name"));
p.getLogger().type="Extension "+request.getParameter("type");
p.getLogger().logaction();
String newurl="/";
if (p.s.wineset.canonicallink!=null&&p.s.wineset.canonicallink.length()>0){
	newurl=p.s.wineset.canonicallink;
	if (p.searchdata.getVintage()!=null&&p.searchdata.getVintage().length()==4) newurl=newurl+"+"+p.searchdata.getVintage();
}
	response.setStatus(302);
	response.setHeader( "Location", newurl);
	response.setHeader( "Connection", "close" );
	return; 
%>
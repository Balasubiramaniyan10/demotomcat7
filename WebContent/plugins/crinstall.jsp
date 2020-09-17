<%@page import="com.freewinesearcher.online.PageHandler"%><%PageHandler p=PageHandler.getInstance(request,response,"CR Extension install");
p.getLogger().logaction();
response.setStatus(302);
response.setHeader( "Location", "/plugins/chrome.crx");
response.setHeader( "Connection", "close" );
return; 
%>
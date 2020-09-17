<%@page import="com.freewinesearcher.online.PageHandler"%><%PageHandler p=PageHandler.getInstance(request,response,"FF Extension install");
p.getLogger().logaction();
response.setStatus(302);
response.setHeader( "Location", "/plugins/vinopedia.xpi");
response.setHeader( "Connection", "close" );
return; 
%>
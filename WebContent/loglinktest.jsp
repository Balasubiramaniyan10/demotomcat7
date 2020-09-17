<html><head><meta name="robots" content="index,follow" /><% PageHandler p=PageHandler.getInstance(request,response,"linktest");
p.logger.logaction();
Dbutil.logger.info("hidden link clicked: "+p.getPageaction());
%>
<%@ include file="/header2.jsp" %></head><body>Human beings should not read this.</body></html>
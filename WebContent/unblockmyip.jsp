<%@page import="com.freewinesearcher.batch.Spider"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<% PageHandler p=PageHandler.getInstance(request,response,"Search");
	p.block=false;
	p.abuse=false;
	Dbutil.executeQuery("update ipblocks set status='Unblocked' where ipaddress='"+Spider.SQLEscape(p.ipaddress)+"';");
%><html><body>OK</body></html>
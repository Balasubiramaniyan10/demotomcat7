<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.Set"%>
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
%>
<% long start=System.currentTimeMillis();
	boolean debuglog=false;%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%
PageHandler p=PageHandler.getInstance(request,response);
p.processSearchdata(request);%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
	if (!searchdata.getName().equals("")) {
		out.print(Webroutines.escape(p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ", "")) + " "
				+ p.t.get("pricesbyfws"));
	} else {
		out.print("vinopedia");
	}
%></title>
<%
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/jsincludes.jsp" %>
<meta name="verify-v1" content="DPurn9ZNRpI1pXuOlIigNqJ6JoMePo97QY0m2L3eBrA=" />
</head>
<body onclick="javascript:emptySuggest();">
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	Ad rightad = new Ad("winered",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid,"");
	Ad betweenresults = new Ad("winered",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner+"");
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<% if (!p.s.search) {
	// No search%>
		<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%>
				</div>
		
			<div class='main'>
			<div id='mainleft'>	
			 
			<br/><br/><h1>Payment received</h1>
			<br/>Thank you, your payment of <%=request.getParameter("mc_currency") %> <%=request.getParameter("mc_gross") %> for <%=request.getParameter("transaction_subject") %> has been received correctly. <br/><br/>
			Kind regards,<br/><br/>
			vinopedia.com management
		<div class='main'><%@ include file="/snippets/footer.jsp" %></div>	</div>	</div>	
<%} 
} %> 
	
	

</body>
</html>
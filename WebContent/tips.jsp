<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
%>
<% long start=System.currentTimeMillis();%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%><jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/><jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%
PageHandler p=PageHandler.getInstance(request,response,"Pageload");
p.processSearchdata(request);%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
		out.print("Best Wine Deals: cheap and fine wines at a discount");
%></title>
<meta name="description" content="We show you online wine offers, discount wines and major price drops. Never miss a cheap wine deal again. Updated daily." />
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/jsincludes.jsp" %>
</head>
<body onclick="javascript:emptySuggest();">
<%@ include file="/snippets/topbar.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	Ad rightad = new Ad("winered",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid,"");
	Ad betweenresults = new Ad("winered",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner+"");
%>
<%@ include file="/snippets/logoandsearch.jsp" %>
	<div class='mainwide'>
	<%@ include file="/snippets/tips.jsp" %>
	</div>
	<div class='clear'></div>
	<div class='main'>
		<div id='adright'><%out.write(rightad.html);%></div>
		<div id='mainleft'>	
			<%=Webroutines.getConfigKey("systemmessage")%>
			<%=Webroutines.getWineTipsHTML(p.t,99999, PageHandler.getInstance(request,response).searchpage,p.searchdata)%>
		</div> <!--  mainleft--> 
	</div> <!--  main-->
	<div class='main'><%@ include file="/snippets/footer.jsp" %></div>	
<%} //block and abuse %>

</body>
</html>
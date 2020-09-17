<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.batch.XpathWineScraper"
import = "com.freewinesearcher.common.Wine"
import = "com.freewinesearcher.common.Wineset"
	import="com.freewinesearcher.common.Context"
	import="com.searchasaservice.parser.xpathparser.Record"
	import="com.searchasaservice.parser.xpathparser.Result"
	import="com.searchasaservice.parser.xpathparser.XpathParser"
	import="com.searchasaservice.parser.xpathparser.XpathParserConfig"
	import="com.searchasaservice.parser.xpathparser.Analyzer2"

%> 
<% long start=System.currentTimeMillis();
	boolean debuglog=false;%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start Pagehandler"); %>
<%Context c=(Context)session.getAttribute("context");
if (c==null){
	c=new Context(request);
	session.setAttribute("context",c);
}

PageHandler p=PageHandler.getInstance(request,response);
p.processSearchdata(request);
XpathWineScraper xpws=new XpathWineScraper(c.an.domainurl,c.an.url,null,c.an.shopid );
Wine[] winesfound=xpws.getWines(c.an.result,false); 
Wineset wineset =new Wineset();
wineset.Wine=winesfound;
wineset.records=winesfound.length;
Wineset sponsoredwineset=null;
p.s.wineset=wineset;
p.s.search=true;
%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
	if (!searchdata.getName().equals("")) {
		out.print(Webroutines.escape(searchdata.getName()) + " "
				+ p.t.get("pricesbyfws"));
	} else {
		out.print("vinopedia");
	}
%></title>
<%
	session.setAttribute("winename", searchdata.getName());
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/jsincludes.jsp" %>
</head>
<body onclick="javascript:emptySuggest();">
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	Ad rightad = new Ad("newdesign",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid,"");
	//Ad bottomleftad = new Ad("newdesign",187, 300, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner + "");			
	Ad betweenresults = new Ad("newdesign",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner+"");
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<% if (!p.s.search) {
	// No search%>
		<%@ include file="/snippets/bigsearchbar.jsp" %>
		<div id='adrightplaceholder'></div>
			<div class='main'>
			<div id='adright'><%out.write(rightad.html);%></div>
			<div id='mainleft'>	
				<%=Webroutines.getConfigKey("systemmessage")%>
				<%@ include file="/snippets/tips.jsp" %>
			</div> <!--  mainleft-->
		</div> <!--  main-->
		<%@ include file="/snippets/announcements.jsp" %>
		<%@ include file="/snippets/otherfeatures.jsp" %>
		<div class='main'><%@ include file="/snippets/footer.jsp" %></div>	
<%} else { 
		//Show search results%>
		<%@ include file="/snippets/logoandsearch.jsp" %>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start rating"); %>
		<% Webroutines.RatingInfo ri=Webroutines.getNewRatingsHTML(p.s.wineset.bestknownwineid, 1000, p.thispage,p.s.singlevintage,p.searchdata,p.t,false);
		out.print(ri.html);%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start refine"); %>
		<%@ include file="/snippets/refine.jsp" %>
		<div id='adbetween'><%out.write(betweenresults.html);%></div>
		<div class='main'>
			<div id='adright'><%out.write(rightad.html);%></div>
			<div id='mainleft'>	
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start results"); %>
				<%=Webroutines.getTabbedWineResultsHTML(p.s.wineset,p.t,searchdata,25,response,"false",false,p.thispage,0,null,true,false)%>
			<div class='pricenote'><%=p.t.get("pricenote")%></div>
			<div class='authornote'><%out.print(ri.authornote);%></div>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start footer"); %>
			<%@ include file="/snippets/footer.jsp" %>	
			</div>
		</div> <!--  main-->
<%} %>
<%} %> 
	
	

</body>
</html>
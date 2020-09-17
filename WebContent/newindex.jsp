<% long start=System.currentTimeMillis();
	boolean debuglog=false;
	%><%PageHandler p=PageHandler.getInstance(request,response);
	if ("sneak preview".equalsIgnoreCase(request.getParameter("name"))) {
		session.setAttribute("testmode",true);
		Dbutil.logger.info("testmode");
	}
	
if (p.ipaddress.startsWith("172.20.20.")||p.ipaddress.startsWith("127.0.0.1")) debuglog=true;
debuglog=false;
p.searchpage="/newindex.jsp";
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start processSearchdata"); 
p.searchdata.sponsoredresults=true;
p.processSearchdata(request); 
if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"End processSearchData"); 
String originaltesturl=(String)request.getAttribute("originalURL");


%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler" 
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Configuration"
%>

<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>


<%@page import="com.freewinesearcher.online.Hemabox"%>
<%@page import="com.freewinesearcher.online.RecommendationAd"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
	if (!p.searchdata.getName().equals("")&&p.s.wineset.records>0) {
		out.print((p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ", ""))+": "+p.s.wineset.records+" "+Webroutines.escape(p.t.get("pricescomparedon"))+" Vinopedia.com");
	} else {
		out.print("Vinopedia.com Wine Search Engine");
	}
%></title>
<%
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
%>
<% if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start header"); %>
<%@ include file="/header2.jsp" %>

<%
	
	String newloc=(PageHandler.getInstance(request,response).s.wineset.knownwineid>0?"/mwine/":"/m?name=")+Webroutines.URLEncode((PageHandler.getInstance(request,response).searchdata.getName()+" "+PageHandler.getInstance(request,response).searchdata.getVintage()).trim());
	if (newloc.endsWith("?name=")) newloc=newloc.replace("?name=","");
	if (p.firstrequest&&p.mobile){
		//Dbutil.logger.info("Redirecting to mobile page.");
	if (newloc!=null&&!newloc.equals("")){
		response.setStatus(302);
		response.setHeader( "Location", newloc);
		response.setHeader( "Connection", "close" );
		return;
	}
}
	%>

<meta name="verify-v1" content="DPurn9ZNRpI1pXuOlIigNqJ6JoMePo97QY0m2L3eBrA=" />
</head>
<body  onload="javascript:doonload();">
<% if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start ads"); %> 
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	 	String bannersshown="";
RecommendationAd ra=new RecommendationAd(p.s.wineset.bestknownwineid,p.searchdata.getLastcountry());
String rightadhtml=ra.getAd(p,"Winead");
if (rightadhtml.equals("")) {
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Got no recommendadtion ad, try again");
	Ad newad=new Ad("winered",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.bestknownwineid,"");
	bannersshown=newad.bannersshown;
	rightadhtml=newad.html;
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Got right ad");
} else {
	if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Got recommendation ad");
}%>
<% if (!p.s.search) {
	// No search%>
		<%@ include file="/snippets/bigsearchbar.jsp" %>
		<div class='textpage'>
				<%=Webroutines.getConfigKey("systemmessage")%>
				<%@ include file="/snippets/tips.jsp" %>
				<div class='hbox' id='hbox'>
				<% //Hemabox.setHtml();%>
				<%=Hemabox.getHtml(p)%>
				</div> <!-- hbox --> 
		<%@ include file="/snippets/footer.jsp" %>
		</div> <!--  textpage-->
		
	
<%} else { 
		//Show search results%>
		<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<% if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start rating"); 
		Webroutines.RatingInfo ri=null;
		if (p.s.singlevintage>0&&p.s.wineset.records>0){
			out.print(Webroutines.getWineInfo(p.s.wineset.bestknownwineid, p.s.singlevintage,p));
		} else {
			ri=Webroutines.getWave1RatingsHTML(p.s.wineset.bestknownwineid, 1000, p.searchpage,p.s.singlevintage,p.searchdata,p.t,request.isUserInRole("admin")); 
			out.print(ri.html);		}
%>

		
		<div class='main'>
			<div id='adright'><%out.write(rightadhtml);%></div>
			<div id='mainleft'>	
<% if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start results"); %>
				<%=Webroutines.getTabbedWineResultsHTML(p.s.wineset,p.t,p.searchdata,25,response,"true",true,p.searchpage,p.s.singlevintage,ri,false,request.isUserInRole("admin"))%>
			<div class='pricenote'><%=p.t.get("pricenote")%></div>
<% if (debuglog) Dbutil.logger.info((System.currentTimeMillis()-start)+" "+"Start footer"); %>
			<%@ include file="/snippets/footer.jsp" %>	
			</div></div> <%// workaround: IE positioning of footer %>
			</div><%if ("sneak preview".equalsIgnoreCase(p.searchdata.getName())) out.write("<br/><br/><h1>Test modus geactiveerd. </h1><h2>Om terug te gaan naar normale modus, sluit je browser helemaal af en keer terug naar Vinopedia.com</h2>Opmerkingen, fouten, etc. graag terugmelden aan <a style='text-decoration:underline;' href='mailto:feedback@vinopedia.com'>feedback@vinopedia.com</a>. <br/><br/>Bedankt, Jasper"); %>
		</div> <!--  main-->
<%} %>
<%} %> 
<script type='text/javascript'>$("#resulttabs").tabs("#gspanes  > div.pan", {tabs:'a.resulttab'});if($("#resulttabs").data("tabs").getCurrentTab().attr("href")=='#map') showmap();</script>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script> 
</body>
</html>
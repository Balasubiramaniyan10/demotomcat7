<%@page import="com.freewinesearcher.common.PerformanceLogger"%>
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<html>
<jsp:include page="/header2.jsp" />
<script type="text/javascript"> 
<!--
function customanalyze(winesep,fieldsep){
  		document.getElementById('winesep').value=winesep;
  		document.getElementById('fieldsep').value=fieldsep;
  		document.getElementById('actie').value='analyze';
		submitForm('<%=response.encodeURL("edittablescraper.jsp")%>');
		return 0;
}

function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
	}
	if (action == 'analyze') { 
  		document.getElementById('actie').value='analyze';
	}	
	if (action == 'showpage') { 
  		document.getElementById('actie').value='showpage';
	}	
	submitForm('<%=response.encodeURL("edittablescraper.jsp")%>');
	return 0;
}
function submitForm(actionPage) {
  
  document.formOne.action=actionPage;
  document.formOne.submit();
  return 0;
}
-->
</script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Webpage"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.TableScraper"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	
%>
<title>
The Page Analyzer
</title>
<%	
	PerformanceLogger pflog=new PerformanceLogger();
	long wsid=0;
	try {wsid = Integer.parseInt(request.getParameter("wsid"));}catch (Exception e){}
	String actie=request.getParameter("actie");
	String url = request.getParameter("url");
	String urlregex = request.getParameter("urlregex");
	String masterurl = request.getParameter("masterurl");
	String winesep = request.getParameter("winesep");
	String fieldsep = request.getParameter("fieldsep");
	String filter = request.getParameter("filter");
	String nameorder = request.getParameter("nameorder");
	String nameregex = request.getParameter("nameregex");
	String nameexclpattern = request.getParameter("nameexclpattern");
	String vintageregex = request.getParameter("vintageregex");
	String vintageorder = request.getParameter("vintageorder");
	String priceregex = request.getParameter("priceregex");
	String priceorder = request.getParameter("priceorder");
	String sizeregex = request.getParameter("sizeregex");
	String sizeorder = request.getParameter("sizeorder");
	String headerregex = request.getParameter("headerregex");
	String message = request.getParameter("message");
	String shopidstr = request.getParameter("shopid");
	String assumesize = request.getParameter("assumesize");
	if (assumesize==null) assumesize="false";
	if (shopidstr==null||shopidstr.equals("")) shopidstr="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	if (shopidstr.startsWith("auto")) {
		auto = "auto";
		shopidstr=shopidstr.substring(4);
	}
	if (shopidstr.startsWith("rating")) {
		auto = "";
		shopidstr=shopidstr.substring(6);
	}
	int shopid=0;
	try {shopid=Integer.parseInt(shopidstr);}catch(Exception e){}
	String rowid= request.getParameter("rowid");
	String getrow = request.getParameter("getrow");
	String tablescraper = request.getParameter("tablescraper");
	String postdata=request.getParameter("postdata");
	String AnalysisHTML="";
	String cookie=request.getParameter("cookie");
	if (cookie==null) cookie="";
	String countregex=request.getParameter("countregex");
	String counturl=request.getParameter("counturl");
	String countpostdata=request.getParameter("countpostdata");
	String countmultiplier=request.getParameter("countmultiplier");
	String encoding=request.getParameter("encoding");
	if (encoding==null) encoding="";
	
	
	String standardcookie=Dbutil.readValueFromDB("Select * from "+auto+"shops where id="+shopid+";","cookie");
	int shoptype=Dbutil.readIntValueFromDB("Select * from "+auto+"shops where id="+shopid+";","shoptype");
	pflog.log("init");
	if (actie!=null&&actie.equals("delete")){
		Webroutines.deleteRow(shopidstr,auto,getrow);
		pflog.log("delete row");
	}
	
	if (actie!=null&&(actie.equals("retrieve")||actie.equals("delete"))){
		rowid="0";
		getrow=Webroutines.retrieveRow(shopidstr,auto);
		pflog.log("retrieve scrapers");

	}
	if (shoptype==2) priceregex="no price";
	//if (encoding.equals("")) encoding="iso-8859-1";
	Spider spider=new Spider(shopidstr,encoding,auto,1);
	
	if ((getrow!=null)&&(!getrow.equals(""))){
		ArrayList<String> rowvalue=Webroutines.getTableScrapeRow(getrow,auto);
		headerregex=rowvalue.get(0);
		tablescraper=rowvalue.get(1);
		postdata=rowvalue.get(2);
		winesep=rowvalue.get(3);		
		fieldsep=rowvalue.get(4);
		filter=rowvalue.get(5);
		nameorder=rowvalue.get(6);
		nameregex=rowvalue.get(7);
		nameexclpattern=rowvalue.get(8);
		vintageorder=rowvalue.get(9);
		vintageregex=rowvalue.get(10);
		priceorder=rowvalue.get(11);
		priceregex=rowvalue.get(12);
		sizeorder=rowvalue.get(13);
		sizeregex=rowvalue.get(14);
		urlregex=rowvalue.get(15);
		assumesize=rowvalue.get(16);
		masterurl=rowvalue.get(17);
		url=rowvalue.get(18);
		counturl=rowvalue.get(19);
		countregex=rowvalue.get(20);
		countpostdata=rowvalue.get(21);
		countmultiplier=rowvalue.get(22);
		rowid=getrow;
		encoding=Dbutil.readValueFromDB("Select * from "+auto+"shops where id="+shopid+";","encoding");
		pflog.log("retrieve row");
	
	} else {
		getrow="0";
	}
	
	
	
	String Page="";
	if (url==null) url="";
	if (urlregex==null) urlregex="";
	if (actie==null) actie="";
	if (getrow==null) getrow="";
	if (winesep==null) winesep="";
	if (fieldsep==null) fieldsep="";
	if (filter==null) filter="";
	if (nameorder==null) nameorder="";
	if (nameregex==null) nameregex="";
	if (nameexclpattern==null) nameexclpattern="";
	if (vintageorder==null) vintageorder="";
	if (vintageregex==null) vintageregex="";
	if (priceorder==null) priceorder="";
	if (priceregex==null) priceregex="";
	if (sizeorder==null) sizeorder="";
	if (sizeregex==null) sizeregex="";
	if (masterurl==null) masterurl="";
	if (headerregex==null) headerregex="";
	if (message==null) message="";
	if (rowid==null) rowid="0";
	if (postdata==null) postdata="";
	if (countregex==null) countregex="";
	if (counturl==null) counturl="";
	if (countpostdata==null) countpostdata="";
	if (assumesize==null) assumesize="";
	if (countmultiplier==null||countmultiplier.equals("")) countmultiplier="1";
	pflog.log();
	if (!url.equals("")&&!actie.equals("retrieve")&&(!getrow.equals(""))) {
		if (masterurl.equals("Email")){
	Page=spider.getPageFromEmail(false);
	pflog.log("retrieve page from email");
	
		} else {
			if ((postdata.hashCode()+url).equals((String)session.getAttribute("url"))){
				Page=(String)session.getAttribute("Page");
			} else if (!actie.equals("retrieve")){
				session.removeAttribute("Page");
				session.removeAttribute("url");
	if (cookie.equals("")){
		Webpage webpage=new Webpage(Webpage.getBaseUrl(url),encoding,"",false,true);
		webpage.standardcookie=standardcookie;
		webpage.errorpause=1;
		webpage.maxattempts=1;
		webpage.readPage();
		cookie=webpage.getCookie();
	}
	if (encoding.equals("")) encoding=Spider.getHtmlEncoding(url);
	Webpage webpage=new Webpage(url,encoding,postdata,false,true);
	webpage.standardcookie=standardcookie;
	webpage.setCookie(cookie);
	webpage.errorpause=1;
	webpage.maxattempts=1;
	webpage.readPage();
	Page=webpage.html;
	cookie=webpage.getCookie();
	session.setAttribute("Page",Page);
	session.setAttribute("url",postdata.hashCode()+url);
	pflog.log("retrieve page from shop");
			}
			}
	}
	if (masterurl.equals("File")) url="File";
	
	if (actie!=null&&actie.equals("analyze")){
		try{
		ArrayList<String> Analysis = TableScraper.Analyzer(Page, url, winesep,fieldsep,vintageregex,priceregex,sizeregex);
		AnalysisHTML=Analysis.get(0);
		winesep=Analysis.get(1);		
		fieldsep=Analysis.get(2);
		nameorder=Analysis.get(3);
		sizeorder=Analysis.get(4);
		nameregex=Analysis.get(5);
		nameexclpattern=Analysis.get(6);
		vintageorder=Analysis.get(7);
		vintageregex=Analysis.get(8);
		priceorder=Analysis.get(9);
		priceregex=Analysis.get(10);
		} catch (Exception e){
			e.printStackTrace();
			message=e.getLocalizedMessage();
		}
		pflog.log("analyze");
		if (masterurl.equals("Email")){
	urlregex="(http://[^'\" >]*)";
		} else {
	urlregex="href=(?:['\"])?([^'\" >]*?[^'\" >]*?)['\" >]";
		}
	}
	
	String nameregexescaped= nameregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (nameregexescaped==null) nameregexescaped="";
	String urlregexescaped= urlregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (urlregexescaped==null) urlregexescaped="";
	String nameexclpatternescaped= nameexclpattern.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (nameexclpatternescaped==null) nameexclpatternescaped="";
	String vintageregexescaped= vintageregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (vintageregexescaped==null) vintageregexescaped="";
	String priceregexescaped= priceregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (priceregexescaped==null) priceregexescaped="";
	String headerregexescaped= headerregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (headerregexescaped==null) headerregexescaped="";
	String countregexescaped= Spider.replaceString(Spider.replaceString(Spider.replaceString(countregex.replaceAll("&","&amp;"),"<","&lt;"),">","&gt;"),"\"","&quot;");
	if (countregexescaped==null) countregexescaped="";
	ArrayList<String> shops = Webroutines.getShopList("");
	ArrayList<String> autoshops = Webroutines.getShopList("auto");
	if(url.equals("")) url=masterurl;
%>
</head>
<body><div class="textpage">
<jsp:include page="moderatorlinks.jsp" />

Shop comment:<form action='/moderator/setshopcomment.jsp' target='_blank'><input type='hidden' name='shopid' value='<%=shopid%>'/><input type='text' style='width:550px;' name='comment' value='<%=Dbutil.readValueFromDB("select * from shops where id="+shopid,"comment") %>'/><input type='submit' name='actie' value='update'/></form>
<font color='red'><%=message %></font><br/>
This page is used to analyze a web page on how to extract wines and test it.

<form name="formOne" action="<%=response.encodeURL("edittablescraper.jsp")%>" method="post"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" > 
<option value="0">New</option>
<%
	for (int i=0;i<shops.size();i=i+2){
%>
<option value="<%=shops.get(i)%>"<%if (shops.get(i).equals(shopid+"")) out.print(" selected='selected'");%>><%=(shops.get(i+1))%></option>
<%
	}
%>
<%
	for (int i=0;i<autoshops.size();i=i+2){
%>
<option value="auto<%=autoshops.get(i)%>"<%if ((autoshops.get(i).equals(String.valueOf(shopid)))&&(auto.equals("auto")) ) out.print(" selected='selected'");%>>(auto)<%=autoshops.get(i+1)%>
<%
	}
%>

</select></TD></TR>
<TR><TD>Wine Url</TD><TD><INPUT TYPE="TEXT" NAME="url" size="100" value="<%out.print(url);%>"></TD></TR>
<TR><TD>Post data (GET&... puts all GET params from URL in POST)</TD><TD><INPUT TYPE="TEXT" NAME="postdata" size="100" value="<%out.print(postdata);%>"></TD></TR>
<TR><TD>Master Url (@@@ separates more than 1)</TD><TD><INPUT TYPE="TEXT" NAME="masterurl" size="100" value="<%out.print(masterurl);%>"></TD></TR>
<%
	if (masterurl.equals("Email")){
	out.print("<TR><TD>Subject line:</TD><TD>Shop="+shopid+"&Code="+Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode()+"</TD></TR>");
}
%>
<TR><TD>Wine Separator</TD><TD><INPUT TYPE="TEXT" NAME="winesep" id="winesep" size="100" value="<%out.print(winesep.replaceAll("\"","&quot;"));%>"></TD></TR>
<TR><TD>Field Separator</TD><TD><INPUT TYPE="TEXT" NAME="fieldsep" id="fieldsep" size="100" value="<%out.print(fieldsep.replaceAll("\"","&quot;"));%>"></TD></TR>
<!-- <TR><TD>Filter</TD><TD><INPUT TYPE="TEXT" NAME="filter" size="100" value="<%out.print(filter);%>"></TD></TR>
<TR><TD>Name Regex</TD><TD><INPUT TYPE="TEXT" NAME="nameregex" size="100" value="<%out.print(nameregexescaped);%>"></TD></TR> -->
<TR><TD>Name Order (1;H;3 for 1, Header, 3)</TD><TD><INPUT TYPE="TEXT" NAME="nameorder" size="100" value="<%out.print(nameorder);%>"></TD></TR>
<TR><TD>Name field exclusion pattern</TD><TD><INPUT TYPE="TEXT" NAME="nameexclpattern" size="100" value="<%out.print(nameexclpatternescaped);%>"></TD></TR>
<TR><TD>Vintage Regex</TD><TD><INPUT TYPE="TEXT" NAME="vintageregex" size="100" value="<%out.print(vintageregexescaped);%>"></TD></TR>
<TR><TD>Vintage Order</TD><TD><INPUT TYPE="TEXT" NAME="vintageorder" size="100" value="<%out.print(vintageorder);%>"></TD></TR>
<TR><TD>Price Regex</TD><TD><INPUT TYPE="TEXT" NAME="priceregex" size="100" value="<%out.print(priceregexescaped);%>"></TD></TR>
<TR><TD>Price Order</TD><TD><INPUT TYPE="TEXT" NAME="priceorder" size="100" value="<%out.print(priceorder);%>"></TD></TR>
<TR><TD>Bottle Size Order</TD><TD><INPUT TYPE="TEXT" NAME="sizeorder" size="100" value="<%out.print(sizeorder);%>"></TD></TR>
<TR><TD>Assume 0.75 l when no size found</TD><TD><INPUT TYPE=CHECKBOX NAME="assumesize" value="true" checked="<%if (assumesize.equals("true")) out.print (" Checked");%>"></TD></TR>
<TR><TD>Url Regex</TD><TD><INPUT TYPE="TEXT" NAME="urlregex" size="100" value="<%out.print(urlregexescaped);%>"></TD></TR>
<TR><TD>Header Regex (H in name)</TD><TD><INPUT TYPE="TEXT" NAME="headerregex" size="100" value="<%out.print(headerregexescaped);%>"></TD></TR>
<!-- <TR><TD># Wines check URL</TD><TD><INPUT TYPE="TEXT" NAME="counturl" size="100" value="<%out.print(counturl);%>"></TD></TR>
<TR><TD># Wines check Regex</TD><TD><INPUT TYPE="TEXT" NAME="countregex" size="100" value="<%out.print(countregexescaped);%>"></TD></TR>
<TR><TD># Wines check post data</TD><TD><INPUT TYPE="TEXT" NAME="countpostdata" size="100" value="<%out.print(countpostdata);%>"></TD></TR>
<TR><TD># Wines check Multiplier</TD><TD><INPUT TYPE="TEXT" NAME="countmultiplier" size="100" value="<%out.print(countmultiplier);%>"></TD></TR> -->
<%Auditlogger al=new Auditlogger(request);
al.setAction("Edit scraper "+actie);
try{al.setShopid(shopid);}catch (Exception e){}
if (shopid>0){
	al.setObjectid(rowid);
	al.logaction();
}
	if (!rowid.equals("0")){ out.print("You are editing row "+rowid+"<br/>");}
%>
</TABLE> 
<input type="hidden" name="wsid" value="<%=wsid %>"/>   
	<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test">
	<INPUT TYPE="HIDDEN" NAME="rowid" value="<%=rowid%>">
	<INPUT TYPE="HIDDEN" NAME="cookie" value="<%=cookie%>">
	<INPUT TYPE="HIDDEN" NAME="encoding" value="<%=encoding%>">
	<input type="button" name="submitButton"
       value="Test" onclick="javascript:doit('test');">
    <input type="button" name="submitButton"
       value="Analyze this!" onclick="javascript:doit('analyze');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%=response.encodeURL("savetablescraper.jsp?saverowid="+rowid)%>');">
    <input type="button" name="submitButton"
       value="Save as New" onclick="javascript:submitForm('<%=response.encodeURL("savetablescraper.jsp")%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
    <input type="button" name="submitButton"
       value="Show page" onclick="javascript:doit('showpage');">
 
</form>


<%
	if (cookie!=null&&!cookie.equals("")) out.write("Cookie:"+standardcookie+cookie+"<br/>");
	if (actie!=null){
	if (actie.equals("test")||actie.equals("analyze")){
		if (url.equals("")){
	out.println("Enter values");} else { 
	if (winesep.equals("")){
		out.println("Enter values");} else {
		if (fieldsep.equals("")){
	out.println("Enter values");} else {
%>Search results:<table><%
	pflog.log();
	out.print ("Total wines according to site: "+Spider.checkShop(counturl, countregex,postdata, Integer.parseInt(countmultiplier))+"<br/>");
	try{
	Wine[] Winesfound = TableScraper.ScrapeWine(Page, shopid, url, urlregex, Dbutil.readValueFromDB("select * from shops where id="+shopid+";","shopurl"),spider.getBaseUrl(), headerregex, tablescraper, winesep, fieldsep, filter, nameorder, nameregex, nameexclpattern,
		vintageorder, vintageregex, priceorder, priceregex, sizeorder, sizeregex, null,1.0,1.0,TableScraper.getRatingScraper(shopid),false);
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	out.println("<tr><th>#</th><th>Wine name</th><th>Vintage</th><th>Size&nbsp;&nbsp;&nbsp;</th><th align='right'>Price</th><th>URL</th></tr>");
	for (int i=0;i<Winesfound.length;i++){
		out.println("<tr><td>" + (i+1)+"</td><td>" + Spider.escape(Winesfound[i].Name)+"</td>");
		out.println("<td> " + Winesfound[i].Vintage+"</td>");
		out.println("<td align='right'> " + Webroutines.formatSizePrecise(Winesfound[i].Size)+"</td>");
		out.println("<td align='right'> " + format.format(Winesfound[i].Price)+"</td>");
		out.println("<td><a href='" + Winesfound[i].SourceUrl+"' target='_blank'>"+ Winesfound[i].SourceUrl+"</a></td></tr>");
		if (Winesfound[i].Ratings.size()>0){
	for (int k=0;k<Winesfound[i].Ratings.size();k++){
		out.println("<tr><td>Rating " + Winesfound[i].Ratings.get(k).author+":</td>");
		out.println("<td> " + Winesfound[i].Ratings.get(k).ratinglow+"</td>");
		out.println("<td align='right'> " + Winesfound[i].Ratings.get(k).ratinghigh+"</td>");
		out.println("<td align='right'></td>");
		out.println("<td></td></tr>");
		
	}
		
		}
	}
	pflog.log("print results");
	}catch (java.util.regex.PatternSyntaxException e){
		out.write("<h2>There was a problem with the regex pattern:"+e.getLocalizedMessage()+"</h2>");
	}
		}
	}
%></table><%
		}
	}
	if (actie!=null&&(actie.equals("retrieve")||actie.equals("delete"))){
	out.print(Webroutines.getTableScrapeListHTML(shopidstr, response.encodeURL("edittablescraper.jsp"),auto));
	}
	if (actie.equals("showpage")){
		String Pageorig=Page;
		Page=Page.replaceAll("<","&lt;");
		Page=Page.replaceAll(">","&gt;");
		Page=Page.replaceAll("&lt;br&gt;","<br>");
		Page=Page.replaceAll("&lt;br/&gt;","<br/>");
		out.println(Page);
		out.println(Pageorig);
		
	}
	
	out.print(AnalysisHTML);
	
	}
	out.print("<br/><br/>"+new java.sql.Timestamp(new java.util.Date().getTime())+" "+pflog.getLog()+" ("+pflog.getTotaltime()+" ms. total)");
%></div>
</body> 
</html>
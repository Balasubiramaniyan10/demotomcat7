
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.online.Shopapplication"%><html>
<script type="text/javascript">
<!--
function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
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
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.TableScraper"
%>
<title>
The Regex for Wines Manipulator
</title>
<body>
<%	long wsid=0;
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
	boolean assumebottlesize=false;
	String assumesize = request.getParameter("assumesize");
	if (assumesize!=null&&assumesize.equals("true")) assumebottlesize=true;
	String headerregex = request.getParameter("headerregex");
	String message = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	String rowid = request.getParameter("saverowid");
	String tablescraper = request.getParameter("tablescraper");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	if (shopid.startsWith("rating")) {
		auto = "rating";
		shopid=shopid.substring(6);
	}
	String postdata= request.getParameter("postdata");
	String resultmessage="";
	String countregex=request.getParameter("countregex");
	String counturl=request.getParameter("counturl");
	String countpostdata=request.getParameter("countpostdata");
	String countmultiplier=request.getParameter("countmultiplier");
	
	if (url==null) url="";
	if (urlregex==null) urlregex="";
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
	if (rowid==null) rowid="0";
	if (postdata==null) postdata="";
	if (countregex==null) countregex="";
	if (counturl==null) counturl="";
	if (countpostdata==null) countpostdata="";
	if (countmultiplier==null) countmultiplier="";
	Spider spider=new Spider(shopid,"iso-8859",auto,1);
	
	
	nameregex = Spider.replaceString(nameregex, "\\", "\\\\");
	nameregex = Spider.replaceString(nameregex, "'", "\\'");
	winesep = Spider.replaceString(winesep, "\\", "\\\\");
	winesep = Spider.replaceString(winesep, "'", "\\'");
	fieldsep = Spider.replaceString(fieldsep, "\\", "\\\\");
	fieldsep = Spider.replaceString(fieldsep, "'", "\\'");
	urlregex = Spider.replaceString(urlregex, "\\", "\\\\");
	urlregex = Spider.replaceString(urlregex, "'", "\\'");
	vintageregex = Spider.replaceString(vintageregex, "\\", "\\\\");
	vintageregex = Spider.replaceString(vintageregex, "'", "\\'");
	priceregex = Spider.replaceString(priceregex, "\\", "\\\\");
	priceregex = Spider.replaceString(priceregex, "'", "\\'");
	sizeregex = Spider.replaceString(sizeregex, "\\", "\\\\");
	sizeregex = Spider.replaceString(sizeregex, "'", "\\'");
	headerregex = Spider.replaceString(headerregex, "\\", "\\\\");
	headerregex = Spider.replaceString(headerregex, "'", "\\'");
	nameexclpattern = Spider.replaceString(nameexclpattern, "\\", "\\\\");
	nameexclpattern = Spider.replaceString(nameexclpattern, "'", "\\'");
	countregex = Spider.replaceString(countregex, "\\", "\\\\");
	countregex = Spider.replaceString(countregex, "'", "\\'");
	
	
	boolean succes=false;

	try{
	
	if (masterurl.equals("")) {
		succes=TableScraper.addTableScrapeRow(shopid, rowid, "Fixed", url, headerregex, winesep, fieldsep, filter, nameorder, nameregex, nameexclpattern, vintageorder, vintageregex, priceorder, priceregex, sizeorder, sizeregex,urlregex,postdata,auto,assumebottlesize);
		if (succes) {
	succes=false;
	succes=TableScraper.addUpdateCheckShop(shopid, counturl, countregex, countpostdata, countmultiplier);
		}
	} else if (masterurl.equals("Email")){
		succes=TableScraper.addTableScrapeRow(shopid, rowid, "Email", masterurl, headerregex, winesep, fieldsep, filter, nameorder, nameregex, nameexclpattern, vintageorder, vintageregex, priceorder, priceregex, sizeorder, sizeregex,urlregex,postdata,auto, assumebottlesize);
		if (succes) {
	succes=false;
	succes=TableScraper.addUpdateCheckShop(shopid, counturl, countregex, countpostdata, countmultiplier);
		}
	}	 else  {
		succes=TableScraper.addTableScrapeRow(shopid, rowid, "Master", masterurl, headerregex, winesep, fieldsep, filter, nameorder, nameregex, nameexclpattern, vintageorder, vintageregex, priceorder, priceregex, sizeorder, sizeregex,urlregex,postdata,auto, assumebottlesize);
		if (succes) {
	succes=false;
	succes=TableScraper.addUpdateCheckShop(shopid, counturl, countregex, countpostdata, countmultiplier);
		}
	}			
	}catch (Exception e){
		e.printStackTrace( new java.io.PrintWriter(out));

	}
	if (succes==false){
%> 
		<H1>Oeps! Update didn't go as expected!!! Press the Back button and see what is wrong! Make sure all fields are filled.<br/>
		</H1>
		<%
	out.write(Dbutil.getErrorHTML(2));
%>
		<% } else { 
		resultmessage = "Wine URL and regex succesfully added for shop "+shopid;
		Auditlogger al=new Auditlogger(request);
		al.setAction("Save scraper");
		try{al.setShopid(Integer.parseInt(shopid));}catch (Exception e){}
		al.setObjectid(rowid);
		al.logaction();
		if (auto.equals("auto")){%>
				<jsp:forward page="viewautoshop.jsp">
				<jsp:param name="shopid"  value="<%=shopid%>" />
				<jsp:param name="auto" value="<%=auto%>" />
				<jsp:param name="message" value="<%=resultmessage%>" />
				</jsp:forward>
			<%} else if (wsid>0){ 
				Shopapplication sa=Shopapplication.retrieve(wsid);
				
			%>
			<jsp:forward page="editspiderregex.jsp">
			<jsp:param name="shopid"  value="<%=shopid%>" />
			<jsp:param name="masterurl" value="<%=masterurl%>" />
			<jsp:param name="message" value="<%=resultmessage%>" />
			</jsp:forward>
		<%}else { %>
				<jsp:forward page="editspiderregex.jsp?actie=retrieve">
				<jsp:param name="shopid"  value="<%=shopid%>" />
				<jsp:param name="masterurl" value="<%=masterurl%>" />
				<jsp:param name="message" value="<%=resultmessage%>" />
				</jsp:forward>
			<%}%>	
		
		<%}%>

</body> 
</html>
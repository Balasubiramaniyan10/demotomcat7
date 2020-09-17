<html>
<head>
<title>List of shops not yet present in Vinopedia</title>

<jsp:include page="/admin/adminlinks.jsp" />
<%@ page 
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%>
<% 	String url = request.getParameter("url");
String countrycode = request.getParameter("countrycode");
if (countrycode==null) countrycode="";
ArrayList<String> countries = Webroutines.getCountries();
String processpage="yes";
	if(url==null) url="";
	String postdata = request.getParameter("postdata");
	if (postdata==null) postdata="";
	String deletetablescraper = request.getParameter("deletetablescraper");
	String deleteautowebpages = request.getParameter("deleteautowebpages");
	String shopid= request.getParameter("shopid");
	if (shopid==null) shopid="";
	if (deletetablescraper==null) deletetablescraper="";
	if (deletetablescraper!=null&&deletetablescraper.equals("on")){
		Webroutines.deleteAutoTableScraper(shopid);
	}
	if (deleteautowebpages!=null&&deleteautowebpages.equals("on")){
		Webroutines.deleteAutoWebPages(shopid);
	}
	String wsshopid= request.getParameter("wsshopid");
	if (wsshopid==null) wsshopid="";
	
	// New shop, Loose url and postdata
	if (wsshopid.equals("")&&shopid.equals("")&&url!=null&&!url.equals("")){
		if (!url.startsWith("http://")) url="http://"+url;
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		wijnzoeker.autoScrapeSite(url,postdata);
		out.write("Spider started. <a href='javascript:window.close();'>Close Window</a>");
		processpage="no";
		%><script language="JavaScript" type="text/javascript">window.close();</script><%
		
	}
	
	// New shop, reference to wsshop
	if (!wsshopid.equals("")){
		processpage="no";
		Wijnzoeker wijnzoeker2=new Wijnzoeker();
			wijnzoeker2.autoScrapeNewWsShop(wsshopid);
			out.write("Spider started. <a href='javascript:window.close();'>Close Window</a>");
			processpage="no";
			%><script type="text/javascript"><!--  window.close();--></script><%
	}
	
	// Known shop, rescrape it possibly using new url or post data
	if (!shopid.equals("")){
		Wijnzoeker wijnzoeker3=new Wijnzoeker();
		wijnzoeker3.autoScrapeWsShop(shopid,url,postdata);
		out.write("Spider started. <a href='javascript:window.close();'>Close Window</a>");
		processpage="no";
		%><script type="text/javascript"><!--  window.close();--></script><%
	}
	if (processpage.equals("yes")){
%>
<br/>
<h2>Select country</h2>
<form action='shopstodo.jsp' method='post'>
<select name="countrycode" >
<option value="" >All</option>
<% for (int i=0;i<countries.size();i=i+2){%>
<option value="<%=countries.get(i)%>" <%if (countries.get(i).equals(countrycode)) out.write(" selected='selected'");  %>><%=countries.get(i+1)%></option>
<%}%>
</select><input type='submit' value ='Filter countries'/>
</form>

<FORM ACTION="<%= response.encodeURL("addautodiscovershop.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR>
<TD width="25%">Enter URL for shop:</TD>
<TD><INPUT TYPE="TEXT" NAME="url" value=""></TD></TR>
<TR>
<TD width="25%">Postdata:</TD>
<TD><INPUT TYPE="TEXT" NAME="postdata" value=""></TD></TR>
</TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" value="testshop">
    <input type="Submit" name="testshop"
       value="Analyze shop" >
</FORM>
<%out.write(Webroutines.getWsShopsHTML(countrycode)); 
}%>




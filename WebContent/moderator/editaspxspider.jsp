
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.regex.Matcher"%>
<html>
<script type="text/javascript">
<!--
function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
	}
	submitForm('<%=response.encodeURL("editaspxspider.jsp")%>');
	return 0;
}

function submitForm(actionPage) {
  document.getElementById('formOne').action=actionPage;
  document.getElementById('formOne').submit();
  return 0;
}
-->
</script>
<head>
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
	import = "com.freewinesearcher.common.Webpage"
	
	
%>
<title>
The Regex for Spidering URLs Manipulator
</title>
</head>
<body>
<div class="textpage">
<jsp:include page="moderatorlinks.jsp" />
<%
	Webpage webpage=new Webpage();
	String cookie=request.getParameter("cookie");
	if (cookie==null) cookie="";
	String masterurl = request.getParameter("masterurl");
	String regex = request.getParameter("regex");
	String filter = request.getParameter("filter");
	String message = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	String Page="";
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	if (shopid.startsWith("rating")) {
		auto = "";
		shopid=shopid.substring(6);
	}
	String actie = request.getParameter("actie");
	if (actie==null) actie="";
	String rowid = request.getParameter("rowid");
	String selectedurl=request.getParameter("selectedurl");
	String ignorepagenotfound=request.getParameter("ignorepagenotfound");
	String postdata=request.getParameter("postdata");
	String getrow = request.getParameter("getrow");
	
	if (selectedurl!=null&&!selectedurl.equals("")){
		selectedurl=Spider.replaceString(selectedurl,"?","\\?");
		if (selectedurl.startsWith("http:")){
	regex="href=(?:'|\")"+selectedurl.substring(0,selectedurl.indexOf("/",8));
	regex=regex+"("+selectedurl.substring(selectedurl.indexOf("/",8))+")(?:'|\")";
		} else {
	regex="href=(?:'|\")("+selectedurl+")(?:'|\")";
		}
	
	}
	if (actie!=null&&actie.equals("retrieve")){
		rowid="0";
		getrow=Webroutines.retrieveSpiderRow(shopid,auto);
		cookie="";
		}

	if ((getrow!=null)&&(!getrow.equals(""))){
		ArrayList<String> rowvalue=Webroutines.getSpiderRegexRow(getrow,auto);
		shopid=rowvalue.get(0);
		regex=rowvalue.get(1);
		filter=rowvalue.get(2);
		masterurl=rowvalue.get(3);
		postdata=rowvalue.get(4);
		ignorepagenotfound=rowvalue.get(5);
		cookie=rowvalue.get(6);
		rowid=getrow;
	} else {
		getrow="0";
	}
	
	
	if (rowid==null) rowid="";
	if (masterurl==null) masterurl="";
	if (regex==null) regex="";
	
	if (filter==null) filter="";
	if (message==null) message="";
	if (postdata==null) postdata="";
	if (ignorepagenotfound==null) ignorepagenotfound="";
	out.print(message+"<br/>");
	String filterescaped= filter.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	
	if (filterescaped==null) filterescaped="";
	ArrayList urls = new ArrayList();
	ArrayList shops = Webroutines.getShopList("");
	ArrayList autoshops = Webroutines.getShopList("auto");
	
	
	if (actie!=null&&(actie.equals("test")||actie.equals("showpage"))){
		
	webpage.setCookie(cookie);
	webpage.urlstring=masterurl;
	webpage.postdata=postdata;
	webpage.readPage();
	Page=webpage.html;
	cookie=webpage.getCookie();
	if (Page.contains("aspnetForm")&&regex.equals("")) regex="aspnetForm";
	}
	String regexescaped= regex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (Page.contains("'Page$")&&filterescaped.equals("")) filterescaped="Page:mandatory";
%>

This page is used to edit spider parameters for ASPX pages.
<%
	if (!rowid.equals("0")){ out.print("You are editing row "+rowid+"<br/>");}
%>
<FORM ACTION="<%=response.encodeURL("editaspxspider.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" >
<%
	for (int i=0;i<shops.size();i=i+2){
%>
<option value="<%=shops.get(i)%>"<%if ((shops.get(i).equals(shopid))&&(auto.equals("")) ) out.print(" Selected");%>><%=shops.get(i+1)%>
<%
	}
%>
<%
	for (int i=0;i<autoshops.size();i=i+2){
%>
<option value="auto<%=autoshops.get(i)%>"<%if ((autoshops.get(i).equals(shopid))&&(auto.equals("auto")) ) out.print(" Selected");%>><%=autoshops.get(i+1)%>
<%
	}
%>

</select></TD></TR><TR><TD>Master Url</TD><TD><INPUT TYPE="TEXT" NAME="masterurl" size="100" value="<%out.print(masterurl);%>"></TD></TR>
<TR><TD>Post data</TD><TD><INPUT TYPE="TEXT" NAME="postdata" size="100" value="<%out.print(postdata);%>"></TD></TR>
<TR><TD>Form name<% if (regex.equals("")){
		out.write("(Forms on page: ");
		Pattern pattern=Pattern.compile("form[^>]+id=[\"']([^'\"]+)['\"]"); 
		Matcher matcher=pattern.matcher(Page);
		int n=0;
		String regexfound="";
		while (matcher.find()){
			n++;
			regexfound=matcher.group(1);
			out.write(" "+matcher.group(1));
		}
		out.write(")");
		if (n==1) {
			regex=regexfound;
			regexescaped=regexfound;
		}
		
	}%></TD><TD><INPUT TYPE="TEXT" NAME="regex" size="100" value="<%out.print(regexescaped);%>"></TD></TR>
<TR><TD>Replace regex (separate with :, "colon"=: character)</TD><TD><INPUT TYPE="TEXT" NAME="filter" size="100" value="<%out.print(filterescaped);%>"></TD></TR>
<TR><TD colspan='2'>(mytext:b replaces mytext with b. To replace colon, use "colon" (mytext:colon). To make a string mandatory, use "mandatory" (Page:mandatory)</TD></TR>
<TR><TD>Ignore Pagenotfound</TD><TD><select name="ignorepagenotfound" >
<option value="N">No
<option value="Y" <%if (ignorepagenotfound.equalsIgnoreCase("Y")) out.write(" SELECTED ");%> >Yes
</select></TD></TR>
<INPUT TYPE="HIDDEN" NAME="cookie" value="<%=cookie%>"></TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test">
	<INPUT TYPE="hidden" NAME="spidertype" id="spidertype" value="aspx">
	<input type="HIDDEN" Name="rowid" value="<%=rowid%>">
    <input type="button" name="submitButton"
       value="Test" onclick="javascript:submitForm('<%=response.encodeURL("editaspxspider.jsp")%>');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%=response.encodeURL("savespiderregex.jsp?auto=")+auto%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
</FORM>
Specify a master URL to search for links to wines. Note; this URL will not be saved here, it is just for testing purposes.<br/>
Specify a regex that will match URLs in group 1 without the baseurl (so from the first /)<br/>
(Optionally) Specify the filter in the form regex1:string1:regex2: This will search for regex1 in the url and replace it with String 1.<br/>
Regex 2 will be deleted because the replacement is empty.
If the resulting URL contains the string "GET&", all data after "GET&" will be added to the POST parameters.<br/>

<%
	if (cookie!=null&&!cookie.equals("")) out.write("Cookie:"+cookie+"<br/>");
	
if (actie!=null&&actie.equals("retrieve")){
	out.print(Webroutines.getSpiderRegexListHTML(shopid, auto,response.encodeURL("editaspxspider.jsp")));
	
}
%>
<%
	if (actie.equals("showpage")){
		String Pageorig=Page;
		Page=Page.replaceAll("<","&lt;");
		Page=Page.replaceAll(">","&gt;");
		Page=Page.replaceAll("&lt;br&gt;","<br>");
		Page=Page.replaceAll("&lt;br/&gt;","<br/>");
		out.println(Page);
		out.println(Pageorig);
	}

	if (masterurl.equals("")){
		out.println("");} else { 
		if (regex.equals("")){
	out.println("");} else {
%><table><%
	int index=masterurl.indexOf("/",8);
	String parenturl=masterurl;
	String baseurl=masterurl;
	if (index>0){
		//parenturl=masterurl.substring(0,index);
		baseurl=masterurl.substring(0,index);
	}
	ArrayList<Spider.UrlSpider> s=new ArrayList<Spider.UrlSpider>();
	Spider.UrlSpider spider=new Spider.UrlSpider();
	spider.regex=(regex);
	spider.filter=(filter);
	spider.spidertype=("aspx");
	s.add(spider);
	urls = Spider.ScrapeASPXUrl(Page, s,parenturl, postdata, baseurl, shopid,"llkj");
	for (int i=0;i<urls.size();i=i+5){
		if (!urls.get(i+2).equals("")){
%>
				<form action="editaspxspider.jsp" target=_blank" method="post">
				<input type='hidden' name='actie' value='showpage'/>
				<input type='hidden' name='filter' value=<%=filter %>/>
				<input type='hidden' name='regex' value=<%=regex%>/>
				<input type='hidden' name='masterurl' value='<%=(String)urls.get(i) %>'/>
				<input type='hidden' name='postdata' value='<%=((String)urls.get(i+2)) %>'/>
				<input type='hidden' name='cookie' value='<%=(webpage.getCookie()) %>'/>
				<%
				out.println("<tr><td>url "+urls.get(i)+" postdata&nbsp;"+urls.get(i+2)+"<br/><input type='submit' value='test'></form></td></tr>");
				}	
			}
			%></table><%
			}
		}
	
%></div>
</body> 
</html>
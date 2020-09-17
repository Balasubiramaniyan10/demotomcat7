<html>
<script type="text/javascript">
<!--
function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
	}
	submitForm('<%=response.encodeURL("editspiderregex.jsp")%>');
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
	
	
%>
<title>
The Regex for Spidering URLs Manipulator
</title>
</head>
<body>
<jsp:include page="/admin/adminlinks.jsp" />
<%
	String masterurl = request.getParameter("masterurl");
	String regex = request.getParameter("regex");
	String filter = request.getParameter("filter");
	String message = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto="auto";
	String actie = request.getParameter("actie");
	String rowid = request.getParameter("rowid");
	String selectedurl=request.getParameter("selectedurl");
	String ignorepagenotfound=request.getParameter("ignorepagenotfound");
	System.out.println(selectedurl);
	String postdata=request.getParameter("postdata");
	Variables var=new Variables();
	String sessionid=request.getParameter("sessionid");
	var.Sessionid=sessionid;
	String getrow = request.getParameter("getrow");
	String cleanedurl="";
	
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
		getrow=Webroutines.retrieveSpiderRow(shopid,auto);
		}

	if ((getrow!=null)&&(!getrow.equals(""))){
		ArrayList<String> rowvalue=Webroutines.getSpiderRegexRow(getrow,auto);
		shopid=rowvalue.get(0);
		regex=rowvalue.get(1);
		filter=rowvalue.get(2);
		masterurl=rowvalue.get(3);
		postdata=rowvalue.get(4);
		ignorepagenotfound=rowvalue.get(5);
		rowid=getrow;
	} else {
		getrow="0";
	}
	
	
	if (rowid==null) rowid="";
	if (masterurl==null) masterurl="";
	if (regex==null) regex="";
	if (regex.equals("")) regex="href=(?:'|\")([^'\"]*?)(?:'|\")";
	if (filter==null) filter="";
	if (message==null) message="";
	if (postdata==null) postdata="";
	if (ignorepagenotfound==null) ignorepagenotfound="";
	out.print(message+"<br/>");
	String regexescaped= regex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	String filterescaped= filter.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (regexescaped==null) regexescaped="";
	if (filterescaped==null) filterescaped="";
	ArrayList<String> urls = new ArrayList<String>();
	ArrayList autoshops = Webroutines.getShopList("auto");
	urls=Webroutines.getUrls(shopid,"auto");
	String baseurl="";
	if (!shopid.equals("")) baseurl=Webroutines.getShopInfo(shopid,"auto").get(2);
	ArrayList<String> urlregex = new ArrayList<String>(0);
	urlregex.add(regex);
	urlregex.add(filter);
	urlregex.add("normal");
	
	ArrayList<String> scrapedurls;
%>

This page is used to add wine search regular expressions.
<%
	if (!rowid.equals("0")){ out.print("You are editing row "+rowid+"<br/>");}
%>
<FORM ACTION="<%=response.encodeURL("editspiderregex.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" >
<%
	for (int i=0;i<autoshops.size();i=i+2){
%>
<option value="<%=autoshops.get(i)%>"<%if ((autoshops.get(i).equals(shopid))&&(auto.equals("auto")) ) out.print(" Selected");%>><%=autoshops.get(i+1)%>
<%
	}
%>
</select></TD></TR>
<TR><TD>Regex</TD><TD><INPUT TYPE="TEXT" NAME="regex" size="100" value="<%out.print(regexescaped);%>"></TD></TR>
<TR><TD>Replace regex (separate with :)</TD><TD><INPUT TYPE="TEXT" NAME="filter" size="100" value="<%out.print(filterescaped);%>"></TD></TR>
<TR><TD>Ignore Pagenotfound</TD><TD><select name="ignorepagenotfound" >
<option value="N">No
<option value="Y" <%if (ignorepagenotfound.equalsIgnoreCase("Y")) out.write(" SELECTED ");%> >Yes
</select></TD></TR>
<INPUT TYPE="HIDDEN" NAME="sessionid" value="<%=var.Sessionid%>"></TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" value="test">
	<input type="HIDDEN" Name="rowid" value="<%=rowid%>">
    <input type="HIDDEN" Name="auto" value="auto">
    <input type="button" name="submitButton"
       value="Test" onclick="javascript:submitForm('<%=response.encodeURL("editautospiderregex.jsp")%>');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%=response.encodeURL("savespiderregex.jsp")%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
</FORM>
Specify a regex that will match URLs in group 1 without the baseurl (so from the first /)<br/>
(Optionally) Specify the filter in the form regex1:string1:regex2: This will search for regex1 in the url and replace it with String 1.<br/>
Regex 2 will be deleted because the replacement is empty.<br/>
<%
	if (actie!=null&&actie.equals("retrieve")){
	out.print(Webroutines.getSpiderRegexListHTML(shopid, "auto",response.encodeURL("editspiderregex.jsp")));
	}
%>
<table><%
	for (int i=0;i<urls.size();i=i+5){ 
		Boolean match=false;
		cleanedurl=urls.get(i);
		ArrayList<String> scrapedurl=Spider.ScrapeUrl(urls.get(i+1),urlregex,urls.get(i+1),baseurl,shopid,"","dwdwe");
		if (scrapedurl.size()>0) {
			match=true;
			cleanedurl=scrapedurl.get(0);
		}
%><tr><td><%if (urls.get(i+4).equals("Ready")) out.write("(");%> <%=urls.get(i+3)%><%if (urls.get(i+4).equals("Ready")) out.write(")");%></td><td><font color='<% if (match) {out.write("black");} else {out.write("red");};%>'><%=cleanedurl%></font></td><td><%=urls.get(i+1)%></td><td><%=urls.get(i+2)%></td></tr>
				<%}%>
				</table>
</body> 
</html>
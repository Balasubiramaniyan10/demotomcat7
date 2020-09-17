
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.common.Webpage"%><html>
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
<div class="textpage">
<jsp:include page="moderatorlinks.jsp" />
<%
	String masterurl = request.getParameter("masterurl");
	if (masterurl!=null&&masterurl.contains("@@@")){
		masterurl=masterurl.substring(0,masterurl.indexOf("@@@"));
	}

	String regex = request.getParameter("regex");
	String filter = request.getParameter("filter");
	String message = request.getParameter("message");
	String onelevel=request.getParameter("onelevel");
	if (!"on".equals(onelevel)) {onelevel="false";} else {onelevel="true";}
	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	String actie = request.getParameter("actie");
	String rowid = request.getParameter("rowid");
	String selectedurl=request.getParameter("selectedurl");
	String ignorepagenotfound=request.getParameter("ignorepagenotfound");
	String postdata=request.getParameter("postdata");
	Variables var=new Variables();
	String sessionid=request.getParameter("sessionid");
	var.Sessionid=sessionid;
	String getrow = request.getParameter("getrow");
	
	if (selectedurl!=null&&!selectedurl.equals("")){
		selectedurl=Spider.replaceString(selectedurl,"+","\\+");
		selectedurl=Spider.replaceString(selectedurl,"$","\\$");
		selectedurl=Spider.replaceString(selectedurl,"^","\\^");
		selectedurl=Spider.replaceString(selectedurl,"(","\\(");
		selectedurl=Spider.replaceString(selectedurl,")","\\)");
		selectedurl=Spider.replaceString(selectedurl,"|","\\|");
		selectedurl=Spider.replaceString(selectedurl,"{","\\{");
		selectedurl=Spider.replaceString(selectedurl,"}","\\}");
		selectedurl=selectedurl.replaceAll("\\d+","\\\\d+");
		selectedurl=Spider.replaceString(selectedurl,"?","\\?");
		selectedurl=Spider.replaceString(selectedurl,"*","\\*");
		selectedurl=selectedurl.replaceAll("&amp;","&");
		selectedurl=Spider.replaceString(selectedurl,"&","&(?:amp;)?");
		selectedurl=selectedurl.replaceAll("PHPSESSID=[^'\"&]*","PHPSESSID=[0123456789abcdefABCDEF]*");
		selectedurl=selectedurl.replaceAll("/MM=[^'\"&:?]+","/MM=[^'\"&:?]+");
		if (selectedurl.startsWith("http:")){
	regex="href=(?:'|\")"+selectedurl.substring(0,selectedurl.indexOf("/",8));
	regex=regex+"("+selectedurl.substring(selectedurl.indexOf("/",8))+")(?:'|\")";
		} else {
	regex="href=(?:'|\")("+selectedurl+")(?:'|\")"; 
		}
	
	}
	if (actie!=null&&actie.equals("delete")){
		Dbutil.executeQuery("delete from spiderregex where shopid="+shopid+" and id="+rowid+";");
		actie="retrieve";
		}

	if (actie!=null&&actie.equals("retrieve")){
		rowid="0";
		postdata="";
		regex="";
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
		onelevel=rowvalue.get(7);
		rowid=getrow;
	} else {
		getrow="0";
	}
	
	
	if (rowid==null) rowid="";
	if (masterurl==null) masterurl="";
	if (regex==null) regex="";
	if (regex.equals("")) regex="href=(?:'|\")([^'\"]*?[^'\"]*?)(?:'|\")";
	if (filter==null) filter="";
	if (message==null) message="";
	if (postdata==null) postdata="";
	if (ignorepagenotfound==null) ignorepagenotfound="";
	out.print(message+"<br/>");
	String regexescaped= regex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	String filterescaped= filter.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (regexescaped==null) regexescaped="";
	if (filterescaped==null) filterescaped="";
	ArrayList urls = new ArrayList();
	ArrayList shops = Webroutines.getShopList("");
	ArrayList autoshops = Webroutines.getShopList("auto");
	Auditlogger al=new Auditlogger(request);
	al.setAction("Edit scraper "+actie);
	try{al.setShopid(Integer.parseInt(shopid));}catch (Exception e){}
	al.setObjectid(rowid);
	al.logaction();
%>
<%=message %>
This page is used to add wine search regular expressions. <br/>
<% if (!rowid.equals("0")){ out.print("You are editing row "+rowid+"<br/>");}%>
Shop comment:<form action='/moderator/setshopcomment.jsp' target='_blank'><input type='hidden' name='shopid' value='<%=shopid%>'/><input type='text' style='width:550px;' name='comment' value='<%=Dbutil.readValueFromDB("select * from shops where id="+shopid,"comment") %>'/><input type='submit' name='actie' value='update'/></form><br/>
<FORM ACTION="<%= response.encodeURL("editspiderregex.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(shopid))&&(auto.equals("")) ) out.print(" Selected");%>><%=shops.get(i+1)%>
<%}%>
<% for (int i=0;i<autoshops.size();i=i+2){%>
<option value="auto<%=autoshops.get(i)%>"<% if ((autoshops.get(i).equals(shopid))&&(auto.equals("auto")) ) out.print(" Selected");%>><%=autoshops.get(i+1)%>
<%}%>
</select></TD></TR><TR><TD>Master Url</TD><TD><INPUT TYPE="TEXT" NAME="masterurl" size="100" value="<%out.print(masterurl);%>"></TD></TR>
<TR><TD>Post data</TD><TD><INPUT TYPE="TEXT" NAME="postdata" size="100" value="<%out.print(postdata);%>"></TD></TR>
<TR><TD>Regex</TD><TD><INPUT TYPE="TEXT" NAME="regex" size="100" value="<%out.print(regexescaped);%>"></TD></TR>
<TR><TD>Replace regex (separate with :)</TD><TD><INPUT TYPE="TEXT" NAME="filter" size="100" value="<%out.print(filterescaped);%>"></TD></TR>
<TR><TD>Ignore Pagenotfound</TD><TD><select name="ignorepagenotfound" >
<option value="N">No
<option value="Y" <% if (ignorepagenotfound.equalsIgnoreCase("Y")) out.write(" SELECTED ");%> >Yes
</select></TD></TR>
<TR><TD>Only one level deep</TD><TD><INPUT TYPE="checkbox" NAME="onelevel" <%=(onelevel.equals("true")?"checked='checked'":"") %>/></TD></TR>
<INPUT TYPE="HIDDEN" NAME="sessionid" value="<%=var.Sessionid%>"></TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test">
	<INPUT TYPE="hidden" NAME="spidertype" id="spidertype" value="normal">
	<input type="HIDDEN" Name="rowid" value="<%=rowid %>">
    <input type="button" name="submitButton"
       value="Test" onclick="javascript:submitForm('<%= response.encodeURL("editspiderregex.jsp")%>');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%= response.encodeURL("savespiderregex.jsp?auto=")+auto%>');">
    <input type="button" name="submitButton"
       value="Save and add another" onclick="javascript:submitForm('<%= response.encodeURL("savespiderregex.jsp?another=true&auto=")+auto%>');">
    <input type="button" name="submitButton"
       value="Save and Test" onclick="javascript:submitForm('<%= response.encodeURL("savespiderregex.jsp?starttest=true&auto=")+auto%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
</FORM>
Specify a master URL to search for links to wines. Note; this URL will not be saved here, it is just for testing purposes.<br/>
Specify a regex that will match URLs in group 1 without the baseurl (so from the first /)<br/>
(Optionally) Specify the filter in the form regex1:string1:regex2: This will search for regex1 in the url and replace it with String 1.<br/>
Regex 2 will be deleted because the replacement is empty.
If the resulting URL contains the string "GET&", all data after "GET&" will be added to the POST parameters.<br/>

<% 	if (actie!=null&&actie.equals("retrieve")){
	out.print(Webroutines.getSpiderRegexListHTML(shopid, auto,response.encodeURL("editspiderregex.jsp")));
	}
%>
<%	if (masterurl.equals("")){
		out.println("");} else { 
		if (regex.equals("")){
			out.println("");} else if (actie==null||!actie.equals("retrieve")){
				
			%><table><%
			String Page="";
			if (masterurl.equals((String)session.getAttribute("url"))){
				Page=(String)session.getAttribute("Page");
			} else {
				
				String cookie=request.getParameter("cookie");
				if (cookie==null) cookie="";
				session.removeAttribute("Page");
				session.removeAttribute("url");
				Webpage webpage=new Webpage(masterurl,null,postdata,false,true);
				webpage.setCookie(cookie);
				webpage.maxattempts=1;
				webpage.readPage();
				Page=webpage.html;
				cookie=webpage.getCookie();
				session.setAttribute("Page",Page);
				session.setAttribute("url",masterurl);
				
			}
			try{
			urls = Webroutines.ScrapeUrl(Page,regex,filter,var,postdata);
			for (int i=0;i<urls.size();i++){
				out.println("<tr><td><a href= \""+("editspiderregex.jsp?rowid="+rowid+"&selectedurl="+URLEncoder.encode(String.valueOf(urls.get(i)))+"&masterurl="+URLEncoder.encode(masterurl)+"&shopid="+shopid)+"&postdata="+URLEncoder.encode(postdata)+"\">"+urls.get(i)+"</a></td></tr>");
				}
			} catch (java.util.regex.PatternSyntaxException e){
				out.write("<h2>There was a problem with the regex pattern:"+e.getLocalizedMessage()+"</h2>");
			}
			%></table><%
			}
		}
	
%>
</div>
</body> 
</html>
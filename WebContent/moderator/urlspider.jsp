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
	import = "com.freewinesearcher.online.Auditlogger"
	import = "com.freewinesearcher.common.Context"
	import = "com.searchasaservice.configmgr.UrlSpider"
	
%>
<title>
Edit URL spidering
</title>
<% 	
 	PageHandler p=PageHandler.getInstance(request,response,"URL Spider");%>
<%@ include file="/header2.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
<%@ include file="/snippets/textpage.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<%
	UrlSpider usp=null;
	if (request.getAttribute("usp")!=null) usp=(UrlSpider)request.getAttribute("usp");
	String message = request.getParameter("message");
	String actie = request.getParameter("actie");
	if (actie!=null&&actie.equals("save")){
		message=usp.save();
		if (message.equals("OK")){
			String resultmessage = "URL spider regex for shop "+usp.getShopid()+" was saved successfully.";
			String autoshopid=usp.getAuto()+usp.getShopid();%>
			<% if (usp.getAuto().equals("auto")){%>
					<jsp:forward page="<%= response.encodeURL("viewautoshop.jsp")%>">
					<jsp:param name="shopid"  value="<%=usp.getShopid()%>" />
					<jsp:param name="message" value="<%=resultmessage%>" />
					</jsp:forward>
				<%} else { %>
					<jsp:forward page="<%= response.encodeURL("analyzer.jsp")%>">
					<jsp:param name="shopid"  value="<%=usp.getShopid()%>" />
					<jsp:param name="message" value="<%=resultmessage%>" />
					</jsp:forward>
				<%}	
			
		}
	}
	String masterurl = request.getParameter("masterurl");
	String regex = request.getParameter("regex");
	if (regex==null) regex="";
	String filter = request.getParameter("filter");
	if (filter==null) filter="";
	String shopidstr = request.getParameter("shopid");
	if (shopidstr==null||shopidstr.equals("")) shopidstr="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	if (shopidstr.startsWith("auto")) {
		auto = "auto";
		shopidstr=shopidstr.substring(4);
	}
	int shopid=0;
	try{shopid=Integer.parseInt(shopidstr);} catch (Exception e){}
	int rowid=0;
	try{rowid=Integer.parseInt(request.getParameter("rowid"));} catch (Exception e){}
	String selectedurl=request.getParameter("selectedurl");
	String ignorepagenotfound=request.getParameter("ignorepagenotfound");
	if (ignorepagenotfound==null) ignorepagenotfound="N";
	boolean ignore=false;
	if (ignorepagenotfound.equals("Y")) ignore=true;
	System.out.println(selectedurl);
	String postdata=request.getParameter("postdata");
	Variables var=new Variables();
	String sessionid=request.getParameter("sessionid");
	var.Sessionid=sessionid;
	String getrow = request.getParameter("getrow");
	ArrayList<UrlSpider> list=null;
	
	if (selectedurl!=null&&!selectedurl.equals("")){
		selectedurl=Spider.replaceString(selectedurl,"?","\\?");
		if (selectedurl.startsWith("http:")){
	regex="href=(?:'|\")"+selectedurl.substring(0,selectedurl.indexOf("/",8));
	regex=regex+"("+selectedurl.substring(selectedurl.indexOf("/",8))+")(?:'|\")";
		} else {
	regex="href=(?:'|\")("+selectedurl+")(?:'|\")"; 
		}
	
	}

	if (usp==null&&rowid>0) usp=new UrlSpider(shopid, new Context(request).tenant,rowid,auto);
	if (usp==null) usp=new UrlSpider(0,new Context(request).tenant,"","",auto);
	usp.setRegex(regex);
	usp.setUrl(masterurl);
	usp.setFilter(filter);
	usp.setIgnorepagenotfound(ignore);
	usp.setShopid(shopid);
	usp.setAuto(auto);
	if (actie!=null&&actie.equals("retrieve")){
		usp=null;
		list=UrlSpider.getUrlSpiders(shopid, new Context(request).tenant,auto);
		if (list.size()==1) usp=list.get(0);
	}
	if (usp==null&&shopid>0){
		list=UrlSpider.getUrlSpiders(shopid, new Context(request).tenant,auto);
		if (list.size()==1) usp=list.get(0);
	}
	if (usp==null) usp=new UrlSpider(0,new Context(request).tenant,"","",auto);

	if (usp.getUrl()==null) usp.setUrl("");
	if (message==null) message="";
	if (ignorepagenotfound==null) ignorepagenotfound="";
	out.print(message+"<br/>");
	String regexescaped= usp.getRegex().replaceAll("&","&amp;").replaceAll("\"","&quot;");
	String filterescaped= usp.getFilter().replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (regexescaped==null) regexescaped="";
	if (filterescaped==null) filterescaped="";
	ArrayList<String> urls = new ArrayList<String>();
	ArrayList<String> shops = Webroutines.getShopList("");
	ArrayList<String> autoshops = Webroutines.getShopList("auto");
%>

This page is used to add wine search regular expressions.
<% if (usp.getScrapelistrow()>0){ out.print("You are editing row "+usp.getScrapelistrow()+"<br/>");}%>
<FORM ACTION="<%= response.encodeURL("editspiderregex.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" >
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(usp.getShopid()))&&(auto.equals("")) ) out.print(" Selected");%>><%=shops.get(i+1)%>
<%}%>
<% for (int i=0;i<autoshops.size();i=i+2){%>
<option value="auto<%=autoshops.get(i)%>"<% if ((autoshops.get(i).equals(usp.getShopid()))&&(auto.equals("auto")) ) out.print(" Selected");%>><%=autoshops.get(i+1)%>
<%}%>
</select></TD></TR><TR><TD>Master Url</TD><TD><INPUT TYPE="TEXT" NAME="masterurl" size="100" value="<%out.print(usp.getUrl());%>"></TD></TR>
<TR><TD>Post data</TD><TD><INPUT TYPE="TEXT" NAME="postdata" size="100" value="<%out.print(usp.getPostdata());%>"></TD></TR>
<TR><TD>Regex</TD><TD><INPUT TYPE="TEXT" NAME="regex" size="100" value="<%out.print(regexescaped);%>"></TD></TR>
<TR><TD>Replace regex (separate with :)</TD><TD><INPUT TYPE="TEXT" NAME="filter" size="100" value="<%out.print(filterescaped);%>"></TD></TR>
<TR><TD>Ignore Pagenotfound</TD><TD><select name="ignorepagenotfound" >
<option value="N">No
<option value="Y" <% if (usp.isIgnorepagenotfound()) out.write(" SELECTED ");%> >Yes
</select></TD></TR>
<INPUT TYPE="HIDDEN" NAME="sessionid" value="<%=var.Sessionid%>"></TABLE>
	<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test">
	<INPUT TYPE="hidden" NAME="spidertype" id="spidertype" value="normal">
	<input type="HIDDEN" Name="rowid" value="<%=rowid %>">
    <input type="button" name="submitButton"
       value="Test" onclick="javascript:submitForm('<%= response.encodeURL("editspiderregex.jsp")%>');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%= response.encodeURL("editspiderregex.jsp?actie=save")%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
</FORM>
Specify a master URL to search for links to wines. <br/>
Specify a regex that will match URLs in group 1 without the baseurl (so from the first /)<br/>
(Optionally) Specify the filter in the form regex1:string1:regex2: This will search for regex1 in the url and replace it with String 1.<br/>
Regex 2 will be deleted because the replacement is empty.
If the resulting URL contains the string "GET&", all data after "GET&" will be added to the POST parameters.<br/>

<% 	if (actie!=null&&actie.equals("retrieve")&&list!=null&&list.size()>1){
		for (UrlSpider u:list){
			out.write("<a href='"+request.getServletPath()+"?shopid="+u.getShopid()+"&rowid="+u.getScrapelistrow()+"'>Edit</a> Url: "+u.getUrl()+", regex: "+u.getRegex());
		}
	}
%>
<%	if (usp.getUrl().equals("")){
		out.println("");
		} else { 
		if (usp.getRegex().equals("")){
			out.println("");} else {
			%><table><%
			try{
				urls = Webroutines.ScrapeUrl(Page,regex,filter,var,postdata);
				for (int i=0;i<urls.size();i++){
					out.println("<tr><td><a href= \""+("editspiderregex.jsp?rowid="+rowid+"&selectedurl="+URLEncoder.encode(String.valueOf(urls.get(i)))+"&masterurl="+URLEncoder.encode(masterurl)+"&shopid="+shopid)+"&postdata="+URLEncoder.encode(postdata)+"\">"+urls.get(i)+"</a></td></tr>");
					}
				} catch (java.util.regex.PatternSyntaxException e){
					out.write("There was a problem with the regex pattern:"+e.getLocalizedMessage());
				}
				%></table><%
		}
	}

	
} %>	
<%@ include file="/snippets/footer.jsp" %>
</body> 
</html>
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "com.freewinesearcher.batch.Spider"
%>
<%	String url = request.getParameter("url");
	String masterurl = request.getParameter("masterurl");
	String regex = request.getParameter("regex");
	regex = Spider.replaceString(regex, "\\", "\\\\");
	regex = Spider.replaceString(regex, "'", "\\'");
	String headerregex = request.getParameter("headerregex");
	if (headerregex==null) headerregex="";
	headerregex = Spider.replaceString(headerregex, "\\", "\\\\");
	headerregex = Spider.replaceString(headerregex, "'", "\\'");
	String order = request.getParameter("order");
	String resultmessage = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	String rowid= request.getParameter("saverowid");
	boolean succes=false;
	if (url==null) url="";
	if (masterurl==null) masterurl="";
	if (regex==null) regex="";
	if (order==null) order="";
	if (resultmessage==null) resultmessage="";
	if (rowid==null||rowid.equals("")) rowid="0";
	out.print(resultmessage+"<br/>");
	resultmessage="";
	Spider spider=new Spider(shopid,"","",auto);
	if (!url.equals("")) if (!regex.equals("")) if (!order.equals("")){
			if (masterurl.equals("")) {
				succes=spider.addUrl(spider.stripURL(url), regex, headerregex, "0",shopid, order, rowid, "Fixed","");
			} else  {
				succes=spider.addUrl(spider.stripURL(masterurl), regex, headerregex, "0", shopid, order, rowid, "Master","");
				
			}			
	}
	if (succes==false){%> 
		<H1>Oeps! Update didn't go as expected!!! Press the Back button and see what is wrong! Make sure all fields are filled.<br/>
		</H1>
		<% out.print(resultmessage);
			} else { 
		resultmessage = "Wine URL and regex succesfully added for shop "+shopid;
		%>
		<jsp:forward page="<%= response.encodeURL("editscrapelist.jsp")%>">
		<jsp:param name="shopid" value="<%=shopid%>" />
		<jsp:param name="message" value="<%=resultmessage%>" /> 
		</jsp:forward>
		<%}%>

</body> 
</html>
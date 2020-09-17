
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.common.Context"%><html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%
	String masterurl = request.getParameter("masterurl");
	String regex = request.getParameter("regex");
	regex = Spider.replaceString(regex, "\\", "\\\\");
	regex = Spider.replaceString(regex, "'", "\\'");
	String filter = request.getParameter("filter");
	filter = Spider.replaceString(filter, "\\", "\\\\");
	filter = Spider.replaceString(filter, "'", "\\'");
	String resultmessage = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	String spidertype=request.getParameter("spidertype");
	if (spidertype==null) spidertype="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	if (shopid.startsWith("rating")) {
		auto = "";
		shopid=shopid.substring(6);
	}String rowid = request.getParameter("rowid");
	String ignorepagenotfound=request.getParameter("ignorepagenotfound");
	String onelevel=request.getParameter("onelevel");
	if (!"on".equals(onelevel)) {onelevel="false";} else {onelevel="true";}
	
	if (rowid==null) rowid="";
	String succes="I did not update anything due to missing parameters";
	if (masterurl==null) masterurl="";
	if (regex==null) regex="";
	if (filter==null) filter="";
	if (resultmessage==null) resultmessage="";
	out.print(resultmessage+"<br/>");
	Spider spider=new Spider(shopid,"",auto,1);
	if (!regex.equals("")) {
		succes=spider.addSpiderRegex(regex, filter, rowid, ignorepagenotfound,spidertype,onelevel);
		}
	if (!succes.equals("")){
%> 
		<H1>Ooops! Update didn't go as expected:<br/>"+succes+"<br/> Press the Back button and see what is wrong!</H1>
		<% } else { 
			Auditlogger al2=new Auditlogger(request);
			if ("".equals(rowid)) {
				al2.setAction("Save new spider");
			} else {
				al2.setAction("Edit spider");
			}
			try{al2.setShopid(Integer.parseInt(shopid));}catch (Exception e){}
			try{al2.setObjectid(rowid);}catch (Exception e){}
			al2.logaction();
		resultmessage = "URL spider regex for shop "+shopid+" was saved successfully.";
		String autoshopid=auto+shopid;%>
		<% if (auto.equals("auto")){%>
				<jsp:forward page="viewautoshop.jsp">
				<jsp:param name="shopid"  value="<%=shopid%>" />
				<jsp:param name="message" value="<%=resultmessage%>" />
				</jsp:forward>
			<%} else { 
				if ("true".equals(request.getParameter("starttest"))){
					ThreadGroup threadgroup = new ThreadGroup("Scrape Shops");
					Spider s=new Spider(shopid, "","",1);
					s.c=new Context(1);
					Thread t=new Thread(threadgroup,s);
					t.start();
					response.sendRedirect("manage.jsp?shopid="+shopid);
					} else if ("true".equals(request.getParameter("another"))){
						response.sendRedirect("editspiderregex.jsp?shopid="+shopid);
						} else { %>
				<jsp:forward page="index.jsp">
				<jsp:param name="shopid"  value="<%=shopid%>" />
				<jsp:param name="message" value="<%=resultmessage%>" />
				</jsp:forward>
			<%}
			}%>	
		<%}%>

</body> 
</html>
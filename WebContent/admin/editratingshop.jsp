<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%
	String shopid = request.getParameter("shopid");
	String reason = request.getParameter("reason");
	if (reason==null) reason="";
	if(shopid==null) shopid="";
	String confirm = request.getParameter("confirm");
	ArrayList<String> currentconfig;
	ArrayList ratingshops = Webroutines.getShopList("rating");
%>
<title>
View ratingmatic discovery for shop <%=shopid%>
</title>
</head>
<body>
<jsp:include page="/admin/adminlinks.jsp" />

<%
	
%>
		<FORM ACTION="<%=response.encodeURL("editratingshop.jsp")%>" METHOD="POST"  id="formOne">
			<TABLE>
			<TR><TD width="25%">Select shop to view</TD><TD width="75%"><select name="shopid" >
			<%
				for (int i=0;i<ratingshops.size();i=i+2){
			%>
			<option value="<%=ratingshops.get(i)%>"<%if ((ratingshops.get(i).equals(shopid))) out.print(" Selected");%>><%=ratingshops.get(i+1)%>
			<%
				}
			%>
			</select></TD></TR>
			<TR><TD><INPUT TYPE="submit" NAME="retrieve" value="Retrieve"></TD><TD></TD></TR>
			</TABLE>
		</FORM>
		<%
			if (!shopid.equals("")) {
		%><br/><H3>View scraping of rating for shop <%=Webroutines.getShopNameFromShopId(Integer.parseInt(shopid),"rating")%></H3><br/>
		<a href='editratingshop.jsp?shopid=<%=shopid%>&confirm=Yes'>Save as a normal shop</a><br/>
		<FORM ACTION="editratingshop.jsp?shopid=<%=shopid%>&confirm=Yes" METHOD="POST"  id="formDelete">
			<TABLE><TR><TD><input type="Submit" name="testshop"  value="Delete shop" ></TD>
			<TD width="25%">Reason:</TD>
			<TD><INPUT TYPE="TEXT" NAME="reason" value=""></TD></TR></TABLE>
		</FORM>
		
			<%
						out.write("<br/>");
						
							ArrayList<String> urls = new ArrayList<String>();
							urls=Webroutines.getUrls(shopid);
							out.write("<a href='addshop.jsp?shopid=rating"+shopid+"&actie=retrieve'><H3>Shop data</H3></a><br/>");
							out.write("Shop name: ");
							if (currentconfig.get(5).contains("www")) {
							out.write("<font color='red'>"+Spider.escape(currentconfig.get(5))+"</font><br/>");
							} else {
						out.write("<font color='black'>"+Spider.escape(currentconfig.get(5))+"</font><br/>");
							}
							out.write("Country: ");
							if (currentconfig.get(6).equals("")) {
						out.write("<font color='red'>"+Spider.escape(currentconfig.get(6))+"</font><br/>");
							} else {
						out.write("<font color='black'>"+Spider.escape(currentconfig.get(6))+"</font><br/>");
							}
							out.write("Exvat: "+Spider.escape(currentconfig.get(7))+"<br/>");
							out.write("<a href='editratingspiderregex.jsp?shopid="+shopid+"&actie=retrieve'><H3>Spider regex</H3></a><br/>Regex: "+Spider.escape(currentconfig.get(4)));
					%>
		<table><%
		for (int i=0;i<urls.size();i=i+4){
			boolean match=false;
			if (urls.get(i+1).matches(".*?"+currentconfig.get(4)+".*?")) match=true;
			%><tr><td><font color='<% if (match) {out.write("black");} else {out.write("red");};%>'><%=urls.get(i)%></font></td><td><%=urls.get(i+1)%></td><td><%=urls.get(i+2)%></td><td><%=urls.get(i+3)%></td></tr>
			<%}%>
	</table><H3><a href='edittablescraper.jsp?shopid=rating<%=shopid%>&actie=retrieve'>Table scraper</a></H3><% 
		out.write(Webroutines.showWinesAbstract(shopid,"rating"));
		
	
	}

%>
</body> 
</html>
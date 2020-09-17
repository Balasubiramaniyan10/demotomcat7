<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/index.jsp");
    return;
  }
%>
<%@ page contentType="text/html; charset=UTF-8" %> 
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<script type="text/javascript">
<!--
function save(action) {
  if (action == 'new') { 
  	document.getElementById('savesearch').id.value="0";
  }
  document.getElementById('savesearch').submit();
  return 0;
}
-->
</script>

<%@ page 
	import = "java.text.*"
	import = "com.freewinesearcher.online.Search"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Searchset"
	import = "com.freewinesearcher.online.Webroutines"
%>

<%
	String status=Webroutines.filterUserInput(request.getParameter("status"));
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	NumberFormat nf = NumberFormat.getInstance();
	String country = "";
	String message="";
	String name="";
	String vintage="";
	String idstring="";
	String rareold="";
	String priceminstring="";
	String pricemaxstring="";
	String description="";
	String cheapest="false";
	float pricemin=0;
	float pricemax=0;
	int id=0;
	int getsearch=0;
	int deletesearch=0;
	ArrayList<String> countries = Webroutines.getCountries();
	String getsearchstring=Webroutines.filterUserInput(request.getParameter("getsearch"));
	
	if (status!=null&&!status.equals("")){
		Webroutines.changeStatus(Webroutines.getEmail(request.getRemoteUser()).split(":")[1],status);
	}
	
	if (getsearchstring==null) getsearchstring="";
	try {
		getsearch=Integer.valueOf(getsearchstring).intValue();
	} catch (Exception exc) {
	}
	
	String deletesearchstring=Webroutines.filterUserInput(request.getParameter("deletesearch"));
	if (deletesearchstring==null) deletesearchstring="";
	try {
		deletesearch=Integer.valueOf(deletesearchstring).intValue();
	} catch (Exception exc) {
	}
	if (deletesearch!=0){
		int result=Search.delete(deletesearch,request.getRemoteUser());
		if (result==1){
	message=message+"Search deleted. ";
	} else {
	message=message+"Could not delete the search. ";
	}
	}
	if (getsearch!=0){
		Search search= new Search(getsearch,request.getRemoteUser());
		if (search!=null){
	id=getsearch;
	description=search.description;
	country=search.country;
	name=search.name;
	vintage=search.vintage;
	pricemin=search.pricemin;
	pricemax=search.pricemax;
	if (pricemin==0){ priceminstring="";} else {priceminstring=format.format(pricemin);}
	if (pricemax==0){ pricemaxstring="";} else {pricemaxstring=format.format(pricemax);}
	rareold=search.rareold;
	cheapest=search.cheapest;
	
		}
	} else {
		if (Webroutines.filterUserInput(request.getParameter("name"))!=null) {
	description=Webroutines.filterUserInput(request.getParameter("description"));
	name=Webroutines.filterUserInput(request.getParameter("name"));
	country = Webroutines.filterUserInput(request.getParameter("country"));
	rareold=Webroutines.filterUserInput(request.getParameter("rareold"));
	vintage=Webroutines.filterUserInput(request.getParameter("vintage"));
	priceminstring=Webroutines.filterUserInput(request.getParameter("pricemin"));
	pricemaxstring=Webroutines.filterUserInput(request.getParameter("pricemax"));
	if (priceminstring==null) priceminstring="";
	if (pricemaxstring==null) pricemaxstring="";
	if (priceminstring=="") priceminstring="0";
	if (pricemaxstring=="") pricemaxstring="0";
	cheapest=Webroutines.filterUserInput(request.getParameter("cheapest"));
	if (cheapest==null) cheapest="false";
	if (message==null) message="";
	rareold=Webroutines.filterUserInput(request.getParameter("rareold"));
	if (rareold==null) rareold="false";
	if (country==null) country="all";
	idstring = Webroutines.filterUserInput(request.getParameter("id"));
	try {
		id=Integer.valueOf("0"+idstring).intValue();
	} catch (Exception exc) {
	}
	try {
		pricemin=nf.parse(priceminstring).floatValue();
		pricemax=nf.parse(pricemaxstring).floatValue();
		if (pricemin==0){ priceminstring="";} else {priceminstring=format.format(pricemin);}
		if (pricemax==0){ pricemaxstring="";} else {pricemaxstring=format.format(pricemax);}
	} catch (Exception exc) {
	exc.printStackTrace();
	message="Price range values are incorrect. Please enter a price like '1500,00'";
	}
	if (name==null) name="";
	if (description==null) description="";
	if (vintage==null) vintage="";
		}
	}
%>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>vinopedia price alerts</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Price alert index");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h4>PriceAlerts</h4>
On this page you can configure your PriceAlerts. You can configure one or more searches and we will automatically do the searching for you. There are two ways you can retrieve the results:
<ul><li>By RSS feed. For an explanation of RSS, see <a href='/showrssurl.jsp'> this page</a>. You can use the following URL in your RSS reader: <br/>
<B>https://www.vinopedia.com/rss.jsp?username=<%=request.getRemoteUser()%>&amp;code=<%=Webroutines.getUserCode(request.getRemoteUser())%></B><br/>
This way, you can configure your different searches easily and get the results back in one RSS-feed.</li>
<li>By email. Every night our wine database is refreshed. If new wines are added that match your criteria, you will receive an email from us. 
<%
	String email=Webroutines.getEmail(request.getRemoteUser());
	if (!email.split(":")[0].contains("active")) {
%>
			<font color='red'><br/>Before you can receive email PriceAlerts, we need to verify your email address (to prevent spam). We do this by sending you an email with a link that you have to click: this will activate your account. The email address that is registered for you is <%=email.split(":")[1]%>, click <a href='/sendactivationmail.jsp?username=<%=request.getRemoteUser()%>'>here</a> to send the activation mail.</font><br/>
	<%
		}
	%>
	</li></ul><%
		if (Webroutines.filterUserInput(request.getParameter("message"))!=null) message = message+Webroutines.filterUserInput(request.getParameter("message"));
		if (message!=null) out.print(message);
		out.print("<br/>");
		message="";
	%>
	
<TABLE width="90%">
<TR>
<TD width="50%" valign="top">
		<TABLE valign="top">
		<TR><TD><h4>PriceAlert Edit window</h4></TD></TR>
		<TR><TD>
			<FORM ACTION="<%=response.encodeURL("savesearch.jsp")%>" METHOD="POST" id="savesearch">
    		<TABLE>
    			<TR><TD>Description of this search</TD><TD><INPUT TYPE="TEXT" NAME="description" value="<%=description%>" size=50></TD></TR>
				<TR><TD>Name</TD><TD><INPUT TYPE="TEXT" NAME="name" value="<%=name%>" size=50></TD></TR>
				<TR><TD>Vintage</TD><TD><INPUT TYPE="TEXT" NAME="vintage" value="<%=vintage%>"></TD></TR>
				<TR><TD>Minimum price</TD><TD><INPUT TYPE="TEXT" NAME="pricemin" value="<%=priceminstring%>"></TD></TR>
				<TR><TD>Maximum price</TD><TD><INPUT TYPE="TEXT" NAME="pricemax" value="<%=pricemaxstring%>"></TD></TR>
				<TR><TD>Country of Seller</TD><TD><select name="country" >
				<option value="All"<%if (country.equals("All")) out.print(" selected");%>>All
				<%
					for (int i=0;i<countries.size();i=i+2){
				%>
				<option value="<%=countries.get(i)%>"<%if (country.equals(countries.get(i))) out.print(" selected");%>><%=countries.get(i+1)%>
				<%
					}
				%>
				</select></TD></TR>
				<TR><TD>New lowest price</TD><TD><INPUT TYPE="CHECKBOX" NAME="cheapest" value="true"<%if (cheapest.equals("true")) out.print("CHECKED");%>> Warns you if a new match is found that is cheaper than any previous match</TD></TR>
				<TR><TD>Show wines in the caregory</TD><TD><select name="rareold">
					<option value="false"<%if (rareold=="false") out.print(" selected");%>>All wines
					<option value="true"<%if (rareold=="true") out.print(" selected");%>>Rare and old wines
					</select></TD></TR>
					<INPUT type="hidden" name="id" value="<%=id%>"/>
					
				<TR><TD></TD><TD><INPUT TYPE="button" VALUE="Save as new PriceAlert"  onclick="save('new')">
					<%
						if (id>0) {
					%><INPUT TYPE="button" VALUE="Save changes to current PriceAlert"  onclick="save('existing')"><%
						}
					%></TD></TR>
			</TABLE>
			</FORM>
  		</TD></TR></TABLE>
  	</TD>
</TD>
<TD width="40%" valign="top">
	<%
		Searchset searchset= new Searchset(request.getRemoteUser());
	%>
		<TABLE width="90%">
		<TR><TD><h4>Email Pricealerts status</h4> <%
		if (email.split(":")[0].equalsIgnoreCase("active")){%>
		Your email PriceAlerts are active right now. Click <a href='/settings/index.jsp?status=inactive'>here</a> to deactivate your email PriceAlerts.
	<% } else if (email.split(":")[0].equalsIgnoreCase("inactive")){%>
		Your email PriceAlerts are inactive right now. Click <a href='/settings/index.jsp?status=active'>here</a> to activate your email PriceAlerts.
	<% } else {%>Your email address has not yet been activated. <br/><%}%>
	 
		</TR></TD></TABLE><TABLE>
<%	if (searchset!=null&&searchset.search.length>0){%>
		<TR><TD width="50%" valign="top"><h4>Existing PriceAlerts</TD></TR>
		<% for (int i=0;i<searchset.search.length;i++){
			out.print("<TR><TD>");
			if (searchset.search[i].id==id) out.print("<B>");
			out.print(searchset.search[i].description);
			if (searchset.search[i].id==id) out.print("</B>");
			out.print("</TD><TD><a href='"+response.encodeURL("index.jsp?getsearch="+searchset.search[i].id)+"'>Edit</a></TD>");
			out.print("<TD><a href='"+response.encodeURL("index.jsp?deletesearch="+searchset.search[i].id)+"'>Delete</a></TD></TR>");
		}%>

		</TABLE>
	</TD><%
	}%>



</TR>
</TABLE>
<br/><br/>You are logged in as user <%= request.getRemoteUser() %>. Click <a href="<%=response.encodeURL("index.jsp?logoff=true") %>">here</a> to logoff.<br/>

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
<br/><br/>



<h4>PriceAlerts</h4>
On this page you can configure your PriceAlerts. You can configure one or more searches and we will automatically do the searching for you. There are two ways you can retrieve the results:
<ul><li>By RSS feed. For an explanation of RSS, see <a href='/showrssurl.jsp'> this page</a>. You can use the following URL in your RSS reader: <br/>
<B>https://www.vinopedia.com/rss.jsp?username=<%=request.getRemoteUser()%>&amp;code=<%=Webroutines.getUserCode(request.getRemoteUser())%></B><br/>
This way, you can configure your different searches easily and get the results back in one RSS-feed.</li>
<li>By email. Every night our wine database is refreshed. If new wines are added that match your criteria, you will receive an email from us. 
<%
	String email=Webroutines.getEmail(request.getRemoteUser());
	if (!email.split(":")[0].contains("active")) {
%>
			<font color='red'><br/>Before you can receive email PriceAlerts, we need to verify your email address (to prevent spam). We do this by sending you an email with a link that you have to click: this will activate your account. The email address that is registered for you is <%=email.split(":")[1]%>, click <a href='/sendactivationmail.jsp?username=<%=request.getRemoteUser()%>'>here</a> to send the activation mail.</font><br/>
	<%
		}
	%>
	</li></ul><%
		if (Webroutines.filterUserInput(request.getParameter("message"))!=null) message = message+Webroutines.filterUserInput(request.getParameter("message"));
		if (message!=null) out.print(message);
		out.print("<br/>");
		message="";
	%>
	
<TABLE width="90%">
<TR>
<TD width="50%" valign="top">
		<TABLE valign="top">
		<TR><TD><h4>PriceAlert Edit window</h4></TD></TR>
		<TR><TD>
			<FORM ACTION="<%=response.encodeURL("savesearch.jsp")%>" METHOD="POST" id="savesearch">
    		<TABLE>
    			<TR><TD>Description of this search</TD><TD><INPUT TYPE="TEXT" NAME="description" value="<%=description%>" size=50></TD></TR>
				<TR><TD>Name</TD><TD><INPUT TYPE="TEXT" NAME="name" value="<%=name%>" size=50></TD></TR>
				<TR><TD>Vintage</TD><TD><INPUT TYPE="TEXT" NAME="vintage" value="<%=vintage%>"></TD></TR>
				<TR><TD>Minimum price</TD><TD><INPUT TYPE="TEXT" NAME="pricemin" value="<%=priceminstring%>"></TD></TR>
				<TR><TD>Maximum price</TD><TD><INPUT TYPE="TEXT" NAME="pricemax" value="<%=pricemaxstring%>"></TD></TR>
				<TR><TD>Country of Seller</TD><TD><select name="country" >
				<option value="All"<%if (country.equals("All")) out.print(" selected");%>>All
				<%
					for (int i=0;i<countries.size();i=i+2){
				%>
				<option value="<%=countries.get(i)%>"<%if (country.equals(countries.get(i))) out.print(" selected");%>><%=countries.get(i+1)%>
				<%
					}
				%>
				</select></TD></TR>
				<TR><TD>New lowest price</TD><TD><INPUT TYPE="CHECKBOX" NAME="cheapest" value="true"<%if (cheapest.equals("true")) out.print("CHECKED");%>> Warns you if a new match is found that is cheaper than any previous match</TD></TR>
				<TR><TD>Show wines in the caregory</TD><TD><select name="rareold">
					<option value="false"<%if (rareold=="false") out.print(" selected");%>>All wines
					<option value="true"<%if (rareold=="true") out.print(" selected");%>>Rare and old wines
					</select></TD></TR>
					<INPUT type="hidden" name="id" value="<%=id%>"/>
					
				<TR><TD></TD><TD><INPUT TYPE="button" VALUE="Save as new PriceAlert"  onclick="save('new')">
					<%
						if (id>0) {
					%><INPUT TYPE="button" VALUE="Save changes to current PriceAlert"  onclick="save('existing')"><%
						}
					%></TD></TR>
			</TABLE>
			</FORM>
  		</TD></TR></TABLE>
  	</TD>
</TD>
<TD width="40%" valign="top">
	<%
		Searchset searchset= new Searchset(request.getRemoteUser());
	%>
		<TABLE width="90%">
		<TR><TD><h4>Email Pricealerts status</h4> <%
		if (email.split(":")[0].equalsIgnoreCase("active")){%>
		Your email PriceAlerts are active right now. Click <a href='/settings/index.jsp?status=inactive'>here</a> to deactivate your email PriceAlerts.
	<% } else if (email.split(":")[0].equalsIgnoreCase("inactive")){%>
		Your email PriceAlerts are inactive right now. Click <a href='/settings/index.jsp?status=active'>here</a> to activate your email PriceAlerts.
	<% } else {%>Your email address has not yet been activated. <br/><%}%>
	 
		</TR></TD></TABLE><TABLE>
<%	if (searchset!=null&&searchset.search.length>0){%>
		<TR><TD width="50%" valign="top"><h4>Existing PriceAlerts</TD></TR>
		<% for (int i=0;i<searchset.search.length;i++){
			out.print("<TR><TD>");
			if (searchset.search[i].id==id) out.print("<B>");
			out.print(searchset.search[i].description);
			if (searchset.search[i].id==id) out.print("</B>");
			out.print("</TD><TD><a href='"+response.encodeURL("index.jsp?getsearch="+searchset.search[i].id)+"'>Edit</a></TD>");
			out.print("<TD><a href='"+response.encodeURL("index.jsp?deletesearch="+searchset.search[i].id)+"'>Delete</a></TD></TR>");
		}%>

		</TABLE>
	</TD><%
	}%>



</TR>
</TABLE>
<br/><br/>You are logged in as user <%= request.getRemoteUser() %>. Click <a href="<%=response.encodeURL("index.jsp?logoff=true") %>">here</a> to logoff.<br/>
<%} %>
</body>
</html>

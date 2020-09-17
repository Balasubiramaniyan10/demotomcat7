<html>
<script type="text/javascript">
<!--
function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
	}	
	submitForm('<%=response.encodeURL("editscrapelist.jsp")%>');
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
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.Webroutines"
%>
<title>
The Regex for Wines Manipulator
</title>
<%
	String actie=request.getParameter("actie");
	String url = request.getParameter("url");
	String masterurl = request.getParameter("masterurl");
	String regex = request.getParameter("regex");
	String headerregex = request.getParameter("headerregex");
	String order = request.getParameter("order");
	String message = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	String getrow = request.getParameter("getrow");
	if ((getrow!=null)&&(!getrow.equals(""))){
		ArrayList<String> rowvalue=Webroutines.getScrapeRow(getrow);
		url=rowvalue.get(0);
		regex=rowvalue.get(1);
		headerregex=rowvalue.get(2);
		order=rowvalue.get(3);
		masterurl=rowvalue.get(4);
	} else {
		getrow="0";
	}
	String rowid=getrow;
	String Page="";
	if (url==null) url="";
	if (masterurl==null) masterurl="";
	if (regex==null) regex="";
	if (headerregex==null) headerregex="";
	if (order==null) order="";
	if (message==null) message="";
	out.print(message+"<br/>");
	String regexescaped= regex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (regexescaped==null) regexescaped="";
	String headerregexescaped= headerregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (headerregexescaped==null) headerregexescaped="";
	ArrayList shops = Webroutines.getShopList("");	
	ArrayList autoshops = Webroutines.getShopList("auto");
%>
</head>
<body>
<jsp:include page="/admin/adminlinks.jsp" />
This page is used to add wine search regular expressions.
<FORM name="formOne" action="<%=response.encodeURL("editscraplist.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" default = "<%=shopid%>">
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
</select></TD></TR>
<TR><TD>Wine Url</TD><TD><INPUT TYPE="TEXT" NAME="url" size="100" value="<%out.print(url);%>"></TD></TR>
<TR><TD>Master Url</TD><TD><INPUT TYPE="TEXT" NAME="masterurl" size="100" value="<%out.print(masterurl);%>"></TD></TR>
<TR><TD>Regex</TD><TD><INPUT TYPE="TEXT" NAME="regex" size="100" value="<%out.print(regexescaped);%>"></TD></TR>
<TR><TD>Header Regex</TD><TD><INPUT TYPE="TEXT" NAME="headerregex" size="100" value="<%out.print(headerregexescaped);%>"></TD></TR>
<TR><TD>Order</TD><TD><INPUT TYPE="TEXT" NAME="order" size="100" value="<%out.print(order);%>"></TD></TR>
<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test"></TD></TR>
<INPUT TYPE="HIDDEN" NAME="rowid" value="rowid"></TD></TR>
<%
	if (!rowid.equals("0")){ out.print("You are editing row "+rowid+"<br/>");}
%>
</TABLE>
    <input type="button" name="submitButton"
       value="Test" onclick="javascript:doit('test');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%=response.encodeURL("savescrapelist.jsp?saverowid="+rowid)%>');">
    <input type="button" name="submitButton"
       value="Save as New" onclick="javascript:submitForm('<%=response.encodeURL("savescrapelist.jsp")%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
  </p>
</form>
  </CENTER>
</FORM>
In "Wine Url", specify the fixed URL or a URL that contains wine(s) in case of a spidered shop.<br/>
In "Master Url", specify the URL that contains a reference to the URLs to spider. If left empty, the wine URL is considered fixed.<br/>
Specify 3 or 4 matches, separated by "/". Group 1 is the name of the wine, group 2 is the vintage, group 3 is the price, group 4 (if specified) the bottle size.<br/>
A match can be built from more than one item. Separate them with ";". A number denotes a group, anything other than a number denotes a string. <br/>
For example: 2;, ;3/1/5 concatenates group 2 and 3 separated by the string ", " (Le Montrachet, Bernard Colin) for the wine<br/>
The vintage is group 1, the price is group 5.<br/>
If you want to test for just one group, try 1/0.5/0.5<br/>
If you specify a header regex, it will be repeatedly used for all wines found. Refer to is as 'H', for instance H; ;1/2/3. Only group 1 is caught in the header regex.<br/>
Useful regex's:<br/>
(?:[^>]*>){n}([^<]*)<  to skip n tags and grab a value <br/> 
>([^<]*?)(\d\d\d\d)?((?<=\d\d\d\d)[^<]*?)?< for catching >Name 2003 other text< <br/>
<tr[^<]*<td.*?(?<=>)\s*([^< ]{3}[^<]*)<.*?</td[^>]*>\s* for generic table scraping<br/>
<%
	if (actie!=null){
	if (actie.equals("test")){
		if (url.equals("")){
	out.println("Enter values");} else { 
	if (regex.equals("")){
		out.println("Enter values");} else {
		if (order.equals("")){
	out.println("Enter values");} else {
%>Search results:<table><%
	Page=Spider.getWebPage(request.getParameter("url"),null,null,null,null);
	Wine[] Winesfound = Wine.ScrapeWine(Page,request.getParameter("url"),request.getParameter("regex"), request.getParameter("headerregex"),0,request.getParameter("order"),null,1.0,1.0);
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	for (int i=0;i<Winesfound.length;i++){
		out.println("<tr><td>" + Winesfound[i].Name+"</td><td>");
		out.println("Vintage:" + Winesfound[i].Vintage+"</td><td>");
		out.println("Price:" + format.format(Winesfound[i].Price)+"</td></tr>");
	}
		}
	}
%></table><%
		}
	}
	if (actie.equals("retrieve")){
	out.print(Webroutines.getScrapeListHTML(shopid, response.encodeURL("editscrapelist.jsp")));
	
	}
	
	}
%>
</body> 
</html>
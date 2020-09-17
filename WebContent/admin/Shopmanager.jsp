<html>
<script type="text/javascript">
<!--
function submitForm(actionPage) {
  document.getElementById('formOne').action=actionPage;
  document.getElementById('formOne').submit();
  return 0;
}
-->
</script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
%>
<title>
The Regex Manipulator
</title>
<%	String url = request.getParameter("url");
	String regex = request.getParameter("regex");
	String order = request.getParameter("order");
	if (url==null) url="";
	if (regex==null) regex="";
	if (order==null) order="";
	String regexescaped= regex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	%>
</head>
<body>
<FORM ACTION="Shopmanager.jsp" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD>Url</TD><TD><INPUT TYPE="TEXT" NAME="url" size="300" value="<%out.print(url);%>"></TD></TR>
<TR><TD>Regex</TD><TD><INPUT TYPE="TEXT" NAME="regex" size="300" value="<%out.print(regexescaped);%>"></TD></TR>
<TR><TD>Order</TD><TD><INPUT TYPE="TEXT" NAME="order" size="300" value="<%out.print(order);%>"></TD></TR>
</TABLE>
    <input type="button" name="submitButton"
       value="Test" onclick="javascript:submitForm('Shopmanager.jsp');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('Saveshop.jsp');">
  </p>
</form>
  </CENTER>
</FORM>
Specify 3 matches, separated by "/". Group 1 is the name of the wine, group 2 is the vintage, group 3 is the price <br/>
A match can be built from more than one item. Separate them with ";". A number denotes a group, anything other than a number denotes a string. <br/>
For example: 2;, ;3/1/5 concatenates group 2 and 3 separated by the string ", " (Le Montrachet, Bernard Colin) for the wine<br/>
The vintage is group 1, the price is group 5.<br/>
If you want to test for just one group, try 1/0.5/0.5<br/>
<%	if (url.equals("")){
		out.println("");} else { 
		%><table><%
		Wine[] Winesfound = Wijnzoeker.ScrapeWine(request.getParameter("url"),request.getParameter("regex"),null,request.getParameter("order"));
		NumberFormat format  = new DecimalFormat("#,##0.00");	
		for (int i=0;i<Winesfound.length;i++){
				out.println("<tr><td>" + Winesfound[i].Name+"</td><td>");
				out.println("Vintage:" + Winesfound[i].Vintage+"</td><td>");
				out.println("Price:" + format.format(Winesfound[i].Price)+"</td></tr>");
		}
	%></table><%
	}
%>
</body> 
</html>
<%@page import="com.gargoylesoftware.htmlunit.html.HtmlElement"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ page 
	import="java.util.ArrayList" 
	import="java.util.List"
	import="java.util.Iterator" import="java.util.ListIterator"
	import="java.io.*" import="java.text.*" import="java.lang.*"
	import="java.sql.*" import="com.freewinesearcher.common.Wine"
	import="com.freewinesearcher.common.Wineset"
	import="com.freewinesearcher.common.Context"
	import="com.freewinesearcher.common.Webpage"
	import="com.freewinesearcher.common.Configuration"
	import="com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.batch.TableScraper"
	import="com.freewinesearcher.common.Variables"
	import="com.freewinesearcher.common.Dbutil"
	import="java.net.URL"
	import="com.searchasaservice.parser.xpathparser.XpathParser"

%><%
	Context c=(Context)session.getAttribute("context");
	if (c==null){
		c=new Context(request);
		session.setAttribute("context",c);
	}
	if (c.an==null||c.an.config==null) {
		out.write("<html><body><script type='text/javascript'>window.location='analyzer.jsp';</script></body></html>");
	} else {
		ArrayList<String> shops = Webroutines.getShopList("");
		
%>
<html><head>

<script type="text/javascript" src="http://www.google.com/jsapi?key=<%=Configuration.GoogleApiKey%>"></script>
<script type="text/javascript">
google.load("dojo", "1.5");
google.load("jquery", "1.4.2");
google.load("jqueryui", "1.8.5");
</script>
<script type="text/javascript" src="scraper.js">
</script>
<% if (c.an.taggeddocument.getFirstByXPath("//head")!=null) out.write(((HtmlElement)c.an.taggeddocument.getFirstByXPath("//head")).asXml());%>


<body>
<div id="dark"></div>
<div class="moveable" id='analysisplaceholder' style='background-color:#ffffff;text-align:left;position:fixed;top:200px;right:50px;width:250px;z-index:9;border-style:solid;border-width:2px;'>
<div id="handle" style='background-color:blue;color:white;font-family: Arial,sans-serif;font-size: 13px;' onmouseover='this.style.cursor="move"'>
Analysis (click to drag)</div>
<div id='analysis' ><table>
<% for (int i=1;i<c.an.config.size();i++){
	out.write("<tr><td><font id='fwsfont"+i+"' style='color:"+c.an.currentcolors[i]+"'>"+c.an.config.get(i).label+"</font></td><td onclick='javascript:editfield("+i+");' onmouseover=\"document.body.style.cursor='pointer'\" onmouseout=\"document.body.style.cursor='default'\" style=\"color:blue;text-decoration:underline\"><div class='fwseditfield'>Edit</div></td><td><div id='undoeditfield"+i+"'></div></td><td><div id='saveeditfield"+i+"'></div></td><tr>");
}
Dbutil.logger.info("appendwineryname="+c.an.config.appendwineryname);
out.write("<tr><td>Append winery name</td><td><input type='checkbox' "+(c.an.config.appendwineryname?" checked='checked' ":"")+" onclick='setAppendWineryField(this.checked);'/></td><tr>");
out.write("<tr><td>Detect next page</td><td onclick='javascript:editfield(100);' onmouseover=\"document.body.style.cursor='pointer'\" onmouseout=\"document.body.style.cursor='default'\" style=\"color:blue;text-decoration:underline\"><div class='fwseditfield'>Edit</div></td><td><div id='undoeditfield100'></div></td><td><div id='saveeditfield100'></div></td><tr>");%>
</table><table>
<tr><td onclick='javascript:savexp();' onmouseover="document.body.style.cursor='pointer'" onmouseout="document.body.style.cursor='default'" style="color:blue;text-decoration:underline"><div class='fwseditfield' id='fwssave'>Save configuration</div></td></tr>

</table>
</div>
</div>	
<div id='targetdocument'>
<% if (c.an.taggeddocument.getByXPath("//body")!=null) {
	out.write(((HtmlElement)c.an.taggeddocument.getByXPath("//body").get(0)).asXml());
} else {
	out.write(c.an.taggeddocument.asXml());
}%>
</div>
<script type="text/javascript">
makeDrag();
refreshColors();
</script>
</body></html>
<%}%>
<%@ page 
	import="java.util.ArrayList" 
	import="java.util.List"
	import="java.util.Iterator" import="java.util.ListIterator"
	import="java.io.*" import="java.text.*" import="java.lang.*"
	import="java.sql.*" import="com.freewinesearcher.common.Wine"
	import="com.freewinesearcher.common.Wineset"
	import="com.freewinesearcher.common.Context"
	import="com.freewinesearcher.common.Webpage"
	import="com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.batch.TableScraper"
	import="com.freewinesearcher.common.Variables"
	import="com.freewinesearcher.common.Dbutil"
	import="java.net.URL"
	import="com.searchasaservice.parser.xpathparser.XpathParser"
	import="com.searchasaservice.parser.xpathparser.Analyzer"
	import="com.searchasaservice.parser.HTMLParser"
%><html>
<head>
<script type="text/javascript" src="/js/Dojo/dojo/dojo.js"
	djconfig="parseOnLoad: true">
</script>
<script type="text/javascript" src="scraper.js">
</script>
<script type="text/javascript">
function submitForm(actie,form) {

 form.actie.value=actie;
  form.submit();
  return 0;
}
</script>
<title>URL Analyzer</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<% PageHandler p=PageHandler.getInstance(request,response,"Price alert index");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<%	Context c=(Context)session.getAttribute("context");
	if (c==null){
		c=new Context(request);
		session.setAttribute("context",c);
	}
	c.an=null;
	String message="";
	String url = request.getParameter("url");
	String actie = request.getParameter("actie");
	if(actie==null) actie="";
	String postdata = request.getParameter("postdata");
	String shopidstr = request.getParameter("shopid");
	if (shopidstr==null) shopidstr="";
	String auto = request.getParameter("auto");
	if (auto == null)
		auto = "";
	if (shopidstr.startsWith("auto")) {
		auto = "auto";
		shopidstr = shopidstr.substring(4);
	}
	int shopid=0;
	try{
		shopid=Integer.parseInt(shopidstr);
	}catch(Exception e){}
	int row=0;
	try{
		row=Integer.parseInt(request.getParameter("row"));
	}catch(Exception e){}
	if (url == null)
		url = "";
	if (actie != null && actie.equals("analyze")) {
		
		URL u=null;
		try{
			u=new URL(url);
		}catch(Exception e){}
		if (u==null) {
			message+="Invalid url, please correct it.<br/>";
			%>
			//<script type="text/javascript">
			//greytext="Please wait while the page is being analyzed.<br/> This may take a minute.";
			//grayOut(false);
			//</script><%
		} else {
				actie="doanalyze";
		}
	}
%>


This page is used to analyze a new web page on how to extract data.<br/>
<%=message %>
<%if (!actie.equals("doanalyze")){ %>
<form name="formOne" action='<%=response.encodeURL("quickanalyzer.jsp")%>'
	method="post" id="formOne">
<table>
	
	<tr>
		<td>Url</td>
		<td><input type="TEXT" name="url" size="100"
			value="<%out.print(url);%>"></td>
	</tr>
	
</table>

<input type="HIDDEN" name="actie" id="actie"	value="unknown">
<input type="button" name="submitButton" value="Start Analyzer" onClick="javascript:submitForm('analyze',document.formOne)">
</form>

<% }
if (actie!=null&&actie.equals("retrieve")&&shopid>0&&c.an==null){
	if (shopid>0){
		int n=XpathParser.getXpathParserIds(c,shopid).size();
		if (n>1){
		out.write("More than one analyzers were found. Please select one:<br/>");
		for (int i:XpathParser.getXpathParserIds(c,shopid)){
			n=i;
			out.write("<a href='analyzer.jsp?actie=retrieve&shopid="+shopid+"&row="+i+"'>Edit row "+i+"</a><br/>");
		}
		}
		if(n==0) out.write("No records found for this entry");
	}
	}
%>
<%
	if (actie.equals("doanalyze")){
		
	%><H1>Please wait while the page is being analyzed. This may take a minute.</H1><br/>
	
	<script type="text/javascript">
	//console.error("Starting greyout");
	//greytext="Please wait while the page is being analyzed.<br/> This may take a minute.";
	//grayOut(true);
	document.onload=analyzetimed();
	</script>
	
	<%}
	}%>

</body>
</html>

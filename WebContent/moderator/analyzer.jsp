<%@page import="com.vinopedia.htmlunit.HtmlUnitParser"%>
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
	import="com.searchasaservice.parser.xpathparser.Record"
	import="com.searchasaservice.parser.xpathparser.Result"
	import="com.searchasaservice.parser.xpathparser.XpathParser"
	import="com.searchasaservice.parser.xpathparser.XpathParserConfig"
	import="com.searchasaservice.parser.xpathparser.Analyzer2"

%><html>
<head>
<title>URL Analyzer</title>
<% PageHandler p=PageHandler.getInstance(request,response,"Price alert index");%>
<%@ include file="/header2.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
<%@ include file="/snippets/textpage.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head> 
<body>
<div id="dark"></div>
<jsp:include page="moderatorlinks.jsp" />
<script type="text/javascript" src="http://www.google.com/jsapi?key=<%=Configuration.GoogleApiKey%>"></script>
<script type="text/javascript">
google.load("dojo", "1.5");
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
<%	Analyzer2 an=null;
	Context c=(Context)session.getAttribute("context");
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
	if (url == null) url = "";
	if (url.equals("")&&shopid>0) url=Dbutil.readValueFromDB("select * from scrapelist where shopid="+shopid,"url");
	if (postdata == null)
		postdata = "";
	ArrayList shops = Webroutines.getShopList("");
	ArrayList autoshops = Webroutines.getShopList("auto");
	if (shopid>0){
		if (url.equals("")) url=Webroutines.getMostCommonUrl(shopid,auto);
	}
	if (actie != null && actie.equals("retrieve")) {
		if (shopid>0){
		c.an = Analyzer2.getAnalyzer(c,shopid,row); 
		if (url.equals("")) {
			url=Webroutines.getMostCommonUrl(shopid,auto);
			postdata=Webroutines.getMostCommonPostdata(shopid,auto);
		}
		URL u=null;
		try{
			u=new URL(url);
		}catch(Exception e){}
		if (u==null) {
			message+="Invalid url, please correct it.<br/>";
		} else {
		if (c.an!=null&&!"".equals(url)) {
			c.an.url=url;
			c.an.postdata=postdata;
			%>
			<script type="text/javascript">
			greytext="Please wait, retrieving information on the page";
			dograyOut(true);
			</script><%
			c.an.getPage();
			c.an.document=c.an.originaldocument;
			c.an.tagDocument(c.an.document);
			c.an.document=c.an.taggeddocument;
			c.an.workdocument=c.an.taggeddocument;
			c.an.result=c.an.parse();
			c.an.highlightnodes(c.an.result);
			response.sendRedirect("scraper.jsp");
		}
		} 
		} else { //Retrieve and shopid=0
			message+="Please select a shop!<br/>";
		}
	}
	if (actie != null && actie.equals("analyze")) {
		URL u=null;
		try{
			u=new URL(url);
		}catch(Exception e){}
		if (u==null) {
			message+="Invalid url, please correct it.<br/>";
		} else {
			
				actie="doanalyze";
			
		}
	}
%>


This page is used to analyze a new web page on how to extract data.<br/>
<%=message %>

<form name="formOne" action='<%=response.encodeURL("analyzer.jsp")%>'
	method="post" id="formOne">
<table>
	<tr>
		<td >Select shop</td>
		<td ><select name="shopid" default="<%=shopidstr%>">
		<option value="0"<%if ("".equals(shopidstr)) out.print("selected");%>>New shop</option><%
			for (int i = 0; i < shops.size(); i = i + 2) {
		%><option value="<%=shops.get(i)%>"<%if (shops.get(i).equals(shopidstr))
					out.print("selected");%>><%=shops.get(i + 1)%></option><%
			}
			for (int i = 0; i < autoshops.size(); i = i + 2) {
		%><option value="auto<%=autoshops.get(i)%>" <%if ((autoshops.get(i).equals(shopidstr)) && (auto.equals("auto")))
					out.print(" selected");%>>(auto)<%=autoshops.get(i + 1)%></option><%
			}
		%>
		</select></td>
	</tr>
	<tr>
		<td>Url</td>
		<td><input type="TEXT" name="url" size="100"
			value="<%out.print(url);%>"></td>
	</tr>
	<tr>
		<td>Post data (GET&... puts all GET params from URL in POST)</td>
		<td><input type="TEXT" name="postdata" size="100"
			value="<%out.print(postdata);%>"></td>
	</tr>
</table>

<input type="HIDDEN" name="actie" id="actie"	value="unknown">
<input type="button" name="submitButton" value="New Analyzer" onClick="javascript:submitForm('analyze',document.formOne)">
<input type="button" name="submitButton" value="Edit existing Analyzer" onClick="javascript:submitForm('retrieve',document.formOne)">
</form>
<a href='analyzer.jsp?actie=analyze&amp;url=https://test.vinopedia.com/testshop.jsp'>Analyze testshop</a>
<% 
if (actie!=null&&actie.equals("retrieve")&&shopid>0&&an==null){
	if (shopid>0){
		int numofanalyzers=Analyzer2.getXpathParserIds(c,shopid).size();
		if (numofanalyzers>1){
			out.write("More than one analyzers were found. Please select one:<br/>");
			for (int i:Analyzer2.getXpathParserIds(c,shopid)){
				out.write("<a href='analyzer.jsp?actie=retrieve&shopid="+shopid+"&row="+i+"'>Edit row "+i+"</a><br/>");
			}
		}
		if(numofanalyzers==0) out.write("No records found for this entry");
	}
}
	if (actie.equals("doanalyze")){
		try{
			XpathParserConfig xppc=Analyzer2.makeConfig("Wine");
			c.an=null;
			an=null;
			an=new Analyzer2(xppc);
			an.setUrl(url);
			an.setPostdata(postdata);
			an.shopid=shopid;
		} catch (Exception e){
			message+="Could not create a parser: "+e.getMessage();
			an=null;
		}
		if (an!=null) {
			c.an=an;
	%>
	<script type="text/javascript">
	greytext="Please wait while the page is being analyzed.<br/> This may take a minute.";
	dograyOut(true);
	redirectpage='scraper.jsp';
	document.onload=analyzetimed();
	</script>
	
	<%} else out.write("Could not create analyzer.");
%>		<jsp:include page="/snippets/footer.jsp" />		
<% 	}
	}%>

</body>
</html>

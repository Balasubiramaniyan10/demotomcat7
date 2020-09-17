<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.common.Wijnzoeker"%><html><head>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList" import="java.util.List"
	import="com.freewinesearcher.online.Translator"
	import="com.freewinesearcher.online.PageHandler"
	import="com.freewinesearcher.common.Shop"
	import="java.util.Iterator" import="java.util.ListIterator"
	import="java.io.*" import="java.text.*" import="java.lang.*"
	import="java.sql.*" import="com.freewinesearcher.common.Wine"
	import="com.freewinesearcher.common.Wineset" import="com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.common.datafeeds.DataFeed" import="com.freewinesearcher.batch.FeedScraper"
	import="com.freewinesearcher.online.Webroutines" import="com.freewinesearcher.batch.TableScraper"
	import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.servlet.ServletFileUpload, org.apache.commons.fileupload.disk.DiskFileItemFactory, org.apache.commons.io.FilenameUtils, java.util.*, java.io.File, java.lang.Exception" 
	import="com.freewinesearcher.common.Variables" import="com.freewinesearcher.common.Dbutil"%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="datafeed" class="com.freewinesearcher.common.datafeeds.DataFeed" scope="session"/>
<jsp:setProperty name="datafeed" property="assume75cl" value="false"/>
<jsp:setProperty name="datafeed" property="*" />

<title>Edit data feed</title>
<jsp:include page="/header2.jsp" />
</head><body>
<%	
	//DataFeed storeddatafeed=(DataFeed)session.getAttribute("datafeed");
	//if (storeddatafeed!=null&&storeddatafeed.feedContent!=null) datafeed.feedContent=storeddatafeed.feedContent;
	PageHandler p=PageHandler.getInstance(request,response,"Edit Data Feed");
	Shop shop=null;
	String savemessage="";
	Wineset wineset=null;
	String feederrors="";
	boolean readyforsave=false;
	long wsid=0;
	try {wsid = Integer.parseInt(request.getParameter("wsid"));}catch (Exception e){}
	long shopid=0;
	try {shopid = Integer.parseInt(request.getParameter("shopid"));}catch (Exception e){}
	String actie = request.getParameter("action");
    if (actie==null) actie="";
    if (session.getAttribute("shop")!=null) shop=(Shop)session.getAttribute("shop");
 	if (Webroutines.getShopFromUserId(request.getRemoteUser())>0) {
 		if (shop==null) shop=new Shop(Webroutines.getShopFromUserId(request.getRemoteUser()));
 		if (!datafeed.validfeed) {
 			String url=datafeed.getUrl();
 			datafeed=DataFeed.getDataFeed(new Context(request),shop.shopid,0);
 			if (datafeed==null) datafeed=new DataFeed(url);
 		}
 		if (datafeed==null) {
 			datafeed=new DataFeed();
 		} else if (datafeed.validfeed){
 			datafeed.parse();
 		}
 	}
 	if (actie!=null&&actie.equals("Retrieve")&&(request.isUserInRole("Moderators")||request.isUserInRole("admin"))) {
		int id=0;
		try{id=Integer.parseInt(request.getParameter("shopid"));}catch(Exception e){}
		if (id>0)shop=new Shop(id);    	
		datafeed=DataFeed.getDataFeed(new Context(request),shop.shopid,0);
		datafeed.parse();
	}
   
	String encoding="";
    if (encoding.equals("")) encoding="iso-8859-1";
    ArrayList<String> countries = Webroutines.getCountries();
	String countrycode = request.getParameter("countrycode");
    if (countrycode!=null&&shop!=null){
    	shop.countrycode=countrycode;
    	shop.country=Webroutines.getCountryFromCode(countrycode);
    }
    ArrayList<String> currencies = Webroutines.getCurrency();
    String linkurl = request.getParameter("linkurl");
    if (linkurl==null) linkurl="";
    if (!linkurl.equals("")&&!linkurl.toLowerCase().startsWith("http")) linkurl="http://"+linkurl;
    if (!linkurl.equals("")&&shop!=null){
    	shop.linkurl=linkurl;
    }
    String currency = request.getParameter("currency");
    if (currency!=null&&shop!=null){
    	shop.currency=currency;
    }
    int vat=-1;
    try{vat=Integer.parseInt(request.getParameter("vat"));}catch(Exception e){}
    if (vat>-1&&shop!=null) shop.exvat=vat;
    if (ServletFileUpload.isMultipartContent(request)){
    	datafeed=new DataFeed(request);
    	if (datafeed.format.equals(DataFeed.formats.Elmar)){
    		shop=new Shop(datafeed.getUrl());
			session.setAttribute("shop",shop);
			datafeed=shop.datafeed;
    	}
    }
    
    
    if (actie!=null&&actie.equals("Analyze")) {
     	shop=null;	
     	session.setAttribute("shop",shop);
    	datafeed=new DataFeed(datafeed.getUrl());    
    	if (datafeed.format.equals(DataFeed.formats.Elmar)){
    		shop=new Shop(datafeed.getUrl());
			session.setAttribute("shop",shop);
			datafeed=shop.datafeed;
    	}
    	Auditlogger al;
    	al=(Auditlogger)session.getAttribute("auditlogger");
    	if (al==null) al=new Auditlogger(request);
    	if (shop!=null&&!al.isUserAuthorized(shop.shopid,request)){
    		al.setAction("Datafeed denied "+actie+" "+shop.ShopConfigUrl);
    		al.setObjecttype("Data feed");
    		al.logaction();
    		shop=null;
    		datafeed=new DataFeed();
    		datafeed.urlstatus=DataFeed.urlstatusses.DuplicateURL;
    	} else {
    		al.setAction("Datafeed "+actie+" "+(shop==null?datafeed.getUrl():shop.ShopConfigUrl));
    		al.setObjecttype("Datafeed feed");
    		al.logaction();
    	}
    	
    }
    if (datafeed!=null&&datafeed.urlstatus==DataFeed.urlstatusses.OK&&datafeed.feedstatus==DataFeed.feedstatusses.OK){
    	readyforsave=true;
    	if (shop==null){
	     	wineset=datafeed.getWines(0,"[Your shop name]",null,1.0,1.0,100,true);
     	} else {
     		wineset=datafeed.getWines(shop.shopid,shop.shopname,null,1.0,1.0,100,true);
     		
     	}
    	feederrors=datafeed.checkWineset(wineset);
    	if (!feederrors.equals("")) readyforsave=false;
    	if (datafeed.url.equals("")) {
    		readyforsave=false;
    		feederrors+="You can only save the datafeed when it is available at a url, not if you uploaded it from your PC (which is just for testing).";
    	}
     	
    	
    }
	session.setAttribute("shop",shop);
	session.setAttribute("datafeed",datafeed);
    	
    	
    	
	if (actie.equals("Saved")){
		out.write("The data feed has been saved. From tomorrow, the wines will be shown on Vinopedia.");
	} else {
%>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>

<% if (request.isUserInRole("admin")){ 
	ArrayList<String> shops = Webroutines.getShopListWithDatafeed();%>
	<form action="editdatafeed.jsp" method="get">Shop
	<select name="shopid" >
	<% for (int i=0;i<shops.size();i=i+2){%>
		<option value="<%=shops.get(i)%>"><%=shops.get(i+1)%></option>
	<%}%>
	</select>
	<input type="hidden" name="wsid" value="<%=wsid %>"/>
	<input type="hidden" name="action" value="Retrieve"/>
	<input type="submit" value="Retrieve"/>
	</form>
<%} %>


<h1>Configure a data feed</h1>

    
On this page you can configure and test your data feed. You can enter the URL of your data feed if it is available on your server. Or you can upload a feed as a file, but in that case you cannot save it (this is for testing purposes only. If the data feed can be processes you must make it available on a server so we can download it daily).
<br/><br/>
<table><tr><td>
<form name="formOne"
	action="editdatafeed.jsp" METHOD="POST"
	id="formOne">
	
	<input type="hidden" name="wsid" value="<%=wsid %>"/>
Data feed url (internet address)<br/>
<input type='text' name='url' value='<%=(datafeed!=null?datafeed.getUrl():"")%>'	size='50'/><input type="hidden" name="action" id="actie" value="Analyze"/> 
<input type="submit" name="submitButton" value="Load data feed"/><br/>
<%if (!savemessage.equals("")) out.write("<font color='red'>"+savemessage+"</font><br/>");%>
<% out.write("<font color='red'>"+datafeed.getUrlStatusMessage()+"</font><br/>");%>
(If your shop supports <a href='http://projekt.wifo.uni-mannheim.de/elmar/nav?dest=impl.shopsystems.index&rid=26' target='_blank'>Elmar</a>, please enter the url of the shopinfo.xml file.)<br/>


</form>
</td><td>
<form action="editdatafeed.jsp" method="post" enctype="multipart/form-data">
Upload a data feed<br/>
	  <input name="myFile" type="file" />
	  <input type="submit" value="Upload" />
	</form>
</td></tr></table>	
	
<br/>
<%if (datafeed.urlstatus.equals(DataFeed.urlstatusses.OK)){ 
	if (!datafeed.feedstatus.equals(DataFeed.feedstatusses.OK)){%>	
	<h3>Feed could not be read</h3>
	<%=datafeed.getFeedStatusMessage()%><br/><br/>
<form action='/contact.jsp'>
Email address: <input type='text' name='email' size='100' />
<input type='hidden' name= 'site' value='<%=((shop==null||shop.shopurl==null)?datafeed.getUrl():shop.shopurl).replaceAll("'","&apos;")%>'/>
<input type='hidden' name='captcha' value='nocheck'/>
<input type='hidden' name='formsent' value='sent'/>
<input type='hidden' name='message' value='A merchant reported a problem with a datafeed.'/>
<input type='submit' value='Send' />
</form>
<%} else {%>

<h2>Review and test data feed</h2>
Your data feed was recognized as <%=datafeed.format%> <strong>but has not yet been saved</strong>. Below you should review the results and change the settings, so the wine name, bottle size etc. are displayed correctly. After testing it you can go on to step 4 (entering your company information) by pressing the button below the configuration editor.
<form id="feedconfig" action="editdatafeed.jsp" method="post">
<input type="hidden" name="wsid" value="<%=wsid %>"/>
<h2>Data feed configuration editor</h2>
We need to know how to find the different types of information in the data feed. "Wine name" should contain all information to identify an individual wine, including producer and region. You can select more than one field; the wine name as it is shown to the users will be composed of the different fields in the order you specify.<br/>
For the other types of information such as vintage and price, you can also select more than one field; only the first suitable match is selected if the fields contain different data.<br/>
Note: you can select the same field for different types of information. For instance: if a "description" field contains the name and the vintage, you select that field for both wine name and vintage.<br/><br/>
<table >
<tr><th width='230px'>Information</th><%for (int i=1;i<=datafeed.maxlabels;i++) out.write("<th>Field "+i+"</th>"); %></tr>
<tr><td>Wine name</td><%=datafeed.getSelectBox("name") %><td><%if (datafeed.getNameorder()[0].equals("")) out.write("<font color='Red'>Please select one or more fields</font>"); %></td></tr>
<tr><td>Vintage</td><%=datafeed.getSelectBox("vintage") %><td><%if (datafeed.getVintageorder()[0].equals("")) out.write("<font color='Red'>Please select one or more fields</font>"); %></td></tr>
<tr><td>Price</td><%=datafeed.getSelectBox("price") %><td><%if (datafeed.getPriceorder()[0].equals("")) out.write("<font color='Red'>Please select one or more fields</font>"); %></td></tr>
<tr><td>Product url</td><%=datafeed.getSelectBox("url") %><td><%if (datafeed.getUrlorder()[0].equals("")) out.write("<font color='Red'>Please select one or more fields with a product link. If no product link is in the feed, the general URL of the shop will be used.</font>"); %></td></tr>
<tr><td>Bottle size</td><%=datafeed.getSelectBox("size") %><td><%if (datafeed.getSizeorder()[0].equals("")) out.write("<font color='Red'>Please select one or more fields</font>"); %></td></tr>
<tr><td colspan='1'>Assume 75cl when no size is found?</td><td><input type='checkbox' name='assume75cl' <%=(datafeed.assume75cl?" checked='checked' ":"")%>/></td><td colspan='2'></td></tr>
</table>
<% out.write (datafeed.assume75cl||wineset.records!=datafeed.wineswithsize?(!datafeed.assume75cl&&wineset.records>2*datafeed.wineswithsize?"<font color='red'>The bottle size was found for only ":"The bottle size was found for ")+datafeed.wineswithsize+" of in total "+wineset.records+" wines."+(!datafeed.assume75cl&&wineset.records>2*datafeed.wineswithsize?" If for normal bottles (75cl) no size is given in the datafeed, please mark the checkbox. </font>":(datafeed.assume75cl?" If no bottle size is found, a size of 75cl is taken (this is not yet shown in the result list below).":"")+" Only mark the checkbox if the data feed contains no size indication for 75cl bottles.<br/>"):""); %>
<font color='red'><%=feederrors%><br/></font>
<br/><br/><table><tr><td><input type="submit" name='action' value="Test"><input type="hidden" name="wsid" value="<%=wsid %>"/><%if (wsid>0){ %>	<input type="hidden" name="shopid" value="<%=shopid %>"/>
	<%} %></form></td><td>
<script language="JavaScript" type="text/javascript">
function shopinfo(){
	document.getElementById("feedconfig").action='/settings/editshopinfo.jsp?test';
	document.getElementById("feedconfig").submit();
	
}
document.write('<input type="button" onclick="javascript:shopinfo()" name="action" value="Step 4: Company details" <%if (!readyforsave) out.write ("disabled=\"true\""); %>/></td></tr></table>');
</script>
<noscript><form action="/settings/editshopinfo.jsp" method="post" ><input type="hidden" name="wsid" value="<%=wsid %>"/><%if (wsid>0) {%>	<input type="hidden" name="shopid" value="<%=shopid %>"/>
	<%} %><input type="submit" name='action' value="Step 4: Company details" <%if (!readyforsave) out.write ("disabled='true'"); %>/></form></td></tr></table>
	
Please make sure you test your settings after making changes, otherwise they will not be saved!<br/></noscript>
<%if (!readyforsave) {%>
Before you can go to the next screen (shop details), you must correct the following issues:<br/>
<%=feederrors %>
<%} else if (new Context(request).userid.equals("")){
%>Note: you must log in in order to save data feeds. If you have no account yet, click <a href="/forum/user/insert.page" target='_blank'>here</a> to create one.
<%}%>
<h2>Data feed content</h2>
The data feed contains the following information:<br/><br/>
<div style="overflow:auto; max-height:250px; width:900px;border-style:solid">
<%out.write(datafeed.toHTML()); %>
</div><br/>
<h2>Data feed results</h2>
In the table below you can see the results of the datafeed editor. Please check if all fields are filled correctly and make any necessary changes in the configuration table above. If all fields are filled correctly, you can go to step 4: company details. There you can change the shop name and currency and save the data feed.<br/><br/>
<form action="/settings/editshopinfo.jsp" method="post"><input type="submit" name='action' value="Step 4: Company details" <%if (!readyforsave) out.write ("disabled='true'"); %>/></form></td></tr></table>
<%

	out.write(Webroutines.getTabbedWineResultsHTML(wineset,new Translator(),searchdata,99999,response,"false",false,"editdatafeed.jsp",p.s.singlevintage,null,true,false));%>
	<%}//datafeed!=null
}//feedcontent>0
}//saved%>	
<%@ include file="/snippets/footer.jsp" %>	
</body>
</html>

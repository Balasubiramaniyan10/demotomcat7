<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.common.Wijnzoeker"%><html><head>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<title>Edit data feed</title>
<jsp:include page="/header2.jsp" />
</head><body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
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
	import="com.freewinesearcher.common.Variables" import="com.freewinesearcher.common.Dbutil"%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="datafeed" class="com.freewinesearcher.common.datafeeds.DataFeed" scope="session"/>
<jsp:setProperty name="datafeed" property="assume75cl" value="false"/>
<jsp:setProperty name="datafeed" property="*" />

<%	Shop shop=null;
	String savemessage="";
	Wineset wineset=null;
	String feederrors="";
	boolean readyforsave=false;
	if (session.getAttribute("shop")!=null) shop=(Shop)session.getAttribute("shop");
 	String actie = request.getParameter("action");
    if (actie==null) actie="";
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
    String currency = request.getParameter("currency");
    if (currency!=null&&shop!=null){
    	shop.currency=currency;
    }
    int vat=-1;
    try{vat=Integer.parseInt(request.getParameter("vat"));}catch(Exception e){}
    if (vat>-1&&shop!=null) shop.exvat=vat;
    
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
    		datafeed.urlstatus=DataFeed.urlstatusses.DuplicateURL;
    		al.setAction("Datafeed denied "+actie+" "+shop.ShopConfigUrl);
    		al.setObjecttype("Data feed");
    		al.logaction();
    		shop=null;
    		datafeed=null;
    	} else {
    		al.setAction("Datafeed "+actie+" "+(shop==null?datafeed.getUrl():shop.ShopConfigUrl));
    		al.setObjecttype("Datafeed feed");
    		al.logaction();
    	}
    	session.setAttribute("shop",shop);
		session.setAttribute("datafeed",datafeed);
    	
    }
    if (datafeed!=null&&datafeed.urlstatus==DataFeed.urlstatusses.OK&&datafeed.feedstatus==DataFeed.feedstatusses.OK){
    	readyforsave=true;
     	if (shop==null){
	     	wineset=datafeed.getWines(0,"[Your shop name]",null,1.0,1.0,9999,true);
     	} else {
     		wineset=datafeed.getWines(shop.shopid,shop.name,null,1.0,1.0,9999,true);
     		shop.checkShop(linkurl);
        	if (!shop.checkshopmessage.equals("")) readyforsave=false;
     	}
    	feederrors=datafeed.checkWineset(wineset);
    	if (!feederrors.equals("")) readyforsave=false;
    	
     	
    	if (actie!=null&&actie.equals("Save")&&readyforsave) {
    		Auditlogger al;
    		al=(Auditlogger)session.getAttribute("auditlogger");
    		if (al==null) al=new Auditlogger(request);
    		int shopid=shop.save();
    		if (shopid>0){
    		shop.shopid=shopid;
    		datafeed.shopid=shopid;
    		long feedid=0;
			if (shopid>0) feedid=datafeed.save(new Context(request));
			if (feedid>0) {actie="Saved";} else {actie="Save failed";};
			al.setAction("Datafeed "+actie+" "+shop.ShopConfigUrl);
    		al.setObjecttype("Datafeed feed");
    		al.logaction();
    		} else if (shopid==-1){
    			savemessage+="Could not save the shop information because a shop with the same name already exists. <br/>";
    		} else {
    			savemessage+="There was a problem while saving the shop information. <br/>";
    		}
    		}    	
    	}
     	
    	
    	
	if (actie.equals("Saved")){
		out.write("The data feed has been saved. From tomorrow, the wines will be shown on Vinopedia.");
	} else {
%>

<h1>Configure a product feed</h1>
On this page you can configure and test your product feed. Please enter the url (internet address) of your product feed below. If your shop supports <a href='http://projekt.wifo.uni-mannheim.de/elmar/nav?dest=impl.shopsystems.index&rid=26' target='_blank'>Elmar</a>, please enter the url of the shopping.xml file.<br/><br/>
<form name="formOne"
	action="<%=response.encodeURL("analyzeelmar.jsp")%>" METHOD="POST"
	id="formOne">

Data feed url: <input type='text' name='url' value='<%=(shop!=null?shop.ShopConfigUrl:datafeed!=null?datafeed.getUrl():"")%>'	size='100'/><br/>
<%if (!savemessage.equals("")) out.write("<font color='red'>"+savemessage+"</font><br/>");%>
<% out.write("<font color='red'>"+datafeed.getUrlStatusMessage()+"</font><br/>");%>
<%if (shop!=null&&!shop.urlmessage.equals("")) out.write("<font color='red'>"+shop.urlmessage+"</font><br/>");%>
<br/>
<input type="hidden" name="action" id="actie" value="Analyze"> <input type="submit" name="submitButton" value="Load data feed">
</form>
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

<form action="<%=PageHandler.getInstance(request,response).thispage %>" method="post">
<%if (shop!=null){ %>
<h2>Shop information<%=(shop.shopid>0?" for shop "+shop.shopid:"") %></h2>

<table>
<%=(shop!=null?shop.getShopInfoHTML():"") %>
<tr><td>Country</td><td><select name="countrycode" >
<% for (int i=0;i<countries.size();i=i+2){%>
<option value="<%=countries.get(i)%>" <%if (countries.get(i).equals(shop.countrycode)) out.write(" selected='selected'");  %>><%=countries.get(i+1)%>
<%}%>
</select></td></tr>
<tr><td>Currency</td><td><select name="currency" >
<% for (int i=0;i<currencies.size();i++){%>
<option value="<%=currencies.get(i)%>" <%if (currencies.get(i).equals(shop.currency)) out.write(" selected='selected'");  %>><%=currencies.get(i)%>
<%}%>
</select></td></tr>
<tr><td><font <%if (shop.exvat==2) out.write(" color='red'");%>>VAT</font></td><td><select name="vat">
<option value="1"  <%if (shop.exvat==1) out.write("Selected"); %>>Included
<option value="0"  <%if (shop.exvat==0) out.write("Selected"); %>>Excluded
<option value="2"  <%if (shop.exvat==2) out.write("Selected"); %>>Unknown!!!
</select></td></tr>
<tr><td><font <%=(linkurl.equals("")?" color='red'":"")%>>Url of page with link to vinopedia.com</font></td><td><input type='text' size=100 name='linkurl' value='<%=Webroutines.EscapeHTML(linkurl) %>'/>
</td></tr>
</table>
<%

if (!shop.checkshopmessage.equals("")) {
	readyforsave=false;
	out.write("<br/><font color='red'>"+shop.checkshopmessage+"</font>");
}
}

%><h2>Data Feed Content</h2>

<% 
out.write("Your data feed was recognized as "+datafeed.format+". ");%>
The content of the data feed is as follows:<br/><br/>
<div style="overflow:auto; max-height:250px; width:900px;border-style:solid">
<%out.write(datafeed.toHTML()); %>
</div><br/>
<h2>Data feed configuration</h2>
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
<br/><br/><input type="submit" name='action' value="Test"><input type="submit" name='action' value="Save" <%if (!readyforsave) out.write ("disabled='true'"); %>>
</form>
<%if (!readyforsave) {%>
Before you can save the data feed, you must correct the following issues:<br/>
<%=shop==null?"":shop.urlmessage %>
<%=shop==null?"":shop.checkshopmessage%>
<%=feederrors %>
<%} %>
<h2>Datafeed results</h2>
In the table below you can see the results of the mapping. Please check if all fields are filled correctly and make any necessary changes in the configuration table above.
<%PageHandler p=PageHandler.getInstance(request,response);

	out.write(Webroutines.getWineResultsHTML(wineset,new Translator(),searchdata,99999,response,"false",false,null,"editdatafeed.jsp",true,false));%>
	<%}//datafeed!=null
}//feedcontent>0
}//saved%>	
<%@ include file="/snippets/footer.jsp" %>	
</body>
</html>

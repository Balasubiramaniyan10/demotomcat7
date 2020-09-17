<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="com.freewinesearcher.online.Shopapplication"%><html><head>
<title>Edit shop information</title>
<jsp:include page="/header2.jsp" />
</head><body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.common.Wijnzoeker"%>
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
<jsp:useBean id="shop" class="com.freewinesearcher.common.Shop" scope="session"/>
<jsp:setProperty name="shop" property="*" />
<jsp:setProperty name="datafeed" property="*" />


<%	long wsid=0;
	try {wsid = Integer.parseInt(request.getParameter("wsid"));}catch (Exception e){}
	if (wsid>0){
		Shopapplication sa=Shopapplication.retrieve(wsid);
		if (sa==null) sa=Shopapplication.generate(wsid);
		shop=new Shop(sa.getShopid());
		shop.linkurl=sa.getUrlforvplink();
	}
	if (shop.emptyshop&&Webroutines.getShopFromUserId(request.getRemoteUser())>0&&!request.isUserInRole("admin")) {
		shop=new Shop(Webroutines.getShopFromUserId(request.getRemoteUser()));
		session.setAttribute("shop",shop);
		if (!datafeed.validfeed) {
			datafeed=DataFeed.getDataFeed(new Context(request),shop.shopid,0); 
			if (datafeed==null) {
				datafeed=new DataFeed();
			} else {
				datafeed.parse();
			}
		}
	}
	ArrayList<String> shops = Webroutines.getShopList("");

	PageHandler p=PageHandler.getInstance(request,response,"Edit Shop info");
	shop.checkShop();
	String savemessage="";
	Wineset wineset=null;
	String feederrors="";
	boolean readyforsave=false;
	String actie = request.getParameter("actie");
    if (actie==null) actie="";
    ArrayList<String> countries = Webroutines.getCountries();
	ArrayList<String> currencies = Webroutines.getCurrency();
    Auditlogger al=null;
    al=(Auditlogger)session.getAttribute("auditlogger");
    if (al==null) al=new Auditlogger(request);
    if (shop!=null&&!al.isUserAuthorized(shop.shopid,request)){
    	al.setAction("Shop edit denied "+actie+" "+shop.ShopConfigUrl);
    	al.setObjecttype("Shop");
    	al.logaction();
    	shop=null;
    	datafeed=new DataFeed();
    	datafeed.urlstatus=DataFeed.urlstatusses.DuplicateURL;
    } else {
    	al.setObjecttype("Shop info");
    	al.setAction("Shop info "+actie);
    	al.logaction();
    }
	if (datafeed!=null&&datafeed.urlstatus==DataFeed.urlstatusses.OK&&datafeed.feedstatus==DataFeed.feedstatusses.OK){
    	readyforsave=true;
     	if (shop==null){
	     	wineset=datafeed.getWines(0,"[Your shop name]",null,1.0,1.0,9999,true);
     	} else {
     		wineset=datafeed.getWines(shop.shopid,shop.shopname,null,1.0,1.0,9999,true);
     		shop.checkShop();
        	if (!shop.checkshopmessage.equals("")) readyforsave=false;
     	}
    	feederrors=datafeed.checkWineset(wineset);
    	if (!feederrors.equals("")) readyforsave=false;
	}    	
	if (actie!=null&&actie.equals("Retrieve")&&request.isUserInRole("admin")) {
		int id=0;
		try{id=Integer.parseInt(request.getParameter("shopid"));}catch(Exception e){}
		if (id>0)shop=new Shop(id);    	
	}
    if (actie!=null&&actie.equals("Save")&&(readyforsave||request.isUserInRole("admin"))) {
    	al=(Auditlogger)session.getAttribute("auditlogger");
    	if (al==null) al=new Auditlogger(request);
    	int shopid=shop.save(new Context(request).userid);
    	if (shopid>0){
    	shop.shopid=shopid;
    	datafeed.shopid=shopid;
    	long feedid=0;
		if (shopid>0) {
			feedid=datafeed.save(new Context(request));
			if (!request.isUserInRole("admin")){
				Dbutil.functionallogger.error("Shop "+shopid+" ("+shop.shopname+") was saved.");
				Auditlogger al2=new Auditlogger(request);
				al2.setObjecttype("Shop");
				al2.setAction("Accepted Site Index Agreement");
			}
		}
		if (feedid>0) {
			actie="Saved";
			
		} else {
			actie="Save failed";
			Dbutil.functionallogger.info("Shop "+shopid+" ("+shop.shopname+") could not be saved.");
		};
		
		al.setAction("Shop info "+actie);
    	al.setObjecttype("Shop info");
    	al.logaction();
    	} else if (shopid==-1){
    		savemessage+="Could not save the shop information because a shop with the same name already exists. <br/>";
    	} else {
    		savemessage+="There was a problem while saving the shop information. <br/>";
    	}
    }    	
     	
    	
    	
	if (actie.equals("Saved")){
		out.write("The shop information has been saved. From tomorrow, the wines will be shown on Vinopedia.");
	} else {
%>
<% if (request.isUserInRole("admin")){ %>
	<form action="<%=PageHandler.getInstance(request,response).thispage %>" method="get">Shop
	<select name="shopid" >
	<% for (int i=0;i<shops.size();i=i+2){%>
		<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(shop.shopid))) out.print(" Selected");%>><%=shops.get(i+1)%></option>
	<%}%>
	</select>
	<input type="submit" name="actie" value="Retrieve"/>
	</form>
<%} %>
<form action="<%=PageHandler.getInstance(request,response).thispage %>" method="get">
<h2>Shop information<%=(shop.shopid>0?" for shop "+shop.shopid:"") %></h2>
<table>
<tr><td>Shop Name</td><td><input type="text" name="shopname" size="100" value="<%=shop.shopname%>"></td></tr>
<tr><td>Address</td><td><input type="text" name="address" size="100" value="<%=shop.address%>"></td></tr>
<tr><td>Country</td><td><select name="countrycode" >
<option value="" >Please select</option>
<% for (int i=0;i<countries.size();i=i+2){%>
<option value="<%=countries.get(i)%>" <%if (countries.get(i).equals(shop.countrycode)) out.write(" selected='selected'");  %>><%=countries.get(i+1)%></option>
<%}%>
</select></td></tr>
<tr><td>Email address</td><td><input type="text" name="email" size="100" value="<%=shop.email%>"></td></tr>
<tr><td>Url of homepage of the shop</font></td><td><input type="text" name="shopurl" size="100"  value="<%=shop.shopurl%>"></td></tr>
<tr><td>Url of link to vinopedia.com</td><td><input type="text" name="linkurl" size="100" value="<%=shop.linkurl%>"></td></tr>
<tr><td>Currency of your prices</td><td><select name="currency" >
<% for (int i=0;i<currencies.size();i++){%>
<option value="<%=currencies.get(i)%>" <%if (currencies.get(i).equals(shop.currency)) out.write(" selected='selected'");  %>><%=currencies.get(i)%>
<%}%>
</select></td></tr>
<tr><td>Is VAT included in your prices? </td><td><select name="exvat">
<option value="0"  <%if (shop.exvat==0) out.write("Selected"); %>>Included</option>
<option value="1"  <%if (shop.exvat==1) out.write("Selected"); %>>Excluded</option>
<option value="2"  <%if (shop.exvat==2) out.write("Selected"); %>>Please select</option>
</select></td></tr>
<tr><td colspan='2'>I accept the <a href='/siteindexationagreement.jsp' target='_blank'>terms and conditions</a><input type="checkbox" name="termsaccepted" <%=(shop.isTermsaccepted()?"checked='checked' ":"")%>></td></tr>
</table>
<input type="submit" name="actie" value="Save"/>
<%

if (actie!=null&&actie.equals("Save")&&!readyforsave) {
	out.write("<br/><font color='red'>"+shop.checkshopmessage+"</font>");
	if (datafeed==null||datafeed.feedstatus!=DataFeed.feedstatusses.OK){
		out.write("<br/><font color='red'>Could not save your company info: there was a problem with your <a href='editdatafeed.jsp'>data feed</a>. Please check your data feed.</font>");
		
	}
}
}//saved%>	
<%@ include file="/snippets/footer.jsp" %>	
</body>
</html>

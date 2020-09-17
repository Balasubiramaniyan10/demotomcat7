<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Wine"
import = "com.freewinesearcher.common.Wineset"
%>
<% long start=System.currentTimeMillis();
	boolean debuglog=false;%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>

<jsp:useBean id="cartmanager" class="com.freewinesearcher.online.shoppingcart.CartManager" scope="session"/>
<jsp:setProperty name="cartmanager" property="*" />
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start Pagehandler"); %>
<% 	String message="";
	String action=request.getParameter("action");
	if (action==null) action="";
	double price=0;
	try {price=Double.parseDouble(request.getParameter("price").replaceAll(",","."));}catch(Exception e){}
	int cartnumber=0;
	try {cartnumber=Integer.parseInt(request.getParameter("cartnumber"));} catch (Exception e){};
	boolean retrieve=false;
	if (action.equals("retrieve")) retrieve=true;
	Shoppingcart cart=cartmanager.getCart(new Context(request),cartnumber,retrieve);
	PageHandler p=PageHandler.getInstance(request,response);


Wineset w=(Wineset)session.getAttribute("wineset"+cart.shopid);
if (w==null) w=Webroutines.getRecommendedWineset(cart.shopid);
session.setAttribute("wineset"+cart.shopid,w);
if (cart.getWineid()>0&&cart.shopid>0&&"add".equals(request.getParameter("action"))){
	cart.changeAmount(cartmanager.getAmount());
}
if (cart.wineid>0&&cart.shopid>0&&"delete".equals(request.getParameter("action"))){
	cart.delete();
}
if ("save".equals(action)){
	try {
		cart.save(new Context(request));
	} catch (CartSerializer.CartAlreadyExistedException e){
		message+=e.getMessage()+"<br/>The shoppingcart that was saved before is shown below. ";;
		cart=cartmanager.getCart(new Context(request),cartnumber,true);
	} catch (CartSerializer.ConcurrentEditConflictException e){
		message+=e.getMessage()+"<br/>The latest status of the shoppingcart is shown below. ";
		cart=cartmanager.getCart(new Context(request),cartnumber,true);
	} catch (Exception e){
		message+="A problem has occurred while trying to save your shoppingcart. Please try again later. ";
	} finally {
		if (message.equals("")) {
			if (cart.role==Shoppingcart.roles.Buyer) {
				message+=p.t.get("ordersdavedbuyer");
			} else {
				message+=p.t.get("ordersavedseller");
			}
		}
	}
}
if ("placeorder".equals(action)){
	try {
		cart.confirmOrder(new Context(request));
	} catch (CartSerializer.OrderNotVerifiedException e){
		message+=e.getMessage()+"<br/>The shoppingcart was sent to the seller for approval. ";;
		cart=cartmanager.getCart(new Context(request),cartnumber,true);
	} catch (CartSerializer.CartAlreadyExistedException e){
		message+=e.getMessage()+"<br/>The shoppingcart that was saved before is shown below. ";;
		cart=cartmanager.getCart(new Context(request),cartnumber,true);
	} catch (CartSerializer.ConcurrentEditConflictException e){
		message+=e.getMessage()+"<br/>The latest status of the shoppingcart is shown below. ";
		cart=cartmanager.getCart(new Context(request),cartnumber,true);
	} catch (Exception e){
		message+="A problem has occurred while trying to save your shoppingcart. Please try again later. ";
	} finally {
		if (message.equals("")) {
			message+=p.t.get("orderplaced");
		}
	}
}
if ("changeprice".equals(action)) cart.changePrice(price);
if ("approve".equals(action)) cart.confirmPrice(price);
if ("outofstock".equals(action)) cart.setOutofStock();
if ("updateshipping".equals(action)) cart.setShipping(price);
if ("updatevat".equals(action)) cart.setVat(price);


Shop shop;
shop=new Shop(cart.shopid);
%>
<jsp:setProperty name="cartmanager" property="amount"  value="0"/> 
<%@page import="com.freewinesearcher.online.shoppingcart.Shoppingcart"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.Comparator"%>
<%@page import="com.freewinesearcher.common.Knownwine"%>
<%@page import="com.freewinesearcher.online.Shop"%>
<%@page import="com.freewinesearcher.common.Configuration"%>
<%@page import="com.freewinesearcher.online.shoppingcart.CartManager"%>
<%@page import="com.freewinesearcher.common.Context"%>
<%@page import="com.freewinesearcher.online.shoppingcart.CartSerializer"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
	if (!p.searchdata.getName().equals("")) {
		out.print(Webroutines.escape(p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ", "")) + " "
				+ p.t.get("pricesbyfws"));
	} else {
		out.print("vinopedia");
	}
%></title>
<%
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/jsincludes.jsp" %>
<meta name="verify-v1" content="DPurn9ZNRpI1pXuOlIigNqJ6JoMePo97QY0m2L3eBrA=" />
</head>
<body onclick="javascript:emptySuggest();">
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	Ad rightad = new Ad("newdesign",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.bestknownwineid,"");
	//Ad bottomleftad = new Ad("newdesign",187, 300, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner + "");			
	Ad betweenresults = new Ad("newdesign",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.bestknownwineid, rightad.partner+"");
%>
<% 
		//Show search results%>
		<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%>
				</div>
<%  int vintage=0;
	try{vintage=Integer.parseInt(cart.wine.Vintage);}catch(Exception e){}
	if (cart.wine!=null&&cart.role!=Shoppingcart.roles.Seller) {
		Webroutines.RatingInfo ri=Webroutines.getNewRatingsHTML(cart.wine.Knownwineid, 1000, p.searchpage,vintage,p.searchdata,p.t);
		out.print(ri.html);
		}%>
		<div class='main'>
			<div >	
			<%=message%>
<%=cart.getCartHTML(p,cartmanager.oldcart) %>
<%=message %>
<%=cartmanager.getCartsHTML(new Context(request),p) %>

<%if (cart.role==Shoppingcart.roles.Buyer)	{ %>

<% if (cart.wine!=null){ %>
<div class='champcolumn'><div class='text'><h1>Wine description from <%=Webroutines.getShopNameFromShopId(cart.shopid,"") %></h1>
<%=cart.wine.Vintage.equals("")?"":cart.wine.Vintage+" "+cart.wine.Name %>
<h2>Price: <%=Webroutines.formatPrice(cart.wine.PriceEuroIn,cart.wine.PriceEuroEx,p.searchdata.getCurrency(),"IN") %></h2>
(this includes the local VAT, the final price when ordering may be corrected for the VAT in the country of delivery).
<%if (cart.wine.Knownwineid>0){ %><form method='link' action='/wine/<%=Webroutines.URLEncode((Knownwines.getUniqueKnownWineName(cart.wine.Knownwineid)+" "+cart.wine.Vintage).trim()) %>' target='_blank'><input type='submit' value='Compare prices'  /></form><%} %>
<form action="shoppingassistant.jsp" method='get'>
<input type='hidden' name='action' value='add'/>
<input type='hidden' name='wineid' value='<%=cart.wine.Id %>'/>
<h2>Add to your shoppinglist:</h2><input type='text' name='amount' value='1' size='1'/> bottles
<input type='submit' value='Add to shoppinglist'/>
</form>
</div><div class='champcolumnbottom'></div></div>

<div class='champcolumn'><div class='text'><h1>Vinopedia wine information</h1>
<%=new Knownwine(cart.wine.Knownwineid).getDescription() %>
</div><div class='champcolumnbottom'></div></div>
<%} %>
<% if (cart.shopid>0){ %>
<div class='champcolumn'><div class='text'>
<h1>About <%=Webroutines.getShopNameFromShopId(cart.shopid,"") %></h1>
Located in Switzerland, we are one of the largest sellers in Europe selling over 10000 wines.
<br/><input id='showmap' type='button' onclick='javascript:loadmap();' value='Show map'/>
	
</div><div class='champcolumnbottom'></div></div>

<div class='clearboth'></div>
<div id="spacer" style="width: 800px; height: 20px;visibility:hidden;"></div>
	<div id="map" style="width: 798px; height: 20px;visibility:hidden;"></div>
    <script type="text/javascript">
    function loadmap() {
    	document.getElementById("spacer").style.height="20px";	
    	document.getElementById("map").style.height="600px";	
  		var map = new GMap2(document.getElementById("map"));
  		map.addControl(new GLargeMapControl());
  		var center = new GLatLng(<%=shop.lat%>,<%=shop.lon%>);
  		 map.setCenter(center, <%=(8)%>);
  		<%
  		out.write("var marker=new GMarker(center, {draggable: true, bouncy: true});\n");
		//String info="";
		//info+=prod.name+"<br/>";
		//info+=prod.address+"<br/>";
		//info=info.replace("'","&apos;");
		//out.write("marker"+i+".bindInfoWindowHtml('<html><body>"+info+"</body></html>','clickable=true;');\n");
		out.write("map.addOverlay(marker);\n");
		%>
		document.getElementById("map").style.visibility="visible";	
		document.getElementById("showmap").value="Hide map";	
		document.getElementById("showmap").onclick=hidemap;	
		return false;  
	  	
	}	
    function hidemap() {
    	document.getElementById("map").style.visibility="hidden";	
    	document.getElementById("map").style.height="0px";	
		document.getElementById("showmap").value='Show map';	
		document.getElementById("showmap").onclick=loadmap;	
		return false;  
	
    }	
	
    </script>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<% out.write(Configuration.serverrole.equals("PRD")?Configuration.GoogleApiKey:Configuration.GoogleApiKeyDev);%>"
      type="text/javascript"></script>

<%} %>

<%	Comparator<Wine> pq = new Wine.bestDealComparator();
	if ("pq".equals(cart.order)) pq=new Wine.pqratioComparator();
	if ("relprice".equals(cart.order)) pq=new Wine.relativepriceComparator();
	if ("vintage".equals(cart.order)) pq=new Wine.vintageComparator();
	if ("rating".equals(cart.order)) pq=new Wine.ratingComparator();
	if ("priceeuroin".equals(cart.order)) pq=new Wine.priceComparator();
	Arrays.sort(w.Wine, pq);
 %>
<% out.write(Webroutines.getWineRecommendationsHTML(w,p.t,p.searchdata,25,response,"false",false,null,"shoppingassistant.jsp",cart));%>










<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start footer"); %>
			<%@ include file="/snippets/footer.jsp" %>	
			</div>
		</div> 

<%
}
}
%>
	
	

</body>
</html>
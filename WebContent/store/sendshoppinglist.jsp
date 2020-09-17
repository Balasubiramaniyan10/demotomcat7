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
<%PageHandler p=PageHandler.getInstance(request,response); %>
<jsp:useBean id="cartmanager" class="com.freewinesearcher.online.shoppingcart.CartManager" scope="session"/>
<jsp:setProperty name="cartmanager" property="*" />
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start Pagehandler"); %>

<%@page import="com.freewinesearcher.online.StoreInfo"%>
<%@page import="com.freewinesearcher.online.Shop"%>
<%@page import="com.freewinesearcher.common.Context"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(p.searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : p.searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title></title>
<%
Shop shop=StoreInfo.getStore(cartmanager.getShopid());
shop.getShopInfo();
String message=request.getParameter("message");
if (message==null) message=cartmanager.getCart(new Context(request),0,false).getCart4Email(new Context(request).getEmail());
String email=request.getParameter("email");
if (email==null) email=new Context(request).getEmail();
String shippingcountry=request.getParameter("shippingcountry");
if (shippingcountry==null) shippingcountry=Webroutines.getCountryFromCode(p.hostcountry);
if (shippingcountry==null||shippingcountry.equals("Europe")) shippingcountry="";
String action=request.getParameter("action");
if (action==null) action="";
boolean sent=false;
int succes=0;

if (action.equals("send")&&!shippingcountry.equals("")&&Webroutines.getRegexPatternValue("(\\w+.*@.*\\w+.*\\..*\\w.*)",email).length()>0){
	// Send cart
	sent=true;
	succes=cartmanager.getCart(new Context(request),0,false).sendByEmail(email,shippingcountry,message);
	p=PageHandler.getInstance(request,response,"Sent Shoppinglist");
	p.logger.price=(float)cartmanager.getCart(new Context(request),0,false).totalamount;
	p.logger.shopid=cartmanager.getCart(new Context(request),0,false).shopid+"";
}

%> 
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/header2.jsp" %>
<script type="text/javascript" src='/js/store.js'/>
<%@ include file="/snippets/guidedsearchincludes.jsp" %>
</head>
<body>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	Ad rightad = new Ad("newdesign",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.bestknownwineid,"");
	//Ad bottomleftad = new Ad("newdesign",187, 300, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner + "");			
	Ad betweenresults = new Ad("newdesign",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.bestknownwineid, rightad.partner+"");
%>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%>
				
		<div class='main'>
		<%if (!sent){ %>
		<h1>Send shopping list to merchant</h1>
		<form action='<%=p.thispage %>' method='post'>
		<table><tr><td>Country or state for delivery of the wines:</td><td><input type="text" name="shippingcountry" value="<%=shippingcountry%>"/></td></tr>
		<%if (action.equals("send")&&shippingcountry.equals("")){
			out.print("<tr><td colspan='2'><font color='red'>Please provide a country for delivery!</font></td></tr>");
		}%>
		<tr><td>Your email address:</td><td><input type='text' name='email' value='<%=email%>'/></td></tr>
		<%if (action.equals("send")&&Webroutines.getRegexPatternValue("(\\w+.*@.*\\w+.*\\..*\\w.*)",email).length()==0){
			out.print("<tr><td colspan='2'><font color='red'>Please provide a valid email address</font></td></tr>");
		}%>
		</table><br/>
		Message:<br/>
		<textarea name='message' style='width:600px;height:200px;'><%=message %></textarea> <br/>
		<input type="hidden" name="action" value="send"/>
		<input type="submit" value="Send"/>
		</form><br/><br/>
		
		<%} else {
			if (succes==1){ %>
			<h1>Your shoppinglist was sent!</h1>
			You should be contacted by <%=shop.name %> shortly. Thank you for using this service, we hope you like it.
			<% }else { %>
				<h1>Sorry...</h1>
				We tried sending your request, but there was a problem while sending the message. 
			<%} 
		}%>
			
		
			<%@ include file="/snippets/footer.jsp" %>	
			</div>
			
		</div>
<%
}

%>
	
	

</body>
</html>
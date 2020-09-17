
<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/logout");
    return;
  }
%>


<%@ page 
	import = "java.text.*"
	import = "com.freewinesearcher.online.Search"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Searchset"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Auditlogger"
	import = "com.freewinesearcher.online.Ad"
%>

<%
	PageHandler p=PageHandler.getInstance(request,response,"Pageload");
	Auditlogger al=new Auditlogger(request);
	Webactionlogger logger;
	session.removeAttribute("image");	
	String banneraction=Webroutines.filterUserInput(request.getParameter("banneraction"));
	if (banneraction==null) banneraction="";
	String bannerid=Webroutines.filterUserInput(request.getParameter("bannerid"));
	if (bannerid==null) bannerid="";
	String cpcstring=Webroutines.filterUserInput(request.getParameter("cpc"));
	String message="";
	boolean cpcupdated=false;
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	ArrayList<ArrayList<String>> bannerinfo=Webroutines.bannerInfo(al.partnerid);
%>
<html>
<head>
<title>Manage banner and sponsored clicks for <%=Webroutines.getShopNameFromShopId(al.shopid,"")%></title>
</head>
<body>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/textpagenosearch.jsp" %>

<%
	message=(String)session.getAttribute("message");
	if (message!=null&&!message.equals("")){
		out.write(message+"<br/>");
	}
	session.removeAttribute("message");
	message="";
	


if (al.shopid==0) {
	out.write("Your account is not linked to any shop. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 

} else if (al.adenabled==false) {
	response.sendRedirect("/shops/agreement.jsp");
}else{
	
%>
Manage advertising for <%=Webroutines.getShopNameFromShopId(al.shopid,"")%><br/><br/>
<%
	float cpc=(float)-1;
if (cpcstring!=null&&!cpcstring.equals("")){
	try{
		cpcstring=cpcstring.replace(",",".");
		cpc = (float)Math.round(Float.parseFloat(cpcstring)*100)/100;
	} catch (Exception e){
		message="There is an error in the amount for Cost Per Click ("+cpcstring+")."; 
		Dbutil.logger.error("Cannot parse float value "+cpcstring+" for shopid "+al.shopid,e);
	}
}
if (cpc>=0){
	if (cpc<0.25&&cpc>0){
		message="The minimum amount for sponsored links is &euro; 0,25.";
	} else {
		if (cpc>10){
	message="The maximum amount for sponsored links is &euro; 10,00.";
		} else {
	al.setAction("CPC update");
	al.setObjecttype("Ranking");
	al.setOldvalue(Webroutines.getLinkBid(al.shopid)+"");
	al.setNewvalue(cpc+"");
	if (Webroutines.setLinkBid(al.shopid,cpc)==cpc) cpcupdated=true;
	if (cpcupdated){
		logger=new Webactionlogger("CPC update from "+Webroutines.getLinkBid(al.shopid)+" to "+cpc+" for shopid "+al.shopid,request.getServletPath(),p.ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
		logger.logaction();
		al.run();
	}
		}
	}
}
%><h4><font color='red'><%=message%></font></h4>
<%
	message="";
%>
<h3>Cost per Click for sponsored links</h3>
Current cost per click for sponsored clicks: <%=format.format(Webroutines.getLinkBid(al.shopid))%> &euro; <%
 	if (cpcupdated) out.print("&nbsp;<b>(Updated)</b>");
 %><br/><br/>
To change the cost per click, enter a new amount in Euro below (minimum is &euro 0,25), and press update. To stop sponsoring links, enter an amount of 0,00.<br/>
<form action="admanagement.jsp" method="POST">New bid: <input name="cpc" type="text" style="width: 70px;" value="<%=format.format(Webroutines.getLinkBid(al.shopid))%>"/>&nbsp;&euro;&nbsp;&nbsp;&nbsp;&nbsp;<input type="submit" value="Update"/></form>
<br/><br/>
<h3>Banners</h3>
<form action="bannerupload.jsp" method="POST"><input type="submit" value="Add a new banner"/></form><br/>

<%
	if (!bannerid.equals("")&&!banneraction.equals("")){
		al.setAction("Set banner status");
		al.setObjecttype("Banner");
		al.setObjectid(bannerid);
		if (banneraction.equals("Deactivate")){
	al.setOldvalue("1");
	al.setNewvalue("0");
	al.setInfo("Price shown: "+Ad.getBannerPrice(bannerid,al.partnerid));
	Ad.setBannerAction(bannerid,0,al.partnerid,al);
		}
		if (banneraction.equals("Activate")){
	al.setOldvalue("0");
	al.setNewvalue("1");
	al.setInfo("Price shown: "+Ad.getBannerPrice(bannerid,al.partnerid));
	Ad.setBannerAction(bannerid,1,al.partnerid,al);
		}
	}
bannerinfo=Webroutines.bannerInfo(al.partnerid);
for (int i=0;i<bannerinfo.size();i++){
%>
<%=bannerinfo.get(i).get(1) %>
<br/>This banner is currently <%=(bannerinfo.get(i).get(5).equals("1")?"active":"inactive") %>.
To <%=(bannerinfo.get(i).get(5).equals("0")?"activate":"deactivate")%>, press the button. <form action="admanagement.jsp" method="POST"><input type="submit" name="banneraction" value="<%=(bannerinfo.get(i).get(5).equals("0")?"Activate":"Deactivate")%>"><input type="hidden" name="bannerid" value="<%=(bannerinfo.get(i).get(0))%>"></form>
Pay per click: &euro; <%=bannerinfo.get(i).get(2)%>.
<br/>Total number of views: <%=bannerinfo.get(i).get(3)%>.
<br/>Total number of clicks: <%=bannerinfo.get(i).get(4)%>.
<br/><br/> 
	<%} %>	
<%} %>	
<%@ include file="snippets/footer.jsp" %>
</body>
</html>

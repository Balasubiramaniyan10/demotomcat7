
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

<h3>Banners</h3>
<form action="bannerupload.jsp" method="POST"><input type="submit" value="Upload a banner"/></form><br/>

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
<br/>Total number of views: <%=bannerinfo.get(i).get(3)%>.
<br/>Total number of clicks: <%=bannerinfo.get(i).get(4)%>.
<br/><br/> 
	<%} %>	
<%} %>	
<%@ include file="snippets/footer.jsp" %>
</body>
</html>

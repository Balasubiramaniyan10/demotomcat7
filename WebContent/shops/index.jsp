
<%@page import="java.util.Map"%>
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
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Auditlogger"
%>

<%	PageHandler p=PageHandler.getInstance(request,response,"Pageload Shop index");
	Auditlogger al=new Auditlogger(request);
	al.setAction("Shop index");
	al.setObjecttype("Shop");
	if (request.isUserInRole("admin")&&(request.getParameter("partnerid")!=null||request.getParameter("shopid")!=null)){
		int shopid=0;
		if (request.getParameter("partnerid")==null&&request.getParameter("shopid")!=null){
			shopid=Integer.parseInt(request.getParameter("shopid"));
			al.setPartnerid(Webroutines.saveShopAsPartner(shopid));
			al.setShopid(shopid);
		}else {
			try{al.setPartnerid(Integer.parseInt(request.getParameter("partnerid")));
			shopid=Dbutil.readIntValueFromDB("select * from partners where id="+al.getPartnerid(),"shopid");
			} catch (Exception e){}
		}
		if (al.getPartnerid()>0){
			session.setAttribute("overrideshopid",shopid);
			session.setAttribute("overridepartnerid",al.getPartnerid());
			
		}
	}
	al.setObjectid(al.shopid+"");
	
	al.logaction();
	session.removeAttribute("image");	
	
%>
<html>
<head>
<title>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></title>
<%@ include file="/header2.jsp" %>
<style type="text/css">
table.padded-table td { 
	padding:10px; 
	}
</style>

</head>
<body>

<%@ include file="/snippets/textpagenosearch.jsp" %>
<%
String banneraction=Webroutines.filterUserInput(request.getParameter("banneraction"));
if (banneraction==null) banneraction="";
String bannerid=Webroutines.filterUserInput(request.getParameter("bannerid"));
if (bannerid==null) bannerid="";
ArrayList<ArrayList<String>> bannerinfo=Webroutines.bannerInfo(al.partnerid);
	Webactionlogger logger;
	logger=new Webactionlogger("Shop overview",request.getServletPath(),al.ip, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	logger.logaction();
%>

<%if (al.partnerid==0) {
	out.write("Your account is not linked to any shop or partner. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
} else { 
if (request.isUserInRole("admin")){
	Map<Integer,String> partners=Webroutines.getPartners();
	Map<Integer,String> shops=Webroutines.getShopsWithoutPartnerId();
	%>
	Known partner: <form action="" method="post"><select name ="partnerid"><%
	for (Integer pid:partners.keySet()){
		out.write("<option value='"+pid+"'>"+partners.get(pid)+"</option>");
	}
	%></select><input type='submit' value='Change'/></form>
	Existing store: <form action="" method="post"><select name ="shopid"><%
	for (Integer sid:shops.keySet()){
		out.write("<option value='"+sid+"'>"+shops.get(sid)+"</option>");
	}
	%></select><input type='submit' value='Change'/></form>
	<% 
	
}

%>
<h3>Personal page for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></h3> 

<h3>Banners</h3>
<% bannerinfo=Webroutines.bannerInfo(al.partnerid);
	if (bannerinfo.size()==0){%>
You have not yet created a banner. 	

<%
	}%>
	<form action='editbanner.jsp' method='post'><input type='submit'value='Create new banner'/></form>
	<%
	if (!bannerid.equals("")&&!banneraction.equals("")){
		al.setAction(banneraction);
		al.setObjecttype("Banner");
		al.setObjectid(al.shopid+"");
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
		if (banneraction.equals("Activate")){
			al.setOldvalue("0");
			al.setNewvalue("1");
			al.setInfo("Price shown: "+Ad.getBannerPrice(bannerid,al.partnerid));
			Ad.setBannerAction(bannerid,1,al.partnerid,al);
		}
	}

for (int i=0;i<bannerinfo.size();i++){
%>
<table class='padded-table'><tr><td>
<a href='editbanner.jsp?bannerid=<%=bannerinfo.get(i).get(0) %>'><%=bannerinfo.get(i).get(1) %></a>
<br/><form action='editbanner.jsp?bannerid=<%=bannerinfo.get(i).get(0) %>' method='post'><input type='submit'value='Edit banner'/></form></td><td>
Links to: <%=bannerinfo.get(i).get(6)%>
<br/>Target countries:<% for (String c:bannerinfo.get(i).get(7).split(",")) out.write(Webroutines.getCountryFromCode(c)+" "); %>
<br/>Number of views: <%=bannerinfo.get(i).get(3)%>.
<br/>Number of clicks: <%=bannerinfo.get(i).get(4)%>.
<br/><br/> </td></tr></table>
	<%} %>	
<%} %>	
<%@ include file="snippets/footer.jsp" %> 
</body>
</html>

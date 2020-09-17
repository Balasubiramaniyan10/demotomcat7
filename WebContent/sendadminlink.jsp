<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>

<%@page import="com.freewinesearcher.online.Auditlogger"%>
<%@page import="com.freewinesearcher.batch.Emailer"%><html>
<head>
<title>
Send link for administration of data feed
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Send admin link");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<%@ include file="/snippets/textpage.jsp" %>
<% 	int shopid=0;
	try{shopid=Integer.parseInt(request.getParameter("shopid"));} catch (Exception e){}
	String targetpage=request.getParameter("targetpage");
	if (shopid>0){
		String email=Dbutil.readValueFromDB("select * from shops where id="+shopid,"email");
		if (email.contains("@")){
			String message="<html><body>Hi,<br/><br/>We have received a request to edit the data feed on Vinopedia.com for "+Webroutines.getShopNameFromShopId(shopid,"")+". To access your data feed, click on the following link: <a href='https://www.vinopedia.com/"+targetpage+"?"+Auditlogger.getAdminLink(shopid)+"'>https://www.vinopedia.com/editdatafeed.jsp?"+Auditlogger.getAdminLink(shopid)+"</a>.<br/><br/>Kind regards,<br/><br/>Vinopedia";
			Emailer emailer=new Emailer();
			if (emailer.sendEmail("do_not_reply@vinopedia.com",email, "Vinopedia data feed", message)){
				out.write("An email has been sent to "+email+".");
			} else {
				out.write("Sorry... there was a problem while trying to send the mail. Please try again later.");
			}
			
		} else {
			out.write("Sorry... No email address has been registered for this shop. Could not send the mail.");
			
		}
	} else {
		out.write("Sorry... Could not find the correct information.");

	}
	request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>




<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	
</div>
</body> 
</html>
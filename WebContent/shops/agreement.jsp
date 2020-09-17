<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%><%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.Auditlogger"
import = "com.freewinesearcher.online.Ad"
%><%
PageHandler p=PageHandler.getInstance(request,response,"Accept agreement");
Auditlogger al=new Auditlogger(request);
boolean accepted=false;
if ("on".equals(request.getParameter("accepted"))) accepted=true;
String fullname=Webroutines.filterUserInput(request.getParameter("fullname"));
if (fullname==null) fullname="";
String message="";
if (accepted&&!"".equals(fullname.trim())){
	if (!Ad.acceptAdAgreement(al,fullname,accepted)){
		message="<br/>Something went wrong while tried to enable the Ad management console for you. Please try again later.";
	}
}
%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN" xml:lang="EN">
<head>
<title>vinopedia</title>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/textpagenosearch.jsp" %>
</head>
<body>
<%
if (al.shopid==0) {	out.write("Your account has not yet been enabled to handle advertisements. Please <a href='/contact.jsp'>contact us</a> if you feel this is an error."); } else {
if (!al.adenabled){%>
<h2>Welcome to the vinopedia Ad management console</h2>
Before you can access the ad management console, you must accept the advertising agreement. <br/>
<a href='advertisingagreement.jsp' target='_blank'>Click here</a> to read the agreement.

<form action='agreement.jsp' method='post'>
<br/>Full name:<input type="text" name='fullname' value='<%=fullname %>'/>
<br/><input type="checkbox" name='accepted'/>I have read the advertising agreement and agree to be bound to it. In this I represent <%=al.partnername%>.
<br/><input type='submit' value='Submit'/>
<form>
<%=message%>
<%@ include file="snippets/footer.jsp" %>
</body>	
<%		} else { //!al.adenabled
			response.sendRedirect("/shops/index.jsp");
		}
	}// shopid==0 %>

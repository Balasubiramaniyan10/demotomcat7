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
	import = "com.freewinesearcher.online.invoices.*"
%>

<%
	PageHandler p=PageHandler.getInstance(request,response,"Financial overview");
	Auditlogger al=new Auditlogger(request);
	al.setAction("Financial overview");
	al.setObjecttype("Shop");
	al.logaction();
	String message="";
	NumberFormat format  = new DecimalFormat("#,##0.00");
%>
<html>
<head>
<title>Financial overview for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></title>
<%@ include file="/header2.jsp" %>
</head>
<body>
<%@ include file="/snippets/textpagenosearch.jsp" %>

<%
	message=(String)session.getAttribute("message");
	if (message!=null&&!message.equals("")){
		out.write(message+"<br/>");
	}
	session.removeAttribute("message");
	message="";
	
if (al.partnerid==0) {
	out.write("Your account is not linked to any partner. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 

} else { 
	ArrayList<Invoice> invoices=Invoice.getInvoices(al.partnerid);
	out.write("<table><th>Invoice id</th><th>Amount</th><th>Status</th><th>Due date</th>");
	for (Invoice invoice:invoices){
		out.write("<tr><td>"+invoice.getId()+"</td><td>&euro; "+format.format(invoice.getTotalamount())+"</td><td>"+invoice.getStatus()+"</td><td>"+invoice.getDuedate()+"</td><td><a href='showinvoice.jsp?invoiceid="+invoice.getId()+"'>Show details</a></td></tr>");	
	}
	out.write("</table>");

%>
<%
%><h4><font color='red'><%=message %></font></h4>
<%message=""; %>

<br/><br/>
<br/><br/> 
<%} %>	
<%@ include file="snippets/footer.jsp" %>
</body>
</html>

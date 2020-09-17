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
	import = "com.freewinesearcher.online.invoices.Invoice"
%>

<%
	Webactionlogger logger;
	int shopid=0;
	shopid=Webroutines.getShopFromUserId(request.getRemoteUser());
	int partnerid=0;
	partnerid=Webroutines.getPartnerFromUserId(request.getRemoteUser());
	String message="";
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	int invoiceid=0;
	try {
		invoiceid=Integer.parseInt(Webroutines.filterUserInput(request.getParameter("invoiceid")));
	} catch (Exception e){
		Dbutil.logger.error("Financial.jsp could not process invoiceid"+invoiceid,e);
	}
	PageHandler p=PageHandler.getInstance(request,response,"Invoice details");
	
%>
<html>
<head>
<title>Invoice details</title>
	<%@ include file="/header2.jsp" %>
	</head><body>
	<%@ include file="/snippets/textpagenosearch.jsp" %>

<%
	message=(String)session.getAttribute("message");
	if (message!=null&&!message.equals("")){
		out.write(message+"<br/>");
	}
	session.removeAttribute("message");
	message="";
	Auditlogger al=new Auditlogger(request);
	al.setPartnerid(partnerid);
	al.setShopid(shopid);
	al.setUserid(request.getRemoteUser());


if (partnerid==0) {
	out.write("Your account is not linked to any partner. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 

} else { 
	
	%><h4><font color='red'><%=message %></font></h4>
	<%message=""; %>
	<%
	if (invoiceid==0) {
		out.write("Sorry... we could not retrieve that invoice. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
	} else {	

	Invoice invoice=new Invoice(invoiceid,partnerid);
	if (invoice.validinvoice){
		out.write("<a href='/shops/invoices/"+invoice.getLastinvoicefilename()+"' target='_blank'>Show invoice in PDF format</a>");
		out.write("<table><th>Invoice id</th><th>Total Amount</th><th>Status</th><th>Due date</th>");
		out.write("<tr><td>"+invoice.getId()+"</td><td>&euro; "+format.format(invoice.getTotalamount())+"</td><td>"+invoice.getStatus()+"</td><td>"+invoice.getDuedate()+"</td></tr>");	
		out.write("</table>");
	} else {
		out.write("Sorry... we could not retrieve this invoice.");
		
	}

%>


<br/><br/>
<br/><br/> 
	<%} %>	
<%} %>	
<%@ include file="snippets/footer.jsp" %>
</body>
</html>

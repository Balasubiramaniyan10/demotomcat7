
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
	import = "com.freewinesearcher.online.Partner"
%>

<%
	PageHandler p=PageHandler.getInstance(request,response,"Pageload");
	Auditlogger al=new Auditlogger(request);
	al.setAction("Financial overview");
	al.setObjecttype("Shop");
	al.logaction();
	String message="";
	NumberFormat format  = new DecimalFormat("#,##0.00");
	SimpleDateFormat sdf=new SimpleDateFormat("d MMM yyyy");
	
	String action=request.getParameter("action");
	if (action==null) action="";
	int partnerid=0;
	try{partnerid=Integer.parseInt(request.getParameter("partnerid"));}catch(Exception e){}
	if (action.equals("sendinvoice")&&partnerid>0){
		Invoice invoice=new Invoice(partnerid,true,request);
		if (invoice.validinvoice){
			message+="Invoice successfully sent.<br/>";
		} else {
			message+="Problem while creating or sending invoice!<br/>";
		}
	}
	
	
%>
<html>
<head>
<title>Financial overview</title>
</head>
<body>
<%@ include file="/header2.jsp" %>


<%
	if (!message.equals("")) out.write ("<h1><font color='red'>"+message+"</font></h1>");
	ArrayList<Invoice> invoices=Invoice.getInvoices(al.partnerid);
	out.write("<h2>Open invoices</h2>");
	out.write("<table><th>Invoice id</th><th>Amount</th><th>Status</th><th>Due date</th>");
	for (Invoice invoice:invoices){
		out.write("<tr><td>"+invoice.getId()+"</td><td>&euro; "+format.format(invoice.getTotalamount())+"</td><td>"+invoice.getStatus()+"</td><td>"+sdf.format(invoice.getDuedate())+"</td><td><a href='/shops/showinvoice.jsp?invoiceid="+invoice.getId()+"'>Show details</a></td></tr>");	
	}
	out.write("</table>");

	ArrayList<Invoice.Debitor> duelist=Invoice.getDuePartners(true);
	out.write("<h2>New invoices to send</h2>");
	out.write("<table><th>Partner</th><th>Amount</th><th>Since</th><th>Action</th>");
	for (Invoice.Debitor debitor:duelist){
		Partner partner=new Partner(debitor.partnerid);
		out.write("<tr><td>"+partner.name+"</td><td>"+format.format(debitor.amount)+"</td><td>"+sdf.format(debitor.since.getTime())+"</td><td><a href='financialoverview.jsp?action=sendinvoice&partnerid="+debitor.partnerid+"'>Send invoice</a></td></tr>");	
	}
	out.write("</table>");
	duelist=Invoice.getDuePartners(false);
	out.write("<h2>Accounts not yet due</h2>");
	out.write("<table><th>Partner</th><th>Amount</th><th>Since</th><th>Action</th>");
	for (Invoice.Debitor debitor:duelist){
		Partner partner=new Partner(debitor.partnerid);
		out.write("<tr><td>"+partner.name+"</td><td>"+format.format(debitor.amount)+"</td><td>"+sdf.format(debitor.since.getTime())+"</td><td><a href='financialoverview.jsp?action=sendinvoice&partnerid="+debitor.partnerid+"'>Send invoice</a></td></tr>");	
	}
	out.write("</table>");

%>


<br/><br/>
<br/><br/> 
</body>
</html>

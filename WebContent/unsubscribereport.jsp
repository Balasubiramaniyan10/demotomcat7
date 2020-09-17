<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.batch.sms.Sms"
import = "com.freewinesearcher.batch.StoreReport"
import = "com.freewinesearcher.common.Configuration"
%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%PageHandler p=PageHandler.getInstance(request,response,"Unsubscribe report");%><!DOCTYPE HTML>
<head>
<title></title>
<meta name="keywords" content="Unsubscribe from traffic report" />
<meta name="description" content="Unsubscribe from traffic report" />
<%@ include file="/header2.jsp" %>
</head>
<body  onload="javascript:doonload();">
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<div class='main'>
	<div id='mainleft'>	
	<noscript><img src='/images/nojs.gif' alt=''/></noscript>
	<% 
int shopid=0;
String code=request.getParameter("authorizationcode");
try{shopid=Integer.parseInt(request.getParameter("store"));}catch(Exception e){};
if (StoreReport.codeOK(code,shopid)||request.isUserInRole("admin")) {
	if	(StoreReport.unsubscribe(shopid)){
		%>You have been unsubscribed from the Vinopedia traffic reports.<% 
	p.getLogger().shopid=shopid+"";	
	} else {
		%>Your authorization code was correct but an error occurred on our side. Please try again later.<%
		Dbutil.logger.error("Could not unsubscribe shop "+shopid);
				new Sms("Could not unsubscribe shop "+shopid);
	}
} else {
	%>Your authorization code is not correct.<%
}
%>
<%@ include file="/snippets/footer.jsp" %>	
		</div></div> <%// workaround: IE positioning of footer %>
		</div>
		</div> <!--  main--> 
<%} %>
	 <script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>
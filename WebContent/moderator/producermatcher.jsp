<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.common.Configuration"
import=" com.freewinesearcher.batch.sitescrapers.Matchers"
%><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%PageHandler p=PageHandler.getInstance(request,response,"Retailer options");%><!DOCTYPE HTML>
<head>
<title></title>
<meta name="keywords" content="How to get listed as a retailer" />
<meta name="description" content="How to get listed as a retailer" />
<%@ include file="/header2.jsp" %>
</head>
<body  onload="javascript:doonload();">
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<%@ include file="/snippets/topbar.jsp" %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div>
<div class='main'>
	<div id='mainleft'>	
	<noscript><img src='/images/nojs.gif' alt=''/></noscript>
<%Matchers.saveMatch((String)request.getParameter("kbname"), (String)request.getParameter("ctname")); %>
<%=Matchers.getProducerMatcher()%>


<%@ include file="/snippets/footer.jsp" %>	
		</div></div> <%// workaround: IE positioning of footer %>
		</div>
		</div> <!--  main--> 
<%} %>
	 <script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body>
</html>
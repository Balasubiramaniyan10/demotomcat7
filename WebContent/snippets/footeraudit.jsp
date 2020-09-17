<%@page import="com.freewinesearcher.online.Auditlogger"%></div>
<% if (request.getAttribute("al")!=null){
	((Auditlogger)request.getAttribute("al")).logaction();
}


%>
<%@ include file="/snippets/footer.jsp" %>	
</div> <!--  main-->
			

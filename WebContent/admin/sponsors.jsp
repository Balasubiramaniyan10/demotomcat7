<%@page import="com.freewinesearcher.batch.SponsorStats"%>
<%@page import="com.freewinesearcher.online.Webroutines"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<html>
<head>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js" ></script><script type="text/javascript" src="<%=request.isSecure()?"":Configuration.static2prefix%>/js/combinedjs4.js?version=25" defer="defer"></script>

<body><%@page import="com.freewinesearcher.batch.StoreReport"%><%@page import="com.freewinesearcher.common.datamining.Chart"%>

<h1>Overview of store sponsorhips</h1>
<%=Webroutines.sponsorships() %>
<%@ include file="/snippets/footer.jsp" %>
</body></html>
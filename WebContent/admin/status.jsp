<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	%><html>
<head>
<title>
Status
</title>
<jsp:include page="/header.jsp" />
<jsp:include page="adminlinks.jsp" />
<br/>

<% out.write(Webroutines.getStatusHTML()); %>
</div>
</body> 
</html>
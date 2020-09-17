<html>
<head>
<%@ include file="/header2.jsp" %>
</head>

<body>
<h1>iFrame</h1>
<br/><a href='link.jsp?exturl=http://www.stardrifter.org/cgi-bin/ref.cgi'>External link</a>
<iframe style='width:800px;height:600px;' src="http://www.stardrifter.org/cgi-bin/ref.cgi"  ></iframe>
	<jsp:include page="/snippets/footer.jsp" flush="true" />
</body></html>
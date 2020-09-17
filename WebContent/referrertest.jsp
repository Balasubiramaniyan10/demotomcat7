<html>
<head>
<%@ include file="/header2.jsp" %>
</head>

<body>
<h1>Test voor referrer</h1>
<br/><a href='link.jsp?exturl=http://www.stardrifter.org/cgi-bin/ref.cgi'>External link</a>
<br/><a href='referrertest.jsp?clicked=true'>This page</a>
<iframe style='width:900px;height:700px;' src="referreriframe.jsp"  ></iframe>
	<jsp:include page="/snippets/footer.jsp" flush="true" />
</body></html>
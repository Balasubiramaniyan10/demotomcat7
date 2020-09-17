<%@	page 
	
	import = "com.freewinesearcher.common.Knownwines"
	
	%>
	<%
	if (request.getParameter("full")==null)	out.write("alert(\""+Knownwines.testKnownWine(request.getParameter("id"))+"\");");
	if (request.getParameter("full")!=null)	out.write("alert(\""+Knownwines.testWines(request.getParameter("id"),request.getParameter("full"),request.getParameter("lit"),request.getParameter("litex"))+"\");");
	%>
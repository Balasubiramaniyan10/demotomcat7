<%@ page 

	import = "com.freewinesearcher.common.Knownwines"
%>

<%	String row = request.getParameter("row");
String term = request.getParameter("term");
String winename = request.getParameter("winename");
term=term.replaceAll(" ","+");
	out.write("document.getElementById(\""+row+"\").innerHTML=\""+Knownwines.removeTerm(row,term,winename).replaceAll("\"","\\\\\"")+"\";");

	%>
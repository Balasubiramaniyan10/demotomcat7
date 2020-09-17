<%@ page 

	import = "com.freewinesearcher.common.Knownwines"
%>

<%	String row = request.getParameter("row");
	String term = request.getParameter("term");
	String ratedwineid = request.getParameter("ratedwineid");
	String improved = request.getParameter("improved");
	if (ratedwineid==null) ratedwineid="";
	if (improved==null) improved="";
	term=term.replaceAll(" ","+");
	improved=improved.replace("  "," +");
	if (improved.equals("")){
		out.write("document.getElementById(\""+row+"\").innerHTML=\""+Knownwines.removeTerm(row,term,ratedwineid).replaceAll("\"","\\\\\"")+"\";");
	} else {
		out.write("document.getElementById(\""+row+"\").innerHTML=\""+Knownwines.removeImprovedTerm(term,row,improved).replaceAll("\"","\\\\\"")+"\";");
	}
	%>
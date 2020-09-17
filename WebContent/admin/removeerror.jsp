<%@ page 

	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.batch.Spider"
	
%>

<%	String actie=request.getParameter("actie");
	String id = request.getParameter("id");
	if (actie.equals("handle")){
		Dbutil.executeQuery("Update errorlog set handled=1 where id="+id);
		%><%=("document.getElementById(\"error"+id+"\").innerHTML=\"\";")%><%
		}
	if (actie.equals("handleall")){
		Dbutil.executeQuery("Update errorlog set handled=1");
		%><%=("document.getElementById(\"errortable\").innerHTML=\"\";")%><%
		}
		
	%>
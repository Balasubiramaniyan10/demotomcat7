<%@ page 

	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.batch.Spider"
	
%>

<%
	String actie=request.getParameter("actie");
	String id = request.getParameter("id");
	String value = request.getParameter("value");
	if (actie.equals("literal")){
%><%=("document.getElementById(\""+id+"\").innerHTML=\""+Spider.replaceString(Knownwines.updateLiteral(id,value),"\"","\\\"")+"\";")%><%
	}
	if (actie.equals("fulltext")){
		value=Spider.replaceString(value,"|","+");
%><%=("document.getElementById(\""+id+"\").innerHTML=\""+Spider.replaceString(Knownwines.updateFulltext(id,value),"\"","\\\"")+"\";")%><%
	}
	if (actie.equals("literalexclude")){
%><%=("document.getElementById(\""+id+"\").innerHTML=\""+Spider.replaceString(Knownwines.updateLiteralExclude(id,value),"\"","\\\"")+"\";")%><%
		}
	if (actie.equals("disable")){
		%><%=("document.getElementById(\""+id+"\").innerHTML=\""+Knownwines.disableKnownwine(id)+"\";")%><%
		}
		
%>
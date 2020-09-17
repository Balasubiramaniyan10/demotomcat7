<%@ page 

	import = "com.searchasaservice.ai.AiHtmlRefiner"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Context"
	
%>

<%	boolean success=false;
	String actie=request.getParameter("actie");
	String idstr = request.getParameter("id");
	int id=0;
	try {id=Integer.parseInt(idstr);}catch(Exception e){}
	if (id>0&&actie!=null&&actie.equals("disable")){
		Context c=new Context(request);
		AiHtmlRefiner r=new AiHtmlRefiner(c.tenant,new Recognizer("name","itemid","wineid","aiwines",1,true));
		success=r.disOrEnableKbKnownwine(id,true);
	}
	if (id>0&&actie!=null&&actie.equals("enable")){
		Context c=new Context(request);
		AiHtmlRefiner r=new AiHtmlRefiner(c.tenant,new Recognizer("name","itemid","wineid","aiwines",1,true));
		success=r.disOrEnableKbKnownwine(id,false);
	}
		if (success){
			if (actie.equals("disable")){
				%>
				document.getElementById('kb<%=id %>').innerHTML="
<%@page import="com.searchasaservice.ai.Recognizer"%><a onClick='javascript:enableWine(<%=id%>);' style='cursor:pointer'>Enable</a>";
				<%
			} else {
				%>
				document.getElementById('kb<%=id %>').innerHTML="<a onClick='javascript:disableWine(<%=id%>);' style='cursor:pointer'>Disable</a>";
				<% 
			}

		} else {
			%>
			
			alert("Problem updating kbknownwine. id=<%=id %>, action=<%=actie %>");
						
			<%
	}
	
		
	%>
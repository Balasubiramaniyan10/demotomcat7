
<%@page import="com.searchasaservice.ai.Recognizer"%><%@ page 

	import = "com.searchasaservice.ai.AiHtmlRefiner"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Context"
	
%>

<%
	String fts=request.getParameter("fts");
	String regex=request.getParameter("regex");
	String regexexcl=request.getParameter("regexexcl");
	String idstr = request.getParameter("id");
	int id=0;
	try {id=Integer.parseInt(idstr);}catch(Exception e){}
	if (id>0&&fts!=null&&regex!=null&&regexexcl!=null){
		Context c=new Context(request);
		AiHtmlRefiner r=new AiHtmlRefiner(c.tenant,new Recognizer("name","itemid","wineid","aiwines",1,true));
		boolean success=r.updateAiRecognizerManual(id,fts,regex,regexexcl);
		if (success){
			
		} else {
			%>
			
			alert("Problem updating airecognizermanual. id=<%=id %>, fts=<%=fts %>, regex=<%=regex %>, regexexcl=<%=regexexcl %>");
						
			<%
		}
	}
		
	%>
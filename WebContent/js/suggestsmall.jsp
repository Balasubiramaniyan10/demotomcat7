<%@ page   
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%>
<% String input=Webroutines.filterUserInput(request.getParameter("input"));
	out.print(Webroutines.autoSuggest(input));
	%>
	
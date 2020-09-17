<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@ page   
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	
	
%><% String input="";
	request.setCharacterEncoding("UTF-8");
	
	try{	
		input=Webroutines.filterUserInput(Webroutines.removeAccents(request.getParameter("input")));
	}catch(Exception e){
		Dbutil.logger.warn("Cannot parse request parameter: not UTF-8");
	}
		if (input!=null&&!"".equals(input)){
		out.print(input+"\n");
		out.print(Webroutines.autoSuggest(input.replaceAll("\\d","")));
	}else {
		try{
		input=Webroutines.filterUserInput(Webroutines.removeAccents(Webroutines.utf8Convert(request.getParameter("q"))));
		out.print(Webroutines.smartSuggest(input));
	}catch(Exception e){
		Dbutil.logger.warn("Cannot parse request parameter: not UTF-8");
	}
	}
	%>
	
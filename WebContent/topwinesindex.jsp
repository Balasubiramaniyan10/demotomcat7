<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<%
 	long startload=System.currentTimeMillis(); 
  	Webactionlogger logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
 %>
<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
	import = "com.freewinesearcher.online.Webactionlogger"	
%>

<%
	request.setCharacterEncoding("ISO-8859-1");
	session = request.getSession(true); 
	
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null) offset="0";
	int start=0;
	try{
		start=Integer.parseInt(offset);
	} catch (Exception e){} 
%>
<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title>Index to the top wines</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Topwines index");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
This is a list of the most popular wines on this site.<br/><br/>

<%int records=Webroutines.getNumberOfTopWines();
for (int i=0;i<records;i=i+100){
	out.write("<a href=\"/topwines.jsp?offset="+i+"\">Wine "+(i+1)+" to "+(Math.min(i+100,records))+"</a><br/>");
}
%>

<% 	logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
	%>
		

<%}%>	

<%@ include file="/snippets/textpagefooter.jsp" %>
</div>
</body> 
</html>
<% long endload=System.currentTimeMillis();

	logger.loadtime=((endload-startload));
	logger.logaction();
%>





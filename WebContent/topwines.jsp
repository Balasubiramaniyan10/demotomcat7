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
<title>Top wines</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Topwines");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
This is a list of the most popular wines on this site.<br/><br/>

<%=Webroutines.getTopWines(start) %>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
	<%	// Handle source IP address
	if (hostcountry.equals("NZ")||(Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp"))){
		if (hostcountry.equals("NZ")){
		out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
		logger=new Webactionlogger("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
		}
	} else { 
	logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
	%>
		
<br/><br/>
This is a list of the most popular wines on this site.<br/><br/>

<%=Webroutines.getTopWines(start) %>
<%}%>	
<%}%>			
</div>

</body> 
</html>
<% long endload=System.currentTimeMillis();

	logger.loadtime=((endload-startload));
	logger.logaction();
%>





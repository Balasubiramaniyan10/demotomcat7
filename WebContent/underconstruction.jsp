<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<%
 	long startload=System.currentTimeMillis(); 
  	Webactionlogger logger=new Webactionlogger("Searchasaservice", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
 %>
<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
	import = "com.freewinesearcher.online.Webactionlogger"	
%>

<%
	request.setCharacterEncoding("ISO-8859-1");
	session = request.getSession(true);
%>
<%@ page contentType="text/html; charset=ISO-8859-1" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title>Search as a Service</title>
<%@ include file="/header.jsp" %>
	<%	// Handle source IP address
	String originalpage=Webroutines.filterUserInput(request.getParameter("originalpage"));
	if (hostcountry.equals("NZ")||(Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp"))){
		if (hostcountry.equals("NZ")){
		out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
		logger=new Webactionlogger("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
		}
	} else { 
	logger=new Webactionlogger(originalpage, request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);
	%>
		
<br/><br/>
<h1><%=originalpage%> is being developed as we speak!</h1><br/><br/>
<%=originalpage%> will offer a specialized search engine for Port. This will be based on the extensive wine and port database of <a href='https://www.vinopedia.com'>vinopedia</a>. The search engine will understand the difference between "Vintage" and "LB Vintage", which makes searching for a special bottle a breeze. Please check back to find the most sophisticated search engine for Port in the beginning of 2009! .
<br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
<%}%>	
		
</div>

</body> 
</html>
<% long endload=System.currentTimeMillis();

	logger.loadtime=((endload-startload));
	logger.logaction();
%>





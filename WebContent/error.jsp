<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.PageHandler"
	import = "com.freewinesearcher.common.Configuration"
	
%>
<%	session = request.getSession(true); 
%>
<%	try{
	request.setAttribute("originalURL", (String)request.getAttribute("javax.servlet.forward.request_uri"));
	
	int errorcode=0;
	String originalURL="";
    boolean handled = false; // Set to true after handling the error
    
    // Get the PageContext
    if(pageContext != null) {
        // Get the ErrorData
        ErrorData ed = null;
        try {
            ed = pageContext.getErrorData();
        } catch(NullPointerException ne) {
            // If the error page was accessed directly, a NullPointerException
            // is thrown at (PageContext.java:514).
            // Catch and ignore it... it effectively means we can't use the ErrorData
        }
        if(ed != null) {
            errorcode=ed.getStatusCode();
            originalURL=ed.getRequestURI();
    
            // Error handled successfully, set a flag
            handled = true;
        }
    }
 
%>
<%	String ipaddress="";
    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
    	ipaddress = request.getRemoteAddr();
    } else {
        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
    }

    if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
    	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
    	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
		
    } else {
    if (originalURL.equals("")) originalURL=request.getServletPath();
		
     //Webroutines.logWebAction(String.valueOf(errorcode),originalURL,ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
	}	
} catch (Exception exc){}

String originalurl=(String)request.getAttribute("originalURL");
if (Configuration.detectSuspectedBot){
int botstatus=PageHandler.getInstance(request,response).getBotstatus();

if (botstatus>=2){
	
	response.setStatus(302);
	response.setHeader( "Location", "/check.jsp?targeturl="+Webroutines.URLEncodeUTF8(originalurl));
	response.setHeader( "Connection", "close" );
	return; 
}
%>
<jsp:include page="/index.jsp" />
<%
} else { 
%>
<jsp:include page="/index.jsp" />
<%}%>
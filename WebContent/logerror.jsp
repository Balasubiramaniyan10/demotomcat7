<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
%>
<%	session = request.getSession(true); 
%>
<%	
int errorcode=0;
String originalURL="";
try{
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
        } else {
        	originalURL=(String)request.getAttribute("originalURL");
        }
    }
 
%>
<%	String ipaddress="";
    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
    	ipaddress = request.getRemoteAddr();
    } else {
        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
    }

    
    if (originalURL.equals("")) originalURL=request.getServletPath();
		
} catch (Exception exc){
	
} finally {
	Dbutil.logger.error("JSP Exception data:\r\nStatus code:"+String.valueOf(errorcode)+"\r\nURL:"+originalURL+"\r\nIP:"+request.getRemoteAddr()+"\r\nX-IP:"+request.getHeader("HTTP_X_FORWARDED_FOR")+"\r\nQuery:"+(String)request.getAttribute("originalQueryString"));
	
}
try{
	%>
<jsp:include page="/index.jsp" />
<% }catch (Exception exc){
Dbutil.logger.error("While processing error page the following error occurred:",exc);	
}%>

<%@ page 
import = "com.freewinesearcher.online.PageHandler"
%><%
	PageHandler.getInstance(request,response).logger.logaction();
	PageHandler.getInstance(request,response).firstrequest=false;
	PageHandler.getInstance(request,response).cleanup();
%>
			
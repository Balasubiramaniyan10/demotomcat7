<%@ page 
import = "com.freewinesearcher.online.PageHandler"
%><%
	PageHandler.getInstance(request,response).logger.logaction();
	//PageHandler.getInstance(request,response).firstrequest=false;
	//PageHandler.getInstance(request,response).cleanup();
%><%if (false){ %><div data-role="footer" data-position="fixed"><a href="/" class="hlink"  title="Full web version">Full version</a></div><!-- /footer --><%}%>
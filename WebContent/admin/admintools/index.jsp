<%@page import="com.freewinesearcher.common.ConnectionTracker"%>
<%String action=request.getParameter("action");
	if (action==null) action="";
	if (action.equals("loggingon")) Dbutil.trackconnections=true;
	if (action.equals("loggingoff")) {
		Dbutil.trackconnections=false;
		ConnectionTracker.clear();
	}
%><html><head>
<jsp:include page="/header2.jsp" />
</head><body>
<%@ include file="/snippets/textpage.jsp" %>
<jsp:include page="/admin/adminlinks.jsp" />
<form action="" method="post">
<input type='submit' value='Turn DB log on'/>
<input type='hidden' name='action' value='loggingon'/> 
 </form>
<form action="" method="post">
<input type='submit' value='Turn DB log off'/>
<input type='hidden' name='action' value='loggingoff'/> 
 </form>
<a href='viewdblog.jsp' target='_blank'>View logging</a> 
<%@ include file="/snippets/textpagefooter.jsp" %>
</body></html>
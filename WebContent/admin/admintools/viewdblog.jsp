<%@page import="com.freewinesearcher.common.ConnectionTracker"%>

<html><head>
<jsp:include page="/header2.jsp" />
</head><body>

<%

for (int i:ConnectionTracker.connectionmap.keySet()){
	try{out.write(ConnectionTracker.connectionmap.get(i));}catch(Exception e){}
}
out.write("Maximum:<br/>");
for (int i:ConnectionTracker.maxconnectionmap.keySet()){
	try{out.write(ConnectionTracker.maxconnectionmap.get(i));}catch(Exception e){}
}
%>
</body></html>
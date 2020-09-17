
<%@page import="com.freewinesearcher.common.Dbutil"%><html><body>
Open Connections:<%=Dbutil.opened %>, connection map size: <%=Dbutil.connectionmap.size() %><br/>
<% 
for (String key:Dbutil.connectionmap.keySet()){
	out.write(key+":<br/>"+Dbutil.connectionmap.get(key)+"<br/>");
}
%>
Open Connections during maximum:<br/>
<% 
for (String key:Dbutil.maxconnectionmap.keySet()){
	out.write(key+":<br/>"+Dbutil.maxconnectionmap.get(key)+"<br/>");
}
%>
</body></html>
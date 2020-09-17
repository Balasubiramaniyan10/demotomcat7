<%@page import="java.sql.Timestamp"%><%@page import="com.freewinesearcher.common.Context"%><%@page import="com.freewinesearcher.online.ChangeLog"%><% int id=0;
try{id=Integer.parseInt(request.getParameter("id"));
	String rollforward=request.getParameter("rollforward");
	if (rollforward==null||!rollforward.equals("true")){
		out.write(ChangeLog.rollbackChanges(ChangeLog.getChanges(new Context(request),id),new Context(request)));
	} else {
		out.write(ChangeLog.rollforwardChanges(ChangeLog.getChanges(new Context(request),id),new Context(request)));
	}
}catch(Exception e){
String username=request.getParameter("username");
String tablename=request.getParameter("tablename");
Timestamp date=Timestamp.valueOf(request.getParameter("datefrom"));

if (username!=null&&tablename!=null&&date!=null){
	out.write(ChangeLog.rollbackChanges(ChangeLog.getChanges(new Context(request).tenant,username,tablename,date,false,true),new Context(request)));
}
}
%>
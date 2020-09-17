<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="com.freewinesearcher.online.ChangeLog"%>
<%@page import="java.sql.Timestamp"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<% PageHandler p=PageHandler.getInstance(request,response);
	String username=request.getParameter("username");
	if (username==null||username.equals("")) username="%";
	String tablename=request.getParameter("tablename");
	if (tablename==null||tablename.equals("")) tablename="%";
	boolean details=false;
	if ("on".equals(request.getParameter("details"))) details=true;
	int history=7;
	try{history=Integer.valueOf(request.getParameter("history"));} catch (Exception e){history=7;}
	if (request.getParameter("history")==null) history=7;
	Timestamp date=null;
	if (history>0){
	try{date=Timestamp.valueOf(ChangeLog.daystohistorydate(history));} catch (Exception e){}
	} else {
		date=Timestamp.valueOf("2009-01-01 00:00:00");
	}
	
%>
<head>
<title>

</title>
<meta name="description" content="" />
<%@ include file="/header2.jsp" %>
<script type="text/javascript">
function rollbackselection(tablename,userid,datefrom){
var rollback=confirm('Rollback all changes in this selection?');
  	  					if (rollback) $.ajax({
  	  	  					url:"/admin/rollbackchange.jsp", 
  	  	  					data:{tablename:tablename,username:userid,datefrom:datefrom},
  	  						success:function(data) {
  	  							if (data!='') {
  	  	  							alert("Could not rollback these changes: "+data);
  	  							} else {
									
  	  	  						}
  	  	  					},
  	  	  					error:function(data) {
  	  							alert("Error. Could not rollback changes.");
  	  							
  	  	  					}
  	  					});
}
</script>
</head>
<body>
<%@ include file="/snippets/topbar.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %></div>
<div class='main'>
<%	out.write(ChangeLog.getChangeSummary()+"<br/><br/>");
	ChangeLog.selectiongroups groups=new ChangeLog.selectiongroups();
	%><form action=''>
	<table><tr><th>Table</th><th>User</th><th>History</th><th>Details?</th><th></th><th></th></tr>
	<tr><td>
	<select name='tablename'>
	<option value="%" <%if (tablename.equals("%")) out.print(" selected='selected'");%>>All</option>
	<%
	for (String table:groups.tables){%>
	<option value="<%=table%>"<%if (tablename.equals(table)) out.print(" selected='selected'");%>><%=table%></option>
	<% }%>
	</select></td>
	<td><select name='username'>
	<option value="%" <%if (username.equals("%")) out.print(" selected='selected'");%>>All</option>
	<%
	for (String user:groups.users){%>
	<option value="<%=user%>"<%if (username.equals(user)) out.print(" selected='selected'");%>><%=user%></option>
	<% }%>
	</select></td>
	<td><select name='history'>
	<option value="0" <%if (history==0) out.print(" selected='selected'");%>>Unlimited</option>
	<option value="1" <%if (history==1) out.print(" selected='selected'");%>>1 days</option>
	<option value="7" <%if (history==7) out.print(" selected='selected'");%>>1 week</option>
	<option value="31" <%if (history==31) out.print(" selected='selected'");%>>1 month</option>
	<option value="365" <%if (history==365) out.print(" selected='selected'");%>>1 year</option>
	</select></td>
	<td><input type='checkbox' name='details' <%if (details) out.write("checked='checked' ");%>/></td>
	<td><input type='submit' value='Select'/></td><td><input type='button' onclick="rollbackselection('<%=tablename %>','<%=username %>','<%=date %>');" value='Rollback selection'/ ></td></tr></table>
	</form>
	<input type='button' onclick="rollbackselection('<%=tablename %>','<%=username %>','<%=date %>');" value='Rollback selection'/ >
	<% 
	if (date==null&&username.equals("%")) {
		out.write("Showing 14 day history, <a href='?datefrom=2009-01-01 00:00:00'>click here to view complete history (long load time)</a><br/><br/>");
		date=Timestamp.valueOf(ChangeLog.daystohistorydate(14));
	}
	if (date==null&&!username.equals("%")) {
		out.write("To rollback ALL actions from user "+username+" click <span onclick=\"javascript:$.post('/admin/rollbackchange.jsp','username="+username+"');\">here</span>.");
	}
	out.write(ChangeLog.getChangeHtml(ChangeLog.getChanges(1,username,tablename,date,true,details)));
%>
</div>
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>
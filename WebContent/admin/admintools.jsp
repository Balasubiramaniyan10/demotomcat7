<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Dbutil"
	import = "java.sql.Connection"
	import = "java.sql.ResultSet"
	import = "java.io.*"
	
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="com.freewinesearcher.batch.WakeOnLan"%>
<%@page import="com.freewinesearcher.batch.DeployNewVersion"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<title>
Admin overview <%=Wijnzoeker.version%>
</title>
<%@ include file="adminlinks.jsp" %>
<%
	String html="";
	session.setMaxInactiveInterval(120);
	String action=request.getParameter("action");
	if (action==null) action="";
	String loglevel=request.getParameter("loglevel");
	if (loglevel==null) loglevel="";
	String systemmessage=request.getParameter("systemmessage");
	if (systemmessage==null) systemmessage="";
	String query=request.getParameter("query");
	if (query==null) query="";
	if (action.equals("login")){
		String password=request.getParameter("password");
		if ("givemeaccess".equals(password)) {
	session.setAttribute("authorized","true");
		} else {
	Dbutil.logger.error("Unauthorized try to gain access to admintools");
		}
	}
	if ("logout".equals(action)) session.setAttribute("authorized",false);
	boolean authorized=false;
	if ("true".equals(session.getAttribute("authorized"))) authorized=true;
	if (!authorized){
%>
<body>
<form method="post" action='admintools.jsp' name="loginform">
  <table border="0" cellspacing="5">
    <tr>
      <th align="right">Please authorize yourself.</th>
      <th align="right">Password:</th>
      <td align="left"><input type="password" name="password"/></td>
    </tr>
    <tr>
      <td align="right"><input type="submit" value="Log In"/>
      <input type="hidden" name="action" value="login"/></td>
      <td align="left"><input type="reset"/></td>
    </tr>
  </table>
</form>
<%
	} else {
%>
<form method="post" action='admintools.jsp' name="logoutform">
<input type="submit" value="Log Out"/></td>
<input type="hidden" name="action" value="logout"/></td>
</form>
<form method="post" action='admintools.jsp' name="deployform">
	<input type="hidden" name="action" value="deploynewversion"/></td>
	<input type="submit" value="Deploy New Version"/></td>
</form>

<form method="post" action='admintools.jsp' name="restoreform">
	<input type="hidden" name="action" value="restorelastknowngood"/></td>
	<input type="submit" value="Restore Last Known Good"/></td>
</form>

<form method="post" action='admintools.jsp' name="wolform">
	<input type="hidden" name="action" value="wakeonlan"/></td>
	<input type="submit" value="Wake DEV On Lan"/></td>
</form>

<form method="post" action='admintools.jsp' name="loglevelform">
  <table border="0" cellspacing="5">
    <tr>
      <th align="left">Please enter new log level (DEBUG for debug, INFO for the rest)</th>
      <td align="left"><input type="text" name="loglevel" size='60'  /> 
      <input type="hidden" name="action" value="setloglevel"/></td>
      </td>
      <td><input type="submit" value="Submit"/></td>
    </tr>
  </table>
</form>
<%
	out.write("Debug is "+Wijnzoeker.debug);
	if (action.equals("setloglevel")){
		try{
	if (loglevel.equalsIgnoreCase("DEBUG")){
		Wijnzoeker.debug=true;
		Dbutil.changeLogLevel();
	} else {
		Wijnzoeker.debug=false;
		Dbutil.changeLogLevel();
	}
	Dbutil.logger.info("Debug is now "+Wijnzoeker.debug);
		} catch (Exception e){
	Dbutil.logger.error("Problem during change of loglevel.",e);
	out.write("Error occurred during change of loglevel:\n");
	out.write(e.getMessage());
		}
	}
%>
<form method="post" action='admintools.jsp' name="selectqueryform">
  <table border="0" cellspacing="5">
    <tr>
      <th align="left">Please enter SQL select query</th>
      <td align="left"><input type="text" name="query" size='60' <%if (action.equals("selectquery")) out.write("value=\""+query+"\"");%> /> <input type="hidden" name="action" value="selectquery"/></td>
      </td>
      <td><input type="submit" value="Submit"/></td>
    </tr>
  </table>
</form>
<form method="post" action='admintools.jsp' name="executequeryform">
  <table border="0" cellspacing="5">
    <tr>
      <th align="left">Please enter SQL execute query</th>
      <td align="left"><input type="text" name="query" size='60' <%if (action.equals("executequery")) out.write("value=\""+query+"\"");%>/> 
      <input type="hidden" name="action" value="executequery"/></td>
      <td><input type="submit" value="Submit"/></td>
    </tr>
  </table>
</form>
<%
	if (action.equals("systemmessage")){
	try{
		Dbutil.logger.info("Setting systemmessage to: "+systemmessage);
		int result=Dbutil.executeQuery("update config set value='"+Spider.SQLEscape(systemmessage)+"' where configkey='systemmessage';");
	} catch (Exception e){
		Dbutil.logger.error("Problem during update of systemmessage:",e);
		out.write("Error occurred during system message update:\n");
		out.write(e.getMessage());
	}
	out.write(html);
}
%>
<form method="post" action='admintools.jsp' name="systemmessageform">
  <table border="0" cellspacing="5">
    <tr><td align="left">System message: <%=Webroutines.getConfigKey("systemmessage")%></td></tr>
    <tr><td align="left">New system message:<input type="text" name="systemmessage" size='60' <%if (action.equals("systemmessage")) out.write("value=\""+systemmessage+"\"");%>/> <input type="hidden" name="action" value="systemmessage"/></td>
      <td><input type="submit" value="Submit"/></td>
    </tr>
  </table>
</form>
<%
if (action!=null&&!action.equals("")) out.write("Action taken:"+action);
if (action.equals("wakeonlan")){
	if (WakeOnLan.WolDev()){
		out.write("WOL packet sent");
	} else {
		out.write("Sending packet failed.");
	}
}
if (action.equals("deploynewversion")){
	out.write("Deploying new version.");
	
	DeployNewVersion.Deploy();
}

if (action.equals("restorelastknowngood")){
	out.write("Restoring Last Known Good");
	DeployNewVersion.RestoreLastKnownGood();
}

if (action.equals("selectquery")&&!"".equals(query)){
		ResultSet rs;
	Connection con=Dbutil.openNewConnection();
	try{
		Dbutil.logger.info("Performing selectquery in admintools.jsp: "+query);
		int columns=0;
		html="";
		rs=Dbutil.selectQuery(query,con);
		if (rs!=null){
		if (rs.next()){
	columns=rs.getMetaData().getColumnCount();
		}
		rs.beforeFirst();
		out.write("<table>");
		out.write("<tr>");
		for (int i=1;i<=columns;i++){
	out.write("<th>"+rs.getMetaData().getColumnName(i)+"</th>");
		}
		out.write("</tr>");
		while (rs.next()){
	out.write("\n<tr>");
	for (int i=1;i<=columns;i++){
		out.write("<td>"+rs.getString(i)+"</td>");
	}
	out.write("</tr>");
		}
		out.write("</table>");
		} else {
	out.write("Error occurred during selectquery. Check the <a href='/log/Tomcat.log'>Tomcat logging</a>.");
		}
	} catch (Exception e){
		Dbutil.logger.error("Problem during selectquery in admintools.jsp:",e);
		out.write("Error occurred during selectquery:\n");
		StringWriter stringWriter = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(stringWriter);
	    e.printStackTrace(printWriter);
	    out.write(stringWriter.toString());
	}
	Dbutil.closeConnection(con)	;
	out.write(html);
}

	if (action.equals("executequery")&&!"".equals(query)){
	try{
		Dbutil.logger.info("Performing executequery in admintools.jsp: "+query);
		int result=Dbutil.executeQuery(query);
		out.write("Result of executeQuery: "+result);
	} catch (Exception e){
		Dbutil.logger.error("Problem during executequery in admintools.jsp:",e);
		out.write("Error occurred during executequery:\n");
		out.write(e.getMessage());
	}
	out.write(html);
}




}%>
	




</div>
</body> 
</html>


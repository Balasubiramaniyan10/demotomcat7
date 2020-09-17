<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%	String target="";
	if (request.getAttribute("javax.servlet.forward.request_uri") == null) {
        response.sendRedirect("/index.jsp");
    } else {
    	request.setAttribute("originalURL", (String)request.getAttribute("javax.servlet.forward.request_uri"));
    	if (((String)request.getAttribute("javax.servlet.forward.request_uri")).contains("settings")) {
    		target="Price Alerts";
    	} 
    	if (((String)request.getAttribute("javax.servlet.forward.request_uri")).contains("editshopinfo")) {
    		target="Edit Shop Info";
    	}
    	if (((String)request.getAttribute("javax.servlet.forward.request_uri")).contains("editdatafeed")) {
    		target="Edit Data Feed";
    	}
    	if (((String)request.getAttribute("javax.servlet.forward.request_uri")).contains("sendshoppinglist")) {
    		target="Shoppinglist";
    	}
    }
    %>
 
    <%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<%@ page contentType="text/html; charset=UTF-8" %> 
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>vinopedia <%=target %></title>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Login");%>
<%@ include file="/header2.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<script type="text/javascript">
function focus(){
document.getElementById("j_username").focus();
}</script>
</head>
<body onload="javascript:focus();">
<% request.setAttribute("ad","false"); %> 
<%@ include file="/snippets/textpage.jsp" %> 
<% if (target.equals("Price Alerts")) {%>
<h3>PriceAlerts</h3>
To create PriceAlerts or change your settings, you have to log in.<br/>
<% } else if (target.equals("Edit Shop Info")||target.equals("Edit Data Feed")) {%>
<h3>Log in required</h3>
To edit your data feed and your company information, you must log in. If you have not yet created an account you can do so <a href="/forum/user/insert.page" target='_blank'>here</a>, it just takes 1 minute.
<% } else {%>
<h3>Log in</h3>
The page you requested requires you to log in.<br/>

<%} %>
<form method="post" action='<%= response.encodeURL("j_security_check") %>' name='form'>
  <table border="0" cellspacing="5">
    <tr>
      <td align="right">Username:</th>
      <td align="left"><input type="text" id="j_username" name="j_username"></td>
    </tr>
    <tr>
      <td align="right">Password:</th>
      <td align="left"><input type="password" name="j_password"></td>
    </tr>
    <tr>
      <td align="right"><input type="submit" value="Log In"></td>
      <td align="left"><input type="reset"></td>
    </tr>
  </table>
</form>
<% if (target.equals("Price Alerts")){ %>
Not registered yet? Create a new user account <a href="/forum/user/insert.page">here</a>, it just takes 1 minute.<br/><br/>
PriceAlerts inform you of new wines on the market that you have been looking for. You just need to specify what wines you are interested in (and, if you like, at what price). Whenever we find a wine that matches your criteria, we will send you an email with the details of the wine. Or, you can use an RSS reader to receive these notifications. This way, you never miss a good deal again.<br/>
<br/><img src='/images/PriceAlert.jpg'/>
<%} else if (target.equals("Shoppinglist")){%>
Not registered yet? Create a new user account <a href="/forum/user/insert.page" target='_blank'>here</a>, it just takes 1 minute.<br/><br/>
After registering return to this page and log in to send your shopping list.
<%} %>
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>
<br/><br/>
<body bgcolor="white" onLoad="document.form.j_username.focus()">
<TABLE class="main" ><TR><TD class="left"></TD><TD class="centre">

<h3>PriceAlerts</h3>
To create PriceAlerts or change your settings, you have to log in.<br/>
<form method="post" action='<%= response.encodeURL("j_security_check") %>' name='form'>
  <table border="0" cellspacing="5">
    <tr>
      <td align="right">Username:</th>
      <td align="left"><input type="text" name="j_username"></td>
    </tr>
    <tr>
      <td align="right">Password:</th>
      <td align="left"><input type="password" name="j_password"></td>
    </tr>
    <tr>
      <td align="right"><input type="submit" value="Log In"></td>
      <td align="left"><input type="reset"></td>
    </tr>
  </table>
</form>
Not registered yet? Create a new user account <a href="/forum/user/insert.page">here</a>, it just takes 1 minute.<br/><br/>
PriceAlerts inform you of new wines on the market that you have been looking for. You just need to specify what wines you are interested in (and, if you like, at what price). Whenever we find a wine that matches your criteria, we will send you an email with the details of the wine. Or, you can use an RSS reader to receive these notifications. This way, you never miss a good deal again.<br/>
<br/><img src='/images/PriceAlert.jpg'> 

</TD><TD class="right">
		
		
	</TD></TR>
</TABLE>	
<%} %>
</div>
</body>
</html>


<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.batch.Spider"%><html>
<head>
<title>Create an account on Vinopedia</title>
<jsp:include page="/header2.jsp" />
</head>
<body>
	<%@ page import="java.io.*" import="java.text.*" import="java.lang.*"
		import="java.sql.*" import="com.freewinesearcher.common.Wine"
		import="com.freewinesearcher.online.Webroutines"
		import="com.freewinesearcher.common.Wineset"%>
	<%
		String username = Webroutines.filterUserInput(request.getParameter("username"));
		String password = request.getParameter("password");
		String password2 = request.getParameter("password2");
		String email = Webroutines.filterUserInput(request.getParameter("email"));
		String message = Webroutines.filterUserInput(request.getParameter("message"));
		String action = Webroutines.filterUserInput(request.getParameter("action"));
		if (action == null)
			action = "";
		if (username == null)
			username = "";
		if (password == null)
			password = "";
		if (password2 == null)
			password2 = "";
		if (email == null)
			email = "";
		boolean showform = true;
		boolean ok = true;
		boolean success = false;
		message = "";
		if (action.equals("newaccount")) {
			if (!password.equals(password2)) {
				ok = false;
				message += "The password and the verification are not the same! ";
			} else if (password.length() < 5) {
				ok = false;
				message += "The password should be at least 5 characters long. ";
			} else if (username.length() < 5) {
				ok = false;
				message += "The username should be at least 5 characters long. ";
			}
			/* else if (Dbutil.readValueFromDB("select * from vp_users where user_id='"+Spider.SQLEscape("username")+"';","id")!=null){
				ok=false;
				message+="A user with that name already exists. If you have forgotten your password please <a href='/account/lostpassword.jsp'>cick here</a> to reset it. otherwise, please choose a different username. ";
			}  */
			else if (!Webroutines.checkEmail(email)) {
				ok = false;
				message += "Your email address is not valid, please correct it. ";
			}
			if (ok) {
				showform = false;
				Webroutines.adduser(username, password, email);
				success = true;
				message = "Your account Registration has been successfull";
			}
		}
		if (showform) {
	%>
	<form action="" method="post">
		To create an account on Vinopedia, please supply a username, password
		and a working email address. We will send an account activation email
		for verification before activating the account.<br />
		<%=message%>
		<table>
			<tr>
				<td>Username</td>
				<td><input type="text" name="username" value="<%=username%>" /></td>
			</tr>
			<tr>
				<td>Password</td>
				<td><input type="password" name="password" value="" /></td>
			</tr>
			<tr>
				<td>Confirm Password</td>
				<td><input type="password" name="password2" value="" /></td>
			</tr>
			<tr>
				<td>Email address</td>
				<td><input type="text" name="email" value="<%=email%>" /></td>
			</tr>
		</table>
		<input type='hidden' name='action' value='newaccount' /> <input
			type="submit" value="submit" />
	</form>
	<%
		}
	%>
	<%
		if (success) {
	%>
	<%=message%>
	<%
		}
	%>
</body>
</html>
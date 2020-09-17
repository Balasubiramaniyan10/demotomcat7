<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%
	Connection conn = null;
	Statement stmt = null;
	try {
		out.println("<br><br>setting class for name........");
		Class.forName("com.mysql.jdbc.Driver");
		out.println("creating database connection............ jdbc:mysql://172.20.20.2:3306/wijn");
		
		//conn = DriverManager.getConnection("jdbc:mysql://172.20.20.2:3336/wijn", "vpapp", "gyh(74%bbGSsw");
		conn = DriverManager.getConnection("jdbc:mysql://localhost:3336/wijn", "vpapp", "gyh(74%bbGSsw");
		
		out.println("creating statement............ ");
		stmt = conn.createStatement();
		out.println("executing the query............ SELECT count(*) FROM wijn.app_user");
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM wijn.app_user");
		out.println("result set created............ SELECT count(*) FROM wijn.app_user");
		if (rs.next()) {
			out.print("COUNT: " + rs.getInt(1));
		}
	} catch (Exception e) {
		out.print("SQL EXCEPTION: " + e);
	}
%>

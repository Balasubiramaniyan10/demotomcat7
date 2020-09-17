<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"%>
<%@page import="java.sql.*,java.util.*"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%
String username=request.getParameter("username");
String password=request.getParameter("password");
Class.forName("com.mysql.jdbc.Driver");
java.sql.Connection con = Dbutil.openNewConnection();
Statement st= con.createStatement();
ResultSet rs=st.executeQuery("select * from vp_users where username='"+username+"' and password='"+password+"'");
try{
if(rs.next())
{
	request.getRequestDispatcher("../admin/adminlinks.jsp").forward(request, response);
}
else{
	request.getRequestDispatcher("../settings/error.jsp").forward(request, response);
}
}
catch (Exception e) {
e.printStackTrace();
}
%>
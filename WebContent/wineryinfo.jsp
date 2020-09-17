<%@page import="com.freewinesearcher.online.Producer"%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><% response.setContentType("text/html");%>
<% int producerid=0;
	try{producerid=Integer.parseInt(request.getParameter("producer"));}catch(Exception e){}
	if (producerid>0){
		Producer producer=new Producer(producerid);
		out.write(producer.getMapHtml());
	} else {
	%>Error<%
	}%>
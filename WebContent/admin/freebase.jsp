<%@ page contentType="plain/text; charset=UTF-8" pageEncoding="UTF-8"%><%@page import="com.freewinesearcher.online.Webroutines"%><% int n=100;
try{n=Integer.parseInt(request.getParameter("n"));}catch(Exception e){}
out.write(Webroutines.getFreebaseData(n));
%>
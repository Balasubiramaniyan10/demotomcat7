<%@page import="com.freewinesearcher.online.Webroutines"%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><html><body><%@ include file="/snippets/jsincludes.jsp" %>
<jsp:useBean id="winerymailing" class="com.freewinesearcher.online.WineryMailing" scope="session"/>
<jsp:setProperty name="winerymailing" property="country"/><%
int priority=0;
boolean reverse=false;
if (request.getParameter("reverse")!=null&&request.getParameter("reverse").equals("true")) reverse=true;
if (request.getParameter("Jeroen")!=null) priority=-1;
if (request.getParameter("Jasper")!=null) priority=-2;
if (request.getParameter("id")!=null){
	Webroutines.savewinerydata(request.getParameter("id"),null,request.getParameter("telephone"),request.getParameter("website"),request.getParameter("email"),priority);
}
int retrieveid=0;
try{retrieveid=Integer.parseInt(request.getParameter("retrieveid"));}catch(Exception e){}
if (request.getParameter("updatecoordinates")!=null) Webroutines.correctWineryCoordinates();
	
	%>
<FORM METHOD="POST"  id="formOne">
Country:<select name='country'>
<%for (String s:com.freewinesearcher.online.WineryMailing.countries) out.write("<option value='"+s+"'"+(s.equals(winerymailing.getCountry())?" selected='selected'":"")+">"+s+"</option>");
%>
</select>
<input type='submit' value='Change country' id='end'/>
</FORM>
<form action='matchwineries.jsp' method='post'><%=Webroutines.refinewinery(retrieveid,winerymailing.getCountry(),reverse)%>
<input type='submit' value='Save' id='end'/>
<%if (!reverse){ %>
<input type='submit' value='Naar Jeroen' name='Jeroen'/>
<%} %>
<%if (reverse){ %>
<input type='submit' value='Naar Jasper' name='Jasper'/>
<%} %>
<input type='hidden' name='reverse' value='<%=(reverse?"true":"false")%>'/>
</form>
<%	out.flush();
	if (request.getParameter("id")!=null){
	Webroutines.savewinerydata(request.getParameter("id"),request.getParameter("address"),request.getParameter("telephone"),request.getParameter("website"),request.getParameter("email"),priority);
}
 %>
</body></html>
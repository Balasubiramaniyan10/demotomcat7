
<%@page import="com.freewinesearcher.online.PageHandler"
 import="com.freewinesearcher.common.Context"
%><html>
<head>
<title>Mail wineries</title>
</head>
<body bgcolor="white">
<jsp:include page="/admin/adminlinks.jsp" />
<%@ page 
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*" 
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Variables"
	
%>
<jsp:useBean id="winerymailing" class="com.freewinesearcher.online.WineryMailing" scope="session"/>
<jsp:setProperty name="winerymailing" property="alreadysent" value="false"/>
<jsp:setProperty name="winerymailing" property="actie" value=""/>
<jsp:setProperty name="winerymailing" property="winery" value="0"/>
<jsp:setProperty name="winerymailing" property="*"/>
<br/><%=winerymailing.doaction()%><br/>
<FORM METHOD="POST"  id="formOne">
Results per page: <input type='text' name='resultsperpage' size='10' value='<%=winerymailing.getResultsperpage() %>'/>
Sort: <select name='sort1'>
<%String sorts="wines desc,wines";
if (winerymailing.isAlreadysent()) sorts+=",storelocatormail desc,demopage desc";
for (String s:sorts.split(",")) out.write("<option value='"+s+"'"+(s.equals(winerymailing.getSort1())?" selected='selected'":"")+">"+s+"</option>");
%>
</select>
Country:<select name='country'>
<%for (String s:com.freewinesearcher.online.WineryMailing.countries) out.write("<option value='"+s+"'"+(s.equals(winerymailing.getCountry())?" selected='selected'":"")+">"+s+"</option>");
%>
</select>
Skip records: <input type='text' name='skip' size='10' value='<%=winerymailing.getSkip() %>'/>
<input type='checkbox' <%if (winerymailing.isAlreadysent()) out.write("checked='checked' "); %> name='alreadysent'>Already sent</input>
<input type='submit' value='List'/>
 
</FORM>
<%=winerymailing.list()%>
<br/><br/>
Edit email message: {0}=winery name, {1}=email address, {2}=link to demo page, {3}=winery page on Vinopedia<br/>
<br/>Subject:<br/>
<form method='post'>
<textarea name='mailheader' cols='100' rows='1'><%=winerymailing.getMailheader() %></textarea>
<input type='hidden' name='actie' value='updateheader'/><br/>
<input type='submit' value='Save new header'/>
</form>
<br/>Body:<br/>
<form method='post'>
<textarea name='mailtext' cols='100' rows='10'><%=winerymailing.getMailtext() %></textarea>
<input type='hidden' name='actie' value='updatetext'/><br/>
<input type='submit' value='Save new text'/>
</form>
Example:<br/>
Subject: <%= winerymailing.getMailHeaderComplete() %><br/><br/>
<%= winerymailing.getMailTextComplete() %>
</body>
</html>
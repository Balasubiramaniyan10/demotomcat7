<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<%@page import="com.freewinesearcher.batch.sitescrapers.SuckCTLabels"%><%@page import="com.freewinesearcher.batch.sitescrapers.SuckCT"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<%@ include file="/header2.jsp" %>

</head><body>
<% boolean saved=false; %>
<% int extraoffset=0;if (request.getParameter("action")!=null&&request.getParameter("action").equals("skip")){
	extraoffset=1;
}
if ("Jeroen".equals(request.getParameter("editor"))) session.setAttribute("editor","Jeroen");
if (request.getParameter("action")!=null&&request.getParameter("action").equals("Save URL")){
	saved=Webroutines.saveLabelFromUrl(request.getParameter("knownwineid"),request.getParameter("url"));
}
	if (request.getParameter("action")!=null&&request.getParameter("action").equals("getall")){
	try{
	SuckCTLabels ct=new SuckCTLabels();
	
	ct.pause=0;
	ct.limit=20;
	ct.knownwineid=Integer.parseInt(request.getParameter("knownwineid"));
	ct.winename=Dbutil.readValueFromDB("select wine from knownwines where id="+ct.knownwineid,"wine");
	ct.spider();
	} catch (Exception e){}
}%>

<% if (saved==false) saved=Webroutines.saveLabel(request.getParameter("knownwineid"),request.getParameter("candidate")); %>

<div id='labels'></div><div id='msg'></div>


<%=Webroutines.selectLabelHtml(extraoffset,(String)session.getAttribute("editor"))%>
<br/><br/>
Sleep het juiste label hieronder naar het invoerveld hierboven.<br/>

<iframe id='myframe'  style='width:100%;height:800px;'></iframe>
<script type='text/javascript'>
$('#myframe').attr('src',(url));
</script>

</body>
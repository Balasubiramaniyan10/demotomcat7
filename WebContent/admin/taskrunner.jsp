<%@	page 
	import = "com.freewinesearcher.batch.Taskrunner"
	%>

<html>
<head>
<title>Admin Taskrunner</title>
</head>
<body>
<jsp:include page="adminlinks.jsp" />
<br><br>
<% String task = request.getParameter("task");
	String directory=request.getParameter("directory");
	
	if (task==null) task="";
	if (!task.equals("")){
		Taskrunner taskrunner=new Taskrunner();
		taskrunner.task=task;
		taskrunner.directory=directory;
		new Thread(taskrunner).start();
		out.write("Task "+task+" started...");%>
		<script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script><%
	}
	
	
%>	
<br><br>
<a href='taskrunner.jsp?task=analyseRatedWines'>Analyse Rated Wines</a><br/>
<a href='taskrunner.jsp?task=analyseKnownWines'>Analyse Known Wines</a><br/>
<a href='taskrunner.jsp?task=analyseRedundantWineTerms'>Analyse Redundant (improved) Wines</a><br/>

<form action='taskrunner.jsp?task=suckWineSearcher' method='POST'>Refresh shop info Wine-Searcher.com<br/>Directory:<input type='text' name='directory' value='C:\temp\winesearcher\winesearcher\www.wine-searcher.com\merchant'/><input type='submit' value='Submit'/></form><br/>

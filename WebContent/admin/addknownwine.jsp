<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Region"
	import = "java.util.ArrayList"
	
	%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<jsp:include page="/admin/adminlinks.jsp" />

</head>
<body>
<%
	String id= request.getParameter("id");
	if (id==null) id="";
	String wine = request.getParameter("wine");
	String appellation=request.getParameter("appellation");
	String fulltext=request.getParameter("fulltext");
	String literalsearch=request.getParameter("literalsearch");
	String literalsearchexclude=request.getParameter("literalsearchexclude");
	String query = request.getParameter("query");
	String action= request.getParameter("action");	
	String color= request.getParameter("color");
	String dryness= request.getParameter("dryness");
	String sparkling= request.getParameter("sparkling");
	int disabled;
	if (color==null) color="";
	if (dryness==null) dryness="";
	if (sparkling==null) sparkling="0";
	if (wine==null) wine="";
	if (query==null) query="";
	if (literalsearch==null) literalsearch="";
	if (literalsearchexclude==null) literalsearchexclude="";
	if (action==null) action="";
	if (appellation==null) appellation="";
	if (fulltext==null) fulltext="";
	if (!query.equals("")) query="'Select * from knownwines where match (wine) against ('"+query+"' in boolean mode);";
	if  (fulltext.equals("")){
	 fulltext=wine.replaceAll("[-'.()\\[\\]]"," ");
	 fulltext=fulltext.replaceAll(" +$","");
	 fulltext="+"+fulltext.replace(" "," +");
	 fulltext=fulltext.replace("+ ","");
	}
	
	if (!id.equals("")&&wine.equals("")){
		wine=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","wine");
		appellation=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","appellation");
		literalsearch=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","literalsearch");
		literalsearchexclude=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","literalsearchexclude");
		fulltext=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","fulltextsearch");
	}
	
	boolean succes=false;
	if (action.equals("save")&&!appellation.equals("")&&!wine.equals("")&&!fulltext.equals("")) {
		succes = Knownwines.editKnownwine(wine,appellation,fulltext,literalsearch,literalsearchexclude,id,color,dryness,sparkling);
		
	if (succes==true){
%>		<h4>Wine was saved successfully.</h4><script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script><%
			} else {
		%>
		<H1>Oeps! Update didn't go as expected!!!</H1>
	<%
		}
			}
			if (action.equals("disable")&&id!=null&&!id.equals("")) {
		String res=Knownwines.disableKnownwine(id);
		
			if (!"Error".equals(res)){
	%>		<h4>Wine was disabled.</h4><script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script><%
			} else {
		%>
		<H1>Oeps! Update didn't go as expected!!!</H1>
	<%
		}
			}
			if (action.equals("enable")&&id!=null&&!id.equals("")) {
		String res=Knownwines.enableKnownwine(id);
		
			if (!"Error".equals(res)){
	%>		<h4>Wine was enabled.</h4><script type="text/javascript">
		if(navigator.appName=="Microsoft Internet Explorer") {
		this.focus();self.opener = this;self.close(); }
		else { window.open('','_parent',''); window.close(); }
		window.opener='x';window.close();</script><%
			} else {
		%>
		<H1>Oeps! Update didn't go as expected!!!</H1>
	<%
		}
			}
	%>

<form action='addknownwine.jsp'>
<input type='text' name='fulltext' value='<%=fulltext%>' size='100'>
<input type='hidden' name='wine' value='<%=wine%>'>
<input type='hidden' name='id' value='<%=id%>'>
<input type='hidden' name='action' value='select'>
<input type='submit'>
</form> 
<br/>Add or edit this wine:<br/>
<form action='addknownwine.jsp'>
<table><tr><td>Wine name</td><td><input type='text' name='wine' value='<%=wine.replace("'","&apos;")%>' size='100'></td></tr>
<tr><td>Full text</td><td><input type='text' name='fulltext' value='<%=fulltext%>' size='100'></td></tr>
<tr><td>Literalsearch</td><td><input type='text' name='literalsearch' value='<%=literalsearch%>' size='100'></td></tr>
<tr><td>Literalsearchexclude</td><td><input type='text' name='literalsearchexclude' value='<%=literalsearchexclude%>' size='100'></td></tr>
<tr><td>Appellation</td><td><select name='appellation'>
<%
	ArrayList<String> regions=Region.getRegions("All");
out.write("<option value='Unknown'>Select");

for (String region:regions){
%>
<option value='<%=region%>' <%if (appellation.equals(region)||(appellation.equals("")&&wine.contains(region))) out.write(" Selected");%>><%=region%>
<%
	}
%>
</select></td></tr>
<%
	if (id>0){
%><tr><td></td><td><%
	color=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","color");
%>
Wine color<select name='color'>
<%
	out.write("<option value=''>Select");
for (String option:new String[]{"Red", "White", "Rosé"}){
%>
<option value='<%=option%>' <%if (color.equals(option)) out.write(" Selected");%>><%=option%></option>
<%
	}
%>
</select>

<%
	dryness=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","dryness");
%>
Dryness<select name='dryness'>
<%
	out.write("<option value=''>Select");
for (String option:new String[]{"Dry", "Sweet/Dessert", "Fortified","Off-dry"}){
%>
<option value='<%=option%>' <%if (dryness.equals(option)) out.write(" Selected");%>><%=option%></option>
<%
	}
%>
</select>

<%
	sparkling=Dbutil.readValueFromDB("select * from knownwines where id="+id+";","sparkling");
%>
Sparkling<select name='sparkling'>
<%
	out.write("<option value='0' >No</option>");
out.write("<option value='1' "+(sparkling.equals("1")?" Selected":"")+">Yes</option>");
%>
</select></td></tr>
<%
	}
%>
</table>
<input type='hidden' id='action' name='action' value='save'>
<input type='hidden' id='id' name='id' value='<%=id%>'>
<input type='submit' value='Save wine'>
<input type='submit' value='Save as new wine' onClick="document.getElementById('id').value='';">
<%
	if (id>0){
%>
<%
	disabled=Dbutil.readIntValueFromDB("select * from knownwines where id="+id+";","disabled");
	if (disabled==0){
%>
<input type='submit' value='Disable this wine' onClick="document.getElementById('action').value='disable';">
<% } else { %>
<input type='submit' value='Enable this wine' onClick="document.getElementById('action').value='enable';">
<%} 
}%>
</form> 
<table><tr><td>
<%
	if (!fulltext.equals("")&&!action.equals("save")) out.write(Knownwines.getKnownWinesListHTML(fulltext));
%>
</td><td>
<%
if (!fulltext.equals("")&&!action.equals("save")) out.write(Knownwines.getRatedWinesListHTML(id));
if (!fulltext.equals("")&&!action.equals("save")) out.write("<br/>"+Knownwines.getCheapestWinesHTML(id,10));

%>

</td></tr></table>

</body> 
</html>
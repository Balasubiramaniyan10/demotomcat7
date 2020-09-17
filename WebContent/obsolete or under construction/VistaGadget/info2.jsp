<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.batch.Spider"

	
	
%>

<%
	String html=Webroutines.getTipsHTML("",(float)0.75,20,"mobile.jsp",Translator.languages.EN);
	if (html.equals("")) html=((Webroutines.getTipsHTML("",(float)0.75,20,"mobile.jsp",Translator.languages.EN)).equals("")?("<br />"+new Translator(Translator.languages.EN).get("notips")):("<br />"+new Translator(Translator.languages.EN).get("pricenote")+"<br />"));
	html=html.replace("\n","");
	html=html.replace("\"","\\\"");
%>

function getInfo(){
document.write("<%=html%>");
}
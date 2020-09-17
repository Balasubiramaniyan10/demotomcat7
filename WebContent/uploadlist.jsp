<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/logout");
    return;
  }
%>


<%@ page 
	import = "java.text.*"
	import = "java.io.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.online.Search"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.ImageInfo"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Searchset"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Auditlogger"
	import = "com.freewinesearcher.online.Ad"	
	import = "com.searchasaservice.ai.Recognizer"
	import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.servlet.ServletFileUpload, org.apache.commons.fileupload.disk.DiskFileItemFactory, org.apache.commons.io.FilenameUtils, java.util.*, java.io.File, java.lang.Exception" %>
<%	
	boolean hideuploaddialog=false;
	boolean infoiscomplete=true;
	boolean showexample=true;
	boolean check=false;
	int numberofrows=100;
	int offset=0;
	String message="";
	String action=Webroutines.filterUserInput(request.getParameter("action"));
	if (action==null) action="";
	String list=(request.getParameter("list"));
	if (list==null) list="";
	
	Webactionlogger logger;
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	FileItem fileItem = null;
	PageHandler p=PageHandler.getInstance(request,response,"Upload winelist");
	%><html>
	<head>
	<title>Upload wine list</title>
	<%@ include file="/header2.jsp" %>
	</head><body>
	<%@ include file="/snippets/textpagenosearch.jsp" %>
<%	
if (!list.equals("")){
	int setid=Recognizer.analyzeUploadList(list);
	Webroutines.getWineListPrices(1, setid);
	out.write(Webroutines.getWineListHTML(1,setid));
}

%>
	<h3>Upload a wine list</h3><br/>
			<form action="uploadlist.jsp" method="post">
				<textarea cols='80' rows='30' name="list" ><%=list %></textarea><br/>
				<input type="hidden" name="action" value="save"/>
				<input type="submit" value="Analyze" />
			</form>
<%@ include file="/snippets/footer.jsp" %>
</body>
</html>

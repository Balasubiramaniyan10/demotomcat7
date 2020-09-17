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
	String link=(request.getParameter("link"));
	if (link==null) link="http://";
	
	Webactionlogger logger;
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	FileItem fileItem = null;
	Searchdata searchdata=new Searchdata();
	searchdata.setName("Château Léoville Las Cases");
	PageHandler p=PageHandler.getInstance(request,response,"Banner upload");
	Auditlogger al=new Auditlogger(request);
	%><html>
	<head>
	<title>Edit banner for <%=Webroutines.getShopNameFromShopId(al.shopid,"")%></title>
	<%@ include file="/header2.jsp" %>
	</head><body>
	<%@ include file="/snippets/textpagenosearch.jsp" %>
<%	String ipaddress=p.ipaddress;
	if (action.equals("reset")){
		session.removeAttribute("image");
		al.setAction("Banner upload cancelled");
		al.setObjecttype("Banner");
		logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
		logger.logaction();
		al.run();
	}
	if (al.shopid==0) {
		out.write("Your account is not linked to any shop. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
	} else if (!al.adenabled){
		response.sendRedirect("/shops/agreement.jsp");
	} else {
%>
	<h3>Upload a new banner for <%=Webroutines.getShopNameFromShopId(al.shopid,"")%></h3><br/>
<%
	// If a file was uploaded, fill fileItem with it
	if (ServletFileUpload.isMultipartContent(request)){
	  	ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
	  	List fileItemsList = servletFileUpload.parseRequest(request);
	  	String optionalFileName = "";
	  	if (fileItemsList.size()>0){
	  		if (fileItemsList.size()>1){
		  		out.write("Upload failed: you can only upload plain images (in GIF, JPEG or PNG format).");
	  		} else {
	      		fileItem = (FileItem)fileItemsList.get(0);
	      		if (fileItem.getSize()>40*1024){
	      			out.write("Upload failed. Size of image is "+(int)(fileItem.getSize()/1024)+"kB.Maximum size of images is 40kB.");
	      			fileItem=null;
	      			al.setAction("Bannerupload: file too big");
	      			al.setObjecttype("Banner");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();
	      		} else {
	      			al.setAction("Bannerupload");
	      			al.setObjecttype("Banner");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();
	      		}
	  		}
	  	}
	}
	
	// If no file uploaded, fill it with the file in the session if any
	if (fileItem==null) {
		fileItem=(FileItem)session.getAttribute("image");
	}
	if (fileItem!=null&&fileItem.getSize() > 0){
		session.setAttribute("image",fileItem);	
  		// Handle the uploaded image
		ImageInfo ii=new ImageInfo();
		ii.setInput(fileItem.getInputStream()); // in can be InputStream or RandomAccessFile
		ii.setDetermineImageNumber(true); // default is false
		ii.setCollectComments(true); // default is false
		check=ii.check();
		if (check&&ii.getMimeType()!=null&&(ii.getMimeType().equals("image/gif")||ii.getMimeType().equals("image/png")||ii.getMimeType().equals("image/jpeg"))){
	if (action.equals("save")){
		if (link.equals("")||link.equals("http://")){
	infoiscomplete=false;
	message+="Please supply a URL where the advertisement should link to.<br/>";
	      			al.setAction("Bannerinfo: URL missing");
	      			al.setObjecttype("Banner");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();

		}
		if (!link.startsWith("http://")&&!link.startsWith("https://")){
	infoiscomplete=false;
	message+="Please supply a full URL including 'http://'<br/>";
	al.setAction("Bannerinfo: URL incomplete");
	      			al.setObjecttype("Banner");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();
		}
		
		if (infoiscomplete){
	/* Save the uploaded file if its size is greater than 0 and type is image. */
	  				try {
	  					session.removeAttribute("image");
		  				java.io.InputStream inStream = fileItem.getInputStream();
		  				Connection con=Dbutil.openNewConnection();
		  				String sqlStatement = "insert into images (contenttype, width, height, data, partnerid) values ('"+ii.getMimeType()+"', 0, 0, ?, '"+al.partnerid+"')";
		  				PreparedStatement pstmt = con.prepareStatement(sqlStatement);
		//	   set up input stream
		  				pstmt.setBinaryStream(1,inStream,inStream.available());
		//	   execute statement
		pstmt.executeUpdate();
		String imagetype=ii.getMimeType().substring(6); 
		ResultSet rs=Dbutil.selectQuery("select LAST_INSERT_ID() as bannerid;",con);
		if (rs.next()){
	int bannerid=rs.getInt("bannerid");
	sqlStatement="insert into banners (hsize,vsize,html,source,link,shopid,partnerid,knownwineid,country,payperclick,active)"+ 
	"values ("+ii.getWidth()+","+ii.getHeight()+",'<img style=\\'width:"+ii.getWidth()+"px;height:"+ii.getHeight()+"px;\\' src=\\'"+Configuration.staticprefix+"/images/gen/"+bannerid+"."+imagetype+"\\' alt=\\'Banner\\' />','upload','"+link+"',"+al.shopid+","+al.partnerid+",0,'',0.50,1);";
	Dbutil.executeQuery(sqlStatement);
	//close connection
	  				//fileItem.write(saveTo);
	  				hideuploaddialog=true;
	//close connection
	  				Dbutil.closeConnection(con); 
	  				al.setAction("Banner stored (id="+bannerid+")");
	      			al.setObjecttype("Banner");
	      			al.setObjectid(bannerid+"");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();
	  				session.setAttribute("message","<b>The uploaded file has been saved successfully.</b><br/>The status of this banner is still set to \"inactive\", which means visitors will not see your banner. In the banner  overview below you can activate it.");
	session.removeAttribute("image");
	  				response.sendRedirect("admanagement.jsp");
	
	out.write("If you are not forwarded, <a href='admanagement.jsp'>click here</a> to continue");
	
		//fileItem.write(saveTo);
		  				hideuploaddialog=true;
%><b>The uploaded file has been saved successfully.</b>
	<%
		}
			}  catch (Exception e){
		  				Dbutil.logger.error("Problem while saving banner: ",e); 
	%><b>An error occurred when we tried to save the uploaded file.</b>
	<%
		al.setAction("Banner not stored due to an error");
		al.setObjecttype("Banner");
		logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
		logger.logaction();
		al.run();
			
			  				}
		
		}
			}	
			if (!action.equals("save")||!infoiscomplete){ 
		// Display image information and ask for confirmation
	%>
  				<b>Uploaded image:</b><br/><br/>
				<img src='/images/gen/<%=Math.abs(new Random().nextInt())%>' /><br/><br/>
				<b>File Information:</b><br/>
				Content type: <%=ii.getMimeType()%><br/>
				<%
					if (check) {
																		out.write("Image size: "+ii.getWidth()+" x "+ii.getHeight()+ " px.<br/>");
																		session.setAttribute("image",fileItem);
																		FileItem sessitem=(FileItem)session.getAttribute("image");
																		
																}
																hideuploaddialog=true;
																if ((ii.getWidth()!=234||ii.getHeight()!=60)) {
																	out.write("<b>The image size ("+ii.getWidth()+"x"+ii.getHeight()+") does not fit the banner size of 234x60 pixels! Please upload a different image.</b><br/>");  
																	showexample=false;
																	hideuploaddialog=false;
																	al.setAction("Banner too big");
																	al.setObjecttype("Banner");
																	logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
																	logger.logaction();
																	al.run();
																}
																if (showexample){
				%>
				<b><%=message%></b>
				<br/>
				<form action="bannerupload.jsp" method="post">
				URL that the banner should link to:<br>
				<input type="text" name="link" value="<%=(link)%>" /><br/>
				<br/>When you are satisfied, press "Save as banner" to store this banner on vinopedia. If you would like to cancel this banner, press "Cancel this upload".<br/>
				<input type="hidden" name="action" value="save"/>
				<input type="submit" value="Save as banner" />
				</form>
				<form action="bannerupload.jsp" method="post">
				<input type="hidden" name="action" value="reset"/>
				<input type="submit" value="Cancel this upload" />
				</form>
				
<%if (showexample){// We show what a page with the ads will look like. This is a copy of index.jsp.


p.processSearchdata(request);%>
		<%@ include file="/snippets/logoandsearch.jsp" %>
		<% Webroutines.RatingInfo ri=Webroutines.getNewRatingsHTML(p.s.wineset.bestknownwineid, 1000, "/indexnewstyle.jsp",p.s.singlevintage,p.searchdata,p.t,false);
		out.print(ri.html);%>
		<%@ include file="/snippets/refine.jsp" %>
		<div id='adbetween'><%if (ii.getWidth()<=768&&ii.getHeight()<=90) out.write ("<img src='/images/gen/"+Math.abs(new Random().nextInt())+"' />");%></div>
		<div id='main'>
			<div id='adright'><%if (ii.getWidth()<=160&&ii.getHeight()<=600) out.write ("<img src='/images/gen/"+Math.abs(new Random().nextInt())+"' />");%></div>
			<div id='mainleft'>	
				<%=Webroutines.getTabbedWineResultsHTML(p.s.wineset,p.t,searchdata,25,response,"false",false,p.thispage,0,null,false,false)%>
			<div class='pricenote'><%=p.t.get("pricenote")%></div>
			<div class='authornote'><%out.print(ri.authornote);%></div>
			<%@ include file="/snippets/footer.jsp" %>	
			</div>
		</div> <!--  main-->

				
<%
	
}
}
}
} else {
if (check) {
out.write("You sent a file of type "+ii.getMimeType()+". "); 
			} else {
	out.write("You sent an unknown file type. ");
}
			out.write ("Only images of type GIF, JPEG or PNG can be uploaded");
al.setAction("Banner uploaded has wrong file type: "+ii.getMimeType());
al.setObjecttype("Banner");
logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
logger.logaction();
al.run();
			}
		}
		if (!hideuploaddialog){
		// Let the user upload an image
	%>
	<form action="bannerupload.jsp" method="post" enctype="multipart/form-data">
	<br/>
	  First choose a banner to upload from your PC, then click the Upload button:<br/><br/>
	  <input name="myFile" type="file" />
	  <input type="submit" value="Upload" />
	</form>
	<br/><br/>
	Banners can be supplied as an image in the following formats: GIF, JPEG (JPG) or PNG. Banners must have the exact size of 234 pixels wide and 60 pixels high.<br/>
	<%}
	}%>
<%@ include file="snippets/footer.jsp" %>
</body>
</html>


<%
	if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("/logout");
    return;
  }
%>


<%@ page import="java.text.*" import="java.io.*" import="java.sql.*"
	import="com.freewinesearcher.online.Search"
	import="com.freewinesearcher.online.Searchdata"
	import="com.freewinesearcher.batch.Spider"
	import="com.freewinesearcher.online.ImageInfo"
	import="java.util.ArrayList"
	import="com.freewinesearcher.common.Searchset"
	import="com.freewinesearcher.common.Wineset"
	import="com.freewinesearcher.common.Wijnzoeker"
	import="com.freewinesearcher.online.Translator"
	import="com.freewinesearcher.online.Webroutines"
	import="com.freewinesearcher.common.Dbutil"
	import="com.freewinesearcher.online.Webactionlogger"
	import="com.freewinesearcher.online.Auditlogger"
	import="com.freewinesearcher.online.Ad"
	import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.servlet.ServletFileUpload, org.apache.commons.fileupload.disk.DiskFileItemFactory, org.apache.commons.io.FilenameUtils, java.util.*, java.io.File, java.lang.Exception"%>
<%	Auditlogger al=new Auditlogger(request);
	int bannerid=0;
	try{bannerid=Integer.parseInt(request.getParameter("bannerid"));}catch(Exception e){}
	Banner banner=Banner.load(bannerid,al.partnerid);
	boolean hideuploaddialog=false;
	boolean infoiscomplete=true;
	boolean showexample=true;
	boolean check=false;
	int numberofrows=100;
	int offset=0;
	String message="";
	String targetcountries="";
	for (String country:Banner.countries.split(",")){ 
		if (request.getParameter(country)!=null) targetcountries+=","+country;
	}
	if (targetcountries.length()>0) {
		targetcountries=targetcountries.substring(1);
		al.setOldvalue(banner.targetcountries+" "+al.getOldvalue());
		banner.targetcountries=targetcountries;
		al.setNewvalue(banner.targetcountries+" "+al.getNewvalue());
	}
	
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
	%>
<%@page import="com.freewinesearcher.online.Banner"%><html>
<head>
<title>Change banner for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></title>
<%@ include file="/header2.jsp"%>
</head>
<body>
<%@ include file="/snippets/textpagenosearch.jsp"%>
<%	String ipaddress=p.ipaddress;
	if (al.shopid==0) {
		out.write("Your account is not linked to any shop. Please <a href='/contact.jsp'>contact us</a> if you feel this is in error."); 
	} else if (!al.adenabled){
		response.sendRedirect("/shops/agreement.jsp");
	} else {
%>
<h3>Change banner for <%=Webroutines.getPartnerNameFromPartnerId(al.partnerid)%></h3>
<%
	// If a file was uploaded, fill fileItem with it
	if (ServletFileUpload.isMultipartContent(request)){
	  	ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
	  	List fileItemsList = servletFileUpload.parseRequest(request);
	  	String optionalFileName = "";
	  	if (fileItemsList.size()>0){
	  		if (fileItemsList.size()>1){
	  			message+=("Upload failed: you can only upload plain images (in GIF, JPEG or PNG format).");
	  		} else {
	      		fileItem = (FileItem)fileItemsList.get(0);
	      		if (fileItem.getSize()>Banner.maxsize*1024){
	      			message+=("Upload failed. Size of image is "+(int)(fileItem.getSize()/1024)+"kB.Maximum size of images is "+Banner.maxsize+".");
	      			fileItem=null;
	      			al.setAction("Bannerupload: file too big");
	      			al.setObjecttype("Banner");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();
	      		} else {
	      			if (fileItem!=null&&fileItem.getSize() > 0){
	      				ImageInfo ii=new ImageInfo();
	      				ii.setInput(fileItem.getInputStream()); // in can be InputStream or RandomAccessFile
	      				ii.setDetermineImageNumber(true); // default is false
	      				ii.setCollectComments(true); // default is false
	      				check=ii.check();
	      				if (check&&ii.getMimeType()!=null&&(ii.getMimeType().equals("image/gif")||ii.getMimeType().equals("image/png")||ii.getMimeType().equals("image/jpeg")||ii.getMimeType().equals("image/pjpeg"))){
	      					if (!((ii.getWidth()==234&&ii.getHeight()==60)||(ii.getWidth()==160&&ii.getHeight()==600))){
	      						
	      						message+=("<b>The image size ("+ii.getWidth()+"x"+ii.getHeight()+") does not fit the banner size of 234x60 pixels or skyscraper size of 160 x 600 pixels! Please upload a different image.</b><br/>");  
								showexample=false;
								hideuploaddialog=false;
								al.setAction("Banner too big");
								al.setObjecttype("Banner");
								logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
								logger.logaction();
								al.run();
	      					} else {
	      						if (banner==null) {
	    	      					banner=new Banner();
	    	      					banner.partnerid=al.partnerid;
	    	      					banner.shopid=al.shopid;
	    	      					banner.width=ii.getWidth();
	    	      					banner.height=ii.getHeight();
	    	      					
	    	      				} 
	    	      				
	      					java.io.InputStream inStream = fileItem.getInputStream();
	      					banner.setImage(ii.getMimeType().substring(6),inStream); 
	      					
	      					banner=Banner.load(banner.id,al.partnerid);
	      					if (bannerid==0){
	      						al.setAction("Created Banner");
    	      					al.setObjecttype("Banner");
    	      					al.setObjectid(banner.id+"");
    	      					logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
    	      					logger.logaction();
    	      					al.run();
	      					} else {
	      						al.setAction("Changed Banner image");
	      						al.setObjectid(banner.id+"");
    	      					al.setObjecttype("Banner");
    	      					logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
    	      					logger.logaction();
    	      					al.run();
	      					}
	      					bannerid=banner.id;
	      					message+="New banner image was saved. ";
	      					}
	      				}
	      			}
	      		}
	  		}
	  	}
	}
	
	
	if (action.equals("setlink")){
			
			if (!link.startsWith("http://")&&!link.startsWith("https://")){
	infoiscomplete=false;
	message+="Please supply a full URL including 'http://'<br/>";
	al.setAction("Bannerinfo: URL incomplete");
	      			al.setObjecttype("Banner");
	      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
	      			logger.logaction();
	      			al.run();
		} else {
			al.setOldvalue(banner.link+" "+al.getOldvalue());
			al.setNewvalue(link+" "+al.getNewvalue());
			banner.setLink(link);
			banner.shopid=al.shopid;
			banner.save();
				message+="Changes were saved. ";
				al.setAction("Banner settings stored");
      			al.setObjecttype("Banner");
      			al.setObjectid(bannerid+"");
      			logger=new Webactionlogger("Banner",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
      			logger.logaction();
      			al.run();
		}
	}
	
		

if (banner!=null){%>
<%=banner.html %><br/>
<%} else  {%>
<h2>Create a new banner</h2>
<%=message %>
<%} %>

<br/>
<form action="editbanner.jsp?bannerid=<%=(banner==null?"0":banner.id) %>" method="post" enctype="multipart/form-data">
<input name="myFile" type="file" size="40"/> <input type="submit" value="Upload" /></form>
<br />To change the banner, click Browse to select the file from your PC, then click the Upload button. <br/>
Banners or Skyscraper ads can be supplied as an image in the following formats: GIF, JPEG
(JPG) or PNG. Banners must have the exact size of 234 pixels wide and 60
pixels high, Skyscraper ads must be 160 pixels wide and 600 pixels high, and they should be <%=Banner.maxsize %>kb or less in file size.
<br /><br />
<% if (banner!=null){%>
<h2>Banner settings</h2>

<form action="editbanner.jsp" method="post">
				URL that the banner links to:<br>
				<input type="text" name="link" value="<%=(banner.getLink())%>" size="40"/><br/>
<br/>Countries targeted with this ad:<br/>
<% for (String country:Banner.countries.split(",")){ %>
	<input type='checkbox' name='<%=country %>' <%if (banner.targetcountries.contains(country)) out.write("checked='checked' ");%> /><% out.write(Webroutines.getCountryFromCode(country)); %><br/>
<%} %>
				<input type="hidden" name="action" value="setlink"/>
				<input type="hidden" name="bannerid" value="<%=banner.id%>"/>
				<input type="submit" value="Save changes" /> <%=message %>
				</form>
				<br/><br/>
				<%} %>
<a href='index.jsp' style='text-decoration:underline;'>Back to banner overview</a>

<%}
	%>
<%@ include file="snippets/footer.jsp"%>
</body>
</html>

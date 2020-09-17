
<%@page import="com.freewinesearcher.online.ExternalManager"%>
<%@page import="com.freewinesearcher.common.Shorturl"%><html><head>
<%@ page 
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.online.PageHandler"
	import = "com.freewinesearcher.common.Dbutil"
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
	<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%
	session = request.getSession(true); 
	String wineid=Webroutines.filterUserInput(request.getParameter("wineid"));
	String shopid=Webroutines.filterUserInput(request.getParameter("shopid"));
	String exturl=(request.getParameter("exturl"));
	String exttarget=(request.getParameter("exttarget"));

%>

<%	PageHandler p=PageHandler.getInstance(request,response,"Link Clicked");
	//p.logger.logaction();
	String url=Webroutines.getUrlFromId(wineid,shopid,request.getRemoteAddr(),request.getRemoteUser(),searchhistory);
	if (!"".equals(url)) {
		//url=ExternalManager.addGoogleParams(url);
		if (url.contains("boottle")) {
			Dbutil.logger.info("Boottle page "+url+" was accessed. IP="+request.getRemoteAddr()+". Useragent="+p.useragent);
		}
		response.setStatus(302);
		response.sendRedirect(url);
	%>
<title>Link to external site</title>
</head>
<body>
You should be forwarded to a new page. If this does not work, click <a href="<%=url%>">here</a>.
	<% 
	} else {
		if (exturl!=null){
			if (exturl.startsWith("http://twitter.com")){
				int indexoflink=exturl.indexOf("https://www.vinopedia.com");
				if (indexoflink>0){
				String text=exturl.substring(0,indexoflink);
				String vplink=exturl.substring(indexoflink,exturl.length());
				exturl=text+Shorturl.shorten(vplink);
				}
			}
			p=PageHandler.getInstance(request,response,"Partner Link Clicked: "+exttarget);
			p.logger.page=exturl;
			p.logger.logaction();
			%>
			You should be forwarded to a new page. If this does not work, click <a href="<%=exturl%>">here</a>.
			<% response.setStatus(302);response.sendRedirect(exturl);
		} else {
			
			response.setStatus(302);response.sendRedirect("https://www.vinopedia.com");
		}
	}
	
%>

		
		
</body> 
</html>
		
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="EN"><%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%> 
<head>
<title>
Browser Tool
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Extension index");%>
<%@ include file="/header2.jsp" %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","-1"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h1>Browser tool</h1>
Access price information and ratings from anywhere on the web!
<table><tr><td style='width:400px;padding:10px;'><h2>1. Highlight a wine and select "Find on Vinopedia" from the menu</h2></td><td style='width:400px;padding:10px;'><h2>2. Vinopedia finds the wine for you!</h2></td></tr>
<tr><td><img src='RPFF2.jpg' alt='Find a wine'/></td><td><img src='VP.jpg' alt='Find a wine'/></td></tr></table>
<h2>For Firefox, Chrome and Internet Explorer browsers</h2>
In order to use this function, you need to install a small extension in your browser. <br/><br/>
<table><tr >
<td style='width:400px;padding:10px;'><img style='cursor:pointer;cursor:hand;' src='ff.jpg' alt='FireFox' onclick="location.href='ffinstall.jsp'"/><h2>Firefox</h2>To install the extension, click the button. <br/><br/><button id="ffButton"
    onclick="location.href='ffinstall.jsp'">
    Install on Firefox</button><br/><br/>
    After installation, right click on any wine and select "Find on Vinopedia".
<br/><br/><br/>
<a href="https://chrome.google.com/webstore/detail/vinopedia/nojgpilcdfiblaijfbmdpbajoagdcgim" alt="install"><img style='cursor:pointer;cursor:hand;' src='cr.jpg' alt='Add to Chrome'"/></a><h2>Google Chrome</h2>To install the extension, click the button. A message may appear at the bottom of the screen to confirm the installation.<br/><br/><button id="crButton"
    onclick="location.href='https://chrome.google.com/webstore/detail/vinopedia/nojgpilcdfiblaijfbmdpbajoagdcgim'">
    Install on Chrome</button><br/><br/>
    After installation, select (highlight) a wine, right click and select "Find on Vinopedia".
    
    </td><td style='width:400px;padding:10px;'><img style='cursor:pointer;cursor:hand;' src='ie.jpg' alt='Internet Explorer' onclick="window.external.AddService('https://www.vinopedia.com/plugins/ie.jsp')"/><h2>Internet Explorer</h2>To install the extension, click the button. Select "Make this my default provider for this Accelerator Category", otherwise the search option is placed in a sub menu.<br/><br/><button id="ieButton"
    onclick="window.external.AddService('https://www.vinopedia.com/plugins/ie.jsp')">
    Install on Internet Explorer</button><br/><br/>
    <!--[if (IE)&(lte IE 7)]>
<b>Note: Your version of Internet Explorer is less than the required verion (version 8 or higher). Please upgrade to use use the extension.</b><br/>
<![endif]-->
    
    Please be aware: on Internet Explorer you first have to select (highlight) a wine. A blue marker appears then (see image below). Click it and select "Find on Vinopedia". That's it!<br/><br/>
    <img src='WIIE.jpg' alt='Selecting a wine in IE'/><br/>
    </td>

</tr></table>


    

<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	
<script type="text/javascript">$(document).ready(function(){setTimeout(function(){ initSmartSuggest(); }, 1500)});</script>
</body> 
</html>


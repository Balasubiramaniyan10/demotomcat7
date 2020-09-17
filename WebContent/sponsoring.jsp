<html>
<head>
<title>
Sponsoring vinopedia
</title>
<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<%@ page contentType="text/html; charset=UTF-8" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>vinopedia price quotes for publishers</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"Publishers");%>
<%@ include file="/header.jsp" %>
<% if ("new".equals(session.getAttribute("design"))){ %> 
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h3>Sponsoring</h3>
By sponsoring vinopedia, your site and wines will gain extra attention by vinopedia users. Sponsored links will show up before all other results are show and will be the first wines potential customers will see. Besides that, in the results list they are marked with a special color. An example is shown below.<br/><br/>
<img src='/images/sponsoredresults.PNG'><br/><br/>
<h3>A cost-effective marketing tool!</h3>
For any search, a maximum of 5 sponsored wines are shown before the complete list of results. Also, these wines are marked with a special color in the results list, gaining extra attention. If you decide to sponsor us, you will bid on a Cost-per-Click rate. You only pay if users actually click on the advertised wine and get directed to your web site. Which sponsored links are shown is determined by the auction principle: the 5 highest bidders are shown as sponsored links. This could mean that if you are the highest bidder and you have more than 5 wines matching the search criteria, all 5 wines shown are from your site. <br/>
<br/>
Sponsoring allows for targeting a very specific audience: the audience that you know for sure is interested in buying one of your wines. This makes it a very attractive and cost-effective solution for attracting more customers to your store. And it's on a No-Click-No-Pay basis: If visitors do not come to your site, it does not cost you anything!<br/>

If you are interested in sponsoring, please <a href='contact.jsp'>contact us</a>. 
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
<% } else {%>


<br/><br/>
<table border=0><tr><td><h3>Sponsoring</h3>
By sponsoring vinopedia, your site and wines will gain extra attention by vinopedia users. Sponsored links will show up before all other results are show and will be the first wines potential customers will see. Besides that, in the results list they are marked with a special color. An example is shown below.<br/><br/>
<img src='/images/sponsoredresults.PNG'><br/><br/>
<h3>A cost-effective marketing tool!</h3>
For any search, a maximum of 5 sponsored wines are shown before the complete list of results. Also, these wines are marked with a special color in the results list, gaining extra attention. If you decide to sponsor us, you will bid on a Cost-per-Click rate. You only pay if users actually click on the advertised wine and get directed to your web site. Which sponsored links are shown is determined by the auction principle: the 5 highest bidders are shown as sponsored links. This could mean that if you are the highest bidder and you have more than 5 wines matching the search criteria, all 5 wines shown are from your site. <br/>
<br/>
Sponsoring allows for targeting a very specific audience: the audience that you know for sure is interested in buying one of your wines. This makes it a very attractive and cost-effective solution for attracting more customers to your store. And it's on a No-Click-No-Pay basis: If visitors do not come to your site, it does not cost you anything!<br/>

If you are interested in sponsoring, please <a href='contact.jsp'>contact us</a>. 
 
</td></tr></table>
<%}%></body> 
</html>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="true"  
	import = "com.freewinesearcher.online.Webroutines"	
%>
<html>
<head>
<title>
About vinopedia
</title>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% PageHandler p=PageHandler.getInstance(request,response,"About");%>
<%@ include file="/header2.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","2"); %>
<%@ include file="/snippets/textpage.jsp" %>
<h2>How can a free service survive?</h2>
One of our goals is to become one of the best resources on the Internet for finding wine prices and information. We believe that if enough people use this service, we can survive by showing advertisements and by receiving donations from happy users. If we would start charging for this service, the number of people that can and will use it would be greatly reduced. And we didn't put all the effort in just to see a small number of people benefiting from it! That is why we do not charge for using Vinopedia. But we can't do this without you. <br/>
<h2>Spread the word!</h2>
If you like this service, <i>please mention www.vinopedia.com</i> in a blog, a forum posting, on your web site or tell your friends. That would really help us! We can only survive with enough visitors.
<h2>Donations</h2>
Running this service and keeping it up-to-date takes a big effort. If you use Vinopedia regularly, or you sell a lot of wines because your shop is listed on Vinopedia, please consider supporting us by making a donation. Any amount is more than welcome, but why not donate 10% of the money you saved or earned through Vinopedia?
<br/><br/><form action="https://www.paypal.com/cgi-bin/webscr" method="post">
<input type="hidden" name="cmd" value="_s-xclick"/>
<input type="hidden" name="hosted_button_id" value="1737259"/>
<input type="image" src="https://www.paypal.com/en_US/i/btn/btn_donate_LG.gif" style="border:0px;"  name="submit" alt="Donate"/>
<img alt="" style="border:0px;" src="https://www.paypal.com/en_US/i/scr/pixel.gif" width="1" height="1"/>
</form>
Thanks for your support!<br/><br/>
Jasper
<%@ include file="/snippets/textpagefooter.jsp" %>
<% } %>
	

</body> 
</html>
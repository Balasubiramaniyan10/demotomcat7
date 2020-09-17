<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.online.web20.*"
%>
<% long start=System.currentTimeMillis();
	String message="";
	boolean debuglog=false;%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start Pagehandler"); %>
<%	String action=request.getParameter("actie");
	if (action==null) action="";
	Publication publication=(Publication)session.getAttribute("publication");
	if (publication==null){
		publication=new Publication();
	}
	if (request.getParameter("editcomment")!=null){
		int commentid=-1;
		try{commentid=Integer.parseInt(request.getParameter("editcomment"));} catch (Exception e){}
		if (commentid>=0){
			publication=Publication.get(commentid);
			if (publication==null||request.getRemoteUser()==null||publication.user==null||!request.getRemoteUser().equals(publication.user.getUsername())) publication=new Publication();
	
		}
	
	}
	publication.update(request);
	session.setAttribute("publication",publication);
	if (action.equals("Save")){
		if (publication.isValid(request)){
			boolean saveok=publication.save();
			if (saveok){
				Subject subject=publication.subject;
				publication=new Publication();
				publication.subject=subject;
				session.setAttribute("publication",publication);
				message="Thanks for your contribution, it was saved successfully!";
			} else {
				message="We are sorry! A problem occurred while we were trying to save your stuff. Please try again later...";
			}
		} else {
			
		}
	}
	
	PageHandler p=PageHandler.getInstance(request,response);
	p.searchpage="/index.jsp";
	p.processSearchdata(request);
	searchdata.setVintage("");

%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
	if (!searchdata.getName().equals("")) {
		out.print(Webroutines.escape(p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ", "")) + " "
				+ p.getTranslator().get("pricesbyfws"));
	} else {
		out.print("vinopedia");
	}
%></title>
<%
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<script language="JavaScript" type="text/javascript" src="/js/starrating.js"></script>
<style type="text/css">
#star ul.star { list-style: none; margin: 0; padding: 0; width: 85px; height: 20px; left: 10px; top: -5px; position: relative; float: left; background: url('/css/stars.gif') repeat-x; cursor: pointer; }
#star li { padding: 0; margin: 0; float: left; display: block; width: 85px; height: 20px; text-decoration: none; text-indent: -9000px; z-index: 20; position: absolute; padding: 0; }
#star li.curr { background: url('/css/stars.gif') left 25px; font-size: 1px; }
#star div.user { visibility:hidden; left: 15px; position: relative; float: left; font-size: 13px; font-family: arial; color: #888; width:30px;}
div.star ul.star { list-style: none; margin: 0; margin-top:10px;padding: 0; width: 85px; height: 20px; left: 10px; top: -5px; position: relative; float: left; background: url('/css/stars.gif') repeat-x; }
div.star li { padding: 0; margin: 0; float: left; display: block; width: 85px; height: 20px; text-decoration: none; text-indent: -9000px; z-index: 20; position: absolute; padding: 0; }
div.star li.curr { background: url('/css/stars.gif') left 25px; font-size: 1px; }
div.star div.user { left: 15px; margin-top:10px;position: relative; float: left; font-size: 13px; font-family: arial; color: #888; }
</style>
<%@ include file="/header2.jsp" %>
</head>
<body onclick="javascript:emptySuggest();" ">
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%>
</div>
<div class='main'>
<div id='mainleft'>	
<%=message.equals("")?"":"<h1>"+message+"</h1>" %>
<% if (publication.subject!=null){%>
<% Publications pubs=new Publications(publication.subject,1);
	if (pubs.size()>0) out.write("<h2>Comments about "+publication.subject.getSubjectInfo()+"</h2>");
	for (Publication pub:pubs){
		out.write(pub.getAllContent(request));
	}
%>
<h1><%=(publication.id>0?"Edit your comment":"Add a comment") %></h1>
<br style="clear:both; margin: 7px 0 0">
<%=publication.content.rating.getHTML(true) %>
<%=publication.content.rating.getRatingComment(publication.subject.type) %>
<div id="updateresult"></div>
<br style="clear: both; margin-bottom: 4px;">


<div>
<form action="<%=p.thispage %>" method="post" enctype="multipart/form-data">
Enter your comment<%=(publication.subject.type==Subject.Types.WINE?" or tasting note":"") %>:<br/>
<textarea id="comment" name='comment' cols='40' rows='5'><%=publication.content.comment.getComment() %></textarea>
<br/>
</div>


<h2>Upload a video about this <%=publication.subject.type.toString().toLowerCase() %></h2>
	  <input name="myFile" type="file" />
<% if (publication.isValid(request)){%>
<input type='submit' name='actie' value='Save'/>
</form>

<br/>

<% 	}%>


<%@ include file="/snippets/footer.jsp" %>	
</div>
</div> <!--  main-->
<%} else {
%><h2>Sorry... we could not determine which subject you wanted to add a comment about.</h2>
<%} %>
<%} %>
	
	

</body>
</html>
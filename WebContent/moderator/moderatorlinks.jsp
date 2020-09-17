<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.batch.Spider"%>
<jsp:include page="/header2.jsp" />
<%if (request.getParameter("shopid")!=null) try{request.setAttribute("shopid",Integer.parseInt(request.getParameter("shopid")));}catch(Exception e){} %>
<a href='<%=Dbutil.readValueFromDB("select * from config where configkey='todourl'", "value")%>'>To Do List</a><br/>
<a href='/admin/menu.jsp'>Admin menu</a><br/>
<a href='editshop.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid")+"&amp;actie=retrieve");%>'>Add or edit shop information</a><br/>
<a href='edittablescraper.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid")+"&amp;actie=retrieve");%>'>Add or edit table scraper</a><br/>
<a href='editspiderregex.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid")+"&amp;actie=retrieve");%>'>Add or edit URL spider for normal pages</a><br/>
<a href='/moderator/testshop.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid"));%>'>Test a shop</a><br/>
<a href='editaspxspider.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid"));%>'>Add or edit URL spider for aspx pages (Javascript)</a><br/>
<a href='shopstodo.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid"));%>'>Add shops from Wine Searcher</a><br/>
<a href='analyzer.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid"));%>'>Add or edit smart analyzer</a><br/>
<a href='/moderator/analyzewines.jsp<%if(request.getAttribute("shopid")!=null) out.write("?shopid="+(Integer)request.getAttribute("shopid"));%>'>Recognize wines from a shop</a><br/>
<a href='shopstats.jsp?sort=lastknowngooddays+desc'>View shop statuses</a><br/>
<%if(request.getAttribute("shopid")!=null) out.write("<a href='/moderator/manage.jsp?shopid="+(Integer)request.getAttribute("shopid")+"'>View batch logging of this shop</a><br/>");%>
<a href='selectlabels.jsp'>Edit wine labels</a><br/> 
<%String helppage=Spider.SQLEscape(PageHandler.getInstance(request,response).thispage);
helppage=helppage.substring(helppage.indexOf("vinopedia.com")+13);
if (helppage.indexOf("?")>0) helppage=helppage.substring(0,helppage.indexOf("?"));
%>
<% if (request.isUserInRole("admin")||Dbutil.readIntValueFromDB("select * from helpscreens where page='"+helppage+"';","id")>0){%>
<a href='/moderator/help.jsp?page=<%=helppage%>' target='_blank'>Help</a><br/><%} %>
<% if (request.getParameter("shopid")!=null){%><a href='issuelog.jsp?shopid=<%=request.getParameter("shopid")%>' >Issuelog</a><br/><%} %>

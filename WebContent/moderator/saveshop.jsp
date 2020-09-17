<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.common.Context"
	%>

<%@page import="com.freewinesearcher.batch.TableScraper"%>
<%@page import="com.freewinesearcher.online.Shopapplication"%>
<%@page import="com.freewinesearcher.common.datafeeds.DataFeed"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.text.SimpleDateFormat"%><html>
<head> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<% 	
 	PageHandler p=PageHandler.getInstance(request,response,"Save shop");%>
<%@ include file="/snippets/headeraudit.jsp" %>
<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
<%@ include file="/snippets/textpage.jsp" %>
<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
</head>
<body>
<%	String shopname = request.getParameter("shopname");
String shopurl = request.getParameter("shopurl");
long wsid=0;
try {wsid = Integer.parseInt(request.getParameter("wsid"));}catch (Exception e){}
String datafeedurl = request.getParameter("datafeedurl");
String auto= request.getParameter("auto");
	if (auto==null) auto="";
	String email = request.getParameter("email");
	if (email==null) email="";
	String address = request.getParameter("address");
	if (address==null) address="";
	Dbutil.logger.info(Webroutines.URLDecodeUTF8(address));
	String disabled = request.getParameter("disabled");
	if (disabled==null) disabled="off";
	if (disabled.equals("on")){
		disabled="1";
	} else {
		disabled="0";
	}
	String linkback = request.getParameter("linkback");
	if (linkback==null) linkback="";
	String sponsoringshop = request.getParameter("sponsoringshop");
	if (sponsoringshop==null) sponsoringshop="off";
	if (sponsoringshop.equals("on")){
		sponsoringshop="1";
	} else {
		sponsoringshop="0";
	}
	int shopidint=0;
	String shopid = request.getParameter("shopid");
	if (shopid==null||shopid.equals("")||shopid.equals("New")) shopid="0";
	if (shopid.startsWith("auto")) {
	auto = "auto";
	shopid=shopid.substring(4);
	}
	boolean succes=false;
	String url = request.getParameter("url");
	String type = "Fixed";
	String vat= request.getParameter("vat");
	String country = request.getParameter("country");
	String vatnumber = request.getParameter("vatnumber");
	if (vatnumber==null) vatnumber="";
	String invoiceaddress = request.getParameter("invoiceaddress");
	if (invoiceaddress==null) invoiceaddress="";
	String  invoiceemail= request.getParameter("invoiceemail");
	if (invoiceemail==null) invoiceemail="";
	String contactname = request.getParameter("contactname");
	if (contactname==null) contactname="";
	String commercialcomment = request.getParameter("commercialcomment");
	if (commercialcomment==null) commercialcomment="";
	String staffelname = request.getParameter("staffelname");
	if (staffelname==null) {
		staffelname="defaultEU";
		if (country.equals("US")) staffelname="defaultUS";
	}
	Timestamp starttrial=null;
	Timestamp startpaying=null;
	SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");

	try {starttrial=new Timestamp(df.parse(request.getParameter("starttrial")).getTime());}catch(Exception e){}
	try {startpaying=new Timestamp(df.parse(request.getParameter("startpaying")).getTime());}catch(Exception e){}

	if (url!=null) if (type!=null) if (!url.equals("")) if (!type.equals("")) if (shopname!=null) if (!shopname.equals("")) {
		shopidint = Webroutines.Saveshop(shopid,shopname, shopurl, url,type,country,vat,auto, email,address,disabled,vatnumber,
				invoiceaddress,invoiceemail,contactname,commercialcomment,staffelname,starttrial,startpaying,sponsoringshop,linkback);
		if (shopidint>0){
			if (wsid>0) Dbutil.executeQuery("update wsshops set shopid="+shopidint+" where wsid="+wsid+";");
			if (datafeedurl!=null&&!datafeedurl.equals("")) TableScraper.addTableScrapeRow(shopidint+"", "0", "Master", datafeedurl, "", "", "", "", "","","","","", "", "", "","","","", auto,false);
		}
	}
		
	if (shopidint==0){%>
		<H1>Oeps! Update didn't go as expected!!! Press the Back button and see what is wrong!</H1>
		<% } else { 
			Auditlogger al2=new Auditlogger(request);
			if ("0".equals(shopid)) {
				al2.setAction("Save new shop");
			} else {
				al2.setAction("Edit shop info");
			}
			try{al2.setShopid(shopidint);}catch (Exception e){}
			al2.logaction();
			if (disabled.startsWith("1")){
				%>
				<jsp:forward page="/moderator/issuelog.jsp"> 
				<jsp:param name="shopid"  value="<%=shopidint%>" />
				<jsp:param name="status"  value="3" />
				</jsp:forward>
				<%
			}
			if (wsid>0){
				Shopapplication s=Shopapplication.retrieve(wsid);
				if (s==null) s=Shopapplication.generate(wsid);
				if (s!=null){
					s.setShopid(shopidint);
					s.setStatus("Added");
					s.save();
					String empty="";
					if (s.getUrldatafeed()!=null&&!s.getUrldatafeed().equals("")&&s.getUrldatafeed().contains(".")){%>
						<jsp:forward page="/settings/editdatafeed.jsp"> 
						<jsp:param name="shopid"  value="<%=shopidint%>" />
						<jsp:param name="url"  value="<%=s.getUrldatafeed()%>" />
						<jsp:param name="action"  value="Analyze" />
						<jsp:param name="wsid" value="<%=wsid%>" />
						</jsp:forward>
						<%
					} else if (s.isBevmedia()||Dbutil.readIntValueFromDB("select * from wsshops where wsid="+wsid,"bevmedia")==1){%>
					<% String feedurl=s.getUrlhomepage()+"/winedirectory.asp?request=wv";
					DataFeed feed=new DataFeed(feedurl);
					feed.shopid=s.getShopid();
					if (feed.feedstatus.equals(DataFeed.feedstatusses.OK)) {
						if (feed.save(new Context(request))>0){
							%>
							<jsp:forward page="/moderator/testshop.jsp?actie=testshop"> 
							<jsp:param name="shopid"  value="<%=shopidint%>" />
							</jsp:forward>
							<%
							
						}
					}
					%>Damn, I wanted to save the data feed for this BevMedia shop but something went wrong. Call Jasper!<%
				} else {
					%>
					<jsp:forward page="edittablescraper.jsp">
					<jsp:param name="shopid"  value="<%=shopidint%>" />
					<jsp:param name="url"  value="<%=(s.getUrldatafeed()!=null&&s.getUrldatafeed().length()>0?s.getUrldatafeed():s.getUrlbrowsethrough())%>" />
					<jsp:param name="masterurl"  value="<%=(s.getUrldatafeed()!=null&&s.getUrldatafeed().length()>0?empty:s.getUrlbrowsethrough())%>" />
					
					<jsp:param name="wsid" value="<%=wsid%>" />
					</jsp:forward>
				<%}
				}
			} else {		
			if (session.getAttribute("savexp")!=null&&(Boolean)session.getAttribute("savexp")){
				Context c=(Context)session.getAttribute("context");
				if (c==null){
					c=new Context(request);
					session.setAttribute("context",c);
				}
				if (c.an==null||c.an.config==null) {
					out.write("Problem: I need to save a new xpathparser, but the xpathparser is null. Please contact the administrator.");
					Dbutil.logger.error("Problem: I need to save a new xpathparser, but the xpathparser is null. Please contact the administrator.");
				} else {
					c.an.shopid=shopidint;
					long resultid=c.an.save(c);
					if (resultid==0){
						out.write("Problem: I could not save the xpathparser. Please contact the administrator.");
						Dbutil.logger.error("Problem: I could not save the xpathparser. Shopid="+shopidint);
				
					} else {
				
					
				
			
			
			
			
			String message = "Shop "+shopidint+" was saved successfully.";
		
			
				String fwdurl="urlspider.jsp?shopid="+c.an.shopid+"&masterurl="+c.an.url;%>
				<jsp:forward page="<%=fwdurl%>">
				<jsp:param name="shopid"  value="<%=shopid%>" />
				<jsp:param name="message" value="<%=message%>" />
				</jsp:forward>
			
		<%
		}
				}
		} else {
			request.setAttribute("shopid",shopidint);
		
			%>
			<%@ include file="moderatorlinks.jsp" %>
			<br/>
			Shop information for shop <%=shopidint %> was saved correctly.
		<%
		}
			}
		}
		}%></body></html>

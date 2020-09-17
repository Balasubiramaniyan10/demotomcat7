<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.batch.XpathWineScraper"%>
<%@page import="com.freewinesearcher.batch.Spider"%>
<%@page import="com.freewinesearcher.online.Shopapplication"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.*"%><html>
<script type="text/javascript"> 
<!--
function submitForm(actionPage) {
  document.getElementById('formOne').action=actionPage;
  document.getElementById('formOne').submit();
  return 0;
} 

function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
	}
	submitForm('<%=response.encodeURL("editshop.jsp")%>');
	return 0;
}

-->
</script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Context"
%>
<title>
Add a shop</title>
<% 	PageHandler p=PageHandler.getInstance(request,response,"Edit shop");%>

<% request.setAttribute("ad","false"); %>
<% request.setAttribute("numberofimages","0"); %>
</head>
<body>
<div class="textpage">
<jsp:include page="moderatorlinks.jsp" />
<%	String xp=request.getParameter("xp");
	String shopname = request.getParameter("shopname");
String shopid = request.getParameter("shopid");
if (shopid==null||shopid.equals("")) shopid="0";


String auto="";
if (shopid.startsWith("auto")) {
	auto = "auto";
	shopid=shopid.substring(4);
}
	boolean alreadyexists=false;
	boolean urlalreadyexists=false;
	String url = request.getParameter("url");
	String datafeedurl = request.getParameter("datafeedurl");
	String shopurl = request.getParameter("shopurl");
	String regex = request.getParameter("regex");
	String order = request.getParameter("order");
	String address = request.getParameter("address");
	String linkback = request.getParameter("linkback");
	if (linkback==null) linkback="";
	String email = request.getParameter("email");
	String actie = request.getParameter("actie");
	String urltype="";
	String vat=request.getParameter("vat");
	String countrycode=request.getParameter("countrycode");
	String currency="";
	String disabled=request.getParameter("disabled");
	String sponsoringshop=request.getParameter("sponsoringshop");
	if (shopname==null) shopname="";
	if (url==null) url="";
	if (datafeedurl==null) datafeedurl="";
	if (!datafeedurl.equals("")&&url.equals("")) {
		if (datafeedurl.indexOf("/",8)>0){
			url= datafeedurl.substring(0,datafeedurl.indexOf("/",8));
		} else {
			url= datafeedurl;
		}
	}
	if (regex==null) regex="";
	if (order==null) order="";
	if (vat==null) vat="2";
	if (vat.equals("N")) vat="1";
	if (vat.equals("Y")) vat="0";
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
	if (staffelname==null) staffelname="";
	Timestamp starttrial=null;
	Timestamp startpaying=null;
	SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");

	try {starttrial=new Timestamp(df.parse(request.getParameter("starttrial")).getTime());}catch(Exception e){}
	try {startpaying=new Timestamp(df.parse(request.getParameter("startpaying")).getTime());}catch(Exception e){}
	if (starttrial==null) {
		TimeZone tz = TimeZone.getTimeZone("Europe/Madrid");

		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MONTH, +1);
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		
		starttrial = new Timestamp(cal.getTime().getTime()); 
			
	}
	if (startpaying==null){
		
		TimeZone tz = TimeZone.getTimeZone("Europe/Madrid");

		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MONTH, +3);
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		startpaying = new Timestamp(cal.getTime().getTime()); 
		
	}
	
	if (email==null) email="";
	if (address==null) address="";
	if (disabled==null) disabled="0";
	long wsid=0;
	try {wsid = Integer.parseInt(request.getParameter("wsid"));}catch (Exception e){}
	
	if (shopid.equals("0")&&wsid>0){
		Shopapplication s=Shopapplication.retrieve(wsid);
		if (s==null) s=Shopapplication.generate(wsid);
		if (s!=null){
			shopname=s.getShopname();
			address=s.getAddress().trim()+(s.getCountry().length()>0?", "+s.getCountry():"");
			if (address.contains("address")) address="";
			shopurl=s.getUrlhomepage();
			if (shopurl.indexOf("/", 9)>0) {
				url=shopurl.substring(0, shopurl.indexOf("/", 9));
			} else {
				url=shopurl;
			}
			url=shopurl;
			email=s.getStoreemailaddressforcustomers();
			datafeedurl=s.getUrldatafeed();
			vat=(s.isIncludingvat()?"0":"1");
			countrycode=Dbutil.readValueFromDB("select * from vat where country='"+Spider.SQLEscape(s.getCountry())+"'","countrycode");
			
		}
	}
	
	if (xp!=null&&xp.equals("true")&&new Context(request)!=null){
		Context c=(Context)session.getAttribute("context");
		if (c.an!=null){
			shopname=c.an.domainurl.replace("http://www.","").replace("http://","");
			shopurl=c.an.domainurl;
			url=c.an.domainurl;
			countrycode=XpathWineScraper.guessCountryCode(c.an.domainurl,0);
		}
		
	}
	if (!shopname.equals("")&&shopid.equals("0")) alreadyexists=(0<Dbutil.readIntValueFromDB("select * from shops where shopname like '"+Spider.SQLEscape(shopname)+"';","id"));
	if (!url.equals("")&&shopid.equals("0")) urlalreadyexists=(0<Dbutil.readIntValueFromDB("select * from shops where shopurl like '"+Spider.SQLEscape(url)+"%';","id"));

	LinkedHashMap<String,Integer> wsshops=Webroutines.getWSShops();
	ArrayList<String> countries = Webroutines.getCountries();
	String regexescaped= regex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	ArrayList shops = Webroutines.getShopList("");
	ArrayList autoshops = Webroutines.getShopList("auto");
	if (actie!=null&&actie.equals("retrieve")&&!"0".equals(shopid)){
		ArrayList<String> rowvalue=Webroutines.getShopInfo(shopid,auto);
		shopname=rowvalue.get(0);
		shopurl=rowvalue.get(1);
		url=rowvalue.get(2);
		urltype=rowvalue.get(3);
		vat=rowvalue.get(4);
		countrycode=rowvalue.get(5);
		currency=rowvalue.get(6);
		email=rowvalue.get(7);
		address=rowvalue.get(8);
		disabled=rowvalue.get(9);
		starttrial=null;
		startpaying=null;
		try {starttrial=Timestamp.valueOf(rowvalue.get(10));}catch(Exception e){}
		try {startpaying=Timestamp.valueOf(rowvalue.get(11));}catch(Exception e){}
		
		staffelname=rowvalue.get(12);
		vatnumber=rowvalue.get(13);
		invoiceaddress=rowvalue.get(14);
		invoiceemail=rowvalue.get(15);
		contactname=rowvalue.get(16);
		commercialcomment=rowvalue.get(17);
		sponsoringshop=rowvalue.get(18);
		linkback=rowvalue.get(19);
		
	} 
	
if(false){	%>Retrieve shop data from Wine-searcher/Snooth
	<form action='' ><select name="wsid" ><% for (String sname:wsshops.keySet()){%><option value="<%=wsshops.get(sname)%>"><%=sname%></option><%} %></select>
	<input type='submit' value='Get shop data'></form>_______________________________________________________<br/><br/><br/>
<%}  %>
<h2>Edit shop data</h2>
<%if (url!=null&&url.startsWith("http")){ %>Visit store site: <a href='<%=url %>' target='_blank'><%=url %></a><br/><%} %>
<FORM ACTION="<%=response.encodeURL("saveshop.jsp")%>" id="formOne" method="post">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" >
<option value="New">New
<% for (int i=0;i<autoshops.size();i=i+2){%>
<option value="auto<%=autoshops.get(i)%>"<% if ((autoshops.get(i).equals(shopid))&&(auto.equals("auto")) ) out.print(" Selected");%>>(auto) <%=autoshops.get(i+1)%>
<%}%>
<% for (int i=0;i<shops.size();i=i+2){%>
<option value="<%=shops.get(i)%>"<% if ((shops.get(i).equals(shopid))&&(auto.equals("")) ) out.print(" Selected");%>><%=shops.get(i+1)%>
<%}%>
</select></TD></TR>
<TR><TD>Shop Name</TD><TD><INPUT TYPE="TEXT" NAME="shopname" size="100" value="<%=shopname%>"><%if (alreadyexists) out.write ("<br/><font color='Red'>Warning: a shop with this name already exists!</font>"); %></TD></TR>
<TR><TD><font <%if (shopurl!=null&&!shopurl.toLowerCase().startsWith("http")) out.write ("color='Red'"); %>>Normal Url for shop</font></TD><TD><INPUT TYPE="TEXT" NAME="shopurl" size="100"  value="<%=shopurl%>"><%if (urlalreadyexists) out.write ("<br/><font color='Red'>Warning: a shop with this url already exists!</font>"); %></TD></TR>
<TR><TD><font <%if (url!=null&&!url.toLowerCase().startsWith("http")) out.write ("color='Red'"); %>>Base Url for finding wines</font></TD><TD><INPUT TYPE="TEXT" NAME="url" size="100"  value="<%=url%>"></TD></TR>
<TR><TD>Country</TD><TD><select name="country" >
<% for (int i=0;i<countries.size();i=i+2){%>
<option value="<%=countries.get(i)%>" <%if (countries.get(i).equals(countrycode)) out.write(" selected='selected'");  %>><%=countries.get(i+1)%>
<%}%>
</select></TD></TR>
<TR><TD>VAT</TD><TD><select name="vat">
<option value="0"  <%if (vat.equals("0")) out.write("Selected"); %>>Included
<option value="1"  <%if (vat.equals("1")) out.write("Selected"); %>>Excluded
<option value="2"  <%if (vat.equals("2")) out.write("Selected"); %>>Unknown!!!
</select></TD></TR>
<TR><TD>Email</TD><TD><INPUT TYPE="TEXT" NAME="email" size="100" value="<%=email%>"></TD></TR>
<TR><TD>Address</TD><TD><INPUT TYPE="TEXT" NAME="address" size="100" value="<%=address%>"></TD></TR>
<TR><TD>URL of link back to Vinopedia</TD><TD><INPUT TYPE="TEXT" NAME="linkback" size="100" value="<%=linkback%>"></TD></TR>
<TR><TD>Disabled</TD><TD><INPUT TYPE="checkbox" NAME="disabled" <%=("1".equals(disabled)?"CHECKED":"")%>></TD></TR>
<TR><TD>Sponsoring shop</TD><TD><INPUT TYPE="checkbox" NAME="sponsoringshop" <%=("1".equals(sponsoringshop)?"CHECKED":"")%>></TD></TR>
<TR><TD>Start trial period</TD><TD><INPUT TYPE="TEXT" NAME="starttrial" size="100" value="<%=(starttrial==null?"":starttrial.toString().substring(0,10))%>"></TD></TR>
<TR><TD>Start paying period</TD><TD><INPUT TYPE="TEXT" NAME="startpaying" size="100" value="<%=(startpaying==null?"":startpaying.toString().substring(0,10))%>"></TD></TR>
<TR><TD>VAT number</TD><TD><INPUT TYPE="TEXT" NAME="vatnumber" size="100" value="<%=vatnumber%>"></TD></TR>
<TR><TD>Invoice address</TD><TD><INPUT TYPE="TEXT" NAME="invoiceaddress" size="100" value="<%=invoiceaddress%>"></TD></TR>
<TR><TD>Invoice email</TD><TD><INPUT TYPE="TEXT" NAME="invoiceemail" size="100" value="<%=invoiceemail%>"></TD></TR>
<TR><TD>Contact name</TD><TD><INPUT TYPE="TEXT" NAME="contactname" size="100" value="<%=contactname%>"></TD></TR>
<TR><TD>Contract comment</TD><TD><INPUT TYPE="TEXT" NAME="commercialcomment" size="100" value="<%=commercialcomment%>"></TD></TR>

</TABLE>
	<INPUT TYPE="HIDDEN" NAME="auto" id="auto" value="<%=auto%>">
	<INPUT TYPE="HIDDEN" NAME="datafeedurl" id="datafeedurl" value="<%=datafeedurl%>">
	<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test">
    <INPUT TYPE="HIDDEN" NAME="wsid" id="wsid" value="<%=wsid %>">
    <input type="submit" value="Save"/>
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
</FORM>
Specify the main URL including http:// without a / at the end. <br/>
Select Fixed for shops that have a single HTML page with all prices, Spider for shops with dynamic pages which should be crawled.
</div>
</body> 
</html>
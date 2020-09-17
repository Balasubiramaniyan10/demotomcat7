<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Searchdata"
	import = "java.util.regex.Matcher"
	import = "java.util.regex.Pattern"

	
%>
<%
	String name=Webroutines.filterUserInput(request.getParameter("name"));
	if (name==null) name=""; 
	name=name.replace("Type wine name here","");
	String vintage="";
	String compileme;
	Pattern pattern;
	Matcher matcher;
	pattern = Pattern.compile("(\\d\\d\\d\\d)");
	matcher = pattern.matcher(name);
	if (matcher.find()){
		vintage=matcher.group(1);
		name=name.replaceAll("\\d\\d\\d\\d+","");
	}
	Webroutines.logWebAction("FWSjs",request.getServletPath(),request.getRemoteAddr(),  request.getHeader("referer"), name,vintage, 0,new Float(0.0).floatValue(), new Float(0.0).floatValue(), "", false, "", "", "", "",0.0);
	String html;
	String tablestyle="style='border:0;top-margin:0;left-margin:0;right-margin:0;font-family:Georgia;font-size:11px;width:100%;table-layout: fixed;border-collapse: collapse;border-width: 0px;color:#4d0027;text-align:left;font-weight:normal;'";
	String trstyle="style='height:14px;text-align:left;'";
	String trstyleodd="style='height:14px;text-align:left;background-color:#f5e5d8;'";
	String tdstyle="style='overflow:hidden;white-space:nowrap;font-weight:normal;'";
	String hrefstyle="style='text-decoration:none;color:#4d0027; {color: #4d0027; text-decoration:none;}  /* a+=0 b+=0 c+=0 */ :visited {color: #4d0027;} /* a+=0 b+=1 c+=0 */ :hover {color:#4d0027}  /* a+=0 b+=1 c+=0 */  :visited:hover {color: #4d0027} '";
	String td2style="style='width:50px;'";
	
	int row=0;
	if (name.replaceAll(" ","").replaceAll("\\d","").length()<3){
		html="<font style='color:blue;font-family:arial;font-size:11;'>Enter at least 3 characters</font>";
	} else {
	NumberFormat format  = new DecimalFormat("#,##0.00");	
		Wineset wineset = new Wineset(name,vintage, 0,new Float(0.0),new Float(0.0),new Float(0.0),"All", false, "","priceeuroex",0,5);
		if (wineset.Wine.length==0){
	html="<font style='color:blue;font-family:arial;font-size:11;background-color: #ffffff;'>No results found</font>";
		} else {
	html="<table "+tablestyle+"><tr "+trstyle+"background-color: #ffffff;'><TD "+tdstyle+">"+"<strong>"+wineset.records+" wines found. Cheapest results:</strong></TD><TD "+td2style+"></TD></TR>";
	for (int i=0;i<wineset.Wine.length;i++){
		row++;
		html=html+"<tr ";
		if (row%2==0) html+=trstyle;
		if (row%2==1) html+=trstyleodd;
		html=html+"'><td "+tdstyle+"><a class='FWSa' href='https://www.vinopedia.com?name="+Webroutines.URLEncode(name)+"&amp;vintage="+vintage+"' title='"+Webroutines.formatSizecompact(wineset.Wine[i].Size)+" "+Spider.escape(wineset.Wine[i].Name).replaceAll("\"","&quot;").replaceAll("'","&quot;")+" "+wineset.Wine[i].Vintage+": &euro; "+format.format(wineset.Wine[i].Price)+"' TARGET='_blank' "+hrefstyle+">"+wineset.Wine[i].Vintage+" "+Spider.escape(wineset.Wine[i].Name)+" "+Webroutines.formatSizecompact(wineset.Wine[i].Size)+"</a></td><td align='right' "+td2style+">"+"&euro;"+format.format(wineset.Wine[i].Price)+"</td></tr>";
	}
	int limit = wineset.records-5;
	if (limit>5) limit=5;
	if (limit>0){
		row=0;
		html=html+"<tr "+trstyle+"background-color: #ffffff;'><td "+tdstyle+">"+"<strong>Most expensive results:</strong></TD><TD "+td2style+"></TD></TR>";
		wineset = new Wineset(name,vintage, 0,new Float(0),new Float(0),new Float(0),"All", false, "","priceeuroex DESC",0,limit);
		for (int i=wineset.Wine.length-1;i>=0;i--){
	row++;
	html=html+"<tr ";
	if (row%2==0) html+=trstyle;
	if (row%2==1) html+=trstyleodd;
	html=html+"'><td style='overflow:hidden;white-space:nowrap;'><a href='https://www.vinopedia.com?name="+Webroutines.URLEncode(name)+"&amp;vintage="+vintage+"' title='"+Webroutines.formatSizecompact(wineset.Wine[i].Size)+" "+Spider.escape(wineset.Wine[i].Name).replaceAll("\"","&quot;").replaceAll("'","&quot;")+" "+wineset.Wine[i].Vintage+": &euro; "+format.format(wineset.Wine[i].Price)+"' TARGET='_blank' "+hrefstyle+">"+wineset.Wine[i].Vintage+" "+Spider.escape(wineset.Wine[i].Name)+" "+Webroutines.formatSizecompact(wineset.Wine[i].Size)+"</A></TD><TD align='right' "+td2style+">"+"&euro;"+format.format(wineset.Wine[i].Price)+"</TD></TR>";
		}
	}
	html=html+"<tr "+trstyle+"background-color: #ffffff;'><td "+tdstyle+" colspan='2'>"+"<a "+hrefstyle+" href='https://www.vinopedia.com?name="+Webroutines.URLEncode(name)+"&amp;vintage="+vintage+"'><strong>Get more results from vinopedia</strong></a></TD></TR>";
	html=html+"</table>";
		 
	
		}
	}
		html=html.replaceAll("'","\"");
	out.print("document.getElementById('TDFWSresults').innerHTML='"+html+"';");
%>

	

<%@page import="com.freewinesearcher.online.PageHandler"%><%@ page   
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "java.util.regex.Matcher"
	import = "java.util.regex.Pattern"
	
%>
<%@ page 
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.net.*"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.batch.Spider"
	import = "java.util.ArrayList"
%>
<%
PageHandler p=PageHandler.getInstance(request,response,"Wise search");
p.processSearchdata(request);
p.searchdata.setNumberofrows(5);
p.searchdata.setName(p.searchdata.getName().replaceAll("(^|\\s+)(w|W)ine()($|\\s+)"," ").replaceAll("(^|\\s+)(P|p)rices?()($|\\s+)"," "));
p.doSearch(request);
int rows=p.s.wineset.records;
p.searchdata.setSize("0.75");
p.doSearch(request);
String ipaddress="";
	if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
		ipaddress = request.getRemoteAddr();
	} else {
	    ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
	}
	String country=Webroutines.getCountryCodeFromIp(ipaddress);
	if (country.equals("NZ")){
	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0);
	} else {
	
	String callback=Webroutines.filterUserInput(request.getParameter("callback"));
	String compileme;
	Pattern pattern;
	Matcher matcher;
	pattern = Pattern.compile("(\\d\\d\\d\\d)");
	String html;
	String tablestyle="style='border:0;top-margin:0;left-margin:0;right-margin:0;font-family:Georgia;font-size:11;width:100%;table-layout: fixed;border-collapse: collapse;border-width: 0px;color:blue;text-align:left;font-weight:normal; {color: blue; text-decoration:none;}  /* a+=0 b+=0 c+=0 */ :visited {color: blue} /* a+=0 b+=1 c+=0 */ :hover {color:blue}  /* a+=0 b+=1 c+=0 */  :visited:hover {color: blue} '";
	String trstyle="style='height:14px;text-align:left;";
	String trstyleodd="style='height:14px;text-align:left;background-color:#f1e7ec;";
	String tdstyle="style='overflow:hidden;white-space:nowrap;font-weight:normal;'";
	String astyle="style='text-decoration:none;color:#4d0027; {color: #4d0027; text-decoration:none;}  /* a+=0 b+=0 c+=0 */ :visited {color: #4d0027;} /* a+=0 b+=1 c+=0 */ :hover {color:#4d0027}  /* a+=0 b+=1 c+=0 */  :visited:hover {color: #4d0027} '";
	String td2style="style='width:50px;'";
	tablestyle="";
	trstyle="";
	trstyleodd="";
	tdstyle="";
	astyle="";
	td2style="";
	
	//String hrefstyle="style='{color: blue; text-decoration:none;}  /* a+=0 b+=0 c+=0 */ :visited {color: blue} /* a+=0 b+=1 c+=0 */ :hover {color:blue}  /* a+=0 b+=1 c+=0 */  :visited:hover {color: blue} '";
	String font="<font style='color:blue;font-family:arial;font-size:11;font-weight:normal;'>";
	int row=0;
	
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	if (p.s.wineset.records>0){
	html="<div>"+rows+" wine deals found. The best ones:</div>";
	for (int i=0;i<p.s.wineset.Wine.length;i++){
		row++;
		html=html+"<div style='height:14px;width:100%;overflow:hidden;white-space:nowrap;'><a style='style='height:14px;overflow:hidden;white-space:nowrap;' href='https://www.vinopedia.com/link.jsp?id="+p.s.wineset.Wine[i].Id+"' title='"+Webroutines.formatSizecompact(p.s.wineset.Wine[i].Size)+" "+Spider.escape(p.s.wineset.Wine[i].Name).replaceAll("\"","&quot;").replaceAll("'","&quot;")+" "+p.s.wineset.Wine[i].Vintage+"' TARGET='_blank' "+astyle+">"+p.s.wineset.Wine[i].Vintage+" "+Spider.escape(p.s.wineset.Wine[i].Name.replace("\"","&quot;"))+" "+Webroutines.formatSizecompact(p.s.wineset.Wine[i].Size)+"</a></div>";
		html=html+"<div class='wise_vinopedia_wineinfo'>For sale at "+p.s.wineset.Wine[i].Shopname+" in "+Webroutines.getCountryFromCode(p.s.wineset.Wine[i].Country)+" for <strong>"+"&euro;"+format.format(p.s.wineset.Wine[i].PriceEuroEx)+"</strong></div>";
	}
	html=html+"<div style='height:14px;width:100%;overflow:hidden;white-space:nowrap;'><a style='style='height:14px;overflow:hidden;white-space:nowrap;' href='https://www.vinopedia.com/wine/"+Webroutines.URLEncode(p.searchdata.getName())+(p.searchdata.getVintage().equals("")?"":"&amp;vintage="+p.searchdata.getVintage())+"'  target='_blank' "+astyle+">Click here</a> to find all deals for "+(p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName())+"</div>";
	html=html+"<a style='border-style: none;' class='wise_vinopedia_logo' href='https://www.vinopedia.com/wine/"+Webroutines.URLEncode(p.searchdata.getName())+(p.searchdata.getVintage().equals("")?"":"&amp;vintage="+p.searchdata.getVintage())+"'><img class='wise_vinopedia_logo' style='padding-top:10px;border-style: none;' src='https://www.vinopedia.com/images/smallheader.jpg' alt='Find your favorite wine for the best price'/></a>";
	html=html+"</table>";
if (false){	%>
thisApp.css = {
	".test":
	{
	"font-size":"20px",
	"color":"red"
	},
	"results":
	{"border":"0",
	"top-margin":"0",
	"left-margin":"0",
	"right-margin":"0",
	"font-family":"Georgia",
	"font-size":"11",
	"width":"100%",
	"table-layout":" fixed",
	"border-collapse":" collapse",
	"border-width":" 0px",
	"color":"green",
	"text-align":"left",
	"font-weight":"normal",
	"a.visited":{"color":"green"},
	
	},
    "tdresult":
    {"color":"black",
    "font-size":"6",
	"a.color":"black"
    },
    "trresult":
    {"height":"14px",
    "text-align":"left"
    },
    "trresultodd":
    {"height":"14px",
    "text-align":"left"
    }
	};

	<% }

	out.write("var resp={\"title\":\"vinopedia - Find the best place to buy "+(p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName())+"\",\"url\":\"https://www.vinopedia.com/wine/"+Webroutines.URLEncode((p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):p.searchdata.getName()))+"\",\"content\":\""+html+"\"};");
	out.write(callback+"(resp);");
	}
	}
	p.logger.logaction();

%>

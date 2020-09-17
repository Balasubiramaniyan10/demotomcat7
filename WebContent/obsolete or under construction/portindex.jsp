<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%> 
<%
 	long startload=System.currentTimeMillis(); 
       	Webactionlogger logger=new Webactionlogger("Pageload", request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), "","",0,(float)0.0,(float)0.0, "", false, "", "", "", "",(double)0.0,0);

       	int numberofresults=0;
 %>
	<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wijnzoeker"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.Shop"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Ad"
	import = "com.freewinesearcher.common.Knownwines"
	import = "com.freewinesearcher.online.Webactionlogger"
	import = "com.freewinesearcher.online.Translator"
	import = "com.freewinesearcher.online.WineLibraryTV"
	
	%><%
		request.setCharacterEncoding("ISO-8859-1");
		session = request.getSession(true); 
		boolean sponsoredresults=false;
		String thispage="portindex.jsp";
		String referrer="";
		WineLibraryTV wltv;
		boolean fuzzy=false;
		String youmayalsolike="";
		if (Webroutines.getConfigKey("showsponsoredlinks").equals("true")) sponsoredresults=true;
	%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
	<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<%
	int numberofrows=Webroutines.numberofnormalrows;
	Wineset wineset=null;
	boolean LBV=("on".equals(Webroutines.filterUserInput(request.getParameter("LBV")))?true:false);
	boolean vintageport=("on".equals(Webroutines.filterUserInput(request.getParameter("vintageport")))?true:false);
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	String map=Webroutines.filterUserInput(request.getParameter("map"));
	if (map==null) map="";
	if (offset==null||offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="order" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
		<jsp:setProperty name="searchdata" property="country" value=""/> 
		<jsp:setProperty name="searchdata" property="priceminstring" value=""/> 
		<jsp:setProperty name="searchdata" property="pricemaxstring" value=""/> 
		<jsp:setProperty name="searchdata" property="*"/> 
		<jsp:setProperty name="searchdata" property="offset" value="0"/>
		
<%
			}else {
		%>
		<jsp:setProperty name="searchdata" property="*"/> 
		
		<%
 					}
 				 				 				 				 				 				 					// Analyse Google query and reuse query on Google instead of Google's link
 				 				 				 				 				 				 					if (request.getHeader("Referer")!=null&&(request.getHeader("Referer").contains("google")||request.getHeader("Referer").contains("hammink"))){
 				 				 				 				 				 				 						referrer=request.getHeader("Referer");
 				 				 				 				 				 				 					}
 				 				 				 				 				 				 					
 				 				 				 				 				 				 					if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")){
 				%>
		<jsp:setProperty name="searchdata" property="vintage" value="<%=searchdata.getVintage().concat("ss ")+Webroutines.getVintageFromName(searchdata.getName())%>"/> 
		<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/> 
	<%
 		}
 	 	 	 	 	 	 		if ("true".equals(request.getParameter("fuzzy"))){
 	 	 	 	 	 	 			fuzzy=true;
 	 	 	 	 	 	 		}
 	 	 	 	 	 	 		wineset=new Wineset(searchdata, numberofrows, false, false, false, vintageport, LBV);
 	 	 	 	 	 	 		
 	 	 	 	 	 	 		
 	 	 	 	 	 	 		//	 Retrieve currency from cookie if not filled already
 	 	 	 	 	 	 		Cookie[] cookies = request.getCookies();
 	 	 	 	 	 	 	    if (searchdata.getCurrency().equals("")){
 	 	 	 	 	 	 			searchdata.setCurrency(Webroutines.getCookieValue(cookies,"currency","EUR"));
 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	    Cookie currencyCookie =
 	 	 	 	 	 	 	        new Cookie("currency", searchdata.getCurrency());
 	 	 	 	 	 	 	    currencyCookie.setMaxAge(60*60*24*365);
 	 	 	 	 	 	 	    response.addCookie(currencyCookie);
 	 	 	 	 	 	 		//  Retrieve language from cookie if not filled already
 	 	 	 	 	 	 		if (searchdata.getLanguage()==null||searchdata.getLanguage().toString().equals("")){
 	 	 	 	 	 	 		searchdata.setLanguage(Webroutines.getCookieValue(cookies,"language",""));
 	 	 	 	 	 	 		}
 	 	 	 	 	 	 	    if (searchdata.getLanguage()!=null&&!searchdata.getLanguage().toString().equals("")){
 	 	 	 	 	 	 			Cookie languageCookie =
 	 	 	 	 	 	 	    	    new Cookie("language", searchdata.getLanguage().toString());
 	 	 	 	 	 	 	    	languageCookie.setMaxAge(60*60*24*365);
 	 	 	 	 	 	 	    	response.addCookie(languageCookie);
 	 	 	 	 	 	 	    }
 	 	 	 	 	 	 	    
 	 	 	 	 	 	 	    ArrayList<String> countries = Webroutines.getCountries();
 	 	 	 	 	 	 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));

 	 	 	 	 	 	 		Translator.languages language=Translator.getLanguage(searchdata.getLanguage());
 	 	 	 	 	 	 	    if (language==null){
 	 	 	 	 	 	 	    	language=Translator.getDefaultLanguageForCountry("UK");
 	 	 	 	 	 	 	    }
 	 	 	 	 	 	 	    Translator t=new Translator();
 	 	 	 	 	 	 	    t.setLanguage(language);
 	 	 	 	 	 	 		session.setAttribute("language",language);
 	%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString().toLowerCase())?"EN":searchdata.getLanguage().toString().toLowerCase())%>">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
<title><%
	if (!searchdata.getName().equals("")){out.print(Spider.escape(searchdata.getName())+" "+t.get("pricesbyfws"));} else {out.print("vinopedia");}
%></title>
<%
	session.setAttribute("winename",searchdata.getName());
%>
<%@ include file="/header.jsp" %>

<!-- google_ad_section_start(weight=ignore) -->	
<script type="text/javascript">
<!--
function feed(form) {
	actionurl="/showrssurl.jsp?name="+form.name.value;
	actionurl=actionurl+"&vintage="+form.vintage.value;
	document.Searchform.action=actionurl; 
	form.submit();
	
  	return 0;
}
-->
</script>
<script type="text/javascript" src="/js/Dojo/dojo/dojo.js" djConfig="parseOnLoad: true">
</script>
<script type="text/javascript"> 
        dojo.require("dojo.dnd.Source"); // capital-S Source in 1.0
        dojo.require("dojo.parser");	
        dojo.require("dojo.fx");	
</script>
<script language="JavaScript" type="text/javascript" src="/js/tn.js?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime()).toString()%>"></script>
<script language="JavaScript" type="text/javascript" src="/js/advice.jsp?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime()).toString()%>"></script>
<script language="JavaScript" type="text/javascript" src="/js/suggest.js?unique=<%=new java.sql.Timestamp(new java.util.Date().getTime()).toString()%>"></script>
<!-- google_ad_section_end -->

	<%
		// Handle source IP address
			if (hostcountry.equals("NZ")||(Webroutines.ipBlocked(ipaddress)&&!request.getServletPath().contains("savecontact.jsp")&&!request.getServletPath().contains("abuse.jsp"))){
		if (hostcountry.equals("NZ")){
		out.print ("An error occurred at line: 17 in the jsp file: /index.jsp");
		logger=new Webactionlogger("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), "","", 0,(float)0.0, (float)0.0, "", true, "", "", "", "",0.0,0);
		}
			} else {
	%>

<table class="main" onclick="javascript:emptySuggest();">
	<tr><td class="left">
	<%
		if (request.getHeader("User-Agent")!=null&&(request.getHeader("User-Agent").contains("dows CE")||request.getHeader("User-Agent").contains("PIE")||request.getHeader("User-Agent").contains("WM5")||request.getHeader("User-Agent").contains("PPC")||request.getHeader("User-Agent").contains("Nokia")||request.getHeader("User-Agent").contains("Symbian"))) out.write("If you are on a mobile device, click <a href='/mobile.jsp?name="+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage()+"'>here</a>");
			if (true){
	%>
		<%=t.get("language")%>: <a href='/lang-EN/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;")+" "+searchdata.getVintage())%>'><img src="/images/flags/english.gif" alt="English" /></a>&nbsp;<a href='/lang-FR/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;"))+" "+searchdata.getVintage()%>'><img src="/images/flags/french.gif" alt="Français" /></a>&nbsp;<a href='/lang-NL/wine/<%=(Webroutines.URLEncode(searchdata.getName()).replace("'","&apos;")+" "+searchdata.getVintage())%>'><img src="/images/flags/dutch.gif" alt="Nederlands" /></a>
		
		<%
					} else {
															out.print("<br />");
															}
				%>
	
	
		<!-- google_ad_section_start(weight=ignore) -->	
		<form action="<%=thispage%>" method="post" id="Searchform" name="Searchform">
		<%=t.get("displaycurrency")%><br />
		<input type="radio" name="currency" value="EUR" <%if (searchdata.getCurrency().equals("EUR")) out.print(" checked=\"checked\"");%> />&euro;&nbsp;<input type="radio" name="currency" value="GBP" <%if (searchdata.getCurrency().equals("GBP")) out.print(" checked=\"checked\"");%> />&#163;&nbsp;<input type="radio" name="currency" value="CHF" <%if (searchdata.getCurrency().equals("CHF")) out.print(" checked=\"checked\"");%> /><font size='1'>CHF</font>&nbsp;<input type="radio" name="currency" value="USD" <%if (searchdata.getCurrency().equals("USD")) out.print(" checked=\"checked\"");%> />$
		<table class="search">
		<tr><td>
		<table class="searchform">
			<tr><td><h4><%=t.get("searchawine")%></h4>
			<%
				if (searchdata.getName().length()<3&&request.getParameter("dosearch")!=null) {
			%><font color='red'><%=t.get("name")%></font><%
				} else {
			%><%=t.get("name")%><%
				}
			%> <a href='/index.jsp?help=true' onmouseover='javascript:document.getElementById("helptext").style.visibility="visible";' onmouseout='javascript:document.getElementById("helptext").style.visibility="hidden";'><%=t.get("help")%></a></td></tr><tr><td><input class="leftfull" type="text" name="name" autocomplete="off" id="name" value="<%=Spider.escape(searchdata.getName())%>" size="25" onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" /><div id="search_suggest" class="search_suggest_noborder" ></div></td></tr>
			<tr><td><%=t.get("vintage")%></td></tr><tr><td><input class="leftfull" type="text" name="vintage" autocomplete="off" value="<%=searchdata.getVintage()%>"  /></td></tr>
			<tr><td><%=t.get("countryofretailer")%></td></tr><tr><td><select name="country" >
					<option value="All"<%if (searchdata.getCountry().equals("All")) out.print(" selected=\"selected\"");%>><%=t.get("all")%></option>
					<%
						for (int i=0;i<countries.size();i=i+2){
					%><option value="<%=countries.get(i)%>"<%if (searchdata.getCountry().equals(countries.get(i))) out.print(" selected=\"selected\"");%>><%=countries.get(i+1)%></option><%
						}
					%>
					</select></td></tr>
			<tr><td>Vintage <input type="checkbox" name="vintageport" <%=("1".equals(vintageport)?"checked":"")%>></td></tr>			
			<tr><td>LBV <input type="checkbox" name="LBV" <%=("1".equals(LBV)?"checked":"")%>></td></tr>			
			</table>
		<!--searchform-->
		<input type="hidden" name="dosearch" value="true" />		
		<input type="hidden" name="order" value="" />		
		<input type="hidden" name="rareoldstring" value="false" />
		<input type="hidden" id="map" name="map" value="" />		
		<input type="submit" value="<%=t.get("search")%>" />
	  	<input type="image" src="/images/xml.bmp" onclick="javascript:feed(this.form);" />
	</td></tr>
	</table>
  	</form>

<%
	if (wineset!=null&&wineset.knownwineid>0){
	if (new File(Wijnzoeker.basedir+"images\\wines\\"+wineset.knownwineid+".gif").exists()){
		out.print ("<img src='/images/wines/"+wineset.knownwineid+".gif' alt='"+searchdata.getName().replace("'","&apos;")+"' />");
	} else {
		if (new File(Wijnzoeker.basedir+"images\\wines\\"+wineset.knownwineid+".jpg").exists()){
	out.print ("<img src='/images/wines/"+wineset.knownwineid+".jpg' alt='"+searchdata.getName().replace("'","&apos;")+"' />");
		}
	}
		
	}
	
	String region="";
	int knownwineid=0;
	if (wineset!=null){
		region=wineset.region;
		knownwineid=wineset.knownwineid;
		if (region==null||region.equals("")){
	if (knownwineid>0){
		region=Dbutil.readValueFromDB("Select * from knownwines where id="+knownwineid+";","appellation");
	}
		}
	}
	Ad rightad= new Ad(120,600,hostcountry,region,knownwineid,"");
	Ad bottomleftad= new Ad(187,300,hostcountry,region,knownwineid,rightad.partner+"");
	Ad betweenresults= new Ad(646,60,hostcountry,region,knownwineid,rightad.partner+","+bottomleftad.partner);
	session.setAttribute("hostcountry",hostcountry);
	session.setAttribute("region",region);
	session.setAttribute("knownwineid",knownwineid);
	
	out.write(bottomleftad.html);
%>
	<!--search-->
	
	</td><td class="centre"><%=Webroutines.getConfigKey("systemmessage")%><div id='helptext' class='helptext' ></div>
	
	<%
			if (searchdata.getName().length()<3) { 
				logger=new Webactionlogger("Pageloadindex2",request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0,0);
		%>
	
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start -->
			<h4><%=t.get("welcome")%></h4><br />
			<%=t.get("welcometext")%>
			<br />&nbsp;<br />
			<h4><%=t.get("todaystips")%></h4>
			<%=t.get("tiptext")%>
			<%
				out.print(Webroutines.getTipsHTML2("",(float)0.75,10,"index2.jsp",t.language,searchdata));
							out.print((Webroutines.getTipsHTML2("",(float)0.75,10,"index2.jsp",t.language,searchdata)).equals("")?("<br />"+t.get("notips")):("<div class='pricenote'>"+t.get("pricenote")+"</div>"));
			%>
			<!-- google_ad_section_end -->
			<!-- google_ad_section_start(weight=ignore) -->	
			
			
	<!--hints-->
	<!-- google_ad_section_end -->
	
	<%
			out.print("<script type=\"text/javascript\">addthis_url = location.href; addthis_title  = document.title;  addthis_pub    = 'vinopedia';</script><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\" ></script><br/>");
				if (request.getParameter("dosearch")!=null) {
			out.print("<br /><br /><font color='red'>"+t.get("3characters")+"</font>");
			}
			}
			
			if (searchdata.getName().length()>2) {
				String tip="";
				if ("true".equals(request.getParameter("tip"))) tip= "Tip ";
				String suggestion="";
				if ("true".equals(request.getParameter("suggestion"))) suggestion= "Suggestion ";
				int records=0;
				if (wineset!=null) records=wineset.records;
				logger=new Webactionlogger(tip+suggestion+"Search",request.getServletPath(),ipaddress,  request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0,records);
				if (request.getParameter("dosearch")!=null) logger.searchhistory=searchhistory;
				if (wineset!=null&&wineset.othervintage){
			out.print(t.get("noresultsfound"));
			out.print(" "+t.get("forvintage")+" "+searchdata.getVintage()+". "+t.get("othervintages"));
				}
				
				if (wineset==null||wineset.Wine==null||wineset.Wine.length==0&&!wineset.othervintage){
			out.print(t.get("noresultsfound"));
			out.print(".");
			ArrayList<String> alternatives=com.freewinesearcher.common.Knownwines.getAlternatives(searchdata.getName());
			if (alternatives.size()>0){	
				out.write (" "+t.get("alternatives")+"<br/>");
				for (int i=0;i<alternatives.size();i=i+3){
					out.write("<a href='/wine/"+Webroutines.URLEncode(alternatives.get(i+1))+"'>"+alternatives.get(i)+ "</a>&nbsp;("+alternatives.get(i+2)+" "+(alternatives.get(i+2).equals("1")?t.get("wine"):t.get("wines"))+")<br/>");
				}
			}
			
				}
				if (wineset!=null&&wineset.Wine!=null&&wineset.Wine.length!=0){
		%><h4><%=t.get("searchresultsfor")%> <%
 	out.print(Spider.escape(searchdata.getName().replaceAll("\\d\\d\\d\\d\\d\\d ",""))+" "+searchdata.getVintage());
 %> (<%
 	out.print(wineset.records+" "+t.get("winesfound")+"). <br />");
    	out.print("</h4>"+t.get("permalink")+": <a href='/wine/"+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'>"+"https://www.vinopedia.com/wine/"+Spider.escape(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"</a><br />");
 %>
			<%
				if (wineset.bestknownwineid>0){ //!wineset.searchtype.equals("text")){
								int singlevintage=0;
								try {
									singlevintage=Integer.parseInt(searchdata.getVintage().trim());
								} catch (Exception e){}
								String ratingshtml=Webroutines.getRatingsHTML(wineset.bestknownwineid,19,"/index2.jsp",singlevintage);
								out.print(ratingshtml);
								if (!"".equals(ratingshtml)&&wineset.knownwineid==0){
									out.print("Note: the ratings given above are for "+Knownwines.getKnownWineName(wineset.bestknownwineid)+" only. The results list may contain other wines as well.");
								}
								out.print("<br/>");
								
							}
								if (false&&wineset.records>numberofrows){ //Disabled
								// Show the links to the rest of the results
								for (int i=0;i<wineset.records;i=i+numberofrows){
								 out.print("<a href='"+response.encodeURL("/index.jsp?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset="+i)+"' style='{color: blue;}'>");
								 if (Integer.toString(i).equals(offset)) out.print("<b>");
								 out.print(t.get("page")+"&nbsp;"+(i/numberofrows+1)+" ");
								 if (Integer.toString(i).equals(offset)) out.print("</b>");
								 out.print("</a>");
								
								}
								}
								out.print("<br/>");
								

							if (!map.equals("true")){
							if (searchdata.getName().length()>3){
								if (wineset.knownwinelist.size()>1){
			%>
	<div class='show' onmouseover='javascript:show("refine");' onmouseout='javascript:hide("refine");'><img src='/images/refine.jpg' alt='Refine results'/></div>	<%
		} 
			if (wineset.bestknownwineid>0){
		youmayalsolike=Webroutines.youMayAlsoLikeHTML(wineset.bestknownwineid,searchdata);
			 	if (!youmayalsolike.equals("")){
	%>
	<div class='show' onmouseover='javascript:show("suggestions");' onmouseout='javascript:hide("suggestions");'><img src='images/ifyoulikethis.jpg' alt='Recommended wines from this area'/></div>
	<%
		}	}
			if (wineset.bestknownwineid>0){
	%>
	<div class='show' onmouseover='javascript:showTNtop(<%=wineset.bestknownwineid%>,"<%=" "+searchdata.getVintage()%>");' onmouseout='javascript:hidetop("toptastingnote")'><img src='/images/TN.jpg' alt='Tasting notes'/></div>
	<%
		}
			if (wineset.bestknownwineid>0){
		wltv=new WineLibraryTV(wineset.bestknownwineid,searchdata.getVintage());
		if (!"".equals(wltv.url)){
	%>
	<div class='show' ><a href='<%=wltv.url%>' target=_blank'><img src='/images/WLTV.jpg' alt='<%=wltv.alt%>'/></a></div>
	<%
		}	}
			if (wineset!=null&&wineset.Wine.length>0&&Webroutines.getConfigKey("map").equals("true")) {
			if (map.equals("true")){
		out.print("<div id='show' class='show'><a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\">Show results as list</a></div>");
			} else {
		out.print("<div id='show' class='show'><a href=\"javascript:document.getElementById('map').value='true';document.getElementById('Searchform').submit();\"><img src='/images/showonmap.jpg' alt='Show on map'/></a></div>");
			}		
		}
	%>
	<div class='show'> <%
 	out.print("<script type=\"text/javascript\">addthis_url = 'https://www.vinopedia.com/wine/"+Webroutines.URLEncode(searchdata.getName())+" "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'; addthis_title  = document.title;  addthis_pub    = 'vinopedia';</script><script type=\"text/javascript\" src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\" ></script>");
 %></div>
	
	<div style='clear:left;'> </div>
	
	<div id='toptastingnote' class='tastingnote' onmouseover='javascript:showTNtop(<%=wineset.bestknownwineid%>,"<%=" "+searchdata.getVintage()%>");' onmouseout='javascript:hidetop("toptastingnote");'><div id='tncontent' class='tncontent' onmouseover='javascript:showTNtop(<%=wineset.bestknownwineid%>,"<%=searchdata.getVintage()%>");' onmouseout='javascript:hidetop("toptastingnote");'></div></div>
	
			<%
					if (wineset.knownwinelist.size()>1){
				%>
				<div id='refine' class='refine' onmouseover='show("refine");' onmouseout='hide("refine");'>
				<%=Webroutines.getRefineHTML(t,searchdata,wineset,thispage)%>
				</div>
				
				<%
									}
																												}
																												if (wineset.bestknownwineid>0){
																											 		if (!"".equals(youmayalsolike)){
																											 			out.write(youmayalsolike);		
																											}
																												}
																												out.write(betweenresults.html);
								%>
	<table class="results"><tr><th class="flag"></th><th class="shop"><%=t.get("store")%></th><th class="name"><%=t.get("wine")%></th><th class="vintage"><%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"vintage")).replace("'","&apos;")+"'>"+t.get("vintage")+"</a>");
	%></th><th class="size" align="right">&nbsp;&nbsp;&nbsp;<%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"size")).replace("'","&apos;")+"'>"+t.get("size")+"</a>&nbsp;&nbsp;&nbsp;");
	%></th><th class="price" align="right"><%
		out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset=0&amp;order="+Webroutines.getOrder(searchdata.getOrder(),"priceeuroin")).replace("'","&apos;")+"'>"+t.get("price")+"</a>");
	%></th></tr>    
			<%
    				NumberFormat format  = new DecimalFormat("#,##0.00");	
    			    			    			    				
    			    			    			    				if (sponsoredresults){
    			    			    			    				
    			    			    			    				if (wineset.SponsoredWine!=null&&wineset.SponsoredWine.length>0) {	
    			    			    			    					out.print("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
    			    			    			    					for (int i=0;i<wineset.SponsoredWine.length;i++){
    			    			    			    							out.print("<tr");
    			    			    			    							if (i%2==1)out.print(" class=\"sponsoredodd\"");
    			    			    			    							if (i%2==0)out.print(" class=\"sponsoredeven\"");
    			    			    			    							out.print (">");
    			    			    			    							out.print("<td class='flag'><a href='"+thispage+"?name="+(Webroutines.URLEncode(searchdata.getName())+"&amp;country="+wineset.Wine[i].Country.toUpperCase()+"&amp;vintage="+searchdata.getVintage()).replace("'","&apos;")+"' target='_blank'><img src='/images/flags/"+wineset.SponsoredWine[i].Country.toLowerCase()+".gif' alt='"+wineset.SponsoredWine[i].Country.toLowerCase()+"' /></a></td>");
    			    			    			    							out.print("<td><a href="+response.encodeURL("link.jsp?shopid="+wineset.SponsoredWine[i].ShopId)+" target='_blank'>"+wineset.SponsoredWine[i].Shopname+"</a></td>");
    			    			    			    							out.print("<td><a href="+response.encodeURL("link.jsp?wineid="+wineset.SponsoredWine[i].Id)+" target='_blank'>"+wineset.SponsoredWine[i].Name+"</a></td>");
    			    			    			    							out.print("<td>" + wineset.SponsoredWine[i].Vintage+"</td>");
    			    			    			    							out.print("<td align='right'>" + Webroutines.formatSize(wineset.SponsoredWine[i].Size)+"</td>");
    			    			    			    							out.print("<td align='right'>"+Webroutines.formatPrice(wineset.SponsoredWine[i].PriceEuroIn,wineset.SponsoredWine[i].PriceEuroEx,searchdata.getCurrency(),searchdata.getVat())+"</td>");
    			    			    			    							out.print("</tr>");
    			    			    			    					}
    			    			    			    					out.print("<tr><td colspan='4'><i>All results:</i></td></tr>");
    			    			    			    				
    			    			    			    				} else {	
    			    			    			    					out.print("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
    			    			    			    					out.print("<tr class=\"sponsoredeven\">");
    			    			    			    					out.print("<td colspan='2'><a href='"+response.encodeURL("/sponsoring.jsp")+"'></a></td>");
    			    			    			    					out.print("<td colspan='2'><a href='"+response.encodeURL("/sponsoring.jsp")+"'>Your wine could be listed here! Click for more information.</a></td>");
    			    			    			    					out.print("<td align='right'></td>");
    			    			    			    					out.print("<td align='right'>&euro; 0.10</td>");
    			    			    			    					out.print("</tr>");
    			    			    			    					out.print("<tr><td colspan='4'><i>All results:</i></td></tr>");
    			    			    			    				}
    			    			    			    				}	
    			    			    			    				
    			    			    			    				// Give the complete result list
    			    			    			    				for (int i=0;i<wineset.Wine.length;i++){
    			    			    			    						out.print("<tr");
    			    			    			    						if (wineset.Wine[i].CPC>0&&sponsoredresults){
    			    			    			    							out.print(" class=\"sponsoredeven\"");
    			    			    			    						} else {
    			    			    			    							if (i%2==1){out.print(" class=\"odd\"");}
    			    			    			    						}
    			    			    			    						out.print (">");
    			    			    			    						out.print("<td class='flag'><a href='"+thispage+"?name="+(Webroutines.URLEncode(searchdata.getName())+"&amp;country="+wineset.Wine[i].Country.toUpperCase()+"&amp;vintage="+searchdata.getVintage()).replace("'","&apos;")+"' target='_blank'><img src='/images/flags/"+wineset.Wine[i].Country.toLowerCase()+".gif' alt='"+wineset.Wine[i].Country.toLowerCase()+"' /></a></td>");
    			    			    			    						out.print("<td><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"&amp;shopid="+wineset.Wine[i].ShopId+"' target='_blank' title='"+wineset.Wine[i].Shopname.replace("&","&amp;").replace("'","&apos;")+"'>"+wineset.Wine[i].Shopname.replace("&","&amp;")+"</a></td>");
    			    			    			    						out.print("<td><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"' title='"+Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)).replace("'","&apos;")+"' target='_blank'>"+Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name))+"</a></td>");
    			    			    			    						out.print("<td>" + (wineset.Wine[i].Vintage.equals("0")?"":wineset.Wine[i].Vintage)+"</td>");
    			    			    			    						out.print("<td align='right'>" + Webroutines.formatSize(wineset.Wine[i].Size)+"</td>");
    			    			    			    						out.print("<td class='price' align='right'>" + Webroutines.formatPrice(wineset.Wine[i].PriceEuroIn,wineset.Wine[i].PriceEuroEx,searchdata.getCurrency(),searchdata.getVat())+"</td>");
    			    			    			    						out.print("</tr>");
    			    			    			    				}
    			%>
	</table>
	<!--results-->	
	<%
			} else {
				// Show on map 
				out.print("<div id='show' class='show'><a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\"><img src='/images/showaslist.jpg' alt='Show as list'/></a></div><div style='clear:left;'> </div>");
		%>
		<%@ include file="/snippets/googlemap.jsp" %><%
			}
		%>
	<div class='pricenote'><%=t.get("pricenote")%><br /></div>
	<%
		if (wineset.records>numberofrows){

			// Show the links to the rest of the results again at the bottom

			for (int i=0;i<wineset.records;i=i+numberofrows){
			 out.print("<a href='"+("/"+thispage+"?name="+Webroutines.URLEncode(searchdata.getName())+"&amp;offset="+i)+"' style='{color: blue;}'>");
			 if (Integer.toString(i).equals(offset)) out.print("<b>");
			 out.print(t.get("page")+"&nbsp;"+(i/numberofrows+1)+" ");
			 if (Integer.toString(i).equals(offset)) out.print("</b>");
			 out.print("</a>");
			}
		}			
			}		
		}
	%>
</td><td class="right">
<%out.write(rightad.html); %>
	</td></tr>
</table>	
<script type="text/javascript">
<!--
document.getElementById("helptext").innerHTML="<%@ include file="/helptext.txt" %>";
-->
</script>	
<!--main-->		
<jsp:include page="/footer.jsp" />	
<%} //NZ filter %>

</div>

</body> 
</html>
<% long endload=System.currentTimeMillis();

	logger.loadtime=((endload-startload));
	logger.logaction();
%>
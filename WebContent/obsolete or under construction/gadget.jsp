<html>
<head>
<title>
vinopedia
</title>
<meta name="MobileOptimized" content="240">
<jsp:include page="/headergadget.jsp" />
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
<script language="JavaScript" type="text/javascript" src="/js/suggest.js"></script>
<!-- google_ad_section_end -->
<%@ page   
	import = "java.io.*"
	import = "java.net.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "java.util.ArrayList"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.online.Searchdata"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.freewinesearcher.online.Translator"

	
	
%>
<%
	session = request.getSession(true);
%>
	<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<%
	String offset=Webroutines.filterUserInput(request.getParameter("offset"));
	if (offset==null||offset.equals("")) { // First empty the fields in case one of the field was made empty: then it would not refresh
		offset="0";
%>
		<jsp:setProperty name="searchdata" property="name" value=""/> 
		<jsp:setProperty name="searchdata" property="order" value=""/> 
		<jsp:setProperty name="searchdata" property="vintage" value=""/> 
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

 					if (!Webroutines.getVintageFromName(searchdata.getName()).equals("")){
 				%>
		<jsp:setProperty name="searchdata" property="vintage" value="<%=searchdata.getVintage().concat("ss ")+Webroutines.getVintageFromName(searchdata.getName())%>"/> 
		<jsp:setProperty name="searchdata" property="name" value="<%=Webroutines.filterVintageFromName(searchdata.getName())%>"/> 
	<%
 		}
 		String ipaddress="";
 	    if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
 	    	ipaddress = request.getRemoteAddr();
 	    } else {
 	        ipaddress = request.getHeader("HTTP_X_FORWARDED_FOR");
 	    }

 	    if (Webroutines.getCountryCodeFromIp(ipaddress).equals("NZ")){
 	    	out.print ("<br/><br/>This service is temporarily unavailable. Please try again later.");
 	    	Webroutines.logWebAction("NZ: Access denied",request.getServletPath(),ipaddress, request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
 			
 	    } else {
 	    
 		ArrayList<String> countries = Webroutines.getCountries();
 		if (searchdata.getVat()==null||searchdata.getVat().equals("")) searchdata.setVat(Webroutines.getCountryCodeFromIp(request.getRemoteAddr()));
 	%>
<%
	Wineset wineset=null;
	if (searchdata.getName().length()>2) {
		//Wineset sponsoredwineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),100);
		wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),15);
		
	}
%>
<TABLE  class="main" onclick="javascript:emptySuggest();">
	<TR>
	
		
	
	<%
						if (searchdata.getName().length()<3) { 
							Webroutines.logWebAction("Pageload gadget",request.getServletPath(),request.getRemoteAddr(), request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
					%>
	<TD class="centre">
	<%
		if (request.getParameter("dosearch")!=null) {
		out.print("<BR/><font color='red'>Please enter at least 3 characters for the wine name.</font>");
			}
		}
		
		if (searchdata.getName().length()>2) {
			out.print("<TD class=\"centre\">");
			Webroutines.logWebAction("Google Gadget",request.getServletPath(),ipaddress,  request.getHeader("referer"), searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getCountry(), searchdata.getRareold(), "", "", "", "",0.0);
			if (wineset==null||wineset.Wine.length==0){
		out.print("No results found. ");
		ArrayList<String> alternatives=com.freewinesearcher.common.Knownwines.getAlternatives(searchdata.getName());
		if (alternatives.size()>0){	
	out.write ("<br/><br/>Were you looking for:<br/>");
	for (int i=0;i<alternatives.size();i=i+3){
		out.write("<a href='/gadget.jsp?name="+Webroutines.URLEncode(alternatives.get(i+1))+"'>"+alternatives.get(i)+ "</a>&nbsp;("+alternatives.get(i+2)+" "+(alternatives.get(i+2).equals("1")?t.get("wine"):t.get("wines"))+")<br/>");
	}
	//if (wineset.searchtype.equals("text")) out.print(Webroutines.didYouMean(searchdata.getName(),"/gadget.jsp"));
			} else {
		if (wineset.searchtype.equals("text")){
	%>Search results: <%
		out.print(wineset.records+" wines found. ");
	%><BR/>Click to sort by <%
		out.print("<a href='"+response.encodeURL("/gadget.jsp?name="+searchdata.getName()+"&offset=0&order="+Webroutines.getOrder(searchdata.getOrder(),"vintage"))+"'>Vintage</a>");
	%>, <%
		out.print("<a href='"+response.encodeURL("/gadget.jsp?name="+searchdata.getName()+"&offset=0&order="+Webroutines.getOrder(searchdata.getOrder(),"size"))+"'>Size</a>, ");
	%><%
		out.print("<a href='"+response.encodeURL("/gadget.jsp?name="+searchdata.getName()+"&offset=0&order="+Webroutines.getOrder(searchdata.getOrder(),"priceeuroex"))+"'>Price</a>");
	%></TR>    
			<%
    				} else {
    			%>Search results for <%
    				out.print(searchdata.getName()+" "+searchdata.getVintage());
    			%> 
				(
				<%
    				out.print(wineset.records+" hits)");
    			%><%
    				int singlevintage=0;
    					try {
    						singlevintage=Integer.parseInt(searchdata.getVintage().trim());
    					} catch (Exception e){}
    					out.print(Webroutines.getRatingsHTML(wineset.knownwineid,8,"gadget.jsp",singlevintage)+"");
    				}
    			%>
	
			<%
					NumberFormat format  = new DecimalFormat("#,##0.00");	
					
					//if (sponsoredwineset.records>0) {	
					//	out.print("<tr><TD><i>Sponsored results:</i></TD></tr>");
					//	for (int i=0;i<sponsoredwineset.Wine.length;i++){
					//			out.print("<tr");
					//			if (i%2==1)out.print(" class=\"sponsoredodd\"");
					//			if (i%2==0)out.print(" class=\"sponsoredeven\"");
					//			out.print (">");
					//			out.print("<TD><a href="+response.encodeURL("link.jsp?shopid="+sponsoredwineset.Wine[i].ShopId)+" target='_blank'>"+sponsoredwineset.Wine[i].Shopname+"</a></TD>");
					//			out.print("<TD><a href="+response.encodeURL("link.jsp?wineid="+sponsoredwineset.Wine[i].Id)+" target='_blank'>"+sponsoredwineset.Wine[i].Name+"</a></TD>");
					//			out.print("<TD>" + sponsoredwineset.Wine[i].Vintage+"</TD>");
					//			out.print("<TD align='right'>" + Webroutines.formatSize(sponsoredwineset.Wine[i].Size)+"</TD>");
					//			out.print("<TD align='right'>&euro; " + format.format(sponsoredwineset.Wine[i].Price)+"</TD>");
					//			out.print("</tr>");
					//	}
					//	out.print("<tr><TD><i>All results:</i></TD></tr>");
						
					//} else {	
					//	out.print("<tr><TD><i>Sponsored results:</i></TD></tr>");
					//	out.print("<tr class=\"sponsoredeven\">");
					//	out.print("<TD><a href='"+response.encodeURL("/sponsoring.jsp")+"'>vinopedia</a></TD>");
					//	out.print("<TD><a href='"+response.encodeURL("/sponsoring.jsp")+"'>Your wine could be listed here! Click for more information.</a></TD>");
					//	out.print("<TD></TD>");
					//	out.print("<TD align='right'></TD>");
					//	out.print("<TD align='right'>&euro; 0.10</TD>");
					//	out.print("</tr>");
					//	out.print("<tr><TD><i>All results:</i></TD></tr>");
					//}
						
					
					
					for (int i=0;i<wineset.Wine.length;i++){
						if (false&&i==5){
				%>
					<TR><TD></TD><TD class="googlead" colspan=5>
					<script type="text/javascript"><!--
google_ad_client = "pub-5573504203886586";
google_ad_width = 466;
google_ad_height = 60;
google_ad_format = "468x60_as";
google_ad_type = "text_image";
//2006-12-06: BetweenResults
google_ad_channel = "3304013985";
google_color_border = "FFFFFF";
google_color_bg = "e0f3ff";
google_color_link = "0000FF";
google_color_text = "0000FF";
google_color_url = "0000FF";
--></script>
<script type="text/javascript"
src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script></TD></TR>
					<%
						}
								out.print("<Table class='results'><TR");
								if (wineset.Wine[i].CPC>0){
									out.print(" class=\"sponsoredeven\"");
								} else {
									if (i%2==1){out.print(" class=\"odd\"");}
								}
								out.print (" class='upper'>");
								out.print("<TD class='flag'><img src='/images/flags/"+wineset.Wine[i].Country.toLowerCase()+".gif'/></td><td><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"' target='_blank'>"+Webroutines.formatCapitals(wineset.Wine[i].Name)+"</a></TD>");
								out.print("<TD class='vintage'>" + wineset.Wine[i].Vintage+"</TD>");
								out.print("</TR></table>");
								out.print("<Table class='results'><TR");
								if (wineset.Wine[i].CPC>0){
									out.print(" class=\"sponsoredeven\"");
								} else {
									if (i%2==1){out.print(" class=\"odd\"");}
								}
								out.print (">");
								out.print("<TD class='shop'><a href='/link.jsp?wineid="+wineset.Wine[i].Id+"&shopid="+wineset.Wine[i].ShopId+"' target='_blank'>"+wineset.Wine[i].Shopname+"</a></TD>");
								out.print("<TD class='size' align='right'>" + Webroutines.formatSizecompact(wineset.Wine[i].Size)+"</TD>");
								out.print("<TD class='price' align='right'>&euro;&nbsp;" + format.format(wineset.Wine[i].PriceEuroEx)+"</TD>");
								out.print("</TR></table>");
						}
					%>
	</TABLE>
	<!--results-->	
	
	<%
				if (wineset.records>15){
					out.print("Page ");
					for (int i=0;i<wineset.records;i=i+15){
					 out.print("<a href='"+response.encodeURL("/gadget.jsp?name="+searchdata.getName().replaceAll("'","&apos;")+"&offset="+i)+"' style='{color: blue;}'>");
					 if (Integer.toString(i).equals(offset)) out.print("<b>");
					 out.print((i/15+1)+"  ");
					 if (Integer.toString(i).equals(offset)) out.print("</b>");
					 out.print("</a>");
					}
				}
				
					}		
				}
			%>

	
	
	
	<TABLE class="search">
		<TR><TD>
		<!-- google_ad_section_start(weight=ignore) -->	
		<FORM ACTION='/gadget.jsp' METHOD="POST" id="Searchform" name="Searchform">
		<TABLE class="searchform">
			<TR><TD><%
				if (searchdata.getName().length()<3) {
			%><font color='red'>Name (mandatory)</font><%
				} else {
			%>Name (mandatory)<%
				}
			%></TD><TD>Vintage</TD></TR>
			<TR><TD><INPUT TYPE="TEXT" NAME="name" id="name" value="<%=searchdata.getName()%>" size=20 onkeypress="return navigationkeys(event);" onkeyup="return searchSuggest(event);" onkeydown="keyDown(event);" autocomplete="off" /></TD>
			<TD><INPUT TYPE="TEXT" NAME="vintage" value="<%=searchdata.getVintage()%>" size=4></TD></TR>
			<TR><TD height=1><div id="search_suggest" class="search_suggest_noborder" ></div></TD><td></td></tr></TABLE>
			<table><TR><TD>Country of Seller</TD><td></td></TR>
			<TR><TD><select name="country">
					<option value="All"<%if (searchdata.getCountry().equals("All")) out.print(" selected");%>>All
					<%
						for (int i=0;i<countries.size();i=i+2){
					%>
					<option value="<%=countries.get(i)%>"<%if (searchdata.getCountry().equals(countries.get(i))) out.print(" selected");%>><%=countries.get(i+1)%>
					<%
						}
					%>
					</select></TD><td><INPUT TYPE="SUBMIT" VALUE="Search"></td></TR>
			
			</TABLE>
		<!--searchform-->
		<input type="hidden" name="dosearch" Value="true">		
		<input type="hidden" name="order" Value="">		
		<input type="hidden" name="rareoldstring" Value="false">
		<input type="hidden" name="createdstring" value="0">
	  	</FORM>
	</TD></TR>
	</TABLE>
	<%
		if (wineset!=null&&wineset.Wine.length>0){
	%>
		
	<font size=1>Note: Prices shown include local VAT but may exclude duty, shipping and handling costs. Always check the price with the seller.</font>
	<%
		} else {
			out.print("Today's wine tips:");
			out.print(Webroutines.getTipsHTML("",(float)0.75,20,"newindex.jsp",Translator.languages.EN));
			out.print((Webroutines.getTipsHTML("",(float)0.75,20,"newindex.jsp",Translator.languages.EN)).equals("")?("<br />"+new Translator(Translator.languages.EN).get("notips")):("<br />"+new Translator(Translator.languages.EN).get("pricenote")+"<br />"));
		}
	%>
	</TD></TR>
</TABLE>	
<!--main-->			
<%} //NZ filter %>

</div>

</body> 
</html>
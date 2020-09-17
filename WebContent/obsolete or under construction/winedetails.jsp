<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.ArrayList"%>
<%@ page 
import = "com.freewinesearcher.online.Webroutines"
import = "com.freewinesearcher.online.Searchdata"
import = "com.freewinesearcher.online.PageHandler"
import = "com.freewinesearcher.online.SearchHandler"
import = "com.freewinesearcher.online.Ad"
import = "com.freewinesearcher.common.Knownwines"
import = "com.freewinesearcher.common.Configuration"
import = "com.freewinesearcher.common.Knownwine"
%>
<% long start=System.currentTimeMillis();
	boolean debuglog=false;%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
%>
<jsp:useBean id="searchdata" class="com.freewinesearcher.online.Searchdata" scope="session"/>
<jsp:useBean id="searchhistory" class="com.freewinesearcher.online.Searchhistory" scope="session"/>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+"Start Pagehandler"); %>
<%
PageHandler p=PageHandler.getInstance(request,response);
p.searchpage="/winedetails.jsp";
p.processSearchdata();


%>

<%@page import="com.freewinesearcher.online.Producers"%>
<%@page import="com.freewinesearcher.online.Producer"%>
<%@page import="com.freewinesearcher.online.Usernote"%>
<%@page import="com.freewinesearcher.common.Context"%><html xmlns="http://www.w3.org/1999/xhtml" lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>" xml:lang="<%=("".equals(searchdata.getLanguage().toString()
							.toLowerCase()) ? "EN" : searchdata.getLanguage()
							.toString().toLowerCase())%>">
<head>
<title><%
	if (!searchdata.getName().equals("")) {
		out.print(Webroutines.escape("Vinopedia background information on "+(p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName().replaceAll("^\\d\\d\\d\\d\\d\\d ", "")) + " "));
	} else {
		out.print("vinopedia");
	}
%></title>
<%
	session.setAttribute("winename", (p.s.wineset.knownwineid>0?Knownwines.getKnownWineName(p.s.wineset.knownwineid):searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""));
%>
<% if (debuglog) System.out.println((System.currentTimeMillis()-start)+""); %>
<%@ include file="/header2.jsp" %>
<%@ include file="/snippets/jsincludes.jsp" %>
<meta name="verify-v1" content="DPurn9ZNRpI1pXuOlIigNqJ6JoMePo97QY0m2L3eBrA=" />
</head>
<%@ include file="/snippets/topbar.jsp" %>

<% if (!PageHandler.getInstance(request,response).block&&!PageHandler.getInstance(request,response).abuse){%>
<% 	Ad rightad = new Ad("winered",160, 600, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid,"");
	Ad betweenresults = new Ad("winered",728, 90, p.hostcountry, p.s.wineset.region, p.s.wineset.knownwineid, rightad.partner+"");
%>
<% Knownwine k=null;
			int knownwineid=0;
			try{knownwineid=p.s.wineset.bestknownwineid;}catch (Exception e){}
			if (knownwineid>0){
				k=new Knownwine(knownwineid);
			}
			if ("addnote".equals(request.getParameter("action"))||"editnote".equals(request.getParameter("action"))){
				Usernote newun=new Usernote(request,"producers");
				if (newun.valid) newun.save(request);
			}
%>

		<div class='container'><%@ include file="/snippets/logoandsearch.jsp" %><%=Webroutines.getConfigKey("systemmessage")%></div> 
		<% Webroutines.RatingInfo ri=Webroutines.getNewRatingsHTML(p.s.wineset.bestknownwineid, 1000, p.searchpage,p.s.singlevintage,p.searchdata,p.t);
		out.print(ri.html.equals("")?"":ri.html);%>
		<div class='main'>
			<div id='adrighthigh'><%out.write(rightad.html);%></div>
			<div id='mainleft'>	
<%if (knownwineid>0){
out.print(ri.html.equals("")?"<h1>"+k.description+"</h1>":"");%> 
<br/>
<div>
<div class='champcolumn'><div class='text'><h2>Wine information</h2>
<%=k.description%><br/><a href='/wine/<%= Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(k.id))%>'>See where you can buy this wine</a><br/>
</div><div class='champcolumnbottom'></div>
	
	</div>

<% Producers producers=new Producers(k.id);
	if (producers!=null&&producers.producer!=null&&producers.producer.size()>0){
		%><div class='champcolumn'><div class='text'><h2>Producer information</h2><%
		if (producers.producer.size()>1){
			out.write("We have more than one adddress that seem to match this producer.<br/>");
		}
		int i=0;
		for (Producer prod:producers.producer){
			i++;
		%>
		<div class='businesscard'>
		<div class='text'>
		<h3><%=prod.name %></h3>
		<%=prod.address.replace(", ",",<br/>").replaceAll("(?<=(^|,)[0-9 ]*),<br/>"," ") %><br/>
		Phone: <%=prod.telephone %><br/>
		<%=prod.website.equals("")?"":"Web site: <a href='"+(prod.website.toLowerCase().startsWith("http")?"":"http://")+Webroutines.URLEncode(prod.website)+"' target='_blank'>"+prod.website+"</a><br/>"%>
	</div></div>
	<input id='showmap<%=i%>' type='button' onclick='javascript:load<%=i%>();' value='Show map'/>
	</div>
	<div class='champcolumnbottom'></div>
	</div>
	</div>
	
	<div class='clear'/>
	<div id="spacer<%=i%>" style="width: 800px; height: 20px;visibility:hidden;"></div>
	<div id="map<%=i%>" style="width: 798px; height: 20px;visibility:hidden;"></div>
    <script type="text/javascript">
    function load<%=i%>() {
    	document.getElementById("spacer<%=i%>").style.height="20px";	
    	document.getElementById("map<%=i%>").style.height="600px";	
  		var map<%=i%> = new GMap2(document.getElementById("map<%=i%>"));
  		map<%=i%>.addControl(new GLargeMapControl());
  		var center = new GLatLng(<%=prod.lat%>,<%=prod.lon%>);
  		 map<%=i%>.setCenter(center, <%=(8)%>);
  		<%
  		out.write("var marker"+i+"=new GMarker(center, {draggable: true, bouncy: true});\n");
		//String info="";
		//info+=prod.name+"<br/>";
		//info+=prod.address+"<br/>";
		//info=info.replace("'","&apos;");
		//out.write("marker"+i+".bindInfoWindowHtml('<html><body>"+info+"</body></html>','clickable=true;');\n");
		out.write("map"+i+".addOverlay(marker"+i+");\n");
		%>
		document.getElementById("map<%=i%>").style.visibility="visible";	
		document.getElementById("showmap<%=i%>").value="Hide map";	
		document.getElementById("showmap<%=i%>").onclick=hide<%=i%>;	
		return false;  
	  	
	}	
    function hide<%=i%>() {
    	document.getElementById("map<%=i%>").style.visibility="hidden";	
    	document.getElementById("map<%=i%>").style.height="0px";	
		document.getElementById("showmap<%=i%>").value='Show map';	
		document.getElementById("showmap<%=i%>").onclick=load<%=i%>;	
		return false;  
	
    }	
	
    </script>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<% out.write(Configuration.serverrole.equals("PRD")?Configuration.GoogleApiKey:Configuration.GoogleApiKeyDev);%>"
      type="text/javascript"></script>

    <div class='champcolumn'><div class='text'><h2>User comments about this producer</h2><%
		ArrayList<Usernote> prodnotes=Usernote.getUserNotes(new Context(request),request,"producers",prod.id);
	boolean userhasnote=false;	
    if (prodnotes.size()==0){
			out.write("There are no user comments on this producer yet.<br/>");
		} else {
			for (Usernote u:prodnotes){
				out.write("On "+(new SimpleDateFormat("d MMM yyyy",new Locale(request.getHeader("Accept-Language")))).format(u.date)+", "+u.userid+" wrote: <br/><i>"+u.note+"</i><br/>");
				if (u.allowedit) {
					userhasnote=true;
					out.write("<input type='button'  onclick='javascript:document.getElementById(\"producernote"+i+"\").style.height=\"150px\";document.getElementById(\"producernote"+i+"\").style.visibility=\"visible\";' value='Edit comment'/>");
				    
				}
				out.write("<br/>");%>
				<div id='producernote<%=i%>' style='height:0px;visibility:hidden;'>
			    <form method='post' action="/winedetails/<%=Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(k.id))%>">
			    Edit your comment:
			    <textarea name='note' cols='40' rows='5'><%=u.note.replaceAll("<br/>","\n") %></textarea>
			    <input type='hidden' name='action' value='editnote'>
			    <input type='hidden' name='producerid' value='<%=u.rowid %>'>
			    <input type='hidden' name='noteid' value='<%=u.id %>'>
			    <input type='Submit' value='Save comment'/>
			    </form>
			    </div><% 
			}
		}
		if (request.getRemoteUser()==null||request.getRemoteUser().equals("")){
			out.write("<a href='/settings/redirect.jsp?url=/winedetails/"+Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(k.id))+"'>Log in</a> to write a comment or edit your notes.");
		} else {
		if (!userhasnote){
		out.write("<div onclick='javascript:document.getElementById(\"producernote"+i+"\").style.height=\"150px\";document.getElementById(\"producernote"+i+"\").style.visibility=\"visible\";'>Be the first to write a new comment</div>");
		out.write("<br/>");
		out.write("<input type='button'  onclick='javascript:document.getElementById(\"producernote"+i+"\").style.height=\"150px\";document.getElementById(\"producernote"+i+"\").style.visibility=\"visible\";' value='Add comment'/>");
	    %>
	    <div id='producernote<%=i%>' style='height:0px;visibility:hidden;'>
	    <form method='post' action="/winedetails/<%=Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(k.id))%>">
	    Enter your comment:
	    <textarea name='note' cols='40' rows='5'></textarea>
	    <input type='hidden' name='action' value='addnote'>
	    <input type='hidden' name='producerid' value='<%=prod.id %>'>
	    <input type='Submit' value='Submit comment'/>
	    </form>
	    </div>
	    <%} %>
	    <%} %>

	
	</div>
	<div class='champcolumnbottom'></div>
	</div>
	</div>
	<div class='clear'/>
	

<% if (k.allwines.size()>1){
	out.write("<br/><h2>Other wines from this producer</h2>");
	Knownwine nwine;
	for (int n:k.allwines){
		
		if (n!=k.id) {
			//out.write("<a href='/winedetails/"+Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(n))+"'>"+(Knownwines.getKnownWineName(n).replace(k.propertydescription.get(1),"").trim().equals("")?Knownwines.getKnownWineName(n):Knownwines.getKnownWineName(n).replace(k.propertydescription.get(1),"").trim())+"</a><br/>");
			String[] type=Knownwines.getGlassImage(n);
			int rating=0;
			nwine=new Knownwine(n);
			out.write("<div class='infobox' >");
			out.write("<div class='infobox"+(rating<50?"no":"")+"rating' >");
			out.write("<a href='/winedetails/"+Webroutines.URLEncode(Knownwines.getKnownWineName(n))+"'><img class='glassimg' src='/css/"+type[0]+"' alt='"+type[1]+"'/></a>");
			out.write("<div class='winename'><a href='/winedetails/"+Webroutines.URLEncode(Knownwines.getKnownWineName(n))+"'>"+(Knownwines.getKnownWineName(n).replace(nwine.propertydescription.get(1),"").trim().equals("")?Knownwines.getKnownWineName(n):Knownwines.getKnownWineName(n).replace(nwine.propertydescription.get(1),"").trim())+"</a>");
			out.write("<div class='appellation'><a href='/winedetails/"+Webroutines.URLEncode(Knownwines.getKnownWineName(n))+"'>"+("Unknown"!=nwine.propertydescription.get(2)?nwine.propertydescription.get(2):"")+"</a></div></div>");
			out.write("<div class='clearboth'></div>");
			out.write("<div class='wineinfoscore' ><a href='/winedetails/"+Webroutines.URLEncode(Knownwines.getKnownWineName(n))+"'>"+(rating>50?rating:"")+"</a></div>");
			out.write("<div class='from' ><a href='/wine/"+Webroutines.URLEncode(Knownwines.getKnownWineName(n))+"'>from</a></div>");
			out.write("<div class='wineinfoprice' ><a href='/winedetails/"+Webroutines.URLEncode(Knownwines.getKnownWineName(n))+"'>"+Webroutines.formatPrice(nwine.minprice,(double)0,searchdata.getCurrency(),"IN")+"</a></div>");
			out.write("</div></div>");
		}
	}
}
	%>





<%
	}
			}%>
	
	<%@ include file="/snippets/footer.jsp" %>	
	</div>
	</div> 

<%} else {
	%><br/><br/>Sorry... No information could be found on this wine.<% 
}
}%>

	
	

</body>
</html>
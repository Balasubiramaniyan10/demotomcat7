<html>
<jsp:include page="/header.jsp" />

<script type="text/javascript" src="/js/Dojo/dojo/dojo.js" djConfig="parseOnLoad: true">
</script>
<script type="text/javascript">
function grayOut(vis, options) {
	  // Pass true to gray out screen, false to ungray
	  // options are optional.  This is a JSON object with the following (optional) properties
	  // opacity:0-100         // Lower number = less grayout higher = more of a blackout 
	  // zindex: #             // HTML elements with a higher zindex appear on top of the gray out
	  // bgcolor: (#xxxxxx)    // Standard RGB Hex color code
	  // grayOut(true, {'zindex':'50', 'bgcolor':'#0000FF', 'opacity':'70'});
	  // Because options is JSON opacity/zindex/bgcolor are all optional and can appear
	  // in any order.  Pass only the properties you need to set.
	  var options = options || {}; 
	  var zindex = options.zindex || 50;
	  var opacity = options.opacity || 70;
	  var opaque = (opacity / 100);
	  var bgcolor = options.bgcolor || '#000000';
	  var dark=document.getElementById('darkenScreenObject');
	  if (!dark) {
	    // The dark layer doesn't exist, it's never been created.  So we'll
	    // create it here and apply some basic styles.
	    // If you are getting errors in IE see: http://support.microsoft.com/default.aspx/kb/927917
	    var tbody = document.getElementsByTagName("body")[0];
	    var tnode = document.createElement('div');           // Create the layer.
	        tnode.style.position='absolute';                 // Position absolutely
	        tnode.style.top='0px';                           // In the top
	        tnode.style.left='0px';                          // Left corner of the page
	        tnode.style.overflow='hidden';                   // Try to avoid making scroll bars            
	        tnode.style.display='none';                      // Start out Hidden
	        tnode.id='darkenScreenObject';                   // Name it so we can find it later
	    tbody.appendChild(tnode);                            // Add it to the web page
	    dark=document.getElementById('darkenScreenObject');  // Get the object.
	    var textnode = document.createElement('div');           // Create the layer.
      textnode.style.position='absolute';                 // Position absolutely
      textnode.style.top='0px';                           // In the top
      textnode.style.left='0px';                          // Left corner of the page
      textnode.style.overflow='hidden';                   // Try to avoid making scroll bars            
      textnode.style.display='none';                      // Start out Hidden
      textnode.id='darkenScreenText';                   // Name it so we can find it later
  tnode.innerHTML="<H1 style='margin-top:200px;font-size:50px;'><%
  String actie=request.getParameter("actie");
	if (actie!=null&&actie.equals("showpage")) out.write("Please wait while page is being loaded");
  	if (actie!=null&&actie.equals("analyze")) out.write("Please wait while page is being analyzed. This will take about 30 seconds.");
  %></H1>";  
		  }
	  if (vis) {
	    // Calculate the page width and height 
	    if( document.body && ( document.body.scrollWidth || document.body.scrollHeight ) ) {
	        var pageWidth = document.body.scrollWidth+'px';
	        var pageHeight = document.body.scrollHeight+'px';
	    } else if( document.body.offsetWidth ) {
	      var pageWidth = document.body.offsetWidth+'px';
	      var pageHeight = document.body.offsetHeight+'px';
	    } else {
	       var pageWidth='100%';
	       var pageHeight='100%';
	    }   
	    //set the shader to cover the entire page and make it visible.
	    dark.style.opacity=opaque;                      
	    dark.style.MozOpacity=opaque;                   
	    dark.style.filter='alpha(opacity='+opacity+')'; 
	    dark.style.zIndex=zindex;        
	    dark.style.backgroundColor=bgcolor;  
	    dark.style.width= pageWidth;
	    dark.style.height= pageHeight;
	    dark.style.display='block';
	    dark.style.color='white';
	    //darktext.style.backgroundColor='red';
	    //darktext.style.align='center';                          
	  } else {
	     dark.style.display='none';
	  }
	}



grayOut(true);

<!--
function customanalyze(winesep,fieldsep){
  		document.getElementById('winesep').value=winesep;
  		document.getElementById('fieldsep').value=fieldsep;
  		document.getElementById('actie').value='analyze';
		submitForm('<%=response.encodeURL("xpathscraper.jsp")%>');
		return 0;
}

function doit(action) {
	if (action == 'retrieve') { 
  		document.getElementById('actie').value='retrieve';
	}
	if (action == 'analyze') { 
  		document.getElementById('actie').value='analyze';
	}	
	if (action == 'showpage') { 
  		document.getElementById('actie').value='showpage';
	}	
	submitForm('<%=response.encodeURL("xpathscraper.jsp")%>');
	return 0;
}
function submitForm(actionPage) {
  
  document.formOne.action=actionPage;
  document.formOne.submit();
  return 0;
}
-->
</script>
<%	
	String url = request.getParameter("url");
	String urlregex = request.getParameter("urlregex");
	String masterurl = request.getParameter("masterurl");
	String winesep = request.getParameter("winesep");
	String fieldsep = request.getParameter("fieldsep");
	String filter = request.getParameter("filter");
	String nameorder = request.getParameter("nameorder");
	String nameregex = request.getParameter("nameregex");
	String nameexclpattern = request.getParameter("nameexclpattern");
	String vintageregex = request.getParameter("vintageregex");
	String vintageorder = request.getParameter("vintageorder");
	String priceregex = request.getParameter("priceregex");
	String priceorder = request.getParameter("priceorder");
	String sizeregex = request.getParameter("sizeregex");
	String sizeorder = request.getParameter("sizeorder");
	String headerregex = request.getParameter("headerregex");
	String message = request.getParameter("message");
	String shopid = request.getParameter("shopid");
	String assumesize = request.getParameter("assumesize");
	if (assumesize==null) assumesize="false";
	if (shopid==null||shopid.equals("")) shopid="0";
	String auto=request.getParameter("auto");
	if (auto==null) auto="";
	if (shopid.startsWith("auto")) {
		auto = "auto";
		shopid=shopid.substring(4);
	}
	if (shopid.startsWith("rating")) {
		auto = "";
		shopid=shopid.substring(6);
	}
	String rowid= request.getParameter("rowid");
	String getrow = request.getParameter("getrow");
	String tablescraper = request.getParameter("tablescraper");
	String postdata=request.getParameter("postdata");
	String AnalysisHTML="";
	String cookie=request.getParameter("cookie");
	if (cookie==null) cookie="";
	String countregex=request.getParameter("countregex");
	String counturl=request.getParameter("counturl");
	String countpostdata=request.getParameter("countpostdata");
	String countmultiplier=request.getParameter("countmultiplier");
	String encoding=Dbutil.readValueFromDB("Select * from "+auto+"shops where id="+shopid+";","encoding");
	String standardcookie=Dbutil.readValueFromDB("Select * from "+auto+"shops where id="+shopid+";","cookie");
	int shoptype=Dbutil.readIntValueFromDB("Select * from "+auto+"shops where id="+shopid+";","shoptype");
	if (actie!=null&&actie.equals("retrieve")){
		rowid="0";
		getrow=Webroutines.retrieveRow(shopid,auto);
	}
	if (shoptype==2) priceregex="no price";
	if (encoding.equals("")) encoding="iso-8859-1";
	Spider spider=new Spider(shopid,encoding,auto,1);
	
	if ((getrow!=null)&&(!getrow.equals(""))){
		ArrayList<String> rowvalue=Webroutines.getTableScrapeRow(getrow,auto);
		headerregex=rowvalue.get(0);
		tablescraper=rowvalue.get(1);
		postdata=rowvalue.get(2);
		winesep=rowvalue.get(3);		
		fieldsep=rowvalue.get(4);
		filter=rowvalue.get(5);
		nameorder=rowvalue.get(6);
		nameregex=rowvalue.get(7);
		nameexclpattern=rowvalue.get(8);
		vintageorder=rowvalue.get(9);
		vintageregex=rowvalue.get(10);
		priceorder=rowvalue.get(11);
		priceregex=rowvalue.get(12);
		sizeorder=rowvalue.get(13);
		sizeregex=rowvalue.get(14);
		urlregex=rowvalue.get(15);
		assumesize=rowvalue.get(16);
		masterurl=rowvalue.get(17);
		url=rowvalue.get(18);
		counturl=rowvalue.get(19);
		countregex=rowvalue.get(20);
		countpostdata=rowvalue.get(21);
		countmultiplier=rowvalue.get(22);
		rowid=getrow;
	
	} else {
		getrow="0";
	}
	
	
	
	String Page="";
	if (url==null) url="";
	if (urlregex==null) urlregex="";
	if (actie==null) actie="";
	if (getrow==null) getrow="";
	if (winesep==null) winesep="";
	if (fieldsep==null) fieldsep="";
	if (filter==null) filter="";
	if (nameorder==null) nameorder="";
	if (nameregex==null) nameregex="";
	if (nameexclpattern==null) nameexclpattern="";
	if (vintageorder==null) vintageorder="";
	if (vintageregex==null) vintageregex="";
	if (priceorder==null) priceorder="";
	if (priceregex==null) priceregex="";
	if (sizeorder==null) sizeorder="";
	if (sizeregex==null) sizeregex="";
	if (masterurl==null) masterurl="";
	if (headerregex==null) headerregex="";
	if (message==null) message="";
	if (rowid==null) rowid="0";
	if (postdata==null) postdata="";
	if (countregex==null) countregex="";
	if (counturl==null) counturl="";
	if (countpostdata==null) countpostdata="";
	if (assumesize==null) assumesize="";
	if (countmultiplier==null||countmultiplier.equals("")) countmultiplier="1";
	XpathParser xp=(XpathParser)session.getAttribute("xpathparser");
	xp=null;
	if (xp==null) {
		String[] labels={"name","vintage","region","price"};
		xp=new XpathParser("Wine",url);
		session.setAttribute("xpathparser",xp);
	}
	if (actie!=null&&actie.equals("showpage")){
		xp.clearPaths();	
		Page=xp.getTaggedDocumentAsString();
		out.println("<div id='targetdocument'>");
		out.println(Page);
		out.println("</div>");
	}
	
	
	if (!url.equals("")&&!actie.equals("retrieve")&&(!getrow.equals(""))) {
	
		if (masterurl.equals("Email")){
	Page=spider.getPageFromEmail(false);
		} else {
		Page=xp.getTaggedDocumentAsString();
		
		}
	}
	if (masterurl.equals("File")) url="File";
	
	
	String nameregexescaped= nameregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (nameregexescaped==null) nameregexescaped="";
	String urlregexescaped= urlregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (urlregexescaped==null) urlregexescaped="";
	String nameexclpatternescaped= nameexclpattern.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (nameexclpatternescaped==null) nameexclpatternescaped="";
	String vintageregexescaped= vintageregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (vintageregexescaped==null) vintageregexescaped="";
	String priceregexescaped= priceregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (priceregexescaped==null) priceregexescaped="";
	String headerregexescaped= headerregex.replaceAll("&","&amp;").replaceAll("\"","&quot;");
	if (headerregexescaped==null) headerregexescaped="";
	String countregexescaped= Spider.replaceString(Spider.replaceString(Spider.replaceString(countregex.replaceAll("&","&amp;"),"<","&lt;"),">","&gt;"),"\"","&quot;");
	if (countregexescaped==null) countregexescaped="";
	ArrayList shops = Webroutines.getShopList("");
	ArrayList autoshops = Webroutines.getShopList("auto");
	

%>




<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ page 
	import = "java.util.ArrayList"
	import = "java.util.List"
	import = "java.util.Iterator"
	import = "java.util.ListIterator"
	import = "java.io.*"
	import = "java.text.*"
	import = "java.lang.*"
	import = "java.sql.*"
	import = "com.freewinesearcher.common.Wine"
	import = "com.freewinesearcher.common.Wineset"
	import = "com.freewinesearcher.common.Webpage"
	import = "com.freewinesearcher.batch.Spider"
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.batch.TableScraper"
	import = "com.freewinesearcher.common.Variables"
	import = "com.freewinesearcher.common.Dbutil"
	import = "com.searchasaservice.parser.XpathParser"
%>
<title>
The Page Analyzer
</title>
</head>
<body>
<%
	out.print(message+"<br/>");
if (actie!=null&&actie.equals("analyze")){
	out.println("<div id='targetdocument'>");
	out.println("</div>");
	
}
%>

This page is used to analyze a web page on how to extract wines and test it.
<FORM name="formOne" action="<%=response.encodeURL("edittablescraper.jsp")%>" METHOD="POST"  id="formOne">
<TABLE>
<TR><TD width="25%">Select shop to update</TD><TD width="75%"><select name="shopid" default = "<%=shopid%>">
<%
	for (int i=0;i<shops.size();i=i+2){
%>
<option value="<%=shops.get(i)%>"<%if (shops.get(i).equals(shopid)) out.print("Selected");%>><%=shops.get(i+1)%>
<%
	}
%>
<%
	for (int i=0;i<autoshops.size();i=i+2){
%>
<option value="auto<%=autoshops.get(i)%>"<%if ((autoshops.get(i).equals(shopid))&&(auto.equals("auto")) ) out.print(" Selected");%>>(auto)<%=autoshops.get(i+1)%>
<%
	}
%>

</select></TD></TR>
<TR><TD>Wine Url</TD><TD><INPUT TYPE="TEXT" NAME="url" size="100" value="<%out.print(url);%>"></TD></TR>
<TR><TD>Post data (GET&... puts all GET params from URL in POST)</TD><TD><INPUT TYPE="TEXT" NAME="postdata" size="100" value="<%out.print(postdata);%>"></TD></TR>
<TR><TD>Master Url</TD><TD><INPUT TYPE="TEXT" NAME="masterurl" size="100" value="<%out.print(masterurl);%>"></TD></TR>
<%
	if (masterurl.equals("Email")){
	out.print("<TR><TD>Subject line:</TD><TD>Shop="+shopid+"&Code="+Integer.signum(("Shop"+shopid).hashCode())*("Shop"+shopid).hashCode()+"</TD></TR>");
}
%>

</TABLE>    
	<INPUT TYPE="HIDDEN" NAME="actie" id="actie" value="test">
	<INPUT TYPE="HIDDEN" NAME="rowid" value="<%=rowid%>">
	<INPUT TYPE="HIDDEN" NAME="cookie" value="<%=cookie%>">
	<input type="button" name="submitButton"
       value="Test" onclick="javascript:doit('test');">
    <input type="button" name="submitButton"
       value="Analyze this!" onclick="javascript:doit('analyze');">
    <input type="button" name="submitButton"
       value="Save" onclick="javascript:submitForm('<%=response.encodeURL("savetablescraper.jsp?saverowid="+rowid)%>');">
    <input type="button" name="submitButton"
       value="Save as New" onclick="javascript:submitForm('<%=response.encodeURL("savetablescraper.jsp")%>');">
    <input type="button" name="submitButton"
       value="Retrieve" onclick="javascript:doit('retrieve');">
    <input type="button" name="submitButton"
       value="Show page" onclick="javascript:doit('showpage');">
 
</form>

<input type="button" name="removehighlights" class='fwsremove'
       value="Remove highlights" onclick="javascript:resetSelection();">
<input type="button" class='fwsaddtoprice'
       value="Add to price" onclick="javascript:addtoprice();">
<input type="button" class='fwsaddtoname'
       value="Add to name" onclick="javascript:addtoname();">
<input type="button" class='fwsaddtoprice'
       value="Add to vintage" onclick="javascript:addtovintage();">
<%
	if (cookie!=null&&!cookie.equals("")) out.write("Cookie:"+cookie+"<br/>");
	if (actie!=null){
	if (actie.equals("test")||actie.equals("analyze")){
		if (url.equals("")){
	out.println("Enter values");} else { 
	if (winesep.equals("")){
		out.println("Enter values");} else {
		if (fieldsep.equals("")){
	out.println("Enter values");} else {
%>Search results:<table><%
	out.print ("Total wines according to site: "+Spider.checkShop(counturl, countregex,postdata, Integer.parseInt(countmultiplier))+"<br/>");
	Wine[] Winesfound = TableScraper.ScrapeWine(Page, shopid, url, urlregex, "Shopurl",spider.getBaseUrl(), headerregex, tablescraper, winesep, fieldsep, filter, nameorder, nameregex, nameexclpattern,
		vintageorder, vintageregex, priceorder, priceregex, sizeorder, sizeregex, null,1.0,1.0,TableScraper.getRatingScraper(shopid),false);
	NumberFormat format  = new DecimalFormat("#,##0.00");	
	out.println("<tr><th>Wine name</th><th>Vintage</th><th>Size&nbsp;&nbsp;&nbsp;</th><th align='right'>Price</th><th>URL</th></tr>");
	for (int i=0;i<Winesfound.length;i++){
		out.println("<tr><td>" + Winesfound[i].Name+"</td>");
		out.println("<td> " + Winesfound[i].Vintage+"</td>");
		out.println("<td align='right'> " + Webroutines.formatSize(Winesfound[i].Size)+"</td>");
		out.println("<td align='right'> " + format.format(Winesfound[i].Price)+"</td>");
		out.println("<td><a href='" + Winesfound[i].SourceUrl+"' target='_blank'>"+ Winesfound[i].SourceUrl+"</a></td></tr>");
		if (Winesfound[i].Ratings.size()>0){
	for (int k=0;k<Winesfound[i].Ratings.size();k++){
		out.println("<tr><td>Rating " + Winesfound[i].Ratings.get(k).author+":</td>");
		out.println("<td> " + Winesfound[i].Ratings.get(k).ratinglow+"</td>");
		out.println("<td align='right'> " + Winesfound[i].Ratings.get(k).ratinghigh+"</td>");
		out.println("<td align='right'></td>");
		out.println("<td></td></tr>");
		
	}
		
		}
	}
		}
	}
%>
</table>

<%
}
	}
	if (actie!=null&&actie.equals("retrieve")){
	out.print(Webroutines.getTableScrapeListHTML(shopid, response.encodeURL("edittablescraper.jsp"),auto));
	}
		
	out.print(AnalysisHTML);
	
	}
	
%>
<script type="text/javascript">
grayOut(true);


window.onload= onloadDoTimed;

function onloadDoTimed(){
	timer=setTimeout("onloadDo();",10);
	
}

function onloadDo(){
	console.info("Capturing events");
	captureEvents();
    DisableEnableLinks(true);
    DisableMouseOver();  
    grayOut(false);  
}

function DisableMouseOver(){
	var nodes=document.evaluate("//@onmouseover", document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null );
	console.info("removing "+nodes.snapshotLength+" onmouseovers");
	for (var i=0;i<nodes.snapshotLength;i++){
		
		nodes.snapshotItem(i).nodeValue="";
		console.info(nodes.snapshotItem(i));
	}
	console.info("removed "+nodes.snapshotLength+" onmouseovers");
}



function DisableEnableLinks(xHow){
  objLinks = document.links;
  for(i=0;i<objLinks.length;i++){
    objLinks[i].disabled = xHow;
    //link with onclick
    if(objLinks[i].onclick && xHow){  
        objLinks[i].onclick = new Function("return false;" + objLinks[i].onclick.toString().getFuncBody());
    }
    //link without onclick
    else if(xHow){  
      objLinks[i].onclick = function(){return false;}
    }
    //remove return false with link without onclick
    else if(!xHow && objLinks[i].onclick.toString().indexOf("function(){return false;}") != -1){            
      objLinks[i].onclick = null;
    }
    //remove return false link with onclick
    else if(!xHow && objLinks[i].onclick.toString().indexOf("return false;") != -1){  
      strClick = objLinks[i].onclick.toString().getFuncBody().replace("return false;","")
      objLinks[i].onclick = new Function(strClick);
    }
  }
}

String.prototype.getFuncBody = function(){ 
  var str=this.toString(); 
  str=str.replace(/[^{]+{/,"");
  str=str.substring(0,str.length-1);   
  str = str.replace(/\n/gi,"");
  if(!str.match(/\(.*\)/gi))str += ")";
  return str; 
} 



/*
	Developed by Robert Nyman, http://www.robertnyman.com
	Code/licensing: http://code.google.com/p/getelementsbyclassname/
*/
var getElementsByClassName = function (className, tag, elm){
	if (document.getElementsByClassName) {
		getElementsByClassName = function (className, tag, elm) {
			elm = elm || document;
			var elements = elm.getElementsByClassName(className),
				nodeName = (tag)? new RegExp("\\b" + tag + "\\b", "i") : null,
				returnElements = [],
				current;
			for(var i=0, il=elements.length; i<il; i+=1){
				current = elements[i];
				if(!nodeName || nodeName.test(current.nodeName)) {
					returnElements.push(current);
				}
			}
			return returnElements;
		};
	}
	else if (document.evaluate) {
		getElementsByClassName = function (className, tag, elm) {
			tag = tag || "*";
			elm = elm || document;
			var classes = className.split(" "),
				classesToCheck = "",
				xhtmlNamespace = "http://www.w3.org/1999/xhtml",
				namespaceResolver = (document.documentElement.namespaceURI === xhtmlNamespace)? xhtmlNamespace : null,
				returnElements = [],
				elements,
				node;
			for(var j=0, jl=classes.length; j<jl; j+=1){
				classesToCheck += "[contains(concat(' ', @class, ' '), ' " + classes[j] + " ')]";
			}
			try	{
				elements = document.evaluate(".//" + tag + classesToCheck, elm, namespaceResolver, 0, null);
			}
			catch (e) {
				elements = document.evaluate(".//" + tag + classesToCheck, elm, null, 0, null);
			}
			while ((node = elements.iterateNext())) {
				returnElements.push(node);
			}
			return returnElements;
		};
	}
	else {
		getElementsByClassName = function (className, tag, elm) {
			tag = tag || "*";
			elm = elm || document;
			var classes = className.split(" "),
				classesToCheck = [],
				elements = (tag === "*" && elm.all)? elm.all : elm.getElementsByTagName(tag),
				current,
				returnElements = [],
				match;
			for(var k=0, kl=classes.length; k<kl; k+=1){
				classesToCheck.push(new RegExp("(^|\\s)" + classes[k] + "(\\s|$)"));
			}
			for(var l=0, ll=elements.length; l<ll; l+=1){
				current = elements[l];
				match = false;
				for(var m=0, ml=classesToCheck.length; m<ml; m+=1){
					match = classesToCheck[m].test(current.className);
					if (!match) {
						break;
					}
				}
				if (match) {
					returnElements.push(current);
				}
			}
			return returnElements;
		};
	}
	return getElementsByClassName(className, tag, elm);
};


function captureEvents(){
//if (window.Event)  document.captureEvents(Event.MOUSEUP);
document.onmouseup = display;
//if (window.Event)  document.captureEvents(Event.CLICK);
document.onclick = myclick;
document.onmouseover = mymouseover;

}

function getSelectedNode(){
      var rng=null,txt="",node=null;
      if (document.selection && document.selection.createRange){
        rng=editdoc.selection.createRange();
        txt=rng.text;
      }else if (window.getSelection){
        rng=window.getSelection();
        txt=rng;
        if (rng.rangeCount > 0 && window.XMLSerializer){
          rng=rng.getRangeAt(0);
          
      }
      if (rng!=null){
          node=rng.startContainer;
          if (node!=null&&node.nodeType==3) node=node.parentNode;
          
        }
      }
      return node;
    }



var n=0;
var paths=new Array();
var priceconfig=new Array();
var nameconfig=new Array();
var vintageconfig=new Array();
var computedpath="";

dojo.require("dojo.dnd.Source"); // capital-S Source in 1.0
dojo.require("dojo.parser");	
dojo.require("dojo.fx");	


var resp="";
var kw = {
        url: (""),
        encoding: "ISO-8859-1",
        handleAs: 'text',
        sync: true,
        load: function(response, ioArgs) { // 
        	gotresponse=true;
			console.info("Response:"+response);
			resp=response;
			return response; // 
        },
        error: function(data){
        		gotresponse=true;
				console.error("Error: ",data);
                return "";
        },
        
        timeout: 10000,
        
};

var paths=0;
var newdoc="";


function display(e) {
	if (!e) {
		 var e = window.event;
	}
	if (e.target) targ = e.target
	else if (e.srcElement) targ = e.srcElement;
	if (targ.nodeType == 3) // defeat Safari bug
		targ = targ.parentNode;
	if (targ.nodeName.substring(0,4)=='HTML'||(targ.type!=null&&targ.type.substring(0,6)=='select')||(targ.className!=null&&targ.className.substring(0,3)=='fws')){
	
	    } else {
	    if (paths==2) {
	    	kw.url="xpathparser.jsp?action=clearPaths";
			dojo.xhrGet(kw);
			paths=0;
    		removehighlights();
    		
    	} else {
    	var tag=targ.getAttribute("fwscounter");
    	console.info("Tag:"+tag);
    	markNode(targ);
    	kw.url="xpathparser.jsp?action=addpath&counter="+tag;
		console.info(kw.url);
		dojo.xhrGet(kw);
		paths++;
		//document.getElementById("n").innerHTML+="<br/>paths length:"+paths.length;
		if (paths==2) {
			kw.url="xpathparser.jsp?action=gethighlights";
			dojo.xhrGet(kw);
			console.info(resp);
			timer=setTimeout("refresh();",10);
			
    	}
    }
	}
    return false;
}

function refresh(){
	var el=document.getElementById('targetdocument');
	console.info(el);
	el.innerHTML=(resp);
}

function myclick(e) {
    return false;
}
function mymouseover(e) {
    return false;
}

function markNode(node){
	node.setAttribute("fwsmarked","true");
	node.setAttribute("oldstyle",node.style);
	node.style.borderColor="blue";
	node.style.borderStyle="dotted";
	node.style.borderWidth="2px";
	
}


function removehighlights(){
	var els=getElementsByAttribute(document,'*','fwsmarked','true');
	for (var i=0;i<els.length;i++){
		els[i].setAttribute("style",els[i].getAttribute("oldstyle"));
	}
	return false;
}
	

function getElementsByAttribute(oElm, strTagName, strAttributeName, strAttributeValue){
   var arrElements = (strTagName == "*" && oElm.all)? oElm.all : oElm.getElementsByTagName(strTagName);
   var arrReturnElements = new Array();
   var oAttributeValue = (typeof strAttributeValue != "undefined")? new RegExp("(^|\\s)" + strAttributeValue + "(\\s|$)", "i") : null;
   var oCurrent;
   var oAttribute;
   for(var i=0; i<arrElements.length; i++){
 	oCurrent = arrElements[i];
   	oAttribute = oCurrent.getAttribute && oCurrent.getAttribute(strAttributeName);
	if(typeof oAttribute == "string" && oAttribute.length > 0){
  		if(typeof strAttributeValue == "undefined" || (oAttributeValue && oAttributeValue.test(oAttribute))){
   			arrReturnElements.push(oCurrent);
   		}
   	}
   }
   return arrReturnElements;
}


function getMinimizedTree(pathtree,targ){
	var origlength=document.evaluate(xpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength;
	console.info("Original targ "+targ+" has textContent "+targ.textContent);
	console.info("The original path is "+xpath+" and has a snapshotlength of "+origlength);
	if (document.evaluate(xpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength<1){
		console.error("The original path "+xpath+" has a snapshotlength of 0");
	} else if (document.evaluate(xpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0).textContent!=targ.textContent){
		console.error("The original path "+xpath+" has a different content");
		console.error("Length="+document.evaluate(xpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength);
		console.error("Content="+document.evaluate(origpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0).textContent);
	} else {	
		var path="/"+pathtree[0];
		for (var i=1;i<pathtree.length;i++){
			var obsolete=false;
			var problem=false;
			// take path, which is the collection so far, and add the rest to it
			var origpath=path;
			for (var j=i;j<pathtree.length;j++){
				origpath="/"+pathtree[j]+origpath;
			}
			// Just to make sure, see if we get the same text content as the original path
			if (document.evaluate(origpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength<1){
				problem=true;
				console.error("Path found so far has nodeitemLength of 0. Path="+origpath);
			}
			if (!problem&&(targ.textContent!=document.evaluate(origpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0).textContent)){
				problem=true;
				console.error("textContent of the original node does not match textcontext of path found so far. Path="+origpath+", textContent="+document.evaluate(origpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0).textContent);
			}
			var newpath=path;
			if (path.substr(0,2)!="//") {
				newpath="/"+newpath;
			}
			for (var j=i+1;j<pathtree.length;j++){
				newpath="/"+pathtree[j]+newpath;
			}
			console.info("Testing without "+pathtree[i]+": "+newpath+". It has snapshotLength "+document.evaluate(newpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength);
			if (!problem&&document.evaluate(newpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength<1){
				console.info("Path "+newpath+" has length 0");
				problem=true;
			}
			if (!problem&&(document.evaluate(origpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0)!=document.evaluate(newpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0))){
				problem=true;
				console.info("Path "+newpath+" has different snapshotItem(0)");
			}
			if (!problem&&(origlength!=document.evaluate(newpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength)){
				problem=true;
				console.info("Path "+newpath+" has different snapshotLength");
			}


			// Old method			
			if (false){
				// This node seems obsolete...
				// But let's see if it is by coincidence,
				// because the index is too high, or it is one of the forst records
				var ready=false;
				var regexppath=path;
				while (!ready){
					var regexppathmatch=/.*(\[(?:[2-9]|\d\d+)\])(.*?)$/.exec(regexppath);
					console.info(regexppath+" has regexpmatch:",regexppathmatch);
					if (regexppathmatch!=null){
						// [\d] found
						regexppath=regexppath.substr(0,regexppath.length-regexppathmatch[1].length-regexppathmatch[2].length)+"[1]"+regexppathmatch[2];
						console.info("regexppath:",regexppath);
						var reducedorigpath=origpath.substr(0,origpath.length-path.length)+regexppath;
						var reducednewpath=newpath.substr(0,newpath.length-path.length)+regexppath;
						console.info("reducednewpath:",reducednewpath," reducedorigpath: ",reducedorigpath);
						
						if (!problem&&(document.evaluate(reducednewpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength==0||document.evaluate(reducedorigpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotLength==0)){
							console.info("Reduced path does not exist. Assume obsolete:"+pathtree[i]+":"+origpath+"="+newpath);
							obsolete=true;
							ready=true;
						}
						if (!problem&&document.evaluate(reducedorigpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0)!=document.evaluate(reducednewpath, document, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null ).snapshotItem(0)){
							console.info("Necessary:"+pathtree[i]+", snapshotlength="+length);
							ready=true;
						}
					} else {
						console.info("Obsolete:"+pathtree[i]+":"+origpath+"="+newpath);
						obsolete=true;
						ready=true;
					}
				}
			}
			if (!problem){
				// This node seems obsolete...
				// But let's see if it is by coincidence,
				// because the index is too high, or it is one of the first records
				var regexppath=path.replace(/\[\d+\]/,"");
				var reducedorigpath=origpath.substr(0,origpath.length-path.length)+regexppath;
				var reducednewpath=newpath.substr(0,newpath.length-path.length)+regexppath;
				if (document.evaluate("count("+reducedorigpath+")", document, null, XPathResult.ANY_TYPE, null ).numberValue!=document.evaluate("count("+reducednewpath+")", document, null, XPathResult.ANY_TYPE, null ).numberValue){
					console.info("Necessary: "+pathtree[i]+". origpath has "+document.evaluate("count("+reducedorigpath+")", document, null, XPathResult.ANY_TYPE, null ).numberValue+" matches, newpath has "+document.evaluate("count("+reducednewpath+")", document, null, XPathResult.ANY_TYPE, null ).numberValue+" matches");
					problem=true;
				}
			}
			if (!problem){
				obsolete=true;
				console.info("Obsolete: "+pathtree[i]+". origpath has "+document.evaluate("count("+reducedorigpath+")", document, null, XPathResult.ANY_TYPE, null ).numberValue+" matches, newpath has "+document.evaluate("count("+reducednewpath+")", document, null, XPathResult.ANY_TYPE, null ).numberValue+" matches");
			}
			if (obsolete){
				if (path.substr(0,2)!="//") path="/"+path;
			} else {
				path="/"+pathtree[i]+path;
			}
		
		}
		//alert(path+"("+xpath+")");
		console.info("Found path is "+path);
		return path;
	}
	// Original path was not even correct
	return "";
}

function updateAnalysis(){
	var an=document.getElementById('analysis');
	an.innerHTML="";
	for (var i=0;i<nameconfig.length;i++){
		if(i==0) an.innerHTML+="Name:<br/>";
			an.innerHTML+=nameconfig[i]+"<br/>";
	}
	for (var i=0;i<vintageconfig.length;i++){
		if(i==0) an.innerHTML+="Vintage:<br/>";
			an.innerHTML+=vintageconfig[i]+"<br/>";
	}
	for (var i=0;i<priceconfig.length;i++){
		if(i==0) an.innerHTML+="Price:<br/>";
		an.innerHTML+=priceconfig[i]+"<br/>";
	}
}

<%
	if (actie!=null&&actie.equals("analyze")){%>
	kw.url="xpathparser.jsp?action=analyze";
	dojo.xhrGet(kw);
	document.getElementById("targetdocument").innerHTML=resp;
	
	<%}%>

</script>

<div style='background-color:#ffff00;position:absolute;top:400px;left:950px;width:200px;z-index:9;'>
Analysis:<br/>
<div id='analysis' onmouseover='test'></div>


</div>
<div id='n'></div>

</body> 
</html>
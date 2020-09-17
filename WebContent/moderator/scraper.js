dojo.require("dojo.dnd.Source"); // capital-S Source in 1.0
dojo.require("dojo.dnd.Moveable"); 
dojo.require("dojo.dnd.move"); 
dojo.require("dojo.parser");	
dojo.require("dojo.fx");	



var n=0;
var paths=new Array();
var priceconfig=new Array();
var nameconfig=new Array();
var vintageconfig=new Array();
var computedpath="";
var paths=0;
var newdoc="";
var resp="";
var greytext="test"
	var kw = {
		url: (""),
		encoding: "ISO-8859-1",
		mimetype: "text",
        sync: true,
		load: function(response, ioArgs) { // console.info(response);
			resp=response;
			return response; // 
		},
		error: function(data){
			gotresponse=true;
			console.error("Error: ",data);
			return "";
		},
		timeout: 10000};

var progressmeter= {
		url: (""),
		encoding: "ISO-8859-1",
		mimetype: "text",
        sync: true,
		load: function(response, ioArgs) { // console.info(response);
			resp=response;
			return response; // 
		},
		error: function(data){
			gotresponse=true;
			console.error("Error: ",data);
			return "";
		},
		timeout: 10000};


function dograyOut(vis) {
	grayOut(vis,null);
}

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
		var tbody = document.getElementById("dark");
		var tnode = document.createElement('div');           // Create the layer.
		tnode.style.position='fixed';                 // Position absolutely
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
		tnode.innerHTML="<H1 style='margin-top:300px;font-size:50px;text-align:center;color:white;'>"+greytext+"</H1><h2 id='progress'  style='font-size:30px;text-align:center;color:white;'></h4>";  
	}
	if (vis) {
		// Calculate the page width and height 
		if( document.body && ( document.body.scrollWidth || document.body.scrollHeight ) ) {
			var pageWidth = document.body.scrollWidth+'px';
			var pageHeight = (1000+document.body.scrollHeight)+'px';
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
function captureEvents(){
		//if (window.Event)  document.captureEvents(Event.MOUSEUP);
		document.onmouseup = display;
		//if (window.Event)  document.captureEvents(Event.CLICK);
		document.onclick = myclick;
		document.onmouseover = mymouseover;

}

function cancelcaptureEvents(){
	//if (window.Event)  document.captureEvents(Event.MOUSEUP);
	document.onmouseup = listen;
	//if (window.Event)  document.captureEvents(Event.CLICK);
	document.onclick = listen;
	document.onmouseover = listen;

}

function listen(){
	return true;

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



	function olddisplay(e) {
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
var field=0;
function editfield(f){
	greytext="<img src='/images/spinner.gif' alt='wait'/>";
	dograyOut(true);
	field=f;
	kw.url="xpathparser.jsp?action=clearHighlights";
	dojo.xhrGet(kw);
	document.getElementById('targetdocument').innerHTML=resp;
	document.getElementById('undoeditfield'+f).parentNode.innerHTML="<div id='undoeditfield"+field+"' onclick='javascript:undoeditfield();' onmouseover='document.body.style.cursor=\"pointer\"' onmouseout=\"document.body.style.cursor='default'\" style=\"color:blue;text-decoration:underline\">Undo</div>";
	document.getElementById('saveeditfield'+f).parentNode.innerHTML="<div id='saveeditfield"+field+"' onclick='javascript:saveeditfield();' onmouseover='document.body.style.cursor=\"pointer\"' onmouseout=\"document.body.style.cursor='default'\" style=\"color:blue;text-decoration:underline\">Save</div>";
	hideedit();
	captureEvents();	
	document.getElementsByTagName("body")[0].onmouseover='this.style.cursor="move"';
	dograyOut(false);
	
}

function setAppendWineryField(checked){
	kw.url="xpathparser.jsp?action=clearAppendWineryField";
	if (checked){
		kw.url="xpathparser.jsp?action=setAppendWineryField";
	} 
	dojo.xhrGet(kw);
}

function undoeditfield(){
	greytext="<img src='/images/spinner.gif' alt='wait'/>";
	dograyOut(true);
	cancelcaptureEvents();	
	kw.url="xpathparser.jsp?action=undonewPath";
	dojo.xhrGet(kw);
	document.getElementById('targetdocument').innerHTML=resp;
	document.getElementById('undoeditfield'+field).innerHTML="";
	document.getElementById('saveeditfield'+field).innerHTML="";
	showedit();
	dograyOut(false);
	
}

function saveeditfield(){
	greytext="<img src='/images/spinner.gif' alt='wait'/>";
	dograyOut(true);
	cancelcaptureEvents();	
	kw.url="xpathparser.jsp?action=savenewPath";
	dojo.xhrGet(kw);
	document.getElementById('targetdocument').innerHTML=resp;
	document.getElementById('undoeditfield'+field).innerHTML="";
	document.getElementById('saveeditfield'+field).innerHTML="";
	refreshColors();
	showedit();
	dograyOut(false);
}

function hideedit(){
	var els=getElementsByAttribute(document,'div','class','fwseditfield');
	for (var i=0;i<els.length;i++){
		els[i].style.visibility="hidden";
		els[i].innerHTML="";
		//alert(els[i].style);
	}
	document.getElementById("fwssave").innerHTML="";
}

function showedit(){
	var els=getElementsByAttribute(document,'div','class','fwseditfield');
	for (var i=0;i<els.length;i++){
		els[i].style.visibility="visible";
		els[i].innerHTML="Edit";
	}
	document.getElementById("fwssave").innerHTML="Save configuration";
}



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
		var tag=targ.getAttribute("fwscounter");
		greytext="<img src='/images/spinner.gif' alt='wait'/>";
		dograyOut(true);
		kw.url="xpathparser.jsp?action=newPath&counter="+tag+"&field="+field;
		dojo.xhrGet(kw);
		
		document.getElementById('targetdocument').innerHTML=resp;
		dograyOut(false);
		
	}
}
function makeDrag(){
	$(function() {
		$( "#analysisplaceholder" ).draggable();
	});
	
//var m=dijit.placeOnScreen(document.getElementById("analysisplaceholder"),800,400,50);
}

function savexp(){
	kw.url="xpathparser.jsp?action=savexp";
	dojo.xhrGet(kw);
	if (resp=="saved"){
		window.location = 'analyzer.jsp';
	} else if (resp=="notsaved") {
		window.location="/moderator/editshop.jsp?xp=true";
	} else {
		alert("ERROR: could not save configuration! Result: -"+resp+"-");
	}
}

function refreshColors(){
	kw.url="xpathparser.jsp?action=getColors";
	var col=dojo.xhrGet(kw);
	//console.info("Col:",col);
	var p = eval("(" + resp + ")");
	for (var i=0;i<p.color.length;i++){
		if (p.color[i]=="#dddddd"){
			document.getElementById("fwsfont"+(i+1)).style.textDecoration="line-through";
			document.getElementById("fwsfont"+(i+1)).style.color="#000000";
		} else {
			document.getElementById("fwsfont"+(i+1)).style.textDecoration="none";
			document.getElementById("fwsfont"+(i+1)).style.color=p.color[i];
		}
	}
}
function analyzetimed(){
	timer=setTimeout("analyze();",100);
}

var progress;
var resp="";
var stopprogress=false;
function initprogresstimer(){
	progress=progressmeter;
	progress.url="xpathparser.jsp?action=progress";
	progress.mimetype="text";
	timer=setTimeout("showprogress();",500);
}
function setprogresstimer(){
	if (!stopprogress) timer=setTimeout("showprogress();",500);
}

function showprogress(){
	dojo.xhrGet(progress);
	document.getElementById('progress').innerHTML=resp;
	setprogresstimer();
}


var redirectpage="showwines.jsp";
function analyze(){
	initprogresstimer();
	kw.url="xpathparser.jsp?action=analyze";
	dojo.xhrGet(kw);
	if (resp=="Error"){
		alert("No records could be found on this page.");
		grayOut(false);
		stopprogress=true;
		
	} else {
		stopprogress=true;
		window.location = redirectpage;
	}
}






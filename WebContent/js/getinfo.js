var mouseover=false;
if (typeof(window.FWScolor) == "undefined"||window.FWScolor.length<3) FWScolor='#ffffff';
if (typeof(window.FWStagname) == "undefined") FWStagname='["item fn", "fn"]';

// This script should display wine prices. It looks for "fn" and "item fn" class tags.
// For each element, the price is quoted
// To do: "item fn" tags are somehow ignored, script refreshes display instead of adding a div box


function makeVisible(){
mouseover=true;
//changeOpac(this.lastChild.id,100);
this.lastChild.style.visibility="visible";
}

function makeBoxVisible(){
mouseover=true;
this.style.visibility="visible";
}

function hide(){
mouseover=false;
//opacity(this.lastChild.id,100,0,1000);
id=this.lastChild.id;
setTimeout("hidenow('"+id+"')",1000);
}

function hidenow(id){
if (!mouseover) document.getElementById(id).style.visibility='Hidden';
}

function hideBox(){
this.style.visibility="hidden";
}

function opacity(id, opacStart, opacEnd, millisec) { 
    //speed for each frame 
    var speed = Math.round(millisec / 100); 
    var timer = 0; 

    //determine the direction for the blending, if start and end are the same nothing happens 
        for(i = opacStart; i >= opacEnd; i--) { 
            if (!mouseover) setTimeout("changeOpac('" + id + "'," + i + ")",(timer * speed)); 
            timer++; 
        } 
    
} 

//change the opacity for different browsers 
function changeOpac(id,opacity) { 
    var object = document.getElementById(id).style; 
    object.opacity = (opacity / 100); 
    object.MozOpacity = (opacity / 100); 
    object.KhtmlOpacity = (opacity / 100); 
    object.filter = "alpha(opacity=" + opacity + ")"; 
} 


function getElementsByClassName(oElm, strTagName, oClassNames){
    var arrElements = (strTagName == "*" && oElm.all)? oElm.all : oElm.getElementsByTagName(strTagName);
    var arrReturnElements = new Array();
    var arrRegExpClassNames = new Array();
    if(typeof oClassNames == "object"){
        for(var i=0; i<oClassNames.length; i++){
            arrRegExpClassNames.push(new RegExp("(^|\\s)" + oClassNames[i].replace(/\-/g, "\\-") + "(\\s|$)"));
        }
    }
    else{
        arrRegExpClassNames.push(new RegExp("(^|\\s)" + oClassNames.replace(/\-/g, "\\-") + "(\\s|$)"));
    }
    var oElement;
    var bMatchesAll;
    
    for(var j=0; j<arrElements.length; j++){
        oElement = arrElements[j];
        bMatchesAll = true;
        for(var k=0; k<arrRegExpClassNames.length; k++){
            if(!arrRegExpClassNames[k].test(oElement.className)){
                bMatchesAll = false;
                break;                      
            }
        }
        if(bMatchesAll){
            arrReturnElements.push(oElement);
        }
    }
    return (arrReturnElements)
}

//window.addEventListener("load", addTags(), false); 
//alert("I got loaded...");

//function addTags(){
var g_remoteServer = 'https://www.vinopedia.com/js/getinfo.jsp?name=';
var tags=getElementsByClassName(document, "*", FWStagname);
for (x=0; x<tags.length; x++) {
	var wine=tags[x].innerHTML;
	var id=tags[x].id;
	var script = document.createElement('script');	
	var args=wine.replace(" ","%20")+"&id="+x+"&url="+window.location.href;	
	script.src = g_remoteServer+args;
	script.type = 'text/javascript';	
	script.defer = false;	
	tags[x].appendChild(script);
	tags[x].onmouseover = makeVisible;
	tags[x].onmouseout=hide;
	//document.getElementById(id).onmouseover = makeBoxVisible;
	tags[x].lastChild.onmouseout = hideBox;
}
//}
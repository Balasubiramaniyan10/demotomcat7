/*
	This is the JavaScript file for the AJAX Suggest Tutorial

	You may use this code in your own projects as long as this 
	copyright is left	in place.  All code is provided AS-IS.
	This code is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
	
	For the rest of the code visit http://www.DynamicAJAX.com
	
	Copyright 2006 Ryan Smith / 345 Technical / 345 Group.	

*/
function getXmlHttpRequestObject() {
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	} else if(window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} 
}

var searchReq = getXmlHttpRequestObject();

function keyDown(oEvent) {
    switch(oEvent.keyCode) {
        case 38: //up
            previousSuggestion();
            break;
        case 40: //down
            nextSuggestion();
            break;
    }
}

function searchSuggest(oEvent) {
	var key =(oEvent.which) ? oEvent.which : oEvent.keyCode;
	if ((key>40)||(key==8)||(key==55)){
		if (searchReq.readyState == 4 || searchReq.readyState == 0) {
			var str = escape(document.getElementById('name').value);
			if (str.length>2){
				searchReq.open("GET", '/js/suggest.jsp?input=' + str, true);
				searchReq.onreadystatechange = handleSearchSuggest; 
				searchReq.send(null);
			}
		}		
	} else {
		if (key==13){
			selectSuggestion();
		} else {
		if (key==27){
			removeSuggestions();
		}
	
	}
	} 
}

function navigationkeys(oEvent) {
	var press=true;
	var key =(oEvent.which) ? oEvent.which : oEvent.keyCode;
	if (key==13){
			oEvent.keycode=55;
			if (document.getElementById('selected')){
				press=false;
			}
	}
	return press;
}

function empty(){}

function handleSearchSuggest() {
	if (searchReq.readyState == 4) {
		var ss = document.getElementById('search_suggest')
		ss.innerHTML = '';
		var str = searchReq.responseText.split("\n");
		if (str.length>3){
			var result=str[0];
			var now = escape(document.getElementById('name').value);
			for(i=1; i < str.length - 2; i=i+2) {
				var suggest = '<div id="notselected" name="'+str[i+1]+'" onmouseover="javascript:suggestOver(this);" ';
				suggest += 'onmouseout="javascript:suggestOut(this);" ';
				suggest += 'onclick="javascript:setSearch(this);" ';
				suggest += 'class="suggest_link">' + str[i] + '</div>';
				ss.innerHTML += suggest;
			}
			ss.className='search_suggest_border';
		} else {
			ss.className='search_suggest_noborder';
		}
		
	}
}

function emptySuggest(){
var ss = document.getElementById('search_suggest')
		ss.className='search_suggest_noborder';
		ss.innerHTML = '';
}

function suggestOver(div_value) {
	if (document.getElementById('selected')!=null){
		if (div_value.id!='selected'){
	document.getElementById('selected').className='suggest_link';
	document.getElementById('selected').id='notselected';
		}
	}
	div_value.className = 'suggest_link_over';
	div_value.id='selected';
}

function setSearch(nodename) {
	document.getElementById('name').value = nodename.getAttribute('name').replace("&amp;","&");
	document.getElementById('search_suggest').innerHTML = '';
	document.getElementById('search_suggest').className='search_suggest_noborder';
	document.getElementById('searchform').submit();
}
function selectSuggestion(){
	var selectedNode=document.getElementById('selected');
    if (selectedNode){
	setSearch(selectedNode);
    }
}
function removeSuggestions(){
	document.getElementById('search_suggest').innerHTML = '';
	document.getElementById('search_suggest').className='search_suggest_noborder';
}

function nextSuggestion() {
    var selectedNode=document.getElementById('selected');
    if (selectedNode){
	    var nextNode=selectedNode.nextSibling;
		if (nextNode){
	    	nextNode.id='selected';
	    	nextNode.className='suggest_link_over';
	    	selectedNode.id='notselected';
	    	selectedNode.className='suggest_link';
	    }
	} else {
		selectedNode=document.getElementById('notselected');
		if (selectedNode){
		   	selectedNode.id='selected';
		   	selectedNode.className='suggest_link_over';
		}
	}
}
function previousSuggestion() {
    var selectedNode=document.getElementById('selected');
    if (selectedNode){
	    var previousNode=selectedNode.previousSibling;
		if (previousNode){
	    	previousNode.id='selected';
	    	previousNode.className='suggest_link_over';
	    	selectedNode.id='notselected';
	    	selectedNode.className='suggest_link';
		}
	}
}
function suggestOut(div_value) {}

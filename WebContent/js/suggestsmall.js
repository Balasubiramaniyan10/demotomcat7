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
//Gets the browser specific XmlHttpRequest Object
function getXmlHttpRequestObject() {
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	} else if(window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} 
}

//Our XmlHttpRequest object to get the auto suggest
var searchReq = getXmlHttpRequestObject();



function keyDown(oEvent) {
    switch(oEvent.keyCode) {
        case 38: //up arrow
            previousSuggestion();
            break;
        case 40: //down arrow
            nextSuggestion();
            break;
    }
}


//Called from keyup on the search textbox.
//Starts the AJAX request.
function searchSuggest(oEvent) {
	var key =(oEvent.which) ? oEvent.which : oEvent.keyCode;
	if ((key>40)||(key==8)){
	
		if (searchReq.readyState == 4 || searchReq.readyState == 0) {
			var str = escape(document.getElementById('name').value);
			searchReq.open("GET", '/js/suggest.jsp?input=' + str, true);
			searchReq.onreadystatechange = handleSearchSuggest; 
			searchReq.send(null);
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
	if ((key==40)||(key==38)){
		press=false;
	} else {
		if (key==13){
			oEvent.keycode=55;
			if (document.getElementById('selected')){
				press=false;
			}
		}
	}
	return press;
}



//Called when the AJAX response is returned.
function handleSearchSuggest() {
	if (searchReq.readyState == 4) {
		var ss = document.getElementById('search_suggest')
		ss.innerHTML = '';
		var str = searchReq.responseText.split("\n");
		for(i=1; i < str.length - 2; i++) {
			//Build our element string.  This is cleaner using the DOM, but
			//IE doesn't support dynamically added attributes.
			var suggest = '<div id="notselected" onmouseover="javascript:suggestOver(this);" ';
			suggest += 'onmouseout="javascript:suggestOut(this);" ';
			suggest += 'onclick="javascript:setSearch(this.innerHTML);" ';
			suggest += 'class="suggest_link">' + str[i] + '</div>';
			ss.innerHTML += suggest;
		}
		if (str.length>3){
		ss.className='search_suggest_border';
		} else {
		ss.className='search_suggest_noborder';
		}
		
	}
}
// Called when clicked outside the box
function emptySuggest(){
var ss = document.getElementById('search_suggest')
		ss.className='search_suggest_noborder';
		ss.innerHTML = '';
		
}

//Mouse over function
function suggestOver(div_value) {
	if (document.getElementById('selected')!=null){
	document.getElementById('selected').className='suggest_link';
	document.getElementById('selected').id='notselected';
	}
	div_value.className = 'suggest_link_over';
	div_value.id='selected';
}
//Mouse out function
function suggestOut(div_value) {
	//div_value.className = 'suggest_link';
	//div_value.id='';
}
//Click function
function setSearch(value) {
	document.getElementById('name').value = value;
	document.getElementById('search_suggest').innerHTML = '';
	document.getElementById('search_suggest').className='search_suggest_noborder';
}

function selectSuggestion(){
	var selectedNode=document.getElementById('selected');
    if (selectedNode){
	setSearch(selectedNode.innerHTML);
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
		//select first
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
	    
		} else {
		// do nothing
		}
	}


}
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
		var ss = document.getElementById('search_suggest');
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
var ss = document.getElementById('search_suggest');
if (ss!=null){		ss.className='search_suggest_noborder';
		ss.innerHTML = '';}
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

// Showing TN and Refine items
var visible='';
var timer;
function hide(id){
	if ((id+'')==('tnsuperholder')){
		hidetn(id);
	} else {
	if (visible==id){
	musthide=id;
	if (document.getElementById(id)!=null) timer=setTimeout(function(){
		if ((id+'')==(''+musthide)){
		
			if (document.getElementById(id)!=null) hidenow(id);
			
		} 
	},1000);
	}
	}
	}

	function hidenow(id){
	if ((id+'')==('tnsuperholder')){
		hidenowtn(id);
	} else {

	if (document.getElementById(id)!=null) document.getElementById(id).style.visibility = 'hidden';
	visible='';
	}
	}

	function show(id){
	if ((id+'')==('tnsuperholder')){
		showtn(id);
	} else {

	if ((id+'')!=(''+visible)) if (document.getElementById(visible)!=null) 	hidenow(visible);
	document.getElementById(id).style.visibility = 'visible';
	visible=id; 
	musthide='';
	}
	}
	function hidetn(id){
		if (visible==id){
			musthide=id;
		if (document.getElementById(id)!=null) timer=setTimeout(function(){
			if ((id+'')==(''+musthide)){
				if (document.getElementById(id)!=null) hidenowtn(id);
				
			} 
		},1000);
		}
	}


	function hidenowtn(id){
		if (document.getElementById(id)!=null) {
			document.getElementById("tnsuperholder").style.visibility = 'hidden';
			document.getElementById("tnholder").style.height = '0px';
			document.getElementById("tnholder").style.visibility = 'hidden';
			document.getElementById("tnheader").style.visibility = 'hidden';
			document.getElementById("divtnframe").style.visibility = 'hidden';
			document.getElementById("tnframe").style.visibility = 'hidden';
		}
		visible='';
	}
	
	var tnframe;

	function showtn(id){
		if ((id+'')!=(''+visible)) if (document.getElementById(visible)!=null) 	document.getElementById(visible).style.visibility = 'hidden';
		if (typeof tnsrc!="undefined"){
			if (typeof tnframe=="undefined"){
				tnframe=document.getElementById('tnframe');
				tnframe.src=tnsrc;
			}
		}
		document.getElementById("tnsuperholder").style.visibility = 'visible';
		document.getElementById("tnholder").style.height = '420px';
		document.getElementById("tnholder").style.visibility = 'visible';
		document.getElementById("tnheader").style.visibility = 'visible';
		document.getElementById("divtnframe").style.visibility = 'visible';
		document.getElementById("tnframe").style.visibility = 'visible';
		visible=id; 
		musthide='';
	}
	function hidetop(id){
		visible='';
		if (document.getElementById(id)!=null) timer=setTimeout("if (visible!='"+id+"') {document.getElementById('"+id+"').style.visibility = 'hidden';document.getElementById('"+id+"').style.display = 'none';}",1000);
	}

	function showtop(id){
		if ('toptastingnote'!=id) hidenow('toptastingnote');
		if ('suggestions'!=id) hidenow('suggestions');
		if ('refine'!=id) hidenow('refine');
		if (id=='ratingexplanation'&&$('#ratingexplanation').html()=='') {$.ajax({url:'/ratingexplanation.jsp',success: function(data){$('#ratingexplanation').html(data)}});}
		document.getElementById(id).style.visibility = 'visible';
		document.getElementById(id).style.display = 'block';
		visible=id; 
		clearTimeout(timer);
	}
	
function initSmartSuggest(){
	$("#name").attr("autocomplete","off");
	$("#name").autocomplete("/js/suggest.jsp",{minChars:3,delay:200,matchSubset:false,onItemSelect: function(li) {selectsuggest(li)}  });
	if (document.getElementById("logoandsearch")==null&&document.getElementById("linkboard")==null) {
		$(".ac_results").addClass("big");
	}else{
		$(".ac_results").addClass("small");
		
	}
}

function vpclick(link){
	var target=''+link;
	location.href=target;
}

function clickshop(storelink,wineid){
	var target=storelink+"?wineid="+wineid;
	location.href=target;
}

function vpclick2(a,b,c){var target=''+a+b+c;location.href=target;}

function setcookie(name,value){document.cookie = name + "=" +escape( value ) +( ( expires ) ? ";expires=" + new Date( today.getTime() + (2000) ).toGMTString() : "" ) +";domain=www.vinopedia.com";}

function selectsuggest(li){
	
	$("#name").attr('name','searchfield');
	$("#name").attr('value',"Searching...");
	$("#searchform").attr('accept-charset','UTF-8');
	$("#searchform").attr('action',li.extra[1]);
	$("#name").addClass("ac_loading");
	$("#searchform").submit();
	if (typeof(console) != "undefined") console.info("Form submit");
}

(function($) {

	$.extend({
		add2cart: function(source_id, target_id, callback) {
    
      var source = $('#' + source_id );
      var target = $('#' + target_id );
      
      var shadow = $('#_shadow');
      if( !shadow.attr('id') ) {
          $('body').prepend('<div id="_shadow" style="display: none; background-color: #ddd; border: solid 1px darkgray; position: static; top: 0px; z-index: 100000;">&nbsp;</div>');
          var shadow = $('#_shadow');
      }
      
      
      
      shadow.width(source.css('width')).height(source.css('height')).css('top', source.offset().top).css('left', source.offset().left).css('opacity', 0.5).show();
      shadow.css('position', 'absolute');
      
      shadow.animate( { width: target.innerWidth(), height: target.innerHeight(), top: target.offset().top, left: target.offset().left }, { duration: 300 } )
        .animate( { opacity: 0 }, { duration: 100, complete: callback } );
        
		}
	});
})(jQuery);


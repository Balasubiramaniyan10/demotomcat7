// JavaScript Document

//var $j = jQuery.noConflict();//To protect from prototype error
function loadJSLibraries(libraryName){
	$('body').append('<script type="text/javascript" src="/js/library/'+libraryName+'.js"><\/script>');
}

$(function(){
//Fancybox single image
	$("a#zoomout_picture").fancybox({
		'transitionIn'	: 'elastic',
		'transitionOut'	: 'elastic'
	});
	
//Fancybox multiple images	
	$("a[rel=zoomout_group]").fancybox({
		'transitionIn'		: 'none',
		'transitionOut'		: 'none',
		'titlePosition' 	: 'over',
		'titleFormat'		: function(title, currentArray, currentIndex, currentOpts) {
			return '<span id="fancybox-title-over">Image ' + (currentIndex + 1) + ' / ' + currentArray.length + (title.length ? ' &nbsp; ' + title : '') + '</span>';
		}
	});
	
//Fancybox popup killer	
	$("#zoomout_content").fancybox();
	
	$('a.popup').click(function(){
		var w = 450 , h = 400;
		var size = $(this).attr("rel");
		if(size != ''){
			var str = size;
			var pattern = /x/i;
			result = str.split(pattern);
			w = result[0];
			h = result[1];
		}
 		//window.open(this.href, '', 'width='+w+', scrollbars="yes", toolbar="no", location="no", status="no", menubar="no", resizable="no", height='+h);
		window.open(this.href,'popup','width='+w+',height='+h+',toolbar=no,location=no,directories=no,status=yes,menubar=no,scrollbars=yes,copyhistory=yes,resizable=yes')
 		return false;
	});
	
//Rotating image gallery
/*	$('.slideshow').cycle({
		fx:     'fade',
		speed:  'fast',
		timeout: 0,
		next:   '#next',
		prev:   '#prev', 
    	after:   onAfter
	});
	function onAfter() { 
		$('#output').html('<p>' + this.children[0].alt + '</p>');
		$('.slideshow').css({height:20+this.children[0].height+$('#output').height()+'px'})
	};*/
});

//for ipad/ipod/iphone/android
/*$('#listmenu a').live('touchend', function(e) {
    var el = $(this);
    var link = el.attr('href');
    window.location = link;
});*/


//OMNITURE
function linkCode(obj,filename) {
	var s=s_gi('mshankenwine');
	s.linkTrackVars='None';
	s.linkTrackEvents='None';
	s.tl(obj,'d',filename);
}

// collapsable

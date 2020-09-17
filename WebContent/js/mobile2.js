function getlocation(){navigator.geolocation.getCurrentPosition(success, error);}

function error() {
		error('Could not determine your location');
		

}
function success(position) {
	  var s = document.querySelector('#status');
	  
	  if (s.className == 'success') {
	    // not sure why we're hitting this twice in FF, I think it's to do with a cached result coming back    
	    return;
	  }
	  
	  s.innerHTML = "found you!";
	  s.className = 'success';
	  
	  window.location.href=("/m.jsp?keepdata=true&order=distance&lat="+position.coords.latitude+"&lon="+position.coords.longitude);
	  
	}

$("#prices").live('pagecreate',function(event){
	pInfScrExecute();
	$(window).scroll(function(){
		$.doTimeout( 'scroll', pInfScrDelay, pInfScrExecute);
		// ensure that, when we're outside the target area, delay is set nice and low
		// this counteracts the longer delay set at window.onpopstate below
		if( $(document).height() - 800 > $(document).scrollTop() + $(window).height() )
			pInfScrDelay = 200;
	});	
	$(".distancebtn").each(function(index) {
	    if ($(this).attr("href")=="#"){$(this).unbind().tap( function() {getlocation();return false;})}});
	});
	



//(4)
//pInfScr = pax infinite scroll
var pInfScrLoading = false;

//turn on or off intra-page navigation when back button used
//1 = back button navigates WITHIN infinitely scrolling page
//0 = back button navigates to previous, referring page
var pInfScrIntraPageBackButton = 0;

/* 
Pax browsing supports the following:
(1) 	/category/page/3/ loads the third page of results from that category
(2) 	/category/pages/3/ loads 3 pages of results, including page 3, from the beginning
			/category/pages/1-3/ does the same as previous, except page 1 is explicit
			/category/pages/2-3/ loads 2 pages of results, including page 3, excluding page 1

Why is this necessary?

We don't want to break the back button. 
(1) 	User loads /all/page/1/
(2) 	Scrolling dynamically adds the contents of /all/page/2/ to the page
			At the same time, the history is updated to reflect the state of the page.
			That's where the pageS capability comes in.
(3a)	As long as we started on page 1, we can just update history to /all/pages/2/
			That way, when the user hits the back button, /all/pages/2/ will be loaded.
(3b)	What if the user started at page /all/page/2/ ?
			The user scrolls, and the page dynamically updates with contents of /all/page/3/
			The history is updated to reflect the state of the page.
			PROBLEM! The history no longer reflects the state of the page.
			The history says the current page is /all/pages/3/, but we started browsing at /all/page/2/.
			You see where this is going?
			This is a problem, because when the user hits the back button, they'll end up at an unfamiliar place
			on the resulting page because /all/pages/3/ loads 3 pages, including page 1. But we started at page 2.
(4)		Instead of simply asking for the number of pages loaded, we also explicitly request the start of page range.
			So, in (3b), user begins at /all/page/2/
			Scrolling results in dynamic load of /all/page/3/, and history is updated to reflect the state:
			History becomes /all/pages/2-3/.
			User scrolls more, resulting in dynamic load of /all/page/4/, history is updated to reflect the state:
			History becomes /all/pages/2-4/.

That unchanging value (2 in the example above) is the page base we must hang onto through 
dynamic loads and history state updates.
Hence the variable below.
Phew.
*/
var pInfScrUrlCarryPageBase = null;

//we'd also like to know the number of items returned
//that way, we can clear them out if the user hits the 'back' button
var pInfScrCount = new Array();

//we'll dynamically change the delay on the loading function,
//depending on the situation
//(1) DEFAULT: user scrolls down to trigger zone, 200 milliseconds, event fires
//(2) SPECIAL: user hits back button, inadvertently in target zone, 5000 millisecond delay, giving user a chance to scroll away from trigger zone
//delay, in milliseconds, for firing debounced infinite scroll function
var pInfScrDelay = 200;

function pInfScrExecute() {
	// jQuery plugin pathchange was authored by Ben Cherry (bcherry@gmail.com),
	// and is released under an MIT License (do what you want with it).
	// http://www.adequatelygood.com/2010/7/Saner-HTML5-History-Management
	// http://www.bcherry.net/playground/sanerhtml5history
	// http://www.bcherry.net/static/lib/js/jquery.pathchange.js
	// Some of the code in this plugin was adapted from Modernizr, which is also available under an MIT License.
	//
	// Modified by Alex Micek to change from a web-app type plugin (intercepting all links, stopping all hashes),
	// to an on-demand function for managing infinite scroll.
	// If path change requested, it is only done in browsers where we can avoid breaking the back button.
	// Contrary to Cherry's original authoring of the plugin, I chose NOT to use hash functionality
	// (monitoring, changing) to preserve application state. Instead, we simply say we cannot
	// do a pathchange, and the calling script takes appropriate action.
	
	// Simple feature detection for History Management (borrowed from Modernizr)
	function detectHistorySupport() {
		return !!(window.history && history.pushState);
	}

	function pathchange(path) {	
		return true;
		// if there is history support, use it
		if (detectHistorySupport()) {
			// remember: pushState/replaceState only allows same-origin changes
			// https://developer.mozilla.org/en/DOM/Manipulating_the_browser_history#The_pushState().c2.a0method

			// as explicated in the "Pax browsing" comment above, we need a base page number
			// set ONLY ONCE per complete page load
			if(pInfScrUrlCarryPageBase == null) {
				/* path will be checked for in two places
				(1) browser URL, where it looks like this: 
						/category/page/xx/
						or this:
						/category/pages/xx-yy/
				(2) the link to the next page:
						/category/page/zz/ in the path variable
				
				xx is the number we are looking for (pInfScrUrlCarryPageBase)
				yy is a number greater than xx
				zz is xx+1
				
				Naturally, we put precedence on version (1); it's less brittle
				*/

				// instanatiate a regular expression that will match the following
				// /page/ or /pages/
				// 1+ 		numbers
				// 0 or 1 dash
				// 0+ 		numbers
				// 0 or 1 forward slash
				// tacked to the end of the string 
				regexp = /\/pages?\/([0-9]+)-?[0-9]*\/?$/;
				
				// (1)
				if(regexp.test(window.location.href)) {
					nextPage = regexp.exec(window.location.href);
					pInfScrUrlCarryPageBase = nextPage[1];
				}
				// (2)
				else {
					nextPage = regexp.exec(path);
					// so xx - 1 will give us the page base				
					pInfScrUrlCarryPageBase = nextPage[1] - 1;
				}				
			}

			// modify path with xx
			path = path.replace("page/","pages/"+pInfScrUrlCarryPageBase+"-");
			
			// change state and report that it worked
			if(pInfScrIntraPageBackButton == 1)
				window.history.pushState(null, null, path);
			else
				window.history.replaceState(null, null, path);
			return true;
		}
	
		// otherwise, report that the pathchange failed
		else
			return false;
	}

	// doc.height()	unitless pixel value of HTML document height
	// .scrollTop()	pixels hidden from view above scrollable area
	//							0 if scroll bar at top, or element not scrollable
	// win.height()	unitless pixel value of browser viewport height
	
	// an AJAX request for content to append to the current page is made if:
	// (1) scroll to 400px or less from bottom of document
	// (2) not currently in process of loading new content
	if(pInfScrLoading == false&&$(document).height() - 1000 < $(document).scrollTop() + $(window).height() ) {
		morelink='a.more'
		// get URL
		pInfScrNode = $(morelink);			
		pInfScrURL = $(morelink).attr("href");
		// make request if
		// (3) node was found
		// (3.5) node is not hidden
		// (4) pathchange is supported, to preserve back button functionality (SCREW THE HASH BANG OPTION)
		if(!!pInfScrURL&&pInfScrNode.length > 0) {
			
			// node was found, pathchange worked... make request
			$.ajax({
				type: 'GET',
				url: pInfScrURL,
				beforeSend: function() {
					// block potentially concurrent requests
					
					pInfScrLoading = true;
					
					// display loading feedback
					//pInfScrNode.clone().empty().insertAfter(pInfScrNode);

					// hide 'more' browser
					pInfScrNode.html("Loading...<img src='/images/spinner.gif'/ alt='Loading'>");
					
					
					
					
				},
				success: function(data) {
					// remove loading feedback
					//pInfScrNode.next().remove();
					// "whitespace nodes have no style attribute, which is what's causing the problem"
					// nodetype to avoid TypeError: Result of expression 'this[a].style' [undefined] is not an object 
					// use nodetype to grab elements				
					var moreDataLink = $(".more",data).attr("href");
					
					var filteredData = $(".divider",data);
					
					// count the number of items returned
					pInfScrCount.push(filteredData.length);
					
					
					// drop data into document
					filteredData.insertBefore(pInfScrNode);
					
					if (!moreDataLink||moreDataLink=="#nomore"){
						pInfScrNode.html("");
						
					}else {
						
					// unblock more requests (reset loading status)
					pInfScrLoading = false;
					pInfScrNode.attr("href",moreDataLink);
					pInfScrNode.html("");
					
					//pInfScrNode.show();
					}
					return false;
				},
				dataType: "html"
			});	
			
		}
	}
}

/*
 * jQuery doTimeout: Like setTimeout, but better! - v1.0 - 3/3/2010
 * http://benalman.com/projects/jquery-dotimeout-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */
(function($){var a={},c="doTimeout",d=Array.prototype.slice;$[c]=function(){return b.apply(window,[0].concat(d.call(arguments)))};$.fn[c]=function(){var f=d.call(arguments),e=b.apply(this,[c+f[0]].concat(f));return typeof f[0]==="number"||typeof f[1]==="number"?this:e};function b(l){var m=this,h,k={},g=l?$.fn:$,n=arguments,i=4,f=n[1],j=n[2],p=n[3];if(typeof f!=="string"){i--;f=l=0;j=n[1];p=n[2]}if(l){h=m.eq(0);h.data(l,k=h.data(l)||{})}else{if(f){k=a[f]||(a[f]={})}}k.id&&clearTimeout(k.id);delete k.id;function e(){if(l){h.removeData(l)}else{if(f){delete a[f]}}}function o(){k.id=setTimeout(function(){k.fn()},j)}if(p){k.fn=function(q){if(typeof p==="string"){p=g[p]}p.apply(m,d.call(n,i))===true&&!q?o():e()};o()}else{if(k.fn){j===undefined?e():k.fn(j===false);return true}else{e()}}}})(jQuery);
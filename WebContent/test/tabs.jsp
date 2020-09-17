<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- DOCTYPE is always recommended. see: http://www.quirksmode.org/css/quirksmode.html -->

<!--
	This is the jQuery Tools standalone demo, the fastest way to get started.
	You can freely copy things on your site. All demos can be found here:

	http://flowplayer.org/tools/demos/

	- css files should not be referenced from flowplayer.org when in production

	Enjoy!
-->

<head>
	<!-- standalone page styling (can be removed)-->
	<link rel="stylesheet" type="text/css" href="http://static.flowplayer.org/tools/css/standalone.css"/>
<link rel="stylesheet" type="text/css" href="/css/tabs.css" />
	<title>jQuery Tools standalone demo</title>
	<!-- default set of jQuery Tools + jQuery 1.3.2 -->

	<script src="http://cdn.jquerytools.org/1.1.2/jquery.tools.min.js"></script>
	
	
</head>

<!-- without body tag IE may have unprodictable behaviours -->
<body>

<!-- now that jQuery Tools is included we can begin with the actual demo -->




<!-- tab styling -->



<!-- tab pane styling -->
<style>

/* tab pane styling */
div.panes div {
	display:none;		
	padding:15px 10px;
	border:1px solid #999;
	border-top:0;
	height:100px;
	font-size:14px;
	background-color:#fff;
}

</style>



<!-- the tabs -->
<ul class="tabs">
	<li><a href="#">Tab 1</a></li>
	<li><a href="#">Tab 2</a></li>
	<li><a href="#">Tab 3</a></li>

</ul>

<!-- tab "panes" -->
<div class="panes">
	<div>First tab content. Tab contents are called "panes"</div>
	<div>Second tab content</div>
	<div>Third tab content</div>
</div>


<!-- This JavaScript snippet activates those tabs -->

<script>

// perform JavaScript after the document is scriptable.
$(function() {
	// setup ul.tabs to work as tabs for each div directly under div.panes
	$("ul.tabs").tabs("div.panes > div");
});
</script>



</body>

var vpmapcenter;
var vpmapzoomlevel=0;


function switchpoi(el){
	jQuery.post('/jsinteraction.jsp?action=setAttribute&amp;name='+el.className+'&amp;value='+el.checked);
	if (el.className=='showproducers'){
		if (el.checked) {
			for(var i=0; i<mapdetailmarkers.length; i++){mapdetailmarkers[i].show();}
		} else {
			for(var i=0; i<mapdetailmarkers.length; i++){mapdetailmarkers[i].hide();}
		}
	}
	if (el.className=='showsubregions'){
		if (el.checked) {
			javascript:$(".regionpoi").show();
		} else {
			javascript:$(".regionpoi").hide();
		}
	}
}

function poiinit(){
	$("#switchpoi").html("<form><input type='checkbox' class='showproducers' id='showproducers' "+(showproducers?"checked='checked'":"")+" onclick='switchpoi(this);' />Show producers<br/><input type='checkbox' class='showsubregions' id='showsubregions' "+(showsubregions?"checked='checked'":"")+" onclick='switchpoi(this);' />Show subregions</form>");
	switchpoi(document.getElementById("showproducers"));
	switchpoi(document.getElementById("showsubregions"));
}

function setTypegotoGuide(type){
	setType(type);
	$('#regiontabs').tabs().click(3);
	$('#regiontabs').tabs().click(4);
}
function setGrapegotoGuide(grape){
	setGrape(grape);
	$('#regiontabs').tabs().click(3);
	$('#regiontabs').tabs().click(4);
}
$(document).ready(function(){
	initSmartSuggest();
	$("#regiontabs").tabs("#regionpane > div");
	if($("#regiontabs").data("tabs")&&$("#regiontabs").data("tabs").getCurrentTab().attr("href")=='#mappane') showregionlocation();
	initAjax();
	swfobject.embedSWF(
			  "/open-flash-chart.swf", "grapestats",
			  "350", "200", "9.0.0", "expressInstall.swf",
			  {"get-data":"get_data_1"} );

			swfobject.embedSWF(
			  "/open-flash-chart.swf", "typestats",
			  "350", "200", "9.0.0", "expressInstall.swf",
			  {"get-data":"get_data_2"} );
});

function showregionlocation(){
	if ($('#mapdetail').html()==''){
		$('#mappane').css('display','block');
	loadmapdetail();
	mapdetail.addControl(new GMapTypeControl());
	mapdetail.addControl(new GLargeMapControl());
	loadmapregion();
	loadmapworld();
	}
}




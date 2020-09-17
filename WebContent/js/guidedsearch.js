var scrollwindow;
var resultpages;
initPages();
var lastpage=new Array(false,false,false,false);
var offset=new Array(0,0,0,0,0);
var lastpageindex=0;
var updatedfirsthalf=new Array(false,false,false,false,false);
var updatedsecondhalf=new Array(false,false,false,false,false);
var request=new Array(0,0,0,0,0);
var activepane=1;
var newsearch=false;
var priceslider;
var doseek=true;


function init(){
	 
	
	$('#pricemin').hide();
	$('#pricemax').hide();
	makePriceSlider();
	if (true||$.browser.msie) {
		$("#gstabs").tabs("#gspanes  > div.pan");
		sttabs=$("#storetabs").tabs("#storepanes > div.storepane",{onClick: function(event, tabIndex) { 
	        getStoreContent(event,tabIndex); 
	    } });
		//alert(sttabs);
		
	}else {
		$("#gstabs").tabs("#gspanes  > div.pan",{effect:'fade'});
		$("#storetabs").tabs("#storepanes > div.storepane",{effect:'slide',onClick: function(event, tabIndex) { 
	        getStoreContent(tabIndex); 
	    } });
		
	}
	updateSlider();
	makeScrollable();
	if (getScrollwindow(1)){
		resultpages[1][0]=getScrollwindow(1).getItems().eq(0).html();
		getPage(2);
		getPage(3);
		if ($("#pane4").html()) getPage(4);
		getPage(1);
		$("#gstabs").tabs().onClick(function(event, tabIndex) { setActivePane(tabIndex+1); });
	}

}

function initAjax(){
	makePriceSlider();
	makeScrollable();
	if (true||$.browser.msie) {
		$("#gstabs").tabs("#gspanes  > div.pan"); 
	}else {
		$("#gstabs").tabs("#gspanes  > div.pan",{effect:'fade'}); 
	} 
	$("#gstabs").tabs().onClick(function(event, tabIndex) { setActivePane(tabIndex+1); });
	newSearch();
}

function makePriceSlider(){
	priceslider=$('select#pricemin, select#pricemax').selectToUISlider({
		sliderOptions: {
		change:function(e, ui) {
		setBudget();
		}
		},
		labels:0,
		animate: true,
		range: true,
		min: $("#slider-min").html(),
		max: $("#slider-max").html(),
		values: [$("#slider-min").html(), $("#slider-max").html()]
		});
}

function makeVintageSlider(){
	$('select#vintageminsl, select#vintagemaxsl').selectToUISlider({
		sliderOptions: {
		change:function(e, ui) {
		$("#vintageclose").hide();
		$("#vintagespinner").show();
		newSearch();
		}
		},
		labels:0,
		animate: true,
		range: true,
		min: $("#vintageminscale").val(),
		max: $("#vintagemaxscale").val(),
		values: [$("#vintageminsl").val(), $("#vintagemaxsl").val()]
		});
	$("#vintageslider .ui-widget-header").removeClass("ui-widget-header").addClass("ui-widget-header"); //IE repositioning
	if (document.getElementById("vintageclose")){
		//$("#vintageslider .ui-widget-greyheader").addClass("ui-widget-header").removeClass("ui-widget-greyheader");
	} else {
		$("#vintageslider .ui-widget-header").addClass("ui-widget-greyheader").removeClass("ui-widget-header");
	}
	

}

function clearVintage(){
	$("#vintageclose").hide();
	$("#vintagespinner").show();
	$("#vintageminsl").val($("#vintageminscale").val());
	$("#vintagemaxsl").val($("#vintagemaxscale").val());
	newSearch();
	
}

function correctVintageSlider(){
	$("#vintagemin").val($("#vintageminsl").val());
	$("#vintagemax").val($("#vintagemaxsl").val());
	if ($("#vintageminscale").val()==$("#vintagemin").val()) $("#vintagemin").val(0);
	if ($("#vintagemaxscale").val()==$("#vintagemax").val()) $("#vintagemax").val(0);
	
}

function setActivePane(pane){
	activepane=pane;
	//console.log("setActivePane: Pane "+1+" page "+getScrollwindow(1).getPageIndex()+", Pane "+2+" page "+getScrollwindow(2).getPageIndex()+", Pane "+3+" page "+getScrollwindow(3).getPageIndex()+"");
	//alert(pane);
	//getPage(0,pane);
}

function getActiveScrollwindow(){
	return $("#pane"+activepane).scrollable();
}

function getScrollwindow(pane){
	return $("#pane"+pane).scrollable();
}

function makeScrollable(){
	$("#pane1").scrollable({nextPage:'#next1',prevPage:'#prev1',size: 1,speed:600,clickable:false,onSeek: function(){getPage(1);}});		 
	$("#pane2").scrollable({nextPage:'#next2',prevPage:'#prev2',size: 1,speed:600,clickable:false,onSeek: function(){getPage(2);}});		 
	$("#pane3").scrollable({nextPage:'#next3',prevPage:'#prev3',size: 1,speed:600,clickable:false,onSeek: function(){getPage(3);}});		 
	if ($("#pane4").html()) $("#pane4").scrollable({nextPage:'#next4',prevPage:'#prev4',size: 1,speed:600,clickable:false,onSeek: function(){getPage(4);}});		 
	//$('#next').click(function () {alert("k")});
	//$('#prev').click(function () {getPage();});

}


function setBudget(){
	spin('price');
	$("#handle_pricemin").removeClass("ui-slider-handle ui-state-default ui-corner-all ui-state-focus").addClass("ui-slider-handle ui-state-default ui-corner-all");
	$("#handle_pricemax").removeClass("ui-slider-handle ui-state-default ui-corner-all ui-state-focus").addClass("ui-slider-handle ui-state-default ui-corner-all");
	newSearch();
}


function newSearch(){
	newsearch=true;
	correctVintageSlider();
	$(".ac_results").hide();
	lastpage=new Array(false,false,false,false);
	initPages();
	document.getElementById("page").value="0";
	getResults(activepane);
	getResults(1);
	getResults(2);
	getResults(3);
}

function clearRegion(){
	document.getElementById('region').value='All';
	newSearch();
}


function oldclearVintage(){
	document.getElementById('vintage').value='';
	newSearch();
}
function clearGrape(){
	document.getElementById('grape').value='';
	newSearch();
}
function setGrape(g){
	document.getElementById('grape').value=g;
	newSearch();
}

function clearBudget(){
	//priceslider.slider('option', 'values', [0,200]);
	document.getElementById("pricemin").value=0;
	document.getElementById("pricemax").value=200;
	//priceslider.unbind("change");
	$('#pricemin').trigger('change');
	$('#pricemax').trigger('change');
	spin('price');
	//priceslider.bind("change",function(e, ui) {
	//	setBudget();
	//	});
	//newSearch();
	
	
}

function spin(id){
	$('#'+id+'close').hide();
	$('#'+id+'spinner').show();
}

function updateSlider(){
	if (document.getElementById("vintageminsl")!=null) makeVintageSlider();
	
	if (document.getElementById("pricemin").value==0&&document.getElementById("pricemax").value==200) {
		$("#priceslider .ui-widget-header").removeClass("ui-widget-header").addClass("ui-widget-greyheader");
		document.getElementById('priceclose').style.display = 'none';
	} else {
		$("#priceslider .ui-widget-greyheader").removeClass("ui-widget-greyheader").addClass("ui-widget-header");
		document.getElementById('priceclose').style.display = 'block';
	}
	$("#handle_pricemin").find(".ttContent").html(document.getElementById('symbol').value+' '+document.getElementById("pricemin").value);
	$("#handle_pricemax").find(".ttContent").html(document.getElementById('symbol').value+' '+document.getElementById("pricemax").value);
	if (document.getElementById("ratingminscale")) makeSlider('rating');
	$("#pricespinner").hide();
	
	
	
}





function makeSlider(id){
	if ($("#"+id+"minscale")){
	var min=Math.max(parseInt($("#ratingmin").val()),parseInt($("#ratingminscale").val()));
	var max=Math.min(parseInt($("#ratingmax").val()),parseInt($("#ratingmaxscale").val()));
	$("#"+id).slider({
		slide: function(e, ui) {//slide function
		var thisHandle = jQuery(ui.handle);
		//handle feedback 
		var textval =(ui.value);
		thisHandle
			.attr('aria-valuetext', textval)
			.attr('aria-valuenow', ui.value)
			.find('.ui-slider-tooltip .ttContent')
				.text( textval );

		//control original select menu
		//var currSelect = jQuery('#' + thisHandle.attr('id').split('handle_')[1]);
		//currSelect.find('option').eq(ui.value).attr('selected', 'selected');
		},
		range: true,
		min:parseInt($("#ratingminscale").val()),max:parseInt($("#ratingmaxscale").val()),values: [min,max],
		change: function(event, ui) {setRating();}
	});
	if (parseInt($("#ratingmin").val())>parseInt($("#ratingminscale").val())||parseInt($("#ratingmax").val())<parseInt($("#ratingmaxscale").val())) {
		$("#"+id+'close').show();
		$("#"+id+" .ui-widget-greyheader").removeClass("ui-widget-greyheader").addClass("ui-widget-header");
	} else {
		$("#"+id+" .ui-widget-header").removeClass("ui-widget-header").addClass("ui-widget-greyheader");
	}
	$("#"+id+' a').append('<span class="screenReaderContext"></span><span class="ui-slider-tooltip ui-widget-content ui-corner-all"><span class="ttContent"></span><span class="ui-tooltip-pointer-down ui-widget-content"><span class="ui-tooltip-pointer-down-inner"></span></span></span></a>');
	$("#"+id+' a:first').find(".ttContent").html(min);
	$("#"+id+' a:last').find(".ttContent").html(max);
	}
}

function clearRating(){
	$("#ratingmin").val(80);
	$("#ratingmax").val(100);
	newSearch();
	
}

function setRating(){
	$('#ratingclose').hide();
	$('#ratingspinner').show();
	if ($('#rating').slider('option', 'values')[0]==parseInt($("#ratingminscale").val())){
		$("#ratingmin").val(80);
	} else {
		$("#ratingmin").val($('#rating').slider('option', 'values')[0]);
	}
	if ($('#rating').slider('option', 'values')[1]==parseInt($("#ratingmaxscale").val())){
		$("#ratingmax").val(100);
	} else {
		$("#ratingmax").val($('#rating').slider('option', 'values')[1]);
	}
	newSearch();
}

function setRegionbyvalue(r){
	$('#regionspinner').show();
	document.getElementById("region").value=r.replace("&apos;","'");
	newSearch();
}


function setRegion(r){
	$('#regionclose').hide();
	if(($("#recommendationtab").length==0)&&$("#winedetails").offset()!=null&&$(window).scrollTop()<$("#winedetails").offset().top) $.scrollTo('#winedetails',1000);
	$("#recommendationtab").click();
	if(r.selectValue){document.getElementById("region").value=r.selectValue;}else{document.getElementById("region").value=r;}
	$('#regionspinner').show();
	newSearch();
}

function initRegion(){
	if (document.getElementById("region").value=='All') document.getElementById("region").value='';
	
}

function clearcountryofseller(){
	document.getElementById('countryofseller').value='All';
	newSearch();
}

function setSubregion(t,el){
	$(el).children('.spinner').show();
	document.getElementById("region").value=t;
	newSearch();
}

function setType(t,el){
	$(el).children('.spinner').show();
	document.getElementById("winetype").value=t;
	newSearch();
}

function donothing(){
}




function initPages(){
	resultpages=new Array(4);
	for (i=0; i <5; i++) resultpages[i]=[];
}

function getPage(pane){
	handlePageLogic(pane);
	handleButtons(pane);
	handleCache(pane);
	//console.log("getPage: Pane "+1+" page "+getScrollwindow(1).getPageIndex()+", Pane "+2+" page "+getScrollwindow(2).getPageIndex()+", Pane "+3+" page "+getScrollwindow(3).getPageIndex()+"");
	
	//alert(pane+" "+resultpages[pane].length);
	//return true;
}

function handleCache(pane){
	
	if ((offset[pane]+getScrollwindow(pane).getPageIndex()+5)>resultpages[pane].length&&!lastpage[pane]){
		document.getElementById("page").value=resultpages[pane].length;
		getResults(pane);
	}
	
}

function handleButtons(pane){
	var p=getScrollwindow(pane).getPageIndex();
	if (offset[pane]+p>=resultpages[pane].length-1) {
		$("#next"+pane).hide();
	} else {
		$("#next"+pane).show();
	}
	if (p==0) {
		$("#prev"+pane).hide();
	} else {
		$("#prev"+pane).show();
	}
	
}

function handlePageLogic(pane){
	if (updatedfirsthalf[pane]){
		updateSecondHalf(pane);
	} else if (updatedsecondhalf[pane]){
		reverseupdateFirstHalf(pane);
	} else {
		
		if ((getScrollwindow(pane).getPageIndex()+3)>getScrollwindow(pane).getPageAmount()&&resultpages[pane].length>(getScrollwindow(pane).getPageAmount()+offset[pane])){
			if (getScrollwindow(pane).getPageAmount()>4) {
				updateFirstHalf(pane);
			} else {
				addNext(pane);
			}
		} else {
		
		
		if ((getScrollwindow(pane).getPageIndex()<2)&&offset[pane]>0) {
			if (getScrollwindow(pane).getPageAmount()>4) {
				reverseupdateSecondHalf(pane);
			} else {
				addPrevious(pane);
			}
		}
		}
	}
}

function removeAllbutfirst(pane) { 
	if (getScrollwindow(pane).getItems().size()>1){
    	getScrollwindow(pane).getItems().eq(1).remove();
    	removeAllbutfirst(pane);
    }
	
}

function updateFirstHalf(pane) { 
	updatedfirsthalf[pane]=true;
	getScrollwindow(pane).getItems().eq(0).html(getScrollwindow(pane).getItems().eq(1).html());
	getScrollwindow(pane).getItems().eq(1).html(getScrollwindow(pane).getItems().eq(2).html());
	getScrollwindow(pane).getItems().eq(2).html(getScrollwindow(pane).getItems().eq(3).html());
	getScrollwindow(pane).setPage(2,0);
}
function updateSecondHalf(pane) { 
	updatedfirsthalf[pane]=false;
	getScrollwindow(pane).getItems().eq(3).html(getScrollwindow(pane).getItems().eq(4).html());
	getScrollwindow(pane).getItems().eq(4).html(resultpages[activepane][offset[pane]+getScrollwindow(pane).getPageAmount()]);
	offset[pane]++;
}

function reverseupdateSecondHalf(pane) { 
	updatedsecondhalf[pane]=true;
	getScrollwindow(pane).getItems().eq(4).html(getScrollwindow(pane).getItems().eq(3).html());
	getScrollwindow(pane).getItems().eq(3).html(getScrollwindow(pane).getItems().eq(2).html());
	getScrollwindow(pane).getItems().eq(2).html(getScrollwindow(pane).getItems().eq(1).html());
	getScrollwindow(pane).setPage(2,0);
}
function reverseupdateFirstHalf(pane) { 
	updatedsecondhalf[pane]=false;
	getScrollwindow(pane).getItems().eq(1).html(getScrollwindow(pane).getItems().eq(0).html());
	//alert("Getting page "+(offset[pane]-1));
	getScrollwindow(pane).getItems().eq(0).html(resultpages[pane][offset[pane]-1]);
	offset[pane]--;
}


function removeLast(pane) { 
    getScrollwindow(pane).getItems().eq(getScrollwindow(pane).getItems().size()-1).remove(); 
}
function addNext(pane) { 
   	getScrollwindow(pane).getItemWrap().append(resultpages[pane][getScrollwindow(pane).getPageAmount()+offset[pane]]);
   	getScrollwindow(pane).reload(); 
   	
	
}
function addPrevious(pane) {
	getScrollwindow(pane).getItemWrap().prepend(resultpages[pane][offset[pane]]);
	getScrollwindow(pane).setPage(getScrollwindow(pane).getPageIndex()+1,1); 
}

function seekOff(pane){
	getScrollwindow(pane).unbind("onSeek");
}

function seekOn(pane){
	doseek=false;
	getScrollwindow(pane).bind("onSeek",function(){seek(pane);});
}

function seek(pane){
	//if (doseek) {
		getPage(activepane);
	//}
	doseek=true;
}

function getResults(pane) { // 
	if (!lastpage[pane]){
		var requestpane=pane;
		if(newsearch) requestpane=0; 
		if(request[requestpane]) {request[requestpane].abort();}
		
		gotresponse=false;
		//document.getElementById("result").innerHTML="<img src='/images/spinner.gif'/>&nbsp;Getting results";
		//spinner(); //cleartype bug with fade
		var param="";
		if (newsearch) param="&newsearch=true";
		newsearch=false;
		request[requestpane]=$.ajax({
			type: "POST",
			url: "/js/advicehtml.jsp?activepane="+pane+param,
			timeout:30000,
			data: $("#GuidedSearchform").serialize(),
			success: function(data){   storedata(data);  },
			error: function(XMLHttpRequest, textStatus, errorThrown){
				gotresponse=true;
				//document.getElementById("result").innerHTML="Sorry... Could not retrieve the results due to a technical problem. Please try again.";
			}
		});
	}
}

var ac=false;
var numpages=1;

function storedata(response){
	gotresponse=true;
	var res=eval('('+response+')');
	var pane=parseInt(res.pane);
	if (res.page==0) {
		lastpage[pane]=false;
		seekOff(pane);
		getScrollwindow(pane).setPage(1,0);
		getScrollwindow(pane).getItems().eq(0).html(res.result[0]);
		getScrollwindow(pane).setPage(0);
		removeAllbutfirst(pane);
		//getScrollwindow(pane).reload().begin(); 
		offset[pane]=0;
	}
	//if (pane==1) alert(getScrollwindow(pane).getItems().eq(0).html());
	
	lastpage[pane]=(res.lastpage === 'true');
	if (res.result!=null) {
		for (i=0;i<res.result.length;i=i+1)	{
		resultpages[pane][parseInt(i)+parseInt(res.page)]=res.result[i];
	}
	if (res.page==0) {
		seekOn(pane);
	}
	
	$("#result h1").html(res.h1); 
	handlePageLogic(pane);
	handleButtons(pane);
	if (document.getElementById("numpages")!=null) {
		numpages=$("#numpages").val();
	}
	$(".numpages").html(numpages);

	}

	if (res.facets.length>10) {
		ac=false;
		document.getElementById("criteria").innerHTML = res.facets; 
		$(".numpages").html($("#numpages").val());
		updateSlider();
	}
	if (!ac) {
		ac=true;
		$("#region").autocomplete("/js/suggestregion.jsp",{onItemSelect:setRegion, minChars:2 });
	}
	
	
	
	
return response;
}


	function spinner(){
		document.getElementById('result').style.removeAttribute('filter');
		$("#result").fadeOut(1500,
		 function(){
			document.getElementById('result').style.removeAttribute('filter');
			$("#result").show();
			$("#result").animate( {opacity: 1.0});
			document.getElementById('result').style.removeAttribute('filter');
			if (!gotresponse){
			document.getElementById("result").innerHTML="<h1><img src='/images/spinner.gif'/>&nbsp;Getting results</h1>";
			
		}
		});
	}
   // 

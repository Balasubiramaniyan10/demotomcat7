function showinfo(id){
	$('#'+id).show();
}

function showWine(id,elid){
	$('#recommendationtab').click();
	$.ajax({
		type: "POST",
		url: "/js/advicehtml.jsp?section=getwinejson&wineid="+id+"&currency="+$('input[name=currency]:checked').val()+"&shopid="+$('#shopid').val() ,
		timeout:10000,
		success: function(data){   handleCartResponse(data,elid);  },
		error: function(XMLHttpRequest, textStatus, errorThrown){
			gotresponse=true;
		}
	});
	$.ajax({
		type: "POST",
		url: "/js/advicehtml.jsp?section=getadjson&wineid="+id+"&currency="+$('input[name=currency]:checked').val()+"&shopid="+$('#shopid').val() ,
		timeout:10000,
		success: function(data){   handleCartResponse(data,elid);  },
		error: function(XMLHttpRequest, textStatus, errorThrown){
			gotresponse=true;
		}
	});
	return false;
}

function showCharts(){
	if($(window).scrollTop()>$("#winedetails").offset().top) $.scrollTo('#winedetails',1000);
	$('#shopcharts').html('<div id="chart1"></div><div id="chart2"></div><div id="chart3"></div>');
	swfobject.embedSWF("/open-flash-chart.swf", "chart1", "468", "250", "9.0.0","expressInstall.swf",{"data-file":"/store/storechart.jsp%3Fchart%3D1"});
	swfobject.embedSWF("/open-flash-chart.swf", "chart2", "468", "250", "9.0.0","expressInstall.swf",{"data-file":"/store/storechart.jsp%3Fchart%3D2"});
}

function addtocart(){
	$.ajax({
		type: "POST",
		url: "/js/cart.jsp?action=addtocart",
		data: $("#wineinfo").serialize(),
		timeout:10000,
		success: function(data){   handleCartResponse(data,'');  },
		error: function(XMLHttpRequest, textStatus, errorThrown){
			gotresponse=true;
		}
	});
	$("#shoppinglist").click();
	return false;
}

function changeamount(wineid,amount){
	if($(window).scrollTop()>$("#cart").offset().top) $.scrollTo('#cart',1000);
	$.ajax({
		type: "POST",
		url: "/js/cart.jsp?action=addtocart&wineid="+wineid+"&amount="+amount,
		timeout:10000,
		success: function(data){   handleCartResponse(data);  },
		error: function(XMLHttpRequest, textStatus, errorThrown){
			gotresponse=true;
		}
	});
	return false;
}


function handleCartResponse(response,id){
	gotresponse=true;
	var res=eval('('+response+')');
	if (res.winehtml){
		if (id){
			$.add2cart(id,'winedetails',function(){
				$('#_shadow').hide();
				$('#winedetails').html(res.winehtml);
				$('#winedetails').show();
			}
			);
		}else {
			$('#winedetails').html(res.winehtml);
			$('#winedetails').show();
		}
	}
	if (res.carthtml){
		$('#cart').html(res.carthtml);
	}
	if (res.adrighthtml){
		$('#adright').html(res.adrighthtml);
	}
}

function handleChartResponse(response){
	gotresponse=true;
	var res=eval('('+response+')');
	$('#shopcharts').html('');
	if (res.image1){
		$('#shopcharts').append('<img src="'+res.image1+'" alt=""/>');
	}
	if (res.image2){
		$('#shopcharts').append('<img src="'+res.image2+'" alt=""/>');
	}

}
function loadmap() {
	if ($('#map').html()==''){
	var map = new GMap2(document.getElementById("map"));
	map.addControl(new GLargeMapControl());
	map.setCenter(center, 8);
	var marker=new GMarker(center, {draggable: true, bouncy: true});
	map.addOverlay(marker);
	document.getElementById("map").style.visibility="visible";	
	return false;  
	}
}	


function getStoreContent(e,index){
	var tab=e.originalTarget || e.srcElement||(e.originalEvent&&(e.originalEvent.originalTarget||e.originalEvent.srcElement));
	if (tab&&tab.getAttribute('id')=='maptab') loadmap();
	if (tab&&tab.getAttribute('id')=='statstab'&&$("#shopstatstext").html()!='') showCharts();
}


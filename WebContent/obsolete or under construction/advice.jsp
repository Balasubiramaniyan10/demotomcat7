var djConfig = {bindEncoding: "ISO-8859-1"};

function donothing()
{
}

function toggleglass(id){
	node=document.getElementById('glass'+id);
	if (node.className=="glass") {
		node.className="glassselected";
		document.getElementById(id).setAttribute("value","true");
	} else {
		node.className="glass";
		document.getElementById(id).setAttribute("value","false");
	}
	getResults();
}

function setBudget(amount){
if (amount==0) amount=document.getElementById('pricemax').value;
if (amount==0) amount=99999;
document.getElementById('5euro').className="money";
document.getElementById('10euro').className="money";
document.getElementById('20euro').className="money";
document.getElementById('50euro').className="money";
document.getElementById('99999euro').className="money";
document.getElementById(amount+'euro').className="moneyselected";
document.getElementById('pricemax').value=amount;
getResults();
}


var gotresponse=false;       
var kw = {
        url: "/js/adviceJSON.jsp",
        encoding: "ISO-8859-1",
        handleAs: 'json',
        load: function(response, ioArgs) { // 
        	gotresponse=true;
        	dojo.byId("result").innerHTML = parsewine(response); // 
        	dojo.fadeOut({node: 'result', duration: 1}).play();
        	dojo.fadeIn({node: 'result', duration: 500}).play();
		return response; // 
        },
        error: function(data){
        		gotresponse=true;
                dojo.byId("result").innerHTML="I could not retrieve the result due to a problem. Please try again.";
				dojo.fadeOut({node: 'result', duration: 1}).play();
        		dojo.fadeIn({node: 'result', duration: 10}).play();
        },
        timeout: 10000,
        form: "RegionSearchform"
};
function parsewine(jsonwine){
var html="";
if (typeof jsonwine.previouspage!="undefined") html+=jsonwine.previouspage;
if (typeof jsonwine.nextpage!="undefined") html+=jsonwine.nextpage;
if (typeof jsonwine.wine!="undefined"){
	html+="<div class=\"clear\" >&nbsp;</div>";
	for (var i=0;i<jsonwine.wine.length;i++){
	html+="<div class=\"spacer\"><div class=\"wineinfo\">"
	html+="<div class=\"top\">";
	html+="<div class=\"winename\">"+jsonwine.wine[i].name+"</div>";
	<%if (request.isUserInRole("admin")){%>
		html+="<a href=\"/admin/addknownwine.jsp?id="+jsonwine.wine[i].kwid+"\" target=\"_blank\">Edit wine</a>";
	<%		}%>
	if (jsonwine.wine[i].region!="Unknown") html+="<div class=\"appellation\">"+jsonwine.wine[i].region+"</div>";
	html+="<div class=\"vintage\">"+jsonwine.wine[i].vintage+"</div>";
	html+="</div>";
	html+="<div class=\"right\">";
	var twenty="";
	if (jsonwine.scale=="20") twenty="twenty";
	html+="<div class=\"score\"><div class=\""+twenty+"ratingbox\"><div class=\""+twenty+"rating\" style=\"background:#"+jsonwine.wine[i].ratingcolor+"\">"+jsonwine.wine[i].rating+"</div></div></div>";
	html+="<div class=\"typeimg\"><img src=\"/images/"+jsonwine.wine[i].img+"\" alt=\""+jsonwine.wine[i].typetext+"\"/></div>";
	html+="<div class=\"typetext\">"+jsonwine.wine[i].typetext+"</div>";
	html+="</div>";
	html+="<div class=\"bottom\">";
	html+="<div class=\"price\">"+jsonwine.wine[i].price+"</div>";
	html+="<div class=\"buttons\"><a href=\""+jsonwine.wine[i].link+"\" target=\"_blank\"><input type=\"button\" class=\"find\"  value=\"Find prices\"/></a>";
	html+="<input type=\"button\" class=\"gettn\" onmouseout=\"javascript:hide('tn"+i+"');\" onclick=\"javascript:showTN("+jsonwine.wine[i].kwid+","+jsonwine.wine[i].vintage+",'"+('tn'+i)+"');\" value=\"Tasting Notes\"/></div>";
	html+="</div>";
	html+="</div>";
	html+="<div class=\"clear\"  onmouseout=\"javascript:hide('tn"+i+"');\" onmouseover=\"javascript:showTN("+jsonwine.wine[i].kwid+","+jsonwine.wine[i].vintage+",'"+('tn'+i)+"');\"><div class=\"advicetastingnote\" id=\"tn"+i+"\"  onmouseout=\"javascript:hide('tn"+i+"');\" onmouseover=\"javascript:showTN("+jsonwine.wine[i].kwid+","+jsonwine.wine[i].vintage+",'"+('tn'+i)+"');\"><div class=\"tncontent\" id=\"tn"+i+"content\"  onmouseout=\"javascript:hide('tn"+i+"');\" onmouseover=\"javascript:showTN("+jsonwine.wine[i].kwid+","+jsonwine.wine[i].vintage+",'"+('tn'+i)+"');\"></div></div></div>";
	html+="</div>";
	}	
} else {
	html="<div class=\"clear\" >&nbsp;</div>No results found. Please broaden your search criteria.";
}
if (typeof jsonwine.previouspage!="undefined") html+=jsonwine.previouspage;
if (typeof jsonwine.nextpage!="undefined") html+=jsonwine.nextpage;
return html;
}



var reg = {
        url: "/js/regionJSON.jsp",
        encoding: "ISO-8859-1",
        handleAs: 'json',
        load: function(response, ioArgs) { // 
        	gotresponse=true;
        	parseregion(response); // 
		return response; // 
        },
        error: function(data){
        		gotresponse=true;
        },
        timeout: 10000,
        form: "RegionSearchform"
};

function changeRegion(region){
dojo.byId("regionselector").innerHTML = "<input type='hidden' name='region' id='region' value='"+region+"'/>";
dojo.byId("selectedregion").value=region;
getResults();
dojo.xhrPost(reg);

}


function parseregion(regionresponse){
var html="";
var region=dojo.byId("region").getAttribute("value");
if (typeof regionresponse.path!="undefined"){
	if (regionresponse.path.length>1){
	for (var i=0;i<regionresponse.path.length;i++){
		if (i<regionresponse.path.length-1){
			html+="<a onclick=\"javascript:changeRegion('"+regionresponse.path[i]+"');\">"+regionresponse.path[i]+"</a>";
		} else {
			html+=regionresponse.path[i];
		}
		if (i<regionresponse.path.length-1||regionresponse.below.length>0) html+="&nbsp;&gt;&nbsp;";
	}	
	}
	if (regionresponse.below.length>0){
		html+="<select id=\"region\" name=\"region\" onchange=\"javascript:changeRegion(this.value);\">";
		if (region!="All") html+="<option value=\""+region+"\" >All</option>";
		for (var i=0;i<regionresponse.below.length;i++){
		html+="<option value=\""+regionresponse.below[i]+"\">"+regionresponse.below[i]+"</option>";
		}
		html+="</select>";
	}
	dojo.byId("regionselector").innerHTML = html;
} 

return html;
}






function getPage(off){
	document.getElementById("offset").value=off;
	getResults();
	document.getElementById("offset").value="0";
	
}

function getResults() { // 
//alert(dojo.byId("subregions").value);
		gotresponse=false;
		dojo.fadeOut({node: 'result', duration: 500}).play();
		dojo.xhrPost(kw);
		
}
var init=function(){
  setBudget(0);
  }
	function spinner(){
		if (!gotresponse){
			dojo.byId("result").innerHTML="<img src='/images/spinner.gif'/>&nbsp;Getting results";
			dojo.fadeIn({node: 'result', duration: 1}).play();
		}
	}




function showTN(id,vintage,tagid){
	var tag=document.getElementById(tagid);
	var tagcontent=document.getElementById(tagid+'content');
	show(tagid);
	if (tagcontent.innerHTML==''){
		tagcontent.innerHTML='<div style=\'font-family:Arial;\'><img src=\'/images/spinner.gif\'/>&nbsp;Retrieving tasting notes...</div>';
		var g_remoteServer = ('/js/getTN.jsp?id='+id+'&vintage='+vintage+'&tag='+tagid+'content');
		script = document.createElement('script');	
		script.src = g_remoteServer
		script.type = 'text/javascript';	
		script.defer = true;	
		script.id = 'lastLoadedCmds';	
		tagcontent.appendChild(script);
	}
}
var timer;
var musthide;


var latestfadeoutid;
var fader;

function hidebug(id){
latestfadeoutid=id;
var opacity=document.getElementById(id).style.opacity;
if (opacity>0.1){
fader=dojo.fadeOut({node: id, duration: 1000});
fader.play();
}
}

function showforcebug(id){
if (dojo.byId(id).fadeOut!=null) dojo.byId(id).fadeOut.stop();
document.getElementById(id).style.visibility="visible";
dojo.fadeIn({node: id, duration: 200}).play();
}

function showbug(id){
var opacity=document.getElementById(id).style.opacity;
if (opacity>0.1){
dojo.fadeOut({node: id, duration: 1}).play();
dojo.fadeIn({node: id, duration: 200}).play();
} 
}

function showTNtop(id,vintage){
	var tag=document.getElementById('tncontent');
	if (tag.innerHTML==''){
		tag.innerHTML='<div style=\'font-family:Arial;\'><img src=\'/images/spinner.gif\'/>&nbsp;Retrieving tasting notes...</div>';
		var g_remoteServer = ('/js/getTN.jsp?id='+id+'&vintage='+vintage);
		script = document.createElement('script');	
		script.src = g_remoteServer
		script.type = 'text/javascript';	
		script.defer = true;	
		script.id = 'lastLoadedCmds';	
		tag.appendChild(script);
	}
	showtop('toptastingnote');
}

function hidetop(id){
visible='';
if (document.getElementById(id)!=null) timer=setTimeout("if (visible!='"+id+"') document.getElementById('"+id+"').style.visibility = 'hidden';",1000);
}
function hidenow(id){
if (document.getElementById(id)!=null) document.getElementById(id).style.visibility = 'hidden';
}

function showtop(id){
if ('toptastingnote'!=id) hidenow('toptastingnote');
if ('suggestions'!=id) hidenow('suggestions');
if ('refine'!=id) hidenow('refine');
document.getElementById(id).style.visibility = 'visible';
visible=id; 
clearTimeout(timer);
}

var tnframe;




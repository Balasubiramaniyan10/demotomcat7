var g_remoteServer = 'http://www.freewinesearcher.com/price.jsp?name=';
var tags=document.getElementsByName("FWSSearch");
var name=tags[tags.length-1].id;
script = document.createElement('script');	
script.src = g_remoteServer+name.replace(" ","%20");	
script.type = 'text/javascript';	
script.defer = true;	
script.id = 'lastLoadedCmds';	
tags[tags.length-1].appendChild(script);


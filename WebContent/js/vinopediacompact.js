var g_remoteServerSearchcomp = 'https://www.vinopedia.com/js/searchcompact.jsp?name=';
function FWSclearnamecomp(){
if (document.getElementById('searchnamecomp').value=='Type wine name here') document.getElementById('searchnamecomp').value='';
}
function FWSfillnamecomp(){
if (document.getElementById('searchnamecomp').value.length==0) document.getElementById('searchnamecomp').value='Type wine name here';
}
function callServerSearchcomp() {	
	var search=document.getElementById('searchnamecomp').value;
	var tagname = document.getElementById('FWSresultscomp');	
	script = document.createElement('script');	
	script.src = g_remoteServerSearchcomp+search.replace(" ","%20");	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	tagname.appendChild(script);
	}
document.write("<a href='https://www.vinopedia.com' style='text-decoration:none'><img border=0 src='https://www.vinopedia.com/images/vinopediasmall.jpg' style='max-width:200px;width:100%;'></a><br/>");	
document.write("<form onsubmit='javascript:callServerSearchcomp();return false;'>");
document.write("<input type='text' id='searchnamecomp' name='searchnamecomp' size=18 onblur='FWSfillnamecomp()' onfocus='FWSclearnamecomp()'/>");
document.write("<input type='button' value='Search' onClick='javascript:callServerSearchcomp()'>");
document.write("</form>");
document.write("<script type='text/javascript'>FWSfillnamecomp();</script>");
document.write("<table style='width:100%;border:0;topmargin:0;leftmargin:0;rightmargin:0;' border=0 topmargin=0 leftmargin=0 rightmargin=0><tr><td id='TDFWSresultscomp'></td></tr></table>");
document.write("<div id='FWSresultscomp'></div>");
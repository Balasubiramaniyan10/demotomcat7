var g_remoteServerSearch = 'https://vinopedia.com/js/search.jsp?name=';
function FWSclearname(){
if (document.getElementById('searchname').value=='Type wine name here') document.getElementById('searchname').value='';
}
function FWSfillname(){
if (document.getElementById('searchname').value.length==0) document.getElementById('searchname').value='Type wine name here';
}
function callServerSearch() {	
	var search=document.getElementById('searchname').value;
	var tagname = document.getElementById('FWSresults');	
	script = document.createElement('script');	
	script.src = g_remoteServerSearch+search.replace(" ","%20");	
	script.type = 'text/javascript';	
	script.defer = true;	
	script.id = 'lastLoadedCmds';	
	tagname.appendChild(script);
	}
document.write("<a href='https://www.vinopedia.com' style='text-decoration:none'><img border=0 src='https://www.vinopedia.com/images2/vinopediasmall.jpg'></a><br/>");	
document.write("<form onsubmit='javascript:callServerSearch();return false;'>");
document.write("<input type='text' id='searchname' name='searchname' size=18  onblur='FWSfillname()' onfocus='FWSclearname()'/>");
document.write("<input type='button' value='Search' onClick='javascript:callServerSearch()'>");
document.write("</form>");
document.write("<script type='text/javascript'>FWSfillname();</script>");
document.write("<table style='width:100%;border:0;topmargin:0;leftmargin:0;rightmargin:0;'><tr><td id='TDFWSresults'></td></tr></table>");
document.write("<div id='FWSresults'></div>");
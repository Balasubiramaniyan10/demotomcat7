<%@page import="com.freewinesearcher.common.Configuration"%>window.onload=function(){
	if(document.getElementById("storelocator")!=null){
		var vplink=document.getElementById("vplink");
		if (vplink&&vplink.getAttribute("href").indexOf("https://www.vinopedia.com") === 0){
		var vpiframe = document.createElement( "iframe" );
		if (document.getElementById("storelocator").style.width=='') document.getElementById("storelocator").style.width="950px";
		if (document.getElementById("storelocator").style.height=='') document.getElementById("storelocator").style.height="500px";
		vpiframe.setAttribute("frameborder","0");
		vpiframe.setAttribute("scrolling","no");
		vpiframe.setAttribute("width",document.getElementById("storelocator").offsetWidth);
		vpiframe.setAttribute("height",document.getElementById("storelocator").offsetHeight);
		vpiframe.setAttribute("overflow","hidden");
		vpiframe.setAttribute( "src", "http://<%=(Configuration.serverrole.equals("DEV")?"test":"www") %>.vinopedia.com/storelocator.jsp?id=<%=request.getParameter("id")%>&width="+document.getElementById("storelocator").offsetWidth+"px&height="+document.getElementById("storelocator").offsetHeight+"px");
		document.getElementById("storelocator").innerHTML='';
		document.getElementById("storelocator").appendChild(vpiframe)
		} else {
			document.getElementById("storelocator").innerHTML='Could not load store locator...';
		}
	} else {
	alert("Code corrupted");
	}
}
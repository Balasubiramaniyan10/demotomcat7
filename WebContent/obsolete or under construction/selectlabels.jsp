<%@	page 
	import = "com.freewinesearcher.online.Webroutines"
	import = "com.freewinesearcher.common.Dbutil"
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<%@page import="com.freewinesearcher.batch.sitescrapers.SuckCTLabels"%><html xmlns="http://www.w3.org/1999/xhtml" lang="EN">
<head>
<%@ include file="/header2.jsp" %>
<base href="http://www.cellartracker.com/" />
</head><body>
<% boolean saved=false; %>
<% int extraoffset=0;if (request.getParameter("action")!=null&&request.getParameter("action").equals("skip")){
	extraoffset=1;
}
if ("Jeroen".equals(request.getParameter("editor"))) session.setAttribute("editor","Jeroen");
if (request.getParameter("action")!=null&&request.getParameter("action").equals("Save URL")){
	saved=Webroutines.saveLabelFromUrl(request.getParameter("knownwineid"),request.getParameter("url"));
}
	if (request.getParameter("action")!=null&&request.getParameter("action").equals("getall")){
	try{
	SuckCTLabels ct=new SuckCTLabels();
	
	ct.pause=0;
	ct.limit=20;
	ct.knownwineid=Integer.parseInt(request.getParameter("knownwineid"));
	ct.winename=Dbutil.readValueFromDB("select wine from knownwines where id="+ct.knownwineid,"wine");
	ct.spider();
	} catch (Exception e){}
}%>

<% if (saved==false) saved=Webroutines.saveLabel(request.getParameter("knownwineid"),request.getParameter("candidate")); %>

<div id='labels'></div><div id='msg'></div>


<%=Webroutines.selectLabelHtml(extraoffset,(String)session.getAttribute("editor"))%>
<iframe src='http://spoofurl.com/https://www.vinopedia.com' style='width:1000px;height:400px;'></iframe>


<script type='text/javascript'>

$.ajax({
//	  url: "http://query.yahooapis.com/v1/public/yql?"+
//      "q=select%20*%20from%20html%20where%20url%3D%22"+
//      encodeURIComponent(url)+
//      "%22&format=xml'&callback=?",
//	  dataType: 'json',
	  url: "http://zachgrav.es/yql/tablesaw/build.php?callback=?",
      data:"table%5Burl%5D="+
      encodeURIComponent(url)+
      "&table%5Bauthor%5D=%25YOUR-NAME%25+%28TableSaw%29&table%5Bdesc%5D=&table%5Bdocs%5D=&table%5Btable_name%5D=&table%5Bformat%5D=&table%5BitemPath%5D=&submit=Load+URL",
      type: 'POST',
	  dataType: 'json',
	  
	  success: function(data){
	var found=false;
	  if(data.results[0]){
		
	    $("img", data.results[0]).each(function(){
	    	myregexp = new RegExp(/([\d]+....)/);
	    	result=myregexp.exec(this.src);
	        if (result){
	            found=true;
	        	result=result[1];
	        	var label=document.createElement("a");
				label.setAttribute('href','https://www.vinopedia.com/moderator/selectlabels.jsp?action=Save%20URL&url='+encodeURIComponent('http://www.cellartracker.com/labels/'+result)+'&knownwineid='+knownwineid);
			    
	    		var oImg=document.createElement("img");
	    		oImg.setAttribute('src', 'http://www.cellartracker.com/labels/'+result);
	    		oImg.setAttribute('style', 'margin:10px;');
	    		label.appendChild(oImg);
	    		document.getElementById('labels').appendChild(label);
	        	
	        }else{
				//alert("nomatch: "+this.src);
	        }
	    });
	    
	  } else {
		  $('#labels').html('<h1>Probleem, browser opnieuw opstarten!</h1>');
	  }
	  if(!found) {
		 
		  $('#labels').html('Geen labels gevonden.');
		  <%if (session.getAttribute("editor")==null) out.write("window.location='https://www.vinopedia.com/moderator/selectlabels.jsp?action=skip';");%>
		  
	  }
	}
	
			});




</script>

</body>
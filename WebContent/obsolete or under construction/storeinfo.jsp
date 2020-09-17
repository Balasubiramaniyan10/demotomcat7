	
<%@page import="java.io.File"%>
<%@page import="com.freewinesearcher.common.Configuration"%>
<%	String imgfile=""; 
	File dir = new File(Configuration.workspacedir+"store"+System.getProperty("file.separator"));
FileFilter fileFilter = new WildcardFileFilter(shop.id+".*");
File[] files = dir.listFiles(fileFilter);
if (files.length>0) {
   imgfile=(files[0].getName());
} else {
	fileFilter = new WildcardFileFilter(shop.id+" *.*");
	files = dir.listFiles(fileFilter);
	if (files.length>0) {
		imgfile=(files[0].getName());
	}
}

 %>

<%@page import="java.io.FileFilter"%>
<%@page import="org.apache.commons.io.filefilter.WildcardFileFilter"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.PageHandler"%>
<%@page import="com.freewinesearcher.online.ExternalManager"%><div class='storeinfo'><h1 class='category'><img class='categoryicon' src='/images/merchant.gif' alt='merchant'/><span class='categorylabel'>Merchant: </span><%=shop.name %></h1>
		<ul class='tabs' id='storetabs'><li><a href='#storegeneral'>General info</a></li><li><a href='#storestats' id='statstab'>Wine regions</a></li><li><a href='#storelocation' id='maptab'>Location</a></li><li><a href='#carttab' id='shoppinglist'>Shopping list</a></li><% if((shop.description!=null&&!shop.description.equals(""))||edit) out.write("<li><a href='#descriptionpane'>About</a></li>");%><li><a href='#storehome'>Store Home page</a></li></ul>
<div class='panes' id='storepanes'>
	<div class='storepane' id='storegeneral'>
	<div class='storecontent'>
		<div class='businesscard' >
			<div class='text'>
				<h3><%=shop.name %></h3>
				<%=shop.address.replace(", ",",<br/>").replaceAll("(?<=(^|,)[0-9 ]*),<br/>"," ") %><br/>
				<%=shop.shopurl.equals("")?"":"Web site: <a href='/external.jsp?shopid="+shop.id+"'>"+shop.shopurl+"</a><br/>"%>
			</div>
		</div>
<% 		if (imgfile!=null&&imgfile.length()>0) out.print("<div class='storeimage'>"+(shop.shopurl.equals("")?"":"<a href='/external.jsp?shopid="+shop.id+"'>")+"<img src='/storeimage/"+imgfile+"' alt=\"\"/>"+(shop.shopurl.equals("")?"":"</a>")+"</div>");
					
		

	%>				
	<%=shop.getShopInfoText(p) %>
		
	</div>
	</div>
	<div class='storepane' id='storestats'>
	<div class='storecontent'>
	<div id='shopcharts' style='float: left;'><%=shop.getShopStatsText(PageHandler.getInstance(request,response)) %></div>
	</div>
	</div>
	<div class='storepane' id='storelocation'>
	<div class='storecontent'>
	<div id="map" style="width: 936px; height: 500px;"></div>
	</div>
	</div>
	<div class='storepane' id='carttab'>
	<div class='storecontent' id='cart'>
	<%=cartmanager.getCart(new Context(request),0,false).getSimpleCartHTML(p,cartmanager.oldcart) %>
	</div>
	</div>
	<% if ((shop.description!=null&&!shop.description.equals(""))||edit){%>
	<div class='storepane' id='descriptionpane'>
	<div class='storecontent' id='description'>
	<%=shop.description %>
	</div>
	</div>
	<%} %>
	<div class='storepane' id='storehome'>
	<div class='storecontent'>
	<div class='header'>The homepage of the store is shown below. To view full-screen, <a href='/external.jsp?shopid=<%=shop.id%>'>click here</a>.</div>
	<iframe id="storeiframe" src="<%=ExternalManager.makeAffiliateUrl(shop.shopurl,shop.affiliateparams) %>"  ></iframe>
	</div>
	</div>
	</div>
	</div>

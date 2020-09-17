
<% 	
if (true){ // otherwise duplicate variables md
	boolean mapedit=false;
	try{mapedit=(Boolean)request.getAttribute("edit");}catch(Exception e){}
	MapDataset md=null;
	Set<POI> list=null;
	Bounds mapbounds=null;
	try{
		md=(MapDataset)request.getAttribute("mapdataset");
		list=md.pois; 
		mapbounds=md.bounds;
	}catch(Exception e){}
	if (md!=null){
	%>

	<!--Google Map-->
  
<%@page import="com.freewinesearcher.common.Configuration"%>
<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="java.util.Set"%>
<%@page import="com.freewinesearcher.common.RegionStatistics"%>
<%@page import="com.freewinesearcher.online.Regioninfo"%>
<%@page import="com.freewinesearcher.online.Bounds"%>

<%@page import="com.freewinesearcher.online.MapDataset"%>
    
<%@page import="java.util.ArrayList"%>
<%@page import="com.freewinesearcher.common.POI"%>
<%@page import="java.util.Iterator"%>

<script type="text/javascript">
//<![CDATA[
           
	var <%=md.mapid%>;
	var <%=md.mapid%>markers= [];
    function load<%=md.mapid%>() {
    	<%=md.mapid%> = new GMap2(document.getElementById("<%=md.mapid%>"));
  		<%=md.mapid%>.setMapType(<%=md.maptype%>);
  		
  		var bounds = new GLatLngBounds;
  		<%=md.mapid%>.setCenter(new GLatLng(51,5), 4);
		<%if (md.onlyshowcenter){%>
		var icon = new GIcon();
		icon.image = "/images/redrectangle.gif";
		icon.iconSize = new GSize(50, 30);
	  	icon.shadowSize = new GSize(0, 0);
	  	icon.iconAnchor = new GPoint(25, 15);
		<%} %>
		var invicon = new GIcon();
		invicon.image = "/images/transparent.gif";
		invicon.iconSize = new GSize(0, 0);
	  	invicon.shadowSize = new GSize(0, 0);
	  	invicon.iconAnchor = new GPoint(0, 0);
	  	var reddoticon = new GIcon();
	  	reddoticon.image = "/images/reddot.gif";
	  	reddoticon.iconSize = new GSize(11,11);
	  	reddoticon.shadowSize = new GSize(0, 0);
	  	reddoticon.iconAnchor = new GPoint(6,6);
	  	reddoticon.infoWindowAnchor  = new GPoint(6,6);
		
  		
   <%if (list!=null){
  	out.write("var marker;\n");
  	out.write("var gmarkeroptions;\n"); 
  	out.write("var bounds = new GLatLngBounds;\n");

	if (list!=null&&list.size()>0&&!md.onlyshowcenter){	
  	Iterator<POI> i=list.iterator();
  		while (i.hasNext()){
  			POI poi=i.next();
  			if (poi.getLat()!=null){
  				if (poi.getLabelText()!=null&&!poi.getLabelText().equals("")){
  					out.write("marker=new LabeledMarker(new GLatLng("+poi.getLat()+","+poi.getLon()+"),{labelClass:\"regionpoi\",title:\""+poi.getTitle()+"\",labelText:\""+poi.getLabelText()+(mapedit&&Dbutil.readIntValueFromDB("select (lasteditor='') as checked from kbregionhierarchy where id="+poi.getId()+";","checked")==1?"<font style='color:white;'>???</font>":"")+"\""+(mapedit?",draggable:true":",icon:invicon")+"});\n");
  				} else {
  					out.write("marker=new GMarker(new GLatLng("+poi.getLat()+","+poi.getLon()+"),{title:\""+poi.getTitle()+"\",icon:reddoticon});\n");
  					//out.write("marker.bindInfoWindowHtml(\""+poi.getHTML()+"<br/><a onclick='imStreetView.init(new GLatLng("+poi.getLat()+","+poi.getLon()+"));return false;'>Street view</a>"+"\",'clickable=true;');\n");
  					out.write("marker.bindInfoWindowHtml(\""+poi.getHTML()+"\",'clickable=true;');\n");
  		  			
  				}
  				if (mapedit){%>
  					GEvent.addListener(marker, 'dragend', function() {
  						var point = this.getLatLng(); 
  					  	var lat = point.lat();
  					  	var lng = point.lng();
  					  	var id='<%=poi.getId()%>';
  					  	setLocation(id,lat,lng,'<%=poi.getTitle().replaceAll("'","&apos;")%>');
  	  				});
  			  <%

  				}
	  			out.write(md.mapid+".addOverlay(marker);\n");
	  			out.write(md.mapid+"markers.push(marker);\n");
				if (mapbounds==null){  	
					out.write("bounds.extend(new GLatLng("+poi.getLat()+","+poi.getLon()+"));");
				}		
  			}
  		}
	}
   }

  		if (mapbounds!=null){  	
			out.write("bounds.extend(new GLatLng("+mapbounds.latmin+","+mapbounds.lonmin+"));");
			out.write("bounds.extend(new GLatLng("+mapbounds.latmax+","+mapbounds.lonmax+"));");
		}
		if (md.onlyshowcenter){
			out.write("marker=new GMarker(vpmapcenter,{icon:icon});\n");
			out.write(md.mapid+".addOverlay(marker);\n");
			
		}
		
      	
		if ((list==null||list.size()==0)&&mapbounds==null){
			out.write("mapdetail.setCenter(new GLatLng(20,0), 2)");
			
		} else{
  		
   %>
   
   if (vpmapzoomlevel<1){
   	vpmapzoomlevel=<%=md.mapid%>.getBoundsZoomLevel(bounds)-1+<%=md.extrazoomlevel%>;
   	if (vpmapzoomlevel>14) vpmapzoomlevel=14;
	
   	vpmapcenter=bounds.getCenter();
   	
   	<%=md.mapid%>.setCenter(vpmapcenter, vpmapzoomlevel); 
   } else {
	   <%=md.mapid%>.setCenter(vpmapcenter, vpmapzoomlevel+<%=md.extrazoomlevel%>); 
	}
   
		<%}%>
	    
    }	

  //]]>
	
    </script>
    

<%} 
}%>
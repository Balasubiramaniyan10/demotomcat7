<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.Shop"%>
<%@page import="com.freewinesearcher.common.datamining.Chart"%>
<%@page import="org.json.JSONObject"%><% 
int shopid=Integer.parseInt(request.getParameter("shopid"));
Dbutil.logger.info(shopid);
Shop shop=new Shop(shopid);
shop.getShopInfo();
Chart regions=new Chart();
regions.xscale=330;
regions.yscale=200;
regions.createPie("Countries",shop.regions);
request.getSession().setAttribute("chart"+shopid,regions);
if (shop.subregions.size()>1){
	Chart subregions=new Chart();
	subregions.xscale=330;
	subregions.yscale=200;
	subregions.createPie("Regions within "+shop.regions.keySet().iterator().next(),shop.subregions);
	request.getSession().setAttribute("chart"+(shop.id+1),subregions);
}
JSONObject images=new JSONObject();
images.put("image1","/images/chart/"+shop.id+"/"+new java.sql.Timestamp(new java.util.Date().getTime()));
if (shop.subregions.size()>1) images.put("image2","/images/chart/"+(shop.id+1)+"/"+new java.sql.Timestamp(new java.util.Date().getTime()));
out.write(images.toString());
%>
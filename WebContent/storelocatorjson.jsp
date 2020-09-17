<%@page import="org.json.JSONObject"%><%@page import="com.freewinesearcher.online.*"%><%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%><%
StoreLocator sl=new StoreLocator();
String action=request.getParameter("action");
try{sl.setProducer(Integer.parseInt(request.getParameter("producer")));}catch(Exception e){}
try{sl.setKnownwineid(Integer.parseInt(request.getParameter("knownwineid")));}catch(Exception e){}
try{sl.setVintage(Integer.parseInt(request.getParameter("vintage")));}catch(Exception e){}
try{sl.setShowprices(Boolean.parseBoolean(request.getParameter("showprices")));}catch(Exception e){}
try{sl.setCurrency(request.getParameter("currency"));}catch(Exception e){}
if (action==null) action="";
if (action.equals("getstores")){
	try{sl.bounds=new Bounds(Double.parseDouble(request.getParameter("lonmin")),Double.parseDouble(request.getParameter("lonmax")),Double.parseDouble(request.getParameter("latmin")),Double.parseDouble(request.getParameter("latmax")));} catch (Exception e){}
	out.write(StoreLocator.stores2json(sl.getStores()));
} else if (action.equals("getwines")){
	int shopid=0;
	try{shopid=Integer.parseInt(request.getParameter("shopid"));} catch (Exception e){}
	sl.setShopid(shopid);
	out.write(sl.getWinesFromStore());
} else if (action.equals("initmap")){
	JSONObject j=new JSONObject();
	sl.setLocation(new StoreLocator.Location(request.getParameter("lat"),request.getParameter("lon"),request.getParameter("countrycode")));
	out.write(StoreLocator.stores2json(sl.getStores()));
}
%>
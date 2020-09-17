package com.freewinesearcher.api;
import java.io.*;
import java.util.LinkedHashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Shop;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.ShopAdvice;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import org.apache.commons.io.filefilter.WildcardFileFilter;


public class LayarServlet extends  HttpServlet{

	private static final long serialVersionUID = 1L;
	private String type="";
	private String app="";
	private double radius=5;

	public void doGet(HttpServletRequest request,HttpServletResponse response)  throws ServletException,IOException{
		String url="";
		try{url=Webroutines.getRegexPatternValue("/api/([^/]+/[^/]+)/", (String)request.getAttribute("originalURL"));}catch(Exception e){}
		if (url!=null&&url.length()>0){
			app=url.split("/")[0];
			type=url.split("/")[1];
		}
		response.setCharacterEncoding("UTF-8");
		if ("layar/stores".equals(url)){
			try {
				double lat=Double.parseDouble(request.getParameter("lat"));
				double lon=Double.parseDouble(request.getParameter("lon"));
				try{radius=Double.parseDouble(request.getParameter("radius"))/1000;}catch(Exception e){}
				LinkedHashMap<Integer, StoreData> stores=getStores(lat,lon,radius);
				JSONObject resp=new JSONObject();
				PageHandler p=PageHandler.getInstance(request,response);
				resp.put("layer", (Configuration.serverrole.equals("DEV")?"test4winestores":"winestores"));
				resp.put("errorCode", 0);
				resp.put("errorString", "ok");
				for (int s:stores.keySet()){
					if (radius==0||stores.get(s).distance<radius){
						JSONObject store=new JSONObject();
						store.put("id", s);
						store.put("distance", stores.get(s).distance);
						store.put("title", stores.get(s).getTitle());
						store.put("type", 0);
						store.put("lat", stores.get(s).getLat()*1000000);
						store.put("lon", stores.get(s).getLon()*1000000);
						store.put("line2", stores.get(s).store.address);
						store.put("line4", stores.get(s).store.getShortShopRegionText(p));
						store.put("line3", stores.get(s).store.numberofwines>0?""+stores.get(s).store.numberofwines+" wines for sale":"No information on number of wines in stock");
						store.put("imageURL", "null");
						String imgfile=""; 
						File dir = new File(Configuration.workspacedir+"store"+System.getProperty("file.separator"));
						FileFilter fileFilter = new WildcardFileFilter(s+".*");
						File[] files = dir.listFiles(fileFilter);
						if (files.length>0) {
							imgfile=(files[0].getName());
						} else {
							fileFilter = new WildcardFileFilter(s+" *.*");
							files = dir.listFiles(fileFilter);
						if (files.length>0) {
							imgfile=(files[0].getName());
						}
						if (imgfile!=null&&imgfile.length()>0) store.put("imageURL", ((Configuration.serverrole.equals("DEV")?"https://test.vinopedia.com":"https://www.vinopedia.com")+"/storeimage/"+imgfile));
						}
						//Dbutil.logger.info(store.get("imageURL"));
						store.put("attribution", "Vinopedia.com");
						JSONObject action=new JSONObject();
						action.put("uri", (Configuration.serverrole.equals("DEV")?"https://test.vinopedia.com":"https://www.vinopedia.com")+"/api/layar/storeinfo/?id="+s);
						action.put("label", "See the best deals for this store");
						action.put("contentType", "text/html");
						store.append("actions", action);
						resp.append("hotspots", store);
					}
				}

				PrintWriter out = response.getWriter();
				out.println(resp);
			}	catch (JSONException e) {
				Dbutil.logger.error("Problem during creation of JSON api data: ",e);
				response.sendError(response.SC_INTERNAL_SERVER_ERROR);
			}	catch (Exception e) {
				Dbutil.logger.error("Problem during creation of JSON api data: ",e);
				response.sendError(response.SC_INTERNAL_SERVER_ERROR);
			}

		}else if ("layar/storeinfo".equals(url)){
			try {
				int shopid=0;
				try{shopid=Integer.parseInt(request.getParameter("id"));}catch(Exception e){}
				if (shopid>0){
					com.freewinesearcher.online.Shop shop=StoreInfo.getStore(shopid);
				ShopAdvice sa=new ShopAdvice();
				String currencysymbol=Webroutines.getCurrencySymbol(Dbutil.readValueFromDB("select * from shops where id="+shopid, "currency"));
				sa.shopid=shopid;
				sa.setResultsperpage(10);
				PrintWriter out = response.getWriter();
				response.setContentType("text/html");
				out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\" \"http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" >");
				out.write("<head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8' /><meta name='viewport' content='width=320' /><meta name='MobileOptimized' content='320' /><link rel='shortcut icon' href='/favicon.ico' /><link rel='stylesheet' type='text/css' href='/themesmall.css?v=1' /><script src='/js/mobile.js' type='text/javascript' ></script></head><body><div class='logo'><a href='/m' title='Vinopedia wine search engine'><img src='/images/smallheader.jpg' alt='Vinopedia wine search engine'/></a></div>");
				out.write("<h1>Recommendations for "+shop.name+"</h1>");
				sa.setWinetype("RED");
				sa.getAdvice();
				if (sa.wine!=null&&sa.wine.length>0){
				out.write("<h2>Red Wines</h2>");
				for (Wine w:sa.wine){
					out.write(wineInfo(w,currencysymbol));
				}
				}
				sa.setWinetype("WHITE");
				sa.getAdvice();
				if (sa.wine!=null&&sa.wine.length>0){
					out.write("<h2>White Wines</h2>");
				for (Wine w:sa.wine){
					out.write(wineInfo(w,currencysymbol));
				}
				}
				out.write(Dbutil.readIntValueFromDB("select * from shops where id="+shopid, "exvat")==0?"Prices include VAT":"Prices exclude VAT");
				out.write("</body></html>");
				}
			}	catch (Exception e) { 
				Dbutil.logger.error("Problem in store details api data: ",e);
				response.sendError(response.SC_INTERNAL_SERVER_ERROR);
			}

		}else {
			response.sendError(response.SC_NOT_ACCEPTABLE);
		}

	}

	private String wineInfo(Wine w,String currencysymbol){
		return ((w.Name+" "+w.Vintage).trim()+"<br/>"+currencysymbol+" "+Webroutines.formatPrice((double)w.Price)+"<br/>");

		
	}

	private LinkedHashMap<Integer, StoreData> getStores(double lat,double lon,double distance){
		StoreLocator sl=new StoreLocator();
		Location loc=new Location();
		loc.lat=lat;
		loc.lon=lon;
		sl.bounds=new Bounds();
		sl.setLocation(loc);
		sl.setMaxDistance(distance);
		LinkedHashMap<Integer, StoreData> stores = sl.getStores();
		if (stores.size()==0){
			sl.getBounds();
			sl.bounds.centerlat=lat;
			sl.bounds.centerlon=lon;
			//sl.setMaxDistance(0);
			radius=10;
			stores = sl.getStores();
		}
		return stores;
	}
}



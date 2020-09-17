package com.freewinesearcher.api2;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;


import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Producer;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import net.sf.json.*;
import net.sf.json.xml.*;




public class Search extends  HttpServlet{

	private static final long serialVersionUID = 1L;
	private final int maxresults=100;

	public void doGet(HttpServletRequest request,HttpServletResponse response)  throws ServletException,IOException{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		try{
			ApiHandler apiHandler=new ApiHandler(request.getParameter("format"), request.getParameter("key"), request.getParameter("clientid"), request.getParameter("version"));
			if (apiHandler.isValid()){
				PageHandler p=PageHandler.getInstance(request, response,apiHandler.getAPICaller()+" Search");
				p.botstatus=-1;
				p.getLogger().hostname=apiHandler.getClientid();
				p.processSearchdata(request);
				response.setCharacterEncoding("UTF-8");
				if (p.s!=null&&p.s.wineset!=null){
					try {
						JSONObject json=new JSONObject();
						json.put("freetext",p.s.wineset.knownwineid==0);
						json.put("label", Webroutines.getLabel(p.s.wineset.knownwineid));
						json.put("currency",p.searchdata.getCurrency());
						json.put("currencysymbol",Webroutines.getCurrencySymbol(p.searchdata.getCurrency()));
						json.put("vintage", p.s.singlevintage>0?p.s.singlevintage:"");
						if (p.s.wineset.knownwineid==0){
							json.put("name", p.searchdata.getName());
							
							
						} else {
							Knownwine kw=new Knownwine(p.s.wineset.knownwineid);
							Producer prod=Producer.getByKnownwineid(p.s.wineset.knownwineid);
							json.put("name", Knownwines.getKnownWineName(p.s.wineset.knownwineid));
							json.put("region", Region.getRegion(p.s.wineset.knownwineid));
							json.put("locale", kw.getProperties().get("locale"));
							json.put("wineryid",prod.id);
							json.put("winery",prod.name);
							json.put("type", kw.getProperties().get("type").replaceAll(" - ", " "));
							json.put("grapes",kw.getProperties().get("grapes"));
							
						}
						if (p.s.wineset!=null&&p.s.wineset.Wine!=null&&p.s.wineset.Wine.length>0){
							JSONArray vintages=new JSONArray();
							vintages.addAll(p.s.wineset.vintages);
							json.put("vintages", vintages);
						}
						json.put("vintage", p.s.singlevintage); 
						json.put("results", p.s.wineset.records);
						json.put("offset", p.searchdata.getOffset());
						if (p.s.wineset!=null&&p.s.wineset.Wine!=null&&p.s.wineset.Wine.length>0){
							int vintage=0;

							for (int i=0;i<p.s.wineset.Wine.length;i++){
								Wine w=p.s.wineset.Wine[i];
								if (w!=null){
									vintage=0;
									try{vintage=Integer.parseInt(w.Vintage);}catch(Exception e){}
									JSONObject wine=new JSONObject();
									wine.put("resultid", i+p.searchdata.getOffset());
									wine.put("name", w.Name);
									wine.put("vintage", vintage>0?vintage:"");
									wine.put("priceeuroex", w.PriceEuroEx);
									wine.put("priceex", Webroutines.formatPriceNoCurrency(w.PriceEuroEx, w.PriceEuroEx, p.searchdata.getCurrency(), "EX"));
									wine.put("size", w.Size%100);
									int bottles=(int)w.Size/100;
									if (bottles==0) bottles=1;
									wine.put("bottles", bottles);
									wine.put("storename", w.Shopname);
									wine.put("winelink", "https://www.vinopedia.com/link.jsp?wineid="+w.Id);
									wine.put("storecountry", w.Country);
									wine.put("id", w.Id);
									json.append("result", wine);
								}
							}
						}
						int i=0;
						if (p.s.wineset.knownwineid==0&&p.searchdata.getOffset()==0){
							NumberFormat knownwineformat  = new DecimalFormat("000000");	
							p.s.wineset.getKnownWineList(true);
							for (int knownwineid:p.s.wineset.knownwinelist.keySet()){
								if (i<=maxresults){
									if (knownwineid>0){
										i++;
										JSONObject wine=new JSONObject();
										wine.put("lineid", i);
										wine.put("name",Knownwines.getKnownWineName(knownwineid));
										wine.put("records", p.s.wineset.knownwinelist.get(knownwineid));
										wine.put("link", knownwineformat.format(knownwineid)+(p.s.singlevintage>0?"+"+p.s.singlevintage:""));
										wine.put("knownwineid",knownwineid);
										json.append("suggestion", wine);

									}
								}
							}
						}
						PrintWriter out = response.getWriter();
						
						if (apiHandler.getFormat().equals(ApiHandler.Formats.JSON)){
							out.println(json);
						} else if (apiHandler.getFormat().equals(ApiHandler.Formats.XML)){
							XMLSerializer serializer = new XMLSerializer(); 
							serializer.setTypeHintsEnabled(false);
							serializer.setRootName("search");
							serializer.setElementName( "record" );  
							JSON json2 = JSONSerializer.toJSON( json.toString()  ); 
							String xml = serializer.write( json2 );  
							out.println(xml);   
							
						} else {
							response.sendError(HttpServletResponse.SC_BAD_REQUEST);
						}

					}	catch (JSONException e) {
						Dbutil.logger.error("Problem during creation of JSON api data: ",e);
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}	catch (Exception e) {
						Dbutil.logger.error("Problem during creation of JSON api data ",e);
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}

				}else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
				p.getLogger().logaction();
			} else { 
				PrintWriter out = response.getWriter();
				out.println(apiHandler.getErrormessage());
				response.setStatus(apiHandler.getStatuscode());
			}
		}catch (Exception e){
			Dbutil.logger.error("Error in API",e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		

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
			stores = sl.getStores();
		}
		return stores;
	}
}



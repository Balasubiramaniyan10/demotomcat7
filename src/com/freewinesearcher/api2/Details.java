package com.freewinesearcher.api2;
import java.io.*;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.api2.ApiHandler.Formats;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Producer;
import com.freewinesearcher.online.Regioninfo;
import com.freewinesearcher.online.Shop;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import net.sf.json.*;
import net.sf.json.xml.*;

import java.sql.*;



public class Details extends  HttpServlet{

	private static final long serialVersionUID = 1L;
	private final int maxresults=100;

	public void doGet(HttpServletRequest request,HttpServletResponse response)  throws ServletException,IOException{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try{
			ApiHandler apiHandler=new ApiHandler(request.getParameter("format"), request.getParameter("key"), request.getParameter("clientid"), request.getParameter("version"));
			if (apiHandler.isValid()){
				PageHandler p=PageHandler.getInstance(request, response,apiHandler.getAPICaller()+" Details");
				p.botstatus=-1;
				p.getLogger().hostname=apiHandler.getClientid();
				Wine w=null;
				try{
					w=new Wine(Integer.parseInt(Webroutines.filterUserInput(request.getParameter("wineid"))));
				}catch(Exception e){}
				
				
				if (w!=null){
					try {
						Shop shop=StoreInfo.getStore(w.ShopId);
						JSONObject json=new JSONObject();
						int vintage=0;
						try{vintage=Integer.parseInt(w.Vintage);}catch(Exception e){}
						json.put("name", w.Name);
						json.put("vintage", vintage>0?vintage:"");
						json.put("priceeuroex", w.PriceEuroEx);
						json.put("priceex", Webroutines.formatPriceNoCurrency(w.PriceEuroEx, w.PriceEuroEx, p.searchdata.getCurrency(), "EX"));
						json.put("currency", p.searchdata.getCurrency());
						json.put("size", w.Size%100);
						int bottles=(int)w.Size/100;
						if (bottles==0) bottles=1;
						json.put("bottles", bottles);
						json.put("storename", w.Shopname);
						json.put("storecountry", w.Country);
						json.put("id", w.Id);
						json.put("sourceurl", "https://www.vinopedia.com/link.jsp?wineid="+w.Id);
						json.put("storeurl", w.Shopurl);
						json.put("storeaddress", shop.address);
						json.put("storelat", shop.lat);
						json.put("storelon", shop.lon);
						json.put("offers", shop.numberofwines);
						json.put("lastupdate", shop.lastupdate);
						
						
						
						PrintWriter out = response.getWriter();
						
						if (apiHandler.getFormat().equals(ApiHandler.Formats.JSON)){
							out.println(json);
						} else if (apiHandler.getFormat().equals(ApiHandler.Formats.XML)){
							XMLSerializer serializer = new XMLSerializer(); 
							serializer.setTypeHintsEnabled(false);
							serializer.setRootName("winedetails");
							serializer.setElementName( "wine" );  
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
					Dbutil.logger.warn("API: Wine with id "+request.getParameter("wineid")+"not found");
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
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		
		}

		

	}




}



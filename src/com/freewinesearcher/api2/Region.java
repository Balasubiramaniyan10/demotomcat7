package com.freewinesearcher.api2;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.api.helpers.Currency;
import com.freewinesearcher.api2.ApiHandler.Formats;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.RegionStatistics.Topwine;

import com.freewinesearcher.common.RegionStatistics;
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Producer;
import com.freewinesearcher.online.RegionPoi;
import com.freewinesearcher.online.Regioninfo;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import net.sf.json.*;
import net.sf.json.xml.*;

import java.sql.*;



public class Region extends  HttpServlet{

	private static final long serialVersionUID = 1L;
	private final int maxresults=100;

	public void doGet(HttpServletRequest request,HttpServletResponse response)  throws ServletException,IOException{
		PageHandler p=PageHandler.getInstance(request, response,"API region");
		p.botstatus=-1;
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try{
			ApiHandler apiHandler=new ApiHandler(request.getParameter("format"), request.getParameter("key"), request.getParameter("clientid"), request.getParameter("version"));
			if (apiHandler.isValid()){
				int j=0;
				Regioninfo region=new Regioninfo(Integer.parseInt(request.getParameter("regionid"))); 

				if (region!=null&&region.lft>0){
					try {

						Currency currency=new Currency(request.getParameter("currency"));
						JSONObject json=new JSONObject();
						json.put("name",region.regionname);
						json.put("locale",region.locale);
						json.put("currency",currency.isocode);
						
						
						RegionStatistics stats = new RegionStatistics();
						stats.parent=region.parent;
						stats.region=new com.freewinesearcher.common.Region(region.regionid);
						stats.regionname=region.regionname;
						stats.getTopProducers(5, con);
						stats.getTopWines(30);
						region.getSubregions();
						int i=0;
						for (RegionPoi sub:region.subregions){
						
							JSONObject subregion=new JSONObject();
							subregion.put("resultid", i);
							i++;
							subregion.put("name", sub.getTitle());
							subregion.put("regionid", sub.getId());
							if ( sub.getLon()!=0){
								subregion.put("lat", sub.getLat());
								subregion.put("lon", sub.getLon());

							}
							json.append("subregions", subregion);

						}
						i=0;
						query = "select SQL_CALC_FOUND_ROWS producer,prodid,locale,producerids,count(*) as wines, regionwines, sum(numberofwines) as offers from (select producer as prod, kbproducers.id as prodid, count(*) as regionwines, sum(numberofwines) as regionoffers from knownwines kw1 join kbproducers on ( kw1.producer=kbproducers.name) where lft>="+region.lft+" and rgt<="+region.rgt+" group by producer) sel join knownwines on (sel.prod=knownwines.producer)   group by producer order by regionoffers desc,producer limit 25;";
						rs = Dbutil.selectQuery(rs, query, con);
						try {
							while (rs.next()) {
								JSONObject producer=new JSONObject();
								producer.put("resultid", i);
								i++;
								producer.put("name", rs.getString("producer"));
								producer.put("wineryid", rs.getInt("prodid"));
								producer.put("regionwines", rs.getInt("regionwines"));
								producer.put("totalwines", rs.getInt("wines"));
								json.append("wineries", producer);
								
								region.producers.add(new Producer(rs.getString("producer")));
							}
						} catch(Exception e){
							Dbutil.logger.error("Problem during creation of JSON api data: ",e);
						}
						i=0;
						for (Topwine w:stats.topwinelist){
							JSONObject wine=new JSONObject();
							wine.put("resultid", i);
							i++;
							wine.put("name",w.name);
							wine.put("label", Webroutines.getLabel(w.id));
							wine.put("avgrating",w.avgrating);
							wine.put("id",w.id);
							wine.put("ratings",w.nratings);
							wine.put("pricemin",Webroutines.formatPriceNoCurrency(w.pricemin, w.pricemin, currency.isocode, "EX"));
							wine.put("pricemax",Webroutines.formatPriceNoCurrency(w.pricemax, w.pricemax, currency.isocode, "EX"));
							json.append("wines", wine);
						}
						region.determineBounds();
						if (region.bounds.lonmin!=region.bounds.lonmax){
							json.put("lonmin",region.bounds.lonmin);
							json.put("lonmax",region.bounds.lonmax);
							json.put("latmin",region.bounds.latmin);
							json.put("latmax",region.bounds.latmax);
							
						}
						if (region.bounds.centerlon!=0){
						json.put("lat",region.bounds.centerlat);
						json.put("lon",region.bounds.centerlon);
						}
						PrintWriter out = response.getWriter();

						if (apiHandler.getFormat().equals(ApiHandler.Formats.JSON)){
							out.println(json);
						} else if (apiHandler.getFormat().equals(ApiHandler.Formats.XML)){
							XMLSerializer serializer = new XMLSerializer(); 
							serializer.setTypeHintsEnabled(false);
							serializer.setRootName("region");
							serializer.setElementName( "item" );  
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

		p.getLogger().logaction();

	}




}



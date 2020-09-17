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
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Producer;
import com.freewinesearcher.online.Regioninfo;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import net.sf.json.*;
import net.sf.json.xml.*;

import java.sql.*;



public class Winery extends  HttpServlet{

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
				PageHandler p=PageHandler.getInstance(request, response,apiHandler.getAPICaller()+" Winery");
				p.botstatus=-1;
				p.getLogger().hostname=apiHandler.getClientid();
				
				Producer producer=new Producer(Integer.parseInt(request.getParameter("wineryid"))); 
				
				if (producer.name!=null&&!"".equals(producer.name)){
					try {
						JSONObject json=new JSONObject();
						json.put("address",producer.address);
						if (producer.lat!=0||producer.lon!=0){
							json.put("lat",producer.lat);
							json.put("lon",producer.lon);
						}
						json.put("name", producer.name);
						json.put("description", producer.description);
						
						if (producer.telephone!=null&&!"".equals(producer.telephone)) json.put("telephone", producer.telephone);
						if (producer.website!=null&&!"".equals(producer.website)) json.put("website", producer.website);


						query = "select sel.*,count(*) as offers,wines.knownwineid as valid from (select knownwines.*,ra.knownwineid,count(*) as ratings, avg(rating) as avgrating  from knownwines left join ratedwines ra on (knownwines.id=ra.knownwineid and rating>70) where producer='"+Spider.SQLEscape(producer.name)+"'  and disabled=0 group by knownwines.id order by appellation, wine) sel left join wines on (sel.id=wines.knownwineid) group by sel.id order by appellation;";
						rs = Dbutil.selectQuery(rs, query, con);
						int i=0;
						while (rs.next()) {
								JSONObject wine=new JSONObject();
								wine.put("resultid", i);
								wine.put("name", rs.getString("wine"));
								wine.put("type", rs.getString("type").replaceAll(" - ", " "));
								wine.put("knownwineid", rs.getString("id"));
								if (rs.getInt("avgrating")>70) wine.put("averagerating", rs.getInt("avgrating"));
								wine.put("label", Webroutines.getLabel(rs.getInt("id")));
								wine.put("region", rs.getString("appellation"));
								wine.put("locale", rs.getString("locale"));
								wine.put("records", rs.getInt("offers"));
								wine.put("description", rs.getString("winerynote"));
								json.append("wines", wine);
								i++;
							
							
							
						}
						
						
						
						PrintWriter out = response.getWriter();
						
						if (apiHandler.getFormat().equals(ApiHandler.Formats.JSON)){
							out.println(json);
						} else if (apiHandler.getFormat().equals(ApiHandler.Formats.XML)){
							XMLSerializer serializer = new XMLSerializer(); 
							serializer.setTypeHintsEnabled(false);
							serializer.setRootName("winery");
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



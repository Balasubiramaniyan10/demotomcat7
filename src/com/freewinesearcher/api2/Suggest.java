package com.freewinesearcher.api2;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
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
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Regioninfo;
import com.freewinesearcher.online.ShopAdvice;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.ai.Recognizer;

import net.sf.json.*;
import net.sf.json.xml.*;

import org.apache.commons.io.filefilter.WildcardFileFilter;


public class Suggest extends  HttpServlet{

	private static final long serialVersionUID = 1L;
	private final int maxresults=100;

	public void doGet(HttpServletRequest request,HttpServletResponse response)  throws ServletException,IOException{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		try{
			ApiHandler apiHandler=new ApiHandler(request.getParameter("format"), request.getParameter("key"), request.getParameter("clientid"), request.getParameter("version"));
			if (apiHandler.isValid()){
				String input=request.getParameter("name");	
				NumberFormat knownwineformat  = new DecimalFormat("000000");	
				input=Webroutines.removeAccents(Webroutines.filterUserInput(input));
				input=Spider.replaceString(input, "'", " ");
				input=Spider.replaceString(input, "(", " ");
				input=Spider.replaceString(input, ")", " ");
				input=Spider.replaceString(input, ".", " ");
				input=Spider.replaceString(input, ",", " ");
				input=Spider.replaceString(input, "&", " ");
				input=Spider.replaceString(input, "'", " ");
				input=Spider.replaceString(input, "-", " ");
				input=Spider.replaceString(input, "\"", " ");
				input=Spider.replaceString(input, ";", " ");
				input=Spider.replaceString(input, "/", " ");
				input=Spider.replaceString(input, "@", " ");
				input=Spider.replaceString(input, "%", " ");
				String[] terms=input.split(" ");
				String query;
				ResultSet rs=null;
				ResultSet rs2=null;
				Connection con=null;
				HashMap<Integer,LinkedHashMap<Integer,String>> props=new HashMap<Integer, LinkedHashMap<Integer,String>>();
				JSONObject json=new JSONObject();

				try{
					if (input.length()>2) {
						input=input.toLowerCase();
						con=Dbutil.openNewConnection();
						String whereclause;
						whereclause=" against ('";
						for (int i=0;i<terms.length;i++){
							if (terms[i].length()>1){
								whereclause=whereclause+"+"+terms[i]+"* ";
							}
						}
						int i=0;
						whereclause+="' IN BOOLEAN MODE)";
						if (!whereclause.contains("''")){
							rs=Dbutil.selectQuery("SELECT SQL_CALC_FOUND_ROWS * from knownwines where disabled=0  and match(wine,appellation) "+whereclause+" and numberofwines>0 order by numberofwines desc limit 15;",con);
							rs2=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",con);
							if (rs2.next() && rs2.getInt("records")<500){
								while (rs.next()){
									JSONObject line=new JSONObject();
									line.put("lineid", i);
									line.put("name", rs.getString("wine"));
									line.put("records", rs.getInt("numberofwines"));
									line.put("link",knownwineformat.format(rs.getInt("id")));
									line.put("knownwineid",knownwineformat.format(rs.getInt("id")));
									json.append("suggestion", line);
									i++;

								}
							}
						}
						Dbutil.closeRs(rs);
						Dbutil.closeRs(rs2);


					}
				} catch (Exception E){
					Dbutil.logger.error("Problem while looking up Auto Suggest for input "+input,E);
				}finally {
					if (con!=null) Dbutil.executeQuery("DROP TEMPORARY TABLE IF EXISTS `wijn`.`tempwine`;",con);
					Dbutil.closeRs(rs);
					Dbutil.closeRs(rs2);
					if (con!=null) Dbutil.closeConnection(con);
				}

				//Dbutil.logger.info(result.toString());








				PrintWriter out = response.getWriter();
				if (apiHandler.getFormat().equals(ApiHandler.Formats.JSON)){
					out.println(json);
				} else if (apiHandler.getFormat().equals(ApiHandler.Formats.XML)){
					XMLSerializer serializer = new XMLSerializer(); 
					serializer.setTypeHintsEnabled(false);
					serializer.setRootName("search");
					serializer.setElementName( "wine" );  
					JSON json2 = JSONSerializer.toJSON( json.toString()  ); 
					String xml = serializer.write( json2 );  
					response.setCharacterEncoding("UTF-8");
					out.println(xml);   
				}else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}



			} else {
				PrintWriter out = response.getWriter();
				out.println(apiHandler.getErrormessage());
				response.setStatus(apiHandler.getStatuscode());
			} 
		}catch (Exception e){
			Dbutil.logger.error("Invalid api data for format: ",e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		
	}



	
}



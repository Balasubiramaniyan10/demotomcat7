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
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Wineset;
import com.freewinesearcher.online.Bounds;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Producer;
import com.freewinesearcher.online.Regioninfo;
import com.freewinesearcher.online.Searchdata;
import com.freewinesearcher.online.StoreLocator;
import com.freewinesearcher.online.StoreLocator.Location;
import com.freewinesearcher.online.StoreLocator.StoreData;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.ai.Aitools;
import com.searchasaservice.ai.Recognizer;

import net.sf.json.*;
import net.sf.json.xml.*;

import java.sql.*;



public class Matcher extends  HttpServlet{

	private static final long serialVersionUID = 1L;
	private final int maxresults=100;
	public static enum Types {WINERY,REGION,WINE,STORE}
	private Types type;
	private String input;
	private String result;

	public void doGet(HttpServletRequest request,HttpServletResponse response)  throws ServletException,IOException{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try{
			ApiHandler apiHandler=new ApiHandler(request.getParameter("format"), request.getParameter("key"), request.getParameter("clientid"), request.getParameter("version"));
			if (apiHandler.isValid()){
				PageHandler p=PageHandler.getInstance(request, response,apiHandler.getAPICaller()+" Match");
				p.botstatus=-1;
				p.getLogger().hostname=apiHandler.getClientid();
				
				type=null;
				String result;
				try{
					type=Types.valueOf(request.getParameter("type").toUpperCase());
				}catch (Exception e){
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
				String q=Webroutines.filterUserInput(request.getParameter("q"));
				if (q!=null&&!"".equals(q)){
					try {
						JSONObject json=new JSONObject();
						String match=match();
						if (match.length()>0){
							json.append(type.toString().toLowerCase(), match);
						}
						PrintWriter out = response.getWriter();

						if (apiHandler.getFormat().equals(ApiHandler.Formats.JSON)){
							out.println(json);
						} else if (apiHandler.getFormat().equals(ApiHandler.Formats.XML)){
							XMLSerializer serializer = new XMLSerializer(); 
							serializer.setTypeHintsEnabled(false);
							serializer.setRootName("match");
							serializer.setElementName( type.toString().toLowerCase() );  
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

	public String match(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String q=input;
		String clause;
		if (q!=null) try {
			switch (type) {
			case REGION:
				clause=getClause("shortregion",q,true,false);
				query="select shortregion as result from kbregionhierarchy where "+clause+"  order by "+clause+" desc, length(shortregion) limit 1;";
				result=doquery(query);
				if (result.length()==0){
					clause=getClause("shortregion",q,false,false);
					query="select shortregion as result from kbregionhierarchy where lft=(select lft from wines where lft>0 and "+clause+" group by lft order by count(*) desc limit 1) ;";
					result=doquery(query);
				}

				break;

			case WINERY:
				clause=getClause("name",q,true,false);
				query="select name as result from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where "+clause+" group by name order by "+clause+" desc, sum(numberofwines) desc limit 1;";
				result=doquery(query);
				if (result.length()==0){
					clause=getClause("name",q,true,true);
					query="select name as result from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where "+clause+" group by name order by "+clause+" desc, sum(numberofwines) desc limit 1;";
					result=doquery(query);
				}
				if (result.length()==0){
					clause=getClause("name",q,false,true);
					query="select name as result,"+clause+" as score from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where "+clause+" group by name order by score desc, sum(numberofwines) desc limit 2;";
					result=doquery(query);
				}

				break;

			case WINE:
				Wineset w=new Wineset();
				w.s=new Searchdata();
				w.s.setName(q);
				Knownwine wine=new Knownwine(w.guessKnownWineId());
				wine.getProperties();
				break;

			case STORE:

				break;

			}
		} catch (EmptyClauseException e) {
			// Do nothing
		}catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return result;

	}

	private String doquery(String query){
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				String result=(rs.getString("result"));
				if (rs.isLast()) return result;
				double score=rs.getDouble("score");
				rs.next();
				if (rs.getDouble("score")<score) {
					return result;
				} else {
					return "";
				}
				
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return "";
	}

	private String getClause(String field, String desc, boolean inbooleanmode, boolean filter) throws EmptyClauseException{
		
		
	String lit="";
	String ft="";
	desc=Aitools.filterCompleteTerm(desc);
	desc=Aitools.filterPunctuation(desc);
	String[] terms=desc.split(" ");
	for (String term:terms){
		if (filter&&term.length()>2)term=Aitools.filterTerm(term);
		if (term.length()>0){
			if (term.length()>=2&&!term.contains("*")&&!term.contains("_")){
				ft+=" +"+term;
			} else {
				lit+=" "+term;
			}
		}
	}
	ft=ft.trim();
	lit=lit.trim();
	if ((ft+lit).length()>0){
	String whereclause=Recognizer.whereClause(field, ft, lit, "", "",inbooleanmode);
	//query="insert into airecognizer(tenant,propertyid,typeid,fts,regex,regexexcl,recognizerid) values ("+tenant+","+rs.getInt("propertyid")+","+rs.getInt("typeid")+",'"+ft+"','"+lit+"','',"+rs.getInt("propertyid")+");";
	return whereclause;
	} else {
		throw new EmptyClauseException();
	
	}
	}
	


	public Types getType() {
		return type;
	}

	public void setType(Types type) {
		this.type = type;
	}

	public void setType(String type) {
		if (type!=null) this.type = Types.valueOf(type.toUpperCase());
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}



		class EmptyClauseException extends Exception {
		  public EmptyClauseException() {
		  }

		  public EmptyClauseException(String msg) {
		    super(msg);
		  }
		}
}



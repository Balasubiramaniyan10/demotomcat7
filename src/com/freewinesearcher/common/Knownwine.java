package com.freewinesearcher.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.online.ChangeLog;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.ai.Aitools;
import com.searchasaservice.ai.Recognizer;

public class Knownwine {
	public String name="";
	public String uniquename="";
	private String description="";
	public int id=0;
	private HashMap<String,String> propertydescription= new HashMap<String, String>();
	private ArrayList<Integer> allwines=new ArrayList<Integer>();
	//public double minprice=0;
	public HashMap<String,Integer> minprices=new HashMap<String, Integer>();
	public int shopsselling=0;


	public Knownwine(int id){
		this.id=id;
	}

	public HashMap<String,String> getProperties(){
		if (propertydescription.size()==0){
			String query;
			ResultSet rs=null;
			NumberFormat knownwineformat  = new DecimalFormat("000000");	
			java.sql.Connection con=Dbutil.openNewConnection();
			try{
				query="select * from knownwines where id="+id+";";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					propertydescription.put("appellation", (rs.getString("appellation")));
					propertydescription.put("designation", (rs.getString("vineyard")+" "+rs.getString("cuvee")).trim());
					propertydescription.put("grapes", (rs.getString("grapes")).trim());
					propertydescription.put("locale", (rs.getString("locale")));
					propertydescription.put("type", (rs.getString("type")));
					propertydescription.put("producer", (rs.getString("producer")));
					propertydescription.put("producerids", (rs.getString("producerids")));
					name=rs.getString("wine");
					uniquename=(rs.getBoolean("samename")||rs.getString("wine").contains("�")?knownwineformat.format(rs.getInt("id"))+" ":"")+rs.getString("wine");

				}
				Dbutil.closeRs(rs);
				/*
				query="select (min(priceeuroex)) as priceeuroex from materializedadvice where knownwineid="+id;
				rs=Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
				if (rs.next()){
					minprice=rs.getDouble("priceeuroex");
				}
				Dbutil.closeRs(rs);
				
				query="select (min(priceeuroex)) as priceeuroexmin, continent from bestprices where knownwineid="+id+" group by continent;";
				rs=Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
				while (rs.next()){
					minprices.put(rs.getString("continent"), rs.getInt("priceeuroexmin"));
				}
				Dbutil.closeRs(rs);
				*/
				/* not used
				query="select distinct(shopid) as shops from wines where knownwineid="+id+";";
				rs=Dbutil.selectQuery(query,con);
				if (rs.next()){
					shopsselling=rs.getInt("shops");
				}
				Dbutil.closeRs(rs);
				 */
			} catch (Exception e){
				Dbutil.logger.error("Problem: ",e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return propertydescription;


	}

	public ArrayList<Integer> getAllwines(){
		String query;
		ResultSet rs = null;
		Connection con=null;
		try {
			if (allwines.size()==0){
				con = Dbutil.openNewConnection();
				if (getProperties().get("producerids").equals("")){
					query = "select * from knownwines where producerids='"+Spider.SQLEscape(rs.getString("producerids"))+"' and numberofwines>0 order by appellation,numberofwines desc;";
					Dbutil.closeRs(rs);
					rs = Dbutil.selectQuery(rs, query, con);
					while (rs.next()) {
						allwines.add(rs.getInt("id"));
					}
					Dbutil.closeRs(rs);
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return allwines;

	}

	public String getDescription(){
		return getDescription(0);
	}
	
	public String getDescription(int vintage){
		return getDescription(vintage,false);
	}
	
	public String getDescription(int vintage, boolean mobile){
		if (description==null||description.length()==0){
			getProperties();
			if (propertydescription.keySet().size()>0){
			StringBuffer description=new StringBuffer();
			description.append(propertydescription.get("designation")==null||propertydescription.get("designation").equals("")?name:Webroutines.formatCapitals("The "+propertydescription.get("designation")));
			if (vintage>0) description.append(" "+vintage);
			description.append(" is a ");
			description.append(propertydescription.get("type")!=null?propertydescription.get("type").toLowerCase()+" ":"");
			description.append("wine, made by <a href='/"+(mobile?"m":"")+"winery/"+Webroutines.URLEncodeUTF8Normalized(getProperties().get("producer")).replaceAll("%2F", "/").replace("&", "&amp;")+"' title='"+getProperties().get("producer").replaceAll("'", "&apos;")+": wines and winery information'>"+propertydescription.get("producer")+"</a>. Its origin is <a href='/"+(mobile?"m":"")+"region/"+Webroutines.removeAccents(getProperties().get("locale")).replaceAll(", ", "/").replaceAll("'", "&apos;").replaceAll(" ", "+")+"' title='"+getProperties().get("appellation").replaceAll("'", "&apos;")+": wine region information'>"+propertydescription.get("appellation")+"</a>");
			if (propertydescription.get("locale").split(", ").length>1) description.append(" in ");
			for (int i=propertydescription.get("locale").split(", ").length-2;i>=0;i--){
				if (i<propertydescription.get("locale").split(", ").length-2) description.append(", ");
				description.append(propertydescription.get("locale").split(", ")[i]);
				
			}
			description.append(". "); 
			if (propertydescription.get("grapes")!=null&&!propertydescription.get("grapes").equals("")&&!propertydescription.get("grapes").toLowerCase().replaceAll("red","").replaceAll("white","").replaceAll("ros.","").replaceAll("blend","").trim().equals("")){
				description.append("It is made from "+(propertydescription.get("grapes").toLowerCase().endsWith("blend")?"a ":propertydescription.get("grapes").contains(",")?"":"the "));
				description.append(propertydescription.get("grapes").replace("Blend","blend"));
				description.append(propertydescription.get("grapes").toLowerCase().endsWith("blend")?". ":(propertydescription.get("grapes").contains(",")?" grapes. ":" grape. "));  
			}
			if (vintage>0) description.append("It is produced from the "+vintage+" harvest. ");
			this.description=description.toString();
			} 
		}
		return description;
	}

	public static boolean editKnownwineId(Context c, int wineid, int ratedwineid, int newknownwineid, int disableknownwineid){
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		String table="";
		int id=0;
		int oldknownwineid=0;
		try {
			if (disableknownwineid==0){

			if (wineid>0) {
				id=wineid;
				table="wines";
			}
			if (ratedwineid>0) {
				id=ratedwineid;
				table="ratedwines";
			}
			oldknownwineid=Dbutil.readIntValueFromDB("select * from "+table+" where id="+id, "knownwineid");
					
			if (!table.equals("")){
				rs=Dbutil.selectQuery("select * from "+table+" where name='"+Spider.SQLEscape(Dbutil.readValueFromDB("select * from "+table+" where id="+id, "name"))+"';", con);
				//Dbutil.logger.info("select * from "+table+" where name='"+Spider.SQLEscape(Dbutil.readValueFromDB("select * from "+table+" where id="+id, "name"))+"';");
				while (rs.next()){
					ChangeLog log=new ChangeLog(c,rs,"id");
					log.setValueOld(rs);
					int result=Dbutil.executeQuery("update "+table+" set knownwineid="+(newknownwineid>0?newknownwineid:0)+", manualknownwineid="+newknownwineid+" where id="+rs.getInt("id")+";");
					if (result>0) {
						Winerating.updateRatingAnalysis(oldknownwineid);
						Winerating.updateRatingAnalysis(newknownwineid);
						rs2=Dbutil.selectQuery("select * from "+table+" where id="+rs.getInt("id")+";", con);
						log.setValueNew(rs2);
						log.save();
					}
					//Dbutil.closeRs(rs);

				}
				return true;
			}
			} else {
				Knownwines.disableKnownwine(disableknownwineid);
				return true;
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}

		return false;
	}

	public static String editKnownwineidHtml(int wineid, int ratedwineid){
		String table="";
		int id=0;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		StringBuffer sb=new StringBuffer();
		ArrayList<String> terms=new ArrayList<String>();
		if (wineid>0) {
			id=wineid;
			table="wines";
		}
		if (ratedwineid>0) {
			id=ratedwineid;
			table="ratedwines";
		}
		String winename=Dbutil.readValueFromDB("select * from "+table+" where id="+id, "name");
		int knownwineid=Dbutil.readIntValueFromDB("select * from "+table+" where id="+id, "knownwineid");
		winename=winename.replaceAll("\\W", " ");
		for (String w:winename.split("\\s+")) if (w.length()>1) terms.add(Webroutines.removeAccents(w).toLowerCase());
		try {
			query = "select * from "+table+" left join knownwines on (knownwineid=knownwines.id) where name='"+Spider.SQLEscape(Dbutil.readValueFromDB("select * from "+table+" where id="+id, "name"))+"';";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				sb.append("<h1>This wine is described as \""+rs.getString("name")+"\"</h1><h2>The system thinks this matches <i>"+rs.getString("wine")+"</i></h2><br/>");
				sb.append("<a href='?disableknownwineid="+knownwineid+"' >Click here</a> to completely disable "+rs.getString("wine")+" and re-analyze wines that are currently recognized as such.<br/>");
			}
			Dbutil.closeRs(rs);
			String fts="match(wine,appellation) against ('"+Recognizer.toFtsUniqueTerms(Dbutil.readValueFromDB("select * from "+table+" where id="+id, "name"))+"')";
			query = "select *,"+fts+" as score from knownwines where "+fts+" having score>0 order by score desc limit 100;";
			rs = Dbutil.selectQuery(rs, query, con);
			sb.append("<h1>Please select which wine this really is:</h1><table><tr><th style='text-align:left;'>Wine name</th><th style='text-align:left;'>Producer</th><th style='text-align:left;'>Appellation</th><th style='text-align:left;'>Cuvee</th><th style='text-align:left;'>Type</th><th style='text-align:left;'>Grape</th></tr>");
			sb.append("<tr><td><a href='?"+(wineid>0?"wineid="+wineid:"ratedwineid="+ratedwineid)+"&newknownwineid=-1'>None of the wines in the list, remove knownwine id</a></td><td></td></tr>");
			while (rs.next()) {
				sb.append("<tr><td><a href='?"+(wineid>0?"wineid="+wineid:"ratedwineid="+ratedwineid)+"&newknownwineid="+rs.getInt("id")+"'  style='text-decoration:none;'>");
				for (String t:rs.getString("wine").split("\\s+")){
					if (terms.contains(Webroutines.removeAccents(t).toLowerCase())){
						sb.append("<b>"+t+"</b> ");
					} else {
						sb.append(t+" ");
					}
				}
				sb.append("</a></td><td>"+rs.getString("producer")+"</td><td>"+rs.getString("appellation")+"</td><td>"+(rs.getString("vineyard")+" "+rs.getString("cuvee")).trim()+"</td><td>"+rs.getString("type")+"</td><td>"+rs.getString("grapes")+"</td></tr>");
			}
			sb.append("</table>");
			Dbutil.closeRs(rs);

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();

	}

}

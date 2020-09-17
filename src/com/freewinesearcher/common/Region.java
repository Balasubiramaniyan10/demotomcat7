package com.freewinesearcher.common;

import java.sql.Connection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.text.*;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import com.freewinesearcher.batch.Spider;




public class Region {
	int id;
	int lft;
	int rgt;
	
	public Region(int id){
		this.id=id;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from kbregionhierarchy where id="+id;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				lft=rs.getInt("lft");
				rgt=rs.getInt("rgt");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}
	
	

	public static void updateParent(){
		Connection con=Dbutil.openNewConnection();
		Connection regioncon=Dbutil.openNewConnection();
		ResultSet regions;
		ResultSet rs;
		String parent="";
		int parentid;
		try{		
			Dbutil.executeQuery("update regions set parentid=0");
			regions=Dbutil.selectQuery("Select * from regions where parentid=0;", regioncon);
			while (regions.next()){
				parentid=0;
				rs=Dbutil.selectQuery("Select * from regions where region='"+Spider.SQLEscape(regions.getString("Parent"))+"';", con);
				if (rs.next()){
					parentid=rs.getInt("id");
				}
				Dbutil.executeQuery("update regions set parentid="+parentid+" where id="+regions.getInt("id"));
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while getting parent id in Regions",exc);
		}
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(regioncon);

	}

	public static String getRegion(int knownwineid){
		String region=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid, "appellation");
		if (region.equals("Unknown")) region="";
		return region;
		}
		
	public static int getLft(int knownwineid){
		int lft=0;
		//lft=Dbutil.readIntValueFromDB("select * from knownwines join kbregionhierarchy on (knownwines.locale=kbregionhierarchy.region) where knownwines.id="+knownwineid, "lft");
		return lft;
		}
		
	public static int getRgt(int knownwineid){
		int rgt=0;
		//rgt=Dbutil.readIntValueFromDB("select * from knownwines join kbregionhierarchy on (knownwines.locale=kbregionhierarchy.region) where knownwines.id="+knownwineid, "rgt");
		return rgt;
		}
		
	public static int rebuildTree(int parent, int left){

		int right=0;
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		try{
			// the right value of this node is the left value + 1
			right = left+1;

			// get all children of this node
			rs = Dbutil.selectQuery("SELECT * FROM regions WHERE id!="+parent+" and parentid="+parent+";",con);
			while (rs.next()) {
				// recursive execution of this function for each
				// child of this node
				// $right is the current right value, which is
				// incremented by the rebuild_tree function
				right = rebuildTree(rs.getInt("id"), right);
			}

			// we've got the left value, and now that we've processed
			// the children of this node we also know the right value
			rs = Dbutil.selectQuery("SELECT count(*) as thecount FROM regions WHERE lft=0;",con);
			rs.next();
			//if (rs.getInt("thecount")==3) {
			//	Dbutil.logger.info(parent);
			//}
			Dbutil.executeQuery("UPDATE regions SET lft="+left+", rgt="+right+" WHERE id="+parent+";");

			// return the right value of this node + 1
		} catch (Exception exc){
			Dbutil.logger.error("Problem while building tree of Regions",exc);
		}
		Dbutil.closeConnection(con);
		return right+1;
	}
	
	public static void updateRegionsinKnownWines(){

		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		try{
			rs = Dbutil.selectQuery("SELECT distinct(knownwineid),regions.lft,regions.rgt FROM wines join knownwines on wines.knownwineid=knownwines.id join regions on knownwines.appellation=regions.region WHERE knownwineid>0;",con);
			while (rs.next()) {
				String query="Update wines set lft="+rs.getInt("lft")+", rgt="+rs.getInt("rgt")+" where knownwineid="+rs.getInt("knownwineid")+";";
				Dbutil.executeQuery(query,con);
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while updating lft/rgt regions",exc);
		}
		Dbutil.closeConnection(con);
	
	}
	

	public static ArrayList<String> getRegions(String area){
		ArrayList<String> regions= new ArrayList<String>();
		ResultSet rs;
		int left;
		int right;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from kbregionhierarchy where shortregion ='"+Spider.SQLEscape(area)+"';", con);
			if (rs.next()){
				left=rs.getInt("lft");
				right=rs.getInt("rgt");
				rs=Dbutil.selectQuery("Select * from kbregionhierarchy where lft between "+left+" and "+right+" and shortregion !='' ORDER BY shortregion ASC;", con);
				while (rs.next()){
					regions.add(rs.getString("shortregion"));
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regiontree of for region "+area,exc);
		}
		Dbutil.closeConnection(con);
		return regions;
	}

	public static String getRegionsAsIntList(String area){

		String regions="";
		ResultSet rs;
		int left;
		int right;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from kbregionhierarchy where shortregion='"+Spider.SQLEscape(area)+"';", con);
			if (rs.next()){
				left=rs.getInt("lft");
				right=rs.getInt("rgt");
				rs=Dbutil.selectQuery("Select * from kbregionhierarchy where lft >= "+left+" and rgt<="+right+" ORDER BY lft ASC;", con);
				while (rs.next()){
					regions+=","+rs.getString("id");
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regiontree of for region "+area,exc);
		}
		if (regions.length()>0) regions=regions.substring(1);
		Dbutil.closeConnection(con);
		return regions;
	}

	public static String getRegionsAsStringList(String area){

		String regions="";
		ResultSet rs;
		int left;
		int right;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from regions where region='"+Spider.SQLEscape(area)+"';", con);
			if (rs.next()){
				left=rs.getInt("lft");
				right=rs.getInt("rgt");
				rs=Dbutil.selectQuery("Select * from regions where lft >= "+left+" and rgt<="+right+" ORDER BY lft ASC;", con);
				while (rs.next()){
					regions+=",'"+Spider.SQLEscape(rs.getString("region"))+"'";
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regiontree of for region "+area,exc);
		}
		if (regions.length()>0) regions=regions.substring(1);
		Dbutil.closeConnection(con);
		return regions;
	}

	
	public static boolean isRegion(String region){
		ResultSet rs;
		boolean isregion=false;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from regions where region='"+Spider.SQLEscape(region)+"';", con);
			if (rs.next()){
				isregion=true;
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while checking isRegion "+region,exc);
		}
		Dbutil.closeConnection(con);
		return isregion;
	}

	
	
	public static ArrayList<String> getRegionPath(String area){
		ArrayList<String> regions= new ArrayList<String>();
		ResultSet rs=null;
		int left;
		int right;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from kbregionhierarchy where shortregion='"+Spider.SQLEscape(area)+"';", con);
			if (rs.next()){
				left=rs.getInt("lft");
				right=rs.getInt("rgt");
				Dbutil.closeRs(rs);
				rs=Dbutil.selectQuery("Select * from kbregionhierarchy where lft<"+left+" and rgt>"+right+" ORDER BY lft ASC;", con);
				while (rs.next()){
					regions.add(rs.getString("shortregion"));
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regiontree of for region "+area,exc);
		}finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			
		}
		return regions;
	}
	public ArrayList<String> getRegionPath(){
		ArrayList<String> regions= new ArrayList<String>();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from regions where lft<"+lft+" and rgt>"+rgt+" ORDER BY lft ASC;", con);
			while (rs.next()){
					regions.add(rs.getString("region"));
				}
			
		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regiontree of for region "+id,exc);
		}finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			
		}
		return regions;
	}

	

	public static JSONObject getRegionJSON(String region){
		JSONObject json = new JSONObject();
		JSONArray below=new JSONArray();
		JSONArray path=new JSONArray();
		try{
			if (region!=null){
				ArrayList<String> belowregions=getRegionsImmediateBelow(region);
				for (String reg:belowregions){
					below.put(reg);
				}
				ArrayList<String> regionpath =getRegionPath(region);	
				for (String reg:regionpath){
					path.put(reg);
				}
				path.put(region);
				json.put("path", path);
				json.put("below", below);
			}else {
				Dbutil.logger.error("getRegionJSON got a null value for region");
				json.put("status", "no results");
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		//Dbutil.logger.info(json);
		return json;
	}

	
	
	
	public static ArrayList<String> getRegionsImmediateBelow(String area){
		ArrayList<String> regions= new ArrayList<String>();
		ResultSet rs;
		int left;
		int right;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from regions where region='"+Spider.SQLEscape(area)+"';", con);
			if (rs.next()){
				rs=Dbutil.selectQuery("Select * from regions where parentid="+rs.getInt("id")+";", con);
				while (rs.next()){
					regions.add(rs.getString("region"));
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regiontree of for region "+area,exc);
		}
		Dbutil.closeConnection(con);
		return regions;
	}

	
	public static String editUnrecognizedRegionsHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs;
		ResultSet rs2;
		ResultSet matches;
		Collator collator = Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;
		String problem;
		String newfull;
		Pattern pattern;
		Matcher matcher;
		String searchterm="";
		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			sb.append("<table style=\"table-layout:fixed;white-space: nowrap;text-overflow:ellipsis;overflow:hidden;\"><tr><th width=10%></th><th width=10%></th><th width=10%></th><th width=70%></th></tr>");

			query="select appellation, thecount,region  from (select appellation, count(*) as thecount from knownwines group by appellation order by thecount desc) as app left join regions on (app.appellation=regions.region) having region is null limit 50;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String[] search=rs.getString("appellation").split("[ -']");
				String fulltext="";
				for (String j:search){
					fulltext+=" +"+j;
				}
				query="select id,lft,region,match (region) against ('"+fulltext+"') as score from regions where match (region) against ('"+fulltext+"') order by score desc limit 20;";
				rs2=Dbutil.selectQuery(query, con);
				while(rs2.next()){
					sb.append("<tr><td><a href=\"http://www.cellartracker.com/list.asp?Table=List&Appellation="+rs.getString("appellation")+"\" target=\"_blank\">"+rs.getString("appellation")+"</a></td><td><a href=\"updateregion.jsp?region="+rs2.getString("region")+"&replacement="+rs.getString("appellation")+"\" target=\"_blank\">"+rs2.getString("region")+"</a></td><td><a href=\"addregion.jsp?region="+rs.getString("appellation")+"&like="+rs2.getString("region")+"\" target=\"_blank\">Add</a></td></tr>\n");
				}
				
			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		}
		sb.append("</table>");

		Dbutil.closeConnection(con);

		return sb.toString();
	}
	
	public static void replaceregion(String region, String replacement){
		
		Dbutil.executeQuery("update regions set region='"+Spider.SQLEscape(replacement)+"' where region='"+Spider.SQLEscape(region)+"';");
		Dbutil.executeQuery("update regions set parent='"+Spider.SQLEscape(replacement)+"' where parent='"+Spider.SQLEscape(region)+"';");

	}

	public static void addRegion(String region, String parent){
		
		Dbutil.executeQuery("insert into regions (region, parent, regionraw) values ('"+Spider.SQLEscape(region)+"','"+Spider.SQLEscape(parent)+"','"+Spider.SQLEscape(region)+"');");

	}

}

package com.freewinesearcher.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;


import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;

public class Bulkripper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length<3){
			System.out.println("Usage: Bulkripper [action] [sourcefile] [destfile]");
		} else {
			String action=args[0];
			String filenamein=args[1];
			String filenameout=args[2];
			int i=0;
			String line="";
	        
			try {
				BufferedReader in = new BufferedReader(new FileReader(filenamein));
				BufferedWriter out = new BufferedWriter(new FileWriter(filenameout+i+".txt"));
		        while ((line = in.readLine()) != null) {
		        	line=line.replaceAll("%(?!\\w\\w)", "");
		        	try{
		        		line=URLDecoder.decode(line,"ISO-8859-1");
		        	}
		            catch (Exception e){}
	        		out.write(line+"\n");
		        }
		        in.close();
		        out.close();
		        in = new BufferedReader(new FileReader(filenameout+i+".txt"));
		        i++;
				out = new BufferedWriter(new FileWriter(filenameout+i+".txt"));
		        Matcher matcher;
		        Pattern pattern;
		        int j=0;
				while ((line = in.readLine()) != null) {
		        	pattern=Pattern.compile("&Wine=([^>]+)&Appellation=([^&]+)&Type=([^&]+)&");
		        	matcher=pattern.matcher(line);
		        	if (matcher.find()){
		        		j++;
		        		out.write("insert into knownwinestype(wine,appellation,type) values ('"+Spider.SQLEscape(matcher.group(1))+"','"+Spider.SQLEscape(matcher.group(2))+"','"+Spider.SQLEscape(matcher.group(3))+"');\n");	
		        	}
		        	pattern=Pattern.compile("&Appellation=([^&]+)&Producer=([^>]+?)&Wine=(.*?)&fInStock");
		        	matcher=pattern.matcher(line);
		        	if (matcher.find()){
		        		j++;
		        		out.write("insert into knownwinesproducer(wine,appellation,producer) values ('"+Spider.SQLEscape(matcher.group(3))+"','"+Spider.SQLEscape(matcher.group(1))+"','"+Spider.SQLEscape(matcher.group(2))+"');\n");	
		        	}
		        	pattern=Pattern.compile("&Wine=([^&]+)&Type=([^>]+?)&");
		        	matcher=pattern.matcher(line);
		        	if (matcher.find()){
		        		j++;
		        		out.write("insert ignore into knownwinestype(wine,appellation,type) values ('"+Spider.SQLEscape(matcher.group(1))+"','','"+Spider.SQLEscape(matcher.group(2))+"');\n");	
		        	}
		        	
		        }
		        in.close();
		        out.close();
		        System.out.println(j+" wines processed");
		        
		    } catch (Exception e) {
		    	System.out.println("Problem while executing Bulkripper");
		    	System.out.println("Line: "+line);
		    	e.printStackTrace();
		    }
		}
	}
	
	
	/* Process the regions from a file that states Region, subregion, appellation
	 * Stores it in a table called appellations! Should be renamed to regions later.
	 * Dbutil.executeQuery("CREATE TABLE  `wijn`.`appellations` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `region` varchar(255) NOT NULL,  `parentid` int(10) unsigned NOT NULL DEFAULT '0',  `lft` int(10) unsigned NOT NULL DEFAULT '0',  `rgt` int(10) unsigned NOT NULL DEFAULT '0',  `parent` varchar(255) NOT NULL DEFAULT '',  PRIMARY KEY (`id`),  UNIQUE KEY `region` (`region`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;");
	 * Dbutil.executeQuery("Insert into appellations (region,parent) values ('All','All');");
	 * Replace all <tr with \n<tr using UE32 first
	 */
	public static void processRegions(String filenamein, String filenameout){
		int i=0;
		String line="";
	    
		try {
			BufferedReader in = new BufferedReader(new FileReader(filenamein));
			BufferedWriter out = new BufferedWriter(new FileWriter(filenameout+i+".txt"));
	        while ((line = in.readLine()) != null) {
	        	line=line.replaceAll("%(?!\\w\\w)", "");
	        	try{
	        		line=URLDecoder.decode(line,"ISO-8859-1");
	        	}
	            catch (Exception e){}
	    		out.write(line+"\n");
	        }
	        in.close();
	        out.close();
	        in = new BufferedReader(new FileReader(filenameout+i+".txt"));
	        i++;
			out = new BufferedWriter(new FileWriter(filenameout+i+".txt"));
	        Matcher matcher;
	        Pattern pattern;
	        int app=0;
	        int region=0;
	        int country=0;
	        while ((line = in.readLine()) != null) {
	        	pattern=Pattern.compile("&Region=([^>]+)&SubRegion=([^&]+)&Appellation=([^&]+)&");
	        	matcher=pattern.matcher(line);
	        	if (matcher.find()){
	        		if (!matcher.group(3).equals("Unknown")){
	        			String parent=matcher.group(2);
	        			if (parent.equals("Unknown")){
	        				parent=matcher.group(1);
	        			}
	        			if (!parent.equalsIgnoreCase(matcher.group(3))){
	        			app++;
	        			Dbutil.executeQuery("insert ignore into appellations(region,parent) values ('"+Spider.SQLEscape(matcher.group(3))+"','"+Spider.SQLEscape(parent)+"');");
	        			}
	        		}
	        	}
	        	pattern=Pattern.compile("&Country=([^&]+)&Region=([^&]+)&SubRegion=([^&]+)&");
	        	matcher=pattern.matcher(line);
	        	if (matcher.find()){
	        		if (!matcher.group(1).equals("Unknown")){
	        			String parent="All";
	        			country++;
	        			Dbutil.executeQuery("insert ignore into appellations(region,parent) values ('"+Spider.SQLEscape(matcher.group(1))+"','"+Spider.SQLEscape(parent)+"');");
	        		}
	        		if (!matcher.group(2).equals("Unknown")){
	        			String parent=matcher.group(1);
	        			region++;
	        			if (!parent.equalsIgnoreCase(matcher.group(2))){
	            		Dbutil.executeQuery("insert ignore into appellations(region,parent) values ('"+Spider.SQLEscape(matcher.group(2))+"','"+Spider.SQLEscape(parent)+"');");
	        			}
	        		}
	        		if (!matcher.group(3).equals("Unknown")){
	        			String parent=matcher.group(2);
	        			if (parent.equals("Unknown")){
	        				parent=matcher.group(1);
	        			}
	        			if (!parent.equalsIgnoreCase(matcher.group(3))){
	        			region++;
	        			Dbutil.executeQuery("insert ignore into appellations(region,parent) values ('"+Spider.SQLEscape(matcher.group(3))+"','"+Spider.SQLEscape(parent)+"');");
	         			}
	        		}
	        	}
	        	
	        }
	        in.close();
	        out.close();
	        System.out.println(app+" appellations processed");
	        System.out.println(region+" regions processed");
	        System.out.println(country+" countries processed");
	        
	    } catch (Exception e) {
	    	System.out.println("Problem while executing Bulkripper");
	    	System.out.println("Line: "+line);
	    	e.printStackTrace();
	    }
	}
		
	public static void copyAppellationstoRegions(){
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		try {
			String query="select * from appellations group by region,parent order by count(*) desc;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				//Dbutil.logger.info("Insert ignore into regions (region,parent) values ('"+Spider.SQLEscape(rs.getString("region"))+"','"+Spider.SQLEscape(rs.getString("parent"))+"');");
				Dbutil.executeQuery("Insert ignore into regions (region,parent) values ('"+Spider.SQLEscape(rs.getString("region"))+"','"+Spider.SQLEscape(rs.getString("parent"))+"');",con);
			}
	    } catch (Exception e) {
	    	System.out.println("Problem while executing Bulkripper");
	    	e.printStackTrace();
	    }
	    Dbutil.closeConnection(con);
	}
		
	public static void analyseknownwinetype(){
		//Knownwinetypeexactsame();
		//updateKnownWinesTypeforKnownWines();
		//Knownwinetype(0, true,true);
		//updateKnownWinesTypeforKnownWines();
		//Knownwinetype(0, false,true);
		//updateKnownWinesTypeforKnownWines();
		//processtype();
		Knownwinetypesame();
		updateKnownWinesTypeforKnownWines();
		saveinKnownWines();
	}
	
	public static void Knownwinetypeexactsame(){ 
		Dbutil.logger.info("Starting job to recognize wines");
		ResultSet rs;
		ResultSet wines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		try{
			//Dbutil.executeQuery("update knownwinestype set knownwineid=0;");
			Dbutil.executeQuery("delete from knownwinestypematch;");
			rs=Dbutil.selectQueryRowByRow("Select * from knownwinestype where appellation!='' and knownwineid>0;", con);
			// Should we not use RowByRow because of a timeout problem???
			while (rs.next()){
				
				query="Select * from knownwines where wine='"+Spider.SQLEscape(rs.getString("wine"))+"' and appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"';";
				wines=Dbutil.selectQuery(query, winescon);
				if (wines.next()){
						Dbutil.executeQuery("insert into knownwinestypematch (knownwineid,wineid) values ("+wines.getInt("id")+","+rs.getString("id")+");",knownwinescon);
				}
			}
			rs.close();
			rs=null;
			wines=null;
			System.gc();
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		}

		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(winescon);
		System.gc();
		Dbutil.logger.info("Finished job to recognize wines");
	}

	public static void Knownwinetypesame(){ 
		Dbutil.logger.info("Starting job to recognize wines");
		ResultSet rs;
		ResultSet wines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		try{
		//Dbutil.executeQuery("update knownwinestype set knownwineid=0;");
		Dbutil.executeQuery("delete from knownwinestypematch;");
		rs=Dbutil.selectQueryRowByRow("Select * from knownwinestype where knownwineid=0;", con);
		// Should we not use RowByRow because of a timeout problem???
		while (rs.next()){
			
			query="Select * from knownwines where wine like '"+Spider.SQLEscape(rs.getString("wine").replace(" 1er Cru", ""))+"%' and color='';";
			wines=Dbutil.selectQuery(query, winescon);
			while (wines.next()){
					Dbutil.executeQuery("insert into knownwinestypematch (knownwineid,wineid) values ("+wines.getInt("id")+","+rs.getString("id")+");",knownwinescon);
			}
		}
		rs.close();
		rs=null;
		wines=null;
		System.gc();
	} catch (Exception exc){
		Dbutil.logger.error("Problem while looking up knownwines",exc);
	}
	

		/*try{
		//Dbutil.executeQuery("update knownwinestype set knownwineid=0;");
		Dbutil.executeQuery("delete from knownwinestypematch;");
		rs=Dbutil.selectQueryRowByRow("Select * from knownwines where color='' group by wine having count(*)=1;", con);
		// Should we not use RowByRow because of a timeout problem???
		while (rs.next()){
			
			query="Select * from knownwinestype where wine='"+Spider.SQLEscape(rs.getString("wine").replace(" 1er Cru", "").replace(" Vineyards", ""))+"';";
			wines=Dbutil.selectQuery(query, winescon);
			while (wines.next()){
					Dbutil.executeQuery("insert into knownwinestypematch (knownwineid,wineid) values ("+rs.getInt("id")+","+wines.getString("id")+");",knownwinescon);
			}
		}
		rs.close();
		rs=null;
		wines=null;
		System.gc();
	} catch (Exception exc){
		Dbutil.logger.error("Problem while looking up knownwines",exc);
	}
	*/

		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(winescon);
		System.gc();
		Dbutil.logger.info("Finished job to recognize wines");
	}

	public static void Knownwinetype(int history, boolean includeregion,boolean refreshall){ 
		Dbutil.logger.info("Starting job to recognize wines, history="+history);
		ResultSet rs;
		ResultSet wines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		try{
			knownwinescon.setAutoCommit(false);
			
			Dbutil.executeQuery("delete from knownwinestypematch;");

			rs=Dbutil.selectQueryRowByRow("Select * from knownwines where disabled=false;", con);
			// Should we not use RowByRow because of a timeout problem???
			while (rs.next()){
				whereclause=Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"), includeregion);

				if (whereclause.equals(";")||whereclause.equals("")){
					Dbutil.logger.debug("Lege zoekstring voor id "+rs.getString("id"));
				} else {
					query="Select * from knownwinestype where"+whereclause+historywhereclause+" and knownwineid=0;";

					wines=Dbutil.selectQuery(query, winescon);
					while (wines.next()){
						Dbutil.executeQuery("Insert into knownwinestypematch (wineid,knownwineid) values ('"+wines.getString("id")+"','"+rs.getString("id")+"');",knownwinescon);
					}
					knownwinescon.commit();
					wines.close();
					wines=null;

				}
			}
			rs.close();
			rs=null;
			System.gc();
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		}

		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(winescon);
		Dbutil.logger.info("Finished job to recognize wines");
	}

	public static void updateKnownWinesTypeforKnownWines(){

		Dbutil.logger.info("Starting job to store matches in knownwinestype");
		ResultSet rs;
		ResultSet knownwines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();

		try{  
			knownwinescon.setAutoCommit(false);
			//First, we will update all single precise matches
			query="select * from knownwinestypematch group by wineid having count(*)=1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.executeQuery("update knownwinestype set knownwineid="+rs.getInt("knownwineid")+" where id="+rs.getInt("wineid")+" and knownwineid=0;",knownwinescon);
			}
			knownwinescon.commit();
			
		} catch (Exception exc){
			Dbutil.logger.error("Problem while saving matches in wines",exc);
		}


		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);
		Dbutil.logger.info("Finished job to store matches in wines");
	}
	
	
	



	
	public static void processtype(){
		ResultSet rs;
		ResultSet rs2;
		Connection con;
		con=Dbutil.openNewConnection();
		try{
			String query="update knownwinestype set color='Red', dry='Dry' where type='Red';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='White', dry='Dry' where type='White';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='Ros�', dry='Dry' where type='Ros�';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='White', dry='Sweet/Dessert' where type='White - Sweet/Dessert';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='Red', dry='Fortified' where type='Red - Fortified';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='White', dry='Dry', sparkling=1 where type='White - Sparkling';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='Ros�', dry='Dry', sparkling=1 where type='Ros� - Sparkling';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='White', dry='Off-dry' where type='White - Off-dry';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='White', dry='Fortified' where type='White - Fortified';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='Red', dry='Sweet/Dessert' where type='Red - Sweet/Dessert';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='Red', dry='Dry', sparkling=1 where type='Red - Sparkling';";
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='Ros�', dry='Sweet/Dessert' where type='Ros� - Sweet/Dessert'	;";		
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='na Fruit', dry='na Fruit' where type='Fruit/Vegetable Wine'	;";		
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='na Spirit', dry='na Spirit' where type='Spirits'	;";		
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='na Liquer', dry='na Liquer' where type='Liqueur'	;";		
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='na Nonalc', dry='na Nonalc' where type='Non-alcoholic'	;";		
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='na Sake', dry='na Sake' where type='Sake'	;";		
			Dbutil.executeQuery(query);
			query="update knownwinestype set color='na Fruit', dry='na Fruit' where type='Fruit%2FVegetable+Wine'	;";		
			Dbutil.executeQuery(query);
				
		}catch(Exception e){
			System.out.println(e);
		}
		
		
		Dbutil.closeConnection(con);
	}

	public static void saveinKnownWines(){
		ResultSet rs;
		ResultSet rs2;
		Connection con;
		con=Dbutil.openNewConnection();
		try{
			String query="select * from knownwinestype where color!='' order by id;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="update knownwines set color='"+rs.getString("color")+"', dryness='"+rs.getString("dry")+"', sparkling="+rs.getInt("sparkling")+" where color='' and id="+rs.getInt("knownwineid")+";"; 
				Dbutil.executeQuery(query, con);
			}
		}catch(Exception e){
			System.out.println(e);
		}
		
		
		Dbutil.closeConnection(con);
	}

	
	public static void updatetype(){
		ResultSet rs;
		ResultSet rs2;
		Connection con;
		con=Dbutil.openNewConnection();
		try{
			String query="select * from knownwines where type is null;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				boolean onetype=false;
				query="Select * from knownwinestypesingle where "+Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"),"",false);
				rs2=Dbutil.selectQuery(query, con);
				if(rs2.next()){
					onetype=true;
					String type=rs2.getString("type");
					while (rs2.next()){
						if(!rs2.getString("type").equals(type)&&!rs2.getString("type").equals("")) onetype=false;
						
					}
					if (onetype){
						query="Update knownwines set type='"+type+"' where id="+rs.getString("id")+";";
						Dbutil.executeQuery(query, con);
					}
				} 
			}
			
		}catch(Exception e){
			System.out.println(e);
		}
		
		
		Dbutil.closeConnection(con);
	}
	
	public static void unmatchedRegions(){
		ResultSet rs;
		ResultSet rs2;
		Connection con;
		con=Dbutil.openNewConnection();
		try{
			String query="select distinct(region) as area from regions order by area;";
			rs=Dbutil.selectQuery(query,con);
			String regions="";
			while (rs.next()){
				regions+=", '"+Spider.SQLEscape(rs.getString("area"))+"'";
			}
			regions=regions.substring(1);
			query="Select distinct(appellation) as region from knownwines where appellation not in ("+regions+");";
			rs2=Dbutil.selectQuery(query, con);
			while(rs2.next()){
				System.out.println(rs2.getString("region"));
			}
			
		}catch(Exception e){
			System.out.println(e);
		}
		
		
		Dbutil.closeConnection(con);
	}
	public static void unescapeknownwineregions(){
		ResultSet rs;
		Connection con;
		con=Dbutil.openNewConnection();
		try{
			String query="select distinct(appellation) from knownwines where appellation like '%#%';";
			rs=Dbutil.selectQuery(query,con);
			String regions="";
			while (rs.next()){
				query="update knownwines set appellation='"+Spider.SQLEscape(Spider.unescape(rs.getString("appellation")))+"' where appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"';";
				System.out.println(query);
				//Dbutil.executeQuery(query,con);
			}
			
			
		}catch(Exception e){
			System.out.println(e);
		}
		
		
		Dbutil.closeConnection(con);
	}

}
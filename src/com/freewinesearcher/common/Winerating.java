package com.freewinesearcher.common;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.online.Webroutines;

public class Winerating implements Serializable{

	private static final long serialVersionUID = 1L;
	public String author;
	public double ratinglow;
	public double ratinghigh;
	public String wine;
	public String vintage;
	public double minpriceeuroex;
	public double avgpriceeuroex;
	public String issuedate="0000-00-00";
	public String issue="";
	public String link;
	
	

	public Winerating(int knownwineid, int vintage, String author){
		int rating=0;
		try{
			String ratingstr=Dbutil.readValueFromDB("select * from ratinganalysis where knownwineid="+knownwineid+" and vintage="+vintage+" and author='"+author+"';", "rating");
			rating=Integer.valueOf(ratingstr);
		} catch (Exception e){}
		this.author=author;
		this.ratinglow=rating;
		this.ratinghigh=0;
	}

	public Winerating(String author, double ratinglow, double ratinghigh){
		this.author=author;
		this.ratinglow=ratinglow;
		this.ratinghigh=ratinghigh;
	}
	public Winerating(String wine, String vintage,String author, double ratinglow, double ratinghigh, double minpriceeuroex, double avgpriceeuroex){
		this.author=author;
		this.ratinglow=ratinglow;
		this.ratinghigh=ratinghigh;
		this.wine=wine;
		this.vintage=vintage;
		this.minpriceeuroex=minpriceeuroex;
		this.avgpriceeuroex=avgpriceeuroex;
	}
	public static void refreshRatedWines(){
		
		boolean problemsduringanalysis=false;
		Dbutil.executeQuery("update ratedwines set knownwineid=0 where name !='' and name!=knownwineid;");
		problemsduringanalysis=analyseRatedWines(true);
		if (!problemsduringanalysis) problemsduringanalysis=storeknownratedwines();
		if (!problemsduringanalysis) problemsduringanalysis=analyseRatedWines(false);
		if (!problemsduringanalysis) problemsduringanalysis=storeknownratedwines();
		if (!problemsduringanalysis) problemsduringanalysis=cleanRatedWines();
		if (!problemsduringanalysis) analyseWineRatings();
		
	}

	public static boolean analyseRatedWines(boolean includeregion){
		/* We do an analysis of all wines listed in ratedwines table.
		 * First, we set all knownwineid's to 0 in table ratedwines 
		 * (except for the legacy which just contains a knownwineid, not a wine name). 
		 * All matches are stored in table ratedwinesmatch. 
		 * This can be done in two ways: including a match in region (appellation) or without it.
 		 */
		boolean problem=false;
		Dbutil.logger.info("Starting job to analyze wines in ratedwines;");
		ResultSet rs=null;
		ResultSet knownwines=null;
		String whereclause;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		Dbutil.executeQuery("delete from ratedwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines where disabled=false;", con);
		try{
			while (rs.next()){
				whereclause=Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"),includeregion);

				if (whereclause.equals(";")||whereclause.equals("")){
					Dbutil.logger.debug("Lege zoekstring voor id "+rs.getString("id"));
				} else {
					query="Select * from ratedwines where"+whereclause+" and knownwineid=0;";
					knownwines=Dbutil.selectQuery(query, knownwinescon);
					while (knownwines.next()){
						Dbutil.executeQuery("Insert into ratedwinesmatch (wineid,knownwineid) values ('"+knownwines.getString("id")+"','"+rs.getString("id")+"');");		
					}
					knownwines.getStatement().close();
				}
			}


		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
			problem=true;
		}
		Dbutil.logger.info("Finished job to analyze wines in ratedwines;");

		Dbutil.closeRs(rs);
		Dbutil.closeRs(knownwines);
		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);
		return problem;
	}

	public static boolean storeknownratedwines(){
		/* All matches were stored in table ratedwinesmatch. 
		 * All entries with exactly 1 knownwineid associated are considered a match, ratedwines is updated accordingly.
		 */
		boolean problem=false;
		Dbutil.logger.info("Starting job to store knownwineid's in ratedwines");
		ResultSet rs;
		ResultSet knownwines;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		try{
			knownwinescon.setAutoCommit(false);
			knownwines=Dbutil.selectQuery("Select *, count(*) as thecount from ratedwinesmatch group by wineid having thecount=1;", knownwinescon);
			while (knownwines.next()){
				if (knownwines.getInt("thecount")==1){
					// update knownwineid
					//						query="insert into ratings (knownwineid,vintage,rating,ratinghigh,author,name) values ("
					//	+knownwines.getInt("knownwineid")+",'"+rs.getInt("vintage")+"','"+rs.getInt("rating")+"','"+rs.getInt("ratinghigh")+"','"+rs.getString("author")+"','"+Spider.SQLEscape(rs.getString("name"))+"') " +
					//	"ON DUPLICATE KEY UPDATE rating="+rs.getInt("rating")+", ratinghigh="+rs.getInt("ratinghigh")+", name='"+Spider.SQLEscape(rs.getString("name"))+"';";
					query="update ratedwines set knownwineid="+knownwines.getInt("knownwineid")+" where id="+knownwines.getInt("wineid")+" and vintage !='';"; 
					Dbutil.executeQuery(query,knownwinescon);
					query="delete from ratedwinesmatch where wineid="+knownwines.getInt("wineid")+";";
					Dbutil.executeQuery(query,knownwinescon);
				} else {
					Dbutil.logger.error("Count should be 1!");
				}
			}
			knownwinescon.commit();

//			 Now, we will see if we can find the most restrictive wine description
			query="select wineid, group_concat(knownwineid) as thelist from ratedwinesmatch group by wineid having count(*)>1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int wineid=rs.getInt("wineid");
				ArrayList<Integer> intlist=new ArrayList<Integer>();
				String[] list=rs.getString("thelist").split(",");
				for (int i=0;i<list.length;i++){
					intlist.add(Integer.parseInt(list[i]));
				}
				ArrayList<Integer> mostrestricted=Knownwines.getMostRestrictedKnownWine(intlist);
				if (mostrestricted.size()==1){
					Dbutil.executeQuery("update ratedwines set knownwineid="+mostrestricted.get(0)+" where id="+rs.getInt("wineid")+";",knownwinescon);
				}
				String mostrestrictedlist="";
				for (int i=0;i<mostrestricted.size();i++){
					mostrestrictedlist+=","+mostrestricted.get(i);
				}
				if (mostrestrictedlist.length()>1){
					mostrestrictedlist=mostrestrictedlist.substring(1);
					Dbutil.executeQuery("delete from ratedwinesmatch where wineid="+rs.getInt("wineid")+" and knownwineid not in ("+mostrestrictedlist+");",knownwinescon);
				} else {
					Dbutil.logger.error("Unexpected result: no restrictive wines for wines.id="+rs.getInt("wineid"));
				}
			}
			knownwinescon.commit();
			

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
			problem=true;
		}

		Dbutil.logger.info("Finished job to store analyzed wines in ratedwines;");

		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);	
		return problem;
	}
	public static boolean cleanRatedWines(){
		boolean problem=false;
		Dbutil.logger.info("Starting job to clean double wines in ratedwines;");
		ResultSet rs=null;
		ResultSet ratedwines=null;
		String whereclause;
		String query;
		int count;
		int n;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		try{
			// Make a selection from the best records in ratedwines.
			// First criterion: name must be filled.
			// Second criterion: ratinghigh is 0 (more accurate)
			// Third criterion: lastupdated dated, the more recent the better.
			// Keep only the best match of knownwineid, vintage and author, delete the rest
			query="update ratedwines set actueel=0 where knownwineid>0;";
			Dbutil.executeQuery(query);
			query="select *, count(*) as thecount from ratedwines  group by author,vintage,knownwineid having  thecount=1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.executeQuery("update ratedwines set actueel=1 where id="+rs.getString("id")+";");
			}
			query="Select * from ratedwines where knownwineid>0 and actueel=0 order by (length(name)>0) desc, issuedate desc, (ratinghigh>0), lastupdated desc;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="Select * from ratedwines where knownwineid="+rs.getInt("knownwineid")+" and author='"+rs.getString("author")+"' and vintage="+rs.getInt("vintage")+" and actueel=1;"; 
				ratedwines=Dbutil.selectQuery(query, con);
				if (!ratedwines.next()){
					Dbutil.executeQuery("update ratedwines set actueel=1 where id="+rs.getInt("id")+";",con);
				}
			}
			query="Select * from ratedwines where knownwineid=0 order by (ratinghigh>0), lastupdated desc;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="Select * from ratedwines where name='"+Spider.SQLEscape(rs.getString("name"))+"' and author='"+rs.getString("author")+"' and vintage="+rs.getInt("vintage")+" and actueel=1;"; 
				ratedwines=Dbutil.selectQuery(query, con);
				if (!ratedwines.next()){
					Dbutil.executeQuery("update ratedwines set actueel=1 where id="+rs.getInt("id")+";",con);
				}
			}
			Dbutil.executeQuery("delete from ratedwines where actueel=0;");
			
				
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
			Dbutil.logger.info("Finished job to analyze wines in ratedwines;");
			problem=true;
		}
		Dbutil.closeRs(rs);
		Dbutil.closeRs(ratedwines);
		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);	
		return problem;
	}
	
	public static int getRating(int knownwineid, int vintage){
		int rating=0;
		rating=Dbutil.readIntValueFromDB("select * from ratinganalysis where author='FWS' and knownwineid="+knownwineid+" and vintage= "+vintage+";", "rating");
		return rating;
	}

	// This routine calculates the average rating for all authors 
	public static void analyseWineRatings(){
		Dbutil.logger.info("Starting job to analyze wine ratings");
		ResultSet rs=null;
		ResultSet rs2=null;
		String query;
		int n=0;
		int knownwineid=0;
		int vintage=0;
		double minimum=0;
		double median=0;
		Connection con=Dbutil.openNewConnection();
		Dbutil.executeQuery("delete from ratinganalysis;");
		//Dbutil.executeQuery("insert into ratinganalysis select r.knownwineid as knownwineid,r.vintage as vintage,r.rating,r.ratinghigh, min(w.priceeuroin), avg(w.priceeuroin),r.author from ratedwines r join wines w on (r.knownwineid=w.knownwineid and r.vintage=w.vintage) where r.knownwineid>0 and r.vintage>0 and w.size=0.75 group by knownwineid,vintage, author;");
		Dbutil.executeQuery("insert into ratinganalysis (knownwineid,vintage,author,rating,ratinghigh,minpriceeuroex,avgpriceeuroex) select r.knownwineid as knownwineid,r.vintage as vintage,'FWS',round(avg(CASE WHEN r.ratinghigh>0 THEN (r.rating+r.ratinghigh)/2 ELSE r.rating END)) as average,0,0,0 from ratedwines r where r.vintage>0 and r.rating>60 and knownwineid>0 group by knownwineid,vintage;");
		try{
			rs=Dbutil.selectQuery("select knownwineid,vintage,( substring_index( substring_index( group_concat( f.priceeuroex order by f.priceeuroex ) , ',' , ceiling(count(*)/2) ) , ',' , -1 ) + substring_index( substring_index( group_concat( f.priceeuroex order by f.priceeuroex ) , ',' , -ceiling(count(*)/2) ) , ',' , 1 ) ) / 2  as median, min(f.priceeuroex) as minimum from wines f  where size=0.75 and knownwineid>0 group by f.knownwineid,f.vintage;", con);
			while(rs.next()){
				Dbutil.executeQuery("update ratinganalysis set avgpriceeuroex="+rs.getDouble("median")+", minpriceeuroex="+rs.getDouble("minimum")+" where knownwineid="+rs.getString("knownwineid")+" and vintage="+rs.getString("vintage")+" and author='FWS';",con);
			}
					// For wines without size=0.75 but with other sizes, set median and minimum=1 so that we do know we have records
			rs=Dbutil.selectQuery("select -1 as median, -1 as minimum,f.knownwineid,f.vintage from wines f where knownwineid>0 and size!=0.75 group by f.knownwineid,f.vintage;", con);
			while(rs.next()){
				Dbutil.executeQuery("update ratinganalysis set avgpriceeuroex="+rs.getDouble("median")+", minpriceeuroex="+rs.getDouble("minimum")+" where knownwineid="+rs.getString("knownwineid")+" and vintage="+rs.getString("vintage")+" and author='FWS' and minpriceeuroex=0;",con);
			}
		} catch (Exception e){
			Dbutil.logger.error("Error while analyzing wine ratings: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
		Dbutil.logger.info("Finished job to analyze wine ratings");
	}
	
	public static void updateRatingAnalysis(int knownwineid){
		Dbutil.executeQuery("update ratinganalysis ra join (select knownwineid,vintage,round(avg(CASE WHEN r.ratinghigh>0 THEN (r.rating+r.ratinghigh)/2 ELSE r.rating END)) as average from ratedwines r where r.vintage>0 and r.rating>60 and r.knownwineid="+knownwineid+" group by r.knownwineid,r.vintage) sel on (ra.knownwineid=sel.knownwineid and ra.vintage=sel.vintage) set ra.rating=sel.average;");
		Dbutil.executeQuery("update ratinganalysis ra join (select knownwineid,vintage,( substring_index( substring_index( group_concat( f.priceeuroex order by f.priceeuroex ) , ',' , ceiling(count(*)/2) ) , ',' , -1 ) + substring_index( substring_index( group_concat( f.priceeuroex order by f.priceeuroex ) , ',' , -ceiling(count(*)/2) ) , ',' , 1 ) ) / 2  as median, min(f.priceeuroex) as minimum from wines f  where size=0.75 and knownwineid="+knownwineid+" group by f.knownwineid,f.vintage) sel on (ra.knownwineid=sel.knownwineid and ra.vintage=sel.vintage) set ra.minpriceeuroex=sel.minimum, ra.avgpriceeuroex=sel.median;");
		//Dbutil.executeQuery("delete from mateializedadvice where knownwineid="+knownwineid);
		//Dbutil.executeQuery("insert ignore into materializedadvice select wines.id,wines.knownwineid, wines.vintage, wines.priceeuroex, ratinganalysis.rating, ((size/0.75)*pqratio.price/priceeuroex) as pqratio, wines.lft,wines.rgt,shops.countrycode, winetypecoding.typeid as winetypecode, wines.shopid,grapes.id from wines left join ratinganalysis on (wines.knownwineid=ratinganalysis.knownwineid and wines.vintage=ratinganalysis.vintage and ratinganalysis.author='FWS') left join pqratio on (ratinganalysis.rating=pqratio.rating) join shops on (wines.shopid=shops.id) left join knownwines on (wines.knownwineid=knownwines.id) natural left join winetypecoding left join grapes on (knownwines.grapes=grapes.grapename) where wines.size=0.75 and wines.knownwineid="+knownwineid+";");
	}
	
	public static String getLink(String author, String winename){
		if (winename.contains(",")) winename=winename.substring(0,winename.indexOf(","));
		if (author.equals("RP")) return "http://www.erobertparker.com/newSearch/pTextSearch.aspx?search=members&amp;textSearchString="+java.text.Normalizer.normalize(winename,java.text.Normalizer.Form.NFD);
		if (author.equals("WS")) return "http://wines.winespectator.com/wine/search?submitted=Y&amp;forwarded=1&amp;size=50&amp;text_search_flag=wine_plus_vintage&amp;search_by=all&amp;fuzzy=&amp;sort_by=vintage&amp;case_prod=&amp;taste_date=&amp;viewtype=&amp;winery="+Webroutines.URLEncodeUTF8(winename);
		return null;
		
	}



	public static double twentypoints(int rating){
		switch (rating){
		case 100: return 20;
		case 99: return 19.5;
		case 98: return 19.5;
		case 97: return 19.0;
		case 96: return 18.5;
		case 95: return 18;
		case 94: return 17.5;
		case 93: return 17.0;
		case 92: return 16.5;
		case 91: return 16;
		case 90: return 15.5;
		case 89: return 15.0;
		case 88: return 14.5;
		case 87: return 14.0;
		case 86: return 13.5;
		case 85: return 13.0;
		case 84: return 13.0;
		case 83: return 12.2;
		case 82: return 12.5;
		case 81: return 12.0;
		case 80: return 12.0;


		}

		return 0;
	}

}

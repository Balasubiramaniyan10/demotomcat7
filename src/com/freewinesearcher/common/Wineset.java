/*
 * Created on 5-mrt-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.common;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.text.*;
import java.lang.*;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.online.Searchdata;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.ai.AiHtmlRefiner;

/**
 * @author Jasper
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Wineset {
	public Searchdata s;
	public String canonicallink="";
	public Wine Wine[];
	public Wine SponsoredWine[];
	public double lowestprice=0.0;
	public double highestprice=0.0;
	public int records=0;
	public String searchtype="";
	public int knownwineid=0;
	public int bestknownwineid=0;
	public String region="";
	public String shortregion="";
	//public ArrayList<ArrayList<Integer>> knownwinelist=new ArrayList<ArrayList<Integer>>(); 
	public LinkedHashMap<Integer,Integer> knownwinelist=new LinkedHashMap<Integer,Integer>(); 
	public boolean othervintage=false;
	public boolean othercountry=false;
	public TreeSet<Integer> vintages=new TreeSet<Integer>();
	public TreeSet<Float> sizes=new TreeSet<Float>();
	NumberFormat knownwineformat  = new DecimalFormat("000000");	
	Connection con;
	

	public Wineset(){

	}

	public Wineset (Searchdata searchdata){
		this.s=searchdata;
	}

	public void getKnownWineList(boolean vintage){
		knownwinelist.clear();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			String nameclause=getNameClause(true);
			String vintageclause="";
			if (vintage) vintageclause=getVintageClause();
			String criteria=nameclause+vintageclause;
			if (criteria.length()>5) {
				query="select knownwineid,count(*) as numberofwines from wineview where knownwineid>0 and "+criteria.substring(5)+" group by knownwineid order by numberofwines desc"; 
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					knownwinelist.put(rs.getInt("knownwineid"), rs.getInt("numberofwines"));
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		

	}
		
	
	public void getKnownWineList(){
		knownwinelist.clear();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			String nameclause=getNameClause(true).replace("Name", "wine");
			String criteria=nameclause;

			if (criteria.length()>5) {
				query="select id,numberofwines from knownwines where "+criteria.substring(5)+" and numberofwines>0 order by numberofwines desc"; 
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					knownwinelist.put(rs.getInt("id"), rs.getInt("numberofwines"));
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}
	
	public void search(){
		con=Dbutil.openNewConnection();
		try{
			determineSearchType(); // get a knownwineid if fuzzy, determine a possible knownwineid if not	
			getWines();
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while doing search", exc);
		} finally {
			Dbutil.closeConnection(con);
		}

	}


	
	private void getWines(){
		String query;
		ResultSet rs=null;
		try{
			String nameclause=getNameClause(true);
			String vintageclause=getVintageClause();
			String sizeclause=getSizeClause();
			String countryclause=getCountryClause();
			String priceclause=getPriceClause();
			String criteria=nameclause+vintageclause+sizeclause+countryclause+priceclause;
			String minmaxpriceclause=nameclause+vintageclause;
			if (criteria.length()>5) {
				executeSearch(false, makequery(criteria),makeminmaxquery(minmaxpriceclause));
				if ((Wine==null||Wine.length==0)&&!countryclause.equals("")){
					criteria=nameclause+vintageclause+sizeclause+priceclause;
					executeSearch(false, makequery(criteria),makeminmaxquery(minmaxpriceclause));
					if (Wine!=null&&Wine.length>0) othercountry=true;
				}
				if ((Wine==null||Wine.length==0)&&!vintageclause.equals("")&&s.getOffset()==0){
					criteria=nameclause+sizeclause+countryclause+priceclause;
					executeSearch(false, makequery(criteria),makeminmaxquery(minmaxpriceclause));
					if (Wine!=null&&Wine.length>0) othervintage=true;
				}
				if ((Wine==null||Wine.length==0)&&!countryclause.equals("")&&!vintageclause.equals("")&&s.getOffset()==0){
					criteria=nameclause+sizeclause+priceclause;
					executeSearch(false, makequery(criteria),makeminmaxquery(minmaxpriceclause));
					if (Wine!=null&&Wine.length>0) {othervintage=true;othercountry=true;}
				}
				if (Wine!=null&&Wine.length>0){
					query="select distinct(vintage) from wineview where "+nameclause.substring(4)+sizeclause+(othercountry?"":countryclause)+priceclause;

					rs=Dbutil.selectQuery(query, con);
					while (rs.next()){
						if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
					}
					Dbutil.closeRs(rs);
					query="select distinct(size) from wineview where "+nameclause.substring(4)+(othervintage?"":vintageclause)+(othercountry?"":countryclause)+priceclause;
					rs=Dbutil.selectQuery(query, con);
					while (rs.next()){
						if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
					}
					Dbutil.closeRs(rs);
					getBestKnownwineid(nameclause+vintageclause+sizeclause);
					if (s.sponsoredresults){
						criteria=nameclause+(othervintage?"":vintageclause)+(othercountry?"":countryclause)+sizeclause+priceclause;
						if (criteria.length()>5) {
							criteria=criteria.substring(4);
							query="select * from wineview  left join ratinganalysis on (wineview.knownwineid=ratinganalysis.knownwineid and wineview.vintage=ratinganalysis.vintage and ratinganalysis.author='FWS' and ratinganalysis.knownwineid>0 and ratinganalysis.vintage>0) where "+criteria+" and cpc>0 order by "+getHostCountryClause()+" cpc desc,rand() limit 0,"+s.sponsoredrows+";";
							executeSearch(true, query,makeminmaxquery(minmaxpriceclause));
							
						}
						Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+query+" executed.");
					}
					if (canonicallink==null||canonicallink.length()==0){
						getCanonicalLink(bestknownwineid);
					}
				}
			}

		}catch( Exception e ) {
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);

		}
	}
	
	private String makeminmaxquery(String criteria){
		String query="";
		if (criteria.length()>5) {
			criteria=criteria.substring(4);
			query="select min(priceeuroex) as min, max(priceeuroex) as max from wineview  where size < 2 and "+criteria;
		}
		return query;
	}

	private String makequery(String criteria){
		String query="";
		String distanceselect="";
		String distance="";
		if (s.getOrder().contains("distance")){
			if (s.lat!=0&&s.lon!=0){
			distanceselect=",60 * 1.1515*DEGREES(ACOS(SIN(RADIANS(lat)) * SIN(RADIANS("+s.lat+")) + COS(RADIANS(lat)) * COS(RADIANS("+s.lat+")) * COS(RADIANS(lon-"+s.lon+")))) as km ";
			distance="ACOS(SIN(RADIANS(lat)) * SIN(RADIANS("+s.lat+")) + COS(RADIANS(lat)) * COS(RADIANS("+s.lat+")) * COS(RADIANS(lon-"+s.lon+")))";
			} else{
				s.setOrder("priceeuroex");
			}
		}
		if (criteria.length()>5) {
			criteria=criteria.substring(4);
			query="select SQL_CALC_FOUND_ROWS *"+distanceselect+" from wineview left join ratinganalysis on (wineview.knownwineid=ratinganalysis.knownwineid and wineview.vintage=ratinganalysis.vintage and ratinganalysis.author='FWS' and ratinganalysis.knownwineid>0 and ratinganalysis.vintage>0) where "+criteria;
			if (s.getOrder()!=null && !s.getOrder().equals("") && s.getOrder().length()>2){
				if (s.getOrder().contains("distance")&&s.lat!=0&&s.lon!=0){
					query=query+" order by "+distance+", priceeuroex,cpc desc";
				} else {
					query=query+" order by wineview."+s.getOrder()+", priceeuroex,cpc desc";
				}
			} else {
				query=query+" order by priceeuroex,cpc desc";
			}
			query+=" limit "+s.getOffset()+","+s.numberofrows+";";
		}
		return query;
	}

	private void executeSearch(boolean sponsored, String query, String minmaxquery) throws Exception{
		Wine[] Wine=new Wine[0];
		ResultSet rs=null;
		rs = Dbutil.selectQuery(rs,query,con);
		if (rs.last()){
			int count = rs.getRow();
			rs.beforeFirst();
			Wine = new Wine[count];
			int i=0;
			while (rs.next()){
				ArrayList<Winerating> rating=null;
				if (rs.getInt("rating")>0) {
					rating=new ArrayList<Winerating>();
					//rating.add(new Winerating(rs.getInt("knownwineid"), rs.getInt("vintage"),"FWS"));
					rating.add(new Winerating("FWS",rs.getDouble("rating"),rs.getDouble("ratinghigh")));
				}
				Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("PriceEuroIn"), rs.getDouble("CPC"), rs.getString("sourceURL"),
						rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),rating);
				i++;
			}
			if (sponsored){
				SponsoredWine=Wine;
			} else {
				this.Wine=Wine;
				Dbutil.closeRs(rs);
				if (lowestprice==0.0){
				rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",con);
				if (rs.next()) records=rs.getInt("records");
				Dbutil.closeRs(rs);
				rs = Dbutil.selectQuery(rs,minmaxquery,con);
				if (rs.next()) {
					lowestprice=rs.getDouble("min");
					highestprice=rs.getDouble("max");
					
				}
				}

			}
			Dbutil.closeRs(rs);

		}
	}

	private void getBestKnownwineid(String whereclause){
		if (knownwineid>0) {
			bestknownwineid=knownwineid;
		} else {
			String query="select knownwineid,count(*) as thecount from (select knownwineid from wines as wineview where "+whereclause.substring(4)+" limit 40) sel group by knownwineid order by thecount desc;";
			ResultSet rs = null;
			try {
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					rs.last();
					int n=rs.getRow();
					rs.beforeFirst();
					rs.next();
					if (rs.getInt("thecount")*2>n){
						// 50% threshold
						bestknownwineid=rs.getInt("knownwineid");
					}
					if (bestknownwineid==0&&rs.next()){
						if (rs.getInt("thecount")*3>n){
							// 33% threshold
							bestknownwineid=rs.getInt("knownwineid");
						}

					}

				}
				rs.beforeFirst();
				while (rs.next()){
					knownwinelist.put(rs.getInt("knownwineid"),rs.getInt("thecount"));
				}


			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);

			}
		}
	}

	private String getNameClause( boolean inbooleanmode){
		if (knownwineid>0) return " AND wineview.knownwineid="+knownwineid;
		String name=s.getName().replaceAll("(?<! |^)-", " ");
		name=name.replaceAll("( |^)-( |$)", " ");
		name=Spider.replaceString(name, "(", " ");
		name=Spider.replaceString(name, ")", " ");
		name=Spider.replaceString(name, ".", " ");
		name=Spider.replaceString(name, ",", " ");
		name=Spider.replaceString(name, "&", " ");
		name=Spider.replaceString(name, "'", " ");
		name=Spider.replaceString(name, "\"", " ");
		name=Spider.replaceString(name, ";", " ");
		name=Spider.replaceString(name, ":", " ");
		name=Spider.replaceString(name, "/", " ");
		name=Spider.replaceString(name, "@", " ");
		name=Spider.replaceString(name, "%", " ");

		String nameclause=" AND match(Name) against ('";
		for (String term:name.split(" ")){
			if (term.length()>1) {
				if (term.startsWith("-")){
					nameclause+=" "+term+(inbooleanmode?"*":"");
				} else {
					nameclause+=" +"+term+(inbooleanmode?"*":"");
				}
			}
		}
		nameclause+="'"+(inbooleanmode?" in boolean mode":"")+") ";
		return nameclause;
	}

	public Double getMedianPriceEuroEx(){
		Double median=(double)0;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query="select ( substring_index( substring_index( group_concat( floor(wineview.priceeuroex*100) order by wineview.priceeuroex ) , ',' , ceiling(count(*)/2) ) , ',' , -1 ) + substring_index( substring_index( group_concat( floor(wineview.priceeuroex*100) order by wineview.priceeuroex ) , ',' , -ceiling(count(*)/2) ) , ',' , 1 ) ) / 2 /100 as median,count(*) from wineview where "+(getNameClause(true)+getVintageClause()).substring(5);
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				median=rs.getDouble("median");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return median;
	}

	private String getVintageClause(){
		String vintagesearch="";
		if (!s.getVintage().equals("")){
			vintagesearch=getVintageClause(s.getVintage());
			if (!vintagesearch.equals("")) vintagesearch=" AND ("+vintagesearch+")";
		}
		return vintagesearch;
	}

	private String getCountryClause(){
		if (s.getCountry().equals("All")||s.getCountry().equals("")){
			return("");
		} else {
			return " AND Country in ('"+Webroutines.getCountries(s.getCountry())+"')";
		}
	}

	private String getHostCountryClause(){
		if (s.getHostcountry().equals("All")||s.getHostcountry().equals("")||s.getHostcountry().equals("ZZ")){
			return("");
		} else {
			return " Country='"+s.getHostcountry()+"' desc, country in ('"+Webroutines.getCountries(Webroutines.getRegion(s.getHostcountry()))+"') desc, ";
		}
	}

	private String getPriceClause(){
		String pc="";
		if (s.getPricemin()>0)	{
			pc+=" AND priceeuroex >= "+s.getPricemin();
		}
		if (s.getPricemax()>0)	{
			pc+=" AND priceeuroex <= "+s.getPricemax();
		}
		return pc;
	}

	private String getSizeClause(){
		if (s.getSize()>0){
			return " AND size= "+s.getSize()+" ";
		}
		return "";
	}



	public void determineSearchType() {
		if (knownwineid>0) {
			searchtype="smart";
		} else {
			if (s.fuzzy) {
				knownwineid=guessKnownWineId();
			} else {
				findknownwineid();
			}
			if (knownwineid>0) {
				searchtype="smart";
				if (canonicallink==null||canonicallink.length()==0){
					getCanonicalLink(knownwineid);
				}
			} else {
				searchtype="text";
			}
		}
		if (knownwineid>0) {
			region=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid, "locale");
			shortregion=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid, "appellation");
		}
	}

	private void filterNameDoubleTerms(){
		String name=s.getName().replaceAll("(?<! |^)-", " ");
		name=name.replaceAll("( |^)-( |$)", " ");
		name=Spider.replaceString(name, "(", " ");
		name=Spider.replaceString(name, ")", " ");
		name=Spider.replaceString(name, ".", " ");
		name=Spider.replaceString(name, ",", " ");
		name=Spider.replaceString(name, "&", " ");
		name=Spider.replaceString(name, "'", " ");
		name=Spider.replaceString(name, "\"", " ");
		name=Spider.replaceString(name, ";", " ");
		name=Spider.replaceString(name, ":", " ");
		name=Spider.replaceString(name, "/", " ");
		name=Spider.replaceString(name, "@", " ");
		name=Spider.replaceString(name, "%", " ");

		HashSet<String> unique=new HashSet<String>();
		int n=0;
		for (String term:name.split(" ")){
			if (n<30&&term.length()>1&&!term.toLowerCase().equals("in")) {
				n++;
				unique.add(term);
			}
		}
		Iterator<String> it=unique.iterator();
		String newterms="";
		
		while (it.hasNext()) {
			newterms+=it.next()+" ";
		}
		s.setName(newterms);
	}

	public int guessKnownWineId(){ 
		int result=0;
		ResultSet rs=null;
		filterNameDoubleTerms();
		String query="select knownwineid from (select * from wines where knownwineid>0 "+getNameClause(true)+" order by "+getNameClause(true).substring(5)+" desc limit 10) sel group by knownwineid order by count(*) desc limit 1;"; 
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query,con);
			if (rs.next()){
				result=rs.getInt("knownwineid");
				return result;
			}
			Dbutil.closeRs(rs);
			rs=null;
			query="select knownwineid,count(*) as thecount from (select * from wines where knownwineid>0 "+getNameClause(false)+" order by "+getNameClause(false).substring(5)+" desc limit 15) sel group by knownwineid order by count(*) desc limit 1;"; 
			rs=Dbutil.selectQuery(query,con);
			if (rs.next()){
				if (rs.getInt("thecount")>2){
					result=rs.getInt("knownwineid");
					return result;
				} else {
					Dbutil.closeRs(rs);
					rs=null;
					
					String clause=getNameClause(false).substring(5).replaceAll("match\\(Name\\)", "match(wine,appellation)");
				
					query="select * from knownwines where  "+clause+" order by "+clause+" desc limit 1;"; 
					rs=Dbutil.selectQuery(query,con);
					if (rs.next()){
						result=rs.getInt("id");
						return result;
					}
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return 0;
	}



	public void findknownwineid(){
		knownwineid=0;
		if (!Webroutines.getRegexPatternValue("(\\d\\d\\d\\d\\d\\d)",s.getName()).equals("")){
			try{
				knownwineid=Integer.parseInt(Webroutines.getRegexPatternValue("(\\d\\d\\d\\d\\d\\d)",s.getName()));
			} catch (Exception e){}
		}
		if (knownwineid==0){
			ResultSet rs=null;
			Connection con=Dbutil.openNewConnection();

			try{
				rs=Dbutil.selectQuery(rs,"select * from knownwines where wine='"+Spider.SQLEscape(s.getName())+"' and disabled=false order by numberofwines desc;", con);
				if (rs.next()){
					knownwineid=rs.getInt("id");
					canonicallink="https://www.vinopedia.com/wine/"+(rs.getBoolean("samename")?(knownwineformat.format(rs.getInt("id"))+"+"):"")+Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine"))).replaceAll("%2F", "/");
					//this.knownwineid=knownwineid;
				}
			} catch (Exception exc) {
				Dbutil.logger.error("Problem while finding knownwineid", exc);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}


	}

	public void getCanonicalLink(int knownwineid){
		if (knownwineid>0){
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "select * from knownwines where id="+knownwineid+";";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					canonicallink="https://www.vinopedia.com/wine/"+(rs.getBoolean("samename")?(knownwineformat.format(rs.getInt("id"))+"+"):"")+Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine"))).replaceAll("%2F", "/");
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
	}

	public static Wineset getWineset(Searchdata searchdata, String referrer, int numberofrows, boolean fuzzy, boolean sponsoredresults){
		Wineset wineset=null;
		if (false){
			// Construct wine name from Google query parameters
			if (referrer==null) referrer="";
			int start=referrer.indexOf("&q=")+3;
			if (start==0) start=referrer.indexOf("?q=")+3;
			if (start>0) {
				int end=referrer.indexOf("&",start+2);
				if (end>start) {
					String googlequery=referrer.substring(start,end).replaceAll("\\+"," ");
					try{
						googlequery=URLDecoder.decode(googlequery,"UTF-8");
						googlequery=googlequery.replaceAll("[Ww]ine","");
						googlequery=googlequery.replaceAll("[Ff]ree","");
						googlequery=googlequery.replaceAll("[Ss]earcher","");
						googlequery=googlequery.replaceAll("[Ss]earch","");
						googlequery=googlequery.replaceAll("[Pp]rice","");
						googlequery=googlequery.replaceAll("\\d+","").trim();
						googlequery=googlequery.replaceAll(" +"," ");
						googlequery=Webroutines.filterUserInput(googlequery);
						if (googlequery.length()>3){
							Wineset tempwineset=new Wineset(googlequery,searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),1);
							if (tempwineset.records>5){
								searchdata.setName(googlequery);

							}			
						}
					} catch (Exception e){}
				}
			}
		}
		if (fuzzy){
			if (searchdata.getName().length()>2) {
				String newname=Knownwines.determineKnownWine(searchdata.getName());
				wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
				Wineset winesetfuzzy=new Wineset(newname,searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
				if (winesetfuzzy!=null&&(wineset==null||winesetfuzzy.records>=wineset.records)){
					searchdata.setName(newname); 	 
					wineset=winesetfuzzy;
					if (sponsoredresults){
						wineset.SponsoredWine = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
					}
				}
				if ((wineset==null||wineset.Wine==null||wineset.Wine.length==0)&&searchdata.getVintage().length()>3){
					wineset = new Wineset(searchdata.getName(),"", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
					winesetfuzzy=new Wineset(newname,"", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
					if (winesetfuzzy!=null&&(wineset==null||winesetfuzzy.records>=wineset.records)){
						searchdata.setName(newname); 	 
						wineset=winesetfuzzy;
						if (sponsoredresults){
							wineset.SponsoredWine = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), new Float(0),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
						}
					}
					if (wineset!=null&&wineset.records>0){
						wineset.othervintage=true;
					}

				}
			}
		} else {
			wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
			//sponsoredwineset=null;

			if (wineset==null||(wineset.searchtype.equals("smart")&&wineset.records==0)){
				wineset = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
				if (sponsoredresults){
					wineset.SponsoredWine = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
				}
			} else {
				if (sponsoredresults){
					wineset.SponsoredWine = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
				}
			}
			if ((wineset==null||wineset.records==0)&&!searchdata.getCountry().equals("")&&!searchdata.getCountry().equals("All")){
				wineset = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),"", searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
				//sponsoredwineset=null;

				if (wineset==null||(wineset.searchtype.equals("smart")&&wineset.records==0)){
					wineset = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),"", searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
					if (sponsoredresults){
						wineset.SponsoredWine = new Wineset(searchdata.getName()+"'",searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),"", searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
					}
				} else {
					if (sponsoredresults){
						wineset.SponsoredWine = new Wineset(searchdata.getName(),searchdata.getVintage(), searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),"", searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
					}
				}
				if (wineset!=null&&wineset.records>0){
					wineset.othercountry=true;
				}
			}
			if ((wineset==null||wineset.records==0)&&searchdata.getVintage().length()>3){
				wineset = new Wineset(searchdata.getName(),"", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
				//sponsoredwineset=null;

				if (wineset==null||(wineset.searchtype.equals("smart")&&wineset.records==0)){
					wineset = new Wineset(searchdata.getName()+"'","", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "",searchdata.getOrder(),searchdata.getOffset(),numberofrows);
					if (sponsoredresults){
						wineset.SponsoredWine = new Wineset(searchdata.getName()+"'","", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
					}
				} else {
					if (sponsoredresults){
						wineset.SponsoredWine = new Wineset(searchdata.getName(),"", searchdata.getCreated(),searchdata.getPricemin(), searchdata.getPricemax(), searchdata.getSize(),searchdata.getCountry(), searchdata.getRareold(), "","sponsored",searchdata.getOffset(),numberofrows).Wine;
					}
				}
				if (wineset!=null&&wineset.records>0){
					wineset.othervintage=true;
				}
			}
		}

		return wineset;
	}


	public Wineset(String namesearch, String vintagesearch, int created, float pricemin, float pricemax, float size, String country, boolean rareold, String cheapest, String order, int offset, int numberofrows){
		// Constructor to translate int created into a concrete date
		if (country.equals("")) country="All";
		String datestring="";
		Wineset tempwineset;
		long longtime = new java.util.Date().getTime();
		longtime=longtime-(long)created*1000*3600*24;
		Timestamp date = new Timestamp(longtime);
		if (created!=0){
			datestring=date.toString();
		} else { 
			datestring="%";
		}
		tempwineset= new Wineset(namesearch, vintagesearch, datestring, pricemin, pricemax, size, country, rareold, cheapest, order,offset, numberofrows);
		this.Wine=tempwineset.Wine;
		this.region=tempwineset.region;
		this.searchtype=tempwineset.searchtype;
		this.records=tempwineset.records;
		this.knownwinelist=tempwineset.knownwinelist;
		this.knownwineid=tempwineset.knownwineid;
		this.bestknownwineid=tempwineset.bestknownwineid;
		this.vintages=tempwineset.vintages;
		this.sizes=tempwineset.sizes;
	}

	public Wineset(String namesearch, String vintagesearch, String datestring, float pricemin, float pricemax, float size, String country, boolean rareold, String cheapest, String order, int offset, int numberofrows){
		//Constructor to decide what the arguments mean and what search path to take
		knownwineid=0;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		Wineset tempwineset=null;
		country=Webroutines.getCountries(country);
		if (!Webroutines.getRegexPatternValue("(\\d\\d\\d\\d\\d\\d)",namesearch).equals("")){
			try{
				knownwineid=Integer.parseInt(Webroutines.getRegexPatternValue("(\\d\\d\\d\\d\\d\\d)",namesearch));
			} catch (Exception e){}
		}

		if (namesearch.startsWith("Region:")){
			// Region search
			namesearch=namesearch.substring(7);
			tempwineset=new Wineset(namesearch, vintagesearch, datestring, pricemin, pricemax, size,country, rareold,order,offset, numberofrows, cheapest);
		} else {
			if (knownwineid==0){

				try{
					rs=Dbutil.selectQuery("select * from knownwines where wine='"+Spider.SQLEscape(namesearch)+"' and disabled=false order by numberofwines desc;", con);
					if (rs.next()){
						knownwineid=rs.getInt("id");
						//this.knownwineid=knownwineid;
					}
				} catch (Exception exc) {
					Dbutil.logger.error("Problem while finding knownwineid", exc);
				}
			}
			if (knownwineid==0){
				// Free text search
				// Replace special characters
				namesearch=namesearch.replace("'"," ").replace("."," ").replace("."," ").replace("("," ").replace(")"," ");

				if (cheapest.equals("true")){
					tempwineset= new Wineset(namesearch, vintagesearch, datestring, pricemin, pricemax, country, rareold, order,offset, cheapest,numberofrows);
				} else {
					tempwineset= new Wineset(namesearch, vintagesearch, datestring, pricemin, pricemax, size,country, rareold, order,offset, numberofrows);
				}
				this.searchtype="text";
			} else {
				// Exact known wine search
				ArrayList<Integer> knownwineids=new ArrayList<Integer>();
				knownwineids.add(knownwineid);
				tempwineset= new Wineset(knownwineids, vintagesearch, datestring, pricemin, pricemax, size,country, rareold, cheapest,order,offset, numberofrows);
				this.searchtype="smart";
			}
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		this.Wine=tempwineset.Wine;
		this.region=tempwineset.region;
		this.knownwinelist=tempwineset.knownwinelist;
		this.records=tempwineset.records;
		this.bestknownwineid=tempwineset.bestknownwineid;
		this.knownwineid=tempwineset.knownwineid;
		this.vintages=tempwineset.vintages;
		this.sizes=tempwineset.sizes;
	}


	public Wineset(String namesearch, String vintagesearch, String created, float pricemin, float pricemax, float size, String country, boolean rareold, String order, int offset, int numberofrows){
		//Search method: free text search, cheapest=false
		ResultSet rs=null;
		country=Webroutines.getCountries(country);
		Connection winecon=Dbutil.openNewConnection();
		try {
			String Query;
			String Whereclausenovintagesize;
			String nameclause="";
			String sizesearch="";


			Whereclausenovintagesize="";
			namesearch=namesearch.replaceAll("(?<! |^)-", " ");
			namesearch=namesearch.replaceAll("( |^)-( |$)", " ");
			if (false){ // hanging query in production: SELECT SQL_CALC_FOUND_ROWS * from wineview where  match(Name) against (' +Te* +Mata* +Estate* +Cabernet/Merlot* +Woodthorpe* +Vineyard*' in boolean mode)  order by priceeuroin, priceeuroin limit 0,25;
				for (int i=0;i<namesearch.split(" ").length;i++){
					if (i!=0){
						Whereclausenovintagesize=Whereclausenovintagesize+" AND ";
					}

					if (!namesearch.split(" ")[i].startsWith("-")){
						Whereclausenovintagesize=Whereclausenovintagesize+"Name like '%"+Spider.SQLEscape(namesearch.split(" ")[i])+"%'";
					} else {
						Whereclausenovintagesize=Whereclausenovintagesize+"Name not like '%"+Spider.SQLEscape(namesearch.split(" ")[i].substring(1))+"%'";
					}

				}
			} else {
				namesearch=Spider.replaceString(namesearch, "(", " ");
				namesearch=Spider.replaceString(namesearch, ")", " ");
				namesearch=Spider.replaceString(namesearch, ".", " ");
				namesearch=Spider.replaceString(namesearch, ",", " ");
				namesearch=Spider.replaceString(namesearch, "&", " ");
				namesearch=Spider.replaceString(namesearch, "'", " ");
				namesearch=Spider.replaceString(namesearch, "\"", " ");
				namesearch=Spider.replaceString(namesearch, ";", " ");
				namesearch=Spider.replaceString(namesearch, "/", " ");
				namesearch=Spider.replaceString(namesearch, "@", " ");
				namesearch=Spider.replaceString(namesearch, "%", " ");

				Whereclausenovintagesize=Whereclausenovintagesize+" match(Name) against ('";
				for (String term:namesearch.split(" ")){
					if (term.length()>1) {
						if (term.startsWith("-")){
							Whereclausenovintagesize+=" "+term+"*";
						} else {
							Whereclausenovintagesize+=" +"+term+"*";
						}
					}
				}
				Whereclausenovintagesize+="' in boolean mode) ";

			}
			nameclause=Whereclausenovintagesize;
			if (!vintagesearch.equals("")){
				vintagesearch=getVintageClause(vintagesearch);
				if (!vintagesearch.equals("")) vintagesearch=" AND ("+vintagesearch+")";

			}
			if (!country.equals("All")&&!country.equals("")){
				Whereclausenovintagesize=Whereclausenovintagesize+" AND Country in ('"+country+"')";
			}

			if (pricemin>0)	{
				Whereclausenovintagesize=Whereclausenovintagesize+" AND priceeuroex >= "+pricemin;
			}
			if (pricemax>0)	{
				Whereclausenovintagesize=Whereclausenovintagesize+" AND priceeuroex <= "+pricemax;
			}
			if (created!=null && !created.equals("%")){
				Whereclausenovintagesize=Whereclausenovintagesize+" AND createdate >= '"+created+"'";
			}
			if (size>0){
				sizesearch=" AND size= "+size+"";
			}
			if (rareold){
				Whereclausenovintagesize=Whereclausenovintagesize+" AND rareold=true";
			}

			Query="SELECT SQL_CALC_FOUND_ROWS * from wineview where ";
			Query=Query+Whereclausenovintagesize+vintagesearch+sizesearch;
			if (order.equals("sponsored")){
				Query=Query+ " and CPC > 0";
				Query=Query+" order by CPC desc,priceeuroex limit 5";
			} else {
				if (order!=null && !order.equals("") && order.length()>2){
					Query=Query+" order by "+order+", priceeuroex";
				} else {
					Query=Query+" order by priceeuroex";
				}
				Query=Query+" limit "+offset+","+numberofrows+";";
			}

			rs = Dbutil.selectQuery(Query,winecon);
			//Dbutil.logger.info("process query");
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			Wine = new Wine[count];
			int i=0;
			while (rs.next()){
				ArrayList<Winerating> rating=null;
				if (rs.getInt("knownwineid")>0&&!filterVintage(rs.getString("vintage")).equals("")) {
					rating=new ArrayList<Winerating>();
					rating.add(new Winerating(rs.getInt("knownwineid"), rs.getInt("vintage"),"FWS"));
				}
				this.Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("PriceEuroIn"), rs.getDouble("CPC"), rs.getString("sourceURL"),
						rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),rating);
				i++;
			}
			rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",winecon);
			rs.next();
			records=rs.getInt("records");
			Dbutil.closeRs(rs);

			Query="select distinct(vintage) from wineview where "+Whereclausenovintagesize+sizesearch+" ";
			rs=Dbutil.selectQuery(Query, winecon);
			while (rs.next()){
				if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
			}
			Query="select distinct(size) from wineview where "+Whereclausenovintagesize+vintagesearch+" ";
			rs=Dbutil.selectQuery(Query, winecon);
			while (rs.next()){
				if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
			}



			Query="select knownwineid,count(*) as thecount from (select knownwineid from wines where "+nameclause+" limit 40) sel group by knownwineid order by thecount desc;";
			rs=Dbutil.selectQuery(Query, winecon);
			if (rs.next()){
				rs.last();
				int n=rs.getRow();
				rs.beforeFirst();
				rs.next();
				if (rs.getInt("thecount")*2>n){
					// 50% threshold
					bestknownwineid=rs.getInt("knownwineid");
				}
				if (bestknownwineid==0&&rs.next()){
					if (rs.getInt("thecount")*3>n){
						// 33% threshold
						bestknownwineid=rs.getInt("knownwineid");
					}

				}

			}
			rs.beforeFirst();
			while (rs.next()){
				knownwinelist.put(rs.getInt("knownwineid"),rs.getInt("thecount"));
			}

			Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+Query+" executed.");


		}catch( Exception e ) {
			Dbutil.logger.error("Error:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);
		}
	}//end main

	public Wineset(Searchdata searchdata, int numberofrows, boolean ruby, boolean tawny, boolean colheita, boolean vintage, boolean LBV){
		//Search method: Just for ports
		String namesearch=searchdata.getName();
		String vintagesearch=searchdata.getVintage();
		String country=searchdata.getCountry();  
		String order=searchdata.getOrder();
		int offset=searchdata.getOffset(); 
		ResultSet rs=null;
		Connection winecon=Dbutil.openNewConnection();
		String Query;
		String Whereclause;
		Whereclause="";

		try {
			String type="";
			if (ruby) type+=" OR wine like '%ruby%'";
			if (tawny) type+=" OR wine like '%tawny%'";
			if (LBV) type+=" OR wine like '%late bottled vintage%'";
			if (vintage) type+=" OR (wine like '%vintage%' AND wine not like '%late bottled%')";
			if (colheita) type+=" OR wine like '%colheita%'";
			if (!type.equals("")){
				type=type.substring(3);
				String ids=Dbutil.readValueFromDB("select group_concat(id) as ids from knownwines where appellation='Porto' and ("+type+");","ids");
				Whereclause+=" knownwineid in ("+ids+")";
				namesearch=namesearch.replaceAll("(?<! )-", " ");
				for (int i=0;i<namesearch.split(" ").length;i++){
					if (i!=0){
						Whereclause=Whereclause+" AND ";
					}
					if (!namesearch.split(" ")[i].startsWith("-")){
						Whereclause=Whereclause+" AND Name like '%"+Spider.SQLEscape(namesearch.split(" ")[i])+"%'";
					} else {
						Whereclause=Whereclause+" AND Name not like '%"+Spider.SQLEscape(namesearch.split(" ")[i].substring(1))+"%'";
					}
				}

				if (!vintagesearch.equals("")){
					vintagesearch=getVintageClause(vintagesearch);
					if (!vintagesearch.equals("")) Whereclause=Whereclause+" AND ("+vintagesearch+")";

				}
				if (!country.equals("All")){
					Whereclause=Whereclause+" AND Country ='"+country+"'";
				}


				Query="SELECT SQL_CALC_FOUND_ROWS * from wineview where ";
				Query=Query+Whereclause;
				if (order.equals("sponsored")){
					Query=Query+ " and CPC > 0";
					Query=Query+" order by CPC desc,priceeuroex limit 5";
				} else {
					if (order!=null && !order.equals("") && order.length()>2){
						Query=Query+" order by "+order+", priceeuroex";
					} else {
						Query=Query+" order by priceeuroex";
					}
					Query=Query+" limit "+offset+","+numberofrows+";";
				}
				rs = Dbutil.selectQuery(Query,winecon);
				rs.last();
				int count = rs.getRow();
				rs.beforeFirst();
				Wine = new Wine[count];
				int i=0;
				while (rs.next()){
					ArrayList<Winerating> rating=null;
					if (rs.getInt("knownwineid")>0&&!filterVintage(rs.getString("vintage")).equals("")) {
						rating=new ArrayList<Winerating>();
						rating.add(new Winerating(rs.getInt("knownwineid"), rs.getInt("vintage"),"FWS"));
					}
					this.Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("PriceEuroIn"), rs.getDouble("CPC"), rs.getString("sourceURL"),
							rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),rating);
					if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
					if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
					i++;
				}
				rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",winecon);
				rs.next();
				records=rs.getInt("records");
				if (false){
					Query="SELECT knownwineid,count(*) as thecount from wineview where "; //Gaat mis door performance issue, zie free text search
					Query=Query+Whereclause;
					Query+=" group by knownwineid order by thecount desc;";
					rs=Dbutil.selectQuery(Query, winecon);
					if (rs.next()){
						if (rs.getInt("thecount")*2>records){
							// 50% threshold
							bestknownwineid=rs.getInt("knownwineid");
						}
						if (bestknownwineid==0&&rs.next()){
							if (rs.getInt("thecount")*3>records){
								// 33% threshold
								bestknownwineid=rs.getInt("knownwineid");
							}

						}

					}
					rs.beforeFirst();
					while (rs.next()){
						knownwinelist.put(rs.getInt("knownwineid"),rs.getInt("thecount"));
					}
				}
				Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+Query+" executed.");
			}

		}catch( Exception e ) {
			Dbutil.logger.error("Error:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);
		}
	}//end main


	public Wineset(ArrayList<Integer> knownwineid, String vintagesearch, String created, float pricemin, float pricemax, float size, String country, boolean rareold, String cheapest, String order, int offset, int numberofrows){
		//search method: Exact known wine (cheapest or not)
		ResultSet rs=null;
		Connection winecon=Dbutil.openNewConnection();
		try {
			String Query;
			//String vintagestring=vintagesearch;
			//String sWhereclause="";
			String Whereclausenovintagesize="";
			String sizesearch="";



			Whereclausenovintagesize="";
			String knownwineids="";
			for (int i:knownwineid){
				knownwineids+=","+i;
			}
			knownwineids=knownwineids.substring(1);

			Whereclausenovintagesize=Whereclausenovintagesize+" wineview.knownwineid in ("+knownwineids+") ";

			if (!vintagesearch.equals("")){
				vintagesearch=getVintageClause(vintagesearch);
				if (vintagesearch.length()>0) vintagesearch=" and "+vintagesearch;
			}
			if (cheapest.equals("true")){
				sizesearch+=" AND wineview.size>0 ";
			}
			if (!country.equals("All")){
				Whereclausenovintagesize+=" AND Country in ('"+country+"')";
			}
			if (pricemin>0)	{
				Whereclausenovintagesize+=" AND priceeuroex >= "+pricemin;
			}
			if (pricemax>0)	{
				Whereclausenovintagesize+=" AND priceeuroex <= "+pricemax;
			}
			if (size>0){
				sizesearch+=" AND size= "+size+"";
			}
			if (created!=null && !created.equals("%")){
				Whereclausenovintagesize+=" AND createdate >= '"+created+"'";
			}
			if (rareold){
				Whereclausenovintagesize+=" AND rareold=true";
			}


			if (cheapest.equals("true")){
				Query="select SQL_CALC_FOUND_ROWS * from wineview join (select knownwineid,size,vintage,min(priceeuroex) as minprice from wineview where "+Whereclausenovintagesize+vintagesearch+sizesearch+" group by vintage,size) as minprices on wineview.vintage=minprices.vintage and priceeuroex=minprices.minprice and wineview.size=minprices.size and wineview.knownwineid=minprices.knownwineid where "+Whereclausenovintagesize+vintagesearch+sizesearch+" ";
			} else {
				Query="select SQL_CALC_FOUND_ROWS * from wineview where "+Whereclausenovintagesize+vintagesearch+sizesearch+" ";
			}
			if (cheapest.equals("true")){
				Query=Query+" AND wineview.size>0 ";
			}

			if (order.equals("sponsored")){
				Query=Query+ " and CPC > 0";
				Query=Query+" order by CPC desc, priceeuroex limit 5";
			} else {
				if (order!=null && !order.equals("") && order.length()>2){
					Query=Query+" order by "+order+", priceeuroex";
				} else {
					Query=Query+" order by priceeuroex";
				}
				Query=Query+" limit "+offset+","+numberofrows+";";
			}
			rs = Dbutil.selectQuery(Query,winecon);
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			Wine = new Wine[count];
			int i=0;
			while (rs.next()){
				ArrayList<Winerating> rating=null;
				if (rs.getInt("knownwineid")>0&&!filterVintage(rs.getString("vintage")).equals("")) {
					//Query="select distinct(vintage) from wineview where "+Whereclause+" ";
					rating=new ArrayList<Winerating>();
					rating.add(new Winerating(rs.getInt("knownwineid"), rs.getInt("vintage"),"FWS"));
				}
				this.Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("PriceEuroIn"), rs.getDouble("CPC"), rs.getString("sourceURL"),
						rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),rating);
				i++;
			}
			rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",winecon);
			rs.next();
			records=rs.getInt("records");


			if (!cheapest.equals("true")&&!order.equals("sponsored")){
				Query="select distinct(vintage) from wineview where "+Whereclausenovintagesize+sizesearch+" ";
				rs=Dbutil.selectQuery(Query, winecon);
				while (rs.next()){
					if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
				}
				Query="select distinct(size) from wineview where "+Whereclausenovintagesize+vintagesearch+" ";
				rs=Dbutil.selectQuery(Query, winecon);
				while (rs.next()){
					if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
				}

			}


			// This does not work as we can expect an array
			// region=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";", "appellation");
			if (knownwineid.size()==1){
				region=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid.get(0)+";", "appellation");
			} else {
				region="";
			}
			if (knownwineid.size()==1){
				bestknownwineid=knownwineid.get(0);
				this.knownwineid=knownwineid.get(0);
			}

			Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+Query+" executed.");


		}catch( Exception e ) {
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);
		}
	}//end main






	public Wineset(String namesearch, String vintagesearch, String created, float pricemin, float pricemax, String country, boolean rareold, String order, int offset, String cheapest, int numberofrows){
		// Search method: Free text search, cheapest true
		ResultSet rs=null;
		Connection winecon=Dbutil.openNewConnection();
		try {
			String Query;
			String vintagestring=vintagesearch;
			String Whereclause;



			Whereclause="";
			namesearch=namesearch.replaceAll("(?<! )-", " ");

			for (int i=0;i<namesearch.split(" ").length;i++){
				if (i!=0){
					Whereclause=Whereclause+" AND ";
				}
				if (!namesearch.split(" ")[i].startsWith("-")){
					Whereclause=Whereclause+"Name like '%"+namesearch.split(" ")[i]+"%'";
				} else {
					Whereclause=Whereclause+"Name not like '%"+namesearch.split(" ")[i].substring(1)+"%'";
				}
			}
			if (!vintagesearch.equals("")){
				vintagesearch=getVintageClause(vintagesearch);
				if (!vintagesearch.equals("")) Whereclause=Whereclause+" AND ("+vintagesearch+")";

			}

			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempselect` (`vintage` char(12) NOT NULL,  `size` Double NOT NULL,  `priceeuroex` Double NOT NULL,  KEY `Allcolumns` (`Vintage`,`Size`,`Priceeuroex`)) ENGINE = MEMORY;", winecon);
			Dbutil.executeQuery("insert into tempselect select vintage,size,min(priceeuroex) from wineview where "+Whereclause+" GROUP BY size, vintage;", winecon);
			Query="select * from wineview natural join tempselect where "+Whereclause;

			if (!country.equals("All")){
				Query=Query+" AND Country in ('"+country+"')";
			}
			if (cheapest.equals("true")){
				Query=Query+" AND wineview.size>0 ";
			}
			if (pricemin>0)	{
				Query=Query+" AND priceeuroex >= "+pricemin;
			}
			if (pricemax>0)	{
				Query=Query+" AND priceeuroex <= "+pricemax;
			}
			if (created!=null && !created.equals("%")){
				Query=Query+" AND createdate >= '"+created+"'";
			}
			if (rareold){
				Query=Query+" AND rareold=true";
			}
			rs = Dbutil.selectQuery(Query,winecon);
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			Wine = new Wine[count];
			int i=0;
			while (rs.next()){
				this.Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("PriceEuroIn"), rs.getDouble("CPC"), rs.getString("sourceURL"),
						rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),null);
				if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
				if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
				i++;
			}

			rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",winecon);
			rs.next();
			records=rs.getInt("records");
			Dbutil.executeQuery("Drop table tempselect;", winecon);
			Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+Query+" executed.");


		}catch( Exception e ) {
			Dbutil.logger.error("Problem during selection of cheapest free text search",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);
		}
	}//end main

	public Wineset(String region, String vintagesearch, String created, float pricemin, float pricemax, float size,String country, boolean rareold, String order, int offset, int numberofrows, String cheapest){
		// Special search method to find wines in a wine region
		String Query;
		String regionlist;
		ResultSet rs=null;
		Connection winecon=Dbutil.openNewConnection();
		try {
			regionlist=Region.getRegionsAsIntList(region);
			String Whereclause;
			Whereclause="";

			Whereclause=Whereclause+" wineview.region in ("+regionlist+") ";

			//if (size>0){
			//Whereclause=Whereclause+" AND size="+size;

			//}

			if (!vintagesearch.equals("")){
				vintagesearch=getVintageClause(vintagesearch);
				if (!vintagesearch.equals("")) Whereclause=Whereclause+" AND ("+vintagesearch+")";

			}

			if (cheapest.equals("true")){
				Query="CREATE TEMPORARY TABLE `wijn`.`tempselect` (`Vintage` char(12) NOT NULL,  `Size` Double NOT NULL,  `PriceEuroex` Double NOT NULL, Knownwineid INTEGER not null, KEY `Allcolumns` (`Vintage`,`Size`,`PriceEuroex`,`Knownwineid`)) ENGINE = MEMORY;";
				Dbutil.executeQuery(Query, winecon);
				Query="insert into tempselect select vintage,size,min(priceeuroex),knownwineid from wineview where "+Whereclause+" GROUP BY size, vintage,knownwineid having count(*)>1;";
				Dbutil.executeQuery(Query, winecon);
				Query="select SQL_CALC_FOUND_ROWS * from wineview natural join tempselect where "+Whereclause;

				//Query="select SQL_CALC_FOUND_ROWS * from wineview join (select knownwineid,size,vintage,min(priceeuroex) as minprice from wineview where "+Whereclause+" group by knownwineid, vintage,size) as minprices on wineview.vintage=minprices.vintage and priceeuroin=minprices.minprice and wineview.size=minprices.size and wineview.knownwineid=minprices.knownwineid where "+Whereclause+" ";
			} else {
				Query="select SQL_CALC_FOUND_ROWS * from wineview where "+Whereclause+" ";
			}
			if (!country.equals("All")){
				Query=Query+" AND Country ='"+country+"'";
			}
			if (pricemin>0)	{
				Query=Query+" AND priceeuroex >= "+pricemin;
			}
			if (pricemax>0)	{
				Query=Query+" AND priceeuroex <= "+pricemax;
			}
			if (created!=null && !created.equals("%")){
				Query=Query+" AND createdate >= '"+created+"'";
			}
			if (rareold){
				Query=Query+" AND rareold=true";
			}

			if (order.equals("sponsored")){
				Query=Query+ " and CPC > 0";
				Query=Query+" order by CPC desc, priceeuroex limit 5";
			} else {
				if (order!=null && !order.equals("") && order.length()>2){
					Query=Query+" order by "+order+", priceeuroex";
				} else {
					Query=Query+" order by priceeuroex";
				}
				Query=Query+" limit "+offset+","+numberofrows+";";
			}
			rs = Dbutil.selectQuery(Query,winecon);
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			Wine = new Wine[count];
			int i=0;
			while (rs.next()){
				ArrayList<Winerating> rating=null;
				if (rs.getInt("knownwineid")>0&&!filterVintage(rs.getString("vintage")).equals("")) {
					rating=new ArrayList<Winerating>();
					rating.add(new Winerating(rs.getInt("knownwineid"), rs.getInt("vintage"),"FWS"));
				}
				this.Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("PriceEuroIn"), rs.getDouble("CPC"), rs.getString("sourceURL"),
						rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),rating);
				if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
				if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
				i++;
			}

			rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",winecon);
			rs.next();
			records=rs.getInt("records");
			region=this.region;
			if (cheapest.equals("true")){
				Dbutil.executeQuery("Drop table tempselect;", winecon);
			}
			Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+Query+" executed.");


		}catch( Exception e ) {
			Dbutil.logger.error("Problem while creating wineset for region "+region,e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);
		}

	}

	public Wineset(String region, String vintagesearch, double size, String order){
		// Special search method to find the cheapest bottles for all wines in a wine region
		String Query;
		ResultSet rs=null;
		ResultSet rs2=null;
		String regionlist;
		Connection winecon=Dbutil.openNewConnection();
		try {
			regionlist=Region.getRegionsAsIntList(region);
			String vintagestring=vintagesearch;
			String Whereclause;
			Whereclause="";

			Whereclause=Whereclause+" wineview.region in ("+regionlist+") ";

			//if (size>0){
			//Whereclause=Whereclause+" AND size="+size;

			//}

			if (!vintagesearch.equals("")){
				vintagesearch=getVintageClause(vintagesearch);
				if (!vintagesearch.equals("")) Whereclause=Whereclause+" AND ("+vintagesearch+")";

			}

			if (size>0) Whereclause+=" and size="+size;

			Query="select wineview.*,min(priceeuroex) as minimum, kbregionhierarchy.lft as lft, kbregionhierarchy.region as regionname, knownwines.wine as wine  from wineview join kbregionhierarchy on (wineview.region=kbregionhierarchy.id) join knownwines on (wineview.knownwineid=knownwines.id) where "+Whereclause+" group by wineview.knownwineid,wineview.vintage ";


			if (order!=null &&order.equals("sponsored")){
				Query=Query+ " and CPC > 0";
				Query=Query+" order by CPC desc, priceeuroex limit 5";
			} else {
				if (order!=null && !order.equals("") && order.length()>2){
					Query=Query+" order by regionname,"+order+", wine,vintage;";
				} else {
					Query=Query+" order by regionname, wine,vintage;";
				}

			}
			rs = Dbutil.selectQuery(Query,winecon);
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			Wine = new Wine[count];
			int i=0;
			Query="CREATE TEMPORARY TABLE `wijn`.`tempratingaverage` (`rating` Integer, `PriceEuroex` DECIMAL(10,2) NOT NULL) ENGINE = MEMORY;";
			Dbutil.executeQuery(Query, winecon);
			while (rs.next()){
				this.Wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("wine")), filterVintage(rs.getString("vintage")), rs.getFloat("Size"), rs.getFloat("PriceEuroEx"), rs.getDouble("PriceEuroEx"), rs.getDouble("minimum"), rs.getDouble("CPC"), rs.getString("sourceURL"),
						rs.getInt("shopid"), rs.getString("shopname"), rs.getString("shopUrl"), rs.getString("createDate"), "", rs.getString("id"), false,rs.getString("Country"),rs.getInt("knownwineid"),null);
				this.Wine[i].Region=rs.getString("regionname");
				this.Wine[i].Ratings=new ArrayList<Winerating>();
				if (!"".equals(rs.getString("knownwineid"))&&!"".equals(filterVintage(rs.getString("vintage")))){
					rs2=Dbutil.selectQuery("select * from ratedwines where knownwineid="+rs.getString("knownwineid")+" and vintage="+filterVintage(rs.getString("vintage"))+";",winecon);
					while (rs2.next()) {
						this.Wine[i].Ratings.add(new Winerating(rs2.getString("author"),rs2.getDouble("rating"),rs2.getDouble("ratinghigh")));
					}
					rs2.getStatement().close();
					int n=0;
					Double rating=0.0;
					Query="select * from ratedwines where rating > 80 and rating <101 and knownwineid="+rs.getString("knownwineid")+" and vintage="+filterVintage(rs.getString("vintage"))+";";
					rs2=Dbutil.selectQuery(Query,winecon);
					while (rs2.next()) {
						rating+=rs2.getDouble("rating");
						if (rs2.getDouble("ratinghigh")>0){
							rating+=rs2.getDouble("ratinghigh");
						} else {
							rating+=rs2.getDouble("rating");
						}
						n=n+2;
					}
					if (n>0) rating=rating/n;
					if (rating>0){
						Query="insert into tempratingaverage (rating,priceeuroex) values ("+Math.round(rating)+","+rs.getDouble("minimum")+");";
						Dbutil.executeQuery(Query,winecon);
					}
					rs2.getStatement().close();

				}
				if (rs.getInt("vintage")>0) vintages.add(rs.getInt("vintage"));
				if (rs.getFloat("size")>0) sizes.add(rs.getFloat("size"));
				i++;
			}

			Double price=0.0;
			Double[] averageprice=new Double[101];
			for (int j=100;j>80;j=j-1){
				rs=Dbutil.selectQuery("select avg(priceeuroex) as average from tempratingaverage where rating="+j+" group by rating;",winecon);
				if (rs.next()){
					if (price==0.0||price>rs.getDouble("average")) price=rs.getDouble("average");
					averageprice[j]=price;
				}
			}

			int rated=1;
			int recom=0;
			double limit=0.5;
			while (limit<1.01&&((double)recom/rated<0.3)){
				// See which ones are good value for money and mark them as recommended
				rated=0;
				recom=0;
				for (int j=0;j<Wine.length;j++){
					if (Wine[j].getAverageRating()>80){
						rated++;
						if (Wine[j].PriceEuroEx<limit*averageprice[Wine[j].getAverageRating()])
						{
							recom++;
							Wine[j].recommended=true;
						}
					}

				}
				limit+=0.1;
			}
			records=i;
			region=this.region;
			Dbutil.logger.debug(new Timestamp(new java.util.Date().getTime())+" Websearch "+Query+" executed.");


		}catch( Exception e ) {
			Dbutil.logger.error("Problem while creating wineset for region "+region,e);
		} finally {
			Dbutil.executeQuery("Drop table tempratingaverage;", winecon);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(winecon);
		}
	}




	public static void analyzeTips(){
		// Special search method to find new knownwines that are cheaper than before
		ResultSet rs=null;
		ResultSet rs2=null;
		String Query;
		Connection winecon=Dbutil.openNewConnection();
		Connection con=Dbutil.openNewConnection();
		try {
			Dbutil.logger.info("Starting job to analyze tips");
			/*
			Query="CREATE TEMPORARY TABLE `wijn`.`tempselect` (`Vintage` char(12) NOT NULL,  `Size` DECIMAL(5,3) NOT NULL,  `PriceEuroex` Double NOT NULL, Knownwineid INTEGER not null, KEY `Allcolumns` (`Vintage`,`Size`,`PriceEuroex`,`Knownwineid`)) ENGINE = MEMORY;";
			Dbutil.executeQuery(Query, winecon);
			Query="insert into tempselect select vintage,size,min(priceeuroex),knownwineid from wineview where knownwineid>0 and size >0 and vintage>0 GROUP BY size, vintage,knownwineid having count(*)>2;";
			Dbutil.executeQuery(Query, winecon);
			Query="Delete from tips;";
			Dbutil.executeQuery(Query,winecon);
			Query="insert ignore into tips select wineview.knownwineid,wineview.vintage,wineview.size,wineview.priceeuroex,'0',regions.id,wineview.id from wineview natural join tempselect join knownwines on (wineview.knownwineid=knownwines.id) join regions on (knownwines.appellation=regions.region) where  createdate >= curdate() order by size,price, priceeuroex";
			Dbutil.executeQuery(Query,winecon);

			Dbutil.executeQuery("Drop table tempselect;", winecon);
			 */
			Query="Delete from tips;";
			Dbutil.executeQuery(Query, winecon);
			Query="insert ignore into tips select distinct wines.knownwineid,wines.vintage,size,wines.priceeuroex,'0',kbregionhierarchy.id,wines.id,bestprices.continent from wines join kbregionhierarchy on (wines.lft=kbregionhierarchy.lft) join bestprices on (wines.knownwineid=bestprices.knownwineid and wines.vintage=bestprices.vintage and bestprices.continent='EU' and wines.priceeuroex=bestprices.priceeuroex) where createdate>date_sub(curdate(),interval 12 hour)  and bestprices.n>2 order by wines.vintage;";
			Dbutil.executeQuery(Query, winecon);
			Query="insert ignore into tips select distinct wines.knownwineid,wines.vintage,size,wines.priceeuroex,'0',kbregionhierarchy.id,wines.id,bestprices.continent from wines join kbregionhierarchy on (wines.lft=kbregionhierarchy.lft) join bestprices on (wines.knownwineid=bestprices.knownwineid and wines.vintage=bestprices.vintage and bestprices.continent='UC' and wines.priceeuroex=bestprices.priceeuroex) where createdate>date_sub(curdate(),interval 12 hour)  and bestprices.n>2 order by wines.vintage;";
			Dbutil.executeQuery(Query, winecon);
			Query="select * from tips;";
			rs=Dbutil.selectQuery(Query, winecon);
			while (rs.next()){
				String query ="select min(priceeuroex) as minprice from wines join shops on (wines.shopid=shops.id) join vat on (shops.countrycode=vat.countrycode) where knownwineid="+rs.getString("knownwineid")+" and vintage="+filterVintage(rs.getString("vintage"))+" and size="+rs.getDouble("size")+" and priceeuroex>("+rs.getString("lowestprice")+"+0.01) and vat.continent='"+rs.getString("continent")+"' limit 1;";
				rs2=Dbutil.selectQuery(query, con);
				rs2.next();
				if (rs2.getDouble("minprice")>0){
					Dbutil.executeQuery("update tips set nextprice="+rs2.getDouble("minprice")+" where knownwineid="+rs.getString("knownwineid")+" and vintage="+filterVintage(rs.getString("vintage"))+" and lowestprice="+rs.getString("lowestprice")+";");		
				}
				rs2.getStatement().close();


			}
			Dbutil.executeQuery("delete from tips where nextprice=0.0;");
			Dbutil.logger.info("Finished analyzing tips");

		}catch( Exception e ) {
			Dbutil.logger.error("Problem while analyzing tips.",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(winecon);
			Dbutil.closeConnection(con);
		}
	}

	/*public static void analyzeWineStatistics(){
		// Gather statistics about the lowest price and avarage rating of a wine
		String query;
		try {
			Dbutil.logger.info("Starting job to analyze wine statistics");
			Dbutil.executeQuery("delete from winestatistics");
			query="insert into winestatistics  (knownwineid,vintage,regionlft,regionrgt,lowestpriceeuroin,ratingavg) select wineview.knownwineid, wineview.vintage, regions.lft, regions.rgt, min(priceeuroin) as minpriceeuroin, avg(ratings.rating) from wineview join knownwines on (wineview.knownwineid=knownwines.id) join ratings on (knownwines.id=ratings.knownwineid and wineview.vintage=ratings.vintage and ratings.author='RP') join regions on (regions.region=knownwines.appellation) where size=0.75 and ratings.author='RP' group by knownwineid, vintage;";
			Dbutil.executeQuery(query);
			Dbutil.logger.info("Finished analyzing wine statistics");

		}catch( Exception e ) {
			Dbutil.logger.error("Problem while analyzing wine statistics. ",e);
		}
	}
	 */
	public static void logWebSearch(String namesearch, String vintagesearch, int created, float pricemin, float pricemax, String country, boolean rareold, String ip){
		String countryip=Webroutines.getCountryCodeFromIp(ip);
		java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime()); 

		Dbutil.executeQuery("Insert into searchlog (date, name, vintage, pricemin, pricemax, country, created, rareold, ip, countryip) values ('"+now+"','"+namesearch+"','"+vintagesearch+
				"','"+pricemin+"','"+pricemax+"','"+country+"','"+created+"',"+rareold+",'"+ip+"','"+countryip+"');");	
	}

	public static String filterVintage(String vintage){
		if (vintage!=null&&vintage.equals("0")) vintage="";
		return vintage;
	}

	public static String getVintageClause(String vintagesearch){
		String vintagestring=vintagesearch;
		String[] vintages;
		String[] vintagerange;
		String from;
		String to;
		vintagestring=vintagestring.replaceAll(", "," ");
		vintagestring=vintagestring.replaceAll(","," ");
		vintages=vintagestring.split(" ");
		String vintageclause="";
		if (!vintagesearch.equals("")){
			for (int i=0; i<vintages.length;i++){
				if (!vintages[i].equals("")){
					if (!vintageclause.equals("")){
						vintageclause=vintageclause+" OR ";
					}
					if (vintages[i].indexOf("-")>-1){
						from="0";
						to="0";
						vintagerange=vintages[i].split("-");
						if (vintagerange.length<2){
							to="3000";
						} else {
							to=vintagerange[1];
						}
						if ((vintagerange.length<1)||(vintagerange[0].equals(""))) {
							from="1";
						} else {
							from=vintagerange[0];
						}
						vintageclause=vintageclause+"wineview.Vintage between "+from+
						" AND "+to;
					} else {
						vintageclause=vintageclause+"wineview.Vintage="+vintages[i];
					}
				}
			}

		}
		return vintageclause;
	}


}




package com.freewinesearcher.common;

import java.io.File;
import java.sql.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Regioninfo;
import com.freewinesearcher.online.Webroutines;

public class RegionStatistics {
	public Region region;
	public int parent=0;
	public String regionname="";
	public Map<String,Integer> grapes=new HashMap<String, Integer>();
	public String grapejson;
	public int grapeslimit=10;
	public int producers=0;
	public int offers=0;
	public String topwines="";
	public String grapestext="";
	public Map<String,Integer> types=new LinkedHashMap<String, Integer>();
	public HashSet<String> topproducers=new LinkedHashSet<String>();
	public Set<Topwine> topwinelist=new LinkedHashSet<Topwine>();
	public String typejson;
	public int typeslimit=8;
	public String typetext="";
	public String bgcolor="#ffffff";
	
	public RegionStatistics(){
		
	}
	
	@SuppressWarnings("unchecked")
	public void getStatistics(PageHandler p){
		Connection con = Dbutil.openNewConnection();
		try {
			getGrapeStats(con);
			getTypeStats(con);
			getTopProducers(5, con);
			if (parent!=100&&region.id!=100) {
				getProducerStats(con);
				getTopWines(40,con,p);
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeConnection(con);
		}
		
	}
	
	public void getGrapeStats(Connection con){
		String query;
		ResultSet rs = null;
		StringBuffer gt=new StringBuffer();
		StringBuffer gj=new StringBuffer();
		gj.append("{\"bg_colour\":\""+bgcolor+"\",\"title\":{\"text\":  \"Grapes used in "+regionname.replaceAll("'", "&apos;")+"\",\"style\": \"{font-size: 17px; color:#4d0027; font-family: Georgia; text-align: center;}\"  },\"elements\":[{\"type\":\"pie\",\"start-angle\": 0,\"radius\": 70,\"colours\":   [\"#73880A\",\"#D15600\",\"#356aa0\",\"#d01f3c\",\"#C79810\",\"#AAAAAA\"],\"alpha\":     0.6,\"stroke\":    2,\"onclick\":	\"pieclick\",\"animate\":   1,\"tip\": \"#label#: #percent#\",\"values\" :   [");
		
		try {
			query = "select count(*) as thesum, grapes from knownwines where lft>="+region.lft+" and rgt<="+region.rgt+" and numberofwines>0 group by grapes order by sum(numberofwines) desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			int total=0;
			while (rs.next()) {
				grapes.put(rs.getString("grapes"),rs.getInt("thesum"));
				total+=rs.getInt("thesum");
			}
			Iterator<String> it = Webroutines.sortByValue(grapes,true).keySet().iterator();
			String grape;
			int n=0;
			int other=0;
			while (it.hasNext()){
				n++;
				grape=it.next();
				if (n<grapeslimit){
					gj.append((n>1?",":"")+"{\"value\":"+grapes.get(grape)+",\"label\":\""+grape.replaceAll("'", "&apos;")+"\""+",\"on-click\":\"setGrapegotoGuide(\\'"+(grape).replaceAll("'", "&apos;")+"\\')\"}");
					gt.append((n>1?",":"")+grape+": "+(((100*grapes.get(grape))/total)>0?((100*grapes.get(grape))/total):"&lt;1")+"%");
				} else {
					other+=grapes.get(grape);
				}
			}
			if (other>0) {
				gj.append((n>1?",":"")+"{\"value\":"+other+",\"label\":\"Other grapes\""+"}");
				gt.append(", other grapes: "+(((100*other)/total)>0?((100*other)/total):"&lt;1")+"%");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
		}
		grapestext=gt.toString();
		gj.append("]}]}");
		grapejson=gj.toString();
		
	}
	
	
	
	public void getTypeStats(Connection con){
		String query;
		ResultSet rs = null;
		StringBuffer tt=new StringBuffer();
		StringBuffer tj=new StringBuffer();
		tj.append("{\"bg_colour\":\""+bgcolor+"\",\"title\":{\"text\":  \"Wine types made  in "+regionname.replaceAll("'", "&apos;")+"\",\"style\": \"{font-size: 17px; color:#4d0027; font-family: Georgia; text-align: center;}\"  },\"elements\":[{\"type\":\"pie\",\"start-angle\": 0,\"radius\": 70,\"colours\":   [\"#73880A\",\"#D15600\",\"#356aa0\",\"#d01f3c\",\"#C79810\",\"#AAAAAA\"],\"alpha\":     0.6,\"stroke\":    2,\"onclick\":	\"pieclick\",\"animate\":   1,\"tip\": \"#label#: #percent#\",\"values\" :   [");
		try {
			query = "select count(*) as thesum, type from knownwines where lft>="+region.lft+" and rgt<="+region.rgt+" and numberofwines>0 group by type;";
			rs = Dbutil.selectQuery(rs, query, con);
			int total=0;
			while (rs.next()) {
				types.put(rs.getString("type"),rs.getInt("thesum"));
				total+=rs.getInt("thesum");
			}
			Iterator<String> it = Webroutines.sortByValue(types,true).keySet().iterator();
			String type;
			int n=0;
			int other=0;
			String reducedtype;
			while (it.hasNext()){
				n++;
				type=it.next();
				if (n<typeslimit){
					reducedtype=getreducedtype(type);
					tj.append((n>1?",":"")+"{\"value\":"+types.get(type)+",\"label\":\""+type+"\""+(reducedtype.length()>0?",\"on-click\":\"setTypegotoGuide(\\'"+Webroutines.removeAccents(reducedtype).toUpperCase()+"\\',\\'\\')\"":"")+"}");
					tt.append((n>1?", ":"")+type+": "+(((100*types.get(type))/total)>0?((100*types.get(type))/total):"&lt;1")+"%");
				} else {
					other+=types.get(type);
				}
			}
			if (other>0) {
				tj.append((n>1?",":"")+"{\"value\":"+other+",\"label\":\"Other types\""+"}");
				tt.append(", other types: "+(((100*other)/total)>0?((100*other)/total):"&lt;1")+"%");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
		}
		typetext=tt.toString();
		tj.append("]}]}");
		typejson=tj.toString();
		
	}
	
	public void getTopProducers(int limit, Connection con){
		String query;
		ResultSet rs = null;
		try {
			query = "select count(*) as thesum, producer from knownwines where lft>="+region.lft+" and rgt<="+region.rgt+" and numberofwines>0 group by producer order by count(*) desc limit "+limit;
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				topproducers.add(rs.getString("producer"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
		}
	}
	
	public void getTopWines(int limit){// used as quicker solution for mobile pages (api)
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select knownwines.*,ra.knownwineid,count(*) as ratings,count(*) as numratings,  avg(rating) as avgrating,(avg(rating)-80)*(50+count(*)) as score from knownwines join ratinganalysis ra on (knownwines.id=ra.knownwineid ) where lft>="+region.lft+" and rgt<="+region.rgt+" group by knownwineid order by avgrating desc limit "+limit;
						

			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				topwinelist.add(new Topwine(rs.getString("wine"),rs.getInt("id"),rs.getInt("numratings"),rs.getInt("avgrating"),Webroutines.getLowestPrice(rs.getInt("id"),""),Webroutines.getAveragePrice(rs.getInt("id"),"")));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		
	}

	
	private void getTopWines(int limit, Connection con, PageHandler p){
		topwines="";
		String winelink="";
		if (p!=null){
		String query;
		ResultSet rs = null;
		boolean mobile=p.mobile;
		StringBuffer sb=new StringBuffer();
		try {
			query = "select *,min(priceeuroex) as minprice, max(priceeuroex) as maxprice, count(*) as numoffers  from (select knownwines.*,ra.knownwineid,count(*) as ratings,count(*) as numratings,  avg(rating) as avgrating,(avg(rating)-80)*(50+count(*)) as score from knownwines join ratedwines ra on (knownwines.id=ra.knownwineid and rating>80) where lft>="+region.lft+" and rgt<="+region.rgt+" group by knownwines.id order by (avg(rating)-80)*(50+count(*)) desc limit "+limit+") sel join (select producer,avg(rating-80)*sum(rating-80) as prodrating from knownwines left join ratedwines ra on (knownwines.id=ra.knownwineid and rating>80) where lft>="+region.lft+" and rgt<="+region.rgt+" group by knownwines.producer order by avg(rating-80)*sum(rating-80) desc limit "+limit+")  ord on (sel.producer=ord.producer)  join wines on (wines.knownwineid=sel.id) group by sel.id order by ord.prodrating desc,sel.score desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			String lastproducer="";
			int avg;
			int stars;
			if (rs.isBeforeFirst()) sb.append("This is a list of the top wines from "+regionname+(region.rgt-region.lft>1?" or one of its subregions":"")+" based on the ratings they received from professional reviewers.<br/><br/>");
			while (rs.next()) {
				if (!rs.getString("producer").equals(lastproducer)){
					lastproducer=rs.getString("producer");
					sb.append("<h2><a href='/"+(mobile?"m":"")+"winery/"+Webroutines.URLEncodeUTF8Normalized((rs.getString("producer")))+"'>"+lastproducer+"</a></h2>");
				}
				winelink=Webroutines.winelink(Knownwines.getUniqueKnownWineName(rs.getInt("id")),0,mobile);
				//avg=rs.getInt("avg");
				stars=(int) Math.round(((rs.getDouble("avgrating")-87)*5)/6);
				if (stars>5) stars=5;
				// See if there is a label
				String labeldiv="";
				if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".jpg").exists()){
					labeldiv="<div class='winerylabel'><a href='"+winelink+"'><img class='label' src='"+Configuration.staticprefix+"/labels/"+rs.getString("id")+".jpg' alt=\""+rs.getString("wine").replaceAll("&","&apos;")+"\" onmouseover='this.parentNode.className=\"winerylabelmouseover\";' onmouseout='this.parentNode.className=\"winerylabel\";'/></a></div>";
				} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".gif").exists()){
					labeldiv="<div class='winerylabel'><a href='"+winelink+"'><img class='label' src='"+Configuration.staticprefix+"/labels/"+rs.getString("id")+".gif' alt='"+rs.getString("wine").replaceAll("&","&apos;")+"' onmouseover='this.parentNode.className=\"winerylabelmouseover\";' onmouseout='this.parentNode.className=\"winerylabel\";'/></a></div>";

				}
				sb.append("<i><a "+(mobile?" rel='external'":"")+"href='"+winelink+"'>"+rs.getString("wine").replaceAll("&", "&amp;")+"</a></i> "+(rs.getBoolean("disabled")?"<i>(This wine is disabled in our system)</i> ":" ")+" ");
				if (labeldiv.length()>0){
					sb.append(mobile?labeldiv:labeldiv);
				}
				sb.append("<br/>"+rs.getInt("numratings")+" ratings, average: "+rs.getInt("avgrating")+"/100 points. We list <a href='"+Webroutines.winelink(Knownwines.getUniqueKnownWineName(rs.getInt("id")),0,mobile)+"'>"+rs.getInt("numoffers")+" offer"+(rs.getInt("numoffers")>1?"s</a> with a price range of "+Webroutines.formatPrice(rs.getDouble("minprice"),rs.getDouble("minprice"), p.searchdata.getCurrency(), "EX")+"-"+Webroutines.formatPrice(rs.getDouble("maxprice"),rs.getDouble("maxprice"), p.searchdata.getCurrency(), "EX")+".":"</a> with a price of "+Webroutines.formatPrice(rs.getDouble("minprice"),rs.getDouble("minprice"), p.searchdata.getCurrency(), "EX"))+"<br/>");
				//for (int i=0;i<stars;i++) sb.append("<img src='/css/star.gif' title='Average rating: "+rs.getInt("avgrating")+"/100' alt='Average rating: "+rs.getInt("avgrating")+"/100'/>");
				sb.append("<br/>");
				//int n=Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="+rs.getString("id")+" group by knownwineid", "thecount");
				topwinelist.add(new Topwine(rs.getString("wine"),rs.getInt("id"),rs.getInt("numratings"),rs.getInt("avgrating"),rs.getDouble("minprice"),rs.getDouble("maxprice")));
				
			}
			
			
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
		}
		topwines=sb.toString();
		}
	}
	
	private void getProducerStats(Connection con){
		String query;
		ResultSet rs = null;
		try {
			query = "select count(distinct(producer)) as producers, sum(numberofwines) as offers from knownwines where lft>="+region.lft+" and rgt<="+region.rgt+" ;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				producers=rs.getInt("producers");
				offers=rs.getInt("offers");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
		}
	}
	
	public String getTopProducerText(){  
		String text="";
		Iterator<String>i=topproducers.iterator();
		while (i.hasNext()){
			String p=i.next();
			if (i.hasNext()){
				text+=", ";
			} else if (topproducers.size()>1){
				text+=" and ";
			}
			text+=p;
		}
		if (text.length()>2) text=text.substring(2);
		return text;
	} 
	
	public static String getreducedtype(String type){
		type=type.toLowerCase();
		if (type.contains("sparkling")) return "sparkling";
		if (type.contains("sweet")||type.contains("fortified")) {
			if (type.contains("red")) {
				return "port";
			} else {
				return "dessert";
			}
		}
		if (type.contains("white")) return "white";
		if (type.contains("red")) return "red";
		if (type.contains("ros�")) return "rose";
		return "";
	}
	
	public static class Topwine{
		public String name;
		public int id;
		public int nratings;
		public int avgrating;
		public int noffers;
		public double pricemin;
		public double pricemax;
		public Topwine(String name, int id, int nratings, int avgrating,
				 double pricemin, double pricemax) {
			super();
			this.name = name;
			this.id = id;
			this.nratings = nratings;
			this.avgrating = avgrating;
			this.pricemin = pricemin;
			this.pricemax = pricemax;
		}
		
		
		
	}

	
}
package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import winterwell.jtwitter.Twitter;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Twit;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wineset;
import com.searchasaservice.ai.Recognizer;

public class Wotd {
	private static Wotd wotd=null;
	private Map<String,WotdWine> wines=new HashMap<String,WotdWine>(); 

	private Wotd(){
		wines.put("RP",readWotdFromDb("RP"));
	}

	private static WotdWine readWotdFromDb(String source) {
		String query;
		ResultSet rs = null;
		WotdWine w=null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from wotd where source='"+source+"';";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				w=new WotdWine(source);
				w.name=rs.getString("name");
				w.vintage=rs.getInt("vintage");
				w.cost=rs.getString("cost");
				w.knownwineid=rs.getInt("knownwineid");
				w.rating=rs.getString("rating");
				w.getPrice();
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return w;

	}

	public static Wotd getInstance(){
		if (wotd==null){
			wotd=new Wotd();
		} 
		return wotd;
	}

	public void refreshAll(){
		for (String source:wines.keySet()){
			wines.get(source).refresh();
		}

	}

	public WotdWine getWine(String source){
		return wines.get(source);
	}

	public static String getHtml(String source){
		String html="";
		if ("RP".equals(source)){
			Webpage webpage=new Webpage();
			webpage.urlstring="http://www.erobertparker.com/entrance.aspx";
			webpage.readPage();
			String viewstate=Webroutines.getRegexPatternValue("id=\"__VIEWSTATE\" value=\"([^\"]+)\"", webpage.html);
			String wineid=Webroutines.getRegexPatternValue("wineID1\" value=\"(\\d+)\"", webpage.html);

			webpage.urlstring="http://www.erobertparker.com/newsearch/wotd.aspx";
			webpage.postdata="__VIEWSTATE="+viewstate+"&wotd=true&ctl00%24ContentplaceholderBottom%24ImageButton1="+wineid+"&wineID="+wineid;
			webpage.readPage();
			html=webpage.html;

		}

		return html;

	}

	public static class WotdWine{
		
		
		public WotdWine(String source) {
			super();
			this.source = source;
			if (source.equals("RP")){
				nameregex="ctl00_ContentMaster_WineNameText[^>]+>([^<]+)<";
				regionregex="ctl00_ContentMaster_Location[^>]+>([^<]+)<";
				costregex="ctl00_ContentMaster_DataList1_ctl00_Label2[^>]+>([^<]+)<";
				ratingregex="ctl00_ContentMaster_DataList1_ctl00_Label7[^>]+><a href[^>]+>([\\d-+ ]+)</a";
			}
		}

		public String source;
		String nameregex;
		String regionregex;
		String costregex;
		String ratingregex;
		public String name;
		public String rating;
		public String region;
		public String composedname;
		public String cost;
		public int knownwineid;
		public int vintage=0;
		//public double priceeuroinEU;
		public double priceeuroexEU;
		//public double priceeuroinUC;
		public double priceeuroexUC;

		public void refresh(){
			String html=getHtml(source);
			String downloadedwine="";
			int id=0;
			if (!html.startsWith("Webpage")){
				downloadedwine=Webroutines.getRegexPatternValue(nameregex, html).trim()+" "+Webroutines.getRegexPatternValue(regionregex, html).trim();
				if (downloadedwine.length()>250) downloadedwine=downloadedwine.substring(0,250);
				if (!downloadedwine.equals(readWotdFromDb(source))){
					name=downloadedwine;
					try {vintage=Integer.parseInt(Webroutines.getVintageFromName(name).trim());}catch (Exception e){}
					cost=Webroutines.getRegexPatternValue(costregex, html);
					rating=Webroutines.getRegexPatternValue(ratingregex, html);
					id=getRow();
					if (id==0){
						Dbutil.executeQuery("delete from wotd where source='"+source+"';");
						int knownwineid=Dbutil.readIntValueFromDB("select * from ratedwines where author='"+source+"' and name='"+Spider.SQLEscape(name.replaceAll(vintage+"", "").trim())+"';", "knownwineid");
						Dbutil.executeQuery("insert into wotd(source,date,name,cost,knownwineid,vintage,rating) values ('"+source+"',SYSDATE(),'"+Spider.SQLEscape(name)+"','"+Spider.SQLEscape(cost)+"',"+knownwineid+","+vintage+",'"+Spider.SQLEscape(rating)+"');");
						id=getRow();
						if (knownwineid==0) Recognizer.recognizeWotd();
					}
					knownwineid=Dbutil.readIntValueFromDB("select * from wotd where id="+id, "knownwineid");
					getPrice();
					Wotd.getInstance().wines.put(source, this);
					
					
				}
			}
		}
		
		

		public void getPrice(){
			//priceeuroinEU=0;
			priceeuroexEU=0;
			//priceeuroinUC=0;
			priceeuroexUC=0;
			if (knownwineid>0){
				String query;
				ResultSet rs = null;
				Connection con = Dbutil.openNewConnection();
				try {
					query = "select * from bestprices where knownwineid="+knownwineid+"  and vintage="+vintage;
					rs = Dbutil.selectQuery(rs, query, con);
					while (rs.next()) {
						if ("EU".equals(rs.getString("continent"))){
							//priceeuroinEU=rs.getDouble("priceeuroin");
							priceeuroexEU=rs.getDouble("priceeuroex");
						}
						if ("UC".equals(rs.getString("continent"))){
							//priceeuroinUC=rs.getDouble("priceeuroin");
							priceeuroexUC=rs.getDouble("priceeuroex");
						}
					}
				} catch (Exception e) {
					Dbutil.logger.error("", e);
				} finally {
					Dbutil.closeRs(rs);
					Dbutil.closeConnection(con);
				}

			}

		}
		
		public int getRow(){
			int n=Dbutil.readIntValueFromDB("select id from wotd where source='"+source+"' and name='"+Spider.SQLEscape(name)+"' and cost='"+Spider.SQLEscape(cost)+"';", "id");
			return n;
		}


	}
}
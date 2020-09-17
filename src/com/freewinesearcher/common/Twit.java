package com.freewinesearcher.common;

import java.sql.Connection;
import java.sql.ResultSet;

import winterwell.jtwitter.Twitter;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.online.Webroutines;
import com.freewinesearcher.online.Wotd;
import com.freewinesearcher.online.Wotd.WotdWine;

public class Twit {
	
	public static void twitter(String message){
		Twitter twitter=new Twitter(Configuration.Twitterusername,Configuration.Twitterpassword);
		twitter.setStatus(message);
	}
	
	public static void Wotd(){
		try{
			Wotd.getInstance().refreshAll();
			WotdWine wotd = Wotd.getInstance().getWine("RP");
			Twitter twitter=new Twitter(Configuration.Twitterusername,Configuration.Twitterpassword);
			String shortname=wotd.name;
			if (shortname.indexOf(",")>0) shortname=shortname.substring(0, shortname.indexOf(","));
			if (wotd.priceeuroexEU>0) {
				String url=Shorturl.shorten("https://www.vinopedia.com/wine/"+Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(wotd.knownwineid))+"?vintage="+wotd.vintage+"&country=EU");
				twitter.setStatus("Parker WOTD: "+Spider.escape(shortname)+(wotd.rating.equals("")?". ":", "+(wotd.rating+"pts. "))+(wotd.priceeuroexEU>0?"At &euro;"+Webroutines.formatPriceNoCurrency(wotd.priceeuroexEU,wotd.priceeuroexEU,"EUR","EX")+" in EU, see "+url:"estimated cost "+wotd.cost));
			}
			if (wotd.priceeuroexUC>0) {
				String url=Shorturl.shorten("https://www.vinopedia.com/wine/"+Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(wotd.knownwineid))+"?vintage="+wotd.vintage+"&country=UC");
				twitter.setStatus("Parker WOTD: "+Spider.escape(shortname)+(wotd.rating.equals("")?". ":", "+(wotd.rating+"pts. "))+(wotd.priceeuroexUC>0?"At US$"+Webroutines.formatPriceNoCurrency(wotd.priceeuroexUC,wotd.priceeuroexUC,"USD","EX")+" in US, see "+url:"estimated cost "+wotd.cost));
			}
		} catch (Exception e){
			Dbutil.logger.error("Could not update WOTD.",e);
		}
	}

	public static void tips(int limit){
		Twitter twitter=new Twitter(Configuration.Twitterusername,Configuration.Twitterpassword);
		String html="";
		ResultSet rs=null;
		String query;
		String name;
		String link;
		String regionwhereclause="";
		Connection con=Dbutil.openNewConnection();
		try{
			query="Select tips.*, knownwines.appellation, typeid from tips join knownwines on (tips.knownwineid=knownwines.id) "+regionwhereclause+" natural left join winetypecoding where (lowestprice/nextprice>0.5) and lowestprice<=100 and size=0.75 order by (lowestprice/nextprice) limit "+limit+";";
			rs=Dbutil.selectQuery(query, con);
			int i=1;
			while(rs.next()){
				name=Knownwines.getKnownWineName(rs.getInt("knownwineid"));
				link=Webroutines.URLEncode(Knownwines.getKnownWineName(rs.getInt("knownwineid"))+" "+rs.getString("Vintage"));
				String[] type=Knownwines.getGlassImageFromWineTypeCode(rs.getInt("typeid"));
				int rating=Winerating.getRating(rs.getInt("knownwineid"), rs.getInt("vintage"));
				twitter.setStatus("Vinopedia Tip "+i+": "+Spider.escape(name)+" "+rs.getInt("vintage")+(rating>70?", "+rating+"/100 pts":"")+" from �"+Webroutines.formatPrice(rs.getDouble("nextprice"))+" for �"+Webroutines.formatPrice(rs.getDouble("lowestprice"))+" on www.vinopedia.com");
				i++;
			}
		} catch (Exception exc){
			Dbutil.logger.error("Error while getting tips HTML.",exc);
			html="";
		}finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}



	public static void main(String[] args){
		Twit.Wotd();
	}

}
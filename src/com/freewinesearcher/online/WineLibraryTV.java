package com.freewinesearcher.online;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Webpage;
import com.searchasaservice.ai.Recognizer;


public class WineLibraryTV {
	public String url="";
	public String alt="";

	public WineLibraryTV(int knownwineid,String vintagestring){
		int vintage=0;
		try{
			vintage=Integer.parseInt(Webroutines.getRegexPatternValue("^(\\d\\d\\d\\d)$",vintagestring));
		}catch (Exception e){}
		String query="";
		String vintageclause="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			if (vintage>0) {
				vintageclause=" and vintage="+vintage;
			}
			query="select * from winelibrarytv where knownwineid="+knownwineid+vintageclause+";";
			rs=Dbutil.selectQuery(query, con);
			if (rs.next()){
				url=rs.getString("link");
				alt="Watch Gary V tasting "+Webroutines.formatCapitals(rs.getString("name"))+" "+rs.getString("vintage")+" on Wine Library TV";
				alt=alt.replace("'", "&apos;");
			}

		}catch(Exception e){
			Dbutil.logger.error("Eror while retrieving WLTV episode",e);
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
	}

	public static void update(){
		Dbutil.logger.info("Starting job to update Wine Library TV wines");
		ripInfo();
		Dbutil.executeQuery("update winelibrarytv set knownwineid=0;");
		if (Configuration.newrecognitionsystem){
			Recognizer.recognizeWLTV();
		}else {
			Knownwines.Knownwinesprecise(0, false, false,"winelibrarytv");
			Knownwines.updateWLTVKnownWines();
			Knownwines.Knownwinesprecise(0, true, false,"winelibrarytv");
			Knownwines.updateWLTVKnownWines();
		}
		Dbutil.logger.info("Finished job to update Wine Library TV wines");
	}

	public static void ripInfo(){
		Webpage webpage=new Webpage();
		String[] lines;
		String[] fields;
		String wine="";
		String nameregex=">([^<]+)</";
		String regionregex=">([^<]+)</td>";
		String vintageregex=">(\\d\\d\\d\\d)<";
		String urlregex="<a href=\"(http://[^\"]+)\"";
		int vintage=0;
		String url="";
		webpage.urlstring="http://spreadsheet.winelibrary.com/";
		webpage.readPage();
		int lastpage=0;
		try{
			lastpage=Integer.parseInt(Webroutines.getRegexPatternValue("(\\d+)</a> <a href=\"/\\?page=2\" class=\"next_page\"" , webpage.html));
		} catch (Exception e){}
		if (lastpage==0){
			Dbutil.logger.error("Could not update Winelibrary TV");
		} else {
			for (int page=1;page<=lastpage;page++){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {

				}
				webpage.urlstring="http://spreadsheet.winelibrary.com/?page="+page;
				webpage.readPage();
				lines=webpage.html.split("<tr");
				for (String line:lines){
					fields=line.split("<td");
					if (fields.length>6){
						wine=Webroutines.getRegexPatternValue(nameregex,fields[5])+" "+Webroutines.getRegexPatternValue(regionregex,fields[6]);
						vintage=0;
						try{
							vintage=Integer.parseInt(Webroutines.getRegexPatternValue(vintageregex,fields[4]));
						}catch (Exception E){}
						url=Webroutines.getRegexPatternValue(urlregex,fields[2]);
						if (!"".equals(wine)&&!"".equals(url)){
							Dbutil.executeQuery("insert ignore into winelibrarytv (link,name,vintage) values (" +
									"'"+Spider.SQLEscape(url)+"',"+
									"'"+Spider.SQLEscape(wine)+"',"+
									vintage+");");
						}
					}
				}
			}
		}
	}
}

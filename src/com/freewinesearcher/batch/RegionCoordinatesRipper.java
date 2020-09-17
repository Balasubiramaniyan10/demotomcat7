package com.freewinesearcher.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.freewinesearcher.common.Dbutil;

public class RegionCoordinatesRipper {
	public RegionCoordinatesRipper() {
		Connection con=Dbutil.openNewConnection();
		Dbutil.executeQuery("delete from regioncoordinates;", con);
		try{ 
			// Now let's scrape as long as we find records with status Ready
			// The Scrapelist loop gets reinitialized so we see new URL's
			File files = new File("C:\\Workspace\\RegionCoordinates\\regions.kml");
		    byte[] fileasbytes;
			String filename;
			String Page;
		    	BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(files),"UTF-8"));
				String inputLine;
				StringBuffer sb=new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
					
				}
				in.close();
				Page=sb.toString();
				Page=Page.replace("\r", "");
		
				Matcher matcher;
				Pattern pattern;
				pattern=Pattern.compile("<name>([^<]+)</name>[^<]+<Point>[^<]+<altitudeMode>[^<]+</altitudeMode>[^<]+<coordinates>([^<,]+),([^<,]+),[^<,]+</coordinates>",Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
				matcher = pattern.matcher(Page);
				while (matcher.find()){
					Dbutil.executeQuery("insert into regioncoordinates(region,lat,lon) values ('"+Spider.SQLEscape(Spider.unescape(matcher.group(1)))+"',"+matcher.group(3)+","+matcher.group(2)+");", con);
					//System.out.println(Spider.unescape(matcher.group(1))+matcher.group(2)+matcher.group(3));
				}
			
		} catch (Exception exc){
			Dbutil.logger.error("Exception while processing WineSearcher pages. ",exc);
		}
		Dbutil.closeConnection(con);
	
	}
	
}

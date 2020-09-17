package com.freewinesearcher.online;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Zipper;

public class StoreInfo  extends HttpServlet{
	private static HashMap<Integer,byte[]> stores=new HashMap<Integer,byte[]>();
	private static String KML="";

	public static Shop getStore(int shopid){
		if (stores.get(shopid)==null) {
			Shop shop=new Shop(shopid);
			shop.getShopInfo();
			stores.put(shopid, Zipper.zipObjectToBytes(shop));
		}
		return  (Shop)Zipper.unzipObjectFromBytes(stores.get(shopid));
	}
	
	public static void clearCache(){
		stores.clear();
	}
	public static void renewCache(){
		Dbutil.logger.info("Refreshing shop cache");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select id from shops where disabled=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				clearCache(rs.getInt("id"));
				getStore(rs.getInt("id"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		Dbutil.logger.info("Finished refreshing shop cache");
		
	}
	
	public static void clearCache(int shopid){
		stores.remove(shopid);
		
	}

	public static String getKML() {
		if (KML.equals("")) getKML();
		return KML;
	}

	public static void refreshKML() {
		new Configuration();
		StringBuffer KML=new StringBuffer(); 
		KML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>");
		//zipOutputStream.write(("<Style id=\"noPlacemark\"><IconStyle><Icon><href>"+Configuration.staticprefix+"/images/smallgreendot32.gif</href></Icon></IconStyle></Style>").getBytes("UTF-8"));
		String pref=(new Configuration()).getPrefix(); 
		KML.append(("<Style id=\"normalPlacemark\"><IconStyle><Icon><href>"+pref+"/images/bluemarker.gif?v=2</href></Icon></IconStyle></Style>"));
		KML.append(("<Style id=\"highlightPlacemark\"><IconStyle><Icon><href>"+pref+"/images/greenmarker.gif</href></Icon></IconStyle></Style>"));
		
		Dbutil.logger.info("Refreshing shop KML");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select *,lastnewwine>now()-interval 7 day as new from shops where disabled=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				if (rs.getDouble("lat")!=0.0){
					KML.append("<Placemark><styleUrl>"+(rs.getBoolean("new")?"#highlighPlacemark":"#normalPlacemark")+"</styleUrl><name><![CDATA["+rs.getString("shopname").replaceAll("&", "&amp;")+"]]></name><description><![CDATA[<div class='vpmarker' id='shop"+rs.getInt("id")+"'><a href='https://www.vinopedia.com/store/"+rs.getString("shopname").replaceAll("&", "&amp;").replaceAll(" ", "+")+"/'>Visit "+rs.getString("shopname").replaceAll("&", "&amp;")+"</a></div>]]></description><Point><coordinates>"+rs.getDouble("lon")+","+rs.getDouble("lat")+"</coordinates></Point></Placemark>\n");
					//KML.append("<h3 onclick=\"map.openInfoWindow(new GLatLng("+rs.getDouble("lat")+","+rs.getDouble("lon")+"),document.createTextNode('"+rs.getString("shopname").replaceAll("&", "&amp;")+"'));\">"+rs.getString("shopname").replaceAll("&", "&amp;")+"</h3>\n");
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			KML.append("</Document></kml>");
		}
		Dbutil.logger.info("Finished refreshing shop KML");
		StoreInfo.KML=KML.toString(); 
		
		
	}
	public void service(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException   {
		response.setContentType("application/vnd.google-earth.kmz");
		ZipEntry entry = new ZipEntry("data.kml");
		ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
		zipOutputStream.setLevel(9);
		zipOutputStream.putNextEntry(entry);
		zipOutputStream.write(getKML().getBytes("UTF-8"));
		zipOutputStream.closeEntry();
		zipOutputStream.close();
	}
	
}

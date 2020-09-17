package com.freewinesearcher.online;

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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
/**
 * @author Jasper
 *
 */
public class Producers {
	public Set<Producer> producer=new LinkedHashSet<Producer>();
	public double avgrelprice=0;
	public int numberofproducers=0;
	public int maxproducers=10000;
	public int records=0;
	public enum sortoptions {NWINES,PARKERAVG,PLUS95}
	public sortoptions sort=sortoptions.PLUS95;

	public Producers(Bounds bounds){
		if (bounds!=null){
			String query="";
			ResultSet rs=null;
			Connection con=Dbutil.openNewConnection();

			try{
				query="select SQL_CALC_FOUND_ROWS * from kbproducers where lat<"+bounds.latmax+" and lat>"+bounds.latmin+" and lon<"+bounds.lonmax+" and lon>"+bounds.lonmin+" order by score desc limit 90;";
				rs=Dbutil.selectQuery(query, con);
				//Dbutil.logger.info(query);
				if (rs.last()){
					numberofproducers=rs.getRow()+1;
					rs.beforeFirst();
				}
				if (numberofproducers<maxproducers+1){
					while (rs.next()){
						producer.add(new Producer(rs.getInt("id")));

					}
				}
				Dbutil.closeRs(rs);
				rs=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",con);
				if (rs.next())	records=rs.getInt("records");
			} catch (Exception exc){
				Dbutil.logger.error("Problem while looking up producers",exc);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}	
	}


	public Producers(int knownwineid){
		try{
			String[] prodids=Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";","producerids").split(",");
			for (String p:prodids){
				try {
					producer.add(new Producer(Integer.parseInt(p)));
				} catch (Exception e) {}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up producers",exc);
		}
	}
	
	public Producers(ArrayList<Integer> prodids){
		Iterator<Integer> id = prodids.iterator();
		while (id.hasNext()) {
			producer.add(new Producer(id.next()));
		}

	}
	public Producers(String prodids){
		if (prodids!=null) for (String idstr:prodids.split(",")){
			try{
			int id=Integer.parseInt(idstr);
			producer.add(new Producer(id));
			}catch(Exception e){}
		}
	}

	public Producers(String[] prodids){
		if (prodids!=null) for (String idstr:prodids){
			try{
			int id=Integer.parseInt(idstr);
			producer.add(new Producer(id));
			}catch(Exception e){}
		}
	}

	
	
	public StringBuffer getAsKml(){
		StringBuffer sb=new StringBuffer();
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document>  <name>The Googleplex</name>  <description><![CDATA[Photos of life at Google Headquarters]]></description>  <Style id=\"style8\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style3\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style23\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style16\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style24\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style21\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style15\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style10\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style9\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style20\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style7\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style13\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style14\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style18\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style17\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style6\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style1\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style25\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style22\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style4\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style12\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style2\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style19\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style11\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style5\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Placemark>    <name>Welcome to the Googleplex building 43!</name>    <description><![CDATA[<IMG height=\"288\" src=\"http://lh6.google.com/image/mapshop.maps/RhCAmQmEHdI/AAAAAAAABNs/wjDQJqi-mHc/IMGP0241.JPG?imgmax=288\" width=\"205\">]]></description>    <styleUrl>#style8</styleUrl>    <Point>      <coordinates>-122.084038,37.421738,0.000000</coordinates>    </Point>  </Placemark>");
		int i=0;
		for (Producer p:producer){
			i++;
			if (i<1000)	sb.append("<Placemark><styleUrl>#normalPlacemark</styleUrl><name><![CDATA["+p.name+"]]></name><description><![CDATA[<div class='vpmarker' id='123'>Veisit "+p.name+"</div>]]></description><Point><coordinates>"+p.lon+","+p.lat+"</coordinates></Point></Placemark>");
		}

		//Dbutil.logger.info(sb);
		return sb;
	}

	public StringBuffer getAsHtml(){
		StringBuffer sb=new StringBuffer();
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document>  <name>The Googleplex</name>  <description><![CDATA[Photos of life at Google Headquarters]]></description>  <Style id=\"style8\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style3\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style23\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style16\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style24\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style21\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style15\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style10\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style9\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style20\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style7\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style13\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style14\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style18\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style17\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style6\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style1\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style25\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style22\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style4\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style12\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style2\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style19\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style11\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style5\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Placemark>    <name>Welcome to the Googleplex building 43!</name>    <description><![CDATA[<IMG height=\"288\" src=\"http://lh6.google.com/image/mapshop.maps/RhCAmQmEHdI/AAAAAAAABNs/wjDQJqi-mHc/IMGP0241.JPG?imgmax=288\" width=\"205\">]]></description>    <styleUrl>#style8</styleUrl>    <Point>      <coordinates>-122.084038,37.421738,0.000000</coordinates>    </Point>  </Placemark>");
		int i=0;
		for (Producer p:producer){
			i++;
			if (i<1000)	sb.append("<h3 onclick=\"map.openInfoWindow(new GLatLng("+p.lat+","+p.lon+"),document.createTextNode('"+p.name+"'));\">"+p.name+"</h3>");
		}

		//Dbutil.logger.info(sb);
		return sb;
	}
	public String getAsJSON(){
		JSONArray pois=new JSONArray();
		JSONObject j;
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document>  <name>The Googleplex</name>  <description><![CDATA[Photos of life at Google Headquarters]]></description>  <Style id=\"style8\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style3\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style23\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style16\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style24\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style21\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style15\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style10\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style9\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style20\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style7\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style13\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style14\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style18\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style17\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style6\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style1\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style25\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style22\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style4\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style12\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style2\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style19\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style11\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style5\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Placemark>    <name>Welcome to the Googleplex building 43!</name>    <description><![CDATA[<IMG height=\"288\" src=\"http://lh6.google.com/image/mapshop.maps/RhCAmQmEHdI/AAAAAAAABNs/wjDQJqi-mHc/IMGP0241.JPG?imgmax=288\" width=\"205\">]]></description>    <styleUrl>#style8</styleUrl>    <Point>      <coordinates>-122.084038,37.421738,0.000000</coordinates>    </Point>  </Placemark>");
		int i=0;
		for (Producer p:producer){
			
			if (i<1000)	{
				try {
					j=new JSONObject();
					j.put("p",new JSONArray("["+p.lat+","+p.lon+"]"));
					j.put("n", p.name);
					j.put("na", Webroutines.URLEncode(Webroutines.removeAccents(p.name)));
					j.put("id", p.id);
					pois.put(i, j);
				} catch (JSONException e) {
					Dbutil.logger.error("Json problem",e);
				}
				//sb.append("<h3 onclick=\"map.openInfoWindow(new GLatLng("+p.lat+","+p.lon+"),document.createTextNode('"+p.name+"'));\">"+p.name+"</h3>");
				i++;
			}
		}

		//Dbutil.logger.info(sb);
		return pois.toString();
	}
}
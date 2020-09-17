package com.freewinesearcher.online;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.json.JSONObject;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.POI;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.common.RegionStatistics;

public class Regioninfo {
	static final long serialVersionUID=5719895;
	public int lft=0;
	public int rgt=99999;
	public int regionid;
	public int parent=0;
	String breadcrumb=""; 
	String breadcrumbtext="";
	public String regionname;
	public String producer="";
	public String regiondescription="";
	public String producerclause="";
	public String regionclause="";
	public String currentlocale="";
	public String locale="All";
	public RegionStatistics stats=new RegionStatistics();
	public Set<Producer> producers=new LinkedHashSet<Producer>();
	public boolean showmap=false;
	public Bounds bounds;
	public boolean debug=false;
	public Set<RegionPoi> subregions=new LinkedHashSet<RegionPoi>();
	public boolean hassubregions=false;
	public boolean hasproducers=false;
	public int producerlimit=80;
	public boolean mobile=false;
	public boolean newmobile=false;

	
	public Regioninfo() {
	
	}
	
	public Regioninfo(int id) {
		this.regionid=id;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			String query;
			query = "select * from kbregionhierarchy where id="+id+";";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				regiondescription=rs.getString("description");
				lft=rs.getInt("lft");
				rgt=rs.getInt("rgt");
				parent=rs.getInt("parentid");
				regionclause=" where lft>"+lft+" and rgt<"+rgt+" ";
				locale=rs.getString("region");
				regionname=rs.getString("shortregion");
			}

		} catch (Exception e){
			Dbutil.logger.error("Error while retrieving region info. ",e);

		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);	
		}

	}



	public Regioninfo(String url, boolean debug, PageHandler p) {
		this.debug=debug;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			Matcher matcher;
			Pattern pattern;
			pattern=Pattern.compile("/m?region/(.*?)(/?$|/type|/producer|/wines)");
			matcher=pattern.matcher(url);
			if (matcher.find()){
				currentlocale=(Webroutines.URLDecodeUTF8(matcher.group(1)));
				locale=currentlocale.replaceAll("/", ", ").replaceAll("%2F", "/");
			}
			String query;
			if (debug) Dbutil.logger.info("Getting info for "+locale);
			query = "select * from kbregionhierarchy where region like '"+Spider.SQLEscape((locale))+"';";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				regionid=rs.getInt("id");
				regiondescription=rs.getString("description");
				lft=rs.getInt("lft");
				rgt=rs.getInt("rgt");
				parent=rs.getInt("parentid");
				regionname=rs.getString("shortregion");
				regionclause=" where lft>"+lft+" and rgt<"+rgt+" ";
				locale=rs.getString("region");
			}
			if (regionid==0) regionid=100;
			Dbutil.closeRs(rs);
			for (String s:locale.split(", ")){
				breadcrumb+=", "+s;
				//if (breadcrumb.startsWith("/")) breadcrumb=breadcrumb.substring(1);
				breadcrumbtext+=(" &gt; "+(locale.endsWith(s)?"":"<a href='/"+(mobile?"m":"")+"region"+locale2href(breadcrumb)+"/'>")+s+(locale.endsWith(s)?"":"</a>"));
				//regionname=s;
			}
			if (regionname==null||regionname.equals("")) regionname="the world";
			stats=new RegionStatistics();
			stats.parent=parent;
			stats.region=new Region(regionid);
			stats.regionname=regionname;
			if (debug) Dbutil.logger.info("Getting statistics");
			stats.getStatistics(p);

		} catch (Exception e){
			String name = e.getClass().getName();
			if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
				// No action
			} else {
				Dbutil.logger.error("Error while retrieving image. ",e);
			} 

		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);	
		}


	}

	public static String locale2href(String locale){
		return Webroutines.URLEncode(Webroutines.removeAccents(locale)).replaceAll("%2C\\+","/").replaceAll("%2F","%252F").replaceAll("'", "&apos;");
	}
	
	public void getSubregions(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select kb.*, min(kb2.lasteditor)  as lasteditorsub from kbregionhierarchy kb left join kbregionhierarchy kb2 on (kb2.lft>kb.lft and kb2.rgt<kb.rgt and kb2.skip=0) where kb.parentid="+regionid+" and kb.id!=100 and kb.skip=0 group by kb.id order by region;";
			if (debug) Dbutil.logger.info("Getting subregions");
			rs = Dbutil.selectQuery(rs, query, con);
			if (debug) Dbutil.logger.info("Got subregions");
			while (rs.next()) {
				if (rs.getDouble("lon")!=0){
					subregions.add(new RegionPoi(rs.getInt("id"),rs.getString("region"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getString("shortregion").replaceAll("&","&amp;").replaceAll("'", "&apos;"), "<a href='/"+(mobile?"m":"")+"region/"+(Webroutines.removeAccents(rs.getString("region"))).replace("&", "&amp;").replaceAll("'", "&apos;").replaceAll(", ", "/")+"/'>"+rs.getString("shortregion").replaceAll("&","&amp;")+"</a>",rs.getString("lasteditor"),rs.getString("lasteditorsub")));
					showmap=true;
				}
				hassubregions=true;
				
			}
			
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public String getInfo(boolean editmode) {
		{
			ResultSet rs=null;
			String query;
			Connection con=Dbutil.openNewConnection();
			StringBuffer sb=new StringBuffer();
			StringBuffer producertext=new StringBuffer();
			StringBuffer regiontext=new StringBuffer();

			try{
				breadcrumbtext="";
				breadcrumb="";
				for (String s:locale.split(", ")){
					breadcrumb+=", "+s;
					//if (breadcrumb.startsWith("/")) breadcrumb=breadcrumb.substring(1);
					breadcrumbtext+=(" &gt; "+(locale.endsWith(s)?"":"<a "+(mobile?"rel='external'":"")+" href='/"+(mobile?"m":"")+"region"+locale2href(breadcrumb)+"/'>")+s+(locale.endsWith(s)?"":"</a>"));
					//regionname=s;
				}
				producertext=getProducers(con);
				getSubregions();

				if (regionid==100){
					regiontext.append("<h2>Countries</h2>");
				} else if (hassubregions){
					regiontext.append("<h2>Subregions in "+regionname+"</h2>");
				}
				for (RegionPoi r:subregions){
					regiontext.append("<a href='/"+(mobile?"m":"")+"region/"+locale2href(r.getLocale())+"/'"+(newmobile?" rel='external'":"")+">"+(editmode&&r.getLasteditorsub()!=null&&r.getLasteditorsub().length()==0?"<span style='color:red'>":"")+r.getName().replaceAll("&","&amp;")+(editmode&&r.getLasteditor().length()==0?" ???":"")+(editmode&&r.getLasteditorsub()!=null&&r.getLasteditorsub().length()==0?"</span>":"")+"</a>"+((editmode&&(r.getLat()!=0||r.getLon()!=0))?" <span style='cursor:pointer;' onclick='javascript:setLocation(\""+rs.getInt("id")+"\",0,0,\"Remove "+r.getName()+" from map?\");'>Remove from map</span>":"")+((editmode&&(r.getLat()==0&&r.getLon()==0))?" <span  style='cursor:pointer;' onclick='putonmap(\""+r.getId()+"\",\""+r.getName().replaceAll("'", "&apos;")+"\");'>Put on map</span>"+(r.getLasteditor().equals("")?"     <span  style='cursor:pointer;' onclick='javascript:setLocation(\""+r.getId()+"\",0,0,\"\");'>Set verified</span>":""):"")+"<br/>");
				}
				Dbutil.closeRs(rs);
				determineBounds();

				sb.append("<h3>You are here: <a href='/"+(mobile?"m":"")+"region/'>Wine regions</a>");
				sb.append(breadcrumbtext+(producer.length()>0?" &gt; "+producer:"")+"</h3>");
				sb.append("<h1 class='category'><img class='categoryicon' src='/images/winecountry.gif' alt='wine region' style='width:24px;height:24px;'/><span class='categorylabel'>Region: </span>"+regionname+"</h1>");
				/*
				if (!editmode&&!Configuration.serverrole.equals("DEV")){
					showmap=false;
				}
				 */
				if (regionid==100) showmap=false;
				if (newmobile){
					sb.append("<div data-role='controlgroup' data-type='horizontal'>"+(stats!=null&&!"".equals(stats.topwines)?"<a href='#topwines' rel='external' data-role='button'>Top wines</a>":"")+(editmode||showmap?"<a href='#mappane' onclick='showregionlocation()' rel='external' data-role='button'>Wine Map</a>":"")+(regionid==100?"":(mobile?"<a href='#grapestats' rel='external' data-role='button'>Grapes used</a><a href='#typestats' data-role='button' rel='external'>Wine types</a>":"<a href='#infopane' rel='external' data-role='button'>General information</a>"))+(hassubregions?"<a href='#subregionspane' rel='external' data-role='button'>"+(regionid==100?"Countries":"Subregions")+"</a>":"")+(hasproducers?"<a href='#producerspane' rel='external' data-role='button'>Producers</a>":"")+(mobile?"":"<a href='#wineguide' rel='external' data-role='button'>Wine-guide</a>")+"</div><div id='regionpane'>");
				}else {
					sb.append("<ul class='tabs' id='regiontabs'>"+(!"".equals(stats.topwines)?"<li><a href='#topwines' >Top wines</a></li>":"")+(editmode||showmap?"<li><a href='#mappane' onclick='showregionlocation()'>Wine Map</a></li>":"")+(regionid==100?"":(mobile?"<li><a href='#grapestats'>Grapes used</a></li><li><a href='#typestats'>Wine types</a></li>":"<li><a href='#infopane'>General information</a></li>"))+(hassubregions?"<li><a href='#subregionspane'>"+(regionid==100?"Countries":"Subregions")+"</a></li>":"")+(hasproducers?"<li><a href='#producerspane'>Producers</a></li>":"")+(mobile?"":"<li><a href='#wineguide'>Wine-guide</a></li>")+"</ul><div id='regionpane'>");
				}
				if (!"".equals(stats.topwines)){
					sb.append("<h2>Top wines from "+regionname+"</h2>");
					sb.append("<div class='pane' id='topwines'>");
					sb.append(stats.topwines);
					sb.append("</div>");
				}
				if (editmode||showmap){
					if (mobile){
						sb.append("<div class='pane' id='mappane'>");
						sb.append("<h2>Map of "+regionname+"</h2>");
						if (regionid!=100){
							sb.append("<div id='mapdetail' class='map' style='width: 90%; height: 240px;'></div>");
							sb.append("<div id='switchpoi'></div>");
						}

						sb.append("</div>");
					} else {
						sb.append("<div class='pane' id='mappane'>");
						if (regionid!=100){
							//sb.append("<div id='mapbig' class='map' style='position:relative;width: 750px; height: 500px;'><div id='mapdetail' class='map' style='position:absolute;width: 750px; height: 500px;'></div><div id='mapstreetview' class='map' style='display:none;position:absolute;width: 750px; height: 500px;'><div id='imStreetViewClose' onclick='imStreetView.toggleStreetView(false);'>Close Streetview</div> </div></div>");
							sb.append("<div id='mapdetail' class='map' style='width: 750px; height: 500px;'></div>");
							sb.append("<div id='mapworld' class='map' style='width: 248px; height: 250px;'></div>");
							sb.append("<div id='mapregion' class='map' style='width: 248px; height: 250px;'></div>");
							sb.append("<div id='switchpoi'></div>");
						}

						sb.append("</div>");
					}
				}
				if (regionid!=100){sb.append("<div class='pane' id='infopane'>");
				sb.append("<div id='grapestats'><h2>Grapes and blends used in "+regionname+"</h2>"+stats.grapestext+"</div>");
				sb.append("<div id='typestats'><h2>Wine types in "+regionname+"</h2>"+stats.typetext+"</div>");
				sb.append("<div id='regiondescription'>"+regiondescription+"</div></div>");
				}
				if (hassubregions){
					sb.append("<div class='pane' id='subregionspane'>");
					sb.append(regiontext);
					sb.append("</div>");
				}
				if (hasproducers){
					sb.append("<div class='pane' id='producerspane'>");
					sb.append(producertext);
					sb.append("</div>");
				}
				//if (regionid!=100) sb.append("</div>");
				
				if (!mobile) sb.append("<div class='pane' id='wineguidepane'>");
				/*
				query = "select count(*) as thecount from knownwines where locale='"+Spider.SQLEscape(locale)+"'"+producerclause+" and numberofwines>0;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					if (rs.getInt("thecount")<100||producerclause.length()>0){
						Dbutil.closeRs(rs);
						query = "select * from knownwines where locale='"+Spider.SQLEscape(locale)+"'"+producerclause+"  and numberofwines>0 order by wine;";
						rs = Dbutil.selectQuery(rs, query, con);
						if (rs.isBeforeFirst()){
							sb.append("<h2>Wines</h2>");
							while (rs.next()) {
								sb.append("<a href='/wine/"+Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine")))+"'>"+rs.getString("wine").replaceAll("&","&amp;")+"</a><br/>");
							}
						}
					} else {
						Dbutil.closeRs(rs);
				 */
				/*
			}
				}
				 */
				Dbutil.closeRs(rs);






			} catch (Exception e){
				String name = e.getClass().getName();
				if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
					// No action
				} else {
					Dbutil.logger.error("Error while retrieving region info. ",e);
				} 

			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);	
			}
			return sb.toString();
		}
	}

	public String getMapData(){
		determineBounds();
		return("<div id='mapdetail' class='map' style='width: 750px; height: 500px;'></div>");

	}

	public StringBuffer getProducers(Connection con){
		String query;
		StringBuffer producertext=new StringBuffer();
		if (regionid==100) return producertext;
		ResultSet rs=null;
		ResultSet rs2=null;
		if (debug) Dbutil.logger.info("Getting producers");
		String limitclause="";
		if (producerlimit>0){
			limitclause=" limit "+producerlimit;
		}
		query = "select SQL_CALC_FOUND_ROWS producer,locale,producerids,count(*) as wines, regionwines, sum(numberofwines) as offers from (select producer as prod, count(*) as regionwines, sum(numberofwines) as regionoffers from knownwines kw1 where lft>="+lft+" and rgt<="+rgt+" group by producer) sel join knownwines on (sel.prod=knownwines.producer)   group by producer order by regionoffers desc,producer "+limitclause+";";
		// where locale='"+Spider.SQLEscape(locale)+"'
		rs = Dbutil.selectQuery(rs, query, con);
		if (debug) Dbutil.logger.info("Got producers: "+query);
		Producer prod;
		try {
			if (rs.isBeforeFirst()) {
				hasproducers = true;
				producertext
				.append("<h2>Producers that make wines from "
						+ regionname + " or subregions</h2><table>");
			}
			while (rs.next()) {
				if (producerlimit>0){
					prod = new Producer(rs.getString("producer"));
					prod.mobile=mobile;
					if (prod.accuracy > 1){
						showmap = true;
						producers.add(prod);
					}
				}
				producertext.append("<tr><td><a href='/"+(mobile?"m":"")+"winery/"
						+ Webroutines.URLEncodeUTF8Normalized(rs.getString("producer")).replaceAll("%2F", "/")
						.replace("&", "&amp;") + "'>"
						+ rs.getString("producer").replaceAll("&", "&amp;")
						+ "</a></td><td>" + rs.getString("regionwines")
						+ " wine" + (rs.getInt("regionwines") > 1 ? "s" : "")
						+ " out of " + rs.getString("wines") + " are from "
						+ regionname + "</td></tr>");
			}
			rs2=Dbutil.selectQuery("SELECT FOUND_ROWS() as records;",con);
			int records=0;
			if (rs2.next()) records=rs2.getInt("records");
			//Dbutil.logger.info(records);
			if (rs.isAfterLast()) {
				if (producerlimit>0&&records>50){
					producertext.append("<tr><td colspan='2'><span style='cursor:pointer;text-decoration:underline;' onclick='$(\".spinner\").show();$.post(\"/jsinteraction.jsp\",\"action=getallproducers&amp;regionid="+regionid+"\",function(data){$(\"#producerspane\").html(data)});'>View all producers...</span><img class='spinner' src='/images/indicator.gif' style='display:none;width:16px;height:16px;' alt='spinner'/></td></tr>");
				}
				producertext.append("</table>");
			}
			if (debug)
				Dbutil.logger.info("Filled producer set");
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
		}
		return producertext;

	}

	public void determineBounds(){
		double centerlon=Double.parseDouble(Dbutil.readValueFromDB("select * from kbregionhierarchy where id="+regionid+";", "lon"));
		double centerlat=Double.parseDouble(Dbutil.readValueFromDB("select * from kbregionhierarchy where id="+regionid+";", "lat"));
		if (centerlon!=0&&centerlat!=0){
			bounds=new Bounds();
			bounds.centerlon=centerlon;
			bounds.centerlat=centerlat;
			Set<POI> b=new HashSet<POI>();
			if (subregions.size()>=4){
				b.addAll(subregions);
			} else if (producers.size()>2){
				b.addAll(producers);
			} else if (subregions.size()>=1){
				b.addAll(subregions);
			}
			bounds=getBounds(bounds,b,(double)100);
		}else if (subregions.size()>=4){
			Set<POI> b=new HashSet<POI>();
			b.addAll(subregions);
			bounds=getBounds(b,(double)100);
		}else if (producers.size()>2){
			Set<POI> b=new HashSet<POI>();
			b.addAll(producers);
			bounds=getBounds(b,(double)1);
		} else if (subregions.size()>=1){
			Set<POI> b=new HashSet<POI>();
			b.addAll(subregions);
			bounds=getBounds(b,(double)100);
		}else {
			double distance=4;
			bounds=new Bounds();
			String parent=Dbutil.readValueFromDB("select * from kbregionhierarchy where id=(select parentid from kbregionhierarchy where id="+regionid+");", "region");

			if (!parent.equals("All")){
				Regioninfo ri=new Regioninfo("/region/"+parent.replaceAll(", ", "/")+"/",debug,null);
				ri.getInfo(false);
				distance=ri.distance(ri.bounds.latmin, ri.bounds.lonmin, ri.bounds.latmax, ri.bounds.lonmax,(double)1);
				distance=rad2deg(distance/40000*2*Math.PI)/10;
				//Dbutil.logger.info(distance);
			}
			bounds.centerlon=centerlon;
			bounds.centerlat=centerlat;
			bounds.lonmin=bounds.centerlon-distance;
			bounds.lonmax=bounds.centerlon+distance;
			bounds.latmin=bounds.centerlat-distance;
			bounds.latmax=bounds.centerlat+distance;
		}
		bounds.centerlon=centerlon;
		bounds.centerlat=centerlat;


	}

	private static Bounds getBounds(Set<POI> pois, double minimum){
		Bounds bounds=new Bounds();
		Map<POI,Double> distances=new LinkedHashMap<POI, Double>();
		POI p1;
		POI p2;
		POI mostcentral=null;
		Double mostcentraldistance=(double)0;
		Iterator<POI> it1=pois.iterator();
		while (it1.hasNext()){
			p1=it1.next();
			if (p1.getId()>0){
				Iterator<POI> it2=pois.iterator();
				while (it2.hasNext()){
					p2=it2.next();
					if (p2.getId()>0){
						if (p1.getId()!=p2.getId()){
							if (distances.get(p1)==null) distances.put(p1, (double)0);
							distances.put(p1, distances.get(p1)+1/distance(p1.getLat(),p1.getLon(),p2.getLat(),p2.getLon(),minimum));
						}
					}
				}
			}
		}

		for (POI p:distances.keySet()){
			if (distances.get(p)>mostcentraldistance){
				mostcentraldistance=distances.get(p);
				mostcentral=p;
			}
		}
		if (mostcentral!=null){
			distances=new LinkedHashMap<POI, Double>();
			it1=pois.iterator();
			while (it1.hasNext()){
				p1=it1.next();
				if (p1.getId()>0){
					distances.put(p1, distance(p1.getLat(),p1.getLon(),mostcentral.getLat(),mostcentral.getLon(),minimum));
				}
			}
			distances=Webroutines.sortByValue(distances,false);
			it1=distances.keySet().iterator();
			int n=0;
			bounds.centerlat=mostcentral.getLat();
			bounds.centerlon=mostcentral.getLon();
			//Dbutil.logger.info(mostcentral.getLabelText());
			while (it1.hasNext()){
				p1=it1.next();
				if (p1.getId()>0){
					n++;
					if (n<distances.size()*2/3+1){
						//Dbutil.logger.info(p1.getLabelText());
						if (bounds.lonmin>p1.getLon()) bounds.lonmin=p1.getLon();
						if (bounds.lonmax<p1.getLon()) bounds.lonmax=p1.getLon();
						if (bounds.latmin>p1.getLat()) bounds.latmin=p1.getLat();
						if (bounds.latmax<p1.getLat()) bounds.latmax=p1.getLat();
					}
				}
			}
			//logBounds(bounds);
			double distance = distance(bounds.latmin, bounds.lonmin, bounds.latmax, bounds.lonmax,(double)1);
			distance=rad2deg(distance/40000*2*Math.PI)*0.7;
			bounds.centerlon=(bounds.lonmin+bounds.lonmax)/2;
			bounds.centerlat=(bounds.latmin+bounds.latmax)/2;
			bounds.lonmin=bounds.centerlon-distance;
			bounds.lonmax=bounds.centerlon+distance;
			bounds.latmin=bounds.centerlat-distance;
			bounds.latmax=bounds.centerlat+distance;
			//logBounds(bounds);

		}

		return bounds;

	}
	private static Bounds getBounds(Bounds bounds,Set<POI> pois, double minimum){
		Map<POI,Double> distances=new LinkedHashMap<POI, Double>();
		Iterator<POI> it1 = pois.iterator();
		POI p1;
		while (it1.hasNext()){
			p1=it1.next();
			if (p1.getId()>0){
				distances.put(p1, distance(p1.getLat(),p1.getLon(),bounds.centerlat,bounds.centerlon,minimum));
			}
		}
		distances=Webroutines.sortByValue(distances,false);
		it1=distances.keySet().iterator();
		int n=0;
		while (it1.hasNext()){
			p1=it1.next();
			if (p1.getId()>0){
				n++;
				if (n<distances.size()*2/3+1){
					//Dbutil.logger.info(p1.getLabelText());
					if (bounds.lonmin>p1.getLon()) bounds.lonmin=p1.getLon();
					if (bounds.lonmax<p1.getLon()) bounds.lonmax=p1.getLon();
					if (bounds.latmin>p1.getLat()) bounds.latmin=p1.getLat();
					if (bounds.latmax<p1.getLat()) bounds.latmax=p1.getLat();
				}
			}
		}
		//logBounds(bounds);
		double distance = distance(bounds.latmin, bounds.lonmin, bounds.latmax, bounds.lonmax,(double)1);
		distance=rad2deg(distance/40000*2*Math.PI)*0.7;
		bounds.lonmin=bounds.centerlon-distance;
		bounds.lonmax=bounds.centerlon+distance;
		bounds.latmin=bounds.centerlat-distance;
		bounds.latmax=bounds.centerlat+distance;
		//logBounds(bounds);



		return bounds;

	}

	private static void logBounds(Bounds b){
		Dbutil.logger.info("Lat min: "+b.latmin);
		Dbutil.logger.info("Lat max: "+b.latmax);
		Dbutil.logger.info("Lon min: "+b.lonmin);
		Dbutil.logger.info("Lon max: "+b.lonmax);
	}


	public static double distance(double lat1, double lon1, double lat2, double lon2,double minimum) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515* 1.609344;
		if (dist<minimum) dist=minimum;
		return (dist);
	}
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public Regioninfo(int id,int lft,int rgt){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select SQL_CALC_FOUND_ROWS producer,producerids from  knownwines where lft>="+lft+" and rgt<="+rgt+" and producerids!='' group by producerids;";
			rs = Dbutil.selectQuery(rs, query, con);
			Producer prod;
			while (rs.next()) {
				if (!rs.getString("producerids").contains(",")) {
					prod=new Producer(rs.getInt("producerids"));
					producers.add(prod);
				}
			}

			determineBounds();
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (bounds!=null) Dbutil.executeQuery("update kbregionhierarchy set lon="+bounds.centerlon+", lat="+bounds.centerlat+" where id="+id+";");
	}


	public static void main (String[] args){
		String query;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		try {
			Dbutil.executeQuery("update kbregionhierarchy set skip=1;");
			query = "select distinct(locale) from knownwines where numberofwines>0;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				//Dbutil.logger.info(rs.getInt("id")+":  "+rs.getString("region"));
				//new Regioninfo(rs.getInt("id"), rs.getInt("lft"),rs.getInt("rgt"));
				Dbutil.executeQuery("update kbregionhierarchy set skip=0 where region='"+Spider.SQLEscape(rs.getString("locale"))+"';");
			}
			Dbutil.closeRs(rs);
			query = "select * from kbregionhierarchy where skip=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				Dbutil.executeQuery("update kbregionhierarchy set skip=0 where lft<"+rs.getInt("lft")+" and rgt>"+rs.getInt("rgt")+";",con);
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
	}


}
package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.freewinesearcher.common.*;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.batch.Spider;

public class StoreLocator {
	String countrycode;
	Location location;
	public Bounds bounds;
	String metrics;
	int producer;
	int knownwineid=0;
	int vintage=0;
	public String producername;
	double proportion=4/3;
	int numberofstoresinbounds=10;
	int shopid=0;
	boolean showprices=false;
	String currency="EUR";
	double maxdistance=0;


	public static String stores2json(HashMap<Integer,StoreData> storedata){
		JSONArray json=new JSONArray();
		for (int n:storedata.keySet()){
			JSONObject store=new JSONObject();
			try {
				store.put("name", storedata.get(n).store.name);
				store.put("shopid", storedata.get(n).store.id);
				store.put("title", storedata.get(n).getTitle());
				store.put("offers", storedata.get(n).offers);
				store.put("lon", storedata.get(n).getLon());
				store.put("lat", storedata.get(n).getLat());
				store.put("html", "<span id='store"+storedata.get(n).store.id+"'  class='infow'><b>"+storedata.get(n).store.name+"</b><br/><img src='/images/spinner.gif'/> Loading...</span>");
				store.put("distance", storedata.get(n).distance);
				store.put("address", storedata.get(n).store.address);
				/*
				JSONObject wines=new JSONObject();
				for (String w:storedata.get(n).wines.keySet()){
					wines.put(w, storedata.get(n).wines.get(w));
				}
				store.put("wines", wines);
				*/
			} catch (JSONException e) {
				Dbutil.logger.info("JSON error: ",e);
			}
			try {
				json.put(store);
			} catch (Exception e) {
				Dbutil.logger.info("JSON error: ",e);
			}

		}
		return json.toString();
	}
	
	public String getWinesFromStore(){
		String query="";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		StringBuffer storelist=new StringBuffer();
		JSONObject json=new JSONObject();
		
		
		try {
			//if (coordinates==null){
			//	query = "select *, null as distance from (select shopid, count(*) as thecount from materializedadvice join (select knownwines.id  from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where kbproducers.id="+producer+") wineselection on (materializedadvice.knownwineid=wineselection.id) "+(countrycode!=null&&countrycode.length()>0?" where materializedadvice.country='"+Spider.SQLEscape(countrycode)+"'":"")+" group by shopid) selectedshops join shops on (selectedshops.shopid=shops.id) order by selectedshops.thecount desc;";
			//} else {
			Shop store=StoreInfo.getStore(shopid);
			if (producer>0){
				query = "select * from wineview join (select knownwines.id,wine  from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where kbproducers.id="+producer+") wineselection on (wineview.knownwineid=wineselection.id) where shopid ="+shopid+" order by shopid,knownwineid,vintage;";
			} else if (knownwineid>0){
				query = "select * from wineview join knownwines on (wineview.knownwineid=knownwines.id) where shopid ="+shopid+" and knownwineid="+knownwineid+(vintage>0?" and vintage="+vintage:"")+" order by shopid,knownwineid,vintage;";
				
			} 
			if (!query.equals("")){
			//}
			//Dbutil.logger.info(query);
			rs = Dbutil.selectQuery(rs, query, con);
			//Dbutil.logger.info("finished");
			
			StringBuffer html=new StringBuffer();
			int n=0;
			html.append("<table>");
			while (rs.next()){
				n++;
				html.append("<tr><td><a href='https://www.vinopedia.com/store/"+Webroutines.URLEncode(Webroutines.removeAccents(store.name)).replaceAll("%2F", "/")+"/?wineid="+rs.getString("id")+"' target='_blank'>"+rs.getString("wine")+(rs.getInt("vintage")>0?" "+rs.getInt("vintage"):"")+(rs.getDouble("size")>0&&rs.getDouble("size")!=0.75?" ("+Webroutines.formatSizecompact(rs.getFloat("size"))+")":"")+"</a></td>"+(showprices?"<td>"+Webroutines.formatPrice(rs.getDouble("priceeuroin"), rs.getDouble("priceeuroex"), currency, "EX")+"</td>":"")+"</tr>");
			}
			html.append("</table>");
			
			//StoreInfo.getStore(rs.getInt("shopid"));
			
			json.put("wines","<b>"+store.name+" ("+n+" offers):</b><br>"+html.toString());
			//Dbutil.logger.info(json.getString("wines"));
			//Dbutil.logger.info(json.toString());
			json.put("shopid",store.id);
			} else { //what's hot
				StringBuffer html=new StringBuffer();
				html.append("<b><a href='https://www.vinopedia.com/store/"+Webroutines.URLEncode(Webroutines.removeAccents(store.name)).replaceAll("%2F", "/")+"' target='_blank'/>"+store.name+"</a></b><br/>");
				String continent=Dbutil.readValueFromDB("select * from shops join vat on (shops.countrycode=vat.countrycode) where shops.id="+shopid, "continent");
				if (!"EU".equals(continent)&&!"UC".equals(continent)) continent="AL";
				int weekupdates=Dbutil.readIntValueFromDB("select sum(if(createdate>=date(now()-interval 7 day),1,0)) as weekupdates from wineview where shopid ="+shopid+" order by shopid,knownwineid,vintage;","weekupdates");
				html.append(weekupdates+" new wines last week. ");
				query="select w.id,w.name,w.priceeuroex,w.priceeuroin,w.vintage,w.size,w.knownwineid, w.priceeuroex/bp.priceeuroex as relprice, kw.wine, w.priceeuroex as minpriceeuroex from wines w join knownwines kw on (w.knownwineid=kw.id) left join bestprices bp on (w.knownwineid=bp.knownwineid and w.vintage=bp.vintage and continent='"+continent+"' and n>1 )  where w.shopid="+shopid+" and w.knownwineid>0 and createdate>=date(now()-interval 7 day) order by relprice  limit 3;";
				// Old query; looked at PQ ratio instead of relative market price
				//query="select * from (select wines.* from wines join materializedadvice on (wines.id=materializedadvice.id) where wines.shopid="+shopid+" and wines.knownwineid>0 and createdate>date(now()-interval 7 day) order by pqratio desc limit 3) sel join knownwines on (sel.knownwineid=knownwines.id);";
				//
				rs=Dbutil.selectQuery(query, con);
				if (rs.isBeforeFirst()) html.append("The most interesting ones:<br/><table>");
				while (rs.next()){
					html.append("<tr><td><a href='https://www.vinopedia.com/store/"+Webroutines.URLEncode(Webroutines.removeAccents(store.name)).replaceAll("%2F", "/")+"/?wineid="+rs.getString("id")+"' target='_blank'>"+rs.getString("wine")+(rs.getInt("vintage")>0?" "+rs.getInt("vintage"):"")+(rs.getDouble("size")>0&&rs.getDouble("size")!=0.75?" ("+Webroutines.formatSizecompact(rs.getFloat("size"))+")":"")+"</a></td>"+(showprices?"<td>"+Webroutines.formatPrice(rs.getDouble("priceeuroin"), rs.getDouble("priceeuroex"), currency, "EX")+"</td>":"")+"</tr>");
				}
				if (rs.isAfterLast()) html.append("</table>");
				json.put("wines",html.toString());
				json.put("shopid",store.id);
			}
			
		} catch (Exception e) {
			Dbutil.logger.error("", e);
			Dbutil.logger.info(location);
			Dbutil.logger.info(bounds);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return json.toString();
	}


	private String getStoreSelect(){
		String q="";
		if (producer>0) {
			q="from (select shopid, count(*) as thecount from materializedadvice join (select knownwines.id  from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where kbproducers.id="+producer+") wineselection on (materializedadvice.knownwineid=wineselection.id) group by shopid) selectedshops join shops on (selectedshops.shopid=shops.id)";
		} else if (knownwineid>0) {
			q="from (select shopid, count(*) as thecount from wines where knownwineid="+knownwineid+(vintage>0?" and vintage="+vintage:"")+" group by shopid) selectedshops join shops on (selectedshops.shopid=shops.id)";
		} else {
			q=",id as shopid,if(lastnewwine>=date(now()-interval 7 day),1,0) as thecount from shops ";
		}
		
		return q;
	}

	public LinkedHashMap<Integer,StoreData> getStores(){
		LinkedHashMap<Integer,StoreData> stores=new LinkedHashMap<Integer,StoreData>();
		float distancefactor=(float) 1;
		if (metrics==null||metrics.equals("km")) distancefactor=(float) 1.609344;
		String query="";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			//if (coordinates==null){
			//	query = "select *, null as distance from (select shopid, count(*) as thecount from materializedadvice join (select knownwines.id  from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where kbproducers.id="+producer+") wineselection on (materializedadvice.knownwineid=wineselection.id) "+(countrycode!=null&&countrycode.length()>0?" where materializedadvice.country='"+Spider.SQLEscape(countrycode)+"'":"")+" group by shopid) selectedshops join shops on (selectedshops.shopid=shops.id) order by selectedshops.thecount desc;";
			//} else {
			
			query = "select *,((ACOS(SIN("+bounds.centerlat+" * PI() / 180) * SIN(lat * PI() / 180) + COS("+bounds.centerlat+" * PI() / 180) * COS(lat * PI() / 180) * COS(("+bounds.centerlon+" - lon) * PI() / 180)) * 180 / PI()) * 60 * 1.1515) as distance "+getStoreSelect()+" where not (lon=0 and lat=0) and lon between "+Math.min(bounds.lonmin,bounds.lonmax)+" and "+Math.max(bounds.lonmin,bounds.lonmax)+" and lat between "+Math.min(bounds.latmin,bounds.latmax)+" and "+Math.max(bounds.latmin,bounds.latmax)+" order by distance;";
			//}
			//Dbutil.logger.info(query);
			rs = Dbutil.selectQuery(rs, query, con);
			//Dbutil.logger.info("finished");
			

			while (rs.next()) {
				StoreData sd=new StoreData();
				sd.store=StoreInfo.getStore(rs.getInt("shopid"));//StoreInfo.getStore(rs.getInt("shopid"));
				sd.offers=rs.getInt("thecount");
				if (rs.getString("distance")!=null) sd.distance=(rs.getFloat("distance")*distancefactor);
				stores.put(rs.getInt("shopid"),sd);
				//storelist.append(", "+rs.getInt("shopid"));
			}
			/*
			if (storelist.length()>0) storelist=storelist.delete(0, 1);
			query = "select * from wineview join (select knownwines.id,wine  from kbproducers join knownwines on (kbproducers.name=knownwines.producer) where kbproducers.id="+producer+") wineselection on (wineview.knownwineid=wineselection.id) where shopid in ("+storelist.toString()+") order by shopid,knownwineid,vintage;";
			Dbutil.logger.info(query);
			rs = Dbutil.selectQuery(rs, query, con);
			Dbutil.logger.info("finished");

			while (rs.next()) {
				//Dbutil.logger.info(rs.getInt("shopid"));
				stores.get(rs.getInt("shopid")).wines.put(rs.getString("wine")+(rs.getInt("vintage")>0?" "+rs.getInt("vintage"):""),rs.getString("price"));
			}
			*/


		} catch (Exception e) {
			Dbutil.logger.error("", e);
			Dbutil.logger.info(location);
			Dbutil.logger.info(bounds);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return stores;
	}

	public void setMaxDistance(double maxdistance){
		this.maxdistance=maxdistance;
		float distancefactor=(float) 1;
		if (metrics==null||metrics.equals("km")) distancefactor=(float) 1.609344;
		bounds=new Bounds();
		bounds.centerlat=location.lat;
		bounds.centerlon=location.lon;
		bounds.lonmin=location.lon-maxdistance/Math.abs(Math.cos(Math.toRadians(location.lat))*69*distancefactor);
		bounds.lonmax=location.lon+maxdistance/Math.abs(Math.cos(Math.toRadians(location.lat))*69*distancefactor);
		bounds.latmin=location.lat-(maxdistance/(69*distancefactor));
		bounds.latmax=location.lat+(maxdistance/(69*distancefactor));
	}
		
	public void getBounds(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select *,((ACOS(SIN("+location.lat+" * PI() / 180) * SIN(lat * PI() / 180) + COS("+location.lat+" * PI() / 180) * COS(lat * PI() / 180) * COS(("+location.lon+" - lon) * PI() / 180)) * 180 / PI()) * 60 * 1.1515) as distance "+getStoreSelect()+" where not (shops.lon=0 and shops.lat=0) order by distance limit "+numberofstoresinbounds+";";
			//Dbutil.logger.info(query);
			rs = Dbutil.selectQueryFromMemory(query,"materializedadvice", con);
			if (rs.isBeforeFirst()){
				bounds=new Bounds();
				bounds.lonmin=location.lon;
				bounds.lonmax=location.lon;
				bounds.latmin=location.lat;
				bounds.latmax=location.lat;
				bounds.centerlon=location.lon;
				bounds.centerlat=location.lat;
				
			while (rs.next()){
				//double deltalat = Regioninfo.distance(rs.getDouble("lat"),location.lon,location.lat,location.lon,0);
				//double deltalon = Regioninfo.distance(location.lat,rs.getDouble("lon"),location.lat,location.lon,0);
				//double distance=Regioninfo.rad2deg(Math.max(deltalon/proportion,deltalat)/40000*2*Math.PI)*1.25;
				//Dbutil.logger.info(distance);
				//Dbutil.logger.info("deltalon:"+deltalon+", deltalat:"+deltalat+". Center lon: "+location.lon+", centerlat: "+location.lat);
				//bounds=new Bounds();
				if (bounds.lonmin>rs.getDouble("lon")) bounds.lonmin=rs.getDouble("lon")-0.1;
				if (bounds.lonmax<rs.getDouble("lon")) bounds.lonmax=rs.getDouble("lon")+0.1;
				if (bounds.latmin>rs.getDouble("lat")) bounds.latmin=rs.getDouble("lat")-0.1;
				if (bounds.latmax<rs.getDouble("lat")) bounds.latmax=rs.getDouble("lat")+0.1;
				//bounds.lonmax=bounds.centerlon+distance*proportion;
				//bounds.latmin=bounds.centerlat-distance;
				//bounds.latmax=bounds.centerlat+distance;
				
				
				}
				

			} else{
				//Dbutil.logger.info("Could not get bounds: "+query);
				bounds=new Bounds();
				bounds.centerlon=location.lon;
				bounds.centerlat=location.lat;
				bounds.lonmin=bounds.centerlon-1*proportion;
				bounds.lonmax=bounds.centerlon+1*proportion;
				bounds.latmin=bounds.centerlat-1;
				bounds.latmax=bounds.centerlat+1;
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		//Dbutil.logger.info(bounds.lonmin);
		//Dbutil.logger.info(bounds.lonmax);
		//Dbutil.logger.info(rs.getDouble("lon"));
		//Dbutil.logger.info(deltalon);
		//Dbutil.logger.info(bounds.latmin);
		//Dbutil.logger.info(bounds.latmax);
		//Dbutil.logger.info(rs.getDouble("lat"));
		//Dbutil.logger.info(deltalat);
	}
	
	public int getShopid() {
		return shopid;
	}

	public void setShopid(int shopid) {
		this.shopid = shopid;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
		if (bounds==null) getBounds();
		
	}


	public void setProducer(int producer){
		this.producer=producer;
		this.producername=Dbutil.readValueFromDB("select name from kbproducers where id="+producer, "name");
	}

	public int getProducer() {
		return producer;
	}

	public String getCountrycode() {
		return countrycode;
	}

	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}


	public String getMetrics() {
		return metrics;
	}

	public void setMetrics(String metrics) {
		this.metrics = metrics;
	}


	public int getKnownwineid() {
		return knownwineid;
	}

	public void setKnownwineid(int knownwineid) {
		this.knownwineid = knownwineid;
	}

	public int getVintage() {
		return vintage;
	}

	public void setVintage(int vintage) {
		this.vintage = vintage;
	}


	public boolean isShowprices() {
		return showprices;
	}

	public void setShowprices(boolean showprices) {
		this.showprices = showprices;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}


	public static class StoreData implements POI{
		public Shop store;
		public int offers;
		public double distance;
		public LinkedHashMap<String,String> wines=new LinkedHashMap<String, String>();


		@Override
		public String getHTML() {
			StringBuffer html=new StringBuffer();
			html.append(store.name+" ("+wines.size()+" offers):<br>");
			for (String wine:wines.keySet()){
				html.append(wine+"<br/>");
			}
			return html.toString();
		}
		@Override
		public int getId() {
			return store.id;
		}
		@Override
		public String getLabelText() {
			return "";
		}
		@Override
		public Double getLat() {
			return store.lat;
		}
		@Override
		public Double getLon() {
			return store.lon;
		}
		@Override
		public String getTitle() {
			return store.name;
		}
	}

	public static class Location{
		public String address;
		String countrycode;
		public double lon;
		public double lat;

		public Location(){}


		public Location(String lat, String lon,String countrycode){
			try {
				this.countrycode=countrycode;
				this.lon=Double.parseDouble(lon);
				this.lat=Double.parseDouble(lat);
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
		}
		
		
		public static Location getAddressLocation(String address){
			Location location=new Location();
			location.address=address;
			try{
				Webpage webpage=new Webpage();
				webpage.errorpause=5;
				webpage.maxattempts=2;
				webpage.encoding="UTF-8";
				String page;
				address=Webroutines.URLEncode(Spider.unescape(address));
				webpage.urlstring="http://local.yahooapis.com/MapsService/V1/geocode?appid=UBv0X4nV34HV8LAuLZ44PWUGyhAPLSrQeFK7rC02sutFo9c.mlZOZtYn3Pm0fdZDL1j0TQ--&location="+address;
				webpage.readPage();
				page=webpage.html;
				if (page.startsWith("Webpage")){
					Dbutil.logger.info("Error in page");
				}
				location.lon=Double.parseDouble(Webroutines.getRegexPatternValue("<Longitude>([^<]+)", page));
				location.lat=Double.parseDouble(Webroutines.getRegexPatternValue("<Latitude>([^<]+)", page));
				location.countrycode=Webroutines.getRegexPatternValue("<CountryCode>([^<]+)", page);
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
			return location;
		}


	}

}

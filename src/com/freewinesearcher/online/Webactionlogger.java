package com.freewinesearcher.online;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;

public class Webactionlogger implements Runnable {
	enum loggingtypes {link,ad,winead};
	loggingtypes loggingtype;
	public String type;
	public String page;
	String ip;
	String referrer;
	public String name;
	public String vintage;
	int created;
	float pricemin;
	float pricemax;
	public String countryseller; 
	boolean rareold;
	public String shopid;
	public String wineid;
	public float price;
	String wineurl; 
	Double cpc;
	int numberofresults=0;
	public long startload=0;
	public long endload=0;
	public long loadtime=0;
	String partnerid;
	String adid="0";
	String region;
	String winename;
	public Searchhistory searchhistory;
	public int knownwineid=0;
	public String bannersshown="";
	public String hostname="";
	public String useragent="";



	public Webactionlogger(String adid, String region, String winename,String partnerid, double cpc, String type, String ip, String name, String vintage, String countryseller, String shopid) {
		this.loggingtype=loggingtypes.ad;
		this.adid=adid;
		this.region=region;
		this.winename=winename;
		this.page = adid;
		this.ip = ip;
		this.name = name;
		this.vintage = vintage;
		this.countryseller = countryseller;
		this.shopid = shopid;
		this.cpc = cpc;
		this.partnerid = partnerid;
		this.type=type;
		this.startload=System.currentTimeMillis();
		this.wineurl="";

	}

	public Webactionlogger(String wineurl, String region, String winename,int wineid, double cpc, String adtype, String ip, String name, String vintage, String countryseller, String shopid) {
		this.loggingtype=loggingtypes.winead;
		this.wineurl=wineurl;
		this.region=region;
		this.winename=winename;
		this.page = adtype;
		this.ip = ip;
		this.name = name;
		this.vintage = vintage;
		this.countryseller = countryseller;
		this.shopid = shopid;
		this.cpc = cpc;
		this.wineid = wineid+"";
		this.type=adtype;
		this.startload=System.currentTimeMillis();

	}
	public Webactionlogger(String type, String page, String ip, String referrer, String name, String vintage, int created, float pricemin, float pricemax, String countryseller, boolean rareold, String shopid, String wineid, String price, String wineurl, Double cpc, int numberofresults, Searchhistory searchhistory){
		this.loggingtype=loggingtypes.link;
		this.type=type;
		this.page=page;
		this.ip=ip;
		this.referrer=referrer;
		this.name=name;
		this.vintage=vintage;
		this.created=created;
		this.pricemin=pricemin;
		this.pricemax=pricemax;
		this.countryseller=countryseller;
		this.rareold=rareold;
		this.shopid=shopid;
		this.partnerid=Partner.getIDFromShopId(shopid)+"";
		this.wineid=wineid;
		if ("".equals(price)) price="0.0";
		this.price=Float.parseFloat(price);
		this.wineurl=wineurl;
		this.cpc=cpc;
		this.numberofresults=numberofresults;
		this.startload=System.currentTimeMillis();
		this.searchhistory=searchhistory;
	}


	public Webactionlogger(String type, String page, String ip, String referrer, String name, String vintage, int created, float pricemin, float pricemax, String countryseller, boolean rareold, String shopid, String wineid, String price, String wineurl, Double cpc, int numberofresults){
		this.loggingtype=loggingtypes.link;
		this.type=type;
		this.page=page;
		this.ip=ip;
		this.referrer=referrer;
		this.name=name;
		this.vintage=vintage;
		this.created=created;
		this.pricemin=pricemin;
		this.pricemax=pricemax;
		this.countryseller=countryseller;
		this.rareold=rareold;
		this.shopid=shopid;
		this.partnerid=Partner.getIDFromShopId(shopid)+"";
		this.wineid=wineid;
		if ("".equals(price)) price="0.0";
		this.price=Float.parseFloat(price);
		this.wineurl=wineurl;
		this.cpc=cpc;
		this.numberofresults=numberofresults;
		this.startload=System.currentTimeMillis();

	}

	public void run() {
		if (loadtime==0&&startload>0) loadtime=System.currentTimeMillis()-startload;
		logmenow();
	}

	private String limitLength(String str, int maxlength){
		if (str!=null&&str.length()>maxlength-1) str=str.substring(0,maxlength-1);
		return str;
	}

	public void logmenow() {
		int shopid=0;
		try{shopid=Integer.parseInt(this.shopid);}catch(Exception e){}
		int wineid=0;
		try{wineid=Integer.parseInt(this.wineid);}catch(Exception e){}
		int id=0;
		int vintageint=0;
		try{vintageint=Integer.parseInt(vintage);} catch (Exception e){}
		
		
		ResultSet rs=null;
		if (hostname==null||hostname.equals("")) try{hostname=InetAddress.getByName(ip).getHostName();} catch (Exception e){}
		Connection con=Dbutil.openNewConnection();
		try{
		if (countryseller==null) countryseller="";
		if (countryseller.equals("All")) countryseller="";
		if (referrer==null) referrer="";
		if (page!=null&&page.contains("host=www.freewinesearcher.com")) page=page.replace("www.vinopedia.com", "www.freewinesearcher.com").replaceAll(".host=www\\.freewinesearcher\\.com", "");
		page=limitLength(page,255);
		hostname=limitLength(hostname,255);
		referrer=limitLength(referrer,255);
		wineurl=limitLength(wineurl,255);
		boolean bot=Webroutines.checkBot(hostname,con);
		boolean suspectedbot=Webroutines.suspectedBot(hostname,useragent);
		String hostcountry=Webroutines.getCountryCodeFromIp(ip);
		java.sql.Timestamp now = new java.sql.Timestamp(startload); 
		if((!Configuration.donotlogforhost.contains(";"+hostname+";")&&hostname!=null&&!referrer.contains("host-tracker"))||(Wijnzoeker.serverrole.equals("DEV"))){
			if (loggingtype.equals(loggingtypes.link)){
				if (false&&cpc>0){
					try{
						if (!bot){
							boolean success=false;
							Dbutil.executeQuery("Insert into billingoverview (date,partnerid,shopid,type,referenceid,cpc,invoice) values ('"+now+"',"+partnerid+","+shopid+",'sponsoredlink',"+adid+","+cpc+",0);",con);
							rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
							if (rs.next()){
								int billingoverviewid=rs.getInt("id");
								Dbutil.executeQuery("Insert into sponsoredlinksclicked (date,wineid,partnerid,shopid,ip,url,cpc,billingoverviewid,wine,vintage) values ('"+now+"',"+wineid+","+partnerid+","+shopid+",'"+ip+"','"+wineurl+"',"+cpc+","+billingoverviewid+",'"+hostcountry+"','"+Spider.SQLEscape(winename)+"',"+vintage+");",con);
								rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
								if (rs.next()){
									int sponsoredlinksclickedid=rs.getInt("id");
									Dbutil.executeQuery("Update billingoverview set referenceid="+sponsoredlinksclickedid+" where id="+billingoverviewid+";",con);
								}
							}
							if (!success){
								Dbutil.logger.error("Problem while saving link click info to the database. Data: shopid, region, winename, partnerid, payperclick="+shopid+", "+region+", "+winename+", "+partnerid+", "+cpc);
							}
						}
					} catch (Exception e){
						Dbutil.logger.error("Problem while saving link click info to the database. Data: shopid, region, winename, partnerid, payperclick="+shopid+", "+region+", "+winename+", "+partnerid+", "+cpc, e);
					}
				}
				if (searchhistory!=null){
					rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as logid;", con);
					try{
						if (rs.next()){
							id=rs.getInt("logid");
						}
					} catch (Exception e){
						Dbutil.logger.error("Could not retrieve the last insert ID while logging webaction",e);
					}
					if (id>0&&type.equals("Link Clicked")){
						searchhistory.setClicks(id);
					}
					if (id>0&&type.equals("Search")){
						searchhistory.setSearches(id);
					}
				}
			}

			if (loggingtype.equals(loggingtypes.ad)){
				try{
					if (!bot){
						boolean success=false;
						Dbutil.executeQuery("Insert into billingoverview (date,partnerid,shopid,type,referenceid,cpc,invoice) values ('"+now+"',"+partnerid+","+shopid+",'banners',"+adid+","+cpc+",0);",con);
						rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
						if (rs.next()){
							int billingoverviewid=rs.getInt("id");
							Dbutil.executeQuery("Insert into bannersclicked (date,partnerid,bannerid,adoverviewid,shopid,ipaddress,hostname,income,hostcountry,region,wine) values ('"+now+"','"+partnerid+"','"+adid+"',0,'"+shopid+"','"+ip+"','"+hostname+"',"+cpc+",'"+hostcountry+"','"+Spider.SQLEscape(region)+"','"+Spider.SQLEscape(winename)+"');",con);
							rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
							if (rs.next()){
								int bannersclickedid=rs.getInt("id");
								//Dbutil.executeQuery("Update billingoverview set referenceid="+bannersclickedid+" where id="+billingoverviewid+";",con);
								Dbutil.executeQuery("Update banners set clicks=clicks+1 where id="+adid+";",con);
								success=true;
							}
						}
						if (!success) {
							Dbutil.logger.error("Problem while saving ad click info to the database. Data: adid, region, winename, partnerid, payperclick="+adid+", "+region+", "+winename+", "+partnerid+", "+cpc);
						}
					}
				} catch (Exception e){
					Dbutil.logger.error("Problem while saving ad click info to the database. Data: adid, region, winename, partnerid, payperclick="+adid+", "+region+", "+winename+", "+partnerid+", "+cpc, e);
				}

			}
			if (loggingtype.equals(loggingtypes.winead)){
				try{
					if (!bot){
						Dbutil.executeQuery("Update wineads set clicks=clicks+1 where wineid="+wineid+";",con);

					}
				} catch (Exception e){
					Dbutil.logger.error("Problem while saving ad click info to the database. Data: adid, region, winename, partnerid, payperclick="+adid+", "+region+", "+winename+", "+partnerid+", "+cpc, e);
				}

			}
			if (!bot) {
				Dbutil.executeQuery("Insert low_priority into logging (type, knownwineid, page, date, ip, hostname, hostcountry, referrer, name, vintage, countryseller, shopid, wineid, wineurl,bot, numberofresults,loadtime,wineprice,bannersshown) values ('"+type+"',"+knownwineid+",'"+Spider.SQLEscape(page)+"','"+now+"','"+ip+"','"+hostname+"','"+hostcountry+"','"+Spider.SQLEscape(referrer)+"','"+
						Spider.SQLEscape(name)+"',"+vintageint+",'"+countryseller+"',"+shopid+","+wineid+",'"+Spider.SQLEscape(wineurl)+"',"+suspectedbot+","+numberofresults+","+loadtime+","+price+",'"+bannersshown+"');",con);
				String b=bannersshown;
				if (b!=null&&b.length()>0){
					if (b.endsWith(",")) b=b.substring(0,b.length()-1);
					Dbutil.executeQuery("update low_priority banners set views=views+1 where id in ("+b+");", con);
				}
			}

		}
		} catch (Exception e){
			Dbutil.logger.error("Problem while logging Webactionlogger", e);
		} finally{ 
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		//loadtime=0;
	}

	public void logaction(){
		if (loadtime==0&&startload>0) loadtime=System.currentTimeMillis()-startload;
		if (QueueLogger.getLogger().getQueueLength()<10000){ 
			QueueLogger.getLogger().log(this);
		} else {
			new Thread(this).start();
		}
	}

	public void logwithqueue(){
		if (loadtime==0&&startload>0) loadtime=System.currentTimeMillis()-startload;
		QueueLogger.getLogger().log(this);
		//new Thread(this).start();
	}

	public String getBannersshown() {
		return bannersshown;
	}

	public void setBannersshown(String bannersshown) {
		this.bannersshown = bannersshown;
	}


}

/*
 * Created on 24-mrt-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.obsolete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.Spider.UrlSpider;
import com.freewinesearcher.batch.TableScraper;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Wine;



/**
 * @author Jasper
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Autospider implements Runnable {

	/**
	 * 
	 */
	String auto;
	String url; //Master url
	String shopname="";
	String Postdata;
	//String Masterurl;
	String amountwines="";
	String wsshopsid="";
	String countrycode="";
	String id;
	String encoding;
	ArrayList<String> StandardUrlregex = getStandardUrlRegex();
	public Connection executecon;
	java.sql.Timestamp now;
	boolean pagenotfound;
	int shopid;

	public static ArrayList<String> getStandardUrlRegex(){
		ArrayList<String> StandardUrlregex = new ArrayList<String>(0);
		StandardUrlregex.add("(?:href=|frame [^>]*src=)(?:'|\")([^'\"]*?)(?:'|\")"); //Regex
		StandardUrlregex.add(".*\\.css::.*\\.GIF::.*\\.PNG::.*\\.jpg::.*\\.JPG::.*\\.png::.*\\.gif::#.*::.*[Jj]ava[Ss]cript.*::(&|&amp;|\\?)osCsid=[a-zA-Z0-9]*::.*buy_now.*::/XTCsid.*::(&|&amp;|\\?)XTCsid=[a-zA-Z0-9]*::.*cPath.*products_id.*:"); //Filter
		return StandardUrlregex;
	}

	//	used in case we add a known ws shop
	public static int addAutospider(String wsid) { //used in case we add a known ws shop
		Connection con=Dbutil.openNewConnection();
		Connection Spidercon=Dbutil.openNewConnection();
		String currency="";
		String auto;
		String url;
		String id="";
		String baseurl;
		String shopname="";
		String Postdata="";
		String amountwines="";
		String countrycode="";
		String exvat="2";
		String encoding;
		ResultSet rs=null;
		ArrayList<String> Urlregex = getStandardUrlRegex();
		Webpage webpage=new Webpage();
		int shopid=0;

		try {

			// Get ws shop info

			rs=Dbutil.selectQuery("Select * from autoshops where wsshopsid='"+wsid+"';", con);
			while (rs.next()){
				String oldshopid=rs.getString("id");
				Dbutil.logger.error("WS shopid already exists in autoshops: "+wsid+". Removing all data.");
				Dbutil.executeQuery("Delete from autoshops where id='"+oldshopid+"';");
				Dbutil.executeQuery("Delete from autoscrapelist where shopid='"+oldshopid+"';");
				Dbutil.executeQuery("Delete from autowebpage where shopid='"+oldshopid+"';");
				Dbutil.executeQuery("Delete from autospiderregex where shopid='"+oldshopid+"';");
				Dbutil.executeQuery("Delete from autotablescraper where shopid='"+oldshopid+"';");

			} 
			rs=Dbutil.selectQuery("Select * from wsshops where wsid='"+wsid+"';", con);
			if (!rs.next()){
				Dbutil.logger.error("WS shopid not found:"+wsid);
			} else {
				url=rs.getString("url").replaceAll("%2F",	"/");
				shopname=rs.getString("name");
				countrycode=rs.getString("countrycode");
				amountwines=rs.getString("numberofwines");

				//Construct parameters for storing
				if (!url.contains("http")) url="http://"+url; 
				int slashposition=url.indexOf("/", 8);
				if (slashposition==-1) slashposition=url.length();
				baseurl=url.substring(0, slashposition);
				webpage= new Webpage("", "ISO-8859-1", "", true,true, shopid, shopname);

				try {
					rs = Dbutil.selectQuery("SELECT currency " +
							"from vat where countrycode='"+countrycode+"';", Spidercon);
					if (rs.next()){
						currency=rs.getString("currency");
					}
				} catch (Exception e) {
					Dbutil.logger.error("Problem while retrieving currency info",e);
				}

				// add shop
				encoding=Spider.getHtmlEncoding(url);
				Dbutil.executeQuery("Insert into autoshops (shopname, baseurl, shopurl, urltype, countrycode,currency,amountwines,status,wsshopsid,exvat,encoding) values ('"+shopname+"','"+baseurl+"','"+url+"','Spider','"+countrycode+"','"+currency+"','"+amountwines+"','New','"+wsid+"',"+exvat+",'"+encoding+"');", Spidercon);
				rs=Dbutil.selectQuery("Select * from autoshops where wsshopsid="+wsid+";", Spidercon);
				if (!rs.next()){
					Dbutil.logger.error("Problem while inserting shop in autoshops using wsshopsid "+wsid);
				} else {
					id=rs.getString("id");
					shopid=rs.getInt("id");
					
				}
				try {
					rs=Dbutil.selectQuery("select * from autospiderregex where shopid="+id+";", Spidercon);
					if (!rs.next()){
						Dbutil.executeQuery("Insert into autospiderregex (shopid,regex,filter) values ("+id+",'"+Spider.SQLEscape(Urlregex.get(0))+"','"+Spider.SQLEscape(Urlregex.get(1))+"');", Spidercon);
					} else {
						Dbutil.logger.info("Already found autospiderregex for shopid "+id+", using that one");
					}

				} catch (Exception e) {
					Dbutil.logger.error("Problem while retrieving/adding autospiderregex data",e);
				}




			}
		} catch (Exception e){
			Dbutil.logger.error("Exception in auto Scrape site Wsshop "+id, e);
		}finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(Spidercon);
		}
		
		return shopid;
	}


	//	used in case we add a completely new shop
	public static int addAutospider(String url, String Postdata) { //used in case we add a completely new shop
		Connection Spidercon=Dbutil.openNewConnection();
		String currency="";
		String auto;
		String id="";
		String baseurl="";
		String shopname;
		String amountwines="0";
		String wsshopsid="";
		String countrycode="";
		ResultSet rs=null;
		ArrayList<String> Urlregex = getStandardUrlRegex();
		int shopid=0;

		try {
			//			Construct parameters for storing
			if (!url.contains("http")) url="http://"+url; 
			int slashposition=url.indexOf("/", 8);
			if (slashposition==-1) slashposition=url.length();
			baseurl=url.substring(0, slashposition);
			shopname=baseurl;


			// add shop
			String encoding=Spider.getHtmlEncoding(url);
			Dbutil.executeQuery("Insert into autoshops (shopname, baseurl, shopurl, urltype, countrycode,currency,amountwines,status,wsshopsid,exvat,encoding) values ('"+shopname+"','"+baseurl+"','"+url+"','Spider','"+countrycode+"','"+currency+"','"+amountwines+"','New','"+wsshopsid+"','2','"+encoding+"');", Spidercon);
			rs=Dbutil.selectQuery("Select * from autoshops where baseurl='"+baseurl+"';", Spidercon);
			if (!rs.next()){
				Dbutil.logger.error("Problem while inserting shop in autoshops using baseurl "+baseurl);
			} else {
				id=rs.getString("id");
				shopid=rs.getInt("id");
			}
			try {
				rs=Dbutil.selectQuery("select * from autospiderregex where shopid="+id+";", Spidercon);
				if (!rs.next()){
					Dbutil.executeQuery("Insert into autospiderregex (shopid,regex,filter) values ("+id+",'"+Spider.SQLEscape(Urlregex.get(0))+"','"+Spider.SQLEscape(Urlregex.get(1))+"');", Spidercon);
				} else {
					Dbutil.logger.info("Already found autospiderregex for shopid "+id+", using that one");
				}

			} catch (Exception e) {
				Dbutil.logger.error("Problem while retrieving/adding autospiderregex data",e);
			}

		} catch (Exception e){
			Dbutil.logger.error("Exception in autoScrape site for url "+url, e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(Spidercon);
		}

		return shopid;
	}


	//	used in case we restart a scan, possibly with a new URL and postdata
	public Autospider(String id, String url, String Postdata) { 
		super();
		this.auto="auto";
		this.id=id;
		this.shopid=0;
		try{this.shopid=Integer.parseInt(id);}catch(Exception e){}
		if (url==null) url="";
		if (Postdata==null) Postdata="";
		this.Postdata=Postdata;
		this.url=url;
		if (!url.equals("")){
			Dbutil.executeQuery("update autoscrapelist set url='"+Spider.SQLEscape(url)+"' where urltype='Master' and shopid="+id+";");
		}
		if (!Postdata.equals("")){
			Dbutil.executeQuery("update autoscrapelist set postdata='"+Spider.SQLEscape(Postdata)+"' where urltype='Master' and shopid="+id+";");
		}
	}

	public void run() {
		if (Wijnzoeker.stopautoshop.contains(id)) Wijnzoeker.stopautoshop="";
		Connection Spidercon=Dbutil.openNewConnection();
		ResultSet rs=null;	
		Spider spider=new Spider(id,"","auto",1);	
		spider.SpiderStarted();
		Thread.currentThread().setName("AutoShop "+id);
		Dbutil.logger.info("Starting spidering of shop (auto) "+id+" ("+spider.getShopName()+")");
		Dbutil.executeQuery("update autoshops set status='Busy: Initializing' where id='"+id+"';");
		String baseurl=spider.getBaseUrl();
		now = new java.sql.Timestamp(new java.util.Date().getTime()); 
		ArrayList<String> Urlsfound = new ArrayList(0); // temp container for found Urls while spidering
		ArrayList<UrlSpider> Urlregex = spider.getUrlRegex();
		ArrayList<String> StandardUrlregex = getStandardUrlRegex();
		Webpage webpage=new Webpage();
		webpage.maxattempts=1;

		ArrayList<ArrayList<String>> Urllist;
		boolean moretodo;
		String Page;
		int totaldatafromweb=0;
		String Url; //temp url container
		String ignorepagenotfound="Y";
		String Regex="Leeg";
		String Regexescaped="Leeg";
		String Headerregex="";
		String Headerregexescaped="";
		String Order="Leeg";
		String tablescraper="";
		String viewstate="";
		String urlrowid="";
		String type;
		String Parenturl;
		Wine[] wine;
		Double pricefactorex;
		Double pricefactorin;
		int totalscraped=0;
		Variables var=new Variables();
		
		boolean isOsCommerceShop=false;
		String urlregex;
		String winesep="";
		String fieldsep="";
		String filter="";
		String nameorder="";
		String nameregex="";
		String nameexclpattern="\\d\\d\\d\\d+";
		String vintageregex="";
		String vintageorder="";
		String priceregex="";
		String priceorder="";
		String sizeregex="";
		String sizeorder="";
		String headerregex="";
		String postdata="";
		String AnalysisHTML="";
		encoding=Dbutil.readValueFromDB("Select encoding from autoshops where id="+id+";", "encoding");

		
		pricefactorex=1.0;
		pricefactorin=1.0;

		try {
			rs=Dbutil.selectQuery("select * from autospiderregex where shopid="+id+";", Spidercon);
			if (!rs.next()){
				Dbutil.executeQuery("Insert into autospiderregex (shopid,regex,filter) values ("+id+",'"+Spider.SQLEscape(StandardUrlregex.get(0))+"','"+Spider.SQLEscape(StandardUrlregex.get(1))+"');", Spidercon);
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving/adding autospiderregex data",e);
		}
		try {
			Dbutil.executeQuery("delete from autowines where shopid="+id+";");

		} catch (Exception e) {
			Dbutil.logger.error("Problem while deleting old wines",e);
		}


		// Put master url in autoscrapelist
		try {
			rs=Dbutil.selectQuery("select * from autoscrapelist where shopid="+id+" and urltype='Master';", Spidercon);
			if (!rs.next()){

				// No autoscrapelist entry, let's find out the best guess
				if (url.equals("")) {
					rs=Dbutil.selectQuery("Select * from autoshops where id="+id+";", Spidercon);
					rs.next();
					url=rs.getString("shopurl");
				}
				Dbutil.executeQuery("delete from autoscrapelist where shopid="+id+";");
				Dbutil.executeQuery("delete from autowines where shopid="+id+";");
				Dbutil.executeQuery("delete from autotablescraper where shopid="+id+";");
				Dbutil.executeQuery("insert into autoscrapelist (shopid, url, urltype, status,postdata,regex,scrapeorder,headerregex,parenturl,urlsource) values ("+id+", '"+url+"', 'Master','Ready','"+Postdata+"','','','','','');");

				try{ 
					// Now let's scrape as long as we find records with status Ready
					// The Scrapelist loop gets reinitialized so we see new URL's
					// All web pages found are stored in autoWebPage table so we can use them later on
					Dbutil.executeQuery("update autoshops set status='Busy: Initial web page retrieval' where id='"+id+"';");
					int limit=300;
					int counter=0;
					moretodo=true;
					while (counter<limit&&moretodo&&!Wijnzoeker.muststopnow&&!Wijnzoeker.stopautoshop.contains(id)){
						Urllist=spider.getScrapeList("Ready");
						if (Urllist.size()==0) moretodo=false;
						for (int i = 0;i<Urllist.size();i++){
							counter++;
							if (counter<limit&&!Wijnzoeker.muststopnow&&!Wijnzoeker.stopautoshop.contains(id)) {
								Dbutil.executeQuery("update autoshops set status='Busy: Initial web page retrieval, i="+counter+", time="+new java.sql.Timestamp(new java.util.Date().getTime())+"' where id='"+id+"';");
								Url=Urllist.get(i).get(0);
								Postdata=Urllist.get(i).get(1);
								type=Urllist.get(i).get(2);
								Regex=Urllist.get(i).get(3);
								Headerregex=Urllist.get(i).get(4);
								Order=Urllist.get(i).get(5);
								tablescraper=Urllist.get(i).get(6);
								Parenturl=Urllist.get(i).get(7);
								viewstate=Urllist.get(i).get(8);
								urlrowid=Urllist.get(i).get(9);
								spider.updateUrlStatus(urlrowid,"Scraping");
								Regexescaped = Spider.replaceString(Regex, "\\", "\\\\");
								Regexescaped =  Spider.replaceString(Regexescaped, "'", "\\'");
								Headerregexescaped =  Spider.replaceString(Headerregex, "\\", "\\\\");
								Headerregexescaped =  Spider.replaceString(Headerregexescaped, "'", "\\'");

								Urlsfound=null;
								if (autowebpagestored(Url)){
									Page=getAutoWebPage(Url);
								} else {
									webpage.postdata=Postdata;
									webpage.encoding=encoding;
									webpage.urlstring=Url;
									webpage.ignorepagenotfound=true;
									webpage.followredirect=true;
									webpage.readPage();
									Page=webpage.html;// Store the webpage for future use
									// This way we don't bother the web site itself when trying out different algorythms
									saveAutoWebPage(Url, Page);
								}
								totaldatafromweb+=Page.length();

								if (!Page.startsWith("Webpage")){


									//	Now we harvest all URLs if this is a master or spidered URL

									if (type.equals("Master")||type.equals("Spideredwithsource")){
										Urlsfound = Spider.ScrapeUrl(Page,Urlregex,Url,baseurl,id,Postdata,"notforurls", AnalysisHTML);
										//ArrayList<String> Cleaned=new ArrayList<String>();
										//for (int j=0;j<Urlsfound.size();j++){
										//	if (!Urlsfound.get(j).toLowerCase().contains("cpath")&&!Urlsfound.get(j).toLowerCase().contains("product_id")){
										//		Cleaned.add(Urlsfound.get(j));
										//	} else {
										//		isOsCommerceShop=true;
										//	}
										//}
										spider.addUrl(Urlsfound, Regexescaped, Headerregexescaped, tablescraper, Order, "0","Spideredwithsource",Postdata,Url);
									}
									Urlsfound=null;
									


									//	Now we count all prices on this page
									int count=countPrices(Page);
									spider.updateUrlWinesFound(urlrowid,count);
									spider.updateUrlStatus(urlrowid,"Done");
									wine=null; //Let's help the garbageman
								}
								Page=null;
								System.gc();
								spider.updateUrlStatus(urlrowid,"Done");
							}
						}
					}

				} catch (Exception exc){
					Dbutil.logger.error("Iets ging mis met het autospideren van shop (auto)"+id+" ("+spider.getShopName()+")"+": ",exc);

				}


				// So now we know which URLs there are on the site and how many prices each url contains.
				// Let's take the top 20 and figure out how the wine names are coded

				try{
					// Let's set everything to nowines in case we have more than 'limit' url's
					Dbutil.executeQuery("update autoshops set status='Busy: Determining table scraper config' where id='"+id+"';");
					Dbutil.executeQuery("update autoscrapelist set status='Skipped' where shopid="+id+";");	
					Connection con=Dbutil.openNewConnection();
					rs=Dbutil.selectQuery("select * from autoscrapelist where shopid="+id+" order by winesfound desc limit 20", Spidercon);
					while (rs.next()){
						Dbutil.executeQuery("update autoscrapelist set status='Ready' where shopid="+id+" and id='"+rs.getString("id")+"';");
						Dbutil.logger.debug("Using url "+rs.getString("url")+" with "+rs.getString("winesfound")+" pricefields");
					}
					Dbutil.closeConnection(con);
				} catch (Exception exc){
					Dbutil.logger.error("Iets ging mis met het autospideren van shop (auto)"+id+" ("+spider.getShopName()+")"+": ",exc);

				}
				// Go over each of the top URLs and let the tablescraper figure out how wines are coded.
				// Save each result in a separate tablescraperow

				try{ 

					moretodo=true;
					while (moretodo&&!Wijnzoeker.muststopnow&&!Wijnzoeker.stopautoshop.contains(id)){
						Urllist=spider.getScrapeList("Ready");
						if (Urllist.size()==0) moretodo=false;
						for (int i = 0;i<Urllist.size();i++){
							Url=Urllist.get(i).get(0);
							Postdata=Urllist.get(i).get(1);
							urlrowid=Urllist.get(i).get(9);
							spider.updateUrlStatus(urlrowid,"Scraping");
							Page= getAutoWebPage(Url);
							ArrayList<String> Analysis = TableScraper.Analyzer(Page, url, "","","","","");
							wine=TableScraper.ScrapeWine(Page, shopid, url, "href=(?:['\"])?([^'\" >]*?)['\" >]",url, baseurl, headerregex, tablescraper, Analysis.get(1), Analysis.get(2), filter, Analysis.get(3), Analysis.get(5), Analysis.get(6),
									Analysis.get(7), Analysis.get(8), Analysis.get(9), Analysis.get(10), Analysis.get(4), "", now, pricefactorex, pricefactorin, null,false);
							AnalysisHTML=Analysis.get(0);
							winesep= Spider.replaceString(Spider.replaceString(Analysis.get(1), "\\", "\\\\"), "'", "\\'");		
							fieldsep=Spider.replaceString(Spider.replaceString(Analysis.get(2), "\\", "\\\\"), "'", "\\'");
							nameorder=Spider.replaceString(Spider.replaceString(Analysis.get(3), "\\", "\\\\"), "'", "\\'");
							sizeorder=Spider.replaceString(Spider.replaceString(Analysis.get(4), "\\", "\\\\"), "'", "\\'");
							nameregex=Spider.replaceString(Spider.replaceString(Analysis.get(5), "\\", "\\\\"), "'", "\\'");
							nameexclpattern=Spider.replaceString(Spider.replaceString(Analysis.get(6), "\\", "\\\\"), "'", "\\'");
							vintageorder=Spider.replaceString(Spider.replaceString(Analysis.get(7), "\\", "\\\\"), "'", "\\'");
							vintageregex=Spider.replaceString(Spider.replaceString(Analysis.get(8), "\\", "\\\\"), "'", "\\'");
							priceorder=Spider.replaceString(Spider.replaceString(Analysis.get(9), "\\", "\\\\"), "'", "\\'");
							priceregex=Spider.replaceString(Spider.replaceString(Analysis.get(10), "\\", "\\\\"), "'", "\\'");
							urlregex=Spider.replaceString(Spider.replaceString("href=(?:['\"])?([^'\" >]*?)['\" >]", "\\", "\\\\"), "'", "\\'");
							if (nameorder.length()<40){ //longer is ridiculous
								TableScraper.addAutoTableScrapeRow(id, winesep, fieldsep, filter, nameorder, nameregex, nameexclpattern, vintageorder, vintageregex, priceorder, priceregex, sizeorder, sizeregex, urlregex, postdata, wine.length);
							}
						}
					}
				} catch (Exception exc){
					Dbutil.logger.error("Iets ging mis met het autospideren van shop (auto)"+id+" ("+spider.getShopName()+")"+": ",exc);

				}

				// So let's figure out the most common wine scraping parameters and use that for the master url
				try {
					rs=Dbutil.selectQuery("Select *, sum(winesfound) as number from autotablescraper where shopid="+id+" group by winesep,fieldsep order by number desc;", Spidercon); 
					rs.next();
					winesep=Spider.SQLEscape(rs.getString("winesep"));
					fieldsep=Spider.SQLEscape(rs.getString("fieldsep"));
					rs=Dbutil.selectQuery("Select *, sum(winesfound) as number from autotablescraper where shopid="+id+" and winesep='"+winesep+"' and fieldsep='"+fieldsep+"' and vintageorder>=0 group by vintageorder order by number desc;", Spidercon); 
					vintageorder="";
					if (rs.next()){
						vintageorder=rs.getString("vintageorder");
					}
					rs=Dbutil.selectQuery("Select *, sum(winesfound) as number from autotablescraper where shopid="+id+" and winesep='"+winesep+"' and fieldsep='"+fieldsep+"' and sizeorder>=0 group by sizeorder order by number desc;", Spidercon); 
					sizeorder="";
					if (rs.next()){
						sizeorder=rs.getString("sizeorder");
					}
					rs=Dbutil.selectQuery("Select *, sum(winesfound) as number from autotablescraper where shopid="+id+" and winesep='"+winesep+"' and fieldsep='"+fieldsep+"' group by nameorder order by number desc;", Spidercon); 
					rs.next();
					int mostcommonrow=rs.getInt("id");
					Dbutil.executeQuery("Update autotablescraper set sizeorder='"+sizeorder+"', vintageorder='"+vintageorder+"' where id="+mostcommonrow+";");
					Dbutil.executeQuery("Update autoscrapelist set tablescraper="+mostcommonrow+" where shopid="+id+" and urltype='Master';");
				} catch (Exception exc){
					Dbutil.logger.error("Iets ging mis met het autospideren van shop (auto)"+id+" ("+spider.getShopName()+")"+": ",exc);
				}

			} else { // So we reuse the tablescraper that was apparently edited
				Dbutil.logger.info("Reusing existing tablescraper for autoshop "+id);
				//String row=rs.getString("id");
				//Dbutil.executeQuery("delete from autoscrapelist where url='"+url+"' and id!="+row+";");
				//Dbutil.executeQuery("update autoscrapelist set url='"+url+"', postdata='"+postdata+"' where id="+row+";");

			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving/adding autoscrapelist data",e);
		}

		// Now we do a complete spider of the site, using our newly found wine parameters
		Dbutil.executeQuery("update autoshops set status='Busy: Complete spider storing wines' where id='"+id+"';");
		spider.updateUrlStatusses("","Delete");// Delete will only affect spidered URL's
		spider.updateUrlStatusses("","Ready");
		try{ 
			// Now let's scrape as long as we find records with status Ready
			// The Scrapelist loop gets reinitialized so we see new URL's
			int limit=300;
			int counter=0;
			moretodo=true;
			while (counter<limit&&moretodo&&!Wijnzoeker.muststopnow&&!Wijnzoeker.stopautoshop.contains(id)){
				Urllist=spider.getScrapeList("Ready");
				if (Urllist.size()==0) moretodo=false;
				for (int i = 0;i<Urllist.size();i++){
					counter++;
					if (counter<limit&&!Wijnzoeker.muststopnow&&!Wijnzoeker.stopautoshop.contains(id)){
						Dbutil.executeQuery("update autoshops set status='Busy: Complete spider storing wines, i="+counter+", time="+new java.sql.Timestamp(new java.util.Date().getTime())+"' where id='"+id+"';");
						Url=Urllist.get(i).get(0);
						Postdata=Urllist.get(i).get(1);
						type=Urllist.get(i).get(2);
						Regex=Urllist.get(i).get(3);
						Headerregex=Urllist.get(i).get(4);
						Order=Urllist.get(i).get(5);
						tablescraper=Urllist.get(i).get(6);
						Parenturl=Urllist.get(i).get(7);
						urlrowid=Urllist.get(i).get(9);
						spider.updateUrlStatus(urlrowid, "Scraping");
						
						Regexescaped = Spider.replaceString(Regex, "\\", "\\\\");
						Regexescaped =  Spider.replaceString(Regexescaped, "'", "\\'");
						Headerregexescaped =  Spider.replaceString(Headerregex, "\\", "\\\\");
						Headerregexescaped =  Spider.replaceString(Headerregexescaped, "'", "\\'");

						Urlsfound=null;
						//Page= Spider.getWebPage(Url, encoding, var,Postdata, ignorepagenotfound);
						if (autowebpagestored(Url)){
							Page=getAutoWebPage(Url);
						} else {
							Page= Spider.getWebPage(Url, encoding, var,Postdata, ignorepagenotfound);
							// Store the webpage for future use
							// This way we don't bother the web site itself when trying out different algorythms
							saveAutoWebPage(Url, Page);
						}
						totaldatafromweb+=Page.length();

						if (!Page.startsWith("Webpage")){
							//	Now we harvest all URLs if this is a master or spidered URL

							if (type.equals("Master")||type.equals("Spideredwithsource")){
								Urlsfound = Spider.ScrapeUrl(Page,Urlregex,Url,baseurl,id,Postdata,"notforurls", AnalysisHTML);
								spider.addUrl(Urlsfound, Regexescaped, Headerregexescaped, tablescraper, Order, "0","Spideredwithsource",Postdata,Url);
							}
							Urlsfound=null;

							//	Of course we harvest all wines for a shop
							wine=null;
							if (tablescraper.equals("0")){
								wine=Wine.ScrapeWine(Page, Url, Regex,Headerregex, shopid, Order,now, pricefactorex, pricefactorin);
							} else {
								wine=TableScraper.ScrapeWine(Page, baseurl,shopid, Headerregex, Url, tablescraper, now, pricefactorex, pricefactorin,auto);
							}
							Wine.addorupdatewine(wine,auto);
							totalscraped += wine.length;
							spider.updateUrlWinesFound(urlrowid,wine.length);
							if (wine.length==0){
								// URL is obsolete or does not contain wines
								spider.updateUrlStatus(urlrowid,"Nowines");
							} else {
								spider.updateUrlStatus(urlrowid,"Done");
							}
							wine=null; //Let's help the garbageman
						}
						Page=null;
						System.gc();
					}
				}
			}

			// Now we have a list of URL's from the site.
			// Let's see if we can improve the spiderregex if it is standard and if we know this kind of shop
			if (Urlregex.equals(StandardUrlregex)){
				rs=Dbutil.selectQuery("Select * from autoscrapelist where shopid="+id+" and url like '%cPath%'", Spidercon);
				if (rs.last()){
					if (rs.getRow()>5){
						// This is a shop with cPath
						Dbutil.executeQuery("update autospiderregex set regex='"+Spider.SQLEscape("(?:href=|frame [^>]*src=)(?:'|\")([^'\"]*?cPath[^'\"]*?)(?:'|\")")+"', filter='"+Spider.SQLEscape(StandardUrlregex.get(1))+Spider.SQLEscape(":.*products_id.*::&sort=[^&]*::&language=[^&]*:")+"' where shopid="+id+";");
					}
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Iets ging mis met het spideren van shop (auto)"+id+" ("+spider.getShopName()+")"+": ",exc);
			Dbutil.executeQuery("update autoshops set status='Crashed' where id='"+id+"';");

		}

		if (Wijnzoeker.stopautoshop.contains(id)){
			Dbutil.executeQuery("update autoshops set status='Aborted, time="+new java.sql.Timestamp(new java.util.Date().getTime())+"' where id='"+id+"';");
		} else {
			Dbutil.executeQuery("update autoshops set status='Finished, time="+new java.sql.Timestamp(new java.util.Date().getTime())+"' where id='"+id+"';");
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(Spidercon);
		wine=null;
		Urlsfound=null;
		Urlregex=null;
		Urllist=null;
		rs=null;
		Dbutil.logger.info(totaldatafromweb+" bytes scraped for shop (auto)"+id);
		String newmaster=findCommonParentUrl();
		Dbutil.logger.info("I think Masterrecord should be "+newmaster);
		Dbutil.executeQuery("update autoscrapelist set status='Spidered' where status like '%NewMaster%' and shopid="+id);
		Dbutil.executeQuery("update autoscrapelist set status='Spidered NewMaster' where url='"+Spider.SQLEscape(newmaster)+"' and shopid="+id);

	}

	private boolean autowebpagestored(String Url){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		boolean autowebpagestored=false;
		try {
			rs=Dbutil.selectQuery("select * from autowebpage where url='"+Spider.SQLEscape(Url)+"' and shopid="+id+";", con);
			if (rs.next()){
				autowebpagestored=true;
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving/adding autospiderregex data",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return autowebpagestored;
	}


	private int countPrices(String page){
		int total=0;
		Pattern pattern;
		Matcher matcher;
		String generalpriceregex=">(?:[^<]*?\\D)??(\\d*[,.]?\\d+[,.]\\d\\d)(?:\\D[^<]*?)?<";
		pattern = Pattern.compile(generalpriceregex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
		matcher = pattern.matcher(page);
		while (matcher.find()){
			String price=matcher.group(1);
			total++;
		}

		return total;

	}

	public void saveAutoWebPage(String Url, String Page){
		if (!Page.contains("PDF")){
			Dbutil.executeQuery("insert ignore into autowebpage(shopid,page, url) values ('"+id+"','"+Spider.SQLEscape(Page)+"','"+Spider.SQLEscape(Url)+"');");
		}
	}

	public String getAutoWebPage(String Url){
		String Page="Webpage not retrieved";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try {
			rs=Dbutil.selectQuery("Select * from autowebpage where shopid="+id+" and url='"+Spider.SQLEscape(Url)+"';",con);
			if (rs.next()){
				Page=rs.getString("Page");
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem retrieving autowebpage with url "+Url,exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return Page;
	}


	public String findCommonParentUrl(){
		String parent="";
		String master="";
		String masterrecord="";
		ResultSet rs=null;
		ResultSet allurls;
		int winerecord=0;
		ArrayList<ArrayList<String>> paths=new ArrayList<ArrayList<String>>();
		Connection con=Dbutil.openNewConnection();
		Connection allcon=Dbutil.openNewConnection();
		try{
			allurls=Dbutil.selectQuery("Select * from autoscrapelist where shopid="+id+" order by id;", allcon);
			int wines=0;
			int count=0;
			while (allurls.next()){
				paths.add(new ArrayList<String>(0));
				paths.get(count).add(allurls.getString("id")); //0=ID
				if (allurls.getInt("winesfound")>0){
					paths.get(count).add("Y"); //1=Y/N wine
					wines++;
				} else {
					paths.get(count).add("N");
				}
				rs=Dbutil.selectQuery("select * from autoscrapelist where url='"+Spider.SQLEscape(allurls.getString("parenturl"))+"' and shopid="+id+";", con);
				if (rs.next()){
					paths.get(count).add(rs.getString("id"));
				} else {
					paths.get(count).add("end");
				}
				count++;
			}
			for (int i=0;i<paths.size();i++){
				parent=paths.get(i).get(2);
				//Dbutil.logger.info("Looking for record "+i+" parent "+parent);
				while (!parent.equals("end")){
					for (int j=0;j<paths.size();j++){
						//Dbutil.logger.info("Checking Record "+j+" value "+paths.get(j).get(0)+", looking for parent "+parent+"for record "+i);
						if (paths.get(j).get(0).equals(parent)){
							paths.get(i).add(paths.get(j).get(2));
							parent=paths.get(j).get(2);
							j=999999;
						} 
					}
				}
				if (paths.get(i).get(1).equals("Y")) winerecord=i;
				String all=";";
				for (int j=2;j<paths.get(i).size();j++){
					all=all+paths.get(i).get(j)+";";
				}
				paths.get(i).add(all);
			}
			int i=2;
			boolean gotit=false;
			while (gotit==false&&i<paths.get(winerecord).size()){
				String record=paths.get(winerecord).get(i);
				boolean foundparent=true;
				for (int j=0;j<paths.size();j++){
					if (paths.get(j).get(1).equals("Y")&&
							!paths.get(j).get(paths.get(j).size()-1).contains(";"+record+";")){
						foundparent=false;
					}
				}
				if (foundparent=true) {
					gotit=true;
					masterrecord=record;
				}
				i++;
			}
			rs=Dbutil.selectQuery("select * from autoscrapelist where id='"+masterrecord+"' and shopid="+id+";", con);
			if (rs.next()){
				master=rs.getString("url");
			}
			return master;


		} catch (Exception e){
			Dbutil.logger.error("Problem finding common parent URL",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(allcon);
		}

		return parent;
	}
}
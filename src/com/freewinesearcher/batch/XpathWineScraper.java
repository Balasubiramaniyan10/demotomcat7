package com.freewinesearcher.batch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.w3c.dom.Document;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.Webroutines;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.searchasaservice.parser.xpathparser.XpathParser;
import com.searchasaservice.parser.xpathparser.Record;
import com.searchasaservice.parser.xpathparser.Result;
import com.vinopedia.htmlunit.HtmlUnitParser;

public class XpathWineScraper {
	
	String Page; 
	String Baseurl; 
	String Url; 
	String shopurl;
	int shopid;
	Timestamp now; 
	Double pricefactorex; 
	Double pricefactorin;
	String shopname="";
	String country="";
	ArrayList<XpathParser> parsers;
	
	
	public XpathWineScraper(String baseurl, String country, Timestamp now,
			Double pricefactorex, Double pricefactorin, int shopid,
			String shopname, String shopurl) {
		super();
		Baseurl = baseurl;
		this.shopurl=shopurl;
		this.country = country;
		this.now = now;
		this.pricefactorex = pricefactorex;
		this.pricefactorin = pricefactorin;
		this.shopid = shopid;
		this.shopname = shopname;
	}
	
	public Wine[] scrape(String page, String url,String postdata) throws FailingHttpStatusCodeException, MalformedURLException, IOException{
		Url=url;
		ArrayList<Wine> wine=new ArrayList<Wine>();
		//Document doc=HTMLParser.parsePage(page).document;
		HtmlUnitParser p=new HtmlUnitParser(url, postdata);
		
		try {
			if (parsers != null) {
				for (XpathParser parser : parsers) {
					parser.document=p.page;
					
					wine.addAll(getWinesAL(parser.parse(),parser.config.appendwineryname));
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem while scraping wines for shopid "+shopid, e);
		}
		Wine[] array=new Wine[wine.size()];
		array=wine.toArray(array);
		return array;
	}
	
	public static String guessCountryCode(String baseurl,int shopid){
		String country="";
		if (shopid>0) country=Dbutil.readValueFromDB("select countrycode from shops where id="+shopid,"countrycode");
		if (country.equals("")){
			country=Webroutines.getRegexPatternValue("\\.(\\w\\w)$", baseurl).toUpperCase();
		}
		if (country.equals("")){
			try {
				String hostname=baseurl.toLowerCase();
				if (hostname.startsWith("http://")) hostname=hostname.substring(7);
				if (hostname.startsWith("https://")) hostname=hostname.substring(8);
				InetAddress host = InetAddress.getByName(hostname);
				String ip = host.getHostAddress();
				country=Webroutines.getCountryCodeFromIp(ip);
			} catch (Exception e) {
				Dbutil.logger.info("Could not lookup host ip for host "+baseurl.toLowerCase());
			}
		}
		if (country.equals("")) country="NL";
		if (country.equals("UK")) country="GB";
		return country;
	}

	public XpathWineScraper(String baseurl, String url, 
			Timestamp now, int shopid) {
		super();
		Baseurl = baseurl;
		Url = url;
		this.now = now;
		this.pricefactorex = Dbutil.getPriceFactorex(shopid+"");
		this.pricefactorin = Dbutil.getPriceFactorin(shopid+"");
				shopname=Webroutines.getShopNameFromShopId(shopid, "");
		if (shopname.equals("")) {
			shopname=baseurl.toLowerCase();
			if (shopname.startsWith("http://")) shopname=shopname.substring(7);
			if (shopname.startsWith("https://")) shopname=shopname.substring(8);
			if (shopname.startsWith("www.")) shopname=shopname.substring(4);
			shopname=shopname.substring(0, 1).toUpperCase()+shopname.substring(1);
		}
		country=guessCountryCode(baseurl, shopid);
		if(shopid==0&&!country.equals("")){
			String currency=Dbutil.readValueFromDB("Select currency from shops where countrycode='"+country+"' group by currency order by count(*) desc;","currency");
			try {
				Double rate = Double.parseDouble(Dbutil.readValueFromDB(
						"Select rate from currency where currency='" + currency
								+ "';", "rate"));
				pricefactorin = rate;
				pricefactorex = rate;
			} catch (Exception e) {
				
			}
		
		}

	}
	public ArrayList<Wine> getWinesAL(Result result, boolean appendshopname){
		ArrayList<Wine> wines=new ArrayList<Wine>();
		try {
			int i=0;
			for (Record record:result){
				wines.add(new Wine (record,pricefactorex,pricefactorin,(Double)0.0,country,"","",0,now+"",false,null,shopid,shopname,shopurl,Url,"",Baseurl,appendshopname));
				i++;
			}
			
		
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return wines;
	}

	
	public Wine[] getWines(Result result, boolean appendshopname){
		Wine[] wines=null;
		try {
			wines=new Wine[result.size()];
			int i=0;
			for (Record record:result){
				wines[i]=new Wine (record,pricefactorex,pricefactorin,(Double)0.0,country,"","",0,now+"",false,null,shopid,shopname,"",Url,"",Baseurl,appendshopname);
				i++;
			}
			
		
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return wines;
	}
	
}

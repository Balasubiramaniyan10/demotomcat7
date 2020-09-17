/*
 * Created on 10-mrt-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.online;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.Emailer;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.StoreReport;
import com.freewinesearcher.batch.sitescrapers.SuckCT;
import com.freewinesearcher.batch.sitescrapers.SuckCTLabels;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.common.Wineset;
import com.freewinesearcher.online.WineAdvice.Searchtypes;
import com.freewinesearcher.online.shoppingcart.Shoppingcart;
import com.searchasaservice.ai.Recognizer;

/**
 * @author Jasper
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Webroutines { // Some routines used in the web pages
	static {
		new Dbutil();
	};
	public static final int numberofnormalrows = Configuration.numberofnormalrows;
	public static final float[] targetprice = getTargetPrices();

	public static Map sortByValue(Map map, final boolean reverse) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (reverse) {
					return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());

				} else {
					return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
				}
			}
		});
		// logger.info(list);
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static float[] getTargetPrices() {
		float[] prices = new float[101];
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String query = "select * from pqratio;";
		try {
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				prices[rs.getInt("rating")] = rs.getFloat("price");
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem while getting PQ ratios", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return prices;

	}

	public static int Saveshop(String id, String shopname, String shopurl, String url, String type, String country,
			String vat, String auto, String email, String address, String disabled, String vatnumber,
			String invoiceaddress, String invoiceemail, String contactname, String commercialcomment,
			String staffelname, Timestamp starttrial, Timestamp startpaying, String sponsoringshop, String linkback) { // saves
																														// a
																														// new
																														// shop
		int i = 0;
		boolean succes = false;
		String currency;
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;
		try {

			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
			if (!url.startsWith("http")) {
				url = "http://" + url;
			}

			rs = Dbutil.selectQuery("SELECT currency " + "from vat where countrycode='" + country + "';", con);
			rs.next();
			currency = rs.getString("currency");

			if (id.equals("0")) { // add a new shop!

				rs = Dbutil.selectQuery(
						"select * from " + auto + "shops where shopname='" + Spider.SQLEscape(shopname) + "';", con);
				if (!rs.next()) {
					Dbutil.closeRs(rs);
					i = Dbutil.executeQuery("Insert LOW_PRIORITY into " + auto
							+ "shops (shopname, shopurl, baseurl, urltype, countrycode, exvat, currency, email,address,disabled,vatnumber,invoiceaddress,invoiceemail,contactname,commercialcomment,staffelname,starttrial,startpaying,description,sponsoringshop,linkback) values ('"
							+ Spider.SQLEscape(shopname) + "','" + shopurl + "','" + url + "','" + type + "','"
							+ country + "','" + vat + "','" + currency + "','" + email + "','"
							+ Spider.SQLEscape(address) + "'," + disabled + ",'" + Spider.SQLEscape(vatnumber) + "','"
							+ Spider.SQLEscape(invoiceaddress) + "','" + Spider.SQLEscape(invoiceemail) + "','"
							+ Spider.SQLEscape(contactname) + "','" + Spider.SQLEscape(commercialcomment) + "','"
							+ Spider.SQLEscape(staffelname) + "','" + (starttrial) + "','" + (startpaying) + "','',"
							+ Spider.SQLEscape(sponsoringshop) + ",'" + (Spider.SQLEscape(linkback)) + "');", con);

					rs = Dbutil.selectQuery("select LAST_INSERT_ID() as shopid;", con);
					rs.next();
					i = rs.getInt("shopid");
					Dbutil.executeQuery("insert LOW_PRIORITY into shopstats (shopid,disabled,lastchange) values (" + i
							+ "," + disabled + ",sysdate());");
					succes = true;
				}
			} else { // Shop url already exists; update the values
				Dbutil.executeQuery("update LOW_PRIORITY " + auto + "shops set shopname = '"
						+ Spider.SQLEscape(shopname) + "', baseurl = '" + url + "', urltype= '" + type
						+ "', countrycode= '" + country + "', exvat= '" + vat + "', shopurl ='" + shopurl
						+ "', email ='" + email + "', address='" + Spider.SQLEscape(address) + "', disabled=" + disabled
						+ ",vatnumber='" + Spider.SQLEscape(vatnumber) + "',invoiceaddress='"
						+ Spider.SQLEscape(invoiceaddress) + "',invoiceemail='" + Spider.SQLEscape(invoiceemail)
						+ "',contactname='" + Spider.SQLEscape(contactname) + "',commercialcomment='"
						+ Spider.SQLEscape(commercialcomment) + "',staffelname='" + Spider.SQLEscape(staffelname) + "'"
						+ (starttrial == null ? "" : ",starttrial='" + starttrial + "'")
						+ (startpaying == null ? "" : ",startpaying='" + startpaying + "'") + ",sponsoringshop="
						+ sponsoringshop + ",linkback='" + Spider.SQLEscape(linkback) + "'  where id=" + id + ";");
				i = Integer.parseInt(id);
				if (disabled.startsWith("1"))
					Dbutil.executeQuery("delete from wines where shopid=" + id, con);
				succes = true;

			}
			if (succes) {
				Dbutil.executeQuery("update LOW_PRIORITY shopstats set lastchange=sysdate() where shopid=" + i + ";");
				String[] coordinates = Coordinates.getCoordinates(address);
				if (coordinates != null && coordinates.length == 3)
					Dbutil.executeQuery("update LOW_PRIORITY shops set lon=" + coordinates[0] + ", lat="
							+ coordinates[1] + ", accuracy=" + coordinates[2] + " where id=" + i + ";");
				StoreInfo.clearCache(i);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem adding/changing shop info for " + auto + "shop " + id + ":", e);
			succes = false;
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (!succes)
			i = 0;

		return i;
	}

	public static String getDefaultCurrency(String countrycode) {
		return Dbutil.readValueFromDB("SELECT currency from vat where countrycode='" + countrycode + "';", "currency");
	}

	public ArrayList<String> getCurrencies() {
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		ArrayList<String> currencies = new ArrayList<String>();
		try {
			query = "select distinct(currency) as currency from vat;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				currencies.add(rs.getString("currency"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return currencies;
	}

	public static boolean checkLinkOnPage(String url, String targetlink) {
		Webpage webpage = new Webpage();
		webpage.urlstring = url;
		webpage.maxattempts = 1;
		webpage.ignorepagenotfound = true;
		webpage.readPage();
		if (!targetlink.equals("") && Webroutines.getRegexPatternValue(targetlink, webpage.html).equals(""))
			return false;
		if (webpage.responsecode != 200)
			return false;
		return true;
	}

	public static String getWsShopsHTML(String countrycode) {
		StringBuffer html = new StringBuffer();
		html.append("<Table>");
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {
			String query = "SELECT wsshops.*  from wsshops where wsshops.countrycode like '" + countrycode
					+ "' and storetype='Retail' and internet like '%online%' order by numberofwines desc,countrycode; ";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				html.append("<tr id='ws" + rs.getString("wsid") + "'><td>" + rs.getString("name") + "</td>");
				html.append("<td>" + rs.getString("Country") + "</td>");
				html.append("<td>" + rs.getString("numberofwines") + "</td>");
				html.append("<td><a href='" + rs.getString("Url") + "' target='_blank'>" + rs.getString("Url")
						+ "</a></td>");
				html.append(
						"<td><form action='updateWSshopcomment.jsp' target='_blank'><input type='hidden' name='shopid' value='"
								+ rs.getString("wsid") + "'/><input type='text' name='reason' value='"
								+ rs.getString("comment") + "'/></form></td>");
				html.append("</tr>");
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving shop list. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		html.append("</table>");
		return html.toString();
	}

	public static String switchMobileUrl(String originalurl, String querystring) {
		String result = originalurl + querystring;

		return result;

	}

	public static ArrayList<Winerating> getWinesByRating(String ratingstring, String author) {
		ArrayList<Winerating> ratings = new ArrayList<Winerating>();
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		int rating = 101;
		try {
			rating = Integer.valueOf(ratingstring);
		} catch (Exception e) {
		}

		if (rating < 101)
			try {

				String query = "SELECT ratinganalysis.*, knownwines.wine from ratinganalysis join knownwines on (ratinganalysis.knownwineid=knownwines.id) where rating="
						+ rating + " and author like '" + author + "' order by minpriceeuroex;";
				rs = Dbutil.selectQuery(query, con);
				while (rs.next()) {
					ratings.add(new Winerating(rs.getString("wine"), rs.getString("vintage"), rs.getString("author"),
							rs.getDouble("rating"), rs.getDouble("ratinghigh"), rs.getDouble("minpriceeuroex"),
							rs.getDouble("avgpriceeuroex")));
				}

			} catch (Exception e) {
				Dbutil.logger.error("Problem while searching wine by ratings. ", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}

		return ratings;
	}

	public static ArrayList<Winerating> getWinesByRating(String ratingstring, String author, String region) {
		ArrayList<Winerating> ratings = new ArrayList<Winerating>();
		Connection con = Dbutil.openNewConnection();
		int rating = 101;
		ResultSet rs = null;

		try {
			rating = Integer.valueOf(ratingstring);
		} catch (Exception e) {
		}

		if (rating < 101)
			try {
				String regionlist = Region.getRegionsAsIntList(region);
				String query = "SELECT ratinganalysis.*, knownwines.wine from ratinganalysis join knownwines on (ratinganalysis.knownwineid=knownwines.id) join regions on (knownwines.appellation=regions.region) where regions.id in ("
						+ regionlist + ") and rating>=" + rating + " and author like '" + author
						+ "' and author !='FWS' order by minpriceeuroex;";
				rs = Dbutil.selectQuery(query, con);
				while (rs.next()) {
					ratings.add(new Winerating(rs.getString("wine"), rs.getString("vintage"), rs.getString("author"),
							rs.getDouble("rating"), rs.getDouble("ratinghigh"), rs.getDouble("minpriceeuroex"),
							rs.getDouble("avgpriceeuroex")));
				}

			} catch (Exception e) {
				Dbutil.logger.error("Problem while searching wine by ratings. ", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}

		return ratings;
	}

	public static ArrayList<String> getShopList(String auto, boolean onlyactive) {

		ArrayList<String> shops = new ArrayList<String>();
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {
			String query = "SELECT * from " + auto + "shops ";
			if (auto.equals("auto")) {
				query = query + "where status !='Busy' and status not like 'Unsuccesful:%' order by lastsearchstarted";
			} else {
				query = query + (onlyactive ? " where disabled=0 " : "") + " order by shopname;";
			}
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				shops.add(rs.getString("id"));
				shops.add(rs.getString("shopname"));

			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving shop list. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return shops;

	}

	public static ArrayList<String> getShopList(String auto) {
		return getShopList(auto, false);
	}

	public static ArrayList<String> getPayingShopList() {
		int i = 0;
		ArrayList<String> shops = new ArrayList();
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {
			String query = "SELECT * from shops where disabled=0 and  (starttrial >date('2001-01-01') or id in (select distinct(shopid) from banners)) ";
			query = query + "order by shopname;";

			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				shops.add(rs.getString("id"));
				shops.add(rs.getString("shopname"));

			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving shop list. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return shops;
	}

	public static ArrayList<String> getShopListWithDatafeed() {
		int i = 0;
		ArrayList<String> shops = new ArrayList();
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {
			String query = "select shops.* from datafeeds join shops on (datafeeds.shopid=shops.id) order by shopname;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				shops.add(rs.getString("id"));
				shops.add(rs.getString("shopname"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving shop list. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return shops;
	}

	public static ArrayList<String> getUrls(String shopid, String auto) {
		// Retrieves all urls from the scrapelist for autodiscovered shops
		int i = 0;
		ArrayList urls = new ArrayList();
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "scrapelist where shopid=" + shopid
					+ " order by winesfound desc, status;", con);
			while (rs.next()) {
				urls.add(rs.getString("url"));
				urls.add(rs.getString("urlsource"));
				urls.add(rs.getString("urltype"));
				urls.add(rs.getString("winesfound"));
				urls.add(rs.getString("status"));
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving auto urls. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return urls;
	}

	public static ArrayList<String> getAutoConfig(String shopid) {
		int i = 0;
		ArrayList<String> config = new ArrayList<String>();
		Connection con = Dbutil.openNewConnection();
		String rowid;
		String tablescraper = "";
		ResultSet rs = null;

		try {
			rs = Dbutil.selectQuery("SELECT * " + "from autoscrapelist where shopid=" + shopid
					+ " and (urltype='Master' or urltype='Fixed');", con);
			if (rs.next()) {
				rowid = rs.getString("id");
				config.add(rowid); // 0=rowid van scrapelist
				tablescraper = rs.getString("tablescraper");
				config.add(tablescraper); // 1 = rowid van tablescraper
				config.add(rs.getString("url")); // 2 = url
				rs = Dbutil.selectQuery("SELECT * " + "from autotablescraper where id='" + tablescraper + "';", con);
				if (rs.next()) {
					config.add(rs.getString("nameregex")); // 3 = wine regex
					config.add(rs.getString("filter")); // 4 = regex filter

				} else {

					config.add("0");
					config.add("0");
				}
			} else {
				config.add("0");
				config.add("0");
				config.add("0");
				config.add("0");
				config.add("0");
			}
			rs = Dbutil.selectQuery("SELECT * " + "from autospiderregex where shopid='" + shopid + "';", con);
			if (rs.next()) {
				config.add(rs.getString("regex")); // 5= spider regex
			} else {
				config.add("0");
			}
			rs = Dbutil.selectQuery("SELECT * " + "from autoshops where id='" + shopid + "';", con);
			rs.next();
			config.add(rs.getString("shopname")); // 6= spider regex
			config.add(rs.getString("countrycode")); // 7= countrycode
			config.add(rs.getString("exvat")); // 8= exvat
			config.add(rs.getString("wsshopsid")); // 9= id uit wsshops table
			config.add(rs.getString("amountwines")); // 10= # wines from ws
			rs = Dbutil.selectQuery("SELECT count(*) as thecount " + "from autowines where shopid='" + shopid + "';",
					con);
			rs.next();
			config.add(rs.getString("thecount")); // 11= # wines found

		} catch (Exception e) {
			Dbutil.logger.error("Problem while retrieving auto config. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return config;
	}

	public static String shopFromAutoToNormal(String shopid) {
		boolean succes = true;
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;
		String newshopid;
		String tablescraperid;
		String newtablescraperid;
		String Query = "";
		try {
			Query = "INSERT into shops (baseurl,URLType,Shopname,Exvat,CountryCode,shopurl,currency,ignorepagenotfound,email,address,cookie) "
					+ "(Select baseurl,URLType,Shopname,Exvat,CountryCode,shopurl,currency,ignorepagenotfound,email,address,'' from autoshops where id="
					+ shopid + ");";
			int i = Dbutil.executeQuery(Query, con);
			if (i == 0)
				succes = false;
			Query = "select LAST_INSERT_ID() as shopid;";
			rs = Dbutil.selectQuery(Query, con);
			rs.next();
			newshopid = rs.getString("shopid");
			Query = "select tablescraper from autoscrapelist where shopid='" + shopid
					+ "' and (urltype='Master' or urltype='Fixed');";
			rs = Dbutil.selectQuery(Query, con);
			rs.next();
			tablescraperid = rs.getString("tablescraper");
			Dbutil.logger.info(newshopid);
			Query = "INSERT into spiderregex (Shopid, regex,filter)" + " Select '" + newshopid
					+ "' as shopid, regex, filter from autospiderregex where shopid=" + shopid + ";";
			if (succes)
				i = Dbutil.executeQuery(Query, con);
			if (i == 0)
				succes = false;
			Query = "INSERT into tablescraper (Shopid, winesep, fieldsep,filter,nameorder,nameregex,nameexclpattern,vintageorder,vintageregex,priceorder,priceregex,sizeorder,sizeregex,urlregex,assumebottlesize)"
					+ " Select '" + newshopid
					+ "' as shopid, winesep, fieldsep,filter,nameorder,nameregex,nameexclpattern,vintageorder,vintageregex,priceorder,priceregex,sizeorder,sizeregex,urlregex,assumebottlesize from autotablescraper where shopid="
					+ shopid + " and id=" + tablescraperid + ";";
			if (succes)
				i = Dbutil.executeQuery(Query, con);
			if (i == 0)
				succes = false;
			Query = "select LAST_INSERT_ID() as tablescraper;";
			rs = Dbutil.selectQuery(Query, con);
			rs.next();
			newtablescraperid = rs.getString("tablescraper");
			Query = "Select url, regex,  Scrapeorder,  Status,  URLType, Headerregex, '" + newtablescraperid
					+ "' as tablescraper,  Winesfound,  Postdata,  Parenturl, '" + newshopid
					+ "' as shopid,0 from autoscrapelist where shopid=" + shopid + " and urltype='Master';";
			rs = Dbutil.selectQuery(Query, con);
			rs.next();
			int hash = (rs.getString("Url") + rs.getString("postdata")).hashCode();
			Query = "INSERT into scrapelist (url, regex,  Scrapeorder,  Status,  URLType, Headerregex,Tablescraper,  Winesfound,  Postdata,  Parenturl, shopid,hashcode)"
					+ " Select url, regex,  Scrapeorder,  Status,  URLType, Headerregex, '" + newtablescraperid
					+ "' as tablescraper,  Winesfound,  Postdata,  Parenturl, '" + newshopid + "' as shopid," + hash
					+ " from autoscrapelist where shopid=" + shopid + " and urltype='Master';";
			Dbutil.logger.info(Query);
			if (succes)
				i = Dbutil.executeQuery(Query);
			Query = "DELETE from autowebpage where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			if (succes)
				i = Dbutil.executeQuery(Query);
			Query = "DELETE from autoscrapelist where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			if (succes)
				i = Dbutil.executeQuery(Query);
			if (i == 0)
				succes = false;
			Query = "DELETE from autoshops where id='" + shopid + "';";
			Dbutil.logger.info(Query);
			if (succes)
				i = Dbutil.executeQuery(Query);
			if (i == 0)
				succes = false;
			Query = "DELETE from autotablescraper where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			if (succes)
				i = Dbutil.executeQuery(Query);
			if (i == 0)
				succes = false;
			Query = "DELETE from autospiderregex where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			if (succes)
				i = Dbutil.executeQuery(Query);
			if (i == 0)
				succes = false;

		} catch (Exception exc) {
			Dbutil.logger.error("Problem converting shop from auto to normal. " + Query, exc);
			succes = false;
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return "" + succes;
	}

	public static String deleteAutoShop(String shopid, String reason) {
		String succes = "true";
		if (reason == null)
			reason = "";
		reason = Spider.SQLEscape(reason);
		String Query = "";
		try {
			Query = "DELETE from autoscrapelist where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			int i = Dbutil.executeQuery(Query);
			Query = "Update autoshops set status='Unsuccesful: " + reason + "' where id='" + shopid + "';";
			Dbutil.logger.info(Query);
			i = Dbutil.executeQuery(Query);
			Query = "DELETE from autotablescraper where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			i = Dbutil.executeQuery(Query);
			Query = "DELETE from autowines where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			i = Dbutil.executeQuery(Query);
			Query = "DELETE from autowebpage where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			i = Dbutil.executeQuery(Query);
			Query = "DELETE from autospiderregex where shopid='" + shopid + "';";
			Dbutil.logger.info(Query);
			i = Dbutil.executeQuery(Query);

		} catch (Exception exc) {
			succes = "false";
			Dbutil.logger.error("Problem deleting autodiscovershop. " + Query, exc);

		}

		return succes;
	}

	public static String deleteAutoTableScraper(String shopid) {
		String succes = "true";
		String Query = "";
		try {
			Query = "DELETE from autoscrapelist where shopid='" + shopid + "';";
			Dbutil.executeQuery(Query);
			Query = "DELETE from autotablescraper where shopid='" + shopid + "';";
			Dbutil.executeQuery(Query);
		} catch (Exception exc) {
			succes = "false";
			Dbutil.logger.error("Problem deleting autodiscovershop. " + Query, exc);
		}
		return succes;
	}

	public static String getScrapeListHTML(String Shopid, String link) { // retrieves the scrapelist for a shop
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String output = "<TABLE>";
		String regex = "";
		try {
			rs = Dbutil.selectQuery(
					"SELECT * " + "from scrapelist where Shopid=" + Shopid + " and urltype not like '%Spidered%';",
					con);
			if (!rs.isAfterLast()) {
				while (rs.next()) {
					regex = rs.getString("regex");
					regex = regex.replaceAll("<", "&lt;");
					regex = regex.replaceAll(">", "&gt;");
					output = output + "<TR>";
					output = output + "<TD><a href=\"" + link + "?getrow=" + rs.getString("id") + "&shopid=" + Shopid
							+ "\">Edit </a></TD>";
					output = output + "<TD>" + rs.getString("url") + "</TD>";
					output = output + "<TD>" + regex + "</TD>";
					output = output + "<TD>" + rs.getString("scrapeorder") + "</TD>";

				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		output = output + "</TABLE>";
		return output;

	}

	public static String getTableScrapeListHTML(String Shopid, String link, String auto) { // retrieves the scrapelist
																							// for a shop
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String output = "<TABLE>";
		String tablescraper = "";
		try {
			rs = Dbutil.selectQuery("SELECT *,group_concat(url SEPARATOR '@@@') as urls " + "from " + auto
					+ "scrapelist where Shopid=" + Shopid + " and urltype not like '%Spidered%' group by tablescraper;",
					con);
			if (!rs.isAfterLast()) {
				while (rs.next()) {
					tablescraper = rs.getString("tablescraper");
					output = output + "<TR>";
					output = output + "<TD><a href=\"" + link + "?getrow=" + rs.getString("id") + "&shopid=" + auto
							+ Shopid + "\">Edit </a></TD>";
					output = output + "<TD>" + rs.getString("urls") + "</TD>";
					output = output + "<TD>" + tablescraper + "</TD>";
					output = output + "<TD><a href=\"" + link + "?actie=delete&getrow=" + rs.getString("id")
							+ "&shopid=" + auto + Shopid + "\"> Delete </a></TD>";
					output = output + "</TR>";

				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		output = output + "</TABLE>";
		output += "<br/>The number behind the url identifies the unique tablescraper. If 2 or more url's with the same number are shown, editing 1 changes the others as well.";
		return output;

	}

	public static String getFeedScrapeListHTML(String Shopid, String link, String auto) { // retrieves the scrapelist
																							// for a shop
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String output = "<TABLE>";
		String feedscraper = "";
		try {
			rs = Dbutil.selectQuery(
					"SELECT * " + "from " + auto + "scrapelist where Shopid=" + Shopid + " and feedscraper>0;", con);
			if (!rs.isAfterLast()) {
				while (rs.next()) {
					feedscraper = rs.getString("feedscraper");
					output = output + "<TR>";
					output = output + "<TD><a href=\"" + link + "?getrow=" + rs.getString("id") + "&shopid=" + auto
							+ Shopid + "\">Edit </a></TD>";
					output = output + "<TD>" + rs.getString("url") + "</TD>";
					output = output + "<TD>" + feedscraper + "</TD>";
					output = output + "<TD>" + rs.getString("scrapeorder") + "</TD>";

				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		output = output + "</TABLE>";
		return output;

	}

	public static String youMayAlsoLikeHTML(int knownwineid, Searchdata searchdata) {
		String html = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {

			WineAdvice advice = new WineAdvice();
			advice.setResultsperpage(15);
			if (!searchdata.getCountry().equals("")) {
				advice.setCountryofseller(searchdata.getCountry());
			} else {
				advice.setCountryofseller("All");
			}
			advice.setCurrency(searchdata.getCurrency());
			advice.setRegion(
					Dbutil.readValueFromDB("Select * from knownwines where id=" + knownwineid + ";", "appellation"));
			rs = Dbutil.selectQueryFromMemory(
					"select min(priceeuroex) as priceeuroex from materializedadvice where knownwineid=" + knownwineid
							+ ";",
					"materializedadvice", con);
			float price = (float) 0;
			if (rs.next())
				price = rs.getFloat("priceeuroex");
			if (price > 0)
				advice.setPricemax(price * 2);
			// if (price>0) advice.setPricemin(price/2);
			advice.setSearchtype(Searchtypes.Q);
			advice.setSubregions(true);
			advice.getAdvice();
			if (advice != null && advice.wine != null) {
				html = "<div id='suggestions' class='youmayalsolike' onmouseover='show(\"suggestions\");' onmouseout='hide(\"suggestions\");'><table class='suggestions'>";
				for (int i = 0; i < advice.wine.length; i++) {
					if (advice.wine[i].Knownwineid != knownwineid
							|| !advice.wine[i].Vintage.equals(searchdata.vintage)) {
						html += "<tr>";
						html += "<td>" + Knownwines.getImageTag(advice.wine[i].Knownwineid) + "</td>";
						html += "<td><a href='/index2.jsp?name="
								+ Webroutines.URLEncode(Knownwines.getKnownWineName(advice.wine[i].Knownwineid) + " "
										+ advice.wine[i].Vintage)
								+ "&amp;suggestion=true'>" + Knownwines.getKnownWineName(advice.wine[i].Knownwineid)
								+ " " + advice.wine[i].Vintage + "</a></td>";
						html += "<td>" + advice.rating[i] + "&nbsp;pts.</td>";
						html += "<td>" + Webroutines.formatPrice(advice.wine[i].PriceEuroIn, advice.wine[i].PriceEuroEx,
								advice.getCurrency(), "EX") + "</td>";
						html += "</tr>";
					}
				}
				html += "</table></div>";
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html = html.replaceAll(".*<table class='suggestions'></table>.*", ""); // if no results found

		return html;
	}

	public static String getSpiderRegexListHTML(String Shopid, String auto, String link) { // retrieves the scrapelist
																							// for a shop
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String output = "<TABLE>";
		String tablescraper = "";
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "spiderregex where Shopid=" + Shopid + ";", con);
			if (!rs.isAfterLast()) {
				while (rs.next()) {
					output = output + "<TR>";
					output = output + "<TD><a href=\"" + link + "?getrow=" + rs.getString("id") + "&shopid=" + auto
							+ Shopid + "\">Edit    </a></TD>";
					output = output + "<TD>" + rs.getString("regex").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
							+ "</TD>";
					output = output + "<TD>" + rs.getString("filter") + "</TD>";
					output = output + "<TD><a href=\"" + link + "?rowid=" + rs.getString("id") + "&shopid=" + auto
							+ Shopid + "&actie=delete\">Delete    </a></TD>";
					output = output + "</TR>";
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		output = output + "</TABLE>";
		return output;

	}

	public static String getStatusHTML() { // retrieves the visitor overview
		String html = "";
		html = html + "<H2>Version: " + Wijnzoeker.version + "</H2>";
		html = html + getVisitorOverview(14);

		return html;

	}

	public static String retrieveRow(String Shopid, String auto) { // retrieves the scrapelist for a shop
		ResultSet rs = null;
		String row = "";
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "scrapelist where Shopid=" + Shopid
					+ " and urltype not like '%Spidered%' group by tablescraper;", con);
			if (rs.next()) {
				row = rs.getString("id");
			}
			if (rs.next()) { // Only return a row number if there is one row
				row = "";
			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return row;

	}

	public static void deleteRow(String shopid, String auto, String getrow) { // deletes the scrapelist for a shop
		Dbutil.executeQuery("delete from " + auto + "scrapelist where shopid=" + shopid + " and id=" + getrow);
	}

	public static String retrieveSpiderRow(String Shopid, String auto) { // retrieves the scrapelist for a shop
		return retrieveSpiderRow(Shopid, auto, "normal");
	}

	public static String retrieveSpiderRow(String Shopid, String auto, String type) { // retrieves the scrapelist for a
																						// shop
		ResultSet rs = null;
		String row = "";
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "spiderregex where Shopid=" + Shopid
					+ " and spidertype like '" + type + "';", con);
			if (rs.next()) {
				row = rs.getString("id");
			}
			if (rs.next()) { // Only return a row number if there is one row
				row = "";
			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return row;

	}

	public static ArrayList<String> getShopInfo(String Shopid, String auto) { // retrieves the scrapelist for a shop
		ResultSet rs = null;
		ArrayList<String> shopinfo = new ArrayList<String>();
		String row = "";
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "shops where id=" + Shopid + ";", con);
			if (rs.next()) {
				shopinfo.add(rs.getString("shopname"));
				shopinfo.add(rs.getString("shopurl"));
				shopinfo.add(rs.getString("baseurl"));
				shopinfo.add(rs.getString("urltype"));
				shopinfo.add(rs.getString("exvat"));
				String country = rs.getString("countrycode");
				shopinfo.add(rs.getString("countrycode"));
				String email = rs.getString("email");
				String address = rs.getString("address");
				String disabled = rs.getString("disabled");
				if (email.equals("null"))
					email = "";
				String currency = rs.getString("currency");
				if (currency.equals("")) {
					Dbutil.closeRs(rs);
					rs = Dbutil.selectQuery("SELECT currency " + "from vat where countrycode='" + country + "';", con);
					if (rs.next()) {
						currency = rs.getString("currency");
					} else {
						currency = "";
					}
				}
				shopinfo.add(currency);
				shopinfo.add(email);
				shopinfo.add(address);
				shopinfo.add(disabled);
				Dbutil.closeRs(rs);
				rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "shops where id=" + Shopid + ";", con);
				if (rs.next()) {
					shopinfo.add(rs.getString("starttrial"));
					shopinfo.add(rs.getString("startpaying"));
					shopinfo.add(rs.getString("staffelname"));
					shopinfo.add(rs.getString("vatnumber"));
					shopinfo.add(rs.getString("invoiceaddress"));
					shopinfo.add(rs.getString("invoiceemail"));
					shopinfo.add(rs.getString("contactname"));
					shopinfo.add(rs.getString("commercialcomment"));
					shopinfo.add(rs.getString("sponsoringshop"));
					shopinfo.add(rs.getString("linkback"));

				}

			} else {
				Dbutil.logger.error("Shop not found: SELECT * " + "from " + auto + "shops where id=" + Shopid + ";");
			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return shopinfo;

	}

	public static void checkLinksBack() {
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		boolean sendmail = false;
		String missinglinks = "The following links back to Vinopedia were not found:<br/><br/>";
		try {
			query = "select * from shops where linkback!=''";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				int result = checkLinkBack(rs.getInt("id"));
				if (result != 1) {
					missinglinks += rs.getInt("id") + " " + rs.getString("shopname") + ": last seen on "
							+ rs.getDate("linkbackok") + " on url <a href='" + rs.getString("linkback") + "'>"
							+ rs.getString("linkback") + "</a><br/>";
					if (rs.getDate("linkbackok") == null
							|| (new java.util.Date().getTime() - rs.getDate("linkbackok").getTime()) / 1000 / 60
									/ 60 < 36) {
						sendmail = true;
					}
					if (rs.getDate("linkbackok") == null) {
						Calendar cal = Calendar.getInstance();
						cal.set(1970, 0, 1);
						Dbutil.executeQuery("update shops set linkbackok='"
								+ new java.sql.Timestamp(cal.getTime().getTime()) + "' where id=" + rs.getInt("id"));
					}
				} else {
					Dbutil.executeQuery("update shops set linkbackok='"
							+ new java.sql.Timestamp(new java.util.Date().getTime()) + "' where id=" + rs.getInt("id"));
				}
			}
			Dbutil.closeRs(rs);
			if (sendmail) {
				new Emailer().sendEmail("site@vinopedia.com", "management@vinopedia.com", "Link back not found",
						missinglinks);

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public static int checkLinkBack(int shopid) {
		String linkbackurl = Dbutil.readValueFromDB("select linkback from shops where id=" + shopid, "linkback");
		return checkLinkBack(linkbackurl);
	}

	public static int checkLinkBack(String linkbackurl) {
		if (linkbackurl.length() < 5)
			return 10; // link not in database
		if (!linkbackurl.startsWith("http://") && !linkbackurl.startsWith("https://"))
			return 11; // invalid link
		Webpage webpage = new Webpage();
		webpage.urlstring = linkbackurl;
		webpage.maxattempts = 1;
		webpage.readPage();
		String page = webpage.html.replaceAll("\r", " ").replaceAll("\n", " ");
		if (webpage.responsecode != 200)
			return webpage.responsecode;
		String linkback = getRegexPatternValue("(<a[^>]+?href=['\"](https://www\\.vinopedia\\.com).*?</a>)", page,
				Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
		if (linkback.equals(""))
			return 0; // Link not found
		if (linkback.contains("no-follow"))
			return 2; // no-follow
		return 1; // full link
	}

	public static boolean sameDomain(String url1, String url2) {
		if (getDomain(url1).equals(getDomain(url2)))
			return true;
		return false;
	}

	public static String getDomain(String url) {
		if (url == null)
			return "";
		if (url.startsWith("http://"))
			url = url.substring(7);
		if (url.startsWith("https://"))
			url = url.substring(8);
		if (url.contains("/"))
			url = url.substring(0, url.indexOf("/"));
		return url;
	}

	public static ArrayList<String> getScrapeRow(String rowid) { // retrieves the scrapelist for a shop
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		ArrayList<String> result = new ArrayList<String>();
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from scrapelist where id=" + rowid + ";", con);
			if (!rs.isAfterLast()) {
				rs.next();
				result.add(rs.getString("Url"));
				result.add(rs.getString("Regex"));
				result.add(rs.getString("Headerregex"));
				result.add(rs.getString("Scrapeorder"));
				String shopid = rs.getString("Shopid");
				rs = Dbutil.selectQuery(
						"SELECT * " + "from scrapelist where shopid=" + shopid + " and URLtype='Master';", con);
				if (rs.isBeforeFirst()) {
					rs.next();
					result.add(rs.getString("Url"));// Master url
				} else {
					result.add("");
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scraperow. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return result;

	}

	public static ArrayList<String> getSpiderRegexRow(String rowid, String auto) { // retrieves the spider regex for a
																					// shop
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		ArrayList<String> result = new ArrayList<String>();
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "spiderregex where id=" + rowid + ";", con);
			if (!rs.isAfterLast()) {
				rs.next();
				result.add(rs.getString("Shopid"));
				result.add(rs.getString("Regex"));
				result.add(rs.getString("Filter"));
				String shopid = rs.getString("Shopid");
				String onelevel = "" + rs.getBoolean("onelevel");
				rs = Dbutil.selectQuery(
						"SELECT * " + "from " + auto + "scrapelist where shopid=" + shopid + " and URLtype='Master';",
						con);
				if (rs.next()) {
					result.add(rs.getString("Url"));// Master url
					result.add(rs.getString("postdata"));// Post data

				} else {
					result.add("");
					result.add("");
				}
				rs = Dbutil.selectQuery("SELECT * from " + auto + "shops where id=" + shopid + ";", con);
				if (rs.next()) {
					result.add(rs.getString("ignorepagenotfound"));
					result.add(rs.getString("cookie"));
				} else {
					Dbutil.logger.error("Shop not found in " + auto + "shops: " + shopid);
				}
				result.add(onelevel);

			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scraperow. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return result;

	}

	public static ArrayList<String> getTableScrapeRow(String rowid, String auto) { // retrieves the tablescrapelist for
																					// a shop
		ResultSet rs = null;
		String tablescraperid = "";
		String type = "";
		String url = "";
		Connection con = Dbutil.openNewConnection();
		ArrayList<String> result = new ArrayList<String>();
		String shopid = "";
		try {
			String query = "SELECT *,url as urls from " + auto + "scrapelist where id=" + rowid + ";";
			int ts = Dbutil.readIntValueFromDB(query, "tablescraper");
			shopid = Dbutil.readValueFromDB(query, "shopid");
			if (ts > 0)
				query = "SELECT *,group_concat(url SEPARATOR '@@@') as urls from scrapelist where shopid=" + shopid
						+ " and tablescraper=" + ts + " and urltype not like '%Spidered%' group by tablescraper;";
			rs = Dbutil.selectQuery(query, con);
			if (!rs.isAfterLast()) {
				rs.next();

				result.add(rs.getString("headerregex"));
				result.add(rs.getString("tablescraper"));
				result.add(rs.getString("postdata"));
				tablescraperid = rs.getString("tablescraper");
				type = rs.getString("URLType");
				shopid = rs.getString("Shopid");
				url = rs.getString("urls");

			}
			rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "tablescraper where id=" + tablescraperid + ";",
					con);
			if (rs.next()) {
				result.add(rs.getString("winesep"));
				result.add(rs.getString("fieldsep"));
				result.add(rs.getString("filter"));
				result.add(rs.getString("nameorder"));
				result.add(rs.getString("nameregex"));
				result.add(rs.getString("nameexclpattern"));
				result.add(rs.getString("vintageorder"));
				result.add(rs.getString("vintageregex"));
				result.add(rs.getString("priceorder"));
				result.add(rs.getString("priceregex"));
				result.add(rs.getString("sizeorder"));
				result.add(rs.getString("sizeregex"));
				result.add(rs.getString("urlregex"));
				result.add(rs.getBoolean("assumebottlesize") + "");

				if (type.equals("Fixed")) {
					result.add("");
					result.add(url);
				} else if (type.equals("Email")) {
					result.add("Email");
					result.add("Email");
				} else {
					result.add(url);// Master url
					rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "scrapelist where Shopid=" + shopid
							+ " and urltype like '%Spidered%' order by winesfound desc limit 1;", con);
					if (rs.isBeforeFirst()) {
						rs.next();
						result.add(rs.getString("Url"));

					} else {
						String wineurl = url;
						if (wineurl.contains("@@@"))
							wineurl = wineurl.substring(0, wineurl.indexOf("@@@"));
						result.add(wineurl);
					}
				}
				rs = Dbutil.selectQuery("SELECT * " + "from checkshop where Shopid=" + shopid + ";", con);
				if (rs.isBeforeFirst()) {
					rs.next();
					result.add(rs.getString("Url"));
					result.add(rs.getString("regex"));
					result.add(rs.getString("postdata"));
					result.add(rs.getString("multiplier"));
				} else {
					result.add("");
					result.add("");
					result.add("");
					result.add("");
				}
			} else {
				Dbutil.logger.error("Error: could not retrieve tablescraper with id=" + tablescraperid + ";");
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving " + auto + "scraperow " + rowid, e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return result;

	}

	public static ArrayList<String> ScrapeUrl(String datablok, String Regex, String Filter, Variables var,
			String postdata) {
		int i;
		String Urlfound;
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> ScrapedUrls = new ArrayList<String>(0);
		try {

			pattern = Pattern.compile(Regex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL + Pattern.MULTILINE);
			matcher = pattern.matcher(datablok);
			while (matcher.find()) {
				Urlfound = matcher.group(1);
				// JH 01-07-2011 This was deleted. If you change the URLs in the editspiderregex
				// screen,
				// the urls you find are different from the urls you scrape.
				//
				// Do some standard replacements as we want some useless things like session ID
				// filtered out
				// Urlfound=Urlfound.replaceAll("&amp;","&"); commented out because
				// editspiderregex could not distinguish between & and &amp;
				// Urlfound=Urlfound.replaceAll("PHPSESSID=[0123456789abcdefABCDEF]*","");
				// Urlfound=Urlfound.replaceAll("&&","&");
				// Now loop over the shop specific replacements
				i = 0;
				if (Filter != null) {
					for (i = 0; i < Filter.split(":").length; i = i + 2) {
						String Search = Filter.split(":")[i];
						String Replace = null;
						if (i == Filter.split(":").length - 1) {
							Replace = "";
						} else {
							Replace = Filter.split(":")[i + 1];
						}
						Urlfound = Urlfound.replaceAll(Search, Replace);
					}
				}
				ScrapedUrls.add(Urlfound);

			}

		} catch (java.util.regex.PatternSyntaxException e) {
			throw e;
		} catch (Exception exc) {
			Dbutil.logger.error("Iets ging mis met het lezen van de data:", exc);

		}
		return ScrapedUrls;
	}

	public static String adduser(String username, String password, String email) {
		String message = "";
		String mailmessage = "";
		int hash = 0;
		int i = 0;
		Connection usercon = Dbutil.openNewConnection();

		try {
			ResultSet rs;
			Statement stmt;
			stmt = usercon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = Dbutil.selectQuery("SELECT * " + "from vp_users where username='" + username + "';", usercon);
			if (rs.next() == false) { // Username not found; add it!
				hash = Math.abs(username.concat(email).hashCode());
				String query = "Insert into vp_users (username, password, email,role,valid) values ('"
						+ username + "','" + password + "','" + email + "','user'," + 1 + ");";
				System.out.println("query...." + query);
				i = stmt.executeUpdate(query);
				if (i == 0) {
					message = "Unknown error";
				} else {
					message = "Problem in sending email";
					mailmessage = "<html><body>Hi, <br/><br/>";
					mailmessage = mailmessage + "Someone, probably you, has requested an account (username " + username
							+ ") on Vinopedia.com. Please ";
					mailmessage = mailmessage
							+ "<a href=\"https://www.vinopedia.com/account/createaccount.jsp?username=";
					mailmessage = mailmessage + username + "&amp;activationcode=" + hash
							+ "&amp;action=activate\">click here</a>";
					mailmessage = mailmessage + " to activate your account.<br/>";
					mailmessage = mailmessage
							+ "If you did not subscribe, you can disregard this email, we won't bother you again.<br/>Regards,<br/>Vinopedia.com";
					mailmessage = mailmessage + "</body></html>";
					Emailer emailer = new Emailer();
					if (emailer.sendEmail("do_not_reply@vinopedia.com", email, "Vinopedia account activation",
							mailmessage)) {
						message = "Success";
					}
				}

			} else { // user already exists; update the values
				message = "User already exists";

			}
		} catch (Exception e) {
			message = "Unknown error";
			e.printStackTrace();
		}
		Dbutil.closeConnection(usercon);// end catch
		return message;
	}

	public static String getShopOverview(String order, boolean problems) {
		StringBuffer html = new StringBuffer();
		if (order == null || order.equals(""))
			order = "shopname";
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;
		boolean problem;
		int duration;
		String ts;
		String te;
		String log;
		String logwarning;
		String shoplist = "";
		try {
			if (problems) {
				rs = Dbutil.selectQuery(
						"select group_concat(id) as shops from (select shops.id as id from shops join (select distinct(shopid) as shopid from wines) wines  on (shops.id=wines.shopid) where  (succes>0  and URLtype!='Email') or (succes>21  and URLtype='Email') group by shopid)  asd;",
						con);
				if (rs.next()) {

					shoplist = rs.getString("shops");
				}
			}
			rs = Dbutil.selectQuery("select count(*) as total from wines;", con);
			if (rs.next()) {
				html.append("<H4>Total: " + rs.getString("total") + " wines on-line.</H4>");
			}
			html.append(
					"<table><tr><th><a href='/moderator/overview.jsp?order=theid'>Id</a></th><th><a href='/moderator/overview.jsp?order=shopname'>Shop Name</a></th><th>Edit</th><th>Admin</th><th>Last succes</th><th>Log</th><th><a href='/moderator/overview.jsp?order=total'>Wines Found</a></th><th><a href='/moderator/overview.jsp?order=totalold'>Wines not found</a></th><th><a href='/moderator/overview.jsp?order=estimated'>Estimated</a></th><th><a href='/moderator/overview.jsp?order=duration'>Duration</a></th><th>Comment</th></tr>");
			Dbutil.closeRs(rs);
			// rs=Dbutil.selectQuery("select id as theid,
			// comment,shopname,succes,disabled,sponsoringshop,(Lastsearchended -
			// Lastsearchstarted) AS duration,shopurl,amountwines AS estimated, total,
			// totalold from shops left join (select count(*) as total, totalold, shopid
			// from wines left join (select count(*) as totalold, shopid as shop from wines
			// where lastupdated<date(curdate()) "+("".equals(shoplist)?"":(" and shopid in
			// ("+shoplist+")"))+" group by shop) as old on wines.shopid=old.shop
			// "+("".equals(shoplist)?"":(" where shopid in ("+shoplist+")"))+" group by
			// shopid) as thealias on (shops.id=thealias.shopid)
			// "+("".equals(shoplist)?"":(" where shops.id in ("+shoplist+")"))+" order by
			// disabled,succes desc, "+order+";",con);
			html.append(
					"<tr><td>This page is currently not working due to long run times. Needs to be fixed by Jasper.</td></tr>");
			if (false)
				while (rs.next()) {
					duration = -1;
					try {
						duration = rs.getInt("duration");
					} catch (Exception E) {
					}
					problem = problemWithShop(duration, rs.getInt("total"), rs.getInt("estimated"),
							rs.getInt("disabled"), rs.getInt("totalold"));
					if (problem)
						ts = "<td><font color='red'>";
					else
						ts = "<td>";
					if (rs.getInt("sponsoringshop") > 0)
						ts += "<b>";
					if (problem)
						te = "</font></td>";
					else
						te = "</td>";
					if (rs.getInt("sponsoringshop") > 0)
						te = "</b>" + te;
					if (rs.getInt("disabled") == 1)
						ts = ts + "<s>";
					if (rs.getInt("disabled") == 1)
						te = "</s>" + te;
					html.append("<tr>");
					html.append(ts + rs.getString("theid") + te);
					html.append(ts + "<a href='" + ("/moderator/detectscraper.jsp?shopid=" + rs.getInt("theid"))
							+ "' target='_blank'>" + rs.getString("shopname") + "</a>" + te);
					html.append(ts + "<a href='"
							+ ("/moderator/editspiderregex.jsp?actie=retrieve&shopid=" + rs.getInt("theid"))
							+ "' target='_blank'>Spider</a>" + te);
					html.append(ts + "<a href='/moderator/manage.jsp?shopid=" + rs.getString("theid")
							+ "' target='_blank'>Manage</a>" + te);
					html.append(ts + rs.getString("succes") + " days ago" + te);
					logwarning = "";
					log = Webroutines.getLogDetails(rs.getString("theid"));
					if (log.contains("WARN"))
						logwarning = "<font color='orange'>WARN</font>";
					if (log.contains("ERROR"))
						logwarning = "<font color='red'>ERROR</font>";
					html.append(ts + logwarning + te);
					html.append(ts + (rs.getInt("total") - rs.getInt("totalold")) + te);
					html.append(ts + rs.getInt("totalold") + te);
					html.append(ts + rs.getInt("estimated") + te);
					html.append(ts + duration + " s." + te);
					html.append(ts
							+ "<form action='/admin/setshopcomment.jsp' target='_blank'><input type='hidden' name='shopid' value='"
							+ rs.getInt("theid") + "'/><input type='text' style='width:250px;' name='comment' value='"
							+ rs.getString("comment") + "'/><input type='submit' name='actie' value='update'/></form>"
							+ te);
					html.append("</tr>");
				}
		} catch (Exception exc) {
			Dbutil.logger.error("Exception while getting overview: ", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html.append("</table>");
		return html.toString();
	}

	public static boolean problemWithShop(int duration, int total, int estimated, int disabled, int totalold) {
		boolean problem = false;
		if (disabled == 0) {
			if (duration < 0)
				problem = true;
			if (totalold > 0)
				problem = true;
			if (estimated > 0)
				if (total < estimated * 0.9)
					problem = true;
			if (total == 0)
				problem = true;
		}
		return problem;
	}

	public static String saveSearch(String username, String idstring, String name, String vintage, String country,
			String description, String priceminstring, String pricemaxstring, String rareold, String cheapest) {
		String message = "";
		String query = "";
		float pricemin = 0;
		float pricemax = 0;
		int id = 0;
		int result = 0;
		NumberFormat nf = NumberFormat.getInstance();
		if (priceminstring == null)
			priceminstring = "0";
		if (pricemaxstring == null)
			pricemaxstring = "0";
		if (priceminstring == "")
			priceminstring = "0";
		if (pricemaxstring == "")
			pricemaxstring = "0";
		try {
			id = Integer.parseInt(idstring);
		} catch (Exception exc) {
		}
		try {
			pricemin = nf.parse(priceminstring).floatValue();
			pricemax = nf.parse(pricemaxstring).floatValue();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		if (description == null)
			description = "";
		if (name == null)
			name = "";
		if (vintage == null)
			vintage = "";
		if (cheapest == null)
			cheapest = "";
		if (id == 0) {
			query = "INSERT into search (username, emailaddress, description,name,vintage,country,pricemin,pricemax,rareold,cheapest) VALUES "
					+ "('" + username + "',(select email from users where username='" + username + "' limit 1),'"
					+ Spider.SQLEscape(description) + "','" + Spider.SQLEscape(name) + "','" + Spider.SQLEscape(vintage)
					+ "','" + Spider.SQLEscape(country) + "','" + pricemin + "','" + pricemax + "',"
					+ Spider.SQLEscape(rareold) + ",'" + Spider.SQLEscape(cheapest) + "');";
			result = Dbutil.executeQuery(query);
			if (result > 0) {
				message = "You now have a new search \"" + description + "\"";
			} else {
				message = "Something went wrong while trying to save your search. Please review your search terms and try again.";
			}
		} else {
			query = "UPDATE search set ";
			query = query + "description = '" + Spider.SQLEscape(description) + "', ";
			query = query + "name = '" + Spider.SQLEscape(name) + "', ";
			query = query + "vintage = '" + Spider.SQLEscape(vintage) + "', ";
			query = query + "country = '" + Spider.SQLEscape(country) + "', ";
			query = query + "pricemin = " + pricemin + ", ";
			query = query + "pricemax = " + pricemax + ", ";
			query = query + "rareold = " + Spider.SQLEscape(rareold) + ", ";
			query = query + "cheapest = '" + Spider.SQLEscape(cheapest) + "' ";
			query = query + "where id=" + id + ";";
			result = Dbutil.executeQuery(query);
			if (result > 0) {
				message = "Your search was saved.";
			} else {
				message = "Something went wrong while trying to save your search. Please review your search terms and try again.";
			}
		}
		return message;
	}

	public static String filterUserInput(String input) {
		try {
			if (input != null) {
				input = input.replaceAll("\t", "");
				input = input.replaceAll(
						"[^a-zA-Z0-9()'\"&% \\*/#�����������������������������������������������������:.,@\\-"
								+ new String(new byte[] { (byte) 0xc2, (byte) 0x9c }, "UTF8") + "]",
						" ");
				input = input.replaceAll("\\s+", " ");
				input = input.trim();
				// input=input.replaceAll("'","'");
			}
		} catch (Exception e) {
			input = "";
		}
		return input;
	}

	/*
	 * public static String filterUserHtml(String input){ AntiSamy as=new
	 * AntiSamy(); String output=""; try { CleanResults result = as.scan(input,
	 * Configuration.basedir+System.getProperty("file.separator")+"WEB-INF"+System.
	 * getProperty("file.separator")+"antisamy.xml"); output=result.getCleanHTML();
	 * } catch (ScanException e) { Dbutil.logger.info("Antisami exception",e); }
	 * catch (PolicyException e) { Dbutil.logger.info("Antisami exception",e); }
	 * return output; }
	 */
	static public String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	static public String charToHex(char c) {
		// Returns hex String representation of char c
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}

	public static String getAdwordsData() {
		StringBuffer sb = new StringBuffer();
		String query;
		String language;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			String CPM = "1,00";
			String CPC = "0,03";
			String excludeknownwines = "140807,150005,176461,96445,179708";

			sb.append("Adgroups\n");
			query = "select `Campaign`,`Ad Group`,`Max CPC`,`Max Content CPC`,`Placement max CPC`,`Max CPM`,`Status` from (select 'NL wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', '"
					+ CPC + "' as 'Max CPC','" + CPC + "' as 'Max Content CPC','" + CPC + "' as 'Placement max CPC','"
					+ CPM
					+ "' as 'Max CPM', 'Active' as 'Status', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline,if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, true, true, false));
			query = "select `Campaign`,`Ad Group`,`Max CPC`,`Max Content CPC`,`Placement max CPC`,`Max CPM`,`Status` from (select 'NL CPM'   as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', '"
					+ CPC + "' as 'Max CPC','" + CPC + "' as 'Max Content CPC','" + CPC + "' as 'Placement max CPC','"
					+ CPM
					+ "' as 'Max CPM', 'Active' as 'Status', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline,if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			query = "select `Campaign`,`Ad Group`,`Max CPC`,`Max Content CPC`,`Placement max CPC`,`Max CPM`,`Status` from (select 'DE wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', '"
					+ CPC + "' as 'Max CPC','" + CPC + "' as 'Max Content CPC','" + CPC + "' as 'Placement max CPC','"
					+ CPM
					+ "' as 'Max CPM', 'Active' as 'Status', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>18,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' Preise')) as Headline, if(length(wine)>31,concat('f�r ',replace(wine,'Ch�teau ','')),concat('f�r ',wine)) as 'Description_Line_2' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			query = "select `Campaign`,`Ad Group`,`Max CPC`,`Max Content CPC`,`Placement max CPC`,`Max CPM`,`Status` from (select 'US CPM'   as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', '"
					+ CPC + "' as 'Max CPC','" + CPC + "' as 'Max Content CPC','" + CPC + "' as 'Placement max CPC','"
					+ CPM
					+ "' as 'Max CPM', 'Active' as 'Status', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prices')) as Headline,if(length(wine)>32,concat('of ',replace(wine,'Ch�teau ','')),concat('of ',wine)) as 'Description_Line_2' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			query = "select `Campaign`,`Ad Group`,`Max CPC`,`Max Content CPC`,`Placement max CPC`,`Max CPM`,`Status` from (select 'US wines'   as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', '"
					+ CPC + "' as 'Max CPC','" + CPC + "' as 'Max Content CPC','" + CPC + "' as 'Placement max CPC','"
					+ CPM
					+ "' as 'Max CPM', 'Active' as 'Status', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prices')) as Headline,if(length(wine)>32,concat('of ',replace(wine,'Ch�teau ','')),concat('of ',wine)) as 'Description_Line_2' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			sb.append("\n\n\n");

			sb.append("Ads\n");
			query = "select 'NL wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, if (numberofwines>999,concat('Vinopedia: Vergelijk ',numberofwines,' prijzen'),concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen')) as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2','www.vinopedia.com' as 'Display URL',concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Ad status' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950;";
			sb.append(query2sb(query, true, true, false));
			query = "select 'NL CPM' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, if (numberofwines>999,concat('Vinopedia: Vergelijk ',numberofwines,' prijzen'),concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen')) as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2','www.vinopedia.com' as 'Display URL',concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Ad status' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950;";
			sb.append(query2sb(query, false, true, false));
			query = "select 'DE wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>18,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' Preise')) as Headline,  if (numberofwines>999,concat('Vinopedia.com:Vergleich ',numberofwines,' Preise'),concat('Vinopedia.com: Vergleich ',numberofwines,' Preise')) as 'Description Line 1', if(length(wine)>31,concat('f�r ',replace(wine,'Ch�teau ','')),concat('f�r ',wine)) as 'Description_Line_2','www.vinopedia.com'as 'Display URL',concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Ad status' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950;";
			sb.append(query2sb(query, false, true, false));
			query = "select 'US CPM' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>18,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prices')) as Headline, if (numberofwines>999,concat('Vinopedia.com: Compare ',numberofwines,' prices'),concat('Vinopedia.com: Compare ',numberofwines,' prices')) as 'Description Line 1', if(length(wine)>32,concat('of ',replace(wine,'Ch�teau ','')),concat('of ',wine)) as 'Description_Line_2','www.vinopedia.com' as 'Display URL',concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Ad status' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950;";
			sb.append(query2sb(query, false, true, false));
			query = "select 'US wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>18,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prices')) as Headline, if (numberofwines>999,concat('Vinopedia.com: Compare ',numberofwines,' prices'),concat('Vinopedia.com: Compare ',numberofwines,' prices')) as 'Description Line 1', if(length(wine)>32,concat('of ',replace(wine,'Ch�teau ','')),concat('of ',wine)) as 'Description_Line_2','www.vinopedia.com' as 'Display URL',concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Ad status' from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3 having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950;";
			sb.append(query2sb(query, false, true, false));
			sb.append("\n\n\n");

			sb.append("Keywords\n");
			query = "select `Campaign`,`Ad Group`,`Keyword`,`Keyword Type`,`Max CPC`,`Destination URL`,`Keyword Status` from (select 'NL wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(wine,'Ch�teau ',''),'-',' '),'Toscana ',' '),'D\\'',' '),'Le ',' '),'La ',' '),' IGT',' '),'&',' '),'d\\'',' '),'L\\'',' '),'\\'',' '),'(',' '),')',' ') as Keyword, 'Broad' as 'Keyword Type', '"
					+ CPC
					+ "' as 'Max CPC', concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Keyword Status',if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen') as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2',numberofwines from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, true, true, false));
			query = "select `Campaign`,`Ad Group`,`Keyword`,`Keyword Type`,`Max CPC`,`Destination URL`,`Keyword Status` from (select 'NL CPM' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(wine,'Ch�teau ',''),'-',' '),'Toscana ',' '),'D\\'',' '),'Le ',' '),'La ',' '),' IGT',' '),'&',' '),'d\\'',' '),'L\\'',' '),'\\'',' '),'(',' '),')',' ') as Keyword, 'Broad' as 'Keyword Type', '"
					+ CPC
					+ "' as 'Max CPC', concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Keyword Status',if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen') as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2',numberofwines from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			query = "select `Campaign`,`Ad Group`,`Keyword`,`Keyword Type`,`Max CPC`,`Destination URL`,`Keyword Status` from (select 'DE wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(wine,'Ch�teau ',''),'-',' '),'Toscana ',' '),'D\\'',' '),'Le ',' '),'La ',' '),' IGT',' '),'&',' '),'d\\'',' '),'L\\'',' '),'\\'',' '),'(',' '),')',' ') as Keyword, 'Broad' as 'Keyword Type', '"
					+ CPC
					+ "' as 'Max CPC', concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Keyword Status',if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>18,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' Preise')) as Headline,  concat('Vinopedia.com:vergleich ',numberofwines,' Preise')  as 'Description Line 1', if(length(wine)>31,concat('f�r ',replace(wine,'Ch�teau ','')),concat('f�r ',wine)) as 'Description_Line_2',numberofwines from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			query = "select `Campaign`,`Ad Group`,`Keyword`,`Keyword Type`,`Max CPC`,`Destination URL`,`Keyword Status` from (select 'US CPM' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(wine,'Ch�teau ',''),'-',' '),'Toscana ',' '),'D\\'',' '),'Le ',' '),'La ',' '),' IGT',' '),'&',' '),'d\\'',' '),'L\\'',' '),'\\'',' '),'(',' '),')',' ') as Keyword, 'Broad' as 'Keyword Type', '"
					+ CPC
					+ "' as 'Max CPC', concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Keyword Status',if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen') as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2',numberofwines from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			query = "select `Campaign`,`Ad Group`,`Keyword`,`Keyword Type`,`Max CPC`,`Destination URL`,`Keyword Status` from (select 'US wines' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(wine,'Ch�teau ',''),'-',' '),'Toscana ',' '),'D\\'',' '),'Le ',' '),'La ',' '),' IGT',' '),'&',' '),'d\\'',' '),'L\\'',' '),'\\'',' '),'(',' '),')',' ') as Keyword, 'Broad' as 'Keyword Type', '"
					+ CPC
					+ "' as 'Max CPC', concat('https://www.vinopedia.com/wine/',replace(wine,' ','+'),'?src=adwords') as 'Destination URL','Active' as 'Keyword Status',if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen') as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2',numberofwines from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, false, true, false));
			sb.append("\n\n\n");

			sb.append("Placements\n");
			query = "select `Campaign`,`Ad Group`,`Website` from (select 'US CPM' as 'Campaign', left(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),50)  as 'Ad Group', 'http://www.cellartracker.com/' as Website, 'Broad' as 'Keyword Type', '"
					+ CPC
					+ "' as 'Max CPC', concat('https://www.vinopedia.com/wine/',wine) as 'Destination URL','Active' as 'Keyword Status',if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prijzen')) as Headline, concat('Vinopedia.com:vergelijk ',numberofwines,' prijzen') as 'Description Line 1', if(length(wine)>31,concat('van ',replace(wine,'Ch�teau ','')),concat('van ',wine)) as 'Description_Line_2',numberofwines from (select knownwineid, count(*) as thecount from logging where date(date)>'2009-06-01' and knownwineid>0 and knownwineid not in ("
					+ excludeknownwines
					+ ") group by knownwineid order by thecount desc,knownwineid limit 5000) log join knownwines on (log.knownwineid=knownwines.id) where numberofwines>3  having (length(Headline)<26 and length(Description_line_2)<36) order by numberofwines desc,headline limit 1950) sel;";
			sb.append(query2sb(query, true, true, true));

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static String getShopAdwordsData(int shopid, String language) {
		StringBuffer sb = new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String shopname = Webroutines.getShopNameFromShopId(shopid, "");
		try {
			String CPM = "1,00";
			String CPC = "0,03";
			String excludeknownwines = "140807,150005,176461,96445,179708";
			String headline = "";
			String description1 = "";
			String description2 = "";
			String destinationurl = "concat('https://www.vinopedia.com/external.jsp?wineid=',wineview.id) as 'Destination URL'";
			String campaign = "'" + shopid + " " + shopname + " " + language + "' as 'Campaign'";
			String adgroup = "left(concat(concat(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),' ',appellation),if(wineview.vintage>0,concat(' ',wineview.vintage),'')),50)  as 'Ad Group'";
			String from = "from (select ma.knownwineid,ma.vintage, min(priceeuroex) as minpriceeuroex from materializedadvice ma where country='NL' group by knownwineid,vintage) min join wineview on (wineview.knownwineid=min.knownwineid and wineview.vintage=min.vintage and wineview.priceeuroex<1.01*min.minpriceeuroex and wineview.shopid=24 and size=0.75)  join knownwines on (wineview.knownwineid=knownwines.id)) asd;";
			String displayurl = "'www.vinopedia.com' as 'Display URL'";
			if (language != null && language.equals("NL")) {
				headline = "concat(if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>20,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),'')),if(wineview.vintage>0,concat(' ',wineview.vintage),'')) as Headline";
				description1 = "concat(shopname,': de goedkoopste') as 'Description Line 1'";
				description2 = "'in NL volgens Vinopedia.com' as 'Description_Line_2'";
			}
			if (language != null && language.equals("EN")) {
				headline = "if(length(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''))>17,replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),concat(replace(replace(replace(wine,'Ch�teau ',''),'Domaine ',''),concat(' ',appellation),''),' prices')) as Headline";
				description1 = "if (numberofwines>999,concat('Vinopedia.com: Compare ',numberofwines,' prices'),concat('Vinopedia.com: Compare ',numberofwines,' prices')) as 'Description Line 1'";
				description2 = "if(length(wine)>32,concat('of ',replace(wine,'Ch�teau ','')),concat('of ',wine)) as 'Description_Line_2'";
			}

			sb.append("Adgroups\n");
			query = "select `Campaign`,`Ad Group`,`Max CPC`,`Max Content CPC`,`Placement max CPC`,`Max CPM`,`Status` from (select "
					+ campaign + ", " + adgroup + ", '" + CPC + "' as 'Max CPC','" + CPC + "' as 'Max Content CPC','"
					+ CPC + "' as 'Placement max CPC','" + CPM + "' as 'Max CPM', 'Active' as 'Status', " + headline
					+ "," + description2 + " " + from;
			sb.append(query2sb(query, true, true, false));
			sb.append("\n\n\n");

			sb.append("Ads\n");
			query = "select `Campaign`,`Ad Group`,`Headline`,`Description Line 1`,`Description_Line_2`,`Display URL`,`Destination URL` from (select "
					+ campaign + ", " + adgroup + ", " + headline + ", " + description1 + ", " + description2 + ","
					+ displayurl + "," + destinationurl + ",'Active' as 'Ad status' " + from;
			sb.append(query2sb(query, true, true, false));
			sb.append("\n\n\n");

			sb.append("Keywords\n");
			query = "select `Campaign`,`Ad Group`,`Keyword`,`Keyword Type`,`Max CPC`,`Keyword Status` from (select "
					+ campaign + ", " + adgroup
					+ ", replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(wine,'Ch�teau ',''),'-',' '),'Toscana ',' '),'D\\'',' '),'Le ',' '),'La ',' '),' IGT',' '),'&',' '),'d\\'',' '),'L\\'',' '),'\\'',' '),'(',' '),')',' ') as Keyword, 'Broad' as 'Keyword Type', '"
					+ CPC + "' as 'Max CPC', 'Active' as 'Keyword Status'," + headline + ", " + description2
					+ ",numberofwines " + from;
			sb.append(query2sb(query, true, true, false));
			sb.append("\n\n\n");

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static StringBuffer query2sb(String query, boolean header, boolean removelinkaccents, boolean removehttp) {
		StringBuffer sb = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String field;
		try {
			rs = Dbutil.selectQuery(rs, query, con);
			java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			if (header) {
				for (int i = 1; i < numberOfColumns + 1; i++) {
					sb.append(rsMetaData.getColumnName(i) + "\t");
				}
				sb.append("\n");
			}

			while (rs.next()) {
				for (int i = 1; i < numberOfColumns + 1; i++) {
					field = rs.getString(i);
					if (removelinkaccents && field.startsWith("http"))
						field = removeAccents(field);
					if (removehttp && field.startsWith("http://"))
						field = field.replace("http://", "");
					sb.append(field + "\t");
				}
				sb.append("\n");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb;
	}

	public static String filterUserCommentInput(String input) {
		if (input != null) {
			input = input.replaceAll("<", "&lt;");
			input = input.replaceAll(">", "&gt;");
			input = input.replaceAll("\n", "<br/>");
			input = input.trim();
		}
		return input;
	}

	public static boolean ipBlocked(String ip) {
		String status = "";
		status = Dbutil.readValueFromDB("select * from ipblocks where ipaddress = '" + ip + "';", "status");
		if ("Blocked".equals(status)) {
			return true;
		}
		return false;
	}

	public static String getTastingNotes(int knownwineid, String vintage) {
		String html = "";
		if (knownwineid > 0) {
			String name = Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid + ";",
					"fulltextsearch");
			String item = "item";
			String title = "title";
			String description = "description";
			String link = "link";
			Matcher itemmatcher;
			Pattern itempattern = Pattern.compile("<" + item + ">(.*?)</" + item + ">");
			Matcher titlematcher;
			Pattern titlepattern = Pattern.compile("<" + title + ">(.*?)</" + title + ">");
			Matcher notematcher;
			Pattern notepattern = Pattern.compile("<" + description + ">(.*?)</" + description + ">");
			Matcher linkmatcher;
			Pattern linkpattern = Pattern.compile("<" + link + ">(.*?)</" + link + ">");
			String url = "http://www.scrugy.com/search?q=" + URLEncode(name + " " + vintage)
					+ "&s=tns&format=rss&hpp=50";
			Webpage webpage = new Webpage();
			webpage.urlstring = url;
			webpage.encoding = "UTF-8";
			webpage.readPage();
			String page = webpage.html.replace("\r\n", "").replace("\n", "");
			// page="<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss
			// xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"
			// version=\"2.0\"><channel><title>Scr&#252;gy Search Results: Tasting
			// Note</title><description>Scr&#252;gy search results for latour 2003
			// </description><link>http://www.scrugy.com/search?q=Latour+2003&amp;s=tns&amp;format=rss</link><copyright>Unless
			// otherwise noted, all content is licensed under <a
			// href=\"http://creativecommons.org/licenses/by-nc-sa/2.5/\"
			// rel=\"license\">Creative Commons</a>.</copyright><image>
			// <url>http://www.scrugy.com/images/logo-scrugy2.jpg</url> <title>Scr&#252;gy -
			// The World of Wine Released Daily</title> <link>http://www.scrugy.com</link>
			// <width>200</width>
			// <height>42</height></image><item><title><![CDATA[CellarTracker! - 2003
			// Château Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by 128. Latour vertical.Popcorn
			// caramel on the nose - weird. Great balance on the palate and thankfully dry.
			// Not my favourite Latour. - Tasted 2/12/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/2003-Chateau-Latour/78215</link><pubDate>12
			// Feb 2007 00:00:00</pubDate> <author>128</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=19202&amp;iNote=317351\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 2003 Louis Latour Santenay]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by tanglenet. Served at a Louis
			// Latour tasting. Quick notes: subtle red under ripe fruit; light weight on the
			// palate with a dry finish. Good. Great QPR at $19 - Tasted 11/3/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/2003-Louis-Latour-Santenay/225285</link><pubDate>03
			// Nov 2007 00:00:00</pubDate> <author>tanglenet</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=198653&amp;iNote=509400\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 2003 Les Forts de Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by Tao. Is it a second wine? Yes,
			// once you read the label, deep purple color, what a smell, like having a fruit
			// buffett! Tons tons of fruit! Fragrant...I have to say, at the same time, you
			// can feel the heatwave of 2003! Bravo! I don't want to say more about the wine
			// itself, you better go to get one and find it out, of course, don't forget to
			// share us your own afterthoughts! (95 pts.) - Tasted 8/1/2006. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/2003-Les-Forts-de-Latour/169606</link><pubDate>01
			// Aug 2006 00:00:00</pubDate> <author>Tao</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=20546&amp;iNote=437665\">CellarTracker!</source></item><item><title><![CDATA[Corkd.com
			// - 1961 Latour a Pomerol]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted from Magnum. Tried both this and
			// the 1947 on the same evening. This is just a fantastic wine. Also had the
			// 1961 Cheval Blanc, and the Latour a Pomerol was by far outstepping that
			// fantastic wine. The awesome vision that this wine has is utterly amazing. At
			// 45 years of age it is drinking like a 12 year old California Cab (I realize
			// it is a Merlot, but the tannin and structure of this wine are mind numbing).
			// Full of plums, ink, vanilla, and violets. This is another of the wines of my
			// life so far. From a little known chateau in Pomerol-if you ever find these
			// wines buy them, they will put some at 5 times the price to
			// shame!]]></description><link>http://www.scrugy.com/tasting-notes/1961-Latour-a-Pomerol/35090</link><pubDate>29
			// Oct 2006 16:00:00</pubDate> <author>sofroglo</author> <source
			// url=\"http://www.corkd.com/wine/view/12629\">Corkd.com</source></item><item><title><![CDATA[CellarTracker!
			// - 2003 Louis Latour Aloxe-Corton Domaine Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by nom de plume. not sure if it
			// was better this time around. seems to have lost a bit of complexity and
			// balance (low on acidity etc) since last tasted. But very good (86 pts.) -
			// Tasted 11/26/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/2003-Louis-Latour-AloxeCorton-Domaine-Latour/240242</link><pubDate>26
			// Nov 2007 00:00:00</pubDate> <author>nom de plume</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=192932&amp;iNote=532689\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 1983 Les Forts de Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by DavidKehler. This was served
			// blind at a Chateau Latour verticle in Philadelphia and surpassed Latour
			// bottles from 1975, 1976, 1981, 1983, 1987, and 1994. It was second only to a
			// bottle of 1986 Latour on this occasion. The bottle of 1983 Latour may have
			// been damaged. - Tasted 1/21/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/1983-Les-Forts-de-Latour/70994</link><pubDate>21
			// Jan 2007 00:00:00</pubDate> <author>DavidKehler</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=61809&amp;iNote=302248\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 2004 Les Forts de Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by win. Tasted at Chateau Latour.
			// Deeper than the Pauillac, and fruitier on nose than Chateau Latour. 90-91 (90
			// pts.) - Tasted 3/1/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/2004-Les-Forts-de-Latour/227219</link><pubDate>01
			// Mar 2007 00:00:00</pubDate> <author>win</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=91007&amp;iNote=512788\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 1980 Château Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by noppakit s.. My 2nd time of
			// Latour 1980. This wine is always classic, nice and delicious. It is not
			// balance in the mouth but everytime of drinking Latour...feelin' so right !!!
			// (87 pts.) - Tasted 5/13/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/1980-Chateau-Latour/188380</link><pubDate>13
			// May 2007 00:00:00</pubDate> <author>noppakit s</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=32757&amp;iNote=461031\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 1979 Château Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by naftaflyer. A classic with
			// grace and length. The real LaTour structure started to show only 3 hours
			// after decanting. In this difficult year - Latour still made a classic wine
			// capable of long aging. (96 pts.) - Tasted 2/11/2008. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/1979-Chateau-Latour/293916</link><pubDate>11
			// Feb 2008 00:00:00</pubDate> <author>naftaflyer</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=13820&amp;iNote=616331\">CellarTracker!</source></item><item><title><![CDATA[CellarTracker!
			// - 1986 Château Latour]]></title><category>Tasting
			// Note</category><description><![CDATA[Tasted by DavidKehler. This was the
			// outstanding bottle in a Chateau Latour verticle held in Philadelphia that
			// included bottles from 1975, 1976, 1981, 1983, 1987,and 1994. - Tasted
			// 1/21/2007. [FIND
			// IT!]]]></description><link>http://www.scrugy.com/tasting-notes/1986-Chateau-Latour/70998</link><pubDate>21
			// Jan 2007 00:00:00</pubDate> <author>DavidKehler</author> <source
			// url=\"http://www.cellartracker.com/wine.asp?iWine=5149&amp;iNote=302242\">CellarTracker!</source></item></channel></rss>";
			itemmatcher = itempattern.matcher(page);
			while (itemmatcher.find()) {
				titlematcher = titlepattern.matcher(itemmatcher.group(1));
				if (titlematcher.find()) {
					if (titlematcher.group(1).contains(vintage)) {
						notematcher = notepattern.matcher(itemmatcher.group(1));
						if (notematcher.find()) {
							linkmatcher = linkpattern.matcher(itemmatcher.group(1));
							if (linkmatcher.find()) {
								if (Knownwines.doesWineMatch(titlematcher.group(1), knownwineid)) {
									if ("".equals(html))
										html = "Tasting notes by <a href='http://www.scrugy.com' target='_blank'>Scrugy</a>:<br/>";
									html += "<a href='" + linkmatcher.group(1) + "' target='_blank'>"
											+ titlematcher.group(1) + "</a>: " + notematcher.group(1) + "<br/>";
								}
							}
						}
					}
				}
			}
			html = html.replace("<![CDATA[", "").replace("]]>", "");
		}
		return html;
	}

	public static String getTastingNotesFromXML(int knownwineid, String vintage) {
		String html = "";
		if (knownwineid > 0) {
			String name = Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid + ";", "wine");
			name = name.replace("Ch�teau ", "");
			String url = "http://www.scrugy.com/search?q=" + URLEncodeUTF8(name + " " + getVintageList(vintage))
					+ "&ot=TastingNote&format=xml&hpp=50";
			Webpage webpage = new Webpage();
			webpage.urlstring = url;
			webpage.encoding = "UTF-8";
			webpage.readPage();
			String page = webpage.html.replace("\r\n", "").replace("\n", ""); // replacement because Javascript should
																				// be one line.
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				InputSource inStream = new InputSource();
				inStream.setCharacterStream(new StringReader(page));
				Document doc = db.parse(inStream);
				// get the root element
				Element docEle = doc.getDocumentElement();
				// get a nodelist of elements
				NodeList nl = docEle.getElementsByTagName("Result");
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0; i < nl.getLength(); i++) {
						// get the tn element
						Element el = (Element) nl.item(i);
						// get the TastingNote object
						TastingNote tn = new TastingNote(el);
						// add tn if it matches the wine we are looking for
						if (tn.matches(knownwineid) && tn.vintageMatches(vintage)) {
							if ("".equals(html))
								html = "<div style='font-family:Arial;'>Tasting notes powered by <a href='http://www.scrugy.com' target='_blank'>Scrugy.com</a>:</div><br/>";
							html += tn.toString();
						}

					}
				}
			} catch (Exception e) {
				Dbutil.logger.error("Could not parse XML with URL " + url, e);
			}

		}
		html = html.replace("<![CDATA[", "").replace("]]>", "");
		if ("".equals(html))
			html = "No tasting notes could be found for this wine.";

		return html;
	}

	public static String getVintageList(String vintagesearch) {
		String vintagestring = vintagesearch;
		String[] vintages;
		vintagestring = vintagestring.replaceAll(", ", " ");
		vintagestring = vintagestring.replaceAll(",", " ");
		vintages = vintagestring.split(" ");
		String vintagelist = "";
		if (!vintagesearch.equals("")) {
			for (int i = 0; i < vintages.length; i++) {
				if (!vintages[i].equals("")) {
					if (vintages[i].indexOf("-") > -1) {
						int from = 0;
						int to = 0;
						try {
							from = Integer.parseInt(Webroutines.getRegexPatternValue("(\\d\\d\\d\\d)-", vintages[i]));
							to = Integer.parseInt(Webroutines.getRegexPatternValue("-(\\d\\d\\d\\d)", vintages[i]));
						} catch (Exception e) {

						}
						if (from > 0 && to > 0 && (to - from <= 50)) {
							for (int j = from; j <= to; j++) {
								vintagelist += " " + j;
							}
						}
					} else {
						vintagelist += " " + vintages[i];
					}
				}
			}

		}
		vintagelist = vintagelist.trim();
		return vintagelist;
	}

	public static String getRefineHTML(Translator t, Searchdata searchdata, Wineset wineset, String page) {
		String html = "";
		NumberFormat knownwineformat = new DecimalFormat("000000");
		int i = 0;
		if (wineset.knownwinelist.size() > 1) {
			html = "<table>";
			for (int knownwineid : wineset.knownwinelist.keySet()) {
				i++;
				if (knownwineid > 0) {
					html += ("<tr><td>" + Knownwines.getImageTag(knownwineid) + "</td><td><a href='" + page + "?name="
							+ Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(knownwineid)) + " "
							+ searchdata.getVintage() + "'>"
							+ (knownwineid == 0 ? "Unrecognized wines" : (Knownwines.getKnownWineName(knownwineid)))
							+ "</a> (" + wineset.knownwinelist.get(knownwineid) + " "
							+ (wineset.knownwinelist.get(knownwineid) == 1 ? t.get("wine") : t.get("wines"))
							+ ")</td></tr>");
				}
			}
			html += "</table>";
		}

		return html;
	}

	public static String getVintageFromName(String input) {
		Matcher matcher;
		Pattern pattern;
		String filteredinput = input;
		String vintage = "";
		pattern = Pattern.compile("(?<!\\d)(\\d\\d\\d\\d-\\d\\d\\d\\d)(?!\\d)");
		matcher = pattern.matcher(input);
		while (matcher.find()) {
			vintage = vintage + matcher.group(1) + " ";
			filteredinput = input.replace(matcher.group(1), "");
		}
		input = filteredinput;
		pattern = Pattern.compile("(?<!\\d)(\\d\\d\\d\\d)(?!\\d)");
		matcher = pattern.matcher(input);
		while (matcher.find()) {
			vintage = vintage + matcher.group(1) + " ";
		}
		return vintage;
	}

	public static String filterVintageFromName(String input) {
		String name = input;
		name = name.replaceAll("(?<!\\d)\\d\\d\\d\\d-\\d\\d\\d\\d(?!\\d)", "");
		name = name.replaceAll("(?<!\\d)\\d\\d\\d\\d(?!\\d)", "");
		name = name.replaceAll(",", "");
		name = name.replaceAll("  ", " ");
		while (name.endsWith(" ")) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}

	public static String cleanName(String input) {
		String name = input;
		name = name.replaceAll("(?<!\\d)\\d\\d\\d\\d\\d\\d(?!\\d)", "");
		name = name.trim();
		return name;
	}

	public static String filterVintage(String input) {
		if (input != null) {
			input = input.replaceAll("[^0-9,\\- ]", "");
		}
		return input;
	}

	public static String formatCapitals(String name) {
		String output = "";
		String part = "";
		String character = "";
		for (int i = 0; i < name.length(); i++) {
			character = name.substring(i, i + 1);

			if (Character.isLetter(name.charAt(i))) { // add letter
				part = part + character;
				character = "";
			}

			if (!Character.isLetter(name.charAt(i)) || i == name.length() - 1) { // test for capitals
				if (part.equals(part.toUpperCase()) && !part.equals("")) { // All capitals
					part = part.substring(0, 1) + part.substring(1).toLowerCase();
				}
				output = output + part + character;
				part = "";
			}
		}
		return output;
	}

	public static String getCountryFromCode(String code) {
		if (code != null && code.equals("EU"))
			return "Europe";
		if (code != null && code.equals("UC"))
			return "USA/Canada";
		return Dbutil.readValueFromDB("select * from vat where countrycode='" + code + "';", "country");
	}

	public static ArrayList<String> getCountries() {
		ArrayList<String> countries = new ArrayList<String>();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT * from vat where haswines>0 order by  (country='USA') desc,country;", con);
			if (!rs.isAfterLast()) {
				while (rs.next()) {
					countries.add(rs.getString("CountryCode"));
					countries.add(rs.getString("Country"));
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving countrylist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return countries;
	}

	public static ArrayList<String> getCurrency() {
		ArrayList<String> currency = new ArrayList<String>();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT * from currency order by id;", con);
			if (!rs.isAfterLast()) {
				while (rs.next()) {
					currency.add(rs.getString("Currency"));
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving currency list. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return currency;
	}

	public static String getUrlFromId(String wineidStr, String shopidStr, String ip, String username,
			Searchhistory searchhistory) {
		ResultSet rs = null;
		String url = "";
		String name = "";
		String vintage = "";
		String price = "0.0";
		String affiliateparams = "";
		int knownwineid = 0;
		double CPC = 0.0;
		int wineid = 0;
		int shopid = 0;
		try {
			wineid = Integer.parseInt(wineidStr);
		} catch (Exception e) {
			Dbutil.logger.info("Received a non-number as wineid for a link: " + wineid);
		}
		try {
			shopid = Integer.parseInt(shopidStr);
		} catch (Exception e) {
			Dbutil.logger.info("Received a non-number as shopid for a link: " + shopid);
		}
		Connection con = Dbutil.openNewConnection();
		try {
			if (wineid > 0 && shopid == 0) {
				rs = Dbutil.selectQuery("SELECT * " + "from wineview where id=" + wineid + ";", con);
				if (rs.next()) {
					shopid = rs.getInt("shopid");
					knownwineid = rs.getInt("knownwineid");
					url = rs.getString("SourceURL");
					if (url.equals("")) {
						url = rs.getString("shopurl");
					}
					name = rs.getString("Name");
					affiliateparams = rs.getString("affiliateparams");
					vintage = rs.getString("Vintage");
					price = rs.getString("Price");
					CPC = rs.getDouble("CPC");
					name = Spider.replaceString(name, "'", "\\'");
					Webroutines.logWebAction("Link Clicked", knownwineid, "/link.jsp", ip, "", name, vintage, 0,
							new Float(0.0).floatValue(), new Float(0.0).floatValue(), "", false, shopid + "",
							wineid + "", price, url, CPC, searchhistory);
				} else {
					Dbutil.logger.info("Link not found for wine " + wineid);
				}
			} else if (wineid > 0) {
				rs = Dbutil.selectQuery("SELECT * " + "from wineview where id=" + wineid + ";", con);
				if (rs.next()) {
					name = rs.getString("Name");
					vintage = rs.getString("Vintage");
					url = rs.getString("shopurl");
					affiliateparams = rs.getString("affiliateparams");
					price = rs.getString("Price");
					CPC = rs.getDouble("CPC");
					if (CPC > 0.0) {
						name = Spider.replaceString(name, "'", "\\'");
						Webroutines.logWebAction("Link Clicked", "/link.jsp", ip, "", name, vintage, 0,
								new Float(0.0).floatValue(), new Float(0.0).floatValue(), "", false, shopid + "",
								wineid + "", price, url, CPC, searchhistory);
					}
				}
			} else if (shopid > 0) {
				rs = Dbutil.selectQuery("SELECT * " + "from shops where id=" + shopid + ";", con);
				if (rs.next()) {
					url = rs.getString("shopurl");
					affiliateparams = rs.getString("affiliateparams");
					name = Spider.replaceString(name, "'", "\\'");
					Webroutines.logWebAction("Link Clicked", "/link.jsp", ip, "", name, vintage, 0,
							new Float(0.0).floatValue(), new Float(0.0).floatValue(), "", false, shopid + "",
							wineid + "", price, url, CPC, searchhistory);
				} else {
					Dbutil.logger.info("Link not found for shop " + shopid);
				}
			} else {
				return "";// probably explicit external link
			}
			url = ExternalManager.makeAffiliateUrl(url, affiliateparams);

		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up a url to forward to for wineid " + wineid, E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return url;
	}

	public static String getCountryCodeFromIp(String ip) {
		ResultSet rs = null;
		long ipnumber = 0;
		String countrycode = "";
		Connection con = Dbutil.openNewConnection();
		try {
			for (int i = 0; i <= 3; i++) {
				ipnumber = ipnumber * 256 + Integer.parseInt(ip.split("\\.")[i]);
			}
			rs = Dbutil.selectQuery(
					"Select * from ip2c where ipfrom<=" + ipnumber + " and ipto>=" + ipnumber + " limit 1;", con);
			if (rs.next()) {
				countrycode = rs.getString("iso1");
			}
		} catch (Exception E) {
			Dbutil.logger.debug("Problem while looking up country for ip address " + ip);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return countrycode;
	}

	public static int getShopFromUserId(String userid) {
		return 0; // uitgezet omdat testshop nu vinsetmillisimes is
		/*
		 * int shopid=0; ResultSet rs=null; Connection con=Dbutil.openNewConnection();
		 * try{
		 * rs=Dbutil.selectQuery("Select partnerid from jforum_users where username='"
		 * +userid+"';",con); if (rs.next()){ int partnerid=rs.getInt("partnerid");
		 * rs=Dbutil.selectQuery("Select shopid from partners where id="+partnerid+";",
		 * con); if (rs.next()){ shopid=rs.getInt("shopid"); } }
		 * 
		 * } catch (Exception E){
		 * Dbutil.logger.error("Problem while looking up Shopid for user "+userid,E);
		 * }finally { Dbutil.closeRs(rs); Dbutil.closeConnection(con); } return shopid;
		 */
	}

	public static int getPartnerFromUserId(String userid) {
		int partnerid = 0;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("Select partnerid from jforum_users where username='" + userid + "';", con);
			if (rs.next()) {
				partnerid = rs.getInt("partnerid");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Partnerid for user " + userid, E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return partnerid;
	}

	public static String getShopNameFromShopId(int shopid, String auto) {
		return getShopNameFromShopId(shopid + "", auto);
	}

	public static String getShopNameFromShopId(String shopid, String auto) {
		String shopname = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("Select shopname from " + auto + "shops where id=" + shopid + ";", con);
			if (rs.next()) {
				shopname = rs.getString("shopname");
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Shopname for shopid " + shopid, E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return shopname;
	}

	public static String getPartnerNameFromPartnerId(int partnerid) {
		String partnername = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		if (partnerid > 0) {
			try {
				rs = Dbutil.selectQuery("Select * from partners where id=" + partnerid + ";", con);
				if (rs.next()) {
					partnername = rs.getString("name");
				}

			} catch (Exception e) {
				Dbutil.logger.error("Problem while looking up partnername for partnerid " + partnerid, e);
				Dbutil.closeConnection(con);
			}
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		return partnername;
	}

	public static String formatNewSize(float size) {
		String sizeString = "&nbsp;";
		NumberFormat format = new DecimalFormat("#.###");
		if (size < 100) {
			sizeString = format.format(size);
		} else {
			sizeString = "Case";
		}
		if (size == 0)
			sizeString = "&nbsp;";
		return sizeString;
	}

	public static String formatSize(float size) {
		String sizeString = "&nbsp;&nbsp;&nbsp;";
		NumberFormat format = new DecimalFormat("#.###");
		if (size < 100) {
			sizeString = "&nbsp;&nbsp;&nbsp;" + format.format(size) + "&nbsp;l.&nbsp;&nbsp;";
		} else {
			sizeString = "&nbsp;&nbsp;&nbsp;Case&nbsp;&nbsp;";
		}
		if (size == 0)
			sizeString = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		return sizeString;
	}

	public static String formatSizePrecise(float size) {
		String sizeString = "";
		NumberFormat format = new DecimalFormat("#.###");
		if (size > 100) {
			sizeString = "" + format.format(size % 100) + "&nbsp;l.&nbsp;&nbsp;";
			sizeString = (int) (size / 100) + "x" + sizeString;
		} else {
			sizeString = "" + format.format(size) + "&nbsp;l.&nbsp;&nbsp;";

		}
		if (size == 0)
			sizeString = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		return sizeString;
	}

	public static String formatSizecompact(float size) {
		String sizeString = "";
		NumberFormat format = new DecimalFormat("#.###");
		if (size < 100) {
			sizeString = format.format(size) + "&nbsp;l.";
		} else {
			sizeString = "Case";
		}
		if (size == 0)
			sizeString = "";
		return sizeString;
	}

	public static String getViewedWines(int month, int year, int shopid, String currency) {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String query = "";
		StringBuffer sb = new StringBuffer();
		sb.append("<table><th>Wine</th><th  style='text-align:right'>Approximate price*</th><th>Views</th>");
		int i = 0;
		int total = 0;
		// String notfound="";
		try {
			// query = "select
			// wineid,concat(wines.name,CAST(if(wines.vintage>0,wines.vintage,'') AS CHAR))
			// as wine,wineprice,count(*) as views from logging left join wines on
			// (logging.wineid=wines.id) where logging.shopid="+shopid+" and date between
			// date('"+StoreReport.getStartDate(month,year)+"') and
			// date('"+StoreReport.getEndDate(month,year)+"') and bot=0 and
			// (type='Storepage' or type='Store wineinfo') group by wineid,wines.vintage
			// order by Views desc;";
			String where = " logging.shopid=" + shopid + " and date between date('"
					+ StoreReport.getStartDate(month, year) + "') and date('" + StoreReport.getEndDate(month, year)
					+ "') and bot=0 and (type='Storepage' or type='Store wineinfo') and wineid>0 ";
			query = "select *,count(*) as views from (select distinct * from (select distinct  wines.name as name,wines.vintage as vintage,wineprice,logging.id as id from logging join wines on (wineid=wines.id) where "
					+ where
					+ " union select distinct  history.name as name,history.vintage as vintage,wineprice,logging.id as id from logging join history on (wineid=history.id) where "
					+ where + ") sel) sel2 group by name,vintage order by views desc;";
			rs = Dbutil.selectQuery(rs, query, con);

			while (rs.next()) {
				if (rs.getString("name") != null) {
					i = i + rs.getInt("views");
					sb.append("<tr><td>" + rs.getString("name")
							+ (rs.getInt("vintage") > 0 ? " " + rs.getInt("vintage") : "") + "</td><td>"
							+ formatPrice(rs.getDouble("wineprice"), rs.getDouble("wineprice"), currency, "false")
							+ "</td><td>" + rs.getInt("views") + "</td></tr>");
				}
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as views from logging where " + where + ";";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				total = rs.getInt("views");
			}
			Dbutil.closeRs(rs);

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		sb.append("</table>");
		sb.append("<br/><br/>Prices are an approximation and may vary with exchange rates.<br/>");
		if (total > i)
			sb.append("Details shown for " + i + " of in total " + total + " views.<br/>");
		return sb.toString();
	}

	public static String getClickOverview(int partnerid, int shopid) {
		String html;
		if (shopid == 0) {
			html = "<table class='results' style='width:600px;'><tr><th style='text-align:left;'> Month</th><th style='text-align:right;'>Banners clicked</th></tr>";
		} else {
			html = "<table class='results' style='width:600px;'><tr><th style='text-align:left;'> Month</th><th style='text-align:right;'>Sponsored Wine links clicked</th><th style='text-align:right;'>Banners clicked</th></tr>";
		}
		NumberFormat format = new DecimalFormat("#0.00");
		ResultSet rs = null;
		ResultSet yearmonth = null;
		Connection con = Dbutil.openNewConnection();
		try {
			yearmonth = Dbutil.selectQuery(
					"select (DATE_FORMAT(date, '%Y-%m')) as yearmonth,(DATE_FORMAT(date, '%Y-%m-%d')) as yearmonthday,(DATE_FORMAT(DATE_ADD(date, INTERVAL 1 MONTH), '%Y-%m-%d')) as nextyearmonthday, (DATE_FORMAT(date, '%Y')) as year,(DATE_FORMAT(date, '%m')) as month from (select date from logging where shopid="
							+ shopid + " and cpc>0 union select date from bannersclicked where partnerid=" + partnerid
							+ ") dates group by yearmonth order by yearmonth desc;",
					con);
			int i = 0;
			while (yearmonth.next()) {
				i = (i + 1) % 2;
				String yearmonthvalue = yearmonth.getString("yearmonth");
				String nextyearmonthday = yearmonth.getString("nextyearmonthday");
				String yearmonthday = yearmonth.getString("yearmonthday");
				rs = Dbutil.selectQuery(
						"SELECT  count(*) as hits FROM logging s where shopid=" + shopid + " and date between '"
								+ yearmonthday + "' and '" + nextyearmonthday
								+ "' group by MONTH(date), Year(date) order by Year(date) desc, MONTH(date) desc;",
						con);
				html = html + "<tr " + (i == 0 ? "class='even'" : "class='odd'")
						+ " ><td  style='text-align:left;'><a href='clickdetails.jsp?year="
						+ yearmonth.getString("year") + "&month=" + yearmonth.getString("month") + "'>"
						+ yearmonth.getString("yearmonth") + "</a></td>";
				if (shopid > 0) {

					if (rs.next()) {
						html += "<td style='text-align:right;'>" + rs.getString("hits") + "</td>";
					} else {
						html = html + "<td style='text-align:right;'>0</td>";

					}
				}
				rs = Dbutil.selectQuery(
						"SELECT count(*) as hits FROM bannersclicked s where partnerid=" + partnerid
								+ " and date like '" + yearmonthvalue
								+ "%' group by MONTH(date), Year(date) order by Year(date) desc, MONTH(date) desc;",
						con);
				if (rs.next()) {
					html += "<td  style='text-align:right;'>" + rs.getString("hits") + "</td>";
				} else {
					html = html + "<td style='text-align:right;'>0</td>";
				}
				html += "<tr>";
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Shopname for shopid " + shopid, E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(yearmonth);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";

		return html;

	}

	public static String getClickOverview() { // only for admin
		String html;
		html = "<table class='results' style='width:800px;'><tr><th style='text-align:left;'> Month</th><th style='text-align:left;'>Partner</th><th style='text-align:right;' width='25%'>Sponsored Wine links clicked</th><th style='text-align:right;' width='25%'>Banners clicked</th></tr>";
		NumberFormat format = new DecimalFormat("#0.00");
		ArrayList<String> partnername = new ArrayList<String>();
		ArrayList<Integer> shopid = new ArrayList<Integer>();
		ArrayList<Integer> partnerid = new ArrayList<Integer>();
		ResultSet rs = null;
		ResultSet yearmonth = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("select * from partners;", con);
			while (rs.next()) {
				shopid.add(rs.getInt("shopid"));
				partnerid.add(rs.getInt("id"));
				partnername.add(rs.getString("name"));
			}
			yearmonth = Dbutil.selectQuery(
					"select (DATE_FORMAT(date, '%Y-%m')) as yearmonth,(DATE_FORMAT(date, '%Y')) as year,(DATE_FORMAT(date, '%m')) as month from (select date from logging where cpc>0 and date>'2007-12-01' union select date from bannersclicked where date>'2007-12-01') dates group by yearmonth order by yearmonth desc;",
					con);
			int i = 0;
			while (yearmonth.next()) {
				i = (i + 1) % 2;
				String yearmonthvalue = yearmonth.getString("yearmonth");
				for (int j = 0; j < shopid.size(); j++) {
					rs = Dbutil.selectQuery(
							"SELECT  count(*) as hits FROM logging s where cpc>0 and shopid=" + shopid.get(j)
									+ " and date like '" + yearmonthvalue
									+ "%' group by MONTH(date), Year(date) order by Year(date) desc, MONTH(date) desc;",
							con);
					String row = "<tr " + (i == 0 ? "class='even'" : "class='odd'")
							+ " ><td  style='text-align:left;'><a href='adminclickdetails.jsp?year="
							+ yearmonth.getString("year") + "&month=" + yearmonth.getString("month") + "&shopid="
							+ shopid.get(j) + "&partnerid=" + partnerid.get(j) + "'>" + yearmonth.getString("yearmonth")
							+ "</a></td><td><a href='adminclickdetails.jsp?year=" + yearmonth.getString("year")
							+ "&month=" + yearmonth.getString("month") + "&shopid=" + shopid.get(j) + "&partnerid="
							+ partnerid.get(j) + "'>" + partnername.get(j) + "</a></td>";
					boolean data = false;
					if (shopid.get(j) > 0) {

						if (rs.next()) {
							data = true;
							row += "<td style='text-align:right;'>" + rs.getString("hits") + "</td>";
						} else {
							row += "<td style='text-align:right;'>0</td>";

						}
					}
					rs = Dbutil.selectQuery(
							"SELECT count(*) as hits FROM bannersclicked s where partnerid=" + partnerid.get(j)
									+ " and date like '" + yearmonthvalue
									+ "%' group by MONTH(date), Year(date) order by Year(date) desc, MONTH(date) desc;",
							con);
					if (rs.next()) {
						data = true;
						row += "<td  style='text-align:right;'>" + rs.getString("hits") + "</td>";
					} else {
						row += "<td style='text-align:right;'>0</td>";
					}
					row += "<tr>";
					if (data)
						html += row;
				}
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up ad overview.", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(yearmonth);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";
		return html;

	}

	public static String getDetailedClickOverview(int partnerid, int shopid, int year, int month, String order) {
		String html = "";
		NumberFormat format = new DecimalFormat("0.00");
		NumberFormat monthformat = new DecimalFormat("00");
		ResultSet rs = null;
		ResultSet banners = null;
		Connection con = Dbutil.openNewConnection();

		if (shopid > 0) {
			html += "<h4>Sponsored Wine Clicks for " + new DateFormatSymbols().getMonths()[month - 1 % 12] + " " + year
					+ "</h4><table class='results' ><tr><th width=\"12%\">Date and time</th><th>CPC</th><th width=\"9%\">IP address</th><th>Country</th><th width='30%'>Searched for</th><th align='left' width='30%'>Url clicked</th><th>Wine price</th></tr>";
			if (order == null || order.equals(""))
				order = "date";
			try {
				rs = Dbutil.selectQuery(
						"SELECT *, (DATE_FORMAT(date, '%d-%m-%Y')) as formatteddate FROM logging where shopid=" + shopid
								+ " and date like '%" + year + "-" + monthformat.format(month) + "-%' order by " + order
								+ " desc;",
						con);
				int i = 0;
				while (rs.next()) {
					i = (i + 1) % 2;
					html = html + "<tr " + (i == 0 ? "class='even'" : "class='odd'") + "align='right'>" + "<td>"
							+ rs.getString("date") + "</a></td>" + "<td>&euro; " + format.format(rs.getDouble("cpc"))
							+ "</td>" + "<td>" + rs.getString("ip").replaceAll("\\d+$", "---") + "</td>" + "<td>"
							+ rs.getString("hostcountry") + "</td>" + "<td>" + rs.getString("name") + "</td>"
							+ "<td><a href='" + rs.getString("wineurl") + "' target='_blank'>" + rs.getString("wineurl")
							+ "</a></td>" + "<td>&euro; " + format.format(rs.getDouble("wineprice")) + "</td>"
							+ "</tr>";
				}
			} catch (Exception E) {
				Dbutil.logger.error("Problem while looking up Shopname for shopid " + shopid, E);
			}
			html = html + "</table>";
			html = html + "<a href=/shops>Back to overview</a>";
		}
		html += "<br/><br/><h4>Banner Clicks for " + new DateFormatSymbols().getMonths()[month - 1 % 12] + " " + year
				+ "</h4>";

		if (order == null || order.equals(""))
			order = "date";
		try {
			banners = Dbutil.selectQuery("select * from banners where partnerid=" + partnerid + ";", con);
			while (banners.next()) {
				int bannerid = banners.getInt("id");
				int clicksthismonth = 0;
				rs = Dbutil.selectQuery("SELECT count(*) as count FROM bannersclicked s where partnerid=" + partnerid
						+ " and bannerid=" + bannerid + " and date like '%" + year + "-" + monthformat.format(month)
						+ "-%' group by bannerid order by " + order + " desc;", con);
				if (rs.next()) {
					clicksthismonth = rs.getInt("count");
				}
				rs = Dbutil.selectQuery(
						"SELECT *, (DATE_FORMAT(date, '%d-%m-%Y')) as formatteddate FROM bannersclicked s where partnerid="
								+ partnerid + " and bannerid=" + bannerid + " and date like '%" + year + "-"
								+ monthformat.format(month) + "-%' order by " + order + " desc;",
						con);
				html = html + "<br/>"
						+ Dbutil.readValueFromDB("select html from banners where id=" + bannerid + ";", "html");
				html += "<br/>Number of clicks in " + new DateFormatSymbols().getMonths()[month - 1 % 12] + " " + year
						+ ": " + clicksthismonth;
				html += "<br/>Total number of views: " + banners.getString("views") + ". Total number of clicks: "
						+ banners.getString("clicks") + ".";
				html = html
						+ "<table class='results'><tr><th>Date and time</th><th>CPC</th><th>IP address</th><th>Country of visitor</th><th>Wine name</th><th>Wine region</th></tr>";
				int i = 0;
				while (rs.next()) {
					i = (i + 1) % 2;
					html = html + "<tr " + (i == 0 ? "class='even'" : "class='odd'") + "align='right'>" + "<td>"
							+ rs.getString("date") + "</a></td>" + "<td>&euro; " + format.format(rs.getDouble("income"))
							+ "</td>" + "<td>" + rs.getString("ipaddress").replaceAll("\\d+$", "---") + "</td>" + "<td>"
							+ rs.getString("hostcountry") + "</td>" + "<td>" + rs.getString("region") + "</td>" + "<td>"
							+ rs.getString("wine") + "</td>" + "</tr>";
				}
				html = html + "</table>";

			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Shopname for shopid " + shopid, E);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeRs(banners);
		Dbutil.closeConnection(con);
		return html;

	}

	public static String getWineAdvice(String appellation, String type, String vintage, float pricemin, float pricemax,
			String countryseller) {
		String html = "";
		String query;
		NumberFormat format = new DecimalFormat("0.00");
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet banners;
		Connection con = Dbutil.openNewConnection();
		String regionclause = "";
		String vintagestring = vintage;
		vintagestring = vintagestring.replaceAll(", ", " ");
		vintagestring = vintagestring.replaceAll(",", " ");
		String[] vintages;
		String[] vintagerange;
		String from;
		String to;
		int lft = 0;
		int rgt = 0;
		vintages = vintagestring.split(" ");
		String vintageclause = "";
		try {
			if (!appellation.equals("All")) {
				query = "select lft,rgt from regions where region = '" + appellation + "'";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					lft = rs.getInt("lft");
					rgt = rs.getInt("rgt");
				}
			}
			if (lft > 0 && rgt > 0) {
				regionclause = " AND regionlft>=" + lft + " and regionrgt<=" + rgt + " ";
			}
			if (!vintage.equals("")) {
				for (int i = 0; i < vintages.length; i++) {
					if (!vintages[i].equals("")) {
						if (!vintageclause.equals("")) {
							vintageclause = vintageclause + " OR ";
						}
						if (vintages[i].indexOf("-") > -1) {
							from = "0";
							to = "0";
							vintagerange = vintages[i].split("-");
							if (vintagerange.length < 2) {
								to = "3000";
							} else {
								to = vintagerange[1];
							}
							if ((vintagerange.length < 1) || (vintagerange[0].equals(""))) {
								from = "1";
							} else {
								from = vintagerange[0];
							}
							vintageclause = vintageclause + "wineview.Vintage between " + from + " AND " + to;
						} else {
							vintageclause = vintageclause + "wineview.Vintage=" + vintages[i];
						}
					}
				}
				if (!vintageclause.equals(""))
					vintageclause = " AND (" + vintageclause + ")";
				query = "CREATE TEMPORARY TABLE `wijn`.`tempselect` (`Vintage` char(12) NOT NULL,  `Size` DECIMAL(5,3) NOT NULL,  `PriceEuroex` DECIMAL(10,2) NOT NULL, Knownwineid INTEGER not null, KEY `Allcolumns` (`Vintage`,`Size`,`PriceEuroex`,`Knownwineid`)) ENGINE = MEMORY;";
				Dbutil.executeQuery(query, con);
				query = "insert into tempselect select vintage,size,min(priceeuroex),knownwineid from wineview where knownwineid>0 and size >0 and vintage>0 GROUP BY size, vintage,knownwineid having count(*)>2;";
				Dbutil.executeQuery(query, con);
				query = "Delete from tips;";
				Dbutil.executeQuery(query, con);
				query = "insert ignore into tips select wineview.knownwineid,wineview.vintage,wineview.size,wineview.priceeuroex,'0',regions.id,wineview.id from wineview natural join tempselect join knownwines on (wineview.knownwineid=knownwines.id) join regions on (knownwines.appellation=regions.region) where  createdate >= curdate() order by size,price, priceeuroex";
				Dbutil.executeQuery(query, con);
				Dbutil.executeQuery("Drop table tempselect;", con);
				query = "select * from tips;";
				rs = Dbutil.selectQuery(query, con);
				while (rs.next()) {
					query = "select min(priceeuroex) as minprice from wines where knownwineid="
							+ rs.getString("knownwineid") + " and vintage=" + rs.getString("vintage")
							+ " and priceeuroex>(" + rs.getString("lowestprice") + "+0.01) limit 1;";
					rs2 = Dbutil.selectQuery("select min(priceeuroex) as minprice from wines where knownwineid="
							+ rs.getString("knownwineid") + " and vintage=" + rs.getString("vintage")
							+ " and priceeuroex>(" + rs.getString("lowestprice") + "+0.01) limit 1;", con);
					rs2.next();
					if (rs2.getDouble("minprice") > 0) {
						Dbutil.executeQuery("update tips set nextprice=" + rs2.getDouble("minprice")
								+ " where knownwineid=" + rs.getString("knownwineid") + " and vintage="
								+ rs.getString("vintage") + " and lowestprice=" + rs.getString("lowestprice") + ";");
					}

				}
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while getting Wine Advice", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}

		return html;

	}

	public static String getShoppingList(int shopid, String currency, boolean onlyrecommendations) {
		StringBuffer sb = new StringBuffer();
		sb.append(
				"<table class='shoppinglist' style='border-collapse: collapse;empty-cells:show;overflow:visible;width:100%;border-width:1px;border-style:solid;border-spacing:0px;'>");
		String query;
		String lastcreatedate = Dbutil.readValueFromDB(
				"select max(createdate) as maxdate from wines where shopid=" + shopid + ";", "maxdate");
		int fwsrating = 0;
		NumberFormat format = new DecimalFormat("0.00");
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		try {
			sb.append(
					"<tr><th style='width:*;'>New</th><th style='width:*;'>Wine</th><th style='width:5%'>Size</th><th style='width:6%'>Price</th><th style='width:6%'>Best price</th><th style='width:8%'>Rating</th><th style='width:10%'>Tip</th></tr>");
			query = "select ma.id,wines.name, (wines.createdate='" + lastcreatedate
					+ "') as new, wines.lft, wines.knownwineid, knownwines.appellation, wines.size, ma.vintage, ma.priceeuroex from materializedadvice ma join wines on (wines.id=ma.id) left join knownwines on wines.knownwineid=knownwines.id where ma.shopid="
					+ shopid + " and wines.knownwineid>0 order by appellation,pqratio desc;";
			rs = Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
			while (rs.next()) {
				Wine wine = new Wine(rs.getString("id"));
				String recommendation = "";
				String minprice = "";
				String rating = wine.getRatingList() + "";
				if (rating.equals("0"))
					rating = "";
				if (rs.getInt("knownwineid") > 0 && rs.getInt("vintage") > 0) {
					try {
						query = "select min(priceeuroex) as minprice from wines where knownwineid="
								+ rs.getString("knownwineid") + " and size=" + rs.getString("size") + " and vintage="
								+ rs.getString("vintage") + " group by knownwineid,size,vintage having count(*)>1;";
						rs2 = Dbutil.selectQuery(query, con);
						if (rs2.next()) {
							if (rs2.getDouble("minprice") > 0) {
								minprice = formatPrice(rs2.getDouble("minprice"), (double) 0, currency, "IN");
								if (rs.getDouble("priceeuroex") < 1.1 * rs2.getDouble("minprice"))
									recommendation = "Cheap";
								if (Math.round(100 * rs.getFloat("priceeuroex")) == Math
										.round(100 * rs2.getFloat("minprice")))
									recommendation = "Cheapest";
							}
						}
					} catch (Exception e) {
					}
				}
				fwsrating = 0;
				try {
					if (!"".equals(rating)) {
						fwsrating = Integer
								.parseInt(
										Dbutil.readValueFromDB(
												"select * from ratinganalysis where knownwineid=" + wine.Knownwineid
														+ " and vintage=" + wine.Vintage + " and author='FWS';",
												"rating"));
					}
				} catch (Exception e) {
				}
				if (fwsrating > 0) {
					if (rs.getDouble("priceeuroex") < 0.9 * Webroutines.targetprice(fwsrating)) {
						if (!recommendation.equals(""))
							recommendation += ", ";
						if (rs.getFloat("priceeuroex") < 0.7 * Webroutines.targetprice(fwsrating)) {
							recommendation += "<b>Super P/Q<b>";
						} else {
							recommendation += "Good P/Q";
						}
					}
				}
				if (!onlyrecommendations || !recommendation.equals("")) {
					sb.append("<tr>");
					sb.append("<td  style='text-align:left;border-width:1px;border-style:solid;'>"
							+ (rs.getBoolean("new") ? "New!" : "") + "</td>");
					sb.append(
							"<td  style='text-align:left;border-width:1px;border-style:solid;'><a href='shopdetails.jsp?wineid="
									+ rs.getString("id") + "'>"
									+ (rs.getString("vintage").equals("0") ? "" : rs.getString("vintage") + " ")
									+ Webroutines.formatCapitals(rs.getString("name")).replace("&", "&amp;") + "("
									+ rs.getString("appellation") + ")" + "</a></td>");
					sb.append("<td style='text-align:right;border-width:1px;border-style:solid;'>"
							+ formatSizecompact(rs.getFloat("size")) + "</td>");
					sb.append("<td style='text-align:right;border-width:1px;border-style:solid;'>"
							+ formatPrice(rs.getDouble("priceeuroin"), (double) 0, currency, "IN") + "</td>");
					sb.append(
							"<td style='text-align:right;border-width:1px;border-style:solid;'>" + minprice + "</td>");
					sb.append("<td style='text-align:left;border-width:1px;border-style:solid;'>" + rating + "</td>");
					sb.append("<td style='text-align:left;border-width:1px;border-style:solid;'>" + recommendation
							+ "</td>");
					sb.append("</tr>");
				}
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while getting Shoppinglist", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
		sb.append("</table>");

		return sb.toString();

	}

	public static Wineset getRecommendedWineset(int shopid) {
		StringBuffer sb = new StringBuffer();
		sb.append(
				"<table class='shoppinglist' style='border-collapse: collapse;empty-cells:show;overflow:visible;width:100%;border-width:1px;border-style:solid;border-spacing:0px;'>");
		String query;
		String lastcreatedate = Dbutil.readValueFromDB(
				"select max(createdate) as maxdate from wines where shopid=" + shopid + ";", "maxdate");
		int fwsrating = 0;
		NumberFormat format = new DecimalFormat("0.00");
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		ArrayList<Wine> recommended = new ArrayList<Wine>();
		Wine wine;
		try {

			query = "select ma.id,wines.name, (wines.createdate='" + lastcreatedate
					+ "') as new, wines.lft, wines.knownwineid, knownwines.appellation, wines.size, ma.vintage, ma.priceeuroex from materializedadvice ma join wines on (wines.id=ma.id) left join knownwines on wines.knownwineid=knownwines.id where ma.shopid="
					+ shopid + " and wines.knownwineid>0 order by appellation,pqratio desc;";
			rs = Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
			while (rs.next()) {
				wine = new Wine(rs.getString("id"));
				String rating = wine.getRatingList() + "";
				if (rating.equals("0"))
					rating = "";
				if (rs.getInt("knownwineid") > 0 && rs.getInt("vintage") > 0) {
					try {
						query = "select min(priceeuroex) as minprice from wines where knownwineid="
								+ rs.getString("knownwineid") + " and size=" + rs.getString("size") + " and vintage="
								+ rs.getString("vintage") + " group by knownwineid,size,vintage having count(*)>1;";
						rs2 = Dbutil.selectQuery(query, con);
						if (rs2.next()) {
							if (rs2.getDouble("minprice") > 0) {
								wine.relativeprice = rs.getDouble("priceeuroex") / rs2.getDouble("minprice");
							}
						}
					} catch (Exception e) {
					}
				}
				fwsrating = 0;
				try {
					if (!"".equals(rating)) {
						fwsrating = Integer
								.parseInt(
										Dbutil.readValueFromDB(
												"select * from ratinganalysis where knownwineid=" + wine.Knownwineid
														+ " and vintage=" + wine.Vintage + " and author='FWS';",
												"rating"));
					}
				} catch (Exception e) {
				}
				if (fwsrating > 0) {
					wine.pqratio = Webroutines.targetprice(fwsrating) / rs.getDouble("priceeuroex");
				}
				if (wine.pqratio > 1 || (wine.relativeprice > 0 && wine.relativeprice < 1.2))
					recommended.add(wine);
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while getting Shoppinglist", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
		Wineset w = new Wineset();
		w.records = recommended.size();
		w.Wine = new Wine[w.records];
		for (int i = 0; i < recommended.size(); i++) {
			w.Wine[i] = recommended.get(i);
		}
		return w;
	}

	public static float targetprice(int rating) {
		float price = (float) 0;
		if (rating >= 80 && rating <= 100) {
			price = targetprice[rating];
		}
		return price;
	}

	public static ArrayList<ArrayList<String>> bannerInfo(int partnerid) {
		ArrayList<ArrayList<String>> bannerinfo = new ArrayList<ArrayList<String>>();
		NumberFormat format = new DecimalFormat("#,##0.00");
		ResultSet banners = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (partnerid > 0) {
				banners = Dbutil
						.selectQuery("select * from banners where partnerid=" + partnerid + " order by id desc;", con);
				int i = 0;
				while (banners.next()) {
					bannerinfo.add(new ArrayList<String>());
					bannerinfo.get(i).add(banners.getString("id"));
					bannerinfo.get(i).add(banners.getString("html"));
					bannerinfo.get(i).add(format.format(banners.getFloat("payperclick")));
					bannerinfo.get(i).add(banners.getString("views"));
					bannerinfo.get(i).add(banners.getString("clicks"));
					bannerinfo.get(i).add(banners.getString("active"));
					bannerinfo.get(i).add(banners.getString("link"));
					bannerinfo.get(i).add(banners.getString("country"));
					bannerinfo.get(i).add(banners.getString("payperclick"));
					i++;
				}
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up banner info for partnerid " + partnerid, E);
		} finally {
			Dbutil.closeRs(banners);
			Dbutil.closeConnection(con);
		}

		return bannerinfo;
	}

	public static String query2table(String query, boolean header, boolean number) {
		StringBuffer sb = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		int row = 0;
		int column;
		try {
			rs = Dbutil.selectQuery(rs, query, con);
			sb.append("<table>");
			java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			if (header) {
				sb.append("<tr>");
				if (number)
					sb.append("<th>n</th>");
				// get the column names; column indexes start from 1
				for (int i = 1; i < numberOfColumns + 1; i++) {
					sb.append("<th>" + rsMetaData.getColumnName(i) + "</th>");
				}
				sb.append("</tr>");
			}
			while (rs.next()) {
				row++;
				sb.append("<tr>");
				column = 1;
				if (number)
					sb.append("<td class='c" + column + "'>" + row + "</td>");
				for (int i = 1; i < numberOfColumns + 1; i++) {
					column++;
					if (rs.getString(i) != null && !rs.getString(i).startsWith("http")) {
						sb.append("<td class='c" + column + "'>" + rs.getString(i) + "</td>");
					} else {
						sb.append("<td class='c" + column + "'><a href='" + (rs.getString(i)) + "' target='_blank'>"
								+ rs.getString(i) + "</a></td>");

					}
				}
				sb.append("</tr>");

			}
			sb.append("</table>");
		} catch (Exception e) {
			Dbutil.logger.error("", e);
			sb = new StringBuffer();
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		String h = sb.toString();
		Pattern pattern = Pattern.compile("<a href=\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(h);
		while (matcher.find()) {
			h = h.replace("\"" + matcher.group(1) + "\"",
					"\"" + URLEncode(Webroutines.removeAccents(matcher.group(1))) + "\"");
		}
		h = h.replaceAll("%3A", ":");
		h = h.replaceAll("%3F", "?");
		h = h.replaceAll("%3D", "=");
		h = h.replaceAll("%2F", "/");
		h = h.replaceAll("<table></table>", "");
		return h;
	}

	public static String getVisitorOverview(int days) {

		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String html = "";
		html = html + "<table width='100%' border='0' align='left'><tr>" + "<th align='right'>Date / time</th>"
				+ "<th align='right'>Unique visitors</th>" + "<th align='right'>Hits</th>"
				+ "<th align='right'>Searches</th>" + "<th align='right'>Clicks</th>"
				+ "<th align='right'>Retailers</th>" + "<th align='right'>Data Feed Info</th>"
				+ "<th align='right'>Data Feed Edit</th>" + "<th align='right'>Bot hits</th>"
				+ "<th align='right'>Abuse</th>" + "<th align='right'>RSS Hits/Readers</th></tr>\n";
		NumberFormat format = new DecimalFormat("#0.00");
		try {
			String query = "select searchdate, sum(search) as searches, sum(uniquevisitors) as uniquevisitors,sum(clicked) as clicks, sum(datafeed) as datafeed, sum(datafeedinfo) as datafeedinfo, sum(rss) as rss, sum(rsshits) as rsshits, sum(hits) as hits, sum(bothits) as bothits,sum(retailers) as retailers, sum(abuse) as abuse from ("
					+
					// Search
					"SELECT date(date) as searchdate, count(*) as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type = 'Search' and name !='' group by to_days(date) union " +
					// Visitors
					"SELECT date(date) as searchdate, 0 as search, count(distinct(ip)) as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, count(*) as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type!='RSS' and type not like 'Abuse%' group by to_days(date) union "
					+
					// RSS
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,count(distinct(ip)) as rss, 0 as rsshits,0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='RSS' group by to_days(date) union " +
					// RSS Hits
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, count(*) as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='RSS' group by to_days(date) union " +
					// Bots
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits, count(*) as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=1 group by to_days(date) union " +
					// Abuse
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits, 0 as bothits,0 as retailers, count(*) as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type like 'Abuse%' group by to_days(date) union " +
					// Clicks
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,count(*) as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='Link clicked' and cpc=0 group by to_days(date) union " +
					// Retailers
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,count(*) as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='Retailers' and cpc=0 group by to_days(date) union " +
					// Data Feed Info
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,0 as clicked,0 as datafeed,count(distinct(ip)) as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='Data Feed info page'  group by to_days(date) union " +
					// Data Feed Edit
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,0 as clicked, count(distinct(ip)) as datafeed, 0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days
					+ " DAY) and bot=0 and type='Edit Data Feed' group by to_days(date)) as thetable group by searchdate order by searchdate desc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				html = html + "<tr align='right'><td><a href='visitordetails.jsp?date=" + rs.getString("searchdate")
						+ "'>" + rs.getString("searchdate") + "</a></td>" + "<td>" + rs.getString("uniquevisitors")
						+ "</td>" + "<td>" + rs.getString("hits") + "</td>" + "<td>" + rs.getString("searches")
						+ "</td>" + "<td>" + rs.getString("clicks") + "</td>" + "<td>" + rs.getString("retailers")
						+ "</td>" + "<td>" + rs.getString("datafeedinfo") + "</td>" + "<td>" + rs.getString("datafeed")
						+ "</td>" + "<td>" + rs.getString("bothits") + "</td>" + "<td>" + rs.getString("abuse")
						+ "</td>" + "<td>" + rs.getString("rsshits") + "/" + rs.getString("rss") + "</td>" + "</tr>\n";
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up visitor overview", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";

		return html;

	}

	public static String getSMSVisitorOverview() {

		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String html = "";
		NumberFormat format = new DecimalFormat("#0.00");
		int days = 3;
		try {
			String query = "select searchdate, sum(search) as searches, sum(uniquevisitors) as uniquevisitors,sum(clicked) as clicks, sum(datafeed) as datafeed, sum(datafeedinfo) as datafeedinfo, sum(rss) as rss, sum(rsshits) as rsshits, sum(hits) as hits, sum(bothits) as bothits,sum(retailers) as retailers, sum(abuse) as abuse from ("
					+
					// Search
					"SELECT date(date) as searchdate, count(*) as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days
					+ " DAY) and bot=0 and page like '/%index.jsp' and type not like 'Abuse%' and name !='' group by to_days(date) union "
					+
					// Visitors
					"SELECT date(date) as searchdate, 0 as search, count(distinct(ip)) as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, count(*) as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type!='RSS' and type not like 'Abuse%' group by to_days(date) union "
					+
					// RSS
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,count(distinct(ip)) as rss, 0 as rsshits,0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='RSS' group by to_days(date) union " +
					// RSS Hits
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, count(*) as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='RSS' group by to_days(date) union " +
					// Bots
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits, count(*) as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=1 group by to_days(date) union " +
					// Abuse
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors, 0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits, 0 as bothits,0 as retailers, count(*) as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type like 'Abuse%' group by to_days(date) union " +
					// Clicks
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,count(*) as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='Link clicked' and cpc=0 group by to_days(date) union " +
					// Retailers
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,0 as clicked,0 as datafeed,0 as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,count(*) as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='Retailers' and cpc=0 group by to_days(date) union " +
					// Data Feed Info
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,0 as clicked,0 as datafeed,count(*) as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days + " DAY) and bot=0 and type='Data Feed info page'  group by to_days(date) union " +
					// Data Feed Edit
					"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,0 as clicked, count(*) as datafeed, sum(cpc) as datafeedinfo,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "
					+ days
					+ " DAY) and bot=0 and type='Edit Data Feed' group by to_days(date)) as thetable group by searchdate order by searchdate desc;";
			rs = Dbutil.selectQuery(query, con);
			int i = 0;
			while (rs.next()) {
				if (i == 0)
					html += "Today:";
				if (i == 1)
					html += "Yest:";
				html += rs.getString("uniquevisitors") + "v " + rs.getString("retailers") + "r "
						+ rs.getString("datafeedinfo") + "dfi " + rs.getString("datafeed") + "df. ";
				i++;
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up visitor overview", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return html;

	}

	public static String getShopWineOverview(int shopid, String order) {

		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String html = "";
		html = html + "<table><tr>" + "<th align='left'>Wine</th>" + "<th align='left'>Recognized as</th>"
				+ "<th align='left'>Price</th>" + "</tr>\n";
		NumberFormat format = new DecimalFormat("#0.00");
		try {
			String query = "select * from wines left join knownwines on (wines.knownwineid=knownwines.id) where shopid="
					+ shopid + ";";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				html = html + "<tr align='left'><td>" + rs.getString("name") + "</td>" + "<td>" + rs.getString("wine")
						+ "</td>" + "<td align='right'>&euro; " + formatPrice(rs.getDouble("priceeuroex")) + "</td>"
						+ "</tr>\n";
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up visitor overview", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";

		return html;

	}

	/*
	 * Retrieves an overview of the referres int the last "history" days, grouped by
	 * domain name
	 */
	public static String getReferrers(int history, int rows) {
		String html = "<table style='table-layout:fixed;'><tr>"
				+ "<th style='width:250px;'>Referrer</th><th style='width:50px;'># referrals</th></tr>";
		if (rows == 0)
			rows = 9999;
		ResultSet rs = null;
		String query = "";
		Connection con = Dbutil.openNewConnection();
		if (history == 0)
			history = 7;
		try {
			String historywhereclause = "";
			if (history > 0) {
				long longtime = new java.util.Date().getTime();
				longtime = longtime - (long) history * 1000 * 3600 * 24;
				Timestamp date = new Timestamp(longtime);
				historywhereclause = " and date>now()-interval " + history + " day ";
			}
			// query="select reverse(referrershort) as
			// search,reverse(mid(referrershort,1,(if(locate('.',referrershort)>0,locate('.',referrershort)-1,100))))
			// as ref, sum(count) as count from (select
			// referrer,(reverse(replace(replace(mid(referrer,8,(length(referrer)-locate('.',reverse(referrer),(length(referrer)-if(LOCATE('/',referrer,9)>0,LOCATE('/',referrer,9),length(referrer)))+1)-7)),'.com',''),'.co','')))
			// as referrershort, count(distinct(ip)) as count from logging where
			// referrer!='' and referrer not like '%localhost%' and referrer not like
			// '%freewinesearcher%' and referrer not like '%vinopedia%' and referrer not
			// like '%192.168.%' "+historywhereclause+" group by referrershort order by
			// count desc limit "+rows+") sel group by ref order by count desc;";
			query = "select referrershort as search,reverse(mid(referrershort,if(left(referrershort,4)='moc.',4,0)+if(left(referrershort,3)='oc.',3,0)+1,(if(locate('.',referrershort,if(left(referrershort,4)='moc.',4,0)+if(left(referrershort,3)='oc.',3,0)+1)>0,locate('.',referrershort,if(left(referrershort,4)='moc.',4,0)+if(left(referrershort,3)='oc.',3,0)+1)-1-(if(left(referrershort,4)='moc.',4,0)+if(left(referrershort,3)='oc.',3,0)),100)))) as ref, sum(count) as count from (select referrer,(reverse(mid(referrer,8,(length(referrer)-locate('.',reverse(referrer),(length(referrer)-if(LOCATE('/',referrer,9)>0,LOCATE('/',referrer,9),length(referrer)))+1)-7)))) as referrershort, count(distinct(ip)) as count from logging where referrer!='' and referrer not like '%localhost%' and referrer not like '%freewinesearcher%' and referrer not like '%vinopedia%' and referrer not like '%192.168.%' "
					+ historywhereclause + " group by referrershort order by count desc limit " + rows
					+ ") sel group by ref order by count desc;";
			// query="select
			// mid(referrer,8,(length(referrer)-locate(\".\",reverse(referrer),(length(referrer)-LOCATE(\"/\",referrer,9))+1)-7))
			// as referrershort, count(distinct(ip)) as count from logging where
			// referrer!='' and referrer not like '%localhost%' and referrer not like
			// '%freewinesearcher%' and referrer not like '%vinopedia%' and referrer not
			// like '%192.168.%' "+historywhereclause+" group by referrershort order by
			// count desc limit "+rows+";";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				html = html
						+ "<tr align='left'><td style='width:250px;overflow:hidden;text-overflow:ellipsis;'><a href='referrers.jsp?referrer="
						+ rs.getString("ref") + "&history=7' target='_blank'>"
						+ (rs.getString("ref").length() > 30 ? (rs.getString("ref").substring(0, 30))
								: rs.getString("ref"))
						+ "</a></td>" + "<td>" + rs.getString("count") + "</td>" + "</tr>\n";
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up referrer overview", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";
		return html;
	}

	public static String getReferrerDetails(int history, String referrer) {
		String html = "<table border='0' ><tr>" + "<th>Referrer</th><th># referrals</th></tr>";
		ResultSet rs = null;
		String query;
		String historywhereclause = "";
		Connection con = Dbutil.openNewConnection();
		try {
			if (history > 0) {
				long longtime = new java.util.Date().getTime();
				longtime = longtime - (long) history * 1000 * 3600 * 24;
				Timestamp date = new Timestamp(longtime);
				historywhereclause = " and date>'" + date.toString() + "'";
			}
			query = "select *, count(*) as count from logging where referrer like '%" + referrer + "%' "
					+ historywhereclause + " group by referrer order by count desc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				html = html + "<tr align='left'><td><a href='" + rs.getString("referrer") + "'>"
						+ rs.getString("referrer") + "</a></td>" + "<td>" + rs.getString("count") + "</td>" + "</tr>\n";
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up referrer overview", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";
		return html;

	}

	public static String getVisitorDetails(String date, String order, boolean markfirsttimeusers) {
		if (order == null || order.equals(""))
			order = "date";
		StringBuffer html = new StringBuffer();
		html.append("<table border='1' ><tr>" + "<th>Date / time</th>" + "<th>Type</a></th>" + "<th>Hostname</a></th>"
				+ "<th>Country user</a></th>" + "<th>Name</a></th>" + "<th>Vintage</a></th>" + "<th># hits</a></th>"
				+ "<th>Loadtime</a></th>" + "<th>Country</a></th>" + "<th>IP</a></th>" + "<th>Referrer</a></th>");

		ResultSet rs = null;
		boolean firsttimeuser = false;
		Connection con = Dbutil.openNewConnection();
		try {
			String query = "select * from logging where date>'" + date + "' and date<DATE_ADD('" + date
					+ "',INTERVAL 1 DAY) and bot=0 and type not like 'Abuse%' and type not like 'RSS%' order by date desc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				firsttimeuser = false;
				if (markfirsttimeusers && rs.getString("date")
						.equals(Dbutil.readValueFromDB(
								"Select min(date) as mindate from logging where ip='" + rs.getString("ip") + "';",
								"mindate"))) {
					firsttimeuser = true;
				}
				html.append("<tr align='left'><td border='1' >" + rs.getString("date") + "</td>"
						+ "<td  border='1' align='left'>" + rs.getString("type") + "</td>" + "<td border='1' >"
						+ (firsttimeuser ? "<font color='green';><b>" : "") + rs.getString("hostname")
						+ (firsttimeuser ? "</b></font>" : "") + "</td>" + "<td border='1' >"
						+ rs.getString("hostcountry") + "</td>" + "<td border='1' >" + rs.getString("name") + "</td>"
						+ "<td border='1' >" + rs.getString("vintage") + "</td>" + "<td border='1' >"
						+ (rs.getString("type").equals("Search") ? rs.getString("numberofresults") : "") + "</td>"
						+ "<td border='1' >"
						+ (rs.getInt("loadtime") > 2000 ? "<font color='red'>" : "<font color='black'>")
						+ rs.getInt("loadtime") + "</font></td>" + "<td border='1' >" + rs.getString("countryseller")
						+ "</td>" + "<td border='1' >" + rs.getString("ip") + "</td>" + "<td border='1' width=30>"
						+ (rs.getString("type").startsWith("404") ? ("404 for " + rs.getString("page"))
								: rs.getString("referrer"))
						+ "</td>" + "</tr>");
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Admin clickdetails", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html.append("</table>");

		return html.toString();

	}

	public static String getTopSearches() {
		String html = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(
					"select name, count(*) as cnt from logging where type='Search' and name not regexp '%' and name !='' and name !='laf' and bot=0 group by name order by cnt desc limit 100;",
					con);
			while (rs.next()) {
				html = html + "<a href='/wine/" + Webroutines.URLEncode(rs.getString("name")) + "'>"
						+ rs.getString("name") + "</a><br/>";
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top search list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		html = html + "</table>";
		return html;
	}

	public static String getTopWines(int offset) {
		final int numberperpage = 100;
		NumberFormat knownwineformat = new DecimalFormat("000000");
		if (offset < 0)
			offset = 0;
		String html = "";
		String name = "";
		String nameregion = "";
		ResultSet rs = null;
		int records = 0;
		Connection con = Dbutil.openNewConnection();
		try {

			rs = Dbutil.selectQuery(
					"select SQL_CALC_FOUND_ROWS * from knownwines where numberofwines>3 order by wine limit "
							+ numberperpage + " offset " + offset + ";",
					con);
			rs = Dbutil.selectQuery("SELECT FOUND_ROWS() as records;", con);
			rs.next();
			records = rs.getInt("records");
			html += "Showing records " + (offset + 1) + " to "
					+ (offset + numberperpage < records ? offset + numberperpage : records) + " of " + records
					+ " wines.<br/>";
			if (offset > 0) {
				html += "<a href='/topwines.jsp?offset=" + (offset - numberperpage)
						+ "'>Previous 100</a>&nbsp;&nbsp;&nbsp;&nbsp;";
			}
			if (offset + numberperpage < records) {
				html += "<a href='/topwines.jsp?offset=" + (offset + numberperpage) + "'>Next 100</a>";
			}
			html += "<br/>";
			rs = Dbutil.selectQuery(
					"select SQL_CALC_FOUND_ROWS * from knownwines where numberofwines>3 order by wine limit "
							+ numberperpage + " offset " + offset + ";",
					con);
			while (rs.next()) {
				String extraid = "";
				if (rs.getBoolean("samename")) {
					extraid = knownwineformat.format(rs.getInt("id")) + " ";

				}
				name = Knownwines.getKnownWineName(rs.getInt("id"));
				nameregion = Knownwines.getKnownWineAndRegionName(rs.getInt("id"));
				html = html + "<a href=\"https://www.vinopedia.com/wine/" + extraid
						+ Webroutines.URLEncode(Webroutines.removeAccents(name)).replaceAll("&", "&amp;") + "\">"
						+ nameregion + "</a><br/>";
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		html += "";
		if (offset > 0) {
			html += "<a href='/topwines.jsp?offset=" + (offset - numberperpage)
					+ "'>Previous 100</a>&nbsp;&nbsp;&nbsp;&nbsp;";
		}
		if (offset + numberperpage < records) {
			html += "<a href='/topwines.jsp?offset=" + (offset + numberperpage) + "'>Next 100</a>";
		}
		html = html + "</table>";
		return html;
	}

	public static int getNumberOfTopWines() {
		ResultSet rs = null;
		int records = 0;
		Connection con = Dbutil.openNewConnection();
		try {

			rs = Dbutil.selectQuery("select SQL_CALC_FOUND_ROWS id from knownwines where numberofwines>3;", con);
			rs = Dbutil.selectQuery("SELECT FOUND_ROWS() as records;", con);
			rs.next();
			records = rs.getInt("records");
		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return records;
	}

	public static String getMobileSiteMap() {
		StringBuffer html = new StringBuffer();
		String name = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			html.append("<url><loc>https://www.vinopedia.com/m</loc>\n");
			html.append("<changefreq>daily</changefreq>\n");
			html.append("<priority>1.0</priority><mobile:mobile/></url>\n");
			rs = Dbutil.selectQuery(
					"select SQL_CALC_FOUND_ROWS * from knownwines where numberofwines>4 order by numberofwines desc  limit 50000;",
					con);
			while (rs.next()) {
				html.append("<url><loc>https://www.vinopedia.com/mwine/"
						+ Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine"))).replace("&", "&amp;")
								.replaceAll("%2F", "/")
						+ "</loc><changefreq>daily</changefreq><mobile:mobile/></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getSiteMap(int limitlow, int limithigh) {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		NumberFormat knownwineformat = new DecimalFormat("000000");
		Connection con = Dbutil.openNewConnection();
		String high = "";
		if (limithigh > 0)
			high = " and numberofwines<=" + limithigh;
		try {
			html.append("<url><loc>https://www.vinopedia.com</loc>\n");
			html.append("<changefreq>daily</changefreq>\n");
			html.append("<priority>1.0</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/tips.jsp</loc>\n");
			html.append("<changefreq>daily</changefreq>\n");
			html.append("<priority>1.0</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/region/</loc>\n");
			html.append("<changefreq>monthly</changefreq>\n");
			html.append("<priority>1.0</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/retailers.jsp</loc>\n");
			html.append("<changefreq>monthly</changefreq>\n");
			html.append("<priority>0.1</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/links.jsp</loc>\n");
			html.append("<changefreq>monthly</changefreq>\n");
			html.append("<priority>0.1</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/publishers.jsp</loc>\n");
			html.append("<changefreq>monthly</changefreq>\n");
			html.append("<priority>0.1</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/about.jsp</loc>\n");
			html.append("<changefreq>monthly</changefreq>\n");
			html.append("<priority>0.1</priority></url>\n");
			html.append("<url><loc>https://www.vinopedia.com/contact.jsp</loc>\n");
			html.append("<changefreq>monthly</changefreq>\n");
			html.append("<priority>0.1</priority></url>\n");
			rs = Dbutil.selectQuery("select SQL_CALC_FOUND_ROWS * from knownwines where numberofwines>" + limitlow
					+ high + " order by numberofwines desc limit 49900;", con);
			while (rs.next()) {
				html.append("<url><loc>https://www.vinopedia.com/wine/"
						+ (rs.getBoolean("samename") ? (knownwineformat.format(rs.getInt("id")) + "+") : "")
						+ Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine"))).replace("&", "&amp;")
								.replaceAll("%2F", "/")
						+ "</loc>\n");
				html.append("<priority>1</priority>\n");
				html.append("<changefreq>daily</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getSiteMapVintages() {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		NumberFormat knownwineformat = new DecimalFormat("000000");
		Connection con = Dbutil.openNewConnection();
		try {

			rs = Dbutil.selectQueryRowByRow(
					"select knownwines.wine, knownwines.id, knownwines.samename, wines.vintage from knownwines join wines on (knownwines.id=wines.knownwineid) where numberofwines>50 and vintage > 1984 and knownwineid>0 group by knownwineid,vintage order by numberofwines desc, knownwines.id,vintage limit 50000;",
					con);
			while (rs.next()) {
				html.append(
						"<url><loc>https://www.vinopedia.com/wine/"
								+ (rs.getBoolean("samename") ? (knownwineformat.format(rs.getInt("id")) + "+") : "")
								+ Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine")))
										.replace("&", "&amp;").replaceAll("%2F", "/")
								+ "+" + rs.getString("vintage") + "</loc>\n");
				html.append("<priority>0.5</priority>\n");
				html.append("<changefreq>weekly</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getSiteMapRecentVintages() {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		NumberFormat knownwineformat = new DecimalFormat("000000");
		Connection con = Dbutil.openNewConnection();
		try {

			rs = Dbutil.selectQueryRowByRow(
					"select knownwines.wine, knownwines.id, knownwines.samename, wines.vintage from knownwines join wines on (knownwines.id=wines.knownwineid) where  vintage > 2004 and knownwineid>0 group by knownwineid,vintage having count(*)>3 order by numberofwines desc, knownwines.id,vintage limit 50000;",
					con);
			while (rs.next()) {
				html.append(
						"<url><loc>https://www.vinopedia.com/wine/"
								+ (rs.getBoolean("samename") ? (knownwineformat.format(rs.getInt("id")) + "+") : "")
								+ Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine")))
										.replace("&", "&amp;").replaceAll("%2F", "/")
								+ "+" + rs.getString("vintage") + "</loc>\n");
				html.append("<priority>0.5</priority>\n");
				html.append("<changefreq>weekly</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getRegionSiteMap() {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			html.append("<url><loc>https://www.vinopedia.com/region/</loc>\n");
			html.append("<priority>0.5</priority>\n");
			html.append("<changefreq>weekly</changefreq></url>\n");
			rs = Dbutil.selectQuery("select  * from kbregionhierarchy where skip=0 and id>100 order by id;", con);
			while (rs.next()) {
				html.append("<url><loc>https://www.vinopedia.com/region/"
						+ Regioninfo.locale2href(rs.getString("region")) + "/</loc>\n");
				html.append("<priority>0.5</priority>\n");
				html.append("<changefreq>weekly</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getMobileRegionSiteMap() {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			html.append("<url><loc>https://www.vinopedia.com/mregion/</loc>\n");
			html.append("<priority>0.5</priority><mobile:mobile/>\n");
			html.append("<changefreq>weekly</changefreq></url>\n");
			rs = Dbutil.selectQuery("select  * from kbregionhierarchy where skip=0 and id>100 order by id;", con);
			while (rs.next()) {
				html.append("<url><loc>https://www.vinopedia.com/mregion/"
						+ Regioninfo.locale2href(rs.getString("region")) + "/</loc>\n");
				html.append("<priority>0.5</priority><mobile:mobile/>\n");
				html.append("<changefreq>weekly</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getWinerySiteMap() {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(
					"select *,sum(numberofwines) as totalwines from knownwines group by producer having totalwines>3 order by totalwines desc;",
					con);
			while (rs.next()) {
				html.append("<url><loc>https://www.vinopedia.com/winery/"
						+ Webroutines.URLEncodeUTF8Normalized((rs.getString("producer"))) + "</loc>\n");
				html.append("<priority>1.0</priority>\n");
				html.append("<changefreq>weekly</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating top wine list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static String getMobileWinerySiteMap() {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(
					"select *,sum(numberofwines) as totalwines from knownwines group by producer having totalwines>3 order by totalwines desc;",
					con);
			while (rs.next()) {
				html.append("<url><loc>https://www.vinopedia.com/mwinery/"
						+ Webroutines.URLEncodeUTF8Normalized((rs.getString("producer"))) + "</loc>\n");
				html.append("<priority>0.5</priority><mobile:mobile/>");
				html.append("<changefreq>weekly</changefreq></url>\n");
			}

		} catch (Exception E) {
			Dbutil.logger.error("Problem while creating mobile winery list", E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html.toString();
	}

	public static boolean validUserCode(String username, String code) {
		boolean valid = false;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(
					"select username from users where username='" + username + "' and password='" + code + "';", con);
			if (rs.next()) {
				valid = true;
			}
		} catch (Exception E) {
			Dbutil.logger.error(
					"Problem while verifying username/passcode for search. Username= " + username + ", code= " + code,
					E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return valid;
	}

	public static String getUserCode(String username) {
		String code = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("select username,password from users where username='" + username + "';", con);
			if (rs.next()) {
				code = rs.getString("password");
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while retrieving passcode for user= " + username, E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return code;
	}

	public static void saveReferrer(String ip, String referer) {
		if (referer == null)
			referer = "";
		if (referer.length() > 250)
			referer = referer.substring(0, 250);
		if (referer.toLowerCase().contains("vinopedia.com"))
			referer = "";
		if (!referer.equals("")) {
			// save
			java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
			Dbutil.executeQuery("Insert into referrer (date, ip, country, referer) values ('" + now + "','" + ip + "','"
					+ getCountryCodeFromIp(ip) + "','" + Spider.SQLEscape(referer) + "');");
		}
	}

	public static String getEmail(String username) {
		ResultSet usernames = null;
		Connection con = Dbutil.openNewConnection();
		String email = "";
		try {
			// Loop over each user with an active search
			usernames = Dbutil.selectQuery(
					"SELECT distinct u.username, u.email as email, p.status from users u, pricealertusers p where u.username = '"
							+ username + "' and u.email=p.email;",
					con);
			if (usernames.next()) {
				email = usernames.getString("status") + ":" + usernames.getString("email");
			} else {
				usernames = Dbutil.selectQuery(
						"SELECT distinct username, email from users u where u.username = '" + username + "';", con);
				if (usernames.next()) {
					email = ":" + usernames.getString("email");
				}
			}
		} catch (Exception e) {

		} finally {
			Dbutil.closeRs(usernames);
			Dbutil.closeConnection(con);
		}
		return email;
	}

	public static float getLinkBid(int shopid) {
		float amount = (float) 0.0;
		try {
			amount = Float.parseFloat(
					Dbutil.readValueFromDB("select costperclick from shops where id=" + shopid + ";", "costperclick"));
		} catch (Exception e) {
			Dbutil.logger.error("Could not read CPC for shopid " + shopid, e);
		}
		return amount;
	}

	public static float setLinkBid(int shopid, float amount) {
		try {
			int i = Dbutil.executeQuery("update shops set costperclick=" + amount + " where id=" + shopid + ";");
			if (i == 0) {
				Dbutil.logger.error("Could update CPC for shopid " + shopid);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could update CPC for shopid " + shopid, e);
		}
		amount = (float) 0;
		try {
			amount = Float.parseFloat(
					Dbutil.readValueFromDB("select costperclick from shops where id=" + shopid + ";", "costperclick"));
		} catch (Exception e) {
			Dbutil.logger.error("Could not read CPC for shopid " + shopid, e);
		}
		return amount;
	}

	public static String sendActivationMail(String username, String email, String emailurlencoded) {
		String message = "";
		String mailmessage = "";
		int hash = 0;
		int i = 0;
		message = "Problem in sending email";
		mailmessage = "<html><body>Hi, <br/><br/>";
		mailmessage = mailmessage + "This is your activation email from vinopedia.com. Please ";
		mailmessage = mailmessage + "<a href=\"https://www.vinopedia.com/activateaccount.jsp?username=";
		mailmessage = mailmessage + username + "&amp;email=" + emailurlencoded + "&amp;activationcode="
				+ username.concat(email).hashCode() + "\">click here</a>";
		mailmessage = mailmessage + " to activate your account.<br/>";
		mailmessage = mailmessage + "If you did not subscribe, you can disregard this email, we won't bother you again";
		mailmessage = mailmessage + "</body></html>";
		Emailer emailer = new Emailer();
		if (emailer.sendEmail("do_not_reply@vinopedia.com", email, "Account activation", mailmessage)) {
			message = "Success";
		}

		return message;
	}

	public static String activateAccount(String username, String email, String activationcode) {
		String message = "";
		int i = 0;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (activationcode.equals(String.valueOf(username.concat(email).hashCode()))) { // code correct
				rs = Dbutil.selectQuery("select * from pricealertusers where email='" + email + "';", con);
				if (rs.next()) { // user exists in table: update status
					i = Dbutil.executeQuery("update pricealertusers set status='active' where email='" + email + "';");
				} else {
					i = Dbutil.executeQuery(
							"insert into pricealertusers (email, status) values ('" + email + "','active');");
				}
				if (i == 0) {
					message = "Unknown error";
				} else {
					message = "Success";
				}
			} else {
				message = "Username/activation code incorrect";
			}

		} catch (Exception e) {
			message = "Unknown error";
			e.printStackTrace();
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return message;
	}

	public static Boolean checkEmail(String address) {
		if (getRegexPatternValue("^[A-Za-z0-9+_.-]+@(.+)$", address).length() == 0)
			return false;
		return true;
	}

	public static String deactivateAccount(String username, String email, String activationcode) {
		String message = "";
		int i = 0;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (activationcode.equals(String.valueOf(username.concat(email).hashCode()))) { // code correct
				changeStatus(email, "inactive");
				message = "Success";
			} else {
				message = "Username/activation code incorrect";
			}

		} catch (Exception e) {
			message = "Unknown error";
			Dbutil.logger.error("Unknown error", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return message;
	}

	public static void changeStatus(String email, String status) {
		if (status.equals("active") || status.equals("inactive"))
			Dbutil.executeQuery("update pricealertusers set status='" + status + "' where email='" + email + "';");
	}

	public static String getOrder(String order, String column) {
		if (order.equalsIgnoreCase(column)) {
			column = column + " DESC";
		}
		if (column.contains("DESC DESC")) {
			Dbutil.logger.error("The resulting order is screwed up because it contains DESC DESC. Order is " + order
					+ ", column is " + column);
			column = column.replaceAll("DESC DESC", "");
		}
		return column;
	}

	public static boolean saveLabel(String knownwineid, String candidate) {
		if (knownwineid != null && knownwineid.length() > 0 && candidate != null && candidate.length() > 4) {
			File src = new File("C:\\" + "labels" + System.getProperty("file.separator") + "CT"
					+ System.getProperty("file.separator") + knownwineid + System.getProperty("file.separator")
					+ candidate);
			File dest = new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
					+ knownwineid + "." + Webroutines.getRegexPatternValue(".*\\.(...)$", candidate));
			try {
				copy(src, dest);
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	public static boolean saveLabelFromUrl(String knownwineid, String url) {
		int maxsize = 250;
		InputStream is = null;
		HttpURLConnection urlcon = null;
		if (knownwineid != null && knownwineid.length() > 0 && url != null && url.length() > 4)
			try {
				SuckCTLabels ct = new SuckCTLabels();
				ct.pause = 0;
				url = url.replaceAll(" ", "%20");
				urlcon = ct.getUrlConnection(url);
				try {
					is = urlcon.getInputStream();
				} catch (Exception e) {
					Coordinates.doPause((int) (ct.pause * 200 + Math.round(Math.random() * ct.pause * 1800)));
					urlcon.disconnect();
					urlcon = ct.getUrlConnection(url);

					is = urlcon.getInputStream();
				}
				String extension = Webroutines.getRegexPatternValue("\\.(...)$", url);
				if (extension.equals("")) {
					String type = urlcon.getContentType();
					if (type != null) {
						type = type.toLowerCase();
						if (type.contains("jpeg"))
							extension = "jpg";
						if (type.contains("gif"))
							extension = "gif";
						if (type.contains("png"))
							extension = "png";
						if (type.contains("jpg"))
							extension = "jpg";

					}
				}
				BufferedImage image = null;
				image = ImageIO.read(is);
				if (image.getHeight() > 0) {
					if (image.getHeight() > maxsize || image.getWidth() > maxsize) {
						int width = 0;
						int height = 0;
						if (image.getHeight() > image.getWidth()) {
							width = (maxsize * image.getWidth()) / image.getHeight();
							height = maxsize;
						} else {
							height = (maxsize * image.getHeight()) / image.getWidth();
							width = maxsize;
						}
						// Create new (blank) image of required (scaled) size

						BufferedImage scaledImage = new BufferedImage(width, height, image.getType());

						// Paint scaled version of image to new image

						Graphics2D graphics2D = scaledImage.createGraphics();
						graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						graphics2D.drawImage(image, 0, 0, width, height, null);

						// clean up

						graphics2D.dispose();
						image = scaledImage;

					}
					ImageIO.write(image, extension.toLowerCase(),
							new File("/workspace/labels/" + knownwineid + "." + extension));

				}

				// Dbutil.executeQuery("update labels set sourceurl='"+Spider.SQLEscape(url)+"
				// where knownwineid="+knownwineid);
				return true;
			} catch (Exception e) {
				Dbutil.logger.error("Cannot save label image", e);
				return false;
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (Exception e) {
					}
				if (urlcon != null)
					try {
						urlcon.disconnect();
					} catch (Exception e) {
					}
			}
		return false;
	}

	public static void copy(File source, File dest) throws IOException {
		FileChannel in = null, out = null;
		try {
			in = new FileInputStream(source).getChannel();
			out = new FileOutputStream(dest).getChannel();

			long size = in.size();
			MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

			out.write(buf);

		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	public static void skipLabel(int knownwineid) {
		String candidatedir = "C:\\" + "labels" + System.getProperty("file.separator") + "CT"
				+ System.getProperty("file.separator");
		File dir = new File(candidatedir + knownwineid);
		for (File f : dir.listFiles())
			f.delete();
		dir.delete();
		Dbutil.executeQuery("update labels set sourceurl='Skipped' where knownwineid=" + knownwineid);
	}

	public static String selectLabelHtml() {
		StringBuffer html = new StringBuffer();
		String candidatedir = "C:\\" + "labels" + System.getProperty("file.separator") + "CT"
				+ System.getProperty("file.separator");
		String labeldir = Configuration.workspacedir + "labels" + System.getProperty("file.separator");
		File candidates = new File(candidatedir);
		File labels = new File(labeldir);
		File[] candidate = candidates.listFiles();
		File[] label = labels.listFiles();
		ArrayList<String> knownwineids = new ArrayList<String>();
		int knownwineid = 0;
		for (File c : label) {
			knownwineids.add(Webroutines.getRegexPatternValue("(.*)\\....$", c.getName()));
		}
		for (File c : candidate) {
			if (!knownwineids.contains(c.getName())) {
				knownwineid = Integer.parseInt(c.getName());
				String name = Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "wine");
				html.append("<title>Label selector " + knownwineid + "</title>");
				html.append("<h2>" + knownwineid + " "
						+ Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "wine") + ", "
						+ Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "locale")
						+ "</h2>");
				/*
				 * File[] images=new File(candidatedir+c.getName()).listFiles(); for (File
				 * i:images){ html.append("<a href='selectlabels.jsp?knownwineid="+knownwineid+
				 * "&candidate="+i.getName()+"'><img src='/labelcandidates/CT/"+knownwineid+"/"+
				 * i.getName()+"'/></a>"); }
				 */
				html.append("<br/>");
				html.append("<script type='text/javascript'>var knownwineid=" + knownwineid
						+ ";var url='http://www.cellartracker.com/list.asp?Table=LabelImage&Page=0&Wine="
						+ SuckCT.URLEncode(name) + "';</script>");
				html.append("<a href='selectlabels.jsp?action=skip&knownwineid=" + knownwineid
						+ "'>Geen label te vinden</a>");
				html.append(
						"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='http://images.google.com/images?hl=en&rlz=1B3GGGL_enNL303NL303&um=1&ie=UTF-8&sa=N&tab=wi&q="
								+ URLEncodeUTF8(name) + "' target='_blank'>Zoek label met Google</a>");
				html.append(
						"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<form action='selectlabels.jsp'><input type='text' name='url' size=70/><input type='hidden' name='knownwineid' value='"
								+ knownwineid + "'/><input type='submit' name='action' value='Save URL'/></form>");
				break;
			}
		}
		return html.toString();

	}

	public static String selectLabelHtml(int extraoffset, String editor) {
		if (editor == null)
			editor = "";
		int offset = 0;
		offset = Dbutil.readIntValueFromDB("select * from config where configkey='labelcounter" + editor + "'", "value")
				+ extraoffset;
		StringBuffer html = new StringBuffer();
		// String
		// candidatedir="C:\\"+"labels"+System.getProperty("file.separator")+"CT"+System.getProperty("file.separator");
		String labeldir = Configuration.workspacedir + "labels" + System.getProperty("file.separator");
		File labels = new File(labeldir);
		File[] themeFiles = labels.listFiles();
		ArrayList<Integer> existinglabels = new ArrayList<Integer>();
		for (File themeFile : themeFiles) {
			if (!"".equals(Webroutines.getRegexPatternValue("^(\\d+)\\.", themeFile.getName())))
				existinglabels
						.add(Integer.parseInt(Webroutines.getRegexPatternValue("^(\\d+)\\.", themeFile.getName())));
		}

		int knownwineid = 0;
		while (knownwineid == 0) {
			knownwineid = Dbutil.readIntValueFromDB(
					"select * from knownwines order by numberofwines desc,id limit " + offset + ",1;", "id");
			if (!existinglabels.contains(knownwineid)) {

				Dbutil.executeQuery(
						"update config set value=" + offset + " where configkey='labelcounter" + editor + "'");
				String name = Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "wine");
				html.append("<title>Label selector " + knownwineid + "</title>");
				html.append("<h2>" + offset + " "
						+ Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "wine") + ", "
						+ Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "locale")
						+ "</h2>");
				/*
				 * File[] images=new File(candidatedir+c.getName()).listFiles(); for (File
				 * i:images){ html.append("<a href='selectlabels.jsp?knownwineid="+knownwineid+
				 * "&candidate="+i.getName()+"'><img src='/labelcandidates/CT/"+knownwineid+"/"+
				 * i.getName()+"'/></a>"); }
				 */
				String query;
				ResultSet rs = null;
				Connection con = Dbutil.openNewConnection();
				try {
					query = "select * from pictureurls where knownwineid=" + knownwineid;
					rs = Dbutil.selectQuery(rs, query, con);
					while (rs.next()) {
						html.append("<a href='selectlabels.jsp?knownwineid=" + knownwineid
								+ "&amp;action=Save URL&amp;url=" + Webroutines.URLEncode(rs.getString("url"))
								+ "'><img src='" + rs.getString("url") + "'/></a>");
					}
				} catch (Exception e) {
					Dbutil.logger.error("", e);
				} finally {
					Dbutil.closeRs(rs);
					Dbutil.closeConnection(con);
				}
				html.append("<br/>");
				html.append("<script type='text/javascript'>var knownwineid=" + knownwineid
						+ ";var url='http://www.cellartracker.com/list.asp?Table=LabelImage&Page=0&szSearch="
						+ SuckCT.URLEncode(removeAccents(name)) + "';</script>");
				html.append(
						"<a id='skip' href='https://www.vinopedia.com/moderator/selectlabels.jsp?action=skip&knownwineid="
								+ knownwineid + "'>Overslaan</a>");
				html.append(
						"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='http://images.google.com/images?hl=en&rlz=1B3GGGL_enNL303NL303&um=1&ie=UTF-8&sa=N&tab=wi&q="
								+ URLEncodeUTF8(name) + "' target='_blank'>Zoek label met Google</a>");
				html.append(
						"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<form action='https://www.vinopedia.com/moderator/selectlabels.jsp'><input type='text' name='url' size=70/><input type='hidden' name='knownwineid' value='"
								+ knownwineid + "'/><input type='submit' name='action' value='Save URL'/></form>");

			} else {
				knownwineid = 0;
				offset++;
			}

		}

		return html.toString();

	}

	public static String showalllabels() {
		StringBuffer html = new StringBuffer();
		String candidatedir = "C:\\" + "labels" + System.getProperty("file.separator") + "CT"
				+ System.getProperty("file.separator");
		String labeldir = Configuration.workspacedir + "labels" + System.getProperty("file.separator");
		File candidates = new File(candidatedir);
		File labels = new File(labeldir);
		File[] candidate = candidates.listFiles();
		File[] label = labels.listFiles();
		ArrayList<String> knownwineids = new ArrayList<String>();
		int knownwineid = 0;
		for (File c : label) {
			try {
				knownwineid = Integer.parseInt(Webroutines.getRegexPatternValue("(.*)\\....$", c.getName()));
				html.append("<h2>" + Dbutil.readValueFromDB("select * from knownwines where id=" + knownwineid, "wine")
						+ "</h2>");
				html.append("<img src='/labels/" + c.getName() + "'/><br/><br/>");
			} catch (Exception e) {
			}
		}

		return html.toString();

	}

	public static String obsoletedidYouMean(String name, String pagelink) {
		boolean newterms = false;
		name = name.replaceAll("'", " ");
		String alt = "";
		String link = "";
		String altterm;
		if (name.length() > 2) {
			String[] terms = name.split(" ");
			for (int i = 0; i < terms.length; i++) {
				if (terms[i].startsWith("-")) {
					alt = alt + "-";
					link = link + "-";
					terms[i] = terms[i].substring(1);
				}
				if (terms[i].length() > 2) {
					altterm = obsoletelookupkeyword(terms[i]);
					if (!altterm.equalsIgnoreCase(terms[i])) {
						newterms = true;
						alt = alt + "<i>" + altterm + "</i> ";
						link = link + altterm + " ";
					} else {
						alt = alt + altterm + " ";
						link = link + altterm + " ";
					}

				} else {
					alt = alt + terms[i] + " ";
					link = link + terms[i] + " ";
				}
			}
			alt = "<a href='" + pagelink + "?name="
					+ Spider.replaceString(link, "'", " ").substring(0, link.length() - 1) + "&offset=0'>Did you mean "
					+ alt.substring(0, alt.length() - 1) + "?</a>";
		}
		if (!newterms)
			alt = "";
		return alt;
	}

	public static String obsoletelookupkeyword(String keyword) {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("set @abc='';", con);
			String query = "call lookupkeyword('" + keyword + "',@abc);";
			rs = Dbutil.selectQuery(query, con);
			rs = Dbutil.selectQuery("select @abc as keyword;", con);

			if (!rs.isAfterLast()) {
				rs.next();
				if (rs.getString("keyword") != null) {
					keyword = rs.getString("keyword").toLowerCase();
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while getting didyoumean. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return keyword;
	}

	public static String getLogDetails(String shopid) {
		String log = "<table>";
		String line = "";
		java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
		String currentdate = now.toString();
		try {
			BufferedReader logreader = new BufferedReader(new FileReader(Configuration.FWSConfig.getProperty("logdir")
					+ Configuration.FWSConfig.getProperty("batchlogfile")));
			while (line != null) {
				line = logreader.readLine();
				if (line != null) {
					if (line.contains("hop " + shopid + " "))
						if (line.contains(currentdate)) {
							log = log + "<tr><td style='white-space: nowrap '>";
							if (line.contains("ERROR"))
								log = log + "<font color='red'>";
							if (line.contains("WARN"))
								log = log + "<font color='orange'>";
							log = log.replaceAll("<br/>", "");
							log = log + line;
							if (line.contains("ERROR"))
								log = log + "</font>";
							log = log + "</td></tr>";
						}
				}
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while opening log file: ", exc);
		}
		log = log + "</table>";
		return log;
	}

	public static String testshop(int shopid, String auto) {
		String html = "<TABLE><TR><TH>Url</TH><TH>Wines found</TH></TR>";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("Select count(*) as total from " + auto + "wines where shopid=" + shopid + ";",
					con);
			rs.next();
			html = html + "<TR><TD>Total # wines in database</TD><TD>" + rs.getString("Total") + "</TD></TR>";
			Dbutil.closeRs(rs);
			rs = Dbutil.selectQuery(
					"Select sum(winesfound) as total from " + auto + "scrapelist where shopid=" + shopid + ";", con);
			rs.next();
			if (rs.getString("Total") != null) {
				html = html + "<TR><TD>Total # wines found in testrun</TD><TD>" + rs.getString("Total") + "</TD></TR>";
			} else {
				Dbutil.closeRs(rs);
				rs = Dbutil.selectQuery(
						"Select max(lastupdated) as lastupdated from " + auto + "wines where shopid=" + shopid + ";",
						con);
				if (rs.next()) {
					String date = rs.getString("lastupdated");
					Dbutil.closeRs(rs);
					rs = Dbutil.selectQuery("Select count(*) as thecount from " + auto + "wines where shopid=" + shopid
							+ " and lastupdated='" + date + "';", con);
					if (rs.next()) {
						html = html + "<TR><TD>Total # wines found in testrun</TD><TD>" + rs.getString("thecount")
								+ "</TD></TR>";
					}
				}
			}
			Dbutil.closeRs(rs);
			rs = Dbutil.selectQuery("Select * from " + auto + "scrapelist where shopid=" + shopid + ";", con);
			while (rs.next()) {
				html = html + "<TR><TD><a href='" + Webroutines.URLEncode(rs.getString("Url")) + "' target='_blank'>"
						+ rs.getString("Url") + "</a></TD><TD>" + rs.getString("Winesfound") + "</TD></TR>";
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Could not get results for test shop from DB. ", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		html = html + "</TABLE>";

		return html;
	}

	public static Double getAveragePrice(String name, String vintage) {
		Double price = 0.0;
		if (!vintage.equals(""))
			vintage = "and vintage like '" + vintage + "'";
		String[] names = name.split(" ");
		if (names.length > 0) {
			name = names[0];
			for (int i = 1; i < names.length; i++) {
				name = name + "%' and name like '%" + Spider.SQLEscape(names[i]);
			}
		}
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		if (!name.equals("")) {
			rs = Dbutil.selectQuery("select priceeuroex from wines where name like '%" + name + "%' " + vintage
					+ " and size=0.75 order by priceeuroin;", con);
			try {
				if (rs.last()) {
					int n = ((rs.getRow() + 1) / 2);
					rs.absolute(n);
					price = rs.getDouble("priceeuroex");
				}
			} catch (Exception exc) {
				Dbutil.logger.error("Exception while determining the average price:", exc);
			}
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		return price;
	}

	public static Double getAveragePrice(int knownwineid, String vintage) {
		Double price = 0.0;
		if (!vintage.equals(""))
			vintage = "and vintage like '" + vintage + "'";

		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		rs = Dbutil.selectQuery("select priceeuroex from wines where knownwineid=" + knownwineid + " " + vintage
				+ " and size=0.75 order by priceeuroex;", con);
		try {
			if (rs.last()) {
				int n = ((rs.getRow() + 1) / 2);
				rs.absolute(n);
				price = rs.getDouble("priceeuroex");
			}

		} catch (Exception exc) {
			Dbutil.logger.error("Exception while determining the average price:", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return price;
	}

	public static Double getLowestPrice(String name, String vintage) {
		Double price = 0.0;
		if (!vintage.equals(""))
			vintage = "and vintage like '" + vintage + "'";
		String[] names = name.split(" ");
		if (names.length > 0) {
			name = names[0];
			for (int i = 1; i < names.length; i++) {
				name = name + "%' and name like '%" + Spider.SQLEscape(names[i]);
			}
		}
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		if (!name.equals("")) {
			rs = Dbutil.selectQuery("select min(priceeuroex) as minimum from wines where name like '%" + name + "%' "
					+ vintage + " and size=0.75;", con);
			try {
				if (rs.next()) {
					price = rs.getDouble("minimum");
				}
			} catch (Exception exc) {
			}
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);

		return price;
	}

	public static Double getLowestPrice(int knownwineid, String vintage) {
		Double price = 0.0;
		String vintagestr = "";
		if (vintage != null && !vintage.equals(""))
			vintagestr = "and vintage like '" + vintage + "'";

		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		rs = Dbutil.selectQuery("select min(priceeuroex) as minimum from wines where knownwineid=" + knownwineid + " "
				+ vintagestr + " and size=0.75;", con);
		try {
			if (rs.next()) {
				price = rs.getDouble("minimum");
			}
		} catch (Exception exc) {

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return price;
	}

	public static String formatPrice(Double price) {
		String priceformatted = "";
		NumberFormat format = new DecimalFormat("#,##0.00");
		try {
			priceformatted = format.format(price);
		} catch (Exception exc) {
		}
		return priceformatted;
	}

	public static String formatPrice(Double priceeuroin, Double priceeuroex, String currency, String vat) {
		Double price = 0.0;
		if (vat.equals("EX")) {
			price = priceeuroex;
		} else {
			price = priceeuroin;
		}
		String priceformatted = "";
		String symbol = "&euro;";
		if (currency == null || currency.equals(""))
			currency = "EUR";
		NumberFormat format = new DecimalFormat("#,##0.00");
		try {
			if (!currency.equals("EUR")) {
				Double pricefactor = Currency.getRate(currency);
				if (pricefactor != 0.0) {
					price = price / pricefactor;
					symbol = Currency.getSymbol(currency);
				}
			}

		} catch (Exception exc) {
			Dbutil.logger.error("Cannot convert to currency " + currency, exc);
		}
		try {
			priceformatted = symbol + "&nbsp;" + format.format(price);
			if ((format.format(price) + "").length() > 7) {
				priceformatted = "<span style='font-size:0.9em'>" + symbol + "&nbsp;" + format.format(price)
						+ "</span>";
			}
		} catch (RuntimeException e) {

		}

		return priceformatted;
	}

	public static String formatPriceMobile(Double priceeuroin, Double priceeuroex, String currency, String vat) {
		Double price = 0.0;
		if (vat.equals("EX")) {
			price = priceeuroex;
		} else {
			price = priceeuroin;
		}
		String priceformatted = "";
		String symbol = "&#8364;";
		if (currency == null || currency.equals(""))
			currency = "EUR";
		NumberFormat format = new DecimalFormat("#,##0.00");
		try {
			if (!currency.equals("EUR")) {
				Double pricefactor = Currency.getRate(currency);
				if (pricefactor != 0.0) {
					price = price / pricefactor;
					symbol = Currency.getSymbol(currency);
				}
			}

		} catch (Exception exc) {
			Dbutil.logger.error("Cannot convert to currency " + currency, exc);
		}
		try {
			priceformatted = symbol + "&#160;" + format.format(price);

		} catch (RuntimeException e) {

		}

		return priceformatted;
	}

	public static String formatPriceNoCurrency(Double priceeuroin, Double priceeuroex, String currency, String vat) {
		Double price = 0.0;
		if (currency.equals("undefined"))
			currency = "EUR";
		if (vat.equals("EX")) {
			price = priceeuroex;
		} else {
			price = priceeuroin;
		}
		String priceformatted = "";
		if (currency.equals(""))
			currency = "EUR";
		NumberFormat format = new DecimalFormat("#,##0.00");
		try {
			if (!currency.equals("EUR")) {
				Double pricefactor = Currency.getRate(currency);
				if (pricefactor != 0.0) {
					price = price / pricefactor;
				}
			}

		} catch (Exception exc) {
			Dbutil.logger.error("Cannot convert to currency " + currency, exc);
		}
		try {
			priceformatted = format.format(price);
		} catch (RuntimeException e) {

		}

		return priceformatted;
	}

	public static String getCurrencySymbol(String currency) {
		return Currency.getSymbol(currency);
	}

	public static void removeOldWines(String shopid) {
		String lastupdated = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("Select max(lastupdated) as lastupdated from wines where shopid=" + shopid + ";",
					con);
			if (rs.next()) {
				lastupdated = rs.getString("lastupdated");
			}
			if (!lastupdated.equals("")) {
				Dbutil.executeQuery("INSERT into history select * from wines where shopid=" + shopid
						+ " and lastupdated < '" + lastupdated + "';");
				Dbutil.executeQuery(
						"Delete from wines where shopid=" + shopid + " and lastupdated < '" + lastupdated + "';");
				Dbutil.executeQuery("update shops set succes=0 where id=" + shopid + ";");
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Could not remove obsolete wines for shop " + shopid, exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public static String displayOldWines(String shopid) {
		StringBuffer html = new StringBuffer();
		html.append("Overview of wines per last updated date:<br/><table><tr><th>Lastupdated</th><th>Count</th>");
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(
					"Select count(*) as count,lastupdated from wines where shopid=" + shopid + " group by lastupdated;",
					con);
			while (rs.next()) {
				html.append("<tr><td>" + rs.getInt("Count") + "</td><td>" + rs.getString("lastupdated") + "</td></tr>");
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Could not select obsolete wines for shop " + shopid, exc);
		}
		Dbutil.closeRs(rs);
		html.append("</table>");
		try {
			String lastupdate = Dbutil.readValueFromDB(
					"Select * from wines where shopid=" + shopid + " order by lastupdated desc limit 1;",
					"lastupdated");

			rs = Dbutil.selectQuery(
					"Select * from wines where shopid=" + shopid + " order by name,vintage,size limit 400;", con);
			html.append(
					"<br/><table><tr><th>Name</th><th>Vintage</th><th>Size</th><th>Lastupdated</th><th>Createdate</th></tr>");
			while (rs.next()) {
				if (!rs.getString("lastupdated").equals(lastupdate)) {
					html.append("<tr style='color:gray'>");
				} else {
					html.append("<tr>");
				}
				html.append("<td>" + rs.getString("Name") + "</td>");
				html.append("<td>" + rs.getString("Vintage") + "</td>");
				html.append("<td>" + rs.getString("Size") + "</td>");
				html.append("<td>" + rs.getString("Lastupdated") + "</td>");
				html.append("<td>" + rs.getString("Createdate") + "</td>");
				html.append("</tr>");
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Could not select obsolete wines for shop " + shopid, exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		html.append("</table>");
		return html.toString();
	}

	public static LinkedHashMap<String, Integer> getWSShops() {
		String query;
		ResultSet rs = null;
		LinkedHashMap<String, Integer> shops = new LinkedHashMap<String, Integer>();
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from wsshops order by name";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				shops.put(rs.getString("name"), rs.getInt("wsid"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return shops;
	}

	public static String displayScrapelist(String shopid) {
		StringBuffer html = new StringBuffer();
		html.append(
				"Overview of spidered URLs:<br/><table><tr><th>URL</th><th>Type</th><th>Status</th><th>Number of wines found</th>");
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		int wines = 0;
		try {
			rs = Dbutil.selectQuery("Select * from scrapelist where shopid=" + shopid + " order by id;", con);
			while (rs.next()) {
				html.append(
						"<tr><td><a href='" + rs.getString("url") + "' target='_blank'>" + rs.getString("url")
								+ "</a></td><td>" + rs.getString("urltype") + "</td><td>"
								+ (rs.getString("status").equals("Scraping") ? "<span style='color:red'>Error</span>"
										: rs.getString("status"))
								+ "</td><td>" + rs.getString("winesfound") + "</td></tr>");
				wines += rs.getInt("winesfound");
			}
			html.append("</table><br/>");
			html.append("<h2>In total " + wines + " wines were spidered, database currently contains " + Dbutil
					.readIntValueFromDB("select count(*) as thecount from wines where shopid=" + shopid, "thecount")
					+ " unique wines for this store.");

		} catch (Exception exc) {
			Dbutil.logger.error("Could not select urls for shop " + shopid, exc);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return html.toString();
	}

	public static String showWinesAbstract(String shopid, String auto) {
		String html = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("Select * from " + auto + "wines where shopid=" + shopid + " limit 1000;", con);
			html = html + "<table><tr><th>Name</th><th>Vintage</th><th>Size</th><th>Link to wine</th><th></th></tr>";
			while (rs.next()) {
				html = html + "<tr>";
				html = html + "<td>" + rs.getString("Name") + "</td>";
				html = html + "<td>" + rs.getString("Vintage") + "</td>";
				html = html + "<td>" + rs.getString("Size") + "</td>";
				html = html + "<td>" + rs.getString("SourceUrl") + "</td>";
				html = html + "<td></td>";
				html = html + "</tr>";
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Could not get wine abstract for shop " + shopid, exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		html = html + "</table>";
		return html;
	}

	public static void OBSOLETElogWebAction(String type, String page, String ip, String hostname, String referrer,
			String name, String vintage, int created, float pricemin, float pricemax, String countryseller,
			boolean rareold, String shopid, String wineid, String price, String wineurl, Double cpc) {
		if (countryseller == null)
			countryseller = "";
		if (countryseller.equals("All"))
			countryseller = "";
		if (referrer == null)
			referrer = "";
		if (referrer != null && referrer.contains("freewinesearcher.com"))
			referrer = "";
		boolean bot = checkBot(hostname);
		String hostcountry = Webroutines.getCountryCodeFromIp(ip);
		java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
		if (!Configuration.donotlogforhost.contains(";" + hostname + ";") && hostname != null
				&& !referrer.contains("host-tracker")) {
			Dbutil.executeQuery(
					"Insert into logging (type, page, date, ip, hostname, hostcountry, referrer, name, vintage, pricemin, pricemax, countryseller, created, rareold, shopid, wineid, wineurl, cpc,bot) values ('"
							+ type + "','" + page + "','" + now + "','" + ip + "','" + hostname + "','" + hostcountry
							+ "','" + referrer + "','" + Spider.SQLEscape(name) + "','" + Spider.SQLEscape(vintage)
							+ "','" + pricemin + "','" + pricemax + "','" + countryseller + "','" + created + "',"
							+ rareold + ",'" + shopid + "','" + wineid + "','" + wineurl + "','" + cpc + "'," + bot
							+ ");");
		}
	}

	public static void logWebAction(String type, String page, String ip, String referrer, String name, String vintage,
			int created, float pricemin, float pricemax, String countryseller, boolean rareold, String shopid,
			String wineid, String price, String wineurl, Double cpc) {
		Webactionlogger logger = new Webactionlogger(type, page, ip, referrer, name, vintage, created, pricemin,
				pricemax, countryseller, rareold, shopid, wineid, price, wineurl, cpc, 0);
		logger.logaction();
	}

	public static void logUserAgentAbuse(String useragent, String ip) {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (useragent != null && useragent.length() > 0) {
				if (useragent.length() > 350)
					useragent = useragent.substring(0, 350);

				rs = Dbutil.selectQueryForUpdate(
						"Select * from ipblocks where ipaddress='" + Spider.SQLEscape(ip) + "';", con);
				while (rs.next()) {
					if (rs.getString("useragent").equals("")) {
						rs.updateString("useragent", useragent);
						String hostname = "";
						try {
							hostname = InetAddress.getByName(ip).getHostName();
						} catch (Exception e) {
						}
						if (hostname != null && hostname.length() > 0) {
							if (hostname.length() > 250)
								hostname = hostname.substring(0, 250);
							if (rs.getString("hostname").equals("")) {
								rs.updateString("hostname", hostname);
							}
						}
						rs.updateRow();
					}

				}
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public static void logWebAction(String type, String page, String ip, String referrer, String name, String vintage,
			int created, float pricemin, float pricemax, String countryseller, boolean rareold, String shopid,
			String wineid, String price, String wineurl, Double cpc, Searchhistory searchhistory) {
		Webactionlogger logger = new Webactionlogger(type, page, ip, referrer, name, vintage, created, pricemin,
				pricemax, countryseller, rareold, shopid, wineid, price, wineurl, cpc, 0);
		logger.searchhistory = searchhistory;
		logger.logaction();
	}

	public static void logWebAction(String type, int knownwineid, String page, String ip, String referrer, String name,
			String vintage, int created, float pricemin, float pricemax, String countryseller, boolean rareold,
			String shopid, String wineid, String price, String wineurl, Double cpc, Searchhistory searchhistory) {
		Webactionlogger logger = new Webactionlogger(type, page, ip, referrer, name, vintage, created, pricemin,
				pricemax, countryseller, rareold, shopid, wineid, price, wineurl, cpc, 0);
		logger.searchhistory = searchhistory;
		logger.knownwineid = knownwineid;
		logger.logaction();
	}

	public static void logWebAction(String type, String page, String ip, String referrer, String name, String vintage,
			int created, float pricemin, float pricemax, String countryseller, boolean rareold, String shopid,
			String wineid, String price, String wineurl, Double cpc, int numberofresults, double loadtime) {
		Webactionlogger logger = new Webactionlogger(type, page, ip, referrer, name, vintage, created, pricemin,
				pricemax, countryseller, rareold, shopid, wineid, price, wineurl, cpc, 0);
		logger.logaction();
	}

	public static boolean checkBot(String hostname) {
		boolean bot = false;
		Connection con = Dbutil.openNewConnection();
		try {
			bot = checkBot(hostname, con);

		} catch (Exception e) {
			Dbutil.logger.error("Problem while looking up bot. ", e);
		} finally {
			Dbutil.closeConnection(con);
		}
		return bot;
	}

	public static boolean checkBot(String hostname, Connection con) {
		boolean bot = false;
		ResultSet rs = null;
		try {
			rs = Dbutil.selectQuery("select * from bots where '" + hostname + "' regexp regex;", con);
			if (rs.next())
				bot = true;
		} catch (Exception e) {
			Dbutil.logger.error("Problem while looking up bot. ", e);
		} finally {
			Dbutil.closeRs(rs);
		}
		return bot;
	}

	public static boolean suspectedBot(String hostname, String useragent) {
		if (useragent == null)
			return false;
		useragent = useragent.toLowerCase();
		try {
			if (useragent.contains("bot"))
				return true;
		} catch (Exception e) {
			Dbutil.logger.error("Problem while looking up bot. ", e);
		}
		return false;
	}

	public static String autoSuggest(String input) {
		// Dbutil.logger.info("Start suggest");
		String result = "";
		NumberFormat knownwineformat = new DecimalFormat("000000");
		input = filterUserInput(input);
		input = Spider.replaceString(input, "(", " ");
		input = Spider.replaceString(input, ")", " ");
		input = Spider.replaceString(input, ".", " ");
		input = Spider.replaceString(input, ",", " ");
		input = Spider.replaceString(input, "&", " ");
		input = Spider.replaceString(input, "'", " ");
		input = Spider.replaceString(input, "-", " ");
		input = Spider.replaceString(input, "\"", " ");
		input = Spider.replaceString(input, ";", " ");
		input = Spider.replaceString(input, "/", " ");
		input = Spider.replaceString(input, "@", " ");

		String[] terms = input.split(" ");
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = null;
		try {
			if (input.length() > 2) {
				String whereclause = " and match(wine,appellation) against ('";
				for (int i = 0; i < terms.length; i++) {
					if (terms[i].length() > 1) {
						whereclause = whereclause + "+" + terms[i] + "* ";
					}
				}
				whereclause += "' IN BOOLEAN MODE)";
				if (!whereclause.contains("''")) {
					con = Dbutil.openNewConnection();
					// rs=Dbutil.selectQuery("SELECT count(*) as thecount from knownwines where
					// disabled=0 "+whereclause+" and numberofwines>0 order by numberofwines
					// desc;",con);
					// Dbutil.logger.info("SELECT SQL_CALC_FOUND_ROWS * from knownwines where
					// disabled=0 "+whereclause+" and numberofwines>0 order by numberofwines desc
					// limit 15;");
					rs = Dbutil.selectQuery("SELECT SQL_CALC_FOUND_ROWS * from knownwines where disabled=0 "
							+ whereclause + " and numberofwines>0 order by numberofwines desc limit 15;", con);
					rs2 = Dbutil.selectQuery("SELECT FOUND_ROWS() as records;", con);
					if (rs2.next() && rs2.getInt("records") < 500) {
						// Dbutil.logger.info(rs2.getInt("records")+"records");
						// Dbutil.logger.info("SELECT count(*) as thecount from knownwines where
						// disabled=0 "+whereclause+" and numberofwines>0 order by numberofwines
						// desc;");
						// if (rs.next()){
						// if (rs.getInt("thecount")<500){
						// rs=Dbutil.selectQuery("SELECT SQL_CALC_FOUND_ROWS * from knownwines where
						// disabled=0 "+whereclause+" and numberofwines>0 order by numberofwines desc
						// limit 15;",con);
						while (rs.next()) {
							if (rs.getBoolean("samename")) {
								result = result + rs.getString("wine") + " (" + rs.getString("appellation") + ")\n";
								result = result + knownwineformat.format(rs.getInt("id")) + " " + rs.getString("wine")
										+ "\n";
							} else {
								result = result + rs.getString("wine").replaceAll("&", "&amp;") + "\n";
								result = result + (removeAccents(rs.getString("wine")).replaceAll("&", "&amp;")) + "\n";
							}

						}
					}
				}
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Auto Suggest for input " + input, E);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			if (con != null)
				Dbutil.closeConnection(con);
		}

		// Dbutil.logger.info("End suggest");

		return result;
	}

	public static String advertiseroverview() {
		String query;
		StringBuffer html = new StringBuffer();
		query = "select name,views,clicks from banners join partners on (banners.partnerid=partners.id) where active=1;";
		html.append("<h3>Banners</h3>");
		html.append(query2table(query, true, false));
		query = "SELECT shopname as Store,SUM(IF(page='Winead',1,0)) as Recommendation, SUM(IF(page='Featured',1,0)) as Featured, SUM(IF(page='Storead',1,0)) as Storead FROM logging join shops on (logging.shopid=shops.id) where type='winead' group by shopid order by count(*) desc;";
		html.append("<h3>Clicks on wine ads</h3>");
		html.append(query2table(query, true, false));
		query = "select shopname, count(*) as 'Orders sent', avg(wineprice) as 'Average value', sum(wineprice) as 'Total value'  from logging join shops on (logging.shopid=shops.id) where type like 'Sent%' group by shopid order by `Orders sent` desc, `Total value` desc;";
		html.append("<h3>Orders placed</h3>");
		html.append(query2table(query, true, false));
		return html.toString();
	}

	public static final String utf8Convert(String utf8String) throws java.io.UnsupportedEncodingException {
		byte[] bytes = new byte[utf8String.length()];
		for (int i = 0; i < utf8String.length(); i++) {
			bytes[i] = (byte) utf8String.charAt(i);
		}
		return new String(bytes, "UTF-8");
	}

	public static String smartSuggest(String input) {
		// Dbutil.logger.info("Start suggest");
		StringBuffer result = new StringBuffer();
		NumberFormat knownwineformat = new DecimalFormat("000000");
		input = removeAccents(filterUserInput(input));
		input = Spider.replaceString(input, "'", " ");
		input = Spider.replaceString(input, "(", " ");
		input = Spider.replaceString(input, ")", " ");
		input = Spider.replaceString(input, ".", " ");
		input = Spider.replaceString(input, ",", " ");
		input = Spider.replaceString(input, "&", " ");
		input = Spider.replaceString(input, "'", " ");
		input = Spider.replaceString(input, "-", " ");
		input = Spider.replaceString(input, "\"", " ");
		input = Spider.replaceString(input, ";", " ");
		input = Spider.replaceString(input, "/", " ");
		input = Spider.replaceString(input, "@", " ");
		input = Spider.replaceString(input, "%", " ");
		String[] terms = input.split(" ");
		String query;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = null;
		int maxresults = 10;
		HashMap<Integer, LinkedHashMap<Integer, String>> props = new HashMap<Integer, LinkedHashMap<Integer, String>>();
		boolean combinations = true;
		try {
			if (input.length() > 2) {
				input = input.toLowerCase();
				con = Dbutil.openNewConnection();

				Dbutil.executeQuery("DROP TEMPORARY TABLE IF EXISTS `wijn`.`tempwine`;", con);
				Dbutil.executeQuery(
						"CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',	  FULLTEXT `namefulltext` (`name`))ENGINE=MyISAM;",
						con);
				Dbutil.executeQuery("Insert into tempwine (name) values ('"
						+ Spider.SQLEscape(input.substring(0, Math.min(input.length(), 290))) + "');", con);

				String candidates = "";
				query = "select *,match(shortregion) against('+(" + (input.replaceAll("\\s+", "* "))
						+ "*)' in boolean mode) as regionmatch,  match(propertydescription) against('+"
						+ (input.replaceAll("\\s+", "* +"))
						+ "*' in boolean mode) as score from aiproperties left join kbregionhierarchy on (aiproperties.propertyid=kbregionhierarchy.id) where typeid in (2,8) and match(propertydescription) against('+("
						+ (input.replaceAll("\\s+", "* "))
						+ "*)' in boolean mode) having (typeid=8 or regionmatch>0) order by score desc,length(propertydescription);";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					LinkedHashMap<Integer, String> c = props.get(rs.getInt("typeid"));
					if (c == null)
						c = new LinkedHashMap<Integer, String>();
					if (rs.getInt("typeid") == 2) {
						if (c.size() <= maxresults)
							c.put(rs.getInt("propertyid"), rs.getString("region"));
					} else {
						if (c.size() <= maxresults)
							c.put(rs.getInt("propertyid"), rs.getString("propertydescription"));
					}
					props.put(rs.getInt("typeid"), c);
				}
				Dbutil.closeRs(rs);
				query = "select * from airecognizer natural join aiproperties left join kbregionhierarchy on (aiproperties.propertyid=kbregionhierarchy.id) where typeid in (5,6,7) and (length(fts)>0);";
				rs = Dbutil.selectQuery(rs, query, con);
				int idd = 0;
				while (rs.next()) {
					rs2 = Dbutil.selectQuery("select * from tempwine where " + Recognizer.whereClause("name",
							rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl")) + ";", con);
					idd++;
					if (rs2.next()) {
						LinkedHashMap<Integer, String> c = props.get(rs.getInt("typeid"));
						if (c == null)
							c = new LinkedHashMap<Integer, String>();
						if (rs.getInt("typeid") == 2) {
							c.put(rs.getInt("propertyid"), rs.getString("shortregion"));
						} else {
							c.put(rs.getInt("propertyid"), rs.getString("propertydescription"));
						}
						props.put(rs.getInt("typeid"), c);
					}
				}
				Dbutil.closeRs(rs);
				Dbutil.closeRs(rs2);
				query = "select *,sum(numberofwines) as sumwines from knownwines where match(producer) against('+"
						+ (input.replaceAll("\\s+", "* +"))
						+ "*' in boolean mode) group by producer having sumwines>0 order by sumwines desc limit 10;";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					LinkedHashMap<Integer, String> c = props.get(1);
					if (c == null)
						c = new LinkedHashMap<Integer, String>();
					c.put(rs.getInt("id"), rs.getString("producer"));
					props.put(1, c);

				}
				Dbutil.closeRs(rs);
				Dbutil.closeRs(rs2);

				if (props.size() == 0 || (props.get(2) == null && props.get(8) == null)) {
					for (int a : props.keySet())
						input = input.replace(props.get(a).get(props.get(a).keySet().iterator().next()).toLowerCase(),
								"");
					combinations = false;
					if (input.replaceAll("\\s+", "").length() > 2) {
						query = "select * from aiproperties left join kbregionhierarchy on (aiproperties.propertyid=kbregionhierarchy.id) where typeid in (2,8) and match(propertydescription) against('+"
								+ (input.replaceAll("\\s+", "* +"))
								+ "*' in boolean mode) order by lft,propertydescription;";
						rs = Dbutil.selectQuery(rs, query, con);
						while (rs.next()) {
							LinkedHashMap<Integer, String> c = props.get(rs.getInt("typeid"));
							if (c == null)
								c = new LinkedHashMap<Integer, String>();
							if (rs.getInt("typeid") == 2) {
								if (rs.getString("shortregion") != null && rs.getString("shortregion").toLowerCase()
										.contains(input.toLowerCase().trim()))
									c.put(rs.getInt("propertyid"), rs.getString("region"));
							} else {
								c.put(rs.getInt("propertyid"), rs.getString("propertydescription"));
							}
							props.put(rs.getInt("typeid"), c);
						}
					}
				}
				String whereclause;
				whereclause = " against ('";
				for (int i = 0; i < terms.length; i++) {
					if (terms[i].length() > 1) {
						whereclause = whereclause + "+" + terms[i] + "* ";
					}
				}
				whereclause += "' IN BOOLEAN MODE)";
				if (!whereclause.contains("''")) {
					// Dbutil.logger.info("SELECT SQL_CALC_FOUND_ROWS * from knownwines where
					// disabled=0 and match(wine,appellation) "+whereclause+" and numberofwines>0
					// order by numberofwines desc limit 15;");
					rs = Dbutil.selectQuery(
							"SELECT SQL_CALC_FOUND_ROWS * from knownwines where disabled=0  and match(wine,appellation) "
									+ whereclause + " and numberofwines>0 order by numberofwines desc limit 15;",
							con);
					rs2 = Dbutil.selectQuery("SELECT FOUND_ROWS() as records;", con);
					if (rs2.next() && rs2.getInt("records") < 500) {
						boolean heading = true;
						while (rs.next()) {
							result.append("<div class='category" + (heading ? "head" : "") + "'>"
									+ (heading ? "<img src='/images/smallbottle.jpg' alt='No label'/>Wines" : "&nbsp;")
									+ "</div><div class='c" + (heading ? "head" : "") + "'>");
							// if (new
							// File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".jpg").exists()||new
							// File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".jpg").exists()){
							// result.append("<img src='/images/gen/labels/thumb/"+rs.getString("id")+"'
							// alt='"+rs.getString("wine").replaceAll("'", "&apos;")+"'/></div>");
							// } else {
							// result.append("<img src='/images/smallbottle.jpg' alt='No label'/></div>");
							// }
							result.append(Spider.escape(rs.getString("wine")) + " ("
									+ Spider.escape(rs.getString("appellation")) + ")</div>||");
							result.append(winelink((rs.getBoolean("samename") || rs.getString("wine").contains("�")
									? knownwineformat.format(rs.getInt("id")) + " "
									: "") + rs.getString("wine"), 0) + "\n");
							heading = false;

						}
					}
				}
				Dbutil.closeRs(rs);
				Dbutil.closeRs(rs2);
				if (props.size() > 0) {
					String type = getwinetype(props);
					if (props.get(2) != null) {
						boolean heading = true;
						for (int j : props.get(2).keySet()) { // all regions
							result.append(
									"<div class='category" + (heading ? "head" : "") + "'>"
											+ (heading ? "<img src='/images/world.gif' alt='wine-guide'/>Wine region"
													: "&nbsp;")
											+ "</div><div class='c" + (heading ? "head" : "") + "'>");
							heading = false;
							if (type.length() > 0 && !props.get(2).get(j).toLowerCase().contains(type.split("-")[0])) {
								result.append(type.split("-")[0].substring(0, 1).toUpperCase()
										+ type.split("-")[0].substring(1) + " ");
								result.append(
										"wines from "
												+ props.get(2).get(j).split(", ")[props.get(2).get(j).split(", ").length
														- 1]
												+ (props.get(2).get(j).split(", ").length > 1 ? " ("
														+ props.get(2).get(j).substring(0,
																props.get(2).get(j).length() - props.get(2).get(j)
																		.split(", ")[props.get(2).get(j)
																				.split(", ").length - 1].length()
																		- 2)
														+ ")" : "")
												+ "</div>||/wine-guide");
								result.append(type.split("-")[1]);
								result.append("/region/" + URLEncodeUTF8Normalized(
										props.get(2).get(j).split(", ")[props.get(2).get(j).split(", ").length - 1])
												.replaceAll("%2F", "%252F")
										+ "\n");
							} else {
								try {
									result.append(
											props.get(2).get(j).split(", ")[props.get(2).get(j).split(", ").length - 1]
													+ (props.get(2).get(j).split(", ").length > 1 ? " (" + props.get(2)
															.get(j).substring(0, props.get(2).get(j).length()
																	- props.get(2).get(j)
																			.split(", ")[props.get(2).get(j)
																					.split(", ").length - 1].length()
																	- 2)
															+ ")" : "")
													+ "</div>||");
									result.append("/region/" + Regioninfo.locale2href(props.get(2).get(j)) + "\n");
								} catch (Exception e) {
									Dbutil.logger.info("Problem in smartsuggest for region " + props.get(2).get(j)
											+ ", for suggestion " + input);
								}
							}
						}
					}
					if (props.get(8) != null) {
						boolean heading = true;
						for (int k : props.get(8).keySet()) { // all grapes
							result.append("<div class='category" + (heading ? "head" : "") + "'>"
									+ (heading ? "<img src='/images/grapered.jpg' alt='wine-guide'/>Grape" : "&nbsp;")
									+ "</div><div class='c" + (heading ? "head" : "") + "'>");
							heading = false;
							if (type.length() > 0 && !props.get(8).get(k).toLowerCase().contains(type.split("-")[0]))
								result.append(type.split("-")[0] + " ");
							result.append("Wines made from " + props.get(8).get(k) + "</div>||/wine-guide");
							if (type.length() > 0)
								result.append(type.split("-")[1]);
							result.append("/grape/" + URLEncodeUTF8Normalized(props.get(8).get(k)) + "\n");

						}
					}
					if (props.get(1) != null) {
						boolean heading = true;
						for (int k : props.get(1).keySet()) { // all wineries
							result.append("<div class='category" + (heading ? "head" : "") + "'>"
									+ (heading ? "<img src='/images/chateau.jpg' alt='winery'/>Winery" : "&nbsp;")
									+ "</div><div class='c" + (heading ? "head" : "") + "'>");
							heading = false;
							result.append(Spider.escape(props.get(1).get(k)) + "</div>||");
							result.append("/winery/"
									+ URLEncodeUTF8Normalized(props.get(1).get(k)).replaceAll("%2F", "/") + "\n");

						}
					}
					if (combinations && props.get(2) != null && props.get(8) != null && props.get(2).size() < 5
							&& props.get(8).size() < 5) {
						boolean heading = true;
						for (int j : props.get(2).keySet()) { // all regions
							for (int k : props.get(8).keySet()) { // all grapes
								result.append("<div class='category" + (heading ? "head" : "") + "'>"
										+ (heading ? "<img src='/images/book_icon.gif' alt='wine-guide'/>Wine guide"
												: "&nbsp;")
										+ "</div><div class='c" + (heading ? "head" : "") + "'>");
								heading = false;
								if (type.length() > 0 && !props.get(2).get(j).toLowerCase().contains(type.split("-")[0])
										&& type.length() > 0
										&& !props.get(8).get(k).toLowerCase().contains(type.split("-")[0]))
									result.append(type.split("-")[0] + " ");
								result.append(props.get(8).get(k) + " wines from "
										+ props.get(2).get(j).split(", ")[props.get(2).get(j).split(", ").length - 1]
										+ "</div>||/wine-guide");
								if (type.length() > 0)
									result.append(type.split("-")[1]);
								result.append(
										"/grape/" + URLEncodeUTF8Normalized(props.get(8).get(k)) + "/region/"
												+ URLEncodeUTF8Normalized(props.get(2).get(j)
														.split(", ")[props.get(2).get(j).split(", ").length - 1])
												+ "\n");
							}
						}
					} else {
						boolean heading = true;
						if (getwinetype(props).length() > 0) {
							result.append(
									"<div class='category" + (heading ? "head" : "") + "'>"
											+ (heading ? "<img src='/images/book_icon.gif' alt='wine-guide'/>Wine type"
													: "&nbsp;")
											+ "</div><div class='c" + (heading ? "head" : "") + "'>");
							heading = false;
							if (type.length() > 0)
								result.append(type.split("-")[0] + " ");
							result.append("Wines" + "</div>||/wine-guide");
							if (type.length() > 0)
								result.append(type.split("-")[1] + "\n");

						}

					}
				}
				query = "select * from shops where disabled=0 and id not in (200,204) and id in (select distinct(shopid) from wines) and shopname like '%"
						+ (input.trim().replaceAll("\\s+", "%")) + "%' order by shopname;";
				rs = Dbutil.selectQuery(rs, query, con);
				boolean heading = true;
				if (rs.last()) {
					if (rs.getRow() <= maxresults) {
						rs.beforeFirst();
						while (rs.next()) {
							result.append("<div class='category" + (heading ? "head" : "") + "'>"
									+ (heading
											? "<img src='/images/cart.jpg' alt='"
													+ rs.getString("shopname").replaceAll("'", "&quot;") + "'/>Merchant"
											: "&nbsp;")
									+ "</div><div class='c" + (heading ? "head" : "") + "'>");
							heading = false;
							result.append(rs.getString("shopname") + "</div>||");
							result.append("/store/"
									+ Webroutines.removeAccents(rs.getString("shopname")).replaceAll("&", "%26")
									+ "/#storegeneral\n");
						}
					}
				}

			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Auto Suggest for input " + input, E);
		} finally {
			if (con != null)
				Dbutil.executeQuery("DROP TEMPORARY TABLE IF EXISTS `wijn`.`tempwine`;", con);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			if (con != null)
				Dbutil.closeConnection(con);
		}

		// Dbutil.logger.info(result.toString());

		return result.toString();
	}

	public static String refinewinery(int retrieveid, String country, boolean reversepriority) {
		StringBuffer sb = new StringBuffer();
		String query;
		int id = 0;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		try {
			int n = Dbutil
					.readIntValueFromDB("select count(*) as num from  kbproducers  where priority>0 and address is null"
							+ (country != null && !country.equals("") && !country.equals("All")
									? " and country='" + Spider.SQLEscape(country) + "' "
									: "")
							+ "; ", "num");
			query = "select sel.*, group_concat(distinct(appellation)) as region from (select * from  kbproducers  where "
					+ (retrieveid > 0 ? "id=" + retrieveid : "address is null")
					+ (country != null && !country.equals("") && !country.equals("All")
							? " and country='" + Spider.SQLEscape(country) + "' "
							: "")
					+ " order by " + (reversepriority ? "(priority=-1) desc," : "")
					+ "priority desc limit 1) sel join knownwines on (sel.name=knownwines.producer) group by (name);";
			rs2 = Dbutil.selectQuery(rs, query, con);
			if (rs2.next()) {
				id = rs2.getInt("id");
				String name = rs2.getString("name");
				String region = Dbutil.readValueFromDB(
						"select group_concat(distinct(locale) SEPARATOR '<br/>') as region from knownwines where producer='"
								+ Spider.SQLEscape(name) + "';",
						"region");
				sb.append(n + "<br/>");
				sb.append("<h1>" + name + "</h1>");
				sb.append(Dbutil.readIntValueFromDB(
						"select count(*) as thecount from kbproducers where address is not null or priority=0;",
						"thecount") + "/45602<br/>");
				sb.append("<a href='http://www.google.com/search?num=100&hl=en&q=" + URLEncode(name)
						+ "' target='_blank'>Google this</a><br/>");
				sb.append("<a href='http://ablegrape.com/search.jsp?lang=en&query=" + URLEncode(name)
						+ "' target='_blank'>AbleGrape this</a><br/>");
				sb.append(region + "<br/>");
				name = name.replaceAll("(?<! |^)-", " ");
				name = name.replaceAll("( |^)-( |$)", " ");
				name = Spider.replaceString(name, "(", " ");
				name = Spider.replaceString(name, ")", " ");
				name = Spider.replaceString(name, ".", " ");
				name = Spider.replaceString(name, ",", " ");
				name = Spider.replaceString(name, "&", " ");
				name = Spider.replaceString(name, "'", " ");
				name = Spider.replaceString(name, "\"", " ");
				name = Spider.replaceString(name, ";", " ");
				name = Spider.replaceString(name, "/", " ");
				name = Spider.replaceString(name, "@", " ");
				name = Spider.replaceString(name, "%", " ");

				String nameclause = "";
				for (String term : name.split(" ")) {
					if (term.length() > 1) {
						nameclause += " +" + term;

					}
				}
				sb.append(
						"<table><tr><th style='text-align:left'>Name</th><th style='text-align:left'>Region</th><th style='text-align:left'>Address</th><th style='text-align:left'>Phone</th><th style='text-align:left'>Web</th><th style='text-align:left'>Email</th></tr>");
				query = "select * from producers where match(name,fulltextsearch) against('" + nameclause
						+ "')>1 order by match(name,fulltextsearch) against('" + nameclause + "') desc limit 20;";
				rs = Dbutil.selectQuery(rs, query, con);
				String namelower = removeAccents(name).toLowerCase();
				while (rs.next()) {
					sb.append("<tr><td>");
					for (String t : rs.getString("name").split("\\s+")) {
						if (namelower.contains(Webroutines.removeAccents(t).toLowerCase())) {
							sb.append("<b>" + t + "</b> ");
						} else {
							sb.append(t + " ");
						}
					}
					sb.append("</td><td>" + rs.getString("region")
							+ "</td><td><a href='#' onclick=\"javascript:document.getElementById('address').value=this.innerHTML;return false;\">"
							+ rs.getString("address").replace(rs.getString("name") + ", ", "")
							+ (rs.getString("address2") != null && rs.getString("address2").length() > 0
									? ", " + rs.getString("address2")
									: "")
							+ (rs.getString("postalcode") != null && rs.getString("postalcode").length() > 0
									? ", " + rs.getString("postalcode")
									: "")
							+ (rs.getString("city") != null && rs.getString("city").length() > 0
									? ", " + rs.getString("city")
									: "")
							+ (rs.getString("state") != null && rs.getString("state").length() > 0
									? ", " + rs.getString("state")
									: "")
							+ (rs.getString("country") != null && rs.getString("country").length() > 0
									? ", " + rs.getString("country")
									: "")
							+ "</a></td><td><a href='#' onclick=\"javascript:document.getElementById('telephone').value=this.innerHTML;return false;\">"
							+ rs.getString("telephone")
							+ "</a></td><td><a href='#' onclick=\"javascript:document.getElementById('website').value=this.innerHTML;if($('#website').attr('value').indexOf('http') < 0) document.getElementById('website').value='http://'+document.getElementById('website').value;$('#websitelink').attr('href',document.getElementById('website').value);$('#websitelink').html(document.getElementById('website').value);return false;\">"
							+ rs.getString("website")
							+ "</a></td><td><a href='#' onclick=\"javascript:document.getElementById('email').value=this.innerHTML;return false;\">"
							+ rs.getString("email") + "</a></td></tr>");
				}
				sb.append(
						"<tr><td></td><td></td><td><input type='text' name='address' id='address' size='60'"
								+ (rs2.getString("address") == null ? ""
										: "value='" + rs2.getString("address").replaceAll("'", "&apos;") + "'")
								+ "/></td><td><input type='text' name='telephone' id='telephone'"
								+ (rs2.getString("telephone") == null ? ""
										: "value='" + rs2.getString("telephone").replaceAll("'", "&apos;") + "'")
								+ "/></td><td><input type='text' name='website' size='20' id='website'"
								+ (rs2.getString("website") == null ? ""
										: "value='" + rs2.getString("website").replaceAll("'", "&apos;") + "'")
								+ "/></td><td><input type='text' name='email' id='email'"
								+ (rs2.getString("email") == null ? ""
										: "value='" + rs2.getString("email").replaceAll("'", "&apos;") + "'")
								+ "/></td></tr>");
				sb.append("<tr><td></td><td></td><td></td><td></td><td><a href='"
						+ (rs2.getString("website") == null ? "" : rs2.getString("website").replaceAll("'", "&apos;"))
						+ "' id='websitelink' target='_blank'/></td><td></td></tr>");
				sb.append("</table><input type='hidden' name='id' value='" + id + "'/>");

			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
		return sb.toString();

	}

	public static void correctWineryCoordinates() {
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from kbproducers where not address is null and accuracy=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				updateWineryCoordinates(rs.getString("id"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public static void updateWineryCoordinates(String id) {
		Dbutil.logger.info("updating winery coordinates for winery " + id);
		String[] coordinates = null;
		String address = Dbutil.readValueFromDB("select * from kbproducers where id=" + id, "address");
		if (address != null && address.length() > 0)
			coordinates = Coordinates.getCoordinates(address);

		if (coordinates != null && coordinates.length == 3) {
			Dbutil.executeQuery("update kbproducers set lon=" + coordinates[0] + ", lat=" + coordinates[1]
					+ ", accuracy=" + coordinates[2] + " where id=" + id);
		}
	}

	public static void savewinerydata(String id, String address, String telephone, String website, String email,
			int priority) {
		String[] coordinates = null;
		if (address != null && address.length() > 0)
			coordinates = Coordinates.getCoordinates(address);

		if (coordinates != null && coordinates.length == 3) {
			Dbutil.executeQuery("update kbproducers set lon=" + coordinates[0] + ", lat=" + coordinates[1]
					+ ", accuracy=" + coordinates[2] + ", address="
					+ (address == null ? "null" : "'" + Spider.SQLEscape(address) + "'") + ",telephone="
					+ (telephone == null ? "null" : "'" + Spider.SQLEscape(telephone) + "'") + ",website="
					+ (website == null ? "null" : "'" + Spider.SQLEscape(website) + "'") + ",email="
					+ (email == null ? "null" : "'" + Spider.SQLEscape(email) + "'") + ",priority=" + priority
					+ " where id=" + id);
		} else {

			Dbutil.executeQuery("update kbproducers set address="
					+ (address == null || address.equals("") ? "null" : "'" + Spider.SQLEscape(address) + "'")
					+ ",telephone=" + (telephone.equals("") ? "null" : "'" + Spider.SQLEscape(telephone) + "'")
					+ ",website=" + (website.equals("") ? "null" : "'" + Spider.SQLEscape(website) + "'") + ",email="
					+ (email.equals("") ? "null" : "'" + Spider.SQLEscape(email.replaceAll("^mailto:", "")) + "'")
					+ ",priority=" + priority + " where id=" + id);
		}
	}

	public static String getFreebaseData(int numberofproducers) {
		String query;
		NumberFormat knownwineformat = new DecimalFormat("000000");

		StringBuffer sb = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			sb.append("URI,wine,producer,appellation,vineyard,cuvee,locale,color,dryness,sparkling,grapes\n");
			query = "select id,samename,wine,producer,appellation,vineyard,cuvee,locale,color,dryness,sparkling,grapes from knownwines join (select distinct(producer) as producers from knownwines order by numberofwines desc limit "
					+ numberofproducers
					+ ") producers on (knownwines.producer=producers.producers) where disabled=0 and not wine like '%vodka%' and not wine like '%wodka%' and not wine like '%whisk%' and not wine like '%brandy%';";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				sb.append("\"" + "https://www.vinopedia.com/wine/"
						+ (rs.getBoolean("samename") ? (knownwineformat.format(rs.getInt("id")) + "+") : "")
						+ Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("wine"))).replaceAll("%2F", "/")
						+ "\"");
				for (int i = 3; i < 13; i++)
					sb.append(",\"" + rs.getString(i) + "\"");
				sb.append("\n");
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static String getwinetype(HashMap<Integer, LinkedHashMap<Integer, String>> props) {
		if (props.get(7) != null && props.get(7).size() == 1) {
			if (props.get(7).containsValue("Sparkling"))
				return "sparkling-/winetype/sparkling";
		}
		if (props.get(5) != null && props.get(5).size() == 1 && !props.get(5).containsValue("Dry")) {
			if (props.get(6) != null && props.get(6).size() == 1 && props.get(6).containsValue("Red")) {
				return "sweet red or port-/winetype/port";
			} else
				return "sweet white or dessert-/winetype/dessert";
		} else if (props.get(6) != null) {
			if (props.get(6).containsValue("Red"))
				return "red-/winetype/red";
			if (props.get(6).containsValue("White"))
				return "white-/winetype/white";
			if (props.get(6).containsValue("Ros�"))
				return "rose-/winetype/rose";
		}
		return "";

	}

	public static String autoSuggestRegion(String input, Guidedsearch gs) {

		// Dbutil.logger.info("Start suggest");
		StringBuffer result = new StringBuffer();
		NumberFormat knownwineformat = new DecimalFormat("000000");
		input = filterUserInput(input);
		input = Spider.replaceString(input, "(", " ");
		input = Spider.replaceString(input, ")", " ");
		input = Spider.replaceString(input, ".", " ");
		input = Spider.replaceString(input, ",", " ");
		input = Spider.replaceString(input, "&", " ");
		input = Spider.replaceString(input, "'", " ");
		input = Spider.replaceString(input, "-", " ");
		input = Spider.replaceString(input, "\"", " ");
		input = Spider.replaceString(input, ";", " ");
		input = Spider.replaceString(input, "/", " ");
		int limit = 15;
		String[] terms = input.split(" ");
		ResultSet rs = null;
		Connection con = null;
		try {
			if (input.length() > 1) {
				String whereclause = " match(shortregion) against ('";
				for (int i = 0; i < terms.length; i++) {
					if (terms[i].length() > 1) {
						whereclause = whereclause + "+" + terms[i] + "* ";
					}
				}
				whereclause += "' IN BOOLEAN MODE)";
				if (!whereclause.contains("''")) {
					con = Dbutil.openNewConnection();
					rs = Dbutil
							.selectQuery("SELECT distinct shortregion from kbregionhierarchy where shortregion like '"
									+ Spider.SQLEscape(input) + "%' order by shortregion;", con);
					while (limit > 0 && rs.next()) {
						if (gs == null || gs.advice.facets.region.containsKey(rs.getString("shortregion"))) {
							limit--;
							result.append(rs.getString("shortregion") + "\n");
						} else {
							// Dbutil.logger.info(rs.getString("shortregion"));
						}
					}
					Dbutil.closeRs(rs);
					rs = Dbutil.selectQuery("SELECT distinct shortregion from kbregionhierarchy where  " + whereclause
							+ " and  shortregion not like '" + Spider.SQLEscape(input) + "%' order by shortregion;",
							con);
					while (limit > 0 && rs.next()) {
						if (gs == null || gs.advice.facets.region.containsKey(rs.getString("shortregion"))) {
							limit--;
							result.append(rs.getString("shortregion") + "\n");
						}
					}
				}
			} else if (gs != null || gs.advice.facets.region.size() < 40) {
				for (String r : gs.advice.facets.region.keySet()) {
					// shortlist
				}
			}
		} catch (Exception E) {
			Dbutil.logger.error("Problem while looking up Auto Suggest for input " + input, E);
		} finally {
			Dbutil.closeRs(rs);
			if (con != null)
				Dbutil.closeConnection(con);
		}

		// Dbutil.logger.info(result);

		// Dbutil.logger.info("End suggest");

		return result.toString();
	}

	public static ArrayList<String> autoSuggestarraylist(String input) {
		ArrayList<String> result = new ArrayList<String>(0);
		String[] terms = input.split(" ");
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		if (input.length() > 2)
			try {
				String whereclause = " disabled=false";
				for (int i = 0; i < terms.length; i++) {
					whereclause = whereclause + " and match(wine) against '+" + terms[i] + "'";
				}
				rs = Dbutil.selectQuery("SELECT * from knownwines where " + whereclause + ";", con);
				while (rs.next()) {
					result.add(rs.getString("wine"));
				}
			} catch (Exception E) {
				Dbutil.logger.error("Problem while looking up Auto Suggest for input " + input, E);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		return result;
	}

	public static String getRatingsHTML(int knownwineid, int size, String searchpage, int singlevintage) {
		String html = "";
		ResultSet rs = null;
		String goodpqvintages = "";
		String vintageswithrecords = "";
		String[] author;
		Connection con = Dbutil.openNewConnection();
		if (knownwineid > 0) {
			int currentvintage;
			int startvintage = 0;
			String singlevintageclause = "";
			String cellclass = "even";
			try {
				// Determine which vintages we have in stock
				String query = "select ra.* from ratinganalysis ra where  ra.minpriceeuroex!=0 and knownwineid="
						+ knownwineid + " and author='FWS';";
				rs = Dbutil.selectQuery(query, con);
				while (rs.next()) {
					vintageswithrecords += "*" + rs.getInt("vintage");

				}
				vintageswithrecords += "*";

				// Determine which vintages have the best PQ ratio
				query = "select ra.*, (minpriceeuroex/price) as pq from ratinganalysis ra join pqratio pq on (ra.rating=pq.rating) where  ra.minpriceeuroex>0 and knownwineid="
						+ knownwineid + " and author='FWS' and vintage>1980 order by pq;";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					rs.last();
					int totalvintages = rs.getRow();
					rs.beforeFirst();
					if (totalvintages > 3) {
						int n = Math.round((float) ((totalvintages / 3) + 0.5));
						for (int i = 0; i < n; i++) {
							rs.next();
							goodpqvintages += "*" + rs.getInt("vintage");
						}
						goodpqvintages += "*";
					}
				}
				rs = Dbutil.selectQuery("select * from ratinganalysis where knownwineid=" + knownwineid
						+ " and author='FWS' group by author;", con);
				if (rs.next()) {
					rs.last();
					author = new String[rs.getRow()];
					rs.beforeFirst();
					int i = 0;
					while (rs.next()) {
						author[i] = rs.getString("author");
						i++;
					}
					html = "<table class='ratings' >";
					html = html + "<tr><td class='even'></td>";
					rs = Dbutil.selectQuery("select * from ratinganalysis where knownwineid=" + knownwineid
							+ " and author='FWS' group by vintage order by vintage desc;", con);
					rs.last();
					if (rs.getRow() > size - 1) {
						rs.absolute(size - 1);
						startvintage = rs.getInt("vintage") - 1;
					}
					if (singlevintage > 0 && singlevintage < startvintage) {
						rs = Dbutil.selectQuery("select * from ratinganalysis where knownwineid=" + knownwineid
								+ " and author='FWS' and vintage=" + singlevintage
								+ " group by vintage order by vintage desc;", con);
						if (rs.next()) {
							// The searched vintage is not in the ratinglist but we have a rating. Add it as
							// the first one.
							rs = Dbutil.selectQuery("select * from ratinganalysis where knownwineid=" + knownwineid
									+ " and author='FWS' group by vintage order by vintage desc;", con);
							rs.last();
							if (rs.getRow() > size) {
								rs.absolute(size - 1);
								startvintage = rs.getInt("vintage") - 1;
								singlevintageclause = " or vintage=" + singlevintage;

							}

						}

					}
					rs = Dbutil.selectQuery("select * from ratinganalysis where knownwineid=" + knownwineid
							+ " and (vintage>" + startvintage + singlevintageclause
							+ " ) and author='FWS' group by vintage order by vintage;", con);
					i = 0;
					if (rs.next()) {
						rs.last();
						String[] vintage = new String[rs.getRow()];
						String[] ratings = new String[rs.getRow()];
						rs.beforeFirst();
						while (rs.next()) {
							if (cellclass.equals("odd")) {
								cellclass = "even";
							} else {
								cellclass = "odd";
							}
							if (vintageswithrecords.contains("*" + rs.getString("vintage") + "*")) {
								html = html + "<td class='" + cellclass + "'><a href='/mwine/" + Webroutines
										.URLEncode(Webroutines.removeAccents(Knownwines.getKnownWineName(knownwineid)))
										.replaceAll("%2F", "/") + "+" + rs.getString("vintage") + "'>"
										+ (goodpqvintages.contains("*" + rs.getString("vintage") + "*") ? "<strong>"
												: "")
										+ rs.getString("vintage")
										+ (goodpqvintages.contains("*" + rs.getString("vintage") + "*") ? "</strong>"
												: "")
										+ "</a></td>";
							} else {
								html = html + "<td class='" + cellclass + "'>" + rs.getString("vintage") + "</td>";
							}
						}
						html = html + "</tr>";
						for (i = 0; i < author.length; i++) {
							cellclass = "even";
							rs = Dbutil.selectQuery("select * from ratinganalysis where knownwineid=" + knownwineid
									+ " and (vintage>" + startvintage + singlevintageclause + " ) order by vintage;",
									con);
							rs.next();
							currentvintage = 0;
							rs.beforeFirst();
							// html=html+"<tr><td class='author'>"+author[i]+"</td>";
							html = html + "<tr><td class='author'>Rating</td>";
							while (rs.next()) {
								if (currentvintage < rs.getInt("vintage")) {
									if (cellclass.equals("odd")) {
										cellclass = "even";
									} else {
										cellclass = "odd";
									}
									if (currentvintage != 0)
										html += "</td>"; // to skip </td> tag the first time

									html = html + "<td class='" + cellclass + "'>";
									currentvintage = rs.getInt("vintage");
								}
								if (rs.getString("author").equals(author[i])) {
									html = html + formatRating(rs.getDouble("rating"), rs.getString("author"));
									if (!rs.getString("ratinghigh").equals("0")) {
										html = html + "-"
												+ formatRating(rs.getDouble("ratinghigh"), rs.getString("author"));
									}
								}
							}
							html = html + "</td></tr>";

						}
					}
					html = html + "</table>";// <div class=\"note\">Ratings copyright of their respective owners. See <a
												// href='/disclaimer.jsp'>\"Disclaimer\"</a> for details. </div>";

				}
			} catch (Exception E) {
				Dbutil.logger.error("Problem while generating Ratings html", E);
			}
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);

		return html;

	}

	public static String getMobileRatingsHTML(int knownwineid, String searchpage, int singlevintage) {
		StringBuffer html = new StringBuffer();
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		int lastvintage = 0;
		if (knownwineid > 0) {
			try {
				rs = Dbutil.selectQuery(
						"select * from ratedwines where knownwineid=" + knownwineid + " order by vintage desc,author;",
						con);
				while (rs.next()) {
					if (rs.getInt("vintage") != lastvintage) {
						lastvintage = rs.getInt("vintage");
						html.append("<h3 id='rating" + rs.getInt("vintage") + "'>"
								+ (rs.getInt("vintage") > 0 ? rs.getInt("vintage") : "N.V.") + "</h3>");
					}
					html.append(getAuthorName(rs.getString("author")) + ": ");
					html.append(formatRating(rs.getDouble("rating"), rs.getString("author")));
					if (!rs.getString("ratinghigh").equals("0")) {
						html.append("-" + formatRating(rs.getDouble("ratinghigh"), rs.getString("author")));
					}
					html.append(" points for the " + (rs.getInt("vintage") > 0 ? rs.getInt("vintage") : "N.V.") + " "
							+ Spider.escape(rs.getString("name")) + ".<br/>");
				}

			} catch (Exception E) {
				Dbutil.logger.error("Problem while generating Ratings html", E);
			}
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);

		return html.toString();

	}

	public static String formatRating(double rating, String author) {
		String result = "";
		if (author.equals("GR")) {

		} else {
			NumberFormat format = new DecimalFormat("##0.##");
			result = format.format(rating);
		}

		return result;
	}

	public static void deleteAutoWebPages(String id) {
		Dbutil.executeQuery("delete from autowebpage where shopid=" + id + ";");
	}

	public static String getNewMasterHTML(String id) {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String newmaster = "";
		try {
			rs = Dbutil.selectQuery(
					"SELECT * from autoscrapelist where shopid =" + id + " and status like '%NewMaster%';", con);
			if (rs.next()) {
				newmaster = "<TR><TD colspan='2'>Suggested New Master URL: <a href='viewautoshop.jsp?shopid=" + id
						+ "&confirm=newmaster&url=" + rs.getString("url") + "'>" + rs.getString("url")
						+ "</a></TD></TR>";
			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving New Master for shopid " + id, e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return newmaster;
	}

	public static String newMaster(String shopid, String url) {
		String result = "Error saving new master URL";
		url = Spider.SQLEscape(url);
		try {
			Dbutil.executeQuery("delete from autoscrapelist where shopid =" + shopid + " and url='" + url
					+ "' and urltype like 'Spidered%';");
			Dbutil.executeQuery(
					"update autoscrapelist set url='" + url + "' where shopid ='" + shopid + "' and urltype='Master';");
			result = "OK, saved";
		} catch (Exception e) {
			Dbutil.logger.error("Error while setting new Master url for shopid " + shopid, e);
			result = "Error saving new master URL";
		}
		return result;
	}

	public static String getAutoShopOverviewHTML() {
		ArrayList<String> currentconfig;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String html = "<table><TH>Shopname</TH><TH>Status</TH><TH># Wines found</TH><TH>URL</TH><TH>Actions</TH><TH></TH>";
		try {
			rs = Dbutil.selectQuery("SELECT * from autoshops where status not like 'Unsuccesful%';", con);
			while (rs.next()) {
				String status = rs.getString("status");
				currentconfig = Webroutines.getAutoConfig(rs.getString("id"));
				html = html + "<TR><TD><a href='viewautoshop.jsp?shopid=" + rs.getString("id") + "'>"
						+ rs.getString("shopname") + "</a></TD>" + "<TD>" + status + "</TD>" + "<TD>"
						+ currentconfig.get(11) + " from " + currentconfig.get(10) + " (WS)</TD>" + "<TD>"
						+ rs.getString("shopurl") + "</TD>" + "<TD><FORM ACTION=\"autoshopoverview.jsp?shopid="
						+ rs.getString("id")
						+ "&confirm=Delete\" METHOD=\"POST\"  id=\"formDelete\"><TABLE><TR><TD><input type=\"Submit\" name=\"testshop\"  value=\"Delete shop\" ></TD><TD width=\"25%\">Reason:</TD>	<TD><INPUT TYPE=\"TEXT\" NAME=\"reason\" value=\"\"></TD></TR></TABLE></FORM></TD>"
						+ "<TD><FORM ACTION=\"autoshopoverview.jsp?shopid=" + rs.getString("id")
						+ "&confirm=Abort\" METHOD=\"POST\"  id=\"formAbort\"><input type=\"Submit\" name=\"abortshop\"  value=\"Abort spider\" ></FORM></TD>"
						+ "<td><a href='addautodiscovershop.jsp?wsshopid=" + rs.getString("wsshopsid")
						+ "' target='_blank'>Start again completely</a></td>" + "</TR>";
			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving autoshopinfo", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		html = html + "</table>";
		return html;
	}

	public static String getConfigKey(String configkey) {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String key = "";
		try {
			rs = Dbutil.selectQuery("SELECT * from config where configkey='" + configkey + "';", con);
			if (rs.next()) {
				String message = rs.getString("value");
				if (!message.equals(""))
					key = message;
			}

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving system message", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return key;
	}

	public static String getCookieValue(Cookie[] cookies, String cookieName, String defaultValue) {
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookieName.equals(cookie.getName()))
					return (cookie.getValue());
			}
		}
		return (defaultValue);
	}

	public static void getWineListPrices(int tenant, int setid) {
		ResultSet rs = null;
		String query;
		String name;
		Connection con = Dbutil.openNewConnection();

		try {
			query = "select * from materializedadvice limit 1;";
			rs = Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
			Dbutil.closeRs(rs);
			query = "select w.id,min(priceeuroex) as priceeuroex from winelists w join wines ma on (w.knownwineid=ma.knownwineid and w.vintage=ma.vintage and w.size=ma.size) where tenant="
					+ tenant + " and setid=" + setid + " group by ma.knownwineid,ma.size,ma.vintage order by w.id;";
			rs = Dbutil.selectQueryForUpdate(query, con);
			while (rs.next()) {
				Dbutil.executeQuery("update winelists set bestprice=" + rs.getDouble("priceeuroex") + " where tenant="
						+ tenant + " and id=" + rs.getInt("w.id"));
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public static String getWineListHTML(int tenant, int setid) {
		StringBuffer sb = new StringBuffer();
		ResultSet rs = null;
		String query;
		String name;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from winelists where tenant=" + tenant + " and setid=" + setid + " order by id;";
			rs = Dbutil.selectQuery(query, con);
			sb.append(
					"<table><tr><th>Wine</th><th>Recognized as</th><th>Vintage</th><th>Size</th><th>Best Price</th></tr>");
			while (rs.next()) {
				sb.append("<tr><td>" + rs.getString("name") + "</td><td>"
						+ Knownwines.getKnownWineName(rs.getInt("knownwineid")) + "</td><td>" + rs.getString("vintage")
						+ "</td><td>" + Webroutines.formatSize(rs.getFloat("size")) + "</td><td>&euro;&nbsp;"
						+ Webroutines.formatPrice(rs.getDouble("bestprice")) + "</td></tr>");
			}
			sb.append("</table>");
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return sb.toString();
	}

	public static String getTipsHTML(String region, Float size) {
		String html = "";
		ResultSet rs = null;
		String query;
		String name;
		Connection con = Dbutil.openNewConnection();
		try {
			if (!region.equals("")) {
				String regionlist = Region.getRegionsAsIntList(region);
				query = "Select * from tips where region in (" + regionlist + ") order by (lowestprice/nextprice)";
				rs = Dbutil.selectQuery(query, con);
				html = html
						+ "<table><tr><th align='left' width='65%'>Description</th><th  align='left' width='5%'>Vintage</th><th  align='left' width='5%'>Size</th><th  align='right' width='12%'>From</th><th  align='right' width='13%'>Best Price</th></tr>";
				while (rs.next()) {
					name = Dbutil.readValueFromDB("Select * from wines where id=" + rs.getString("wineid"), "name");
					html = html + "<tr>";
					html = html + "<td><a href=\"/index.jsp?name="
							+ Webroutines.URLEncode(Knownwines.getKnownWineName(rs.getInt("Knownwineid"))) + "&vintage="
							+ rs.getString("Vintage") + "\">" + Spider.escape(name) + "</a></td>";
					html = html + "<td>" + rs.getString("Vintage") + "</td>";
					html = html + "<td>" + formatSizecompact(rs.getFloat("Size")) + "</td>";
					html = html + "<td align='right'><s>&euro;&nbsp;" + formatPrice(rs.getDouble("Nextprice"))
							+ "</s></td>";
					html = html + "<td align='right'>&euro;&nbsp;" + formatPrice(rs.getDouble("Lowestprice")) + "</td>";
					html = html + "</tr>";
				}
			}
			html = html + "</table>";
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html;
	}

	/*
	 * public static String getResultHTML(Wine[] wines, Translator t){ String
	 * html=""; int i=0; try{ for (Wine wine:wines){ <table class="results"><tr><th
	 * class="flag"></th><th class="shop"><%=t.get("store")%></th><th
	 * class="name"><%=t.get("wine")%></th><th class="vintage"><% out
	 * .print("<a href='" + ("/" + thispage + "?name=" + Webroutines
	 * .URLEncode(searchdata .getName()) + "&amp;offset=0&amp;order=" + Webroutines
	 * .getOrder(searchdata .getOrder(), "vintage")) .replace("'", "&apos;") + "'>"
	 * + t.get("vintage") + "</a>"); %></th><th class="size"
	 * align="right">&nbsp;&nbsp;&nbsp;<% out .print("<a href='" + ("/" + thispage +
	 * "?name=" + Webroutines .URLEncode(searchdata .getName()) +
	 * "&amp;offset=0&amp;order=" + Webroutines .getOrder(searchdata .getOrder(),
	 * "size")) .replace("'", "&apos;") + "'>" + t.get("size") +
	 * "</a>&nbsp;&nbsp;&nbsp;"); %></th><th class="price" align="right"><% out
	 * .print("<a href='" + ("/" + thispage + "?name=" + Webroutines
	 * .URLEncode(searchdata .getName()) + "&amp;offset=0&amp;order=" + Webroutines
	 * .getOrder(searchdata .getOrder(), "priceeuroin")) .replace("'", "&apos;") +
	 * "'>" + t.get("price") + "</a>"); %></th></tr> <% NumberFormat format = new
	 * DecimalFormat("#,##0.00");
	 * 
	 * if (sponsoredresults) {
	 * 
	 * if (wineset.SponsoredWine != null && wineset.SponsoredWine.length > 0) { out
	 * .print("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>"); for (int i
	 * = 0; i < wineset.SponsoredWine.length; i++) { out.print("<tr"); if (i % 2 ==
	 * 1) out .print(" class=\"sponsoredodd\""); if (i % 2 == 0) out
	 * .print(" class=\"sponsoredeven\""); out.print(">"); out
	 * .print("<td class='flag'><a href='" + thispage + "?name=" + (Webroutines
	 * .URLEncode(searchdata .getName()) + "&amp;country=" + wineset.Wine[i].Country
	 * .toUpperCase() + "&amp;vintage=" + searchdata .getVintage()) .replace("'",
	 * "&apos;") + "' target='_blank'><img src='/images/flags/" +
	 * wineset.SponsoredWine[i].Country .toLowerCase() + ".gif' alt='" +
	 * wineset.SponsoredWine[i].Country .toLowerCase() + "' /></a></td>"); out
	 * .print("<td><a href=" + response .encodeURL("link.jsp?shopid=" +
	 * wineset.SponsoredWine[i].ShopId) + " target='_blank'>" +
	 * wineset.SponsoredWine[i].Shopname + "</a></td>"); out .print("<td><a href=" +
	 * response .encodeURL("link.jsp?wineid=" + wineset.SponsoredWine[i].Id) +
	 * " target='_blank'>" + wineset.SponsoredWine[i].Name + "</a></td>"); out
	 * .print("<td>" + wineset.SponsoredWine[i].Vintage + "</td>"); out
	 * .print("<td align='right'>" + Webroutines
	 * .formatSize(wineset.SponsoredWine[i].Size) + "</td>"); out
	 * .print("<td align='right'>" + Webroutines .formatPrice(
	 * wineset.SponsoredWine[i].PriceEuroIn, wineset.SponsoredWine[i].PriceEuroEx,
	 * searchdata .getCurrency(), searchdata .getVat()) + "</td>");
	 * out.print("</tr>"); } out
	 * .print("<tr><td colspan='4'><i>All results:</i></td></tr>");
	 * 
	 * } else { out
	 * .print("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
	 * out.print("<tr class=\"sponsoredeven\">"); out
	 * .print("<td colspan='2'><a href='" + response .encodeURL("/sponsoring.jsp") +
	 * "'></a></td>"); out .print("<td colspan='2'><a href='" + response
	 * .encodeURL("/sponsoring.jsp") +
	 * "'>Your wine could be listed here! Click for more information.</a></td>");
	 * out.print("<td align='right'></td>"); out
	 * .print("<td align='right'>&euro; 0.10</td>"); out.print("</tr>"); out
	 * .print("<tr><td colspan='4'><i>All results:</i></td></tr>"); } }
	 * 
	 * // Give the complete result list for (int i = 0; i < wineset.Wine.length;
	 * i++) { out.print("<tr"); if (wineset.Wine[i].CPC > 0 && sponsoredresults) {
	 * out.print(" class=\"sponsoredeven\""); } else { if (i % 2 == 1) {
	 * out.print(" class=\"odd\""); } } out.print(">"); out
	 * .print("<td class='flag'><a href='" + thispage + "?name=" + (Webroutines
	 * .URLEncode(searchdata .getName()) + "&amp;country=" + wineset.Wine[i].Country
	 * .toUpperCase() + "&amp;vintage=" + searchdata .getVintage()).replace( "'",
	 * "&apos;") + "' target='_blank'><img src='/images/flags/" +
	 * wineset.Wine[i].Country .toLowerCase() + ".gif' alt='" +
	 * wineset.Wine[i].Country .toLowerCase() + "' /></a></td>");
	 * out.print("<td><a href='/link.jsp?wineid=" + wineset.Wine[i].Id +
	 * "&amp;shopid=" + wineset.Wine[i].ShopId + "' target='_blank' title='" +
	 * wineset.Wine[i].Shopname.replace("&", "&amp;").replace("'", "&apos;") + "'>"
	 * + wineset.Wine[i].Shopname.replace("&", "&amp;") + "</a></td>"); out
	 * .print("<td><a href='/link.jsp?wineid=" + wineset.Wine[i].Id + "' title='" +
	 * Spider .escape( Webroutines .formatCapitals(wineset.Wine[i].Name))
	 * .replace("'", "&apos;") + "' target='_blank'>" + Spider .escape(Webroutines
	 * .formatCapitals(wineset.Wine[i].Name)) + "</a></td>"); out .print("<td>" +
	 * (wineset.Wine[i].Vintage .equals("0") ? "" : wineset.Wine[i].Vintage) +
	 * "</td>"); out .print("<td align='right'>" + Webroutines
	 * .formatSize(wineset.Wine[i].Size) + "</td>");
	 * out.print("<td class='price' align='right'>" + Webroutines.formatPrice(
	 * wineset.Wine[i].PriceEuroIn, wineset.Wine[i].PriceEuroEx,
	 * searchdata.getCurrency(), searchdata.getVat()) + "</td>");
	 * out.print("</tr>"); } %> </table> } } catch (Exception exc){
	 * Dbutil.logger.error("Error while getting wine HTML.",exc); html=""; }
	 * 
	 * 
	 * return html; }
	 */

	public static String getTipsHTML(String region, Float size, int limit, String searchpage,
			Translator.languages language) {
		String html = "";
		ResultSet rs = null;
		String query;
		String name;
		String regionwhereclause = "";
		Connection con = Dbutil.openNewConnection();
		try {
			if (!region.equals("")) {
				regionwhereclause = " where region in (" + Region.getRegionsAsIntList(region) + ")";
			}
			query = "Select tips.*, knownwines.appellation from tips join knownwines on (tips.knownwineid=knownwines.id) "
					+ regionwhereclause + " where (lowestprice/nextprice>0.5) order by (lowestprice/nextprice) limit "
					+ limit + ";";
			rs = Dbutil.selectQuery(query, con);
			int i = 1;
			while (rs.next()) {
				String vintage = rs.getString("Vintage") + " ";
				if (vintage.equals(" "))
					vintage = "";
				name = Dbutil.readValueFromDB("Select * from wines where id=" + rs.getString("wineid"), "name");
				html = html + "<table class='tips'>";
				html = html + "<tr";
				if (i % 2 == 1)
					html = html + " class=\"odd\"";
				html = html + ">";
				html = html + "<td class='name' colspan='2'><a href=\"/mwine/"
						+ Webroutines.URLEncode(
								Webroutines.removeAccents(Knownwines.getKnownWineName(rs.getInt("Knownwineid"))))
						+ "+" + rs.getString("Vintage") + "?tip=true\">" + vintage
						+ Knownwines.getKnownWineName(rs.getInt("Knownwineid")).replaceAll("&", "&amp;") + "</a></td>";
				html = html + "<td class='vintage'></td>";
				html = html + "</tr></table>";
				html = html + "<table class='tips'>";
				html = html + "<tr";
				if (i % 2 == 1)
					html = html + " class=\"odd\"";
				html = html + ">";
				html = html + "<td>" + (rs.getString("appellation")) + "</td>";
				html = html + "<td class='size'>" + formatSizecompact(rs.getFloat("Size")) + "</td>";
				html = html + "<td class='oldprice'>&euro;&nbsp;" + formatPrice(rs.getDouble("Nextprice")) + "</td>";
				html = html + "<td class='price'>&euro;&nbsp;" + formatPrice(rs.getDouble("Lowestprice")) + "</td>";
				html = html + "</tr></table>";
				i++;

			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
			html = "";
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		// if (html.equals("")) html="<br />"+new Translator(language).get("notips");

		return html;
	}

	public static String getTipsHTML2(String region, Float size, int limit, String searchpage,
			Translator.languages language, Searchdata searchdata) {
		String html = "";
		ResultSet rs = null;
		String query;
		String name;
		String regionwhereclause = "";
		Connection con = Dbutil.openNewConnection();
		try {
			if (!region.equals("")) {
				regionwhereclause = " where region in (" + Region.getRegionsAsIntList(region) + ")";
			}
			query = "Select tips.*, knownwines.appellation from tips join knownwines on (tips.knownwineid=knownwines.id) "
					+ regionwhereclause + " where (lowestprice/nextprice>0.5) order by (lowestprice/nextprice) limit "
					+ limit + ";";
			rs = Dbutil.selectQuery(query, con);
			int i = 1;
			html += "<div class=\"clear\" >&nbsp;</div>";
			while (rs.next()) {
				String[] type = Knownwines.getIcon(rs.getInt("Knownwineid"), 60);
				int rating = Winerating.getRating(rs.getInt("knownwineid"), rs.getInt("vintage"));
				html += "<div class=\"spacer\"><div class=\"wineinfo\">";
				html += "<div class=\"top\">";
				html += "<div class=\"winename\"><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'>" + Knownwines.getKnownWineName(rs.getInt("knownwineid")) + "</a></div>";
				if (!"Unknown".equals(rs.getString("appellation")))
					html += "<div class=\"appellation\">" + rs.getString("appellation") + "</div>";
				html += "<div class=\"vintage\">" + rs.getString("Vintage") + "</div>";
				html += "</div>";
				html += "<div class=\"right\">";
				html += "<div class='score'>";
				if (rating > 0) {
					html += "<div class='ratingbox'><div class='rating' style='background:#"
							+ Integer.toHexString(0x100 | Math.min(255, (Math.round((100 - rating) * 256 / 10))))
									.substring(1)
							+ Integer.toHexString(0x100 | Math.min(255, (Math.round((rating - 75) * 256 / 15))))
									.substring(1)
							+ "00;'>" + (rating > 0 ? rating : "") + "</div></div>";
				} else {
					html += "&nbsp;";
				}
				html += "</div>";
				html += "<div class=\"typeimg\"><img src=\"/images/" + type[0] + "\" alt=\"" + type[1] + "\"/></div>";
				html += "<div class=\"typetext\">" + type[1] + "</div>";
				html += "</div>";
				html += "<div class=\"bottom\">";
				html += "<div class=\"oldprice\">"
						+ Webroutines.formatPrice(rs.getDouble("nextprice"), (double) 0, searchdata.getCurrency(), "IN")
						+ "</div>";
				html += "<div class=\"price\">" + Webroutines.formatPrice(rs.getDouble("lowestprice"), (double) 0,
						searchdata.getCurrency(), "IN") + "</div>";
				html += "<div class=\"buttons\"><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "' target=\"_blank\"><input type=\"button\" class=\"find\"  value=\"Find prices\"/></a>";
				html += "<input type=\"button\" class=\"gettn\" onmouseout=\"javascript:hide('tn" + i
						+ "');\" onclick=\"javascript:showTN(" + rs.getInt("knownwineid") + "," + rs.getInt("vintage")
						+ ",'tn" + i + "');\" value=\"Tasting Notes\"/></div>";
				html += "</div>";
				html += "</div>";
				html += "<div class=\"clear\"  onmouseout=\"javascript:hide('tn" + i
						+ "');\" onmouseover=\"javascript:showTN(" + rs.getInt("knownwineid") + ","
						+ rs.getInt("vintage") + ",'tn" + i + "');\"><div class=\"advicetastingnote\" id=\"tn" + i
						+ "\"  onmouseout=\"javascript:hide('tn" + i + "');\" onmouseover=\"javascript:showTN("
						+ rs.getInt("knownwineid") + "," + rs.getInt("vintage") + ",'tn" + i
						+ "');\"><div class=\"tncontent\" id=\"tn" + i + "content\"  onmouseout=\"javascript:hide('tn"
						+ i + "');\" onmouseover=\"javascript:showTN(" + rs.getInt("knownwineid") + ","
						+ rs.getInt("vintage") + ",'tn" + i + "');\"></div></div></div>";
				html += "</div>";
				i++;
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
			html = "";
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		// if (html.equals("")) html="<br />"+new Translator(language).get("notips");

		return html;
	}

	// Used for new design
	public static String getoldTipsHTML3(String region, Float size, int limit, String searchpage,
			Translator.languages language, Searchdata searchdata) {
		String html = "";
		ResultSet rs = null;
		String query;
		String name;
		String regionwhereclause = "";
		Connection con = Dbutil.openNewConnection();
		try {
			if (!region.equals("")) {
				regionwhereclause = " where region in (" + Region.getRegionsAsIntList(region) + ")";
			}
			query = "Select tips.*, knownwines.appellation from tips join knownwines on (tips.knownwineid=knownwines.id) "
					+ regionwhereclause
					+ " where (lowestprice/nextprice>0.5) and lowestprice<=100 and size=0.75 order by (lowestprice/nextprice) limit "
					+ limit + ";";
			rs = Dbutil.selectQuery(query, con);
			int i = 1;
			while (rs.next()) {
				String[] type = Knownwines.getGlassImage(rs.getInt("Knownwineid"));
				int rating = Winerating.getRating(rs.getInt("knownwineid"), rs.getInt("vintage"));
				html += "<div class='infobox' >";
				html += "<div class='infobox" + (rating < 50 ? "no" : "") + "rating' >";
				html += "<a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'><img class='glassimg' src='/css/" + type[0] + "' alt='" + type[1] + "'/></a>";
				html += "<div class='winename'><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'>" + Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage")
						+ "</a>";
				html += "<div class='appellation'><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'>" + ("Unknown" != rs.getString("appellation") ? rs.getString("appellation") : "")
						+ "</a></div></div>";
				html += "<div class='clearboth'></div>";
				html += "<div class='wineinfoscore' ><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'>" + (rating > 50 ? rating : "") + "</a></div>";
				html += "<div class='wineinfooldprice' ><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'>"
						+ Webroutines.formatPrice(rs.getDouble("nextprice"), (double) 0, searchdata.getCurrency(), "IN")
						+ "</a></div>";
				html += "<div class='wineinfoprice' ><a href='/wine/"
						+ Webroutines.URLEncode(
								Knownwines.getKnownWineName(rs.getInt("knownwineid")) + " " + rs.getString("Vintage"))
						+ "'>" + Webroutines.formatPrice(rs.getDouble("lowestprice"), (double) 0,
								searchdata.getCurrency(), "IN")
						+ "</a></div>";
				html += "</div></div>";

				// html+="<div
				// class=\"oldprice\">"+Webroutines.formatPrice(rs.getDouble("nextprice"),(double)0,searchdata.getCurrency(),"IN")+"</div>";
				// html+="<div class=\"buttons\"><a
				// href='/wine/"+Webroutines.URLEncode(Knownwines.getKnownWineName(rs.getInt("knownwineid"))+"
				// "+rs.getString("Vintage"))+"' target=\"_blank\"><input type=\"button\"
				// class=\"find\" value=\"Find prices\"/></a>";
				// html+="<input type=\"button\" class=\"gettn\"
				// onmouseout=\"javascript:hide('tn"+i+"');\"
				// onclick=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"
				// value=\"Tasting Notes\"/></div>";
				// html+="<div class=\"clear\" onmouseout=\"javascript:hide('tn"+i+"');\"
				// onmouseover=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"><div
				// class=\"advicetastingnote\" id=\"tn"+i+"\"
				// onmouseout=\"javascript:hide('tn"+i+"');\"
				// onmouseover=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"><div
				// class=\"tncontent\" id=\"tn"+i+"content\"
				// onmouseout=\"javascript:hide('tn"+i+"');\"
				// onmouseover=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"></div></div></div>";
				// html+="</div>";
				i++;
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
			html = "";
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		// if (html.equals("")) html="<br />"+new Translator(language).get("notips");

		return html;
	}

	public static String getCountries(String countrycode) {
		if ("EU".equals(countrycode))
			return "AT','BE','CZ','CY','DK','EE','FI','FR','DE','GR','H','IE','IT','LV','LT','LU','MT','NL','PL','PT','SK','SI','ES','SE','GB','CH','BG";
		if ("UC".equals(countrycode))
			return "US','CA";
		if ("AP".equals(countrycode))
			return "AU";
		return countrycode;
	}

	public static String getRegion(String countrycode) {
		if (countrycode == null)
			return "";
		if ("AT','BE','CZ','CY','DK','EE','FI','FR','DE','GR','H','IE','IT','LV','LT','LU','MT','NL','PL','PT','SK','SI','ES','SE','GB','CH','BG',EU"
				.contains(countrycode))
			return "EU";
		if ("US,CA,UC".contains(countrycode))
			return "UC";
		// if ("AP".contains(countrycode)) return "AP";
		return "";

	}

	// Used for new design with new colors
	public static String getTipsHTML3(String region, int limit, Searchdata searchdata) {
		String html = "";
		ResultSet rs = null;
		String query;
		String name;
		String uniquename = "";
		String link;
		String regionwhereclause = "";
		String countryorderclause = "";
		Connection con = Dbutil.openNewConnection();
		try {
			if (!region.equals("")) {
				regionwhereclause = " and region in (" + Region.getRegionsAsIntList(region) + ")";
			}
			if (!searchdata.getCountry().equals("") && !searchdata.getCountry().equals("All")) {
				countryorderclause = ", (country in ('" + getCountries(searchdata.getCountry()) + "','"
						+ getCountries(getRegion(searchdata.getCountry())) + "')) desc";
			}
			query = "Select tips.*, knownwines.appellation, typeid,country from tips join knownwines on (tips.knownwineid=knownwines.id)  join wineview on (tips.wineid=wineview.id) natural left join winetypecoding where (lowestprice/nextprice>0.5) and lowestprice<=100 and tips.size=0.75 "
					+ regionwhereclause + " order by (country='" + searchdata.getCountry() + "') desc"
					+ countryorderclause + ",(lowestprice/nextprice) limit " + limit + ";";
			rs = Dbutil.selectQuery(query, con);
			int i = 1;
			while (rs.next()) {
				name = Knownwines.getKnownWineName(rs.getInt("knownwineid"));
				uniquename = Knownwines.getUniqueKnownWineName(rs.getInt("knownwineid"));
				link = winelink(uniquename, rs.getInt("Vintage"));
				String[] type = Knownwines.getGlassImageFromWineTypeCode(rs.getInt("typeid"));
				int rating = Winerating.getRating(rs.getInt("knownwineid"), rs.getInt("vintage"));
				html += "<div class='infobox' >";
				html += "<div class='infobox" + (rating < 50 ? "no" : "") + "rating' >";
				html += "<a href='" + link + "'><img class='glassimg' src='" + Configuration.cdnprefix + "/css2/"
						+ type[0] + "' alt='" + type[1] + "'/></a>";// to do sprite gebruiken, maar sprite glas images
																	// zijn te donker voor de tips
				html += "<div class='winename'><a href='" + link + "'>" + name.replaceAll("&", "&amp;") + " "
						+ rs.getString("Vintage") + "</a>";
				html += "<div class='appellation'><a href='" + link + "'>"
						+ ("Unknown" != rs.getString("appellation") ? rs.getString("appellation") : "")
						+ "</a></div></div>";
				html += "<div class='clearboth'></div>";
				html += "<div class='wineinfoscore' ><a href='" + link + "'>" + (rating > 50 ? rating : "")
						+ "</a></div>";
				html += "<div class='wineinfooldprice' ><a href='" + link + "'>"
						+ Webroutines.formatPrice(rs.getDouble("nextprice"), (double) 0, searchdata.getCurrency(), "IN")
						+ "</a></div>";
				html += "<div class='wineinfoprice' ><img src='" + Configuration.cdnprefix
						+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
						+ rs.getString("country").toLowerCase() + "'/><a href='" + link + "'>" + Webroutines
								.formatPrice(rs.getDouble("lowestprice"), (double) 0, searchdata.getCurrency(), "IN")
						+ "</a></div>";
				html += "</div></div>";

				// html+="<div
				// class=\"oldprice\">"+Webroutines.formatPrice(rs.getDouble("nextprice"),(double)0,searchdata.getCurrency(),"IN")+"</div>";
				// html+="<div class=\"buttons\"><a
				// href='/wine/"+Webroutines.URLEncode(Knownwines.getKnownWineName(rs.getInt("knownwineid"))+"
				// "+rs.getString("Vintage"))+"' target=\"_blank\"><input type=\"button\"
				// class=\"find\" value=\"Find prices\"/></a>";
				// html+="<input type=\"button\" class=\"gettn\"
				// onmouseout=\"javascript:hide('tn"+i+"');\"
				// onclick=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"
				// value=\"Tasting Notes\"/></div>";
				// html+="<div class=\"clear\" onmouseout=\"javascript:hide('tn"+i+"');\"
				// onmouseover=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"><div
				// class=\"advicetastingnote\" id=\"tn"+i+"\"
				// onmouseout=\"javascript:hide('tn"+i+"');\"
				// onmouseover=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"><div
				// class=\"tncontent\" id=\"tn"+i+"content\"
				// onmouseout=\"javascript:hide('tn"+i+"');\"
				// onmouseover=\"javascript:showTN("+rs.getInt("knownwineid")+","+rs.getInt("vintage")+",'tn"+i+"');\"></div></div></div>";
				// html+="</div>";
				i++;
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
			html = "";
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		// if (html.equals("")) html="<br />"+new Translator(language).get("notips");

		return html;
	}

	public static String URLEncode(String input) {
		if (input == null)
			return "";
		String result = input;
		try {
			result = java.net.URLEncoder.encode(input, "ISO-8859-1");
		} catch (Exception e) {
			Dbutil.logger.error("Error URLencoding input " + input, e);
		}
		return result;
	}

	public static String URLEncodeUTF8(String input) {
		String result = input;
		try {
			result = java.net.URLEncoder.encode(input, "UTF-8");
		} catch (Exception e) {
			Dbutil.logger.error("Error: ", e);
		}
		return result;
	}

	public static String removeAccents(String input) {
		if (input == null)
			return "";
		try {
			return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		} catch (Exception e) {
		}
		return input;
	}

	public static String URLEncodeUTF8Normalized(String input) {
		if (input == null)
			return "";
		String result = removeAccents(input);
		try {
			result = java.net.URLEncoder.encode(result, "UTF-8");
		} catch (Exception e) {
			Dbutil.logger.error("Error encoding " + input, e);
			Thread.dumpStack();
		}
		return result;
	}

	public static String URLDecodeUTF8(String input) {
		String result = input;
		try {
			result = java.net.URLDecoder.decode(input, "UTF-8");
		} catch (Exception e) {
			Dbutil.logger.error("Error: ", e);
		}
		return result;
	}

	public static String URLDecodeUTF8(String input, String encoding) {
		String result = input;
		try {
			result = java.net.URLDecoder.decode(input, encoding);
		} catch (Exception e) {
			Dbutil.logger.error("Error: ", e);
		}
		return result;
	}

	public static String getRegexPatternValue(String regex, String target) {
		Pattern pattern;
		Matcher matcher;
		String result = "";
		try {
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(target);
			if (matcher.find()) {
				result = matcher.group(1);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not retrieve regex value from pattern. ", e);
		}
		return result;
	}

	public static String getRegexPatternValue(String regex, String target, int flags) {
		Pattern pattern;
		Matcher matcher;
		String result = "";
		try {
			pattern = Pattern.compile(regex, flags);
			matcher = pattern.matcher(target);
			if (matcher.find()) {
				result = matcher.group(1);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not retrieve regex value from pattern. ", e);
		}
		return result;
	}

	public static String getAuditLoggingHTML(int history) {
		String html = "<h3>Audit logging</h3><table>";
		ResultSet rs = null;
		String historywhereclause = "";
		String query;
		String name;
		String regionwhereclause = "";
		Connection con = Dbutil.openNewConnection();
		try {
			long longtime = new java.util.Date().getTime();
			longtime = longtime - (long) history * 1000 * 3600 * 24;
			Timestamp date = new Timestamp(longtime);
			if (history > 0)
				historywhereclause = " where date>'" + date.toString() + "'";
			query = "select * from auditlogging " + historywhereclause + " order by date desc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				html += "<tr><td>" + rs.getString("date") + "</td>";
				html += "<td>" + Webroutines.getPartnerNameFromPartnerId(rs.getInt("partnerid")) + "</td>";
				html += "<td>" + rs.getString("userid") + "</td>";
				html += "<td>" + rs.getString("objecttype") + " " + rs.getString("objectid") + "</td>";
				html += "<td>" + rs.getString("action") + "</td>";
				html += "<td>" + rs.getString("info") + "</td></tr>";
			}
			html += "</table>";
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
			html = "";
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html;
	}

	public static String EscapeHTML(String Page) {
		Page = Page.replaceAll("<", "&lt;");
		Page = Page.replaceAll(">", "&gt;");
		Page = Page.replaceAll("&lt;br&gt;", "<br>");
		Page = Page.replaceAll("&lt;br/&gt;", "<br/>");
		return Page;
	}

	public static String getLabels(int knownwineid) {
		String html = "";
		File dir;

		try {
			dir = new File("C:\\labels\\" + knownwineid);
			if (dir.exists()) {
				File[] list = dir.listFiles();
				html += "<br/>" + Knownwines.getKnownWineName(knownwineid) + "<br/>";
				for (File file : list) {
					html += "<img src='/labels/" + knownwineid + "/" + file.getName() + "'/>";
				}
			}

		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting tips HTML.", exc);
			html = "";
		}
		return html;
	}

	public static String getLabel(int knownwineid) {

		try {
			if (new File(
					Configuration.workspacedir + "labels" + System.getProperty("file.separator") + knownwineid + ".jpg")
							.exists()) {
				return Configuration.staticprefix + "/labels/" + knownwineid + ".jpg";
			} else if (new File(
					Configuration.workspacedir + "labels" + System.getProperty("file.separator") + knownwineid + ".gif")
							.exists()) {
				return Configuration.staticprefix + "/labels/" + knownwineid + ".gif";

			}

		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting label.", exc);
		}
		return "";
	}

	public static int[] getKnownwineids(String name) {
		ResultSet rs = null;
		int[] list = new int[0];
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("select * from knownwines where wine like '%" + Spider.SQLEscape(name) + "%';",
					con);
			if (rs.last()) {
				list = new int[rs.getRow()];
				rs.beforeFirst();
				int i = 0;
				while (rs.next()) {
					list[i] = rs.getInt("id");
					i++;
				}
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while getting knownwineid list", exc);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return list;
	}

	public static String showCaptchaHTML(HttpServletRequest request) {
		return "<div class=\"g-recaptcha\" data-sitekey=\"" + Configuration.recaptchasitekey + "\"></div>";
	}

	/**
	 * @param request
	 * @return result: 0=unsuccessful 1=successful 2=No response received
	 */
	public static int checkCaptcha(HttpServletRequest request) {
		int result = 2; // No response received
		// System.out.println(request.getParameter("g-recaptcha-response"));
		String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
		if (gRecaptchaResponse == null || gRecaptchaResponse.length() == 0) {
			return result;
		}
		try {
			URL verifyUrl = new URL(Configuration.recaptchasiteverifyurl);

			// Open a Connection to URL above.
			HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();

			// Add the Header informations to the Request to prepare send to the server.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			// Data will be sent to the server.
			String postParams = "secret=" + Configuration.recaptchasecretkey //
					+ "&response=" + gRecaptchaResponse;

			// Send Request
			conn.setDoOutput(true);

			// Get the output stream of Connection.
			// Write data in this stream, which means to send data to Server.
			OutputStream outStream = conn.getOutputStream();
			outStream.write(postParams.getBytes());

			outStream.flush();
			outStream.close();

			// Response code return from Server.
			int responseCode = conn.getResponseCode();
			// System.out.println("responseCode=" + responseCode);

			// Get the Input Stream of Connection to read data sent from the Server.
			InputStream is = conn.getInputStream();

			JsonReader jsonReader = Json.createReader(is);
			JsonObject jsonObject = jsonReader.readObject();
			jsonReader.close();

			// ==> {"success": true}
			// System.out.println("Response: " + jsonObject);

			boolean success = jsonObject.getBoolean("success");
			result = success ? 1 : 0;
		} catch (Exception e) {
			e.printStackTrace();
			result = 0;
		}
		return result;
	}

	public static String getMostCommonUrl(int shopid, String auto) {
		return Dbutil.readValueFromDB(
				"SELECT * from " + auto + "scrapelist where Shopid=" + shopid
						+ " and (urltype like '%Spidered%' or urltype like '%Fixed%') order by winesfound desc,id;",
				"url");
	}

	public static String getMostCommonPostdata(int shopid, String auto) {
		return Dbutil.readValueFromDB(
				"SELECT * from " + auto + "scrapelist where Shopid=" + shopid
						+ " and (urltype like '%Spidered%' or urltype like '%Fixed%') order by winesfound desc,id;",
				"postdata");
	}

	public static String escape(String source) {
		if (source == null)
			return null;

		StringBuffer result = new StringBuffer(source.length());

		for (int i = 0; i < source.length(); i++) {

			char ch = source.charAt(i);

			if ((int) ch > 127) {
				result.append("&#").append((int) ch).append(';');
			} else {
				if ((int) ch == 38) {
					result.append("&amp;");
				} else {
					if ((int) ch == 60) {
						result.append("&lt;");
					} else {
						if ((int) ch == 62) {
							result.append("&gt;");
						} else {
							result.append(ch);
						}
					}
				}
			}
		}
		return result.toString();
	}

	public static String getWineResultsHTML(Wineset wineset, Translator t, Searchdata searchdata, int numberofrows,
			HttpServletResponse response, String map, boolean sponsoredresults, Wineset sponsoredwineset,
			String thispage) {
		return getWineResultsHTML(wineset, t, searchdata, numberofrows, response, map, sponsoredresults,
				sponsoredwineset, thispage, false, false);
	}

	public static String ActivityMonitor(int workinginterval) {
		String totals = query2table(
				"select count(*) as Total,sum(if(wsshops.shopid=0,1,0)) as ToDo,sum(if(succes<7 and winestotal>0,1,0 and !shops.disabled)) as OK,sum(if(succes<7 and winestotal=0,1,0 and shops.disabled=0)) as EmptyStores,sum(if(succes>=7 and !shops.disabled,1,0)) as Onderhoud,sum(succes>=7 and shops.disabled) as Disabled from wsshops left join shops on (wsshops.shopid=shops.id) left join shopstats on (wsshops.shopid=shopstats.shopid) where storetype='retail' and numberofwines>50 ;",
				true, false);
		String problems = query2table(
				"select count(*) as Total,sum(if(succes<7 and winestotal>0,1,0 and !shops.disabled)) as OK,sum(if(succes<7 and winestotal=0 and shops.disabled=0 and actueel is null,1,0)) as EmptyStores,sum(status=1) as `Actie NL`,sum(status=2) as `Actie China`,sum(if(succes>=7 and !shops.disabled and actueel is null,1,0)) as Onderhoud,sum(shops.disabled) as Disabled from shops left join shopstats on (shops.id=shopstats.shopid) left join issuelog on (issuelog.shopid=shops.id and actueel=1) ;",
				true, false);
		String hours = query2table("select date(date) as week,userid,count(*)*" + workinginterval
				+ " as minutes, round(count(*)*" + workinginterval
				+ "/60/5*10)/10 as hrsperday from (select date,userid,sessionid from auditlogging where userid in ("
				+ Configuration.chinaaccounts
				+ ") and sessionid!='' and date>date_sub(sysdate(),INTERVAL 161 DAY) group by round(UNIX_TIMESTAMP(date)/60/"
				+ workinginterval + "),sessionid) sel group by week(date),userid order by date(date) desc;", true,
				false);
		String query = "select actiondate as date,sel.userids,sessionids,sel.shop as shopid,newshop as `new`, shopname,if (winesunique>0,lastknowngooddays,-1) as ok, concat('<a href=\\'/moderator/issuelog.jsp?shopid=',sel.shop,'\\' target=\\'_blank\\'>',if(max(actueel)>0,'Yes',if(max(status)>2,'---','')),'</a>') as cmt,actions from (select date as datetm,date(date) as actiondate,(0+shopid) as shop,group_concat(distinct(auditlogging.userid)) as userids,group_concat(distinct(left(sessionid,4))) as sessionids,group_concat(distinct(action)) like '%Save new shop%' as newshop,group_concat(distinct(action)) as actions from auditlogging where date>date_sub(sysdate(),interval 31 day) group by actiondate,shopid) sel left join shopstats on (sel.shop=shopstats.shopid) join shops on (shops.id=sel.shop)  left join issuelog on (sel.shop=issuelog.shopid) group by sel.actiondate,sel.shop  order by actiondate desc,sel.shop desc;";
		return "<br/><br/>Wine Searcher Stores Retail, > 50 wines:<br/>" + totals
				+ "<br/><br/>Store status in Vinopedia:<br/>" + problems + "<br/>Working hours China:<br/>" + hours
				+ "<br/><br/>" + query2table(query, true, false);

	}

	public static Integer saveShopAsPartner(int shopid) {
		int partnerid = 0;
		if (shopid > 0) {
			partnerid = Dbutil.readIntValueFromDB("select * from partners where shopid=" + shopid, "id");
			if (partnerid == 0) {
				Dbutil.executeQuery(
						"insert into partners(name,shopid,email,address,adenabled) select shopname,id,email,address,1 from shops where id="
								+ shopid);
				partnerid = Dbutil.readIntValueFromDB("select * from partners where shopid=" + shopid, "id");
			}
		}
		return partnerid;
	}

	public static Map<Integer, String> getPartners() {
		Map<Integer, String> partners = new LinkedHashMap<Integer, String>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select partners.*,partners.id as partnerid,if (shops.shopname is null, name,shopname) as thename from partners left join shops on (partners.shopid=shops.id) order by thename; ";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				partners.put(rs.getInt("partnerid"), rs.getString("thename"));

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return partners;
	}

	public static Map<Integer, String> getShopsWithoutPartnerId() {
		Map<Integer, String> shops = new LinkedHashMap<Integer, String>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select shops.*, partners.id as partnerid from shops left join partners on (partners.shopid=shops.id) where disabled=0 and shoptype=1 having partnerid is null order by shopname; ";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				shops.put(rs.getInt("id"), rs.getString("shopname"));

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return shops;
	}

	public static int getMessageNumber() {
		int n = 0;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from config where configkey='messagecounter'";
			rs = Dbutil.selectQueryForUpdate(query, con);
			if (rs.next()) {
				n = rs.getInt("value");
				rs.updateInt("value", n + 1);
				rs.updateRow();
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return n;
	}

	public static String lastshopissue(int shopid) {
		String html = "";
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from issuelog where shopid=" + shopid + " order by date desc, id desc limit 1;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				html += "Status: ";
				switch (rs.getInt("status")) {
				case 0:
					html += "Issue resolved";
					break;
				case 1:
					html += "Action/question for Netherlands";
					break;
				case 2:
					html += "Action/question for China";
					break;
				case 3:
					html += "Problem cannot be solved";
					break;
				}
				html += ". Set by " + rs.getString("userid") + " on " + rs.getString("date")
						+ "<br/><font style='color:#4d0027'>" + Spider.escape(rs.getString("message")) + "</font>";
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return html;
	}

	public static String StoresToDoHtml(String country, boolean newest, int minwines, String type) {
		StringBuffer sb = new StringBuffer();
		String query;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		Connection con2 = Dbutil.openNewConnection();
		String url = Dbutil.readValueFromDB("select * from config where configkey='todourl'", "value");
		try {
			String countryclause = "";
			if (country != null && country.length() > 0)
				countryclause = "wsshops.country in ('" + Spider.SQLEscape(country) + "') desc,";
			String typeclause = "";
			if (type != null && type.length() > 0)
				typeclause = " and storetype='" + Spider.SQLEscape(type) + "' ";
			String newestclause = "";
			if (newest)
				countryclause = "wsid desc,";
			query = "Select wsshops.* from wsshops join vat on (wsshops.countrycode=vat.countrycode) where haswines=1 and  shopid=0 and url!='' and url not like 'http://www.wineaccess.com%' and url not like 'http://www.totalwine.com%' "
					+ typeclause + " and numberofwines>" + minwines + " order by " + newestclause + countryclause
					+ " numberofwines desc,wsid desc limit 20;";
			rs = Dbutil.selectQuery(rs, query, con);
			sb.append(
					"<table><tr><th style='text-align:left;'>Name (url)</th><th>Wines</th><th>Country</th><th style='text-align:left;'>Similar shops</th><tr>");
			while (rs.next()) {
				String similar = "";
				String input = removeAccents(filterUserInput(rs.getString("name")));
				input = Spider.replaceString(input, "'", " ");
				input = Spider.replaceString(input, "(", " ");
				input = Spider.replaceString(input, ")", " ");
				input = Spider.replaceString(input, ".", " ");
				input = Spider.replaceString(input, ",", " ");
				input = Spider.replaceString(input, "&", " ");
				input = Spider.replaceString(input, "'", " ");
				input = Spider.replaceString(input, "-", " ");
				input = Spider.replaceString(input, "\"", " ");
				input = Spider.replaceString(input, ";", " ");
				input = Spider.replaceString(input, "/", " ");
				input = Spider.replaceString(input, "@", " ");
				input = Spider.replaceString(input, "%", " ");
				rs2 = Dbutil.selectQuery(rs2,
						"Select * from shops where match(shopname) against ('+" + (input.replaceAll("\\s+", "* "))
								+ "*' in boolean mode) order by match(shopname) against ('+"
								+ (input.replaceAll("\\s+", "* ")) + "*' in boolean mode) desc limit 5 ;",
						con2);
				while (rs2.next()) {
					similar += rs2.getString("shopname") + " (" + rs2.getString("shopurl") + ")<form action='" + url
							+ "' method='post'><input type='hidden' name='wsid' value='" + rs.getInt("wsid")
							+ "'/><input type='hidden' name='shopid' value='" + rs2.getInt("id")
							+ "'/><input type='submit' value='Same shop'/></form> ";
				}
				String shopurl = rs.getString("url").replaceAll("https?://", "");
				if (shopurl.contains("/"))
					shopurl = shopurl.substring(0, shopurl.indexOf("/"));
				Dbutil.closeRs(rs2);
				sb.append("<tr><td><a href='editshop.jsp?wsid=" + rs.getString("wsid") + "'>" + rs.getString("name")
						+ " (" + shopurl + ")</a></td><td>" + rs.getString("numberofwines") + "</td><td>"
						+ rs.getString("country") + "</td><td>" + similar + "</td></tr>");
			}
			sb.append("</table>");

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();
	}

	public static String sponsorships() {
		StringBuffer sb = new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from shops join shopstats on (shopid=shops.id) where shops.disabled=0 order by costperclick=0, linkbackok,email='',shopstats.winesunique desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			sb.append(
					"<table><tr><th style='text-align:left;'>Store</th><th>Sponsorship</th><th style='text-align:left;'>Link back</th><th style='text-align:left;'>Last spotted</th><th style='text-align:left;'>Requested</th></tr>");
			while (rs.next()) {

				sb.append("<tr><td><a href='/moderator/editshop.jsp?actie=retrieve&amp;shopid=" + rs.getString("id")
						+ "' target='_blank'>" + (rs.getFloat("costperclick") > 0 ? "<i>Sponsor: " : "")
						+ rs.getString("shopname") + (rs.getFloat("costperclick") > 0 ? "</i>" : "") + "</a></td><td>");
				sb.append("<form autocomplete='off' id='shops" + rs.getString("id") + "costperclick'>"
						+ "<input type='hidden' name='content' value='"
						+ (rs.getFloat("costperclick") > 0.1 ? "0" : "1") + "'/>"
						+ "<input type='hidden' name='id' value='" + rs.getString("id") + "'/>"
						+ "<input type='hidden' name='contentcolumn' value='costperclick'/>"
						+ "<input type='hidden' name='idcolumn' value='id'/>"
						+ "<input type='hidden' name='tablename' value='shops'/>"
						+ "<input type='button' value='Turn Sponsor "
						+ (rs.getFloat("costperclick") > 0.1 ? "off" : "on")
						+ "' onclick=\"$.ajax({type: 'POST',url: '/communityupdate.jsp', data: $('#shops"
						+ rs.getString("id")
						+ "costperclick').serialize(),success:function(data){if ('Error'===data){alert('Error')}else{ alert('OK! Refresh page to see the change.');}}});\">"
						+ "</form></div>");
				sb.append("<td><a href='" + rs.getString("linkback") + "' target='_blank'>" + rs.getString("linkback")
						+ "</a></td><td>"
						+ ((rs.getString("commercialcomment").contains("Basic Listing") ? "Basic"
								: rs.getDate("linkbackok") == null ? "Never" : rs.getDate("linkbackok")))
						+ "</td><td>"
						+ getRegexPatternValue("(\\d\\d\\d\\d-\\d\\d-\\d\\d)", rs.getString("commercialcomment"))
						+ "</td><td><a href='/admin/salesdashboard.jsp?shopid=" + rs.getString("id")
						+ "' target='_blank'>Dashboard</td></tr>");
			}
			sb.append("</table>");
			if (!rs.isAfterLast())
				sb = new StringBuffer();
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return sb.toString();
	}

	public static String IssuesToDoHtml(boolean admin) {
		StringBuffer sb = new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String url = Dbutil.readValueFromDB("select * from config where configkey='todourl'", "value");
		try {
			String actionclause = "";
			if (admin) {
				actionclause = " status=1 ";
				int n = Dbutil.readIntValueFromDB(
						"Select count(*) as thecount from issuelog where actueel=1 and " + actionclause + ";",
						"thecount");
				sb.append(n + " issues are open for you.<br/>");
				query = "Select * from issuelog left join wsshops on (issuelog.shopid=wsshops.shopid) where actueel=1 and issuelog.status=1 order by numberofwines desc, date limit 20;";
			} else {
				actionclause = " status=2 ";
				int n = Dbutil.readIntValueFromDB(
						"Select count(*) as thecount from issuelog where actueel=1 and " + actionclause + ";",
						"thecount");
				sb.append(n + " issues are open for you.<br/>");
				query = "Select * from issuelog where actueel=1 and " + actionclause + " order by date limit 10;";
			}

			rs = Dbutil.selectQuery(rs, query, con);
			sb.append(
					"<table><tr><th style='text-align:left;'>Date</th><th style='text-align:left;'>Store</th><th style='text-align:left;'>Comment</th></tr>");
			while (rs.next()) {
				String comment = rs.getString("message");
				if (comment.length() > 150)
					comment = comment.substring(0, 150) + "...";
				sb.append("<tr><td><a href='issuelog.jsp?shopid=" + rs.getString("shopid") + "'>" + rs.getString("date")
						+ "</a></td><td><a href='issuelog.jsp?shopid=" + rs.getString("shopid") + "'>"
						+ getShopNameFromShopId(rs.getInt("shopid"), "") + " (" + rs.getString("shopid")
						+ ")</a></td><td><a href='issuelog.jsp?shopid=" + rs.getString("shopid") + "'>" + comment
						+ "</a></td></tr>");
			}
			sb.append("</table>");
			if (!rs.isAfterLast())
				sb = new StringBuffer();
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return sb.toString();
	}

	public static String getWineResultsHTML(Wineset wineset, Translator t, Searchdata searchdata, int numberofrows,
			HttpServletResponse response, String map, boolean sponsoredresults, Wineset sponsoredwineset,
			String thispage, boolean feedtest, boolean storepage) {
		StringBuffer sb = new StringBuffer();
		String link = "";
		String shoplink = "";
		if (wineset.records > 0) {
			if (wineset.othercountry)
				sb.append("<h3>" + t.get("noresultsfound") + " for '" + searchdata.getName() + "' in "
						+ Webroutines.getCountryFromCode(searchdata.getCountry()) + ".</h3>");
			if (wineset.othervintage)
				sb.append("<h3>" + t.get("noresultsfound") + " for " + searchdata.getVintage() + ".</h3>");

			sb.append("<div class='results'>");
			if (!feedtest)
				sb.append("<h2>" + wineset.records + " " + t.get("resultsfor") + " '"
						+ (Spider
								.escape((wineset.knownwineid > 0 ? Knownwines.getKnownWineName(wineset.knownwineid)
										: searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", ""))
								+ " " + (!wineset.othervintage ? searchdata.getVintage() : "")).trim()
						+ "'</h2>\n");
			if (searchdata.freetextresults > 0)
				sb.append("<a class='freetext' href='" + thispage + "?keepdata=true&freetext=true'>[Show all "
						+ searchdata.freetextresults + " results for '" + searchdata.getName() + "']</a>");
			sb.append(
					"<div class='bookmark'>\n<script type=\"text/javascript\">\naddthis_url = 'https://www.vinopedia.com/wine/"
							+ Webroutines.URLEncode(searchdata.getName()) + " "
							+ searchdata.getVintage().replaceAll(";jsessionid.*$", "").replace("'", "&apos;")
							+ "';\naddthis_title  = document.title;\n  addthis_pub    = 'freewinesearcher';\n</script>\n<script type=\"text/javascript\" src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\" >\n</script>\n</div>");
			sb.append("\n<div class='permalink'>\n<a href='/wine/" + Webroutines.URLEncode(searchdata.getName())
					+ (searchdata.getVintage().equals("") ? "" : "+")
					+ searchdata.getVintage().replaceAll(";jsessionid.*$", "").replace("'", "&apos;") + "'>\n[ "
					+ t.get("permalink") + " ]</a>&nbsp;</div>\n");
			if (wineset != null && wineset.Wine.length > 0 && Webroutines.getConfigKey("map").equals("true")) {
				if (map.equals("true")) {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\">Show results as list</a>");
				} else {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='true';document.getElementById('Searchform').submit();\"><img src='/images/bottles/Map.png' alt='Show on map'/>&nbsp;New! Show results on map</a>");
				}
			}
			if (!map.equals("true")) {
				String image = "sortasc.jpg";
				if (searchdata.getOrder().contains("desc"))
					image = "sortdesc.jpg";

				// "+((searchdata.getOrder().contains("price")||searchdata.getOrder().equals(""))?"<img
				// class='sort' src='/css/"+sortorder+".jpg'/>":"")+"
				sb.append(
						"<div class='resulth'><div class='flag'></div><div class='shop'>Store</div><div class='winename'>Wine</div><div class='vintage'><a href='"
								+ thispage + "?keepdata=true&amp;order=vintage'>Vintage"
								+ ((searchdata.getOrder().contains("vintage"))
										? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
										: "")
								+ "</a></div><div class='rating'>" + (feedtest ? "" : "Rating")
								+ "</div><div class='size'><a href='" + thispage + "?keepdata=true&amp;order=size'>Size"
								+ ((searchdata.getOrder().contains("size"))
										? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
										: "")
								+ "</a></div><div class='price'><a href='" + thispage
								+ "?keepdata=true&amp;order=priceeuroin'>Price"
								+ ((searchdata.getOrder().contains("priceeuro") || searchdata.getOrder().equals(""))
										? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
										: "")
								+ "</a></div><div class='currency'></div></div>");
				sb.append("<table class=\"results\">");

				NumberFormat format = new DecimalFormat("#,##0.00");

				if (sponsoredresults) {

					if (sponsoredwineset != null && sponsoredwineset.records > 0) {
						sb.append("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
						for (int i = 0; i < sponsoredwineset.Wine.length; i++) {
							sb.append("<tr");
							if (i % 2 == 1)
								sb.append(" class=\"sponsoredodd\"");
							if (i % 2 == 0)
								sb.append(" class=\"sponsoredeven\"");
							sb.append(">");
							sb.append("<td class='flag'><a href='" + thispage + "?name="
									+ (Webroutines.URLEncode(searchdata.getName()) + "&amp;country="
											+ wineset.Wine[i].Country.toUpperCase() + "&amp;vintage="
											+ searchdata.getVintage()).replace("'", "&apos;")
									+ "' target='_blank'><img src='/images/flags/"
									+ sponsoredwineset.Wine[i].Country.toLowerCase() + ".gif' alt='"
									+ sponsoredwineset.Wine[i].Country.toLowerCase() + "' /></a></td>");
							sb.append("<td><a href="
									+ response.encodeURL("link.jsp?shopid=" + sponsoredwineset.Wine[i].ShopId)
									+ " target='_blank'>" + sponsoredwineset.Wine[i].Shopname + "</a></td>");
							sb.append("<td><a href="
									+ response.encodeURL("link.jsp?wineid=" + sponsoredwineset.Wine[i].Id)
									+ " target='_blank'>" + sponsoredwineset.Wine[i].Name + "</a></td>");
							sb.append("<td>" + sponsoredwineset.Wine[i].Vintage + "</td>");
							sb.append("<td align='right'>" + Webroutines.formatSize(sponsoredwineset.Wine[i].Size)
									+ "</td>");
							sb.append("<td align='right'>" + Webroutines.formatPrice(
									sponsoredwineset.Wine[i].PriceEuroIn, sponsoredwineset.Wine[i].PriceEuroEx,
									searchdata.getCurrency(), searchdata.getVat()) + "</td>");
							sb.append("</tr>\n");
						}
						sb.append("<tr><td colspan='4'><i>All results:</i></td></tr>");

					} else {
						sb.append("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
						sb.append("<tr class=\"sponsoredeven\">");
						sb.append("<td colspan='2'><a href='" + response.encodeURL("/sponsoring.jsp") + "'></a></td>");
						sb.append("<td colspan='2'><a href='" + response.encodeURL("/sponsoring.jsp")
								+ "'>Your wine could be listed here! Click for more information.</a></td>");
						sb.append("<td align='right'></td>");
						sb.append("<td align='right'>&euro; 0.10</td>");
						sb.append("</tr>");
						sb.append("<tr><td colspan='4'><i>All results:</i></td></tr>");
					}
				}
				String rating;
				// Give the complete result list
				for (int i = 0; i < wineset.Wine.length; i++) {
					if (feedtest) {
						link = wineset.Wine[i].SourceUrl;
						shoplink = wineset.Wine[i].SourceUrl;
					} else if (storepage) {
						link = "/store/" + URLEncode(wineset.Wine[i].Shopname) + "/?wineid=" + wineset.Wine[i].Id;
						shoplink = "/store/" + URLEncode(wineset.Wine[i].Shopname) + "/?wineid=" + wineset.Wine[i].Id;
					} else {
						link = "/link.jsp?wineid=" + wineset.Wine[i].Id;
						shoplink = "/link.jsp?wineid=" + wineset.Wine[i].Id + "&amp;shopid=" + wineset.Wine[i].ShopId;
					}
					sb.append("\n<tr");
					if (wineset.Wine[i].CPC > 0 && sponsoredresults) {
						sb.append(" class=\"sponsoredeven\"");
					} else {
						if (i % 2 == 1) {
							sb.append(" class=\"odd\"");
						}
					}
					sb.append(">");
					sb.append("<td class='flag'><a href='" + thispage + "?name="
							+ (Webroutines.URLEncode(searchdata.getName()) + "&amp;country="
									+ wineset.Wine[i].Country.toUpperCase() + "&amp;vintage=" + searchdata.getVintage())
											.replace("'", "&apos;")
							+ "' " + (storepage ? "" : "target='_blank'") + "><img src='/images/flags/"
							+ wineset.Wine[i].Country.toLowerCase() + ".gif' alt='"
							+ wineset.Wine[i].Country.toLowerCase() + "' /></a></td>");
					sb.append("<td class='shop'><a href='" + shoplink + "' " + (storepage ? "" : "target='_blank'")
							+ ">" + wineset.Wine[i].Shopname.replace("&", "&amp;") + "</a></td>");
					sb.append("<td class='winename'><a href='" + link + "' " + (storepage ? "" : "target='_blank'")
							+ " title='"
							+ Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)).replace("'", "&apos;")
							+ "'>" + Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)) + "</a></td>");
					sb.append("<td class='vintage'>"
							+ ("0".equals(wineset.Wine[i].Vintage) ? "" : wineset.Wine[i].Vintage) + "</td>");
					sb.append("<td class='rating'>"
							+ ((rating = wineset.Wine[i].getAverageRating() + "").equals("0") ? "" : rating) + "</td>");
					sb.append("<td class='size'>" + Webroutines.formatNewSize(wineset.Wine[i].Size) + "</td>");
					sb.append("<td class='price' align='right'>"
							+ Webroutines.formatPriceNoCurrency(wineset.Wine[i].PriceEuroIn,
									wineset.Wine[i].PriceEuroEx, searchdata.getCurrency(), searchdata.getVat())
							+ "</td>");
					sb.append("<td class='currency' align='right'>"
							+ Webroutines.getCurrencySymbol(searchdata.getCurrency()) + "</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				int page = searchdata.getOffset() / numberofrows + 1;
				int lastpage = wineset.records / numberofrows + 1;
				int startpage = page - 2;
				if (startpage < 1)
					startpage = 1;
				int endpage = startpage + 4;
				if (endpage > lastpage) {
					endpage = lastpage;
					startpage = endpage - 4;
					if (startpage < 1)
						startpage = 1;
				}
				sb.append("<div class='resultf'><div class='pages'>" + t.get("page") + "&nbsp;" + page + " "
						+ t.get("of") + " " + lastpage + "</div><div class='pageselector'>");
				if (page > 1)
					sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((page - 2) * numberofrows) + "'>"
							+ t.get("previous") + "</a>&nbsp;&nbsp;&nbsp;");
				if (startpage > 1)
					sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=0'>1</a>&hellip;");
				for (int i = startpage; i <= endpage; i++) {
					sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((i - 1) * numberofrows) + "'>"
							+ i + "</a>");
				}
				if (lastpage > endpage)
					sb.append("&hellip;<a href='" + thispage + "?keepdata=true&amp;offset="
							+ (lastpage - 1) * numberofrows + "'>" + lastpage + "</a>");
				if (page < lastpage)
					sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((page) * numberofrows)
							+ "'>&nbsp;&nbsp;&nbsp;" + t.get("next") + "</a>");

				sb.append("</div></div></div>");

			}
		} else {
			sb.append("<h2>" + t.get("noresultsfound") + " for '" + searchdata.getName() + "'</h2>");

			if (wineset.knownwineid == 0 && searchdata.getCountry().equals("")) {
				ArrayList<String> alternatives = com.freewinesearcher.common.Knownwines
						.getNewAlternatives(searchdata.getName());
				if (alternatives.size() > 0) {
					sb.append("<div class='alternatives'>");
					sb.append("<h2>" + t.get("alternatives") + "</h2>");
					sb.append(
							"<div class='alternativesh'><div class='left'></div><div class='winename'>Wine</div><div class='region'>Region</div><div class='hits'>Results</div></div>");
					sb.append("<table class=\"alternatives\">");
					for (int i = 0; i < alternatives.size(); i = i + 4) {
						sb.append("<tr");
						if ((i / 4) % 2 == 1) {
							sb.append(" class=\"odd\"");
						}
						sb.append("><td class='winename'><a href='/wine/"
								+ Webroutines.URLEncode(alternatives.get(i + 2)) + "'>" + alternatives.get(i)
								+ "</a></td><td class='region'>" + alternatives.get(i + 1) + "</td><td class='number'>"
								+ alternatives.get(i + 3) + "</td></tr>");
					}
					sb.append("</table>");
					sb.append("<div class='alternativesf'><div class='left'></div><div class='right'></div></div>");
					sb.append("<div class='alternatives'>");
				}
			}
		}
		return sb.toString();

	}

	public static int updateCoordinates(HttpServletRequest request, int regionid, double lat, double lon) {
		String query;
		int result = 0;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from kbregionhierarchy where id=" + regionid;
			rs = Dbutil.selectQuery(rs, query, con);
			ChangeLog log = new ChangeLog(new Context(request), rs, "id");
			log.setValueOld(rs);
			Dbutil.closeRs(rs);
			query = "update kbregionhierarchy set lat=" + lat + ", lon=" + lon + " , lasteditor='"
					+ Spider.SQLEscape(request.getRemoteUser()) + "' where id=" + regionid;
			result = Dbutil.executeQuery(query, con);
			if (result > 0) {
				query = "select * from kbregionhierarchy where id=" + regionid;
				rs = Dbutil.selectQuery(rs, query, con);
				log.setValueNew(rs);
				log.save();
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
			result = 0;
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return result;
	}

	public static String getWineInfo(int knownwineid, int vintage, PageHandler p) {
		if (knownwineid == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		String query = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			Knownwine k = new Knownwine(knownwineid);
			k.getProperties();
			int n = Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="
					+ knownwineid + " group by knownwineid", "thecount");
			String label = "";
			if (new File(
					Configuration.workspacedir + "labels" + System.getProperty("file.separator") + knownwineid + ".jpg")
							.exists()) {
				label = "<img style='float:right;max-width:250px;max-height:250px;' src='" + Configuration.staticprefix
						+ "/labels/" + knownwineid + ".jpg' alt=\"" + Knownwines.getKnownWineName(knownwineid)
						+ " label\" />";
			} else if (new File(
					Configuration.workspacedir + "labels" + System.getProperty("file.separator") + knownwineid + ".gif")
							.exists()) {
				label = "<img style='float:right;max-width:250px;max-height:250px;' src='" + Configuration.staticprefix
						+ "/labels/" + knownwineid + ".gif' alt='"
						+ Knownwines.getKnownWineName(knownwineid).replace("'", "&apos;") + " label' />";
			}
			StringBuffer ratingnote = new StringBuffer();
			if (n > 0) {
				ratingnote.append(getRatingText(vintage, knownwineid));
				ratingnote.append(
						"<span class='hreview-aggregate'>For all vintages of <span class='item'><span class='fn'>"
								+ k.name + "</span></span> we have <span class='count'>" + n
								+ "</span> ratings on record with an average of <span class='rating'><span class='average'>"
								+ Dbutil.readIntValueFromDB(
										"select avg(rating) as rating from ratinganalysis where author='FWS' and knownwineid="
												+ knownwineid + " group by knownwineid",
										"rating")
								+ "</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span></span><br/><h3><a href='"
								+ winelink(k.uniquename, 0) + "' title='" + k.name.replaceAll("'", "&apos;")
								+ " ratings' >See all vintages and ratings of " + k.name + "</a>.</h3>");

			} else {
				ratingnote.append("<h2>Other vintages</h2><a href='" + winelink(k.uniquename, 0) + "' title='Buy "
						+ k.name.replaceAll("'", "&apos;") + "' >View all vintages of " + k.name + "</a>");
			}
			String currency = "EUR";
			if (k.getProperties().get("locale").contains("USA"))
				currency = "USD";
			ratingnote.append(
					"<div itemscope='itemscope' itemtype='http://data-vocabulary.org/Product'><span itemprop='offerDetails' itemscope itemtype='http://data-vocabulary.org/Offer-aggregate'><meta itemprop='offerCount' content='"
							+ p.s.wineset.records + "' /><meta itemprop='currency' content='" + currency
							+ "' /><meta itemprop='lowPrice' content='"
							+ formatPriceNoCurrency(p.s.wineset.lowestprice, p.s.wineset.lowestprice, currency, "EX")
							+ "'><meta itemprop='highPrice' content='"
							+ formatPriceNoCurrency(p.s.wineset.highestprice, p.s.wineset.highestprice, currency, "EX")
							+ "'></span></div>");

			StringBuffer factsheet = new StringBuffer();
			String country = "";
			try {
				country = k.getProperties().get("locale").split(", ")[0];
			} catch (Exception e) {
			}
			if (country.equals(k.getProperties().get("appellation")))
				country = "";
			factsheet.append("<div class='factsheet'><div class='factsheettop'><h2>Fact Sheet</h2></div><table>");
			factsheet.append("<tr><td class='category'>Winery:</td><td><a href='/winery/"
					+ Webroutines.URLEncodeUTF8Normalized(k.getProperties().get("producer")).replaceAll("%2F", "/")
							.replace("&", "&amp;")
					+ "' title='" + k.getProperties().get("producer").replaceAll("'", "&apos;")
					+ ": wines and winery information'>" + k.getProperties().get("producer") + "</a></td></tr>");
			factsheet.append("<tr><td class='category'>Appellation:</td><td><a href='/region/"
					+ removeAccents(k.getProperties().get("locale")).replaceAll(", ", "/").replaceAll("'", "&apos;")
							.replaceAll(" ", "+")
					+ "' title='" + k.getProperties().get("appellation").replaceAll("'", "&apos;")
					+ ": wine region information'>" + k.getProperties().get("appellation") + "</a></td></tr>");
			if (country.length() > 0)
				factsheet.append("<tr><td class='category'>Country:</td><td>" + country + "</td></tr>");
			if (k.getProperties().get("designation") != null && k.getProperties().get("designation").length() > 0)
				factsheet.append("<tr><td class='category'>Cuv�e:</td><td>" + k.getProperties().get("designation")
						+ "</td></tr>");
			factsheet.append("<tr><td class='category'>Vintage:</td><td>" + vintage + "</td></tr>");
			factsheet.append(
					"<tr><td class='category'>Wine type:</td><td>" + k.getProperties().get("type") + "</td></tr>");
			factsheet.append(
					"<tr><td class='category'>Grapes:</td><td>" + k.getProperties().get("grapes") + "</td></tr>");

			Dbutil.closeRs(rs);

			factsheet.append("</table><div class='factsheetbottom'></div></div>");

			sb.append("<div class='container vintageinfo'>");
			sb.append("<h1>" + k.name + (vintage > 0 ? " " + vintage : "") + "</h1>");
			sb.append(factsheet);
			sb.append(label);
			sb.append("<p>" + k.getDescription(vintage) + "</p>");
			sb.append(ratingnote);
			sb.append("</div><div class='clear'></div>");
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static StringBuffer getRatingText(int vintage, int knownwineid) {
		StringBuffer ratingnote = new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (vintage > 0) {
				query = "select * from ratedwines where knownwineid=" + knownwineid + " and vintage=" + vintage
						+ " and author !='FWS';";
				rs = Dbutil.selectQuery(query, con);
				if (rs.isBeforeFirst()) {
					ratingnote.append("<ul>");
					while (rs.next()) {
						ratingnote.append("<li>" + getAuthorName(rs.getString("author")).replaceAll("&", "&amp;")
								+ " gave a rating of " + rs.getString("rating")
								+ (rs.getInt("ratinghigh") > 0 ? "-" + rs.getString("ratinghigh") : "")
								+ getAuthorScale(rs.getString("author")) + " to the " + vintage + " "
								+ formatCapitals(rs.getString("name")).replaceAll("&", "&amp;") + "</li>");
					}
					ratingnote.append("</ul>");

				}
				Dbutil.closeRs(rs);
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs);
		}
		return ratingnote;

	}

	public static String getWineStory(int knownwineid, int vintage, RatingInfo ri) {
		StringBuffer sb = new StringBuffer();
		String query = "select knownwines.*,kbproducers.description as producerdesc,grapes.pedia as grapedesc, kb.description as regiondesc,kb.shortregion from knownwines left join kbproducers on (producer=kbproducers.name) left join kbregionhierarchy on (locale=region) left join kbregionhierarchy kb on (kbregionhierarchy.lft>=kb.lft and kbregionhierarchy.rgt<=kb.rgt and kb.description!='') left join grapes on (grapename=grapes) where knownwines.id="
				+ knownwineid + " order by (kb.rgt-kb.lft);";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				String producerdesc = rs.getString("producerdesc");
				String grapedesc = rs.getString("grapedesc");
				String regiondesc = rs.getString("regiondesc");
				if ((producerdesc != null && producerdesc.length() > 0)
						|| (regiondesc != null && regiondesc.length() > 0)
						|| (grapedesc != null && grapedesc.length() > 0)) {
					Knownwine k = new Knownwine(knownwineid);
					k.getProperties();
					String label = "";
					if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
							+ knownwineid + ".jpg").exists()) {
						label = "<img src='/labels/" + knownwineid + ".jpg' alt=\""
								+ Knownwines.getKnownWineName(knownwineid) + "\" /><br/><br/>";
					} else if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
							+ knownwineid + ".gif").exists()) {
						label = "<img src='/labels/" + knownwineid + ".gif' alt='"
								+ Knownwines.getKnownWineName(knownwineid).replace("'", "&apos;") + "' /><br/><br/>";

					}
					sb.append("<dl><dt><h2>" + k.name + (vintage > 0 ? " " + vintage : "")
							+ " information</h2></dt><dd>" + label + k.getDescription(vintage)
							+ (ri != null && ri.ratingnote != null && ri.ratingnote.length() > 0
									? "<br/>" + ri.ratingnote
									: "")
							+ "</dd></dl>");

				}

				if (producerdesc != null && producerdesc.length() > 0) {
					sb.append("<h2>Winery: " + rs.getString("producer") + "</h2>" + producerdesc);
					sb.append("<a href='/winery/"
							+ Webroutines.URLEncodeUTF8Normalized(rs.getString("producer")).replaceAll("%2F", "/")
									.replace("&", "&amp;")
							+ "'>View all wines, contact details and a map of " + rs.getString("producer") + "</a>");
				}
				if (regiondesc != null && regiondesc.length() > 0) {
					sb.append("<h2>Region: " + rs.getString("appellation") + "</h2>"
							+ (rs.getString("shortregion").equals(rs.getString("appellation")) ? ""
									: "(information taken from " + rs.getString("shortregion") + ")<br/>")
							+ regiondesc);
					sb.append("<a href='/region/"
							+ rs.getString("locale").replaceAll("/", "%252F").replaceAll(", ", "/").replaceAll(" ", "+")
							+ "/'>View the important producers, grapes and the wine guide for "
							+ rs.getString("appellation") + "</a>");

				}
				if (grapedesc != null && grapedesc.length() > 0) {
					sb.append("<h2>Grapes used: " + rs.getString("grapes") + "</h2>" + grapedesc);
				}

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static String getTabbedWineResultsHTML(Wineset wineset, Translator t, Searchdata searchdata,
			int numberofrows, HttpServletResponse response, String map, boolean sponsoredresults, String thispage,
			int singlevintage, RatingInfo ri, boolean feedtest, boolean showeditbuttons) {
		StringBuffer sb = new StringBuffer();
		String link = "";
		String shoplink = "";
		String rating;
		String winename = "";
		String uniquewinename = "";
		if (wineset.records > 0) {
			int page = searchdata.getOffset() / numberofrows + 1;
			int lastpage = (wineset.records - 1) / numberofrows + 1;
			int startpage = page - 2;
			if (startpage < 1)
				startpage = 1;
			int endpage = startpage + 4;
			if (endpage > lastpage) {
				endpage = lastpage;
				startpage = endpage - 4;
				if (startpage < 1)
					startpage = 1;
			}
			winename = (wineset.knownwineid > 0 ? Knownwines.getKnownWineName(wineset.knownwineid)
					: searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", "");
			uniquewinename = (wineset.knownwineid > 0 ? Knownwines.getUniqueKnownWineName(wineset.knownwineid)
					: searchdata.getName());

			sb.append("<div class='results' id='wr'>");
			if (wineset.othercountry) {
				sb.append("<h2>" + t.get("noresultsfound") + " for '" + searchdata.getName() + "' in "
						+ Webroutines.getCountryFromCode(searchdata.getCountry()) + ", showing " + wineset.records
						+ " results worldwide.</h2>");
			} else {
				if (!feedtest)
					sb.append("<h2>" + t.get("pricecomparisonfor") + " '"
							+ (Spider.escape(winename) + " " + (!wineset.othervintage ? searchdata.getVintage() : ""))
									.trim()
							+ "'"
							+ (searchdata.country.equals("All") ? "" : " in " + getCountryFromCode(searchdata.country))
							+ "</h2>\n");
				// if (singlevintage>0&&wineset.knownwineid>0) sb.append("<a
				// href='/wine/"+URLEncode(removeAccents(uniquewinename))+"'
				// title='"+winename+"'>View all vintages of "+winename+"</a><br/><br/>");
			}
			if (wineset.othervintage)
				sb.append("<h2>" + t.get("noresultsfound") + " for " + searchdata.getVintage()
						+ ", showing other vintages.</h2>");

			if (searchdata.freetextresults > 0)
				sb.append("<a class='freetext' href='" + thispage + "?keepdata=true&freetext=true'>[Show all "
						+ searchdata.freetextresults + " results for '" + searchdata.getName() + "']</a>");
			// sb.append("<div class='bookmark'>\n<script
			// type=\"text/javascript\">\naddthis_url =
			// 'https://www.vinopedia.com/wine/"+Webroutines.URLEncode(searchdata.getName())+"
			// "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"';\naddthis_title
			// = document.title;\n addthis_pub = 'freewinesearcher';\n</script>\n<script
			// type=\"text/javascript\"
			// src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\"
			// >\n</script>\n</div>");
			// sb.append("\n<div class='permalink'>\n<a
			// href='/wine/"+Webroutines.URLEncode(searchdata.getName())+(searchdata.getVintage().equals("")?"":"+")+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'>\n[
			// "+t.get("permalink")+" ]</a>&nbsp;</div>\n");
			if (wineset != null && wineset.Wine.length > 0 && Webroutines.getConfigKey("map").equals("true")) {
				if (map.equals("true")) {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\">Show results as list</a>");
				} else {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='true';document.getElementById('Searchform').submit();\"><img src='/images/bottles/Map.png' alt='Show on map'/>&nbsp;New! Show results on map</a>");
				}
			}

			String image = "sortasc.jpg";
			if (searchdata.getOrder().contains("desc"))
				image = "sortdesc.jpg";
			String sortimage = "sortasc";
			if (searchdata.getOrder().contains("desc"))
				sortimage = "sortdesc";
			String pedia = "";
			// pedia=getWineStory(wineset.knownwineid,singlevintage,ri);
			// "+((searchdata.getOrder().contains("price")||searchdata.getOrder().equals(""))?"<img
			// class='sort' src='/css/"+sortorder+".jpg'/>":"")+"
			String mappane = "";
			if (map.equals("true") && wineset.bestknownwineid > 0) {
				// mappane="<div id='storelocator' style='width:825px;height:500px;border:1px
				// solid #4d0027;'></div>\n<script
				// type='text/javascript'>\n/*<![CDATA[*/\nfunction showmap(){
				// }\n/*]]>*/\n</script>";
				mappane = "<div id='storelocator' style='width:825px;height:500px;border:1px solid #4d0027;'></div>\n<script type='text/javascript'>\n/*<![CDATA[*/\nfunction showmap(){	if(document.getElementById(\"storelocator\")!=null&&document.getElementById(\"storelocator\").innerHTML==''){		var vpiframe = document.createElement( \"iframe\" );		if (document.getElementById(\"storelocator\").style.width=='') document.getElementById(\"storelocator\").style.width=\"950px\";		if (document.getElementById(\"storelocator\").style.height=='') document.getElementById(\"storelocator\").style.height=\"500px\";		vpiframe.setAttribute(\"frameborder\",\"0\");		vpiframe.setAttribute(\"scrolling\",\"no\");		vpiframe.setAttribute(\"width\",document.getElementById(\"storelocator\").style.width);		vpiframe.setAttribute(\"height\",document.getElementById(\"storelocator\").style.height);		vpiframe.setAttribute(\"overflow\",\"hidden\");		vpiframe.setAttribute( \"src\", \"\"+\"/storelocator.jsp?knownwineid="
						+ wineset.bestknownwineid
						+ (singlevintage > 0 && !wineset.othervintage ? "&vintage=" + singlevintage : "")
						+ "&width=\"+document.getElementById(\"storelocator\").style.width+\"&height=\"+document.getElementById(\"storelocator\").style.height+\"&showprices=true\");		document.getElementById(\"storelocator\").appendChild(vpiframe);	}}\n/*]]>*/\n</script>";
			}
			sb.append(
					"<ul class='tabs' id='resulttabs'><li id='Worldwide'><a href='?country=All&amp;keepdata=true' rel='nofollow'"
							+ (wineset.othercountry || searchdata.getCountry().equals("All")
									? " class='current resulttab'"
									: "")
							+ ">Worldwide</a></li><li id='USA'><a href='?country=UC&amp;keepdata=true' rel='nofollow'"
							+ (!wineset.othercountry && searchdata.country.equals("UC") ? " class='current resulttab'"
									: "")
							+ ">USA/Canada</a></li><li><a href='?country=EU&amp;keepdata=true' rel='nofollow'"
							+ (!wineset.othercountry && searchdata.country.equals("EU") ? " class='current resulttab'"
									: "")
							+ ">Europe</a></li>"
							+ (!"".equals(searchdata.lastcountry)
									? ("<li><a href='?country=" + searchdata.lastcountry
											+ "&amp;keepdata=true' rel='nofollow' "
											+ (!wineset.othercountry
													&& searchdata.country.equals(searchdata.lastcountry)
															? "class='current resulttab'"
															: "")
											+ ">" + (wineset.othercountry ? "<strike>" : "")
											+ getCountryFromCode(searchdata.lastcountry)
											+ (wineset.othercountry ? "</strike>" : "") + "</a></li>")
									: "")
							+ (pedia.length() > 0 ? "<li><a href='#pedia' class='s resulttab'>Pedia</a></li>" : "")
							+ (mappane.length() > 0
									? "<li><a href='#map' onclick='javascript:showmap()' class='s resulttab'>Map</a></li>"
									: "")
							+ "</ul><div class='panes' id='gspanes'><div class='pan' id='pane1'>");
			sb.append("<div class='items' id='items1'>");
			sb.append((page < lastpage
					? "<div id='rnext' class='nextPage'><a href='?offset=" + (page) * numberofrows
							+ "&amp;keepdata=true' rel='nofollow'>Next</a></div>"
					: "")
					+ (page > 1
							? "<div id='rprev' class='prevPage nav'><a href='?offset=" + (page - 2) * numberofrows
									+ "&amp;keepdata=true' rel='nofollow'>Previous</a></div>"
							: ""));
			sb.append(
					"<div class='spriter spriter-resulth'><div class='sprite sprite-resulthlstr'></div><div class='shop'>Store</div><div class='winename'>Wine</div><div class='vintage'><a href='"
							+ thispage + "?keepdata=true&amp;order=vintage' rel='nofollow'>Vintage"
							+ ((searchdata.getOrder().contains("vintage")) ? "<img class='sprite sprite-" + sortimage
									+ " sort' alt='sort' src='" + Configuration.cdnprefix + "/images/transparent.gif'/>"
									: "")
							+ "</a></div><div class='rating'>" + (feedtest ? "" : "Rating")
							+ "</div><div class='size'><a href='" + thispage
							+ "?keepdata=true&amp;order=size' rel='nofollow'>Size"
							+ ((searchdata.getOrder().contains("size")) ? "<img class='sprite sprite-" + sortimage
									+ " sort' alt='sort' src='" + Configuration.cdnprefix + "/images/transparent.gif'/>"
									: "")
							+ "</a></div><div class='price'><a href='" + thispage
							+ "?keepdata=true&amp;order=priceeuroex' rel='nofollow'>Price"
							+ ((searchdata.getOrder().contains("priceeuro") || searchdata.getOrder().equals(""))
									? "<img class='sprite sprite-" + sortimage + " sort' alt='sort' src='"
											+ Configuration.cdnprefix + "/images/transparent.gif'/>"
									: "")
							+ "</a></div><div class='currency sprite sprite-resulthr'></div></div>");
			sb.append("<table class=\"results\">");

			NumberFormat format = new DecimalFormat("#,##0.00");

			if (wineset.SponsoredWine != null && wineset.SponsoredWine.length > 0) {
				sb.append("<tr><td class='flag'></td>");
				sb.append("<td class='shop'><i>Featured merchants:</i></td>");
				sb.append("<td class='winename'></td>");
				sb.append("<td class='vintage'></td>");
				sb.append("<td class='rating'></td>");
				sb.append("<td class='size'></td>");
				sb.append("<td class='price'></td>");
				sb.append("<td class='currency'></td>");
				sb.append("</tr>");
				for (int i = 0; i < wineset.SponsoredWine.length; i++) {
					if (feedtest) {
						link = wineset.SponsoredWine[i].SourceUrl;
						shoplink = wineset.SponsoredWine[i].SourceUrl;
					} else {
						// link="/adhandler.jsp?type=Featured&amp;wineid="+wineset.SponsoredWine[i].Id;
						// shoplink="/adhandler.jsp?type=Featured&amp;wineid="+wineset.SponsoredWine[i].Id;
						link = "\"vpclick('/'+['adhandler.jsp','type=Featured&amp;wineid=" + wineset.SponsoredWine[i].Id
								+ "'].join('?'))\"";
						shoplink = link;
					}
					sb.append("\n<tr");
					if (i % 2 == 1) {
						sb.append(" class=\"sponsoredodd\"");
					} else {
						sb.append(" class=\"sponsoredeven\"");
					}
					sb.append(">");
					sb.append("<td class='flag'><span class='jslink' onclick='vpclick(\"" + thispage + "?name="
							+ (Webroutines.URLEncode(searchdata.getName()) + "&amp;country="
									+ wineset.SponsoredWine[i].Country.toUpperCase() + "&amp;vintage="
									+ searchdata.getVintage()).replace("'", "&apos;")
							+ "\");'><img src='" + Configuration.cdnprefix
							+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
							+ wineset.SponsoredWine[i].Country.toLowerCase() + "'/></span></td>");
					sb.append("<td class='shop'><span class='jslink' onclick=" + shoplink + ">"
							+ wineset.SponsoredWine[i].Shopname.replace("&", "&amp;") + "</span></td>");
					sb.append("<td class='winename'><span class='jslink' onclick=" + link + " >"
							+ Spider.escape(Webroutines.formatCapitals(wineset.SponsoredWine[i].Name))
							+ "</span></td>");
					sb.append("<td class='vintage'>"
							+ ("0".equals(wineset.SponsoredWine[i].Vintage) ? "" : wineset.SponsoredWine[i].Vintage)
							+ "</td>");
					sb.append("<td class='rating'>"
							+ ((rating = wineset.SponsoredWine[i].getAverageRating() + "").equals("0") ? "" : rating)
							+ "</td>");
					sb.append("<td class='size'>" + Webroutines.formatNewSize(wineset.SponsoredWine[i].Size) + "</td>");
					sb.append("<td class='price'>"
							+ Webroutines.formatPriceNoCurrency(wineset.SponsoredWine[i].PriceEuroIn,
									wineset.SponsoredWine[i].PriceEuroEx, searchdata.getCurrency(), searchdata.getVat())
							+ "</td>");
					sb.append("<td class='currency'>" + Webroutines.getCurrencySymbol(searchdata.getCurrency())
							+ "</td>");
					if (showeditbuttons) {
						sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
								+ wineset.SponsoredWine[i].Id
								+ "&newknownwineid=-1' target='_blank'><img src='/images/trash.gif' alt='remove'/></a></td>");
						sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
								+ wineset.SponsoredWine[i].Id
								+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='edit'/></a></td>");
					}
					sb.append("</tr>");
				}
				sb.append("<tr><td class='flag'></td>");
				sb.append("<td class='shop'><i>All results:</i></td>");
				sb.append("<td class='winename'></td>");
				sb.append("<td class='vintage'></td>");
				sb.append("<td class='rating'></td>");
				sb.append("<td class='size'></td>");
				sb.append("<td class='price'></td>");
				sb.append("<td class='currency'></td>");
				sb.append("</tr>");

			} else if (false) {
				sb.append("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
				sb.append("<tr class=\"sponsoredeven\">");
				sb.append("<td colspan='2'><a href='" + response.encodeURL("/sponsoring.jsp") + "'></a></td>");
				sb.append("<td colspan='2'><a href='" + response.encodeURL("/sponsoring.jsp")
						+ "'>Your wine could be listed here! Click for more information.</a></td>");
				sb.append("<td align='right'></td>");
				sb.append("<td align='right'>&euro; 0.10</td>");
				sb.append("</tr>");
				sb.append("<tr><td colspan='4'><i>All results:</i></td></tr>");
			}
			// Give the complete result list
			for (int i = 0; i < wineset.Wine.length; i++) {
				if (feedtest) {
					link = wineset.Wine[i].SourceUrl;
					shoplink = wineset.Wine[i].SourceUrl;
				} else {
					// link="/store/"+URLEncode(removeAccents(wineset.Wine[i].Shopname)).replaceAll("%2F",
					// "/")+"/?wineid="+wineset.Wine[i].Id;
					// shoplink="/store/"+URLEncode(removeAccents(wineset.Wine[i].Shopname)).replaceAll("%2F",
					// "/")+"/?wineid="+wineset.Wine[i].Id;
					link = "\"/store/" + URLEncode(removeAccents(wineset.Wine[i].Shopname)).replaceAll("%2F", "/")
							+ "/\"," + wineset.Wine[i].Id;
				}
				sb.append("\n<tr");
				if (i % 2 == 0) {
					sb.append(" class=\"odd\"");
				} else {
					sb.append(" class=\"even\"");
				}
				sb.append(">");
				sb.append("<td class='flag'><span class='jslink' onclick='vpclick(\"" + thispage + "?name="
						+ (Webroutines.URLEncode(searchdata.getName()) + "&amp;country="
								+ wineset.Wine[i].Country.toUpperCase() + "&amp;vintage=" + searchdata.getVintage())
										.replace("'", "&apos;")
						+ "&amp;keepdata=true\");'><img src='" + Configuration.cdnprefix
						+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
						+ wineset.Wine[i].Country.toLowerCase() + "'/></span></td>");
				sb.append("<td class='shop'><span class='jslink' onclick='clickshop(" + link + ");'>"
						+ wineset.Wine[i].Shopname.replace("&", "&amp;") + "</span></td>");
				sb.append("<td class='winename'><span class='jslink' onclick='clickshop(" + link + ");' >"
						+ Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)) + " </span></td>");
				sb.append("<td class='vintage'>" + ("0".equals(wineset.Wine[i].Vintage) ? "" : wineset.Wine[i].Vintage)
						+ "</td>");
				sb.append("<td class='rating'>"
						+ ((rating = wineset.Wine[i].getAverageRating() + "").equals("0") ? "" : rating) + "</td>");
				sb.append("<td class='size'>" + Webroutines.formatNewSize(wineset.Wine[i].Size) + "</td>");
				sb.append(
						"<td class='price'>"
								+ Webroutines.formatPriceNoCurrency(wineset.Wine[i].PriceEuroIn,
										wineset.Wine[i].PriceEuroEx, searchdata.getCurrency(), searchdata.getVat())
								+ "</td>");
				sb.append("<td class='currency'>" + Webroutines.getCurrencySymbol(searchdata.getCurrency()) + "</td>");
				if (showeditbuttons) {
					sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
							+ wineset.Wine[i].Id
							+ "&newknownwineid=-1' target='_blank'><img src='/images/trash.gif' alt='remove'/></a></td>");
					sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
							+ wineset.Wine[i].Id
							+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='edit'/></a></td>");
				}
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("<div class='resultf spriter spriter-resultf'><div class='pages sprite sprite-resultfl'>"
					+ t.get("page") + "&nbsp;" + page + " " + t.get("of") + " " + lastpage
					+ "</div><div class='sprite sprite-resultfr'>&nbsp;</div><div class='pageselector'>");
			if (page > 1)
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((page - 2) * numberofrows)
						+ "' rel='nofollow'>" + t.get("previous") + "</a>&nbsp;&nbsp;&nbsp;");
			if (startpage > 1)
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=0' rel='nofollow'>1</a>&hellip;");
			for (int i = startpage; i <= endpage; i++) {
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((i - 1) * numberofrows)
						+ "' rel='nofollow'>" + i + "</a>");
			}
			if (lastpage > endpage)
				sb.append("&hellip;<a href='" + thispage + "?keepdata=true&amp;offset=" + (lastpage - 1) * numberofrows
						+ "'>" + lastpage + "</a>");
			if (page < lastpage)
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((page) * numberofrows)
						+ "'>&nbsp;&nbsp;&nbsp;" + t.get("next") + "</a>");
			sb.append("</div></div>");
			if (true || mappane.length() > 0)
				sb.append(getRefineNarrow(wineset, t, thispage, searchdata));
			sb.append("</div><!--items-->"); // items
			sb.append("</div>"
					+ (pedia.length() > 0
							? "\n<div class='pan' id='pedia'><div class='items' id='items2'><div class='page'>" + pedia
									+ "</div></div><!--items--></div>"
							: "")
					+ (mappane.length() > 0
							? "\n<div class='pan' id='map'><div class='items' id='items2'><div class='page'>" + mappane
									+ "</div></div><!--items--></div>"
							: "")); // panes
			// sb.append("</div>\n</div>");

		} else {
			sb.append("<h2>" + t.get("noresultsfound") + " for '" + searchdata.getName() + "'</h2>");

			if (wineset.knownwineid == 0) {
				ArrayList<String> alternatives = com.freewinesearcher.common.Knownwines
						.getNewerAlternatives(searchdata.getName());
				if (alternatives.size() > 0) {
					sb.append("<div class='alternatives'>");
					sb.append("<h2>" + t.get("alternatives") + "</h2>");
					for (int i = 0; i < alternatives.size(); i++) {
						sb.append(alternatives.get(i) + "<br/>");
					}
					// sb.append("<div class='alternatives'>");
				}
			}
		}
		return sb.toString();

	}

	public static String JOINgetTabbedWineResultsHTML(Wineset wineset, Translator t, Searchdata searchdata,
			int numberofrows, HttpServletResponse response, String map, boolean sponsoredresults, String thispage,
			int singlevintage, RatingInfo ri, boolean feedtest, boolean showeditbuttons) {
		StringBuffer sb = new StringBuffer();
		String link = "";
		String shoplink = "";
		String rating;
		String winename = "";
		String uniquewinename = "";
		if (wineset.records > 0) {
			int page = searchdata.getOffset() / numberofrows + 1;
			int lastpage = (wineset.records - 1) / numberofrows + 1;
			int startpage = page - 2;
			if (startpage < 1)
				startpage = 1;
			int endpage = startpage + 4;
			if (endpage > lastpage) {
				endpage = lastpage;
				startpage = endpage - 4;
				if (startpage < 1)
					startpage = 1;
			}
			winename = (wineset.knownwineid > 0 ? Knownwines.getKnownWineName(wineset.knownwineid)
					: searchdata.getName()).replaceAll("^\\d\\d\\d\\d\\d\\d ", "");
			uniquewinename = (wineset.knownwineid > 0 ? Knownwines.getUniqueKnownWineName(wineset.knownwineid)
					: searchdata.getName());

			sb.append("<div class='results' id='wr'>");
			if (wineset.othercountry) {
				sb.append("<h2>" + t.get("noresultsfound") + " for '" + searchdata.getName() + "' in "
						+ Webroutines.getCountryFromCode(searchdata.getCountry()) + ", showing " + wineset.records
						+ " results worldwide.</h2>");
			} else {
				if (!feedtest)
					sb.append("<h2>" + t.get("pricecomparisonfor") + " '"
							+ (Spider.escape(winename) + " " + (!wineset.othervintage ? searchdata.getVintage() : ""))
									.trim()
							+ "'"
							+ (searchdata.country.equals("All") ? "" : " in " + getCountryFromCode(searchdata.country))
							+ "</h2>\n");
				// if (singlevintage>0&&wineset.knownwineid>0) sb.append("<a
				// href='/wine/"+URLEncode(removeAccents(uniquewinename))+"'
				// title='"+winename+"'>View all vintages of "+winename+"</a><br/><br/>");
			}
			if (wineset.othervintage)
				sb.append("<h2>" + t.get("noresultsfound") + " for " + searchdata.getVintage()
						+ ", showing other vintages.</h2>");

			if (searchdata.freetextresults > 0)
				sb.append("<a class='freetext' href='" + thispage + "?keepdata=true&freetext=true'>[Show all "
						+ searchdata.freetextresults + " results for '" + searchdata.getName() + "']</a>");
			// sb.append("<div class='bookmark'>\n<script
			// type=\"text/javascript\">\naddthis_url =
			// 'https://www.vinopedia.com/wine/"+Webroutines.URLEncode(searchdata.getName())+"
			// "+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"';\naddthis_title
			// = document.title;\n addthis_pub = 'freewinesearcher';\n</script>\n<script
			// type=\"text/javascript\"
			// src=\"http://s7.addthis.com/js/addthis_widget.php?v=12\"
			// >\n</script>\n</div>");
			// sb.append("\n<div class='permalink'>\n<a
			// href='/wine/"+Webroutines.URLEncode(searchdata.getName())+(searchdata.getVintage().equals("")?"":"+")+searchdata.getVintage().replaceAll(";jsessionid.*$","").replace("'","&apos;")+"'>\n[
			// "+t.get("permalink")+" ]</a>&nbsp;</div>\n");
			if (wineset != null && wineset.Wine.length > 0 && Webroutines.getConfigKey("map").equals("true")) {
				if (map.equals("true")) {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\">Show results as list</a>");
				} else {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='true';document.getElementById('Searchform').submit();\"><img src='/images/bottles/Map.png' alt='Show on map'/>&nbsp;New! Show results on map</a>");
				}
			}

			String image = "sortasc.jpg";
			if (searchdata.getOrder().contains("desc"))
				image = "sortdesc.jpg";
			String pedia = "";
			// pedia=getWineStory(wineset.knownwineid,singlevintage,ri);
			// "+((searchdata.getOrder().contains("price")||searchdata.getOrder().equals(""))?"<img
			// class='sort' src='/css/"+sortorder+".jpg'/>":"")+"
			String mappane = "";
			if (map.equals("true") && wineset.bestknownwineid > 0) {
				// mappane="<div id='storelocator' style='width:825px;height:500px;border:1px
				// solid #4d0027;'></div>\n<script
				// type='text/javascript'>\n/*<![CDATA[*/\nfunction showmap(){
				// }\n/*]]>*/\n</script>";
				mappane = "<div id='storelocator' style='width:825px;height:500px;border:1px solid #4d0027;'></div>\n<script type='text/javascript'>\n/*<![CDATA[*/\nfunction showmap(){	if(document.getElementById(\"storelocator\")!=null&&document.getElementById(\"storelocator\").innerHTML==''){		var vpiframe = document.createElement( \"iframe\" );		if (document.getElementById(\"storelocator\").style.width=='') document.getElementById(\"storelocator\").style.width=\"950px\";		if (document.getElementById(\"storelocator\").style.height=='') document.getElementById(\"storelocator\").style.height=\"500px\";		vpiframe.setAttribute(\"frameborder\",\"0\");		vpiframe.setAttribute(\"scrolling\",\"no\");		vpiframe.setAttribute(\"width\",document.getElementById(\"storelocator\").style.width);		vpiframe.setAttribute(\"height\",document.getElementById(\"storelocator\").style.height);		vpiframe.setAttribute(\"overflow\",\"hidden\");		vpiframe.setAttribute( \"src\", \"\"+\"/storelocator.jsp?knownwineid="
						+ wineset.bestknownwineid
						+ (singlevintage > 0 && !wineset.othervintage ? "&vintage=" + singlevintage : "")
						+ "&width=\"+document.getElementById(\"storelocator\").style.width+\"&height=\"+document.getElementById(\"storelocator\").style.height+\"&showprices=true\");		document.getElementById(\"storelocator\").appendChild(vpiframe);	}}\n/*]]>*/\n</script>";
			}
			sb.append(
					"<ul class='tabs' id='resulttabs'><li><a href='?country=All&amp;keepdata=true' rel='nofollow'"
							+ (wineset.othercountry || searchdata.getCountry().equals("All")
									? " class='current resulttab'"
									: "")
							+ ">Worldwide</a></li><li><a href='?country=UC&amp;keepdata=true' rel='nofollow'"
							+ (!wineset.othercountry && searchdata.country.equals("UC") ? " class='current resulttab'"
									: "")
							+ ">USA/Canada</a></li><li><a href='?country=EU&amp;keepdata=true' rel='nofollow'"
							+ (!wineset.othercountry && searchdata.country.equals("EU") ? " class='current resulttab'"
									: "")
							+ ">Europe</a></li>"
							+ (!"".equals(searchdata.lastcountry)
									? ("<li><a href='?country=" + searchdata.lastcountry
											+ "&amp;keepdata=true' rel='nofollow' "
											+ (!wineset.othercountry
													&& searchdata.country.equals(searchdata.lastcountry)
															? "class='current resulttab'"
															: "")
											+ ">" + (wineset.othercountry ? "<strike>" : "")
											+ getCountryFromCode(searchdata.lastcountry)
											+ (wineset.othercountry ? "</strike>" : "") + "</a></li>")
									: "")
							+ (pedia.length() > 0
									? "<li><a href='#pedia' class='s resulttab'>Pedia</a></li>"
									: "")
							+ (mappane.length() > 0
									? "<li><a href='#map' onclick='javascript:showmap()' class='s resulttab'>Map</a></li>"
									: "")
							+ "</ul><div class='panes' id='gspanes'><div class='pan' id='pane1'>");
			sb.append("<div class='items' id='items1'>");
			sb.append((page < lastpage
					? "<div id='rnext' class='nextPage'><a href='?offset=" + (page) * numberofrows
							+ "&amp;keepdata=true' rel='nofollow'>Next</a></div>"
					: "")
					+ (page > 1
							? "<div id='rprev' class='prevPage nav'><a href='?offset=" + (page - 2) * numberofrows
									+ "&amp;keepdata=true' rel='nofollow'>Previous</a></div>"
							: ""));
			sb.append(
					"<div class='spriter spriter-resulth'><div class='sprite sprite-resulthlstr'></div><div class='shop'>Store</div><div class='winename'>Wine</div><div class='vintage'><a href='"
							+ thispage + "?keepdata=true&amp;order=vintage' rel='nofollow'>Vintage"
							+ ((searchdata.getOrder().contains("vintage")) ? "<img class='sort' alt='sort' src='"
									+ Configuration.cdnprefix + "/css2/" + image + "'/>" : "")
							+ "</a></div><div class='rating'>" + (feedtest ? "" : "Rating")
							+ "</div><div class='size'><a href='" + thispage
							+ "?keepdata=true&amp;order=size' rel='nofollow'>Size"
							+ ((searchdata.getOrder().contains("size")) ? "<img class='sort' alt='sort' src='"
									+ Configuration.cdnprefix + "/css2/" + image + "'/>" : "")
							+ "</a></div><div class='price'><a href='" + thispage
							+ "?keepdata=true&amp;order=priceeuroex' rel='nofollow'>Price"
							+ ((searchdata.getOrder().contains("priceeuro") || searchdata.getOrder().equals(""))
									? "<img class='sort' alt='sort' src='"
											+ Configuration.cdnprefix + "/css2/" + image + "'/>"
									: "")
							+ "</a></div><div class='currency sprite sprite-resulthr'></div></div>");
			sb.append("<table class=\"results\">");

			NumberFormat format = new DecimalFormat("#,##0.00");

			if (wineset.SponsoredWine != null && wineset.SponsoredWine.length > 0) {
				sb.append("<tr><td class='flag'></td>");
				sb.append("<td class='shop'><i>Featured merchants:</i></td>");
				sb.append("<td class='winename'></td>");
				sb.append("<td class='vintage'></td>");
				sb.append("<td class='rating'></td>");
				sb.append("<td class='size'></td>");
				sb.append("<td class='price' align='right'></td>");
				sb.append("<td class='currency' align='right'></td>");
				sb.append("</tr>");
				for (int i = 0; i < wineset.SponsoredWine.length; i++) {
					if (feedtest) {
						link = wineset.SponsoredWine[i].SourceUrl;
						shoplink = wineset.SponsoredWine[i].SourceUrl;
					} else {
						link = "'/'+['adhandler.jsp','type=Featured&amp;wineid=" + wineset.SponsoredWine[i].Id
								+ "'].join('?')";
						shoplink = "'/'+['adhandler.jsp','type=Featured&amp;wineid=" + wineset.SponsoredWine[i].Id
								+ "'].join('?')";
					}
					sb.append("\n<tr");
					if (i % 2 == 1) {
						sb.append(" class=\"sponsoredodd\"");
					} else {
						sb.append(" class=\"sponsoredeven\"");
					}
					sb.append(">");
					sb.append("<td class='flag'><span class='jslink' onclick=\"vpclick('/'+['"
							+ thispage.replaceAll("^/", "") + "','name="
							+ (Webroutines.URLEncode(searchdata.getName()) + "&amp;country="
									+ wineset.SponsoredWine[i].Country.toUpperCase() + "&amp;vintage="
									+ searchdata.getVintage()).replace("'", "&apos;")
							+ "&amp;keepdata=true'].join('?'))\");\"><img src='" + Configuration.cdnprefix
							+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
							+ wineset.SponsoredWine[i].Country.toLowerCase() + "'/></span></td>");
					sb.append("<td class='shop'><span class='jslink' onclick=\"vpclick(" + shoplink + ");\">"
							+ wineset.SponsoredWine[i].Shopname.replace("&", "&amp;") + "</span></td>");
					sb.append("<td class='winename'><span class='jslink' onclick=\"vpclick(" + link + ");\" >"
							+ Spider.escape(Webroutines.formatCapitals(wineset.SponsoredWine[i].Name))
							+ "</span></td>");
					sb.append("<td class='vintage'>"
							+ ("0".equals(wineset.SponsoredWine[i].Vintage) ? "" : wineset.SponsoredWine[i].Vintage)
							+ "</td>");
					sb.append("<td class='rating'>"
							+ ((rating = wineset.SponsoredWine[i].getAverageRating() + "").equals("0") ? "" : rating)
							+ "</td>");
					sb.append("<td class='size'>" + Webroutines.formatNewSize(wineset.SponsoredWine[i].Size) + "</td>");
					sb.append("<td class='price' align='right'>"
							+ Webroutines.formatPriceNoCurrency(wineset.SponsoredWine[i].PriceEuroIn,
									wineset.SponsoredWine[i].PriceEuroEx, searchdata.getCurrency(), searchdata.getVat())
							+ "</td>");
					sb.append("<td class='currency' align='right'>"
							+ Webroutines.getCurrencySymbol(searchdata.getCurrency()) + "</td>");
					if (showeditbuttons) {
						sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
								+ wineset.SponsoredWine[i].Id
								+ "&newknownwineid=-1' target='_blank'><img src='/images/trash.gif' alt='remove'/></a></td>");
						sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
								+ wineset.SponsoredWine[i].Id
								+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='edit'/></a></td>");
					}
					sb.append("</tr>");
				}
				sb.append("<tr><td class='flag'></td>");
				sb.append("<td class='shop'><i>All results:</i></td>");
				sb.append("<td class='winename'></td>");
				sb.append("<td class='vintage'></td>");
				sb.append("<td class='rating'></td>");
				sb.append("<td class='size'></td>");
				sb.append("<td class='price' align='right'></td>");
				sb.append("<td class='currency' align='right'></td>");
				sb.append("</tr>");

			} else if (false) {
				sb.append("<tr><td colspan='4'><i>Sponsored results:</i></td></tr>");
				sb.append("<tr class=\"sponsoredeven\">");
				sb.append("<td colspan='2'><a href='" + response.encodeURL("/sponsoring.jsp") + "'></a></td>");
				sb.append("<td colspan='2'><a href='" + response.encodeURL("/sponsoring.jsp")
						+ "'>Your wine could be listed here! Click for more information.</a></td>");
				sb.append("<td align='right'></td>");
				sb.append("<td align='right'>&euro; 0.10</td>");
				sb.append("</tr>");
				sb.append("<tr><td colspan='4'><i>All results:</i></td></tr>");
			}
			// Give the complete result list
			for (int i = 0; i < wineset.Wine.length; i++) {
				if (feedtest) {
					link = wineset.Wine[i].SourceUrl;
					shoplink = wineset.Wine[i].SourceUrl;
				} else {
					link = "['','store','" + URLEncode(removeAccents(wineset.Wine[i].Shopname)).replaceAll("%2F", "/")
							+ "','?wineid=" + wineset.Wine[i].Id + "'].join('/')";
					shoplink = "['','store','"
							+ URLEncode(removeAccents(wineset.Wine[i].Shopname)).replaceAll("%2F", "/") + "','?wineid="
							+ wineset.Wine[i].Id + "'].join('/')";
					// shoplink="/store/"+URLEncode(removeAccents(wineset.Wine[i].Shopname)).replaceAll("%2F",
					// "/")+"/?wineid="+wineset.Wine[i].Id;
				}
				sb.append("\n<tr");
				if (i % 2 == 0) {
					sb.append(" class=\"odd\"");
				} else {
					sb.append(" class=\"even\"");
				}
				sb.append(">");
				// sb.append("<td class='flag'><span class='jslink'
				// onclick='vpclick(\""+thispage+"?name="+(Webroutines.URLEncode(searchdata.getName())+"&amp;country="+wineset.Wine[i].Country.toUpperCase()+"&amp;vintage="+searchdata.getVintage()).replace("'","&apos;")+"&amp;keepdata=true\");'><img
				// src='"+Configuration.cdnprefix+"/images/transparent.gif' alt='country'
				// class='sprite flag
				// sprite-"+wineset.Wine[i].Country.toLowerCase()+"'/></span></td>");
				sb.append("<td class='flag'><span class='jslink' onclick=\"vpclick('/'+['"
						+ thispage.replaceAll("^/", "") + "','name="
						+ (Webroutines.URLEncode(searchdata.getName()) + "&amp;country="
								+ wineset.Wine[i].Country.toUpperCase() + "&amp;vintage=" + searchdata.getVintage())
										.replace("'", "&apos;")
						+ "&amp;keepdata=true'].join('?'))\");\"><img src='" + Configuration.cdnprefix
						+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
						+ wineset.Wine[i].Country.toLowerCase() + "'/></span></td>");
				sb.append("<td class='shop'><span class='jslink' onclick='vpclick(" + shoplink + ");'>"
						+ wineset.Wine[i].Shopname.replace("&", "&amp;") + "</span></td>");
				sb.append("<td class='winename'><span class='jslink' onclick='vpclick(" + link + ");' >"
						+ Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)) + " </span></td>");
				sb.append("<td class='vintage'>" + ("0".equals(wineset.Wine[i].Vintage) ? "" : wineset.Wine[i].Vintage)
						+ "</td>");
				sb.append("<td class='rating'>"
						+ ((rating = wineset.Wine[i].getAverageRating() + "").equals("0") ? "" : rating) + "</td>");
				sb.append("<td class='size'>" + Webroutines.formatNewSize(wineset.Wine[i].Size) + "</td>");
				sb.append(
						"<td class='price' align='right'>"
								+ Webroutines.formatPriceNoCurrency(wineset.Wine[i].PriceEuroIn,
										wineset.Wine[i].PriceEuroEx, searchdata.getCurrency(), searchdata.getVat())
								+ "</td>");
				sb.append("<td class='currency' align='right'>"
						+ Webroutines.getCurrencySymbol(searchdata.getCurrency()) + "</td>");
				if (showeditbuttons) {
					sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
							+ wineset.Wine[i].Id
							+ "&newknownwineid=-1' target='_blank'><img src='/images/trash.gif' alt='remove'/></a></td>");
					sb.append("<td class='currency'><a href='/admin/ai/correctknownwineid.jsp?wineid="
							+ wineset.Wine[i].Id
							+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='edit'/></a></td>");
				}
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("<div class='resultf spriter spriter-resultf'><div class='pages sprite sprite-resultfl'>"
					+ t.get("page") + "&nbsp;" + page + " " + t.get("of") + " " + lastpage
					+ "</div><div class='sprite sprite-resultfr'>&nbsp;</div><div class='pageselector'>");
			if (page > 1)
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((page - 2) * numberofrows)
						+ "' rel='nofollow'>" + t.get("previous") + "</a>&nbsp;&nbsp;&nbsp;");
			if (startpage > 1)
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=0' rel='nofollow'>1</a>&hellip;");
			for (int i = startpage; i <= endpage; i++) {
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((i - 1) * numberofrows)
						+ "' rel='nofollow'>" + i + "</a>");
			}
			if (lastpage > endpage)
				sb.append("&hellip;<a href='" + thispage + "?keepdata=true&amp;offset=" + (lastpage - 1) * numberofrows
						+ "'>" + lastpage + "</a>");
			if (page < lastpage)
				sb.append("<a href='" + thispage + "?keepdata=true&amp;offset=" + ((page) * numberofrows)
						+ "'>&nbsp;&nbsp;&nbsp;" + t.get("next") + "</a>");
			sb.append("</div></div>");
			if (mappane.length() > 0)
				sb.append(getRefineNarrow(wineset, t, thispage, searchdata));
			sb.append("</div><!--items-->"); // items
			sb.append("</div>"
					+ (pedia.length() > 0
							? "\n<div class='pan' id='pedia'><div class='items' id='items2'><div class='page'>" + pedia
									+ "</div></div><!--items--></div>"
							: "")
					+ (mappane.length() > 0
							? "\n<div class='pan' id='map'><div class='items' id='items2'><div class='page'>" + mappane
									+ "</div></div><!--items--></div>"
							: "")); // panes
			// sb.append("</div>\n</div>");

		} else {
			sb.append("<h2>" + t.get("noresultsfound") + " for '" + searchdata.getName() + "'</h2>");

			if (wineset.knownwineid == 0 && searchdata.getCountry().equals("")) {
				ArrayList<String> alternatives = com.freewinesearcher.common.Knownwines
						.getNewAlternatives(searchdata.getName());
				if (alternatives.size() > 0) {
					sb.append("<div class='alternatives'>");
					sb.append("<h2>" + t.get("alternatives") + "</h2>");
					sb.append(
							"<div class='alternativesh'><div class='left'></div><div class='winename'>Wine</div><div class='region'>Region</div><div class='hits'>Results</div></div>");
					sb.append("<table class=\"alternatives\">");
					for (int i = 0; i < alternatives.size(); i = i + 4) {
						sb.append("<tr");
						if ((i / 4) % 2 == 1) {
							sb.append(" class=\"odd\"");
						}
						sb.append("><td class='winename'><a href='/wine/"
								+ Webroutines.URLEncode(alternatives.get(i + 2)) + "'>" + alternatives.get(i)
								+ "</a></td><td class='region'>" + alternatives.get(i + 1) + "</td><td class='number'>"
								+ alternatives.get(i + 3) + "</td></tr>");
					}
					sb.append("</table>");
					sb.append("<div class='alternativesf'><div class='left'></div><div class='right'></div></div>");
					// sb.append("<div class='alternatives'>");
				}
			}
		}
		return sb.toString();

	}

	public static String currenciesOptions(String selected) {
		if (selected == null)
			selected = "EUR";
		StringBuffer sb = new StringBuffer();
		sb.append(
				"<option value=\"EUR\"" + (selected.equals("EUR") ? " selected=\"selected\"" : "") + ">Euro</option>");
		sb.append("<option value=\"USD\"" + (selected.equals("USD") ? " selected=\"selected\"" : "")
				+ ">US Dollar</option>");
		sb.append("<option value=\"CAD\"" + (selected.equals("CAD") ? " selected=\"selected\"" : "")
				+ ">Canadian Dollar</option>");
		sb.append("<option value=\"GBP\"" + (selected.equals("GBP") ? " selected=\"selected\"" : "")
				+ ">British Pound</option>");
		sb.append("<option value=\"CHF\"" + (selected.equals("CHF") ? " selected=\"selected\"" : "")
				+ ">Swiss Frank</option>");
		sb.append("<option value=\"AUD\"" + (selected.equals("AUD") ? " selected=\"selected\"" : "")
				+ ">Australian Dollar</option>");
		sb.append("<option value=\"NZD\"" + (selected.equals("NZD") ? " selected=\"selected\"" : "")
				+ ">NZ Dollar</option>");
		sb.append("<option value=\"SGD\"" + (selected.equals("SGD") ? " selected=\"selected\"" : "")
				+ ">Singapore Dollar</option>");
		sb.append("<option value=\"CNY\"" + (selected.equals("CNY") ? " selected=\"selected\"" : "")
				+ ">Yuan Renminbi</option>");
		sb.append("<option value=\"HKD\"" + (selected.equals("HKD") ? " selected=\"selected\"" : "")
				+ ">Hong Kong Dollar</option>");
		sb.append("<option value=\"JPY\"" + (selected.equals("JPY") ? " selected=\"selected\"" : "") + ">Yen</option>");
		sb.append("<option value=\"SEK\"" + (selected.equals("SEK") ? " selected=\"selected\"" : "")
				+ ">Swedish Krona</option>");
		sb.append("<option value=\"NOK\"" + (selected.equals("NOK") ? " selected=\"selected\"" : "")
				+ ">Norwegian Krone</option>");
		sb.append("<option value=\"DKK\"" + (selected.equals("DKK") ? " selected=\"selected\"" : "")
				+ ">Danish Krone</option>");
		sb.append("<option value=\"ZAR\"" + (selected.equals("ZAR") ? " selected=\"selected\"" : "")
				+ ">SA Rand</option>");
		return sb.toString();
	}

	public static String getRefineNarrow(Wineset wineset, Translator t, String searchpage, Searchdata searchdata) {
		String refineHTML = Webroutines.getRefineResultsHTML(searchpage, wineset, t, searchdata, 20);
		ArrayList<String> countries = Webroutines.getCountries();
		StringBuffer sb = new StringBuffer();
		if (wineset.records > 0) {
			sb.append(
					"<div class='clear'></div><div class='refine narrow spriter-refine spriter'><div class='refinel sprite-refinel sprite'></div>");
			sb.append(
					"<div class='refinewines' onclick='javascript:show(\"refinelist\");' onmouseout='javascript:hide(\"refinelist\");'>");
			sb.append(
					"<div id='refinelist' class='refinelist' onmouseover='show(\"refinelist\");' onmouseout='hide(\"refinelist\");'>"
							+ refineHTML + "</div>");
			if (!"".equals(refineHTML))
				sb.append(t.get("wines") + "<img src='" + Configuration.cdnprefix
						+ "/css2/arrowgreen.jpg' alt='refine'/>");
			sb.append("</div>");
			sb.append("<form id='refineform' action='" + searchpage + "' method='post'><div class='text'>"
					+ t.get("country")
					+ "</div><select name=\"country\" onchange='javascript:document.getElementById(\"refineform\").submit();'>");
			sb.append("<option value=\"All\" ");
			if (searchdata.getCountry().equals("All"))
				sb.append(" selected=\"selected\"");
			sb.append(">" + t.get("all") + "</option><option value=\"EU\"");
			if (searchdata.getCountry().equals("EU"))
				sb.append(" selected=\"selected\"");
			sb.append(">Europe</option>");
			for (int i = 0; i < countries.size(); i = i + 2) {
				sb.append("<option value='" + countries.get(i) + "'"
						+ ((searchdata.getCountry().equals(countries.get(i))) ? (" selected=\"selected\"") : "") + ">"
						+ countries.get(i + 1) + "</option>");
			}
			sb.append("</select><div class='text'>" + t.get("vintage")
					+ "</div><select name='vintage'  onchange='javascript:document.getElementById(\"refineform\").submit();'>");
			sb.append("<option value=\"All\""
					+ (searchdata.getVintage().equals("All") || searchdata.getVintage().equals("")
							? (" selected=\"selected\"")
							: "")
					+ ">" + t.get("all") + "</option>");
			for (int vintage : wineset.vintages) {
				sb.append("<option value=\"" + vintage + "\""
						+ (searchdata.getVintage().equals(vintage + "") ? (" selected=\"selected\"") : "") + ">"
						+ vintage + "</option>");
			}
			sb.append("</select><div class='text'>" + t.get("size")
					+ "</div><select name='size'  onchange='javascript:document.getElementById(\"refineform\").submit();'><option value=\"All\""
					+ (searchdata.getSize() == null || searchdata.getSize() == (float) 0 ? (" selected=\"selected\"")
							: "")
					+ ">" + t.get("all") + "</option>");
			for (Float size : wineset.sizes) {
				sb.append("<option value=\"" + size + "\""
						+ (searchdata.getSize().equals(size) ? (" selected=\"selected\"") : "") + ">"
						+ Webroutines.formatSize(size) + "</option>");
			}
			sb.append("</select><div class='text'>" + t.get("displaycurrency")
					+ "</div><select name=\"currency\" onchange='javascript:document.getElementById(\"refineform\").submit();'>");
			sb.append(currenciesOptions(searchdata.getCurrency()));

			sb.append(
					"</select><input type='hidden' name='keepdata' value='true'/></form><div class='refiner sprite-refiner sprite'></div>");
			sb.append("<img class='sprite sprite-greengo greengo' src='" + Configuration.cdnprefix
					+ "/images/transparent.gif' onclick='document.getElementById(\"refineform\").submit()' alt='Go'/></div>");
		}
		return sb.toString();
	}

	public static String getWineRecommendationsHTML(Wineset wineset, Translator t, Searchdata searchdata,
			int numberofrows, HttpServletResponse response, String map, boolean sponsoredresults,
			Wineset sponsoredwineset, String thispage, Shoppingcart cart) {
		StringBuffer sb = new StringBuffer();
		if (wineset.records > 0) {
			if (wineset != null && wineset.Wine.length > 0 && Webroutines.getConfigKey("map").equals("true")) {
				if (map.equals("true")) {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='false';document.getElementById('Searchform').submit();\">Show results as list</a>");
				} else {
					sb.append(
							"&nbsp;&nbsp;<a href=\"javascript:document.getElementById('map').value='true';document.getElementById('Searchform').submit();\"><img src='/images/bottles/Map.png' alt='Show on map'/>&nbsp;New! Show results on map</a>");
				}
			}
			if (!map.equals("true")) {
				if (wineset.Wine.length > 0) {
					sb.append("<h1>Our recommended wines from this seller</h1>");
					String image = "sortasc.jpg";
					if (!cart.getOrder().contains("priceeuro") && !cart.getOrder().equals("vintage")
							&& !cart.getOrder().equals("size"))
						image = "sortdesc.jpg";

					// "+((searchdata.getOrder().contains("price")||searchdata.getOrder().equals(""))?"<img
					// class='sort' src='/css2/"+sortorder+".jpg'/>":"")+"
					sb.append(
							"<div class='results'><div class='recommendation'><div class='resulth'><div class='flag'></div><div class='winename'>Wine</div><div class='vintage'><a href='"
									+ thispage + "?order=vintage'>Vintage"
									+ ((cart.getOrder().contains("vintage"))
											? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
											: "")
									+ "</a></div><div class='rating'><a href='" + thispage + "?order=rating'>Rating"
									+ ((cart.getOrder().contains("rating"))
											? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
											: "")
									+ "</a></div><div class='pq'><a href='" + thispage + "?order=pq'>P/Q"
									+ ((cart.getOrder().contains("pq"))
											? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
											: "")
									+ "</a></div><div class='relprice'><a href='" + thispage + "?order=relprice'>Price"
									+ ((cart.getOrder().contains("relprice"))
											? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
											: "")
									+ "</a></div><div class='size'><a href='" + thispage + "?order=size'>Size"
									+ ((cart.getOrder().contains("size"))
											? "<img class='sort' alt='sort' src='/css2/" + image + "'/>"
											: "")
									+ "</a></div><div class='price'><a href='" + thispage
									+ "?keepdata=true&amp;order=priceeuroex'>Price"
									+ ((cart.getOrder().contains("priceeuro"))
											? "<img class='sort' alt='sort' src='/css/" + image + "'/>"
											: "")
									+ "</a></div><div class='currency'></div></div></div>");
					sb.append("<table class=\"results\">");

					NumberFormat format = new DecimalFormat("#,##0.00");

					String rating;
					String pqratio;
					// Give the complete result list
					for (int i = 0; i < wineset.Wine.length; i++) {
						sb.append("\n<tr");

						if (wineset.Wine[i].CPC > 0 && sponsoredresults) {
							sb.append(" class=\"sponsoredeven\"");
						} else {
							if (i % 2 == 1) {
								sb.append(" class=\"odd\"");
							}
						}
						sb.append(">");
						sb.append("<td class='winename'><a href='shoppingassistant.jsp?wineid=" + wineset.Wine[i].Id
								+ "' title='"
								+ Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)).replace("'", "&apos;")
								+ "'>" + Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name)) + "</a></td>");
						sb.append("<td class='vintage'>"
								+ (wineset.Wine[i].Vintage.equals("0") ? "" : wineset.Wine[i].Vintage) + "</td>");
						sb.append("<td class='rating'>"
								+ ((rating = wineset.Wine[i].getAverageRating() + "").equals("0") ? "" : rating)
								+ "</td>");
						sb.append("<td class='rating'>"
								+ (wineset.Wine[i].pqratio > 1 ? "<img src='/css/starsmall.gif'/>" : "")
								+ (wineset.Wine[i].pqratio > 1.5 ? "<img src='/css/starsmall.gif'/>" : "")
								+ (wineset.Wine[i].pqratio > 2 ? "<img src='/css/starsmall.gif'/>" : "") + "</td>");
						sb.append("<td class='rating'>"
								+ (wineset.Wine[i].relativeprice < 1.2 ? "<img src='/css/starsmall.gif'/>" : "")
								+ (wineset.Wine[i].relativeprice < 1.1 ? "<img src='/css/starsmall.gif'/>" : "")
								+ (wineset.Wine[i].relativeprice < 1.01 ? "<img src='/css/starsmall.gif'/>" : "")
								+ "</td>");
						sb.append("<td class='size'>" + Webroutines.formatNewSize(wineset.Wine[i].Size) + "</td>");
						sb.append("<td class='price' align='right'>"
								+ Webroutines.formatPriceNoCurrency(wineset.Wine[i].PriceEuroIn,
										wineset.Wine[i].PriceEuroEx, searchdata.getCurrency(), searchdata.getVat())
								+ "</td>");
						sb.append("<td class='currency' align='right'>"
								+ Webroutines.getCurrencySymbol(searchdata.getCurrency()) + "</td>");
						sb.append("</tr>");
					}
					sb.append("</table>");
					int page = searchdata.getOffset() / numberofrows + 1;
					int lastpage = wineset.records / numberofrows + 1;
					int startpage = page - 2;
					if (startpage < 1)
						startpage = 1;
					int endpage = startpage + 4;
					if (endpage > lastpage) {
						endpage = lastpage;
						startpage = endpage - 4;
						if (startpage < 1)
							startpage = 1;
					}
					sb.append(
							"<div class='resultf'><div class='pages'></div><div class='pageselector'></div></div></div>");
				}

			}
		}
		return sb.toString();

	}

	public static HashSet<String> getRandomImages(String path, int n) {
		File file = new File(path);
		String[] files = file.list(new JpgFilter());
		HashSet<String> images = new HashSet<String>();
		int image = 0;
		for (int i = 0; i < Math.min(n, files.length); i++) {
			image = 0;
			while (image == 0) {
				image = (int) Math.floor(Math.random() * files.length);
				if (!images.contains(files[image])) {
					images.add(files[image]);
				} else {
					image = 0;
				}
			}
		}
		return images;

	}

	static class JpgFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".jpg");
		}
	}

	public static String getWineTipsHTML(Translator t, int numberofrows, String searchpage, Searchdata s) {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		StringBuffer sb = new StringBuffer();
		String uniquename;
		String link;

		try {
			sb.append("<div class='results'>");
			sb.append(
					"<div class='spriter spriter-resulth'><div class='roundwinename'>Wine</div><div class='region'>Region</div><div class='vintage'>Vintage</div><div class='rating'>Rating</div><div class='size'>Size</div><div class='oldprice'>From</div><div class='price'>New Price</div><div class='currency'></div></div>");
			sb.append("<table class=\"results\">");

			String query = "select tips.*, kbregionhierarchy.shortregion as regionname, country from tips  join wineview on (tips.wineid=wineview.id) join kbregionhierarchy on (tips.region=kbregionhierarchy.id) order by lowestprice/nextprice;";
			rs = Dbutil.selectQuery(query, con);

			NumberFormat format = new DecimalFormat("#,##0.00");

			int rating;
			// Give the complete result list
			int i = 0;
			while (rs.next()) {
				sb.append("<tr");
				rating = Winerating.getRating(rs.getInt("knownwineid"), rs.getInt("vintage"));
				uniquename = Knownwines.getUniqueKnownWineName(rs.getInt("knownwineid"));
				link = winelink(uniquename, rs.getInt("Vintage"));

				if (i % 2 == 1) {
					sb.append(" class=\"odd\"");
				}
				sb.append(">");
				sb.append("<td class='flag'><a href='" + link + "'><img src='" + Configuration.cdnprefix
						+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
						+ rs.getString("country").toLowerCase() + "'/></a></td>");
				sb.append(
						"<td class='winename'><a href='" + link + "'>"
								+ Webroutines.formatCapitals(
										Spider.escape(Knownwines.getKnownWineName(rs.getInt("knownwineid"))))
								+ "</a></td>");
				sb.append("<td class='region'>" + rs.getString("regionname") + "</td>");
				sb.append("<td class='vintage'>" + (rs.getInt("vintage") == 0 ? "" : rs.getInt("vintage")) + "</td>");
				sb.append("<td class='rating'>" + (rating == 0 ? "" : rating) + "</td>");
				sb.append("<td class='size'>" + Webroutines.formatNewSize(rs.getFloat("Size")) + "</td>");
				sb.append("<td class='oldprice' align='right'>" + Webroutines.formatPrice(rs.getDouble("nextprice"),
						rs.getDouble("nextprice"), s.getCurrency(), "EX") + "</td>");
				sb.append("<td class='price' align='right'>" + Webroutines.formatPrice(rs.getDouble("lowestprice"),
						rs.getDouble("lowestprice"), s.getCurrency(), "EX") + "</td>");
				sb.append("<td class='currency' align='right'>" + Webroutines.getCurrencySymbol("EUR") + "</td>");
				sb.append("</tr>");
				i++;
			}
			sb.append("</table></div>");
		} catch (Exception e) {
			Dbutil.logger.error("Problem creating all tips.html", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return sb.toString();

	}

	public static String getRefineResultsHTML(String thispage, Wineset wineset, Translator t, Searchdata searchdata,
			int maxresults) {
		StringBuffer html = new StringBuffer();
		int i = 1;
		if (wineset.knownwinelist.size() > 1) {
			html.append("<table>");
			for (int knownwineid : wineset.knownwinelist.keySet()) {
				if (i <= maxresults) {
					if (knownwineid > 0) {
						i++;
						html.append("<tr" + (i % 2 == 1 ? " class='odd'" : "") + "><td>"
								+ Knownwines.getImageTag(knownwineid) + "</td><td><a href='" + thispage + "?name="
								+ Webroutines.URLEncode(Knownwines.getUniqueKnownWineName(knownwineid)) + "+"
								+ searchdata.getVintage() + "'>"
								+ (knownwineid == 0 ? "Unrecognized wines" : (Knownwines.getKnownWineName(knownwineid)))
								+ "</a></td></tr>");
					}
				}
			}
			html.append("</table>");
		}
		return html.toString();

	}

	public static String getRefineResultsMobile(PageHandler p, int maxresults, boolean allwines) {
		StringBuffer html = new StringBuffer();
		int i = 1;
		if (p.s.wineset.knownwinelist.size() > 1) {
			html.append("<ul data-role='listview' data-theme='a'>");
			if (allwines) {
				html.append(
						"<li><a rel='external' href='" + p.searchpage + "?name=" + URLEncodeUTF8(p.searchdata.getName())
								+ "&amp;freetext=true'>All wines with \"" + p.searchdata.getName()
								+ "\" in name<span class='ui-li-count'>" + p.s.wineset.records + "</span></a></li>");
			}
			for (int knownwineid : p.s.wineset.knownwinelist.keySet()) {
				if (i <= maxresults) {
					if (knownwineid > 0) {
						i++;
						html.append("<li><a rel='external' href='"
								+ winelink(Knownwines.getUniqueKnownWineName(knownwineid), p.s.singlevintage, true)
								+ "'>" + Knownwines.getKnownWineName(knownwineid) + "<span class='ui-li-count'>"
								+ p.s.wineset.knownwinelist.get(knownwineid) + "</span></a></li>");
					}
				}
			}
			html.append("</ul>");
		}
		return html.toString();

	}

	public static String getSpecialPageLinks(String winename) {
		if (winename == null)
			return "";
		if (winename.toLowerCase().contains("moscato"))
			return "<div class='pricenote'><a href='/moscato%20wine.jsp' title='Moscato Wine'>Moscato wine</a></div>";
		if (winename.toLowerCase().contains("beaujolais") && winename.toLowerCase().contains("nouveau")) {
			int vintage = java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
			if (java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.MONTH) < 9)
				vintage = vintage - 1;
			return "<div class='pricenote'><a href='/beaujolais nouveau.jsp' title='Beaujolais Nouveau " + vintage
					+ "'>Beaujolais Nouveau " + vintage + "</a></div>";
		}
		return "";
	}

	public static String getSuggestionHTML(int knownwineid, Searchdata searchdata, String thispage) {

		StringBuffer html = new StringBuffer();
		String result = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (knownwineid > 0) {

				WineAdvice advice = new WineAdvice();
				advice.setResultsperpage(15);
				if (!searchdata.getCountry().equals("")) {
					advice.setCountryofseller(searchdata.getCountry());
				} else {
					advice.setCountryofseller("All");
				}
				advice.setCurrency(searchdata.getCurrency());
				if (Configuration.newrecognitionsystem) {
					advice.setRegion(Dbutil.readValueFromDB(
							"Select * from knownwines join kbregionhierarchy on (knownwines.locale=kbregionhierarchy.region) where knownwines.id="
									+ knownwineid + ";",
							"shortregion"));
				} else {
					advice.setRegion(Dbutil.readValueFromDB("Select * from knownwines where id=" + knownwineid + ";",
							"appellation"));
				}
				if (!"Unknown".equals(advice.getRegion())) {
					rs = Dbutil.selectQueryFromMemory(
							"select min(priceeuroex) as priceeuroex from materializedadvice where knownwineid="
									+ knownwineid + ";",
							"materializedadvice", con);
					float price = (float) 0;
					if (rs.next())
						price = rs.getFloat("priceeuroex");
					if (price > 0)
						advice.setPricemax(price * 2);
					// if (price>0) advice.setPricemin(price/2);
					advice.setSearchtype(Searchtypes.Q);
					advice.setSubregions(true);
					advice.excludewineid = knownwineid;
					advice.getAdvice();
				}
				if (advice != null && advice.wine != null) {
					html.append("<table class='suggestiontable'>");
					int odd = 1;
					for (int i = 0; i < advice.wine.length; i++) {
						if (advice.wine[i].Knownwineid != knownwineid
								|| !advice.wine[i].Vintage.equals(searchdata.vintage)) {
							odd++;
							html.append("<tr" + (odd % 2 == 1 ? " class='odd'" : "") + ">");
							html.append("<td>" + Knownwines.getImageTag(advice.wine[i].Knownwineid) + "</td>");
							html.append("<td><a href='/wine/"
									+ Webroutines.URLEncode(Knownwines.getKnownWineName(advice.wine[i].Knownwineid))
									+ "?vintage=" + advice.wine[i].Vintage + "&amp;suggestion=true'>"
									+ Knownwines.getKnownWineName(advice.wine[i].Knownwineid) + " "
									+ advice.wine[i].Vintage + "</a></td>");
							html.append("<td>" + advice.rating[i] + "&nbsp;points</td>");
							html.append("<td>" + Webroutines.formatPrice(advice.wine[i].PriceEuroIn,
									advice.wine[i].PriceEuroEx, advice.getCurrency(), "EX") + "</td>");
							html.append("</tr>");
						}
					}
					html.append("</table>");
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving scrapelist. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		result = html.toString();
		if (result.length() < 100)
			result = result.replaceAll(".*<table class='suggestions'></table>.*", ""); // if no results found

		return result;
	}

	public static String respontojsinteraction(HttpServletRequest request) {
		String response = "";
		String action = request.getParameter("action");
		if (action != null) {
			if (action.equals("setAttribute")) {
				String name = request.getParameter("name");
				String value = request.getParameter("value");
				if (name != null && value != null) {
					if (name.equals("showproducers") || name.equals("showsubregions")) {
						HttpSession session = request.getSession(true);
						session.setAttribute(name, value);
					}
				}
			}
			if (action.equals("getallproducers")) {
				Connection con = null;
				try {
					int region = Integer.parseInt(request.getParameter("regionid"));
					Regioninfo ri = new Regioninfo(region);
					ri.producerlimit = 0;
					con = Dbutil.openNewConnection();
					response = ri.getProducers(con).toString();

				} catch (Exception e) {
					Dbutil.logger.error("Problem: ", e);
				} finally {
					Dbutil.closeConnection(con);
				}

			}
		}
		return response;

	}

	public static String getShopstodo() {
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("<table>");
			String query = "select * from shopapplication sa join wsshops ws on (sa.id=ws.wsid) order by sa.status desc, date desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				Shopapplication sa = Shopapplication.retrieve(rs.getLong("wsid"));
				int n = 0;
				if (rs.getInt("shopid") > 0)
					n = Dbutil.readIntValueFromDB(
							"SELECT count(*) as thecount from wines where shopid=" + rs.getInt("shopid"), "thecount");
				sb.append("<tr><td>" + rs.getString("name") + "</td><td>" + (n > 0 ? n : "") + "</td><td>"
						+ rs.getString("date") + "</td><td>" + rs.getString("sa.status") + "</td><td>"
						+ (rs.getInt("shopid") == 0
								? "<a href='editshop.jsp?wsid=" + rs.getString("wsid") + "'target='_blank'>Add shop</a>"
								: "<a href='editshop.jsp?shopid=" + rs.getString("shopid")
										+ "&actie=retrieve' target='_blank'>Edit shop</a>")
						+ "</td><td><a href='/getstoreinvitation.jsp?id=" + rs.getString("wsid") + "&password="
						+ sa.showPassword() + " 'target='_blank'>Show form</a></td>"
						+ (rs.getInt("shopid") == 0 ? "<td></td><td></td>"
								: "<td><a href='edittablescraper.jsp?shopid=" + rs.getString("shopid") + "&wsid="
										+ rs.getString("wsid") + "&actie=retrieve&url=" + sa.getUrlbrowsethrough()
										+ "' target='_blank'>Edit table scraper</a></td><td><a href='/settings/editdatafeed.jsp?shopid="
										+ rs.getString("shopid") + "&wsid=" + rs.getString("wsid")
										+ "&actie=retrieve&url=" + sa.getUrldatafeed()
										+ "' target='_blank'>Edit data feed</a></td>")
						+ "<td><form action='/admin/setshopcomment.jsp' target='_blank'><input type='hidden' name='wsid' value='"
						+ rs.getInt("wsid") + "'/><input type='text' style='width:250px;' name='comment' value='"
						+ rs.getString("comment")
						+ "'/><input type='submit' name='actie' value='update'/></form></td></tr>");
			}
			sb.append("</table>");

		} catch (Exception e) {
			Dbutil.logger.error("Error while retrieving shops to do list. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static String getAuthorNote(String author) {
		if ("RP".equals(author)) {
			return "&#169; The Wine Advocate, full review available on <a href='http://eRobertParker.com' target='_blank'>eRobertParker.com</a>.<br/>";
		}
		if ("WS".equals(author)) {
			return "&#169; Wine Spectator, full review available on <a href='http://www.winespectator.com' target='_blank'>WineSpectator.com</a>.<br/>";
		}
		if ("B&D".equals(author)) {
			return "&#169; Bettane &amp; Desseauve, <a href='http://www.bettanedesseauve.com' target='_blank'>bettanedesseauve.com</a>.<br/>";
		}
		if ("IWC".equals(author)) {
			return "&#169; Stephen Tanzer, <a href='http://www.wineaccess.com/expert/tanzer/newhome.html' target='_blank'>International Wine Cellar</a>.<br/>";
		}

		return "";
	}

	public static String getAuthorName(String author) {
		if ("RP".equals(author)) {
			return "Robert Parker";
		}
		if ("WS".equals(author)) {
			return "Wine Spectator";
		}
		if ("B&D".equals(author)) {
			return "Bettane &amp; Desseauve";
		}
		if ("IWC".equals(author)) {
			return "Stephen Tanzer";
		}
		if ("Dec".equals(author)) {
			return "Decanter";
		}

		return author;
	}

	public static String getAuthorScale(String author) {
		if ("RP".equals(author)) {
			return " out of 100";
		}
		if ("WS".equals(author)) {
			return " out of 100";
		}
		if ("B&D".equals(author)) {
			return "";
		}
		if ("IWC".equals(author)) {
			return " out of 100";
		}
		if ("Dec".equals(author)) {
			return "";
		}

		return author;
	}

	public static RatingInfo oldgetNewRatingsHTML(int knownwineid, int width, String searchpage, int singlevintage,
			Searchdata searchdata, Translator t) {
		String html = "";
		StringBuffer sb = new StringBuffer();
		ResultSet rs = null;
		WineLibraryTV wltv = null;
		String query;
		String authorinfo = "";
		String goodpqvintages = "";
		HashMap<String, String> map = new HashMap<String, String>();
		TreeSet<Integer> vintages = new TreeSet<Integer>();
		HashSet<String> authors = new HashSet<String>();
		Connection con = Dbutil.openNewConnection();
		if (knownwineid > 0) {
			if (true || Dbutil.readIntValueFromDB(
					"select count(*) as thecount from ratedwines where knownwineid=" + knownwineid + " and rating>70;",
					"thecount") > 0) {
				int currentvintage;
				int startvintage = 0;
				String singlevintageclause = "";
				String cellclass;
				try {
					// See if there is a label
					String labeldiv = "";
					if (new File(Configuration.basedir + "images\\wines\\" + knownwineid + ".gif").exists()) {
						labeldiv = "<div class='label'><img src='/images/wines/" + knownwineid + ".gif' alt='"
								+ Knownwines.getKnownWineName(knownwineid).replace("'", "&apos;")
								+ "' onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
					} else {
						if (new File(Configuration.basedir + "images\\wines\\" + knownwineid + ".jpg").exists()) {
							labeldiv = "<div class='label'><img src='/images/wines/" + knownwineid + ".jpg' alt=\""
									+ Knownwines.getKnownWineName(knownwineid)
									+ "\" onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
						}
					}
					wltv = new WineLibraryTV(knownwineid, singlevintage + "");

					// wine div
					sb.append("<div class='winecontainer'>" + "<img class='wineglass' src='/css/"
							+ Knownwines.getGlassImage(knownwineid)[0] + "' alt='"
							+ Knownwines.getGlassImage(knownwineid)[1] + "'/>" + "<div class='wine'>" + labeldiv
							+ (false ? "<div class='tastingnote' onclick='javascript:showTNtop(" + knownwineid + ",\""
									+ searchdata.getVintage()
									+ "\");' onmouseout='javascript:hidetop(\"toptastingnote\")'>"
									+ "<div id='toptastingnote' class='tastingnotes' onmouseover='javascript:showTNtop("
									+ knownwineid + ",\"" + searchdata.getVintage()
									+ "\");' onmouseout='javascript:hidetop(\"toptastingnote\");'><div id='tncontent' class='tncontent' onmouseover='javascript:showTNtop("
									+ knownwineid + ",\"" + searchdata.getVintage()
									+ "\");' onmouseout='javascript:hidetop(\"toptastingnote\");'></div></div></div>"
									: "")
							+ "<div class='winename'>" + Knownwines.getKnownWineName(knownwineid) + "</div>"
							+ "<div class='region'>" + Region.getRegion(knownwineid) + "</div>"
							+ (wltv != null && !"".equals(wltv.url)
									? "<a href='" + wltv.url + "' target='_blank'><div class='wltv'></div></a>"
									: "")
							+ "</div><div id='ratingexplanation' onmouseover='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'>"
							+ t.get("ratingexplanation") + "</div></div>");

					// select vintages

					int totalwidth = 170; // author width
					if (singlevintage > 0) {
						rs = Dbutil.selectQuery("select *,max(ratinghigh) as high from ratedwines where knownwineid="
								+ knownwineid + " and author!='FWS' and vintage=" + singlevintage
								+ " and rating>70 group by vintage order by vintage desc;", con);
						if (rs.next()) {
							vintages.add(singlevintage);
							if (rs.getInt("high") > 0) {

								totalwidth += 65;
							} else {
								totalwidth += 50;
							}
						}
					}
					rs = Dbutil.selectQuery("select *,max(ratinghigh) as high from ratedwines where knownwineid="
							+ knownwineid + " and author!='FWS' and vintage!=" + singlevintage
							+ " and rating>70 group by vintage order by vintage desc;", con);
					while (totalwidth < width && rs.next()) {
						if (rs.getInt("high") > 0) {
							totalwidth += 65;
						} else {
							totalwidth += 50;
						}
						if (totalwidth < width) {
							vintages.add(rs.getInt("vintage"));
							startvintage = rs.getInt("vintage") - 1;
							singlevintageclause = " or vintage=" + singlevintage;
						}
					}
					String authorwidth = "";
					if (totalwidth < width)
						authorwidth = " style='width=" + (width - totalwidth) + "px;'";
					String selectedvintages = "";
					for (Integer vin : vintages.toArray(new Integer[0])) {
						selectedvintages += "," + vin;
					}
					if (selectedvintages.length() > 1)
						selectedvintages = selectedvintages.substring(1);
					if (selectedvintages.equals("")) {
						sb.append("<table class='ratings'><tr><td class='noratings'></td>");
						sb.append("<td class='vintageo' style='width:800px;'></td>");
						sb.append("<td class='roundrvintageno'></td></tr></table>");
					} else {
						// Determine which vintages have the best PQ ratio
						query = "select ra.*, (minpriceeuroex/price) as pq from ratinganalysis ra join pqratio pq on (ra.rating=pq.rating) where  ra.minpriceeuroex>0 and knownwineid="
								+ knownwineid + " and author='FWS' and vintage in (" + selectedvintages
								+ ") order by pq;";
						rs = Dbutil.selectQuery(query, con);
						if (rs.next()) {
							rs.last();
							int totalvintages = rs.getRow();
							rs.beforeFirst();
							if (totalvintages > 3) {
								int n = Math.round((float) ((totalvintages / 3) + 0.5));
								for (int i = 0; i < n; i++) {
									rs.next();
									goodpqvintages += "*" + rs.getInt("vintage");
								}
								goodpqvintages += "*";
							}
						}

						// Get all different authors
						rs = Dbutil.selectQuery("select * from ratedwines where knownwineid=" + knownwineid
								+ " and author!='FWS' and vintage in (" + selectedvintages
								+ ") and rating>70 group by author;", con);
						while (rs.next()) {
							authors.add(rs.getString("author"));
						}
						// fill map
						query = "select * from ratedwines where knownwineid=" + knownwineid + " and (vintage>"
								+ startvintage + singlevintageclause + " ) and author!='FWS' and rating>70 ;";
						rs = Dbutil.selectQuery(query, con);
						while (rs.next())
							map.put(rs.getString("author") + rs.getString("vintage"), rs.getString("rating")
									+ (rs.getInt("ratinghigh") > 0 ? "-" + rs.getString("ratinghigh") : ""));

						// Header rows
						sb.append(
								"<table class='ratings'><tr><td class='ratingstar' onclick='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'></td>");
						if (!"".equals(authorwidth))
							sb.append("<td class='vintageo' " + authorwidth + "></td>");
						int td = 0;
						for (int vintage : vintages) {
							if (td == vintages.size() - 1) {
								sb.append("<td class='roundrvintage" + (td % 2 == 0 ? "e" : "o") + "'><a href='"
										+ searchpage + "?name="
										+ Webroutines.URLEncode(Knownwines.getKnownWineName(knownwineid))
										+ "&amp;vintage=" + vintage + "'"
										+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'" : "")
										+ ">" + (vintage > 0 ? vintage : "NV") + "</a></td>");
							} else {
								sb.append("<td class='vintage" + (td % 2 == 0 ? "e" : "o") + "'><a href='" + searchpage
										+ "?name=" + Webroutines.URLEncode(Knownwines.getKnownWineName(knownwineid))
										+ "&amp;vintage=" + vintage + "'"
										+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'" : "")
										+ " rel='nofollow'>" + (vintage > 0 ? vintage : "NV") + "</a></td>");
							}
							td++;
						}
						sb.append("</tr>");

						// Loop over authors and vintages
						int tr = 0;
						for (String author : authors) {
							td = 0;
							/*
							 * String stars="<sup>&#42"; for (int j=0;j<tr;j++) stars+="&#42";
							 * stars+="</sup>"; authorinfo+=stars+"&nbsp;"+getAuthorNote(author);
							 */
							if (tr == authors.size() - 1) {
								sb.append("<tr><td class='roundauthor" + (tr % 2 == 0 ? "e" : "o") + "'>"
										+ getAuthorName(author) + "</td>\n");
							} else {
								sb.append("<tr><td class='author" + (tr % 2 == 0 ? "e" : "o") + "'>"
										+ getAuthorName(author) + "</td>\n");
							}

							if (!"".equals(authorwidth))
								sb.append(
										"<td class='rating" + (tr % 2 == 0 ? "e" : "o") + "o" + authorwidth + "></td>");
							for (int vintage : vintages) {
								if (tr == authors.size() - 1 && td == vintages.size() - 1) {
									sb.append("<td class='roundrrating" + (tr % 2 == 0 ? "e" : "o")
											+ (td % 2 == 0 ? "e" : "o") + " sprite sprite-roundrrating"
											+ (tr % 2 == 0 ? "e" : "o") + (td % 2 == 0 ? "e" : "o") + "'>"
											+ (map.get(author + vintage) == null ? "" : map.get(author + vintage))
											+ "</td>");
								} else {
									sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o")
											+ (td % 2 == 0 ? "e" : "o") + "'>"
											+ (map.get(author + vintage) == null ? "" : map.get(author + vintage))
											+ "</td>");
								}
								td++;
							}
							sb.append("</tr>");
							tr++;
						}
						sb.append("</table>");
					}
					// sb.append("<div class=\"note\">Ratings copyright of their respective owners.
					// See <a href='/disclaimer.jsp'>\"Disclaimer\"</a> for details. </div>");
				} catch (Exception E) {
					Dbutil.logger.error("Problem while generating Ratings html", E);
				}
			}
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		RatingInfo ri = new RatingInfo();
		ri.html = sb.toString();
		ri.authornote = authorinfo;

		return ri;

	}

	public static RatingInfo getNewRatingsHTML(int knownwineid, int width, String searchpage, int singlevintage,
			Searchdata searchdata, Translator t, boolean showeditbuttons) {
		String html = "";
		StringBuffer sb = new StringBuffer();
		ResultSet rs = null;
		WineLibraryTV wltv = null;
		String query;
		String authorinfo = "";
		String goodpqvintages = "";
		String knownwinename = "";
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> winemap = new HashMap<String, String>();
		HashMap<String, Integer> ratedwinemap = new HashMap<String, Integer>();
		HashMap<String, String> linkmap = new HashMap<String, String>();
		TreeSet<Integer> vintages = new TreeSet<Integer>();
		HashSet<String> authors = new HashSet<String>();
		Connection con = Dbutil.openNewConnection();
		if (knownwineid > 0) {
			if (true || Dbutil.readIntValueFromDB(
					"select count(*) as thecount from ratedwines where knownwineid=" + knownwineid + " and rating>70;",
					"thecount") > 0) {
				int currentvintage;
				int startvintage = 0;
				String singlevintageclause = "";
				String cellclass;
				try {
					// See if there is a label
					String labeldiv = "";
					if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
							+ knownwineid + ".jpg").exists()) {
						labeldiv = "<div class='label'><img src='/labels/" + knownwineid + ".jpg' alt=\""
								+ Knownwines.getKnownWineName(knownwineid)
								+ "\" onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
					} else if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
							+ knownwineid + ".gif").exists()) {
						labeldiv = "<div class='label'><img src='/labels/" + knownwineid + ".gif' alt='"
								+ Knownwines.getKnownWineName(knownwineid).replace("'", "&apos;")
								+ "' onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";

					}
					wltv = new WineLibraryTV(knownwineid, singlevintage + "");
					String[] glass = Knownwines.getGlassImage(knownwineid);
					knownwinename = Knownwines.getKnownWineName(knownwineid);
					Knownwine k = new Knownwine(knownwineid);
					k.getProperties();
					// wine div
					sb.append("<div class='winecontainer'>" + "<img class='wineglass' src='/css2/d" + glass[0]
							+ "' alt='" + glass[1] + "'/>" + "<div class='wine'>" + labeldiv
							+ (true ? "<div class='tastingnote' onclick='javascript:show(\"tnsuperholder\");' onmouseout='javascript:hide(\"tnsuperholder\");'>"
									+ "</div>" : "")
							+ "<div class='winename'><h1>" + knownwinename + "</h1></div>" + "<div class='region'><h1>"
							+ Region.getRegion(knownwineid) + "</h1></div>"
							+ (wltv != null && !"".equals(wltv.url)
									? "<a href='" + wltv.url + "' target='_blank'><div class='wltv'></div></a>"
									: "")
							+ "<div id='tnsuperholder' onmouseover='javascript:show(\"tnsuperholder\");' onmouseout='javascript:hide(\"tnsuperholder\");'><div id='tnholder'><div id='tnheader'>Tasting notes from <a href=\"http://www.cellartracker.com/list.asp?Table=Notes&amp;iUserOverride=0&amp;szSearch="
							+ Knownwines.getKnownWineName(knownwineid) + "+" + searchdata.vintage
							+ "\" target='_blank'>CellarTracker.com (click to open in separate page)</a></div><div id='divtnframe'><iframe id='tnframe' name='tnframe' src=''></iframe></div></div></div>"
							+ "</div><div id='ratingexplanation' onmouseover='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'>"
							+ t.get("ratingexplanation")
							+ "<div class='hreview-aggregate'>For <span class='item'><span class='fn'>" + knownwinename
							+ "</span></span> we found <span class='count'>"
							+ Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="
									+ knownwineid + " group by knownwineid", "thecount")
							+ "</span> ratings with an average of <span class='rating'><span class='average'>"
							+ Dbutil.readIntValueFromDB(
									"select avg(rating) as rating from ratinganalysis where author='FWS' and knownwineid="
											+ knownwineid + " group by knownwineid",
									"rating")
							+ "</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span>Price range: <span class='pricerange'>starting at "
							+ (k.minprices.get("US") != null ? "$" + k.minprices.get("US") + " in US" : "")
							+ (k.minprices.get("US") != null && k.minprices.get("EU") != null ? ", " : "")
							+ (k.minprices.get("EU") != null ? "&euro;" + k.minprices.get("EU") + " in EU" : "")
							+ ", with " + k.shopsselling + " shops selling this wine.</span></div>" + "</div>");

					// select vintages

					int totalwidth = 170; // author width
					if (singlevintage > 0) {
						rs = Dbutil.selectQuery("select *,max(ratinghigh) as high from ratedwines where knownwineid="
								+ knownwineid + " and author!='FWS' and vintage=" + singlevintage
								+ " and rating>70 group by vintage order by vintage desc;", con);
						if (rs.next()) {
							vintages.add(singlevintage);
							if (rs.getInt("high") > 0) {

								totalwidth += 65;
							} else {
								totalwidth += 50;
							}
						}
					}
					rs = Dbutil.selectQuery("select *,max(ratinghigh) as high from ratedwines where knownwineid="
							+ knownwineid + " and author!='FWS' and vintage!=" + singlevintage
							+ " and rating>70 group by vintage order by vintage desc;", con);
					while (totalwidth < width && rs.next()) {
						if (rs.getInt("high") > 0) {
							totalwidth += 65;
						} else {
							totalwidth += 50;
						}
						if (totalwidth < width) {
							vintages.add(rs.getInt("vintage"));
							startvintage = rs.getInt("vintage") - 1;
							singlevintageclause = " or vintage=" + singlevintage;
						}
					}
					String authorwidth = "";
					if (totalwidth < width)
						authorwidth = " style='width:" + (width - totalwidth) + "px;'";
					String selectedvintages = "";
					for (Integer vin : vintages.toArray(new Integer[0])) {
						selectedvintages += "," + vin;
					}
					if (selectedvintages.length() > 1)
						selectedvintages = selectedvintages.substring(1);
					if (selectedvintages.equals("")) {
						sb.append("<table class='ratings'><tr><td class='noratings'>No ratings available</td>");
						sb.append("<td style='width:780px;'></td>");
						sb.append("<td class='roundrvintageno'></td></tr></table>");
					} else {
						// Determine which vintages have the best PQ ratio
						query = "select ra.*, (minpriceeuroex/price) as pq from ratinganalysis ra join pqratio pq on (ra.rating=pq.rating) where  ra.minpriceeuroex>0 and knownwineid="
								+ knownwineid + " and author='FWS' and vintage in (" + selectedvintages
								+ ") order by pq;";
						rs = Dbutil.selectQuery(query, con);
						if (rs.next()) {
							rs.last();
							int totalvintages = rs.getRow();
							rs.beforeFirst();
							if (totalvintages > 3) {
								int n = Math.round((float) ((totalvintages / 3) + 0.5));
								for (int i = 0; i < n; i++) {
									rs.next();
									goodpqvintages += "*" + rs.getInt("vintage");
								}
								goodpqvintages += "*";
							}
						}

						// Get all different authors
						rs = Dbutil.selectQuery("select * from ratedwines where knownwineid=" + knownwineid
								+ " and author!='FWS' and vintage in (" + selectedvintages
								+ ") and rating>70 group by author;", con);
						while (rs.next()) {
							authors.add(rs.getString("author"));
						}
						// fill map
						query = "select * from ratedwines where knownwineid=" + knownwineid + " and (vintage>"
								+ startvintage + singlevintageclause + " ) and author!='FWS' and rating>70 ;";
						rs = Dbutil.selectQuery(query, con);
						while (rs.next()) {
							if (linkmap.get("RP") == null && rs.getString("author").equals("RP")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("erobertparker"))
								linkmap.put(rs.getString("author"),
										"http://www.erobertparker.com/newSearch/pTextSearch.aspx?search=members&amp;textSearchString="
												+ java.text.Normalizer.normalize(rs.getString("name"),
														java.text.Normalizer.Form.NFD)
												+ " ");
							if (linkmap.get("WS") == null && rs.getString("author").equals("WS")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("winespectator")) {
								linkmap.put(rs.getString("author"),
										"http://wines.winespectator.com/wine/search?submitted=Y&amp;forwarded=1&amp;size=50&amp;text_search_flag=wine_plus_vintage&amp;search_by=all&amp;fuzzy=&amp;sort_by=vintage&amp;case_prod=&amp;taste_date=&amp;viewtype=&amp;winery="
												+ URLEncodeUTF8(rs.getString("name")) + " ");
							}
							map.put(rs.getString("author") + rs.getString("vintage"), rs.getString("rating")
									+ (rs.getInt("ratinghigh") > 0 ? "-" + rs.getString("ratinghigh") : ""));
							winemap.put(rs.getString("author") + rs.getString("vintage"),
									rs.getString("name").replaceAll("'", "&apos;"));
							ratedwinemap.put(rs.getString("author") + rs.getString("vintage"), rs.getInt("id"));
						}

						// Header rows
						sb.append(
								"<table class='ratings'><tr class='ratingsheader'><td class='ratingstar' onclick='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'>Expert ratings</td>");
						if (!"".equals(authorwidth))
							sb.append("<td class='nobackground' " + authorwidth + "></td>");
						int td = 0;
						for (int vintage : vintages) {
							if (td == vintages.size() - 1) {
								sb.append("<td class='roundrvintage" + (td % 2 == 0 ? "e" : "o") + "'><a href='"
										+ searchpage + "?name="
										+ Webroutines.URLEncode(Knownwines.getKnownWineName(knownwineid))
										+ "&amp;vintage=" + vintage + "'"
										+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'" : "")
										+ " rel='nofollow'>" + (vintage > 0 ? vintage : "NV") + "</a></td>");
							} else {
								sb.append("<td class='vintage" + (td % 2 == 0 ? "e" : "o") + "'><a href='" + searchpage
										+ "?name=" + Webroutines.URLEncode(Knownwines.getKnownWineName(knownwineid))
										+ "&amp;vintage=" + vintage + "'"
										+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'" : "")
										+ " rel='nofollow'>" + (vintage > 0 ? vintage : "NV") + "</a></td>");
							}
							td++;
						}
						sb.append("</tr>");

						// Loop over authors and vintages
						int tr = 0;
						for (String author : authors) {
							td = 0;
							/*
							 * String stars="<sup>&#42"; for (int j=0;j<tr;j++) stars+="&#42";
							 * stars+="</sup>"; authorinfo+=stars+"&nbsp;"+getAuthorNote(author);
							 */
							if (tr == authors.size() - 1) {
								sb.append("<tr class='ratingsrow'><td class='roundauthor" + (tr % 2 == 0 ? "e" : "o")
										+ "'>"
										+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author)
												+ "' target='_blank' rel='nofollow'>" : "")
										+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "")
										+ "</td>\n");
							} else {
								sb.append("<tr class='ratingsrow'><td class='author" + (tr % 2 == 0 ? "e" : "o") + "'>"
										+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author)
												+ "' target='_blank' rel='nofollow'>" : "")
										+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "")
										+ "</td>\n");
							}

							if (!"".equals(authorwidth))
								sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o") + "o'" + authorwidth
										+ "></td>");
							for (int vintage : vintages) {
								if (tr == authors.size() - 1 && td == vintages.size() - 1) {
									sb.append("<td class='roundrrating" + (tr % 2 == 0 ? "e" : "o")
											+ (td % 2 == 0 ? "e" : "o") + "' title='"
											+ (winemap.get(author + vintage) == null ? ""
													: winemap.get(author + vintage) + ": Rating "
															+ map.get(author + vintage))
											+ "'>"
											+ (map.get(author + vintage) == null ? "" : map.get(author + vintage))
											+ (map.get(author + vintage) != null && showeditbuttons
													? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
															+ ratedwinemap.get(author + vintage)
															+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
													: "")
											+ "</td>");
								} else {
									sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o")
											+ (td % 2 == 0 ? "e" : "o") + "' title='"
											+ (winemap.get(author + vintage) == null ? ""
													: winemap.get(author + vintage) + ": Rating "
															+ map.get(author + vintage))
											+ "'>"
											+ (map.get(author + vintage) == null ? "" : map.get(author + vintage))
											+ (map.get(author + vintage) != null && showeditbuttons
													? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
															+ ratedwinemap.get(author + vintage)
															+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
													: "")
											+ "</td>");
								}
								td++;
							}
							sb.append("</tr>");
							tr++;
						}
						sb.append("</table>");
					}
					sb.append("</div>");
					// sb.append("<div class=\"note\">Ratings copyright of their respective owners.
					// See <a href='/disclaimer.jsp'>\"Disclaimer\"</a> for details. </div>");
				} catch (Exception E) {
					Dbutil.logger.error("Problem while generating Ratings html", E);
				}
			}
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		RatingInfo ri = new RatingInfo();
		ri.html = sb.toString();
		ri.authornote = authorinfo;

		return ri;

	}

	public static RatingInfo getRatingsMicroformatHTML(PageHandler p, int knownwineid, int width, String searchpage,
			int singlevintage, Searchdata searchdata, Translator t, boolean showeditbuttons) {
		String html = "";
		StringBuffer sb = new StringBuffer();
		String ratingnote = "";
		ResultSet rs = null;
		WineLibraryTV wltv = null;
		String query;
		boolean hasratings = false;
		String authorinfo = "";
		String goodpqvintages = "";
		String knownwinename = "";
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> winemap = new HashMap<String, String>();
		HashMap<String, Integer> ratedwinemap = new HashMap<String, Integer>();
		HashMap<String, String> linkmap = new HashMap<String, String>();
		TreeSet<Integer> vintages = new TreeSet<Integer>();
		TreeSet<Integer> availablevintages = new TreeSet<Integer>();
		HashSet<String> authors = new HashSet<String>();
		Connection con = Dbutil.openNewConnection();
		if (knownwineid > 0) {
			int currentvintage;
			int startvintage = 0;
			String singlevintageclause = "";
			String cellclass;
			try {
				wltv = new WineLibraryTV(knownwineid, singlevintage + "");
				String[] glass = Knownwines.getGlassImage(knownwineid);
				Knownwine k = new Knownwine(knownwineid);
				k.getProperties();
				knownwinename = k.name.replaceAll("&", "&amp;");
				// See if there is a label
				String labeldiv = "";
				if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator") + knownwineid
						+ ".jpg").exists()) {
					labeldiv = "<div class='label' itemprop='image'><img itemprop='image' src='"
							+ Configuration.staticprefix + "/labels/" + knownwineid + ".jpg' alt=\"" + knownwinename
							+ "\" onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
				} else if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
						+ knownwineid + ".gif").exists()) {
					labeldiv = "<div class='label' itemprop='image'><img itemprop='image'  src='"
							+ Configuration.staticprefix + "/labels/" + knownwineid + ".gif' alt='"
							+ knownwinename.replace("'", "&apos;")
							+ "' onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";

				}

				// wine div
				sb.append(
						"<div itemscope='itemscope' itemtype='http://data-vocabulary.org/Product'><div class='winecontainer'>"
								+
								// "<img class='wineglass' src='/css2/d"+glass[0]+"' alt='"+glass[1]+"'/>" +
								"<div class='wineglass sprite glass sprite-d" + glass[0].replaceAll(".jpg", "")
								+ "'>&nbsp;</div>" +

								"<div class='wine'>" + labeldiv
								+ (true ? "<div class='tastingnote' onclick='vpclick(\"/winelinks.jsp?source=CT&amp;name="
										+ URLEncode(removeAccents(k.uniquename))
										+ (searchdata.vintage.length() > 0 ? "&amp;vintage=" + searchdata.vintage : "")
										+ "\");' onmouseout='javascript:hide(\"tnsuperholder\");'>" + "</div>" : "")
								+ "<div class='winename'><h1>" + getWineNameLinkHtml5(k)
								+ (singlevintage > 0 ? " " + singlevintage : "") + "</h1></div>"
								+ "<div class='region'><ul id='breadcrumb'>");
				String region = Region.getRegion(knownwineid);
				String locale = k.getProperties().get("locale");
				String regionpath = "";
				for (String r : locale.split(", ")) {
					if (!r.equals("All")) {
						regionpath += "/" + r.replaceAll("/", "%252F");
						if (singlevintage == 0) {
							sb.append("<li><a href='/region" + removeAccents(regionpath).replaceAll(" ", "+") + "/'>"
									+ r + "</a>");
						} else {
							sb.append("<li><span class='jslink' onclick='vpclick(\"/region" + removeAccents(regionpath)
									+ "\");'>" + r + "</span>");
						}
						if (!region.equals(r)) {
							sb.append(" &raquo; </li>");
						} else {
							sb.append("</li>");
						}
					}
				}
				sb.append("</ul></div>"
						+ (wltv != null && !"".equals(wltv.url)
								? "<a href='/winelinks.jsp?source=GV&amp;name=" + URLEncode(removeAccents(k.uniquename))
										+ (searchdata.vintage.length() > 0 ? "&amp;vintage=" + searchdata.vintage : "")
										+ "' ><div class='wltv'></div></a>"
								: "")
						+ "<a href='/link.jsp?exttarget=Twitter&amp;exturl="
						+ URLEncode("http://twitter.com/home?status=%23nowdrinking "
								+ URLEncodeUTF8(knownwinename).replaceAll("'", "&apos;")
								+ " https://www.vinopedia.com/wine/"
								+ URLEncode(removeAccents(k.uniquename)).replaceAll("'", "&apos;"))
						+ "' title='Click to send this page to Twitter!' target='_blank' rel='nofollow'><img id='twitdrinkingnow' src='"
						+ Configuration.cdnprefix + "/images/twitterdrinkingwine.png' alt='Twitter this wine'/></a>"
						+ "</div><div id='ratingexplanation' onmouseover='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'></div>");

				// ratingnote="For "+knownwinename+" we found <span
				// itemprop='count'>"+Dbutil.readIntValueFromDB("select count(*) as thecount
				// from ratedwines where knownwineid="+knownwineid+" group by knownwineid",
				// "thecount")+"</span> ratings with an average of <span class='rating'><span
				// class='average' itemprop='average'>"+Dbutil.readIntValueFromDB("select
				// avg(rating) as rating from ratinganalysis where author='FWS' and
				// knownwineid="+knownwineid+" group by knownwineid", "rating")
				// +"</span>/100 points.<span class='best'><span class='value-title'
				// title='100'> </span></span><span class='worst'><span class='value-title'
				// title='50'> </span></span></span><span></div>";

				// select vintages

				int totalwidth = 170; // author width
				rs = Dbutil.selectQuery(
						"select * from (select id,name,vintage,author,sourceurl,rating,ratinghigh from ratedwines where knownwineid="
								+ knownwineid
								+ " and author!='FWS'  and rating>70 union select 0 as id,name,vintage,'' as author,null as sourceurl,0 as rating, 0 as ratinghigh from wines where knownwineid="
								+ knownwineid + " and vintage>0 group by vintage) sel order by "
								+ (singlevintage > 0 ? "(vintage=2006) desc," : "")
								+ "vintage<2006,rating=0,vintage desc;",
						con);
				while (rs.next()) {
					if (totalwidth < width) {
						if (!vintages.contains(rs.getInt("vintage"))) {
							if (rs.getInt("ratinghigh") > 0) {
								totalwidth += 65;
							} else {
								totalwidth += 50;
							}
							vintages.add(rs.getInt("vintage"));
						}
						if (!hasratings && rs.getInt("rating") > 70)
							hasratings = true;
						if (!rs.getString("author").equals("") && !authors.contains(rs.getString("author")))
							authors.add(rs.getString("author"));
						if (rs.getInt("rating") > 0) {
							if (linkmap.get("RP") == null && rs.getString("author").equals("RP")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("erobertparker"))
								linkmap.put(rs.getString("author"), "/winelinks.jsp?name="
										+ URLEncode(removeAccents(k.uniquename)) + "&amp;source=RP");
							if (linkmap.get("WS") == null && rs.getString("author").equals("WS")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("winespectator")) {
								linkmap.put(rs.getString("author"), "/winelinks.jsp?name="
										+ URLEncode(removeAccents(k.uniquename)) + "&amp;source=WS");
							}
							map.put(rs.getString("author") + rs.getString("vintage"), rs.getString("rating")
									+ (rs.getInt("ratinghigh") > 0 ? "-" + rs.getString("ratinghigh") : ""));
							winemap.put(rs.getString("author") + rs.getString("vintage"),
									rs.getString("name").replaceAll("'", "&apos;"));
							ratedwinemap.put(rs.getString("author") + rs.getString("vintage"), rs.getInt("id"));
						}
					}
					if (rs.getInt("rating") == 0) {
						availablevintages.add(rs.getInt("vintage"));
					}

				}
				Dbutil.closeRs(rs);
				String selectedvintages = "";
				for (Integer vin : vintages.toArray(new Integer[0])) {
					selectedvintages += "," + vin;
				}
				if (selectedvintages.length() > 1)
					selectedvintages = "and vintage in (" + selectedvintages.substring(1) + ") ";

				String authorwidth = "";
				if (totalwidth < width)
					authorwidth = " style='width:" + (width - totalwidth) + "px;'";
				// Determine which vintages have the best PQ ratio
				query = "select ra.*, (minpriceeuroex/price) as pq from ratinganalysis ra join pqratio pq on (ra.rating=pq.rating) where  ra.minpriceeuroex>0 and knownwineid="
						+ knownwineid + " and author='FWS' " + selectedvintages + " order by pq;";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					rs.last();
					int totalvintages = rs.getRow();
					rs.beforeFirst();
					if (totalvintages > 3) {
						int n = Math.round((float) ((totalvintages / 3) + 0.5));
						for (int i = 0; i < n; i++) {
							rs.next();
							goodpqvintages += "*" + rs.getInt("vintage");
						}
						goodpqvintages += "*";
					}
				}
				Dbutil.closeRs(rs);

				// Header rows
				sb.append("<table class='ratings'><tr class='ratingsheader'><td class='ratingstar' " + (hasratings
						? "onclick='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'>Expert ratings"
						: (vintages.size() > 0 ? ">Available vintages" : ">No ratings")) + "</td>");
				if (!"".equals(authorwidth))
					sb.append("<td class='nobackground' " + authorwidth + "></td>");
				int td = 0;
				for (int vintage : vintages) {
					if (td == vintages.size() - 1) {
						if (hasratings) {
							sb.append("<td class='sprite sprite-roundrvintage" + (td % 2 == 0 ? "e" : "o") + "'>"
									+ (!availablevintages.contains(vintage) ? (vintage > 0 ? vintage : "NV")
											: "<a href='" + winelink(k.uniquename, vintage) + "'"
													+ (goodpqvintages.contains("*" + vintage + "*")
															? " class='recommended'"
															: "")
													+ (singlevintage == 0
															? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
																	+ vintage + "'"
															: " title='" + knownwinename.replaceAll("'", "&apos;")
																	+ "' ")
													+ " >" + (vintage > 0 ? vintage : "NV") + "</a>")
									+ "</td>");
						} else {
							sb.append("<td class='sprite-roundrvintage" + (td % 2 == 0 ? "e" : "o") + "'>"
									+ (!availablevintages.contains(vintage) ? (vintage > 0 ? vintage : "NV")
											: "<a href='" + winelink(k.uniquename, vintage) + "'"
													+ (goodpqvintages.contains("*" + vintage + "*")
															? " class='recommended'"
															: "")
													+ (singlevintage == 0
															? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
																	+ vintage + "'"
															: " title='" + knownwinename.replaceAll("'", "&apos;")
																	+ "' ")
													+ " >" + (vintage > 0 ? vintage : "NV") + "</a>")
									+ "</td>");
						}
					} else {
						sb.append("<td class='spriter spriter-vintage" + (td % 2 == 0 ? "e" : "o") + "'>"
								+ (!availablevintages.contains(vintage) ? (vintage > 0 ? vintage : "NV")
										: "<a href='" + winelink(k.uniquename, vintage) + "'"
												+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'"
														: "")
												+ (singlevintage == 0
														? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
																+ vintage + "'"
														: " title='" + knownwinename.replaceAll("'", "&apos;") + "' ")
												+ " >" + (vintage > 0 ? vintage : "NV") + "</a>")
								+ "</td>");
					}
					td++;
				}
				sb.append("</tr>");

				// Loop over authors and vintages
				int tr = 0;
				for (String author : authors) {
					td = 0;
					/*
					 * String stars="<sup>&#42"; for (int j=0;j<tr;j++) stars+="&#42";
					 * stars+="</sup>"; authorinfo+=stars+"&nbsp;"+getAuthorNote(author);
					 */
					if (tr == authors.size() - 1) {
						sb.append(
								"<tr class='ratingsrow'><td class='sprite sprite-roundauthor"
										+ (tr % 2 == 0 ? "e" : "o") + "'>"
										+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author)
												+ "' target='_blank' rel='nofollow'>" : "")
										+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "")
										+ "</td>\n");
					} else {
						sb.append("<tr class='ratingsrow'><td class='author" + (tr % 2 == 0 ? "e" : "o") + "'>"
								+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author) + "' rel='nofollow'>"
										: "")
								+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "") + "</td>\n");
					}

					if (!"".equals(authorwidth))
						sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o") + "o'" + authorwidth + "></td>");
					for (int vintage : vintages) {
						if (tr == authors.size() - 1 && td == vintages.size() - 1) {
							sb.append("<td class='roundrrating" + (tr % 2 == 0 ? "e" : "o") + (td % 2 == 0 ? "e" : "o")
									+ "' title='"
									+ (winemap.get(author + vintage) == null ? ""
											: winemap.get(author + vintage).replaceAll("&", "&amp;") + ": Rating "
													+ map.get(author + vintage))
									+ "'>" + (map.get(author + vintage) == null ? "" : map.get(author + vintage))
									+ (map.get(author + vintage) != null && showeditbuttons
											? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
													+ ratedwinemap.get(author + vintage)
													+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
											: "")
									+ "</td>");
						} else {
							sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o") + (td % 2 == 0 ? "e" : "o")
									+ "' title='"
									+ (winemap.get(author + vintage) == null ? ""
											: winemap.get(author + vintage).replaceAll("&", "&amp;") + ": Rating "
													+ map.get(author + vintage))
									+ "'>" + (map.get(author + vintage) == null ? "" : map.get(author + vintage))
									+ (map.get(author + vintage) != null && showeditbuttons
											? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
													+ ratedwinemap.get(author + vintage)
													+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
											: "")
									+ "</td>");
						}
						td++;
					}
					sb.append("</tr>");
					tr++;
				}
				sb.append("</table>");
				int n = Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="
						+ knownwineid + " group by knownwineid", "thecount");
				if (n > 1)
					sb.append(
							"<div style='margin:10px;' ><span itemprop='review' itemscope  itemtype='http://data-vocabulary.org/Review-aggregate'>For all vintages of "
									+ k.name + " we have <span itemprop='count'>" + n
									+ "</span> ratings on record with an average of <span itemprop='rating'><span class='average'>"
									+ Dbutil.readIntValueFromDB(
											"select avg(rating) as rating from ratinganalysis where author='FWS' and knownwineid="
													+ knownwineid + " group by knownwineid",
											"rating")
									+ "</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span></span></div>");
				// sb.append("<span itemprop='offerDetails' itemscope
				// itemtype='http://data-vocabulary.org/Offer-aggregate'><meta
				// itemprop='offerCount' content='"+p.s.wineset.records+"' /><meta
				// itemprop='currency' content='USD' /><meta itemprop='lowPrice'
				// content='119.99'><meta itemprop='highPrice' content='219.99'></span>");
				String currency = "EUR";
				if (k.getProperties().get("locale").contains("USA"))
					currency = "USD";
				sb.append(
						"<span itemprop='offerDetails' itemscope itemtype='http://data-vocabulary.org/Offer-aggregate'><meta itemprop='offerCount' content='"
								+ p.s.wineset.records + "' /><meta itemprop='currency' content='" + currency
								+ "' /><meta itemprop='lowPrice' content='"
								+ formatPriceNoCurrency(p.s.wineset.lowestprice, p.s.wineset.lowestprice, currency,
										"EX")
								+ "'><meta itemprop='highPrice' content='"
								+ formatPriceNoCurrency(p.s.wineset.highestprice, p.s.wineset.highestprice, currency,
										"EX")
								+ "'></span>");

				sb.append("</div></div>\n");

				// sb.append("<div class=\"note\">Ratings copyright of their respective owners.
				// See <a href='/disclaimer.jsp'>\"Disclaimer\"</a> for details. </div>");
			} catch (Exception E) {
				Dbutil.logger.error("Problem while generating Ratings html", E);
			}

		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		RatingInfo ri = new RatingInfo();
		ri.html = sb.toString();
		ri.authornote = authorinfo;
		ri.ratingnote = ratingnote;

		return ri;

	}

	public static RatingInfo getWave1RatingsHTML(int knownwineid, int width, String searchpage, int singlevintage,
			Searchdata searchdata, Translator t, boolean showeditbuttons) {
		String html = "";
		StringBuffer sb = new StringBuffer();
		String ratingnote = "";
		ResultSet rs = null;
		WineLibraryTV wltv = null;
		String query;
		boolean hasratings = false;
		String authorinfo = "";
		String goodpqvintages = "";
		String knownwinename = "";
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> winemap = new HashMap<String, String>();
		HashMap<String, Integer> ratedwinemap = new HashMap<String, Integer>();
		HashMap<String, String> linkmap = new HashMap<String, String>();
		TreeSet<Integer> vintages = new TreeSet<Integer>();
		TreeSet<Integer> availablevintages = new TreeSet<Integer>();
		HashSet<String> authors = new HashSet<String>();
		Connection con = Dbutil.openNewConnection();
		if (knownwineid > 0) {
			int currentvintage;
			int startvintage = 0;
			String singlevintageclause = "";
			String cellclass;
			try {
				wltv = new WineLibraryTV(knownwineid, singlevintage + "");
				String[] glass = Knownwines.getGlassImage(knownwineid);
				Knownwine k = new Knownwine(knownwineid);
				k.getProperties();
				knownwinename = k.name.replaceAll("&", "&amp;");
				// See if there is a label
				String labeldiv = "";
				if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator") + knownwineid
						+ ".jpg").exists()) {
					labeldiv = "<div class='label'><img src='" + Configuration.staticprefix + "/labels/" + knownwineid
							+ ".jpg' alt=\"" + knownwinename
							+ "\" onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
				} else if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
						+ knownwineid + ".gif").exists()) {
					labeldiv = "<div class='label'><img src='" + Configuration.staticprefix + "/labels/" + knownwineid
							+ ".gif' alt='" + knownwinename.replace("'", "&apos;")
							+ "' onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";

				}

				// wine div
				sb.append("<div class='winecontainer'>" +
				// "<img class='wineglass' src='/css2/d"+glass[0]+"' alt='"+glass[1]+"'/>" +
						"<div class='wineglass sprite glass sprite-d" + glass[0].replaceAll(".jpg", "")
						+ "'>&nbsp;</div>" +

						"<div class='wine'>" + labeldiv
						+ (true ? "<div class='tastingnote' onclick='vpclick(\"/winelinks.jsp?source=CT&amp;name="
								+ URLEncode(removeAccents(k.uniquename))
								+ (searchdata.vintage.length() > 0 ? "&amp;vintage=" + searchdata.vintage : "")
								+ "\");' onmouseout='javascript:hide(\"tnsuperholder\");'>" + "</div>" : "")
						+ "<div class='winename'><h1>" + getWineNameLink(k)
						+ (singlevintage > 0 ? " " + singlevintage : "") + "</h1></div>"
						+ "<div class='region'><ul id='breadcrumb'>");
				String region = Region.getRegion(knownwineid);
				String locale = k.getProperties().get("locale");
				String regionpath = "";
				for (String r : locale.split(", ")) {
					if (!r.equals("All")) {
						regionpath += "/" + r.replaceAll("/", "%252F");
						if (singlevintage == 0) {
							sb.append("<li><a href='/region" + removeAccents(regionpath) + "/'>" + r + "</a>");
						} else {
							sb.append("<li><span class='jslink' onclick='vpclick(\"/region" + removeAccents(regionpath)
									+ "\");'>" + r + "</span>");
						}
						if (!region.equals(r)) {
							sb.append(" &raquo; </li>");
						} else {
							sb.append("</li>");
						}
					}
				}
				sb.append("</ul></div>"
						+ (wltv != null && !"".equals(wltv.url)
								? "<a href='/winelinks.jsp?source=GV&amp;name=" + URLEncode(removeAccents(k.uniquename))
										+ (searchdata.vintage.length() > 0 ? "&amp;vintage=" + searchdata.vintage : "")
										+ "' ><div class='wltv'></div></a>"
								: "")
						+ "<a href='/link.jsp?exttarget=Twitter&amp;exturl="
						+ URLEncode("http://twitter.com/home?status=%23nowdrinking "
								+ URLEncodeUTF8(knownwinename).replaceAll("'", "&apos;")
								+ " https://www.vinopedia.com/wine/"
								+ URLEncode(removeAccents(k.uniquename)).replaceAll("'", "&apos;"))
						+ "' title='Click to send this page to Twitter!' target='_blank' rel='nofollow'><img id='twitdrinkingnow' src='"
						+ Configuration.cdnprefix + "/images/twitterdrinkingwine.png' alt='Twitter this wine'/></a>"
						+ "</div><div id='ratingexplanation' onmouseover='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'></div>");

				ratingnote = "<div class='hreview-aggregate'>For <span class='item'><span class='fn'>" + knownwinename
						+ "</span></span> we found <span class='count'>"
						+ Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="
								+ knownwineid + " group by knownwineid", "thecount")
						+ "</span> ratings with an average of <span class='rating'><span class='average'>"
						+ Dbutil.readIntValueFromDB(
								"select avg(rating) as rating from ratinganalysis where author='FWS' and knownwineid="
										+ knownwineid + " group by knownwineid",
								"rating")
						+ "</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span></div>";

				// select vintages

				int totalwidth = 170; // author width
				rs = Dbutil.selectQuery(
						"select * from (select id,name,vintage,author,sourceurl,rating,ratinghigh from ratedwines where knownwineid="
								+ knownwineid
								+ " and author!='FWS'  and rating>70 union select 0 as id,name,vintage,'' as author,null as sourceurl,0 as rating, 0 as ratinghigh from wines where knownwineid="
								+ knownwineid + " and vintage>0 group by vintage) sel order by "
								+ (singlevintage > 0 ? "(vintage=2006) desc," : "") + " rating=0,vintage desc;",
						con);
				while (rs.next()) {
					if (totalwidth < width) {
						if (!vintages.contains(rs.getInt("vintage"))) {
							if (rs.getInt("ratinghigh") > 0) {
								totalwidth += 65;
							} else {
								totalwidth += 50;
							}
							vintages.add(rs.getInt("vintage"));
						}
						if (!hasratings && rs.getInt("rating") > 70)
							hasratings = true;
						if (!rs.getString("author").equals("") && !authors.contains(rs.getString("author")))
							authors.add(rs.getString("author"));
						if (rs.getInt("rating") > 0) {
							if (linkmap.get("RP") == null && rs.getString("author").equals("RP")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("erobertparker"))
								linkmap.put(rs.getString("author"), "/winelinks.jsp?name="
										+ URLEncode(removeAccents(k.uniquename)) + "&amp;source=RP");
							if (linkmap.get("WS") == null && rs.getString("author").equals("WS")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("winespectator")) {
								linkmap.put(rs.getString("author"), "/winelinks.jsp?name="
										+ URLEncode(removeAccents(k.uniquename)) + "&amp;source=WS");
							}
							map.put(rs.getString("author") + rs.getString("vintage"), rs.getString("rating")
									+ (rs.getInt("ratinghigh") > 0 ? "-" + rs.getString("ratinghigh") : ""));
							winemap.put(rs.getString("author") + rs.getString("vintage"),
									rs.getString("name").replaceAll("'", "&apos;"));
							ratedwinemap.put(rs.getString("author") + rs.getString("vintage"), rs.getInt("id"));
						}
					}
					if (rs.getInt("rating") == 0) {
						availablevintages.add(rs.getInt("vintage"));
					}

				}
				Dbutil.closeRs(rs);
				String selectedvintages = "";
				for (Integer vin : vintages.toArray(new Integer[0])) {
					selectedvintages += "," + vin;
				}
				if (selectedvintages.length() > 1)
					selectedvintages = "and vintage in (" + selectedvintages.substring(1) + ") ";

				String authorwidth = "";
				if (totalwidth < width)
					authorwidth = " style='width:" + (width - totalwidth) + "px;'";
				// Determine which vintages have the best PQ ratio
				query = "select ra.*, (minpriceeuroex/price) as pq from ratinganalysis ra join pqratio pq on (ra.rating=pq.rating) where  ra.minpriceeuroex>0 and knownwineid="
						+ knownwineid + " and author='FWS' " + selectedvintages + " order by pq;";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					rs.last();
					int totalvintages = rs.getRow();
					rs.beforeFirst();
					if (totalvintages > 3) {
						int n = Math.round((float) ((totalvintages / 3) + 0.5));
						for (int i = 0; i < n; i++) {
							rs.next();
							goodpqvintages += "*" + rs.getInt("vintage");
						}
						goodpqvintages += "*";
					}
				}
				Dbutil.closeRs(rs);

				// Header rows
				sb.append("<table class='ratings'><tr class='ratingsheader'><td class='ratingstar' " + (hasratings
						? "onclick='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'>Expert ratings"
						: (vintages.size() > 0 ? ">Available vintages" : ">No ratings")) + "</td>");
				if (!"".equals(authorwidth))
					sb.append("<td class='nobackground' " + authorwidth + "></td>");
				int td = 0;
				for (int vintage : vintages) {
					if (td == vintages.size() - 1) {
						if (hasratings) {
							sb.append("<td class='sprite sprite-roundrvintage" + (td % 2 == 0 ? "e" : "o") + "'>"
									+ (!availablevintages.contains(vintage) ? (vintage > 0 ? vintage : "NV")
											: "<a href='" + winelink(k.uniquename, vintage) + "'"
													+ (goodpqvintages.contains("*" + vintage + "*")
															? " class='recommended'"
															: "")
													+ (singlevintage == 0
															? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
																	+ vintage + "'"
															: " title='" + knownwinename.replaceAll("'", "&apos;")
																	+ "' ")
													+ " >" + (vintage > 0 ? vintage : "NV") + "</a>")
									+ "</td>");
						} else {
							sb.append("<td class='sprite-roundrvintage" + (td % 2 == 0 ? "e" : "o") + "'>"
									+ (!availablevintages.contains(vintage) ? (vintage > 0 ? vintage : "NV")
											: "<a href='" + winelink(k.uniquename, vintage) + "'"
													+ (goodpqvintages.contains("*" + vintage + "*")
															? " class='recommended'"
															: "")
													+ (singlevintage == 0
															? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
																	+ vintage + "'"
															: " title='" + knownwinename.replaceAll("'", "&apos;")
																	+ "' ")
													+ " >" + (vintage > 0 ? vintage : "NV") + "</a>")
									+ "</td>");
						}
					} else {
						sb.append("<td class='spriter spriter-vintage" + (td % 2 == 0 ? "e" : "o") + "'>"
								+ (!availablevintages.contains(vintage) ? (vintage > 0 ? vintage : "NV")
										: "<a href='" + winelink(k.uniquename, vintage) + "'"
												+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'"
														: "")
												+ (singlevintage == 0
														? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
																+ vintage + "'"
														: " title='" + knownwinename.replaceAll("'", "&apos;") + "' ")
												+ " >" + (vintage > 0 ? vintage : "NV") + "</a>")
								+ "</td>");
					}
					td++;
				}
				sb.append("</tr>");

				// Loop over authors and vintages
				int tr = 0;
				for (String author : authors) {
					td = 0;
					/*
					 * String stars="<sup>&#42"; for (int j=0;j<tr;j++) stars+="&#42";
					 * stars+="</sup>"; authorinfo+=stars+"&nbsp;"+getAuthorNote(author);
					 */
					if (tr == authors.size() - 1) {
						sb.append(
								"<tr class='ratingsrow'><td class='sprite sprite-roundauthor"
										+ (tr % 2 == 0 ? "e" : "o") + "'>"
										+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author)
												+ "' target='_blank' rel='nofollow'>" : "")
										+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "")
										+ "</td>\n");
					} else {
						sb.append("<tr class='ratingsrow'><td class='author" + (tr % 2 == 0 ? "e" : "o") + "'>"
								+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author) + "' rel='nofollow'>"
										: "")
								+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "") + "</td>\n");
					}

					if (!"".equals(authorwidth))
						sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o") + "o'" + authorwidth + "></td>");
					for (int vintage : vintages) {
						if (tr == authors.size() - 1 && td == vintages.size() - 1) {
							sb.append("<td class='roundrrating" + (tr % 2 == 0 ? "e" : "o") + (td % 2 == 0 ? "e" : "o")
									+ "' title='"
									+ (winemap.get(author + vintage) == null ? ""
											: winemap.get(author + vintage).replaceAll("&", "&amp;") + ": Rating "
													+ map.get(author + vintage))
									+ "'>" + (map.get(author + vintage) == null ? "" : map.get(author + vintage))
									+ (map.get(author + vintage) != null && showeditbuttons
											? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
													+ ratedwinemap.get(author + vintage)
													+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
											: "")
									+ "</td>");
						} else {
							sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o") + (td % 2 == 0 ? "e" : "o")
									+ "' title='"
									+ (winemap.get(author + vintage) == null ? ""
											: winemap.get(author + vintage).replaceAll("&", "&amp;") + ": Rating "
													+ map.get(author + vintage))
									+ "'>" + (map.get(author + vintage) == null ? "" : map.get(author + vintage))
									+ (map.get(author + vintage) != null && showeditbuttons
											? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
													+ ratedwinemap.get(author + vintage)
													+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
											: "")
									+ "</td>");
						}
						td++;
					}
					sb.append("</tr>");
					tr++;
				}
				sb.append("</table>");
				int n = Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="
						+ knownwineid + " group by knownwineid", "thecount");
				if (n > 1)
					sb.append(
							"<div style='margin:10px;'><span class='hreview-aggregate'>For all vintages of <span class='item'><span class='fn'>"
									+ k.name + "</span></span> we have <span class='count'>" + n
									+ "</span> ratings on record with an average of <span class='rating'><span class='average'>"
									+ Dbutil.readIntValueFromDB(
											"select avg(rating) as rating from ratinganalysis where author='FWS' and knownwineid="
													+ knownwineid + " group by knownwineid",
											"rating")
									+ "</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span></span></div>");

				sb.append("</div>");

				// sb.append("<div class=\"note\">Ratings copyright of their respective owners.
				// See <a href='/disclaimer.jsp'>\"Disclaimer\"</a> for details. </div>");
			} catch (Exception E) {
				Dbutil.logger.error("Problem while generating Ratings html", E);
			}

		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		RatingInfo ri = new RatingInfo();
		ri.html = sb.toString();
		ri.authornote = authorinfo;
		ri.ratingnote = ratingnote;

		return ri;

	}

	public static RatingInfo getWave1RatingsHTMLOriginal(int knownwineid, int width, String searchpage,
			int singlevintage, Searchdata searchdata, Translator t, boolean showeditbuttons) {
		String html = "";
		StringBuffer sb = new StringBuffer();
		String ratingnote = "";
		ResultSet rs = null;
		WineLibraryTV wltv = null;
		String query;
		boolean hasratings = false;
		String authorinfo = "";
		String goodpqvintages = "";
		String knownwinename = "";
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> winemap = new HashMap<String, String>();
		HashMap<String, Integer> ratedwinemap = new HashMap<String, Integer>();
		HashMap<String, String> linkmap = new HashMap<String, String>();
		TreeSet<Integer> vintages = new TreeSet<Integer>();
		HashSet<String> authors = new HashSet<String>();
		Connection con = Dbutil.openNewConnection();
		if (knownwineid > 0) {
			if (true || Dbutil.readIntValueFromDB(
					"select count(*) as thecount from ratedwines where knownwineid=" + knownwineid + " and rating>70;",
					"thecount") > 0) {
				int currentvintage;
				int startvintage = 0;
				String singlevintageclause = "";
				String cellclass;
				try {
					wltv = new WineLibraryTV(knownwineid, singlevintage + "");
					String[] glass = Knownwines.getGlassImage(knownwineid);
					Knownwine k = new Knownwine(knownwineid);
					k.getProperties();
					knownwinename = k.name.replaceAll("&", "&amp;");
					// See if there is a label
					String labeldiv = "";
					if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
							+ knownwineid + ".jpg").exists()) {
						labeldiv = "<div class='label'><img src='" + Configuration.staticprefix + "/labels/"
								+ knownwineid + ".jpg' alt=\"" + knownwinename
								+ "\" onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
					} else if (new File(Configuration.workspacedir + "labels" + System.getProperty("file.separator")
							+ knownwineid + ".gif").exists()) {
						labeldiv = "<div class='label'><img src='" + Configuration.staticprefix + "/labels/"
								+ knownwineid + ".gif' alt='" + knownwinename.replace("'", "&apos;")
								+ "' onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";

					}

					// wine div
					sb.append("<div class='winecontainer'>" +
					// "<img class='wineglass' src='/css2/d"+glass[0]+"' alt='"+glass[1]+"'/>" +
							"<div class='wineglass sprite glass sprite-d" + glass[0].replaceAll(".jpg", "")
							+ "'>&nbsp;</div>" +

							"<div class='wine'>" + labeldiv
							+ (true ? "<div class='tastingnote' onclick='vpclick(\"/winelinks.jsp?source=CT&amp;name="
									+ URLEncode(removeAccents(k.uniquename))
									+ (searchdata.vintage.length() > 0 ? "&amp;vintage=" + searchdata.vintage : "")
									+ "\");' onmouseout='javascript:hide(\"tnsuperholder\");'>" + "</div>" : "")
							+ "<div class='winename'><h1>" + getWineNameLink(k)
							+ (singlevintage > 0 ? " " + singlevintage : "") + "</h1></div>" + "<div class='region'>");
					String region = Region.getRegion(knownwineid);
					String locale = k.getProperties().get("locale");
					String regionpath = "";
					for (String r : locale.split(", ")) {
						if (!r.equals("All")) {
							regionpath += "/" + r.replaceAll("/", "%252F");
							if (singlevintage == 0) {
								sb.append("<a href='/region" + removeAccents(regionpath) + "/'>" + r + "</a>");
							} else {
								sb.append("<span class='jslink' onclick='vpclick(\"/region" + removeAccents(regionpath)
										+ "\");'>" + r + "</span>");
							}
							if (!region.equals(r))
								sb.append(" &raquo; ");
						}
					}
					sb.append(
							"</div>" + (wltv != null && !"".equals(wltv.url)
									? "<a href='/winelinks.jsp?source=GV&amp;name="
											+ URLEncode(removeAccents(k.uniquename))
											+ (searchdata.vintage.length() > 0 ? "&amp;vintage=" + searchdata.vintage
													: "")
											+ "' ><div class='wltv'></div></a>"
									: "") + "<a href='/link.jsp?exttarget=Twitter&amp;exturl="
									+ URLEncode("http://twitter.com/home?status=%23nowdrinking "
											+ URLEncodeUTF8(knownwinename).replaceAll("'", "&apos;")
											+ " https://www.vinopedia.com/wine/"
											+ URLEncode(removeAccents(k.uniquename)).replaceAll("'", "&apos;"))
									+ "' title='Click to send this page to Twitter!' target='_blank' rel='nofollow'><img id='twitdrinkingnow' src='"
									+ Configuration.cdnprefix
									+ "/images/twitterdrinkingwine.png' alt='Twitter this wine'/></a>"
									+ "</div><div id='ratingexplanation' onmouseover='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'></div>");

					ratingnote = "<div class='hreview-aggregate'>For <span class='item'><span class='fn'>"
							+ knownwinename + "</span></span> we found <span class='count'>"
							+ Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="
									+ knownwineid + " group by knownwineid", "thecount")
							+ "</span> ratings with an average of <span class='rating'><span class='average'>"
							+ Dbutil.readIntValueFromDB(
									"select avg(rating) as rating from ratinganalysis where author='FWS' and knownwineid="
											+ knownwineid + " group by knownwineid",
									"rating")
							+ "</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span></div>";

					// select vintages

					int totalwidth = 170; // author width
					if (singlevintage > 0) {
						rs = Dbutil.selectQuery("select *,max(ratinghigh) as high from ratedwines where knownwineid="
								+ knownwineid + " and author!='FWS' and vintage=" + singlevintage
								+ " and rating>70 group by vintage order by vintage desc;", con);
						if (rs.next()) {
							vintages.add(singlevintage);
							if (rs.getInt("high") > 0) {

								totalwidth += 65;
							} else {
								totalwidth += 50;
							}
						}
						Dbutil.closeRs(rs);
					}
					rs = Dbutil.selectQuery("select *,max(ratinghigh) as high from ratedwines where knownwineid="
							+ knownwineid + " and author!='FWS' and vintage!=" + singlevintage
							+ " and rating>70 group by vintage order by vintage desc;", con);
					while (totalwidth < width && rs.next()) {
						hasratings = true;
						if (rs.getInt("high") > 0) {
							totalwidth += 65;
						} else {
							totalwidth += 50;
						}
						if (totalwidth < width) {
							vintages.add(rs.getInt("vintage"));
							startvintage = rs.getInt("vintage") - 1;
							singlevintageclause = " or vintage=" + singlevintage;
						}
					}
					Dbutil.closeRs(rs);
					String selectedvintages = "";
					for (Integer vin : vintages.toArray(new Integer[0])) {
						selectedvintages += "," + vin;
					}
					if (selectedvintages.length() > 1)
						selectedvintages = selectedvintages.substring(1);
					Dbutil.closeRs(rs);
					rs = Dbutil.selectQuery("select vintage,0 as high from wines where knownwineid=" + knownwineid
							+ " and vintage>0 "
							+ (selectedvintages.length() > 0 ? "and not vintage in (" + selectedvintages + ")" : "")
							+ " group by vintage order by vintage desc;", con);
					while (totalwidth < width && rs.next()) {
						totalwidth += 50;
						if (totalwidth < width) {
							vintages.add(rs.getInt("vintage"));
						}
					}
					Dbutil.closeRs(rs);
					selectedvintages = "";
					for (Integer vin : vintages.toArray(new Integer[0])) {
						selectedvintages += "," + vin;
					}
					if (selectedvintages.length() > 1)
						selectedvintages = selectedvintages.substring(1);
					String authorwidth = "";
					if (totalwidth < width)
						authorwidth = " style='width:" + (width - totalwidth) + "px;'";
					if (selectedvintages.equals("")) {
						sb.append("<table class='ratings'><tr><td class='noratings'>No ratings available</td>");
						sb.append("<td style='width:780px;'></td>");
						sb.append("<td class='roundrvintageno'></td></tr></table>");
					} else {
						// Determine which vintages have the best PQ ratio
						query = "select ra.*, (minpriceeuroex/price) as pq from ratinganalysis ra join pqratio pq on (ra.rating=pq.rating) where  ra.minpriceeuroex>0 and knownwineid="
								+ knownwineid + " and author='FWS' and vintage in (" + selectedvintages
								+ ") order by pq;";
						rs = Dbutil.selectQuery(query, con);
						if (rs.next()) {
							rs.last();
							int totalvintages = rs.getRow();
							rs.beforeFirst();
							if (totalvintages > 3) {
								int n = Math.round((float) ((totalvintages / 3) + 0.5));
								for (int i = 0; i < n; i++) {
									rs.next();
									goodpqvintages += "*" + rs.getInt("vintage");
								}
								goodpqvintages += "*";
							}
						}
						Dbutil.closeRs(rs);

						// Get all different authors
						rs = Dbutil.selectQuery("select * from ratedwines where knownwineid=" + knownwineid
								+ " and author!='FWS' and vintage in (" + selectedvintages
								+ ") and rating>70 group by author;", con);
						while (rs.next()) {
							authors.add(rs.getString("author"));
						}
						Dbutil.closeRs(rs);
						// fill map
						query = "select * from ratedwines where knownwineid=" + knownwineid + " and (vintage>"
								+ startvintage + singlevintageclause + " ) and author!='FWS' and rating>70 ;";
						rs = Dbutil.selectQuery(query, con);
						while (rs.next()) {
							if (linkmap.get("RP") == null && rs.getString("author").equals("RP")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("erobertparker"))
								linkmap.put(rs.getString("author"), "/winelinks.jsp?name="
										+ URLEncode(removeAccents(k.uniquename)) + "&amp;source=RP");
							if (linkmap.get("WS") == null && rs.getString("author").equals("WS")
									&& rs.getString("sourceurl") != null
									&& rs.getString("sourceurl").contains("winespectator")) {
								linkmap.put(rs.getString("author"), "/winelinks.jsp?name="
										+ URLEncode(removeAccents(k.uniquename)) + "&amp;source=WS");
							}
							map.put(rs.getString("author") + rs.getString("vintage"), rs.getString("rating")
									+ (rs.getInt("ratinghigh") > 0 ? "-" + rs.getString("ratinghigh") : ""));
							winemap.put(rs.getString("author") + rs.getString("vintage"),
									rs.getString("name").replaceAll("'", "&apos;"));
							ratedwinemap.put(rs.getString("author") + rs.getString("vintage"), rs.getInt("id"));
						}
						Dbutil.closeRs(rs);

						// Header rows
						sb.append(
								"<table class='ratings'><tr class='ratingsheader'><td class='ratingstar' " + (hasratings
										? "onclick='showtop(\"ratingexplanation\")' onmouseout='hidetop(\"ratingexplanation\")'>Expert ratings"
										: ">Available vintages") + "</td>");
						if (!"".equals(authorwidth))
							sb.append("<td class='nobackground' " + authorwidth + "></td>");
						int td = 0;
						for (int vintage : vintages) {
							if (td == vintages.size() - 1) {
								if (hasratings) {
									sb.append("<td class='sprite sprite-roundrvintage" + (td % 2 == 0 ? "e" : "o")
											+ "'><a href='/wine/" + URLEncode(removeAccents(k.uniquename))
											+ (singlevintage > 0 ? "" : "+" + vintage) + "'"
											+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'"
													: "")
											+ (singlevintage == 0
													? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
															+ vintage + "'"
													: " title='" + knownwinename.replaceAll("'", "&apos;")
															+ "' onclick='javascript:window.location=\"/wine/"
															+ URLEncode(removeAccents(k.uniquename)) + "+" + vintage
															+ "\";return false;'")
											+ " >" + (vintage > 0 ? vintage : "NV") + "</a></td>");
								} else {
									sb.append("<td class='sprite-roundrvintage" + (td % 2 == 0 ? "e" : "o")
											+ "'><a href='/wine/" + URLEncode(removeAccents(k.uniquename))
											+ (singlevintage > 0 ? "" : "+" + vintage) + "'"
											+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'"
													: "")
											+ (singlevintage == 0
													? " title='" + knownwinename.replaceAll("'", "&apos;") + " "
															+ vintage + "'"
													: " title='" + knownwinename.replaceAll("'", "&apos;")
															+ "' onclick='javascript:window.location=\"/wine/"
															+ URLEncode(removeAccents(k.uniquename)) + "+" + vintage
															+ "\";return false;'")
											+ " >" + (vintage > 0 ? vintage : "NV") + "</a></td>");
								}
							} else {
								sb.append("<td class='spriter spriter-vintage" + (td % 2 == 0 ? "e" : "o")
										+ "'><a href='/wine/" + URLEncode(removeAccents(k.uniquename))
										+ (singlevintage > 0 ? "" : "+" + vintage) + "'"
										+ (goodpqvintages.contains("*" + vintage + "*") ? " class='recommended'" : "")
										+ (singlevintage == 0
												? " title='" + knownwinename.replaceAll("'", "&apos;") + " " + vintage
														+ "'"
												: " title='" + knownwinename.replaceAll("'", "&apos;")
														+ "' onclick='javascript:window.location=\"/wine/"
														+ URLEncode(removeAccents(k.uniquename)) + "+" + vintage
														+ "\";return false;'")
										+ " >" + (vintage > 0 ? vintage : "NV") + "</a></td>");
							}
							td++;
						}
						sb.append("</tr>");

						// Loop over authors and vintages
						int tr = 0;
						for (String author : authors) {
							td = 0;
							/*
							 * String stars="<sup>&#42"; for (int j=0;j<tr;j++) stars+="&#42";
							 * stars+="</sup>"; authorinfo+=stars+"&nbsp;"+getAuthorNote(author);
							 */
							if (tr == authors.size() - 1) {
								sb.append("<tr class='ratingsrow'><td class='sprite sprite-roundauthor"
										+ (tr % 2 == 0 ? "e" : "o") + "'>"
										+ (linkmap.get(author) != null ? "<a href='" + linkmap.get(author)
												+ "' target='_blank' rel='nofollow'>" : "")
										+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "")
										+ "</td>\n");
							} else {
								sb.append("<tr class='ratingsrow'><td class='author" + (tr % 2 == 0 ? "e" : "o") + "'>"
										+ (linkmap.get(author) != null
												? "<a href='" + linkmap.get(author) + "' rel='nofollow'>"
												: "")
										+ getAuthorName(author) + (linkmap.get(author) != null ? "</a>" : "")
										+ "</td>\n");
							}

							if (!"".equals(authorwidth))
								sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o") + "o'" + authorwidth
										+ "></td>");
							for (int vintage : vintages) {
								if (tr == authors.size() - 1 && td == vintages.size() - 1) {
									sb.append("<td class='roundrrating" + (tr % 2 == 0 ? "e" : "o")
											+ (td % 2 == 0 ? "e" : "o") + "' title='"
											+ (winemap.get(author + vintage) == null ? ""
													: winemap.get(author + vintage).replaceAll("&", "&amp;")
															+ ": Rating " + map.get(author + vintage))
											+ "'>"
											+ (map.get(author + vintage) == null ? "" : map.get(author + vintage))
											+ (map.get(author + vintage) != null && showeditbuttons
													? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
															+ ratedwinemap.get(author + vintage)
															+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
													: "")
											+ "</td>");
								} else {
									sb.append("<td class='rating" + (tr % 2 == 0 ? "e" : "o")
											+ (td % 2 == 0 ? "e" : "o") + "' title='"
											+ (winemap.get(author + vintage) == null ? ""
													: winemap.get(author + vintage).replaceAll("&", "&amp;")
															+ ": Rating " + map.get(author + vintage))
											+ "'>"
											+ (map.get(author + vintage) == null ? "" : map.get(author + vintage))
											+ (map.get(author + vintage) != null && showeditbuttons
													? "<a href='/admin/ai/correctknownwineid.jsp?ratedwineid="
															+ ratedwinemap.get(author + vintage)
															+ "&newknownwineid=0' target='_blank'><img src='/images/edit.gif' alt='Edit'/></a>"
													: "")
											+ "</td>");
								}
								td++;
							}
							sb.append("</tr>");
							tr++;
						}
						sb.append("</table>");
					}
					sb.append("</div>");
					// sb.append("<div class=\"note\">Ratings copyright of their respective owners.
					// See <a href='/disclaimer.jsp'>\"Disclaimer\"</a> for details. </div>");
				} catch (Exception E) {
					Dbutil.logger.error("Problem while generating Ratings html", E);
				}
			}
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		RatingInfo ri = new RatingInfo();
		ri.html = sb.toString();
		ri.authornote = authorinfo;
		ri.ratingnote = ratingnote;

		return ri;

	}

	public static String getWineNameLink(Knownwine k) {
		String title = k.name;
		if (title.startsWith(k.getProperties().get("producer"))) {
			title = "<a style='text-decoration:underline;color:white;' href='/winery/"
					+ Webroutines.URLEncodeUTF8Normalized(k.getProperties().get("producer")).replaceAll("%2F", "/")
							.replace("&", "&amp;")
					+ "'>" + k.getProperties().get("producer").replace("&", "&amp;") + "</a> "
					+ title.substring(k.getProperties().get("producer").length()).replace("&", "&amp;");
		} else {
			title = title.replace("&", "&amp;");
		}
		return title;
	}

	public static String getWineNameLinkHtml5(Knownwine k) {
		String title = k.name;
		if (title.startsWith(k.getProperties().get("producer"))) {
			if (title.substring(k.getProperties().get("producer").length()).equals("")) {
				title = "<a  style='text-decoration:underline;color:white;' href='/winery/"
						+ Webroutines.URLEncodeUTF8Normalized(k.getProperties().get("producer")).replaceAll("%2F", "/")
								.replace("&", "&amp;")
						+ "'><span itemprop='name'>" + k.getProperties().get("producer").replace("&", "&amp;")
						+ "</span></a>";
			} else {
				title = "<a style='text-decoration:underline;color:white;' href='/winery/"
						+ Webroutines.URLEncodeUTF8Normalized(k.getProperties().get("producer")).replaceAll("%2F", "/")
								.replace("&", "&amp;")
						+ "'><span itemprop='brand'>" + k.getProperties().get("producer").replace("&", "&amp;")
						+ "</span></a> <span itemprop='name'>"
						+ title.substring(k.getProperties().get("producer").length()).replace("&", "&amp;") + "</span>";
			}
		} else {
			title = "<span itemprop='name'>" + title.replace("&", "&amp;") + "</span>";
		}
		return title;
	}

	public static String winelink(String winename, int vintage, boolean mobile) {
		return (mobile ? "/mwine/" : "/wine/") + URLEncode(removeAccents(winename)).replaceAll("%2F", "/")
				+ (vintage > 0 ? "+" + vintage : "");
	}

	public static String winelink(String winename, int vintage) {
		return winelink(winename, vintage, false);
	}

	public static class RatingInfo {
		public String html;
		public String authornote;
		public String ratingnote;

	}

	public static String mobileprices(Wineset wineset, PageHandler p) {
		if (wineset != null && wineset.Wine != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < wineset.Wine.length; i++) {
				sb.append("<div class='divider'>");
				sb.append("<div class='flag'><img src='" + Configuration.cdnprefix
						+ "/images/transparent.gif' alt='country' class='sprite flag sprite-"
						+ wineset.Wine[i].Country.toLowerCase() + "'/></div>");
				sb.append("<div class='price' >" + Webroutines
						.formatPriceMobile(wineset.Wine[i].PriceEuroIn, wineset.Wine[i].PriceEuroEx,
								p.searchdata.getCurrency(), "EX")
						.replaceAll("&nbsp;", "&#160;").replaceAll("&euro;", "&#8364;") + "</div>");
				sb.append("<div class='size' >"
						+ Webroutines.formatSizecompact(wineset.Wine[i].Size).replaceAll("&nbsp;", "&#160;")
						+ "</div>");
				sb.append("<div class='shop'><a href='/mdetails.jsp?wineid=" + wineset.Wine[i].Id + "' >"
						+ Spider.escape(wineset.Wine[i].Shopname) + "</a></div>");
				sb.append(
						"<div class='wine'>"
								+ (wineset.Wine[i].Vintage + " "
										+ Spider.escape(Webroutines.formatCapitals(wineset.Wine[i].Name))).trim()
								+ "</div>");
				sb.append("</div>");
			}
			return sb.toString();
		} else {
			return "";
		}
	}

}
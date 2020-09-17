package com.freewinesearcher.batch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.online.Webroutines;

/**
 * @author Jasper Analysis routine which can try out multiple combinations of
 *         likely parameters for scraping a site. When no parameters are given,
 *         a standard set is tried. When some parameters are given, they are
 *         considered fixed and will not change.
 */
public class TableScraper {
	ArrayList<ArrayList<String>> Config = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<Integer>> Score = new ArrayList<ArrayList<Integer>>();

	TableScraper(String Page, String url, String fixedwinesep, String fixedfieldsep, String fixedvintageregex,
			String fixedpriceregex, String fixedbottleregex) {

		ArrayList<String> Configrecord = new ArrayList<String>();
		ArrayList<Integer> Scorerecord = new ArrayList<Integer>();
		ArrayList<String> classtags = getClassTags(Page);
		String nameorder = "";
		String sizeorder = "";
		int[] namecolumnarray;
		int[] sizecolumnarray;
		// String nameregex = "";
		// String nameexclpattern = "";
		// String vintageorder = "";
		// String priceorder = "";
		// String bottleorder = "";
		String record = "";
		Pattern pattern;
		Matcher matcher;
		int maxarraysize = 0;
		int tempmaxarraysize = 0; // Simply the largest number of columns found in a record
		int pricematchcolumn = 0; // The largest number of columns found for records with a valid price
		int vintagematchcolumn = 0;
		int pricematches = 0;
		int vintagematches = 0;
		int namematches = 0;
		int sizematches = 0;
		ArrayList<Integer> count;
		String generalpriceregex = "^(?:.*?\\D)??((\\d+[ ,.])?\\d+ ?[,.]\\d\\d)(?:\\D.*?)?$";
		String generalvintageregex = "^(?:.*?\\D)?((18\\d\\d)|(19\\d\\d)|(20\\d\\d))(?:\\D.*?)?$";
		String generalvintageregexgermany = "^(?:.*?\\D)?((18\\d\\d)|(19\\d\\d)|(20\\d\\d)|(\\d\\d))(?:\\D.*?)?$";
		String vintageregexfound = "";
		// String name = null;
		// String vintage = null;
		ArrayList<String> Winesep = new ArrayList<String>();
		ArrayList<String> Fieldsep = new ArrayList<String>();
		ArrayList<String> Vintageregex = new ArrayList<String>();
		ArrayList<String> Priceregex = new ArrayList<String>();
		// ArrayList<String> Bottleregex = new ArrayList<String>();
		if (fixedwinesep.equals("")) {
			for (String tag : classtags) {
				Winesep.add(tag);
			}
			Winesep.add("<table");
			Winesep.add("<form");
			Winesep.add("<tr");
		} else {
			Winesep.add(fixedwinesep);
		}
		if (fixedfieldsep == null || fixedfieldsep.equals("")) {
			Fieldsep.add("<td");
			Fieldsep.add("<div");

		} else {
			Fieldsep.add(fixedfieldsep);
		}
		if (url.startsWith("pdf")) {
			Winesep = new ArrayList<String>();
			Fieldsep = new ArrayList<String>();
			Winesep.add("<tr");
			Fieldsep.add("<td");
		}
		if (fixedvintageregex == null || fixedvintageregex.equals("")) {
			Vintageregex.add(generalvintageregex);
			Vintageregex.add(generalvintageregexgermany);

		} else {
			Vintageregex.add(fixedvintageregex);
		}
		if (fixedpriceregex == null || fixedpriceregex.equals("")) {
			Priceregex.add(generalpriceregex);
		} else {
			Priceregex.add(fixedpriceregex);
			generalpriceregex = fixedpriceregex;
		}

		// String[] Eurosign = { "&euro;", "eur", "EUR", "Eur", "Euro", "�", "&8364;" };

		float price = 0;
		String priceString = "";
		// boolean rareold = Wine.isRareOld(url);
		// String timestampstring = "";
		// ResultSet rs;
		ArrayList<ArrayList<String>> wines;
		ArrayList<ArrayList<String>> allrecords;

		for (String winesep : Winesep) {
			for (String fieldsep : Fieldsep) {
				try {
					// Pattern p = Pattern.compile(winesep);
					// p = Pattern.compile(fieldsep);
					allrecords = new ArrayList<ArrayList<String>>(0);

					allrecords = scrapetable(Page, winesep, fieldsep);
					// Webpage split by the field separator and the wine separator
					tempmaxarraysize = 0;
					for (int i = 0; i < allrecords.size(); i++) { // Find dimension of largest array
						if (tempmaxarraysize < allrecords.get(i).size()) {
							tempmaxarraysize = allrecords.get(i).size();
						}
					}

					// find out which column contains prices
					count = new ArrayList<Integer>(0);
					for (int i = 0; i < tempmaxarraysize; i++) {
						count.add(0);
					}

					maxarraysize = 0;
					for (int i = 1; i < allrecords.size(); i++) {
						for (int j = 2; j < allrecords.get(i).size(); j++) {
							pattern = Pattern.compile("([Ll]iter)", Pattern.DOTALL); // filter out price per liter
							matcher = pattern.matcher(allrecords.get(i).get(j));
							// if (!matcher.find()) {
							if (true) { // price per liter may be appended to normal price
								pattern = Pattern.compile(generalpriceregex, Pattern.DOTALL);
								matcher = pattern.matcher(allrecords.get(i).get(j));
								if (matcher.find()) {
									priceString = matcher.group(1);

									if (true) {
										price = 0;
										price = getPrice(buildMatch(allrecords.get(i), (j - 1) + "", Priceregex.get(0),
												"", Page, "Price"));

										if (price != 0) {
											count.set(j, count.get(j) + 1);
											if (maxarraysize < allrecords.get(i).size()) { // &&i!=0&&i<allrecords.size()
																							// remove as a condition
												maxarraysize = allrecords.get(i).size();

											}
										}
									}
									/*
									 * else {
									 * 
									 * if (!priceString.equals("")) { if (!priceString.equals("0.75")) if
									 * (!priceString.equals("0,75")) try { price = 0; if (priceString.length() > 2)
									 * { if (priceString .substring((priceString.length() - 3),
									 * (priceString.length() - 2)) .equals(".") || priceString
									 * .substring((priceString.length() - 3), (priceString.length() - 2))
									 * .equals(",")) { priceString = Spider.replaceString(priceString, ".", "");
									 * priceString = Spider.replaceString(priceString, ",", ""); price =
									 * Float.valueOf(priceString).floatValue() / 100; } } if (price == 0) { price =
									 * Float.valueOf(priceString.replaceAll(",", ".")) .floatValue(); } if (price !=
									 * 0) { count.set(j, count.get(j) + 1); if (maxarraysize <
									 * allrecords.get(i).size()) { // &&i!=0&&i<allrecords.size() // remove as // a
									 * // condition maxarraysize = allrecords.get(i).size();
									 * 
									 * } } } catch (Exception e) {
									 * Dbutil.logger.info("Could not parse price with priceString " + priceString +
									 * " for wine " + name + " at URL " + url); }
									 * 
									 * } }
									 */
								}

							}
						}
					}
					pricematches = 0;
					for (int j = 0; j < maxarraysize; j++) {
						if (pricematches < count.get(j)) {
							pricematches = count.get(j);
							pricematchcolumn = j;
						}

					}
					pricematchcolumn = pricematchcolumn - 1;
					// This is because we added an extra field with the total winefield to the
					// arraylist.
					if (pricematchcolumn == -1)
						pricematchcolumn = 0;
					// now we know the price column, construct a new resultset with wines
					wines = new ArrayList<ArrayList<String>>(0);
					try {
						for (int i = 1; i < allrecords.size(); i++) {
							priceString = buildMatch(allrecords.get(i), String.valueOf(pricematchcolumn),
									Priceregex.get(0), "", "", "Price");
							price = getPrice(priceString);
							if (price > 1.5) { // Valid price found
								wines.add(allrecords.get(i));
							}
						}
					} catch (Exception E) {
						Dbutil.logger.error("Exception determining the price using priceString " + priceString);
					}

					// find out which column contains the vintage
					count = new ArrayList<Integer>(0);
					for (int i = 0; i < maxarraysize; i++) {
						count.add(0);
					}
					for (int i = 0; i < wines.size(); i++) {
						for (int j = 0; j < Vintageregex.size(); j++) {
							pattern = Pattern.compile(Vintageregex.get(j), Pattern.DOTALL);
							for (int k = 2; k < wines.get(i).size(); k++) {
								matcher = pattern.matcher(wines.get(i).get(k));
								if (matcher.find()) {
									if (!matcher.group(1).equals("")) {
										if (k >= count.size()) {
											// Dbutil.logger.info(k+" "+wines.get(i).get(k));
										} else {
											count.set(k, count.get(k) + 1);
										}
									}
								}
							}
						}
					}
					vintagematches = 0;
					vintagematchcolumn = 0;
					for (int j = 1; j < maxarraysize; j++) {
						if (vintagematches < count.get(j)) {
							vintagematches = count.get(j);
							vintagematchcolumn = j;
						}
					}
					vintagematchcolumn = vintagematchcolumn - 1; // This is because we added an extra field with the
																	// total winefield to the arraylist.
					// vintageorder = String.valueOf(vintagematchcolumn);

					if (Vintageregex.size() > 1 && vintagematchcolumn >= 0) {
						// find out which regex best matches the vintage if more than one regex
						// if it is a choice between standard and german variant, only choose the German
						// variant if the normal one does not work.
						count = new ArrayList<Integer>(0);
						for (int i = 0; i < Vintageregex.size(); i++) {
							count.add(0);
						}
						for (int i = 0; i < wines.size(); i++) {
							for (int j = 0; j < Vintageregex.size(); j++) {
								if (wines.get(i).size() > vintagematchcolumn) {
									String vintageregex = Vintageregex.get(j);
									pattern = Pattern.compile(vintageregex, Pattern.DOTALL);
									matcher = pattern.matcher(wines.get(i).get(vintagematchcolumn));
									if (matcher.find()) {
										if (!matcher.group(1).equals("") && !matcher.group(1).equals("75")) { // 75 from
																												// 0,75
																												// liter
											count.set(j, count.get(j) + 1);
										}
									}
								}
							}
						}
						vintagematches = 0;
						for (int j = 0; j < Vintageregex.size(); j++) {
							if (vintagematches < count.get(j)) {
								vintagematches = count.get(j);
								vintageregexfound = Vintageregex.get(j);
							}
						}
						if (Vintageregex.size() == 2) { // Standard and Germany
							if (2 * count.get(0) < wines.size() && count.get(1) > 2 * count.get(0)) {
								// less than 50% found with normal and twice as many with german than normal
								vintagematches = count.get(1);
								vintageregexfound = Vintageregex.get(1);
							} else {
								vintageregexfound = Vintageregex.get(0);
							}
						}

					} else {
						vintageregexfound = Vintageregex.get(0);
					}
					// Construct name order
					// Step 1: Go through each column and see if it contains useful information
					// Step 2: If information is found, add to the nameorder
					namecolumnarray = new int[maxarraysize];
					for (int t = 0; t < maxarraysize; t++) {
						namecolumnarray[t] = 0;
					}
					for (int i = 1; i < wines.size(); i++) {
						for (int j = 2; j < maxarraysize; j++) {
							if (j < wines.get(i).size()) {
								record = Spider.unescape(wines.get(i).get(j));
								if (record != null) {
									pattern = Pattern.compile("[a-z,A-Z]{3}");
									matcher = pattern.matcher(
											record.replaceAll("iter", "").replaceAll("Eur", "").replaceAll("EUR", ""));
									if (matcher.find()) {
										if (!record.startsWith("http://")) {
											namecolumnarray[j] = namecolumnarray[j] + 1;
										}

									}
								}

							}
						}
					}
					nameorder = "";
					namematches = 0;
					// First try without price column, then with pricecolumn if no other is found
					for (int i = 0; i < maxarraysize; i++) {
						if (i != pricematchcolumn + 1) {
							if (namecolumnarray[i] * 4 > wines.size()) { // threshold: 1/4 must contain useful
																			// information
								if (!nameorder.equals(""))
									nameorder = nameorder + ";";
								nameorder = nameorder + (i - 1);
								if (namematches < namecolumnarray[i])
									namematches = namecolumnarray[i];
							}
						}
					}
					if (namematches == 0) { // Only price column is found
						for (int i = 0; i < maxarraysize; i++) {

							if (namecolumnarray[i] * 4 > wines.size()) { // threshold: 1/4 must contain useful
																			// information
								if (!nameorder.equals(""))
									nameorder = nameorder + ";";
								nameorder = nameorder + (i - 1);
								if (namematches < namecolumnarray[i])
									namematches = namecolumnarray[i];
							}

						}
					}
					// Construct bottle size order
					// Step 1: Go through each column and see if it contains useful information
					// Step 2: If information is found, add to the bottleorder
					sizecolumnarray = new int[maxarraysize];
					for (int i = 1; i < wines.size(); i++) {
						for (int j = 2; j < maxarraysize; j++) {
							if (j < wines.get(i).size()) {
								record = Spider.unescape(wines.get(i).get(j));
								if (record != null) {
									for (int k = 0; k < Configuration.sizeregex.length; k++) {
										pattern = Pattern.compile(Configuration.sizeregex[k]);
										matcher = pattern.matcher(record);
										if (matcher.find()) {
											sizecolumnarray[j] = sizecolumnarray[j] + 1;
										}

									}
								}

							}
						}
					}
					sizeorder = "";
					for (int i = 0; i < maxarraysize; i++) {
						if (i != pricematchcolumn)

							if (sizecolumnarray[i] * 4 > vintagematches) { // threshold: 1/4 must contain useful
																			// information
								if (!sizeorder.equals(""))
									sizeorder = sizeorder + ";";
								sizeorder = sizeorder + (i - 1);
								if (sizematches < namecolumnarray[i])
									sizematches = sizecolumnarray[i];
							}

					}

					Configrecord = new ArrayList<String>(0);
					Configrecord.add(winesep);
					Configrecord.add(fieldsep);
					Configrecord.add(nameorder);
					Configrecord.add(sizeorder);
					Configrecord.add(""); // nameregex
					String namefilter = "(?<!&#)\\d\\d\\d\\d+";
					if (vintageregexfound.equals(generalvintageregexgermany)) {
						namefilter += ":\\d\\der";
					}
					Configrecord.add(namefilter); // namefilter
					Configrecord.add(String.valueOf(vintagematchcolumn));
					Configrecord.add(vintageregexfound);
					Configrecord.add(String.valueOf(pricematchcolumn));
					Configrecord.add(generalpriceregex);
					Config.add(Configrecord);

					Scorerecord = new ArrayList<Integer>(0);
					Scorerecord.add(pricematches); // # of found wines
					Scorerecord.add(vintagematches); // # of found wines
					if (nameorder.contains(String.valueOf(pricematchcolumn))) {
						Scorerecord.add(0);
					} else {
						Scorerecord.add(namematches); // # of found wine descriptions
					}
					Scorerecord.add(sizematches); // # of found wine descriptions
					Score.add(Scorerecord);

				} catch (Exception e) {
				}
			}

		}

	}

	public static float parsePrice(String priceString) {
		float price = 0;
		try {
			if (priceString.length() > 2) {
				if (priceString.substring((priceString.length() - 3), (priceString.length() - 2)).equals(".")
						|| priceString.substring((priceString.length() - 3), (priceString.length() - 2)).equals(",")) {
					priceString = Spider.replaceString(priceString, ".", "");
					priceString = Spider.replaceString(priceString, ",", "");
					price = Float.valueOf(priceString).floatValue() / 100;
				}
			}
		} catch (Exception e) {
		}
		try {
			if (price == 0 && priceString.length() > 0) {
				price = Float.valueOf(priceString.replaceAll(",", ".")).floatValue();
			}
		} catch (Exception e) {
		}
		return price;
	}

	public static ArrayList<String> Analyzer(String Page, String url, String fixedwinesep, String fixedfieldsep,
			String fixedvintageregex, String fixedpriceregex, String fixedbottleregex) {
		StringBuffer html = new StringBuffer();
		String Pageescaped = Page.replaceAll("\r?\n", "");
		int score;
		int bestmatch = 0;
		int mostwinesfound = 0;
		// int mostvintagesfound = 0;
		ArrayList<ArrayList<String>> wines = new ArrayList<ArrayList<String>>();
		ArrayList<String> results = new ArrayList<String>(0);
		TableScraper ts = new TableScraper(Page, url, fixedwinesep, fixedfieldsep, fixedvintageregex, fixedpriceregex,
				fixedbottleregex);
		html.append("<TABLE><TR>");
		html.append("<TH ></TH>");
		html.append("<TH >Winesep</TH>");
		html.append("<TH >Fieldsep</TH>");
		html.append("<TH >Wines Found</TH>");
		html.append("<TH >Descriptions Found</TH>");
		html.append("<TH >Vintages Found</TH>");
		html.append("<TH >Bottle Sizes Found</TH>");
		html.append("</TR>");

		for (int i = 0; i < ts.Config.size(); i++) {
			score = ts.Score.get(i).get(0) + ts.Score.get(i).get(2);
			if (mostwinesfound < score) {
				mostwinesfound = score;
				bestmatch = i;

			}
		}
		for (int i = 0; i < ts.Config.size(); i++) {
			score = ts.Score.get(i).get(0) + ts.Score.get(i).get(2);
			if (score * 3 > mostwinesfound) {
				html.append("<TR><TD><input type=\"button\" value=\"Use this\" onclick=\"javascript:customanalyze('"
						+ Spider.replaceString(ts.Config.get(i).get(0), "\"", "&quot;") + "','"
						+ ts.Config.get(i).get(1) + "')\"></TD>");
				html.append("<TD>" + Spider.escape(ts.Config.get(i).get(0)) + "</TD>");
				html.append("<TD>" + Spider.escape(ts.Config.get(i).get(1)) + "</TD>");
				html.append("<TD>" + String.valueOf(ts.Score.get(i).get(0)) + "</TD>");
				html.append("<TD>" + String.valueOf(ts.Score.get(i).get(2)) + "</TD>");
				html.append("<TD>" + String.valueOf(ts.Score.get(i).get(1)) + "</TD>");
				html.append("<TD>" + String.valueOf(ts.Score.get(i).get(3)) + "</TD>");
				html.append("</TR>");
			}
		}
		html.append("</TABLE>");
		wines = scrapetable(Page, ts.Config.get(bestmatch).get(0), ts.Config.get(bestmatch).get(1));
		html.append("<TABLE>");
		for (int i = 5; i < wines.size() && i < 20; i++) {
			html.append("<TR>");
			for (int j = 2; j < wines.get(i).size(); j++)
				html.append("<td>" + (j - 1) + "=" + wines.get(i).get(j) + "   </td>");
			html.append("</TR>");

		}
		html.append("</TABLE>");
		html.append("<TABLE>");
		for (int i = 0; i < wines.size(); i++) {
			html.append("<TR>" + getName(wines.get(i), ts.Config.get(bestmatch).get(2), "", "", "", Pageescaped, true)
					+ "</TR>");
		}
		html.append("</TABLE>");
		results.add(html.toString());
		for (int j = 0; j < ts.Config.get(bestmatch).size(); j++) {
			results.add(ts.Config.get(bestmatch).get(j));
		}
		return results;
	}

	public static Wine[] ScrapeWine(String Page, String baseurl, int shopid, String headerregex, String url,
			String tablescraper, java.sql.Timestamp now, Double pricefactorex, Double pricefactorin, String auto) {
		String urlregex = "";
		String shopurl = "";
		String winesep = "";
		String fieldsep = "";
		String filter = "";
		String nameorder = "";
		String nameregex = "";
		String nameexclpattern = "";
		String vintageorder = "";
		String vintageregex = "";
		String priceorder = "";
		String priceregex = "";
		String bottleorder = "";
		String bottleregex = "";
		boolean assumebottlesize = false;
		ArrayList<ArrayList<String>> ratingscraper = new ArrayList<ArrayList<String>>(0);
		Wine[] Wine = null;

		ResultSet rs;
		Connection con = Dbutil.openNewConnection();

		rs = Dbutil.selectQuery("Select * from " + auto + "tablescraper where id=" + tablescraper + ";", con);
		try {
			if (rs.next()) {
				urlregex = rs.getString("urlregex");
				winesep = rs.getString("winesep");
				fieldsep = rs.getString("fieldsep");
				filter = rs.getString("filter");
				nameorder = rs.getString("nameorder");
				nameregex = rs.getString("nameregex");
				nameexclpattern = rs.getString("nameexclpattern");
				vintageorder = rs.getString("vintageorder");
				vintageregex = rs.getString("vintageregex");
				priceorder = rs.getString("priceorder");
				priceregex = rs.getString("priceregex");
				bottleorder = rs.getString("sizeorder");
				bottleregex = rs.getString("sizeregex");
				assumebottlesize = rs.getBoolean("assumebottlesize");
			}
		} catch (Exception exc) {
			Dbutil.logger.error("No record found for " + auto + "tablescraper " + tablescraper, exc);
		}
		rs = Dbutil.selectQuery("Select * from " + auto + "shops where id=" + shopid + ";", con);
		try {
			if (rs.next()) {
				shopurl = rs.getString("shopurl");
				if (rs.getInt("shoptype") == 2)
					priceregex = "no price";
			}
		} catch (Exception exc) {
			Dbutil.logger.error("", exc);
		}
		ratingscraper = getRatingScraper(shopid);

		Dbutil.closeConnection(con);
		try {
			Wine = ScrapeWine(Page, shopid, url, urlregex, shopurl, baseurl, headerregex, tablescraper, winesep,
					fieldsep, filter, nameorder, nameregex, nameexclpattern, vintageorder, vintageregex, priceorder,
					priceregex, bottleorder, bottleregex, now, pricefactorex, pricefactorin, ratingscraper,
					assumebottlesize);
		} catch (Exception e) {
			Dbutil.logger.error("Problem tablescraping url " + url, e);
		}
		return Wine;

	}

	public static ArrayList<ArrayList<String>> getRatingScraper(int shopid) {
		ArrayList<ArrayList<String>> ratingscraper = new ArrayList<ArrayList<String>>(0);
		ArrayList<String> ratingscraperow;
		// Wine[] Wine;

		ResultSet rs;
		Connection con = Dbutil.openNewConnection();

		rs = Dbutil.selectQuery("Select * from ratingscraper where shopid=" + shopid + ";", con);
		try {
			while (rs.next()) {
				ratingscraperow = new ArrayList<String>(0);
				ratingscraperow.add(rs.getString("author"));
				ratingscraperow.add(rs.getString("regex"));
				ratingscraperow.add(rs.getString("regexlow"));
				ratingscraperow.add(rs.getString("regexhigh"));
				ratingscraper.add(ratingscraperow);
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Could not retrieve ratingscraper for shop " + shopid, exc);
		}
		Dbutil.closeConnection(con);
		return ratingscraper;
	}

	public static Wine[] ScrapeWine(String Page, int shopid, String url, String urlregex, String shopurl,
			String baseurl, String headerregex, String tablescraper, String winesep, String fieldsep, String filter,
			String nameorder, String nameregex, String nameexclpattern, String vintageorder, String vintageregex,
			String priceorder, String priceregex, String sizeorder, String sizeregex, java.sql.Timestamp now,
			Double pricefactorex, Double pricefactorin, ArrayList<ArrayList<String>> ratingscraper,
			boolean assumebottlesize) {
		Dbutil.logger.debug("TableScraping wines from page: " + url);
		ArrayList<Winerating> ratings = null;
		String name = null, vintage = null, wineurl = null;
		float price = 0;
		float size = 0;
		String Pageescaped = Page.replaceAll("\r?\n", "");
		String priceString = "";
		boolean rareold = Wine.isRareOld(url);
		ArrayList<Wine> WineAL = new ArrayList<Wine>();
		String timestampstring = "";

		if (now != null) {
			timestampstring = now.toString();
		}
		ArrayList<ArrayList<String>> wines = new ArrayList<ArrayList<String>>();

		wines = scrapetable(Page, winesep, fieldsep);
		Page = Page.replaceAll("\r?\n", "");

		for (int i = 0; i < wines.size(); i++) {
			// Check if regex="no price". If this is the case, we just need the wine for the
			// rating, a price is not present on the site. Use a dummy price instead.
			if (!priceregex.equals("no price")) {
				priceString = buildMatch(wines.get(i), priceorder, priceregex, "", "", "Price");
			} else {
				priceString = "684.00";
			}
			price = getPrice(priceString);
			try {
				if (price > 1.5) {
					name = getName(wines.get(i), nameorder, nameregex, nameexclpattern, headerregex, Pageescaped,
							false);
					if (name.length() > 2) {
						if (name.length() > 254)
							name = name.substring(0, 254);
						vintage = buildMatch(wines.get(i), vintageorder, vintageregex, headerregex, Page, "Vintage");
						if (vintage.equals(""))
							vintage = "0";
						size = getSize(wines.get(i), sizeorder, Page, price, assumebottlesize);
						wineurl = getUrl(urlregex, url, shopurl, baseurl, wines.get(i));
						ratings = getRating(wines.get(i), ratingscraper, Page, url);
						if (size == price) {
							size = 0; // Oops, we mistook the price for the size...
						}
						if (!priceregex.equals("no price") || ratings.size() > 0) {
							Wine foundwine = new Wine(name, vintage, size, price, price * pricefactorex,
									price * pricefactorin, 0.0, wineurl, shopid, "", "", timestampstring,
									timestampstring, null, rareold, "", 0, ratings);
							// Many pages use a relative url like "images/23435.jpg" which refers to
							// "/image" really.
							// This means that the wrong location is determined, because of the missing
							// leading "/".
							// Too bad... Don't get pictures otherwise a lot of 404 will appear.
							foundwine.pictureurl = getPictureUrl(url, shopurl, baseurl, wines.get(i), Page);
							WineAL.add(foundwine);
						}
					}
				}
			} catch (java.util.regex.PatternSyntaxException e) {
				throw e;
			} catch (Exception e) {
				Dbutil.logger.error("Problem tablescraping url " + url, e);
			}
		}

		Wine[] Wine = new Wine[WineAL.size()];

		for (int j = 0; j < WineAL.size(); j++) {
			Wine[j] = WineAL.get(j);
		}

		Dbutil.logger.debug("Finished scraping wines");

		return Wine;

	}

	public static float getPrice(String priceString) {
		float price = 0;

		try {
			if (priceString.length() > 2) {
				if (priceString.contains(",") || priceString.contains(".") || priceString.contains(" ")) {
					priceString = priceString.replaceAll("[^0-9., ]", "");
					if (priceString.substring((priceString.length() - 3), (priceString.length() - 2)).equals(" ")
							|| priceString.substring((priceString.length() - 3), (priceString.length() - 2)).equals(".")
							|| priceString.substring((priceString.length() - 3), (priceString.length() - 2))
									.equals(",")) {
						priceString = priceString.replaceAll("\\D", "");
						priceString = Spider.replaceString(priceString, ".", "");
						priceString = Spider.replaceString(priceString, ",", "");
						priceString = Spider.replaceString(priceString, "'", "");
						price = Float.valueOf(priceString).floatValue() / 100;
					} else {
						if (priceString.substring((priceString.length() - 2), (priceString.length() - 1)).equals(" ")
								|| priceString.substring((priceString.length() - 2), (priceString.length() - 1))
										.equals(".")
								|| priceString.substring((priceString.length() - 2), (priceString.length() - 1))
										.equals(",")) {
							priceString = priceString.replaceAll("\\D", "");
							priceString = Spider.replaceString(priceString, ".", "");
							priceString = Spider.replaceString(priceString, ",", "");
							price = Float.valueOf(priceString).floatValue() / 10;
						} else {
							if (priceString.substring((priceString.length() - 4), (priceString.length() - 3))
									.equals(" ")
									|| priceString.substring((priceString.length() - 4), (priceString.length() - 3))
											.equals(".")
									|| priceString.substring((priceString.length() - 4), (priceString.length() - 3))
											.equals(",")) {
								priceString = priceString.replaceAll("\\D", "");
								priceString = Spider.replaceString(priceString, ".", "");
								priceString = Spider.replaceString(priceString, ",", "");
								price = Float.valueOf(priceString).floatValue();
							}
						}
					}
				} else {
					if (priceString.replaceAll(" ", "").length() + 1 == priceString.length())
						priceString = priceString.replace(" ", ".").replaceAll("[^0-9.,]", "");
				}
			}
			if (price == 0) {
				price = Float.valueOf(priceString.replaceAll(",", ".")).floatValue();
			}
		} catch (Exception e) {
			// Dbutil.logger.error("Problem getting price "+url);
		}

		return price;

	}

	public static ArrayList<ArrayList<String>> scrapetable(String page, String winesep, String fieldsep) {
		ArrayList<ArrayList<String>> wines = new ArrayList<ArrayList<String>>();
		ArrayList<String> fields = new ArrayList<String>();
		Pattern winepattern;
		Matcher winematcher;
		Pattern fieldpattern, strikethroughpattern, strikethroughpattern2, valuepattern;
		Matcher fieldmatcher;
		// Pattern pattern;
		Matcher matcher;
		String valueregex = "";
		String winestring = "";
		String fieldstring = "";
		String valuestring = "";
		String strikethroughregex = "";
		String strikethroughregex2 = "";
		// String commentregex;
		int winestart = 0;
		int wineend = 0;
		int fieldstart = 0;
		int fieldend = 0;
		if (winesep.equals("\\t")) {
			winesep = "\t";
		}
		if (winesep.equals("\\n")) {
			winesep = "\r?\n";
		} else {
			page = page.replaceAll("\r?\n", "");
		}
		if (fieldsep.equals("|"))
			fieldsep = "\\|";
		// valueregex="(?:>|^)\\s*([^<>\\s][^<>]*?)\\s*(?:<|$)"; //Does not allow for
		// unescaped < and >
		// valueregex="(?:[^>]*>)?([^<]*[^>]*?)(?:<?[^<>]*)?"; // Does not pick up
		// values like 12345</td>
		// valueregex="(?:<[^>]*>)?([^<]*[^>]*?)(?:<?[^<>]*>?)?"; // WRONG, leaves all >
		// in value
		// valueregex="(?:[^>]*>)?([^<]*[^>]*?)(?:<?)?";// Wrong, if line ends with
		// <span then span is used
		// valueregex="(?:[^>]*>)?([^<]*[^>]*?)(?:<[^>]+)?"; Wrong 25-03-2012 skips
		// value<tag>
		valueregex = "(?:[^<>]*>)?([^<]*[^>]*?)(?:<[^>]+)?";

		strikethroughregex = "(<s>.*?</s>)";
		strikethroughregex2 = "(<strike>.*?</strike>)";

		// commentregex="(<!--.*?-->)";
		// pattern = Pattern.compile(strikethroughregex); // Filter out anything that is
		// commented out
		// matcher = pattern.matcher(page);
		// while (matcher.find()) {
		// page=page.replaceAll(matcher.group(1),"");
		// }

		winepattern = Pattern.compile(winesep, Pattern.CASE_INSENSITIVE);
		winematcher = winepattern.matcher(page);
		if (winematcher.find()) {
			winestart = 0;
			wineend = winematcher.start();
			fieldpattern = Pattern.compile(fieldsep, Pattern.CASE_INSENSITIVE);
			strikethroughpattern = Pattern.compile(strikethroughregex); // Filter out anything that is strikethrough
			strikethroughpattern2 = Pattern.compile(strikethroughregex2); // Filter out anything that is strikethrough
			valuepattern = Pattern.compile(valueregex);

			while (wineend < page.length()) {
				winestart = wineend;
				if (winematcher.find()) {
					wineend = winematcher.start();
				} else {
					wineend = page.length();
				}
				winestring = page.substring(winestart, wineend);

				fieldstart = 0;
				fieldend = 0;
				fields = new ArrayList<String>();
				fields.add(String.valueOf(winestart)); // index of this wine is used to get the header (last headerregex
														// before this wine)
				fields.add(winestring); // We need the original line to get the URL, as this is between the < > brackets

				fieldmatcher = fieldpattern.matcher(winestring);

				while (fieldend < winestring.length()) {
					fieldstart = fieldend;
					if (fieldmatcher.find()) {
						fieldend = fieldmatcher.start();
					} else {
						fieldend = winestring.length();
					}
					fieldstring = winestring.substring(fieldstart, fieldend);

					matcher = strikethroughpattern.matcher(fieldstring);
					while (matcher.find()) {
						fieldstring = fieldstring.replaceAll(matcher.group(1), "");
					}
					matcher = strikethroughpattern2.matcher(fieldstring);
					while (matcher.find()) {
						fieldstring = fieldstring.replaceAll(matcher.group(1), "");
					}
					fieldstring = Spider.replaceString(fieldstring, "|", "");
					fieldstring = Spider.replaceString(fieldstring, "\r?\n", "");
					fieldstring = Spider.replaceString(fieldstring, "\t", "");

					matcher = valuepattern.matcher(fieldstring);
					valuestring = "";
					while (matcher.find()) {
						if (!"".equals(matcher.group(1).trim())) {
							valuestring = valuestring + " " + matcher.group(1).trim();
						}
					}
					valuestring = valuestring.replaceAll("\\s+", " ");
					valuestring = valuestring.replaceAll("^\\s", "");
					valuestring = valuestring.replaceAll("\\s$", "");
					fields.add(valuestring);
					// System.out.println("Valuestring: "+valuestring);

				}
				wines.add(fields);
			}
		}
		return wines;
	}

	public static String getName(ArrayList<String> values, String nameorder, String nameregex, String nameexclpattern,
			String headerregex, String Page, boolean verbose) {
		String resultString = "";
		String header = "";
		String filteredheader = "";
		int start = Integer.valueOf(values.get(0));
		Pattern pattern;
		Matcher matcher;
		String valueregex = "(?:[^>]*>)?([^<]*[^>]*?)(?:<[^>]+)?";
		nameexclpattern = "^\\d+$:" + nameexclpattern; // Filter column with only digits as it is probably a product
														// code
		String[] elements = nameorder.split(";");

		if (!headerregex.equals("")) {
			// Now find the last header regex before the match
			pattern = Pattern.compile(headerregex);
			matcher = pattern.matcher(Page);
			while (matcher.find()) {
				if (matcher.start(1) < start) {
					header = matcher.group(1);
				} else {
					break;
				}
			}
			if (!"".equals(header)) {

				pattern = Pattern.compile(valueregex);
				matcher = pattern.matcher(header);
				while (matcher.find()) {
					if (!"".equals(matcher.group(1).trim())) {
						filteredheader += " " + matcher.group(1).trim();
					}
				}
				filteredheader = filteredheader.replaceAll("\\s+", " ");
				filteredheader = filteredheader.trim();
				header = filteredheader;

			}
		}

		for (int i = 0; i < elements.length; i++) {
			if (Spider.isInteger(elements[i])) {
				if (values.size() > (Integer.parseInt(elements[i]) + 1))
					if (verbose) {
						resultString = resultString + "<TD>(" + Integer.parseInt(elements[i]) + ")"
								+ filterValue(values.get(Integer.parseInt(elements[i]) + 1), nameexclpattern) + "</TD>";

					} else {
						resultString += values.get(Integer.parseInt(elements[i]) + 1) + " ";
					}
			} else {
				if (!elements[i].equals("H")) {
					resultString = resultString + elements[i];
				} else {
					resultString = resultString + header;
				}
			}

			// System.out.print("BuildString: "+buildString+", i: "+i+", resultaat:
			// "+resultString);
		}
		resultString = filterWineName(resultString, nameexclpattern, values);

		return resultString;
	}

	public static String getPictureUrl(String parenturl, String shopurl, String Baseurl, ArrayList<String> values,
			String Page) {
		String url = "";
		// String urlregex="img[^>]*(?:'|\")([^'\"]+\\.(gif|jpg|png))(?:'|\")";
		String urlregex = "(<a [^>]*href=['\"]([^'\"]+(gif|jpg|png))['\"][^>]*>\\s*)?<img[^>]*(?:'|\")([^'\"]+\\.(gif|jpg|png))(?:'|\")";
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(urlregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		matcher = pattern.matcher(values.get(1));
		if (url.equals("") && matcher.find()) {
			url = matcher.group(2);
			if (url != null && !(Page.length() == (Page.replace(url, "").length() + url.length())))
				url = "";
			if (url == null || url.equals("")) {
				url = matcher.group(4);
				if (url == null || !(Page.length() == (Page.replace(url, "").length() + url.length())))
					url = "";
			}
			if (Baseurl.equals("File")) {
				if (url.startsWith("http://")) {
					return cleanUrl(url);
				} else {
					url = "";
				}
			} else {
				if (!"".equals(url))
					url = Spider.padUrl(url, parenturl, Baseurl, shopurl);
			}
		}
		return cleanUrl(url);

	}

	public static String filterWineName(String resultString, String nameexclpattern, ArrayList<String> values) {
		resultString = filterValue(resultString, nameexclpattern);
		resultString = Spider.unescape(resultString);
		resultString = resultString.replaceAll("\\s+", " ");
		resultString = resultString.trim();
		if (values != null)
			if (Webroutines.getRegexPatternValue(
					">[^<]*?(sold(?!e)|ausverkauft|uitverkocht|no disponible|out of stock|non in vendita)[^>]*?<",
					values.get(1), Pattern.CASE_INSENSITIVE).length() > 0)
				resultString = "";
		resultString = resultString.replaceAll(".*(N|n)ot available.*", "");

		return resultString;
	}

	public static ArrayList<Winerating> getRating(ArrayList<String> values, ArrayList<ArrayList<String>> ratingscraper,
			String Page, String url) {

		// ratingscraper: ArrayList van author(0), regex(1), regexlow(2), regexhigh(3)
		ArrayList<Winerating> ratings = new ArrayList<Winerating>();
		String record = "";
		String rating = "";
		double ratinglow = 0;
		double ratinghigh = 0;
		String issuedate = "";
		int issue = 0;
		// String header = "";
		if (ratingscraper != null) {
			for (String valuepart : values) {
				record += valuepart + " ";
			}
			// Pattern pattern;
			// Matcher matcher;
			for (int i = 0; i < ratingscraper.size(); i++) {
				rating = buildMatch(values, "", ratingscraper.get(i).get(1), "", "", "Rating");
				if (!rating.equals("")) {
					ratinglow = convertRating(matchthis(rating, ratingscraper.get(i).get(2), 1),
							ratingscraper.get(i).get(0));
					ratinghigh = convertRating(matchthis(rating, ratingscraper.get(i).get(3), 1),
							ratingscraper.get(i).get(0));
					issuedate = Spider.determineIssueDate(url, record);
					issue = Spider.determineIssue(url, record, Page);
					if (ratinglow > 0 && ratinglow <= 100 && ratinghigh <= 100) {
						Winerating wr = new Winerating(ratingscraper.get(i).get(0), ratinglow, ratinghigh);
						wr.issuedate = issuedate;
						wr.issue = issue + "";
						ratings.add(wr);
					}
				}
			}
		}
		return ratings;
	}

	public static double convertRating(String rating, String author) {
		Double result = 0.0;
		rating = Spider.replaceString(rating, ",", ".");

		try {
			if (author.equals("RP") || author.equals("WS") || author.equals("B&D") || author.equals("ST")
					|| author.equals("Dec")) {
				if (!rating.equals("")) {
					result = Double.valueOf(rating);
				}
			}
			return result;
		} catch (Exception exc) {
			Dbutil.logger.error("could not parse rating " + rating, exc);
		}
		return result;

	}

	public static String buildMatch(ArrayList<String> values, String order, String regex, String headerregex,
			String Page, String field) {
		String resultString = "";
		String value;
		// String priceString;
		String header = "";
		String[] elements = new String[0];
		int start = Integer.valueOf(values.get(0));
		Pattern pattern;
		Matcher matcher;
		String fieldvalue;

		if (!headerregex.equals("")) {
			// Now find the last header regex before the match
			pattern = Pattern.compile(headerregex);
			matcher = pattern.matcher(Page);
			while (matcher.find()) {
				if (matcher.start(1) < start) {
					header = matcher.group(1);
				} else {
					break;
				}
			}
		}
		if (order.length() > 0) {
			elements = order.split(";");
		} else {
			elements = new String[values.size()];
			for (int i = 1; i < values.size(); i++) {
				elements[i] = String.valueOf(i);
			}
		}
		for (int i = 0; i < elements.length; i++) {
			if (resultString.equals("")) { // With vintage and price, take the first hit
				if (elements[i] != null) {
					if (Spider.isInteger(elements[i]) || "H".equals(elements[i])) {
						fieldvalue = "";
						if (Spider.isInteger(elements[i])) {
							if (values.size() > (Integer.parseInt(elements[i]) + 1)) {
								fieldvalue = values.get(Integer.parseInt(elements[i]) + 1);
								if (field.equals("Price"))
									fieldvalue = Spider.unescape(fieldvalue);
							}
						} else if ("H".equals(elements[i])) {
							fieldvalue = header;
						}
						if (!"".equals(fieldvalue)) {
							fieldvalue = fieldvalue.replace((char) 160, (char) 32).replace((char) 160, (char) 32)
									.replace((char) 160, (char) 32);
							pattern = Pattern.compile(regex, Pattern.DOTALL);
							matcher = pattern.matcher(fieldvalue);
							if (matcher.find()) {
								value = matcher.group(1);
								if (field.equals("Price") && matcher.groupCount() > 1 && matcher.group(2) != null
										&& !value.contains(matcher.group(2))) {
									if (matcher.group(2).startsWith(",") || matcher.group(2).startsWith(".")) {
										value += matcher.group(2);
									} else {
										value += "," + matcher.group(2);
									}
								}
								try {

									if (field.equals("Price") && getPrice(value) < 1.6)
										value = "";
									if (field.equals("Vintage") && value.equals("1855"))
										value = "";
									if (field.equals("Vintage") && value.equals("75"))
										value = "";
									if (field.equals("Vintage") && value.length() == 2) {
										// Y2K problem
										if (Integer.parseInt(value) > 70) {
											value = "19" + value;
										} else if (Integer.parseInt(value) < 20) {
											value = "20" + value;
										} else {
											value = ""; // Probably not a year
										}
									}
									if (field.equals("Vintage") && value.length() == 4) {
										// No wines from the future
										if (Integer.parseInt(value) > Calendar.getInstance().get(Calendar.YEAR)) {
											value = "";
										}
									}

									resultString = resultString + value;
								} catch (Exception e) {
								}
							}

						}
					} else {
						if (elements[i].startsWith("\\")) {
							// A \ means treat the next characters as a literal, not a number
							resultString = resultString + elements[i].substring(1);
						} else {
							resultString = resultString + elements[i];
						}

					}
				}
			}
			// System.out.print("BuildString: "+buildString+", i: "+i+", resultaat:
			// "+resultString);
		}

		return resultString;

	}

	public static float getSize(ArrayList<String> values, String order, String Page, float price,
			boolean assumebottlesize) {
		float size = 0;
		int matchingregex = 0;
		float casesize = 0;
		String[] elements = new String[0];
		String value;
		Pattern pattern;
		Matcher matcher;

		if (order.length() > 0) {
			elements = order.split(";");
		} else {
			elements = new String[values.size()];
			for (int i = 1; i < values.size(); i++) {
				elements[i] = String.valueOf(i);
			}
		}
		for (int i = 0; i < elements.length; i++) {
			if (size == 0) { // With size, take the first hit
				if (elements[i] != null) {
					if (Spider.isInteger(elements[i])) {
						if (values.size() > (Integer.parseInt(elements[i]) + 1)) {
							value = Spider.unescape(values.get(Integer.parseInt(elements[i]) + 1));

							// Now loop over the different bottle regular expressions

							for (int j = 0; j < Configuration.sizeregex.length; j++) {
								if (size == 0) { // With size, take the first hit
									pattern = Pattern.compile(Configuration.sizeregex[j],
											Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
									matcher = pattern.matcher(value);

									if (matcher.find()) {

										if (!(Configuration.size[j] == price)) {
											matchingregex = j;
											size = Configuration.size[j];
										}
									}

								}

							}
						}
					}
				}
			}
		}

		// Check for case
		for (int i = 0; i < elements.length; i++) {
			if (casesize == 0) { // With size, take the first hit
				if (elements[i] != null) {
					if (Spider.isInteger(elements[i])) {
						if (values.size() > (Integer.parseInt(elements[i]) + 1)) {
							value = Spider.unescape(values.get(Integer.parseInt(elements[i]) + 1));
							// Try the size regex with a prefix
							if (matchingregex > 0 && Configuration.sizeregex[matchingregex].contains("^(?:.*?\\D)?")) {
								String[] caseregex = new String[3];
								caseregex[0] = Configuration.sizeregex[matchingregex].replace("^(?:.*?\\D)?",
										"^(?:.*?\\D)?12 ?[/x] ?");
								caseregex[1] = Configuration.sizeregex[matchingregex].replace("^(?:.*?\\D)?",
										"^(?:.*?\\D)?6 ?[/x] ?");
								caseregex[2] = Configuration.sizeregex[matchingregex].replace("^(?:.*?\\D)?",
										"^(?:.*?\\D)?24 ?[/x] ?");
								float[] casesizearray = new float[3];
								casesizearray[0] = 1200f;
								casesizearray[1] = 600f;
								casesizearray[2] = 2400f;
								for (int j = 0; j < caseregex.length; j++) {
									if (casesize == 0) { // With casesize, take the first hit
										pattern = Pattern.compile(caseregex[j],
												Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
										matcher = pattern.matcher(value);
										if (matcher.find()) {
											casesize = casesizearray[j];

										}

									}

								}

							}
							// Now loop over the different case regular expressions
							for (int j = 0; j < Configuration.caseregex.length; j++) {
								if (casesize == 0) { // With size, take the first hit
									pattern = Pattern.compile(Configuration.caseregex[j],
											Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
									matcher = pattern.matcher(value);
									if (matcher.find()) {
										casesize = Configuration.casesize[j];

									}

								}

							}
						}
					}
				}
			}
		}
		if (assumebottlesize && size < 0.01)
			size = 0.75f;
		size = size + casesize;

		return size;

	}

	public static String getUrl(String urlregex, String parenturl, String shopurl, String Baseurl,
			ArrayList<String> values) {
		String url = "";
		if (urlregex == null || urlregex.equals("")) { // just get the parent url
			return parenturl;
		} else if (urlregex.equalsIgnoreCase("Shop")) { // Refer to the general shop Url
			return shopurl;
		} // Get the URL from the individual wine
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(urlregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		matcher = pattern.matcher(values.get(1));
		while (url.equals("") && matcher.find()) {
			url = matcher.group(1);
			if (Baseurl.equals("File")) {
				if (url.startsWith("http://")) {
					return cleanUrl(url);
				} else {
					Dbutil.logger.warn("Url from file is not a complete url: " + url);
					return shopurl;
				}
			} else {
				url = Spider.padUrl(url, parenturl, Baseurl, shopurl);
			}
			url = cleanUrl(url);
		}
		if (url.equals("")) {
			url = shopurl;
		}
		return url;

	}

	public static String cleanUrl(String Urlfound) {
		Urlfound = Urlfound.replaceAll("&amp;", "&");
		Urlfound = Urlfound.replaceAll("PHPSESSID=[0123456789abcdefABCDEF]*", "");
		Urlfound = Urlfound.replaceAll("&&", "&");
		Urlfound = Urlfound.replaceAll("&&", "&");
		Urlfound = Urlfound.replaceAll("&&", "&");
		Urlfound = Urlfound.replaceAll(".*[Mm]ailto.*", "");
		Urlfound = Urlfound.replaceAll(".*[Jj]avascript.*", "");
		Urlfound = Urlfound.replaceAll("file://.*", "");
		return Urlfound;
	}

	/*
	 * public static String getVintage(ArrayList<String> values,String
	 * vintageorder,String vintageregex,String headerregex,String Page){ String
	 * resultString=""; String vintage=""; String header=""; String[] elements=new
	 * String[0]; int start=Integer.valueOf(values.get(0)); Pattern pattern; Matcher
	 * matcher;
	 * 
	 * if (!headerregex.equals("")){ // Now find the last header regex before the
	 * match pattern = Pattern.compile(headerregex); matcher =
	 * pattern.matcher(Page); while (matcher.find()){ if (matcher.start(1)<start){
	 * header=matcher.group(1); } else { break; } } } if (vintageorder.length()>0){
	 * elements = vintageorder.split(";"); } else { elements = new
	 * String[values.size()]; for (int i=1;i<values.size();i++){
	 * elements[i]=String.valueOf(i); } } for (int i=0;i<elements.length;i++) { if
	 * (Spider.isInteger(elements[i])){ pattern = Pattern.compile(vintageregex);
	 * matcher = pattern.matcher(values.get(Integer.parseInt(elements[i]))); if
	 * (matcher.find()){ vintage=vintage+matcher.group(1); } } else { if
	 * (!elements[i].equals("H")){ vintage = vintage+ elements[i]; } else { vintage
	 * = vintage+ header; } }
	 * //System.out.print("BuildString: "+buildString+", i: "+i+", resultaat: "
	 * +resultString); }
	 * 
	 * 
	 * return vintage;
	 * 
	 * }
	 * 
	 * public static String getPrice(ArrayList<String> values,String
	 * priceorder,String priceregex){ String resultString=""; String priceString="";
	 * float price=0; String header=""; String[] elements=new String[0]; int
	 * start=Integer.valueOf(values.get(0)); Pattern pattern; Matcher matcher;
	 * 
	 * 
	 * if (priceorder.length()>0){ elements = priceorder.split(";"); } else {
	 * elements = new String[values.size()]; for (int i=1;i<values.size();i++){
	 * elements[i]=String.valueOf(i); } } for (int i=0;i<elements.length;i++) { if
	 * (Spider.isInteger(elements[i])){ if
	 * (values.size()>Integer.parseInt(elements[i]))
	 * resultString=resultString+values.get(Integer.parseInt(elements[i])); } else {
	 * if (!elements[i].equals("H")){ resultString = resultString+ elements[i]; }
	 * else { resultString = resultString+ header; } }
	 * 
	 * }
	 * 
	 * 
	 * pattern = Pattern.compile(priceregex); matcher =
	 * pattern.matcher(resultString); if (matcher.find()){
	 * priceString=matcher.group(1); }
	 * 
	 * return priceString;
	 * 
	 * }
	 * 
	 */
	public static final boolean addTableScrapeRow(String Shopid, String Rowid, String Type, String Url,
			String Headerregex, String winesep, String fieldsep, String filter, String nameorder, String nameregex,
			String nameexclpattern, String vintageorder, String vintageregex, String priceorder, String priceregex,
			String sizeorder, String sizeregex, String urlregex, String postdata, String auto, boolean assumebottlesize)
			throws Exception {
		boolean succes = false;
		int i;
		int tablescraperow = 0;
		String encoding = null;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {

			Dbutil.executeQuery("Update " + auto + "shops set URLtype = '" + Type + "' where id=" + Shopid + ";");

			// Get encoding (to do: in separate routine)
			rs = Dbutil.selectQuery("SELECT Encoding from " + auto + "shops where id='" + Shopid + "';", con);
			if (!rs.next() || rs.getString("Encoding").equals("")) {
				encoding = Spider.getHtmlEncoding(Url);
				i = Dbutil.executeQuery(
						"Update " + auto + "shops set encoding = '" + encoding + "' where id=" + Shopid + ";");
			}
			Dbutil.closeRs(rs);

			int hashcode = (Url + postdata).hashCode();
			if (Type.equals("Email")) {
				hashcode = (Shopid + "").hashCode();
			}
			if (Rowid.equals("0")) {
				i = Dbutil.executeQuery("Insert into " + auto
						+ "tablescraper (shopid,winesep,fieldsep,filter,nameorder,nameregex,nameexclpattern,vintageorder,vintageregex,priceorder,priceregex,sizeorder,sizeregex, urlregex,assumebottlesize"
						+ (!"".equals(auto) ? ",allcolumns" : "") + ")" + "values ('" + Shopid + "', '" + winesep
						+ "', '" + fieldsep + "', '" + filter + "', '" + nameorder + "', '" + nameregex + "', '"
						+ nameexclpattern + "', '" + vintageorder + "', '" + vintageregex + "', '" + priceorder + "', '"
						+ priceregex + "', '" + sizeorder + "', '" + sizeregex + "', '" + urlregex + "',"
						+ assumebottlesize + (!"".equals(auto) ? ",''" : "") + ");", con);
				if (i > 0) {
					rs = Dbutil.selectQuery("SELECT LAST_INSERT_ID();", con);
					rs.next();
					tablescraperow = rs.getInt(1);
					Dbutil.closeRs(rs);

					for (String u : Url.split("@@@")) {
						Dbutil.executeQuery("delete from " + auto + "scrapelist where shopid=" + Shopid + " and url='"
								+ Spider.SQLEscape(u) + "';");
						hashcode = (u + postdata).hashCode();
						i = Dbutil.executeQuery("Insert into " + auto
								+ "scrapelist (Url, Postdata, Headerregex, regex, scrapeorder,parenturl,Shopid, URLType, tablescraper, Status,hashcode) "
								+ "values ('" + Spider.SQLEscape(u) + "', '" + postdata + "', '" + Headerregex
								+ "', '','','', '" + Shopid + "', '" + Type + "', '" + tablescraperow + "', 'Ready',"
								+ hashcode + ");");
						if (i != 0) {
							succes = true;
						}
					}
				}
			} else {
				Dbutil.closeRs(rs);

				rs = Dbutil.selectQuery("Select tablescraper from " + auto + "scrapelist where id = " + Rowid + ";",
						con);
				rs.next();
				tablescraperow = rs.getInt("tablescraper");
				Dbutil.closeRs(rs);

				int j = Dbutil.executeQuery("Update " + auto + "tablescraper set winesep='" + winesep + "', fieldsep='"
						+ fieldsep + "', filter='" + filter + "', nameorder='" + nameorder + "', nameregex='"
						+ nameregex + "', nameexclpattern='" + nameexclpattern + "', vintageorder='" + vintageorder
						+ "', vintageregex='" + vintageregex + "', priceorder='" + priceorder + "', priceregex='"
						+ priceregex + "', sizeorder='" + sizeorder + "', sizeregex='" + sizeregex + "', urlregex='"
						+ urlregex + "', assumebottlesize=" + assumebottlesize + " where id=" + tablescraperow + ";");
				Dbutil.executeQuery(
						"delete from " + auto + "scrapelist where shopid=" + Shopid + " and id=" + Rowid + ";");
				for (String u : Url.split("@@@")) {
					hashcode = (u + postdata).hashCode();
					Dbutil.executeQuery("delete from " + auto + "scrapelist where url='" + Spider.SQLEscape(u)
							+ "' and postdata='" + Spider.SQLEscape(postdata) + "';");
					i = Dbutil.executeQuery("Insert into " + auto
							+ "scrapelist (Url, Postdata, Headerregex, regex, scrapeorder,parenturl,Shopid, URLType, tablescraper, Status,hashcode) "
							+ "values ('" + Spider.SQLEscape(u) + "', '" + Spider.SQLEscape(postdata) + "', '"
							+ Headerregex + "', '','','', '" + Shopid + "', '" + Type + "', '" + tablescraperow
							+ "', 'Ready'," + hashcode + ");");
					if (j != 0 && i != 0) {
						succes = true;
					}
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not save record in Scrapelist ", e);
			throw e;
		}
		Dbutil.closeConnection(con);
		if (succes) {
			Dbutil.executeQuery("update shopstats set lastchange=sysdate() where shopid=" + Shopid + ";");
		}

		return succes;
	}

	public static final boolean addAutoTableScrapeRow(String Shopid, String winesep, String fieldsep, String filter,
			String nameorder, String nameregex, String nameexclpattern, String vintageorder, String vintageregex,
			String priceorder, String priceregex, String sizeorder, String sizeregex, String urlregex, String postdata,
			int winesfound) {
		boolean succes = false;
		// int i;
		// String encoding = null;
		// String completeUrl;
		// ResultSet rs;
		Connection con = Dbutil.openNewConnection();
		try {
			// i =
			Dbutil.executeQuery(
					"Insert into autotablescraper (shopid,winesep,fieldsep,filter,nameorder,nameregex,nameexclpattern,vintageorder,vintageregex,priceorder,priceregex,sizeorder,sizeregex, urlregex,allcolumns,winesfound) "
							+ "values ('" + Shopid + "', '" + winesep + "', '" + fieldsep + "', '" + filter + "', '"
							+ nameorder + "', '" + nameregex + "', '" + nameexclpattern + "', '" + vintageorder + "', '"
							+ vintageregex + "', '" + priceorder + "', '" + priceregex + "', '" + sizeorder + "', '"
							+ sizeregex + "', '" + urlregex + "','" + winesep + fieldsep + nameorder + vintageorder
							+ priceorder + sizeorder + "'," + winesfound + ");",
					con);

		} catch (Exception e) {
			Dbutil.logger.error("Could not save record in autoScrapelist ", e);
		}
		Dbutil.closeConnection(con);
		return succes;
	}

	public static boolean addUpdateCheckShop(String shopid, String url, String regex, String postdata,
			String multiplier) {
		boolean succes = true;
		int i;
		Connection con = Dbutil.openNewConnection();
		ResultSet rs;
		if (url != null && !url.equals("") && regex != null && !regex.equals("")) {
			succes = false;
			rs = Dbutil.selectQuery("select * from checkshop where shopid=" + shopid, con);
			try {
				if (rs.next()) {
					i = Dbutil.executeQuery("Update checkshop set url='" + url + "', regex='" + regex + "', postdata='"
							+ postdata + "',multiplier=" + multiplier + " where shopid=" + shopid + ";");
					if (i > 0)
						succes = true;
				} else {
					i = Dbutil.executeQuery("Insert into checkshop (shopid,url,regex,postdata,multiplier) values ("
							+ shopid + ",'" + url + "','" + regex + "','" + postdata + "'," + multiplier + ");");
					if (i > 0)
						succes = true;
				}
			} catch (Exception e) {
				Dbutil.logger.error("Could not save checkshop record. " + e);
			}
		}
		Dbutil.closeConnection(con);
		return succes;
	}

	public static String filterValue(String value, String filter) {
		// String before;
		// String after;
		if (!filter.equals("")) {
			String[] regex = filter.split(":");
			// Pattern pattern;
			// Matcher matcher;
			for (int i = 0; i < regex.length; i++) {
				/*
				 * pattern = Pattern.compile(regex[i]); matcher = pattern.matcher(value); if
				 * (matcher.find()){ before=""; after=""; if (value.length()>matcher.end()){
				 * after=value.substring(matcher.end()); } if (matcher.start()>0) {
				 * before=value.substring(0,matcher.start()); } value=before+after;
				 * 
				 * 
				 * }
				 */
				value = value.replaceAll(regex[i], "");
			}
		}
		return value;
	}

	private static ArrayList<String> getClassTags(String page) {
		ArrayList<String> tags = new ArrayList<String>(0);
		String regex = "(class *= *(?:'|\")[^'\"]*(?:'|\"))";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(page);
		boolean found;
		while (matcher.find()) {
			found = false;
			for (String tag : tags) {
				if (matcher.group(1).equalsIgnoreCase(tag)) {
					found = true;
					break;
				}
			}
			if (!found)
				tags.add(matcher.group(1));
		}
		return tags;
	}

	public static String matchthis(String input, String regex, int group) {
		String result = "";
		try {
			if (input != null && regex != null && !input.equals("") && !regex.equals("")) {
				Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher matcher = pattern.matcher(input);

				if (matcher.find()) {
					result = matcher.group(group);
				}
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Error while finding match in matchthis: ", exc);
		}
		return result;
	}

}
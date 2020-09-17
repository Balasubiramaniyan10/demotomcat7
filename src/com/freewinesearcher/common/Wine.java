package com.freewinesearcher.common;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.TableScraper;
import com.searchasaservice.parser.xpathparser.Record;

public class Wine implements Serializable {

	private static final long serialVersionUID = 1L;
	public String Name;
	public String Vintage;
	public float Size;
	public float Price;
	public Double PriceEuroEx;
	public Double PriceEuroIn;
	public Double CPC;
	public String SourceUrl;
	public int ShopId;
	public String LastUpdated;
	public Date createdate;;
	public Date lastupdate;
	public String CreateDate;
	public int Id;
	public String Shopname;
	public String Shopurl;
	public String Country;
	public boolean Rareold;
	public int Knownwineid;
	public String Region;
	public ArrayList<Winerating> Ratings;
	public boolean recommended = false;
	public String pictureurl = "";
	public double pqratio = 0;
	public double relativeprice = 0;
	public int pricestars = 0;
	public int pqstars = 0;

	public Wine(String name, String vintage, float size, float price, Double priceeuroex, Double priceeuroin,
			Double cpc, String sourceURL, int shopId, String shopName, String shopUrl, String createDate,
			String lastUpdated, String id, boolean rareold, String country, int knownwineid,
			ArrayList<Winerating> ratings) {
		if (name.length() > 230)
			name = name.substring(0, 230);
		Name = name;
		Vintage = vintage;
		Price = price;
		PriceEuroEx = priceeuroex;
		PriceEuroIn = priceeuroin;
		CPC = cpc;
		Size = size;
		SourceUrl = sourceURL;
		ShopId = shopId;
		Shopname = shopName;
		Shopurl = shopUrl;
		LastUpdated = lastUpdated;
		CreateDate = createDate;
		try {
			Id = Integer.parseInt(id);
		} catch (Exception e) {
		}
		Rareold = rareold;
		Country = country;
		Ratings = ratings;
		Knownwineid = knownwineid;
	}

	public Wine(String id) throws Exception {

		Connection winecon = Dbutil.openNewConnection();
		ResultSet rs = null;
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from wineview where id='" + id + "'", winecon);
			rs.next();
			Id = rs.getInt("id");
			Name = rs.getString("name");
			Vintage = rs.getString("vintage");
			Price = rs.getFloat("price");
			PriceEuroIn = rs.getDouble("priceeuroin");
			PriceEuroEx = rs.getDouble("priceeuroex");
			Size = rs.getFloat("size");
			Country = rs.getString("country");
			CPC = rs.getDouble("CPC");
			SourceUrl = rs.getString("sourceurl");
			if (SourceUrl == null || SourceUrl.equals(""))
				SourceUrl = rs.getString("Shopurl");
			if (rs.getString("Shopurl").contains("malware.jsp"))
				SourceUrl = "https://www.vinopedia.com/malware.jsp?exturl=" + SourceUrl.replace(":", "%3A");
			Shopname = rs.getString("Shopname");
			ShopId = rs.getInt("Shopid");
			Shopurl = rs.getString("Shopurl");
			Knownwineid = rs.getInt("Knownwineid");
			createdate = rs.getDate("createdate");
			lastupdate = rs.getDate("lastupdates");
			Rareold = Boolean.parseBoolean(rs.getString("Rareold"));
			Ratings = getRatings(winecon);
		} catch (Exception e) {
			throw (new Exception("Could not find wine with id=" + id));
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);

		}
	}

	public Wine(int id) throws Exception {
		ResultSet rs = null;
		Connection winecon = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT * " + "from wineview where id='" + id + "'", winecon);
			rs.next();
			Id = rs.getInt("id");
			Name = rs.getString("name");
			Vintage = rs.getString("vintage");
			Price = rs.getFloat("price");
			PriceEuroIn = rs.getDouble("priceeuroin");
			PriceEuroEx = rs.getDouble("priceeuroex");
			Size = rs.getFloat("size");
			Country = rs.getString("country");
			CPC = rs.getDouble("CPC");
			SourceUrl = rs.getString("sourceurl");
			if (SourceUrl == null || SourceUrl.equals(""))
				SourceUrl = rs.getString("Shopurl");
			if (rs.getString("Shopurl").contains("malware.jsp"))
				SourceUrl = "https://www.vinopedia.com/malware.jsp?exturl=" + SourceUrl.replace(":", "%3A");
			Shopname = rs.getString("Shopname");
			ShopId = rs.getInt("Shopid");
			Shopurl = rs.getString("Shopurl");
			Knownwineid = rs.getInt("Knownwineid");
			Rareold = Boolean.parseBoolean(rs.getString("Rareold"));
			Ratings = getRatings(winecon);
		} catch (Exception e) {
			throw (new Exception("Wine with id " + id + " not found"));
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(winecon);
		}
	}

	public static void addorupdatewine(Wine[] Wine, String auto) {
		Connection winecon = Dbutil.openNewConnection();
		try {
			addorupdatewine(Wine, auto, winecon);
		} catch (Exception e) {
		} finally {
			Dbutil.closeConnection(winecon);
		}
	}

	public static void addorupdatewine(Wine[] Wine, String auto, Connection winecon) {
		int i = 0;
		int rowsaffected;
		String name;
		String query;
		NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "US"));
		DecimalFormat priceFormat = (DecimalFormat) nf;
		priceFormat.applyPattern("0.00");
		ResultSet rs = null;
		try {
			winecon.setAutoCommit(false);

			for (i = 0; i < Wine.length; i++) {
				if (Wine[i].Price < 1000000 && Wine[i].Name != null && Wine[i].Name.length() > 3) {
					String formattedPrice = priceFormat.format(Wine[i].Price);
					name = Spider.SQLEscape(Spider.unescape(Wine[i].Name));
					Wine[i].SourceUrl = Spider.SQLEscape(Wine[i].SourceUrl);
					if (Wine[i].Vintage.equals(""))
						Wine[i].Vintage = "0";
					if (Wine[i].Vintage.length() <= 4 && name.length() <= 500) { // Otherwise we'll get a database error
						// Check if the wine already exists in the database with an updated date < now
						// and for the rest the same and update it rightaway
						query = "Update LOW_PRIORITY " + auto + "wines set PriceEuroEx=" + Wine[i].PriceEuroEx
								+ ", PriceEuroIn=" + Wine[i].PriceEuroIn + ", SourceURL='" + Wine[i].SourceUrl
								+ "', LastUpdated='" + Wine[i].LastUpdated + "', Rareold=" + Wine[i].Rareold
								+ "  where Name='" + name + "' and Vintage = '" + Wine[i].Vintage + "' and Size="
								+ Wine[i].Size + " and Price=" + formattedPrice + " and lastupdated<'"
								+ Wine[i].LastUpdated + "' and Shopid = '" + Wine[i].ShopId + "';";
						rowsaffected = Dbutil.executeQuery(query, winecon);
						if (rowsaffected != 1) { // So it was either new, the price changed or it was updated already.
													// Let's find out...
							rs = Dbutil.selectQuery("SELECT * " + "from " + auto + "wines where Name='" + name
									+ "' and Vintage='" + Wine[i].Vintage + "' and Size=" + Wine[i].Size
									+ " and Shopid='" + Wine[i].ShopId + "' ;", winecon);
							if (rs.next()) {
								if (rs.getString("lastupdated").substring(0, 19)
										.equals(Wine[i].LastUpdated.substring(0, 19))) {
									// So it was already scraped before, now only update if the price is lower
									if (Wine[i].Price < rs.getFloat("price")) {
										// Update price only, don't a record in the history table
										// This is because if a wine is listed with two prices, we don't consider it a
										// lower price than before
										// if the "before" price was just found.
										// This does mean we are missing a lower price if the price really was lowered
										// but also a higher price was scraped before in this sesion
										rowsaffected = Dbutil.executeQuery(
												"Update LOW_PRIORITY " + auto + "wines SET Price = " + Wine[i].Price
														+ ", PriceEuroEx=" + Wine[i].PriceEuroEx + ", PriceEuroIn="
														+ Wine[i].PriceEuroIn + ", SourceURL='" + Wine[i].SourceUrl
														+ "', LastUpdated='" + Wine[i].LastUpdated + "', Rareold="
														+ Wine[i].Rareold + "  where Name='" + name
														+ "' and Vintage = '" + Wine[i].Vintage + "' and Size="
														+ Wine[i].Size + " and Shopid = '" + Wine[i].ShopId + "';",
												winecon);

									} else {
										Dbutil.logger.debug("Skipped wine " + name + " " + Wine[i].Vintage
												+ " with size " + Wine[i].Size + " at URL " + Wine[i].SourceUrl
												+ " because it was already in the DB");
									}

								} else {
									// Only consider it new is the price is lower, otherwise just update
									if (Wine[i].Price < rs.getFloat("price")) {
										// Consider it a significant update
										if (auto.equals(""))
											Dbutil.executeQuery("INSERT into history select * from wines where id="
													+ rs.getString("id") + ";", winecon);
										rowsaffected = Dbutil.executeQuery("Update LOW_PRIORITY " + auto
												+ "wines SET Price = " + Wine[i].Price + ", PriceEuroEx="
												+ Wine[i].PriceEuroEx + ", PriceEuroIn=" + Wine[i].PriceEuroIn
												+ ", SourceURL='" + Wine[i].SourceUrl + "', LastUpdated='"
												+ Wine[i].LastUpdated + "', CreateDate='" + Wine[i].LastUpdated
												+ "', Rareold=" + Wine[i].Rareold + "  where Name='" + name
												+ "' and Vintage = '" + Wine[i].Vintage + "' and Size=" + Wine[i].Size
												+ " and Shopid = '" + Wine[i].ShopId + "';", winecon);

									} else {
										// Just update
										rowsaffected = Dbutil.executeQuery(
												"Update LOW_PRIORITY " + auto + "wines SET Price = " + Wine[i].Price
														+ ", PriceEuroEx=" + Wine[i].PriceEuroEx + ", PriceEuroIn="
														+ Wine[i].PriceEuroIn + ", SourceURL='" + Wine[i].SourceUrl
														+ "', LastUpdated='" + Wine[i].LastUpdated + "', Rareold="
														+ Wine[i].Rareold + "  where Name='" + name
														+ "' and Vintage = '" + Wine[i].Vintage + "' and Size="
														+ Wine[i].Size + " and Shopid = '" + Wine[i].ShopId + "';",
												winecon);

									}

								}

							} else {
								// New wine
								rowsaffected = Dbutil.executeQuery("INSERT INTO " + auto
										+ "wines(Name, Vintage, Price, PriceEuroEx, PriceEuroIn, Size, Shopid, SourceURL, LastUpdated, CreateDate, Rareold) "
										+ " VALUES('" + name + "','" + Wine[i].Vintage + "','" + formattedPrice + "','"
										+ Wine[i].PriceEuroEx + "','" + Wine[i].PriceEuroIn + "','" + Wine[i].Size
										+ "','" + Wine[i].ShopId + "','" + Wine[i].SourceUrl + "','"
										+ Wine[i].LastUpdated + "','" + Wine[i].LastUpdated + "'," + Wine[i].Rareold
										+ ");", winecon);
							}
						}
						// Store picture if necessary
						int knownwineid = 0;
						int wineid = 0;
						try {
							// if
							// (Wijnzoeker.totalimagesize<500000000&&Wine[i].ShopId!=200&&!Wine[i].pictureurl.equals("")){
							if (Wine[i].ShopId != 200 && !Wine[i].pictureurl.equals("")) {
								knownwineid = Dbutil.readIntValueFromDB(
										"Select * from " + auto + "wines where name='" + name + "' and knownwineid>0;",
										"knownwineid");
								if (knownwineid > 0) {
									wineid = Dbutil.readIntValueFromDB("SELECT * from " + auto + "wines where Name='"
											+ name + "' and Vintage='" + Wine[i].Vintage + "' and Size=" + Wine[i].Size
											+ " and Shopid='" + Wine[i].ShopId + "' ;", "id");
									Dbutil.executeQuery(
											"insert ignore into pictureurls (wineid,knownwineid,url) values (" + wineid
													+ "," + knownwineid + ",'" + Spider.SQLEscape(Wine[i].pictureurl)
													+ "');");
									/*
									 * 
									 * previously we were saving the actual picture, now we just save the URL in
									 * table pictureurls
									 * 
									 * 
									 * 
									 * String extension=Webroutines.getRegexPatternValue("\\.(...)$",
									 * Wine[i].pictureurl); if (extension.equals("gif")) extension="png";// in 1.5
									 * writing GIF is not supported wineid=
									 * Dbutil.readIntValueFromDB("SELECT * from "+auto+"wines where Name='"
									 * +name+"' and Vintage='"+Wine[i].Vintage+"' and Size="+Wine[i].
									 * Size+" and Shopid='"+Wine[i].ShopId+"' ;","id"); String
									 * dirname="C:\\labels\\"+knownwineid; new File(dirname).mkdirs(); String
									 * filename=dirname+"\\"+wineid+"."+extension.toLowerCase(); File file=new
									 * File(filename); if (!file.exists()){ BufferedImage image = null; // Read from
									 * a URL URL url = new URL(Wine[i].pictureurl); image = ImageIO.read(url); if
									 * (image.getHeight()>0) { ImageIO.write(image, extension.toLowerCase(), file);
									 * } Wijnzoeker.totalimagesize+=file.length();
									 * Dbutil.logger.info(Wijnzoeker.totalimagesize+""); }
									 * 
									 */
								}
							}
						} catch (Exception e) {
							Dbutil.logger.info("Could not save picture with url " + Wine[i].pictureurl
									+ ", knownwineid=" + knownwineid + ", wineid=" + wineid, e);
						}
					}
				}
			}
			winecon.commit();
			addorupdaterating(Wine, auto, 0, "");
		} catch (Exception e) {
			Dbutil.logger.error("Problem during update of wines for shop " + Wine[i].ShopId + " , wine " + Wine[i].Name,
					e);
		} // end catch
		Dbutil.closeRs(rs);

	}// end main

	public static void addorupdaterating(Wine[] Wine, String auto, int issue, String issuedate) {
		Dbutil.logger.info("Wine -> addorupdaterating -> Wine=" + (Wine == null ? "NULL" : Wine.length) + ", auto="
				+ auto + ", issue=" + issue + ", issuedate=" + issuedate);
		int i = 0;
		Connection winecon = Dbutil.openNewConnection();
		// int rowsaffected;
		String name;
		try {
			// ResultSet rs;
			for (i = 0; i < Wine.length; i++) {
				name = Spider.SQLEscape(Spider.unescape(Wine[i].Name));
				Dbutil.logger.info("Wine -> addorupdaterating -> Wine name=" + name);
				// Now insert ratings
				if (!Wine[i].Vintage.equals("") && Wine[i].Ratings != null && Wine[i].Ratings.size() > 0) {
					// Store this wine in ratedwines for later analysis
					for (int j = 0; j < Wine[i].Ratings.size(); j++) {
						Dbutil.logger.info("Wine -> addorupdaterating -> Wine Wine[" + i + "].Ratings.get(j).ratinglow="
								+ Wine[i].Ratings.get(j).ratinglow + ", Wine[" + i + "].Ratings.get(j).ratinghigh="
								+ Wine[i].Ratings.get(j).ratinghigh);

						if (Wine[i].Ratings.get(j).ratinglow <= 100 && Wine[i].Ratings.get(j).ratinghigh <= 100) {
							Dbutil.logger.info("Wine -> addorupdaterating -> Inserting record in ratedwines");
							Dbutil.executeQuery(
									"insert into ratedwines (name,vintage,shopid,sourceurl,lastupdated, issuedate, rating,ratinghigh,author,issue) values "
											+ "('" + name + "','" + Wine[i].Vintage + "','" + Wine[i].ShopId + "','"
											+ Wine[i].SourceUrl + "','" + Wine[i].LastUpdated + "', '"
											+ (Wine[i].Ratings.get(j).issuedate.equals("") ? "0000-00-00 00:00:00.000"
													: Wine[i].Ratings.get(j).issuedate + " 00:00:00.000")
											+ "','" + Wine[i].Ratings.get(j).ratinglow + "','"
											+ Wine[i].Ratings.get(j).ratinghigh + "','" + Wine[i].Ratings.get(j).author
											+ "', " + issue + ")" + "on duplicate key update lastupdated='"
											+ Wine[i].LastUpdated + "', issuedate='"
											+ (issuedate.equals("") ? "0000-00-00 00:00:00.000"
													: issuedate + " 00:00:00.000")
											+ "',issue=" + issue + ", shopid=" + Wine[i].ShopId + ", sourceurl='"
											+ Wine[i].SourceUrl + "', rating='" + Wine[i].Ratings.get(j).ratinglow
											+ "', ratinghigh='" + Wine[i].Ratings.get(j).ratinghigh + "';");
						}
					}
					// Dbutil.logger.info(rs.getRow());
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem during update of wines for shop " + Wine[i].ShopId + " , wine " + Wine[i].Name,
					e);
		} // end catch
		Dbutil.closeConnection(winecon);

	}// end main

	public static Wine[] ScrapeWine(String Page, String urlstring, String Regex, String Headerregex, int shopid,
			String order, Timestamp now, Double Pricefactorex, Double Pricefactorin) {
		Dbutil.logger.debug("Scraping wines from page: " + urlstring + " with regex " + Regex);
		Pattern pattern;
		Matcher matcher;
		Float vintagefloat = new Float(0);
		int vintageint = 0;
		// int i = 0;
		boolean rareold;
		ArrayList<Wine> WineAL = new ArrayList<Wine>();
		String timestampstring = "";
		String priceString = "";
		String winebuilder, yearbuilder, pricebuilder;
		String name = null, vintage = null;
		float price = 0;
		rareold = false; // isRareOld(urlstring);
		winebuilder = order.split("/")[0];
		yearbuilder = order.split("/")[1];
		pricebuilder = order.split("/")[2];
		pattern = Pattern.compile(Regex);
		matcher = pattern.matcher(Page);
		if (now != null) {
			timestampstring = now.toString();
		}
		/*
		 * while(matcher.find()) { Dbutil.logger.debug("Counting wines: "+i);
		 * 
		 * i++; } Wine Wine[]=new Wine[i];
		 */
		// i=0;
		pattern = Pattern.compile(Regex);
		matcher = pattern.matcher(Page);
		while (matcher.find()) {
			price = 0;
			vintageint = 0;
			name = Spider.buildMatchtest(matcher, winebuilder, Headerregex, Page).replaceAll("'", "\"");
			vintage = Spider.buildMatchtest(matcher, yearbuilder, Headerregex, Page);
			if (vintage.equals("null"))
				vintage = "";
			try {
				vintagefloat = new Float(vintage);
				vintageint = vintagefloat.intValue();
			} catch (Exception e) {
				Dbutil.logger
						.debug("Could not parse vintage \"" + vintage + "\" for wine " + name + " at URL " + urlstring);

			}
			if (vintageint != 0 && vintageint <= Configuration.rareoldyear) {
				rareold = true;
			}
			try {
				priceString = (Spider.buildMatch(matcher, pricebuilder));
				if (priceString.length() > 2) {
					if (priceString.substring((priceString.length() - 3), (priceString.length() - 2)).equals(".")
							|| priceString.substring((priceString.length() - 3), (priceString.length() - 2))
									.equals(".")) {
						priceString = Spider.replaceString(priceString, ".", "");
						priceString = Spider.replaceString(priceString, ",", "");
						price = Float.valueOf(priceString).floatValue() / 100;
					}
				}
				if (price == 0) {
					price = Float.valueOf(Spider.buildMatch(matcher, pricebuilder).replaceAll(",", ".")).floatValue();
				}
				if (price != 0) {
					WineAL.add(new Wine(name, vintage, 0, price, price * Pricefactorex, price * Pricefactorin, 0.0,
							urlstring, shopid, "", "", timestampstring, timestampstring, null, rareold, "", 0, null));
					Dbutil.logger.debug("Shop " + shopid + " wine " + name + " found, price " + price);
				}
			} catch (Exception e) {
				if (priceString.length() > 1) {
					Dbutil.logger.info("Could not parse price \"" + Spider.buildMatch(matcher, pricebuilder)
							+ "\" with priceString " + priceString + " for wine " + name + " at URL " + urlstring);
				}
			}

		}
		Wine[] Wine = new Wine[WineAL.size()];

		for (int j = 0; j < WineAL.size(); j++) {
			Wine[j] = WineAL.get(j);
		}

		Dbutil.logger.debug("Finished scraping wines");

		return Wine;
	}

	public static boolean isRareOld(String url) {
		boolean rareold = false;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		rs = Dbutil.selectQuery("select scrapelist.url from scrapelist, rareoldurls " + "where scrapelist.url='"
				+ Spider.SQLEscape(url) + "' and scrapelist.url like rareoldurls.url;", con);
		try {
			if (rs.next()) {
				rareold = true;
			}

		} catch (SQLException e) {

		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		return rareold;
	}

	public int getVintage() {
		int vintage = 0;
		if (Vintage != null && Vintage.length() == 4)
			try {
				vintage = Integer.parseInt(this.Vintage);
			} catch (Exception e) {
			}
		return vintage;
	}

	public String getRatingList() {
		String ratings = "";
		NumberFormat ratingformat = new DecimalFormat("###.#");
		if (Ratings != null && Ratings.size() > 0) {
			for (int i = 0; i < Ratings.size(); i++) {
				ratings += ", " + Ratings.get(i).author + ratingformat.format(Ratings.get(i).ratinglow);
				if (Ratings.get(i).ratinghigh > 0)
					ratings += "-" + ratingformat.format(Ratings.get(i).ratinghigh);
			}
			ratings = ratings.substring(2);
		}
		return ratings;
	}

	public int getAverageRating() {
		int avg = 0;
		double rating = 0;
		int n = 0;
		try {
			if (Ratings != null && Ratings.size() > 0) {
				// Double sum = 0.0;
				for (int i = 0; i < Ratings.size(); i++) {
					n++;
					if (Ratings.get(i).ratinghigh > 0) {
						rating = rating + ((Ratings.get(i).ratinglow + Ratings.get(i).ratinghigh) / 2);
					} else {
						rating = rating + Ratings.get(i).ratinglow;
					}
				}
				avg = (int) Math.round(rating / n);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem getting average rating", e);
		}

		return avg;
	}

	public ArrayList<Winerating> getRatings(Connection con) {
		ArrayList<Winerating> ratings = new ArrayList<Winerating>();
		if (Knownwineid > 0 && !Vintage.equals("")) {
			ResultSet rs = Dbutil.selectQuery(
					"select * from ratedwines where knownwineid=" + Knownwineid + " and vintage=" + Vintage + ";", con);
			try {
				if (rs != null) {
					while (rs.next()) {
						Winerating wr = new Winerating(rs.getString("author"), rs.getInt("rating"),
								rs.getInt("ratinghigh"));
						wr.link = Winerating.getLink(rs.getString("author"), rs.getString("name"));
						ratings.add(wr);
					}
				}
			} catch (Exception e) {
				Dbutil.logger.error("Problem getting ratings", e);
			}
		}
		return ratings;
	}

	public Wine(Record record, Double pricefactorex, Double pricefactorin, Double cpc, String country,
			String createDate, String id, int knownwineid, String lastUpdated, boolean rareold,
			ArrayList<Winerating> ratings, int shopId, String shopname, String shopurl, String sourceUrl,
			String pictureurl, String baseurl, boolean appendshopname) {
		super();
		Name = (String) record.get("Producer");
		if (Name == null)
			Name = "";
		if (record.get("Region") != null && !Name.contains((String) record.get("Region")))
			Name += (" " + (String) record.get("Region"));
		if (record.get("Name") != null && !Name.contains((String) record.get("Name")))
			Name += (" " + (String) record.get("Name"));
		if (appendshopname && Name.length() > 0)
			Name += " " + shopname;

		/*
		 * if (!record.get("Name").equals(record.get("Producer"))) { if
		 * (((String)record.get("Name"))==null||"".equals((String)record.get("Name"))||
		 * Name.contains((String)record.get("Name"))){ // Do nothing } else if
		 * (((String)record.get("Name")).contains((String)record.get("Producer"))){
		 * Name=(String)record.get("Name"); } else {
		 * Name+=" "+(String)record.get("Name"); } } if
		 * (!record.get("Producer").equals(record.get("Region"))) { if
		 * (((String)record.get("Region"))==null||"".equals((String)record.get("Region")
		 * )||Name.contains((String)record.get("Region"))){ // Do nothing } else if
		 * (((String)record.get("Region")).contains(Name)){
		 * Name=(String)record.get("Region"); } else {
		 * Name+=" "+(String)record.get("Region"); } }
		 */
		Vintage = (String) record.get("Vintage");
		if (Vintage == null)
			Vintage = "0";
		Price = TableScraper.parsePrice((String) record.get("Price"));
		try {
			Size = (Float) record.get("Bottlesize");
		} catch (Exception e) {
		}
		PriceEuroEx = Price * pricefactorex;
		PriceEuroIn = Price * pricefactorin;
		CPC = cpc;
		Country = country;
		CreateDate = createDate;
		try {
			Id = Integer.parseInt(id);
		} catch (Exception e) {
		}
		Knownwineid = knownwineid;
		LastUpdated = lastUpdated;
		Rareold = rareold;
		Ratings = ratings;
		ShopId = shopId;
		Shopname = shopname;
		Shopurl = shopurl;
		SourceUrl = Spider.padUrl((String) record.get("Url"), sourceUrl, baseurl, shopurl);
		SourceUrl = TableScraper.cleanUrl(SourceUrl);
		this.pictureurl = pictureurl;
	}

	public static class pqratioComparator implements Comparator<Wine> {

		// Comparator interface requires defining compare method.
		public int compare(Wine a, Wine b) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			if (a.pqratio > b.pqratio) {
				return -1;

			} else if (a.pqratio < b.pqratio) {
				return 1;

			} else {
				return 0;
			}
		}
	}

	public static class relativepriceComparator implements Comparator<Wine> {

		// Comparator interface requires defining compare method.
		public int compare(Wine a, Wine b) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			if (a.relativeprice < b.relativeprice) {
				return -1;

			} else if (a.relativeprice > b.relativeprice) {
				return 1;

			} else {
				return 0;
			}
		}
	}

	public static class priceComparator implements Comparator<Wine> {

		// Comparator interface requires defining compare method.
		public int compare(Wine a, Wine b) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			if (a.PriceEuroEx < b.PriceEuroEx) {
				return -1;

			} else if (a.PriceEuroEx > b.PriceEuroEx) {
				return 1;

			} else {
				return 0;
			}
		}
	}

	public static class ratingComparator implements Comparator<Wine> {

		// Comparator interface requires defining compare method.
		public int compare(Wine a, Wine b) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			if (a.getAverageRating() > b.getAverageRating()) {
				return -1;

			} else if (a.getAverageRating() < b.getAverageRating()) {
				return 1;

			} else {
				return new pqratioComparator().compare(a, b);
			}
		}
	}

	public static class vintageComparator implements Comparator<Wine> {

		// Comparator interface requires defining compare method.
		public int compare(Wine a, Wine b) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.
			int vintagea = 0;
			try {
				vintagea = Integer.parseInt(a.Vintage);
			} catch (Exception e) {
			}
			int vintageb = 0;
			try {
				vintageb = Integer.parseInt(b.Vintage);
			} catch (Exception e) {
			}

			if (vintagea > vintageb) {
				return -1;

			} else if (vintagea < vintageb) {
				return 1;

			} else {
				return 0;
			}
		}
	}

	public int getrecommendationscore() {
		int score = 0;
		if (relativeprice < 1.2)
			score = score + 10;
		if (relativeprice < 1.1)
			score = score + 10;
		if (relativeprice < 1.01)
			score = score + 10;
		if (pqratio > 1)
			score = score + 11;
		if (pqratio > 1.5)
			score = score + 11;
		if (pqratio > 2)
			score = score + 11;
		return score;
	}

	public static class bestDealComparator implements Comparator<Wine> {

		// Comparator interface requires defining compare method.
		public int compare(Wine a, Wine b) {
			// ... Sort directories before files,
			// otherwise alphabetical ignoring case.

			if (a.getrecommendationscore() > b.getrecommendationscore()) {
				return -1;

			} else if (a.getrecommendationscore() < b.getrecommendationscore()) {
				return 1;

			} else {
				return 0;
			}
		}
	}

}

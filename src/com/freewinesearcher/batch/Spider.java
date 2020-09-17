package com.freewinesearcher.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringEscapeUtils;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.datafeeds.DataFeed;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.parser.xpathparser.XpathParser;

public class Spider implements Runnable {

	String auto;
	String id;
	String encoding;
	int shoptype;
	public Connection executecon;
	java.sql.Timestamp now;
	boolean pagenotfound;
	public boolean muststopthisthread = false;
	public Context c;
	public int issue = 0; // For WS/Parker
	public String issuedate;
	long interval = Configuration.minimumCrawlInterval; // at least " interval" milliseconds between each crawl
	int shopid;
	String shopName;

	public Spider(String id, String encoding, String auto, int shoptype) {
		super();
		this.auto = auto;
		this.id = id;
		this.shopid = 0;
		try {
			shopid = Integer.parseInt(id);
		} catch (Exception e) {
		}
		this.encoding = encoding;
		this.pagenotfound = false;
		this.shoptype = shoptype;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			SpiderStarted();
			Thread.currentThread().setName("Shop " + id);
			shopName = getShopName();
			Dbutil.logger.info("Starting spidering of shop " + id + " (" + shopName + ") ");
			now = new java.sql.Timestamp(new java.util.Date().getTime());
			updateShopStatus(false);
			// if (auto.equals("")) auto="cached";
			// Connection cachedcon=openCachedWinesConnection();
			ArrayList<String> Urlsfound = new ArrayList<String>(0); // temp container for found Urls while spidering
			ArrayList<UrlSpider> Urlregex = new ArrayList<UrlSpider>(0);
			ArrayList<ArrayList<String>> Urllist;
			boolean moretodo;
			ResultSet rs = null;
			String issuedate = ""; // this is only used for making up a file name, not for issues of individual
									// wines
			String Page;
			boolean succes = true;
			int totaldatafromweb = 0;
			int totalRPwines = 0;
			String Url = "";
			String Postdata;
			boolean ignorepagenotfound = false;
			String Regex = "Leeg";
			String Regexescaped = "Leeg";
			String Headerregex = "";
			String Headerregexescaped = "";
			String Urltype = "";
			String shopurl = "";
			String Order = "Leeg";
			String tablescraper = "";
			String viewstate = "";
			String urlrowid = "";
			String type = "";
			boolean followredirects = true;
			String Parenturl;
			Wine[] wine;
			Double pricefactorex;
			Double pricefactorin;
			String Baseurl = getBaseUrl();
			XpathWineScraper xp;
			DataFeed datafeed = DataFeed.getDataFeed(new Context(1), shopid, 0);
			int totalscraped = 0;
			int pagenum = 0;
			String shopname = "";
			Connection Spidercon = Dbutil.openNewConnection();
			boolean soldwinesremoved = false;
			int minwait = 0;
			int maxwait = 0;
			int shopid = 0;
			java.sql.Timestamp lastcrawl = new java.sql.Timestamp(new java.util.Date().getTime());
			long currentinterval;
			int crawls = 0;
			try {
				shopid = Integer.parseInt(id);
			} catch (Exception e) {
			}
			Webpage webpage = new Webpage();
			// if (!Wijnzoeker.proxyaddress.equals("")&&Wijnzoeker.proxyport!=0){
			// if (!id.equals("4")){
			// webpage.proxy=new Proxy(Proxy.Type.HTTP, new
			// InetSocketAddress(Wijnzoeker.proxyaddress, Wijnzoeker.proxyport));
			// }
			// }
			String cookie = "";

			// Get encoding, followredirects, urltype and ignorepagenotfound
			try {
				rs = Dbutil.selectQuery("SELECT * from " + auto + "shops where id='" + id + "';", Spidercon);
				if (rs.next()) {
					ignorepagenotfound = rs.getBoolean("ignorepagenotfound");
					followredirects = rs.getBoolean("followredirects");
					Urltype = rs.getString("urltype");
					shopurl = rs.getString("shopurl");
					shopname = rs.getString("shopname");
					cookie = rs.getString("cookie");
					minwait = rs.getInt("minwait");
					maxwait = rs.getInt("maxwait");
					encoding = rs.getString("Encoding");
					if (!Urltype.equals("Email") && rs.getString("Encoding").equals("")) {
						rs = Dbutil.selectQuery("SELECT * from " + auto + "scrapelist where shopid='" + id + "';",
								Spidercon);
						if (rs.next()) {
							encoding = Spider.getHtmlEncoding(rs.getString("Url"));
							Dbutil.executeQuery(
									"update " + auto + "shops set encoding = '" + encoding + "' where id=" + id + ";");
						}
					}
				}

			} catch (Exception exc) {
				Dbutil.logger.error(
						"Could not determine the encoding for Shop ID: " + id + " Shop Name: " + shopName + " ", exc);
			}
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(Spidercon);

			pricefactorex = Dbutil.getPriceFactorex(id);
			pricefactorin = Dbutil.getPriceFactorin(id);

			if (datafeed != null) {
				wine = datafeed.spider(shopid, now, pricefactorex, pricefactorin);
				Wine.addorupdatewine(wine, auto);
				totalscraped = wine.length;
				if (totalscraped > 0) {
					soldwinesremoved = removeSoldWines(id);
				}
			} else {

				try {
					if (!java.nio.charset.Charset.isSupported(encoding)) {
						Dbutil.logger.warn("Charset " + encoding + " is not supported for Shop ID: " + id
								+ " Shop Name: " + shopName + ", trying with ISO-8859-1");
						encoding = "ISO-8859-1";
					}
				} catch (Exception exc) {
					Dbutil.logger.error("Unknown charset \"" + encoding + "\" for Shop ID: " + id + " Shop Name: "
							+ shopName + " ");
					encoding = "ISO-8859-1";
					succes = false;
				}

				try {
					Urlregex = getUrlRegex();
					xp = new XpathWineScraper(Baseurl, null, now, pricefactorex, pricefactorin, shopid, shopname,
							shopurl);
					if (c != null)
						xp.parsers = XpathParser.getXpathParsers(c, shopid);
					// Update the status of all found records to Ready: all must be searched
					updateUrlStatusses("", "Delete");// Delete will only affect spidered URL's
					updateUrlStatusses("", "Ready");
					webpage = new Webpage(Baseurl, encoding, "", ignorepagenotfound, followredirects, shopid, shopname);
					webpage.standardcookie = cookie;
					webpage.readPage();

					// Now let's scrape as long as we find records with status Ready
					// The scrapelist loop gets reinitialized so we see new URL's
					moretodo = true;
					while (moretodo && !Wijnzoeker.muststopnow) {
						Urllist = getScrapeList("Ready");
						if (Urllist.size() == 0)
							moretodo = false;
						for (int i = 0; i < Urllist.size(); i++) {
							crawls++;
							if (crawls > Configuration.maxCrawlsPerSite) {
								moretodo = false;
								i = Urllist.size();
								if (crawls == Configuration.maxCrawlsPerSite)
									Dbutil.logger.error("Number of pagecrawls for shop " + id + " (" + getShopName()
											+ " has exceeded the maximum of " + Configuration.maxCrawlsPerSite);
							} else {
								if (!Wijnzoeker.muststopnow && !muststopthisthread) {
									Url = Urllist.get(i).get(0);
									if (shoptype == 2 && Url.contains("%ISSUEDATE%") && !"".equals(issuedate)
											&& !"0000-00-00".equals(issuedate)) {
										Url = Url.replace("%ISSUEDATE%", issuedate);
									}
									if (maxwait > 0 && !Url.startsWith("file://")) {
										Long waittime = (long) (1000 * (minwait + Math.random() * (maxwait - minwait)));
										Thread.sleep(waittime);
									}
									Postdata = Urllist.get(i).get(1);
									type = Urllist.get(i).get(2);
									Regex = Urllist.get(i).get(3);
									Headerregex = Urllist.get(i).get(4);
									Order = Urllist.get(i).get(5);
									tablescraper = Urllist.get(i).get(6);
									Parenturl = Urllist.get(i).get(7);
									viewstate = Urllist.get(i).get(8);
									urlrowid = Urllist.get(i).get(9);
									updateUrlStatus(urlrowid, "Scraping");
									Regexescaped = replaceString(Regex, "\\", "\\\\");
									Regexescaped = replaceString(Regexescaped, "'", "\\'");
									Headerregexescaped = replaceString(Headerregex, "\\", "\\\\");
									Headerregexescaped = replaceString(Headerregexescaped, "'", "\\'");

									Urlsfound = null;
									Page = "";
									if (type.equals("Email")) {
										Baseurl = "File";
										Page = getPageFromEmail(false);
									} else {
										if (viewstate != null && !viewstate.equals("")) {
											webpage.postdata = Postdata + "&__VIEWSTATE=" + viewstate;
										} else {
											webpage.postdata = Postdata;
										}
										webpage.encoding = encoding;
										if (shoptype == 1) {
											// webpage.useragent="Mozilla/5.0 (compatible; Vinopedia price updater;
											// https://www.vinopedia.com/about.jsp)";
										}
										webpage.urlstring = Url;
										webpage.ignorepagenotfound = ignorepagenotfound;
										webpage.followredirect = followredirects;
										currentinterval = (interval
												- new java.sql.Timestamp(new java.util.Date().getTime()).getTime()
												+ lastcrawl.getTime());
										if (currentinterval > 0) {
											Thread.sleep(currentinterval);
										}
										lastcrawl = new java.sql.Timestamp(new java.util.Date().getTime());
										webpage.readPage();
										Page = webpage.html;

										// if (!"PRD".equals(Wijnzoeker.serverrole)) Dbutil.logger.info(Page);

									}
									totaldatafromweb += Page.length();
									if (Page.equals("Webpage unavailable") && (ignorepagenotfound == false)) {
										pagenotfound = true;
										Dbutil.logger.warn("Web page error Shop ID: " + id + " Shop Name: (" + shopName
												+ ") url=" + Url + ", parent " + Parenturl);
										succes = false;
									}
									if (!Page.startsWith("Webpage")) {
										// Now we harvest all URLs if this is a master or spidered URL

										if (type.equals("Master") || type.equals("Spidered")
												|| type.equals("Spideredwithsource")) {
											Urlsfound = ScrapeUrl(Page, Urlregex, Url, Baseurl, id, Postdata,
													"notforurls", type);
											addUrl(Urlsfound, Regexescaped, Headerregexescaped, tablescraper, Order,
													"0", "Spidered", Postdata, Url);
										}
										if (type.equals("Master") || type.equals("Spidered")) {
											Urlsfound = ScrapeASPXUrl(Page, Urlregex, Url, Postdata, Baseurl, id,
													"notforurls");
											addASPXUrl(Urlsfound, Regexescaped, Headerregexescaped, tablescraper, Order,
													"0", "Spidered", Postdata, Url);
										}

										// Of course we harvest all wines for a shop
										wine = null;

										if (xp.parsers != null && xp.parsers.size() > 0) {
											wine = xp.scrape(Page, Url, Postdata);
										} else {
											// if (tablescraper.equals("0")){
											// wine=Wine.ScrapeWine(Page, Url, Regex,Headerregex, shopid, Order,now,
											// pricefactorex, pricefactorin);
											// } else {
											wine = TableScraper.ScrapeWine(Page, Baseurl, shopid, Headerregex, Url,
													tablescraper, now, pricefactorex, pricefactorin, auto);
											// }
										}

										if (shoptype == 1) {
											// if (auto.equals("")){
											// Wine.addorupdatewine(wine,"cached",cachedcon);
											// } else {
											Wine.addorupdatewine(wine, auto);
											// }
										}

										if (shoptype == 2) {
											if (issuedate.equals("") || issuedate.equals("0000-00-00"))
												issuedate = determineIssueDate(Url, Page);
											if (totalRPwines == 0) {
												try {
													totalRPwines = Integer.parseInt(Webroutines.getRegexPatternValue(
															"<span id=\"ctl00_ContentMaster_numLabel\" class=\"search_label\">(\\d+)</span>",
															Page)); // this is used for logging purposes
												} catch (Exception e) {
													// Dbutil.logger.error("Problem: ", e);
												}
											}
											if (!Url.startsWith("file")) {
												pagenum++;
												savePage(Url, issuedate, pagenum, Page, issue + "");
											}
											Wine.addorupdaterating(wine, auto, issue, issuedate);

										}
										totalscraped += wine.length;
										updateUrlWinesFound(urlrowid, wine.length);
										if (wine.length == 0) {
											// URL is obsolete or does not contain wines
											updateUrlStatus(urlrowid, "Nowines");
										} else {
											updateUrlStatus(urlrowid, "Done");
										}
										/*
										 * if (getShopName().toLowerCase().contains("robertparker")){ if
										 * ((wine==null||wine.length<2)&&(Urlsfound.size()!=5&&Urlsfound.size()!=10)){
										 * // In case of scraping of Robert Parker, abort in case a page // does not
										 * contain wines or links (2 or 1 * 5 due to Urlsfound structure): // something
										 * could have gone wrong Wijnzoeker.muststopnow=true; Dbutil.logger.
										 * error("While scraping RP, we found a page without wines or links. Aborting session. scrapelist id is "
										 * +urlrowid+", file="+id+"_"+issuedate+"_"+pagenum+".txt"); } }
										 */
										if (getShopName().toLowerCase().contains("wine spectator")
												&& Url.contains("wine/search")) {
											if (wine == null || wine.length < 2) {
												// In case of scraping of Wine Spectator, abort in case a page
												// does not contain wines: something could have gone wrong
												Wijnzoeker.muststopnow = true;
												Dbutil.logger.error(
														"While scraping WS, we found a page without wines. Aborting session. Scrapelist id is "
																+ urlrowid + ", file=" + id + "_" + issuedate + "_"
																+ pagenum + ".txt");
											}
										}
										Urlsfound = null;
										wine = null; // Let's help the garbageman
									}
									Page = null;
									System.gc();
								}
							}
						}
					}
					// closeCachedWinesConnection(cachedcon);
					if (totalscraped > 0 && pagenotfound == false && !Wijnzoeker.muststopnow) {
						soldwinesremoved = removeSoldWines(id);

					}
					// int i=deleteObsoleteUrls(id);
					// Dbutil.logger.debug("Removed "+i+" obsolete Urls for shop "+id);

				} catch (InterruptedException exc) {
					Dbutil.logger.info("Spider thread was stopped. Shop ID: " + id + " Shop Name: " + shopName + " ");
					muststopthisthread = true;
					succes = false;
				} catch (Exception exc) {
					Dbutil.logger.error("Iets ging mis met het spideren van Shop ID: " + id + " Shop Name: (" + shopName
							+ "), url=" + Url + ": ", exc);
					// closeCachedWinesConnection(cachedcon);
					succes = false;
				}
			}
			if (!muststopthisthread) {
				if (type.equals("Email")) {
					if (totalscraped > 0 && pagenotfound == false && soldwinesremoved == true && !Wijnzoeker.muststopnow
							&& succes == true) {
						Scrapestatistics(totalscraped, 0);
						updateShopStatus(true);
						getPageFromEmail(true);// Remove file

					} else {
						if (pagenotfound == true) {
							updateShopStatus(false);
							Dbutil.logger.error("Email received for Shop ID: " + id + " Shop Name: " + shopName
									+ " but there was a problem processing the price list.");
						} else {
							Dbutil.logger.info("No mail today for Shop ID: " + id + " Shop Name: " + shopName + " ");
						}
					}
				} else {
					if (shoptype == 2) {
						Dbutil.logger.info(
								"I scraped " + totalscraped + " wines from " + totalRPwines + " for issue " + issue);
					}
					Scrapestatistics(totalscraped, checkShop(webpage));
					if (totalscraped > 0 && pagenotfound == false && soldwinesremoved == true && !Wijnzoeker.muststopnow
							&& succes == true) {
						updateShopStatus(true);
					}
				}

				if (Wijnzoeker.muststopnow) {
					Dbutil.logger.warn("Interrupted spidering of Shop ID: " + id + " Shop Name: " + shopName
							+ ") due to the time limits");

				} else {
					Dbutil.logger.info("Finished spidering of Shop ID: " + id + " Shop Name: " + shopName + " ");
					SpiderEnded(id, succes);
				}
				// removeOldWines();

			}
			wine = null;
			Urlsfound = null;
			Urlregex = null;
			Urllist = null;
			rs = null;
			Dbutil.logger
					.info(totaldatafromweb + " bytes scraped for Shop ID: " + id + " Shop Name: " + shopName + " ");
		} catch (Exception exc) {
			muststopthisthread = true;
			Dbutil.logger.error("Iets ging mis met het spideren van Shop ID: " + id + " Shop Name: " + shopName + " ",
					exc);

		}

	}

	private void savePage(String Url, String issuedate, int pagenum, String Page, String issue) {
		String filename = System.getProperty("file.separator") + id + "_" + issue + "_" + issuedate + "_" + pagenum
				+ ".txt";
		try {
			// Create file
			String dir = Configuration.cachedpagesdir + System.getProperty("file.separator") + id
					+ System.getProperty("file.separator") + (issue.equals("0") ? issuedate : issue);
			Dbutil.logger.info("************** SPIDER -> SAVEpAGE -> CREATING DIRECTORY : " + dir);
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			File file = new File(directory, filename);
			// FileWriter fstream = new FileWriter(dir + filename);
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(Page);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			Dbutil.logger.error("************** ERROR: SPIDER -> SAVEpAGE -> CREATING -> " + e.getMessage(), e);
		}
	}

	private void updateShopStatus(boolean succes) {
		if (succes) {
			Dbutil.executeQuery("Update shops set succes=0 where id=" + id + ";");
		} else {
			Dbutil.executeQuery("Update shops set succes=succes+1 where id=" + id + " and succes <99;");
		}
	}

	public String getPageFromEmail(boolean delete) {
		File file;
		File dir = new File(Configuration.emaildir + id);
		String Page = "Webpage empty because no email was received";
		String[] filenames;
		if (dir.exists()) {
			filenames = dir.list();
			if (filenames.length > 1) {
				Dbutil.logger.error(
						"Error: Received more than one attachment for shop " + id + ", will not process any files.");
				pagenotfound = true;
			} else {
				for (String filename : filenames) {
					file = new File(Configuration.emaildir + id + System.getProperty("file.separator") + filename);
					try {
						Page = "Webpage cannot be read from file";
						if (filename.endsWith(".xls") || filename.endsWith(".XLS")) {
							Page = Excelreader.ReadFile(file);
						} else {
							StringBuffer sb = new StringBuffer();
							String inputLine;
							BufferedReader in = new BufferedReader(
									new InputStreamReader(new FileInputStream(file), encoding));
							while ((inputLine = in.readLine()) != null) {
								sb.append(inputLine);
								sb.append("\n");
							}
							in.close();
							Page = sb.toString();
						}
						if (delete == true && !Page.startsWith("Webpage")) {
							if (!file.delete()) {
								Dbutil.logger.error("Could not delete file name " + file.getName());
							}
						}
						if (Page.startsWith("Webpage") && !Page.equals("Webpage empty because no email was received")) {
							pagenotfound = true;
						}
					} catch (Exception exc) {
						Dbutil.logger.error("Cannot read file " + Configuration.emaildir + id
								+ System.getProperty("file.separator") + file.getName());
						pagenotfound = true;
					}
				}
			}
		}
		return Page;
	}

	private void Scrapestatistics(int totalscraped, int checkfromsite) {
		int totalinDB = 0;
		String onsite = "";
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT count(*) AS Total " + "from " + auto + "wines where Shopid='" + id + "' ;",
					con);
			if (rs.next()) {
				totalinDB = rs.getInt("total");
			}
		} catch (Exception e) {
			Dbutil.logger.error("Fout bij statistics: ", e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		if (checkfromsite > 0) {
			onsite = "There should be " + checkfromsite + " wines on the site. ";
		}
		Dbutil.logger.info(onsite + "We scraped " + totalscraped + " from web, " + totalinDB + " stored in DB for shop "
				+ id + " (" + getShopName() + ")");
		if (checkfromsite > totalscraped * 1.1) {
			Dbutil.logger.warn("There should be " + checkfromsite + " wines on the web, but I only found "
					+ totalscraped + "for shop " + id + "(" + getShopName() + ")");
		}
	}

	public int checkShop(Webpage webpage) {
		int target = 0;
		String url = "";
		int multiplier;
		String Page = "";
		Pattern pattern;
		Matcher matcher;
		String regex;
		ResultSet rs = null;
		String postdata;
		Connection con = Dbutil.openNewConnection();

		try {
			rs = Dbutil.selectQuery("SELECT * from " + auto + "checkshop where Shopid=" + id + ";", con);
			if (rs.first()) {
				url = rs.getString("url");
				regex = rs.getString("regex");
				postdata = rs.getString("postdata");
				multiplier = rs.getInt("multiplier");
				if (multiplier == 0)
					multiplier = 1;
				webpage.urlstring = url;
				webpage.postdata = postdata;
				webpage.readPage();
				Page = webpage.html;
				pattern = Pattern.compile(regex);
				matcher = pattern.matcher(Page);
				if (matcher.find()) {
					target = Integer.parseInt(matcher.group(1)) * multiplier;
				}
				if (target > 0) {
					Dbutil.executeQuery("update " + auto + "shops set amountwines=" + target + " where id=" + id + ";");
				}

			}

		} catch (Exception e) {
			Dbutil.logger.error("Fout bij statistics: ", e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);

		return target;

	}

	public static int checkShop(String url, String regex, String postdata, int multiplier) {
		if (multiplier == 0)
			multiplier = 1;
		int target = 0;
		String Page = "";
		Pattern pattern;
		Matcher matcher;
		if (url != null && !url.equals("") && regex != null && !regex.equals("")) {
			Page = getWebPage(url, "iso-8859-1", null, postdata, "N");
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(Page);
			if (matcher.find()) {
				target = Integer.parseInt(matcher.group(1)) * multiplier;
			}
		}
		return target;

	}

	private boolean removeSoldWines(String shopid) {
		int deleted = 0;
		boolean succes = false;
		ResultSet rs = null;
		int totalinDB = 0;
		int totalupdated = 0;
		Connection con = Dbutil.openNewConnection();
		try {

			rs = Dbutil.selectQuery("SELECT count(*) AS Total " + "from " + auto + "wines where Shopid=" + shopid
					+ " and Lastupdated='" + now + "';", con);
			if (rs.next())
				totalupdated = rs.getInt("Total");
			Dbutil.closeRs(rs);
			rs = Dbutil.selectQuery("SELECT count(*) AS Total " + "from " + auto + "wines where Shopid=" + shopid + ";",
					con);
			if (rs.next())
				totalinDB = rs.getInt("Total");

			// Remove wines if 65% of wines were found
			if (totalupdated > 0.65 * totalinDB) {
				int moved = Dbutil.executeQuery("INSERT into history select * from " + auto + "wines where Shopid='"
						+ shopid + "' and Lastupdated !='" + now + "'");
				deleted = Dbutil.executeQuery(
						"DELETE from " + auto + "wines where Shopid='" + shopid + "' and Lastupdated !='" + now + "'");
				if (deleted > 0)
					Dbutil.logger.info("Removed " + deleted + " wines from shop " + shopid);
				if (moved != deleted)
					Dbutil.logger.error("Error: " + moved + " rows moved to history but " + deleted
							+ " rows deleted from wines!!!");
				succes = true;
			} else {
				Dbutil.logger.warn("I only found " + totalupdated + " wines out of " + totalinDB + " for shop " + shopid
						+ " (" + getShopName() + "). Did not remove any wines.");

			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem in removing wines: ", e);
		} // end catch
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		return succes;
	}

	// private int removeOldWines() {
	// int deleted = 0;
	// Connection con = Dbutil.openNewConnection();
	// try {
	// Dbutil.executeQuery(
	// "delete from materializedadvice where id in (select id from " + auto + "wines
	// where Shopid='" + id
	// + "' AND (TO_DAYS(curdate())-TO_DAYS(lastupdated))>" +
	// Configuration.graceperiod + ");",
	// con);
	// int moved = Dbutil.executeQuery("INSERT into history select * from " + auto +
	// "wines where Shopid='" + id
	// + "' AND (TO_DAYS(curdate())-TO_DAYS(lastupdated))>" +
	// Configuration.graceperiod + ";");
	// deleted = Dbutil.executeQuery("DELETE from " + auto + "wines where Shopid='"
	// + id
	// + "' AND (TO_DAYS(curdate())-TO_DAYS(lastupdated))>" +
	// Configuration.graceperiod + ";");
	// if (deleted > 0)
	// Dbutil.logger.info("Removed " + deleted + " wines from shop " + id + "
	// because they were older than "
	// + Configuration.graceperiod + " days");
	// if (moved != deleted)
	// Dbutil.logger.error(
	// "Error: " + moved + " rows moved to history but " + deleted + " rows deleted
	// from wines!!!");
	//
	// } catch (Exception e) {
	// Dbutil.logger.error("Error removing " + Configuration.graceperiod + " days
	// old wines: ", e);
	// } // end catch
	// Dbutil.closeConnection(con);
	// return deleted;
	// }

	public static int removeAllOldWines() {
		int deleted = 0;
		Connection con = Dbutil.openNewConnection();
		try {
			Dbutil.executeQuery(
					"delete from materializedadvice where id in (select id from wines where (TO_DAYS(curdate())-TO_DAYS(lastupdated))>"
							+ Configuration.graceperiod + ");",
					con);
			int moved = Dbutil.executeQuery(
					"INSERT into history select * from wines where (TO_DAYS(curdate())-TO_DAYS(lastupdated))>"
							+ Configuration.graceperiod + ";");
			deleted = Dbutil.executeQuery("DELETE from wines where (TO_DAYS(curdate())-TO_DAYS(lastupdated))>"
					+ Configuration.graceperiod + ";");
			if (deleted > 0)
				Dbutil.logger.info("Removed " + deleted + " wines from inactive shops because they were older than "
						+ Configuration.graceperiod + " days");
			if (moved != deleted)
				Dbutil.logger.error(
						"Error: " + moved + " rows moved to history but " + deleted + " rows deleted from wines!!!");

		} catch (Exception e) {
			Dbutil.logger.error("Error removing " + Configuration.graceperiod + " days old wines: ", e);
		} // end catch
		Dbutil.closeConnection(con);
		return deleted;
	}

	public static String OBSOLETEgetWebPage(String urlstring, String encoding, String postdata,
			boolean ignorepagenotfound) {
		Webpage webpage = new Webpage(urlstring, encoding, postdata, ignorepagenotfound, true, 0, "");
		webpage.readPage();
		return webpage.html;
	}

	public static String getWebPage(String urlstring, String encoding, String postdata, String cookie,
			boolean ignorepagenotfound, boolean followredirect) {
		Webpage webpage = new Webpage(urlstring, encoding, postdata, ignorepagenotfound, true, 0, "");
		webpage.setCookie(cookie);
		webpage.readPage();
		return webpage.html;
	}

	public static String getWebPage(String urlstring, String encoding, Variables var, String postdata,
			String ignorepagenotfoundstring) {
		boolean ignorepagenotfound = false;
		if (ignorepagenotfoundstring.toLowerCase().startsWith("y"))
			ignorepagenotfound = true;
		Webpage webpage = new Webpage(urlstring, encoding, postdata, ignorepagenotfound, true, 0, "");
		if (var != null)
			webpage.setCookie(var.Sessionid);
		webpage.readPage();
		return webpage.html;
	}

	public static String getWebPage(String urlstring, String encoding, Variables var, String postdata,
			String ignorepagenotfoundstring, boolean followredirect) {
		boolean ignorepagenotfound = false;
		if (ignorepagenotfoundstring.toLowerCase().startsWith("y"))
			ignorepagenotfound = true;
		Webpage webpage = new Webpage(urlstring, encoding, postdata, ignorepagenotfound, true, 0, "");
		webpage.setCookie(var.Sessionid);
		webpage.followredirect = followredirect;
		webpage.readPage();
		return webpage.html;
	}

	public static String getFeed(String urlstring, String separator, String encoding, Variables var,
			String ignorepagenotfound, boolean followredirect) {
		String Page = "";
		int attempt = 0;
		int maxattempts = 3;
		boolean succes = false;
		String exceptionmessage = "";
		urlstring = Spider.replaceString(urlstring, " ", "%20");
		URL url = null;
		HttpURLConnection urlcon;
		// long lastmodified;
		String inputLine;
		StringBuffer sb = new StringBuffer();
		try {
			url = new URL(urlstring);
		} catch (Exception exc) {
			Dbutil.logger.warn("Foute URL " + urlstring);
			// throw new MalformedURLException();
		}
		Dbutil.logger.debug("Starting to get web page");

		while (succes == false && attempt < maxattempts) {
			try {
				attempt++;
				urlcon = (HttpURLConnection) url.openConnection();
				urlcon.setConnectTimeout(120000);
				urlcon.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
				urlcon.setInstanceFollowRedirects(followredirect); // Set true for shop 77 to work
				if (var != null) {
					if (var.Sessionid != null) {
						urlcon.setRequestProperty("Cookie", var.Sessionid);
						// System.out.println("Set cookie to "+var.Sessionid);
					}
				}

				// lastmodified=urlcon.getLastModified();
				if (encoding == null || encoding.equals("")) {
					encoding = "ISO-8859-1";
				}
				if (!java.nio.charset.Charset.isSupported(encoding)) {
					// Dbutil.logger.info("Charset "+encoding+" is not supported for URL
					// "+urlstring+", trying with ISO-8859-1");
					encoding = "ISO-8859-1";
				}
				// System.out.println("Cookie: "+urlcon.getHeaderField("Set-Cookie"));
				if (var != null) {
					// System.out.println("var is niet null");
					// System.out.println("var.Sessionid:"+var.Sessionid);

					if (var.Sessionid == null || var.Sessionid.equals("null")) {
						var.Sessionid = "";
						String cookieVal = "";
						String headerName = "";
						for (int i = 1; (headerName = urlcon.getHeaderFieldKey(i)) != null; i++) {
							if (headerName.equals("Set-Cookie")) {
								cookieVal = urlcon.getHeaderField(i);
								cookieVal = cookieVal.substring(0, cookieVal.lastIndexOf(";") + 1) + " ";
								if (cookieVal != null && cookieVal.contains(";")) {
									var.Sessionid = var.Sessionid + cookieVal; // .substring(0, cookieVal.indexOf(";"));
								}
							}
						}
					}
				}

				// System.out.println("Sessionid:"+var.Sessionid);
				sb.append("<table>");
				BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream(), encoding));
				while ((inputLine = in.readLine()) != null) {
					sb.append("<tr><td>");
					sb.append(Spider.replaceString(inputLine, "|", "</td><td>"));
					sb.append("</td></tr>");

				}
				sb.append("</table>");

				in.close();
				urlcon.disconnect();
				Page = sb.toString();
				sb = null;
				Dbutil.logger.debug("Page retrieved from url " + urlstring);
				succes = true;
			} catch (Exception exc) {
				exceptionmessage = exc.toString();
				if (ignorepagenotfound.equalsIgnoreCase("N")) {
					Dbutil.logger.debug("Cannot find wine feed, attempt " + attempt + ", URL = " + urlstring, exc);
				}
			}
		}
		if (succes == false) {
			if (ignorepagenotfound.equalsIgnoreCase("Y")) {
				Page = "Webpage can be ignored";
			} else {
				Dbutil.logger.warn("Cannot find web page after " + (attempt)
						+ " tries, will not delete wines from this shop. Problem url= " + urlstring + ", error: "
						+ exceptionmessage);
				Page = "Webpage unavailable";
			}
		}
		return Page;
	}

	public static String getHtmlEncoding(String urlstring) {
		URL url = null;
		HttpURLConnection urlcon;
		String encoding = "Unknown";
		String key = "charset=";
		// String page = "";
		String inputLine;
		// StringBuffer sb = new StringBuffer();
		// int bufSize = 2000;
		// byte[] buf = new byte[bufSize];
		// int len;
		if (!urlstring.equals("Email")) {
			try {
				System.setProperty("https.protocols", "TLSv1.2");
				System.setProperty("http.agent", "");
				// System.setProperty("http.agent", "Chrome");

				SSLContext context = SSLContext.getInstance("TLSv1.2");
				context.init(null, null, null);
				SSLContext.setDefault(context);

				url = new URL(urlstring);
			} catch (Exception exc) {
				Dbutil.logger.error("Foute url: " + urlstring, exc);
			}
			if (url != null) {
				try {
					if (urlstring.startsWith("https://")) {
						System.out.print("******************************************* EDIT DATA FEED - HTTPS");
						HttpsURLConnection urlcons = (HttpsURLConnection) url.openConnection();
						urlcons.addRequestProperty("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

						// urlcons.setRequestProperty("Content-Language", "en-US");
						// urlcons.setUseCaches(false);
						// urlcons.setDoInput(true);
						// urlcons.setDoOutput(true);

						String type = urlcons.getContentType();
						String enc = urlcons.getContentEncoding();
						int index = 0;
						if (type != null)
							index = type.toLowerCase().indexOf(key);
						if (index > 0) {
							int indexend = type.indexOf(";", index + 8);
							if (indexend < index)
								indexend = type.length();
							encoding = type.substring(index + 8, indexend);
						}
						if (encoding.equals("") || encoding.equals("Unknown")) {
							if (enc != null) {
								encoding = enc;
							}
						}
						if (encoding.equals("") || encoding.equals("Unknown")) {
							byte[] bytes = new byte[5];
							urlcons.getInputStream().read(bytes, 0, 4);
							if (bytes[0] == -2 && bytes[1] == -1) {
								encoding = "UTF-16";
							} else if (bytes[0] == -1 && bytes[1] == -2) {
								encoding = "UTF-16";
							} else if (bytes[0] == 0xef && bytes[1] == 0xbb && bytes[2] == 0xbf) {
								encoding = "UTF-8";
							}
						}
						if (encoding.equals("") || encoding.equals("Unknown")) {
							BufferedReader in = new BufferedReader(
									new InputStreamReader(urlcons.getInputStream(), "UTF-8"));
							if ((inputLine = in.readLine()) != null) {
								if (!Webroutines.getRegexPatternValue("encoding=\"([^\"]+)\"", inputLine).equals("")) {
									encoding = Webroutines.getRegexPatternValue("encoding=\"([^\"]+)\"", inputLine);
								} else {
								}
							}
							in = new BufferedReader(new InputStreamReader(urlcons.getInputStream(), "ISO-8859-1"));
							while (encoding.equals("") && (inputLine = in.readLine()) != null) {
								if (inputLine.contains("Ãƒ"))
									encoding = "UTF-8";
							}
							if (encoding.equals("") || encoding.equals("Unknown"))
								encoding = "ISO-8859-1";
						}
						urlcons.disconnect();
					} else if (urlstring.startsWith("http://")) {
						System.out.print("******************************************* EDIT DATA FEED - HTTP");
						urlcon = (HttpURLConnection) url.openConnection();
						urlcon.addRequestProperty("User-Agent",
								"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
						// urlcons.setRequestProperty("Content-Language", "en-US");
						// urlcons.setUseCaches(false);
						// urlcons.setDoInput(true);
						// urlcons.setDoOutput(true);

						String type = urlcon.getContentType();
						String enc = urlcon.getContentEncoding();
						int index = 0;
						if (type != null) {
							index = type.toLowerCase().indexOf(key);
						}
						if (index > 0) {
							int indexend = type.indexOf(";", index + 8);
							if (indexend < index) {
								indexend = type.length();
							}
							encoding = type.substring(index + 8, indexend);
						}
						if (encoding.equals("") || encoding.equals("Unknown")) {
							if (enc != null) {
								encoding = enc;
							}
						}
						if (encoding.equals("") || encoding.equals("Unknown")) {
							byte[] bytes = new byte[5];
							urlcon.getInputStream().read(bytes, 0, 4);
							if (bytes[0] == -2 && bytes[1] == -1) {
								encoding = "UTF-16";
							} else if (bytes[0] == -1 && bytes[1] == -2) {
								encoding = "UTF-16";
							} else if (bytes[0] == 0xef && bytes[1] == 0xbb && bytes[2] == 0xbf) {
								encoding = "UTF-8";
							}
						}
						if (encoding.equals("") || encoding.equals("Unknown")) {
							BufferedReader in = new BufferedReader(
									new InputStreamReader(urlcon.getInputStream(), "UTF-8"));
							if ((inputLine = in.readLine()) != null) {
								if (!Webroutines.getRegexPatternValue("encoding=\"([^\"]+)\"", inputLine).equals("")) {
									encoding = Webroutines.getRegexPatternValue("encoding=\"([^\"]+)\"", inputLine);
								} else {

								}
							}
							in = new BufferedReader(new InputStreamReader(urlcon.getInputStream(), "ISO-8859-1"));
							while (encoding.equals("") && (inputLine = in.readLine()) != null) {
								if (inputLine.contains("Ãƒ")) {
									encoding = "UTF-8";
								}
							}
							if (encoding.equals("") || encoding.equals("Unknown")) {
								encoding = "ISO-8859-1";
							}
						}
						urlcon.disconnect();
					}
					/*
					 * 
					 * if ((len = page.length()) >0) {
					 * 
					 * int p1 = page.indexOf(key); if (p1 >= 0) { int p2 = p1 + key.length(); p1 +=
					 * key.length(); if (page.charAt(p1) == '\'' || page.charAt(p1) == '"') { p1++;
					 * } while (p2 < page.length() && "'\" >;,.".indexOf(page.charAt(p2)) < 0) {
					 * p2++; } if (p2 <= page.length()) { encoding = page.substring(p1, p2); } } }
					 */
				} catch (IOException e) {
					Dbutil.logger.warn("Fout in url connectie met url " + urlstring, e);
				}
			}
		}

		return encoding;
	}

	public static ArrayList<String> ScrapeUrl(String Page, ArrayList<UrlSpider> Regex, String parentUrl, String Baseurl,
			String Shopid, String postdata, String shopurl, String type) {

		int i;
		Pattern pattern;
		Matcher matcher;
		String Filter;
		// int positionoflastslash;
		String Urlfound;
		String Source;
		// ResultSet rs;
		Connection con = Dbutil.openNewConnection();
		ArrayList<String> ScrapedUrls = new ArrayList<String>(0);

		try {
			for (UrlSpider spider : Regex) {
				if (spider.spidertype.equals("normal") && (!spider.onelevel || type.equals("Master"))) {
					pattern = Pattern.compile(spider.regex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
					matcher = pattern.matcher(Page);
					Filter = spider.filter;
					if (Filter == null) {
						Filter = "";
					}
					// Dbutil.logger.debug("Regex"+Regex);
					while (matcher.find()) {
						Urlfound = matcher.group(1);
						Source = matcher.group(0);
						// Loop over the shop specific replacements
						i = 0;

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
						// Do some standard replacements as we want some useless things like session ID
						// filtered out
						Urlfound = TableScraper.cleanUrl(Urlfound);

						if (!Urlfound.equals("") && !Urlfound.equals("/")) {

							Urlfound = padUrl(Urlfound, parentUrl, Baseurl, shopurl);
							Urlfound = Urlfound.replaceAll("/\\./", "/"); // Urls with xyz/./abc
							Urlfound = Urlfound.replaceAll("/[^\\./\"\']*/\\.\\./", "/"); // Urls with xyz/../abc
							// unescape it
							Urlfound = Spider.unescape(Urlfound);
							// now store it if it aint empty (could be url from other site)
							if (!Urlfound.equals("") && !Urlfound.equals("/")) {
								ScrapedUrls.add(Urlfound);
								ScrapedUrls.add(Source);
								ScrapedUrls.add((Urlfound + postdata).hashCode() + "");

							}
						}

					}
				}
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem in scrapeurl: ", exc);
		}
		Dbutil.closeConnection(con);
		pattern = null;
		matcher = null;
		Filter = null;
		Urlfound = null;
		// rs = null;
		return ScrapedUrls;
	}

	public static ArrayList<String> ScrapeASPXUrl(String Page, ArrayList<UrlSpider> Regex, String parentUrl,
			String parentPostdata, String Baseurl, String Shopid, String shopurl) {
		Aspxprocessor aspx;
		ArrayList<String> ScrapedUrls = new ArrayList<String>(0);
		Pattern pattern;
		Matcher matcher;
		// int positionoflastslash;
		String Urlfound;
		String postdata;
		String Source;
		// ResultSet rs;
		Connection con = Dbutil.openNewConnection();
		String action = "";
		String inputregex = "<input((?: *\\w+=[\"'][^>]*[\"'])+) */*>";
		String postbackregex = "javascript:__(doPostBack)\\('([^']+)','([^']*)'\\)";
		String Filter;
		String formname = "";
		for (UrlSpider spider : Regex) {
			if (spider.spidertype.equals("aspx")) {

				aspx = new Aspxprocessor();
				Filter = spider.filter;
				if (Filter == null) {
					Filter = "";
				}
				if (Filter.contains("Page:mandatory")) { // only allow the next page to prevent circular spidering
					int currentpage = 1;
					try {
						currentpage = Integer
								.parseInt(Webroutines.getRegexPatternValue("Page(?:\\$|%24)(\\d+)", parentPostdata));
					} catch (Exception e) {
					}
					postbackregex = "javascript:__(doPostBack)\\('([^']+)','([^']*Page\\$" + (currentpage + 1)
							+ ")'\\)";
					// Dbutil.logger.info(postbackregex);
				}
				aspx.Filter = Filter;
				aspx.parenturl = parentUrl;
				formname = spider.regex;
				if (formname == null) {
					formname = "";
				}

				if (formname.equals("")) {
					Dbutil.logger.error("Aspx form name is empty");
				} else {
					String actionregex = "<form[^>]*? name=['\"]" + formname + "['\"][^>]*? action=['\"]([^'\"]+)['\"]";
					String formregex = "<form[^>]*? name=['\"]" + formname + "['\"](.*?)</form";
					try {
						pattern = Pattern.compile(formregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
						matcher = pattern.matcher(Page);
						if (matcher.find()) {
							String formcontent = matcher.group(0);
							pattern = Pattern.compile(inputregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
							matcher = pattern.matcher(formcontent);
							while (matcher.find()) {
								// String input = matcher.group(0);
								String name = Webroutines.getRegexPatternValue("name=['\"]([^'\"]+)['\"]",
										matcher.group(0));
								String type = Webroutines.getRegexPatternValue("type=['\"]([^'\"]+)['\"]",
										matcher.group(0));
								String value = Webroutines.getRegexPatternValue("value=['\"]([^'\"]*)['\"]",
										matcher.group(0));
								String checked = Webroutines.getRegexPatternValue("checked=['\"]([^'\"]+)['\"]",
										matcher.group(0));
								if (type.equalsIgnoreCase("checkbox") && checked.equalsIgnoreCase("checked")
										&& value.equals(""))
									value = "on";
								ArrayList<String> postvaluepair = new ArrayList<String>();
								postvaluepair.add(type);
								postvaluepair.add(name);
								postvaluepair.add(value);
								if (!type.equalsIgnoreCase("submit"))
									aspx.postvalues.add(postvaluepair);
							}
							pattern = Pattern.compile(actionregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
							matcher = pattern.matcher(Page);
							if (matcher.find()) {
								action = matcher.group(1);
							}
							aspx.action = action;
							pattern = Pattern.compile(postbackregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
							matcher = pattern.matcher(Page);
							// if (Filter == null) {
							// Filter = "";
							// }
							while (matcher.find()) {
								ArrayList<String> actionvaluepair = new ArrayList<String>();
								actionvaluepair.add(matcher.group(1));
								actionvaluepair.add(matcher.group(2));
								actionvaluepair.add(matcher.group(3));
								aspx.actions.add(actionvaluepair);
							}

							ArrayList<ArrayList<String>> urlsfound = aspx.getUrls();
							for (int i = 0; i < urlsfound.size(); i++) {
								Urlfound = urlsfound.get(i).get(0);
								postdata = urlsfound.get(i).get(1);
								Source = parentUrl;
								// Loop over the shop specific replacements

								// Do some standard replacements as we want some useless things like session ID
								// filtered out
								Urlfound = Urlfound.replaceAll("&amp;", "&");
								Urlfound = Urlfound.replaceAll("PHPSESSID=[0123456789abcdefABCDEF]*", "");
								Urlfound = Urlfound.replaceAll("&&", "&");
								Urlfound = Urlfound.replaceAll("&&", "&");
								Urlfound = Urlfound.replaceAll("&&", "&");
								Urlfound = Urlfound.replaceAll(".*[Mm]ailto.*", "");
								Urlfound = Urlfound.replaceAll("file://.*", "");

								if (!Urlfound.equals("") && !Urlfound.equals("/")) {

									Urlfound = padUrl(Urlfound, parentUrl, Baseurl, shopurl);
									Urlfound = Urlfound.replaceAll("/\\./", "/"); // Urls with xyz/./abc
									Urlfound = Urlfound.replaceAll("/[^\\./\"\']*/\\.\\./", "/"); // Urls with
																									// xyz/../abc
									// unescape it
									Urlfound = Spider.unescape(Urlfound);
									// now store it if it aint empty (could be url from other site)
									if (!Urlfound.equals("") && !Urlfound.equals("/") && !postdata.equals("")) {
										ScrapedUrls.add(Urlfound);
										ScrapedUrls.add(Source);
										ScrapedUrls.add(postdata);
										ScrapedUrls.add("");
										ScrapedUrls.add((Urlfound + postdata).hashCode() + "");

									}
								}

							}
						}

					} catch (Exception exc) {
						Dbutil.logger.error("Problem in scrapeurl: ", exc);
					}
				}
			}
		}
		Dbutil.closeConnection(con);
		pattern = null;
		matcher = null;
		Filter = null;
		Urlfound = null;
		// rs = null;

		return ScrapedUrls;
	}

	public static ArrayList<String> OLDScrapeASPXUrl(String Page, ArrayList<String> Regex, String parentUrl,
			String Baseurl, String Shopid, String postdata, String shopurl) {

		ArrayList<String> ScrapedUrls = new ArrayList<String>(0);
		int i;
		Pattern pattern;
		Matcher matcher;
		// int positionoflastslash;
		String Urlfound;
		String Source;
		// ResultSet rs;
		Connection con = Dbutil.openNewConnection();
		String action = "";
		// String inputregex = "<input [^>]*?name=(?:\"|')([^\"']+)(\"|')
		// value=(?:\"|')([^\"']+)(\"|')";

		String __EVENTTARGET = "";
		String __EVENTARGUMENT = "";
		String __LASTFOCUS = "";
		String __VIEWSTATE = "";
		String Filter;
		String formname = "";
		for (int j = 0; j < Regex.size(); j = j + 3) {
			if (Regex.get(j + 2).equals("aspx")) {

				Filter = Regex.get(j + 1);
				if (Filter == null) {
					Filter = "";
				}
				formname = Regex.get(j);
				if (formname == null) {
					formname = "";
				}
				String viewstateregex = "name=\"__VIEWSTATE\"[^>]* value=(?:\"|')([^\"']+)(\"|')";
				String lastfocusregex = "name=\"__LASTFOCUS\"[^>]* value=(?:\"|')([^\"']+)(\"|')";
				String eventregex = "javascript:__doPostBack\\('([^']+)','([^']*)'\\)";
				String actionregex = "<form\\s+name=\"" + formname + "\"[^>]* action=\"([^'\"]+)\"";

				if (formname.equals("")) {
					Dbutil.logger.error("Aspx form name is empty");
				} else {

					try {
						pattern = Pattern.compile(viewstateregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
						matcher = pattern.matcher(Page);
						if (matcher.find()) {
							__VIEWSTATE = matcher.group(1);
						}
						pattern = Pattern.compile(lastfocusregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
						matcher = pattern.matcher(Page);
						if (matcher.find()) {
							__LASTFOCUS = matcher.group(1);
						}
						pattern = Pattern.compile(actionregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
						matcher = pattern.matcher(Page);
						if (matcher.find()) {
							action = matcher.group(1);
						}
						pattern = Pattern.compile(eventregex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
						matcher = pattern.matcher(Page);
						// if (Filter == null) {
						// Filter = "";
						// }
						while (matcher.find()) {
							__EVENTTARGET = matcher.group(1).replace("$", ":"); // we saw this happen in the javascipt
																				// of youngcharly.com
							__EVENTARGUMENT = matcher.group(2);
							postdata = "__EVENTTARGET=" + __EVENTTARGET + "&__EVENTARGUMENT=" + __EVENTARGUMENT
									+ "&__LASTFOCUS=" + __LASTFOCUS;
							postdata = postdata.replace("+", "%2B"); // Why??? Don't know, the sniffer shows that it
																		// happens
							postdata = postdata.replace(":", "%3A"); // Why??? Don't know, the sniffer shows that it
																		// happens
							__VIEWSTATE = __VIEWSTATE.replace("+", "%2B");
							__VIEWSTATE = __VIEWSTATE.replace("+", "%2B");
							// Maybe all postdata has to be URLencoded in the webpage post statement
							// instead...
							Urlfound = action;
							Source = parentUrl;
							// Loop over the shop specific replacements
							i = 0;

							for (i = 0; i < Filter.split(":").length; i = i + 2) {
								String Search = Filter.split(":")[i];
								String Replace = null;
								if (i == Filter.split(":").length - 1) {
									Replace = "";
								} else {
									Replace = Filter.split(":")[i + 1];
								}
								if (Replace.equals("colon"))
									Replace = ":";
								if (Replace.equals("mandatory")) {
									if (Webroutines.getRegexPatternValue("(" + Search + ")", postdata).equals(""))
										postdata = "";
								} else {
									postdata = postdata.replaceAll(Search, Replace);
								}
							}
							// Do some standard replacements as we want some useless things like session ID
							// filtered out
							Urlfound = Urlfound.replaceAll("&amp;", "&");
							Urlfound = Urlfound.replaceAll("PHPSESSID=[0123456789abcdefABCDEF]*", "");
							Urlfound = Urlfound.replaceAll("&&", "&");
							Urlfound = Urlfound.replaceAll("&&", "&");
							Urlfound = Urlfound.replaceAll("&&", "&");
							Urlfound = Urlfound.replaceAll(".*[Mm]ailto.*", "");
							Urlfound = Urlfound.replaceAll("file://.*", "");

							if (!Urlfound.equals("") && !Urlfound.equals("/")) {

								Urlfound = padUrl(Urlfound, parentUrl, Baseurl, shopurl);
								Urlfound = Urlfound.replaceAll("/\\./", "/"); // Urls with xyz/./abc
								Urlfound = Urlfound.replaceAll("/[^\\./\"\']*/\\.\\./", "/"); // Urls with xyz/../abc
								// unescape it
								Urlfound = Spider.unescape(Urlfound);
								// now store it if it aint empty (could be url from other site)
								if (!Urlfound.equals("") && !Urlfound.equals("/") && !postdata.equals("")) {
									ScrapedUrls.add(Urlfound);
									ScrapedUrls.add(Source);
									ScrapedUrls.add(postdata);
									ScrapedUrls.add(__VIEWSTATE);
									ScrapedUrls.add((Urlfound + postdata + __VIEWSTATE).hashCode() + "");

								}
							}

						}

					} catch (Exception exc) {
						Dbutil.logger.error("Problem in scrapeurl: ", exc);
					}
				}
			}
		}
		Dbutil.closeConnection(con);
		pattern = null;
		matcher = null;
		Filter = null;
		Urlfound = null;
		// rs = null;

		return ScrapedUrls;
	}

	public static String determineIssueDate(String url, String Page) {
		String parseddate = "0000-00-00";
		String date = "0000-00-00";
		if (url.contains("testshop")) {
			date = Webroutines.getRegexPatternValue("Issue: ([^ ]+) ", Page);
			parseddate = convertDate("dd-mm-yyyy", "yyyy-mm-dd", date);
		}
		if (url.toLowerCase().contains("erobertparker") || url.contains("cachedpages\\200")) {
			try {
				// int issue=Integer.parseInt(Webroutines.getRegexPatternValue("from WA
				// #(\\d+)'", Page));
				date = "01 "
						+ Webroutines.getRegexPatternValue("#\\d{1,3}<br />(\\w\\w\\w (?:19|20)\\d\\d)</span>", Page);
				if (date.length() == 11)
					parseddate = convertDate("dd MMM yyyy", "yyyy-MM-dd", date);
			} catch (Exception e) {
			}
		}
		if (url.toLowerCase().contains("winespectator") || url.contains("cachedpages\\204")) {
			try {
				parseddate = Configuration.wsissuedate;

			} catch (Exception e) {
			}
		}
		if (parseddate.equals(""))
			date = "0000-00-00";
		return parseddate;

	}

	public static int determineIssue(String url, String record, String Page) {
		int issue = 0;
		if (url.toLowerCase().contains("erobertparker") || url.contains("cachedpages\\200")) {
			try {
				issue = Integer.parseInt(Webroutines.getRegexPatternValue(" #(\\d+)<br", record));
				if (issue < 150) {
					// Dbutil.logger.info(issue);
				}
			} catch (Exception e) {
			}
		}
		if (url.toLowerCase().contains("winespectator") || url.contains("cachedpages\\204")) {
			try {
				issue = Integer.parseInt(Webroutines.getRegexPatternValue("&iss=(\\d+)", Page));
			} catch (Exception e) {
			}
		}
		return issue;

	}

	public static String convertDate(String formatold, String formatnew, String date) {
		String newdate = "0000-00-00";
		java.util.Date dtDate = new Date();

		SimpleDateFormat dold = new SimpleDateFormat(formatold, Locale.US);
		SimpleDateFormat dnew = new SimpleDateFormat(formatnew);
		try {
			dtDate = dold.parse(date);
			newdate = dnew.format(dtDate);
		} catch (ParseException e) {
			Dbutil.logger.warn(
					"Could not parse date " + date + " with formatold " + formatold + " and formatnew " + formatnew, e);
		}
		if (newdate == null || newdate.equals(""))
			newdate = "0000-00-00";
		return newdate;
	}

	public static String padUrl(String url, String parenturl, String baseurl, String shopurl) {
		int positionoflastslash;
		positionoflastslash = parenturl.lastIndexOf("/");
		if (positionoflastslash < 8) {
			parenturl = parenturl + "/";
			positionoflastslash = parenturl.length();
		}
		String paddedurl = "";
		// Make it a complete url
		if (url == null || url.startsWith("#"))
			return "";
		if (url.startsWith("?"))
			return parenturl.replaceAll("\\?.*$", "") + url;
		if (url.startsWith("/")) { // Url is from root, pad with base URL
			if (baseurl.endsWith("/")) {
				paddedurl = baseurl.substring(0, baseurl.length() - 1) + url;
			} else {
				paddedurl = baseurl + url;
			}
		} else { // Url is relative to the parent URL or could be an external link
			if (url.toLowerCase().startsWith(baseurl.toLowerCase())
					|| url.toLowerCase().startsWith(shopurl.toLowerCase())
					|| url.toLowerCase().startsWith("http://" + baseurl.substring(11).toLowerCase())) { // already a
																										// complete URL,
																										// maybe despite
																										// the www.
																										// prefix
				paddedurl = url;
			} else { // Looks like a relative url. Filter href=www.etc.com
				if (!url.startsWith("http")) {
					positionoflastslash = parenturl.lastIndexOf("/");
					if (positionoflastslash > -1) {
						paddedurl = parenturl.substring(0, positionoflastslash + 1) + url;

					} else { // no slash, so add it to the complete parent url
						paddedurl = parenturl + "/" + url;
					}

				} else {
					// Dbutil.logger.error("Could not pad url for url "+url+" and baseurl
					// "+baseurl);
					// paddedurl=baseurl;
				}
			}
		}
		return paddedurl;
	}

	public String getBaseUrl() {
		ResultSet rs = null;
		String Baseurl = "";
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("SELECT Baseurl " + "from " + auto + "shops where id='" + id + "';", con);
			if (rs.next())
				Baseurl = rs.getString("baseurl");
		} catch (Exception e) {
			Dbutil.logger.error("Could not find baseurl for shopid " + id, e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		return Baseurl;
	}

	public final void updateUrlStatusses(String From, String To) {
		if (To.equals("Delete")) {
			Dbutil.executeQuery("Delete from " + auto + "scrapelist " + "WHERE status like '%" + From + "%' "
					+ "AND URLType like '%Spidered%'" + "AND Shopid = '" + id + "';");

		} else {
			String type = Dbutil.readValueFromDB("Select * from scrapelist where shopid=" + id + " and urltype='File';",
					"urltype");
			if (type.equals("")) {
				// For a normal spider there are no records in the scrapelist with type=file
				Dbutil.executeQuery("update " + auto + "scrapelist " + "SET status='" + To + "' "
						+ "WHERE status like '%" + From + "%' " + "AND Shopid = '" + id + "';");
			} else {
				Dbutil.executeQuery("update " + auto + "scrapelist " + "SET status='Done' " + "WHERE status like '%"
						+ From + "%' " + "AND Shopid = '" + id + "'" + " AND urltype!='File';");
			}
		}
	}

	public final void updateUrlStatus(String urlrowid, String NewStatus) {
		try {
			Dbutil.executeQuery("update " + auto + "scrapelist " + "SET status='" + NewStatus + "' " + "WHERE id="
					+ urlrowid + ";");

		} catch (Exception e) {
			Dbutil.logger.error("Problem updating url with id " + urlrowid, e);
		}
	}

	public final void updateUrlWinesFound(String urlrowid, int winesFound) {
		try {
			Dbutil.executeQuery("update " + auto + "scrapelist " + "SET Winesfound='" + winesFound + "' " + "WHERE id="
					+ urlrowid + ";");

		} catch (Exception e) {
			Dbutil.logger.error("Problem:", e);
		}
	}

	public final boolean addUrl(ArrayList<String> Urlsfound, String Regex, String Headerregex, String Tablescraper,
			String Order, String Rowid, String Type, String postdata, String parenturl) {
		Connection con = Dbutil.openNewConnection();
		boolean succes = false;
		// int numberofurls = 0;
		int j = 0;
		if (postdata.startsWith("__EVENTTARGET"))
			postdata = ""; // When the parent url was an aspx page but this page is a normal url, we have
							// to clear the postdata to prevent errors
		String hash = "0";
		try {
			// Looping is different for normal and auto discovery:
			// autodiscovery has 2 series behind eachother with the urlsource as well
			for (j = 0; j < Urlsfound.size(); j = j + 3) {
				Dbutil.logger.debug("Found URL " + Urlsfound.get(j).toString());
				hash = (Urlsfound.get(j + 2));
				// rs=Dbutil.selectQuery("select * from scrapelist where url='"+Url+"';", con);
				// if (!rs.next()){
				if (Type.equals("Spidered") || Type.equals("File")) {
					Dbutil.executeQuery("Insert ignore into " + auto
							+ "scrapelist (Url, Regex, Headerregex, Tablescraper, Shopid, Scrapeorder, URLType, Status,postdata,parenturl,hashcode) "
							+ "values ('" + Spider.SQLEscape(Urlsfound.get(j).toString()) + "', '" + Regex + "', '"
							+ Headerregex + "', '" + Tablescraper + "', '" + id + "', '" + Order + "', '" + Type
							+ "', 'Ready', '" + postdata + "', '" + Spider.SQLEscape(parenturl) + "'," + hash + ");",
							con);

				} else if (Type.equals("Spideredwithsource")) { // Only used for auto discovery
					Dbutil.executeQuery("Insert ignore into " + auto
							+ "scrapelist (Url, Regex, Headerregex, Tablescraper, Shopid, Scrapeorder, URLType, Status,postdata,parenturl,urlsource) "
							+ "values ('" + Spider.SQLEscape(Urlsfound.get(j).toString()) + "', '" + Regex + "', '"
							+ Headerregex + "', '" + Tablescraper + "', '" + id + "', '" + Order + "', '" + Type
							+ "', 'Ready', '" + postdata + "', '" + Spider.SQLEscape(parenturl) + "','"
							+ Spider.SQLEscape(Urlsfound.get(j + 1).toString()) + "');", con);

				} else {
					Dbutil.logger.error("The wrong addurl was called with an url of type " + Type);
				}
				// }
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem in addurl. ", e);
		}
		Dbutil.closeConnection(con);
		return succes;
	}

	public final boolean addASPXUrl(ArrayList<String> Urlsfound, String Regex, String Headerregex, String Tablescraper,
			String Order, String Rowid, String Type, String postdata, String parenturl) {
		Connection con = Dbutil.openNewConnection();
		boolean succes = false;
		// int numberofurls = 0;
		int j = 0;
		// ResultSet rs;
		try {
			for (j = 0; j < Urlsfound.size(); j = j + 5) {
				Dbutil.logger.debug("Found URL " + Urlsfound.get(j).toString());
				// rs=Dbutil.selectQuery("select * from scrapelist where url='"+Url+"';", con);
				// if (!rs.next()){
				if (!Urlsfound.get(j + 2).equals("")) {
					if (Type.equals("Spidered")) {
						Dbutil.executeQuery("Insert ignore into " + auto
								+ "scrapelist (Url, Regex, Headerregex, Tablescraper, Shopid, Scrapeorder, URLType, Status,postdata,parenturl,hashcode) "
								+ "values ('" + Spider.SQLEscape(Urlsfound.get(j)) + "', '" + Regex + "', '"
								+ Headerregex + "', '" + Tablescraper + "', '" + id + "', '" + Order + "', '" + Type
								+ "', 'Ready', '" + Spider.SQLEscape(Urlsfound.get(j + 2)) + "', '"
								+ Spider.SQLEscape(parenturl) + "', " + Urlsfound.get(j + 4) + ");", con);

					} else {
						Dbutil.logger.error("The wrong addurl was called with an url of type " + Type);
					}
				}
				// }
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem in addASPXurl. ", e);
		}
		Dbutil.closeConnection(con);
		return succes;
	}

	// Only used to add url from web interface
	public final boolean addUrl(String Url, String Regex, String Headerregex, String Tablescraper, String Order,
			String Rowid, String Type, String postdata, String parenturl) {
		boolean succes = false;
		int i;
		// String encoding = null;
		// String completeUrl;
		// ResultSet rs;
		Connection con = Dbutil.openNewConnection();
		int hash = 0;
		// ResultSet rs;
		try {
			hash = (Url + postdata).hashCode();
			con.setAutoCommit(false);
			if (Type.equals("Spidered")) {
				// This should never be the case. This routine is only called to add master or
				// fixed URL's
				succes = false;

			} else {
				if (Rowid.equals("0")) {
					i = Dbutil.executeQuery("insert into  " + auto
							+ "scrapelist (Url, Regex, Headerregex, Tablescraper, Shopid, Scrapeorder, URLType, Status,postdata,hashcode) "
							+ "values ('" + Url + "', '" + Regex + "', '" + Headerregex + "', '" + Tablescraper + "', '"
							+ id + "', '" + Order + "', '" + Type + "', 'Ready', '" + postdata + "', '" + parenturl
							+ "'," + hash + ");");
					if (i != 0)
						succes = true;
				} else {
					i = Dbutil.executeQuery("update " + auto + "scrapelist set Url='" + Url + "', Regex='" + Regex
							+ "', Postdata='" + postdata + "',Headerregex='" + Headerregex + "', Tablescraper='"
							+ Tablescraper + "', Scrapeorder='" + Order + "', parenturl='" + parenturl + "',hash="
							+ hash + " " + " where id=" + Rowid + ";");
					if (i != 0)
						succes = true;
				}
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem adding url.", e);
		}
		try {
			con.commit();
		} catch (Exception e) {
			Dbutil.logger.error("Could not commit connection while adding urls for shop " + id + ". ", e);
		}
		Dbutil.closeConnection(con);
		return succes;
	}

	public final String addSpiderRegex(String Regex, String Filter, String row, String ignorepagenotfound,
			String spidertype, String onelevel) {
		String succes = "";

		try {
			if (!row.equals("") && Integer.valueOf(row) > 0) {
				Dbutil.executeQuery("update LOW_PRIORITY " + auto + "spiderregex set regex='" + Regex + "', Filter='"
						+ Filter + "',onelevel=" + onelevel + " where " + "Shopid=" + id + " and id=" + row + ";");

			} else {
				Dbutil.executeQuery("Insert LOW_PRIORITY into " + auto
						+ "spiderregex (Shopid, Regex, Filter,spidertype,onelevel) " + "values ('" + id + "', '" + Regex
						+ "', '" + Filter + "', '" + spidertype + "'," + onelevel + ");");

			}
			Dbutil.executeQuery("Update LOW_PRIORITY " + auto + "shops set ignorepagenotfound='" + ignorepagenotfound
					+ "' where " + "id=" + id + ";");

		} catch (Exception e) {
			Dbutil.logger.error("Could not save spider regex: ", e);
			succes = e.toString();
		}
		if ("".equals(succes))
			Dbutil.executeQuery("update LOW_PRIORITY shopstats set lastchange=sysdate() where shopid=" + id + ";");

		return succes;
	}

	public void SpiderStarted() {
		Dbutil.executeQuery("update " + auto + "shops set lastsearchstarted=sysdate() where id = " + id + ";");
	}

	public void SpiderEnded(String id, boolean succes) {
		Dbutil.executeQuery("update " + auto + "shops set lastsearchended=sysdate() where id = " + id + ";");
	}

	public static String buildMatch(Matcher matcher, String buildString) {
		String resultString = "";
		String[] elements = buildString.split(";");
		for (int i = 0; i < elements.length; i++) {
			if (isInteger(elements[i])) {
				resultString = resultString + matcher.group(Integer.parseInt(elements[i]));
			} else {
				resultString = resultString + elements[i];
			}
			// System.out.print("BuildString: "+buildString+", i: "+i+", resultaat:
			// "+resultString);
		}
		// System.out.println(". Totaal Resultstring: "+resultString);
		resultString = unescape(resultString);
		return resultString;
	}

	public static String buildMatchtest(Matcher matcher, String buildString, String headerregex, String Page) {
		int start;
		String header = "";
		Pattern pattern;
		String resultString = "";
		String[] elements = buildString.split(";");
		String[] groupresults = new String[elements.length];
		// Read all groups and store them in groupresults

		for (int i = 0; i < elements.length; i++) {
			if (isInteger(elements[i])) {
				groupresults[i] = matcher.group(Integer.parseInt(elements[i]));
			} else {
				groupresults[i] = elements[i];
			}
			// System.out.print("BuildString: "+buildString+", i: "+i+", resultaat:
			// "+resultString);
		}
		if (!headerregex.equals("")) {
			// Now find the last header regex before the match
			start = matcher.start();
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

		for (int i = 0; i < elements.length; i++) {
			if (!elements[i].equals("H")) {
				resultString = resultString + groupresults[i];
			} else {
				resultString = resultString + header;
			}
			// System.out.print("BuildString: "+buildString+", i: "+i+", resultaat:
			// "+resultString);
		}

		// System.out.println(". Totaal Resultstring: "+resultString);
		resultString = unescape(resultString);
		return resultString;
	}

	public static class UrlSpider {
		public String regex;
		public String filter;
		public String spidertype;
		public boolean onelevel;
	}

	public ArrayList<UrlSpider> getUrlRegex() {
		ArrayList<UrlSpider> urlregex = new ArrayList<UrlSpider>();
		Connection urlregexcon = Dbutil.openNewConnection();
		ResultSet rs = null;
		try {
			// Get a list of Regex's from the Spiderregex table
			rs = Dbutil.selectQuery("SELECT * from " + auto + "spiderregex where Shopid = '" + id + "';", urlregexcon);
			while (rs.next()) {
				UrlSpider u = new UrlSpider();
				u.regex = (rs.getString("regex"));
				u.filter = (rs.getString("filter"));
				u.spidertype = (rs.getString("spidertype"));
				u.onelevel = rs.getBoolean("onelevel");
				urlregex.add(u);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not get URL regex", e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(urlregexcon);
		rs = null;
		return urlregex;
	}

	public ArrayList<ArrayList<String>> getScrapeList(String Status) {
		Connection con = Dbutil.openNewConnection();
		ArrayList<ArrayList<String>> urllist = new ArrayList<ArrayList<String>>(0);
		ResultSet rs = null;
		int i = 0;
		try {
			// Get a list of Url's and Regex's from the scrapelist table
			rs = Dbutil.selectQuery("SELECT * from " + auto + "scrapelist where Shopid = '" + id
					+ "' AND Status like '%" + Status + "' order by id;", con);
			while (rs.next()) {
				urllist.add(new ArrayList<String>(0));
				urllist.get(i).add(rs.getString("Url"));
				String postdata = rs.getString("Postdata");
				if (postdata == null)
					postdata = "";
				urllist.get(i).add(postdata);
				urllist.get(i).add(rs.getString("UrlType"));
				urllist.get(i).add(rs.getString("Regex"));
				urllist.get(i).add(rs.getString("Headerregex"));
				urllist.get(i).add(rs.getString("Scrapeorder"));
				urllist.get(i).add(rs.getString("Tablescraper"));
				urllist.get(i).add(rs.getString("Parenturl"));
				urllist.get(i).add("");
				urllist.get(i).add(rs.getString("id"));
				i++;
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem in getScrapeList.", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return urllist;
	}

	public static boolean isInteger(String s) {
		try {
			Long.parseLong(s.trim());
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public String stripURL(String urlstring) {
		String baseurl = "";
		ResultSet rs;
		Connection con = Dbutil.openNewConnection();
		try {
			rs = Dbutil.selectQuery("Select baseurl from " + auto + "shops where id = " + id + ";", con);
			rs.next();
			baseurl = rs.getString("baseurl");
		} catch (SQLException e) {
			Dbutil.logger.error("Kon baseurl niet ophalen uit shops. " + e);
		}
		// System.out.println(urlstring+" "+ baseurl);
		Dbutil.closeConnection(con);
		if (urlstring.startsWith(baseurl)) {
			urlstring = urlstring.substring(baseurl.length());
			// System.out.println(urlstring+" "+ baseurl+ baseurl.length());
		}

		return urlstring;
	}

	public static String replaceString(String value, String replace, String replaceWith) {
		if (value != null) {
			StringBuffer result = new StringBuffer(value);
			int idx = value.indexOf(replace);
			while (idx != -1) {
				result.replace(idx, (idx + 1), replaceWith);
				idx = result.toString().indexOf(replace, idx + 2);
			}
			return result.toString();
		} else
			return "";
	}

	public static String SQLEscape(String value) {
		if (value != null) {
			value = Spider.replaceString(value, "\\", "\\\\");
			value = Spider.replaceString(value, "'", "\\'");
		}
		return value;
	}

	private final static String[][] ENTITIES = { { "&lt;", "<" }, { "&gt;", ">" }, { "&amp;", "&" }, { "&quot;", "\"" },
			{ "&acute;", "'" }, { "&middot;", "Â·" }, { "&deg;", "Â°" }, { "&ntilde;", "Ã±" }, { "&Ntilde;", "Ã‘" },
			{ "&ordm;", "Âº" }, { "&aacute;", "Ã¡" }, { "&oacute;", "Ã³" }, { "&uacute;", "Ãº" }, { "&iacute;", "Ã­" },
			{ "&Iacute;", "Ã�" }, { "&icirc;", "Ã®" }, { "&Icirc;", "ÃŽ" }, { "&deg;", "Â°" }, { "&agrave;", "Ã " },
			{ "&Agrave;", "Ã€" }, { "&acirc;", "Ã¢" }, { "&auml;", "Ã¤" }, { "&atilde;", "Ã£" }, { "&Auml;", "Ã„" },
			{ "&Acirc;", "Ã‚" }, { "&aring;", "Ã¥" }, { "&Aring;", "Ã…" }, { "&aelig;", "Ã¦" }, { "&AElig;", "Ã†" },
			{ "&ccedil;", "Ã§" }, { "&Ccedil;", "Ã‡" }, { "&eacute;", "Ã©" }, { "&Eacute;", "Ã‰" },
			{ "&egrave;", "Ã¨" }, { "&Egrave;", "Ãˆ" }, { "&ecirc;", "Ãª" }, { "&Ecirc;", "ÃŠ" }, { "&euml;", "Ã«" },
			{ "&Euml;", "Ã‹" }, { "&iuml;", "Ã¯" }, { "&Iuml;", "Ã�" }, { "&Iacute;", "Ã�" }, { "&icirc;", "Ã®" },
			{ "&ocirc;", "Ã´" }, { "&ograve;", "Ã²" }, { "&Ocirc;", "Ã”" }, { "&Oacute;", "Ã“" }, { "&ouml;", "Ã¶" },
			{ "&Ouml;", "Ã–" }, { "&oslash;", "Ã¸" }, { "&Oslash;", "Ã˜" }, { "&szlig;", "ÃŸ" }, { "&ugrave;", "Ã¹" },
			{ "&Ugrave;", "Ã™" }, { "&Uacute;", "Ãš" }, { "&ucirc;", "Ã»" }, { "&Ucirc;", "Ã›" }, { "&uuml;", "Ã¼" },
			{ "&Uuml;", "Ãœ" }, { "&yacute;", "Ã½" }, { "&nbsp;", " " }, { "&#232;", "Ã¨" }, { "&#233;", "Ã©" },
			{ "&#039;", "'" }, { "&reg;", "\u00a9" }, { "&copy;", "\u00ae" }, { "&lsquo;", "â€˜" },
			{ "&rsquo;", "â€™" }, { "&raquo;", "Â»" }, { "&rsaquo;", "â€º" }, { "&laquo;", "Â«" },
			{ "&euro;", "\u20a0" } };

	public static String unescape(String source) {
		if (source == null || source.indexOf('&') == -1) {
			// no entities
			return source;
		}
		int len = source.length();
		StringBuffer result = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			int startPos = source.indexOf('&', i);

			if (startPos != i) {
				// we skipped some chars, append them to result
				result.append(source.substring(i, startPos == -1 ? len : startPos));
			}

			if (startPos == -1) {
				// no more entities
				break;
			}

			int endPos = source.indexOf(';', startPos);
			if (endPos == -1) {
				// broken entity
				result.append(source.substring(startPos));
				break;
			}

			String entity = source.substring(startPos, endPos + 1);
			int num = 0;
			if (entity.startsWith("&#")) {
				try {
					num = Integer.parseInt(source.substring(startPos + 2, endPos));
				} catch (Exception e) {
				}
			}
			if (num > 0) {
				result.append((char) num);
			} else {
				int p = 0;
				for (; p < ENTITIES.length; p++) {
					if (entity.equals(ENTITIES[p][0])) {
						result.append(ENTITIES[p][1]);
						break;
					}
				}
				if (p >= ENTITIES.length) {
					// no entity replacement found, leave as-is
					result.append(entity);
				}
			}
			// skip ahead a littlebit
			i = endPos;
		}

		return StringEscapeUtils.unescapeHtml(result.toString());
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

	public static String getShopName(String shopid) {
		String shopname = "";
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = Dbutil.selectQuery("Select shopname from shops where id=" + shopid + ";", con);
		try {
			rs.next();
			shopname = rs.getString("shopname");
		} catch (Exception exc) {
			Dbutil.logger.error("Could not find shopname for shop " + shopid, exc);
		}
		Dbutil.closeConnection(con);
		return shopname;
	}

	public String getShopName() {
		String shopname = "";
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = Dbutil.selectQuery("Select shopname from " + auto + "shops where id=" + id + ";", con);
		try {
			rs.next();
			shopname = rs.getString("shopname");
		} catch (Exception exc) {
			Dbutil.logger.error("Could not find shopname for shop " + id, exc);
		}
		Dbutil.closeConnection(con);
		return shopname;
	}

	public static void main(String[] args) {
		try {
			System.setProperty("http.agent", "");
			System.setProperty("https.protocols", "TLSv1.2");

			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, null, null);
			SSLContext.setDefault(context);

			URL url = new URL(
					"https://winehouseportugal.com/modules/xmlfeeds/api/xml.php?id=5&affiliate=affiliate_name");
			HttpsURLConnection urlcon = (HttpsURLConnection) url.openConnection();
			urlcon.addRequestProperty("User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
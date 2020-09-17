package com.freewinesearcher.common;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.CurrencyClient;
import com.freewinesearcher.batch.Emailer;
import com.freewinesearcher.batch.FileSpider;
import com.freewinesearcher.batch.MailProcessor;
import com.freewinesearcher.batch.Shopstats;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.sitescrapers.SuckWineSearcher;
import com.freewinesearcher.batch.sms.Sms;
import com.freewinesearcher.obsolete.Autospider;
import com.freewinesearcher.online.ShopAdvice;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;
import com.freewinesearcher.online.WineLibraryTV;
import com.freewinesearcher.online.housekeeping.CacheManager;
import com.searchasaservice.ai.Recognizer;

public class Wijnzoeker {
	static {
		new Dbutil();
	};

	public static String version = getVersion();
	public static boolean debug = false;
	// public static Properties FWSConfig = new Properties();
	public static String serverrole = Configuration.serverrole;
	public static int shoptodebug = Configuration.shoptodebug;
	public static int maxDataSourceConnections = 0;
	public static String auto = "";
	public static int totalimagesize = 0;
	public static int type = 1; // type 1 is normal shop, type 2 is only site with ratings
	public static boolean muststopnow = false;
	public static String stopautoshop = "";
	// public static String
	// GoogleApiKey="ABQIAAAAuPfgtY5yGQowyqWw-A_zlhQhPudxHcpaGNIyfv4t2H6-759f0BR-IPeybPo9uisRsQ0BIZ7-8K5V_Q";
	// public static String
	// GoogleApiKeyDev="ABQIAAAAuPfgtY5yGQowyqWw-A_zlhRoSqbKJiQQxlp8TJqrchpUmVcCHBQsCH801p-acuBEm05F20CusfgLVw";
	// public static String[] typevalues={"Red (dry)","White (dry)","Ros�
	// (dry)","Red - Fortified","Red - Sparkling","Red - Sweet/Dessert","White -
	// Fortified","White - Off-dry","White - Sparkling","White -
	// Sweet/Dessert","Ros� - Sparkling","Ros� - Sweet/Dessert"};
	public int tenantid = 1;
	static {
		Map<String, String> envs = System.getenv();
		if (envs.get("isSetupOnlineProcesses") == null || envs.get("isSetupOnlineProcesses").equals("true"))
			setupOnlineProcesses();
	}
	static {
		// Exception e=new Exception();
		// Dbutil.logger.info("Stack",e);
		Dbutil.logger.info("Current build is " + version);

	}

	public final static void main(String[] args) throws Exception {
		execute(args);
	}

	public static void execute(String[] args) {
		if (args.length == 0) {
			Dbutil.logger.info("Starting batch.");
			if (Configuration.FWSConfig.getProperty("updatecurrency", "TRUE").equals("TRUE")) {
				Dbutil.logger.info("Updating Exchange rates.");
				CurrencyClient.updateRates2();
				Dbutil.logger.info("Finished updating Exchange rates.");
				Dbutil.logger.info("Updating prices in euro");
				CurrencyClient.recalculateAllPrices();
				Dbutil.logger.info("Finished updating prices in euro");
			}
			Wijnzoeker wijnzoeker = new Wijnzoeker();
			shoptodebug = 0;
			Webroutines.checkLinksBack();

			if (Configuration.FWSConfig.getProperty("updatewines", "FALSE").equals("TRUE")) {
				MailProcessor.receiveUploadMessages();
				Wijnzoeker.auto = "";
				Wijnzoeker.type = 1; // Normal shops
				Dbutil.logger.info("Going to Update sites");
				wijnzoeker.updateSites();
				Dbutil.logger.info("Stoped Updating sites.");
				Dbutil.logger.info("Going to remove all old wines.");
				Spider.removeAllOldWines();
				Dbutil.logger.info("removing all old wines finished.");
				MailProcessor.notifyToUpload();
				Dbutil.logger.info("MailProcessor.notifyToUpload(); finished.");
				if (Configuration.FWSConfig.getProperty("updatewines", "FALSE").equals("TRUE")) {
					// Send a mail with the status
					mailBatchStatus();
					Dbutil.logger.info("mailBatchStatus(); finished.");
				}
			}
			wijnzoeker = null;
			if (Configuration.FWSConfig.getProperty("updatewltv", "FALSE").equals("TRUE")) {
				WineLibraryTV.update();
				Dbutil.logger.info("WineLibraryTV.update(); finished.");
			}
			// if
			// (Configuration.FWSConfig.getProperty("updateregions","FALSE").equals("TRUE"))
			// {
			// Region.updateParent();
			// Region.rebuildTree(1, 1);
			// Region.updateRegionsinKnownWines();
			// }
			if (Configuration.newrecognitionsystem) {
				Dbutil.logger.info("newrecognitionsystem is true.");
				if (Configuration.FWSConfig.getProperty("knownwines", "FALSE").equals("TRUE")) {
					Dbutil.logger.info("Configuration.FWSConfig.getProperty(knownwines,FALSE).equals(TRUE) is true.");
					if (Configuration.FWSConfig.getProperty("knownwinesmethod").equals("PARTIAL")) {
						Dbutil.logger
								.info("Configuration.FWSConfig.getProperty(knownwinesmethod).equals(PARTIAL) is true.");
						Recognizer.recognizeKnownWines(Configuration.partialknownwinesperiod, false);
						Dbutil.logger.info(
								"Recognizer.recognizeKnownWines(Configuration.partialknownwinesperiod,false) finished.");
					} else {
						Dbutil.logger.info(
								"Configuration.FWSConfig.getProperty(knownwinesmethod).equals(PARTIAL) is false.");
						Recognizer.recognizeKnownWines(0, true);
						Dbutil.logger.info("Recognizer.recognizeKnownWines(0,true); finished.");
					}
					if (Configuration.FWSConfig.getProperty("twitwotd", "FALSE").equals("TRUE")) {
						Dbutil.logger
								.info("Configuration.FWSConfig.getProperty(twitwotd,FALSE).equals(TRUE) is false.");
						Twit.Wotd();
						Dbutil.logger.info("Twit.Wotd(); finished.");
					}

					if (Configuration.FWSConfig.getProperty("analyzeratedwines", "FALSE").equals("TRUE")) {
						Dbutil.logger.info(
								"Configuration.FWSConfig.getProperty(analyzeratedwines,FALSE).equals(TRUE) is false.");
						Recognizer.recognizeRatedWines();
						Dbutil.logger.info("Recognizer.recognizeRatedWines(); finished.");
					}

				} else {
					if (Configuration.FWSConfig.getProperty("twitwotd", "FALSE").equals("TRUE")) {
						Dbutil.logger
								.info("Configuration.FWSConfig.getProperty(twitwotd,FALSE).equals(TRUE) is false.");
						Twit.Wotd();
						Dbutil.logger.info("Twit.Wotd(); finished.");
					}

				}
			} else {
				Dbutil.logger.info("newrecognitionsystem is false.");
				if (Configuration.FWSConfig.getProperty("knownwines", "FALSE").equals("TRUE")) {
					Dbutil.logger.info("newrecognitionsystem is false. 1");
					if (Configuration.FWSConfig.getProperty("knownwinesmethod").equals("PARTIAL")) {
						Dbutil.logger.info("newrecognitionsystem is false. 2");
						new Knownwines(1);
						Dbutil.logger.info("newrecognitionsystem is false. 3");
						Knownwines.updateNumberOfWines();
						Dbutil.logger.info("newrecognitionsystem is false. 4");
					} else {
						Dbutil.logger.info("newrecognitionsystem is false. 5");
						new Knownwines(0);
						Dbutil.logger.info("newrecognitionsystem is false. 6");
						Knownwines.updateNumberOfWines();
						Dbutil.logger.info("newrecognitionsystem is false. 7");
					}
					if (Configuration.FWSConfig.getProperty("analyzeratedwines", "FALSE").equals("TRUE")) {
						Dbutil.logger.info("newrecognitionsystem is false. 8");
						Winerating.refreshRatedWines();
						Dbutil.logger.info("newrecognitionsystem is false. 9");
						// Recognizer.recognizeRatedWines();
					}

					// Recognizer.recognizeKnownWines();
				}
			}
			// Dbutil.renewTable("materializedadvice");
			Dbutil.logger.info("ShopAdvice.refreshBestPrices(); start");
			ShopAdvice.refreshBestPrices();
			Dbutil.logger.info("ShopAdvice.refreshBestPrices(); end");

			if (Configuration.FWSConfig.getProperty("analyzetips", "FALSE").equals("TRUE")) {
				Wineset.analyzeTips();
			}
			if (Configuration.FWSConfig.getProperty("mailupdates", "FALSE").equals("TRUE")) {
				mailUpdates();
			}
			if (Configuration.FWSConfig.getProperty("generateknownwinesdoubles", "FALSE").equals("TRUE")) {
				// Knownwines.generateKnownwinesPreciseDoubles();
			}

			if (Configuration.FWSConfig.getProperty("updatecoordinates", "FALSE").equals("TRUE")) {
				Coordinates.getCoordinatesShops();
			}

			Shopstats.updateStats();
			Dbutil.executeQuery("update config set value='true' where configkey='finishedbatch';"); // Signal to refresh
																									// cache in web
																									// server
			Dbutil.logger.info("Finished batch.");

		} else {
			// debug=false;
			if (args[0].equals("suckwinesearcher")) {
				Wijnzoeker wijnzoeker = new Wijnzoeker();
				wijnzoeker.suckWineSearcher();
			}
			if (args[0].equals("completerecognition")) {
				// Wijnzoeker wijnzoeker=new Wijnzoeker();
				Recognizer.completeRecognition();
			}
			if (args[0].equals("robertparker") && args.length > 1) {
				String issue = "";
				try {
					for (int i = 1; i < args.length; i++) {
						issue = args[i];
						int issuenumber = Integer.parseInt(issue);
						if (!Wijnzoeker.muststopnow && issuenumber > 0 && modifyParkerIssue(issuenumber)) {
							if (!Wijnzoeker.muststopnow) {
								Dbutil.logger.info("Starting to scrape RP issue " + issuenumber);
								Wijnzoeker wijnzoeker = new Wijnzoeker();
								shoptodebug = 200;
								Configuration.issuenumber = issuenumber;
								Wijnzoeker.auto = "";
								Wijnzoeker.type = 2; // rating
								Configuration.maxConcurrentThreads = 1;
								wijnzoeker.updateSites();
								if (i + 1 < args.length) {
									Thread.sleep(30 * 60 * 1000);
								}
								Dbutil.logger.info("Finishedscraping RP issue " + issuenumber);

							}
						} else {
							Dbutil.logger.error("Did not start RP run: could not update Url");
						}
					}
				} catch (Exception e) {
					Dbutil.logger.error("Received wrong issue number for RP run: " + issue);
				}
			}

			if (args[0].equals("robertparkerupdate")) {
				String issueregex = "#(\\d\\d\\d) -";
				int lastissue = 0;
				int thisissue;
				int newissue;
				Pattern pattern;
				Matcher matcher;

				try {
					// Determine highest issue in the directory
					for (File file : new File(Configuration.cachedpagesdir + System.getProperty("file.separator")
							+ "200" + System.getProperty("file.separator")).listFiles()) {
						thisissue = 0;
						try {
							thisissue = Integer.parseInt(file.getName());
						} catch (Exception e) {
						}
						if (lastissue < thisissue) {
							lastissue = thisissue;
						}
					}
					newissue = lastissue + 1;
					Webpage webpage = new Webpage();
					webpage.setCookie(Dbutil.readValueFromDB("select * from shops where id=200;", "cookie"));
					webpage.maxattempts = 1;
					webpage.urlstring = "http://www.erobertparker.com/members/home.aspx";
					webpage.readPage();
					pattern = Pattern.compile(issueregex);
					matcher = pattern.matcher(webpage.html);
					int latest = 0;
					if (matcher.find()) {
						try {
							latest = Integer.parseInt(matcher.group(1));
						} catch (Exception e) {
							Sms sms = new Sms(Configuration.gmailusername, Configuration.gmailpassword,
									"Problem scraping Robert Parker");
							sms.send();
						}
					} else {
						Dbutil.logger.error("Cannot find latest Parker issue on " + webpage.urlstring);
						Dbutil.logger.error(webpage.html);
					}
					if (newissue <= latest) {
						if (!Wijnzoeker.muststopnow && modifyParkerIssue(newissue)) {
							Dbutil.logger.info("Starting to scrape RP issue " + newissue);
							// Twit.twitter("Wine Advocate issue "+newissue+" has just been released.
							// http://www.erobertparker.com/members/search/issuesearch.asp#ISSUE"+newissue);
							Wijnzoeker wijnzoeker = new Wijnzoeker();
							shoptodebug = 200;
							Configuration.issuenumber = newissue;
							Wijnzoeker.auto = "";
							Wijnzoeker.type = 2; // rating
							Configuration.maxConcurrentThreads = 1;
							wijnzoeker.updateSites();
							Thread.sleep(30 * 60 * 1000);
							Dbutil.logger.info("Finished scraping RP issue " + newissue);
						}
					} else {
						Dbutil.logger.info("Looking for new RP issue " + newissue + ", but did not find it");
					}
				} catch (Exception e) {
					Dbutil.logger.error("Could not check for update on erobertparker.com: ", e);
				}
			}

			if (args[0].equals("robertparkerbatch") && args.length > 1) {
				int issuenumber = 0;
				try {
					int n = Integer.parseInt(args[1]);
					for (int i = 0; i < n; i++) {
						// Determine highest issue in the directory
						for (File file : new File(Configuration.cachedpagesdir + System.getProperty("file.separator")
								+ "200" + System.getProperty("file.separator")).listFiles()) {
							int thisissue = 0;
							try {
								thisissue = Integer.parseInt(file.getName());
							} catch (Exception e) {
							}
							if (issuenumber < thisissue) {
								issuenumber = thisissue;
							}
						}
						issuenumber = issuenumber + 1;
						if (!Wijnzoeker.muststopnow) {
							if (issuenumber > 175 && modifyParkerIssue(issuenumber)) {
								long wait = (long) (Math.random() * 1000 * 60 * 60 * 2);
								Dbutil.logger.info("Before starting daily RP batch I will sleep for "
										+ wait / (1000 * 60) + " mins.");
								Thread.sleep(wait);
								Dbutil.logger.info("Starting to scrape RP issue " + issuenumber);
								Wijnzoeker wijnzoeker = new Wijnzoeker();
								shoptodebug = 200;
								Configuration.issuenumber = issuenumber;
								Wijnzoeker.auto = "";
								Wijnzoeker.type = 2; // rating
								Configuration.maxConcurrentThreads = 1;
								wijnzoeker.updateSites();
								Dbutil.logger.info("Finished scraping RP issue " + issuenumber);
								if (i + 1 < n) {
									Dbutil.logger.info("Taking a power nap now.");
									Thread.sleep((long) ((20 + (Math.random() * 20)) * 60 * 1000));
								}
							} else {
								Dbutil.logger.error(
										"Did not start RP run: could not update Url. Issuenumber=" + issuenumber);
							}
						}
					}
				} catch (Exception e) {
					Dbutil.logger.error("Calculated wrong issue number for RP run: " + issuenumber, e);
					Dbutil.logger.error("Directory checked: " + Configuration.cachedpagesdir
							+ System.getProperty("file.separator") + "200" + System.getProperty("file.separator"));
				}
			}

			if (args[0].equals("robertparkerfromcache")) {
				new FileSpider(200);
			}

			/*
			 * if (args[0].equals("winespectator")&&args.length>1){ String issue=""; try {
			 * for (int i=1;i<args.length;i++){ issue=args[i]; int
			 * issuenumber=Integer.parseInt(issue); if
			 * (!Wijnzoeker.muststopnow&&issuenumber>0&&modifyWSIssue(issuenumber)){ if
			 * (!Wijnzoeker.muststopnow){
			 * Dbutil.logger.info("Starting to scrape WS issue "+issuenumber); Wijnzoeker
			 * wijnzoeker=new Wijnzoeker(); shoptodebug=204; Wijnzoeker.auto="";
			 * Wijnzoeker.type=2; //rating Wijnzoeker.maxConcurrentThreads=1;
			 * wijnzoeker.updateSites(); if (i+1<args.length){ Thread.sleep(30*60*1000); }
			 * Dbutil.logger.info("Finished scraping WS issue "+issuenumber);
			 * 
			 * } } else { Dbutil.logger.error("Did not start WS run: could not update Url");
			 * } } } catch (Exception e){
			 * Dbutil.logger.error("Received wrong issue number for WS run: "+issue); } }
			 * 
			 * if (args[0].equals("winespectatorbatch")&&args.length>1){ long
			 * wait=(long)(Math.random()*1000*60*60*2);
			 * Dbutil.logger.info("Before starting daily WS batch I will sleep for "+wait/(
			 * 1000*60)+" mins."); Thread.sleep(wait); int issuenumber=0; try { int
			 * n=Integer.parseInt(args[1]); for (int i=0;i<n;i++){ // Determine lowest issue
			 * in the directory for (File file:new
			 * File("C:\\cachedpages\\204\\").listFiles()){ int thisissue=0; try {
			 * thisissue=Integer.parseInt(file.getName()); } catch (Exception e) {} if
			 * (issuenumber==0||issuenumber>thisissue){ issuenumber=thisissue; } } if
			 * (issuenumber>1) issuenumber=issuenumber-1; if
			 * (!Wijnzoeker.muststopnow&&issuenumber>0&&modifyWSIssue(issuenumber)){ if
			 * (!Wijnzoeker.muststopnow){
			 * Dbutil.logger.info("Starting to scrape WS issue "+issuenumber); Wijnzoeker
			 * wijnzoeker=new Wijnzoeker(); shoptodebug=204; Wijnzoeker.auto="";
			 * Wijnzoeker.type=2; //rating Wijnzoeker.maxConcurrentThreads=1;
			 * wijnzoeker.updateSites();
			 * Dbutil.logger.info("Finished scraping WS issue "+issuenumber); if (i+1<n){
			 * Dbutil.logger.info("Taking a power nap now.");
			 * Thread.sleep((long)((20+(Math.random()*20))*60*1000)); }
			 * 
			 * 
			 * } } else { Dbutil.logger.error("Did not start WS run: could not update Url");
			 * } } } catch (Exception e){
			 * Dbutil.logger.error("Calculated wrong issue number for WS run: "+issuenumber)
			 * ; } }
			 */

			// WINE SPECTATOR - START HERE

			if (args[0].equals("winespectatorupdate")) {
				int issuenumber = 0;
				Pattern pattern = null;
				Matcher matcher = null;
				DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
				int backyear = 1;
				try {
					backyear = Integer.parseInt(args[1]);
				} catch (Exception e) {
					backyear = 1;
				}
				try {
					String newissuedate = "";
					Webpage webpage2 = new Webpage();
					webpage2.setCookie(Dbutil.readValueFromDB("select * from shops where id=204;", "cookie"));
					webpage2.maxattempts = 1;
					webpage2.urlstring = "http://www.winespectator.com/issue?year="
							+ (java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR)
									- backyear);
					Dbutil.logger.info("************* AAAAA INSIDE execute BACK YEAR=" + backyear
							+ "; WEBPAGE2 URLSTRING=" + webpage2.urlstring);
					webpage2.readPage();
					String issueregex = "\"/issue/show/date/(\\d\\d\\d\\d-\\d\\d-\\d\\d)\"";
					pattern = Pattern.compile(issueregex);
					matcher = pattern.matcher(webpage2.html);
					Dbutil.logger.info("************* BBBBB INSIDE execute matcher=" + matcher);

					while (matcher.find() && newissuedate.equals("")) {
						Dbutil.logger.info(
								"************* CCCCC INSIDE execute inside while loop newissuedate is empty. matcher.group(1)="
										+ matcher.group(1));
						boolean after = dfm.parse(matcher.group(1)).after(dfm.parse("2009-05-31"));
						Dbutil.logger.info("************* DDDDD INSIDE execute dfm.parse(" + matcher.group(1)
								+ ").after(dfm.parse(\"2009-05-31\"))? " + after);
						if (after) {
							int count = Dbutil.readIntValueFromDB(
									"select count(*) as thecount from ratedwines where author='WS' and issuedate='"
											+ matcher.group(1) + "';",
									"thecount");
							Dbutil.logger.info(
									"************* EEEEE INSIDE execute SQL query : select count(*) as thecount from ratedwines where author='WS' and issuedate='"
											+ matcher.group(1) + "=" + count);
							if (count == 0) {
								Dbutil.logger.info(
										"************* FFFFF INSIDE execute SQL query : COUNT ZERO, SETTING NEW ISSUE DATE");
								newissuedate = matcher.group(1);
							}
							Dbutil.logger.info(
									"************* GGGGG INSIDE execute SQL query : COUNT ZERO, NEW ISSUE DATE SET newissuedate="
											+ newissuedate);
						}
					}
					Dbutil.logger
							.info("************* HHHHH INSIDE execute after while loop newissuedate=" + newissuedate);
					if (newissuedate.equals("")) {
						Dbutil.logger.info(
								"************* IIIII INSIDE execute after while loop inside if newissuedate is blank");
						webpage2.urlstring = "http://www.winespectator.com/issue";
						webpage2.readPage();
						pattern = Pattern.compile(issueregex);
						matcher = pattern.matcher(webpage2.html);
						Dbutil.logger.info(
								"************* JJJJJ INSIDE execute after while loop inside if newissuedate is blank -> matcher="
										+ matcher);
						boolean matcherfound = matcher.find();
						Dbutil.logger.info(
								"************* KKKKK INSIDE execute after while loop inside if newissuedate is blank -> matcherfound? "
										+ matcherfound);
						Dbutil.logger.info(
								"************* KKKKK INSIDE execute after while loop inside if newissuedate is blank -> matcherfound? "
										+ matcherfound);
						while (matcherfound && newissuedate.equals("")) {
							boolean after = dfm.parse(matcher.group(1)).after(dfm.parse("2009-05-31"));
							Dbutil.logger.info("************* LLLLL INSIDE execute dfm.parse(" + matcher.group(1)
									+ ").after(dfm.parse(\"2009-05-31\"))? " + after);
							int count = Dbutil.readIntValueFromDB(
									"select count(*) as thecount from ratedwines where author='WS' and issuedate='"
											+ matcher.group(1) + "';",
									"thecount");
							Dbutil.logger.info(
									"************* MMMMM INSIDE execute SQL query : select count(*) as thecount from ratedwines where author='WS' and issuedate='"
											+ matcher.group(1) + "=" + count);
							if (after && count == 0) {
								Dbutil.logger.info(
										"************* NNNNN INSIDE execute after while loop inside if newissuedate is blank after=true and count > 0");
								newissuedate = matcher.group(1);
								Dbutil.logger.info(
										"************* OOOOO INSIDE execute after while loop inside if newissuedate is blank after=true and count > 0 new newissuedate="
												+ newissuedate);
							}
							matcherfound = matcher.find();

							Dbutil.logger.info(
									"************* OOOOO 22222 INSIDE execute after while loop inside if newissuedate is blank -> matcherfound? "
											+ matcherfound);
						}
						Dbutil.logger.info(
								"************* PPPPP INSIDE execute after while loop inside if newissuedate is blank after while loop newissuedate="
										+ newissuedate);
					}
					Dbutil.logger.info("************* QQQQQ INSIDE execute newissuedate=" + newissuedate
							+ ", Wijnzoeker.muststopnow=" + Wijnzoeker.muststopnow);
					if (!newissuedate.equals("") && !Wijnzoeker.muststopnow) {
						boolean modifyWSIssue = modifyWSIssue(newissuedate);
						Dbutil.logger.info("************* RRRRR INSIDE execute modifyWSIssue(" + newissuedate + ")="
								+ modifyWSIssue);
						if (modifyWSIssue) {
							Dbutil.logger.info("************* SSSSS INSIDE execute");
							if (!Wijnzoeker.muststopnow) {
								long wait = 0;// (long)(Math.random()*1000*60*60*2);
								// Dbutil.logger.info("************* TTTTT INSIDE execute New Wine Spectator
								// issue found: "
								// + newissuedate + ". Before starting daily WS batch I will sleep for "
								// + wait / (1000 * 60) + " mins.");
								// Thread.sleep(wait);
								Wijnzoeker wijnzoeker = new Wijnzoeker();
								shoptodebug = 204;
								Configuration.wsissuedate = newissuedate;
								Wijnzoeker.auto = "";
								Wijnzoeker.type = 2; // rating
								Configuration.maxConcurrentThreads = 1;
								Dbutil.logger
										.info("************* TTTTT INSIDE execute CALLING wijnzoeker.updateSites()");
								wijnzoeker.updateSites();
								Dbutil.logger.info("************* UUUUU INSIDE execute Finished scraping WS issue "
										+ newissuedate);
							}
							Dbutil.logger.info("************* VVVVV INSIDE execute");
						} else {
							Dbutil.logger.error(
									"************* WWWWW INSIDE execute Did not start WS run: could not update Url, sending mail "
											+ webpage2.urlstring);
							Sms sms = new Sms(Configuration.gmailusername, Configuration.gmailpassword,
									"Problem scraping Wine Spectator");
							sms.send();
							// modifyWSIssue("2010-05-31");
						}
					}
					Dbutil.logger.error("************* XXXXX INSIDE execute END HEREEEEEEEEEEEEEEEEE");
				} catch (Exception e) {
					Dbutil.logger.error("YYYYY Calculated wrong issue number for WS run: " + issuenumber);
				}
			}
			Dbutil.logger.error("************* ZZZZZ INSIDE execute");

			// WINE SPECTATOR - END HERE

			if (args[0].equals("winespectatorfromcache")) {
				new FileSpider(204);
			}

			if (args[0].equals("scrapesingleshop")) {
				if (args.length > 1) {
					int shopnumber = 0;
					try {
						shopnumber = Integer.parseInt(args[1]);
					} catch (Exception e) {
					}
					if (shopnumber > 0) {
						Wijnzoeker wijnzoeker = new Wijnzoeker();
						shoptodebug = shopnumber;
						MailProcessor.receiveUploadMessages();
						Wijnzoeker.auto = "";
						Wijnzoeker.type = 1; // Normal shops
						wijnzoeker.updateSites();
					}
				}
			}
			if (args[0].equals("debugrun")) {
			}
		}

	}

	public static String[] getBottleRegex(int priority) {

		// priority>0: get only expressions with priority higher (lower number) than int
		// priority;
		String whereclause = "";
		if (priority > 0) {
			whereclause = " where priority<" + priority + " ";
		}
		ArrayList<String> regex = new ArrayList<String>(0);
		Connection con = Dbutil.openNewConnection();
		ResultSet rs;
		try {
			rs = Dbutil.selectQuery("select * from bottlesize " + whereclause + " order by priority;", con);
			while (rs.next()) {
				regex.add(rs.getString("Search"));
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while retrieving bottle size regex;", exc);
		}
		String[] regexarray = new String[regex.size()];

		for (int j = 0; j < regex.size(); j++) {
			regexarray[j] = regex.get(j);
		}
		Dbutil.closeConnection(con);
		return regexarray;
	}

	public static String[] getCaseRegex() {
		ArrayList<String> regex = new ArrayList<String>(0);
		Connection con = Dbutil.openNewConnection();
		ResultSet rs;
		try {
			rs = Dbutil.selectQuery("select * from casesize order by priority;", con);
			while (rs.next()) {
				regex.add(rs.getString("Search"));
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while retrieving case size regex;", exc);
		}
		String[] regexarray = new String[regex.size()];

		for (int j = 0; j < regex.size(); j++) {
			regexarray[j] = regex.get(j);
		}
		Dbutil.closeConnection(con);
		return regexarray;
	}

	public static String[] getSynonyms() {
		ArrayList<String> synonyms = new ArrayList<String>(0);
		Connection con = Dbutil.openNewConnection();
		ResultSet rs;
		try {
			rs = Dbutil.selectQuery("select * from synonyms;", con);
			while (rs.next()) {
				synonyms.add(rs.getString("term"));
				synonyms.add(rs.getString("synonym"));
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while retrieving synonym list;", exc);
		}
		String[] synonymarray = new String[synonyms.size()];

		for (int j = 0; j < synonyms.size(); j++) {
			synonymarray[j] = synonyms.get(j);
		}
		Dbutil.closeConnection(con);
		return synonymarray;
	}

	public static float[] getBottleSize(int priority) {

		// priority>0: get only expressions with priority higher (lower number) than int
		// priority;
		String whereclause = "";
		if (priority > 0)
			whereclause = " where priority<" + priority + " ";

		ArrayList<Float> size = new ArrayList<Float>(0);
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;
		try {
			rs = Dbutil.selectQuery("select * from bottlesize " + whereclause + " order by priority", con);
			while (rs.next()) {
				size.add(rs.getFloat("Size"));
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while retrieving bottle size ", exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		float[] sizearray = new float[size.size()];

		for (int j = 0; j < size.size(); j++) {
			sizearray[j] = size.get(j);
		}
		return sizearray;
	}

	public static float[] getCaseSize() {
		ArrayList<Float> size = new ArrayList<Float>(0);
		Connection con = Dbutil.openNewConnection();
		ResultSet rs;
		try {
			rs = Dbutil.selectQuery("select * from casesize order by priority", con);
			while (rs.next()) {
				size.add(rs.getFloat("Size"));
			}
		} catch (Exception exc) {
			Dbutil.logger.error("Problem while retrieving case size ", exc);
		}
		Dbutil.closeConnection(con);
		float[] sizearray = new float[size.size()];

		for (int j = 0; j < size.size(); j++) {
			sizearray[j] = size.get(j);
		}
		return sizearray;
	}

	public static void setupOnlineProcesses() {
		if ("PRD".equals(Wijnzoeker.serverrole) && (System.getProperty("Batch") == null
				|| (System.getProperty("Batch") != null && !System.getProperty("Batch").equals("true")))) {
			// com.freewinesearcher.batch.sms.SMSMailreader.getInstance();
			// Dbutil.logger.info("Starting Vinopedia system monitor");
			// com.freewinesearcher.batch.sms.SystemMonitor.getInstance();
			com.freewinesearcher.online.housekeeping.BotDetector.getInstance();
			com.freewinesearcher.online.housekeeping.Searchstats.getInstance();
			CacheManager.getInstance();
			Configuration.webpagetimeout = 20000;

		}
		if (!"PRD".equals(Wijnzoeker.serverrole) && (System.getProperty("Batch") == null
				|| (System.getProperty("Batch") != null && !System.getProperty("Batch").equals("true")))) {
			// StoreInfo.refreshKML();
		}
		if ((System.getProperty("Batch") == null
				|| (System.getProperty("Batch") != null && !System.getProperty("Batch").equals("true")))) {

			try {
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				ObjectName name = new ObjectName("com.freewinesearcher.common:type=VPDatasource");
				VPDatasource mbean = new VPDatasource();
				try {
					mbs.unregisterMBean(name);
				} catch (Exception e) {
				}
				mbs.registerMBean(mbean, name);

			} catch (Exception e) {
				Dbutil.logger.error("Problem with JMX: ", e);
			}
		}
		// CacheManager.getInstance();
	}

	@SuppressWarnings("deprecation")
	public void updateSites() {
		try {

			ThreadGroup threadgroup = new ThreadGroup("Scrape Shops");
			Connection Shopcon;
			ResultSet Shoplist;
			Shopcon = Dbutil.openNewConnection();
			Shoplist = GetShops(Shopcon);
			long starttime = System.currentTimeMillis();

			long maxruntime = (long) ((60 * 1000) * Configuration.maxruntime);

			while (Shoplist.next() && (System.currentTimeMillis() - starttime) < maxruntime) {
				while ((threadgroup.activeCount() >= Configuration.maxConcurrentThreads
						&& (System.currentTimeMillis() - starttime) < maxruntime)) {
					Thread.sleep(1000);
				}
				System.gc();
				if ((System.currentTimeMillis() - starttime) < maxruntime) {
					// if(type==1){
					Spider s = new Spider(Shoplist.getString("id"), Shoplist.getString("Encoding"), auto, type);
					if (Configuration.issuenumber > 0) {
						s.issue = Configuration.issuenumber;
					}
					s.c = new Context(tenantid);
					new Thread(threadgroup, s).start();
					Thread.sleep(500);
					// } else if(type==2) {
					// new Thread(threadgroup,new Ratingspider(Shoplist.getString("id"),
					// Shoplist.getString("URLType"),Shoplist.getString("Encoding"),auto)).start();
					// }
				}

			}
			while (threadgroup.activeCount() > 0) {
				if (shoptodebug > 0) {
					Thread.sleep(1000);
				} else {
					Thread.sleep(60000);
				}
				if ((System.currentTimeMillis() - starttime) > maxruntime) {
					if (!muststopnow) {
						Dbutil.logger.warn("Time over. Stopping active threads now.");
					}
					muststopnow = true;
					Thread[] threads = new Thread[threadgroup.activeCount()];
					int n = threadgroup.enumerate(threads);
					Dbutil.logger.info("Open connections: " + ConnectionTracker.opened);
					Dbutil.logger.info("Active threads: " + n);
					for (int i = 0; i < n; i++) {
						Dbutil.logger.info("Active thread " + (i + 1) + "= " + threads[i].getName());
						Dbutil.logger.info("Stack trace of thread " + threads[i].getName() + ":");
						StackTraceElement[] STE = threads[i].getStackTrace();
						for (int j = 0; j < STE.length; j++) {
							Dbutil.logger.info(threads[i].getName() + " " + STE[j]);
						}
					}

					if (shoptodebug == 0) {
						Thread.sleep(600000);
					} else {
						Thread.sleep(5000);
					}

					threads = new Thread[threadgroup.activeCount()];
					n = threadgroup.enumerate(threads);
					if (n > 0) {
						Dbutil.logger.info("Tired of waiting. Going to kill active threads now. ");
						Dbutil.logger.info("Active threads: " + n);
						for (int i = 0; i < n; i++) {
							Dbutil.logger.info("Active thread " + (i + 1) + "= " + threads[i].getName());
							Dbutil.logger.info("Stack trace of thread " + threads[i].getName() + ":");
							StackTraceElement[] STE = threads[i].getStackTrace();
							for (int j = 0; j < STE.length; j++) {
								Dbutil.logger.info(threads[i].getName() + " " + STE[j]);
							}
							threads[i].stop();
							Dbutil.logger.info("Killed thread " + threads[i].getName());
							Thread.sleep(60000);
							threads = new Thread[threadgroup.activeCount()];
							n = threadgroup.enumerate(threads);
							if (n > 0) {
								Dbutil.logger.info("Following threads could not be killed. ");
								Dbutil.logger.info("Active threads: " + n);
								for (i = 0; i < n; i++) {
									Dbutil.logger.info("Active thread " + (i + 1) + "= " + threads[i].getName());
									Dbutil.logger.info("Stack trace of thread " + threads[i].getName() + ":");
									STE = threads[i].getStackTrace();
									for (int j = 0; j < STE.length; j++) {
										Dbutil.logger.info(threads[i].getName() + " " + STE[j]);
									}
								}
							}
						}
					} else {
						Dbutil.logger.info("All active threads have been stopped gently. ");

					}
				}
			}
			Dbutil.closeConnection(Shopcon);
		} catch (Exception exc) {
			Dbutil.logger.error("Exception in updatesites: ", exc);
		}
		Dbutil.logger.info("Finished batch job. Open connections:" + ConnectionTracker.opened);
	}

	public void autoScrapeSite(String Url, String Postdata) {
		try {
			int id = Autospider.addAutospider(Url, Postdata);
			ThreadGroup threadgroup = new ThreadGroup("Auto Scrape Shops");
			new Thread(threadgroup, new Autospider(id + "", Url, Postdata)).start();

		} catch (Exception exc) {
			Dbutil.logger.error("Exception in auto Scrape site " + Url, exc);
		}
	}

	public void autoScrapeNewWsShop(String wsid) {
		try {
			int id = Autospider.addAutospider(wsid);
			ThreadGroup threadgroup = new ThreadGroup("Auto Scrape Shops");
			Dbutil.logger.info("id started autoscraping: " + id);
			new Thread(threadgroup, new Autospider(id + "", "", "")).start();

		} catch (Exception exc) {
			Dbutil.logger.error("Exception in auto Scrape site Wsshop " + wsid, exc);
		}

	}

	public void autoScrapeWsShop(String id, String Url, String Postdata) {
		if (Postdata == null)
			Postdata = "";
		if (Url == null)
			Url = "";
		try {
			ThreadGroup threadgroup = new ThreadGroup("Auto Scrape Shops");
			new Thread(threadgroup, new Autospider(id, Url, Postdata)).start();

		} catch (Exception exc) {
			Dbutil.logger.error("Exception in auto Scrape site of shopid " + id, exc);
		}

	}

	public void suckWineSearcher() {
		try {

			ThreadGroup threadgroup = new ThreadGroup("Suck Wine Searcher");
			// Connection Shopcon;
			// ResultSet Shoplist;
			new Thread(threadgroup, new SuckWineSearcher()).start();

		} catch (Exception exc) {
			Dbutil.logger.error("Exception in SuckWineSearcher", exc);
		}
		Dbutil.logger.info("Finished batch job. Open connections:" + ConnectionTracker.opened);
	}

	public static ResultSet GetShops(Connection Shopcon) {
		ResultSet rs = null;
		try {

			// Get a list of shop ID's to cycle through from the shops table
			if (shoptodebug == 0) {
				rs = Dbutil.selectQuery("SELECT id, URLType, encoding from shops where disabled=0 and shoptype=" + type
						+ " order by (lastsearchended!='0000-00-00 00:00:00'),datediff(sysdate(),lastsearchstarted) desc,abs(lastsearchended-lastsearchstarted) desc;",
						Shopcon);
			} else {
				rs = Dbutil.selectQuery(
						"SELECT id, URLType, encoding from " + auto + "shops where id=" + shoptodebug + " order by id;",
						Shopcon);

			}
		} catch (Exception e) {
			Dbutil.logger.error("GetShops problem: ", e);
		}
		return rs;
	}

	public static ArrayList<Wine> getSearchResults(String username, String lastsearched, String order, int rowcount) {
		ArrayList<Wine> totalwineset = new ArrayList<Wine>(0);
		Wineset wineset = null;
		ResultSet rs;
		// String cheapest="false";
		boolean rareoldboolean = false;
		Connection con = Dbutil.openNewConnection();
		Timestamp now = new Timestamp(new java.util.Date().getTime());
		if (lastsearched == null || lastsearched.equals("")
				|| (now.getTime() - Timestamp.valueOf(lastsearched).getTime() > Configuration.maxhistorysearch)) {
			lastsearched = new Timestamp(now.getTime() - Configuration.maxhistorysearch).toString();
		}
		try {
			rs = Dbutil.selectQuery("SELECT * from search where username='" + username + "';", con);
			while (rs.next()) {
				// cheapest=rs.getString("cheapest");
				if (rs.getString("Rareold").equals("1")) {
					rareoldboolean = true;
				} else {
					rareoldboolean = false;
				}

				// if (rs.getString("Name").startsWith("Region:")){
				// all wines in a region, always take the cheapest
				// cheapest="true";
				// }
				// if (rs.getString("Cheapest").equals("true")){
				// Only cheapest wines
				// wineset = new Wineset(rs.getString("Name"), rs.getString("Vintage"),
				// lastsearched, Float.valueOf(rs.getString("Pricemin")).floatValue(),
				// Float.valueOf(rs.getString("Pricemax")).floatValue(),
				// rs.getString("country"), rareoldboolean, order,0,rowcount);
				// } else {
				// all wines
				wineset = new Wineset(rs.getString("Name"), rs.getInt("Vintage") + "", lastsearched,
						Float.valueOf(rs.getString("Pricemin")).floatValue(),
						Float.valueOf(rs.getString("Pricemax")).floatValue(), new Float(0), rs.getString("country"),
						rareoldboolean, rs.getString("cheapest"), order, 0, rowcount);

				// }
				for (int i = 0; i < wineset.Wine.length; i++) {
					totalwineset.add(wineset.Wine[i]);
				}
			}
		} catch (Exception exc) {
			Dbutil.logger.error(
					"Problem in getSearchResults for user " + username + " and lastsearched " + lastsearched + ". ",
					exc);
		}

		Dbutil.closeConnection(con);
		return totalwineset;
	}

	public static void mailUpdates() {
		Dbutil.logger.info("Starting job to send PriceAlert mails.");
		ResultSet usernames = null;
		Connection con = Dbutil.openNewConnection();
		String Results = "";
		int numberofmailssent = 0;
		boolean foundresults = false;
		ArrayList<Wine> winesfound;
		Timestamp now = new Timestamp(new java.util.Date().getTime());
		NumberFormat format = new DecimalFormat("#,##0.00");
		try {
			// Loop over each user with an active search
			usernames = Dbutil.selectQuery(
					"SELECT distinct u.username as username, p.lastsearched as lastsearched, u.user_email as email from jforum_users u, pricealertusers p where u.user_email=p.email;",
					con);
			while (usernames.next()) {
				Results = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/TR/html4/loose.dtd\">";
				Results = Results + "<html><body><div style='font-family:Arial;font-size:10pt;color:blue;'>"
						+ "Hi,<br/><br/>This is a message from <a href='https://www.vinopedia.com'>Vinopedia.com</a>. "
						+ "We found new results for your Price Alerts. Prices exlude VAT.<br/><br/>"
						+ "<TABLE><TH align='left'>Wine(s) found</TH><TH>Vintage</TH><TH>Size</TH><TH width=10%>Price</TH><TH>Shop</TH>";
				foundresults = false;
				winesfound = getSearchResults(usernames.getString("username"), usernames.getString("lastsearched"), "",
						100);
				for (int i = 0; i < winesfound.size(); i++) {
					foundresults = true;
					Results = Results + "<TR>";
					Results = Results + "<TD><a href =\"https://www.vinopedia.com/link.jsp?wineid="
							+ winesfound.get(i).Id
							+ "\" style='{color: #4d0027; text-decoration:none;}  /* a+=0 b+=0 c+=0 */ :visited {color: #4d0027} /* a+=0 b+=1 c+=0 */ :hover {color:#4d0027}  /* a+=0 b+=1 c+=0 */  :visited:hover {color: #4d0027} '>"
							+ winesfound.get(i).Name + "</a></TD>";
					Results = Results + "<TD>" + winesfound.get(i).Vintage + "</TD>";
					Results = Results + "<TD>" + Webroutines.formatSizecompact(winesfound.get(i).Size) + "</TD>";
					Results = Results + "<TD>&euro; " + format.format(winesfound.get(i).PriceEuroEx) + "</TD>";
					Results = Results + "<TD>" + winesfound.get(i).Shopname + "</TD>";
					Results = Results + "</TR>";
				}
				Results = Results
						+ "</TABLE><br/><br/>If you do not want to receive any more messages from Vinopedia, click <a href=\"https://www.vinopedia.com/deactivateaccount.jsp?username="
						+ usernames.getString("username") + "&amp;email=" + usernames.getString("email")
						+ "&amp;activationcode="
						+ usernames.getString("username").concat(usernames.getString("email")).hashCode()
						+ "\">here</a>";
				Results = Results + "</div></body></html>";
				if (foundresults) {
					Emailer emailer = new Emailer();
					emailer.sendEmail("do_not_reply@vinopedia.com", usernames.getString("email"),
							"Vinopedia PriceAlert", Results);
					Webactionlogger wal = new Webactionlogger("PriceAlertMail", "", usernames.getString("email"), "",
							"", "", 0, (float) 0.0, (float) 0.0, "", false, "", "", "", "", (Double) 0.0, 0);
					wal.logmenow();
					numberofmailssent++;
				}
				Dbutil.executeQuery("Update pricealertusers set lastsearched='" + now.toString() + "' where email='"
						+ usernames.getString("email") + "';");

			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem while sending email searchresults. ", e);
		}
		Dbutil.closeConnection(con);
		Dbutil.logger.info(numberofmailssent + " PriceAlert emails sent.");
		Dbutil.logger.info("Finished job to send PriceAlert mails.");
	}

	public static String getVersion() {
		String version = "";
		File file = new File(Configuration.basedir + "vinopedia.build");
		try {
			version = "Build " + Webroutines.getRegexPatternValue("build.number=(\\d+)",
					FileUtils.readFileToString(file), Pattern.MULTILINE) + " " + new Timestamp(file.lastModified());
		} catch (IOException e) {
			System.out.println("Could not generate version number");
			e.printStackTrace();
		}
		return version;
	}

	public static void mailBatchStatus() {
		Dbutil.logger.info("Starting job to send Batch Status mail.");
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		StringBuffer html = new StringBuffer();
		int duration;
		try {
			// if (false) {
			// String smstxt = "";
			// if (muststopnow)
			// smstxt += "Did not finish batch! ";
			// rs = Dbutil.selectQuery(
			// "select count(*) as thecount from (select shops.id as id from shops join
			// (select distinct(shopid) as shopid from wines) wines on
			// (shops.id=wines.shopid) where (succes>0 and URLtype!='Email') or (succes>21
			// and URLtype='Email') group by shopid) asd;",
			// con);
			// if (rs.next()) {
			// smstxt += rs.getInt("thecount") + " issues.";
			// }
			// smstxt += Webroutines.getSMSVisitorOverview();
			// Sms sms = new Sms(Configuration.gmailusername, Configuration.gmailpassword,
			// smstxt);
			// Calendar cal = GregorianCalendar.getInstance();
			// Calendar targettime = GregorianCalendar.getInstance();
			// targettime.set(Calendar.HOUR, 9);
			// targettime.set(Calendar.MINUTE, 0);
			// targettime.set(Calendar.SECOND, 0);
			// int difference = targettime.compareTo(cal) / 60000;
			// difference = 4 * 60 * 0;
			// if (!muststopnow)
			// sms.setMinutesahead(difference);
			// sms.send();
			// }
			String shoplist = "0";
			String shoplistchina = "";
			int todochina = 0;
			rs = Dbutil.selectQuery(
					"select group_concat(shops.id) as shops,count(*) as thecount from shopstats join shops on (shopstats.shopid=shops.id) left join issuelog on (shops.id=issuelog.shopid) left join datafeeds on (shops.id=datafeeds.shopid) where  succes>"
							+ Configuration.daysshopsnok
							+ " and shoptype=1 and shops.disabled=0 order by (succes between 20 and 30) desc,succes desc, lastknowngoodwinesunique desc,shops.id ;",
					con);
			if (rs.next()) {

				shoplistchina = rs.getString("shops");
				todochina = rs.getInt("thecount");
			}
			html.append("To do China: " + todochina + " shops (" + shoplistchina + ").");
			Dbutil.closeRs(rs);
			rs = Dbutil.selectQuery(
					"select group_concat(id) as shops from (select shops.id as id from shops join (select distinct(shopid) as shopid from wines) wines  on (shops.id=wines.shopid) where  (succes>"
							+ Configuration.daysshopsnok
							+ "  and URLtype!='Email') or (succes>21  and URLtype='Email') group by shopid)  asd;",
					con);
			if (rs.next()) {

				shoplist = rs.getString("shops");
			}
			if ("".equals(shoplist))
				shoplist = "0";

			// rs=Dbutil.selectQuery("select id as theid,
			// shopname,succes,disabled,(Lastsearchended - Lastsearchstarted) AS
			// duration,shopurl,amountwines AS estimated, total, totalold from shops left
			// join (select count(*) as total, totalold, shopid from wines left join (select
			// count(*) as totalold, shopid as shop from wines where
			// lastupdated<date(curdate()) group by shop) as old on wines.shopid=old.shop
			// group by shopid) as thealias on (shops.id=thealias.shopid) where succes>0 and
			// total>0 order by disabled,succes desc;",con);
			rs = Dbutil.selectQuery(
					"select id as theid, shopname,succes,disabled,(Lastsearchended - Lastsearchstarted) AS duration,shopurl,amountwines AS estimated, total, totalold from shops left join (select count(*) as total, totalold, shopid from wines left join (select count(*) as totalold, shopid as shop from wines where lastupdated<date(curdate()) "
							+ ("".equals(shoplist) ? "" : (" and shopid in (" + shoplist + ")"))
							+ " group by shop) as old on wines.shopid=old.shop "
							+ ("".equals(shoplist) ? "" : (" where shopid in (" + shoplist + ")"))
							+ " group by shopid) as thealias on (shops.id=thealias.shopid) "
							+ ("".equals(shoplist) ? "" : (" where shops.id in (" + shoplist + ")"))
							+ " order by disabled,succes desc;",
					con);
			while (rs.next()) {
				duration = -1;
				try {
					duration = rs.getInt("duration");
				} catch (Exception E) {
				}
				html.append(rs.getString("shopname") + ": ");
				html.append(rs.getString("succes") + " days no succes, ");
				html.append((rs.getInt("total") - rs.getInt("totalold")) + "/");
				html.append(rs.getInt("totalold") + " scraped, duration ");
				html.append(duration + "s.<br/>\n");
			}
			html.append(Webroutines.getVisitorOverview(2));
			Emailer emailer = new Emailer();
			emailer.sendEmail("do_not_reply@vinopedia.com", "jasper.hammink@freewinesearcher.com",
					"FreeWineSearcher Batch status", html.toString());

		} catch (Exception e) {
			Dbutil.logger.error("Problem while sending email searchresults. ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		Dbutil.logger.info("Finished job to send Batch Status mail.");
	}

	private static boolean modifyParkerIssue(int issue) {
		boolean succes = false;
		String rowid = Dbutil.readValueFromDB("select * from scrapelist where shopid=200 and urltype='Master';", "id");
		if (!rowid.equals("")) {
			String url = Dbutil.readValueFromDB("select * from scrapelist where id=" + rowid + ";", "Url");
			String newurl = url.replaceAll("SourceIssue=\\d+", "SourceIssue=" + issue);
			Dbutil.executeQuery("update scrapelist set Url='" + Spider.SQLEscape(newurl) + "' where id=" + rowid + ";");
			newurl = Dbutil.readValueFromDB("select * from scrapelist where id=" + rowid + ";", "Url");
			if (!url.equals(newurl)) {
				succes = true;
			} else {
				Dbutil.logger.error("I tried updating RP url for issue " + issue
						+ " but that did not work out: old url=" + url + ", new url=" + newurl);
			}
		}
		return succes;
	}

	private static boolean modifyWSIssue(String issuedate) {
		Dbutil.logger.info("************* INSIDE modifyWSIssue issuedate=" + issuedate);
		boolean succes = false;
		String rowid = Dbutil.readValueFromDB(
				"select * from scrapelist where shopid=204 and urltype='Master' and url like '%/wine/search%';", "id");
		Dbutil.logger.info("************* INSIDE modifyWSIssue rowid=" + rowid);
		if (!rowid.equals("")) {
			String url = Dbutil.readValueFromDB("select * from scrapelist where id=" + rowid + ";", "Url");
			String newurl = url.replaceAll("issue_date=\\d\\d\\d\\d-\\d\\d-\\d\\d", "issue_date=" + issuedate);
			Dbutil.logger.info("************* INSIDE modifyWSIssue newurl=" + newurl);
			Dbutil.executeQuery("update scrapelist set Url='" + Spider.SQLEscape(newurl) + "' where id=" + rowid + ";");
			newurl = Dbutil.readValueFromDB("select * from scrapelist where id=" + rowid + ";", "Url");
			if (!url.equals(newurl)) {
				succes = true;
			} else {
				Dbutil.logger.error("I tried updating WS url for issuedate " + issuedate
						+ " but that did not work out: old url=" + url + ", new url=" + newurl);
			}
		}
		Dbutil.logger.info("************* INSIDE modifyWSIssue succes=" + succes);
		return succes;
	}

}

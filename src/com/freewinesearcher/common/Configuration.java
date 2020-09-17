package com.freewinesearcher.common;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
	public static Properties FWSConfig = new Properties();
	public static String workspacedir=getWorkspacedir();
	
	static {
		fetchConfig();
	}
	
	


	/**
	 * Open a specific text file containing generic FWS
	 * parameters, and populate a corresponding Properties object.
	 * @throws FileNotFoundException 
	 */
	private static void fetchConfig()  {
		
		String file=System.getProperty("configfile", "fws.properties");
		if (file.equals("")) file="fws.properties";
		file=workspacedir+file;
		
		if (!new File(file).exists()){
			
			System.err.println("Cannot find configuration file "+file);
			Dbutil.logger.error("Cannot find configuration file "+file);

		} else {
			InputStream input = null;
			try {
				//If possible, one should try to avoid hard-coding a path in this
				//manner; in a web application, one should place such a file in
				//WEB-INF, and access it using ServletContext.getResourceAsStream.
				//Another alternative is Class.getResourceAsStream.
				//This file contains the javax.mail config properties mentioned above.

				input = new FileInputStream(file);
				FWSConfig.load( input );
				input.close();
				
			}
			catch (Exception ex ){
				System.err.println( "Cannot open FWS properties file "+file );
			}
			finally {
				try {
					if ( input != null ) input.close();
				}
				catch ( IOException ex ){
					Dbutil.logger.error( "Problem while handling FWS properties file.",ex );
				}
			}
		}
	}


	private static String getWorkspacedir() {
		String file=System.getProperty("file.separator")+"workspace"+System.getProperty("file.separator");
		if (System.getProperty("file.separator").equals("\\")) file="C:"+file;
		return file;
	}


	public static final String cdnprefix="https://static.vinopedia.com"; //https://vinopedia.appspot.com
	public static final String cdn2prefix="https://static2.vinopedia.com"; //https://vinopedia.appspot.com
	public static String basedir=FWSConfig.getProperty("basedir", "C:\\Program Files\\apache-tomcat-5.5.20\\webapps\\ROOT\\");
	public static String serverrole=FWSConfig.getProperty("serverrole", "PRD");
	public static int webpagetimeout=Integer.valueOf(FWSConfig.getProperty("webpagetimeout", "180"))*1000;
	public static String emaildir=FWSConfig.getProperty("emaildir", "C:\\email\\");
	public static String uploadaccount=FWSConfig.getProperty("emailaccount", "upload@vinopedia.com");
	public static String emailhost=FWSConfig.getProperty("emailhost", "www.vinopedia.com");
	public static String emailpassword=FWSConfig.getProperty("emailpassword", "");
	public static String invoiceaccount=FWSConfig.getProperty("invoiceaccount", "");
	public static String smsmailaccount=FWSConfig.getProperty("smsmailacount", "sms@vinopedia.com");
	public static int thisyear=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
	//public static String smsmailpassword=FWSConfig.getProperty("smsmailpassword", "");
	public static String gmailusername=FWSConfig.getProperty("gmailusername", "");
	public static String gmailpassword=FWSConfig.getProperty("gmailpassword", "");
	public static String SSLpassword=FWSConfig.getProperty("SSLpassword", "");
	public static String SSLcertificate=FWSConfig.getProperty("SSLcertificate", "");
	public static String cachedpagesdir=FWSConfig.getProperty("cachedpagesdir", "/workspace/cachedpages/");
	public static int issuenumber=0; // For WS/Parker
	public static int shoptodebug = Integer.valueOf(FWSConfig.getProperty("shoptodebug","0"));;
	public static String invoicedir="";
	public static String videodir="";
	public static double maxruntime=Integer.valueOf(FWSConfig.getProperty("maxruntime","480")); // Maximum runtime in minutes
	public static float[] casesize=Wijnzoeker.getCaseSize();
	public static int defaultpayterm=14; //days
	public static float[] size=Wijnzoeker.getBottleSize(0);
	public static String[] sizeregex=Wijnzoeker.getBottleRegex(0);
	public static float[] limitedsize=Wijnzoeker.getBottleSize(2000);
	public static String[] limitedsizeregex=Wijnzoeker.getBottleRegex(2000);
	public static String[] caseregex=Wijnzoeker.getCaseRegex();
	public static String[] synonyms=Wijnzoeker.getSynonyms();
	public static String captchapublickey="6LdPowMAAAAAAAejzRoexwmZr2cZtgexTjiyKvrQ";
	public static String captchaprivatekey="6LdPowMAAAAAAJfVFzTK4ExOqGnql-8a9MVFSEHw";
	public static String recaptchasiteverifyurl="https://www.google.com/recaptcha/api/siteverify";
	public static String recaptchasecretkey="6Ld4YEUUAAAAACNZXEPBUe7HAaur2dBAY6sUolMV";
	public static String recaptchasitekey="6Ld4YEUUAAAAABu1XOYEiZnK8rSJboDmoDnz4VPY";
	public static String donotlogforhost=FWSConfig.getProperty("donotlogforhost", "");
	public static String[] colorvalues={"Red","White","Ros�"};
	public static String[] drynessvalues={"Dry","Sweet/Dessert","Off-dry","Fortified"};
	public static String proxyaddress=FWSConfig.getProperty("proxyaddress", "127.0.0.1");
	public static int proxyport=Integer.parseInt(FWSConfig.getProperty("proxyport", "8118"));
	static int maxConcurrentThreads=Integer.valueOf(FWSConfig.getProperty("maxconcurrentthreads","5"));
	public static boolean newrecognitionsystem=Boolean.parseBoolean(FWSConfig.getProperty("newrecognitionsystem","false"));
	public static int maxCrawlsPerSite=Integer.valueOf(FWSConfig.getProperty("maxcrawlspersite","2000"));
	public static int minimumCrawlInterval=Integer.valueOf(FWSConfig.getProperty("minimumcrawlinterval","5000"));
	public static int daysshopsnok=Integer.valueOf(FWSConfig.getProperty("daysshopsnok","4"));
	static long maxhistorysearch = (long)Integer.valueOf(FWSConfig.getProperty("maxhistorysearch","7"))*24*3600*1000;
	public static final int numberofnormalrows=Integer.valueOf(FWSConfig.getProperty("numberofnormalrows", "25"));
	public static int graceperiod=Integer.valueOf(FWSConfig.getProperty("graceperiod", "30"));
	public static int rareoldyear = Integer.valueOf(FWSConfig.getProperty("rareoldyear","1998"));
	public static String GoogleApiKeyv3="AIzaSyBeBAoIjlNc0iExzqg_on27HgHnxMFQtSo";
	public static String GoogleApiKey="ABQIAAAAuPfgtY5yGQowyqWw-A_zlhQVq_qKtmEoIwKy-05cQ_o4M6mWQxRxZ5Z1U9sxCq2D_B2AgkztMmWsfA";
	public static String GoogleApiKeyDev="ABQIAAAAuPfgtY5yGQowyqWw-A_zlhQVq_qKtmEoIwKy-05cQ_o4M6mWQxRxZ5Z1U9sxCq2D_B2AgkztMmWsfA";
	public static String[] typevalues={"Red (dry)","White (dry)","Ros� (dry)","Red - Fortified","Red - Sparkling","Red - Sweet/Dessert","White - Fortified","White - Off-dry","White - Sparkling","White - Sweet/Dessert","Ros� - Sparkling","Ros� - Sweet/Dessert"};
	public static String Twitterusername=FWSConfig.getProperty("Twitterusername", "");
	public static String Twitterpassword=FWSConfig.getProperty("Twitterpassword", "");
	public static String chinaaccounts=FWSConfig.getProperty("chinaaccounts", "'china','LuoQi','WuJiaQi'");
	public static String chinaemail=FWSConfig.getProperty("chinaemail", "");
	public static String wsissuedate="0000-00-00";
	public static final String Facebookapi="";
	public static final String Facebooksecret="";
	public static final String Facebookapplicationid="";
	public static int partialknownwinesperiod=Integer.valueOf(FWSConfig.getProperty("partialknownwinesperiod", "3"));
	public static int parallelrecognitionthreads=Integer.valueOf(FWSConfig.getProperty("parallelrecognitionthreads","1"));
	public static final String staticprefix="https://"+("DEV".equals(serverrole)?"test":"static")+".vinopedia.com";
	public static final String static2prefix="https://"+("DEV".equals(serverrole)?"test":"static2")+".vinopedia.com";
	public static final String stylesheet=staticprefix+"/css/stylesheet3.css?version=33";
	public static final String securestylesheet="/css/stylesheet3.css?version=33";
	public static final String adtext="<div class='head'>Ads by Vinopedia.com</div>";
	public static final String jquerymobilejs="http://code.jquery.com/mobile/1.0.1/jquery.mobile-1.0.1.min.js";
	public static final String jquerymobilecss="http://code.jquery.com/mobile/1.0.1/jquery.mobile-1.0.1.min.css";
	public static final String PayPalApiUsername=FWSConfig.getProperty("PayPalApiUsername", "");
	public static final String PayPalApiPassword=FWSConfig.getProperty("PayPalApiPassword", "");
	public static final String PayPalApiSignature=FWSConfig.getProperty("PayPalApiSignature", "");
	public static final String devpcip="82.157.45.39";
	
	public static final boolean detectSuspectedBot=Boolean.parseBoolean(FWSConfig.getProperty("detectSuspectedBot", "false"));
	public static final boolean logSuspectedBot=Boolean.parseBoolean(FWSConfig.getProperty("logSuspectedBot", "false"));
	
	
	public static void writemsg(String msg){
		String fileName=System.getProperty("file.separator")+"workspace"+System.getProperty("file.separator")+"error.log";
		try {
			FileOutputStream file = new FileOutputStream(fileName,true);
			DataOutputStream out   = new DataOutputStream(file);
			  out.writeBytes(msg+"\n");
			  out.flush();
			  out.close(); 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}

	public String getPrefix(){
		String pref="https://"+("DEV".equals(serverrole)?"test":"static")+".vinopedia.com";
		return pref;
	}

}
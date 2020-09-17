package com.freewinesearcher.batch.sitescrapers;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.NetworkTool;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.online.Webroutines;


public class SuckCT {
	int pause; // average pause in seconds between 2 fetches
	//Webpage webpage;
	boolean ok;
	String useragent="Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.79 Safari/535.11";
	CommonUtilities util;
	boolean ipworking;
	ArrayList<String> badips=new ArrayList<String>();
	int rowid;
	int status;
	Browser browser;

	public Browser getBrowser() {
		Browser b=null;
		while (b==null){
			try{
				b=ChromeBrowser.getInstance();
				if (b.isAnonymous()) return b;
				b.close();
			} catch (Exception e){
				try{
					b.close();
				}catch(Exception ef){}
				b=null;
			}
		}
		return null;
	}

	public SuckCT(int pause){
		try{
			//browser=ChromeBrowser.getInstance();
			browser=getBrowser();
			//externalip=browser.ip();
			this.pause=pause;
			util=new CommonUtilities();
			util.dirname="C:\\CT";
			String locale;
			rowid=0;
			String extra="";
			//if (tabel.equals("SubRegion")) extra ="&Appellation=Unknown";
			//if (tabel.equals("Region")) extra ="&Appellation=Unknown&SubRegion=Unknown";
			String tabel="Locale";

			boolean ready=false;
			try{
				while (!ready){
					status=0;
					locale=getNextValue();
					String appellation=locale.split(", ")[locale.split(", ").length-1].trim();
					if (!locale.equals("")){
						ok=true;
						Dbutil.logger.info("Starting to read "+tabel+" "+locale);
						Dbutil.executeQuery("delete from ctknownwines where locale='"+Spider.SQLEscape(locale)+"'");
						/*
						if (status==1) read("http://www.cellartracker.com/list.asp?PrinterFriendly=true&table=Pivot&S2=qd&Pivot1=Wine&S1=n&Pivot2=Type&Pivot3=Varietal"+extra+"&"+tabel+"="+URLEncode(appellation)+"&iUserOverride=0&S3=qd", 
								"tv", rowid, appellation,0,tabel);
						if (status==1) read("http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Designation&S2=q&Pivot3=Vineyard&PrinterFriendly=true"+extra+"&"+tabel+"="+URLEncode(appellation)+"&iUserOverride=0&S3=q", 
								"cv", rowid, appellation,1,tabel);
						if (status==1) read("http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Producer&S2=q&Pivot3=Locale&PrinterFriendly=true"+extra+"&"+tabel+"="+URLEncode(appellation)+"&iUserOverride=0&S3=q", 
								"pl", rowid, appellation,2,tabel);
						 */
						read("http://www.cellartracker.com/list.asp?PrinterFriendly=true&table=Pivot&S2=qd&Pivot1=Wine&S1=n&Pivot2=Type&Pivot3=Varietal"+extra+"&"+tabel+"="+URLEncode(locale)+"&iUserOverride=0&S3=qd", 
								"tv", rowid, locale,appellation,0,tabel);
						if (status==1) read("http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Designation&S2=qd&Pivot3=Vineyard&PrinterFriendly=true"+extra+"&"+tabel+"="+URLEncode(locale)+"&iUserOverride=0&S3=q", 
								"cv", rowid, locale,appellation,1,tabel);
						if (status==1) read("http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Producer&S2=qd&Pivot3=Locale&PrinterFriendly=true"+extra+"&"+tabel+"="+URLEncode(locale)+"&iUserOverride=0&S3=q", 
								"pl", rowid, locale,appellation,2,tabel);
						if (status==1){
							Dbutil.executeQuery("update ct"+tabel.toLowerCase()+"s set status=1 where "+tabel.toLowerCase()+"='"+Spider.SQLEscape(locale)+"';");
							Dbutil.logger.info("Got info for "+tabel+" "+locale);
						} else {
							Dbutil.executeQuery("update ct"+tabel.toLowerCase()+"s set status="+status+" where "+tabel.toLowerCase()+"='"+Spider.SQLEscape(locale)+"';");
							Dbutil.logger.info("Problem: status "+status+" while getting page for "+tabel+" "+locale);
						}

					} else {
						ready=true;
					}
				}
			} catch (Exception e){
				Dbutil.logger.error("Problem: ",e);
				System.exit(1);
			} 
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			System.exit(1);
		} 


	}






	public void read(String url, String fileextension, int rowid,String locale,String appellation,int type,String category) throws Exception{
		/*
		webpage=new Webpage();
		webpage.maxattempts=1;
		webpage.useragent=useragent;
		webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
		webpage.urlstring=url;
		webpage.readPage();
		 */
		String html="";
		ok=false;
		/*String ip=browser.ip();
		if (!ip.equals(externalip)){
			browser.driver.get("http://www.cellartracker.com/intro.asp");
			Coordinates.doPause((int)(5000));
			externalip=ip;
		}
		 */
		//browser.driver.get(url);
		browser.get(url);
		html=browser.getHtml();
		ok=pageIsOK(html);
		if (!ok) {
			System.out.print("\007"); System.out.flush();
			Thread.sleep(60000); // refresh
			html=browser.getHtml();
			ok=pageIsOK(html);
			
		}
		
		while (status==2){
			Dbutil.logger.info("Captcha");
			System.out.print("\007"); System.out.flush();
			Thread.sleep(60000); // Solve captcha
			html=browser.getHtml();
			ok=pageIsOK(html);
			if (status==2){ // Not solved, new proxy
				browser.close();
				browser=getBrowser();
				browser.get(url);
				html=browser.getHtml();
				ok=pageIsOK(html);
				if (status==2) Thread.sleep(60000);
			}
		}


		if (!ok) Thread.sleep(10000);


		//ok=pageIsOK(webpage.html);
		if (ok){
			ipworking=true;
			//externalip=browser.ip();
			Dbutil.logger.info("OK, IP="+browser.getIp()+", got "+rowid+"-"+fileextension+" for "+appellation);
			util.deleteFile(rowid+"-"+fileextension+"-"+category+".html");
			util.saveFile(rowid+"-"+fileextension+"-"+category+"-utf8.html", html);
			ripinfo(html,locale,appellation,type);


		} else {
			//if (status==2){
			/*

				//String currentip=browser.ip();
				util.saveFile(rowid+"-"+fileextension+"."+currentip+".error.html", html);
				badips.add(currentip);
				if (ipworking&&externalip.equals(currentip)){
					// An ip that was working does not work any more.. slower spidering
					Dbutil.logger.info("IP "+externalip+" is not working for url "+url);
					Dbutil.logger.info("IP "+externalip+" that was working does not work any more... slowing down spidering from "+pause+" to "+(int)(pause*1.1));
					pause=(int)(pause*1.1);

				}else {
					Dbutil.logger.info("IP "+externalip+" is not working for url "+url);

				}
				externalip=currentip;
				do{
					NetworkTool.requestNewIdenty(externalip);
					externalip=browser.ip();
				} while (badips.contains(externalip));
				ipworking=false;
			} else {
				String currentip=browser.ip();
				util.saveFile(rowid+"-"+fileextension+"."+currentip+".error.html", html);
				Dbutil.logger.info("Status "+status+" for "+url);
			 */
			//}
		}
		Coordinates.doPause((int)(pause*1000+Math.round(Math.random()*pause*1000)));
	}


	private String obsoletegetNextValue(String type){
		String id="";
		if(!type.equals("appellation")) id="c.id, ";
		String query = "SELECT * FROM ct"+type.toLowerCase()+"s c left join knownwines on (c."+type.toLowerCase()+"=knownwines.appellation) where status=0 group by c.id order by "+id+"count(*) desc limit 1;";
		Connection con=Dbutil.openNewConnection();
		ResultSet rs= null;
		String value="";
		try{
			rs= Dbutil.selectQuery(query, con);
			if (rs.next()){
				value=rs.getString(type);
				rowid=rs.getInt("id");
				Dbutil.closeRs(rs);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			System.exit(1);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return value;

	}

	private String getNextValue(){
		String query = "SELECT * FROM ctlocales c where status=0 order by priority desc,bottles desc limit 1;";
		Connection con=Dbutil.openNewConnection();
		ResultSet rs= null;
		String value="";
		try{
			rs= Dbutil.selectQuery(query, con);
			if (rs.next()){
				value=rs.getString("locale");
				rowid=rs.getInt("id");
				Dbutil.closeRs(rs);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			System.exit(1);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return value;

	}

	//For subregions as not all wines have an appellation
	public SuckCT(int pause, float anyString){ //anystring just to make it different from the other SuckCT
		// TO DO: aanpassen met TOR change identity
		this.pause=pause;
		CommonUtilities util=new CommonUtilities();
		util.dirname="C:\\CT";
		Webpage webpage=new Webpage();
		webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String appellation;
		int rowid=0;
		boolean ok;
		boolean ready=false;
		try{
			while (!ready){
				query="select * from ctsubregions where status=0 order by rand() limit 1;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					appellation=rs.getString("subregion");
					rowid=rs.getInt("id");
					Dbutil.closeRs(rs);
					ok=true;
					if (ok){
						webpage=new Webpage();
						webpage.maxattempts=1;
						webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
						webpage.urlstring="http://www.cellartracker.com/list.asp?PrinterFriendly=true&table=Pivot&S2=qd&Pivot1=Wine&S1=n&Pivot2=Type&Pivot3=Varietal&Appellation=Unknown&SubRegion="+URLEncode(appellation)+"&iUserOverride=0&S3=qd";
						//http://www.cellartracker.com/list.asp?table=Pivot&SubRegion=Cademario&Appellation=Unknown&iUserOverride=0&Pivot1=iWine &Appellation=Unknown&SubRegion=Cademario
						webpage.readPage();
						ok=pageIsOK(webpage.html);
						if (ok){
							util.saveFile("sr"+rowid+"-"+"tv.html", webpage.html);
							ripinfo(webpage.html,appellation,appellation,0);
							Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
						}
					}
					if (ok){
						webpage=new Webpage();
						webpage.maxattempts=1;
						webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
						webpage.urlstring="http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Designation&S2=q&Pivot3=Vineyard&PrinterFriendly=true&Appellation=Unknown&SubRegion="+URLEncode(appellation)+"&iUserOverride=0&S3=q";
						//webpage.urlstring="file://C:\\Workspace\\Cellartrackerdata\\testripwineinfoproducerlocale.htm";
						webpage.readPage();
						ok=pageIsOK(webpage.html);
						if (ok){
							util.saveFile("sr"+rowid+"-"+"cv.html", webpage.html);
							ripinfo(webpage.html,appellation,appellation,1);
							Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
						}
					}
					if (ok){
						webpage=new Webpage();
						webpage.maxattempts=1;
						webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
						webpage.urlstring="http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Producer&S2=q&Pivot3=Locale&PrinterFriendly=true&Appellation=Unknown&SubRegion="+URLEncode(appellation)+"&iUserOverride=0&S3=q";
						//webpage.urlstring="file://C:\\Workspace\\Cellartrackerdata\\testripwineinfodesigvineyard.htm";
						webpage.readPage();
						ok=pageIsOK(webpage.html);
						if (ok){
							util.saveFile("sr"+rowid+"-"+"pl.html", webpage.html);
							ripinfo(webpage.html,appellation,appellation,2);
							Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
						}
					}
					if (ok){
						Dbutil.executeQuery("update ctsubregions set status=1 where subregion='"+Spider.SQLEscape(appellation)+"';");
					} else {
						Dbutil.executeQuery("update ctsubregions set status=2 where subregion='"+Spider.SQLEscape(appellation)+"';");
						Dbutil.logger.info("Problem while getting page for subregion "+appellation);
					}


				} else {
					ready=true;
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			System.exit(1);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}



	}

	//For regions as not all wines have an appellation
	public SuckCT(int pause, int anyInt){ //anystring just to make it different from the other SuckCT
		// TO DO: aanpassen met TOR change identity
		this.pause=pause;
		CommonUtilities util=new CommonUtilities();
		util.dirname="C:\\CT";
		Webpage webpage=new Webpage();
		webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String appellation;
		int rowid=0;
		boolean ok;
		boolean ready=false;
		try{
			while (!ready){
				query="select * from ctregions where status=0 order by rand() limit 1;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					appellation=rs.getString("region");
					rowid=rs.getInt("id");
					Dbutil.closeRs(rs);
					ok=true;
					if (ok){
						webpage=new Webpage();
						webpage.maxattempts=1;
						webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
						webpage.urlstring="http://www.cellartracker.com/list.asp?PrinterFriendly=true&table=Pivot&S2=qd&Pivot1=Wine&S1=n&Pivot2=Type&Pivot3=Varietal&Appellation=Unknown&SubRegion=Unknown&Region="+URLEncode(appellation)+"&iUserOverride=0&S3=qd";
						//http://www.cellartracker.com/list.asp?table=Pivot&SubRegion=Cademario&Appellation=Unknown&iUserOverride=0&Pivot1=iWine &Appellation=Unknown&SubRegion=Cademario
						webpage.readPage();
						ok=pageIsOK(webpage.html);
						if (ok){
							util.saveFile("re"+rowid+"-"+"tv.html", webpage.html);
							ripinfo(webpage.html,appellation,appellation,0);
							Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
						}
					}
					if (ok){
						webpage=new Webpage();
						webpage.maxattempts=1;
						webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
						webpage.urlstring="http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Designation&S2=q&Pivot3=Vineyard&PrinterFriendly=true&Appellation=Unknown&SubRegion=Unknown&Region="+URLEncode(appellation)+"&iUserOverride=0&S3=q";
						//webpage.urlstring="file://C:\\Workspace\\Cellartrackerdata\\testripwineinfoproducerlocale.htm";
						webpage.readPage();
						ok=pageIsOK(webpage.html);
						if (ok){
							util.saveFile("re"+rowid+"-"+"cv.html", webpage.html);
							ripinfo(webpage.html,appellation,appellation,1);
							Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
						}
					}
					if (ok){
						webpage=new Webpage();
						webpage.maxattempts=1;
						webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
						webpage.urlstring="http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&S1=n&Pivot2=Producer&S2=q&Pivot3=Locale&PrinterFriendly=true&Appellation=Unknown&SubRegion=Unknown&Region="+URLEncode(appellation)+"&iUserOverride=0&S3=q";
						//webpage.urlstring="file://C:\\Workspace\\Cellartrackerdata\\testripwineinfodesigvineyard.htm";
						webpage.readPage();
						ok=pageIsOK(webpage.html);
						if (ok){
							util.saveFile("re"+rowid+"-"+"pl.html", webpage.html);
							ripinfo(webpage.html,appellation,appellation,2);
							Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
						}
					}
					if (ok){
						Dbutil.executeQuery("update ctregions set status=1 where region='"+Spider.SQLEscape(appellation)+"';");
					} else {
						Dbutil.executeQuery("update ctregions set status=2 where region='"+Spider.SQLEscape(appellation)+"';");
						Dbutil.logger.info("Problem while getting page for region "+appellation);
					}


				} else {
					ready=true;
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			System.exit(1);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}



	}

	public boolean pageIsOK(String page){

		if (page.contains("No results returned")) {
			status=4;
			return false;
		}
		if (page.contains("Script times out")) {
			status=3;
			return false;
		}
		if (page.contains("Timeout expired")) {
			status=3;
			return false;
		}
		if (!page.contains("TOTAL COLLECTION")) {
			status=2;
			return false;
		}
		if (page.contains("Locale")){
			status=1;
			return true;
		}
		status=5;
		return false;
	}



	public static void ripinfo(String page, String locale,String appellation,int type) throws Exception{
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
		String[] cache=new String[3];
		String[] values=new String[3];
		Integer[] rowspan=new Integer[3];
		for (int i=0;i<=2;i++){
			cache[i]="";
			values[i]="";
			rowspan[i]=0;
		}
		String[] lines=page.replaceAll("\n", "").split("<tr");
		String[] cols;
		int offset=2;
		int col=0;
		int readfrom=0;
		int rowspanlines;
		int maxbottles=0;
		boolean cached=false;
		int bottles = 0;
		for (String line:lines){
			if ((line.contains("class=\"properties\"")||line.contains("class='properties'"))&&!line.contains("TOTAL COLLECTION")){
				readfrom=offset;
				cols=line.split("<td");
				try {
					cached=false;
					maxbottles=0;
					for (col = 0; col <= 2; col++) {
						values[col] = "";

						
						values[col] = Spider.unescape(Webroutines
								.getRegexPatternValue(">([^<]+)<",
										cols[readfrom]).trim());
						if (values[col].equals(" ")){
							values[col]=cache[col];
						} else {
							cache[col]=values[col];
							if (type>0&&col==0){
								// more data from wine with same name, only update the line with the max amount of bottles
								cached=true;
							}
						}
						if (type==0&&col==2){
							bottles=Integer.parseInt(Webroutines.getRegexPatternValue("\\D(\\d+) bottle",cols[readfrom]));
						}
						if (type==0&&col==0&&values[col].contains("?")){
							Dbutil.executeQuery("update ctlocales set status=0 where locale='"+Spider.SQLEscape(locale)+"';");
							
						}
						if (type>0&&col==2){
							bottles=Integer.parseInt(Webroutines.getRegexPatternValue("\\D(\\d+) bottle",cols[readfrom]));
							if (bottles>maxbottles) maxbottles=bottles;
						}
						
						readfrom++;
						//}
						if (values[col].equals("")
								|| !Webroutines.getRegexPatternValue("(&.{1,5};)", values[col]).equals("")) {
							Dbutil.logger.warn("Problem: value " + values[col]
							                                              + " found.");
						}
						if (values[col].equals("(blank)")||values[col].equals("Unknown"))	{
							values[col] = "";
						}

					}
				} catch (Exception e) {
					Dbutil.logger.error("Problem: ", e);
					Dbutil.logger.error("Appellation: "+appellation);
					Dbutil.logger.error("Line: "+line);
					Dbutil.logger.error("Cols: ");
					for (int i=0;i<cols.length;i++) Dbutil.logger.info(i+": "+cols[i]);
					Dbutil.logger.error("Cache: ");
					for (int i=0;i<cache.length;i++) Dbutil.logger.info(i+": "+cache[i]);
					Dbutil.logger.error("col: "+col);
					Dbutil.logger.error("readfrom: "+readfrom);
				}
				if (!values[0].trim().equals("")){ //sometimes empty line

					if (type==0&&!values[0].startsWith("Unknown ")){

						query="insert ignore into ctknownwines (wine,locale,appellation,type,grapes,bottles) values ('"+Spider.SQLEscape(values[0])+"','"+Spider.SQLEscape(locale)+"','"+Spider.SQLEscape(appellation)+"','"+Spider.SQLEscape(values[1])+"','"+Spider.SQLEscape(values[2])+"',"+bottles+");";
						Dbutil.executeQuery(query);
					} 
					if (type==1&&(!cached||bottles==maxbottles)){
						query="update ctknownwines set cuvee='"+Spider.SQLEscape(values[1])+"', vineyard='"+Spider.SQLEscape(values[2])+"' where wine='"+Spider.SQLEscape(values[0])+"' and locale='"+Spider.SQLEscape(locale)+"';";
						Dbutil.executeQuery(query,con);
					} 
					if (type==2&&(!cached||bottles==maxbottles)){
						if (values[1].trim().equals("")){
							Dbutil.logger.error("Producer name empty for line "+line);
							query="delete from ctknownwines where wine='"+Spider.SQLEscape(values[0])+"' and locale='"+Spider.SQLEscape(locale)+"';";
							Dbutil.executeQuery(query,con);
						}
						if (values[2].trim().equals("")){
							Dbutil.logger.error("Locale name empty for line "+line);
							throw new Exception("N");
						}
						query="update ctknownwines set producer='"+Spider.SQLEscape(values[1])+"' where wine='"+Spider.SQLEscape(values[0])+"' and locale='"+Spider.SQLEscape(locale)+"';";
						Dbutil.executeQuery(query,con);
					} 
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
	public static void refresh(){
		Dbutil.executeQuery("update ctappellations set status=0;");
		Dbutil.executeQuery("delete from ctknownwines;");

	}
	public static String URLEncode(String input){
		String result=input;
		try{
			result=java.net.URLEncoder.encode(escape(input), "Cp1252").replace("%25", "%").replaceAll("%26amp%3B", "%26");
		} catch (Exception e){
			Dbutil.logger.error("Error: ",e);
		}
		return result;
	}

	public static String escape(String source) {
		if (source==null)
			return null;

		StringBuffer result = new StringBuffer(source.length());

		for (int i=0;i<source.length();i++) {

			char ch = source.charAt(i);

			if ((int)ch>127){
				try {
					if (!java.net.URLEncoder.encode(ch+"", "Cp1252").equals("%3F")){
						result.append(java.net.URLEncoder.encode(ch+"", "Cp1252"));
					} else {
						result.append("&#").append((int)ch).append(';');
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if ((int)ch==38) {
					result.append("&amp;");
				} else {
					if ((int)ch==60) {
						result.append("&lt;");
					} else {
						if ((int)ch==62) {
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

	public static void unescapeDB(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		Connection con2 = Dbutil.openNewConnection();
		try {
			query = "select * from ctappellations;";
			rs = Dbutil.selectQuery(rs,query, con);
			while (rs.next()) {
				query="update ctappellations set appellation='"+ Spider.SQLEscape(Spider.unescape(rs.getString("appellation")))+"' where id="+rs.getString("id");
				Dbutil.executeQuery(query,con2);
			}
			query = "select * from ctregions;";
			rs = Dbutil.selectQuery(rs,query, con);
			while (rs.next()) {
				query="update ctregions set region='"+ Spider.SQLEscape(Spider.unescape(rs.getString("region")))+"' where id="+rs.getString("id");
				Dbutil.executeQuery(query,con2);
			}
			query = "select * from ctsubregions;";
			rs = Dbutil.selectQuery(rs,query, con);
			while (rs.next()) {
				query="update ctsubregions set subregion='"+ Spider.SQLEscape(Spider.unescape(rs.getString("subregion")))+"' where id="+rs.getString("id");
				Dbutil.executeQuery(query,con2);
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public static void ripRegioninfo(String page, String sub) throws Exception{
		String[] cache=new String[3];
		String[] values=new String[3];
		Integer[] rowspan=new Integer[3];
		for (int i=0;i<=2;i++){
			cache[i]="";
			values[i]="";
			rowspan[i]=0;
		}
		Dbutil.executeQuery("truncate table ct"+sub+"regions");
		String[] lines=page.replaceAll("\n", "").split("<tr");
		String[] cols;
		int offset=2;
		int col=0;
		int readfrom=0;
		int rowspanlines;
		int bottles = 0;
		
		String query;
		for (String line:lines){
			if (line.contains("class='properties'")&&!line.contains("TOTAL COLLECTION")){
				readfrom=offset;
				cols=line.split("<td");
				try {
					col=0;
					values[col] = "";
					if (rowspan[col] > 0) {
						// Read from cache
						values[col] = cache[col];
						rowspan[col]--;
					} else {
						// read from line
						// check if colspan in element
						if (cols[readfrom].contains("rowspan=")) {
							rowspanlines = Integer.parseInt(Webroutines
									.getRegexPatternValue(
											"rowspan=.(\\d+)",
											cols[readfrom]));
							if (rowspanlines > 0) {
								rowspan[col] = rowspanlines - 1;
								cache[col] = Spider.unescape(Webroutines
										.getRegexPatternValue(">([^<]+)<",
												cols[readfrom]));
							} else {
								Dbutil.logger
								.info("Could not retrieve rowspan, element="
										+ cols[readfrom]);
							}
						}
						values[col] = Spider.unescape(Webroutines
								.getRegexPatternValue(">([^<]+)<",
										cols[readfrom]).trim());


						readfrom++;
					}
					if (values[col].equals("")
							|| !Webroutines.getRegexPatternValue("(&.{1,5};)", values[col]).equals("")) {
						Dbutil.logger.warn("Problem: value " + values[col]
						                                              + " found.");
					}
					if (values[col].equals("(blank)"))	{
						values[col] = "";
					}
					Dbutil.logger.info(values[col]);
					if (!values[col].equals("")) Dbutil.executeQuery(" insert into ct"+sub+"regions  ("+sub+"region,status) values ('"+Spider.SQLEscape(values[col])+"',0);");

				} catch (Exception e) {
					Dbutil.logger.error("Problem: ", e);
					//Dbutil.logger.error("Appellation: "+appellation);
					Dbutil.logger.error("Line: "+line);
					Dbutil.logger.error("Cols: ");
					for (int i=0;i<cols.length;i++) Dbutil.logger.info(i+": "+cols[i]);
					Dbutil.logger.error("Cache: ");
					for (int i=0;i<cache.length;i++) Dbutil.logger.info(i+": "+cache[i]);
					Dbutil.logger.error("col: "+col);
					Dbutil.logger.error("readfrom: "+readfrom);
				}
				if (!values[0].trim().equals("")){ //sometimes empty line


				}
			}
		}
	}
	public static void ripAppellationInfo() throws Exception{
		// URL voor alle locales (appellaties): http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Country&PrinterFriendly=true&iUserOverride=0&Pivot2=Locale
		File file=new File("C:\\Users\\Jasper\\Downloads\\locales.htm");
		StringBuffer sb=new StringBuffer();
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String strLine;
		//Read File Line By Line
		try {
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				sb.append (strLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Close the input stream
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String page=sb.toString();
		String[] cache=new String[3];
		String[] values=new String[3];
		Integer[] rowspan=new Integer[3];
		for (int i=0;i<=2;i++){
			cache[i]="";
			values[i]="";
			rowspan[i]=0;
		}
		Dbutil.executeQuery("truncate table ctlocales");
		String[] lines=page.replaceAll("\n", "").split("<tr");
		String[] cols;
		int offset=2;
		int col=0;
		int readfrom=0;
		int rowspanlines;
		int bottles = 0;
		String query;
		for (String line:lines){
			if (line.contains("class='properties'")&&!line.contains("TOTAL COLLECTION")){
				readfrom=offset;
				cols=line.split("<td");
				try {
					for (col = 0; col <= 2; col++) {
						values[col] = "";
						if (rowspan[col] > 0) {
							// Read from cache
							values[col] = cache[col];
							rowspan[col]--;
						} else {
							// read from line
							// check if colspan in element
							if (cols[readfrom].contains("rowspan=")) {
								rowspanlines = Integer.parseInt(Webroutines
										.getRegexPatternValue(
												"rowspan=.(\\d+)",
												cols[readfrom]));
								if (rowspanlines > 0) {
									rowspan[col] = rowspanlines - 1;
									cache[col] = Spider.unescape(Webroutines
											.getRegexPatternValue(">([^<]+)<",
													cols[readfrom]));
								} else {
									Dbutil.logger
									.info("Could not retrieve rowspan, element="
											+ cols[readfrom]);
								}
							}
							values[col] = Spider.unescape(Webroutines
									.getRegexPatternValue(">([^<]+)<",
											cols[readfrom]).trim());

							if (col==1) values[0]=  Spider.unescape(Webroutines
									.getRegexPatternValue("\\(([\\d,]+) bottle",cols[readfrom]).trim().replace(",", ""));

							if (values[col].equals("")
									|| !Webroutines.getRegexPatternValue("(&.{1,5};)", values[col]).equals("")) {
								Dbutil.logger.warn("Problem: value " + values[col]
								                                              + " found.");
							}
							if (values[col].equals("(blank)"))	{
								values[col] = "";
							}
							values[col]=values[col].trim();
							readfrom++;
						}
					}


					Dbutil.logger.info(values[1]);
					if (!values[1].trim().equals("")) Dbutil.executeQuery(" insert into ctlocales  (locale,appellation,status,bottles,priority) values ('"+Spider.SQLEscape(values[1])+"','"+Spider.SQLEscape(values[2])+"',0,"+Spider.SQLEscape(values[0])+","+Spider.SQLEscape(values[0])+");");

				} catch (Exception e) {
					Dbutil.logger.error("Problem: ", e);
					//Dbutil.logger.error("Appellation: "+appellation);
					Dbutil.logger.error("Line: "+line);
					Dbutil.logger.error("Cols: ");
					for (int i=0;i<cols.length;i++) Dbutil.logger.info(i+": "+cols[i]);
					Dbutil.logger.error("Cache: ");
					for (int i=0;i<cache.length;i++) Dbutil.logger.info(i+": "+cache[i]);
					Dbutil.logger.error("col: "+col);
					Dbutil.logger.error("readfrom: "+readfrom);
				}
				if (!values[0].trim().equals("")){ //sometimes empty line


				}
			}
		}
	}

	public static void readRegions(String sub){
		// URL voor alle locales (appellaties): http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Country&PrinterFriendly=true&iUserOverride=0&Pivot2=Locale
		File file=new File("C:\\Users\\Jasper\\Downloads\\"+sub+"regions.htm");
		StringBuffer sb=new StringBuffer();
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String strLine;
		//Read File Line By Line
		try {
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				sb.append (strLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Close the input stream
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ripRegioninfo(sb.toString(),sub);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static void processfiles(){
		CommonUtilities util=new CommonUtilities();
		
		File dir=new File("C:\\CT\\");
		for (File file:dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return (name.contains("-tv-"));
			}
		})){
			try {
				int type=0;
				if (file.getName().contains("-cv-")) type=1;
				if (file.getName().contains("-pl-")) type=2;
				String encoding="Windows-1252";
				if (file.getName().contains("utf8")) encoding="UTF-8";
				String page=util.readFile(file,encoding);
				String locale=Webroutines.getRegexPatternValue("name=['\"]Locale['\"] value='([^']+?)'", page);
				if ("".equals(locale)) locale=Webroutines.getRegexPatternValue("name=['\"]Locale['\"] value=\"([^\"]+?)\"", page);
				if ("".equals(locale)){
					Dbutil.logger.info("Empty locale!!!");
				}
				String appellation=locale.split(", ")[locale.split(", ").length-1].trim();
				ripinfo(page, locale, appellation, type);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dir=new File("C:\\CT\\");
		for (File file:dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return (name.contains("-cv-")||name.contains("-pl-"));
			}
		})){
			try {
				int type=0;
				if (file.getName().contains("-cv-")) type=1;
				if (file.getName().contains("-pl-")) type=2;
				String encoding="Windows-1252";
				if (file.getName().contains("utf8")) encoding="UTF-8";
				String page=util.readFile(file,encoding);
				String locale=Webroutines.getRegexPatternValue("name=['\"]Locale['\"] value='([^']+?)'", page);
				if ("".equals(locale)) locale=Webroutines.getRegexPatternValue("name=['\"]Locale['\"] value=\"([^\"]+?)\"", page);
				if ("".equals(locale)){
					Dbutil.logger.info("Empty locale!!!");
				}
				String appellation=locale.split(", ")[locale.split(", ").length-1].trim();
				ripinfo(page, locale, appellation, type);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	

	public static void main(String[] args){
		
		
		//readRegions(""); Met de hand gesaved: lijst
		//readRegions("sub");
		//http://www.cellartracker.com/list.asp?table=Pivot&Pivot1=Wine&Pivot2=Type&Locale=France%2C+Rh%F4ne%2C+Southern+Rh%F4ne%2C+Ch%E2teauneuf-du-Pape&fInStock=0&Pivot3=Varietal
		try {
			//ripAppellationInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//processfiles();
		//processNewKnownwines();
		new SuckCT(20);
		//new SuckCT(20,"Appellation");
		//new SuckCT(20,"SubRegion");
		//new SuckCT(20,"Region");

	}




}

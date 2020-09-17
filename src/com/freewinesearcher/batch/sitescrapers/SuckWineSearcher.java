package com.freewinesearcher.batch.sitescrapers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import com.freewinesearcher.batch.Excelreader;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Webpage;


public class SuckWineSearcher implements Runnable {

	/**
	 * 
	 */
	String auto="ws";
	String url;
	String Postdata="";
	String id="4";
	String encoding;
	public Connection executecon;
	java.sql.Timestamp now;
	boolean pagenotfound;
	public String directory="C:\\Temp\\winesearcherstores\\WSStores\\www.wine-searcher.com\\merchant";
    


	public SuckWineSearcher() {
		super();
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Dbutil.logger.info("Starting job to suck Wine-Searcher.com");
		Connection con=Dbutil.openNewConnection();
		try{ 
			File dir = new File(directory);
			// Now let's scrape as long as we find records with status Ready
			// The Scrapelist loop gets reinitialized so we see new URL's
			File[] files = dir.listFiles();
			byte[] fileasbytes;
			String filename;
			String Page;
			int n=0;
			Dbutil.logger.info("Parsing "+files.length+" files");
		    for (int i=0;i<files.length;i++){
				fileasbytes=getBytesFromFile(files[i]);
				Page=new String(fileasbytes);
				filename=files[i].getName();
				Page=Page.replace("\r", "");
				ripShopInfo(Page,con,filename);
				n++;
				Dbutil.logger.info("Store "+n);
				
				Page=null;
				fileasbytes=null;
			}
		    Dbutil.executeQuery("update wsshops join shops on (wsshops.url=shops.shopurl) set wsshops.shopid=shops.id where wsshops.shopid=0;");
		    Dbutil.executeQuery("update wsshops join shops on (wsshops.name=shops.shopname) set wsshops.shopid=shops.id where wsshops.shopid=0;");
			Dbutil.logger.info("Finished job to suck Wine-Searcher.com");

		} catch (Exception exc){
			Dbutil.logger.error("Exception while processing WineSearcher pages. ",exc);
		}
		/*Properties systemSettings = System.getProperties();
		systemSettings.put( "proxySet", "true" );
		systemSettings.put( "proxyHost", "www.freewinesearcher.com" );
		systemSettings.put( "proxyPort", "8118" );
		System.setProperties(systemSettings);
		
		Spider spider=new Spider("4","Spider","ISO-8859-1","ws");	
		String Baseurl="http://www.wine-searcher.com";
		

		Thread.currentThread().setName("Wine-Searcher");
		Dbutil.logger.info("Starting spidering of Wine-Searcher");
		now = new java.sql.Timestamp(new java.util.Date().getTime()); 
		//ArrayList<String> Urls = new ArrayList(1); //will contain all Urls found in the DB and spidered
		ArrayList Urlsfound = new ArrayList(0); // temp container for found Urls while spidering
		ArrayList<String> Urlregex = new ArrayList<String>(0);
		ArrayList<ArrayList<String>> Urllist;
		boolean moretodo;
		String Page;
		int totaldatafromweb=0;
		String Url;
		String ignorepagenotfound="Y";
		String Regex="";
		String Regexescaped="";
		String Headerregex="";
		String Headerregexescaped="";
		String Order="";
		String tablescraper="";
		String type;
		String Parenturl;
		Wine[] wine;
		Double pricefactorex;
		Double pricefactorin;
		int totalscraped=0;
		Variables var=new Variables();

		String urlregex;
		String winesep="";
		String fieldsep="";
		String filter="";
		String nameorder="";
		String nameregex="";
		String nameexclpattern="";
		String vintageregex="";
		String vintageorder="";
		String priceregex="";
		String priceorder="";
		String sizeregex="";
		String sizeorder="";
		String headerregex="";
		String postdata="";
		String AnalysisHTML="";
		
		encoding="ISO-8859-1";

		pricefactorex=1.0;
		pricefactorin=1.0;
		Urlregex.add("href=(?:'|\")(([^'\"]*?)searcher.com/merchant([^'\"]*?))(?:'|\")"); //Regex
		Urlregex.add(".*\\.jpg::.*\\.png::.*\\.gif::.*\\.\\..*::#.*::.*javascript.*:"); //Filter

		spider.updateUrlStatusses("","Delete");// Delete will only affect spidered URL's
		spider.updateUrlStatusses("","Ready");
		
		  
		    
		    moretodo=true;
			while (moretodo&&!Wijnzoeker.muststopnow){
				Urllist=getScrapeList("Ready");
				if (Urllist.size()==0) moretodo=false;
				for (int i = 0;i<Urllist.size();i++)
					if (!Wijnzoeker.muststopnow){
						Url=Urllist.get(i).get(0);
						spider.updateUrlStatus(Url,"Scraping");
						type=Urllist.get(i).get(1);
						Parenturl=Urllist.get(i).get(2);
						Urlsfound=null;
						Page= getWebPage(Url, encoding, var,"", "false");
						while (Page.contains("Excessive")){
							Page=Spider.getWebPage("http://www.whatismyip.com", encoding, new Variables(),"", "true");
							String myip=rip("<h1>[^<]*</h1>[^<]*<h1>([^<]*)</h1>",Page);
							Dbutil.logger.info("Excessive use detected for URL "+Url+" on IP address "+myip);
							Page= Spider.getWebPage("http://2.2.2.2", encoding, new Variables(),"", "true");
							Thread.sleep(100000);
							Page=Spider.getWebPage("http://www.whatismyip.com", encoding, new Variables(),"", "true");
							myip=rip("<h1>[^<]*</h1>[^<]*<h1>([^<]*)</h1>",Page);
							Dbutil.logger.info("Tryin again with IP address "+myip);
							Page= Spider.getWebPage(Url, encoding, new Variables(),"", "false");
						}
						//Thread.sleep(1000);
						totaldatafromweb+=Page.length();

						if (!Page.startsWith("Webpage")){
							//	Now we harvest all URLs if this is a master or spidered URL

							if (type.equals("Master")||type.equals("Spidered")){
								Urlsfound = Spider.ScrapeUrl(Page,Urlregex,Url,Baseurl,"1","");
								spider.addUrl(Urlsfound, Regexescaped, Headerregexescaped, "0", Order, "0","Spidered",Postdata,Url);
							}
							Urlsfound=null;

							
							//	now harvest shop info if this is a shop
							if (Url.contains("/merchant/")) {
								ripShopInfo(Page,con,Url);
								spider.updateUrlStatus(Url,"Shop");
							} else {
								spider.updateUrlStatus(Url,"No Shop");
							} 
							
						}
						Page=null;
						System.gc();
					}
				}
				
				
			} catch (Exception exc){
				Dbutil.logger.error("Iets ging mis met het spideren van wine-searcher. ",exc);
				
			}


		
		
		wine=null;
		Urlsfound=null;
		Urlregex=null;
		Urllist=null;
		Dbutil.closeConnection(con);
		Dbutil.perflogger.info(totaldatafromweb+" bytes scraped for shop (auto)"+id);
		*/
	}

	
    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


	
	
	
	private static void ripShopInfo(String Page, Connection con, String url){
	ResultSet rs;
	String shopname=Spider.SQLEscape(rip("<H1>([^<]*)</H1>",Page));
	String shopurl=Spider.SQLEscape(rip("Web Address:[^>]*>[^>]*>[^>]*>([^<]*)<",Page));
	String country=Spider.SQLEscape(rip("Country:[^>]*>[^>]*>[^>]*>.&nbsp;.([^<]*)<",Page));
	String numberofwines=Spider.SQLEscape(rip("(\\d*) listings now showing",Page));
	if (numberofwines.equals("")) numberofwines="0";
	String storetype=Spider.SQLEscape(rip("Description:[^>]*>[^>]*>([^<]*)<",Page));
	String internet=Spider.SQLEscape(rip("Internet:[^>]*>[^>]*>([^<]*)<",Page));
	String address=Spider.SQLEscape(rip("Physical Address: &nbsp;[^>]*>[^>]*>([^<]*)<",Page));
	if (address.length()==0) address=Spider.SQLEscape(rip("Address: &nbsp;[^>]*>[^>]*>([^<]*)<",Page));
	address=address.replaceAll("&nbsp;"," ").trim();
	String postaladdress=Spider.SQLEscape(rip("Postal Address: &nbsp;[^>]*>[^>]*>([^<]*)<",Page));
	postaladdress=postaladdress.replaceAll("&nbsp;"," ").trim();
	String contact=Spider.SQLEscape(rip("Contact:[^>]*>[^>]*>(.*?)</TD>",Page));
	String terms=Spider.SQLEscape(rip("General Terms: &nbsp;[^>]*>[^>]*>(.*?)</TD>",Page));
	String states=Spider.SQLEscape(rip("Will ship wine to the following states: ([^.]+).",Page));
	String exvatstr=Spider.SQLEscape(rip("Prices listed ([^ ]+) sales tax",Page));
	int exvat=2;
	if (exvatstr.length()>1&&exvatstr.equals("include")) exvat=0;
	if (exvatstr.length()>1&&exvatstr.equals("exclude")) exvat=1;
	
	String rowid="";
	String countrycode="";
	String wsid=rip("(\\d+).html",url);
	String Query;
	if (country.length()>3){
	Query="select * from vat where country like '"+country.substring(0,3)+"%' order by (country='"+country+"') desc;";
	} else {
		Query="select * from vat where country = '"+country+"';";
	}	
	rs=Dbutil.selectQuery(Query, con);
	try {
		if (rs.next()){
			countrycode=rs.getString("countrycode");
		}
	} catch (Exception exc){
		Dbutil.logger.error("Error while getting countrycode for country "+country+" in ripShopInfo. ",exc);
	}
	if ("UK".equals(country)) countrycode="UK";
	
	Query="Insert into wsshops (wsid,name,country,countrycode,url,numberofwines,storetype,contact,address,terms,shopid,internet,states,exvat,postaladdress) values ("+wsid+",'"+shopname+"','"+country+"','"+countrycode+"','"+shopurl+"','"+numberofwines+"','"+storetype+"','"+contact+"','"+address+"','"+terms+"',0,'"+internet+"','"+states+"',"+exvat+",'"+address+"') on duplicate key update name='"+shopname+"', url='"+shopurl+"',numberofwines='"+numberofwines+"',storetype='"+storetype+"',address='"+address+"',contact='"+contact+"',terms='"+terms+"', internet='"+internet+"',states='"+states+"',exvat="+exvat+",postaladdress='"+postaladdress+"';";
	//Dbutil.logger.info(Query);
	if (shopname.length()>2) Dbutil.executeQuery(Query);
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

	public ArrayList<ArrayList<String>> getScrapeList(String Status) {
		Connection con=Dbutil.openNewConnection();
		ArrayList<ArrayList<String>> urllist=new ArrayList<ArrayList<String>>(0);
		ResultSet rs = null;
		int i=0;
		try{
			//Get a list of Url's and Regex's from the Scrapelist table
			rs = Dbutil.selectQuery("SELECT * from wsscrapelist where Status like '%"+Status+"';",con);
			while (rs.next()){
				urllist.add(new ArrayList<String>(0));
				urllist.get(i).add(rs.getString("Url"));
				urllist.get(i).add(rs.getString("UrlType"));
				urllist.get(i).add(rs.getString("Parenturl"));
				i++;
			}
		}catch( Exception e ) {
			Dbutil.logger.error("Error while getting wsscrapelist. ",e);
		}
		Dbutil.closeConnection(con);
		rs=null;
		return urllist;
	}	
	public final void updateUrlStatus(String Url, String NewStatus){
		try{
			Dbutil.executeQuery(
					"update wsscrapelist " +
					"SET status='"+NewStatus+"' " +
					"WHERE Url='"+Spider.SQLEscape(Url)+"';");
			
		}catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static String rip(String regex, String Page){
		String result="";
		Matcher matcher;
		Pattern pattern;
		pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
		matcher = pattern.matcher(Page);
		if (matcher.find()){
			result=matcher.group(1);
		}
		result=result.replace("\n", " ");
		result=result.replace("&amp;", "&");
		result=result.trim();
		return result;
	}
	
	
	public static String obsoletegetWebPage(String urlstring, String encoding, Variables var, String postdata, String ignorepagenotfound) {
		String Page="";
		int attempt=0;
		int maxattempts=3;
		boolean succes=false;
		String exceptionmessage="";
		if (urlstring.endsWith("xls")){
			Page=Excelreader.ReadUrl(urlstring);
		} else {
			urlstring=Spider.replaceString(urlstring, " ", "%20");
			URL url =  null;
			HttpURLConnection urlcon;
			//long lastmodified;
			String inputLine;
			StringBuffer sb=new StringBuffer();
			try {
				url = new URL(urlstring);
			}
			catch (Exception exc){
				Dbutil.logger.warn("Foute URL "+urlstring);
				//throw new MalformedURLException();
			}
			Dbutil.logger.debug("Starting to get web page");

			while(succes==false&&attempt<maxattempts){
				try{
					attempt++;
					urlcon = (HttpURLConnection)url.openConnection();
					urlcon.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
					urlcon.setRequestProperty("Connection", "keep-alive");
					//urlcon.setInstanceFollowRedirects(false); uncomment this for shop 77 to work
					if (var!=null){
						if (var.Sessionid!=null){
							urlcon.setRequestProperty("Cookie", var.Sessionid);
							//System.out.println("Set cookie to "+var.Sessionid);
						}
					}
					if (postdata!=null&&!postdata.equals("")){
						DataOutputStream    printout;
						urlcon.setDoInput (true);
						// Let the RTS know that we want to do output.
						urlcon.setDoOutput (true);
						// No caching, we want the real thing.
						urlcon.setUseCaches (false);
						// Specify the content type.
						urlcon.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
						// Send POST output.
						printout = new DataOutputStream (urlcon.getOutputStream ());

						printout.writeBytes (postdata);
						printout.flush ();
						printout.close ();

					}


					//lastmodified=urlcon.getLastModified();
					if (encoding == null||encoding.equals("")) {
						encoding="ISO-8859-1";
					}
					if (!java.nio.charset.Charset.isSupported(encoding)){
						//Dbutil.logger.info("Charset "+encoding+" is not supported for URL "+urlstring+", trying with ISO-8859-1");
						encoding="ISO-8859-1";
					}
					//System.out.println("Cookie: "+urlcon.getHeaderField("Set-Cookie"));
					if (var!=null){
						//System.out.println("var is niet null");
						//System.out.println("var.Sessionid:"+var.Sessionid);

						if (var.Sessionid==null||var.Sessionid.equals("null")){
							var.Sessionid="";
							String cookieVal ="";
							String headerName ="";
							for (int i=1; (headerName = urlcon.getHeaderFieldKey(i))!=null; i++) {
								if (headerName.equals("Set-Cookie")) {                  
									cookieVal = urlcon.getHeaderField(i);
									cookieVal = cookieVal.substring(0, cookieVal.lastIndexOf(";")+1)+" ";
									if(cookieVal != null&&cookieVal.contains(";")){
										var.Sessionid = var.Sessionid+cookieVal; //.substring(0, cookieVal.indexOf(";"));
									}
								}
							}
						}
					}



					//System.out.println("Sessionid:"+var.Sessionid);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(urlcon.getInputStream(),encoding));
					while ((inputLine = in.readLine()) != null) {
						sb.append(inputLine);
					}

					in.close();
					//urlcon.disconnect();
					Page=sb.toString();
					sb=null;
					Dbutil.logger.debug("Page retrieved from url "+urlstring);
					succes=true;
				} catch (Exception exc){
					exceptionmessage=exc.toString();
					if (ignorepagenotfound.equalsIgnoreCase("N")){
						Dbutil.logger.debug("Cannot find web page, attempt "+attempt+ ", URL = "+urlstring,exc);
					}
				}
			}
			if (succes==false){
				if (ignorepagenotfound.equalsIgnoreCase("Y")){
					Page="Webpage can be ignored";
				} else {
					Dbutil.logger.warn("Cannot find web page after "+(attempt)+" tries, will not delete wines from this shop. Problem url= "+urlstring+", error: "+exceptionmessage);
					Page="Webpage unavailable";
				}
			}
		}

		return Page;

	}
	public static void matchShopid(){
		Dbutil.executeQuery("update shops join wsshops on (wsshops.url like concat(shops.baseurl,'%')) set wsshops.shopid=shops.id;");
	}
	

	public static void checkCpath(){
		Connection con=Dbutil.openNewConnection();
		ResultSet rs = null;
		String query;
		Webpage webpage=new Webpage();
		webpage.followredirect=true;
		String lowercase;
		webpage.maxattempts=1;
		try{
			query="Select * from wsshops where countrycode !='' and oscommerce=2 and url!='';";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				webpage=new Webpage();
				webpage.followredirect=true;
				webpage.urlstring=rs.getString("url");
				if (!"".equals(webpage.urlstring.trim())){
					webpage.readPage();
					if (webpage.html.startsWith("Webpage")){
						webpage=new Webpage();
						webpage.followredirect=true;
						webpage.urlstring=rs.getString("url")+"/";
						webpage.readPage();	
					}
					if (!webpage.html.startsWith("Webpage")){
						lowercase=webpage.html.toLowerCase();
						if (lowercase.contains("cpath")||lowercase.contains("oscsid")){
							Dbutil.executeQuery("update wsshops set oscommerce=1 where wsid="+rs.getInt("wsid")+";", con);
						} else {
							Dbutil.executeQuery("update wsshops set oscommerce=0 where wsid="+rs.getInt("wsid")+";", con);
						}
					}
				}
			}
		}catch( Exception e ) {
			Dbutil.logger.error("Error while getting wsscrapelist. ",e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		rs=null;

	}
	
	public static void main(String[] args){
		SuckWineSearcher s=new SuckWineSearcher();
		s.run();
	}

}

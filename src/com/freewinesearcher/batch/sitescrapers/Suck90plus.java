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
import com.freewinesearcher.batch.TableScraper;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;


public class Suck90plus {

	/**
	 * 
	 */
	



	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public Suck90plus() {
		Connection con=Dbutil.openNewConnection();
		Dbutil.executeQuery("delete from ratedwines where sourceurl='90pluswines.com';", con);
		try{ 
			// Now let's scrape as long as we find records with status Ready
			// The Scrapelist loop gets reinitialized so we see new URL's
			File dir = new File("C:\\temp\\90plus");
		    File[] files = dir.listFiles();
			byte[] fileasbytes;
			String filename;
			String Page;
		    for (int i=0;i<files.length;i++){
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(files[i]),"UTF-8"));
				String inputLine;
				StringBuffer sb=new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
					
				}
				in.close();
				Page=sb.toString();
				Page=Page.replace("\r", "");
				filename=files[i].getName();
				ripShopInfo(Page,con,filename);
			}
		} catch (Exception exc){
			Dbutil.logger.error("Exception while processing WineSearcher pages. ",exc);
		}
		Dbutil.closeConnection(con);
	
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
		
	String wine;
	String vintage;
	String RP;
	String WS;
	String IWC;
	String[] pageArray=Page.split("class=\"listlinks\"");
	int i=0;
	for (String line:pageArray){
		wine=Spider.SQLEscape(rip("detail.aspx\\?vintageid=[^']+'>([^<]*)</a>",line));
		if (wine!=null&&!wine.equals("")){
			vintage=Spider.SQLEscape(rip("lblYear\">(\\d\\d\\d\\d)</span>",line));
			RP=Spider.SQLEscape(rip(">WA</td></tr><tr><td align=center valign=middle >([0123456789\\--]+)</td>",line));
			WS=Spider.SQLEscape(rip(">WS</td></tr><tr><td align=center valign=middle >([0123456789\\--]+)</td>",line));
			IWC=Spider.SQLEscape(rip(">IWC</td></tr><tr><td align=center valign=middle >([0123456789\\--]+)</td>",line));
			storeScore(RP,wine,vintage,"RP",con);
			storeScore(WS,wine,vintage,"WS",con);
			storeScore(IWC,wine,vintage,"IWC",con);
		}
	}
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
	
	public static void storeScore(String score, String wine, String vintage,String author, Connection con){
		if (score!=null&&!score.equals("")){
			try{
				String low=TableScraper.matchthis(score, "(\\d+)", 1);
				String high=TableScraper.matchthis(score, "-(\\d+)", 1);
				if (high.equals("")) high="0";
				Dbutil.executeQuery("insert ignore into ratedwines (name,vintage,rating,ratinghigh,author,sourceurl,shopid,lastupdated)" +
						"values ('"+Spider.SQLEscape(wine)+"','"+vintage+"','"+low+"','"+high+"','"+author+"','90pluswines.com',0,sysdate());",con);

			}catch( Exception e ) {
				Dbutil.logger.error("Problem saving ratings: ",e);
			}
		}
	}
	
	
	
	public static String rip(String regex, String Line){
		String result="";
		Matcher matcher;
		Pattern pattern;
		pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
		matcher = pattern.matcher(Line);
		if (matcher.find()){
			result=matcher.group(1);
		}
		return result;
	}
	
	
	public static String getWebPage(String urlstring, String encoding, Variables var, String postdata, String ignorepagenotfound) {
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


}

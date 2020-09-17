package com.freewinesearcher.batch.sitescrapers;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.online.Webroutines;

public class SuckCTLabels {
	public int pause; // average pause in seconds between 2 fetches
	Proxy proxy=null;//new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(Configuration.proxyaddress,Configuration.proxyport));
	public String useragent="Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3";
	ArrayList<String> urls;
	public static final Pattern picturepattern=Pattern.compile("img src='labels/(\\d+.(jpg|gif|png))'");
	Matcher matcher;
	public int knownwineid;
	public String winename;
	boolean ok;
	boolean ready=false;
	Webpage webpage=new Webpage();
	public int limit=6;

	public SuckCTLabels(){

	}

	public SuckCTLabels(int pause){
		this.pause=pause;
		CommonUtilities util=new CommonUtilities();
		util.dirname="C:\\CT";
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();

		try{
			while (!ready){
				query="select knownwines.*, labels.knownwineid as d from knownwines left join labels on (knownwines.id=labels.knownwineid) having d is null order by numberofwines desc limit 1;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					winename=rs.getString("wine");
					knownwineid=rs.getInt("id");
					Dbutil.closeRs(rs);
					spider();
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			ready=true;
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}



	}

	public void spider() throws Exception{
		int rowid=0;
		String url;
		webpage.proxy=proxy; 
		Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
		webpage=new Webpage();
		webpage.proxy=proxy;
		url="http://www.cellartracker.com/list.asp?Table=LabelImage&Page=0&Wine="+SuckCT.URLEncode(winename);
		webpage.urlstring=url;
		webpage.readPage();
		ok=pageIsOK(webpage);

		if (ok){
			urls=new ArrayList<String>();
			matcher=picturepattern.matcher(webpage.html);
			while (matcher.find()){
				urls.add("http://www.cellartracker.com/labels/"+matcher.group(1));
			}
			if (urls.size()==0){


				url="http://www.cellartracker.com/list.asp?Table=LabelImage&Page=0&szSearch="+SuckCT.URLEncode(winename);
				webpage.urlstring=url;
				webpage.readPage();
				ok=pageIsOK(webpage);

				if (ok){
					urls=new ArrayList<String>();
					matcher=picturepattern.matcher(webpage.html);
					while (matcher.find()){
						urls.add("http://www.cellartracker.com/labels/"+matcher.group(1));
					}
				}
			}
			ok=ripinfo();


		} else {
			ready=true;
		}

		if (ok){
			Dbutil.executeQuery("insert ignore into labels (knownwineid,sourceurl) values ("+knownwineid+",'"+Spider.SQLEscape(url)+"');");
		} else {

		}


	} 


	public static boolean pageIsOK(Webpage webpage){
		if (!webpage.html.contains("IMAGES PENDING")) return false;
		//if (webpage.responsecode!=200) return false;
		return true;
	}

	public boolean ripinfo() throws Exception{
		InputStream is=null;
		boolean ok=true;
		String url;
		int labels=0;
		for (int n=urls.size()-1;n>=0&&labels<limit;n--){
			url=urls.get(n);
			Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));

			try{
				is=getInputStream(url);
				String extension=Webroutines.getRegexPatternValue("\\.(...)$", url);
				String labelnr=Webroutines.getRegexPatternValue("(\\d+)\\....$", url);
				if (extension.equals("gif")) extension="png";// in 1.5 writing GIF is not supported
				String dirname="C:\\labels\\CT\\"+knownwineid;
				new File(dirname).mkdirs();
				String filename=dirname+"\\"+labelnr+"."+extension.toLowerCase();
				File file=new File(filename);
				if (!file.exists()){
					BufferedImage image = null;
					image = ImageIO.read(is);
					if (image.getHeight()>0)  {
						ImageIO.write(image, extension.toLowerCase(), file);
						labels++;
					}

				}
			}catch (Exception e){
				Dbutil.logger.error("Could not read label "+url,e);
				throw new Exception();

			} finally{
				if (is!=null)
					try {
						is.close();
					} catch (IOException e) {

					}
			}
		}
		if (labels>0) return ok;
		Dbutil.logger.info("No labels found for "+winename+" (knownwineid "+knownwineid+")");
		Dbutil.executeQuery("insert ignore into labels (knownwineid,sourceurl) values ("+knownwineid+",'Error');");
		return false;
	}
	public static void refresh(){
		Dbutil.executeQuery("update kbappellations set status=0;");
		Dbutil.executeQuery("delete from kbknownwines;");

	}

	public  HttpURLConnection getUrlConnection(String urlstring) throws IOException {
		URL url =  new URL(urlstring);
		HttpURLConnection urlcon=null;


		if (proxy==null){
			urlcon = (HttpURLConnection)url.openConnection();
		} else {
			urlcon = (HttpURLConnection)url.openConnection(proxy);
		}
		urlcon.setConnectTimeout(Configuration.webpagetimeout);
		urlcon.setReadTimeout(Configuration.webpagetimeout);
		urlcon.setRequestProperty("User-Agent",useragent);
		return urlcon;
		
	}
	
	public InputStream getInputStream(String urlstring) throws IOException {
		HttpURLConnection urlcon=getUrlConnection(urlstring);
		

		
		try{
			return urlcon.getInputStream();
		} catch (Exception e){
			Coordinates.doPause((int)(pause*200+Math.round(Math.random()*pause*1800)));
			urlcon.disconnect();
			urlcon=getUrlConnection(urlstring);
			
			return urlcon.getInputStream();
		}
	}


	public static void main(String[] args){
		new SuckCTLabels(5);
		//new SuckCT(200,"subregion");
		//new SuckCT(200);

	}






}

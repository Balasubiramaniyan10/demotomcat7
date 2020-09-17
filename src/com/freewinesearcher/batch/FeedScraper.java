package com.freewinesearcher.batch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.common.datafeeds.DataFeed;
import com.freewinesearcher.common.datafeeds.DataFeed.formats;


public class FeedScraper {
	public DataFeed feed=null; 
	public int shopid=0; 
	public DataFeed.formats format=null; 
	public String url=""; 
	public String formatstring=""; 
	public String encoding=""; 
	public String shopurl=""; 
	private String itemsepconfig=""; 
	public String itemsep;
	public String postdata=""; 
	public String urlregex=""; 
	public String urlorder=""; 
	public String headerregex=""; 
	public String delimiter=""; 
	public String filter=""; 
	public String nameorder=""; 
	public String nameregex=""; 
	public String nameexclpattern=""; 
	public String vintageorder=""; 
	public String vintageregex=""; 
	public String priceorder=""; 
	public String priceregex=""; 
	public String sizeorder=""; 
	public String sizeregex=""; 
	java.sql.Timestamp now; 
	public Double pricefactorex=1.0; 
	public Double pricefactorin=1.0; 
	boolean assumebottlesize;
	public String auto="";
	public String row;

	public FeedScraper(){
	}

	public void analyzeFeed(){
		if (!"".equals(url)&&url!=null){
			Pattern pattern;
			Matcher matcher;
			String Page=Spider.getWebPage(url, "", new Variables(), "", "True", true);
			if (!Page.startsWith("Webpage")){
			try{
				feed=null;
				if (Page.toLowerCase().contains("<?xml")){
					///XML format
					if (Page.contains("<rss version=\"2.0\"")){
						feed=new DataFeed();
						feed.feed=Page;
						if (Page.contains("xmlns:g=\"http://base.google.com/ns/1.0\">")){
							feed.format=DataFeed.formats.GoogleRSS20;
						} else {
							feed.format=DataFeed.formats.RSS20;
						}
					}
					else if (Page.contains("<feed version=\"0.3\"")&&Page.contains("xmlns=\"http://purl.org/atom/ns#\"")){
						feed=new DataFeed();
						feed.feed=Page;
						if (Page.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")){
							feed.format=DataFeed.formats.GoogleAtom03;
						} else {
							feed.format=DataFeed.formats.Atom03;
						}
						feed.setItemsep("entry");
					}
					else if (Page.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\"")){
						feed=new DataFeed();
						feed.feed=Page;
						if (Page.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")){
							feed.format=DataFeed.formats.GoogleAtom10;
						} else {
							feed.format=DataFeed.formats.Atom10;
						}
						feed.setItemsep("entry");
					}
					else if (Page.contains("<rdf:RDF")&&Page.contains("xmlns=\"http://purl.org/rss/1.0/\"")){
						feed=new DataFeed();
						feed.feed=Page;
						if (Page.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")){
							feed.format=DataFeed.formats.GoogleRSS10;
						} else {
							feed.format=DataFeed.formats.RSS10;
						}
					}
					else {
						feed=new DataFeed();
						feed.format=formats.OtherXML;
						feed.feed=Page;
						
					}
				} else {
					//Flat File Format
					String line1="";
					String line2="";
					pattern=Pattern.compile("^(.*?)\n", Pattern.MULTILINE+Pattern.DOTALL);
					matcher=pattern.matcher(Page);
					if (matcher.find()){
						line1=matcher.group(1);
					}
					pattern=Pattern.compile("^.*?\n(.*?)\n", Pattern.MULTILINE+Pattern.DOTALL);
					matcher=pattern.matcher(Page);
					if (matcher.find()){
						line2=matcher.group(1);
					}
					String both=line1+line2;
					char delimiter;
					int tab=0;
					delimiter= '\t';
					for(int i = 0; i < both.length(); i++) {
						char next = both.charAt(i);
						if(next == delimiter) {
							tab++;
						}
					}
					int comma=0;
					delimiter= ',';
					for(int i = 0; i < both.length(); i++) {
						char next = both.charAt(i);
						if(next == delimiter) {
							comma++;
						}
					}int semicolon=0;
					delimiter= ';';
					for(int i = 0; i < both.length(); i++) {
						char next = both.charAt(i);
						if(next == delimiter) {
							semicolon++;
						}
					}
					int pipe=0;
					delimiter= '|';
					for(int i = 0; i < both.length(); i++) {
						char next = both.charAt(i);
						if(next == delimiter) {
							pipe++;
						}
					}
					if (tab>5||comma>5){
						if (tab>=comma&&tab>=semicolon&&tab>=pipe){
							//Tab
							feed=new DataFeed();
							feed.feed=Page;
							feed.setDelimiter("\t");
							this.delimiter="\t";
						} else if (comma>=semicolon&&comma>=pipe){
							//Comma
							feed=new DataFeed();
							feed.feed=Page;
							feed.setDelimiter(",");
							this.delimiter=",";
						} else if (semicolon>=pipe){
							//Semicolo
							feed=new DataFeed();
							feed.feed=Page;
							feed.setDelimiter(";");
							this.delimiter=";";
						} else {
							//Pipe
							feed=new DataFeed();
							feed.feed=Page;
							feed.setDelimiter("|");
							this.delimiter="|";
						}
						recognizeFlatFile();
					} else {
						feed=new DataFeed();
						feed.feed="";
						feed.setDelimiter("");
					}
					feed.setUniformfeed("");
					if (feed.format==null) feed.format=DataFeed.formats.FlatFile;
					feed.setItemsep("item");
				}
				format=feed.format;
				if(getPriceregex().equals("")) setPriceregex("(?:.*?\\D)??(\\d*[,.]?\\d+[,.]\\d+)(?:\\D.*?)?");
				if(getVintageregex().equals("")) setVintageregex("(?:.*?\\D)?((18\\d\\d)|(19\\d\\d)|(200\\d))(?:\\D.*?)?");
				if(getUrlregex().equals("")) setUrlregex("(https?://([^'\" ><]*))");
				if (feed.format.toString().startsWith("Google")){
					if (nameorder.equals("")) nameorder="title;description;summary";
					if (vintageorder.equals("")) vintageorder="title;description;summary;year";
					if (priceorder.equals("")) priceorder="g:price";
					if (sizeorder.equals("")) sizeorder="g:size;title;description;summary";
					if (urlorder.equals("")) urlorder="link";
					
				}
				
			} catch (Exception e){
				Dbutil.logger.error("Could not analyze data feed. ",e);
			}
			} else {
				// Cannot reach URL
				Dbutil.logger.info("Could not analyze data feed: cannot access url "+url);
			}
		}
	}

	public void recognizeFlatFile(){
		if (delimiter.equals("\t")&&feed.feed.contains("product_name")&&feed.feed.contains("price")&&feed.feed.contains("URL")){
			feed.format=format.MySimon;
			if (nameorder.equals("")) nameorder="product_name";
			if (vintageorder.equals("")) vintageorder="product_name";
			if (priceorder.equals("")) priceorder="price";
			if (sizeorder.equals("")) sizeorder="product_name";
			if (urlorder.equals("")) urlorder="URL";
		} else if (delimiter.equals(",")&&feed.feed.contains("Product Description")&&feed.feed.contains("Product Price")&&feed.feed.startsWith("MPN")){
			feed.format=format.Shopping;
			if (nameorder.equals("")) nameorder="Product_Description";
			if (vintageorder.equals("")) vintageorder="Product_Description";
			if (priceorder.equals("")) priceorder="Product_Price";
			if (sizeorder.equals("")) sizeorder="Product_Description";
			if (urlorder.equals("")) urlorder="Product_URL";
		} else if (delimiter.equals("\t")&&feed.feed.contains("product-url")&&feed.feed.contains("description")&&feed.feed.startsWith("code")){
			feed.format=format.Yahoo;
			if (nameorder.equals("")) nameorder="name;description";
			if (vintageorder.equals("")) vintageorder="name;description";
			if (priceorder.equals("")) priceorder="price";
			if (sizeorder.equals("")) sizeorder="name;description";
			if (urlorder.equals("")) urlorder="product-url";
		}
	}

	public void getFeedScraper(String row){
		this.row=row;
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();

		rs=Dbutil.selectQuery("Select * from "+auto+"scrapelist where id="+row+";",con);
		try { 
			if (rs.next()){
				url=rs.getString("url");
				int scrapelistrow=rs.getInt("feedscraper");
				rs=Dbutil.selectQuery("Select * from "+auto+"feedscraper where id="+scrapelistrow+";",con);
				if (rs.next()){
					urlregex=rs.getString("urlregex");
					urlorder=rs.getString("urlorder");
					itemsepconfig=rs.getString("itemsep");
					delimiter=rs.getString("delimiter");
					filter=rs.getString("filter");
					nameorder=rs.getString("nameorder");
					nameregex=rs.getString("nameregex");
					nameexclpattern=rs.getString("nameexclpattern");
					vintageorder=rs.getString("vintageorder");
					vintageregex=rs.getString("vintageregex");
					priceorder=rs.getString("priceorder");
					priceregex=rs.getString("priceregex");
					sizeorder=rs.getString("sizeorder");
					sizeregex=rs.getString("sizeregex");
					assumebottlesize=rs.getBoolean("assumebottlesize");
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("No record found for "+auto+"feedscraper "+row,exc);
		}

		Dbutil.closeConnection(con);
	}	




	public Wine[] ScrapeWine(){
		Dbutil.logger.debug("FeedScraping wines from page: "+url);
		ArrayList<Winerating> ratings=null;
		String name = null, vintage = null, wineurl=null;
		float price = 0;
		float size=0;
		String priceString="";
		boolean rareold=Wine.isRareOld(url);	
		ArrayList<Wine> WineAL=new ArrayList<Wine>();
		String timestampstring="";
		if (now!=null){
			timestampstring=now.toString();
		}
		String[] wines=feed.getUniformFeed().split(feed.getItemsep());
		for (int i=0;i< wines.length;i++){
			priceString=buildMatch(wines[i], priceorder, priceregex, "","Price");
			price=0;
			try{
				if (priceString.length()>2) {
					if (priceString.contains(",")||priceString.contains(".")){
						if (priceString.substring((priceString.length()-3),(priceString.length()-2)).equals(".")||priceString.substring((priceString.length()-3),(priceString.length()-2)).equals(",")){
							priceString=priceString.replaceAll("\\D","");
							priceString=Spider.replaceString(priceString,".","");
							priceString=Spider.replaceString(priceString,",","");
							priceString=Spider.replaceString(priceString,"'","");
							price=Float.valueOf(priceString).floatValue()/100;
						} else {
							if (priceString.substring((priceString.length()-2),(priceString.length()-1)).equals(".")||priceString.substring((priceString.length()-2),(priceString.length()-1)).equals(",")){
								priceString=priceString.replaceAll("\\D","");
								priceString=Spider.replaceString(priceString,".","");
								priceString=Spider.replaceString(priceString,",","");
								price=Float.valueOf(priceString).floatValue()/10;
							}	else {
								if (priceString.substring((priceString.length()-4),(priceString.length()-3)).equals(".")||priceString.substring((priceString.length()-4),(priceString.length()-3)).equals(",")){
									priceString=priceString.replaceAll("\\D","");
									priceString=Spider.replaceString(priceString,".","");
									priceString=Spider.replaceString(priceString,",","");
									price=Float.valueOf(priceString).floatValue();
								}
							}
						}
					}
				}
				if (price==0) {
					price=Float.valueOf(priceString.replaceAll(",",".")).floatValue();
				}
				if (price>1.5){
					name=buildMatch(wines[i], nameorder,nameregex,nameexclpattern,"Name");

					if (name.length()>2){
						if (name.length()>254)	name=name.substring(0,254);
						vintage=buildMatch(wines[i], vintageorder, vintageregex,"","Vintage");
						size=getSize(wines[i], sizeorder, price);
						if(assumebottlesize&&size==0){
							size=new Float(0.75);
						}
						wineurl=buildMatch(wines[i],urlorder,urlregex,"","Url");
						if (wineurl.equals("")) wineurl=shopurl;
						//ratings=getRating(wines.get(i),ratingscraper,Page);
						if (size==price) {
							size=0; // Oops, we mistook the price for the size...
						}
						WineAL.add(new Wine(name,vintage,size,price,price*pricefactorex,price*pricefactorin,0.0,wineurl,shopid,"","",timestampstring, timestampstring,null, rareold,"",0,ratings));
					}					
				}
			} catch (NumberFormatException e) {
				Dbutil.logger.debug("Could not parse price with priceString "+priceString+" at URL "+url);
			}	catch (Exception e) {
				Dbutil.logger.error("Problem scraping wine",e);
			}
		}


		Wine[] Wine=new Wine[WineAL.size()];

		for (int j=0;j<WineAL.size();j++){
			Wine[j]=WineAL.get(j);
		}

		Dbutil.logger.debug("Finished scraping wines");

		return Wine;




	}	

	public static String buildMatch(String record,String fields, String regex, String exclusionpattern, String field){
		String resultString="";
		String value;
		String priceString;
		String header="";
		String totalregex="";
		String[] elements=new String[0];
		Pattern pattern;
		Matcher matcher;

		elements = fields.split(";");
		if (elements.length==1&&"".equals(elements[0])){
			elements[0]="[^>]+";
		}
		if (regex.equals("")) regex="(.*?)";
		for (int i=0;i<elements.length;i++) {
			if (field.equals("Name")||resultString.equals("")){ // With vintage and price, take the first hit
				if (elements[i]!=null){
					value="";
					totalregex="< *"+elements[i]+"(?: [^>]*)*>"+regex+"</";
					pattern = Pattern.compile(totalregex,Pattern.DOTALL);
					matcher = pattern.matcher(record);
					if (matcher.find()){
						value=matcher.group(1);
					} else {
						totalregex="< *"+elements[i]+"(?: [^>]*)*?"+regex+"(?: [^>]*)*?/ ?>";
						pattern = Pattern.compile(totalregex,Pattern.DOTALL);
						matcher = pattern.matcher(record);
						if (matcher.find()){
							value=matcher.group(1);
						}
					}
					if (!value.equals("")){
						if (field.equals("Vintage")&&value.equals("1855")) value="";
						if (field.equals("Vintage")&&value.length()==2){
							// Y2K problem
							if (Integer.parseInt(value)>70){
								value="19"+value;
							}else if (Integer.parseInt(value)<10) {
								value="20"+value;
							} else {
								value=""; //Probably not a year
							}
						}
						resultString=resultString+value+" ";
					}
				}
			}
		} 

		resultString=TableScraper.filterWineName(resultString,exclusionpattern,null);





		return resultString;

	}

	public static float getSize(String record,String fields,float price){
		float size=0;
		String[] elements=new String[0];
		Pattern pattern;
		Matcher matcher;
		String totalregex="";

		elements = fields.split(";");

		for (int i=0;i<elements.length;i++) {
			if (size==0){ // With size, take the first hit
				if (elements[i]!=null){
					// Now loop over the different bottle regular expressions
					for (int j=0; j<Configuration.sizeregex.length;j++ ){
						if (size==0){ // With size, take the first hit
							totalregex="<"+elements[i]+"[^><]*>"+Configuration.sizeregex[j].replace("^","")+"<";
							pattern = Pattern.compile(totalregex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
							matcher = pattern.matcher(record);
							while (size==0&&matcher.find()){
								if (!(Configuration.size[j]==price)){
									size=Configuration.size[j];
								}
							}

						}

					}
				}
			}
		}


		return size;

	}

	public boolean addFeedScrapeRow(){
		boolean succes=false;
		int i;
		String encoding=null;
		String completeUrl;
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		try{

			Dbutil.executeQuery("Update "+auto+"shops set URLtype = 'Fixed' where id="+shopid+";");

			// Get encoding (to do: in separate routine)
			rs = Dbutil.selectQuery("SELECT Encoding from "+auto+"shops where id='"+shopid+"';",con);
			if (!rs.next()||rs.getString("Encoding").equals("")){
				encoding=Spider.getHtmlEncoding(url);
				i = Dbutil.executeQuery(
						"Update "+auto+"shops set encoding = '"+encoding+"' where id="+shopid+";");
			}

			if (row.equals("0")){

				i=Dbutil.executeQuery(
						"Insert into "+auto+"feedscraper (shopid,format,itemsep,delimiter,filter,nameorder,nameregex,nameexclpattern,vintageorder,vintageregex,priceorder,priceregex,sizeorder,sizeregex, urlregex,urlorder,assumebottlesize) " +
						"values ('"+shopid+"', '"+Spider.SQLEscape(format.toString())+"', '"+Spider.SQLEscape(itemsepconfig)+"', '"+Spider.SQLEscape(delimiter)+"', '"+Spider.SQLEscape(filter)+"', '"+Spider.SQLEscape(nameorder)+"', '"+Spider.SQLEscape(nameregex)+"', '"+Spider.SQLEscape(nameexclpattern)+"', '"+Spider.SQLEscape(vintageorder)+"', '"+Spider.SQLEscape(vintageregex)+"', '"+Spider.SQLEscape(priceorder)+"', '"+Spider.SQLEscape(priceregex)+"', '"+Spider.SQLEscape(sizeorder)+"', '"+Spider.SQLEscape(sizeregex)+"', '"+Spider.SQLEscape(urlregex)+"', '"+Spider.SQLEscape(urlorder)+"',"+assumebottlesize+");",con);
				if (i>0){
					rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID();",con);
					rs.next();
					i= rs.getInt(1);
					Dbutil.executeQuery("delete from "+auto+"scrapelist where shopid="+shopid+" and url='"+url+"';");

					i = Dbutil.executeQuery(
							"Insert into "+auto+"scrapelist (Url, Postdata, Headerregex, regex, scrapeorder,parenturl,Shopid, URLType, feedscraper, Status) " +
							"values ('"+Spider.SQLEscape(url)+"', '"+Spider.SQLEscape(postdata)+"', '', '','','', '"+shopid+"', 'Fixed', '"+i+"', 'Ready');");
					if (i!=0) succes=true;
				}
			} else {
				String feedscraperow=Dbutil.readValueFromDB("select feedscraper from scrapelist where id="+row+";", "feedscraper");
				Dbutil.executeQuery(
						"Update "+auto+"scrapelist set url='"+Spider.SQLEscape(url)+"' where id="+row+";");
				int j = Dbutil.executeQuery(
						"Update "+auto+"feedscraper set itemsep='"+Spider.SQLEscape(itemsepconfig)+"', format='"+format.toString()+"', delimiter='"+Spider.SQLEscape(delimiter)+"', filter='"+Spider.SQLEscape(filter)+"', nameorder='"+Spider.SQLEscape(nameorder)+"', nameregex='"+Spider.SQLEscape(nameregex)+"', nameexclpattern='"+Spider.SQLEscape(nameexclpattern)+"', vintageorder='"+Spider.SQLEscape(vintageorder)+"', vintageregex='"+Spider.SQLEscape(vintageregex)+"', priceorder='"+Spider.SQLEscape(priceorder)+"', priceregex='"+Spider.SQLEscape(priceregex)+"', sizeorder='"+Spider.SQLEscape(sizeorder)+"', sizeregex='"+Spider.SQLEscape(sizeregex)+"', urlregex='"+Spider.SQLEscape(urlregex)+"', urlorder='"+Spider.SQLEscape(urlorder)+"', assumebottlesize="+assumebottlesize+" where id="+feedscraperow+";");

				if (j!=0) succes=true;
			}



		}catch( Exception e ) {
			Dbutil.logger.error("Could not save record in Scrapelist ", e);
		}
		Dbutil.closeConnection(con);	
		return succes;
	}


	public boolean isAssumebottlesize() {
		return assumebottlesize;
	}


	public void setAssumebottlesize(boolean assumebottlesize) {
		this.assumebottlesize = assumebottlesize;
	}


	public DataFeed getFeed() {
		if (feed==null||feed.feed==null||feed.feed.equals("")){
			if (url!=null&&!"".equals(url)){
				String page=Spider.getWebPage(url, encoding, new Variables(), postdata, "false");
				feed=new DataFeed(page,format);
				if (itemsep!=null) feed.setItemsep(itemsep);
			}
		}
		return feed;
	}


	public void setFeed(DataFeed feed) {
		this.feed = feed;
	}


	public String getDelimiter() {
		return delimiter;
	}


	public void setDelimiter(String delimiter) {
		if (delimiter.equals("[TAB]")) delimiter="\t";
		this.delimiter = delimiter;
	}


	public String getFilter() {
		return filter;
	}


	public void setFilter(String filter) {
		this.filter = filter;
	}


	public String getItemsep() {
		return itemsep;
	}


	public void setItemsep(String itemsep) {
		this.itemsep = itemsep;
		if (getFeed()!=null) getFeed().setItemsep(itemsep);
	}


	public String getHeaderregex() {
		return headerregex;
	}


	public void setHeaderregex(String headerregex) {
		this.headerregex = headerregex;
	}


	public String getNameexclpattern() {
		return nameexclpattern;
	}


	public void setNameexclpattern(String nameexclpattern) {
		this.nameexclpattern = nameexclpattern;
	}


	public String getNameorder() {
		return nameorder;
	}


	public void setNameorder(String nameorder) {
		this.nameorder = nameorder;
	}


	public String getNameregex() {
		return nameregex;
	}


	public void setNameregex(String nameregex) {
		this.nameregex = nameregex;
	}


	public java.sql.Timestamp getNow() {
		return now;
	}


	public void setNow(java.sql.Timestamp now) {
		this.now = now;
	}


	public Double getPricefactorex() {
		return pricefactorex;
	}


	public void setPricefactorex(Double pricefactorex) {
		this.pricefactorex = pricefactorex;
	}


	public Double getPricefactorin() {
		return pricefactorin;
	}


	public void setPricefactorin(Double pricefactorin) {
		this.pricefactorin = pricefactorin;
	}


	public String getPriceorder() {
		return priceorder;
	}


	public void setPriceorder(String priceorder) {
		this.priceorder = priceorder;
	}


	public String getPriceregex() {
		return priceregex;
	}


	public void setPriceregex(String priceregex) {
		this.priceregex = priceregex;
	}


	public int getShopid() {
		return shopid;
	}


	public void setShopid(int shopid) {
		this.shopid = shopid;
	}


	public String getShopurl() {
		return shopurl;
	}


	public void setShopurl(String shopurl) {
		this.shopurl = shopurl;
	}


	public String getSizeorder() {
		return sizeorder;
	}


	public void setSizeorder(String sizeorder) {
		this.sizeorder = sizeorder;
	}


	public String getSizeregex() {
		return sizeregex;
	}


	public void setSizeregex(String sizeregex) {
		this.sizeregex = sizeregex;
	}



	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getUrlorder() {
		return urlorder;
	}


	public void setUrlorder(String urlorder) {
		this.urlorder = urlorder;
	}


	public String getUrlregex() {
		return urlregex;
	}


	public void setUrlregex(String urlregex) {
		this.urlregex = urlregex;
	}


	public String getVintageorder() {
		return vintageorder;
	}


	public void setVintageorder(String vintageorder) {
		this.vintageorder = vintageorder;
	}


	public String getVintageregex() {
		return vintageregex;
	}


	public void setVintageregex(String vintageregex) {
		this.vintageregex = vintageregex;
	}



	public String getAuto() {
		return auto;
	}




	public void setAuto(String auto) {
		this.auto = auto;
	}




	public String getPostdata() {
		return postdata;
	}




	public void setPostdata(String postdata) {
		this.postdata = postdata;
	}




	public String getRow() {
		if (row==null) row="0";
		return row;
	}



	public DataFeed.formats getFormat() {
		return format;
	}

	public void setFormatstring(String formatstr) {
		this.formatstring = formatstr;
		this.format=format.valueOf(formatstr);
	}




}

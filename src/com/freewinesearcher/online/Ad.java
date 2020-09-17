package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Random;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.common.Wijnzoeker;

public class Ad  implements Runnable{
	public int id=0;
	public int rotatingbannerid=0;
	public String html="";
	public int partner=0;
	boolean https=false;
	String google_color_border = "FFFFFF";
	String google_color_bg = "FFFFFF";
	String google_color_link = "0000FF";
	String google_color_text = "000000";
	String google_color_url = "0000FF";
	int hsize;
	int vsize;
	String country;
	String region;
	int knownwineid;
	String skipthesepartners;
	public String bannersshown="";
	int version=2;
	boolean logbanner=true;
	boolean bot=false;
	
	
	

	public Ad(int hsize, int vsize, String country, String region, int knownwineid, String skipthesepartners){
		html=new Ad(hsize,vsize,country,region,knownwineid,skipthesepartners,false).html;

	}

	public Ad(boolean bot,String design,int hsize, int vsize, String country, String region, int knownwineid, String skipthesepartners) {
		super();
		this.country = country;
		this.hsize = hsize;
		this.knownwineid = knownwineid;
		this.region = region;
		this.skipthesepartners = skipthesepartners;
		this.vsize = vsize;
		this.bot=bot;
		if (design.equals("suggest")){
			this.version=3;
		} 
		getHTML();
	}
	
	public Ad(String design,int hsize, int vsize, String country, String region, int knownwineid, String skipthesepartners) {
		super();
		this.country = country;
		this.hsize = hsize;
		this.knownwineid = knownwineid;
		this.region = region;
		this.skipthesepartners = skipthesepartners;
		this.vsize = vsize;
		if (design.equals("suggest")){
			this.version=3;
		} 
		getHTML();
	}



	public Ad(int hsize, int vsize, String country, String region, int knownwineid, String skipthesepartners, boolean secure) {
		super();
		this.country = country;
		this.hsize = hsize;
		this.knownwineid = knownwineid;
		this.region = region;
		this.skipthesepartners = skipthesepartners;
		this.vsize = vsize;
		getHTML();
	}

	public void getHTML(){
		//if (secure) https=true;
		int customads=Dbutil.readIntValueFromDB("select * from config where configkey='customads';", "value");
		if (customads==0) customads=30;
		ResultSet rs=null;
		String query;
		Connection con=Dbutil.openNewConnection();

		try{
			if (hsize==0) hsize=9999;
			if (vsize==0) vsize=9999;
			int lft=Region.getLft(knownwineid);
			int rgt=Region.getRgt(knownwineid);
			if (version==3){
				RecommendationAd ad=new RecommendationAd(knownwineid,country);
				html=ad.getAd(null,"");
				if (html.length()>20) id=-1;
			}
			if (hsize==1000){
				html=getRotatingBannerHtml();
				id=-1;
				if (html.length()>20) id=-1;
			}
			if (true){
				
				/*
				ArrayList<String> regions=Region.getRegionPath(region);
				String standardwhere="hsize<="+hsize+" and vsize<="+vsize+" and active=true";
				if (!skipthesepartners.equals("")) standardwhere=standardwhere+" and partnerid not in ("+skipthesepartners+")"; 

				// Priority 1 : individual wine, country of visitor matching
				if (id==0&&knownwineid>0){
					query="SELECT * FROM banners where "+standardwhere+" and country like '%"+country+"%' and knownwineid="+knownwineid+"  ORDER BY RAND() LIMIT 1;";
					rs=Dbutil.selectQuery(query,con);
					if (rs.next()) id=rs.getInt("id");
				}
				// Priority 2 : individual wine, country of visitor =any
				if (id==0&&knownwineid>0){
					query="SELECT * FROM banners where "+standardwhere+" and knownwineid="+knownwineid+" and country='' ORDER BY RAND() LIMIT 1;";
					rs=Dbutil.selectQuery(query,con);
					if (rs.next()) id=rs.getInt("id");
				}
				// Priority 3 : region matching, country of visitor matching
				// Select the region best matching to the required region, otherwise move up the region tree
				if (id==0&&!region.equals("")){
					if(rgt>0){
						query="SELECT * FROM banners where "+standardwhere+" and country like '%"+country+"%' and lft<='"+lft+"' and rgt>='"+rgt+"' ORDER BY RAND() LIMIT 1;";
						rs=Dbutil.selectQuery(query,con);
						if (rs.next()) id=rs.getInt("id");

					}
				}
				// Priority 4 : region matching, country of visitor =any
				// Select the region best matching to the required region, otherwise move up the region tree
				if (id==0&&!region.equals("")){
					if(rgt>0){
						query="SELECT * FROM banners where "+standardwhere+" and lft<='"+lft+"' and rgt>='"+rgt+"' ORDER BY RAND() LIMIT 1;";
						rs=Dbutil.selectQuery(query,con);
						if (rs.next()) id=rs.getInt("id");

					}

				}
				// Priority 5 : country of visitor matching, region and individual wine=any
				if (id==0){
					query="SELECT * FROM banners where "+standardwhere+" and country like '%"+country+"%' and knownwineid=0 and rgt=0 ORDER BY RAND() LIMIT 1;";
					rs=Dbutil.selectQuery(query,con);
					if (rs.next()) id=rs.getInt("id");
				}

				// Lowest priority: get any ad with no specific requirements 
				// Choose between different options
				// 30% chance of a custom ad
				int choice=new Random().nextInt(100);
				
				//Dbutil.logger.info("Choice="+choice);
				if (id==0&&choice<customads){
					query="SELECT * FROM banners where "+standardwhere+" and country='' and knownwineid=0 and rgt=0 "+(knownwineid>0?"":" and html not like '%@WINENAME@%' ")+" ORDER BY RAND() LIMIT 1;";
					rs=Dbutil.selectQuery(query,con);
					if (rs.next()) id=rs.getInt("id");
				}
				
				if (id>0){
					query="SELECT * FROM banners where id="+id+";";
					rs=Dbutil.selectQuery(query,con);
					html+="<div class='bannerad'>";
					if (rs.next()){ 
						partner=rs.getInt("partnerid");
						if (rs.getString("link").equals("")){
							html=html+rs.getString("html");
						} else {
							html=html+"<a href='/adhandler.jsp?id="+rs.getString("id")+"' target='_blank'>";
							html=html+rs.getString("html");
							html=html+"</a>";
						}
						html=html+"<font style='font-face:Arial,sans-serif;font-size: 11px;'><a href='/advertising.jsp'><br/>Ads by Vinopedia</a><br/></font></div>\n";
						Dbutil.executeQuery("Update banners set views=views+1 where id="+rs.getString("id")+";");
					}
				}
				*/
			}
			if (id==0){
				// Google
				if(version==0){
					int googlehsize=0;
					int googlevsize=0;
					String googlechannel="";
					/*
					if (hsize>=180&&vsize>=150){
						googlehsize=180;
						googlevsize=150;
						googlechannel="3981610134";
					}
					if (hsize>=468&&vsize>=60){
						googlehsize=468;
						googlevsize=60;
						googlechannel="3304013985";
					}
					if (hsize>=120&&vsize>=600){
						googlehsize=120;
						googlevsize=600;
						googlechannel="7713744603";
					}
					 */
					if (hsize>=160&&vsize>=600){
						googlehsize=160;
						googlevsize=600;
						googlechannel="8134861081";
					}
					if (hsize>=728&&vsize>=90){
						googlehsize=728;
						googlevsize=90;
						googlechannel="6518527089";
					}


					if (!(googlehsize+""+googlevsize+googlechannel).equals("00")){
						html="<script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script>\n" +
							"<!-- ad.java 1 -->\n" +
							"<ins class=\"adsbygoogle\"\n" +
							"style=\"display:inline-block;width:" + googlehsize + "px;height:" + googlevsize + "px\"\n" +
							"data-ad-client=\"ca-pub-5573504203886586\"\n" +
							"data-ad-slot=\"" + googlechannel + "\"></ins>\n" +
							"<script>\n" +
								"(adsbygoogle = window.adsbygoogle || []).push({});\n" +
							"</script>";
//						html="<script type=\"text/javascript\"><!-- \n"+
//						"google_ad_client = \"pub-5573504203886586\";\n"+
//						"google_ad_width = "+googlehsize+";\n"+
//						"google_ad_height = "+googlevsize+";\n"+
//						"google_ad_format = \""+googlehsize+"x"+googlevsize+"_as\";\n"+
//						"google_ad_type = \"text_image\";\n"+
//						"google_ad_channel = \""+googlechannel+"\";\n"+
//						"google_color_border = \""+google_color_border+"\";\n"+
//						"google_color_bg = \""+google_color_bg+"\";\n"+
//						"google_color_link = \""+google_color_link+"\";\n"+
//						"google_color_text = \""+google_color_text+"\";\n"+
//						"google_color_url = \""+google_color_url+"\";\n"+
//						"//	--></script>\n"+
//						"<script type=\"text/javascript\"\n"+
//						"  src=\"http"+(https?"s":"")+"://pagead2.googlesyndication.com/pagead/show_ads.js\">\n"+
//						"</script>";
						
					}
				}
				if (version==1){
					if (hsize>=728&&vsize>=90){
//						html="<script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-5573504203886586\";\n/* 728x90, gemaakt 25-7-08 */\ngoogle_ad_slot = \"9290077030\";\ngoogle_ad_width = 728;\ngoogle_ad_height = 90;\n//-->\n</script>\n<script type=\"text/javascript\"\n src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script>";
						html="<script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script>\n<!-- Ad.java 3 -->\n<ins class=\"adsbygoogle\"\nstyle=\"display:inline-block;width:728px;height:90px\"\ndata-ad-client=\"ca-pub-5573504203886586\"\ndata-ad-slot=\"9471993483\"></ins>\n<script>\n(adsbygoogle = window.adsbygoogle || []).push({});\n</script>";
					}
					if (hsize>=160&&vsize>=600){
//						html="<script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-5573504203886586\";\n/* 160x600, gemaakt 29-7-08 */\ngoogle_ad_slot = \"6865149805\";\ngoogle_ad_width = 160;\ngoogle_ad_height = 600;\n//-->\n</script>\n<script type=\"text/javascript\"\n src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script>";
						html="<script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script>\n<!-- Ad.java 4 -->\n<ins class=\"adsbygoogle\"\nstyle=\"display:inline-block;width:160px;height:600px\"\ndata-ad-client=\"ca-pub-5573504203886586\"\ndata-ad-slot=\"4902193085\"></ins>\n<script>\n(adsbygoogle = window.adsbygoogle || []).push({});\n</script>";
					}
				}
				if (version==2||version==3){
					if (hsize>=728&&vsize>=90){
//						html="<div class='googlead'><script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-5573504203886586\";\n/* 728x90, Bordeaux kleur */\ngoogle_ad_slot = \"6938544770\";\ngoogle_ad_width = 728;\ngoogle_ad_height = 90;\n//-->\n</script>\n<script type=\"text/javascript\"\n src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script></div>";
						html="<script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script>\n<!-- Ad.java 5 -->\n<ins class=\"adsbygoogle\"\nstyle=\"display:inline-block;width:728px;height:90px\"\ndata-ad-client=\"ca-pub-5573504203886586\"\ndata-ad-slot=\"1809125885\"></ins>\n<script>\n(adsbygoogle = window.adsbygoogle || []).push({});\n</script>";
					}
					if (hsize>=160&&vsize>=600){
						//html="<div class='googlead'><script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-5573504203886586\";\n/* 160x600 Bordeaux achtergrond */\ngoogle_ad_slot = \"1498961530\";\ngoogle_ad_width = 160;\ngoogle_ad_height = 600;\n//-->\n</script>\n<script type=\"text/javascript\"\n src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script></div>";
//						html="<div class='googlead'><script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-5573504203886586\";\n/* skyscraper red */\ngoogle_ad_slot = \"3284462890\";\ngoogle_ad_width = 160;\ngoogle_ad_height = 600;\n//-->\n</script>\n<script type=\"text/javascript\"\n src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script></div>";
						html="<script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script>\n<!-- Ad.java 6 -->\n<ins class=\"adsbygoogle\"\nstyle=\"display:inline-block;width:160px;height:600px\"\ndata-ad-client=\"ca-pub-5573504203886586\"\ndata-ad-slot=\"7716058684\"></ins>\n<script>\n(adsbygoogle = window.adsbygoogle || []).push({});\n</script>";
					}
				}
			}
			if (html.contains("@WINENAME@")) html=html.replace("@WINENAME@", Knownwines.getKnownWineName(knownwineid));
			if (html.contains("@WINECOLOR@")) html=html.replace("@WINECOLOR@", new Knownwine(knownwineid).getProperties().get("type"));
			if (html.contains("@WINEREGION@")) html=html.replace("@WINEREGION@", Region.getRegion(knownwineid));

		} catch (Exception exc){
			Dbutil.logger.error("Could not retrieve ad. ",exc);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	private String getRotatingBannerHtml() {
		StringBuffer sb=new StringBuffer();
		String query;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query="select distinct(partnerid) as partnerid FROM banners where hsize=234 and vsize=60 and country like '%"+country+"%' and active=1 ORDER BY partnerid!=4,RAND() limit 4;";
			rs = Dbutil.selectQueryForUpdate(query, con);
			if (rs.isBeforeFirst()){
				sb.append("<div class='banners'>"+Configuration.adtext);
			}
			while (rs.next()) {
				query="SELECT * FROM banners where partnerid="+rs.getString("partnerid")+" and hsize=234 and vsize=60 and active=1 and country like '%"+country+"%'  ORDER BY RAND() LIMIT 1;";
				rs2=Dbutil.selectQuery(query, con);
				if (rs2.next()){
					sb.append("<div class='banner'><a href='/adhandler.jsp?id="+rs2.getString("id")+"' target='_blank' rel='nofollow'>"+rs2.getString("html")+"</a></div>");
					rotatingbannerid=rs2.getInt("id");
					//if (!bot) new Thread(this).start();
					//Dbutil.executeQuery("update low_priority banners set views=views+1 where id="+rs2.getInt("id")+";");
					bannersshown+=rs2.getString("id")+",";
				}
				Dbutil.closeRs(rs2);
				//rs.updateInt("views", rs.getInt("views")+1);
			}
			if (rs.isAfterLast()){
				sb.append("</div><div class='clear'></div>");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}

	public static String getUrl(String idstr, String ip, String hostname,Searchdata searchdata,String hostcountry,String region, String knownwineid){
		String url="/index.jsp";
		int id=0;
		try{
			id=Integer.parseInt(idstr);
		} catch (Exception e){
			Dbutil.logger.info("Received a non-integer argument for ad id. Someone is fooling with us.");
		}
		if (id>0){
			ResultSet rs=null;
			String winename="";
			if (region==null||region.equals(null)) region="";
			if (knownwineid==null||knownwineid.equals(null)) knownwineid="";
			Connection con=Dbutil.openNewConnection();
			if (knownwineid!=null&&!knownwineid.equals("")){
				try {
					winename=Dbutil.readValueFromDB("Select * from knownwines where id="+knownwineid+";", "wine");
				} catch (Exception exc){}
			}
			if (winename.equals("")) winename=searchdata.getName();
			try{
				String query="SELECT * FROM banners where id="+id+";";
				rs=Dbutil.selectQuery(query,con);
				if(rs.next()){
					String tempurl=rs.getString("link");
					String partnerid=rs.getString("Partnerid");
					String shopid=rs.getString("Shopid");
					double cpc=rs.getDouble("Payperclick");
					if ((!ip.contains("192.168.1.")&&!ip.contains("127.0.0.1"))||Wijnzoeker.serverrole.equals("DEV")){
						Webactionlogger logger=new Webactionlogger(id+"", region, winename, partnerid, cpc, "Banner", ip, winename, searchdata.getVintage(), searchdata.getCountry(), shopid);
						logger.logaction();
						/*Dbutil.executeQuery("Insert into adoverview (date,type,referenceid,payperclick,invoice) values ('"+now+"','banners',"+id+","+payperclick+",0);",con);
					rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
					if (rs.next()){
						int adoverviewid=rs.getInt("id");
						Dbutil.executeQuery("Insert into bannersclicked (date,partnerid,bannerid,shopid,ipaddress,hostname,income,adoverviewid,hostcountry,region,wine) values ('"+now+"','"+partnerid+"','"+id+"','"+shopid+"','"+ip+"','"+hostname+"',"+payperclick+","+adoverviewid+",'"+hostcountry+"','"+Spider.SQLEscape(region)+"','"+Spider.SQLEscape(winename)+"');",con);
						Dbutil.executeQuery("Update banners set clicks=clicks+1 where id="+id+";",con);
						// if all succeeded, go ahead
					}
						 */
					}
					url=tempurl;

				}
			} catch (Exception exc){
				Dbutil.logger.error("Could not retrieve ad link. ",exc);
			}
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return url;

	}

	public static void setBannerAction(String bannerid, int status, int partnerid, Auditlogger al){
		Dbutil.executeQuery("Update banners set active="+status+" where id="+bannerid+" and partnerid="+partnerid+";");
		al.run();

	}

	public static String getBannerPrice(String bannerid, int partnerid){
		return Dbutil.readValueFromDB("Select payperclick from banners where id="+bannerid+" and partnerid="+partnerid+";","payperclick");

	}

	public static float setLinkBid(int shopid, float amount){
		try{
			int i=Dbutil.executeQuery("update shops set costperclick="+amount+" where id="+shopid+";");
			if (i==0){
				Dbutil.logger.error("Could update CPC for shopid "+shopid);
			}
		} catch (Exception e){
			Dbutil.logger.error("Could update CPC for shopid "+shopid,e);
		}
		amount=(float)0;
		try{
			amount=Float.parseFloat(Dbutil.readValueFromDB("select costperclick from shops where id="+shopid+";", "costperclick"));
		} catch (Exception e){
			Dbutil.logger.error("Could not read CPC for shopid "+shopid,e);
		}
		return amount;
	}

	public static boolean acceptAdAgreement(Auditlogger al, String fullname, boolean accepted){
		if (accepted&&fullname!=null&&!fullname.trim().equals("")){
			al.setAction("Ad Agreement accepted by "+fullname+" for partner "+al.partnerid+" ("+al.partnername+").");
			al.logaction();
			if (Dbutil.executeQuery("update partners set adenabled=1 where id="+al.partnerid+";")>0)	{
				al.adenabled=true;
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		Dbutil.executeQuery("update low_priority banners set views=views+1 where id="+rotatingbannerid+";");
		
	}



}

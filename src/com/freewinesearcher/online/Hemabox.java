package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import com.freewinesearcher.common.Dbutil;

public class Hemabox implements Runnable{
	static int history=7; 
	static int n=10;
	private static Integer[] periods={1,7,30};
	private static String[] regions={"","EU","UC","AP"};
	private static HashMap<String,HashMap<Integer,String>> html=new HashMap<String, HashMap<Integer,String>>();
	private static long lastupdate=0;
	private static final String advancedsearchpage="/wine-guide/"; 
	private static String winecount="";
	
	public static String getWinecount() {
		if ("".equals(winecount)) setWinecount();
		return winecount;
	}



	public static void setWinecount() {
		int n=Dbutil.readIntValueFromDB("Select count(*) as thecount from wines","thecount");
		NumberFormat format  = new DecimalFormat("#,###,###");
		winecount=format.format(n);
		
	}



	public static String getHtml(PageHandler p){
		return "";
		/*
		int period=7;
		String c="";
		try{
			c=Webroutines.getRegion(p.searchdata.country);
			period=p.searchdata.getHistory();
		}catch (Exception e){}
		boolean valid=false;
		for (Integer per:periods) if (period==per) valid=true;
		if (!valid) period=7;
		
		try{
			if (lastupdate==0||new Date().getTime()-lastupdate>12*1000*60*60){
			lastupdate=new Date().getTime();
			new Thread(new Hemabox()).start();
			//Dbutil.logger.info("Refreshing top 10 lists");
			if (html.get(c)!=null&&html.get(c).get(period)!=null) return html.get(c).get(period);
			return "No data available, please check back in a few minutes.";
		}
		if (html.get(c)==null||html.get(c).get(period)==null) return  "No data available, please check back in a few minutes.";
		return html.get(c).get(period);
		} catch (Exception e){
			Dbutil.logger.error("Could not retrieve Hemabox for period "+period+" and "+c,e);
			return "";
		}
		*/
	}
	


	public static void setHtml(){
		setWinecount();
		if (false){
		Dbutil.logger.info("Refreshing Hemabox html...");
		lastupdate=new Date().getTime();
		for (Integer history:periods){
			for (String region:Hemabox.regions){
				try {
					String countryclause="";
					if (region.length()>1) countryclause=" and hostcountry in ('"+Webroutines.getCountries(region)+"') ";
					StringBuffer sb=new StringBuffer(); 
					sb.append("<div class='clear'><h2>What's hot</h2></div><div class='hbox'><div id='regiontab'><ul class='tabs region' ><li><a href='?country=EU#hbox'"+(region.equals("EU")?" class='current'":"")+">Europe</a></li><li><a href='?country=UC#hbox'"+(region.equals("UC")?" class='current'":"")+">North America</a></li><li><a href='?country=All#hbox'"+(region.equals("")?" class='current'":"")+">World wide</a></li></ul></div>" +
							"<div id='periodtab'><ul class='tabs periods' ><li><a href='?history=1#hbox'"+(history==1?" class='current'":"")+">Today</a></li><li><a href='?history=7#hbox'"+(history==7?" class='current'":"")+">This week</a></li><li><a href='?history=30#hbox'"+(history==30?" class='current'":"")+">This month</a></li></ul></div>");
					sb.append("</div><div class='dialog'><div class='content'><div class='t'></div>");
					//sb.append("Period: <a href='?history=1' method='post'>Today </a> <a href='?history=7' method='post'>This week</a> <a href='?history=30' method='post'>This month</a>");
					//sb.append("Region: <a href='?country=EU' method='post'>Europe</a> <a href='?country=US' method='post'>USA/Canada</a> <a href='?country=AP' method='post'>Asia/Pacific</a> <a href='?country=All' method='post'>All</a>");
					sb.append("<table class='top10'><tr><th></th><th></th><th></th></tr><tr><td>"); 
					sb.append("<h2>Most popular red wines</h2>");
					sb.append(Webroutines.query2table("select concat('<a href=\"/wine/',knownwines.wine,'\" alt=\"Buy ',knownwines.wine,' online\">',replace(knownwines.Wine,'&','&amp;'),'</a>') as Wine from logging join knownwines on (logging.knownwineid=knownwines.id) where date > now() - interval "+history+" day and bot=0 and knownwineid>0 and knownwines.type='Red'"+countryclause+" group by knownwineid order by count(distinct(ip)) desc limit "+n+";",false,true));
					Thread.sleep(10000);
					sb.append("<h2>Most popular white wines</h2>");
					sb.append(Webroutines.query2table("select concat('<a href=\"/wine/',knownwines.wine,'\" alt=\"Buy ',knownwines.wine,' online\">',replace(knownwines.Wine,'&','&amp;'),'</a>') as Wine from logging join knownwines on (logging.knownwineid=knownwines.id) where date > now() - interval "+history+" day and bot=0 and knownwineid>0 and knownwines.type='White'"+countryclause+" group by knownwineid order by count(distinct(ip)) desc limit "+n+";",false,true));
					Thread.sleep(10000);
					sb.append("</td><td>");
					sb.append("<h2>Fastest risers in popularity</h2>");
					sb.append(Webroutines.query2table("select concat('<a href=\"/wine/',knownwines.wine,'\" alt=\"Buy ',knownwines.wine,' online\">',replace(knownwines.Wine,'&','&amp;'),'</a>') as Wine, concat('Up ',round((100*difference/thecount)),'%') as 'Percentage up' from (select thisweek.knownwineid, thisweek.thecount as thecount, thisweek.thecount-if(lastweek.thecount is null,0,lastweek.thecount) as difference from (SELECT knownwineid,count(distinct(ip)) as thecount FROM logging where date > now() - interval "+history+" day and bot=0 and vintage>0"+countryclause+" group by knownwineid) thisweek left join (SELECT knownwineid,count(distinct(ip)) as thecount FROM logging use index (Date_bot) where date > now() - interval "+(history*2)+" day and date < now() - interval "+history+" day and bot=0 and knownwineid!=0 and vintage>0"+countryclause+" group by knownwineid) lastweek on (thisweek.knownwineid=lastweek.knownwineid) where thisweek.knownwineid>0 having difference > 0 order by difference desc limit 10) sel join knownwines on (sel.knownwineid=knownwines.id) order by difference/thecount desc,difference desc;",false,true));
					Thread.sleep(10000);
					sb.append("<h2>Most popular regions</h2>");
					sb.append(Webroutines.query2table("select concat('<a href=\"/region/',replace(region,', ','/'),'/\">',kbregionhierarchy.shortregion,'</a>') as Region from logging join knownwines on (logging.knownwineid=knownwines.id) join kbregionhierarchy on (knownwines.locale=kbregionhierarchy.region) where date > now() - interval "+history+" day and bot=0 and knownwineid>0"+countryclause+" group by knownwines.locale order by count(distinct(ip)) desc limit "+n+";",false,true));
					Thread.sleep(10000);
					sb.append("</td><td>");
					sb.append("<h2>Most popular grape varieties</h2>");
					sb.append(Webroutines.query2table("select concat('<a href=\""+advancedsearchpage+"grape/',knownwines.grapes,'\">',knownwines.grapes,'</a>') as Grapes from logging join knownwines on (logging.knownwineid=knownwines.id) where date > now() - interval "+history+" day and bot=0 and knownwineid>0"+countryclause+"  group by knownwines.grapes order by count(distinct(ip)) desc limit "+n+";",false,true));
					Thread.sleep(10000);
					sb.append("<h2>Hot latest Parker reviews</h2>");
					sb.append(Webroutines.query2table("select concat('<a href=\"/wine/',knownwines.wine,CAST(if(ratedwines.vintage>0,concat(' ',ratedwines.vintage),'') AS CHAR),'\" alt=\"Buy ',knownwines.wine,' online\">',replace(knownwines.Wine,'&','&amp;'),' ',CAST(if(ratedwines.vintage>0,ratedwines.vintage,'') AS CHAR),'</a>') as Wine, concat(ratedwines.rating,if(ratedwines.ratinghigh>0,concat('-',ratedwines.ratinghigh),'')) as Rating  from (SELECT max(issuedate) as issuedate FROM ratedwines where author='RP') sel natural join ratedwines join logging on (ratedwines.knownwineid=logging.knownwineid) join wines on (wines.knownwineid=ratedwines.knownwineid and wines.vintage=ratedwines.vintage) join  knownwines on (ratedwines.knownwineid=knownwines.id) where date > now() - interval "+history+" day and bot=0 and ratedwines.knownwineid>0"+countryclause+"  group by ratedwines.knownwineid order by count(distinct(ip)) desc limit "+n+";",false,true));
					Thread.sleep(10000);
					sb.append("</td></tr></table>");
					sb.append("</div><div class='b'><div></div></div></div>");
					HashMap<Integer,String> p=null;
					if (html.get(region)!=null) {
						p=html.get(region);
					} else {
						p=new HashMap<Integer,String>();
					}
					p.put(history,sb.toString());
					html.put(region,p);
				} catch (InterruptedException e) {
					Dbutil.logger.error("Could not create Hemabox",e);
					
				}catch (Exception e) {
					Dbutil.logger.error("Could not create Hemabox",e);
					
				}
			} 
		}
		Dbutil.logger.info("Finished refreshing Hemabox html...");
		}
	}
	

	@Override
	public void run() {
		setHtml();
		
	}
}

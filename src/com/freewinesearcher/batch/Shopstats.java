package com.freewinesearcher.batch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;

public class Shopstats {

	public static void updateStats(){
		Dbutil.logger.info("Start updating shopstats");
		Dbutil.executeQuery("insert ignore into shopstats (shopid,disabled) select id,disabled from shops;");
		Dbutil.executeQuery("update shopstats join (select id as shopid,disabled,succes,if (TIMESTAMPDIFF(SECOND,lastsearchstarted,lastsearchended)>0,TIMESTAMPDIFF(SECOND,lastsearchstarted,lastsearchended),999999) as duration from shops) sel on (sel.shopid=shopstats.shopid) set shopstats.disabled=sel.disabled,shopstats.lastknowngooddays=sel.succes,shopstats.duration=sel.duration;");
		Dbutil.executeQuery("update shopstats set urls=0,winestotal=0,winesunique=0;");
		Dbutil.executeQuery("update shopstats join (select shopid,sum(if(size=0,0,1)) as size,sum(if(lastupdated>date_sub(now(),interval 2 day),1,0)) as winesfound,sum(if(lastupdated>date_sub(now(),interval 2 day),0,1)) as winesnotfound, sum(if(knownwineid=0,0,1)) as knownwines from wines group by shopid) sel on (sel.shopid=shopstats.shopid) set shopstats.winesunique=sel.winesfound,shopstats.winesnotfound=sel.winesnotfound,shopstats.size=if(sel.size>0,sel.size/(sel.winesfound+sel.winesnotfound),0),shopstats.knownwines=if(sel.knownwines>0,sel.knownwines/(sel.winesfound+sel.winesnotfound),0);");
		//Dbutil.executeQuery("update shopstats left join (select shopid,count(*) as winesunique from wines where date(lastupdated)>=date(sysdate()) group by shopid) sel on (sel.shopid=shopstats.shopid) set shopstats.winesunique=if(sel.winesunique is null,0,sel.winesunique);");
		Dbutil.executeQuery("update shopstats join (select shopid,count(*) as urls,sum(winesfound) as winestotal from scrapelist group by shopid) sel on (sel.shopid=shopstats.shopid) set shopstats.winestotal=sel.winestotal,shopstats.urls=sel.urls;");
		Dbutil.executeQuery("update shopstats join (select shopid,count(*) as winesunique from wines group by shopid) sel on (sel.shopid=shopstats.shopid) set shopstats.lastknowngoodwinesunique=sel.winesunique where shopstats.lastknowngooddays=0;");
		Dbutil.executeQuery("update shopstats join (select shopid,count(*) as urls,sum(winesfound) as winestotal from scrapelist group by shopid) sel on (sel.shopid=shopstats.shopid) set shopstats.lastknowngoodwinestotal=sel.winestotal,shopstats.lastknowngoodurls=sel.urls where shopstats.lastknowngooddays=0;");
		Dbutil.executeQuery("update shopstats join (select id as shopid,disabled,succes,if (TIMESTAMPDIFF(SECOND,lastsearchstarted,lastsearchended)>0,TIMESTAMPDIFF(SECOND,lastsearchstarted,lastsearchended),999999) as duration from shops where succes=0) sel on (sel.shopid=shopstats.shopid) set shopstats.disabled=sel.disabled,shopstats.lastknowngooddays=sel.succes,shopstats.lastknowngoodduration=sel.duration;");
		Dbutil.logger.info("Finished updating shopstats");
	}
	
	
	public static String getHtml(String order, boolean problemsonly, boolean admin){
		if (order==null) order="id";
		StringBuffer sb=new StringBuffer();
		String query;
		int n=0;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String alert=" class='alert'";
		String warn=" class='warn'";
		String probablecause="";
		try { 
			sb.append("<table class='shopstats'>");
			sb.append("<thead><tr><th><a href='?sort=shopid"+("shopid".equals(order)?"+desc":"")+"'>id</a></th><th><a href='?sort=shopname"+("shopname".equals(order)?"+desc":"")+"'>Name</a></th><th>Details</th><th><a href='?sort=lastchange"+("lastchange".equals(order)?"+desc":"")+"'>Last change</a></th><th><a href='?sort=knownwines"+("knownwines".equals(order)?"+desc":"")+"'>Recognized</a></th><th><a href='?sort=size"+("size".equals(order)?"+desc":"")+"'>Size</a></th><th>Spidered URLs</th><th>Scraped wines</th><th><a href='?sort=lastknowngooddays"+("lastknowngooddays".equals(order)?"+desc":"")+"'>Last time OK</th></tr></thead>");
			sb.append("<tbody >");
			query="select shopstats.*,shopname,comment from shopstats join shops on (shopstats.shopid=shops.id) where shoptype!=2 order by disabled,"+order+",id" ;
			if (problemsonly){
				if (admin){
					query="select SQL_CALC_FOUND_ROWS shopstats.*,shopname,comment,issuelog.status as owner from shopstats join shops on (shopstats.shopid=shops.id) left join issuelog on (shops.id=issuelog.shopid) where  succes>"+Configuration.daysshopsnok+" and shoptype=1 and shops.disabled=0 having (owner is null || owner!=2) order by (succes between 20 and 30) desc,succes desc, lastknowngoodwinesunique desc,shops.id limit 10  ;" ;
				} else {
					query="select SQL_CALC_FOUND_ROWS shopstats.*,shopname,comment,issuelog.status as owner,datafeeds.shopid as feed from shopstats join shops on (shopstats.shopid=shops.id) left join issuelog on (shops.id=issuelog.shopid) left join datafeeds on (shops.id=datafeeds.shopid) where  succes>"+Configuration.daysshopsnok+" and shoptype=1 and shops.disabled=0 having (owner is null || owner!=1) and feed is null order by (succes between 20 and 30) desc,succes desc, lastknowngoodwinesunique desc,shops.id limit 10  ;" ;
				}
			}
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				probablecause="";
				boolean markmanage=(rs.getInt("winesunique")*100>rs.getInt("winesnotfound")*66&&rs.getInt("winesunique")*100>rs.getInt("lastknowngoodwinesunique")*66);
				if (markmanage) probablecause=rs.getString("shopname")+" usually had around "+rs.getInt("lastknowngoodwinesunique")+" wines. We now find "+rs.getInt("winesunique")+" wines but they are named differently from the "+rs.getInt("winesnotfound")+" wines we found before. Something may have changed in the web page causing all wines to be named slightly different than before, but they may be the same wines. Check the details, correct the scraper if necessary and if the new wine names are OK and should replace the old wine names, remove the old wines.";
				sb.append("<tr><td>"+rs.getString("shopid")+"</td><td><a href='/moderator/editshop.jsp?actie=retrieve&amp;shopid="+rs.getInt("shopid")+"' target='_blank'>"+(rs.getInt("disabled")>0?"<s>":"")+rs.getString("shopname")+(rs.getInt("disabled")>0?"</s>":"")+"</a></td><td><a href='/moderator/manage.jsp?shopid="+rs.getInt("shopid")+"' target='_blank'"+(markmanage&&rs.getInt("lastknowngooddays")>0?alert:"")+">Details</a></td><td>"+(rs.getString("lastchange")!=null?rs.getString("lastchange"):"")+"</td><td><a "+(rs.getDouble("knownwines")<0.66&&rs.getInt("winesunique")>0?warn:"")+" href='/moderator/detectscraper.jsp?shopid="+rs.getInt("shopid")+"' target='_blank'>"+percentage(rs.getDouble("knownwines"))+"</a></td><td><a"+(rs.getDouble("size")<0.66&&rs.getInt("winesunique")>0?warn:"")+" href='/moderator/detectscraper.jsp?shopid="+rs.getInt("shopid")+"' target='_blank'>"+percentage(rs.getDouble("size"))+"</a></td>");
				if (rs.getInt("lastknowngooddays")>0||rs.getInt("winesunique")==0){
					Boolean markurls=(!markmanage&&100*rs.getInt("urls")<66*rs.getInt("lastknowngoodurls"));
					if (markurls) probablecause="For "+rs.getString("shopname")+", we spidered only "+rs.getInt("urls")+" urls instead of the usual "+rs.getInt("lastknowngoodurls")+" urls. Check the spider.";
					sb.append("<td><a href='/moderator/editspiderregex.jsp?shopid="+rs.getInt("shopid")+"&amp;actie=retrieve' target='_blank'"+(markurls?alert:"")+">"+rs.getInt("urls")+" ("+rs.getInt("lastknowngoodurls")+") urls</a></td>");
					//Boolean marktotal=(!markurls&&100*rs.getInt("winestotal")/(1+rs.getInt("urls"))<66*rs.getInt("lastknowngoodwinestotal")/(1+rs.getInt("lastknowngoodurls")));
					//sb.append("<td><a href='/moderator/detectscraper.jsp?shopid="+rs.getInt("shopid")+"' target='_blank'"+(marktotal?alert:"")+">"+rs.getInt("winestotal")+" ("+rs.getInt("lastknowngoodwinestotal")+") wines scraped</a></td>");
					//Boolean markunique=(!marktotal&&!markurls&&100*rs.getInt("winesunique")/(1+rs.getInt("urls"))<66*rs.getInt("lastknowngoodwinesunique")/(1+rs.getInt("lastknowngoodurls")));
					//sb.append("<td><a href='/moderator/detectscraper.jsp?shopid="+rs.getInt("shopid")+"' target='_blank'"+(markunique?alert:"")+">"+rs.getInt("winesunique")+" ("+rs.getInt("lastknowngoodwinesunique")+") wines stored</a></td>");
					Boolean markwines=(!markmanage&&!markurls&&100*rs.getInt("winesunique")/(1+rs.getInt("urls"))<66*rs.getInt("lastknowngoodwinestotal")/(1+rs.getInt("lastknowngoodurls")));
					if (markwines) probablecause="For "+rs.getString("shopname")+", we found only "+rs.getInt("winesunique")+" wines from the usual "+rs.getInt("lastknowngoodwinestotal")+". Check the scraper to see if it can find the wines. If it can still find all wines, remove the old wines.";
					sb.append("<td><a href='/moderator/detectscraper.jsp?shopid="+rs.getInt("shopid")+"' target='_blank'"+(markwines?alert:"")+">"+rs.getInt("winesunique")+"/<s>"+rs.getInt("winesnotfound")+"</s> ("+rs.getInt("lastknowngoodwinesunique")+") wines scraped</a></td>");
					sb.append("<td>"+rs.getInt("lastknowngooddays")+"</td>");
				} else {
					sb.append("<td>"+rs.getInt("urls")+"</td><td>"+rs.getInt("winesunique")+"</td><td></td>");
					
				}
				sb.append("</tr>"); 
				if (rs.getInt("lastknowngooddays")>0){
					sb.append("<tr><td></td><td colspan='8'><a href='issuelog.jsp?shopid="+rs.getInt("shopid")+"'>Last comment: "+Spider.escape(Dbutil.readValueFromDB("select * from issuelog where shopid="+rs.getInt("shopid")+" order by id desc;", "message"))+"</a></td></tr>");
					if (probablecause.length()>0) sb.append("<tr><td></td><td colspan='8'>"+probablecause+"</td></tr>");
				}
			}
			Dbutil.closeRs(rs);
			rs=Dbutil.selectQuery("SELECT FOUND_ROWS();",con);
			if (rs.next()){
				n=rs.getInt(1);
			}
			sb.append("</tbody></table>");
			
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (problemsonly) return n+" stores with problems in total.<br/>"+sb.toString();
		return sb.toString();
	}
	
	public static String determineproblem (ResultSet rs) throws SQLException{
		String cause="";
		if (rs.getInt("urls")>0&&rs.getInt("urls")<Math.round(rs.getInt("lastknowngoodurls")*0.66)){
			cause+="We spidered only "+rs.getInt("urls")+" instead of the normal "+rs.getInt("lastknowngoodurls");
		}
		return cause;
	}
	
	public static String percentage(Double d){
		NumberFormat nf=NumberFormat.getPercentInstance();
		return nf.format(d);
	}
	
}

package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;

public class Producerinfo {
	public String name;
	public int id;
	public Set<Producer> producers=new HashSet<Producer>();
	
	public Producerinfo(String name){
		this.id=Dbutil.readIntValueFromDB("select * from kbproducers where name='"+Spider.SQLEscape("name")+"';", "id");
		this.name=name;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query="select * from kbproducers where id="+id+"";
			rs=Dbutil.selectQuery(rs, query, con);
			if (rs.next()){
				for (String p:rs.getString("producerids").split(",")){
					try{producers.add(new Producer(Integer.parseInt(p)));}catch(Exception e){}
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public Producerinfo(int id){
		this.id=id;
		this.name=Dbutil.readValueFromDB("select * from kbproducers where id="+id, "name");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query="select * from kbproducers where id="+id+"";
			rs=Dbutil.selectQuery(rs, query, con);
			if (rs.next()){
				for (String p:rs.getString("producerids").split(",")){
					try{producers.add(new Producer(Integer.parseInt(p)));}catch(Exception e){}
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public String getInfo(PageHandler p) {
		{
			Double pricefactor=Double.valueOf(Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.currency+"';", "rate"));
			if (pricefactor==(double)0) pricefactor=(double)1;
			String symbol=Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.currency+"';", "symbol");
			ResultSet rs=null;
			String query;
			Connection con=Dbutil.openNewConnection();
			StringBuffer sb=new StringBuffer();

			try{
				if (producers.size()>0){
					sb.append("<h1>"+name+"</h1>");
					sb.append("<ul class='tabs' id='winerytabs'><li><a href='#wines'>Wines</a></li>"+(producers.size()>0?"<li><a href='#mappane'>Location</a></li>":"")+"</ul><div id='winerypane'>");
					sb.append("<div class='pane' id='wines'>");
					
				}
				sb.append("<table>");
				query = "select *,avg(bestprices.priceeuroex) as avg,min(bestprices.priceeuroex) as min,std(bestprices.priceeuroex) as std, avg(rating) as rating from knownwines left join bestprices on (knownwines.id=bestprices.knownwineid and continent='AL') left join ratinganalysis ra on (knownwines.id=ra.knownwineid and author='FWS') where producer='"+Spider.SQLEscape(name)+"' group by knownwines.id order by appellation, wine;";
				rs = Dbutil.selectQuery(rs, query, con);
				String lastregion="";
				int avg;
				int stars;
				while (rs.next()) {
					if (!rs.getString("appellation").equals(lastregion)){
						lastregion=rs.getString("appellation"); 
						Dbutil.logger.info(Regioninfo.locale2href(rs.getString("locale"))); 
						sb.append("<tr><td colspan='2'><h2><a href='/region/"+Regioninfo.locale2href(rs.getString("locale"))+"/'>"+lastregion+"</a></h2></td></tr>");
					}
					avg=rs.getInt("avg");
					stars=(int) Math.round(((rs.getDouble("rating")-87)*5)/6);
					if (stars>5) stars=5;
					sb.append("<tr><td>"+(avg>0?"<a href='/wine/"+Webroutines.URLEncode(Webroutines.removeAccents(Knownwines.getUniqueKnownWineName(rs.getInt("id")))).replaceAll("&", "&amp;")+"'>":"")+rs.getString("wine").replaceAll("&", "&amp;")+(avg>0?"</a>":"")+" ");
					for (int i=0;i<stars;i++) sb.append("<img src='/css/star.gif' alt='star'/>");
					sb.append("</td><td>"+(rs.getInt("avg")>0?symbol+(Math.round(rs.getDouble("min")/pricefactor))+" - "+symbol+(Math.round((rs.getDouble("avg")+rs.getDouble("std"))/pricefactor)):"n.a.")+"</td></tr>");
					sb.append("<tr><td colspan='2'> <i>A "+(rs.getString("type").replaceAll(" - ", " ").toLowerCase()+" wine").replaceAll("spirits wine", "spirit").replaceAll("liqueur wine", "liqueur")+" made from "+(rs.getString("grapes").toLowerCase().contains("blend")?"a ":"")+rs.getString("grapes")+"</i></td></tr>");
				}
				sb.append("</table>");
				if (producers.size()>0){
					sb.append("</div><div class='pane' id='mappane'><table><tr>");
					Iterator<Producer> i=producers.iterator();
					Producer prod;
					while(i.hasNext()){
						prod=i.next();
						sb.append("<td><table>");
						sb.append("<tr><td colspan='2'>Address information given is for winery "+prod.name+"</td></tr>");
						sb.append("<tr><td>Address</td><td>"+prod.address+"</td></tr>");
						sb.append("<tr><td>Phone number</td><td>"+prod.telephone+"</td></tr>");
						if (prod.website!=null&&prod.website.length()>0)sb.append("<tr><td>Web site</td><td><a href='/external.jsp?exturl="+Webroutines.URLEncode(prod.website).replaceAll("'","&apos;")+"'>"+prod.website+"</a></td></tr>");
						sb.append("</table></td>");
					}
					sb.append("</tr></table>");
					sb.append("<div id='map' style='width: 700px; height: 400px;'></div></div>");
					
					sb.append("</div></div>");
				}




			} catch (Exception e){
				String name = e.getClass().getName();
				if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
					// No action
				} else {
					Dbutil.logger.error("Error while retrieving image. ",e);
				} 

			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);	
			}
			return sb.toString();
		}
	}



}

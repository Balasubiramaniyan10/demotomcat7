package com.freewinesearcher.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.online.Webroutines;
import com.freewinesearcher.online.WineAdvice;
import com.freewinesearcher.online.WineAdvice.Searchtypes;
import com.searchasaservice.ai.Aitools;
import com.searchasaservice.ai.Recognizer;


/**
 * @author Jasper
 *
 */
public class Knownwines {


	private static Knownwines instance = null;
	protected Knownwines() {
		// Exists only to defeat instantiation.
	}
	public static Knownwines getInstance() {
		if(instance == null) {
			instance = new Knownwines();
		}
		return instance;
	}
	/*
	 * Analyzes wines table for knownwines. 
	 * History is in days, if 0, all knownwinesmatches are deleted and all wines are analyzed.
	 */	
	public Knownwines(int history){ 
		Dbutil.logger.info("Starting job to recognize wines");
		Knownwinesprecise(history, false, true,"wines");
		updateWinesforKnownWines();
		Knownwinesprecise(history, true, false,"wines");
		//Knownwinesbestmatch(history);
		updateWinesforKnownWines();
		updateNumberOfWines();
		updateSameName();
		Dbutil.renewTable("materializedadvice");
		filterTooCheapKnownwines(5, 5);
		Dbutil.renewTable("materializedadvice");
		Dbutil.logger.info("Finished job to recognize wines");
	}


	public static void getColorFromType(){
		Dbutil.executeQuery("update knownwines set color='Red' where type like 'Red%';");
		Dbutil.executeQuery("update knownwines set color='Ros�' where type like 'Ros�%';");
		Dbutil.executeQuery("update knownwines set color='White' where type like 'White%';");
		Dbutil.executeQuery("update knownwines set dryness='Dry' where type not like '%Sweet%' and type not like '%Off%' and type not like '%Fortified%';");
		Dbutil.executeQuery("update knownwines set dryness='Sweet/Dessert' where type like '%Sweet/Dessert%';");
		Dbutil.executeQuery("update knownwines set dryness='Off-dry' where type like '%Off-dry%'");
		Dbutil.executeQuery("update knownwines set dryness='Fortified' where type like '%Fortified%'");
		Dbutil.executeQuery("update knownwines set sparkling=1 where type like '%Sparkling%';");
	}
	
	
	public static void Knownwinesbestmatch(int history){ 
		Dbutil.logger.info("Starting job to get best matches for wines");
		ResultSet rs;
		ResultSet knownwines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		Double score;
		String query;
		String query2;
		Connection con=Dbutil.openNewConnection();
		String historywhereclause="";
		Connection updatewinescon=Dbutil.openNewConnection();
		Knownwines kw=Knownwines.getInstance();
		try{
			Dbutil.executeQuery("delete from knownwinesbestmatch;");
			Dbutil.executeQuery("insert into knownwinesbestmatch (wineid,knownwineid,score) (select id,knownwineid,0 from wines where knownwineid>0);");
			boolean moretodo=true;
			int lastrow=0;
			while (moretodo){
				query="Select * from wines where knownwineid=0 order by id limit "+lastrow+",100;";
				rs=Dbutil.selectQuery(query, con);
				if (!rs.next()) moretodo=false;
				rs.beforeFirst();	
				while (moretodo&&rs.next()){
					bestmatch=kw.bestMatchKnownWineId(rs.getString("name"));
					lastrow++;
					Dbutil.executeQuery("insert into knownwinesbestmatch (wineid,knownwineid,score) values ("+rs.getString("id")+","+bestmatch+",0);",updatewinescon);
				}
				Dbutil.closeRs(rs);
				System.gc();
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		}


		Dbutil.closeConnection(con);
		Dbutil.logger.info("Finished job to get best matches for wines");

	}
	
	
	public static void newKnownwinesbestmatch(){ 
		Dbutil.logger.info("Starting job to get best matches for wines");
		ResultSet rs;
		ResultSet knownwines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		Double score;
		String query;
		String query2;
		Connection con=Dbutil.openNewConnection();
		String historywhereclause="";
		int knownwineid;
		try{
			Dbutil.executeQuery("delete from knownwinesbestmatch;");
			boolean moretodo=true;
			int lastrow=0;
			while (moretodo){
				query="Select * from wines order by id limit 100;";
				rs=Dbutil.selectQuery(query, con);
				if (!rs.isBeforeFirst()) moretodo=false;
				while (rs.next()){
					knownwineid=rs.getInt("knownwineid");
					if (knownwineid==0){
						knownwineid=determineKnownWineIdFuzzy(rs.getString("name"),true);
					}
					Dbutil.executeQuery("insert into knownwinesbestmatch(')");
				}
				Dbutil.closeRs(rs);
				System.gc();
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		}


		Dbutil.closeConnection(con);
		Dbutil.logger.info("Finished job to get best matches for wines");
		Dbutil.executeQuery("delete from knownwinesbestmatch;");
		Dbutil.executeQuery("insert into knownwinesbestmatch (select * from knownwinesbestmatchmemory);");

	}

	public static String editKnownWinesHTML(String appellation){
		StringBuffer sb=new StringBuffer();
		ResultSet rs;
		String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;

		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			//query="select distinct(appellation) from knownwines;";
			//rs=Dbutil.selectQuery(query, con);
			//while (rs.next()){
			//	sb.append("<a href=\"editknownwineslist.jsp?appellation="+rs.getString("appellation")+"\">"+rs.getString("appellation")+"</a> - ");
			//}

			sb.append("<table style=\"table-layout:fixed;\"><tr><th width=50%></th><th width=50%></th></tr>");

			query="select wines.name, lk.id as lid, rk.id as rid, lk.appellation as lap, rk.appellation as rap, lk.wine as lwine, rk.wine as rwine, lk.fulltextsearch as lfull, lk.literalsearch as llit, lk.literalsearchexclude as llitex, rk.fulltextsearch as rfull, rk.literalsearch as rlit, rk.literalsearchexclude as rlitex   from wines, knownwines lk, knownwines rk, knownwinesmatch l join knownwinesmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid) where lk.disabled=false and rk.disabled=false and l.knownwineid=lk.id and r.knownwineid=rk.id and l.wineid=wines.id and lk.appellation='"+Spider.SQLEscape(appellation)+"' group by lwine,rwine order by lk.id, rk.appellation;";
			if (appellation.equals("mostmatches")){
				String list="";
				query="Select a.knownwineid as kw from knownwinesmatch a join knownwinesmatch b on (a.wineid=b.wineid and a.knownwineid!=b.knownwineid) group by kw order by count(*) desc limit 50";
				rs=Dbutil.selectQuery(query, con);
				while (rs.next()){
					list+=","+rs.getString("kw");	
				}
				list=list.substring(1);
				query="select wines.name, lk.id as lid, rk.id as rid, lk.appellation as lap, rk.appellation as rap, lk.wine as lwine, rk.wine as rwine, lk.fulltextsearch as lfull, lk.literalsearch as llit, lk.literalsearchexclude as llitex, rk.fulltextsearch as rfull, rk.literalsearch as rlit, rk.literalsearchexclude as rlitex   from wines, knownwines lk, knownwines rk, knownwinesmatch l join knownwinesmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid) where lk.disabled=false and rk.disabled=false and l.knownwineid=lk.id and r.knownwineid=rk.id and l.wineid=wines.id and lk.id in ("+list+") group by lwine,rwine order by lk.id, rk.appellation;";
			}
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				lextra="";
				lfull=rs.getString("lfull");
				rfullar=rs.getString("rfull").split(" ");
				for (int i=0;i<rfullar.length;i++){
					if (rfullar[i].startsWith("+")){
						if (!lfull.contains(rfullar[i].substring(1))){
							// If empty, add 'Add'
							if (lextra.equals("")) lextra="Add ";
							// Add a term to remove
							lextra=lextra+"<a href=\"updateknownwine.jsp?sql=update knownwines set fulltextsearch=concat(fulltextsearch,&quot; -"+Spider.SQLEscape(rfullar[i].substring(1))+"&quot;) where id="+rs.getString("lid")+";"+"\" target=\"_blank\" onclick=\"mark(this)\">"+" -"+rfullar[i].substring(1)+"</a>";
						}
					}
				}
				rextra="";
				rfull=rs.getString("rfull");
				lfullar=rs.getString("lfull").split(" ");
				for (int i=0;i<lfullar.length;i++){
					if (lfullar[i].startsWith("+")){
						if (!rfull.contains(lfullar[i].substring(1))){
							// If empty, add 'Add'
							if (rextra.equals("")) rextra="Add ";
							// Add a term to remove
							rextra=rextra+"<a href=\"updateknownwine.jsp?sql=update knownwines set fulltextsearch=concat(fulltextsearch,&quot; -"+Spider.SQLEscape(lfullar[i].substring(1))+"&quot;) where id="+rs.getString("rid")+";"+"\" target=\"_blank\" onclick=\"mark(this)\">"+" -"+lfullar[i].substring(1)+"</a>";
							//rextra=rextra+" -"+lfullar[i].substring(1);
						}
					}
				}
				//lSQL="";
				//if (!lextra.equals("")){
				//	lSQL="update knownwines set fulltextsearch=concat(fulltextsearch,&quot;"+Spider.SQLEscape(lextra)+"&quot;) where id="+rs.getString("lid")+";";
				//}
				//rSQL="";
				//if (!rextra.equals("")){
				//	rSQL="update knownwines set fulltextsearch=concat(fulltextsearch,&quot;"+Spider.SQLEscape(rextra)+"&quot;) where id="+rs.getString("rid")+";";
				//}
				sb.append("<tr><td>_____________________________________________</td><td>__________________________________________________</td></tr>");
				sb.append("<tr><td><i>"+rs.getString("name")+"</i></td><td></td></tr>");
				sb.append("<tr><td><a href=\"updateknownwine.jsp?sql=update knownwines set disabled=1 where id="+rs.getString("lid")+";\" target=\"_blank\" onclick=\"mark(this)\">Disable </a>"+rs.getString("lwine")+" ("+rs.getString("lap")+")</td><td><a href=\"updateknownwine.jsp?sql=update knownwines set disabled=1 where id="+rs.getString("rid")+";\" target=\"_blank\" onclick=\"mark(this)\">Disable </a>"+rs.getString("rwine")+" ("+rs.getString("rap")+")</td></tr>");
				sb.append("<tr><td>Full: "+rs.getString("lfull")+", lit: "+rs.getString("llit")+", litex:"+rs.getString("llitex")+"</td><td>Full: "+rs.getString("rfull")+", lit: "+rs.getString("rlit")+", litex:"+rs.getString("rlitex")+"</td></tr>");
				sb.append("<tr><td>"+lextra+"</td>");
				sb.append("<td>"+rextra+"</td></tr>");
				//if (lSQL.equals("")){
				//} else {
				//	sb.append("<tr><td><a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">Add "+lextra+"</a></td>");
				//}
				//if (rSQL.equals("")){
				//	sb.append("<td></td></tr>\n");
				//} else {
				//	sb.append("<td><a href=\"updateknownwine.jsp?sql="+rSQL+"\" target=\"_blank\" onclick=\"mark(this)\">Add "+rextra+"</a></td><tr>\n");
				//}

			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		}
		sb.append("</table>");

		Dbutil.closeConnection(con);

		return sb.toString();
	}
	public static String editDoubleRatedWinesHTML(String appellation){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;

		String query;
		Connection con=Dbutil.openNewConnection();
		if (true){
			try{
				String regionlist=Region.getRegionsAsStringList(appellation);
				sb.append("<table style=\"table-layout:fixed;\"><tr><th width=50%></th><th width=50%></th></tr>");
				if (!"%".equals(appellation)){
					query="select ratedwines.name, lk.id as lid, rk.id as rid, lk.appellation as lap, rk.appellation as rap, lk.wine as lwine, rk.wine as rwine, lk.fulltextsearch as lfull, lk.literalsearch as llit, lk.literalsearchexclude as llitex, rk.fulltextsearch as rfull, rk.literalsearch as rlit, rk.literalsearchexclude as rlitex   from ratedwines, knownwines lk, knownwines rk, ratedwinesmatch l join ratedwinesmatch r on (l.wineid=r.wineid and l.knownwineid>r.knownwineid) where lk.disabled=false and (lk.appellation in ("+regionlist+") or rk.appellation in ("+regionlist+")) and rk.disabled=false and l.knownwineid=lk.id and r.knownwineid=rk.id and l.wineid=ratedwines.id group by lid,rid order by ratedwines.name,lk.id, rk.appellation;";
					rs=Dbutil.selectQuery(query, con);
				} else {
					String selection="";
					//query="Select theselection.lft, count(*) as thecount from (select l.knownwineid as lft, r.knownwineid as rgt from ratedwinesmatch l join ratedwinesmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid) group by l.knownwineid, r.knownwineid) as theselection group by theselection.lft order by thecount desc limit 50;";
					query="select rwm.knownwineid as lft,count(*) from ratedwinesmatch rwm join ratedwines rw on (rwm.wineid=rw.id) where rw.issue>0 group by rwm.knownwineid order by count(*) desc limit 200;";
					rs=Dbutil.selectQuery(query, con);
					while (rs.next()){
						selection+=","+rs.getString("lft");
					}
					if (selection.length()>0){
						query="select ratedwines.name, lk.id as lid, rk.id as rid, lk.appellation as lap, rk.appellation as rap, lk.wine as lwine, rk.wine as rwine, lk.fulltextsearch as lfull, lk.literalsearch as llit, lk.literalsearchexclude as llitex, rk.fulltextsearch as rfull, rk.literalsearch as rlit, rk.literalsearchexclude as rlitex   from ratedwines, knownwines lk, knownwines rk, ratedwinesmatch l join ratedwinesmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid) where lk.disabled=false and (lk.id in ("+selection.substring(1)+") ) and rk.disabled=false and l.knownwineid=lk.id and r.knownwineid=rk.id and l.wineid=ratedwines.id group by lid,rid order by l.wineid, rk.appellation;";
						rs=Dbutil.selectQuery(query, con);
					}

				}
				int wineid=0;
				while (rs.next()){
					lextra="";
					lfull=rs.getString("lfull");
					rfullar=rs.getString("rfull").split(" ");
					for (int i=0;i<rfullar.length;i++){
						if (rfullar[i].startsWith("+")){
							if (!lfull.contains(rfullar[i].substring(1))){
								lextra=lextra+" -"+rfullar[i].substring(1);
							}
						}
					}
					rextra="";
					rfull=rs.getString("rfull");
					lfullar=rs.getString("lfull").split(" ");
					for (int i=0;i<lfullar.length;i++){
						if (lfullar[i].startsWith("+")){
							if (!rfull.contains(lfullar[i].substring(1))){
								rextra=rextra+" -"+lfullar[i].substring(1);
							}
						}
					}
					sb.append("<tr><td>_____________________________________________</td><td>__________________________________________________</td></tr>");
					if (wineid!=rs.getInt("lid")){
						wineid=rs.getInt("lid");
						sb.append("<tr><td><h3>"+rs.getString("lwine")+"</h3></td><td></td></tr>");
						sb.append("<tr><td><input type='text' size=60 id='fulltext"+rs.getString("lid")+"' value='"+rs.getString("lfull")+"'><a onClick='javascript:updateValue("+rs.getString("lid")+",\"fulltext\")'> Update Fulltext</a></td></tr>");
						sb.append("<tr><td><input type='text' id='literal"+rs.getString("lid")+"' value='"+rs.getString("llit")+"'><a onClick='javascript:updateValue("+rs.getString("lid")+",\"literal\")'> Update Literalsearch</a></td><td><input type='text' id='literalexclude"+rs.getString("lid")+"' value='"+rs.getString("llitex")+"'><a onClick='javascript:updateValue("+rs.getString("lid")+",\"literalexclude\")'> Update LiteralsearchExclude</a></td></tr>");
					}
					sb.append("<tr><td><i>"+rs.getString("name")+"</i> <a href='addknownwine.jsp?wine="+rs.getString("name").replace("'", "&apos;")+"' target='_blank'>Add</a> <a href=\"http://images.google.nl/images?q="+rs.getString("name")+"\" target=\"_blank\">Google</a> </td><td></td></tr>");
					sb.append("<tr><td><a href=\"updateknownwine.jsp?sql=update knownwines set disabled=1 where id="+rs.getString("lid")+";\" target=\"_blank\" onclick=\"mark(this)\">Disable </a><a href=\"addknownwine.jsp?id="+rs.getString("lid")+";\" target=\"_blank\" >Edit </a><a href=\"http://images.google.nl/images?q="+rs.getString("lwine")+" "+rs.getString("lap")+"\" target=\"_blank\">Google </a> "+(new File("C:\\labels\\"+rs.getString("lid")).exists()?("<a href=\"showlabels.jsp?name="+rs.getString("lid")+"\" target=\"_blank\">Labels</a> "):(" "))+rs.getString("lwine")+" ("+rs.getString("lap")+")</td><td><a href=\"updateknownwine.jsp?sql=update knownwines set disabled=1 where id="+rs.getString("rid")+";\" target=\"_blank\" onclick=\"mark(this)\">Disable </a><a href=\"addknownwine.jsp?id="+rs.getString("rid")+";\" target=\"_blank\" >Edit </a><a href=\"http://images.google.nl/images?q="+rs.getString("rwine")+" "+rs.getString("rap")+"\" target=\"_blank\">Google </a> "+(new File("C:\\labels\\"+rs.getString("rid")).exists()?("<a href=\"showlabels.jsp?name="+rs.getString("rid")+"\" target=\"_blank\">Labels</a> "):(" "))+rs.getString("rwine")+" ("+rs.getString("rap")+")</td></tr>");
					sb.append("<tr><td>Full: ");
					for (int i=0;i<lfull.split(" ").length;i++){
						String term=Webroutines.URLEncode(Spider.SQLEscape(lfull.split(" ")[i]));
						if (i==0) {term+=" ";} else term=" "+term; 
						lSQL="update knownwines set fulltextsearch=replace(fulltextsearch,&apos;"+term+"&apos;,&apos;&apos;) where id="+rs.getString("lid")+";";
						sb.append("<a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+lfull.split(" ")[i]+"</a> ");
					}
					sb.append(", lit: "+rs.getString("llit")+", litex:"+rs.getString("llitex")+"</td><td>Full: ");
					for (int i=0;i<rfull.split(" ").length;i++){
						String term=Webroutines.URLEncode(Spider.SQLEscape(rfull.split(" ")[i]));
						if (i==0) {term+=" ";} else term=" "+term; 
						rSQL="update knownwines set fulltextsearch=replace(fulltextsearch,&apos;"+term+"&apos;,&apos;&apos;) where id="+rs.getString("rid")+";";
						sb.append("<a href=\"updateknownwine.jsp?sql="+rSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+rfull.split(" ")[i]+"</a> ");
					}
					sb.append(", lit: "+rs.getString("rlit")+", litex:"+rs.getString("rlitex")+"</td></tr>");
					if (lextra.equals("")){
						sb.append("<tr><td></td>");
					} else {
						sb.append("<tr><td>Add ");
						lSQL="";
						for (int i=0;i<lextra.split(" ").length;i++){
							lSQL="update knownwines set fulltextsearch=concat(fulltextsearch,&apos; "+Spider.SQLEscape(lextra.split(" ")[i])+"&apos;) where id="+rs.getString("lid")+";";
							sb.append("<a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+lextra.split(" ")[i]+"</a> ");
						}
						sb.append("</td>");
						//sb.append("<tr><td><a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">Add "+lextra+"</a></td>");
					}
					if (rextra.equals("")){
						sb.append("<td></td></tr>\n");
					} else {
						sb.append("<td>Add ");
						rSQL="";
						for (int i=0;i<rextra.split(" ").length;i++){
							rSQL="update knownwines set fulltextsearch=concat(fulltextsearch,&apos; "+Spider.SQLEscape(rextra.split(" ")[i])+"&apos;) where id="+rs.getString("rid")+";";
							sb.append("<a href=\"updateknownwine.jsp?sql="+rSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+rextra.split(" ")[i]+"</a> ");
						}
						sb.append("</td></tr>\n");
						//sb.append("<tr><td><a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">Add "+rextra+"</a></td>");
					}


				}
				sb.append("</table>");
			}catch (Exception exc){
				Dbutil.logger.error("Problem: ",exc);
			}
		}

		Dbutil.closeConnection(con);

		return sb.toString();
	}

	public static String editBestPQRatedWinesHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;

		String query;
		Connection con=Dbutil.openNewConnection();
		if (true){
			try{
				WineAdvice advice=new WineAdvice();
				advice.setResultsperpage(200);
				advice.setSearchtype(Searchtypes.PQ);
				advice.getAdvice();
				sb.append("<table style=\"table-layout:fixed;\"><tr><th width=50%></th><th width=50%></th></tr>");
				for (int n=0;n<advice.wine.length;n++){
					lextra="";
					rs=Dbutil.selectQuery("select * from knownwines where id="+advice.wine[n].Knownwineid+";", con);
					if (rs.next()){
						lfull=rs.getString("fulltextsearch");
						rfullar=advice.wine[n].Name.split("[ '-,\"]");
						for (int i=0;i<rfullar.length;i++){
							if (!lfull.contains(rfullar[i])){
								lextra=lextra+" -"+rfullar[i];
							}

						}
						sb.append("<tr><td><h3><a href=\"updateknownwine.jsp?sql=update knownwines set disabled=1 where id="+rs.getString("id")+";\" target=\"_blank\" onclick=\"mark(this)\">Disable </a>"+rs.getString("wine")+"</h3> </td><td></td></tr>");
						sb.append("<tr><td><input type='text' size=60 id='fulltext"+rs.getString("id")+"' value='"+rs.getString("fulltextsearch")+"'><a onClick='javascript:updateValue("+rs.getString("id")+",\"fulltext\")'> Update Fulltext</a></td></tr>");
						sb.append("<tr><td><input type='text' id='literal"+rs.getString("id")+"' value='"+rs.getString("literalsearch")+"'><a onClick='javascript:updateValue("+rs.getString("id")+",\"literal\")'> Update Literalsearch</a></td><td><input type='text' id='literalexclude"+rs.getString("id")+"' value='"+rs.getString("literalsearchexclude")+"'><a onClick='javascript:updateValue("+rs.getString("id")+",\"literalexclude\")'> Update LiteralsearchExclude</a></td></tr>");
						sb.append("<tr><td><i>"+advice.wine[n].Name+"</i><a href='addknownwine.jsp?wine="+Webroutines.URLEncode(advice.wine[n].Name)+"' target='_blank'>Add</a></td><td></td></tr>");
						sb.append("<tr><td>Full: ");
						for (int i=0;i<lfull.split(" ").length;i++){
							lSQL="update knownwines set fulltextsearch=replace(fulltextsearch,&apos; "+Webroutines.URLEncode(Spider.SQLEscape(lfull.split(" ")[i]))+"&apos;,&apos;&apos;) where id="+rs.getString("id")+";";
							sb.append("<a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+lfull.split(" ")[i]+"</a> ");
						}
						sb.append("</td>");
						sb.append("</tr>");
						if (lextra.equals("")){
							sb.append("<tr><td></td>");
						} else {
							sb.append("<tr><td>Add ");
							lSQL="";
							for (int i=0;i<lextra.split(" ").length;i++){
								lSQL="update knownwines set fulltextsearch=concat(fulltextsearch,&apos; "+Spider.SQLEscape(lextra.split(" ")[i])+"&apos;) where id="+rs.getString("id")+";";
								sb.append("<a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+lextra.split(" ")[i]+"</a> ");
							}
							sb.append("</td>");
							//sb.append("<tr><td><a href=\"updateknownwine.jsp?sql="+lSQL+"\" target=\"_blank\" onclick=\"mark(this)\">Add "+lextra+"</a></td>");
						}
						if (rextra.equals("")){
							sb.append("<td></td></tr>\n");
						} else {
							sb.append("<td>Add ");
							rSQL="";
							for (int i=0;i<rextra.split(" ").length;i++){
								rSQL="update knownwines set fulltextsearch=concat(fulltextsearch,&apos; "+Spider.SQLEscape(rextra.split(" ")[i])+"&apos;) where id="+rs.getString("rid")+";";
								sb.append("<a href=\"updateknownwine.jsp?sql="+rSQL+"\" target=\"_blank\" onclick=\"mark(this)\">"+rextra.split(" ")[i]+"</a> ");
							}
							sb.append("</td></tr>\n");
						}
					}
				}
				sb.append("</table>");
			}catch (Exception exc){
				Dbutil.logger.error("Problem: ",exc);
			}
		}

		Dbutil.closeConnection(con);

		return sb.toString();
	}




	public static String editKnownWinesInternalDoublesHTML(int row, String region){
		ResultSet doubles;
		ResultSet rs;
		StringBuffer sb=new StringBuffer();
		ResultSet wines;
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection doublescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String html="";
		String disabled="";
		String regionclause="";
		try{
			if (region!=null&&!region.equals("")){
				//				We find all knownwines which match other knownwines for a specific region
				sb.append("<table style=\"style:fixed; width:100%\">");
				query="select knownwineid,count(*) as thecount from knownwinesdoublesinternal join knownwines on (knownwinesdoublesinternal.Knownwineid=knownwines.id) where appellation='"+region+"' and knownwinesdoublesinternal.disabled=false group by knownwineid order by thecount desc,wine limit 200;";
			} else {

				//				We find all knownwines which match other knownwines
				query="select knownwineid,count(*) as thecount from knownwinesdoublesinternal ";
				if (row>0) {		
					query=query+" where knownwineid="+row+" and disabled=false group by knownwineid;";

				} else {
					sb.append("<table style=\"style:fixed; width:100%\">");
					query=query+" where disabled=false group by knownwineid order by thecount desc limit 200;";
				}
			}
			doubles=Dbutil.selectQuery(query, con);
			if (doubles!=null) while (doubles.next()){
				rs=Dbutil.selectQuery("Select * from knownwines where id="+doubles.getString("knownwineid"), con);
				if (rs.next()){
					whereclause="";
					literalsearch="";
					literalterm=rs.getString("literalsearch").replaceAll("\\?", "").replaceAll("\\\\\\. ", " ").split(" ");
					for (int i=0;i<literalterm.length;i++){
						if (literalterm[i].length()>0){
							if (literalsearch.equals("")){
								literalsearch=literalsearch+" wine REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
							} else {
								literalsearch=literalsearch+" AND wine REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

							}
						}
					}

					literaltermexclude=rs.getString("literalsearchexclude").replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
					for (int i=0;i<literaltermexclude.length;i++){
						if (literaltermexclude[i].length()>0){
							if (literalsearch.equals("")){
								literalsearch=literalsearch+" wine NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
							} else {
								literalsearch=literalsearch+" AND wine NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

							}
						}
					}



					whereclause="";
					if (!rs.getString("fulltextsearch").equals("")){
						if (literalsearch.equals("")){
							whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE)";
						} else {
							whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND "+literalsearch;
						}
					} else	whereclause = whereclause+literalsearch;
					if (whereclause.equals(";")){
						Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
					} else {
						query="Select * from knownwines where"+whereclause+" order by wine;";
						//Dbutil.logger.info(query);
						wines=Dbutil.selectQuery(query, winescon);
						if (row==0) sb.append("<tr><td id=\""+rs.getString("id")+"\">");
						sb.append("<table><tr><td><h3>"+rs.getString("wine")+" <i>("+rs.getString("appellation")+")</i></h3><a onClick='javascript:updateValue("+rs.getString("id")+",\"disable\")'> Disable</a></td><td><input type='text' id='literal"+rs.getString("id")+"' value='"+rs.getString("literalsearch")+"'><a onClick='javascript:updateValue("+rs.getString("id")+",\"literal\")'> Update Literalsearch</a></td></tr>");
						sb.append("<tr><td><input type='text' size=60 id='fulltext"+rs.getString("id")+"' value='"+rs.getString("fulltextsearch")+"'><a onClick='javascript:updateValue("+rs.getString("id")+",\"fulltext\")'> Update Fulltext</a></td><td><input type='text' id='literalexclude"+rs.getString("id")+"' value='"+rs.getString("literalsearchexclude")+"'><a onClick='javascript:updateValue("+rs.getString("id")+",\"literalexclude\")'> Update LiteralsearchExclude</a></td></tr>");
						while (wines.next()){
							if (wines.getInt("Disabled")>0) {disabled=" <i>(Disabled)</i>";} else disabled="";
							sb.append("<tr><td>"+wines.getString("wine")+"("+wines.getString("appellation")+")"+disabled+"</td><td></td></tr>");
						}
						wines.close();
						wines=null;
						sb.append("</table>");
						if (row==0) sb.append("</td></tr>");
					}
				}
			}
			if (row==0) sb.append("</table>");

		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		}

		Dbutil.closeConnection(con);
		Dbutil.closeConnection(doublescon);
		Dbutil.closeConnection(winescon);
		//Dbutil.logger.info(sb.toString());
		return sb.toString();
	}

	public static String editDoubleImprovedKnownWinesHTML(){
		ResultSet doubles=null;
		ResultSet rs=null;
		ResultSet wines=null;
		StringBuffer sb=new StringBuffer();
		int bestmatch;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String knownwine;
		String query;
		int knownwineid=0;
		Connection con=Dbutil.openNewConnection();
		Connection doublescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String html="";
		String disabled="";
		String regionclause="";
		String newfulltext="";
		String otherfulltext="";
		String name="";
		try{

			//			We find improved knownwines which match other improved knownwines
			query="SELECT l.knownwineid, count(*) as thecount FROM knownwinesimprovedmatch l join knownwinesimprovedmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid) group by l.knownwineid order by thecount desc limit 100;";
			doubles=Dbutil.selectQuery(query, con);
			if (doubles!=null) while (doubles.next()){
				query="select * from knownwinesimprovement where knownwineid="+doubles.getString("knownwineid")+" order by newmatches desc, id;";
				rs=Dbutil.selectQuery(query, winescon);
				if (rs.next()){
					newfulltext=rs.getString("fulltextsearch");
					knownwineid=rs.getInt("knownwineid");
				}
				Dbutil.closeRs(rs);
				query="select * from knownwines where id="+knownwineid+";";
				rs=Dbutil.selectQuery(query, winescon);
				if (rs.next()){
					name=rs.getString("wine");
				}
				Dbutil.closeRs(rs);
				sb.append("<br/><b>"+name+"</b> <a href='addknownwine.jsp?id="+knownwineid+"' target='_blank'>Edit</a><br/>");
				sb.append("<b>New fulltext: "+newfulltext+"</b><br/>");
				query="SELECT l.* FROM knownwinesimprovedmatch l join knownwinesimprovedmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid and r.knownwineid="+knownwineid+") group by l.knownwineid ;";
				rs=Dbutil.selectQuery(query, winescon);
				while (rs.next()){
					sb.append("<b>Conflict with "+Knownwines.getKnownWineName(rs.getInt("knownwineid"))+"</b> <a href='addknownwine.jsp?id="+rs.getString("knownwineid")+"' target='_blank'>Edit</a><br/>");
					sb.append("Fulltext: "+Dbutil.readValueFromDB("Select * from knownwinesimprovement where knownwineid="+rs.getInt("knownwineid")+" order by newmatches desc, id;", "fulltextsearch")+"<br/>");
					query="SELECT wines.name as name FROM knownwinesimprovedmatch l join knownwinesimprovedmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid and r.knownwineid="+knownwineid+" and l.knownwineid="+rs.getInt("knownwineid")+") join wines on (l.wineid=wines.id);";
					wines=Dbutil.selectQuery(query, winescon);
					while (wines.next()){
						sb.append(wines.getString("name")+"<br/>");
					}
					Dbutil.closeRs(wines);
				}
				Dbutil.closeRs(rs);
			}
			Dbutil.closeRs(doubles);

		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		}

		Dbutil.closeConnection(con);
		Dbutil.closeConnection(doublescon);
		Dbutil.closeConnection(winescon);
		//Dbutil.logger.info(sb.toString());
		return sb.toString();
	}


	public static String almostMatchedHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		Collator collator = Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;
		String problem;
		String newfull;
		Pattern pattern;
		Matcher matcher;

		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			sb.append("<table style=\"table-layout:fixed;\"><tr><th width=50%></th><th width=50%></th></tr>");

			//	 We find all bestmatches with no record in precisematch
			query="select wines.name, knownwines.wine,knownwines.id, knownwines.fulltextsearch, knownwines.literalsearch, knownwines.literalsearchexclude, knownwinesbestmatch.wineid,knownwinesbestmatch.knownwineid,knownwinesmatch.knownwineid as thisisnull from wines, knownwines, knownwinesbestmatch left join knownwinesmatch on (knownwinesmatch.wineid=knownwinesbestmatch.wineid) where wines.id=knownwinesbestmatch.wineid and knownwines.id=knownwinesbestmatch.knownwineid and wines.knownwineid=0 group by wineid having thisisnull is null order by score desc limit 1000;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				lfullar=rs.getString("fulltextsearch").split(" ");
				problem="";
				newfull="";
				for (int i=0;i<lfullar.length;i++){
					if (lfullar[i].startsWith("+")) {
						pattern=Pattern.compile(lfullar[i].substring(1), Pattern.CANON_EQ+Pattern.CASE_INSENSITIVE);
						matcher=pattern.matcher(rs.getString("name"));
						if (!matcher.find()){
							//							if (!rs.getString("name").toLowerCase().contains(lfullar[i].substring(1).toLowerCase())){
							problem=problem+" "+lfullar[i];
						} else {
							newfull=newfull+" "+lfullar[i];
						}
					} else if (lfullar[i].startsWith("-")) {
						pattern=Pattern.compile(lfullar[i].substring(1), Pattern.CANON_EQ+Pattern.CASE_INSENSITIVE);
						matcher=pattern.matcher(rs.getString("name"));
						if (matcher.find()){
							//if (rs.getString("name").toLowerCase().contains(lfullar[i].substring(1).toLowerCase())){
							problem=problem+" "+lfullar[i];
						} else {
							newfull=newfull+" "+lfullar[i];
						}
					} 			
				}
				if (newfull.startsWith(" ")){
					newfull=newfull.substring(1);
				}
				sb.append("<tr><td>"+rs.getString("name")+"</td><td>Best match: "+rs.getString("wine")+"</td></tr>");
				sb.append("<tr><td>Remove <a href=\"updateknownwin.jsp?sql=update knownwines set fulltextsearch=&apos;"+newfull+"&apos; where id="+rs.getString("id")+";\">"+newfull+"<font color='red'>"+problem+"</font></a></td><td>Full: "+rs.getString("fulltextsearch")+", lit: "+rs.getString("literalsearch")+", litex: "+rs.getString("literalsearchexclude")+"</td></tr>");
			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		sb.append("</table>");


		return sb.toString();
	}

	public static String editUnrecognizedRatedWinesHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet matches=null;
		Collator collator = Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;
		String problem;
		String newfull;
		Pattern pattern;
		Matcher matcher;
		String searchterm="";
		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			sb.append("<table style=\"table-layout:fixed;white-space: nowrap;text-overflow:ellipsis;overflow:hidden;\"><tr><th width=10%></th><th width=89%></th></tr>");

			query="select distinct(ratedwines.name), ratedwines.id, ratedwinesmatch.id as thematch from ratedwines left join ratedwinesmatch on (ratedwines.id=ratedwinesmatch.wineid) where ratedwines.knownwineid=0 group by ratedwines.name having thematch is null order by count(*) desc limit 50;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				sb.append("<tr><td></td><td><b>"+rs.getString("name")+"</b> <a href='addknownwine.jsp?wine="+Webroutines.URLEncode(rs.getString("name"))+"' target='_blank'>Add </a><a href=\"http://images.google.nl/images?q="+Webroutines.URLEncode(rs.getString("name"))+"\" target=\"_blank\">Google </a></td></tr>\n");
				searchterm="";
				for (int i=0; i<rs.getString("name").split(" ").length;i++){
					searchterm=searchterm+" +"+rs.getString("name").split(" ")[i];
				}
				searchterm=searchterm.substring(1);
				query="Select *,match wine against ('"+Spider.SQLEscape(searchterm)+"') as score  from knownwines where match wine against ('"+Spider.SQLEscape(searchterm)+"') and knownwines.disabled=0 order by score desc limit 10;";
				matches=Dbutil.selectQuery(query, con);

				while (matches.next()){
					sb.append("<tr><td></td><td><i>"+matches.getString("wine")+" ("+matches.getString("appellation")+")</i></td></tr>\n");
					String fulltext=matches.getString("fulltextsearch");		
					sb.append("<tr id='"+matches.getString("id")+"'><td>"+matches.getString("id")+"</td><td>"+extraTermsRatedWines(fulltext,matches.getString("id"), rs.getString("id"))+" (Lit. "+matches.getString("literalsearch")+", Lit. Excl. "+matches.getString("literalsearchexclude")+") <a href='addknownwine.jsp?id="+matches.getInt("id")+"' target='_blank'>Edit</a>  <a onClick='javascript:testKnownWine("+matches.getInt("id")+")'>Test</a> <a href=\"updateknownwine.jsp?sql=update knownwines set disabled=1 where id="+matches.getInt("id")+";\" target=\"_blank\">Disable</a></td></tr>\n");
				}
			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(matches);
			Dbutil.closeConnection(con);
		}

		sb.append("</table>");



		return sb.toString();
	}

	public static String editTooCheapWinesHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet matches=null;
		Collator collator = Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);String lfull;
		String lextra="";
		String[] rfullar;
		String rfull;
		String rextra="";
		String[] lfullar;
		String lSQL;
		String rSQL;
		String problem;
		String newfull;
		Pattern pattern;
		Matcher matcher;
		String searchterm="";
		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			sb.append("<table style=\"table-layout:fixed;white-space: nowrap;text-overflow:ellipsis;overflow:hidden;\"><tr><th width=10%></th><th width=89%></th></tr>");

			query="select * from ratinganalysis group by knownwineid,vintage order by (minpriceeuroex/avgpriceeuroex) limit 500;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				sb.append("<tr><td></td><td><b>"+getKnownWineAndRegionName(rs.getInt("knownwineid"))+" "+rs.getInt("vintage")+", average price &euro; "+Webroutines.formatPrice(rs.getDouble("avgpriceeuroex"))+"</b> <a href='addknownwine.jsp?id="+Webroutines.URLEncode(rs.getString("knownwineid"))+"' target='_blank'>Edit</a> <a href='/index.jsp?name="+Webroutines.URLEncode(getKnownWineName(rs.getInt("knownwineid")))+" "+rs.getInt("vintage")+"' target='_blank'>Search</a> </td></tr>\n");
				query="Select * from wines where knownwineid="+rs.getInt("knownwineid")+" and vintage="+rs.getInt("vintage")+" and size=0.75 order by priceeuroex limit 3";
				matches=Dbutil.selectQuery(query, con);

				while (matches.next()){
					sb.append("<tr><td>(&euro; "+Webroutines.formatPrice(matches.getDouble("priceeuroex"))+")</td><td><i>"+matches.getString("name")+"</i></td></tr>\n");
				}
			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(matches);
			Dbutil.closeConnection(con);
		}

		sb.append("</table>");



		return sb.toString();
	}


	public static String editImprovedKnownWinesHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet wines=null;
		ResultSet matches=null;
		String searchterm="";
		String origfulltext="";
		String name="";
		String lit="";
		String litex="";
		String newfulltext="";
		String result;
		int knownwineid;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		try{
			sb.append("<table style=\"table-layout:fixed;white-space: nowrap;text-overflow:ellipsis;overflow:hidden;\"><tr><th width=50%></th><th width=50%></th></tr>");

			query="select knownwineid,oldmatches, conflicts, max(newmatches) as newmatches from knownwinesimprovement where knownwineid not in (SELECT distinct(l.knownwineid) FROM knownwinesimprovedmatch l join knownwinesimprovedmatch r on (l.wineid=r.wineid and l.knownwineid!=r.knownwineid)) group by knownwineid order by newmatches desc limit 250;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="select * from knownwines where id="+rs.getString("knownwineid")+";";
				wines=Dbutil.selectQuery(query, winescon);
				if (wines.next()){
					origfulltext=wines.getString("fulltextsearch");
					lit=wines.getString("literalsearch");
					litex=wines.getString("literalsearchexclude");
					name=wines.getString("wine");
				}
				query="select * from knownwinesimprovement where knownwineid="+rs.getString("knownwineid")+" order by newmatches desc, id;";
				wines=Dbutil.selectQuery(query, winescon);
				if (wines.next()){
					newfulltext=wines.getString("fulltextsearch");
				}

				sb.append("<tr><td><b>"+name+"</b> (<a href='addknownwine.jsp?id="+rs.getString("knownwineid")+"' target=_blank'>Edit</a>)</td><td></td></tr>\n");
				sb.append("<tr><td id='"+rs.getString("knownwineid")+"'>");
				sb.append(deleteTermsimprovedKnownWines(origfulltext,newfulltext,rs.getString("knownwineid")));
				sb.append("</td><td></td></tr>\n");

				result=testWines(rs.getString("knownwineid"),newfulltext,lit,litex).replaceAll("------A.*", "");
				String[] resultarray=result.split("\\\\n");
				sb.append("<tr><td>");
				for (String wine :resultarray){
					sb.append(wine+" ");
					if (!wine.equals("")&&!wine.startsWith("---")&&!wine.startsWith("Select"))
						for (String term:origfulltext.split(" ")){
							term=term.toLowerCase();
							if (term.startsWith("+")&&!newfulltext.toLowerCase().contains(term)&&!wine.toLowerCase().contains(term.substring(1))){
								sb.append(" <font style='color:red;'>"+term.substring(1)+"</font>");
							}
						}
					sb.append("<br/>");
				}
				sb.append("</td><td></td></tr>");


			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeRs(matches);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		sb.append("</table>");

		return sb.toString();
	}
	public static String editBestMatchUnmatchedWinesHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet wines=null;
		ResultSet matches=null;
		String searchterm="";
		String fulltext="";
		String lit="";
		String litex="";
		String[] terms;
		String missingterms;
		String wine;
		String result;
		int knownwineid;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		Knownwines kw=Knownwines.getInstance();
		try{
			sb.append("<table style=\"table-layout:fixed;\"><tr><th width=70%></th><th width=30%></th></tr>");
			query="select * from wines where knownwineid=0 and match (name) against ('+(whisk* grappa glas* cognac 40 vodka Wodka armagnac calvados sherry jerez manzanilla ximenez malt spiegelau riedel bourbon licor beer olio olijf* olive oil brandy rum ron pasta gin)' in boolean mode);";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="update knownwinesbestmatch set knownwineid=0 where wineid="+rs.getString("id")+";";
				Dbutil.executeQuery(query, con);
			}	
			query="select *,count(*) from knownwinesbestmatch kwbm join wines on (kwbm.wineid=wines.id) where wines.knownwineid=0 and kwbm.knownwineid>0 group by kwbm.knownwineid order by count(*) desc limit 250;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="select * from knownwines where id="+rs.getString("knownwineid")+";";
				wines=Dbutil.selectQuery(query, winescon);
				if (wines.next()){
					lit=wines.getString("literalsearch");
					litex=wines.getString("literalsearchexclude");
					fulltext=wines.getString("fulltextsearch");
					terms=fulltext.toLowerCase().split("\\+");
					wine=wines.getString("wine");
					Dbutil.closeRs(wines);
					if (!wine.contains("70")&&!wine.contains("CL")&&!wine.contains("Magnum")&&!wine.contains("0,75")){
						sb.append("<tr><td><a href='addknownwine.jsp?id="+rs.getString("knownwineid")+"' target='_blank'>Edit</a> <b>"+wine+"</b></td><td></td>");
						sb.append("<tr><td>fulltext "+fulltext+", lit. "+lit+", litex "+litex+"</td><td></td>");
						query="select * from wines join knownwinesbestmatch kwbm on (kwbm.wineid=wines.id) where wines.knownwineid=0 and kwbm.knownwineid="+rs.getString("knownwineid")+";";
						wines=Dbutil.selectQuery(query, winescon);
						while (wines.next()){
							missingterms="";
							wine=wines.getString("name").toLowerCase();
							for (int i=1;i<terms.length;i++){
								if (wine.equals(wine.replaceAll("(^| )"+terms[i].trim()+"($| )", ""))){
									missingterms=missingterms+"<td style=\"width:60px;\">"+terms[i]+"</td>";
								} else {
									missingterms=missingterms+"<td style=\"width:60px;\"></td>";
								}
							}
							sb.append("<tr><td>"+wines.getString("name")+"</td><td><table style=\"table-layout:fixed;\"><tr>"+missingterms+"</tr></table></td></tr>");

						}
						Dbutil.closeRs(wines);
					}
				}
			}
			sb.append("</table>");
		}catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeRs(matches);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		return sb.toString();
	}

	public static String deleteTermsimprovedKnownWines(String origfulltext, String newfulltext, String knownwineid){
		Collator collator = Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);String lfull;
		StringBuffer sb=new StringBuffer();
		String[] rfullar=origfulltext.split(" ");
		for (int i=0;i<rfullar.length;i++){
			if (rfullar[i].startsWith("+")&&!newfulltext.contains(rfullar[i])){
				sb.append(" <a href=\"javascript:removeterm("+knownwineid+",'"+rfullar[i]+"','"+newfulltext.replace("+", "%2B")+"')\" style=\"decoration:none; color:red;\">"+rfullar[i]+"</a>");
			} else{
				sb.append(" "+rfullar[i]);
			}

		}

		return sb.toString();
	}

	public static String removeImprovedTerm(String term, String knownwineid, String improvedfulltext){
		String fulltextsearch=Dbutil.readValueFromDB("Select fulltextsearch from knownwines where id="+knownwineid+";", "fulltextsearch");
		try{
			fulltextsearch=fulltextsearch.replaceAll("\\"+term+"( |$)","");
			fulltextsearch=fulltextsearch.replaceAll("  "," ");
			if (fulltextsearch.startsWith(" ")) fulltextsearch=fulltextsearch.substring(1);
			String query="Update knownwines set fulltextsearch='"+Spider.SQLEscape(fulltextsearch)+"' where id="+knownwineid+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem deleting fulltext term "+term,exc);
		}
		fulltextsearch=Dbutil.readValueFromDB("Select fulltextsearch from knownwines where id="+knownwineid+";", "fulltextsearch");
		return deleteTermsimprovedKnownWines(fulltextsearch,improvedfulltext,knownwineid);

	}




	public static String extraTermsRatedWines(String fulltext, String id, String idratedwine){
		String winename=Dbutil.readValueFromDB("Select name from ratedwines where id="+idratedwine+";", "name");
		Collator collator = Collator.getInstance(Locale.US);
		collator.setStrength(Collator.PRIMARY);
		String[] lfullar;
		Pattern pattern;
		Matcher matcher;
		Connection con=Dbutil.openNewConnection();
		lfullar=fulltext.split(" ");
		try{
			for (int i=0;i<lfullar.length;i++){
				if (lfullar[i].startsWith("+")) {
					pattern=Pattern.compile(lfullar[i].substring(1).replace("*", "[^ ]*"), Pattern.CANON_EQ+Pattern.CASE_INSENSITIVE);
					matcher=pattern.matcher(winename);
					if (!matcher.find()){
						fulltext=fulltext.replaceAll("\\"+lfullar[i].replace("*", "[^ ]*")+"($| )", "<font color=\"red\"><B><a href=\"javascript:removeterm("+id+",'"+lfullar[i]+"',"+idratedwine+")\" style=\"decoration:none; color:red;\">"+lfullar[i]+"</a></B></font> ");
					} else {
						//
					}
				} else if (lfullar[i].startsWith("-")) {
					pattern=Pattern.compile(lfullar[i].substring(1).replace("*", "[^ ]*"), Pattern.CANON_EQ+Pattern.CASE_INSENSITIVE);
					matcher=pattern.matcher(winename);

					if (matcher.find()){
						fulltext=fulltext.replaceAll(lfullar[i].replace("*", "[^ ]*")+"(?!<)", "<font color=\"red\"><B><a href=\"javascript:removeterm("+id+",'"+lfullar[i]+"',"+idratedwine+")\" style=\"decoration:none; color:red;\"> "+lfullar[i]+"</a></B></font> ");						
					} else {
						//
					}
				} 			
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem while creating extra terms. String fulltext:"+fulltext+", String id:"+id+", String idratedwine:"+idratedwine+".",e);
		} finally{
			Dbutil.closeConnection(con);
		}

		return fulltext;
	}


	public static String redundantWineTermsHTML(int row, String region){
		ResultSet rs=null;
		StringBuffer sb=new StringBuffer();
		ResultSet wines=null;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String fulltext;
		String literalsearch;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		String currentterm="";
		try{
			//sb.append("<table style=\"table-layout:fixed;\"><tr><th width=50%></th><th width=50%></th></tr>");

			//	 We find all knownwines where search terms are redundant.
			// We consider a term redundant when a fulltext search term can be deleted
			// and the search will still only match itself.
			query="select * from knownwines where appellation like '"+region+"'";
			if (row>0) {
				query=query+" and id="+row;
			} else {
				sb.append("<table style=\"table-layout:fixed;\"><tr><th width=5%></th><th width=50%></th><th width=45%></th></tr>");
			}
			query=query+" order by wine limit 1000;";
			rs=Dbutil.selectQuery(query, con);
			if (rs!=null) while (rs.next()){
				whereclause="";
				literalsearch="";
				literalterm=rs.getString("literalsearch").replaceAll("\\?", "").replaceAll("\\\\\\. ", " ").split(" ");
				for (int i=0;i<literalterm.length;i++){
					if (literalterm[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
						} else {
							literalsearch=literalsearch+" AND wine REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

						}
					}
				}

				literaltermexclude=rs.getString("literalsearchexclude").replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
				for (int i=0;i<literaltermexclude.length;i++){
					if (literaltermexclude[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine NOT REGEXP '^[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
						} else {
							literalsearch=literalsearch+" AND wine NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

						}
					}
				}

				// We keep this the same for every search as it was made by hand
				// Now we filter out searchterms one by one

				fulltext=rs.getString("fulltextsearch");
				String fulltextterms[]=("+dummy "+rs.getString("fulltextsearch")).split(" ");
				int origid=rs.getInt("id");
				boolean redundant=false;
				boolean originalhasonematch=false;
				for (String term:fulltextterms){
					if (originalhasonematch||term.equals("+dummy")){
						currentterm=term;
						Dbutil.logger.info(term);
						whereclause="";
						if (!rs.getString("fulltextsearch").equals("")){
							if (literalsearch.equals("")){
								whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch")).replaceAll("\\"+term+"( |$)", "")+"' IN BOOLEAN MODE)";
							} else {
								whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch")).replaceAll("\\"+term+"( |$)", "")+"' IN BOOLEAN MODE) AND "+literalsearch;
							}
						} else	whereclause = whereclause+literalsearch;
						if (whereclause.equals(";")){
							Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
						} else {
							query="Select * from knownwines where"+whereclause+historywhereclause+";";
							wines=Dbutil.selectQuery(query, winescon);
							boolean otherrow=false;
							while (wines.next()){
								if (wines.getInt("id")!=origid) otherrow=true;
							}
							if (!otherrow&&wines.last()&&wines.getRow()==1){
								if (term.equals("+dummy")){
									originalhasonematch=true;
								} else {
									redundant=true;
									fulltext=fulltext.replace(term, "<font color=\"red\"><B><a href=\"javascript:removeterm("+origid+",'"+term+"')\" style=\"decoration:none; color:red;\">"+term+"</a></B></font>");
								}
							}
						}
						wines=null;
					}
				}
				if (redundant){
					if (row==0){
						sb.append("<tr id='"+origid+"'><td>"+origid+"</td><td>"+fulltext+"</td><td style='white-space: nowrap;text-overflow:ellipsis;overflow:hidden;'>"+rs.getString("Wine")+"</td></tr>");
					} else {
						sb.append("<td>"+origid+"</td><td>"+fulltext+"</td><td style='white-space: nowrap;text-overflow:ellipsis;overflow:hidden;'>"+rs.getString("Wine")+"</td>");
					}
				}

			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem, term= "+currentterm,exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		if (row==0) sb.append("</table>");


		return sb.toString();
	}


	public static String redundantWineTermsHTML2(int row, String region){
		ResultSet rs=null;
		StringBuffer sb=new StringBuffer();
		ResultSet wines=null;
		String whereclause;
		String fulltext;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String html="";
		String currentterm="";
		try{
			//	 We try to find the minimum set of search terms that gives a match for only itself.
			query="select * from knownwines where appellation like '"+region+"'";
			if (row>0) {
				query=query+" and id="+row;
			} else {
				sb.append("<table style=\"table-layout:fixed;\"><tr><th width=5%></th><th width=50%></th><th width=45%></th></tr>");
			}
			query=query+" order by wine limit 30;";
			rs=Dbutil.selectQuery(query, con);
			if (rs!=null) while (rs.next()){
				// First check that the current search returns only one row: itself
				whereclause="";
				whereclause=Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false);
				whereclause=whereclause.replaceAll("name","wine");
				query="Select * from knownwines where"+whereclause+";";
				wines=Dbutil.selectQuery(query, winescon);
				boolean otherrow=false;
				int origid=rs.getInt("id");
				while (wines.next()){
					if (wines.getInt("id")!=origid) otherrow=true;
				}
				if (!otherrow&&wines.last()){
					html="\n<h4>"+rs.getString("wine")+"</h4><table>";
					// Now we filter out searchterms one by one
					fulltext=rs.getString("fulltextsearch");
					int n=1; // The number of terms we need as a minimum;
					String fulltextterms[]=(rs.getString("fulltextsearch")).split(" ");
					int totalterms=fulltextterms.length;
					boolean ready=false;
					while (!ready&&n<totalterms-1){ //max 1 less than all terms
						Integer[] counter=new Integer[n]; 
						counter[0]=0;
						for (int i=1;i<n;i++){
							counter[i]=counter[i-1]+1;
						} 
						// Start position with n=3, totalterms=4: 0,1,2
						while (counter[n-1]<totalterms){
							// Do testing here
							fulltext="";
							for (int i=0;i<counter.length;i++){
								fulltext+=" "+fulltextterms[counter[i]];
							}
							whereclause="";
							whereclause=Knownwines.whereClauseKnownWines(fulltext, rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false);
							whereclause=whereclause.replaceAll("name","wine");
							query="Select * from knownwines where"+whereclause+" and id!="+origid+";";
							wines=Dbutil.selectQuery(query, winescon);
							if (!wines.next()){
								if (testWines(rs.getInt("id"),fulltext,rs.getString("literalsearch"),rs.getString("literalsearchexclude"),true)[2]==0){


									// OK, single match found
									ready=true;
									html+="\n<tr id='"+origid+"'><td>"+origid+"</td><td>"+fulltext;
									for (int i=0;i<fulltextterms.length;i++){
										if (!fulltext.contains(fulltextterms[i])){
											html+="<font color='red'>"+fulltextterms[i]+"</font>";
										}
									}
									html+="<a onClick=\"javascript:testKnownWine('"+Webroutines.URLEncode(rs.getString("id"))+"','"+Webroutines.URLEncode(fulltext)+"','"+Webroutines.URLEncode(rs.getString("literalsearch"))+"','"+Webroutines.URLEncode(rs.getString("literalsearchexclude"))+"')\">Test</a>";
									html+=", lit. "+rs.getString("literalsearch")+", lit.excl. "+rs.getString("literalsearchexclude")+"</td><td style='white-space: nowrap;text-overflow:ellipsis;overflow:hidden;'>"+rs.getString("Wine")+"</td></tr>";		

								}
							}

							//							 update counters
							updateCounters(counter,n-1,totalterms-1,n);
							Dbutil.closeRs(wines);


						}


						n++;
					}
					if (ready){
						html+="</table>";
						sb.append(html);
					}

				}

			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem, term= "+currentterm,exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		if (row==0) sb.append("</table>");


		return sb.toString();
	}

	public static String producersFirstNameHTML(int row, String region){
		ResultSet rs=null;
		ResultSet rs2=null;
		StringBuffer sb=new StringBuffer();
		ResultSet wines=null;
		String whereclause;
		String[] producer;
		boolean redundant=false;
		boolean stopnow;
		String fulltext;
		String query;
		int i=0;
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String html="";
		String currentterm="";
		try{
			//	 We try to find producer names with redundant terms, especially first names
			query="select * from knownwines ";
			if (row>0) {
				query=query+" and id="+row;
			} else {
				sb.append("<table style=\"table-layout:fixed;\"><tr><th width=5%></th><th width=50%></th><th width=45%></th></tr>");
			}
			query=query+" group by producer order by count(*) desc limit 200;";
			rs=Dbutil.selectQuery(query, con);
			if (rs!=null) while (rs.next()){
				redundant=false;
				producer=Spider.SQLEscape(rs.getString("producer")).split(" ");
				i=1;
				stopnow=false;
				while (!stopnow&&(i<(producer.length))){
					fulltext="";
					for (int j=i;j<producer.length;j++){
						producer[j]=producer[j].replace("&", "");
						if (producer[j].length()>1){
							fulltext="+"+producer[j]+" ";
						}
					}
					//Dbutil.logger.info("select count(*) as thecount from knownwines where match(producer) against ('"+fulltext+"' in boolean mode) and producer !='"+Spider.SQLEscape(rs.getString("producer"))+"';");
					int matches=Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where match(producer) against ('"+fulltext+"' in boolean mode) and producer !='"+Spider.SQLEscape(rs.getString("producer"))+"';","thecount");
					if (matches==0){
						redundant=true;
						i++;
					} else {
						stopnow=true;
					}
				}
				if (redundant){
					i--;
					fulltext="";
					fulltext+=rs.getString("producer")+": ";
					for (int j=0;j<i;j++){
						fulltext+="<s>"+producer[j]+"</s> ";
					}
					for (int j=i;j<producer.length;j++){
						fulltext+=producer[j]+" ";
					}
					sb.append(fulltext);
					sb.append("<br/>");
				}
			}

		}catch (Exception exc){
			Dbutil.logger.error("Problem, term= "+currentterm,exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		if (row==0) sb.append("</table>");


		return sb.toString();
	}



	public static void analyseRedundantWineTerms(){
		Dbutil.logger.info("Starting job to analyse redundant terms in knownwines");
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String fulltext;
		String fulltextnegative;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String currentterm="";
		String otherrows="";
		String whereotherrows="";
		int newmatches;
		String bestmatch="";
		Integer[] result;
		Integer[] counter;
		int n;
		String[] fulltextterms;
		int totalterms;
		boolean ready;
		int origid;
		String literaladapted;
		try{
			//	 We try to find the minimum set of search terms that gives a match for only itself.
			Dbutil.executeQuery("delete from knownwinesimprovement;");
			Dbutil.executeQuery("delete from knownwinesimprovedmatch;");
			query="select * from knownwines;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				// First check that the current search returns only one row: itself
				// in case more rows are returned store the rownumbers: it may not become less restrictive
				newmatches=0;
				otherrows="";
				whereotherrows="";
				whereclause="";
				literaladapted=rs.getString("literalsearch");
				literaladapted=literaladapted.replaceAll("((^| )[^ ])($| )", "");
				literaladapted=literaladapted.replaceAll("((^| )[^ ][^ ])($| )", "");
				literaladapted=literaladapted.trim();
				whereclause=Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false);
				whereclause=whereclause.replaceAll("name","wine");
				query="Select * from knownwines where"+whereclause+";";
				wines=Dbutil.selectQuery(query, winescon);
				origid=rs.getInt("id");
				while (wines.next()){
					if (wines.getInt("id")!=origid) otherrows+=","+rs.getString("id");
				}
				Dbutil.closeRs(wines);
				if (otherrows.length()>0){
					whereotherrows=" and id not in("+otherrows.substring(1)+") ";
				}
				// Now we filter out searchterms one by one
				fulltext=rs.getString("fulltextsearch").replaceAll(" +", " ").replaceAll("-[^ ]+( |$)", "");
				fulltextnegative=" "+rs.getString("fulltextsearch").replaceAll(" +", " ").replaceAll("\\+[^ ]+( |$)", "");
				n=1; // The number of terms we need as a minimum;
				fulltextterms=(fulltext.split("\\+"));
				for (int i=0;i<fulltextterms.length;i++){
					fulltextterms[i]="+"+fulltextterms[i].trim();
				}
				totalterms=fulltextterms.length;
				ready=false;
				while (!ready&&n<totalterms-1){ //max 1 less than all terms
					counter=new Integer[n]; 
					counter[0]=0;
					for (int i=1;i<n;i++){
						counter[i]=counter[i-1]+1;
					} 
					// Start position with n=3, totalterms=4: 0,1,2
					while (counter[n-1]<totalterms){
						// Do testing here
						fulltext="";
						for (int i=0;i<counter.length;i++){
							fulltext+=" "+fulltextterms[counter[i]];
						}
						whereclause="";
						whereclause=Knownwines.whereClauseKnownWines(fulltext+fulltextnegative, rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false);
						whereclause=whereclause.replaceAll("name","wine");
						query="Select id from knownwines where"+whereclause+whereotherrows+" limit 2;";
						wines=Dbutil.selectQuery(query, winescon);
						if (wines.next()){
							if (wines.getInt("id")==origid&&!wines.next()){
								result=testWines(rs.getInt("id"),fulltext,rs.getString("literalsearch"),rs.getString("literalsearchexclude"),true);
								if (result[2]==0){
									// OK, single match found
									result=testWines(rs.getInt("id"),fulltext,rs.getString("literalsearch"),rs.getString("literalsearchexclude"),false);
									if (result[0]>newmatches||newmatches==0){
										bestmatch=fulltext+fulltextnegative;
										newmatches=result[0];
										Dbutil.executeQuery("delete from knownwinesimprovement where knownwineid="+rs.getString("id")+";",winescon);
										Dbutil.executeQuery("insert into knownwinesimprovement (knownwineid,fulltextsearch,newmatches,oldmatches,conflicts) values ("+rs.getString("id")+",'"+Spider.SQLEscape(fulltext)+"',"+result[0]+","+result[1]+","+result[2]+");",winescon);
									}
									ready=true;

								}
							}
						}
						Dbutil.closeRs(wines);
						//							 update counters
						updateCounters(counter,n-1,totalterms-1,n);
					}
					n++;
				}
				if (ready&&newmatches>0){
					query="Select id from wines where "+Knownwines.whereClauseKnownWines(bestmatch, rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false)+";";
					wines=Dbutil.selectQuery(query,winescon);
					while (wines.next()){
						Dbutil.executeQuery("insert into knownwinesimprovedmatch(wineid,knownwineid) values ("+wines.getString("id")+","+rs.getString("id")+");");
					}
					Dbutil.closeRs(wines);
					System.gc();

				}

			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem, term= "+currentterm,exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}
		Dbutil.logger.info("Finished job to analyse redundant terms in knownwines");

	}

	public static Integer[] updateCounters(Integer[] counter, int reel, int max, int reels){
		try{
			if (counter[reel]<max+reel-reels+1||reel==0){
				counter[reel]++;
				for (int i=reel+1;i<reels;i++){
					counter[i]=counter[i-1]+1;
				}
			} else {
				counter=updateCounters(counter,reel-1,max, reels);
			}
		} catch (Exception e){
			Dbutil.logger.error(reel+" "+max+" ");
		}
		return counter;
	}




	public static String removeTerm(String row, String term, String ratedwineid){
		int id=0;
		String fulltextsearch=Dbutil.readValueFromDB("Select fulltextsearch from knownwines where id="+row+";", "fulltextsearch");
		try{
			id=Integer.valueOf(row);
			//Dbutil.logger.info(term);
			fulltextsearch=fulltextsearch.replace(term,"");
			fulltextsearch=fulltextsearch.replaceAll("  "," ");
			if (fulltextsearch.startsWith(" ")) fulltextsearch=fulltextsearch.substring(1);
			String query="Update knownwines set fulltextsearch='"+Spider.SQLEscape(fulltextsearch)+"' where id="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem deleting fulltext term "+term,exc);
		}
		if (id>0) {
			if (ratedwineid==null||ratedwineid.equals("")){
				// This is the case if we are looking for internal doubles in knownwines
				return redundantWineTermsHTML(id,"%"); 
			} else  {
				// This is the case if we removed a term from unmatched rated wines
				return "<td>"+row+"</td><td>"+extraTermsRatedWines(fulltextsearch,row,ratedwineid)+"</td>";
			}

		}
		return "Error";

	}

	public static String updateFulltext(String row, String value){
		int id=0;
		try{
			id=Integer.valueOf(row);
			String query="Update knownwines set fulltextsearch='"+Spider.SQLEscape(value)+"' where id="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem updating literaltext value "+value,exc);
		}
		if (id>0) return editKnownWinesInternalDoublesHTML(id,""); 
		return "Error";

	}



	public static boolean editKnownwine(String wine, String appellation, String fulltext, String literalsearch, String literalsearchexclude, String id, String color, String dryness, String sparkling){
		int row=0;
		try{
			if (id.equals("")){
				String query="Insert into knownwines (wine,appellation,fulltextsearch,literalsearch,literalsearchexclude) values ('"+Spider.SQLEscape(wine)+"','"+Spider.SQLEscape(appellation)+"','"+Spider.SQLEscape(fulltext)+"','"+Spider.SQLEscape(literalsearch)+"','"+Spider.SQLEscape(literalsearchexclude)+"');";
				//Dbutil.logger.info(query);
				row=Dbutil.executeQuery(query);
			} else {
				String query="update knownwines set wine='"+Spider.SQLEscape(wine)+"', appellation='"+Spider.SQLEscape(appellation)+"', fulltextsearch='"+Spider.SQLEscape(fulltext)+"',literalsearch='"+Spider.SQLEscape(literalsearch)+"',literalsearchexclude='"+Spider.SQLEscape(literalsearchexclude)+"', color='"+Spider.SQLEscape(color)+"',dryness='"+Spider.SQLEscape(dryness)+"',sparkling="+Spider.SQLEscape(sparkling)+" where id="+id+";";
				//Dbutil.logger.info(query);
				row=Dbutil.executeQuery(query);
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem inserting or updating knownwine.",exc);
		}
		if (row>0) return true;
		return false;

	}

	public static String getKnownWinesListHTML(String fulltext){
		String html="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			String query="Select * from knownwines where match (wine) against ('"+Spider.SQLEscape(fulltext)+"'in boolean mode) limit 100;";
			//Dbutil.logger.info(query);
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				html+=(rs.getBoolean("disabled")?"<strike>":"")+rs.getString("wine")+"("+rs.getString("appellation")+")"+(rs.getBoolean("disabled")?"</strike>":"")+" <a href='addknownwine.jsp?id="+rs.getString("id")+"' target='_blank'> Edit</a><br/>";			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem retrieving knownwines.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html;

	}


	public static String getRatedWinesListHTML(String knownwineid){
		String html="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			html+="Rated wines with this knownwineid: <br/>";
			String query="Select name, author, avg(rating) as rating from ratedwines where knownwineid="+knownwineid+" group by name,author;";
			//Dbutil.logger.info(query);
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				html+=rs.getString("author")+": "+rs.getString("name")+" ("+rs.getInt("rating")+" pts.)<br/>";
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem retrieving knownwines.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html;

	}

	public static String getCheapestWinesHTML(String knownwineid, int n){
		String html="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			html+="Cheapest wines with this knownwineid: <br/>";
			String query="Select * from wines where knownwineid="+knownwineid+" order by priceeuroex limit "+n+";";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				html+=rs.getString("name")+" ("+Webroutines.formatPrice(rs.getDouble("priceeuroex"))+")<br/>";
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem retrieving knownwines.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html;

	}


	public static String updateLiteral(String row, String value){
		int id=0;
		try{
			id=Integer.valueOf(row);
			String query="Update knownwines set literalsearch='"+Spider.SQLEscape(value)+"' where id="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem updating literaltext value "+value,exc);
		}
		if (id>0) return editKnownWinesInternalDoublesHTML(id,""); 
		return "Error";

	}

	public static String updateLiteralExclude(String row, String value){
		int id=0;
		try{
			id=Integer.valueOf(row);
			String query="Update knownwines set literalsearchexclude='"+Spider.SQLEscape(value)+"' where id="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem updating literalexclude value "+value,exc);
		}
		if (id>0) return editKnownWinesInternalDoublesHTML(id,""); 
		return "Error";

	}
	
	public static void disableKnownwine(int knownwineid){
		Dbutil.executeQuery("update ratedwines set knownwineid=0 where knownwineid="+knownwineid);
		Dbutil.executeQuery("delete from aiitems where itemid="+knownwineid);
		Dbutil.executeQuery("delete from aiitemproperties where itemid="+knownwineid);
		Dbutil.executeQuery("delete from aiitempropsconsolidated where itemid="+knownwineid);
		Dbutil.executeQuery("update knownwines set disabled=1 where id="+knownwineid);
		Dbutil.executeQuery("update wines set knownwineid=0,manualknownwineid=-2 where knownwineid="+knownwineid);
		Dbutil.executeQuery("Update knownwinesdoublesinternal set matches=0,disabled=1 where knownwineid="+knownwineid+";");
		//Recognizer.recognizeDeletedKnownWine(knownwineid);
		
	}
	
	public static void queueForAnalysis(int knownwineid){
		Dbutil.executeQuery("update ratedwines set manualknownwineid=-2 where knownwineid="+knownwineid);
		Dbutil.executeQuery("update wines set manualknownwineid=-2 where knownwineid="+knownwineid);
		
	}
	
	public static void queueProducerForAnalysis(int producerid){ 
		String producer=Dbutil.readValueFromDB("select * from kbproducers where id="+producerid, "name");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from knownwines where producer='"+Spider.SQLEscape(producer)+"';";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				queueForAnalysis(rs.getInt("id"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}


	public static String disableKnownwine(String row){
		int id=0;
		try{
			id=Integer.valueOf(row);
			String query="Update knownwines set disabled=1 where id="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
			query="Update knownwinesdoublesinternal set disabled=1 where knownwineid="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem disabling knownwine "+id,exc);
		}
		if (id>0) return editKnownWinesInternalDoublesHTML(id,""); 
		return "Error";

	}
	public static String enableKnownwine(String row){
		int id=0;
		try{
			id=Integer.valueOf(row);
			String query="Update knownwines set disabled=0 where id="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
			query="Update knownwinesdoublesinternal set disabled=0 where knownwineid="+row+";";
			//Dbutil.logger.info(query);
			Dbutil.executeQuery(query);
		} catch (Exception exc){
			Dbutil.logger.error("Problem enabling knownwine "+id,exc);
		}
		if (id>0) return editKnownWinesInternalDoublesHTML(id,""); 
		return "Error";

	}


	public static void Knownwinesprecise(int history, boolean includeregion,boolean refreshall, String table){ 
		Dbutil.logger.info("Starting job to recognize "+table+", history="+history);
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		try{
			knownwinescon.setAutoCommit(false);
			if (history>0){
				long longtime = new java.util.Date().getTime();
				longtime=longtime-(long)history*1000*3600*24;
				Timestamp date = new Timestamp(longtime);
				historywhereclause=" and createdate>'"+date.toString()+"'";
			} else if (refreshall){
				Dbutil.executeQuery("update "+table+" set knownwineid=0, region=0;");
			}
			Dbutil.executeQuery("delete from knownwinesmatch;");

			rs=Dbutil.selectQueryRowByRow("Select * from knownwines where disabled=false;", con);
			while (rs.next()){
				whereclause=whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"), includeregion);

				if (whereclause.equals(";")||whereclause.equals("")){
					Dbutil.logger.debug("Lege zoekstring voor id "+rs.getString("id"));
				} else {
					query="Select * from "+table+" where"+whereclause+historywhereclause+" and knownwineid=0;";

					wines=Dbutil.selectQuery(query, winescon);
					while (wines.next()){
						Dbutil.executeQuery("Insert into knownwinesmatch (wineid,knownwineid) values ('"+wines.getString("id")+"','"+rs.getString("id")+"');",knownwinescon);
					}
					Dbutil.closeRs(wines);

				}
			}
			knownwinescon.commit();
			rs.close();
			rs=null;
			System.gc();
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(knownwinescon);
		}

		Dbutil.logger.info("Finished job to recognize "+table);
	}


	/*
	 * This routine looks for the most restricted wine in a list.
	 * That is, if a wine is matched by two knownwines, one of them is a generic wine and the other a special cuvee,
	 * we should choose the special cuvee because it is more restrictive. 
	 * For instance: Colin Chassagne Montrachet and Colin Chassagne Montrachet Les Chenevottes. 
	 * If a wine matches both, of course is is the latter one because the first is more generic, its criteria match also the special cuvee.
	 * To find if a wine is more restrictive, we will match the criteria of all matching wines against the other wines. 
	 * If we find that there is exactly one wine which does not match the other wines, that is the wine we are looking for. 
	 */
	public static ArrayList<Integer> getMostRestrictedKnownWine(ArrayList<Integer> wines){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		String whereclausei;
		String whereclausej;
		String wineidlist;
		try {
			for (int i=0;i<wines.size();i++){
				for (int j=i+1;j<wines.size();j++){
					query="Select * from knownwines where id="+wines.get(i);
					rs=Dbutil.selectQuery(query, con);
					if (rs.next());
					whereclausei=whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"), false);
					whereclausei=whereclausei.replace("match (name", "match (wine,appellation");
					whereclausei=whereclausei.replace("name", "wine");
					Dbutil.closeRs(rs);
					query="Select * from knownwines where id="+wines.get(j);
					rs=Dbutil.selectQuery(query, con);
					if (rs.next());
					whereclausej=whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"), false);
					whereclausej=whereclausej.replace("match (name", "match (wine,appellation");
					whereclausej=whereclausej.replace("name", "wine");
					Dbutil.closeRs(rs);

					query="Select * from knownwines where "+whereclausei+" and id="+wines.get(j)+";";
					rs=Dbutil.selectQuery(query, con);
					boolean iselectsj=rs.next();
					Dbutil.closeRs(rs);
					query="Select * from knownwines where "+whereclausej+" and id="+wines.get(i)+";";
					rs=Dbutil.selectQuery(query, con);
					boolean jselectsi=rs.next();
					Dbutil.closeRs(rs);
					if (iselectsj&&jselectsi) {
						whereclausei=whereclausei.replace("match (wine,appellation", "match (wine");
						whereclausej=whereclausej.replace("match (wine,appellation", "match (wine");
						query="Select * from knownwines where "+whereclausei+" and id="+wines.get(j)+";";
						rs=Dbutil.selectQuery(query, con);
						iselectsj=rs.next();
						Dbutil.closeRs(rs);
						query="Select * from knownwines where "+whereclausej+" and id="+wines.get(i)+";";
						rs=Dbutil.selectQuery(query, con);
						jselectsi=rs.next();
						Dbutil.closeRs(rs);
					}
					if (iselectsj&&!jselectsi) {
						wines.remove(i); //remove i
						j=9999;
						i=i-1;
					}
					if (!iselectsj&&jselectsi) {
						wines.remove(j);//remove j
						j=j-1;
					} 


				}

			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return wines;
	}

	public static String getAllRestrictedKnownWines(ArrayList<Integer> wines){
		String result="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		String whereclause;
		String wineidlist;
		try {
			for (int i=0;i<wines.size();i++){
				query="Select * from knownwines where id="+wines.get(i);
				rs=Dbutil.selectQuery(query, con);
				if (rs.next());
				whereclause=whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"), false);
				whereclause=whereclause.replace("name", "wine");
				wineidlist="";
				for (int j=0;j<wines.size();j++){
					if (j!=i){
						wineidlist+=","+wines.get(j);
					}
				}
				wineidlist=wineidlist.substring(1);
				query="Select * from knownwines where "+whereclause+" and id in ("+wineidlist+");";
				rs=Dbutil.selectQuery(query, con);
				if (!rs.next()){
					result+=","+wines.get(i);
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		if (result.length()>1) result=result.substring(1);
		return result;
	}

	public static String testKnownWine(String id){
		String result="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query="";
		String whereclause;
		try {
			query="Select * from knownwines where id="+id+";";
			rs=Dbutil.selectQuery(query, con);
			if (rs.next());
			whereclause=whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), rs.getString("appellation"), false);
			query="Select * from ratedwines where "+whereclause+";";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				result+="\\n"+rs.getString("name");
			}


		} catch (Exception e) {
			Dbutil.logger.error("Problem: ",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		result=(query+"\\n"+result).replace(";", "");
		return result;
	}

	public static String testWines(String id,String full, String lit, String litex){
		String result="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query="";
		String whereclause=whereClauseKnownWines(full,lit,litex,"",false);
		try {
			query="Select * from wines where "+whereclause+" and knownwineid!="+id+" order by knownwineid desc;";
			rs=Dbutil.selectQuery(query, con);
			int lastknownwineid=1;
			if (rs.next()){
				if (rs.getInt("knownwineid")>0){
					result+="\\n------Possible conflicts:------";
				}
			}
			rs.beforeFirst();
			while (rs.next()){
				if (rs.getInt("knownwineid")==0&&lastknownwineid>0){
					result+="\\n-------New Matches:------------";
				}
				lastknownwineid=rs.getInt("knownwineid");
				result+="\\n"+rs.getString("name").replace("\"", "&quot;");
			}
			Dbutil.closeRs(rs);
			query="Select * from wines where "+whereclause+" and knownwineid="+id+";";
			rs=Dbutil.selectQuery(query, con);
			if (rs.next()){
				result+="\\n------Already matched wines:------";

			}
			rs.beforeFirst();
			while (rs.next()){
				result+="\\n"+rs.getString("name").replace("\"", "&quot;");
			}



		} catch (Exception e) {
			Dbutil.logger.error("Problem: ",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		result=(query+"\\n"+result).replace(";", "");
		return result;
	}

	public static Integer[] testWines(int id,String full, String lit, String litex, boolean fast){
		Integer[] result=new Integer[3];
		// 1: new matches. 2: old matches. 3: conflicts
		// if fast=true, only look for conflicts and limit search results
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query="";
		String whereclause=whereClauseKnownWines(full,lit,litex,"",false);
		try {
			if (!fast){
				query="Select count(*) as thecount from wines where "+whereclause+" and knownwineid=0;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					result[0]=rs.getInt("thecount");
				}
				Dbutil.closeRs(rs);
				query="Select count(*) as thecount from wines where "+whereclause+" and knownwineid="+id+";";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					result[1]=rs.getInt("thecount");
				}
				Dbutil.closeRs(rs);
				query="Select count(*) as thecount from wines where "+whereclause+" and knownwineid>0 and knownwineid!="+id+";";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					result[2]=rs.getInt("thecount");
				}
				Dbutil.closeRs(rs);
			} else {
				query="Select id from wines where "+whereclause+" and knownwineid>0 and knownwineid!="+id+" limit 1;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					result[2]=1;
				} else {
					result[2]=0;
				}
				Dbutil.closeRs(rs);

			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return result;
	}


	public static String whereClauseKnownWines(String fulltextsearch, String literalsearch, String literalsearchexclude, String region, boolean includeregion){
		// This routine uses the parameters to compose a SQL where clause 
		// that will match a wine decription against these parameters 
		// fulltext uses the MySQL fulltext search feature
		// literalsearch is used to check terms in order:
		// 		an underscore is replaced by a space, 
		//		so a search for term1_term2 shows records with "term1 term2" 
		// literalsearchexclude is the exact opposite: it excludes records with a literal text.

		String whereclause;
		String literalsearchterm="";
		String regionsearch="";
		String[] regionterms;
		String[] literalterm;
		String[] literaltermexclude;
		String wordstart="(^|�|[^[:alnum:]])";
		String wordend="($|[^[:alnum:]]|�)";
		String underscore="([^[:alnum:]()]|�)+"; // () because of (chateau) Margaux
		for (int i=0;i<Configuration.synonyms.length;i=i+2){
			if (!literalsearch.toLowerCase().equals(literalsearch.toLowerCase().replaceAll("(?=( |^))"+Configuration.synonyms[i].toLowerCase()+"(?=( |$))", ""))){
				//replace with synonym
				literalsearch=literalsearch.toLowerCase().replaceAll("(?=( |^))"+Configuration.synonyms[i].toLowerCase()+"(?=( |$))", "");
				literalsearch+=" ("+Configuration.synonyms[i].replace(" +", "_")+"|"+Configuration.synonyms[i+1]+")";
			}if (!fulltextsearch.toLowerCase().equals(fulltextsearch.toLowerCase().replaceAll("\\+"+Configuration.synonyms[i].toLowerCase().replace("+", "\\+")+"(?=( |$))", ""))){
				//replace with synonym
				fulltextsearch=fulltextsearch.toLowerCase().replaceAll("\\+"+Configuration.synonyms[i].toLowerCase().replace("+", "\\+")+"(?=( |$))", "");
				literalsearch+=" ("+Configuration.synonyms[i].replace(" +", "_")+"|"+Configuration.synonyms[i+1]+")";
			}

		}
		for (int i=0;i<Configuration.synonyms.length;i=i+2){
			if (!fulltextsearch.toLowerCase().equals(fulltextsearch.toLowerCase().replaceAll("-"+Configuration.synonyms[i].toLowerCase()+"(?=( |$))", ""))){
				//replace with synonym
				fulltextsearch=fulltextsearch.toLowerCase().replaceAll("-"+Configuration.synonyms[i].toLowerCase()+"(?=( |$))", "");
				literalsearchexclude+=" ("+Configuration.synonyms[i]+"|"+Configuration.synonyms[i+1]+")";
			}
		}

		whereclause="";


		if ((includeregion&&region.trim()!="")){
			region=region.replaceAll("Unknown","");
			region=region.replaceAll("IGT","");
			region=region.replaceAll("Hills","");
			region=region.replaceAll("District","");
			region=region.replaceAll("County","");
			region=region.replaceAll("Valley","");
			region=region.replaceAll("Saint","");
			region=region.replaceAll("St\\."," ");
			region=region.replaceAll("Grand","");
			region=region.replaceAll("Cru( |$)","");
			region=region.replaceAll("Region","");
			region=region.replaceAll("Coast","");
			region=region.replaceAll("-"," ");
			region=region.replaceAll("[.']"," ");
			regionterms=region.split(" ");
			for (int i=0;i<regionterms.length;i++){
				if (regionterms[i].length()>0){
					regionsearch+=" +"+regionterms[i];


				}
			}
		}


		literalsearchterm="";
		literalterm=literalsearch.replaceAll("\\\\\\. ", " ").split(" ");
		for (int i=0;i<literalterm.length;i++){
			if (literalterm[i].length()>0){
				if (literalsearchterm.equals("")){
					literalsearchterm=literalsearchterm+" lower(name) REGEXP '"+wordstart+Spider.replaceString((Spider.replaceString(literalterm[i],"_",underscore)), "'", "\\'")+wordend+"'";
				} else {
					literalsearchterm=literalsearchterm+" AND lower(name) REGEXP '"+wordstart+Spider.replaceString((Spider.replaceString(literalterm[i],"_",underscore)), "'", "\\'")+wordend+"'";

				}
			}
		}

		literaltermexclude=literalsearchexclude.replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
		for (int i=0;i<literaltermexclude.length;i++){
			if (literaltermexclude[i].length()>0){
				if (literalsearchterm.equals("")){
					literalsearchterm=literalsearchterm+" lower(name) NOT REGEXP '"+wordstart+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_",underscore)), "'", "\\'")+wordend+"'";
				} else {
					literalsearchterm=literalsearchterm+" AND lower(name) NOT REGEXP '"+wordstart+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_",underscore)), "'", "\\'")+wordend+"'";

				}
			}
		}
		whereclause="";
		if (!fulltextsearch.equals("")){
			if (literalsearchterm.equals("")){
				whereclause = " match (name) against ('"+Spider.SQLEscape(fulltextsearch)+regionsearch+"' IN BOOLEAN MODE)";
			} else {
				whereclause = " match (name) against ('"+Spider.SQLEscape(fulltextsearch)+regionsearch+"' IN BOOLEAN MODE) AND "+literalsearchterm;
			}
		} else	whereclause = whereclause+literalsearchterm;


		return whereclause;
	}

	public static String determineKnownWine(String input){ 
		int id=determineKnownWineIdFuzzy(input,false); // It turns out the fuzzy method is no good. Only use boolean mode
		if (id==0){
			id=Knownwines.getInstance().bestMatchKnownWineId(input);
		}
		if (id==0){
			return input;
		} else {
			return getKnownWineName(id);
		}
	}


	public static int determineKnownWineIdFuzzy(String input, boolean fuzzy){ 
		ResultSet rs=null;
		input=input.replaceAll("\\d\\d\\d\\d", "");
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String[] inputarray=input.split(" ");
		String inputfulltext="";
		String literalsearch;
		String query;
		String booleanmode="";
		if (!fuzzy) booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		String historywhereclause="";
		try{
			String fulltext=Aitools.filterCompleteTerm(Aitools.filterPunctuation(input)).replaceAll("\\s+", " +");
			rs=Dbutil.selectQuery("select knownwineid, match (name) against ('"+fulltext+"') as score from wines where match (name) against ('"+fulltext+"') and knownwineid!=0 limit 1;",con);
			if (rs.next()){
				return rs.getInt("knownwineid");  
					
			}
			/*
			
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			//Dbutil.executeQuery("CREATE TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',  PRIMARY KEY(`name`),  FULLTEXT `fulltext`(`name`)) ENGINE = MYISAM;", con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',	  FULLTEXT `namefulltext` (`name`));", con);
			Dbutil.executeQuery("Insert into tempwine (name) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"');", con);
			if (fuzzy){ // necessary: otherwise fulltextindex returns score 0 for all matching words
				Dbutil.executeQuery("Insert into tempwine (name) values ('werwer werwer werwer');", con);
				Dbutil.executeQuery("Insert into tempwine (name) values ('werwer werwer werwer');", con);
				Dbutil.executeQuery("Insert into tempwine (name) values ('werwer werwer werwer');", con);
			}
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `wine` varchar(255) NOT NULL DEFAULT '', `appellation` varchar(255) NOT NULL DEFAULT '',  `fulltextsearch` text NOT NULL,  `disabled` tinyint(1) NOT NULL DEFAULT '0', `literalsearch` varchar(255) NOT NULL DEFAULT '', `literalsearchexclude` varchar(255) NOT NULL DEFAULT '', `doubles` int(10) unsigned NOT NULL DEFAULT '0', `numberofwines` int(10) unsigned NOT NULL DEFAULT '0', `type` VARCHAR(45) DEFAULT null,   `color` varchar(10) NOT NULL DEFAULT '',  `dryness` varchar(20) NOT NULL DEFAULT '',  `sparkling` tinyint(1) NOT NULL DEFAULT '0',  `samename` tinyint(1) NOT NULL DEFAULT '0', `score` DOUBLE NOT NULL DEFAULT 0.0, PRIMARY KEY (`id`), KEY `Wine` (`wine`),  KEY `Appellation` (`appellation`),	  FULLTEXT KEY `winefulltext` (`wine`),  FULLTEXT KEY `wineappelfulltext` (`wine`,`appellation`)) AUTO_INCREMENT=98529 DEFAULT CHARSET=utf8;",con);
			Dbutil.executeQuery("Insert into firstselection select id,wine, appellation,fulltextsearch,disabled, literalsearch, literalsearchexclude,doubles,numberofwines, type,color,dryness,sparkling,samename,  match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines where disabled=false having score>0 order by score desc limit 200;",con);
			if (fuzzy){
				Dbutil.executeQuery("update firstselection set score=0;",con);

			}
			rs=Dbutil.selectQuery("Select * from firstselection;", con);
			while (rs.next()){

				whereclause="";
				literalsearch="";
				if (!rs.getString("literalsearch").equals("")){
					literalterm=rs.getString("literalsearch").replaceAll("\\?", "").replaceAll("\\\\\\. ", " ").split(" ");
					for (int i=0;i<literalterm.length;i++){
						if (literalterm[i].length()>0){
							if (literalsearch.equals("")){
								literalsearch=literalsearch+" name REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
							} else {
								literalsearch=literalsearch+" AND name REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

							}
						}
					}
				}
				if (!rs.getString("literalsearchexclude").equals("")){
					literaltermexclude=rs.getString("literalsearchexclude").replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
					for (int i=0;i<literaltermexclude.length;i++){
						if (literaltermexclude[i].length()>0){
							if (literalsearch.equals("")){
								literalsearch=literalsearch+" name NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
							} else {
								literalsearch=literalsearch+" AND name NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

							}
						}
					}
				}


				whereclause="";
				if (!rs.getString("fulltextsearch").equals("")){
					if (literalsearch.equals("")){
						whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+")";
					} else {
						whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+") AND "+literalsearch;
					}
				} else	whereclause = whereclause+literalsearch;
				if (whereclause.equals(";")){
					Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
				} else {
					if (fuzzy){
						query="Select *, match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"') as score from tempwine where name='"+Spider.SQLEscape(input)+"';";
					} else {
						query="Select *, match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"') as score from tempwine where"+whereclause+historywhereclause+";";						
					}
					wines=Dbutil.selectQuery(query, con);
					//Dbutil.logger.info("Query="+query);
					if (wines.next()){
						if (!fuzzy){
							int id=rs.getInt("id");
							return id;
						} else {
							Dbutil.executeQuery("Update firstselection set score="+wines.getDouble("score")+" where id="+rs.getInt("id")+";",con);
						}
					}
					Dbutil.closeRs(wines);

				}
			}
			Dbutil.closeRs(rs);
			if (!fuzzy){
				return 0;
			} else{
				int result=0;
				rs=Dbutil.selectQuery("select * from firstselection order by score desc limit 1;", con);
				if (rs.next()){
					result=rs.getInt("id");
				}
				return result;
			}
			*/
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		
		return 0;
	}

	public static Boolean containsWineName(String input){  
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String[] inputarray=splitStringForFulltext(input);
		String inputfulltext="";
		String literalsearch;
		String query;
		String booleanmode="";
		booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		boolean skip=false;
		String[] toocommon={"more"};
		for (String term:toocommon){
			if(!"".equals(Webroutines.getRegexPatternValue("(?:^|\\W)("+term+")(?:$|\\W)", input, Pattern.CASE_INSENSITIVE))) skip=true;
		}
		if(!skip) try{
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			//Dbutil.executeQuery("CREATE TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',  PRIMARY KEY(`name`),  FULLTEXT `fulltext`(`name`)) ENGINE = MYISAM;", con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',`id` int(10) unsigned NOT NULL ,	  FULLTEXT `namefulltext` (`name`));", con);
			Dbutil.executeQuery("Insert into tempwine (name,id) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"',1);", con);
			Dbutil.executeQuery("Insert into tempwine (name,id) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"',2);", con);
			Dbutil.executeQuery("Insert into tempwine (name,id) values ('',3);", con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `wine` varchar(255) NOT NULL DEFAULT '', `appellation` varchar(255) NOT NULL DEFAULT '', `producer` varchar(255) NOT NULL DEFAULT '',  `fulltextsearch` text NOT NULL,  `disabled` tinyint(1) NOT NULL DEFAULT '0', `literalsearch` varchar(255) NOT NULL DEFAULT '', `literalsearchexclude` varchar(255) NOT NULL DEFAULT '', `doubles` int(10) unsigned NOT NULL DEFAULT '0', `numberofwines` int(10) unsigned NOT NULL DEFAULT '0', `type` VARCHAR(45) DEFAULT null,   `color` varchar(10) NOT NULL DEFAULT '',  `dryness` varchar(20) NOT NULL DEFAULT '',  `sparkling` tinyint(1) NOT NULL DEFAULT '0',  `samename` tinyint(1) NOT NULL DEFAULT '0', `score` DOUBLE NOT NULL DEFAULT 0.0, PRIMARY KEY (`id`), KEY `Wine` (`wine`),  KEY `Appellation` (`appellation`),	  FULLTEXT KEY `winefulltext` (`wine`),  FULLTEXT KEY `wineappelfulltext` (`wine`,`appellation`)) AUTO_INCREMENT=98529 DEFAULT CHARSET=utf8;",con);
			Dbutil.executeQuery("Insert into firstselection select id,wine, appellation,producer,fulltextsearch,disabled, literalsearch, literalsearchexclude,doubles,numberofwines, type,color,dryness,sparkling,samename,  match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines where disabled=false having score>0 order by score desc limit 100;",con);
			rs=Dbutil.selectQuery("Select * from firstselection order by score desc;", con);
			while (rs.next()){
				//row 1: wine name plus appellation
				Dbutil.executeQuery("update tempwine set name='"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290-rs.getString("appellation").length())))+" "+Spider.SQLEscape(rs.getString("appellation"))+"' where id=1;", con);
				//row 2: wine name plus appellation plus producer
				Dbutil.executeQuery("update tempwine set name='"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290-rs.getString("appellation").length()-rs.getString("producer").length())))+" "+Spider.SQLEscape(rs.getString("appellation"))+" "+Spider.SQLEscape(rs.getString("producer"))+"' where id=2;", con);
				//row 3: just appellation and producer. Should not match and if they do, check for row 1 to match 
				Dbutil.executeQuery("update tempwine set name='"+Spider.SQLEscape(rs.getString("appellation"))+" "+Spider.SQLEscape(rs.getString("producer"))+"' where id=3;", con);
				boolean dosearch=rs.getString("fulltextsearch").equals("");
				if (!dosearch){
					whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+")";
					query="Select * from tempwine where"+whereclause+";";						
					wines=Dbutil.selectQuery(query, con);
					//Dbutil.logger.info("Query="+query);
					if (wines.next()){
						dosearch=true;
					}
				}
				if (dosearch){
					whereclause="";
					literalsearch="";
					if (!rs.getString("literalsearch").equals("")){
						literalterm=rs.getString("literalsearch").replaceAll("\\?", "").replaceAll("\\\\\\. ", " ").split(" ");
						for (int i=0;i<literalterm.length;i++){
							if (literalterm[i].length()>0){
								if (literalsearch.equals("")){
									literalsearch=literalsearch+" name REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
								} else {
									literalsearch=literalsearch+" AND name REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

								}
							}
						}
					}
					if (!rs.getString("literalsearchexclude").equals("")){
						literaltermexclude=rs.getString("literalsearchexclude").replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
						for (int i=0;i<literaltermexclude.length;i++){
							if (literaltermexclude[i].length()>0){
								if (literalsearch.equals("")){
									literalsearch=literalsearch+" name NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
								} else {
									literalsearch=literalsearch+" AND name NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

								}
							}
						}
					}


					whereclause="";
					if (!rs.getString("fulltextsearch").equals("")){
						if (literalsearch.equals("")){
							whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+")";
						} else {
							whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+") AND "+literalsearch;
						}
					} else	whereclause = whereclause+literalsearch;
					if (whereclause.equals(";")){
						Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
					} else {
						
//						query="Select * from tempwine;";						
//						wines=Dbutil.selectQuery(query, con);
//						while(wines.next())	Dbutil.logger.info("Record "+wines.getInt("id")+": "+wines.getString("name"));
						
						query="Select group_concat(id) from tempwine where"+whereclause+historywhereclause+" order by id;";						
						/*
						wines=Dbutil.selectQuery(query, con);
						Dbutil.logger.info("Query="+query);
						if (wines.next()) Dbutil.logger.info("Result="+wines.getString(1));
						*/
						wines=Dbutil.selectQuery(query, con);
						if (wines.next()){ // Always group_concat returns a row
							String result=wines.getString(1);
							if(result==null) return false;
							if(result.contains("1")) { // record 1: wine + region matching
								return true;
							} else if (result.contains("3")){ 
								// Record 3 matching: 3 was an empty wine name plus region plus producer
								// If 3 is matching, 1 should also match otherwise wine does not contain a wine name
								return false;
							} else if (result.contains("2")){ 
								// Only record 2 matching means record 1 was missing the appellation but does contain the wine name
								return true;
							} else {
								// No match
								Dbutil.logger.error("This code should never be reached. Result="+result);
							
							}
						}
						Dbutil.closeRs(wines);

					}
				}
			}
			Dbutil.closeRs(rs);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
			return false;
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(winescon);
		return false;
	}


	public static String[] splitStringForFulltext(String input){
		return input.replaceAll("[\\d'\\-#%&*_()\":;<>,./?\\\\]"," ").replaceAll("\\s.(?=\\s)"," ").split("\\s+");
	}
	
	public static Boolean containsRegionName(String input){ 
		boolean result=false;
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String[] inputarray=splitStringForFulltext(input);
		String inputfulltext="";
		String literalsearch;
		String query;
		String booleanmode="";
		booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		try{
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',	  FULLTEXT `namefulltext` (`name`));", con);
			Dbutil.executeQuery("Insert into tempwine (name) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"');", con);

			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `region` varchar(255) NOT NULL DEFAULT '', `fulltextsearch` text NOT NULL,`score` DOUBLE NOT NULL DEFAULT 0.0,  PRIMARY KEY (`id`), KEY `region` (`region`),   FULLTEXT KEY `regionfulltext` (`region`))  DEFAULT CHARSET=utf8;",con);
			Dbutil.executeQuery("Insert into firstselection select id,region, '',match (region) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from regions having score>0 order by score desc limit 200;",con);
			rs=Dbutil.selectQuery("Select * from firstselection;", con);
			while (rs.next()){
				String fulltext="";
				for (String field:splitStringForFulltext(rs.getString("region"))) fulltext+=" +"+field;
				Dbutil.executeQuery("update firstselection set fulltextsearch='"+fulltext+"' where id="+rs.getString("id"),con);
			}
			rs=Dbutil.selectQuery("Select * from firstselection;", con);

			while (!result&&rs.next()){

				whereclause="";
				if (!rs.getString("fulltextsearch").equals("")){
					whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+");";
					query="Select * from tempwine where match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' in boolean mode);";
					wines=Dbutil.selectQuery(query, con);
					//Dbutil.logger.info("Query="+query);
					if (wines.next()){
						result=true;
					}
					Dbutil.closeRs(wines);

				}
			}
			Dbutil.closeRs(rs);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
			return result;

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(winescon);
		return false;
	}


	public static Boolean containsProducerName(String input){ 
		boolean result=false;
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String[] inputarray=splitStringForFulltext(input);
		String inputfulltext="";
		String literalsearch;
		String query;
		String booleanmode="";
		booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String historywhereclause="";
		boolean skip=false;
		String[] toocommon={"plus", "par"};
		for (String term:toocommon){
			if(!"".equals(Webroutines.getRegexPatternValue("(?:^|\\W)("+term+")(?:$|\\W)", input, Pattern.CASE_INSENSITIVE))) skip=true;
		}
		input=input.replaceAll("\\d", " ");
		if(!skip) try{
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',	  FULLTEXT `namefulltext` (`name`));", con);
			Dbutil.executeQuery("Insert into tempwine (name) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"');", con);

			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `producer` varchar(255) NOT NULL DEFAULT '', `fulltextsearch` text NOT NULL,`score` DOUBLE NOT NULL DEFAULT 0.0,  PRIMARY KEY (`id`), KEY `producer` (`producer`),   FULLTEXT KEY `producerfulltext` (`producer`))  DEFAULT CHARSET=utf8;",con);
			Dbutil.executeQuery("Insert into firstselection (producer,fulltextsearch,score) select distinct(producer),'',1 from (select producer,id, '',match (producer) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines having score>0 order by score desc) sel limit 200;",con);
			rs=Dbutil.selectQuery("Select * from firstselection;", con);
			while (rs.next()){
				String fulltext="";
				for (String field:splitStringForFulltext(rs.getString("producer"))) fulltext+=" +"+field;
				Dbutil.executeQuery("update firstselection set fulltextsearch='"+fulltext+"' where id="+rs.getString("id"),con);
			}
			rs=Dbutil.selectQuery("Select * from firstselection;", con);

			while (!result&&rs.next()){

				whereclause="";
				if (!rs.getString("fulltextsearch").equals("")){
					whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+");";
					query="Select * from tempwine where match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' in boolean mode);";
					wines=Dbutil.selectQuery(query, con);
					//Dbutil.logger.info("Query="+query);
					if (wines.next()){
						result=true;
					}
					Dbutil.closeRs(wines);

				}
			}
			Dbutil.closeRs(rs);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
			return result;

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}

		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
		Dbutil.closeConnection(con);
		Dbutil.closeConnection(winescon);
		return false;
	}



	public static boolean doesWineMatch(String input, int knownwineid){ 
		boolean match=false;
		ResultSet rs=null;
		String whereclause;
		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',	  FULLTEXT `namefulltext` (`name`));", con);
			Dbutil.executeQuery("Insert into tempwine (name) values ('"+Spider.SQLEscape(input)+"');", con);
			query="select * from knownwines where id="+knownwineid+";";
			rs=Dbutil.selectQuery(query, con);
			if (rs.next()){
				whereclause=whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false);
				query="select * from tempwine where "+whereclause+";";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					match=true;
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return match;
	}


	public static boolean doesVintageMatch(String winedescription, String vintagesearch){ 
		boolean match=false;
		try{
			String vintageclause;
			Matcher matcher;
			Pattern pattern;
			pattern=Pattern.compile("(?:^|\\D)(\\d\\d\\d\\d)(?:$|\\D)");
			matcher=pattern.matcher(winedescription);
			while (!match&&matcher.find())		{
				vintageclause=Wineset.getVintageClause(vintagesearch);
				if (vintageclause.equals("")){
					match=true;
				} else {
					vintageclause=vintageclause.replace("Vintage ", matcher.group(1)+" ");
					vintageclause=vintageclause.replace("Vintage=", matcher.group(1)+"=");
					if (Dbutil.readIntValueFromDB("select ("+vintageclause+") as vintagematch", "vintagematch")>0){
						match=true;
					}
				}

			} 
		}catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		}
		return match;
	}



	public static String bestMatchKnownWine(String input){ 
		int id=Knownwines.getInstance().bestMatchKnownWineId(input);
		if (id>0) return getKnownWineName(id);
		return input;
	}

	public int bestMatchKnownWineId(String input){
		return bestMatchKnownWineId(input,false);
	}

	
	public static int bestMatchKnownWineId(String input,boolean musthavewines){ 
		int result=0;
		ResultSet rs=null;
		input=input.replaceAll("\\W"," ").replaceAll("\\d", " ").replaceAll(" +", " ").trim();
		String[] inputarray=input.split(" ");
		String inputfulltext="";
		String query;
		Connection con=Dbutil.openNewConnection();
		try{
			for (int i=0;i<inputarray.length;i++){
				if (inputarray[i].length()>1)	inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			rs=Dbutil.selectQuery("select *, match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines where match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"')>5 and disabled=false "+(musthavewines?"and numberofwines>0 ":"")+" order by score desc, length(wine) limit 1",con);
			if (rs.next()){
				result=rs.getInt("id");
				return result;
			}
			Dbutil.closeRs(rs);
			rs=null;
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return 0;
	}


	public static ArrayList<String> getAlternatives(String input){ 
		ResultSet rs=null;
		String[] inputarray=input.split("[ '-]");
		String inputfulltext="";
		NumberFormat knownwineformat  = new DecimalFormat("000000");	
		Connection con=Dbutil.openNewConnection();
		ArrayList<String> alternatives=new ArrayList<String>();
		try{
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			String query="select *, match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines where disabled=false and numberofwines>0 order by score desc limit 10;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				if (rs.getInt("numberofwines")>0&&rs.getFloat("score")>5){
					alternatives.add((rs.getString("wine")+(rs.getString("appellation").equals("Unknown")?"":(" ("+rs.getString("appellation")+")"))));
					alternatives.add((rs.getBoolean("samename")?(knownwineformat.format(rs.getInt("id"))+" "):"")+rs.getString("wine"));
					alternatives.add(rs.getString("numberofwines"));
				}
			}
			if (alternatives.size()==0){
				inputfulltext="";
				for (int i=0;i<inputarray.length;i++){
					inputfulltext=inputfulltext+" +"+inputarray[i]+"*";
				}
				query="select *, match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"' in boolean mode) as score from knownwines where disabled=false and numberofwines>0 order by score desc limit 10;";
				rs=Dbutil.selectQuery(query,con);
				while (rs.next()){
					if (rs.getInt("numberofwines")>0&&rs.getFloat("score")>0.5){
						alternatives.add((rs.getString("wine")+(rs.getString("appellation").equals("Unknown")?"":(" ("+rs.getString("appellation")+")"))));
						alternatives.add((rs.getBoolean("samename")?(rs.getString("id")+" "):"")+rs.getString("wine"));
						alternatives.add(rs.getString("numberofwines"));
					}
				}

			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return alternatives;
	}

	public static ArrayList<String> getNewAlternatives(String input){ 
		ResultSet rs=null;
		String[] inputarray=input.split("[ '-]");
		String inputfulltext="";
		NumberFormat knownwineformat  = new DecimalFormat("000000");	
		Connection con=Dbutil.openNewConnection();
		ArrayList<String> alternatives=new ArrayList<String>();
		try{
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			String query="select *, match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines where disabled=false and numberofwines>0 order by score desc, numberofwines desc limit 21;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				if (rs.getInt("numberofwines")>0&&rs.getFloat("score")>5){
					alternatives.add(rs.getString("wine"));
					alternatives.add(rs.getString("appellation").equals("Unknown")?"":rs.getString("appellation"));
					alternatives.add((rs.getBoolean("samename")?(knownwineformat.format(rs.getInt("id"))+" "):"")+rs.getString("wine"));
					alternatives.add(rs.getString("numberofwines"));
				}
			}
			
			
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return alternatives;
	}

	public static ArrayList<String> getNewerAlternatives(String input){ 
		ResultSet rs=null;
		String[] inputarray=input.split("[ '-]");
		String inputfulltext="";
		NumberFormat knownwineformat  = new DecimalFormat("000000");	
		Connection con=Dbutil.openNewConnection();
		ArrayList<String> alternatives=new ArrayList<String>();
		try{
			for (int i=0;i<inputarray.length;i++){
				inputfulltext=inputfulltext+" +"+inputarray[i];
			}
			String query="select *, match (wine,appellation) against ('"+Spider.SQLEscape(inputfulltext)+"') as score from knownwines where disabled=false and numberofwines>0 order by score desc, numberofwines desc limit 21;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				if (rs.getInt("numberofwines")>0&&rs.getFloat("score")>5){
					alternatives.add("<a href='"+Webroutines.winelink(rs.getString("wine"),0)+"'>"+rs.getString("wine")+"</a> (wine)");
				}
			}
			query="select *, match (shopname) against ('+("+Spider.SQLEscape(inputfulltext).replaceAll("\\+", "")+")' in boolean mode) as score from shops where disabled=false having score>0 order by score desc limit 21;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				if (rs.getFloat("score")>0){
					alternatives.add("<a href='/store/"+Webroutines.URLEncode(Webroutines.removeAccents(rs.getString("shopname"))).replaceAll("%2F", "/")+"/'>"+rs.getString("shopname")+"</a> (store)");
				}
			}
			
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return alternatives;
	}



	public static int determineKnownWineId(String input){ 
		int id=0;
		String knownwinename=determineKnownWine(input);	
		try{
			id=Integer.valueOf(Dbutil.readValueFromDB("select id from knownwines where wine='"+Spider.SQLEscape(knownwinename)+"';", "id"));
		}catch (Exception e){};
		return id;
	}


	public static void generateKnownwinesPreciseDoubles(){ 
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String literalsearch;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		Dbutil.logger.info("Starting job to analyse knownwines internal doubles");
		try{
			knownwinescon.setAutoCommit(false);
			Dbutil.executeQuery("delete from knownwinesdoublesinternal;");

			rs=Dbutil.selectQueryRowByRow("Select * from knownwines where disabled=false;", con);
			// Should we not use RowByRow because of a timeout problem???
			while (rs.next()){
				whereclause="";
				literalsearch="";
				literalterm=rs.getString("literalsearch").replaceAll("\\?", "").replaceAll("\\\\\\. ", " ").split(" ");
				for (int i=0;i<literalterm.length;i++){
					if (literalterm[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
						} else {
							literalsearch=literalsearch+" AND wine REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literalterm[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

						}
					}
				}

				literaltermexclude=rs.getString("literalsearchexclude").replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
				for (int i=0;i<literaltermexclude.length;i++){
					if (literaltermexclude[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine NOT REGEXP '^[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";
						} else {
							literalsearch=literalsearch+" AND wine NOT REGEXP '[[:<:]]"+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_"," ")), "'", "\\'")+"[[:>:]]'";

						}
					}
				}



				whereclause="";
				if (!rs.getString("fulltextsearch").equals("")){
					if (literalsearch.equals("")){
						whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE)";
					} else {
						whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND "+literalsearch;
					}
				} else	whereclause = whereclause+literalsearch;
				if (whereclause.equals(";")){
					Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
				} else {
					query="Select * from knownwines where id!="+rs.getString("id")+" and "+whereclause+";";
					wines=Dbutil.selectQuery(query, winescon);
					while (wines.next()){
						Dbutil.executeQuery("Insert into knownwinesdoublesinternal (matches,knownwineid) values ('"+wines.getString("id")+"','"+rs.getString("id")+"');",knownwinescon);
					}
					knownwinescon.commit();
					wines.close();
					wines=null;

				}
			}
			rs.close();
			rs=null;
			System.gc();
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwinesdoublesinternal",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(knownwinescon);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);

		}

		Dbutil.logger.info("Finished job to analyse knownwines internal doubles");
	}

	public static void updateWinesforKnownWines(){

		Dbutil.logger.info("Starting job to store matches in wines");
		ResultSet rs=null;
		ResultSet rs2=null;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();

		try{  
			knownwinescon.setAutoCommit(false);
			//First, we will update all single precise matches
			query="select knownwinesmatch.wineid,knownwinesmatch.knownwineid,regions.id as region, regions.lft, regions.rgt from wines, knownwinesmatch join knownwines on(knownwinesmatch.knownwineid=knownwines.id) left join regions on (knownwines.appellation=regions.region) where wines.id=knownwinesmatch.wineid and wines.knownwineid=0 group by wineid having count(*)=1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.executeQuery("update wines set knownwineid="+rs.getInt("knownwineid")+", region="+rs.getInt("region")+", lft="+rs.getInt("lft")+",rgt="+rs.getInt("rgt")+" where id="+rs.getInt("wineid")+";",knownwinescon);
				Dbutil.executeQuery("delete from knownwinesmatch where wineid="+rs.getInt("wineid")+";",knownwinescon);
			}
			knownwinescon.commit();

			// Now, we will see if we can find the most restrictive wine description
			query="select wineid, group_concat(knownwineid) as thelist from knownwinesmatch group by wineid having count(*)>1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int wineid=rs.getInt("wineid");
				ArrayList<Integer> intlist=new ArrayList<Integer>();
				String[] list=rs.getString("thelist").split(",");
				for (int i=0;i<list.length;i++){
					intlist.add(Integer.parseInt(list[i]));
				}
				ArrayList<Integer> mostrestricted=getMostRestrictedKnownWine(intlist);
				if (mostrestricted.size()==1){
					query="select regions.id as region, regions.lft, regions.rgt from knownwines join regions on (knownwines.appellation=regions.region) where knownwines.id="+mostrestricted.get(0)+";";
					rs2=Dbutil.selectQuery(query, con);
					if (rs2.next()){
						Dbutil.executeQuery("update wines set knownwineid="+mostrestricted.get(0)+", region="+rs2.getInt("region")+", lft="+rs2.getInt("lft")+",rgt="+rs2.getInt("rgt")+" where id="+rs.getInt("wineid")+";",knownwinescon);
					}
				}
				String mostrestrictedlist="";
				for (int i=0;i<mostrestricted.size();i++){
					mostrestrictedlist+=","+mostrestricted.get(i);
				}
				if (mostrestrictedlist.length()>1){
					mostrestrictedlist=mostrestrictedlist.substring(1);
					Dbutil.executeQuery("delete from knownwinesmatch where wineid="+rs.getInt("wineid")+" and knownwineid not in ("+mostrestrictedlist+");",knownwinescon);
				} else {
					Dbutil.logger.error("Unexpected result: no restrictive wines for wines.id="+rs.getInt("wineid"));
				}
			}
			knownwinescon.commit();

			// Next, we will update bestmatches where bestmatch is one of more precisematches
			//query="select knownwinesbestmatch.wineid,knownwinesbestmatch.knownwineid,regions.id as region from wines,knownwinesbestmatch, knownwinesmatch join knownwines on(knownwinesmatch.knownwineid=knownwines.id) left join regions on (knownwines.appellation=regions.region) where wines.id=knownwinesmatch.wineid and knownwinesmatch.wineid=knownwinesbestmatch.wineid and knownwinesmatch.knownwineid=knownwinesbestmatch.knownwineid and wines.knownwineid=0 group by wineid;";
			//rs=Dbutil.selectQuery(query, con);
			//while (rs.next()){
			//	Dbutil.executeQuery("update wines set knownwineid="+rs.getInt("knownwineid")+", region="+rs.getInt("region")+" where id="+rs.getInt("wineid")+";",knownwinescon);
			//}
			//knownwinescon.commit();
			// Then we find all bestmatches with no record in precisematch
			//query="select knownwinesbestmatch.wineid,knownwinesbestmatch.knownwineid,knownwinesmatch.knownwineid as thisisnull from wines, knownwinesbestmatch left join knownwinesmatch on (knownwinesmatch.wineid=knownwinesbestmatch.wineid) where wines.id=knownwinesbestmatch.wineid and wines.knownwineid=0 group by wineid having thisisnull is null;";
			//rs=Dbutil.selectQuery(query, con);
			//while (rs.next()){
			//	Dbutil.executeQuery("update wines set knownwineid="+rs.getInt("knownwineid")+" where id="+rs.getInt("wineid")+";",knownwinescon);
			//}
			//knownwinescon.commit();
			// Now we should be complete, unless we have bestmatches that have a different precisematch
			// Those records deserve a review
			//query="select knownwinesbestmatch.wineid,knownwinesbestmatch.knownwineid,knownwinesmatch.knownwineid as precisematch from wines, knownwinesbestmatch join knownwinesmatch on (knownwinesmatch.wineid=knownwinesbestmatch.wineid) where wines.id=knownwinesbestmatch.wineid and wines.knownwineid=0;";
			//rs=Dbutil.selectQuery(query, con);
			//while (rs.next()){
			//	Dbutil.logger.info("Wine "+rs.getString("wineid")+" has bestmatch "+rs.getString("knownwineid")+", and precisematch "+rs.getString("precisematch"));
			//}
			//knownwinescon.commit();


		} catch (Exception exc){
			Dbutil.logger.error("Problem while saving matches in wines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(knownwinescon);
			Dbutil.closeConnection(con);

		}
		Dbutil.logger.info("Finished job to store matches in wines");
	}

	public static void updateWLTVKnownWines(){

		Dbutil.logger.info("Starting job to store matches in winelibrarytv");
		ResultSet rs=null;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();

		try{  
			knownwinescon.setAutoCommit(false);
			query="select knownwinesmatch.wineid,knownwinesmatch.knownwineid from winelibrarytv, knownwinesmatch join knownwines on(knownwinesmatch.knownwineid=knownwines.id) where winelibrarytv.id=knownwinesmatch.wineid and winelibrarytv.knownwineid=0 group by wineid having count(*)=1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.executeQuery("update winelibrarytv set knownwineid="+rs.getInt("knownwineid")+" where id="+rs.getInt("wineid")+";",knownwinescon);
				Dbutil.executeQuery("delete from knownwinesmatch where wineid="+rs.getInt("wineid")+";",knownwinescon);

			}
			knownwinescon.commit();

			//			Now, we will see if we can find the most restrictive wine description
			query="select wineid, group_concat(knownwineid) as thelist from knownwinesmatch group by wineid having count(*)>1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int wineid=rs.getInt("wineid");
				ArrayList<Integer> intlist=new ArrayList<Integer>();
				String[] list=rs.getString("thelist").split(",");
				for (int i=0;i<list.length;i++){
					intlist.add(Integer.parseInt(list[i]));
				}
				ArrayList<Integer> mostrestricted=Knownwines.getMostRestrictedKnownWine(intlist);
				if (mostrestricted.size()==1){
					Dbutil.executeQuery("update winelibrarytv set knownwineid="+mostrestricted.get(0)+" where id="+rs.getInt("wineid")+";",knownwinescon);
				}
			}
			knownwinescon.commit();


		} catch (Exception exc){
			Dbutil.logger.error("Problem while saving matches in wines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(knownwinescon);
			Dbutil.closeConnection(con);

		}
		Dbutil.logger.info("Finished job to store matches in wines");
	}


	/*
	 * Summarizes number of knownwines for each knownwine. 
	 * Used in autosuggest to suggest the most probable match.
	 */	
	public static void updateNumberOfWines(){
		Dbutil.logger.info("Starting job to update number of known wines");
		int id;
		ResultSet numberofwines=null;
		Connection con=Dbutil.openNewConnection();
		Dbutil.executeQuery("Update knownwines set numberofwines=0;");
		numberofwines=Dbutil.selectQuery("Select knownwineid, count(*) as thecount from wines group by knownwineid;", con);
		try{
			while (numberofwines.next()){
				Dbutil.executeQuery("Update knownwines set numberofwines="+numberofwines.getInt("thecount")+" where id="+numberofwines.getInt("knownwineid")+";");
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while updating numberofwines",exc);
		} finally{
			Dbutil.closeRs(numberofwines);
			Dbutil.closeConnection(con);

		}

		Dbutil.logger.info("Finished job to update number of known wines");
	}

	public static void updateSameName(){
		Dbutil.logger.info("Starting job to update samename column in knownwines");
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		Dbutil.executeQuery("Update knownwines set samename=0;");
		rs=Dbutil.selectQuery("Select group_concat(id) as ids, count(*) as thecount from knownwines where disabled=0 group by wine having thecount>1;", con);
		try{
			while (rs.next()){
				Dbutil.executeQuery("Update knownwines set samename=1 where id in("+Spider.SQLEscape(rs.getString("ids"))+");");
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while updating numberofwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		Dbutil.logger.info("Finished job to update samename column in knownwines");
	}




	public static void refine(){ 
		ResultSet rs=null;
		ResultSet knownwines=null;
		String whereclause;
		String[] literalterm;
		String literalsearch;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines where disabled=false and wine like '%chateau%';", con);
		try{
			while (rs.next()){
				whereclause="";
				literalsearch="";
				literalterm=rs.getString("literalsearch").replaceAll("\\?", "").split(" ");
				for (int i=0;i<literalterm.length;i++){
					if (literalterm[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literalterm[i],"_"," "))+"( .*)?$'";
						} else {
							literalsearch=literalsearch+" AND wine REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literalterm[i],"_"," "))+"( .*)?$'";

						}
					}
				}
				whereclause="";
				if (!rs.getString("fulltextsearch").equals("")){
					if (literalsearch.equals("")){
						whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND id!="+rs.getString("id")+";";
					} else {
						whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND "+literalsearch+" AND id!="+rs.getString("id")+";";
					}
				} else	whereclause = whereclause+literalsearch+" AND id!="+rs.getString("id")+";";
				if (whereclause.equals(" AND id!="+rs.getString("id")+";")){
					Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
				} else {
					String extraterms="";
					String shorter=rs.getString("fulltextsearch");
					knownwines=Dbutil.selectQuery("select * from knownwines where "+whereclause, knownwinescon);
					while (knownwines.next()){
						System.out.println(rs.getString("wine")+" is part of "+knownwines.getString("wine"));
						String[] longer=knownwines.getString("fulltextsearch").split(" ");
						for (int i=0;i<longer.length;i++){
							longer[i]=longer[i].replaceAll("\\++", "+");
							if (!shorter.contains(longer[i])&&longer[i].length()>2){
								shorter=shorter+" "+longer[i];
								extraterms=extraterms+" "+longer[i].replaceAll("\\+", "-");
							}
						}
					}
					if (!extraterms.equals("")){
						System.out.println(rs.getString("fulltextsearch")+" gets added "+extraterms);

					}
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(knownwines);
			Dbutil.closeConnection(knownwinescon);
			Dbutil.closeConnection(con);

		}



	}


	public static void DoNotRunfilterorreplacecommonterms(){
		ResultSet rs;
		ResultSet knownwines;
		String scrapedwine;
		String whereclause;
		String literalterm;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines;", con);
		try{
			while (rs.next()){
				literalterm=rs.getString("literalsearch");
				String literaltermorig=literalterm;
				String fulltextsearch=rs.getString("fulltextsearch");
				String fulltextsearchorig=fulltextsearch;
				literalterm=literalterm.replace("(((Ste\\.|Sainte)|Sainte)|Sainte)", "(Ste\\.|Sainte)");
				literalterm=literalterm.replace("Dom\\.", " ");
				fulltextsearch=fulltextsearch.replace("+Tenuta", " ");
				fulltextsearch=fulltextsearch.replace("+Bodegas", " ");
				fulltextsearch=fulltextsearch.replace("+Bodega", " ");
				//fulltextsearch=fulltextsearch.replace("+Pere ", " ");
				//fulltextsearch=fulltextsearch.replace("+P�re ", " ");
				//fulltextsearch=fulltextsearch.replaceAll("\\+Fils( |$)", " ");
				fulltextsearch=fulltextsearch.replace("+IGT", " ");
				fulltextsearch=fulltextsearch.replace("+I.G.T.", " ");
				fulltextsearch=fulltextsearch.replace("+i.g.t.", " ");
				fulltextsearch=fulltextsearch.replaceAll("\\+(C|c)uv(�|e)e", " ");
				//fulltextsearch=fulltextsearch.replaceAll("\\+Cru( |$)", " ");
				fulltextsearch=fulltextsearch.replaceAll("\\+1er( |$)", " ");
				fulltextsearch=fulltextsearch.replace("+DOCG", " ");
				fulltextsearch=fulltextsearch.replace("+DOC", " ");
				fulltextsearch=fulltextsearch.replace(" Toscan*", " +Toscan*");
				fulltextsearch=fulltextsearch.replaceAll(" z( |$)", " +Cruz ");
				fulltextsearch=fulltextsearch.replaceAll(" x( |$)", " +Crux ");
				fulltextsearch=fulltextsearch.replaceAll(" shed( |$)", " +Crushed ");
				fulltextsearch=fulltextsearch.replaceAll(" sh( |$)", " +Crush ");
				fulltextsearch=fulltextsearch.replaceAll(" s( |$)", " ");
				fulltextsearch=fulltextsearch.replaceAll(" G( |$)", " ");
				fulltextsearch=fulltextsearch.replace("+Toscana", "+Toscan*");
				literalterm=literalterm.replaceAll(" de( |$)", " ");
				literalterm=literalterm.replaceAll("^de( |$)", "");
				literalterm=literalterm.replaceAll(" et( |$)", " ");
				literalterm=literalterm.replaceAll("^et( |$)", "");
				literalterm=literalterm.replaceAll(" le( |$)", " ");
				literalterm=literalterm.replaceAll("^le( |$)", "");
				fulltextsearch=fulltextsearch.replaceAll("\\+(D|d)omaine" , " ");
				literalterm=literalterm.replaceAll(" +"," ");
				literalterm=literalterm.replaceAll("^ ","");
				literalterm=literalterm.replaceAll(" $","");
				fulltextsearch=fulltextsearch.replaceAll(" +"," ");
				fulltextsearch=fulltextsearch.replaceAll("^ ","");
				fulltextsearch=fulltextsearch.replaceAll(" $","");
				if (!fulltextsearch.equals(fulltextsearchorig)){
					//System.out.println(fulltextsearchorig+" becomes "+fulltextsearch);
					Dbutil.executeQuery("update knownwines set fulltextsearch='"+Spider.SQLEscape(fulltextsearch)+"' where id="+rs.getInt("id")+";");
				}
				if (!literaltermorig.equals(literalterm)){
					//System.out.println(literaltermorig+" becomes "+literalterm);
					Dbutil.executeQuery("update knownwines set literalsearch='"+Spider.SQLEscape(literalterm)+"' where id="+rs.getInt("id")+";");
					//}
				}

			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while filterorreplacecommonterms: ",exc);
		}


		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);

	}


	public static void suggestDeleteTerms(){
		ResultSet rs=null;
		ResultSet match=null;
		Connection con=Dbutil.openNewConnection();
		Connection matchcon=Dbutil.openNewConnection();
		String wine;
		int wineid;
		int knownwineid;
		String[] fulltextsearch;
		String fulltextsearchstring;
		try{
			rs=Dbutil.selectQuery("Select wines.id,wines.name,wines.knownwineid,knownwinesmatch.knownwineid as thenull from wines left join knownwinesmatch on (wines.id=knownwinesmatch.wineid and wines.knownwineid=knownwinesmatch.knownwineid) having thenull is null;", con);
			while (rs.next()){
				wineid=rs.getInt("id");
				wine=rs.getString("name");
				knownwineid=rs.getInt("knownwineid");
				match=Dbutil.selectQuery("select * from knownwines where id="+knownwineid+";",matchcon);
				fulltextsearch=new String[0];
				fulltextsearchstring="";
				if (match.next()){
					fulltextsearch=match.getString("fulltextsearch").split(" ");
					fulltextsearchstring=match.getString("fulltextsearch");
				}
				String toomuch="";
				for (int i=0;i<fulltextsearch.length;i++){
					match=Dbutil.selectQuery("Select * from wines where match name against ('"+fulltextsearch[i]+"' in boolean mode) and id="+wineid+" limit 1;", matchcon);
					if (!match.next()){
						toomuch=toomuch+fulltextsearch[i]+" ";
					}
				}
				if (!toomuch.equals("")){
					Dbutil.logger.info(wine+" does not contain "+toomuch+". Terms: "+fulltextsearchstring);
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem: ",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(match);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(matchcon);

		}

	}


	public static void generateRefineSQL(){
		ResultSet rs=null;
		ResultSet knownwines=null;
		String scrapedwine;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String literalsearch;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines where disabled=false and appellation !='unknown' order by appellation;", con);
		try{
			while (rs.next()){
				whereclause="";
				literalsearch="";
				literalterm=rs.getString("literalsearch").replaceAll("\\?", "").split(" ");
				for (int i=0;i<literalterm.length;i++){
					if (literalterm[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literalterm[i],"_"," "))+"( .*)?$'";
						} else {
							literalsearch=literalsearch+" AND wine REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literalterm[i],"_"," "))+"( .*)?$'";

						}
					}
				}
				literaltermexclude=rs.getString("literalsearchexclude").replaceAll("\\?", "").split(" ");
				for (int i=0;i<literaltermexclude.length;i++){
					if (literaltermexclude[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine NOT REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literaltermexclude[i],"_"," "))+"( .*)?$'";
						} else {
							literalsearch=literalsearch+" AND wine NOT REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literaltermexclude[i],"_"," "))+"( .*)?$'";

						}
					}
				}
				whereclause="";
				if (!rs.getString("fulltextsearch").equals("")){
					if (literalsearch.equals("")){
						whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND id!="+rs.getString("id")+";";
					} else {
						whereclause = " match (wine) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND "+literalsearch+" AND id!="+rs.getString("id")+";";
					}
				} else	whereclause = whereclause+literalsearch+" AND id!="+rs.getString("id")+";";
				if (whereclause.equals(" AND id!="+rs.getString("id")+";")){
					Dbutil.logger.info("Lege zoekstring voor id "+rs.getString("id"));
				} else {

					whereclause="wine not like '%"+Spider.SQLEscape(rs.getString("wine"))+"%' AND appellation !='"+Spider.SQLEscape(rs.getString("appellation"))+"' AND appellation !='unknown' AND "+whereclause;
					String extraterms="";
					String shorter=rs.getString("fulltextsearch");
					knownwines=Dbutil.selectQuery("select * from knownwines where "+whereclause, knownwinescon);
					while (knownwines.next()){
						System.out.println(rs.getString("wine")+"("+rs.getString("appellation")+") is part of "+knownwines.getString("wine")+"("+knownwines.getString("appellation")+")");
						String[] longer=knownwines.getString("fulltextsearch").split(" ");
						for (int i=0;i<longer.length;i++){
							longer[i]=longer[i].replaceAll("\\++", "+");
							if ((longer[i].startsWith("+")&&!longer[i].toLowerCase().contains("cru")&&!longer[i].toLowerCase().contains("1er")&&!longer[i].toLowerCase().contains("grand")&&!longer[i].toLowerCase().contains("winery")&&!longer[i].toLowerCase().contains("exceptionelle")&&!longer[i].toLowerCase().contains("cuvee"))){
								if (!shorter.toLowerCase().contains(longer[i].substring(1).toLowerCase())&&longer[i].length()>2){
									shorter=shorter+" -"+longer[i].substring(1);
									extraterms=extraterms+" -"+longer[i].substring(1);
								}
							}
						}
					}
					if (extraterms.equals(" -Blanc")){
						System.out.println(rs.getString("fulltextsearch")+" gets added "+extraterms);
						Dbutil.logger.info("Update knownwines set fulltextsearch='"+Spider.SQLEscape(shorter)+"' where id="+rs.getString("id")+"; \n");
					}

				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(knownwines);
			Dbutil.closeConnection(knownwinescon);
			Dbutil.closeConnection(con);
		}		



	}

	public static void filldoubles(){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		Connection updatecon=Dbutil.openNewConnection();

		Dbutil.executeQuery("Update knownwines set doubles=0;" );
		rs=Dbutil.selectQuery("SELECT k.knownwineid as id, count(*) as thecount FROM knownwinesmatch k, knownwinesmatch l where k.wineid=l.wineid and k.knownwineid != l.knownwineid  group by id, l.knownwineid order by thecount desc;", con);
		try{
			while (rs.next()){
				Dbutil.executeQuery("update knownwines set doubles="+rs.getString("thecount")+" where id="+rs.getString("id")+" and doubles<"+rs.getString("thecount")+";",updatecon);
			}
		}catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(updatecon);
			Dbutil.closeConnection(con);
		}		





	}

	public static void clean(){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines where disabled=false;", con);
		try{
			while (rs.next()){
				String search=rs.getString("fulltextsearch");
				String newsearch="";
				String[] terms=search.split(" ");
				for (int i=0;i<terms.length;i++){
					terms[i]=terms[i].replaceAll("\\++", "+");
					if (terms[i].length()>3){
						newsearch=newsearch+" "+terms[i];
					}
				}
				if (newsearch.length()>0) newsearch=newsearch.substring(1);
				if (!newsearch.equals(search)){
					Dbutil.executeQuery("Update knownwines set fulltextsearch='"+Spider.SQLEscape(newsearch)+"' where id="+rs.getString("id"));
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public static void haakjesweg(){
		ResultSet rs=null;
		String query;
		Connection con=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines where wine like '%(%)%' and disabled=false;", con);
		try{
			while (rs.next()){
				String search=rs.getString("wine");
				search=search.replaceAll(".*\\(", "");
				search=search.replaceAll("\\).*", "");
				String[] terms=search.split("( |-|')");
				for (int i=0;i<terms.length;i++){
					terms[i]="+"+terms[i];
					if (terms[i].length()>3){
						query="update knownwines set fulltextsearch=replace(fulltextsearch,'"+terms[i]+" ','' where id ="+rs.getInt("id")+";";
						Dbutil.logger.info(query);
						query="update knownwines set fulltextsearch=replace(fulltextsearch,'"+terms[i]+"','' where id ="+rs.getInt("id")+";";
						Dbutil.logger.info(query);
						//Dbutil.executeQuery(query);
					}
				}

			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}


	public static void fillinnumbers(){
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		rs=Dbutil.selectQuery("Select * from knownwines where disabled=false and wine regexp '#\\d*' and literalsearch='';", con);
		Pattern pattern;
		Matcher matcher;
		try{
			while (rs.next()){
				String search=rs.getString("fulltextsearch");
				pattern=Pattern.compile("#(\\d+)");
				matcher=pattern.matcher(search);
				while (matcher.find()){
					String number=matcher.group(1);
					Dbutil.executeQuery("Update knownwines set literalsearch='"+number+"' where id="+rs.getString("id")+" and literalsearch='';");
				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public static void makedoubleslist(){

		ResultSet knownwines;
		String whereclause;
		String[] literalterm;
		String[] literaltermexclude;
		String literalsearch;
		Connection knownwinescon=Dbutil.openNewConnection();
		//Dbutil.executeQuery("delete from knownwinesmatch;");
		knownwines=Dbutil.selectQuery("Select * from knownwines where disabled=false and appellation !='unknown' order by appellation;", knownwinescon);
		try{
			while (knownwines.next()){
				whereclause="";
				literalsearch="";
				literalterm=knownwines.getString("literalsearch").replaceAll("\\?", "").split(" ");
				for (int i=0;i<literalterm.length;i++){
					if (literalterm[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literalterm[i],"_"," "))+"( .*)?$'";
						} else {
							literalsearch=literalsearch+" AND wine REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literalterm[i],"_"," "))+"( .*)?$'";

						}
					}
				}
				literaltermexclude=knownwines.getString("literalsearchexclude").replaceAll("\\?", "").split(" ");
				for (int i=0;i<literaltermexclude.length;i++){
					if (literaltermexclude[i].length()>0){
						if (literalsearch.equals("")){
							literalsearch=literalsearch+" wine NOT REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literaltermexclude[i],"_"," "))+"( .*)?$'";
						} else {
							literalsearch=literalsearch+" AND wine NOT REGEXP '^(.* )?"+Spider.SQLEscape(Spider.replaceString(literaltermexclude[i],"_"," "))+"( .*)?$'";

						}
					}
				}
				whereclause="";
				if (!knownwines.getString("fulltextsearch").equals("")){
					if (literalsearch.equals("")){
						whereclause = " match (wine) against ('"+Spider.SQLEscape(knownwines.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND id!="+knownwines.getString("id")+";";
					} else {
						whereclause = " match (wine) against ('"+Spider.SQLEscape(knownwines.getString("fulltextsearch"))+"' IN BOOLEAN MODE) AND "+literalsearch+" AND id!="+knownwines.getString("id")+";";
					}
				} else	whereclause = whereclause+literalsearch+" AND id!="+knownwines.getString("id")+";";
				if (whereclause.equals(" AND id!="+knownwines.getString("id")+";")){
					Dbutil.logger.info("Lege zoekstring voor id "+knownwines.getString("id"));
				} else {

					whereclause="wine not like '%"+Spider.SQLEscape(knownwines.getString("wine"))+"%' AND appellation !='"+Spider.SQLEscape(knownwines.getString("appellation"))+"' AND appellation !='unknown' AND "+whereclause;
					String extraterms="";
					String shorter=knownwines.getString("fulltextsearch");
					knownwines=Dbutil.selectQuery("select * from knownwines where "+whereclause, knownwinescon);
					while (knownwines.next()){
						String[] longer=knownwines.getString("fulltextsearch").split(" ");
						for (int i=0;i<longer.length;i++){
							longer[i]=longer[i].replaceAll("\\++", "+");
							if ((longer[i].startsWith("+")&&!longer[i].toLowerCase().contains("cru")&&!longer[i].toLowerCase().contains("1er")&&!longer[i].toLowerCase().contains("grand")&&!longer[i].toLowerCase().contains("winery")&&!longer[i].toLowerCase().contains("exceptionelle")&&!longer[i].toLowerCase().contains("cuvee"))){
								if (!shorter.toLowerCase().contains(longer[i].substring(1).toLowerCase())&&longer[i].length()>2){
									shorter=shorter+" -"+longer[i].substring(1);
									extraterms=extraterms+" -"+longer[i].substring(1);
								}
							}
						}
					}
					if (extraterms.equals(" -Blanc")){
						System.out.println(knownwines.getString("fulltextsearch")+" gets added "+extraterms);
						Dbutil.logger.info("Update knownwines set fulltextsearch='"+Spider.SQLEscape(shorter)+"' where id="+knownwines.getString("id")+"; \n");
					}

				}
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.closeRs(knownwines);
			Dbutil.closeConnection(knownwinescon);

		}
	}

	public static ArrayList<Integer> getKnownWinesFromRegion(String region){
		ArrayList<Integer> wines=new ArrayList<Integer>();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from knownwines where appellation='"+Spider.SQLEscape(region)+"';", con);
			while (rs.next()){
				wines.add(rs.getInt("id"));
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving wines from region "+region,exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return wines;
	}

	public static boolean OLDfilteredterm (String name){
		boolean match=false;
		Matcher matcher;
		Pattern pattern;
		String terms="ch.teau ch\\. st\\.";
		String[] term=terms.split(" ");
		for (int i=0;i<term.length;i++){
			pattern=Pattern.compile("^"+term[i]+"$", Pattern.CASE_INSENSITIVE );
			matcher=pattern.matcher(name);
			if (matcher.find()){
				match=true;
			}
		}
		return match;
	}

	public static String getKnownWineName(int knownwineid){
		return Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";", "wine");
	}

	public static String getUniqueKnownWineName(int knownwineid){
		NumberFormat knownwineformat  = new DecimalFormat("000000");	
		String name="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("select id,samename,wine from knownwines where id="+knownwineid+";", con);
			if (rs.next()){
				if (rs.getBoolean("samename")){
					name=knownwineformat.format(rs.getInt("id"))+" "+rs.getString("wine");
				} else {
					name=rs.getString("wine");
				}
			}
		} catch (Exception E){
			Dbutil.logger.error("Problem while looking up Unique wine name",E);
		}finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return name;

	}
	public static String getKnownWineAndRegionName(int knownwineid){
		return (Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";", "wine")+" <i>"+("("+Dbutil.readValueFromDB("select * from knownwines where id="+knownwineid+";", "appellation")+")").replace("(Unknown)","")+"</i>");
	}
	public static ArrayList<String>getIndividualRegions(){
		ArrayList<String> regions=new ArrayList<String>();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from regions where rgt=lft+1 order by region;", con);
			while (rs.next()){
				regions.add(rs.getString("Region"));
			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving regions.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return regions;
	}

	public static String[] getIcon(int knownwineid){
		String[] file=new String[2];
		file[0]="";
		file[1]="";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("Select * from knownwines where id="+knownwineid+";", con);
			if (rs.next()){
				if (rs.getBoolean("sparkling")){
					file[0]="champagne18.gif";
					file[1]="Sparkling";
				} else if ("Red".equals(rs.getString("color"))){
					if ("Dry".equals(rs.getString("dryness"))){
						file[0]="red18.gif";
						file[1]="Red";
					} else if ("Sweet/Dessert".equals(rs.getString("dryness"))||"Fortified".equals(rs.getString("dryness"))){
						file[0]="port18.gif";
						file[1]="Red fortified/dessert";
					}
				} else if ("White".equals(rs.getString("color"))){
					if ("Dry".equals(rs.getString("dryness"))){
						file[0]="white18.gif";
						file[1]="White";
					} else if ("Sweet/Dessert".equals(rs.getString("dryness"))||"Fortified".equals(rs.getString("dryness"))){
						file[0]="dessert18.gif";
						file[1]="White dessert/fortified";
					}
				} else if ("Ros�".equals(rs.getString("color"))){
					if ("Dry".equals(rs.getString("dryness"))){
						file[0]="rose18.gif";
						file[1]="Ros�";
					} else if ("Sweet/Dessert".equals(rs.getString("dryness"))||"Fortified".equals(rs.getString("dryness"))){
						file[0]="dessert18.gif";
						file[1]="Ros� dessert/fortified";
					}
				}

			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while getting icon.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return file;
	}

	public static String[] getIcon(int knownwineid,int size){
		String[] info=new String[]{"empty"+size+".jpg","Wine type unknown"};
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQueryFromMemory("Select * from materializedadvice where knownwineid="+knownwineid+" limit 1;","materializedadvice", con);
			if (rs.next()){
				int type=rs.getInt("winetypecode");
				switch(type) {
				case 1:info=new String[] {"red"+size+".jpg","Dry red wine"}; break;
				case 2:info=new String[] {"champagne"+size+".jpg","Sparkling dry white wine"};break;
				case 3:info=new String[] {"white"+size+".jpg","Dry white wine"};break;
				case 4:info=new String[] {"rose"+size+".jpg","Dry ros� wine"};break;
				case 5:info=new String[] {"dessert"+size+".jpg","White sweet/dessert wine"};break;
				case 6:info=new String[] {"port"+size+".jpg","Red sweet wine/port"};break;
				case 7:info=new String[] {"white"+size+".jpg","White off-dry wine"};break;
				case 8:info=new String[] {"champagne"+size+".jpg","Sparkling ros� wine"};break;
				case 9:info=new String[] {"port"+size+".jpg","Port/ fortified red wine"};break;
				case 10:info=new String[] {"dessert"+size+".jpg","Sweet/dessert ros� wine"};break;
				case 11:info=new String[] {"dessert"+size+".jpg","White dessert/fortified wine"};break;
				case 12:info=new String[] {"champagne"+size+".jpg","Sparkling red wine"};break;
				}

			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving wine icon.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return info;
	}
	public static String[] getGlassImage(int knownwineid){
		String[] info=new String[]{"glassno.jpg","Wine type unknown"};
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQueryFromMemory("Select * from knownwines natural left join winetypecoding where knownwines.id="+knownwineid+";","materializedadvice", con);
			if (rs.next()){
				int type=rs.getInt("typeid");
				switch(type) {
				case 1:info=new String[] {"glassred.jpg","Dry red wine"}; break;
				case 2:info=new String[] {"glasssparkling.jpg","Sparkling dry white wine"};break;
				case 3:info=new String[] {"glasswhite.jpg","Dry white wine"};break;
				case 4:info=new String[] {"glassrose.jpg","Dry ros� wine"};break;
				case 5:info=new String[] {"glassdessert.jpg","White sweet/dessert wine"};break;
				case 6:info=new String[] {"glassport.jpg","Red sweet/ desert wine"};break;
				case 7:info=new String[] {"glasswhite.jpg","White off-dry wine"};break;
				case 8:info=new String[] {"glasssparkling.jpg","Sparkling ros� wine"};break;
				case 9:info=new String[] {"glassport.jpg","Port/ fortified red wine"};break;
				case 10:info=new String[] {"glassdessert.jpg","Sweet/dessert ros� wine"};break;
				case 11:info=new String[] {"glassdessert.jpg","White port/ fortified wine"};break;
				case 12:info=new String[] {"glasssparkling.jpg","Sparkling red wine"};break;
				}

			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while retrieving wine icon.",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return info;
	}

	public static String[] getGlassImageFromWineTypeCode(int winetypecode){
		String[] info=new String[]{"glassno.jpg","Wine type unknown"};
		switch(winetypecode) {
				case 1:info=new String[] {"glassred.jpg","Dry red wine"}; break;
				case 2:info=new String[] {"glasssparkling.jpg","Sparkling dry white wine"};break;
				case 3:info=new String[] {"glasswhite.jpg","Dry white wine"};break;
				case 4:info=new String[] {"glassrose.jpg","Dry ros� wine"};break;
				case 5:info=new String[] {"glassdessert.jpg","White sweet/dessert wine"};break;
				case 6:info=new String[] {"glassport.jpg","Red sweet/ desert wine"};break;
				case 7:info=new String[] {"glasswhite.jpg","White off-dry wine"};break;
				case 8:info=new String[] {"glasssparkling.jpg","Sparkling ros� wine"};break;
				case 9:info=new String[] {"glassport.jpg","Port/ fortified red wine"};break;
				case 10:info=new String[] {"glassdessert.jpg","Sweet/dessert ros� wine"};break;
				case 11:info=new String[] {"glassdessert.jpg","White port/ fortified wine"};break;
				case 12:info=new String[] {"glasssparkling.jpg","Sparkling red wine"};break;
		}
		return info;
	}

	public static String getImageTag(int knownwineid){
		String[] imagedata=Knownwines.getIcon(knownwineid);
		String image="";
		if (!"".equals(imagedata[0])) image="<img src=\"/images/"+imagedata[0]+"\" alt=\""+imagedata[1]+"\" />";
		return image;
	}

	/* Filters out all wines that have a knownwineid>0 (are known wines), 
	 * that have a priceeuroex more than "factor" times as cheap
	 * as the median of the price for that wine and vintage.
	 * Minimum number of wines is "count".
	 * Parameters:
	 * factor:difference between cheapest wine and median
	 * count:minimum number of wines from a vintage
	 * 
	 */
	public static void filterTooCheapKnownwines(int factor, int count){
		//String query="select wines.id, wines.name,wines.vintage,wine,median,wines.priceeuroex from materializedadvice ma join (select ( substring_index( substring_index( group_concat( f.priceeuroex order by f.priceeuroex ) , ',' , ceiling(count(*)/2) ) , ',' , -1 ) + substring_index( substring_index( group_concat( f.priceeuroex order by f.priceeuroex ) , ',' , -ceiling(count(*)/2) ) , ',' , 1 ) ) / 2  as median, min(priceeuroex) as minimum,knownwineid,vintage,count(*) as thecount from wines f where knownwineid>0 and size=0.75 group by knownwineid,vintage having median>5*minimum and thecount>=5) as selection on (ma.knownwineid=selection.knownwineid and ma.vintage=selection.vintage and selection.median>5*ma.priceeuroex ) join wines on (ma.id=wines.id) join knownwines on ma.knownwineid=knownwines.id;";
		String query="select wines.id, wines.name,wines.vintage,wine,median,minimum,wines.priceeuroex from materializedadvice ma join (select ( substring_index( substring_index( group_concat( floor(f.priceeuroex) order by f.priceeuroex ) , ',' , ceiling(count(*)/2) ) , ',' , -1 ) + substring_index( substring_index( group_concat( floor(f.priceeuroex) order by f.priceeuroex ) , ',' , -ceiling(count(*)/2) ) , ',' , 1 ) ) / 2  as median, min(priceeuroex) as minimum,knownwineid,count(*) as thecount from wines f where knownwineid>0 and vintage>2000 and size=0.75 group by knownwineid having median>5*minimum and thecount>=5) as selection on (ma.knownwineid=selection.knownwineid  and selection.median>5*ma.priceeuroex ) join wines on (ma.id=wines.id) join knownwines on ma.knownwineid=knownwines.id;";
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query, con);
			while(rs.next()){
				Dbutil.executeQuery("update wines set knownwineid=0 where id="+rs.getString("id"));
				Dbutil.executeQuery("delete from materializedadvice where id="+rs.getString("id"));
				
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem while removing impossibly cheap nownwines",e);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
	}

	/**public static void CreatesearchtermsSHOULDNOTBEUSED(){
		ResultSet rs;
		ResultSet knownwines;
		String scrapedwine;
		String fulltext;
		String literal;
		String[] wineterms;
		String knownwine;
		String query;
		Connection con=Dbutil.openNewConnection();
		Connection knownwinescon=Dbutil.openNewConnection();
		rs=Dbutil.selectQuery("Select * from knownwines;", con);
		try{
			while (rs.next()){
				fulltext="";
				literal="";
				scrapedwine=rs.getString("wine");
				scrapedwine=scrapedwine.replaceAll("&","");
				scrapedwine=scrapedwine.replaceAll("\\(","");
				scrapedwine=scrapedwine.replaceAll("\\)","");
				scrapedwine=scrapedwine.replaceAll("\\*", "");
				scrapedwine=scrapedwine.replaceAll("\\?", "");
				scrapedwine=scrapedwine.replaceAll("-", " ");
				scrapedwine=scrapedwine.replaceAll("'", " ");
				wineterms=scrapedwine.split(" ");
				for (int i=0;i<wineterms.length;i++){
					if (!filteredterm(wineterms[i])){
						if (wineterms.length>0){
							if (wineterms[i].length()<3||wineterms[i].contains(".")){
								if (literal.equals("")){
									literal=literal+Spider.SQLEscape(Spider.replaceString(wineterms[i],".","\\."));
								} else {		
									literal=literal+" "+Spider.SQLEscape(wineterms[i]);
								}
								wineterms[i]=wineterms[i].replaceAll("\\.", " ");
								wineterms[i]=wineterms[i].replaceAll("'", " ");
								wineterms[i]=wineterms[i].replaceAll("-", " ");
								wineterms[i]=wineterms[i].replaceAll(" +", " +");
								if (wineterms[i].startsWith(" +")){
									wineterms[i]=wineterms[i].substring(1);
								} else {
									wineterms[i]="+"+wineterms[i];
								}
								//if (fulltext.equals("")){
								//	fulltext=fulltext+"+"+Spider.SQLEscape(wineterms[i]);
								//} else {		
								//	fulltext=fulltext+" +"+Spider.SQLEscape(wineterms[i]);
								//}
							} else {
								if (fulltext.equals("")){
									fulltext=fulltext+"+"+Spider.SQLEscape(wineterms[i]);
								} else {		
									fulltext=fulltext+" +"+Spider.SQLEscape(wineterms[i]);
								}
							}
						}
					}
				}
				//query="update knownwines set fulltextsearch='"+fulltext+"', literalsearch='"+literal+"' where id="+rs.getString("id");
				//Dbutil.executeQuery(query, knownwinescon);

			}

		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		}


		Dbutil.closeConnection(knownwinescon);
		Dbutil.closeConnection(con);
	}

	 **/
	public static void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		getColorFromType();
	}

}
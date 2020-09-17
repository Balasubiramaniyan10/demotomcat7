package com.freewinesearcher.obsolete;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.text.Normalizer;


import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.parser.xpathparser.ContentHandler;
import com.searchasaservice.parser.xpathparser.TextHandler;

public class ObsoleteFastWineContentHandler extends TextHandler implements ContentHandler {

	private static final long serialVersionUID = 1L;

	public Set<String> OldrecognizeContent(Set<String> wineset) {
		Dbutil.logger.info("init");
		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String query;
		String booleanmode="";
		booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String full;
		
		Set<String> allwords=new HashSet<String>();
		for (String value:wineset) {
			for(String term:toFulltext(value,null).split(" ?\\+")){
				allwords.add(term);
			}
		}
		String[] toocommon={"more"};
		allwords.remove("");
		for (String term:toocommon){
			allwords.remove(term);
		}
		
		Set<String> result=new HashSet<String>();
		Set<String> matches=new HashSet<String>();
		StringBuffer sb=new StringBuffer();
		for (String term:allwords) sb.append(" +"+term);
		String allwines=sb.toString().toLowerCase();
		

		try{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',`id` int(10) unsigned NOT NULL AUTO_INCREMENT,	  FULLTEXT `namefulltext` (`name`),KEY `PK_ID` (`id`)) ;", con);
			for (String wine:wineset){

				Dbutil.executeQuery("Insert into tempwine (name) values ('"+Spider.SQLEscape(wine.substring(0,Math.min(wine.length(),290)))+"');", con);
			}
			//Dbutil.executeQuery("Insert into tempwine (name,id) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"',2);", con);
			//Dbutil.executeQuery("Insert into tempwine (name,id) values ('',3);", con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `wine` varchar(255) NOT NULL DEFAULT '', `appellation` varchar(255) NOT NULL DEFAULT '', `producer` varchar(255) NOT NULL DEFAULT '',  `fulltextsearch` varchar(1000) NOT NULL,  `disabled` tinyint(1) NOT NULL DEFAULT '0', `literalsearch` varchar(255) NOT NULL DEFAULT '', `literalsearchexclude` varchar(255) NOT NULL DEFAULT '', `doubles` int(10) unsigned NOT NULL DEFAULT '0', `numberofwines` int(10) unsigned NOT NULL DEFAULT '0', `type` VARCHAR(45) DEFAULT null,   `color` varchar(10) NOT NULL DEFAULT '',  `dryness` varchar(20) NOT NULL DEFAULT '',  `sparkling` tinyint(1) NOT NULL DEFAULT '0',  `samename` tinyint(1) NOT NULL DEFAULT '0', `score` DOUBLE NOT NULL DEFAULT 0.0, PRIMARY KEY (`id`), KEY `Wine` (`wine`),  KEY `Appellation` (`appellation`)) ENGINE=MEMORY  DEFAULT CHARSET=utf8;",con);
			Dbutil.logger.info("firstselection vullen");
			
			Dbutil.executeQuery("Insert into firstselection select id,wine, appellation,producer,fulltextsearch,disabled, literalsearch, literalsearchexclude,doubles,numberofwines, type,color,dryness,sparkling,samename,  match (wine,appellation) against ('"+Spider.SQLEscape(allwines)+"') as score from knownwines where disabled=false having score>0 order by score desc limit 1000;",con);
			rs=Dbutil.selectQuery("select * from firstselection", con);
			Dbutil.logger.info("firstselection adjust fulltext");
			while (rs.next()){
				full=filterTerms(rs.getString("fulltextsearch"),rs.getString("appellation")+" "+rs.getString("producer"));
				if ("".equals(full)||!full.contains("+")) full=filterTerms(rs.getString("fulltextsearch"),rs.getString("appellation"));
				Dbutil.executeQuery("update firstselection set fulltextsearch='"+Spider.SQLEscape(full)+"' where id="+rs.getInt("id")+";", con);
			}
			rs=Dbutil.selectQuery("Select * from firstselection order by score desc;", con);
//			query="Select * from tempwine;";						
//			wines=Dbutil.selectQuery(query, con);
//			while (wines.next()){
//				Dbutil.logger.info(wines.getString("name"));
//			}
			Dbutil.logger.info("Loop over firstselection");
			
			while (rs.next()){
				boolean dosearch=false;
				whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+")";
				query="Select * from tempwine where"+whereclause+";";						
				wines=Dbutil.selectQuery(query, con);
				if (wines.next()){
					dosearch=true;
				}
				if (dosearch){
					whereclause=Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false);
					query="Select * from tempwine where"+whereclause+" order by id;";						
					wines=Dbutil.selectQuery(query, con);
					while (wines.next()){
						matches.add(wines.getString("name"));
						Dbutil.executeQuery("delete from tempwine where id="+wines.getInt("id"), con);
					}

					Dbutil.closeRs(wines);

				}
			}


		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(wines);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}
		Dbutil.logger.info("finished");
		
		return matches;
	}

	public static String toFulltext(String input, String[] toocommon){
		input=" "+input.toLowerCase()+" ";
		if (toocommon!=null){
			for (String term:toocommon){
				input=input.replace(" "+term.toLowerCase()+" ", " ");
			}
		}
		return input.replaceAll("[\\d'\\#%&*_()\":;<>,./?\\\\]"," ").replaceAll("\\s.(?=\\s)"," ").replaceAll("\\s+"," +").replaceAll("\\+-","-").trim();
	}

	public static String filterTerms(String input, String filter){
		String[] terms=filter.split("\\(\\) ");
		for (String term:terms){
			if (!term.equals("")) input=input.replaceAll("\\+"+term+"( |$)", " ").replaceAll("\\s+", " ").trim();
		}
		return input;
	}
	
	public static final void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Set<String> set = new HashSet<String>();
		set.add("Chateau Margaux");
		set.add("leoville las cases");
		set.add("d'Yquem");
		set.add("Ch�teau l'Ermitage");
		set.add("Margaux");
		
		ObsoleteFastWineContentHandler wch =new ObsoleteFastWineContentHandler();
		//Dbutil.logger.info(wch.recognizeContent(set));
		
		
	}
	
	public String removeAccents(String s){
		try {
			return Normalizer.normalize(s, Normalizer.Form.NFD);
			//return Normalizer.decompose( s, false,0 ).replaceAll( "\\p{InCombiningDiacriticalMarks}+", "" );
		} catch ( NoSuchMethodError ex ) {

		} 
		s = s.toUpperCase();
		s = s.replaceAll( "[�����]", "A" );
		s = s.replaceAll( "[����]", "E" );
		s = s.replaceAll( "[����]", "I" );
		s = s.replaceAll( "[�����]", "O" );
		s = s.replaceAll( "[����]", "U" );
		s = s.replaceAll( "�", "C" );
		s = s.replaceAll( "�", "N" );
		return s;
	}


}
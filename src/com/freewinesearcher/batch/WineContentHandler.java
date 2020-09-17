package com.freewinesearcher.batch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.parser.xpathparser.ContentHandler;
import com.searchasaservice.parser.xpathparser.TextHandler;

public class WineContentHandler extends TextHandler implements ContentHandler {

	private static final long serialVersionUID = 1L;

	public HashMap<Node,String> recognizeContent(HashMap<Node,String> wineset) {
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
		for (String value:wineset.values()) {
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
		HashMap<Node,String> matches=new HashMap<Node,String>();
		HashMap<Integer,Node> nodemap=new HashMap<Integer, Node>();
		int i=0;
		for (Node n:wineset.keySet()){
			nodemap.put(i,n);
			i++;
		}
		StringBuffer sb=new StringBuffer();
		for (String term:allwords) sb.append(" +"+term);
		String allwines=sb.toString().toLowerCase();
		

		try{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',`id` int(10) unsigned NOT NULL AUTO_INCREMENT,	  FULLTEXT `namefulltext` (`name`),KEY `PK_ID` (`id`));", con);
			for (int j:nodemap.keySet()){

				Dbutil.executeQuery("Insert into tempwine (id,name) values ("+j+",'"+Spider.SQLEscape(wineset.get(nodemap.get(j)).substring(0,Math.min(wineset.get(nodemap.get(j)).length(),290)))+"');", con);
			}
			//Dbutil.executeQuery("Insert into tempwine (name,id) values ('"+Spider.SQLEscape(input.substring(0,Math.min(input.length(),290)))+"',2);", con);
			//Dbutil.executeQuery("Insert into tempwine (name,id) values ('',3);", con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `wine` varchar(255) NOT NULL DEFAULT '', `appellation` varchar(255) NOT NULL DEFAULT '', `producer` varchar(255) NOT NULL DEFAULT '',  `fulltextsearch` text NOT NULL,  `disabled` tinyint(1) NOT NULL DEFAULT '0', `literalsearch` varchar(255) NOT NULL DEFAULT '', `literalsearchexclude` varchar(255) NOT NULL DEFAULT '', `doubles` int(10) unsigned NOT NULL DEFAULT '0', `numberofwines` int(10) unsigned NOT NULL DEFAULT '0', `type` VARCHAR(45) DEFAULT null,   `color` varchar(10) NOT NULL DEFAULT '',  `dryness` varchar(20) NOT NULL DEFAULT '',  `sparkling` tinyint(1) NOT NULL DEFAULT '0',  `samename` tinyint(1) NOT NULL DEFAULT '0', `score` DOUBLE NOT NULL DEFAULT 0.0, PRIMARY KEY (`id`), KEY `Wine` (`wine`),  KEY `Appellation` (`appellation`),	  FULLTEXT KEY `winefulltext` (`wine`),  FULLTEXT KEY `wineappelfulltext` (`wine`,`appellation`)) AUTO_INCREMENT=98529 DEFAULT CHARSET=utf8;",con);
			Dbutil.logger.info("firstselection");
			
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
						matches.put(nodemap.get(wines.getInt("id")),wineset.get(nodemap.get(wines.getInt("id"))));
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
		
		
		
	}

}

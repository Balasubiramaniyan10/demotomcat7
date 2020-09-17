package com.freewinesearcher.obsolete;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.WineContentHandler;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.parser.xpathparser.ContentHandler;
import com.searchasaservice.parser.xpathparser.TextHandler;

public class ObsoleteFWSContentHandler extends TextHandler implements ContentHandler {

	private static final long serialVersionUID = 1L;
	String tablename="knownwines";
	String fieldname="producer";
	String[] toocommon={"more"};

	public ObsoleteFWSContentHandler(String tablename,String fieldname, String[] toocommon) {
		super();
		this.fieldname = fieldname;
		this.tablename = tablename;
		this.toocommon = toocommon;
	}


	public Set<String> OldrecognizeContent(Set<String> recordset) {

		ResultSet rs=null;
		ResultSet wines=null;
		String whereclause;
		String query;
		String booleanmode="";
		booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		String full;
		HashSet<String> filteredset=new HashSet<String>();
		for (String entry:recordset){
			filteredset.add(entry.replaceAll("[\\d.,:;]", ""));
		}

		Set<String> allwords=new HashSet<String>();
		for (String value:recordset) {
			for(String term:WineContentHandler.toFulltext(value,null).split(" ?\\+")){
				allwords.add(term);
			}
		}
		allwords.remove("");
		if(toocommon!=null){
			for (String term:toocommon){
				allwords.remove(term);
			}
		}
		Set<String> result=new HashSet<String>();
		Set<String> matches=new HashSet<String>();
		StringBuffer sb=new StringBuffer();
		for (String term:allwords) sb.append(" +"+term);
		String allwines=sb.toString().toLowerCase();


		try{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`"+fieldname+"` VARCHAR(300) NOT NULL DEFAULT ' ',`id` int(10) unsigned NOT NULL AUTO_INCREMENT,	  FULLTEXT `"+fieldname+"fulltext` (`"+fieldname+"`),KEY `PK_ID` (`id`));", con);
			for (String record:recordset){
				Dbutil.executeQuery("Insert into tempwine ("+fieldname+") values ('"+Spider.SQLEscape(record.substring(0,Math.min(record.length(),290)))+"');", con);
			}
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `record` varchar(255) NOT NULL DEFAULT '', `fulltextsearch` varchar(255) NOT NULL DEFAULT '', `score` DOUBLE NOT NULL DEFAULT 0.0, PRIMARY KEY (`id`));",con);
			Dbutil.executeQuery("Insert into firstselection select id,"+fieldname+",'',  match ("+fieldname+") against ('"+Spider.SQLEscape(allwines)+"') as score from "+tablename+" group by "+fieldname+" having score>0  order by score desc limit 1000;",con);
			rs=Dbutil.selectQuery("select * from firstselection", con);
			while (rs.next()){
				full=WineContentHandler.toFulltext(rs.getString("record"),null);
				Dbutil.executeQuery("update firstselection set fulltextsearch='"+Spider.SQLEscape(full)+"' where id="+rs.getInt("id")+";", con);
			}
			//query="Select * from firstselection;";						
			//wines=Dbutil.selectQuery(query, con);
			//while (wines.next()){
			//	Dbutil.logger.info(wines.getString("record")+": "+wines.getString("fulltextsearch"));
			//}
			Dbutil.logger.info("Loop over firstselection");
			
			rs=Dbutil.selectQuery("Select * from firstselection order by score desc;", con);
			while (rs.next()){
				whereclause = " match ("+fieldname+") against ('"+Spider.SQLEscape(rs.getString("fulltextsearch"))+"' "+booleanmode+")";
				query="Select * from tempwine where"+whereclause+";";						
				wines=Dbutil.selectQuery(query, con);
				while (wines.next()){
					matches.add(wines.getString(fieldname));
					Dbutil.executeQuery("delete from tempwine where id="+wines.getInt("id"), con);
				}

				Dbutil.closeRs(wines);


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


		return matches;
	}

	public static final void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Set<String> set = new HashSet<String>();
		set.add("Chateau Margaux");
		set.add("leoville las cases");
		set.add("d'Yquem");
		set.add("Ch�teau l'Ermitage");
		set.add("Margaux");
		
		//map.put("100", "Margaux");
		ObsoleteFWSContentHandler wch =new ObsoleteFWSContentHandler("Regions","region",null);
		//Dbutil.logger.info(wch.recognizeContent(set));


	}

}
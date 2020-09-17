package com.searchasaservice.ai;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.searchasaservice.parser.xpathparser.ContentHandler;
import com.searchasaservice.parser.xpathparser.TextHandler;

public class AiContentHandler  extends TextHandler implements ContentHandler{

	private static final long serialVersionUID = 1L;
	private int typeid;
	private int tenant;
	private boolean bestmatches=false; 	// If false, all nodes are returned which match a property in boolean mode. If true,
										// only the best scoring results (30% max) are returned.
	
	
	
	
	public AiContentHandler(int tenant, int typeid) {
		super();
		this.tenant = tenant;
		this.typeid = typeid;
	}

	public Set<Node> recognizeContent(Set<Node> inputHashSet) {
		boolean log=false;
		if (log) Dbutil.logger.info("init");
		ResultSet rs=null;
		ResultSet items=null;
		String whereclause;
		String query;
		String booleanmode="";
		booleanmode=" in boolean mode";
		Connection con=Dbutil.openNewConnection();
		Connection winescon=Dbutil.openNewConnection();
		
		Set<String> allwords=new HashSet<String>();
		for (Node n:inputHashSet) {
			String value=n.getTextContent();
			for(String term:toFulltext(value,null).split(" ?\\+")){
				allwords.add(term);
			}
		}
		String[] toocommon={"more","in"};
		allwords.remove("");
		for (String term:toocommon){
			allwords.remove(term);
		}
		
		HashSet<Node> matches=new HashSet<Node>();
		HashMap<Integer,Node> nodemap=new HashMap<Integer, Node>();
		int i=0;
		for (Node n:inputHashSet){
			nodemap.put(i,n);
			i++;
		}
		StringBuffer sb=new StringBuffer();
		for (String term:allwords) sb.append(" +"+term);
		String wordsft=sb.toString().toLowerCase();
		

		try{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` (`name` VARCHAR(300) NOT NULL DEFAULT ' ',`id` int(10) unsigned NOT NULL AUTO_INCREMENT,	  FULLTEXT `namefulltext` (`name`),KEY `PK_ID` (`id`));", con);
			for (int j:nodemap.keySet()){

				Dbutil.executeQuery("Insert into tempwine (id,name) values ("+j+",'"+Spider.SQLEscape(nodemap.get(j).getTextContent().substring(0,Math.min(nodemap.get(j).getTextContent().length(),290)))+"');", con);
			}
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`firstselection` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT, `propertyid` int(10) unsigned,  `fts` text NOT NULL, `regex` varchar(255) NOT NULL DEFAULT '', `regexexcl` varchar(255) NOT NULL DEFAULT '', PRIMARY KEY (`id`)) AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;",con);
			if (log) Dbutil.logger.info("firstselection");
			
			Dbutil.executeQuery("Insert into firstselection (propertyid,fts,regex,regexexcl) select propertyid,fts,regex,regexexcl from (select tenant,typeid,propertyid from aiproperties where tenant="+tenant+" and typeid="+typeid+" and match(propertydescription) against ('"+Spider.SQLEscape(wordsft)+"') limit 1000) candidates natural join airecognizer;",con);

			
			rs=Dbutil.selectQuery("Select * from firstselection order by id;", con);
			if (log) Dbutil.logger.info("Loop over firstselection");
			
			while (rs.next()){
				boolean dosearch=false;
				whereclause = " match (name) against ('"+Spider.SQLEscape(rs.getString("fts"))+"' "+booleanmode+")";
				query="Select * from tempwine where"+whereclause+" limit 1;";						
				items=Dbutil.selectQuery(query, con);
				if (items.next()){
					dosearch=true;
				}
				if (dosearch){
					whereclause=Recognizer.whereClause("name",rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"));
					query="Select * from tempwine where"+whereclause+" order by id;";						
					items=Dbutil.selectQuery(query, con);
					while (items.next()){
						matches.add(nodemap.get(items.getInt("id")));
						Dbutil.executeQuery("delete from tempwine where id="+items.getInt("id"), con);
					}

					Dbutil.closeRs(items);

				}
			}


		} catch (Exception exc){
			if (log) Dbutil.logger.error("Problem while looking up knownwines",exc);
		} finally{
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
			Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`firstselection`;",con);
			Dbutil.closeRs(rs);
			Dbutil.closeRs(items);
			Dbutil.closeConnection(winescon);
			Dbutil.closeConnection(con);
		}
		if (log) Dbutil.logger.info("finished");
		
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

		
}

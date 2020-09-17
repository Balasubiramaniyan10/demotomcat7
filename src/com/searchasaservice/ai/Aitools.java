package com.searchasaservice.ai;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;
import com.google.gdata.data.extensions.Where;

public class Aitools {
	int tenant;
	int fulllimit=10; // max number of elements for which to create a full permutation map
	int depth=4;
	int pairstoselect=10;
	public String itemtable="knownwines";
	public String regionhierarchytable="kbregionhierarchy";
	HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>> permutations=new HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>>(); 
	HashMap<Integer,HashMap<Integer,Integer>> limitedpermutations=new HashMap<Integer,HashMap<Integer,Integer>>(); 

	public Aitools(int tenant) {
		super();
		this.tenant = tenant;
	}

	public void fillProperties(){
		Dbutil.logger.info("Starting fillProperties");
		String query;
		String desc;
		String ft;
		String lit;
		int row;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			query="DROP TABLE IF EXISTS `wijn`.`airecognizer`;";
			Dbutil.executeQuery(query);
			query="CREATE TABLE  `wijn`.`airecognizer` (`tenant` int(10) unsigned NOT NULL,`propertyid` int(10) unsigned NOT NULL,`typeid` int(10) unsigned NOT NULL,`fts` varchar(255) NOT NULL DEFAULT '',`regex` varchar(255) NOT NULL DEFAULT '',`regexexcl` varchar(255) NOT NULL DEFAULT '',`recognizerid` int(10) unsigned NOT NULL AUTO_INCREMENT,PRIMARY KEY (`recognizerid`)) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;";
			Dbutil.executeQuery(query);
			query="DROP TABLE IF EXISTS `wijn`.`aiproperties`;";
			Dbutil.executeQuery(query);
			query="CREATE TABLE  `wijn`.`aiproperties` (`tenant` int(10) unsigned NOT NULL,`propertyid` int(10) unsigned NOT NULL AUTO_INCREMENT,`typeid` int(10) unsigned NOT NULL,`propertydescription` varchar(255) NOT NULL,PRIMARY KEY (`tenant`,`typeid`,`propertyid`) USING BTREE, UNIQUE KEY `index_2` (`propertydescription`,`typeid`),UNIQUE KEY `propid` (`propertyid`)) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;";
			Dbutil.executeQuery(query, con);
			query="insert into aiproperties (propertydescription,typeid,tenant) select distinct(dryness),5,1 from "+itemtable+" where dryness is not null and dryness !=''  and dryness not like 'na%';";
			Dbutil.executeQuery(query, con);
			query="insert into aiproperties (propertydescription,typeid,tenant) select distinct(color),6,1 from "+itemtable+" where color is not null and color !='' and color not like 'na%';";
			Dbutil.executeQuery(query, con);
			query="insert into aiproperties (propertydescription,typeid,tenant) values('Still',7,1),('Sparkling',7,1);";
			Dbutil.executeQuery(query, con);
			Dbutil.executeQuery("ALTER TABLE aiproperties AUTO_INCREMENT = 100",con);

			query="insert into aiproperties (propertyid,propertydescription,typeid,tenant) select id,region,2,1 from "+regionhierarchytable+" where region!='' order by id;";
			Dbutil.executeQuery(query, con);
			query="insert into aiproperties (propertydescription,typeid,tenant) select distinct(producer),1,1 from "+itemtable+" where producer!='' and disabled=0 order by producer;";
			Dbutil.executeQuery(query, con);
			//query="insert into aiproperties (propertydescription,typeid,tenant) select distinct(vineyard),3,1 from "+itemtable+" where vineyard!='' and disabled=0 order by vineyard;";
			//Dbutil.executeQuery(query, con);
			query="insert into aiproperties (propertydescription,typeid,tenant) select distinct(grapename),8,1 from grapes;";
			Dbutil.executeQuery(query, con);

			query="select distinct(cuvee) as cuvee from "+itemtable+" where disabled=0;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				desc=rs.getString("cuvee");
				desc=filterPunctuation(desc);
				String[] terms=desc.split(" +");
				for (String term:terms){
					term=filterTermCuvee(term);
					query="insert ignore into aiproperties(tenant,propertydescription,typeid) values ("+tenant+",'"+term+"',4);";
					Dbutil.executeQuery(query, con2);
				}
			}
			query="select distinct(vineyard) as vineyard from "+itemtable+" where disabled=0;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				desc=rs.getString("vineyard");
				desc=filterPunctuation(desc);
				String[] terms=desc.split(" +");
				for (String term:terms){
					term=filterTermCuvee(term);
					query="insert ignore into aiproperties(tenant,propertydescription,typeid) values ("+tenant+",'"+term+"',4);";
					Dbutil.executeQuery(query, con2);
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.executeQuery("ALTER TABLE `wijn`.`airecognizer` ADD INDEX `index_1`(`tenant`, `typeid`, `propertyid`);");
			Dbutil.executeQuery("ALTER TABLE `wijn`.`aiproperties` ADD FULLTEXT INDEX `ft`(`propertydescription`);");
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}

	}

	public void filterColorGrapetermsinCuvee(){
		Dbutil.logger.info("Starting filter on cuvee terms that also match color and/or grapes");
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			query="select * from airecognizermanual where propertyid<100;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="delete from aiproperties where tenant="+tenant+" and typeid=4 and "+Recognizer.whereClause("propertydescription", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"))+" and propertydescription not like 'port' and propertydescription not like 'brut' and propertydescription not like 'cava';";
				Dbutil.executeQuery(query, con2);
			}
			query="select * from grapes where grapename!='Bacchus';";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="delete from aiproperties where tenant="+tenant+" and typeid=4 and propertydescription like '"+Spider.SQLEscape(rs.getString("grapename"))+"';";
				Dbutil.executeQuery(query, con2);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}

	}


	
	public static String filterPunctuation(String desc){
		String original=desc;
		Pattern pattern=Pattern.compile("(?:^| )((?:\\w[-.&])+\\w)(?=( |$|\\.))");
		Matcher matcher=pattern.matcher(desc);
		//desc=desc.replace("_", "");
		while (matcher.find()){
			desc=desc.replace(matcher.group(1), matcher.group(1).replace("-", "_").replace("&", "_").replace(".", "_"));
		}
		desc=desc.replace("\\", " ");
		desc=desc.replace("�", " ");
		desc=desc.replace("�", " ");
		desc=desc.replace("�", " ");
		desc=desc.replace("�", " ");
		desc=desc.replace("�", " ");
		desc=desc.replace(Character.toString((char) 148), "");
		desc=desc.replace(Character.toString((char) 191), "");
		desc=desc.replace(Character.toString((char) 150), "");
		desc=desc.replace(Character.toString((char) 147), "");
		desc=desc.replace(Character.toString((char) 145), "");
		desc=desc.replace(Character.toString((char) 142), "");
		desc=desc.replace(Character.toString((char) 132), "");
		desc=desc.replace(Character.toString((char) 138), "");
		desc=desc.replace(Character.toString((char) 928), "");
		desc=desc.replace(";", " ");
		desc=desc.replace("+", " ");
		desc=desc.replace("[", " ");
		desc=desc.replace("]", " ");
		desc=desc.replace("\"", " ");
		desc=desc.replace(",", " ");
		desc=desc.replace("'", " ");
		desc=desc.replace("�", " ");
		desc=desc.replace("&", " ");
		desc=desc.replace(".", " ");
		desc=desc.replace("(", " ");
		desc=desc.replace(")", " ");
		desc=desc.replace(":", " ");
		desc=desc.replace("-", " ");
		desc=desc.replace("/", " ");
		desc=desc.replace("%", " ");
		desc=desc.replace("\"", " ");
		desc=desc.replace("#", " ");
		desc=desc.replace("?", "");
		if (!original.equals(desc)) desc=filterPunctuation(desc);

		return desc;
	}


	public void generateRecognizerFullText(){
		Dbutil.logger.info("Starting generateRecognizerFullText");
		String query;
		String desc;
		String ft;
		String lit;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		ResultSet rs2=null;
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("truncate table airecognizer");
			query="select * from aiproperties where tenant="+tenant+" and typeid!=4 and typeid!=3 and typeid!=2;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				desc=rs.getString("propertydescription");
				lit="";
				ft="";
				desc=filterCompleteTerm(desc);
				desc=filterPunctuation(desc);
				String[] terms=desc.split(" ");
				for (String term:terms){
					if (rs.getInt("typeid")==1){
						term=filterTerm(term);
					}
					if (term.length()>0){
						if (term.length()>=2&&!term.contains("*")&&!term.contains("_")){
							ft+=" +"+term;
						} else {
							lit+=" "+term;
						}
					}
				}
				ft=ft.trim();
				lit=lit.trim();
				query="insert into airecognizer(tenant,propertyid,typeid,fts,regex,regexexcl,recognizerid) values ("+tenant+","+rs.getInt("propertyid")+","+rs.getInt("typeid")+",'"+ft+"','"+lit+"','',"+rs.getInt("propertyid")+");";
				Dbutil.executeQuery(query, con);
			}

			query="select * from aiproperties where tenant="+tenant+" and typeid=4;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				desc=rs.getString("propertydescription");
				lit="";
				ft="";
				desc=filterPunctuation(desc);
				String[] terms=desc.split(" +");
				for (String term:terms){
					term=filterTermCuvee(term);
					ft="";
					lit="";
					if (term.replace("\\","").length()>0){
						if (term.length()>=2&&!term.contains("*")){
							ft="+"+term;
						} else {
							lit=term;
						}
						if (!(ft+lit).equals("")){
							query="insert into airecognizer(tenant,propertyid,typeid,fts,regex,regexexcl,recognizerid) values ("+tenant+","+rs.getInt("propertyid")+",4,'"+ft+"','"+lit+"','',"+rs.getInt("propertyid")+");";
							Dbutil.executeQuery(query, con2);
						}

					}
				}
			}


			query="select * from aiproperties join "+regionhierarchytable+" on (propertydescription=region) where tenant="+tenant+" and typeid=2;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				desc=rs.getString("shortregion");
				lit="";
				ft="";
				desc=filterRegion(desc);
				desc=filterCompleteTerm(desc);
				desc=filterPunctuation(desc);
				String[] terms=desc.split(" ");
				for (String term:terms){
					if (rs.getInt("typeid")==2){
						term=filterRegionTerm(term);
					}
					if (term.length()>0){
						if (term.length()>=2&&!term.contains("*")&&!term.contains("_")){
							ft+=" +"+term;
						} else {
							lit+=" "+term;
						}
					}
				}
				ft=ft.trim();
				lit=lit.trim();
				query="insert into airecognizer(tenant,propertyid,typeid,fts,regex,regexexcl,recognizerid) values ("+tenant+","+rs.getInt("propertyid")+","+rs.getInt("typeid")+",'"+ft+"','"+lit+"','',"+rs.getInt("propertyid")+");";
				Dbutil.executeQuery(query, con);
			}
			Dbutil.executeQuery("update airecognizer set regex=replace(regex,'*','\\\\\\\\*') where regex like '%*%';");
			//filterProducerFirstName();
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
		}

	}
	
	public void filterProducerFirstName(){
		Dbutil.logger.info("Starting filterProducerFirstName");
		String query;
		String desc;
		String ft;
		String originalft;
		String lit;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		int n;
		String test;
		try{
			query="select * from airecognizer where tenant="+tenant+" and typeid=1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				originalft=rs.getString("fts");
				ft=originalft;
				String[] terms=ft.split(" ");
				for (String term:terms){
					if (term.startsWith("+")&&!term.contains("(")&&!term.contains(")")){
						test=ft.replaceAll("\\"+term+"( |$)","");
						if (!test.equals("\\( |)$")){
							query="select count(*) as num from aiproperties where typeid=1 and match(propertydescription) against ('"+ft.replaceAll("\\"+term+"( |$)","")+"' in boolean mode);";
							rs2=Dbutil.selectQuery(rs2,query, con2);
							if (rs2.next()){
								n=rs2.getInt("num");
								if (n==1){
									ft=test;
								}
							}
						}
					}
				}
				if (!originalft.equals(ft)){
					Dbutil.executeQuery("update airecognizer set fts='"+Spider.SQLEscape(ft)+"' where tenant="+tenant+" and typeid=1 and propertyid="+rs.getInt("propertyid"));
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
		}
		
	}

	public void manualRecognizer(){
		Dbutil.logger.info("Starting manualRecognizer");
		String query;
		String desc;
		String ft;
		String lit;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("update airecognizermanual join aiproperties on (aiproperties.propertydescription=airecognizermanual.propertydescription and aiproperties.typeid=airecognizermanual.typeid) set airecognizermanual.propertyid=aiproperties.propertyid where aiproperties.tenant="+tenant+" and airecognizermanual.tenant="+tenant+";");
			query="select * from airecognizermanual where tenant="+tenant+" and fts='' and regex='';";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="delete from airecognizer where tenant="+tenant+" and propertyid="+rs.getInt("propertyid")+" and typeid="+rs.getInt("typeid")+";";
				Dbutil.executeQuery(query, con);
			}
			query="select * from airecognizermanual where tenant="+tenant+" and fts!='' or regex!='';";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="update airecognizer set fts='"+Spider.SQLEscape(rs.getString("fts"))+"', regex='"+Spider.SQLEscape(rs.getString("regex"))+"', regexexcl='"+Spider.SQLEscape(rs.getString("regexexcl"))+"' where tenant="+tenant+" and propertyid="+rs.getInt("propertyid")+" and typeid="+rs.getInt("typeid")+";";
				Dbutil.executeQuery(query, con);
			}
			Dbutil.closeRs(rs);
			/*
			rs=Dbutil.selectQuery("select * from airecognizer where typeid in (3,5,6,7);", con);
			while (rs.next()){
				query="delete airecognizer.* from airecognizer natural join aiproperties where tenant="+tenant+" and typeid=4 and "+Recognizer.whereClause("propertydescription", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"))+";";
				Dbutil.executeQuery(query,con);
				query="delete from aiproperties where tenant="+tenant+" and typeid=4 and "+Recognizer.whereClause("propertydescription", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"))+";";
				Dbutil.executeQuery(query,con);
			}

			rs=Dbutil.selectQuery("select * from airecognizer where typeid=8;", con);
			while (rs.next()){
				String[] terms=rs.getString("fts").split(" ?\\+");
				for (String term:terms){
					if (term.length()>3){
						String ids=Dbutil.readValueFromDB("select group_concat(propertyid) as ids from aiproperties where typeid=4 and propertydescription like '"+term+"';", "ids");
						query="delete from airecognizer where tenant="+tenant+" and typeid=4 and propertyid in ("+ids+");";
						Dbutil.executeQuery(query,con);
						query="delete from aiproperties where tenant="+tenant+" and typeid=4 and propertyid in ("+ids+");";
						Dbutil.executeQuery(query,con);
					}
				}
			}
			 */


		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}


	}


	public static String filterCompleteTerm(String term){

		if (term.toLowerCase().contains("s.p.a.")) term=term.toLowerCase().replace("s.p.a.","");
		if (term.toLowerCase().contains(" s.a.")) term=term.toLowerCase().replace(" s.a.","");
		if (term.toLowerCase().contains(" d'")) term=term.toLowerCase().replace(" d'"," ");
		if (term.toLowerCase().contains("'s ")) term=term.toLowerCase().replace("'s "," ");
		return term;
	}

	public static String filterTerm(String term){

		if (term.toLowerCase().equals("wines")) term="";
		if (term.toLowerCase().equals("vineyards")) term="";
		if (term.toLowerCase().equals("vineyard")) term="";
		if (term.toLowerCase().equals("cantine")) term="";
		if (term.toLowerCase().equals("cantina")) term="";
		if (term.toLowerCase().equals("quinta")) term="";
		if (term.toLowerCase().equals("weingut")) term="";
		if (term.toLowerCase().equals("bodega")) term="";
		if (term.toLowerCase().equals("celler")) term="";
		if (term.toLowerCase().equals("cellar")) term="";
		if (term.toLowerCase().equals("cellars")) term="";
		if (term.toLowerCase().equals("family")) term="";
		if (term.toLowerCase().equals("winery")) term="";
		if (term.toLowerCase().equals("maison")) term="";
		if (term.toLowerCase().equals("azienda")) term="";
		if (term.toLowerCase().equals("agricola")) term="";
		if (term.toLowerCase().equals("sa")) term="";
		//if (term.toLowerCase().equals("poggio")) term="";
		if (term.toLowerCase().equals("bodegas")) term="";
		if (term.toLowerCase().equals("bodega")) term="";
		if (term.toLowerCase().equals("earl")) term="";
		if (term.toLowerCase().equals("tenuta")) term="";
		if (term.toLowerCase().equals("chateau")) term="";
		if (term.toLowerCase().equals("ch�teau")) term="";
		if (term.toLowerCase().equals("domaine")) term="";
		if (term.toLowerCase().equals("pere")) term="";
		if (term.toLowerCase().equals("p�re")) term="";
		if (term.toLowerCase().equals("le")) term="";
		if (term.toLowerCase().equals("les")) term="";
		if (term.toLowerCase().equals("la")) term="";
		if (term.toLowerCase().equals("et")) term="";
		if (term.toLowerCase().equals("y")) term="";
		if (term.toLowerCase().equals("e")) term="";
		if (term.toLowerCase().equals("fils")) term="";
		if (term.toLowerCase().equals("igt")) term="";
		if (term.toLowerCase().equals("p�re")) term="";
		if (term.toLowerCase().equals("cuvee")) term="";
		if (term.toLowerCase().equals("cuv�e")) term="";
		if (term.toLowerCase().equals("doc")) term="";
		if (term.toLowerCase().equals("docg")) term="";
		if (term.toLowerCase().equals("de")) term="";
		if (term.toLowerCase().equals("del")) term="";
		if (term.toLowerCase().equals("da")) term="";
		if (term.toLowerCase().equals("of")) term="";
		if (term.toLowerCase().equals("du")) term="";
		if (term.toLowerCase().equals("el")) term="";
		if (term.toLowerCase().equals("di")) term="";
		if (term.toLowerCase().equals("dr")) term="";
		if (term.toLowerCase().equals("�")) term="";
		if (term.toLowerCase().equals("a")) term="";
		if (term.toLowerCase().equals("and")) term="";
		if (term.toLowerCase().equals("sainte")) term="(ste sainte)";
		if (term.toLowerCase().equals("saint")) term="(st saint)";
		if (term.toLowerCase().equals("st")) term="(st saint)";
		if (term.toLowerCase().equals("ste")) term="(ste sainte)";

		return term;
	}

	public static String filterRegionTerm(String term){

		if (term.toLowerCase().equals("le")) term="";
		if (term.toLowerCase().equals("les")) term="";
		if (term.toLowerCase().equals("la")) term="";
		if (term.toLowerCase().equals("d")) term="";
		if (term.toLowerCase().equals("y")) term="";
		if (term.toLowerCase().equals("igt")) term="";
		if (term.toLowerCase().equals("aoc")) term="";
		if (term.toLowerCase().equals("doc")) term="";
		if (term.toLowerCase().equals("vqa")) term="";
		if (term.toLowerCase().equals("docg")) term="";
		if (term.toLowerCase().equals("vdqs")) term="";
		if (term.toLowerCase().equals("de")) term="";
		if (term.toLowerCase().equals("da")) term="";
		if (term.toLowerCase().equals("of")) term="";
		if (term.toLowerCase().equals("du")) term="";
		if (term.toLowerCase().equals("el")) term="";
		if (term.toLowerCase().equals("di")) term="";
		if (term.toLowerCase().equals("dr")) term="";
		if (term.toLowerCase().equals("�")) term="";
		if (term.toLowerCase().equals("a")) term="";
		if (term.toLowerCase().equals("l")) term="";
		if (term.toLowerCase().equals("and")) term="";
		if (term.toLowerCase().equals("saint")) term="(st saint)";
		if (term.toLowerCase().equals("sainte")) term="(ste sainte)";
		if (term.toLowerCase().equals("st")) term="(st saint)";
		if (term.toLowerCase().equals("ste")) term="(ste sainte)";
		if (term.toLowerCase().equals("1er")) term="(1er premier)";

		return term;
	}

	public static String filterRegion(String term){

		if (term.startsWith("Vin de Pays des C�teaux ")) term=term.replace("Vin de Pays des C�teaux ", "");
		if (term.startsWith("Vin de Pays de ")) term=term.replace("Vin de Pays de ", "");
		if (term.startsWith("Vin de Pays des ")) term=term.replace("Vin de Pays des ", "");
		if (term.toLowerCase().startsWith("vin de pays du ")) term=term.toLowerCase().replace("vin de pays du ", "");
		if (term.startsWith("Vino de la Tierra ")) term=term.replace("Vino de la Tierra ", "");
		if (term.startsWith("C�tes du Rh�ne Villages ")) term=term.replace("C�tes du Rh�ne Villages ", "");
		if (term.startsWith("Vino de la Tierra ")) term=term.replace("Vino de la Tierra ", "");
		if (term.startsWith("Coteaux du Languedoc ")) term=term.replace("Coteaux du Languedoc ", "");
		if (term.startsWith("Vinho Regional ")) term=term.replace("Vinho Regional ", "");
		 
		
		return term;
	}

	
	public static String filterTermCuvee(String term){
		String desc=term.toLowerCase();
		if (desc.equals("igt")) term="";
		if (desc.equals("doc")) term="";
		if (desc.equals("docg")) term="";
		if (desc.equals("aoc")) term="";
		if (desc.equals("magnum")) term="";
		if (desc.equals("cuvee")) term="";
		if (desc.equals("cuv�e")) term="";
		if (desc.equals("de")) term="";
		if (desc.equals("d")) term="";
		if (desc.equals("s")) term="";
		if (desc.equals("da")) term="";
		if (desc.equals("of")) term="";
		if (desc.equals("du")) term="";
		if (desc.equals("el")) term="";
		if (desc.equals("di")) term="";
		if (desc.equals("dr")) term="";
		if (desc.equals("and")) term="";
		if (desc.equals("saint")) term="(st saint)";
		if (desc.equals("st")) term="(st saint)";
		return term;
	}
	
	public void fillAiitems(){
		fillAiitems("knownwines");
	}
	
	public void fillAiitems(String sourcetable){
		String query;
		String desc;
		String ft;
		String lit;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			query="Truncate TABLE  `wijn`.`aiitems`;";
			Dbutil.executeQuery(query);
			//query="CREATE TABLE  `wijn`.`aiitems` (  `tenant` int(10) unsigned NOT NULL,  `itemid` int(10) unsigned NOT NULL AUTO_INCREMENT,  `productgroupid` int(10) unsigned NOT NULL,  `itemdescription` varchar(255) NOT NULL,  `itemregexexcl` varchar(255) NOT NULL,  `probability` int(10) unsigned NOT NULL DEFAULT '0', PRIMARY KEY (`tenant`,`itemid`),  KEY `inditemdescription` (`itemdescription`),  FULLTEXT KEY `ftdescription` (`itemdescription`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;";			
			//Dbutil.executeQuery(query);
			query="insert into aiitems (tenant,itemid,productgroupid,itemdescription,itemregexexcl,probability) select 1,id,1,wine,'',bottles from "+sourcetable+" where disabled=0;";
			Dbutil.executeQuery(query);
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}


	public void fillAiitemproperties(){
		Dbutil.logger.info("Starting fillAiitemproperties");
		String query;
		String desc;
		String ft;
		String lit;
		ResultSet rs=null;
		ResultSet rs2=null;
		String recognize="";
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			query="DROP TABLE IF EXISTS `wijn`.`aiitemproperties`;";
			Dbutil.executeQuery(query);
			query="CREATE TABLE  `wijn`.`aiitemproperties` (  `tenant` int(10) unsigned NOT NULL,  `itemid` int(10) unsigned NOT NULL,  `propertyid` int(10) unsigned NOT NULL,  `typeid` int(10) unsigned NOT NULL,  PRIMARY KEY (`tenant`,`itemid`,`propertyid`,`typeid`) USING BTREE,  KEY `itemidtypeid` (`itemid`,`typeid`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;";
			Dbutil.executeQuery(query);

			query="select * from airecognizer where tenant=1 and (typeid=4 or typeid=8) order by typeid,propertyid desc;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="";
				//if (rs.getInt("typeid")==1) query="select * from aiitems left join aiitemproperties on (aiitems.itemid=aiitemproperties.itemid and aiitemproperties.typeid=1) where itemdescription like '"+Spider.SQLEscape(rs.getString("propertydescription"))+"%' having aiitemproperties.propertyid is null;";
				if (rs.getInt("typeid")==8) query="select id as itemid from "+itemtable+" where disabled=0 and "+Recognizer.whereClause("grapes,cuvee", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"))+";";
				if (rs.getInt("typeid")==4) query="select id as itemid from "+itemtable+" where disabled=0 and "+Recognizer.whereClause("wine,locale", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"))+";";
				if (!query.equals("")&&!query.endsWith("and ;")){
					rs2=Dbutil.selectQuery(query, con2);
					while (rs2.next()){
						Dbutil.executeQuery("insert into aiitemproperties (tenant,itemid,propertyid,typeid) values ("+tenant+","+rs2.getInt("itemid")+","+rs.getInt("propertyid")+","+rs.getInt("typeid")+");", con2);
					}
					Dbutil.closeRs(rs2);
				}
				if (rs.getInt("typeid")==4) {
					query="select id as itemid from "+itemtable+" where disabled=0 and "+Recognizer.whereClause("cuvee,vineyard", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"))+";";
					if (!query.equals("")&&!query.endsWith("and ;")){
						rs2=Dbutil.selectQuery(query, con2);
						while (rs2.next()){
							Dbutil.executeQuery("insert into aiitemproperties (tenant,itemid,propertyid,typeid) values ("+tenant+","+rs2.getInt("itemid")+","+rs.getInt("propertyid")+",3);", con2);
						}
						Dbutil.closeRs(rs2);
					}
				}
			}

			query="insert into aiitemproperties (tenant,itemid,propertyid,typeid) select 1,id as itemid,propertyid,typeid from aiproperties join "+itemtable+" on (propertydescription=producer) where tenant=1 and typeid=1 and disabled=0 order by propertyid;";
			Dbutil.executeQuery(query, con);
			query="insert into aiitemproperties (tenant,itemid,propertyid,typeid) select 1,id as itemid,propertyid,typeid from aiproperties join "+itemtable+" on (propertydescription=locale) where tenant=1 and typeid=2 and disabled=0 order by propertyid;";
			Dbutil.executeQuery(query, con);
			query="insert into aiitemproperties (tenant,itemid,propertyid,typeid) select 1,id as itemid,propertyid,typeid from aiproperties join "+itemtable+" on (propertydescription=vineyard) where tenant=1 and typeid=3 and disabled=0 order by propertyid;";
			Dbutil.executeQuery(query, con);
			int propid=0;
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=7 and propertydescription='Sparkling';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",7,1 from "+itemtable+" where type like '%Sparkling' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=6 and propertydescription='Red';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",6,1 from "+itemtable+" where type like 'Red%' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=6 and propertydescription='White';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",6,1 from "+itemtable+" where type like 'White%' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=6 and propertydescription='Ros�';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",6,1 from "+itemtable+" where type like 'Ros�%' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=5 and propertydescription='Sweet/Dessert';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",5,1 from "+itemtable+" where type like '%Sweet/Dessert' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=5 and propertydescription='Fortified';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",5,1 from "+itemtable+" where type like '%Fortified' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=5 and propertydescription='Off-dry';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",5,1 from "+itemtable+" where type like '%Off-dry' and disabled=0;";
			Dbutil.executeQuery(query);
			propid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=5 and propertydescription='Dry';", "propertyid");
			query="insert into aiitemproperties (itemid,propertyid,typeid,tenant) select distinct(id),"+propid+",5,1 from "+itemtable+" where (type='Red' or type='White' or type='Ros�' or type='Red - Sparkling' or type='White - Sparkling' or type='Ros� - Sparkling') and disabled=0";
			Dbutil.executeQuery(query);


			/*
			query="select id,color.propertyid as color, dryness.propertyid as dryness, if (sparkling is null,null,sparkling+8) as sparkling from "+itemtable+" left join aiproperties color on (color.typeid=6 and color.propertydescription=color)  left join aiproperties dryness on (dryness.typeid=5 and dryness.propertydescription=dryness) where disabled=0;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				if (rs.getString("color")!=null) Dbutil.executeQuery("insert into aiitemproperties (itemid,propertyid,typeid,tenant) values("+rs.getInt("id")+","+rs.getInt("color")+",6,1);",con2);
				if (rs.getString("dryness")!=null) Dbutil.executeQuery("insert into aiitemproperties (itemid,propertyid,typeid,tenant) values("+rs.getInt("id")+","+rs.getInt("dryness")+",5,1);",con2);
				if (rs.getString("sparkling")!=null) Dbutil.executeQuery("insert into aiitemproperties (itemid,propertyid,typeid,tenant) values("+rs.getInt("id")+","+rs.getInt("sparkling")+",7,1);",con2);
			}
			 */

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
		}


	}

	public void fillPropsConsolidated(){
		Dbutil.logger.info("Starting fillPropsConsolidated");
		Dbutil.executeQuery("truncate aiitempropsconsolidated;");
		Dbutil.executeQuery("alter table aiitempropsconsolidated disable keys;");
		Dbutil.executeQuery("load index into cache aiitemproperties;");
		Dbutil.executeQuery("insert ignore into aiitempropsconsolidated (itemid,propertyids) select distinct itemid,0  from aiitemproperties order by itemid;");
		//Dbutil.executeQuery("update aiitempropsconsolidated join (select itemid,group_concat(propertyid) as ids from (select * from aiitemproperties where typeid=4 order by itemid,propertyid) df group by itemid) props on (aiitempropsconsolidated.itemid=props.itemid) set propertyids=props.ids;");
		Dbutil.executeQuery("update aiitempropsconsolidated join (select itemid,group_concat(propertyid order by propertyid) as ids from aiitemproperties where typeid=4 group by itemid order by itemid) props on (aiitempropsconsolidated.itemid=props.itemid) set propertyids=props.ids;");
		Dbutil.executeQuery("update aiitempropsconsolidated join (select itemid,group_concat(propertyid order by propertyid) as ids from aiitemproperties where typeid=3 group by itemid order by itemid) props on (aiitempropsconsolidated.itemid=props.itemid) set type3id=props.ids;");
		Dbutil.executeQuery("update aiitempropsconsolidated join aiitemproperties on (typeid=1 and aiitemproperties.tenant="+tenant+" and aiitemproperties.itemid=aiitempropsconsolidated.itemid) set type1id=aiitemproperties.propertyid;");
		Dbutil.executeQuery("update aiitempropsconsolidated join aiitemproperties on (typeid=2 and aiitemproperties.tenant="+tenant+" and aiitemproperties.itemid=aiitempropsconsolidated.itemid) set type2id=aiitemproperties.propertyid;");
		Dbutil.executeQuery("update aiitempropsconsolidated join aiitemproperties on (typeid=5 and aiitemproperties.tenant="+tenant+" and aiitemproperties.itemid=aiitempropsconsolidated.itemid) set type5id=aiitemproperties.propertyid;");
		Dbutil.executeQuery("update aiitempropsconsolidated join aiitemproperties on (typeid=6 and aiitemproperties.tenant="+tenant+" and aiitemproperties.itemid=aiitempropsconsolidated.itemid) set type6id=aiitemproperties.propertyid;");
		Dbutil.executeQuery("update aiitempropsconsolidated join aiitemproperties on (typeid=7 and aiitemproperties.tenant="+tenant+" and aiitemproperties.itemid=aiitempropsconsolidated.itemid) set type7id=aiitemproperties.propertyid;");
		Dbutil.executeQuery("update aiitempropsconsolidated join (select itemid,group_concat(propertyid) as ids from (select * from aiitemproperties where typeid=8 order by itemid,propertyid) df group by itemid) props on (aiitempropsconsolidated.itemid=props.itemid) set type8ids=props.ids;");

		TreeSet<Integer> t;
		ResultSet rs=null;
		ResultSet rs2=null;
		String recognize="";
		Iterator it;
		String newprop;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectUpdatableQuery("select id,propertyids from aiitempropsconsolidated order by itemid", con);
			while (rs.next()){
				newprop="";
				t=new TreeSet<Integer>();
				for (String num:rs.getString("propertyids").split(",")){
					t.add(Integer.parseInt(num));
				}
				it=t.iterator();
				while (it.hasNext()){
					newprop+=","+it.next();
				}
				if (newprop.length()>1) newprop=newprop.substring(1);
				rs.updateString("propertyids", newprop);
				rs.updateRow();
			}


		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.executeQuery("alter table aiitempropsconsolidated enable keys;");
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}


	}

	public void generateUniqueCombinations(){
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		ResultSet rs3=null;
		String[] props=null;
		String[] prop=new String[4];
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("delete from aicombinations;");
			//Dbutil.executeQuery("delete from ainotuniquecombinations;");
			query="select * from aiitempropsconsolidated where itemid>351;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.logger.info("Itemid="+rs.getInt("itemid"));
				Dbutil.logger.info("Properties:"+rs.getString("propertyids").split(",").length);
				HashMap<Integer,Integer> set=new HashMap<Integer,Integer>();
				HashMap<Integer,Integer> reverseset=new HashMap<Integer,Integer>();
				props=rs.getString("propertyids").split(",");
				int t=0;
				for (String num:props){
					set.put(t,Integer.parseInt(num));
					reverseset.put(Integer.parseInt(num),t);
					t++;
				}
				Dbutil.executeQuery("delete from aihanoy;");
				Dbutil.executeQuery("alter table aihanoy auto_increment=0;");
				for (int i=0;i<permutations.get(props.length-1).size();i++){

					String combi="";
					for (int j=0;j<permutations.get(props.length-1).get(i).size();j++){
						combi+="+"+props[permutations.get(props.length-1).get(i).get(j)]+" ";
					}
					Dbutil.executeQuery("insert into aihanoy (propertyids) values ('"+combi.trim()+"');", con);
				}
				boolean finished=false;
				boolean threecombinations=false; // This boolean tracks when we have reached the point in Hanoy with 3 combinatioons, at which point we may start filtering if there are too many results left
				int hanoy=0;
				int notunique=0;
				int sel=0;
				while (!finished){
					long start=new java.util.Date().getTime();
					rs2=Dbutil.selectQuery("select * from aihanoy where items=0 order by id limit 1", con);
					if (rs2.next()){
						if (!threecombinations){
							if (rs2.getString("propertyids").split("\\+").length>3){
								threecombinations=true;
								if (rs2.getString("propertyids").split("\\+ ?")[1].equals(rs2.getString("propertyids").split("\\+ ?")[2])){
									// Permutations were limited and must be expanded
									rs3=Dbutil.selectQuery("select group_concat(propertyids) as selection from (select propertyids, items from aihanoy where items>0 and propertyids like '+%+%' order by items limit 20) sel group by '1';", con);
									if (rs3.next()){
										String[] selection=rs3.getString("selection").split(",");
										HashMap<Integer,HashMap<Integer,Integer>> basis=new HashMap<Integer,HashMap<Integer,Integer>>();
										for (String pair:selection){
											pair=pair.replace("+", "");
											HashMap<Integer,Integer> p=new HashMap<Integer,Integer>();
											p.put(0,reverseset.get(Integer.parseInt(pair.split(" ")[0])));
											p.put(1,reverseset.get(Integer.parseInt(pair.split(" ")[1])));
											basis.put(basis.size(), p);
										}
										generateLimitedPermutations(basis, props.length-1);
										query="delete from aihanoy where items=0 or items>1;";
										Dbutil.executeQuery(query, con);
										for (int i=0;i<limitedpermutations.size();i++){

											String combi="";
											//Dbutil.logger.info(limitedpermutations.get(i));
											for (int j=0;j<limitedpermutations.get(i).size();j++){
												combi+="+"+props[limitedpermutations.get(i).get(j)]+" ";
											}
											Dbutil.executeQuery("insert ignore into aihanoy (propertyids) values ('"+combi.trim()+"');", con);
										}
										// Filter combinations that were already unique
										query="select group_concat(propertyids) as ids from aihanoy where items=1 group by '1';";
										Dbutil.closeRs(rs3);
										rs3=Dbutil.selectQuery(query, con);
										if (rs3.next()){
											String[] filter=rs3.getString("ids").split(",");
											for (String id:filter){
												Dbutil.executeQuery("delete from aihanoy where propertyids like '%"+id.replace(" ", "%")+"%';", con);
											}

										}
										Dbutil.closeRs(rs3);
									}
								}
							}

						}


						hanoy+=new java.util.Date().getTime()-start;
						//Dbutil.logger.info("Testing "+rs2.getString("propertyids"));
						start=new java.util.Date().getTime();
						//rs3=Dbutil.selectQuery("select count(*) as thecount from ainotuniquecombinations where fts=md5('"+rs2.getString("propertyids")+"');", con);
						//if (rs3.next()&&rs3.getInt("thecount")>0){
						//	notunique+=new java.util.Date().getTime()-start;
						//	Dbutil.executeQuery("delete from aihanoy where id="+rs2.getInt("id"), con);
						//} else {
						notunique+=new java.util.Date().getTime()-start;
						Dbutil.closeRs(rs3);
						start=new java.util.Date().getTime();
						query="select count(distinct itemid) as thecount from aiitempropsconsolidated where match (propertyids) against('"+rs2.getString("propertyids")+"' in boolean mode);";
						rs3=Dbutil.selectQuery(query, con);
						if (rs3.next()){
							sel+=new java.util.Date().getTime()-start;
							if (rs3.getInt("thecount")==1){
								prop[0]=null;
								prop[1]=null;
								prop[2]=null;
								prop[3]=null;
								int c=0;
								for (String propje:rs2.getString("propertyids").replace("+", "").split(" +")){
									if (c<4) prop[c]=propje;
									c++;
								}
								Dbutil.executeQuery("insert into aicombinations (tenant,itemid,fts,ftsexclude,propertyid1,propertyid2,propertyid3,propertyid4) values ("+tenant+","+rs.getInt("itemid")+",'"+rs2.getString("propertyids")+"','',"+prop[0]+","+prop[1]+","+prop[2]+","+prop[3]+");", con);
								Dbutil.executeQuery("update aihanoy set items=1 where id="+rs2.getInt("id"), con);
								//Dbutil.executeQuery("delete from aihanoy where propertyids like '%"+rs2.getString("propertyids").replace(" ", "%")+"%';", con);
							} else {
								Dbutil.executeQuery("update aihanoy set items="+rs3.getInt("thecount")+" where id="+rs2.getInt("id"), con);
								//Dbutil.executeQuery("insert into ainotuniquecombinations (fts) values (md5('"+rs2.getString("propertyids")+"'));", con);

							}
						} else {
							sel+=new java.util.Date().getTime()-start;
							Dbutil.logger.error("Should never have reached this code");
							//Dbutil.executeQuery("delete from aihanoy where id="+rs2.getInt("id"), con);
						}
						//}
					} else {
						finished=true;
					}
					Dbutil.closeRs(rs2);
					Dbutil.closeRs(rs3);
				}
				Dbutil.logger.info("Hanoy:"+hanoy);
				Dbutil.logger.info("Notunique:"+notunique);
				Dbutil.logger.info("Sel:"+sel);

			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			try {
				Dbutil.logger.info(rs.getString("propertyids"));
			} catch (SQLException e1) {
				Dbutil.logger.error("Problem: ",e1);

			}
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeRs(rs3);
			Dbutil.closeConnection(con);
		}

	}

	public void generateUniqueCombinations(boolean negatives){
		Dbutil.executeQuery("load index into cache aiitempropsconsolidated;");
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		ResultSet rs3=null;
		String[] props=null;
		String[] negs=null;
		String[] prop=new String[4];
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		java.sql.Connection con3=Dbutil.openNewConnection();
		java.sql.Connection con4=Dbutil.openNewConnection();
		String negative="";
		boolean perf=true;
		int t=0;
		int rows=0;
		int ft=0;
		int hanoy=0;
		int select=0;
		int nselect=0;
		int insert=0;
		int other=0;
		int thiscounted=0;
		long overallstart=0;
		long start=0;
		boolean finished;
		boolean found;
		HashMap<Integer,Integer> set;
		HashMap<Integer,Integer> reverseset;

		try{
			Dbutil.executeQuery("SET SESSION group_concat_max_len = 1000000;", con2);
			if (!negatives) Dbutil.executeQuery("delete from aicombinations;");
			//Dbutil.executeQuery("delete from ainotuniquecombinations;");
			query="select * from aiitempropsconsolidated left join aicombinations on (aiitempropsconsolidated.itemid=aicombinations.itemid) where aiitempropsconsolidated.itemid>0 having propertyid1 is null;";
			if (!negatives) query="select * from aiitempropsconsolidated where itemid>0;";
			rs=Dbutil.selectQuery(query, con2);
			Dbutil.executeQuery("ALTER TABLE `wijn`.`aicombinations` DROP INDEX `indexfts`, DROP INDEX `propertyids`, DROP INDEX `itemid`;");
			while (rs.next()){
				int row=rs.getInt("itemid");
				if (perf) overallstart=new java.util.Date().getTime();
				thiscounted=0;
				finished=false;
				found=false;
				if (negatives){
					query="select group_concat(distinct(propertyids)) as negative from aiitempropsconsolidated where match (propertyids) against ('+"+rs.getString("propertyids").replace(",", " +")+"' in boolean mode) and itemid !="+rs.getInt("itemid")+";";
					rs2=Dbutil.selectQuery(query, con2);
					if (rs2.next()){
						negative=rs2.getString("negative");
						TreeSet<Integer> negset=new TreeSet<Integer>();
						props=rs.getString("propertyids").split(",");
						negs=negative.split(",");
						for (String num:negs){
							negset.add(Integer.parseInt(num));
						}
						Iterator<Integer> ni=negset.iterator();
						negative="";
						while (ni.hasNext()){
							negative+=","+ni.next();
						}
						for (String num:props){
							negative=negative.replaceAll("(^|,)"+num+"($|,)", ",");
						}

						negset=null;
						ni=null;
						negative=(","+negative).replaceAll(",+", ",").replaceAll(",$", "").replace(",", " +").trim();
						if (negative.equals("")) {
							String conflict=Dbutil.readValueFromDB("select group_concat(itemid) as ids from aiitempropsconsolidated where propertyids='"+rs.getString("propertyids")+"';", "ids");
							Dbutil.logger.info("Cannot distinguish between wines "+conflict);
							finished=true;
						}
					}
					Dbutil.closeRs(rs2);
				}
				if (!negatives) {
					query="select distinct itemid as itemid from aiitempropsconsolidated where itemid!="+rs.getInt("itemid")+" and match (propertyids) against('+"+rs.getString("propertyids").replace(",", " +")+"' in boolean mode) limit 1;";
					rs2=Dbutil.selectQuery(query, con2);

					if (rs2.next()){
						//Dbutil.logger.info("Skipping itemid "+rs.getInt("itemid")+" ("+Dbutil.readValueFromDB("select itemdescription from aiitems where itemid="+rs.getInt("itemid"), "itemdescription")+") because it requires a negative to be distinguished from "+rs2.getInt("itemid")+" ("+Dbutil.readValueFromDB("select itemdescription from aiitems where itemid="+rs2.getInt("itemid"), "itemdescription")+").");
						finished=true;
					}
					Dbutil.closeRs(rs2);

				}
				if (negatives) {
					query="select distinct itemid as itemid from aiitempropsconsolidated where itemid!="+rs.getInt("itemid")+" and propertyids='"+rs.getString("propertyids")+"' limit 1;";
					rs2=Dbutil.selectQuery(query, con2);

					if (rs2.next()){
						Dbutil.logger.info("Skipping itemid "+rs.getInt("itemid")+" ("+Dbutil.readValueFromDB("select itemdescription from aiitems where itemid="+rs.getInt("itemid"), "itemdescription")+") because has the same properties as "+rs2.getInt("itemid")+" ("+Dbutil.readValueFromDB("select itemdescription from aiitems where itemid="+rs2.getInt("itemid"), "itemdescription")+").");
						finished=true;
					}
					Dbutil.closeRs(rs2);

				}
				if (!finished){
					//Dbutil.logger.info("Itemid="+rs.getInt("itemid"));
					//Dbutil.logger.info("Properties:"+rs.getString("propertyids").split(",").length);
					rows++;
					if (perf) start=new java.util.Date().getTime();
					//set=new HashMap<Integer,Integer>();
					reverseset=new HashMap<Integer,Integer>();
					props=rs.getString("propertyids").split(",");
					t=0;
					for (String num:props){
						//set.put(t,Integer.parseInt(num));
						reverseset.put(Integer.parseInt(num),t);
						t++;
					}
					//Dbutil.executeQuery("delete from aihanoy;");
					//Dbutil.executeQuery("alter table aihanoy auto_increment=0;");
					Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`aihanoy`;",con4);
					Dbutil.executeQuery("CREATE TABLE  `wijn`.`aihanoy` ( `propertyids` varchar(200) NOT NULL, `id` int(10) unsigned NOT NULL AUTO_INCREMENT, `items` int(10) unsigned NOT NULL DEFAULT '0', PRIMARY KEY (`id`,`items`) USING BTREE, UNIQUE KEY `propertyids` (`propertyids`)) ENGINE=MEMORY AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;");
					for (int i=0;i<permutations.get(props.length-1).size();i++){

						String combi="";
						for (int j=0;j<permutations.get(props.length-1).get(i).size();j++){
							combi+="+"+props[permutations.get(props.length-1).get(i).get(j)]+" ";
						}
						Dbutil.executeQuery("insert into aihanoy (propertyids) values ('"+combi.trim()+"');", con4);
						//Dbutil.executeQuery("insert into aihanoy (propertyids) values ('"+combi.trim()+"');", con);
					}
					if (perf) hanoy+=(new java.util.Date().getTime()-start);
					if (perf) thiscounted+=(new java.util.Date().getTime()-start);
					boolean threecombinations=false; // This boolean tracks when we have reached the point in Hanoy with 3 combinatioons, at which point we may start filtering if there are too many results left
					while (!finished){
						rs2=Dbutil.selectQuery("select * from aihanoy where items=0 order by id limit 1", con2);
						if (rs2.next()){
							if (!threecombinations){
								if (rs2.getString("propertyids").split("\\+").length>3){
									threecombinations=true;
									if (rs2.getString("propertyids").split("\\+ ?")[1].equals(rs2.getString("propertyids").split("\\+ ?")[2])){
										// Permutations were limited and must be expanded
										rs3=Dbutil.selectQuery("select group_concat(propertyids) as selection from (select propertyids, items from aihanoy where items>0 and propertyids like '+%+%' order by items limit "+pairstoselect+") sel group by '1';", con3);
										if (rs3.next()){
											String[] selection=rs3.getString("selection").split(",");
											HashMap<Integer,HashMap<Integer,Integer>> basis=new HashMap<Integer,HashMap<Integer,Integer>>();
											for (String pair:selection){
												pair=pair.replace("+", "");
												HashMap<Integer,Integer> p=new HashMap<Integer,Integer>();
												p.put(0,reverseset.get(Integer.parseInt(pair.split(" ")[0])));
												p.put(1,reverseset.get(Integer.parseInt(pair.split(" ")[1])));
												basis.put(basis.size(), p);
												p=null;
											}
											generateLimitedPermutations(basis, props.length-1);
											basis=null;
											query="delete from aihanoy where items=0 or items>1;";
											Dbutil.executeQuery(query, con4);
											for (int i=0;i<limitedpermutations.size();i++){

												String combi="";
												//Dbutil.logger.info(limitedpermutations.get(i));
												for (int j=0;j<limitedpermutations.get(i).size();j++){
													combi+="+"+props[limitedpermutations.get(i).get(j)]+" ";
												}
												Dbutil.executeQuery("insert ignore into aihanoy (propertyids) values ('"+(combi.trim()+" "+negative.replace("+","-")).trim()+"');", con4);
											}
											// Filter combinations that were already unique
											query="select group_concat(propertyids) as ids from aihanoy where items=1 group by '1';";
											Dbutil.closeRs(rs3);
											rs3=Dbutil.selectQuery(query, con3);
											if (rs3.next()){
												String[] filter=rs3.getString("ids").split(",");
												for (String id:filter){
													Dbutil.executeQuery("delete from aihanoy where propertyids like '%"+id.replace(" ", "%")+"%';", con4);
												}

											}
											Dbutil.closeRs(rs3);
										}
									}
								}

							}


							//hanoy+=new java.util.Date().getTime()-start;
							//Dbutil.logger.info("Testing "+rs2.getString("propertyids"));
							//start=new java.util.Date().getTime();
							//rs3=Dbutil.selectQuery("select count(*) as thecount from ainotuniquecombinations where fts=md5('"+rs2.getString("propertyids")+"');", con);
							//if (rs3.next()&&rs3.getInt("thecount")>0){
							//	notunique+=new java.util.Date().getTime()-start;
							//	Dbutil.executeQuery("delete from aihanoy where id="+rs2.getInt("id"), con);
							//} else {
							//notunique+=new java.util.Date().getTime()-start;
							Dbutil.closeRs(rs3);
							if (perf) start=new java.util.Date().getTime();
							query="select count(distinct itemid) as thecount from aiitempropsconsolidated where match (propertyids) against('"+rs2.getString("propertyids")+(negatives?" "+negative.replace("+","-"):"")+"' in boolean mode);";
							rs3=Dbutil.selectQuery(query, con3);
							if (rs3.next()){
								if (perf) nselect+=1;
								if (perf) select+=(new java.util.Date().getTime()-start);
								if (perf) thiscounted+=(new java.util.Date().getTime()-start);

								if (rs3.getInt("thecount")==1){
									found=true;
									prop[0]=null;
									prop[1]=null;
									prop[2]=null;
									prop[3]=null;
									int c=0;
									for (String propje:rs2.getString("propertyids").replaceAll(" -.*$", "").replace("+", "").split(" +")){
										if (c<4) prop[c]=propje;
										c++;
									}
									//Dbutil.executeQuery("insert into aicombinations (tenant,itemid,fts,ftsexclude,propertyid1,propertyid2,propertyid3,propertyid4) values ("+tenant+","+rs.getInt("itemid")+",'"+rs2.getString("propertyids")+"','',"+prop[0]+","+prop[1]+","+prop[2]+","+prop[3]+");", con);
									if (perf) start=new java.util.Date().getTime();
									Dbutil.executeQuery("insert into aicombinations (tenant,itemid,fts,ftsexclude,propertyid1,propertyid2,propertyid3,propertyid4) values ("+tenant+","+rs.getInt("itemid")+",'"+rs2.getString("propertyids").replaceAll("-.*$", "")+"','"+negative.replace("+", "-")+"',"+prop[0]+","+prop[1]+","+prop[2]+","+prop[3]+");", con4);
									Dbutil.executeQuery("update aihanoy set items=1 where propertyids like '%"+rs2.getString("propertyids").replace(" ", "%")+"%';", con4);
									if (perf) insert+=(new java.util.Date().getTime()-start);
									if (perf) thiscounted+=(new java.util.Date().getTime()-start);
									//	Dbutil.executeQuery("delete from aihanoy where propertyids like '%"+rs2.getString("propertyids").replace(" ", "%")+"%';", con);
								} else {
									if (perf) start=new java.util.Date().getTime();
									Dbutil.executeQuery("update aihanoy set items="+rs3.getInt("thecount")+" where id="+rs2.getInt("id"), con4);
									//Dbutil.executeQuery("insert into ainotuniquecombinations (fts) values (md5('"+rs2.getString("propertyids")+"'));", con);
									if (perf) insert+=(new java.util.Date().getTime()-start);
									if (perf) thiscounted+=(new java.util.Date().getTime()-start);

								}
							} else {
								//sel+=new java.util.Date().getTime()-start;
								Dbutil.logger.error("Should never have reached this code");
								//Dbutil.executeQuery("delete from aihanoy where id="+rs2.getInt("id"), con);
							}
							//}
						} else {
							finished=true;
							if (!found){
								Dbutil.logger.info("Could not find a unique combination for item "+rs.getInt("itemid")+"("+Dbutil.readValueFromDB("select itemdescription from aiitems where itemid="+rs.getInt("itemid"), "itemdescription")+")");
							}
						}
						Dbutil.closeRs(rs2);
						Dbutil.closeRs(rs3);
					}
					reverseset=null;
					other+=(new java.util.Date().getTime()-thiscounted-overallstart);
					if (rows==100){
						Dbutil.logger.info("Time per select:"+(select*1000)/nselect);
						Dbutil.logger.info("Select:"+select);
						Dbutil.logger.info("Insert:"+insert);
						Dbutil.logger.info("Hanoy:"+hanoy);
						Dbutil.logger.info("Other:"+other);
						rows=0;
						select=0;
						nselect=0;
						insert=0;
						hanoy=0;
						other=0;
						Dbutil.executeQuery("load index into cache aiitempropsconsolidated;");

					}
					//Dbutil.logger.info("Hanoy:"+hanoy);
					//Dbutil.logger.info("Notunique:"+notunique);
					//Dbutil.logger.info("Sel:"+sel);
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			try {
				Dbutil.logger.info(rs.getString("propertyids"));
			} catch (SQLException e1) {
				Dbutil.logger.error("Problem: ",e1);

			}
		} finally {
			Dbutil.executeQuery("alter table aicombinations add  KEY `indexfts` (`fts`)");
			Dbutil.executeQuery("alter table aicombinations add   KEY `propertyids` (`propertyid1`,`propertyid2`,`propertyid3`,`propertyid4`) USING BTREE;");
			Dbutil.executeQuery("alter table aicombinations add   KEY `itemid` (`itemid`)");
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeRs(rs3);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
			Dbutil.closeConnection(con3);
			Dbutil.closeConnection(con4);
		}

	}

	public void fillRegionHierarchy(){
		Dbutil.logger.info("Starting fillRegionHierarchy");
		String query;
		boolean finished;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			if (true){
			query="delete from `wijn`.`"+regionhierarchytable+"`;";
			Dbutil.executeQuery(query);
			query="ALTER TABLE "+regionhierarchytable+" AUTO_INCREMENT = 10100;";
			Dbutil.executeQuery(query);
			query="insert into "+regionhierarchytable+" (region,parentid,lft,rgt,parent,active,lat,lon,accuracy,description) values ('All',10100,0,0,10100,1,0,0,0,'');";
			Dbutil.executeQuery(query, con);
			query="insert into "+regionhierarchytable+" (region,parentid,lft,rgt,parent,active,lat,lon,accuracy,shortregion,description) select distinct locale,0,0,0,appellation,1,0,0,0,appellation,'' from "+itemtable+" order by locale;";
			Dbutil.executeQuery(query, con);
			query="delete from "+regionhierarchytable+" where region like '%ö%';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent=replace(replace(region,region,concat(region,';')),concat(parent,';'),';');";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent=replace(parent,', ;','');";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent=replace(parent,' ;','');";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='All' where parent=';';";
			Dbutil.executeQuery(query, con);
			refineShortRegion();
			query="update "+regionhierarchytable+" set parent=concat(parent,', Alsace AOC') where region like 'France, Alsace, %, %Grand Cru AOC';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Gevrey-Chambertin' where region like 'France, %Chambertin%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Vosne-Roman�e' where region like 'France, %Roman�e%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Vosne-Roman�e' where region like 'France, %Grande Rue%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Vosne-Roman�e' where region like 'France, %La Tache%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Vosne-Roman�e' where region like 'France, %Roman�e%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Vosne-Roman�e' where region like 'France, %Richebourg%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Morey St. Denis' where region like 'France, Burgundy, %Clos%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Vougeot' where region like 'France, Burgundy, %Clos%Vougeot%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Flagey-Echezeaux' where region like 'France, Burgundy,%Echezeaux%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Chambolle-Musigny' where region like 'France, Burgundy,%Musigny%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Nuits, Chambolle-Musigny' where region like 'France, Burgundy,%Bonnes Mares%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Beaune, Chassagne-Montrachet' where region like 'France, Burgundy,%Criots-Batard%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Beaune, Puligny-Montrachet' where region like 'France, Burgundy,%Chevalier%Montrachet%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, C�te de Beaune, Puligny-Montrachet' where region like 'France, Burgundy,%Bienvenues%Montrachet%Grand Cru';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Burgundy, Bourgogne' where region like 'France, Burgundy, Bourgogne %';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Languedoc Roussillon, Languedoc' where region = 'France, Rh�ne, Southern Rh�ne, Costi�res-de-N�mes';";
			Dbutil.executeQuery(query, con);
			query="update "+regionhierarchytable+" set parent='France, Rh�ne, Southern Rh�ne, C�tes du Rh�ne' where region like '%C�tes du Rh�ne Villages %';";
			Dbutil.executeQuery(query, con);
			query="select kb1.id as id from "+regionhierarchytable+" kb1 join "+regionhierarchytable+" kb2 on (kb1.region=concat(kb2.region,' 1er Cru'));";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="update "+regionhierarchytable+" set parent=replace(region,' 1er Cru','') where id="+rs.getInt("id")+";";
				Dbutil.executeQuery(query, con);
			}
			}
			finished = false;
			while(!finished){
				rs=Dbutil.selectQuery("Select * from "+regionhierarchytable+" where parentid=0;", con);
				if (!rs.next()){
					finished=true;
				} else {
					rs.beforeFirst();	
					while (rs.next()){
						rs2=Dbutil.selectQuery("Select * from "+regionhierarchytable+" where region='"+Spider.SQLEscape(rs.getString("Parent"))+"';", con);
						if (rs2.next()){
							Dbutil.executeQuery("update "+regionhierarchytable+" set parentid="+rs2.getInt("id")+" where id="+rs.getInt("id"));
						} else{
							query="insert into "+regionhierarchytable+" (region,parentid,lft,rgt,parent,active,lat,lon,accuracy,description) values ('"+Spider.SQLEscape(rs.getString("parent"))+"',0,0,0,'"+Spider.SQLEscape(rs.getString("parent").replaceAll("^[^,]+$", "All").replaceAll(",[^,]+$", ""))+"',1,0,0,0,'');";
							Dbutil.executeQuery(query, con);
						}
						Dbutil.closeRs(rs2);
					}
					Dbutil.closeRs(rs);
				}
			}
			Dbutil.executeQuery("update "+regionhierarchytable+" set id=100,parent='',parentid=100 where id=10100;");
			updateRegionChild("All", 100);
			rebuildTree(100, 100);
			rs=Dbutil.selectQuery("select * from "+regionhierarchytable+" order by lft desc;", con);
			while (rs.next()){
				Dbutil.executeQuery("update "+regionhierarchytable+" set parentid="+rs.getInt("lft")+" where parentid="+rs.getInt("id"));
				Dbutil.executeQuery("update "+regionhierarchytable+" set id=lft where id="+rs.getInt("id"));
			}
			
			/*
			finished = false;
			int rowid=99;
			int counter=101;
			while (!finished){
				rs=Dbutil.selectQuery("Select * from "+regionhierarchytable+" where id>"+rowid+" and id<10000 order by id;", con);
				if (!rs.next()){
					finished=true;
				} else {
					rs.beforeFirst();
					while (rs.next()){
						rowid=rs.getInt("id");
						query="Select * from "+regionhierarchytable+" where parent='"+Spider.SQLEscape(rs.getString("Region"))+"' and id>10000;";
						rs2=Dbutil.selectQuery(query, con);
						while (rs2.next()){
							Dbutil.executeQuery("update "+regionhierarchytable+" set parentid="+rs.getInt("id")+",id="+counter+" where id="+rs2.getInt("id"));
							counter++;
						}
						Dbutil.closeRs(rs2);
					}
				}
			}
			*/
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}

	}

	public void updateRegionChild(String parent,int id){
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("select * from "+regionhierarchytable+" where id>10000 and parent='"+Spider.SQLEscape(parent)+"' order by region;", con);
			while (rs.next()){
				int newid=Dbutil.readIntValueFromDB("select max(id) as id from "+regionhierarchytable+" where id<10000","id")+1;
				Dbutil.executeQuery("update "+regionhierarchytable+" set id="+(newid)+", parentid="+id+" where id="+rs.getInt("id"), con);
				updateRegionChild(rs.getString("region"),newid);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	
	}
	
	public int rebuildTree(int parent, int left){

		int right=0;
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		try{
			// the right value of this node is the left value + 1
			right = left+1;

			// get all children of this node
			rs = Dbutil.selectQuery("SELECT * FROM "+regionhierarchytable+" WHERE id!="+parent+" and parentid="+parent+" order by id;",con);
			while (rs.next()) {
				// recursive execution of this function for each
				// child of this node
				// $right is the current right value, which is
				// incremented by the rebuild_tree function
				right = rebuildTree(rs.getInt("id"), right);
			}

			// we've got the left value, and now that we've processed
			// the children of this node we also know the right value
			//rs = Dbutil.selectQuery("SELECT count(*) as thecount FROM regions WHERE lft=0;",con);
			//rs.next();
			//if (rs.getInt("thecount")==3) {
			//	Dbutil.logger.info(parent);
			//}
			Dbutil.executeQuery("UPDATE "+regionhierarchytable+" SET lft="+left+", rgt="+right+" WHERE id="+parent+";");

			// return the right value of this node + 1
		} catch (Exception exc){
			Dbutil.logger.error("Problem while building tree of Regions",exc);
		} finally{
			Dbutil.closeConnection(con);
		}
		return right+1;
	}
	
	public static void refineShortRegion(){

		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		String region;
		try{
			rs = Dbutil.selectQuery("SELECT * FROM kbregionhierarchy where shortregion like '%Champagne%' or shortregion like '%Bourgogne%' or shortregion like '%Alsace%' group by shortregion having count(*)>1;",con);
			while (rs.next()) {
				rs2=Dbutil.selectQuery("Select * from kbregionhierarchy where shortregion='"+Spider.SQLEscape(rs.getString("shortregion"))+"';", con2);
				while (rs2.next()){
					region=rs2.getString("parent").replaceAll("^.*, ", "")+" "+rs2.getString("shortregion");
					region=region.replace(" AOC","");
					region=region.replace(" DOC","");
					region=region.replace(" IGT","");
					region=region.replaceAll("Alsace ","");
					region=region.replaceAll("France ","");
					region=region.replaceAll("Burgundy ","");
					Dbutil.executeQuery("update kbregionhierarchy set shortregion='"+Spider.SQLEscape(region)+"' where id="+rs2.getInt("id"));
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while building tree of Regions",exc);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
	}

	public void OldgenerateUniqueCombinationsNegativeMatch(){
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		ResultSet rs3=null;
		String[] props=null;
		String[] prop=new String[4];
		String negative;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			query="select * from aiitempropsconsolidated left join aicombinations on (aiitempropsconsolidated.itemid=aicombinations.itemid) having propertyid1 is null;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="select group_concat(propertyids) as negative from aiitempropsconsolidated where match (propertyids) against ('+"+rs.getString("propertyids").replace(",", " +")+"' in boolean mode) and itemid !="+rs.getInt("itemid")+";";
				rs2=Dbutil.selectQuery(query, con);
				if (rs2.next()){
					negative=rs2.getString("negative");
					HashMap<Integer,Integer> set=new HashMap<Integer,Integer>();
					props=rs.getString("propertyids").split(",");
					int t=0;
					for (String num:props){
						set.put(t,Integer.parseInt(num));
						negative=negative.replaceAll("(^|,)"+num+"($|,)", "");
						t++;
					}
					negative=(","+negative).replaceAll(",+", ",").replace(",", " +").trim();
					Dbutil.executeQuery("delete from aihanoy;");
					Dbutil.executeQuery("alter table aihanoy auto_increment=0;");
					for (int i=0;i<permutations.get(props.length-1).size();i++){
						String combi="";
						for (int j=0;j<permutations.get(props.length-1).get(i).size();j++){
							combi+="+"+props[permutations.get(props.length-1).get(i).get(j)]+" ";
						}
						Dbutil.executeQuery("insert into aihanoy (propertyids) values ('"+combi.trim()+" "+negative.replace("+","-")+"');", con);
					}
					boolean finished=false;
					while (!finished){
						rs2=Dbutil.selectQuery("select * from aihanoy order by id limit 1", con);
						if (rs2.next()){
							//Dbutil.logger.info("Testing "+rs2.getString("propertyids"));
							rs3=Dbutil.selectQuery("select distinct itemid from aiitempropsconsolidated where match (propertyids) against('"+rs2.getString("propertyids")+"' in boolean mode) limit 2;", con);
							if (rs3.next()){
								if (!rs3.next()){
									prop[0]=null;
									prop[1]=null;
									prop[2]=null;
									prop[3]=null;
									int c=0;
									for (String propje:rs2.getString("propertyids").substring(0,rs2.getString("propertyids").indexOf(" -")).replaceAll("-.*$", "").replace("+", "").split(" +")){
										if (c<4) prop[c]=propje;
										c++;
									}
									Dbutil.executeQuery("insert into aicombinations (tenant,itemid,fts,ftsexclude,propertyid1,propertyid2,propertyid3,propertyid4) values ("+tenant+","+rs.getInt("itemid")+",'"+rs2.getString("propertyids").substring(0,rs2.getString("propertyids").indexOf(" -"))+"','"+negative.replace("+", "-")+"',"+prop[0]+","+prop[1]+","+prop[2]+","+prop[3]+");", con);
									Dbutil.executeQuery("delete from aihanoy where propertyids like '%"+rs2.getString("propertyids").replace(" ", "%")+"%';", con);
								} else {
									Dbutil.executeQuery("delete from aihanoy where id="+rs2.getInt("id"), con);
								}
							} else {
								Dbutil.executeQuery("delete from aihanoy where id="+rs2.getInt("id"), con);
							}
						} else {
							finished=true;
						}
						Dbutil.closeRs(rs2);
						Dbutil.closeRs(rs3);
					}
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			try {
				Dbutil.logger.info(rs.getString("propertyids"));
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeRs(rs3);
			Dbutil.closeConnection(con);
		}

	}

	public void generatePermutations(){
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			for (int index=0;index<60;index++){
				HashMap<Integer,HashMap<Integer,Integer>> combinations=new HashMap<Integer,HashMap<Integer,Integer>>();
				HashMap<Integer,HashMap<Integer,Integer>> lastcombinations=new HashMap<Integer,HashMap<Integer,Integer>>();
				int n=index+1;
				for (int i=0;i<n;i++){
					HashMap<Integer,Integer> first=new HashMap<Integer,Integer>();
					first.put(0, i);
					lastcombinations.put(i,first);
				}
				combinations.putAll(lastcombinations);
				int global=n;
				int thisdepth=n;
				if (thisdepth>depth) thisdepth=5;
				if (index>fulllimit) thisdepth=2;
				for (int i=1;i<=thisdepth;i++){
					lastcombinations=getMap(lastcombinations, n,global-lastcombinations.size(),global);
					combinations.putAll(lastcombinations);
					global+=lastcombinations.size();
				}
				if (index>fulllimit){
					HashMap<Integer,Integer> signal=new HashMap<Integer,Integer>();
					signal.put(0,0);
					signal.put(1,0);
					signal.put(2,0);
					combinations.put(combinations.size(), signal);
				}
				permutations.put(index, combinations);
				lastcombinations=null;
				combinations=null;

			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}

	}

	public static HashMap<Integer,HashMap<Integer,Integer>> getMap(HashMap<Integer,HashMap<Integer,Integer>> prev, int max, int start,int end){
		HashMap<Integer,HashMap<Integer,Integer>> map=new HashMap<Integer,HashMap<Integer,Integer>>();
		int i=0;
		int j=0;
		int counter=end;
		try{ 
			for (i=start;i<end;i++){

				for (j=prev.get(i).get(prev.get(i).size()-1)+1;j<max;j++){
					HashMap<Integer,Integer> newmap=new HashMap<Integer,Integer>();
					newmap.put(prev.get(i).size(), j);
					newmap.putAll(prev.get(i));
					map.put(counter, newmap);
					counter++;
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem, i="+i+", j="+j);
			Dbutil.logger.error("prev="+prev);
			Dbutil.logger.error("Problem:",e);
		}
		return map;
	}

	public void generateLimitedPermutations(HashMap<Integer,HashMap<Integer,Integer>> lastcombinations, int index){
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		limitedpermutations=new HashMap<Integer,HashMap<Integer,Integer>>();
		try{
			int n=index+1;
			int end=lastcombinations.size();
			int global=0;
			int thisdepth=n;
			if (thisdepth>depth) thisdepth=depth;
			for (int i=3;i<=thisdepth;i++){
				lastcombinations=getLimitedMap(lastcombinations, n,end-lastcombinations.size(),end,global);
				limitedpermutations.putAll(lastcombinations);
				global+=lastcombinations.size();
				end=global;
			}
			lastcombinations=null;


		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}

	}


	public static HashMap<Integer,HashMap<Integer,Integer>> getLimitedMap(HashMap<Integer,HashMap<Integer,Integer>> prev, int max, int start,int end, int counter){
		HashMap<Integer,HashMap<Integer,Integer>> map=new HashMap<Integer,HashMap<Integer,Integer>>();
		int i=0;
		int j=0;
		try{ 
			for (i=start;i<end;i++){

				for (j=0;j<max;j++){
					if (!prev.get(i).containsValue(j)){
						TreeSet<Integer> set=new TreeSet<Integer>(prev.get(i).values());
						set.add(j);
						HashMap<Integer,Integer> newmap=new HashMap<Integer,Integer>();
						int f=0;
						Iterator<Integer> it=set.iterator();
						while (it.hasNext()){
							newmap.put(f, it.next());
							f++;
						}
						map.put(counter, newmap);
						counter++;
					}
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem, i="+i+", j="+j);
			Dbutil.logger.error("prev="+prev);
			Dbutil.logger.error("Problem:",e);
		}
		return map;
	}



	public void generateUniqueCombinationsOld(){
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("delete from aicombinations;");
			query="select row1.itemid,row1.propertyid,count(*) from aiitemproperties row1 where row1.tenant=1 group by row1.propertyid having count(*)=1;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.executeQuery("insert into aicombinations (tenant,itemid,fts) values ("+tenant+","+rs.getInt("itemid")+",'+"+rs.getInt("propertyid")+"');", con);
			}
			query="select props.itemid,prop1,prop2,com.propertyid1 from (select row1.itemid as itemid,row1.propertyid as prop1,row2.propertyid as prop2, count(*) from aiitemproperties row1 join  aiitemproperties row2 on (row1.tenant=row2.tenant and row1.itemid=row2.itemid and row1.propertyid<row2.propertyid) where row1.tenant=1 group by row1.propertyid having count(*)=1) props left join aicombinations com on (com.propertyid1=props.prop1 or com.propertyid1=props.prop2) having com.propertyid1 is null ;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				Dbutil.executeQuery("insert into aicombinations (tenant,itemid,fts) values ("+tenant+","+rs.getInt("itemid")+",'+"+rs.getInt("propertyid")+"');", con);
			}


		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}
	
	public static void fixKnownwine(int knownwineid){
		String query="delete from aiitems where id="+knownwineid;
		Dbutil.executeQuery(query);
		query="insert into aiitems (tenant,itemid,productgroupid,itemdescription,itemregexexcl,probability) select 1,id,1,wine,'',bottles from knownwines where id="+knownwineid;
		Dbutil.executeQuery(query);
		query="insert ignore into aiproperties (propertydescription,typeid,tenant) select producer,1,1 from knownwines where knownwineid="+knownwineid+";";
		Dbutil.executeQuery(query);
		
		String desc=Dbutil.readValueFromDB("select distinct(cuvee) as cuvee from knownwines where id="+knownwineid, "cuvee");
		desc=filterPunctuation(desc);
		String[] terms=desc.split(" +");
		for (String term:terms){
			term=filterTermCuvee(term);
			query="insert ignore into aiproperties(tenant,propertydescription,typeid) values ("+1+",'"+term+"',4);";
			Dbutil.executeQuery(query);
			
		}
		desc=Dbutil.readValueFromDB("select distinct(vineyard) as vineyard from knownwines where id="+knownwineid, "vineyard");
		desc=filterPunctuation(desc);
		terms=desc.split(" +");
		for (String term:terms){
			term=filterTermCuvee(term);
			query="insert ignore into aiproperties(tenant,propertydescription,typeid) values ("+1+",'"+term+"',4);";
			Dbutil.executeQuery(query);
		}
		
	}
	
	public void refreshItems(){
		fillAiitems();
		fillProperties();
		filterColorGrapetermsinCuvee();
		generateRecognizerFullText();
		manualRecognizer();
		fillAiitemproperties();
		fillPropsConsolidated();
	}
	

	public static void main(String[] args){
		//Wijnzoeker wijnzoeker=new Wijnzoeker();
		
		//Aitools aitools=new Aitools(1);
		//aitools.fillRegionHierarchy();
		//aitools.refreshItems();
		/* obsolete
		//aitools.generatePermutations();
		//aitools.generateUniqueCombinations(false);
		//aitools.generateUniqueCombinations(true);
		 */
	}


	public static void test(){
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		rs=Dbutil.selectQuery("select * from aiproperties order by propertydescription desc limit 40;", con);
		try {
			while (rs.next()) {
				String cuvee = rs.getString("propertydescription");
				Dbutil.logger.info(Character.codePointAt(cuvee, 0)+": "+cuvee + " = " + filterPunctuation(cuvee));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
	}

}
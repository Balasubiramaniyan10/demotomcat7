package com.freewinesearcher.batch.sitescrapers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.apache.log4j.Level;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.ai.Aitools;
import com.searchasaservice.ai.ParallelRecognizer;
import com.searchasaservice.ai.Recognizer;

public class Matchers {

	public static void saveMatch(String kbname, String ctname){
		if (kbname!=null&&ctname!=null){
			Dbutil.executeQuery("Update ctproducermatcher set ctname='"+Spider.SQLEscape(ctname)+"' where kbname='"+Spider.SQLEscape(kbname)+"';");
		}
	}

	public static void saveWineMatch(String kbid, String ctid){
		if (kbid!=null&&ctid!=null){
			if (kbid.equals(ctid)){
				Dbutil.logger.info("Same wine id");
			} else {
				Dbutil.executeQuery("Update ctmatcher set ctid="+Spider.SQLEscape(ctid)+" where kwid="+Spider.SQLEscape(kbid)+";");
			}
		}
	}


	public static String getProducerMatcher(){
		StringBuffer sb=new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from ctproducermatcher  join kbproducers on (kbname=name) where (address is not  null or lat!=0 or website is not null) and ctname='' order by rand() limit 1;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				String name=rs.getString("kbname");
				sb.append("<h1>"+name+"</h1>");
				sb.append("<a href='?kbname="+Webroutines.URLEncodeUTF8(name)+"&amp;ctname=not+found'>(skip)</a><br/><br/>");
				Dbutil.closeRs(rs);
				String ft="";
				String[] terms=name.split("[ ']");
				for (String term:terms){
					if (term.length()>0){
						if (term.length()>=2&&!term.contains("*")&&!term.contains("_")){
							ft+=" +"+term;
						} 
					}
				}

				query="select * from ctproducers where match(name) against ('"+ft+"') order by name like '%"+Spider.SQLEscape(name).replaceAll(" ", "%")+"%' desc, match(name) against ('"+ft+"') desc limit 500;";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					sb.append("<a href='?kbname="+Webroutines.URLEncodeUTF8(name)+"&amp;ctname="+Webroutines.URLEncodeUTF8(rs.getString("name"))+"'>"+rs.getString("name")+"</a><br/>"); 

				}


			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}


		return sb.toString();
	}


	public static String getWineMatcher(boolean emptylabels){
		StringBuffer sb=new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from ctmatcher "+(emptylabels?" join labelstatus on (ctmatcher.kwid=labelstatus.id) ":" ")+"join knownwines on (knownwines.id=ctmatcher.kwid) where ctid=0 order by rand() limit 1;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				String wine=rs.getString("wine");
				if (!wine.equals(Spider.SQLEscape(wine))){
					Dbutil.logger.info(wine);
				}
				int knownwineid=rs.getInt("knownwines.id");
				sb.append("<h1>"+wine+"</h1>");
				String label="";
				if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+knownwineid+".jpg").exists()){
					label="<img style='float:right;max-width:250px;max-height:250px;' src='"+Configuration.staticprefix+"/labels/"+knownwineid+".jpg' alt=\""+Knownwines.getKnownWineName(knownwineid)+" label\" />";
				} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+knownwineid+".gif").exists()){
					label="<img style='float:right;max-width:250px;max-height:250px;' src='"+Configuration.staticprefix+"/labels/"+knownwineid+".gif' alt='"+Knownwines.getKnownWineName(knownwineid).replace("'","&apos;")+" label' />";
				}
				sb.append(label);
				sb.append("<a href='?kwid="+rs.getString("knownwines.id")+"&amp;ctid=-1'>(skip)</a><br/><br/>");
				Dbutil.closeRs(rs);
				String ft="";
				String[] terms=wine.split("[ ']");
				for (String term:terms){
					if (term.length()>0){
						if (term.length()>=2&&!term.contains("*")&&!term.contains("_")){
							ft+=" +"+term;
						} 
					}
				}

				query="select * from ctknownwines where match(wine) against ('"+ft+"') order by wine like '%"+Spider.SQLEscape(wine).replaceAll(" ", "%")+"%' desc, match(wine) against ('"+ft+"') desc limit 500;";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					sb.append("<a href='?kwid="+knownwineid+"&amp;ctid="+rs.getString("ctknownwines.id")+"'>"+rs.getString("wine")+" ("+rs.getString("appellation")+","+rs.getString("grapes")+")</a><br/>"); 

				}


			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}


		return sb.toString();
	}



	public static void producerMatcher(){
		StringBuffer sb=new StringBuffer();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			boolean ready=false;
			while (!ready){
				query = "select * from ctproducermatcher where ctname='' order by kbname limit 1;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					String name=rs.getString("kbname");
					//Dbutil.logger.info(name);

					Dbutil.closeRs(rs);
					String ft="";
					String[] terms=name.split(" ");
					for (String term:terms){
						if (term.length()>0){
							if (term.length()>=2&&!term.contains("*")&&!term.contains("_")){
								ft+=" +"+Spider.SQLEscape(term);
							} 
						}
					}

					query="select name,concat('',match(name) against ('"+ft+"'),'') as score from ctproducers where match(name) against ('"+ft+"')>6 order by name like '%"+Spider.SQLEscape(name.replaceAll(" ", "%"))+"%' desc, match(name) against ('"+ft+"') desc limit 500;";
					rs = Dbutil.selectQuery(rs, query, con);
					sb=new StringBuffer(); 
					int j=0;
					double first=0,second=0;
					boolean goodfit=false;
					while (rs.next()) {
						//Dbutil.logger.info(rs.getString("name")+" "+rs.getDouble("score"));
						if (j==0) first=rs.getDouble("score");
						if (j==1) second=rs.getDouble("score");
						sb.append(",'"+Spider.SQLEscape(rs.getString("name"))+"'"); 
						j++;
					}
					if (first!=0&&second/first<0.6) goodfit=true;
					Dbutil.closeRs(rs);
					if (sb.length()>0) {
						String prod=sb.toString().substring(1);
						sb=new StringBuffer();
						query="select distinct wine,appellation from knownwines where producer='"+Spider.SQLEscape(name)+"';" ;
						rs = Dbutil.selectQuery(rs, query, con);
						int n=0;
						int o;
						while (rs.next()) {
							if (!"".equals(Spider.SQLEscape(rs.getString("wine")).replace(name, ""))){
								sb.append(" or (wine like '%"+Spider.SQLEscape(rs.getString("wine")).replace(name, "")+"' and appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"')");
								n++;
							}
						}
						if (n>0){
							Dbutil.closeRs(rs);
							query="Select producer,count(distinct(concat(wine,appellation))) as thecount from ctknownwines where producer in ("+prod+") and ("+sb.toString().substring(3)+") group by producer order by count(*) desc;";
							rs = Dbutil.selectQuery(rs, query, con);
							if (rs.next()) {
								String p=rs.getString("producer");
								o=rs.getInt("thecount");
								if (o==n&&(!rs.next()||rs.getInt("thecount")<n||goodfit)){
									// match
									//Dbutil.logger.info("Match: "+name+"="+p);
									saveMatch(name,p);

								} else {
									//Dbutil.logger.info("No match: "+name+"!="+p+". "+o+" wines found from "+n);

									saveMatch(name,"not found");

								}
							} else {
								saveMatch(name,"not found");
							}
						} else {
							saveMatch(name,"not found");
						}

					}else {
						saveMatch(name,"not found");
					}
				} else {
					ready=true;
					Dbutil.executeQuery("Update ctproducermatcher set ctname='' where ctname='not found';");
				}

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}



	}

	public static void processNewKnownwines(){
		// Step 0: copy knownwines from PRD to DEV
		// Step 1: Disable wines that previously were disabled
		Dbutil.executeQuery("update knownwines kw  join ctknownwines ct on (kw.wine=ct.wine and kw.appellation=ct.appellation and kw.producer=ct.producer and kw.grapes=ct.grapes) set ct.disabled=1 where kw.disabled=1;");
		String query;
		String cttable="ctknownwines";
		String ctproducerstable="ctproducers";
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		Connection con2 = Dbutil.openNewConnection();
		try {
			if (false){
				// Create new table that determines if an old wine was correctly found and processed/switched.
				Dbutil.executeQuery("drop table if exists ctmatcher;");
				Dbutil.executeQuery("create table ctmatcher as select id as kwid,0 as ctid from knownwines;");
				Dbutil.executeQuery("ALTER TABLE `wijn`.`ctmatcher` ADD PRIMARY KEY (`kwid`);");
				Dbutil.executeQuery("ALTER TABLE `wijn`.`ctknownwines` ADD FULLTEXT INDEX `wineft` (`wine` ASC) ;");



				// Step 2: Find wines with samename in old list, find corresponding wine in new list
				query = "select ct.id as ctid,kw.id as kwid from "+cttable+" ct join knownwines kw on (ct.wine=kw.wine and ct.producer=kw.producer and ct.appellation=kw.appellation and ct.grapes=kw.grapes and ct.type=kw.type) where kw.samename=1 group by ct.id having count(*)=1";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					query="update ctmatcher set ctid="+rs.getInt("ctid")+" where kwid="+rs.getInt("kwid")+";";
					Dbutil.executeQuery(query,con2);
				}
				Dbutil.closeRs(rs);
				// Step : Find wines with samename=0 in old list, find exactly same wine in new list and if only 1 is found, switch numbers (necessary because labels are matched by knownwineid)
				query = "select ct.id as ctid,kw.id as kwid from "+cttable+" ct join knownwines kw on (ct.wine=kw.wine and ct.producer=kw.producer and ct.appellation=kw.appellation and ct.grapes=kw.grapes and ct.type=kw.type) where kw.samename=0 group by ct.id having count(*)=1";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					query="update ctmatcher set ctid="+rs.getInt("ctid")+" where kwid="+rs.getInt("kwid")+";";
					Dbutil.executeQuery(query,con2);
				}
				Dbutil.closeRs(rs);
				// Step : Find wines with samename=0 in old list, find wine with same name in new list and if only 1 is found, switch numbers (necessary because labels are matched by knownwineid)
				query = "select ct.id as ctid,kw.id as kwid from "+cttable+" ct join knownwines kw on (ct.wine=kw.wine) where kw.samename=0 group by ct.id having count(*)=1";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					query="update ctmatcher set ctid="+rs.getInt("ctid")+" where kwid="+rs.getInt("kwid")+";";
					Dbutil.executeQuery(query,con2);
				}
				Dbutil.closeRs(rs);
				Dbutil.executeQuery("ALTER TABLE `wijn`.`ctknownwines` ADD INDEX `producer` USING HASH(`producer`)");
				// Create ctproducers
				Dbutil.executeQuery("drop table if exists ctproducers;");
				Dbutil.executeQuery("CREATE TABLE  `wijn`.`"+ctproducerstable+"` (  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,  `name` varchar(255) DEFAULT NULL,  `address` varchar(255) DEFAULT NULL,  `email` varchar(255) DEFAULT NULL,  `telephone` varchar(255) DEFAULT NULL,  `visiting` varchar(255) DEFAULT NULL,  `countrycode` varchar(2) DEFAULT NULL,  `lat` double DEFAULT NULL,  `lon` double DEFAULT NULL,  `accuracy` int(10) unsigned DEFAULT '0',  `phonelastdigits` varchar(8) DEFAULT '0',  `website` varchar(255) DEFAULT '',  `sourceid` int(10) unsigned DEFAULT '0',  `description` text,  `edithashcode` varchar(45) NOT NULL,  `priority` int(10) NOT NULL,  `storelocatormail` datetime DEFAULT NULL,  `demopage` datetime DEFAULT NULL,  `wines` int(10) unsigned NOT NULL DEFAULT '0',  `country` varchar(45) NOT NULL,  `twitter` varchar(45) NOT NULL DEFAULT '',  `score` int(10) unsigned NOT NULL DEFAULT '0',  PRIMARY KEY (`id`),  KEY `name` (`name`),  KEY `unindexed` (`address`,`priority`),  FULLTEXT KEY `name_ft` (`name`)) ENGINE=MyISAM  DEFAULT CHARSET=utf8;");
				Dbutil.executeQuery("insert into "+ctproducerstable+" (name,description,edithashcode,priority,country) select distinct(producer),'','notset',0,'' from "+cttable+" order by producer;");
				Dbutil.executeQuery("drop table if exists ctproducermatcher;");
				Dbutil.executeQuery("create table ctproducermatcher as select distinct(name) as kbname,name as ctname from kbproducers order by name;");
				Dbutil.executeQuery("ALTER TABLE `wijn`.`ctproducermatcher` MODIFY COLUMN `kbname` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL, ADD PRIMARY KEY (`kbname`);");
				Dbutil.executeQuery("update  ctproducermatcher set ctname='' ;");
				// Step: Find data from kbproducer that matches ctproducer
				query = "select kb.name as kbname, ct.name as ctname from kbproducers kb join "+ctproducerstable+" ct on (kb.name=ct.name)";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					query="update ctproducermatcher set ctname='"+Spider.SQLEscape(rs.getString("ctname"))+"' where kbname='"+Spider.SQLEscape(rs.getString("kbname"))+"';";
					Dbutil.executeQuery(query,con2);
				}
				Dbutil.closeRs(rs);
			}
			//Step: find similar ctproducers and compare their wines with what we expect
			producerMatcher();
			// Step: Find data from kbproducer that cannot be found in ctproducer, find closest matches, if match found copy data and store coresponding producers in table
			// Step: for wines not found, find match by translating producer
			// Step: for wines not found, select best match by hand (jsp, match() fulltext, order by match, min(length).
			// If match found by hand and producer name is different, process the rest automatically with the new producer name
			// Store new producer name in separate table to match kbproducers
			// Check by hand if all old wines have been found and processed
			// Step: manual check for all labels of wine with samename=1
			// Step 4: Copy data from corresponding producer in kbproducer
			// Grapes
			// Regionhierarchy
			// Create new table for new knownwine recognition
			// Analyze wines
			// Analyze ratedwines
			// Switch

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}


	}

	public static void buildNewWineRecognition(){
		Dbutil.executeQuery("ALTER TABLE `wijn`.`ctknownwines` ADD COLUMN `kwid` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `winerynote`;");
		Aitools aitools=new Aitools(1);
		aitools.fillAiitems("ctknownwines");
		//aitools.fillRegionHierarchy();
		aitools.fillProperties();
		aitools.filterColorGrapetermsinCuvee();
		aitools.generateRecognizerFullText();
		aitools.manualRecognizer();
		aitools.fillAiitemproperties();
		aitools.fillPropsConsolidated();
	}

	public static void recognizeKnownWines(){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Wijnzoeker.debug=true;
		Dbutil.logger.setLevel((Level) Level.DEBUG);
		Recognizer rec=new Recognizer("wine","kwid","id","knownwines",1,false);
		Dbutil.logger.info("Building new wine recognition");
		Aitools aitools=new Aitools(1);
		aitools.itemtable="ctknownwines";
		aitools.regionhierarchytable="ctregionhierarchy";
		rec.regionhierarchytable=aitools.regionhierarchytable;
		
		//aitools.fillRegionHierarchy();
		if (false){
			//aitools.fillAiitems("ctknownwines");
			aitools.fillProperties();
			aitools.filterColorGrapetermsinCuvee();
			aitools.generateRecognizerFullText();
			aitools.manualRecognizer();

			aitools.fillAiitemproperties();
			aitools.fillPropsConsolidated();
			Dbutil.logger.info("Finished building new wine recognition");
		}
		Dbutil.logger.info("Starting job to recognize wines");
		if (true){
			Dbutil.executeQuery("delete from airecords where tenant=1;");
			Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select 1,id,concat(wine,' ',appellation,' ',type,' ',grapes),0 from knownwines join ctmatcher on (kwid=knownwines.id) where ctid=0 and kwid=295 order by id;");
		}
		rec.skipmanualknownwineid=true;
		rec.refreshAll=false;
		//rec.history=history;
		//rec.fillAiRecords();
		if (true){
		rec.getProperties(0);
		rec.getPropertiesConsolidated();
		}
		rec.restart();
		rec.restart=false;
		//rec.measureperformance=false;
		rec.debug=true;
		rec.info=true;
		
		ParallelRecognizer pr=new ParallelRecognizer(rec,Recognizer.desiredparallelprocesses);

		pr.startAnalyses();
		if (!pr.problem){
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			Connection con2 = Dbutil.openNewConnection();
			try {

				rs=Dbutil.selectQuery(rs,"select * from airesults group by recordid having count(*)=1;",con);

				while (rs.next()){
					if (rs.getInt("itemid")==rs.getInt("recordid")){
						Dbutil.logger.info(" Same number" );
					}
					Dbutil.executeQuery("update LOW_PRIORITY ctmatcher set ctid="+rs.getInt("itemid")+" where kwid="+rs.getInt("recordid")+";", con2);
				}

			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
				Dbutil.closeConnection(con2);
			}

			//Dbutil.renewTable("materializedadvice");
			Dbutil.executeQuery("update wines set manualknownwineid=0 where manualknownwineid=-2;");
			Dbutil.logger.info("Finished job to recognize wines");
		} else {
			Dbutil.logger.error("Problem while recognizing knownwines");
		}
	}




	public static void processlabelstatus(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		Connection con2 = Dbutil.openNewConnection();
		try {
			query = "select * from labelstatus join ctmatcher on (labelstatus.id=kwid and ctid!=0)  where ok=0";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				if (rs.getInt("ctid")!=0){
					Dbutil.executeQuery("update labelstatus set ok=1 where id="+rs.getInt("labelstatus.id"), con2);
				}


			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
	}

	public static void main(String[] args){

		//processNewKnownwines();
		//buildNewWineRecognition();
		recognizeKnownWines();
		//producerMatcher();
		//processlabelstatus();

	}
}

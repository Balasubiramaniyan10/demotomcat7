package com.searchasaservice.ai;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.online.Searchdata;
import com.freewinesearcher.online.Shop;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.Webroutines;
import com.freewinesearcher.batch.TableScraper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Jasper
 *
 */
/**
 * @author Jasper
 *
 */
public class Recognizer  implements Runnable { 
	public static int desiredparallelprocesses=Configuration.parallelrecognitionthreads;
	public boolean debug=false;
	public boolean info=false;
	public boolean restart=false;
	private int tenant;
	private String table;
	private String lookupfield;
	private String resultfield;
	private String idfield;
	public String regionhierarchytable="kbregionhierarchy";
	private int recordid;
	private TreeSet<Integer> recordprops;
	private TreeSet<Integer> allrecordprops;
	private TreeSet<Integer> limitedrecordprops;
	private TreeSet<Integer> regions;
	private HashMap<Integer,Long> executiontime=new HashMap<Integer, Long>();
	private HashMap<Integer,Integer> numberofexecutions=new HashMap<Integer, Integer>();
	private HashMap<Integer,String> stepdescription=new HashMap<Integer, String>();
	private HashMap<Integer,TreeSet<Integer>> itemprops;
	private HashMap<Integer,TreeSet<Integer>> limiteditemprops;
	private HashMap<Integer,TreeSet<Integer>> lastitemprops;
	private HashMap<Integer,TreeSet<Integer>> propertymap;
	private HashMap<Integer,TreeSet<Integer>> grapemap;
	private HashMap<Integer,Integer> itemregionmap;
	private TreeSet<Integer> color;
	private TreeSet<Integer> grapes;
	private int step;
	private String itemdescription;
	private Integer[] results;
	private boolean measureperformance=false;
	private FullTextItems ftitems;
	public boolean refreshAll=true;
	Connection temptablecon=Dbutil.openNewConnection();
	boolean temporary=false;
	String idswith2letters="";
	public boolean skipmanualknownwineid=true; // By default, do not analyze items which manualknownwineid is set to any other value than 0
	int history=0; // History>0 means go back that many days in time and update only records younger than that time
	int blendid=Dbutil.readIntValueFromDB("select * from aiproperties where typeid=8 and propertydescription='Blend';", "propertyid");
	int refreshshopcache=0;
	public Recognizer(String lookupfield, String resultfield, String idfield,String table, int tenant,boolean temporary) {
		super();
		this.lookupfield = lookupfield;  //Description
		this.resultfield=resultfield;  // Store the result in this column
		this.idfield=idfield;  // column that identifies unique entries
		this.table = table; // Table to analyze
		this.tenant = tenant;
		this.temporary=temporary;
		if (temporary) setupTempTables();
		
	}

	public Recognizer clone(){
		Recognizer newrec=new Recognizer(lookupfield,resultfield,idfield,table,tenant,false);
		newrec.temporary=temporary;
		newrec.restart=false;
		newrec.skipmanualknownwineid=skipmanualknownwineid;
		newrec.debug=debug;
		newrec.regionhierarchytable=regionhierarchytable;
		
		newrec.info=info;
		newrec.measureperformance=measureperformance;
		try {
			newrec.temptablecon.close();
		} catch (SQLException e) {

		}
		newrec.temptablecon=temptablecon;

		return newrec;
	}

	public void setupTempTables(){
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`airesults`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`ainocandidates`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`airecords`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`aipropertymatches`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`aipropertymatchesconsolidated`;",temptablecon);
		Dbutil.executeQuery("CREATE TEMPORARY TABLE  `wijn`.`airesults` (`recordid` int(10) unsigned NOT NULL,`itemid` int(10) unsigned NOT NULL, `tenant` int(10) unsigned NOT NULL, PRIMARY KEY (`recordid`,`itemid`,`tenant`) USING BTREE) ENGINE=MyISAM DEFAULT CHARSET=utf8;",temptablecon);
		Dbutil.executeQuery("CREATE TEMPORARY TABLE  `wijn`.`ainocandidates` ( `recordid` int(10) unsigned NOT NULL AUTO_INCREMENT, PRIMARY KEY (`recordid`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;",temptablecon);
		Dbutil.executeQuery("CREATE TEMPORARY TABLE  `wijn`.`airecords` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`description` varchar(255) NOT NULL,`itemid` int(10) unsigned NOT NULL DEFAULT '0',`tenant` int(10) unsigned NOT NULL,PRIMARY KEY (`tenant`,`id`) USING BTREE,FULLTEXT KEY `ftsname` (`description`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;",temptablecon);
		Dbutil.executeQuery("CREATE TEMPORARY TABLE  `wijn`.`aipropertymatches` (`propertymatchid` int(10) unsigned NOT NULL AUTO_INCREMENT,`recordid` int(10) unsigned NOT NULL,`propertyid` int(10) unsigned NOT NULL,`typeid` int(10) unsigned NOT NULL,`tenant` int(10) unsigned NOT NULL,PRIMARY KEY (`tenant`,`propertymatchid`) USING BTREE,KEY `index_2` (`tenant`,`typeid`,`recordid`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;",temptablecon);
		Dbutil.executeQuery("CREATE TEMPORARY TABLE  `wijn`.`aipropertymatchesconsolidated` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`recordid` int(10) unsigned NOT NULL,`propertyids` varchar(705) DEFAULT NULL,`type1id` varchar(705) DEFAULT NULL,`type2id` varchar(705) DEFAULT NULL,`type3id` varchar(705) DEFAULT NULL,`type4id` varchar(705) DEFAULT NULL,`type5id` varchar(705) DEFAULT NULL,`type6id` varchar(705) DEFAULT NULL,`type7id` varchar(705) DEFAULT NULL,`type8id` varchar(705) DEFAULT NULL,`tenant` int(10) unsigned NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `recordid` (`recordid`)) ENGINE=MyISAM DEFAULT CHARSET=latin1;",temptablecon);
	}

	public void destroy(){
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`airesults`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`ainocandidates`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`airecords`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`aipropertymatches`;",temptablecon);
		Dbutil.executeQuery("drop TEMPORARY TABLE if exists `wijn`.`aipropertymatchesconsolidated`;",temptablecon);
		Dbutil.closeConnection(temptablecon);
	}

	/**
	 * @throws Exception 
	 * 
	 * 	 
	 * */
	public void getMatches(int itemtodebug, int threads,int thisthread) throws Exception{
		String parallelclause="";
		if (threads>1) parallelclause=" and mod(recordid,"+threads+")="+thisthread; 
		if (threads>1) Dbutil.logger.warn("starting getMatches with "+threads+" thread(s), this is thread "+thisthread+".");
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		long start=0;
		int itemcriteriatime=0;
		int itempropstime=0;
		int insert=0;
		int whichitem=0;
		int whichitemeromheen=0;
		int getrecordprops=0;
		Integer[] item;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		java.sql.Connection con3=Dbutil.openNewConnection();
		String properties;
		boolean moretodo=true;
		int rowsperselect=100;
		int offset=-1;
		boolean logged=false;
		boolean flag;


		try{

			if (restart){
				restart();
			} else {
				//offset=Dbutil.readIntValueFromDB("select max(recordid) as id from airesults;", "id");
			}
			Dbutil.executeQuery("SET SESSION group_concat_max_len = 1000000;", con2);
			Dbutil.executeQuery("load index into cache aiitempropsconsolidated;", con2);
			Dbutil.executeQuery("load index into cache knownwines;", con2);
			Dbutil.executeQuery("load index into cache aiitems;", con2);
			int record=0;
			while (moretodo){
				if (itemtodebug>1){
					query="select * from aipropertymatchesconsolidated where tenant="+tenant+(itemtodebug>0?" and recordid="+itemtodebug:"")+";";
					moretodo=false;
				} else {
					query="select * from aipropertymatchesconsolidated where tenant="+tenant+" and recordid > "+offset+parallelclause+" order by recordid limit "+rowsperselect+";";
				}
				rs=Dbutil.selectQuery(rs,query, temptablecon);
				if (!rs.next()){
					moretodo=false;
				} else {
					rs.last();
					offset=rs.getInt("recordid");
					rs.beforeFirst();
				}

				while (rs.next()){
					if (measureperformance) {
						record++;

						if (record%100==1){
							for (int t:stepdescription.keySet()){
								Dbutil.logger.error("Step "+t+" ("+stepdescription.get(t)+": "+numberofexecutions.get(t)+" calls in "+(executiontime.get(t))+" milliseconds.");
							}
							Dbutil.logger.error("Total whichitem: "+whichitem+" ms.");
							Dbutil.logger.error("Record props: "+getrecordprops+" ms.");
							Dbutil.logger.error("Item criteria: "+itemcriteriatime+" ms.");
							Dbutil.logger.error("Item properties: "+itempropstime+" ms.");
							Dbutil.logger.error("Insert result: "+insert+" ms.");

						}
					}
					if (measureperformance) start=new java.util.Date().getTime();
					logged=false;
					recordprops=getTreeSet(rs.getString("propertyids"));
					grapes=getTreeSet(rs.getString("type8id"));
					color=getTreeSet(rs.getString("type6id"));
					//query="select * from "+table+" where "+idfield+"="+rs.getInt("recordid");
					query="select * from airecords where id="+rs.getInt("recordid");
					rs2=Dbutil.selectQuery(rs2,query,temptablecon);
					if (rs2.next()){
						itemdescription=rs.getInt("recordid")+" "+rs2.getString("description");
						recordid=rs.getInt("recordid");
						ftitems=FullTextScore(rs2.getString("description"), 0, 3);
					
					Dbutil.closeRs(rs2);
					query="";
					//propertyids="";
					properties="";
					if (measureperformance) getrecordprops+=(new java.util.Date().getTime()-start);
					if (measureperformance) start=new java.util.Date().getTime();
					properties=getProperties(rs, true);
					if (properties.length()>4){
						flag=false; //flag if there are very many items matching so no distinguishing properties
						if (!properties.contains("and type1id")&&!properties.contains("type2id")){
							flag=true;
						} else {
							query="select count(*) as thecount from aiitempropsconsolidated where "+properties.substring(5)+";";
							rs2=Dbutil.selectQuery(rs2,query, con2);
							if (rs2.next()&&rs2.getInt("thecount")>100){
								flag=true;
							}
						}
						if (flag){
							Dbutil.closeRs(rs2);
							if (!properties.contains("and type1id")&&!properties.contains("type2id")){
								query="select group_concat(itemid) as list from (select itemid,match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') as score from aiitems where tenant="+tenant+" and match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') > 6 order by score desc limit 100) asd;";
							} else {
								query="select group_concat(itemid) as items from aiitempropsconsolidated where "+properties.substring(5)+";";
								rs2=Dbutil.selectQuery(rs2,query, con2);
								if (rs2.next()){
									if (rs2.getString("items").length()<20000){
									query="select group_concat(itemid) as list from (select itemid,match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') as score from aiitems where tenant="+tenant+" and itemid in ("+rs2.getString("items")+") and match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') > 6 order by score desc limit 100) asd;";
									} else {
										//Dbutil.logger.info("Too many itemids found for record '"+itemdescription+"', query "+query);
										query="select group_concat(itemid) as list from (select itemid,match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') as score from aiitems where tenant="+tenant+" and match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') > 6 order by score desc limit 100) asd;";
									}
									Dbutil.closeRs(rs2);
								}
							}
							rs2=Dbutil.selectQuery(rs2,query,con2);
							if (rs2.next()){
								query="select *,concat(type1id,',',type2id) as other from aiitempropsconsolidated where "+properties.substring(5)+" and itemid in ("+rs2.getString("list")+") ";
							}
							Dbutil.closeRs(rs2);
						} else 	if (rs2.getInt("thecount")==0){
							properties=getProperties(rs, false);
							query="select count(*) as thecount from aiitempropsconsolidated where "+properties.substring(5)+";";
							Dbutil.closeRs(rs2);
							rs2=Dbutil.selectQuery(rs2,query, con2);
							if (rs2.next()&&rs2.getInt("thecount")>100){
								Dbutil.closeRs(rs2);
								rs2=Dbutil.selectQuery(rs2,"select group_concat(itemid) as list from (select itemid,match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') as score from aiitems where tenant="+tenant+" and match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') > 9 order by score desc limit 100) asd;",con2);
								if (rs2.next()){
									query="select *,concat(type1id,',',type2id) as other from aiitempropsconsolidated where "+properties.substring(5)+" and itemid in ("+rs2.getString("list")+") ";
								}
								Dbutil.closeRs(rs2);
							} else 	if (rs2.getInt("thecount")==0){
								Dbutil.closeRs(rs2);
								query="";
								//Dbutil.logger.warn("NOK: No candidates found for wine "+rs.getInt("recordid")+": "+Dbutil.readValueFromDB("select * from aiwines where wineid="+rs.getInt("recordid"), "name")+", using properties "+Dbutil.readValueFromDB("select group_concat(concat(propertydescription,'(',typeid,')')) as descr from aiproperties where propertyid in ("+propertyids.substring(1)+") order by typeid;","descr"));
								if (!temporary){
									Dbutil.executeQuery("update aidefects set faults=(faults+1) where tenant="+tenant+" and description='No candidates found';", con3);
									Dbutil.executeQuery("insert ignore into ainocandidates select "+rs.getInt("recordid")+";", con3);
								}
								logged=true;
								/*
								query=query.replaceAll("type1id in .*? and ", "");
								rs2=Dbutil.selectQuery(rs2,query, con2);
								rs2.next();
								if (rs2.getInt("thecount")>0){	
									Dbutil.logger.warn("But found wines without producers:");
									Iterator<Integer> tempi=getTreeSet(rs.getString("type1id")).iterator();
									while (tempi.hasNext()){
										int id=tempi.next();
										Dbutil.logger.warn(id+": "+Dbutil.readValueFromDB("select * from aiproperties where propertyid="+id, "propertydescription"));
									}
								}
								 */
							} else {
								query="select *,concat(type1id,',',type2id) as other from aiitempropsconsolidated where "+properties.substring(5)+";";
							}

						} else {
							query="select *,concat(type1id,',',type2id) as other from aiitempropsconsolidated where "+properties.substring(5)+";";
						}
					} else {
						rs2=Dbutil.selectQuery(rs2,"select group_concat(itemid) as list from (select itemid,match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') as score from aiitems where tenant="+tenant+" and match (itemdescription) against('+"+Aitools.filterPunctuation(itemdescription).trim().replaceAll("\\s+", " +")+"') > 9 order by score desc limit 100) lkj;",con2);
						if (rs2.next()){
							query="select *,concat(type1id,',',type2id) as other from aiitempropsconsolidated where itemid in ("+rs2.getString("list")+") ";
						}
						Dbutil.closeRs(rs2);					
					}
					if (measureperformance) itemcriteriatime+=(new java.util.Date().getTime()-start);
					if (measureperformance) start=new java.util.Date().getTime();

					if (!"".equals(query)){
						// get item info
						itemprops=new HashMap<Integer,TreeSet<Integer>>();
						limiteditemprops=new HashMap<Integer,TreeSet<Integer>>();
						propertymap=new HashMap<Integer,TreeSet<Integer>>();
						grapemap=new HashMap<Integer,TreeSet<Integer>>();
						itemregionmap=new HashMap<Integer,Integer>();
						rs2=Dbutil.selectQuery(rs2,query, con2);
						while (rs2.next()){
							grapemap.put(rs2.getInt("itemid"), getTreeSet(rs2.getString("type8ids")));
							limiteditemprops.put(rs2.getInt("itemid"), getTreeSet(rs2.getString("type3id")));
							itemprops.put(rs2.getInt("itemid"), getTreeSet(rs2.getString("propertyids")));
							propertymap.put(rs2.getInt("itemid"), getTreeSet(rs2.getString("other")));
							itemregionmap.put(rs2.getInt("itemid"), rs2.getInt("type2id"));
							//if (info) Dbutil.logger.debug("Candidate "+rs2.getInt("itemid")+", "+Dbutil.readValueFromDB("select * from aiitems where itemid="+rs2.getInt("itemid"), "itemdescription")+" has properties "+rs2.getString("propertyids"));
						}
						Dbutil.closeRs(rs2);
						if (measureperformance) itempropstime+=(new java.util.Date().getTime()-start);
						if (measureperformance) start=new java.util.Date().getTime();
						item=whichItem();
						if (measureperformance) whichitem+=(new java.util.Date().getTime()-start);
						if (measureperformance) start=new java.util.Date().getTime();
						//if (info) Dbutil.logger.debug("Match: item "+item+" ("+Dbutil.readValueFromDB("select * from aiitems where itemid="+item, "itemdescription")+") matches "+Dbutil.readValueFromDB("select * from aiwines where wineid="+rs.getInt("recordid"), "name"));
						if (item.length>0){
							if (item.length==1){
								Dbutil.executeQuery("insert ignore into airesults (tenant,itemid,recordid) values ("+tenant+","+item[0]+","+rs.getInt("recordid")+");",temptablecon);
								if (!temporary) Dbutil.executeQuery("update aidefects set faults=(faults+1) where tenant="+tenant+" and description='OK';",temptablecon);
								logged=true;
								if (info) Dbutil.logger.debug("Match: record "+rs.getInt("recordid")+" "+Dbutil.readValueFromDB("select * from "+table+" where "+idfield+"="+rs.getInt("recordid"), lookupfield)+" matches item "+item[0]+" ("+Dbutil.readValueFromDB("select * from aiitems where itemid="+item[0], "itemdescription")+") ");								
							} else {
								if (!temporary) Dbutil.executeQuery("update aidefects set faults=(faults+1) where tenant="+tenant+" and description='More than 1 candidates left';",temptablecon);
								logged=true;
								for (Integer t:item){
									Dbutil.executeQuery("insert ignore into airesults (tenant,itemid,recordid) values ("+tenant+","+t+","+rs.getInt("recordid")+");",temptablecon);
									//if (info) Dbutil.logger.debug("Match: record "+rs.getInt("recordid")+" "+Dbutil.readValueFromDB("select * from aiwines where wineid="+rs.getInt("recordid"), "name")+" matches item "+item+" ("+Dbutil.readValueFromDB("select * from aiitems where itemid="+item, "itemdescription")+") ");
								}
							}

						} else {
							if (!temporary) Dbutil.executeQuery("update aidefects set faults=(faults+1) where tenant="+tenant+" and description='No candidates found';",temptablecon);
							if (!temporary) Dbutil.executeQuery("insert into ainocandidates select "+rs.getInt("recordid")+";",temptablecon);
							logged=true;


						}
						if (measureperformance) insert+=(new java.util.Date().getTime()-start);

					}
					item=null;
					if (!logged) Dbutil.logger.error(rs.getInt("recordid")+" was not logged!");
					} else {
						Dbutil.logger.error("Could not find record with query "+query);
					}
				}
				Dbutil.closeRs(rs);
				Dbutil.closeRs(rs2);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			Dbutil.logger.error(itemdescription+" has caused a problem:");
			throw new Exception();
		} finally {
			Dbutil.logger.warn("Finished getMatches thread "+thisthread+".");
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
			Dbutil.closeConnection(con3);
		}
	}

	public void restart(){
		Dbutil.executeQuery("delete from airesults where tenant="+tenant+";",temptablecon);
		Dbutil.executeQuery("delete from ainocandidates;",temptablecon);
		Dbutil.executeQuery("update aidefects set faults=0 where tenant="+tenant+";",temptablecon);

	}

	/**
	 * 
	 * 	 
	 * */
	public void getMatchesProducers(int itemtodebug){
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		long start=0;
		int itemcriteriatime=0;
		int itempropstime=0;
		int insert=0;
		int whichitem=0;
		int whichitemeromheen=0;
		int getrecordprops=0;
		Integer[] item;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		java.sql.Connection con3=Dbutil.openNewConnection();
		String properties;
		boolean moretodo=true;
		int rowsperselect=100;
		int offset=0;
		boolean logged=false;
		boolean flag;
		TreeSet<Integer> producers;
		String country;
		String[] region;
		String address;
		String[] coordinates;
		String phonehash;
		int id;
		HashMap<String,String[]> cache=new HashMap<String, String[]>();


		try{

			if (restart){
				Dbutil.executeQuery("update knownwines set producerids='' ;",temptablecon);
			} else {
				offset=Dbutil.readIntValueFromDB("select count(*) as thecount from aipropertymatchesconsolidated where recordid<(select max(recordid) as id from airesults);", "thecount");
			}
			Dbutil.executeQuery("SET SESSION group_concat_max_len = 1000000;", con2);
			Dbutil.executeQuery("load index into cache aiitempropsconsolidated;", con2);
			Dbutil.executeQuery("load index into cache knownwines;", con2);

			int record=0;
			while (moretodo){
				if (itemtodebug>1){
					query="select * from aiproperties where tenant="+tenant+(itemtodebug>0?" and propertyid="+itemtodebug:"")+";";
					moretodo=false;
				} else {
					query="select * from aiproperties natural join aipropertymatches where tenant="+tenant+" and typeid=1 group by propertyid order by propertyid limit "+offset+","+rowsperselect+";";
				}
				rs=Dbutil.selectQuery(rs,query, temptablecon);
				if (!rs.next()){
					moretodo=false;
				} else {
					rs.beforeFirst();
				}
				offset=offset+rowsperselect;
				while (rs.next()){
					Dbutil.logger.debug(rs.getString("propertydescription"));
					region=Dbutil.readValueFromDB("select "+regionhierarchytable+".* from (select * from aiitempropsconsolidated where type1id="+rs.getInt("propertyid")+" group by type2id order by count(*) desc limit 1) reg join "+regionhierarchytable+" on (type2id="+regionhierarchytable+".lft);","region").split(", ");
					country=region[0];
					// select producers in the vicinity
					address="";
					for (int i=region.length-1;i>-1;i--){
						address+=", "+region[i];
					}
					if (address.length()>1)	address=address.substring(2);
					coordinates=cache.get(address);
					if (coordinates==null)	{
						coordinates=Coordinates.getCoordinates(address);
						cache.put(address,coordinates);
					}
					if (!coordinates[0].equals("0")){
						query="select group_concat(recordid) as can, group_concat(lon) as lon, group_concat(lat) as lat, group_concat(name) as name from aipropertymatches join producers on (recordid=producers.id) where aipropertymatches.propertyid="+rs.getInt("propertyid")+";";
						rs2=Dbutil.selectQuery(rs2,query, temptablecon);
						producers=null;
						if (rs2.next()){
							producers=getTreeSet(rs2.getString("can"));
							/*
							Dbutil.logger.error(producers);
							Dbutil.logger.error(rs2.getString("lon"));
							Dbutil.logger.error(coordinates[0]);
							Dbutil.logger.error(rs2.getString("lat"));
							Dbutil.logger.error(coordinates[1]);
							Dbutil.logger.error(rs2.getString("name"));
							Dbutil.logger.error(rs.getString("propertydescription"));
							 */
						}
						Dbutil.closeRs(rs2);
						if (producers!=null&&producers.size()>0){
							query="select group_concat(recordid) as can from aipropertymatches join producers on (recordid=producers.id) where aipropertymatches.propertyid="+rs.getInt("propertyid")+" and lon!=0 and abs(producers.lon-"+coordinates[0]+")<4 and abs(producers.lat-"+coordinates[1]+")<4;";
							rs2=Dbutil.selectQuery(rs2,query, temptablecon);
							producers=null;
							if (rs2.next()){
								producers=getTreeSet(rs2.getString("can"));
							}
							Dbutil.closeRs(rs2);
						}
						if (producers!=null&&producers.size()>0){
							if (producers.size()==1){
								query="update aiitempropsconsolidated join knownwines on (aiitempropsconsolidated.itemid=knownwines.id) set knownwines.producerids='"+producers.iterator().next()+"' where type1id="+rs.getInt("propertyid");
								Dbutil.executeQuery(query, con3);
							} else {
								query="select *,count(*) as thecount from producers where id in ("+getString(producers)+") and phonelastdigits!='' group by phonelastdigits order by count(*) desc,accuracy desc;";
								rs2=Dbutil.selectQuery(query, con2);
								if (rs2.next()){
									if (rs2.getInt("thecount")>1){
										id=Dbutil.readIntValueFromDB("select * from producers where id in ("+getString(producers)+") and phonelastdigits='"+rs2.getString("phonelastdigits")+"' order by accuracy desc,website desc;", "id");
										Dbutil.executeQuery("update aiitempropsconsolidated join knownwines on (aiitempropsconsolidated.itemid=knownwines.id) set knownwines.producerids='"+producers.iterator().next()+"' where type1id="+rs.getInt("propertyid"), con3);									
									}
								}
								Dbutil.closeRs(rs2);
							}
						}
					}

				}
			}
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
			Dbutil.closeConnection(con3);
		}
	}

	public String getProperties(ResultSet rs,boolean filter) throws SQLException{
		String property="";
		String properties="";
		for (int i=1;i<8;i++){
			if (i!=2&&i!=3){ //LELIJK!!!
				if (true||filter){ // Riskant
					property=getString(filterMostRestrictiveProperty(getTreeSet(rs.getString("type"+i+"id"))));
				} else {
					property=getString(getTreeSet(rs.getString("type"+i+"id")));
				}
				//if (i==2) property=getString(filterMostRestrictiveAppellation(getTreeSet(property)));
				if (property!=null&&!property.equals("0")&&!property.equals("")){
					if (i==6){
						// if blanc, add exception for producernames with color in the name, i.e. Cheval Blanc
						String producers=getPropertiesMatchingOtherProperties(1,property);
						if (property.contains(",")){
							properties+=" and (type"+i+"id in ("+property+")";
						} else {
							properties+=" and (type"+i+"id="+property;
						}
						if (!producers.equals("")) properties+=" or type1id in ("+producers+")";
						properties+=")";


					} else {
						if (property.contains(",")){
							properties+=" and type"+i+"id in ("+property+")";
						} else {
							properties+=" and type"+i+"id="+property;
						}
					}
				}
			}
			if (i==2){
				properties+=getRegionClause((filterMostRestrictiveRegion(getTreeSet(rs.getString("type"+i+"id")),filter)),filter);
				regions=filterMostRestrictiveRegion(getTreeSet(rs.getString("type"+i+"id")),true);
			}
		}
		return properties;

	}

	@SuppressWarnings("unchecked")
	public Integer[] whichItem(){
		if (itemprops.size()==0) return new Integer[]{};
		if (itemprops.size()==1) return new Integer[]{itemprops.keySet().iterator().next()};
		allrecordprops=(TreeSet<Integer>) recordprops.clone();
		lastitemprops=(HashMap<Integer, TreeSet<Integer>>) itemprops.clone();
		results=null;
		step=0;

		if (debug) dumpProperties();

		filterRpropsNotFound();
		filterIrrelevantRprops();
		//filterLimitedIprops();
		// At this point, relevantrprops contains only those properties
		// that are contained in at least one item and not all items.

		// Filter only those items that have at least one relevant property
		//if (itemprops.size()>0) filterRelevantProperty(false);
		//if (results!=null) return results;

		//if (itemprops.size()>0) filterRelevantProperty(true);
		//if (results!=null) return results;

		// Filter out all candidates based on grapes but be gentle on the blends
		if (itemprops.size()>0) filterGrapes(grapes,grapemap,false);
		if (results!=null) return results;

		// Filter items that match all properties
		//if (itemprops.size()>0) filterAllProperties();
		//if (results!=null) return results;

		if (itemprops.size()>0) filterMoreDescriptive(true);
		if (results!=null) return results;

		// was uitgecomment, is hier iets mis mee?
		if (itemprops.size()>0) filterLeastRestrictive();
		if (results!=null) return results;

		// Filter non-descriptive producers
		if (itemprops.size()>0) filterNonDescriptiveProducers();
		if (results!=null) return results;

		// Filter more-descriptive producers
		if (itemprops.size()>0)	filterMoreDescriptiveProducers();
		if (results!=null) return results;


		// Second try:

		if (itemprops.size()>0) filterMoreDescriptive(false);
		if (results!=null) return results;

		// reduce properties with the limited set we have now
		if (itemprops.size()>0) filterRpropsNotFound();
		if (itemprops.size()>0) filterIrrelevantRprops();

		if (itemprops.size()>0) filterRelevance();
		if (results!=null) return results;


		//if (itemprops.size()>0) filterLimitedIprops();

		// Filter only those items that have at least one relevant property
		//if (itemprops.size()>0) filterRelevantProperty(false);
		//if (results!=null) return results;

		//if (itemprops.size()>0) filterRelevantProperty(true);
		//if (results!=null) return results;

		//if (itemprops.size()>0) filterAllProperties();
		//if (results!=null) return results;


		//if (itemprops.size()>0) filterLeastRestrictive();
		//if (results!=null) return results;



		// Filter out all candidates based on grapes and try to find an exact match
		if (itemprops.size()>0) filterGrapes(grapes,grapemap,true);
		if (results!=null) return results;

		// Pick highest regions
		if (itemprops.size()>0) filterRegion();
		if (results!=null) return results;

		if (itemprops.size()>0)	filterMissingtermsRelevance();
		if (results!=null) return results;


		// If no color was found, select in order of preference: Red, White, Rose
		if (itemprops.size()>0) filterColor(color);
		if (results!=null) return results;

		// Select the 10x more probable candidate
		if (itemprops.size()>0) filterMostProbable();
		if (results!=null) return results;


		if (info) Dbutil.logger.debug("NOK Not found: "+itemdescription);	
		if (info) Dbutil.logger.debug("Remaining candidates: "+itemdescription);	
		for (int i:itemprops.keySet()){
			if (info) Dbutil.logger.debug("- "+i+": "+Dbutil.readValueFromDB("select * from aiitems where itemid="+i, "itemdescription")+";");

		}



		return itemprops.keySet().toArray(new Integer[1]);
		/*
		// filter only the candidates that have a unique property in common with the record propertyset. 
		candidates=new TreeSet<Integer>();
		it=rprops.iterator();
		String unique="";
		while (it.hasNext()){
			candidate=0;
			prop=it.next();
			it2=relevantiprops.keySet().iterator();
			while (it2.hasNext()&&candidate>-1){
				int c=it2.next();
				if (relevantiprops.get(c).contains(prop)){
					if (candidate==0) {
						candidate=c;
					} else {
						candidate=-1;
					}
				}
			}
			if (candidate>0&&unique.equals("")) {
				candidates.add(candidate);
				unique=Dbutil.readValueFromDB("select * from aiproperties where propertyid="+prop, "propertydescription");
			}
		}


		if (candidates.size()==1) {
			if (info) Dbutil.logger.debug("OK: Unique property found ('"+unique+"'. Record="+wine+", recognized as "+Dbutil.readValueFromDB("select * from aiitems where itemid="+candidates.first(), "itemdescription"));
			return new Integer[]{candidates.first()};
		}

		if (debug) Dbutil.logger.debug("From "+lastiprops.size()+" to "+candidates.size()+" with filter on unique property");
		if (candidates.size()>0){
			lastiprops=new HashMap<Integer, TreeSet<Integer>>();
			for (int t:candidates){
				lastiprops.put(t, relevantiprops.get(t));
			}
			relevantiprops=(HashMap<Integer, TreeSet<Integer>>) lastiprops.clone();
			if (debug){
				Dbutil.logger.debug("Remaining:");
				it=relevantiprops.keySet().iterator();
				while (it.hasNext()){
					int t=it.next();
					if (debug) Dbutil.logger.debug("Item "+t+": "+Dbutil.readValueFromDB("select * from aiitems where itemid="+t, "itemdescription")+"");
				}
			}

		}
		lastiprops=(HashMap<Integer, TreeSet<Integer>>) relevantiprops.clone();

		 */


		// See which items have all relevantrprops 

		/* Dubbel
		// Find candidates which have a match for all properties
		candidates=new TreeSet<Integer>();
		it=relevantiprops.keySet().iterator();
		while (it.hasNext()){
			int c=it.next();
			candidate=c;
			it2=iprops.get(c).iterator();
			while (it2.hasNext()){
				int d=it2.next();
				if (d>0&&!rprops.contains(d)){
					candidate=0;
				}
			}
			if (candidate>0) candidates.add(candidate); 
		}

		if (candidates.size()==1) {
			if (info) Dbutil.logger.debug("OK: Match on all cuvee properties for one wine. Record="+wine+", recognized as "+Dbutil.readValueFromDB("select * from aiitems where itemid="+candidates.first(), "itemdescription"));
			return new Integer[]{candidates.first()};
		}
		if (candidates.size()==0){
			if (debug) Dbutil.logger.debug("No candidates where all properties matched.");	
		} else {

			if (debug) Dbutil.logger.debug("From "+lastiprops.size()+" to "+candidates.size()+" with filter on all cuvee properties");
			lastiprops=new HashMap<Integer, TreeSet<Integer>>();
			it=candidates.iterator();
			while (it.hasNext()){
				int d=it.next();
				lastiprops.put(d, relevantiprops.get(d));
			}
			relevantiprops=(HashMap<Integer, TreeSet<Integer>>) lastiprops.clone();
			if (debug){
				Dbutil.logger.debug("Remaining:");
				it=relevantiprops.keySet().iterator();
				while (it.hasNext()){
					int t=it.next();
					Dbutil.logger.debug("Item "+t+": "+Dbutil.readValueFromDB("select * from aiitems where itemid="+t, "itemdescription")+"");
				}
			}
		}
		 */


	}

	private void dumpProperties(){
		if (debug){
			Iterator<Integer> it;
			Dbutil.logger.debug("Record:"+itemdescription);
			//Dbutil.logger.debug("Producer Properties:");
			//Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in (select distinct(propertyid) from aipropertymatches where typeid=1 and tenant="+tenant+" and recordid="+recordid+");", "props"));

			Dbutil.logger.debug("Region Properties:");
			String propstring=getString(regions);
			if (propstring.length()>0){
				Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in ("+propstring+");", "props"));
			} else {
				Dbutil.logger.debug("No region properties.");
			}
			Dbutil.logger.debug("Cuvee Properties:");
			propstring=getString(recordprops);
			if (propstring.length()>0){
				Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in ("+propstring+");", "props"));
			} else {
				Dbutil.logger.debug("No cuvee properties.");
			}
			Dbutil.logger.debug("Relevant cuvee properties:");
			propstring=getString(limitedrecordprops);
			if (propstring.length()>0){
				Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in ("+propstring+");", "props"));
			} else {
				Dbutil.logger.debug("No relevant record properties.");
			}
			Dbutil.logger.debug("Relevant limited record properties:");
			propstring=getString(limitedrecordprops);
			if (propstring.length()>0){
				Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in ("+propstring+");", "props"));
			} else {
				Dbutil.logger.debug("No relevant limited record properties.");
			}


			Dbutil.logger.debug("Items:");
			it=itemprops.keySet().iterator();
			while (it.hasNext()){
				int t=it.next();
				Dbutil.logger.debug("Item "+t+": "+Dbutil.readValueFromDB("select * from aiitems where itemid="+t, "itemdescription")+" has properties:");
				propstring=getString(itemprops.get(t));
				if (!propstring.equals("")) Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in ("+propstring+");", "props"));
				Dbutil.logger.debug("Item "+t+": "+Dbutil.readValueFromDB("select * from aiitems where itemid="+t, "itemdescription")+" has limited properties:");
				propstring=getString(limiteditemprops.get(t));
				if (!propstring.equals("")) Dbutil.logger.debug(Dbutil.readValueFromDB("select group_concat(concat (propertyid,':',propertydescription)) as props from aiproperties where propertyid in ("+propstring+");", "props"));
			}
		}


	}

	private void filterRpropsNotFound(){

		TreeSet<Integer> alliprops=new TreeSet<Integer>();
		TreeSet<Integer> alllimitediprops=new TreeSet<Integer>();
		Iterator<Integer> it;
		int counter;
		it=itemprops.keySet().iterator();
		while (it.hasNext()){
			counter=it.next();
			//iprops.put(counter,filterProperties(iprops.get(counter),ipropsother.get(counter)));
			alliprops.addAll(itemprops.get(counter));
		}
		it=((TreeSet<Integer>)recordprops.clone()).iterator();
		// Filter out properties that are not matched by any candidate
		while (it.hasNext()){
			int t=it.next();
			if (!alliprops.contains(t)) recordprops.remove(t); 
		}
		it=itemprops.keySet().iterator();
		while (it.hasNext()){
			counter=it.next();
			//iprops.put(counter,filterProperties(iprops.get(counter),ipropsother.get(counter)));
			alllimitediprops.addAll(limiteditemprops.get(counter));
		}
		limitedrecordprops=(TreeSet<Integer>) recordprops.clone();
		it=((TreeSet<Integer>)limitedrecordprops.clone()).iterator();
		// Filter out properties that are not matched by any candidate
		while (it.hasNext()){
			int t=it.next();
			if (!alllimitediprops.contains(t)) limitedrecordprops.remove(t); 
		}

	}

	public static String getPropertiesMatchingOtherProperties(int lookuptypeid,String propertyids){
		String result="";
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			if (propertyids!=null&&propertyids.length()>0){
				query="Select * from airecognizer where propertyid in ("+propertyids+");";
				rs=Dbutil.selectQuery(query, con);
				while (rs.next()){
					result+=","+Dbutil.readValueFromDB("select group_concat(propertyid) as ids from aiproperties where typeid="+lookuptypeid+" and "+whereClause("propertydescription", rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"),"")+";", "ids"); 
				}
				result=result.replaceAll(",+", ",").replaceAll("^,", "");
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return result;

	}

	private void filterLimitedIprops(){

		Iterator<Integer> it;
		int t;
		Iterator<Integer> it2;
		int t2;
		it=itemprops.keySet().iterator();
		while (it.hasNext()){
			t=it.next();
			it2=((TreeSet<Integer>)limiteditemprops.get(t).clone()).iterator();
			while(it2.hasNext()){
				t2=it2.next();
				if (!limitedrecordprops.contains(t2)){
					limiteditemprops.get(t).remove(t2);
				}
			}
		}
	}


	private void filterIrrelevantRprops() {
		// Filter out properties that are matched by all candidates
		TreeSet<Integer> irrelevantcandidates = new TreeSet<Integer>();
		Iterator<Integer>it2;
		Iterator<Integer>it=recordprops.iterator();
		while (it.hasNext()){
			int t=it.next();
			it2=itemprops.keySet().iterator();
			while (t>0&&it2.hasNext()){
				if (!itemprops.get(it2.next()).contains(t)){
					t=0;
				}
			}
			if (t>0) irrelevantcandidates.add(t);
		}
		recordprops.removeAll(irrelevantcandidates);

		irrelevantcandidates = new TreeSet<Integer>();
		it=limitedrecordprops.iterator();
		dumpProperties();
		while (it.hasNext()){
			int t=it.next();
			it2=itemprops.keySet().iterator();
			while (t>0&&it2.hasNext()){
				if (!itemprops.get(it2.next()).contains(t)){ // one of the items is missing the property so it is relevant
					t=0;
				}
			}
			if (t>0) irrelevantcandidates.add(t);
		}
		limitedrecordprops.removeAll(irrelevantcandidates);


	}

	// Look at the region of the wines found. 
	// Pick the one with the highest in hierarchy that was found. 
	// The hierachies that were found are assumed to be filtered already 
	// on all parents, so that they only contain the lowest regions
	// that were realy found in the description

	public void filterRegion(){
		boolean log=true;
		String filterdescription="Region";
		long starttime=new java.util.Date().getTime();
		if (regions.size()>0){
			for (int t:lastitemprops.keySet()){
				if (!regions.contains(itemregionmap.get(t))) itemprops.remove(t);
			}
		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}


	// From 2 items, select the one which is ten times more probably than the other
	public static HashMap<Integer, TreeSet<Integer>> filterMostProbable(HashMap<Integer, TreeSet<Integer>> itemids){
		if (itemids.size()!=2) return itemids;
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		int id1=0;
		int id2=0;
		int prob1=0;
		int prob2=0;
		int c;
		try{
			query="select * from aiitems where itemid in ("+getString(itemids.keySet())+");";
			rs=Dbutil.selectQuery(rs,query, con);
			if (rs.next()){
				id1=rs.getInt("itemid");
				prob1=rs.getInt("probability");

				if (rs.next()){
					id2=rs.getInt("itemid");
					prob2=rs.getInt("probability");

					if (rs.next()){
						Dbutil.logger.error("More than two rows found for itemids "+getString(itemids.keySet()));
					} else {

						if (prob1>=10*(prob2+1)){ //add 1 to probability just in case prob2 is 0 and 1 would win from 0
							itemids.remove(id2);
						} else if (prob2>=10*(prob1+1)){
							itemids.remove(id1);
						}
					}

				}
			}

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		return itemids;
	}


	// Only do this if we have found one or more grapes
	// Filter out all candidates which have known grape varieties that are not found 
	// in the record and candidates that do not contain all grapes found.
	// If strict = false, take into account that a "blend" (grape 0) means we do not know 
	// exactly which grapes the item contains, so for a blend it is not necessary
	// that all grapes found in the record were mentioned
	public void filterGrapes(TreeSet<Integer> grapes,HashMap<Integer,TreeSet<Integer>> grapemap, boolean strict){
		boolean log=true;
		String filterdescription="Grapes";
		long starttime=new java.util.Date().getTime();
		TreeSet<Integer> irrelevantcandidates = new TreeSet<Integer>();
		Iterator<Integer> it;
		grapes.remove(blendid);
		int id;
		HashMap<Integer,TreeSet<Integer>> grapemapnoblend=new HashMap<Integer, TreeSet<Integer>>();
		TreeSet<Integer> allitemgrapes=new TreeSet<Integer>();

		for (int t:grapemap.keySet()) {
			grapemapnoblend.put(t,(TreeSet<Integer>)grapemap.get(t).clone());
			grapemapnoblend.get(t).remove(blendid);
			allitemgrapes.addAll(grapemapnoblend.get(t));
		}
		it=((TreeSet<Integer>) grapes.clone()).iterator();
		while(it.hasNext()){
			id=it.next();
			if (!allitemgrapes.contains(id)) grapes.remove(id); // To filter uit cuvees with the name of an unknown grape like Ermitage or Bacchus
		}
		if (grapes.size()>0){ 

			for (int c:itemprops.keySet()){
				if (!grapemap.get(c).contains(blendid)&&!(grapemap.get(c).containsAll(grapes))){
					irrelevantcandidates.add(c);
				}
				if (grapemap.get(c).contains(blendid)&&!grapes.containsAll(grapemapnoblend.get(c))){
					irrelevantcandidates.add(c);
				}
				if (strict){
					if (!grapemapnoblend.get(c).containsAll(grapes)||!grapes.containsAll(grapemapnoblend.get(c))){
						irrelevantcandidates.add(c);
					}
				}
			}

		} else {
			if (debug) Dbutil.logger.debug("Grapes skipped.");
			log=false;
		}

		if (irrelevantcandidates.size()>0){
			it=irrelevantcandidates.iterator();
			while (it.hasNext()){
				itemprops.remove(it.next());
			}
		}
		if (strict){
			if (itemprops.size()==2){
				irrelevantcandidates = new TreeSet<Integer>();
				it=itemprops.keySet().iterator();
				int key1=it.next();
				int key2=it.next();
				if (grapemap.get(key1).contains(blendid)&&!grapemap.get(key2).contains(blendid)&&grapemap.get(key1).containsAll(grapemap.get(key2))&&grapemap.get(key2).containsAll(grapemapnoblend.get(key1))) irrelevantcandidates.add(key1); 
				if (grapemap.get(key2).contains(blendid)&&!grapemap.get(key1).contains(blendid)&&grapemap.get(key2).containsAll(grapemap.get(key1))&&grapemap.get(key1).containsAll(grapemapnoblend.get(key2))) irrelevantcandidates.add(key2);
				if (irrelevantcandidates.size()>0){
					it=irrelevantcandidates.iterator();
					while (it.hasNext()){
						itemprops.remove(it.next());
					}
				}
			}
		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}

	// filter items that match all record properties found
	public void filterAllProperties(){
		boolean log=true;
		String filterdescription="All record properties found";
		long starttime=new java.util.Date().getTime();
		TreeSet<Integer> irrelevantcandidates = new TreeSet<Integer>();
		Iterator<Integer> it = limitedrecordprops.iterator();
		Iterator<Integer> it2;
		while (it.hasNext()){
			int t=it.next();
			it2=itemprops.keySet().iterator();
			while (it2.hasNext()){
				int c=it2.next();
				if (!itemprops.get(c).contains(t)){
					irrelevantcandidates.add(c);
				}
			}
		}
		it=irrelevantcandidates.iterator();
		while (it.hasNext()){
			itemprops.remove(it.next());
		}

		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}

	// filter wines with
	public void filter(){
		boolean log=true;
		String filterdescription="";
		long starttime=new java.util.Date().getTime();

		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}

	// Find a candidate with much more relevant terms found than the other candidates 
	// This filters candidates with very common words like "Les", "Vineyard" etc.
	public void filterRelevance(){
		boolean log=true;
		long starttime=new java.util.Date().getTime();
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String filterdescription="Filter on full text relevance";
		TreeSet<Integer> props=new TreeSet<Integer>();
		for (int t:itemprops.keySet()) {
			for (int u:limiteditemprops.get(t)) {
				if (limitedrecordprops.contains(u)) props.add(u);
			}
		}
		try{
			if (props.size()>0){
				String fts=Dbutil.readValueFromDB("select replace(group_concat(concat('+',propertydescription,' ')),',','') as term from aiproperties where propertyid in ("+getString(props)+");", "term");

				query="select itemid,match(itemdescription) against ('"+fts+"') as score from aiitems where tenant="+tenant+" and itemid in ("+getString(itemprops.keySet())+") order by score desc;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					double score=rs.getDouble("score");
					int item=rs.getInt("itemid");
					if (rs.next()){
						double score2=rs.getDouble("score");
						if ((score>score2+2&&score2<5)||score>(2*score2)){
							itemprops=new HashMap<Integer, TreeSet<Integer>>();
							itemprops.put(item, lastitemprops.get(item));
						}
					}
				}

			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}

	// Find a candidate with much more relevant terms found than the other candidates 
	// This filters candidates with very common words like "Les", "Vineyard" etc.
	public void filterMissingtermsRelevance(){
		boolean log=true;
		long starttime=new java.util.Date().getTime();
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String filterdescription="Filter on full text relevance";
		TreeSet<Integer> props=new TreeSet<Integer>();
		for (int t:itemprops.keySet()) {
			for (int u:limiteditemprops.get(t)) {
				if (!allrecordprops.contains(u)) props.add(u);
			}
		}
		try{
			if (props.size()>0){
				String fts=Dbutil.readValueFromDB("select replace(group_concat(concat('+',propertydescription,' ')),',','') as term from aiproperties where propertyid in ("+getString(props)+");", "term");
				query="select itemid,match(itemdescription) against ('"+fts+"') as score from aiitems where tenant="+tenant+" and itemid in ("+getString(itemprops.keySet())+") order by score;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					double score=rs.getDouble("score");
					int item=rs.getInt("itemid");
					if (rs.next()){
						double score2=rs.getDouble("score");
						if (score*2<(score2)){
							itemprops=new HashMap<Integer, TreeSet<Integer>>();
							itemprops.put(item, lastitemprops.get(item));
						}
					}
				}

			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}



	// select a wine if it is 10x more probable than the alternative
	private void filterMostProbable(){
		boolean log=true;
		String filterdescription="Most probable";
		long starttime=new java.util.Date().getTime();
		// If two left and one is more than ten times as probable as the other, pick that one
		if (itemprops.size()==2){
			String query;
			ResultSet rs=null;
			java.sql.Connection con=Dbutil.openNewConnection();
			int id1=0;
			int id2=0;
			int prob1=0;
			int prob2=0;
			int c;
			try{
				query="select * from aiitems where itemid in ("+getString(itemprops.keySet())+");";
				rs=Dbutil.selectQuery(rs,query, con);
				if (rs.next()){
					id1=rs.getInt("itemid");
					prob1=rs.getInt("probability");

					if (rs.next()){
						id2=rs.getInt("itemid");
						prob2=rs.getInt("probability");

						if (rs.next()){
							Dbutil.logger.error("More than two rows found for itemids "+getString(itemprops.keySet()));
						} else {

							if (prob1>=10*(prob2+1)){ //add 1 to probability just in case prob2 is 0 and 1 would win from 0
								itemprops.remove(id2);
							} else if (prob2>=10*(prob1+1)){
								itemprops.remove(id1);
							}
						}

					}
				}

			} catch (Exception e){
				Dbutil.logger.error("Problem: ",e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}

		}

		if (itemprops.size()>1){

			// See if all properties are identical. If so, choose the one with the highest probability
			if (itemprops.size()<10){
				if (1==Dbutil.readIntValueFromDB("select count(*) as thecount from (select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") group by type1id,type3id,type6id,type8ids) asd;","thecount")){
					int d=Dbutil.readIntValueFromDB("select * from aiitems where itemid in ("+getString(itemprops.keySet())+") order by probability desc,itemid;","itemid");
					itemprops=new HashMap<Integer, TreeSet<Integer>>();
					itemprops.put(d, lastitemprops.get(d));
				}
			} 

		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}

	// if no color info was found and only one red wine was found, select it
	// if no color info was found and , no red and only one white wine was found, select it
	public void filterColor(TreeSet<Integer> color){
		boolean log=true;
		String filterdescription="Color";
		long starttime=new java.util.Date().getTime();
		// See if the only difference is in color. If no color found, choose the red one, then white
		if (itemprops.size()<10&&color.size()==0){
			if (1==Dbutil.readIntValueFromDB("select count(*) as thecount from (select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") group by type1id,type3id) asd;","thecount")&&1==Dbutil.readIntValueFromDB("select count(*) as thecount from (select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") and type6id='6') asd;","thecount")){
				int d=Dbutil.readIntValueFromDB("select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") and type6id='6';","itemid");
				if (info) Dbutil.logger.debug("Assumed color is red for this wine. Record="+itemdescription+", recognized as "+Dbutil.readValueFromDB("select * from aiitems where itemid="+itemprops.keySet().iterator().next(), "itemdescription"));
				itemprops=new HashMap<Integer, TreeSet<Integer>>();
				itemprops.put(d, lastitemprops.get(d));
			} else 
				if (1==Dbutil.readIntValueFromDB("select count(*) as thecount from (select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") group by type1id,type3id) asd;","thecount")&&0==Dbutil.readIntValueFromDB("select count(*) as thecount from (select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") and type6id='6') asd;","thecount")&&1==Dbutil.readIntValueFromDB("select count(*) as thecount from (select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") and type6id='5') asd;","thecount")){
					int d=Dbutil.readIntValueFromDB("select * from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") and type6id='5';","itemid");
					if (info) Dbutil.logger.debug("Assumed color is white and not rose for this wine. Record="+itemdescription+", recognized as "+Dbutil.readValueFromDB("select * from aiitems where itemid="+itemprops.keySet().iterator().next(), "itemdescription"));
					itemprops=new HashMap<Integer, TreeSet<Integer>>();
					itemprops.put(d, lastitemprops.get(d));
				}
		} else {
			if (debug) Dbutil.logger.debug("Skipped assuming red");
			log=false;
		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}

	/**
	 * Find more descriptive candidates: candidates where there is a more descriptive item in the set
	 * Remove if there is another item that has at least one extra property 
	 * that was found, and none of the properties that it is missing were found
	 */
	public void filterMoreDescriptive(boolean limited){
		boolean log=true;
		String filterdescription="More descriptive candidates";
		long starttime=new java.util.Date().getTime();
		TreeSet<Integer> candidates = new TreeSet<Integer>();
		TreeSet<Integer> extrac;
		TreeSet<Integer> extrad;
		boolean cmoresignificant;
		boolean dmoresignificant;
		TreeSet<Integer> delete = new TreeSet<Integer>();
		candidates.addAll(lastitemprops.keySet());
		Iterator<Integer> it;
		for (int c:candidates){
			for (int d:candidates){ 
				if (c!=d&&!delete.contains(c)&&!delete.contains(d)) {
					if (limited){
						extrac=(TreeSet<Integer>) limiteditemprops.get(c).clone();
						extrac.removeAll(itemprops.get(d));
						extrad=(TreeSet<Integer>) limiteditemprops.get(d).clone();
						extrad.removeAll(itemprops.get(c));
					} else {
						extrac=(TreeSet<Integer>) itemprops.get(c).clone();
						extrac.removeAll(itemprops.get(d));
						extrad=(TreeSet<Integer>) itemprops.get(d).clone();
						extrad.removeAll(itemprops.get(c));
					}
					
					cmoresignificant=false;
					dmoresignificant=false;
					it=extrac.iterator();
					while (!cmoresignificant&&it.hasNext()){
						if (recordprops.contains(it.next())){
							cmoresignificant=true;
						}
					}
					it=extrad.iterator();
					while (!dmoresignificant&&it.hasNext()){
						if (recordprops.contains(it.next())){
							dmoresignificant=true;
						}
					}
					if (cmoresignificant&&!dmoresignificant) delete.add(d);
					if (dmoresignificant&&!cmoresignificant) delete.add(c);
					if (!cmoresignificant&&!dmoresignificant){
						if (extrac.size()==0&&extrad.size()>0) delete.add(d);
						if (extrad.size()==0&&extrac.size()>0) delete.add(c);
					}
				}
			}
		}
		for (int t:delete){
			itemprops.remove(t);
		}
		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}


	// find items with less properties than this item,
	// and all extra properties this item has were not found

	public void filterLeastRestrictive(){
		boolean log=true;
		String filterdescription="Least Restrictive";
		long starttime=new java.util.Date().getTime();
		// Find the least restrictive candidates: candidates where all more descriptive wines in the set have no match on their more restrictive properties
		TreeSet<Integer> candidates = new TreeSet<Integer>();
		TreeSet<Integer> delete = new TreeSet<Integer>();
		TreeSet<Integer> testset;
		dumpProperties();
		candidates.addAll(itemprops.keySet());
		for (int c:candidates){
			for (int d:candidates){ 
				if (!delete.contains(c)&&c!=d&&itemprops.get(c).containsAll(limiteditemprops.get(d))&&!itemprops.get(d).containsAll(itemprops.get(c))){
					testset=(TreeSet<Integer>) itemprops.get(c).clone();
					testset.removeAll(itemprops.get(d));
					int initialsize=testset.size();
					testset.removeAll(recordprops);
					if (initialsize==testset.size()){
						delete.add(c);
					}

				}
			}
		}
		for (int c:delete){
			itemprops.remove(c);
		}

		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}


	// filter wines with a at least one relevant property
	public void filterRelevantProperty(boolean limited){
		boolean log=true;
		String filterdescription="Relevant property";
		long starttime=new java.util.Date().getTime();
		TreeSet<Integer> candidates = new TreeSet<Integer>();
		Iterator<Integer> it;
		dumpProperties();
		if (limited){
			it=limitedrecordprops.iterator();
			Iterator<Integer> it2;
			while (it.hasNext()){
				int t=it.next();
				it2=itemprops.keySet().iterator();
				while (it2.hasNext()){
					int c=it2.next();
					if (itemprops.get(c).contains(t)){
						candidates.add(c);
					}
				}
			}
		} else {
			it=recordprops.iterator();
			Iterator<Integer> it2;
			while (it.hasNext()){
				int t=it.next();
				it2=itemprops.keySet().iterator();
				while (it2.hasNext()){
					int c=it2.next();
					if (itemprops.get(c).contains(t)){
						candidates.add(c);
					}
				}
			}
		}
		TreeSet<Integer> set=new TreeSet<Integer>();
		set.addAll(itemprops.keySet());
		it=set.iterator();
		// Filter out properties that are not matched by any candidate
		while (it.hasNext()){
			int t=it.next();
			if (!candidates.contains(t)) itemprops.remove(t); 
		}


		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false,log);
	}


	// filter wines with a producer name that matches other wines in the set (e.g. Sonoma vineyards)
	public void filterNonDescriptiveProducers(){
		long starttime=new java.util.Date().getTime();
		String filterdescription="Non descriptive producers";
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		TreeSet<Integer> delete=new TreeSet<Integer>();
		boolean measureperformance=false;
		long start=(long)0;
		long start2=(long)0;
		long selection=(long)0;
		if (measureperformance) start=new java.util.Date().getTime();

		try{
			query="select count(distinct(type1id)) as num from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+");";
			rs=Dbutil.selectQuery(rs, query, con);
			if (rs.next()){
				if (rs.getInt("num")>1){
					Dbutil.closeRs(rs);
					query="select distinct(type1id),fts,regex,regexexcl from aiitempropsconsolidated join airecognizer on (type1id=airecognizer.propertyid) where airecognizer.tenant="+tenant+" and airecognizer.typeid=1 and itemid in ("+getString(itemprops.keySet())+");";
					rs=Dbutil.selectQuery(rs, query, con);

					while (rs.next()){
						query="select count(*) as num from knownwines use key (primary) where id in ("+getString(itemprops.keySet())+") and "+whereClause("wine,locale",rs.getString("fts"),rs.getString("regex"),rs.getString("regexexcl"),"")+";";
						if (query.endsWith("and ;")){
							Dbutil.logger.warn("No where clause for propertyid "+rs.getString("type1id"));
						} else {
							if (measureperformance) start2=new java.util.Date().getTime();
							int y=Dbutil.readIntValueFromDB(query, "num");
							if (measureperformance) selection+=(new java.util.Date().getTime()-start2);
							if (y==itemprops.size()){
								query="select group_concat(itemid) as itemids from aiitempropsconsolidated where itemid in ("+getString(itemprops.keySet())+") and type1id="+rs.getInt("type1id")+";";
								rs2=Dbutil.selectQuery(rs2, query, con);
								if (rs2.next()){
									for (String id:rs2.getString("itemids").split(",")){
										delete.add(Integer.parseInt(id));
									}
								}

							}

						}
					}

					if (delete.size()>0&&delete.size()<itemprops.size()){
						for (int t:delete){
							itemprops.remove(t);
						}
					}
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		if (measureperformance) Dbutil.logger.error("Selection "+selection);
		if (measureperformance) Dbutil.logger.error("Total "+(new java.util.Date().getTime()-start)+"");

		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false, true);

	}


	// filter out wines with a producer name that is also matched by other wines in the set (e.g. Ch�teau Pichon Longueville Comtesse de Lalande from Pauillac matches also Pauillac de Pichon Lalande)
	public void filterMoreDescriptiveProducers(){
		long starttime=new java.util.Date().getTime();
		String filterdescription="More descriptive producers";
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		TreeSet<Integer> delete=new TreeSet<Integer>();
		boolean measureperformance=false;
		long start=(long)0;
		long start2=(long)0;
		long selection=(long)0;
		if (measureperformance) start=new java.util.Date().getTime();
		int c=0;
		int d=0;
		String cclause;
		String dclause;

		try{
			// Get all different producers in the wine set
			query="select * from aiitems natural join aiitemproperties natural join airecognizer where typeid=1 and itemid in ("+getString(itemprops.keySet())+");";
			rs=Dbutil.selectQuery(rs, query, con);
			while (rs.next()){
				if (!delete.contains(c)){
					c=rs.getInt("itemid");
					cclause=whereClause("wine,locale", rs.getString("fts"),rs.getString("regex"),rs.getString("regexexcl"),"")+";";
					query="select * from aiitems natural join aiitemproperties natural join airecognizer where typeid=1 and itemid in ("+getString(itemprops.keySet())+") and itemid>"+c+";";
					rs2=Dbutil.selectQuery(rs2, query, con);
					while (rs2.next()){
						d=rs2.getInt("itemid");
						if (!delete.contains(d)){
							dclause=whereClause("wine,locale", rs2.getString("fts"),rs2.getString("regex"),rs2.getString("regexexcl"),"")+";";
							if (cclause.equals(";")){
								delete.add(c);
							} else if (dclause.equals(";")){
								delete.add(d);
							} else {
								if (Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where id="+c+" and "+dclause,"thecount")==1){
									if (Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where id="+d+" and "+cclause,"thecount")==0){
										delete.add(d);
									}
								} else {
									if (Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where id="+d+" and "+cclause,"thecount")==1){
										delete.add(c);
									}
								}
							}
						}
					}	
				}
			}
			for (int del:delete){
				itemprops.remove(del);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		if (measureperformance) Dbutil.logger.error("Selection "+selection);
		if (measureperformance) Dbutil.logger.error("Total "+(new java.util.Date().getTime()-start)+"");

		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false, true);

	}



	// filter out wines with cuvee properties that are also matched by non-cuvee fields from the other wines in the set (e.g. Romanee Conti as a cuvee is also matched bij Domaine de la romanee conti Richebourg)
	public void filterCuveePropertiesMatchedByOtherProperties(){
		long starttime=new java.util.Date().getTime();
		String filterdescription="Non descriptive cuvee props";
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		TreeSet<Integer> delete=new TreeSet<Integer>();
		boolean measureperformance=false;
		long start=(long)0;
		long start2=(long)0;
		long selection=(long)0;
		if (measureperformance) start=new java.util.Date().getTime();
		int c=0;
		int d=0;
		String cclause;
		String dclause;

		try{
			// Get all different producers in the wine set
			query="select * from aiitems natural join aiitemproperties natural join airecognizer where typeid=1 and itemid in ("+getString(itemprops.keySet())+");";
			rs=Dbutil.selectQuery(rs, query, con);
			while (rs.next()){
				if (!delete.contains(c)){
					c=rs.getInt("itemid");
					cclause=whereClause("wine,locale", rs.getString("fts"),rs.getString("regex"),rs.getString("regexexcl"),"")+";";
					query="select * from aiitems natural join aiitemproperties natural join airecognizer where typeid=1 and itemid in ("+getString(itemprops.keySet())+") and itemid>"+c+";";
					rs2=Dbutil.selectQuery(rs2, query, con);
					while (rs2.next()){
						d=rs2.getInt("itemid");
						if (!delete.contains(d)){
							dclause=whereClause("wine,locale", rs2.getString("fts"),rs2.getString("regex"),rs2.getString("regexexcl"),"")+";";
							if (cclause.equals(";")){
								delete.add(c);
							} else if (dclause.equals(";")){
								delete.add(d);
							} else {
								if (Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where id="+c+" and "+dclause,"thecount")==1){
									if (Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where id="+d+" and "+cclause,"thecount")==0){
										delete.add(d);
									}
								} else {
									if (Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where id="+d+" and "+cclause,"thecount")==1){
										delete.add(c);
									}
								}
							}
						}
					}	
				}
			}
			for (int del:delete){
				itemprops.remove(del);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		if (measureperformance) Dbutil.logger.error("Selection "+selection);
		if (measureperformance) Dbutil.logger.error("Total "+(new java.util.Date().getTime()-start)+"");

		logExecution(filterdescription,new java.util.Date().getTime()-starttime);
		filterResults(filterdescription,false, true);

	}



	public void logExecution(String description,long time){
		if (measureperformance){
			if (!stepdescription.containsKey(step)){
				//First call
				stepdescription.put(step, description);
				numberofexecutions.put(step,0);
				executiontime.put(step,(long)0);
			}
			numberofexecutions.put(step,numberofexecutions.get(step)+1);
			executiontime.put(step,executiontime.get(step)+time);

			step++;
		}

	}


	public static TreeSet<Integer> filterMostRestrictiveProperty(TreeSet<Integer> props){
		if (props.size()<2) return props;
		String query;
		TreeSet<Integer> delete=new TreeSet<Integer>();
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		String set="";
		Iterator<Integer> it;
		int c;
		try{
			it=props.iterator();
			while (it.hasNext()){
				set+=","+it.next();
			}
			set=set.substring(1);
			it=props.iterator();
			while (it.hasNext()){
				c=it.next();
				query="select * from aiproperties natural join airecognizer where propertyid="+c+";";
				rs=Dbutil.selectQuery(rs,query, con);
				if (rs.next()){
					query="select * from aiproperties natural join airecognizer where propertyid in ("+set+") and propertyid!="+c+" and "+whereClause("propertydescription",rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"),"")+";";
					rs2=Dbutil.selectQuery(rs2,query, con2);
					while (rs2.next()){
						query="select * from aiproperties where propertyid="+c+" and "+whereClause("propertydescription",rs2.getString("fts"), rs2.getString("regex"), rs2.getString("regexexcl"),"")+";";
						if (Dbutil.readIntValueFromDB(query, "propertyid")==0){
							delete.add(c);
						}
					}
				}
				Dbutil.closeRs(rs2);
				Dbutil.closeRs(rs);
			}
			it=delete.iterator();
			props.removeAll(delete);
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}

		return props;
	}

	public FullTextItems FullTextScore(String name,int itemid, int n){
		name=toFtsUniqueTerms(name);
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		FullTextItems items=new FullTextItems();
		try{
			if (!name.equals("")){
				rs=Dbutil.selectQuery("select itemid,match(itemdescription) against ('"+name+"') as score from aiitems where match(itemdescription) against ('"+name+"') "+(itemid>0?" and itemid="+itemid+" ":"")+" limit "+n+";" , con);
				while (rs.next()){
					items.itemid.add(rs.getInt("itemid"));
					items.score.add(rs.getDouble("score"));
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return items;
	}

	public class FullTextItems{
		ArrayList<Integer> itemid=new ArrayList<Integer>();
		ArrayList<Double> score=new ArrayList<Double>();
	}

	public static String toFtsUniqueTerms(String name){
		String fts="";
		TreeSet<String> terms=new TreeSet<String>();
		name=Aitools.filterPunctuation(name);
		for(String term:name.split("\\s+")){
			if (term.length()>1) fts+=" "+term;
		}
		fts=AiHtmlRefiner.fts(fts);
		if (fts.length()>1) return fts;
		return "";
	}

	public  TreeSet<Integer> filterMostRestrictiveRegion(TreeSet<Integer> props,boolean filter){
		if (props.size()<2) return props;
		String query;
		String whereclause="";
		TreeSet<Integer> parents=new TreeSet<Integer>();
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String set="";
		Iterator<Integer> it;
		try{
			if (props.size()>1&&filter){
				//Direct parent
				it=props.iterator();
				while (it.hasNext()){
					set+=","+it.next();
				}
				set=set.substring(1);
				query="select * from "+regionhierarchytable+" where id in ("+set+");";
				rs=Dbutil.selectQuery(rs,query, con);
				while (rs.next()){
					parents.add(rs.getInt("parentid"));
				}
				Dbutil.closeRs(rs);
				// second parent
				set="";
				it=parents.iterator();
				while (it.hasNext()){
					set+=","+it.next();
				}
				if (set.length()>1){
					set=set.substring(1);
					query="select * from "+regionhierarchytable+" where id in ("+set+");";
					rs=Dbutil.selectQuery(rs,query, con);
					while (rs.next()){
						parents.add(rs.getInt("parentid"));
					}
				}
				Dbutil.closeRs(rs);

				// third parent
				set="";
				it=parents.iterator();
				while (it.hasNext()){
					set+=","+it.next();
				}
				if (set.length()>1){
					set=set.substring(1);
					query="select * from "+regionhierarchytable+" where id in ("+set+");";
					rs=Dbutil.selectQuery(rs,query, con);
					while (rs.next()){
						parents.add(rs.getInt("parentid"));
					}
				}
				Dbutil.closeRs(rs);

				props.removeAll(parents);
				props=filterMostRestrictiveProperty(props);
			}

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return props;
	}

	public String getRegionClause(TreeSet<Integer> props,boolean filter){
		if (props.size()==0) return "";
		String query;
		String whereclause="";
		TreeSet<Integer> parents=new TreeSet<Integer>();
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String set="";
		Iterator<Integer> it;
		String fulltextitemids="";
		try{
			set="";
			it=props.iterator();
			while (it.hasNext()){
				set+=","+it.next();
			}
			set=set.substring(1);
			query="select * from "+regionhierarchytable+" where id in ("+set+");";
			rs=Dbutil.selectQuery(rs,query, con);
			while (rs.next()){
				//if (filter){
				//	whereclause+=" or (type2id="+rs.getInt("lft")+")";
				//} else {
				whereclause+=" or (type2id>="+rs.getInt("lft")+" and type2id<="+rs.getInt("rgt")+")";
				//}
			}
			for (int t:ftitems.itemid){
				fulltextitemids+=","+t;
			}
			if (fulltextitemids.length()>1){
				whereclause+=" or itemid in ("+fulltextitemids.substring(1)+")";
			}
			whereclause=" and ("+whereclause.substring(3)+")";
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return whereclause;
	}

	/**
	 * @deprecated
	 */
	public static TreeSet<Integer> OBSOLETEfilterMostRestrictiveAppellation(TreeSet<Integer> props){
		if (props.size()<2) return props;
		String query;
		TreeSet<Integer> delete=new TreeSet<Integer>();
		HashMap<Integer,Integer> left=new HashMap<Integer, Integer>();
		HashMap<Integer,Integer> right=new HashMap<Integer, Integer>();
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String set="";
		Iterator<Integer> it;
		Iterator<Integer> it2;
		int c;
		int d;
		try{
			it=props.iterator();
			while (it.hasNext()){
				set+=","+it.next();
			}
			set=set.substring(1);
			it=props.iterator();
			while (it.hasNext()){
				c=it.next();
				query="select * from aiproperties join regions on (aiproperties.propertydescription=regions.region) where propertyid in ("+set+");";
				rs=Dbutil.selectQuery(rs,query, con);
				while (rs.next()){
					left.put(rs.getInt("propertyid"), rs.getInt("lft"));
					right.put(rs.getInt("propertyid"), rs.getInt("rgt"));
				}
				Dbutil.closeRs(rs);
			}
			it=left.keySet().iterator();
			while (it.hasNext()){
				c=it.next();
				it2=left.keySet().iterator();
				while (it2.hasNext()){
					d=it2.next();
					if (c!=d&&left.get(c)<left.get(d)&&right.get(c)>right.get(d)){
						props.remove(c);
					}
				}
			}

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}

		return props;
	}

	@SuppressWarnings("unchecked")
	public void filterResults(String filterdescription,boolean forceRemoval, boolean log){
		if (itemprops.size()==1) {
			// Only one item left with relevant producer
			if (log&&info) Dbutil.logger.debug("OK: only 1 item left after filter on "+filterdescription+". Record="+itemdescription+", recognized as "+Dbutil.readValueFromDB("select * from aiitems where itemid="+itemprops.keySet().iterator().next(), "itemdescription"));
			results= new Integer[]{itemprops.keySet().iterator().next()};
		} else { 
			Dbutil.logger.debug("From "+lastitemprops.size()+" to "+itemprops.size()+" with filter on "+filterdescription);
		}
		if (itemprops.size()>0) {
			if (log&&debug) {

				Dbutil.logger.debug("Remaining:");
				Iterator<Integer> it=itemprops.keySet().iterator();
				while (it.hasNext()){
					int t=it.next();
					if (debug) Dbutil.logger.debug("Item "+t+": "+Dbutil.readValueFromDB("select * from aiitems where itemid="+t, "itemdescription")+"");
				}
			}
			lastitemprops=(HashMap<Integer, TreeSet<Integer>>) itemprops.clone();
		} else if (!forceRemoval){
			itemprops=(HashMap<Integer, TreeSet<Integer>>) lastitemprops.clone();
		}
		lastitemprops=(HashMap<Integer, TreeSet<Integer>>) itemprops.clone();

	}

	public static TreeSet<Integer> getTreeSet(String properties){
		TreeSet<Integer> t=new TreeSet<Integer>();
		if (properties!=null){
			for (String num:properties.split(",")){
				try{
					if (!num.equals("")&&!num.equals("0")) t.add(Integer.parseInt(num));
				} catch (Exception e){}
			}
		}
		return t;
	}

	public static String getString(Set<Integer> properties){
		String result="";
		if (properties!=null&&properties.size()>0){
			Iterator<Integer> it;
			it=properties.iterator();
			while (it.hasNext()){
				result+=","+it.next();
			}
			result=result.substring(1);
		}
		return result;
	}

	public void getSingleMatches(){
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			if (refreshAll) Dbutil.executeQuery("update "+table+" set "+resultfield+"=0;");
			rs=Dbutil.selectQuery(rs,"select * from airesults group by recordid having count(*)=1;",temptablecon);

			while (rs.next()){
				Dbutil.executeQuery("update LOW_PRIORITY "+table+" set "+resultfield+"="+rs.getInt("itemid")+" where "+idfield+"="+rs.getInt("recordid")+";", con2);
			}
			Dbutil.closeRs(rs);
			if (false){
				Dbutil.logger.setLevel(Level.INFO);
				rs=Dbutil.selectQuery(rs,"select * from aipropertymatchesconsolidated order by recordid;",temptablecon);
				while (rs.next()){

					Dbutil.logger.debug("Record "+rs.getInt("recordid")+" producer "+rs.getInt("type1id")+" region "+rs.getString("type2id")+".");
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
	}

	public void updateLftRgt(int shopid){
		if (shopid==0){
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "select distinct(knownwineid) as knownwineid from wines;";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					Dbutil.executeQuery("update LOW_PRIORITY wines join knownwines on (wines.knownwineid=knownwines.id) join "+regionhierarchytable+"  on (knownwines.locale="+regionhierarchytable+".region) set wines.lft="+regionhierarchytable+".lft, wines.rgt="+regionhierarchytable+".rgt,wines.region="+regionhierarchytable+".id where knownwineid="+rs.getString("knownwineid")+";",temptablecon);
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
			
		} else {
			Dbutil.executeQuery("update LOW_PRIORITY "+regionhierarchytable+" join knownwines on (knownwines.locale="+regionhierarchytable+".region) join wines on (wines.knownwineid=knownwines.id and wines.shopid="+shopid+") set wines.lft="+regionhierarchytable+".lft, wines.rgt="+regionhierarchytable+".rgt,wines.region="+regionhierarchytable+".id;",temptablecon);
		}
	}

	public void getSingleMatchesDistinct(){
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			if (refreshAll) Dbutil.executeQuery("update "+table+" set "+resultfield+"=0;");
			rs=Dbutil.selectQuery(rs,"select * from airesults join "+table+" on (airesults.recordid="+table+"."+idfield+") group by recordid having count(*)=1;",temptablecon);

			while (rs.next()){
				Dbutil.executeQuery("update "+table+" set "+resultfield+"="+rs.getInt("itemid")+" where "+lookupfield+"='"+Spider.SQLEscape(rs.getString(lookupfield))+"';", con2);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
	}

	public static TreeSet<Integer> filterProperties(TreeSet<Integer> toFilter, TreeSet<Integer> against){
		//TreeSet<Integer> result=new TreeSet<Integer>();
		//HashMap<Integer,String> filterMap=new HashMap<Integer,String>();
		Iterator<Integer> it;
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			it=against.iterator();
			String againstclause="";
			while (it.hasNext()){
				againstclause+=","+it.next();
			}
			if (againstclause.length()>1){
				againstclause=againstclause.substring(1);
				String againstString=Dbutil.readValueFromDB("Select group_concat(propertydescription) as againstString from aiproperties where propertyid in ("+againstclause+");","againstString");
				againstString=" "+Aitools.filterPunctuation(againstString).replace(",", " ")+" ";

				it=toFilter.iterator();
				String filterclause="";
				while (it.hasNext()){
					filterclause+=","+it.next();
				}
				if (filterclause.length()>1){
					filterclause=filterclause.substring(1);
					query="select propertyid,propertydescription from aiproperties where typeid=4 and propertyid in ("+filterclause+");";
					rs=Dbutil.selectQuery(rs,query, con);
					while (rs.next()){
						if (againstString.contains(" "+rs.getString("propertydescription")+" ")) toFilter.remove(rs.getInt("propertyid")); 
					}				
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return toFilter;

	}
	
	public static String whereClause(String field, String fulltextsearch, String literalsearch, String literalsearchexclude){
		return whereClause(field, fulltextsearch, literalsearch, literalsearchexclude,"");
	}

	public static String whereClause(String field, String fulltextsearch, String literalsearch, String literalsearchexclude,String idswith2letters, boolean booleanmode){
		String whereclause;
		StringBuffer literalsearchterm=new StringBuffer();
		String regionsearch="";
		String[] regionterms;
		String[] literalterm;
		String[] literaltermexclude;
		String wordstart="(^|�|[^[:alnum:]])";
		String wordend="($|[^[:alnum:]]|�)";
		String underscore="([^[:alnum:]()]|�)+"; // () because of (chateau) Margaux
		whereclause="";
		String extrawordstart="";
		String extrawordend="";
		String regexterm;
		String liketerm;
		String validliketermregex="^([a-zA-Z0-9]_[a-zA-Z0-9]($|_))";
		boolean twoletters=false;
		
		literalterm=literalsearch.replaceAll("\\\\\\. ", " ").split(" ");
		for (int i=0;i<literalterm.length;i++){
			if (literalterm[i].length()>0){
				if (!twoletters&&!Webroutines.getRegexPatternValue(validliketermregex,literalterm[i]).equals("")) twoletters=true;
				extrawordstart="";
				extrawordend="";
				if (literalterm[i].contains("*")) {
					extrawordstart="?(^|[^*])";
					extrawordend="($|[^*])";
				}
				if (field.split(",").length==1){
					if (literalsearchterm.length()==0){
						literalsearchterm.append(" "+field+" REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literalterm[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'");
						
					} else {
						literalsearchterm.append(" AND "+field+" REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literalterm[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'");
						
					}
				} else {
					regexterm="";
					for (String fieldname:field.split(",")){
						if (regexterm.equals("")){
							regexterm=regexterm+" "+fieldname+" REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literalterm[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'";
						} else {
							regexterm=regexterm+" OR "+fieldname+" REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literalterm[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'";
						}
					}
					if (literalsearchterm.length()==0){
						literalsearchterm.append(" ("+regexterm+")");
					} else {
						literalsearchterm.append(" AND ("+regexterm+")");
					}
					
				}
			}
		}

		literaltermexclude=literalsearchexclude.replaceAll("\\?", "").replaceAll("\\. ", " ").split(" ");
		for (int i=0;i<literaltermexclude.length;i++){
			if (literaltermexclude[i].length()>0){
				if (field.split(",").length==1){
					if (literalsearchterm.length()==0){
						literalsearchterm.append(" "+field+" NOT REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'");
					} else {
						literalsearchterm.append(" AND "+field+" NOT REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'");
					}
				} else {
					regexterm="";
					for (String fieldname:field.split(",")){
						if (regexterm.equals("")){
							regexterm=regexterm+" "+fieldname+" NOT REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'";
						} else {
							regexterm=regexterm+" AND "+fieldname+" NOT REGEXP '"+wordstart+extrawordstart+Spider.replaceString((Spider.replaceString(literaltermexclude[i],"_",underscore)), "'", "\\'")+(extrawordend.equals("")?wordend:extrawordend)+"'";
						}
					}
					if (literalsearchterm.length()==0){
						literalsearchterm.append(" ("+regexterm+")");
					} else {
						literalsearchterm.append(" AND ("+regexterm+")");
					}
				}

			}
		}
		whereclause="";
		if (!fulltextsearch.equals("")){
			if (literalsearchterm.length()==0){
				whereclause = " match ("+field+") against ('"+Spider.SQLEscape(fulltextsearch)+regionsearch+"' "+(booleanmode?" IN BOOLEAN MODE":"")+")";
			} else {
				whereclause = " match ("+field+") against ('"+Spider.SQLEscape(fulltextsearch)+regionsearch+"' "+(booleanmode?" IN BOOLEAN MODE":"")+") AND "+literalsearchterm.toString();
			}
		} else	{
			whereclause = literalsearchterm.toString();
		}
		if (twoletters) {
			whereclause=whereclause+idswith2letters;
		}

		return whereclause;
	}
	
	/**
	 * This routine uses the parameters to compose a MySQL where clause that will match a product description 
	 * against these parameters. fulltext uses the MySQL fulltext search feature, literalsearch is used 
	 * to check terms with regex: an underscore is replaced by a space, so a search for term1_term2 shows 
	 * records with "term1 term2". literalsearchexclude is the exact opposite: it excludes records with a 
-	 * literal text.
	 * @param field: the name of the field in the table to look up
	 * @param fulltextsearch: full text search String in the form "+term1 +term2 -term3"
	 * @param literalsearch: String
	 * @param literalsearchexclude: String 
	 * @return a where clause without the "where" operator.
	 */
	public static String whereClause(String field, String fulltextsearch, String literalsearch, String literalsearchexclude,String idswith2letters){
		return whereClause(field, fulltextsearch, literalsearch, literalsearchexclude,"",true);
	}


	public void standardTemplate(){
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}
	/*
	 * @deprecated
	 */
	public void NotEfficientanalyzeProperties(){
		Dbutil.executeQuery("delete from aipropertymatches where tenant="+tenant+";");
		String query;
		ResultSet rs=null;
		ResultSet rs2=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			query="select * from airecognizer where tenant="+tenant+";";
			rs=Dbutil.selectQuery(rs,query, con);
			while (rs.next()){
				query="select * from "+table+" where "+Recognizer.whereClause(lookupfield, rs.getString("fts"), rs.getString("regex"), rs.getString("regexexcl"),"");
				rs2=Dbutil.selectQuery(rs2,query, con2);
				while (rs2.next()){
					Dbutil.executeQuery("insert into aipropertymatches (tenant,itemid,propertyid,typeid) values ("+tenant+","+rs2.getInt("itemid")+","+rs.getInt("propertyid")+","+rs.getInt("typeid")+");", con2);
				}
				Dbutil.closeRs(rs2);
			}
			Dbutil.executeQuery("delete from aipropertymatchesconsolidated where tenant="+tenant+";",con);
			Dbutil.executeQuery("insert into aipropertymatchesconsolidated (tenant,recordid,propertyids) select tenant,recordid,group_concat(propertyid) from aipropertymatches where tenant="+tenant+" group by recordid;",con);
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
		}

	}

	public void getIdswith2letters(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		StringBuffer sb=new StringBuffer();
		try {
			query = "Select id from airecords where description  REGEXP '(^|�|[^[:alnum:]])[:alnum:]([^[:alnum:]()]|�)+[:alnum:]($|[^[:alnum:]]|�)';";
			rs = Dbutil.selectQueryRowByRow(query, con);
			while (rs.next()) {
				sb.append(","+rs.getString("id"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (sb.length()>1) idswith2letters=" and  id in ("+sb.toString().substring(1)+") ";
		
	}

	/**
	 * getProperties selects all aiproperties (or only properties of a specific type if argument>0)
	 * for the tenant and matches them with instance variables for table and field.
	 * All matches are stored in aipropertymatches
	 */
	public void getProperties(int typeid){
		String query;
		String whereclause;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		ResultSet rs2=null;
		Connection con2=Dbutil.openNewConnection();
		
		try{
			Dbutil.executeQuery("delete from aipropertymatches where tenant="+tenant+";", temptablecon);
			Dbutil.executeQuery("SET SESSION group_concat_max_len = 1000000;", temptablecon);
			Dbutil.executeQuery("alter table aipropertymatches disable keys;", temptablecon);
			//Dbutil.executeQuery("load index into cache "+table+";", con);
			
			getIdswith2letters();
			query="select * from airecognizer where tenant="+tenant+(typeid>0?" and typeid="+typeid:"")+";";
			rs2=Dbutil.selectQuery(rs2,query, con);
			while (rs2.next()){
				whereclause=whereClause("description",rs2.getString("fts"),rs2.getString("regex"),rs2.getString("regexexcl"),idswith2letters);
				//if (rs2.getInt("propertyid")==15990) Dbutil.logger.error(whereclause);
				if (!whereclause.equals("")){
					//query="Select group_concat(concat('(',"+tenant+",',',"+rs2.getString("propertyid")+",',',"+rs2.getString("typeid")+",',',id,')')) matches from airecords where tenant="+tenant+" and "+whereclause;
					query="insert into aipropertymatches (tenant,propertyid,typeid,recordid) select "+tenant+","+rs2.getString("propertyid")+","+rs2.getString("typeid")+",id from airecords where tenant="+tenant+" and "+whereclause;
					Dbutil.executeQuery(query,temptablecon);
					//rs=Dbutil.selectQuery(rs,query, temptablecon);
					//if (rs.next()&&rs.getString("matches")!=null){
					//	Dbutil.executeQuery("insert into aipropertymatches (tenant,propertyid,typeid,recordid) values "+rs.getString("matches")+";",temptablecon);
					//}
					//Dbutil.closeRs(rs);
				}
			}


		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.executeQuery("alter table aipropertymatches enable keys;", temptablecon);
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con2);
		}
	}

	public void getPropertiesConsolidated(){
		Dbutil.executeQuery("delete from aipropertymatchesconsolidated where tenant="+tenant+";",temptablecon);
		Dbutil.executeQuery("insert into aipropertymatchesconsolidated (tenant,recordid,propertyids) select distinct tenant,recordid,'' from aipropertymatches where tenant="+tenant+" group by recordid;",temptablecon);
		Dbutil.executeQuery("update (select recordid,group_concat(propertyid) as ids from aipropertymatches where typeid=4 group by recordid) props join aipropertymatchesconsolidated on (aipropertymatchesconsolidated.recordid=props.recordid) set propertyids=props.ids;",temptablecon);
		Dbutil.executeQuery("update aipropertymatchesconsolidated join (select *,group_concat(propertyid) as ids from aipropertymatches where typeid=1 group by recordid) sd on (aipropertymatchesconsolidated.recordid=sd.recordid) set aipropertymatchesconsolidated.type1id=ids;",temptablecon);
		Dbutil.executeQuery("update aipropertymatchesconsolidated join (select *,group_concat(propertyid) as ids from aipropertymatches where typeid=2 group by recordid) sd on (aipropertymatchesconsolidated.recordid=sd.recordid) set aipropertymatchesconsolidated.type2id=ids;",temptablecon);
		Dbutil.executeQuery("update aipropertymatchesconsolidated join (select *,group_concat(propertyid) as ids from aipropertymatches where typeid=3 group by recordid) sd on (aipropertymatchesconsolidated.recordid=sd.recordid) set aipropertymatchesconsolidated.type3id=ids;",temptablecon);
		Dbutil.executeQuery("update aipropertymatchesconsolidated join (select *,group_concat(propertyid) as ids from aipropertymatches where typeid=5 group by recordid) sd on (aipropertymatchesconsolidated.recordid=sd.recordid) set aipropertymatchesconsolidated.type5id=ids;",temptablecon);
		Dbutil.executeQuery("update aipropertymatchesconsolidated join (select *,group_concat(propertyid) as ids from aipropertymatches where typeid=6 group by recordid) sd on (aipropertymatchesconsolidated.recordid=sd.recordid) set aipropertymatchesconsolidated.type6id=ids;",temptablecon);
		Dbutil.executeQuery("update aipropertymatchesconsolidated join (select *,group_concat(propertyid) as ids from aipropertymatches where typeid=7 group by recordid) sd on (aipropertymatchesconsolidated.recordid=sd.recordid) set aipropertymatchesconsolidated.type7id=ids;",temptablecon);
		Dbutil.executeQuery("update (select recordid,group_concat(propertyid) as ids from aipropertymatches where typeid=8 group by recordid) props join aipropertymatchesconsolidated on (aipropertymatchesconsolidated.recordid=props.recordid) set type8id=props.ids;",temptablecon);

	}

	public void getFullTextMatches(){
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		java.sql.Connection con2=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("truncate table aifulltextresults;");
			Dbutil.executeQuery("load index into cache aiitems;", con2);
			rs=Dbutil.selectQuery(rs,"select * from "+table+";",con);
			while (rs.next()){
				ftitems=FullTextScore(rs.getString(lookupfield), 0, 1);
				if (ftitems.itemid.size()>0) Dbutil.executeQuery("insert into aifulltextresults (tenant,recordid,itemid) values ("+tenant+","+rs.getInt(idfield)+","+ftitems.itemid.get(0)+");", con2);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}

	}

	public void checkTestResults(int itemtodebug){ 
		String query;
		String whereclause;
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("select * from aitestset"+(itemtodebug>0?" where wineid="+itemtodebug:"")+" order by wineid;", con);
			while (rs.next()){
				if (rs.getInt("itemid")!=rs.getInt("expecteditemid")){
					Dbutil.logger.warn("Wine "+rs.getInt("wineid")+" matches "+Dbutil.readValueFromDB("select * from aiitems where itemid="+rs.getInt("itemid"), "itemdescription")+" instead of "+Dbutil.readValueFromDB("select * from aiitems where itemid="+rs.getInt("expecteditemid"), "itemdescription"));
					if (itemtodebug==0) doTestRun(rs.getInt("wineid"));
				} else {
					Dbutil.logger.info("Wine "+rs.getInt("wineid")+" is OK.");
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}


	public void fillAiRecords(){ 
		Dbutil.executeQuery("delete from airecords where tenant="+tenant+";",temptablecon);
		String timeclause="";
		if (history>0){
			long pointintime = new java.util.Date().getTime();
			pointintime=pointintime-(long)history*1000*60*60*24; 
			Timestamp now = new java.sql.Timestamp(pointintime); 
			timeclause=" and createdate>'"+now+"' ";
		}
		if (refreshAll){
			Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+(skipmanualknownwineid?" where manualknownwineid=0":"")+" order by "+idfield+";",temptablecon);
		} else {
			Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+" where "+resultfield+"=0"+timeclause+(skipmanualknownwineid?" and manualknownwineid=0":"")+" order by "+idfield+";",temptablecon);
			
		}
	}

	public int fillAiRecordsWineLists(String list){ 
		int setid=Dbutil.readIntValueFromDB("select max(setid) as setid from winelists", "setid")+1;
		int id=0;
		float size;
		int vintage=0;
		ArrayList<String> sizestring;
		for (String wine:list.split("\n")){
			id++;
			vintage=0;
			wine=wine.replaceAll("\r", "");
			try{vintage=Integer.parseInt(Webroutines.getVintageFromName(wine).trim());} catch (Exception e){};
			if (vintage==0) try{vintage=Integer.parseInt(Webroutines.getRegexPatternValue("(?:^|\\D)(\\d\\d)(?:$|\\D)", wine).trim());} catch (Exception e){};
			sizestring=new ArrayList<String>();
			sizestring.add("");
			sizestring.add(wine);
			sizestring.add(wine);
			size=TableScraper.getSize(sizestring, "", wine, (float)0.0,true);

			Dbutil.executeQuery("insert into winelists(tenant,id,name,vintage,size,setid) values ("+tenant+","+id+",'"+Spider.SQLEscape((wine))+"',"+vintage+","+size+","+setid+");",temptablecon);
		}
		Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+" where setid="+setid+";",temptablecon);
		return setid;
	}

	public void fillAiRecordsDeletedKnownwine(int knownwineid){ 
		Dbutil.executeQuery("delete from airecords where tenant="+tenant+";",temptablecon);
		String timeclause="";
		Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+" where "+resultfield+"="+knownwineid+timeclause+(skipmanualknownwineid?" and (manualknownwineid=0 or manualknownwineid=-2)":"")+";",temptablecon);
		
	}
	
	public void deletedKnownwineUnrecognized(int knownwineid){ 
		Dbutil.executeQuery("update wines set knownwineid=0 where knownwineid="+knownwineid);
	}
	

	public void fillAiRecordsForShop(int shopid){ 
		Dbutil.executeQuery("delete from airecords where tenant="+tenant+";",temptablecon);
		String timeclause="";
		Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+" where shopid="+shopid+timeclause+(skipmanualknownwineid?" and manualknownwineid=0":"")+";",temptablecon);

	}


	public void fillAiRecordsDistinct(){ 
		Dbutil.executeQuery("delete from airecords where tenant="+tenant+";",temptablecon);
		if (refreshAll){
			Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+(skipmanualknownwineid?" where (manualknownwineid=0 or manualknownwineid=-2)":"")+" group by "+lookupfield+";",temptablecon);
		} else {
			Dbutil.executeQuery("insert into airecords(tenant,id,description,itemid) select "+tenant+","+idfield+","+lookupfield+",0 from "+table+" where "+resultfield+"=0"+(skipmanualknownwineid?" and manualknownwineid=0":"")+"  group by "+lookupfield+";",temptablecon);
		}
	}

	public static void recognizeKnownWines(int history, boolean refreshall){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Dbutil.logger.info("Building new wine recognition");
		Aitools aitools=new Aitools(1);
		
		aitools.refreshItems();
		Dbutil.logger.info("Finished building new wine recognition");
		Dbutil.logger.info("Starting job to recognize wines");
		Recognizer rec=new Recognizer("name","knownwineid","id","wines",1,false);
		rec.skipmanualknownwineid=true;
		rec.refreshAll=refreshall;
		rec.history=history;
		rec.fillAiRecords();
		if (history>0) Dbutil.executeQuery("insert ignore into airecords(tenant,id,description,itemid) select 1,id,name,0 from wines where manualknownwineid=-2 order by id;");
		rec.getProperties(0);
		rec.getPropertiesConsolidated();
		rec.restart();
		rec.restart=false;
		rec.measureperformance=false;
		ParallelRecognizer pr=new ParallelRecognizer(rec,Recognizer.desiredparallelprocesses);
		pr.startAnalyses();
		Dbutil.logger.info("End of analysis");
		if (!pr.problem){
			rec.getSingleMatches();
			rec.updateLftRgt(0);
			Knownwines.updateNumberOfWines();
			Knownwines.updateSameName();
			Dbutil.renewTable("materializedadvice");
			Knownwines.filterTooCheapKnownwines(5, 5);
			//Dbutil.renewTable("materializedadvice");
			Dbutil.executeQuery("update wines set manualknownwineid=0 where manualknownwineid=-2;");
			Dbutil.logger.info("Finished job to recognize wines");
		} else {
			Dbutil.logger.error("Problem while recognizing knownwines");
		}
	}

	//Niet gebruikt, manualknownwineid=-2 wordt automatisch opnieuw geanalyseerd
	public static void recognizeDeletedKnownWine(int knownwineid){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Recognizer rec=new Recognizer("name","knownwineid","id","wines",1,true);
		rec.skipmanualknownwineid=true;
		rec.refreshAll=false;
		rec.history=0;
		rec.debug=true;
		rec.fillAiRecordsDeletedKnownwine(knownwineid);
		rec.deletedKnownwineUnrecognized(knownwineid);
		Dbutil.logger.setLevel((Level) Level.DEBUG);
		new Thread(rec).start();
	}

	public static void recognizeWinesFromShop(int shopid){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Dbutil.logger.info("Starting job to recognize wines for shop "+shopid);
		Recognizer rec=new Recognizer("name","knownwineid","id","wines",1,true);
		rec.skipmanualknownwineid=true;
		rec.refreshAll=false;
		rec.history=0;
		rec.fillAiRecordsForShop(shopid);
		rec.refreshshopcache=shopid;
		new Thread(rec).start();
	}

	public static void recognizeRatedWines(){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Dbutil.logger.info("Starting job to recognize rated wines");
		Recognizer rec=new Recognizer("name","knownwineid","id","ratedwines",1,true);
		rec.skipmanualknownwineid=true;
		rec.refreshAll=false;
		rec.fillAiRecordsDistinct();
		rec.getProperties(0);
		rec.getPropertiesConsolidated();
		rec.restart=true;
		ParallelRecognizer pr=new ParallelRecognizer(rec,Recognizer.desiredparallelprocesses);
		pr.startAnalyses();
		rec.getSingleMatchesDistinct();
		Winerating.cleanRatedWines();
		Winerating.analyseWineRatings();
		Dbutil.executeQuery("update ratedwines set manualknownwineid=0 where manualknownwineid=-2;");
		Dbutil.renewTable("materializedadvice");
		Dbutil.executeQuery("update kbproducers join (select kbproducers.name,sum(if (rating>90,100-rating,0)) as score from ratedwines join knownwines on (ratedwines.knownwineid=knownwines.id) join kbproducers on (knownwines.producer=kbproducers.name) where knownwineid>0 group by knownwines.producer) sel on (sel.name=kbproducers.name) set kbproducers.score=sel.score;");
		Dbutil.logger.info("Finished job to recognize rated wines");

	}

	public static void recognizeProducerAdresses(){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		try {
			Recognizer rec = new Recognizer("name", "propertyid", "id",
					"producers", 1, true);
			rec.refreshAll = true;
			rec.fillAiRecords();
			rec.getProperties(1);
			//rec.getPropertiesConsolidated();
			rec.restart = true;
			//rec.getMatches(1);
			rec.getMatchesProducers(0);
			//Dbutil.renewTable("materializedadvice");
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
	}

	public static void recognizeWLTV(){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		try {
			Recognizer rec = new Recognizer("name", "knownwineid", "id",
					"winelibrarytv", 1, true);
			rec.refreshAll = false;
			rec.skipmanualknownwineid=false;
			rec.fillAiRecords();
			rec.getProperties(0);
			rec.getPropertiesConsolidated();
			rec.restart = true;
			rec.getMatches(0,1,0);
			rec.getSingleMatches();
			//Dbutil.renewTable("materializedadvice");
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}

	}

	public static void recognizeWotd(){
		try {
			Recognizer rec = new Recognizer("name", "knownwineid", "id",
					"wotd", 1, true);
			rec.refreshAll = false;
			rec.skipmanualknownwineid=false;
			rec.fillAiRecords();
			rec.getProperties(0);
			rec.getPropertiesConsolidated();
			rec.restart = true;
			rec.getMatches(0,1,0);
			rec.getSingleMatches();
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}

	}
	public static int analyzeUploadList(String list){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		int setid=0;
		try {
			Recognizer rec = new Recognizer("name", "knownwineid", "id",
					"winelists", 1, true);
			rec.refreshAll = false;
			setid=rec.fillAiRecordsWineLists(list);
			rec.getProperties(0);
			rec.getPropertiesConsolidated();
			rec.restart = true;
			rec.getMatches(0,1,0);
			rec.getSingleMatches();

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			setid=0;
		}
		return setid;
	}



	public static void doTestRun(int itemtodebug){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		try {
			Recognizer rec=new Recognizer("name","itemid","wineid","aitestset",1,false);
			rec.restart=false;
			rec.measureperformance=false;
			if (itemtodebug==0){
				rec.restart();
				rec.debug=false;
				rec.info=false;
				rec.skipmanualknownwineid=false;
				rec.fillAiRecords();
				rec.getProperties(0);
				rec.getPropertiesConsolidated();
				Dbutil.logger.setLevel(Level.WARN);
				ParallelRecognizer pr=new ParallelRecognizer(rec,8);
				if (itemtodebug>0) pr.parallellism=1;
				pr.startAnalyses();
				rec.getSingleMatches();
				rec.checkTestResults(itemtodebug);
			} else {
				rec.refreshAll=false;
				rec.debug=true;
				rec.info=true;
				Dbutil.logger.setLevel(Level.DEBUG);
				rec.getMatches(itemtodebug, 1,0);
			}



		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}

	}
	
	public static void buildAitools(){
		Aitools aitools=new Aitools(1);
		aitools.fillAiitems();
		aitools.fillRegionHierarchy();
		aitools.fillProperties();
		aitools.filterColorGrapetermsinCuvee();
		aitools.generateRecognizerFullText();
		aitools.manualRecognizer();
		aitools.fillAiitemproperties();
		aitools.fillPropsConsolidated();
	}
	
	public static void completeRecognition(){
		buildAitools();
		recognizeKnownWines(0,true);

	}


	public static void main (String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		int itemtodebug=0;
		String action="debugrun";
		if (args.length>0) itemtodebug=Integer.parseInt(args[0]);
		if (args.length>1) action=args[1];

		if (action.equals("debugrun")){
			if (itemtodebug==1) {
				doTestRun(0);
			} else	if (itemtodebug>1&&itemtodebug<=100) {
				doTestRun(itemtodebug);
			} else	if (itemtodebug>100) {
				Recognizer rec=new Recognizer("name","itemid","wineid","aiwines",1,false);
				Dbutil.logger.setLevel((Level) Level.ERROR);
				rec.debug=true;
				rec.info=true;
				Dbutil.logger.setLevel((Level) Level.DEBUG);
				try {
					rec.getMatches(itemtodebug,1,0);
				} catch (Exception e) {
					
				}
			} else {
				Recognizer.desiredparallelprocesses=3;
				recognizeRatedWines();
				//recognizeKnownWines(0,true);
				//recognizeRatedWines();

				//Dbutil.logger.setLevel((Level) Level.ERROR);
				//Recognizer rec=new Recognizer("name","knownwineid","id","wines",1,false);
				//Dbutil.logger.setLevel((Level) Level.INFO);

				//rec.fillAiRecords();
				//rec.getProperties(0);
				//rec.getPropertiesConsolidated();

				//Dbutil.logger.info("starting getMatches");
				//rec.debug=false;
				//rec.info=true;
				//rec.restart=false;
				//rec.measureperformance=false;
				//rec.refreshAll=true;
				//Knownwines.updateNumberOfWines();
				//Knownwines.updateSameName();
				//Dbutil.renewTable("materializedadvice");
				//Knownwines.filterTooCheapKnownwines(5, 5);
				//Dbutil.renewTable("materializedadvice");

				//rec.getMatches(itemtodebug);
				//rec.getSingleMatches();
				//rec.updateLftRgt();
				//recognizeProducerAdresses();
				//recognizeWLTV();
				//recognizeKnownWines();
				//recognizeRatedWines();

			} 
		} else if (action.equals("analyzePRD")){
			if (Configuration.serverrole.equals("DEV")){
				// Issues: 
				// Collation op dev en PRD kloppen niet, restore van aiproperties gaat mis of ringgel-s en s
				// kbregionhierarchy bouwen klopt niet helemaal op USA;
				// update hangt op laatste record, finisht nooit.
				// Restore handmatig van tabellen airecognizer, aiproperties, aiitems, aiitemproperties, aiitempropsconsolidated, kbregionhierarchy
				Recognizer.desiredparallelprocesses=3;
				copyTableFromPRDtoDEV("wines","id");
				copyTableFromPRDtoDEV("knownwines","id");
				copyTableFromPRDtoDEV("ratedwines","id");
				copyTableFromPRDtoDEV("airecognizermanual","recognizerid");
				completeRecognition();
				recognizeRatedWines();
				Dbutil.logger.info("Finished analyzing wines and knownwines.");
			}
		} else if (action.equals("updatePRD")){
			if (Configuration.serverrole.equals("DEV")){
				String query;
				ResultSet rs = null;
				Connection DEVcon = Dbutil.getDEVConnection();
				Connection PRDcon = Dbutil.getPRDConnection();
				try {
					int n=0;
					boolean finished=false;
					while (!finished){
					query = "select * from wines order by id desc limit "+n+",10000;";
					rs = Dbutil.selectQueryRowByRow(query, DEVcon);
					Dbutil.logger.info(n);
					if (!rs.isBeforeFirst()) finished=true;
					while (rs.next()) {
						n++;
						Dbutil.executeQuery("update low_priority wines set knownwineid="+rs.getInt("knownwineid")+", lft="+rs.getInt("lft")+", rgt="+rs.getInt("rgt")+" where id="+rs.getInt("id")+";", PRDcon);
					}
					Dbutil.closeRs(rs);
					}
					//query = "select * from ratedwines;";
					//rs = Dbutil.selectQueryRowByRow(query, DEVcon);
					//while (rs.next()) {
					//	Dbutil.executeQuery("update ratedwines set knownwineid="+rs.getInt("knownwineid")+" where id="+rs.getInt("id")+";", PRDcon);
					//}
				} catch (Exception e) {
					Dbutil.logger.error("", e);
				} finally {
					Dbutil.closeRs(rs);
					Dbutil.closeConnection(DEVcon);
					Dbutil.closeConnection(PRDcon);
				}
			}
		}

	}

	public static void copyTableFromPRDtoDEV(String tablename, String idcolumnname){
		if (Configuration.serverrole.equals("DEV")){
			Dbutil.logger.info("Starting to copy table "+tablename+" from production DB to development DB.");
			Connection DEVcon = Dbutil.getDEVConnection();
			Connection PRDcon = Dbutil.getPRDConnection();
			String query;
			ResultSet rs = null;
			String values;
			try {
				query = "truncate table "+tablename+";";
				Dbutil.executeQuery(query, DEVcon);
				query = "alter table "+tablename+" disable keys;";
				Dbutil.executeQuery(query, DEVcon);
				query="select * from "+tablename+" order by "+idcolumnname+";";
				rs = Dbutil.selectQueryRowByRow(query, PRDcon);
				while (rs.next()){
					values="";
					for (int i=1;i<=rs.getMetaData().getColumnCount();i++) if (rs.getMetaData().getColumnType(i)==java.sql.Types.TIMESTAMP||rs.getMetaData().getColumnType(i)==java.sql.Types.VARCHAR||rs.getMetaData().getColumnType(i)==java.sql.Types.LONGVARCHAR){
						values+=",'"+Spider.SQLEscape(rs.getString(i))+"'";
					} else {
						values+=","+Spider.SQLEscape(rs.getString(i));
					}
					values=values.substring(1);
					Dbutil.executeQuery("insert ignore into "+tablename+" values ("+values+");", DEVcon);
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				query = "alter table "+tablename+" enable keys;";
				Dbutil.executeQuery(query, DEVcon);
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(DEVcon);
				Dbutil.closeConnection(PRDcon);
			}
			Dbutil.logger.info("Finished copying table "+tablename+".");
		}
	}

	@Override
	public void run() {
		getProperties(0);
		getPropertiesConsolidated();
		restart=true;
		measureperformance=false;
		try {
			getMatches(0,1,0);
			getSingleMatches();
			if (refreshshopcache>0){
				updateLftRgt(refreshshopcache);
				StoreInfo.clearCache(refreshshopcache);
				StoreInfo.getStore(refreshshopcache);
				Dbutil.logger.info("Finished job to recognize wines for shop "+refreshshopcache);

			}
		} catch (Exception e) {
			
		}

	}

}
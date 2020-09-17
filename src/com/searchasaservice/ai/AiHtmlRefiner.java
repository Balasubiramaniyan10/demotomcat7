package com.searchasaservice.ai;

import java.sql.*;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;

public class AiHtmlRefiner {
	public int tenant;
	public Recognizer rec;

	public AiHtmlRefiner(int i, Recognizer rec){
		tenant=i;
		this.rec=rec;
	}

	public String refineProducersHtml(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		String query="";
		try{
			query="select *,group_concat(propertyid) as props from aiunrecognizedproducers join airecords on (recordid=airecords.id) group by recordid limit 600;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int propertyid=rs.getInt("propertyid");
				String name=rs.getString("name");
				query="select * from aiproperties where tenant="+tenant+" and typeid=1 and propertyid in ("+rs.getString("props")+");";
				rs2=Dbutil.selectQuery(rs2,query, con);
				sb.append("<h1>"+name+" ("+rs.getInt("recordid")+")</h1><a href='findproperty.jsp?term="+name+"' target='_blank'>Find similar</a><br/>");
				while (rs2.next()){
					sb.append("<a href='findproperty.jsp?term="+rs2.getInt("propertyid")+"' target='_blank'>"+rs2.getString("propertydescription")+"</a><br/>");
					
				}
				Dbutil.closeRs(rs2);
			}
			/*query="select airecognizermanual.fts as manualfts, airecognizer.fts as fts, airecognizer.regex as regex, airecognizer.regexexcl as regexexcl, aiproperties.propertyid,aiproperties.propertydescription from (select propertyid,max(score) as total from aiunrecognizedproducers group by propertyid order by total desc) al natural join aiproperties natural join airecognizer left join airecognizermanual on (aiproperties.propertyid=airecognizermanual.propertyid) having manualfts is null limit 600;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int propertyid=rs.getInt("propertyid");
				String propertydescription=rs.getString("propertydescription");
				query="select group_concat(concat('<br/>',name)) as name from (select * from aiunrecognizedproducers where propertyid="+propertyid+" order by score desc limit 10) asd join aiwines on (wineid=recordid) group by propertyid;";
				if (!"".equals(rs.getString("fts")+rs.getString("regex"))){
					rs2=Dbutil.selectQuery(rs2,query, con);
					if (rs2.next()){
						sb.append("<h1>"+propertydescription+"</h1><a href='findproperty.jsp?term="+propertydescription+"' target='_blank'>Find similar</a>");
						sb.append(rs2.getString("name"));
						sb.append("<table>");
						sb.append("<tr><td>Fulltext:</td><td><input type='text' size=60 id='fulltext"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs.getString("fts"))+"'></td></tr>");
						sb.append("<tr><td>Regex:</td><td><input type='text' size=60 id='regex"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs.getString("regex"))+"'></td></tr>");
						sb.append("<tr><td>Regex excl:</td><td><input type='text' size=60 id='regexexcl"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs.getString("regexexcl"))+"'></td></tr>");
						sb.append("<tr><td colspan='2'><a onClick='javascript:updateValue("+rs.getInt("propertyid")+");' style='cursor:pointer'> Update recognizer</a></td></tr>");
						sb.append("<tr><td colspan='2'>-----------------------</td></tr>");
						sb.append("</table>");
					}
					Dbutil.closeRs(rs2);
				}
			}
			*/
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();
	}

	public String analyzeCandidates(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		String query="";
		int recordid;
		String name;
		int lastitem=0;
		int producer;
		try{
			query="select * from airesults group by recordid having count(*)>1 order by RAND() limit 250;";
			rs=Dbutil.selectQuery(rs,query, con);
			while (rs.next()){
				recordid=rs.getInt("recordid");
				name=Dbutil.readValueFromDB("select * from airecords where id="+recordid, "name");
				sb.append("<h1>"+name+" ("+recordid+")</h1>");
				query="select * from airesults natural join aiitems natural join aipropertymatchesconsolidated join kbknownwines on (itemid=kbknownwines.id) where recordid="+recordid+" and tenant="+tenant+" order by match(itemdescription) against ('"+fts(name)+"') desc;";
				rs2=Dbutil.selectQuery(rs2,query, con);
				if (rs2.next()){
					if (rs2.getString("type1id")==null){
						producer=Dbutil.readIntValueFromDB("Select * from aiitempropsconsolidated where itemid="+rs2.getInt("itemid"), "type1id");
						sb.append(analyzeRedundantTerm(producer, name));
						
					}
				}
				rs2.beforeFirst();
				while (rs2.next()){
					sb.append("<a href='edititemproperties.jsp?itemid="+rs2.getInt("itemid")+"' target='_blank'>"+rs2.getString("itemdescription")+"</a>&nbsp;("+rs2.getString("bottles")+" bottles,"+rs2.getString("appellation")+","+rs2.getString("cuvee")+","+rs2.getString("vineyard")+","+rs2.getString("type")+","+rs2.getString("grapes")+")<br/>");
				}
				Dbutil.closeRs(rs2);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();
	}

	public String showitemswithproperty(int propertyid){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query="";
		if (propertyid>0){
			try{
				query="select * from aiitemproperties natural join aiitems where tenant="+tenant+" and propertyid="+propertyid+";";
				rs=Dbutil.selectQuery(rs,query, con);
				while (rs.next()){
					sb.append("<a href='edititemproperties.jsp?itemid="+rs.getInt("itemid")+"' target='_blank'>"+rs.getString("itemdescription")+"</a><br/>");
				}
				Dbutil.closeRs(rs);
			} catch (Exception e){
				Dbutil.logger.error("Problem:",e);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return sb.toString();
	}

	public static String analyzeRedundantTerm(int propertyid,String name){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String newfts="";
		String oldfts="";
		String oldftsaccented="";
		int typeid=0;
		try{
			rs=Dbutil.selectQuery("select * from aiproperties natural join airecognizer where propertyid="+propertyid, con);
			if (rs.next()){
				typeid=rs.getInt("typeid");
				oldfts=fts(rs.getString("fts"));
				oldftsaccented=oldfts;
				String[] terms=rs.getString("fts").split(" ?\\+");
				for (String term:terms){
					if (!fts(term).equals("+")){
						if (name.contains(fts(term).substring(1))){
							newfts+=" "+fts(term);
						} else {
							oldftsaccented=oldftsaccented.replace(fts(term), "<b>"+fts(term)+"</b>");
						}
					}
				}
			}
			Dbutil.closeRs(rs);
			if (!newfts.trim().equals("")&&!oldfts.trim().equals(newfts.trim())){
				rs=Dbutil.selectQuery("select * from aiproperties where typeid="+typeid+" and propertyid="+propertyid+";", con);
				while (rs.next()){
					sb.append(rs.getString("propertydescription")+" has fts "+oldftsaccented+"<br/>");
				}
				sb.append("Suggested new fts:"+newfts+"<br/>");
				Dbutil.closeRs(rs);
				rs=Dbutil.selectQuery("select * from aiproperties where typeid="+typeid+" and propertyid!="+propertyid+" and match(propertydescription) against ('"+newfts+"' in boolean mode);", con);
				while (rs.next()){
					sb.append(rs.getString("propertydescription")+"<br/>");
				}
			}
		}catch(Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			
			Dbutil.closeConnection(con);
		}

		return sb.toString();
	}
	
	
	public String refineNoCandidatesHtml(int n){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		String query="";
		String props;
		try{
			query="select *,concat(ifnull(type1id,''),',',ifnull(type2id,''),',',ifnull(type5id,''),',',ifnull(type6id,''),',',ifnull(type7id,''),',',ifnull(type8id,'')) as props from ainocandidates natural join aipropertymatchesconsolidated join airecords on (recordid=airecords.id) limit "+n+";";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				props=rs.getString("props");
				if (props!=null) {
					props=props.replaceAll("^,+","").replaceAll(",+$","").replaceAll(",+",",");
				} else {
					Dbutil.logger.info(rs.getInt("recordid"));
				}
				query="select typedescription,aiproperties.propertydescription,propertyid from aiproperties natural join aipropertytypes where aiproperties.propertyid in ("+props+");";
				rs2=Dbutil.selectQuery(rs2,query, con);
				sb.append("<h1>Record "+rs.getInt("recordid")+": "+rs.getString("name")+"</h1>");
				sb.append("Found properties:<br/>");
				while (rs2.next()){
					sb.append(rs2.getString("typedescription")+": "+rs2.getString("propertydescription")+"&nbsp;<a href='findproperty.jsp?term="+Webroutines.URLEncode(rs2.getString("propertyid"))+"' target=_blank'>Edit</a>&nbsp;<a href='findproperty.jsp?term="+Webroutines.URLEncode(rs2.getString("propertydescription"))+"' target=_blank'>Find similar</a>&nbsp;<a href='showitemswithproperty.jsp?propertyid="+Webroutines.URLEncode(rs2.getString("propertyid"))+"' target=_blank'>Show all item with this property</a><br/>");
				}
				Dbutil.closeRs(rs2);
				query="select itemid,itemdescription,ai1.propertyid as prodid,  ai1.propertydescription as proddesc,  ai2.propertyid as appid,  ai2.propertydescription as appdesc from (Select * from aiitems where match(itemdescription) against ('+"+Aitools.filterPunctuation(rs.getString("name")).trim().replaceAll("\\s+", " +")+"')>5 order by match(itemdescription) against ('+"+Aitools.filterPunctuation(rs.getString("name")).trim().replaceAll("\\s+", " +")+"') desc limit 10) sel natural join aiitempropsconsolidated join aiproperties ai1 on (type1id=ai1.propertyid) join aiproperties ai2 on (type2id=ai2.propertyid);";
				rs2=Dbutil.selectQuery(rs2,query, con);
				while (rs2.next()){
					sb.append("<a href='edititemproperties.jsp?itemid="+rs2.getInt("itemid")+"' target='_blank'>"+rs2.getString("itemdescription")+"</a><br/>");
					sb.append("Producer: "+rs2.getString("proddesc")+", Appellation: "+rs2.getString("appdesc")+"<br/>");
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();
	}
	
	public static String fts(String term){
		return "+"+Aitools.filterPunctuation(term).trim().replaceAll("\\s+", " +").replaceAll("\\+-", " -");
	}
	
	public String refineProducersHTML(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		int id=0;
		String query="";
		try{
			query="select producers.propertyid,name,propertydescription,count(*) from producers join aiproperties on (producers.propertyid=aiproperties.propertyid) group by producers.propertyid order by count(*) desc limit 100;";
			rs=Dbutil.selectQuery(query, con2);
			while (rs.next()){
				sb.append(findpropertyHtml(rs.getString("propertyid")));
			}

			
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();

	}
	
	

	public String findpropertyHtml(String term){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		int id=0;
		String query="";
		try{
			try {
				id=Integer.parseInt(term);
			} catch (Exception e) {
			}
			if (term!=null&&!term.equals("")){	
				if (id==0){
					query="select * from (select *,match (propertydescription) against('+"+Aitools.filterPunctuation(term).trim().replaceAll("\\s+", " +")+"') as score from aiproperties where match (propertydescription) against('+"+Aitools.filterPunctuation(term).trim().replaceAll("\\s+", " +")+"') order by score desc limit 40) asd order by typeid;";
				} else {
					query="select * from aiproperties where propertyid="+id+";";
				}
				rs=Dbutil.selectQuery(query, con);
				while (rs.next()){
					int propertyid=rs.getInt("propertyid");
					String propertydescription=rs.getString("propertydescription");
					query="select typedescription,IFNULL(airecognizermanual.fts,airecognizer.fts) as fts, IFNULL(airecognizermanual.regex,airecognizer.regex) as regex, IFNULL(airecognizermanual.regexexcl,airecognizer.regexexcl) as regexexcl, aiproperties.propertyid,aiproperties.propertydescription from aiproperties natural join aipropertytypes natural join airecognizer left join airecognizermanual on (aiproperties.propertyid=airecognizermanual.propertyid) where aiproperties.propertyid="+propertyid+";";
					rs2=Dbutil.selectQuery(rs2,query, con);
					if (rs2.next()){
						sb.append("<h1>"+propertydescription+" ("+rs2.getString("typedescription")+", id="+rs.getInt("propertyid")+")</h1>");
						sb.append("<table>");
						sb.append("<tr><td>Fulltext:</td><td><input type='text' size=60 id='fulltext"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs2.getString("fts"))+"'></td></tr>");
						sb.append("<tr><td>Regex:</td><td><input type='text' size=60 id='regex"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs2.getString("regex"))+"'></td></tr>");
						sb.append("<tr><td>Regex excl:</td><td><input type='text' size=60 id='regexexcl"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs2.getString("regexexcl"))+"'></td></tr>");
						sb.append("<tr><td colspan='2'><a onClick='javascript:updateValue("+rs.getInt("propertyid")+");' style='cursor:pointer'> Update recognizer</a></td></tr>");
						sb.append("<tr><td colspan='2'>-----------------------</td></tr>");
						sb.append("</table>");
					}
					Dbutil.closeRs(rs2);
				}
				if (id>0){
					query="select * from aiitemproperties natural join aiitems where propertyid="+id+";";
					rs2=Dbutil.selectQuery(rs2,query, con);
					while (rs2.next()){
						sb.append("<br/>"+rs2.getString("itemdescription"));
					}
					Dbutil.closeRs(rs2);
				}

			}
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();
	}

	public void analyseUnrecognizedProducers(){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		String query="";
		try{
			Dbutil.executeQuery("delete from aiunrecognizedproducers");
			query="select * from aipropertymatchesconsolidated join aiwines on (aipropertymatchesconsolidated.recordid=aiwines.wineid) where type1id is null;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				int recordid=rs.getInt("recordid");
				String recordname=rs.getString("name");
				query="Select aiproperties.propertyid as propertyid,match(aiproperties.propertydescription) against ('+"+Aitools.filterPunctuation(recordname).trim().replaceAll("\\s+", " +")+"') as score from aiproperties  where aiproperties.typeid=1 and match(aiproperties.propertydescription) against ('+"+Aitools.filterPunctuation(recordname).trim().replaceAll("\\s+", " +")+"')>10 group by propertyid order by score desc limit 10;";
				rs2=Dbutil.selectQuery(rs2,query, con);
				while (rs2.next()){
					Dbutil.executeQuery("insert into aiunrecognizedproducers (propertyid,recordid,score) values("+rs2.getInt("propertyid")+","+recordid+","+rs2.getDouble("score")+");", con2);
				}
				Dbutil.closeRs(rs2);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
	}

	public String refineRecognizerHtml(int numofrecords){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		ResultSet rs2=null;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		String query="";
		int recordid=0;
		String recordname;
		String itemids;
		int producer;

		try{
			query="Select recordid,description,group_concat(airesults.itemid) as itemids from (SELECT recordid FROM airesults a group by recordid having count(*)>1 order by count(*) limit "+numofrecords+") asd natural join airesults join airecords on (asd.recordid=airecords.id) group by recordid;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				recordid=rs.getInt("recordid");
				recordname=rs.getString("description");
				itemids=rs.getString("itemids");
				sb.append("<h1>"+recordname+" (recordid "+recordid+")</h1>");
				sb.append("Candidates found:<br/>");
				/*
				query="Select * from aiitems where itemid in ("+itemids+");";
				rs2=Dbutil.selectQuery(rs2,query, con);
				if (rs2.isBeforeFirst()){
					
				}
				while (rs2.next()){
					sb.append("<a href='edititemproperties.jsp?itemid="+rs2.getInt("itemid")+"' target='_blank'>"+rs2.getString("itemdescription")+"</a><br/>");
				}
				Dbutil.closeRs(rs2);
				*/
				query="select * from airesults natural join aiitems natural join aipropertymatchesconsolidated join kbknownwines on (itemid=kbknownwines.id) where recordid="+recordid+" and tenant="+tenant+" order by match(itemdescription) against ('"+fts(recordname)+"') desc;";
				rs2=Dbutil.selectQuery(rs2,query, con);
				while (rs2.next()){
					sb.append("<a href='edititemproperties.jsp?itemid="+rs2.getInt("itemid")+"' target='_blank'>"+rs2.getString("itemdescription")+"</a>&nbsp;("+rs2.getString("bottles")+" bottles,"+rs2.getString("appellation")+","+rs2.getString("cuvee")+","+rs2.getString("vineyard")+","+rs2.getString("type")+","+rs2.getString("grapes")+")<br/>");
				}
				Dbutil.closeRs(rs2);
				query="Select * from aiitems where itemid not in ("+itemids+") and match(itemdescription) against ('+"+Aitools.filterPunctuation(recordname).trim().replaceAll("\\s+", " +")+"')>5 order by match(itemdescription) against ('+"+Aitools.filterPunctuation(recordname).trim().replaceAll("\\s+", " +")+"') desc limit 10;";
				rs2=Dbutil.selectQuery(rs2,query, con);
				if (rs2.isBeforeFirst()){
					sb.append("Candidates not found or selected:<br/>");
				}
				while (rs2.next()){
					sb.append("<a href='edititemproperties.jsp?itemid="+rs2.getInt("itemid")+"' target='_blank'>"+rs2.getString("itemdescription")+"</a><br/>");
				}
				Dbutil.closeRs(rs2);
				
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}
		return sb.toString();
	}


	
	public String editItemPropertiesHtml(String itemid){
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query="";
		String[] propertyids;
		try {
			propertyids = Dbutil.readValueFromDB("select group_concat(propertyid) as ids from aiitemproperties where itemid="+itemid+" order by typeid;","ids").split(",");
		try{
			sb.append("<table>");
			for (String prop:propertyids){
				query="Select * from aiproperties natural join airecognizermanual natural join aipropertytypes where propertyid="+prop+";";
				rs=Dbutil.selectQuery(rs,query, con);
				if (rs.next()){
					rs.beforeFirst();
				} else {
					query="Select * from aiproperties natural join airecognizer natural join aipropertytypes where propertyid="+prop+";";
					rs=Dbutil.selectQuery(rs,query, con);
				}
				while (rs.next()){
					sb.append("<tr><td colspan='2'>"+rs.getString("propertydescription")+" ("+rs.getString("typedescription")+", id="+rs.getString("propertyid")+")</td></tr>");
					sb.append("<tr><td>Fulltext:</td><td><input type='text' size=60 id='fulltext"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs.getString("fts"))+"'></td></tr>");
					sb.append("<tr><td>Regex:</td><td><input type='text' size=60 id='regex"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs.getString("regex"))+"'></td></tr>");
					sb.append("<tr><td>Regex excl:</td><td><input type='text' size=60 id='regexexcl"+rs.getInt("propertyid")+"' value='"+Webroutines.escape(rs.getString("regexexcl"))+"'></td></tr>");
					sb.append("<tr><td colspan='2'><a onClick='javascript:updateValue("+rs.getInt("propertyid")+");' style='cursor:pointer'> Update recognizer</a></td></tr>");
					sb.append("<tr><td colspan='2'>-----------------------</td></tr>");
				}
			}
			sb.append("</table>");

		} catch (Exception e){
			Dbutil.logger.error("Problem:",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		} catch (Exception e1) {
		}
		
		return sb.toString();
	}

	public boolean updateAiRecognizerManual(int propertyid,String fulltext,String regex, String regexexcl){
		String query;
		if (Dbutil.readIntValueFromDB("Select * from airecognizermanual where tenant="+tenant+" and propertyid="+propertyid+";", "propertyid")!=propertyid){
			query="insert into airecognizermanual(tenant,propertyid,propertydescription,typeid,fts,regex,regexexcl) select "+tenant+",propertyid,propertydescription,typeid,fts,regex,regexexcl from airecognizer natural join aiproperties where tenant="+tenant+" and propertyid="+propertyid+";";
			Dbutil.executeQuery(query);
		}
		query="update airecognizer set fts='"+Spider.SQLEscape(fulltext)+"', regex='"+Spider.SQLEscape(regex)+"', regexexcl='"+Spider.SQLEscape(regexexcl)+"' where tenant="+tenant+" and propertyid="+propertyid+";";
		Dbutil.executeQuery(query);
		query="update airecognizermanual set fts='"+Spider.SQLEscape(fulltext)+"', regex='"+Spider.SQLEscape(regex)+"', regexexcl='"+Spider.SQLEscape(regexexcl)+"' where tenant="+tenant+" and propertyid="+propertyid+";";
		int k=Dbutil.executeQuery(query);
		if (k>0) {
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "select * from aiitemproperties where propertyid="+propertyid;
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					Knownwines.queueForAnalysis(rs.getInt("itemid"));
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
			return true;
		}
		return false;
	}

	public boolean disOrEnableKbKnownwine(int id,boolean disable){
		int result=0;
		if (disable){
			Knownwines.disableKnownwine(id);
		} else {
			result=Dbutil.executeQuery("update knownwines set disabled=0 where id="+id);
		}
		if (result>0) return true;
		return false;
	}
	public static void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		AiHtmlRefiner r=new AiHtmlRefiner(1, new Recognizer("name","itemid","wineid","aiwines",1,true));
		r.analyseUnrecognizedProducers();
	}

}

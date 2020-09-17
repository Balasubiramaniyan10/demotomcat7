package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.sms.Sms;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class KnownwineEditor {
	int knownwineid;
	String wine;
	int regionid;
	boolean disabled;
	String type;
	int grapeid;
	int producerid;
	String cuvee;
	String vineyard;
	String code;
	String winerynote;
	boolean overridesecurity=false;
	boolean valid=false;

	String producer;
	String appellation;
	String locale;
	int lft;
	int rgt;
	String color;
	String dryness;
	boolean sparkling;
	String grapename;
	boolean samename=false;
	public String errormessage="";

	public KnownwineEditor(){
	
	}
		
	
	
	public KnownwineEditor(int knownwineid,String code){
		this.knownwineid=knownwineid;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select knownwines.*, grapes.id as grapeid,kbregionhierarchy.id as regionid, kbproducers.id as producerid from knownwines left join grapes on (knownwines.grapes=grapes.grapename) left join kbregionhierarchy on (knownwines.locale=region) left join kbproducers on (knownwines.producer=kbproducers.name) where knownwines.id="+knownwineid+";";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				wine=rs.getString("wine");
				regionid=rs.getInt("regionid");
				grapeid=rs.getInt("grapeid");
				disabled=rs.getBoolean("disabled");
				type=rs.getString("type");
				producerid=rs.getInt("producerid");
				cuvee=rs.getString("cuvee");
				vineyard=rs.getString("vineyard");
				winerynote=rs.getString("winerynote");
				this.code=code;
				
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}
	
	
	public String check(){
		valid=true;
		String errors="";
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {

			//Security
			if (!overridesecurity&&(code==null||code.equals("")||!code.equals(Dbutil.readValueFromDB("select * from kbproducers where id="+producerid, "edithashcode")))){
				valid=false;
				//Dbutil.logger.info(code);
				//Dbutil.logger.info(Dbutil.readValueFromDB("select * from kbproducers where id="+producerid, "edithashcode"));
				
				errors+="Incorrect access code or insufficient priviliges to edit this wine.<br/>";
			}
			
			
			//Producer
			producer=Dbutil.readValueFromDB("select * from kbproducers where id="+producerid, "name");
			
			//Wine name
			if (getWine()==null){
				valid=false;
				errors+="Wine name may not be empty.<br/>";
			} else if (getWine().length()<6){
				valid=false;
				errors+="Wine name may not be shorter than 6 characters.<br/>";
			} else if (!getWine().startsWith(producer)){
				valid=false;
				errors+="Wine name should start with the name of your winery ("+producer+").<br/>";
			}

			// region
			if (getRegionid()<101){
				valid=false;
				errors+="Region/appellation must be set.<br/>";
			} else {
				query = "select * from kbregionhierarchy where id="+getRegionid();
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					locale=rs.getString("region");
					appellation=rs.getString("shortregion");
					lft=rs.getInt("lft");
					rgt=rs.getInt("rgt");
					if (locale.length()<3||appellation.length()<2||rgt<1||lft<1){
						valid=false;
						errors+="Region/appellation is incorrect.<br/>";
					}
				} else {
					valid=false;
					errors+="Region/appellation is incorrect.<br/>";
				}
			}

			if (getType()==null||getType().length()<3){
				valid=false;
				errors+="Wine type is incorrect.<br/>";
			} else {
				query = "select * from knownwines where type='"+Spider.SQLEscape(getType())+"' limit 1;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					color=rs.getString("color");
					dryness=rs.getString("dryness");
					sparkling=rs.getBoolean("sparkling");
				} else {
					valid=false;
					errors+="Wine type is incorrect.<br/>";
				}
			}

			if (grapeid>0){
				query = "select * from grapes where id="+getGrapeid()+";";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					grapename=rs.getString("grapename");
				} else {
					valid=false;
					errors+="Grape type is incorrect.<br/>";
				}
			} else {
				valid=false;
				errors+="Grapes must be set.<br/>";

			}
			if (cuvee==null) cuvee="";
			if (vineyard==null) vineyard="";

			if (Dbutil.readIntValueFromDB("select * from knownwines where wine='"+Spider.SQLEscape(wine)+"' and id!="+knownwineid, "id")>0){
				samename=true;
				errors+="Warning: a wine with an identical name already exists. Consider changing the wine name to make it different.<br/>";
			}

		} catch (Exception e) {
			valid=false;
			errors="Unknown error has occured, please contact jasper@vinopedia.com";
			Dbutil.logger.error("", e);

		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return errors;

	}
	
	public static void handleSameName(String oldname,String newname){
		int i;
		i=0;
		if (oldname!=null) i=Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where wine='"+Spider.SQLEscape(oldname)+"' and disabled=0;","thecount");
		if (i>1) {
			Dbutil.executeQuery("update knownwines set samename=1 where  wine='"+Spider.SQLEscape(oldname)+"';");
		} else {
			Dbutil.executeQuery("update knownwines set samename=0 where  wine='"+Spider.SQLEscape(oldname)+"';");
		}
		i=0;
		if (newname!=null) i=Dbutil.readIntValueFromDB("select count(*) as thecount from knownwines where wine='"+Spider.SQLEscape(newname)+"' and disabled=0;","thecount");
		if (i>1) {
			Dbutil.executeQuery("update knownwines set samename=1 where  wine='"+Spider.SQLEscape(newname)+"';");
		} else {
			Dbutil.executeQuery("update knownwines set samename=0 where  wine='"+Spider.SQLEscape(newname)+"';");
		}
	}

	public boolean save(HttpServletRequest request){
		check();
		errormessage="";
		if (valid){
			String oldname=null;
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				ChangeLog log=new ChangeLog();
				Context c=new Context(request);
				log.tenant=1;
				log.userid=c.userid;
				if (log.userid==null) log.userid="Winery: "+producer;
				log.tablename="knownwines";
				log.rowid=knownwineid;
				if (knownwineid>0){
					rs=Dbutil.selectQuery("select * from knownwines where id="+knownwineid, con);
				}
				log.setValueOld(rs);
				Dbutil.closeRs(rs);
				log.date=new java.sql.Timestamp(new java.util.Date().getTime()); 
				int result=0;
				if (knownwineid==0){
					query="insert into knownwines (wine,appellation,disabled,type,grapes,producer,locale,cuvee,vineyard,bottles,"+
							"color,dryness,sparkling,samename,producerids,numberofwines,lft,rgt,winerynote)"+
							" values ('"+Spider.SQLEscape(wine)+"',"+
							"'"+Spider.SQLEscape(appellation)+"',"+
							disabled+","+
							"'"+Spider.SQLEscape(type)+"',"+
							"'"+Spider.SQLEscape(grapename)+"',"+
							"'"+Spider.SQLEscape(producer)+"',"+
							"'"+Spider.SQLEscape(locale)+"',"+
							"'"+Spider.SQLEscape(cuvee)+"',"+
							"'"+Spider.SQLEscape(vineyard)+"',"+
							"0,"+
							"'"+Spider.SQLEscape(color)+"',"+
							"'"+Spider.SQLEscape(dryness)+"',"+
							sparkling+","+
							samename+","+
							"'',"+
							"0,"+
							lft+","+
							rgt+","+
							"'"+Spider.SQLEscape(getWinerynote())+"');";
					result=Dbutil.executeQueryWithExceptions(query, con);
					if (result>0){
						rs=Dbutil.selectQuery("select LAST_INSERT_ID() as knownwineid;", con);
						rs.next();
						knownwineid=rs.getInt("knownwineid");
						
					}
							
				} else {
					oldname=Dbutil.readValueFromDB("select wine from knownwines where id="+knownwineid, "wine");
					query="update knownwines set "+
					"wine='"+Spider.SQLEscape(wine)+"',"+
					"appellation='"+Spider.SQLEscape(appellation)+"',"+
					"disabled="+isDisabled()+","+
					"type='"+Spider.SQLEscape(type)+"',"+
					"grapes='"+Spider.SQLEscape(grapename)+"',"+
					"locale='"+Spider.SQLEscape(locale)+"',"+
					"cuvee='"+Spider.SQLEscape(getCuvee())+"',"+
					"vineyard='"+Spider.SQLEscape(getVineyard())+"',"+
					"color='"+Spider.SQLEscape(color)+"',"+
					"dryness='"+Spider.SQLEscape(dryness)+"',"+
					"sparkling="+sparkling+","+
					"samename="+samename+","+
					"lft="+lft+","+
					"rgt="+rgt+","+
					"winerynote='"+Spider.SQLEscape(getWinerynote())+"' where id="+knownwineid+";";
					result=Dbutil.executeQueryWithExceptions(query, con);
					 
				}
				if (result>0) {
					rs=Dbutil.selectQuery("select * from knownwines where id="+knownwineid+";", con);
					if (rs.next()){ 
						log.setValueNew(rs);
						if (request.isUserInRole("admin")){
							log.userid=request.getRemoteUser();
						} else {
							log.userid="Producer "+getProducerid();
						}
						log.save();
						Knownwines.queueProducerForAnalysis(producerid);
						handleSameName(oldname, wine);
						return true;
					}
				} else {
					throw new Exception();
				}
				
			} catch(java.sql.SQLException e){
				if (e.getMessage().contains("for key 'Uniq'")){
					errormessage="Another wine with the same name, appellation and wine type already exists! We did not save this wine. If this really is a different wine, please use a wine name that distinguishes these wines and save again.";
				} else {
					Sms sms=new Sms();
					sms.setSms("Problem while saving a change in Knownwine by producer "+producer);
					sms.send();
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
				Sms sms=new Sms();
				sms.setSms("Problem while saving a change in Knownwine by producer "+producer);
				sms.send();
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}

		}
		return false;
	}

	public static HashMap<Integer,String> getOptions(String tablename, String idfield, String valuefield){
		LinkedHashMap<Integer,String> map=new LinkedHashMap<Integer, String>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from "+tablename+" order by "+valuefield;
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				map.put(rs.getInt(idfield), rs.getString(valuefield));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return map;
	}


	public static HashSet<String> getOptions(String tablename, String valuefield){
		HashSet<String> set=new HashSet<String>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select distinct("+valuefield+") from "+tablename+" order by "+valuefield;
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				set.add(rs.getString(valuefield));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return set;
	}

	public int getKnownwineid() {
		return knownwineid;
	}

	public void setKnownwineid(int knownwineid) {
		this.knownwineid = knownwineid;
	}
	
	public String getProducer(){
		return producer;
	}

	public String getWine() {
		if (wine==null) wine=producer+" ";
		return wine;
	}
	public void setWine(String wine) {
		try {
			wine=Webroutines.filterUserInput(wine);
			wine = HtmlFilter.getHtmlFilter().filterText(wine);
			wine=Spider.unescape(wine);
			this.wine = wine;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			this.wine="";
		}
	}
	public int getRegionid() {
		return regionid;
	}
	public void setRegionid(int regionid) {
		this.regionid = regionid;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getType() {
		
		return type;
	}
	public void setType(String type) {
		try {
			type = Spider.unescape(HtmlFilter.getHtmlFilter().filterText(type));
			this.type = type;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			this.type="";
		}
	}

	public String getWinerynote() {
		if (winerynote==null) winerynote="";
		return winerynote.replaceAll("<br />","\n");
	}
	public void setWinerynote(String winerynote) {
		try {
			if (winerynote==null) winerynote="";
			winerynote=winerynote.replaceAll("\r\n","<br />").replaceAll("\r","<br />").replaceAll("\n","<br />");
			winerynote = HtmlFilter.getHtmlFilter().filterText(winerynote);
			this.winerynote = winerynote;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			this.winerynote="";
		}
	}
public int getGrapeid() {
		return grapeid;
	}
	public void setGrapeid(int grapeid) {
		this.grapeid = grapeid;
	}
	public int getProducerid() {
		return producerid;
	}
	public void setProducerid(int producerid) {
		this.producerid = producerid;
	}
	public String getCuvee() {
		if (cuvee==null) cuvee="";
		return cuvee;
	}
	public void setCuvee(String cuvee) {
		try {
			if (cuvee==null) cuvee="";
			cuvee = HtmlFilter.getHtmlFilter().filterText(cuvee);
			this.cuvee = cuvee;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			this.cuvee="";
		}
	}
	public String getVineyard() {
		if (vineyard==null) vineyard="";

		return vineyard;
	}
	public void setVineyard(String vineyard) {
		try {
			vineyard = HtmlFilter.getHtmlFilter().filterText(vineyard);
			this.vineyard = vineyard;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			this.vineyard="";
		}
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}



	public boolean isOverridesecurity() {
		return overridesecurity;
	}



	public void setOverridesecurity(HttpServletRequest request) {
		if (request.isUserInRole("admin")) overridesecurity=true;
		
	}


}

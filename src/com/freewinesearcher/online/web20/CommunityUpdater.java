package com.freewinesearcher.online.web20;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Auditlogger;
import com.freewinesearcher.online.ChangeLog;
import com.freewinesearcher.online.HtmlFilter;
import com.freewinesearcher.online.Sanitizer;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.Webroutines;

public class CommunityUpdater {
	String tablename;
	String contentcolumn;
	String idcolumn;
	int id;
	String accesscode;
	String content;
	String elementid;
	//Context context;
	Auditlogger al;
	public static final HashMap<String,String> authorizedelements = new HashMap<String, String>();
	public static final HashMap<String,String> elementexplanation = new HashMap<String, String>();
	static{
		loadAuthorizedelements();
		loadElementExplanation();
	}
	
	
	
	
	public String update(HttpServletRequest request){
		String type=authorizedelements.get(tablename+"/"+contentcolumn);
		if (type==null) return "Error";
		if (tablename==null||idcolumn==null||id==0||content==null||al==null) return "Error";
		if (tablename.equals("")||idcolumn.equals("")||content.equals("")) return "Error";
		boolean allowed=false;
		allowed=al.isUserAuthorized(tablename+"/"+contentcolumn, request);
		if (!allowed) allowed=validAccessCode();
		if (allowed){

				String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				if (type.equals("html")){
					content=HtmlFilter.getHtmlFilter().filterHtml(content);
					//content=s.filter(Spider.unescape(content));
					//content=Sanitizer.filterInvalidImages(content);
				} else {
					content=HtmlFilter.getHtmlFilter().filterText(content);
					//content=Webroutines.filterUserInput(content);
				}
				
				

				Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime()); 
				query = "select * from "+tablename+" where +"+idcolumn+"='"+id+"';";
				rs=Dbutil.selectQuery(query, con);
				ChangeLog log=new ChangeLog(new Context(request),rs,"id");
				log.setValueOld(rs);
				Dbutil.closeRs(rs);
				log.date=now;
				int result=Dbutil.executeQuery("update "+tablename+" set "+contentcolumn+"='"+Spider.SQLEscape(content)+"' where "+idcolumn+"="+id);
				if (result>0) {
					rs=Dbutil.selectQuery("select * from "+tablename+" where "+idcolumn+"="+id+";", con);
					if (rs.next()){
						log.setValueNew(rs);
						log.save();
						finalizeChange();
						return rs.getString(contentcolumn);
					}
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
			
		} else {
			Dbutil.logger.info(accesscode+" "+Dbutil.readValueFromDB("select * from "+tablename+" where "+idcolumn+"="+id+";", "edithashcode"));
		}
		
		return "Error";
	}
	
	private void finalizeChange(){
		if ((tablename+"/"+contentcolumn).equals("kbproducers/address")){
			String[] coordinates=Coordinates.getCoordinates(Dbutil.readValueFromDB("select * from "+tablename+" where "+idcolumn+"="+id+";", "address"));
			if (coordinates!=null&&coordinates.length==3) Dbutil.executeQuery("update "+tablename+" set lon="+coordinates[0]+", lat="+coordinates[1]+", accuracy="+coordinates[2]+" where "+idcolumn+"="+id+";");
		}
		if ((tablename+"/"+contentcolumn).equals("shops/description")){
			StoreInfo.clearCache(id);
		}
	}
	
	private static void loadAuthorizedelements() {
		authorizedelements.put("kbproducers/description","html");
		authorizedelements.put("kbproducers/address","text");
		authorizedelements.put("kbproducers/telephone","text");
		authorizedelements.put("kbproducers/website","text");
		authorizedelements.put("kbproducers/email","text");
		authorizedelements.put("kbproducers/visiting","text");
		authorizedelements.put("kbproducers/twitter","text");
		authorizedelements.put("shops/description","html");
		authorizedelements.put("shops/costperclick","text");
		authorizedelements.put("kbregionhierarchy/description","html");
		authorizedelements.put("helpscreens/text","html");
		
	}

	private static void loadElementExplanation() {
		elementexplanation.put("kbproducers/description","This is a description of the winery. Things you may want to touch: the goals and philosophy in winemaking, typicity of the wines, history of the winery etc.");
		elementexplanation.put("kbproducers/address","The address of the winery. If there is more than one location, the address where people can go to visit the winery.");
		elementexplanation.put("kbproducers/telephone","");
		elementexplanation.put("kbproducers/email","");
		elementexplanation.put("kbproducers/website","Your web site");
		elementexplanation.put("kbproducers/twitter","If you use Twitter to keep people informed, please enter your Twitter account name");
		elementexplanation.put("kbproducers/visiting","Please indicate: is wine tasting possible, do you have a cellar tour, do you sell your wines directly. Also indicate conditions such as opening hours, whether an appointment is necessary and whether there is a charge for the tour or tasting.");
		elementexplanation.put("shops/description","");
		elementexplanation.put("shops/costperclick","");
		elementexplanation.put("kbregionhierarchy/description","");
		elementexplanation.put("helpscreens/text","");
		
	}

	public String text2html(String input){
		input=input.replaceAll("\r", "<br/>");
		return input;
	}
	
	public String getHtml(HttpServletRequest request){
		StringBuffer sb=new StringBuffer();
		if (tablename==null||idcolumn==null||id==0||al==null) return "";
		if (tablename.equals("")||idcolumn.equals("")) return "";
		if (al.isUserAuthorized(tablename+"/"+contentcolumn, request)||validAccessCode()){
			String type=authorizedelements.get(tablename+"/"+contentcolumn);
			String explanation=elementexplanation.get(tablename+"/"+contentcolumn);
			String content=Dbutil.readValueFromDB("select * from "+tablename+" where "+idcolumn+"="+id+";", contentcolumn);
			if (content==null||content.equals("null")) content=" ";
			if (type.equals("text")){
				sb.append("<script type='text/javascript'>function setupEditor"+tablename+id+contentcolumn+"() {$('#"+tablename+id+contentcolumn+"originalcontent').hide();$('#"+tablename+id+contentcolumn+"setupeditor').hide();$('#"+tablename+id+contentcolumn+"editor').show();};</script>");
				sb.append("<div class='editor' id='"+tablename+id+contentcolumn+"editor' style='display:none;'>"+explanation+"<form autocomplete='off' id='"+tablename+id+contentcolumn+"'>" +
						"<textarea name='content' id='"+tablename+id+contentcolumn+"ta' style='width: 500px;height:30px;'>"+content+"</textarea>" +
						"<input type='hidden' name='id' value='"+id+"'/>"+		
						"<input type='hidden' name='contentcolumn' value='"+contentcolumn+"'/>"+		
						"<input type='hidden' name='idcolumn' value='"+idcolumn+"'/>"+		
						(validAccessCode()?"<input type='hidden' name='accesscode' value='"+request.getParameter("accesscode")+"'/>":"")+
						"<input type='hidden' name='tablename' value='"+tablename+"'/>"+
						"<input type='button' value='Save' onclick=\"$.ajax({type: 'POST',url: '/communityupdate.jsp', data: $('#"+tablename+id+contentcolumn+"').serialize()"+(elementid!=null?",success:function(data){$('#"+tablename+id+contentcolumn+"originalcontent').html(data).show();$('#"+tablename+id+contentcolumn+"editor').hide();$('#"+tablename+id+contentcolumn+"setupeditor').show();}":"")+"});"+"\">"+
				"</form></div>");
				sb.append("<script type='text/javascript'>$('#"+elementid+"').html(\"<div id='"+tablename+id+contentcolumn+"originalcontent'>\"+$('#"+elementid+"').html()+\"</div><input type='button' id='"+tablename+id+contentcolumn+"setupeditor' onclick='javascript:setupEditor"+tablename+id+contentcolumn+"()' value='Edit'>\");$('#"+tablename+id+contentcolumn+"editor').appendTo('#"+elementid+"');</script>");
				
			} else {
			sb.append("<script type='text/javascript'>tinyMCE.init({mode : 'exact',elements:'"+tablename+id+contentcolumn+"ta', theme : 'advanced',gecko_spellcheck : true,theme_advanced_toolbar_align : 'left',theme_advanced_toolbar_location : 'top',theme_advanced_buttons1 : 'bold,italic,underline,|,cut,copy,paste,|,formatselect,bullist,numlist,|,undo,redo,|,link,unlink,image,cleanup,|,removeformat,visualaid,|,sub,sup,|,charmap,|,help,code',theme_advanced_buttons2 : '',theme_advanced_buttons3 : '',theme_advanced_buttons4 : '',theme_advanced_blockformats : 'p,h1,h2,h3,h4',convert_urls : 0}); function setupEditor"+tablename+id+contentcolumn+"() {$('#"+tablename+id+contentcolumn+"originalcontent').hide();$('#"+tablename+id+contentcolumn+"setupeditor').hide();$('#"+tablename+id+contentcolumn+"editor').show();};</script>");
			sb.append("<div class='editor' id='"+tablename+id+contentcolumn+"editor' style='display:none;'>"+explanation+"<form autocomplete='off' id='"+tablename+id+contentcolumn+"'>" +
					"<textarea name='content' id='"+tablename+id+contentcolumn+"ta' style='width: 100%;height:300px;'>"+content+"</textarea>" +
					"<input type='hidden' name='id' value='"+id+"'/>"+		
					"<input type='hidden' name='contentcolumn' value='"+contentcolumn+"'/>"+		
					"<input type='hidden' name='idcolumn' value='"+idcolumn+"'/>"+		
					(validAccessCode()?"<input type='hidden' name='accesscode' value='"+request.getParameter("accesscode")+"'/>":"")+
					"<input type='hidden' name='tablename' value='"+tablename+"'/>"+
					"<input type='button' value='Save' onclick=\"tinyMCE.triggerSave(false,false);$.ajax({type: 'POST',url: '/communityupdate.jsp', data: $('#"+tablename+id+contentcolumn+"').serialize()"+(elementid!=null?",success:function(data){$('#"+tablename+id+contentcolumn+"originalcontent').html(data).show();$('.editor').hide();$('#"+tablename+id+contentcolumn+"setupeditor').show();}":"")+"});"+"\">"+
			"</form></div>");
			sb.append("<script type='text/javascript'>$('#"+elementid+"').html(\"<div id='"+tablename+id+contentcolumn+"originalcontent'>\"+$('#"+elementid+"').html()+\"</div><input type='button' id='"+tablename+id+contentcolumn+"setupeditor' onclick='javascript:setupEditor"+tablename+id+contentcolumn+"()' value='Edit'>\");$('#"+tablename+id+contentcolumn+"editor').appendTo('#"+elementid+"');</script>");
			}
		}
		
		return sb.toString();
	}
	
	public boolean validAccessCode(){
		if (tablename==null||idcolumn==null||id==0||al==null) return false;
		if (accesscode!=null&&accesscode.equals(Dbutil.readValueFromDB("select * from "+tablename+" where "+idcolumn+"="+id+";", "edithashcode"))) return true;
		return false;
	}
	
	public String getAccesscode() {
		return accesscode;
	}

	public void setAccesscode(String accesscode) {
		this.accesscode = accesscode;
	}

	
	
	public String getContentcolumn() {
		return contentcolumn;
	}

	public void setContentcolumn(String contentcolumn) {
		this.contentcolumn = contentcolumn;
	}

	public String getElementid() {
		return elementid;
	}

	public void setElementid(String elementid) {
		this.elementid = elementid;
	}

	public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	public String getIdcolumn() {
		return idcolumn;
	}
	public void setIdcolumn(String idcolumn) {
		this.idcolumn = idcolumn;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Auditlogger getAl() {
		return al;
	}
	public void setAl(Auditlogger al) {
		this.al = al;
	}
	
	
}

package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;


public class Usernote {

	public int tenant;
	public String userid;
	public String table;
	public int rowid;
	public int id;
	public String note;
	public boolean allowedit=false;
	public Date date;
	public static enum actions {get,save,delete};
	public boolean valid=true;

	private Usernote (int tenant,String userid,String table, int rowid, String note, int id, boolean allowedit, Date date){
		this.tenant=tenant;
		this.userid=userid;
		this.table=table;
		this.rowid=rowid;
		this.note=note;
		this.id=id;
		this.allowedit=allowedit;
		this.date=date;
	}

	public Usernote (HttpServletRequest request,String table){
		try{
			Context c=new Context(request);
			this.tenant=c.tenant;
			this.userid=c.userid;
			this.table=table;
			this.rowid=Integer.parseInt(request.getParameter("producerid"));
			this.note=request.getParameter("note");
			this.id=0;
			try{id=Integer.parseInt(request.getParameter("noteid"));}catch (Exception e){}
			this.allowedit=true;
			this.date=new java.sql.Timestamp(new java.util.Date().getTime()); 
		} catch (Exception e){}
		if (userid.equals("")||note==null||(note.equals("")&&id==0)||rowid==0) valid=false;
	}

	public static ArrayList<Usernote> getUserNotes(Context c, HttpServletRequest request,String table, int rowid){
		ArrayList<Usernote> notes=new ArrayList<Usernote>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from usernotes where tenant="+c.tenant+" and tablename='"+table+"' and rowid="+rowid+";";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				notes.add(new Usernote(c.tenant,rs.getString("userid"),rs.getString("tablename"), rs.getInt("rowid"),rs.getString("note"),rs.getInt("id"), (rs.getString("userid").equals(c.userid)),rs.getDate("date")));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return notes;
	}

	public boolean isAllowed(HttpServletRequest request, actions action){
		boolean allowed=false;
		if (action==actions.get){
			if ("knownwines".equals(table)) allowed=true;
			if ("producers".equals(table)) allowed=true;
		}
		if (action==actions.save){
			if (("knownwines".equals(table)||"producers".equals(table))&&userid!=null&&(userid.equals(Dbutil.readValueFromDB("select * from usernotes where id="+id+" and tablename='"+table+"';", "userid"))||request.isUserInRole("admin"))) allowed=true;
		}
		return allowed;
	}

	public boolean save(HttpServletRequest request){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			if (valid){
				Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime()); 
				note=Webroutines.filterUserCommentInput(note);
				if (isAllowed(request,actions.save)){
					if (id>0){
						if ("".equals(note)){
							rs=Dbutil.selectQuery("select * from usernotes where tablename='"+table+"' and id="+id+" and rowid="+rowid+" and userid='"+Spider.SQLEscape(userid)+"';", con);
							ChangeLog log=new ChangeLog(new Context(request),rs,"id");
							log.setValueOld(rs);
							log.date=now;
							int result=Dbutil.executeQuery("delete from usernotes where tablename='"+table+"' and id="+id+" and rowid="+rowid+" and userid='"+Spider.SQLEscape(userid)+"';");
							if (result>0) {
								log.save();
								return true;
							}
						}
						rs=Dbutil.selectQuery("select * from usernotes where tablename='"+table+"' and id="+id+" and rowid="+rowid+" and userid='"+Spider.SQLEscape(userid)+"';", con);
						ChangeLog log=new ChangeLog(new Context(request),rs,"id");
						log.setValueOld(rs);
						log.date=now;
						int result=Dbutil.executeQuery("update usernotes set note='"+Spider.SQLEscape(note)+"' , date='"+now+"' where tablename='"+table+"' and id="+id+" and rowid="+rowid+" and userid='"+Spider.SQLEscape(userid)+"';");
						if (result>0) {
							Dbutil.closeRs(rs);
							rs=Dbutil.selectQuery("select * from usernotes where tablename='"+table+"' and id="+id+" and rowid="+rowid+" and userid='"+Spider.SQLEscape(userid)+"';", con);
							log.setValueNew(rs);
							log.save();
							return true;
						}
					} else {
						int result=Dbutil.executeQuery("insert into usernotes (tenant,date,userid,tablename,rowid,note) values ("+tenant+",'"+now+"','"+Spider.SQLEscape(userid)+"','"+table+"',"+rowid+",'"+Spider.SQLEscape(note)+"');",con);
						if (result>0)
						{
							rs=Dbutil.selectQuery("select LAST_INSERT_ID() as id;", con);

							rs.next();
							id=rs.getInt("id");
							rs=Dbutil.selectQuery("select * from usernotes where tablename='"+table+"' and id="+id+" and rowid="+rowid+" and userid='"+Spider.SQLEscape(userid)+"';", con);
							ChangeLog log=new ChangeLog(new Context(request),rs,"id");
							log.setValueNew(rs);
							log.date=now;
							log.save();
							return true;
						}
					}
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("Problem while saving Usernote.",e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return false;
	}

}

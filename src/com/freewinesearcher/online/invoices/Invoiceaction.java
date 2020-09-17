package com.freewinesearcher.online.invoices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;


public class Invoiceaction {
	Integer id;
	Date actiondate;
	String statusfrom;
	String statusto;
	Date duedate;
	String filename;
	String action;
	
	
	
	public Invoiceaction(String action, Date actiondate, Date duedate,
			String filename, String statusfrom, String statusto, int partnerid, int invoiceid, boolean generatefilename, Connection con) throws Exception {
		super();
		this.action = action;
		this.actiondate = actiondate;
		this.duedate = duedate;
		this.filename = filename;
		this.statusfrom = statusfrom;
		this.statusto = statusto;
		ResultSet rs=null;
		//Connection con=Dbutil.openNewConnection();
		try{
			Dbutil.executeQuery("Insert into invoiceactions (invoiceid,actiondate,statusfrom,statusto,filename,action) values " +
					"("+invoiceid+",'"+new java.sql.Timestamp(actiondate.getTime()).toString()+"','"+statusfrom+"','"+statusto+"','','"+action+"');", con);
			if (generatefilename){
				rs=Dbutil.selectQuery("SELECT LAST_INSERT_ID() as id;",con);
				rs.next();
				int iaid=rs.getInt("id");
				this.filename=partnerid+"_"+invoiceid+"_"+iaid+"_"+("rnd"+iaid).hashCode()+".pdf";
				if (0==Dbutil.executeQuery("update invoiceactions set filename='"+Spider.SQLEscape(this.filename)+"' where id="+iaid+";", con)){
					throw new Exception();
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("Problem while creating new Invoiceaction",e);
			throw e;
		} finally {
			Dbutil.closeRs(rs);
			//Dbutil.closeConnection(con);
		}
		
	}

	public Invoiceaction(int id){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			query = "Select * from invoiceactions where id=" + id + ";";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				id=(rs.getInt("id"));
				setActiondate(rs.getDate("actiondate"));
				setStatusfrom(rs.getString("statusfrom"));
				setStatusto(rs.getString("statusto"));
				//setDuedate(rs.getDate("duedate"));
				setFilename(rs.getString("filename"));
				setAction(rs.getString("action"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving invoiceaction with id="+id, e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getActiondate() {
		return actiondate;
	}

	public void setActiondate(Date actiondate) {
		this.actiondate = actiondate;
	}

	public Date getDuedate() {
		return duedate;
	}

	public void setDuedate(Date duedate) {
		this.duedate = duedate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getStatusfrom() {
		return statusfrom;
	}

	public void setStatusfrom(String statusfrom) {
		this.statusfrom = statusfrom;
	}

	public String getStatusto() {
		return statusto;
	}

	public void setStatusto(String statusto) {
		this.statusto = statusto;
	}
	
}

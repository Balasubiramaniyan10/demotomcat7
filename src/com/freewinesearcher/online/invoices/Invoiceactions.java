package com.freewinesearcher.online.invoices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.freewinesearcher.common.Dbutil;




public class Invoiceactions {
	
	public ArrayList<Invoiceaction> actions=new ArrayList<Invoiceaction>();
	
	public Invoiceactions(Invoice invoice){
		getInvoiceActions(invoice);	
		
	}
	
	public Invoiceactions(ArrayList<Invoiceaction> actions){
		// For testing purposes only!
		this.actions=actions;	
	}
	
	private void getInvoiceActions(Invoice invoice){
		int invoiceid=invoice.getId();
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			query = "Select * from invoiceactions where invoiceid=" + invoiceid + " order by id;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				actions.add(new Invoiceaction(rs.getInt("id")));
			}
			query = "Select * from invoiceactions where invoiceid=" + invoiceid + " and filename !='' order by id desc;";
			rs = Dbutil.selectQuery(query, con);
			if (rs.next()) {
				invoice.setLastinvoicefilename(rs.getString("filename"));
			}
		
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving invoice with id="+invoiceid, e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
	}

	
	
}

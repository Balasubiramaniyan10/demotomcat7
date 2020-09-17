package com.freewinesearcher.online.invoices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;

public class InvoiceDetails {
	Date fromdate;
	Date todate;
	ArrayList<Details> details=new ArrayList<Details>();
	
	public InvoiceDetails(int partnerid){
		NumberFormat format  = new DecimalFormat("#,##0.00");	
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		String query;
		try {
			query = "Select date(min(date)) as mindate, date(max(date)) as maxdate from billingoverview  where partnerid=" + partnerid + ";";
			rs = Dbutil.selectQuery(query, con);
			if (rs.next()){
				fromdate=rs.getDate("mindate");
				todate=rs.getDate("maxdate");
				
			}
			query = "Select type, cpc, count(*) as thecount, sum(cpc) as total from billingoverview where partnerid=" + partnerid + " group by type, cpc;";
			rs = Dbutil.selectQuery(query, con);
			while (rs.next()) {
				Details lineitem=new Details();
				if (rs.getString("type").equalsIgnoreCase("banners")) {
					lineitem.description="Banner clicks";
				} else if (rs.getString("type").equalsIgnoreCase("sponsoredlinks")) {
					lineitem.description="Sponsored results";
				} else if (rs.getString("type").equalsIgnoreCase("orders")) {
					lineitem.description="Commision for orders";
				} else {
					Dbutil.logger.error("unknown type in table billingoverview");
				}
				lineitem.cpc=rs.getDouble("cpc");
				lineitem.amount=rs.getDouble("total");
				lineitem.clicks=rs.getInt("thecount");
				details.add(lineitem);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem retrieving invoice details for partner with id="+partnerid, e);
		}		
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);	


		
	}
	
	static class Details{
		String description;
		int clicks;
		Double cpc;
		Double amount;
	}

	
	

}

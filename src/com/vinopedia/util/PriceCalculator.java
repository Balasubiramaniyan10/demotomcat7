package com.vinopedia.util;

import java.sql.*;

import com.freewinesearcher.common.Dbutil;

public class PriceCalculator {

	public double price;
	public String currency;
	public String countrycode;
	public boolean ispriceexvat;
	public boolean ispriceexduty;
	public boolean sparkling=false;
	public double bottlesize;
	public double targetprice;
	public String targetcurrency;

	public PriceCalculator(double price, String currency, String countrycode, double bottlesize, boolean ispriceexvat, boolean ispriceexduty, boolean sparkling) {
		super();
		this.price = price;
		this.currency = currency;
		this.countrycode = countrycode;
		this.ispriceexvat = ispriceexvat;
		this.ispriceexduty = ispriceexduty;
		this.sparkling = sparkling;
		this.bottlesize=bottlesize;
	}

	public void calculatePrice(String targetcurrency, String targetcountrycode, boolean exvat, boolean exduty) {
		if (targetcurrency!=null) this.targetcurrency=targetcurrency;
		String query;
		double result=0;
		ResultSet rs = null;
		ResultSet rs2 = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from vat join currency on (vat.currency=currency.currency) where countrycode='"+countrycode+"';";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				result=price;
				if (!currency.equals(rs.getString("currency"))){
					// get original price in local currency
					// to EUR
					rs2=Dbutil.selectQuery(rs2, "select * from currency where currency='"+currency+"';", con);
					if (rs2.next()){
						result=result*rs2.getDouble("rate");
					} else {
						Dbutil.logger.info("Could not find currency "+currency);
						result=0;
					}
					Dbutil.closeRs(rs2);
					// to local currency
					result=result/rs.getDouble("rate");
				}
				if (!ispriceexduty){
					result=result-(rs.getDouble("duty"+(sparkling?"sparkling":""))*bottlesize/100);
				}
				if (!ispriceexvat){
					result=result*(100/(100+rs.getDouble("vat")));
				}
				// result is in local currency ex vat and ex duty
				// change to currency of target country
				query = "select * from vat join currency on (vat.currency=currency.currency) where countrycode='"+targetcountrycode+"';";
				rs2 = Dbutil.selectQuery(rs2, query, con);
				if (rs2.next()) {
					result=result/rs2.getDouble("rate");
					// add target VAT if necessary
					if (!exvat){
						result=result*(100+rs2.getDouble("vat"))/100;
					}
					// add target duty if necessary
					if (!exduty){
						result=result+(rs2.getDouble("duty"+(sparkling?"sparkling":""))*bottlesize/100);
					}
					// Change price to target currency if necessary
					if (targetcurrency==null){
						this.targetcurrency=rs2.getString("currency");
					} else {
						if (!targetcurrency.equals(rs2.getString("currency"))){
							// to EUR
							result=result*rs2.getDouble("rate");
							// to target currency
							Dbutil.closeRs(rs2);
							rs2=Dbutil.selectQuery(rs2, "select * from currency where currency='"+targetcurrency+"';", con);
							if (rs2.next()){
								result=result/rs2.getDouble("rate");
							} else {
								Dbutil.logger.info("Could not find currency "+targetcurrency);
								result=0;
							}
						} 
					}

				}else {
					Dbutil.logger.info("Could not find countrycode "+targetcountrycode);
					result=0;
				}

			} else {
				Dbutil.logger.info("Could not find countrycode "+countrycode);
				result=0;
			}
			if (result==0){
				Dbutil.logger.info("Could not calculate price");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
			result=0;
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
		}
		targetprice=result;
		
	}

	public static void main(String[] args){
		PriceCalculator p=new PriceCalculator(10, "EUR", "NL", 0.75, false, false, false);
		p.calculatePrice("GBP", "GB", false, false);
		System.out.println("GB incl vat and duty: "+p.targetprice);
		
		
	}



}

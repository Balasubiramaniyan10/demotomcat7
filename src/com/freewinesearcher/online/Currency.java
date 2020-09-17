package com.freewinesearcher.online;

import java.sql.*;
import java.util.HashMap;

import com.freewinesearcher.common.Dbutil;

public class Currency {
	private static HashMap<String, Double> rates=null;
	private static HashMap<String,String> symbols=null;
	
	public static String getSymbol(String currency){
		String symbol=getSymbols().get(currency);
		if (symbol==null){ 
			return "";
		} else {
			return symbol;
		}
	}

	public static Double getRate(String currency){
		Double rate=getRates().get(currency);
		if (rate==null||rate<0.001) rate=1.0;
		return rate;
	}
	
	public static synchronized void clearCache(){
		rates=null;
		symbols=null;
		getSymbol("EUR");
		getRate("EUR");
	}

	private static synchronized HashMap<String, Double> getRates() {
		if (rates==null){
			rates=new HashMap<String,Double>();
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "Select * from currency;";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					rates.put(rs.getString("currency"), rs.getDouble("rate"));
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return rates;
	}

	private static synchronized HashMap<String, String> getSymbols() {
		if (symbols==null){
			symbols=new HashMap<String,String>();
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "Select * from currency;";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					symbols.put(rs.getString("currency"), rs.getString("symbol"));
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return symbols;
	}

}

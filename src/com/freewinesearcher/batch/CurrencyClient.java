package com.freewinesearcher.batch;


import java.io.*;
import java.net.*;
import java.util.*;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.soap.util.xml.*;
import org.apache.soap.*;
import org.apache.soap.rpc.*;
import org.json.JSONException;
import org.json.JSONObject;


import com.freewinesearcher.api2.ApiHandler;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.Shop;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.Webroutines;

import java.sql.ResultSet;
import java.sql.Connection;

import javax.servlet.http.HttpServletResponse;


public class CurrencyClient{

	public static float getRate (URL url, String country1, String country2) throws Exception {

		Call call = new Call ();

		// Set encoding style. Use the standard SOAP encoding
		String encodingStyleURI = Constants.NS_URI_SOAP_ENC;
		call.setEncodingStyleURI(encodingStyleURI);

		// Set service locator parameters
		call.setTargetObjectURI ("urn:xmethods-CurrencyExchange");
		call.setMethodName ("getRate");

		// Create the input parameter vector 
		Vector params = new Vector ();
		params.addElement (new Parameter("country1", String.class, country1, null));
		params.addElement (new Parameter("country2", String.class, country2, null));
		call.setParams (params);

		// Invoke the service ...
		Response resp = call.invoke (url,"");

		// ... and evaluate the result
		if (resp.generatedFault ()) {
			throw new Exception();
		} else {

			// Call was succesfull. Extract response parameter and return
			Parameter result = resp.getReturnValue ();
			Float rate=(Float) result.getValue();
			return rate.floatValue();
		}
	}


	public static void updateRatesObsolete(){
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		String country1;
		String country2="euro";
		float rate;
		try {
			URL url=new URL("http://services.xmethods.net:80/soap");
			rs=Dbutil.selectQuery("Select country, currency from vat group by currency;",con);
			while (rs.next()){
				if (!rs.getString("currency").equals("EUR")){
					rate=1;
					country1=rs.getString("country");
					rate = getRate(url,country1,country2);
					if (rate>0) Dbutil.executeQuery("Update currency set rate="+rate+" where currency='"+rs.getString("currency")+"';");
				}
			}

		}
		catch (Exception e) {
			Dbutil.logger.error("Problem during exchange rate update; ", e);
		}
		try{
			Dbutil.closeConnection(con);
		} catch (Exception exc) {
			Dbutil.logger.error("Could not close connection: ",exc);

		}
	}


	public static void updateRates2(){
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		String currency="";
		float rate;
		try {
			String rates=Spider.getWebPage("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", "UTF-8", null, "", "Yes");
			if (rates.contains("currency")){ 
				String updatetime=Webroutines.getRegexPatternValue("time='([^']+)'>",rates);
				System.out.println(rates);
				rs=Dbutil.selectQuery("Select currency from currency;",con);
				while (rs.next()){
					if (!rs.getString("currency").equals("EUR")){
						rate=0;
						currency=rs.getString("currency");
						try{
							rate = Float.valueOf(Webroutines.getRegexPatternValue("currency='"+currency+"' rate='([\\d.]+)'/>",rates));
						}catch (Exception e){
							Dbutil.logger.warn("Could not update exchange rate for currency "+currency);
						}
						Dbutil.logger.info("Currency "+currency+" rate "+rate);
						if (rate>0) Dbutil.executeQuery("Update currency set rate="+1/rate+", updated='"+updatetime+"' where currency='"+rs.getString("currency")+"';");
					}
				}
			} else {
				Dbutil.logger.warn("Could not update currency rates.");
			}

		}
		catch (Exception e) {
			Dbutil.logger.error("Problem during exchange rate update. ", e);
		}
		Dbutil.closeConnection(con);

	}

	public static float toEuro(float amount, String currency){
		float result=amount;
		try{
			String rate=Dbutil.readValueFromDB("Select rate from currency where currency='"+currency+"';","rate");
			result=amount*Float.parseFloat(rate);
		} catch (Exception e) {
			Dbutil.logger.error("Problem calculating euro value for "+currency, e);
		}
		return result;
	}

	public static double convertCurrency(double amount, String from, String to){
		double result=0;
		try{
			String ratefrom="1";
			if (!"EUR".equals(from)) ratefrom=Dbutil.readValueFromDB("Select rate from currency where currency='"+from+"';","rate");
			String rateto="1";
			if (!"EUR".equals(to)) rateto=Dbutil.readValueFromDB("Select rate from currency where currency='"+to+"';","rate");
			result=amount*Double.parseDouble(ratefrom)/Double.parseDouble(rateto);
		} catch (Exception e) {
			Dbutil.logger.error("Problem calculating value for currency "+from+". ", e);
		}
		return result;
	}

	public static String getCurrencyForShop(int shopid){
		return Dbutil.readValueFromDB("Select currency, countrycode, exvat from shops where id="+shopid+";","currency");
	}


	public static void recalculateAllPrices(){
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		Connection con2 = Dbutil.openNewConnection();
		int shopid=0;
		try{
			rs = Wijnzoeker.GetShops(con);
			while (rs.next()){
				shopid=rs.getInt("id");
				updateShopPrices(shopid,con2);
			}
			Dbutil.closeRs(rs);
		}catch (Exception e){
			Dbutil.logger.error("",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			Dbutil.closeConnection(con2);
		}

	}

	public static void updateShopPrices(int shopid, Connection con){
		try{
			Double pricefactorex = Dbutil.getPriceFactorex(shopid+"");
			Double pricefactorin = Dbutil.getPriceFactorin(shopid+"");
			String query="update LOW_PRIORITY wines set priceeuroex=price*"+pricefactorex+", priceeuroin=price*"+pricefactorin+" where shopid="+shopid+";";
			Dbutil.executeQuery(query, con);
			
		}catch (Exception e){
			Dbutil.logger.error("",e);
		} 
	}


}



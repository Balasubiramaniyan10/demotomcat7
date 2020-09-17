package com.freewinesearcher.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import com.freewinesearcher.api.Api.Actions;
import com.freewinesearcher.api.helpers.Currency;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;

public class PriceInfo extends Api implements IApi{

	private static final long serialVersionUID = 2939349313800466727L;
	
	public PriceInfo(HttpServletRequest request, HttpServletResponse response, Actions action, Map<String, String> params,Webactionlogger logger) {
		super(request, response, action, params,logger);
	}

	@Override
	public void process() {
		try {
			Knownwine wine=Api.matchWine(params.get("name"));
			logger.name=params.get("name");
			logger.knownwineid=wine.id;
			if (wine.id==0){
				JSONObject object = new JSONObject();
				object.append("name", "unknown");
				object.append("type", "wine");

				result = new Result(object, null);
			} else {
				Currency currency = new Currency(params.get("currency"));
				String vintagestr=params.get("vintage");
				int vintage=0;
				if (vintagestr==null) vintagestr="";
				try{vintage=Integer.parseInt(vintagestr);}catch(Exception e){}
				logger.vintage=vintage+"";
				int stores=0;
				int offers=0;
				double minimum = Webroutines.getLowestPrice(wine.id,vintagestr);
				double average = Webroutines.getAveragePrice(wine.id,vintagestr);
				String query;
				ResultSet rs = null;
				Connection con = Dbutil.openNewConnection();
				try {
					query = "select distinct(shopid) as numshops from wines where knownwineid="+wine.id+(vintage>0?" and vintage="+vintage:"");
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						stores=rs.getInt("numshops");
					}
					Dbutil.closeRs(rs);
					query = "select count(*) as offers from wines where knownwineid="+wine.id+(vintage>0?" and vintage="+vintage:"");
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						offers=rs.getInt("offers");
					}
				} catch (Exception e) {
					Dbutil.logger.error("", e);
				} finally {
					Dbutil.closeRs(rs);
					Dbutil.closeConnection(con);
				}
				
				JSONObject json = new JSONObject();
				json.append("minimumprice", Webroutines.formatPriceNoCurrency(minimum, minimum, currency.isocode, "EX"));
				json.append("stores", stores);
				json.append("offers", offers);
				json.append("averageprice", Webroutines.formatPriceNoCurrency(average, average, currency.isocode, "EX"));
				json.append("currency", currency.isocode);
				JSONObject object = new JSONObject();
				object.append("name", wine.name);
				object.append("url", "https://www.vinopedia.com"+Webroutines.winelink(Knownwines.getUniqueKnownWineName(wine.id), vintage));
				object.append("type", "wine");
				object.append("producer", wine.getProperties().get("producer"));
				object.append("region", wine.getProperties().get("locale"));
				result = new Result(object, json);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			result=null;
		}
	}

	@Override
	public Result getResult() {
		return result;
	}

}

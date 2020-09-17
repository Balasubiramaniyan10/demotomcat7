package com.freewinesearcher.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.Calendar;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.freewinesearcher.api.Api.Actions;
import com.freewinesearcher.api.helpers.Currency;
import com.freewinesearcher.api2.Matcher;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;

public class SearchStats extends Api implements IApi{

	private static final long serialVersionUID = 2939349313800466727L;

	public SearchStats(HttpServletRequest request, HttpServletResponse response, Actions action, Map<String, String> params,Webactionlogger logger) {
		super(request, response, action, params,logger);
	}

	@Override
	public void process() {
		try {
			Matcher match=new Matcher();
			match.setType(Matcher.Types.WINERY);
			match.setInput(params.get("winery"));
			String winery=match.match();
			logger.name=params.get("winery");
			//logger.knownwineid=wine.id;
			if (winery==null||winery.length()==0){
				JSONObject object = new JSONObject();
				object.append("name", "unknown");
				object.append("type", "winery");

				result = new Result(object, null);
			} else {
				String query;
				ResultSet rs = null;
				Connection con = Dbutil.openNewConnection();
				JSONArray json = new JSONArray();
				try {
					long MILLIS_IN_A_DAY = 1000*60*60*24;
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					Date startdate=null;
					Date enddate=null;
					Calendar today=Calendar.getInstance();
					Calendar yest=Calendar.getInstance();
					yest.add(Calendar.DATE, -1);
					try{
						startdate = new java.sql.Date(formatter.parse(params.get("startdate")).getTime());
						enddate = new java.sql.Date(formatter.parse(params.get("enddate")).getTime());
					}catch (Exception e){}
					if (startdate==null||startdate.compareTo(new Date(yest.getTimeInMillis()))>0) startdate=new  java.sql.Date(yest.getTimeInMillis());
					if (enddate==null||startdate.compareTo(enddate)>0) enddate=startdate;
					enddate=new java.sql.Date(enddate.getTime()+ MILLIS_IN_A_DAY);
					query = "select * from searchstats join knownwines on (knownwineid=knownwines.id) where date >= '"+startdate+"' and date <'"+enddate+"' and searchstats.producer='"+Spider.SQLEscape(winery)+"' order by date,knownwineid";
					rs = Dbutil.selectQuery(rs, query, con);
					while (rs.next()) {
						JSONObject record = new JSONObject();
						record.append("date", rs.getDate("date"));
						record.append("wine", rs.getString("wine"));
						record.append("searches", rs.getInt("visitors"));
						json.put(record);
					}
					Dbutil.closeRs(rs);
					
				} catch (Exception e) {
					Dbutil.logger.error("", e);
				} finally {
					Dbutil.closeRs(rs);
					Dbutil.closeConnection(con);
				}

				JSONObject resultobj=new JSONObject();
				resultobj.append("searchdata", json);
				JSONObject object = new JSONObject();
				object.append("winery", winery);
				object.append("url", "https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(Webroutines.removeAccents(winery))+"/");
				object.append("type", "winery");
				result = new Result(object, resultobj);
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

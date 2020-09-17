package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.common.Dbutil;

public class Preferences {
	private String uid;
	private Map<String,Object> preferences;

	public Preferences getPreferences(HttpServletRequest request, PageHandler p){
		if (request.getSession(true).getAttribute("preferences")!=null) return (Preferences)request.getSession(true).getAttribute("preferences");
		for (Cookie c:request.getCookies()) if (c.getName().equals("uid")) uid=c.getValue();
		if (uid!=null) {
			Preferences pref=new Preferences(uid);
			request.getSession(true).setAttribute("preferences", pref);
			return pref;
		}
		return new Preferences(request,p);
	}

	private Preferences (String uid){
		super();
		this.uid=uid;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from preferences where uid='"+uid+"';";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				ResultSetMetaData md = rs.getMetaData();
				for (int n=0;n<md.getColumnCount();n++) setPreference(md.getColumnName(n), (rs.getObject(n).getClass().cast(rs.getObject(n))));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not read preferences for uid "+uid, e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}


	public void setPreference(String key, Object value){
		if (key!=null&&value!=null&!preferences.get(key).equals(value)){
			preferences.put(key,value);
		}
	}

	private Preferences (HttpServletRequest request,PageHandler p){
		String currency=null;
		if (request.getCookies()!=null) {
			for (Cookie cookie:request.getCookies()) {
				if (cookie.getName().equals("currency")) currency=cookie.getValue();
			}
		}
		if (currency==null){
			if (p.hostcountry!=null) {
				if (p.hostcountry.equals("US")||p.hostcountry.equals("CA")) {
					currency="USD";  
				}else if (p.hostcountry.equals("UK")) {
					currency="GBP";
				} else	if (p.hostcountry.equals("CH")) currency="CHF";
			}
		}
		if (currency==null) currency="EUR";
		setPreference("currency", currency);
		setPreference("language", "EN");
		if (p.seemsbot){
			setPreference("country", "All");		
			setPreference("lastcountry", "All");		
		} else {
			setPreference("country", Webroutines.getRegion(p.hostcountry));		
		}		
		if (preferences.get("country").equals("")) setPreference("county","All");
		setPreference("lastcountry", preferences.get("country"));		
		request.getSession(true).setAttribute("preferences", this);
	}
	
	

	public void save(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (uid==null){
				Dbutil.executeQuery("insert into preferences (uid) values (null);",con);
				query = "select LAST_INSERT_ID() as uid;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					uid=rs.getString("uid");
				}
			}

		StringBuffer sb=new StringBuffer();
		sb.append("update preferences set ");
		for (String k:preferences.keySet()) sb.append(k+"='"+preferences.get(k)+"',");
		sb.deleteCharAt(sb.length()-1);
		sb.append(" where uid='"+uid+"';");
		Dbutil.executeQuery(sb.toString(),con);
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}
	
	


}

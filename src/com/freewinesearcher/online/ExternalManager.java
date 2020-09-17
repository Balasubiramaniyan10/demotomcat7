package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;

public class ExternalManager {
	public String url;
	public static final int expirydays = 30;

	public ExternalManager(HttpServletRequest request, HttpServletResponse response, Searchhistory searchhistory) {

		int wineid = 0;
		int shopid = 0;
		try {
			wineid = Integer.parseInt(request.getParameter("wineid"));
		} catch (Exception e) {
		}
		try {
			shopid = Integer.parseInt(request.getParameter("shopid"));
		} catch (Exception e) {
		}
		String exturl = (request.getParameter("exturl"));
		String exttarget = (request.getParameter("exttarget"));
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String query = "";

		String name = "";
		String vintage = "";
		String price = "0.0";
		String affiliateparams = "";
		int knownwineid = 0;
		double CPC = 0.0;

		try {
			if (wineid > 0) {
				query = "SELECT * from wineview where id=" + wineid + ";";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					shopid = rs.getInt("shopid");
					knownwineid = rs.getInt("knownwineid");
					url = rs.getString("SourceURL");
					if (url.equals("")) {
						url = rs.getString("shopurl");
					}
					name = rs.getString("Name");
					affiliateparams = rs.getString("affiliateparams");
					vintage = rs.getString("Vintage");
					price = rs.getString("Price");
					CPC = rs.getDouble("CPC");
					name = Spider.replaceString(name, "'", "\\'");
					Webroutines.logWebAction("Link Clicked", knownwineid, "/link.jsp", request.getRemoteAddr(), "",
							name, vintage, 0, new Float(0.0).floatValue(), new Float(0.0).floatValue(), "", false,
							shopid + "", wineid + "", price, url, CPC, searchhistory);
				}
			} else if (shopid > 0) {
				query = "SELECT * from shops where id=" + shopid + ";";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					url = rs.getString("shopurl");
					affiliateparams = rs.getString("affiliateparams");
					name = Spider.replaceString(name, "'", "\\'");
					Webroutines.logWebAction("Link Clicked", "/link.jsp", request.getRemoteAddr(), "", name, vintage, 0,
							new Float(0.0).floatValue(), new Float(0.0).floatValue(), "", false, shopid + "",
							wineid + "", price, url, CPC, searchhistory);
				}

			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		url = makeAffiliateUrl(url, affiliateparams);
		if (exturl != null) {
			url = exturl;
			PageHandler p = PageHandler.getInstance(request, response, "Partner Link Clicked: " + exttarget);
			p.logger.page = exturl;
			p.logger.logaction();
		}

		if (shopid > 0 & CPC > 0) {
			Cookie cookie = new Cookie("S" + shopid,
					Webroutines.URLEncodeUTF8(new java.sql.Timestamp(new java.util.Date().getTime()).toString()));
			cookie.setDomain(".vinopedia.com");
			cookie.setPath("/external");
			cookie.setMaxAge(expirydays * 24 * 3600);
			response.addCookie(cookie);
		}

	}

	public static String makeAffiliateUrl(String url, String affiliateparams) {
		if (affiliateparams != null && !affiliateparams.equals("")) {
			while (!Webroutines.getRegexPatternValue("(%REPLACE\\('[^']+','([^']*)'\\)%)", affiliateparams)
					.equals("")) { // %REPLACE('regex','String')%
				String regex = Webroutines.getRegexPatternValue("%REPLACE\\('([^']+)','", affiliateparams);
				String replacement = Webroutines.getRegexPatternValue("%REPLACE\\('[^']+','([^']*)'", affiliateparams);
				url = url.replaceAll(regex, replacement);
				affiliateparams = affiliateparams.replace(
						Webroutines.getRegexPatternValue("(%REPLACE\\('[^']+','([^']*)'\\)%)", affiliateparams), "");
			}
			affiliateparams = affiliateparams.replaceAll("%URL%", url);
			affiliateparams = affiliateparams.replaceAll("%ESCAPEDURL%", Webroutines.URLEncode(url));
			affiliateparams = affiliateparams.replaceAll("%REPLACECOLONURL%", url.replaceAll(":", "%3A"));
			url = affiliateparams;
		}

		return addGoogleParams(url);

	}

	public static String addGoogleParams(String url) {

		if (url != null && url.contains("boottle")) {
			url = Spider.unescape(url);
			try {
				url = url.substring(0, url.lastIndexOf("/") + 1) + Webroutines
						.URLEncodeUTF8(Spider.unescape(url.substring(url.lastIndexOf("/") + 1, url.length() - 21)));
			} catch (Exception e) {
			}
			;
			String googleparams = "?utm_source=vinopedia.com";
			url += googleparams;

		}
		if (url != null && url.contains("winesearcher.com"))
			url = url.replaceAll("winesearcher.com", "vinopedia.com");
		return url;
	}

}

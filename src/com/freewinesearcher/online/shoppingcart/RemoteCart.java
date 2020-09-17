package com.freewinesearcher.online.shoppingcart;

import java.sql.Connection;
import java.sql.ResultSet;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.online.Webroutines;

public class RemoteCart {
	String sessionid;
	String viewcarturl;
	String viewcartdata;
	String addtocarturl;
	String addtocartdata;
	String removefromcarturl;
	String removefromcartdata;
	String articleregex;
	String method;
	Shoppingcart cart;
	
	// Work in progress, never tested. Idea:
	// initialize cart without cookies, fill remote cart with items from shoppingcart by calling remote adtocarturl
	// transfer result url to client
	
	public RemoteCart(Shoppingcart cart){
		this.cart=cart;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from remotecart where active=1 and shopid="+cart.shopid;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				Webpage webpage=new Webpage();
				webpage.allowcookies=false;
				webpage.followredirect=true;
				webpage.urlstring=rs.getString("initcarturl");
				webpage.readPage();
				sessionid=Webroutines.getRegexPatternValue(rs.getString("sessionregex"), webpage.html);
				viewcarturl=rs.getString("viewcartprefix")+Webroutines.getRegexPatternValue(rs.getString("viewcartregex"), webpage.html);
				addtocarturl=rs.getString("addtocarturl");
				addtocartdata=rs.getString("addtocartdata");
				removefromcarturl=rs.getString("removefromcarturl");
				removefromcartdata=rs.getString("removefromcartdata");
				articleregex=rs.getString("articleregex");
				method=rs.getString("method");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}
	
	public void fillCart(){
		Webpage webpage=new Webpage();
		webpage.allowcookies=false;
		webpage.followredirect=true;
		for (int i:cart.cart.keySet()){
			String url=addtocarturl;
			String data=addtocartdata;
			String itemid=Webroutines.getRegexPatternValue(articleregex, Dbutil.readValueFromDB("select * from wines where id="+cart.cart.get(i).itemid, "sourceurl"));
			int amount=cart.cart.get(i).amount;
			url=url.replaceAll("%itemid%", itemid);
			url=url.replaceAll("%amount%", amount+"");
			url=url.replaceAll("%sessionid%", sessionid);
			data=data.replaceAll("%itemid%", itemid);
			data=data.replaceAll("%amount%", amount+"");
			data=data.replaceAll("%sessionid%", sessionid);
			if (method.equals("GET")){
				if (url.contains("?")){
					url+="&"+data;
				} else {
					url+="?"+data;
				}
			} else {
				webpage.postdata=data;
			}
			webpage.urlstring=url;
			webpage.readPage();
		}
		
		
	}
	
	public String getViewcarturl() {
		return viewcarturl.replaceAll("%sessionid%", sessionid);
	}

	public void setViewcarturl(String viewcarturl) {
		this.viewcarturl = viewcarturl;
	}


}

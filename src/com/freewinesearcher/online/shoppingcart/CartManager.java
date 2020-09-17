package com.freewinesearcher.online.shoppingcart;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Webroutines;
import com.freewinesearcher.online.shoppingcart.Shoppingcart.roles;

public class CartManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Shoppingcart cart;
	public Shoppingcart oldcart;
	int shopid;
	int amount;
	int wineid;
	String order;
	public String action="";
	transient public PageHandler p;
	transient JSONObject returnobject=new JSONObject();
	transient JSONArray result=new JSONArray();
	public Wine thiswine;
	//String role="Buyer";

	public String getAction() {
		if (action==null) action="";
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String handleAction(HttpServletRequest request){
		p.getLogger().type=action;
		returnobject=new JSONObject();
		if (action.equals("addtocart")&&wineid>0){
			try {
				
				p.getLogger().wineid=thiswine.Id+"";
				p.getLogger().shopid=shopid+"";
				p.getLogger().price=thiswine.PriceEuroEx.floatValue();
				p.getLogger().countryseller=thiswine.Country;
				p.getLogger().name=thiswine.Name;
				p.getLogger().vintage=thiswine.Vintage;
				p.getLogger().knownwineid=thiswine.Knownwineid;
				getCart(request).wineid=wineid;
				getCart(request).changeAmount(getAmount());
				getCart(request).checkOrder();
				returnobject.put("carthtml", getCart(request).getSimpleCartHTML(p,null));
				
			} catch (JSONException e) {
				Dbutil.logger.error("Error generating carthtml in cart",e);
			} catch(Exception e){}

		}
		if (action.equals("fillremotecart")){
			try {
				RemoteCart rc=new RemoteCart(getCart(request));
				rc.fillCart();
				return rc.getViewcarturl();
			} catch (Exception e) {
				Dbutil.logger.error("Error generating remote cart",e);
			}
		}
		return returnobject.toString();
	}

	public Shoppingcart getCart(HttpServletRequest request){
		if (cart==null) cart= new Shoppingcart(shopid,new Context(request));
		return cart;
	}


	public Shoppingcart getCart(Context c, int cartnumber, boolean forceretrieve){
		setWineid(wineid);
		if (cart!=null&&cart.shopid==shopid&&!forceretrieve) {
			cart.setWineid(wineid);
			return cart;
		}
		oldcart=null;
		cart=getLatestCart(c, cartnumber, shopid);
		oldcart=getOldCart(c, cartnumber, shopid);
		if (cart==null) {
			cart=new Shoppingcart(shopid,c);
			cart.setWineid(wineid);
		} else {
			if (Webroutines.getShopFromUserId(c.userid)==cart.shopid) {
				cart.role=roles.Seller;
			} else {
				cart.role=roles.Buyer;
			}
		}

		cart.changed=false;
		// Only for testing purposes!!!


		return cart;
	}

	public int getAmount() {
		return amount;
	}


	public void setAmount(int amount) {
		this.amount = amount;
	}


	public int getWineid() {
		return wineid;
	}
	public void setShopname(String shopname){
		shopname=shopname.replaceAll("\\\\", "").replaceAll(";", "");
		int shopid=Dbutil.readIntValueFromDB("select * from shops where shopname='"+Spider.SQLEscape(shopname)+"';", "id");
		if (shopid!=this.shopid) {
			wineid=0;
		}
		this.shopid=shopid;
	}


	public void setWineid(int wineid) {
		this.wineid=wineid;
		if (wineid>0){
			try{
				Wine wine=new Wine(wineid);
				if (wine!=null) {
					thiswine=wine;
					shopid=wine.ShopId;
					

				} else {
					shopid=0;
				}
			}catch(Exception e){}
		} else {
			thiswine=null;
		}
	}

	public int getShopid() {
		return shopid;
	}

	public void setShopid(int shopid) {
		if (shopid!=this.shopid) {
			cart=null;
		}
		this.shopid=shopid;
		if (wineid>0){
			try{
			Wine wine=new Wine(wineid);
			if (wine.ShopId!=shopid){
				wineid=0;
			}
		}catch(Exception e){}
		}
	}

	public Shoppingcart getLatestCart(Context c, int cartnumber, int shopid){
		Shoppingcart cart=null;
		if (shopid>0){
			int sellershopid=Webroutines.getShopFromUserId(c.userid);

			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "select * from shoppingcarts where tenant="+c.tenant+(cartnumber==0?" and customeruserid='"+c.userid+"' and shopid="+shopid:" and cartnumber="+cartnumber+" and shopid="+sellershopid)+" and status!='Ordered' and status!='Cancelled' order by cartnumber desc, version desc;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					return CartSerializer.readCart(rs.getInt("id"), shopid, c.tenant, rs.getInt("cartnumber"));
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return cart;
	}

	public Shoppingcart getOldCart(Context c, int cartnumber, int shopid){
		Shoppingcart cart=null;
		if (shopid>0){
			int sellershopid=Webroutines.getShopFromUserId(c.userid);

			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				query = "select * from shoppingcarts where tenant="+c.tenant+(cartnumber==0?" and customeruserid='"+c.userid+"' and shopid="+shopid:" and cartnumber="+cartnumber+" and shopid="+sellershopid)+" and status!='Ordered' and status!='Cancelled' and actorrole='"+(cartnumber==0?"Buyer":"Seller")+"' order by cartnumber desc, version desc;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					return CartSerializer.readCart(rs.getInt("id"), shopid, c.tenant, rs.getInt("cartnumber"));
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return cart;
	}


	/*
	public String getRole() {
		if ("".equals(role)) return "Buyer";
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}

	 */

	public String getOrder() {
		if ("".equals(order)) return "bestdeal";
		return order;
	}


	public void setOrder(String order) {
		this.order = order;
	}

	public String getCartsHTML(Context c, PageHandler p){
		String html="";
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		int sellershopid=Webroutines.getShopFromUserId(c.userid);
		try {
			if (sellershopid>0){
				query = "select * from shoppingcarts join (select cartnumber, max(version) as version from shoppingcarts where shopid="+sellershopid+" and status!='Ordered' and status!='Cancelled' group by cartnumber) sel on (shoppingcarts.cartnumber=sel.cartnumber and shoppingcarts.version=sel.version) where shopid="+sellershopid+" and status!='Ordered' and status!='Cancelled' order by actorrole, date;";
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.isBeforeFirst()){
					html+="<h2>Customer carts for "+Webroutines.getShopNameFromShopId(sellershopid, "")+"</h2>";
					html+="<table><tr><th>Cart number</th><th>Last updated</th><th>Status</th></tr>";
					while (rs.next()) {
						html+="<tr><td><a href='"+p.thispage.replaceAll("\\?.*$", "")+"?shopid="+sellershopid+"&cartnumber="+rs.getInt("cartnumber")+"&action=retrieve'>"+rs.getString("customeruserid")+"</a></td><td>"+rs.getDate("date")+"</td><td>"+(rs.getString("actorrole").equals("Buyer")?"Updated":"Unchanged")+"</tr>";
					}
					html+="</table>";
				}
			}
			query = "select * from shoppingcarts join (select cartnumber, max(version) as version from shoppingcarts where customeruserid='"+c.userid+"' and status!='Ordered' and status!='Cancelled' group by cartnumber) sel on (shoppingcarts.cartnumber=sel.cartnumber and shoppingcarts.version=sel.version) where customeruserid='"+c.userid+"' and status!='Ordered' and status!='Cancelled' order by actorrole desc, date;;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.isBeforeFirst()){
				html+="<h2>Your saved carts</h2>";
				html+="<table>";
				while (rs.next()) {
					html+="<tr><td><a href='"+p.thispage.replaceAll("\\?.*$", "")+"?shopid="+rs.getInt("shopid")+"&amp;action=retrieve'>"+Webroutines.getShopNameFromShopId(rs.getInt("shopid"), "")+"</a></td><td>"+rs.getDate("date")+"</td><td>"+(rs.getString("actorrole").equals("Seller")?"Updated":"Unchanged")+"</tr>";
				}
				html+="</table>";
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return html;
	}



}

package com.freewinesearcher.online.shoppingcart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.batch.CurrencyClient;
import com.freewinesearcher.batch.Emailer;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.online.ExternalManager;
import com.freewinesearcher.online.PageHandler;
import com.freewinesearcher.online.Searchdata;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;

public class Shoppingcart implements Serializable{
	public static final long serialVersionUID = 1L;
	public int amount;
	public int wineid;
	public int shopid;
	public int cartnumber;
	public int version;
	public String order="";
	public String customeruserid;
	transient public Wine wine;
	public static enum statuses {Unverified,Verified,Ordered};
	public static enum flags {New,Price_changed,Deleted_by_buyer,Amount_changed,Unavailable,Confirmed};
	public static enum roles {Buyer,Seller,Admin};
	public static enum submitstatuses {OK,NOK};
	public HashMap<Integer,lineItem> cart=new HashMap<Integer,lineItem>();
	public Shipping shipping=new Shipping();
	public Vat vat=new Vat();
	public Vinopediacommission commission=new Vinopediacommission();
	public statuses status;
	int numberofbottles=0;
	public double totalamount=0;
	public double amountin=0;
	public boolean checked=false;
	public roles role=roles.Buyer;
	static double commissionvinopedia=0.07;
	static double discount=0.00;
	boolean priceincludesvat=false;
	public String currency;
	public double exchangerate;
	public boolean newcart=true;
	boolean changed=false;
	public String targetcountry="Belgium";
	public static double dutchvat=Dbutil.getVat("NL")/100;
	public String message="";
	public String messagelanguage="";
	public double vatdutyfactor=999;
	public boolean shophasemail=false;
	public boolean shopsupportsremotecart=false;





	public Shoppingcart(int shopid, Context c){
		super();
		this.shopid=shopid;
		this.customeruserid=c.userid;
		shophasemail=!Dbutil.readValueFromDB("select * from shops where id="+shopid, "email").equals("");
		shopsupportsremotecart=!Dbutil.readValueFromDB("select * from remotecart where shopid="+shopid+" and active=1", "active").equals("");
		refreshCurrency();
	}

	public void refreshCurrency(){
		this.currency=CurrencyClient.getCurrencyForShop(shopid);
		if (!currency.equals("")){
			this.exchangerate=CurrencyClient.convertCurrency(1, "EUR", currency);
		} else {
			this.exchangerate=1;
		}


	}



	public void checkOrder(){
		if (status!=statuses.Ordered) status=statuses.Verified;
		newcart=false;
		totalamount=0;
		numberofbottles=0;
		try {
			HashSet<Integer> remove=new HashSet<Integer>();
			for (int i : cart.keySet()) {

				if (cart.get(i).status.equals(statuses.Unverified)) {
					status = statuses.Unverified;
				}
				if (cart.get(i).flag.equals(flags.Unavailable)) {
					cart.get(i).amount=0;

				}
				numberofbottles+=cart.get(i).amount;
				totalamount+=cart.get(i).priceperitem*cart.get(i).amount;

			}
			for (int i : remove) {
				cart.remove(i);
			}

			if (numberofbottles!=shipping.numberofbottles) {// The verification was for a different number of bottles
				shipping.status=statuses.Unverified;
				shipping.flag=flags.Amount_changed;
			}
			if (!priceincludesvat){
				vat.amount=totalamount*vatdutyfactor;

			}

			if (vat.totalamountex!=totalamount&&!priceincludesvat){
				vat.status=statuses.Unverified;
			}
			if (vat.status.equals(statuses.Unverified)) {
				status = statuses.Unverified;
			}
			if (shipping.status.equals(statuses.Unverified)) {
				status = statuses.Unverified;
			}
			commission.setAmount(totalamount*commissionvinopedia);
			if (status == statuses.Verified) commission.status=statuses.Verified;
			checked=true;
		} catch (Exception e) {
			status=statuses.Unverified;
			totalamount=0;
			amountin=0;
			checked=false;
			Dbutil.logger.error("Problem: ", e);

		}

	}

	public submitstatuses submit(){
		for (int i : cart.keySet()) {
			if (cart.get(i).flag==flags.Deleted_by_buyer&&role.equals(roles.Seller)) cart.remove(i);
		}
		return submitstatuses.OK;
	}


	public void changeAmount(int amount){
		if (vatdutyfactor==999&&wineid>0) {
			try {double vatdutyfactor=Dbutil.getVat(new Wine(wineid).Country)/100;
			if (vatdutyfactor!=0) this.vatdutyfactor=vatdutyfactor;
			} catch (Exception e){}
		}
		if (cart.get(wineid)!=null) {
			cart.get(wineid).changeAmount(amount);
			if (cart.get(wineid).amount<0) cart.remove(wineid);
			if (cart.get(wineid).amount==0) cart.remove(wineid);
			changed=true;
		} else {
			cart.put(wineid, new lineItem(wineid,amount,priceincludesvat));
			if (!priceincludesvat){
				vat.amount=cart.get(wineid).amount*cart.get(wineid).priceperitem*vatdutyfactor;
				vat.totalamountex=cart.get(wineid).amount*cart.get(wineid).priceperitem;
			}
			changed=true;
		}


	}

	public void changePrice(double price){
		if (role==roles.Seller&&cart.get(wineid)!=null&&price>0) {
			cart.get(wineid).changePrice(price);
			changed=true;
		}
	}

	public void confirmPrice(double price){
		if (role==roles.Seller&&cart.get(wineid)!=null) {
			cart.get(wineid).confirmItem();
			changed=true;
		}
	}
	public void setVat(double price){
		if (role==roles.Seller) {
			vat.amount=price;
			checkOrder();
			vat.totalamountex=totalamount;
			vat.status=statuses.Verified;
			vat.flag=flags.Confirmed;
			changed=true;
		}
	}

	public void setShipping(double price){
		if (role==roles.Seller) {
			shipping.amount=price;
			checkOrder();
			shipping.numberofbottles=numberofbottles;
			shipping.status=statuses.Verified;
			shipping.flag=flags.Confirmed;
			changed=true;
		}
	}

	public void setOutofStock(){
		if (role==roles.Seller&&cart.get(wineid)!=null) {
			cart.get(wineid).setItemUnavailable(flags.Unavailable);
			changed=true;
		}
	}

	public int getAmount() {
		return amount;
	}


	public void setAmount(int amount) {
		if ((role==roles.Buyer||(role==roles.Seller&&this.amount>amount))&&cart.get(wineid)!=null&&cart.get(wineid).flag!=flags.Unavailable){
			this.amount = amount;
			changed=true;
		}
	}



	public void delete(){
		if (cart.get(shopid)!=null) {
			if (cart.get(wineid)!=null) {
				cart.remove(wineid);
				changed=true;
			}
		}
	}


	public int getShopid() {
		return shopid;
	}




	public int getWineid() {
		return wineid;
	}


	public void setWineid(int wineid) {
		if (wineid>0){
			try{
				this.wineid=wineid;
				wine=new Wine(wineid+"");

			} catch (Exception e){}
		}
	}


	public String getOrder() {
		if ("".equals(order)) return "bestdeal";
		return order;
	}


	public void setOrder(String order) {
		this.order = order;
	}

	public long save(Context c) throws Exception{
		long id=0;
		if(shopid>0&&c.userid!=null&&!c.userid.equals("")){
			id = CartSerializer.writeCart(c, this);
			if (id>0) changed=false;
			return id;
		} else {
			Dbutil.logger.error("Could not save cart because shopid=0");
		}
		return id;
	}

	public long confirmOrder(Context c) throws Exception{
		long id=0;
		if(shopid>0&&c.userid!=null&&!c.userid.equals("")){
			checkOrder();
			if (status==statuses.Verified){
				status=statuses.Ordered;

				id = CartSerializer.writeCart(c, this);
				if (id>0) changed=false;
				return id;
			} else {
				save(c);
				throw new CartSerializer.OrderNotVerifiedException();
			}
		} else {
			Dbutil.logger.error("Could not save cart because shopid=0");
		}
		return id;
	}



	public static class Vat implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double amount=0;
		double totalamountex=0;
		statuses status=statuses.Unverified;
		flags flag=flags.New;	

	}

	public static class Shipping implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double amount=0;
		int numberofbottles=0;
		statuses status=statuses.Unverified;
		flags flag=flags.New;	

	}

	public static class Vinopediacommission implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double amount=0;
		double vat=0;
		public static final String currency="EUR";
		statuses status=statuses.Unverified;

		public void setAmount(double amount){
			this.amount=amount;
			this.vat=amount*dutchvat;
		}
	}

	public static class lineItem implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int itemid;
		public int amount;
		public double priceperitem;
		public statuses status=statuses.Unverified;
		public flags flag=flags.New;

		public lineItem(int itemid,int amount, boolean priceincludesvat){
			try{
				Wine wine=new Wine(itemid);
				this.itemid=itemid;
				this.amount=amount;
				if (priceincludesvat){
					priceperitem=wine.Price;
				} else {
					priceperitem=wine.Price;
				}
			}catch(Exception e){}
			//priceeuroinperitem=wine.PriceEuroIn;
			status=statuses.Unverified;
		}

		private void changeAmount(int newamount){
			this.amount=newamount;
		}

		void changePrice(double price){
			this.priceperitem=price;
			this.status=statuses.Verified;
		}

		void confirmItem(){
			this.status=statuses.Verified;
			this.flag=flags.Confirmed;

		}

		void setItemUnavailable(flags flag){
			amount=0;
			this.status=statuses.Verified;
			this.flag=flag;
		}

		void checkLineItem(lineItem olditem, roles role){
			if (olditem!=null){
				if (amount!=olditem.amount){
					if (amount==0) {
						if (role.equals(roles.Buyer)){
							status=statuses.Verified;
							flag=flags.Deleted_by_buyer;
						}
					} else {
						status=statuses.Unverified;
						flag=flags.Amount_changed;
					}
				} else if (priceperitem!=olditem.priceperitem){
					status=statuses.Verified;
					flag=flags.Price_changed;
				}
			} else {
				status=statuses.Unverified;
				flag=flags.New;
			}

		}
	}

	public static class ConvertCurrency{
		double exchangerate;
		String fromsymbol;
		String tosymbol;
		boolean showboth;

		public ConvertCurrency(double exchangerate, String fromsymbol, String tosymbol,boolean showboth) {
			super();
			this.exchangerate=exchangerate;
			this.fromsymbol=fromsymbol;
			this.tosymbol=tosymbol;
			this.showboth=showboth;
		}

		public String get(double amount,statuses status,boolean exvat){
			String html="";
			if (showboth){
				html+=fromsymbol+" "+Webroutines.formatPrice(amount)+(status.equals(statuses.Verified)?"":"*");
				html+=" ("+tosymbol+" "+Webroutines.formatPrice(amount*exchangerate)+")"+(exvat?" (excl. VAT":"");
			} else {
				html+=tosymbol+" "+Webroutines.formatPrice(amount*exchangerate)+(status.equals(statuses.Verified)?"":"*")+(exvat?" (excl. VAT":"");
			}
			return html;
		}
	}

	public int sendByEmail(String emailsender, String shippingcountry,String message){
		Emailer e=new Emailer();
		e.replyto=emailsender;
		ArrayList<String> cc=new ArrayList<String>();
		cc.add(emailsender);
		ArrayList<String> bcc=new ArrayList<String>();
		bcc.add("orders@vinopedia.com");
		e.cc=cc;
		e.bcc=bcc;
		String footer="<br/><br/><div style='font-size:11px;'>This email message was sent through <a href='https://www.vinopedia.com'>vinopedia.com</a>. If you have any questions, feel free to contact us on <a href='mailto:management@vinopedia.com'>management@vinopedia.com</a> or <a href='https://www.vinopedia.com/contact.jsp'>https://www.vinopedia.com/contact.jsp</a></div>";
		String shipping="<br/>The wines should be shipped to "+shippingcountry+". ";
		message=message.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>").replace("[email address]",emailsender)+shipping+footer;
		String emailshop=Dbutil.readValueFromDB("select * from shops where id="+shopid, "email");
		String subject="Information request for buying wines at "+Webroutines.getShopNameFromShopId(shopid, "");
		if (emailshop!=null&&emailshop.length()>5){
			
			if (e.sendEmail("orders@vinopedia.com", emailshop, subject, message)) {
				
				return 1;
			}

		}
		return 0;
	}


	public String getCart4Email(String emailsender){
		String html="Hello,\n\nI am interested in buying the following wines from you:\n\n";
		for (int i:cart.keySet()) {
			try{
				Wine thiswine=new Wine(i+"");
				html+=(((thiswine.Vintage.equals("0")?"":(thiswine.Vintage+" "))+thiswine.Name).trim()+", "+Webroutines.formatSizecompact(thiswine.Size)+": "+cart.get(i).amount+" bottle"+(cart.get(i).amount>1?"s":"")+"\n");
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
		}
		html+="\nPlease contact me via email at [email address] with details about pricing, availability, shipping costs and payment conditions. ";

		return html;
	}

	public String getSimpleCartHTML(PageHandler p,Shoppingcart oldcart){
		String html="";
		checkOrder();
		String displaycurrency=p.searchdata.getCurrency();
		if (displaycurrency.equals("")) displaycurrency="EUR";
		boolean showbothprices=false;
		String currencysymbol=Webroutines.getCurrencySymbol(currency);
		String displaycurrencysymbol=Webroutines.getCurrencySymbol(displaycurrency);
		String thispage=p.thispage.replaceAll("\\?.*$", "");
		if (cart.size()>0) {
			html+="<h1>"+(status.equals(Shoppingcart.statuses.Unverified)?p.t.get("yourshoppinglist"):p.t.get("yourorder"))+"</h1>";
			if (!displaycurrency.equals(currency)&&!newcart){
				showbothprices=true;
				html+="Note: the prices in "+Webroutines.getCurrencySymbol(displaycurrency)+" are only an indication, as conversion rates may vary over time (we used exchange rate "+Webroutines.getCurrencySymbol(displaycurrency)+" 1,00 = "+Webroutines.getCurrencySymbol(currency)+" "+Webroutines.formatPrice((double)CurrencyClient.toEuro(1, currency)/(double)CurrencyClient.toEuro(1, displaycurrency))+").<br/>";
			}
			ConvertCurrency c=new ConvertCurrency(CurrencyClient.convertCurrency(1, currency, displaycurrency),currencysymbol,displaycurrencysymbol,showbothprices);
			html+="<table class='shoppinglist'><tr><th class='name'>"+Spider.escape(p.t.get("winename"))+"</th><th class='order'>"+p.t.get("order")+"</th><th class='amount'>"+p.t.get("amount")+"</th></tr>";
			for (int i:cart.keySet()) {
			try{
				Wine thiswine=new Wine(i+"");
				html+=("<tr><td class='name'><a id='cart-"+thiswine.Id+"' onclick=\"javascript:showWine("+thiswine.Id+",&quot;cart-"+thiswine.Id+"&quot;);$('#storeiframe').attr('src','"+ExternalManager.addGoogleParams(thiswine.SourceUrl.replaceAll("'", "&apos;"))+"');$('#storetabs').tabs().click(0);\">"+((thiswine.Vintage.equals("0")?"":(thiswine.Vintage+" "))+Spider.escape(thiswine.Name)).trim()+", "+Webroutines.formatSizecompact(thiswine.Size)+"</a></td>");
				html+="<td class='order' >"+(cart.get(i).flag==flags.Unavailable?"<b><strike>":(cart.get(i).amount>0?"<img src='/images/minus.gif' onclick='javascript:changeamount("+cart.get(i).itemid+","+(cart.get(i).amount-1)+");'/>":""))+" "+cart.get(i).amount+" "+(cart.get(i).flag==flags.Unavailable?"</strike></b>":"<img src='/images/plus.gif' onclick='javascript:changeamount("+cart.get(i).itemid+","+(cart.get(i).amount+1)+");'/>")+"</td>";
				html+="<td class='amount'>"+(cart.get(i).flag==flags.Unavailable?p.t.get("itemwasmarkedunavailable"):c.get(cart.get(i).priceperitem*cart.get(i).amount, cart.get(i).status, priceincludesvat))+"</td>";
				html+="</tr>";
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
			}
			html+="<tr><td class='name'>"+p.t.get("subtotal")+""+(priceincludesvat?"":" "+p.t.get("excludingvat"))+"</td><td class='order'></td><td class='amount'>"+c.get(totalamount, statuses.Verified, priceincludesvat)+"&nbsp;&nbsp;</td></tr>";
			html+="</table><br/>";
			if (status!=statuses.Verified) {
				html+="Prices exclude VAT and additional costs. <br/>*"+p.t.get("notyetchecked")+"<br/><br/>";
			}
			if (shopsupportsremotecart){
				html+=p.t.get("preliminarylist")+"<br/>"; 
				html+="<form action='/js/cart.jsp' method='post'>";
				html+="<input type='hidden' name='action' value='fillremotecart'/>";
				html+="<input type='submit' value='"+"Checkout"+"'/>";
				html+="</form>";
			} else 	if (shophasemail){
				html+=p.t.get("preliminarylist")+"<br/>"; 
				html+="<form action='/store/sendshoppinglist.jsp' method='post'>";
				html+="<input type='hidden' name='action' value='save'/>";
				html+="<input type='submit' value='"+p.t.get("emailshoppinglist")+"'/>";
				html+="</form>";
			} else {
				html+="<br/>We have no email address from this merchant. Please visit the merchants web site to buy these wines. ";
			}

		} else {
			html+="<h3>Your shopping list is empty.</h3>You can add wines to your shopping list by selecting a wine and clicking the &quot;add to shoppinglist&quot; button.";

		}
		return html;



	}
	public String getCartHTML(PageHandler p,Shoppingcart oldcart){
		String html="";
		checkOrder();
		String displaycurrency=p.searchdata.getCurrency();
		if (displaycurrency.equals("")) displaycurrency="EUR";
		boolean showbothprices=false;
		String currencysymbol=Webroutines.getCurrencySymbol(currency);
		String displaycurrencysymbol=Webroutines.getCurrencySymbol(displaycurrency);
		if (role==roles.Seller) {
			displaycurrency=currency;
			displaycurrencysymbol=currencysymbol;
		}
		String thispage=p.thispage.replaceAll("\\?.*$", "");

		if (cart.size()>0) {
			if (status!=statuses.Ordered){
				if (role==roles.Seller){
					html+="<h1>"+p.t.get("customerorder")+"</h1>";
					ConvertCurrency c=new ConvertCurrency(CurrencyClient.convertCurrency(1, currency, displaycurrency),currencysymbol,displaycurrencysymbol,showbothprices);
					html+="<table class='shoppinglist'><tr><th class='name'>"+p.t.get("wine")+"</th><th>"+p.t.get("order")+"</th><th>"+p.t.get("amount")+"</th><th>"+p.t.get("priceeach")+"</th></tr>";
					for (int i:cart.keySet()) {
						try{
							Wine thiswine=new Wine(i+"");
						
						html+=("<tr><td class='name'>"+(cart.get(i).flag==flags.Unavailable?"<b><strike>":"")+(thiswine.Vintage+" "+Spider.escape(thiswine.Name)).trim()+(cart.get(i).flag==flags.Unavailable?"</strike></b>":"")+"</td>");
						html+="<td class='name'>"+(cart.get(i).amount>0?cart.get(i).amount+" x "+Webroutines.formatSizecompact(thiswine.Size)+", "+c.get(cart.get(i).priceperitem, cart.get(i).status, false)+p.t.get("each"):"")+"</td>";
						html+="<td>"+c.get(cart.get(i).priceperitem*cart.get(i).amount, cart.get(i).status, false)+"</td>";
						html+="<td>"+(cart.get(i).flag!=flags.Unavailable?"<div class='buttonform'><form  class='buttonform' action='"+thispage+"'><input type='hidden' name='wineid' value='"+cart.get(i).itemid+"'/><input type='text' name='price' size='4' value='"+Webroutines.formatPrice(cart.get(i).priceperitem)+"'/><input type='hidden' name='action' value='changeprice'/><input type='submit' value='"+p.t.get("editprice")+"'/></form></div><div class='buttonform'><form action='"+thispage+"'><input type='hidden' name='wineid' value='"+cart.get(i).itemid+"'/><input type='hidden' name='action' value='outofstock'/><input type='submit' value='"+p.t.get("itemunavailable")+"'/></form></div><div class='buttonform'><form  class='buttonform' action='"+thispage+"'><input type='hidden' name='wineid' value='"+cart.get(i).itemid+"'/><input type='hidden' name='action' value='approve'/><input type='submit' value='"+p.t.get("confirm")+"'/></form></div>":"")+"</td>";
						html+="</tr>";
						} catch (Exception e) {
							Dbutil.logger.error("Problem: ", e);
						}
					}
					if (oldcart!=null) for (int i:oldcart.cart.keySet()) {
						if (cart.get(i)==null&&oldcart.cart.get(i).flag==flags.Unavailable){
							try{
							Wine thiswine=new Wine(i+"");
							html+=("<tr><td class='name'><b><strike>"+(thiswine.Vintage+" "+thiswine.Name).trim()+"</strike></b></td>");
							html+="<td class='name'><b><strike>"+oldcart.cart.get(i).amount+" x "+Webroutines.formatSizecompact(thiswine.Size)+", "+c.get(cart.get(i).priceperitem, cart.get(i).status, false)+p.t.get("each")+"</strike></b></td>";
							html+="<td><b><strike>"+c.get(cart.get(i).priceperitem*cart.get(i).amount, cart.get(i).status, priceincludesvat)+"</strike> "+p.t.get("itemwasmarkedunavailable")+"</b></td>";
							html+="</tr>";
							} catch (Exception e) {
								Dbutil.logger.error("Problem: ", e);
							}
						}
					}

					html+="<tr><td class='name'></td><td></td><td></td></tr>";
					html+="<tr><td class='name'>"+p.t.get("subtotal")+""+(priceincludesvat?"":" "+p.t.get("excludingvat"))+"</td><td></td><td>"+c.get(totalamount, statuses.Verified, priceincludesvat)+"</td></tr>";
					html+="<tr><td class='name'>"+p.t.get("shippingcosts")+(shipping.status==statuses.Unverified?"*":"")+"</td><td></td><td colspan='2'><form action='"+p.thispage.replaceAll("\\?.*$", "")+"' method='post'>"+currencysymbol+" "+"<input type='text' name='price' size='4' value='"+Webroutines.formatPrice(shipping.amount)+"'/><input type='hidden' name='action' value='updateshipping'/><input type='submit' value='"+p.t.get("updatetransport")+"'/></form></td></tr>";
					if (!priceincludesvat) {
						html+="<tr><td class='name'>"+p.t.get("vatplusduty")+(vat.status==statuses.Unverified?"*":"")+"</td><td></td><td colspan='2'><form action='"+p.thispage.replaceAll("\\?.*$", "")+"' method='get'>"+currencysymbol+" "+"<input type='text' name='price' size='4' value='"+Webroutines.formatPrice(vat.amount)+"'/><input type='hidden' name='action' value='updatevat'/><input type='submit' value='"+p.t.get("updatevat")+"'/></form></td></tr>";
					}
					html+="<tr><td class='name'>"+p.t.get("totalamount")+(shipping.amount==0&&shipping.status!=statuses.Verified?" "+p.t.get("excludingshippingcosts"):"")+"</td><td></td><td>"+c.get(totalamount+shipping.amount+vat.amount, statuses.Verified, priceincludesvat)+"</td></tr>";
					html+="<tr><td class='name'>"+p.t.get("vinopediacommission")+"</td><td>"+Webroutines.formatPrice(commission.amount,commission.amount,Vinopediacommission.currency,"EX")+" + "+p.t.get("vat")+" "+Webroutines.formatPrice(commission.vat,commission.vat,Vinopediacommission.currency,"EX")+"</td><td>"+Webroutines.formatPrice(commission.vat+commission.amount,commission.vat+commission.amount,Vinopediacommission.currency,"EX")+"</td></tr>";
					html+="</table><br/>";
					if (status!=statuses.Verified) {
						html+="* "+p.t.get("checkitems")+"<br/><br/>";
					}
					if (status==statuses.Verified&&changed){
						html+=p.t.get("approveorder");
						html+="<form action='/store/sendshoppinglist.jsp' method='post'>";
						html+="<input type='submit' value='"+p.t.get("askquote")+"'/>";
						html+="</form>";
					}
				} else {
					html+="<h1>"+(status.equals(Shoppingcart.statuses.Unverified)?p.t.get("yourshoppinglist"):p.t.get("yourorder"))+"</h1>";
					if (!displaycurrency.equals(currency)&&!newcart){
						showbothprices=true;
						html+="Note: the prices shown in "+Webroutines.getCurrencySymbol(currency)+" are the actual prices you will be charged for if you decide to order. The prices in ("+Webroutines.getCurrencySymbol(displaycurrency)+") are only an indication as conversion rated may vary over time (using exchange rate "+Webroutines.getCurrencySymbol(displaycurrency)+" 1,00 = "+Webroutines.getCurrencySymbol(currency)+" "+Webroutines.formatPrice((double)CurrencyClient.toEuro(1, currency)/(double)CurrencyClient.toEuro(1, displaycurrency))+").<br/>";
					}
					ConvertCurrency c=new ConvertCurrency(CurrencyClient.convertCurrency(1, currency, displaycurrency),currencysymbol,displaycurrencysymbol,showbothprices);
					html+="<table class='shoppinglist'><tr><th class='name'>"+p.t.get("wine")+"</th><th>"+p.t.get("order")+"</th><th>"+p.t.get("amount")+"</th></tr>";
					for (int i:cart.keySet()) {
						try{
						Wine thiswine=new Wine(i+"");
						html+=("<tr><td class='name'>"+((thiswine.Vintage.equals("0")?"":(thiswine.Vintage+" "))+thiswine.Name).trim()+", "+Webroutines.formatSizecompact(thiswine.Size)+"</td>");
						html+="<td class='name'>"+(cart.get(i).flag==flags.Unavailable?"<b><strike>":(cart.get(i).amount>0?"<img src='/images/minus.gif' onclick='javascript:changeamount("+cart.get(i).itemid+","+(cart.get(i).amount-1)+");'/>":""))+" "+cart.get(i).amount+" "+(cart.get(i).flag==flags.Unavailable?"</strike></b>":"<img src='/images/plus.gif' onclick='javascript:changeamount("+cart.get(i).itemid+","+(cart.get(i).amount+1)+");'/>")+"</td>";
						html+="<td>"+(cart.get(i).flag==flags.Unavailable?p.t.get("itemwasmarkedunavailable"):c.get(cart.get(i).priceperitem*cart.get(i).amount, cart.get(i).status, priceincludesvat))+"</td>";
						html+="</tr>";
						} catch (Exception e) {
							Dbutil.logger.error("Problem: ", e);
						}
					}

					html+="<tr><td class='name'></td><td></td><td></td></tr>";
					html+="<tr><td class='name'>"+p.t.get("subtotal")+""+(priceincludesvat?"":" "+p.t.get("excludingvat"))+"</td><td></td><td>"+c.get(totalamount, statuses.Verified, priceincludesvat)+"</td></tr>";
					html+="<tr><td class='name'>"+p.t.get("shippingcosts")+"</td><td></td><td>"+(shipping.amount==0?p.t.get("unknown"):c.get(shipping.amount, shipping.status, priceincludesvat))+"</td></tr>";
					if (!priceincludesvat) {
						html+="<tr><td class='name'>"+p.t.get("vatplusduty")+"</td><td></td><td>"+c.get(vat.amount, vat.status, priceincludesvat)+"</td></tr>";
					}
					html+="<tr><td class='name'>"+p.t.get("totalamount")+(shipping.amount==0&&shipping.status!=statuses.Verified?" "+p.t.get("excludingshippingcosts"):"")+"</td><td></td><td>"+c.get(totalamount+shipping.amount+vat.amount, status, priceincludesvat)+"</td></tr>";
					html+="</table><br/>";
					if (status!=statuses.Verified) {
						html+="* "+p.t.get("notyetchecked")+"<br/><br/>";
					}
					if (changed) {
						if (shophasemail){
							html+=p.t.get("preliminarylist")+"<br/>"; 
							html+="<form action='/settings/sendshoppinglist.jsp' method='post'>";
							html+="<input type='hidden' name='action' value='save'/>";
							html+="<input type='submit' value='"+p.t.get("emailshoppinglist")+"'/>";
							html+="</form>";
						} else {
							html+="<br/>We have no email address from this merchant. Please visit the merchants web site to buy these wines. ";
						}
					} else if (status==statuses.Verified){
						html+="<form action='"+thispage+"' method='post'>";
						html+="<input type='hidden' name='action' value='placeorder'/>";
						html+="<input type='submit' value='"+(newcart?p.t.get("askquote"):status==statuses.Verified?p.t.get("placeorder"):p.t.get("updatecart"))+"'/>";
						html+="</form>";
					}
				}
			} else {
				html+="<h1>"+p.t.get("order")+"</h1>";
				ConvertCurrency c=new ConvertCurrency(CurrencyClient.convertCurrency(1, currency, currency),currencysymbol,currencysymbol,false);
				html+="<table class='shoppinglist'><tr><th class='name'>"+p.t.get("wine")+"</th><th>"+p.t.get("order")+"</th><th>"+p.t.get("amount")+"</th></tr>";
				for (int i:cart.keySet()) {
					if (cart.get(i).amount>0){
						try{
						Wine thiswine=new Wine(i+"");
						html+=("<tr><td class='name'>"+(thiswine.Vintage+" "+thiswine.Name).trim()+"</td>");
						html+="<td class='name'>"+cart.get(i).amount+" x "+Webroutines.formatSizecompact(thiswine.Size)+", "+c.get(cart.get(i).priceperitem, cart.get(i).status, false)+p.t.get("each")+"</td>";
						html+="<td>"+c.get(cart.get(i).priceperitem*cart.get(i).amount, cart.get(i).status, priceincludesvat)+"</td>";
						html+="</tr>";
						} catch (Exception e) {
							Dbutil.logger.error("Problem: ", e);
						}
					}
				}

				html+="<tr><td class='name'></td><td></td><td></td></tr>";
				html+="<tr><td class='name'>"+p.t.get("subtotal")+""+(priceincludesvat?"":" "+p.t.get("excludingvat"))+"</td><td></td><td>"+c.get(totalamount, statuses.Verified, priceincludesvat)+"</td></tr>";
				html+="<tr><td class='name'>"+p.t.get("shippingcosts")+"</td><td></td><td>"+c.get(shipping.amount, shipping.status, priceincludesvat)+"</td></tr>";
				if (!priceincludesvat) {
					html+="<tr><td class='name'>"+p.t.get("vatplusduty")+"</td><td></td><td>"+c.get(vat.amount, vat.status, priceincludesvat)+"</td></tr>";
				}
				html+="<tr><td class='name'>"+p.t.get("totalamount")+(shipping.amount==0&&shipping.status!=statuses.Verified?" "+p.t.get("excludingshippingcosts"):"")+"</td><td></td><td>"+c.get(totalamount+shipping.amount+vat.amount, status, priceincludesvat)+"</td></tr>";
				if (role==roles.Seller) html+="<tr><td class='name'>"+p.t.get("vinopediacommission")+"</td><td>"+Webroutines.formatPrice(commission.amount,commission.amount,Vinopediacommission.currency,"EX")+" + "+p.t.get("vat")+" "+Webroutines.formatPrice(commission.vat,commission.vat,Vinopediacommission.currency,"EX")+"</td><td>"+Webroutines.formatPrice(commission.vat+commission.amount,commission.vat+commission.amount,Vinopediacommission.currency,"EX")+"</td></tr>";
				html+="</table><br/>";
			}

		} else {
			html+="<h3>Your shopping list is empty.</h3>You can add wines to your shopping list by selecting a wine and clicking the &quot;add to shoppinglist&quot; button.";

		}
		return html;

	}




}



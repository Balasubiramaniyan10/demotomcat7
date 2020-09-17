package com.vinopedia.affiliates;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;

public class Sale {
	private int shopid;
	private java.sql.Timestamp cookiedate;
	private Float amount;
	private Float commissionpercentage;
	private String currency;
	private String reference;
	private String ip;
	private String requesturl;
	private String hostcountry;


	public Sale() {
	}

	public void add(){
		Dbutil.executeQuery("insert into affiliatetracking(shopid,transactiondate,cookiedate,amount,currency,commissionpercentage,ip,reference,hostcountry,requesturl) values ("+shopid+",now(),'"+cookiedate+"',"+amount+",'"+Spider.SQLEscape(currency)+"',"+commissionpercentage+",'"+Spider.SQLEscape(ip)+"','"+Spider.SQLEscape(reference)+"','"+Spider.SQLEscape(hostcountry)+"','"+Spider.SQLEscape(requesturl)+"');");
	}

	public int getShopid() {
		return shopid;
	}

	public void setShopid(int shopid) {
		this.shopid = shopid;
	}

	public java.sql.Timestamp getCookiedate() {
		return cookiedate;
	}

	public void setCookiedate(java.sql.Timestamp cookiedate) {
		this.cookiedate = cookiedate;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public void setAmount(String amount) {
		try{this.amount = Float.parseFloat(amount);}catch(Exception e){
			try{this.amount = Float.parseFloat(amount.replaceAll(",", "."));}catch(Exception f){}
		}
	}

	public Float getCommissionpercentage() {
		return commissionpercentage;
	}

	public void setCommissionpercentage(Float commissionpercentage) {
		this.commissionpercentage = commissionpercentage;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRequesturl() {
		return requesturl;
	}

	public void setRequesturl(String requesturl) {
		this.requesturl = requesturl;
	}

	public String getHostcountry() {
		return hostcountry;
	}

	public void setHostcountry(String hostcountry) {
		this.hostcountry = hostcountry;
	}


}

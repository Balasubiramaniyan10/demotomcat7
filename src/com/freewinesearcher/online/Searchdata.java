package com.freewinesearcher.online;

import java.io.Serializable;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Translator.languages;



public class Searchdata  implements Serializable{
	private static final long serialVersionUID = 1L;

	String name;
	public String vintage;
	String message; 
	String country;
	String lastcountry="";
	String hostcountry="";
	String vat;
	float pricemin;
	float pricemax;
	String createdstring;
	String priceminstring;
	String pricemaxstring;
	String rareoldstring;
	String currency;
	public String order;
	Translator.languages language;
	int offset;
	int created;
	Float size;
	public int freetextresults;
	public boolean freetext=false;
	int history=7;
	public boolean fuzzy=false;
	public String referrer="";
	public boolean sponsoredresults=false;
	public int numberofrows=25;
	public int sponsoredrows=3;
	public float lat=0;
	public float lon=0;
	
	public Float getSize() {
		if (size==null) return (float)0;
		return size;
	}
	public void setSize(String sizestr) {
		Float size=(float)0;
		try{size=Float.parseFloat(sizestr);}catch(Exception e){}
		this.size = size;
	}
	
	boolean rareold=Boolean.parseBoolean(rareoldstring);
	
	public String getCountry() {
		if (country==null) country="All";
		if (country.equals("")) country="All";
		return country;
	}
	public void setCountry(String country) {
		if (country==null) country="All";
		if (country.equals("")) country="All";
		this.country = Webroutines.filterUserInput(country);
	}
	public String getLastcountry() {
		if (lastcountry==null) return getCountry();
		return lastcountry;
	}
	public String getCurrency() {
		if (currency==null) currency="EUR";
		if (currency.equals("undefined")) currency="EUR";
		
		return currency;
	}
	public void setCurrency(String currency) {
		if (currency==null) currency="";
		this.currency = Webroutines.filterUserInput(currency);
	}
	public int getCreated(){
		if (createdstring==null||createdstring.equals("null")) 
			createdstring="0";
			return Integer.parseInt(createdstring);
	}
	
	public String getCreatedstring() {
		if (createdstring==null||createdstring.equals("null")) 
			createdstring="0";
			return createdstring;
			
	}
	public void setCreatedstring(String createdstring) {
		if (createdstring==null||createdstring.equals("null")) 
		createdstring="0";
		this.createdstring = Webroutines.filterUserInput(createdstring);
	}
	public int getHistory() {
		return history;
	}
	public void setHistory(String history) {
		try{
		this.history = Integer.parseInt(history);
		} catch (Exception e){}
	}
	public String getHostcountry() {
		if (hostcountry==null) return ""; 
		return hostcountry;
	}
	public void setHostcountry(String hostcountry) {
		this.hostcountry = hostcountry;
	}
	public String getMessage() {
		if (message==null) message="";
		return message;
	}
	public void setMessage(String message) {
		if (message==null) message="";
		this.message = Webroutines.filterUserInput(message);
	}
	public String getOrder() {
		if (order==null) order="";
		return order;
	}
	public void setOrder(String order) {
		if (order==null) order="";
		order=order.toLowerCase();
		if (this.order!=null&&this.order.equals(order)&&!order.equals("")){
		this.order = Webroutines.filterUserInput(order+" desc");
		} else {
		this.order = Webroutines.filterUserInput(order);
		}
		this.order=this.order.replace(" desc desc", "");
		order=this.order.replace(" desc", "");
		if (!order.equals("priceeuroex")&&!order.equals("vintage")&&!order.equals("size")&&!order.equals("distance")){
			this.order="";
		}
	}
	public String getName() {
		if (name==null) name="";
		return name;
	}
	public void setName(String name) {
		if (name==null) name="";
		this.name = Webroutines.filterUserInput(name).replaceAll("� +", "�");
	}
	public int getNumberofrows() {
		if (numberofrows==0) numberofrows=Configuration.numberofnormalrows;
		return numberofrows;
	}
	public void setNumberofrows(int numberofrows) {
		if (numberofrows==0) numberofrows=Configuration.numberofnormalrows;
		this.numberofrows = numberofrows;
	}
	public int getOffset() {
		return offset;//
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public float getPricemax() {
		pricemax=0;
		try {
			if (!getPricemaxstring().equals("")) pricemax=Float.valueOf(getPricemaxstring()).floatValue();
		} catch (Exception exc) {
			message="Price range values are incorrect. Please enter a price like '1500,00'";
		}
		return pricemax;
	}
	public void setPricemax(float pricemax) {
		//this.pricemax = pricemax;
	}
	public String getPricemaxstring() {
		if (pricemaxstring==null) pricemaxstring="";
		return pricemaxstring;
	}
	public void setPricemaxstring(String pricemaxstring) {
		if (pricemaxstring==null) pricemaxstring="";
		this.pricemaxstring = pricemaxstring;
	}
	public float getPricemin() {
		pricemin=0;
		try {
			if (!getPriceminstring().equals("")) pricemin=Float.valueOf(getPriceminstring()).floatValue();
		} catch (Exception exc) {
			message="Price range values are incorrect. Please enter a price like '1500,00'";
		}return pricemin;
	}
	public void setPricemin(float pricemin) {
		//this.pricemin = pricemin;
	}
	public String getPriceminstring() {
		if (priceminstring==null) priceminstring="";
		return priceminstring;
	}
	public void setPriceminstring(String priceminstring) {
		if (priceminstring==null) priceminstring="";
		this.priceminstring = Webroutines.filterUserInput(priceminstring);
	}
	public boolean isRareold() {
		if (rareoldstring==null) rareoldstring="false";
			return Boolean.parseBoolean(rareoldstring);
	}
	public boolean getRareold() {
		boolean rareold=false;
		if (rareoldstring==null) rareoldstring="false";
		try {rareold=Boolean.parseBoolean(rareoldstring);} catch(Exception e){}
		return rareold;
	}
	public String getRareoldstring() {
		if (rareoldstring==null) rareoldstring="false";
			
		return rareoldstring;
	}
	public void setRareoldstring(String rareoldstring) {
		if (rareoldstring==null) rareoldstring="false";
		this.rareoldstring = Webroutines.filterUserInput(rareoldstring);
	}
	public String getVat() {
		if (vat==null||vat.equals("")) vat="EX";
			return vat;
	}
	public void setVat(String vat) {
		if (vat==null||vat.equals("")) vat="EX";
		this.vat = vat;
	}
	public String getVintage() {
		if (vintage==null) vintage="";
			return vintage;
	}
	public void setVintage(String vintage) {
		if (vintage==null) vintage="";
		vintage=Webroutines.filterVintage(vintage);
		vintage=Webroutines.filterUserInput(vintage);
		this.vintage = vintage;
	}
	public String getLanguage() {
		String lang="";
		if (language!=null) lang=language.toString();
		return lang;
	}
	public void setLanguage(Translator.languages language) {
		this.language = language;
	}
	public void setLanguage(String language) {
		this.language=Translator.getLanguage(language); 
	}
	public boolean isFreetext() {
		return freetext;
	}
	public void setFreetext(boolean freetext) {
		this.freetext = freetext;
	}
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public float getLon() {
		return lon;
	}
	public void setLon(float lon) {
		this.lon = lon;
	}
	
	
	
}
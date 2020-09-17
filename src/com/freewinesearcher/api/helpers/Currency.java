package com.freewinesearcher.api.helpers;



public class Currency {
	public Double pricefactor=0.0;
	public String symbol="";
	public String isocode="";
	
	public Currency(Object cur){
		String currency=(String)cur;
		if (currency==null) currency="EUR";
		currency=currency.toUpperCase();
		pricefactor=com.freewinesearcher.online.Currency.getRate(currency);
		if (pricefactor==0.0||pricefactor==1.0){
			currency="EUR";
			pricefactor=com.freewinesearcher.online.Currency.getRate(currency);
		}
		isocode=currency;
		symbol=com.freewinesearcher.online.Currency.getSymbol(currency);
	}
}

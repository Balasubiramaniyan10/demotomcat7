package com.vinopedia.paypal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.net.ssl.HttpsURLConnection;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;
import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;

public class Button {

	String websitecode="";
	String emaillink="";
	Double amount=0.0;
	String currency="USD";
	int store=0;
	String invoice="";
	public String message="";
	boolean sandbox=false;

	public boolean isValid(){
		if (amount==null||amount<1) return false;
		if (currency.length()!=3) return false;
		if (invoice==null||invoice.equals("")) return false;
		if ("".equals(Webroutines.getShopNameFromShopId(store, ""))) return false;
		return true;
	}
	
	public void clear(){
		websitecode="";
	}
	
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public int getStore() {
		return store;
	}

	public void setStore(int store) {
		this.store = store;
	}

	public String getInvoice() {
		return invoice;
	}

	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}

	public String getWebsitecode() throws Exception{
		if ("".equals(websitecode)){
			createEncryptedButton();
		}
		return websitecode;
	}
	public String getEmaillink() throws Exception{
		if ("".equals(emaillink)){
			createEncryptedButton();
		}
		return emaillink;
	}
	
	public String getAmountstr(){
		DecimalFormat snf=new DecimalFormat("#0.00");
		DecimalFormatSymbols custom=new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		snf.setDecimalFormatSymbols(custom);
		return snf.format(amount);
	}
	
	public Button(){
		
	}
	
	public Button(Double amount, String currency, String invoice, int store){
		this.amount=amount;
		this.currency=currency;
		this.invoice=invoice;
		this.store=store;
	}
	
	public void createEncryptedButton() throws Exception {    
		message="";
		NVPEncoder encoder = new NVPEncoder();
		String item_name="Vinopedia payment for listing";
		if (store>0) item_name="Listing of "+Webroutines.getShopNameFromShopId(store, "");
	    encoder.add("METHOD","BMCreateButton");
	    //encoder.add("BUTTONCODE","TOKEN");
	    encoder.add("BUTTONTYPE","BUYNOW");
	    encoder.add("BUTTONSUBTYPE","SERVICES");
	    encoder.add("L_BUTTONVAR1","amount="+getAmountstr());
	    encoder.add("L_BUTTONVAR2","item_name="+item_name);
	    encoder.add("L_BUTTONVAR3","currency_code="+currency);
	    encoder.add("L_BUTTONVAR4","invoice="+invoice);
	    encoder.add("L_BUTTONVAR5","image_url=https:/www.vinopedia.com/images/logopaypal.png");
	    encoder.add("L_BUTTONVAR6","lc=US");
	    encoder.add("L_BUTTONVAR8","rm=2");
	    if (sandbox){
	    	
	    } else {
	    	encoder.add("L_BUTTONVAR7","return=https://www.vinopedia.com/thankyou.jsp");
		    encoder.add("L_BUTTONVAR0","business="+"management@vinopedia.com"); 
		    encoder.add("USER", Configuration.PayPalApiUsername);
		    encoder.add("PWD",Configuration.PayPalApiPassword); //API Password
		    encoder.add("SIGNATURE",Configuration.PayPalApiSignature); //API Signature
	    	
	    }
	    encoder.add("VERSION","65.2"); //Version numbers differ from Paypal and Sandbox site. Do View > Source and look in source code for current version number under each site.

	    String strNVPString = encoder.encode();
	    //Dbutil.logger.info(strNVPString);
	    String ppresponse = call(strNVPString);
	    //Dbutil.logger.info(ppresponse);
	    NVPDecoder results = new NVPDecoder();
	    results.decode(ppresponse);                

	    websitecode = results.get("WEBSITECODE");
	    emaillink = results.get("EMAILLINK");
	    if (!"Success".equals(results.get("ACK"))) message=results.get("L_LONGMESSAGE0");

	                

	}

	   public String call(String payload) throws Exception {

	//Remember to setup your API credentials, whether you're using Sandbox
	//for testing or Paypal when you go live


	//this is for Sandbox testing
	//when you go live with paypal, switch it to
	//https://api-3t.paypal.com/nvp 
		   URL url;
		   if (sandbox){
			   url = new URL("https://api-3t.sandbox.paypal.com/nvp");
			      
		   } else {
			   url = new URL("https://api-3t.paypal.com/nvp");
		   }
		   
	        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
	        connection.setDoOutput(true);
	        connection.setUseCaches(false);
	        connection.setRequestProperty("Content-Type", "text/namevalue");
	        DataOutputStream outst = new DataOutputStream(connection.getOutputStream());        
	        outst.write(payload.getBytes());
	        outst.close();

	        // Read the gateway response
	        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        StringBuffer sb = new StringBuffer();
	        String line;
	        while ((line = in.readLine()) != null) {
	            sb.append(line);
	        }
	        in.close();
	        return sb.toString();
	    } // call
}

package com.freewinesearcher.api2;

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.PageHandler;

public class ApiHandler {
	long key=(long)0;
	String clientid;
	double version=0.0;
	public enum Formats {XML,JSON};
	private Formats format;
	private HashMap<Long,String> keys;
	private int statuscode=0;
	private String errormessage="";
	
	
	public ApiHandler(){
		fillKeys();
		
	}
	
	private void fillKeys(){
		keys=new HashMap<Long, String>();
		keys.put((long)285039823, "App iPhone");
		keys.put((long)542334626, "test");
	}
	
	public ApiHandler(String format, String key, String clientid, String version){
		setStatuscode(200);
		fillKeys();
		setClientid(clientid);
		setKey(key);
		setVersion(version);
		setFormat(format);
	}
	
	public boolean isValid(){
		if (statuscode==200){
			return true;
		}
		return false;
	}

	public long getKey() {
		return key;
	}

	public void setKey(String key) {
		try{this.key=Integer.parseInt(key);}catch(Exception e){}
		if (!keys.containsKey(this.key)){
			setErrormessage("Invalid key");
			setStatuscode(HttpServletResponse.SC_FORBIDDEN);
			Dbutil.logger.warn("Invalid api key "+key);			}
		
	}
	
	public String getAPICaller(){
		if (key>0) return keys.get(key);
		return "API Unknown source";
		
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		if (clientid==null) clientid="";
		if (clientid.length()>50) clientid=clientid.substring(0, 49);
		this.clientid=clientid;
		if (this.clientid.length()==0){
			setErrormessage("No client id");
			setStatuscode(HttpServletResponse.SC_FORBIDDEN);
			Dbutil.logger.warn("Missing client id");			
			}
		
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(String version) {
		try{this.version=Double.parseDouble(version);}catch(Exception e){}
		if (this.version!=0.1){
			setErrormessage("Invalid version");
			setStatuscode(HttpServletResponse.SC_BAD_REQUEST);
			Dbutil.logger.warn("Invalid version "+version);		
		}
	}

	public Formats getFormat() {
		return format;
	}

	public void setFormat(String format) {
		try{this.format=Formats.valueOf(format);}catch(Exception e){}
		if (this.format==null){
			setErrormessage("Invalid format");
			setStatuscode(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	public void setStatuscode(int statuscode) {
		this.statuscode = statuscode;
	}

	public int getStatuscode() {
		return statuscode;
	}

	public void setErrormessage(String errormessage) {
		this.errormessage = errormessage;
	}

	public String getErrormessage() {
		return errormessage;
	}
	
	
}

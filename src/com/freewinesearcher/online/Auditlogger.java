package com.freewinesearcher.online;

import java.io.*;
import java.net.*;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.common.Dbutil;

public class Auditlogger implements Runnable {
	public String userid="";
	public int shopid=0;
	public int partnerid=0;
	public String ip="";
	public boolean adenabled=false;
	public String partnername;
	String page="";
	String action="";
	String objecttype="";
	String objectid="0";
	String oldvalue="";
	String newvalue="";
	Double cpc=0.0;
	public String info="";
	String sessionid="";
	
	
	
	public Auditlogger(){
	}
	
	public Auditlogger(HttpServletRequest request){
		
		userid=request.getRemoteUser();
		sessionid=request.getSession().getId();
		shopid=Webroutines.getShopFromUserId(request.getRemoteUser());
		if (request.getSession().getAttribute("overrideshopid")!=null){
			shopid=(Integer)request.getSession().getAttribute("overrideshopid");
		}
		if (shopid==0){
			if (request.getParameter("authshop")!=null&&!request.getParameter("authshop").equals("")){
				String auth=request.getParameter("authshop");
				int i=0;
				try{i=Integer.parseInt(auth.split("-")[0]);}catch (Exception e){}
				if (i>0){
					int j=0;
					try{j=Integer.parseInt(auth.split("-")[1]);}catch (Exception e){}
					if (j>0){
						if (Math.abs(("Shop"+i).hashCode())==j){
							shopid=i;
						}
					}
					
				}
			}
		}
		partnerid=Webroutines.getPartnerFromUserId(request.getRemoteUser());
		if (request.getSession().getAttribute("overridepartnerid")!=null){
			partnerid=(Integer)request.getSession().getAttribute("overridepartnerid");
		}
		page=request.getRequestURI();
		action="Pageload";
		if (request.getHeader("HTTP_X_FORWARDED_FOR") == null) {
			ip = request.getRemoteAddr();
		} else {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (partnerid>0){
			int enabled=Dbutil.readIntValueFromDB("select * from partners where id="+partnerid+";","adenabled");
			if (enabled==1) adenabled=true;
		}
		if (partnerid>0) partnername=Dbutil.readValueFromDB("select * from partners where id="+partnerid+";", "name");
	}
	
	public static String getAdminLink(int shopid){
		return "authshop="+shopid+"-"+Math.abs(("Shop"+shopid).hashCode());
	}

	public void run() {
		String hostname="";
		try{hostname=InetAddress.getByName(ip).getHostName();} catch (Exception e){}
		String hostcountry=Webroutines.getCountryCodeFromIp(ip);
		java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime()); 
		//if((!Wijnzoeker.donotlogforhost.contains(";"+hostname+";")&&hostname!=null)||(Wijnzoeker.serverrole.equals("DEV"))){
		if (objectid.equals("")) objectid="0";
		Dbutil.executeQuery("Insert into auditlogging (date,shopid,partnerid,userid, ip, hostname, action,objecttype,objectid,oldvalue,newvalue,sessionid,info) " +
				"values (now(),'"+shopid+"','"+partnerid+"','"+userid+"','"+ip+"','"+hostname+"','"+action+"','"+objecttype+"','"+objectid+"','"+oldvalue+"','"+newvalue+"','"+sessionid+"','"+info+"');");
		//}
	}
	/* This action starts a separate thread that will log the action in the audit log
	 * 
	 */
	public void logaction(){
		new Thread(this).start();
	}
	
	public boolean isUserAuthorized(int shopid, HttpServletRequest request){
		boolean authorized=false;
		if (shopid==this.shopid||request.isUserInRole("admin")) authorized=true;
		return authorized;
	}
	
	public boolean isUserAuthorized(String object, HttpServletRequest request){
		// TO DO: create table in DB with fields object and role. Read it and see if user has a role that fits the object.
		boolean authorized=false;
		if (request.isUserInRole("admin")) authorized=true;
		return authorized;
	}
	
	
	

	public void setAction(String action) {
		this.action = action;
	}

	public void setCpc(Double cpc) {
		this.cpc = cpc;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setNewvalue(String newvalue) {
		this.newvalue = newvalue;
	}

	public void setObjectid(String objectid) {
		this.objectid = objectid;
	}

	public void setObjecttype(String objecttype) {
		this.objecttype = objecttype;
	}

	public void setOldvalue(String oldvalue) {
		this.oldvalue = oldvalue;
	}

	public String getOldvalue() {
		return oldvalue;
	}

	public String getNewvalue() {
		return newvalue;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public void setPartnerid(int partnerid) {
		this.partnerid = partnerid;
	}

	public int getPartnerid() {
		return this.partnerid;
	}

	public void setShopid(int shopid) {
		this.shopid = shopid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	
}

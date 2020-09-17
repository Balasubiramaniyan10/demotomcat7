package com.freewinesearcher.online;


import java.io.File;

import org.owasp.validator.html.*;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;

public class HtmlFilter {
	private static HtmlFilter myinstance;
	private Policy htmlpolicy;
	private Policy textpolicy;
	private AntiSamy as;
	// Note that the constructor is private
	
	private HtmlFilter() throws PolicyException {
		//Dbutil.logger.info(new File(Configuration.basedir+System.getProperty("file.separator")+"WEB-INF"+System.getProperty("file.separator")+"antisamy.xml").getAbsolutePath());
		as=new AntiSamy();
		htmlpolicy=Policy.getInstance(Configuration.basedir+System.getProperty("file.separator")+"WEB-INF"+System.getProperty("file.separator")+"html.xml");
		textpolicy=Policy.getInstance(Configuration.basedir+System.getProperty("file.separator")+"WEB-INF"+System.getProperty("file.separator")+"textonly.xml");
	}
	public static HtmlFilter getHtmlFilter() throws PolicyException {
		if (myinstance == null) {
			myinstance = new HtmlFilter();
		}
		return myinstance;
	}
	
	public String filterHtml(String input){
		CleanResults cr;
		try {
			return as.scan(input,htmlpolicy).getCleanHTML();
		} catch (Exception e) {
			Dbutil.logger.info("Could not validate user input",e);
		} 
		return "";
		
	}
	public String filterText(String input){
		try {
			return as.scan(input,textpolicy).getCleanHTML();
		} catch (Exception e) {
			Dbutil.logger.info("Could not validate user input",e);
		} 
		return "";
		
	}

}

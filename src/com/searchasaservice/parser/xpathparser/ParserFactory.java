package com.searchasaservice.parser.xpathparser;


import com.freewinesearcher.common.Dbutil;
import com.vinopedia.htmlunit.HtmlUnitParser;

public class ParserFactory {
	public static Parser getParser(String url,String postdata){
		try {
			return new HtmlUnitParser(url,postdata);
		} catch (Exception e) {
			Dbutil.logger.info("Problem: ",e);
		} 
		return null;
	}
}

package com.freewinesearcher.common;

import com.freewinesearcher.online.Webroutines;

public class Shorturl {
	public String url;

	public Shorturl(String url){
		this.url=url;
	}
	
	public static String shorten(String url) throws Exception{
		Shorturl s=new Shorturl(url);
		return s.getShortUrl();
	}

	public String getShortUrl() throws Exception{

		Webpage webpage=new Webpage();
		webpage.urlstring="http://api.bit.ly/shorten?version=2.0.1&longUrl="+Webroutines.URLEncode(url)+"&login=jhammink&apiKey=R_8d317509b477a025f363bc93ded3f00e";
		webpage.readPage();
		if (webpage.responsecode==200){
			String shorturl=Webroutines.getRegexPatternValue("shortUrl\": \"(http:[^\"]+)\"", webpage.html);
			if (!shorturl.equals("")) {
				return shorturl;
			}
			throw (new Exception("Illigal response from bit.ly"));
		}else{
			throw (new Exception("Could not create short url"));
		}
	}
}

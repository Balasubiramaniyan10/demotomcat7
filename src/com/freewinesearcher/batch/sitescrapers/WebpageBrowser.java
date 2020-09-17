package com.freewinesearcher.batch.sitescrapers;

import java.net.InetSocketAddress;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;

public class WebpageBrowser implements Browser{
	Webpage webpage;
	String ip;
	
	public WebpageBrowser() throws Exception{
		webpage=new Webpage();
		webpage.maxattempts=1;
		webpage.allowcookies=false;
		if (!isAnonymous()) throw new Exception("Not anonymous");
	}
	
	public WebpageBrowser(String proxy,java.net.Proxy.Type type) throws Exception{
		webpage=new Webpage();
		webpage.maxattempts=1;
		webpage.allowcookies=false;
		String ip=proxy.split(":")[0];
		String port=proxy.split(":")[1];
		if (ip.length()>0&&!ip.endsWith(".")&&port.length()>0){
			webpage.proxy=new java.net.Proxy(type, new InetSocketAddress(ip, Integer.parseInt(port)));
		} else {
			throw new Exception("Incorrect proxy");
		}
		if (!isAnonymous()) throw new Exception("Not anonymous");
	}

	
	
	@Override
	public void get(String url) {
		webpage.urlstring=url;
		webpage.readPage();
		
	}

	@Override
	public String getHtml() {
		return webpage.html;
	}

	@Override
	public void setHeader(String key, String value) throws Exception {
		webpage.headers=key+"="+value;
		
	}
	public void setUseragent(String agent) throws Exception{
		webpage.useragent=agent;
	}
	
	public static Browser getGoogleBotBrowser() throws Exception{
		Browser b=new WebpageBrowser("203.42.246.231:80",java.net.Proxy.Type.HTTP);
		b.setUseragent("Googlebot/2.1 (+http://www.googlebot.com/bot.html)");
		b.setHeader("X-Forwarded-For","66.249.64.25");
		b.setHeader("HTTP_X_FORWARDED_FOR","66.249.64.25");
		return b;
	}
	
	public static Browser getBrowser() throws Exception{
		Browser b=new WebpageBrowser("203.42.246.231:80",java.net.Proxy.Type.HTTP);
		b.setUseragent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:7.0.1) Gecko/20100101 Firefox/7.0.12011-10-16 20:21:05");
		//b.setHeader("X-Forwarded-For","66.249.64.25");
		//b.setHeader("HTTP_X_FORWARDED_FOR","66.249.64.25");
		return b;
	}
	
	public boolean isAnonymous() throws Exception{
		get("http://www.ericgiguere.com/tools/http-header-viewer.html");
		String html=getHtml();
		if (!html.contains("HTTP headers"))  throw new Exception("Not anonymous");
		if (html.contains(Configuration.devpcip))  throw new Exception("Not anonymous");
		get("https://test.vinopedia.com/myipis.jsp");
		html=getHtml();
		if (!html.contains("OK:"))  throw new Exception("Not anonymous");
		if (html.contains(Configuration.devpcip))  throw new Exception("Not anonymous");
		
		return true;
	}
	
	public String getIp(){
		return "";
	}
	
	public void close(){
		
	}
	
	public static void main(String [] args){
		try {
			Browser b=getGoogleBotBrowser();
			b.get("http://www.ericgiguere.com/tools/http-header-viewer.html");
			b.get("https://test.vinopedia.com/myipis.jsp");
			Dbutil.logger.info(b.getHtml());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package com.freewinesearcher.batch.sitescrapers;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;

public class Proxy {
	public String ip=null;
	public String port=null;
	
	public Proxy(){
		/*
		 * 89.188.136.116:80
		 * http://old.cool-proxy.net/index.php?action=anonymous-proxy-list&sort=working_average&sort-type=desc
		 * 
		 * http://tools.rosinstrument.com/cgi-bin/fp.pl/showlines?lines=200&sortor=3
		 * 
		 */
		
		
		Configuration.webpagetimeout=20000;
	Webpage webpage=new Webpage();
	webpage.urlstring="http://www.hidemyass.com/proxy-list/search-340364";
	webpage.postdata="ac=on&c%5B%5D=United+States&p=&pr%5B%5D=0&a%5B%5D=3&sp%5B%5D=3&ct%5B%5D=3&s=1&o=0&pp=2&sortBy=response_time";
	webpage.readPage();
	Pattern pattern;
	Matcher matcher;
	String result="";
	String html=webpage.html.replaceAll("\r", "").replaceAll("\n", "");
	//String html="\">2 minutes</span></td>         <td><span><span class=\"56\">41</span><span style=\"display:none\">81</span><div style=\"display:none\">150</div><span class=\"\" style=\"\">.</span>67<span style=\"display:none\">198</span><span></span>.<span>21</span><div style=\"display:none\">165</div>.<span style=\"display: inline\">19</span></span></td>	         <td>8080</td>                  <td rel=\"sd\"><span class=\"country\"><img src=\"http://static.hidemyass.com/flags/sd.png\" alt=\"flag\" /> Sudan</span></td>                  <td> <div class=\"";
	try{
		boolean ok=false;
		while (!ok){
		pattern=Pattern.compile("updatets(.*?)speedbar");
		matcher=pattern.matcher(html);
		int j=(int)(Math.random()*10);
		for (int i=0;i<j;i++) {
			boolean find=matcher.find();
			//Dbutil.logger.info(find);
		}
		if (matcher.find()){
			result=matcher.group(1);
		
		//result=html;
		String ip="";
		String port="";
		pattern=Pattern.compile("(?<!none\")>\\.?([0-9]+)\\.?<");
		matcher=pattern.matcher(result);
		if (matcher.find()) ip+=matcher.group(1)+".";
		if (matcher.find()) ip+=matcher.group(1)+".";
		if (matcher.find()) ip+=matcher.group(1)+".";
		if (matcher.find()) ip+=matcher.group(1);
		pattern=Pattern.compile("([0-9][0-9][0-9][0-9])");
		matcher=pattern.matcher(ip);
		if (matcher.find()){
			port=matcher.group(1);
		}
		pattern=Pattern.compile("<td>\\s*(\\d+)</td>");
		matcher=pattern.matcher(result);
		if (matcher.find()){
			port=matcher.group(1);
		}
		if (ip.length()>0&&!ip.endsWith(".")&&port.length()>0){
			webpage=new Webpage();
			webpage.urlstring="https://test.vinopedia.com/myipis.jsp";
			
			webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port)));
			webpage.maxattempts=1;
			long timenow=System.currentTimeMillis();
			webpage.readPage();
			if (webpage.html.contains("OK:")){
				if (System.currentTimeMillis()-timenow<10000){
				this.ip=ip;
				this.port=port;
				ok=true;
				Dbutil.logger.info("Proxy: "+ip+":"+port);
				} else {
					Dbutil.logger.info("Proxy too slow: "+ip+":"+port+" ("+(System.currentTimeMillis()-timenow) +"ms.)");
				}
			} else{
				Dbutil.logger.info("Proxy not working: "+ip+":"+port);
			}
		}
		}
		}
	} catch (Exception e){
		Dbutil.logger.error("Could not retrieve regex value from pattern. ",e);
	}
	
	
	}
	
	public static Proxy getProxy (int which){
		switch(which){
		case 0:{
			return new Proxy("updatets(.*?)speedbar","(?<!none\")>\\.?([0-9]+)\\.?<","<td>\\s*(\\d+)</td>","http://www.hidemyass.com/proxy-list/search-340364","ac=on&c%5B%5D=United+States&p=&pr%5B%5D=0&a%5B%5D=3&sp%5B%5D=3&ct%5B%5D=3&s=1&o=0&pp=2&sortBy=response_time");
			
		}
		case 1:{
			return new Proxy("(\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+)","(\\d+)",":(\\d+)","http://old.cool-proxy.net/index.php?action=anonymous-proxy-list&sort=response_time_average&sort-type=asc",""); 
		}
		}
		return null;
	}
	
	public Proxy(String linepattern, String ippattern, String portpattern, String url, String postdata){
		/*
		 * 89.188.136.116:80
		 * http://old.cool-proxy.net/index.php?action=anonymous-proxy-list&sort=working_average&sort-type=desc
		 * 
		 * http://tools.rosinstrument.com/cgi-bin/fp.pl/showlines?lines=200&sortor=3
		 * 
		 */
		
		
		Configuration.webpagetimeout=20000;
	Webpage webpage=new Webpage();
	webpage.urlstring=url;
	webpage.postdata=postdata;
	webpage.readPage();
	Pattern pattern;
	Matcher matcher;
	String result="";
	String html=webpage.html.replaceAll("\r", "").replaceAll("\n", "");
	//String html="\">2 minutes</span></td>         <td><span><span class=\"56\">41</span><span style=\"display:none\">81</span><div style=\"display:none\">150</div><span class=\"\" style=\"\">.</span>67<span style=\"display:none\">198</span><span></span>.<span>21</span><div style=\"display:none\">165</div>.<span style=\"display: inline\">19</span></span></td>	         <td>8080</td>                  <td rel=\"sd\"><span class=\"country\"><img src=\"http://static.hidemyass.com/flags/sd.png\" alt=\"flag\" /> Sudan</span></td>                  <td> <div class=\"";
	try{
		boolean ok=false;
		while (!ok){
		pattern=Pattern.compile(linepattern);
		matcher=pattern.matcher(html);
		int j=(int)(Math.random()*10);
		for (int i=0;i<j;i++) {
			boolean find=matcher.find();
			//Dbutil.logger.info(find);
		}
		if (matcher.find()){
			result=matcher.group(1);
		
		//result=html;
		String ip="";
		String port="";
		pattern=Pattern.compile(ippattern);
		matcher=pattern.matcher(result);
		if (matcher.find()) ip+=matcher.group(1)+".";
		if (matcher.find()) ip+=matcher.group(1)+".";
		if (matcher.find()) ip+=matcher.group(1)+".";
		if (matcher.find()) ip+=matcher.group(1);
		pattern=Pattern.compile(portpattern);
		matcher=pattern.matcher(result);
		if (matcher.find()){
			port=matcher.group(1);
		}
		if (ip.length()>0&&!ip.endsWith(".")&&port.length()>0){
			webpage=new Webpage();
			webpage.urlstring="https://test.vinopedia.com/myipis.jsp";
			
			webpage.proxy=new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port)));
			webpage.maxattempts=1;
			long timenow=System.currentTimeMillis();
			webpage.readPage();
			if (webpage.html.contains("OK:")){
				if (System.currentTimeMillis()-timenow<10000){
				this.ip=ip;
				this.port=port;
				ok=true;
				Dbutil.logger.info("Proxy: "+ip+":"+port+" speed "+(System.currentTimeMillis()-timenow)+"ms.");
				} else {
					Dbutil.logger.info("Proxy too slow: "+ip+":"+port+" ("+(System.currentTimeMillis()-timenow) +"ms.)");
				}
			} else{
				Dbutil.logger.info("Proxy not working: "+ip+":"+port);
			}
		}
		}
		}
	} catch (Exception e){
		Dbutil.logger.error("Could not retrieve regex value from pattern. ",e);
	}
	
	
	}
	
	public Proxy(String proxy,java.net.Proxy.Type type) throws Exception{
		try{
			Webpage webpage=new Webpage();
			ip=proxy.split(":")[0];
			port=proxy.split(":")[1];
			if (ip.length()>0&&!ip.endsWith(".")&&port.length()>0){
				webpage=new Webpage();
				webpage.urlstring="https://test.vinopedia.com/myipis.jsp";
				
				webpage.proxy=new java.net.Proxy(type, new InetSocketAddress(ip, Integer.parseInt(port)));
				webpage.maxattempts=1;
				long timenow=System.currentTimeMillis();
				webpage.readPage();
				if (webpage.html.contains("OK:")){
					if (System.currentTimeMillis()-timenow<10000){
					this.ip=ip;
					this.port=port;
					Dbutil.logger.info("Proxy: "+ip+":"+port);
					} else {
						Dbutil.logger.info("Proxy too slow: "+ip+":"+port+" ("+(System.currentTimeMillis()-timenow) +"ms.)");
					}
				} else{
					Dbutil.logger.info("Proxy not working: "+ip+":"+port);
				}
			}
			
		} catch (Exception e){
			Dbutil.logger.error("Could not retrieve regex value from pattern. ",e);
		}
		
		
		}
	

}

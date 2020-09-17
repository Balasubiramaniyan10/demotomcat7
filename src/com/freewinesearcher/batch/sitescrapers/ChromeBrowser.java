package com.freewinesearcher.batch.sitescrapers;

import java.util.Arrays;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;


public class ChromeBrowser implements Browser {
	WebDriver driver;
	Proxy proxy=null;
	
	
	public static ChromeBrowser getInstance() throws Exception{
		//Proxy proxy=new Proxy("203.42.246.231:80",java.net.Proxy.Type.HTTP);//super, veel gebruikt
		// Toppers:
		//122.72.0.105:80
		//41.190.16.17:8080
		//41.158.128.190:80
		Proxy proxy=Proxy.getProxy(1);
		//Proxy proxy=new Proxy("41.158.128.190:80",java.net.Proxy.Type.HTTP);//
		//Proxy proxy=new Proxy("164.77.196.75:80",java.net.Proxy.Type.HTTP);
		
		if (proxy.ip!=null){
			try {
				ChromeBrowser b= new ChromeBrowser(proxy.ip+":"+proxy.port);
				b.proxy=proxy;
				
				return b;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		throw new Exception("Could not get proxy");
	}
	
	public String getIp(){
		return proxy.ip;
	}
	public ChromeBrowser() throws Exception{
		//System.setProperty("webdriver.chrome.bin", "C:\\Workspace\\Selenium\\bin\\chromedriver.exe");
		System.setProperty("webdriver.chrome.driver", "C:\\Workspace\\Selenium\\bin\\chromedriver.exe");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		  //capabilities.setCapability("chrome.switches", Arrays.asList("--proxy-server=https://127.0.0.1:8118"));
		  ChromeOptions options = new ChromeOptions();
		  //options.addArguments("--start-maximized"); //Chrome starts always maximized
		  options.addArguments("--no-referrers"); // no HTTP referer will be send
		  //options.addArguments("--proxy-server=https://127.0.0.1:8118"); // use Tor
		  //options.addArguments("--load-extension=C:\\Users\\Jasper\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Extensions\\bmagokdooijbeehmkpknfglimnifench\\1.4.0.11967_0");
		  options.addArguments("--browserSessionReuse=true"); // use new sessions
			
		  options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 6.0; rv:11.0) Gecko/20100101 Firefox/11.0");
		  driver = new ChromeDriver(options); //starts Chrome
		  //ip();
		  //driver = new ChromeDriver(capabilities);
		  
	}
	
	public ChromeBrowser(String proxy) throws Exception{
		//System.setProperty("webdriver.chrome.bin", "C:\\Workspace\\Selenium\\bin\\chromedriver.exe");
		System.setProperty("webdriver.chrome.driver", "C:\\Workspace\\Selenium\\bin\\chromedriver.exe");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		  //capabilities.setCapability("chrome.switches", Arrays.asList("--proxy-server=https://127.0.0.1:8118"));
		  ChromeOptions options = new ChromeOptions();
		  //options.addArguments("--start-maximized"); //Chrome starts always maximized
		  options.addArguments("--no-referrers"); // no HTTP referer will be send
		  options.addArguments("--proxy-server="+proxy); // use proxy
		  //options.addArguments("--load-extension=C:\\Users\\Jasper\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Extensions\\bmagokdooijbeehmkpknfglimnifench\\1.4.0.11967_0");
		  options.addArguments("--browserSessionReuse=true"); // use new sessions
			
		  options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 6.0; rv:11.0) Gecko/20100101 Firefox/11.0");
		  driver = new ChromeDriver(options); //starts Chrome
		  ip();
		  
		  //driver = new ChromeDriver(capabilities);
		  
	}
	
	public String ip() throws Exception{
		driver.get("https://test.vinopedia.com/myipis.jsp");
		String ip=Webroutines.getRegexPatternValue("OK:(\\d+\\.\\d+\\.\\d+\\.\\d+)", driver.getPageSource());
		if ("".equals(ip)) throw new Exception("Not Anonymous!");
		return ip;
	}
	
	public boolean isAnonymous() throws Exception{
		get("http://www.ericgiguere.com/tools/http-header-viewer.html");
		String html=getHtml();
		if (!html.contains("HTTP headers")) throw new Exception("Not anonymous");
		if (html.contains(Configuration.devpcip)) throw new Exception("Not anonymous");
		get("https://test.vinopedia.com/myipis.jsp");
		html=getHtml();
		if (!html.contains("OK:")) throw new Exception("Not anonymous");
		if (html.contains(Configuration.devpcip)) throw new Exception("Not anonymous");
		return true;
	}
	
	public void close(){
		driver.close();
	}
	
	public static void main(String[] args){
		try {
			ChromeBrowser browser = ChromeBrowser.getInstance();
			
			//browser.driver.get("http://whatsmyuseragent.com/");
			browser.driver
					.get("http://www.ericgiguere.com/tools/http-header-viewer.html");
			System.out.println(browser.driver.getPageSource());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			browser.driver.get("https://test.vinopedia.com/myipis.jsp");
			System.out.println(browser.driver.getPageSource());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			browser.driver.close();
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		System.exit(0);
	}
	
	public void get(String url){
		driver.get(url);
	}
	
	
	public String getHtml(){
		return driver.getPageSource();
	}
	
	public void setHeader(String key, String value) throws Exception{
		throw new Exception("Not supported");
	}
	public void setUseragent(String agent) throws Exception{
		throw new Exception("Not supported");
	}
	
}

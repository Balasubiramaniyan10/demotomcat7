package com.freewinesearcher.batch;

import java.io.*;
import java.util.ArrayList;


import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;


public class FileSpider {
	ArrayList<String> url=new ArrayList<String>();
	String filter="";
	
	
	public FileSpider(int shopid){
		String Postdata="";
		String Regexescaped="Leeg";
		String Headerregexescaped="";
		String Order="Leeg";
		String tablescraper="";
		String maindir="C:\\cachedpages\\"+shopid+"\\";
		Dbutil.executeQuery("Delete from scrapelist " +
				"WHERE URLType like 'File'" +
				"AND Shopid = '"+shopid+"';");
		
		addFiles(new File(maindir));
		String encoding=Dbutil.readValueFromDB("Select * from shops where id="+shopid+";", "encoding");
		Spider spider=new Spider(shopid+"",encoding,"",2);
		ArrayList<ArrayList<String>> Urllist=spider.getScrapeList("");
		for (int i = 0;i<Urllist.size();i++){
			if (Urllist.get(i).get(2).equals("Master")&&tablescraper.equals("")){
				Postdata=Urllist.get(i).get(1);
				Order=Urllist.get(i).get(5);
				tablescraper=Urllist.get(i).get(6);
			}
		}
		if (!tablescraper.equals("")){
			spider.addUrl(url, Regexescaped, Headerregexescaped, tablescraper, Order, "0","File",Postdata,maindir);
			Wijnzoeker wijnzoeker=new Wijnzoeker();
			Wijnzoeker.shoptodebug=shopid;
			Wijnzoeker.auto="";
			Wijnzoeker.type=2; //rating 
			wijnzoeker.updateSites();
			Dbutil.executeQuery("Delete from scrapelist " +
					"WHERE URLType like 'File'" +
					"AND Shopid = '"+shopid+"';");
		}
				
		
	}
	
	public void addFiles(File dir){
		for (File file:dir.listFiles(new NameFilter())){
			if (file.isDirectory()) addFiles(file);
			if (file.isFile()) {
				url.add("file://"+file.getAbsolutePath());
				url.add("file://"+file.getAbsolutePath());
				url.add(("file://"+file.getAbsolutePath()).hashCode()+"");
			}
		}
	}
	class NameFilter implements FilenameFilter {
	    public boolean accept(File dir, String name) {
	        if (name.contains("0000-00-00")) return false;
	    	return true;
	    }
	}
}

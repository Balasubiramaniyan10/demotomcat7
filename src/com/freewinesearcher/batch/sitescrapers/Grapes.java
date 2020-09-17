package com.freewinesearcher.batch.sitescrapers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;

public class Grapes {
	public static void main(String[] args){
		try {
			StringBuffer fileData = new StringBuffer(1000);
	        BufferedReader reader = new BufferedReader(
	                new FileReader("C:\\temp\\wgg.html"));
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	            fileData.append(buf, 0, numRead);
	        }
	        reader.close();
	        String page= fileData.toString().replaceAll("\r\n", "").replaceAll("\n", "");
	        String aliasregex="<a href=\"#(.*?)\">([^<]+)</a>";
	        String descregex="<dt><a name=\"[1]\">([^<]+)</a>:[^<]*<dd>(.*?)<dt>";
	        Pattern p;
	        Matcher m;
	        HashMap<String,String> grapes=new HashMap<String,String>();
	        HashMap<String,String> officialname=new HashMap<String,String>();
	        HashMap<String,String> descriptions=new HashMap<String,String>();
	        p=Pattern.compile(aliasregex);
	        m=p.matcher(page);
	        while (m.find()){
	        	grapes.put(Spider.unescape(m.group(2)), Spider.unescape(m.group(1)));
	        }
	        FileWriter fstream = new FileWriter("C:\\temp\\grapes.txt");
	        BufferedWriter out = new BufferedWriter(fstream);
	        int i=0;
        	for (String grapename:grapes.keySet()){
        		String grape=grapes.get(grapename);
	        	p=Pattern.compile(descregex.replace("[1]", grape));
	        	m=p.matcher(page);
	        	if (m.find()){
		        	officialname.put(grape, Spider.unescape(m.group(1)));
		        	String desc=m.group(2);
		        	while (!Webroutines.getRegexPatternValue("<a href=\"#[^\"]+\">([^<]+)</a>", desc).equals("")){
		        		String g=Webroutines.getRegexPatternValue("<a href=\"#[^\"]+\">([^<]+)</a>", desc);
		        		String w=Webroutines.getRegexPatternValue("<a href=\"#([^\"]+)\">([^<]+)</a>", desc);
		        		desc=desc.replaceAll("<a href=\"#"+w+"\">([^<]+)</a>", g);
		        	}
		        	desc=desc.replaceAll("</?b>", "");
		        	desc=desc.replaceAll("</?p>", "");
		        	descriptions.put(grape, desc);
		        	i++;
		        	System.out.println(i+"\t"+grapename+"\t"+Spider.unescape(m.group(1))+"\t"+desc);
		        	out.write(grapename+"\t"+Spider.unescape(m.group(1))+"\t"+desc+"\n");
		        	int result=Dbutil.executeQuery("update grapes set pedia='"+Spider.SQLEscape(Spider.unescape(desc))+"', aka='"+Spider.SQLEscape(Webroutines.formatCapitals(Spider.unescape(m.group(1))))+"' where grapename like '"+Spider.SQLEscape(grapename)+"';");
		        	
		        }
		        
	        }
        	//Close the output stream
    	    out.close();
    	    
	        
	        
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

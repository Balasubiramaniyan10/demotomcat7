package com.freewinesearcher.batch.temp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;

/**
 * @author Jasper
 *
 */
public class Producer {
	Wijnzoeker wijnzoeker=new Wijnzoeker();
	String name="";
	String address="";
	String phone="";
	String web="";
	String email="";
	
	

	public Producer(String name, int countrycode){
		String phoneregex="(\\+"+countrycode+"[\\d -().]{7,25})";
		String phonefilter="[\\D.]";
		String addressregex="^(.*\\d\\d\\d\\d\\d.*)";
		String emailregex="([\\S.-]+@[\\S.-]+)";
		String webregex="(www\\..*?[ \"'])";
		
		this.name=name;
		Webpage webpage=new Webpage();
		webpage.urlstring="http://www.google.com/search?num=100&hl=en&rlz=1B2GGGL_nlNL204NL204&ie=ISO-8859-1&q="+name.replace(" ", "+")+"+%2B"+countrycode+"&btnG=Search&aq=t";
		webpage.readPage();
		String page=webpage.html;
		page=page.replaceAll("<[^<>]+>","");
		SortedMap<String,Integer> phone=results(phoneregex,phonefilter,page);
		Dbutil.logger.info(phone);
		webpage.urlstring="http://www.google.com/search?num=100&hl=en&rlz=1B2GGGL_nlNL204NL204&ie=ISO-8859-1&q="+name.replace(" ", "+")+"+email&btnG=Search&aq=t";
		webpage.readPage();
		page=webpage.html;
		page=page.replaceAll("<[^<>]+>","");
		SortedMap<String,Integer> email=results(emailregex,"",page);
		Dbutil.logger.info(email);
	}
	

	
	/**
	 * Used to validate an address from the producers own web site
	 * TO BE FINISHED!!!
	 * 
	 * @param name
	 * @param countrycode
	 * @param webpage
	 * @param postalcode
	 */
	public Producer(String name, int countrycode, String webpage, String postalcode){
		String phoneregex="(\\+"+countrycode+"[\\d -().]{7,25})";
		String phonefilter="[\\D.]";
		String addressregex="^(.*\\d\\d\\d\\d\\d.*)";
		String emailregex="([\\S.-]+@[\\S.-]+)";
		String webregex="(www\\..*?[ \"'])";
		
		this.name=name;
		Webpage webpage2=new Webpage();
		webpage2.urlstring="http://www.google.com/search?num=100&hl=en&rlz=1B2GGGL_nlNL204NL204&ie=ISO-8859-1&q="+name.replace(" ", "+")+"+%2B"+countrycode+"&btnG=Search&aq=t";
		webpage2.readPage();
		String page=webpage2.html;
		page=page.replaceAll("<[^<>]+>","");
		SortedMap<String,Integer> phone=results(phoneregex,phonefilter,page);
		Dbutil.logger.info(phone);
		webpage2.urlstring="http://www.google.com/search?num=100&hl=en&rlz=1B2GGGL_nlNL204NL204&ie=ISO-8859-1&q="+name.replace(" ", "+")+"+email&btnG=Search&aq=t";
		webpage2.readPage();
		page=webpage2.html;
		page=page.replaceAll("<[^<>]+>","");
		SortedMap<String,Integer> email=results(emailregex,"",page);
		Dbutil.logger.info(email);
	}
	
private SortedMap<String,Integer> results(String regex, String filter,String page){
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		HashMap<String,Integer> sortedmap=new HashMap<String,Integer>();
		Pattern pattern=Pattern.compile(regex);
		Matcher matcher=pattern.matcher(page);
		while (matcher.find()){
			String value=matcher.group(1).replaceAll(filter, "");
			if (map.containsKey(value)){
				map.put(value,(Integer)((Integer)map.get(value)+1));
			} else {
				map.put(value,1);
			}
		}
		byValueComparator bvc =
			  new byValueComparator(map);
		TreeMap sorted_map =
			  new TreeMap(bvc);
		sorted_map.putAll(map);
		return sorted_map;
	}
	public class byValueComparator implements Comparator {
		  Map base_map;
			
		  public byValueComparator(Map base_map) {
		    this.base_map = base_map;
		  }
			
		  public int compare(Object arg0, Object arg1) {
		    if(!base_map.containsKey(arg0) || !base_map.containsKey(arg1)) {
		      return 0;
		    }
			
		    if((Integer)base_map.get(arg0) < (Integer)base_map.get(arg1)) {
		      return 1;
		    } else if((Integer)base_map.get(arg0) == (Integer)base_map.get(arg1)) {
		      return 0;
		    } else {
		      return -1;
		    }
		  }
		}	
	public static void main(String[] args){
		new Producer("Azienda Agricola Giovanni Puiatti",39);
	}
	
}

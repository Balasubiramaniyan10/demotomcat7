package com.freewinesearcher.online;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import com.freewinesearcher.common.Knownwines;


public class TastingNote {

	private String winename;
	private int vintage;
	private String author;
	private String sourcesite;
	private String sourcesitelink;
	private String link;
	private String tn;
	private float score;

	public TastingNote(Element e){
		//		for each element get values
		winename = getTextValue(e,"Title");
		vintage = getIntValue(e,"Vintage");
		author=  getTextValue(e,"Reviewer");
		sourcesite=getTextValue(e,"Source","SiteName");
		sourcesitelink=getTextValue(e,"Source","Link");
		link=  getTextValue(e,"Link");
		score= getFloatValue(e,"Score");
		tn=getTextValue(e,"Summary");
		
	}

	public boolean matches(int knownwineid){
		return Knownwines.doesWineMatch(winename, knownwineid);
	}
	
	public boolean vintageMatches(String vintagesearch){
		return Knownwines.doesVintageMatch(winename, vintagesearch);
	}
	
	public String toString(){
		String html="";
		html+="<a href='"+link+"' target='_blank'>"+winename+"</a>";
		html+=(score>0?(". Score:"+score+" / 5."):"");
		html+=("".equals(author)?"":(" Author: "+author));
		html+=("".equals(sourcesite)?"":" on <a href='"+sourcesitelink+"' target='_blank'>"+sourcesite+"</a>");
		html+="<br/>";
		html+=cleanup(tn)+"<br/>";
		return html;
	}
	
	private String cleanup(String input){
		input=input.replaceAll("<[^>]+>", "");
		input=input.replaceAll("\\[FIND.*", "");
		return input;
	}
	
	private String getTextValue(Element ele, String tagName) {
		
		String textVal="";
		try {
			NodeList nl = ele.getElementsByTagName(tagName);
			if (nl != null && nl.getLength() > 0) {
				Element el = (Element) nl.item(0);
				textVal = el.getFirstChild().getNodeValue();
			}
		} catch (Exception e) {
			
		}		
		return textVal;
	}

	private String getTextValue(Element ele, String subName, String tagName) {
		String textVal = "";
		try {
			NodeList nl = ele.getElementsByTagName(subName);
			if (nl != null && nl.getLength() > 0) {
				Element el = (Element) nl.item(0);
				NodeList nl2 = el.getElementsByTagName(tagName);
				if (nl2 != null && nl2.getLength() > 0) {

					Element el2 = (Element) nl2.item(0);
					textVal = el2.getFirstChild().getNodeValue();
				}
			}
		} catch (Exception e) {
			
		}		
		return textVal;
	}


	/**
	 * Calls getTextValue and returns a int value
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private int getIntValue(Element ele, String tagName) {
		try {
			return Integer.parseInt(getTextValue(ele, tagName));
		} catch (Exception e) {
		}	
		return 0;
	}

	private float getFloatValue(Element ele, String tagName) {
		try {
			return Float.parseFloat(getTextValue(ele, tagName));
		} catch (Exception e) {
			
		}	
		return 0;
	}


	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public String getSourcesite() {
		return sourcesite;
	}
	public void setSourcesite(String sourcesite) {
		this.sourcesite = sourcesite;
	}
	public String getSourcesitelink() {
		return sourcesitelink;
	}
	public void setSourcesitelink(String sourcesitelink) {
		this.sourcesitelink = sourcesitelink;
	}
	public int getVintage() {
		return vintage;
	}
	public void setVintage(int vintage) {
		this.vintage = vintage;
	}
	public String getWinename() {
		return winename;
	}
	public void setWinename(String winename) {
		this.winename = winename;
	}

	public String getTn() {
		return tn;
	}

	public void setTn(String tn) {
		this.tn = tn;
	}


}

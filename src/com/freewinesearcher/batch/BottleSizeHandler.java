package com.freewinesearcher.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.parser.xpathparser.ContentHandler;

public class BottleSizeHandler implements ContentHandler {

	private static final long serialVersionUID = 1L;

	public Object getContent(Node n) {
		if (n==null) return null;
		String input=n.getTextContent();
		float size=(float)0;
		String match;
		for (int j=0; j<Configuration.sizeregex.length;j++ ){
			if (size==0){ // With size, take the first hit
				match=Webroutines.getRegexPatternValue("("+Configuration.sizeregex[j]+")",input,Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
				if (!"".equals(match)){
					size=Configuration.size[j];
				}
			}

		}
		return size;
	}

	public String init() {
		return "";
	}

	public Set<Node> recognizeContent(Set<Node> set) {
		float size=(float)0;
		Set<Node> list=new HashSet<Node>();
		String allsizes="";
		for (int j=0; j<Configuration.limitedsizeregex.length;j++ ){
			allsizes+="|"+Configuration.limitedsizeregex[j];
		}
		allsizes=allsizes.substring(1);
		String textcontent;
		for (Node n:set){
			textcontent=n.getTextContent();
			size=0;
			if (!"".equals(Webroutines.getRegexPatternValue("("+allsizes+")",textcontent,Pattern.CASE_INSENSITIVE+Pattern.MULTILINE))){
				for (int j=0; j<Configuration.limitedsizeregex.length;j++ ){
					if (size==0){ // With size, take the first hit
						if (!"".equals(Webroutines.getRegexPatternValue("("+Configuration.limitedsizeregex[j]+")",textcontent,Pattern.CASE_INSENSITIVE+Pattern.MULTILINE))){
							size=Configuration.limitedsize[j];
							j=99999;
						}
					}
				}
			}
			if (size>0) list.add(n);
		}
		return list;
	}

	public String setNegativeConfig(String input) {
		return "";
	}

	public String setPositiveConfig(String input) {
		return "";
	}

}

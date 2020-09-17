package com.searchasaservice.parser.xpathparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webroutines;

public class TextHandler implements ContentHandler {

	private static final long serialVersionUID = 1L;
	private String regex="";
	private String excluderegex="";
	
	public String getContent(Node n) {
		if (n==null) return null;
		String input=n.getTextContent();
		if (input==null) return null;
		String value=null;
		if (!regex.contains("(")||!regex.contains(")")) {
			value=input;
		} else { 
			if (input.equals("")){
				return null;
			} else {
				value=Webroutines.getRegexPatternValue(regex, input);
			}
		}
		if (value!=null&&!value.equals("")) {
			if ("".equals(excluderegex)||"".equals(Webroutines.getRegexPatternValue("("+excluderegex+")", input))){
				return value;
			} 
		}
		return null;
	}

	public Set<Node> recognizeContent(Set<Node> set) {
		HashSet<Node> result=new HashSet<Node>();
		for (Node n:set){
			if(getContent(n)!=null) result.add(n);
		}
		return result;
	}

	public String setNegativeConfig(String input) {
		String message="";
		try {
			Pattern.compile(input);
			excluderegex=input;
			message="";
		} catch (Exception e) {
			message+="Problem while setting negative configuration: Could not compile input "+input+". Input must be a valid regular expression.";
			Dbutil.logger.info(message,e);
		}
		return message;
		
	}

	public String setPositiveConfig(String input) {
		String message="Problem while setting positive configuration: Unknown error. ";
		if (input==null){
			message="Problem while setting positive configuration: Input is null. ";
		} else {
			if (!"".equals(input)&&!input.contains("(")||!input.contains(")")) {
				message="Problem while setting positive configuration: input does not contain grouping parantheses. The regex to be caught must be in group 1. ";
			}
			try {
				Pattern.compile(input);
				regex=input;
				message="";
			} catch (Exception e) {
				message="Problem while setting positive configuration: Could not compile input "+input+". Input must be a valid regular expression. ";
				Dbutil.logger.info(message,e);
			}
		}
		return message;
	}

	public String init() {
		String message="";
		if (!"".equals(setPositiveConfig(regex))) message=setPositiveConfig(regex);
		if (!"".equals(setNegativeConfig(excluderegex))) message=setPositiveConfig(excluderegex);
		return message;
	}

}

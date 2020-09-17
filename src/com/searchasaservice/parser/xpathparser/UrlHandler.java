package com.searchasaservice.parser.xpathparser;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;


public class UrlHandler implements ContentHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Object getContent(Node n) {
		if (n.getAttributes()==null||n.getAttributes().getNamedItem("href")==null) return null;
		return n.getAttributes().getNamedItem("href").getTextContent();
	}

	public String init() {
		return "";
	}

	public Set<Node> recognizeContent(Set<Node> set) {
		HashSet<Node> result=new HashSet<Node>();
		for (Node n:set){
			if(getContent(n)!=null) result.add(n);
		}
		return result;
	}

	
	public String setNegativeConfig(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	public String setPositiveConfig(String input) {
		// TODO Auto-generated method stub
		return null;
	}

}

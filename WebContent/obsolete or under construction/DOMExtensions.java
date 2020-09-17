package com.searchasaservice.parser;
import org.apache.xpath.NodeSet;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
public class DOMExtensions {
	Node n;
	
	public interface myNode extends  org.w3c.dom.Node{
		
		public NodeSet getNodesWithTextNodes();
	}
	
}
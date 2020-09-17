package com.searchasaservice.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lobobrowser.html.*;
import org.lobobrowser.html.gui.*;
import org.lobobrowser.html.parser.*;
import org.lobobrowser.html.test.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.HtmlParser;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.searchasaservice.parser.xpathparser.Analyzer;


public class LoboParser implements ErrorHandler {

	public Document document=null;

	public LoboParser() {
	}


	public LoboParser(String url) {
		if (true){
			InputStream in=null;
			try {

				Webpage webpage=new Webpage();
				webpage.encoding=Spider.getHtmlEncoding(url);
				webpage.urlstring=url;
				in = webpage.getAsInputStream();
				// Disable most Cobra logging.
				Logger.getLogger("org.lobobrowser").setLevel(Level.WARNING);
				UserAgentContext uacontext = new SimpleUserAgentContext();
				// In this case we will use a standard XML document
				// as opposed to Cobra's HTML DOM implementation.
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Reader reader = new InputStreamReader(in, "ISO-8859-1");
				document = builder.newDocument();
				Dbutil.logger.info("Start parsing Lobo with encoding "+webpage.encoding);
				// Here is where we use Cobra's HTML parser.            
				HtmlParser parser = new HtmlParser(uacontext, document);
				parser.parse(reader);
				
				// Now we use XPath to locate "a" elements that are
				// descendents of any "html" element.
				//filter();
				//Dbutil.logger.info(Analyzer.getNodeAsXML(document));
			} catch (Exception e) {
				Dbutil.logger.error("Could not parse html with Lobo.",e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			document=new LoboBrowser(url).document;
			Dbutil.logger.info(document.getClass());
		}
	}


	public void warning (SAXParseException e)
	throws SAXException
	{
		System.out.println("Warning:"+e.getMessage());
	}

	public void error (SAXParseException e)
	throws SAXException
	{
		throw new SAXException(e.getMessage());
	}


	public void fatalError (SAXParseException e)
	throws SAXException
	{
		System.out.println("Fatal error");
		throw new SAXException(e.getMessage());
	}


	public static LoboParser parsePage(String page){
		LoboParser lp=new LoboParser();
		try {DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		UserAgentContext uacontext = new SimpleUserAgentContext();

		StringReader sr=new StringReader(page);
		lp.document= builder.newDocument();
		// Here is where we use Cobra's HTML parser.            
		VpHTMLParser parser = new VpHTMLParser(uacontext, lp.document);

		parser.parse(sr);
		lp.filter();
		} catch (Exception e) {
			Dbutil.logger.error("Could not Lobo parse page ",e);
		}
		return lp;

	}

	public void filter() throws XPathExpressionException{
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xpath.evaluate("//*[(@src or @rel='stylesheet')]", document, XPathConstants.NODESET);
		int length = nodeList.getLength();
		for(int i = 0; i < length; i++) {
			Element element = (Element) nodeList.item(i);
			element.removeAttribute("src");
			element.removeAttribute("href");
			//System.out.println("## Anchor # " + i + ": " + element.getTextContent());
		}
		nodeList = (NodeList) xpath.evaluate("//script", document, XPathConstants.NODESET);
		length = nodeList.getLength();
		for(int i = 0; i < length; i++) {
			Element element = (Element) nodeList.item(i);
			element.getParentNode().removeChild(element); 
			//System.out.println("## Anchor # " + i + ": " + element.getTextContent());
		}
		nodeList = (NodeList) xpath.evaluate("//base", document, XPathConstants.NODESET);
		length = nodeList.getLength();
		for(int i = 0; i < length; i++) {
			Element element = (Element) nodeList.item(i);
			element.getParentNode().removeChild(element); 
			//System.out.println("## Anchor # " + i + ": " + element.getTextContent());
		}
	}

}

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


public class LoboParserTest implements ErrorHandler {

	public Document document=null;

	public LoboParserTest() {
	}


	public LoboParserTest(String url) {
		if (false){
		InputStream in=null;
		try {Logger.getLogger("org.lobobrowser").setLevel(Level.WARNING);
		UserAgentContext uacontext = new SimpleUserAgentContext();
		// In this case we will use a standard XML document
		// as opposed to Cobra's HTML DOM implementation.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);

		DocumentBuilder builder = factory.newDocumentBuilder();
		Webpage webpage=new Webpage();
		webpage.encoding=Spider.getHtmlEncoding(url);
		webpage.urlstring=url;

		webpage.readPage();
		String page=webpage.html;
		page=page.replaceAll("(^|\\s)xmlns(:[^=\"]+)?=\"[^\"]*\""," ");
		page=page.replaceAll("(?<=</?)\\w+:","");
		page=page.replaceAll("<!DOCTYPE [^>]*>","");

		Dbutil.logger.info("Start parsing Lobo with encoding "+webpage.encoding);


		if (true){
			in = webpage.getAsInputStream();

			// A Reader should be created with the correct charset,
			// which may be obtained from the Content-Type header
			// of an HTTP response.
			Reader reader=new StringReader(page);
			// InputSourceImpl constructor with URI recommended
			// so the renderer can resolve page component URLs.
			InputSource is = new InputSourceImpl(reader, url);
			HtmlPanel htmlPanel = new HtmlPanel();
			
			UserAgentContext ucontext = new SimpleUserAgentContext();
			HtmlRendererContext rendererContext = 
				new SimpleHtmlRendererContext(htmlPanel, ucontext);

			// Set a preferred width for the HtmlPanel,
			// which will allow getPreferredSize() to
			// be calculated according to block content.
			// We do this here to illustrate the 
			// feature, but is generally not
			// recommended for performance reasons.
			htmlPanel.setPreferredWidth(800);

			builder = 
				new DocumentBuilderImpl(
						rendererContext.getUserAgentContext(), 
						rendererContext);
			document = builder.parse(is);
			String parsed=(Analyzer.getNodeAsXML(document));
			document = parsePage(parsed).document;
		} else {
			UserAgentContext ucontext = new SimpleUserAgentContext();
			Reader reader = new InputStreamReader(in, webpage.encoding);

			// Here is where we use Cobra's HTML parser.  
			builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
			document = builder.newDocument();
			HtmlParser parser = new HtmlParser(ucontext, document,new org.xml.sax.helpers.DefaultHandler(),"","");
			parser.parse(reader);
			Dbutil.logger.info("Finished parsing Lobo");
		}
		filter();
		} catch (Exception e) {
			Dbutil.logger.error("Could not parse html with Lobo.",e);
		} finally {
			try {
				if (in!=null) in.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		document=new LoboBrowser("https://test.vinopedia.com/rayastestpage.html").document;

	}


	public void warning (SAXParseException e)
	throws SAXException
	{
		System.out.println("Warning:"+e.getMessage());
	}

	public void error (SAXParseException e)
	throws SAXException
	{
		System.out.println("Error");
		//throw new SAXException(e.getMessage());
	}


	public void fatalError (SAXParseException e)
	throws SAXException
	{
		System.out.println("Fatal error");
		//throw new SAXException(e.getMessage());
	}


	public static LoboParser parsePage(String page){
		LoboParser lp=new LoboParser();
		try {DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		UserAgentContext uacontext = new SimpleUserAgentContext();

		StringReader sr=new StringReader(page);
		lp.document= builder.newDocument();
		// Here is where we use Cobra's HTML parser.            
		HtmlParser parser = new HtmlParser(uacontext, lp.document);

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
	}

	public static void main(String[] args){
		LoboParserTest lpt=new LoboParserTest("https://test.vinopedia.com/rayastestpage.html");

	}

}

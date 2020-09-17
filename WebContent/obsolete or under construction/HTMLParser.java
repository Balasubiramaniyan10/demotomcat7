package com.searchasaservice.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.bcel.classfile.Node;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.searchasaservice.parser.xpathparser.Analyzer;

public class HTMLParser {

	public Document document=null;
	public String url="http://www.roelvin.eu/wijnenwijn.php";

	public HTMLParser(String html){
		//Thread.currentThread().dumpStack();
		// First remove any xmlns as it will confuse the parser
		html=html.replaceAll("(^|\\s)xmlns(:[^=\"]+)?=\"[^\"]*\""," ");
		html=html.replaceAll("(?<=</?)\\w+:","");
		html=html.replaceAll("<!DOCTYPE [^>]*>","");
		if (!html.contains("<html")&&!html.contains("<xml")){
			// Data feed?
			int tab=html.split("\t").length;
			int pipe=html.split("\\|").length;
			int comma=html.split("['\"],['\"]").length;
			if (tab+pipe+comma>200){
				String separator="\t";
				if (pipe>tab) separator="\\|";
				if (comma>tab||comma>pipe) separator="['\"],['\"]";
				html=html.replaceAll(separator, "</td><td>");
				html=html.replaceAll("\n", "</td></tr><tr><td>");
				html="<html><body><table><tr><td>"+html;
				html+="</td></tr></table></body></html>";
			}
		}
		//Dbutil.logger.info(html);
		
		//html=html.replaceAll("\n","");
		
		try {
			getXML(html);
		} catch (Exception e) {

		} 
		if (document==null){
			// First try tidy, then Tagsoup 
			String tidiedhtml="";
			try {
				tidiedhtml=tidy(html);
				if ("".equals(tidiedhtml)) {
					tidiedhtml=html; // This happens when Tidy can't chew through it
				}
				document = tagSoup(tidiedhtml);
			} catch (Exception e) {
				Dbutil.logger.info("Problem: cannot parse html as it is", e);
				tidiedhtml="";
			}
			if ("".equals(tidiedhtml)){
				html=html.replaceAll("<\\w+:[^>]*?>", "<");
				html=html.replaceAll("</\\w+:[^>]*?>", "<");

				try {
					tidiedhtml=tidy(html);
					if ("".equals(tidiedhtml)) {
						tidiedhtml=html; // This happens when Tidy can't chew through it
					}
					document = tagSoup(tidiedhtml);
				} catch (Exception e) {
					Dbutil.logger.info("Problem: cannot parse html as it is", e);
					tidiedhtml="";
				}

			}
			if ("".equals(tidiedhtml)){
				Dbutil.logger.error("Sorry. Could not parse html.");
			}

		}
		//Dbutil.logger.info(Analyzer.getNodeAsXML(document));
		
	}

	public String tidy (String html) throws Exception{
		// Tidy produces a clean document that is not 100% correct.
		// A second pass by Tag Soup is necessary

		Tidy tidy = new Tidy(); // obtain a new Tidy instance
		tidy.setXHTML(false); 
		tidy.setQuiet(true);
		tidy.setIndentContent(false);
		tidy.setIndentAttributes(false);
		tidy.setShowWarnings(false);
		tidy.setFixComments(false);
		
		//tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		//tidy.setErrout(new PrintWriter(err));

		//byte currentXMLBytes[] = html.getBytes("UTF-8");
		byte currentXMLBytes[] = html.getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes); 
		tidy.parse(byteArrayInputStream, out);
		out.close();
		String parsedhtml=out.toString();
		parsedhtml=parsedhtml.replace("\r\n", " ");
		return parsedhtml;

	}
	public void getXML (String html) throws Exception{
		Document doc=null;
		byte currentXMLBytes[] = html.getBytes("UTF-8");
		ByteArrayInputStream c = new ByteArrayInputStream(currentXMLBytes); 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.parse(c);
		doc.getDocumentElement().normalize();
		document=doc;

	}
	public Document tagSoup (String html) throws Exception{
		InputSource is=new InputSource(new StringReader(html));
		DOMResult result=null;
		XMLReader reader = new org.ccil.cowan.tagsoup.Parser();
		reader.setFeature(Parser.namespacesFeature, false);
		reader.setFeature(Parser.namespacePrefixesFeature, false);
		System.setProperty("javax.xml.transform.TransformerFactory","org.apache.xalan.processor.TransformerFactoryImpl");

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		//Dbutil.logger.info("Transformer: "+transformer);

		result = new DOMResult();
		transformer.transform(new SAXSource(reader, is),result);
		org.w3c.dom.Node n=result.getNode();
		//Dbutil.logger.info(n.getClass());
		
		return (Document)n;

	}

	public static String getPage(String urlstr){
		URL url=null;
		InputSource is;
		String Page=null;
		String domainurl="";
		String urlpath="";

		try{
			if(urlstr.startsWith("file://")){
				File file=new File(urlstr);
				FileReader fstream = new FileReader(file);
				BufferedReader in = new BufferedReader(fstream);
				StringBuffer sb=new StringBuffer();
				while (in.ready()){
					sb.append(in.readLine());
				}
				in.close();
				Page=sb.toString();
			} else {
				if (urlstr.indexOf("/", 9)>0) {
					domainurl=urlstr.substring(0, urlstr.indexOf("/", 9));
				} else {
					domainurl=urlstr;
				}

				urlpath=urlstr.substring(0, urlstr.lastIndexOf("/"));
				if (domainurl.length()>urlpath.length()) urlpath=domainurl;
				String encoding="";
				try{
					url = new URL(urlstr);
					encoding=Spider.getHtmlEncoding(urlstr);
				} catch (Exception e){}
				Webpage webpage=new Webpage();
				webpage.encoding=encoding;
				webpage.urlstring=domainurl;
				webpage.readPage();
				webpage.urlstring=urlstr;
				webpage.readPage();
				Page=webpage.html;
				
			}
		} catch(Exception e){}
		if (url==null) {
			try {
				FileInputStream fis = new java.io.FileInputStream(new java.io.File(
				"C:\\Temp\\uk.xml"));
				int x = fis.available();
				byte b[] = new byte[x];
				fis.read(b);
				Page = new String(b);
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
		}
		return Page;

	}



	public Document OBSOLETEgetDocumentFromDirtyHTMLHTMLParser (InputSource is) throws Exception{
		// HTMLparser does not create a hierarchy, it only produces a flat nodelist
		DOMResult result=null;
		org.htmlparser.sax.XMLReader reader = new org.htmlparser.sax.XMLReader();

		//reader.setFeature(name, value)
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		result = new DOMResult();
		transformer.transform(new SAXSource(reader, is),result);
		return (Document)result.getNode();

		//String outputHTML=HTMLParser.cleanHTML(inputHTML);
		//Transformer transformer = TransformerFactory.newInstance().newTransformer();
		//InputSource is=new InputSource(new StringReader(outputHTML));
		//DOMResult result = new DOMResult();
		//transformer.transform(new SAXSource(is),result);
		//return (Document)result.getNode();
	}

	public void OBSOLETEMozillaParser(String Page){
		if (false){
			/*
			// The mozilla parser does not output correct XML or HTML, the resulting document gives DOM exceptions
			File parserLibraryFile = new File("native/bin/MozillaParser" + EnviromentController.getSharedLibraryExtension());
			String parserLibrary = parserLibraryFile.getAbsolutePath();
			System.out.println("Loading Parser Library :" + parserLibrary);
			//	mozilla.dist.bin directory :
			final File mozillaDistBinDirectory = new File("native\\bin\\mozilla.dist.bin."+EnviromentController.getOperatingSystemName());
			System.out.println(mozillaDistBinDirectory.getAbsolutePath());

			MozillaParser.init(parserLibrary,mozillaDistBinDirectory.getAbsolutePath());		
			MozillaParser parser = new MozillaParser();

			parser.callNativeHtmlParser(Page);
			Document originaldocument = parser.parse(Page);
			//System.out.println("Generated document :" + getNodeAsXML(originaldocument));
			//is=new InputSource(new StringReader(getNodeAsXML(originaldocument)));
			DOMResult result = new DOMResult();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			//transformer.transform(new SAXSource(is),result);
			originaldocument= (Document)result.getNode();
			System.out.println("Generated document :" + ((org.dom4j.Document)originaldocument).asXML());
			 */
		}


	}

}

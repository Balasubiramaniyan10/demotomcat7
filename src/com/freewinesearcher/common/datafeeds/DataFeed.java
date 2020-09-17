package com.freewinesearcher.common.datafeeds;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.StreamResult;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.record.formula.functions.Rows;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.apache.xpath.NodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.freewinesearcher.batch.CSVReader;
import com.freewinesearcher.batch.Excelreader;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.TableScraper;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Serializer;
import com.freewinesearcher.common.Shop;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Wineset;
import com.freewinesearcher.online.Webroutines;
import com.searchasaservice.parser.xpathparser.XpathParser;
import com.searchasaservice.parser.xpathparser.Analyzer2;
import com.searchasaservice.parser.xpathparser.XpathParserConfig;

public class DataFeed implements Serializable {
	private static final long serialVersionUID = 1L;
	public long id = 0;
	public int shopid = 0;
	public int maxlabels = 3;
	public int skiplines = 0;
	transient public String feed;
	public String encoding;

	public static enum formats {
		FlatFile, FroogleFlatFile, GoogleRSS20, Shopping, PriceGrabber, Atom03, Atom10, RSS10, GoogleRSS10, GoogleAtom10, GoogleAtom03, RSS20, MySimon, Yahoo, OtherXML, CSV, BevMediaCSV, Unknown, HTML, malformedXML, Elmar, Excel
	};

	public formats format = formats.Unknown;

	public static enum urlstatusses {
		OK, MalformedURL, CannotLoadURL, EmptyURL, DuplicateURL, NOK
	};

	public urlstatusses urlstatus = urlstatusses.NOK;

	public static enum feedstatusses {
		OK, NOKHTML, NOKMalformedXML, NOKElmarShopinfo, NOKUnrecognizedFormat, NOKParseError, NOK
	};

	public feedstatusses feedstatus = feedstatusses.NOK;
	private String delimiter = null;
	private char quotechar = "\"".charAt(0);
	transient private String xmlfeed = null;
	private String itemsep = null;
	private ArrayList<String> labels = new ArrayList<String>(0);
	private boolean headerrow = true;
	public boolean assume75cl = false;
	transient public FeedContent feedContent;
	String[] nameorder = getEmptyOrder();
	String[] vintageorder = getEmptyOrder();
	String[] urlorder = getEmptyOrder();
	String[] priceorder = getEmptyOrder();
	String[] sizeorder = getEmptyOrder();
	String priceregex = "(?:.*?\\D)??(\\d*[,.]?\\d+[,.]?\\d*)(?:\\D.*?)?";
	String priceregexforanalysis = "(?:.*?\\D)??(\\d*[,.]?\\d+[,.]\\d+)(?:\\D.*?)?";
	String priceregexexclude = "(0[,.]75)";
	String nameregex = "([a-zA-Z]{3})";
	String nameregexexclude = "(http)";
	String vintageregex1test = "(?:.*?\\D)?((19[890]\\d)|(200\\d))(?:\\D.*?)?";
	String vintageregex2test = "(?:.*?\\D)?((19[890]\\d)|(200\\d)|(\\d\\d))(?:\\D.*?)?";
	String vintageregex1real = "(?:.*?\\D)?((19\\d\\d)|(20[01]\\d))(?:\\D.*?)?";
	String vintageregex2real = "(?:.*?\\D)?((19\\d\\d)|(20[01]\\d)|(\\d\\d))(?:\\D.*?)?";
	String vintageregex = vintageregex1real;
	String urlregex = "(https?://([^'\" ><]*))";
	String urlregexexclude = "\\.(jpg|png|gif)";
	String problemdescription = "";
	public String url = "";
	public String urlproblem = "";
	public String shopconfigurl = "";
	transient public int wineswithsize = 0;
	transient public int responsecode = 0;
	transient public boolean badurl = false;
	public boolean validfeed = true;

	private ArrayList<String> getEmptyArray() {
		ArrayList<String> dummy = new ArrayList<String>(maxlabels);
		for (int i = 0; i < maxlabels; i++)
			dummy.add("");
		return dummy;
	}

	public DataFeed() {
		validfeed = false;
	}

	public DataFeed(String url) {
		if (!"".equals(url.trim()) && url != null) {
			setUrl(url);
			URL u = null;
			try {
				u = new URL(url);
			} catch (Exception e) {
				urlproblem = e.getMessage();
				urlstatus = urlstatusses.MalformedURL;
			}
			if (u != null) {
				encoding = Spider.getHtmlEncoding(url);
				Webpage webpage = new Webpage();
				webpage.maxattempts = 1;
				webpage.encoding = encoding;
				webpage.urlstring = url;
				webpage.ignorepagenotfound = true;
				webpage.readPage();
				badurl = webpage.badurl;
				String Page = webpage.html;
				responsecode = webpage.responsecode;

				if (!Page.startsWith("Webpage")) {
					urlstatus = urlstatusses.OK;
					feed = Page;
					String header = Webroutines.getRegexPatternValue(
							"^(\\n<html>\\n\\t<body>\\n(\\t<div align=\"center\">\\n)?<pre>\\nWine Directory List\\n<BR>\\n\\n)",
							feed);
					if (header.length() > 0) {
						feed = feed.substring(header.length()); // BevMedia feeds
						format = formats.BevMediaCSV;
						feedstatus = feedstatusses.OK;
						setDelimiter("\t");
						nameorder[0] = ("Wine Title");
						vintageorder[0] = ("Vintage");
						priceorder[0] = ("Price");
						sizeorder[0] = ("Bottle Size");
						urlorder[0] = ("URL");
						parse();
					} else {
						guessFeedFormat(webpage);

						if (feedstatus.equals(feedstatusses.NOKParseError) && format.equals(formats.Excel)) {
							feedstatus = feedstatusses.OK;
							format = formats.CSV;
							guessDelimiter();
							parse();
							if (feedstatus.equals(feedstatusses.NOKParseError)) {
								format = formats.Excel;
							}
						}
						if (feedstatus.equals(feedstatusses.OK)) {
							if (format.equals(formats.CSV)) {
								guessDelimiter();
								guessLeadingEmptyLines();
							}
							parse();
							if (labels.size() < 3 && feed.toLowerCase().contains("<html")) {
								feedstatus = feedstatusses.NOKHTML;
								format = formats.HTML;

							}
							guessFeedLabels();
						}
					}
				} else {
					if (badurl)
						urlstatus = urlstatusses.MalformedURL;
					if (responsecode == 0 || responsecode >= 400)
						urlstatus = urlstatusses.CannotLoadURL;
				}
			}
		} else {
			urlstatus = urlstatusses.EmptyURL;
			validfeed = false;
		}
	}

	private void guessLeadingEmptyLines() {
		try {
			feedContent = new FeedContent();
			CSVReader reader = new CSVReader(new java.io.StringReader(feed), delimiter.charAt(0), quotechar, 0);
			List<String[]> rows = reader.readAll();
			int linenumber = 0;
			int numberoffields = 0;
			for (String[] values : rows) {
				if (linenumber < 50 && values.length > numberoffields) {
					numberoffields = values.length;
					skiplines = linenumber;
				}
				linenumber++;
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem: Could not parse CSV", e);
			feedstatus = feedstatusses.NOKParseError;
		}

	}

	public DataFeed(String url, Document doc) { // Elmar
		this(url);
		/*
		 * setUrl(url); encoding=Spider.getHtmlEncoding(url); Webpage webpage=new
		 * Webpage(); webpage.maxattempts=1; webpage.encoding=encoding;
		 * webpage.urlstring=url; webpage.ignorepagenotfound=true; webpage.readPage();
		 * badurl=webpage.badurl; String Page=webpage.html;
		 * responsecode=webpage.responsecode; if (!Page.startsWith("Webpage")){
		 * urlstatus=urlstatusses.OK; feed=Page; format=formats.CSV;
		 */

		delimiter = Shop.getNodeValueFromXpath(doc, "//SpecialCharacters/@delimiter");
		if (delimiter.equals("[tab]"))
			delimiter = "\t";
		nameorder = getEmptyOrder();
		nameorder[0] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='name']/@columnName");
		priceorder = getEmptyOrder();
		priceorder[0] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='price']/@columnName");
		vintageorder = getEmptyOrder();
		vintageorder[0] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='name']/@columnName");
		vintageorder[1] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='longdescription']/@columnName");
		urlorder = getEmptyOrder();
		urlorder[0] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='url']/@columnName");
		sizeorder = getEmptyOrder();
		sizeorder[0] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='name']/@columnName");
		sizeorder[1] = Shop.getNodeValueFromXpath(doc, "//Mappings/Mapping[@type='longdescription']/@columnName");
	}

	public DataFeed(HttpServletRequest request) {
		// If a file was uploaded, fill fileItem with it
		try {
			if (ServletFileUpload.isMultipartContent(request)) {
				ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
				List fileItemsList = servletFileUpload.parseRequest(request);
				String optionalFileName = "";
				if (fileItemsList.size() > 0) {
					if (fileItemsList.size() > 1) {

					} else {
						FileItem fileItem = (FileItem) fileItemsList.get(0);
						String type = fileItem.getContentType();
						if (type.contains("excel") || type.contains("xls") || type.contains("octet-stream")) {
							urlstatus = urlstatusses.OK;
							format = formats.Excel;
							feedContent = Excelreader.getasFeed(fileItem.getInputStream());
							parseExcel();
							guessFeedLabels();
						} else {
							byte[] file = fileItem.get();
							String Page = new String(file, "iso-8859-1");
							if (Page.contains("�"))
								Page = new String(file, "UTF-8");
							urlstatus = urlstatusses.OK;
							feed = Page;
							guessFeedFormat(new Webpage());
							if (feedstatus.equals(feedstatusses.OK)) {
								if (format.equals(formats.CSV)) {
									guessDelimiter();
								}
								parse();
								guessFeedLabels();
							}
						}

					}
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		}
	}

	public long save(Context c) {
		if (shopid > 0) {
			try {
				if (id == 0)
					id = Dbutil.readIntValueFromDB("select *,count(*) as thecount from datafeeds where tenant="
							+ c.tenant + " and shopid=" + shopid
							+ " and classname='com.freewinesearcher.common.datafeeds.DataFeed' group by tenant,shopid having thecount=1;",
							"id");
				id = Serializer.writeJavaObject("datafeeds", c.tenant, shopid, id, serialVersionUID, this);
				return id;
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
		} else {
			Dbutil.logger.error("Could not save datafeed because shopid=0");
		}
		return id;
	}

	public static DataFeed getDataFeed(Context c, int shopid, int row) {
		if (row == 0) {
			row = Dbutil.readIntValueFromDB("select *,count(*) as thecount from datafeeds where tenant=" + c.tenant
					+ " and shopid=" + shopid
					+ " and classname='com.freewinesearcher.common.datafeeds.DataFeed' group by tenant,shopid having thecount=1;",
					"id");
		}
		if (row > 0) {
			try {
				DataFeed feed = (DataFeed) Serializer.readJavaObject("datafeeds", c.tenant, shopid, row);
				feed.id = row;
				feed.shopid = shopid;
				feed.readFeed();
				return feed;
			} catch (Exception e) {
				Dbutil.logger.error(
						"Problem retrieving datafeed with id " + row + ", shopid " + shopid + " for tenant " + c.tenant,
						e);
			}
		}
		return null;
	}

	public void readFeed() {
		feed = Spider.getWebPage(url, encoding, new Variables(), "", "True", true);
		String header = Webroutines.getRegexPatternValue(
				"^(\\n<html>\\n\\t<body>\\n(\\t<div align=\"center\">\\n)?<pre>\\nWine Directory List\\n<BR>\\n\\n)",
				feed);
		if (header.length() > 0) {
			feed = feed.substring(header.length()); // BevMedia feeds
		}
		feed = feed.replaceAll("^\\s*\r?\n", "");

	}

	public void parse() {
		feedContent = new FeedContent();
		if (format.equals(formats.CSV) || format.equals(formats.BevMediaCSV)) {
			readFeed();
			parseCSV();
		} else if (format.equals(formats.Excel)) {
			try {
				feedContent = Excelreader.getasFeed(url);
			} catch (Exception e) {
				feedstatus = feedstatusses.NOKParseError;
			}
			parseExcel();
		} else if (format.equals(formats.Elmar)) {
			// do nothing because this is not the product feed
		} else if (format.equals(formats.Yahoo)) {
			parseYahoo();
		} else {
			parseXML();
		}
	}

	private void parseXML() {
		try {
			readFeed();
			xmlfeed = feed;
			encoding = Webroutines.getRegexPatternValue("encoding=\"([^\"]+)\"", feed);
			if (encoding.equals(""))
				encoding = "utf-8";
			Row row;
			NodeSet rownodes;
			NodeSet itemnodes;
			Document doc = null;
			try {
				byte currentXMLBytes[] = xmlfeed.getBytes(encoding);
				ByteArrayInputStream c = new ByteArrayInputStream(currentXMLBytes);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();// TO DO: Fix this. Cannot parse feed
																					// http://www.debarrica.com/wein.cc.txt
																					// .
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(c);
				doc.getDocumentElement().normalize();

			} catch (Exception e) {
				feedstatus = feedstatusses.NOKMalformedXML;
			}
			if (feedstatus.equals(feedstatusses.OK)) {
				itemnodes = getNodeSetFromxpath((Node) doc, "//" + itemsep);
				for (int i = 0; i < itemnodes.getLength(); i++) {
					row = new Row();
					rownodes = XpathParser.getNodesWithTextNodes(itemnodes.item(i), "");
					for (int j = 0; j < rownodes.getLength(); j++) {
						if (rownodes.item(j).getTextContent() != null) {
							row.put(rownodes.item(j).getNodeName(), rownodes.item(j).getTextContent());
							if (!labels.contains(rownodes.item(j).getNodeName()))
								labels.add(rownodes.item(j).getNodeName());
						}
					}
					if (row.size() > 0)
						feedContent.add(row);
				}
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem: Could not parse XML", e);
			feedstatus = feedstatusses.NOKParseError;
			format = formats.malformedXML;
		}
	}

	// Yahoo contains all data in values, we need to transform it.
	private void parseYahoo() {

		try {
			readFeed();
			xmlfeed = feed;
			encoding = Webroutines.getRegexPatternValue("encoding=\"([^\"]+)\"", feed);
			if (encoding.equals(""))
				encoding = "utf-8";
			Row row;
			NodeSet rownodes;
			NodeSet itemnodes;
			Document doc = null;
			try {
				byte currentXMLBytes[] = xmlfeed.getBytes(encoding);
				ByteArrayInputStream c = new ByteArrayInputStream(currentXMLBytes);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(c);
				doc.getDocumentElement().normalize();

			} catch (Exception e) {
				feedstatus = feedstatusses.NOKMalformedXML;
			}
			if (feedstatus.equals(feedstatusses.OK)) {
				itemnodes = getNodeSetFromxpath((Node) doc, "//" + itemsep);
				for (int i = 0; i < itemnodes.getLength(); i++) {
					row = new Row();
					rownodes = getNodesWithName(itemnodes.item(i), "ItemField");
					for (int j = 0; j < rownodes.getLength(); j++) {
						row.put(rownodes.item(j).getAttributes().getNamedItem("TableFieldID").getNodeValue(),
								rownodes.item(j).getAttributes().getNamedItem("Value").getNodeValue());
						if (!labels
								.contains(rownodes.item(j).getAttributes().getNamedItem("TableFieldID").getNodeValue()))
							labels.add(rownodes.item(j).getAttributes().getNamedItem("TableFieldID").getNodeValue());
					}
					if (row.size() > 0)
						feedContent.add(row);
				}
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem: Could not parse Yahoo XML", e);
			feedstatus = feedstatusses.NOKParseError;
			format = formats.malformedXML;
		}
	}

	public static NodeSet getNodesWithName(Node n, String name) {
		// Dbutil.logger.info(getNodeAsXML(n));
		String regex = "descendant::" + name;
		NodeSet nodeset = new NodeSet();
		try {
			XPathEvaluator xpeval = new XPathEvaluator();
			XPathExpression exp = xpeval.compile(regex);
			NodeList ni = (NodeList) exp.evaluate(n, XPathConstants.NODESET);
			for (int i = 0; i < ni.getLength(); i++) {
				// Add non-empty nodes
				// Dbutil.logger.info(ni.item(i).getNodeName()+":"+ni.item(i).getTextContent());

				nodeset.addNode(ni.item(i));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return nodeset;
	}

	public String toHTML() {
		StringBuffer sb = new StringBuffer();
		if (feedContent != null) {
			sb.append("<table><tr>");
			for (String label : labels)
				sb.append("<th style='width:100px;text-align:left'>" + label + "</th>");
			sb.append("</tr>");
			int i = 0;
			for (Row row : feedContent) {
				if (i < 100) {
					sb.append("<tr>");

					for (String label : labels) {
						if (row.get(label) == null) {
							if (format.equals(formats.CSV)) {
								sb.append("<td>Missing data!</td>");
							} else {
								sb.append("<td></td>");

							}
						} else {
							sb.append("<td>" + Spider.escape(row.get(label)) + "</td>");
						}
					}
					sb.append("</tr>");
				}
				i++;
			}
			sb.append("</table>");
		}
		return sb.toString();
	}

	public String[] getOrderObject(String field) {
		String[] fieldvalues = getEmptyOrder();
		if (field.equals("name"))
			fieldvalues = nameorder;
		if (field.equals("size"))
			fieldvalues = sizeorder;
		if (field.equals("url"))
			fieldvalues = urlorder;
		if (field.equals("vintage"))
			fieldvalues = vintageorder;
		if (field.equals("price"))
			fieldvalues = priceorder;
		return fieldvalues;
	}

	public String getSelectBox(String field) {
		StringBuffer sb = new StringBuffer();
		String[] fieldvalues = getOrderObject(field);
		for (int i = 0; i < maxlabels; i++) {
			sb.append("<td><select name='" + field + "order'>");
			sb.append("<option value=''>None</option>");
			for (String label : labels) {
				sb.append("<option value='" + label + "' "
						+ (label.equals(fieldvalues[i]) ? "selected='selected' " : "") + ">" + label + "</option>");
			}
		}
		sb.append("</select></td>");

		return sb.toString();
	}

	public static NodeSet getNodeSetFromxpath(Node node, String xpath) {
		NodeSet n = new NodeSet();
		if ("" != xpath) {
			DOMNodeList nl;
			try {
				XPathEvaluator xpeval = new XPathEvaluator();
				XPathExpression exp = xpeval.compile(xpath);
				nl = (DOMNodeList) exp.evaluate(node, XPathConstants.NODESET);
				for (int i = 0; i < nl.getLength(); i++) {
					n.addNode(nl.item(i));
				}

			} catch (Exception e) {
				Dbutil.logger.info("Problem with xpath " + xpath, e);
			}
		}
		return n;
	}

	public formats guessFeedFormat(Webpage webpage) {
		Document doc = null;
		try {
			if (webpage.type != null && (webpage.type.contains("excel") || webpage.type.contains("xls"))) {
				format = formats.Excel;
				try {
					feedContent = Excelreader.getasFeed(webpage.urlstring);
					feedstatus = feedstatusses.OK;
				} catch (Exception e) {
					feedstatus = feedstatusses.NOKParseError;
				}

			} else {
				if (feed.contains("<?xml") || feed.contains("<?XML")) {
					feedstatus = feedstatusses.OK;
					try {
						/// XML format
						byte currentXMLBytes[] = feed.getBytes("UTF-8");
						ByteArrayInputStream c = new ByteArrayInputStream(currentXMLBytes);
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						doc = db.parse(c);
						doc.getDocumentElement().normalize();
					} catch (Exception e) {
						format = formats.malformedXML;
						feedstatus = feedstatusses.NOKMalformedXML;
					}
					if (format == null || !format.equals(formats.malformedXML)) {
						if (feed.contains("http://store.yahoo.com/doc/dtd/Catalog.dtd")) {
							format = DataFeed.formats.Yahoo;
							setItemsep("Item");
						} else if (feed.contains("<feed version=\"0.3\"")
								&& feed.contains("xmlns=\"http://purl.org/atom/ns#\"")) {
							if (feed.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")) {
								format = DataFeed.formats.GoogleAtom03;
							} else {
								format = DataFeed.formats.Atom03;
							}
							setItemsep("entry");
						} else if (feed.contains("<feed version=\"0.3\"")
								&& feed.contains("xmlns=\"http://purl.org/atom/ns#\"")) {
							if (feed.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")) {
								format = DataFeed.formats.GoogleAtom03;
							} else {
								format = DataFeed.formats.Atom03;
							}
							setItemsep("entry");
						} else if (feed.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\"")) {
							if (feed.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")) {
								format = DataFeed.formats.GoogleAtom10;
							} else {
								format = DataFeed.formats.Atom10;
							}
							setItemsep("entry");
						} else if (feed.contains("<rdf:RDF") && feed.contains("xmlns=\"http://purl.org/rss/1.0/\"")) {
							if (feed.contains("xmlns:g=\"http://base.google.com/ns/1.0\"")) {
								format = DataFeed.formats.GoogleRSS10;
							} else {
								format = DataFeed.formats.RSS10;
							}
							setItemsep("item");
						} else if (feed.contains("http://elektronischer-markt.de/schema")) {
							format = DataFeed.formats.Elmar;
							feedstatus = feedstatusses.NOKElmarShopinfo;
						} else {
							format = formats.OtherXML;
							guessItemSeparator((Node) doc);
						}
					}
				} else if (false && feed.toLowerCase().contains("<html")) {
					feedstatus = feedstatusses.NOKHTML;
					format = formats.HTML;

				} else {
					feedstatus = feedstatusses.OK;
					format = formats.CSV;
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not analyze data feed. ", e);
		}
		return format;
	}

	public void guessItemSeparator(Node node) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		NodeSet ns = getNodeSetFromxpath(node, "//*[descendant::element()[child::text()[matches(.,\"\\S\\S\\S\")]]]");
		for (int i = 0; i < ns.getLength(); i++) {
			if (map.containsKey(ns.item(i).getNodeName())) {
				map.put((ns.item(i).getNodeName()), map.get(ns.item(i).getNodeName()) + 1);
			} else {
				map.put((ns.item(i).getNodeName()), 1);
			}
		}
		ArrayList<Map.Entry<String, Integer>> as = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

		Collections.sort(as, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map.Entry e1 = (Map.Entry) o1;
				Map.Entry e2 = (Map.Entry) o2;
				Integer first = (Integer) e1.getValue();
				Integer second = (Integer) e2.getValue();
				return -1 * first.compareTo(second);
			}
		});

		Iterator i = as.iterator();
		if (i.hasNext()) {
			itemsep = (String) ((Map.Entry) i.next()).getKey();
		}

	}

	public void guessFeedLabels() {
		if (format.equals(formats.CSV)) {
			recognizeFlatFile();
		}
		vintageregex = vintageregex1real;
		if (urlorder[0] == null || urlorder[0].equals(""))
			urlorder = guessLabel(urlregex, "", urlregexexclude);
		if (priceorder[0] == null || priceorder[0].equals(""))
			priceorder = guessLabel(priceregexforanalysis, "", priceregexexclude);
		if (vintageorder[0] == null || vintageorder[0].equals(""))
			vintageorder = guessLabel(vintageregex1test, "", "");
		if (sizeorder[0] == null || sizeorder[0].equals(""))
			sizeorder = guessSizeLabel();
		if (nameorder[0] == null || nameorder[0].equals(""))
			nameorder = guessLabel(nameregex, "(name)", nameregexexclude);
		if (nameorder[0] == null || nameorder[0].equals(""))
			nameorder = guessLabel(nameregex, "", nameregexexclude);
		if (vintageorder[0] == null || vintageorder[0].equals("")) {
			vintageregex = vintageregex2real;
			vintageorder = guessLabel(vintageregex2test, "", "");
		}
		if (priceorder[0].equals("") && feed.toLowerCase().contains("<html")) {
			feedstatus = feedstatusses.NOKHTML;
			format = formats.HTML;

		}
	}

	public String[] guessSizeLabel() {
		String[] labellist = getEmptyOrder();
		ArrayList<String> values = new ArrayList<String>();
		values.add("0");
		String order = "0";
		float size;
		int max = Math.min(20, feedContent.size());
		TreeMap<String, Integer> hits = new TreeMap<String, Integer>();
		for (String l : labels)
			hits.put(l, 0);
		for (int i = 0; i < max; i++) {
			for (String label : feedContent.get(i).keySet()) {
				values = new ArrayList<String>();
				values.add("");
				values.add(feedContent.get(i).get(label));
				size = TableScraper.getSize(values, order, "", 0, false);
				if (size > 0 && size < 4) {
					hits.put(label, hits.get(label) + 1);
				}
			}
		}
		ArrayList<Map.Entry<String, Integer>> as = new ArrayList<Map.Entry<String, Integer>>(hits.entrySet());

		Collections.sort(as, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map.Entry e1 = (Map.Entry) o1;
				Map.Entry e2 = (Map.Entry) o2;
				Integer first = (Integer) e1.getValue();
				Integer second = (Integer) e2.getValue();
				return -1 * first.compareTo(second);
			}
		});

		Iterator i = as.iterator();
		if (i.hasNext()) {
			String label = (String) ((Map.Entry) i.next()).getKey();
			max = hits.get(label);
			if (max > 0) {
				labellist[0] = label;
			}
			int counter = 1;
			while (i.hasNext() && counter < maxlabels) {
				label = (String) ((Map.Entry) i.next()).getKey();
				int value = hits.get(label);
				if (value > max / 2) {
					labellist[counter] = label;
					counter++;
				}
			}
		}
		return labellist;

	}

	public String[] guessLabel(String regex, String labelregex, String regexexclude) {
		String[] labellist = getEmptyOrder();
		int max = Math.min(20, feedContent.size());
		TreeMap<String, Integer> hits = new TreeMap<String, Integer>();
		for (String l : labels)
			hits.put(l, 0);
		for (int i = 0; i < max; i++) {
			for (String label : feedContent.get(i).keySet()) {
				if (labels.contains(label)) {
					if (labelregex.equals("") || !Webroutines
							.getRegexPatternValue(labelregex, label, Pattern.CASE_INSENSITIVE).equals("")) {
						if (!Webroutines.getRegexPatternValue(regex, feedContent.get(i).get(label)).equals("")) {
							if (regexexclude.equals("") || Webroutines.getRegexPatternValue(regexexclude,
									feedContent.get(i).get(label), Pattern.CASE_INSENSITIVE).equals("")) {
								hits.put(label, hits.get(label) + 1);
							}
						}
					}
				}
			}
		}
		ArrayList<Map.Entry<String, Integer>> as = new ArrayList<Map.Entry<String, Integer>>(hits.entrySet());

		Collections.sort(as, new Comparator() {
			public int compare(Object o1, Object o2) {
				Map.Entry e1 = (Map.Entry) o1;
				Map.Entry e2 = (Map.Entry) o2;
				Integer first = (Integer) e1.getValue();
				Integer second = (Integer) e2.getValue();
				return -1 * first.compareTo(second);
			}
		});

		Iterator i = as.iterator();
		if (i.hasNext()) {
			String label = (String) ((Map.Entry) i.next()).getKey();
			max = hits.get(label);
			if (max > 0) {
				labellist[0] = label;
			}
			int counter = 1;
			while (i.hasNext() && counter < maxlabels) {
				label = (String) ((Map.Entry) i.next()).getKey();
				int value = hits.get(label);
				if (value > max / 2) {
					labellist[counter] = label;
					counter++;
				}
			}
		}
		return labellist;
	}

	public String[] getEmptyOrder() {
		String[] n = new String[maxlabels];
		for (int i = 0; i < maxlabels; i++) {
			n[i] = "";
		}
		return n;
	}

	class HitSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			return ((Integer) o1).compareTo(((Integer) o2) * -1);
		}
	}

	public void recognizeFlatFile() {
		if (delimiter.equals("\t") && feed.contains("product_name") && feed.contains("price") && feed.contains("URL")) {
			if (nameorder.equals(""))
				nameorder[0] = ("product_name");
			if (vintageorder.equals(""))
				vintageorder[0] = ("product_name");
			if (priceorder.equals(""))
				priceorder[0] = ("price");
			if (sizeorder.equals(""))
				sizeorder[0] = ("product_name");
			if (urlorder.equals(""))
				urlorder[0] = ("URL");
		} else if (delimiter.equals(",") && feed.contains("Product Description") && feed.contains("Product Price")
				&& feed.startsWith("MPN")) {
			if (nameorder.equals(""))
				nameorder[0] = ("Product_Description");
			if (vintageorder.equals(""))
				vintageorder[0] = ("Product_Description");
			if (priceorder.equals(""))
				priceorder[0] = ("Product_Price");
			if (sizeorder.equals(""))
				sizeorder[0] = ("Product_Description");
			if (urlorder.equals(""))
				urlorder[0] = ("Product_URL");
		} else if (delimiter.equals("\t") && feed.contains("product-url") && feed.contains("description")
				&& feed.startsWith("code")) {
			if (nameorder.equals(""))
				nameorder[0] = ("name;description");
			if (vintageorder.equals(""))
				vintageorder[0] = ("name;description");
			if (priceorder.equals(""))
				priceorder[0] = ("price");
			if (sizeorder.equals("")) {
				sizeorder[0] = ("name");
				sizeorder[0] = ("description");
			}
			if (urlorder.equals(""))
				urlorder[0] = ("product-url");
		}
	}

	private int countDelimiters(String line, char delimiter) {
		int count = 0;
		for (int i = 0; i < line.length(); i++) {
			char next = line.charAt(i);
			if (next == delimiter) {
				count++;
			}
		}
		return count;

	}

	private String guessDelimiter() {
		// Flat File Format
		Pattern pattern;
		Matcher matcher;
		String line = "";
		boolean found = false;
		pattern = Pattern.compile("([^\\n]*?\\w\\w\\w[^\n]*)\n", Pattern.MULTILINE + Pattern.DOTALL);
		matcher = pattern.matcher(feed);
		setDelimiter("");
		int tab = 0;
		int comma = 0;
		int semicolon = 0;
		int pipe = 0;
		while (!found && matcher.find()) {
			line = matcher.group(1);
			tab += countDelimiters(line, '\t');
			comma += countDelimiters(line, ',');
			semicolon += countDelimiters(line, ';');
			pipe += countDelimiters(line, '|');
			if (tab > 20 || comma > 20 || pipe > 20 || semicolon > 20) {
				found = true;
			}
		}
		if (tab > 3 || comma > 3 || pipe > 3 || semicolon > 3) {

			if (tab >= comma && tab >= semicolon && tab >= pipe) {
				// Tab
				setDelimiter("\t");
			} else if (comma >= semicolon && comma >= pipe) {
				// Comma
				setDelimiter(",");
			} else if (semicolon >= pipe) {
				// Semicolon
				setDelimiter(";");
			} else {
				// Pipe
				setDelimiter("|");
			}
		}
		if (getDelimiter().equals("")) {
			feedstatus = feedstatusses.NOKParseError;
		}
		return getDelimiter();
	}

	public DataFeed(String feed, formats format) {
		this.feed = feed;
		this.format = format;
		this.labels = null;
	}

	public String[] getNameorder() {
		for (int i = 0; i < maxlabels; i++)
			if (nameorder[i] == null)
				nameorder[i] = "";
		return nameorder;
	}

	public void setNameorder(String[] nameorder) {
		for (int i = 0; i < maxlabels; i++)
			if (nameorder[i] == null)
				nameorder[i] = "";
		this.nameorder = nameorder;

	}

	public String getFeedContent() {
		if (feedContent == null)
			return null;
		return feedContent.toString();
	}

	public String[] getVintageorder() {
		for (int i = 0; i < maxlabels; i++)
			if (vintageorder[i] == null)
				vintageorder[i] = "";
		return vintageorder;
	}

	public void setVintageorder(String[] vintageorder) {
		for (int i = 0; i < maxlabels; i++)
			if (vintageorder[i] == null)
				vintageorder[i] = "";
		this.vintageorder = vintageorder;
	}

	public String[] getUrlorder() {
		for (int i = 0; i < maxlabels; i++)
			if (urlorder[i] == null)
				urlorder[i] = "";
		return urlorder;
	}

	public void setUrlorder(String[] urlorder) {
		for (int i = 0; i < maxlabels; i++)
			if (urlorder[i] == null)
				urlorder[i] = "";
		this.urlorder = urlorder;
	}

	public String[] getPriceorder() {
		for (int i = 0; i < maxlabels; i++)
			if (priceorder[i] == null)
				priceorder[i] = "";
		return priceorder;
	}

	public void setPriceorder(String[] priceorder) {
		for (int i = 0; i < maxlabels; i++)
			if (priceorder[i] == null)
				priceorder[i] = "";
		this.priceorder = priceorder;
	}

	public String[] getSizeorder() {
		for (int i = 0; i < maxlabels; i++)
			if (sizeorder[i] == null)
				sizeorder[i] = "";
		return sizeorder;
	}

	public void setSizeorder(String[] sizeorder) {
		for (int i = 0; i < maxlabels; i++)
			if (sizeorder[i] == null)
				sizeorder[i] = "";
		this.sizeorder = sizeorder;
	}

	public boolean isAssume75cl() {
		return assume75cl;
	}

	public void setAssume75cl(boolean assume75cl) {
		this.assume75cl = assume75cl;
	}

	public void setHeaderrow(boolean headerrow) {
		this.headerrow = headerrow;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void setItemsep(String itemsep) {
		this.itemsep = itemsep;
	}

	public void setUniformfeed(String uniformfeed) {
		this.xmlfeed = uniformfeed;
	}

	public String getUniformFeed() {
		if (xmlfeed == null || xmlfeed.equals("")) {
			if (!getDelimiter().equals("")) {

			}
			if (xmlfeed == null)
				xmlfeed = feed;
		}
		if (xmlfeed == null)
			xmlfeed = "";
		return xmlfeed;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getItemsep() {
		if (itemsep == null && format != null) {
			if ("".equals(getDelimiter())) {
				switch (format) {
				case Atom03: {
					itemsep = "entry";
					break;
				}
				case Atom10: {
					itemsep = "entry";
					break;
				}
				case GoogleAtom10: {
					itemsep = "entry";
					break;
				}
				case GoogleAtom03: {
					itemsep = "entry";
					break;
				}

				}
			}
		}
		if (itemsep == null)
			itemsep = "item";
		return itemsep;
	}

	public String getUrl() {
		if (url == null)
			return "";
		return url;
	}

	public void setUrl(String url) {
		this.url = url.replaceAll(" ", "%20");
	}

	public void parseCSV() {
		try {
			Row row;
			feedContent = new FeedContent();
			CSVReader reader = new CSVReader(new java.io.StringReader(feed), delimiter.charAt(0), quotechar, 0);
			for (int i = 0; i < skiplines; i++)
				reader.readNext();
			for (String label : reader.readNext())
				labels.add(label);
			List<String[]> rows = reader.readAll();
			int linenumber = 0;
			for (String[] values : rows) {
				if (linenumber >= skiplines) {
					if (values.length > 2) {
						row = new Row();
						for (int j = 0; j < values.length; j++) {
							if (j < labels.size())
								row.put(labels.get(j), values[j]);
						}
						if (row.size() > 0)
							feedContent.add(row);
					}
				}
				linenumber++;
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: Could not parse CSV", e);
			feedstatus = feedstatusses.NOKParseError;
		}

	}

	public void parseExcel() {
		try {
			if (feedContent.size() == 0) {
				feedstatus = feedstatusses.NOKParseError;
			} else {
				int maxcolumns = 0;
				for (Row row : feedContent) {
					if (!labels.containsAll(row.keySet())) {
						for (String label : row.keySet()) {
							if (!labels.contains(label))
								labels.add(label);
						}
					}
				}
				Collections.sort(labels);
				feedstatus = feedstatusses.OK;
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: Could not parse Excel", e);
			feedstatus = feedstatusses.NOKParseError;
		}

	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void checkValues() {
		nameorder = getNameorder();
		vintageorder = getVintageorder();
		urlorder = getUrlorder();
		sizeorder = getSizeorder();
		priceorder = getPriceorder();
	}

	public Wine[] spider(int shopid, Timestamp now, Double pricefactorex, Double pricefactorin) {
		parse();
		return getWines(shopid, "", now, pricefactorex, pricefactorin, 99999, false).Wine;
	}

	public Wineset getWines(int shopid, String shopname, Timestamp now, Double pricefactorex, Double pricefactorin,
			int maxrows, boolean testrun) {
		Wineset wineset = new Wineset();
		wineset.Wine = new Wine[0];
		ArrayList<Wine> wines = new ArrayList<Wine>();
		if (now == null)
			now = new java.sql.Timestamp(new java.util.Date().getTime());
		if (shopid > 0)
			shopname = Webroutines.getShopNameFromShopId(shopid, "");

		if (feedContent != null) {

			checkValues();
			int rownum = 1;
			for (Row row : feedContent) {
				if (rownum < maxrows) {
					rownum++;
					String name = "";
					String url = "";
					int vintage = 0;
					float price = 0;
					float size = 0;
					for (int i = 0; i < maxlabels; i++) {
						if (row.get(nameorder[i]) != null && !nameorder[i].equals("")) {
							name = (name + " " + row.get(nameorder[i])).trim();
						}
						if (row.get(urlorder[i]) != null && url.equals("") && !urlorder[i].equals("")) {
							if (!Webroutines
									.getRegexPatternValue(urlregex, row.get(urlorder[i]), Pattern.CASE_INSENSITIVE)
									.equals("")) {
								if (urlregexexclude.equals("") || Webroutines.getRegexPatternValue(urlregexexclude,
										row.get(urlorder[i]), Pattern.CASE_INSENSITIVE).equals("")) {
									url = Webroutines.getRegexPatternValue(urlregex, row.get(urlorder[i]),
											Pattern.CASE_INSENSITIVE).replace("HTTP:", "https:");
								}
							}

						}

					}
					ArrayList<String> values;
					String order = "";
					values = new ArrayList<String>();
					values.add("0");
					order = "";
					for (int j = 0; j < maxlabels; j++) {
						if (!vintageorder[j].equals("")) {
							if (row.get(vintageorder[j]) != null) {
								values.add(row.get(vintageorder[j]));
								order += ";" + (j);
							}
						}
					}
					if (order.startsWith(";"))
						order = order.substring(1);

					try {
						vintage = Integer
								.parseInt(TableScraper.buildMatch(values, order, vintageregex, "", "", "Vintage"));
					} catch (Exception e) {
					}
					values = new ArrayList<String>();
					values.add("0");
					order = "";
					for (int j = 0; j < maxlabels; j++) {
						if (!priceorder[j].equals("")) {
							if (row.get(priceorder[j]) != null) {
								values.add(row.get(priceorder[j]));
								order += ";" + (j);
							}
						}
					}
					if (order.startsWith(";"))
						order = order.substring(1);
					price = TableScraper.getPrice(TableScraper.buildMatch(values, order, priceregex, "", "", "Price"));
					values = new ArrayList<String>();
					values.add("0");
					order = "";
					for (int j = 0; j < maxlabels; j++) {
						if (!sizeorder[j].equals("")) {
							if (row.get(sizeorder[j]) != null) {
								values.add(row.get(sizeorder[j]));
								order += ";" + (j);
							}
						}
					}
					if (order.startsWith(";"))
						order = order.substring(1);
					size = TableScraper.getSize(values, order, "", price, (!testrun && size == 0 && assume75cl));
					if (price > 1.5)
						wines.add(new Wine(name, vintage + "", size, price, (double) price * pricefactorex,
								(double) price * pricefactorin, (double) price, url, shopid, shopname, "",
								now.toString(), now.toString(), "", false, "", 0, null));
				}

			}
			wineset.Wine = new Wine[wines.size()];
			wineset.records = wines.size();
			int n = 0;
			for (Wine w : wines) {
				wineset.Wine[n] = w;
				n++;
			}
		}

		return wineset;

	}

	public String checkWineset(Wineset wineset) {
		String errors = "";
		wineswithsize = 0;
		if (wineset != null) {
			if (wineset.Wine == null || wineset.Wine.length == 0) {
				errors += "No wines with correct price were found. ";
			}
			for (Wine wine : wineset.Wine) {
				if (wine.Size > 0.1)
					wineswithsize++;
				// if (wine.SourceUrl.equals("")&&!errors.contains("url")) errors+="One or more
				// wines do not have a proper url to link to. ";
				if (wine.Name.equals("") && !errors.contains("name"))
					errors += "The wine name is missing for one or more wines. ";
				// if (wine.Price<0.1&&!errors.contains("price")) errors+="The price is missing
				// for one or more wines. ";
			}
		} else {
			errors += "No wines with correct price were found. ";
		}
		return errors;
	}

	public String getUrlStatusMessage() {
		String message = "";
		switch (urlstatus) {
		case CannotLoadURL:
			message += "The url you supplied cannot be reached"
					+ (responsecode > 0 ? " (we received error code " + responsecode + ")" : "") + ". <br/>";
			break;
		case MalformedURL:
			message += "The url you supplied is not a correct url"
					+ (!"".equals(urlproblem) ? " (" + urlproblem + ")" : "") + ".<br/>";
			break;
		case DuplicateURL:
			message += "This data feed already exists and can only be edited by the owner of the shop. To edit it you need an access code. This can be emailed to the email address that is currently registered for this shop ("
					+ new Shop(shopid).email
					+ "). If you are the owner of this shop and can access that email address, click <a href='/sendadminlink.jsp?shopid="
					+ shopid + "&amp;targetpage=analyzeelmar.jsp'>here</a> to send an email with the access code.<br/>";
		}
		return message;
	}

	public String getFeedStatusMessage() {
		String message = "";
		switch (feedstatus) {
		case NOKHTML:
			message += "Could it be this url points to a HTML (web) page? If this is correct and this is a complete listing of your wines in HTML, please enter your details <a href='/retailers.jsp'>here</a> and we will try to get the prices from this page. <br/><br/>If this is really a data feed, then it could be that our software did not recognize the content properly. If you think the data feed is OK, please leave your email address below. We will look at your feed and see if we can solve the problem.";
			break;
		case NOKUnrecognizedFormat:
			message += "Somehow the content of your data feed could not be read. This may not be a problem with the data feed: It could be that our software did not recognize the content properly. If you think the data feed is OK, please leave your email address below. We will look at your feed and see if we can solve the problem.";
			break;
		case NOKMalformedXML:
			message += "Your data feed looks like XML, but its syntax may not be 100% correct. Click <a href='"
					+ getUrl()
					+ "' target='_blank'>here</a> to open the feed in a web browser. If the browser complains about the format, the XML is incorrect. If it opens fine in a browser, the problem could be on our side. In that case, please leave your email address below and we will try to find out where the problem is.";
			break;
		case NOKParseError:
			message += "We think your data feed may be in " + format
					+ " format. But somehow we could not read it correctly. If you think the data feed is OK, please leave your email address below. We will look at your feed and see if we can solve the problem.";
			break;
		}

		return message;
	}

	public static void main(String[] a) {
		DataFeed datafeed = DataFeed.getDataFeed(new Context(1), 4413, 0);
		datafeed.encoding = "UTF-8";
		datafeed.save(new Context(1));
		/*
		 * DataFeed df=new
		 * DataFeed("http://hopsandgrapesonline.com/docs/GoogleBaseFeed.txt");
		 * df.readFeed(); df.parse(); df.getWines(0, "", null, (double)1, (double)1,
		 * 100, true); //Dbutil.logger.info(df.feed);
		 */

	}
}
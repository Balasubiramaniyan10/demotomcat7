package com.searchasaservice.parser.xpathparser;

import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.*;
import com.searchasaservice.ai.AiContentHandler;
//import com.searchasaservice.parser.HTMLParser;
import com.searchasaservice.parser.HTMLParser;
import com.searchasaservice.parser.LoboParser;


import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.xpath.XPathEvaluator;



import org.apache.xpath.NodeSet;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import javax.xml.xpath.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;



/**
 * @author Jasper
 *
 */
public class Analyzer extends XpathParser implements Serializable{

	private static final long serialVersionUID = 1L;
	public Document originaldocument=null;
	public Document taggeddocument=null;
	public Document workdocument=null;
	private ArrayList<String> paths=new ArrayList<String>(0);
	private static String[] colors={"#000000","#ff0000","#009900","#0000dd","#999900","#009999","#990099","#555555","#550000","#005500","#000055","#555500","#005555","#550055"};
	public String[] currentcolors=colors.clone();
	public String domainurl="";
	String urlpath="";
	String itemtagfilter="[local-name()!='del'][local-name()!='strike'][local-name()!='s'][local-name()!='body'][local-name()!='html'][local-name()!='head'][local-name()!='script']";
	public Result result=new Result();
	public String description;
	public String progress;


	public Analyzer(XpathParserConfig config) throws Exception{
		super(config);
	}

	public void setUrl(String urlstr){
		url=urlstr;

	}
	public void setPostdata(String postdata){
		this.postdata=postdata;

	}


	public static Analyzer getAnalyzer(Context c,int shopid, int row){
		if (row==0) row=Dbutil.readIntValueFromDB("select *,count(*) as thecount from xpathobjects where tenant="+c.tenant+" and shopid="+shopid+" and classname='com.searchasaservice.parser.xpathparser.XpathParserConfig' group by tenant,shopid having thecount=1;", "id");
		if (row>0) {

			try {
				Analyzer xp=new Analyzer((XpathParserConfig) Serializer
						.readJavaObject("xpathobjects",c.tenant, shopid, row));
				xp.id=row;
				xp.shopid=shopid;
				Dbutil.logger.info(xp.config.appendwineryname);
				return xp;
			} catch (Exception e) {
				Dbutil.logger.error("Problem retrieving xpathparser with id "+row+", shopid "+shopid+" for tenant "+c.tenant, e);
			}
		}
		return null;
	}


	public static XpathParserConfig makeConfig(String type) throws Exception{
		XpathParserConfig config=new XpathParserConfig();
		if (type.equals("Wine")){
			config.add(new ConfigField("Item"));
			config.add(new ConfigField(
					ConfigField.Defaulttype.PRICE, "Price"));
			ConfigField name = new ConfigField("Name");
			name.setContentHandler(new AiContentHandler(1,4));
			//name.mustcontainvalue=true;
			config.add(name);
			config.add(new ConfigField(
					ConfigField.Defaulttype.VINTAGE, "Vintage"));
			ConfigField region = new ConfigField("Region");
			region.setContentHandler(new AiContentHandler(1,2));
			config.add(region);
			ConfigField producer = new ConfigField("Producer");
			producer.setContentHandler(new AiContentHandler(1,1));
			//producer.mustcontainvalue=true;
			config.add(producer);
			config.add(new ConfigField(
					ConfigField.Defaulttype.URL, "Url"));
			config.add(new ConfigField(ConfigField.Defaulttype.BOTTLESIZE,"Bottlesize"));
			return config;
		}
		return null;
	}



	public static ArrayList<Integer> getXpathParserIds(Context c,int shopid){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		ArrayList<Integer>ids=new ArrayList<Integer>();

		try {
			rs=Dbutil.selectQuery("select * from xpathobjects where tenant="+c.tenant+" and shopid="+shopid+" and classname='com.searchasaservice.parser.xpathparser.XpathParserConfig';", con);
			while (rs.next()){
				ids.add(rs.getInt("id"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return ids;


	}


	public void setNewPage(Document document,String urlstr) throws Exception{
		getUrls(urlstr);
		if (init()) {
			this.originaldocument=(Document)document.cloneNode(true);
			tagDocument(originaldocument);
			fixLinks(domainurl, urlpath);
		}
	}


	public Document getOriginalDocument(){
		return (Document)originaldocument.cloneNode(true);
	}
	private void getUrls(String urlstr){
		if (urlstr!=null&&!urlstr.equals("")){
			if (urlstr.indexOf("/", 9)>0) {
				domainurl=urlstr.substring(0, urlstr.indexOf("/", 9));
			} else {
				domainurl=urlstr;
			}

			urlpath=urlstr.substring(0, urlstr.lastIndexOf("/"));
			if (domainurl.length()>urlpath.length()) urlpath=domainurl;
		}
	}

	private boolean init(){
		boolean isok=true;
		try {
			for (int i=1;i<config.size();i++) {
				if (isok&&!"".equals(config.get(i).init())) {
					isok=false;
					Dbutil.logger.info("Could not initialize field "+i+" ("+config.get(i).label+"): "+config.get(i).init());
				}

			}
			config.get(0).isItemField=true;
			if (isok) if (!"".equals(config.get(0).init())) {
				isok=false;
				Dbutil.logger.info("Could not initialize field "+0+" ("+config.get(0).label+"): "+config.get(0).init());
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			isok=false;
		}
		return isok;
	}

	private ConfigField f(int index){
		return (ConfigField) config.get(index);
	}

	public void tagDocument(Document document){
		try {
			taggeddocument=(Document) document.cloneNode(true);
			XPathEvaluator xpe=new XPathEvaluator();
			DOMNodeList ni = (DOMNodeList)xpe.evaluate("//element()",taggeddocument, XPathConstants.NODESET);
			Node n;
			for (int i=0;i<ni.getLength();i++){
				n=ni.item(i);
				((Element) n).setAttribute("fwscounter", String.valueOf(i));

			}
			workdocument=taggeddocument;
			fixLinks(domainurl, urlpath);
			

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}


	}



	public String getTaggedDocumentAsString(){
		return getNodeAsXML(taggeddocument);
	}




	public String getHighlightedHTML(String xpath){
		Node hl=null;

		Node body=null;
		try {
			body=XPathAPI.selectSingleNode(taggeddocument, "//body");
			if (xpath.equals("")){
				Dbutil.logger.info("Empty xpath expression received, cannot produce new HTML version");

			} else {
				hl=taggeddocument.cloneNode(true);

				NodeIterator ni = XPathAPI.selectNodeIterator(hl, xpath);
				Node n;
				while ((n = (Node)ni.nextNode())!=null){
					Element e=null;
					try {
						e=(Element)n;
					}catch (Exception ex){}
					if (e!=null){
						e.setAttribute("fwsmarked", "true");
						String oldstyle=e.getAttribute("style");
						if (!"".equals(oldstyle)) e.setAttribute("oldstyle",e.getAttribute("style"));
						e.setAttribute("style", oldstyle+"border-color:red;border-style:solid;border-width:2px;");
					}
				}
				body=XPathAPI.selectSingleNode(hl, "//body");
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		//Dbutil.logger.info(getNodeAsXML(body));

		return getNodeAsXML(body);
	}

	private void highlightNode(Node n, String color){
		try {
			Element e=null;
			try {
				e=(Element)n;
			}catch (Exception ex){}
			if (e!=null){
				String style=e.getAttribute("style");
				if (style.contains("border-color:")){
					style=style.replaceAll("border-color:[^;]+;", "border-color:"+color+";");
				} else {
					style+=";border-color:"+color+";";
				}
				if (style.contains("border-style:")){
					style=style.replaceAll("border-style:[^;]+;", "border-style:solid;");
				} else {
					style+=";border-style:solid;";
				}
				if (style.contains("border-width:")){
					style=style.replaceAll("border-width:[^;]+;", "border-width:2px;");
				} else {
					style+= ";border-width:2px;";
				}
				e.setAttribute("style", style);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		//Dbutil.logger.info(getNodeAsXML(body));
	}



	public static String getNodeAsXML(Node n){
		String xmlString="";
		try {
			// Set up the output transformer
			System.setProperty("javax.xml.transform.TransformerFactory",
			"org.apache.xalan.processor.TransformerFactoryImpl");
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(n);
			trans.transform(source, result);
			xmlString = sw.toString();

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return xmlString;
	}
/*

	private static String getNodeListAsString(NodeList n){
		StringBuffer content=new StringBuffer();
		try {

			for (int i=0;i<n.getLength();i++){
				// Get element
				content.append(getNodeAsXML(n.item(i)));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return content.toString();
	}

	/**
	 * Finds a node from an xpath expression;
	 * Takes into account that Mozilla can add ghost nodes and skips them
	 * 
	 * @param xpath
	 * 			"Dirty" path from Mozilla
	 * @return String: cleaned path
	 * 			
	 
	private String cleanMozillaXpath(String mxpath){
		String xpath="";
		String[] elements=mxpath.split("/");
		String previous="";
		Node n=(Node)workdocument;

		try{
			NodeList nl; 

			for (String el:elements){
				if (el.equals("")) {
					previous=(previous+"/").replaceAll("///+", "//");
				} else {
					el=previous+el;
					previous="/";

					nl = XPathAPI.selectNodeList(workdocument,(xpath+el).toLowerCase());

					if (nl.getLength()==0){
						// Skip node
						Dbutil.logger.info("Skipped:"+el);
						if (el.startsWith("//")) previous="//";
					} else {
						// Found the node, add it
						xpath+=el.toLowerCase();
					}

				}
			}

		} catch(Exception e){
			Dbutil.logger.info("Could not find node",e);
		}
		Dbutil.logger.info("Mozilla path "+mxpath+" became "+xpath);
		return xpath;
	}
*/
	/**
	 * Get the minimum xpath that gives the same nodes as the xpath to analyze
	 * 
	 * @param xpath
	 * 			Original xpath
	 * @return String: cleaned path
	 * 			
	 */
	private String getMinimizedPath(String origxpath){
		String[] elements=origxpath.split("/");
		String[] newelements=origxpath.split("/");
		String newpath="";
		try{
			NodeList nl; 
			NodeList nlorig=XPathAPI.selectNodeList(workdocument,(origxpath)); 

			for (int i=1;i<elements.length-1;i++){
				newelements[i]="";
				newpath="";
				for (int j=1;j<elements.length;j++)	newpath+="/"+newelements[j];
				newpath=newpath.replaceAll("//+", "//");
				nl = XPathAPI.selectNodeList(workdocument,(newpath).toLowerCase());
				boolean thesame=true;
				if (nl.getLength()==nlorig.getLength()){
					for (int j=0;j<nl.getLength();j++){
						if (!nl.item(j).isSameNode(nlorig.item(j))){
							thesame=false;
							Dbutil.logger.info("Different nodes:");
							Dbutil.logger.info(getNodeAsXML(nl.item(j)));
							Dbutil.logger.info(getNodeAsXML(nlorig.item(j)));
							Dbutil.logger.info(nl.item(j).getTextContent());
							Dbutil.logger.info(nlorig.item(j).getTextContent());
							break;
						}
					}
				} else thesame=false;
				if (thesame){
					elements=newelements.clone();
				} else {
					// Different nodeset, the element was necessary
					for (int j=0;j<nl.getLength();j++){
						//Dbutil.logger.info(getNodeAsXML(nl.item(j)));
						//Dbutil.logger.info(nl.item(j).getTextContent());
					}

				}
				newelements=elements.clone();

			}

		} catch(Exception e){
			Dbutil.logger.info("Could not find node. newpath="+newpath+".");
		}
		newpath="";
		for (int j=1;j<elements.length;j++)	newpath+="/"+elements[j];
		newpath=newpath.replaceAll("//+", "//");

		return newpath;
	}


	/**
	 * Get the minimum xpath that gives the same nodes as the xpath to analyze,
	 * relative to a certain set of nodes. If the original path did not find any nodes,
	 * leave it out of the analysis. That way, we can get a more general xpath
	 * which may find extra nodes even if the hierarchy is a bit different for a certain node.
	 * @deprecated Use getMinimizedRelativePath(String,String) instead (100x faster)
	 * @param xpath
	 * 			Original xpath
	 * @return String: cleaned path
	 * 			
	 
	private String getMinimizedRelativePath(String origxpath, NodeSet ns){
		String xpath="";
		String[] elements=origxpath.split("/");
		String[] newelements=origxpath.split("/");
		String previous="";
		String newpath="";
		try{
			NodeList nl; 
			NodeList nlorig;
			for (int i=0;i<elements.length-1;i++){
				//Dbutil.logger.info(newelements[i]);
				boolean thesame=true;
				newelements[i]="";
				if (i==0) newelements[0]="self::*";
				newpath=newelements[0];
				for (int j=1;j<elements.length;j++)	newpath+="/"+newelements[j];
				newpath=newpath.replaceAll("//+", "//");

				for (int k=0;k<ns.getLength();k++){
					nlorig=XPathAPI.selectNodeList(ns.item(k),(origxpath)); 
					nl = XPathAPI.selectNodeList(ns.item(k),(newpath));
					if (nlorig.getLength()>0){
						if (nl.getLength()==nlorig.getLength()){
							for (int j=0;j<nl.getLength();j++){
								if (!nl.item(j).isSameNode(nlorig.item(j))){
									thesame=false;
									k=9999;
									j=9999;
									Dbutil.logger.info("Different nodes:");
									Dbutil.logger.info(getNodeAsXML(nl.item(j)));
									Dbutil.logger.info(getNodeAsXML(nlorig.item(j)));
									Dbutil.logger.info(nl.item(j).getTextContent());
									Dbutil.logger.info(nlorig.item(j).getTextContent());
									break;
								}
							}
						} else {
							thesame=false;
							break;
						}
					}
				}
				if (thesame){
					elements=newelements.clone();
				} else {
					newelements=elements.clone();
				}
			}

		} catch(Exception e){
			Dbutil.logger.info("Could not find node. newpath="+newpath,e);
		}
		newpath=elements[0];
		for (int j=1;j<elements.length;j++)	newpath+="/"+elements[j];
		newpath=newpath.replaceAll("//+", "//");

		//Dbutil.logger.info("Original path "+origxpath+" became "+newpath);
		return newpath;
	}
	*/
	/**
	 * Get the minimum xpath that gives the same nodes as the xpath to analyze,
	 * relative to a certain set of nodes. If the original path did not find any nodes,
	 * leave it out of the analysis. That way, we can get a more general xpath
	 * which may find extra nodes even if the hierarchy is a bit different for a certain node.
	 * 
	 * @param xpath
	 * 			Original xpath
	 * @return String: cleaned path
	 * 			
	 */
	private String getMinimizedRelativePath(String origxpath, String itemxpath){
		//if(!origxpath.startsWith("/")) 
		if(origxpath.trim().equals("")){
			return origxpath;
		} 
		itemxpath+="/";
		String[] elements=origxpath.split("/");
		String[] newelements=origxpath.split("/");
		String newpath="";
		try{
			NodeList nl; 
			NodeList nlorig=XPathAPI.selectNodeList(document,(itemxpath+origxpath));
			if (nlorig.getLength()>0){
				for (int i=0;i<elements.length-1;i++){
					//Dbutil.logger.info(newelements[i]);
					boolean thesame=true;
					newelements[i]="";
					if (i==0) newelements[0]="self::*";
					newpath=newelements[0];
					for (int j=1;j<elements.length;j++)	newpath+="/"+newelements[j];
					newpath=newpath.replaceAll("//+", "//");
					nl = XPathAPI.selectNodeList(document,(itemxpath+newpath));
					if (nl.getLength()==nlorig.getLength()){
						for (int j=0;j<nl.getLength();j++){
							if (!nl.item(j).isSameNode(nlorig.item(j))){
								thesame=false;
								j=9999;
								Dbutil.logger.info("Different nodes:");
								Dbutil.logger.info(getNodeAsXML(nl.item(j)));
								Dbutil.logger.info(getNodeAsXML(nlorig.item(j)));
								Dbutil.logger.info(nl.item(j).getTextContent());
								Dbutil.logger.info(nlorig.item(j).getTextContent());
								break;
							}
						}
					} else {
						thesame=false;
						break;
					}
					if (thesame){
						elements=newelements.clone();
					} else {
						newelements=elements.clone();
					}
				}
			} else {
				Dbutil.logger.info("Could not find any items with the original path! "+itemxpath+" "+origxpath);
			}



		} catch(Exception e){
			Dbutil.logger.info("Could not find node. newpath="+newpath,e);
		}
		newpath=elements[0];
		for (int j=1;j<elements.length;j++)	newpath+="/"+elements[j];
		newpath=newpath.replaceAll("//+", "//");

		return newpath;
	}


	/**
	 * Get the minimum xpath that gives the same nodes as the xpath to analyze,
	 * relative to a specific node. If the original path did not find any nodes,
	 * return the original value. That way, we can get a more general xpath
	 * which may find extra nodes even if the hierarchy is a bit different for a certain node.
	 * 
	 * @param xpath
	 * 			Original xpath
	 * @return String: cleaned path
	 * 			
	 
	private String getMinimizedRelativePath(String origxpath, Node n){
		String xpath="";
		String[] elements=origxpath.split("/");
		String[] newelements=origxpath.split("/");
		String previous="";
		String newpath="";
		try{
			NodeList nl; 

			for (int i=0;i<elements.length-1;i++){
				//Dbutil.logger.info(newelements[i]);
				boolean thesame=true;
				NodeList nlorig=XPathAPI.selectNodeList(n,(origxpath)); 
				if (nlorig.getLength()>0){
					newelements[i]="";
					if (i==0) newelements[0]="child::*";
					newpath=newelements[0];
					for (int j=1;j<elements.length;j++)	newpath+="/"+newelements[j];
					newpath=newpath.replaceAll("//+", "//");
					nl = XPathAPI.selectNodeList(n,(newpath));
					if (nl.getLength()==nlorig.getLength()){
						for (int j=0;j<nl.getLength();j++){
							if (!nl.item(j).isSameNode(nlorig.item(j))){
								thesame=false;
								j=9999;
								Dbutil.logger.info("Different nodes:");
								Dbutil.logger.info(getNodeAsXML(nl.item(j)));
								Dbutil.logger.info(getNodeAsXML(nlorig.item(j)));
								Dbutil.logger.info(nl.item(j).getTextContent());
								Dbutil.logger.info(nlorig.item(j).getTextContent());
								break;
							}
						}
					} else thesame=false;

				}
				if (thesame){
					elements=newelements.clone();
				}
				newelements=elements.clone();

			}

		} catch(Exception e){
			Dbutil.logger.info("Could not find node. newpath="+newpath,e);
		}
		newpath=elements[0];
		for (int j=1;j<elements.length;j++)	newpath+="/"+elements[j];
		newpath=newpath.replaceAll("//+", "//");

		//Dbutil.logger.info("Original path "+origxpath+" became "+newpath);
		return newpath;
	}

*/

	private Node getNodeFromTag(String tag){
		Node n=null;
		if (!tag.equals("")){
			try {
				n = XPathAPI.selectSingleNode(taggeddocument, "//*[@fwscounter='"+tag+"']");
			} catch (Exception e) {
				//Dbutil.logger.error("Problem: ", e);
			}
		}
		return n;
	}
	private Node getNodeFromTag(CachedXPathAPI cxapi,String tag){
		Node n=null;
		if (!tag.equals("")){
			try {
				n = cxapi.selectSingleNode(taggeddocument, "//*[@fwscounter='"+tag+"']");
			} catch (Exception e) {
				//Dbutil.logger.error("Problem: ", e);
			}
		}
		return n;
	}

	public Node getNodeFromxpath(String xpath){
		Node n=null;
		try {
			n = XPathAPI.selectSingleNode(taggeddocument, xpath);
		} catch (Exception e) {
			Dbutil.logger.error("Problem with xpath "+xpath, e);
		}
		return n;
	}
	public Node getNodeFromxpath(CachedXPathAPI cxapi,String xpath){
		Node n=null;
		try {
			n = cxapi.selectSingleNode(taggeddocument, xpath);
		} catch (Exception e) {
			Dbutil.logger.error("Problem with xpath "+xpath, e);
		}
		return n;
	}

	private NodeSet getNodeSetFromxpath(String xpath){
		NodeSet n=new NodeSet();
		if (""!=xpath){
			DOMNodeList nl;
			try {
				XPathEvaluator xpeval=new XPathEvaluator();
				XPathExpression exp=xpeval.compile(xpath);
				nl=(DOMNodeList)exp.evaluate(workdocument,XPathConstants.NODESET);
				for (int i=0;i<nl.getLength();i++) {
					n.addNode(nl.item(i));
				}

			} catch (Exception e) {
				Dbutil.logger.info("Problem with xpath "+xpath,e);
			}
		}
		return n;
	}

/*
	private static Node getNodeFromRelXpath(String xpath, Node ref){
		Node n=null;
		if(!"".equals(xpath)){
			try {
				CachedXPathAPI cxapi=new CachedXPathAPI();
				n = cxapi.selectSingleNode(ref, xpath);
			} catch (Exception e) {
				Dbutil.logger.error("Problem with xpath "+xpath, e);
			}
		}
		return n;
	}
	private static Node getNodeFromRelXpath(CachedXPathAPI cxapi,String xpath, Node ref){
		Node n=null;
		if(!"".equals(xpath)){
			try {
				n = cxapi.selectSingleNode(ref, xpath);
			} catch (Exception e) {
				Dbutil.logger.error("Problem with xpath "+xpath, e);
			}
		}
		return n;
	}

	*/
	private String getSeriesPath(String[] args){
		ArrayList<String> paths=new ArrayList<String>(0);
		if (args.length==0){
			paths=this.paths;
		} 
		if (args.length==2){
			paths.add(args[0]);
			paths.add(args[1]);
		}
		String resultpath="";
		if (paths.size()!=2){
			Dbutil.logger.warn("Paths length is "+paths.size()+" instead of 2.");
		} else {
			String[] path0=paths.get(0).split("/");
			String[] path1=paths.get(1).split("/");
			boolean problem=false;
			if (path0.length!=path1.length){
				problem=true;
			}
			if (!problem) for (int i=0;i<path0.length;i++){
				if (path0[i].equals(path1[i])){
					// Exactly the same
					resultpath+="/"+path0[i];
				} else {
					if (path0[i].replaceAll("\\[\\d+\\]","").equals(path1[i].replaceAll("\\[\\d+\\]",""))){
						// The series is in this element
						resultpath+="/"+path0[i].replaceAll("\\[\\d+\\]","[*]");
					} else if (path0[i].replaceAll("\\[\\d+\\]","").replaceAll("\\[@class[^\\]]+\\]","").equals(path1[i].replaceAll("\\[\\d+\\]","").replaceAll("\\[@class[^\\]]+\\]",""))){
						// the difference is in class name, remove class
						resultpath+="/"+path0[i].replaceAll("\\[\\d+\\]","[*]").replaceAll("\\[@class[^\\]]+\\]","");
					} else {
						// Hmmm. We found a real difference. Let's skip this element
						resultpath="";
						problem=true;
					}
				}
			}
			if(problem) resultpath="";
			if (resultpath.length()>0){
				resultpath=resultpath.substring(1,resultpath.length());
				resultpath=resultpath.replaceAll("///+","//");
			}			

		}
		while (resultpath.endsWith("/")) {
			resultpath=resultpath.substring(0,resultpath.length()-1);
		}
		return resultpath;
	}

	public void OBSOLETEaddPath(String counter){

		paths.add(getXpath((Element)getNodeFromTag(counter),""));

	}

	public void clearPaths(){
		paths=new ArrayList<String>(0);

	}

	/**
	 * Get the xpath from the root node to an element
	 * @param node: the element to find
	 * @param path: leave this empty (""), this parameter is used because it is a recursive method.
	 * @return xpath
	 * @throws Exception 
	 */
	private String getXpath(Element node, String path) {
		if (node!=null&&node.getNodeType()!=Node.DOCUMENT_NODE){

			try {

				String id = "";
				if (node.getAttribute("id") != null&&!node.getAttribute("id").equals(""))
					id = "[@id='" + node.getAttribute("id") + "']";
				String classname = "";
				if (node.getAttribute("class") != null&&!node.getAttribute("class").equals(""))
					classname = "[@class='" + node.getAttribute("class") + "']";
				String nodename=node.getNodeName();
				if (nodename.contains(":")) nodename="*[local-name()='"+nodename.split(":")[1]+"']";
				String reg="child::" + nodename + id + classname;
				String fwscounter="";
				if (node.getAttribute("fwscounter") != null&&node.getAttribute("fwscounter")!="")
					fwscounter = node.getAttribute("fwscounter");

				XPathFactory xpf=XPathFactory.newInstance();
				XPath xpe=xpf.newXPath();
				//Dbutil.logger.info(node.getClass().getCanonicalName());
				//reg=nodename+ id + classname;
				NodeList nl=(NodeList)xpe.evaluate(reg,node.getParentNode(),XPathConstants.NODESET);
				int n = 0;
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).isSameNode(node))
						n = i + 1;
				}
				if (n == 0) {
					Dbutil.logger.info("Checked "+nl.getLength()+" nodes but could not find node with fwscounter="+fwscounter+"! Parentnode has "
							+ nl.getLength() + " nodes that match "
							+ node.getNodeName() + id + classname);
					Dbutil.logger.info("Regex="+reg);
					Dbutil.logger.info("Parent:"+node.getParentNode());
					if (true||reg.equals("child::p")){
						nl=(NodeList)xpe.evaluate("child::*",node.getParentNode(),XPathConstants.NODESET);
						for (int i = 0; i < nl.getLength(); i++) {
							Dbutil.logger.info("child="+nl.item(i));
							
						}
					}
				}
				String order = "[" + n + "]";
				path = "/" + nodename + id + classname + order + path;
				//alert(node.nodeName);
				Element parent=null;
				try{
					parent=(Element)node.getParentNode();
				} catch (Exception e){}
				path = getXpath(parent, path);
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);

			}
		}
		return path;
	}

	public String newPath(String nodetag, int field){

		Result newresult=new Result();
		int nodeint=0;
		try{nodeint=Integer.parseInt(nodetag);}catch(Exception e){}
		if (field>0&&nodeint>0){
			f(field).xpath = "";
			int itemnodetag=0;
			for (int i=0;i<result.size();i++){
				if (result.get(i).get(0).fwscounter>itemnodetag&&result.get(i).get(0).fwscounter<=nodeint) {
					itemnodetag=result.get(i).get(0).fwscounter;
				}
			}
			if (itemnodetag==0){
				f(field).xpath="";
			} else {
				Node newnode=getNodeFromTag(nodeint+"");
				Node itemnode=getNodeFromTag(itemnodetag+"");
				f(field).xpath=getRelXpath((Element)newnode,itemnode);
				Dbutil.logger.info(f(field).xpath);
				tagDocument(originaldocument);
				try {
					newresult=parse();
				} catch (Exception e) {
					Dbutil.logger.error("Problem: ", e);
				}
			}
			setColors();
			for (int i=0;i<newresult.size();i++){
				highlightNode(getNodeFromTag(newresult.get(i).get(field).fwscounter+""),currentcolors[field]);
			}
		}
		return getNodeAsXML(taggeddocument);


	}


	/**
	 * Recursive function that determines the relative path from node to refnode
	 * @param node: node to lookup
	 * @param refnode: ancestor node
	 * @return: relative path from refnode to node
	 */
	private String getRelXpath(Element node, String itempath){
		XPathEvaluator xpeval=new XPathEvaluator();
		try {
			XPathExpression exp = xpeval.compile(itempath);
			NodeList ni = (NodeList) exp.evaluate(workdocument,
					XPathConstants.NODESET);
			for (int i = 0; i < ni.getLength(); i++) {
				if (ni.item(i).compareDocumentPosition(node)==20) {
					return getRelXpath(node, ni.item(i));
				} 
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			Dbutil.logger.error("Workdocument: "+workdocument.getClass());
			Dbutil.logger.error("itempath: "+itempath);


		}
		Dbutil.logger.info("Could not find node "+getXpath(node, "")+" which should have been relative to "+itempath);
		return "";
	}



	/**
	 * Recursive function that determines the relative path from node to refnode
	 * @param node: node to lookup
	 * @param refnode: ancestor node
	 * @return: relative path from refnode to node
	 */
	private String getRelXpath(Element node, Node refnode){
		String path=getRelXpath(node,refnode,"");
		if (path!=null&&path.length()>0){
			path=path.substring(1);
		}
		return path;
	}


	/**
	 * Recursive function that determines the relative path from node to refnode
	 * Should not be called directly, only through getRelXpath(node,refnode)!
	 * @param node: node to lookup
	 * @param refnode: ancestor node
	 * @param path: input for recursion
	 * @return: relative path including trailing /
	 */
	private String getRelXpath(Element node, Node refnode, String path){
		if (node!=null&&!node.isSameNode(refnode)){

			try {

				String id = "";
				if (node.getAttribute("id") != null&&node.getAttribute("id")!="")
					id = "[@id='" + node.getAttribute("id") + "']";
				String classname = "";
				if (node.getAttribute("class") != null&&node.getAttribute("class") != "")
					classname = "[@class='" + node.getAttribute("class") + "']";
				String reg="child::" + node.getNodeName() + id + classname;
				XPathFactory xpf=XPathFactory.newInstance();
				XPath xpe=xpf.newXPath();
				reg="*[local-name() = '"+node.getNodeName()+"']" + id + classname;
				NodeList nl=(NodeList)xpe.evaluate(reg,node.getParentNode(),XPathConstants.NODESET);
				int n = 0;
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).isSameNode(node))
						n = i + 1;
				}
				
				String order = "[" + n + "]";
				path = "/" + node.getNodeName() + id + classname + order + path;
				if (n == 0) {
					Dbutil.logger.info("Could not find node! Parentnode has "
							+ nl.getLength() + " nodes that match "
							+ node.getNodeName() + id + classname);
					Dbutil.logger.info("reg:"+reg);
					//path=null;
					//Dbutil.logger.info("Parent node XML:"+getNodeAsXML(node.getParentNode()));
				}//alert(node.nodeName);
				Element parent=null;
				try{
					parent=(Element)node.getParentNode();
				} catch (Exception e){}
				path = getRelXpath(parent, refnode,path);
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);

			}
		} else {
			if ("".equals(path)) path=" self";
		}
		
		return path;
	}

/*
	/**
	 * Finds all text elements whose text match a regex and returns their xpath
	 * @param regex: a regular expression to match.
	 * Include ^ and $ signs if/when needed 
	 * @return String[] xpaths: the xpaths of all elements that match
	 
	private String[] getXpathsFromRegex(Node n, String regex){
		regex="descendant::element()[child::text()[matches(.,\""+regex+"\")]]";
		String[] result=null;
		try {
			XPathEvaluator xpeval=new XPathEvaluator();
			XPathExpression exp=xpeval.compile(regex);
			NodeList ni=(NodeList)exp.evaluate(n,XPathConstants.NODESET);
			result=new String[ni.getLength()];
			for (int i=0;i<ni.getLength();i++){
				//Add non-empty nodes
				if (!"".equals(ni.item(i).getTextContent().trim()))	result[i]=getXpath((Element)ni.item(i),"");
				//result[i]=ni.item(i).getNodeName();
			}


		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return result;
	}

	private String[] getItemXpathsFromNodeRegex(String regexfilter, String nodenamefilter){
		//regex="descendant::element()[count(descendant::text()[matches(.,\""+regex+"\")])=1][parent::*[count(descendant::text()[matches(.,\""+regex+"\")])>1]]";
		String regex="descendant::element()[count(descendant::text()"+regexfilter+"[parent::*"+nodenamefilter+"])<=2][parent::*[count(descendant::element()[child::text()"+regexfilter+"[parent::*"+nodenamefilter+"]])>2]]";

		ArrayList<String> result=new ArrayList<String>();
		try {
			XPathEvaluator xpeval=new XPathEvaluator();
			XPathExpression exp=xpeval.compile(regex);
			NodeList ni=(NodeList)exp.evaluate(workdocument,XPathConstants.NODESET);
			for (int i=0;i<ni.getLength();i++){
				//Add non-empty nodes that are not options in a select box
				if (!"".equals(ni.item(i).getTextContent().trim())&&!"option".equals(ni.item(i).getLocalName()))	{
					result.add(getXpath((Element)ni.item(i),""));
					//Dbutil.logger.info(ni.item(i).getNodeName()+": "+ni.item(i).getTextContent());
					//Dbutil.logger.info(getNodeAsXML(ni.item(i)));
				}
				//result[i]=ni.item(i).getNodeName();
			}


		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return ArrayList2StringArray(result);
	}

*/

	
	private Set<Node> getAllTextNodes(Node n,String filter){
		HashMap<Node,String> map =new HashMap<Node,String>();
		String regex="descendant::element()"+filter+"[child::text()[matches(.,\"\\S\\S\\S\")]]";
		String nodetext;
		try {
			XPathEvaluator xpeval=new XPathEvaluator();
			XPathExpression exp=xpeval.compile(regex);
			NodeList ni=(NodeList)exp.evaluate(n,XPathConstants.NODESET);
			for (int i=0;i<ni.getLength();i++){
				//Add non-empty nodes
				//Dbutil.logger.info(ni.item(i).getNodeName()+":"+ni.item(i).getTextContent());

				if (!(nodetext=ni.item(i).getTextContent().trim()).equals(""))	{
					map.put(ni.item(i), nodetext);
					//				if (relative){
					//					map.put(getRelXpath((Element)ni.item(i), n), ni.item(i).getTextContent());
					//				} else {
					//					map.put(getXpath((Element)ni.item(i), ""), ni.item(i).getTextContent());
					//				}
				} else {
					//Dbutil.logger.info("trimmed:"+ni.item(i).getTextContent().trim()+" gives "+!"".equals(ni.item(i).getTextContent().trim()));

				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return map.keySet();
	}


	/**
	 * Determines the xpath that retrieves the most nodes whose xpath's is given 
	 * @param xpaths: the xpaths to match
	 * @return: the generated best xpath
	 */
	private String getBestXpath(String[] xpaths){
		String[] paths=xpaths.clone();
		for (int i=0;i<paths.length;i++) paths[i]=paths[i].replaceAll("\\[[^\\]]*]", "");
		Map<String, Integer> m = new HashMap<String, Integer>();

		// Determine the most common path just by element name 
		for (String a : paths) {
			Integer freq = m.get(a);
			m.put(a, (freq == null) ? 1 : freq + 1);
		}
		String bestelpath="";
		Integer freq=0;
		for (String path:m.keySet()){
			if (m.get(path)>freq){
				freq=m.get(path);
				bestelpath=path;
			}
		}
		// Bestelpath now contains the best candidate
		// See which xpaths match the candidate
		ArrayList<String> candidates=new ArrayList<String>(0);
		for (int i=0;i<xpaths.length;i++) {
			if (xpaths[i].replaceAll("\\[[^\\]]*]", "").equals(bestelpath)) candidates.add(xpaths[i]);
		}
		// Candidates contains all xpaths that are relevant
		// Now compare each two candidates, and make a map of the resulting xpath
		m = new HashMap<String, Integer>();
		String[] testpaths=new String[2];
		for (int i=0;i<candidates.size()-1;i++){
			for (int j=i+1;j<candidates.size();j++){
				testpaths[0]=candidates.get(i);
				testpaths[1]=candidates.get(j);
				String a=getSeriesPath(testpaths);
				if(!"".equals(a)){
					freq = m.get(a);
					m.put(a, (freq == null) ? 1 : freq + 1);
				}
			}
		}
		bestelpath="";
		freq=0;
		for (String path:m.keySet()){
			if (m.get(path)>freq){
				freq=m.get(path);
				bestelpath=path;
			}
		}
		//Dbutil.logger.info("Best shot:"+bestelpath);
		return bestelpath;
	}

	/**
	 * From a set of relative xpaths, this routine determines the relative paths with the highest frequency.
	 * @param xpaths: all candidates
	 * @return : the most common relative path
	 */
	private ArrayList<String> getBestRelXpath(String[] xpaths){
		ArrayList<String> bestpaths=new ArrayList<String>(0);
		String[] paths=xpaths.clone();
		Map<String, Integer> m = new HashMap<String, Integer>();

		// Determine the most common path 
		for (String a : paths) {
			Integer freq = m.get(a);
			m.put(a, (freq == null) ? 1 : freq + 1);
		}
		Integer freq=0;
		for (String path:m.keySet()){
			if (m.get(path)>freq){
				bestpaths=new ArrayList<String>(0);
				bestpaths.add(path);
				freq=m.get(path);
			} else if (m.get(path)==freq){
				bestpaths.add(path);

			}


		}
		// Bestpaths now contains the best candidates
		return bestpaths;
	}

/*



	private String getTextFromXpath(String xpath){
		String result="";
		try {
			XPathEvaluator xpeval = new XPathEvaluator();
			XPathExpression exp = xpeval.compile(xpath);
			NodeList ni = (NodeList) exp.evaluate(workdocument,	XPathConstants.NODESET);
			for (int i=0;i<ni.getLength();i++){
				result+=ni.item(i).getTextContent().trim()+"\n";
				//result[i]=ni.item(i).getNodeName();
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return result;
	}


	private String getItemPath(NodeSet pricenodes,String pricepath){
		int hops=0;
		String itempath=pricepath;

		try {
			boolean ready=false;
			while(!ready){
				String newitempath=itempath.substring(0, itempath.lastIndexOf("/"));
				if (newitempath.endsWith("/")) newitempath=newitempath.substring(0, newitempath.length()-1);
				NodeSet ns=getNodeSetFromxpath(newitempath);
				for (int i=0;i<ns.getLength();i++){
					int nodesfound=0;
					for (int j=0;j<pricenodes.getLength();j++){
						Dbutil.logger.info(ns.item(i).compareDocumentPosition(pricenodes.item(j)));

						if (ns.item(i).compareDocumentPosition(pricenodes.item(j))==20) {
							nodesfound++;
							if (nodesfound>1){
								ready=true;
								j=9999;
								i=9999;
							}
						}
					}
				}
				if(!ready){
					itempath=newitempath;
				}
			}

		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return itempath;
	}



	private static NodeSet nodeList2NodeSet(NodeList nl){
		NodeSet ns=new NodeSet();
		for (int i=0;i<nl.getLength();i++) ns.addNode(nl.item(i));
		return ns;
	}

	/**
	 * Retrieves a nodeset where every node is the highest node that contains exactly 1 node matches by parameter xpath
	 * @param xpath: an xpath that points to a number of nodes
	 * @return the resulting nodeset
	 * @deprecated
	 
	private NodeSet OBSOLETEgetItemNodeSet(String xpath){
		NodeSet nodeSet=new NodeSet();
		String wildcardpath=xpath.substring(0,xpath.indexOf("[*]"));
		try {
			XPathEvaluator xpeval = new XPathEvaluator();
			XPathExpression exp = xpeval.compile(xpath);
			NodeList ni = (NodeList) exp.evaluate(workdocument,	XPathConstants.NODESET);
			for (int i=0;i<ni.getLength();i++){
				String localpath=getXpath((Element)ni.item(i),"");
				if (localpath.startsWith(wildcardpath)){
					String rest=localpath.substring(wildcardpath.length(), localpath.length());
					String nodepath=wildcardpath+rest.substring(0,rest.indexOf("]")+1);
					Node n=getNodeFromxpath(nodepath);
					nodeSet.addNode(n);
				} else {
					Dbutil.logger.error("Wildcardpath is "+wildcardpath+" but node has path "+localpath);
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return nodeSet;
	}
*/

	/**
	 * Paths may be a collection of possible candidates for a relative path. 
	 * We need to find the deepest node because we expect it to be the most accurate,
	 * while skipping irrelevant text
	 * @param paths
	 * @return
	 */
	private static String getDeepestPath(ArrayList<String> paths){
		ArrayList<String> candidates=new ArrayList<String>(0);
		for (String path:paths){
			boolean add=true;
			for (int i=0;i<candidates.size();i++){
				if (path.contains(candidates.get(i))){
					// replace with new deeper path
					candidates.set(i, path);
					add=false;
				} else if (candidates.get(i).contains(path)){
					add=false;
				}
			}
			if (add) candidates.add(path);
		}
		if (candidates.size()==1) return candidates.get(0);
		// There are more than one excluding paths. Find the one with the highest number of steps
		int maxsteps=0;
		String result="";
		TreeSet<String> sorted=new TreeSet<String>();
		sorted.addAll(candidates);


		for (String path:sorted){
			if (path.split("/").length>maxsteps){
				maxsteps=path.split("/").length;
				result=path;
			}
		}
		return result;
	}
	
	public HashSet<String> getFrameUrls(String page,String sourceurl){
		String regex="<i?frame[^>]+src=['\"]([^'\"]+)['\"]";
		Pattern pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		Matcher matcher=pattern.matcher(page);
		String baseurl="";
		HashSet<String> frames=new HashSet<String>();
		if (sourceurl.startsWith("http")){
			if (sourceurl.indexOf("/", 9)>0) {
				baseurl=sourceurl.substring(0, sourceurl.indexOf("/", 9));
			} else {
				baseurl=sourceurl;
			}
		}
			
		if (matcher.find()){
			String frameurl=Spider.padUrl(matcher.group(1), sourceurl, baseurl,"shopurl");
			frames.add(frameurl);
		}
		return frames;
	}


	public boolean moreDocuments(LinkedHashSet<String> frames){
		if (frames.size()>0){
			String newurl=frames.iterator().next();
			//Dbutil.logger.info("Analizing "+newurl);
			frames.remove(newurl);
			if (!newurl.equals("Existing document")){
				String page=HTMLParser.getPage(newurl);
				HTMLParser p=new HTMLParser(page);
				//LoboParser p=new LoboParser(newurl);
				//String page=getNodeAsXML(p.document);
				originaldocument=p.document;
				tagDocument(originaldocument);
				document=taggeddocument;
				workdocument=taggeddocument;

			}
			return (true);
		} else {
			return false;
		}
	}
	
	
	public Result analyseDocument(int numberofitemstoanalyze, int mandatoryitem) throws Exception{

		boolean log=true;
		LinkedHashSet<String> frames=new LinkedHashSet<String>();
		ArrayList<ArrayList<String>> fieldpaths=new ArrayList<ArrayList<String>>();
		try{
			f(0).setContentHandler(f(mandatoryitem).getContentHandler());
			init();
			fieldpaths=new ArrayList<ArrayList<String>>(config.size());
			for (int i=0;i<config.size();i++) fieldpaths.add(new ArrayList<String>(0));

			int progressInt=0;
			progress="";
			progress+="<br/>Getting remote web page...";
			progress=progressInt+"%...";
			if (document==null) {
				getUrls(url);
				Parser p=ParserFactory.getParser(url);
				String page=getNodeAsXML((Node)p.getDocument());
				//Dbutil.logger.info(page);
				//HTMLParser p=new HTMLParser(page);
				//LoboParser p=LoboParser.parsePage(page);
				//Dbutil.logger.info(p.document.getClass());
				//Dbutil.logger.info(getNodeAsXML(p.document));
				
				frames.add("Existing document");
				frames.addAll(getFrameUrls(page,url));
				originaldocument=p.getDocument();
				tagDocument(originaldocument);
				document=taggeddocument;
				workdocument=taggeddocument;

			} else {
				frames.add("Existing document");
			}
			progressInt=progressInt+9;
			progress=progressInt+"%...";
			boolean founditempath=false;
			while (moreDocuments(frames)&&!founditempath){
				
				progress+="<br/>Analyzing page structure";
				progressInt=progressInt+1;
				progress=progressInt+"%...";

				if (log) Dbutil.logger.info("Get all text nodes and their path");
				// Get path and nodes for items (f(0))
				Set<Node> allTextNodesMap=getAllTextNodes(workdocument, itemtagfilter);
				
				if (log) Dbutil.logger.info("Get all item paths");
				Set<Node> itemnodes=f(mandatoryitem).getContentHandler().recognizeContent(allTextNodesMap);
				//Set<String> itemvalues
				if (itemnodes.size()<5){
					if (config.get(1).label.equals("Price")) config.set(1, new ConfigField(
							ConfigField.Defaulttype.PRICENODECIMAL, "Price"));
					itemnodes=f(mandatoryitem).getContentHandler().recognizeContent(allTextNodesMap);
				}
				if (itemnodes.size()>1){
					ArrayList<String> itempaths=new ArrayList<String>();
					for (Node key:itemnodes) {
						
						if (itempaths.size()<numberofitemstoanalyze){
							itempaths.add(getXpath((Element)key,""));
						}
					}
					String[] allitemxpaths=(String[])itempaths.toArray(new String[0]);
					if (log){
						Dbutil.logger.info("Itempaths:");
						for (String s:allitemxpaths) Dbutil.logger.info(s);	
					}
					f(0).xpath=getBestXpath(allitemxpaths);
					if (f(0).xpath.indexOf("[*]")>0){
						founditempath=true;
					}
				}
			}
				if(log) Dbutil.logger.info(f(0).xpath);
			if (f(0).xpath.indexOf("[*]")==-1){
				return null;
			} else {
				progressInt=20;
				progress=progressInt+"%...";
				f(0).xpath=f(0).xpath.substring(0,f(0).xpath.lastIndexOf("[*]"));
				f(0).xpath=getMinimizedPath(f(0).xpath);
				if (log) Dbutil.logger.info("Itempath: "+f(0).xpath);

				// Find all text nodes within itemnodes
				if (log) Dbutil.logger.info("Find all text nodes within itemnodes");
				NodeSet itemnodes=getNodeSetFromxpath(f(0).xpath);
				//HashSet<String> nodevalues=new HashSet<String>();

				Set<Node> nodes=new HashSet<Node>();

				//ArrayList<String[]> list=new ArrayList<String[]>();
				//HashMap<String,String> itemtextnodes=new HashMap<String,String>(); 
				int nodecounter=0;
				Set<Node> tempset;
				for (int i=0;(i<itemnodes.getLength()&&nodecounter<numberofitemstoanalyze);i++){
					tempset=getAllTextNodes(itemnodes.item(i), itemtagfilter);
					if (tempset.size()>0){
						nodes.addAll(tempset);
						nodecounter++;
					} else {
						itemnodes.removeElement(itemnodes.item(i));
					}
				}
				if (log) Dbutil.logger.info("Analyzing individual fields");
				for (int k=1;k<config.size();k++){
					progress+="<br/>Finding all "+config.get(k).label+" nodes...";
					progressInt=progressInt+10;
					progress=progressInt+"%...";
					if (log) Dbutil.logger.info("Analyzing "+config.get(k).label);
					ArrayList<String> paths=new ArrayList<String>();
					Set<Node> matches=config.get(k).getContentHandler().recognizeContent(nodes);
					if (log) Dbutil.logger.info("get paths");
					for (Node n:matches) paths.add(getRelXpath((Element)n, config.get(0).xpath));
					if (log) Dbutil.logger.info("get best rel path");
					fieldpaths.set(k,getBestRelXpath((String[])paths.toArray(new String[0])));
					if (log) Dbutil.logger.info("get deepest path");
					config.get(k).xpath=getDeepestPath(fieldpaths.get(k));
					if (log) Dbutil.logger.info("get minimized path");
					config.get(k).xpath=getMinimizedRelativePath(config.get(k).xpath, config.get(0).xpath);
					if (log) Dbutil.logger.info(config.get(k).label+" path: "+config.get(k).xpath);
					progress+="<br/>Got all "+config.get(k).label+" nodes.";
					if (config.get(k).mustcontainvalue&&config.get(k).xpath.equals("")){
						//if (log) Dbutil.logger.info(config.get(k).label+" is mandatory but not found!");
						return null;
					}

				}
				setColors();
				progressInt=progressInt+10;
				progress=progressInt+"%...";
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
			throw (e);
		}
		this.result=parse();
		return this.result;
	}

	public void setColors(){
		for (int field=0;field<config.size();field++){
			int colornumber=-1;
			for (int i=field-1;i>0;i--){
				if(f(field).xpath.equals(f(i).xpath)){
					colornumber=i;
				}
			}
			if(colornumber==-1) colornumber=field;
			if (colornumber>=colors.length) colornumber=colors.length-1; 
			currentcolors[field]=colors[colornumber];
			if (f(field).xpath.equals("")) currentcolors[field]="#dddddd";
		}

	}

	public void fixLinks(String domainurl,String urlpath){
		NodeSet ns=getNodeSetFromxpath("//attribute::src");
		for (int i=0;i<ns.getLength();i++){
			String src=ns.item(i).getNodeValue();
			if (!src.startsWith("http")){
				if (src.startsWith("/")){
					src=domainurl+src;
				} else {
					src=urlpath+"/"+src;
				}
			}
			ns.item(i).setNodeValue(src);
		}
		ns=getNodeSetFromxpath("//attribute::href");
		for (int i=0;i<ns.getLength();i++){
			String src=ns.item(i).getNodeValue();
			if (!src.startsWith("http")){
				if (src.startsWith("/")){
					src=domainurl+src;
				} else {
					src=urlpath+"/"+src;
				}
			}
			ns.item(i).setNodeValue(src);
		}
		ns=getNodeSetFromxpath("//text()[matches(.,\"src\\s*=\\s*\"\".*?\"\"\")]");
		for (int i=0;i<ns.getLength();i++){
			String nodetext=ns.item(i).getNodeValue();
			Pattern pattern=Pattern.compile("(src\\s*=\\s*\")([^\"]*)\"");
			Matcher matcher=pattern.matcher(nodetext);

			while (matcher.find()){
				if (!matcher.group(2).startsWith("http")){
					String replacement="";
					if (matcher.group(2).startsWith("/")){
						replacement=matcher.group(1)+urlpath+matcher.group(2)+"\"";
					} else {
						replacement=matcher.group(1)+domainurl+"/"+matcher.group(2)+"\"";
					}
					nodetext=nodetext.replaceAll(matcher.group(1)+matcher.group(2)+"\"", replacement);
				}

			}
			ns.item(i).setNodeValue(nodetext);
		}
	}


	public static String[] ArrayList2StringArray(ArrayList<String> input) throws java.lang.ClassCastException{
		String[] output=new String[input.size()];
		int i=0;
		for (String item:input) {
			output[i]=input.get(i);
			i++;
		}
		return output;
	}

	public void highlightnodes(Result result){
		HashMap<String,String> uniquetags=new HashMap<String, String>();
		int n=0;
		int counter=0;
		for (AbstractConfigField f:config){
			if (f.xpath.equals("")){
				currentcolors[counter]="#dddddd";
			} else {
				if (uniquetags.get(f.xpath)==null){
					currentcolors[counter]=colors[Math.min(n,colors.length-1)];
					uniquetags.put(f.xpath, colors[Math.min(n,colors.length-1)]+"");
					n++;
				} else {
					currentcolors[counter]="#888888";
				}
			}
			counter++;
		}

		CachedXPathAPI cxapi=new CachedXPathAPI();
		for (Record record:result){
			for (int j=0;j<record.size();j++){
				//Dbutil.logger.info(record.get(j).label+": "+record.get(j).content);
				if (!currentcolors[j].equals("#888888")) highlightNode(getNodeFromTag(cxapi,record.get(j).fwscounter+""),currentcolors[j]);
			}
		}
	}



	public static final void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		String urlstr="http://www.roelvin.eu/wijnenwijn.php";



		if (true){
			//Dbutil.logger.info(com.freewinesearcher.common.Knownwines.containsRegionName("Regio: Gevrey Chambertin "));
			//Dbutil.logger.info(com.freewinesearcher.common.Knownwines.containsRegionName("Regio: Gevre hambertin "));
			//XpathParser xp=new XpathParser("http://www.delicasa.com/search.php?mode=search&catoption=6&subcat_option=4",labels);
			//XpathParser xp=new XpathParser("http://www.delicasa.com/search.php?mode=search&catoption=6&subcat_option=4",labels);
			//XpathParser xp=new XpathParser("http://www.gute-weine.de/sites/GuteWeine/index.jsp?pageIndex_artikel_GuteWeine=2&midPart=restposten");
			//XpathParser xp=new XpathParser("");
			try {
				long id=0;

				if (true){
					//an.highlightnodes(an.analyseDocument(40, 1));
					Analyzer an;
					XpathParserConfig xppc=makeConfig("Wine");
					an=new Analyzer(xppc);
					an.setUrl(urlstr);
					an.result=an.analyseDocument(40, 1);
					Result result=an.result;
					for (Record record:result){
						for (int j=0;j<record.size();j++){
							Dbutil.logger.info(record.get(j).label+": "+record.get(j).content);

						}
					}
				}
				//id=Serializer.writeJavaObject(0,4,0,"Test");
				//Dbutil.logger.info((String)Serializer.readJavaObject(0,4,id));
				//id=1;
				if (id==0) id=12;
				if (false&&id>0){
					/*
				Analyzer savedxpp=new Analyzer((XpathParserConfig)Serializer.readJavaObject(0,3,id));
				Webpage webpage=new Webpage();
				webpage.urlstring=urlstr;
				webpage.readPage();
				savedxpp.setNewPage(new HTMLParser(webpage.html).document, urlstr);
				ArrayList<XpathParserRecord> result=savedxpp.scrape();
				for (XpathParserRecord record:result){
					for (int j=0;j<record.size();j++){
						Dbutil.logger.info(record.get(j).label+": "+record.get(j).content);
						savedxpp.highlightNode(savedxpp.getNodeFromTag(record.get(j).fwscounter+""),colors[Math.min(j,colors.length-1)]);
					}
				}
					 */
				}
				//String xpath="descendant::element()[child::text()[matches(.,\"\\d+[.,]\\d+\")]]";
				//xpath="descendant::element()[count(descendant::text()[matches(.,\"\\d+[.,]\\d+\")])=1][parent::*[count(descendant::text()[matches(.,\"\\d+[.,]\\d+\")])>1]]";
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}

			//NodeSet ns=xp.getNodeSetFromxpath(xpath);
			//for (int i=0;i<ns.getLength();i++){
			//	System.out.println(ns.item(i).getNodeName()+": "+ns.item(i).getTextContent());
			//}
		}
	}


}

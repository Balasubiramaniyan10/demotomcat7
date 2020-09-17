package com.searchasaservice.parser.xpathparser;

import java.io.Serializable;
import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.NodeSet;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Jasper Hammink
 *
 */

public class XpathParser2 implements Serializable{

	private static final long serialVersionUID = 1L;
	public XpathParserConfig config=new XpathParserConfig();
	public Document document;
	public String url="";
	public String postdata="";
	public int shopid;
	long id;
	
	public XpathParser2(XpathParserConfig config) throws Exception{
		super();
		this.config=config;
		config.init();
	}
	public static XpathParser getXpathParser(Context c,int shopid, int row){
		if (row==0) row=Dbutil.readIntValueFromDB("select *,count(*) as thecount from xpathobjects where tenant="+c.tenant+" and shopid="+shopid+" and classname='com.searchasaservice.parser.XpathParserConfig' group by tenant,shopid having thecount=1;", "id");
		if (row>0) {
			
			try {
				Analyzer2 xp=new Analyzer2((XpathParserConfig) Serializer
						.readJavaObject("xpathobjects",c.tenant, shopid, row));
				xp.id=row;
				xp.shopid=shopid;

				return xp;
			} catch (Exception e) {
				Dbutil.logger.error("Problem retrieving xpathparser with id "+row+", shopid "+shopid+" for tenant "+c.tenant, e);
			}
		}
		return null;
	}
	public static ArrayList<XpathParser> getXpathParsers(Context c,int shopid){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		ArrayList<XpathParser> list=new ArrayList<XpathParser>();

		try {
			rs=Dbutil.selectQuery("select * from xpathobjects where tenant="+c.tenant+" and shopid="+shopid+" and classname='com.searchasaservice.parser.xpathparser.XpathParserConfig';", con);
			while (rs.next()){
				list.add(getXpathParser(c, shopid, rs.getInt("id")));
			}
		} catch (Exception e) {
				Dbutil.logger.error("Problem retrieving xpathparser for shop "+shopid+" for tenant "+c.tenant, e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return list;
	}

	public long save(Context c){
		if(shopid>0){
		try {

			//Dbutil.executeQuery("delete from xpathobjects where tenant="+c.tenant+" and shopid="+shopid+";");
			id = Serializer.writeJavaObject("xpathobjects",c.tenant, shopid, id, config.getUID() , config);
			Dbutil.executeQuery("delete from scrapelist where shopid="+shopid+" and url='"+Spider.SQLEscape(url)+"' and postdata='"+Spider.SQLEscape(postdata)+"';");
			int hashcode=(url+postdata).hashCode();
			int i = Dbutil.executeQuery(
					"Insert into scrapelist (Url, Postdata, Headerregex, regex, scrapeorder,parenturl,Shopid, URLType, tablescraper, Status,hashcode) " +
					"values ('"+Spider.SQLEscape(url)+"', '"+Spider.SQLEscape(postdata)+"', '', '','','', '"+shopid+"', 'Master', '0', 'Ready',"+hashcode+");");
			if (i>0) return id;
			return 0;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		} else {
			Dbutil.logger.error("Could not save xpathparser because shopid=0");
		}
		return 0;
	}

	/*private static Node getNodeFromRelXpath(String xpath, Node ref){
		Node n=null;
		if(!"".equals(xpath)){
			try {
				n = XPathAPI.selectSingleNode(ref, xpath);
			} catch (Exception e) {
				Dbutil.logger.error("Problem with xpath "+xpath, e);
			}
		}
		return n;
	}
	*/
	
	public static NodeSet getNodesWithTextNodes(Node n,String filter){
		//Dbutil.logger.info(getNodeAsXML(n));
		String regex="descendant::element()"+filter+"[child::text()[matches(.,\"\\S\\S\\S\")]]";
		NodeSet nodeset=new NodeSet();
		try {
			XPathEvaluator xpeval=new XPathEvaluator();
			XPathExpression exp=xpeval.compile(regex);
			NodeList ni=(NodeList)exp.evaluate(n,XPathConstants.NODESET);
			for (int i=0;i<ni.getLength();i++){
				//Add non-empty nodes
				//Dbutil.logger.info(ni.item(i).getNodeName()+":"+ni.item(i).getTextContent());

				if (!"".equals(ni.item(i).getTextContent().trim()))	{
					nodeset.addNode(ni.item(i));
				} else {
					//Dbutil.logger.info("trimmed:"+ni.item(i).getTextContent().trim()+" gives "+!"".equals(ni.item(i).getTextContent().trim()));

				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return nodeset;
	}

	
	
	private static Node getNodeFromRelXpath(CachedXPathAPI cxapi,String xpath, Node ref){
		Node n=null;
		if ("self".equals(xpath)) {
			n=ref;
		} else 	if(!"".equals(xpath)){
			try {
				n = cxapi.selectSingleNode(ref, xpath);
			} catch (Exception e) {
				Dbutil.logger.error("Problem with xpath "+xpath, e);
			}
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
				nl=(DOMNodeList)exp.evaluate(document,XPathConstants.NODESET);
				for (int i=0;i<nl.getLength();i++) {
					n.addNode(nl.item(i));
				}

			} catch (Exception e) {
				Dbutil.logger.info("Problem with xpath "+xpath,e);
			}
		}
		return n;
	}


	

	public Result parse() throws Exception{

		CachedXPathAPI cxapi=new CachedXPathAPI();
		Result result=new Result();
		NodeSet itemnodes=getNodeSetFromxpath(config.get(0).xpath);
		Node n;
		Field field;
		for (int i=0;i<itemnodes.getLength();i++){
			boolean itemfound=true;
			Record record=new Record();
			for (AbstractConfigField xpfield:config){
				field=xpfield.getField();
				if (field.label.equals("Url")) {
					xpfield.setDefaultValue(url);
				}
				if (itemfound&&!xpfield.isItemField){
					n=getNodeFromRelXpath(cxapi,xpfield.xpath, itemnodes.item(i));
					if (n!=null){
						field.setContent(xpfield.getContentHandler().getContent(n));
						if (!"".equals(((Element)n).getAttribute("fwscounter"))) field.fwscounter=Integer.parseInt(((Element)n).getAttribute("fwscounter"));
						
					} 
					if (field.content==null&&xpfield.defaultvalue!=null){
						field.setContent(xpfield.defaultvalue);
					}
					if (xpfield.mustcontainvalue&&field.content==null){
						itemfound=false;
					} else {
						itemfound=true;
					}
				} else {
					if(xpfield.isItemField){
						field=xpfield.getField();
						if (!"".equals(((Element)itemnodes.item(i)).getAttribute("fwscounter"))) field.fwscounter=Integer.parseInt(((Element)itemnodes.item(i)).getAttribute("fwscounter"));
					}
				}
				
				if (field!=null) record.add(field);
			}
			if (itemfound){
				result.add(record);
			}
		}
		return result;
	}
	

}

package com.freewinesearcher.common;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.apache.xpath.NodeSet;
import org.w3c.dom.*;

import org.w3c.dom.Document;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.datafeeds.DataFeed;
import com.freewinesearcher.online.Auditlogger;
import com.freewinesearcher.online.Webroutines;

public class Shop {
	public int shopid=0;
	public String shopname="";
	public String address="";
	public String email="";
	public String shopurl="";
	public String linkurl="";
	public String feedurl="";
	public int exvat=2;
	public String currency="";
	public String countrycode="";
	public String country="";
	public boolean disabled;
	public String encoding="";
	public DataFeed datafeed=null;
	public String ShopConfigUrl="";
	public String urlmessage="";
	public String checkshopmessage="";
	public String auto="";
	public boolean emptyshop=false;
	public float costperclick;
	java.sql.Timestamp acceptedsiteindexagreementon=null;
	java.sql.Timestamp acceptedadagreementon=null;
	
	public Shop(){
		emptyshop=true;
	}
	
	public Shop(String urlString){
		super();
		ShopConfigUrl=urlString;

		Document doc =readXML(urlString);
		if (doc!=null){
			shopname=getNodeValueFromXpath((Node) doc, "/osp:Shop/Name");
			if (shopname==null) {
				urlmessage+="Somehow this shopinfo.xml does not contain the right data.";
			} else {
				shopurl=getNodeValueFromXpath((Node) doc, "/osp:Shop/Url");
				address=getNodeValueFromXpath((Node) doc, "//Address[@sale='yes']/Street")+", "+getNodeValueFromXpath((Node) doc, "//Address[@sale='yes']/Postcode")+", "+getNodeValueFromXpath((Node) doc, "//Address[@sale='yes']/City");
				if (address.length()<5) address=getNodeValueFromXpath((Node) doc, "//Address[@sale='no']/Street")+", "+getNodeValueFromXpath((Node) doc, "//Address[@sale='no']/Postcode")+", "+getNodeValueFromXpath((Node) doc, "//Address[@sale='no']/City");
				Document coordinates=readXML("http://local.yahooapis.com/MapsService/V1/geocode?appid=UBv0X4nV34HV8LAuLZ44PWUGyhAPLSrQeFK7rC02sutFo9c.mlZOZtYn3Pm0fdZDL1j0TQ--&location="+Webroutines.URLEncode(address));
				country=getNodeValueFromXpath((Node) coordinates, "//*[local-name() = 'State']");
				countrycode=getNodeValueFromXpath((Node) coordinates, "//*[local-name() = 'Country']");
				currency=Webroutines.getDefaultCurrency(countrycode);
				feedurl=getNodeValueFromXpath((Node) doc, "//CSV/Url");
				email=getNodeValueFromXpath((Node) doc, "//PublicMailAddress");
				if (1==Dbutil.readIntValueFromDB("select count(*) as thecount from shops where shopname='"+Spider.SQLEscape(shopname)+"';","thecount")){
					shopid=Dbutil.readIntValueFromDB("select id from shops where shopname='"+Spider.SQLEscape(shopname)+"';","id");
					email=Dbutil.readValueFromDB("select * from shops where shopname='"+Spider.SQLEscape(shopname)+"';","email");
					linkurl=Dbutil.readValueFromDB("select * from shops where shopname='"+Spider.SQLEscape(shopname)+"';","linkurl");
					currency=Dbutil.readValueFromDB("select * from shops where id="+shopid+";","currency");
					exvat=Dbutil.readIntValueFromDB("select * from shops where id="+shopid+";","exvat");
					countrycode=Dbutil.readValueFromDB("select * from shops where id="+shopid+";","countrycode");
					country=Webroutines.getCountryFromCode(countrycode);
					datafeed=DataFeed.getDataFeed(new Context(1), shopid, 0);
				}
				if (datafeed==null) datafeed=new DataFeed(feedurl,doc);
				datafeed.shopconfigurl=urlString;
				encoding=datafeed.encoding;
			}

		} else {
			urlmessage+="We could not reach the url you provided.<br/>";
		}



	}

	public Shop(int shopid){
		
		super();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from shops where id="+shopid;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				this.shopid=shopid;
				shopname=rs.getString("shopname");
				email=rs.getString("email");
				address=rs.getString("address");
				currency=rs.getString("currency");
				exvat=rs.getInt("exvat");
				shopurl=rs.getString("shopurl");
				linkurl=rs.getString("linkurl");
				costperclick=rs.getFloat("costperclick");
				countrycode=rs.getString("countrycode");
				country=Webroutines.getCountryFromCode(countrycode);
				//datafeed=DataFeed.getDataFeed(new Context(1), shopid, 0);
				address=address.replaceAll(", "+country+"$", "");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public void checkShop(){
		checkshopmessage="";
		shopid=Dbutil.readIntValueFromDB("select * from shops where shopname='"+Spider.SQLEscape(shopname)+"';", "id");
		if (shopname==null||shopname.equals("")) checkshopmessage+="Please enter a name for the shop.<br/>";
		if (address==null||address.equals("")) checkshopmessage+="Please enter the adress of the company.<br/>";
		if (countrycode==null||countrycode.equals("")) checkshopmessage+="Please select the country where the company is located.<br/>";
		if (email==null||email.equals("")) checkshopmessage+="Please enter the email adress of the company.<br/>";
		if (shopurl==null||shopurl.equals("")) checkshopmessage+="Please enter the home page url of the company.<br/>";
		if (linkurl.equals("")){
			checkshopmessage+="No url for the link to vinopedia.com was given.<br/>";
		} else {
			URL linkURL=null;
			URL shopURL=null;
			try {linkURL=new URL(linkurl);}catch(Exception e){}
			try {shopURL=new URL(shopurl);}catch(Exception e){}
			if (linkURL==null){
				checkshopmessage+="The link to vinopedia.com is not a corect url.<br/>";
			} else if (shopURL==null){
				checkshopmessage+="The link to the shop is not a correct url.<br/>";
			} else if (!linkURL.getHost().equalsIgnoreCase(shopURL.getHost())){
				checkshopmessage+="The domain of the link to vinopedia.com is different from the domain of your webshop <br/>("+shopurl+").<br/>";
			} else if (!Webroutines.checkLinkOnPage(linkurl,"(href=.?https://www.vinopedia.com)")){
				checkshopmessage+="The link to vinopedia.com could not be found on "+linkurl+".<br/>";
			} else if (!Webroutines.checkLinkOnPage(shopurl,"")){
				checkshopmessage+="The link to your shop could not be reached.<br/>";
			}
		}
		if (exvat==2){
			checkshopmessage+="Please indicate if VAT is included in your prices.<br/>";
		}
		if (!isTermsaccepted()) checkshopmessage+="You must accept the terms and conditions for placement on Vinopedia.<br/>";
		
	}

	public int save(String username){
		int i=0;
		boolean succes=false;
		int partnerid=0;
		Connection con=Dbutil.openNewConnection();
		ResultSet rs=null;
		try{
			if (shopid==0){ // add a new shop!
				int oldid=Dbutil.readIntValueFromDB("select count(*) as thecount from shops where shopname='"+Spider.SQLEscape(shopname)+"';","thecount");
				if (oldid==0){
					i=Dbutil.executeQuery(
							"Insert into "+auto+"shops (shopname, shopurl, baseurl, urltype, countrycode, exvat, currency, email,address,disabled,encoding,linkurl,acceptedagreementon) values ('"+Spider.SQLEscape(shopname)+"','"+shopurl+"','"+shopurl+"','Fixed','"+countrycode+"','"+exvat+"','"+currency+"','"+email+"','"+Spider.SQLEscape(address+", "+country)+"',"+disabled+",'"+encoding+"','"+linkurl+"','"+new java.sql.Timestamp(new java.util.Date().getTime())+"');",con);
					rs=Dbutil.selectQuery("select LAST_INSERT_ID() as shopid;", con);
					rs.next();
					i=rs.getInt("shopid");
					String acceptedon="0000-00-00 0:00:00";
					if (isAdagreementsaccepted()) {
						acceptedon=new java.sql.Timestamp(new java.util.Date().getTime()).toString();
					}
					int result=Dbutil.executeQuery(
							"Insert into partners (name, shopid,adenabled,email,address,cpclink,cpcbanner,payterm,acceptedadagreementon) values ('"+Spider.SQLEscape(shopname)+"',"+i+",0,'','',0,0,"+Configuration.defaultpayterm+",'"+acceptedon+"');",con);
					rs=Dbutil.selectQuery("select LAST_INSERT_ID() as partnerid;", con);
					rs.next();
					partnerid=rs.getInt("partnerid");
					if (partnerid>0) {
						if (isAdagreementsaccepted()) {
							Auditlogger al=new Auditlogger();
							al.setUserid(username);
							al.partnerid=partnerid;
							al.shopid=shopid;
							al.setAction("Accepted Ad Agreement");
						}

						Dbutil.executeQuery("Update jforum_users set partnerid="+partnerid+" where partnerid=0 and username='"+Spider.SQLEscape(username)+"';",con);
						succes=true;
					}
				} else {
					i=-1;
				}
			} else { //Shop url already exists; update the values
				Dbutil.executeQuery(
						"update "+auto+"shops set shopname = '"+Spider.SQLEscape(shopname)+"', baseurl = '"+shopurl+"', linkurl = '"+linkurl+"', urltype= 'Fixed', countrycode= '"+countrycode+"', currency= '"+currency+"', encoding= '"+encoding+"', exvat= '"+exvat+"', shopurl ='"+shopurl+"', email ='"+email+"', disabled="+disabled+" where id="+shopid+";");
				i=shopid;
				succes=true;

			}
			
			
		}catch( Exception e ) {
			Dbutil.logger.error("Problem adding/changing shop info for "+auto+"shop "+shopid+":",e );
			succes=false;
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if(!succes) i=0;

		return i;


	}

	public static Document readXML(String urlString){
		Document doc = null;
		try {
			URL url = new URL(urlString);
			URLConnection URLconnection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
			InputStream in = httpConnection.getInputStream();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(in);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return doc;
	}

	public String getShopEditHTML(){

		String info="";
		if (shopname!=null){
			info+="<tr><td>Name</td><td>"+shopname+"</td></tr>";
			info+="<tr><td>Url</td><td>"+shopurl+"</td></tr>";
			info+="<tr><td>Address</td><td>"+address+", "+country+"</td></tr>";
			info+="<tr><td>Email</td><td>"+email+"</td></tr>";

		}
		return info;
	}


	public static String getNodeValueFromXpath(Node node,String xpath){

		NamespaceContext ctx = new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				if (prefix.equals("osp")) return "http://elektronischer-markt.de/schema";
				if (prefix.equals("yahoo")) return "http://api.local.yahoo.com/MapsService/V1/GeocodeResponse.xsd";
				return "";
			}
			public Iterator getPrefixes(String val) {
				return null;
			}
			public String getPrefix(String uri) {
				return null;
			}
		};
		NodeSet n=new NodeSet();
		String value="";
		if (""!=xpath){
			DOMNodeList nl;
			try {
				XPathEvaluator xpeval=new XPathEvaluator();
				xpeval.setNamespaceContext(ctx);
				XPathExpression exp=xpeval.compile(xpath);
				nl=(DOMNodeList)exp.evaluate(node,XPathConstants.NODESET);
				if (nl.getLength()>0) {
					value=nl.item(0).getTextContent();
				}
			} catch (Exception e) {
				Dbutil.logger.info("Problem with xpath "+xpath,e);
			}
		}
		return value;
	}

	public String getShopname() {
		return shopname;
	}

	public void setShopname(String shopname) {
		this.shopname = shopname;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isTermsaccepted() {
		if (acceptedsiteindexagreementon!=null) return true;
		return false;
	}

	public void setTermsaccepted(boolean accepted) {
		if (accepted) {
			acceptedsiteindexagreementon=new java.sql.Timestamp(new java.util.Date().getTime());
		} else {
			acceptedsiteindexagreementon=null;
		}
	}

	public boolean isAdagreementsaccepted() {
		if (acceptedadagreementon!=null) return true;
		return false;
	}

	public void setAdagreementaccepted(boolean accepted) {
		if (accepted) {
			acceptedadagreementon=new java.sql.Timestamp(new java.util.Date().getTime());
		} else {
			acceptedadagreementon=null;
		}
	}

	public String getShopurl() {
		return shopurl;
	}

	public void setShopurl(String shopurl) {
		this.shopurl = shopurl;
	}

	public String getLinkurl() {
		return linkurl;
	}

	public void setLinkurl(String linkurl) {
		if (!linkurl.equals("")&&!linkurl.toLowerCase().startsWith("http")) linkurl="http://"+linkurl;
	    this.linkurl = linkurl;
	}

	public int getExvat() {
		return exvat;
	}

	public void setExvat(int exvat) {
		this.exvat = exvat;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCountrycode() {
		return countrycode;
	}

	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}
	
	


}

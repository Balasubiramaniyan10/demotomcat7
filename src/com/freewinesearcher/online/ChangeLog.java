package com.freewinesearcher.online;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Context;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;
import com.searchasaservice.parser.xpathparser.XpathParser;

public class ChangeLog {
	int id;
	String tablename;
	public Timestamp date=new java.sql.Timestamp(new java.util.Date().getTime()); 
	public String userid;
	int tenant;
	int rowid;
	Document valueold=null;
	Document valuenew=null;
	private String valuenewstring=null;
	String changes;
	public Timestamp rollbackdate; 



	public ChangeLog(){

	}
	public ChangeLog(Context c,ResultSet rs, String idcolumn){
		try {
			tenant=c.tenant;
			userid=c.userid;
			if (rs.isBeforeFirst()) rs.next();
			tablename = rs.getMetaData().getTableName(1);
			rowid=rs.getInt(idcolumn);
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
	}

	public int save(){
		getChanges();
		if (date==null) date=new java.sql.Timestamp(new java.util.Date().getTime());
		if (changes.length()>0)	return Dbutil.executeQuery("insert into changelog (tablename,date,userid,tenant,rowid,valueold,valuenew,changes) values ('"+tablename+"','"+date+"','"+userid+"',"+tenant+","+rowid+",'"+Spider.SQLEscape(getNodeAsXML(valueold).replaceAll("\r", "").replaceAll("\t", ""))+"','"+Spider.SQLEscape(getNodeAsXML(valuenew).replaceAll("\r", "").replaceAll("\t", ""))+"','"+changes+"');");
		return 0;
	}

	public static Document toDocument(ResultSet rs) throws ParserConfigurationException, SQLException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder        = factory.newDocumentBuilder();
		Document doc                   = builder.newDocument();

		Element results = doc.createElement("results");
		doc.appendChild(results);

		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount           = rsmd.getColumnCount();


		Element row = doc.createElement("row");
		results.appendChild(row);

		for (int i = 1; i <= colCount; i++)
		{
			String columnName = rsmd.getColumnName(i);
			String value      = rs.getString(i);
			if (value==null) value="null";

			Element node      = doc.createElement(columnName);
			node.appendChild(doc.createTextNode(value.toString()));
			row.appendChild(node);
		}
		return doc;
	}

	public void setValueOld(ResultSet rs){
		if (rs!=null){
			try {
				if (rs.isBeforeFirst()) rs.next();
				valueold= toDocument(rs);
			} catch (Exception e) {
				Dbutil.logger.error("Could not get row as XML",e);
				valueold=null;
			}
		} else {
			valueold=null;
		}
	}

	public void setValueNew(ResultSet rs){
		if (rs!=null){
			try {
				if (rs.isBeforeFirst()) rs.next();
				valuenew= toDocument(rs);
			} catch (Exception e) {
				Dbutil.logger.error("Could not get row as XML",e);
				valuenew=null;
			}
		} else {
			valuenew=null;
		}
	}

	public void getChanges(){
		changes="";
		if (valueold==null){
			NodeList nl=valuenew.getElementsByTagName("*");
			for (int i=0;i<nl.getLength();i++){
				changes+=","+nl.item(i).getNodeName();

			}
		} else if (valuenew==null){
			NodeList nl=valueold.getElementsByTagName("*");
			for (int i=0;i<nl.getLength();i++){
				changes+=","+nl.item(i).getNodeName();

			}
		} else {
			NodeList old=valueold.getElementsByTagName("*");
			for (int i=0;i<old.getLength();i++){
				if (!old.item(i).getTextContent().equals(valuenew.getElementsByTagName(old.item(i).getNodeName()).item(0).getTextContent())){
					if (!old.item(i).getNodeName().equals("results")&&!old.item(i).getNodeName().equals("row")&&!old.item(i).getNodeName().equals("date")){
						changes+=","+old.item(i).getNodeName();
					}
				}
			}
		}
		if (changes.length()>1) changes=changes.substring(1);
	}

	public static String rollbackChanges(ArrayList<ChangeLog> changes, Context c){
		String result="";
		for (ChangeLog change:changes){
			if (change.rollback()==0){
				result+="From "+XpathParser.getNodeAsXML(change.valueold)+" to "+XpathParser.getNodeAsXML(change.valuenew);

			} else {
				Dbutil.executeQuery("update changelog set rollbackdate='"+new java.sql.Timestamp(new java.util.Date().getTime())+"', rolledbackby='"+c.userid+"' where id="+change.id+";");
			}
		}
		return result;
	}

	public static String rollforwardChanges(ArrayList<ChangeLog> changes, Context c){
		String result="";
		for (ChangeLog change:changes){
			if (change.rollforward()==0){
				result+="From "+XpathParser.getNodeAsXML(change.valuenew)+" to "+XpathParser.getNodeAsXML(change.valueold);

			} else {
				Dbutil.executeQuery("update changelog set rollbackdate='0000-00-00 00:00:00.000', rolledbackby='"+c.userid+"' where id="+change.id+";");
			}
		}
		return result;
	}

	public static ArrayList<ChangeLog> getChanges(int tenant,String userid, String tablename, Timestamp date, boolean all, boolean parsechanges){
		ArrayList<ChangeLog> changes=new ArrayList<ChangeLog>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from changelog where tenant="+tenant+" and userid like '"+userid+"' and tablename like '"+tablename+"' "+(date==null?"":" and date>='"+date+"'")+(all?"":" and rollbackdate is null")+" order by id desc";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				ChangeLog c=new ChangeLog();
				c.id=rs.getInt("id");
				c.tablename=rs.getString("tablename");
				c.date=rs.getTimestamp("date");
				c.userid=rs.getString("userid");
				c.tenant=rs.getInt("tenant");
				if (parsechanges){
					c.valueold=getDocument(rs.getString("valueold"));
					c.valuenew=getDocument(rs.getString("valuenew"));
				}
				c.valuenewstring=rs.getString("valuenew");
				c.changes=rs.getString("changes");
				c.rowid=rs.getInt("rowid");
				c.rollbackdate=rs.getTimestamp("rollbackdate");
				changes.add(c);
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return changes;
	}

	public static ArrayList<ChangeLog> getChanges(Context ct, int id){
		ArrayList<ChangeLog> changes=new ArrayList<ChangeLog>();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from changelog where tenant="+ct.tenant+" and id="+id;
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				ChangeLog c=new ChangeLog();
				c.id=rs.getInt("id");
				c.tablename=rs.getString("tablename");
				c.date=rs.getTimestamp("date");
				c.userid=rs.getString("userid");
				c.tenant=rs.getInt("tenant");
				c.valueold=getDocument(rs.getString("valueold"));
				c.valuenew=getDocument(rs.getString("valuenew"));
				c.changes=rs.getString("changes");
				changes.add(c);
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return changes;
	}

	public int rollback(){
		int result=0;
		String set="";
		String insertcolumns="";
		String insertvalues="";
		String where="";
		String query;
		try {

			if (valueold!=null) for (int i=0;i<valueold.getElementsByTagName("row").item(0).getChildNodes().getLength();i++){
				if (valueold!=null&&!valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName().equals("#text")) set+=", "+valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName()+"='"+Spider.SQLEscape(valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getTextContent())+"'";
				if (valuenew==null&&valueold!=null&&!valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName().equals("#text")) {
					insertcolumns+=", "+valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName();
					insertvalues+=", '"+Spider.SQLEscape(valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getTextContent())+"'";
				}
			}
			if (valuenew!=null) for (int i=0;i<valuenew.getElementsByTagName("row").item(0).getChildNodes().getLength();i++){
				if (valuenew!=null&&!valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName().equals("#text")) {
					where+=" and concat("+valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName()+",'')='"+Spider.SQLEscape(valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getTextContent())+"'";
				} 
			}
			if (set.length()==0){
				query = "delete from "+tablename+" where "+where.substring(5)+";";
				result=Dbutil.executeQuery(query);
			} else if (where.length()==0){
				query = "insert into "+tablename+" ("+insertcolumns.substring(2)+") values ("+insertvalues.substring(2)+");";
				result=Dbutil.executeQuery(query);
			} else {
				query = "update "+tablename+" set "+set.substring(2)+" where "+where.substring(5)+";";
				result=Dbutil.executeQuery(query);
			}


		} catch (Exception e) {
			Dbutil.logger.error("", e);
		}
		return result;
	}

	public int rollforward(){
		int result=0;
		String set="";
		String insertcolumns="";
		String insertvalues="";
		String where="";
		String query;
		try {

			if (valuenew!=null) for (int i=0;i<valuenew.getElementsByTagName("row").item(0).getChildNodes().getLength();i++){
				if (valuenew!=null&&!valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName().equals("#text")) set+=", "+valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName()+"='"+Spider.SQLEscape(valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getTextContent())+"'";
				if (valuenew==null&&valuenew!=null&&!valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName().equals("#text")) {
					insertcolumns+=", "+valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName();
					insertvalues+=", '"+Spider.SQLEscape(valuenew.getElementsByTagName("row").item(0).getChildNodes().item(i).getTextContent())+"'";
				}
			}
			if (valueold!=null) for (int i=0;i<valueold.getElementsByTagName("row").item(0).getChildNodes().getLength();i++){
				if (valueold!=null&&!valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName().equals("#text")) {
					where+=" and concat("+valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getNodeName()+",'')='"+Spider.SQLEscape(valueold.getElementsByTagName("row").item(0).getChildNodes().item(i).getTextContent())+"'";
				} 
			}
			if (set.length()==0){
				query = "delete from "+tablename+" where "+where.substring(5)+";";
				result=Dbutil.executeQuery(query);
			} else if (where.length()==0){
				query = "insert into "+tablename+" ("+insertcolumns.substring(2)+") values ("+insertvalues.substring(2)+");";
				result=Dbutil.executeQuery(query);
			} else {
				query = "update "+tablename+" set "+set.substring(2)+" where "+where.substring(5)+";";
				result=Dbutil.executeQuery(query);
			}


		} catch (Exception e) {
			Dbutil.logger.error("", e);
		}
		return result;
	}

	public static Document getDocument (String rowvalue) throws Exception{
		Document doc=null;
		if (rowvalue!=null&&!rowvalue.trim().equals("")){
			try {
				byte currentXMLBytes[] = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + rowvalue).getBytes("UTF-8");
				ByteArrayInputStream c = new ByteArrayInputStream(currentXMLBytes);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(c);
				doc.getDocumentElement().normalize();

			} catch (Exception e) {
				//Dbutil.logger.info("Problem parsing changelog document: "+"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + rowvalue);
			}
		}
		return doc;
	}

	public static String getChangeHtml(ArrayList<ChangeLog> changes){
		StringBuffer sb=new StringBuffer();
		sb.append("<table><tr><th>Date</th><th>User</th><th>Table</th><th>id</th><th>Changes</th><th>New Value</th><th>Action</th></tr>");
		for (ChangeLog change:changes){
			sb.append("<tr><td><a href='?datefrom="+change.date+"'>"+change.date+"</a></td><td><a href='?username="+change.userid+"'>"+change.userid+"</a></td><td><a href='?tablename="+change.tablename+"'>"+change.tablename+"</td><td>"+change.rowid+"</td>");
			if (change.rollbackdate==null){
				sb.append("<td>");
			} else {
				sb.append("<td style='text-decoration:line-through'>");
			}
			for (String field:change.changes.split(",")){
				if (change.valuenew!=null){
					try{
						sb.append(field+": from "+(change.valueold.getElementsByTagName(field).item(0).getTextContent())+" to "+(change.valuenew.getElementsByTagName(field).item(0).getTextContent())+". <br/>");
					}catch (Exception e){
						sb.append(field+": Cannot parse documented change! ");
					}
				}
			}
			if (change.valuenew!=null) change.valuenewstring=XpathParser.getNodeAsXML(change.valuenew);
			if (change.rollbackdate==null){
				sb.append("</td><td>"+change.valuenewstring+"</td><td onclick=\"javascript:$.post('/admin/rollbackchange.jsp','id="+change.id+"');\">Rollback</td></tr>");
			} else {
				sb.append("</td><td style='text-decoration:line-through'>"+change.valuenewstring+"</td><td onclick=\"javascript:$.post('/admin/rollbackchange.jsp','id="+change.id+"&amp;rollforward=true');\">Roll forward</td></tr>");			
			}


		}
		sb.append("</table>");
		return sb.toString();
	}

	public static String getChangeSummary(){
		StringBuffer sb=new StringBuffer();
		sb.append("<table><tr><th>User</th><th>Changes</th></tr>");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select userid,count(*) as n from changelog where date>=date_sub(now(),interval 1 month) group by userid order by count(*) desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				sb.append("<tr><td><a href='?username="+rs.getString("userid")+"'>"+rs.getString("userid")+"</a></td><td>"+rs.getInt("n")+"</td></tr>");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		sb.append("</table>");
		return sb.toString();
	}

	public static void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		Dbutil.logger.info(rollbackChanges(getChanges(1,"jhammink","usernotes",Timestamp.valueOf("2008-12-15 14:00:01"),false,true), new Context(1)));
	}
	
	public static String daystohistorydate(int days){
		return Dbutil.readValueFromDB("select date_sub(now(),interval "+days+" day) as d;","d");
	}
	
	public static class selectiongroups{
		public ArrayList<String> users;
		public ArrayList<String> tables;
		
		public selectiongroups(){
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			users=new ArrayList<String>();
			tables=new ArrayList<String>();
			try {
				query = "select distinct(tablename) as tablename from changelog order by tablename";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					tables.add(rs.getString("tablename"));
				}
				query = "select distinct(userid) as user from changelog order by userid";
				rs = Dbutil.selectQuery(rs, query, con);
				while (rs.next()) {
					users.add(rs.getString("user"));
				}
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		
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

	
}

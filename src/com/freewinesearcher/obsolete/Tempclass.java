package com.freewinesearcher.obsolete;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.sitescrapers.SuckWineSearcher;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;

public class Tempclass {

	public static int test(){
		//ResultSet rsstatic=Dbutil.selectQuery("Select * from wines", con);
		DBConnection testje=new DBConnection();
		//Dbutil.logger.info("Select using connection "+testje.con.toString());
		testje.selectQuery("Select * from wines limit 10");
		testje.close();
		testje=null;
		//ResultSet rs=null;
		//Connection con=Dbutil.openNewConnection();
//rs=Dbutil.selectQuery("Select * from wines limit 10", con);
//Dbutil.closeRs(rs);
//Dbutil.closeConnection(con);
		
		return 2;
	}


	
	public static void matchKnownWineUnknownAppellation(){
		String query="select knownwines.id as knownwineid, unknownappellations.*  from knownwines left join unknownappellations on (knownwines.wine=unknownappellations.wine) where knownwines.appellation='Unknown' having unknownappellations.wine is not  null;";
		String updatequery;
		ResultSet rs;
		ResultSet rs2;
		Connection con=Dbutil.openNewConnection();
		try{
			//rs=Dbutil.selectQuery(query, con);
			//while (rs.next()){
			//	updatequery="update knownwines set appellation='"+Spider.SQLEscape(rs.getString("region"))+"' where id="+rs.getString("knownwineid")+";";
			//	Dbutil.executeQuery(updatequery,con);
			//}
			query="select * from knownwines where appellation='Unknown'";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				query="select * from unknownappellations where "+Knownwines.whereClauseKnownWines(rs.getString("fulltextsearch"), rs.getString("literalsearch"), rs.getString("literalsearchexclude"), "", false).replace("name", "wine")+" group by region having count(*)=1;";
				rs2=Dbutil.selectQuery(query, con);
				if (rs2.next()){
					updatequery="update knownwines set appellation='"+Spider.SQLEscape(rs2.getString("region"))+"' where id="+rs.getString("id")+";";
					//Dbutil.logger.info(updatequery);
					Dbutil.executeQuery(updatequery,con);
				}
				
			}
			
		}catch (Exception e){
			Dbutil.logger.error("",e);
		}
		



	Dbutil.closeConnection(con);
}

	
	
	public static void readUnknownAppellations(){
		File file=new File("C:\\Temp\\unknownappellations.html");
		Dbutil.executeQuery("delete from unknownappellations");
		byte[] fileasbytes=null;
		try {
			fileasbytes = SuckWineSearcher.getBytesFromFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String Page=new String(fileasbytes);
		String rowregex="(<tr.*?</tr)";
		Matcher matcher;
		Pattern pattern;
		pattern=Pattern.compile(rowregex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
		matcher = pattern.matcher(Page);
		String row;
		while (matcher.find()){
			row=matcher.group(1);
			row=row.replace("&amp;", "&");
			ripAppellationInfo(row);
		}

		}
	
	
	private static void ripAppellationInfo(String row){
		String query;
		String winename=(SuckWineSearcher.rip(">([^<]+)</a>",row));
		String region=(SuckWineSearcher.rip("&Region=([^&]+)&",row));
		String subregion=(SuckWineSearcher.rip("&SubRegion=([^&]+)&",row));
		String finalregion=subregion;
		if (finalregion.equals("Unknown")) finalregion=region;
		if (!finalregion.equals("")){
			try {
				finalregion=java.net.URLDecoder.decode(finalregion,"iso-8859-1");
				query="Insert into unknownappellations(wine,region) values ('"+Spider.SQLEscape(winename)+"','"+Spider.SQLEscape(finalregion)+"');";
				Dbutil.executeQuery(query);
			} catch (Exception exc){
				Dbutil.logger.error("Error while retrieving info from unknown region.",exc);

			}
		}
	}






	public static void matchRegions(){
		String query="select distinct appellation,region from knownwines left join regions on (knownwines.appellation=regions.region) having region is null;";
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] search;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query, con);
			BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Workspace\\regiontable.html"));
			out.write("<html><body><table>\n");
			while (rs.next()){
				search=rs.getString("appellation").split("[ -']");
				fulltext="";
				for (String j:search){
					fulltext+=" +"+j;
				}
				query="select id,lft,region from regions where match (region) against ('"+fulltext+"');";
				rs2=Dbutil.selectQuery(query, con);
				while(rs2.next()){
					out.write("<tr><td>"+rs.getString("appellation")+"</td><td>"+rs2.getString("id")+"</td><td>"+rs2.getString("lft")+"</td><td>"+rs2.getString("region")+"</td></tr>\n");
				}
			}
			out.write("</table>\n</html></body>");
			out.close();
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




	Dbutil.closeConnection(con);
}


	public static void updatehashcodes(){
		String query="select * from scrapelist;";
		ResultSet rs;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String postdata=rs.getString("postdata");
				if (postdata==null) postdata="";
				int hash=(rs.getString("url")+postdata).hashCode();
				query="update scrapelist set hashcode="+hash+" where id="+rs.getInt("id")+";";
				Dbutil.executeQuery(query);
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);
		}




		Dbutil.closeConnection(con);
	}



	public static void matchAppellations(){
		String query="select distinct appellation,region from knownwines left join regions on (knownwines.appellation=regions.region) having region is null;";
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] search;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query, con);
			BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Workspace\\regiontable.html"));
			out.write("<html><body><table>\n");
			while (rs.next()){
				search=rs.getString("appellation").split("[ -']");
				fulltext="";
				for (String j:search){
					fulltext+=" +"+j;
				}
				query="select id,lft,region from regions where match (region) against ('"+fulltext+"');";
				rs2=Dbutil.selectQuery(query, con);
				while(rs2.next()){
					out.write("<tr><td>"+rs.getString("appellation")+"</td><td>"+rs2.getString("id")+"</td><td>"+rs2.getString("lft")+"</td><td>"+rs2.getString("region")+"</td></tr>\n");
				}
			}
			out.write("</table>\n</html></body>");
			out.close();
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}

	public static void matchProducers(){
		String query="select * from knownwines where producer='';";
		ResultSet rs;
		ResultSet rs2=null;
		String fulltext="";
		String[] search;
		Connection con=Dbutil.openNewConnection();
		Connection con2=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query, con2);
			while (rs.next()){
				Dbutil.logger.info("Id="+rs.getInt("id"));
				query="select * from knownwinesproducer where wine='"+Spider.SQLEscape(rs.getString("wine"))+"' and appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"' group by wine having count(*)=1;";			
				Dbutil.closeRs(rs2);
				rs2=Dbutil.selectQuery(query, con);
				if (rs2.next()){
					Dbutil.executeQuery("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
					Dbutil.closeRs(rs2);
				} else {
					
					String wine[]=rs.getString("wine").split(" ");
					for (int i=1;i<5;i++){
						String shortwine="";
						for (int j=0;j<i;j++){
							if (wine.length>j){
								shortwine+=wine[j]+"%";
							}
						}
						shortwine=Spider.SQLEscape(shortwine.trim()).replace("-", "%");
						query="select * from knownwinesproducer where wine like '"+shortwine+"%' and appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"' group by producer;";			
						Dbutil.closeRs(rs2);
						rs2=Dbutil.selectQuery(query, con);
						if (rs2.last()){
							if (rs2.getRow()==1){
								rs2.beforeFirst();
								rs2.next();
								//Dbutil.logger.info("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
								Dbutil.executeQuery("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
								Dbutil.closeRs(rs2);
								i=600;
								
							}
						} else {
							query="select * from knownwinesproducer where wine like '%"+shortwine+"%' and appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"' group by producer;";			
							Dbutil.closeRs(rs2);
							rs2=Dbutil.selectQuery(query, con);
							if (rs2.last()){
								if (rs2.getRow()==1){
									rs2.beforeFirst();
									rs2.next();
									//Dbutil.logger.info("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
									Dbutil.executeQuery("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
									Dbutil.closeRs(rs2);
									i=600;
								}
							} else {
								query="select * from knownwinesproducer where wine like 'Domaine "+shortwine+"%' and appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"' group by producer;";			
								Dbutil.closeRs(rs2);
								rs2=Dbutil.selectQuery(query, con);
								if (rs2.last()){
									if (rs2.getRow()==1){
										rs2.beforeFirst();
										rs2.next();
										//Dbutil.logger.info("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
										Dbutil.executeQuery("update knownwines set producer='"+Spider.SQLEscape(rs2.getString("producer"))+"' where id="+rs.getInt("id")+";");
										Dbutil.closeRs(rs2);
										i=600;
									}
								}
							}
						}
					}
				}
			}
			
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}


	public static void cleanAppellations(){
		String query="select distinct(appellation) from knownwines where appellation!=replace(appellation,'%','!');";
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] search;
		Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String newapp=rs.getString("appellation");
				newapp=java.net.URLDecoder.decode(newapp, "ISO-8859-1");
				newapp=Spider.unescape(newapp);
				query="update knownwines set appellation='"+Spider.SQLEscape(newapp)+"' where appellation='"+Spider.SQLEscape(rs.getString("appellation"))+"';";
				Dbutil.logger.info(query);
				Dbutil.executeQuery(query);
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}



}

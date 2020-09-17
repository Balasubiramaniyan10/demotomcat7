package com.freewinesearcher.batch.sitescrapers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.ResultSet;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;

public class CommonUtilities {
	public String dirname="C:\\temp";

	public void saveFile(String filename, String data){
		new File(dirname).mkdirs();
		try {
			//FileWriter fstream = new FileWriter(dirname + "\\" + filename);
			FileOutputStream fos = new FileOutputStream(dirname + "\\" + filename); 
			OutputStreamWriter fstream = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data);
			//Close the output stream
			out.close();
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
	}

	public void deleteFile(String filename){
		File file =new File(dirname + "\\" + filename);
		if (!file.isDirectory()) file.delete();
	}
	
	public String readFile(File file,String encoding) throws IOException{
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(file),encoding));
		String inputLine;
		StringBuffer sb=new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
			sb.append("\n");
		}
		in.close();
		 return sb.toString();
		
	}
	
	public static void readRegions(){
		String query;
		int id=0;
		ResultSet rs=null;
		String parent="";
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("select distinct vineyard,appellation,locale from kbknownwines;", con);
			while (rs.next()){
				int result=getRegionId(rs.getString("vineyard"),rs.getString("appellation"),rs.getString("locale"));
				if (result>0){
					System.out.println((rs.getString("vineyard").equals("")?"":(rs.getString("vineyard")+", "))+rs.getString("appellation")+" matches "+Dbutil.readValueFromDB("select concat(region,',',parent) as app from kbapphierarchy where id="+result, "app"));
				} else {
					System.out.println("No match for "+(rs.getString("vineyard").equals("")?"":(rs.getString("vineyard")+", "))+rs.getString("appellation"));
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		

	

		
	}
	
	public static int getRegionId(String vineyard,String appellation,String locale){
		String query;
		int id=0;
		ResultSet rs=null;
		String parent="";
		if (locale.split(",").length-2>-1) parent=locale.split(",")[locale.split(",").length-2];
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			if (id==0){
				if (!vineyard.equals("")){
					id=Dbutil.readIntValueFromDB("select * from kbapphierarchy where region='"+Spider.SQLEscape(vineyard)+"' and parent='"+Spider.SQLEscape(appellation)+"';", "id"); 
				}
			}
			if (id==0){
				if (!vineyard.equals("")){
					id=Dbutil.readIntValueFromDB("select * from kbapphierarchy where region='"+Spider.SQLEscape(vineyard)+"' ;", "id"); 
				}
			}
			if (id==0){
				if (!vineyard.equals("")){
					id=Dbutil.readIntValueFromDB("select * from kbapphierarchy where region='"+Spider.SQLEscape(appellation)+"' and parent='"+Spider.SQLEscape(parent)+"';", "id"); 
				}
			}
			if (id==0){
				id=Dbutil.readIntValueFromDB("select * from kbapphierarchy where region='"+Spider.SQLEscape(appellation)+"';", "id"); 
				
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		

		return id;


	}

	public static void main (String[] args){
		readRegions();
		
	}
}

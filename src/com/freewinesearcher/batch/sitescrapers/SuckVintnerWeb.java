package com.freewinesearcher.batch.sitescrapers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import com.freewinesearcher.batch.Coordinates;
import com.freewinesearcher.batch.Excelreader;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.TableScraper;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;


public class SuckVintnerWeb {


	public static void parseDirectory(String dirname, Connection con) {
		try{ 
			File dir=new File(dirname);
			String[] files = dir.list();
			for (int i=0;i<files.length;i++){
				File file=new File(dir.getCanonicalPath()+"\\"+files[i]);
				if (file.isDirectory()){
					parseDirectory(dir.getCanonicalPath()+"\\"+files[i],con);
				} else {
					if (files[i].contains("html")){
						String filename;
						String Page;
						BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
						String inputLine;
						StringBuffer sb=new StringBuffer();
						while ((inputLine = in.readLine()) != null) {
							sb.append(inputLine);

						}
						in.close();
						Page=sb.toString();
						Page=Page.replace("\r", "");
						int id=0;
						try {id=Integer.parseInt(Webroutines.getRegexPatternValue("^(\\d+)_",files[i]));}catch(Exception e){}
						ripinfo(Page,id);
					}
				}
			}
		} catch (Exception exc){
			Dbutil.logger.error("Exception while processing WineSearcher pages. ",exc);
		}
		Dbutil.closeConnection(con);

	}


	// Returns the contents of the file in a byte array.
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}





	public static void ripinfo(String page, int id){
		String line=page.replaceAll("\n", "");
		String producer=Spider.SQLEscape(rip("<span class=\"headline\">([^<]+)</span>",line)).trim();
		String addressline=(rip("<td class=\"small\" width=110>Address</td>(.*?)<td class=\"small\">",line));
		String address=Spider.SQLEscape(rip("<b>([^<]+)</b>",addressline)).trim().replace("<BR>", ", ");
		String phone=Spider.SQLEscape(rip("<td class=\"small\">Phone</td><td class=\"text\">([^<]+)</td>",line)).trim();
		String emailline=rip("(<td class=\"small\">email.*?mailto.*?</script>)",line).trim();
		String emailvar=rip(";(\\D+)\\d = '[^']mailto",emailline);
		String email="";
		if (!"".equals(emailvar)){
			for (int i=0;i<=15;i++){
				email+=Spider.SQLEscape(rip(emailvar+i+" = '([^']+)';",emailline)).trim();
			}
		}
		email=rip(">([^<]+@[^<]+)<",email);
		String webpage=Spider.SQLEscape(rip("<td class=\"small\">Website</td><td class=\"text\"><a href=[^>]+target=\"_blank\">([^<]+)</a>",line)).trim();
		//		String[] coordinates=Coordinates.getCoordinates(address);
		String[] coordinates={"0","0","0"};
		if (!"".equals(producer)&&!"".equals(address)) {
			int result=Dbutil.executeQuery("update producersvintnerweb set addresscomplete='"+producer+", "+address+"',address='"+address+"', name='"+producer+"' where id="+id+";");
			if (result==0)	Dbutil.executeQuery("insert ignore into producersvintnerweb(id,addresscomplete,email,telephone,name,address,lon,lat,accuracy,webpage) values ("+id+",'"+producer+", "+address+"','"+email+"','"+phone+"','"+producer+"','"+address+"',"+coordinates[0]+","+coordinates[1]+","+coordinates[2]+",'"+webpage+"');");
		}


	}
	public static String rip(String regex, String Line){
		String result="";
		Matcher matcher;
		Pattern pattern;
		pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
		matcher = pattern.matcher(Line);
		while (matcher.find()){
			result+=" "+matcher.group(1);
		}
		result=Spider.unescape(result);
		return result.trim();
	}

	public static void main(String[] args){
		Wijnzoeker w=new Wijnzoeker();
		/*Dbutil.executeQuery("Delete from producersvintnerweb;");
		Connection con=Dbutil.openNewConnection();
		parseDirectory("C:\\temp\\WinHTTrack\\vintner-web.com",con);
		Dbutil.executeQuery("update producersvintnerweb set countrycode='FR' where countrycode='' and address like '%France';");
		Dbutil.executeQuery("update producersvintnerweb set countrycode='IT' where countrycode='' and address like '%Italy';");
		Dbutil.executeQuery("update producersvintnerweb set countrycode='DE' where countrycode='' and address like '%Germany';");
		Dbutil.executeQuery("update producersvintnerweb set countrycode='AT' where countrycode='' and address like '%Austria';");
		Dbutil.executeQuery("update producersvintnerweb set countrycode='US' where countrycode='' and address like '%USA';");
		Dbutil.executeQuery("update producersvintnerweb set countrycode='ES' where countrycode='' and address like '%Spain';");
		*/
		Coordinates.getCoordinatesVintnerweb();
		
		Dbutil.executeQuery("Delete from producers where source='producersvintnerweb'");
		Dbutil.executeQuery("insert into producers (name,address,email, telephone,source,sourceid,visiting,countrycode,lat,lon,accuracy,fulltextsearch) select name,addresscomplete,email,telephone,'producersvintnerweb',id,'','',lat,lon,accuracy,concat(name,' ',addresscomplete) from producersvintnerweb;");
		//Dbutil.closeConnection(con);
	}
}

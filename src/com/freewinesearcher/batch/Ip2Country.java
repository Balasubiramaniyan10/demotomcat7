package com.freewinesearcher.batch;

/**
 * Changes the IpToCountry.csv file as downloaded 
 * (after taking away the portions following the # and any empty lines) 
 * from http://software77.net/cgi-bin/ip-country/geo-ip.pl
 * to a tab separated file usable by MySQL
 * 
 * Instructions:
 * Download file to admin dir
 * Remove all # lines
 * Run this program
 * In SQL admin, load the .sql script file in admin dir
 * 
 * 
 */

import java.io.*;
import java.util.StringTokenizer;

import com.freewinesearcher.common.Dbutil;

public class Ip2Country{

   private StringBuffer buffer;
   private String tab;

   public Ip2Country(String inputFile, String outputFile){
	buffer = new StringBuffer(10240);
	/* All the shenanigans in the next four lines just to get 
	 * a reference to a String object which is a tab character
	 * There is most likely a more elegant way of doing it
	 */
	byte[] bytes = new byte[1];
	int z = 9;
	byte tabbyte = (byte)z;
	bytes[0] = tabbyte;
	tab = new String(bytes);
	go(inputFile, outputFile);
   }
   
   private void go(String inputFile, String outputFile){
	try{
	   FileReader fileReader = new FileReader(inputFile); 
	   BufferedReader reader = new BufferedReader(fileReader);
	   if(reader.ready()){
		buffer=buffer.append("delete from ip2country;\n");
		   String read = reader.readLine();
		while(read != null){
		   read=read.replaceAll("'", " ");
			StringTokenizer tokens = new StringTokenizer(read, ",", false);
		   String from = tokens.nextToken();
		   String to = tokens.nextToken();
		   // A few entries we are not interested in before we get to 
		   // the real country code
		   String countryCode1 = tokens.nextToken();
		   countryCode1 = tokens.nextToken();
		   countryCode1 = tokens.nextToken();
		   String countryCode2 = tokens.nextToken();
		   String countryCode3 = tokens.nextToken();
		   // Replace any occurences of UK with GB
		   if(countryCode1.equalsIgnoreCase("\"UK\""))
				countryCode1 = "GB";
		   if (countryCode3.contains(" (Formally Czechoslovakia)")){
			   countryCode3=countryCode3.replace(" (Formally Czechoslovakia)", "");
		   }
		   String line = "Insert into ip2country (ipfrom,ipto,iso1,iso2,country) values ("+from+","+to+","+countryCode1+","+countryCode2+","+countryCode3+");\n";
		   // Replace all quotes
		   line = line.replaceAll("\"", "'");
		   buffer = buffer.append(line);
		   read = reader.readLine();
		}
		fileReader.close();
		reader.close();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
		out.write(buffer.toString());
		out.flush();
		out.close();
	   }
	}
	catch(Exception e){
	   Dbutil.logger.error("Exception (remove all lines with # first!): ",e);
	}
   }

   public static void main(String[] args){
	if(args.length != 2){
	   System.out.println("Usage: java Ip2Country inputFile outputFile");
	   System.exit(1);
	}
	Ip2Country ip2country = new Ip2Country(args[0], args[1]);	
   }
}
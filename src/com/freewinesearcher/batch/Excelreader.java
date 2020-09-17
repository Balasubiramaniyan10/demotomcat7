package com.freewinesearcher.batch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.io.*;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.*;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.datafeeds.FeedContent;




public class Excelreader {
	public static String ReadUrl(String urlstring) {
		String Page="";
		try {
			InputStream input=getInputStream(urlstring);
			StringBuffer sb=parseExcel(new BufferedInputStream(input));
			input.close();
			Page=sb.toString();

		} catch (Exception exc){
			Dbutil.logger.warn("Cannot parse Excel file on url "+urlstring,exc);
			Page="Webpage unavailable";
		}

		return Page;
	}

	public static String ReadUrl(InputStream input, String urlstring) {
		String Page="";
		try {
			StringBuffer sb=parseExcel(new BufferedInputStream(input));
			Page=sb.toString();
//Dbutil.logger.info(Page);
		} catch (Exception exc){
			Dbutil.logger.warn("Cannot parse Excel file on url "+urlstring,exc);
			Page="Webpage unavailable";
		}

		return Page;
	}

	public static InputStream getInputStream(String urlstring){
		InputStream input=null;
		URL url =  null;
		HttpURLConnection urlcon;
		try {
			try {
				url = new URL(urlstring);
			} catch (Exception exc) {
				Dbutil.logger.warn("Foute URL " + urlstring);
			}
			Dbutil.logger.debug("Starting to get Excel sheet");
			urlcon = (HttpURLConnection) url.openConnection();
			urlcon.setRequestProperty("User-Agent",
			"Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
			input = urlcon.getInputStream();
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		}
		return input;
	}

	public static String ReadFile(File file){
		String Page="Webpage connot be read from Excel file";
		try {
			InputStream input=new FileInputStream(file);
			StringBuffer sb=parseExcel(input);
			input.close();
			Page=sb.toString();

		} catch (Exception exc){
			Dbutil.logger.error("Cannot parse Excel file, will not delete wines from this shop. Problem file= "+file.getName(),exc);
		}

		return Page;
	}
	public static FeedContent getasFeed(String urlstring){
		return getasFeed(getInputStream(urlstring));
	}
		
	public static FeedContent getasFeed(InputStream input){
		FeedContent feedContent = new FeedContent();
		String column;
		Cell cell;
		try{
			Workbook wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(0);
			// Iterate over each row in the sheet
			Iterator<Row> rows = sheet.rowIterator();
			while( rows.hasNext() ) { 
				com.freewinesearcher.common.datafeeds.Row feedrow=new com.freewinesearcher.common.datafeeds.Row();
				Row row = rows.next();
				int columncounter=65;
				int firstCellNum = row.getFirstCellNum();
				int lastCellNum = row.getLastCellNum();
				for( int i = firstCellNum; i < lastCellNum; i++ ) {
					cell = row.getCell((short) i);
					column=new Character((char)columncounter).toString();
					columncounter++;
					if( cell != null ) {
						feedrow.put(column, getCellValue(cell));
					}
				}
				if (feedrow.size()>1) feedContent.add(feedrow);
			}
		} catch (Exception exc){
			//Dbutil.logger.error("Could not read Excel file", exc);
			throw new java.lang.IllegalArgumentException();
		}
		
		return feedContent;
	}

	public static StringBuffer parseExcel(InputStream input) throws Exception{
		StringBuffer sb=new StringBuffer();
		
			Workbook wb = WorkbookFactory.create(input);
			//POIFSFileSystem fs = new POIFSFileSystem( input );
			//HSSFWorkbook wb = new HSSFWorkbook(fs);
			Sheet sheet = wb.getSheetAt(0);
			sb.append("<html><body><table>");
			// Iterate over each row in the sheet
			Iterator<Row> rows = sheet.rowIterator();
			while( rows.hasNext() ) {          
				//for (int j=0;j<100;j++) {   //For testing purpose as the whole sheet may be too big       
				sb.append("<tr>");
				Row row = rows.next();
				//System.out.println( "Row #" + row.getRowNum() );

				// Iterate over each cell in the row and print out the cell's content
				int firstCellNum = row.getFirstCellNum();
				int lastCellNum = row.getLastCellNum();
				for( int i = firstCellNum; i < lastCellNum; i++ ) {
					Cell cell = row.getCell((short) i);
					if( cell != null ) {
						sb.append("<td>"+getCellValue(cell)+"</td>");
						/*
						DecimalFormat df = new DecimalFormat("#0.00###");
						// Removed: original code tried to apply formatting. We just want the value.
						short dataFormatNr = cell.getCellStyle().getDataFormat();
						if (dataFormatNr > 0 && dataFormatNr < 
								HSSFDataFormat.getNumberOfBuiltinBuiltinFormats()) {
							Dbutil.logger.info(dataFormatNr+": "+HSSFDataFormat.getBuiltinFormat(dataFormatNr));
							df = new DecimalFormat(HSSFDataFormat.getBuiltinFormat(dataFormatNr));
						}
						else {
							df = new DecimalFormat("#0.###");
							String formatStr = wb.createDataFormat().getFormat(dataFormatNr);
							if (!formatStr.toUpperCase().equals("GENERAL")) {
								try
								{
									formatStr=formatStr.replaceAll("[^\\d#.,]","");
									df.applyPattern(formatStr);
								}
								catch (IllegalArgumentException e) {
									// the formatStr is not a decimal format (or not supported).

								}
							}
						}
						 
						switch ( cell.getCellType() ) {
						case Cell.CELL_TYPE_NUMERIC:
							sb.append("<td>"+df.format(cell.getNumericCellValue())+"</td>");
							//System.out.println( cell.getNumericCellValue() );
							break;
						case Cell.CELL_TYPE_STRING:
							sb.append("<td>"+cell.getStringCellValue()+"</td>");
							//System.out.println( cell.getStringCellValue() );
							break;
						default:
							//System.out.println( "unsuported cell type" );
							break;
						}		
						*/
					}
				}

				sb.append("</tr>");
			}
			sb.append("</table></body></html>");
			
			
		return sb;
	}
	
	public static String getCellValue(Cell cell){
		String value="";
			switch ( cell.getCellType() ) {
			case Cell.CELL_TYPE_NUMERIC:
				DecimalFormat df= new DecimalFormat("#0.00###");
				String format=cell.getCellStyle().getDataFormatString();
				try{
					if (format.indexOf(";")>0) format=format.substring(0, format.indexOf(";"));
					if (!format.contains("0")) format="#0.##";
					df=new DecimalFormat(format);} catch (Exception e){
					
				}
				value=df.format(cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_STRING:
				value=cell.getStringCellValue();
				break;
			default:
				break;
			}		
		return value;
	}

	public static void main (String[] args){

		System.out.println(ReadFile(new File("C:\\Workspace\\vinopedia\\testfeeds\\preisliste.xlsx")));
	}


}

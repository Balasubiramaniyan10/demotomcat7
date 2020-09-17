package com.freewinesearcher.common.datamining;

import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.batch.TableScraper;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;

public class Pricehistory {
	public Date startdate;
	public Date enddate;
	public int knownwineid=0;
	public int vintage=0;
	public HashMap<Integer,HashMap<String,Float>> pricecollection= new HashMap<Integer,HashMap<String,Float>>();
	public enum types {minimum,average,individual};
	public types type=types.minimum;
	public boolean hasdata=false;

	public Pricehistory(Date startdate, Date enddate, int knownwineid, int vintage, types type){
		this.startdate=startdate;
		this.enddate=enddate;
		this.knownwineid=knownwineid;
		this.vintage=vintage;
		this.type=type;
		getPrices();
	}


	private void getPrices(){
		ResultSet rs=null;
		float price;
		Connection con=Dbutil.openNewConnection();
		HashMap<String,Float> prices=new HashMap<String,Float>();
		int n=0;

		try{
			String DATE_FORMAT = "yyyy-MM-dd";
			SimpleDateFormat sdf =
				new SimpleDateFormat(DATE_FORMAT);
			String query="select * from wines where knownwineid="+knownwineid+" and vintage="+vintage+" and createdate<'"+sdf.format(enddate)+"' limit 1 union select * from history where knownwineid="+knownwineid+" and vintage="+vintage+" and lastupdated>='"+sdf.format(startdate)+"' and createdate<'"+sdf.format(enddate)+"' limit 1;";
			rs=Dbutil.selectQuery(query, con);
			if (rs.next()){
				hasdata=true;
				Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
				Dbutil.executeQuery("CREATE TEMPORARY TABLE `wijn`.`tempwine` select * from wines where knownwineid="+knownwineid+" and vintage="+vintage+" and createdate<'"+enddate+"' union select * from history where knownwineid="+knownwineid+" and vintage="+vintage+" and lastupdated>='"+startdate+"' and createdate<'"+enddate+"';", con);

				Calendar calendar = new GregorianCalendar();
				calendar.setTime(startdate);
				calendar.set(Calendar.HOUR,0);
				calendar.set(Calendar.MINUTE,0);
				calendar.set(Calendar.SECOND,0);
				calendar.set(Calendar.MILLISECOND,0);
				if (type.equals(types.individual)){
					try{
						int id=0;
						query="select distinct(id) as id from tempwine;";
						rs=Dbutil.selectQuery(query, con);
						while (rs.next()){
							id=rs.getInt("id");
							prices=new HashMap<String,Float>();
							calendar = new GregorianCalendar();
							calendar.setTime(startdate);
							calendar.set(Calendar.HOUR,0);
							calendar.set(Calendar.MINUTE,0);
							calendar.set(Calendar.SECOND,0);
							calendar.set(Calendar.MILLISECOND,0);
							while (calendar.getTime().before(enddate)){
								price=getPrice(sdf.format(calendar.getTime()),con,id);
								if (price>0){
									prices.put(sdf.format(calendar.getTime()),price);
								}
								calendar.add(Calendar.DATE, 1);
							}
							pricecollection.put(n, prices);
							n++;
						}
					} catch (Exception e){
						Dbutil.logger.error("Error while reading individual wineprices from DB",e);
					}
				} else {
					while (calendar.getTime().before(enddate)){
						prices.put(sdf.format(calendar.getTime()),getPrice(sdf.format(calendar.getTime()),con,0));
						calendar.add(Calendar.DATE, 1);
					}
					pricecollection.put(n, prices);
				}
			}
		} catch (Exception e){
			Dbutil.logger.error("Error while reading wineprices from DB",e);
		}
		Dbutil.executeQuery("DROP TABLE IF EXISTS `wijn`.`tempwine`;",con);
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);

	}

	private float getPrice(String date, Connection con, int id){
		ResultSet rs=null;
		String query="";
		float pricewines=0;
		float pricehistory=0;
		float price=0;
		try{
			if (type.equals(types.minimum)){
				query="Select min(priceeuroex) as priceeuroex from tempwine where date(createdate)<='"+date+"' and date(lastupdated)>='"+date+"';";
			}
			if (type.equals(types.average)){
				query="Select avg(priceeuroex) as priceeuroex from tempwine where date(createdate)<='"+date+"' and date(lastupdated)>='"+date+"';";
			}
			if (type.equals(types.individual)){
				query="Select priceeuroex as priceeuroex from tempwine where date(createdate)<='"+date+"' and date(lastupdated)>='"+date+"' and id="+id+";";
			}

			rs=Dbutil.selectQuery(query, con);
			if (rs.next()){
				pricewines=rs.getFloat("priceeuroex");
			}
			price=pricewines;


		} catch (Exception e){
			Dbutil.logger.error("Problem calculating price history for knownwineid= "+knownwineid,e);
		}
		Dbutil.closeRs(rs);
		return price;
	}


}

package com.freewinesearcher.batch;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;

import org.jfree.chart.ChartUtilities;
import org.jfree.data.time.Month;
import org.jfree.date.MonthConstants;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Shop;
import com.freewinesearcher.common.datamining.Chart;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;




public class SponsorStats {
	private int month;
	private int year;
	private byte[] image=null;
	private Chart chart=null;
	public  int featureduniqueip=0;
	public  int featured=0;
	public  int totaluniqueip=0;
	public  int bannerclickuniqueip=0;
	public int bannerclicks=0;
	public  int totalsponsoredclicks=0;
	public  int totalip=0;
	public int sponsoredpercentage=0;
	private static HashMap<String,SponsorStats> cache=new HashMap<String, SponsorStats>();
	

	public static String getMonthName(int month){
		return new java.text.DateFormatSymbols(Locale.US).getMonths()[month-1];
	}
	
	public static void refresh(){
		cache=new HashMap<String, SponsorStats>();
		
	}
	
	public static String getMonthString(int month) {
		try{
		DecimalFormat myFormatter = new DecimalFormat("00");
		return myFormatter.format(month);
		}catch (Exception e){
			Dbutil.logger.error("Invalid month "+month);
			return "";
		}
	}
	
	private SponsorStats(int month, int year) {
		super();
		this.month = month;
		this.year = year;
		getSponsorstats();
		cache.put(month+" "+year, this);
		org.jfree.date.MonthConstants s=null;
	}

	public static SponsorStats getSponsorStats(int month, int year){
		if (SponsorStats.cache!=null&&SponsorStats.cache.get(month+" "+year)!=null) return SponsorStats.cache.get(month+" "+year);
		return new SponsorStats(month,year);
	}

	public Chart getChart(){
		return chart;
	}
	
	public byte[] getImage(){
		return image;
	}
	
	private void getSponsorstats(){
		
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String query = "";
		String sponsoringshops="0";
		try {
			query="select group_concat(sponsors) as sponsors from (select sponsors from (select distinct(shopid) as sponsors, 1 from logging where shopid>0 and  type='Featured' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+year+"-"+getMonthString(month)+"-03') and bot=0 union select distinct(shopid) ,2 as sponsors from logging where shopid>0 and  type='Featured' and date between date('"+year+"-"+getMonthString(month)+"-27') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01') and bot=0 ) sel group by sponsors having count(*)>1) sel;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				sponsoringshops=rs.getString("sponsors");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as clicks, count(distinct(ip)) as visitors from logging where type='Featured' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01') and bot=0 and shopid in ("+sponsoringshops+");";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				featureduniqueip=rs.getInt("visitors");
				featured=rs.getInt("clicks");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as visitors,count(distinct(ip)) as uniquevisitors from logging where (type='Storepage' or type='Banner') and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01') and bot=0 and shopid in ("+sponsoringshops+") ;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				totaluniqueip=rs.getInt("uniquevisitors");
				totalip=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as visitors, count(distinct(ip)) as uniquevisitors from logging where type= 'Banner' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01') and bot=0 and shopid in ("+sponsoringshops+") ;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				bannerclickuniqueip=rs.getInt("uniquevisitors");
				bannerclicks=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as visitors from logging where (type= 'Banner' or type='Featured') and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01') and bot=0 and shopid in ("+sponsoringshops+") ;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				totalsponsoredclicks=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			chart=new Chart();
			chart.xscale=500;
			chart.yscale=240;
			Map<String, Integer> data=new LinkedHashMap<String, Integer>();
			if (totalip>0) sponsoredpercentage=(featured+bannerclicks)*100/totalip;
			data.put("Sponsored clicks",(int)featured+bannerclicks);
			data.put("Other/organic clicks",(int)(totalip-featured-bannerclicks));
			chart.createPieSponsors("Visitors of our sponsors in "+getMonthName(month)+" "+year, data);
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(out, chart.chart, chart.xscale, chart.yscale);
			out.close();
			image = out.toByteArray();
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		
		
	}
	

}

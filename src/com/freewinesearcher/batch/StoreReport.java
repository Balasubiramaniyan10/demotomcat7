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




public class StoreReport {
	public static String salt="rfjcf398-282tj895ef3443wef34rf3f3958";
	public Shop shop;
	private int month;
	private int year;
	public String report;
	public String body;
	public String subject;
	public Chart sponsorship=null;
	public byte[] image=null;
	private SponsorStats sponsorstats;
	public String startdate="";
	public String enddate="";
	

	public void setShop(int shopid) {
		shop=new Shop(shopid);

	}
	public int getMonth() {
		return month;
	}
	public static String getMonthName(int month){
		return new java.text.DateFormatSymbols(Locale.US).getMonths()[month-1];
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
	public String getPercentage(float fraction) {
		DecimalFormat myFormatter = new DecimalFormat("##0");
		return myFormatter.format(fraction*100);

	}
	public StoreReport(int month, int year) {
		super();
		this.month = month;
		this.year = year;
		sponsorstats=SponsorStats.getSponsorStats(month, year);
		
	}


	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i=0; i < b.length; i++) {
			result +=
					Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
	public static String toSHA1(byte[] convertme) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		}
		return byteArrayToHexString(md.digest(convertme));
	}

	public static boolean codeOK(String code, int shopid){
		return (toSHA1((shopid+salt).getBytes()).equals(code));
	}

	public static boolean unsubscribe(int shopid){
		return Dbutil.executeQuery("update shops set trafficreport=false where id="+shopid)>0;
	}

	public static String getStartDate(int month, int year){
		return year+"-"+getMonthString(month)+"-01";
	}
	public static String getEndDate(int month,int year){
		return (month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01";
	}
	public void generateReport(){
		int totaluniqueip=sponsorstats.totaluniqueip;
		if (totaluniqueip==0) totaluniqueip=1;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String query = "";
		int storepageunique=0;
		int storepage=0;
		int featured=0;
		int uniquefeatured=0;
		int bannerclicks=0;
		int bannersshown=0;
		int wines=0;
		int offers=0;
		float value=0;
		String mailedordervalue="0.00";
		int mailedorders=0;
		try {
			startdate=getStartDate(month, year);
			enddate=getEndDate(month, year);
			query = "select count(distinct(ip)) as visitors from logging where shopid="+shop.shopid+" and (type = 'Storepage' or type='Banner') and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				storepageunique=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as visitors from logging where shopid="+shop.shopid+" and (type = 'Storepage' or type='Banner')  and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				storepage=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select count(distinct(ip)) as visitors from logging where shopid="+shop.shopid+" and type='Featured' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				uniquefeatured=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as visitors from logging where shopid="+shop.shopid+" and type='Featured' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				featured=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select count((ip)) as visitors from logging where shopid="+shop.shopid+" and type = 'Banner' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				bannerclicks=rs.getInt("visitors");
				
			}
			Dbutil.closeRs(rs);
			query="select sum(wineprice) as value ,count(*) as visitors from logging  where logging.shopid="+shop.shopid+" and  (logging.type = 'Storepage' or logging.type='Store wineinfo') and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0 and wineid>0;";
			//query = "select sum(priceeuroex) as value from logging join wines on (logging.wineid=wines.id) where logging.shopid="+shop.shopid+" and  (logging.type = 'Storepage' or logging.type='Store wineinfo') and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				value=rs.getFloat("value");
				wines=rs.getInt("visitors");
			}
			Dbutil.closeRs(rs);
			query = "select format(sum(wineprice),2) as value from logging where logging.shopid="+shop.shopid+" and logging.type = 'Sent Shoppinglist' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				mailedordervalue=rs.getString("value");
			}
			Dbutil.closeRs(rs);
			query = "select count(*) as orders from logging where logging.shopid="+shop.shopid+" and logging.type = 'Sent Shoppinglist' and date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				mailedorders=rs.getInt("orders");
			}
			Dbutil.closeRs(rs);
			query = "select group_concat(id) as banners from banners where shopid="+shop.shopid+";";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				String banners=rs.getString("banners");
				if (banners!=null){
					banners="(^|,)("+banners.replaceAll(",", "|")+")(,|$)";
					Dbutil.closeRs(rs);
					query="select count(*) as shown from logging where date between date('"+year+"-"+getMonthString(month)+"-01') and date('"+(month==12?year+1:year)+"-"+(month==12?"01":getMonthString(month+1))+"-01')  and bot=0 and bannersshown regexp '"+banners+"';";
					//Dbutil.logger.info(query);
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						bannersshown=rs.getInt("shown");
					}
				}
				Dbutil.closeRs(rs);

			}
			query = "select count(*) as thecount from wines where shopid="+shop.shopid;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				offers=rs.getInt("thecount");
			}
			Dbutil.closeRs(rs);
			
			
		} catch (Exception e) {
			Dbutil.logger.error("Problem while generating report for shop "+shop.shopid, e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (storepage==0||storepageunique==0||shop.shopid==0) return ;
		StringBuffer report=new StringBuffer();
		report.append("<span style='font-family:Georgia;color:black;'>Dear sir/madam,<br/><br/>We would like to inform you about the listing of your store on <a href='www.vinopedia.com'>www.vinopedia.com</a>. Below you will find details of wine buyers visiting vinopedia that were directed to your site during the past month.<br/><br/><hr><span style='color:#4d0027;'>Vino</span>pedia Traffic Report for {0}, {1}<br/>");
		report.append("<ul>");
		report.append("<li>Your web site was visited {3} times by {2} unique visitors via Vinopedia.</li>");
		if (featured==0){
		} else {
			report.append("<li>{15} of these views ({5}%) came from \"Featured Merchant\" or banner clicks"+(shop.costperclick>0?"":" (*)")+".</li>");
		}
		report.append("<li>Your wines were viewed {6} times on Vinopedia with a total value of {7} (ex. VAT). For more details about what specific wines your audience is looking for please select the following <a href='https://www.vinopedia.com/viewselectedwines.jsp?store="+shop.shopid+"&amp;authorizationcode="+toSHA1((shop.shopid+salt).getBytes())+"'>link</a>.</li>");
		
		if (featured==0){
			report.append("<li>You can significantly improve traffic to your web site by <a href='https://www.vinopedia.com/retailers.jsp'>becoming a sponsoring store</a> on vinopedia. <b>Please be aware that in {1} our sponsors received {10} of their traffic from \"Featured Merchant\" links and banners</b></li>");
		}
		//report.append("<li>We emailed you {11} shopping lists from our visitors with a total value of {12}. </li>");
		if (bannersshown==0){
			//report.append("<li>You do not have any banners showing on Vinopedia (*)</li>");
		} else {
			report.append("<li>Your banners were displayed {9} times.</li>");
		}
		report.append("</ul>");
		if (shop.costperclick==0) {
			sponsorship=sponsorstats.getChart();
			image=sponsorstats.getImage();
			report.append("<img src='cid:stats'><br/><br/>");
			report.append("<hr/>We currently have {14} different wines listed from your store. Your store is listed under <a href=\"{16}\">{16}</a>, and all you wine offers will appear in the search results as well.");
			if (Dbutil.readIntValueFromDB("select * from datafeeds where shopid="+shop.shopid, "id")==0) report.append("The best way to insure all your wines are properly listed is by providing us with a datafeed. For more information see: <a href='https://www.vinopedia.com/datafeed.jsp'>https://www.vinopedia.com/datafeed.jsp</a>. ");
			report.append("<br/><br/>If you have any questions about your listing, please feel free to contact us at <a href='mailto:freelistings@vinopedia.com'>freelistings@vinopedia.com</a>.<br/><br/>");
			report.append("Kind regards,<br/<br/>Jeroen Starrenburg<br/>Commercial director vinopedia.com<br/>");
			report.append("<hr/>You are receiving this monthly report because your company is listed on Vinopedia.com. If you no longer wish to receive visitor reports, please click <a href='https://www.vinopedia.com/unsubscribereport.jsp?store="+shop.shopid+"&amp;authorizationcode="+toSHA1((shop.shopid+salt).getBytes())+"'>here</a> to unsubscribe.<br/>");
		} else {
			try {
				sponsorship=new Chart();
				sponsorship.xscale=400;
				sponsorship.yscale=300;
				Map<String, Integer> data=new LinkedHashMap<String, Integer>();
				data.put("Normal Search",storepage-featured-bannerclicks);
				data.put("Featured / Banners",featured+bannerclicks);
				//data.put("Banners",bannerclicks);
				sponsorship.create3dBar("Click report for "+shop.shopname, data);
				ByteArrayOutputStream out=new ByteArrayOutputStream();
				ChartUtilities.writeChartAsPNG(out, sponsorship.chart, sponsorship.xscale, sponsorship.yscale);
				out.close();
				image = out.toByteArray();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			report.append("<br/><br/><img src='cid:stats'><br/>");
			report.append("<hr/>If you have any questions about your listing, please feel free to contact us at <a href='mailto:sponsoredlistings@vinopedia.com'>sponsoredlistings@vinopedia.com</a>.<br/><br/>");
			report.append("Kind regards,<br/><br/>Jeroen Starrenburg<br/>Commercial director vinopedia.com<br/>");
		}


		report.append("</span>");
		body= MessageFormat.format(
				report.toString(),
				shop.shopname,//0
				new java.text.DateFormatSymbols(Locale.US).getMonths()[month-1]+" "+year,//1
				storepageunique,//2
				storepage,//3
				uniquefeatured, //4
				getPercentage((float)(featured+bannerclicks)/storepage),//5 
				wines,//6
				Webroutines.formatPrice((double)value,(double) value, shop.currency, "no"),//7
				bannerclicks,//8
				bannersshown,//9
				sponsorstats.sponsoredpercentage+"%",//10
				mailedorders,//11
				shop.currency+" "+mailedordervalue,//12
				featured, //13
				offers, //14
				(featured+bannerclicks), //15 total sponsored clicks
				"https://www.vinopedia.com/store/"+Webroutines.URLEncode(Webroutines.removeAccents(shop.shopname)).replaceAll("%2F", "/")+"/" //16 Store url
				);
		subject=MessageFormat.format("Vinopedia Traffic Report for {0}, {1}",//0
				shop.shopname,//0
				new java.text.DateFormatSymbols(Locale.US).getMonths()[month-1]+" "+year,//1
				storepageunique,//2
				storepage,//3
				uniquefeatured, //4
				getPercentage((float)(featured+bannerclicks)/storepage),//5 
				wines,//6
				Webroutines.formatPrice((double)value,(double) value, shop.currency, "no"),//7
				bannerclicks,//8
				bannersshown,//9
				sponsorstats.sponsoredpercentage+"%",//10
				mailedorders,//11
				shop.currency+" "+mailedordervalue,//12
				featured, //13
				offers, //14
				(featured+bannerclicks), //15 total sponsored clicks
				"https://www.vinopedia.com/store/"+Webroutines.URLEncode(Webroutines.removeAccents(shop.shopname)).replaceAll("%2F", "/")+"/" //16 Store url
				);


	}


	public static void sendreports(int month,int year){
		StoreReport report;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		String query = "";
		int shopid=0;
		report=new StoreReport(month,year);

		try {
			query = "select * from shops where email like '%@%' and trafficreport=1 and disabled=0;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				try{
					shopid=rs.getInt("id");
					String email=rs.getString("email");
					report.setShop(shopid);
					report.generateReport();
					if (report.body!=null){
						Emailer emailer=new Emailer();
						emailer.images=new HashMap<String, javax.activation.DataSource>();
						emailer.images.put("stats", new ByteArrayDataSource(report.image,"image/png"));
						emailer.bcc.add("archief@vinopedia.com");
						if (emailer.sendEmail("management@vinopedia.com",email, report.subject, report.body)){
							Webactionlogger wal=new Webactionlogger("Traffic report "+month+" "+year, "", email, "", "", "", 0, (float)0.0, (float)0.0, "", false, "", "", "","", (Double)0.0, 0);
							wal.logmenow();
							Dbutil.logger.info("Sending traffic report for "+report.shop.shopname+" period "+year+"-"+month+" to "+email);
							
						} else {
							Dbutil.logger.info("Could not send report for shop "+shopid);
						}
					} else {
						Dbutil.logger.info("Empty report for shop "+shopid);
					}
				}catch (Exception e){
					Dbutil.logger.error("Could not create traffic report for store "+shopid+" month "+getMonthString(month)+" "+year,e);
				}
			}
			Dbutil.closeRs(rs);
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public static void main(String[] args){
		int month=0;
		int year=0;
		if (args.length<2){
			year=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.YEAR);
			month=java.util.GregorianCalendar.getInstance().get(java.util.GregorianCalendar.MONTH);


		} else {
			month=Integer.parseInt(args[0]);
			year=Integer.parseInt(args[1]);
		}
		if (month==-1){
			year--;month=11;
		}
		Dbutil.logger.info("Sending traffic reports for "+getMonthName(month)+" "+year);
		try {
			sendreports(month,year);
		} catch (Exception e) {
			Dbutil.logger.error("Could not send traffic reports for month "+getMonthName(month)+" and year "+year);
		}
		Dbutil.logger.info("Finished sending traffic reports for "+getMonthName(month)+" "+year);

	}


}

package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.jfree.chart.plot.SpiderWebPlot;

import com.freewinesearcher.common.*;
import com.freewinesearcher.batch.*;

public class WineryMailing {
	String country;
	String sort1="wines desc";
	String sort2="";
	boolean hasemail=true;
	boolean alreadysent=false;
	String mailtext;
	String mailheader;
	int winery;
	int resultsperpage=10;
	int skip=0;
	String actie="list";
	static final String mailtextkey="winerytext";
	static final String mailheaderkey="wineryheader";
	public static ArrayList<String> countries=new ArrayList<String>();
	static{
		fillCountries();
	}
	
	
	public String doaction(){
		String result="";
		if (getActie().equals("updatetext")) saveMailtext();
		if (getActie().equals("updateheader")) saveMailheader();
		if (getActie().equals("sendmail")) result=sendMail();
		return result+" ";
	}
	
	private static void fillCountries() {
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select distinct(left(locale,instr(locale,',')-1)) as country from knownwines where numberofwines>10 order by country;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				countries.add(rs.getString("country").length()>0?rs.getString("country"):"All");
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		
	}

	private String sendMail() {
		Emailer emailer=new Emailer();
		emailer.bcc=new ArrayList<String>();
		emailer.bcc.add("jeroen@vinopedia.com");
		if (emailer.sendEmail("jeroen@vinopedia.com",Dbutil.readValueFromDB("select * from kbproducers where id="+winery, "email").replaceAll("^mailto:", ""), getMailHeaderComplete(),getMailTextComplete())){
			Dbutil.executeQuery("update kbproducers set storelocatormail=now() where id="+winery);
			return "Mail sent";
		} else {
			return "<h1>Error:Could not send email!</h1>";
		}
	}

	public String list(){
		StringBuffer sb=new StringBuffer();
		String query;
		ResultSet rs = null;
		boolean complete=false;
		Connection con = Dbutil.openNewConnection();
		try {
			String where="";
			if (hasemail) where+=" and not email is null and email !=''";
			if (alreadysent) {
				where+=" and not storelocatormail is null";
			} else {
				where+=" and storelocatormail is null";
			}
			if (getCountry().length()>0&&!getCountry().equals("All")) where+=" and country='"+getCountry()+"'";
			if (where.length()>4) where=" where "+where.substring(4);
			query = "select *,count(*) as difwines, sum(numberofwines) as totalnumberofwines from (select * from kbproducers "+where+" order by "+sort1+(sort2.length()>0?", "+sort2:"")+" limit "+skip+","+resultsperpage+") sel join knownwines on (knownwines.producer=sel.name) group by producer  order by "+sort1+(sort2.length()>0?", "+sort2:"")+";";
			
			//Dbutil.logger.info(query);
			rs = Dbutil.selectQuery(rs, query, con);
			sb.append("<table>");
			while (rs.next()) {
				if (winery==0) winery=rs.getInt("id");
				complete=false;
				if (rs.getString("address")!=null&&rs.getString("address").length()>10&&rs.getString("email")!=null&&rs.getString("email").length()>10&&rs.getString("email").contains("@")&&rs.getString("website")!=null&&rs.getString("website").length()>8&&rs.getString("telephone")!=null&&rs.getString("telephone").length()>6) complete=true;
				if (alreadysent){
					sb.append("<tr><td><h4  id='winery"+rs.getInt("id")+"'>"+(complete?"<font style='color:green'>":"")+rs.getString("name")+(complete?"</font>":"")+"</h4></td><td>"+rs.getString("email")+" <a href='/moderator/matchwineries.jsp?retrieveid="+rs.getInt("id")+"' target='_blank'>Edit</a></td><td>Sent on "+rs.getString("storelocatormail")+(rs.getDate("demopage")!=null?", demopage last viewed on "+rs.getString("demopage"):"")+"</td><td><form method='post' action='winerymailing.jsp#winery"+rs.getInt("id")+"'><input type='hidden' name='actie' value='sendmail'/><input type='hidden' name='alreadysent' value='"+alreadysent+"'/><input type='hidden' name='winery' value='"+rs.getInt("id")+"'/><input type='submit' value='Send again'/></form></td></tr>");
				} else {
					sb.append("<tr><td><h4  id='winery"+rs.getInt("id")+"'>"+(complete?"<font style='color:green'>":"")+rs.getString("name")+(complete?"</font>":"")+"</h4></td><td>"+rs.getString("email")+" <a href='/moderator/matchwineries.jsp?retrieveid="+rs.getInt("id")+"' target='_blank'>Edit</a></td><td><form method='post' action='winerymailing.jsp#winery"+rs.getInt("id")+"'><input type='hidden' name='actie' value='sendmail'/><input type='hidden' name='winery' value='"+rs.getInt("id")+"'/><input type='submit' value='Send'/></form></td><td>"+rs.getInt("difwines")+" different wines, "+rs.getInt("wines")+" offers.</td></tr>");
				}
			}
			if (!alreadysent) sb.append("<tr><td>Vinopedia (testmail)</td><td>jeroen@vinopedia.com</td><td><form method='post'><input type='hidden' name='actie' value='sendmail'/><input type='hidden' name='winery' value='0'/><input type='submit' value='Send'/></form></td><td></td></tr>");
			sb.append("</table>");
			
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return sb.toString();
	}
	
	public boolean isAlreadysent() {
		return alreadysent;
	}

	public void setAlreadysent(boolean alreadysent) {
		this.alreadysent = alreadysent;
	}

	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public boolean isHasemail() {
		return hasemail;
	}

	public void setHasemail(boolean hasemail) {
		this.hasemail = hasemail;
	}

	public int getResultsperpage() {
		return resultsperpage;
	}

	public void setResultsperpage(int resultsperpage) {
		this.resultsperpage = resultsperpage;
	}

	public String getCountry() {
		if (country==null) return "All";
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getSort1() {
		return sort1;
	}
	public void setSort1(String sort1) {
		this.sort1 = sort1;
	}
	public String getSort2() {
		return sort2;
	}
	public void setSort2(String sort2) {
		this.sort2 = sort2;
	}
	public String getMailtext() {
		return Dbutil.readValueFromDB("select * from config where configkey='"+mailtextkey+"';", "value");
	}
	public void setMailtext(String mailtext) {
		this.mailtext = mailtext;
	}
	
	public void saveMailtext(){
		Dbutil.executeQuery("update config set value='"+Spider.SQLEscape(mailtext)+"' where configkey='"+mailtextkey+"';");
	}
	
	public String getMailheader() {
		return Dbutil.readValueFromDB("select * from config where configkey='"+mailheaderkey+"';", "value");
	}
	public void setMailheader(String mailheader) {
		this.mailheader = mailheader;
	}
	
	public void saveMailheader(){
		Dbutil.executeQuery("update config set value='"+Spider.SQLEscape(mailheader)+"' where configkey='"+mailheaderkey+"';");
	}
	
	public int getWinery() {
		return winery;
	}
	public void setWinery(int winery) {
		this.winery = winery;
	}
	public String getActie() {
		if (actie==null) return "";
		return actie;
	}
	public void setActie(String actie) {
		this.actie = actie;
	}
	
	public String getMailTextComplete(){
        MessageFormat formatter = new MessageFormat("");
        formatter.applyPattern(getMailtext());
        String name=Dbutil.readValueFromDB("select * from kbproducers where id="+winery, "name");
        String code=Dbutil.readValueFromDB("select * from kbproducers where id="+winery, "edithashcode");
        Object[] messageArguments = {
        		name,
        	    Dbutil.readValueFromDB("select * from kbproducers where id="+winery, "email"),
        	    "<a href='https://www.vinopedia.com/storelocatordemo.jsp?id="+winery+"'>https://www.vinopedia.com/storelocatordemo.jsp?id="+winery+"</a>",
        	    "<a href='https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(name).replaceAll("%2F", "/").replace("&", "&amp;") + "'>https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(name).replaceAll("%2F", "/").replace("&", "&amp;")+"</a>",
        	    "<a href='https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(name).replaceAll("%2F", "/").replace("&", "&amp;") + "?accesscode="+code+"'>https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(name).replaceAll("%2F", "/").replace("&", "&amp;")+"?accesscode="+code+"</a>"
        	};

        return formatter.format(messageArguments);


	}
	public String getMailHeaderComplete(){
        MessageFormat formatter = new MessageFormat("");
        formatter.applyPattern(getMailheader());
        String name=Dbutil.readValueFromDB("select * from kbproducers where id="+winery, "name");
        Object[] messageArguments = {
        		name,
        	    Dbutil.readValueFromDB("select * from kbproducers where id="+winery, "email"),
        	    "<a href='https://www.vinopedia.com/storelocatordemo.jsp?id="+winery+"'>https://www.vinopedia.com/storelocatordemo.jsp?id="+winery+"</a>",
        	    "<a href='https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(name).replaceAll("%2F", "/").replace("&", "&amp;") + "'>https://www.vinopedia.com/winery/"+Webroutines.URLEncodeUTF8Normalized(name).replaceAll("%2F", "/").replace("&", "&amp;")+"</a>"
        	};

        return formatter.format(messageArguments);


	}
	
}

package com.searchasaservice.configmgr;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;

public class UrlSpider {

	int tenant=0;
	int shopid=0;
	int scrapelistrow=0;
	int spiderregexrow=0;
	String url="";
	String postdata="";
	String regex="";
	String filter="";
	String auto="";
	boolean ignorepagenotfound=false;
	boolean isvalid=false;
	String spiderregextype="normal";


	public UrlSpider(int shopid, int tenant, String url, String postdata,String auto) {
		super();
		this.shopid = shopid;
		this.tenant = tenant;
		this.url = url;
		this.postdata = postdata;
		this.scrapelistrow=0;
		this.spiderregexrow=0;
		this.regex="";
		this.filter="";
	}

	public static ArrayList<UrlSpider> getUrlSpiders(int shopid, int tenant,String auto){
		ArrayList<UrlSpider> list=new ArrayList<UrlSpider>();
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("select * from scrapelist where tenant="+tenant+" and shopid="+shopid+" and urltype='Master';", con);
			while (rs.next()){
				UrlSpider usp=new UrlSpider(shopid,tenant,rs.getInt("id"),auto);
				if (usp.isvalid) list.add(usp);
			}
		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return list;
	}

	public UrlSpider(int shopid, int tenant, int row, String auto) {
		super();
		UrlSpider usp=null;
		this.scrapelistrow=row;
		this.shopid = shopid;
		this.tenant = tenant;
		this.auto=auto;
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		try{
			rs=Dbutil.selectQuery("select * from "+auto+"shops where shopid="+shopid+";", con);
			if (rs.next()){
				this.ignorepagenotfound=rs.getBoolean("ignorepagenotfound");
			}
			rs=Dbutil.selectQuery("select * from "+auto+"scrapelist where tenant="+tenant+" and shopid="+shopid+" and id="+scrapelistrow+" and urltype='Master';", con);
			if (rs.next()){
				this.url=rs.getString("url");
				this.postdata=rs.getString("postdata");
				this.spiderregexrow=rs.getInt("regexspiderrow");
			}
			rs=Dbutil.selectQuery("select * from "+auto+"spiderregexrow where tenant="+tenant+" and shopid="+shopid+" and id="+spiderregexrow+" and urltype='Master';", con);
			if (rs.next()){
				this.filter=rs.getString("filter");
				this.regex=rs.getString("regex");
				this.isvalid=true;
			}

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public String save(){
		String query;
		ResultSet rs=null;
		java.sql.Connection con=Dbutil.openNewConnection();
		String succes = "";
		int i;
		try{
			// Pad url with baseurl if necessary
			rs = Dbutil.selectQuery("SELECT Baseurl " +
					"from "+auto+"shops where id='"+shopid+"';",con);
			rs.next();
			String completeUrl=url;
			if (!url.startsWith(rs.getString("Baseurl"))&&!url.startsWith("pdf:")){
				completeUrl=rs.getString("Baseurl")+url;
			}
			completeUrl=Spider.SQLEscape(completeUrl);

			// Get encoding (to do: in separate routine)
			rs = Dbutil.selectQuery("SELECT Encoding from "+auto+"shops where id='"+shopid+"';",con);
			if (!rs.next()||rs.getString("Encoding").equals("")){
				String encoding=Spider.getHtmlEncoding(completeUrl);
				i = Dbutil.executeQuery(
						"Update "+auto+"shops set encoding = '"+encoding+"' where id="+shopid+";");
			}
			int hashcode=(completeUrl+postdata).hashCode();
			//if (Type.equals("Email")) hashcode=(Shopid+"").hashCode();

			if (this.scrapelistrow>0){
				i = Dbutil.executeQuery("Update "+auto+"tablescraper set url='"+completeUrl+"', postdata='"+Spider.SQLEscape(postdata)+"', hashcode='"+hashcode+"' where id="+scrapelistrow+";");
				if (i!=0) succes="OK";

				Dbutil.executeQuery("update "+auto+"spiderregex set regex='"+regex+"', Filter='"+filter+"' where " +
						"Shopid="+shopid+" and id="+spiderregexrow+";");

			} else {
				this.spiderregexrow=Dbutil.executeQuery("Insert into "+auto+"spiderregex (tenant,Shopid, Regex, Filter,spidertype) " +
						"values ("+tenant+",'"+shopid+"', '"+regex+"', '"+filter+"', '"+spiderregextype+"');");
				if (this.spiderregexrow>0){
					Dbutil.executeQuery("delete from "+auto+"scrapelist where shopid="+shopid+" and url='"+url+"' and postdata='"+Spider.SQLEscape(postdata)+"' and id!="+scrapelistrow);
					i = Dbutil.executeQuery(
							"Insert into "+auto+"scrapelist (tenant,spiderregex,Url, Postdata, Headerregex, regex, scrapeorder,parenturl,Shopid, URLType, tablescraper, Status,hashcode) " +
							"values ("+tenant+","+this.spiderregexrow+",'"+completeUrl+"', '"+Spider.SQLEscape(postdata)+"', '', '','','', '"+shopid+"', 'Master', '0', 'Ready',"+hashcode+");");
					if (i!=0) succes="OK";
				}
			}
			Dbutil.executeQuery(
					"Update "+auto+"shops set ignorepagenotfound='"+ignorepagenotfound+"' where " +
					"id="+shopid+";");

		} catch (Exception e){
			Dbutil.logger.error("Problem: ",e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return succes;
	}

	public int getScrapelistrow() {
		return scrapelistrow;
	}

	public void setScrapelistrow(int scrapelistrow) {
		this.scrapelistrow = scrapelistrow;
	}

	public int getSpiderregexrow() {
		return spiderregexrow;
	}

	public void setSpiderregexrow(int spiderregexrow) {
		this.spiderregexrow = spiderregexrow;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPostdata() {
		return postdata;
	}

	public void setPostdata(String postdata) {
		this.postdata = postdata;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getAuto() {
		return auto;
	}

	public void setAuto(String auto) {
		this.auto = auto;
	}

	public boolean isIgnorepagenotfound() {
		return ignorepagenotfound;
	}

	public void setIgnorepagenotfound(boolean ignorepagenotfound) {
		this.ignorepagenotfound = ignorepagenotfound;
	}

	public boolean isIsvalid() {
		return isvalid;
	}

	public void setIsvalid(boolean isvalid) {
		this.isvalid = isvalid;
	}

	public int getTenant() {
		return tenant;
	}

	public int getShopid() {
		return shopid;
	}

	public void setTenant(int tenant) {
		this.tenant = tenant;
	}

	public void setShopid(int shopid) {
		this.shopid = shopid;
	}

	public void setSpiderregextype(String spiderregextype) {
		this.spiderregextype = spiderregextype;
	}








}

package com.freewinesearcher.online;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;

public class Banner {
	public int id;
	public int shopid;
	public int partnerid;
	public String html;
	public float payperclick;
	public int views;
	public int clicks;
	public boolean active=false;
	public String link="";
	public int width;
	public int height;
	public int imageid=0;
	public String targetcountries="";
	public static final String countries="US,CA,GB,DE,FR,CH,NL,BE,IT,ES";
	public static final int maxsize=65; //Size in kb
	
	public static Banner load(int id, int partnerid){
		Banner banner=null;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select * from banners where id="+id+" and partnerid="+partnerid;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				banner=new Banner();
				banner.partnerid=partnerid;
				banner.id=id;
				banner.setPayperclick(rs.getFloat("payperclick"));
				banner.setActive(rs.getBoolean("active"));
				banner.setTargetcountry(rs.getString("Country"));
				banner.setHtml(rs.getString("html"));
				banner.setViews(rs.getInt("views"));
				banner.setClicks(rs.getInt("clicks"));
				banner.setLink(rs.getString("link"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return banner;
	}
	
	public void save(){
		if (id==0){
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			try {
				Dbutil.executeQuery("insert into banners (id,shopid,partnerid,html,payperclick,views,clicks,active,link,country,hsize,vsize,source) values (" +
						id+","+shopid+","+partnerid+",'"+Spider.SQLEscape(html)+"',"+payperclick+",0,0,"+active+",'"+Spider.SQLEscape(link)+"','"+Spider.SQLEscape(targetcountries)+"',"+width+","+height+",'Upload');",con);
				rs=Dbutil.selectQuery("select LAST_INSERT_ID() as id;", con);
				rs.next();
				id=rs.getInt("id");
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
			
		} else {
			Dbutil.executeQuery("Update banners set active="+active+", html='"+Spider.SQLEscape(html)+"', payperclick="+payperclick+",link='"+Spider.SQLEscape(link)+"'" +
					", country='"+Spider.SQLEscape(targetcountries)+"' where id="+id+" and partnerid="+partnerid+";");
		}
	}
	public void setImage(String type, InputStream is){
		Connection con=Dbutil.openNewConnection();
		if (imageid==0){
			String sqlStatement = "insert into images (contenttype, width, height, data, partnerid) values ('"+type+"', 0, 0, ?, '"+partnerid+"')";
			try {
				PreparedStatement pstmt = con.prepareStatement(sqlStatement);
				pstmt.setBinaryStream(1, is, is.available());
				pstmt.executeUpdate();
				ResultSet rs=Dbutil.selectQuery("select LAST_INSERT_ID() as imageid;",con);
				if (rs.next()){
					imageid=rs.getInt("imageid");
					this.html="<img src='/images/gen/"+imageid+"."+type+"' alt='Banner' />";
					this.save();
				}
			
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
		} else {
			String sqlStatement = "update images set contenttype='"+type+"', data=? where id="+imageid+";";
			try {
				PreparedStatement pstmt = con.prepareStatement(sqlStatement);
				pstmt.setBinaryStream(1, is, is.available());
				pstmt.executeUpdate();
			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			}
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public float getPayperclick() {
		return payperclick;
	}
	public void setPayperclick(float payperclick) {
		this.payperclick = payperclick;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public int getClicks() {
		return clicks;
	}
	public void setClicks(int clicks) {
		this.clicks = clicks;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTargetcountry() {
		return targetcountries;
	}
	public void setTargetcountry(String country) {
		this.targetcountries = country;
	}
	
	

}

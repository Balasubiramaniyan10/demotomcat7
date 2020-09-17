/*
 * Created on 18-mrt-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.online;


import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;



/**
 * @author Jasper
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Search {
	public int id;
	public String username="";
	public String description="";
	public String name="";
	public String vintage="";
	public String country="All";
	public int pricemin=0;
	public int pricemax=0;
	public String rareold="";
	public String cheapest="";
	
	
	/**
	 * Retieves the saved searches 
	 * 
	 * @param id: identifier of the row
	 * @param username: username of the user who is logged in
	 */
	public Search(int id, String username){
		Connection con = Dbutil.openNewConnection();
		try {
			ResultSet rs;
			
			rs = Dbutil.selectQuery("Select * from search where id="+id+" and username='"+username+"';", con);
			if (rs.next()){
				this.id=id;
				username=rs.getString("username");
				description=rs.getString("description");
				name=rs.getString("name");
				vintage=rs.getString("vintage");
				country=rs.getString("country");
				pricemin=Integer.valueOf(rs.getString("pricemin")).intValue();
				pricemax=Integer.valueOf(rs.getString("pricemax")).intValue();
				rareold=rs.getString("rareold");
				if (rareold.equals("1")) {rareold="true";} else {rareold="false";};
				cheapest=rs.getString("cheapest");
			}			
			
		} catch (Exception exc){
			exc.printStackTrace();
		}
	Dbutil.closeConnection(con);	
	}
	
	public static int delete(int id, String username){
		int i=0;
		String query;
		query="DELETE from search where id="+id+" AND username='"+username+"';";
		i=Dbutil.executeQuery(query);
	return i;
	}
	
	
}

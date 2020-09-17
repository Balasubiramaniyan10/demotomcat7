/*
 * Created on 18-mrt-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.common;

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

import com.freewinesearcher.online.Search;




public class Searchset {
	public Search search[];
	
	/**
	 * Retrieves all saved searches from a user
	 * @param username
	 */
	public Searchset(String username){
		Connection con = Dbutil.openNewConnection();
		try {
			ResultSet rs;
			rs = Dbutil.selectQuery("Select * from search where username='"+username+"' order by description,id;", con);
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			search = new Search[count];
			int i=0;
			while (rs.next()){
				this.search[i]= new Search(Integer.valueOf(rs.getString("id")).intValue(),username);
				i++;
			}			
		} catch (Exception exc){
			exc.printStackTrace();
		}
	Dbutil.closeConnection(con);	
	}
}

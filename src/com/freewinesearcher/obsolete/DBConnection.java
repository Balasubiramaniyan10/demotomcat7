package com.freewinesearcher.obsolete;

import java.sql.*;
import javax.sql.DataSource;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.jdbcplus.*;

import com.freewinesearcher.common.Dbutil;

import java.util.ArrayList;

/*
 * This class was created to test memory leaks. 
 * It turns out that when Dbutil is used properly, it causes no memory leak.
 * If close() or finalize() is not called, connections may stay open,
 * so this is not the perfect solution either.
 */
public class DBConnection {
	Statement stmt=null;
	ResultSet rs=null;
	Connection con=Dbutil.openNewConnection();

	protected void finalize(){
		try{
			if (this.rs!=null) this.rs.close();
		} catch (Exception e){}	
		try{
			if (this.stmt!=null) this.stmt.close();
		} catch (Exception e){}	
		try{
			if (this.con!=null) {
				//Dbutil.logger.info("Closing connection con "+con.toString());
				Dbutil.closeConnection(this.con);
			}

		} catch (Exception e){}
		rs=null;
		stmt=null;
		con=null;
	}

	public void close(){
		finalize();
	}
	
	public void selectQuery(String query) {
		try{
			stmt = con.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);


		}catch( Exception e ) {
			Dbutil.logger.error("Could not execute query: "+query);
			Dbutil.logger.error("Stack trace: ",e);

		}
	}	

}



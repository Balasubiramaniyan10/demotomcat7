/*
 * Created on 24-mrt-2006
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freewinesearcher.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.jdbcplus.JDBCPoolConnectionHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
//import org.apache.commons.dbcp.BasicDataSource;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.freewinesearcher.online.housekeeping.BotDetector;

/**
 * @author Jasper
 *
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Dbutil implements JDBCPoolConnectionHandler {
	public static BasicDataSource dataSource = setupDataSource();

	public static Logger logger = Logger.getLogger(Dbutil.class);
	public static Logger functionallogger = Logger.getLogger("wijnzoeker");
	// public static Logger perflogger = Logger.getLogger(Dbutil.class);
	static boolean dummy = initializeLogger();
	public static boolean trackconnections = false;

	/**
	 * 
	 */
	public Dbutil() {
		super();
	}

	public static Connection openNewConnection() {
		return openNewConnection(true);
	}

	public static Connection openNewConnection(boolean errorlogging) {

		// Connection connection = null;
		// String dburl;
		Connection conn = null;
		try {
			if (System.getProperty("Batch") == null) {
				if (dataSource.getNumActive() == 90) {
					trackconnections = false;
					ConnectionTracker.clear();
				}
				if (dataSource.getNumActive() == 100)
					trackconnections = true;
				if (trackconnections && dataSource.getNumActive() == 120) {
					trackconnections = false;
					System.out.println("Database connections:");
					if (ConnectionTracker.maxconnectionmap.size() > 1) {
						for (int i : ConnectionTracker.maxconnectionmap.keySet()) {
							try {
								System.out.println(ConnectionTracker.maxconnectionmap.get(i));
							} catch (Exception e) {
							}
						}
					}
					ConnectionTracker.clear();
					BotDetector b = BotDetector.getInstance();
					b.detect();
				}

				if (dataSource.getNumActive() > Wijnzoeker.maxDataSourceConnections) {
					if (dataSource.getNumActive() - Wijnzoeker.maxDataSourceConnections < 11) {
						// Dbutil.logger.warn("Only
						// "+(dataSource.getMaxActive()-Wijnzoeker.maxDataSourceConnections)+" DB
						// connections left!!!");
					}
					// if (dataSource.getMaxActive()-2*Wijnzoeker.maxDataSourceConnections==0){
					// //half of the connections used
					// BotDetector b=BotDetector.getInstance(); Problem: detect itself is
					// recursively calling openNewConnection --> eindeloze loop
					// b.detect();
					// }
				}
			}

			try {
				conn = dataSource.getConnection();
			} catch (Exception e) {
				// if first try fails, try again
			}
			if (conn == null)
				conn = dataSource.getConnection();
			if (trackconnections) {
				try {
					StackTraceElement[] Stack = Thread.currentThread().getStackTrace();
					String trace = "Opened at " + (new java.sql.Timestamp(new java.util.Date().getTime())).toString()
							+ "<br/>";
					for (int i = 3; i < 8; i++) {
						if (Stack.length > i + 1)
							trace += "at " + Stack[i].toString() + "<br/>";
					}
					trace += "__________<br/>";
					ConnectionTracker.track(conn.hashCode(), trace, ConnectionTracker.actions.ADD);
				} catch (Exception e) {
				}
			}
			// Dbutil.logger.debug("Opening new connection, total open="+opened);
			// Dbutil.logger.debug("Stack calling procedure:",ex);

		} catch (Exception e) {
			if (errorlogging) {
				Dbutil.logger.warn("Fout bij creeeren DB connectie: ", e);
				Dbutil.logger.info(dataSource.getNumActive() + " active conections.", e);
			} else {
				try {
					System.err.println("Fout bij creeeren DB connectie. " + e.toString());
					System.err.println("Caused by: " + e.getCause().getLocalizedMessage());
				} catch (Exception f) {
				}
			}
		}
		return conn;
	}

	public Connection getConnection() {

		// Only used for JDBC logging
		return openNewConnection(false);
	}

	public Connection getConnection(String a, String b, String c) {
		return openNewConnection(false);
		// Only used for JDBC logging
		/*
		 * Connection conn = null; try{ conn = dataSource.getConnection(); opened++; }
		 * catch (Exception e) {
		 * //Dbutil.logger.error("Fout bij creeeren DB connectie: ",e); } return conn;
		 */
	}

	public void freeConnection(Connection con) {
		closeConnection(con, false);
		/*
		 * try{ //Dbutil.logger.info("Closing connection, total open="+opened);
		 * con.close(); opened--; } catch (Exception e) {
		 * Dbutil.logger.error("Could not close DB connection. ",e); }
		 */
	}

	public static void closeConnection(Connection con) {
		closeConnection(con, true);
	}

	public static void closeConnection(Connection con, boolean errorlogging) {
		try {
			// Dbutil.logger.info("Closing connection "+con.toString()+", total
			// open="+opened);
			if (con != null) {
				int hash = con.hashCode();
				con.close();
				if (trackconnections)
					ConnectionTracker.track(hash, null, ConnectionTracker.actions.REMOVE);

			}

		} catch (Exception e) {
			if (errorlogging)
				Dbutil.logger.warn("Could not close DB connection. ", e);
		}
		con = null;
	}

	public static ResultSet selectQuery(String query, Connection con) {
		Statement stmt;
		ResultSet rs = null;
		try {
			if (con != null) {
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(query);
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not execute query: " + query);
			Dbutil.logger.error("Stack trace: ", e);

		}
		return rs;
	}

	public static ResultSet selectQueryForUpdate(String query, Connection con) {
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery(query);

		} catch (Exception e) {
			Dbutil.logger.error("Could not execute query: " + query);
			Dbutil.logger.error("Stack trace: ", e);

		}
		return rs;
	}

	public static ResultSet selectQuery(ResultSet rs, String query, Connection con) {
		Statement stmt;
		Dbutil.closeRs(rs);
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);

		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		}
		return rs;
	}

	public static ResultSet selectUpdatableQuery(String query, Connection con) {
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);

		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		}
		return rs;
	}

	public static ResultSet selectQueryFromMemory(String query, String table, Connection con) {
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery("Select count(*) as cnt from " + table + ";");
			if (rs != null && rs.next()) {
				if (rs.getInt("cnt") == 0) {
					fillTable(table, con);
				}
			}
			rs = stmt.executeQuery(query);

		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		}
		return rs;
	}

	public static void fillTable(String table, Connection con) {
		String query = "";
		ResultSet rs = null;
		try {
			if ("materializedadvice".equals(table) || "materializedadvicenew".equals(table)) {
				query = "lock tables " + table
						+ " write,wines read,ratinganalysis read, shops read, winetypecoding read, grapes read,pqratio read,knownwines read;";
				Dbutil.executeQuery(query, con);
				Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery("Select count(*) as cnt from " + table + ";");
				if (rs != null && rs.next()) {
					if (rs.getInt("cnt") == 0) {
						if ("materializedadvice".equals(table)) {
							Dbutil.logger.info("table materializedadvice was empty, filling it.");
							Thread.dumpStack();
						}
						Dbutil.logger.info("Filling " + table + "");
						query = "alter table " + table + " disable keys;";
						Dbutil.executeQuery(query, con);
						query = "insert ignore into " + table
								+ " select wines.id,wines.knownwineid, wines.vintage, wines.priceeuroex, ratinganalysis.rating, ((size/0.75)*pqratio.price/priceeuroex) as pqratio, wines.lft,wines.rgt,shops.countrycode, winetypecoding.typeid as winetypecode, wines.shopid,grapes.id from wines left join ratinganalysis on (wines.knownwineid=ratinganalysis.knownwineid and wines.vintage=ratinganalysis.vintage and ratinganalysis.author='FWS') left join pqratio on (ratinganalysis.rating=pqratio.rating) join shops on (wines.shopid=shops.id) left join knownwines on (wines.knownwineid=knownwines.id) natural left join winetypecoding left join grapes on (knownwines.grapes=grapes.grapename) where wines.size=0.75 and wines.knownwineid>0;";
						Dbutil.executeQuery(query, con);

					}
				}
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.logger.info("Enabling keys " + table + "");
			Dbutil.executeQuery("unlock tables;", con);
			query = "alter table " + table + " enable keys;";
			Dbutil.executeQuery(query, con);
			Dbutil.logger.info("Done refreshing " + table + "");
			Dbutil.closeRs(rs);
		}

	}

	public static void refreshMaterializedAdvice() {
		String query = "";
		Connection con = Dbutil.openNewConnection();

		try {
			query = "create table if not exists materializedadvicenew like materializedadvice;";
			Dbutil.executeQuery(query, con);
			query = "truncate table materializedadvicenew;";
			Dbutil.executeQuery(query, con);
			query = "ALTER TABLE materializedadvicenew ENGINE=MEMORY";
			Dbutil.executeQuery(query, con);
			fillTable("materializedadvicenew", con);
			query = "rename table materializedadvice to materializedadviceold, materializedadvicenew to materializedadvice;";
			Dbutil.executeQuery(query, con);
			query = "drop table materializedadviceold;";
			Dbutil.executeQuery(query, con);
		} catch (Exception e) {

		} finally {
			Dbutil.closeConnection(con);
		}
	}

	public static void renewTable(String table) {
		// String query = "";
		Connection con = Dbutil.openNewConnection();
		try {
			if ("materializedadvice".equals(table)) {
				refreshMaterializedAdvice();
				/*
				 * query="delete from "+table+";"; Dbutil.executeQuery(query,con);
				 * query="ALTER TABLE "+table+" ENGINE=MEMORY"; Dbutil.executeQuery(query,con);
				 * Dbutil.selectQueryFromMemory("select count(*) from "+table+";", table, con);
				 */
			}
		} catch (Exception e) {

		} finally {
			Dbutil.closeConnection(con);
		}
	}

	public static ResultSet selectQueryRowByRow(String query, Connection con) {
		// Statement stmt;
		ResultSet rs = null;
		try {
			/*
			 * stmt = con.createStatement( //ResultSet.FETCH_FORWARD,
			 * ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			 * stmt.setFetchSize(Integer.MIN_VALUE); rs = stmt.executeQuery(query);
			 */
			PreparedStatement stat = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			stat.setFetchSize(Integer.MIN_VALUE);
			rs = stat.executeQuery();

		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		}
		return rs;
	}

	public static ResultSet callProcedure(String query, Connection con) {
		CallableStatement stmt;
		ResultSet rs = null;
		try {
			stmt = con.prepareCall(query);
			rs = stmt.executeQuery();
		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		}
		stmt = null;
		return rs;
	}

	public static int executeQuery(String query) {
		int result = 0;
		Statement stmt = null;
		Connection con = openNewConnection();
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			result = stmt.executeUpdate(query);
			stmt.close();
		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				Dbutil.logger.warn("Could not close statement: " + query);
				Dbutil.logger.warn("Stack trace: ", e);

			}
		}
		stmt = null;
		Dbutil.closeConnection(con);
		return result;
	}

	public static int executeQuery(String query, Connection con) {
		int result = 0;
		Statement stmt = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			result = stmt.executeUpdate(query);
			stmt.close();
		} catch (Exception e) {
			Dbutil.logger.warn("Could not execute query: " + query);
			Dbutil.logger.warn("Stack trace: ", e);

		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				Dbutil.logger.warn("Could not close statement: " + query);
				Dbutil.logger.warn("Stack trace: ", e);

			}
		}
		stmt = null;
		return result;
	}

	public static int executeQueryWithExceptions(String query, Connection con) throws SQLException {
		int result = 0;
		Statement stmt;
		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		result = stmt.executeUpdate(query);
		stmt.close();
		stmt = null;
		return result;
	}

	public static String readValueFromDB(String query, String fieldname) {
		ResultSet rs = null;
		String object = "";
		Connection con = openNewConnection();
		try {
			rs = Dbutil.selectQuery(query, con);
			if (rs.next()) {
				object = rs.getString(fieldname);
			}
		} catch (Exception e) {
			Dbutil.logger.warn("Could not readRowFromDB with query " + query + " and fieldname " + fieldname);
			Dbutil.logger.warn("Stack trace: ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return object;
	}

	public static int readIntValueFromDB(String query, String fieldname) {
		ResultSet rs = null;
		int object = 0;
		Connection con = openNewConnection();
		try {
			rs = Dbutil.selectQuery(query, con);
			if (rs.next()) {
				object = rs.getInt(fieldname);
			}
		} catch (Exception e) {
			Dbutil.logger.warn("Could not readRowFromDB with query " + query + " and fieldname " + fieldname);
			Dbutil.logger.warn("Stack trace: ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return object;
	}

	public static Connection getPRDConnection() {
		BasicDataSource pds = new BasicDataSource();
		pds.setDriverClassName("com.mysql.jdbc.Driver");
		pds.setUsername("vpapp");
		pds.setPassword("gyh(74%bbGSsw");
		pds.setUrl(
				"jdbc:mysql://localhost:3336/wijn?jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull");
		pds.setMaxIdle(150);
		pds.setMaxWaitMillis(10000);
		pds.setTestOnBorrow(true);
		pds.setTimeBetweenEvictionRunsMillis(60000);
		pds.setMinEvictableIdleTimeMillis(5 * 60 * 1000);
		try {
			return pds.getConnection();
		} catch (SQLException e) {
			Dbutil.logger.warn("Could not create connection to PRD");
			return null;
		}
	}

	public static Connection getDEVConnection() {
		BasicDataSource pds = new BasicDataSource();
		pds.setDriverClassName("com.mysql.jdbc.Driver");
		pds.setUsername("vpapp");
		pds.setPassword("gyh(74%bbGSsw");
		pds.setUrl(
				"jdbc:mysql://localhost:3336/wijn?jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull");
		pds.setMaxIdle(150);
		pds.setMaxWaitMillis(10000);
		pds.setTestOnBorrow(true);
		pds.setTimeBetweenEvictionRunsMillis(60000);
		pds.setMinEvictableIdleTimeMillis(5 * 60 * 1000);
		try {
			return pds.getConnection();
		} catch (SQLException e) {
			Dbutil.logger.warn("Could not create connection to PRD");
			return null;
		}
	}

	public static BasicDataSource setupDataSource() {
		BasicDataSource ds = null;
		try {
			InitialContext ic = new InitialContext();
			javax.naming.Context xmlContext = (javax.naming.Context) ic.lookup("java:comp/env"); // thats everything
																									// from the
																									// context.xml and
																									// from the global
																									// configuration
			ds = (BasicDataSource) xmlContext.lookup("jdbc/DB");
			System.out.println("Datasource loaded from context.xml");
		} catch (Exception e) {
			System.err.println("Could not load datasource");
			System.err.println(e.getLocalizedMessage());
			System.out.println("Loading it the old-fashioned way");
			ds = new BasicDataSource();
			ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setUsername("vpapp");
			ds.setPassword("gyh(74%bbGSsw");
			ds.setUrl(
					"jdbc:mysql://localhost:3336/wijn?jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull&useUnicode=yes&characterEncoding=UTF8");
			ds.setMaxIdle(150);
			ds.setMaxWaitMillis(10000);
			ds.setTestOnReturn(true);
			ds.setTestWhileIdle(true);
			ds.setTestOnBorrow(true);
			ds.setTimeBetweenEvictionRunsMillis(60000);
			ds.setMinEvictableIdleTimeMillis(5 * 60 * 1000);
			// ds.setLogAbandoned(true);
			// ds.setRemoveAbandoned(true);
			// ds.setRemoveAbandonedTimeout(60);
			// if (ds==null) System.err.println("Cannot connect to database to get pool.");
		}
		return ds;
	}

	public static void changeLogLevel() {
		if (Wijnzoeker.debug) {
			logger.setLevel((Level) Level.DEBUG);

		} else {
			logger.setLevel((Level) Level.toLevel(Configuration.FWSConfig.getProperty("loglevel", "INFO")));
		}
	}

	public static boolean initializeLogger() {
		if (logger != null && logger.getAllAppenders() != null)
			logger.removeAllAppenders();
		String pattern = "%d{ISO8601} %p %m ";
		pattern += "@ %l ";
		pattern += "<Class> %C ";
		pattern += "%n";

		String smspattern = "%m@%l %C ";

		PatternLayout layout = new PatternLayout(pattern);
		new PatternLayout(smspattern);
		ConsoleAppender consapp = null;
		DailyRollingFileAppender appender = null;
		RollingFileAppender appendererror = null;

		try {
			consapp = new ConsoleAppender(layout);
			logger.addAppender(consapp);
			// functionallogger.addAppender(consapp);

			// org.apache.log4j.jdbcplus.JDBCAppender DBAppender = new
			// org.apache.log4j.jdbcplus.JDBCAppender();
			// DBAppender.setConnectionHandler(new Dbutil());
			// DBAppender.setSql("INSERT INTO errorlog (Date, Message, Trace, Level) VALUES
			// ('@TIMESTAMP@', '@MSG@', '@THROWABLE@', '@PRIO@');");
			// DBAppender.setThreshold(Level.ERROR);
			// logger.addAppender(DBAppender);
			// functionallogger.addAppender(DBAppender);

			// if (false && "PRD".equals(Wijnzoeker.serverrole)) {
			// LimitingEvaluator smseval = new LimitingEvaluator();
			// smseval.maxmessages = 3;
			// org.apache.log4j.net.SMTPAppender SMTPapp2 = new
			// org.apache.log4j.net.SMTPAppender(smseval);
			// SMTPapp2.setSMTPHost("localhost");
			// SMTPapp2.setFrom("do_not_reply@vinopedia.com");
			// SMTPapp2.setTo(Configuration.smsmailaccount);
			// SMTPapp2.setSubject("");
			// SMTPapp2.setLayout(smslayout);
			// SMTPapp2.setThreshold(Level.ERROR);
			// SMTPapp2.activateOptions();
			// logger.addAppender(SMTPapp2);
			// functionallogger.addAppender(SMTPapp2);
			// org.apache.log4j.net.SMTPAppender SMTPapp = new
			// org.apache.log4j.net.SMTPAppender(
			// new LimitingEvaluator());
			// SMTPapp.setSMTPHost("smtp.wanadoo.nl");
			// SMTPapp.setFrom("do_not_reply@vinopedia.com");
			// SMTPapp.setTo("jasper.hammink@vinopedia.com");
			// SMTPapp.setSubject("Error message from Vinopedia");
			// SMTPapp.setLayout(layout);
			// SMTPapp.setThreshold(Level.ERROR);
			// SMTPapp.activateOptions();
			// // logger.addAppender(SMTPapp);
			// org.apache.log4j.net.SMTPAppender functionalSMTPapp = new
			// org.apache.log4j.net.SMTPAppender();
			// functionalSMTPapp.setSMTPHost("smtp.wanadoo.nl");
			// functionalSMTPapp.setFrom("do_not_reply@vinopedia.com");
			// functionalSMTPapp.setTo("jasper.hammink@vinopedia.com");
			// functionalSMTPapp.setSubject("Functional message from Vinopedia");
			// functionalSMTPapp.setLayout(layout);
			// functionalSMTPapp.setThreshold(Level.INFO);
			// functionalSMTPapp.activateOptions();
			// functionallogger.addAppender(functionalSMTPapp);
			// }

			if (System.getProperty("Batch") != null && System.getProperty("Batch").equals("true")) {
				appender = new DailyRollingFileAppender(layout, Configuration.FWSConfig.getProperty("logdir")
						+ Configuration.FWSConfig.getProperty("batchlogfile"), "'.'yyyy-MM-dd'.log'");
				appendererror = new RollingFileAppender(layout, Configuration.FWSConfig.getProperty("logdir")
						+ Configuration.FWSConfig.getProperty("errorlogfile"), false);
				// appendererror.setMaxFileSize("1MB");
				// appendererror.setMaxBackupIndex(10);
				appendererror.setThreshold(Level.ERROR);
				logger.addAppender(appender);
				logger.addAppender(appendererror);
				functionallogger.addAppender(appender);
				functionallogger.addAppender(appendererror);

			}

		} catch (Exception e) {
			System.out.println("Probleem: " + e);
			e.printStackTrace();
		}

		if (Wijnzoeker.debug) {
			logger.setLevel((Level) Level.DEBUG);
			functionallogger.setLevel((Level) Level.DEBUG);

		} else {
			logger.setLevel((Level) Level.toLevel(Configuration.FWSConfig.getProperty("loglevel", "INFO")));
			functionallogger.setLevel((Level) Level.toLevel(Configuration.FWSConfig.getProperty("loglevel", "INFO")));

		}
		// Dbutil.logger.info("Logprops:"+Wijnzoeker.FWSConfig.getProperty("logdir")+Wijnzoeker.FWSConfig.getProperty("batchlogfile"));
		return true;
	}

	public static Double getPriceFactorex(String Shopid) {
		String currency;
		Double rate;
		String country;
		int exvat;
		Double vat;
		Double pricefactor = 1.0;
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {

			// Get the general price information about this shop, like VAT and exchange
			// rate,
			// and determine the factor to calculate price in Euro ex VAT.

			rs = Dbutil.selectQuery("Select currency, countrycode, exvat from shops where id=" + Shopid + ";", con);
			if (rs.next()) {
				currency = rs.getString("currency");
				exvat = rs.getInt("exvat");
				if (exvat == 2) {
					exvat = 1;
				}
				country = rs.getString("countrycode");
				rs.close();
				rs = Dbutil.selectQuery("Select VAT from vat where countrycode='" + country + "';", con);
				rs.next();
				vat = rs.getDouble("VAT");
				rs.close();
				rs = Dbutil.selectQuery("Select rate from currency where currency='" + currency + "';", con);
				rs.next();
				rate = rs.getDouble("rate");

				pricefactor = rate / (1 + (1 - exvat) * (vat / 100));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not calculate pricefactor: ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return pricefactor;
	}

	public static Double getVat(String countrycode) {
		double vat = 0;
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {

			// Get the general price information about this shop, like VAT and exchange
			// rate,
			// and determine the factor to calculate price in Euro ex VAT.

			rs = Dbutil.selectQuery("Select VAT from vat where countrycode='" + countrycode + "';", con);
			rs.next();
			vat = rs.getDouble("VAT");
		} catch (Exception e) {
			Dbutil.logger.error("Could not retrieve vat for countrycode " + countrycode, e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return vat;
	}

	public static Double getPriceFactorin(String Shopid) {
		String currency;
		Double rate;
		String country;
		int exvat;
		Double vat;
		Double pricefactor = 1.0;
		Connection con = Dbutil.openNewConnection();
		ResultSet rs = null;

		try {

			// Get the general price information about this shop, like VAT and exchange
			// rate,
			// and determine the factor to calculate price in Euro incl VAT.

			rs = Dbutil.selectQuery("Select currency, countrycode, exvat from shops where id=" + Shopid + ";", con);
			if (rs.next()) {
				currency = rs.getString("currency");
				exvat = rs.getInt("exvat");
				if (exvat == 2)
					exvat = 1;
				country = rs.getString("countrycode");
				rs.close();
				rs = Dbutil.selectQuery("Select VAT from vat where countrycode='" + country + "';", con);
				rs.next();
				vat = rs.getDouble("VAT");
				rs.close();
				rs = Dbutil.selectQuery("Select rate from currency where currency='" + currency + "';", con);
				rs.next();
				rate = rs.getDouble("rate");

				pricefactor = rate * (1 + (exvat * (vat / 100)));
			}
		} catch (Exception e) {
			Dbutil.logger.error("Could not calculate pricefactor: ", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return pricefactor;
	}

	public static String getErrorHTML(int rows) {
		ResultSet rs;
		StringBuffer sb = new StringBuffer();
		// ResultSet wines;
		// int bestmatch;
		// String scrapedwine;
		// String whereclause;
		// String[] literalterm;
		// String[] literaltermexclude;
		// String fulltext;
		// String literalsearch;
		// String knownwine;
		String query;
		Connection con = Dbutil.openNewConnection();
		// String historywhereclause = "";
		// String html = "";
		try {
			query = "select * from errorlog where handled=0 order by id desc limit " + rows + ";";
			rs = Dbutil.selectQuery(query, con);
			sb.append("<table id='errortable'>");
			if (rs != null)
				while (rs.next()) {
					sb.append("<tr id='error" + rs.getString("id") + "'><td><a href=\"javascript:updateValue("
							+ rs.getString("id") + ",'handle')\">Delete</a><td>" + rs.getString("Date")
							+ "</td><td colspan='2'>" + rs.getString("Message"));
					if (!rs.getString("Trace").equals("")) {
						String[] trace = rs.getString("Trace").split("\n");
						for (int i = 0; i < trace.length; i++) {
							sb.append("<br>" + trace[i]);
						}
					}
					sb.append("</td></tr>");

				}
			sb.append("</table>");

		} catch (Exception exc) {
			Dbutil.logger.error("Problem: ", exc);
		}
		Dbutil.closeConnection(con);
		return sb.toString();
	}

	public static void closeRs(ResultSet rs) {
		try {
			if (rs != null) {
				if (rs.getStatement() != null) {
					Statement st = rs.getStatement();
					rs.close();
					st.close();
				}
				rs = null;
			}
		} catch (Exception e) {
		}
	}

	static class LimitingEvaluator implements TriggeringEventEvaluator {
		private int interval = 60; // in seconds
		public int maxmessages = 10;

		public boolean isTriggeringEvent(LoggingEvent event) {
			String query = "select (TIMESTAMPDIFF(second,date,'"
					+ new java.sql.Timestamp(new java.util.Date().getTime()) + "')>" + interval
					+ ") as event from errorlog order by id desc limit " + maxmessages + ",1;";
			// Dbutil.logger.info(query);
			if (Dbutil.readIntValueFromDB(query, "event") == 0) {
				return false;
			}
			return true;
		}

	}

}

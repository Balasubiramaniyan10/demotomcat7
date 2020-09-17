package com.freewinesearcher.online.housekeeping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.freewinesearcher.batch.sms.Sms;

public class ProblemSolver {
	private static final int OK = 0;
	private static final int UNKNOWNISSUE = 1;
	// private static final int HANGINGQUERYSOLVED = 1;
	private static final int HANGINGQUERY = 11;
	private static final int DBDOWN = 12;
	private static final int TOMCATDOWN = 13;
	// private static final int INTERNETDOWN = 14;
	private static final int TOMCATHANG = 15;
	Connection con = null;
	ArrayList<String> hangingqueries = new ArrayList<String>();

	public ProblemSolver() {
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
				+ " ProblemSolver started. Trying to get connection to DB...");
		getConnection();
		if (con == null) {
			System.out.println(
					new java.sql.Timestamp(new java.util.Date().getTime()) + " Cannot get a connection to MySQL");
			if (!canViewLocalhost()) {
				System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Cannot get Petrus");
				System.exit(DBDOWN);
			} else {
				System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
						+ " But I can view Petrus page, assuming everything is OK");
				System.exit(OK);
			}
		} else {
			System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Got connection");
			try {
				// if (countVisitors(con, 2) == 0) {
					if (!isTomcatUp()) {
						System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
								+ " Tomcat server is not running");
						System.exit(TOMCATDOWN);
					}
					// else if (false && !hasInternetConnection()) {
					// System.out.println(new java.sql.Timestamp(new
					// java.util.Date().getTime())
					// + " No internet connection available");
					// System.exit(INTERNETDOWN);
					// }
					else if (cannotKillHangingConnections()) {
						System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
								+ " Cannot kill a hanging query");
						System.exit(HANGINGQUERY);
					} else if (!canViewLocalhost()) {
						System.out
								.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Cannot Find wines");
						System.exit(TOMCATHANG);
					} else if (countVisitors(con, 5) == 0) {
						System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
								+ " Cannot determine the problem");
						System.exit(UNKNOWNISSUE);
					} else {
						System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Checked and OK.");
					}
				// } else {
				//	System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Checked and OK.");
				// }
			} catch (Exception e) {
			}
		}
		// close database connection
		close(con);

		System.exit(OK);
	}

	private void close(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
			} finally {
				con = null;
			}
		}
	}

	public boolean cannotKillHangingConnections() {
		int id = 0;
		HangingQueryKiller hqk = null;
		HangingQuery hq;
		while ((hq = new HangingQuery(con)).id != id) {
			id = hq.id;
			if (id > 0) {
				hangingqueries.add(hq.query);
				if (hqk == null) {
					hqk = new HangingQueryKiller();
					hqk.con = con;
				}
				hqk.id = id;
				Thread t = new Thread(hqk);
				t.start();
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
				}
				if (new HangingQuery(con).id != id) {
					System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
							+ " Killed hanging query: " + hq.query);
				} else {
					System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
							+ " Could not kill hanging query: " + hq.query);
					return true;
				}
			}
		}
		return false;
	}

	public boolean canViewLocalhost() {
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Checking Petrus...");
		TimedReadPage trp = new TimedReadPage();
		trp.urlstring = "https://www.vinopedia.com/wine/Petrus";
		Thread t = new Thread(trp);
		t.start();
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
		}
		if (trp.html.length() > 100) { // trp.html.contains("results")
			System.out.println("Found Petrus...");
			return true;
		} else {
			trp = new TimedReadPage();
			trp.urlstring = "http://www.vinopedia.com/wine/Petrus";
			t = new Thread(trp);
			t.start();
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
			if (trp.html.length() > 100) { // trp.html.contains("results")
				System.out.println("Found Petrus...");
				return true;
			}			
		}
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Did not find Petrus...");
		return false;
	}

	public boolean isTomcatUp() {
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Checking favicon...");
		TimedReadPage trp = new TimedReadPage();
		trp.urlstring = "https://www.vinopedia.com/favicon.ico";
		Thread t = new Thread(trp);
		t.start();
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
		}
		if (trp.html.length() > 100) {
			System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Found favicon...");
			return true;
		} else {
			trp = new TimedReadPage();
			trp.urlstring = "http://www.vinopedia.com/favicon.ico";
			t = new Thread(trp);
			t.start();
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
			if (trp.html.length() > 100) {
				System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Found favicon...");
				return true;
			}	
		}
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Did not find favicon...");
		return false;
	}

	public boolean hasInternetConnection() {
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Checking Internet connection...");
		TimedReadPage trp = new TimedReadPage();
		trp.urlstring = "http://www.google.com";
		Thread t = new Thread(trp);
		t.start();
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
		}
		if (trp.html.contains("Google"))
			return true;
		return false;
	}

	public void getConnection() {
		CustomConnectionPool ccp = new CustomConnectionPool();
		Thread t = new Thread(ccp);
		t.start();
		try {
			Thread.sleep(10000);
		} catch (Exception e) {

		}
		if (ccp.con != null) {
			con = ccp.con;
		}
	}

	public static class HangingQuery {
		int id = 0;
		String query = "";

		public HangingQuery(Connection con) {
			ResultSet rs = null;
			try {
				rs = selectQuery(
						"SELECT * FROM information_schema.PROCESSLIST where command!='Sleep' and db='wijn' and time>=180 order by id;",
						con);
				if (rs.next()) {
					id = rs.getInt("ID");
					query = rs.getString("info");
					System.out.println(new java.sql.Timestamp(new java.util.Date().getTime())
							+ " Hanging query with id=" + id + ": " + query);
				}
			} catch (Exception e) {

			} finally {
				closeRs(rs);
			}

		}
	}

	public static class TimedReadPage implements Runnable {
		public String html = "";
		public String urlstring = "";

		public void run() {
			html = readPage(urlstring);

		}

	}

	public static class CustomConnectionPool implements Runnable {
		public Connection con = null;

		public void run() {
			BasicDataSource dataSource = setupRootDataSource();
			try {
				con = dataSource.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static class HangingQueryKiller implements Runnable {
		int id;
		Connection con;

		public void run() {
			if (con != null) {
				executeQuery("kill " + id, con);

			}
		}
	}

	public static BasicDataSource setupRootDataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername("vpapp");
		ds.setPassword("gyh(74%bbGSsw");
		ds.setUrl("jdbc:mysql://localhost:3336/wijn?jdbcCompliantTruncation=false");
		ds.setMaxIdle(150);
		ds.setMaxWaitMillis(10000);
		ds.setTestOnBorrow(true);
		ds.setTimeBetweenEvictionRunsMillis(60000);
		ds.setMinEvictableIdleTimeMillis(5 * 60 * 1000);
		return ds;
	}

	public static ResultSet selectQuery(String query, Connection con) {
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Could not execute query: " + query);
			// System.out.println("Stack trace: ",e);
		}
		return rs;
	}

	public static void closeRs(ResultSet rs) {
		Statement st = null;
		try {
			if (rs != null) {
				st = rs.getStatement();

				// close result set
				rs.close();

				// close statement
				if (st != null) {
					st.close();
				}
			}
		} catch (Exception e) {
		} finally {
			rs = null;
			st = null;
		}
	}

	public static int executeQuery(String query, Connection con) {
		int result = 0;
		Statement stmt;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			result = stmt.executeUpdate(query);
			// stmt.close();
		} catch (Exception e) {
			System.out.println("Could not execute query: " + query);
			// System.out.println("Stack trace: ",e);
		}
		// stmt = null;
		return result;
	}

	public static int countVisitors(Connection con, int minutes) {
		System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " Counting visitors in last "
				+ minutes + " mins");
		if (con == null) {
			return 0;
		}
		int visits = 0;
		String query = "select count(*) as thecount from logging where date>=DATE_SUB(now(),INTERVAL " + minutes
				+ " MINUTE);";
		ResultSet rs = null;
		try {
			rs = ProblemSolver.selectQuery(query, con);
			if (rs.next()) {
				visits = rs.getInt("thecount");
			}
			if (visits == 0) {
				System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " " + visits
						+ " visitors in last " + minutes + " min");
			} else {
				System.out.println(new java.sql.Timestamp(new java.util.Date().getTime()) + " " + visits
						+ " visitors in last " + minutes + " mins");

			}
		} catch (Exception e) {
		} finally {
			ProblemSolver.closeRs(rs);
		}
		return visits;
	}

	public static String readPage(String urlstring) {
		String trimmedurl = urlstring;
		String Page = "";
		URL url = null;

		HttpURLConnection urlcon = null;
		// long lastmodified;
		String inputLine;
		StringBuffer sb = new StringBuffer();
		try {
			url = new URL(trimmedurl);
		} catch (Exception exc) {
		}
		try {
			urlcon = (HttpURLConnection) url.openConnection();
			urlcon.setConnectTimeout(10000);
			String encoding = "ISO-8859-1";
			BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream(), encoding));
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
				sb.append("\n");
			}
			in.close();

			Page = sb.toString();
			sb = null;
		} catch (Exception exc) {
			// Dbutil.logger.error("",exc);
			urlcon.disconnect();
		}

		return Page;
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			Sms sms = new Sms();
			sms.setSms(args[0]);
			sms.send();
			System.exit(OK);
		} else {
			new ProblemSolver();
		}
	}
}
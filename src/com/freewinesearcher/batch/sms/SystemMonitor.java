package com.freewinesearcher.batch.sms;

import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.housekeeping.ProblemSolver;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.client.*;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.extensions.When;
import java.sql.ResultSet;





public class SystemMonitor implements Runnable {
	private static SystemMonitor instance=null;
	private static boolean muststop=false;
	private String userName=Configuration.gmailusername;
	private String userPassword=Configuration.gmailpassword;
	public String title="Vinopedia is down";
	public String recoveredtitle="Vinopedia is online again";
	int defaultinterval=5;
	public boolean novisitors=false;




	private SystemMonitor(){
		ThreadGroup threadgroup = new ThreadGroup("SystemMonitor");
		new Thread(threadgroup,this).start();
	}

	public static SystemMonitor getInstance(){
		if(instance == null) {
			instance = new SystemMonitor();
		}
		return instance;
	}

	public void run(){
		CalendarEventEntry FWSEntry=null;
		CalendarService myService=null;
		TimeZone tz = TimeZone.getTimeZone("Europe/Madrid");

		try {
			URL feedUrl = new URL("http://www.google.com/calendar/feeds/"
					+ userName + "/private/full");
			myService = new CalendarService("FWSSMS");
			//Dbutil.logger.info("username "+userName+" password "+userPassword);
			myService.setUserCredentials(userName, userPassword);
			Query myQuery = new Query(feedUrl);
			myQuery.setFullTextQuery(title);


			while (!muststop){
				int interval=Dbutil.readIntValueFromDB("select * from config where configkey='monitorinterval';", "value");
				if (interval==0) interval=defaultinterval;
				try	{
					boolean monitor=false;
					try{
						monitor=(Boolean.parseBoolean(Dbutil.readValueFromDB("select * from config where configkey='systemmonitor';", "value")));

					} catch (Exception e){}
					if (monitor){
						//checkVisitors(null);
						CalendarEventFeed myResultsFeed = myService.query(myQuery, 
								CalendarEventFeed.class);
						List<CalendarEventEntry> events=myResultsFeed.getEntries();
						if (events.size()==1){
							for (CalendarEventEntry entry:myResultsFeed.getEntries()) {
								Calendar cal = GregorianCalendar.getInstance();
								cal.add(Calendar.MINUTE, interval*2+1);
								DateTime startTime = new DateTime(cal.getTime(), tz); 
								cal.add(Calendar.MINUTE, 1);
								DateTime endTime = new DateTime(cal.getTime(), tz);  
								cal = GregorianCalendar.getInstance();
								cal.add(Calendar.MINUTE, interval);
								for (When times:entry.getTimes()){
									if ((new DateTime(cal.getTime(), tz)).compareTo(times.getStartTime())>0){
										Sms newEntry=new Sms();
										newEntry.sms=recoveredtitle;
										newEntry.send();
									}
									times.setStartTime(startTime);
									times.setEndTime(endTime);
								}
								entry.update();
							}
						} else { //Schedule new event
							for (CalendarEventEntry entry:myResultsFeed.getEntries()) {
								entry.delete();
							}
							Sms newEntry=new Sms();
							newEntry.minutesahead=interval*2+1;
							newEntry.sms=title;
							newEntry.send();
						}
					}
				}
				catch (Exception exc)
				{
					Dbutil.logger.warn("Error while monitoring FWS connectivity. ",exc);
				}

				try{
					Thread.sleep(60*1000*interval);
				} catch (Exception e){}
			}
		}catch(Exception e){
			Dbutil.logger.error("Problem with Systemmonitor",e);
		}
	}
	
	public void checkVisitors(Connection con){
		int visits=countVisitors(con);
		if (visits==0){
			if (novisitors){
				// Do nothing
			} else {
				Sms sms=new Sms();
				sms.sms="No visits on Vinopedia in the last 10 minutes!";
				sms.send();
				novisitors=true;
			}
		} else {
			if (novisitors){
				Sms sms=new Sms();
				sms.sms="Visits again on Vinopedia.";
				sms.send();
				novisitors=false;
			} else {
				// Do nothing
			}
			
		}
	}


	public static int countVisitors(Connection con){
		int visits=0;
		String query="select count(*) as thecount from logging where date>=DATE_SUB(now(),INTERVAL 5 MINUTE);";
		if (con!=null){
			ResultSet rs=null;
			Statement stmt;
			try{
				stmt = con.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE,
						ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(query);
				if (rs.next()) visits=rs.getInt("thecount");
			}catch( Exception e ) {
			}
			ProblemSolver.closeRs(rs);
		} 
		return visits;
	}

	
	
	public void stop(){
		muststop=true;
	}



}

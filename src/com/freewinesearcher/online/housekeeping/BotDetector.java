package com.freewinesearcher.online.housekeeping;

import java.sql.Connection;
import java.sql.ResultSet;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;




public class BotDetector implements Runnable {
	private static BotDetector instance=null;
	private static boolean muststop=false;
	ThreadGroup threadgroup;


	public void destroy(){
		muststop=true;
		threadgroup.interrupt();
	}

	private BotDetector(){
		threadgroup = new ThreadGroup("BotDetector");
		new Thread(threadgroup,this).start();
	}

	public static BotDetector getInstance(){
		if(instance == null) {
			instance = new BotDetector();
		}
		return instance;
	}

	public void run(){
		Dbutil.logger.info("Starting Vinopedia Bot Detector");

		try {
			while (!muststop){
				detect();
				try{
					Thread.sleep(60*1000);
				} catch (Exception e){}
			}
		}catch(Exception e){
			Dbutil.logger.error("Problem with BotDetector",e);
		}
	}
	
	public void detect(){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try	{
			rs=Dbutil.selectQuery("select distinct ip,ipaddress from (select ip,count(*) from logging where date>DATE_SUB(now(),INTERVAL 1 MINUTE) and bot=0 and type like '%search%' and hostname not like '%googlebot.com' group by ip having count(*)>60) abuse left join ipblocks on (abuse.ip=ipblocks.ipaddress) having ipblocks.ipaddress is null;", con);
			while (rs.next()){
				Dbutil.executeQuery("insert into ipblocks (ipaddress,status) values ('"+rs.getString("ip")+"','Blocked');");
				Dbutil.functionallogger.info("Added ip address "+rs.getString("ip")+" to blocked ip list. ");
			}
			rs=Dbutil.selectQuery("select distinct ip,ipaddress from (select ip,count(*) from logging where date>DATE_SUB(now(),INTERVAL 4 HOUR) and bot=0 and type like '%search%'  and hostname not like '%googlebot.com' group by ip having count(*)>800) abuse left join ipblocks on (abuse.ip=ipblocks.ipaddress) having ipblocks.ipaddress is null;", con);
			while (rs.next()){
				Dbutil.executeQuery("insert into ipblocks (ipaddress,status) values ('"+rs.getString("ip")+"','Blocked');");
				Dbutil.functionallogger.info("Added ip address "+rs.getString("ip")+" to blocked ip list. ");
			}
			rs=Dbutil.selectQuery("select distinct ip,ipaddress from (select ip,count(*) from logging where date>DATE_SUB(now(),INTERVAL 15 MINUTE) and bot=0 and type like '%search%'  and hostname not like '%googlebot.com' group by ip having count(*)>200) abuse left join ipblocks on (abuse.ip=ipblocks.ipaddress) having ipblocks.ipaddress is null;", con);
			while (rs.next()){
				Dbutil.executeQuery("insert into ipblocks (ipaddress,status) values ('"+rs.getString("ip")+"','Blocked');");
				Dbutil.functionallogger.info("Added ip address "+rs.getString("ip")+" to blocked ip list. ");
			}
			
		}
		catch (Exception exc)
		{
			Dbutil.logger.warn("Error while executing Bot Detection. ",exc);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		
	}
	

	public void stop(){
		muststop=true;
	}



}



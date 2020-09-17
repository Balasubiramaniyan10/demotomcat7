package com.freewinesearcher.online.housekeeping;

import com.freewinesearcher.common.Dbutil;

public class Searchstats implements Runnable{

	private static Searchstats instance=null;
	private static boolean muststop=false;
	ThreadGroup threadgroup;
	
	private Searchstats(){
		threadgroup = new ThreadGroup("Searchstats");
		new Thread(threadgroup,this).start();
	}
	public void destroy(){
		muststop=true;
		threadgroup.interrupt();
	}

	
	public static Searchstats getInstance(){
		if(instance == null) {
			instance = new Searchstats();
		}
		return instance;
	}

	public void run(){
		try {
			while (!muststop){
				if (newDay()) fillData();
				try{
					Thread.sleep(60*1000);
				} catch (Exception e){}
			}
		}catch(Exception e){
			Dbutil.logger.error("Problem with BotDetector",e);
		}
		
	}
	
	public boolean newDay(){
		int days=Dbutil.readIntValueFromDB("select datediff(sysdate(),(select max(date) from searchstats)) as diff;", "diff");
		return (days>1);
	}
	
	public void fillData(){
		String date=Dbutil.readValueFromDB("select date_add((max(date)),interval 1 day) as date from searchstats;", "date");
		// Dbutil.logger.info("Filling searchstats for "+date);
		Dbutil.executeQuery("insert into searchstats select date(date) as date,count(distinct(ip)) as visitors,producer,knownwineid from logging join knownwines on (knownwineid=knownwines.id)  where date >= date('"+date+"') and  date < date_add(date('"+date+"'),interval 1 day) group by knownwineid,date(date) order by date,producer,knownwineid;");
	}
}

package com.freewinesearcher.online.housekeeping;

import com.freewinesearcher.batch.sms.SystemMonitor;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Currency;
import com.freewinesearcher.online.StoreInfo;
import com.freewinesearcher.online.WineAdvice;

public class CacheManager implements Runnable{
	private static CacheManager instance=null;
	public boolean forcerefresh=true;
	int defaultinterval=5;

	private CacheManager(){
		ThreadGroup threadgroup = new ThreadGroup("CacheManager");
		Dbutil.logger.info("Starting Cache Manager");
		new Thread(threadgroup,this).start();
	}

	public static CacheManager getInstance(){
		if(instance == null) {
			instance = new CacheManager();
		}
		return instance;
	}



	private static void buildCache(){
		Dbutil.logger.info("Cache Manager is refreshing caches");
		Thread t = new Thread(new WineAdvice());
		t.start();
		Thread t2=new Thread(new com.freewinesearcher.online.Hemabox());
		t2.start();
		StoreInfo.renewCache();
		Currency.clearCache();
	}


	@Override
	public void run() {
		int interval=Dbutil.readIntValueFromDB("select * from config where configkey='monitorinterval';", "value");
		if (interval==0) interval=defaultinterval;
		while(true){
			try	{
				boolean monitor=false;
				try{
					monitor=(Boolean.parseBoolean(Dbutil.readValueFromDB("select * from config where configkey='finishedbatch';", "value")));
					if (monitor||forcerefresh){
						monitor=false;
						Dbutil.executeQuery("update config set value='false' where configkey='finishedbatch';");
						forcerefresh=false;
						buildCache();
					}
				} catch(Exception e){
					Dbutil.logger.info("Problem in CacheManager: ",e);
				}	Thread.sleep(60*1000*interval);
			} catch(Exception e){
				Dbutil.logger.info("Problem in CacheManager: ",e);
			}
		}

	}

}

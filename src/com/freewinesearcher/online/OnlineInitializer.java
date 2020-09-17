package com.freewinesearcher.online;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.housekeeping.BotDetector;
import com.freewinesearcher.online.housekeeping.Searchstats;

public class OnlineInitializer implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		QueueLogger.getLogger().destroy();
		BotDetector.getInstance().destroy();
		Searchstats.getInstance().destroy();
	
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		new Wijnzoeker();	
		Dbutil.logger.info("Initialized!");
	}
	

}

package com.freewinesearcher.common;

import com.google.gdata.data.DateTime;

public class PerformanceLogger {
	private String log;
	private long time;
	private long totaltime;
	
	
	public PerformanceLogger() {
		super();
		log="";
		time=DateTime.now().getValue();
		totaltime=0;
	}


	public String getLog() {
		return log;
	}


	public void log(String action) {
		this.log+=action+": "+(DateTime.now().getValue()-time)+"ms. ";
		totaltime+=(DateTime.now().getValue()-time);
		time=DateTime.now().getValue();
	}

	// reset timer only
	public void log() {
		totaltime+=(DateTime.now().getValue()-time);
		time=DateTime.now().getValue();
	}


	public long getTime(){
		return time;
	}

	public long getTotaltime() {
		return totaltime;
	}


	
	
	
}

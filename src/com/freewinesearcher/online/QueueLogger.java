package com.freewinesearcher.online;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.freewinesearcher.common.Dbutil;


public class QueueLogger extends Thread {
	private int i=0;
	private final int numberofloggers=20;
	private BlockingQueue<Webactionlogger> itemsToLog = new LinkedBlockingQueue<Webactionlogger>();
	private static final QueueLogger instance = new QueueLogger();
	private ThreadGroup loggerthreads = new ThreadGroup("Queue Loggers");
	
	public static QueueLogger getLogger() {
		return instance;
	}
	
	public void destroy(){
		loggerthreads.interrupt();
	}
	
	private void startNewLogger(){
		i++;
		Dbutil.logger.info("Starting queue logger thread "+i);
		Thread t=new Thread(loggerthreads,new Logger(i),"QueueLogger "+i);
		t.setDaemon(true);
		t.start();

		
	}
	
	private QueueLogger() {
		for (int i=1;i<=numberofloggers;i++){
			startNewLogger();
		}
	}

	public int getQueueLength(){
		return itemsToLog.size();
	}

	public void log(Webactionlogger logrecord) {
		try {
			itemsToLog.put(logrecord);
		} catch (InterruptedException iex) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Unexpected interruption");
		}
	}

	
	class Logger implements Runnable {
		private int loggernumber=0;
		private boolean run=true;
		
		private Logger(int n){
			loggernumber=n;
		}
		
		public void run() {
			try {
				Webactionlogger logrecord;
				while (run){
					logrecord = itemsToLog.take();
					logrecord.logmenow();
					logrecord=null;
					if (loggerthreads.activeCount()<numberofloggers){
						startNewLogger();
					}
				}
			} catch (Exception iex) {
				run=false;
			} finally {

			}
		}
		
		public void destroy(){
			run=false;
			System.out.println("Destroy called for thread "+Thread.currentThread().getName());
			Thread.currentThread().interrupt();
		}
	}
	
	
}


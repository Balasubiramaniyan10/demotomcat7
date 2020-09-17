package com.freewinesearcher.common;

import java.sql.Timestamp;
import java.util.HashMap;

public class ConnectionTracker {
	public static HashMap<Integer,String> connectionmap=new HashMap<Integer,String>();
	public static HashMap<Integer,String> maxconnectionmap=new HashMap<Integer,String>();
	public static Timestamp maxconnectiondatetime;
	public static int opened=0;
	static int closed=0;
	static enum actions {ADD,REMOVE};
	
	static synchronized void track(int id, String stack, actions action){
		if (action.equals(actions.ADD)){
			connectionmap.put(id, stack);
			
			
			if (connectionmap.size()>maxconnectionmap.size()) {
				Wijnzoeker.maxDataSourceConnections=connectionmap.size();
				maxconnectionmap.clear();
				for (int k:connectionmap.keySet()) maxconnectionmap.put(k, connectionmap.get(k));
				maxconnectiondatetime=new java.sql.Timestamp(new java.util.Date().getTime()); 
				//Dbutil.logger.info("Max connections: "+connectionmap.size());
			}
			
			opened++;
			
		}
		if  (action.equals(actions.REMOVE)){
			//if (!connectionmap.containsKey(id)) Dbutil.logger.info("Could not find connection "+id+" in connectionmap.");
			connectionmap.remove(id);
			opened--;
			
		}
	}
	public static void clear(){
		connectionmap=new HashMap<Integer,String>();
		maxconnectionmap=new HashMap<Integer,String>();
		opened=0;
		closed=0;
	}
	
}

package com.freewinesearcher.common;

import java.util.Iterator;

public class VPDatasource implements VPDatasourceMBean{

	@Override
	public String trace() {
		StringBuffer sb=new StringBuffer();
		sb.append(Dbutil.dataSource.getNumActive()+" active connections\r\n");
		Iterator<Integer> it = ConnectionTracker.connectionmap.keySet().iterator();
		while (it.hasNext()){
			sb.append(ConnectionTracker.connectionmap.get(it.next()).replaceAll("<br/>", "\r\n"));
		}
		sb.append("\r\nOpen connection during maximum:\r\n");
		it = ConnectionTracker.maxconnectionmap.keySet().iterator();
		while (it.hasNext()){
			sb.append(ConnectionTracker.maxconnectionmap.get(it.next()).replaceAll("<br/>", "\r\n"));
		}
		return sb.toString();
	}

	@Override
	public void doConnectionLogging(boolean logging) {
		Dbutil.trackconnections=logging;
		
	}

}

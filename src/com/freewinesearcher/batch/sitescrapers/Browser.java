package com.freewinesearcher.batch.sitescrapers;

public interface Browser {

	public void get(String url);
	
	
	public String getHtml();
	public void close();
	public void setHeader(String key, String value) throws Exception ;
	public void setUseragent(String agent) throws Exception ;
	public boolean isAnonymous() throws Exception;
	public String getIp();
}

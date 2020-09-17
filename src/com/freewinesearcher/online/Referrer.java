package com.freewinesearcher.online;

public class Referrer {
	String referrer;

	public String getReferrer() {
		if (referrer==null) referrer="";
		return referrer;
	}

	public void setReferrer(String referrer) {
		if (referrer==null) referrer="";
		this.referrer = referrer;
	}
}

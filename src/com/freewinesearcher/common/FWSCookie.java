package com.freewinesearcher.common;

public class FWSCookie {
	String key="";
	String value="";
	
	public FWSCookie(String key, String value){
		this.key=key;
		this.value=value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}

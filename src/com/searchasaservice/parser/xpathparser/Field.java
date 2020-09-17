package com.searchasaservice.parser.xpathparser;

import java.io.Serializable;

public class Field implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String label;
	public Object content=null;
	int fwscounter=0;

	public Field(String label) {
		this.label = label;
	}
	
	public void setContent(Object content){
		this.content=content;
	}
}

package com.searchasaservice.parser;

import java.io.Serializable;

public class Field implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String label;
	Object content;
	boolean hascontent;
	int fwscounter=0;

	public Field(String label,boolean hascontent) {
		super();
		this.hascontent = hascontent;
		this.label = label;
	}
	
	public Field(XpathParserConfigField xpfield,Object content){
		super();
		this.hascontent=xpfield.hascontent;
		this.label=xpfield.label;
		this.content=content;
		
	}
}

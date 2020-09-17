package com.searchasaservice.parser;

import java.util.ArrayList;

public class XpathParserConfig extends ArrayList<XpathParserConfigField>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() throws Exception{
		for(XpathParserConfigField field:this){
				field.init();
		}
	}
	
	public long getUID(){
		return serialVersionUID;
	}
	
}

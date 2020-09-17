package com.searchasaservice.parser.xpathparser;

import java.util.ArrayList;

public class XpathParserConfig extends ArrayList<AbstractConfigField>{

	private static final long serialVersionUID = 1L;
	public boolean appendwineryname=false;
	

	public void init() throws Exception{
		for(AbstractConfigField field:this){
				field.init();
		}
	}
	
	public long getUID(){
		return serialVersionUID;
	}
	
}

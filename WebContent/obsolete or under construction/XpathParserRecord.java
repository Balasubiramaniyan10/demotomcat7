package com.searchasaservice.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XpathParserRecord extends ArrayList<Field> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> map = new HashMap<String, Integer>();
	//private ArrayList<Field> fields;
	
	public XpathParserRecord(){
		super();
	}
	
	public XpathParserRecord(ArrayList<Field> fields){
		super();
		super.clear();
		int i=0;
		for (Field field:fields){
			this.add(field);
			map.put(field.label, i);
			i++;
		}
	}
	
	
	public boolean add(Field field){
		super.add(field);
		map.put(field.label, map.size());
		return true;
	}
	

	
	public Object get(String fieldlabel){
		return super.get(map.get(fieldlabel)).content;
	}
	
}

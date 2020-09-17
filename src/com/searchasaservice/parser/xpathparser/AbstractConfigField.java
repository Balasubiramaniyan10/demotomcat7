package com.searchasaservice.parser.xpathparser;

import java.io.Serializable;

import org.w3c.dom.Node;

import com.freewinesearcher.common.Dbutil;

public abstract class AbstractConfigField implements Serializable{
	private static final long serialVersionUID = 1L;
	private ContentHandler contentHandler;
	public String label="";
	public boolean mustcontainvalue=false;
	public boolean isItemField=false;
	public Object defaultvalue=null;
	public String xpath="";
	
	
	public String setContentHandler(ContentHandler contentHandler){
		String message=contentHandler.init();
		if ("".equals(message)) {
			this.contentHandler=contentHandler;
				
		}
		return message;
	}
	
	public ContentHandler getContentHandler(){
		return contentHandler;
	}
	
	
	
	public Field getField(){
		return new Field(label);
	}
	
	public String init(){
		String message="";

		try {
			message+=contentHandler.init();
			if (defaultvalue!=null&&!defaultvalue.getClass().equals(contentHandler.getClass().getMethod("getContent", new Class[] {String.class}).getReturnType())){
				message+="Class of defaultvalue ("+defaultvalue.getClass().getName()+") does not match the returntype of the contenthandler ("+contentHandler.getClass().getMethod("getContent", new Class[] {String.class}).getReturnType()+"). ";
			}
			return message;
		} catch (NullPointerException e) {
			message+="ContentHandler is null for field "+label+".";
		} catch (SecurityException e) {
			message+="Could not access method getContent of class "+contentHandler.getClass();
		} catch (NoSuchMethodException e) {
			message+="Class "+contentHandler.getClass()+" does not implement method recognizeContent(Map<String,String> map). ";
		}
		return message;
	}
	
	public String setDefaultValue(String valuestr){
		String message="";
		if (contentHandler==null){
			message="You must set the contentHandler for a configuration field before setting the default value";
		} else {
			message=contentHandler.init();
			if (!message.equals("")){
				message="Could not initialize contentHandler before setting a return value. Reason: "+message;
			} else {
				try {
					defaultvalue=contentHandler.getClass().getMethod("getContent", new Class[] {Node.class}).getReturnType().cast(valuestr);
				} catch (Exception e) {
					try {
						message="Could not translate defaultvalue \""+defaultvalue+"\" to the type "+contentHandler.getClass().getMethod("getContent", new Class[] {Node.class}).getReturnType().getName();
					} catch (Exception e1) {
						Dbutil.logger.error("This should not have happened: problem while initializing defaultvalue while getting the return type of the contentHandler after it has been properly initialized!",e1);
					}
				}
			}
		}
		return message;
	}
	
}

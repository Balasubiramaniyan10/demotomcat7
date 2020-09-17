package com.freewinesearcher.online.web20;

import java.io.Serializable;

public class Comment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String html;
	public static int MINIMUMHTMLLENGTH=5;
	
	public Comment(){
		
	}
	
	public Comment(String html){
		if (html!=null) this.html=html;
	}
	
	public boolean hasContent(){
		if (html!=null&&html.length()>=MINIMUMHTMLLENGTH) return true;
		return false;
	}
	
	public String getComment(){
		if (html!=null) return html;
		return "";
	}
}

package com.searchasaservice.parser;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpression;

import net.sf.saxon.xpath.XPathEvaluator;

import org.w3c.dom.Element;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.online.Webroutines;

public class XpathParserConfigField implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String contentclass=String.class.getName();
	public enum Defaulttype { ITEM,PRICE,VINTAGE,BOTTLESIZE }
	public String regex="";
	public ArrayList<String> regexexclude=new ArrayList<String>(0);
	private String regexexcludestring="";
	public String xpath="";
	public String label="";
	public String color="";
	public String recognizeContentMethodClass=this.getClass().getName();
	public String getContentMethodClass=this.getClass().getName();
	public String recognizeContentMethodName="defaultrecognizeContent";
	public String getContentMethodName="defaultgetContent";
	private String recognizeContentMethodArg=String.class.getName();
	private String getContentMethodArg=String.class.getName();
	private transient Method recognizeContent=null;
	private transient Method getContent=null;
	//public boolean checkismandatory=false; // if false, check is only used to see if field contains valuable information for analysis purposes
	public boolean mustcontainvalue=false; // if false, content may be empty for this field
	private Map<Object, Boolean> map = new HashMap<Object, Boolean>();
	public boolean isItemField=false;
	public transient boolean hascontent=false;
	

	public XpathParserConfigField(String label){
		this.label=label;

	}
	
	

	public XpathParserConfigField(Defaulttype type, String label) throws SecurityException, NoSuchMethodException{
		super();
		if (type.equals(Defaulttype.PRICE)){
			this.label=label;
			regex="(\\d+[.,]\\d+)"; 
			regexexclude.add("(^|\\D)0[.,]75");
			regexexclude.add("(^|\\D)1[.,]5");
			mustcontainvalue=true;

		}
		if (type.equals(Defaulttype.VINTAGE)){
			this.label=label;
			regex="(18[0-4,6-9]\\d|185[0-4,6-9]|19\\d\\d|200\\d)";
			regexexclude.add("1855");

		}
		if (type.equals(Defaulttype.BOTTLESIZE)){
			this.label=label;
			this.contentclass=float.class.getName();
			setRecognizeContentMethod(this.getClass().getMethod("isBottleSize", new Class [] {Float.class}));
			setgetContentMethod(this.getClass().getMethod("getBottleSize", new Class [] {String.class}));
			
		}
		if (type.equals(Defaulttype.ITEM)){
			this.label=label;
			isItemField=true;
		}


	}

	
	
	public Boolean isValid(Object value) throws Exception{
		if (map.get(value)==null){
			if ((Boolean)recognizeContent.invoke(this,value)){
				map.put(value,true);
				return true;
			} else {
				map.put(value,false);
				return false;
			}
		} else {
			if (map.get(value)){
				return true;
			} else {
				return false;
			}
		}
	}

	public Boolean defaultrecognizeContent(String text){
		boolean isok=true;
		if (isok&&!"".equals(regex)) isok=(!"".equals(Webroutines.getRegexPatternValue(regex, text)));
		if (isok){
			if (!"".equals(regexexcludestring)){
				if (!"".equals(Webroutines.getRegexPatternValue(regexexcludestring, text))){
					isok=false;
				}
			}
		}
		if (isok&&("".equals(text))) isok=false;
		return isok;
	}

	public String defaultgetContent(String text){
		hascontent=false;
		if (!regex.contains("(")||!regex.contains(")")) {
			if (text!=null&&!text.equals("")) hascontent=true;
			return text;
		}
		String value=Webroutines.getRegexPatternValue(regex, text);
		if (value!=null&&!value.equals("")) hascontent=true;
		return value;
	}

	public Boolean isBottleSize(Float size){
		if (size>0) return true;
		return false;
	}
	
	public void setRecognizeContentMethod(Method m){
		recognizeContentMethodArg=m.getParameterTypes()[0].getName();
		recognizeContentMethodClass=m.getDeclaringClass().getName();
		recognizeContentMethodName=m.getName();
	}

	public void setgetContentMethod(Method m){
		//getContentMethodArg=m.getParameterTypes();
		getContentMethodClass=m.getDeclaringClass().getName();
		getContentMethodName=m.getName();
	}

	public Method getRecognizeContent() {
		return recognizeContent;
	}



	public Method getGetContent() {
		return getContent;
	}



	public float getBottleSize(String text){
		hascontent=false;
		float size=(float)0;
		String match;
		for (int j=0; j<Configuration.limitedsizeregex.length;j++ ){
			if (size==0){ // With size, take the first hit
				match=Webroutines.getRegexPatternValue("("+Configuration.limitedsizeregex[j]+")",text,Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
				if (!"".equals(match)){
					size=Configuration.limitedsize[j];
				}
			}

		}
		if (size>0) hascontent=true;
		return size;

	}

	public boolean init() throws Exception {
		boolean isok=true;
		regexexcludestring="";
		for (String regex:regexexclude){
			regexexcludestring+="|"+regex;
		}
		if (regexexcludestring.length()>0){
			regexexcludestring=("("+regexexcludestring.substring(1)+")");
		}	
		XPathEvaluator xpeval=new XPathEvaluator();
		if(!"".equals(regexexcludestring)) try{
			XPathExpression exp=xpeval.compile("//*[matches(.,\""+regexexcludestring+"\")]");
		} catch (Exception e){
			Dbutil.logger.error("Cannot compile regexexcludestring //*[matches(.,\""+regexexcludestring+"\")] for field "+label);
			isok=false;
			throw(e);
		}
		try{
			XPathExpression exp=xpeval.compile("//*[matches(.,\""+regex+"\")]");
		} catch (Exception e){
			Dbutil.logger.error("Cannot compile regex //*[matches(.,\""+regex+"\")] for field "+label);
			isok=false;
		}
		
		//Dbutil.logger.info(this.getClass().getName());
		if (label.equals("Item")) isItemField=true;
		recognizeContent=Class.forName(recognizeContentMethodClass).getMethod(recognizeContentMethodName, Class.forName(recognizeContentMethodArg));
		getContent=Class.forName(getContentMethodClass).getMethod(getContentMethodName, Class.forName(getContentMethodArg));
		if (getContent==null) getContent=this.getClass().getMethod("defaultgetContent", new Class [] {String.class});
		String type=recognizeContent.getReturnType().getName();
		if (!type.equals("boolean")&&!type.equals("java.lang.Boolean")) {
			Dbutil.logger.info("Return type of recognize for field "+label+" must be boolean, but is "+type+". Method is "+recognizeContentMethodClass+"."+recognizeContentMethodName);
			isok=false;	
		}
		if (!getContent.getReturnType().getName().equals(contentclass)) {
			Dbutil.logger.info("For field "+label+", return type of getContent must be equal to contentclass "+contentclass+", but is "+getContent.getReturnType().getName()+". Content method is "+getContentMethodClass+"."+getContentMethodName);
			isok=false;	
		}
		return isok;
	}
}



package com.freewinesearcher.online;

import java.io.*;
import java.net.*;
import javax.servlet.http.*;

import com.freewinesearcher.common.Dbutil;

import java.util.Properties;

public class Translator {
	public static enum languages {EN,FR,NL,DE,ES,IT};
	public static Properties EN = new Properties();
	public static Properties FR = new Properties();
	public static Properties NL = new Properties();
	public static Properties DE = new Properties();
	public static Properties ES = new Properties();
	public static Properties IT = new Properties();
	public Translator.languages language=languages.EN;
	
	
	static {
	    fetchConfig();
	}
	public Translator(){
	}
	
	public Translator(languages language){
		this.language=language;
	}
	
	  public String getLanguage() {
		return language.toString();
	}



	  public void setLanguage(languages language) {
			if (language==null) language=Translator.languages.EN;
			this.language = language;
		}

	  public static languages getLanguage(String languageString) {
			languages language=null;
		  try{
			  language=Translator.languages.valueOf(languageString);
		  } catch (Exception e){}
			return language;
		}

	public String get(String label){
		if (language!=null){
			try{
				switch (language){
				case EN:{
					if (EN.getProperty(label,"").equals("")){
						Dbutil.logger.error("Could not find English label"+label);
					}
					return EN.getProperty(label,"");
				}
				case FR:return FR.getProperty(label,FR.getProperty(label,""));	
				case NL:return NL.getProperty(label,EN.getProperty(label,""));	
				case DE:return DE.getProperty(label,EN.getProperty(label,""));	
				case ES:return ES.getProperty(label,EN.getProperty(label,""));	
				case IT:return IT.getProperty(label,EN.getProperty(label,""));	
				}
			} catch (Exception e){
				return EN.getProperty(label,"");
			}
		}
		return EN.getProperty(label,"");
	}

	/**
	  * Open a specific text file containing translations of text used on the site
	  * Populate a corresponding Properties object.
	  */
	private static void fetchConfig() {
		
		InputStream input = null;
		String file="";
		String sourcedir="../../../../";
		try {
			//File file1=new File(".\\WEB-INF");
			//Dbutil.logger.info("Loading langage files from directory "+file1.getAbsolutePath());
			//URL url = Translator.class.getResource("../../EN-language.properties");
			//Dbutil.logger.info("URL "+url.getPath());
			//File file1 = new File(url.getFile());
		    //Dbutil.logger.info("Loading langage files from directory "+file1.getAbsolutePath());
			//file=".\\WEB-INF\\"+"EN"+"-language.properties";
			//file="/"+"EN"+"-language.properties";
//			input=new FileInputStream(file);
			file=sourcedir+"EN-language.properties";
			input=Translator.class.getResourceAsStream(file);
			EN.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		try {
			file=sourcedir+"NL-language.properties";
			input=Translator.class.getResourceAsStream(file);
			NL.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		try {
			file=sourcedir+"FR-language.properties";
			input=Translator.class.getResourceAsStream(file);
			FR.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		
		
		if (false){ // All languages for which we do not have a language file yet
		try {
			file=sourcedir+"FR-language.properties";
			input=Translator.class.getResourceAsStream(file);
			FR.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		try {
			file=sourcedir+"DE-language.properties";
			input=Translator.class.getResourceAsStream(file);
			DE.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		try {
			file=sourcedir+"ES-language.properties";
			input=Translator.class.getResourceAsStream(file);
			ES.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		try {
			file=sourcedir+"IT-language.properties";
			input=Translator.class.getResourceAsStream(file);
			IT.load(input);
		} catch (Exception ex ){
			Dbutil.logger.error( "Cannot open language file "+file,ex);
		}
		}
		try {
			if ( input != null ) input.close();
		}
		catch ( IOException ex ){
			System.err.println( "Cannot close language file." );
		}
	}

	public static languages getDefaultLanguageForCountry(String country){
		languages language=null;
		if (country!=null&&!country.equals("")){
			if(country.equals("NL")) return Translator.languages.NL;
		}
		
		return language;
	}

	

}

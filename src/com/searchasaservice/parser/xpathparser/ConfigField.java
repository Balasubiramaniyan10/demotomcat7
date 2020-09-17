package com.searchasaservice.parser.xpathparser;

import java.io.Serializable;

import com.freewinesearcher.batch.BottleSizeHandler;
import com.freewinesearcher.common.Dbutil;

public class ConfigField extends AbstractConfigField implements Serializable{

	private static final long serialVersionUID = 1L;
	public enum Defaulttype { ITEM,PRICE,PRICENODECIMAL,VINTAGE,BOTTLESIZE,URL }
	
	public ConfigField(String label){
		this.label=label;
	}
	
	public ConfigField(Defaulttype type, String label) throws Exception{
		super();
		String message="";
		if (type.equals(Defaulttype.PRICE)){
			this.label=label;
			TextHandler th=new TextHandler();
			message+=th.setPositiveConfig("(\\d+[.,]\\d+)");
			message+=th.setNegativeConfig("((^|\\D)(0[.,]375|0[.,]75|1[.,]5))");
			setContentHandler(th);
			mustcontainvalue=true;
		}

		if (type.equals(Defaulttype.PRICENODECIMAL)){
			this.label=label;
			TextHandler th=new TextHandler();
			message+=th.setPositiveConfig("((^|[^.,])\\d\\d+)");
			message+=th.setNegativeConfig("((^|\\D)19\\d\\d|(^|\\D)20\\d\\d|(^|\\D)[89]\\d)");
			setContentHandler(th);
			mustcontainvalue=true;
		}

		
		if (type.equals(Defaulttype.VINTAGE)){
			this.label=label;
			TextHandler th=new TextHandler();
			message+=th.setPositiveConfig("(18[0-4,6-9]\\d|185[0-4,6-9]|19\\d\\d|200\\d)");
			message+=th.setNegativeConfig("1855");
			setContentHandler(th);
		}
		if (type.equals(Defaulttype.URL)){
			this.label=label;
			setContentHandler(new UrlHandler());
			mustcontainvalue=false;
		}

		if (type.equals(Defaulttype.BOTTLESIZE)){
			this.label=label;
			setContentHandler(new BottleSizeHandler());
		}

		if (type.equals(Defaulttype.ITEM)){
			this.label=label;
			isItemField=true;
		}
		if (!message.equals("")){
			Dbutil.logger.error(message);
			throw (new Exception());
		}


	}

	
}


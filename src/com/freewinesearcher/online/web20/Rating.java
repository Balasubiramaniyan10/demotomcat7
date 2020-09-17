package com.freewinesearcher.online.web20;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Rating implements Serializable{

	private static final long serialVersionUID = 1L;
	public int rating;
	public int scale=100;
	
	public Rating (String rating){
		this.rating=Integer.parseInt(rating);
	}
	public Rating (int rating){
		this.rating=rating;
	}
	
	public static String getRatingComment(Subject.Types subject){
		String html="";
		switch (subject){
			case WINE:html="Rate this wine (0 stars=\"Undrinkable\", 5 stars=\"Absolutely loved it!\")";
			break;
			case PRODUCER: html="Rate this producer (0 stars=\"Sucks bad\", 5 stars=\"Truely fabulous!\")";
			break;
			case STORE: html="Rate this seller (0 stars=\"Would not buy here again\", 5 stars=\"Favorite store!\")";
			break;
			
		}
		return html;
	}
	
	public String getHTML(boolean editable){
		if (editable){
			return "<div id=\"star\"><ul id=\"star1\" class=\"star\" onmousedown=\"star.update(event,this)\" onmousemove=\"star.mouse(event,this)\" title=\"Rate This!\"><li id=\"starCur1\" class=\"curr\" title=\"0\" style=\"width: "+(rating*84)/100+"px;\"></li></ul><div id=\"starUser1\" class=\"user\">"+(rating>0?(rating+"%"):"&nbsp;")+"</div></div>";
		}
		NumberFormat format  = new DecimalFormat("0.0");	
		return "<br style=\"clear:both; margin: 7px 0 0\"><div class=\"star\"><ul class=\"star\"><li class=\"curr\" title=\""+rating+"\" style=\"width: "+(rating*84)/100+"px;\"></li></ul><div class=\"user\">"+format.format((double)rating/20)+"</div><br style=\"clear: both; margin-bottom: 4px;\"></div>";

	}
}

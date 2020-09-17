package com.freewinesearcher.online;

import com.freewinesearcher.common.POI;

public class RegionPoi implements POI{
	private int id;
	private String lasteditor;
	private String lasteditorsub;
	private String locale;
	private Double lat;
	private Double lon;
	private String name;
	public String labelText;

	public RegionPoi(int id,String locale, Double lat, Double lon, String name,String labelText, String lasteditor, String lasteditorsub) {
		super();
		this.id=id;
		this.locale = locale;
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.labelText = labelText;
		this.lasteditor=lasteditor;
		this.lasteditorsub=lasteditorsub;
		
	}
	public RegionPoi(int id,String locale, Double lat, Double lon, String name,String labelText) {
		super();
		this.id=id;
		this.locale = locale;
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.labelText = labelText;

		
	}
	
	
	public String getLocale() {
		return locale;
	}


	public void setLocale(String locale) {
		this.locale = locale;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setId(int id) {
		this.id = id;
	}


	public void setLat(Double lat) {
		this.lat = lat;
	}


	public void setLon(Double lon) {
		this.lon = lon;
	}


	
	
	public String getLasteditor() {
		return lasteditor;
	}


	public void setLasteditor(String lasteditor) {
		this.lasteditor = lasteditor;
	}


	public String getLasteditorsub() {
		return lasteditorsub;
	}


	public void setLasteditorsub(String lasteditorsub) {
		this.lasteditorsub = lasteditorsub;
	}


	

	
	@Override
	public String getHTML() {
		return "<h3><a href='/region/"+Webroutines.URLEncode(Webroutines.removeAccents(locale)).replace("&", "&amp;")+"'>"+name+"</a></h3>";
	}

	@Override
	public Double getLat() {
		return lat;
	}

	@Override
	public Double getLon() {
		return lon;
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public String getLabelText() {
		return labelText;
	}


	@Override
	public int getId() {
		return id;
	}
}

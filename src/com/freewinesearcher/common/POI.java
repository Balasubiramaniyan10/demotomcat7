package com.freewinesearcher.common;

import com.freewinesearcher.online.Producer;

public interface POI {
	public int getId();
	public Double getLat();
	public Double getLon();
	public String getHTML();
	public String getTitle();
	public String getLabelText(); // If text is given, only text is shown, no marker icon

}

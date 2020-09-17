package com.freewinesearcher.online;

public class Bounds {
	public double lonmin=(double)99999;
	public double lonmax=(double)-99999;
	public double latmin=(double)99999;
	public double latmax=(double)-99999;
	public double centerlat=0;
	public double centerlon=0;

	public Bounds(Double lonmin, Double lonmax, Double latmin, Double latmax) {
		super();
		this.lonmin = Math.min(lonmin,lonmax);
		this.lonmax = Math.max(lonmin,lonmax);
		this.latmin = Math.min(latmin,latmax);
		this.latmax = Math.max(latmin,latmax);
		centerlat=(latmin+latmax)/2;
		centerlon=(lonmin+lonmax)/2;
	}
	public Bounds() {
	}
}

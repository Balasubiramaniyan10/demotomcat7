package com.freewinesearcher.online;
import java.io.Serializable;
import java.util.ArrayList;
public class Searchhistory  implements Serializable{
	private static final long serialVersionUID = 1L;
	ArrayList<Integer> clicks=new ArrayList<Integer>();
	ArrayList<Integer> searches=new ArrayList<Integer>();

	
	
	public ArrayList<Integer> getClicks() {
		return clicks;
	}
	public void setClicks(int clicks) {
		this.clicks.add(clicks);
	}
	public ArrayList<Integer> getSearches() {
		return searches;
	}
	public void setSearches(int searches) {
		this.searches.add(searches);
	} 
	
} 
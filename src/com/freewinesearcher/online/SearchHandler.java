package com.freewinesearcher.online;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wineset;

public class SearchHandler {

	public Wineset wineset=new Wineset();
	public Wineset sponsoredwineset=new Wineset();
	public String message="";
	public boolean search=false;
	public int singlevintage=0;
	public boolean fuzzy=false;
	

	public SearchHandler(){}
	
	public SearchHandler (Searchdata searchdata, String referrer, boolean fuzzy, Translator t, boolean newsearch){
		//Dbutil.logger.info(fuzzy);
		this.fuzzy=fuzzy;
		searchdata.fuzzy=fuzzy;
		int numberofresults = 0;
		boolean sponsoredresults = false;
		WineLibraryTV wltv;
		String youmayalsolike = "";
		if (searchdata.getOrder().equals("")) searchdata.setOrder("priceeuroex");
		if (Webroutines.getConfigKey("showsponsoredlinks").equals("true"))
			sponsoredresults = true;

		int numberofrows = Webroutines.numberofnormalrows;
		if (searchdata.numberofrows>0) numberofrows=searchdata.numberofrows;
		if (!newsearch&&searchdata.getName().length()<3){
			
			wineset=new Wineset();
		} else {
			if (searchdata.getName().length()<3){
				wineset=new Wineset();
				message+="Please enter at least 3 characters for the wine name.<br/>";
			} else {
				search=true;
				//Dbutil.logger.info("get Wineset");
				wineset=new Wineset(searchdata);
				wineset.search();
				//wineset = Wineset.getWineset(searchdata, referrer, numberofrows,fuzzy, false);
				//Dbutil.logger.info(searchdata.getCountry());
				searchdata.freetextresults=0;
				// Temporary: do not automatically smart search on the best match until matching is better
				if (false&&wineset.bestknownwineid>0&&!wineset.searchtype.equals("smart")&&!searchdata.isFreetext()){
					searchdata.freetextresults=wineset.records;
					String freetextname=searchdata.getName();
					LinkedHashMap<Integer, Integer> oldlist=(LinkedHashMap<Integer, Integer>)wineset.knownwinelist.clone();
					searchdata.setName(Knownwines.getUniqueKnownWineName(wineset.bestknownwineid));
					wineset = Wineset.getWineset(searchdata, referrer, numberofrows,fuzzy, false);
					searchdata.setName(freetextname);
					wineset.knownwinelist=oldlist;
				}
				//Dbutil.logger.info("got Wineset");
				try {singlevintage = Integer.parseInt(searchdata.getVintage().trim());} catch (Exception e) {}
				int knownwineid = 0;
				if (wineset != null) {
					if (wineset.region == null || wineset.region.equals("")) {
						if (wineset.bestknownwineid > 0) {
							wineset.region = Dbutil.readValueFromDB(
									"Select * from knownwines where id="
									+ knownwineid + ";", "appellation");
						}
					}
				}
				if (wineset != null && wineset.othervintage) {
					message+=(t.get("noresultsfound")+" " + t.get("forvintage") + " "+ searchdata.getVintage() + ". "+ t.get("othervintages"))+"<br/>";
				}
				if (wineset.canonicallink!=null&&wineset.canonicallink.length()>2){
					if (singlevintage>0&&!wineset.othervintage) wineset.canonicallink+="+"+singlevintage;
				}
				

				if (wineset == null || wineset.Wine == null
						|| wineset.Wine.length == 0
						&& !wineset.othervintage) {
					message+=t.get("noresultsfound")+".<br/>";

				}
			}
		}
	}
	public Wineset getWineset(){
		if (wineset==null) wineset=new Wineset();
		return wineset;
	}

}

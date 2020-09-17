package com.freewinesearcher.online;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Region;
import com.freewinesearcher.online.Webroutines.RatingInfo;

public class Linkboard {
	int knownwineid;
	int vintage;
	Map<String,String> links;
	static Map<String,String> categories;
	static Map<String,String> images;
	String source;
	int categoryindex;


	public static void fillStaticInfo(){
		categories=new HashMap<String,String>();
		categories.put("RP","Expert reviews");
		categories.put("WS","Expert reviews");
		categories.put("GV","Expert reviews");
		categories.put("CT","Community tasting notes");
		images=new HashMap<String,String>();
		images.put("RP", "/images/erobertparker.gif");
		images.put("WS", "/images/winespectator.gif");
		images.put("GV", "/images/wltv.gif");
		images.put("CT", "/images/cellartracker.gif");

	}

	public void generateLinks(){
		if (categories==null) Linkboard.fillStaticInfo();
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			links=new LinkedHashMap<String,String>();
			query = "select * from ratedwines where knownwineid="+knownwineid+" and  (sourceurl like '%winespectator%' or sourceurl like '%erobertparker%') group by author,name order by count(*) desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				if (!links.containsKey(rs.getString("author"))){
					String name=rs.getString("name");
					String link="";
					if (name.contains(",")) name=name.substring(0,name.indexOf(","));
					name=java.text.Normalizer.normalize(name,java.text.Normalizer.Form.NFD);
					if (rs.getString("author").equals("RP")) link="http://www.erobertparker.com/newSearch/pTextSearch.aspx?search=members&textSearchString="+Webroutines.URLEncode(name);
					if (rs.getString("author").equals("WS")) link="http://www.winespectator.com/wine/search?submitted=Y&forwarded=1&size=50&text_search_flag=wine_plus_vintage&search_by=all&fuzzy=&sort_by=vintage&case_prod=&taste_date=&viewtype=&winery="+Webroutines.URLEncodeUTF8(name);
					if (!link.equals("")){
						if (vintage>0) link+="+"+vintage;
						links.put(rs.getString("author"), link);
					}
				}
			}
			Dbutil.closeRs(rs);
			query = "select * from winelibrarytv where knownwineid="+knownwineid+(vintage>0?" and vintage="+vintage:"")+" order by vintage desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				links.put("GV", rs.getString("link"));
			}
			links.put("CT", "http://www.cellartracker.com/list.asp?Table=Notes&iUserOverride=0&szSearch="+Webroutines.URLEncode(Knownwines.getKnownWineName(knownwineid))+(vintage>0?"+"+vintage:""));
			String sourcecat=categories.get(source);
			HashSet<String> cats=new HashSet<String>();
			cats.addAll(categories.values());
			int n=0;
			setCategoryindex(0);
			if (sourcecat!=null){
				for (String cat:cats){
					if (sourcecat.equals(cat)) setCategoryindex(n);
					n++;
				}
			}
			//Dbutil.logger.info(links.size());
			
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public int getCategoryindex() {
		return categoryindex;
	}

	public void setCategoryindex(int categoryindex) {
		this.categoryindex = categoryindex;
	}

	public String getLinkBoardHeader(int width, String searchpage, Searchdata searchdata, Translator t, boolean showeditbuttons,String lasturl){
		String html="";
		StringBuffer sb=new StringBuffer();
		ResultSet rs=null;
		WineLibraryTV wltv=null;
		String query;
		String authorinfo="";
		String goodpqvintages="";
		String knownwinename="";
		HashMap<String,String> map=new HashMap<String,String>();
		HashMap<String,String> winemap=new HashMap<String,String>();
		HashMap<String,Integer> ratedwinemap=new HashMap<String,Integer>();
		HashMap<String,String> linkmap=new HashMap<String,String>();
		TreeSet<Integer> vintages=new TreeSet<Integer>();
		HashSet<String> authors=new HashSet<String>();
		Connection con=Dbutil.openNewConnection();
		if (true||knownwineid>0){
			if (true||Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="+knownwineid+" and rating>70;", "thecount")>0){
				int currentvintage;
				int startvintage=0;
				String singlevintageclause="";
				String cellclass;
				try{
					// Skip labels
					String labeldiv="";
					if (false){
					if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+knownwineid+".jpg").exists()){
						labeldiv="<div class='label'><img src='/labels/"+knownwineid+".jpg' alt=\""+Knownwines.getKnownWineName(knownwineid)+"\" onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";
					} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+knownwineid+".gif").exists()){
						labeldiv="<div class='label'><img src='/labels/"+knownwineid+".gif' alt='"+Knownwines.getKnownWineName(knownwineid).replace("'","&apos;")+"' onmouseover='this.parentNode.className=\"labelmouseover\";' onmouseout='this.parentNode.className=\"label\";'/></div>";

					}
					}
					String[] glass=Knownwines.getGlassImage(knownwineid);
					knownwinename=Knownwines.getKnownWineName(knownwineid);
					Knownwine k=new Knownwine(knownwineid);
					k.getProperties();
					// wine div
					sb.append("<div class='winecontainer'>");
					sb.append("<img class='wineglass' src='/css2/d"+glass[0]+"' alt='"+glass[1]+"'/>" +
							"<div class='wine'>"+labeldiv+	
							"<div class='winename'><h1>"+knownwinename+(vintage>0?" "+vintage:"")+"</h1></div>" 
							+"<div class='region'><h1>");
					String region=Region.getRegion(knownwineid);
					for (String r:Region.getRegionPath(region)){
						if (!r.equals("All")) sb.append("<a href='/wine-guide/region/"+Webroutines.removeAccents(r)+"' target='_top'>"+r+"</a> &raquo; ");
					}
					sb.append("<a href='/wine-guide/region/"+Webroutines.removeAccents(region)+"' target='_top'>"+region+"</a></h1></div></div>");
					sb.append("<form method='post' action='"+lasturl+"' target='_top'><input type='hidden' name='keepdata' value='true'><input type='submit'  id='back' value='Back to Vinopedia'/></form>");
					//sb.append("<div class='search'><div class='findwine'>"+t.get("searchwine")+"</div><form action='/winelinksheader.jsp' method='post' id='searchform' name='searchform'><input type='hidden' name='dosearch' value='true' /><input type='hidden' name='source' id='source' value='"+source+"'><input class='searchinput' id='name' type='text' autocomplete='off' name='name' value='"+Webroutines.escape(knownwinename).replaceAll("'", "&apos;")+(vintage>0?" "+vintage:"")+"' size='25' /></form><div class='sprite sprite-gosmall searchgosmall' onclick='document.getElementById(\"searchform\").submit()' alt='Search'></div></div>");
					sb.append("</div>");

				} catch (Exception E){
					Dbutil.logger.error("Problem while generating Ratings html",E);
				}
			}
		}

		Dbutil.closeRs(rs);
		Dbutil.closeConnection(con);
		RatingInfo ri=new RatingInfo();
		return sb.toString();

	}



	public String getLinkHtml(){
		StringBuffer sb=new StringBuffer();
		if(knownwineid>0){
			HashSet<String> cats=new HashSet<String>();
			cats.addAll(categories.values());
			//sb.append("<ul id='linkboardnavi'>");
			//for (String cat:cats){
			//	sb.append("<li>"+cat+"</li>");
			//}
			//sb.append("</ul>");
			sb.append("<div id='links'><div id='pages'>");
			for (String cat:cats){
			//	sb.append("<div class='page'><a class='prevPage browse left'>Previous</a><div class='scrollable'><div class='items'>");
				for(String src:links.keySet()){
					if (categories.get(src).equals(cat)){
						sb.append("<div class='item'><a href='"+getLinks().get(src).replaceAll("&", "&amp;").replaceAll("'", "&apos;")+"' target='bottom_frame' onclick='setSource(\""+src+"\")' ><img src='"+Linkboard.getImages().get(src)+"' alt='"+Webroutines.getAuthorName(src)+"' class='linkimage'/></a></div>");		
					}
				}
			//	sb.append("</div></div><a class='nextPage browse right'>Next</a></div>");
			}
			sb.append("</div></div>");
		}
		return sb.toString();
	}


	public int getKnownwineid() {
		return knownwineid;
	}
	public void setKnownwineid(int knownwineid) {
		this.knownwineid = knownwineid;
	}
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getVintage() {
		return vintage;
	}
	public void setVintage(int vintage) {
		this.vintage = vintage;
	}
	public Map<String, String> getLinks() {
		return links;
	}
	public void setLinks(Map<String, String> links) {
		this.links = links;
	}
	public static Map<String, String> getCategories() {
		return categories;
	}

	public static void setCategories(Map<String, String> categories) {
		Linkboard.categories = categories;
	}

	public static Map<String, String> getImages() {
		if (images==null) fillStaticInfo();
		return images;
	}

	public static void setImages(Map<String, String> images) {
		Linkboard.images = images;
	}




}

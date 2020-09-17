package com.freewinesearcher.online;


import java.io.File;
import java.io.Serializable;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;


import com.freewinesearcher.batch.CurrencyClient;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.common.Zipper;
import com.google.gdata.data.DateTime;

public class WineAdvice implements Runnable,Serializable{


	private static final long serialVersionUID = 1L;
	public static transient ConcurrentHashMap<Integer,byte[]> cache=new ConcurrentHashMap<Integer, byte[]>();
	public static int defaultlimit=9;
	public static enum Winetypes {ALLTYPES,RED,WHITE,ROSE,SPARKLING,DESSERT,PORT};
	public static enum Searchtypes {PQ,Q,P,DA};
	public Searchtypes searchtype=Searchtypes.PQ;
	public Winetypes type=Winetypes.ALLTYPES;
	public String winetype="";
	float pricemin=0;
	float pricemax=0;
	String region="All";
	String winetypecoding="";
	String color="All";
	String dryness="All";
	String countryofseller="All";
	String vintage="";
	String grape="";
	String currency="EUR";
	String[] cur=new String[4];
	int ratingmin=0;
	int ratingmax=0;
	int vintagemin=0;
	int vintagemax=0;
	int resultsperpage=9;
	int page=0;
	int pages=1;
	int offset=0;
	int rows=0;
	int activepane=1;
	public int shopid=0;
	String scale="100";
	boolean subregions=true;
	boolean json=true;
	String order;
	public int excludewineid=0;
	public transient Facets facets=null;
	public int priceupperlimit=200;
	public long timer=DateTime.now().getValue();
	public String timerlog="";
	String continent="AL";
	public String loggerinfo="";
	public String searchinfo="";
	public String html;


	public Wine[] wine;
	public int[] rating;
	public int[] ratinghigh;

	public String getHavingclause(){
		String havingclause="";
		return havingclause;

	}

	public String getwhereclausePrice(){
		String whereclause="";
		if (pricemin>0){
			whereclause+=" AND priceeuroex>="+CurrencyClient.toEuro(pricemin, currency);
		}
		if (pricemax>0&&pricemax<priceupperlimit){
			whereclause+=" AND priceeuroex<="+CurrencyClient.toEuro(pricemax, currency);
		}
		return whereclause;

	}



	public String getWhereclause(String skip){
		StringBuffer whereclause=new StringBuffer();
		String vintagestring=vintage;
		vintagestring=vintagestring.replaceAll(", "," ");
		vintagestring=vintagestring.replaceAll(","," ");
		String[] vintages=vintagestring.split(" ");
		String vintageclause="";
		String from,to;
		String[] vintagerange;
		boolean logarguments=false;
		if (searchinfo.length()==0) logarguments=true;
		if (pricemin>0){
			whereclause.append(" AND priceeuroex>="+CurrencyClient.toEuro(pricemin, currency));
			if (logarguments) searchinfo+="priceeuroex>="+CurrencyClient.toEuro(pricemin, currency)+" ";
		}
		if (pricemax>0&&pricemax<priceupperlimit){
			whereclause.append(" AND priceeuroex<="+CurrencyClient.toEuro(pricemax, currency));
			if (logarguments) searchinfo+="priceeuroex<="+CurrencyClient.toEuro(pricemax, currency)+" ";
		}
		/*
		if (!"vintage".equals(skip)&&!vintage.equals("")){
			for (int i=0; i<vintages.length;i++){
				if (!vintages[i].equals("")){
					if (!vintageclause.equals("")){
						vintageclause=vintageclause+" OR ";
					}
					if (vintages[i].indexOf("-")>-1){
						from="0";
						to="0";
						vintagerange=vintages[i].split("-");
						if (vintagerange.length<2){
							to="3000";
						} else {
							to=vintagerange[1];
						}
						if ((vintagerange.length<1)||(vintagerange[0].equals(""))) {
							from="1";
						} else {
							from=vintagerange[0];
						}
						vintageclause=vintageclause+"Vintage between "+from+
						" AND "+to;
					} else {
						vintageclause=vintageclause+"Vintage="+vintages[i];
					}
				}
			}
			if (!vintageclause.equals("")) {
				whereclause.append(" AND ("+vintageclause+")");
				if (logarguments) searchinfo+="vintage="+vintage+" ";
			}

		}
		 */
		if (!"vintage".equals(skip)&&(vintagemin>0||vintagemax>0)){
			if (vintagemin>0) vintageclause=" and vintage>="+vintagemin;
			if (vintagemax>0&&vintagemax<5000) vintageclause+=" and vintage<="+vintagemax;
			if (!vintageclause.equals("")) whereclause.append(vintageclause);
			if (logarguments) searchinfo+="vintage="+vintage+" ";
			//Dbutil.logger.info(vintageclause);
		}
		if (!"region".equals(skip)&&!region.equals("")&&!region.equals("All")){ 
			int lft=0;
			int rgt=9999;
			if (Configuration.newrecognitionsystem){
				Connection con=Dbutil.openNewConnection();
				ResultSet rs=null;
				try{

					rs=Dbutil.selectQuery("select lft,rgt,shortregion from kbregionhierarchy where shortregion like '"+Spider.SQLEscape(region)+"';", con);
					if (rs.next()){
						lft=rs.getInt("lft");
						rgt=rs.getInt("rgt");
						region=rs.getString("shortregion");
					} else {
						region="All";
					}

				} catch (Exception e){

				}finally{
					Dbutil.closeRs(rs);
					Dbutil.closeConnection(con);
				}

			} else {
				try{
					lft=Integer.valueOf(Dbutil.readValueFromDB("select lft from regions where region='"+Spider.SQLEscape(region)+"';", "lft"));
					rgt=Integer.valueOf(Dbutil.readValueFromDB("select rgt from regions where region='"+Spider.SQLEscape(region)+"';", "rgt"));
				} catch (Exception e){}
			}
			//String regionlist=Region.getRegionsAsIntList(region);
			if (rgt<9999) {
				if (logarguments) searchinfo+="region="+region+" ";

				if (subregions){
					whereclause.append(" AND lft>="+lft+" and rgt<="+rgt+" ");
				} else {
					whereclause.append(" AND lft="+lft+" and rgt="+rgt+" ");
				}
			}
		}

		if (!"grape".equals(skip)&&!grape.equals("")){
			int grapeid=Dbutil.readIntValueFromDB("select id from grapes where grapename='"+Spider.SQLEscape(grape)+"';", "id");
			//Dbutil.logger.info(grape+grapeid);
			if (grapeid>0) {
				whereclause.append(" AND grapes="+grapeid);
				grape=Dbutil.readValueFromDB("select grapename from grapes where grapename='"+Spider.SQLEscape(grape)+"';", "grapename");
			}
			if (logarguments) searchinfo+="grape="+grape+" ";
		}
		if (!"shopid".equals(skip)&&shopid>0){
			whereclause.append(" AND shopid="+shopid);
		}
		if (!"rating".equals(skip)&&ratingmin>80){
			whereclause.append(" AND rating>="+ratingmin);
			if (logarguments) searchinfo+="rating>="+ratingmin+" ";
		}
		if (!"rating".equals(skip)&&ratingmax<100&&ratingmax>0){
			whereclause.append(" AND (rating is null OR rating<="+ratingmax+")");
			if (logarguments) searchinfo+="rating<="+ratingmax+" ";
		}

		if (!"countryofseller".equals(skip)&&!countryofseller.equals("All")){
			whereclause.append(" AND country in ('"+Webroutines.getCountries(Spider.SQLEscape(countryofseller))+"')");
			if (logarguments) searchinfo+="countryofseller="+countryofseller+" ";
		}



		if (excludewineid>0){
			whereclause.append(" and knownwineid!="+excludewineid);
		}

		winetypecoding="";
		if (!"type".equals(skip)&&!type.equals(Winetypes.ALLTYPES)){
			switch(type){
			case RED: winetypecoding+=",1";
			break;
			case WHITE: winetypecoding+=",3,7";
			break;
			case ROSE: winetypecoding+=",4";
			break;
			case SPARKLING: winetypecoding+=",2,8,12";
			break;
			case DESSERT: winetypecoding+=",5,10,11";
			break;
			case PORT: winetypecoding+=",6,9";
			break;

			}
			if (winetypecoding.length()>0) winetypecoding=winetypecoding.substring(1);
			if (!winetypecoding.equals("")){
				whereclause.append(" AND winetypecode in("+winetypecoding+")");
				if (logarguments) searchinfo+="type="+type+" ";

			}
		}
		//whereclause.append(" AND rating>0 ";

		if (whereclause.length()>4) return "where "+whereclause.substring(4);
		return "";

	}

	public static WineAdvice readfromcache(int hashcode){
		if (cache.get(hashcode)!=null){
			return (WineAdvice)Zipper.unzipObjectFromBytes(cache.get(hashcode));
		}else {
			return null;
		}
	}

	public void writetocache(){
		cache.put(this.hashCode(), Zipper.zipObjectToBytes(this));
	}


	public void getAdvice(){
		WineAdvice cached=WineAdvice.readfromcache(this.hashCode());
		if (cached==null||cached.wine==null){
			//Dbutil.logger.info("Not in wineAdvice.getAdvice() cache:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
			ResultSet rs=null;
			ResultSet rs2=null;
			Connection con=Dbutil.openNewConnection();
			wine=null;
			//if (page==0) pages=1;
			int limit=pages*resultsperpage;
			try{
				String whereclause=getWhereclause("");
				String query="";
				if (searchtype.equals(Searchtypes.PQ)){
					query="select * from knownwines kw join (select knownwineid,vintage,rating,max(pqratio) as maxpqratio,min(priceeuroex) as minpriceeuroex from materializedadvice "+whereclause+" group by knownwineid,vintage "+getHavingclause()+" order by maxpqratio desc, minpriceeuroex limit "+(page)*resultsperpage+","+limit+") selection on kw.id=selection.knownwineid order by selection.maxpqratio desc, minpriceeuroex desc;";
					//Dbutil.logger.info(query);

				} else if (searchtype.equals(Searchtypes.P)){
					query="select * from knownwines kw join (select knownwineid,vintage,rating,max(pqratio) as maxpqratio,min(priceeuroex) as minpriceeuroex from materializedadvice "+whereclause+" group by knownwineid,vintage "+getHavingclause()+" order by minpriceeuroex,maxpqratio desc limit "+(page)*resultsperpage+","+limit+") selection on kw.id=selection.knownwineid order by minpriceeuroex,selection.rating desc;";
				} else {
					query="select * from knownwines kw join (select knownwineid,vintage,rating,min(priceeuroex) as minpriceeuroex from materializedadvice "+whereclause+" group by knownwineid,vintage "+getHavingclause()+" order by rating desc, minpriceeuroex limit "+(page)*resultsperpage+","+limit+") selection on kw.id=selection.knownwineid order by selection.rating desc,selection.minpriceeuroex;";
				}
				//Dbutil.logger.info(query);
				//rs=Dbutil.selectQueryFromMemory(query,"materializedadvice", con);
				//String id="";
				//while (rs.next()){
				//	id+=", "+rs.getString("id");
				//}
				//if (!"".equals(id)){
				//	id=id.substring(1);	
				//query="select * from wineview wv join ratinganalysis ra on (ra.knownwineid=wv.knownwineid and ra.vintage=wv.vintage) join ("+query+") selection on wv.id=selection.id where ra.author='FWS' order by selection.pqratio desc;";
				doPerformanceLog("WineAdvice before query");
				rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
				doPerformanceLog("WineAdvice query");


				if (rs!=null){
					rs.last();
					int size=rs.getRow();
					rs.beforeFirst();
					if (size>0){
						wine=new Wine[size];
						rating=new int[size];
						//ratinghigh=new int[size];
						int i=0;
						while (rs.next()){
							wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("wine")), rs.getString("vintage"), (float)0.75, (float)0.0, rs.getDouble("minpriceeuroex"), rs.getDouble("minpriceeuroex"), (double)0.0, "",
									0, "", "", "", "", rs.getString("id"), false,"",rs.getInt("knownwineid"),null);
							wine[i].Region=rs.getString("appellation");
							rating[i]=rs.getInt("rating"); //suggestions html
							rs2=Dbutil.selectQuery("select * from ratedwines where knownwineid="+rs.getInt("knownwineid")+" and vintage="+rs.getInt("vintage")+" order by author;", con);
							wine[i].Ratings=new ArrayList<Winerating>();
							while (rs2.next()){
								Winerating wr=new Winerating(rs2.getString("author"),rs2.getInt("rating"),rs2.getInt("ratinghigh"));
								wr.link=Winerating.getLink(rs2.getString("author"), rs2.getString("name"));
								wine[i].Ratings.add(wr);
							}
							rs2.close();

							//ratinghigh[i]=0;
							i++;
						}
					}
					/*
				doPerformanceLog("WineAdvice creating wineset");

				query="SELECT found_rows() as thecount;";
				rs=Dbutil.selectQuery(query, con);
				doPerformanceLog("WineAdvice query count start");
				if (rs.next()){
					rows=rs.getInt("thecount");
				}
					 */
				}

			} catch (Exception e){
				Dbutil.logger.error("Problem while getting wineadvice: ",e);
			}
			Dbutil.closeRs(rs);
			Dbutil.closeRs(rs2);
			Dbutil.closeConnection(con);
			writetocache();
		} else{
			//Dbutil.logger.info("In wineAdvice.getAdvice() cache:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
			//Dbutil.logger.info("cache advice");
		}

	}


	public String getAdviceHTML(){
		if (page>1) {
			loggerinfo="Wine guide "+(json?searchtype+" page "+page:"initial load");
		}
		WineAdvice cached=WineAdvice.readfromcache(this.hashCode());
		if (cached!=null&&cached.html!=null){
			//Dbutil.logger.info("In wineAdvice cache:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
			return cached.html;
		}
		if (cached!=null&&cached.wine!=null) wine=cached.wine;
		//Dbutil.logger.info("Not in wineAdvice cache:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
		Translator t=new Translator();
		t.setLanguage(Translator.languages.EN);
		NumberFormat nf=new DecimalFormat("#.##");
		JSONObject returnobject=new JSONObject();
		JSONArray result=new JSONArray();
		StringBuffer html=new StringBuffer();
		if (wine!=null){
			//Dbutil.logger.info("Getting page "+page+" to "+(page+pages)+" for pane "+activepane);
			Wine w;
			for (int i=0;i<wine.length;i++){
				w=wine[i];
				if (page==0&&i==0){
					String vintagetext="";
					if (vintagemin>0){
						if (vintagemax>vintagemin){
							vintagetext="from #vintagemin# to #vintagemax# ";
						} else if (vintagemin==vintagemax){
							vintagetext="from #vintagemin# ";
						} else {
							vintagetext="younger than #vintagemin# ";
						}
					} else if (vintagemax>0){
						vintagetext="older than #vintagemax# ";
					}
					vintagetext=vintagetext.replaceAll("#vintagemin#", vintagemin+"").replaceAll("#vintagemax#", vintagemax+"");
					String ratingtext="";
					if (ratingmin>80){
						if (ratingmax>ratingmin&&ratingmax<100){
							ratingtext="rated between #ratingmin# and #ratingmax# points ";
						} else if (ratingmin==ratingmax){
							ratingtext="rated #ratingmin# points ";
						} else {
							ratingtext="rated at least #ratingmin# points ";
						}
					} else if (ratingmax>0&&ratingmax<100){
						ratingtext="rated lower than #ratingmax# points ";
					}
					ratingtext=ratingtext.replaceAll("#ratingmin#", ratingmin+"").replaceAll("#ratingmax#", ratingmax+"");
					String h1=((vintagetext+ratingtext+region+type.toString()+grape+pricemax).equals("AllALLTYPES0.0")||(vintagetext+ratingtext+region+type.toString()+grape+pricemax).equals("AllALLTYPES200.0"))?"Results:":"Selection: "+(type.equals("")||type.equals(Winetypes.ALLTYPES)?"":Webroutines.formatCapitals(type.toString())+" ")+"wines "+vintagetext+(region.equals("")||region.equals("All")?"":"from "+region+" ")+ratingtext+(grape.equals("")?"":"made from "+(grape.contains("blend")?"a ":"")+grape)+(pricemax==0||pricemax==200?"":" under "+Webroutines.getCurrencySymbol(currency)+(Math.round(pricemax)));
					//Dbutil.logger.info(vintagetext+ratingtext+region+type.toString()+grape+pricemax);
					if (json) {
						if(rows>0){
							try {
								returnobject.put("h1", h1);

							} catch (JSONException e) {
								Dbutil.logger.error("Problem while adding h1 to advicehtml",e);
							}
						}
					}else {
						html.append("<h1>"+h1+"</h1>");
						html.append("<ul class='tabs'  id='gstabs'><li><a href='#'>Best value</a></li><li><a href='#storewineinfo'>By Rating</a></li><li><a href='#'>By Price</a></li></ul><div class='panes' id='gspanes'><div class='pan' id='pane1'>");
						html.append("<div class='items' id='items1'>");
					}

				}
				if (i%resultsperpage==0){
					html.append("<div class='page'>");
				}
				Knownwine kw=new Knownwine(w.Knownwineid);
				kw.getProperties();
				String[] type=Knownwines.getIcon(w.Knownwineid,18);
				html.append("<div class='wa' >");
				if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+w.Knownwineid+".jpg").exists()){
					html.append("<img class='wrlabel"+(i%resultsperpage>0?" bottom":"")+"' src='/labels/"+w.Knownwineid+".jpg' alt='"+w.Name+"'/>");
				} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+w.Knownwineid+".gif").exists()){
					html.append("<img class='wrlabel"+(i%resultsperpage>0?" bottom":"")+"' src='/labels/"+w.Knownwineid+".gif' alt='"+w.Name+"'/>");
				}
				html.append("<div class='wn'>"+(i+1+page*resultsperpage)+". "+"<a href='"+Webroutines.winelink(kw.uniquename,Integer.parseInt(w.Vintage))+"'  title='"+w.Name.replaceAll("'", "&apos;")+"' target='_blank'>"+(w.Vintage.equals("0")?"":w.Vintage)+" "+w.Name.replaceAll("\"", "&quot;")+"</a></div>");
				html.append("<div class='detail'>");
				html.append("<div class='reg'>");
				for (int q=0;q<kw.getProperties().get("locale").split(", ").length;q++){
					html.append("<span class='cc' onclick='javascript:setRegionbyvalue(&quot;"+kw.getProperties().get("locale").split(", ")[q].replace("'", "&apos;")+"&quot;);'>"+kw.getProperties().get("locale").split(", ")[q]+"</span>"+(q<kw.getProperties().get("locale").split(", ").length-1?" &raquo; ":""));
				}
				html.append("</div>");
				html.append("<div class='gt'><span class='"+(type[1].toLowerCase().contains("white")?"sprite sprite-grapewhite":"sprite sprite-grapered")+"'><span class='cc' onclick='javascript:setGrape(&quot;"+kw.getProperties().get("grapes")+"&quot;);'>"+kw.getProperties().get("grapes")+"</span>");
				html.append("<img src='/images/transparent.gif' class='sprite sprite-"+type[0].replaceAll(".jpg", "")+"' alt='"+type[1]+"'/>"+type[1]+"</span></div>");
				html.append("<div class='sprite sprite-zchateau'>"+kw.getProperties().get("producer")+"</div>");
				html.append("</div>");
				html.append("<div class='ras'>");
				for (Winerating r:w.Ratings){
					html.append("<div class='au'>"+(r.link==null?"":"<a href='/winelinks.jsp?knownwineid="+w.Knownwineid+"&amp;vintage="+w.Vintage+"&amp;source="+r.author+"'  target='_blank'>")+r.author+(r.link==null?"":"</a>")+"</div><div class='po'>"+(r.link==null?"":"<a href='/winelinks.jsp?knownwineid="+w.Knownwineid+"&amp;vintage="+w.Vintage+"&amp;source="+r.author+"'  target='_blank'>")+nf.format(r.ratinglow)+(r.ratinghigh>0?"-"+nf.format(r.ratinghigh):"")+(r.link==null?"":"</a>")+"</div>");
					//html.append("<div class='au'>"+(r.link==null?"":"<a href='"+r.link+"' target='_blank'>")+r.author+(r.link==null?"":"</a>")+"</div><div class='po'>"+(r.link==null?"":"<a href='"+r.link+"' target='_blank'>")+nf.format(r.ratinglow)+(r.ratinghigh>0?"-"+nf.format(r.ratinghigh):"")+(r.link==null?"":"</a>")+"</div>");
					//html.append("<div class='au'>"+r.author+"</div><div class='po'>"+nf.format(r.ratinglow)+(r.ratinghigh>0?"-"+nf.format(r.ratinghigh):"")+"</div>");
				}
				html.append("</div>");
				//html.append("<div class='pr'><div class='pre'>"+Webroutines.formatPrice(w.priceeuroex, w.PriceEuroEx, "EUR", "IN")+"</div>");
				//html.append("<div class='pra'>"+Webroutines.formatPrice(w.PriceEuroIn, w.PriceEuroEx, "USD", "IN")+"</div>");
				//html.append("</div>");
				html.append("<div class='act'><div class='sprite sprite-magnglass'><a href='"+Webroutines.winelink(kw.uniquename,Integer.parseInt(w.Vintage))+"'  title='"+w.Name.replaceAll("'", "&apos;")+"' target='_blank'>"+Webroutines.formatPrice(w.PriceEuroIn, w.PriceEuroEx, currency, "EX")+"</a></div>");
				//html.append("<div class='wish'>Wishlist</div>");
				html.append("</div></div>");
				if ((i+1)%resultsperpage==0||i+1==wine.length){
					html.append("<div style='clear:both; margin:0; padding:0; font-size:0;line-height:0;'><!--&nbsp;--></div>");
					//html.append((rows>0?rows+" results, page":"Page")+" "+((int)(i+resultsperpage-1)/resultsperpage+page)+(rows>0?" of "+(1+(int)Math.floor((double)((rows-1)/resultsperpage))):""));//page
					html.append("Page "+((int)(i+resultsperpage-1)/resultsperpage+page)+" of <span class='numpages'>"+(1+(int)Math.floor((double)((rows-1)/resultsperpage)))+"</span>");//page
					html.append("</div><!--page-->");//page
					if (json) {
						result.put(html.toString());
						html=new StringBuffer();
					} else {
						html.append("</div><!--items-->"); //items
						html.append("<div id='next1' class='nextPage'><a>Next</a></div><div id='prev1' class='prevPage nav'><a>Previous</a></div>");

						html.append("</div>\n<div class='pan' id='pane2'><div class='items' id='items2'><div class='page'></div></div><!--items--><div id='next2' class='nav nextPage'><a>Next</a></div><div id='prev2'  class='prevPage'><a>Previous</a></div></div>\n<div class='pan' id='pane3'><div class='items' id='items3'><div class='page'></div></div><!--items--><div id='next3' class='nav nextPage'><a>Next</a></div><div id='prev3'  class='nav prevPage'><a>Previous</a></div></div></div><div class='clearboth'></div>"); //panes
						this.html=html.toString();
						if (cached==null) {
							writetocache();
						} else {
							cached.html=this.html.toString();
							cached.writetocache();
						}
						return html.toString();
					}

				}
			}	
			try{
				returnobject.put("pane", activepane);
				returnobject.put("result", result);
				if (wine.length<(pages*resultsperpage)) {
					returnobject.put("lastpage", "true");
				} else {
					returnobject.put("lastpage", "false");
				}
			} catch (JSONException e) {
				Dbutil.logger.error("Problem while adding result to advicehtml",e);
			}
			this.html=returnobject.toString();
			if (cached==null) {
				writetocache();
			} else {
				cached.html=this.html.toString();
				cached.writetocache();
			}
			return (returnobject.toString());
		} else {
			if (json){
				try{

					returnobject.put("pane", activepane);
					returnobject.put("result", "");
					returnobject.put("lastpage", "true");
					if (cached==null) {
						writetocache();
					} else if (this.html!=null){
						cached.html=this.html.toString();
						cached.writetocache();
					}
					return (returnobject.toString());
				}catch (JSONException e) {
					Dbutil.logger.error("Problem while empty JSON advice advicehtml",e);
				}
			} 
			html.append("<h1>No results found. Please widen your search criteria.</h1>");
			html.append("<ul class='tabs'  id='gstabs'><li><a href='#'>Best value</a></li><li><a href='#storewineinfo'>By Rating</a></li><li><a href='#'>By Price</a></li></ul><div class='panes' id='gspanes'><div class='pan' id='pane1'>");
			html.append("<div class='items' id='items1'>");
			html.append("</div><!--items-->"); //items
			html.append("<div id='next1' class='nextPage'><a>Next</a></div><div id='prev1' class='prevPage nav'><a>Previous</a></div>");
			html.append("</div>\n<div class='pan' id='pane2'><div class='items' id='items2'><div class='page'></div></div><!--items--><div id='next2' class='nav nextPage'><a>Next</a></div><div id='prev2'  class='prevPage'><a>Previous</a></div></div>\n<div class='pan' id='pane3'><div class='items' id='items3'><div class='page'></div></div><!--items--><div id='next3' class='nav nextPage'><a>Next</a></div><div id='prev3'  class='nav prevPage'><a>Previous</a></div></div></div><div class='clearboth'></div>"); //panes
			return(html.toString());

		}
	}



	public String getNavigation(int p){
		Translator t=new Translator();
		t.setLanguage(Translator.languages.EN);
		int lastpage=(int) Math.ceil((double)getRows()/getResultsperpage());
		String navigation="";
		navigation+="<div class='nav'>Page ";
		navigation+=("<a onclick='javascript:getPage("+p+");'>"+p+"</a>&nbsp;&nbsp;&nbsp;");

		navigation+=("</div><div class='clear'/> ");
		return navigation;
	}
	/*
	public String getNavigationOrig(){
		Translator t=new Translator();
		t.setLanguage(Translator.languages.EN);
		int lastpage=(int) Math.ceil((double)getRows()/getResultsperpage());
		int startpage=pagenum-2;
		if (startpage<1) startpage=1;
		int endpage=startpage+4;
		if (endpage>lastpage){
			endpage=lastpage;
			startpage=endpage-4;
			if (startpage<1) startpage=1;
		}
		String navigation="";
		navigation+="<div class='nav'>Page ";
		if (pagenum>1) navigation+=("<a onclick='javascript:getPage("+((pagenum-2)*getLimit())+");'>"+t.get("previous")+"</a>&nbsp;&nbsp;&nbsp;");
		if (startpage>1) navigation+=("<a onclick='javascript:getPage(0);'"+(1==pagenum?" class='pgsel'":"")+">1</a>&hellip;");
		for (int i=startpage;i<=endpage;i++){
			navigation+=("<a onclick='javascript:getPage("+((i-1)*getLimit())+");'"+(i==pagenum?" class='pgsel'":"")+">"+i+"</a>");
		}
		if (lastpage>endpage) navigation+=("&hellip;<a onclick='javascript:getPage("+(lastpage-1)*getLimit()+");'>"+lastpage+"</a>");
		if (pagenum<lastpage) navigation+=("<a onclick='javascript:getPage("+((pagenum)*getLimit())+");return;'>"+t.get("next")+"</a>");

		navigation+=("</div><div class='clear'/> ");
		return navigation;
	}

	 */


	public String whereClauseFromType(){
		if (type.equals("All")) return "";
		if (type.equals("Red (dry)")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where color='Red' and dryness='Dry' and sparkling=0","typeid");
		if (type.equals("White (dry)")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='White' and dryness='Dry' and sparkling=0","typeid");
		if (type.equals("Ros� (dry)")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='Ros�' and dryness='Dry' and sparkling=0","typeid");
		if (type.equals("Red - Fortified")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='Red' and dryness='Fortified' and sparkling=0","typeid");
		if (type.equals("Red - Sweet/Dessert")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='Red' and dryness='Sweet/Dessert' and sparkling=0","typeid");
		if (type.equals("Red - Sparkling")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='Red' and sparkling=1","typeid");
		if (type.equals("White - Fortified")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='White' and dryness='Fortified' and sparkling=0","typeid");
		if (type.equals("White - Sweet/Dessert")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='White' and dryness='Sweet/Dessert' and sparkling=0","typeid");
		if (type.equals("White - Off-dry")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='White' and dryness='Off-dry' and sparkling=0","typeid");
		if (type.equals("White - Sparkling")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='White' and sparkling=1","typeid");
		if (type.equals("Ros� - Sparkling")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='Ros�' and sparkling=1","typeid");
		if (type.equals("Ros� - Sweet/Dessert")) return "and winetypecode = "+Dbutil.readValueFromDB("select typeid from winetypecoding where  color='Ros�' and dryness='Sweet/Dessert' and sparkling=0","typeid");
		return"";
	}


	public String getCountryofseller() {
		return countryofseller;
	}
	public void setCountryofseller(String countryofseller) {
		this.countryofseller = countryofseller;
	}

	public String getCurrency() { 
		return currency;
	}
	public void setCurrency(String currency) {
		if (currency.equals("undefined")) currency="EUR";
		if (currency==null||currency.equals("")) currency="EUR"; 
		this.currency = currency;
	}
	public String getGrape() {
		return grape;
	}

	public void setGrape(String grape) {
		this.grape = grape;
	}

	public boolean isJson() {
		return json;
	}

	public void setJson(boolean json) {
		this.json = json;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public float getPricemax() {
		return pricemax;
	}
	public void setPricemax(float pricemax) {
		this.pricemax = pricemax;
	}
	public float getPricemin() {
		return pricemin;
	}
	public void setPricemin(float pricemin) {
		this.pricemin = pricemin;
	}
	public int getRatingmax() {
		return ratingmax;
	}
	public void setRatingmax(int ratingmax) {
		this.ratingmax = ratingmax;
	}
	public int getRatingmin() {
		return ratingmin;
	}
	public void setRatingmin(int ratingmin) {
		this.ratingmin = ratingmin;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public int getResultsperpage() {
		return resultsperpage;
	}

	public void setResultsperpage(int resultsperpage) {
		this.resultsperpage = resultsperpage;
	}

	public Searchtypes getSearchtype() {
		return searchtype;
	}

	public void setSearchtype(Searchtypes searchtype) {
		this.searchtype = searchtype;
	}

	public int getShopid() {
		return shopid;
	}

	public void setShopid(int shopid) {
		if (shopid!=this.shopid){
			continent=Dbutil.readValueFromDB("select * from shops join vat on (shops.countrycode=vat.countrycode) where shops.id="+shopid, "continent");
			if (!"EU".equals(continent)&&!"UC".equals(continent)) continent="AL";
		}
		this.shopid = shopid;
	}

	public String getVintage() {
		return vintage;
	}
	public void setVintage(String vintage) {
		this.vintage = vintage;
	}


	public void setWinetype(String winetypestring) {
		try{this.type=Winetypes.valueOf(winetypestring);}catch (Exception e){}

	}
	public String getWinetype() {
		return winetype;
	}



	public void setSubregions(boolean subregions) {
		this.subregions = subregions;
	}


	public String getWinetypecoding() {
		return winetypecoding;
	}


	public void setWinetypecoding(String winetypecoding) {
		this.winetypecoding = winetypecoding;
	}


	public int getOffset() {
		return offset;
	}


	public void setOffset(int offset) {
		this.offset = offset;
	}


	

	public void setActivepane(String panestr) {
		int pane=0;
		try{pane=Integer.parseInt(panestr.substring(0, 1));}catch(Exception e){}
		this.activepane=pane;
		switch (pane){
		case 1:setSearchtype(Searchtypes.PQ);break;
		case 2:setSearchtype(Searchtypes.Q);break;
		case 3:setSearchtype(Searchtypes.P);break;
		case 4:setSearchtype(Searchtypes.DA);break;
		}
	}

	
	public static int getDefaultlimit() {
		return defaultlimit;
	}


	public static void setDefaultlimit(int defaultlimit) {
		WineAdvice.defaultlimit = defaultlimit;
	}


	public int getRows() {
		return rows;
	}


	public void setRows(int rows) {
		this.rows = rows;
	}


	public String getScale() {
		return scale;
	}


	public void setScale(String scale) {
		this.scale = scale;
	}

	protected void setFacets(){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			String whereclause=getWhereclause("");
			String query="";
			facets.priceupperlimit=priceupperlimit;
			/*
			query="select min(minpriceeuroin) as minpriceeuroin, max(minpriceeuroin) as maxpriceeuroin from (select min(priceeuroin) as minpriceeuroin from materializedadvice "+whereclause+" group by knownwineid,vintage) sel  ;";
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			perf+="Pricerange: "+(DateTime.now().getValue()-start)+", ";
			start=DateTime.now().getValue();
			if (rs!=null&&rs.next()){
				facets.pricemin=rs.getFloat("minpriceeuroin");
				facets.pricemax=rs.getFloat("maxpriceeuroin");
			}
			Dbutil.closeRs(rs);
			 */
			String havingclause=getHavingclause();
			Dbutil.executeQuery("CREATE temporary TABLE `wijn`.`tempwineadvice` (`knownwineid` INTEGER UNSIGNED NOT NULL, `vintage` INTEGER UNSIGNED NOT NULL, `rating` INTEGER UNSIGNED,`grapes` INTEGER UNSIGNED NOT NULL, `winetypecode` INTEGER UNSIGNED NOT NULL, `country` varchar(2), `lft` INTEGER UNSIGNED NOT NULL, `minpriceeuroex` double) ENGINE = MEMORY;",con);
			Dbutil.executeQuery("insert into tempwineadvice select knownwineid,vintage,if(rating is null,0,rating),grapes,if(winetypecode is null,0,winetypecode),country,lft,min(priceeuroex) as minpriceeuroex from materializedadvice "+whereclause+" group by knownwineid,vintage "+havingclause+";", con);
			rs=Dbutil.selectQuery("select count(*) as thecount from tempwineadvice;",con);
			if (rs.next()){
				rows=rs.getInt("thecount");
			}
			Dbutil.closeRs(rs);
			doPerformanceLog("tempwineadvice");
			facets.winetypecodes=new Integer[13];
			for (int i=0;i<facets.winetypecodes.length;i++) facets.winetypecodes[i]=0;
			if (!type.equals(Winetypes.ALLTYPES)){
				String tempwhereclause=getWhereclause("type");
				query="select winetypecode,count(*) as thecount from (select winetypecode,min(priceeuroex)  as minpriceeuroex from materializedadvice "+tempwhereclause+" group by knownwineid,vintage "+getHavingclause()+") sel group by winetypecode;";
			}else {
				query="select winetypecode,count(*) as thecount from tempwineadvice group by winetypecode;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("winetypes");
			if (rs!=null) while (rs.next()){
				facets.winetypecodes[rs.getInt("winetypecode")]=rs.getInt("thecount");
			}
			Dbutil.closeRs(rs);
			if(vintagemin>0||(vintagemax>0&&vintagemax<2100)){
				String tempwhereclause=getWhereclause("vintage");
				query="select vintage,count(*) as thecount from (select vintage,min(priceeuroex)  as minpriceeuroex from materializedadvice "+tempwhereclause+" group by knownwineid,vintage "+getHavingclause()+" ) sel group by vintage order by vintage ;";
			} else {
				query="select vintage,count(*) as thecount from tempwineadvice group by vintage order by vintage ;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("vintage");
			facets.vintage=new LinkedHashMap<Integer, Integer>();
			facets.vintagemin=0;
			facets.vintagemax=0;
			if (rs!=null) while (rs.next()){
				if (vintagemin>=rs.getInt("vintage")) facets.vintagemin=rs.getInt("vintage");
				if (facets.vintagemax==0&&vintagemax>0&&vintagemax<=rs.getInt("vintage")) facets.vintagemax=rs.getInt("vintage");
				facets.vintage.put(rs.getInt("vintage"),rs.getInt("thecount"));
			}
			Dbutil.closeRs(rs);
			if (!grape.equals("")){
				String tempwhereclause=getWhereclause("grape");
				query="select grapename from (select grapes from (select grapes,min(priceeuroex) as minpriceeuroex from materializedadvice "+tempwhereclause+" group by knownwineid,vintage "+getHavingclause()+" ) sel group by grapes order by grapes)sel2 join grapes on (sel2.grapes=grapes.id) ;";
			} else {
				query="select * from (select grapes from tempwineadvice group by grapes order by grapes)sel2 join grapes on (sel2.grapes=grapes.id) ;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("grapes");
			facets.grape=new LinkedHashMap<String, Integer>();
			if (rs!=null) while (rs.next()){
				facets.grape.put(rs.getString("grapename"),1);
			}
			Dbutil.closeRs(rs);
			if (ratingmin>80||ratingmax<100){
				String tempwhereclause=getWhereclause("rating");
				query="select min(rating) as ratingmin, max(rating) as ratingmax from (select rating,min(priceeuroex)  as minpriceeuroex from materializedadvice "+tempwhereclause+(tempwhereclause.equals("")?" where rating>79":" and rating>79")+" group by knownwineid,vintage "+getHavingclause()+")sel;";
			} else {
				query="select min(rating) as ratingmin, max(rating) as ratingmax from tempwineadvice where rating>79;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("rating");
			if (rs!=null) if (rs.next()){
				facets.ratingmin=rs.getInt("ratingmin");
				facets.ratingmax=rs.getInt("ratingmax");
			}
			Dbutil.closeRs(rs);
			if (!countryofseller.equals("All")){
				String tempwhereclause=getWhereclause("countryofseller");
				query="select * from vat join (select country,count(*) as thecount from (select country,min(priceeuroex)  as minpriceeuroex from materializedadvice "+tempwhereclause+(tempwhereclause.equals("")?" where vintage>0":" and vintage>0")+" group by knownwineid,vintage,country "+getHavingclause()+" ) sel group by country) sel2 on (vat.countrycode=sel2.country) order by vat.country;";
			} else {
				query="select * from vat join (select country,count(*) as thecount from tempwineadvice group by country) sel2 on (vat.countrycode=sel2.country) order by vat.country;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("countries");
			facets.countryofseller=new LinkedHashMap<String, String>();
			facets.countryofseller.put("EU","Europe");
			facets.countryofseller.put("UC","North America");
			if (rs!=null) while (rs.next()){
				facets.countryofseller.put(rs.getString("countrycode"),rs.getString("country"));
			}
			Dbutil.closeRs(rs);
			if (!facets.countryofseller.containsKey("US")&&!facets.countryofseller.containsKey("CA")) facets.countryofseller.remove("UC");
			if (facets.countryofseller.containsKey("UC")&&facets.countryofseller.size()<4) facets.countryofseller.remove("EU");

			if (!region.equals("")&&!region.equals("All")){
				String tempwhereclause=getWhereclause("region");
				query="select distinct kb2.shortregion from (select distinct(lft) as lft from materializedadvice "+tempwhereclause+(tempwhereclause.equals("")?" where lft>0":" and lft>0")+getwhereclausePrice()+") sel join kbregionhierarchy kb1 on (sel.lft=kb1.lft) join kbregionhierarchy kb2 on (kb1.lft>=kb2.lft and kb1.rgt<=kb2.rgt) order by shortregion;";
			} else {
				query="select distinct kb2.shortregion from (select distinct(lft) as lft from tempwineadvice where lft>0) sel join kbregionhierarchy kb1 on (sel.lft=kb1.lft) join kbregionhierarchy kb2 on (kb1.lft>=kb2.lft and kb1.rgt<=kb2.rgt) order by shortregion;";
			}
			//Dbutil.logger.info(query);
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("regions");
			facets.region=new LinkedHashMap<String, Integer>();
			try{
				if (rs!=null) while (rs.next()){
					facets.region.put(rs.getString("shortregion"),1);
				}
			}catch (Exception e){
				Dbutil.logger.error("Problem while getting wineadvice: ",e);
			}
			Dbutil.closeRs(rs);
			facets.subregion=new LinkedHashMap<String, Integer>();
			if (!region.equals("")&&!region.equals("All")){
				query="select shortregion, count(*) as thecount from (select distinct(lft) as lft from tempwineadvice where lft>0) sel join kbregionhierarchy kb1 on (sel.lft=kb1.lft) group by shortregion order by count(*) desc, shortregion;";
				//Dbutil.logger.info(query);
				rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
				doPerformanceLog("subregions");
				try{
					if (rs!=null) while (rs.next()){
						if (!region.equalsIgnoreCase(rs.getString("shortregion"))) facets.subregion.put(rs.getString("shortregion"),rs.getInt("thecount"));
					}
				}catch (Exception e){
					Dbutil.logger.error("Problem while getting wineadvice: ",e);
				}
				Dbutil.closeRs(rs);
			}

		} catch (Exception e){
			Dbutil.logger.error("Problem while getting wineadvice: ",e);
		} finally{
			Dbutil.executeQuery("drop temporary table if exists tempwineadvice", con);
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
			WineAdvice cached=WineAdvice.readfromcache(this.hashCode());
			if (cached==null) {
				writetocache();
			} else {
				cached.facets=this.facets;
				cached.rows=this.rows;
				cached.writetocache();
			}


		}

	}

	public int getVintagemin() {
		return vintagemin;
	}

	public void setVintagemin(int vintagemin) {
		this.vintagemin = vintagemin;
	}

	public int getVintagemax() {
		return vintagemax;
	}

	public void setVintagemax(int vintagemax) {
		this.vintagemax = vintagemax;
	}

	public String getWineHtml(String wineid){
		doPerformanceLog("init");
		StringBuffer html=new StringBuffer();
		Wine w=null;
		try{
			w=new Wine(wineid);
		} catch (Exception e){
			return "";
		}
		shopid=w.ShopId;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query="select sel.*,group_concat(concat(rw.author,':',rw.rating,if (rw.ratinghigh>0,concat('-',rw.ratinghigh),''))) as ratings from (select w.id,w.name,w.vintage,w.size,w.knownwineid,kw.appellation,w.priceeuroex,pq.price/w.priceeuroex*w.size/0.75 as pq, case when (pq.price/w.priceeuroex*w.size/0.75)>1.2 then 5 when (pq.price/w.priceeuroex*w.size/0.75)>1.1 then 4 when (pq.price/w.priceeuroex*w.size/0.75)>1 then 3 when (pq.price/w.priceeuroex*w.size/0.75)>0.9 then 2 when (pq.price/w.priceeuroex*w.size/0.75)>.8 then 1 else 0 END as pqstars, w.priceeuroex/bp.priceeuroex as relprice, case when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.0001 then 5 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.05 then 4 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.10 then 3 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.15 then 2 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.2 then 1 else 0 end as pricestars, kw.wine, ra.rating, w.priceeuroex as minpriceeuroex from wines w left join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') left join pqratio pq on (ra.rating=pq.rating) left join bestprices bp on (w.knownwineid=bp.knownwineid and w.vintage=bp.vintage and continent='"+continent+"' and n>1 ) left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding where w.id="+w.Id+") sel left join ratedwines rw on (sel.knownwineid=rw.knownwineid and sel.vintage=rw.vintage) group by sel.id;";
			rs = Dbutil.selectQuery(rs, query, con);
			doPerformanceLog("wine data");
			if (rs.next()) {
				w.pricestars=rs.getInt("pricestars");
				w.pqstars=rs.getInt("pqstars");

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		if (w!=null){
			String shopcurrency=Dbutil.readValueFromDB("select * from shops where exvat=1 and id="+w.ShopId+" and currency='"+getCurrency()+"';", "currency");
			boolean pricematch=false;
			if (shopcurrency.length()>0) pricematch=true;
			String[] type=Knownwines.getIcon(w.Knownwineid,18);
			NumberFormat nf=new DecimalFormat("#.##");
			html.append("<table><tr><td>");
			html.append("<h2>Selected wine</h2>");
			html.append("</td><td></td><td>");
			if (w.Knownwineid>0){
				html.append("<h2>Background information</h2>");
			}
			html.append("</td></tr><tr><td class='left'>");
			//html.append("<div class='content left cell'>");
			html.append("<div class='name'>");
			html.append(((w.Vintage.equals("")||w.Vintage.equals("0")||w.Name.startsWith(w.Vintage))?"":w.Vintage+" ")+Spider.escape(w.Name));
			html.append("</div>");
			if (pricematch){
				html.append("<h2>Price: "+Webroutines.getCurrencySymbol(Dbutil.readValueFromDB("select * from shops where id="+w.ShopId, "currency"))+" "+Webroutines.formatPrice((double)w.Price)+(w.Size>0?", size: "+Webroutines.formatSizecompact(w.Size):"")+"</h2>");
			} else {
				html.append("<h2>Price: "+Webroutines.formatPrice(w.PriceEuroIn,w.PriceEuroEx,getCurrency(),"EX")+(w.Size>0?", size: "+Webroutines.formatSizecompact(w.Size):"")+"</h2>");
			}

			html.append("<h3>Price as advertized on site: "+Webroutines.getCurrencySymbol(Dbutil.readValueFromDB("select * from shops where id="+w.ShopId, "currency"))+" "+Webroutines.formatPrice((double)w.Price)+(Dbutil.readIntValueFromDB("select * from shops where id="+w.ShopId, "exvat")==0?" (incl. VAT)":" (excl. VAT)")+"</h3>");
			//html.append("<a href='/external.jsp?wineid="+w.Id+"' >View this wine on "+w.Shopurl+"</a>");
			html.append("<span class='jslink' onclick=\"$('#storeiframe').attr('src','"+ExternalManager.addGoogleParams(w.SourceUrl.replaceAll("'", "&apos;"))+"');$('#storetabs').tabs().click(0);\" >View this wine on "+w.Shopurl+"</span>");
			html.append("<div class='addedon'>Added to our index on "+w.createdate+", last updated on "+w.lastupdate+"</div>");
			html.append("<form id='wineinfo' onsubmit='return addtocart()' ><input type='hidden' name='action' value='addtocart'/><input type='hidden' name='wineid' value='"+w.Id+"'/><input type='text' name='amount' value='1' size='1'/> bottles<input type='button' value='Add to shoppinglist' onclick='addtocart();'/></form>");
			html.append("</td><td class='center'></td>");
			if (w.Knownwineid>0){
				Knownwine kw=new Knownwine(w.Knownwineid);
				doPerformanceLog("init Knownwine data");
				HashMap<String,String>props=kw.getProperties();
				doPerformanceLog("Knownwine data");
				html.append("<td class='right'>");
				if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+w.Knownwineid+".jpg").exists()){
					html.append("<div class='label'><img class='label' src='"+Configuration.staticprefix+"/labels/"+w.Knownwineid+".jpg' alt='"+kw.name+"'/></div>");
				} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+w.Knownwineid+".gif").exists()){
					html.append("<div style='position:relative;z-index:100;'><img class='label' src='"+Configuration.staticprefix+"/labels/"+w.Knownwineid+".gif' alt='"+kw.name+"'/></div>");
				}

				html.append("<div class='wa content cell' id='info"+w.Id+"'><div class='wn'><a href='"+Webroutines.winelink((kw.uniquename),Integer.parseInt(w.Vintage))+"' >"+(w.Vintage.equals("0")?"":w.Vintage)+" "+Spider.escape(kw.name).replaceAll("\"", "&quot;")+"</a></div>");
				html.append("<div class='detail'>");
				html.append("<div class='reg'>");
				for (int q=0;q<kw.getProperties().get("locale").split(", ").length;q++){
					html.append("<span class='cc' onclick='javascript:setRegionbyvalue(&quot;"+kw.getProperties().get("locale").split(", ")[q]+"&quot;);'>"+kw.getProperties().get("locale").split(", ")[q]+"</span>"+(q<kw.getProperties().get("locale").split(", ").length-1?" &raquo; ":""));
				}
				html.append("</div>");
				html.append("<div class='gt'><span class='sprite sprite-"+(type[1].toLowerCase().contains("white")?"grapewhite":"grapered")+"'><span class='cc' onclick='javascript:setGrape(&quot;"+kw.getProperties().get("grapes")+"&quot;);'>"+kw.getProperties().get("grapes")+"</span>");
				html.append("<img src='/images/"+type[0]+"' alt='"+type[1]+"'/>"+type[1]+"</span></div>");
				html.append("<div class='sprite sprite-zchateau'>"+kw.getProperties().get("producer")+"</div>");
				int n=Dbutil.readIntValueFromDB("select n as thecount from bestprices where knownwineid="+w.Knownwineid+" and vintage="+w.Vintage+" and continent='"+continent+"';", "thecount");
				if (n>1) html.append("<div class='offerings'>Market price is based on <a href='/wine/"+Webroutines.URLEncode(Webroutines.removeAccents(kw.name))+(w.Vintage.equals("0")?"":"?vintage="+w.Vintage)+"&amp;country="+continent+"' >"+n+" offers</a> for this wine</div>");

				html.append("</div>");
				html.append("<div class='ras'>");
				if (w.Ratings!=null) for (Winerating r:w.Ratings){
					html.append("<div class='au'>"+(r.link==null?"":"<a href='/winelinks.jsp?knownwineid="+w.Knownwineid+"&amp;vintage="+w.Vintage+"&amp;source="+r.author+"' >")+r.author.replaceAll("&", "&amp;")+(r.link==null?"":"</a>")+"</div><div class='po'>"+(r.link==null?"":"<a href='/winelinks.jsp?knownwineid="+w.Knownwineid+"&amp;vintage="+w.Vintage+"&amp;source="+r.author+"' >")+nf.format(r.ratinglow)+(r.ratinghigh>0?"-"+nf.format(r.ratinghigh):"")+(r.link==null?"":"</a>")+"</div>");
				}
				if (w.pricestars>0) {
					html.append("<div class='starslabel'>Market price:</div><div class='stars'>");
					for (int s=0;s<w.pricestars;s++) html.append("<img src='/css/star.gif' alt='star'/>");
					html.append("</div>");
				} else if (n>1){
					html.append("<div class='starslabel'>Market price:</div><div class='stars'>");
					html.append("<img src='/css/starred.gif' alt='star'/>");
					html.append("</div>");
				}if (w.pqstars>0) {
					html.append("<div class='starslabel'>P/Q ratio:</div><div class='stars'>");
					for (int s=0;s<w.pqstars;s++) html.append("<img src='/css/starsilver.gif' alt='star'/>");
					html.append("</div>");
				}

				html.append("</div>");
				html.append("</div>");
			} else {
				html.append("<td>");
			}
			html.append("</td></tr></table>");



		}

		return html.toString();
	}

	public String getWineJson(String wineidstr,PageHandler p){
		JSONObject returnobject=new JSONObject();
		try {
			returnobject.put("winehtml",getWineHtml(wineidstr));
		} catch (JSONException e) {
			Dbutil.logger.error("Cannot fetch wine info:",e);
		}
		return returnobject.toString();
	}

	public String getAdJson(String wineidstr,PageHandler p){
		JSONObject returnobject=new JSONObject();
		try {
			int wineid=0;
			try{wineid=Integer.parseInt(wineidstr);}catch(Exception e){}
			if (wineid>0)	{
				doPerformanceLog("init ad");
				RecommendationAd ra=new RecommendationAd(wineid);
				ra.setShopid(shopid);
				ra.setCountrycode(StoreInfo.getStore(shopid).countrycode);
				String ad=ra.getAd(p,"Storead");
				if (ad.length()>0) returnobject.put("adrighthtml", ad);
				doPerformanceLog("Got ad");
			}
		} catch (JSONException e) {
			Dbutil.logger.error("Cannot fetch wine info:",e);
		}
		return returnobject.toString();
	}


	public void doPerformanceLog(String subject){
		timerlog+=subject+": "+(DateTime.now().getValue()-timer)+" ms. ";
		timer=DateTime.now().getValue();
	}

	public Facets getFacets(){
		if (facets!=null) return facets;
		WineAdvice cached=WineAdvice.readfromcache(this.hashCode());
		if (cached!=null&&cached.facets!=null){
			//Dbutil.logger.info("In wineAdvice.facets cache:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
			rows=cached.rows;
			facets=cached.facets;
			return facets;
		} else {
			//Dbutil.logger.info("Not in "+this.getClass().getName()+" facets cache:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));

		}
		if (facets==null) {
			facets=new Facets();
			setFacets();
		}
		return facets;
	}

	public boolean equals(WineAdvice w){
		//Dbutil.logger.info("Comparison");
		if (vintagemin!=w.vintagemin) return false;
		if (vintagemax!=w.vintagemax) return false;
		if (ratingmin!=w.ratingmin) return false;
		if (ratingmax!=w.ratingmax) return false;
		if (resultsperpage!=w.resultsperpage) return false;
		if (page!=w.page) return false;
		if (offset!=w.offset) return false;
		if (shopid!=w.shopid) return false;
		if (subregions!=w.subregions) return false;
		if (json!=w.json) return false;
		if (pricemin!=w.pricemin) return false;
		if (pricemax!=w.pricemax) return false;
		if (excludewineid!=w.excludewineid) return false;
		if (!region.toLowerCase().equals(w.region.toLowerCase())) return false;
		if (!searchtype.equals(w.searchtype)) return false;
		if (!type.equals(w.type)) return false;
		if (!countryofseller.equals(w.countryofseller)) return false;
		if (!grape.equals(w.grape)) return false;
		if (!currency.equals(w.currency)) return false;
		if (!continent.equals(w.continent)) return false;
		return true;
	}

	public String hashString(){
		return (vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency);
	}
	
	public int hashCode(){
		int code=(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency).hashCode();
		return code;
	}

	public static class Facets implements Serializable{

		private static final long serialVersionUID = 1L;
		float pricemin=0;
		float pricemax=0;
		int ratingmin=0;
		int ratingmax=100;
		int vintagemin=0;
		int vintagemax=100;
		Integer[] winetypecodes=new Integer[13];
		Integer priceupperlimit=0;
		LinkedHashMap<Integer,Integer> vintage;
		LinkedHashMap<String,String> countryofseller;
		public LinkedHashMap<String,Integer> region;
		public LinkedHashMap<String,Integer> subregion;
		LinkedHashMap<String,Integer> grape;

		public LinkedHashMap<String,Integer> getGrape(){
			return grape;
		}


	}

	public void refreshCache(){
		cache.remove(this);
		getAdvice();
		getAdviceHTML();
		getFacets(); 
	}

	public static void refreshCompleteCache(){
		Dbutil.logger.info("Refreshing WineAdvice and ShopAdvice cache");
		WineAdvice wa;
		cache.clear();
		for (String cs:new String[]{"EU","UC","All","NL"}){
			for (String c:new String[]{"EUR","USD","GBP","CHF"}){
				wa=new WineAdvice();
				wa.json=false;
				wa.resultsperpage=5;
				wa.countryofseller=cs;
				wa.currency=c;
				wa.refreshCache(); 
			}
		}
		ShopAdvice sa;
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		ArrayList<Integer> shopids=new ArrayList<Integer>();
		try {
			query = "select * from shops where disabled=0";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				shopids.add(rs.getInt("id"));
			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		for (String c:new String[]{"EUR","USD","GBP","CHF"}){
			Iterator<Integer> i=shopids.iterator();
			while (i.hasNext()){
				int shopid=i.next();

				sa=new ShopAdvice();
				sa.json=false;
				sa.resultsperpage=20;
				sa.shopid=shopid;
				sa.currency=c;
				sa.refreshCache(); 

			}

		}
		Dbutil.logger.info("Finished refreshing WineAdvice and ShopAdvice cache");
		
	}

	public static void main(String[] args){
		refreshCompleteCache();
	}

	@Override
	public void run() {
		refreshCompleteCache();

	}


}
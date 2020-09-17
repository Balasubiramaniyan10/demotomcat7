package com.freewinesearcher.online;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.batch.CurrencyClient;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.freewinesearcher.online.WineAdvice.Searchtypes;
import com.freewinesearcher.online.WineAdvice.Winetypes;
import com.google.gdata.data.DateTime;

public class ShopAdvice extends WineAdvice {


	@Override
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
			whereclause.append(" AND w.priceeuroex>="+CurrencyClient.toEuro(pricemin, currency));
			if (logarguments) searchinfo+="priceeuroex>="+CurrencyClient.toEuro(pricemin, currency)+" ";
		}
		if (pricemax>0&&pricemax<priceupperlimit){
			whereclause.append(" AND w.priceeuroex<="+CurrencyClient.toEuro(pricemax, currency));
			if (logarguments) searchinfo+="priceeuroex<="+CurrencyClient.toEuro(pricemax, currency)+" ";
		}

		/*if (!"vintage".equals(skip)&&!vintage.equals("")){
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
						vintageclause=vintageclause+"w.Vintage between "+from+
						" AND "+to;
					} else {
						vintageclause=vintageclause+"w.Vintage="+vintages[i];
					}
				}
			}
			if (!vintageclause.equals("")) whereclause.append(" AND ("+vintageclause+")");
			if (logarguments) searchinfo+="vintage="+vintage+" ";
		}
		 */
		if (!"vintage".equals(skip)&&(vintagemin>0||vintagemax>0)){
			if (vintagemin>0) vintageclause=" and w.Vintage>="+vintagemin;
			if (vintagemax>0&&vintagemax<5000) vintageclause+=" and w.Vintage<="+vintagemax;
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

					rs=Dbutil.selectQuery("select lft,rgt from kbregionhierarchy where shortregion like '"+Spider.SQLEscape(region)+"';", con);
					if (rs.next()){
						lft=rs.getInt("lft");
						rgt=rs.getInt("rgt");
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
					whereclause.append(" AND w.lft>="+lft+" and w.rgt<="+rgt+" ");
				} else {
					whereclause.append(" AND w.lft="+lft+" and w.rgt="+rgt+" ");
				}
			}
		}

		if (!"grape".equals(skip)&&!grape.equals("")){
			whereclause.append(" AND grapes='"+Spider.SQLEscape(grape)+"'");
			if (logarguments) searchinfo+="grape="+grape+" ";

		}
		if (!"shopid".equals(skip)&&shopid>0){
			whereclause.append(" AND shopid="+shopid);
		}
		if (!"rating".equals(skip)&&ratingmin>80){
			whereclause.append(" AND ra.rating>="+ratingmin);
			if (logarguments) searchinfo+="rating>="+ratingmin+" ";
		}
		if (!"rating".equals(skip)&&ratingmax<100&&ratingmax>0){
			whereclause.append(" AND (ra.rating is null OR ra.rating<="+ratingmax+")");
			if (logarguments) searchinfo+="rating<="+ratingmax+" ";
		}

		if (!"countryofseller".equals(skip)&&!countryofseller.equals("All")){
			whereclause.append(" AND country='"+countryofseller+"'");
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
				whereclause.append(" AND typeid in("+winetypecoding+")");
				if (logarguments) searchinfo+="type="+type+" ";
			}
		}
		//whereclause.append(" AND rating>0 ";


		if (whereclause.length()>4) return "where "+whereclause.substring(4);
		Dbutil.logger.info("whereclause empty in shopadvice for "+shopid);
		return "";

	}



	@Override
	public void getAdvice(){
		ShopAdvice cached=null;
		try{cached=(ShopAdvice)WineAdvice.readfromcache(this.hashCode());}catch(Exception e){
			Dbutil.logger.info("Got a Wineadvice from cache instead of shopadvice...");
		}
		if (cached==null||cached.wine==null){
			//Dbutil.logger.info("not in cache ShopAdvice.getAdvice:"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
			ResultSet rs=null;
			ResultSet rs2=null;
			Connection con=Dbutil.openNewConnection();
			wine=null;
			int limit=pages*resultsperpage;
			try{
				String whereclause=getWhereclause("");
				String query="";
				if (searchtype.equals(Searchtypes.PQ)){
					query="select sel.*,group_concat(concat(ra.author,':',ra.rating,if (ra.ratinghigh>0,concat('-',ra.ratinghigh),''))) as ratings from (select w.id,w.name,w.price,w.vintage,w.size,w.knownwineid,kw.appellation,w.priceeuroex,pq.price/w.priceeuroex*w.size/0.75 as pq, case when (pq.price/w.priceeuroex*w.size/0.75)>1.2 then 5 when (pq.price/w.priceeuroex*w.size/0.75)>1.1 then 4 when (pq.price/w.priceeuroex*w.size/0.75)>1 then 3 when (pq.price/w.priceeuroex*w.size/0.75)>0.9 then 2 when (pq.price/w.priceeuroex*w.size/0.75)>.8 then 1 else 0 END as pqstars, w.priceeuroex/bp.priceeuroex as relprice, case when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.0001 then 5 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.05 then 4 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.10 then 3 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.15 then 2 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.2 then 1 else 0 end as pricestars, kw.wine, ra.rating, w.priceeuroex as minpriceeuroex from wines w left join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') left join pqratio pq on (ra.rating=pq.rating) left join bestprices bp on (w.knownwineid=bp.knownwineid and w.vintage=bp.vintage and continent='"+continent+"' and n>1 ) left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+(whereclause.equals("")?"where ":whereclause+" and")+" w.shopid="+shopid+" order by (pqstars+pricestars) desc,pq desc limit "+(page)*resultsperpage+","+limit+") sel left join ratinganalysis ra on (sel.knownwineid=ra.knownwineid and sel.vintage=ra.vintage and ra.knownwineid>0) group by sel.id order by (pqstars+pricestars) desc,pq desc;";
				} else if (searchtype.equals(Searchtypes.P)){
					query="select sel.*,group_concat(concat(ra.author,':',ra.rating,if (ra.ratinghigh>0,concat('-',ra.ratinghigh),''))) as ratings from (select w.id,w.name,w.price,w.vintage,w.size,w.knownwineid,kw.appellation,w.priceeuroex,pq.price/w.priceeuroex*w.size/0.75 as pq, case when (pq.price/w.priceeuroex*w.size/0.75)>1.2 then 5 when (pq.price/w.priceeuroex*w.size/0.75)>1.1 then 4 when (pq.price/w.priceeuroex*w.size/0.75)>1 then 3 when (pq.price/w.priceeuroex*w.size/0.75)>0.9 then 2 when (pq.price/w.priceeuroex*w.size/0.75)>.8 then 1 else 0 END as pqstars, w.priceeuroex/bp.priceeuroex as relprice, case when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.0001 then 5 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.05 then 4 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.10 then 3 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.15 then 2 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.2 then 1 else 0 end as pricestars, kw.wine, ra.rating, w.priceeuroex as minpriceeuroex from wines w left join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') left join pqratio pq on (ra.rating=pq.rating) left join bestprices bp on (w.knownwineid=bp.knownwineid and w.vintage=bp.vintage and continent='"+continent+"' and n>1 ) left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+(whereclause.equals("")?"where ":whereclause+" and")+" w.shopid="+shopid+" order by priceeuroex,(pqstars+pricestars) desc,pq desc limit "+(page)*resultsperpage+","+limit+") sel left join ratinganalysis ra on (sel.knownwineid=ra.knownwineid and sel.vintage=ra.vintage and ra.knownwineid>0) group by sel.id order by priceeuroex,(pqstars+pricestars) desc,pq desc;";
				} else if (searchtype.equals(Searchtypes.Q)){
					query="select sel.*,group_concat(concat(ra.author,':',ra.rating,if (ra.ratinghigh>0,concat('-',ra.ratinghigh),''))) as ratings from (select w.id,w.name,w.price,w.vintage,w.size,w.knownwineid,kw.appellation,w.priceeuroex,pq.price/w.priceeuroex*w.size/0.75 as pq, case when (pq.price/w.priceeuroex*w.size/0.75)>1.2 then 5 when (pq.price/w.priceeuroex*w.size/0.75)>1.1 then 4 when (pq.price/w.priceeuroex*w.size/0.75)>1 then 3 when (pq.price/w.priceeuroex*w.size/0.75)>0.9 then 2 when (pq.price/w.priceeuroex*w.size/0.75)>.8 then 1 else 0 END as pqstars, w.priceeuroex/bp.priceeuroex as relprice, case when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.0001 then 5 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.05 then 4 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.10 then 3 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.15 then 2 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.2 then 1 else 0 end as pricestars, kw.wine, ra.rating, w.priceeuroex as minpriceeuroex from wines w left join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') left join pqratio pq on (ra.rating=pq.rating) left join bestprices bp on (w.knownwineid=bp.knownwineid and w.vintage=bp.vintage and continent='"+continent+"' and n>1 ) left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+(whereclause.equals("")?"where ":whereclause+" and")+" w.shopid="+shopid+" order by rating desc,priceeuroex limit "+(page)*resultsperpage+","+limit+") sel left join ratinganalysis ra on (sel.knownwineid=ra.knownwineid and sel.vintage=ra.vintage and ra.knownwineid>0) group by sel.id order by rating desc,priceeuroex;";
				} else {
					query="select sel.*,group_concat(concat(ra.author,':',ra.rating,if (ra.ratinghigh>0,concat('-',ra.ratinghigh),''))) as ratings from (select w.id,w.createdate,w.name,w.price,w.vintage,w.size,w.knownwineid,kw.appellation,w.priceeuroex,pq.price/w.priceeuroex*w.size/0.75 as pq, case when (pq.price/w.priceeuroex*w.size/0.75)>1.2 then 5 when (pq.price/w.priceeuroex*w.size/0.75)>1.1 then 4 when (pq.price/w.priceeuroex*w.size/0.75)>1 then 3 when (pq.price/w.priceeuroex*w.size/0.75)>0.9 then 2 when (pq.price/w.priceeuroex*w.size/0.75)>.8 then 1 else 0 END as pqstars, w.priceeuroex/bp.priceeuroex as relprice, case when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.0001 then 5 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.05 then 4 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.10 then 3 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.15 then 2 when (w.priceeuroex/bp.priceeuroex*0.75/w.size)<1.2 then 1 else 0 end as pricestars, kw.wine, ra.rating, w.priceeuroex as minpriceeuroex from wines w left join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') left join pqratio pq on (ra.rating=pq.rating) left join bestprices bp on (w.knownwineid=bp.knownwineid and w.vintage=bp.vintage and continent='"+continent+"' and n>1 ) left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+(whereclause.equals("")?"where ":whereclause+" and")+" w.shopid="+shopid+" order by createdate desc,(pqstars+pricestars) desc,pq desc limit "+(page)*resultsperpage+","+limit+") sel left join ratinganalysis ra on (sel.knownwineid=ra.knownwineid and sel.vintage=ra.vintage and ra.knownwineid>0) group by sel.id order by createdate desc,(pqstars+pricestars) desc,pq desc;";
				}
				doPerformanceLog("WineAdvice before query");
				rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
				doPerformanceLog("WineAdvice query");
				//Dbutil.logger.info(query);
				if (rs!=null){
					rs.last();
					int size=rs.getRow();
					rs.beforeFirst();
					if (size>0){
						wine=new Wine[size];
						rating=new int[size];
						ratinghigh=new int[size];
						int i=0;
						while (rs.next()){
							wine[i]= new Wine(Webroutines.formatCapitals(rs.getString("name")), rs.getString("vintage"), rs.getFloat("size"), rs.getFloat("price"), rs.getDouble("minPriceEuroex"), rs.getDouble("minPriceEuroex"), (double)0.0, "",
									0, "", "", "", "", rs.getString("id"), false,"",rs.getInt("knownwineid"),null);
							wine[i].Region=rs.getString("appellation");
							wine[i].pricestars=rs.getInt("pricestars");
							wine[i].pqstars=rs.getInt("pqstars");
							rating[i]=rs.getInt("rating");
							if (rs.getString("ratings")!=null){
								wine[i].Ratings=new ArrayList<Winerating>();
								for (String r:rs.getString("ratings").split(",")){
									wine[i].Ratings.add(new Winerating(r.split(":")[0],Double.parseDouble(r.split(":")[1].split("-")[0]),Double.parseDouble((r.split(":")[1].split("-").length>1?r.split(":")[1].split("-")[1]:"0"))));
								}
							}
							i++;
						}
					}
				}

			} catch (Exception e){
				Dbutil.logger.error("Problem while getting wineadvice: ",e);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeRs(rs2);
				Dbutil.closeConnection(con);
				if (cached==null) {
					writetocache();
				} else{ 
					cached.wine=wine;
					cached.writetocache();
				}
			}
		} else {
			this.wine=((ShopAdvice)WineAdvice.readfromcache(this.hashCode())).wine;
		}


	}



	@Override
	public String getAdviceHTML(){
		ShopAdvice cached=(ShopAdvice)WineAdvice.readfromcache(this.hashCode());
		if (cached!=null&&cached.html!=null){
			return cached.html;
		} else {
			//Dbutil.logger.info("not in cache ShopAdvice.getAdviceHTML():"+(vintagemin+"/"+vintagemax+"/"+ratingmin+"/"+ratingmax+"/"+resultsperpage+"/"+page+"/"+offset+"/"+shopid+"/"+subregions+"/"+json+"/"+pricemin+"/"+pricemax+"/"+excludewineid+"/"+region+"/"+searchtype+"/"+type+"/"+countryofseller+"/"+grape+"/"+continent+"/"+currency));
		}
		if (cached!=null&&cached.wine!=null) wine=cached.wine;
		doPerformanceLog("WineAdvice before html");
		String shopcurrency=Dbutil.readValueFromDB("select * from shops where exvat=1 and id="+shopid+" and currency='"+getCurrency()+"';", "currency");
		boolean pricematch=false;
		if (shopcurrency.length()>0) pricematch=true;
		if (page>1) loggerinfo="Store "+(json?searchtype+" page "+page:"initial load");
		Translator t=new Translator();
		t.setLanguage(Translator.languages.EN);
		JSONObject returnobject=new JSONObject();
		JSONArray result=new JSONArray();
		if (wine!=null){
			StringBuffer html=new StringBuffer();
			Wine w;
			String rating;

			for (int i=0;i<wine.length;i++){
				w=wine[i];
				if (page==0&&i==0){
					String h1=(vintage+region+type.toString()+grape+pricemax).equals("AllALLTYPES0.0")||(vintage+region+type.toString()+grape+pricemax).equals("AllALLTYPES200.0")?"Results:":"Selection: "+(vintage.equals("")?"":vintage+" ")+(type.equals("")||type.equals(Winetypes.ALLTYPES)?"":Webroutines.formatCapitals(type.toString())+" ")+"wines "+(region.equals("")||region.equals("All")?"":"from "+region+" ")+(grape.equals("")?"":"made from "+(grape.contains("blend")?"a ":"")+grape)+(pricemax==0||pricemax==200?"":" under "+Webroutines.getCurrencySymbol(currency)+(Math.round(pricemax)));
					if (json) {
						try {
							returnobject.put("h1", h1);

						} catch (JSONException e) {
							Dbutil.logger.error("Problem while adding h1 to advicehtml",e);
						}
					}else {
						html.append("<h1>"+h1+"</h1>");
						html.append("<ul class='tabs' id='gstabs'><li><a href='#value' class='s'>Best value</a></li><li><a href='#rating' class='s'>By Rating</a></li><li><a href='#price' class='s'>By Price</a></li><li><a href='#newest'>Newest arrivals</a></li></ul><div class='panes' id='gspanes'><div class='pan' id='pane1'>");
						html.append("<div class='items' id='items1'>");
					}

				}
				if (i%resultsperpage==0){
					html.append("<div class='page'>");
					html.append("<div class='results' ><div class='brd' >");
					html.append("<table class='results'>");
				}
				html.append("\n<tr");
				if (i%2==1){html.append(" class=\"odd\"");}
				html.append(">");
				
				html.append("<td class='winename' id='wine"+activepane+"-"+w.Id+"'><a style='color:#4D0027' href='"+(w.Knownwineid==0?"/":Webroutines.winelink(Knownwines.getUniqueKnownWineName(w.Knownwineid),0))+"' title='"+w.Name.replaceAll("'", "&apos;")+"'  onclick='javascript:return showWine("+w.Id+",&quot;wine"+activepane+"-"+w.Id+"&quot;);'>"+Spider.escape(("0".equals(w.Vintage)?"":w.Vintage+" ")+Webroutines.formatCapitals(w.Name))+"</a></td>");
				html.append("<td class='rating'>" + ((rating=w.getAverageRating()+"").equals("0")?"":rating)+"</td>");
				html.append("<td class='size'>" + Webroutines.formatNewSize(w.Size)+"</td>");
				html.append("<td class='price'>" + (pricematch?Webroutines.formatPrice((double)w.Price):Webroutines.formatPriceNoCurrency(w.PriceEuroIn,w.PriceEuroEx,currency,"EX"))+"</td>");
				html.append("<td class='currency'>" + Webroutines.getCurrencySymbol(currency)+"</td>");
				html.append("<td class='marketprice'>");
				for (int s=0;s<w.pricestars;s++) html.append("<img src='/css/star.gif' alt='star'/>");
				html.append("</td>");
				html.append("</tr>");



				if ((i+1)%resultsperpage==0||i+1==wine.length){
					html.append("</table><!--results--></div>");//page
					html.append("Page "+((int)(i+19)/resultsperpage+page)+" of <span class='numpages'>"+(1+(int)Math.floor((double)((rows-1)/resultsperpage)))+"</span>");//page
					html.append("</div><!--results-->");//results
					html.append("</div><!--page-->");//page
					if (json) {
						result.put(html.toString());
						html=new StringBuffer();
					} else {
						html.append("</div><!--items-->"); //items
						html.append("<div id='next1' class='nextPage'><a>Next</a></div><div id='prev1' class='prevPage nav'><a>Previous</a></div>");

						html.append("</div>\n<div class='pan' id='pane2'><div class='items' id='items2'><div class='page'></div></div><!--items--><div id='next2' class='nav nextPage'><a>Next</a></div><div id='prev2'  class='prevPage'><a>Previous</a></div></div>\n<div class='pan' id='pane3'><div class='items' id='items3'><div class='page'></div></div><!--items--><div id='next3' class='nav nextPage'><a>Next</a></div><div id='prev3'  class='nav prevPage'><a>Previous</a></div></div>\n<div class='pan' id='pane4'><div class='items' id='items4'><div class='page'></div></div><!--items--><div id='next4' class='nav nextPage'><a>Next</a></div><div id='prev4'  class='nav prevPage'><a>Previous</a></div></div></div>"); //panes
						this.html=html.toString();
						if (cached==null) {
							writetocache();
						} else {
							cached.html=this.html;
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
			doPerformanceLog("WineAdvice after html");

			this.html=returnobject.toString();
			if (cached==null) {
				writetocache();
			} else {
				cached.html=this.html;
				cached.writetocache();
			}
			return (returnobject.toString());
		} else {
			if (json){
				try{
					returnobject.put("pane", activepane);
					returnobject.put("pages", pages);
					returnobject.put("result", "");
					returnobject.put("lastpage", "true");
					this.html=returnobject.toString();
					if (cached==null) {
						writetocache();
					} else {
						cached.html=this.html;
						cached.writetocache();
					}
					return (returnobject.toString());
				}catch (JSONException e) {
					Dbutil.logger.error("Problem while empty JSON advice advicehtml",e);
				}
			} 
			return("No results found. Please widen your search criteria.");

		}
	}


	@Override
	protected void setFacets(){
		ResultSet rs=null;
		Connection con=Dbutil.openNewConnection();
		try{
			String whereclause=getWhereclause("");
			String query="";
			String rajoin="";
			if (ratingmin>80||ratingmax<100) rajoin=" left join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') ";
			facets.priceupperlimit=priceupperlimit;
			String havingclause=getHavingclause();
			rs=Dbutil.selectQuery("select count(*) as thecount from wines w  left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+";",con);
			if (rs.next()){
				rows=rs.getInt("thecount");
			}
			Dbutil.closeRs(rs);
			doPerformanceLog("tempwineadvice");
			facets.winetypecodes=new Integer[13];
			for (int i=0;i<facets.winetypecodes.length;i++) facets.winetypecodes[i]=0;
			if (!type.equals(Winetypes.ALLTYPES)){
				String tempwhereclause=getWhereclause("type");
				query="select typeid,count(*) as thecount from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+tempwhereclause+" and w.shopid="+shopid+" group by typeid;";
			}else {
				query="select typeid,count(*) as thecount from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+" and w.shopid="+shopid+" group by typeid;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("winetypes");
			if (rs!=null) while (rs.next()){
				facets.winetypecodes[rs.getInt("typeid")]=rs.getInt("thecount");
			}
			Dbutil.closeRs(rs);
			if(vintagemin>0||(vintagemax>0&&vintagemax<2100)){
				String tempwhereclause=getWhereclause("vintage");
				query="select w.vintage,count(*) as thecount from wines w  left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+tempwhereclause+" and w.shopid="+shopid+" group by vintage order by vintage;";
			}else {
				query="select w.vintage,count(*) as thecount from wines w  left join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+" and w.shopid="+shopid+" group by vintage order by vintage;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("vintage");
			facets.vintage=new LinkedHashMap<Integer, Integer>();
			facets.vintagemin=vintagemin;
			facets.vintagemax=vintagemax;
			if (rs!=null) while (rs.next()){
				facets.vintage.put(rs.getInt("vintage"),rs.getInt("thecount"));
				//if (facets.vintagemin>rs.getInt("vintage"))facets.vintagemin=rs.getInt("vintage");
				//if (facets.vintagemax<rs.getInt("vintage"))facets.vintagemax=rs.getInt("vintage");
			}
			//Dbutil.logger.info(facets.vintagemax);
			Dbutil.closeRs(rs);
			if (!grape.equals("")){
				String tempwhereclause=getWhereclause("grape");
				query="select grapes,count(*) as thecount from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+tempwhereclause+" and w.shopid="+shopid+" group by grapes;";
			}else {
				query="select grapes,count(*) as thecount from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+" and w.shopid="+shopid+" group by grapes;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("grapes");
			facets.grape=new LinkedHashMap<String, Integer>();
			if (rs!=null) while (rs.next()){
				facets.grape.put(rs.getString("grapes"),1);
			}
			Dbutil.closeRs(rs);
			if (ratingmin>80||ratingmax<100){
				String tempwhereclause=getWhereclause("rating");
				query="select min(rating) as ratingmin, max(rating) as ratingmax from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') "+tempwhereclause+" and rating>79;";
			} else {
				query="select min(rating) as ratingmin, max(rating) as ratingmax from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding join ratinganalysis ra on (w.knownwineid=ra.knownwineid and w.vintage=ra.vintage and ra.author='FWS') "+whereclause+" and rating>79;";
			}
			rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
			doPerformanceLog("rating");
			if (rs!=null) if (rs.next()){
				facets.ratingmin=rs.getInt("ratingmin");
				facets.ratingmax=rs.getInt("ratingmax");
			}
			Dbutil.closeRs(rs);
			if (!region.equals("")&&!region.equals("All")){
				String tempwhereclause=getWhereclause("region");
				query="select distinct(shortregion) as shortregion from (select distinct w.lft,w.rgt from wines w join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+tempwhereclause+" and w.lft>0) sel  join kbregionhierarchy on (sel.lft>=kbregionhierarchy.lft and sel.rgt<=kbregionhierarchy.rgt) order by shortregion;";
				//query="select distinct(appellation) as appellation from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+tempwhereclause+" and w.shopid="+shopid+";";
			}else {
				query="select distinct(shortregion) as shortregion from (select distinct w.lft,w.rgt from wines w join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+" and w.lft>0) sel  join kbregionhierarchy on (sel.lft>=kbregionhierarchy.lft and sel.rgt<=kbregionhierarchy.rgt) order by shortregion;";
				//query="select distinct(appellation) as appellation from wines w  join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+" and w.shopid="+shopid+";";
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
				query="select appellation, count(*) as thecount from wines w join knownwines kw on (w.knownwineid=kw.id) natural left join winetypecoding "+rajoin+whereclause+" and w.lft>0 group by appellation order by count(*) desc,  appellation;";
				//Dbutil.logger.info(query);
				rs=Dbutil.selectQueryFromMemory(query, "materializedadvice",con);
				doPerformanceLog("subregions");
				try{
					if (rs!=null) while (rs.next()){
						if (!region.equalsIgnoreCase(rs.getString("appellation"))) facets.subregion.put(rs.getString("appellation"),rs.getInt("thecount"));
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
			ShopAdvice cached=null;
			try{cached=(ShopAdvice)WineAdvice.readfromcache(this.hashCode());}catch(Exception e){}// on very rare occasions, wineadvice and shopadvice may lead to same hash code
			if (cached==null) {
				writetocache();
			} else {
				cached.facets=this.facets;
				cached.rows=this.rows;
				cached.writetocache();
			}
		}

	}

	public static void refreshBestPrices(){
		Dbutil.executeQuery("truncate table bestprices");
		Dbutil.executeQuery("insert into bestprices select knownwineid,vintage, continent, min(priceeuroex) as minpriceeuroex,count(*) from wines join shops on (wines.shopid=shops.id) join vat on (shops.countrycode=vat.countrycode) where size=0.75 and knownwineid>0 and vintage>0 group by knownwineid, vintage,continent;");
		Dbutil.executeQuery("insert into bestprices select knownwineid,vintage, 'AL', min(priceeuroex) as minpriceeuroex,count(*) from wines join shops on (wines.shopid=shops.id) join vat on (shops.countrycode=vat.countrycode) where size=0.75 and knownwineid>0 and vintage>0 group by knownwineid, vintage;");
		Dbutil.executeQuery("update shops join (select shopid,max(createdate) as lastwine from wines where knownwineid>0 group by shopid) sel on (shops.id=sel.shopid) set lastnewwine=lastwine;");
	}
}

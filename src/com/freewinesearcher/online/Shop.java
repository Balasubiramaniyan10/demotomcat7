package com.freewinesearcher.online;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.freewinesearcher.common.Dbutil;

public class Shop implements  Serializable{

	private static final long serialVersionUID = 1L;
	public double lat=0;
	public double lon=0;
	public String address="";
	public String name="";
	public int id=0;
	public LinkedHashMap<String,Double> regions=new LinkedHashMap<String, Double>();
	public LinkedHashMap<String,Double> subregions=new LinkedHashMap<String, Double>();
	public LinkedHashMap<Integer,Integer> prices=new LinkedHashMap<Integer, Integer>();
	public int avgvintage=0;
	public int numberofwines=0;
	public float avgpriceeuroex;
	public float medianpriceeuroex;
	public Date lastupdate;
	public String shopurl="";
	public int pricescale=10;
	public String description="";
	public String affiliateparams="";
	public String countrycode=""; 
	
	public Shop(int id){
		this.id=id;
		String query;
		ResultSet rs;
		String result="";
		Connection con=Dbutil.openNewConnection();
		if (id>0){
			try{
				query="select * from shops where id="+id+";";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					lat=rs.getDouble("lat");
					lon=rs.getDouble("lon");
					address=rs.getString("address");
					name=rs.getString("shopname");
					shopurl=rs.getString("shopurl");
					countrycode=rs.getString("countrycode");
					description=rs.getString("description");
					affiliateparams=rs.getString("affiliateparams");
					this.id=rs.getInt("id");
				}
			}catch (Exception e){
				Dbutil.logger.error("",e);
			}
		}
		Dbutil.closeConnection(con);


	}

	public void getShopInfo(){
		int maxregions=5;
		avgvintage=Dbutil.readIntValueFromDB("select avg(vintage) as vintage from wines where shopid="+id+" and vintage>0", "vintage");
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query="select CASE ceiling(priceeuroex/10)*10 WHEN 10 THEN 10 WHEN 20 THEN 20 WHEN 30 THEN 30 WHEN 40 THEN 50 WHEN 60 THEN 100 WHEN 70 THEN 100 WHEN 80 THEN 100 WHEN 80 THEN 100 WHEN 90 THEN 100 WHEN 100 THEN 100 ELSE 150 END as pricerange,count(*) as thecount from wines where shopid="+id+" group by pricerange;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				prices.put(rs.getInt("pricerange"), rs.getInt("thecount"));
				if (rs.getInt("thecount")>pricescale) pricescale=(int) (Math.ceil(rs.getDouble("thecount")/50)*50);
			}
			Dbutil.closeRs(rs);
			query = "select kb.shortregion,count(*) as thecount,kb.id from wines join kbregionhierarchy kb  on (wines.lft>=kb.lft and wines.rgt<=kb.rgt) where shopid="+id+" and kb.parentid=100 group by kb.id order by count(*) desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			int n=0;
			int sum=0;
			int all=1;
			int max=0;
			int maxcount=0;
			int maxpercentage=0;
			while (rs.next()&&n<maxregions&&rs.getInt("thecount")>0) {
				if (rs.getString("shortregion").equals("All")){
					all=rs.getInt("thecount");
				} else {
					if (max==0) {
						max=rs.getInt("id");
						maxcount=rs.getInt("thecount");
						maxpercentage=(rs.getInt("thecount")*100)/all;
					}
					sum+=rs.getInt("thecount");
					if ((rs.getInt("thecount")*100)/all>0){
						regions.put(rs.getString("shortregion"), (double)(rs.getInt("thecount")*100)/all);
						n++;
					}
				}
			}
			if (sum<all){
				regions.put("Other", (double)((all-sum)*100)/all);
			}
			Dbutil.closeRs(rs);
			query = "select kb.shortregion,count(*) as thecount,kb.id from wines join kbregionhierarchy kb  on (wines.lft>=kb.lft and wines.rgt<=kb.rgt) where shopid="+id+" and kb.parentid="+max+" group by kb.id order by count(*) desc;";
			rs = Dbutil.selectQuery(rs, query, con);
			n=0;
			sum=0;
			while (rs.next()&&n<maxregions&&rs.getInt("thecount")>0) {

				sum+=rs.getInt("thecount");
				if ((rs.getInt("thecount")*100)/maxcount>0){
					subregions.put(rs.getString("shortregion"), (double)(rs.getInt("thecount")*100)/maxcount);
					n++;
				}

			}
			if (sum<all){
				subregions.put("Other", (double)((maxcount-sum)*100)/all);
			}

			Dbutil.executeQuery("SET @R:=0;",con);
			query="select priceeuroex from (select priceeuroex,(@R:=@R+1) r from wines where shopid="+id+" order by priceeuroex asc) t where r=((select count(*)+1 from wines where shopid="+id+") DIV 2);";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()){
				medianpriceeuroex=rs.getFloat("priceeuroex");
			}
			Dbutil.closeRs(rs);
			query = "select avg(priceeuroex) as price,count(*) as thecount, max(lastupdated) as lastdate from wines where shopid="+id+";";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()){
				avgpriceeuroex=rs.getFloat("price");
				numberofwines=rs.getInt("thecount");
				lastupdate=rs.getDate("lastdate");
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public String getShopRegionText(PageHandler p){
		String html="This store ";
		Iterator r=regions.keySet().iterator();
		String r1=r.next().toString();
		String r2="";
		Double nr2=(double)0;
		try{
			r2=r.next().toString();
			nr2=regions.get(r2);
		}catch (Exception e){}
		String rareold="";
		if (avgvintage<2000) rareold="rare and old ";

		if (regions.get(r1)>50){
			html+="specializes in "+rareold+"wines from "+r1;
			Iterator s=subregions.keySet().iterator();
			String s1=s.next().toString();
			String s2="";
			try{
				s2=s.next().toString();
			}catch (Exception e){}
			if (subregions.get(s1)>50){
				html+=", especially "+s1+". ";
			} else {
				html+=", mainly from "+s1+" and "+s2+". ";
			}
		} else {
			if (regions.get(r1)+nr2>50){
				html+="mainly has "+rareold+"wines from "+r1+" and "+r2+". ";
			} else {
				html+="has "+rareold+"wines from various countries like "+r1+" and "+r2+". ";
			}
		}
		return html;
	}

	public String getShortShopRegionText(PageHandler p){
		String html="";
		Iterator r=regions.keySet().iterator();
		String r1=r.next().toString();
		String r2="";
		Double nr2=(double)0;
		try{
			r2=r.next().toString();
			nr2=regions.get(r2);
		}catch (Exception e){}
		String rareold="";
		
		if (regions.get(r1)>50){
			Iterator s=subregions.keySet().iterator();
			String s1=s.next().toString();
			String s2="";
			try{
				s2=s.next().toString();
			}catch (Exception e){}
			if (subregions.get(s1)>50){
				html+=""+s1+" specialist";
			} else {
				html+=""+s1+" and "+s2+" specialist";
			}
		} else {
			if (regions.get(r1)+nr2>50){
				html+="From "+r1+", "+r2+" and other countries. ";
			} else {
				html+="From "+r1+", "+r2+" and other countries. ";
				}
		}
		return html;
	}

	public String getShopPriceText(PageHandler p){
		String html="";
		if (medianpriceeuroex>20){
			html+="The wines are generally in the more exclusive segment with a typical price of around &euro; "+Math.round(medianpriceeuroex/10)*10+(avgpriceeuroex>100?", apart from some very expensive ones":"")+". ";
		} else 	if (medianpriceeuroex<10){
			html+="The wines are generally affordable with a typical price of around &euro; "+Math.round(medianpriceeuroex)+". ";
		} else {
			html+="The wines have an average price of around &euro; "+Math.round(medianpriceeuroex/5)*5+",-. ";
		}
		return html;
	}
	public String getShopPriceTextHtml5(PageHandler p){
		String html="";
		double price=medianpriceeuroex;
		String currency=PageHandler.getCurrencyFromCountrycode(countrycode);
		if (!currency.equals("EUR")){
			Double pricefactor=Currency.getRate(currency);
			if (pricefactor!=0.0){
				price = price/pricefactor;
			}
		}
		if (medianpriceeuroex>20){
			html+="The wines are generally in the more exclusive segment with a typical price of <span itemprop='priceRange'>around "+Webroutines.getCurrencySymbol(currency)+" "+Math.round(price/10)*10+"</span>"+(avgpriceeuroex>100?", apart from some very expensive ones":"")+". ";
		} else 	if (medianpriceeuroex<10){
			html+="The wines are generally affordable with a typical price of <span itemprop='priceRange'>around "+Webroutines.getCurrencySymbol(currency)+" "+Math.round(price)+"</span>. ";
		} else {
			html+="The wines have an average price of <span itemprop='priceRange'>around "+Webroutines.getCurrencySymbol(currency)+" "+Math.round(price/5)*5+"</span>,-. ";
		}
		return html;
	}

	public String getShopInfoText(PageHandler p){
		return getShopRegionText(p)+getShopPriceTextHtml5(p)+"<br/><br/>Vinopedia indexes "+(numberofwines>0?numberofwines:"no")+" wines from this merchant. Their price list was updated on "+lastupdate+". ";
	}


	public String getShopStatsText(PageHandler p){
		StringBuffer html=new StringBuffer();
		html.append("<div id='shopstatstext'><h2>Countries</h2>This store carries wines from the following countries:<br/>");
		int n=0;
		for (String r:regions.keySet()) {
			if (Math.round(regions.get(r))>0){  
				n++;
				if (n>1) html.append(", ");
				html.append(r+" ("+Math.round(regions.get(r))+"%)");              
			}   
		}
		n=0;
		html.append("<h2>Wine regions in "+regions.keySet().iterator().next()+":</h2>");
		for (String r:subregions.keySet()) {
			if (Math.round(subregions.get(r))>0){  
				n++;
				if (n>1) html.append(", ");
				html.append(r+" ("+Math.round(subregions.get(r))+"%)");            
			}
		}
		html.append("</div>");
		return html.toString();
	}


	public Shop(String id){
		String query;
		ResultSet rs;
		String result="";
		Connection con=Dbutil.openNewConnection();
		if (!id.equals("")){
			try{
				query="select * from shops where id="+id+" and lat!=0;";
				rs=Dbutil.selectQuery(query, con);
				if (rs.next()){
					lat=rs.getDouble("lat");
					lon=rs.getDouble("lon");
					address=rs.getString("address");
					name=rs.getString("shopname");
					this.id=rs.getInt("id");
				}
			}catch (Exception e){
				Dbutil.logger.error("",e);
			}
		}
		Dbutil.closeConnection(con);

	}

	


}

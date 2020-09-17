package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.common.Wine;
import com.freewinesearcher.common.Winerating;
import com.vinopedia.util.PriceCalculator;

public class RecommendationAd {
	int wineid;
	int vintage=0;
	int knownwineid;
	int lft;
	int rgt;
	int parentlft;
	int parentrgt;
	int shopid=0;
	String countrycode="";
	double minprice=0;
	String targetcountrycode;
	String bannersshown="";

	
	public RecommendationAd(){
		super();
	}

	public RecommendationAd(int knownwineid, String targetcountrycode, int lft, int rgt) {
		super();
		this.knownwineid = knownwineid;
		this.targetcountrycode=targetcountrycode;
		this.lft = lft;
		this.rgt = rgt;

	}

	public RecommendationAd(int wineid) {
		super();
		this.wineid=wineid;
	}

	public RecommendationAd(int knownwineid, String targetcountrycode) {
		super();
		this.targetcountrycode=targetcountrycode;
		this.knownwineid = knownwineid;
	}

	private void getParameterData(){
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			if (wineid>0){
				query = "select * from wines join knownwines on (wines.knownwineid=knownwines.id) join kbregionhierarchy on (knownwines.locale=kbregionhierarchy.region) where wines.id="+wineid;
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					this.shopid=rs.getInt("shopid");
					this.knownwineid=rs.getInt("knownwineid");
					this.vintage=rs.getInt("vintage");
					this.lft = rs.getInt("lft");
					this.rgt = rs.getInt("rgt");
					query = "select * from kbregionhierarchy where id="+rs.getInt("parentid");
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						this.parentlft = rs.getInt("lft");
						this.parentrgt = rs.getInt("rgt");
					}
					Dbutil.closeRs(rs);
					query = "select min(priceeuroex) as minprice from wines where knownwineid="+knownwineid+" and size=0.75";
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						minprice=rs.getDouble("minprice");
					}
				}
			} else {
				query = "select * from knownwines join kbregionhierarchy on (knownwines.locale=kbregionhierarchy.region) where knownwines.id="+knownwineid;
				rs = Dbutil.selectQuery(rs, query, con);
				if (rs.next()) {
					this.lft = rs.getInt("lft");
					this.rgt = rs.getInt("rgt");
					query = "select * from kbregionhierarchy where id="+rs.getInt("parentid");
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						this.parentlft = rs.getInt("lft");
						this.parentrgt = rs.getInt("rgt");
					}
					Dbutil.closeRs(rs);
					query = "select min(priceeuroex) as minprice from wines where knownwineid="+knownwineid+" and size=0.75";
					rs = Dbutil.selectQuery(rs, query, con);
					if (rs.next()) {
						minprice=rs.getDouble("minprice");
					}

				}
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

	}

	public int getWineid() {
		return wineid;
	}

	public void setWineid(int wineid) {
		this.wineid = wineid;
	}

	public int getVintage() {
		return vintage;
	}

	public void setVintage(int vintage) {
		this.vintage = vintage;
	}

	public int getKnownwineid() {
		return knownwineid;
	}

	public void setKnownwineid(int knownwineid) {
		this.knownwineid = knownwineid;
	}

	public int getLft() {
		return lft;
	}

	public void setLft(int lft) {
		this.lft = lft;
	}

	public int getRgt() {
		return rgt;
	}

	public void setRgt(int rgt) {
		this.rgt = rgt;
	}

	public int getParentlft() {
		return parentlft;
	}

	public void setParentlft(int parentlft) {
		this.parentlft = parentlft;
	}

	public int getParentrgt() {
		return parentrgt;
	}

	public void setParentrgt(int parentrgt) {
		this.parentrgt = parentrgt;
	}

	public double getMinprice() {
		return minprice;
	}

	public void setMinprice(double minprice) {
		this.minprice = minprice;
	}

	public String getTargetcountrycode() {
		return targetcountrycode;
	}

	public void setTargetcountrycode(String targetcountrycode) {
		this.targetcountrycode = targetcountrycode;
	}


	public String getCountrycode() {
		return countrycode;
	}

	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}
	public String getBannersshown() {
		return bannersshown;
	}

	public void setBannersshown(String bannersshown) {
		this.bannersshown = bannersshown;
	}

	public String getStoreSkyScraper(){

		String ad="";
		if (shopid>0 && countrycode!=null&&countrycode.length()>0) {
			String query;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			String countries=Webroutines.getCountries(Webroutines.getRegion(countrycode));
			try {
				query = "select * from banners join shops on (shopid=shops.id) where active=1 and hsize=160 and vsize=600 and countrycode in ('"+countries+"') order by (shopid="+shopid+") desc, countrycode='"+countrycode+"' desc,rand() limit 1;";
				rs = Dbutil.selectQuery(query, con);
				if (rs.next()) {
					ad=Configuration.adtext+"<a href='/adhandler.jsp?id="+rs.getString("banners.id")+"' target='_blank' rel='nofollow'>"+rs.getString("html")+"</a>";
					bannersshown=rs.getString("banners.id");
					//Dbutil.executeQuery("update banners set views=views+1 where id="+rs.getInt("id"),con);
				}
				
			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return ad;
	}

	public String getAd(PageHandler p, String type){
		getParameterData();
		StringBuffer sb=new StringBuffer();
		if (knownwineid>0||wineid>0){
			String query;
			String header="";
			PriceCalculator pc=null;
			ResultSet rs = null;
			Connection con = Dbutil.openNewConnection();
			//if (targetcountrycode.equals("ZZ")||targetcountrycode.equals("EU")||targetcountrycode.equals("US")) targetcountrycode="NL";
			try {
				/* No graphical ads used
			int typeid=Dbutil.readIntValueFromDB("SELECT * FROM knownwines natural join winetypecoding where knownwines.id="+knownwineid, "typeid");
			query = "select * from (select wineads.ratings,m.id, m.country,(m.knownwineid="+knownwineid+") as knownwinematch, (m.lft>="+lft+" and rgt <="+rgt+") as regionmatch, (winetypecode="+typeid+") as typematch, if(pqratio is null,0,pqratio) as pqratio , if("+minprice+">0,if(abs ((priceeuroex-"+minprice+")/priceeuroex)<0.5,1,0),1) as priceindex from partners join shops s on (partners.shopid=s.id) join materializedadvice m on (s.id=m.shopid)  join wineads on (m.id=wineads.wineid) where commission>0 and m.lft>="+parentlft+" and rgt <="+parentrgt+" order by regionmatch desc,typematch desc,priceindex desc,pqratio desc limit 6) subsel join wines on (subsel.id=wines.id) left join knownwines on (wines.knownwineid=knownwines.id) order by knownwinematch desc,rand() limit 3;";
			//Dbutil.logger.info(query);
			rs = Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
			if (rs.isBeforeFirst()){ //Graphical Ad available
				sb.append("<div class='recommendationad'>");
				sb.append("<font style='font-face:Arial,sans-serif;font-size: 11px;'><a href='/advertising.jsp' rel='nofollow'><br/>Ads by Vinopedia</a><br/></font>\n<h3>Recommended wines from our partners</h3><table>");
				while (rs.next()) {
					pc=new PriceCalculator(rs.getDouble("priceeuroin"),"EUR",rs.getString("Country"), rs.getDouble("size"),false,false,(rs.getString("sparkling")==null?false:rs.getBoolean("sparkling")));
					pc.calculatePrice(null, targetcountrycode, false, false);
					sb.append("<tr><td><div class='price'>"+Webroutines.getCurrencySymbol(pc.targetcurrency)+"&nbsp;"+Webroutines.formatPrice(pc.targetprice)+"</div>"+(rs.getInt("vintage")>0?"<div class='info'>"+rs.getInt("vintage")+"</div>":"")+"<div class='info'>"+Webroutines.formatSizecompact(rs.getFloat("size")).replace(",",".")+"</div></td></tr>"); 
					String[] r=rs.getString("ratings").split(",");
					String ratinghtml="<tr><td>";
					for (String rat:r){
						if (rat.length()>3&&rat.split(" ").length>1){
							ratinghtml+=("<div class='rating'><div class='author'>"+rat.split(" ")[0]+"</div><div class='points'>"+rat.split(" ")[1]+"</div></div>"); 
						}
					}
					ratinghtml+="</td></tr>";
					sb.append("<tr><td><a href='/adhandler.jsp?type="+type+"&amp;wineid="+rs.getInt("id")+"' target='_blank' alt='Recommended wine' rel='nofollow'><img src='/images/gen/winead/"+rs.getInt("id")+"' /></a></td></tr>");
					if (ratinghtml.length()>20) sb.append(ratinghtml);
					Dbutil.executeQuery("update wineads set views=views+1 where wineid="+rs.getInt("id"), con);
				}
				sb.append("</table>");
				sb.append("<div class='pricenote'>Note: Ad prices are an indication for "+Webroutines.getCountryFromCode(targetcountrycode)+" and will be finalized when ordering.</div>");
				sb.append("</div>");
			}
			Dbutil.closeRs(rs);
				 */
				if (sb.length()==0&&p!=null){
					if (knownwineid==0) knownwineid=-2;
					boolean js=false;
					if (shopid>0){
						query="select * from shops where id="+shopid;
						rs=Dbutil.selectQuery(rs, query, con);
						if (rs.next()){
							if (rs.getFloat("costperclick")>0){
								query="select * from materializedadvice w  join wines on (w.id=wines.id) where w.shopid="+shopid+" and w.lft="+lft+" and w.rgt="+rgt+" and w.id!="+wineid+" order by pqratio desc limit 3;";
								header="Other suggestions";
								js=true;
							} else {

								if (targetcountrycode==null) targetcountrycode=rs.getString("countrycode");
								//																																						added temporarily! for test in NL
								query="select * from materializedadvice w join (select @rownum:=@rownum+1 as rank,id from shops,(SELECT @rownum:=0) r where costperclick>0 order by (countrycode='"+targetcountrycode+"') desc,countrycode in ('"+Webroutines.getCountries(Webroutines.getRegion(targetcountrycode))+"') desc,costperclick desc) sel on (w.shopid=sel.id)  join wines on (w.id=wines.id) where w.lft="+lft+" and w.rgt="+rgt+" order by (if (w.knownwineid="+knownwineid+" and w.vintage="+vintage+",w.priceeuroex,99999)) , w.knownwineid="+knownwineid+" desc, rank,pqratio desc limit 3;";
								header="Recommended wines from our partners";
							}

						}
					} else {//                                                                                                                                     added temporarily! for test in NL
						query="select * from materializedadvice w join (select @rownum:=@rownum+1 as rank,id from shops,(SELECT @rownum:=0) r where costperclick>0 and countrycode='"+targetcountrycode+"' order by (countrycode='"+targetcountrycode+"') desc,countrycode in ('"+Webroutines.getCountries(Webroutines.getRegion(targetcountrycode))+"') desc,costperclick desc) sel on (w.shopid=sel.id)  join wines on (w.id=wines.id) where w.lft="+lft+" and w.rgt="+rgt+" order by rank,pqratio desc limit 3;";
						header="Recommended wines from our partners";
					}
					rs = Dbutil.selectQueryFromMemory(query, "materializedadvice", con);
					if (rs.isBeforeFirst()){ // Commission shops
						NumberFormat nf=new DecimalFormat("#.##");
						sb.append("<div class='recommendationad'>");
						sb.append("<font style='font-face:Arial,sans-serif;font-size: 11px;'><a href='/advertising.jsp' rel='nofollow'><br/>Ads by Vinopedia</a><br/></font>\n<h3>"+header+"</h3>");
						int adnumber=0;
						String spancontent="";
						while (rs.next()) {
							adnumber++;
							Wine w=new Wine(rs.getInt("id"));
							spancontent="<span class='jslink' onclick=\"vpclick('/'+['adhandler.jsp','type="+type+"&amp;wineid="+w.Id+"'].join('?'))\">";
							sb.append("<div class='clear'></div><div class='spacer'></div>");
							sb.append("<div class='seller'><img class='sprite flag sprite-"+w.Country.toLowerCase()+"' alt='country' src='"+Configuration.cdnprefix+"/images/transparent.gif'/>"+w.Shopname.replaceAll("&", "&amp;")+"</div>");
							sb.append("<div class='adlabel' id='ad"+adnumber+"'>"+spancontent+(Knownwines.getKnownWineName(w.Knownwineid)).trim().replaceAll("&", "&amp;")+"<br/><span class='vintage'>"+(!w.Vintage.equals("0")?w.Vintage:"")+"</span></span>"+
							"</div>");
							sb.append("<div class='sizecont'><div class='price'>"+Webroutines.formatPrice(rs.getDouble("priceeuroex"), rs.getDouble("priceeuroex"), p.searchdata.getCurrency(), "EX")+"</div><div class='size'>Cont.: "+Webroutines.formatSizecompact(w.Size).replace(",",".")+"</div></div>");
							if (js){
								sb.append("<div class='buy' onclick='javascript:showWine("+w.Id+",&quot;ad"+adnumber+"&quot;);'>Info</div>");
							} else {
								sb.append("<div class='buy'>"+spancontent+"Info</span></div>");
							}
							sb.append("<div class='ras'>");
							if (w.Ratings!=null) for (Winerating r:w.Ratings){
								sb.append("<div class='au'>"+r.author.replaceAll("&", "&amp;")+"</div><div class='po'>"+nf.format(r.ratinglow)+(r.ratinghigh>0?"-"+nf.format(r.ratinghigh):"")+"</div>");
							}
							sb.append("</div>");

							//Dbutil.executeQuery("update wineads set views=views+1 where wineid="+rs.getInt("id"), con);
						}
						sb.append("<div class='adpricenote'>Note: Ad prices are an indication only and will be finalized when ordering.</div>");
						sb.append("</div>");
					}
				}

			} catch (Exception e) {
				Dbutil.logger.error("", e);
			} finally {
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}
		return sb.toString();

	}

	public int getShopid() {
		return shopid;
	}

	public void setShopid(int shopid) {
		this.shopid = shopid;
	}

	public static String getUrl(int wineid, String ip, String hostname,Searchdata searchdata,String hostcountry,String region, String knownwineidstr, String adtype){
		String url="/index.jsp";
		if (wineid>0){
			ResultSet rs=null;
			String winename="";
			if (region==null||region.equals(null)) region="";
			Connection con=Dbutil.openNewConnection();
			if (winename.equals("")) winename=searchdata.getName();
			try{
				String query="SELECT * FROM wines join shops on (wines.shopid=shops.id) where wines.id="+wineid+";";
				rs=Dbutil.selectQuery(query,con);
				if(rs.next()){
					url="/store/"+rs.getString("Shopname")+"/?wineid="+wineid;
					int knownwineid=rs.getInt("knownwineid");
					if (knownwineid>0){
						try {
							winename=Dbutil.readValueFromDB("Select * from knownwines where id="+knownwineid+";", "wine");
						} catch (Exception exc){}
					}

					if ((!ip.contains("192.168.1.")&&!ip.contains("127.0.0.1"))||Wijnzoeker.serverrole.equals("DEV")){
						Webactionlogger logger=new Webactionlogger(url, region, winename, wineid, (double)0, adtype, ip, winename, searchdata.getVintage(), searchdata.getCountry(), rs.getString("shopid"));
						logger.knownwineid=knownwineid;
						
						logger.logaction();

					}
					url="/store/"+rs.getString("Shopname")+"/?wineid="+wineid;

				}
			} catch (Exception exc){
				Dbutil.logger.error("Could not retrieve ad link. ",exc);
			}
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return url;

	}



}

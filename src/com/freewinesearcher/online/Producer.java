package com.freewinesearcher.online;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwines;
import com.freewinesearcher.common.POI;

public class Producer implements POI{

	public int id;
	public String name=new String();
	public String nameinsource="";
	public String address=new String();
	public String email=new String();
	public String website=new String();
	public String visiting=new String();
	public String telephone=new String();
	public String description=new String();
	public String twitter="";
	public Double lat;
	public Double lon;
	public Integer accuracy=0;
	public boolean hasvalidlocation=false;
	public boolean showallinfo=false;
	public String generateddescription="";
	public String keywords="";
	public String edithashcode="";
	public String mapjs="";
	public boolean mobile=false;
	public boolean newmobile=false;



	public Producer(String producername){
		id=Dbutil.readIntValueFromDB("select * from kbproducers where name='"+Spider.SQLEscape(producername)+"';", "id");
		getData();
	}


	public Producer(int producerid){
		this.id=producerid;
		getData();
	}

	public static Producer getByKnownwineid(int knownwineid){
		return new Producer(Dbutil.readIntValueFromDB("select kbproducers.id from knownwines join kbproducers on (producer=kbproducers.name) where knownwines.id="+knownwineid, "id"));
	}
	
	private void getData(){


		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select kbproducers.*, producers.name as nameinsource from kbproducers left join producers on (kbproducers.sourceid=producers.id) where kbproducers.id="+id;
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				name=rs.getString("kbproducers.name");
				nameinsource=rs.getString("nameinsource");
				address=rs.getString("address");
				email=rs.getString("email");
				website=rs.getString("website");
				if (website!=null&&!website.toLowerCase().startsWith("http://")&&website.length()>4) website="http://"+website;
				visiting=rs.getString("visiting");
				telephone=rs.getString("telephone");
				if (telephone!=null) telephone=telephone.replaceAll("<br>", "<br/>");
				lat=rs.getDouble("lat");
				lon=rs.getDouble("lon");
				accuracy=rs.getInt("accuracy");
				if (lon!=null&&lat!=null&&lon!=0&&lat!=0) hasvalidlocation=true;
				description=rs.getString("description");
				twitter=rs.getString("twitter");
				if (twitter==null) twitter="";
				if (twitter.startsWith("@")) twitter=twitter.substring(1);
				edithashcode=rs.getString("edithashcode");
				generateddescription="Information about winery "+name+(accuracy>0?": Address, map":"")+" and an overview of their wines, prices and availability.";
			}

		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	

	public String getInfo(PageHandler p) {
		{
			//Double pricefactor=(double)1;
			//try{pricefactor=Double.valueOf(Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.getCurrency()+"';", "rate"));}catch(Exception e){}
			//if (pricefactor==(double)0) pricefactor=(double)1;
			//String symbol=Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.currency+"';", "symbol");
			ResultSet rs=null;
			String query;
			Connection con=Dbutil.openNewConnection();
			StringBuffer sb=new StringBuffer();
			boolean showcontact=false;
			String winelink="";
			if ((address!=null&&address.length()>0)||(telephone!=null&&telephone.length()>0)||(website!=null&&website.length()>0)||(visiting!=null&&visiting.length()>0)||(twitter!=null&&twitter.length()>0)||showallinfo)
				showcontact=true;

			try{
				sb.append("<h1 class='category'><img class='categoryicon' src='/images/chateau.gif' alt='winery'/><span class='categorylabel'>Winery: </span>"+name.replaceAll("&","&amp;")+"</h1>");
				if (mobile){
					sb.append("<div data-role='controlgroup' data-type='horizontal'>"+(description!=null||showallinfo?"<a data-role='button' href='#description'>Information</a>":"")+"<a href='#wines' data-role='button'>Wines</a>"+(showcontact||showallinfo?"<a data-role='button' href='#mappane'"+(mobile?"":" onclick='showwinerylocation()'")+">Contact</a>":"")+(!mobile?"<a data-role='button' href='#storelocator' onclick='showmap()'>Stores</a>":"")+"</div><div id='winerypane'>");
				} else {
					sb.append("<ul class='tabs' id='winerytabs'><li><a href='#wines'>Wines</a></li>"+(description!=null||showallinfo?"<li><a href='#description'>Information</a></li>":"")+(showcontact||showallinfo?"<li><a href='#mappane'"+(mobile?"":" onclick='showwinerylocation()'")+">Contact</a></li>":"")+(!mobile?"<li><a href='#storelocator' onclick='showmap()'>Stores</a></li>":"")+((website!=null&&website.length()>0&&!mobile)?"<li><a href='#websitepane'>Web site</a></li>":"")+"</ul><div id='winerypane'>");
				}
				if (mobile&&(description!=null||showallinfo)){
					sb.append("<div class='pane' id='description'><h2>Winery information</h2>"+(description!=null?description:"")+"</div>");

				}
				sb.append("<div class='pane' id='wines'><h2>The wines from "+name.replaceAll("&","&amp;")+":</h2>");
				if (!mobile) sb.append("<table class='winerywines'>");
				//query = "select *,avg(bestprices.priceeuroex) as avg,min(bestprices.priceeuroex) as min,std(bestprices.priceeuroex) as std, avg(rating) as rating from knownwines left join bestprices on (knownwines.id=bestprices.knownwineid and continent='AL') left join ratinganalysis ra on (knownwines.id=ra.knownwineid and author='FWS') where producer='"+Spider.SQLEscape(name)+"' "+(showallinfo?"":" and disabled=0")+" group by knownwines.id order by appellation, wine;";
				//query = "select *,count(*) as ratings, avg(rating) as avgrating  from knownwines left join ratinganalysis ra on (knownwines.id=ra.knownwineid and author='FWS') where producer='"+Spider.SQLEscape(name)+"' "+(showallinfo?"":" and disabled=0")+" group by knownwines.id order by appellation, wine;";
				query = "select sel.*,count(*) as offers,wines.knownwineid as valid from (select knownwines.*,ra.knownwineid,count(*) as ratings, avg(rating) as avgrating  from knownwines left join ratedwines ra on (knownwines.id=ra.knownwineid and rating>70) where producer='"+Spider.SQLEscape(name)+"' "+(showallinfo?"":" and disabled=0")+" group by knownwines.id order by appellation, wine) sel left join wines on (sel.id=wines.knownwineid) group by sel.id order by appellation;";
				rs = Dbutil.selectQuery(rs, query, con);
				String lastregion="";
				int avg;
				int stars;
				keywords=(name+(website!=null?", "+website:""));
				while (rs.next()) {
					if (!rs.getString("appellation").equals(lastregion)){
						lastregion=rs.getString("appellation");
						keywords+=", "+lastregion;
						sb.append((mobile?"":"<tr><td colspan='2'>")+"<h2><a href='/"+(mobile?"m":"")+"region/"+Regioninfo.locale2href(rs.getString("locale"))+"/'>"+lastregion+"</a></h2>"+(mobile?"":"</td></tr>"));
					}
					winelink=Webroutines.winelink(Knownwines.getUniqueKnownWineName(rs.getInt("id")),0,mobile);
					//avg=rs.getInt("avg");
					stars=(int) Math.round(((rs.getDouble("avgrating")-87)*5)/6);
					if (stars>5) stars=5;
					// See if there is a label
					String labeldiv="";
					if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".jpg").exists()){
						labeldiv="<div class='winerylabel'><a rel='external' href='"+winelink+"'><img class='label' src='"+Configuration.staticprefix+"/labels/"+rs.getString("id")+".jpg' alt=\""+rs.getString("wine").replaceAll("&","&apos;")+"\" onmouseover='this.parentNode.className=\"winerylabelmouseover\";' onmouseout='this.parentNode.className=\"winerylabel\";'/></a></div>";
					} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".gif").exists()){
						labeldiv="<div class='winerylabel'><a rel='external' href='"+winelink+"'><img class='label' src='"+Configuration.staticprefix+"/labels/"+rs.getString("id")+".gif' alt='"+rs.getString("wine").replaceAll("&","&apos;")+"' onmouseover='this.parentNode.className=\"winerylabelmouseover\";' onmouseout='this.parentNode.className=\"winerylabel\";'/></a></div>";

					}
					if (labeldiv.length()>0){
						sb.append(mobile?labeldiv:"<tr><td></td><td class='label'>"+labeldiv+"</td></tr>");
					}
					sb.append((mobile?"":"<tr><td>")+"<h3><a rel='external' href='"+winelink+"'>"+rs.getString("wine").replaceAll("&", "&amp;")+" "+(rs.getBoolean("disabled")?"<i>(This wine is disabled in our system) ":" ")+"</a> ");
					for (int i=0;i<stars;i++) sb.append("<img src='/css/"+(newmobile?"b":"")+"star.gif' title='Average rating: "+rs.getInt("avgrating")+"/100' alt='Average rating: "+rs.getInt("avgrating")+"/100'/>");
					sb.append("</h3>"+(mobile?"":"</td><td></td></tr><tr><td colspan='2'>"));
					sb.append("A "+(rs.getString("type").replaceAll(" - ", " ").toLowerCase()+" wine").replaceAll("spirits wine", "spirit").replaceAll("liqueur wine", "liqueur")+" made from "+(rs.getString("grapes").toLowerCase().contains("blend")?"a ":"")+rs.getString("grapes")+(mobile?"<br/>":"</td></tr>"));
					if (rs.getString("winerynote")!=null&&rs.getString("winerynote").length()>0) sb.append(mobile?rs.getString("winerynote").replaceAll("\r","<br/>"):"<tr><td class='winerynote' colspan='2'>"+rs.getString("winerynote").replaceAll("\r","<br/>")+"</td></tr>");
					//int n=Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="+rs.getString("id")+" group by knownwineid", "thecount");
					if (rs.getInt("avgrating")>70||rs.getInt("valid")>0) sb.append(
							(mobile?"":"<tr><td>")+
							"<span class='hreview-aggregate'>For all vintages of <span class='item'><span class='fn'>"+rs.getString("wine").replaceAll("&", "&amp;")+"</span></span> we have "+
							(rs.getInt("valid")>0?
									rs.getInt("offers")+" offers"+(
											rs.getInt("avgrating")>70?
													" and "
													:" on record."
									)
									:"no prices but ")
									+(rs.getInt("avgrating")>70?
													"<span class='count'>"+rs.getInt("ratings")+"</span> rating"+(
															rs.getInt("ratings")>1?
																	"s"
																	:""
													)+" on record with an average of <span class='rating'><span class='average'>"+rs.getInt("avgrating")+"</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span>":"")
													+"</span>"+(
															mobile?
																	""
																	:"</td><td></td></tr>")
													
					);
					//sb.append("</td><td>"+(avg>0?""+symbol+(Math.round(rs.getDouble("min")/pricefactor))+" - "+symbol+(Math.round((rs.getDouble("avg")+rs.getDouble("std"))/pricefactor)):"n.a.")+"</td></tr>");
					if (showallinfo) sb.append("<tr><td colspan='2'><a href='/edit/wineeditor.jsp?action=get&amp;knownwineid="+rs.getInt("id")+"&amp;code="+edithashcode+"'><b>Edit this wine</b></a></td></tr>");
					if (!mobile) sb.append("<tr><td colspan='2'></td></tr>");

				}
				if (!mobile)sb.append("</table>");
				if (showallinfo) sb.append("<a href='/edit/wineeditor.jsp?producerid="+id+"&amp;code="+edithashcode+"' target='_blank'><b>Add a new wine</b></a>");
				sb.append("</div>");
				if (!mobile&&(description!=null||showallinfo)){
					sb.append("<div class='pane' id='description'>"+(description!=null?description:"")+"</div>");

				}
				if (showallinfo||showcontact){
					//map
					if (mobile) sb.append("<h2>Contact information</h2>");
					sb.append("<div class='pane' id='mappane'><table>");
					if ((address!=null&&address.length()>0)||showallinfo) sb.append("<tr><td >Address</td><td id='address'>"+(showallinfo?(address!=null?address:""):"<img src='/text2gif/?address="+id+(newmobile?"&amp;mobile=true":"")+"' alt='address'/>")+"</td></tr>");
					if ((telephone!=null&&telephone.length()>0)||showallinfo) sb.append("<tr><td>Phone number</td><td id='telephone'>"+(telephone!=null?telephone:"")+"</td></tr>");
					if (showallinfo) sb.append("<tr><td>Email</td><td id='email'>"+(email!=null?email:"")+"</td></tr>");
					if ((website!=null&&website.length()>0)||showallinfo)sb.append("<tr><td >Web site</td><td id='website'><a rel='external' href='/external.jsp?exturl="+Webroutines.URLEncode((website!=null?website:"")).replaceAll("'","&apos;")+"'>"+(website!=null?website:"")+"</a></td></tr>");
					if ((visiting!=null&&visiting.length()>0)||showallinfo)sb.append("<tr><td >Visitor information</td><td id='visiting'>"+visiting+"</td></tr>");
					if ((twitter!=null&&twitter.length()>0)||showallinfo)sb.append("<tr><td >Twitter account</td><td id='twitter'><a href='"+(twitter.startsWith("http")?twitter:"http://twitter.com/#!/"+twitter)+"' target='_blank'>"+twitter+"</a></td></tr>");

					sb.append("</table>");
					if (accuracy>0&&!mobile){
						//sb.append("<div id='mapbig' class='map' style='position:relative;width: 750px; height: 500px;'><div id='mapdetail' class='map' style='position:absolute;width: 750px; height: 500px;'></div><div id='mapstreetview' class='map' style='display:none;position:absolute;width: 750px; height: 500px;'><div id='imStreetViewClose' onclick='imStreetView.toggleStreetView(false);'>Close Streetview</div> </div></div>");
						sb.append("<div id='mapdetail' class='map' style='width: 750px; height: 500px;'></div>");
						sb.append("<div id='mapworld' class='map' style='width: 248px; height: 250px;'></div>");
						sb.append("<div id='mapregion' class='map' style='width: 248px; height: 250px;'></div>");

					}
					sb.append("</div>");
				}
				if (!mobile){
					sb.append("<div class='pane' id='storelocator'></div>");
					if ((website!=null&&website.length()>0)) sb.append("<div class='pane' id='websitepane'><iframe style='width:950px;height:500px;' src='"+(!website.startsWith("http")?"http://":"")+(website)+"'></iframe></div>");

				}
				sb.append("</div>");



			} catch (Exception e){
				String name = e.getClass().getName();
				if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
					// No action
				} else {
					Dbutil.logger.error("Error while retrieving producer information. ",e);
				} 

			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);	
			}
			return sb.toString();
		}
	}


	public String getVisitingReport(PageHandler p, boolean map) {
		{
			Double pricefactor=Double.valueOf(Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.currency+"';", "rate"));
			if (pricefactor==(double)0) pricefactor=(double)1;
			String symbol=Dbutil.readValueFromDB("Select * from currency where currency='"+p.searchdata.currency+"';", "symbol");
			ResultSet rs=null;
			String query;
			Connection con=Dbutil.openNewConnection();
			StringBuffer sb=new StringBuffer();
			boolean showcontact=false;
			if ((address!=null&&address.length()>0)||(telephone!=null&&telephone.length()>0)||(website!=null&&website.length()>0)||(visiting!=null&&visiting.length()>0)||(twitter!=null&&twitter.length()>0)||showallinfo)
				showcontact=true;

			try{
				sb.append("<div><h1 style='page-break-before: always;'><img class='categoryicon' src='/images/chateau.gif' alt='winery'/><span class='categorylabel'>Winery: </span>"+name+"</h1></div>");
				if (showallinfo||showcontact){
					//map
					sb.append("<table>");
					if ((address!=null&&address.length()>0)||showallinfo) sb.append("<tr><td >Address</td><td id='address'>"+(showallinfo?(address!=null?address:""):"<img src='/text2gif/?address="+id+"' alt='address'/>")+"</td></tr>");
					if ((telephone!=null&&telephone.length()>0)||showallinfo) sb.append("<tr><td>Phone number</td><td id='telephone'>"+(telephone!=null?telephone:"")+"</td></tr>");
					if (showallinfo) sb.append("<tr><td>Email</td><td id='email'>"+(email!=null?email:"")+"</td></tr>");
					if ((website!=null&&website.length()>0)||showallinfo)sb.append("<tr><td >Web site</td><td id='website'><a href='/external.jsp?exturl="+Webroutines.URLEncode((website!=null?website:"")).replaceAll("'","&apos;")+"'>"+(website!=null?website:"")+"</a></td></tr>");
					if ((visiting!=null&&visiting.length()>0)||showallinfo)sb.append("<tr><td >Visitor information</td><td id='visiting'>"+visiting+"</td></tr>");
					if ((twitter!=null&&twitter.length()>0)||showallinfo)sb.append("<tr><td >Twitter account</td><td id='twitter'><a href='"+(twitter.startsWith("http")?twitter:"http://twitter.com/#!/"+twitter)+"' target='_blank'>"+twitter+"</a></td></tr>");
					sb.append("</table>");
					if (map){
						if (accuracy>0){
							mapjs+="var map1"+id+" = new GMap2(document.getElementById('map1"+id+"'));map1"+id+".setCenter(new GLatLng("+lat+","+lon+"), 15);";
							sb.append("<div class='map' id='map1"+id+"' style='width: 400px; height: 300px;float:left;margin:10px;'></div>");
							mapjs+="var map2"+id+" = new GMap2(document.getElementById('map2"+id+"'));map2"+id+".setCenter(new GLatLng("+lat+","+lon+"), 12);";
							sb.append("<div class='map' id='map2"+id+"' style='width: 400px; height: 300px;float:left;margin:10px;'></div>");
						}
					}
				}
				sb.append("<div class='clear'><h2>The wines from "+name+":</h2>");
				sb.append("<table class='winerywines'>");
				//query = "select *,avg(bestprices.priceeuroex) as avg,min(bestprices.priceeuroex) as min,std(bestprices.priceeuroex) as std, avg(rating) as rating from knownwines left join bestprices on (knownwines.id=bestprices.knownwineid and continent='AL') left join ratinganalysis ra on (knownwines.id=ra.knownwineid and author='FWS') where producer='"+Spider.SQLEscape(name)+"' "+(showallinfo?"":" and disabled=0")+" group by knownwines.id order by appellation, wine;";
				//query = "select *,count(*) as ratings, avg(rating) as avgrating  from knownwines left join ratinganalysis ra on (knownwines.id=ra.knownwineid and author='FWS') where producer='"+Spider.SQLEscape(name)+"' "+(showallinfo?"":" and disabled=0")+" group by knownwines.id order by appellation, wine;";
				query = "select sel.*,count(*) as offers,wines.knownwineid as valid from (select knownwines.*,ra.knownwineid,count(*) as ratings, avg(rating) as avgrating  from knownwines left join ratedwines ra on (knownwines.id=ra.knownwineid and rating>70) where producer='"+Spider.SQLEscape(name)+"' "+(showallinfo?"":" and disabled=0")+" group by knownwines.id order by appellation, wine) sel left join wines on (sel.id=wines.knownwineid) group by sel.id order by appellation;";
				rs = Dbutil.selectQuery(rs, query, con);
				String lastregion="";
				int avg;
				int stars;
				keywords=(name+(website!=null?", "+website:""));
				while (rs.next()) {
					if (!rs.getString("appellation").equals(lastregion)){
						lastregion=rs.getString("appellation");
						keywords+=", "+lastregion;
						sb.append("<tr><td colspan='2'><h2><a href='/region/"+Regioninfo.locale2href(rs.getString("locale"))+"/'>"+lastregion+"</a></h2></td></tr>");
					}
					//avg=rs.getInt("avg");
					stars=(int) Math.round(((rs.getDouble("avgrating")-87)*5)/6);
					if (stars>5) stars=5;
					// See if there is a label
					String labeldiv="";
					if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".jpg").exists()){
						labeldiv="<div class='winerylabel'><img class='label' src='"+Configuration.staticprefix+"/labels/"+rs.getString("id")+".jpg' alt=\""+rs.getString("wine").replaceAll("&","&apos;")+"\" onmouseover='this.parentNode.className=\"winerylabelmouseover\";' onmouseout='this.parentNode.className=\"winerylabel\";'/></div>";
					} else if (new File(Configuration.workspacedir+"labels"+System.getProperty("file.separator")+rs.getString("id")+".gif").exists()){
						labeldiv="<div class='winerylabel'><img class='label' src='"+Configuration.staticprefix+"/labels/"+rs.getString("id")+".gif' alt='"+rs.getString("wine").replaceAll("&","&apos;")+"' onmouseover='this.parentNode.className=\"winerylabelmouseover\";' onmouseout='this.parentNode.className=\"winerylabel\";'/></div>";

					}
					if (labeldiv.length()>0){
						sb.append("<tr><td></td><td class='label'>"+labeldiv+"</td></tr>");
					}
					sb.append("<tr><td><h3><i><a href='"+Webroutines.winelink(Knownwines.getUniqueKnownWineName(rs.getInt("id")),0)+"'>"+rs.getString("wine").replaceAll("&", "&amp;")+" "+(rs.getBoolean("disabled")?"<i>(This wine is disabled in our system)</i> ":" ")+"</a> ");
					for (int i=0;i<stars;i++) sb.append("<img src='/css/star.gif' title='Average rating: "+rs.getInt("avgrating")+"/100' alt='Average rating: "+rs.getInt("avgrating")+"/100'/>");
					sb.append("</i></h3></td><td></td></tr>");
					sb.append("<tr><td colspan='2'>A "+(rs.getString("type").replaceAll(" - ", " ").toLowerCase()+" wine").replaceAll("spirits wine", "spirit").replaceAll("liqueur wine", "liqueur")+" made from "+(rs.getString("grapes").toLowerCase().contains("blend")?"a ":"")+rs.getString("grapes")+"</td></tr>");
					if (rs.getString("winerynote")!=null&&rs.getString("winerynote").length()>0) sb.append("<tr><td class='winerynote' colspan='2'>"+rs.getString("winerynote").replaceAll("\r","<br/>")+"</td></tr>");
					//int n=Dbutil.readIntValueFromDB("select count(*) as thecount from ratedwines where knownwineid="+rs.getString("id")+" group by knownwineid", "thecount");
					if (rs.getInt("avgrating")>70||rs.getInt("valid")>0) sb.append("<tr><td><span class='hreview-aggregate'>For all vintages of <span class='item'><span class='fn'>"+rs.getString("wine").replaceAll("&", "&amp;")+"</span></span> we have "+(rs.getInt("valid")>0?rs.getInt("offers")+" offers"+(rs.getInt("avgrating")>70?" and ":" on record."):"no price but ")+(rs.getInt("avgrating")>70?"<span class='count'>"+rs.getInt("ratings")+"</span> rating"+(rs.getInt("ratings")>1?"s":"")+" on record with an average of <span class='rating'><span class='average'>"+rs.getInt("avgrating")+"</span>/100 points.<span class='best'><span class='value-title' title='100'> </span></span><span class='worst'><span class='value-title'  title='50'> </span></span></span></span></td><td></td></tr>":""));
					//sb.append("</td><td>"+(avg>0?""+symbol+(Math.round(rs.getDouble("min")/pricefactor))+" - "+symbol+(Math.round((rs.getDouble("avg")+rs.getDouble("std"))/pricefactor)):"n.a.")+"</td></tr>");
					if (showallinfo) sb.append("<tr><td colspan='2'><a href='/edit/wineeditor.jsp?action=get&amp;knownwineid="+rs.getInt("id")+"&amp;code="+edithashcode+"'><b>Edit this wine</b></a></td></tr>");
					sb.append("<tr><td colspan='2'></td></tr>");

				}
				sb.append("</table>");
				if (showallinfo) sb.append("<a href='/edit/wineeditor.jsp?producerid="+id+"&amp;code="+edithashcode+"' target='_blank'><b>Add a new wine</b></a>");
				sb.append("</div>");
				if (description!=null||showallinfo){
					sb.append("<div class='pane' id='description'>"+(description!=null?description:"")+"</div>");

				}



			} catch (Exception e){
				String name = e.getClass().getName();
				if  (name.equals("org.apache.catalina.connector.ClientAbortException")) {
					// No action
				} else {
					Dbutil.logger.error("Error while retrieving producer information. ",e);
				} 

			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);	
			}
			return sb.toString();
		}
	}


	public String getMapHtml(){
		StringBuffer sb=new StringBuffer();
		sb.append("<table>");
		if ((address!=null&&address.length()>0)||showallinfo) sb.append("<tr><td width='100px'>Address</td><td width='400px;' id='address'>"+(showallinfo?(address!=null?address:""):"<img src='/text2gif/?address="+id+"' alt='Loading address...'/>")+"</td></tr>");
		if ((telephone!=null&&telephone.length()>0)||showallinfo) sb.append("<tr><td>Phone number</td><td id='telephone'>"+(telephone!=null?telephone:"")+"</td></tr>");
		if (showallinfo) sb.append("<tr><td>Email</td><td id='email'>"+(email!=null?email:"")+"</td></tr>");
		if ((website!=null&&website.length()>0)||showallinfo)sb.append("<tr><td >Web site</td><td id='website'><a href='/external.jsp?exturl="+Webroutines.URLEncode((website!=null?website:"")).replaceAll("'","&apos;")+"' target='_blank'>"+(website!=null?website:"")+"</a></td></tr>");
		if ((visiting!=null&&visiting.length()>0)||showallinfo)sb.append("<tr><td >Visitor information</td><td id='visiting'>"+visiting+"</td></tr>");
		if ((twitter!=null&&twitter.length()>0)||showallinfo)sb.append("<tr><td >Twitter account</td><td id='twitter'><a href='"+(twitter.startsWith("http")?twitter:"http://twitter.com/#!/"+twitter)+"' target='_blank'>"+twitter+"</a></td></tr>");
		sb.append("</table>");
		return sb.toString();
	}

	@Override
	public String getHTML() {
		return "<div class='infowindow'><h3><a href='/"+(mobile?"m":"")+"winery/"+Webroutines.URLEncode(Webroutines.removeAccents(name)).replace("&", "&amp;")+"'>"+name+"</a></h3><img src='/text2gif/?address="+id+"' alt='address'/></div>";
		//return "<div id='content'><div id='siteNotice'></div><h1 ><a href='/winery/"+Webroutines.URLEncode(Webroutines.removeAccents(name)).replace("&", "&amp;")+"'>"+name+"</a></h1><div ><p><b>Uluru</b>, also referred to as <b>Ayers Rock</b>, is a large sandstone rock formation in the southern part of the Northern Territory, central Australia. It lies 335&nbsp;km (208&nbsp;mi) south west of the nearest large town, Alice Springs; 450&nbsp;km (280&nbsp;mi) by road. Kata Tjuta and Uluru are the two major features of the Uluru - Kata Tjuta National Park. Uluru is sacred to the Pitjantjatjara and Yankunytjatjara, the Aboriginal people of the area. It has many springs, waterholes, rock caves and ancient paintings. Uluru is listed as a World Heritage Site.</p><p>Attribution: Uluru, <a href='http://en.wikipedia.org/w/index.php?title=Uluru&amp;oldid=297882194'>http://en.wikipedia.org/w/index.php?title=Uluru</a> (last visited June 22, 2009).</p></div></div>";
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
		return "";
	}


	public Integer getAccuracy() {
		return accuracy;
	}


	public void setAccuracy(Integer accuracy) {
		this.accuracy = accuracy;
	}


	@Override
	public int getId() {
		return id;
	}





}

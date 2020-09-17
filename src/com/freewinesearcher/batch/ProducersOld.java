package com.freewinesearcher.batch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;


import com.freewinesearcher.common.Dbutil;
/**
 * @author Jasper
 *
 */
public class ProducersOld {
	public ArrayList<String> name=new ArrayList<String>();
	public ArrayList<String> address=new ArrayList<String>();
	public ArrayList<String> visiting=new ArrayList<String>();
	public ArrayList<String> telephone=new ArrayList<String>();
	public ArrayList<Double> lat=new ArrayList<Double>();
	public ArrayList<Double> lon=new ArrayList<Double>();
	public ArrayList<Integer> accuracy=new ArrayList<Integer>();
	public ArrayList<ArrayList<String>> wines=new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<String>> ratings=new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<Double>> relativeratings=new ArrayList<ArrayList<Double>>();
	public ArrayList<ArrayList<Double>> prices=new ArrayList<ArrayList<Double>>();
	public double avgrelprice=0;
	public int numberofproducers=0;
	public int maxproducers=200;


	public ProducersOld(Double lat1,Double lon1,Double lat2,Double lon2, boolean onlyratedwines, int fromvintage){
		String query="";
		String ratedwinesclause="";
		ResultSet rs=null;
		ResultSet rs2=null;
		ResultSet rs3=null;
		Connection con=Dbutil.openNewConnection();
		int n=0;
		double avgrelprice=0;
		HashSet<Integer> prod=new HashSet<Integer>();
		try{
			if (onlyratedwines){
				query="select ph.* from kbproducers ph join knownwines kw on (ph.name=kw.producer) join ratedwines on (kw.id=ratedwines.knownwineid) where lat<"+lat2+" and lat>"+lat1+" and lon<"+lon2+" and lon>"+lon1+" and vintage>"+fromvintage+" group by ph.id order by lat desc;";
			} else {
				query="select * from kbproducers where lat<"+lat2+" and lat>"+lat1+" and lon<"+lon2+" and lon>"+lon1+" order by lat desc;";
			}
			rs=Dbutil.selectQuery(query, con);
			if (rs.last()){
				numberofproducers=rs.getRow()+1;
				rs.beforeFirst();
			}
			if (numberofproducers<maxproducers+1){
				while (rs.next()){
					/*
					prod.add(rs.getInt("id"));
					rs3=Dbutil.selectQuery("select q.producerid from producerknownwines p join producerknownwines q on (p.knownwineid=q.knownwineid and p.producerid!=q.producerid) where p.producerid="+rs.getInt("id")+" group by q.producerid ;", con);
					while (rs3.next()){
						
					}*/
					name.add(rs.getString("name"));
					address.add(rs.getString("address"));
					visiting.add(rs.getString("visiting"));
					telephone.add(rs.getString("telephone"));
					lat.add(rs.getDouble("lat"));
					lon.add(rs.getDouble("lon"));
					accuracy.add(rs.getInt("accuracy"));
					ArrayList<String> winelist=new ArrayList<String>();
					ArrayList<String> ratinglist=new ArrayList<String>();
					ArrayList<Double> pricelist=new ArrayList<Double>();
					ArrayList<Double> relativeratinglist=new ArrayList<Double>();
					if (onlyratedwines){
						query="select knownwines.id as knownwineid,ratedwines.vintage,group_concat(concat(author,':',ratedwines.rating, case when ratinghigh=0 then '' else concat('-',ratinghigh) end)) as rating, knownwines.*,(select min(priceeuroin) as priceeuroin from wines where (knownwines.id=wines.knownwineid and ratedwines.vintage=wines.vintage and size=0.75)) as priceeuroin  from ratedwines join knownwines on (ratedwines.knownwineid=knownwines.id) where knownwines.producer='"+Spider.SQLEscape(rs.getString("name"))+"' and ratedwines.vintage>"+fromvintage+" group by knownwines.id, vintage order  by knownwines.id, vintage;";
						rs2=Dbutil.selectQueryFromMemory(query,"materializedadvice", con);
						while (rs2.next()){
							winelist.add(rs2.getString("wine")+" "+rs2.getString("vintage"));
							ratinglist.add(rs2.getString("rating"));
							pricelist.add (rs2.getDouble("priceeuroin"));
							if (rs2.getDouble("priceeuroin")>0){
								query="SELECT "+rs2.getDouble("priceeuroin")+"/price as relprice FROM ratinganalysis r join pqratio on (pqratio.rating=r.rating) where knownwineid="+rs2.getString("knownwineid")+" and vintage="+rs2.getString("vintage")+" and author='FWS';";
								rs3=Dbutil.selectQuery(query, con);
								if (rs3.next()){
									relativeratinglist.add (rs3.getDouble("relprice"));
									if (rs3.getDouble("relprice")>0){
										avgrelprice+=rs3.getDouble("relprice");
										n++;
									}
								} else {
									relativeratinglist.add ((double)0);
								}
								Dbutil.closeRs(rs3);

							}else {
								relativeratinglist.add ((double)0);

							}


						}
						Dbutil.closeRs(rs2);
					}
					wines.add(winelist);
					ratings.add(ratinglist);
					prices.add(pricelist);
					relativeratings.add(relativeratinglist);

				}
				if (n>0) this.avgrelprice=avgrelprice/n;
			}
		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up producers",exc);
		}
		Dbutil.closeRs(rs);
		Dbutil.closeRs(rs2);
		Dbutil.closeRs(rs3);
		Dbutil.closeConnection(con);


	}



	/**
	 * @deprecated
	 */
	public static void ObsoletematchKnownwinesproducers(){
		Dbutil.logger.info("Starting job to get best matches for producers");
		ResultSet rs=null;
		ResultSet rs2=null;
		String query;
		Connection con=Dbutil.openNewConnection();
		String wordstart="(^|�|[^[:alnum:]])";
		String wordend="($|[^[:alnum:]]|�)";
		String[] countries={"France","Italy","Germany"};
		String[] countrycodes={"FR","IT","DE"};
		//String[] tables={"producershachette","producersitalianwinehub"};


		try{
			Dbutil.executeQuery("delete from producerknownwines;");
			for (int i=0;i<countries.length;i++){
				int lft=Dbutil.readIntValueFromDB("select * from regions where region='"+countries[i]+"';", "lft");
				int rgt=Dbutil.readIntValueFromDB("select * from regions where region='"+countries[i]+"';", "rgt");
				query="select * from knownwines join regions on knownwines.appellation=regions.region where producer!='' and lft>="+lft+" and rgt<="+rgt+";";
				rs=Dbutil.selectQuery(query, con);
				while (rs.next()){
					String fulltext="";
					String literal="";
					String literalproducer="";
					String country=" and countrycode='"+countrycodes[i]+"' ";
					String[] fulltextarray=rs.getString("producer").split("[ '.-]");
					for (String term:fulltextarray){
						term=term.replace("&", "");
						term=term.replace("+", "");
						if (!term.equals("")&&!term.equalsIgnoreCase("le")&&!term.equalsIgnoreCase("Domaine")&&!term.equalsIgnoreCase("Ch�teau")&&!term.equalsIgnoreCase("maison")&&!term.equalsIgnoreCase("P�re")&&!term.equalsIgnoreCase("et")&&!term.equalsIgnoreCase("Fils")&&term.length()>1) fulltext+=" +"+term;
						if (term.length()==1) literal+=" and fulltextsearch regexp '"+wordstart+term+"' ";
					}
					if (!fulltext.equals("")) {
						literalproducer=" and fulltextsearch regexp '.*"+Spider.SQLEscape(rs.getString("producer").replace("&", "(&|et)").replace("-", " ").replace("EARL", "").replace(" P�re et Fils", "").replace(" et Fils", "").replace("(", "").replace(")", "").replace("+", "").replaceAll(" +", "[^[:alnum:]]*"))+".*'";
						query="select id, match(fulltextsearch) against ('"+fulltext+"') as score from producers where match(fulltextsearch) against ('"+fulltext+"' in boolean mode) "+literal+literalproducer+country+" order by score desc limit 3;";
						if (rs.getDouble("lon")!=0) query="select id, (abs(ph.lon-"+rs.getDouble("lon")+")+abs(ph.lat-"+rs.getDouble("lat")+")) as distance from producers ph where match(fulltextsearch) against ('"+fulltext+"' in boolean mode) "+literal+literalproducer+country+" order by distance limit 1;";
						Dbutil.closeRs(rs2);
						rs2=Dbutil.selectQuery(query, con);
						if (rs2.first()){
							rs2.beforeFirst();
						} else {
							query="select id, match(fulltextsearch) against ('"+fulltext+"') as score from producers where match(fulltextsearch) against ('"+fulltext+"' in boolean mode) "+literal+country+" order by score desc limit 3;";
							if (rs.getDouble("lon")!=0) query="select id, (abs(ph.lon-"+rs.getDouble("lon")+")+abs(ph.lat-"+rs.getDouble("lat")+")) as distance from producers ph where match(fulltextsearch) against ('"+fulltext+"' in boolean mode) "+literal+country+" order by distance limit 1;";
							Dbutil.closeRs(rs2);
							rs2=Dbutil.selectQuery(query, con);
						}
						String producers="";
						while (rs2.next()){
							Dbutil.executeQuery("insert into producerknownwines (knownwineid,producerid) values ("+rs.getString("id")+","+rs2.getString("id")+");" , con);
							producers+=","+rs2.getString("id");
						}
						if (producers.length()>1) {
							producers=producers.substring(1);
						}
						if (producers.length()<30) {
							Dbutil.executeQuery("update knownwines set producerids='"+producers+"' where id="+rs.getString("id"));
						} else {
							Dbutil.executeQuery("update knownwines set producerids='' where id="+rs.getString("id"));
						}
					}

				}
			}


		} catch (Exception exc){
			Dbutil.logger.error("Problem while looking up producers",exc);
		}


		Dbutil.closeConnection(con);
		Dbutil.logger.info("Finished job to get best matches for producers");


	}
	
	

		
	public static void main(String[] args){
		
	}

	/*SQL statements:
		DROP TABLE IF EXISTS `wijn`.`producers`;
	CREATE TABLE  `wijn`.`producers` (
	  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
	  `name` varchar(255) NOT NULL,
	  `address` varchar(255) NOT NULL,
	  `email` varchar(255) NOT NULL,
	  `telephone` varchar(255) NOT NULL,
	  `source` varchar(255) NOT NULL,
	  `sourceid` varchar(45) NOT NULL,
	  `visiting` varchar(255) NOT NULL,
	  `countrycode` varchar(2) NOT NULL,
	  `lat` double NOT NULL,
	  `lon` double NOT NULL,
	  `accuracy` int(10) unsigned NOT NULL DEFAULT '0',
	  `fulltextsearch` varchar(255) NOT NULL,
	  PRIMARY KEY (`id`),
	  FULLTEXT KEY `fulltextindex` (`fulltextsearch`)
	) ENGINE=MyISAM AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
	insert into producers (name,address,email,telephone,source,sourceid,visiting,countrycode,lat,lon,accuracy,fulltextsearch) select namefull1, concat(address,', ',communecedex),email,telephone,'producershachette',id,visiting,'FR',lat,lon,accuracy,addresscomplete from producershachette order by id;
	insert into producers (name,address,email,telephone,source,sourceid,visiting,countrycode,lat,lon,accuracy,fulltextsearch) select name,address, email,telephone,'producersitalianwinehub',id,'','IT',lat,lon,accuracy,name from producersitalianwinehub order by id;
	
	 */


}
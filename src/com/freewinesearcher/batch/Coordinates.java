package com.freewinesearcher.batch;

import java.sql.Connection;
import java.sql.ResultSet;


import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Variables;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.common.Wijnzoeker;
import com.freewinesearcher.online.Webroutines;

public class Coordinates {

	static String Yahoo="UBv0X4nV34HV8LAuLZ44PWUGyhAPLSrQeFK7rC02sutFo9c.mlZOZtYn3Pm0fdZDL1j0TQ--";
	static String Google="ABQIAAAAuPfgtY5yGQowyqWw-A_zlhSWbZnLUFTo4QI7JJ99u1XlFae5FRTIvNMt7rPlWIt8z9eFfYAtlQwM1g";


	public static void getCoordinatesWsShops(){
		String query;
		String address;
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from wsshops where accuracy=0 and address!='No address given.' and country='Netherlands';";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				result=getCoordinates(rs.getString("address"));
				if (!result[2].equals("0")&&!result[0].equals("")) Dbutil.executeQuery("update wsshops set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";",con);
				doPause(2000);
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}
		Dbutil.closeConnection(con);

	}

	public static void getCoordinatesRegions(){
		String query;
		String address;
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from regions where lon =0 and lft>=892 and rgt<=2129;";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String region=rs.getString("region").replace(" Grand Cru", "").replace(" 1er Cru", "").replace("C�tes du Rh�ne Villages ", "");
				String parent=rs.getString("parent").replace(" Grand Cru", "").replace(" 1er Cru", "").replace("C�tes du Rh�ne Villages ", "");
				result=getCoordinates(region+", "+parent+", France");
				if (!result[2].equals("0")&&!result[0].equals("")) {
					Dbutil.executeQuery("update regions set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";",con);
				} else {
					result=getCoordinates(region+", France");
					if (!result[2].equals("0")&&!result[0].equals("")) {
						Dbutil.executeQuery("update regions set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";",con);
					} else {
						Dbutil.logger.info("Not found: "+region+", "+parent+", France");
					}

				}
				doPause(2000);
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}
		Dbutil.closeConnection(con);

	}



	public static void getCoordinatesShops(){
		String query;
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from shops where (accuracy=0 or address='');";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String address=rs.getString("address");
				if (address.equals("")){
					String url=Webroutines.getRegexPatternValue("([^.]+\\.[^.]+)$", rs.getString("shopurl"));
					query="select * from wsshops where url like '%"+url+"%';";
					rs2=Dbutil.selectQuery(query, con);
					if (rs2.next()){
						address=rs2.getString("address")+" "+rs2.getString("country");
						if (rs2.next()){ //2 matches
							url=Webroutines.getRegexPatternValue("([^.]+\\.[^.]+\\.[^.]+)$", rs.getString("shopurl"));
							query="select * from wsshops where url like '%"+url+"%';";
							rs2=Dbutil.selectQuery(query, con);
							if (rs2.next()){
								address=rs2.getString("address")+" "+rs2.getString("country");
								if (rs2.next()){ //again 2 matches???
									address="";
								}else {
									Dbutil.executeQuery("update shops set address='"+Spider.SQLEscape(address)+"' where id="+rs.getInt("id"));
								}
							}
						} else {
							Dbutil.executeQuery("update shops set address='"+Spider.SQLEscape(address)+"' where id="+rs.getInt("id"));
						}
					}
				}
				if (!address.equals("")){
					result=getCoordinates(address);
					if (result[2].equals("0")) {
						doPause(2000);
						result=getCoordinates(address.replaceAll(" "+rs.getString("countrycode")+"-"," ").replace("-"," "));
					}
					if (result[2].equals("0")) {
						doPause(2000);
						result=getCoordinates(address.replaceAll(" "+rs.getString("countrycode")," ").replace("-"," "));
					}
					if (result[2].equals("0")) {
						doPause(2000);
						result=getCoordinates(address.replaceAll(" and ","").replaceAll("\\d+","").replace("-"," "));
					}
					if (result[2].equals("0")) {
						doPause(2000);
						result=getCoordinates(Webroutines.getRegexPatternValue(",(.*)$", address).replaceAll(" "+rs.getString("countrycode")+"-"," ").replace("-"," "));
					}
					if (!result[2].equals("0")&&!result[0].equals("")) {
						Dbutil.executeQuery("update shops set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";",con);
					} else {
						Dbutil.logger.info("Could not find address:"+address);
					}
					doPause(2000);
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}

	public static void getCoordinatesProducersHachette(){
		String query;
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from producershachette where (accuracy<6);";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String address=rs.getString("address")+" "+rs.getString("communecedex")+" France";
				if (!address.equals("")){
					result=getCoordinates(address);
					if (result[2].equals("0")) {
						doPause(800);
						address=rs.getString("communecedex").replaceAll("\\d\\d\\d\\d\\d ", "")+" France";
						result=getCoordinates(address);
					}
					if (result[2].equals("0")) {
						doPause(800);
						address=rs.getString("zip")+" France";
						result=getCoordinates(address);
					}
					doPause(800);
					if (!result[2].equals("0")){
						Dbutil.logger.info("update producershachette set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
						Dbutil.executeQuery("update producershachette set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
					}
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}

	public static void getCoordinatesItalianWineHub(){
		String query;
		ResultSet rs;
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from producersitalianwinehub where (accuracy=0);";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String address=rs.getString("address");
				if (!address.endsWith("Italy")) address+=" Italy";
				if (!address.equals("")){
					result=getCoordinates(address);
					doPause(800);
					if (!result[2].equals("0")){
						Dbutil.logger.info("update producersitalianwinehub set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
						Dbutil.executeQuery("update producersitalianwinehub set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
					}
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}

	public static void getCoordinatesVintnerweb(){
		String query;
		ResultSet rs;
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from producersvintnerweb where (accuracy=0) and addresscomplete like '%Spain';";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String address=rs.getString("address");
				if (!address.equals("")){
					result=getCoordinates(address);
					doPause(100);
					if (!result[2].equals("0")){
						Dbutil.logger.info("update producersvintnerweb set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
						Dbutil.executeQuery("update producersvintnerweb set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
					}
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}

	public static void getCoordinatesRalph(){
		String query;
		ResultSet rs;
		String[] result;
		Connection con=Dbutil.openNewConnection();
		try{
			query="select * from producers where (accuracy=0);";
			rs=Dbutil.selectQuery(query, con);
			while (rs.next()){
				String address=rs.getString("address");
				if (!address.equals("")){
					result=getCoordinates(address);
					doPause(100);
					if (!result[2].equals("0")){
						Dbutil.logger.info("update producers set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
						Dbutil.executeQuery("update producers set lon="+result[0]+", lat="+result[1]+", accuracy="+result[2]+" where id="+rs.getInt("id")+";");
					}
				}
			}
		}catch (Exception e){
			Dbutil.logger.error("",e);


		}




		Dbutil.closeConnection(con);
	}



	public static String[] getCoordinates(String address){
		String query;
		ResultSet rs;
		ResultSet rs2;
		String fulltext="";
		String[] result={"0","0","0"};
		Webpage webpage=new Webpage();
		webpage.errorpause=5;
		webpage.maxattempts=2;
		webpage.encoding="UTF-8";
		try{
			String lon;
			String lat;
			String acc;
			String page;
			address=Webroutines.URLEncodeUTF8(Spider.unescape(address));
			webpage.urlstring="http://local.yahooapis.com/MapsService/V1/geocode?appid=UBv0X4nV34HV8LAuLZ44PWUGyhAPLSrQeFK7rC02sutFo9c.mlZOZtYn3Pm0fdZDL1j0TQ--&location="+address;
			webpage.readPage();
			page=webpage.html;
			if (page.startsWith("Webpage")){
				Dbutil.logger.info("Error in page");
			}
			lon=Webroutines.getRegexPatternValue("<Longitude>([^<]+)", page);
			lat=Webroutines.getRegexPatternValue("<Latitude>([^<]+)", page);
			acc=Webroutines.getRegexPatternValue("Result precision=\"([^\"]+)\"", page);
			if (acc.equals("address")||acc.equals("street")){
				result[0]=lon;
				result[1]=lat;
				result[2]="8";

			} else if (!lon.equals("")){
				result[0]=lon;
				result[1]=lat;
				result[2]="4";

			}
			if (!"8".equals(result[2])){ 
				webpage.urlstring="http://maps.google.com/maps/geo?q="+address+"&output=xml&key=ABQIAAAAuPfgtY5yGQowyqWw-A_zlhSWbZnLUFTo4QI7JJ99u1XlFae5FRTIvNMt7rPlWIt8z9eFfYAtlQwM1g";
				webpage.readPage();
				page=webpage.html;
				if (page.startsWith("Webpage")){
					Dbutil.logger.info("Error in page");
				}
				lon=Webroutines.getRegexPatternValue("<coordinates>([^<,]+),[^<,]+,", page);
				lat=Webroutines.getRegexPatternValue("<coordinates>[^<,]+,([^<,]+),", page);
				acc=Webroutines.getRegexPatternValue("Accuracy=\"(\\d+)\"", page);
				if (acc.equals("")) acc="0";
				if (!acc.equals("0")){
					if ("".equals(result[2])||Integer.parseInt(acc)>4){
						result[0]=lon;
						result[1]=lat;
						result[2]=acc;
					}
				}
			}



		}catch (Exception e){
			Dbutil.logger.error("",e);
		}
		return result;
	}



	public static void doPause(int iTimeInMilliSeconds){
		try{
			Thread.sleep(iTimeInMilliSeconds);
		}catch (Exception e){}
		/*
		long t0, t1;
		//System.out.println("timer start");
		t0=System.currentTimeMillis( );
		t1=System.currentTimeMillis( )+(iTimeInMilliSeconds);

		//System.out.println("T0: "+t0);
		//System.out.println("T1: "+t1);

		do {
			t0=System.currentTimeMillis( );

		} while (t0 < t1);
		 */
		//System.out.println("timer end");

	}
	
	public static void main(String[] args){
		Wijnzoeker wijnzoeker=new Wijnzoeker();
		getCoordinatesRalph();
	}


}
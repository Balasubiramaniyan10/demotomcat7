package com.freewinesearcher.online;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;

public class Regionpois {
	public Set<RegionPoi> pois=new LinkedHashSet<RegionPoi>();
	private int numberofregions;
	private int maxnumberofregions=200;

	public Regionpois(Bounds bounds){
		if (bounds!=null){
			String query="";
			ResultSet rs=null;
			Connection con=Dbutil.openNewConnection();

			try{
				query="select SQL_CALC_FOUND_ROWS * from kbregionhierarchy where lat<"+bounds.latmax+" and lat>"+bounds.latmin+" and lon<"+bounds.lonmax+" and lon>"+bounds.lonmin+" order by lft;";
				rs=Dbutil.selectQuery(query, con);
				//Dbutil.logger.info(query);
				if (rs.last()){
					numberofregions=rs.getRow()+1;
					rs.beforeFirst();
				}
				if (numberofregions<maxnumberofregions+1){
					while (rs.next()){
						pois.add(new RegionPoi(rs.getInt("id"),rs.getString("region"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getString("shortregion"), rs.getString("shortregion")));
					}
				}
				Dbutil.closeRs(rs);

			} catch (Exception exc){
				Dbutil.logger.error("Problem while looking up regions",exc);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
		}	
	}

	public Regionpois(Bounds bounds, int maxpois){
		if (bounds!=null){
			String query="";
			ResultSet rs=null;
			Connection con=Dbutil.openNewConnection();
			LinkedHashSet<RegionPoi> currentpois = new LinkedHashSet<RegionPoi>();
			LinkedHashSet<RegionPoi> higherpois = new LinkedHashSet<RegionPoi>();
			try{
				query="select SQL_CALC_FOUND_ROWS *,(length(region)-length(replace(region,',',''))) as level,((rgt-lft)=1) as lowest from kbregionhierarchy where lat<"+bounds.latmax+" and lat>"+bounds.latmin+" and lon<"+bounds.lonmax+" and lon>"+bounds.lonmin+" and not (lon=0 and lat=0) order by level desc,lowest desc,shortregion desc;";
				rs=Dbutil.selectQuery(query, con);
				// deepest level first.
				int curlevel=10;
				while (rs.next()){
					if (curlevel==10) curlevel=rs.getInt("level"); // set the level to start with
					
					if (curlevel==rs.getInt("level")){ // same level, add pois
						currentpois.add(new RegionPoi(rs.getInt("id"),rs.getString("region"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getString("shortregion"), rs.getString("shortregion")));
					} else 	if (curlevel==1+rs.getInt("level")){
						// we are on the next level. Count the current number and if bigger than required, remove all lower levels and set new level
						if (currentpois.size()>maxpois) {
							currentpois=new LinkedHashSet<RegionPoi>();
							higherpois=new LinkedHashSet<RegionPoi>();
							curlevel=rs.getInt("level");
							currentpois.add(new RegionPoi(rs.getInt("id"),rs.getString("region"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getString("shortregion"), rs.getString("shortregion")));
						}
						if (rs.getBoolean("lowest")){ // Move to final collection
							higherpois.add(new RegionPoi(rs.getInt("id"),rs.getString("region"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getString("shortregion"), rs.getString("shortregion")));						
						} 

					} else {
						rs.last();
						
					}
				}
				Dbutil.closeRs(rs);

			} catch (Exception exc){
				Dbutil.logger.error("Problem while looking up regions",exc);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
				pois.addAll(currentpois);
				if (currentpois.size()+higherpois.size()<=maxpois){
					pois.addAll(higherpois);
				}
			}
		}	
	}

	public String getAsJSON(){
		JSONArray poiset=new JSONArray();
		JSONObject j;
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document>  <name>The Googleplex</name>  <description><![CDATA[Photos of life at Google Headquarters]]></description>  <Style id=\"style8\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style3\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style23\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style16\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style24\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style21\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style15\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style10\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style9\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style20\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style7\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style13\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style14\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style18\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style17\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style6\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style1\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style25\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style22\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style4\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style12\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style2\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style19\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style11\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style5\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Placemark>    <name>Welcome to the Googleplex building 43!</name>    <description><![CDATA[<IMG height=\"288\" src=\"http://lh6.google.com/image/mapshop.maps/RhCAmQmEHdI/AAAAAAAABNs/wjDQJqi-mHc/IMGP0241.JPG?imgmax=288\" width=\"205\">]]></description>    <styleUrl>#style8</styleUrl>    <Point>      <coordinates>-122.084038,37.421738,0.000000</coordinates>    </Point>  </Placemark>");
		int i=0;
		for (RegionPoi p:pois){

			if (i<1000)	{
				try {
					j=new JSONObject();
					j.put("p",new JSONArray("["+p.getLat()+","+p.getLon()+"]"));
					j.put("n", p.getLabelText());
					j.put("id", "r"+p.getId());
					poiset.put(i, j);
				} catch (JSONException e) {
					Dbutil.logger.error("Json problem",e);
				}
				//sb.append("<h3 onclick=\"map.openInfoWindow(new GLatLng("+p.lat+","+p.lon+"),document.createTextNode('"+p.name+"'));\">"+p.name+"</h3>");
				i++;
			}
		}

		//Dbutil.logger.info(sb);
		return "{\"regions\":"+poiset.toString();
	}

	public StringBuffer getAsKml(){
		StringBuffer sb=new StringBuffer();
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.2\"><Document>  <name>The Googleplex</name>  <description><![CDATA[Photos of life at Google Headquarters]]></description>  <Style id=\"style8\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style3\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style23\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style16\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style24\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style21\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style15\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style10\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style9\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style20\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style7\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style13\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style14\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style18\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style17\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style6\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style1\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style25\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style22\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style4\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style12\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style2\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style19\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style11\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Style id=\"style5\">    <IconStyle>      <Icon>        <href>http://maps.google.com/mapfiles/ms/icons/blue-dot.png</href>      </Icon>    </IconStyle>  </Style>  <Placemark>    <name>Welcome to the Googleplex building 43!</name>    <description><![CDATA[<IMG height=\"288\" src=\"http://lh6.google.com/image/mapshop.maps/RhCAmQmEHdI/AAAAAAAABNs/wjDQJqi-mHc/IMGP0241.JPG?imgmax=288\" width=\"205\">]]></description>    <styleUrl>#style8</styleUrl>    <Point>      <coordinates>-122.084038,37.421738,0.000000</coordinates>    </Point>  </Placemark>");
		int i=0;
		for (RegionPoi p:pois){
			i++;
			//if (i<1000)	sb.append("<PhotoOverlay><Icon><href>"+Configuration.staticprefix+"/text2gif/?region="+p.getId()+"</href></Icon><name><![CDATA["+p.labelText+"]]></name><description><![CDATA["+p.getHTML()+"]]></description><Point><coordinates>"+p.getLon()+","+p.getLat()+"</coordinates></Point></PhotoOverlay>");
			if (i<1000)	sb.append("<Placemark><styleUrl>#noPlacemark</styleUrl> <name><![CDATA["+p.labelText+"]]></name><description><![CDATA["+p.getHTML()+"]]></description><Point><coordinates>"+p.getLon()+","+p.getLat()+"</coordinates></Point></Placemark>");
		}
		return sb;
	}
}

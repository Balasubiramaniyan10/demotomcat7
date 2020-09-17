package com.freewinesearcher.online;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.freewinesearcher.batch.Emailer;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;

public class Shopapplication implements Serializable{

	private static final long serialVersionUID = 1L;
	private int id;
	private int shopid=0;
	private int version=0;
	
	private String status;
	private String password;
	private String country="";
	private String shopname="";
	private String address="";
	private String contactname="";
	private String storeemailaddressforvp="";
	private String storeemailaddressforcustomers="";
	private String storephonenumber="";
	private String storegeneraldescription="";
	private boolean onlineordering;
	private boolean includingvat;
	private String percentagesalestax="";
	private String[] shiptostates=new String[50];
	private boolean global;
	private String countriesstatesshippingto="";
	private boolean physical;
	private String storeaddress="";
	private String urlhomepage="";
	private String urldatafeed="";
	private String urlbrowsethrough="";
	private boolean acknowledgeTandCY;
	private String urlforvplink="";
	private boolean interestinadvertisingY;
	private boolean affiliateprogramY;
	private String affiliateprogram="";
	private String questions="";
	public Timestamp lastupdated=null;
	public String comment="";
	private boolean bevmedia=false;

	static final String INSERT_OBJECT_SQL = "INSERT INTO shopapplication(id,shopid,status, date,application) VALUES (?,?,?,sysdate(),?)";
	static final String INSERT_IGNORE_OBJECT_SQL = "INSERT IGNORE INTO shopapplication(id,shopid,status, date,application) VALUES (?,?,?,sysdate(),?) on duplicate key UPDATE application=?, status=?, shopid=if(shopid>0,shopid,?),date=sysdate() ";
	static final String READ_OBJECT_SQL = "SELECT * FROM shopapplication WHERE id = ?";

	public long save(){
		if (passwordOK()){
			ByteArrayOutputStream baos;
			ObjectOutputStream out;
			baos = new ByteArrayOutputStream();
			try {
				out = new ObjectOutputStream(baos);
				out.writeObject(this);
				out.close();
			} catch (Exception e) {
				Dbutil.logger.error("Problem:",e);
			}
			byte[] byteObject = baos.toByteArray();



			long resultid=0;
			Connection con=Dbutil.openNewConnection();
			ResultSet rs=null;

			String className;
			PreparedStatement pstmt;

			try {
				con.setAutoCommit(false);
				pstmt = con.prepareStatement(INSERT_IGNORE_OBJECT_SQL);
				//pstmt = con.prepareStatement(INSERT_OBJECT_SQL);
				// set input parameters
				pstmt.setInt(1, id);
				pstmt.setInt(2, shopid);
				if (status!=null&&!status.equals("")){
					pstmt.setString(3, status);
				} else {
					pstmt.setString(3, "new");
				}
				pstmt.setObject(4, byteObject);
				pstmt.setObject(5, byteObject);
				pstmt.setString(6, status);
				pstmt.setInt(7, shopid);
				resultid=pstmt.executeUpdate();
				if (resultid==0) Dbutil.logger.error("Problem: Could not update object with id "+id);



			} catch (Exception e) {
				Dbutil.logger.error("Problem: ", e);
			} finally{
				Dbutil.closeRs(rs);
				Dbutil.closeConnection(con);
			}
			return resultid;
		}
		return 0;
	}

	public static Shopapplication retrieve(long id)  {
		Connection con=Dbutil.openNewConnection();
		ResultSet rs=null;
		Shopapplication object=null;
		byte[] byteObject=null;
		try{
			PreparedStatement pstmt = con.prepareStatement(READ_OBJECT_SQL);
			pstmt.setLong(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()){
				byteObject = rs.getBytes("application");
				ByteArrayInputStream bais;
				ObjectInputStream in;
				bais = new ByteArrayInputStream(byteObject);
				in = new ObjectInputStream(bais);
				object = ((Shopapplication)in.readObject());
				object.lastupdated=rs.getTimestamp("date");
				in.close();
			}
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
		} finally{
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return (Shopapplication)object;
	}
	public static Shopapplication generate(long id) throws Exception {
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		Shopapplication s=new Shopapplication();
		try {
			query = "select ws.*, snoothshops.email from (select * from wsshops ws where ws.wsid="+id+" and ws.storetype like '%Retail%' and url not like 'http://www.wineaccess.com%') ws left join snoothshops on (ws.url=snoothshops.url);";
			rs = Dbutil.selectQuery(rs, query, con);
			if (rs.next()) {
				s.id=rs.getInt("wsid");
				s.country=rs.getString("country");
				if (s.country!=null&&s.country.contains("USA (")) s.country="USA";
				s.password=s.showPassword();
				s.shopname=rs.getString("name");
				s.urlhomepage=rs.getString("url");
				s.address=rs.getString("address");
				s.setBevmedia(rs.getBoolean("bevmedia"));
				if (rs.getString("email")!=null) s.storeemailaddressforcustomers=rs.getString("email");
				if (rs.getString("email")!=null) s.storeemailaddressforvp=rs.getString("email");
				if (rs.getString("Internet").contains("ordering")) s.onlineordering=true;
				s.storegeneraldescription="Information for people who plan to visit your store. Most of the time they are interested in things like: what wine regions you specialize in, for how long have you been in business, number of employees, how big is your store, discount programs, etc. This is an opportunity to tell new customers about your store. ";
				//Dbutil.logger.info(s.shopname+" "+s.storeemailaddressforvp);

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
		return s;
	}
	public boolean shipstostate(String state){
		for (String el:getShiptostates()){
			if (state.equals(el)) return true;
		}
		return false;
	}

	public boolean passwordOK(){
		if ((("s"+id).hashCode()+"").equals(getPassword())) return true;
		return false;
	}

	public String showPassword(){
		return ("s"+id).hashCode()+"";
	}

	public String getComment() {
		if (comment==null) comment="";
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getShopid() {
		return shopid;
	}

	public void setShopid(int shopid) {
		this.shopid = shopid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getShopname() {
		return shopname;
	}

	public void setShopname(String shopname) {
		this.shopname = shopname;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getStoreemailaddressforvp() {
		return storeemailaddressforvp;
	}

	public void setStoreemailaddressforvp(String storeemailaddressforvp) {
		this.storeemailaddressforvp = storeemailaddressforvp;
	}

	public String getStoreemailaddressforcustomers() {
		return storeemailaddressforcustomers;
	}

	public void setStoreemailaddressforcustomers(
			String storeemailaddressforcustomers) {
		this.storeemailaddressforcustomers = storeemailaddressforcustomers;
	}

	public String getStorephonenumber() {
		return storephonenumber;
	}

	public void setStorephonenumber(String storephonenumber) {
		this.storephonenumber = storephonenumber;
	}

	public String getStoregeneraldescription() {
		return storegeneraldescription;
	}

	public void setStoregeneraldescription(String storegeneraldescription) {
		this.storegeneraldescription = storegeneraldescription;
	}

	public boolean isOnlineordering() {
		return onlineordering;
	}

	public void setOnlineordering(boolean onlineordering) {
		this.onlineordering = onlineordering;
	}

	public boolean isIncludingvat() {
		return includingvat;
	}

	public void setIncludingvat(boolean includingvat) {
		this.includingvat = includingvat;
	}

	public String getPercentagesalestax() {
		return percentagesalestax;
	}

	public void setPercentagesalestax(String percentagesalestax) {
		this.percentagesalestax = percentagesalestax;
	}

	public String[] getShiptostates() {
		return shiptostates;
	}

	public void setShiptostates(String[] shiptostates) {
		this.shiptostates = shiptostates;
	}


	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public String getCountriesstatesshippingto() {
		return countriesstatesshippingto;
	}

	public void setCountriesstatesshippingto(String countriesstatesshippingto) {
		this.countriesstatesshippingto = countriesstatesshippingto;
	}

	public boolean isPhysical() {
		return physical;
	}

	public void setPhysical(boolean physical) {
		this.physical = physical;
	}

	public String getStoreaddress() {
		return storeaddress;
	}

	public void setStoreaddress(String storeaddress) {
		this.storeaddress = storeaddress;
	}

	public String getUrlhomepage() {
		return urlhomepage;
	}

	public void setUrlhomepage(String urlhomepage) {
		this.urlhomepage = urlhomepage;
	}

	public String getUrldatafeed() {
		return urldatafeed;
	}

	public void setUrldatafeed(String urldatafeed) {
		this.urldatafeed = urldatafeed;
	}

	public String getUrlbrowsethrough() {
		return urlbrowsethrough;
	}

	public void setUrlbrowsethrough(String urlbrowsethrough) {
		this.urlbrowsethrough = urlbrowsethrough;
	}

	public boolean isAcknowledgeTandCY() {
		return acknowledgeTandCY;
	}

	public void setAcknowledgeTandCY(boolean acknowledgeTandCY) {
		this.acknowledgeTandCY = acknowledgeTandCY;
	}

	public String getUrlforvplink() {
		return urlforvplink;
	}

	public void setUrlforvplink(String urlforvplink) {
		this.urlforvplink = urlforvplink;
	}

	public boolean isInterestinadvertisingY() {
		return interestinadvertisingY;
	}

	public void setInterestinadvertisingY(boolean interestinadvertisingY) {
		this.interestinadvertisingY = interestinadvertisingY;
	}

	public boolean isAffiliateprogramY() {
		return affiliateprogramY;
	}

	public void setAffiliateprogramY(boolean affiliateprogramY) {
		this.affiliateprogramY = affiliateprogramY;
	}

	public String getAffiliateprogram() {
		return affiliateprogram;
	}

	public void setAffiliateprogram(String affiliateprogram) {
		this.affiliateprogram = affiliateprogram;
	}

	public String getCountry() {
		if (country==null) return "";
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setQuestions(String questions) {
		this.questions = questions;
	}

	public String getQuestions() {
		return questions;
	}

	public void setContactname(String contactname) {
		this.contactname = contactname;
	}

	public String getContactname() {
		return contactname;
	}
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}


	public static int getnextid(int skip){
		return Dbutil.readIntValueFromDB("select ws.*, snoothshops.email,shopapplication.id from (select * from wsshops ws where ws.country like 'USA%' and ws.storetype like '%Retail%' and url not like 'http://www.wineaccess.com%' and url not like '' order by ws.numberofwines desc limit 100,5000) ws left join snoothshops on (ws.url=snoothshops.url) left join shopapplication on (ws.wsid=shopapplication.id) having shopapplication.id is null order by ws.numberofwines desc limit "+skip+",1;","wsid");
	}
	
	public static String sentwithtextbox(int id, String comment){
		if (retrieve(id)==null){
			try {
				Shopapplication s;
				s = Shopapplication.generate(id);
				s.comment=comment;
				s.status="Message box";
				String urlstring="<font color='red'>Url for "+s.getShopname()+" is https://www.vinopedia.com/getstoreinvitation.jsp?id="+id+"&version=1&password="+s.showPassword()+"</font><br/>";
				if (s.save()>0) return urlstring;
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return "Could not save the form!";
	}

	public static boolean mailRequest(int id, String email, String comment){
		if (retrieve(id)==null){
			try {
				Shopapplication s;
				s = Shopapplication.generate(id);
				s.storeemailaddressforvp=email;
				s.storeemailaddressforcustomers=email;
				s.comment=comment;
				Webpage webpage=new Webpage();
				webpage.urlstring="https://www.vinopedia.com/getstoreinvitation.jsp?id="+id+"&version=1&password="+s.showPassword()+"&storeemailaddressforvp="+email;
				webpage.readPage();
				Emailer emailer=new Emailer();
				if (emailer.sendEmail("jeroen@vinopedia.com",email, "Listing of "+Webroutines.getRegexPatternValue("name=\"shopname\" type=\"text\" value=\"([^\"]+)\"", webpage.html)+ " on vinopedia.com" , webpage.html)){
					emailer.sendEmail("jeroen@vinopedia.com","jeroen@vinopedia.com", "Listing of "+Webroutines.getRegexPatternValue("name=\"shopname\" type=\"text\" value=\"([^\"]+)\"", webpage.html)+ " on vinopedia.com" , webpage.html);
					s.save();
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;
	}

	public static void main (String[] args){
		Shopapplication s=new Shopapplication();
		s.id=4161;
		Dbutil.logger.info(s.showPassword());
		String query;
		ResultSet rs = null;
		Connection con = Dbutil.openNewConnection();
		try {
			query = "select ws.*, snoothshops.email from (select * from wsshops ws where ws.country like 'USA%' and ws.storetype like '%Retail%' and url not like 'http://www.wineaccess.com%' order by ws.numberofwines desc limit 100,100) ws left join snoothshops on (ws.url=snoothshops.url);";
			rs = Dbutil.selectQuery(rs, query, con);
			while (rs.next()) {
				s=new Shopapplication();
				s.id=rs.getInt("wsid");
				s.password=s.showPassword();
				s.shopname=rs.getString("name");
				s.urlhomepage=rs.getString("url");
				s.address=rs.getString("address");
				if (rs.getString("email")!=null) s.storeemailaddressforcustomers=rs.getString("email");
				if (rs.getString("email")!=null) s.storeemailaddressforvp=rs.getString("email");
				if (rs.getString("Internet").contains("ordering")) s.onlineordering=true;
				Dbutil.logger.info(s.shopname+" "+s.storeemailaddressforvp);

			}
		} catch (Exception e) {
			Dbutil.logger.error("", e);
		} finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}
	}

	public void setBevmedia(boolean bevmedia) {
		this.bevmedia = bevmedia;
	}

	public boolean isBevmedia() {
		return bevmedia;
	}



}

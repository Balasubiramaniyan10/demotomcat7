package com.freewinesearcher.online;

import com.freewinesearcher.common.Dbutil;

public class Partner {
	public String name;
	public String address;
	//public String representative;
	public String email;
	public int payterm; // In how many days should they pay their bills?
	
		
	public Partner(int id){
		name=Dbutil.readValueFromDB("Select * from partners where id="+id+";", "name");
		address=Dbutil.readValueFromDB("Select * from partners where id="+id+";", "address");
		//representative=Dbutil.readValueFromDB("Select * from partners where id="+id+";", "representative");
		email=Dbutil.readValueFromDB("Select * from partners where id="+id+";", "email");
		payterm=Dbutil.readIntValueFromDB("Select * from partners where id="+id+";", "payterm");
	}
	
	public static int getIDFromShopId(String shopid){
		if (shopid.equals("")) return 0;
		return Dbutil.readIntValueFromDB("select * from partners where shopid="+shopid+";", "id");
		
	}
	
}

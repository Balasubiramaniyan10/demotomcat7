package com.freewinesearcher.online.web20;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.web20.Subject;

public class WineSubject extends Subject {
	public int vintage;
	public static Exception IllegalVintageException=new Exception();
	
	
	public WineSubject(int knownwineid,int vintage) throws Exception{
		super(Subject.Types.WINE,knownwineid);
		this.vintage=vintage;
		if (vintage==0){
			throw IllegalVintageException;
		}
	} 
	
	public String getSubjectInfo(){
		return (Dbutil.readValueFromDB("select * from knownwines where id="+rowid, "wine")+" "+vintage);
		
	}
}

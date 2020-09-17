package com.freewinesearcher.online.web20;

import java.io.Serializable;

import com.freewinesearcher.common.Dbutil;

public class Subject implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static Exception IllegalSubjectException=new Exception();
	public static enum Types {WINE,PRODUCER,STORE};
	public Types type;
	public int rowid;

	public Types getType() {
		return type;
	}

	public void setType(Types type) {
		this.type = type;
	}

	public int getRowid() {
		return rowid;
	}

	public void setRowid(int rowid) {
		this.rowid = rowid;
	}

	public Subject (Types type,int rowid) throws Exception{
		try{
			this.type=type;
			this.rowid=rowid;
			if (rowid==0){
				throw IllegalSubjectException;
			}
		} catch (Exception e){
			throw IllegalSubjectException;
		}
	}
	
	public Subject (String type,String rowidstr) throws Exception{
		this(Types.valueOf(type.toUpperCase()),Integer.parseInt(rowidstr));
	}
	
	public String getSubjectInfo(){
		switch (type){
			case PRODUCER:
				return Dbutil.readValueFromDB("select * from producers where id="+rowid, "name");
			case STORE:
				return Dbutil.readValueFromDB("select * from shops where id="+rowid, "shopname");
			
		}
		return "";
	}

}

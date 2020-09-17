package com.freewinesearcher.common;

import javax.servlet.http.HttpServletRequest;

import com.freewinesearcher.batch.Spider;
import com.searchasaservice.parser.xpathparser.Analyzer2;
import com.searchasaservice.parser.xpathparser.Result;

public class Context {
	public int tenant=0;
	public Analyzer2 an=null;
	public Result newresult;
	public String userid="";
	public Context(int tenant){
		this.tenant=tenant;
	}
	
	public Context(HttpServletRequest request) {
		if (request.getServerName()!=null&&(request.getServerName().toLowerCase().contains("vinopedia")||request.getServerName().toLowerCase().contains("localhost"))){
			tenant=1;
		} else {
			Wijnzoeker wijnzoeker=new Wijnzoeker();
			tenant=wijnzoeker.tenantid;
		}
		userid=request.getRemoteUser();
		if (userid==null)  userid="";
	}
	
	public String getEmail(){
		return Dbutil.readValueFromDB("select * from users where username='"+Spider.SQLEscape(userid)+"';", "email");
	}
	
}

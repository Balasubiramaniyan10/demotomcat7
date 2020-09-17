package com.freewinesearcher.online;

import javax.servlet.http.HttpServletRequest;
	
public class Authorization {
	String username="";
	int shopid=0;
	int partnerid=0;
	String domain="";
	
	public Authorization(HttpServletRequest request){
		username=request.getRemoteUser();
		shopid=Webroutines.getShopFromUserId(request.getRemoteUser());
		partnerid=Webroutines.getPartnerFromUserId(request.getRemoteUser());
	}
	
	public boolean checkDomain(String domain){
		boolean isAuthorized=false;
		String email=Webroutines.getEmail(username);
		try{
			domain=domain.toLowerCase();
			if (email.contains(":")) email=email.split(":")[1];
			String emaildomain=email.substring(email.indexOf("@")+1).toLowerCase();
			if (!email.contains("gmail.com")&&!email.contains("yahoo.com")&&!email.contains("aol.com")&&!email.contains("hotmail.com")&&!email.contains("lycos.com")){
				if (domain.startsWith("www.")) domain=domain.substring(4);
				if (emaildomain.endsWith(domain)) isAuthorized=true;
			}
		}catch (Exception e){}
		return isAuthorized;
	}

	public boolean checkShopId(int reqshopid){
		if (reqshopid==shopid) return true;
		return false;
	}
	
}

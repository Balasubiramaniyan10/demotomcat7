package com.freewinesearcher.api2;

import org.apache.commons.codec.digest.DigestUtils;

import com.freewinesearcher.api.ApiConfig;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;

public class TestApi {
	static String testclient="6e07bc25d94a88048ecdb19735a38544";
	
	public static void main(String[] args){
		
		Webpage w=new Webpage();
		w.maxattempts=1;
		long time=System.currentTimeMillis();
		//Dbutil.logger.info(DigestUtils.md5Hex(time+""));
		//Dbutil.logger.info(DigestUtils.md5Hex(time+"vwfgwefgewfe"));
		//w.urlstring="https://www.vinopedia.com/api/?action=price&name=aaassfsf&vintage=2001&currency=usd&clientid="+testclient+"&timestamp="+time+"&sig="+getSig(time);
		//w.readPage();
		//Dbutil.logger.info(w.html);
		//w=new Webpage();
		//w.maxattempts=1;
		//w.urlstring="https://www.vinopedia.com/api/?action=searchstats&winery=yquem&clientid="+testclient+"&timestamp="+time+"&sig="+getSig(time);
		//w.readPage();
		//Dbutil.logger.info(w.html);
		w=new Webpage();
		w.urlstring="https://test.vinopedia.com/api2/search/?clientid=testbak&version=0.1&key=542334626&format=XML&name=Petrus";
		w.readPage();
		Dbutil.logger.info(w.html);
		
		
	}
	
	public static String getSig(long time){
		StringBuilder buff = new StringBuilder();
		buff.append(testclient).append("\n");
		buff.append("GET").append("\n");
		buff.append(ApiConfig.getClients().get("secret-"+testclient)).append("\n");
		buff.append(Long.toString(time)).append("\n");
		String src = buff.toString().toLowerCase(); 
		String sig = DigestUtils.md5Hex(src);
		//Dbutil.logger.info("Call sig \n"+sig);
		//Dbutil.logger.info("Call  \n"+src);

		return sig;
	}
	
	public static String ExampleCode(long time){
		StringBuilder buff = new StringBuilder();
		buff.append(testclient).append("\n");
		buff.append("GET").append("\n");
		buff.append("Your secret").append("\n");
		buff.append(Long.toString(time)).append("\n");
		String src = buff.toString().toLowerCase(); 
		String sig = DigestUtils.md5Hex(src);
		return sig;
	}
	
	
	
}
